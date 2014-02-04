/*
 * Copyright (c) 2002-2006 The MITRE Corporation
 * 
 * Except as permitted below
 * ALL RIGHTS RESERVED
 * 
 * The MITRE Corporation (MITRE) provides this software to you without
 * charge to use for your internal purposes only. Any copy you make for
 * such purposes is authorized provided you reproduce MITRE's copyright
 * designation and this License in any such copy. You may not give or
 * sell this software to any other party without the prior written
 * permission of the MITRE Corporation.
 * 
 * The government of the United States of America may make unrestricted
 * use of this software.
 * 
 * This software is the copyright work of MITRE. No ownership or other
 * proprietary interest in this software is granted you other than what
 * is granted in this license.
 * 
 * Any modification or enhancement of this software must inherit this
 * license, including its warranty disclaimers. You hereby agree to
 * provide to MITRE, at no charge, a copy of any such modification or
 * enhancement without limitation.
 * 
 * MITRE IS PROVIDING THE PRODUCT "AS IS" AND MAKES NO WARRANTY, EXPRESS
 * OR IMPLIED, AS TO THE ACCURACY, CAPABILITY, EFFICIENCY,
 * MERCHANTABILITY, OR FUNCTIONING OF THIS SOFTWARE AND DOCUMENTATION. IN
 * NO EVENT WILL MITRE BE LIABLE FOR ANY GENERAL, CONSEQUENTIAL,
 * INDIRECT, INCIDENTAL, EXEMPLARY OR SPECIAL DAMAGES, EVEN IF MITRE HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You accept this software on the condition that you indemnify and hold
 * harmless MITRE, its Board of Trustees, officers, agents, and
 * employees, from any and all liability or damages to third parties,
 * including attorneys' fees, court costs, and other related costs and
 * expenses, arising out of your use of this software irrespective of the
 * cause of said liability.
 * 
 * The export from the United States or the subsequent reexport of this
 * software is subject to compliance with United States export control
 * and munitions control restrictions. You agree that in the event you
 * seek to export this software you assume full responsibility for
 * obtaining all necessary export licenses and approvals and for assuring
 * compliance with applicable reexport restrictions.
 */

package org.mitre.ace2004.callisto;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import org.xml.sax.*;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import gov.nist.atlas.*;
import gov.nist.atlas.ref.AnnotationRef;
import gov.nist.atlas.util.ATLASElementSet;

import org.mitre.jawb.atlas.*;
import org.mitre.jawb.gui.GUIUtils;
import org.mitre.jawb.io.*;
import org.mitre.jawb.tasks.*;

public final class ExportAPF5_0_2 implements Exporter {

  private static final String APF_VERSION = "5.0.3";

  private static final int DEBUG = 0;

  public ExportAPF5_0_2() {
    super();
  }

// Exporter
  /**
   * A user readable String to identify the input format. This String should
   * also be returned from toString as it is used in GUI displays.
   */
  public String getFormat () {
    return "APF v"+APF_VERSION;
  }
  
  /**
   * A short description of the import function or <code>null</code>.
   */
  public String getDescription () {
    return "Saves the selected file in APF v"+APF_VERSION+" format.";
  }

  /**
   * Should return the same value from toString as from {@link #getFormat}.
   * This is an implementation side affect, and won't break anything, if not.
   * Implementations which don't, will have a strange name in the dialog.
   */
  public String toString () {
    return getFormat();
  }

