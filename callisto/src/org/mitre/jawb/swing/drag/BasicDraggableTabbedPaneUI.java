
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
 * Created on Dec 13, 2004
 */
package org.mitre.jawb.swing.drag;

import java.awt.*;
import java.awt.dnd.DragSource;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.WeakHashMap;

import javax.swing.*;
import javax.swing.FocusManager;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentInputMapUIResource;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.View;

import org.mitre.jawb.DebugLevel;
import org.mitre.jawb.swing.drag.DraggableTabbedPane.ComponentMenuItem;
import org.mitre.jawb.swing.drag.DraggableTabbedPane.SplitPane;
import org.mitre.jawb.swing.drag.DraggableTabbedPane.TabbedPane;

/**
 * TODO Describe type
 *
 * @author Galen B. Williamson
 * @version Dec 13, 2004
 */
public class BasicDraggableTabbedPaneUI extends BasicTabbedPaneUI {
  
  private final static int DEBUG = DebugLevel.getDebugLevel(BasicDraggableTabbedPaneUI.class, 0);
  
  public class CloseButtonAction extends AbstractAction implements
  MouseInputListener {
    
    protected MouseEvent lastMouseEvent;
    
    public CloseButtonAction() {
      super("close tab", new CloseButtonIcon());
      InputMap im = (InputMap)UIManager.get("Desktop.ancestorInputMap");
      if (im != null) {
        KeyStroke[] allKeys = im.allKeys();
        for (int i = 0; i < allKeys.length; i++) {
          KeyStroke stroke = allKeys[i];
          Object o = im.get(stroke);
          if ("close".equals(o)) {
            putValue(ACCELERATOR_KEY, stroke);
            break;
          }
        }
      }
    }
    
    public void mouseClicked(MouseEvent e) {}
    
    public void mouseEntered(MouseEvent e) {}
    
    public void mouseExited(MouseEvent e) {}
    
    public void mousePressed(MouseEvent e) {}
    
    public void mouseReleased(MouseEvent e) {
      lastMouseEvent = e;
    }
    
    public void mouseDragged(MouseEvent e) {}
    
    public void mouseMoved(MouseEvent e) {}
    
    public void actionPerformed(ActionEvent e) {
      SwingUtilities.invokeLater(createActionHandler(e));
    }
    
    protected ActionHandler createActionHandler(ActionEvent e) {
      return new ActionHandler(e, actionHandler);
    }
    
    private ActionListener actionHandler;
    
    public void setActionHandler(ActionListener l) {
      this.actionHandler = l;
    }
    
    protected class ActionHandler implements Runnable, ActionListener {
      ActionEvent e;
      ActionListener actionHandler;
      protected ActionHandler(ActionEvent e, ActionListener l) {
        this.e = e;
        this.actionHandler = l;
      }
      public void run() {
        if (actionHandler != null)
          actionHandler.actionPerformed(e);
        else
          actionPerformed(e);
      }
      public void actionPerformed(ActionEvent e) {
        int tab = -1;
        Point p = null;
        MouseEvent lastMouseEvent = closeButtonAction.getLastMouseEvent(false);
        if (lastMouseEvent != null) {
          p = lastMouseEvent.getPoint();
          closeButtonAction.getLastMouseEvent(true);
        }
        else {
          Object source = e.getSource();
          if (source instanceof Component) {
            Component comp = (Component) source;
            if (comp instanceof JTabbedPane) {
              tab = ((JTabbedPane) comp).getSelectedIndex();
            }
            else
              p = comp.getLocation();
          }
        }
        if (tab == -1 && p != null) {
          tab = tabForCoordinate(tabPane, p.x, p.y);
        }
        if (tab != -1) {
          Component c = tabPane.getComponentAt(tab);
          if (c != null) {
            c.setEnabled(false);
          }
        }
      }
    }
    
    public MouseEvent getLastMouseEvent(boolean reset) {
      MouseEvent mouseEvent = lastMouseEvent;
      if (reset)
        lastMouseEvent = null;
      return mouseEvent;
    }
    
  }
  
  protected CloseButtonAction createCloseButtonAction() {
    if (closeButtonAction == null) {
      closeButtonAction = new CloseButtonAction();
    }
    return closeButtonAction;
  }
  
  protected CloseButtonAction closeButtonAction;
  
  protected class CloseTabButtonModel extends DefaultButtonModel {
    int tabIndex;
    CloseTabButtonModel(int tabIndex) {
      this.tabIndex = tabIndex;
    }
    protected void reset() {
      stateMask = ENABLED;
    }
    public boolean isVisible() {
      if (isRollover() && ! isDragging) {
        return true;
      }
      if (tabIndex != -1) {
        if (tabPane.getSelectedIndex() == tabIndex || (hoverTab ==  tabIndex && ! isDragging)) {
          return true;
        }
      }
      return false;
    }
    /**
     * @return Returns the tabIndex.
     */
    public int getTabIndex() {
      return tabIndex;
    }
    /**
     * @param tabIndex The tabIndex to set.
     */
    public void setTabIndex(int tabIndex) {
      this.tabIndex = tabIndex;
    }
  }
  
  protected class CloseTabButton extends JButton implements UIResource {
    
    protected final CloseTabButtonModel model;
    
    protected CloseTabButton(int tabIndex) {
      super();
      setAction(createCloseButtonAction());
      super.setModel(model = new CloseTabButtonModel(tabIndex));
      setBorderPainted(false);
      setRolloverEnabled(true);
      setOpaque(false);
      setFocusable(false);
      closeButtonAction = (CloseButtonAction) getAction();
      KeyStroke stroke = (KeyStroke) closeButtonAction.getValue(Action.ACCELERATOR_KEY);
      if (stroke != null) {
        ActionMap am = tabPane.getActionMap();
        InputMap im = tabPane.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        im.put(stroke, "closeTab");
        am.put("closeTab", closeButtonAction);
      }
    }
    
    public String getText() {
      return null;
    }
    
    public void updateUI() {
      super.updateUI();
      super.setUI(new BasicButtonUI());
    }
    
    public void setBounds(int x, int y, int width, int height) {
      if (model != null) {
        model.reset();
      }
      super.setBounds(x, y, width, height);
    }
    
    public String paramString() {
      String s = super.paramString()+",tabIndex="+model.getTabIndex();
      return s;
    }
  }
  
  public class CloseButtonIcon implements Icon, UIResource, Serializable {
    
    public int getIconHeight() {
      Font font = tabPane.getFont();
      FontMetrics fm = tabPane.getFontMetrics(font);
      return Math.max(13, fm.getHeight() - 2);
    }
    
    public int getIconWidth() {
      return getIconHeight();
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
      CloseTabButton b = (CloseTabButton) c;
      CloseTabButtonModel model = (CloseTabButtonModel) b.getModel();
      if (! model.isVisible()) {
        return;
      }
      boolean isRollover = model.isRollover();
      boolean isPressed = model.isPressed();
      boolean isArmed = model.isArmed();
      int w = getIconWidth() - 2;
      int h = getIconHeight() - 2;
      
      if (isPressed && isArmed) {
        x += 1;
        y += 1;
      }
      Polygon poly = makeXPolygon(x, y, w, h);
      
      Color outlineColor = UIManager.getColor("");
      outlineColor = Color.black;
      
      if (isRollover) {
        g.setColor(Color.ORANGE);
      }
      else {
        g.setColor(Color.WHITE);
      }
      
      Graphics2D g2d = null;
      RenderingHints hints = null;
      if (false && g instanceof Graphics2D) {
        g2d = (Graphics2D) g;
        hints = g2d.getRenderingHints();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      }
      
      g.fillPolygon(poly);
      g.setColor(outlineColor);
      g.drawPolygon(poly);
      if (g2d != null && hints != null) {
        g2d.setRenderingHints(hints);
      }
    }
    
    /**
     * @return a hollow X with pointed ends
     */
    private Polygon makeXPolygon(int x, int y, int w, int h) {
      int w4 = (int) Math.floor(w / 4.0);
      int w2 = w4 + w4;
      int corner = w4; // Math.max(3, w4);
      // System.err.println("w: "+w+" corner: "+corner);
      int x0 = x;
      int x1 = x0 + corner;
      int x2 = x0 + (w2) - corner;
      int x3 = x0 + (w2);
      int x4 = x0 + (2 * w2) - corner;
      int x5 = x0 + (2 * w2) + corner - (w2);
      int x6 = x0 + (2 * w2);
      // System.err.println(" x0: "+x0+" x1: "+x1+" x2: "+x2+" x3: "+x3+" x4:
      // "+x4+" x5: "+x5+" x6: "+x6);
      int h4 = (int) Math.floor(h / 4.0);
      int h2 = h4 + h4;
      corner = h4; // Math.max(3, h4);
      int y0 = y;
      int y1 = y0 + corner;
      int y2 = y0 + (h2) - corner;
      int y3 = y0 + (h2);
      int y4 = y0 + (2 * h2) - corner;
      int y5 = y0 + (2 * h2) + corner - (h2);
      int y6 = y0 + (2 * h2);
      Polygon poly = new Polygon();
      poly.addPoint(x0, y1); // 1
      poly.addPoint(x0, y0); // 1 corner
      poly.addPoint(x1, y0); // 2
      poly.addPoint(x3, y2); // 3
      poly.addPoint(x5, y0); // 4
      poly.addPoint(x6, y0); // 4 corner
      poly.addPoint(x6, y1); // 5
      poly.addPoint(x4, y3); // 6
      poly.addPoint(x6, y5); // 7
      poly.addPoint(x6, y6); // 7 corner
      poly.addPoint(x5, y6); // 8
      poly.addPoint(x3, y4); // 9
      poly.addPoint(x1, y6); // 10
      poly.addPoint(x0, y6); // 10 corner
      poly.addPoint(x0, y5); // 11
      poly.addPoint(x2, y3); // 12
      return poly;
    }
    
    /**
     * @return a hollow X with flat ends
     */
    private Polygon makeXPolygon0(int x, int y, int w, int h) {
      int w4 = (int) Math.floor(w / 4.0);
      int w2 = w4 + w4;
      int corner = w4; // Math.max(3, w4);
      // System.err.println("w: "+w+" corner: "+corner);
      int x0 = x;
      int x1 = x0 + corner;
      int x2 = x0 + (w2) - corner;
      int x3 = x0 + (w2);
      int x4 = x0 + (2 * w2) - corner;
      int x5 = x0 + (2 * w2) + corner - (w2);
      int x6 = x0 + (2 * w2);
      // System.err.println(" x0: "+x0+" x1: "+x1+" x2: "+x2+" x3: "+x3+" x4:
      // "+x4+" x5: "+x5+" x6: "+x6);
      int h4 = (int) Math.floor(h / 4.0);
      int h2 = h4 + h4;
      corner = h4; // Math.max(3, h4);
      int y0 = y;
      int y1 = y0 + corner;
      int y2 = y0 + (h2) - corner;
      int y3 = y0 + (h2);
      int y4 = y0 + (2 * h2) - corner;
      int y5 = y0 + (2 * h2) + corner - (h2);
      int y6 = y0 + (2 * h2);
      Polygon poly = new Polygon();
      poly.addPoint(x0, y1); // 1
      poly.addPoint(x1, y0); // 2
      poly.addPoint(x3, y2); // 3
      poly.addPoint(x5, y0); // 4
      poly.addPoint(x6, y1); // 5
      poly.addPoint(x4, y3); // 6
      poly.addPoint(x6, y5); // 7
      poly.addPoint(x5, y6); // 8
      poly.addPoint(x3, y4); // 9
      poly.addPoint(x1, y6); // 10
      poly.addPoint(x0, y5); // 11
      poly.addPoint(x2, y3); // 12
      return poly;
    }
    
  }
  protected class DraggableLayout extends TabbedPaneLayout {
    
    TabbedPaneLayout layout;
    
    Insets buttonInsets = new Insets(3, 3, 3, 3);
    protected DraggableLayout(LayoutManager layout) {
      this.layout = (TabbedPaneLayout) layout;
    }
    
    public void layoutContainer(Container parent) {
      super.layoutContainer(parent);
      layoutButtons(parent);
    }
    
    public Dimension minimumLayoutSize(Container parent) {
      if (parent instanceof TabbedPane) {
        if (((TabbedPane) parent).getTabCount() == 0) {
          return new Dimension(0, 0);
        }
      }
      return super.minimumLayoutSize(parent);
    }

    public Dimension preferredLayoutSize(Container parent) {
      if (parent instanceof TabbedPane) {
        if (((TabbedPane) parent).getTabCount() == 0) {
          return new Dimension(0, 0);
        }
      }
      return super.preferredLayoutSize(parent);
    }

    protected void padSelectedTab(int tabPlacement, int selectedIndex) {
      // Padding gets done by calculateTabWidth
    }
    
    protected void calculateTabRects(int tabPlacement, int tabCount) {
      super.calculateTabRects(tabPlacement, tabCount);
      // create the stylized tab shape
      layoutSelectedTabShape(tabPane.getSelectedIndex());
    }
    
    protected void padTabRun(int tabPlacement, int start, int end, int max) {
      // we only pad the last tab in a run, and only if it is either
      // (1) not in the selected tab run (which is always run 0), or
      // (2) after the selected tab in the selected tab's run
      // This makes all the tabs except the last one in a run take their normal
      // space, and only the last tab gets the extra space.
      if (tabPlacement == TOP) {
        int selectedIndex = tabPane.getSelectedIndex();
        if (start <= selectedIndex && selectedIndex <= end) { // if selected tab is in this run
          if (start < selectedIndex) {
            // if there is a tab before the selected tab in this run, give it
            // some extra room to compensate for the rounded left edge of the
            // selected tab
            rects[selectedIndex - 1].width += round;
          }
          // start padding only after the selected tab
          start = selectedIndex + 1;
          if (start > end) {
            // if there are no tab left to pad...
            return;
          }
        }
        Rectangle lastRect = rects[end];
        int runWidth = (lastRect.x + lastRect.width) - rects[start].x;
        int deltaWidth = max - (lastRect.x + lastRect.width);
        float factor = (float)deltaWidth / (float)runWidth;
        
        for (int j = start; j <= end; j++) {
          Rectangle pastRect = rects[j];
          if (j > start) {
            pastRect.x = rects[j-1].x + rects[j-1].width;
          }
          if (j == end && j != selectedIndex) {
            // don't pad any tabs but the last one in the run, unless it is the selected tab
            pastRect.width += Math.round((float)pastRect.width * factor);
          }
        }
        lastRect.width = max - lastRect.x;
      } else {
        super.padTabRun(tabPlacement, start, end, max);
      }
    }

    public void addLayoutComponent(String name, Component comp) {
      if (DEBUG > 0) {
        tabPane.putClientProperty(comp, name);
      }
      super.addLayoutComponent(name, comp);
    }
    
  }
  
  /**
   * Hit testing shape for the selected tab
   */
  protected Shape selectedTabShape = null;
  /**
   * Rendering shape for the selected tab
   */
  protected Shape selectedTabDrawShape = null;
  
//protected boolean shouldPadTabRun(int tabPlacement, int run) {
//return super.shouldPadTabRun(tabPlacement, run);
//}
  
