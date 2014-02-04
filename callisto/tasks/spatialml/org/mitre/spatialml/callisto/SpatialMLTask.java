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

import gov.nist.atlas.ATLASElement;
import gov.nist.atlas.Annotation;
import gov.nist.atlas.Region;
import gov.nist.atlas.type.AnnotationType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.HasSubordinates;
import org.mitre.jawb.tasks.AbstractTask;
import org.mitre.jawb.tasks.Exporter;
import org.mitre.jawb.tasks.Importer;
import org.mitre.jawb.tasks.TaskToolKit;

/**
 * This is the place for any necessary hard-coded information about the task
 * to reside.
 */
public class SpatialMLTask extends AbstractTask {

  public static final String TASK_VERSION = "0.10.2";
  public static final String TASK_TITLE = "SpatialML Task";
  public static final String TASK_NAME = "org.mitre.spatialml";
  public static final String TASK_DESCRIPTION = "SpatialML - defines a simple annotation task for geospatial expressions.";
  
  public static final String SPATIALML_VERSION = "3.0";
  
  private static final String MAIA_SCHEME = "http://callisto.mitre.org/maia/spatialml.maia.xml";
  private static final String LOCAL_MAIA_SCHEME = "/resource/spatialml.maia.xml";

  private static final int DEBUG = 0;

  /** Constant for storing unknown sgml elements in the document */
  public static final String SPAM_KEY = "SpatialML::SpamElements";

  public static final String PLACE_NAME = "PLACE";
  //public static final String IPLACE_NAME = "iPLACE";
  public static final String RLINK_NAME = "RLINK";
  public static final String RLINK_EXTENT_NAME = "rlink-extent";
  public static final String SIGNAL_NAME = "SIGNAL";
  public static final String LINK_NAME = "LINK";
  public static final String LINK_EXTENT_NAME = "link-extent";
  public static final String LINK_SUBORDINATES_NAME = "link-subordinates";
  public static final String ROOT_NAME = "SpatialML";


  /* private mapping between annotation type name and Class */

  private Set highlights = null;
  private Map defaultPrefs = null;

  private Importer[] importers = null;
  private Exporter[] exporters = null;

  public SpatialMLTask () {
    super (TASK_TITLE, TASK_NAME, TASK_VERSION, TASK_DESCRIPTION,
        MAIA_SCHEME, LOCAL_MAIA_SCHEME);

    // other initializations here as needed
    //initializeClassMap();
    //initHighlightKeys ();
    initPrefsAndHighlights ();
    initIO ();
  }
  /** Returns a new instance of the toolkit each time */
  public TaskToolKit getToolKit () {
    return new SpatialMLToolKit (this);
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
    // what are the possible values for a given attribute type
    if (annotType.equals(SpatialMLUtils.PLACE_TYPE)) {
      // TODO: take out hardcoding of attribute names. it's elsewhere too, shoot.
      if (attr.equals("country")) {
        return SpatialMLUtils.countryCodeSet;
      } else if (attr.equals("continent")) {
        return SpatialMLUtils.continentSet;
      } else if (attr.equals("mod")) {
        return SpatialMLUtils.modSet;
      } else if (attr.equals("type")) {
        return SpatialMLUtils.placeTypeSet;
      } else if (attr.equals("CTV")) {
        return SpatialMLUtils.ctvSet;
      } else if (attr.equals("form")) {
        return SpatialMLUtils.formSet;
      }
    } else if (annotType.equals(SpatialMLUtils.RLINK_TYPE)) {
      if (attr.equals("frame")) {
        return SpatialMLUtils.frameSet;
      } else if (attr.equals("direction")) {
        return SpatialMLUtils.directionSet;
      }
    } else if (annotType.equals(SpatialMLUtils.LINK_TYPE)) {
      if (attr.equals("linkType")) {
        return SpatialMLUtils.linkTypeSet;
      }
    } else if (annotType.equals(SpatialMLUtils.SIGNAL_TYPE)) {
      if (attr.equals("signalType")) {
        return SpatialMLUtils.signalTypeSet;
      }
    }
    
    // otherwise, it's a free-for-all
    return null;
  }

