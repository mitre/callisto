
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
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.TextAction;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.prefs.ColorSpec;

/**
 * Widget to hold commonly used actions as buttons.
 */
public class ToolBar extends JToolBar implements PropertyChangeListener {

  private static final int DEBUG = 0;

  /** track the buttons we support */
  private HashMap actionMap = new HashMap ();
  
  /**
   * Constructor
   * Purpose: Creates a mention popup menu
   */
  public ToolBar () {
    super ("TextPane ToolBar");
    setFloatable (false);
  }
  
  public JButton add (Action a) {
    throw new UnsupportedOperationException ("So I don't mis-type");
  }

  public void addAction (Action action) {
    // only text actions
    if (! (action instanceof TextAction))
      return;
    // only add once
    if (actionMap.get (action) != null)
      return;
    
    JButton button = new JButton (action);

    // will this change to something more complex?
    // if it exists, listen for changes
    ColorSpec color = (ColorSpec) action.getValue (JawbAction.HIGHLIGHT_COLOR);
    if (DEBUG > 0)
      System.err.println ("ToolBar.addAction: " + 
                          action.getValue (Action.NAME) +
                          "color = " + 
                          (color == null?"null":color.toString()));
    if (color != null) {
      action.addPropertyChangeListener (this);
      button.setBackground (color.getBackground());
      button.setForeground (color.getForeground());
    }
    
    actionMap.put (action, button);
    
    add (button);
  }
  
  public void remove (Action action) {
    action.removePropertyChangeListener (this);

    JButton b = (JButton)actionMap.remove (action);
    if (b != null)
      remove (b);
  }

  public void clear () {
    Action[] actArray = (Action[])actionMap.keySet().toArray (new Action[0]);
    for (int i=0; i<actArray.length; i++)
      remove (actArray[i]);
  }

  /**
   * Listen for changes for color keys of actions added here
   */
  public void propertyChange (PropertyChangeEvent evt) {
    String name = evt.getPropertyName ();
    if (JawbAction.HIGHLIGHT_COLOR.equals (name)) {
      Action action = (Action)evt.getSource ();
      JButton b = (JButton) actionMap.get (action);
      if (b != null) {
        ColorSpec cs = (ColorSpec)evt.getNewValue();
        b.setBackground (cs.getBackground());
        b.setForeground (cs.getForeground());
      }
      else // report the error
        System.err.println ("ToolBar.propertyChange: "+
                            "property change on unknown action: "+
                            action.getValue (action.NAME));
    }
  }
}
