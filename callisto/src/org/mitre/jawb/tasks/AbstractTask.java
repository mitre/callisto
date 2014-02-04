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

import org.mitre.jawb.atlas.AWBATLASImplementation;
import org.mitre.jawb.atlas.MultiPhraseAnnotation;
import org.mitre.jawb.atlas.PhraseTaggingAnnotation;
import org.mitre.jawb.atlas.SubordinateSetsAnnotation;
import org.mitre.jawb.atlas.AWBAnnotationImpl;

import java.awt.Component;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.text.Highlighter;

import gov.nist.atlas.MIMEClass;
import gov.nist.atlas.type.*;
import gov.nist.maia.MAIAScheme;
import gov.nist.maia.MAIALoader;
import gov.nist.atlas.ATLASAccessException;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.TextExtentRegion;
import org.mitre.jawb.atlas.NamedExtentRegions;
import org.mitre.jawb.io.URLUtils;
import org.mitre.jawb.prefs.PreferenceItem;
import org.mitre.jawb.gui.JawbFrame;

/**
 * Definition of the capabilities and properties of an annotation Task. <p>
 */
public abstract class AbstractTask implements Task {

  public static final int DEBUG = 0;

  protected String taskTitle = null;
  protected String taskName = null;
  protected String taskVersion = null;
  protected String taskDescription = null;
  protected URI homePageURI = null;
  protected URI localDocsURI = null;
  protected URI maiaURI = null;
  protected URI localMaiaURI = null;


  private MAIAScheme theMAIAScheme;
  private Set annotationTypes = null;
  private CorpusType corpusType = null;
  private AWBATLASImplementation theATLASImplementation;

  /* private maps of AnnotationType -> Set of attribute names (Strings)
   * contentAttributes is all the roles of basic Parameter content
   * subordinateAttrClasses are the names of all role-identified 
   * sub-Annotations
   * allAttributes is the union of those, plus any TextExtent-related 
   * attributes
   */
  protected Map allAttrClasses = new HashMap();
  protected Map contentAttrClasses = new HashMap();
  protected Map subordinateAttrClasses = new HashMap();
  protected Map annotationClass = new HashMap();
  
  /**
   * subclasses whose constructors call the no-args constructor will need to
   * initialize the fields above when the subclass is initialized, and finish
   * by calling initializeATLASObjects();
   */
  public AbstractTask () {
  }

  /**
   * Creates a new <code>AbstractTask</code> instance.
   *
   * @param title the human-readable title of this Task
   * @param name the language independant name of this Task. See the rules
   * defined for {@link Task#getName} for restrictions
   * @param version version identifier for the task.
   * @param descr a short, 3-4 sentance description of the task.
   * @param maiaURLString string representation of the cononical URL for this
   * tasks MAIA scheme.  Must not throw an exception using {@link URL#URL(String)}
   * @param localMaiaString 2
   * of this tasks MAIA scheme.  Must return non-null from {@link
   * Class#getResource(String)} when called from the Task object's class.
   */
  public AbstractTask (String title, String name,
                       String version, String descr,
		       String maiaURLString, String localMaiaString) {
    taskTitle = title;
    taskName = name;
    System.err.println("AbstractTask.constr taskName = " + taskName);
    System.err.flush();
    taskVersion = version;
    taskDescription = descr;
    try { // test the value
      maiaURI = new URI (maiaURLString);
    } catch (URISyntaxException x) {
      throw new RuntimeException ("Invalid MAIA Scheme URL", x);
    }
    URL localURL = this.getClass().getResource(localMaiaString);
    if (localURL == null)
      throw new RuntimeException("Local MAIA reference for "+taskTitle+
                                 " not found: "+localMaiaString);
    localMaiaURI = URLUtils.badURLToURI (localURL);

    initializeATLASObjects();
  }


  
  /**
   * Get the name of this task as a human-readable string. This may be
   * an i18n string if the task has been i18nized
   */
  public String getTitle () {
    return taskTitle;
  }

  /**
   * Get the name of this Task as a language independant ID, used to
   * differentiate tasks internally at runtime. This string must conform to
   * java 'name' specification.  This requirement is verified when the task is
   * initially loaded, and loading fails if the Name fails this
   * requirement.<p>
   */
  public String getName () {
    //System.err.println("AbstractTask.getName returning: " + taskName);
    return taskName;
  }

  /**
   * A version identifier for the task.
   */
  public String getVersion () {
    return taskVersion;
  }