  /**
   */
  private boolean writeAPF(Document xmlDoc, URI uri) throws IOException {
    File apfFile = new File(uri);
    File backFile = new File(apfFile.toString()+"~");
    File tmpFile = ATLASHelper.createTempFile(uri);
    if (DEBUG > 0)
      System.err.println("Export APF File: "+apfFile+
                          "\n       TMP File: "+tmpFile);

    FileOutputStream fileOut = new FileOutputStream(tmpFile);
    // always output APF as UTF-8, though the source file may be something else
    Writer writer = new OutputStreamWriter(fileOut,"UTF-8");
    writer = new BufferedWriter(writer);

    OutputFormat format = OutputFormat.createPrettyPrint();
    XMLWriter xmlWriter = new XMLWriter(writer, format);
    xmlWriter.write(xmlDoc);
    xmlWriter.close();

    if (apfFile.exists()) {
      if (backFile.exists())
        backFile.delete();
      apfFile.renameTo(backFile);
    }
    
    return tmpFile.renameTo(apfFile);
  }
  
  
  /**
   * Open and convert the data at the specified URI, to a new
   * AWBDocument. Implementations should verify that the URI specified is
   * absolute. URI's are used due to ambiguities in the URL class.
   *
   * @see URI#isAbsolute
   * @param doc the data to be exported
   * @param uri location to save data to
   * @return true if the export succeded, false otherwise.
   * @throws IOException on error writing to file.
   * @throws IllegalArgumentException implementations should throw an
   *         IllegalArgumentException if the uri specified is of an unsupported
   *         scheme, or if the preconditions of that scheme are not met (file:
   *         and http: protocols require the uri be absolute and heirarchal).
   */
  public boolean exportDocument (AWBDocument awbDoc, URI uri)
      throws IOException {

    // note that this only happens automatically on export of APF
    coreferenceRelations(awbDoc);

    Document xmlDoc = null;
    
    try {
      xmlDoc = DocumentHelper.createDocument();
      xmlDoc.addDocType("source_file", "SYSTEM", "apf.v"+APF_VERSION+".dtd");
      
      String encoding = awbDoc.getEncoding();
      
      // if this document came from import, it will have 'source' (maybe)
      // and 'uri' specified by the 'source_file' tag. keep them.
      String source = (String) awbDoc.getClientProperty(IdTracker.DOC_SOURCE);
      if (source == null)
        source = "unknown";

      URI signalURI = (URI) awbDoc.getClientProperty(IdTracker.DOC_URI);
      if (signalURI == null)
        signalURI = awbDoc.getSignalURI ();
      
      // relativize with destination URI
      String path = uri.getRawPath ();
      URI    base = uri.resolve (path.substring
                                 (0,path.lastIndexOf ('/')+1));
      signalURI = base.relativize (signalURI);

      // what's the point in versioning by X.0 if every X.y is incompatable?
      Element root = xmlDoc.addElement("source_file")
        .addAttribute("TYPE", "text")
        .addAttribute("VERSION", "5.0")
        .addAttribute("SOURCE", source)
        .addAttribute("URI", signalURI.toString())
        .addAttribute("ENCODING", encoding);

      // warning to users reading the xml
      if (!encoding.equals ("UTF-8"))
        root.addComment("This document is encoded in 'UTF-8',"+
                        " the /source/ is encoded in '"+encoding+"'");

      Element doc = root.addElement("document")
        .addAttribute("DOCID", IdTracker.getDocId(awbDoc));

      // don't know the order returned, so make collections
      HashSet entities = new LinkedHashSet();
      HashSet relations = new LinkedHashSet();
      HashSet events = new LinkedHashSet();
      HashSet quantities = new LinkedHashSet();

      Iterator annotIter = awbDoc.getAllAnnotations ();
      while (annotIter.hasNext ()) {
        AWBAnnotation annot = (AWBAnnotation) annotIter.next();
        String type = annot.getAnnotationType().getName();

        if (type.equals(ACE2004Task.ENTITY_TYPE_NAME))
          entities.add(annot);

        else if (type.equals(ACE2004Task.QUANTITY_TYPE_NAME))
          quantities.add(annot);

        else if (type.equals(ACE2004Task.RELATION_TYPE_NAME))
          relations.add(annot);

        else if (type.equals(ACE2004Task.EVENT_TYPE_NAME))
          events.add(annot);
      }

      if (DEBUG > 0) {
        System.err.println("Entities:   "+entities.size());
        System.err.println("Quantities: "+quantities.size());
        System.err.println("Relations:  "+relations.size());
        System.err.println("Events:     "+events.size());
      }
      // iterate over each
      Iterator iterator = null;

      iterator = entities.iterator();
      while (iterator.hasNext())
        addEntity(doc, awbDoc, (HasSubordinates) iterator.next());

      iterator = quantities.iterator();
      while (iterator.hasNext())
        addQuantity(doc, awbDoc, (HasSubordinates) iterator.next());

      iterator = relations.iterator();
      while (iterator.hasNext())
        addRelation(doc, awbDoc, (HasSubordinates) iterator.next());

      iterator = events.iterator();
      while (iterator.hasNext())
        addEvent(doc, awbDoc, (HasSubordinates) iterator.next());

    } catch (Exception t) {
      t.printStackTrace();
    }

    boolean success = writeAPF(xmlDoc, uri);
    if (DEBUG > 0) {
      System.err.println("\nExport succeeded: " + success + "\n");
    }
    return success;
  }

