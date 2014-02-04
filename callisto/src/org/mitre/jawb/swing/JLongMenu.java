
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

package org.mitre.jawb.swing;

import javax.swing.*;
import java.awt.*;

/**
 * A subclass of JMenu which adds a simple heuristic for ensuring
 * that the popup menu gets placed onscreen.<p>
 *
 * IMPORTANT: This only supports FIXED menus, that get only additions of
 * JMenuItems!  If you like to remove items from the menu in run-time, or add
 * other types of components, it needs to be developed! (but this is good for
 * most of the cases).<p>
 *
 * Location change is based on Sun's workaround for bug 4236438.
 * http://developer.java.sun.com/developer/bugParade/bugs/4236438.html<p>
 *
 * I have added the two changes also mentioned in the forum thread. Also, I
 * use java 1.4 capabilities to account for native system taskbars and
 * menubars properly.
 *
 * @author <a href="http://forum.java.sun.com/profile.jsp?user=149932>Moti Pinhassi</a>, 10-Apr-2002.
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @see <a href="http://forum.java.sun.com/thread.jsp?forum=57&thread=241368">
 *       Java Forums: Solution for a too long JMenu</a>
 * @version 1.1
 */
public class JLongMenu extends JMenu {
  JLongMenu moreMenu = null;
  int maxItems = 15; // default

  public JLongMenu(String label) {
    super(label);
    JMenuItem getHeightMenu = new JMenuItem("Temporary");
    int menuItemHeight = getHeightMenu.getPreferredSize().height;
    int visibleHeight = GraphicsEnvironment.getLocalGraphicsEnvironment()
      .getMaximumWindowBounds().height;

    // leave one for the "more" menu
    maxItems = visibleHeight / menuItemHeight - 1;
  }

  // We override this method just so we can call our version of
  // getPopupMenuOrigin. It is pretty much just a copy of
  // JMenu.setPopupMenuVisible

  public void setPopupMenuVisible(boolean b) {
    if (!isEnabled())
      return;
    boolean isVisible = isPopupMenuVisible();
    if (b != isVisible) {
      // We can't call ensurePopupMenuCreated() since it is private so
      // we call a method that calls it. (Sneaky huh?).
      isPopupMenuVisible();
      // Set location of popupMenu (pulldown or pullright)
      //  Perhaps this should be dictated by L&F
      if ((b==true) && isShowing()) {
        Point p = getPopupMenuOrigin();
        getPopupMenu().show(this, p.x, p.y);
      } else {
        getPopupMenu().setVisible(false);
      }
    }
  }

  /**
   * Compute the origin for the JMenu's popup menu.
   *
   * @return a Point in the coordinate space of the menu instance
   * which should be used as the origin of the JMenu's popup menu.
   */
  protected Point getPopupMenuOrigin() {
    int x = 0;
    int y = 0;
    JPopupMenu pm = getPopupMenu();
    // Figure out the sizes needed to caclulate the menu position
    Dimension screenSize =Toolkit.getDefaultToolkit().getScreenSize();
    Dimension s = getSize();
    Dimension pmSize = pm.getSize();
    // For the first time the menu is popped up,
    // the size has not yet been initiated
    if (pmSize.width==0) {
      pmSize = pm.getPreferredSize();
    }
    Point position = getLocationOnScreen();

    Container parent = getParent();
    if (parent instanceof JPopupMenu) {
      // We are a submenu (pull-right)

      // if( SwingUtilities.isLeftToRight(this) ) { // Package private.
      if( getComponentOrientation().isLeftToRight()) {
        // First determine x:
        if (position.x+s.width + pmSize.width < screenSize.width) {
          x = s.width;         // Prefer placement to the right
        } else {
          x = 0-pmSize.width;  // Otherwise place to the left
        }
      } else {
        // First determine x:
        if (position.x < pmSize.width) {
          x = s.width;         // Prefer placement to the right
        } else {
          x = 0-pmSize.width;  // Otherwise place to the left
        }
      }
      // Then the y:
      if (position.y+pmSize.height < screenSize.height) {
        y = 0;                       // Prefer dropping down
      } else {
        y = s.height-pmSize.height;  // Otherwise drop 'up'
        if(y < 0-position.y)
          y = 0-position.y;
      }
    } else {
      // We are a toplevel menu (pull-down)

      // if( SwingUtilities.isLeftToRight(this) ) { // Package private.
      if( getComponentOrientation().isLeftToRight () ) {
        // First determine the x:
        if (position.x+pmSize.width < screenSize.width) {
          x = 0;                     // Prefer extending to right
        } else {
          x = s.width-pmSize.width;  // Otherwise extend to left
        }
      } else {
        // First determine the x:
        if (position.x+s.width < pmSize.width) {
          x = 0;                     // Prefer extending to right
        } else {
          x = s.width-pmSize.width;  // Otherwise extend to left
        }
      }
      // Then the y:
      if (position.y+s.height+pmSize.height < screenSize.height) {
        y = s.height;          // Prefer dropping down
      } else {
        y = 0-pmSize.height;   // Otherwise drop 'up'
        if(y < 0-position.y)
          y = 0-position.y;
      }
    }
    return new Point(x,y);
  }

  public JMenuItem add (JMenuItem item) {
    if (moreMenu != null) {
      // We already have a more menu - add it there.
      return moreMenu.add(item);
    }

    if (getItemCount() < maxItems) {
      // We don't go over the limit - just add it.
      return super.add(item);
    }

    // If we reached here, we reached the limit and we don't have a more menu.
    // Lets create it and add the item there.
    moreMenu = new JLongMenu("More...");

    super.add (moreMenu);
    return moreMenu.add(item);
  }
  
  public void removeAll () {
    if (moreMenu != null) {
      moreMenu.removeAll ();
      moreMenu = null;
    }
    super.removeAll();
  }
}


