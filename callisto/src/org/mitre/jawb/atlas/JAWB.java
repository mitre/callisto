
/* ----------------------------------------------------------------------
 * 
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
 * 
 * ----------------------------------------------------------------------
 * 
 * NOTICE
 * 
 * This software was produced for the U. S. Government
 * under Contract No. W15P7T-09-C-F600, and is
 * subject to the Rights in Noncommercial Computer Software
 * and Noncommercial Computer Software Documentation
 * Clause 252.227-7014 (JUN 1995).
 * 
 * (c) 2009 The MITRE Corporation. All Rights Reserved.
 * 
 * ----------------------------------------------------------------------
 *
 */
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

package org.mitre.jawb.atlas;

import gov.nist.atlas.ATLASElement;
import gov.nist.atlas.Analysis;
import gov.nist.atlas.Anchor;
import gov.nist.atlas.Annotation;
import gov.nist.atlas.CorporaManager;
import gov.nist.atlas.Corpus;
import gov.nist.atlas.Id;
import gov.nist.atlas.IdentifiableATLASElement;
import gov.nist.atlas.MIMEClass;
import gov.nist.atlas.Region;
import gov.nist.atlas.ReusableATLASElement;
import gov.nist.atlas.Signal;
import gov.nist.atlas.ref.ATLASRef;
import gov.nist.atlas.ref.AnchorRef;
import gov.nist.atlas.ref.AnnotationRef;
import gov.nist.atlas.ref.RegionRef;
import gov.nist.atlas.type.ATLASType;
import gov.nist.atlas.type.AnalysisType;
import gov.nist.atlas.type.AnnotationType;
import gov.nist.atlas.type.RegionType;
import gov.nist.atlas.util.ATLASElementFactory;
import gov.nist.atlas.util.ATLASImplementation;
import gov.nist.maia.MAIAScheme;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mitre.jawb.tasks.Task;

/**
 * JAWB is the basic class used for opening a file and its related ATLAS
 * structures.  The constructors require either an existing AIF file, or a
 * MAIA scheme and a plain text file.  The AIF constructor should probably
 * require a MAIA scheme as well, but for now it doesn't as the current AIF
 * importing code does not use the MAIA scheme for validation or anything
 * else.  The constructor that takes the AIF file also doesn't fill the tsd,
 * so creating tags using the simpler createAnnotation method won't work right
 * now if that construcor is used.<p>
 *
 * This file makes the assumption that jAWB will always support
 * phrase-tagging, that is Annotations with a text-extent region, and simple
 * string-valued attributes.  Phrase-tagging annotations can be added using
 * the createAnnotation method.  Subclasses may define additional new tag
 * creation methods, but they should be clearly named, i.e., newEDTTag.<p>
 *
 * This file assumes that phrase-tagging MAIA files will always
 * conform to certain requirements.  These are:<p>
 *
 * <ol>
 *   <li>Must contain the following Anchor, Region & Parameter definitions:
 *  <code>
 *  &lt;AnchorType name='text-point'
 *    &lt;SignalType ref='text' role='text'/&gt;
 *    &lt;ParameterType ref='char' role='char'/&gt;
 *  &lt;/AnchorType&gt;
 *
 *  &lt;RegionType name='text-extent'&gt;
 *    &lt;AnchorType ref='text-point' role='start'/&gt;
 *    &lt;AnchorType ref='text-point' role='end'/&gt;
 *  &lt;/RegionType&gt;
 *
 *  &lt;Parameter name='char'&gt;
 *  </code>
 *
 *   <li>Corpus must include exactly one Analysis type per Annotation type
 * (though multiple Annotation types may go into the same Analysis)
 *
 *   <li>Assumes a text SimpleSignal
 * </ol>
 */
public class JAWB {

  private static final int DEBUG = 0;

  private AWBCorpusImpl theCorpus;
  // subclasses will want to set the Implementation themselves
  // so this is protected rather than private
  protected AWBATLASImplementation theImplementation;
  private ATLASElementFactory theFactory;
  private MAIAScheme theMAIAScheme;
  private AWBSimpleSignal theSignal;
  private HashMap tsd;
  private HashSet myAnnotationTypeSet;
  private Set annotationTypeSet;
  private Task theTask;