  public int tabForCoordinate(JTabbedPane pane, int x, int y) {
    // TODO this isn't working when there are multiple runs with 1 tab on top and 2 on the bottom run, when dragging
    int tab = super.tabForCoordinate(pane, x, y);
    int sel = tabPane.getSelectedIndex();
    if (tab != -1) {
      if (tab == sel) {
        int tabCount = pane.getTabCount();
        int run = getRunForTab(tabCount, sel);
        int next = getNextTabIndexInRun(tabCount, sel);
        if (selectedTabShape != null) {
          if (! selectedTabShape.contains(x, y)) {
            if (getRunForTab(tabCount, next) == run && //rects[next].x > x && y >= rects[next].y && y < rects[next].y + rects[next].height) {
                rects[next].x == rects[sel].x + rects[sel].width) {
              tab = next;
            }
            else {
              tab = -1;
            }
          }
        }
      }
//    else if (sel >= 0) { // if (sel >= 0) {
//    if (getTabBounds(pane, sel).contains(x, y))
//    tab = sel;
//    }
    }
    else if (sel != -1) {
      if (selectedTabShape != null) {
        if (selectedTabShape.contains(x, y)) {
          tab = sel;
        }
      }
    }
    return tab;
  }
  
  Insets buttonInsets = new Insets(3, 3, 3, 3);
  int minMaxButtonAreaWidth = -1;
  protected void layoutButtons(Container parent) {
    // TODO make min and max buttons actual child components
    AbstractButton min = draggableTabbedPane.minButton;
    AbstractButton max = draggableTabbedPane.maxButton;
    if (min != null || max != null) {
      Insets tabInsets = getSelectedTabInsets();
      int tabPlacement = draggableTabbedPane.getTabPlacement();
      Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
      int fontHeight = getTabFontHeight() - 4 - tabInsets.top - tabInsets.bottom;
      int top = calculatedTabAreaInsets.top;
      fontHeight = Math.min(fontHeight, lastTabAreaHeight - top - tabAreaInsets.bottom - 2);
      Dimension size = parent.getSize();
      int right = calculatedTabAreaInsets.left + size.width - calculatedTabAreaInsets.right /*- tabInsets.right*/;
      int left = right;
      if (max != null) {
        max.getInsets(buttonInsets);
        int height = fontHeight + buttonInsets.top + buttonInsets.bottom;
        int width = height; //9 * height / 10;
        left -= width;
        switch (tabPlacement) {
        case TOP:
          max.setBounds(left, top, width, height);
          break;
        case BOTTOM:
          max.setBounds(left, size.height - tabAreaInsets.bottom - height, width, height);
          break;
        }
        left -= width + buttonInsets.left; //tabInsets.left + tabInsets.right;
      }
      if (min != null) {
        min.getInsets(buttonInsets);
        int height = fontHeight + buttonInsets.top + buttonInsets.bottom;
        int width = height; //9 * height / 10;
        switch (tabPlacement) {
        case TOP:
          min.setBounds(left, top, width, height);
          break;
        case BOTTOM:
          min.setBounds(left, size.height - tabAreaInsets.bottom - height, width, height);
          break;
        }
      }
      minMaxButtonAreaWidth = left - right;
    }

    Component[] pcomps = tabPane.getComponents();
    for (int i = 0; i < pcomps.length; i++) {
      Component c = pcomps[i];
      if (c instanceof CloseTabButton) {
        tabPane.remove(c);
      }
    }
    // layout all the close tab buttons
    for (int i = 0; i < tabPane.getTabCount(); i++) {
      Component comp = tabPane.getComponentAt(i);
      if (comp == null)
        continue;
      CloseTabButton close = (CloseTabButton) draggableTabbedPane.getTabComponent(i, CLOSE_TAB_BUTTON);
      if (close == null) {
        close = new CloseTabButton(i);
      }
      else if (close.model.getTabIndex() != i) {
        close.model.setTabIndex(i);
      }
      if (close.getParent() != tabPane)
        draggableTabbedPane.add(close, CLOSE_TAB_BUTTON, i);
      layoutCloseButton(close);
      if (comp instanceof JComponent) {
        final JComponent jc = (JComponent) comp;
        final CloseTabButton cb = close;
        jc.addAncestorListener(new AncestorListener() {

          public void ancestorAdded(AncestorEvent event) {}

          public void ancestorRemoved(AncestorEvent event) {
            if (event.getAncestor() == tabPane && ! tabPane.isAncestorOf(jc)) {
              tabPane.remove(cb);
              jc.removeAncestorListener(this);
            }
          }

          public void ancestorMoved(AncestorEvent event) {}
          
        });
      }
    }
  }
  
  protected void layoutCloseButton(CloseTabButton close) {
    if (close != null) {
      int tabIndex = close.model.getTabIndex();
      if (tabIndex != -1 && tabIndex < tabPane.getTabCount()) {
        int selectedIndex = tabPane.getSelectedIndex();
        boolean isSelected = selectedIndex == tabIndex;
        int tabPlacement = tabPane.getTabPlacement();
        FontMetrics metrics = getFontMetrics();
        String title = tabPane.getTitleAt(tabIndex);
        Rectangle closeIconRect = new Rectangle(),
        iconRect = new Rectangle(),
        textRect = new Rectangle(),
        tabRect = new Rectangle(rects[tabIndex]);
        Icon icon = tabPane.getIconAt(tabIndex);
        
        int oldWidth = rects[tabIndex].width;
        int oldX = rects[tabIndex].x;
        
        layoutLabel(tabPlacement, metrics, tabIndex, title, icon,
            tabRect, iconRect, textRect, isSelected);
        Icon closeIcon = close.getIcon();
        
        int w = closeIcon.getIconWidth();
        int h = closeIcon.getIconHeight();
        
        rects[tabIndex].width = tabRect.width;
        rects[tabIndex].height = tabRect.height;
        rects[tabIndex].x = tabRect.x;
        rects[tabIndex].y = tabRect.y;
        
        int ydiff = iconRect.y - tabRect.y;
        ydiff += iconRect.height / 2;
        ydiff -= h / 2;
        ydiff -= 3 - (textRect.y - tabRect.y); // fudge?
        if (isSelected)
          ydiff -= 1;
        int xdiff = iconRect.x - tabRect.x;
        int tabCount = tabPane.getTabCount();
        int run = getRunForTab(tabCount, tabIndex);
        boolean firstInRun = tabRuns[run] == tabIndex;
        if (runCount > 1 && run < runCount - 1) {
          firstInRun = false;
        }
        boolean isRound = isSelected || firstInRun;
        if (isRound) {
          xdiff -= round;
        }
        if (true) {
          closeIconRect.x = textRect.x + textRect.width + (textRect.x - (iconRect.x + iconRect.width));
          if (closeIconRect.x > rects[tabIndex].x + rects[tabIndex].width) {
            rects[tabIndex].width = closeIconRect.x - rects[tabIndex].x + w + xdiff + 1;
          }
        }
        else {
          // this doesn't work so well
          int tw = isSelected ? calculateFocusIndicatorWidth(rects[tabIndex]) : rects[tabIndex].width;
          closeIconRect.x = rects[tabIndex].x + tw - w - xdiff + 1;
          if (isRound)
            closeIconRect.x += round;
          if (closeIconRect.x - (textRect.x + textRect.width) < 2 * textIconGap) {
            closeIconRect.x = (textRect.x + textRect.width) + 2 * textIconGap;
            int twd = 0;
            if (isSelected)
              twd = rects[tabIndex].width - calculateFocusIndicatorWidth(rects[tabIndex]);
            rects[tabIndex].width = //round + calculateFocusIndicatorWidth(rects[tabIndex]) - w - xdiff + 1 + w;
              closeIconRect.x - rects[tabIndex].x + xdiff + twd - 1;
          }
        }
        int xDelta = rects[tabIndex].x - oldX;
        int widthDelta = rects[tabIndex].width - oldWidth;
        if (widthDelta > 0 || xDelta > 0) {
          // if we've changed the width or x position of this tab, fix up the
          // positions and widths of the remaining tabs in the run
          int lastTabInRun = lastTabInRun(tabCount, run);
          for (int i = tabIndex + 1; i < rects.length; i++) {
            if (getRunForTab(tabCount, i) == run && (runCount == 1 || i != selectedIndex)) {
              // the tab is in the same run and either it's the only run or it
              // is not the selected tab
              rects[i].x = rects[i - 1].x + rects[i - 1].width;
            }
          }
        }
        closeIconRect.y = tabRect.y + ydiff;
        closeIconRect.width = w;
        closeIconRect.height = h;
        close.setBounds(closeIconRect.x, closeIconRect.y, w, h);
        
        if (isSelected) {
          // make sure selected tab's shapes are accurate
          layoutSelectedTabShape(selectedIndex);
        }
      }
      else {
        close.getParent().remove(close);
      }
    }
  }
  
  protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon, Rectangle tabRect, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    title = debugTitle(tabIndex, title);
    if (false) {
      super.layoutLabel(tabPlacement,
          metrics,
          tabIndex,
          title,
          icon,
          tabRect,
          iconRect,
          textRect,
          isSelected);
      return;
    }
    final int h = metrics.getHeight();
    final Icon originalIcon = icon;
    if (isSelected) {
      icon = new Icon() {
        
        public int getIconHeight() {
          if (originalIcon != null) {
            return Math.max(h + 4, originalIcon.getIconHeight() + 4);
          }
          return h + 4;
        }
        
        public int getIconWidth() {
          if (originalIcon != null) {
            return originalIcon.getIconWidth();
          }
          return 0;
        }
        
        public void paintIcon(Component c, Graphics g, int x, int y) {}
        
      };
    }
    if (false) {
      super.layoutLabel(tabPlacement,
          metrics,
          tabIndex,
          title,
          icon,
          tabRect,
          iconRect,
          textRect,
          isSelected);
    }
    else {
      textRect.x = textRect.y = iconRect.x = iconRect.y = 0;
      
      View v = getTextViewForTab(tabIndex);
      if (v != null) {
        tabPane.putClientProperty("html", v);
      }
      
      SwingUtilities.layoutCompoundLabel((JComponent) tabPane,
          metrics, title, icon,
          SwingUtilities.CENTER,
          SwingUtilities.LEADING,
          SwingUtilities.CENTER,
          SwingUtilities.TRAILING,
          tabRect,
          iconRect,
          textRect,
          textIconGap);
      
      tabPane.putClientProperty("html", null);
      
      int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
      int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
      iconRect.x += xNudge;
      iconRect.y += yNudge;
      textRect.x += xNudge;
      textRect.y += yNudge;
    }
    textRect.x += round + textIconGap * 2;
    iconRect.x += round + textIconGap * 2;
    if (isSelected)
      iconRect.y += 2;
    CloseTabButton button = (CloseTabButton) draggableTabbedPane.getTabComponent(tabIndex, CLOSE_TAB_BUTTON);
    if (button != null) {
      Icon closeIcon = button.getIcon();
      if (icon != null) {
        textRect.height = Math.max(textRect.height, closeIcon.getIconHeight() + 4);
        if (isSelected)
          textRect.y += 1;
        tabRect.height = Math.max(tabRect.height, closeIcon.getIconHeight() + 8);
        tabRect.width = Math.max(tabRect.width, textRect.width + textRect.x - tabRect.x);
        if (isSelected) {
          int ih = tabRect.height - Math.max(iconRect.height, originalIcon == null ? 0 : originalIcon.getIconHeight());
          int d = ih * 2 - 1;
          iconRect.y = Math.max(tabRect.y + d, iconRect.y + d);
        }
      }
    }
  }
  
  private String debugTitle(int tabIndex, String title) {
    if (title == null)
      return null;
    if (DEBUG > 0) {
      title = tabPane.getTitleAt(tabIndex);
      title = title.substring(0, title.length() - 1) + tabIndex;
    }
    return title;
  }
  
  protected void paintFocusIndicator(Graphics g, int tabPlacement,
      Rectangle[] rects, int tabIndex,
      Rectangle iconRect, Rectangle textRect,
      boolean isSelected) {
    Rectangle tabRect = rects[tabIndex];
    if (tabPane.hasFocus() && isSelected) {
      int x, y, w, h;
      g.setColor(focus);
      switch(tabPlacement) {
      case LEFT:
        x = tabRect.x + 3;
        y = tabRect.y + 3;
        w = tabRect.width - 5;
        h = tabRect.height - 6;
        break;
      case RIGHT:
        x = tabRect.x + 2;
        y = tabRect.y + 3;
        w = tabRect.width - 5;
        h = tabRect.height - 6;
        break;
      case BOTTOM:
        x = tabRect.x + 3;
        y = tabRect.y + 2;
        w = tabRect.width - 6;
        h = tabRect.height - 5;
        break;
      case TOP:
      default: {
        x = tabRect.x + round;
        y = tabRect.y + 3;
        w = calculateFocusIndicatorWidth(tabRect);
        h = tabRect.height - 4;
      }
      }
      BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
    }
  }
  
  protected int calculateFocusIndicatorWidth(Rectangle tabRect) {
    return tabRect.width - 3 - round - 25;
  }
  
  protected static final int FLOAT = 0;
  protected static final int NONE = 0;
  protected static final int NORTH = 1 << 0;
  protected static final int SOUTH = 1 << 1;
  protected static final int WEST = 1 << 2;
  protected static final int EAST = 1 << 3;
  
  protected static final int NORTHEAST = NORTH | EAST;
  protected static final int NORTHWEST = NORTH | WEST;
  protected static final int SOUTHEAST = SOUTH | EAST;
  protected static final int SOUTHWEST = SOUTH | WEST;
  
  protected static final int STATE_NONE = 0;
  protected static final int STATE_FLOATING = 1 << 0;
  protected static final int STATE_FLOATED = STATE_FLOATING | 1 << 1;
  protected static final int STATE_DOCKING = 1 << 2;
  protected static final int STATE_DOCKED = STATE_DOCKING | 1 << 3;
  protected static final int DRAG_FRAME_SIZE = 5;
  
  DraggableTabbedPane rootPane;
  DraggableTabbedPane.TabbedPane draggableTabbedPane;
  
  Point draggingTabPaneOrigin = new Point();
  Point globalOrigin = null;
  boolean isDragging = false;
  boolean isMoving = false;
  boolean isSizing = false;
  boolean isInside = false;
  Point offset = new Point();
  Point lastPressPosition = new Point();
  
  private WeakHashMap draggableStateTable = new WeakHashMap();
  int pressedTabIndex = -1;
  Component pressedTabComponent = null;
  Cursor savedCursor = null;
  
  protected class CancelDragKeyHandler implements KeyListener {
    
    public void keyPressed(KeyEvent e) {}
    
    public void keyReleased(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        Component c = (Component) e.getSource();
        isDragging = isMoving = isSizing = false;
        endDrag();
        if (pressedTabComponent != null) {
          draggableStateTable.remove(pressedTabComponent);
          pressedTabIndex = -1;
        }
        c.removeKeyListener(this);
      }
    }
    
    public void keyTyped(KeyEvent e) {}
    
  }
  
  protected int hoverTab = -1;
  protected Point hoverPoint = null;
  
  public class DragMouseHandler extends MouseHandler implements MouseInputListener {
    protected class CloseAllTabsAction extends AbstractAction {
      protected CloseAllTabsAction() {
        super("Close All Tabs in Pane");
      }

      public void actionPerformed(ActionEvent e) {
        if (pressedTabComponent != null &&
            tabPane.indexOfComponent(pressedTabComponent) == pressedTabIndex) {
          for (int i = tabPane.getTabCount() - 1; i >= 0; --i) {
            tabPane.getComponentAt(i).setEnabled(false);
          }
        }
      }
    }

    protected class CloseOtherTabsAction extends AbstractAction {
      protected CloseOtherTabsAction() {
        super("Close Others in Pane");
      }

      public void actionPerformed(ActionEvent e) {
        if (pressedTabComponent != null &&
            tabPane.indexOfComponent(pressedTabComponent) == pressedTabIndex) {
          for (int i = tabPane.getTabCount() - 1; i >= 0; --i) {
            if (i != pressedTabIndex)
              tabPane.getComponentAt(i).setEnabled(false);
          }
        }
      }
    }

    protected class ClosePressedTabAction extends AbstractAction {
      protected ClosePressedTabAction() {
        super("Close Tab");
      }

      public void actionPerformed(ActionEvent e) {
        if (pressedTabComponent != null &&
            tabPane.indexOfComponent(pressedTabComponent) == pressedTabIndex) {
          pressedTabComponent.setEnabled(false);
        }
      }
    }

    boolean lastPressWasPopupTrigger = false;
    boolean lastPressWasShifted = false;
    CancelDragKeyHandler cancelDragHandler = new CancelDragKeyHandler();
    protected ClosePressedTabAction closePressedTabAction = new ClosePressedTabAction();
    protected CloseOtherTabsAction closeOtherTabsAction = new CloseOtherTabsAction();
    protected CloseAllTabsAction closeAllTabsAction = new CloseAllTabsAction();
    
    public void mouseMoved(MouseEvent e) {
      checkHoverTab(e.getX(), e.getY());
    }
    
    public void mouseEntered(MouseEvent e) {
      checkHoverTab(e.getX(), e.getY());
    }
    
    public void mouseExited(MouseEvent e) {
      hoverPoint = null;
      checkHoverTab(-1, -1);
    }
    
    public void mouseDragged(MouseEvent e) {
      if (e.isPopupTrigger() || lastPressWasPopupTrigger)
        return;
      Object s = e.getSource();
      if (! (s instanceof Component))
        return;
      Component source = (Component) s;
      Point position = e.getPoint();
//    if (isDragging && !isSizing && dragComponent != null && ! dragComponent.isVisible()) {
//    // don't show dragComponent until it has been dragged a bit
//    Rectangle rect = getTabBounds(tabPane, pressedTabIndex);
//    if (true || ! rect.contains(position)
//    || Math.abs(position.x - offset.x) > rect.getWidth() / 10.0
//    || Math.abs(position.y - offset.y) > rect.getHeight() / 2.0) {
//    dragComponent.setVisible(true);
////  savedCursor = rootPane.getCursor();
//    Container window = rootPane.getTopLevelAncestor();
//    JRootPane jRootPane = (window instanceof JComponent) ?
//    ((JComponent) window).getRootPane() :
//    rootPane.getRootPane();
//    savedCursor = jRootPane.getGlassPane().getCursor();
////  savedCursor = tabPane.getCursor();
//    
////  jRootPane.getGlassPane().setCursor(null);
////  jRootPane.getGlassPane().setVisible(true);
////  rootPane.setCursor(DragSource.DefaultMoveDrop);
//    }
//    }
      if (! isDragging && ! isSizing) {
        if (sizeState != NONE) {
          isDragging = true;
          isSizing = true;
          if (dragComponent != null) {
            dragComponent.setVisible(false);
            dragComponent = null;
          }
        }
        else if (pressedTabComponent != null && rootPane != null) {
          isDragging = true;
          isMoving = true;
          globalOrigin = source.getLocationOnScreen();
          Point rootOrigin = rootPane.getLocationOnScreen();
          draggingTabPaneOrigin.x = globalOrigin.x - rootOrigin.x;
          draggingTabPaneOrigin.y = globalOrigin.y - rootOrigin.y;
          offset.setLocation(position);
          dragComponent = dragFloatingComponent;
          dragComponent.setVisible(false);
        }
      }
//    else if (isSizing) {
//    sizeTo(source, position);
//    }
      else { //if (isMoving) {
        dragTo(pressedTabComponent, position);
        rootPane.repaint();
      }
      if (isDragging && !isSizing && dragComponent != null && ! dragComponent.isVisible()) {
        Component focusOwner = FocusManager.getCurrentManager().getFocusOwner();
        if (focusOwner != null)
          focusOwner.addKeyListener(cancelDragHandler);
        // don't show dragComponent until it has been dragged a bit
        Rectangle rect = getTabBounds(tabPane, pressedTabIndex);
        if (! rect.contains(position)
            || Math.abs(position.x - lastPressPosition.x) > rect.getWidth() / 7.5
            || Math.abs(position.y - lastPressPosition.y) >
            rect.getHeight() / 4.0
        ) {
          dragComponent.setVisible(true);
          Container window = rootPane.getTopLevelAncestor();
          JRootPane jRootPane = (window instanceof JComponent) ?
              ((JComponent) window).getRootPane() :
                rootPane.getRootPane();
              savedCursor = jRootPane.getGlassPane().getCursor();
//            savedCursor = rootPane.getCursor();
//            Container window = rootPane.getTopLevelAncestor();
//            JRootPane jRootPane = (window instanceof JComponent) ?
//            ((JComponent) window).getRootPane() :
//            rootPane.getRootPane();
//            savedCursor = jRootPane.getGlassPane().getCursor();
//            savedCursor = tabPane.getCursor();
              
//            jRootPane.getGlassPane().setCursor(null);
//            jRootPane.getGlassPane().setVisible(true);
//            rootPane.setCursor(DragSource.DefaultMoveDrop);
        }
      }
    }
    
//  private HashMap savedCursors = new HashMap();
    
//  public void mouseMoved(MouseEvent e) {
//  if (true)
//  return;
//  if (e.isPopupTrigger() || lastPressWasPopupTrigger)
//  return;
//  Object s = e.getSource();
//  if (! (s instanceof Component))
//  return;
//  Component source = (Component) s;
//  Point position = e.getPoint();
//  if (! isDragging) {
//  int save = sizeState;
//  if ((sizeState = checkSizable(source.getSize(), position)) != NONE) {
//  if (sizeState != save) {
//  int cursor = Cursor.DEFAULT_CURSOR;
//  switch (sizeState) {
//  case NORTH: cursor = Cursor.N_RESIZE_CURSOR; break;
//  case SOUTH: cursor = Cursor.S_RESIZE_CURSOR; break;
//  case WEST:  cursor = Cursor.W_RESIZE_CURSOR; break;
//  case EAST:  cursor = Cursor.E_RESIZE_CURSOR; break;
//  case NORTHEAST: cursor = Cursor.NE_RESIZE_CURSOR; break;
//  case NORTHWEST: cursor = Cursor.NW_RESIZE_CURSOR; break;
//  case SOUTHEAST: cursor = Cursor.SE_RESIZE_CURSOR; break;
//  case SOUTHWEST: cursor = Cursor.SW_RESIZE_CURSOR; break;
//  }
//  savedCursors.put(source, source.getCursor());
//  source.setCursor(Cursor.getPredefinedCursor(cursor));
//  }
//  }
//  else {
//  if (sizeState != save) {
//  Cursor cursor = (Cursor) savedCursors.remove(source);
//  if (cursor == null)
//  cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
//  source.setCursor(cursor);
//  }
//  }
//  }

    public void mousePressed(MouseEvent e) {
      if (DEBUG > 0)
        System.err.println("pressed: lastPressWasPopupTrigger: "+lastPressWasPopupTrigger+" current is popup trigger:  "+e.isPopupTrigger()+"\n"+e);
      // TODO popup trigger clicked on tab displays menu of tabs
      lastPressWasPopupTrigger = e.isPopupTrigger() || !SwingUtilities.isLeftMouseButton(e);
      lastPressWasShifted = e.isAltDown() || e.isShiftDown();
      if (tabPane == null || !tabPane.isEnabled()) {
        return;
      }
      lastPressPosition.setLocation(e.getPoint());
      if (!lastPressWasPopupTrigger) {
        if (isDragging) {
          if (isMoving || isSizing) {
            if (dragComponent != null) {
              dragComponent.setVisible(false);
              dragComponent = null;
            }
          }
        }
      }
      isDragging = false;
      isMoving = false;
      isSizing = false;
      
      pressedTabComponent = null;
      int tabIndex = pressedTabIndex = tabForCoordinate(tabPane, e.getX(), e.getY());
      
      if (pressedTabIndex >= 0 && tabPane.isEnabledAt(pressedTabIndex)) {
        if (draggableTabbedPane.detachedPanelIndex == pressedTabIndex
            && draggableTabbedPane.detachedPanelList != null) {
          pressedTabIndex = -1;
        }
        else {
          pressedTabComponent = tabPane.getComponentAt(pressedTabIndex);
        }
      }
      if (tabIndex >= 0 && tabPane.isEnabledAt(tabIndex)) {
        boolean changeTab = tabIndex != tabPane.getSelectedIndex();
        boolean wasFocused = isTabPaneFocusAncestor;
        if (changeTab) {
          tabPane.setSelectedIndex(tabIndex);
        }
        if (wasFocused && ! changeTab && tabPane.isRequestFocusEnabled()) {
          tabPane.requestFocus();
        }
        else if (pressedTabComponent != null
            && pressedTabComponent instanceof JScrollPane) {
          final Component target = ((JScrollPane) pressedTabComponent).getViewport().getView();
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() != target) {
                target.requestFocus();
                tabPane.repaint(/*getTabBounds(tabPane, tabIndex)*/);
              }
            }
          });
        }
        else if (pressedTabComponent != null) {
          pressedTabComponent.requestFocus();
        }
        else if (tabPane.isRequestFocusEnabled()) {
          tabPane.requestFocus();
        }
        tabPane.repaint(/*getTabBounds(tabPane, tabIndex)*/);
      }
      
      if (pressedTabComponent != null) {
        draggableStateTable.remove(pressedTabComponent);
        pressedTabIndex = tabPane.indexOfComponent(pressedTabComponent);
      }
    }
    
    public void mouseReleased(MouseEvent e) {
      // TODO preserve mnemonics of dropped tabs
      if (DEBUG > 0)
        System.err.println("released: lastPressWasPopupTrigger: "+lastPressWasPopupTrigger+" current is popup trigger:  "+e.isPopupTrigger()+"\n"+e);
      if (e.isPopupTrigger() || lastPressWasPopupTrigger) {
        mousePressed(e);
        if (pressedTabIndex != -1 && pressedTabComponent != null) {
          JMenu menu = new JMenu(tabPane.getTitleAt(pressedTabIndex));
          menu.add(new JMenuItem(closePressedTabAction));
          menu.add(new JMenuItem(closeOtherTabsAction));
          menu.add(new JMenuItem(closeAllTabsAction));
          menu.add(new JSeparator());
          rootPane.fillComponentsMenu(lastPressWasShifted);
          Component[] components = rootPane.getComponentsMenu().getMenuComponents();
          for (int i = 0; i < components.length; i++) {
            Component c = components[i];
            if (c instanceof ComponentMenuItem) {
              ComponentMenuItem mi = (ComponentMenuItem) c;
              if (lastPressWasShifted || mi.c != pressedTabComponent) {
                menu.add(mi);
              }
            }
          }
          if (DEBUG > 0) {
            JMenu dm = new JMenu("Debug");
            menu.add(dm);
            components = tabPane.getComponents();
            for (int i = 0; i < components.length; i++) {
              final Component comp = components[i];
              final Object constraints = tabPane.getClientProperty(comp);
              String text = (i+1) + " " + comp.getBounds()+", "+constraints + ": " + comp;
              text = "<html><body><div width="+(Toolkit.getDefaultToolkit().getScreenSize().width / 2)+">"+text+"</div></body></html>";
              AbstractAction action = new AbstractAction(text) {

                public void actionPerformed(ActionEvent e) {
                  if (comp.getParent() == tabPane) {
                    tabPane.remove(comp);
                  }
                }
                
              };
              JMenuItem mi = new JMenuItem(action);
              dm.add(mi);
            }
          }
          JPopupMenu popup = menu.getPopupMenu();
          popup.show(tabPane, e.getX(), e.getY());
          rootPane.fillComponentsMenu(false);
        }
        return;
      }
      if (isDragging && pressedTabIndex != -1) {
        if (isMoving || isSizing) {
          
          Object s = e.getSource();
          if (s instanceof DraggableTabbedPane.TabbedPane) {
            DraggableTabbedPane.TabbedPane source = (DraggableTabbedPane.TabbedPane) s;
            if (isMoving) {
              Component dragged = pressedTabComponent;
              DraggableState dragState = (DraggableState) draggableStateTable.remove(dragged);
              
              // is there a drag state and was a valid tab pressed?
              if (dragState != null && pressedTabIndex >= 0) {
                Container target = rootPane.getPaneAtLocation(dragState.lastMousePosition.x, dragState.lastMousePosition.y);
                
                target = dragState.tabPaneTarget;
                if (dragState.rootPaneTarget != null) {
                  target = dragState.rootPaneTarget;
                }
                else if (dragState.splitPaneTarget != null) {
                  target = dragState.splitPaneTarget;
                  SplitPane targetSplitPane = (SplitPane) target;
                  Component targetComponent = null;
                  switch (dragState.dock) {
                  case NORTH:
                    targetComponent = targetSplitPane.getTopComponent();
                    break;
                  case SOUTH:
                    targetComponent = targetSplitPane.getBottomComponent();
                    break;
                  case WEST:
                    targetComponent = targetSplitPane.getLeftComponent();
                    break;
                  case EAST:
                    targetComponent = targetSplitPane.getRightComponent();
                    break;
                  default:
                    targetComponent = targetSplitPane.getLeftComponent();
                  }
                  if (targetComponent == null) {
                    targetComponent = rootPane.createTabbedPane();
                    switch (dragState.dock) {
                    case NORTH: case WEST:
                      targetSplitPane.setLeftComponent(targetComponent);
                      break;
                    case SOUTH: case EAST:
                      targetSplitPane.setRightComponent(targetComponent);
                      break;
                    }
                  }
                  dragState.dock = FLOAT;
                  final SplitPane sp = targetSplitPane;
                  SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                      sp.setDividerLocation(DraggableTabbedPane.ONE_HALF);
                    }
                  });
                  if (targetComponent instanceof Container)
                    target = (Container) targetComponent;
                  else
                    target = null;
                }
                // is the Container mouse released over (drag dropped into) a DraggableTabbedPane.TabbedPane?
                if (target instanceof TabbedPane) {
                  DraggableTabbedPane.TabbedPane targetTabPane = (DraggableTabbedPane.TabbedPane) target;
                  int tabIndex = targetTabPane.ui
                  .tabForCoordinate(targetTabPane,
                      dragState.targetMousePos.x,
                      dragState.targetMousePos.y);
                  
                  // is the drop over a valid tab?
                  if (tabIndex >= 0) {
                    
                    // is the drop tab in the same tab pane as the dragged tab is coming from?
                    if (targetTabPane == tabPane) {
                      if (tabIndex != pressedTabIndex) {
                        Component insertBefore = tabPane.getComponentAt(tabIndex);
                        
                        // is the drop over a different tab?
                        if (insertBefore != pressedTabComponent) {
                          pressedTabIndex = tabPane.indexOfComponent(pressedTabComponent);
                          if (pressedTabIndex >= 0 && pressedTabIndex < tabPane.getTabCount()) {
                            String title = tabPane.getTitleAt(pressedTabIndex);
                            Icon icon = ((DraggableTabbedPane.TabbedPane) tabPane).getUserIcon(pressedTabComponent);//getIconAt(pressedTabIndex);
                            String tip = tabPane.getToolTipTextAt(pressedTabIndex);
                            int mnemonic = tabPane.getMnemonicAt(pressedTabIndex);
                            int fromIndex =  tabPane.indexOfComponent(pressedTabComponent);
                            int insertIndex = tabPane.indexOfComponent(insertBefore);
                            if (fromIndex < insertIndex) {
                              insertIndex++;
                              if (insertIndex < tabPane.getTabCount())
                                insertBefore = tabPane.getComponentAt(insertIndex);
                              else
                                insertBefore = null;
                            }
                            tabPane.remove(pressedTabComponent);
                            if (insertBefore == null)
                              insertIndex = tabPane.getTabCount();
                            else
                              insertIndex = tabPane.indexOfComponent(insertBefore);
                            tabPane.insertTab(title, icon, pressedTabComponent, tip, insertIndex);
                            int insertedIndex = tabPane.indexOfComponent(pressedTabComponent);
                            if (insertedIndex != -1) {
                              tabPane.setMnemonicAt(insertedIndex, mnemonic);
                              tabPane.setSelectedIndex(insertedIndex);
                            }
                          }
                        }
                      }
                    }
                    else { // tabTarget != tabPane
                      Component insertBefore = targetTabPane.getComponentAt(tabIndex);
                      pressedTabIndex = tabPane.indexOfComponent(pressedTabComponent);
                      if (pressedTabIndex >= 0 && pressedTabIndex < tabPane.getTabCount()) {
                        String title = tabPane.getTitleAt(pressedTabIndex);
                        int mnemonic = tabPane.getMnemonicAt(pressedTabIndex);
                        Icon icon = ((DraggableTabbedPane.TabbedPane) tabPane).getUserIcon(pressedTabComponent);//getIconAt(pressedTabIndex);
                        String tip = tabPane.getToolTipTextAt(pressedTabIndex);
                        tabPane.remove(pressedTabComponent);
                        int insertIndex = targetTabPane.indexOfComponent(insertBefore);
                        targetTabPane.insertTab(title, icon, pressedTabComponent, tip, insertIndex);
                        targetTabPane.setSelectedComponent(pressedTabComponent);
                        int insertedIndex = targetTabPane.indexOfComponent(pressedTabComponent);
                        if (insertedIndex != -1) {
                          targetTabPane.setMnemonicAt(insertedIndex, mnemonic);
                        }
                      }
                    }
                  }
                  else if (dragState.dock != FLOAT) {
                    // split the target pane
                    // TODO use the weights when splitting the target pane
                    int orientation = DraggableTabbedPane.HORIZONTAL_SPLIT;
                    String anchor = null;
                    double weight = 0.5;
                    switch (dragState.dock) {
                    case NORTH:
                      orientation = DraggableTabbedPane.VERTICAL_SPLIT;
                      anchor = DraggableTabbedPane.TOP;
                      weight = DraggableTabbedPane.ONE_THIRD;
                      break;
                    case SOUTH:
                      orientation = DraggableTabbedPane.VERTICAL_SPLIT;
                      anchor = DraggableTabbedPane.BOTTOM;
                      weight = DraggableTabbedPane.TWO_THIRDS;
                      break;
                    case WEST:
                      orientation = DraggableTabbedPane.HORIZONTAL_SPLIT;
                      anchor = DraggableTabbedPane.LEFT;
                      weight = DraggableTabbedPane.ONE_THIRD;
                      break;
                    case EAST:
                      orientation = DraggableTabbedPane.HORIZONTAL_SPLIT;
                      anchor = DraggableTabbedPane.RIGHT;
                      weight = DraggableTabbedPane.TWO_THIRDS;
                      break;
                    }
                    if (anchor != null) {
                      rootPane.splitTabbedPane(targetTabPane, source, pressedTabIndex, orientation, anchor, weight);
                    }
                  }
                  else if (targetTabPane != tabPane) { // tabIndex < 0
                    pressedTabIndex = tabPane.indexOfComponent(pressedTabComponent);
                    if (pressedTabIndex >= 0 && pressedTabIndex < tabPane.getTabCount()) {
                      String title = tabPane.getTitleAt(pressedTabIndex);
                      Icon icon = ((DraggableTabbedPane.TabbedPane) tabPane).getUserIcon(pressedTabComponent);//getIconAt(pressedTabIndex);
                      String tip = tabPane.getToolTipTextAt(pressedTabIndex);
                      int mnemonic = tabPane.getMnemonicAt(pressedTabIndex);
                      tabPane.remove(pressedTabComponent);
                      int insertIndex = targetTabPane.getTabCount();
                      targetTabPane.insertTab(title, icon, pressedTabComponent, tip, insertIndex);
                      targetTabPane.setSelectedComponent(pressedTabComponent);
                      int insertedIndex = targetTabPane.indexOfComponent(pressedTabComponent);
                      if (insertedIndex != -1) {
                        targetTabPane.setMnemonicAt(insertedIndex, mnemonic);
                      }
                    }
                  }
                }
                else if (target == rootPane) {
                  // handle drop in root pane
                  // TODO use the weights when splitting the root pane
                  int orientation = DraggableTabbedPane.HORIZONTAL_SPLIT;
                  String anchor = null;
                  double weight = 0.5;
                  switch (dragState.dock) {
                  case NORTH:
                    orientation = DraggableTabbedPane.VERTICAL_SPLIT;
                    anchor = DraggableTabbedPane.TOP;
                    weight = DraggableTabbedPane.ONE_THIRD;
                    break;
                  case SOUTH:
                    orientation = DraggableTabbedPane.VERTICAL_SPLIT;
                    anchor = DraggableTabbedPane.BOTTOM;
                    weight = DraggableTabbedPane.TWO_THIRDS;
                    break;
                  case WEST:
                    orientation = DraggableTabbedPane.HORIZONTAL_SPLIT;
                    anchor = DraggableTabbedPane.RIGHT;
                    weight = DraggableTabbedPane.ONE_THIRD;
                    break;
                  case EAST:
                    orientation = DraggableTabbedPane.HORIZONTAL_SPLIT;
                    anchor = DraggableTabbedPane.LEFT;
                    weight = DraggableTabbedPane.TWO_THIRDS;
                    break;
                  }
                  if (anchor != null) {
                    rootPane.splitRootPane(source, pressedTabIndex, orientation, anchor, weight);
                  }
                }
                if (pressedTabComponent != null
                    && pressedTabComponent instanceof JScrollPane) {
                  final Component focus = ((JScrollPane) pressedTabComponent).getViewport().getView();
                  SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                      focus.requestFocus();
                    }
                  });
                }
              }
            }
            /*
             else { // if (isSizing) {
             }
             */
          }
        }
        
      }
      endDrag();
    }
  }
  
  DragFloatingComponent dragComponent = null;
  DragFloatingComponent dragFloatingComponent = new DragFloatingComponent();
  
  protected class DragFloatingComponent extends Canvas {
    BufferedImage buffImage = new BufferedImage(2, 2, BufferedImage. TYPE_4BYTE_ABGR);
    Color bgColor;
    Color fgColor;
    Insets insets = new Insets(DRAG_FRAME_SIZE, DRAG_FRAME_SIZE, DRAG_FRAME_SIZE, DRAG_FRAME_SIZE);
    protected DragFloatingComponent() {
      super();
      bgColor = UIManager.getColor("SplitPane.background");
      fgColor = UIManager.getColor("SplitPane.darkShadow");
      Color bg = bgColor;
      if (bg.getAlpha() > 0f) {
        float[] comp = bg.getRGBComponents(null);
        bg= new Color(comp[0], comp[1], comp[2], 0f);
      }
      int[] imageData = new int[] {
          fgColor.getRGB(), bg.getRGB(),
          bg.getRGB(), fgColor.getRGB(),
      };
      buffImage.setRGB(0, 0, 2, 2, imageData, 0, 2);
      texturePaint = new TexturePaint(buffImage, new Rectangle(0, 0, 2, 2));
      super.setVisible(false);
    }
    public void setVisible(boolean b) {
      if (b) {
        rootPane.dragComponent = this;
      }
      else if (rootPane.dragComponent == this) {
        rootPane.dragComponent = null;
      }
      super.setVisible(b);
      rootPane.repaint();
    }
    
    private Rectangle bounds = new Rectangle();
    private TexturePaint texturePaint;
    public void paint(Graphics g) {
      getBounds(bounds);
      int x = bounds.x;
      int y = bounds.y;
      int width = bounds.width;
      int height = bounds.height;
      
      g.translate(x, y);
      if (g instanceof Graphics2D) {
        Graphics2D g2d = (Graphics2D) g;
        Paint p = g2d.getPaint();
        if (true) {
          // use alpha-blended checkerboard frame
          g2d.setPaint(texturePaint);
          g2d.fillRect(0, 0, width - insets.right, insets.top);
          g2d.fillRect(0, insets.top, insets.left, height - insets.top);
          g2d.fillRect(insets.left, height - insets.bottom, width - insets.left, insets.bottom);
          g2d.fillRect(width - insets.right, 0, insets.right, height - insets.bottom);
        }
        else {
          if (false) {
            // boring XOR frame
            Color oldColor = g.getColor();
            g.setXORMode(bgColor);
            g.setColor(fgColor);
          }
          else {
            // alpha-blended checkerboard frame
            g2d.setPaint(texturePaint);
          }
          // draw drag frame with tab pane shape (rounded top corners) 
          Shape s = createTabPaneBorderShape(0, 0, width, height);
          Stroke stroke = g2d.getStroke();
          BasicStroke bs = new BasicStroke(insets.left);
          g2d.setStroke(bs);
          RenderingHints hints = g2d.getRenderingHints();
          g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
          g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
          g2d.draw(s);
          g2d.setRenderingHints(hints);
          g2d.setStroke(stroke);
        }
        g2d.setPaint(p);
      }
      else {
        // boring XOR frame
        Color oldColor = g.getColor();
        g.setXORMode(bgColor);
        g.setColor(fgColor);
        
        g.fillRect(0, 0, width - insets.right, insets.top);
        g.fillRect(0, insets.top, insets.left, height - insets.top);
        g.fillRect(insets.left, height - insets.bottom, width - insets.left, insets.bottom);
        g.fillRect(width - insets.right, 0, insets.right, height - insets.bottom);
        g.setColor(oldColor);
      }
      g.translate(-x, -y);
    }
  }

  protected class Border extends LineBorder {
    public Border(Color borderColor) {
      super(borderColor, 1, true); // more than 1 leaves unfilled pixels in the corners
    }
    public void paintBorder(Component c, Graphics g, int x, int y,
        int width, int height) {
      JTabbedPane tp = null;
      if (c instanceof JTabbedPane)
        tp = (JTabbedPane) c;
      
      Color oldColor = g.getColor();
      int i;
      
      if (tp != null && tp.getTabPlacement() == TOP) {
        g.setColor(lineColor);
        if (g instanceof Graphics2D) {
          Graphics2D g2d = (Graphics2D) g;
          Stroke stroke = g2d.getStroke();
          BasicStroke bs = new BasicStroke(getThickness());
          g2d.setStroke(bs);
          GeneralPath s = createTabPaneBorderShape(x, y, width, height);
          RenderingHints hints = g2d.getRenderingHints();
          g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
          g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
          g2d.draw(s);
          g2d.setRenderingHints(hints);
          g2d.setStroke(stroke);
        }
        else {
          for(i = 0; i < thickness; i++)  {
            g.drawLine(x+i+round, y+i, x+width-i-1-round, y+i);  // top
            g.drawLine(x+i, y+i+round, x+i, y+i+height-i-1); // left
            g.drawLine(x+width-i-1, y+i+round, x+width-i-1, y+i+height-i-1); // right
            g.drawLine(x+i, y+i+height-i-1, x+width-i-1, y+i+height-i-1); // bottom
            
            int size = 2*round;
            int bound = size;
            g.drawArc(x+i, y+i, bound, bound, 180, -90);
            g.drawArc(x+width-i-1-round-round, y+i, bound, bound, 0, 90);
          }
        }
        g.setColor(oldColor);
      }
      else {
        super.paintBorder(c, g, x, y, width, height);
      }
    }
  }
  
  public Border getBorder(Color borderColor) {
    return new Border(borderColor);
  }
  
  public BasicDraggableTabbedPaneUI(DraggableTabbedPane.TabbedPane draggableTabbedPane) {
    this.draggableTabbedPane = draggableTabbedPane;
  }
  
  protected LayoutManager createLayoutManager() {
    return new DraggableLayout(super.createLayoutManager());
  }

  protected void installListeners() {
    super.installListeners();
    tabPane.addMouseMotionListener((MouseInputListener) mouseListener);
  }
  
  protected void uninstallListeners() {
    if (mouseListener != null) {
      tabPane.removeMouseMotionListener((MouseInputListener) mouseListener);
    }
    super.uninstallListeners();
  }
  boolean isTabPaneFocusAncestor = false;
  
  class FocusWatcher implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent e) {
      final Component old1 = (Component) e.getOldValue();
//    Component new1 = (Component) e.getNewValue();
//    Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
//    isTabPaneFocusAncestor = tabPane == owner || tabPane.isAncestorOf(owner);
//    if (isTabPaneFocusAncestor) {
//    int x = 1;
//    }
//    if (isTabPaneFocusAncestor || tabPane == old1 || tabPane.isAncestorOf(old1)) {
//    tabPane.repaint();
//    }
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
          isTabPaneFocusAncestor = tabPane == owner || tabPane.isAncestorOf(owner);
          if (isTabPaneFocusAncestor) {
            int x = 1;
          }
          if (isTabPaneFocusAncestor || tabPane == old1 || tabPane.isAncestorOf(old1)) {
            tabPane.repaint();
          }
          
        }
      });
    }
  }
  FocusWatcher focusWatcher;
  BasicTabbedPaneUI parentUI = null;
