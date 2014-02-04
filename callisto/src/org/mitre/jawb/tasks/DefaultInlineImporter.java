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

package org.mitre.jawb.tasks;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Set;

import gov.nist.atlas.type.AnnotationType;

import org.mitre.jawb.atlas.*;
import org.mitre.jawb.tasks.*;
import org.mitre.jawb.io.*;
import org.mitre.jawb.tasks.*;


/*
 * This code opens a named input SGML file and transforms
 * it into a standard Atlas Interchange Format (AIF) output
 * file.
 */
public class DefaultInlineImporter implements Importer {

  public int DEBUG = 0;

  protected boolean caseSensitive = true;
  protected Task task;
  private String format;
  private String descr = "Accepts SGML, and moves tags matching the Task's annotation types into an aif document. Matching attributes of these tags are also imported. Tags not defined in the AIF as simple spans are not imported. Remaining tags are saved for future export as usual.";

  // caches for lazy population
  protected HashMap tagNameMap = null;

  public DefaultInlineImporter (Task task, String format) {
    this.task = task;
    this.format = format;
  }

  public String getFormat () {
    return format;
  }
  public String toString () {
    return getFormat ();
  }

  public Task getTask () {
    return task;
  }

  /** Set the description (else a default is used). */
  public void setDescription(String descr) {
    this.descr = descr;
  }

  public String getDescription () {
    return "Accepts SGML, and maintains unrecognized tags in an SGML structure.";
  }

  /** Set whether importing tags/attributes is case sensitive (default=true). */
  public void setCaseSensitive(boolean cs) {
    caseSensitive = cs;
  }
    
  public boolean isCaseSensitive() {
    return caseSensitive;
  }

  public AWBDocument importDocument (URI uri, String encoding)
    throws IOException {

    // parse the input sgml into a new ParseSgml object
    if (DEBUG > 0)
      System.err.println("DefaultImporter: entering...\n  " + uri);

    File inputFile = new File(uri);
    Reader sgmlIn =
      new InputStreamReader (new FileInputStream (inputFile), encoding);

    return importDocument(sgmlIn, inputFile.toString(), encoding);
  }

  public AWBDocument importDocument (Reader sgmlIn, String filename, String encoding)
    throws IOException {

    SgmlDocument sgmlDoc = new SgmlDocument (new BufferedReader (sgmlIn));

    // convert sgml to ATLAS...
    
    // first, make a collection of all the tags we're interested in, and a new
    // SGML doc containing only those we don't (we have to do this first
    // because we have to have a task-tag-less signal to create the AIF)
    // TODO: UGGH this is crazy, we parse like 4 times by the time the user
    // finally see's it. fix th SGML stuff to allow 'remove' and just pass
    // AWBDocument the SGMLDocument
    SgmlDocument spamSgmlDoc = new SgmlDocument(sgmlDoc.getSignalText());
    LinkedList relevant = new LinkedList();

    // build a list of tags to import. Force lower case when case insensitive.
    if (tagNameMap == null)
      initNameMaps();

    // find relevant task tags and save for next step. all else to spamSgmlDoc
    for (Iterator i = sgmlDoc.iterator(); i.hasNext(); ) {
      SgmlElement element = (SgmlElement) i.next();
      String gidText = element.getOpenTag().getGid();
      if (DEBUG > 0)
        System.err.println("DefaultInlineImporter: element " + 
			   element.getOpenTag());
      
      if (! caseSensitive)
        gidText = gidText.toLowerCase();

      if (tagNameMap.containsKey(gidText)) {
        relevant.add(element);
	if (DEBUG > 1)
	  System.err.println("\tis relevant");
      } else {
        spamSgmlDoc.addElement(element, true);
	if (DEBUG > 1)
	  System.err.println("\tis spam");
      }
    }
    
    // save "spam sgml" to file as signal for the AIF
    File spamSgmlFile = new File(filename + ".sgml");
    Writer exportWriter =
      new OutputStreamWriter (new FileOutputStream (spamSgmlFile), encoding);
    exportWriter = new BufferedWriter (exportWriter);
    spamSgmlDoc.writeSgml (exportWriter);
    exportWriter.close();
    
    // create an AWBDocument from slimmed down signal
    AWBDocument doc = AWBDocument.fromSignal (spamSgmlFile.toURI(), task,
                                              "sgml", encoding);
    // add the relevant tags as ATLAS tags
    for(Iterator i=relevant.iterator(); i.hasNext(); ) {
      SgmlElement element = (SgmlElement) i.next();
      convertSgmlToAnnotation(doc, element);
    }
    
    if (DEBUG > 1)
      System.err.println("DefaultImporter: exiting...");
    
    return doc;
  }
  
