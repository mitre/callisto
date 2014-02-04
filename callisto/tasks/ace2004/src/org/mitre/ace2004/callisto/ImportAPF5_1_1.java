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
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import gov.nist.atlas.type.ATLASType;

import org.mitre.jawb.tasks.Importer;
import org.mitre.jawb.atlas.*;
import org.mitre.jawb.io.*;
import org.mitre.jawb.tasks.Task;

public final class ImportAPF5_1_1  implements Importer {

  private int debugLevel = 0;
  
  /** collect annots that don't already have id's for assignment at end of import. */
  private LinkedList missingId = new LinkedList();
  private IdTracker idTracker;
    
  /** Fixes bad APF where entity_type was written as abbrivated form */
  private TreeMap entityTypeFix = null;
  
  public ImportAPF5_1_1  () {
    super ();
    entityTypeFix = new TreeMap (String.CASE_INSENSITIVE_ORDER);
    // mappings for bad APF fix
    String[] attribs =
      new String[] {"PER","ORG","LOC","FAC","GPE",
                    "Person","Organization","Location","Facility","GPE"};
    for (int i=0; i+5<attribs.length; i++)
      entityTypeFix.put (attribs[i], attribs[i+5]);
  }
  
  public String getFormat () {
    return "ACE2004 APF v.5.1.1";
  }
  public String toString () {
    return getFormat();
  }

  public String getDescription () {
    return "Import APF v5.1.1, dropping unknown tags";
  }

  public AWBDocument importDocument (URI uri, String encoding)
    throws IOException {

    // convert sgml to ATLAS: find orignal text from standoff apf
    // doc. write to raw text file, then convert apf to ATLAS
    AWBDocument atlasDoc;
    atlasDoc = mapApf2Atlas (uri, null, encoding);

    return atlasDoc;
  }


  /* ******************************************************************* *
   * mapApf2Atlas                                                        *
   * ******************************************************************* */
  
  /**
   * Returns null on error
   *
   * @param apfURI absolute location of the apf data
   * @param rawTextURI Location to save the raw text file, or
   *   <code>null</code> to have it auto determined from the apf location
   * @param encoding encoding of signal file
   * @throws NullPointerException if apfURI is null
   * @throws IllegalArgumentException if apfURI does not meet preconditions to
   *   get a stream from that scheme.
   * @throws IOException on IO failure
   */
  private AWBDocument mapApf2Atlas (URI apfURI, URI rawTextURI, String encoding)
    throws IOException  {

    File    sourceSgmlFile;
    
    /* Open I/O streams */
    Reader sgmlIn;
    Writer textOut;

    // parse the input sgml into a new ParseSgml object
    if (debugLevel > 0)
      System.err.println("ImportAPF5.0: entering...\n  " + apfURI);
    File apfFile = new File(apfURI);
    sgmlIn = new InputStreamReader (new FileInputStream (apfFile), encoding);
    
    SgmlDocument apfDoc =
      new SgmlDocument (new BufferedReader (sgmlIn), debugLevel);
    
    sgmlIn.close ();
    
    // Now find the name of the real signal file.
    // This is done by looping over all sgmlElements in the file, looking for
    // either the source_file and/or the document xml elements.
    if (debugLevel > 0)
      System.err.println("    identifying sgml source file...");
    Iterator sgmlElementsIter = apfDoc.iterator();
    SgmlElement element;
    
    String sourceType, sourceSource;
    String sourceEncoding = null, sourceUri = null;    // source_file info
    String documentDocId;                          // document info
    
    while (sgmlElementsIter.hasNext()) {
      element  = (SgmlElement) sgmlElementsIter.next();
      if (debugLevel > 0)
        System.err.println ("    element: "+element);

      if (element.getGid().equalsIgnoreCase("source_file")) {
        sourceType    = element.getAttribute("TYPE");
        sourceSource  = element.getAttribute("SOURCE");
        sourceUri     = element.getAttribute("URI");
        sourceEncoding= element.getAttribute("ENCODING");
        if (debugLevel > 0)
          System.err.println("    source_file type = "+sourceType+" source = "
                             +sourceSource+" uri = "+sourceUri);
        break;

      } else if (element.getGid().equalsIgnoreCase("document")) {
        documentDocId = element.getAttribute("DOCID");
        if (debugLevel > 0)
          System.err.println("    document docid = " + documentDocId);
        break;
      }
    }
    
    if (sourceUri == null) {
      throw new 
        IllegalArgumentException ("File does not appear to be an APF file\n"+
                                  "No appropriate 'source_file' or "+
                                  "'document' tag found.");
    }

    if (sourceEncoding != null)
      encoding = sourceEncoding;
    
    sourceSgmlFile = new File(apfFile.getParentFile(), sourceUri);
    if (! sourceSgmlFile.exists()) {
      System.err.println("Source sgml file <<" + sourceSgmlFile +
                         ">> doesn't exist");
      throw new IOException ("Can't find source file:\n"+sourceSgmlFile);
    }

    org.mitre.jawb.Jawb.initTasks();
    Task task = ACE2004Task.getInstance ();
    
    // Now actually convert into Atlas (and save to AIF).
    AWBDocument doc = null;
    try {
      doc = convertApfToAtlas (apfDoc, sourceSgmlFile, encoding);
    } catch (UnmodifiableAttributeException x) {
      // this would be some programmatic error so I'm wrapping it.
      RuntimeException e = new RuntimeException ("Attempt to set an unmodifiable attribute");
      e.initCause(x);
      throw e;
    }

    System.err.println("APF Import: done");
    return doc;
  }

  /* ******************************************************************* *
   * convertToAltas                                                      *
   * ******************************************************************* */