  private void addCharseq(Element element, AWBDocument awbDoc,
                          int start, int end) {
    String text = awbDoc.getSignal().getCharsAt(start, end);

    Element charseq = element.addElement("charseq")
      .addAttribute("START", String.valueOf(start))
      .addAttribute("END", String.valueOf(end-1))
      .addText(text.trim().replaceAll("(\n\r|\n|\r)"," "));
  }

  private void addEntity(Element doc, AWBDocument awbDoc,
                         HasSubordinates aceEntity) {

    String id = toString(aceEntity.getAttributeValue("ace_id"));
    String type = toString(aceEntity.getAttributeValue("type"));
    String subtype = toString(aceEntity.getAttributeValue("subtype"));
    String clazz = toString(aceEntity.getAttributeValue("class"));

    // Case by case fixes for legacy errors:
    if ("Buildings_Grounds".equals(subtype))
      subtype = "Building-Grounds";
    if ("Other".equals(subtype))
      subtype = null;


    if (DEBUG > 0)
      System.err.println("  entity: " + id + " sub="+subtype+" cls="+clazz);

    Element entity = doc.addElement("entity")
      .addAttribute("ID", id)
      .addAttribute("TYPE", type)
      .addAttribute("SUBTYPE", subtype)
      .addAttribute("CLASS", clazz);
    
    // mentions, primary first
    NamedExtentRegions primary =
      (NamedExtentRegions) aceEntity.getAttributeValue("primary-mention");
    if (primary == null)
      System.err.println(" Unspecified primary mention");
    else
      addMention(entity, awbDoc, primary);

    AWBAnnotation[] mentions =
      aceEntity.getSubordinates(ACE2004Utils.ENTITY_MENTION_TYPE);
    for (int i = 0; i < mentions.length; i++) {
      if (mentions[i] != primary)
        addMention(entity, awbDoc, (NamedExtentRegions) mentions[i]);
    }

    LinkedHashMap names = new LinkedHashMap();
      
    // entity_attributes (repeats head info in mentions of type=name)
    Element attributes = entity.addElement("entity_attributes");
    if (primary != null) {
      if ("NAM".equals(primary.getAttributeValue("type"))) {
        CharSeq charseq = getCharSeq(awbDoc, primary, "head");

        if (charseq != null && names.get(charseq.text) == null)
          names.put(charseq.text, charseq);
      }
    }

    for (int i=0; i<mentions.length; i++) {
      if (mentions[i] != primary &&
          "NAM".equals(mentions[i].getAttributeValue("type"))) {
        CharSeq charseq = getCharSeq(awbDoc, mentions[i], "head");

        if (charseq != null && names.get(charseq.text) == null)
          names.put(charseq.text, charseq);
      }
    }

    Iterator nameIter = names.keySet().iterator();
    while (nameIter.hasNext()) {
      CharSeq charseq = (CharSeq) names.get(nameIter.next());
      addEntityNameArg(attributes, awbDoc, charseq);
    }
  }

  private CharSeq getCharSeq(AWBDocument awbDoc, AWBAnnotation mention,
                             String extent) {
    String ext = (extent == null ? "" : (extent + "."));
    Integer s = (Integer) mention.getAttributeValue(ext + "TextExtentStart");
    Integer e = (Integer) mention.getAttributeValue(ext + "TextExtentEnd");
    if (s == null || e == null)
      return null;
    String t = awbDoc.getSignal().getCharsAt(s.intValue(), e.intValue());

    CharSeq charseq = new CharSeq(s.intValue(), e.intValue(),
                                  t.trim().replaceAll("(\n\r|\n|\r)"," "));
    return charseq;
  }

