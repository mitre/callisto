
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
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.FocusManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.plaf.basic.BasicButtonUI;

import org.mitre.jawb.DebugLevel;
import org.mitre.jawb.gui.drag.JawbDraggableTabbedPane;
import org.mitre.jawb.swing.DetachableTabbedPane;
import org.mitre.jawb.swing.DetachableTabbedPane.Detachable;
import org.mitre.jawb.swing.drag.BasicDraggableTabbedPaneUI.CloseTabButton;

/**
 * TODO Describe type
 * 
 * @author Galen B. Williamson
 * @version Dec 13, 2004
 */
public class DraggableTabbedPane extends JPanel {

  private final static int DEBUG = DebugLevel.getDebugLevel(DraggableTabbedPane.class, 0);

  static {
    System.err.println(DraggableTabbedPane.class.toString());
  }

  protected static class AddConstraintsComparator implements Comparator {
    private static AddConstraintsComparator INSTANCE = null;
    
    public static AddConstraintsComparator getInstance() {
      if (INSTANCE == null) {
        INSTANCE = new AddConstraintsComparator();
      }
      return INSTANCE;
    }
    
    private AddConstraintsComparator() {}
    
    public int compare(Object o1, Object o2) {
      if (o1 instanceof String && o2 instanceof String) {
        return compareConstraints(new ConstraintSplitter((String) o1).split(),
            new ConstraintSplitter((String) o2).split(), 0);
      }
      AddConstraints a1 = (AddConstraints) o1;
      AddConstraints a2 = (AddConstraints) o2;
      int c = 0;
      String c1 = a1.constraints;
      String c2 = a2.constraints;
      String[] cs1 = new ConstraintSplitter(c1).split();
      String[] cs2 = new ConstraintSplitter(c2).split();
      c = compareConstraints(cs1, cs2, 0);
      if (c == 0) {
        c = (a1.index < a2.index ? -1 : (a1.index == a2.index ? 0 : 1)); 
      }
      return c;
    }

    private int compareConstraints(String[] c1, String c2[], int i) {
      if (i >= c1.length) {
        if (i >= c2.length) {
          return 0;
        }
        return -1;
      }
      if (i >= c2.length) {
        return 1;
      }
      int c = compareConstraints(c1[i], c2[i]);
      if (c == 0) {
        return compareConstraints(c1, c2, i + 1);
      }
//      else {
//        if (c1.length != c2.length) {
//          c = c2.length - c1.length;
//        }
//      }
      return c;
    }

    private int compareConstraints(String c1, String c2) {
      int o1 = orderConstraint(c1);
      int o2 = orderConstraint(c2);
      if (o1 < o2)
        return -1;
      if (o1 > o2)
        return 1;
      return 0;
    }

    private int orderConstraint(String constraint) {
      int i = 0;
      if (constraint.equals(TOP))
        i = 0;
      else if (constraint.equals(BOTTOM))
        i = 1;
      else if (constraint.equals(LEFT))
        i = 2;
      else if (constraint.equals(RIGHT))
        i = 3;
      return i;
    }
  }

  protected class ComponentMenuItem extends JMenuItem {
    protected final ComponentMenuAction a;
    protected final Component c;
    
    Font currentFont = super.getFont();
    
    Color currentColor = super.getForeground();
    private boolean debugInfo;
    
    protected ComponentMenuItem(ComponentMenuAction a, Component c, boolean debugInfo) {
      super();
      this.a = a;
      this.c = c;
      this.debugInfo = debugInfo ;
      setAction(a);
    }
    
    public String getText() {
      Action action = getAction();
      if (action == null || DraggableTabbedPane.this == null)
        return null;
      String name = (String) action.getValue(Action.NAME);
      if ((debugInfo || DEBUG > 0) && DraggableTabbedPane.this != null) {
        AddConstraints currentConstraints = getCurrentConstraints(c);
        name += ": " + currentConstraints;
      }
      checkPresentation(a);
      if (! c.isEnabled()) {
        name = "<html><body><font style=\"color: gray; font-style: normal\">"+name+"</font></body></html>";
      }
      if (! name.equals(super.getText())) {
        setText(name);
      }
      return name;
    }
    
    private void checkPresentation(final ComponentMenuAction a) {
      Color fg = super.getForeground();
      if (a == null)
        return;
      if (a.isSelected()) { // currentConstraints.enabled) {
        currentFont = super.getFont();
        currentColor = fg;
      }
      else {
        currentFont = super.getFont().deriveFont(Font.ITALIC);
        currentFont = currentFont.deriveFont(Font.BOLD);
        currentColor = fg.darker();
        if (fg.equals(currentColor))
          currentColor = fg.brighter();
      }
    }
    
    public Color getForeground() {
      checkPresentation(a);
      return currentColor;
    }
    
    public Font getFont() {
      checkPresentation(a);
      return currentFont;
    }
    
    public boolean isEnabled() {
      Action action = getAction();
      if (action == null)
        return super.isEnabled();
      if (action.isEnabled()) {
        return true;
      }
      getParent().remove(this);
      return false;
    }
  }

    protected class FocusWatcher
        implements PropertyChangeListener
    {
        private Component previousOwner;
        private Component currentOwner;
        public void propertyChange(PropertyChangeEvent evt) {
            FocusManager fm = FocusManager.getCurrentManager();
            Component newOwner = fm.getFocusOwner();
            if (newOwner != currentOwner) {
                if (currentOwner != null)
                    previousOwner = currentOwner;
                currentOwner = newOwner;
            }
        }
        public Component getCurrentOwner() {
            return currentOwner;
        }
        public Component getPreviousOwner() {
            return previousOwner;
        }
    }

    public final static String LEFT = JSplitPane.LEFT;
    public final static String RIGHT = JSplitPane.RIGHT;
    public final static String TOP = JSplitPane.TOP;
    public final static String BOTTOM = JSplitPane.BOTTOM;
    public final static int VERTICAL_SPLIT = JSplitPane.VERTICAL_SPLIT;
    public final static int HORIZONTAL_SPLIT = JSplitPane.HORIZONTAL_SPLIT;
    public final static String TABBED_PANE_PARENT = "TabbedPaneParent";
    
    protected int tabPlacement;
    protected boolean detachable;
    protected int tabLayoutPolicy;
    protected boolean continuousLayout;
    protected Image detachedIconImage = null;
    protected Map mnemonicTitleMap = null;
    
    Component dragComponent = null;
    
    protected interface ForciblyInvisible {
      public boolean isForcedInvisible();
      public boolean isTreeForcedInvisible();
    }
    
    public class TabbedPane extends DetachableTabbedPane implements ForciblyInvisible {

        protected JToggleButton minButton;
        protected JToggleButton maxButton;
        protected TabPaneAncestry ancestry;
        protected boolean emptied = false;
        protected Container savedParent;
        protected Container savedGrandParent;
        protected SplitPane splitParent;
        protected SplitPane splitGrandParent;
        protected boolean leftChild;
        protected boolean leftParent;
        protected BasicDraggableTabbedPaneUI ui;
        protected Container lastParent = null;
        protected int savedOrientation;
        protected DetachedPanelList detachedPanelList = null;
        protected JScrollPane detachedPanelListScrollPane = null;
        protected int detachedPanelIndex = -1;
        
        protected class DetachedPanelList extends JList {
            /**
             * TODO Describe type
             * 
             * @author Galen B. Williamson
             * @version Dec 27, 2004
             */
            protected class DetachedPanelListCellRenderer extends
            DefaultListCellRenderer {
                
                public Component getListCellRendererComponent(JList list,
                                                              Object value, int index, boolean isSelected,
                                                              boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
                        index, isSelected, cellHasFocus);
                    DraggableDetachable d = (DraggableDetachable) value;
                    label.setText(d.getTitle());
                    return label;
                }
            }
            public DetachedPanelList() {
                super(new DefaultListModel());
                DefaultListModel lm = (DefaultListModel) getModel();
                Iterator i = panelToDetMap.values().iterator();
                while (i.hasNext()) {
                    DraggableDetachable dd = (DraggableDetachable) i.next();
                    lm.addElement(dd);
                }
            }
            public ListCellRenderer getCellRenderer() {
                return new DetachedPanelListCellRenderer();
            }
        }
        
        public boolean showEmptyTab() {
            if (detachedPanelList != null) {
                if (detachedPanelIndex != -1) {
                    Component c = getComponentAt(detachedPanelIndex);
                    if (c instanceof JScrollPane) {
                        JScrollPane sp = (JScrollPane) c;
                        Component cc = sp.getViewport().getView();
                        if (c == detachedPanelList && c.isVisible()) {
                            return false;
                        }
                    }
                }
            }
            if (panelToDetMap.isEmpty() || getTabCount() > 0)
                return false;
            detachedPanelList = new DetachedPanelList();
            detachedPanelListScrollPane = new JScrollPane(detachedPanelList);
            insertTab(DraggableDetachable.DETACHED_PANEL_LIST_TITLE, null, detachedPanelListScrollPane, "Detached Panel List", -1);
            detachedPanelIndex = indexOfComponent(detachedPanelListScrollPane);
            return detachedPanelIndex != -1;
        }

        // TODO Ctrl-Alt arrow key actions to switch focus between tab panes
        final KeyStroke KS_CA_PD = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_MASK | InputEvent.ALT_MASK);
        final KeyStroke KS_CA_PU = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_MASK | InputEvent.ALT_MASK);
        final Action nextTabAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int selectedTab = getSelectedIndex();
                if (selectedTab != -1) {
                    int newTab = (selectedTab + 1) % getTabCount();
                    if (selectedTab != newTab)
                        setSelectedIndex(newTab);
                }
            }
        };
        final Action prevTabAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int selectedTab = getSelectedIndex();
                if (selectedTab != -1) {
                    int newTab = selectedTab - 1;
                    if (newTab < 0)
                        newTab = getTabCount() - 1;
                    if (selectedTab != newTab)
                        setSelectedIndex(newTab);
                }
            }
        };
        public TabbedPane(int tabPlacement, int tabLayoutPolicy, boolean detachable) {
            super(tabPlacement, tabLayoutPolicy, detachable);
            ui.rootPane = DraggableTabbedPane.this;
            Color borderColor = UIManager.getColor("TabbedPane.shadow");
            super.setBorder(//BorderFactory.createCompoundBorder(
//              BorderFactory.createLineBorder(borderColor, 1)
                ui.getBorder(borderColor)
//              ,
//              BorderFactory.createEmptyBorder(3, 3, 3, 3)
//              )
            );
            if (DraggableTabbedPane.this.detachedIconImage != null)
                super.setDetachedIconImage(DraggableTabbedPane.this.detachedIconImage);
            InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            ActionMap actionMap = getActionMap();
            inputMap.put(KS_CA_PD, "next-tab");
            actionMap.put("next-tab", nextTabAction);
            inputMap.put(KS_CA_PU, "prev-tab");
            actionMap.put("prev-tab", prevTabAction);
            initializeSizes();
        }
        
        public boolean isForcedInvisible() {
          if (forcedInvisible)
            return true;
          return forcedInvisible;
        }

        public boolean isVisible() {
          return !isForcedInvisible() && super.isVisible();
        }
        
//        private Rectangle savedBounds = null;
//        
//        public void reshape(int x, int y, int w, int h) {
//          if (isForcedInvisible()) {
//            if (savedBounds == null) {
//              savedBounds = new Rectangle(x, y, w, h);
//            }
//            else {
//              savedBounds.setRect(x, y, w, h);
//            }
//            super.reshape(0, 0, 0, 0);
//          }
//          else {
//            super.reshape(x, y, w, h);
//          }
//        }
        
        public boolean isTreeForcedInvisible() {
          Component[] components = getComponents();
          for (int i = 0; i < components.length; i++) {
            Component c = components[i];
            if (c instanceof ForciblyInvisible) {
              if (! ((ForciblyInvisible)c).isTreeForcedInvisible()) {
                return false;
              }
            }
            else if (getTabCount() > 0 && indexOfComponent(c) != -1 && !(c instanceof FillerComponent)) { 
              return false;
            }
          }
          return true;
        }

        protected boolean forcedInvisible = false;
        public void addNotify() {
          super.addNotify();
          Container parent = getParent();
          if (parent instanceof SplitPane) {
            if (getTabCount() == 0) {
              forcedInvisible = true;
              setVisible(false);
              revalidate();
//              parent.validate();
            }
            else if (forcedInvisible && ! isVisible()) {
              setVisible(true);
              forcedInvisible = false;
              revalidate();
//              parent.validate();
            }
           }
        }

        public final void setBorder(Border border) {
            // don't mess with my border!
        }
        
        public void updateUI() {
            if (ui == null)
                ui = new BasicDraggableTabbedPaneUI(this);
            setUI(ui);
        }

        public void removeTabAt(int index) {
            boolean detachedPanelListRemoved = false;
            if (detachedPanelListScrollPane == getComponentAt(index)) {
                detachedPanelListRemoved = true;
            }
            Component c = getComponentAt(index);
            int mnemonic = getMnemonicAt(index);
            if (mnemonic != -1) {
              mnemonicTitleMap.remove(new Integer(mnemonic));
            }
            super.removeTabAt(index);
            if (detachedPanelListRemoved) {
                detachedPanelList.setVisible(false);
                detachedPanelList = null;
                detachedPanelListScrollPane = null;
                detachedPanelIndex = -1;
                emptied = false;
            }
            else {
                emptied = panelToDetMap.isEmpty();
            }
            if (index != -1 && ! detachedPanelListRemoved && ! fixingIndices) {
                removeTabComponent(this, c);
                checkForcedVisibility();
            }
        }