  public String getDefaultValue (AnnotationType annotType, String attr) {
    return null;
  }

  /**
   *  Correspondence between AnnotationTypes and the Class of
   *  AWBAnnotation used to implement them
   */
  /*
  public Class getAnnotationClass(ATLASType type) {
    return (Class)annotationClass.get(type);
  }
*/
  public Set getHighlightKeys () {
    return highlights;
  }

  /**
   * No 'constraint' is expected for simple TextExtentRegions
   */
  public String getHighlightKey (AWBAnnotation annot, Object constraint) {
    
    if (annot.getAnnotationType().equals(SpatialMLUtils.PLACE_TYPE) 
        && annot.getAttributeValue("type") != null 
        && !annot.getAttributeValue("type").equals("")) {
      
      return annot.getAnnotationType().getName() + ":" + annot.getAttributeValue("type");
    } else {
      return annot.getAnnotationType().getName();
    }
    
  }

  /***********************************************************************/
  /* Init methods */
  /***********************************************************************/

/*
  private void initializeClassMap() {
    annotationClass = new HashMap();
    annotationClass.put(getAnnotationType (PLACE_NAME),
        PhraseTaggingAnnotation.class);
    annotationClass.put(getAnnotationType (SIGNAL_NAME),
        PhraseTaggingAnnotation.class);
    annotationClass.put(getAnnotationType (PATH_EXTENT_NAME),
        PhraseTaggingAnnotation.class);

    annotationClass.put(getAnnotationType (PATH_NAME),
        Subord.class);
    annotationClass.put(getAnnotationType (LINK_NAME),
        AWBAnnotationImpl.class);
    annotationClass.put(getAnnotationType(LINK_EXTENT_NAME), 
	PhraseTaggingAnnotation.class);
    
  }
*/
  /** used several times, so I made them constants */
  //private static final String[] STYLE_KEYS = {PLACE_NAME, LINK_EXTENT_NAME, PATH_EXTENT_NAME, SIGNAL_NAME};
  //private static final String[] COLORS = {"{#8612a0,#ffffff}", "{#ef8d3d,#000000}", "{#990000,#ffffff}", "{#35FF62,#000000}"};

  /*
  private void initHighlightKeys () {
    highlights = new HashSet ();
    for (int i=0; i<STYLE_KEYS.length; i++)
      highlights.add (STYLE_KEYS[i]);

    highlights = Collections.unmodifiableSet (highlights);
  }
*/
  private void initPrefsAndHighlights () {
    defaultPrefs = new LinkedHashMap();
    highlights = new HashSet();
    
    highlights.add(LINK_EXTENT_NAME);
    defaultPrefs.put("task." + TASK_NAME + "." + LINK_EXTENT_NAME, "{#ef8d3d,#000000}");
    
    highlights.add(RLINK_EXTENT_NAME);
    defaultPrefs.put("task." + TASK_NAME + "." + RLINK_EXTENT_NAME, "{#44aabb,#000000}");
    
    highlights.add(SIGNAL_NAME);
    defaultPrefs.put("task." + TASK_NAME + "." + SIGNAL_NAME, "{#35FF62,#000000}");
    
    // different place types are a gradient of purples
    
    // start and stop color values for gradient computation
    int startR = 0xE0;
    int startG = 0x40;
    int startB = 0x90;
    int stopR = 0x20;
    int stopG = 0x10;
    int stopB = 0xE0;
    
    for (int i = 0; i < SpatialMLUtils.placeTypes.length; i++) {
      String type = SpatialMLUtils.placeTypes[i];
      if (type == null) {
        // for untyped places
        highlights.add(PLACE_NAME);
        defaultPrefs.put("task." + TASK_NAME + "." + PLACE_NAME, "{#8612a0,#ffffff}");
      } else {
        
        int r = (int)(((double)i / (double)SpatialMLUtils.placeTypes.length) * (stopR - startR)) + startR;
        int g = (int)(((double)i / (double)SpatialMLUtils.placeTypes.length) * (stopG - startG)) + startG;
        int b = (int)(((double)i / (double)SpatialMLUtils.placeTypes.length) * (stopB - startB)) + startB;
        
        String color = Integer.toHexString(0x100 | r).substring(1).toUpperCase() +
                       Integer.toHexString(0x100 | g).substring(1).toUpperCase() +
                       Integer.toHexString(0x100 | b).substring(1).toUpperCase();
        
        if (DEBUG > 2) {
          System.err.println(PLACE_NAME + ":" + type + " color is #" + color);
        }
        
        // for other places, base it on the type
        highlights.add(PLACE_NAME + ":" + type);
        defaultPrefs.put("task." + TASK_NAME + "." + PLACE_NAME + ":" + type, "{#" + color + ",#ffffff}");
      }
    }
    
    defaultPrefs = Collections.unmodifiableMap (defaultPrefs);
    highlights = Collections.unmodifiableSet (highlights);
  }