//MetalTabbedPaneUI metalUI = null;
//MotifTabbedPaneUI motifUI = null;
//WindowsTabbedPaneUI windowsUI = null;
  public void installUI(JComponent c) {
    TabbedPaneUI ui = ((JTabbedPane) c).getUI();
    if (ui != this) {
      if (ui != parentUI && ! (ui instanceof BasicDraggableTabbedPaneUI))
        parentUI = (BasicTabbedPaneUI) ui;
//    if (parentUI instanceof MetalTabbedPaneUI)
//    metalUI = new DraggableMetalTabbedPaneUI(metalUI);
    }
//  c.addFocusListener(repainter);
    super.installUI(c);
//  JTabbedPane tp = ((JTabbedPane) c);
//  LayoutManager lm = tp.getLayout();
//  tp.setLayout(new DraggableLayout(lm));
    
    KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    fm.addPropertyChangeListener("permanentFocusOwner", focusWatcher = new FocusWatcher());
  }
  public void uninstallUI(JComponent c) {
//  c.removeFocusListener(repainter);
    KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    fm.removePropertyChangeListener("permanentFocusOwner", focusWatcher);
    focusWatcher = null;
    parentUI = null;
    super.uninstallUI(c);
  }
  /**
   * @see javax.swing.plaf.basic.BasicTabbedPaneUI#createMouseListener()
   * @return
   */
  DragMouseHandler dragMouseHandler;
  protected MouseListener createMouseListener() {
    if (dragMouseHandler == null)
      dragMouseHandler = new DragMouseHandler();
    return dragMouseHandler;
  }
  
  
  protected FocusListener createFocusListener() {
    return new FocusAdapter() { };
  }
  
  //protected static final int DOCKING_TOLERANCE = 3;
  //protected static final int DOCKING_TOLERANCE_DIVISOR = 4;
  
  private int checkDockable(Container target, Dimension dragged, Point dragPoint) {
    int dock = FLOAT;
    Dimension targetSize = new Dimension(target.getSize());
    int tolerance = 0;
    if (target instanceof DraggableTabbedPane.TabbedPane) {
      DraggableTabbedPane.TabbedPane targetPane = (DraggableTabbedPane.TabbedPane) target;
      BasicDraggableTabbedPaneUI ui = targetPane.ui;
      int tabPlacement = targetPane.getTabPlacement();
      switch (tabPlacement) {
      case JTabbedPane.TOP:
      case JTabbedPane.BOTTOM:
        tolerance = ui.lastTabAreaHeight;
        break;
      case JTabbedPane.LEFT:
      case JTabbedPane.RIGHT:
        tolerance = ui.lastTabAreaWidth;
        break;
      }
    }
    else if (target == rootPane) {
      Insets insets = rootPane.getInsets();
      tolerance = Math.max(insets.bottom, insets.top);
      tolerance = Math.max(tolerance, insets.left);
      tolerance = Math.max(tolerance, insets.right);
    }
    if (dragPoint.y <= 0 + tolerance)
      dock = NORTH;
    else if (dragPoint.y >= targetSize.height - tolerance)
      dock = SOUTH;
    else if (dragPoint.x <= 0 + tolerance)
      dock = WEST;
    else if (dragPoint.x >= targetSize.width - tolerance)
      dock = EAST;
    
    /*
     // TODOn't make tolerance divisor larger for smaller target component sizes
      tolerance.width /= DOCKING_TOLERANCE_DIVISOR;
      tolerance.height /= DOCKING_TOLERANCE_DIVISOR;
      */
//  if (dragPoint.y <= 0 + tolerance.height)
//  dock = NORTH;
//  else if (dragPoint.y + dragged.height >= target.height - tolerance.height)
//  dock = SOUTH;
//  else if (dragPoint.x <= 0 + tolerance.width)
//  dock = WEST;
//  else if (dragPoint.x + dragged.width >= target.width - tolerance.width)
//  dock = EAST;
    
//  if (dragPoint.y <= 0 + DOCKING_TOLERANCE)
//  dock = NORTH;
//  else if (dragPoint.y + dragged.height + DOCKING_TOLERANCE >= target.height)
//  dock = SOUTH;
//  else if (dragPoint.x <= 0 + DOCKING_TOLERANCE)
//  dock = WEST;
//  else if (dragPoint.x + dragged.width + DOCKING_TOLERANCE >= target.width)
//  dock = EAST;
    return dock;
  }
  
  Cursor savedWindowCursor = null;
  Point globalMousePos = new Point();
  Point mousePos = new Point();
  Point dragPoint = new Point();
  Point dragOffset = new Point();
  Dimension rootSize = new Dimension();
  Point targetMousePos = new Point();
  Rectangle windowBounds = new Rectangle();
  Dimension dragComponentSize = new Dimension();
  Point targetDragPoint = new Point();
  
  protected void dragTo(Component dragged, Point pos) {
    Rectangle pressedTabRect = getTabBounds(tabPane, pressedTabIndex);
    
    DraggableState state = (DraggableState) draggableStateTable.get(dragged);
    if (state == null) {
      state = new DraggableState();
      draggableStateTable.put(dragged, state);
    }
    state.tabPaneTarget = null;
    state.splitPaneTarget = null;
    state.rootPaneTarget = null;
    if (state.savedSize == null) {
      state.savedSize = pressedTabRect.getSize();
    }
    state.dock = FLOAT;
    
    mousePos.x = draggingTabPaneOrigin.x + pos.x;
    mousePos.y = draggingTabPaneOrigin.y + pos.y;
    dragPoint.x = mousePos.x - offset.x;
    dragPoint.y = mousePos.y - offset.y;
    dragOffset.x = offset.x - pressedTabRect.x;
    dragOffset.y = offset.y - pressedTabRect.y;
    rootPane.getSize(rootSize);
    Point rootLocation = rootPane.getLocationOnScreen();
    Container target = rootPane.getPaneAtLocation(mousePos.x, mousePos.y);
    
    if (savedCursor != null) {
      Container window = rootPane.getTopLevelAncestor();
      JRootPane jRootPane = (window instanceof JComponent) ?
          ((JComponent) window).getRootPane() :
            rootPane.getRootPane();
          window.getBounds(windowBounds);
          globalMousePos.x = globalOrigin.x + pos.x;
          globalMousePos.y = globalOrigin.y + pos.y;
          Component glassPane = jRootPane.getGlassPane();
          glassPane.setVisible(true);
          
          if (! windowBounds.contains(globalMousePos)) {
            glassPane.setCursor(DragSource.DefaultMoveNoDrop);
//          if (savedWindowCursor == null) {
//          savedWindowCursor = window.getCursor();
//          }
//          window.setCursor(DragSource.DefaultMoveNoDrop);
//          tabPane.setCursor(DragSource.DefaultMoveNoDrop);
          }
          else {
            glassPane.setCursor(DragSource.DefaultMoveDrop);
            //                tabPane.setCursor(DragSource.DefaultMoveDrop);
//          if (savedWindowCursor != null) {
//          window.setCursor(savedWindowCursor);
//          savedWindowCursor = null;
//          }
          }
    }
    
    Point targetLocation = target.getLocationOnScreen();
    targetLocation.x -= rootLocation.x;
    targetLocation.y -= rootLocation.y;
    targetMousePos.x = mousePos.x - targetLocation.x;
    targetMousePos.y = mousePos.y - targetLocation.y;
    
    while (target != null && target != rootPane && target instanceof DraggableTabbedPane.SplitPane) {
      DraggableTabbedPane.SplitPane targetSplitPane = (DraggableTabbedPane.SplitPane) target;
      int orientation = targetSplitPane.getOrientation();
      Component c = null; // targetSplitPane.getLeftComponent();
      double dividerSize = targetSplitPane.getDividerSize() / 2.0d;
      double split = targetSplitPane.getUI().getDividerLocation(targetSplitPane);
      if (orientation == JSplitPane.VERTICAL_SPLIT) {
        if (targetMousePos.y <= split + dividerSize) {
          c = targetSplitPane.getTopComponent();
          state.splitPaneAnchor = JSplitPane.TOP;
//        state.dock = NORTH;
        }
        else if (targetMousePos.y > split + dividerSize) {
          c = targetSplitPane.getBottomComponent();
          state.splitPaneAnchor = JSplitPane.BOTTOM;
//        state.dock = SOUTH;
        }
      }
      else if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
        if (targetMousePos.x <= split + dividerSize) {
          c = targetSplitPane.getLeftComponent();
          state.splitPaneAnchor = JSplitPane.LEFT;
//        state.dock = WEST;
        }
        else if (targetMousePos.x > split + dividerSize) {
          c = targetSplitPane.getRightComponent();
          state.splitPaneAnchor = JSplitPane.RIGHT;
//        state.dock = EAST;
        }
      }
      if (c == null) {
        break;
      }
      else if (! c.isVisible()) {
        break;
      }
      else if (c instanceof Container) {
        state.dock = FLOAT;
        target = (Container) c;
        targetLocation = target.getLocationOnScreen();
        targetLocation.x -= rootLocation.x;
        targetLocation.y -= rootLocation.y;
        targetMousePos = mousePos.getLocation();
        targetMousePos.x -= targetLocation.x;
        targetMousePos.y -= targetLocation.y;
      }
      else
        // TODO allow non-tab pane children of split panes
        target = null;
    }
    if (target != null && target != rootPane) {
      if (! (target instanceof DraggableTabbedPane.SplitPane)) {
        if (! (target instanceof DraggableTabbedPane.TabbedPane)) {
          Object foo = null;
        }
      }
    }
    if (target == null)
      target = rootPane;
    Dimension targetSize = target.getSize();
    
    targetLocation = target.getLocationOnScreen();
    targetLocation.x -= rootLocation.x;
    targetLocation.y -= rootLocation.y;
    targetMousePos = mousePos.getLocation();
    targetMousePos.x = mousePos.x - targetLocation.x;
    targetMousePos.y = mousePos.y - targetLocation.y;
    
    targetDragPoint.x = dragPoint.x - targetLocation.x;
    targetDragPoint.y = dragPoint.y - targetLocation.y;
    double dragComponentDivisor = 3.0;
