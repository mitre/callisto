
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
import gov.nist.atlas.type.AnnotationType;

import java.util.Set;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public interface AWBAnnotation extends Annotation
{

  /// HACK

  void setJawb(JAWB jawb);
  JAWB getJawb();

  /// end HACK

    /**
     * Provides a list of the attributes an Annotation supports
     * This will include attributes relating to content and to region
     */
    String[] getAttributeKeys ();

    /**
     * Returns a java Class object specifying the type of data an 
     * attribute contains. 
     */
    Class getAttributeType (String attributeKey);

    /**
     * Returns the value for the attribute specified. The returned
     * object can be safely cast to the class Type returned by the
     * 'getAttributeType?' method. Requests for the value of
     * unsupported keys will get a return value of null. Some keys may
     * return null as their value, so it's up to the widget to be sure
     * the key exists.
    */
    Object getAttributeValue (String attributeKey);

    /**
     * Changes the value of the specified attribute to the specified value
     * Returns true if the change is successful, and false otherwise.
     *
     * We might eventually wish to provide a means of checking if the
     * value is modifiable before trying to set it, and then changing
     * this to a void return value with the possibility of throwing
     * exceptions for unmodifiableAttributes and/or incorrectValueType 
     * and/or disallowedValue (see below)
     */
    boolean setAttributeValue (String attributeKey, Object value)
	throws UnmodifiableAttributeException;

    /**
     * hmm, this doesn't really seem to belong here....  it is
     * provided to allow subClasses of the basic AWBAnnotationImpl
     * implementation of this interface to add subclass-specific
     * region keys.  This seems to be the best argument in favor of
     * doing away with this implementation level all together, since
     * we do expect that all implementation of this iterface will do
     * so by subclassing AWBAnnotationImpl anyhow.... FIXME
     */
    void addRegionKey(String key);


  /**
   * Returns a Set of all Annotation subordinates in this Annotation's
   * Region, or subRegions thereof that do not have any other
   * super-Annotations.
   */
  Set getExclusiveSubAnnotations();

  /** Returns the first annotation it finds of type superType that has 
   *  this annotation as a subordinate, or null if it does not find one
   */
  public AWBAnnotation getSuperAnnotation(AnnotationType superType);

  /**
   * Returns a Set of all Regions that are subElements of this Annotation,
   * either directly or recursively as subRegions of this Annotation's Region.
   */
  Set getAllRecursiveRegions();

    /**
     * Name: addPropertyChangeListener
     * Purpose: Basic support for "something changed here"
     */     
    public void addPropertyChangeListener(PropertyChangeListener l);
    
    /**
     * Name: removePropertyChangeListener
     * Purpose: Makes a PropertyChangeListener goes poof
     * @param l Listener to remove from this annotation
     */    
    public void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * Name: removeAllPropertyChangeListeners
     * Purpose: Removes all the LropertyChangeListeners on this annotation
     */
    public void removeAllPropertyChangeListeners();

    /**
     * Name: firePropertyChange
     * Purpose: Subclasses notify by calling this
     * @param e Event to fire
     */    
    public void firePropertyChange(PropertyChangeEvent e);
}