  private class CharSeq {
    int start;
    int end;
    String text;
    CharSeq(int s, int e, String t) {
      start = s;
      end = e;
      text = t;
    }
  }

  /* Goes in entity_args, and repeats info in mention heads */
  private void addEntityNameArg (Element attributes, AWBDocument awbDoc,
                                 CharSeq charseq) {
    Element name = attributes.addElement("name")
      .addAttribute("NAME", charseq.text);
    addCharseq(name, awbDoc, charseq.start, charseq.end);
  }

  private void addMention(Element entity, AWBDocument awbDoc,
                          NamedExtentRegions aceMention) {
    String id = toString(aceMention.getAttributeValue("ace_id"));
    String type = toString(aceMention.getAttributeValue("type"));
    String role = toString(aceMention.getAttributeValue("role"));
    String ldctype = toString(aceMention.getAttributeValue("ldctype"));
    String metonymic = toString(aceMention.getAttributeValue("metonymy"));
    String ldcatr = toString(aceMention.getAttributeValue("ldcatr"));
    
    if (DEBUG > 0)
      System.err.println ("  entity-mention=" + id);
    
    Element mention = entity.addElement("entity_mention")
      .addAttribute("ID", id)
      .addAttribute("TYPE", type)
      .addAttribute("ROLE", role)
      .addAttribute("LDCTYPE", ldctype)
      .addAttribute("METONYMY_MENTION", upCase(metonymic))
      .addAttribute("LDCATR", upCase(ldcatr));

    Element extent = mention.addElement("extent");
    addCharseq(extent, awbDoc,
               aceMention.getTextExtentStart("full"),
               aceMention.getTextExtentEnd("full"));
    
    Element head = mention.addElement("head");
    addCharseq(head, awbDoc,
               aceMention.getTextExtentStart("head"),
               aceMention.getTextExtentEnd("head"));
  }

  private void addQuantity(Element doc, AWBDocument awbDoc,
                           HasSubordinates aceQuantity) {
    String id = toString(aceQuantity.getAttributeValue("ace_id"));
    String type = toString(aceQuantity.getAttributeValue("type"));
    String subtype = toString(aceQuantity.getAttributeValue("subtype"));
    String val = toString(aceQuantity.getAttributeValue("timex_val"));
    String mod = toString(aceQuantity.getAttributeValue("timex_mod"));
    String anchor_val = toString(aceQuantity.getAttributeValue("timex_anchor_val"));
    String anchor_dir = toString(aceQuantity.getAttributeValue("timex_anchor_dir"));
    String set = toString(aceQuantity.getAttributeValue("timex_set"));
    String non_specific = toString(aceQuantity.getAttributeValue("timex_non_specific"));
    String comment = toString(aceQuantity.getAttributeValue("timex_comment"));

    // Case by case fixes for legacy errors:
    if ("Other".equals(subtype))
      subtype = null;


    if (DEBUG > 0)
      System.err.println ("  quantity: " + aceQuantity.getId() + ": " + id);

    Element quantity = doc.addElement("quantity")
      .addAttribute("ID", id)
      .addAttribute("TYPE", type)
      .addAttribute("SUBTYPE", subtype)
      .addAttribute("TIMEX2_VAL", val)
      .addAttribute("TIMEX2_MOD", mod)
      .addAttribute("TIMEX2_ANCHOR_VAL", anchor_val)
      .addAttribute("TIMEX2_ANCHOR_DIR", anchor_dir)
      .addAttribute("TIMEX2_SET", set)
      .addAttribute("TIMEX2_NON_SPECIFIC", non_specific)
      .addAttribute("TIMEX2_COMMENT", comment);

    // mentions, primary first
    TextExtentRegion primary =
      (TextExtentRegion) aceQuantity.getAttributeValue("primary-mention");
    addQuantityMention(quantity, awbDoc, primary);

    AWBAnnotation[] mentions =
      aceQuantity.getSubordinates(ACE2004Utils.QUANTITY_MENTION_TYPE);
    for (int i = 0; i < mentions.length; i++) {
      if (mentions[i] != primary)
        addQuantityMention(quantity, awbDoc, (TextExtentRegion) mentions[i]);
    }
  }