//        protected void checkForcedVisibility() {
//          Container parent = getParent();
//          if (parent != null) {
//            boolean treeForcedInvisible = isTreeForcedInvisible();
//            if (treeForcedInvisible) {
//              forcedInvisible = true;
//              setVisible(false);
//              invalidate();
//              parent.validate();
//            }
//            else if (forcedInvisible || ! isVisible()) {
//              forcedInvisible = false;
//              setVisible(true);
//              invalidate();
//              parent.validate();
//            }
//          }
//        }

        boolean checkingForcedVisibility = false;
        protected void checkForcedVisibility() {
          if (checkingForcedVisibility)
            return;
          checkingForcedVisibility = true;
          try {
            Container parent = getParent();
            if (parent != null) {
              boolean treeForcedInvisible = isTreeForcedInvisible();
              if (treeForcedInvisible) {
                initializeSizes();
                forcedInvisible = true;
                if (isVisible()) {
                  setVisible(false);
                  revalidate();
//                parent.validate();
                }
              }
              else if (forcedInvisible || ! isVisible()) {
                resetSizes();
                forcedInvisible = false;
                if (! isVisible()) {
                  setVisible(true);
                  revalidate();
//                parent.validate();
                }
              }
            }
          } finally {
            checkingForcedVisibility = false;
          }
        }

        public void invalidate() {
          if (isValid()) {
            checkForcedVisibility();
          }
          super.invalidate();
        }

        private HashMap tabButtons = new HashMap();
        private WeakHashMap tabButtonsR = new WeakHashMap();
        
        public Component getTabComponent(int tabIndex, String tag) {
          WeakReference ref = (WeakReference) tabButtons.get(getTabComponentKey(tabIndex, tag));
          if (ref != null)
            return (Component) ref.get();
          return null;
        }

        private String getTabComponentKey(int tabIndex, String tag) {
          return tabIndex+":"+tag;
        }
        
        public void remove(Component comp) {
          if (comp instanceof CloseTabButton) {
            String key = (String) tabButtonsR.remove(comp);
            if (key != null)
              tabButtons.remove(key);
          }
          super.remove(comp);
        }

        protected void addImpl(Component comp, Object constraints, int index) {
          if (constraints instanceof String) { //CLOSE_TAB_BUTTON.equals(constraints)) {
            String key = getTabComponentKey(index, (String) constraints);
            tabButtons.put(key, new WeakReference(comp));
            tabButtonsR.put(comp, key);
            super.addImpl(comp, null, -1);
          }
          else {
            if (comp instanceof JScrollPane) {
              JScrollPane sp = (JScrollPane) comp;
              Border border = sp.getBorder();
              if (border instanceof UIResource)
                sp.setBorder(BorderFactory.createEmptyBorder());
            }
            int tabCount = getTabCount();
            if (true || tabCount == 1) {
              if (detachedPanelListScrollPane != null) {
                if (comp != detachedPanelListScrollPane) {
                  int removeIndex = indexOfComponent(detachedPanelListScrollPane);
                  if (removeIndex != -1 && removeIndex < tabCount) {
                    // TODO: check whether this is working right, had java.lang.ArrayIndexOutOfBoundsException: 1 >= 1
                    remove(removeIndex);
                  }
                }
              }
            }
            // TODO handle possibility that grand parent is gone when
            // re-attaching
//          if (false && this.ancestry != null) {
//          TabPaneAncestry ancestry = this.ancestry;
//          Container parent = ancestry.getParent();
//          while (ancestry != null
//          && parent != DraggableTabbedPane.this) {
//          if (DraggableTabbedPane.this.isAncestorOf(parent)) {
//          break;
//          }
//          else {
//          ancestry = ancestry.grandParent;
//          parent = ancestry.getParent();
//          }
//          }
//          if (ancestry == null) {
//          // TODO what to do? maybe split into root pane?
//          }
//          else if (parent == DraggableTabbedPane.this) {
//          if (leftChild) {
////        splitRootPane(this, this.ancestry.orientation,
////        this.ancestry.anchor);
//          }
//          }
//          else {

//          }
//          }
//          else 
            if (savedGrandParent != null) {
              if (splitGrandParent != null) {
                Component sibling;
                if (leftParent) {
                  sibling = splitGrandParent.getLeftComponent();
                }
                else {
                  sibling = splitGrandParent.getRightComponent();
                }
                splitGrandParent.remove(sibling);
                int orientation = JSplitPane.VERTICAL_SPLIT;
                if (splitGrandParent.getOrientation() != JSplitPane.HORIZONTAL_SPLIT)
                  orientation = JSplitPane.HORIZONTAL_SPLIT;
                SplitPane splitParent = new SplitPane(orientation, continuousLayout,
                    leftChild ? this : sibling, leftChild ? sibling : this, 0);
                if (leftParent)
                  splitGrandParent.setLeftComponent(splitParent);
                else
                  splitGrandParent.setRightComponent(splitParent);
                splitPanes.put(splitParent, null);
              }
              else if (savedGrandParent == DraggableTabbedPane.this) {
                splitTabbedPane(rootTabPane, this, savedOrientation, leftChild ? DraggableTabbedPane.LEFT : DraggableTabbedPane.RIGHT, -1);
              }
              savedGrandParent = splitGrandParent = null;
              savedParent = splitParent = null;
            }
            super.addImpl(comp, constraints, index);
            if (comp instanceof JComponent) {
              JComponent jcomp = (JComponent) comp;
              jcomp.putClientProperty(TABBED_PANE_PARENT, this);
            }
//          if (forcedInvisible && getTabCount() > 0) {
//          if (comp != null && forcedInvisible) {
//          forcedInvisible = false;
//          setVisible(true);
//          invalidate();
//          Container parent = getParent();
//          if (parent != null)
//          parent.validate();
//          }
//          }
            checkForcedVisibility();
          }
        }
        
        public void insertTabSuper(String title, Icon icon, Component comp, String tip, int index) {
            super.insertTab(title, icon, comp, tip, index);
        }

        private Dimension maximumSize = null;
        private Dimension preferredSize = null;
        private Dimension minimumSize = null;
        
        public void setMaximumSize(Dimension maximumSize) {
          super.setMaximumSize(this.maximumSize = maximumSize);
        }

        public void setMinimumSize(Dimension minimumSize) {
          super.setMinimumSize(this.minimumSize = minimumSize);
        }

        public void setPreferredSize(Dimension preferredSize) {
          super.setPreferredSize(this.preferredSize = preferredSize);
        }

        protected void initializeSizes() {
          initSize = new Dimension(0, 0);
          super.setMaximumSize(initSize);
          super.setMinimumSize(initSize);
          super.setPreferredSize(initSize);
        }

        protected void resetSizes() {
          if (getMaximumSize() == initSize) {
            super.setMaximumSize(maximumSize);
          }
          if (getMinimumSize() == initSize) {
            super.setMinimumSize(minimumSize);
          }
          if (getPreferredSize() == initSize) {
            super.setPreferredSize(preferredSize);
          }
        }
        
        public void insertTab(String title, Icon icon, Component comp, String tip, int index) {
          if (comp instanceof FillerComponent)
            return;
            resetSizes();
            int tabCount = getTabCount();
//          if (true || tabCount == 1) {
//          if (detachedPanelListScrollPane != null) {
//          if (comp != detachedPanelListScrollPane) {
//          int removeIndex = indexOfComponent(detachedPanelListScrollPane);
//          if (removeIndex != -1) {
//          removeTabAt(removeIndex);
//          }
//          }
//          }
//          }
//            LinkedList removeComponents = new LinkedList();
//            for (int i = 0; i < tabCount; i++) {
//                Component sib = getComponentAt(i);
//                if (sib instanceof FillerComponent) {
//                    removeComponents.addLast(sib);
//                }
//            }
//            if (tabCount == 1) {
//                Component sib = getComponentAt(0);
//                if (sib instanceof FillerComponent) {
//                    removeTabAt(0);
//                }
//            }
            int mnemonic = -1;
            if (title != null) {
                int ampIndex = title.indexOf("&");
                int mnemonicIndex = ampIndex + 1;
                if (ampIndex >= 0 && title.length() > mnemonicIndex) {
                    mnemonic = title.charAt(mnemonicIndex);
                    title = title.substring(0, ampIndex) + title.substring(ampIndex+1);
                    Integer m = new Integer(mnemonic);
                    if (mnemonicTitleMap == null) {
                        mnemonicTitleMap = new HashMap();
                        mnemonicTitleMap.put(m, title);
                    }
                    else {
                        for (int i = 0; i < title.length() && mnemonicTitleMap.containsKey(m); i++) {
                            if (++mnemonicIndex >= title.length())
                              mnemonicIndex = 0;
                            m = new Integer(title.charAt(mnemonicIndex));
                        }
                        if (! mnemonicTitleMap.containsKey(m)) {
                            mnemonicTitleMap.put(m, title);
                            mnemonic = m.intValue();
                        }
                    }
                }
            }
            super.insertTab(title, icon, comp, tip, index);
            setMnemonicForComponent(comp, mnemonic);
            int newTabCount = getTabCount();
            LinkedList removeComponents = new LinkedList();
            for (int i = 0; i < newTabCount; i++) {
                Component sib = getComponentAt(i);
                if (sib != comp && sib instanceof FillerComponent) {
                    removeComponents.addLast(sib);
                }
            }
            for (Iterator removeIter = removeComponents.iterator(); removeIter.hasNext();) {
                Component removeComponent = (Component) removeIter.next();
                remove(removeComponent);
            }
            if (index >= tabCount || index < 0)
                return;
            // fix up tab order so index is honored -- DetachableTabbedPane
            // ignores index argument altogether!
            if (newTabCount == tabCount)
                return;
            Object det = panelToDetMap.get(comp);
            if (det == null || ! (det instanceof DraggableDetachable))
                return;
            DraggableDetachable dd = (DraggableDetachable) det;
            int ddIndex = dd.getIndex();
            if (ddIndex == index)
                return;
            Iterator iter = panelToDetMap.values().iterator();
            while (iter.hasNext ()) {
                DraggableDetachable d = (DraggableDetachable) iter.next();
                int dIndex = d.getIndex();
                if (dIndex >= index && d != dd)
                    d.setIndex(dIndex + 1);
            }
            dd.setIndex(index);
            TreeSet dets = new TreeSet(new Comparator() {
                public int compare(Object left, Object right) {
                    if (left != null && right != null) {
                        int leftIndex = ((DraggableDetachable) left).getIndex();
                        int rightIndex = ((DraggableDetachable) right).getIndex();
                        if (leftIndex < rightIndex)
                            return -1;
                        if (leftIndex > rightIndex)
                            return 1;
                    }
                    return 0;
                }
            });
            dets.addAll(panelToDetMap.values());
            fixingIndices = true;
            removeAll();
            panelToDetMap.clear();
            iter = dets.iterator();
            while (iter.hasNext()) {
                DraggableDetachable di = (DraggableDetachable) iter.next();
                di.insertTab();
                di.dispose();
                if (di.isDetached()) {
                    Detachable detached = (Detachable) panelToDetMap.get(di.getComponent());
                    detached.setDetached(true);
                }
            }
            fixingIndices = false;
        }

        public void setMnemonicForComponent(Component comp, int mnemonic) {
          if (comp == null) return;
          int index = indexOfComponent(comp);
          if (index == -1) return;
          if (mnemonic != -1) {
            if (mnemonic >= 'a' && mnemonic <= 'z') {
              mnemonic -= ('a' - 'A');
            }
          }
          super.setMnemonicAt(index, mnemonic);
        }

        boolean fixingIndices = false;
        private Dimension initSize;
        // Fix over-compositing of icons
        protected Detachable createDetachable(String title, Icon icon,
                                              Component comp, String tip, int index) {
            return new DraggableDetachable(title, icon, comp, tip, index);
        }
        protected class DraggableDetachable extends Detachable {
            final static String DETACHED_PANEL_LIST_TITLE = "Detached";
            boolean isDetachedPanelList;
            public DraggableDetachable(String title, Icon icon, Component comp,
                                       String tip, int index) {
                super(title, icon, comp, tip, index);
                isDetachedPanelList = detachedPanelList != null
                && detachedPanelListScrollPane == comp;
                if (isDetachedPanelList) {
                    this.icon = null;
                    frame.addComponentListener(new ComponentAdapter() {
                        public void componentShown(ComponentEvent e) {
                            //frame.removeComponentListener(this);
                            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                        }
                    });
                }
            }
            public Icon getUserIcon() {
                return userIcon;
            }
            public int getIndex() {
                return index;
            }
            public void setIndex(int i) {
                index = i;
            }
            public void insertTab() {
                DraggableTabbedPane.TabbedPane pane = DraggableTabbedPane.TabbedPane.this;
                pane.insertTabSuper(title, userIcon, component, tip, index);
            }
        }
        public Icon getUserIconAt(int tabIndex) {
            Component comp = getComponentAt(tabIndex);
            return getUserIcon(comp);
        }
        public Icon getUserIcon(Component c) {
            Object d = panelToDetMap.get(c);
            if (d != null && d instanceof DraggableDetachable)
                return ((DraggableDetachable) d).getUserIcon();
            return null;
        }
