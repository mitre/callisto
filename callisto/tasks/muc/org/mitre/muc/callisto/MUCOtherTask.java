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

package org.mitre.muc.callisto;

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
import org.mitre.jawb.tasks.DefaultInlineImporter;
import org.mitre.jawb.tasks.DefaultInlineExporter;
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
public class MUCOtherTask extends AbstractTask {
  
  /** Constant for storing unknown sgml elements in the document */
  public static final String SPAM_KEY = "MUCOther::SpamElements";

  public static final String ENAMEX_NAME = "ENAMEX";
  public static final String TIMEX_NAME = "TIMEX";
  public static final String NUMEX_NAME = "NUMEX";
  public static final String OTHER_NAME = "OTHER";

  private static final String TASK_TITLE = "MUC+Other Task";
  private static final String TASK_NAME = "org.mitre.muc.other";
  private static final String TASK_VERSION = "1.0";
  private static final String TASK_DESCRIPTION =
    "MUC+Other - \"Message Understanding Conference\" Task defines a simple annotation task with ENAMEX, TIMEX, NUMEX and OTHER tags.";
  private static final String MAIA_SCHEME =
    "http://callisto.mitre.org/maia/MUC.other.maia.xml";
  private static final String LOCAL_MAIA_SCHEME = "/resource/MUC.other.maia.xml";

  /* private mapping between annotation type name and Class */
  private Map annotationClass = null;

  private Set highlights = null;
  private Map defaultPrefs = null;
  
  private Importer[] importers = null;
  private Exporter[] exporters = null;

  private PreferenceItem preferences = null;

  public MUCOtherTask () {
    super (TASK_TITLE, TASK_NAME, TASK_VERSION, TASK_DESCRIPTION,
           MAIA_SCHEME, LOCAL_MAIA_SCHEME);
    
    // other initializations here as needed
    initializeClassMap();
    initValueSets();
    initHighlightKeys ();
    initDefaultPrefs ();
    initIO ();
  }
  /** Returns a new instance of the toolkit each time */
  public TaskToolKit getToolKit () {
    return new MUCOtherToolKit (this);
  }

  public Importer[] getImporters () {
    return importers;
  }
    
  public Exporter[] getExporters () {
    return exporters;
  }

  /** A preference! */
  public PreferenceItem getPreferenceItem () {
    if (preferences == null)
      preferences = new MUCPrefs(this);
    return preferences;
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
    if (annotType.getName().equals(ENAMEX_NAME)) {
      if (attr.equals("type"))
        return enamexTypeValues;
    }
    if (annotType.getName().equals(TIMEX_NAME)) {
      if (attr.equals("type"))
        return timexTypeValues;
    }
    if (annotType.getName().equals(NUMEX_NAME)) {
      if (attr.equals("type"))
        return numexTypeValues;
    }
    return null;
  }

  public String getDefaultValue (AnnotationType annotType, String attr) {
    if (annotType.getName().equals(ENAMEX_NAME)) {
      if (attr.equals("type"))
        return (String) enamexTypeValues.iterator().next();
    }
    if (annotType.getName().equals(TIMEX_NAME)) {
      if (attr.equals("type"))
        return (String) timexTypeValues.iterator().next();
    }
    if (annotType.getName().equals(NUMEX_NAME)) {
      if (attr.equals("type"))
        return (String) numexTypeValues.iterator().next();
    }
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
    // very simple, since in MUC, you can just as easily key on type: we do.
    String type = (String) annot.getAttributeValue ("type");
    if (type == null || type.equals (""))
      return OTHER_NAME;
    return type;
  }

  /***********************************************************************/
  /* Init methods */
  /***********************************************************************/

  private void initializeClassMap() {
    annotationClass = new HashMap();
    annotationClass.put(getAnnotationType (ENAMEX_NAME),
                        PhraseTaggingAnnotation.class);
    annotationClass.put(getAnnotationType (TIMEX_NAME),
                        PhraseTaggingAnnotation.class);
    annotationClass.put(getAnnotationType (NUMEX_NAME),
                        PhraseTaggingAnnotation.class);
    annotationClass.put(getAnnotationType (OTHER_NAME),
                        PhraseTaggingAnnotation.class);
  }

  /** used several times, so I made them constants */
  private static final String[] STYLE_KEYS = {"PERSON",
                                              "ORGANIZATION",
                                              "LOCATION",
                                              "DATE",
                                              "TIME",
                                              "MONEY",
                                              "PERCENT",
                                              "OTHER"};
  private static final String[] COLORS = {"{#0033cc,#ffffff}",
                                          "{#0099ff,#ffffff}",
                                          "{#66ffff,#000000}",
                                          "{#c15858,#ffffff}",
                                          "{#ffa3a3,#000000}",
                                          "{#68ab76,#ffffff}",
                                          "{#99ff99,#000000}",
                                          "{#ccff33,#000000}"};

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
    importers = new Importer[] {new DefaultInlineImporter (this, "MUC+Other SGML")};
    exporters = new Exporter[] {new DefaultInlineExporter (this, "MUC+Other SGML")};
  }
  
  private Set enamexTypeValues;
  private Set timexTypeValues;
  private Set numexTypeValues;

  private void initValueSets () {
    enamexTypeValues = new LinkedHashSet (Arrays.asList (new String[]
      {"PERSON","ORGANIZATION","LOCATION"}));
    timexTypeValues = new LinkedHashSet (Arrays.asList (new String[]
      {"DATE", "TIME"}));
    numexTypeValues = new LinkedHashSet (Arrays.asList (new String[]
      {"MONEY","PERCENT"}));
  }
}