  private void addQuantityMention(Element quantity, AWBDocument awbDoc,
                                  TextExtentRegion aceMention) {
    String id = toString(aceMention.getAttributeValue("ace_id"));
    
    if (DEBUG > 0)
      System.err.println ("  quantity-mention=" + id);
    
    Element mention = quantity.addElement("quantity_mention")
      .addAttribute("ID", id);

    Element extent = mention.addElement("extent");
    addCharseq(extent, awbDoc,
               aceMention.getTextExtentStart(),
               aceMention.getTextExtentEnd());
  }
  
  private void addRelation(Element doc, AWBDocument awbDoc,
                           HasSubordinates aceRelation) {

    String id = toString(aceRelation.getAttributeValue("ace_id"));
    String type = toString(aceRelation.getAttributeValue("type"));
    String subtype = toString(aceRelation.getAttributeValue("subtype"));
    String modality = toString(aceRelation.getAttributeValue("modality"));
    String tense = toString(aceRelation.getAttributeValue("tense"));

    // Case by case fixes for legacy errors:
    if ("Other".equals(subtype))
      subtype = null;

    if (DEBUG > 0)
      System.err.println ("  relation: " + aceRelation.getId() + ": " + id);
    
    Element relation = doc.addElement("relation")
      .addAttribute("ID", id)
      .addAttribute("TYPE", type)
      .addAttribute("SUBTYPE", subtype)
      .addAttribute("MODALITY", modality)
      .addAttribute("TENSE", tense);

    // add arg-1 and arg-2 as argument elements
    AWBAnnotation arg1 = (AWBAnnotation) aceRelation.getAttributeValue("arg1");
    addArgument(relation, "relation_argument", "Arg-1", arg1);

    AWBAnnotation arg2 = (AWBAnnotation) aceRelation.getAttributeValue("arg2");
    addArgument(relation, "relation_argument", "Arg-2", arg2);

    // Promote relation_mention_arguments to the relation_arguments, while
    // creating the relation_mentions.  Add these relation_mentions to the
    // relation after, so that relation_arguments appear first in XML

    // collect relation_mentions for later addition
    Vector mentions = new Vector();

    // collect relation_arguments so they are unique
    LinkedHashMap parentArgs = new LinkedHashMap();
    
    AWBAnnotation[] aceMentions =
      aceRelation.getSubordinates(ACE2004Utils.RELATION_MENTION_TYPE);

    // Loop over mentions: create standalone, adding structure, but promoting
    // it's arguments to the relation before adding the mentions to the
    // relation
    for (int i = 0; i < aceMentions.length; i++) {
      HasSubordinates aceMention = (HasSubordinates) aceMentions[i];

      String mentionID = toString(aceMention.getAttributeValue("ace_id"));
      String condition = toString(aceMention.getAttributeValue("lexicalcondition"));
      
      if (DEBUG > 0)
        System.err.println ("  relation-mention: " + aceMention.getId() +
                            ": " + mentionID);

      // Create standalone mention, and add to relation /after/ arguments
      Element mention = DocumentHelper.createElement("relation_mention")
        .addAttribute("ID", mentionID)
        .addAttribute("LEXICALCONDITION", condition);

      mentions.add(mention);
      
      // add arg-1 and arg-2 as argument elements
      AWBAnnotation mentionArg1 = (AWBAnnotation) aceMention.getAttributeValue("arg1");
      addArgument(mention, "relation_mention_argument", "Arg-1", mentionArg1);
      
      AWBAnnotation mentionArg2 = (AWBAnnotation) aceMention.getAttributeValue("arg2");
      addArgument(mention, "relation_mention_argument", "Arg-2", mentionArg2);
      
      // add mention_arguments and promote to relation_arguments
      ATLASElementSet aceArguments =
        aceMention.getRegion().getSubordinateSet(ACE2004Utils.ARGUMENT_MENTION_TYPE);

      addArgumentList(relation, mention, parentArgs, "relation", aceArguments);
    } // for (...mention-relation...)

    // add the collected (unique) relation_arguments
    Iterator argIter = parentArgs.keySet().iterator();
    while (argIter.hasNext()) {
      String role = (String) argIter.next();
      // there can be more than one value for a role, but no duplicate values
      // for the same role
      LinkedHashSet roleValues = (LinkedHashSet) parentArgs.get(role);

      Iterator valueIter = roleValues.iterator();
      while (valueIter.hasNext()) {
        AWBAnnotation value = (AWBAnnotation) valueIter.next();
        addArgument(relation, "relation_argument", role, value);
      }
    }

    // finally add the relation_mentions to the relation
    Iterator mentionIter = mentions.iterator();
    while (mentionIter.hasNext()) {
      relation.add((Element) mentionIter.next());
    }
  }