  /**
   * Get the URL (as an absolute URI) of the tasks home page, where updates and
   * latest information can be found. May be null.
   */
  public URI getHomePage () {
    return homePageURI;
  }

  /**
   * A URI of files distributed locally with the task. May be null.
   */
  public URI getLocalDocs () {
    return localDocsURI;
  }

  /**
   * A description string used to describe the Task. This should be kept to
   * 3-4 short sentances <i>at most</i>. Used to describe the task to
   * users. Note that the first sentance (or a fragment of it) may be used in
   * certain places where a short description is needed.
   */
  public String getDescription () {
    return taskDescription;
  }

  /**
   * Get the cononical URL (as an absolute URI) of a MAIA document which
   * defines the structure of annotation for the task. A local copy of the
   * MAIA document must also be distributed with the task, and its (absolute)
   * URI provided by {@link #getLocalMaiaURI}.
   */
  public URI getMaiaURI () {
    return maiaURI;
  }

  /**
   * Get the URL (as an absolute URI) for a local copy of this Tasks MAIA
   * document.
   * @see #getMaiaURI
   */
  public URI getLocalMaiaURI () {
    return localMaiaURI;
  }
  
  /**
   * Get the ATLAS MAIAScheme object that describes this task.
   */
  public MAIAScheme getMaiaScheme() {
    return theMAIAScheme;
  }


  /**
   * Get the AWBATLASImplementation that has been initialized with
   * the pointer to this Task object.
   */
  public AWBATLASImplementation getATLASImplementation() {
    return theATLASImplementation;
  }

  /**
   * Returns <code>null</code> indicating this task has no importers by
   * default.
   */
  public Importer[] getImporters () {
    return null;
  }

  /**
   * Returns <code>null</code> indicating this task has no exporters by
   * default.
   */
  public Exporter[] getExporters () {
    return null;
  }
  
  /**
   * Returns <code>null</code> indicating this task has no exporters by
   * default.
   */
  public Validator[] getValidators() {
    return null;
  }

  /**
   * Returns <code>null</code> indicating this task has no exporters by
   * default.
   */
  public Validator[] getAutoValidators() {
    return null;
  }

  /**
   * Returns the DefaultAutotagger for simple text-extent autotagging by
   * default.
   */
  public Autotagger getAutotagger() {
    return new DefaultAutotagger();
  }

  /**
   * Returns null indicating that this task has no user definable preferences
   * other than highlights.
   */
  public PreferenceItem getPreferenceItem () {
    return null;
  }


  /**
   * Returns an <code>ATLASType</code> object which can be used to
   * create a corpus for this task. While a MaiaScheme may have more than one
   * corpus, Jawb only works with one.
   */
  public CorpusType getCorpusType () {
    return corpusType;
  }

  /**
   *  Correspondence between AnnotationTypes and the Class of
   *  AWBAnnotation used to implement them
   */
  public Class getAnnotationClass(ATLASType type) {
    return (Class) annotationClass.get(type);
  }

  /**
   * Returns a set of <code>ATLASType</code> objects which can be used to
   * create annotations for this task.
   */
  public Set getAnnotationTypes () {
    return annotationTypes;
  }
  
  /**
   * Returns a subset of the <code>ATLASType</code> objects which can
   * be used to create annotations for this task, consisting of only
   * those for which there should be a "Modify Extent" or one or more
   * "Modify <named> Extent" action(s).  This version returns all
   * annotation types, in which case modify actions will be created
   * for every type that has one or more text extents.  If other
   * behavior is desired, override this method.
   */
  public Set getExtentModifiableAnnotationTypes () {
    return getAnnotationTypes();
  }
  
  /**
   * Returns the <code>AnnotationType</code> object which can be used to
   * create and compare annotations for this task.
   */
  public AnnotationType getAnnotationType (String typeName) {
    if (DEBUG > 0)
      System.err.println("AbsTask.getAnnotType(" + typeName + ") returning " +
                         theMAIAScheme.getAnnotationType(typeName));
    return theMAIAScheme.getAnnotationType(typeName);
  }

  /**
   * Returns the String names of attributes (including single subordinates)
   * available for a particular type of annotation. Does not need an instance.
   */
  public Set getAttributes (AnnotationType annotType) {
    /*
    if (!allAttrClasses.containsKey(annotType)) {
      initializeAttributes(annotType);
      }*/
    Map map = (Map) allAttrClasses.get(annotType);
    if (map == null)
      return Collections.EMPTY_SET;
    return map.keySet();
  }

