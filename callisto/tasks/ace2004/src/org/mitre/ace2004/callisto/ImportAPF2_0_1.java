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

public final class ImportAPF2_0_1 implements Importer {

  private int debugLevel = 0;
  
  /** Fixes bad APF where entity_type was written as abbrivated form */
  private TreeMap entityTypeFix = null;
  
  public ImportAPF2_0_1  () {
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
    return "ACE2003 APF v.2.0.1";
  }
  public String toString () {
    return getFormat();
  }

  public String getDescription () {
    return "Import APF v2.0.1, dropping unknown tags";
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
      System.out.println("ImportAPF2: entering...\n  " + apfURI);
    File apfFile = new File(apfURI);
    sgmlIn = new InputStreamReader (new FileInputStream (apfFile), encoding);
    
    SgmlDocument apfDoc =
      new SgmlDocument (new BufferedReader (sgmlIn), debugLevel);
    
    sgmlIn.close ();
    /*    
    // generate a name for the raw signal text
    if (rawTextURI == null)
      rawTextFile = new File(apfFile.toString()+".txt");
    else
      rawTextFile = new File(rawTextURI);
    
    // Create the text output file consisting of raw text.
    //if (! rawTextFile.exists ())) {
    if (debugLevel > 0)
      System.out.println("ImportAPF2.mapA2A: Generating raw text signal...");
    */
    // Now find the name of the real signal file.
    // This is done by looping over all sgmlElements in the file, looking for
    // either the source_file and/or the document xml elements.
    if (debugLevel > 0)
      System.out.println("    identifying sgml source file...");
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
          System.out.println("    source_file type = "+sourceType+" source = "
                             +sourceSource+" uri = "+sourceUri);
        break;

      } else if (element.getGid().equalsIgnoreCase("document")) {
        documentDocId = element.getAttribute("DOCID");
        if (debugLevel > 0)
          System.out.println("    document docid = " + documentDocId);
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
      System.out.println("Source sgml file <<" + sourceSgmlFile +
                         ">> doesn't exist");
      throw new IOException ("Can't find source file:\n"+sourceSgmlFile);
    }

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