//        public void addCloseTabButton(JButton closeTabButton) {
//          addImpl(closeTabButton, BasicDraggableTabbedPaneUI.CLOSE_TAB_BUTTON, -1);
//          ui.layoutContainer(this);
//        }
        public void setMaxButton(JToggleButton maxButton) {
            this.maxButton = maxButton;
        }
        public void setMinButton(JToggleButton minButton) {
            this.minButton = minButton;
        }
        public void paint(Graphics g) {
            super.paint(g);
            if (maxButton != null) {
                maxButton.paint(g);
            }
            if (minButton != null) {
                minButton.paint(g);
            }
//            if (closeTabButton != null) {
//              Rectangle cr = closeTabButton.getBounds();
//              Graphics cg = g.create(cr.x, cr.y, cr.width, cr.height);
//              closeTabButton.paint(cg);
//            }
        }
        void getDetachables(Collection c) {
            Detachable[] d = getDetachables();
            c.addAll(Arrays.asList(d));
        }
        Detachable getDetachableFor(Component comp) {
            return super.getDetachable(comp);
        }
    }
    
    protected TabbedPane createTabbedPane(int tabPlacement, int tabLayoutPolicy, boolean detachable) {
        return new TabbedPane(tabPlacement, tabLayoutPolicy, detachable);
    }
    
    protected TabbedPane createTabbedPane() {
      return createTabbedPane(tabPlacement, tabLayoutPolicy, detachable);
    }
    
    public class SplitPane extends JSplitPane implements ForciblyInvisible {
        public final static int LEFT_MIN = 1 << 0; 
        public final static int LEFT_MAX = 1 << 1; 
        public final static int RIGHT_MIN = 1 << 2; 
        public final static int RIGHT_MAX = 1 << 3; 
        public final static int MIN_MAX_NONE = 0;
        
        BasicDraggableSplitPaneUI ui;
        
        protected JToggleButton leftMinButton;
        protected JToggleButton leftMaxButton;
        protected JToggleButton rightMinButton;
        protected JToggleButton rightMaxButton;
        
        protected int minMaxState = MIN_MAX_NONE;
        
//      protected boolean minimizedLeft = false;
//      protected boolean minimizedRight = false;
//      protected boolean maximizedLeft = false;
//      protected boolean maximizedRight = false;
        
        protected int lastDividerMinMaxLocation;
        
        public int getLastDividerMinMaxLocation() {
            return lastDividerMinMaxLocation;
        }
        public void setLastDividerMinMaxLocation(int newLocation) {
            int oldLocation = lastDividerMinMaxLocation;
            lastDividerMinMaxLocation = newLocation;
        }
        public int getMinMaxState() {
            return minMaxState;
        }
        public void setMinMaxState(int state) {
            this.minMaxState = state & (LEFT_MIN | LEFT_MAX | RIGHT_MIN | RIGHT_MAX); 
        }
        public void setLeftMinimized(boolean b) {
            if (b)
                minMaxState |= LEFT_MIN;
            else
                minMaxState &= ~LEFT_MIN;
        }
        public void setLeftMaximized(boolean b) {
            if (b)
                minMaxState |= LEFT_MAX;
            else
                minMaxState &= ~LEFT_MAX;
        }
        public void setRightMinimized(boolean b) {
            if (b)
                minMaxState |= RIGHT_MIN;
            else
                minMaxState &= ~RIGHT_MIN;
        }
        public void setRightMaximized(boolean b) {
            if (b)
                minMaxState |= RIGHT_MAX;
            else
                minMaxState &= ~RIGHT_MAX;
        }
        public void clearMinMaxSelection(int which) {
            if ((which & LEFT_MIN) != 0) {
                if (leftMinButton != null)
                    leftMinButton.getModel().setSelected(false);
            }
            if ((which & LEFT_MAX) != 0) {
                if (leftMaxButton != null)
                    leftMaxButton.getModel().setSelected(false);
            }
            if ((which & RIGHT_MIN) != 0) {
                if (rightMinButton != null)
                    rightMinButton.getModel().setSelected(false);
            }
            if ((which & RIGHT_MAX) != 0) {
                if (rightMaxButton != null)
                    rightMaxButton.getModel().setSelected(false);
            }
            //setMinMaxState(~which);
            this.minMaxState = this.minMaxState & ~which;
        }
        public void setLeftMaxButton(JToggleButton leftMaxButton) {
            this.leftMaxButton = leftMaxButton;
            Component c = getLeftComponent();
            if (c instanceof TabbedPane)
                ((TabbedPane) c).setMaxButton(leftMaxButton);
        }
        public void setLeftMinButton(JToggleButton leftMinButton) {
            this.leftMinButton = leftMinButton;
            Component c = getLeftComponent();
            if (c instanceof TabbedPane)
                ((TabbedPane) c).setMinButton(leftMinButton);
        }
        public void setRightMaxButton(JToggleButton rightMaxButton) {
            this.rightMaxButton = rightMaxButton;
            Component c = getRightComponent();
            if (c instanceof TabbedPane)
                ((TabbedPane) c).setMaxButton(rightMaxButton);
        }
        public void setRightMinButton(JToggleButton rightMinButton) {
            this.rightMinButton = rightMinButton;
            Component c = getRightComponent();
            if (c instanceof TabbedPane)
                ((TabbedPane) c).setMinButton(rightMinButton);
        }
        
        protected void deferFocusFromChild(Component child) {
            if (child != null) {
                FocusManager fm = FocusManager.getCurrentManager();
                Component focused = fm.getFocusOwner();
                if (focused == child || (focused != null && (child instanceof Container) && ((Container) child).isAncestorOf(focused))) {
//                    fm.focusPreviousComponent();
//                    focused.transferFocusBackward();
                    Component prev = focusWatcher.getPreviousOwner();
                    if (prev != null) {
                        SplitPane sp = null;
                        Container p = prev.getParent();
                        while (p != null && sp == null) {
                            if (p instanceof SplitPane)
                                sp = (SplitPane) p;
                            else
                                p = p.getParent();
                        }
                        if (sp == null)
                            prev.requestFocusInWindow();
                        else {
                            int leftRight = 0;
                            Container cc = (Container) sp.getLeftComponent();
                            if (cc.isAncestorOf(prev)) {
                                leftRight = LEFT_MIN;
                            }
                            else if ((cc = (Container) sp.getRightComponent()).isAncestorOf(prev)) {
                                leftRight = RIGHT_MIN;
                            }
                            if ((sp.getMinMaxState() & leftRight) != 0) {
                                fm.focusPreviousComponent();
                            }
                            else {
                                prev.requestFocusInWindow();
                            }
                        }
                    }
                }
            }
        }
        public void minimizeLeft() {
            ui.minimizeLeft();
            Component child = getLeftComponent();
            deferFocusFromChild(child);
        }
        public void maximizeLeft() {
            ui.maximizeLeft();
        }
        public void restoreLeft() {
            ui.restoreLeft();
        }
        public void minimizeRight() {
            ui.minimizeRight();
            Component child = getRightComponent();
            deferFocusFromChild(child);
        }
        public void maximizeRight() {
            ui.maximizeRight();
        }
        public void restoreRight() {
            ui.restoreRight();
        }
        
        // prevent tab areas of draggable tab panes from being hidden when using
        // one-touch expansion
//        public void setDividerLocation0(int location) {
//            // TODO better to implement all this in terms of explicit minimize
//            // and restore buttons living in the tab areas
//            int currentLocation = ui.getDividerLocation(this);
//            int lastLocation = getLastDividerLocation();
//            Insets insets = getInsets();
//            int orientation = getOrientation();
//            if (false && orientation == JSplitPane.VERTICAL_SPLIT) {
//                // TODO deal with tabPlacement == BOTTOM
//                int rightMinimumSize = 0;
//                Component c = getRightComponent();
//                // TODO fix de-expansion - test in OneTouchActionHandler is not
//                // detecting what we want
//                if (c instanceof TabbedPane) {
//                    TabbedPane tabbedPane = (TabbedPane) c;
//                    if (tabbedPane.getTabCount() > 0)
//                        rightMinimumSize = tabbedPane.ui.lastTabAreaHeight;
//                }
//                else if (c instanceof SplitPane) {
//                    SplitPane splitPane = (SplitPane) c;
//                    rightMinimumSize = splitPane.getMinimizedHeight();
//                }
//                int rightMinimumLocation = getHeight() - insets.bottom - rightMinimumSize;
//                if (location >= rightMinimumLocation)
//                    location =  rightMinimumLocation;
//                else if (currentLocation >= rightMinimumLocation)
//                    location = lastLocation;
//                else {
//                    int leftMinimumSize = 0;
//                    c = getLeftComponent();
//                    if (c instanceof TabbedPane) {
//                        TabbedPane tabbedPane = (TabbedPane) c;
//                        if (tabbedPane.getTabCount() > 0)
//                            leftMinimumSize = tabbedPane.ui.lastTabAreaHeight;
//                    }
//                    else if (c instanceof SplitPane) {
//                        SplitPane splitPane = (SplitPane) c;
//                        leftMinimumSize = splitPane.getMinimizedHeight();
//                    }
//                    int leftMinimumLocation = insets.top + leftMinimumSize;
//                    if (location <= leftMinimumLocation)
//                        location =  leftMinimumLocation;
//                    else if (currentLocation <= leftMinimumLocation)
//                        location = lastLocation;
//                }
//                
////              if (location <= insets.top) {
////              Component c = getLeftComponent();
////              if (c instanceof TabbedPane) {
////              TabbedPane tabbedPane = (TabbedPane) c;
////              if (tabbedPane.getTabCount() > 0)
////              location = insets.top + tabbedPane.ui.lastTabAreaHeight;
////              }
////              else if (c instanceof SplitPane) {
////              SplitPane splitPane = (SplitPane) c;
////              location = insets.top + splitPane.getMinimizedHeight();
////              }
////              }
////              else if (location >= insets.top + ui.getDivider().getHeight() -
////              insets.bottom) {
////              Component c = getRightComponent();
////              if (c instanceof TabbedPane) {
////              TabbedPane tabbedPane = (TabbedPane) c;
////              if (tabbedPane.getTabCount() > 0)
////              location = insets.top + ui.getDivider().getHeight()
////              - insets.bottom
////              - tabbedPane.ui.lastTabAreaHeight;
////              }
////              else if (c instanceof SplitPane) {
////              SplitPane splitPane = (SplitPane) c;
////              location = insets.top + ui.getDivider().getHeight() - insets.bottom
////              - splitPane.getMinimizedHeight();
////              }
////              }
//            }
//            super.setDividerLocation(location);
//        }
        // figure out my smallest height that won't hide my draggable tab pane's tab areas
//        int getMinimizedHeight0() {
//            int h = 0;
//            Component left = getLeftComponent();
//            TabbedPane leftTabbedPane = null;
//            int leftPaneHeight = 0;
//            Component right = getRightComponent();
//            TabbedPane rightTabbedPane = null;
//            int rightPaneHeight = 0;
//            if (left instanceof TabbedPane) {
//                leftTabbedPane = (TabbedPane) left;
//                if (leftTabbedPane.getTabCount() > 0)
//                    leftPaneHeight = leftTabbedPane.ui.lastTabAreaHeight;
//            }
//            else if (left instanceof SplitPane) {
//                SplitPane leftSplitPane = (SplitPane) left;
//                leftPaneHeight = leftSplitPane.getMinimizedHeight();
//            }
//            if (right instanceof TabbedPane) {
//                rightTabbedPane = (TabbedPane) right;
//                if (rightTabbedPane.getTabCount() > 0)
//                    rightPaneHeight = rightTabbedPane.ui.lastTabAreaHeight;
//            }
//            else if (right instanceof SplitPane) {
//                SplitPane rightSplitPane = (SplitPane) right;
//                rightPaneHeight = rightSplitPane.getMinimizedHeight();
//            }
//            if (getOrientation() == HORIZONTAL_SPLIT) {
//                h = Math.max(leftPaneHeight, rightPaneHeight);
//            }
//            else { // getOrientation() == VERTICAL_SPLIT
//                h = leftPaneHeight + rightPaneHeight;
//            }
//            return h;
//        }
        int getMinimizedHeight() {
            int h = 0;
            int leftPaneHeight = getMinimizedHeight(true);
            int rightPaneHeight = getMinimizedHeight(false);
            if (getOrientation() == HORIZONTAL_SPLIT) {
                h = Math.max(leftPaneHeight, rightPaneHeight);
            }
            else { // getOrientation() == VERTICAL_SPLIT
                Insets dividerInsets = ui.getDivider().getInsets();
                h = leftPaneHeight + rightPaneHeight + getDividerSize() + dividerInsets.top + dividerInsets.bottom;
            }
            return h;
        }
        int getMinimizedWidth() {
            int w = 0;
            int leftPaneWidth = getMinimizedWidth(true);
            int rightPaneWidth = getMinimizedWidth(false);
            if (getOrientation() == VERTICAL_SPLIT) {
                w = Math.max(leftPaneWidth, rightPaneWidth);
            }
            else { // getOrientation() == HORIZONTAL_SPLIT
                Insets dividerInsets = ui.getDivider().getInsets();
                w = leftPaneWidth + rightPaneWidth + getDividerSize() + dividerInsets.left + dividerInsets.right;
            }
            return w;
        }
        int getMinimizedHeight(boolean checkLeft) {
            Component comp = checkLeft
                ? getLeftComponent()
                : getRightComponent();
            TabbedPane tabbedPane = null;
            int paneHeight = 0;
            if (comp instanceof TabbedPane) {
                tabbedPane = (TabbedPane) comp;
                Insets tabInsets = tabbedPane.getInsets();
                if (tabbedPane.getTabCount() > 0)
                    paneHeight = tabbedPane.ui.getMinTabAreaHeight() + tabInsets.top + tabInsets.bottom;
            }
            else if (comp instanceof SplitPane) {
                SplitPane splitPane = (SplitPane) comp;
                paneHeight = splitPane.getMinimizedHeight();
            }
            return paneHeight;
        }
        int getMinimizedWidth(boolean checkLeft) {
            Component comp = checkLeft
            ? getLeftComponent()
                : getRightComponent();
            TabbedPane tabbedPane = null;
            int paneWidth = 0;
            if (comp instanceof TabbedPane) {
                tabbedPane = (TabbedPane) comp;
                Insets tabInsets = tabbedPane.getInsets();
                if (tabbedPane.getTabCount() > 0)
                    paneWidth = tabbedPane.ui.getMinTabAreaWidth() + tabInsets.left + tabInsets.right;
            }
            else if (comp instanceof SplitPane) {
                SplitPane splitPane = (SplitPane) comp;
                paneWidth = splitPane.getMinimizedWidth();
            }
            return paneWidth;
        }
        /*
         public void setLastDividerLocation(int newLastLocation) {
         super.setLastDividerLocation(newLastLocation);
         }
         */
        protected SplitPane(int newOrientation, boolean newContinuousLayout,
            Component newLeftComponent, Component newRightComponent,
            final double weight) {
            super(newOrientation, newContinuousLayout, newLeftComponent,
                newRightComponent);
            // TODO if it's the root split pane, give it the default split pane
            // border, otherwise make it empty
            setBorder(BorderFactory.createEmptyBorder());
            if (weight > 0) {
                setResizeWeight(weight);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setResizeWeight(weight);
                        setDividerLocation(weight);
                    }
                });
            }
        }
        public void updateUI() {
            setUI(ui = new BasicDraggableSplitPaneUI(DraggableTabbedPane.this));
        }
        public Dimension getMinimumSize() {
            return new Dimension(getMinimizedWidth(), getMinimizedHeight());
        }

        public boolean isForcedInvisible() {
          if (forcedInvisible)
            return true;
          return forcedInvisible;
        }

        public boolean isTreeForcedInvisible() {
//          Component[] components = getComponents();
//        for (int i = 0; i < components.length; i++) {
//        Component c = components[i];
//        if (c instanceof ForciblyInvisible) {
//        if (! ((ForciblyInvisible)c).isTreeForcedInvisible()) {
//        return false;
//        }
//        }
//        }
          if (leftComponent != null) {
            if (leftComponent instanceof ForciblyInvisible) {
              if (!((ForciblyInvisible)leftComponent).isTreeForcedInvisible()) {
                return false;
              }
            }
            else if (leftComponent.isVisible()) {
              return false;
            }
          }
          if (rightComponent != null) {
            if (rightComponent instanceof ForciblyInvisible) {
              if (!((ForciblyInvisible)rightComponent).isTreeForcedInvisible()) {
                return false;
              }
            }
            else if (rightComponent.isVisible()) {
              return false;
            }
          }
          return true;
        }
        
        protected boolean forcedInvisible = false;
        public void addNotify() {
          super.addNotify();
          Container parent = getParent();
          if (parent instanceof SplitPane) {
            checkForcedVisibility();
          }
        }

        protected void addImpl(Component comp, Object constraints, int index) {
          super.addImpl(comp, constraints, index);
          checkForcedVisibility();
        }

