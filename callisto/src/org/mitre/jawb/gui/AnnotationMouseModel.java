
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

package org.mitre.jawb.gui;

import java.util.Collections;
import java.util.Vector;
import java.util.List;
import java.util.Arrays;
import java.awt.event.MouseEvent;

import org.mitre.jawb.atlas.AWBAnnotation;

/** 
 * AnnotationMouseModel is the focal point for Views/Control of mouse events
 * on Annotations displayed in Callisto widgets.  
 *
 * This will be instantiated in the JawbDocument.  Widgets that want to
 * respond to mouse events should implement AnnotationMouseListeners.
 * All Widgets' MouseListeners should fire AnnotationMouseEvent's for
 * each MouseEvent that affects Annotation(s).
 */


public class AnnotationMouseModel {

  private static final int DEBUG = 0;
  
  private Vector listeners = new Vector (5);
  /**
   * Reusable array for safe listener iteration so that if one decides to
   * remove itself after recieving an event from us, we won't get a concurrent
   * modification exception.
   */
  private AnnotationMouseListener[] array = new AnnotationMouseListener[0];


  /***********************************************************************/
  /*                         Event Propogation                           */
  /***********************************************************************/

  /** Add an observer to this model. */
  public void addAnnotationMouseListener (AnnotationMouseListener l) {
    if (! (l == null || listeners.contains(l))) {
      if (DEBUG > 3)
	System.err.println("AnnotMouseModel.addAML adding: " + l);
      listeners.add(l);
    }
  }

  /**
   * Remove an observer from this model
   * @see #addAnnotationMouseListener
   */
  public void removeAnnotationMouseListener (AnnotationMouseListener l) {
    listeners.remove(l);
    // null out the array since it will need to be smaller next time
    Arrays.fill(array,null);
  }

  /**
   * Creates an AnnotationMouseEvent and reports it to all registered
   * listeners.
   */
  public void fireAnnotationClickedEvent (MouseEvent e, AWBAnnotation annot,
					JawbComponent component) {
    AnnotationMouseEvent ame = 
      new AnnotationMouseEvent(e,annot,component,true);
    array = (AnnotationMouseListener[]) listeners.toArray(array);

    if (DEBUG > 1)
      System.err.println("AnnotMouseModel.fireClick to " + 
			 (array.length+1) + "listeners");
    for (int i=0; i<array.length && array[i] != null; i++) {
      array[i].mouseClicked(ame);
    }
  }

  /**
   * Creates an AnnotationMouseEvent and reports it to all registered
   * listeners.
   */
  public void fireAnnotationEnteredEvent (MouseEvent e, AWBAnnotation annot,
					JawbComponent component) {
    AnnotationMouseEvent ame = 
      new AnnotationMouseEvent(e,annot,component,true);
    array = (AnnotationMouseListener[]) listeners.toArray(array);

    for (int i=0; i<array.length && array[i] != null; i++) {
      array[i].mouseEntered(ame);
    }
  }

  /**
   * Creates an AnnotationMouseEvent and reports it to all registered
   * listeners.
   */
  public void fireAnnotationExitedEvent (MouseEvent e, AWBAnnotation annot,
					JawbComponent component) {
    AnnotationMouseEvent ame = 
      new AnnotationMouseEvent(e,annot,component,true);
    array = (AnnotationMouseListener[]) listeners.toArray(array);

    for (int i=0; i<array.length && array[i] != null; i++) {
      array[i].mouseExited(ame);
    }
  }

  /**
   * Creates an AnnotationMouseEvent and reports it to all registered
   * listeners.
   */
  public void fireAnnotationPressedEvent (MouseEvent e, AWBAnnotation annot,
					JawbComponent component) {
    AnnotationMouseEvent ame = 
      new AnnotationMouseEvent(e,annot,component,true);
    array = (AnnotationMouseListener[]) listeners.toArray(array);

    for (int i=0; i<array.length && array[i] != null; i++) {
      array[i].mousePressed(ame);
    }
  }