  /**
   *
   * @throws IllegalArgumentException If the URL is not absolute
   * @throws MalformedURLException If a protocol handler for the URL could not
   *   be found, or if some other error occurred while constructing the URL
   */
  public static JAWB fromAIF (URI uri, Task task)
    throws IllegalArgumentException, MalformedURLException {

    URL url = uri.toURL ();
    if (DEBUG > 0)
      System.out.println("JAWB.fromAIF: loading from url: "+url);
    JAWB jawb = new JAWB (task);

    jawb.theCorpus = (AWBCorpusImpl)
      CorporaManager.loadCorpus(url, jawb.theImplementation);

    if (DEBUG > 0)
      System.out.println("JAWB.fromAIF: loaded corpus "+
                         jawb.theCorpus.getLocation());
    jawb.theMAIAScheme = jawb.theCorpus.getMAIAScheme();

    // TODO: This has no way of knowing what character 'encoding' the signal
    // is, because jATLAS ignores it when creating (see 'fromSignal')

    // find the signal whose type attribute is 'text'
    ATLASElement[] signals = jawb.theCorpus.getAllSignals().toArray();
    for (int i = 0; i < signals.length; i++) {
      AWBSimpleSignal signal = (AWBSimpleSignal) signals[i];
      if (signal.getATLASType().getName().equals("text")) {
        // JAWB wants to pretend the 'text' type signal is the only signal
        jawb.theSignal = signal;
        break;
      }
    }
    // TODO: if there is no signal with type 'text', we're in trouble
    if (jawb.theSignal == null) {
      throw new RuntimeException("The document has no text signal.");
    }

    // HACK:  if adding pointers into the Annotations, we will need
    // to go through all and add them here, as that is not a saved
    // part of the Annotation...
    Iterator annotIter = jawb.theCorpus.getAllAnnotations().iterator();
    while (annotIter.hasNext()) {
      AWBAnnotation annot = (AWBAnnotation)annotIter.next();
      if (DEBUG > 0) {
        System.out.println("JAWB: Setting Jawb for: " + annot.asAIFString());
        System.out.println("JAWB: Annotation Class is " + annot.getClass());
      }
      annot.setJawb(jawb);
    }

    jawb.initTypesAndTsd();
    return jawb;
  }

  /** @deprecated use {@link #fromSignal(URI,Task,String,String)} */
  public static JAWB fromSignal (URI uri, String encoding, Task task)
    throws IllegalArgumentException, MalformedURLException {
    return fromSignal (uri, task, null, encoding);
  }
  public static JAWB fromSignal (URI uri, Task task,
                                 String mimeType, String encoding)
    throws IllegalArgumentException, MalformedURLException {

    JAWB jawb = new JAWB (task);
    URL url = uri.toURL ();

    jawb.theMAIAScheme = task.getMaiaScheme();
    /* need to figure out the name to pass in */
    jawb.theCorpus = (AWBCorpusImpl)
      jawb.theFactory.createCorpus(task.getCorpusType());
    jawb.theSignal = (AWBSimpleSignal)
      jawb.theFactory.createSimpleSignal("text", jawb.theCorpus,
                                         MIMEClass.TEXT,
                                         mimeType, encoding,
                                         "", url.toString());
    jawb.initTypesAndTsd();
    return jawb;
  }

  private JAWB (Task task) {
    // This is always required by ATLAS, even for test code
    System.getProperties().put ("java.protocol.handler.pkgs",
                                "gov.nist.atlas.impl");
    theTask = task;
    /* do I actually need this anymore?? FIXME */
    theImplementation = task.getATLASImplementation();
    theFactory = ATLASElementFactory.getFactoryFor(theImplementation);
  }

  /** Release memory and resources from ATLAS for the backing Corpus. */
  public void close () {
    CorporaManager.releaseCorpus (theCorpus);
  }