//        protected void checkForcedVisibility() {
//          Container parent = getParent();
//          if (parent != null && isTreeForcedInvisible()) {
//            forcedInvisible = true;
//            setVisible(false);
//            invalidate();
//            parent.validate();
//          }
//        }

//        protected void checkForcedVisibility() {
//          Container parent = getParent();
//          if (parent != null) {
//            if (! forcedInvisible) {
//              if (isTreeForcedInvisible()) {
//                forcedInvisible = true;
//                setVisible(false);
//                invalidate();
//                parent.validate();
//              }
//            }
//            else {
//              if (! isTreeForcedInvisible()) {
//                forcedInvisible = false;
//                setVisible(true);
//                invalidate();
//                parent.validate();
//              }
//            }
//          }
//        }

//        protected void checkForcedVisibility() {
//          Container parent = getParent();
//          if (parent != null) {
//            boolean treeForcedInvisible = isTreeForcedInvisible();
//            if (treeForcedInvisible) {
//              forcedInvisible = true;
//              if (isVisible()) {
//                setVisible(false);
//                revalidate();
////                parent.validate();
//              }
//            }
//            else if (forcedInvisible || ! isVisible()) {
//              forcedInvisible = false;
//              if (! isVisible()) {
//                setVisible(true);
//                revalidate();
////                parent.validate();
//              }
//            }
//          }
//        }
        
        boolean checkingForcedVisibility = false;
        protected void checkForcedVisibility() {
          if (checkingForcedVisibility)
            return;
          checkingForcedVisibility = true;
          try {
            Container parent = getParent();
            if (parent != null) {
              boolean treeForcedInvisible = isTreeForcedInvisible();
              if (treeForcedInvisible) {
                forcedInvisible = true;
                if (isVisible()) {
                  setVisible(false);
                  revalidate();
                }
              }
              else if (forcedInvisible || ! isVisible()) {
                forcedInvisible = false;
                if (! isVisible()) {
                  setVisible(true);
                  revalidate();
                }
              }
            }
          } finally {
            checkingForcedVisibility = false;
          }
        }

        public void remove(Component component) {
          super.remove(component);
          checkForcedVisibility();
        }

        public void remove(int index) {
          super.remove(index);
          checkForcedVisibility();
        }

        public void invalidate() {
          if (isValid()) {
            checkForcedVisibility();
          }
          super.invalidate();
        }
    }
    
    protected WeakHashMap splitPanes = new WeakHashMap();
    protected WeakHashMap tabPanes = new WeakHashMap();
    
    SplitPane rootSplitPane = null;
    TabbedPane rootTabPane;
    
    public static final int DEFAULT_TAB_LAYOUT_POLICY = JTabbedPane.WRAP_TAB_LAYOUT;
    
    /**
     * 
     */
    public DraggableTabbedPane() {
        this(JTabbedPane.TOP, true);
    }
    
    /**
     * @param tabPlacement
     * @param detachable
     */
    public DraggableTabbedPane(int tabPlacement, boolean detachable) {
        this(tabPlacement, DEFAULT_TAB_LAYOUT_POLICY, detachable);
    }
    
    /**
     * @param tabPlacement
     * @param tabLayoutPolicy
     * @param detachable
     */
    public DraggableTabbedPane(int tabPlacement, int tabLayoutPolicy,
                               boolean detachable) {
        this(tabPlacement, DEFAULT_TAB_LAYOUT_POLICY, detachable, true);
    }
    
    public DraggableTabbedPane(int tabPlacement, int tabLayoutPolicy,
                               boolean detachable, boolean continuousLayout) {
        this.tabPlacement = tabPlacement;
        this.detachable = detachable;
        this.tabLayoutPolicy = tabLayoutPolicy;
        this.continuousLayout = continuousLayout;
        initialize();
    }
    
    protected FocusWatcher focusWatcher;
    private static final Comparator actionComparator = new Comparator() {
      public int compare(Object o1, Object o2) {
        if (o1 instanceof AbstractButton)
          o1 = ((AbstractButton) o1).getAction();
        if (o2 instanceof AbstractButton)
          o2 = ((AbstractButton) o2).getAction();
        Action a1 = (Action) o1;
        Action a2 = (Action) o2;
        return String.CASE_INSENSITIVE_ORDER.compare((String)a1.getValue(Action.NAME), (String)a2.getValue(Action.NAME));
      }
    };
    private LinkedHashMap componentActions = new LinkedHashMap();
    /** Set of JawbComponents added to this pane */
    private LinkedHashMap componentConstraints = new LinkedHashMap();
    private WeakHashMap components = new WeakHashMap();
    private JMenu componentsMenu;
    public static final double ONE_HALF = 0.5D;
    
    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        FocusManager.getCurrentManager().addPropertyChangeListener("focusOwner", focusWatcher = new FocusWatcher());
        // this.setSize(300, 150);
        // this.setUI(new BasicDraggableTabbedPaneUI());
        rootTabPane = createTabbedPane();
        rootTabPane.setPreferredSize(new Dimension(0, 0));
        tabPanes.put(rootTabPane, null);
        setLayout(new BorderLayout());
        super.add(rootTabPane, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder());
    }
    
    void splitRootPane(TabbedPane sourcePane, int tabIndex, int orientation, String anchor, double weight) {
        int tabCount = sourcePane.getTabCount();
        if ((rootTabPane != null && sourcePane == rootTabPane && sourcePane.getTabCount() == 1) || tabIndex >= tabCount || tabIndex < 0) {
            return;
        }
        Component dropped = sourcePane.getComponentAt(tabIndex);
        String title = sourcePane.getTitleAt(tabIndex);
        Icon icon = sourcePane.getUserIconAt(tabIndex);
        String tip = sourcePane.getToolTipTextAt(tabIndex);
        title = insertMnemonicCharInTitle(sourcePane, tabIndex, title);
        sourcePane.remove(dropped);
        splitRootPane(dropped, title, icon, tip, orientation, anchor, weight);
//        TabbedPane targetTabPane = getTabbedPaneAncestor(dropped);
//        int insertedIndex = targetTabPane.indexOfComponent(dropped);
//        if (insertedIndex != -1) {
//          targetTabPane.setMnemonicAt(insertedIndex, mnemonic);
//        }        
    }

    private String insertMnemonicCharInTitle(TabbedPane sourcePane, int tabIndex, String title) {
      int mnemonic = sourcePane.getMnemonicAt(tabIndex);
      return insertMnemonicCharInTitle(title, (char) mnemonic);
    }

    private String insertMnemonicCharInTitle(String title, char mnemonic) {
      if (mnemonic != '\0') {
        int mnemonicIndex = title.indexOf(mnemonic);
        if (mnemonicIndex != -1) {
          title = title.substring(0, mnemonicIndex) + '&' + title.substring(mnemonicIndex);
        }
      }
      return title;
    }
    
    void splitRootPane(Component dropped, String title, Icon icon, String tip, int orientation, String anchor, double weight) {
        TabbedPane newTabbedPane = createTabbedPane();
        tabPanes.put(newTabbedPane, null);
        splitRootPane(newTabbedPane, orientation, anchor, weight);
        newTabbedPane.insertTab(title, icon, dropped, tip, 0);
    }
    
    void splitRootPane(TabbedPane droppedPane, int orientation, String anchor, double weight) {
        if (rootSplitPane == null) {
            splitTabbedPane(rootTabPane, droppedPane, orientation, anchor, weight);
        }
        else {
            Component left, right;
            if (LEFT.equals(anchor) || BOTTOM.equals(anchor)) {
                left = rootSplitPane;
                right = droppedPane; 
            }
            else {
                left = droppedPane;
                right = rootSplitPane;
            }
            super.remove(rootSplitPane);
            SplitPane newSplitPane = new SplitPane(orientation, continuousLayout, left, right, weight);
            splitPanes.put(newSplitPane, null);
            rootSplitPane = newSplitPane;
            super.addImpl(rootSplitPane, BorderLayout.CENTER, -1);
            revalidate();
        }
    }
    
    public SplitPane splitTabbedPane(TabbedPane targetPane, TabbedPane sourcePane, int tabIndex, int orientation, String anchor, double weight) {
        int tabCount = sourcePane.getTabCount();
        if ((sourcePane == targetPane && sourcePane.getTabCount() == 1) || tabIndex >= tabCount || tabIndex < 0) {
            return null;
        }
        Component dropped = sourcePane.getComponentAt(tabIndex);
        String title = sourcePane.getTitleAt(tabIndex);
        Icon icon = sourcePane.getUserIconAt(tabIndex);
        String tip = sourcePane.getToolTipTextAt(tabIndex);
//        int mnemonic = sourcePane.getMnemonicAt(tabIndex);
        title = insertMnemonicCharInTitle(sourcePane, tabIndex, title);
        sourcePane.remove(dropped);
        SplitPane splitTabbedPane = splitTabbedPane(targetPane, dropped, title, icon, tip, orientation, anchor, weight);
//        TabbedPane tabPane = getTabbedPaneAncestor(dropped);
//        if (tabPane != null) {
//          int droppedIndex = tabPane.indexOfComponent(dropped);
//          if (droppedIndex != -1) {
//            tabPane.setMnemonicAt(droppedIndex, mnemonic);
//          }
//        }
        return splitTabbedPane;
    }
    
    protected SplitPane splitTabbedPane(TabbedPane targetPane, Component dropped, String title, Icon icon, String tip, int orientation, String anchor, double weight) {
        TabbedPane newTabbedPane = createTabbedPane();
        tabPanes.put(newTabbedPane, null);
        SplitPane newSplitPane = splitTabbedPane(targetPane, newTabbedPane, orientation, anchor, weight);
        newTabbedPane.insertTab(title, icon, dropped, tip, 0);
        return newSplitPane;
    }
    
    protected SplitPane splitTabbedPane(TabbedPane targetPane, TabbedPane droppedPane, int orientation, String anchor, double weight) {
        SplitPane newSplitPane = null;
        if (rootSplitPane == null || targetPane.getParent() == this) {
            /*
             if (rootTabPane != targetPane) {
             throw new IllegalStateException("Can only split my own top-level tab pane");
             }
             if (targetPane.getParent() != this) {
             throw new IllegalStateException("Cannot split unowned top-level tab pane");
             }
             */
            super.remove(rootTabPane);
            TabbedPane leftTabPane = targetPane, rightTabPane = droppedPane;
            if (LEFT.equals(anchor) || TOP.equals(anchor)) {
                leftTabPane = droppedPane;
                rightTabPane = targetPane;
            }
            rootTabPane = null;
            newSplitPane = rootSplitPane = new SplitPane(orientation, continuousLayout, leftTabPane, rightTabPane, weight);
            if (weight > 0) {
                newSplitPane.setResizeWeight(weight);
            }
            splitPanes.put(rootSplitPane, null);
            super.add(rootSplitPane, BorderLayout.CENTER);
        }
        else {
            Component left, right;
            Container parent = targetPane.getParent();
            if (parent instanceof SplitPane) {
                SplitPane parentSplitPane = (SplitPane) parent;
                boolean leftChild = (parentSplitPane.getLeftComponent() == targetPane);
                // parentSplitPane.remove(targetPane);
                /*
                 if (LEFT.equals(anchor) || BOTTOM.equals(anchor)) {
                 left = targetPane;
                 right = droppedPane; 
                 }
                 else {
                 left = droppedPane;
                 right = targetPane;
                 }
                 newSplitPane = new SplitPane(orientation, continuousLayout, left, right);
                 splitPanes.add(newSplitPane);
                 if (leftChild)
                 parentSplitPane.setLeftComponent(newSplitPane);
                 else
                 parentSplitPane.setRightComponent(newSplitPane);
                 */
                newSplitPane = new SplitPane(orientation, continuousLayout, null, null, weight);
                if (LEFT.equals(anchor)) {
                    newSplitPane.setLeftComponent(droppedPane);
                    newSplitPane.setRightComponent(targetPane);
                }
                else if (TOP.equals(anchor)) {
                    newSplitPane.setTopComponent(droppedPane);
                    newSplitPane.setBottomComponent(targetPane);
                }
                else if (RIGHT.equals(anchor)) {
                    newSplitPane.setLeftComponent(targetPane);
                    newSplitPane.setRightComponent(droppedPane);
                }
                else if (BOTTOM.equals(anchor)) {
                    newSplitPane.setTopComponent(targetPane);
                    newSplitPane.setBottomComponent(droppedPane);
                }
                splitPanes.put(newSplitPane, null);
                if (leftChild)
                    parentSplitPane.setLeftComponent(newSplitPane);
                else
                    parentSplitPane.setRightComponent(newSplitPane);
            }
            else if (parent == this) {
                
            }
        }
        revalidate();
        return newSplitPane;
    }
    
    class TabPaneAncestry {
        private ContainerRef parent;
        private TabbedPaneRef tabParent;
        private SplitPaneRef splitParent;
        private ComponentRef sibling;
        
        boolean leftChild = false;
        TabPaneAncestry grandParent = null;
        int orientation = JSplitPane.HORIZONTAL_SPLIT;
        
        Container getParent() {
            return parent.getContainer();
        }
        Component getSibling() {
            return sibling.getComponent();
        }
        SplitPane getSplitParent() {
            return splitParent.getSplitPane();
        }
        TabbedPane getTabParent() {
            return tabParent.getTabbedPane();
        }
        
        private class ContainerRef extends WeakReference {
            ContainerRef(Container referent) {
                super(referent);
            }
            Container getContainer() {
                return (Container) super.get();
            }
        }
        private class ComponentRef extends WeakReference {
            ComponentRef(Component referent) {
                super(referent);
            }
            Component getComponent() {
                return (Component) super.get();
            }
        }
        private class TabbedPaneRef extends WeakReference {
            TabbedPaneRef(TabbedPane referent) {
                super(referent);
            }
            TabbedPane getTabbedPane() {
                return (TabbedPane) super.get();
            }
        }
        private class SplitPaneRef extends WeakReference {
            SplitPaneRef(SplitPane referent) {
                super(referent);
            }
            SplitPane getSplitPane() {
                return (SplitPane) super.get();
            }
        }
        
        TabPaneAncestry(Container parent, Component child) {
            this.parent = new ContainerRef(parent);
            if (parent instanceof TabbedPane)
                tabParent = new TabbedPaneRef((TabbedPane) parent);
            else if (parent instanceof SplitPane) {
                SplitPane splitParent = (SplitPane) parent;
                this.splitParent = new SplitPaneRef(splitParent);
                Component sibling = splitParent.getRightComponent();
                if (sibling == child)
                    sibling = splitParent.getRightComponent();
                else
                    leftChild = true;
                this.sibling = new ComponentRef(sibling);
                orientation = splitParent.getOrientation();
            }
            Container grandParent = parent.getParent();
            if (parent != DraggableTabbedPane.this && grandParent != null)
                this.grandParent = new TabPaneAncestry(grandParent, parent);
        }
    }
    public final static boolean COLLAPSE_EMPTY_TAB_PANES = true;
    public static final double TWO_THIRDS = 2.0d/3.0d;
    public static final double ONE_THIRD = 1.0d/3.0d;
    public static final double ONE_QUARTER = 0.25d;
    
    void removeTabComponent(TabbedPane tabbedPane, Component removed) {
//    new RuntimeException(tabbedPane.getParent() + "\n" + removed).printStackTrace(System.out);
      // TODO handle possibility that grand parent might be gone when
      // re-attaching later
//    if (true) {
//    collapseSplitPane(rootSplitPane);
//    return;
//    }
      if (tabbedPane.getTabCount() == 0) {
        if (COLLAPSE_EMPTY_TAB_PANES) {
          if (! tabbedPane.showEmptyTab()) {
            Container parent = tabbedPane.getParent();
//          tabbedPane.ancestry = new TabPaneAncestry(parent, tabbedPane);
            Container grandParent = null;
            SplitPane splitParent = null;
            SplitPane splitGrandParent = null;
            boolean leftChild = false;
            boolean leftParent = false;
            if (parent instanceof SplitPane) {
              splitParent = (SplitPane) parent;
              splitPanes.remove(splitParent);
              Component sibling = splitParent.getRightComponent();
              if (sibling == tabbedPane)
                sibling = splitParent.getLeftComponent();
              else
                leftChild = true;
              grandParent = splitParent.getParent();
              if (sibling != null && collapseSplitPane(sibling)) {
                parent.remove(sibling);
                sibling = null;
              }
              if (grandParent instanceof SplitPane) {
                splitGrandParent = (SplitPane) grandParent;
                leftParent = (splitParent == splitGrandParent.getLeftComponent());
                splitGrandParent.remove(splitParent);
                if (sibling != null) {
                  splitParent.remove(sibling);
                  if (leftParent)
                    splitGrandParent.setLeftComponent(sibling);
                  else
                    splitGrandParent.setRightComponent(sibling);
                }
              }
              else if (grandParent == this) {
                if (true || getComponentCount() == 1) {
                  super.remove(splitParent);
                  splitParent.removeAll();
                  if (sibling != null) {
                    super.addImpl(sibling, BorderLayout.CENTER, -1);
                    if (sibling instanceof DraggableTabbedPane.TabbedPane) {
                      rootTabPane = (DraggableTabbedPane.TabbedPane) sibling;
                      rootSplitPane = null;
                    }
                    else if (sibling instanceof DraggableTabbedPane.SplitPane) {
                      rootSplitPane = (DraggableTabbedPane.SplitPane) sibling;
                    }
                  }
                  else {
                    rootTabPane = createTabbedPane();
                    rootSplitPane = null;
                  }
                }
              }
//            if (sibling instanceof TabbedPane) {
//TabbedPane tabbedSibling = (TabbedPane) sibling;
//if (tabbedSibling.getTabCount() == 0) {
//removeTabComponent(tabbedSibling, null);
//}
//}
            }
            else if (parent == this) {
              // what to do here? nothing, for now...
            }
            else {
              // weird state
              throw new IllegalStateException("parent of tab pane not split pane or root pane");
            }
            if (! tabbedPane.emptied) {
              // detachables remain, and may come back!
              tabbedPane.splitGrandParent = splitGrandParent;
              tabbedPane.splitParent = splitParent;
              tabbedPane.savedParent = parent;
              tabbedPane.savedGrandParent = grandParent;
              tabbedPane.leftChild = leftChild;
              tabbedPane.leftParent = leftParent;
              tabbedPane.savedOrientation = splitParent != null ? splitParent.getOrientation() : HORIZONTAL_SPLIT;
            }
            revalidate();
          }
        }
      }
    }