  // SAM 1/16/06: Extracted this  method in order to specialize this class.
  
  public void convertSgmlToAnnotation(AWBDocument doc, SgmlElement element) {
    String gidText = element.getOpenTag().getGid();
    if (! caseSensitive)
      gidText = gidText.toLowerCase();
    
    AnnotationType type = (AnnotationType) tagNameMap.get(gidText);
    // drop anything that's not a TextExtentRegion
    Class simpleText = TextExtentRegion.class;
    if (! simpleText.isAssignableFrom(task.getAnnotationClass(type))) {
      if (DEBUG > 1)
        System.err.println(" DefaultImporter: ComplexTag "+gidText);
      return;
    }
    
    TextExtentRegion annot = (TextExtentRegion) doc.createAnnotation(type);
    
    copyAttributes (task, annot, type, element);
    
    annot.setTextExtents (element.getStart(),element.getEnd());
    if (DEBUG > 1)
      System.err.println(" DefaultImporter: InlineTag  "+gidText);
  }

  protected void copyAttributes (Task task, AWBAnnotation annot, 
				 AnnotationType type, SgmlElement element) {
    for (Iterator j=task.getAttributes(type).iterator(); j.hasNext(); ) {
      // sgml retrieves case insensitively
      String attr = (String) j.next();
      Class attrType = task.getAttributeType(type, attr);
      if (attrType.equals(String.class) &&
	  ! attr.equals(TextExtentRegion.TEXT_EXTENT) ) {
	copyAttribute(annot, element, attr);
      } else if (attrType.equals(Boolean.class)) {
        copyBooleanAttribute(annot, element, attr);
      }
    }
  }


  /** Convience method */
  protected void copyAttribute (AWBAnnotation annot, SgmlElement element,
                              String attrib) {
    try {
      annot.setAttributeValue (attrib, element.getAttribute (attrib));
    } catch (Exception x) { /* ignore other immutables.*/ }
  }

  protected void copyBooleanAttribute (AWBAnnotation annot, 
                                       SgmlElement element,
                                       String attrib) {
    String value = element.getAttribute(attrib);
    try {
      if (value.equalsIgnoreCase("true") ||
          value.equalsIgnoreCase("t") ||
          value.equalsIgnoreCase("yes") ||
          value.equalsIgnoreCase("y")) {
        annot.setAttributeValue (attrib, Boolean.TRUE);
      } else {
        annot.setAttributeValue (attrib, Boolean.FALSE);
      }
    } catch (Exception x) { /* ignore other immutables.*/ }
  }

  protected void initNameMaps() {
    tagNameMap = new HashMap(); // map name to type for when case insensitive
    for (Iterator i = task.getAnnotationTypes().iterator(); i.hasNext(); ) {
      AnnotationType type = (AnnotationType)i.next();
      String tagName = type.getName();

      if (! caseSensitive)
        tagName = tagName.toLowerCase();
      if (DEBUG > 0)
        System.err.println("DefaultInlineImporter.initNameMaps: " + tagName);
      
      tagNameMap.put (tagName, type);
    }
  }
}