  // assumes one AnalysisType per AnnotationType
  // if this is not the case, the last one returned by getAllAnalyses
  // will be the Analysis where annotations of that type get put
  // in the tsd (Tag Set Definitions)
  private void initTypesAndTsd () {
    tsd = new HashMap(20);
    Iterator allAnalysesIter = theCorpus.getAllAnalyses().iterator();
    myAnnotationTypeSet = new HashSet();
    while (allAnalysesIter.hasNext()) {
      AnalysisType analType =
	((Analysis)allAnalysesIter.next()).getAnalysisType();
      Iterator subtypeIter =
	analType.getContainedTypesInSubordinateSets();
      while (subtypeIter.hasNext()) {
	AnnotationType subtype = (AnnotationType)subtypeIter.next();
	tsd.put (subtype,analType.getName());
	// since annotationTypes is a Set, add will only add the
	// subtype if it's not already in there
	myAnnotationTypeSet.add(subtype);
      }
    }
    annotationTypeSet = Collections.unmodifiableSet(myAnnotationTypeSet);
    if (DEBUG > 0)
      System.out.println("JAWB: New TSD Generated from MAIAScheme:\n" +
			 tsd.toString());
  }

  /** returns an immutable Set contain all the AnnotationType
   * objects allowed in this JAWB object's corpus. */
  public Set getAnnotationTypes() {
    return annotationTypeSet;
  }


  /**
   * Create an empty AWBAnnotation of the given type.  Region and
   * Content details will have to be added later.
   *
   * The Annotation thus created is Invalid!  If required
   * elements are not added, saving the corpus as AIF may fail.
   *
   * The analysis in which the Annotation is created is retrieved
   * from the tsd, the tag-set-definitions HashMap which is inferred
   * from the MAIA scheme.
   *
   * The type must be an AnnotationType which is defined in the MAIA
   * file.  The set of valid AnnotationTypes can be retrieved by calling
   * getAnnotationTypes().
   *
   */
  public final AWBAnnotation createAnnotation(AnnotationType type) {

    if (DEBUG > 0)
      System.err.println("JAWB.createAnnot of type " + type.getName());

    // retrieve the analysisRole from the tsd
    String analysisRole = (String)(tsd.get(type));

    if (DEBUG > 1)
      System.err.println("JAWB.createAnnot analysisRole is " + analysisRole);

    // retrieve the appropriate analysis
    Analysis analysis = theCorpus.getAnalysisWithRole(analysisRole);

    // Get the correct RegionType and role for this AnnotationType
    RegionType rtype = type.getTypeForRegion();
    String rrole = type.getRoleForRegion();
    if (DEBUG > 1)
      System.err.println("JAWB.createAnnot rtype = " + rtype.getName() +
			 " rrole = " + rrole);

    // Create empty region and a reference to it
    Id regionId = theFactory.createNewIdFor(rtype);
    Region emptyRegion = theFactory.createEmptyRegion(rtype,
						      theCorpus,
						      regionId);
    RegionRef ref = ATLASRef.createRegionRef(emptyRegion, rrole);

    // Create a new ID for the new Annotation
    Id newId = theFactory.createNewIdFor(type);
    
    if (DEBUG > 1)
      System.err.println("JAWB.createAnnot newId=" + newId);

    // Create the annotation and set defaults
    AWBAnnotation annotation =
      (AWBAnnotation)(theFactory.createPrimalAnnotation(type,
							analysis,
							newId,
							ref));
    if (DEBUG > 0)
      System.out.println("JAWB: created primal annotation\n");

    // HACK
    annotation.setJawb(this);

    String[] attribs = annotation.getAttributeKeys ();
    if (DEBUG > 0)
      System.err.println("JAWB.createAnnot attribs: " + attribs);
    if (DEBUG > 2) {
      for (int i=0; i<attribs.length; i++) {
        System.err.println("\tattribs[" + i + "]=" + attribs[i]);
      }
    }
    for (int i=0; i<attribs.length; i++) {
      String defaultValue = theTask.getDefaultValue (type, attribs[i]);
        if (DEBUG > 1) {
          System.err.println("JAWB.createAnnot setting " + attribs[i]+" to " +
                             (defaultValue==null?"null":defaultValue));
        }
      if (defaultValue != null) {
        try {
          annotation.setAttributeValue (attribs[i], defaultValue);
        } catch (UnmodifiableAttributeException ignored) {
          System.err.println("JAWB.createAnnot unmodifiable attr: " +
                             attribs[i] + "->" + defaultValue);
        }
      }
    }

    if (DEBUG > 0)
      System.out.println("JAWB: set attrs to default values\n");

    // return the Annotation
    return annotation;

  }