//    void removeTabComponent(TabbedPane tabbedPane, Component removed) {
//      new RuntimeException(tabbedPane.getParent() + "\n" + removed).printStackTrace(System.out);
//      // TODO handle possibility that grand parent might be gone when
//      // re-attaching later
//      if (tabbedPane.getTabCount() == 0) {
//        if (COLLAPSE_EMPTY_TAB_PANES) {
//          if (! tabbedPane.showEmptyTab()) {
//            Container parent = tabbedPane.getParent();
//            tabbedPane.ancestry = new TabPaneAncestry(parent, tabbedPane);
//            Container grandParent = null;
//            SplitPane splitParent = null;
//            SplitPane splitGrandParent = null;
//            boolean leftChild = false;
//            boolean leftParent = false;
//            if (parent instanceof SplitPane) {
//              splitParent = (SplitPane) parent;
//              splitPanes.remove(splitParent);
//              Component sibling = splitParent.getRightComponent();
//              if (sibling == tabbedPane)
//                sibling = splitParent.getLeftComponent();
//              else
//                leftChild = true;
//              grandParent = splitParent.getParent();
//              if (grandParent instanceof SplitPane) {
//                splitGrandParent = (SplitPane) grandParent;
//                leftParent = (splitParent == splitGrandParent.getLeftComponent());
//                splitParent.remove(sibling);
//                splitGrandParent.remove(splitParent);
//                if (leftParent)
//                  splitGrandParent.setLeftComponent(sibling);
//                else
//                  splitGrandParent.setRightComponent(sibling);
//              }
//              else if (grandParent == this) {
//                if (true || getComponentCount() == 1) {
//                  super.remove(splitParent);
//                  splitParent.removeAll();
//                  super.addImpl(sibling, BorderLayout.CENTER, -1);
//                  if (sibling instanceof DraggableTabbedPane.TabbedPane) {
//                    rootTabPane = (DraggableTabbedPane.TabbedPane) sibling;
//                    rootSplitPane = null;
//                  }
//                  else if (sibling instanceof DraggableTabbedPane.SplitPane) {
//                    rootSplitPane = (DraggableTabbedPane.SplitPane) sibling;
//                  }
//                }
//              }
//            }
//            else if (parent == this) {
//              // what to do here? nothing, for now...
//            }
//            else {
//              // weird state
//              throw new IllegalStateException("parent of tab pane not split pane or root pane");
//            }
//            if (! tabbedPane.emptied) {
//              // detachables remain, and may come back!
//              tabbedPane.splitGrandParent = splitGrandParent;
//              tabbedPane.splitParent = splitParent;
//              tabbedPane.savedParent = parent;
//              tabbedPane.savedGrandParent = grandParent;
//              tabbedPane.leftChild = leftChild;
//              tabbedPane.leftParent = leftParent;
//              tabbedPane.savedOrientation = splitParent != null ? splitParent.getOrientation() : HORIZONTAL_SPLIT;
//            }
//            revalidate();
//          }
//        }
//      }
//    }
    
    private boolean collapseSplitPane(Component pane) {
      if (pane != null) {
        if (pane instanceof TabbedPane) {
          TabbedPane tp = (TabbedPane) pane;
          return tp.getTabCount() == 0;
        }
        if (pane instanceof SplitPane) {
          SplitPane sp = (SplitPane) pane;
          Component left = sp.getLeftComponent();
          if (collapseSplitPane(left)) {
            sp.setLeftComponent(left = null);
          }
          Component right = sp.getRightComponent();
          if (collapseSplitPane(right)) {
            sp.setRightComponent(right = null);
          }
          if (left == null && right == null) {
            splitPanes.remove(pane);
            return true;
          }
        }
        return false;
      }
      return true;
    }

    Container getPaneAtLocation(int x, int y) {
        /*
         if (rootSplitPane == null) {
         Point loc = rootSplitPane.getLocation();
         if (leftRootTabPane.contains(x - loc.x, y - loc.y))
         return rootSplitPane;
         }
         
         return null;
         */
        Component c = findComponentAt(x, y);
        while (c != null && c != this
                                        && ! (c instanceof SplitPane)
                                        && ! (c instanceof TabbedPane))
            c = c.getParent();
        // if (c == this)
        //    throw new IllegalStateException("findComponentAt returned unhappy descendant");
        if (c == null)
            c = this;
        return (Container) c;
    }
    
    /**
     * Adds a <code>component</code> with a tab title defaulting to
     * the name of the component which is the result of calling
     * <code>component.getName</code>.
     * Cover method for <code>insertTab</code>.
     * 
     * @param component the component to be displayed when this tab is clicked
     * @return the component
     * 
     */
    public Component add(Component component) {
        if (!(component instanceof UIResource)) {
            addTab(component.getName(), component);
        } else {
            super.add(component);
        }
        return component;
    }
    
    /**
     * Adds a <code>component</code> at the specified tab index with a tab
     * title defaulting to the name of the component.
     * Cover method for <code>insertTab</code>.
     * 
     * @param component the component to be displayed when this tab is clicked
     * @param index the position to insert this new tab
     * @return the component
     * 
     */
    public Component add(Component component, int index) {
        return add(component, index);
    }
    
    /**
     * Adds a <code>component</code> to the tabbed pane.
     * If <code>constraints</code> is a <code>String</code> or an
     * <code>Icon</code>, it will be used for the tab title,
     * otherwise the component's name will be used as the tab title. 
     * Cover method for <code>insertTab</code>.
     * 
     * @param component the component to be displayed when this tab is clicked
     * @param constraints the object to be displayed in the tab
     * 
     */
    public void add(Component component, Object constraints) {
        add(component, constraints, -1);
    }
    
    /**
     * Adds a <code>component</code> at the specified tab index.
     * If <code>constraints</code> is a <code>String</code> or an
     * <code>Icon</code>, it will be used for the tab title,
     * otherwise the component's name will be used as the tab title. 
     * Cover method for <code>insertTab</code>.
     * 
     * @param component the component to be displayed when this tab is clicked
     * @param constraints the object to be displayed in the tab
     * @param index the position to insert this new tab
     * 
     */
    public void add(Component component, Object constraints, int index) {
        if (rootTabPane != null) {
            rootTabPane.add(component, constraints, index);
        }
        else {
            // TODO what to do?
        }
    }
    
    /**
     * Adds a <code>component</code> with the specified tab title.
     * Cover method for <code>insertTab</code>.
     * 
     * @param title the title to be displayed in this tab
     * @param component the component to be displayed when this tab is clicked
     * @return the component
     * 
     */
    public Component add(String title, Component component) {
        addTab(title, component);
        return component;
    }
    
    /**
     * Adds a <code>component</code> represented by a <code>title</code>
     * and no icon. 
     * Cover method for <code>insertTab</code>.
     * 
     * @param title the title to be displayed in this tab
     * @param component the component to be displayed when this tab is clicked
     * 
     */
    public void addTab(String title, Component component) {
        addTab(title, null, component, null, null, -1, -1, false);
    }
    public void addTab(String title, Component component, boolean guessPath) {
      addTab(title, null, component, null, null, -1, -1, guessPath);
    }
    
    public void addTab(String title, Component component, double weight) {
        addTab(title, null, component, null, null, weight, -1, false);
    }
    public void addTab(String title, Component component, double weight, boolean guessPath) {
      addTab(title, null, component, null, null, weight, -1, guessPath);
    }
    
    /**
     * Adds a <code>component</code> represented by a <code>title</code>
     * and/or <code>icon</code>, either of which can be <code>null</code>.
     * If <code>icon</code> is non- <code>null</code> and it implements
     * <code>ImageIcon</code> a corresponding disabled icon will automatically
     * be created and set on the tabbedpane. 
     * Cover method for <code>insertTab</code>. 
     * 
     * @param title the title to be displayed in this tab
     * @param icon the icon to be displayed in this tab
     * @param component the component to be displayed when this tab is clicked
     * 
     */
    public void addTab(String title, Icon icon, Component component) {
        addTab(title, icon, component, null, null, -1, -1, false);
    }
    public void addTab(String title, Icon icon, Component component, boolean guessPath) {
      addTab(title, icon, component, null, null, -1, -1, guessPath);
    }
    
    public void addTab(String title, Icon icon, Component component, double weight) {
        addTab(title, icon, component, null, null, weight, -1, false);
    }
    public void addTab(String title, Icon icon, Component component, double weight, boolean guessPath) {
      addTab(title, icon, component, null, null, weight, -1, guessPath);
    }
    
    /**
     * Adds a <code>component</code> and <code>tip</code>
     * represented by a <code>title</code> and/or <code>icon</code>,
     * either of which can be <code>null</code>.  If <code>icon</code>
     * is non-<code>null</code> and it implements <code>ImageIcon</code>
     * a corresponding disabled icon will automatically be created
     * and set on the tabbedpane. 
     * Cover method for <code>insertTab</code>.
     * 
     * @param title the title to be displayed in this tab
     * @param icon the icon to be displayed in this tab
     * @param component the component to be displayed when this tab is clicked
     * @param tip the tooltip to be displayed for this tab
     * 
     */
    public void addTab(String title, Icon icon, Component component, String tip) {
        addTab(title, icon, component, tip, null, -1, -1, false); 
    }
    public void addTab(String title, Icon icon, Component component, String tip, boolean guessPath) {
      addTab(title, icon, component, tip, null, -1, -1, guessPath); 
    }
    
    public void addTab(String title, Icon icon, Component component, String tip, String constraints) {
        addTab(title, icon, component, tip, constraints, -1, -1, false); 
    }
    public void addTab(String title, Icon icon, Component component, String tip, String constraints, boolean guessPath) {
      addTab(title, icon, component, tip, constraints, -1, -1, guessPath); 
    }
    
    public void addTab(String title, Component component, String constraints) {
        addTab(title, null, component, null, constraints, -1, -1, false); 
    }
    public void addTab(String title, Component component, String constraints, boolean guessPath) {
      addTab(title, null, component, null, constraints, -1, -1, guessPath); 
    }
    
    public void addTab(String title, Icon icon, Component component, String tip, double weight) {
        addTab(title, icon, component, tip, null, weight, -1, false); 
    }
    public void addTab(String title, Icon icon, Component component, String tip, double weight, boolean guessPath) {
      addTab(title, icon, component, tip, null, weight, -1, guessPath); 
    }
    
    public void addTab(String title, Component component, String constraints, double weight) {
        addTab(title, null, component, null, constraints, weight, -1, false); 
    }
    public void addTab(String title, Component component, String constraints, double weight, int index) {
      addTab(title, null, component, null, constraints, weight, index, false); 
    }
    public void addTab(String title, Component component, String constraints, double weight, boolean guessPath) {
      addTab(title, null, component, null, constraints, weight, -1, guessPath); 
    }
    
    public static class AddConstraints {
      private Component component = null;
        protected String title;
        protected Icon icon;
        protected String tip;
        protected String constraints;
        protected double weight = 0.0;
        protected boolean enabled = false;
        protected int index = -1;
        protected double height = -1;
        protected double width = -1;
        protected char mnemonic = '\0';
    
        public String getConstraints() {
          return constraints;
        }
    
        public boolean isEnabled() {
          return enabled;
        }
    
        public Icon getIcon() {
          return icon;
        }
    
        public int getIndex() {
          return index;
        }
    
        public String getTip() {
          return tip;
        }
    
        public String getTitle() {
          return title;
        }
    
        public double getWeight() {
          return weight;
        }
    
        public double getHeight() {
          return height;
        }
    
        public double getWidth() {
          return width;
        }
    
        public char getMnemonic() {
          return mnemonic;
        }
        
        protected AddConstraints(AddConstraints o) {
          if (o != null) {
            this.title = o.title;
            this.icon = o.icon;
            this.tip = o.tip;
            this.constraints = o.constraints;
            this.weight = o.weight;
            this.enabled = o.enabled;
            this.index = o.index;
            this.width = o.width;
            this.height = o.height;
            this.mnemonic = o.mnemonic;
          }
        }
        
        protected AddConstraints() { }
    
        private final static String TO_STRING =
          "title:{0},tip:{1},enabled:{2},weight:{3},index:{4},constraints:{5},width:{6},height:{7},mnemonic:{8}";
    
        private final static Pattern FROM_STRING =
          Pattern.compile("title:(.*?),tip:(.*?),enabled:(?:(?i)(true|false)),weight:(-?[.0-9]*?),index:(-1|[0-9]+?),constraints:(.*?),width:(-?[.0-9]*?),height:(-?[.0-9]*?),mnemonic:(.?)");
    
        protected AddConstraints(String title, Icon icon, String tip,
                String constraints, double weight, boolean enabled, int index, int width, int height, char mnemonic) {
            this.title = title;
            this.icon = icon;
            this.tip = tip;
            this.constraints = constraints;
            this.weight = weight;
            this.enabled = enabled;
            this.index = index;
            this.width = width;
            this.height = height;
            this.mnemonic = mnemonic;
        }
    
        public static AddConstraints fromString(String s) {
          AddConstraints ac = new AddConstraints();
          Matcher m = FROM_STRING.matcher(s);
          if (! m.matches())
            return null;
          int c = m.groupCount();
          switch (c - 1) {
          case 8: {
            String g = m.group(c--);
            ac.mnemonic = g.length() > 0 ? g.charAt(0) : '\0';
          }
          case 7: ac.height = Double.parseDouble(m.group(c--));
          case 6: ac.width = Double.parseDouble(m.group(c--));
          case 5: ac.constraints = m.group(c--);
          case 4: ac.index = Integer.parseInt(m.group(c--));
          case 3: ac.weight = Double.parseDouble(m.group(c--));
          case 2: ac.enabled = Boolean.valueOf(m.group(c--)).booleanValue();
          case 1: ac.tip = m.group(c--);
          case 0: ac.title = m.group(c--);
          break;
          }
          return ac;
        }
    
        public static AddConstraints[] parse(String s) {
          Vector v = new Vector();
          Matcher m = FROM_STRING.matcher(s);
          while (m.find()) {
            AddConstraints ac = fromString(m.group(0));
            v.add(ac);
          }
          return (AddConstraints[]) v.toArray(new AddConstraints[v.size()]);
        }
        
        public String toString() {
//          String mnemonic = "\\u";
//          String u = Integer.toHexString(this.mnemonic);
//          while (u.length() < 4) u = '0' + u;
//          mnemonic += u;
          String mnemonic = ""+this.mnemonic;
          return MessageFormat.format(TO_STRING, new Object[] {
              title, tip, Boolean.toString(enabled), Double.toString(weight), Integer.toString(index), constraints, Double.toString(width), Double.toString(height), mnemonic
          });
        }
    
        protected void set(String title, Icon icon, String tip,
                String constraints, double weight, boolean enabled, int index, int width, int height, char mnemonic) {
          this.title = title;
          this.icon = icon;
          this.tip = tip;
          this.constraints = constraints;
          this.weight = weight;
          this.enabled = enabled;
          this.index = index;
          this.width = width;
          this.height = height;
          this.mnemonic = mnemonic;
        }
    }

    protected class ComponentMenuAction extends AbstractAction {
      private final WeakReference ref;
      private final String title;
      private ComponentMenuAction(String title, Component c) {
        super(title.replaceAll("&", ""));
        this.title = title;
        int index = title.indexOf("&");
        if (index != -1 && title.length() > index + 1) {
          super.putValue(Action.MNEMONIC_KEY, new Integer(title.charAt(index+1)));
        }
        this.ref = new WeakReference(c);
      }
    
      public boolean isSelected() {
        Component c = (Component) ref.get();
        if (c != null)
          return c.isEnabled();
        setEnabled(false);
        return false;
      }
    
      public void actionPerformed(ActionEvent e) {
        final Component component = (Component) ref.get();
        if (component != null) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              if (!component.isEnabled()) {
                component.setEnabled(true);
              }
              else {
                TabbedPane tp = getTabbedPaneAncestor(component);
                if (tp != null) {
                  tp.setSelectedComponent(component);
                  SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                      component.requestFocusInWindow();
                    }
                  });
                }
              }
              //        component.setEnabled(! component.isEnabled());
            }
          });
        }
      }
    
      public String toString() {
        return "ComponentAction("+getValue(Action.NAME)+"): "+ref.get();
      }
    }

    public void addTab(String title, Component component, String constraints, double weight, boolean guessPath, int index) {
      addTab(title, null, component, null, constraints, weight, -1, guessPath); 
    }
    
    
    protected static final class ConstraintSplitter {
        private final static Pattern constraintsSplitPattern =
            Pattern.compile(
                "(?i)(" + LEFT + "|" + RIGHT + "|" + TOP + "|" + BOTTOM + ")");
        private Matcher matcher = null;
        protected ConstraintSplitter(String constraints) {
          this.constraints = constraints;
          if (constraints != null)
            matcher = constraintsSplitPattern.matcher(constraints); 
        }
        int index = 0;
        private String constraints;
        protected String nextToken() {
            if (matcher != null && matcher.find(index)) {
                int end = matcher.end();
                index = end;
                return matcher.group(0);
            }
            return null;
        }
        protected boolean hasMoreTokens() {
          if (matcher == null)
            return false;
          return matcher.find(index);
        }
        protected String[] split() {
          if (matcher == null)
            return new String[0];
          Vector v = new Vector();
          matcher.reset();
          while (matcher.find()) {
            v.add(matcher.group(1));
          }
          return (String[]) v.toArray(new String[v.size()]);
        }
    }
    
    protected static final class FillerComponent extends JLabel {
        protected FillerComponent() {
            super("Filler Component");
        }
        public Dimension getMinimumSize() {
          return new Dimension(0, 0);
        }
        public Dimension getPreferredSize() {
          return new Dimension(0, 0);
        }
        public Dimension getMaximumSize() {
          return new Dimension(0, 0);
        }
        public boolean isVisible() {
          return false;
        }
        public void setVisible(boolean ignored) {
          super.setVisible(false);
        }
    }
    
    protected static class DraggableTabbedPaneButtonListener extends BasicButtonListener {
      DraggableTabbedPane.TabbedPane parent;
      AbstractButton button;
      public DraggableTabbedPaneButtonListener(AbstractButton b, DraggableTabbedPane.TabbedPane parent) {
        super(b);
        this.button = b;
        this.parent = parent;
        parent.addMouseListener(this);
        parent.addMouseMotionListener(this);
      }
      Rectangle bounds = new Rectangle(0, 0, 0, 0);
      Rectangle repaintRect = new Rectangle(-1, -1, 1, 1);
      Insets insets = new Insets(0, 0, 0, 0);
      protected MouseEvent transformEvent(MouseEvent ee) {
        if (ee.getSource() != parent)
          return null;
        int x = ee.getX();
        int y = ee.getY();
        button.getBounds(bounds);
        if (! bounds.contains(x, y))
          return null;
        x -= bounds.x;
        y -= bounds.y;
        MouseEvent e = new MouseEvent(button, ee.getID(), ee.getWhen(),
            ee.getModifiersEx() | ee.getModifiers(), x, y, ee.getClickCount(),
            ee.isPopupTrigger(), ee.getButton());
        button.getInsets(insets);
        parent.repaint(bounds.x - insets.left, bounds.y - insets.top,
            bounds.width + insets.left + insets.right,
            bounds.height + insets.top + insets.bottom);
        return e;
      }
      protected MouseEvent transformEvent(MouseEvent ee, int id) {
        int x = ee.getX();
        int y = ee.getY();
        button.getBounds(bounds);
        x -= bounds.x;
        y -= bounds.y;
        MouseEvent e = new MouseEvent(button, id, ee.getWhen(),
            ee.getModifiersEx() | ee.getModifiers(), x, y, ee.getClickCount(),
            ee.isPopupTrigger(), ee.getButton());
        button.getInsets(insets);
        parent.repaint(bounds.x - insets.left, bounds.y - insets.top,
            bounds.width + insets.left + insets.right,
            bounds.height + insets.top + insets.bottom);
        return e;
      }
      // repaint button when anything interesting happens
      public void mouseClicked(MouseEvent e) {
        //              if ((e = transformEvent(e)) != null)
        //              super.mouseClicked(e);
      }
      public void mousePressed(MouseEvent e) {
        if ((e = transformEvent(e)) != null)
          super.mousePressed(e);
      }
      public void mouseReleased(MouseEvent e) {
        if (e.getSource() == button) {
          DefaultButtonModel model = (DefaultButtonModel) button.getModel();
          //                  if (button.isSelected() && model.isArmed()) {
          //                  ButtonGroup bg = model.getGroup();
          //                  Enumeration en = bg.getElements();
          //                  JToggleButton clearButton = (JToggleButton) en.nextElement();
          //                  clearButton.setSelected(true);
          //                  }
          //                  else {
          super.mouseReleased(e);
          //                  }
        }
        else if ((e = transformEvent(e)) != null)
          mouseReleased(e);
      }
      public void mouseDragged(MouseEvent ee) {
        MouseEvent e = null;
        if (! entered) {
          if ((e = transformEvent(ee)) != null) {
            entered = true;
            super.mouseDragged(e);
            e = transformEvent(ee, MouseEvent.MOUSE_ENTERED);
            super.mouseEntered(e);
          }
        }
        else {
          if ((e = transformEvent(ee)) == null) {
            entered = false;
            e = transformEvent(ee, MouseEvent.MOUSE_EXITED);
            super.mouseExited(e);                        
          }
        }
      }
      boolean entered = false;
      public void mouseMoved(MouseEvent ee) {
        MouseEvent e = null;
        if (! entered) {
          if ((e = transformEvent(ee)) != null) {
            entered = true;
            super.mouseMoved(e);
            e = transformEvent(ee, MouseEvent.MOUSE_ENTERED);
            super.mouseEntered(e);
          }
        }
        else {
          if ((e = transformEvent(ee)) == null) {
            entered = false;
            e = transformEvent(ee, MouseEvent.MOUSE_EXITED);
            super.mouseExited(e);                        
          }
        }
      }
      public void mouseEntered(MouseEvent e) {
        if (e.getSource() instanceof AbstractButton)
          super.mouseEntered(e);
      }
      public void mouseExited(MouseEvent e) {
        if (e.getSource() instanceof AbstractButton)
          super.mouseExited(e);
      }
      public void stateChanged(ChangeEvent e) {
        super.stateChanged(e);
        button.getBounds(bounds);
        button.getInsets(insets);
        parent.repaint(bounds.x - insets.left, bounds.y - insets.top, bounds.width + insets.left + insets.right, bounds.height + insets.top + insets.bottom);
      }
    }

    protected static class DraggableTabbedPaneButtonUI extends BasicButtonUI {
        DraggableTabbedPane.TabbedPane parent;
        
        public DraggableTabbedPaneButtonUI(DraggableTabbedPane.TabbedPane parent) {
            this.parent = parent;
        }
        protected BasicButtonListener createButtonListener(AbstractButton b) {
            return new DraggableTabbedPaneButtonListener(b, parent);
        }
    }

    public void addTab(String title, Icon icon, Component component, String tip, String constraints, double weight) {
        addTab(title, icon, component, tip, constraints, weight, -1, false, false);
    }
    
    public void addTab(String title, Icon icon, Component component, String tip, String constraints, double weight, int index) {
      addTab(title, icon, component, tip, constraints, weight, index, false, false);
    }
    
    public TabbedPane addTab(String title, Icon icon, Component component, String tip, String constraints, double weight, int index, boolean guessPath) {
      return addTab(title, icon, component, tip, constraints, weight, index, guessPath, false);
    }

    /**
     * This is where all the work of splitting panes to add a tab gets done.
     * 
     * @param title
     * @param icon
     * @param component
     * @param tip
     * @param constraints
     * @param weight
     * @param index
     * @param guessPath if true, then if path specified by constraints leads to
     *          a split pane, not a tab pane, then go left/top until a tab pane
     *          is found
     * @param truncatePath if true, then don't create any split panes; if path
     *          specified by constraints leads to a tab pane before path runs
     *          out, stop following path and put component in that tab pane
     */
    protected TabbedPane insertTabComponent(String title, Icon icon, Component component, String tip, String constraints, double weight, int index, boolean guessPath, boolean truncatePath) {
      if (constraints == null) {
        if (rootTabPane != null) {
          if (index == -1)
            rootTabPane.addTab(title, icon, component, tip);
          else
            rootTabPane.insertTab(title, icon, component, tip, index);
          return rootTabPane;
        }
      }
      ConstraintSplitter st = new ConstraintSplitter(constraints);
      Container target = rootSplitPane != null ? (Container) rootSplitPane : (Container) rootTabPane;
      String constraint = null;
      boolean truncatePathNow = false;
      while (st.hasMoreTokens() && target != null) {
        if (constraint == null)
          constraint = st.nextToken();
        int orientation = HORIZONTAL_SPLIT;
        if (TOP.equals(constraint) || BOTTOM.equals(constraint))
          orientation = VERTICAL_SPLIT;
        if (target == null) {
          // should not happen!
        }
        else if (target instanceof TabbedPane) {
          if (st.hasMoreTokens()) {
            if (truncatePath && truncatePathNow) {
              // stop following path
              break;
            }
            else if (truncatePath) {
              // only allow one split?
              truncatePathNow = true;
            }
////            if (truncatePath) {
////              break;
////            }
            // Fill component case
            target = splitTabbedPane((TabbedPane) target, orientation, constraint, weight);
            continue;
          }
          SplitPane splitPane = splitTabbedPane((TabbedPane) target, component, title, icon, tip, orientation, constraint, weight);
          target = null;
        }
        else if (target instanceof SplitPane) {
          SplitPane splitPane = (SplitPane) target;
          Component c = null;
          if (LEFT.equals(constraint))
            c = splitPane.getLeftComponent();
          else if (TOP.equals(constraint))
            c = splitPane.getTopComponent();
          else if (RIGHT.equals(constraint))
            c = splitPane.getRightComponent();
          else if (BOTTOM.equals(constraint))
            c = splitPane.getBottomComponent();
          if (orientation != splitPane.getOrientation()
              || (!st.hasMoreTokens() && c instanceof SplitPane)) {
            // add new split pane with new orientation, new comp
            // according to constraint, old split pane in other
            Container p = splitPane.getParent();
            if (p instanceof SplitPane) {
              SplitPane parent = (SplitPane) p;
              SplitPane newParent = new SplitPane(orientation, continuousLayout, null, null, weight);
              splitPanes.put(newParent, null);
              TabbedPane tabPane = createTabbedPane();
              tabPanes.put(tabPane, null);
              try {
                if (parent.getLeftComponent() == splitPane) {
                  parent.setLeftComponent(newParent);
                }
                else if (parent.getRightComponent() == splitPane) {
                  parent.setRightComponent(newParent);
                }
                if (LEFT.equals(constraint)) {
                  newParent.setLeftComponent(tabPane);
                  newParent.setRightComponent(splitPane);
                }
                else if (TOP.equals(constraint)) {
                  newParent.setTopComponent(tabPane);
                  newParent.setBottomComponent(splitPane);
                }
                else if (RIGHT.equals(constraint)) {
                  newParent.setLeftComponent(splitPane);
                  newParent.setRightComponent(tabPane);
                }
                else if (BOTTOM.equals(constraint)) {
                  newParent.setTopComponent(splitPane);
                  newParent.setBottomComponent(tabPane);
                }
              } catch (IllegalArgumentException x) {
                x.printStackTrace();
                throw x;
              }
              target = tabPane;
            }
            else if (p == this) { // p had better be the root pane!
              remove(splitPane);
              SplitPane newParent = new SplitPane(orientation, continuousLayout, null, null, weight);
              TabbedPane tabPane = new TabbedPane(tabPlacement, tabLayoutPolicy, detachable);
              if (LEFT.equals(constraint)) {
                newParent.setLeftComponent(tabPane);
                newParent.setRightComponent(splitPane);
              }
              else if (TOP.equals(constraint)) {
                newParent.setTopComponent(tabPane);
                newParent.setBottomComponent(splitPane);
              }
              else if (RIGHT.equals(constraint)) {
                newParent.setLeftComponent(splitPane);
                newParent.setRightComponent(tabPane);
              }
              else if (BOTTOM.equals(constraint)) {
                newParent.setTopComponent(splitPane);
                newParent.setBottomComponent(tabPane);
              }
              splitPanes.put(newParent, null);
              super.add(newParent, BorderLayout.CENTER);
              rootSplitPane = newParent;
              target = tabPane;
            }
          }
          else {
            if (c != null) {
              if (c instanceof TabbedPane)
                target = (Container) c;
              else if (c instanceof SplitPane) {
                target = (Container) c;
              }
              else {
                target = null;
              }
            }
            else target = null;
          }
        }
        constraint = null;
      }
      if (target != null) {
        if (target instanceof SplitPane) {
          if (! guessPath) {
            // running out of path with reaching a tab pane
            throw new IllegalArgumentException("DraggableTabbedPane.addTab("+title+"): invalid constraints lead to existing SplitPane: {"+constraints+"} target="+target);
          }
          while (target != null && target instanceof SplitPane) {
            // go left/top until a tab pane is reached
            SplitPane sp = (SplitPane) target;
            Component left = sp.getLeftComponent();
            if (left instanceof Container) {
              target = (Container) left;
            }
            else target = null;
          }
        }
        if (target instanceof TabbedPane) {
          TabbedPane tabbedPane = (TabbedPane) target;
          if (index == -1)
            tabbedPane.addTab(title, icon, component, tip);
          else
            tabbedPane.insertTab(title, icon, component, tip, index);
        }
        else {
          throw new IllegalArgumentException("DraggableTabbedPane.addTab("+title+"): invalid constraints: {"+constraints+"} target="+target);
        }
      }
      revalidate();
      if (target instanceof TabbedPane)
        return (TabbedPane) target;
//      new RuntimeException("Target not TabbedPane: "+target).printStackTrace();
      return null;
    }

    public void fillSplitConstraintPath(String constraint, double weight) {
        addTab("Filler", null, new FillerComponent(), null, constraint, weight, -1, false);
    }
    
    protected SplitPane splitTabbedPane(TabbedPane target, int orientation, String constraint, double weight) {
//      if (DEBUG > 11) {
//        new RuntimeException("filler splitTabbedPane("+target+","+orientation+","+constraint+","+weight+")").printStackTrace();
//      }
        return splitTabbedPane(target, new FillerComponent(), "Filler", null, null, orientation, constraint, weight);
    }
    
    /**
     * Computes the path to <code>leaf</code> from the root of this
     * {@link DraggableTabbedPane}, encoded as a string suitable for passing as
     * the <code>constraint</code> argument to one of the
     * {@link #addTab(String, Icon, Component, String, String, double, int, boolean)} methods.
     * The path terminates at the {@link TabbedPane} that contains
     * <code>leaf</code> or one of its ancestors.
     * <p>
     * If <code>leaf</code> is not a descedant of this
     * {@link DraggableTabbedPane}, or if there is another
     * {@link DraggableTabbedPane} which is a descendant of this one and also is
     * an ancestor of <code>leaf</code>, returns <code>null</code>.
     * 
     * @param leaf
     *          a {@link Component} for which this {@link DraggableTabbedPane}
     *          is an ancestor
     * @return the path string from the root of this {@link DraggableTabbedPane}
     *         to <code>leaf</code> if one exists, or <code>null</code>
     */
    public String getPathToRoot(Component leaf) {
      LinkedList path = new LinkedList();
      if (rootTabPane != null && rootTabPane.isAncestorOf(leaf)) {
        return "";
      }
      else if (rootSplitPane != null && rootSplitPane.isAncestorOf(leaf)) {
        Component c = leaf;
        Container p = c.getParent();
        while (p != null && c != rootSplitPane && ! (p instanceof DraggableTabbedPane)) {
          if (p instanceof SplitPane) {
            SplitPane sp = (SplitPane) p;
            if (sp.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
              if (sp.getTopComponent() == c)
                path.addFirst(JSplitPane.TOP);
              else if (sp.getBottomComponent() == c)
                path.addFirst(JSplitPane.BOTTOM);
            }
            else {
              if (sp.getLeftComponent() == c)
                path.addFirst(JSplitPane.LEFT);
              else if (sp.getRightComponent() == c)
                path.addFirst(JSplitPane.RIGHT);
            }
          }
          c = p;
          p = p.getParent();
        }
        if (c == rootSplitPane) {
          StringBuffer sb = new StringBuffer();
          for (Iterator i = path.iterator(); i.hasNext();) {
            String element = (String) i.next();
            sb.append(element);
          }
          return sb.toString();
        }
      }
      return null;
    }
    
    /**
     * @see java.awt.Component#paint(java.awt.Graphics)
     * @param g
     */
    public void paint(Graphics g) {
        super.paint(g);
        if (dragComponent != null)
            dragComponent.paint(g);
    }
    public void setDetachedIconImage (Image image) {
        detachedIconImage = image;
        Iterator i = tabPanes.keySet().iterator();
        while (i.hasNext()) {
            TabbedPane pane = (TabbedPane) i.next();
            pane.setDetachedIconImage(image);
        }        
    }
    
    //
    // Delegate methods for the descendent TabbedPane's
    //
    
    public List getDetachables() {
        LinkedList list = new LinkedList();
        Iterator i = tabPanes.keySet().iterator();
        while (i.hasNext()) {
            TabbedPane pane = (TabbedPane) i.next();
            pane.getDetachables(list);
        }
        return list;
    }
    
    public Detachable getDetachable(Component c) {
        Detachable d = null;
        Iterator i = tabPanes.keySet().iterator();
        while (d == null && i.hasNext()) {
            TabbedPane pane = (TabbedPane) i.next();
            d = pane.getDetachableFor(c);
        }
        return d;
    }

    public TabbedPane getTabbedPaneAncestor(Component component) {
      Component c = component;
      Container p = c.getParent();
      while (p != null && !(p instanceof TabbedPane)) {
        c = p;
        p = p.getParent();
      }
      if (p == null)
        return null;
      return (TabbedPane) p;
    }
    
    public SplitPane getSplitPaneAncestor(Component component) {
      Component c = component;
      Container p = c.getParent();
      while (p != null && !(p instanceof SplitPane)) {
        c = p;
        p = p.getParent();
      }
      if (p == null)
        return null;
      return (SplitPane) p;
    }
    
    public String getSplitPaneSide(Component component) {
      String side = null;
      Component c = component;
      Container p = c.getParent();
      while (p != null && !(p instanceof SplitPane)) {
        c = p;
        p = p.getParent();
      }
      if (p != null) {
        SplitPane sp = (SplitPane) p;
        int orientation = sp.getOrientation();
        if (sp.getLeftComponent() == c) {
          if (orientation == JSplitPane.HORIZONTAL_SPLIT)
            side = SplitPane.LEFT;
          else
            side = SplitPane.TOP;
        }
        else if (sp.getRightComponent() == c) {
          if (orientation == JSplitPane.HORIZONTAL_SPLIT)
            side = SplitPane.RIGHT;
          else
            side = SplitPane.BOTTOM;
        }
      }
      return side;
    }

    protected AddConstraints addedComponent(Component c, String title, Icon icon, String tip,
            String constraints, double weight, int index, int mnemonic) {
        AddConstraints ac = getAddConstraints(c);
        if (ac == null)
          ac = new AddConstraints(title, icon, tip, constraints, weight, c.isEnabled(), index, -1, -1, (char) mnemonic);
        else {
          ac.set(title, icon, tip, constraints, weight, c.isEnabled(), index, -1, -1, (char) mnemonic);
        }
        componentConstraints.put(c, ac);
        return ac;
    }

    public TabbedPane addTab(String title, Icon icon, Component c, String tip,
        String constraints, double weight, int index, boolean guessPath,
        boolean truncatePath) {
      TabbedPane tabbedPane = insertTabComponent(title, icon, c, tip, constraints, weight, index, guessPath, truncatePath);
      if (c instanceof FillerComponent) {
        return tabbedPane;
      }
      synchronized (components) {
        components.put(c, title);
      }
      int mnemonic = -1;
//      Container p = c.getParent();
//      while (tabbedPane == null && p != null) {
//        if (p instanceof TabbedPane) {
//          tabbedPane = (TabbedPane) p;
//        }
//        else {
//          p = p.getParent();
//        }
//      }
      if (tabbedPane == null)
        tabbedPane = getTabbedPaneAncestor(c);
      if (tabbedPane != null) {
        int atIndex = tabbedPane.indexOfComponent(c);
        if (atIndex != -1) {
          mnemonic = tabbedPane.getMnemonicAt(atIndex);
        }
      }
      AddConstraints ac = addedComponent(c, title, icon, tip, constraints, weight, index, mnemonic);
      if (! ac.enabled) {
        c.getParent().remove(c);
      }
      
      componentActions.put(c, new ComponentMenuAction(title, c));
      getComponentsMenu().removeAll();
      fillComponentsMenu(false);
      c.addPropertyChangeListener("enabled", new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          Component c = (Component) evt.getSource();
          boolean enabled = c.isEnabled();
          final AddConstraints ac = getAddConstraints(c);
          if (ac != null && ac.enabled != enabled) {
            ac.enabled = enabled;
            if (enabled) {
              int index = ac.index;
              final double weight = ac.weight;
              final double width = ac.width;
              final double height = ac.height;
//            final int mnemonic = ac.mnemonic;
              final Component component = c;
              String title = insertMnemonicCharInTitle(ac.title, ac.mnemonic);
              if (DEBUG > 11) {
                if (isApplyingConstraints()) {
                  Map acc = getAllComponentConstraints();
                  System.err.println("Constraints before restoring " + ac.title
                      + " at " + ac.constraints + " [" + acc.size() + "]" + formatCollectionString(acc.keySet()));
                  new RuntimeException(ac.title).printStackTrace();
                  if (false && isShowing()) {
                    JOptionPane.showConfirmDialog(DraggableTabbedPane.this, 
                        ac + "\n" + getCurrentConstraints(c, null), "Restore this component?",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                    acc = getAllComponentConstraints();
                    System.out.println("Constraints before restoring " + ac.title
                        + " at " + ac.constraints + " [" + acc.size() + "]" + formatCollectionString(acc.keySet()));
                  }
                }
              }
              final TabbedPane[] tabbedPane = new TabbedPane[] {
                  insertTabComponent(title, ac.icon, c, ac.tip, ac.constraints, ac.weight, index, true, true)
              };
//            if (tabbedPane[0] == null) {
//            tabbedPane[0] = getTabbedPaneAncestor(component);
//            }
              if (tabbedPane[0] != null) {
                int indexOfComponent = tabbedPane[0].indexOfComponent(c);
//              if (indexOfComponent != -1) {
//              tabbedPane[0].setMnemonicAt(indexOfComponent, mnemonic);
//              }
                if (tabbedPane[0].getTabCount() == 1 && indexOfComponent == 0) {
                  SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                      restoreSplitPaneParentDivider(weight, width, height, component, true);
                    }
                  });
                }
              }
              SwingUtilities.invokeLater(new Runnable() {
                int count = 10;
                public void run() {
                  if (tabbedPane[0] == null)
                    tabbedPane[0] = getTabbedPaneAncestor(component);
                  if (tabbedPane[0] == null) {
                    if (count-- > 0)
                      SwingUtilities.invokeLater(this);
                    else
                      throw new RuntimeException("Could not restore component after 10 tries: "+ac+"\n"+component.getParent());
                    return;
                  }
                  int index = tabbedPane[0].indexOfComponent(component);
                  if (index >= 0 && index < tabbedPane[0].getTabCount()) {
//                  tabbedPane[0].setMnemonicAt(index, mnemonic);
                    tabbedPane[0].setSelectedComponent(component);
                    component.requestFocusInWindow();
                  }
                }
              });
              //                Container p = c.getParent();
              //                while (p != null && !(p instanceof SplitPane)) {
              //                  p = p.getParent();
              //                }
              //                if (p != null) {
              //                  final SplitPane sp = (SplitPane) p;
              //                  SwingUtilities.invokeLater(new Runnable() {
              //                      public void run() {
              //                        if (weight > 1) {
              //                          sp.setDividerLocation((int) weight);
              //                        }
              //                        else if (weight > 0) {
              //                          sp.setDividerLocation(weight);
              //                        }
              //                      }
              //                    });
              //                }
            }
            else {
              if (isAncestorOf(c)) {
                getCurrentConstraints(c, ac);
                c.getParent().remove(c);
              }
            }
          }
          if (getComponentsMenu() != null) {
            getComponentsMenu().removeAll();
            fillComponentsMenu(false);
          }
        }
      });
      return tabbedPane;
    }

    protected void fillComponentsMenu(boolean debugInfo) {
      getComponentsMenu().removeAll();
      ArrayList al = new ArrayList();
      for (Iterator i = componentActions.keySet().iterator(); i.hasNext();) {
        final Component c = (Component) i.next();
        final ComponentMenuAction a = (ComponentMenuAction) componentActions.get(c);
        JMenuItem mi = new ComponentMenuItem(a, c, debugInfo);
        mi.setSelected(a.isSelected());
        al.add(mi);
      }
      Collections.sort(al, actionComparator);
      for (Iterator i = al.iterator(); i.hasNext();) {
        JMenuItem mi = (JMenuItem) i.next();
        getComponentsMenu().add(mi);
      }
    }

    public AddConstraints getAddConstraints(Component c) {
        return (AddConstraints) componentConstraints.get(c);
    }

    public Map getAllComponents() {
        synchronized (components) {
            Map m = new WeakHashMap(components);
            return m;
        }
    }

    public List getComponentActions() {
      ArrayList actions = new ArrayList(componentActions.values());
      Collections.sort(actions, actionComparator);
      return actions;
    }

    public AddConstraints getCurrentConstraints(Component component) {
      return getCurrentConstraints(component, getAddConstraints(component));
    }

    public AddConstraints getCurrentConstraints(Component component, AddConstraints ac) {
      if (ac == null)
        ac = new AddConstraints(getAddConstraints(component));
      Container p = getTabbedPaneAncestor(component);
      if (p == null && component.isEnabled()) {
//        System.err.println("No TPA for enabled "+component+"\nparent: "+component.getParent());
      }
      if (p != null) {
        TabbedPane tp = (TabbedPane) p;
        ac.index = tp.indexOfComponent(component);
        ac.icon = tp.getUserIconAt(ac.index);
        ac.tip = tp.getToolTipTextAt(ac.index);
        ac.title = tp.getTitleAt(ac.index);
        ac.enabled = component.isEnabled() && tp.isEnabledAt(ac.index);
        ac.constraints = getPathToRoot(component);
        Dimension s = tp.getSize();
        ac.width = s.width;
        ac.height = s.height;
        SplitPane sp = getSplitPaneAncestor(component);
        if (sp != null) {
          double weight = getSplitPaneDividerProportion(sp);
          if (weight != -1) {
            ac.weight = weight;
            int orientation = sp.getOrientation();
            if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
              ac.width = ac.weight;
              while (sp != null && sp.getOrientation() != JSplitPane.VERTICAL_SPLIT) {
                Container spp = sp.getParent();
                if (spp != null && spp instanceof SplitPane) {
                  sp = (SplitPane) spp;
                }
                else {
                  sp = null;
                }
              }
              if (sp != null) {
                weight = getSplitPaneDividerProportion(sp);
                if (weight != -1) {
                  ac.height = weight;
                }
              }
            }
            else if (orientation == JSplitPane.VERTICAL_SPLIT) {
              ac.height = ac.weight;
              while (sp != null && sp.getOrientation() != JSplitPane.HORIZONTAL_SPLIT) {
                Container spp = sp.getParent();
                if (spp != null && spp instanceof SplitPane) {
                  sp = (SplitPane) spp;
                }
                else {
                  sp = null;
                }
              }
              if (sp != null) {
                weight = getSplitPaneDividerProportion(sp);
                if (weight != -1) {
                  ac.width = weight;
                }
              }
            }
          }

        }
        ac.mnemonic = (char) tp.getMnemonicAt(ac.index);
      }
      return ac;
    }

    protected double getSplitPaneDividerProportion(SplitPane sp) {
      double dividerLocation = sp.getDividerLocation();
      int size = sp.getOrientation() == JSplitPane.HORIZONTAL_SPLIT 
        ? sp.getWidth() : sp.getHeight();
      //      double min = sp.getMinimumDividerLocation();
//      double max = sp.getMaximumDividerLocation();
//      double divisor = max - min;
      double divisor = size - sp.getDividerSize();
      double weight = -1;
      if (divisor > 0) {
        weight = dividerLocation / divisor;
      }
      return weight;
    }

    public Map getAllComponentConstraints() {
      Map m = getAllComponents();
      LinkedHashMap map = new LinkedHashMap();
      for (Iterator iter = m.keySet().iterator(); iter.hasNext();) {
        Component component = (Component) iter.next();
        String title = (String) m.get(component);
        AddConstraints ac = getCurrentConstraints(component, null);
        map.put(ac, component);
      }
      return map;
    }

    private boolean applyingConstraints = false;

    public void applyConstraints(List newConstraints) {
      setApplyingConstraints(true);
      if (DEBUG > 9)
        new Exception("applyConstraints: "+formatCollectionString(newConstraints)).printStackTrace();
      Map currentConstraints = getAllComponentConstraints();
//    StringBuffer sb = new StringBuffer();
//    for (Iterator it = currentConstraints.keySet().iterator(); it.hasNext();) {
//    Object key = it.next();
//    sb.append(key);
//    sb.append("=");
//    sb.append(currentConstraints.get(key));
//    if (it.hasNext())
//    sb.append('\n');
//    }
//    System.out.println("Current Constraints:\n"+sb);
      Map m = new HashMap();
      for (Iterator iter = currentConstraints.keySet().iterator(); iter.hasNext();) {
        AddConstraints ac = (AddConstraints) iter.next();
        Object old = m.put(ac.title, ac);
        if (old != null) {
          // punt!!!
        }
      }
      final TreeMap dividerConstraints = new TreeMap(AddConstraintsComparator.getInstance());
      newConstraints = new Vector(newConstraints);
      final Set runnables = new LinkedHashSet();
      Collections.sort(newConstraints, new AddConstraintsComparator());
      final LinkedHashMap tabPanes = new LinkedHashMap();
      if (newConstraints.isEmpty()) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            setApplyingConstraints(false);
          }
        });
      }
      else {
        for (Iterator iter = newConstraints.iterator(); iter.hasNext();) {
          final AddConstraints ac = (AddConstraints) iter.next();
          AddConstraints current = (AddConstraints) m.get(ac.title);
          if (DEBUG > 0)
            System.out.println("\napplyConstraints("+ac.constraints+"\n\tnewcons: "+ac+"\n\tcurrent: "+current);
          if (current != null) {
            final Component c = (Component) currentConstraints.get(current);
            if (DEBUG > 0)
              System.out.println("\tcomponent: "+c);
            if (c != null) {
              c.setEnabled(false);
              Runnable runnable = new Runnable() {
                int count = -1;
                AddConstraints savedAC = null;
                boolean enabled;

                public void run() {
                  if (count++ < 0) {
//                    c.setEnabled(false);
//                    SwingUtilities.invokeLater(this);
//                  }
//                  else if (count == 0) {
                    savedAC = new AddConstraints(ac);
                    savedAC.component = c;
                    enabled = ac.enabled;
                    if (enabled) {
                      dividerConstraints.put(savedAC.constraints, savedAC);
                    }
                    ac.enabled = false;
                    componentConstraints.put(c, ac);
                    c.setEnabled(true);
                    SwingUtilities.invokeLater(this);
                  }
                  else if (!enabled) {
                    c.setEnabled(false);
                    runnables.remove(this);
                  }
                  else if (count < 5) {
                    TabbedPane tabbedPaneAncestor = getTabbedPaneAncestor(c);
                    if (tabbedPaneAncestor != null) {
                      runnables.remove(this);
                      if (!tabPanes.containsKey(tabbedPaneAncestor)) {
                        tabPanes.put(tabbedPaneAncestor, savedAC);
                      }
                    }
                    else {
                      SwingUtilities.invokeLater(this);
                    }
                  }
                  else {
                    runnables.remove(this);
                  }
                  if (runnables.isEmpty()) {
                    LinkedHashMap tp = new LinkedHashMap(tabPanes);
                    tabPanes.clear();
                    for (Iterator iterator = tp.keySet().iterator(); iterator.hasNext();) {
                      final TabbedPane tabPane = (TabbedPane) iterator.next();
                      final AddConstraints ac = (AddConstraints) tp.get(tabPane);
                      if (tabPane.getTabCount() > 0) {
                        if (DEBUG > 0)
                          System.err.println(tabPane.getTitleAt(0));
                        tabPane.setSelectedIndex(0);
//                        SwingUtilities.invokeLater(new Runnable() {
//                          public void run() {
//                            restoreSplitPaneParentDivider(ac.weight, ac.width,
//                                ac.height, tabPane.getComponentAt(0));
//                          }
//                        });
                      }
                    }
                    for (Iterator it = dividerConstraints.values().iterator(); it.hasNext();) {
                      AddConstraints ac = (AddConstraints) it.next();
                      restoreSplitPaneParentDivider(ac.weight, ac.width, ac.height, ac.component, false);
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                      public void run() {
                        setApplyingConstraints(false);
                      }
                    });
                  }
                }
              };
              runnables.add(runnable);
            }
          }
        }
        LinkedHashSet runners = new LinkedHashSet(runnables);
        HashSet ran = new HashSet();
        for (Iterator iter = runners.iterator(); iter.hasNext();) {
          Runnable runnable = (Runnable) iter.next();
          iter.remove();
          if (runnables.contains(runnable) && ! ran.contains(runnable)) {
            SwingUtilities.invokeLater(runnable);
          }
          ran.add(runnable);
          if (runnables.size() > runners.size() + ran.size()) {
            runners = new LinkedHashSet(runnables);
            runners.removeAll(ran);
            iter = runners.iterator();
          }
        }
      }
    }

    protected void restoreSplitPaneParentDivider(double weight, double width,
        double height, Component component, boolean doAncestors) {
      SplitPane sp = getSplitPaneAncestor(component);
      if (sp != null) {
        Dimension d = sp.getSize();
        int ds = sp.getDividerSize();
        double dividerLocation = weight;
        if (width >= 0 && width <= 1) {
          SplitPane sph = sp;
          while (sph != null
              && sph.getOrientation() != JSplitPane.HORIZONTAL_SPLIT && doAncestors) {
            Container p = sph.getParent();
            if (p != null && p instanceof SplitPane) {
              sph = (SplitPane) p;
            }
            else
              sph = null;
          }
          if (sph != null && sph.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
            Component left = sph.getLeftComponent();
            Component right = sph.getRightComponent();
            if ((width > 0.0 || left == null || left instanceof FillerComponent)
                && (width < 1.0 || right == null || right instanceof FillerComponent))
              sph.setDividerLocation(width);
            dividerLocation = -1;
          }
        }
        if (height >= 0 && height <= 1) {
          SplitPane spv = sp;
          while (spv != null
              && spv.getOrientation() != JSplitPane.VERTICAL_SPLIT && doAncestors) {
            Container p = spv.getParent();
            if (p != null && p instanceof SplitPane) {
              spv = (SplitPane) p;
            }
            else
              spv = null;
          }
          if (spv != null && spv.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
            Component top = spv.getTopComponent();
            Component bottom = spv.getBottomComponent();
            if ((height > 0.0 || top == null || top instanceof FillerComponent)
                && (height < 1.0 || bottom == null || bottom instanceof FillerComponent))
              spv.setDividerLocation(height);
            dividerLocation = -1;
          }
          // if (spv != null) {
          // spv.setDividerLocation(height);
          // dividerLocation = -1;
          // }
        }
        if (dividerLocation > 1) {
          sp.setDividerLocation((int) dividerLocation);
        }
        else if (dividerLocation > 0) {
          sp.setDividerLocation(dividerLocation);
        }
      }
    }

    public static class ApplyingConstraintsEvent extends EventObject {
      public ApplyingConstraintsEvent(DraggableTabbedPane source) {
        super(source);
      }
    }

    public interface ApplyingConstraintsListener extends EventListener {
      public void applyingConstraintsComplete(ApplyingConstraintsEvent e);
    }

    EventListenerList applyingConstraintsListeners = new EventListenerList();
    
    public void addApplyingConstraintsListener(ApplyingConstraintsListener l) {
      applyingConstraintsListeners.add(ApplyingConstraintsListener.class, l);
    }
    public void removeApplyingConstraintsListener(ApplyingConstraintsListener l) {
      applyingConstraintsListeners.remove(ApplyingConstraintsListener.class, l);
    }
    // Notify all listeners that have registered interest for
    // notification on this event type.  The event instance 
    // is lazily created using the parameters passed into 
    // the fire method.
    protected void fireApplyingConstraintsComplete() {
      // Guaranteed to return a non-null array
      Object[] listeners = applyingConstraintsListeners.getListenerList();
      // Process the listeners last to first, notifying
      // those that are interested in this event
      ApplyingConstraintsEvent event = null;
      for (int i = listeners.length-2; i>=0; i-=2) {
        if (listeners[i]==ApplyingConstraintsListener.class) {
          // Lazily create the event:
          if (event == null)
            event = new ApplyingConstraintsEvent(this);
          ((ApplyingConstraintsListener)listeners[i+1]).applyingConstraintsComplete(event);
        }
      }
    }    

    public boolean isApplyingConstraints() {
      return applyingConstraints;
    }

    protected void setApplyingConstraints(boolean applyingConstraints) {
      boolean fire = ! applyingConstraints && this.applyingConstraints;
//      new Exception("Fire: "+fire).printStackTrace(System.out);
      this.applyingConstraints = applyingConstraints;
      if (fire) {
        fireApplyingConstraintsComplete();
      }
    }

    protected void setComponentsMenu(JMenu componentsMenu) {
      this.componentsMenu = componentsMenu;
    }

    protected JMenu getComponentsMenu() {
      if (componentsMenu == null) {
        componentsMenu = new JMenu("Views");
        componentsMenu.setMnemonic('V');
      }
      return componentsMenu;
    }

    private String formatCollectionString(Collection collection) {
      return collection.toString().replaceAll(", ", ",\n\t").replaceFirst("\\[", "[\n\t").replaceAll("\\]$", "\n]");
    }

}
