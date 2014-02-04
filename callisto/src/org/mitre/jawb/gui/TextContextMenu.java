
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
package org.mitre.jawb.gui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.mitre.jawb.gui.ActionModel.ActionCollection;
import org.mitre.jawb.gui.ActionModel.ActionDelegate;
import org.mitre.jawb.gui.ActionModel.ActionGroup;
import org.mitre.jawb.tasks.TaskToolKit;

public abstract class TextContextMenu extends JPopupMenu {
  
  private static final int DEBUG = 0;
  
  public TextContextMenu(String s) {
    super(s);
  }
  
  public TextContextMenu() {
    super();
  }
  
  // We need to add the action sets, based on what the menu wants. 
  // This function should call addActionSet to add the actions.
  // This is one of two possible entry points. If you never use 
  // this one, don't bother to specialize it.
  
  public boolean addActionSets(ActionModel actionModel) {
    return false;
  }
  
  // This is only JComponent because that's the lowest common
  // parent between JMenu (which we need for the cascades) and JPopupMenu.
  // Grrr. You're guaranteed that you'll be able to cast to one
  // or the other of those, if necessary.
  
  // This function should test the appropriateness of the action, and call
  // addAction if it's appropriate, and return the value of addAction.
  
  // Once the task actions are added, the implementer has the option of
  // adding more elements to the toplevel entry. somethingAdded is true if
  // the menu is already nonempty. The function should return 
  // true if it's added something.
  
  private boolean addAction(JComponent menu, Action nextAct, ActionCollection c, boolean enabledOnly) {
    ActionGroup ag = null;
    if (c != null) {
      ag = c.getActionGroup(nextAct);
    }
    if (ag != null) {
      
      JMenu cascade = 
        new JMenu (ag);
      
      boolean somethingAdded = populateMenuFromActionGroup(cascade, ag, c, enabledOnly);
      if (DEBUG > 3) 
        System.err.println("TCM.addAction: adding sub-act");
      // This is a regular add. Only do it if the popup is not empty.
      if (somethingAdded) {
        ActionModel.addColor(nextAct, cascade);
        menu.add(cascade);
      }
      return somethingAdded;
    } else if (menu instanceof JMenu) {
      JMenu jmenu = (JMenu) menu;
      JMenuItem mi = jmenu.add(nextAct);
      ActionModel.addColor(nextAct, mi);
      return true;
    } else if (menu instanceof JPopupMenu) {
      JPopupMenu popup = (JPopupMenu) menu;
      JMenuItem mi = popup.add(nextAct);
      ActionModel.addColor(nextAct, mi);
      return true;
    } else {
      return false;
    }
  }

  public boolean populateMenuFromActionGroup(JComponent menu, ActionGroup nextAct,
      ActionCollection c, boolean enabledOnly) {
    Action[] subActions = 
      nextAct.getSubActions();
    if (DEBUG > 1)
      System.err.println("TCM.addAction: cascade with " +
          subActions.length + " sub-actions");
    
    boolean somethingAdded = false;
    
    if (!nextAct.isDummyParent()) {
      // If we're supposed to include the action parent,
      // add an element using the action, and set the string.
      // Menu might be a popup menu, or a menu.
      if (menu instanceof JMenu) {
        JMenuItem mi = ((JMenu) menu).add(nextAct.getAction());
        mi.setText("(this tag)");
        somethingAdded = true;
      } else if (menu instanceof JPopupMenu) {
        JMenuItem mi = ((JPopupMenu) menu).add(nextAct.getAction());
        mi.setText("(this tag)");
        somethingAdded = true;
      }
    }
    // Arbitrary recursion is possible.
    if (addActionSetRecursive(menu, subActions, c, enabledOnly)) {
      somethingAdded = true;
    }
    return somethingAdded;
  }

  public void showMenu(MouseEvent me, ActionModel m) {
    showMenu(me.getComponent(), me.getX(), me.getY(), m);
  }
  
  public void showMenu(Component c, int x, int y, ActionModel m) {
    removeAll();
    if (DEBUG > 1)
      System.err.println ("TCM.show: cleared");
    boolean somethingAdded = false;
    
    somethingAdded = addActionSets(m);
    
    if (somethingAdded) {
      show (c, x, y);
    }
  }
  
  public void showMenu(MouseEvent me, ActionGroup a, ActionCollection c, boolean enabledOnly) {
    showMenu(me.getComponent(), me.getX(), me.getY(), a, c, enabledOnly);
  }
  
  public void showMenu(Component c, int x, int y, ActionGroup a, ActionCollection coll, boolean enabledOnly) {
    removeAll();
    if (DEBUG > 1)
      System.err.println ("TCM.show: cleared");
    boolean somethingAdded = false;
    
    somethingAdded = populateMenuFromActionGroup(this, a, coll, enabledOnly);
    
    if (somethingAdded) {
      show (c, x, y);
    }
  }
  
  private static Action[] actionArrayPrototype = new Action[] {} ;
  
  public boolean addActionSet(Collection taskActions, ActionCollection c, boolean enabledOnly) {
    return addActionSetRecursive(this, (Action[]) taskActions.toArray(actionArrayPrototype), c, enabledOnly);
  }
  
  public boolean addActionSet(Collection taskActions, ActionCollection c) {
    return addActionSet(taskActions, c, true);
  }
  
  public boolean addActionSet(Action[] taskActions, ActionCollection c, boolean enabledOnly) {
    return addActionSetRecursive(this, taskActions, c, enabledOnly);
  }
  
  public boolean addActionSet(Action[] taskActions, ActionCollection c) {
    return addActionSet(taskActions, c, true);
  }
  
  public boolean addActionSet(ActionCollection c) {
    return addActionSet(c, c);
  }
  
  public boolean addActionSet(ActionCollection c, boolean enabledOnly) {
    return addActionSet(c, c, enabledOnly);
  }
  
  private boolean addActionSetRecursive(JComponent menu, Action[] taskActions, 
      ActionCollection c, boolean enabledOnly) {
    if (DEBUG > 0)
      System.err.println("TextContextMenu.addActSetRec: enabledOnly = " +
                         enabledOnly);
    // first pass for text actions
    boolean somethingAdded = false;
    for (int i = 0; i < taskActions.length; i++) {
      Action act = taskActions[i];
      if (DEBUG > 2)
        System.err.println("TextContextMenu.addActSetRec adding: " + act +
                           "\n\tenabled: " + act.isEnabled());
      if (((!enabledOnly) || act.isEnabled()) && addAction(menu, act, c, enabledOnly)) {
        somethingAdded = true;
      }
    }
    return somethingAdded;
  }

}