  /**
   *  Creates a new AWBAnnotation of the given type, with the given
   *  subordinates.  Any of the array parameters may be null if not
   *  required
   *
   * @param type               the type of the AWBAnnotation to create
   * @param roleSubAnnotations role-identified subAnnotations
   * @param subRegions         role-identified sub-regions of this
   *                           annotation's main Region
   * @param subAnchors         role-identified anchors defining this
   *                           annotation's main Region
   * @param setSubAnnotations  subAnnotations of this Annotation's main
   *                           region belonging in subordinate sets (not
   *                           role-identified -- the correct set for each is
   *                           determined by its AnnotationType
   */
  public final AWBAnnotation createAnnotation(AnnotationType type,
				    AnnotationRef[] roleSubAnnotations,
				    RegionRef[] subRegions,
				    AnchorRef[] subAnchors,
				    Annotation[] setSubAnnotations) {
    // retrieve the analysisRole from the tsd
    String analysisRole = (String)(tsd.get(type));

    // retrieve the appropriate analysis
    Analysis analysis = theCorpus.getAnalysisWithRole(analysisRole);

    // Get the correct RegionType and role for this AnnotationType
    RegionType rtype = type.getTypeForRegion();
    String rrole = type.getRoleForRegion();

    // Create the region and a reference to it
    RegionRef ref = newRegionRef(rrole, rtype,
				 roleSubAnnotations,
				 subRegions,
				 subAnchors);
    if (setSubAnnotations != null) {
      addSetSubAnnotations(ref, setSubAnnotations);
    }

    // Create the annotation and set defaults
    AWBAnnotation annotation =
      (AWBAnnotation)(theFactory.createAnnotation(type, analysis, ref));

    // HACK
    // moved up to before setting defaults RK 8/2/07 so Jawb will be set as
    // soon as annot is created, in case something (like autotag) triggers
    // off of the creation or setting of some of the attributes and needs
    // to access it
    annotation.setJawb(this);

    String[] attribs = annotation.getAttributeKeys ();
    for (int i=0; i<attribs.length; i++) {
      String defaultValue = theTask.getDefaultValue (type, attribs[i]);
      if (defaultValue != null) {
        try {
          annotation.setAttributeValue (attribs[i], defaultValue);
        } catch (UnmodifiableAttributeException ignored) {}
      }
    }


    return annotation;
  }

  /**
   *  Creates a new AWBAnnotation of the given type, with the given
   *  subordinates.  Role-identified subordinates are specified in a
   *  matching pair of arrays of Annotation subordinates and String
   *  roles.
   */
  public final AWBAnnotation createAnnotation(AnnotationType type,
				    Annotation[] roleSubAnnotations,
				    String[] roles,
				    RegionRef[] subRegions,
				    AnchorRef[] subAnchors,
				    Annotation[] setSubAnnotations) {
    AnnotationRef[] subAnnotationRefs =
      roleIdentifiedAnnotations(roleSubAnnotations, roles);
    return createAnnotation(type, subAnnotationRefs,
		  subRegions, subAnchors, setSubAnnotations);
  }

  /**
   * Creates a new text-extent tag with no other region subordinates
   * or content.
   *
   * The type must be an AnnotationType which is defined in the MAIA file
   */

  public final AWBAnnotation createAnnotation(AnnotationType type,
				    int start, int end) {
    AnchorRef[] textExtentAnchors =
      newAnchorRefs(start, end);
    return createAnnotation(type, null, null, textExtentAnchors, null);
  }


