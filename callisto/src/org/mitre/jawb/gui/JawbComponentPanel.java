
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

import java.awt.*;
import java.util.Collections;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;

/**
 * Extension of JPanel implementing {@link JawbComponent} interface. This is an
 * adaptation of {@link JawbComponentContainer} that supports containment (and
 * layout) of any number of {@link JawbComponent} objects in the same manner as
 * JPanel supports Component objects.
 * 
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @author <a href="mailto:gwilliam@mitre.org">Galen B. Williamson</a>
 */
public class JawbComponentPanel extends JPanel
  implements JawbComponent, Scrollable {
  
  private JawbComponent[] jcs = new JawbComponent[0];
  private int njcs = 0;
  private JawbDocument jawbDocument;
  private JScrollPane scrollPane;
  private JawbComponentPanel contentPane;
  
  public JawbComponentPanel() {
    this(new BorderLayout(), false);
  }

  public JawbComponentPanel(LayoutManager layout) {
    this(layout, false);
  }

  public JawbComponentPanel(LayoutManager layout, boolean scrollable) {
    super();
    if (scrollable) {
      setLayout(new BorderLayout());
      contentPane = new JawbComponentPanel(layout, false);
      scrollPane = new JScrollPane(contentPane);
      add(scrollPane, BorderLayout.CENTER);
    }
    else {
      setLayout(layout);
      contentPane = this;
    }
  }

  public final void addJawbComponent (JawbComponent component) {
    Object constraints = null;
    if (contentPane == this && getLayout() instanceof BorderLayout) {
      constraints = BorderLayout.CENTER;
    }
    addJawbComponent(component, constraints);
  }
  
  public final void removeJawbComponent(JawbComponent component) {
    synchronized (getTreeLock()) {
      if (contentPane != this) {
        contentPane.removeJawbComponent(component);
      }
      else {
        remove(component.getComponent());
        for (int i = njcs; --i >= 0;) {
          if (jcs[i] == component) {
            System.arraycopy(jcs, i + 1, component, i, njcs - i - 1);
            jcs[--njcs] = null;
          }
        }
      }
    }
  }
  
  public final void addJawbComponent (JawbComponent component, Object constraints) {
    addJawbComponent(component, constraints, -1);
  }

  public final void addJawbComponent(JawbComponent component, Object constraints,
      int index) {
    synchronized (getTreeLock()) {
      if (contentPane != this) {
        contentPane.addJawbComponent(component, constraints, index);
      }
      else {
        for (int i = 0; i < njcs; i++) {
          if (jcs[i] == component) {
            removeJawbComponent(component);
          }
        }
        if (njcs == jcs.length) {
          JawbComponent newjcss[] = new JawbComponent[njcs * 2 + 1];
          System.arraycopy(jcs, 0, newjcss, 0, njcs);
          jcs = newjcss;
        }
        if (index == -1 || index == njcs) {
          jcs[njcs++] = component;
        }
        else {
          System.arraycopy(jcs, index, jcs, index + 1, njcs - index);
          jcs[index] = component;
          njcs++;
        }
        addImpl(component.getComponent(), constraints, index);
      }
    }
    validate();
    component.setJawbDocument(jawbDocument);
  }

  public final JawbComponent[] getJawbComponents() {
    synchronized (getTreeLock()) {
      if (contentPane != this) {
        return contentPane.getJawbComponents();
      }
      JawbComponent list[] = new JawbComponent[njcs];
      System.arraycopy(jcs, 0, list, 0, njcs);
      return list;
    }
  }
  
  /**
   * Set the JawbDocument for both sub panels of this splitpane.
   * 
   * @param doc the <code>JawbDocument</code> to edit
   */
  public void setJawbDocument (JawbDocument doc) {
    jawbDocument = doc;
    if (contentPane != this) {
      contentPane.setJawbDocument(doc);
    }
    else {
      JawbComponent[] jcs = getJawbComponents();
      for (int i = 0; i < jcs.length; i++) {
        jcs[i].setJawbDocument(doc);
      }
    }
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

  public Dimension getPreferredScrollableViewportSize() {
    return getMinimumSize();
  }

  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
//    Dimension size = getSize();
    Dimension vsize = visibleRect.getSize();
    return vsize.height;
  }

  public boolean getScrollableTracksViewportHeight() {
    return false;
  }

  public boolean getScrollableTracksViewportWidth() {
    return true;
  }

  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    Dimension size = getSize();
    Dimension vsize = visibleRect.getSize();
    FontMetrics fontMetrics = getFontMetrics(getFont());
    return (int) Math.ceil((size.getHeight() / vsize.getHeight()) * fontMetrics.getHeight());
//    return fontMetrics.getHeight();
  }
  
}
