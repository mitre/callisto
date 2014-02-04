
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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.mitre.jawb.Jawb;
import org.mitre.jawb.gui.JawbComponent;
import org.mitre.jawb.gui.JawbDocument;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.swing.DetachableTabbedPane;

/**
 * DetachableTabbedPane which implements the JawbComponent interface, and
 * propogates JawbComponent calls to it's subpanels (if they are JawbComponets
 * themselves).<p>
 *
 * Users of this class should be carefull with when they remove
 * subpanels. When subpanels are removed, they do not have their JawbDocument
 * set to null. This is good for moving panels around between
 * JawbComponentContainers, but if the JawbDocument is ever closed, and a
 * component with reference to it is not in the Container heirarchy, it will
 * have a dead reference (or more acurately, keep the dead reference alive).<p>
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */
public class DetachableTabsJawbComponent extends DetachableTabbedPane
  implements JawbComponent {

  protected JawbDocument jawbDocument = null;
  /**
   * Used to store information in user prefs, currently only geometry. Nothing
   * is stored if namespace is null.  Property keys should look like
   * "<property>.<namespace>.<tab-title>", eg:
   * "windows.org.mitre.rdc.Relation".
   */
  protected String namespace = null;
  
  public DetachableTabsJawbComponent () {
    super ();
    addAncestorListener(new JFTracker());
    setDetachedIconImage (GUIUtils.getJawbIconImage ());
  }
  public DetachableTabsJawbComponent (String namespace) {
    this ();
    addAncestorListener(new JFTracker());
    this.namespace = namespace;
  }
  public DetachableTabsJawbComponent (int tabPlacement, boolean detachable,
                                      String namespace) {
    super (tabPlacement, detachable);
    addAncestorListener(new JFTracker());
    setDetachedIconImage (GUIUtils.getJawbIconImage ());
    this.namespace = namespace;
  }
  public DetachableTabsJawbComponent (int tabPlacement, int tabLayoutPolicy,
                                      boolean detachable, String namespace) {
    super (tabPlacement, tabLayoutPolicy, detachable);
    addAncestorListener(new JFTracker());
    setDetachedIconImage (GUIUtils.getJawbIconImage ());
    this.namespace = namespace;
  }

  /**
   * Returns property which, if set will be used to store the window geometry
   * of each of the detachable frames of this component.  The namespace is not
   * dynamic at runtime.
   */ 
  public String getNamespace () {
    return namespace;
  }

  protected Detachable createDetachable (String title, Icon icon,
                                         Component comp,
                                         String tip, int index) {

    // final, since used within the closures below
    final Detachable d = super.createDetachable (title, icon, comp,
                                                 tip, index);
    
    // set this so that GUIUtils.getJawbDocument() can be called on events
    // originating from the detached tab.
    // TODO: this will break if this DetachableTabs... is ever moved to a
    // different jawb frame
    JComponent c = d.getFrame ().getRootPane ();
    c.putClientProperty (GUIUtils.JAWB_FRAME_KEY,GUIUtils.getJawbFrame (this));
    
    if (namespace != null) {
      // prefer the components name, but use title (may be i12ized or null too)
      String name = d.getComponent().getName();
      if (name == null)
        name = d.getTitle();

      // final strings to avoid all the string ops later in the closures
      final Preferences prefs = Jawb.getPreferences ();
      final String prefKey = namespace+"."+name;
      final String detachedPrefKey = "windows."+prefKey+".detached";

      // load the geometry for the panel if already stored
      GUIUtils.loadGeometry (d.getFrame(), prefKey);
      // set the panel 'detached' state if stored (default is false)
      boolean detached = 
        prefs.getBoolean (detachedPrefKey);
      d.setDetached (detached);
      
      // listen for movements and window hide/show (storing geometry/detached)
      d.getFrame ().addComponentListener (new ComponentListener () {
          public void componentMoved (ComponentEvent e) { storeGeometry(); }
          public void componentResized (ComponentEvent e) { storeGeometry(); }
          public void componentShown (ComponentEvent e) { storeDetached(); }
          public void componentHidden (ComponentEvent e) { storeDetached(); }
          /**
           * componentMoved/Resized save location and dimension
           */
          private void storeGeometry () {
            if (namespace != null)
              GUIUtils.storeGeometry (d.getFrame(), prefKey);
          }
          /**
           * componentShown/Hidden is triggered if the tabbed pane is
           * hidden, but the component is still 'detached', so store the
           * detached state explicitly, not the window's visibility.
           */
          private void storeDetached () {
            if (namespace != null &&
              	prefs.getBoolean (Preferences.STORE_WINDOW_GEOMETRY_KEY))
              prefs.setPreference (detachedPrefKey, d.isDetached ());
          }
        });
    }
    return d;
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
   * Overridden to set JawbComponent state if subcomponent accepts. All the
   * <code>add</code> and <code>addTab</code> methods are cover methods for
   * <code>insertTab</code>.
   */
  public void insertTab (String title, Icon icon, Component comp,
                         String tip, int index) {
    addJawbComponent (comp);
    super.insertTab (title, icon, comp, tip, index);
  }
  protected void addImpl (Component comp, Object constraints, int index) {
    addJawbComponent (comp);
    super.addImpl (comp, constraints, index);
  }
  
  /**
   * Set the JawbDocument for both sub panels of this splitpane.
   * @param doc the <code>JawbDocument</code> to edit
   */
  public void setJawbDocument (JawbDocument doc) {
    Iterator iter = panelToDetMap.keySet ().iterator ();
    while (iter.hasNext ()) {
      Component comp = (Component) iter.next ();

      // change the document of JawbComponents
      if (comp instanceof JawbComponent)
        ((JawbComponent) comp).setJawbDocument (doc);
    }
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
  
  /**
   * Listener to keep the JawbFrame reference in Detachables up to date.
   */
  private class JFTracker implements AncestorListener {
    public void ancestorAdded(AncestorEvent e) {
      Detachable[] detachables = getDetachables();
      for (int i=0;i<detachables.length; i++) {
        Detachable d = detachables[i];
        JComponent c = d.getFrame ().getRootPane ();
        c.putClientProperty (GUIUtils.JAWB_FRAME_KEY,
                             GUIUtils.getJawbFrame (DetachableTabsJawbComponent.this));
      }
    }
    public void ancestorMoved(AncestorEvent e) {}
    public void ancestorRemoved(AncestorEvent e) {
      Component[] comps = getComponents();
      for (int i=0;i<comps.length; i++) {
        Detachable d = getDetachable(comps[i]);
        JComponent c = d.getFrame ().getRootPane ();
        c.putClientProperty (GUIUtils.JAWB_FRAME_KEY,
                             GUIUtils.getJawbFrame (DetachableTabsJawbComponent.this));
      }
    }
  }
  
}// SplitPaneJawbComponent