  /**
   *  Create a text-extent tag with other subordinates as specified.
   *  Start and end text-point offsets are provided instead of an
   *  array of AnchorRef's.
   */
  public final AWBAnnotation createAnnotation(AnnotationType type,
				    int start, int end,
				    AnnotationRef[] roleSubAnnotations,
				    RegionRef[] subRegions,
				    Annotation[] setSubAnnotations) {

    AnchorRef[] textExtentAnchors =
      newAnchorRefs(start, end);
    return createAnnotation(type, roleSubAnnotations, subRegions,
		  textExtentAnchors, setSubAnnotations);
  }

  /**
   *  Creates a new RegionRef to a region with the provided role and
   *  subordinates.
   *
   *  End users should not call this directly.  They should call
   *  createAnnotation which will create the region appropriately.
   */
  protected final RegionRef newRegionRef(String role, RegionType type,
					 AnnotationRef[] roleSubAnnotations,
					 RegionRef[] subRegions,
					 AnchorRef[] subAnchors) {
    Region region = theFactory.createRegion(type, theCorpus,
					    roleSubAnnotations,
					    subRegions,
					    subAnchors);
    return ATLASRef.createRegionRef(region, role);
  }

  /**
   *  Creates a new RegionRef to a region with the provided role and
   *  subordinates, plus annotations that belong in the Region's
   *  subordinate sets.
   */
  protected final RegionRef newRegionRef(String role, RegionType type,
					 AnnotationRef[] roleSubAnnotations,
					 RegionRef[] subRegions,
					 AnchorRef[] anchors,
					 Annotation[] setSubAnnotations) {

    RegionRef ref =
      newRegionRef(role, type, roleSubAnnotations, subRegions, anchors);
    if (setSubAnnotations != null) {
      addSetSubAnnotations(ref, setSubAnnotations);
    }
    return ref;
  }


  /**
   * utility for creating text-extent style RegionRef
   */
  protected final RegionRef newRegionRef(int start, int end,
					 String role,
					 RegionType type) {
      return newRegionRef(start, end, role, type, "text-point");
  }

  /**
   * Utility for creating text-extent style RegionRefs, but with anchors of the
   * specified ATLAS type.
   *
   * @param start character offset of start of region
   * @param end character offset of end of region
   * @param role the role this RegionRef will have in its parent
   * @param type the ATLAS type of the Region
   * @param anchorType the ATLAS type of the Anchor
   * @return a new Region reference
   */
  protected final RegionRef newRegionRef(int start, int end,
					 String role,
					 RegionType type, String anchorType) {
    return newRegionRef(role, type,
        null, null,
        newAnchorRefs(anchorType, start, end));
  }


  /**
   * Returns a pair of text-point anchors with roles start and end,
   * and with the given integer values.
   *
   * FIXME "text-point" and "char" are hard-coded -- probably not necessary
   */
  public final AnchorRef[] newAnchorRefs (int start, int end) {
   return newAnchorRefs("text-point", start, end);
  }

  /**
   * @param anchorType ATLAS type of anchor to create
   * @param start
   * @param end
   * @return a pair of text-point anchors of the given ATLAS type with roles
   *         start and end, and with the given integer values
   */
  public final AnchorRef[] newAnchorRefs (String anchorType, int start, int end) {
    // create start and end anchors
    // checks if they already exist and reuses them if possible
    String startStr = Integer.toString(start);
    Anchor startAnchor = theCorpus.getAnchorByOffset(anchorType, startStr);
    if (startAnchor == null) {
      startAnchor = newTextAnchor(anchorType, startStr);
    }
    String endStr = Integer.toString(end);
    Anchor endAnchor = theCorpus.getAnchorByOffset(anchorType, endStr);
    if (endAnchor == null) {
      endAnchor = newTextAnchor(anchorType, endStr);
    }
    //System.out.println("created or reused anchors\n");
    //System.out.println("Start Anchor:");
    //System.out.println(startAnchor.asAIFString());

    AnchorRef startRef = ATLASRef.createAnchorRef(startAnchor, "start");
    AnchorRef endRef = ATLASRef.createAnchorRef(endAnchor, "end");

    return new AnchorRef[]{startRef, endRef};
  }