  private void addEvent(Element doc, AWBDocument awbDoc,
                        HasSubordinates aceEvent) {

    String id = toString(aceEvent.getAttributeValue("ace_id"));
    String type = toString(aceEvent.getAttributeValue("type"));
    String subtype = toString(aceEvent.getAttributeValue("subtype"));
    String modality = toString(aceEvent.getAttributeValue("modality"));
    String polarity = toString(aceEvent.getAttributeValue("polarity"));
    String genericity = toString(aceEvent.getAttributeValue("genericity"));
    String tense = toString(aceEvent.getAttributeValue("tense"));

    // Case by case fixes for legacy errors:
    if ("Other".equals(modality))
      modality = "Unspecified";
    if ("Unspecified".equals(tense))
      tense = null;
    

    if (DEBUG > 0)
      System.err.println ("  event: " + aceEvent.getId() + ": " + id);
    
    Element event = doc.addElement("event")
      .addAttribute("ID", id)
      .addAttribute("TYPE", type)
      .addAttribute("SUBTYPE", subtype)
      .addAttribute("MODALITY", modality)
      .addAttribute("POLARITY", polarity)
      .addAttribute("GENERICITY", genericity)
      .addAttribute("TENSE", tense);

    // Promote event_mention_arguments to the event_arguments, while
    // creating the event_mentions.  Add these event_mentions to the
    // event after, so that event_arguments appear first in XML

    // collect event_mentions for later addition
    Vector mentions = new Vector();

    // collect event_arguments so they are unique
    LinkedHashMap parentArgs = new LinkedHashMap();
        
    AWBAnnotation[] aceMentions =
      aceEvent.getSubordinates(ACE2004Utils.EVENT_MENTION_TYPE);

    // Loop over mentions: create standalone, adding structure, but promoting
    // it's arguments to the event before adding the mentions to the relation
    for (int i = 0; i < aceMentions.length; i++) {
      TextExtentRegion aceMention = (TextExtentRegion) aceMentions[i];

      String mentionID = toString(aceMention.getAttributeValue("ace_id"));
      String level = toString(aceMention.getAttributeValue("level"));
      
      if (DEBUG > 0)
        System.err.println ("  event-mention: " + aceMention.getId() +
                            ": " + mentionID);

      // Create standalone mention, and add to event /after/ arguments
      Element mention = DocumentHelper.createElement("event_mention")
        .addAttribute("ID", mentionID)
        .addAttribute("LEVEL", level);

      mentions.add(mention);

      // the text regions
      // TODO: The maia is rather old here. it needs to be fixed!!!
      TextExtentRegion aceExtent =
        (TextExtentRegion) aceMention.getAttributeValue("extent");

      if (aceExtent != null) {
        Element extent = mention.addElement("extent");
        addCharseq(extent, awbDoc,
                   aceExtent.getTextExtentStart(),
                   aceExtent.getTextExtentEnd());
      }
      else {
        System.err.println("Event-Mention " + mentionID + " missing extent");
      }
  
      // TODO: There may be multiple here (there aren't: see comment above)
      Element anchor = mention.addElement("anchor");
      addCharseq(anchor, awbDoc,
                 aceMention.getTextExtentStart(),
                 aceMention.getTextExtentEnd());
      
      
      // add mention_arguments and promote to event_arguments
      ATLASElementSet aceArguments =
        aceMention.getRegion().getSubordinateSet(ACE2004Utils.ARGUMENT_MENTION_TYPE);

      addArgumentList(event, mention, parentArgs, "event", aceArguments);
    } // for (...mention-event...)


    // add the collected (unique) event_aguments
    Iterator argIter = parentArgs.keySet().iterator();
    while (argIter.hasNext()) {
      String role = (String) argIter.next();
      // there can be more than one value for a role, but no duplicate values
      // for the same role
      LinkedHashSet roleValues = (LinkedHashSet) parentArgs.get(role);

      Iterator valueIter = roleValues.iterator();
      while (valueIter.hasNext()) {
        AWBAnnotation value = (AWBAnnotation) valueIter.next();
        addArgument(event, "event_argument", role, value);
      }
    }

    // finally add the event_mentions to the event
    Iterator iter = mentions.iterator();
    while (iter.hasNext()) {
      event.add((Element) iter.next());
    }
  }

