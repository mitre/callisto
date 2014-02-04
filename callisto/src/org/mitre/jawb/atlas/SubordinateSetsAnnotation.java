
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

import gov.nist.atlas.*;
import gov.nist.atlas.spi.ImplementationDelegate;
import gov.nist.atlas.impl.AnnotationInitializer;
import gov.nist.atlas.impl.AnnotationTypeImpl;
import gov.nist.atlas.type.ATLASType;
import gov.nist.atlas.type.AnnotationType;
import gov.nist.atlas.ref.ATLASRef;
import gov.nist.atlas.ref.AnnotationRef;
import gov.nist.atlas.util.ATLASElementSet;

import java.io.FileReader;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

/**
 *  This implementation is appropriate for tags without a text-extent
 *  region that have one or more subordinate sets, as specified by a
 *  region definition like this:
 *
 * <code>
 *  &lt;RegionType name='ace_mentions'&gt;
 *    &lt;AnnotationType ref='ace_mention' hasIndefiniteCardinality='true'/&gt;
 *    &lt;AnnotationType ref='ace_reltime' hasIndefiniteCardinality='true'/&gt;
 *  &lt;/RegionType&gt;
 * </code>
 */
public class SubordinateSetsAnnotation extends AWBAnnotationImpl 
    implements HasSubordinates{

  private static final int DEBUG = 0;


    /* a set of the different AnnotationTypes of the subordinates */
    protected HashSet subordinateTypes;

    protected SubordinateSetsAnnotation(ATLASType type, ATLASElement parent, 
				Id id, ImplementationDelegate delegate, 
				AnnotationInitializer initializer) {
	super(type, parent, id, delegate, initializer);
    }

    // Because HasSubordinates operates on the Attribute Keys level as 
    // well as the Interfaces level, we don't need to add subtag-set-specific
    // attribute keys

    // extend initialization to create a set of the Subordinate set roles
    protected void initialize() {
	super.initialize();
	/* now initialize subordinate set types */
	ATLASElementSet subAnnotations = 
	    getRegion().getAllSubordinatesInSubordinateSetsWith(ATLASClass.ANNOTATION);
	Iterator annotIter = subAnnotations.iterator();
	subordinateTypes = new HashSet(subAnnotations.size());
	while (annotIter.hasNext()) {
	    subordinateTypes.add((AnnotationType)((Annotation)(annotIter.next())).getATLASType());
	}
    }

    /* The following methods implement the HasSubordinates Interface */


    /**
     * Returns a list of keys for subordinates of an annotation. 
     */
    public AnnotationType[] getSubordinateTypes () {
	// have to use the AnnotationTypeImpl because
	// AnnotationType is not a subclass of Object!
	// (which is needed for toArray())
	AnnotationTypeImpl[] allTypes = 
	    new AnnotationTypeImpl[subordinateTypes.size()];
	subordinateTypes.toArray(allTypes);
	return allTypes;
    }

  /** 
   * Returns the array of subordinates of the specified type. 
   */
  public AWBAnnotation[] getSubordinates (AnnotationType subType) {
    // System.out.println("getSubordinates of type " + subType.getName());
    ATLASElementSet sub =
      getRegion().getSubordinateSet(subType);
    AWBAnnotation[] subArray = 
      new AWBAnnotation[sub.size()];
    Iterator subIter = sub.iterator();
    int i=0;
    while (subIter.hasNext()) {
      Object nextObject = subIter.next();
      if (nextObject instanceof AnnotationRef) {
	subArray[i++] = 
	  (AWBAnnotation)((AnnotationRef)nextObject).getElement();
      } else {
	subArray[i++] = (AWBAnnotation)nextObject;
      }
    }
    return subArray;
  }

    /**
     * Used to add multiple subordinates at once.
     * 
     * might need to throw an exception if this addition will cause
     * the number of subordinates in the set to exceed the maximum
     * allowed
     *
     * might also need to throw an exception if the subordinates
     * specified are of the wrong type
     *
     * returns true if all subordinates were added successfully; 
     * false otherwise
     *
     */
    public boolean addSubordinates (AWBAnnotation[] subords) {
        int i;
	boolean retVal = true;
        Region region = getRegion ();
        // break on the first failure so we know which were added, directly
        // adding rather than calling addSubordinate, to limit events.
	for (i=0; i<subords.length && retVal; i++) {
          retVal = region.addToSubordinateSet(subords[i]);
	}
        if (i > 0 && retVal) {       // complete success!
          fireSubChange(subords, true);
        
        } else if (i > 1) {          // partial success
          AWBAnnotation[] subset = new AWBAnnotation[i-1];
          System.arraycopy (subords, 0, subset, 0, i-1);
          fireSubChange (subset, true);
        }
	return retVal;
    }

    /**
     * Add a single subordinate to the set of the appropriate type.
     * Automatically gets the correct AnnotationType from the
     * AWBAnnotation passed in, so we don't have to explicitly specify
     * this.
     *
     * might need to throw the same exceptions as addSubordinates
     */
    public boolean addSubordinate (AWBAnnotation subord) {
      if (DEBUG > 1)
        System.err.println("SubSetsAnnot.addSubordinate adding sub of type " +
                           subord.getAnnotationType() + " to region of type "
                           + getRegion().getRegionType());
      boolean retVal = getRegion().addToSubordinateSet(subord);
      if (retVal)
        fireSubChange(new AWBAnnotation[] { subord }, true);
      return retVal;
    }

    /**
     * Remove all of the subordinates provided from the set of the
     * appropriate type.
     *
     * Returns true if all passed Annotations were successfully removed;
     * false otherwise.
     */
    public boolean removeSubordinates (AWBAnnotation[] subords) {
        int i;
        boolean retVal = true;
        Region region = getRegion ();
        // break on the first failure so we know which were removed, directly
        // removing, rather than calling removeSubordinate, to limit events.
	for (i=0; i<subords.length && retVal; i++) {
	    retVal = region.removeFromSubordinateSet(subords[i]);
	}
        if (i > 0 && retVal) {       // complete success!
          fireSubChange (subords, false);
        
        } else if (i > 1) {          // partial success
          AWBAnnotation[] subset = new AWBAnnotation[i-1];
          System.arraycopy (subords, 0, subset, 0, i-1);
          fireSubChange (subset, false);
        }
	return retVal;
    }	

    /**
     * Remove the provided subordinate from the set of the appropriate type.
     *
     * Returns true if successful; false otherwise.
     */
    public boolean removeSubordinate (AWBAnnotation subord) {
        boolean retVal = getRegion().removeFromSubordinateSet(subord);
        if (retVal)
          fireSubChange (new AWBAnnotation[] { subord }, false);
	return retVal;
    }

    /**
     *  Remove all subordinates from the set of the appropriate type.
     *
     *  Returns true if successful; false otherwise.
     */
    public boolean clearSubordinates (AnnotationType subType) {
        return removeSubordinates (getSubordinates (subType));
    }

    /**
     * Convience method to keep code clean
     */
    private final void fireSubChange (AWBAnnotation[] subs, boolean insert) {
        firePropertyChange(new SubAnnotChange(this, subs, insert));
    }
}