    System.out.println("APF Import: done");
    return doc;
  }

  /* ******************************************************************* *
   * convertToAltas                                                      *
   * ******************************************************************* */

  private AWBDocument convertApfToAtlas (SgmlDocument apfDocument,
                                         File signalFile,
                                         String encoding)
    throws UnmodifiableAttributeException, IOException {
    
    System.out.println("APF Import: converting to Atlas... signal="+signalFile);
    URI signalURI = signalFile.toURI();
    AWBDocument doc = AWBDocument.fromSignal (signalURI, ACE2004Task.getInstance(),
                                              "sgml", encoding);
    
    IdTracker idTracker = IdTracker.getIdTracker (doc);
    
    int i = 0;
    String sourceType, sourceSource, sourceUri;    /* source_file info      */
    String documentDocId;                          /* document info         */
    
    LinkedHashMap entityMap          = new LinkedHashMap ();
    LinkedHashMap mentionMap         = new LinkedHashMap ();
    // this maps relations to arguments 1 and 2, since there's no
    // guarantee that the args will have been seen (and thus in the
    // mentioniMap) before the relationship
    LinkedHashMap relationArgsMap    = new LinkedHashMap ();
    LinkedHashMap relationEntArgsMap = new LinkedHashMap ();
    LinkedHashMap relationMentionMap = new LinkedHashMap ();
    // collect annots that don't already have id's for assignment at end.
    LinkedList missingId = new LinkedList();
    
    /* Initialize strings ... */
    sourceType = ""; sourceSource = ""; sourceUri = ""; 
    documentDocId = "";
    
    Iterator sgmlElementsIter = apfDocument.iterator();
    while (sgmlElementsIter.hasNext()) {
      i++;
      SgmlElement element = (SgmlElement) sgmlElementsIter.next();
      // System.out.println(" element (" + i + ") = " + 
      //                    element.getOpenTag().tagText);

      if (element.getGid().equalsIgnoreCase("source_file")) {
        sourceType    = element.getAttribute("TYPE");
        sourceSource  = element.getAttribute("SOURCE");
        sourceUri     = element.getAttribute("URI");
        // System.out.println("    source_file type = "+sourceType+" source = "
        //                    + sourceSource + " uri = " + sourceUri);

        // store these properties for output when exporting
        // turn sourceURI into an absolute URI
        URI realSourceURI =
          new File(signalFile.getParentFile(),sourceUri).toURI();
        doc.putClientProperty (IdTracker.DOC_SOURCE, sourceSource);
        doc.putClientProperty (IdTracker.DOC_URI, realSourceURI);

      } else if (element.getGid().equalsIgnoreCase("document")) {
        documentDocId = element.getAttribute("DOCID");
        doc.putClientProperty (IdTracker.DOCID, documentDocId);
        // System.out.println("    document docid = " + documentDocId);

      } else if (element.getGid().equalsIgnoreCase("entity")) {
        boolean isNewEntity    = true;
        String entityId        = element.getAttribute("ID");
        // System.out.println("    entity id = " + entityId);

        HasSubordinates entityAnnot =
          (HasSubordinates) doc.createAnnotation (ACE2004Utils.ENTITY_TYPE);
        
        idTracker.registerAceId (entityAnnot, entityId);
        if (entityId == null || entityId.length() == 0)
          missingId.add(entityAnnot);

        entityAnnot.setAttributeValue ("ace_id", entityId);
        // map entityID's to the entity for linking relations
        entityMap.put(entityId, entityAnnot);
          
        Iterator entityIter    = element.getChildren().iterator();
        while (entityIter.hasNext()) {
          SgmlElement entitySubElement = (SgmlElement) entityIter.next();
          /* System.out.println(" entitySubElement = " +
             entitySubElement.getOpenTag().tagText); */
          if (entitySubElement == null)
            continue;

          
          String entitySubGid = entitySubElement.getGid();

          if (entitySubGid.equalsIgnoreCase("entity_type")) {

            // GENERIC is now CLASS="GEN"
            String entityGeneric     = element.getAttribute("GENERIC");
            if (entityGeneric != null &&
                entityGeneric.equalsIgnoreCase("FALSE"))
              entityAnnot.setAttributeValue("class", "GEN");
            
            String entityType  = apfDocument.getSignalText(entitySubElement);
            // some APF use abbreviated entity_types
            if (entityType != null) {
              //System.out.println(" entityType = " + entityType);
              if (entityType.equalsIgnoreCase("ORGANIZATION") ||
                  entityType.equalsIgnoreCase("PERSON") ||
                  entityType.equalsIgnoreCase("FACILITY") ||
                  entityType.equalsIgnoreCase("LOCATION"))
                entityType = entityType.substring(0,3);
            };
            entityAnnot.setAttributeValue ("type", entityType);
              
	  } else if (entitySubGid.equalsIgnoreCase("entity_mention")) {
            String mentionId       = entitySubElement.getAttribute("ID");
            String mentionType     = entitySubElement.getAttribute("TYPE");
            String mentionRole     = entitySubElement.getAttribute("ROLE");
            //String mentionLDCType  = entitySubElement.getAttribute("LDCTYPE");
            String mentionRef      = entitySubElement.getAttribute("REFERENCE");
            //String mentionMetonymy = entitySubElement.getAttribute("METONYMY_MENTION");
            //String mentionLDCAtr   = entitySubElement.getAttribute("LDCATR");
            int fullStart=-1, fullEnd=-1, headStart=0, headEnd=0;
            SgmlElement a;
            a = entitySubElement.getEmbeddedElement("extent;charseq;start;");
            if (a != null)
              fullStart = Integer.parseInt (apfDocument.getSignalText(a));
            a = entitySubElement.getEmbeddedElement("extent;charseq;end;");
            if (a != null)
              fullEnd = Integer.parseInt (apfDocument.getSignalText(a));
            a = entitySubElement.getEmbeddedElement("head;charseq;start;");
            if (a != null)
              headStart = Integer.parseInt (apfDocument.getSignalText(a));
            a = entitySubElement.getEmbeddedElement("head;charseq;end;");
            if (a != null)
              headEnd   = Integer.parseInt (apfDocument.getSignalText(a));
            if (fullStart < 0 || headStart < 0 ||
                fullEnd < 1   || headEnd < 1) {
              System.err.println ("    ERROR: invalid mention: full=("+
                                  fullStart+","+fullEnd+") head=("+
                                  headStart+","+headEnd+")");
              continue;
            }
            /*System.out.println
              (" AIF:: mention synType = " + mentionType +
              " id = " + mentionId +
              " role = " + mentionRole +
              " fstart = " + mentionFullStart +
              " fend = " + mentionFullEnd +
              " hstart = " + mentionHeadStart +
              " hend = " + mentionHeadEnd); */
            NamedExtentRegions mentionAnnot = 
              (NamedExtentRegions)doc.createAnnotation(ACE2004Utils.ENTITY_MENTION_TYPE);
            entityAnnot.addSubordinate (mentionAnnot);
            idTracker.registerAceId (mentionAnnot, mentionId);
            if (mentionId == null || mentionId.length() == 0)
              missingId.add (mentionAnnot);

            mentionAnnot.setTextExtents("full", fullStart, fullEnd+1);
            mentionAnnot.setTextExtents("head", headStart, headEnd+1);
            mentionAnnot.setAttributeValue("type", mentionType);
            mentionAnnot.setAttributeValue("role", mentionRole);
            //mentionAnnot.setAttributeValue("ldctype", mentionLDCType);
            mentionAnnot.setAttributeValue("reference", mentionRef);
            //mentionAnnot.setAttributeValue("metonymy", mentionMetonymy);
            //mentionAnnot.setAttributeValue("ldcatr", mentionLDCAtr);
            if (isNewEntity) {
              entityAnnot.setAttributeValue("primary-mention", mentionAnnot);
              isNewEntity = false;
            }
            
            // map mentionID's to the mention for linking relation_mentions
            mentionMap.put (mentionId, mentionAnnot);
            
          } else {
            //System.out.println("  ignoring entitySubElement = "+entitySubGid);
          }
        } // while (entityIter.hasNext())
      
      } else if (element.getGid().equalsIgnoreCase("relation")) {
        String relationId      = element.getAttribute("ID");
        String relationType    = element.getAttribute("TYPE");
        String relationSubType = element.getAttribute("SUBTYPE");
        //String relationClass   = element.getAttribute("CLASS"); removed from spec
        /* System.out.println("    [skipping aif generation for now!]"+
                              " relation id = " + relationId +
                              " type = " + relationType +
                              " subType = " + relationSubType + 
                              " class = " + relationClass);*/
        HasSubordinates relationAnnot =
          (HasSubordinates) doc.createAnnotation (ACE2004Utils.RELATION_TYPE);
        idTracker.registerAceId (relationAnnot, relationId);
        relationAnnot.setAttributeValue ("ace_id", relationId);
        if (relationId == null || relationId.length() < 0)
          missingId.add(relationAnnot);

        relationAnnot.setAttributeValue("type",relationType);
        relationAnnot.setAttributeValue("subtype",relationSubType);
        /*
        if ("EXPLICIT".equalsIgnoreCase(relationClass)) {
          relationAnnot.setAttributeValue ("ace_rdc-is-explicit", "TRUE");
        } else if ("IMPLICIT".equalsIgnoreCase(relationClass)) {
          relationAnnot.setAttributeValue ("ace_rdc-is-explicit", "FALSE");
        }
        */
        
        // were entities loaded already? delay resolving entityIds until end of
        // import w/ arg pair, and if entity_args not found, infer. from
        // relMentions
        RelMenArgPair entArgPair = new RelMenArgPair();
        relationEntArgsMap.put(relationAnnot, entArgPair);
        
        Iterator relationIter  = element.getChildren().iterator();
        while (relationIter.hasNext()) {
          SgmlElement relSubElement = (SgmlElement) relationIter.next();
          if (relSubElement == null)
            continue;

          String relSubGid = relSubElement.getGid();
          if (relSubGid.equalsIgnoreCase("rel_entity_arg")) {
            
            //System.err.println("   ignoring rel_entity_arg: "+
            //                   relSubElement.getOpenTag());
            String entArgNum = relSubElement.getAttribute("ARGNUM");
            //System.out.println(" relationArgNum = <<"+relationArgNum+">>");
            if (entArgNum.equals("1")) {
              entArgPair.arg1Id = relSubElement.getAttribute("ENTITYID");
            } else {
              entArgPair.arg2Id = relSubElement.getAttribute("ENTITYID");
            }

          } else if (relSubGid.equalsIgnoreCase("relation_mentions")) {
            
            Iterator relMentionsIter = relSubElement.getChildren().iterator();
            while (relMentionsIter.hasNext()) {
              SgmlElement relMention = (SgmlElement) relMentionsIter.next();
              if (relMention == null)
                continue;
              
              // System.out.println(" relMention = "+relMention.getOpenTag());
              if (relMention.getGid().equalsIgnoreCase("relation_mention")) {
                String relMentionId = relMention.getAttribute("ID");

                HasSubordinates relMentionAnnot = (HasSubordinates) 
                  doc.createAnnotation (ACE2004Utils.RELATION_MENTION_TYPE);
                relationAnnot.addSubordinate (relMentionAnnot);
                
                idTracker.registerAceId (relMentionAnnot, relMentionId);
                relMentionAnnot.setAttributeValue ("ace_id", relMentionId);
                if (relMentionId == null || relMentionId.length() == 0)
                  missingId.add(relMentionAnnot);

                // were mentions loaded already? delay resolving
                // mentionId untill end of import w/ arg pair
                RelMenArgPair argPair = new RelMenArgPair();
                relationArgsMap.put (relMentionAnnot, argPair);

                // collect reltimes then combine VALUE & ANCHOR
                // types of equal extent below
                LinkedHashSet reltimeSet = new LinkedHashSet ();
                
                Iterator relMenIter = relMention.getChildren().iterator();
                while (relMenIter.hasNext()) {
                  SgmlElement relMenElement = (SgmlElement) relMenIter.next();
                  if (relMenElement == null)
                    continue;

                  // System.out.println(" relMention = "+
                  //                    relMention.getOpenTag());
                  String relMenGid = relMenElement.getGid();
                  if (relMenGid.equalsIgnoreCase("rel_mention_arg")) {
                    String relMentionArg = relMenElement.getAttribute("ARGNUM");
                    if (relMentionArg.equals("1")) {
                      argPair.arg1Id = relMenElement.getAttribute("MENTIONID");
                    } else {
                      argPair.arg2Id = relMenElement.getAttribute("MENTIONID");
                    }
                    // we ignore the charseq, as it is redundant with
                    // information obtainable by the mention reference, which
                    // will be used as ground truth

                  } else if (relMenGid.equalsIgnoreCase("rel_mention_time")) {
                    // times are now referenced 'quantity' annots

                    //System.err.println ("RELTIME: "+relMenElement.getOpenTag());
                    TempReltime reltime = new TempReltime ();
                    reltime.type = relMenElement.getAttribute("TYPE");
                    
                    // consider for import, only if valid. Turns out much data
                    // was created /without/ extents for these, so we turn them
                    // into mentionless quantities
                    if (reltime.type == null ||
                        ! (reltime.type.equalsIgnoreCase ("VALUE") ||
                           reltime.type.equalsIgnoreCase ("ANCHOR"))) {
                      System.err.println("  ERROR ignoring invalid: <rel_mention_time>: "+
                                         " in relation "+
                                         relationAnnot.getAttributeValue("ace_id")+
                                         ": type='"+ reltime.type +
                                         "' val='"+ reltime.val +
                                         "' mod='"+ reltime.mod +
                                         "' dir='"+ reltime.dir +"'>");
                    }

                    if (reltime.type.equalsIgnoreCase ("VALUE")) {
                      reltime.val  = relMenElement.getAttribute("VAL");
                      reltime.mod  = relMenElement.getAttribute("MOD");
                    } else { // ANCHOR
                      reltime.anchorVal = relMenElement.getAttribute("VAL");
                      reltime.anchorDir = relMenElement.getAttribute("DIR");
                    }
                    
                    SgmlElement a;
                    a = relMenElement.getEmbeddedElement("source;extent;charseq;start;");
                    if (a != null) {
                      reltime.start = Integer.parseInt (apfDocument.getSignalText(a));
                      a = relMenElement.getEmbeddedElement("source;extent;charseq;end;");
                      if (a != null) {
                        reltime.end = Integer.parseInt(apfDocument.getSignalText(a));
                      }
                    }
                    reltimeSet.add (reltime);

                  } else {
                    System.out.println(" ignoring relMenElement = " + 
                                       relMenElement.getOpenTag()); 
                  }
                } // while (relMenIter.hasNext())...
                
                // converge reltimes of like extents
                // TODO: is this even the right thing to do? fix the output in
                // Exporter if not loop over each good reltime comparing it
                // with all others, removing each as matched up
                Iterator outerReltimeIter = reltimeSet.iterator ();
                while (outerReltimeIter.hasNext()) {
                  // set outer to null to hop out of inner loop
                  TempReltime outer = (TempReltime) outerReltimeIter.next();
                  TempReltime inner = null;
                  //System.err.println ("-- "+reltimeSet.size()+
                  //                    " Creating Reltime:  outer="+outer);
                  
                  // if an anchor is merged into an outer, it's anchorVal will
                  // be nulled below, don't merge or create
                  if (outer.val == null && outer.anchorVal == null)
                    continue;

                  // loop over the remaining times to see if there's a merge possible
                  Iterator innerReltimeIter = reltimeSet.iterator ();
                  while (innerReltimeIter.hasNext() && outer != null) {
                    inner = (TempReltime) innerReltimeIter.next();
                    if (inner == outer) {
                      if (reltimeSet.size()==1) { // force creation
                        inner = null;
                        break;
                      }
                      else // don't compare to self!
                        continue;
                    }
                    //System.err.println (" inner="+inner);
                    
                    if (outer.start >= 0 && outer.end >= 0 &&
                        outer.start == inner.start && outer.end == inner.end) {

                      // if the two arent' the same type (VALUE | ANCHOR), combine
                      if (! outer.type.equalsIgnoreCase (inner.type)) {
                        // ok combine the ANCHOR with VALUE and remove
                      
                        TempReltime valueRT =
                          outer.type.equalsIgnoreCase("VALUE") ? outer : inner;
                        TempReltime anchorRT =
                          valueRT==outer ? inner : outer;

                        valueRT.anchorVal = anchorRT.anchorVal;
                        valueRT.anchorDir = anchorRT.anchorDir;

                        // if we just merged an outer anchor into a value, move
                        // to next outer
                        if (outer.type.equalsIgnoreCase("ANCHOR")) {
                          outer = null;
                        } else {
                          inner.anchorVal = null;
                        }
                          break;
                      } // if(! outer.type....
                    }
                  } // while (innerReltimeIter.hasNext()...

                  // if outer was an anchor merged with a later value, don't
                  // create an annot for it
                  if (outer != null) {
                    // create the quantity and associated arguments!
                    //System.err.println ("   combining!");
                    HasSubordinates quantityAnnot = (HasSubordinates)
                      doc.createAnnotation (ACE2004Utils.QUANTITY_TYPE);
                    idTracker.getAceId(quantityAnnot);
                    
                    AWBAnnotation quantityMentAnnot = null;

                    // if there was no extent, there's no mention
                    if (outer.start > 0) {
                      quantityMentAnnot =
                        doc.createAnnotation (ACE2004Utils.QUANTITY_MENTION_TYPE,
                                              outer.start, outer.end+1, null);
                      quantityAnnot.addSubordinate(quantityMentAnnot);
                      idTracker.getAceId(quantityMentAnnot);
                    }
                    quantityAnnot.setAttributeValue("type","TIMEX2");
                    quantityAnnot.setAttributeValue("timex2_val",outer.val);
                    quantityAnnot.setAttributeValue("timex2_mod",outer.mod);
                    quantityAnnot.setAttributeValue("timex2_anchor-val",outer.anchorVal);
                    quantityAnnot.setAttributeValue("timex2_anchor-dir",outer.anchorDir);

                    // associate with the relation and relation-mention via
                    // arguments, leaving role empty, as we've no idea what
                    // role the time plays
                    HasSubordinates argAnnot = (HasSubordinates)
                      doc.createAnnotation(ACE2004Utils.ARGUMENT_TYPE);
                    // eh? currently they don't have ids... booch?
                    //idTracker.getAceId(argAnnot);
                    
                    argAnnot.setAttributeValue("quantity-value", quantityAnnot);
                    relationAnnot.addSubordinate(argAnnot);

                    if (quantityMentAnnot != null) {
                      AWBAnnotation argMentionAnnot =
                        doc.createAnnotation (ACE2004Utils.ARGUMENT_MENTION_TYPE);
                      argMentionAnnot.setAttributeValue("quantity-value", quantityMentAnnot);

                      relMentionAnnot.addSubordinate(argMentionAnnot);
                      // eh? currently they don't have ids... booch?
                      //idTracker.getAceId(argMentionAnnot);
                    }
                  }
                  
                  // done with the outer
                  outerReltimeIter.remove();
                  
                }// while (outerReltimes.hasNext())...

              } // if (...equalsIgnoreCase("relation_mention"))
            } // while (relMentionsIter.hasNext())...
          } else {
              System.out.println(" ignoring relSubElement = " +
                                 relSubElement.getOpenTag());
          } // if (relSubGid.equals(....)

        } // while (relationIter.hasNext())...

      } else {
        //System.out.println(" ignoring element = " + element.getOpenTag());
      } // if (element.getGid.equals(...)

    } // while (sgmlElementsIter.hasNext())

    // Now that we've seen all known ID's assign ID's where missing
    Iterator missingIter = missingId.iterator();
    while (missingIter.hasNext())
      idTracker.getAceId((AWBAnnotation) missingIter.next());
    
    // Now we can map the mentionId's in mentionRelations to actual mentions
    Iterator relMentIter = relationArgsMap.keySet().iterator();
    while (relMentIter.hasNext()) {
      HasSubordinates relMenAnnot = (HasSubordinates) relMentIter.next();
      RelMenArgPair argPair = (RelMenArgPair)relationArgsMap.get (relMenAnnot);
      relMenAnnot.setAttributeValue ("arg1", mentionMap.get (argPair.arg1Id));
      relMenAnnot.setAttributeValue ("arg2", mentionMap.get (argPair.arg2Id));
    }

    // Now we can map the entityId's in relations to actual entities, or infer
    Iterator relationIter = relationEntArgsMap.keySet().iterator();
    while (relationIter.hasNext()) {
      HasSubordinates relationAnnot = (HasSubordinates) relationIter.next();
      RelMenArgPair argPair = (RelMenArgPair)relationEntArgsMap.get (relationAnnot);
      HasSubordinates arg1 = (HasSubordinates) entityMap.get (argPair.arg1Id);
      HasSubordinates arg2 = (HasSubordinates) entityMap.get (argPair.arg2Id);

      // TODO: infer null args from relation-mentions here

      relationAnnot.setAttributeValue ("arg1", arg1);
      relationAnnot.setAttributeValue ("arg2", arg2);
    }

    return doc;
  }

  private class RelMenArgPair {
    String arg1Id, arg2Id;
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
  
  static String VERSION = "2";
  static String LASTMOD = "2003-08-08";
  
  public static void main(String[] args) throws IOException {

    System.out.println("Running ImportAPF2 v." + VERSION +
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
      System.out.println("ImportAPF2 -h         show abbreviated cmd line args and exit\n" +
                         " -help               show more detailed cmd line args and exit\n" +
                         " -a <input-APF-file> name of APF input file\n" +

                         " -i <in-sgml-file>   name of SGML input file\n" +
                         " -o <out-aif-file>  name of AIF annotations output file\n" +
                         " -t <out-txt-file>  name of text signal output file\n" +
                         " -a <1 or 0>        treat as APF data (default = 0)\n" +

                         "");
      return;
    } else if (showArgs == 2) {
      System.out.println("ImportAPF2 -h          show abbreviated cmd line args and exit\n" +
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

    ImportAPF2_0_1  importer = new ImportAPF2_0_1  ();
    importer.debugLevel = debugLevel;
    if (!apfFileInString.equals ("")) {
      apfFileIn = new File(apfFileInString);
      if (!apfFileIn.exists()) {
        System.out.println("Couldn't find input file <<" + apfFileInString);
        return;
      }
      textFileOut = new File(apfFileInString + ".txt");
      String encoding = System.getProperty ("file.encoding").toString();
      importer.mapApf2Atlas(apfFileIn.toURI(), textFileOut.toURI(), encoding);
    }
  }
}
