
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

import gov.nist.atlas.util.ATLASElementSet;
import gov.nist.atlas.Anchor;
import gov.nist.atlas.Region;
import gov.nist.atlas.Annotation;
import gov.nist.atlas.Analysis;
import gov.nist.atlas.type.AnnotationType;

import org.mitre.jawb.atlas.AnnotationModelEvent.EventType;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * AnnotationModel is the focal point for View/Controller interaction with
 * ATLAS annotations controlled through Callisto.<p>
 *
 * NOTE: This event model does not send notification of changes made directly
 * through ATLAS method calls. Creation and deletion must be done from the
 * AnnotationModel, and modification of Annotations must be done using the
 * methds available from AWBAnnotation and below. This is expected to change
 * in a future version.
 */
public interface AnnotationModel {
  /*
   * TODO: is this a better place for things about the 'annotations' than the
   * Task, such as 'getAnnotationType (String)' etc? ie. in the case were some
   * component wants information about annotation set, shouldn't all that
   * information be available from here? Is it possible to even remove that
   * stuff from Task though since it's there simply because it's reqired just
   * to open the Corpus in the first place?
   */
  
  /**********************************************************/
  /*                  Accessor Methods                      */
  /**********************************************************/
  
  /**
   * Returns all the annotations for a given document. When iteratoring
   * through this, be sure to cast to the right thing. All should be subtypes
   * of AWBAnnotation.
   *
   * @return Iterator of Annotation objects
   */    
  public Iterator getAllAnnotations();

  /**
   * Allows access to the Signal of this document.
   */
  public AWBSimpleSignal getSignal ();

  
  /***********************************************************************/
  /*                   Annotation Model Control                          */
  /***********************************************************************/

  
  /*
   * *sigh* I was going to have these here, but I'm currently thinking that
   * the model shouldn't provide controler access, since it shouldn't deal
   * with gui code, and deletion may need to 'pop up' user prompts. Still
   * under debate, but right now, the document implements create/delete
   * methods and gui code that needs to pop up should be there. The
   * AnnotationModel implementation (AWBDocument) also has these methods for
   * the actual modification of the model, and it is there that the events are
   * fired from.
   */

  
  /**
   * Create a new text-based annotation and fires an event to all the
   * observers of this AnnotationModel.
   *
   * @param type type of anontation to create
   * @param start starting offset of annotation
   * @param end ending offset of annotation
   * @param mainExtentRole this should be null unless
   *                       creating a mention annotation
   * @return a new annotation
  public AWBAnnotation createAnnotation(AnnotationType type,
                                        int start, int end,
                                        String mainExtentRole);
   */
    
  /**
   * Creates an empty annotation.
   * @param type type of annotation to create
   * @return the new annotation
  public AWBAnnotation createAnnotation (AnnotationType type);
   */

  /**
   * Delete an anontation from the corpus
   * @param AWBAnnotation an annotation to remove
  public boolean deleteAnnotation (AWBAnnotation annot);
   */
  
  
  /***********************************************************************/
  /*                         Event Propogation                           */
  /***********************************************************************/

  /** Add an observer to this model. */
  public void addAnnotationModelListener (AnnotationModelListener l);

  /**
   * Remove an observer from this model
   * @see #addAnnotationModelListener
   */
  public void removeAnnotationModelListener (AnnotationModelListener l);
}