//  int oldDock = state.dock;
//  if (state.dock == FLOAT)
    state.dock = checkDockable(target, state.savedSize, targetMousePos);
    dragComponentSize.width = dragComponentSize.height = -1;
    int tabIndex = -1;
    DraggableTabbedPane.TabbedPane tabTarget = null;
    BasicDraggableTabbedPaneUI ui = null;
    if (target instanceof DraggableTabbedPane.TabbedPane) {
      tabTarget = (DraggableTabbedPane.TabbedPane) target;
      ui = tabTarget.ui;
      tabIndex = ui.tabForCoordinate(tabTarget, mousePos.x - targetLocation.x, mousePos.y - targetLocation.y);
      if (tabTarget != null)
        state.tabPaneTarget = tabTarget;
    }
    if (tabIndex >= 0) {
      Rectangle tabRect = ui.getTabBounds(tabTarget, tabIndex);
      // drop is over a valid tab
      dragComponentSize.setSize(tabRect.width, tabRect.height);
//    Point p = tabRect.getLocation();
//    p.x += targetLocation.x;
//    p.y += targetLocation.y;
      dragComponent.setLocation(tabRect.x + targetLocation.x, tabRect.y + targetLocation.y);
    }
    else {
      // drop is not over a tab
      dragPoint.setLocation(targetLocation);
//    dragPoint = targetLocation.getLocation();
      if (tabTarget != null && tabTarget != tabPane) {
        // drop is over a draggable tab pane
        // check for drop to right of last tab of a different pane
        int tabPlacement = tabTarget.getTabPlacement();
        Insets insets = tabTarget.getInsets();
        Insets tabAreaInsets = ui.getTabAreaInsets(tabPlacement);
        if (tabPlacement == JTabbedPane.TOP || tabPlacement == JTabbedPane.BOTTOM) {
          int tabAreaHeight = ui.lastTabAreaHeight;
          if (tabPlacement == JTabbedPane.TOP) {
            int tabAreaTop = insets.top + tabAreaInsets.top;
            if (tabAreaTop <= targetMousePos.y
                && targetMousePos.y <= tabAreaTop + tabAreaHeight) {
              int tabCount = tabTarget.getTabCount();
              if (tabCount > 0) {
                int lastTab = tabCount - 1;
                if (ui.getTabRunCount(tabTarget) > 1) {
                  int tab = tabTarget.getSelectedIndex();
                  if (tab != -1) {
                    int run = ui.getRunForTab(tabCount, tab);
                    int next = ui.getNextTabIndexInRun(tabCount, run);
                    for (int n = next; next != tab && run == ui.getRunForTab(tabCount, n); n = ui.getNextTabIndexInRun(tabCount, n)) {
                      if (ui.getRunForTab(tabCount, n) == run)
                        next = n;
                    }
                    if (run == ui.getRunForTab(tabCount, next))
                      lastTab = next;
                    else
                      lastTab = tab;
                  }
                }
                Rectangle tabBounds = ui.getTabBounds(tabTarget, lastTab);
                int lastTabWidth = tabBounds.width;
                int lastTabRightEdge = tabBounds.x + lastTabWidth;
                int tabAreaRightEdge = tabTarget.getWidth() - (insets.right + tabAreaInsets.right);
                if (lastTabRightEdge <= targetMousePos.x
                    && targetMousePos.x <= tabAreaRightEdge) {
                  int dclX = targetLocation.x + lastTabRightEdge + 1;
                  int dclY = tabBounds.y + targetLocation.y;
//                Point dragComponentLocation = new Point(targetLocation.x + lastTabRightEdge + 1,
//                tabBounds.y + targetLocation.y);
                  state.dock = FLOAT;
                  if (dclX < targetLocation.x + tabAreaRightEdge) {
//                  dragComponentSize = new Dimension();
//                  dragComponentSize.width = Math.min(lastTabWidth, tabAreaRightEdge - lastTabRightEdge);
                    dragComponentSize.width = Math.min(pressedTabRect.width, tabAreaRightEdge - lastTabRightEdge);
                    dragComponentSize.height = tabBounds.height;
                    dragPoint.setLocation(dclX, dclY);
                  }
                }
              }
            }
          }
          else { // tabPlacement == JTabbedPane.BOTTOM
            
          }
        }
        else { // tabPlacement == JTabbedPane.LEFT || tabPlacement == JTabbedPane.RIGHT
          
        }
      }
      else if (target instanceof SplitPane) {
        dragComponentDivisor = 2.0;
        DraggableTabbedPane.SplitPane targetSplitPane = (DraggableTabbedPane.SplitPane) target;
        int orientation = targetSplitPane.getOrientation();
        double dividerSize = targetSplitPane.getDividerSize() / 2.0d;
        double split = targetSplitPane.getUI().getDividerLocation(targetSplitPane);
        if (orientation == JSplitPane.VERTICAL_SPLIT) {
          if (targetMousePos.y <= split + dividerSize) {
            state.splitPaneAnchor = JSplitPane.TOP;
            state.dock = NORTH;
          }
          else if (targetMousePos.y > split + dividerSize) {
            state.splitPaneAnchor = JSplitPane.BOTTOM;
            state.dock = SOUTH;
          }
        }
        else if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
          if (targetMousePos.x <= split + dividerSize) {
            state.splitPaneAnchor = JSplitPane.LEFT;
            state.dock = WEST;
          }
          else if (targetMousePos.x > split + dividerSize) {
            state.splitPaneAnchor = JSplitPane.RIGHT;
            state.dock = EAST;
          }
        }
        state.splitPaneTarget = targetSplitPane;
      }
      else if (target == rootPane) {
        state.rootPaneTarget = rootPane;
      }
      
      if (state.dock != FLOAT) {
        switch (state.dock) {
        case NORTH:
        case SOUTH:
          dragComponentSize.width = targetSize.width;
          dragComponentSize.height = (int) Math.round(targetSize.getHeight() / dragComponentDivisor);
          dragPoint.x = targetLocation.x;
          dragPoint.y = targetLocation.y;
          if (state.dock == SOUTH)
            dragPoint.y += targetSize.height - dragComponentSize.height;
          break;
        case WEST:
        case EAST:
          dragComponentSize.width = (int) Math.round(targetSize.getWidth() / dragComponentDivisor);
          dragComponentSize.height = targetSize.height;
          dragPoint.x = targetLocation.x;
          if (state.dock == EAST)
            dragPoint.x += targetSize.width - dragComponentSize.width;
          dragPoint.y = targetLocation.y;
          break;
        }
      }
      else if (target == rootPane) {
        // TODO what if target is rootPane, but not docking? should never happen
        pressedTabRect = getTabBounds(tabPane, pressedTabIndex);
        dragComponentSize.setSize(pressedTabRect.width, pressedTabRect.height);
      }
      else if (dragComponentSize.width < 0 || dragComponentSize.height < 0){
        dragComponentSize.setSize(targetSize);
      }
      dragComponent.setLocation(dragPoint);
    }
    dragComponent.setSize(dragComponentSize);
    state.lastPosition.setLocation(dragPoint);
    // TODO use lastSize to determine size of split
    state.lastSize.setSize(dragComponentSize);
    state.lastMousePosition.setLocation(mousePos);
    state.targetLocation.setLocation(targetLocation);
    state.targetDragPoint.setLocation(targetDragPoint);
    state.targetMousePos.setLocation(targetMousePos);
  }
  
  /**
   * @deprecated This is dead code that only makes sense if sides of tab are
   *             sizeable (in case we ever try to something more sophisticated
   *             than just nesting split panes)
   */
  protected void sizeTo(Component c, Point pos) {
    
    Dimension min = c.getMinimumSize(),
    max = c.getMaximumSize();
    
    DraggableState state = (DraggableState) draggableStateTable.get(c);
    int w = state.lastSize.width, h = state.lastSize.height;
    int x = state.lastPosition.x, y = state.lastPosition.y;
    
    if ((sizeState & NORTH) != 0) {
      int yy = -pos.y;
      h += yy;
      y -= yy;
    }
    else if ((sizeState & SOUTH) != 0) {
      h = pos.y;
    }
    if ((sizeState & WEST) != 0) {
      int xx = -pos.x;
      w += xx;
      x -= xx;
    }
    else if ((sizeState & EAST) != 0) {
      w = pos.x;
    }
    
    if (w < min.width) { w = min.width; x = state.lastPosition.x; }
    else if (w > max.width) w = max.width;
    
    if (h < min.height) { h = min.height; y = state.lastPosition.y; }
    else if (h > max.height) h = max.height;
    
    if (w != state.lastSize.width || h != state.lastSize.height) {
      c.setSize(w, h);
      state.savedSize.width = w;
      state.savedSize.height = h;
    }
    if (x != state.lastPosition.x || y != state.lastPosition.y) {
      c.setLocation(x, y);
    }
  }
  
  // should depend on size of cursor, but that info is not available
  protected static final int SIZE_TOLERANCE = 10;
  protected int sizeState = NONE;
  
  private int checkSizable(Dimension d, Point p) {
    if (true) return NONE;
    int sizeState = NONE;
    
    int h_min = 0 - SIZE_TOLERANCE;
    int h_max = d.height + SIZE_TOLERANCE;
    int w_min = 0 - SIZE_TOLERANCE;
    int w_max = d.width + SIZE_TOLERANCE;
    
    if (p.y > h_min && p.y <= SIZE_TOLERANCE)
      sizeState |= NORTH;
    else if (p.y >= d.height - SIZE_TOLERANCE && p.y < h_max)
      sizeState |= SOUTH;
    
    if (p.x > w_min && p.x <= SIZE_TOLERANCE)
      sizeState |= WEST;
    else if (p.x > d.width - SIZE_TOLERANCE && p.x < w_max)
      sizeState |= EAST;
    
    return NONE; //sizeState;
  }
  
  
  protected static class DraggableState {
    Dimension lastSize = new Dimension();
    Dimension savedSize = new Dimension();
    Point lastPosition = new Point();
    Point lastMousePosition = new Point();
    Point targetLocation = new Point();
    Point targetDragPoint = new Point();
    Point targetMousePos = new Point();
    int sizeState = NONE;
    int floatState = STATE_FLOATED;
    int dock = FLOAT;
    TabbedPane tabPaneTarget = null;
    SplitPane splitPaneTarget = null;
    DraggableTabbedPane rootPaneTarget = null;
    String splitPaneAnchor = null;
  }
  
  int lastTabAreaHeight = -1;
  int lastTabAreaWidth = -1;
  protected int calculateMaxTabHeight(int tabPlacement) {
    FontMetrics metrics = getFontMetrics();
    int tabCount = tabPane.getTabCount();
    int result = 0;
    int fontHeight = metrics.getHeight();
    for(int i = 0; i < tabCount; i++) {
      result = Math.max(calculateTabHeight(tabPlacement, i, fontHeight), result);
    }
    return result;
  }
  
  int getTabAreaHeight() {
    calculateTabAreaHeight(tabPane.getTabPlacement(), runCount, maxTabHeight);
    return lastTabAreaHeight;
  }
  
  int getMinTabAreaHeight() {
    calculateTabAreaHeight(tabPane.getTabPlacement(), 1, maxTabHeight);
    return lastTabAreaHeight;
  }
  
  int getTabAreaWidth() {
    calculateTabAreaWidth(tabPane.getTabPlacement(), runCount, maxTabWidth);
    return lastTabAreaWidth;
  }
  
  int getMinTabAreaWidth() {
    calculateTabAreaWidth(tabPane.getTabPlacement(), runCount, maxTabWidth);
    return lastTabAreaWidth;
  }
  
  protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount,
      int maxTabHeight) {
    return lastTabAreaHeight = super.calculateTabAreaHeight(tabPlacement, horizRunCount,
        maxTabHeight);
  }
  protected int calculateTabAreaWidth(int tabPlacement, int vertRunCount,
      int maxTabWidth) {
    return lastTabAreaWidth = super.calculateTabAreaWidth(tabPlacement, vertRunCount,
        maxTabWidth);
  }
  
  protected int getTabFontHeight() {
    FontMetrics metrics = getFontMetrics();
    return metrics.getHeight();
  }
  
  protected int calculateTabWidth0(int tabPlacement, int tabIndex, FontMetrics metrics) {
    Icon icon = getIconForTab(tabIndex);
    Insets tabInsets = getTabInsets(tabPlacement, tabIndex);
    int width = tabInsets.left + tabInsets.right + 3;
    
    if (icon != null) {
      width += icon.getIconWidth() + textIconGap;
    }
    View v = getTextViewForTab(tabIndex);
    if (v != null) {
      // html
      width += (int)v.getPreferredSpan(View.X_AXIS);
    } else {
      // plain text
      String title = tabPane.getTitleAt(tabIndex);
      width += SwingUtilities.computeStringWidth(metrics, title);
    }
    
    return width;
  }

  protected int calculateTabWidth(int tabPlacement, int tabIndex,
      FontMetrics metrics) {
    int w = calculateTabWidth0(tabPlacement, tabIndex, metrics);
    CloseTabButton closeButton = (CloseTabButton) draggableTabbedPane.getTabComponent(tabIndex, CLOSE_TAB_BUTTON);
    int iconWidth = 0;
    if (closeButton != null)
      iconWidth = closeButton.getIcon().getIconWidth();
    if (tabIndex == tabPane.getSelectedIndex()) {
      w += selectedTabPadInsets.left + selectedTabPadInsets.right + iconWidth;
    }
    else { // if (tabIndex > 0) {
      w += 2 * textIconGap + iconWidth;
    }
    return w;
  }
  
  protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
    int height = 0;
    View v = getTextViewForTab(tabIndex);
    if (v != null) {
      // html
      height += (int)v.getPreferredSpan(View.Y_AXIS);
    } else {
      // plain text
      height += fontHeight;
    }
    Icon icon = getIconForTab(tabIndex);
    Insets tabInsets = getTabInsets(tabPlacement, tabIndex);
    
    if (icon != null) {
      height = Math.max(height, icon.getIconHeight());
    }
    height += tabInsets.top + tabInsets.bottom + 2;
    
    return height;
  }
  Insets getSelectedTabInsets() {
    return super.getTabInsets(tabPane.getTabPlacement(), tabPane.getSelectedIndex());
  }
  protected Insets calculatedTabAreaInsets;
  protected Insets getTabAreaInsets(int tabPlacement) {
    Insets insets = super.getTabAreaInsets(tabPlacement);
    calculatedTabAreaInsets = (Insets) insets.clone(); //new Insets(insets.top, insets.left, insets.bottom, insets.right);
    insets.top = 0;
    insets.bottom = 0;
    insets.left = 0;
    insets.right = 0;
    int numButtons = 0;
    if (draggableTabbedPane.minButton != null)
      numButtons++;
    if (draggableTabbedPane.maxButton != null)
      numButtons++;
    int fontHeight = getTabFontHeight();
    Insets tabInsets = getSelectedTabInsets();
    int height = fontHeight + tabInsets.top + tabInsets.bottom;
    int width = height * numButtons + tabInsets.left /*+ tabInsets.right */+ 3 + round;
    //tabInsets.left * numButtons + tabInsets.right * numButtons;
    switch (tabPlacement) {
    case LEFT:
      break;
    case RIGHT:
      break;
    case TOP:
      insets.right += width;
      break;
    case BOTTOM:
      insets.right += width;
      break;
    }
    return insets;
  }