  /**
   *  Create a new Anchor of type "text-point" with the "char"
   *  parameter set to the passed in value of loc.
   */
  public final Anchor newTextAnchor (String loc) {
    return newTextAnchor("text-point", loc);
  }

  /**
   * @param anchorType
   * @param loc
   * @return a new Anchor of type <code>anchorType</code> with the "char"
   *         parameter set to the passed in value of <code>loc</code>
   */
  public final Anchor newTextAnchor (String anchorType, String loc) {
      return newTextAnchor(anchorType, loc, theSignal);
  }

  /**
   * @param anchorType
   * @param loc
   * @param theSignal Signal against which to define the new anchor
   * @return a new Anchor of type <code>anchorType</code> with the "char"
   *          parameter set to the passed in value of <code>loc</code>, and
   *          the signal reference set to <code>theSignal</code>
   */
  public final Anchor newTextAnchor (String anchorType, String loc, Signal theSignal) {
    Anchor textAnchor =
      theFactory.createAnchor(anchorType, theCorpus, theSignal);
    textAnchor.setValueOfParameterWithRole(loc, "char");
    return textAnchor;
  }

  public final Region newEmptyRegion (RegionType rtype) {
    Id regionId = theFactory.createNewIdFor(rtype);
    return theFactory.createEmptyRegion(rtype, theCorpus, regionId);
  }


  /**
   *  Adds subAnnotations to the referenced region's subordinate
   *  sets.  The AnnotationType of each Annotation determines the
   *  appropriate subordinate set.
   */
  public final void addSetSubAnnotations(RegionRef region,
					 Annotation[] setSubAnnotations) {
    for (int i=0; i<setSubAnnotations.length; i++) {
      region.addAnnotation(setSubAnnotations[i]);
    }
  }

  /**
   *  Creates an array of AnnotationRef's from a pair of
   *  corresponding arrays of annotations and their roles.
   */
  public final AnnotationRef[] roleIdentifiedAnnotations(Annotation[] annots,
							 String[] roles)
    throws IllegalArgumentException {
    // check array parameters
    int numRoles = roles.length;
    if (annots.length != numRoles) {
      throw new IllegalArgumentException("Sizes of the roles and annots arrays must be the same");
    }

    AnnotationRef[] roleSubAnnotations = new AnnotationRef[numRoles];
    for (int i=0; i<numRoles; i++) {
      roleSubAnnotations[i] =
	ATLASRef.createAnnotationRef(annots[i], roles[i]);
      if (DEBUG > 0) {
        System.out.println("JAWB.ria: \n\tAnnotation: "+annots[i].toString());
        System.out.println("\tRole: " + roles[i]);
        System.out.println("\tnew subAnnotation reference: " +
                           roleSubAnnotations[i].toString());
      }
    }

    return roleSubAnnotations;
  }

