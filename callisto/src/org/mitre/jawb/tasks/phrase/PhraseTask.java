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


package org.mitre.jawb.tasks.phrase;

import java.awt.Component;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.*;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.AWBATLASImplementation;
import org.mitre.jawb.atlas.PhraseTaggingAnnotation;
import org.mitre.jawb.tasks.AbstractTask;
import org.mitre.jawb.tasks.TaskToolKit;
import org.mitre.jawb.prefs.PreferenceItem;

import gov.nist.atlas.type.AnnotationType;
import gov.nist.atlas.type.ATLASType;
import gov.nist.atlas.type.CorpusType;
import gov.nist.maia.MAIAScheme;
import gov.nist.maia.MAIALoader;

import org.mitre.jawb.Jawb;

/* 
 * This is the place for any necessary hard-coded information about
 * the task to reside. This class is NOT intended to be extended.
 */
public class PhraseTask extends AbstractTask {

  private static final String TASK_TITLE = "Phrase";
  private static final String TASK_NAME = "org.mitre.phrase";
  private static final String TASK_VERSION = "1.0";
  private static final String TASK_DESCRIPTION =
    "Phrase Tagging for simple tasks and annotation testing.";
  /** Cononical URL for the 'phrase' maia */
  private static final String PHRASE_MAIA_SCHEME =
    "http://callisto.mitre.org/maia/GPEPhraseTagging.maia.xml";
  /** file in this class' jar file, full path since it's not in same dir */
  private static final String LOCAL_MAIA_SCHEME =
    "/org/mitre/jawb/resources/maia/GPEPhraseTagging.maia.xml";

  private Set highlights = null;
  private Map defaultPrefs = null;
    
  /* private mapping between annotation type name and Class */
  private Map annotationClass = null;
  private Set annotationTypes = null;

  public PhraseTask () {
    super (TASK_TITLE, TASK_NAME, TASK_VERSION, TASK_DESCRIPTION,
           PHRASE_MAIA_SCHEME, LOCAL_MAIA_SCHEME);

    initializeClassMap();

    // GUI stuff, some of which needs the above to be done first
    initHighlightKeys ();
    initDefaultPrefs ();
  }

  /** Retrieve a new Toolkit for this task. */
  public TaskToolKit getToolKit () {
    return new PhraseToolKit (this);
  }

  /**
   * Default colors.
   */
  public Map getDefaultPreferences () {
    return defaultPrefs;
  }

  /* one with an object, one without for always true possibilities */
  public Set getPossibleValues (AWBAnnotation annot, String type) {
    return null;
  }
  public Set getPossibleValues (AnnotationType annot, String type) {
    return null;
  }

  /**
   * Correspondence between AnnotationTypes and the Class of AWBAnnotation
   * used to implement them
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
    if (annot.getAnnotationType().getName().equals ("phrase-tag"))
      return (String)annot.getAttributeValue("type");
    
    return null;
  }

  /***********************************************************************/
  /* Init methods */
  /***********************************************************************/

  private void initializeClassMap() {
    annotationClass = new HashMap();
    annotationClass.put(getAnnotationType ("phrase-tag"),
                        PhraseTaggingAnnotation.class);
  }

  /** used several times, so I made them constants */
  private static final String[] STYLE_KEYS = {"phrase-tag.mention-head",
                                              "phrase-tag.mention-extent"};
  private static final String[] COLORS = {"#8ff7ff","#8bc6ff"};

  private void initHighlightKeys () {
    highlights = new HashSet ();
    for (int i=0; i<STYLE_KEYS.length; i++)
      highlights.add (STYLE_KEYS[i]);
    
    highlights = Collections.unmodifiableSet (highlights);
  }

  private void initDefaultPrefs () {
    defaultPrefs = new HashMap ();
    for (int i=0; i<STYLE_KEYS.length; i++)
      defaultPrefs.put ("task."+TASK_NAME+"."+STYLE_KEYS[i], COLORS[i]);
    
    defaultPrefs = Collections.unmodifiableMap (defaultPrefs);
  }
}