  /**
   * Creates an AnnotationMouseEvent and reports it to all registered
   * listeners.
   */
  public void fireAnnotationReleasedEvent (MouseEvent e, AWBAnnotation annot,
					JawbComponent component) {
    AnnotationMouseEvent ame = 
      new AnnotationMouseEvent(e,annot,component,true);
    array = (AnnotationMouseListener[]) listeners.toArray(array);

    for (int i=0; i<array.length && array[i] != null; i++) {
      array[i].mouseReleased(ame);
    }
  }

  /**
   * Creates an AnnotationMouseEvent and reports it to all registered
   * listeners.
   */
  public void fireAnnotationMotionEvent (MouseEvent e, AWBAnnotation annot,
					JawbComponent component) {
    AnnotationMouseEvent ame = 
      new AnnotationMouseEvent(e,annot,component,true);
    array = (AnnotationMouseListener[]) listeners.toArray(array);

    for (int i=0; i<array.length && array[i] != null; i++) {
      array[i].mouseMotion(ame);
    }
  }


  public void fireAnnotationClickedEvents (MouseEvent e, List annots,
					   JawbComponent component) {
    fireAnnotationClickedEvents(e, annots, component, null);
  }


  /**
   * Report a mouse event impacting multiple annotations to all
   * registered listeners.
   *
   * This fires multiple AnnotationMouseEvent's, one for each
   * AWBAnnotation in annots, in the order that the annots are listed
   * in the List.
   */
  public void fireAnnotationClickedEvents (MouseEvent e, List annots,
					   JawbComponent component,
					   AnnotationMouseListener def) {

    AnnotationMouseEvent ame = null;
    array = (AnnotationMouseListener[]) listeners.toArray(array);

    if (DEBUG > 0) 
      System.err.println ("AnnotMouseModel.fireAnnotationClickedEvents for " +
			  annots.size() + " annots to " +
			  array.length + " listeners with " +
			  (def == null ? "no" : "one") + " default");

    for (int i=0; i<annots.size()-1; i++) {
      ame = new AnnotationMouseEvent(e, (AWBAnnotation)annots.get(i), 
				     component, false);
      for (int j=0; j<array.length && array[j] != null; j++) {
	array[j].mouseClicked(ame);
      }
      if (def != null && !ame.isConsumed()) {
	if (DEBUG > 1) 
	  System.err.println ("fireAnnotationClickedEvents sending annot# " + 
			      i + " to default listener");
	def.mouseClicked(ame);
      }
    }

    ame = new AnnotationMouseEvent(e, 
				   (AWBAnnotation)annots.get(annots.size()-1), 
				   component, true);
    for (int j=0; j<array.length && array[j] != null; j++) {
      if (DEBUG > 3)
	System.err.println("fireAnnotationClickedEvents sending annot# " +
			   (annots.size()-1) + " to: " + array[j]);
      array[j].mouseClicked(ame);
    }
    if (def != null && !ame.isConsumed()) {
      if (DEBUG > 1)
	System.err.println ("fireAnnotationClickedEvents sending annot# " +
			    (annots.size()-1) + " to default listener");
      def.mouseClicked(ame);
    }
  }
  /**
   * Report a mouse event impacting multiple annotations to all
   * registered listeners.
   *
   * This fires multiple AnnotationMouseEvent's, one for each
   * AWBAnnotation in annots, in the order that the annots are listed
   * in the List.
   */
  public void fireAnnotationEnteredEvents (MouseEvent e, List annots,
					 JawbComponent component) {

    AnnotationMouseEvent ame = null;
    array = (AnnotationMouseListener[]) listeners.toArray(array);

    for (int i=0; i<annots.size()-1; i++) {
      ame = new AnnotationMouseEvent(e, (AWBAnnotation)annots.get(i), 
				     component, false);
      for (int j=0; j<array.length && array[j] != null; j++) {
	array[j].mouseEntered(ame);
      }
    }
    ame = new AnnotationMouseEvent(e, 
				   (AWBAnnotation)annots.get(annots.size()-1), 
				   component, true);
    for (int j=0; j<array.length && array[j] != null; j++) {
      array[j].mouseEntered(ame);
    }
  }
  /**
   * Report a mouse event impacting multiple annotations to all
   * registered listeners.
   *
   * This fires multiple AnnotationMouseEvent's, one for each
   * AWBAnnotation in annots, in the order that the annots are listed
   * in the List.
   */
  public void fireAnnotationExitedEvents (MouseEvent e, List annots,
					 JawbComponent component) {

    AnnotationMouseEvent ame = null;
    array = (AnnotationMouseListener[]) listeners.toArray(array);

    for (int i=0; i<annots.size()-1; i++) {
      ame = new AnnotationMouseEvent(e, (AWBAnnotation)annots.get(i), 
				     component, false);
      for (int j=0; j<array.length && array[j] != null; j++) {
	array[j].mouseExited(ame);
      }
    }
    ame = new AnnotationMouseEvent(e, 
				   (AWBAnnotation)annots.get(annots.size()-1), 
				   component, true);
    for (int j=0; j<array.length && array[j] != null; j++) {
      array[j].mouseExited(ame);
    }
  }
 