  /**
   *  Removes an Annotation from its corpus and Analysis.
   *  This automatically removes the included content.
   *  When the ATLAS removal takes place, the region subordinates are
   *  left in case they are used elsewhere (this is ATLAS policy), but
   *  the Region and any Region subordinates it has are explicitly
   *  deleted here, as we don't reuse Regions.
   *
   *  If this Annotation is referred to by any other elements, it will
   *  not be removed and this method will return false.  This method
   *  will also return false if for any reason
   *  Analysis.removeAnnotation() fails on this Annotation.
   *
   *  when this returns false, it is unclear whether the failure was
   *  caused by failure to delete the Annotation from the Analysis, or
   *  failure to remove some subordinate Region, or failure to
   *  remove this element from a containing subordinateSet
   */
  public final boolean removeAnnotation(AWBAnnotation annot) {
    if (DEBUG > 0)
      System.err.println("JAWB.remAnn: " + (annot==null?null:annot.getAnnotationType().getName()+":"+annot.getId()));
    if (DEBUG > 2)
      Thread.dumpStack ();


    Analysis analysis = (Analysis)annot.getParent();
    // check first to see whether this Annotation is referred to by
    // any other elements -- if so, return false and do not remove it
    Set references = annot.getReferentElements();
    if (references.size() == 0) {
      // get a set of all subordinate Regions (direct and recursive)
      Iterator regionsIter = annot.getAllRecursiveRegions().iterator();
      // delete the actual Annotation
      boolean success = removeAnnotationAndReferences(annot, analysis);
      if (success) {
      // remove all subordinate Regions
	while (regionsIter.hasNext()) {
	  Region r = (Region)regionsIter.next();
          if (DEBUG > 0)
            System.out.println("JAWB.remAnn: Removing Region: "+r.asAIFString());
	  success &= removeRegionAndReferences(r, theCorpus);
	}
      } else {
	System.out.println("JAWB.remAnn: Annotation removal failed: "+annot);
      }
      return success;
    } else {
      // FIXME -- must first determine if the referents have role-identified
      // references to this element, or if it's just in a subordinate set;
      // if the latter, removal is still allowed
      if (DEBUG > 0)
        System.out.println("JAWB.remAnn: Annotation has referents");
      Iterator refIter = references.iterator();
      HashSet nonRoleRefs = new HashSet();
      while (refIter.hasNext()) {
	IdentifiableATLASElement referent = (IdentifiableATLASElement)refIter.next();
        if (DEBUG > 0)
          System.out.println("JAWB.remAnn: Referent: "+referent.asAIFString());
	ATLASType refType = referent.getATLASType();
	Iterator rolesIter = refType.getDefinedRolesForSubordinates();
	while (rolesIter.hasNext()) {
	  String role = (String)rolesIter.next();
	  IdentifiableATLASElement roleSub = (IdentifiableATLASElement) referent.getSubordinateWithRole(role);
          if (DEBUG > 0)
            System.out.println("\tRole: " + role + " = " + roleSub);
	  if (roleSub != null && roleSub.equals(annot)) {
	    // annotation has role-identified reference(s) to it
	    System.out.println("JAWB.remAnn: Error:  Annotation "+roleSub.getATLASType().getName()+":"+roleSub.getId()+" has role-identified referents "+referent.getATLASType().getName()+":"+referent.getId()+" -- not deleting");
	    return false;
	  }
	}
	// this referent did not have a role-identified reference to
	// this annotation -- keep track of it in case we delete the
	// annotation and need to remove it from the subordinate set
	// of this referent
	if (DEBUG > 0 && referent == null) {
	  System.err.println("JAWB.remAnn: " +
			     "adding null referent to nonRoleRefs!!!!");
	}
	nonRoleRefs.add(referent);
      }
      // all referents have been checked, and none had role-identified
      // references to this annotation -- go ahead and delete it and remove
      // it from the subordinate sets of the nonRoleRefs

      // get a set of all subordinate Regions (direct and recursive)
      Iterator regionsIter = annot.getAllRecursiveRegions().iterator();
      // delete the actual Annotation
      boolean success = removeAnnotationAndReferences(annot, analysis);
      if (DEBUG > 0) {
	System.err.println("JAWB.remAnn: Annotation removed: " + success +
			   " annot is now: " + annot);
      }
      if (success) {
	// remove all subordinate Regions
	while (regionsIter.hasNext()) {
	  Region r = (Region)regionsIter.next();
	  //	  System.out.println("Removing Region: " + r.asAIFString());
	  success &= removeRegionAndReferences(r, theCorpus);
	}
	// remove from subordinate sets of containing elements
	Iterator setsIter = nonRoleRefs.iterator();
	while (setsIter.hasNext()) {
	  if (DEBUG > 0 && annot == null) {
	    System.err.println("JAWB.remAnn: " +
			       "remove from subordinate set: annot is null!!");
	  }
	  success &=
	    ((ATLASElement)setsIter.next()).removeFromSubordinateSet(annot);
	}
      } else {
	System.out.println("JAWB.remAnn: Annotation removal failed: "+annot);
      }
      return success;
    }
  }

