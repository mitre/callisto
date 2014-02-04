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

import java.util.Set;
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.gui.ActionModel;
import org.mitre.jawb.gui.JawbComponent;
import org.mitre.jawb.gui.JawbDocument;

/**
 * A TaskToolKit is a pluggable module that allows for differnt editing
 * mechanisms for each of the tasks Callisto may use.<p>
 *
 * A tool kit's components may work together, although good designs will keep
 * component as separate as possible, using the events advertised from the
 * AnnotationModel and the current JawbDocument to modify the model.  A kit
 * can safely store editing state, as an instance of the kit will be dedicated
 * to each frame. New kits may be created by cloning a prototype kit. Kits can
 * safely expect that the JawbDocument of will be kept the same between the
 * MainComponent and the EditorComponent, although no guarantee is made as to
 * which will be set first.
 */
public interface TaskToolKit {

  /**
   * Return the task this ToolKit is good for.
   */
  public Task getTask ();
  
  /**
   * Return an instance of the JawbComponent who's component will be displayed
   * in the top of the JawbFrame.<p>
   *
   * An TaskToolKit should always return the same object when asked for its
   * MainComponent. If null is returned, the Callisto default editor will be
   * used.
   *
   * @return a Jawb compliant widget for basic display, or <code>null</code>
   * to use the Callisto default.
   */
  public JawbComponent getMainComponent ();

  /**
   * Return the JawbComponent for this TaskToolKit who's component will be
   * displayed in the bottom of the JawbFrame, generally used for editing
   * annotations. This widget may nest several others or even spawn separate
   * frames.<p>
   *
   * An TaskToolKit should always return the same object when asked for its
   * MainComponent.<p>
   *
   * @return a JawbComponent widget to edit annotations. A return value of
   * <code>null</code> is considered an error.
   */
  public JawbComponent getEditorComponent ();

  /**
   * These actions are added to the main widget and frame whenever this task
   * becomes current.  If this task uses the default MainComponent, any
   * actions which are subclasses of {@link javax.swing.text.TextAction} will
   * be added to a context menu for the MainComponent, and possibly as buttons
   * in other components.<p>
   *
   * An TaskToolKit should always return the same set of actions when
   * requested.<p>
   *
   * If the HIGHLIGHT_KEY value of an action, is equal any of the tasks
   * highlight keys (see {@link Task#getHighlightKeys}) Callisto will color
   * the buttons and menu items which represent this action in the default
   * MainWidget, with the associated highlight color (by maintaining the
   * JawbAction.HIGHLIGHT_COLOR value, which the Task designer needn't worry
   * about).
   */
  public Set getActions ();

  /**
   * Handle any Task specific complexities with deleting an annotation. This
   * should handle all annotations possible in a task. I'm not entirely
   * conmfortable with the fact that Components now have two ways to delete
   * an annotation: from here, or from the JawbDocument.  It's not clear
   * which they are to use.
   *
   * @return true if the annotation was deleted from the corpus. 
   */
  public boolean deleteAnnotation (AWBAnnotation annot, JawbDocument doc);
  
  /**
   * Returns an action model, which provides a view for the GUI of
   * the task actions, divided into various relevant pots of actions, and
   * maintaining a consisteng view of the availability.
   * 
   * @return ActionModel
   */
  
  public ActionModel getActionModel ();

  // to allow customization by context, must call this on the Toolkit
  // rather than the task -- this forwards to the task unless the
  // implementation wishes to customize
  public Set getExtentModifiableAnnotationTypes ();

}
