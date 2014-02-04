
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.Caret;
import javax.swing.text.TextAction;
import javax.swing.text.JTextComponent;

import gov.nist.atlas.type.AnnotationType;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.JawbLogger;
import org.mitre.jawb.atlas.*;
import org.mitre.jawb.gui.*;
import org.mitre.jawb.tasks.Task;

/**
 * Extension of {@link TextAction} to modify the extent of a Text
 * Annotation. This will work on both TextExtentRegions and
 * NamedExtentRegions, though is currently hard coded to RDC such that if the
 * annotation being modified is a NamedExtentRegions, enforces the rule that
 * the 'head' extent is a substring of the 'full' extent.
 */
public class CreateTextAnnotAction extends TextAction {

  public static final int DEBUG = 0;

  // TODO: stop hard coding for RDC.
  final String HEAD = "head";
  final String FULL = "full";

  private Task task;
  private JawbLogger logger;
  protected AnnotationType type;
  protected Class annotClass;
  protected List extents;

  /**
   * Create an action which creates a text based annotation of AnnotationType
   * {@link NamedExtentRegions} or {@link TextExtentRegion}. For
   * NamedExtentRegions, all regions are given the extent of the currently
   * swiped text.
   *
   * @param name name of action presented to user
   * @param type AnnotationType of annotation to create, which must create
   * annotation with the class namedExtentRegions or TextExtentAnnotation.
   */
  public CreateTextAnnotAction (String name, Task task,
                                AnnotationType type, String highlightKey) {
    super (name);
    if (DEBUG > 0)
      System.err.println("CreateTextAnnotAction: name=" + name +
			 " task=" + task + " type=" + type +
			 " highlight=" + highlightKey);
    if (name == null)
      throw new IllegalArgumentException ("name");
    if (task == null)
      throw new IllegalArgumentException ("task");
    if (type == null)
      throw new IllegalArgumentException ("type");
    if (highlightKey == null)
      throw new IllegalArgumentException ("highlightKey");

    this.task = task;
    this.type = type;
    this.logger = Jawb.getLogger();

    putValue (JawbAction.HIGHLIGHT_KEY, highlightKey);
    putValue (ACTION_COMMAND_KEY, "createTextAnnot-"+type.getName());

    // get the class for the annot
    annotClass = task.getAnnotationClass (type);
    if (DEBUG > 0)
      System.err.println("CreateTextAnnotAction: annotClass for "
			 + type + " = " +
			 (annotClass==null?"null":annotClass.toString()));

    // determine the extents to set
    if (TextExtentRegion.class.isAssignableFrom (annotClass)) {
      // no extents
      extents = null;

    } else if (NamedExtentRegions.class.isAssignableFrom (annotClass)) {
      // multiple extents.
      extents = new LinkedList ();
      Iterator attribIter = task.getAttributes (type).iterator ();
      while (attribIter.hasNext ()) {
        String attrib = (String) attribIter.next();
        if (attrib.endsWith ("TextExtent")) {
          int dotIndex = attrib.indexOf ('.');
          String substring = dotIndex == -1 ? attrib : attrib.substring (0,dotIndex);
          extents.add (substring.intern());
        }
      }

    } else {
      throw new IllegalArgumentException ("AnnotationClass of type "+type+
                                          " is neither TextExtentRegion"+
                                          ", or NamedExtentRegions");
    }
  }

  /** Several sanity checks want pretty much the same error message. */
  protected AWBAnnotation error (String msg) {
    System.err.println ("CreateTextAnnotAct: "+msg+
                        "\n\tthis is a bug. "+this);
    Thread.dumpStack ();
    GUIUtils.beep();
    return null;
  }


  /**
   * Subclasses may want direct access to the annotation created so this
   * method is separated so it may be followed with direct modifications to
   * the annotation.
   */
  public AWBAnnotation createAnnotation (ActionEvent e) {

    JawbDocument doc = GUIUtils.getJawbDocument(e);
    JTextComponent component = getTextComponent (e);

    // sanity check: rather difficult if we can't get the doc or component
    if (doc == null || component == null)
      return error ("no document or component");

    // now get the region of text selected
    Caret caret = component.getCaret ();
    int dot = caret.getDot();
    int mark = caret.getMark();
    if (DEBUG > 1)
      System.err.println("CreateTextAnnotAction.createAnnot dot=" + dot +
                         " mark=" +mark);
                         

    // sanity check: shouldn't be available to user if no text swiped.
    if (dot == mark)
      return error ("no text is 'swiped'");

    int start = Math.min (dot, mark);
    int end = Math.max (dot, mark);
    if (DEBUG > 2)
      System.err.println("CreateTextAnnotAction.createAnnot start=" + start +
                         " end=" +end);
                         

    AWBAnnotation annot = null;

    // ****************************
    // OK, create plain TextRegions
    if (TextExtentRegion.class.isAssignableFrom (annotClass)) {
      // no extents, but if the region has subannots, standard method fails
      annot = doc.createAnnotation (type);
      ((TextExtentRegion)annot).setTextExtents (start, end);
      if (DEBUG > 0)
        System.err.println ("CreateTAnnot: created: "+annot);


    // *****************************************
    // OK, create multiextent NamedExtentRegions
    } else if (NamedExtentRegions.class.isAssignableFrom (annotClass)) {
      // multiple extents.
      annot = doc.createAnnotation (type);
      if (DEBUG > 0)
        System.out.print("CreateTAnnot: created: "+annot+"\n  extents ");

      Iterator extentIter = extents.iterator ();
      while (extentIter.hasNext ()) {
        String extent = (String) extentIter.next();

        ((NamedExtentRegions)annot).setTextExtents (extent, start, end);
        if (DEBUG > 0)
          System.out.print (extent+"=("+start+","+end+") ");
      }
      if (DEBUG > 0)
        System.out.println();
    } // else impossible: checked in ctor

    if (annot != null)
      logCreation (type.getName(), annot.getId().getAsString(),
                   String.valueOf(start), String.valueOf(end));

    return annot;
  }

  /**
   * TODO: stop hard coding for RDC.
   */
  public void actionPerformed (ActionEvent e) {
    createAnnotation (e);
  }

  /** 
   * log the creation of the text annotation
   * may be specialized by subclasses to log additional arguments
   */
  protected void logCreation(String type, String id, 
                             String start, String end) {
    // log it
    Jawb.getLogger().info(JawbLogger.LOG_CREATE_TEXT_ANNOT, 
                          new Object[] {type, id, start, end});
  }

  public String toString () {
    return "[CreateTextAnnotAction"+type.getName()+"]";
  }
}