  /**
   * Takes an Annotation and its parent Analysis, and removes the
   * Annotation from the Analysis, and removes the Annotation from its
   * (primary) Region's list of referent elements.
   *
   * ATLAS should do this, but for some reason, when an element is
   * deleted, it isn't removed from the sets of referrent elements its
   * subordinates keep.  So this method is a HACK to do that until
   * ATLAS comes around.
   *
   * This should only be called once we have determined that it is safe
   * to delete the element (has no referents).
   *
   * for now, the ATLASElement must be an AWBAnnotation or a Region
   */
  public final boolean removeAnnotationAndReferences(AWBAnnotation annot,
						     Analysis parent) {
    // An Annotation can only have a Content, which is not reusable
    // and thus not an issue, and a Region.  So we just have to deal
    // with the region
    Region r = annot.getRegion();
    boolean success = parent.removeAnnotation(annot);
    if (success) {
      // if this fails, what should we do?  FIXME
      r.removeReferentElement(annot);
    }
    return success;
  }

  /**
   * Takes a Region and its parent Corpus and deletes the Region from
   * the corpus, after first getting a list of all subordinates with a
   * referentElement reference to the element to be deleted.  If
   * deletion succeeds, the deleted element is removed from the
   * referent elements sets of all the subordinates.
   *
   * ATLAS should do this, but for some reason, when an element is
   * deleted, it isn't removed from the sets of referrent elements its
   * subordinates keep.  So this method is a HACK to do that until
   * ATLAS comes around.
   *
   * This should only be called once we have determined that it is
   * safe to delete the element (has no referents).
   */
  public final boolean removeRegionAndReferences(Region reg, Corpus c) {
    // A Region can have all kinds of subordinates:
    // Anchors, Annotations (both role-id'd and in sets), other Regions
    Iterator anchorIter = reg.getAllAnchors().iterator();
    Iterator annotIter = reg.getAllAnnotations().iterator();
    Iterator regIter = reg.getAllRegions().iterator();
    boolean success = c.removeRegion(reg);
    if (success) {
      // if this fails, what should we do?  FIXME
      while (anchorIter.hasNext()) {
	((ReusableATLASElement)anchorIter.next()).removeReferentElement(reg);
      }
      while (annotIter.hasNext()) {
	((ReusableATLASElement)annotIter.next()).removeReferentElement(reg);
      }
      while (regIter.hasNext()) {
	((ReusableATLASElement)regIter.next()).removeReferentElement(reg);
      }
    }
    return success;
  }

  public final ATLASImplementation getImplementation() {
    return theImplementation;
  }

  public final Corpus getCorpus() {
    return theCorpus;
  }

  public final Task getTask() {
    return theTask;
  }

  protected final ATLASElementFactory getFactory() {
    return theFactory;
  }

  public final AWBSimpleSignal getSignal() {
    return theSignal;
  }

  public final void saveAsAIF(OutputStream os) {
      // first check the Anchors and see if they have referents;
      // remove those that don't
      Iterator anchorIter = theCorpus.getAllAnchors().iterator();
      while (anchorIter.hasNext()) {
	  Anchor anchor = (Anchor)anchorIter.next();
	  if (anchor.getReferentElements().size() == 0) {
	      theCorpus.removeAnchor(anchor);
	  }
      }
      // now actually save out the corpus
      AIFXMLExport export = new AIFXMLExport();
      if (DEBUG > 0)
	  System.out.println("JAWB.saveAsAIF: export = " + export.toString());
      export.save(theCorpus, os);
  }

  public final void saveAsAIF(URL aifURL) {
    // first check the Anchors and see if they have referents;
    // remove those that don't
    if (DEBUG > 0)
      System.out.println("JAWB.saveAsAIF: aifURL = " + aifURL.toString());
    Iterator anchorIter = theCorpus.getAllAnchors().iterator();
    while (anchorIter.hasNext()) {
      Anchor anchor = (Anchor)anchorIter.next();
      if (anchor.getReferentElements().size() == 0) {
	theCorpus.removeAnchor(anchor);
      }
    }
    // now actually save out the corpus
    AIFXMLExport export = new AIFXMLExport(aifURL);
    if (DEBUG > 0)
      System.out.println("JAWB.saveAsAIF: export = " + export.toString());
    export.save(theCorpus);
  }

  public final Analysis getAnalysisFor(AnnotationType type) {
    return theCorpus.getAnalysisWithRole((String)tsd.get(type));
  }


}

