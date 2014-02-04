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
import gov.nist.atlas.type.ATLASType;

import org.mitre.jawb.atlas.*;
import org.mitre.jawb.gui.GUIUtils;
import org.mitre.jawb.io.*;
import org.mitre.jawb.tasks.*;

import java.math.*;

public final class ExportAPF5_1_1 implements Exporter {

    private static final String APF_VERSION = "5.1.1";
    private static final int DEBUG = 0;
    private static int externalResourceCount = 0;
    private static String [] externalResourceUrn  = new String[100];
    private static String [] externalResourceName = new String[100];

  public ExportAPF5_1_1() {
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

      // Let's find out who the "author" of *this* version of the APF file is:
      String userSystemName = System.getProperty("user.name");

      // what's the point in versioning by X.0 if every X.y is incompatable?
      Element root = xmlDoc.addElement("source_file")
        .addAttribute("URI", signalURI.toString())
        .addAttribute("SOURCE", source)
        .addAttribute("TYPE", "text")
        .addAttribute("VERSION", "5.0")
        .addAttribute("AUTHOR", userSystemName)
        .addAttribute("ENCODING", encoding);

      // warning to users reading the xml
      if (!encoding.equals ("UTF-8"))
        root.addComment("This document is encoded in 'UTF-8',"+
                        " the /source/ is encoded in '"+encoding+"'");

      Element doc = root.addElement("document")
        .addAttribute("DOCID", IdTracker.getDocId(awbDoc));

      // don't know the order returned, so make collections
      HashSet entities       = new LinkedHashSet();
      HashSet relations      = new LinkedHashSet();
      HashSet events         = new LinkedHashSet();
      HashSet quantities     = new LinkedHashSet();
      HashSet timex2entities = new LinkedHashSet();

      Iterator annotIter = awbDoc.getAllAnnotations ();
      while (annotIter.hasNext ()) {
        AWBAnnotation annot = (AWBAnnotation) annotIter.next();
        String type = annot.getAnnotationType().getName();

        if (type.equals(ACE2004Task.ENTITY_TYPE_NAME))
          entities.add(annot);

        else if (type.equals(ACE2004Task.QUANTITY_TYPE_NAME))
          quantities.add(annot);

        else if (type.equals(ACE2004Task.TIMEX2_TYPE_NAME))
          timex2entities.add(annot);

        else if (type.equals(ACE2004Task.RELATION_TYPE_NAME))
          relations.add(annot);

        else if (type.equals(ACE2004Task.EVENT_TYPE_NAME))
          events.add(annot);
      }

      if (DEBUG > 0) {
        System.err.println("Entities:       "+entities.size());
        System.err.println("Values:         "+quantities.size());
        System.err.println("Timex2Entities: "+timex2entities.size());
        System.err.println("Relations:      "+relations.size());
        System.err.println("Events:         "+events.size());
      }
      // iterate over each
      Iterator iterator = null;

      iterator = entities.iterator();
      while (iterator.hasNext())
        addEntity(doc, awbDoc, (HasSubordinates) iterator.next());

      iterator = quantities.iterator();
      while (iterator.hasNext())
        addQuantity(doc, awbDoc, (HasSubordinates) iterator.next());

      iterator = timex2entities.iterator();
      while (iterator.hasNext())
        addTimex2(doc, awbDoc, (TextExtentRegion) iterator.next());

      iterator = relations.iterator();
      while (iterator.hasNext())
        addRelation(doc, awbDoc, (HasSubordinates) iterator.next());

      iterator = events.iterator();
      while (iterator.hasNext())
        addEvent(doc, awbDoc, (HasSubordinates) iterator.next());

      addExternalResourceDeclarations(doc, awbDoc);

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

  private void addExternalResourceDeclarations(Element doc, AWBDocument awbDoc) {
    for (int resCount = 0; resCount < externalResourceCount; resCount++) {
	Element externalResource = doc.addElement("external_resource")
	    .addAttribute("NAME", externalResourceName[resCount])
	    .addAttribute("URN",  externalResourceUrn[resCount]);
    }
  }


  private void addEntity(Element doc, AWBDocument awbDoc,
                         HasSubordinates aceEntity) {

    String id = toString(aceEntity.getAttributeValue("ace_id"));
    String type = toString(aceEntity.getAttributeValue("type"));
    String subtype = toString(aceEntity.getAttributeValue("subtype"));
    String clazz = toString(aceEntity.getAttributeValue("class"));
    String externalLinkName = toString(aceEntity.getAttributeValue("external-link-resource-name"));
    String externalLinkUrn  = toString(aceEntity.getAttributeValue("external-link-resource-urn"));
    String externalLinkEid  = toString(aceEntity.getAttributeValue("external-link-eid"));
    boolean newNameP = true;
    for (int resCount = 0; resCount < externalResourceCount; resCount++) {
	// Since we store this info, repetetively, on each entity, we don't need to
	// overwrite the same string in the externalResourceUrn table entry multiple
	// times.  We *do* want to know if this is an externalResourceName that we have
	// not yet encountered.
	if (externalResourceName[resCount].equals(externalLinkName)) {
	    newNameP = false;
	}
    }
    if (newNameP) {
	externalResourceCount++;
	externalResourceUrn[externalResourceCount]  = externalLinkUrn;
	externalResourceName[externalResourceCount] = externalLinkName;
    }

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
    if (externalLinkEid != null && externalLinkEid.length() > 0) {
	if (externalLinkName == null) externalLinkName = "NONAME";
	Element externalLink = entity.addElement("external_link")
	    .addAttribute("RESOURCE", externalLinkName)
	    .addAttribute("EID", externalLinkEid);
    }
    
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


    // cannot hash on text because we need to include all mentions,
    // even if the text is duplicated RK 9/12/05
    // LinkedHashMap names = new LinkedHashMap();
    LinkedHashSet nameSet = new LinkedHashSet();
      
    // entity_attributes (repeats head info in mentions of type=name)
    if (primary != null) {
      if ("NAM".equals(primary.getAttributeValue("type"))) {
        CharSeq charseq = getCharSeq(awbDoc, primary, "head");

        if (charseq != null)
          nameSet.add(charseq);
      }
    }

    for (int i=0; i<mentions.length; i++) {
      if (mentions[i] != primary &&
          "NAM".equals(mentions[i].getAttributeValue("type"))) {
        CharSeq charseq = getCharSeq(awbDoc, mentions[i], "head");
        if (DEBUG > 0)
          System.err.println ("ExportAPF: found mention of type name: " + 
                              charseq);

        if (charseq != null)
          nameSet.add(charseq);
      }
    }

    Iterator nameIter = nameSet.iterator();
    boolean attributesElementP = false;
    Element attributes = null;
    while (nameIter.hasNext()) {
	if (!attributesElementP) {
	    attributes = entity.addElement("entity_attributes");
	    attributesElementP = true;
	}
      CharSeq charseq = (CharSeq) nameIter.next();
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
      .addAttribute("LDCTYPE", ldctype)
      .addAttribute("ROLE", role)
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


    // Case by case fixes for legacy errors:
    if ("Other".equals(subtype))
      subtype = null;


    if (DEBUG > 0)
      System.err.println ("  value: " + aceQuantity.getId() + ": " + id);

    Element quantity = doc.addElement("value")
	.addAttribute("ID", id)
	.addAttribute("TYPE", type)
	.addAttribute("SUBTYPE", subtype);

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
      System.err.println ("  value-mention=" + id);
    
    Element mention = quantity.addElement("value_mention")
      .addAttribute("ID", id);

    Element extent = mention.addElement("extent");
    addCharseq(extent, awbDoc,
               aceMention.getTextExtentStart(),
               aceMention.getTextExtentEnd());
  }
  

  private void addTimex2(Element doc, AWBDocument awbDoc,
                           TextExtentRegion aceTimex2) {
    String mentionId    = toString(aceTimex2.getAttributeValue("ace_id"));
    String timex2Id     = toString(aceTimex2.getAttributeValue("timex2-id"));
    String val          = toString(aceTimex2.getAttributeValue("val"));
    String mod          = toString(aceTimex2.getAttributeValue("mod"));
    String anchor_val   = toString(aceTimex2.getAttributeValue("anchor-val"));
    String anchor_dir   = toString(aceTimex2.getAttributeValue("anchor-dir"));
    String set          = toString(aceTimex2.getAttributeValue("set"));
    String non_specific = toString(aceTimex2.getAttributeValue("non-specific"));
    String comment      = toString(aceTimex2.getAttributeValue("comment"));

    if (DEBUG > 0) 
	System.err.println ("  timex2: " + aceTimex2.getId() + ": " + mentionId);

    if (timex2Id == null) {
	int finalHyphenPos = mentionId.lastIndexOf('-');
	// System.err.println ("  timex2: finalHyphenPos= " + finalHyphenPos);
	timex2Id = mentionId.substring(0,finalHyphenPos);
	// System.err.println ("  timex2: timex2Id " + timex2Id);
    }
    
    if (DEBUG > 0) 
	System.err.println ("  timex2 adding Element...");
    Element timex2 = doc.addElement("timex2")
      .addAttribute("ID",           timex2Id)
      .addAttribute("VAL",          val)
      .addAttribute("MOD",          mod)
      .addAttribute("ANCHOR_VAL",   anchor_val)
      .addAttribute("ANCHOR_DIR",   anchor_dir)
      .addAttribute("SET",          set)
      .addAttribute("NON_SPECIFIC", non_specific)
      .addAttribute("COMMENT",      comment);

    Element mention = timex2.addElement("timex2_mention")
      .addAttribute("ID", mentionId);

    Element extent = mention.addElement("extent");
    addCharseq(extent, awbDoc,
               aceTimex2.getTextExtentStart(),
               aceTimex2.getTextExtentEnd());
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

    if (DEBUG > 2)
      System.err.println ("    modality: " + 
                          (modality == null?"null":modality) +
                          "    tense: " +
                          (tense == null?"null":tense));
    
    Element relation = doc.addElement("relation")
      .addAttribute("ID", id)
      .addAttribute("TYPE", type)
      .addAttribute("SUBTYPE", subtype)
      .addAttribute("TENSE", tense)
	.addAttribute("MODALITY", modality);

    // RK 8/15/05 grab mentions now in case needed to infer arg-1 or arg-2
    AWBAnnotation[] aceMentions =
      aceRelation.getSubordinates(ACE2004Utils.RELATION_MENTION_TYPE);

    // add arg-1 and arg-2 as argument elements
    AWBAnnotation arg1 = (AWBAnnotation) aceRelation.getAttributeValue("arg1");
    if (arg1 == null) {
      // RK 8/15/05 if arg1 not found, infer it from a relation mention now
      for (int i = 0; i < aceMentions.length; i++) {
        AWBAnnotation aceMention = (AWBAnnotation) aceMentions[i];
        AWBAnnotation mentionArg1 = 
          (AWBAnnotation) aceMention.getAttributeValue("arg1");
        AWBAnnotation mentionArg1Entity =
          ACE2004Task.getParent(mentionArg1, ACE2004Task.ENTITY_TYPE_NAME);
        arg1 = mentionArg1Entity;
        break;
      }
    }
    addArgument(relation, "relation_argument", "Arg-1", arg1);      

    AWBAnnotation arg2 = (AWBAnnotation) aceRelation.getAttributeValue("arg2");
    if (arg2 == null) {
      // RK 8/15/05 if arg2 not found, infer it from a relation mention now
      for (int i = 0; i < aceMentions.length; i++) {
        AWBAnnotation aceMention = (AWBAnnotation) aceMentions[i];
        AWBAnnotation mentionArg2 = 
          (AWBAnnotation) aceMention.getAttributeValue("arg2");
        AWBAnnotation mentionArg2Entity =
          ACE2004Task.getParent(mentionArg2, ACE2004Task.ENTITY_TYPE_NAME);
        arg2 =  mentionArg2Entity;
        break;
      }
    }
    addArgument(relation, "relation_argument", "Arg-2", arg2);      


    // Promote relation_mention_arguments to the relation_arguments, while
    // creating the relation_mentions.  Add these relation_mentions to the
    // relation after, so that relation_arguments appear first in XML

    // collect relation_mentions for later addition
    Vector mentions = new Vector();

    // collect relation_arguments so they are unique
    LinkedHashMap parentArgs = new LinkedHashMap();
    
    // Loop over mentions: create standalone, adding structure, but promoting
    // it's arguments to the relation before adding the mentions to the
    // relation
    for (int i = 0; i < aceMentions.length; i++) {
	// HasSubordinates aceMention = (HasSubordinates) aceMentions[i];
      AWBAnnotation aceMention = (AWBAnnotation) aceMentions[i];

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
      
      
      // add mention_extent
      Element relMenExtent = mention.addElement("extent");

      TextExtentRegion relMenAnnotTER = (TextExtentRegion) aceMention.getAttributeValue("relation-mention-extent");
      if (DEBUG > 0)
        System.err.println ("  relation-mention-extent: " + relMenAnnotTER.getTextExtentStart() + "," +
			    relMenAnnotTER.getTextExtentEnd() + ": " +
			    awbDoc.getSignal().getCharsAt(relMenAnnotTER.getTextExtentStart(),
							  relMenAnnotTER.getTextExtentEnd()));

      addCharseq(relMenExtent, awbDoc,
		 relMenAnnotTER.getTextExtentStart(),
		 relMenAnnotTER.getTextExtentEnd());

      // RK 10/10/05 moved args after extent, as required by current DTD
      // add arg-1 and arg-2 as argument elements
      AWBAnnotation mentionArg1 = (AWBAnnotation) aceMention.getAttributeValue("arg1");
      addMentionArgument(mention, "relation_mention_argument", "Arg-1", mentionArg1, awbDoc);
      
      AWBAnnotation mentionArg2 = (AWBAnnotation) aceMention.getAttributeValue("arg2");
      addMentionArgument(mention, "relation_mention_argument", "Arg-2", mentionArg2, awbDoc);
      

      // add mention_arguments and promote to relation_arguments
      ATLASElementSet aceArguments =
        aceMention.getRegion().getSubordinateSet(ACE2004Utils.ARGUMENT_MENTION_TYPE);

      addArgumentList(relation, mention, parentArgs, "relation", aceArguments, awbDoc);
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
    // These legacy errors are now the required format for 5.1.1 again, 
    // so remove these "fixes"
    /***********************************************
    if ("Other".equals(modality))
      modality = "Unspecified";
    if ("Unspecified".equals(tense))
      tense = null;
    *********************/
    
    // instead fix this Tense the other way -- if null, insert as "Unspecified"
    if (tense == null)
      tense = "Unspecified";
    

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

      String mentionID     = toString(aceMention.getAttributeValue("ace_id"));
      String level         = toString(aceMention.getAttributeValue("level"));
      String ldcScopeStartString = toString(aceMention.getAttributeValue("ldcscope-start"));
      String ldcScopeEndString   = toString(aceMention.getAttributeValue("ldcscope-end"));
      int    ldcScopeStart = -1, ldcScopeEnd = -1;

      if (ldcScopeStartString != null && ldcScopeStartString.length() > 0 &&
	  ldcScopeEndString   != null && ldcScopeEndString.length()   > 0) {
	  ldcScopeStart = Integer.parseInt (ldcScopeStartString);
	  ldcScopeEnd   = Integer.parseInt (ldcScopeEndString);
      }
      /*
      System.err.println(" ldcScopeStartString = " + ldcScopeStartString);
      System.err.println(" ldcScopeEndString   = " + ldcScopeEndString);
      System.err.println(" ldcScopeStart       = " + ldcScopeStart);
      System.err.println(" ldcScopeEnd         = " + ldcScopeEnd);
      */

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
  
      if (ldcScopeStart >= 0 && ldcScopeEnd >= 0) {
        Element ldcscope = mention.addElement("ldc_scope");
	// Note: We don't actually end up using the ldcScopeString!
        // Also, since this was read in directly from APF, we add 1 to
	// the offset, since the addCharseq method automatically subtracts
	// from the end point to create valid charseq values.
	addCharseq(ldcscope, awbDoc, ldcScopeStart, ldcScopeEnd+1);

      } else {
        System.err.println("Event " + mentionID + " missing ldc_scope");
      }

      // TODO: There may be multiple here (there aren't: see comment above)
      Element anchor = mention.addElement("anchor");
      addCharseq(anchor, awbDoc,
                 aceMention.getTextExtentStart(),
                 aceMention.getTextExtentEnd());
      
      
      // add mention_arguments and promote to event_arguments
      ATLASElementSet aceArguments =
        aceMention.getRegion().getSubordinateSet(ACE2004Utils.ARGUMENT_MENTION_TYPE);

      addArgumentList(event, mention, parentArgs, "event", aceArguments, awbDoc);
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

    private void addMentionArgument(Element element, String name,
				    String role, AWBAnnotation aceMention, AWBDocument awbDoc) {
	if (aceMention != null) {
	    String type = aceMention.getAnnotationType().getName();
	    Element nameElement = element.addElement(name)
		.addAttribute("ROLE", role)
		.addAttribute("REFID", toString(aceMention.getAttributeValue("ace_id")));
	    Element extent = nameElement.addElement("extent");
	    if (type.equals(ACE2004Task.TIMEX2_TYPE_NAME) ||
		type.equals(ACE2004Task.QUANTITY_MENTION_TYPE_NAME)) {
		TextExtentRegion aceMentionTER = (TextExtentRegion) aceMention;
		addCharseq(extent, awbDoc,
			   aceMentionTER.getTextExtentStart(),
			   aceMentionTER.getTextExtentEnd());
	    } else {
		// We presume, then, that this is an entity mention
		if (DEBUG > 0) {
		    System.err.println("addMentionArgument aceMention = " + aceMention);
		}
		NamedExtentRegions aceMentionNER = (NamedExtentRegions) aceMention;
		addCharseq(extent, awbDoc,
			   aceMentionNER.getTextExtentStart("full"),
			   aceMentionNER.getTextExtentEnd("full"));
	    }
	} else {
	    if (DEBUG > 0)
		System.err.println(role + " is null in " + element.attributeValue("ace_id"));
	}
    }

    private void addArgument(Element element, String name,
			     String role, AWBAnnotation value) {
	if (value != null && role != null) {
	    String type = value.getAnnotationType().getName();
	    if (type.equals(ACE2004Task.TIMEX2_TYPE_NAME)) {
		element.addElement(name)
		    .addAttribute("REFID", toString(value.getAttributeValue("timex2-id")))
		    .addAttribute("ROLE", role);
	    } else
		element.addElement(name)
		    .addAttribute("REFID", toString(value.getAttributeValue("ace_id")))
		    .addAttribute("ROLE", role);
	} else {
	    if (DEBUG > 0)
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
                               ATLASElementSet aceArguments, AWBDocument awbDoc) {
      if (DEBUG > 0) {
	  System.err.println("Export.APF5_1_1 addArgumentList parent mention "
			     + parent + " " + mention);
      }
    Iterator iter = aceArguments.iterator();
    while (iter.hasNext()) {

      Object next = iter.next();
      if (DEBUG > 0) {
	  System.err.println("Export.APF5_1_1 next = " + next);
      }
      if (next instanceof AnnotationRef)
	next = ((AnnotationRef) next).getElement();
      
      AWBAnnotation aceArgument = (AWBAnnotation) next;

      if (DEBUG > 0) {
	  System.err.println("Export.APF5_1_1 aceArgument = " + aceArgument);
      }
      String role = toString(aceArgument.getAttributeValue("role"));
      if (DEBUG > 0) {
	  System.err.println("Export.APF5_1_1 role = " + role);
      }
      
      // one value or the other...
      AWBAnnotation value =
        (AWBAnnotation) aceArgument.getAttributeValue("entity-value");
      if (value == null) {
	  value = (AWBAnnotation) aceArgument.getAttributeValue("quantity-value");
	  if (value == null) {
	      value = (AWBAnnotation) aceArgument.getAttributeValue("timex2-value");
	  }
      }
      

      String type;
      if (DEBUG > 0) {
	  System.err.println("Export.APF5_1_1 value = " + value);
      }
      if (value == null || role == null) {
        System.err.println ("    NULL in attribute: role=" + role +
                            " value=" + value);
        continue;
      } else {
	  type = value.getAnnotationType().getName();
      }
      
      addMentionArgument(mention, parentName + "_mention_argument", role, value, awbDoc);

      // promote to relation_argument
      // collect all values for 'role' so that no duplicates are output later
      LinkedHashSet values = (LinkedHashSet) parentArgs.get(role);
      if (values == null) {
        values = new LinkedHashSet();
        parentArgs.put(role, values);
      }
      
      if (type.equals(ACE2004Task.TIMEX2_TYPE_NAME)) {
	  // TIMEX2 mentions and entities are the same
	  values.add(value);
      } else {
	  values.add(ACE2004Task.getMentionParent(value));
      }
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
