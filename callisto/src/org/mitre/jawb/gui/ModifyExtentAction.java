
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

import java.util.EventObject;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;
import javax.swing.text.TextAction;
import javax.swing.text.JTextComponent;

import org.mitre.jawb.atlas.*;
import org.mitre.jawb.gui.*;

/**
 * Extension of {@link TextAction} to modify the extent of a Text
 * Annotation. This will work on both TextExtentRegions and
 * NamedExtentRegions, though is currently hard coded to RDC such that if the
 * annotation being modified is a NamedExtentRegions, enforces the rule that
 * the 'head' extent is a substring of the 'full' extent.
 */
public class ModifyExtentAction extends TextAction {

  public static final int DEBUG = 0;

  // TODO: stop hard coding for RDC.
  final String HEAD = "head";
  final String FULL = "full";

  private String extent = null;
  
  /**
   * Creates an action which modifies the named extent <code>extent</code> of
   * a {@link NamedExtentRegions} annotation, or the full extent of a plain
   * {@link TextExtentRegion} if <code>extent</code> is null. The GUI is
   * expected to provide only the appropriate ModifyingActions when a single
   * annotation is selected, and some text is swiped.
   *
   * @param name name of action presented to user
   * @param extent sub-extent of annotation to modify.
   */
  public ModifyExtentAction (String name, String extent) {
    super (name);
    if (name == null)
      throw new IllegalArgumentException ("name");
    this.extent = extent;
    putValue (ACTION_COMMAND_KEY,
              "modifyExtent"+( (extent==null) ? "" : "-"+extent));
  }

  /** Several sanity checks want pretty much the same error message. */
  private void error (String msg) {
    System.err.println ("ModifyExtentAction: "+msg+
                        "\n\tthis is a bug. "+this);
    Thread.dumpStack ();
    GUIUtils.beep();
  }

  /**
   * TODO: stop hard coding for RDC.
   */
  public void actionPerformed (ActionEvent e) {

    final JawbDocument doc = GUIUtils.getJawbDocument(e);
    JTextComponent component = getTextComponent (e);

    // sanity check: rather difficult if we can't get the doc or component
    if (doc == null || component == null) {
      error ("no document or component");
      return;
    }

    // now get the region of text selected
    Caret caret = component.getCaret ();
    int dot = caret.getDot();
    int mark = caret.getMark();

    // sanity check: shouldn't be available to user if no text swiped.
    if (dot == mark) {
      error ("no text is 'swiped'");
      return;
    }

    int start = Math.min (dot, mark);
    int end = Math.max (dot, mark);

    // sanity check: shouldn't be available to user if annot selected != 1
    final AWBAnnotation annot = doc.getSingleSelectedAnnotation();
    if (annot == null) {
      error ("exactly 1 annotation can be modified at a time");
      return;
    }

      if (DEBUG > 0)
    System.err.println ("ModifyExtentAction: dot/mark=["+dot+","+mark+
                        "] start/end=["+start+","+end+"]");
    
    // ****************************
    // OK, modify plain TextRegions
    if (annot instanceof TextExtentRegion && extent == null) {
      TextExtentRegion terAnnot = (TextExtentRegion) annot;
      if (DEBUG > 0)
        System.out.println("ModifyExtentAction: TextExtentRegion="+
                           terAnnot.getId());

      terAnnot.setTextExtents(start, end);
      
    // ***************************************
    // OK, modify extent of NamedExtentRegions
    } else if (annot instanceof NamedExtentRegions && extent != null) {
      NamedExtentRegions nerAnnot = (NamedExtentRegions) annot;
      if (DEBUG > 0)
        System.out.println("ModifyExtentAction: NamedExtentRegions="+
                           nerAnnot.getId());

      // make sure the head fits at least partially within the new extent, or
      // cause them to be equal. if modifying a head, if ther's no overlap
      // with extent, extent is entirely moved, not just extended. if extent
      // is neither HEAD, nor FULL, this probably isn't a Mention, so we won't
      // restrict in any way.
      int headStart,headEnd,fullStart,fullEnd;
      headStart = headEnd = fullStart = fullEnd = 0;
      boolean headUpdate = false;
      boolean fullUpdate = false;
      if (extent.equals (FULL)) {
        fullStart = start;
        fullEnd = end;
        fullUpdate = true;
        headStart = nerAnnot.getTextExtentStart (HEAD);
        headEnd = nerAnnot.getTextExtentEnd (HEAD);

        if (headStart <  start || end <= headStart) {
          headStart = start;
          headUpdate = true;
        }
        if (headEnd   <= start || end <  headEnd) {
          headEnd = end;
          headUpdate = true;
        }
        // else the HEAD already fit within new FULL

      } else if (extent.equals (HEAD)) {
        headStart = start;
        headEnd = end;
        headUpdate = true;
        fullStart = nerAnnot.getTextExtentStart (FULL);
        fullEnd = nerAnnot.getTextExtentEnd (FULL);
        int tempStart = fullStart;
        if (start <  fullStart || fullEnd <= start) {
          tempStart = start;
          fullUpdate = true;
        }
        if (end > fullEnd || fullStart >= end) {
          fullEnd = end;
          fullUpdate = true;
        }
        fullStart = tempStart;
        // else the FULL already enclosed the new HEAD
      }

      if (DEBUG > 1) {
        System.out.println("    ModExt: "+extent+"     change to ("+
                           headStart+","+headEnd+","+fullStart+","+fullEnd+")");
      }
      // and finally, make the modification requested
      if (headUpdate)
        nerAnnot.setTextExtents (HEAD, headStart, headEnd);
      if (fullUpdate)
        nerAnnot.setTextExtents (FULL, fullStart, fullEnd);
    }
    // extent modification will cause jtable to get change event, causing it to
    // unselect all annots, so reselect the modified annot after fallout.
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          doc.selectAnnotation(annot);
        }
      });
  }

  public String toString () {
    return "["+getValue (ACTION_COMMAND_KEY)+"]";
  }
}