  /**
   * Report a mouse event impacting multiple annotations to all
   * registered listeners.
   *
   * This fires multiple AnnotationMouseEvent's, one for each
   * AWBAnnotation in annots, in the order that the annots are listed
   * in the List.
   */
  public void fireAnnotationMotionEvents (MouseEvent e, List annots,
					 JawbComponent component) {

    AnnotationMouseEvent ame = null;
    array = (AnnotationMouseListener[]) listeners.toArray(array);

    for (int i=0; i<annots.size()-1; i++) {
      ame = new AnnotationMouseEvent(e, (AWBAnnotation)annots.get(i), 
				     component, false);
      for (int j=0; j<array.length && array[j] != null; j++) {
	array[j].mouseMotion(ame);
      }
    }
    ame = new AnnotationMouseEvent(e, 
				   (AWBAnnotation)annots.get(annots.size()-1), 
				   component, true);
    for (int j=0; j<array.length && array[j] != null; j++) {
      array[j].mouseMotion(ame);
    }
  }
  /**
   * Report a mouse event impacting multiple annotations to all
   * registered listeners.
   *
   * This fires multiple AnnotationMouseEvent's, one for each
   * AWBAnnotation in annots, in the order that the annots are listed
   * in the List.
   */
  public void fireAnnotationPressedEvents (MouseEvent e, List annots,
					 JawbComponent component) {

    AnnotationMouseEvent ame = null;
    array = (AnnotationMouseListener[]) listeners.toArray(array);

    for (int i=0; i<annots.size()-1; i++) {
      ame = new AnnotationMouseEvent(e, (AWBAnnotation)annots.get(i), 
				     component, false);
      for (int j=0; j<array.length && array[j] != null; j++) {
	array[j].mousePressed(ame);
      }
    }
    ame = new AnnotationMouseEvent(e, 
				   (AWBAnnotation)annots.get(annots.size()-1), 
				   component, true);
    for (int j=0; j<array.length && array[j] != null; j++) {
      array[j].mousePressed(ame);
    }
  }
  /**
   * Report a mouse event impacting multiple annotations to all
   * registered listeners.
   *
   * This fires multiple AnnotationMouseEvent's, one for each
   * AWBAnnotation in annots, in the order that the annots are listed
   * in the List.
   */
  public void fireAnnotationReleasedEvents (MouseEvent e, List annots,
					 JawbComponent component) {

    AnnotationMouseEvent ame = null;
    array = (AnnotationMouseListener[]) listeners.toArray(array);

    for (int i=0; i<annots.size()-1; i++) {
      ame = new AnnotationMouseEvent(e, (AWBAnnotation)annots.get(i), 
				     component, false);
      for (int j=0; j<array.length && array[j] != null; j++) {
	array[j].mouseReleased(ame);
      }
    }
    ame = new AnnotationMouseEvent(e, 
				   (AWBAnnotation)annots.get(annots.size()-1), 
				   component, true);
    for (int j=0; j<array.length && array[j] != null; j++) {
      array[j].mouseReleased(ame);
    }
  }
}
