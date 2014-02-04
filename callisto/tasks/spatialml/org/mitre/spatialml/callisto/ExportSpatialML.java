/*
 * Copyright (c) 2002-2008 The MITRE Corporation
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

package org.mitre.spatialml.callisto;

import gov.nist.atlas.util.ATLASElementSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.AWBDocument;
import org.mitre.jawb.atlas.TextExtentRegion;
import org.mitre.jawb.io.OverlappingElementException;
import org.mitre.jawb.io.SgmlDocument;
import org.mitre.jawb.io.SgmlElement;
import org.mitre.jawb.tasks.Exporter;
import org.mitre.jawb.tasks.Importer;

public class ExportSpatialML implements Exporter {

  /** Constant for storing document in a map */
  public static final String SGML_DOC_KEY = "Sgml::ParsedDocument";

  public static final int DEBUG = 0;

  public ExportSpatialML () {}

  public String getFormat () {
    return "Spatial ML";
  }
  public String toString () {
    return getFormat ();
  }

  public String getDescription () {
    return "Accepts SGML, and maintains unrecognized tags in an SGML structure.";
  }

  public boolean exportDocument (AWBDocument doc, URI uri)
  throws IOException {

    // add AIF tags to the sgml document
    //    map the aif tags to sgml tags
    SgmlDocument sgmlDoc = convertAtlasToSpatialML (doc);
    String encoding = doc.getEncoding();

    Writer exportWriter = new OutputStreamWriter
    (new FileOutputStream (new File(uri)), encoding);
    exportWriter = new BufferedWriter (exportWriter);

    sgmlDoc.writeSgml (exportWriter);
    exportWriter.close();

    return true;
  }

  private SgmlDocument convertAtlasToSpatialML (AWBDocument doc) {
    // retrieve the original sgml document or create a new one
    SgmlDocument sgmlDoc = new SgmlDocument (doc.getSignal().getCharsAt(0));


    // now try to add the spam back in, dropping them if they cause
    // problems
    List sgmlList = (List)doc.getClientProperty (AWBDocument.SGML_TAG_LIST_KEY);
    if (sgmlList != null) {
      //try {sgmlDoc.writeSgml (new PrintWriter(System.err));}catch(Exception e){};
      for (Iterator iter = sgmlList.iterator (); iter.hasNext(); ) {
        SgmlElement tag = (SgmlElement)iter.next();
        //System.err.println ("Spam: "+tag.getOpenTag() + "(" + tag.getStart() + ", " + tag.getEnd() + ")");
        try {
          sgmlDoc.addElement (tag, false);
        } catch (IndexOutOfBoundsException e) {
          System.err.println (" Tango: SGML tag found on import no longer fits!\n"+e);
        } catch (OverlappingElementException e) {
          System.err.println (" Tango: SGML tag found on import now overlaps existing tag!\n"+e);
        }
      }
    }

    
    // copy over annotations from the AWBDocument
    Iterator annotIter = doc.getAllAnnotations();
    while (annotIter.hasNext()) {
      AWBAnnotation annot = (AWBAnnotation)annotIter.next();
      String typeName = annot.getAnnotationType ().getName();

      if (DEBUG > 0)
        System.err.println(" SpatialML:: "+typeName);
      if (typeName.equals(SpatialMLTask.PLACE_NAME) || typeName.equals(SpatialMLTask.SIGNAL_NAME)) {
        // places and signals are pretty straightforward...
        int start           = ((TextExtentRegion)annot).getTextExtentStart ();
        int end             = ((TextExtentRegion)annot).getTextExtentEnd ();
        SgmlElement element = sgmlDoc.createContentTag (start, end, typeName, true);

        String[] attributeKeys = annot.getAttributeKeys();
        for (int i = 0; i < attributeKeys.length; i++) {
          String attrib = attributeKeys[i];
          if (!attrib.startsWith(TextExtentRegion.TEXT_EXTENT)) { // TODO: HACK! this filters out the TextExtent stuff, but it ought to be more elegant
            copyAttribute(annot, element, attrib);
          }
        }


      
      } else if (typeName.equals(SpatialMLTask.RLINK_NAME)) {

        // put all paths at the end as empty tags
        
        SgmlElement element = sgmlDoc.createEmptyTag(sgmlDoc.getEnd(), typeName, true); // nested flag puts this inside whatever's there
        
        System.err.println("   RLINK: "+ "(" + element.getStart() + ", " + element.getEnd() + ")");

        String[] attributeKeys = annot.getAttributeKeys();
        for (int i = 0; i < attributeKeys.length; i++) {
          String attrib = attributeKeys[i];
          if (!attrib.equals("source") && !attrib.equals("target")) {
            copyAttribute(annot, element, attrib);
          }
        }
        
        
        // now grab the sub-annotations
        ATLASElementSet subs = annot.getRegion().getSubordinateSet(SpatialMLUtils.SIGNAL_TYPE);
        
        // build out the list of sub elements
        AWBAnnotation el  = null;
        Iterator it = subs.iterator();
        if (it.hasNext()) {
          el = SpatialMLUtils.convertAnnotation(it.next());
          
          StringBuffer buf = new StringBuffer(); 
          if (el != null) {
          
            buf.append(el.getAttributeValue("id").toString());
            
            el = null;
            
            while (it.hasNext()) {
              el = SpatialMLUtils.convertAnnotation(it.next());
              
              if (el != null) {
                buf.append(" ");
                buf.append(el.getAttributeValue("id").toString());
              }
              el = null;
            }
          }
          
          setAttribute(element, "signals", buf.toString());

        }
        
        AWBAnnotation source = SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("source"));
        if (source != null) {
          setAttribute(element, "source", source.getAttributeValue("id").toString());
        }
        AWBAnnotation target = SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("target"));
        if (target != null) {
          setAttribute(element, "target", target.getAttributeValue("id").toString());
        }

      } else if (typeName.equals(SpatialMLTask.LINK_NAME)) {
        
        // put all links at the end
        
        SgmlElement element = sgmlDoc.createEmptyTag(sgmlDoc.getEnd(), typeName, true); // nested flag puts this inside whatever tag's there
        
        System.err.println("   LINK: "+ "(" + element.getStart() + ", " + element.getEnd() + ")");
        
        String[] attributeKeys = annot.getAttributeKeys();
        for (int i = 0; i < attributeKeys.length; i++) {
          String attrib = attributeKeys[i];
          if (!attrib.equals("source") && !attrib.equals("target") &&
              !attrib.equals("sourceRlink") && !attrib.equals("targetRlink")) {
            copyAttribute(annot, element, attrib);
          }
        }

        // now grab the sub-annotations
        ATLASElementSet subs = annot.getRegion().getSubordinateSet(SpatialMLUtils.SIGNAL_TYPE);
        
        // build out the list of sub elements
        AWBAnnotation el  = null;
        Iterator it = subs.iterator();
        if (it.hasNext()) {
          el = SpatialMLUtils.convertAnnotation(it.next());
          
          StringBuffer buf = new StringBuffer(); 
          if (el != null) {
          
            buf.append(el.getAttributeValue("id").toString());
            
            el = null;
            
            while (it.hasNext()) {
              el = SpatialMLUtils.convertAnnotation(it.next());
              
              if (el != null) {
                buf.append(" ");
                buf.append(el.getAttributeValue("id").toString());
              }
              el = null;
            }
          }
          
          setAttribute(element, "signals", buf.toString());

        }

        // handle both types of source and target (RLINK and PLACE)
        AWBAnnotation source = SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("source"));
        if (source != null) {
          setAttribute(element, "source", source.getAttributeValue("id").toString());
        } else {
          source = SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("sourceRlink"));
          if (source != null) {
            setAttribute(element, "source", source.getAttributeValue("id").toString());
          }
          
        }
        AWBAnnotation target = SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("target"));
        if (target != null) {
          setAttribute(element, "target", target.getAttributeValue("id").toString());
        } else {
          target = SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("targetRlink"));
          if (target != null) {
            setAttribute(element, "target", target.getAttributeValue("id").toString());
          }
        }
        
      } else { // all else is meta
        if (DEBUG > 0)
          System.err.println(" SpatialML:: META text = " +typeName);
      }
    }
    
    SgmlElement wrapper = sgmlDoc.createContentTag(sgmlDoc.getStart(), sgmlDoc.getEnd(), SpatialMLTask.ROOT_NAME, false);
    setAttribute(wrapper, "version", SpatialMLTask.SPATIALML_VERSION);
    
    return sgmlDoc;
  }


  /** Convience methods */
  private void copyAttribute (AWBAnnotation annot, SgmlElement element,
      String attrName) {
    copyAttribute(annot, element, attrName, attrName);
  }

  private void copyAttribute (AWBAnnotation annot, SgmlElement element,
      String attrName, String attrOutputName) {
    try {
      String attrValue = (String)annot.getAttributeValue (attrName);

      setAttribute(element, attrOutputName, attrValue);
      
    } catch (Exception x) { // shouldn't ever happen
      System.err.println ("Couldn't copy attribute: "+
          annot.getAnnotationType().getName()+
          "."+attrName);
    }
  }

  private void setAttribute(SgmlElement element, String attrOutputName, String attrValue) {
    if (attrValue != null && !attrValue.equals("")) { // filter out all the empty ones
      if (attrValue.equals("YES")) { // filter booleans appropriately
        attrValue = "true";
      }
      attrValue = "\"" + attrValue + "\"";
      element.putAttribute (attrOutputName, attrValue);
    }
  }

  
  public static void main (String[] args) throws IOException {
    Exporter exporter = new ExportSpatialML();
    Importer importer = new ImportSpatialML(new SpatialMLTask ());

    if (args.length < 2) {
      System.err.println ("\nUsage: ExportSpatialML <spatial-in> <spatial-ML-out>\n"+
      "       uses the ImportSpatialML to convert to atlas, then right back");
      return;
    }
    File spIn = new File (args[0]);
    File spOut = new File (args[1]);

    AWBDocument doc = importer.importDocument (spIn.toURI(), "UTF-8");
    boolean success = exporter.exportDocument (doc, spOut.toURI());
    System.err.println ("\nSuccessfull = "+success);
  }
}
