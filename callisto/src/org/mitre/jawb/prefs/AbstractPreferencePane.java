
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

package org.mitre.jawb.prefs;

import java.awt.*;
import java.beans.*;
import javax.swing.*;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.swing.EnableLabel;

/**
 * Abstract implementation of the <code>PreferencePane</code> interface used
 * as a JPanel, giving some default functionality. Subclasses need only
 * implement the save and reset methods.
 */
public abstract class AbstractPreferencePane extends JPanel implements PreferenceItem
{
  /** cheat to get size for separators based on text size */
  private static JLabel cheater = new JLabel (" ");
  
  protected int y = 0;
  protected GridBagLayout gridBag;

  /**
   * Creates a new option pane.
   * @param name Uses specified name as identifier, and looks in the
   *             preferences for the title. "option.&lt;name&gt;.title" is *
   *             looked up in the preferences, otherwise 'name' is used as the
   *             title.
   */
  public AbstractPreferencePane (String name) {
    setName (name);
    setLayout (gridBag = new GridBagLayout());
  }

  /**
   * Adds a labeled component in the pane. All the components
   * are placed on bottom of each other (vertically sorted) and aligned nicely.
   * @param label The label to be displayed next to the component
   * @param comp The component to be added
   */
  protected void addComponent (String label, Component comp) {
    GridBagConstraints cons = new GridBagConstraints();
    cons.gridy = y++;
    cons.gridwidth = 3;
    cons.fill = cons.NONE;
    cons.weightx = 1.0f;

    cons.gridx = 0;
    cons.anchor = cons.WEST;
    JLabel l = new EnableLabel(label, SwingConstants.LEFT);
    l.setLabelFor (comp);
    gridBag.setConstraints(l, cons);
    add(l);

    cons.gridx = 3;
    cons.gridwidth = 1;
    cons.anchor = GridBagConstraints.WEST;

    gridBag.setConstraints(comp, cons);
    add(comp);
  }

  /**
   * Does the same as <code>addComponent(String, Component)</code>
   * but don't add a label next to the component. Forwarding method do to
   * addComponent(Component, int) with fill specified as
   * GridBagConstraints.NONE.
   * @param comp The component to be added
   */
  protected void addComponent (Component comp) {
    addComponent (comp, GridBagConstraints.NONE);
  }
  
  /**
   * Does the same as <code>addComponent(String, Component)</code>
   * but don't add a label next to the component. Resizes the component
   * according to the fill value, as specified in GridBagConstraints
   * @param comp The component to be added
   * @param fill the GridBagConstratints.fill value to use when resizing the
   * component, if it's display area is larger than requested size.
   * @see GridBagConstraints#fill
   */
  protected void addComponent (Component comp, int fill) {
    GridBagConstraints cons = new GridBagConstraints();
    cons.gridy = y++;
    cons.gridwidth = cons.REMAINDER;
    cons.fill = fill;
    cons.anchor = cons.WEST;
    cons.weightx = 1.0f;

    gridBag.setConstraints(comp, cons);
    add(comp);
  }

  /**
   * Adds a separator at the current y position to space components apart, and
   * logically divide preference groups within.
   */
  protected void addSeparator () {
    JPanel p = new JPanel (new BorderLayout ());
    p.add (Box.createVerticalStrut (cheater.getPreferredSize().height),
                                    BorderLayout.EAST);
    p.add (new JSeparator (JSeparator.HORIZONTAL));

    addComponent (p, GridBagConstraints.BOTH);
  }

  /**
   * Returns itself.
   */
  public Component getPreferenceComponent () {
    return this;
  }

  /**
   * Looks in the preferences for a title. Looks up preference
   * "option.&lt;name&gt;.title", and if not found, 'name' is used
   * as the title.
   */
  public String toString () {
    String name = getName ();
    String title = Jawb.getPreferences ().getPreference ("option."+ name +".title");
    return (title==null ? name : title);
  }

}// AbstractPreferencePane.java
