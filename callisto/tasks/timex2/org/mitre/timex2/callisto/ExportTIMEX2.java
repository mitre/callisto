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

package org.mitre.timex2.callisto;

import java.io.*;
import java.net.URI;
import java.util.*;

import gov.nist.atlas.*;

import org.mitre.jawb.atlas.*;
import org.mitre.jawb.io.*;
import org.mitre.jawb.tasks.*;

public class ExportTIMEX2 implements Exporter {

  /** Constant for storing document in a map */
  public static final String SGML_DOC_KEY = "Sgml::ParsedDocument";
  
  public static final int DEBUG = 0;

  public ExportTIMEX2 () {}

  public String getFormat () {
    return "TIMEX2 SGML";
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
    SgmlDocument sgmlDoc = convertAtlasToTIMEX2 (doc);
    String encoding = doc.getEncoding();
            
    Writer exportWriter = new OutputStreamWriter
      (new FileOutputStream (new File(uri)), encoding);
    exportWriter = new BufferedWriter (exportWriter);

    sgmlDoc.writeSgml (exportWriter);
    exportWriter.close();

    return true;
  }

  private SgmlDocument convertAtlasToTIMEX2 (AWBDocument doc) {
    // retrieve the original sgml document or create a new one
    SgmlDocument sgmlDoc = new SgmlDocument (doc.getSignal().getCharsAt(0));

    // copy over annotations from the AWBDocument
    Iterator annotIter = doc.getAllAnnotations();
    while (annotIter.hasNext()) {
      AWBAnnotation annot = (AWBAnnotation)annotIter.next();
      String typeName = annot.getAnnotationType ().getName();
          
      if (typeName.equals(TIMEX2Task.TIMEX2_NAME)) {

        int start           = ((TextExtentRegion)annot).getTextExtentStart ();
        int end             = ((TextExtentRegion)annot).getTextExtentEnd ();
        SgmlElement element = sgmlDoc.createContentTag (start, end, typeName, false);
            
        copyAttribute (annot, element, "val");
        copyAttribute (annot, element, "mod");
        copyAttribute (annot, element, "set");
        copyAttribute (annot, element, "non-specific", "non_specific");
        copyAttribute (annot, element, "anchor-val", "anchor_val");
        copyAttribute (annot, element, "anchor-dir", "anchor_dir");
        copyAttribute (annot, element, "comment");
        if (DEBUG > 0)
          System.err.println(" TIMEX2:: "+typeName);

      } else { // all else is meta
        if (DEBUG > 0)
          System.err.println(" TIMEX2:: META text = " +typeName);
      }
    }

    // now try to add the spam back in, dropping them if they cause
    // problems
    List sgmlList = (List)doc.getClientProperty (AWBDocument.SGML_TAG_LIST_KEY);
    if (sgmlList != null) {
      //try {sgmlDoc.writeSgml (new PrintWriter(System.err));}catch(Exception e){};
      for (Iterator iter = sgmlList.iterator (); iter.hasNext(); ) {
        SgmlElement tag = (SgmlElement)iter.next();
        //System.err.println ("Spam: "+tag.getOpenTag());
        try {
          sgmlDoc.addElement (tag, true);
        } catch (IndexOutOfBoundsException e) {
          System.err.println (" Tango: SGML tag found on import no longer fits!\n"+e);
        } catch (OverlappingElementException e) {
          System.err.println (" Tango: SGML tag found on import now overlaps existing tag!\n"+e);
        }
      }
    }
    return sgmlDoc;
  }
    
  /** Convience method */
  private void copyAttribute (AWBAnnotation annot, SgmlElement element,
                              String attrName) {
    copyAttribute(annot, element, attrName, attrName);
  }

  private void copyAttribute (AWBAnnotation annot, SgmlElement element,
                              String attrName, String attrOutputName) {
    try {
      String attrValue = (String)annot.getAttributeValue (attrName);
      Object eventID = annot.getAttributeValue ("eventID");
      if (attrValue != null) {
        attrValue = "\"" + attrValue + "\"";
        element.putAttribute (attrOutputName, attrValue);
      }
    } catch (Exception x) { // shouldn't ever happen
      System.err.println ("Couldn't copy attribute: "+
                          annot.getAnnotationType().getName()+
                          "."+attrName);
    }
  }
    
  public static void main (String[] args) throws IOException {
    Exporter exporter = new ExportTIMEX2();
    Importer importer = new ImportTIMEX2(new TIMEX2Task ());

    if (args.length < 2) {
      System.err.println ("\nUsage: ExportTimeML <timeML-in> <time-ML-out>\n"+
                          "       uses the ImportTimeML to convert to atlas, then right back");
      return;
    }
    File timeIn = new File (args[0]);
    File timeOut = new File (args[1]);
        
    AWBDocument doc = importer.importDocument (timeIn.toURI(), "UTF-8");
    boolean success = exporter.exportDocument (doc, timeOut.toURI());
    System.err.println ("\nSuccessfull = "+success);
  }
}
