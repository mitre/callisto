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

import gov.nist.atlas.type.AnnotationType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.AWBDocument;
import org.mitre.jawb.atlas.TextExtentRegion;
import org.mitre.jawb.io.SgmlDocument;
import org.mitre.jawb.io.SgmlElement;
import org.mitre.jawb.tasks.Importer;
import org.mitre.jawb.tasks.Task;

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

public class ImportSpatialML implements Importer {

  public int DEBUG = 0;

  private boolean caseSensitive = false; // eh?
  private Task task;

  // caches for lazy population
  private HashMap tagNameMap = null;

  public ImportSpatialML (Task t) {
    task = t;
  }

  public String getFormat () {
    return "SpatialML";
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
      System.err.println("ImportSpatialML: entering...\n  " + uri);

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
    
    Map placeMap = new HashMap(); // map from 'id' to 'place' annotation
    Map signalMap = new HashMap(); // map from 'id' to 'signal' annotation
    Map rlinkMap = new HashMap(); // map from 'id' to 'rlink' annotation
    
    List rlinksForLater = new Vector(); // list of places to post-process
    List rlinkElementsForLater = new Vector();
    List linksForLater = new Vector(); // list of links to post-process
    List linkElementsForLater = new Vector();
    
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
    //int i=0;    
    for (Iterator iter=relevant.iterator(); iter.hasNext(); ) {
      SgmlElement element  = (SgmlElement) iter.next();
      String gidText = element.getOpenTag().getGid();
      if (DEBUG > 0)
        System.err.println(" element = " + element.getOpenTag());

      // note I'm comparing the entity name (from the sgml) to the Task tag
      // names only works because the mapping is 1:1
      if (gidText.equalsIgnoreCase(SpatialMLTask.PLACE_NAME)) {
        TextExtentRegion annot =
          (TextExtentRegion) doc.createAnnotation (gidText.toUpperCase()); // all SpatialML tags are uppercased

        annot.setTextExtents (element.getStart(), element.getEnd());

        if (DEBUG > 0)
          System.err.println(" SpatialML:: "+gidText);

        Iterator it = element.attributeIterator();
        while (it.hasNext()) {
          String attr = (String)it.next();
          if (DEBUG > 2)
            System.err.println("  (attribute)::" + attr);
          
          if (attr.equals("id") && element.getAttribute(attr) != null) {
            placeMap.put(element.getAttribute(attr), annot);
          }
          
          copyAttribute(annot, element, attr);
        }

      } else if (gidText.equalsIgnoreCase(SpatialMLTask.SIGNAL_NAME)) {
        TextExtentRegion annot =
          (TextExtentRegion) doc.createAnnotation (gidText.toUpperCase()); // all SpatialML tags are uppercased

        annot.setTextExtents (element.getStart(), element.getEnd());

        if (DEBUG > 0)
          System.err.println(" SpatialML:: "+gidText);

        Iterator it = element.attributeIterator();
        while (it.hasNext()) {
          String attr = (String)it.next();
          if (DEBUG > 2)
            System.err.println("  (attribute)::" + attr);

          if (attr.equals("id") && element.getAttribute(attr) != null) {
            signalMap.put(element.getAttribute(attr), annot);
          }

          copyAttribute(annot, element, attr);
        }

      } else if (gidText.equalsIgnoreCase(SpatialMLTask.RLINK_NAME)) {
        
        AWBAnnotation annot = doc.createAnnotation(SpatialMLTask.RLINK_NAME);
        
        //TextExtentRegion extent = (TextExtentRegion) doc.createAnnotation(SpatialMLTask.PATH_EXTENT_NAME);

        //extent.setTextExtents(element.getStart(), element.getEnd());
        
        //annot.getRegion().setAnnotationWithRole(extent, "path-extent");
        
        if (DEBUG > 0)
          System.err.println(" SpatialML:: "+gidText);

        Iterator it = element.attributeIterator();
        while (it.hasNext()) {
          String attr = (String)it.next();

          if (DEBUG > 2)
            System.err.println("  (attribute)::" + attr);
          
          if (!attr.equals("source") && !attr.equals("target") && !attr.equals("signals")) {
            if (attr.equals("id") && element.getAttribute(attr) != null) {
              rlinkMap.put(element.getAttribute(attr), annot); // save for later, by id
            }
            copyAttribute(annot, element, attr);
          }

        }
        
        // save the bits for post-processing, once we've actually filled in the
        // rest of the document. this way we can properly handle subordinates
        
        rlinksForLater.add(annot);
        rlinkElementsForLater.add(element);
        
      } else if (gidText.equalsIgnoreCase(SpatialMLTask.LINK_NAME)) {
        
        AWBAnnotation annot = doc.createAnnotation(SpatialMLTask.LINK_NAME);
        
        if (DEBUG > 0)
          System.err.println(" SpatialML:: "+gidText);

        Iterator it = element.attributeIterator();
        while (it.hasNext()) {
          String attr = (String)it.next();

          if (DEBUG > 2)
            System.err.println("  (attribute)::" + attr);
          
          if (!attr.equals("source") && !attr.equals("target")) {
            copyAttribute(annot, element, attr);
          }

        }
        
        // save the bits for post-processing, once we've actually filled in the
        // rest of the document. this way we can properly handle subordinates
        
        linksForLater.add(annot);
        linkElementsForLater.add(element);
        
        
      }
      
      
      // else { // all else is ignored
    }

    // Post-process all paths and links to fill in the subordinates
    
    if (DEBUG > 0)
      System.err.println("Post-processing " + rlinksForLater.size() + " rlinks and " + linksForLater + " links...");
    
    Iterator annotIt = rlinksForLater.iterator();
    Iterator elemIt = rlinkElementsForLater.iterator();
    
    while (annotIt.hasNext() && elemIt.hasNext()) {
      AWBAnnotation annot = (AWBAnnotation)annotIt.next();
      SgmlElement element = (SgmlElement)elemIt.next();
      
      if (DEBUG > 0)
        System.err.println(" annot = " + annot);
      
      String signalsAttr = element.getAttribute("signals");
      if (signalsAttr != null && !signalsAttr.trim().equals("")) {
        String[] signalIds = signalsAttr.split(" "); // signals is a space-delimited set of IDs
        for (int i = 0; i < signalIds.length; i++) {
          AWBAnnotation signal = (AWBAnnotation) signalMap.get(signalIds[i]);
          if (signal != null)
            annot.getRegion().addToSubordinateSet(signal);
        }
      }
      
      String sourceAttr = element.getAttribute("source");
      if (sourceAttr != null && !sourceAttr.trim().equals("")) {
        AWBAnnotation source = (AWBAnnotation)placeMap.get(sourceAttr);
        if (source != null) 
          annot.getRegion().setSubordinateWithRole(source, "source");
      }
      
      String targetAttr = element.getAttribute("target");
      if (targetAttr != null && !targetAttr.trim().equals("")) {
        AWBAnnotation target = (AWBAnnotation)placeMap.get(targetAttr);
        if (target != null) 
          annot.getRegion().setSubordinateWithRole(target, "target");
      }
      
    }
    
    
    // Post-process links
    
    annotIt = linksForLater.iterator();
    elemIt = linkElementsForLater.iterator();
    
    while (annotIt.hasNext() && elemIt.hasNext()) {
      AWBAnnotation annot = (AWBAnnotation)annotIt.next();
      SgmlElement element = (SgmlElement)elemIt.next();
      
      if (DEBUG > 0)
        System.err.println(" annot = " + annot);
      
      String signalsAttr = element.getAttribute("signals");
      if (signalsAttr != null && !signalsAttr.trim().equals("")) {
        String[] signalIds = signalsAttr.split(" "); // signals is a space-delimited set of IDs
        for (int i = 0; i < signalIds.length; i++) {
          AWBAnnotation signal = (AWBAnnotation) signalMap.get(signalIds[i]);
          if (signal != null)
            annot.getRegion().addToSubordinateSet(signal);
        }
      }
      
      String sourceAttr = element.getAttribute("source");
      if (sourceAttr != null && !sourceAttr.trim().equals("")) {
        AWBAnnotation source = (AWBAnnotation)placeMap.get(sourceAttr);
        if (source != null) {
          annot.getRegion().setSubordinateWithRole(source, "source");
        } else {
          source = (AWBAnnotation)rlinkMap.get(sourceAttr);
          if (source != null) {
            annot.getRegion().setSubordinateWithRole(source, "sourceRlink");
          }
        }
      }
      
      String targetAttr = element.getAttribute("target");
      if (targetAttr != null && !targetAttr.trim().equals("")) {
        AWBAnnotation target = (AWBAnnotation)placeMap.get(targetAttr);
        if (target != null) {
          annot.getRegion().setSubordinateWithRole(target, "target");
        } else {
          target = (AWBAnnotation)rlinkMap.get(targetAttr);
          if (target != null) {
            annot.getRegion().setSubordinateWithRole(target, "targetRlink");
          }
        }
      }
      
    }
    
    if (DEBUG > 0)
      System.err.println("ImportSpatialML: exiting...");

    return doc;
  }

  /** Convience method */
  private void copyAttribute (AWBAnnotation annot, SgmlElement element, String attrib) {
    copyAttribute(annot, element, attrib, attrib);
  }

  private void copyAttribute (AWBAnnotation annot, SgmlElement element,
      String attrib, String sgmlAttrib) {
    try {
      String val = element.getAttribute(sgmlAttrib);
      if (Boolean.valueOf(val).booleanValue()) {
        val = "YES";
      }
      annot.setAttributeValue (attrib, val);
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
    tagNameMap.put(SpatialMLTask.ROOT_NAME, null);
  }
}
