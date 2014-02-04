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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import gov.nist.atlas.type.AnnotationType;

import org.mitre.jawb.atlas.*;
import org.mitre.jawb.tasks.*;
import org.mitre.jawb.io.*;
import org.mitre.jawb.tasks.*;

/* This code opens a named input SGML file and transforms
 * it into a standard Atlas Interchange Format (AIF) output
 * file.
 * Author: David Day
 *
 * Edit History:
 * 2003/01/25 Began development.
 * 2003-06-17 Hacked by red for Tango.
 * 2003-08-07 Hacked by red to be a ImportTimeML
 */

public class ImportTIMEX2 implements Importer {

  public int DEBUG = 0;

  private boolean caseSensitive = true;
  private Task task;

  // caches for lazy population
  private HashMap tagNameMap = null;

  public ImportTIMEX2 (Task t) {
    task = t;
  }

  public String getFormat () {
    return "TIMEX2 SGML";
  }
  public String toString () {
    return getFormat ();
  }

  public String getDescription () {
    return "Accepts SGML, and maintains unrecognized tags in an SGML structure.";
  }
    
  public AWBDocument importDocument (URI uri, String encoding)
    throws IOException {

    // parse the input sgml into a new ParseSgml object
    if (DEBUG > 0)
      System.err.println("ImportTimex2: entering...\n  " + uri);

    File inputFile = new File(uri);
    Reader sgmlIn = new InputStreamReader (new FileInputStream (inputFile), encoding);
    SgmlDocument sgmlDoc = new SgmlDocument (new BufferedReader (sgmlIn));

    // convert sgml to ATLAS...
    
    // first, make a collection of all the tags we're interested in, and a new
    // SGML doc containing only those we don't (we have to do this first
    // because we have to have a task-tag-less signal to create the AIF)
    // TODO: UGGH this is crazy, we parse like 4 times by the time the user
    // finally see's it. fix th SGML stuff to allow 'remove' and just pass
    // AWBDocument the SGMLDocument
    SgmlDocument newSgmlDoc = new SgmlDocument(sgmlDoc.getSignalText());
    LinkedList relevant = new LinkedList();

    // build a list of tags to import. Force lower case when case insensitive.
    if (tagNameMap == null)
      initNameMaps();
 
    // find relevant task tags and save for next step. all else to newSgmlDoc
    for (Iterator i = sgmlDoc.iterator(); i.hasNext(); ) {
      SgmlElement element = (SgmlElement) i.next();
      String gidText = element.getOpenTag().getGid();
      if (DEBUG > 0)
        System.err.println(" element " + element.getOpenTag());
      
      if (! caseSensitive)
        gidText = gidText.toLowerCase();

      if (tagNameMap.containsKey(gidText)) {
        relevant.add(element);
      } else {
        newSgmlDoc.addElement(element, true);
      }
    }
    
    // save "new sgml" to file as signal for the AIF
    File newSgmlFile = new File(inputFile.toString()+".sgml");
    Writer exportWriter =
      new OutputStreamWriter (new FileOutputStream (newSgmlFile), encoding);
    exportWriter = new BufferedWriter (exportWriter);
    newSgmlDoc.writeSgml (exportWriter);
    exportWriter.close();
    
    // create an AWBDocument from slimmed down signal
    AWBDocument doc = AWBDocument.fromSignal (newSgmlFile.toURI(), task,
                                              "sgml", encoding);
    int i=0;    
    for (Iterator iter=relevant.iterator(); iter.hasNext(); ) {
      SgmlElement element  = (SgmlElement) iter.next();
      String gidText = element.getOpenTag().getGid();
      if (DEBUG > 0)
        System.err.println(" element (" + i + ") = " + element.getOpenTag());

      // note I'm comparing the entity name (from the sgml) to the Task tag
      // names only works because the mapping is 1:1
      if (gidText.equalsIgnoreCase(TIMEX2Task.TIMEX2_NAME)) {
        TextExtentRegion annot =
          (TextExtentRegion) doc.createAnnotation (TIMEX2Task.TIMEX2_NAME);
        copyAttribute (annot, element, "val");
        copyAttribute (annot, element, "mod");
        copyAttribute (annot, element, "set");
        copyAttribute (annot, element, "non-specific", "non_specific");
        copyAttribute (annot, element, "anchor-val", "anchor_val");
        copyAttribute (annot, element, "anchor-dir", "anchor_dir");
        copyAttribute (annot, element, "comment");

        annot.setTextExtents (element.getStart(),element.getEnd());

        if (DEBUG > 0)
          System.err.println(" TIMEX2:: "+gidText);
        
      }// else { // all else is ignored
    }

    if (DEBUG > 0)
      System.err.println("ImportTimex2: exiting...");

    return doc;
  }

  /** Convience method */
  private void copyAttribute (AWBAnnotation annot, SgmlElement element,
                              String attrib) {
    copyAttribute(annot, element, attrib, attrib);
  }

  private void copyAttribute (AWBAnnotation annot, SgmlElement element,
                              String attrib, String sgmlAttrib) {
    try {
      annot.setAttributeValue (attrib, element.getAttribute (sgmlAttrib));
    } catch (Exception x) { // shouldn't ever happen
      if (DEBUG > 0)
        System.err.println ("Couldn't copy attribute: "+
                            annot.getAnnotationType().getName()+"."+attrib);
    }
  }

  private void initNameMaps() {
    tagNameMap = new HashMap(); // map name to type for when case insensitive
    for (Iterator i = task.getAnnotationTypes().iterator(); i.hasNext(); ) {
      AnnotationType type = (AnnotationType)i.next();
      String tagName = type.getName();

      if (! caseSensitive)
        tagName = tagName.toLowerCase();
      
      tagNameMap.put (tagName, type);
    }
  }
}