//protected void assureRectsCreated(int tabCount) {
//super.assureRectsCreated(tabCount);
//}
//protected int calculateMaxTabHeight(int tabPlacement) {
//return super.calculateMaxTabHeight(tabPlacement);
//}
//protected int calculateMaxTabWidth(int tabPlacement) {
//return super.calculateMaxTabWidth(tabPlacement);
//}
//protected int calculateTabHeight(int tabPlacement, int tabIndex,
//int fontHeight) {
//return super.calculateTabHeight(tabPlacement, tabIndex, fontHeight);
//}
//protected int calculateTabWidth(int tabPlacement, int tabIndex,
//FontMetrics metrics) {
//return super.calculateTabWidth(tabPlacement, tabIndex, metrics);
//}
////ActionMap createActionMap() {
////return super.createActionMap();
////}
//protected ChangeListener createChangeListener() {
//return super.createChangeListener();
//}
//protected FocusListener createFocusListener() {
//return super.createFocusListener();
//}
//protected LayoutManager createLayoutManager() {
//return super.createLayoutManager();
//}
//protected PropertyChangeListener createPropertyChangeListener() {
//return super.createPropertyChangeListener();
//}
//protected void expandTabRunsArray() {
//super.expandTabRunsArray();
//}
////ActionMap getActionMap() {
////return super.getActionMap();
////}
//protected Insets getContentBorderInsets(int tabPlacement) {
//return super.getContentBorderInsets(tabPlacement);
//}
//protected FontMetrics getFontMetrics() {
//return super.getFontMetrics();
//}
//protected Icon getIconForTab(int tabIndex) {
//return super.getIconForTab(tabIndex);
//}
////InputMap getInputMap(int condition) {
////return super.getInputMap(condition);
////}
//public Dimension getMaximumSize(JComponent c) {
//return super.getMaximumSize(c);
//}
//public Dimension getMinimumSize(JComponent c) {
//return super.getMinimumSize(c);
//}
//protected int getNextTabIndex(int base) {
//return super.getNextTabIndex(base);
//}
//protected int getNextTabIndexInRun(int tabCount, int base) {
//return super.getNextTabIndexInRun(tabCount, base);
//}
//protected int getNextTabRun(int baseRun) {
//return super.getNextTabRun(baseRun);
//}
//public Dimension getPreferredSize(JComponent c) {
//return super.getPreferredSize(c);
//}
//protected int getPreviousTabIndex(int base) {
//return super.getPreviousTabIndex(base);
//}
//protected int getPreviousTabIndexInRun(int tabCount, int base) {
//return super.getPreviousTabIndexInRun(tabCount, base);
//}
//protected int getPreviousTabRun(int baseRun) {
//return super.getPreviousTabRun(baseRun);
//}
//protected int getRunForTab(int tabCount, int tabIndex) {
//return super.getRunForTab(tabCount, tabIndex);
//}
//protected Insets getSelectedTabPadInsets(int tabPlacement) {
//return super.getSelectedTabPadInsets(tabPlacement);
//}
//protected Rectangle getTabBounds(int tabIndex, Rectangle dest) {
//return super.getTabBounds(tabIndex, dest);
//}
//public Rectangle getTabBounds(JTabbedPane pane, int i) {
//return super.getTabBounds(pane, i);
//}
//protected Insets getTabInsets(int tabPlacement, int tabIndex) {
//return super.getTabInsets(tabPlacement, tabIndex);
//}
//protected int getTabLabelShiftX(int tabPlacement, int tabIndex,
//boolean isSelected) {
//return super.getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
//}
//protected int getTabLabelShiftY(int tabPlacement, int tabIndex,
//boolean isSelected) {
//return super.getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
//}
//public int getTabRunCount(JTabbedPane pane) {
//return super.getTabRunCount(pane);
//}
//protected int getTabRunIndent(int tabPlacement, int run) {
//return super.getTabRunIndent(tabPlacement, run);
//}
//protected int getTabRunOffset(int tabPlacement, int tabCount, int tabIndex,
//boolean forward) {
//return super.getTabRunOffset(tabPlacement, tabCount, tabIndex, forward);
//}
//protected int getTabRunOverlay(int tabPlacement) {
//return super.getTabRunOverlay(tabPlacement);
//}
//protected View getTextViewForTab(int tabIndex) {
//return super.getTextViewForTab(tabIndex);
//}
//protected Component getVisibleComponent() {
//return super.getVisibleComponent();
//}
//protected void installComponents() {
//super.installComponents();
//}
//protected void installDefaults() {
//super.installDefaults();
//}
//protected void installKeyboardActions() {
//super.installKeyboardActions();
//}
//protected int lastTabInRun(int tabCount, int run) {
//return super.lastTabInRun(tabCount, run);
//}
//protected void layoutLabel(int tabPlacement, FontMetrics metrics,
//int tabIndex, String title, Icon icon, Rectangle tabRect,
//Rectangle iconRect, Rectangle textRect, boolean isSelected) {
//super.layoutLabel(tabPlacement, metrics, tabIndex, title, icon,
//tabRect, iconRect, textRect, isSelected);
//}
//protected void navigateSelectedTab(int direction) {
//super.navigateSelectedTab(direction);
//}
  
  private static class SetSelectedIndexAction extends AbstractAction {
    Action superAction;
    SetSelectedIndexAction(Action superAction) {
      this.superAction = superAction;
    }
    public void actionPerformed(ActionEvent e) {
      JTabbedPane pane = (JTabbedPane)e.getSource();
      
      if (pane != null && (pane.getUI() instanceof BasicDraggableTabbedPaneUI)) {
        BasicDraggableTabbedPaneUI ui = (BasicDraggableTabbedPaneUI)pane.getUI();
        String command = e.getActionCommand();
        
        if (command != null && command.length() > 0) {
          int mnemonic = e.getActionCommand().charAt(0);
          if (mnemonic >= 'a' && mnemonic <= 'z') {
            mnemonic  -= ('a' - 'A');
          }
          Integer index = (Integer)ui.mnemonicToIndexMap.
          get(new Integer(mnemonic));
          if (index != null && pane.isEnabledAt(index.intValue())) {
            if (!(pane.isAncestorOf(FocusManager.getCurrentManager().getFocusOwner()))
                || pane.getSelectedIndex() != index.intValue()) {
              pane.setSelectedIndex(index.intValue());
              pane.requestFocus();
              return;
            }
          }
        }
      }
      if (superAction != null)
        superAction.actionPerformed(e);
    }
  };
  
  protected void installKeyboardActions() {
    super.installKeyboardActions();
    ActionMap oldMap = SwingUtilities.getUIActionMap(tabPane);
    if (oldMap instanceof UIResource) {
      Action setSelectedIndex;
      if (! ((setSelectedIndex = oldMap.get("setSelectedIndex")) instanceof SetSelectedIndexAction)) {
        ActionMap newMap = new ActionMap();
        newMap.setParent(oldMap);
        SwingUtilities.replaceUIActionMap(tabPane, newMap);
        newMap.put("setSelectedIndex", new SetSelectedIndexAction(setSelectedIndex));
      }
    }
  }
  
  protected void uninstallKeyboardActions() {
    super.uninstallKeyboardActions();
    SwingUtilities.replaceUIInputMap(tabPane, JComponent.WHEN_IN_FOCUSED_WINDOW, null);
  }
  
  private Hashtable mnemonicToIndexMap;
  
  /**
   * InputMap used for mnemonics. Only non-null if the JTabbedPane has
   * mnemonics associated with it. Lazily created in initMnemonics.
   */
  private InputMap mnemonicInputMap;
  
  /**
   * Reloads the mnemonics. This should be invoked when a memonic changes,
   * when the title of a mnemonic changes, or when tabs are added/removed.
   */
  private void updateMnemonics() {
    resetMnemonics();
    for (int c = tabPane.getTabCount() - 1; c >= 0; c--) {
      int mnemonic = tabPane.getMnemonicAt(c);
      if (mnemonic > 0) {
        addMnemonic(c, mnemonic);
      }
    }
  }
  
  /**
   * Resets the mnemonics bindings to an empty state.
   */
  private void resetMnemonics() {
    if (mnemonicToIndexMap != null) {
      mnemonicToIndexMap.clear();
      mnemonicInputMap.clear();
    }
  }
  
  /**
   * Adds the specified mnemonic at the specified index.
   */
  private void addMnemonic(int index, int mnemonic) {
    if (mnemonicToIndexMap == null) {
      initMnemonics();
    }
    mnemonicInputMap.put(KeyStroke.getKeyStroke(mnemonic, Event.ALT_MASK), "setSelectedIndex");
    mnemonicToIndexMap.put(new Integer(mnemonic), new Integer(index));
  }
  
  /**
   * Installs the state needed for mnemonics.
   */
  private void initMnemonics() {
    mnemonicToIndexMap = new Hashtable();
    mnemonicInputMap = new ComponentInputMapUIResource(tabPane);
    SwingUtilities.replaceUIInputMap(tabPane,
        JComponent.WHEN_IN_FOCUSED_WINDOW,
        mnemonicInputMap);
  }
  
  public void paint(Graphics g, JComponent c) {
    checkHoverTab(-1, -1);
    super.paint(g, c);
    updateMnemonics();
  }
  
  private  Color selectedColor;
  protected void paintContentBorder(Graphics g, int tabPlacement,
      int selectedIndex) {
    
    if (tabPlacement != TOP) {
      super.paintContentBorder(g, tabPlacement, selectedIndex);
      return;
    }
    
    int width = tabPane.getWidth();
    int height = tabPane.getHeight();
    Insets insets = tabPane.getInsets();
    
    int x = insets.left;
    int y = insets.top;
    int w = width - insets.right - insets.left;
    int h = height - insets.top - insets.bottom;
    y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
    h -= (y - insets.top);
    
//  if (selectedColor == null) {
//  g.setColor(UIManager.getColor("TabbedPane.background")); //tabPane.getBackground());
//  }
//  else {
//  g.setColor(lightHighlight);
//  }
    g.setColor(shadow);
    // draw top line
    
    g.setColor(lightHighlight);
//  g.setColor(Color.black);
    // fill content area
    g.fillRect(x+1,y+1,w-2,h-2);
    g.setColor(shadow);
    Rectangle selRect = selectedIndex < 0 ? null :
      getTabBounds(selectedIndex, calcRect);
    if (selRect != null) {
      int selRectRightX = selRect.x+selRect.width + 6;
      if (selectedTabDrawShape != null) {
        Rectangle r = selectedTabDrawShape.getBounds();
        selRectRightX = r.x + r.width - round;
      }
      if (selRect.x >= x) {
        if (selRect.x > x + 1)
          g.drawLine(x, y, selRect.x, y);
        // Break line to show visual connection to selected tab
        g.drawLine(selRectRightX-2, y, x + w - 1, y);
      }
    }
    if (isTabPaneFocusAncestor) {
      g.setColor(selectedColor);
    }
    else {
      g.setColor(highlight);
    }
//  g.setColor(Color.orange);
//  x+=1; w-=2;
    y+=1; h-=1;
    paintContentBorderTopEdge(g, tabPlacement, selectedIndex, x, y, w, h);
    paintContentBorderLeftEdge(g, tabPlacement, selectedIndex, x, y, w, h);
    paintContentBorderBottomEdge(g, tabPlacement, selectedIndex, x, y, w, h);
    paintContentBorderRightEdge(g, tabPlacement, selectedIndex, x, y, w, h);
  }
  
  protected void paintContentBorderTopEdge(Graphics g, int tabPlacement,
      int selectedIndex,
      int x, int y, int w, int h) {
    if (tabPlacement != TOP) {
      super.paintContentBorderTopEdge(g, tabPlacement, selectedIndex, x, y, w, h);
      return;
    }
    Rectangle selRect = selectedIndex < 0? null :
      getTabBounds(selectedIndex, calcRect);
    
//  g.setColor(selectedColor);
    
    if (true || isTabPaneFocusAncestor) { //tabPane.hasFocus() || tabPane.getSelectedComponent().hasFocus()) {
      if (true) {
        for (int i = 0; i < cbThick; i++) {
          g.drawLine(x+i, y+i, x+w-i-1, y+i);
        }
      }
      else {
//      Break line to show visual connection to selected tab
        if (selRect.x > x) {
          g.drawLine(x, y, selRect.x+1, y);
          if (selRect.x > x+1) {
            g.drawLine(x+1, y+1, selRect.x+1, y+1);
          }
        }
        int selRectRight = selRect.x + selRect.width + 37;
        if (selRectRight < x + w - 2) {
          g.drawLine(selRectRight - 1, y, x+w-1, y);
          if (selRectRight < x + w - 3) {
            g.drawLine(selRectRight - 1, y+1, x+w-2, y+1);
          }
        }
//      else {
//      g.setColor(lightHighlight);
//      g.drawLine(x+w-2, y, x+w-2, y);
//      }
      }
    }
  }
  
  protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement,
      int selectedIndex,
      int x, int y, int w, int h) {
    if (tabPlacement != TOP)
      super.paintContentBorderLeftEdge(g, tabPlacement, selectedIndex, x, y, w, h);
    
//  g.setColor(selectedColor);
    
    if (true || isTabPaneFocusAncestor) { //tabPane.hasFocus() || tabPane.getSelectedComponent().hasFocus()) {
      for (int i = 0; i < cbThick; i++) {
        g.drawLine(x+i, y+i, x+i, y+h-i-1);
      }
//    g.drawLine(x, y, x, y+h-2);
//    g.drawLine(x+1, y+1, x+1, y+h-2);
//    g.drawLine(x+2, y+2, x+2, y+h-3);
    }
  }
  
  protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
      int selectedIndex,
      int x, int y, int w, int h) {
    if (tabPlacement != TOP)
      super.paintContentBorderBottomEdge(g, tabPlacement, selectedIndex, x, y, w, h);
    
//  g.setColor(selectedColor);
    
    if (true || isTabPaneFocusAncestor) { //tabPane.hasFocus() || tabPane.getSelectedComponent().hasFocus()) {
      for (int i = 0; i < cbThick; i++) {
        g.drawLine(x+i, y+h-i-1, x+w-i-1, y+h-i-1);
      }
//    g.drawLine(x, y+h-1, x+w-1, y+h-1);
//    g.drawLine(x+1, y+h-2, x+w-2, y+h-2);
//    g.drawLine(x+2, y+h-3, x+w-3, y+h-3);
    }
  }
  
  protected void paintContentBorderRightEdge(Graphics g, int tabPlacement,
      int selectedIndex,
      int x, int y, int w, int h) {
    if (tabPlacement != TOP)
      super.paintContentBorderRightEdge(g, tabPlacement, selectedIndex, x, y, w, h);
    
//  g.setColor(selectedColor);
    
    if (true || isTabPaneFocusAncestor) { //tabPane.hasFocus() || tabPane.getSelectedComponent().hasFocus()) {
      for (int i = 0; i < cbThick; i++) {
        g.drawLine(x+w-i-1, y+i, x+w-i-1, y+h-i-1);
      }
//    g.drawLine(x+w-1, y, x+w-1, y+h-1);
//    g.drawLine(x+w-2, y+1, x+w-2, y+h-2);
//    g.drawLine(x+w-3, y+2, x+w-3, y+h-3);
    }
  }
