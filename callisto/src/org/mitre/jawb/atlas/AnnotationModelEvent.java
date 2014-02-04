
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

/**
 * Interface for AnnotationModel change notifications. Provides detailed
 * information to AnnotationModel observers about how the model has
 * changed.<p>
 *
 * Directly provides for high level information such as type of change,
 * and <code>AnnotaionModel</code> where change was made, and the annotation
 * it occured on.  Indirectly provides for the more detailed structural changes
 * to an annotation using the {@link AnnotationChange} interface.<p>
 *
 * Implemented as an interface so that instances may subclass UndoableEdit for
 * added functionality.
 */
public interface AnnotationModelEvent {
  
  /**
   * Defines the <code>CREATE</code>, <code>DELETE</code>,
   * <code>CHANGE</code>, <code>REMOVE</code> and <code>INSERT</code> event
   * types, with their string representation returned from
   * <code>toString()</code>
   */
  public static final class EventType {
    /** Create type. */
    public static final EventType CREATE = new EventType ("Create");
    /** Delete type. */
    public static final EventType DELETE = new EventType ("Delete");
    /** Change type. */
    public static final EventType CHANGE = new EventType ("Change");
    /** Insert type. */
    public static final EventType INSERT = new EventType ("Insert");
    /** Remove type. */
    public static final EventType REMOVE = new EventType ("Remove");

    private String type;
    /** Private for safety! */
    private EventType (String type) { this.type = type; }
    
    /** Convert the type to a string. */
    public String toString () { return type; }
  }

  /**
   * Interface to encapsulate detailed changes to an annotation.
   *
   * If the <code>AnnotationModelEvent</code> was of type <code>CHANGE</code>,
   * getPropertyName() will the name of the property which changed, if known,
   * and getOldValue and getNewValue will return the appropriate objects.  An
   * Annotation may send a null object as the name to indicate that an
   * arbitrary set of if its properties have changed. In this case the old and
   * new values should also be null as well (see {@link
   * java.beans.PropertyChangeEvent}). For all other type events, name, old,
   * and new values will all be null.<p>
   *
   * If the <code>AnnotationModelEvent</code> was of type <code>REMOVE</code>
   * or <code>INSERT</code>, getAnnotationAdded and getAnnotationRemoved will
   * return the inserted or removed annotaions respectively.
   */
  public static interface AnnotationChange {
    /**
     * Returns the name of the property/attribute that changed if the
     * ModelEvent was of type "Change" and there was only a single attribute
     * changed. If this returns null, getOldValue and getNewValue should be *
     * expected to return null as well.
     */
    public String getPropertyName ();
    
    /** Returns the New value if the ModelEvent was of type "Change" */
    public Object getNewValue ();
    
    /** Returns the Old value if the ModelEvent was of type "Change" */
    public Object getOldValue ();
    
    /** Return the annotation this event concerns. */
    public AWBAnnotation getAnnotation ();

    /**
     * Get a list of sub-annotations that were inserted. This needn't include
     * sub-sub-annotations, only those directly added to the concerned
     * annotations.
     */
    public AWBAnnotation[] getAnnotationsInserted ();
    
    /**
     * Get a list of sub-annotations that were removed. This needn't include
     * sub-sub-annotations, only those directly removed from the concerned
     * annotation.
     */
    public AWBAnnotation[] getAnnotationsRemoved ();
  }

  /**
   * Returns the change information for the given Annotation. If this event is
   * of type <code>CREATE</code>, or <code>DELETE</code>, <code>null</code> is
   * returned.
   */
  public AnnotationChange getChange ();

  /** Return the type of this event. */
  public EventType getType ();

  /** Returns the AnnotationModel that sourced the change event. */
  public AnnotationModel getModel ();
  
  /** Return the annotation this event concerns. */
  public AWBAnnotation getAnnotation ();
}

