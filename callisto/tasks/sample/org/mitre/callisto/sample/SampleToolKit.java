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

package org.mitre.callisto.sample;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.LinkedHashSet;

import gov.nist.atlas.type.AnnotationType;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.gui.CreateTextAnnotAction;
import org.mitre.jawb.gui.DeleteAnnotAction;
import org.mitre.jawb.gui.AnnotationPopupListener;
import org.mitre.jawb.gui.GUIUtils;
import org.mitre.jawb.gui.JawbComponent;
import org.mitre.jawb.gui.JawbDocument;
import org.mitre.jawb.gui.LabelJawbComponent;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;
import org.mitre.jawb.tasks.AbstractToolKit;

/**
 * A TaskToolKit is a pluggable module that allows for differnt editing
 * mechanisms for each of the tasks Callisto may use. All the gui specific
 * stuff is here.
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */
public class SampleToolKit extends AbstractToolKit {

  
  private Task task;

  private JawbComponent labelComponent;
  private Set actions = null;

  SampleToolKit (Task task) {
    this.task = task;
    final AnnotationPopupListener popListener =
      new AnnotationPopupListener(this);
    // override the setJawbComponent to handle the popup listener
    this.labelComponent = new LabelJawbComponent (task.getTitle ()) {
	public void setJawbDocument (JawbDocument doc) {
	  JawbDocument old = getJawbDocument();
	  super.setJawbDocument(doc);
	  if (old != null)
	    old.getAnnotationMouseModel().
	      removeAnnotationMouseListener(popListener);
	  if (doc != null)
	    doc.getAnnotationMouseModel().
	      addAnnotationMouseListener(popListener);
	}
      };
  }

  public Task getTask () {
    return task;
  }
  
  /**
   * Returns null, indicating this task uses the default main component.
   */
  public JawbComponent getMainComponent () {
    return null;
  }
  
  /**
   * Returns the JawbComponent to edit this tasks annotations. Always the same
   * object for a given RDCEditorKit.
   */
  public JawbComponent getEditorComponent () {
    return labelComponent;
  }

  /**
   * Some of these actions are tied to the SubordinateAssignor.
   */
  public Set getActions () {
    if (actions == null)  // lazy aren't we
      initActions ();
    return actions;
  }

  public boolean deleteAnnotation (AWBAnnotation annot, JawbDocument doc) {
    return doc.deleteAnnotation (annot);
  }

  private void initActions () {
    actions = new LinkedHashSet ();
    actions.add (new CreateTagAction ("NP"));
    actions.add (new CreateTagAction ("VP"));
    actions.add (new DeleteAnnotAction(this));
  }

  class CreateTagAction extends CreateTextAnnotAction {
    private String type;
    public CreateTagAction (String type) {
      super (type+" Tag", task, task.getAnnotationType ("tag"), type);
      this.type = type;
    }
    public void actionPerformed (ActionEvent e) {
      AWBAnnotation annot = super.createAnnotation (e);
      try {
        annot.setAttributeValue ("type", type);
      } catch (Exception x) {};
    }
  }
}