//protected void paintFocusIndicator(Graphics g, int tabPlacement,
//Rectangle[] rects, int tabIndex, Rectangle iconRect,
//Rectangle textRect, boolean isSelected) {
//super.paintFocusIndicator(g, tabPlacement, rects, tabIndex, iconRect,
//textRect, isSelected);
//}
//protected void paintIcon(Graphics g, int tabPlacement, int tabIndex,
//Icon icon, Rectangle iconRect, boolean isSelected) {
//super.paintIcon(g, tabPlacement, tabIndex, icon, iconRect, isSelected);
//}
//protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects,
//int tabIndex, Rectangle iconRect, Rectangle textRect) {
//super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
//}
//protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
////if (parentUI == null) {
//super.paintTabArea(g, tabPlacement, selectedIndex);
////}
////else {
////parentUI.rects = rects;
////parentUI.tabPane = tabPane;
////parentUI.tabRuns = tabRuns;
////parentUI.paintTabArea(g, tabPlacement, selectedIndex);
////}
//}
  protected void paintTabBackground(Graphics g, int tabPlacement,
      int tabIndex, int x, int y, int w, int h, boolean isSelected) {
    Color c = tabPane.getBackgroundAt(tabIndex);
    if (isTabPaneFocusAncestor && isSelected && selectedColor != null)
      c = selectedColor;
    else if (c instanceof UIResource) {
      c =
//      UIManager.getColor("TabbedPane.background");
        tabPane.getBackground();
//    selectedColor;
      if (c instanceof UIResource) {
        c = highlight;
      }
    }
    g.setColor(c);
//  g.setColor(Color.PINK);
    switch(tabPlacement) {
    case TOP:
//    g.fillRect(x+1, y+1, w-3, h-1);
      if (isSelected) {
        w -= 27;
        int size = 2*round;
        int bound = size;
        int bbound = bound + bound - round;
        if (tabIndex > 0 || getTabRunCount(tabPane) > 1) {
          g.fillRect(x+round-1, y, w-round, h);
          g.fillRect(x, y+round, round-1, h-round);
          g.fillArc(x, y, bound, bound, 180, -90); // top-left corner
        }
        else {
          g.fillRect(x+round-1, y, w-round, h);
          g.fillRect(x, y+round-1, round, h-round+1);
          g.fillArc(x-1, y-1, bound, bound, 180, -90); // top-left corner
        }
        if (selectedTabDrawShape != null && g instanceof Graphics2D) {
          Rectangle rect = selectedTabDrawShape.getBounds();
          Graphics2D g2 = (Graphics2D) g;
          Color hi = c.brighter();
          float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
          float diff = 0.15f;
          float bright = hsb[2] * (1 + diff);
          if (bright > 1.0)
            bright = hsb[2] * (1 - diff / 2.0f);
          hi = Color.getHSBColor(hsb[0], hsb[1], bright);
//        hi = Color.green;
          Color lo = c;
//        lo = Color.magenta;
//        GradientPaint gp = new GradientPaint(rect.x, rect.y, hi, (float) (rect.x + rect.getWidth() * 0.01f), (float) (rect.y + rect.getHeight() * 1.40f), lo, true);
          GradientPaint gp = new GradientPaint(rect.x, rect.y, hi, (float) (rect.x + rect.getWidth() * 0.00f), (float) (rect.y + rect.getHeight() * 1.0f), lo, false);
          Paint paint = g2.getPaint();
          g2.setPaint(gp);
          g2.fill(selectedTabDrawShape);
          g2.setPaint(paint);
        }
        else {
          for (int i = 0; i < 29; i++) { // right "ski-slope" curve
            g.drawArc(-i+x+w-1-bound, y, bbound+round, bbound+round, 90, -65);
            g.drawLine(-i+x+w-1+bound-3, y+round-1, -i+x+w-1+bound+round-4, y+bound-2);
            g.drawArc(-i+x+w-1+bound+round-4, y, bbound+bbound, bbound, 195, 65);
          }
        }
      }
      break;
    default:
      super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
    }
  }
  protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
      int x, int y, int w, int h, boolean isSelected) {
    Rectangle rect = getTabBounds(tabPane, tabIndex);
    int tabCount = tabPane.getTabCount();
    int currentRun = getRunForTab( tabCount, tabIndex );
    int lastIndex = lastTabInRun( tabCount, currentRun );
    int firstIndex = tabRuns[ currentRun ];
    
    g.setColor(lightHighlight);
    
    switch (tabPlacement) {
    case TOP:
      Insets insets = tabPane.getInsets();
      int rightTopX = x+w-1;
      int bottomEdgeY = y+h-1;
      if (isSelected) {
        rightTopX -= 27;
        int size = 2*round;
        int bound = size;
        g.setColor(shadow);
//      g.drawLine(x+w-1, y, x+w-1, y+h-1); // right vertical side
        if (getTabRunCount(tabPane) > 1) {
          if (currentRun == 0) {
            int prevRun = getNextTabRun(currentRun);
            int firstPrev = tabRuns[prevRun];
            int tabAbove = firstPrev;
            for (int last = tabAbove; last < tabCount && getRunForTab(tabCount, last) == prevRun;) {
              tabAbove = last;
              last++;
            }
            int rightX = rects[tabAbove].x + rects[tabAbove].width - 1;
            if (rightX >= rightTopX) {
              g.drawLine(rightTopX, y, rightX, y);
            }
            boolean drawnRightEdge = false;
            if (selectedTabShape != null) {
              if (selectedTabDrawShape.intersects(rightX, y, 0, bottomEdgeY - y)) {
                int rx = rightX;
                int ry = y + 1;
                boolean test = false;
                int d = 1;
                Rectangle r = selectedTabDrawShape.getBounds();
                if (rx - r.x > r.width / 2) {
                  d = -1;
                  ry = r.y + r.height - 1;
                  test = true;
                }
                for (int py = ry; test == selectedTabDrawShape.contains(rx, py);) {
                  if (!test)
                    ry = py;
                  py += d;
                  if (test)
                    ry = py;
                }
                g.drawLine(rx, y, rx, ry);
                drawnRightEdge = true;
              }
            }
            if (!drawnRightEdge && rightX > rightTopX) {
              g.drawLine(rightTopX, y, rightX, y);
              if (rightX >= x + w) {
                g.drawLine(rightX, y, rightX, bottomEdgeY);
              }
            }
          }
        }
        boolean drawnShape = false;
        if (selectedTabDrawShape != null && g instanceof Graphics2D) {
          Graphics2D g2 = (Graphics2D) g;
          Paint paint = g2.getPaint();
//        g2.setColor(Color.red);
          RenderingHints hints = g2.getRenderingHints();
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
          g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
          g2.draw(selectedTabDrawShape);
          g2.setRenderingHints(hints);
          g2.setPaint(paint);
//        g.drawLine(x + w, y, x+w, y + h); // XXX draw right edge for testing
          drawnShape = true;
        }
        else {
          // right "ski-slope" curve
          int bbound = bound + bound - round;
          g.drawArc(rightTopX-bound, y, bbound+round, bbound+round, 90, -65); // top-left of curve
          g.drawLine(rightTopX+bound-3, y+round-1, rightTopX+bound+round-4, y+bound-2); // center straight of curve
          g.drawArc(rightTopX+bound+round-4, y, bbound+bbound, bbound, 195, 65); // bottom-right of curve
        }
        if (! drawnShape) {
          if (getTabRunCount(tabPane) > 1 || y >= insets.top + tabAreaInsets.top) {
            int stretchRightTopX = rightTopX;
            if (runCount == 1 ||
                (tabIndex != lastIndex)) {
              stretchRightTopX += 27;
            }
            if (firstIndex == tabIndex) {
              g.drawLine(x+round, y, stretchRightTopX, y);  // top
              g.drawArc(x, y, bound, bound, 180, -90); // top-left corner
            }
            else {
              g.drawLine(x+round, y, stretchRightTopX, y);  // top
              g.drawLine(x, y+round, x, bottomEdgeY); // left
              g.drawArc(x, y, bound, bound, 180, -90); // top-left corner
            }
          }
          else if (tabIndex > 0) {
            g.drawLine(x, y+round-1, x, bottomEdgeY); // left
            g.drawArc(x, y-1, bound, bound, 180, -90); // top-left corner
          }
          // else top-left corner drawn by tab-pane
          if (getTabRunCount(tabPane) > 1
              && y > insets.top + tabAreaInsets.top) {
            g.drawLine(x, y, x+round, y); // top
          }
        }
      }
      else { // ! isSelected
        g.setColor(shadow);
        Insets tabAreaInsets = getTabAreaInsets(tabPlacement);
//      if (tabIndex == 0 || x <= insets.left + tabAreaInsets.left) {
//      // only draw left edge if this is the left-most tab
//      // okay, never draw left edge
//      }
        int selectedIndex = tabPane.getSelectedIndex();
        int selectedRun = getRunForTab(tabCount, selectedIndex);
        int tabRunOverlay = getTabRunOverlay(tabPlacement);
        if (true || selectedRun == currentRun)
          tabRunOverlay = 1;
        if (selectedRun != currentRun || tabIndex + 1 != selectedIndex)
          // don't draw right edge if selected tab is to the right
          g.drawLine(rightTopX, y, rightTopX, y+h-tabRunOverlay); // right line
        if (getTabRunCount(tabPane) > 1
            && //currentRun !=  getRunForTab(tabCount, tabPane.getSelectedIndex()))
            y > insets.top + tabAreaInsets.top) {
          int right = rightTopX;
//        if (selectedRun == currentRun && tabIndex+1 == selectedIndex)
//        right += round;
          // only draw top line if
          g.drawLine(x, y, right, y); // topline
        }
//      if (getTabRunCount(tabPane) > 1
//      && currentRun !=  getRunForTab(tabCount, tabPane.getSelectedIndex()))
//      // only draw bottom line if not in the bottom-most run, i.e, the one with the selected tab
//      g.drawLine(x, y+h-2, x+w-1, y+h-2); // bottom line
      }
      break;
    default:
      super.paintTabBorder(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
    }
  }
  protected void paintText(Graphics g, int tabPlacement, Font font,
      FontMetrics metrics, int tabIndex, String title,
      Rectangle textRect, boolean isSelected) {
    super.paintText(g, tabPlacement, font, metrics, tabIndex, debugTitle(tabIndex, title),
        textRect, isSelected);
  }
////boolean requestFocusForVisibleComponent() {
////return super.requestFocusForVisibleComponent();
////}
//protected void selectAdjacentRunTab(int tabPlacement, int tabIndex,
//int offset) {
//super.selectAdjacentRunTab(tabPlacement, tabIndex, offset);
//}
//protected void selectNextTab(int current) {
//super.selectNextTab(current);
//}
//protected void selectNextTabInRun(int current) {
//super.selectNextTabInRun(current);
//}
//protected void selectPreviousTab(int current) {
//super.selectPreviousTab(current);
//}
//protected void selectPreviousTabInRun(int current) {
//super.selectPreviousTabInRun(current);
//}
//protected void setVisibleComponent(Component component) {
//super.setVisibleComponent(component);
//}
//protected boolean shouldPadTabRun(int tabPlacement, int run) {
//return super.shouldPadTabRun(tabPlacement, run);
//}
//protected boolean shouldRotateTabRuns(int tabPlacement) {
//return super.shouldRotateTabRuns(tabPlacement);
//}
//public int tabForCoordinate(JTabbedPane pane, int x, int y) {
//return super.tabForCoordinate(pane, x, y);
//}
//protected void uninstallComponents() {
//super.uninstallComponents();
//}
//protected void uninstallDefaults() {
//super.uninstallDefaults();
//}
//protected void uninstallKeyboardActions() {
//super.uninstallKeyboardActions();
//}
//public int getAccessibleChildrenCount(JComponent c) {
//return super.getAccessibleChildrenCount(c);
//}
//public boolean contains(JComponent c, int x, int y) {
//return super.contains(c, x, y);
//}
//public Accessible getAccessibleChild(JComponent c, int i) {
//return super.getAccessibleChild(c, i);
//}
//public void update(Graphics g, JComponent c) {
//super.update(g, c);
//}
//public int hashCode() {
//return super.hashCode();
//}
//protected void finalize() throws Throwable {
//super.finalize();
//}
//protected Object clone() throws CloneNotSupportedException {
//return super.clone();
//}
//public boolean equals(Object obj) {
//return super.equals(obj);
//}
//public String toString() {
//return super.toString();
//}
  
  /**
   * The width and height of the bounding box around the rounded left corners of
   * tabs and both top corners of the whole pane's border
   */
  protected int round = 7;
  
  /**
   * How thick to make the content boundary
   */
  protected int cbThick = 5;
  
  /**
   * Constant used as the constraint when adding close tab buttons to the tab pane
   */
  private static final String CLOSE_TAB_BUTTON = "close tab button";
  
  protected void installDefaults() {
    super.installDefaults();
    // we want tabs to have smaller than default size labels
    Font font = tabPane.getFont();
    float size = Math.max(font.getSize2D() - 1.0f, 10.0f);
    font = font.deriveFont(Font.PLAIN);
    font = font.deriveFont(size);
    tabPane.setFont(font);
//  selectedColor = UIManager.getColor("ScrollBar.thumbHighlight");//lightHighlight; //UIManager.getColor("TabbedPane.selected");
    // TODO pick a color that looks good in the Windows L&F
    selectedColor = UIManager.getColor("ScrollBar.thumb");
//  selectedTabPadInsets.right += selectedTabPadInsets.left + 1;
//  selectedTabPadInsets.left = 0;
//  selectedTabPadInsets.left = round;
//  selectedTabPadInsets = new Insets(selectedTabPadInsets.top, selectedTabPadInsets.left+round, selectedTabPadInsets.bottom, selectedTabPadInsets.right+round);
//  selectedTabPadInsets = new Insets(selectedTabPadInsets.top, selectedTabPadInsets.left, selectedTabPadInsets.bottom, selectedTabPadInsets.right);
//  selectedTabPadInsets = new Insets(selectedTabPadInsets.top, selectedTabPadInsets.left, selectedTabPadInsets.bottom, selectedTabPadInsets.right+closeTabButton.getIcon().getIconWidth() + 1);
    selectedTabPadInsets = new Insets(selectedTabPadInsets.top, 0, selectedTabPadInsets.bottom, selectedTabPadInsets.right+30 + textIconGap);
//  selectedTabPadInsets = new Insets(selectedTabPadInsets.top, selectedTabPadInsets.left, selectedTabPadInsets.bottom, selectedTabPadInsets.right+closeTabButton.getIcon().getIconWidth() + 1);
    contentBorderInsets = new Insets(contentBorderInsets.top+cbThick - 1, contentBorderInsets.left+cbThick - 1, contentBorderInsets.bottom+cbThick - 1, contentBorderInsets.right+cbThick - 1);
//  tabInsets = new Insets(tabInsets.top+1, tabInsets.left+1, tabInsets.bottom+1, tabInsets.right+1);
//  tabInsets = new Insets(tabInsets.top+1, tabInsets.left, tabInsets.bottom+1, tabInsets.right+closeTabButton.getIcon().getIconWidth() + 1);
    tabInsets = new Insets(tabInsets.top+1, tabInsets.left, tabInsets.bottom+1, tabInsets.right);
    tabRunOverlay = Math.max(tabRunOverlay - 2, 0);
  }
  
  protected boolean isLastTabInRun(int tabIndex) {
    boolean isLastTabInRun = false;
    if (runCount > 1) {
      int tabCount = tabPane.getTabCount();
      isLastTabInRun = lastTabInRun(tabCount, getRunForTab(tabCount, tabIndex)) == tabIndex;
    }
    return isLastTabInRun;
  }
  
  protected void layoutSelectedTabShape(int selectedIndex) {
    if (selectedIndex != -1) {
      int tabCount = tabPane.getTabCount();
      int run = getRunForTab(tabCount, selectedIndex);
      boolean isFirstInRun = (tabRuns[run] == selectedIndex);
      boolean isTop = (runCount == 1);
      
      Rectangle selRect = rects[selectedIndex];
      selectedTabDrawShape = selRect;
      GeneralPath s[] = new GeneralPath[2];
      // s[0] is the tab's hit boundary shape
      // s[1] is the tab's render shape
      for (int i = 0; i < 2; i++) {
        s[i] = new GeneralPath();
        float rectW = (float) selRect.getWidth();
        float rectLeftX = (float) selRect.getX();
        float drawLeftX = rectLeftX;
        if (isFirstInRun)
          drawLeftX -= 1;
        float rectRightX = rectLeftX + rectW - 30;
        float rectH = (float) selRect.getHeight();
        float rectTopY = (float) selRect.getY();
        float drawTopY = rectTopY;
        if (isTop)
          drawTopY -= 1;
        float rectBottomY = rectTopY + rectH;
        float drawBottomY = rectBottomY + 1;
        float roundf = round;
        float roundf2 = roundf + roundf;
        float roundf3 = roundf2 + roundf;
        float roundf4 = roundf3 + roundf;
        float roundf5 = roundf4 + roundf;
        float leftCornerRound = roundf;
        if (i == 0) {
          s[i].moveTo(rectLeftX, rectTopY);
          s[i].lineTo(rectRightX, rectTopY);
        }
        else {
//        s[i].moveTo(rectRightX, rectTopY);
          s[i].moveTo(drawLeftX + leftCornerRound, drawTopY);
          s[i].lineTo(rectRightX, drawTopY);
        }
        //        s[i].quadTo(rectRightX + roundf3, rectTopY + roundf, rectRightX + roundf2, rectTopY + roundf2);
        //        s[i].quadTo(rectRightX + roundf3, rectBottomY - roundf, rectRightX + roundf5, rectBottomY);
        float controlPointX = rectRightX + roundf3 - roundf / 2;
        if (i == 0) {
          s[i].curveTo(
              controlPointX, rectTopY,
              controlPointX, rectBottomY,
              rectRightX + roundf5 + roundf / 2, rectBottomY);
        }
        else {
          s[i].curveTo(
              controlPointX, drawTopY,
              controlPointX, rectBottomY,
              rectRightX + roundf5 + roundf / 2, drawBottomY);
        }
        if (i == 0) {
          s[i].lineTo(rectLeftX, rectBottomY);
          s[i].lineTo(rectLeftX, rectTopY);
        }
        else {
          s[i].lineTo(drawLeftX, drawBottomY);
          s[i].lineTo(drawLeftX, drawTopY + leftCornerRound);
          s[i].quadTo(drawLeftX, drawTopY, drawLeftX + leftCornerRound, drawTopY);
        }
      }
      selectedTabShape = s[0];
      selectedTabDrawShape = s[1];
    }
  }
  
  protected void endDrag() {
    Container window = rootPane.getTopLevelAncestor();
    if (savedCursor != null) {
//    rootPane.setCursor(savedCursor);
      JRootPane jRootPane = (window instanceof JComponent) ?
          ((JComponent) window).getRootPane() :
            rootPane.getRootPane();
          jRootPane.getGlassPane().setCursor(savedCursor);
          jRootPane.getGlassPane().setVisible(false);
//        tabPane.setCursor(savedCursor);
          savedCursor = null;
    }
    if (savedWindowCursor != null) {
      window.setCursor(savedWindowCursor);
      savedWindowCursor = null;
    }
    if (dragComponent != null) {
      dragComponent.setVisible(false);
      dragComponent = null;
    }
    isDragging = false;
    isSizing = false;
    isMoving = false;
    if (pressedTabComponent != null) {
      draggableStateTable.remove(pressedTabComponent);
    }
    pressedTabIndex = -1;
    pressedTabComponent = null;
  }
  
  protected GeneralPath createTabPaneBorderShape(int x, int y, int width, int height) {
    GeneralPath s = new GeneralPath();
    float roundf = round;
    float leftX = x;
    float rightX = x + width - 1;
    float topY = y;
    float bottomY = y + height - 1;
    s.moveTo(leftX + roundf, topY);
    s.lineTo(rightX - roundf, topY);
    s.quadTo(rightX, topY, rightX, topY + roundf);
    s.lineTo(rightX, bottomY);
    s.lineTo(leftX, bottomY);
    s.lineTo(leftX, topY + roundf);
    s.quadTo(leftX, topY, leftX + roundf, topY);
    return s;
  }
  
  protected int checkHoverTab(int x, int y) {
    Point oldPoint = hoverPoint;
    if (x >= 0 && y >= 0) {
      hoverPoint = new Point(x, y);
    }
    int oldHover = hoverTab;
    hoverTab = hoverPoint != null ? tabForCoordinate(tabPane, hoverPoint.x, hoverPoint.y) : -1;
    if (oldHover != hoverTab) {
      CloseTabButton button;
      if (oldHover != -1) {
        button = (CloseTabButton) draggableTabbedPane.getTabComponent(oldHover, CLOSE_TAB_BUTTON);
        if (button != null)
          button.repaint();
      }
      if (hoverTab != -1) {
        button = (CloseTabButton) draggableTabbedPane.getTabComponent(hoverTab, CLOSE_TAB_BUTTON);
        if (button != null)
          button.repaint();
      }
    }
    return hoverTab;
  }
}