  /**
   * Returns a Set of the String names of content attributes available
   * for a particular type of annotation. Does not need an instance.
   */
  public Set getContentAttributes (AnnotationType annotType) {
    Map map = (Map) contentAttrClasses.get(annotType);
    if (map == null)
      return Collections.EMPTY_SET;
    return map.keySet();
  }

  /**
   * Returns a Set of the String names of any role-identified (single)
   * subordinate attributes available for a particular type of
   * annotation. Does not need an instance.
   */
  public Set getSubordinateAttributes (AnnotationType annotType) {
    Map map = (Map) subordinateAttrClasses.get(annotType);
    if (map == null)
      return Collections.EMPTY_SET;
    return map.keySet();
  }


  /** Returns <code>null</code> for all default value requests. */
  public String getDefaultValue (AnnotationType annotType, String attr) {
    return null;
  }

  /**
   * Pass <code>annot</code>'s type and <code>attr</code> to {@link
   * #getDefaultValue(AnnotationType,String)}
   */
  public String getDefaultValue (AWBAnnotation annot, String attr) {
    return getDefaultValue (annot.getAnnotationType(), attr);
  }


  /**
   * Return the reflected Class type of an attribute value.  This is ambiguous
   * for sub-annotations (single cardinality annotation regions which have
   * been given an attribute name (ie RDC-relation.arg1)), as the class name
   * is the same though the AnnotationType allowed may be different. For
   * better specification, use {@link #getSubordinateType}.
   */
  public Class getAttributeType (AnnotationType type, String attribute) {
    Map classMap = (Map) allAttrClasses.get(type);
    if (classMap == null)
      System.err.println("AbstrTask.getAT: Unknown attribute type: "+
                         type.getName());
    Class clazz = (Class) classMap.get(attribute);
    if (clazz == null) {
      System.err.println ("AbstrTask.getAT: Unable to find attribute type '"+
                          attribute+"' in AnnotationType "+type.getName ());
      System.err.println ("AbstrTask.getAT: classMap was " +
			  classMap.toString());
      Thread.dumpStack();
      return Object.class;
    }
    return clazz;
  }

  /**
   * Determines whether or not the given subordinate Annotation is a
   * valid filler of the given role in the given superordinate
   * Annotation.  If role is null, determines whether or not the given
   * subordinate Annotation is a valid member of a subordinate set of
   * the given superordinate Annotation.  If the role provided is not
   * a valid role for the superordinate, or does not take an
   * Annotation, this method will return false.
   *
   * This simple default annotation just checks whether the
   * AnnotationTypes are consistent with a valid assignment.
   */
  public boolean isValidSubordinate(AWBAnnotation superordinate, 
				    AWBAnnotation subordinate,
				    String role) {
    AnnotationType superType = superordinate.getAnnotationType();
    AnnotationType subType = subordinate.getAnnotationType();
    // Check whether or not the subType is appropriate for the
    // superType and role, if provided
    if (role != null) {
      AnnotationType expectedSubType = getSubordinateType(superType, role);
      if (expectedSubType == null || !expectedSubType.equals(subType)) {
	return false;
      }
    } else {
      try {
        return superType.getTypeForRegion().canAddToSubordinateSet(subordinate);
      } catch (Exception e) {
	// if anything went wrong with the above test, it isn't valid
	// ATLASAccessException is one possibility, but maybe not the only one
	return false;
      }
    }

    // if no other restrictions and we got this far, it must be a
    // valid subordiante.
    return true;	
  }

  /**
   * Returns the AnnotationType of the role-identified subordinate
   * with the given role for the given type of the superordinate
   * Annotation.
   */
  public AnnotationType getSubordinateType(AnnotationType type, String role) {
    try {
      AnnotationType subType =
	(AnnotationType)type.getTypeForRegion().getTypeOfSubordinateWith(role);
      return subType;
    } catch (ClassCastException e) {
      return null;
    } catch (ATLASAccessException e) {
      return null;
    }
  }

  /**
   * Returns the human readable task name and the machine readable name as the
   * string representation
   */
  public String toString () {
    return getTitle () + " ("+getName ()+")";
  }


  /***********************************************************************/
  /* Init methods */
  /***********************************************************************/

