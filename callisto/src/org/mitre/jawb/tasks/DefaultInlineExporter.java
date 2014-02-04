/*
 * Copyright (c) 2002-2009 The MITRE Corporation
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

package org.mitre.jawb.tasks;

import java.io.*;
import java.net.URI;
import java.util.*;
import javax.swing.JOptionPane;

import gov.nist.atlas.*;
import gov.nist.atlas.type.AnnotationType;

import org.mitre.jawb.atlas.*;
import org.mitre.jawb.io.*;
import org.mitre.jawb.tasks.*;

public class DefaultInlineExporter implements Exporter {

  public static final int DEBUG = 0;

  protected Task task;
  protected String format;
  private String descr = "All simple text extent tags are exported to an SGML document.  Text attributes are also exported. Non-Task tags from the original document are then inserted back into the SGML, and tags with crossing extents are dropped.";

  public DefaultInlineExporter (Task task, String format) {
    this.task = task;
    this.format = format;
  }
  public String getFormat() {
    return format;
  }
  public String toString() {
    return getFormat ();
  }

  /** Set the description (else a default is used). */
  public void setDescription(String descr) {
    this.descr = descr;
  }

  public String getDescription () {
    return descr;
  }
    
  public boolean exportDocument (AWBDocument doc, URI uri)
    throws IOException {

    // add AIF tags to the sgml document
    //    map the aif tags to sgml tags
    SgmlDocument sgmlDoc = convertAtlasToSgml (doc);
    String encoding = doc.getEncoding();
    
    Writer exportWriter = new OutputStreamWriter
      (new FileOutputStream (new File(uri)), encoding);
    exportWriter = new BufferedWriter (exportWriter);

    sgmlDoc.writeSgml (exportWriter);
    exportWriter.close();

    return true;
  }

  private SgmlDocument convertAtlasToSgml (AWBDocument doc) {

    SgmlDocument sgmlDoc = new SgmlDocument (doc.getSignal().getCharsAt(0));

    // copy over annotations from the AWBDocument
    Iterator annotIter = doc.getAllAnnotations();
    while (annotIter.hasNext()) {
      AWBAnnotation annot = (AWBAnnotation)annotIter.next();

      convertAnnotationToSgml(sgmlDoc, annot);
    }
    
    // now try to add the spam back in, remembering those who's extents cross
    List sgmlTags = (List)doc.getClientProperty(AWBDocument.SGML_TAG_LIST_KEY);
    List xTags = new LinkedList ();
    if (sgmlTags != null) {
      //try {sgmlDoc.writeSgml (new PrintWriter(System.err));}catch(Exception e){};
      for (Iterator iter = sgmlTags.iterator (); iter.hasNext(); ) {
        SgmlElement tag = (SgmlElement)iter.next();
        if (DEBUG > 1)
          System.err.println (" DefaultExporter: Reinserting: "+
                              tag.getOpenTag()+" "+tag.getCloseTag());
        try {
          sgmlDoc.addElement (tag, true);
        } catch (IndexOutOfBoundsException e) {
          xTags.add (tag);
          System.err.println ("SGML tag "+tag.getOpenTag()+" out of bounds: "+e.getMessage());
          JOptionPane.showMessageDialog(null, "Dropping SGML tag " 
                                        + tag.getOpenTag() +
                                        " -- out of bounds: " + 
                                        e.getMessage());
        } catch (OverlappingElementException e) {
          xTags.add (tag);
          System.err.println ("SGML tag "+tag.getOpenTag()+" overlaps ends: "+e.getMessage());
          JOptionPane.showMessageDialog(null, "Dropping SGML tag " + 
                                        tag.getOpenTag() +
                                        " -- overlaps ends: " + 
                                        e.getMessage());
                                        
        }
      }
    }
    return sgmlDoc;
  }
  
  // SAM 1/16/05: Extracted this method in order to be able to specialize this
  // class for, e.g., MultiPhraseAnnotations.
  
  public void convertAnnotationToSgml(SgmlDocument sgmlDoc, AWBAnnotation annot) {
    
    AnnotationType type = annot.getAnnotationType ();
    
    if (annot instanceof TextExtentRegion) {
      int start           = ((TextExtentRegion)annot).getTextExtentStart ();
      int end             = ((TextExtentRegion)annot).getTextExtentEnd ();
      SgmlElement element =
        sgmlDoc.createContentTag(start, end, type.getName(), false);
      
      if (DEBUG > 3) System.err.println("convertAnnot2Sgml task =" +
                                        task.getName() + " type=" + 
                                        type.getName() + " annot= " +
                                        annot);
      for (Iterator i=task.getAttributes(type).iterator(); i.hasNext(); ) {
        // sgml retrieves case insensitively
        String attr = (String) i.next();
        if (DEBUG > 2) System.err.println("convertAnnot2Sgml: attr= " + attr);
        if (task.getAttributeType(type, attr).equals(String.class) &&
            ! attr.equals(TextExtentRegion.TEXT_EXTENT) ) {
          copyAttribute(annot, element, attr);
        } else {
          if (task.getAttributeType(type, attr).equals(Boolean.class)) {
            copyBooleanAttribute(annot, element, attr);
          }
        }
      }
      if (DEBUG > 1)
        System.err.println(" DefaultExporter: InlineTag  "+type.getName());
      
    } else {
      if (DEBUG > 1)
        System.err.println(" DefaultExporter: complexTag "+type.getName());
    }
  }
    
  /** Convience method */
  protected void copyAttribute (AWBAnnotation annot, SgmlElement element,
                              String attrName) {
    try {
      String attrValue = (String)annot.getAttributeValue (attrName);
      char quote = '"';
      if (attrValue.indexOf('"') >= 0) {
        if (attrValue.indexOf('\'') < 0)
          quote = '\'';
        else  // mixed quotes
          attrValue.replaceAll("\"","&quot;");
      }
      if (attrValue != null) {
        attrValue = "\"" + attrValue + "\"";
        element.putAttribute (attrName, attrValue);
      }
    } catch (Exception x) { // shouldn't ever happen
      System.err.println ("Couldn't copy attribute: "+
                          annot.getAnnotationType().getName()+
                          "."+attrName);
    }
  }

  protected void copyBooleanAttribute (AWBAnnotation annot, 
                                       SgmlElement element,
                                       String attrName) {
    try {
      Boolean attrValue = (Boolean)annot.getAttributeValue (attrName);
      String attrValString = "";
      if (attrValue != null) {
        attrValString = "\"" + attrValue.toString() + "\"";
        element.putAttribute (attrName, attrValString);
      }
    } catch (Exception x) { // shouldn't ever happen
      System.err.println ("Couldn't copy attribute: "+
                          annot.getAnnotationType().getName()+
                          "."+attrName);
    }
  }
  /*
  public static void main (String[] args) throws IOException {
    Exporter exporter = new DefaultInlineExporter(null,"MUC SGML");
    Importer importer = new DefaultInlineImporter(null,"MUC SGML");

    if (args.length < 2) {
      System.err.println ("\nUsage: ExportMUC <SGML-in> [SGML-out]\n"+
                          "       uses the ImportMUC to convert to aif, then right back");
      return;
    }
    File timeIn = new File (args[0]);
    File timeOut = new File (args[1]);
        
    AWBDocument doc = importer.importDocument (timeIn.toURI(), "UTF-8");
    boolean success = exporter.exportDocument (doc, timeOut.toURI());
    System.err.println ("\nSuccessfull = "+success);
  }
  */
}