  private AWBDocument convertApfToAtlas (SgmlDocument apfDocument,
                                         File signalFile,
                                         String encoding)
    throws UnmodifiableAttributeException, IOException {
    
    System.err.println("APF Import: signal="+signalFile);
    URI signalURI = signalFile.toURI();
    AWBDocument doc = AWBDocument.fromSignal (signalURI, ACE2004Task.getInstance(),
                                              "sgml", encoding);

    idTracker = IdTracker.getIdTracker(doc);
    
    int i = 0;
    String sourceType, sourceSource, sourceUri;    /* source_file info      */
    String documentDocId;                          /* document info         */
    int externalResourceCount = 0;
    String [] externalResourceUrn  = new String[100];
    String [] externalResourceName = new String[100];

    // Allows lookup by ID
    LinkedHashMap entityMap          = new LinkedHashMap ();
    LinkedHashMap entityMentionMap   = new LinkedHashMap ();
    LinkedHashMap quantityMap        = new LinkedHashMap ();
    LinkedHashMap quantityMentionMap = new LinkedHashMap ();
    LinkedHashMap timex2Map          = new LinkedHashMap ();
    LinkedHashMap timex2MentionMap   = new LinkedHashMap ();

    // Maps an Annotation to a map of Arguments for delayed id resolution
    LinkedHashMap relationArguments         = new LinkedHashMap();
    LinkedHashMap relationMentionArguments  = new LinkedHashMap();
    LinkedHashMap eventArguments            = new LinkedHashMap();
    LinkedHashMap eventMentionArguments     = new LinkedHashMap();

    
    /* Initialize strings ... */
    sourceType = ""; sourceSource = ""; sourceUri = ""; 
    documentDocId = "";
    
    Iterator sgmlElementsIter = apfDocument.iterator();
    while (sgmlElementsIter.hasNext()) {
      i++;
      SgmlElement element = (SgmlElement) sgmlElementsIter.next();
      if (debugLevel > 0) 
	  System.err.println(" element (" + i + ") = " + element.getGid());

      if (element.getGid().equalsIgnoreCase("source_file")) {
	  if (debugLevel > 0)
	      System.err.println (" GID/source_file = " + element.getGid());
        // ======================================================================
        sourceType    = element.getAttribute("TYPE");
        sourceSource  = element.getAttribute("SOURCE");
        sourceUri     = element.getAttribute("URI");
        // System.err.println("    source_file type = "+sourceType+" source = "
        //                    + sourceSource + " uri = " + sourceUri);

        // store these properties for output when exporting
        // turn sourceURI into an absolute URI
        URI realSourceURI =
          new File(signalFile.getParentFile(),sourceUri).toURI();
        doc.putClientProperty (IdTracker.DOC_SOURCE, sourceSource);
        doc.putClientProperty (IdTracker.DOC_URI, realSourceURI);

      } else if (element.getGid().equalsIgnoreCase("document")) {
        // ======================================================================
	  if (debugLevel > 0)
	      System.err.println (" GID/document = " + element.getGid());
        documentDocId = element.getAttribute("DOCID");
        doc.putClientProperty (IdTracker.DOCID, documentDocId);
        if (debugLevel > 0)
	    System.err.println("    document docid = " + documentDocId);

      } else if (element.getGid().equalsIgnoreCase("external_resource")) {
        // ======================================================================
        System.err.println("    External Resource: NAME=" +
                           element.getAttribute("NAME") + " URN=" +
                           element.getAttribute("URN"));
	
	externalResourceUrn[externalResourceCount]  = element.getAttribute("URN");
	externalResourceName[externalResourceCount] = element.getAttribute("NAME");
	externalResourceCount++;

      } else if (element.getGid().equalsIgnoreCase("entity")) {
        // ======================================================================
	  if (debugLevel > 0)
	      System.err.println (" GID/entity = " + element.getGid());
        boolean isNewEntity    = true;
        String entityId        = element.getAttribute("ID");
        String entityType      = element.getAttribute("TYPE");
        String entitySubtype   = element.getAttribute("SUBTYPE");
        String entityClass     = element.getAttribute("CLASS");
        if (debugLevel > 0)
	    System.err.println("    entity id = " + entityId);

        HasSubordinates entityAnnot =
          (HasSubordinates) doc.createAnnotation (ACE2004Utils.ENTITY_TYPE);
        
        registerAnnot(idTracker, entityAnnot, entityId);

        entityAnnot.setAttributeValue ("ace_id",  entityId);
        entityAnnot.setAttributeValue ("type",    entityType);
        entityAnnot.setAttributeValue ("subtype", entitySubtype);
        entityAnnot.setAttributeValue ("class",   entityClass);

        // map IDs to the annotation for delayed id resolution
        entityMap.put (entityId, entityAnnot);
        
        Iterator entityIter    = element.getChildren().iterator();
        while (entityIter.hasNext()) {
          SgmlElement entitySubElement = (SgmlElement) entityIter.next();
          if (entitySubElement == null)
            continue;
          /* System.err.println(" entitySubElement = " +
             entitySubElement.getOpenTag().tagText); */
          
          String entitySubGid = entitySubElement.getGid();

          if (entitySubGid.equalsIgnoreCase("external_link")) {
            // ======================================================================
            System.out.println("  Handling External Link: RESOURCE=" +
                               entitySubElement.getAttribute("RESOURCE") + " EID=" +
                               entitySubElement.getAttribute("EID"));
	    String externalLinkName = entitySubElement.getAttribute("RESOURCE");
	    String externalLinkEid  = entitySubElement.getAttribute("EID");
	    entityAnnot.setAttributeValue ("external-link-resource-name", externalLinkName);
	    entityAnnot.setAttributeValue ("external-link-eid", externalLinkEid);

          } else if (entitySubGid.equalsIgnoreCase("entity_mention")) {
            // ======================================================================
            String mentionId       = entitySubElement.getAttribute("ID");
            String mentionType     = entitySubElement.getAttribute("TYPE");
            String mentionRole     = entitySubElement.getAttribute("ROLE");
            String mentionLDCType  = entitySubElement.getAttribute("LDCTYPE");
            String mentionRef      = entitySubElement.getAttribute("REFERENCE");
            String mentionMetonymy = entitySubElement.getAttribute("METONYMY_MENTION");
            String mentionLDCAtr   = entitySubElement.getAttribute("LDCATR");
            
            int fullStart=-1, fullEnd=-1, headStart=0, headEnd=0;
            SgmlElement a;
            a = entitySubElement.getEmbeddedElement("extent;charseq;");
            if (a != null) {
              fullStart = Integer.parseInt (a.getAttribute("START"));
              fullEnd   = Integer.parseInt (a.getAttribute("END"));
              // text of full: = apfDocument.getSignalText(a)
            }else {
              System.err.println("--No extent!");
            }
            a = entitySubElement.getEmbeddedElement("head;charseq;");
            if (a != null) {
              headStart = Integer.parseInt (a.getAttribute("START"));
              headEnd   = Integer.parseInt (a.getAttribute("END"));
            } else {
              System.err.println("--No head!");
            }
            if (fullStart < 0 || headStart < 0 ||
                fullEnd < 1   || headEnd < 1) {
              System.err.println ("    ERROR: invalid mention: full=("+
                                  fullStart+","+fullEnd+") head=("+
                                  headStart+","+headEnd+")");
              continue;
            }
	    if (debugLevel > 0)
		System.err.println(" AIF:: mention synType = " + mentionType +
				   " id = " + mentionId +
				   " role = " + mentionRole +
				   " fstart = " + fullStart +
				   " fend = " + fullEnd +
				   " hstart = " + headStart +
				   " hend = " + headEnd);
            NamedExtentRegions mentionAnnot = 
              (NamedExtentRegions)doc.createAnnotation(ACE2004Utils.ENTITY_MENTION_TYPE);
            entityAnnot.addSubordinate (mentionAnnot);
            
            registerAnnot(idTracker, mentionAnnot, mentionId);

            mentionAnnot.setTextExtents("full", fullStart, fullEnd+1);
            mentionAnnot.setTextExtents("head", headStart, headEnd+1);
            mentionAnnot.setAttributeValue("ace_id", mentionId);
            mentionAnnot.setAttributeValue("type", mentionType);
            mentionAnnot.setAttributeValue("role", mentionRole);
            mentionAnnot.setAttributeValue("ldctype", mentionLDCType);
            mentionAnnot.setAttributeValue("reference", mentionRef);
            mentionAnnot.setAttributeValue("metonymy", mentionMetonymy);
            mentionAnnot.setAttributeValue("ldcatr", mentionLDCAtr);

            // map IDs to the annotation for delayed id resolution
	    if (debugLevel > 0)
		System.err.println("DSD: mapping to entityMentionMap: " + mentionId + ", " + mentionAnnot);
            entityMentionMap.put (mentionId, mentionAnnot);
            
            if (isNewEntity) {
              entityAnnot.setAttributeValue("primary-mention", mentionAnnot);
              isNewEntity = false;
            }
            
          } else {
            // ======================================================================
            if (debugLevel > 0)
              System.err.println("  ignoring entitySubElement = "+entitySubGid);
          }
        } // while (entityIter.hasNext())
      
      } else if (element.getGid().equalsIgnoreCase("value")) {
        // ======================================================================
	  if (debugLevel > 0)
	      System.err.println (" GID/value = " + element.getGid());
        boolean isNewQuantity    = true;
        String quantityId        = element.getAttribute("ID");
        String quantityType      = element.getAttribute("TYPE");
        String quantitySubtype   = element.getAttribute("SUBTYPE");
	if (debugLevel > 0)
	    System.err.println("    value id = " + quantityId);

        HasSubordinates quantityAnnot =
          (HasSubordinates) doc.createAnnotation (ACE2004Utils.QUANTITY_TYPE);
        
        registerAnnot(idTracker, quantityAnnot, quantityId);

        quantityAnnot.setAttributeValue ("ace_id",  quantityId);
        quantityAnnot.setAttributeValue ("type",    quantityType);
        quantityAnnot.setAttributeValue ("subtype", quantitySubtype);

        // map IDs to the annotation for delayed id resolution
        quantityMap.put (quantityId, quantityAnnot);
        
        Iterator quantityIter    = element.getChildren().iterator();
        while (quantityIter.hasNext()) {
          SgmlElement quantitySubElement = (SgmlElement) quantityIter.next();
          if (quantitySubElement == null)
            continue;
          /* System.err.println(" quantitySubElement = " +
             quantitySubElement.getOpenTag().tagText); */
          
          String quantitySubGid = quantitySubElement.getGid();

          if (quantitySubGid.equalsIgnoreCase("value_mention")) {
            // ======================================================================
            String mentionId       = quantitySubElement.getAttribute("ID");
            
            int start=-1, end=-1;
            SgmlElement a;
            a = quantitySubElement.getEmbeddedElement("extent;charseq;");
            if (a != null) {
              start = Integer.parseInt (a.getAttribute("START"));
              end   = Integer.parseInt (a.getAttribute("END"));
              // text of full: = apfDocument.getSignalText(a)
            } else {
              System.err.println("--No extent!");
            }
            if (start < 0 || end < 1) {
              System.err.println ("    ERROR: invalid quantity: ("+start+","+end+")");
              continue;
            }
            TextExtentRegion mentionAnnot = 
              (TextExtentRegion)doc.createAnnotation(ACE2004Utils.QUANTITY_MENTION_TYPE);
            quantityAnnot.addSubordinate (mentionAnnot);
            
            registerAnnot(idTracker, mentionAnnot, mentionId);

            mentionAnnot.setAttributeValue ("ace_id", mentionId);
            mentionAnnot.setTextExtents(start, end+1);

            // map IDs to the annotation for delayed id resolution
            quantityMentionMap.put (mentionId, mentionAnnot);
            
            if (isNewQuantity) {
              quantityAnnot.setAttributeValue("primary-mention", mentionAnnot);
              isNewQuantity = false;
            }
            
          } else {
            // ======================================================================
            if (debugLevel > 0)
              System.err.println("  ignoring quantitySubElement = "+quantitySubGid);
          }
        } // while (quantityIter.hasNext())
      
      } else if (element.getGid().equalsIgnoreCase("timex2")) {
        // ======================================================================
	  if (debugLevel > 0)
	      System.err.println (" GID/timex2 = " + element.getGid());
        String timex2Id          = element.getAttribute("ID");
        String timex2Val         = element.getAttribute("VAL");
        String timex2Mod         = element.getAttribute("MOD");
        String timex2AnchorVal   = element.getAttribute("ANCHOR_VAL");
        String timex2AnchorDir   = element.getAttribute("ANCHOR_DIR");
        String timex2Set         = element.getAttribute("SET");
        String timex2NonSpecific = element.getAttribute("NON_SPECIFIC");
        String timex2Comment     = element.getAttribute("COMMENT");

	if (debugLevel > 0)
	    System.err.println("DSD  timex2 id = " + timex2Id);

        TextExtentRegion timex2Annot =
          (TextExtentRegion) doc.createAnnotation (ACE2004Utils.TIMEX2_TYPE);
                                                             
        String aceId = timex2Id + "-1"; // the ace_id is the timex2
                                        // mention id and it's always
                                        // 1 because there is only one
                                        // mention of each timex
        registerAnnot(idTracker, timex2Annot, aceId); // register the aceId, 
                                                      // not the timex2Id

        timex2Annot.setAttributeValue ("timex2-id",  timex2Id);
        timex2Annot.setAttributeValue ("ace_id", aceId);
        timex2Annot.setAttributeValue ("val", timex2Val);
        timex2Annot.setAttributeValue ("mod", timex2Mod);
        timex2Annot.setAttributeValue ("anchor-val", timex2AnchorVal);
        timex2Annot.setAttributeValue ("anchor-dir", timex2AnchorDir);
        timex2Annot.setAttributeValue ("set", timex2Set);
        timex2Annot.setAttributeValue ("non-specific", timex2NonSpecific);
        timex2Annot.setAttributeValue ("comment", timex2Comment);

        // map IDs to the annotation for delayed id resolution
        timex2Map.put (timex2Id, timex2Annot);
        
	int timex2mentionCnt = 0;
        Iterator timex2Iter  = element.getChildren().iterator();
        while (timex2Iter.hasNext()) {
	    timex2mentionCnt = timex2mentionCnt + 1;
	    if (timex2mentionCnt > 1) {
		System.err.println("WARNING: Multiple timex2 mentions within Timex2 element.");
	    }
	    SgmlElement timex2SubElement = (SgmlElement) timex2Iter.next();
	    if (timex2SubElement == null)
		continue;
          
          String timex2SubGid = timex2SubElement.getGid();

          if (timex2SubGid.equalsIgnoreCase("timex2_mention")) {
	      // ======================================================================
	      String mentionId   = timex2SubElement.getAttribute("ID");
	      String mentionText = "";
            
            int start=-1, end=-1;
            SgmlElement a;
            a = timex2SubElement.getEmbeddedElement("extent;charseq;");
            if (a != null) {
              start = Integer.parseInt (a.getAttribute("START"));
              end   = Integer.parseInt (a.getAttribute("END"));
              mentionText  = apfDocument.getSignalText(a);
	      if (debugLevel > 0)
		  System.err.println("DSD: timex2mention start, end, text: " +
				     start + " " + end + " " + mentionText);
            } else {
              System.err.println("--No extent!");
            }
            if (start < 0 || end < 1 || end < start) {
              System.err.println ("    ERROR: invalid timex2 mention: ("+start+","+end+")");
              continue;
            }

	    timex2Annot.setTextExtents(start,end+1);
	    timex2Annot.setAttributeValue("ace_id",mentionId);
	    // map IDs to the annotation for delayed id resolution
	    // NOTE: mentionID and timex2Id map to same annotation object, timex2Annot!!!!
	    timex2MentionMap.put (mentionId, timex2Annot);

	  } else {
	      // ======================================================================
              System.err.println("  ignoring timex2SubElement = "+timex2SubGid);
          }
        } // while (timex2Iter.hasNext())
      
      } else if (element.getGid().equalsIgnoreCase("relation")) {
        // ======================================================================
	  if (debugLevel > 0)
	      System.err.println (" GID/relation = " + element.getGid());
        String relationId       = element.getAttribute("ID");
        String relationType     = element.getAttribute("TYPE");
        String relationSubtype  = element.getAttribute("SUBTYPE");
        String relationModality = element.getAttribute("MODALITY");
        String relationTense    = element.getAttribute("TENSE");

        if (debugLevel > 0)
          System.err.println("  relation element: " + relationId);

        HasSubordinates relationAnnot =
          (HasSubordinates) doc.createAnnotation (ACE2004Utils.RELATION_TYPE);

        registerAnnot(idTracker, relationAnnot, relationId);

        relationAnnot.setAttributeValue ("ace_id", relationId);
        relationAnnot.setAttributeValue("type", relationType);
        relationAnnot.setAttributeValue("subtype", relationSubtype);
        relationAnnot.setAttributeValue("modality", relationModality);
        relationAnnot.setAttributeValue("tense", relationTense);
        
        // delay resolving refids until end of import
        HashMap arguments = new HashMap();
        relationArguments.put(relationAnnot, arguments);
        
        Iterator relationIter  = element.getChildren().iterator();
        while (relationIter.hasNext()) {
          SgmlElement relSubElement = (SgmlElement) relationIter.next();
          if (relSubElement == null)
            continue;

          String relSubGid = relSubElement.getGid();

          if (relSubGid.equalsIgnoreCase("relation_argument")) {
            // ======================================================================

            String role = relSubElement.getAttribute("ROLE");
            String refid = relSubElement.getAttribute("REFID");
            arguments.put(role, refid);
            if (debugLevel > 0)
              System.err.println("  relation_argument: " + role + " + " + refid);
            
          } else if (relSubGid.equalsIgnoreCase("relation_mention")) {
            // ======================================================================
	      int extentStart=-1, extentEnd=-1;
            
            SgmlElement relMentionSGML = relSubElement;

            String relMentionId = relMentionSGML.getAttribute("ID");
            String relationLexCondition = relMentionSGML.getAttribute("LEXICALCONDITION");
            
            if (debugLevel > 0)
              System.err.println("  relation_mention " + relMentionId);
            
            /*
	      HasSubordinates relMentionAnnot = (HasSubordinates) 
              doc.createAnnotation (ACE2004Utils.RELATION_MENTION_TYPE);
	    */
	    AWBAnnotation relMentionAnnot = (AWBAnnotation) 
		doc.createAnnotation (ACE2004Utils.RELATION_MENTION_TYPE);
            relationAnnot.addSubordinate (relMentionAnnot);
            
            registerAnnot(idTracker, relMentionAnnot, relMentionId);
            
            relMentionAnnot.setAttributeValue("ace_id", relMentionId);
            relMentionAnnot.setAttributeValue("lexicalcondition",relationLexCondition);
            
            // delay resolving refids untill end of import
            HashMap mentionArguments = new HashMap();
            relationMentionArguments.put(relMentionAnnot, mentionArguments);
            
            Iterator relMenIter = relMentionSGML.getChildren().iterator();
            while (relMenIter.hasNext()) {
              SgmlElement relMenElement = (SgmlElement) relMenIter.next();
              if (relMenElement == null)
                continue;
              
              // System.err.println(" relMentionSGML = "+ relMentionSGML.getOpenTag());
              String relMenGid = relMenElement.getGid();

              if (relMenGid.equalsIgnoreCase("extent")) {
		  // ======================================================================
		  if (debugLevel > 0) {
		      System.err.println("Processing relation_mention extent...");
		  }
		  SgmlElement rCharSeqSGML = relMenElement.getEmbeddedElement("charseq;");
		  if (rCharSeqSGML != null) {
		      extentStart = Integer.parseInt (rCharSeqSGML.getAttribute("START"));
		      extentEnd   = Integer.parseInt (rCharSeqSGML.getAttribute("END"));
		      // text of full: = apfDocument.getSignalText(a)
		  }
		  if (extentStart < 0 || extentEnd < 1) {
		      System.err.println ("    ERROR: invalid relation mention: extent=("+
					  extentStart+","+extentEnd+")");
		  } else {
		      TextExtentRegion relMentExtent =
			  (TextExtentRegion)doc.createAnnotation(ACE2004Utils.RELATION_MENTION_EXTENT_TYPE);
		      if (debugLevel > 0) 
			  System.err.println("relMentExtent = " + relMentExtent);
		      relMentExtent.setTextExtents(extentStart, extentEnd+1);
		      // For debugging purposes only:
		      if (debugLevel > 0) {
			  int start = relMentExtent.getTextExtentStart();
			  System.err.println("DSD: relMenAnnotTER.getTextExtentStart = " + start);
		      }
		      relMentionAnnot.setAttributeValue("relation-mention-extent", relMentExtent);
		  }

                
              } else if (relMenGid.equalsIgnoreCase("relation_mention_argument")) {
                // ======================================================================
		  if (debugLevel > 0) {
		      System.err.println("Processing relation_mention_argument...");
		  }
                
                String role = relMenElement.getAttribute("ROLE");
                String refid = relMenElement.getAttribute("REFID");
                mentionArguments.put(role, refid);
		if (debugLevel > 0)
		    System.err.println("DSD relation args:  "+relMenGid+": "+role+" + "+refid);
                
              } else {
                System.err.println(" ignoring relMenElement = " + relMenGid); 
              }
            } // while (relMenIter.hasNext())...

            if (debugLevel > 0)
		System.err.println(" finished parsing relation_mention element.");
	    
          } else {
            if (debugLevel > 0)
              System.err.println(" ignoring relSubElement = " + relSubGid);
          } // if (relSubGid.equals(....)

        } // while (relationIter.hasNext())...

      } else if (element.getGid().equalsIgnoreCase("event")) {
        // ======================================================================
        if (debugLevel > 0)
          System.err.println (" GID/event = " + element.getGid());
        boolean isNewEvent     = true;
        String eventId         = element.getAttribute("ID");
        String eventType       = element.getAttribute("TYPE");
        String eventSubtype    = element.getAttribute("SUBTYPE");
        String eventModality   = element.getAttribute("MODALITY");
        String eventPolarity   = element.getAttribute("POLARITY");
        String eventGenericity = element.getAttribute("GENERICITY");
        String eventTense      = element.getAttribute("TENSE");

        // For backward compatibility with 5.1.0
        if ("Unspecified".equalsIgnoreCase(eventModality)) {
          eventModality = "Other";
          System.err.println("ImportAPF5_1_1 encountered 5.1.0 format: Modality = \"Unspecified\" translated to \"Other\"");
        }
        if (eventTense == null) {
          eventTense = "Unspecified";
          System.err.println("ImportAPF5_1_1 encountered 5.1.0 format: Tense = null translated to \"Unpsecified\"");
        }

        HasSubordinates eventAnnot =
          (HasSubordinates) doc.createAnnotation (ACE2004Utils.EVENT_TYPE);
        
        registerAnnot(idTracker, eventAnnot, eventId);

        eventAnnot.setAttributeValue ("ace_id",     eventId);
        eventAnnot.setAttributeValue ("type",       eventType);
        eventAnnot.setAttributeValue ("subtype",    eventSubtype);
        eventAnnot.setAttributeValue ("modality",   eventModality);
        eventAnnot.setAttributeValue ("polarity",   eventPolarity);
        eventAnnot.setAttributeValue ("genericity", eventGenericity);
        eventAnnot.setAttributeValue ("tense",      eventTense);
                    
        // map IDs to the annotation for delayed id resolution
        //eventMap.put (mentionId, mentionAnnot);

        // delay resolving refids until end of import
        HashMap arguments = new HashMap();
        eventArguments.put(eventAnnot, arguments);
        
        Iterator eventIter    = element.getChildren().iterator();
        while (eventIter.hasNext()) {
          SgmlElement eventSubElement = (SgmlElement) eventIter.next();
          if (eventSubElement == null)
            continue;
          /* System.err.println(" eventSubElement = " +
             eventSubElement.getOpenTag().tagText); */
          
          String eventSubGid = eventSubElement.getGid();

          if (eventSubGid.equalsIgnoreCase("event_argument")) {
            // ======================================================================

            String role = eventSubElement.getAttribute("ROLE");
            String refid = eventSubElement.getAttribute("REFID");
            arguments.put(role, refid);
            if (debugLevel > 0)
              System.err.println("  relation_argument: " + role + " + " + refid);
            
          } else if (eventSubGid.equalsIgnoreCase("event_mention")) {
            // ======================================================================
            String mentionId       = eventSubElement.getAttribute("ID");
            String mentionLevel    = eventSubElement.getAttribute("LEVEL");
	    int    ldcScopeStart   = -1;
	    int    ldcScopeEnd     = -1;
            
	    /*
            TextExtentRegion mentionAnnot = 
              (TextExtentRegion) doc.createAnnotation(ACE2004Utils.EVENT_MENTION_TYPE);
	    */
            AWBAnnotation mentionAnnot = 
              (AWBAnnotation) doc.createAnnotation(ACE2004Utils.EVENT_MENTION_TYPE);
            eventAnnot.addSubordinate (mentionAnnot);

            registerAnnot(idTracker, mentionAnnot, mentionId);

            int extentStart=-1, extentEnd=-1, anchorStart=0, anchorEnd=0;
            SgmlElement a;
            a = eventSubElement.getEmbeddedElement("extent;charseq;");
            if (a != null) {
              extentStart = Integer.parseInt (a.getAttribute("START"));
              extentEnd   = Integer.parseInt (a.getAttribute("END"));
              // text of full: = apfDocument.getSignalText(a)
            }
            a = eventSubElement.getEmbeddedElement("ldc_scope;charseq;");
	    // System.err.println("ldc_scope = " + a);
            if (a != null) {
		ldcScopeStart = Integer.parseInt (a.getAttribute("START"));
		ldcScopeEnd   = Integer.parseInt (a.getAttribute("END"));
		// System.err.println("ldcScopeStart = " + ldcScopeStart);
		// System.err.println("ldcScopeEnd = " + ldcScopeEnd);
            }
            a = eventSubElement.getEmbeddedElement("anchor;charseq;");
            if (a != null) {
              anchorStart = Integer.parseInt (a.getAttribute("START"));
              anchorEnd   = Integer.parseInt (a.getAttribute("END"));
            }
            if (anchorStart < 0 || anchorEnd < 1) {
              System.err.println ("    ERROR: invalid relation mention: anchor=("+
                                  anchorStart+","+anchorEnd+") extent=("+
                                  extentStart+","+extentEnd+")");
              continue;
            }

            // delay resolving refids untill end of import
            HashMap mentionArguments = new HashMap();
	    if (debugLevel > 0) {
		System.err.println("Capturing Event Mention Arguments");
		System.err.println("  mentionAnnot = "+mentionAnnot.getAttributeValue("ace_id"));
	    }
            eventMentionArguments.put(mentionAnnot, mentionArguments);
            
            Iterator eventMentSubIter = eventSubElement.getChildren().iterator();
            while (eventMentSubIter.hasNext()) {
              SgmlElement emse = (SgmlElement) eventMentSubIter.next();
              /* System.err.println("DSD eventMentSubElement = "+emse.getOpenTag().tagText); */

              String emseGid = emse.getGid();
	      if (debugLevel > 0)
		  System.err.println("DSD emseGid = "+emseGid);

              if (emseGid.equalsIgnoreCase("event_mention_argument")) {
                // ======================================================================
                // TODO: extent and event_mention_argument.extent?
                
                String role = emse.getAttribute("ROLE");
                String refid = emse.getAttribute("REFID");
                mentionArguments.put(role, refid);
		if (debugLevel > 0)
		    System.err.println("DSD event args:  "+emseGid+": "+role+" = "+refid);
              }
            }
            
	    TextExtentRegion mentionAnnotTER = (TextExtentRegion) mentionAnnot;
            mentionAnnotTER.setTextExtents(anchorStart, anchorEnd+1);

            // create the extent annot if needed
            if (extentStart >= 0 && extentEnd >= 0) {
              TextExtentRegion extent = (TextExtentRegion)
                doc.createAnnotation(ACE2004Utils.EVENT_MENTION_EXTENT_TYPE);
              extent.setTextExtents(extentStart, extentEnd+1);
              mentionAnnotTER.setAttributeValue("extent", extent);
            }
            
            mentionAnnotTER.setAttributeValue("level", mentionLevel);
	    String numAsString = "" + ldcScopeStart;
            mentionAnnotTER.setAttributeValue("ldcscope-start", numAsString);
	    numAsString = "" + ldcScopeEnd;
            mentionAnnotTER.setAttributeValue("ldcscope-end",   numAsString);
            
            if (isNewEvent) {
              eventAnnot.setAttributeValue("primary-mention", mentionAnnotTER);
              isNewEvent = false;
            }
            
          } else {
            // ======================================================================
            if (debugLevel > 0)
              System.err.println("  ignoring eventSubElement = "+eventSubGid);
          }
        } // while (eventIter.hasNext())
        
      } else {
        if (debugLevel > 0)
          System.err.println(" ignoring element (a) = " + element.getGid());
      } // if (element.getGid.equals(...)
        
    } // while (sgmlElementsIter.hasNext())


    // David/2007/08/03
    // Just in case this element was encountered *after* the entity elements were
    // processed, go back and fill in the Urn field for each of the entity models:
    System.out.println("Ensuring that all entities have EID, URN and NAME set...");
    Iterator annotIter = doc.getAllAnnotations ();
    while (annotIter.hasNext ()) {
	AWBAnnotation annot = (AWBAnnotation) annotIter.next();
	String type = annot.getAnnotationType().getName();
	if (type.equals(ACE2004Task.ENTITY_TYPE_NAME)) {
	    String externalLinkName = annot.getAttributeValue("external-link-resource-name").toString();
	    for (int resCount = 0; resCount < externalResourceCount; resCount++) {
		if (externalResourceName[resCount].equals(externalLinkName)) {
		    annot.setAttributeValue ("external-link-resource-urn", externalResourceUrn[resCount]);
		    String externalLinkUrn = annot.getAttributeValue("external-link-resource-urn").toString();
		    System.out.println("Setting link " + externalLinkName + " to " + externalLinkUrn);
		}
	    }
	}
    }
	




    // Now that we've seen all known ID's, assign ID's where missing
    Iterator missingIter = missingId.iterator();
    while (missingIter.hasNext())
      idTracker.getAceId((AWBAnnotation) missingIter.next());
    
    // Now we can map the ace_id's from arguments, to actual annots for
    // relation_mentions
    Iterator relMenEntryIter = relationMentionArguments.entrySet().iterator();
    while (relMenEntryIter.hasNext()) {
      Map.Entry mEntry = (Map.Entry) relMenEntryIter.next();
      // HasSubordinates relMenAnnot = (HasSubordinates) mEntry.getKey();
      AWBAnnotation relMenAnnot = (AWBAnnotation) mEntry.getKey();
      HashMap arguments = (HashMap) mEntry.getValue();

      if (debugLevel > 0) {
        System.err.println("DSD resolving rel args: "+relMenAnnot.getAttributeValue("ace_id"));
        System.err.println("  "+arguments);
      }
      Iterator entryIter = arguments.entrySet().iterator();
      while (entryIter.hasNext()) {
        Map.Entry entry = (Map.Entry) entryIter.next();
        String role = (String) entry.getKey();
        String refid = (String) entry.getValue();

        // add arg1 and arg2 as role-identified subordinates
        if ("Arg-1".equalsIgnoreCase(role)) {
          Object entityMention = entityMentionMap.get(refid);
          relMenAnnot.setAttributeValue("arg1", entityMention);
        }
        else if ("Arg-2".equalsIgnoreCase(role)) {
          Object entityMention = entityMentionMap.get(refid);
          relMenAnnot.setAttributeValue("arg2", entityMention);
        }
        // all others get tossed in after resolving (and atm they are all quantities)
        else {
          AWBAnnotation argument = doc.createAnnotation (ACE2004Utils.ARGUMENT_MENTION_TYPE);
          AWBAnnotation target = (AWBAnnotation) quantityMap.get(refid);
	  if (target != null) {
	      argument.setAttributeValue("role", role);
	      argument.setAttributeValue("quantity-value", target);
	  } else {
	      if (debugLevel > 0)
		  System.err.println("DSDSD: assigning relation argument to timex2-value: " + target);
	      target = (AWBAnnotation) timex2MentionMap.get(refid);
	      argument.setAttributeValue("role", role);
	      argument.setAttributeValue("timex2-value", target);
	  }
          // relMenAnnot.addSubordinate(argument);
	  relMenAnnot.getRegion().addToSubordinateSet(argument);
        }
      }
    }
    
    // References are ignored in relation_arguments in callisto. when writing
    // apf, they are inferred from the relation_mention_arguments. Perhaps some
    // 'sanity' checks with warnings at some point 

    // Unfortunately, in contradiction to the above comment, the
    // relation co-referencing code acutally expects arg1 and arg2
    // within the ace_relation_region to be set when checking relation
    // co-reference.  New annotations formed within Callisto set these
    // args and appear to keep them synchronized.  And who knows where
    // else this might be depended upon.  I am going to change the
    // coreference code to not use these anymore, but since I don't
    // know where else they might be used, it looks like to be safe,
    // we should import them too.  NOTE that I am not checking that
    // they are consistent with the args listed in the contained
    // relation mentions.  So... IF they are inconsistent, and if some
    // other chunk of code is unwisely relying on them being correct,
    // bad things might happen. RK 9/13/05
    
    // relation_arguments
    Iterator relEntryIter = relationArguments.entrySet().iterator();
    while (relEntryIter.hasNext()) {
      Map.Entry mEntry = (Map.Entry) relEntryIter.next();
      // HasSubordinates relAnnot = (HasSubordinates) mEntry.getKey();
      AWBAnnotation relAnnot = (AWBAnnotation) mEntry.getKey();
      HashMap arguments = (HashMap) mEntry.getValue();

      if (debugLevel > 0) {
        System.err.println("DSD resolving rel args: "+relAnnot.getAttributeValue("ace_id"));
        System.err.println("  "+arguments);
      }
      Iterator entryIter = arguments.entrySet().iterator();
      while (entryIter.hasNext()) {
        Map.Entry entry = (Map.Entry) entryIter.next();
        String role = (String) entry.getKey();
        String refid = (String) entry.getValue();

        // add arg1 and arg2 as role-identified subordinates
        if ("Arg-1".equalsIgnoreCase(role)) {
          Object entity = entityMap.get(refid);
          relAnnot.setAttributeValue("arg1", entity);
        }
        else if ("Arg-2".equalsIgnoreCase(role)) {
          Object entity = entityMap.get(refid);
          relAnnot.setAttributeValue("arg2", entity);
        }
      }
    }

    // event_mention_arguments
    Iterator eventMentionIter = eventMentionArguments.entrySet().iterator();
    while (eventMentionIter.hasNext()) {
	Map.Entry mEntry = (Map.Entry) eventMentionIter.next();
	AWBAnnotation eventMentionAnnot = (AWBAnnotation) mEntry.getKey();
	/* HasSubordinates eventMentionAnnotHS = (HasSubordinates) eventMentionAnnot; */
	HashMap arguments = (HashMap) mEntry.getValue();
	Iterator eventIter = arguments.entrySet().iterator();
	while (eventIter.hasNext()) {
	    Map.Entry entry = (Map.Entry) eventIter.next();
	    String role = (String) entry.getKey();
	    String refid = (String) entry.getValue();

	    AWBAnnotation argument = (AWBAnnotation)
		doc.createAnnotation (ACE2004Utils.ARGUMENT_MENTION_TYPE);
        
        // first check quantities, then timex2, then entities
        AWBAnnotation target = (AWBAnnotation) quantityMentionMap.get(refid);
        if (target != null) {
	    // System.err.println("target is a quantity mention.");
	    argument.setAttributeValue("quantity-value", target);
        } else {
	    target = (AWBAnnotation) timex2MentionMap.get(refid);
	    if (target != null) {
		// System.err.println("target is a timex2.");
		argument.setAttributeValue("timex2-value", target);
		// System.err.println(argument.getAttributeValue("timex2-value"));
	    } else {
		target = (AWBAnnotation) entityMentionMap.get(refid);
		if (target != null) {
		    // System.err.println("target is an entity. target = " + target.toString());
		    argument.setAttributeValue("entity-value", target);
		    // System.err.println(argument.getAttributeValue("entity-value"));
		}
	    }
	}
	if (target == null) {
	    System.err.println("target is null");
	} else {
	    argument.setAttributeValue("role", role);
	    /* eventMentionAnnotHS.addSubordinate(argument); */
	    eventMentionAnnot.getRegion().addToSubordinateSet(argument);
	}
      }
    }

    // event_argumenst, like relation_arguments, are ignored and inferred from
    // relation_mention_arguments on export
    
    return doc;
  }