  private void initIO () {
    importers = new Importer[] {new ImportSpatialML (this)}; 
    exporters = new Exporter[] {new ExportSpatialML ()};
  }

  /*
   * Static methods for exhangers
   */
  
  public static HasSubordinates[] getReferentParents(AWBAnnotation annot, AnnotationType supertype) {
    return getReferentParents(annot, supertype.getName());
  }
  
  public static HasSubordinates[] getReferentParents(AWBAnnotation annot, String supertypeName) {
    return getReferentParents(annot, Pattern.compile("\\Q"+supertypeName+"\\E"));
  }
  
  public static HasSubordinates[] getReferentParents(AWBAnnotation annot) {
    return getReferentParents(annot, (Pattern) null);
  }

  public static HasSubordinates[] getReferentParents(AWBAnnotation annot, Pattern matchPattern) {
    // get the containting type
//    String typeName = annot.getAnnotationType().getName();
    if (annot != null) {
      if (DEBUG > 3)
        System.err.println("SpatialMLTask.getReferentParents: annot = " + annot);
      LinkedHashSet referentsSet = new LinkedHashSet();
      Iterator referents = annot.getReferentElements().iterator();
      while (referents.hasNext()) {
        ATLASElement referent = (ATLASElement)referents.next();
        if (DEBUG > 3)
          System.err.println("SpatialMLTask.getReferentParents: referent = " + referent);
        if (referent instanceof Region) {
          Iterator regionReferents = ((Region)referent).getReferentElements().iterator();
          while (regionReferents.hasNext()) {
            ATLASElement regionReferent = (ATLASElement)regionReferents.next();
            if (DEBUG > 3)
              System.err.println("SpatialMLTask.getReferentParents: regionReferent = " + regionReferent);
            String rrName = ((Annotation)regionReferent).getAnnotationType().getName();
            if (DEBUG > 3) {
              System.err.println("SpatialMLTask.getReferentParents; regionReferentString = " + rrName);
              if (regionReferent instanceof HasSubordinates) 
                System.err.println("SpatialMLTask.getReferentParents: regionReferent instanceof HasSubordinates = T");
            }            
            if (regionReferent instanceof HasSubordinates &&
                (matchPattern == null || matchPattern.matcher(rrName).matches())) {
              if (DEBUG > 3)
                System.err.println("SpatialMLTask.getReferentParents: this was the HasSubordinates target");
              referentsSet.add(regionReferent);
            }
          }
        }
      }
      return referentsSet.size() == 0 ? null : 
        (HasSubordinates[]) referentsSet.toArray(new HasSubordinates[referentsSet.size()]);
      // haven't found a superordinate that contains it
    }
    if (DEBUG > 0)
      System.err.println("SpatialMLTask.getMC: no parent found for: "+annot);
    return null;
  }


  
}
