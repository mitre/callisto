
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

import javax.swing.JSplitPane;
import java.awt.Component;
import java.util.Collections;
import java.util.Set;
import org.mitre.jawb.gui.JawbComponent;
import org.mitre.jawb.gui.JawbDocument;

/**
 * JSplitPanel which implements the JawbComponent interface, and propogates
 * JawbComponent calls to it's subpanels (if they are JawbComponets
 * themselves).<p>
 *
 * Users of this class should be carefull with when they remove
 * subpanels. When subpanels are removed, they do not have their JawbDocument
 * set to null. This is good for moving panels around between
 * JawbComponentContainers, but if the JawbDocument is ever closed, and a
 * component with reference to it is not in the Container heirarchy, it will
 * have a dead reference (or more acurately, keep the dead reference alive).<p>
 *
 * This class also implements ancestor listener to work around 
 * bugs in JSplitPane. Most have to do with the fact that for proper layout,
 * JSplitPane wants to be added to a visible Component Heirarch before having
 * any of it's layout parameters specified.<p>
 * <ul><li>http://developer.java.sun.com/developer/bugParade/bugs/4182558.html
 *     <li>http://developer.java.sun.com/developer/bugParade/bugs/4188905.html
 *     <li>http://developer.java.sun.com/developer/bugParade/bugs/4101306.html
 *     <li>http://developer.java.sun.com/developer/bugParade/bugs/4131528.html
 * </ul>
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */
public class SplitPaneJawbComponent extends JSplitPane
  implements JawbComponent { // TODO: AncestorListener {
  
  protected JawbDocument jawbDocument = null;
  
  public SplitPaneJawbComponent () {
    super ();
  }
  public SplitPaneJawbComponent (int orientation) {
    super (orientation);
  }
  public SplitPaneJawbComponent (int orientation,
                                 boolean continuousLayout) {
    super (orientation, continuousLayout);
  }
  public SplitPaneJawbComponent (int orientation,
                                 boolean continuousLayout,
                                 Component leftComponent,
                                 Component rightComponent) {
    super (orientation, continuousLayout, leftComponent, rightComponent);
    // simply set's null to the components, in case they already have
    // something
    setJawbDocument (jawbDocument); 
  }
  public SplitPaneJawbComponent (int orientation,
                                 Component leftComponent,
                                 Component rightComponent) {
    super (orientation, leftComponent, rightComponent);
  }

  /**
   * Repetitive work when adding compoenents to propogate state for
   * JawbComponents.
   */
  private void addJawbComponent (Component c) {
    if (c instanceof JawbComponent) {
      ((JawbComponent)c).setJawbDocument (jawbDocument);
    }
  }
  
  /**
   * Overridden to set JawbComponent state if subcomponent accepts.
  public void setLeftCompoent (Component c) {
    addJawbComponent (c);
    super.setLeftComponent (c);
  }
  
  /**
   * Overridden to set JawbComponent state if subcomponent accepts.
  public void setRightCompoent (Component c) {
    addJawbComponent (c);
    super.setRightComponent (c);
  }
  
  /**
   * Overridden to set JawbComponent state if subcomponent accepts.
  public void setTopCompoent (Component c) {
    addJawbComponent (c);
    super.setTopComponent (c);
  }
  
  /**
   * Overridden to set JawbComponent state if subcomponent accepts.
  public void setBottomCompoent (Component c) {
    addJawbComponent (c);
    super.setBottomComponent (c);
  }
  */

  /**
   * Overridden to set JawbComponent state if subcomponent accepts.
   */
  protected void addImpl (Component comp, Object constraints, int index) {
    addJawbComponent (comp);
    super.addImpl (comp, constraints, index);
  }
  
// Implementation of org.mitre.jawb.gui.JawbComponent

  /**
   * Set the JawbDocument for both sub panels of this splitpane.
   * @param doc the <code>JawbDocument</code> to edit
   */
  public final void setJawbDocument (JawbDocument doc) {
    if (leftComponent instanceof JawbComponent)
      ((JawbComponent)leftComponent).setJawbDocument (doc);
    if (rightComponent instanceof JawbComponent)
      ((JawbComponent)rightComponent).setJawbDocument (doc);
    jawbDocument = doc;
  }

  /**
   * Get the JawbDocument this split pane was last set to edit.  This does
   * <i>not</i> check the JawbDocument of the the contained subpanels.
   * @return the <code>JawbDocument</code> this splitpane is to be editing.
   */
  public JawbDocument getJawbDocument () {
    return jawbDocument;
  }


  /** Returns he EMPTY_SET */
  public Set getSelectedAnnots () {
    return Collections.EMPTY_SET;
  }
 
  /**
   * @return this
   */
  public Component getComponent() {
    return this;
  }
  
}// SplitPaneJawbComponent