  private void registerAnnot(IdTracker idTracker, AWBAnnotation annot, String id) {
    idTracker.registerAceId (annot, id);
    if (id == null || id.length() == 0)
      missingId.add (annot);
  }

  private class RelMenArgPair {
    String arg1Id, arg2Id;
    public String toString() {
      return "[" + arg1Id + "," + arg2Id + "]";
    }
  }
  
  private class TempReltime {
    String type, val, mod, dir, anchorVal, anchorDir;
    int start = -1;
    int end = -1;
    public String toString () {
      return "<RELTIME type='"+ type +
        "' val='"+ val +
        "' mod='"+ mod +
        "' dir='"+ dir +
        "' start='"+ start +
        "' end='"+ end +
        "'>";
    }
  }

  /**********************************************************************/
  /*  Testing  */
  /**********************************************************************/
  
  static String VERSION = "1";
  static String LASTMOD = "2005-09-26";
  
  public static void main(String[] args) throws IOException {

    System.err.println("Running ImportAPF5.1.1 v." + VERSION +
                       " Last Modified: " + LASTMOD);

    /* Initialize ParseSgml class variables. */
    String  apfFileInString  = "";
    String  textFileOutString = "";
    File    apfFileIn, textFileOut;

    String  sgmlFileInString  = "";
    String  aifFileOutString  = "";
    File    sgmlFileIn;
    File    aifFileOut;
    boolean newFileNameP = false;

    String  argKey     = "";
    String  argValue   = "";
    int     showArgs   = 0;
    int     debugLevel = 0;

    boolean treatAsApfP = false;

    /* Open I/O streams */
    Reader in;
    Writer aifOut;
    Writer textOut;

    /* Parse command line arguments. */
    for (int i = 0; i < args.length; i++) {
      argKey = args[i];
      if ((i + 1) < args.length) 
        argValue = args[i + 1];
      if (argKey.equals("-i"))
        sgmlFileInString = argValue;
      // have to do this right:
      if (argKey.equals("-d"))
        debugLevel = Integer.parseInt (argValue);
      if (argKey.equals("-o"))
        aifFileOutString = argValue;
      if (argKey.equals("-t"))
        textFileOutString = argValue;
      if (argKey.equals("-h"))
        showArgs = 1;
      if (argKey.equals("-a")) {
        apfFileInString = argValue;
      }
      if (argKey.equals("-help"))
        showArgs = 2;
    }
    if (showArgs == 1 || (args.length == 0)) {
      System.err.println("ImportAPF5.0 -h      show abbreviated cmd line args and exit\n" +
                         " -help               show more detailed cmd line args and exit\n" +
                         " -a <input-APF-file> name of APF input file\n" +

                         " -i <in-sgml-file>   name of SGML input file\n" +
                         " -o <out-aif-file>   name of AIF annotations output file\n" +
                         " -t <out-txt-file>   name of text signal output file\n" +
                         " -a <1 or 0>         treat as APF data (default = 0)\n" +

                         "");
      return;
    } else if (showArgs == 2) {
      System.err.println("ImportAPF5.0 -h       show abbreviated cmd line args and exit\n" +
                         " -help                show more detailed cmd line args and exit\n" +
                         " -a <input-APF-file>  name of APF input file\n" +

                         " -i <in-sgml-file>  name of SGML input file\n" +
                         "                      ='stdin' will read from standard input\n" +
                         " -o <out-aif-file>  name of AIF annotations output file\n" +
                         "                      ='stdout' will send to standard output\n" +
                         "                      defaults to <in-sgml-file>.aif\n" +
                         "                      or <in-sgml-file>.<n>.aif\n" +
                         " -t <out-txt-file>  name of text signal output file\n" +
                         "                      defaults to <in-sgml-file>.txt\n" +
                         "                      or <in-sgml-file>.<n>.txt\n" +
                         "                      or stdin.txt if reading from standard input.\n" +
                         " -a <1 or 0>        treat as APF data (default = 0)\n" +
                         "                      APF =df ACE Pilot Format\n" +
                         "");
      return;
    }

    ImportAPF5_1_1 importer = new ImportAPF5_1_1 ();
    importer.debugLevel = debugLevel;
    if (!apfFileInString.equals ("")) {
      apfFileIn = new File(apfFileInString);
      if (!apfFileIn.exists()) {
        System.err.println("Couldn't find input file <<" + apfFileInString);
        return;
      }
      textFileOut = new File(apfFileInString + ".txt");
      String encoding = System.getProperty ("file.encoding").toString();
      AWBDocument doc =
        importer.mapApf2Atlas(apfFileIn.toURI(), textFileOut.toURI(), encoding);

      int suffix = apfFileInString.indexOf(".apf");
      aifFileOutString = apfFileInString.substring(0, suffix);
      aifFileOut = new File(aifFileOutString+".aif.xml");
      URI uri = org.mitre.jawb.io.URLUtils.badURLToURI(aifFileOut.toURL());
      doc.save(uri, true);
    }
  }
}
