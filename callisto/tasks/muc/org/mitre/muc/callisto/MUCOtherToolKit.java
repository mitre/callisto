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

package org.mitre.muc.callisto;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.LinkedHashSet;
import javax.swing.JTabbedPane;

import gov.nist.atlas.type.AnnotationType;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.gui.CreateTextAnnotAction;
import org.mitre.jawb.gui.DeleteAnnotAction;
import org.mitre.jawb.gui.AnnotationPopupListener;
import org.mitre.jawb.gui.JawbComponent;
import org.mitre.jawb.gui.JawbDocument;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;
import org.mitre.jawb.tasks.AbstractToolKit;
import org.mitre.jawb.gui.DetachableTabsJawbComponent;

import org.mitre.muc.callisto.session.*;


/**
 * A TaskToolKit is a pluggable module that allows for differnt editing
 * mechanisms for each of the tasks Callisto may use. All the gui specific
 * stuff is here.
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */
public class MUCOtherToolKit extends AbstractToolKit {

  private static final int DEBUG = 0;

  private Task task;

  private Set actions = null;
  private JawbComponent editorComponent;

  MUCOtherToolKit (Task task) {
    this.task = task;
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
    if (editorComponent == null)
      initEditorComponent ();
    return editorComponent;
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
    actions.add (new MyCreateTagAction (task, "ENAMEX", "PERSON"));
    actions.add (new MyCreateTagAction (task, "ENAMEX", "ORGANIZATION"));
    actions.add (new MyCreateTagAction (task, "ENAMEX", "LOCATION"));
    actions.add (new MyCreateTagAction (task, "TIMEX", "DATE"));
    actions.add (new MyCreateTagAction (task, "TIMEX", "TIME"));
    actions.add (new MyCreateTagAction (task, "NUMEX", "MONEY"));
    actions.add (new MyCreateTagAction (task, "NUMEX", "PERCENT"));
    actions.add (new MyCreateTagAction (task, "OTHER", "OTHER"));
    actions.add (new DeleteAnnotAction (this));
  }

  
  private void initEditorComponent () {
    // Overriding the 'setJawbDocument' method to handle logging too.
    final SessionLogger logger = new SessionLogger();
    final AnnotationPopupListener popListener = 
      new AnnotationPopupListener(this);
    JTabbedPane tp = new DetachableTabsJawbComponent (task.getName()) {
        public void setJawbDocument (JawbDocument doc) {
	  JawbDocument old = getJawbDocument();
          super.setJawbDocument (doc); // this is a must!!!
          logger.setJawbDocument (doc);
	  if (DEBUG > 0)
	    System.err.println("MucOtherTK.DetTabJC.setJD old=" +
			       old + " new=" + doc);
	  if (old != null)
	    old.getAnnotationMouseModel().
	      removeAnnotationMouseListener(popListener);
	  if (doc != null)
	    doc.getAnnotationMouseModel().
	      addAnnotationMouseListener(popListener);
        }
      };
    
    Component enamexEd =new SimpleTypeAnnotEditor(this, "ENAMEX",
                                                  new String[] {null, "PERSON",
                                                                "ORGANIZATION",
                                                                "LOCATION"});
    Component timexEd =new SimpleTypeAnnotEditor(this, "TIMEX",
                                                  new String[] {null, "DATE",
                                                                "TIME"});
    Component numexEd =new SimpleTypeAnnotEditor(this, "NUMEX",
                                                  new String[] {null, "MONEY",
                                                                "PERCENT"});
    Component otherEd =new SimpleTypeAnnotEditor(this, "OTHER", null);

    tp.add ("ENAMEX", enamexEd);
    tp.add ("TIMEX",  timexEd);
    tp.add ("NUMEX",  numexEd);
    tp.add ("OTHER",  otherEd);
    
    editorComponent = (JawbComponent)tp;
  }

  class MyCreateTagAction extends CreateTextAnnotAction {
    private String attrType;
    public MyCreateTagAction (Task task, String type, String attrType) {
      super (attrType+" Tag", task, task.getAnnotationType (type), attrType);
      this.attrType = attrType;
    }
    public void actionPerformed (ActionEvent e) {
      AWBAnnotation annot = super.createAnnotation (e);
      try {
          annot.setAttributeValue ("type", attrType);
      } catch (Exception x) {};
    }
  }
}
