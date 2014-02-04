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

import java.awt.Component;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import javax.swing.*;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.AWBATLASImplementation;
import org.mitre.jawb.atlas.PhraseTaggingAnnotation;
import org.mitre.jawb.tasks.AbstractTask;
import org.mitre.jawb.tasks.Importer;
import org.mitre.jawb.tasks.Exporter;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;
import org.mitre.jawb.tasks.TaskToolKit;
import org.mitre.jawb.prefs.PreferenceItem;

import gov.nist.atlas.type.AnnotationType;
import gov.nist.atlas.type.ATLASType;
import gov.nist.atlas.type.CorpusType;
import gov.nist.maia.MAIAScheme;
import gov.nist.maia.MAIALoader;

/**
 * This is the place for any necessary hard-coded information about the task
 * to reside.
 */
public class TIMEX2Task extends AbstractTask {

  private static final int DEBUG = 0;
  
  /** Constant for storing unknown sgml elements in the document */
  public static final String SPAM_KEY = "TIMEX2::SpamElements";

  public static final String TIMEX2_NAME = "TIMEX2";

  private static final String TASK_TITLE = "TIMEX2 Task";
  private static final String TASK_NAME = "org.mitre.timex2";
  private static final String TASK_VERSION = "1.2";
  private static final String TASK_DESCRIPTION =
    "TIMEX2 - \"Timex2\" Task defines a simple annotation task for temporal expressions.";
  private static final String MAIA_SCHEME =
  "http://callisto.mitre.org/maia/timex2.maia.xml";
  private static final String LOCAL_MAIA_SCHEME = "/resource/timex2.maia.xml";

  /* private mapping between annotation type name and Class */
  private Map annotationClass = null;

  private Set highlights = null;
  private Map defaultPrefs = null;
  
  private Importer[] importers = null;
  private Exporter[] exporters = null;

  public TIMEX2Task () {
    super (TASK_TITLE, TASK_NAME, TASK_VERSION, TASK_DESCRIPTION,
           MAIA_SCHEME, LOCAL_MAIA_SCHEME);
    
    // other initializations here as needed
    initializeClassMap();
    initHighlightKeys ();
    initDefaultPrefs ();
    initIO ();
  }
  /** Returns a new instance of the toolkit each time */
  public TaskToolKit getToolKit () {
    return new TIMEX2ToolKit (this);
  }

  public Importer[] getImporters () {
    return importers;
  }
    
  public Exporter[] getExporters () {
    return exporters;
  }

  /**
   * Default colors.
   */
  public Map getDefaultPreferences () {
    return defaultPrefs;
  }

  /* one with an object, one without for always true possibilities */
  public Set getPossibleValues (AWBAnnotation annot, String attr) {
    AnnotationType annotType = annot.getAnnotationType();
    return getPossibleValues(annotType, attr);
  }
  public Set getPossibleValues (AnnotationType annotType, String attr) {
    return null;
  }

  public String getDefaultValue (AnnotationType annotType, String attr) {
    return null;
  }

  /**
   *  Correspondence between AnnotationTypes and the Class of
   *  AWBAnnotation used to implement them
   */
  public Class getAnnotationClass(ATLASType type) {
    return (Class)annotationClass.get(type);
  }

  public Set getHighlightKeys () {
    return highlights;
  }

  /**
   * No 'constraint' is expected for simple TextExtentRegions
   */
  public String getHighlightKey (AWBAnnotation annot, Object constraint) {
    // if any arg has a non-null value, return "timex2", otherwise
    // return "timex2.incomplete"
    String [] attrKeys = annot.getAttributeKeys();
    for (int i=0; i<attrKeys.length; i++) {
      if (!attrKeys[i].startsWith("TextExtent")) {

	String val = (String) annot.getAttributeValue(attrKeys[i]);
	if (DEBUG > 2) 
	  System.err.println("Check attribute: " + attrKeys[i] +
			     " = " + (val==null?"<null>":val));

	if ((val != null) && !val.equals("") && !val.equals("null")) {

	  if (DEBUG > 1) 
	    System.err.println("found " + attrKeys[i] + " set to " + val);

	  //Thread.dumpStack();
	  return "timex2";
	}
      }
    }
    //Thread.dumpStack();
    return "timex2.incomplete";
  }

  /***********************************************************************/
  /* Init methods */
  /***********************************************************************/

  private void initializeClassMap() {
    annotationClass = new HashMap();
    annotationClass.put(getAnnotationType (TIMEX2_NAME),
                        PhraseTaggingAnnotation.class);
  }

  /** used several times, so I made them constants */
  private static final String[] STYLE_KEYS = {"timex2","timex2.incomplete"};
  private static final String[] COLORS = {"{#8612a0,#ffffff}",
					  "{#ef8d3d,#000000}"};

  private void initHighlightKeys () {
    highlights = new HashSet ();
    for (int i=0; i<STYLE_KEYS.length; i++)
      highlights.add (STYLE_KEYS[i]);
    
    highlights = Collections.unmodifiableSet (highlights);
  }

  private void initDefaultPrefs () {
    defaultPrefs = new LinkedHashMap ();
    for (int i=0; i<STYLE_KEYS.length; i++)
      defaultPrefs.put ("task."+TASK_NAME+"."+STYLE_KEYS[i], COLORS[i]);
    
    defaultPrefs = Collections.unmodifiableMap (defaultPrefs);
  }

  private void initIO () {
    importers = new Importer[] {new ImportTIMEX2 (this),
				new ImportTIMEX2NoAttrVals (this)};
    exporters = new Exporter[] {new ExportTIMEX2 ()};
  }
  
}
