
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

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.Point;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.KeyStroke;

/**
 * A JWindow, which can be created without a visible parent, and once shown,
 * dissappears when the user clicks elsewhere (just like a popup menu). This
 * code was originally posted to the java developer forums with the following
 * text:<p>
 *
 * Your big problem is parenting the JWindow. You probably want to create your
 * calendar widget before it's container is visible, which makes it impossible
 * to detemine which java.awt.Window will be the parent of your JWindow. This
 * does not prevent you from showing the JWindow, but parentless Windows (AWT
 * or Swing) get not mouse or keyboard focus, so your calendar widget will not
 * respont to user input. Bummer.<p>
 *
 * Once you have that sorted out, your next problem is preventing all other
 * components in youir application from grabbing mouse focus whilst your
 * widget is visible. This can be done with recursion, but can be problematic
 * if the parenting problem above has not been solved.<p>
 *
 * Once you've figured that out, you have to be able to work out when the user
 * got bored with your widget and clicked on something else, at this point
 * your want to hide your widget. This is actually the same problem as above,
 * essentialy you want to hide your popup under the same circumstances as a
 * JPopupMenu (and hence a JComboBox drop down) gets hidden.<p>
 *
 * @author <a href="http://forum.java.sun.com/profile.jsp?user=167984">dchsw</a>
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @see <a href="http://forum.java.sun.com/thread.jsp?forum=57&thread=230866">
 *       Java Forums: Jcombobox in a JPopupmenu</a>
 */
public class PopupWindow {
  private JWindow mDelegate;
  private Container mContainer;
  private Vector mGrabbed= new Vector();
  private WindowListener mWindowListener;
  private ComponentListener mComponentListener;
  private MouseListener mMouseListener;
  private MouseMotionListener mMouseMotionListener;
  private Action mCancelAction;
  private JComponent mComponent;
  private JComponent mRelative;

  public PopupWindow (Container container) {
    mContainer= container;
    createDelegate();
    createListeners();
    createActions();
  }

  private void createDelegate () {
    Window window= getWindow();
    if (window != null) 
      mDelegate= new JWindow(window);
  }

  public void add (JComponent component)  {
    mComponent= component;
    if (mDelegate != null) {
      component.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put (KeyStroke.getKeyStroke ("ESCAPE"), mCancelAction.getValue (Action.NAME));
      component.getActionMap ()
        .put (mCancelAction.getValue (Action.NAME), mCancelAction);
      mDelegate.getContentPane().add(component);
      mDelegate.pack();
    }
  }

  public void show (Component relative, int x, int y)  {
    if (mDelegate == null) {
      createDelegate();
      if (mDelegate == null) 
        return;
      add(mComponent);
    }
    Point location= relative.getLocationOnScreen();
    mDelegate.setLocation(location.x +x, location.y +y);
    mDelegate.setVisible(true);

    if (relative instanceof JComponent) {
      mRelative = (JComponent)relative;
      mRelative.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put (KeyStroke.getKeyStroke ("ESCAPE"), mCancelAction.getValue (Action.NAME));
      mRelative.getActionMap ()
        .put (mCancelAction.getValue (Action.NAME), mCancelAction); 
    }
    grabContainers();
  }

  public void hide ()  {
    mDelegate.setVisible (false);
    if (mRelative != null) {
      mRelative.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put (KeyStroke.getKeyStroke ("ESCAPE"), "none");
    }
    releaseContainers ();
  }

  private void createListeners () {
    mWindowListener= new WindowListener() {
        public void windowOpened (WindowEvent e) {}
        public void windowClosing (WindowEvent e) { hide (); }
        public void windowClosed (WindowEvent e) { hide (); }
        public void windowIconified (WindowEvent e) { hide (); }
        public void windowDeiconified (WindowEvent e) {}
        public void windowActivated (WindowEvent e) { }
        public void windowDeactivated (WindowEvent e) { }

      };

    mComponentListener= new ComponentListener () {
        public void componentResized (ComponentEvent e) { hide (); }
        public void componentMoved (ComponentEvent e) { hide (); }
        public void componentShown (ComponentEvent e) { hide (); }
        public void componentHidden (ComponentEvent e) { hide (); }
      };


    mMouseListener= new MouseListener () {
        public void mousePressed (MouseEvent e) { hide (); }
        public void mouseReleased (MouseEvent e) { }
        public void mouseEntered (MouseEvent e) { }
        public void mouseExited (MouseEvent e) { }
        public void mouseClicked (MouseEvent e) { }
      };

    mMouseMotionListener= new MouseMotionListener () {
        public void mouseDragged (MouseEvent e) { }
        public void mouseMoved (MouseEvent e) { }
      };
  }

  private void createActions () {
    mCancelAction = new AbstractAction ("cancelPopup") {
        public void actionPerformed (ActionEvent e) {
          hide ();
        }};
  }

  private Window getWindow () {
    Container c= mContainer;
    while(!(c instanceof Window) && c.getParent() != null) 
      c= c.getParent();
    if (c instanceof Window)
      return (Window) c;
    return null;
  }

  private void grabContainers () {
    Container c= mContainer;
    while(!(c instanceof Window) && c.getParent() != null) 
      c= c.getParent();
    grabContainer(c);
  }

  private void grabContainer (Container c) {
    if (c instanceof Window) {
      ((Window)c).addWindowListener(mWindowListener);
      c.addComponentListener(mComponentListener);
      mGrabbed.addElement(c);
    }

    synchronized (c.getTreeLock()) {
      int ncomponents= c.getComponentCount();
      Component[] component= c.getComponents();
      for (int i= 0 ; i < ncomponents ; i++) {
        Component comp= component[i];
        if(!comp.isVisible())
          continue;
        comp.addMouseListener(mMouseListener);
        comp.addMouseMotionListener(mMouseMotionListener);
        mGrabbed.addElement(comp);
        if (comp instanceof Container) {
          Container cont= (Container) comp;
          grabContainer(cont);
        } 
      }
    }
  }

  void releaseContainers () {
    for(int i=0; i < mGrabbed.size(); i++) {
      Component c= (Component) mGrabbed.elementAt(i);
      if(c instanceof Window) {
        ((Window)c).removeWindowListener(mWindowListener);
        ((Window)c).removeComponentListener(mComponentListener);
      } 
      else {
        c.removeMouseListener(mMouseListener);
        c.removeMouseMotionListener(mMouseMotionListener);
      }
    }
    mGrabbed.removeAllElements();
  }
}
