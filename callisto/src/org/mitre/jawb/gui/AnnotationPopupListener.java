
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
import java.awt.Menu;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.Action;


import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.gui.ActionModel.ActionCollection;

import gov.nist.atlas.type.AnnotationType;

public class AnnotationPopupListener extends AnnotationMouseAdapter {

  public static final int DEBUG = 0;

  private JMenuItem nullAction = new JMenuItem ("No Action");
  private TaskToolKit toolkit;
  
  // The toolkit is no longer used, but it's still here because
  // lots of tasks create popup listeners.

  public AnnotationPopupListener (TaskToolKit kit) {
    toolkit = kit;
    cMenu = new PopupContextMenu();
  }
  
  private PopupContextMenu cMenu;
  
  class PopupContextMenu extends TextContextMenu {
    
    Set annots;
    AnnotationType type;
    boolean multiTypes;

    public PopupContextMenu() {
      super();
    }

    public boolean addActionSets(ActionModel actionModel) {
      // What action sets do we want? Just the annotation actions. And 
      // the actions we want are the ones which are enabled. That's the default.
      ActionCollection aSet = actionModel.getGroupedAnnotationActions();
      boolean somethingAdded = addActionSet(aSet);
      if (!somethingAdded) {
        this.add(nullAction);
      }
      return true;
    }
    
  }

  // show context menu with actions appropriate to the annots selected
  // in the component. I no longer need to know here what annotations 
  // are selected, or anything - the ActionModel takes care of all of that,
  // as it should.
  
  private void showContextMenu (AnnotationMouseEvent e) {
    JawbComponent component = e.getJawbComponent();
    JawbDocument doc = component.getJawbDocument();
    MouseEvent me = e.getMouseEvent();
    
    // OK, nere I have to find the annot model. I'm going to 
    // hand it to the document, even though it doesn't need it. Getting 
    // it through the component hierarchy is unbelievably tiresome.
    
    ActionModel actionModel = toolkit.getActionModel();
    if (actionModel != null) {
      cMenu.showMenu(me, actionModel);
    }
  }

  public void mouseReleased (AnnotationMouseEvent e) {
    if (DEBUG > 0)
      System.err.println("APL.annotMouseReleased: pop=" + e.isPopupTrigger());
    MouseEvent me = e.getMouseEvent();
    if (!me.isConsumed() && e.isPopupTrigger()) {
      activate(e, me, "Released");
    }
  }

  // SAM 11/16/05: extracted the activate() method in order to handle the case
  // of both pressed and released AND to enable activation in external cases
  // (i.e., if there's a different sort of event, like a hyperlink event).
  
  public void activate(AnnotationMouseEvent e, MouseEvent me, String how) {
    // if there is an annot in the AME and
    // if this annot is not in the JC's selection model already,
    // clear selected annots and select this one
    // TODO there may be a problem if the "wrong" one is on top
    // or is not the first AME sent -- that would be a bug in MTP
    JawbComponent component = e.getJawbComponent();
    JawbDocument doc = component.getJawbDocument();
    Set selected = component.getSelectedAnnots();
    AWBAnnotation annot = e.getAnnotation();
    if (DEBUG > 2)
      System.err.println("APL.annotMouse" + how + ": " + selected.size() +
      " annots in JC's selection set");
    if (DEBUG > 3) 
      System.err.println("APL.annotMouse" + how + ": selected annots are: " +
          selected + " AME annot is " + annot);
    if (DEBUG > 3)
      System.err.println("APL.annotMouse" + how + ": selected.contains(annot) = " +
          selected.contains(annot));
    
    if (annot != null) {
      if (!selected.contains(annot)) {
        if (DEBUG > 2)
          System.err.println("APL.annotMouse" + how + ": annot not in " +
          "JC's selection set; change selection");
        doc.unselectAllAnnotations();
        doc.selectAnnotation(annot);
      }
    }
    if (annot != null || !selected.isEmpty()) {
      // if there was an annot in the AME or some annots selected in the jc
      // go ahead and do the context menu
      if (DEBUG > 1)
        System.err.println("APL.annotMouse" + how + ": show context menu");
      showContextMenu (e);
      me.consume ();
      e.consume();
    }
  }

  public void mousePressed (AnnotationMouseEvent e) {
    if (DEBUG > 0)
      System.err.println("APL.annotMousePressed: pop=" + e.isPopupTrigger());
    MouseEvent me = e.getMouseEvent();
    if (!me.isConsumed() && e.isPopupTrigger()) {
      activate(e, me, "Pressed");
    }
  }
}

