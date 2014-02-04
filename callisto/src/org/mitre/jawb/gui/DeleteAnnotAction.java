
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
import java.util.Set;
import javax.swing.text.Caret;
import javax.swing.text.TextAction;
import javax.swing.text.JTextComponent;
import javax.swing.JOptionPane;
import java.awt.KeyboardFocusManager;

import gov.nist.atlas.type.AnnotationType;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.JawbLogger;
import org.mitre.jawb.atlas.*;
import org.mitre.jawb.gui.*;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;

/**
 * Extension of {@link TextAction} to modify the extent of a Text
 * Annotation. This will work on both TextExtentRegions and
 * NamedExtentRegions, though is currently hard coded to RDC such that if the
 * annotation being modified is a NamedExtentRegions, enforces the rule that
 * the 'head' extent is a substring of the 'full' extent.
 */
public class DeleteAnnotAction extends AnnotationAction {

  public static final int DEBUG = 0;

  private TaskToolKit toolkit;
  
  private JawbLogger logger;

  private boolean warnOnMultiDelete = false;

  /**
   * Create an action which can be deleted by the specified TaskToolKit.
   * @param toolkit The toolkit to forward the delete action to.
   */
  public DeleteAnnotAction (TaskToolKit toolkit) {
    super ("Delete Annotation", null, -1);
    this.toolkit = toolkit;
    this.logger = Jawb.getLogger();
    if (toolkit == null)
      throw new IllegalArgumentException ("toolkit");
    
    putValue (ACTION_COMMAND_KEY, "delete-annotation");
  }

  /**
   * Create an action which can be deleted by the specified TaskToolKit.
   * @param toolkit The toolkit to forward the delete action to.
   */
  public DeleteAnnotAction (TaskToolKit toolkit, boolean warn) {
    super ("Delete Annotation", null, -1);
    this.toolkit = toolkit;
    this.logger = Jawb.getLogger();
    if (toolkit == null)
      throw new IllegalArgumentException ("toolkit");
    this.warnOnMultiDelete = warn;
    
    putValue (ACTION_COMMAND_KEY, "delete-annotation");

  }

  /**
   * TODO: stop hard coding for RDC.
   */
  public void actionPerformed (ActionEvent e) {
    // Kludgy but necesssary as offsets change while being deleted.
    Set removable = getSelectedAnnots (e);
    JawbDocument doc = getJawbDocument(e);
    if (DEBUG > 0) {
      System.err.println("DeleteAnnotAction Triggered for " +
			 removable.size() + " annotations");
    }
    if (DEBUG > 2) {
      System.err.println("DAA: Action Event Details:  \n\t" +
			 "Action Command: " + e.getActionCommand() + "\n\t" +
			 "Param String: " + e.paramString() + "\n\t" +
			 "When: " + e.getWhen());
    }
    
    if (warnOnMultiDelete && removable.size() > 1) {
      int choice =
        JOptionPane.showConfirmDialog (getFocusOwner(), 
                                       "Do you really want to delete " 
                                       + removable.size() + " annotations?",
                                       "Confirm Multi-Annot Delete",
                                       JOptionPane.OK_CANCEL_OPTION,
                                       JOptionPane.QUESTION_MESSAGE);
      if (choice == JOptionPane.CANCEL_OPTION ||
          choice == JOptionPane.CLOSED_OPTION)
        return;
    }

    // loop over selected annotations, deleting each
    Iterator iter = removable.iterator ();
    while (iter.hasNext ()) {
      AWBAnnotation annot = (AWBAnnotation)iter.next ();
      String id = annot.getId().getAsString();
      String type = annot.getAnnotationType().getName();
      if (DEBUG > 0) {
	System.err.println("DeleteAnnotAction trying to delete " + annot);
      }
      if (! toolkit.deleteAnnotation (annot, doc)) { // delete failed
        logger.info(JawbLogger.LOG_DELETE_ANNOT_FAIL, 
                    new Object[] {type, id});
        if (DEBUG > 0)
          System.err.println ("DeleteAnnotAction: failed to delete "+ id);
        // break ;// RK 1/31/06 don't break
                  // should try to delete each even if one fails
      } else { // it did delete, log it
        logger.info(JawbLogger.LOG_DELETE_ANNOT, new Object[] {type, id});
      }
    }
  }

  public static Component getFocusOwner () {
    return KeyboardFocusManager.getCurrentKeyboardFocusManager()
      .getPermanentFocusOwner();
  }

  public String toString () {
    return "[DeleteAnnotAction: "+toolkit.getTask ().getName()+"]";
  }
}