  protected void initializeATLASObjects() {
    theATLASImplementation = new AWBATLASImplementation (this);
    
    // Initialize the maia scheme with the ATLASImplementation
    MAIALoader loader = new MAIALoader(theATLASImplementation);
    if (DEBUG > 1)
      System.err.println ("AbstractTask: loading MAIA from "+localMaiaURI);
    try {
      theMAIAScheme =
        loader.loadTypeDefinitionsFrom(new URL(localMaiaURI.toString ()));
    } catch (MalformedURLException e) {/* we know it's ok, and will get
                                        * checked later anyway */}
    
    // Retrieve the corpus type. Should only be one, so if isn't one... let
    // the exception fly. Should be exactly one.
    Iterator iter = theMAIAScheme.iteratorOverCorpusTypes();
    corpusType = (CorpusType)iter.next();

    
    // retrieve the AnnotationTypes
    Set annotTypes = new HashSet();
    iter = theMAIAScheme.iteratorOverAnnotationTypes();
    while (iter.hasNext()) {
      AnnotationType annotType = (AnnotationType) iter.next();
      initializeAnnotation(annotType);
      annotTypes.add(annotType);
    }
    annotationTypes = Collections.unmodifiableSet(annotTypes);

  }

  /** initialize set of 'allAttrClasses' and 'attributeClasses' */
  protected void initializeAnnotation(AnnotationType annotType) {
    if (DEBUG > 1)
      System.err.println("AbstrTask.initAttr2: "+annotType.getName());
    Map allClasses = new HashMap();
    Map contentClasses = new HashMap();
    Map subordinateClasses = new HashMap();
    
    // content parameters:
    ContentType ctype = annotType.getTypeForContent();
    if (DEBUG > 2)
      System.err.println("  -- type for content is: " + ctype.getName());
    Iterator roleIter = ctype.getDefinedRolesForSubordinates();
    while (roleIter.hasNext()) {
      String role = (String) roleIter.next();
      if (DEBUG > 2) 
	System.err.println ("  -- has content role: " + role);
      ParameterType ptype = (ParameterType) ctype.getTypeOfSubordinateWith(role);
      String name = ptype.getName();
      // ignore case for legacy files: should be String
      if (name.equalsIgnoreCase("String"))
        contentClasses.put(role, String.class);
      else if (name.equalsIgnoreCase("Boolean"))
        contentClasses.put(role, Boolean.class);
      else if (name.equalsIgnoreCase("Integer"))
        contentClasses.put(role, Boolean.class);
      else {
        System.err.println("Unrecognized parameter type: "+role+
                           ": using String");
        contentClasses.put(role, String.class);
      }
    }

    // subordinate annotation parameters:
    RegionType rtype = annotType.getTypeForRegion();
    Iterator subRoleIter = rtype.getDefinedRolesForSubordinates();
    while (subRoleIter.hasNext()) {
      String subRole = (String) subRoleIter.next();
      ATLASType stype = rtype.getTypeOfSubordinateWith(subRole);
      if (stype instanceof AnnotationType) {
	subordinateClasses.put(subRole, AWBAnnotation.class);
      }
    }

    // Verify the annotation has a region supported by Callisto
    Set namedExtents = null;

    // set the annotation Class use to instantiate the annotation, and add some
    // pseudo variables that Callisto uses to speed things up
    if (isTextExtentRegion(rtype)) {
      if (DEBUG > 1)
        System.err.println("   --isTextExtent");
      allClasses.put("TextExtentStart", Integer.class);
      allClasses.put("TextExtentEnd", Integer.class);
      allClasses.put("TextExtent", String.class);

      annotationClass.put(annotType, PhraseTaggingAnnotation.class);
    }
    else if ( (namedExtents = getNamedExtentRegionRoles(rtype)).size() > 0) {
      if (DEBUG > 1)
        System.err.println("   --hasNamedExtents");
      Iterator nIter = namedExtents.iterator();
      while (nIter.hasNext()) {
        String subreg = (String) nIter.next();
        if (DEBUG > 1)
          System.err.println("     extent: "+subreg);
        allClasses.put(subreg+".TextExtentStart", Integer.class);
        allClasses.put(subreg+".TextExtentEnd", Integer.class);
        allClasses.put(subreg+".TextExtent", String.class);
      }
      annotationClass.put(annotType, MultiPhraseAnnotation.class);
    }
    else if (rtype.getSubordinateSetNumber() > 0) {
      if (DEBUG > 1)
        System.err.println("   --SubordinateSets");
      annotationClass.put(annotType, SubordinateSetsAnnotation.class);
    }
    else {
      if (DEBUG > 1)
        System.err.println("   --simple Annot");
      //annotationClass.put(annotType, SubordinateSetsAnnotation.class);
      // To be precise, it should be simple, but it seems that when a role
      // identified sub-annot has the same type as a set if
      // indefinate-cardnality annots, then the 
      annotationClass.put(annotType, AWBAnnotationImpl.class);
    }

    allClasses.putAll(contentClasses);
    allClasses.putAll(subordinateClasses);

    // add these sets to the hash maps of attributes for this annotType
    allAttrClasses.put(annotType, allClasses);
    contentAttrClasses.put(annotType, contentClasses);
    subordinateAttrClasses.put(annotType, subordinateClasses);
  }