  private void addArgument(Element element, String name,
                           String role, AWBAnnotation value) {
    if (value != null) {
      element.addElement(name)
        .addAttribute("ROLE", role)
        .addAttribute("REFID", toString(value.getAttributeValue("ace_id")));
    }
    else {
      System.err.println(role + " is null in " + element.attributeValue("ace_id"));
    }
  }

  /**
   * Adds a <parentName>_mention_argument element to 'mention', and collects
   * varous values for the arg roles in a LinkedHashSet, linked to the role in
   * the parentArgs hashMap. This allows us to eliminate duplicate args at the
   * parent level.
   */ 
  private void addArgumentList(Element parent, Element mention,
                               LinkedHashMap parentArgs, String parentName,
                               ATLASElementSet aceArguments) {
    Iterator iter = aceArguments.iterator();
    while (iter.hasNext()) {

      Object next = iter.next();
      if (next instanceof AnnotationRef)
	next = ((AnnotationRef) next).getElement();
      
      AWBAnnotation aceArgument = (AWBAnnotation) next;

      String role = toString(aceArgument.getAttributeValue("role"));
      
      // one value or the other...
      AWBAnnotation value =
        (AWBAnnotation) aceArgument.getAttributeValue("entity-value");
      if (value == null) {
        value = (AWBAnnotation) aceArgument.getAttributeValue("quantity-value");
      }
      
      if (value == null || role == null) {
        System.err.println ("    NULL in attribute: role=" + role +
                            " value=" + value);
        continue;
      }
      
      addArgument(mention, parentName + "_mention_argument", role, value);

      // promote to relation_argument
      // collect all values for 'role' so that no duplicates are output later
      LinkedHashSet values = (LinkedHashSet) parentArgs.get(role);
      if (values == null) {
        values = new LinkedHashSet();
        parentArgs.put(role, values);
      }
      
      values.add(ACE2004Task.getMentionParent(value));
    }
  }

  private void coreferenceRelations(AWBDocument awbDoc) {
    Boolean coref = (Boolean)
      awbDoc.getClientProperty(ACE2004Task.RELATIONS_COREFERENCED);
    if (! Boolean.TRUE.equals(coref)) {
      ACE2004ToolKit.coreferenceRelations(awbDoc);
    }
  }

  
  private boolean stringIsEmpty(String s) {
    return (s == null) || (s.trim().length() == 0);
  }

  /** Tests for null first, also returns null if string value is empty */
  private String toString(Object o) {
    String s = null;
    if (o != null) {
      s = o.toString();
      if (s.length() == 0)
        s = null;
    }
    return s;
  }

  private String upCase(String s) {
    if (s != null)
      s = s.toUpperCase();
    return s;
  }
}