  private Set getNamedExtentRegionRoles(RegionType rtype) {
    Set namedExtents = new LinkedHashSet();
    
    Iterator regionSubIter = rtype.getDefinedRolesForSubordinates();
    while (regionSubIter.hasNext()) {
      String subRole = (String) regionSubIter.next();
      ATLASType stype = rtype.getTypeOfSubordinateWith(subRole);
      if (DEBUG > 3)
	System.err.println(" ----checking "+subRole+", "+stype.getName());
      if (stype instanceof RegionType && isTextExtentRegion((RegionType)stype))
        namedExtents.add(subRole);
    }
    return namedExtents;
  }
  
  /**
   * Check a region for the structure to qualify as a "TextExtentRegion".
   * There are three things Callisto expects:
   * 
   * <ol><li>Has AnchorType subordinates with roles 'start' and 'end'
   *     <li>Each of those AnchorTypes has a single SignalType with mimeClass
   *         'text'
   *     <li>Each of those AnchorTypes also has a single ParameterType of type
   *         'char' and name 'char'
   * </ol>
   */
  private boolean isTextExtentRegion(RegionType rtype) {
    // unfortunately, ATLAS doesn't have a query mechanism to see if
    // subordinate exists, other than calling getTypeOfSubordinate() and
    // catching an exception or not.  Because of the large overhead of creating
    // exceptions it is faster to iterate over subordinates individually, and
    // set flags :(

    boolean hasStart = false;
    boolean hasEnd = false;

    Iterator regionSubIter = rtype.getDefinedRolesForSubordinates();
    while (regionSubIter.hasNext()) {
      String subRole = (String) regionSubIter.next();
      ATLASType stype = rtype.getTypeOfSubordinateWith(subRole);
      //System.err.println("   -checking "+subRole+", "+stype.getName());
      if (stype instanceof AnchorType &&
          (subRole.equals("start") || subRole.equals("end"))) {
        boolean hasCharParam = false;
        boolean hasTextSignal = false;
        
        Iterator anchorSubIter = stype.getDefinedRolesForSubordinates();
        while (anchorSubIter.hasNext()) {
          String ssRole = (String) anchorSubIter.next();
          ATLASType sstype = stype.getTypeOfSubordinateWith(ssRole);
          //System.err.println("      -checking "+ssRole+", "+sstype.getName());
          if (sstype instanceof ParameterType) {
            hasCharParam = ssRole.equals("char") && sstype.getName().equals("char");
          }
          else if (sstype instanceof SignalType) {
            SignalType sigType = (SignalType) sstype;
            hasTextSignal = sigType.getMIMEClass() == MIMEClass.TEXT;
          }
          //System.err.println("       char="+hasCharParam+", signal="+hasTextSignal);
        }

        if (hasCharParam && hasTextSignal) {
          if (subRole.equals("start"))
            hasStart = true;
          else if (subRole.equals("end"))
            hasEnd = true;
        }
      }
    }

    return (hasStart && hasEnd);
  }

  /**
   * Returns true if this task saves out changes immediately as the
   * happen, and false otherwise.  Returns false by default.  Tasks
   * that do save continuously must override this method.
   */
  public boolean savesContinuously() {
    return false;
  }

  /**
   * Does anything task-specific that needs to be done before closing
   * a JawbDocument.  Returns true if the close should continue, false
   * if the pre-close activites result in the close being cancelled.
   */
  public boolean documentClosing(JawbFrame jf) {
    return true;
  }

  /**
   * Does anything task-specific that needs to be done after closing a
   * JawbDocument.  Returns true if successful or if there is nothing to
   * do, false otherwise.
   */
  public boolean documentClosed(JawbFrame jf) {
    return true;
  }

}
