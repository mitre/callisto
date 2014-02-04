
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
 * Created on Dec 15, 2004
 */
package org.mitre.jawb.swing.drag;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import org.mitre.jawb.swing.drag.DraggableTabbedPane.DraggableTabbedPaneButtonUI;

/**
 * TODO Describe type
 * 
 * @author Galen B. Williamson
 * @version Dec 15, 2004
 */
public class BasicDraggableSplitPaneUI extends BasicSplitPaneUI {

    private final static boolean SHOW_MIN_MAX_BUTTONS = false;
    
    public final static int LEFT_MIN = DraggableTabbedPane.SplitPane.LEFT_MIN; 
    public final static int LEFT_MAX = DraggableTabbedPane.SplitPane.LEFT_MAX; 
    public final static int RIGHT_MIN = DraggableTabbedPane.SplitPane.RIGHT_MIN;
    public final static int RIGHT_MAX = DraggableTabbedPane.SplitPane.RIGHT_MAX;
    public final static int MIN_MAX_NONE = DraggableTabbedPane.SplitPane.MIN_MAX_NONE;
    
    public final static int SPLIT_PANE_DIVIDER_SIZE = 4;

    public class BasicDraggableVerticalLayoutManager
        extends BasicDraggableHorizontalLayoutManager
    {
        public BasicDraggableVerticalLayoutManager(BasicSplitPaneUI.BasicHorizontalLayoutManager parent) {
            super(parent, 1);
        }
    }
    
    public class BasicDraggableHorizontalLayoutManager
        implements LayoutManager2
    {
        protected int axis;
        protected BasicHorizontalLayoutManager parent;
        public BasicDraggableHorizontalLayoutManager(BasicSplitPaneUI.BasicHorizontalLayoutManager parent) {
            this(parent, 0);
        }
        public BasicDraggableHorizontalLayoutManager(BasicSplitPaneUI.BasicHorizontalLayoutManager parent, int axis) {
            this.axis = axis;
            this.parent = parent;
            
        }
        public float getLayoutAlignmentX(Container target) {
            return parent.getLayoutAlignmentX(target);
        }

        public float getLayoutAlignmentY(Container target) {
            return parent.getLayoutAlignmentY(target);
        }

        public void invalidateLayout(Container target) {
            parent.invalidateLayout(target);
        }

        public Dimension maximumLayoutSize(Container target) {
            return parent.maximumLayoutSize(target);
        }

        public void addLayoutComponent(Component comp, Object constraints) {
            parent.addLayoutComponent(comp, constraints);
        }

        public void removeLayoutComponent(Component comp) {
            parent.removeLayoutComponent(comp);
        }

        public void layoutContainer(Container parent) {
            // TODO only if neither side is minimized
            this.parent.layoutContainer(parent);
        }

        public void addLayoutComponent(String name, Component comp) {
            parent.addLayoutComponent(name, comp);
        }

        public Dimension minimumLayoutSize(Container parent) {
            return this.parent.minimumLayoutSize(parent);
        }

        public Dimension preferredLayoutSize(Container parent) {
            return this.parent.preferredLayoutSize(parent);
        }
        
    }

    
    static final Cursor defaultCursor =
        Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    
    
    
    protected class BasicDraggableDivider extends BasicSplitPaneDivider {
        
        public boolean isEnabled() {
            return super.isEnabled() && ((draggableSplitPane.getMinMaxState() & (RIGHT_MIN | LEFT_MIN)) == 0);
        }
        
        protected class MyMouseHandler
            extends MouseHandler
        {
            Cursor savedCursor = null;
            public void mouseDragged(MouseEvent e) {
                if (isEnabled())
                    super.mouseDragged(e);
            }
            public void mouseMoved(MouseEvent e) {
                if (isEnabled()) {
                    if (savedCursor != null) {
                        setCursor(savedCursor);
                        savedCursor = null;
                    }
                    super.mouseMoved(e);
                }
                else {
                    if (savedCursor == null) {
                        savedCursor = getCursor();
                        setCursor(getParent().getCursor());
                    }
                }
            }
            public void mousePressed(MouseEvent e) {
                if (isEnabled())
                    super.mousePressed(e);
            }
            public void mouseReleased(MouseEvent e) {
                if (isEnabled())
                    super.mouseReleased(e);
            }
        }

        protected class LeftRightComponentChangeListener
            extends ContainerAdapter
        {
            public void componentAdded(ContainerEvent e) {
                if (splitPane != null && (splitPane instanceof DraggableTabbedPane.SplitPane)
                    && splitPane == e.getContainer()) {
                    Component added = e.getChild();
                    if (splitPane.getLeftComponent() == added) {
                        
                    }
                    else if (splitPane.getRightComponent() == added) {
                        
                    }
                    else return;
                    installMinMaxButtons();
                }
            }

            public void componentRemoved(ContainerEvent e) {
                if (splitPane != null && (splitPane instanceof DraggableTabbedPane.SplitPane)
                                                && splitPane == e.getContainer()) {
                    Component removed = e.getChild();
                    if (splitPane.getLeftComponent() == removed) {
                        resetLeftComponentMinimumSize();
                    }
                    else if (splitPane.getRightComponent() == removed) {
                        resetRightComponentMinimumSize();
                    }
                    uninstallMinMaxButtons();
                }
            }
            
        }
        private ContainerListener splitPaneContainerListener = new LeftRightComponentChangeListener();
        protected int minMaxState = MIN_MAX_NONE;
        
        protected Color fgColor;
        
        public BasicDraggableDivider(BasicSplitPaneUI ui) {
            super(ui);
            //setLayout(new DraggableDividerLayout());
            //setBorder(BorderFactory.createEmptyBorder());
            fgColor = UIManager.getColor("ScrollBar.thumbHighlight");
        }
        
        //      protected JButton createLeftOneTouchButton() {
//      return super.createLeftOneTouchButton();
//      }
//      protected JButton createRightOneTouchButton() {
//      return super.createRightOneTouchButton();
//      }
        protected MinMaxButton leftMinButton;
        protected MinMaxButton leftMaxButton;
        protected MinMaxButton rightMinButton;
        protected MinMaxButton rightMaxButton;
//      protected ButtonGroup minMaxButtonGroup;
        protected void installMinMaxButtons() {
            uninstallMinMaxButtons();            
//          if (minMaxButtonGroup != null) {
//          Enumeration e = minMaxButtonGroup.getElements();
//          while (e.hasMoreElements()) {
//          AbstractButton b = (AbstractButton) e.nextElement();
//          ItemListener[] listeners = b.getItemListeners();
//          for (int i = 0; i < listeners.length; i++)
//          b.removeItemListener(listeners[i]);
//          minMaxButtonGroup.remove(b);
//          }
//          }
//          else {
//          minMaxButtonGroup = new ButtonGroup();
//          }
//          minMaxButtonGroup.add(new JToggleButton(null, null, true));
            if (leftMinButton == null && leftMaxButton == null && splitPane instanceof DraggableTabbedPane.SplitPane) {
                final DraggableTabbedPane.SplitPane dragSplitPane = (DraggableTabbedPane.SplitPane) splitPane;
                if (dragSplitPane != null) { // && dragSplitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
                    Component leftComponent = dragSplitPane.getLeftComponent();
                    if (leftComponent != null && leftComponent instanceof DraggableTabbedPane.TabbedPane) {
                        final DraggableTabbedPane.TabbedPane left = (DraggableTabbedPane.TabbedPane) leftComponent;
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                int width = dragSplitPane.getMinimizedWidth(true);
                                int height = dragSplitPane.getMinimizedHeight(true);
                                setLeftComponentMinimumSize(width, height, left);
                            }
                        });
                        leftMinButton = createMinimizeButton(left);
                        if (leftMinButton != null) {
                            leftMinButton.addItemListener(new MinMaxItemListener(LEFT_MIN, this));
//                          minMaxButtonGroup.add(leftMinButton);
                        }
                        leftMaxButton = createMaximizeButton(left);
                        if (leftMaxButton != null) {
                            leftMaxButton.addItemListener(new MinMaxItemListener(LEFT_MAX, this));
//                          minMaxButtonGroup.add(leftMaxButton);
                        }
                    }
                    if (leftMinButton != null && leftMaxButton != null) {
                        dragSplitPane.setLeftMinButton(leftMinButton);
                        dragSplitPane.setLeftMaxButton(leftMaxButton);
                    }
                }
            }
            if (rightMinButton == null && rightMaxButton == null && splitPane instanceof DraggableTabbedPane.SplitPane) {
                final DraggableTabbedPane.SplitPane dragSplitPane = (DraggableTabbedPane.SplitPane) splitPane;
                if (dragSplitPane != null) { // && dragSplitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
                    Component rightComponent = dragSplitPane.getRightComponent();
                    if (rightComponent != null && rightComponent instanceof DraggableTabbedPane.TabbedPane) {
                        final DraggableTabbedPane.TabbedPane right = (DraggableTabbedPane.TabbedPane) rightComponent;
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                int width = dragSplitPane.getMinimizedWidth(false);
                                int height = dragSplitPane.getMinimizedHeight(false);
                                setRightComponentMinimumSize(width, height, right);
                            }
                        });
                        rightMinButton = createMinimizeButton(right);
                        if (rightMinButton != null) {
                            rightMinButton.addItemListener(new MinMaxItemListener(RIGHT_MIN, this));
//                          minMaxButtonGroup.add(rightMinButton);
                        }
                        rightMaxButton = createMaximizeButton(right);
                        if (rightMaxButton != null) {
                            rightMaxButton.addItemListener(new MinMaxItemListener(RIGHT_MAX, this));
//                          minMaxButtonGroup.add(rightMaxButton);
                        }
                    }
                    if (rightMinButton != null && rightMaxButton != null) {
                        // TODO this isn't happening when splitting the root pane
                        dragSplitPane.setRightMinButton(rightMinButton);
                        dragSplitPane.setRightMaxButton(rightMaxButton);
                    }
                }
            }
            invalidate();
        }
        
        protected void uninstallMinMaxButtons() {
            if (splitPane != null
                && (leftMinButton != null || leftMaxButton != null || rightMinButton != null || rightMaxButton != null)) {
                if (splitPane instanceof DraggableTabbedPane.SplitPane) {
                    DraggableTabbedPane.SplitPane dragSplitPane = (DraggableTabbedPane.SplitPane) splitPane;
                    dragSplitPane.setLeftMinButton(null);
                    dragSplitPane.setLeftMaxButton(null);
                    dragSplitPane.setRightMinButton(null);
                    dragSplitPane.setRightMaxButton(null);
                    leftMinButton = rightMinButton = leftMaxButton = rightMaxButton = null;
                    dragSplitPane.invalidate();
                }
            }
        }
        
        protected MinMaxButton createMinimizeButton(final DraggableTabbedPane.TabbedPane parent) {
            return new MinMaxButton(true, parent);
        }
        
        protected MinMaxButton createMaximizeButton(final DraggableTabbedPane.TabbedPane parent) {
            return new MinMaxButton(false, parent);
        }
        
//      public static final int DIVIDER_LINE_THICKNESS = 1;
        public void paint(Graphics g) {
            super.paint(g);
            
//          g.setColor(fgColor);
//          Dimension size = getSize();
//          int x = 0;
//          int y = 0;
//          int width = size.width;
//          int height = size.height;
//          if (size.width < size.height) {
//          x = Math.max(size.width / 2 - DIVIDER_LINE_THICKNESS, x);
//          width = DIVIDER_LINE_THICKNESS * 2;
//          //g.drawLine(size.width / 2, 0, size.width / 2, size.width);
//          }
//          else {
//          y = Math.max(size.height / 2 - DIVIDER_LINE_THICKNESS, y);
//          height = DIVIDER_LINE_THICKNESS * 2;
//          //g.drawLine(0, size.height / 2, size.width, size.height / 2);
//          }
//          g.fillRect(x, y, width, height);
            
        }
        
        public Border getBorder() {
            return null; //super.getBorder();
        }

        protected MyMouseHandler myMouseHandler;
        
        public void setBasicSplitPaneUI(BasicSplitPaneUI newUI) {
            boolean oldOneTouch = true;
            if (splitPane != null) {
                splitPane.removeContainerListener(splitPaneContainerListener);
                // deinitialize min/max buttons
                uninstallMinMaxButtons();
                oldOneTouch = splitPane.isOneTouchExpandable();
                if (oldOneTouch)
                    splitPane.setOneTouchExpandable(false);
                if (myMouseHandler != null) {
                    splitPane.removeMouseListener(myMouseHandler);
                    splitPane.removeMouseMotionListener(myMouseHandler);
                    removeMouseListener(myMouseHandler);
                    removeMouseMotionListener(myMouseHandler);
                }
            }
            super.setBasicSplitPaneUI(newUI);
            if (splitPane != null) {
                if (SHOW_MIN_MAX_BUTTONS) {
                    installMinMaxButtons();
                    splitPane.addContainerListener(splitPaneContainerListener);
                }
                if (oldOneTouch && !splitPane.isOneTouchExpandable()) {
                    splitPane.setOneTouchExpandable(oldOneTouch);
                }
                if (mouseHandler != null) {
                    splitPane.removeMouseListener(mouseHandler);
                    splitPane.removeMouseMotionListener(mouseHandler);
                    removeMouseListener(mouseHandler);
                    removeMouseMotionListener(mouseHandler);
                    mouseHandler = null;
                }
                if (myMouseHandler == null) myMouseHandler = new MyMouseHandler();
                splitPane.addMouseListener(myMouseHandler);
                splitPane.addMouseMotionListener(myMouseHandler);
                addMouseListener(myMouseHandler);
                addMouseMotionListener(myMouseHandler);
            }
        }

//        public void setEnabled(boolean b) {
//            boolean wasEnabled = super.isEnabled();
//            super.setEnabled(b);
//            if (wasEnabled != b && splitPane != null && mouseHandler != null) {
//                if (b) {
//                    splitPane.addMouseListener(mouseHandler);
//                    splitPane.addMouseMotionListener(mouseHandler);
//                    addMouseListener(mouseHandler);
//                    addMouseMotionListener(mouseHandler);
//                }
//                else {
//                    splitPane.removeMouseListener(mouseHandler);
//                    splitPane.removeMouseMotionListener(mouseHandler);
//                    removeMouseListener(mouseHandler);
//                    removeMouseMotionListener(mouseHandler);
//                }
//            }
//        }
    }
    
    private class MinMaxActionHandler implements ActionListener {
        private int buttonRole;
        
        MinMaxActionHandler(int buttonRole) {
            this.buttonRole = buttonRole;
        }
        
        public void actionPerformed(ActionEvent e) {
            if (draggableSplitPane == null)
                return;
            AbstractButton button = (AbstractButton) e.getSource();
            ButtonModel model = button.getModel();
            boolean selected = model.isSelected();
            int newState = buttonRole;
            switch (buttonRole) {
            case LEFT_MIN:
                if (selected) {
                    draggableSplitPane.minimizeLeft();
                }
                else
                    draggableSplitPane.restoreLeft();
                break;
            case LEFT_MAX:
                if (selected) {
                    draggableSplitPane.maximizeLeft();
                }
                else
                    draggableSplitPane.restoreLeft();
                break;
            case RIGHT_MIN:
                if (selected) {
                    draggableSplitPane.minimizeRight();
                }
                else
                    draggableSplitPane.restoreRight();
                break;
            case RIGHT_MAX:
                if (selected) {
                    draggableSplitPane.maximizeRight();
                }
                else
                    draggableSplitPane.restoreRight();
                break;
            }
        }
    }

    public class MinMaxButton extends JToggleButton implements UIResource
    {
        public class MinMaxButtonModel
            extends ToggleButtonModel
        {
            public boolean isSelected() {
                if (draggableSplitPane != null) {
                    boolean isLeft = draggableSplitPane.getLeftComponent() == parent;
                    int state = draggableSplitPane.getMinMaxState();
                    if (minimizer) {
                        if (isLeft)
                            return (state & LEFT_MIN) != 0;
                        else
                            return (state & RIGHT_MIN) != 0;
                    }
                    else {
                        if (isLeft)
                            return (state & LEFT_MAX) != 0;
                        else
                            return (state & RIGHT_MAX) != 0;
                    }
                }
                return false;
            }
        }
    
        boolean minimizer;
        Color selColor = null;
        Insets insets = new Insets(3, 3, 3, 3);
        DraggableTabbedPane.TabbedPane parent;
        
        public MinMaxButton(boolean minimizer, DraggableTabbedPane.TabbedPane parent) {
            super();
            this.minimizer = minimizer;
            this.parent = parent;
            setCursor(defaultCursor);
            setFocusPainted(false);
            setBorderPainted(false);
            setRequestFocusEnabled(false);
            setRolloverEnabled(true);
            setUI(new DraggableTabbedPaneButtonUI(parent));
        }
        public void setBorder(Border b) {
        }
        public Insets getInsets(Insets i) {
            if (i == null)
                i = new Insets(0, 0, 0, 0);
            i.top = insets.top;
            i.left = insets.left;
            i.bottom = insets.bottom;
            i.right = insets.right;
            return i;
        }
        public Insets getInsets() {
            return new Insets(insets.top, insets.left, insets.bottom, insets.right);
        }
        public void paint(Graphics g) {
            ButtonModel model = getModel();
            if (selColor == null) {
                selColor = Color.white;
            }
            Rectangle bounds = getBounds();
            int height = bounds.height - insets.top - insets.bottom;
            int width = bounds.width - insets.right - insets.left;
            int x = bounds.x + insets.left;
            int y = bounds.y + insets.top;
            // Fill the background first ...
            g.setColor(this.getBackground());
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            if (model.isRollover()) {
                g.setColor(selColor);
                Dimension corner = new Dimension(bounds.width / 3, bounds.height / 3 ); //bounds.width / 3, bounds.height / 3);
                g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, corner.width, corner.height);
                g.setColor(Color.black);
                g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, corner.width, corner.height);
            }
            if (! model.isSelected()) {
                if (minimizer) {
                    int h = height / 5 + 2; // 1;
                    // ... then draw the little minimize tab
                    g.setColor(selColor);
                    g.fillRect(x, y, width, h);
                    g.setColor(Color.black);
                    g.drawRect(x, y, width, h);
                }
                else {
                    // ... then draw the maximize button
                    g.setColor(selColor);
                    g.fillRect(x, y, width, height);
                    g.setColor(Color.black);
                    g.drawRect(x, y, width, height);
                    int lineY = 3; //height / 5; //Math.max(tabInsets.top, 2) + 3;
                    g.drawLine(x, y + lineY, x + width, y + lineY);
                }
            }
            else {
                // ... else draw the restore button
                Dimension boxSize = new Dimension(width / 2, height / 2);
                Dimension boxOffset = new Dimension(Math.max(boxSize.width / 2, 3), Math.max(boxSize.width / 2, 3));
                // draw back box up and to right
                g.setColor(selColor);
                g.fillRect(x + boxSize.width, y, boxSize.width, boxSize.height);
                g.setColor(Color.black);
                g.drawRect(x + boxSize.width, y, boxSize.width, boxSize.height);
                g.fillRect(x + boxSize.width,
                    y + 1,
                    boxSize.width,
                    boxOffset.height - 2);
                // draw front box down and to left
                g.setColor(selColor);
                g.fillRect(x + boxSize.width - boxOffset.width,
                    y + boxOffset.height,
                    boxSize.width, boxSize.height);
                g.setColor(Color.black);
                g.drawRect(x + boxSize.width - boxOffset.width,
                    y + boxOffset.height,
                    boxSize.width, boxSize.height);
                g.fillRect(x + boxSize.width - boxOffset.width,
                    y + 1 + boxOffset.height,
                    boxSize.width,
                    boxOffset.height - 2);
            }
        }
        // Don't want the button to participate in focus traversable.
        public boolean isFocusTraversable() {
            return false;
        }
    }

    private class MinMaxItemListener implements ItemListener {
                private int buttonRole;
                private BasicDraggableDivider divider;
                MinMaxItemListener(int buttonRole, BasicDraggableDivider divider) {
                    this.buttonRole = buttonRole;
                    this.divider = divider;
                }
                public void itemStateChanged(ItemEvent e) {
                    if (draggableSplitPane == null)
                        return;
                    MinMaxButton button = (MinMaxButton) e.getSource();
                    boolean selected = e.getStateChange() == ItemEvent.SELECTED;
                    if (! selected && divider.minMaxState != buttonRole) {
                        return;
                    }
                    if (button.parent == null)
                        return;
                    Container c = button.parent.getParent();
                    DraggableTabbedPane.SplitPane draggableSplitPane = (DraggableTabbedPane.SplitPane) c;
    //                while (c != null && (c instanceof DraggableTabbedPane.SplitPane)) {
    //                    draggableSplitPane = (DraggableTabbedPane.SplitPane) c;
    //                    if (draggableSplitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT)
    //                        break;
    //                    c = c.getParent();
    //                }
                    if (c == null || ! (c instanceof DraggableTabbedPane.SplitPane))
                        return;
                    ButtonModel model = button.getModel();
                    model.setRollover(false);
                    divider.minMaxState = selected ? buttonRole : MIN_MAX_NONE;
                    switch (buttonRole) {
                    case LEFT_MIN:
                        if (selected) {
                            draggableSplitPane.minimizeLeft();
                        }
                        else
                            draggableSplitPane.restoreLeft();
                        break;
                    case LEFT_MAX:
                        if (selected) {
                            draggableSplitPane.maximizeLeft();
                        }
                        else
                            draggableSplitPane.restoreLeft();
                        break;
                    case RIGHT_MIN:
                        if (selected) {
                            draggableSplitPane.minimizeRight();
                        }
                        else
                            draggableSplitPane.restoreRight();
                        break;
                    case RIGHT_MAX:
                        if (selected) {
                            draggableSplitPane.maximizeRight();
                        }
                        else
                            draggableSplitPane.restoreRight();
                        break;
                    }
                }
            }


    protected DraggableTabbedPane rootPane;
    protected DraggableTabbedPane.SplitPane draggableSplitPane;
    
    public BasicDraggableSplitPaneUI(DraggableTabbedPane rootPane) {
        super();
        this.rootPane = rootPane;
    }
    
    public BasicSplitPaneDivider createDefaultDivider() {
        return new BasicDraggableDivider(this);
    }
    protected void installDefaults() {
        super.installDefaults();
        splitPane.setDividerSize(SPLIT_PANE_DIVIDER_SIZE);
        
        divider.setDividerSize(splitPane.getDividerSize());
        dividerSize = divider.getDividerSize();
        
        splitPane.setOneTouchExpandable(false);
        splitPane.setResizeWeight(0.5);
    }
    public void installUI(JComponent c) {
        super.installUI(c);
        this.draggableSplitPane = (DraggableTabbedPane.SplitPane) splitPane;
    }
    
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        if (divider != null)
            ((BasicDraggableDivider) divider).uninstallMinMaxButtons();
        this.draggableSplitPane = null;
    }
    
    double savedLeftResizeWeight = -1;
    double savedRightResizeWeight = -1;
    Dimension savedLeftSize = null;
    Dimension savedLeftMinimumSize = null;
    JComponent savedLeftComponent = null;
    Dimension savedRightSize = null;
    Dimension savedRightMinimumSize = null;
    JComponent savedRightComponent = null;
    /*
     * Vertical split minimize:
     * - if left minimize and left child is split pane, minimize left child's left child
     * - if left minimize and right is minimized, do what?
     */
    public void minimizeLeft() {
        int minMaxState = draggableSplitPane.getMinMaxState();
        if ((minMaxState & LEFT_MIN) != 0) {
            restoreLeft();
            return;
        }
        // if right pane is minimized...
        if ((minMaxState & RIGHT_MIN) != 0) {
            Container p = draggableSplitPane.getParent();
            // ...and if my parent is a split pane, I've gotta tell it to minimize the pane containing me
            if (p instanceof DraggableTabbedPane.SplitPane) {
                DraggableTabbedPane.SplitPane parentSplitPane = (DraggableTabbedPane.SplitPane) p;
                if (draggableSplitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
                    if (parentSplitPane.getLeftComponent() == draggableSplitPane) {
                        parentSplitPane.minimizeLeft();
                        draggableSplitPane.setLeftMinimized(true);
                    }
                    else if (parentSplitPane.getRightComponent() == draggableSplitPane) {
                        parentSplitPane.minimizeRight();
                        draggableSplitPane.setLeftMinimized(true);
                    }
                    int currentLoc = getDividerLocation(splitPane);
                    Dimension size = splitPane.getLeftComponent().getSize();
//                  int minimalHeight = size.height = draggableSplitPane.getMinimizedHeight(true);
//                  Insets insets = splitPane.getInsets();
//                  newLoc = insets.top + minimalHeight;
                    int newLoc = getLeftMinimizedDividerLocation(currentLoc, size);
//                    Insets insets = splitPane.getInsets();
//                    int newLoc = currentLoc;
//                    Dimension size = splitPane.getLeftComponent().getSize();
//                    int minimalHeight = size.height = draggableSplitPane.getMinimizedHeight(true);
//                    newLoc = insets.top + minimalHeight;
                    if (currentLoc != newLoc) {
                        draggableSplitPane.setLeftMinimized(true);
                        minimizeLeftDividerLocation(currentLoc, newLoc, size.width, size.height);
                    }
                }
//                else {
//                    draggableSplitPane.clearMinMaxSelection(DraggableTabbedPane.SplitPane.LEFT_MIN);
//                }
            }
            return;
        }
        /*else*/ {
            int currentLoc = getDividerLocation(splitPane);
            Dimension size = splitPane.getLeftComponent().getSize();
            int newLoc = getLeftMinimizedDividerLocation(currentLoc, size);
            if (currentLoc != newLoc) {
                draggableSplitPane.setLeftMinimized(true);
                minimizeLeftDividerLocation(currentLoc, newLoc, size.width, size.height);
//              if (draggableSplitPane.getLastDividerMinMaxLocation() != -1)
                draggableSplitPane.setLastDividerMinMaxLocation(currentLoc);
            }
            draggableSplitPane.clearMinMaxSelection(DraggableTabbedPane.SplitPane.LEFT_MAX);
        }
    }

    private int getLeftMinimizedDividerLocation(int newLoc, Dimension size) {
        Insets insets = splitPane.getInsets();
        if (splitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
            int minimalHeight = size.height = draggableSplitPane.getMinimizedHeight(true);
            newLoc = insets.top + minimalHeight;
//            if (currentLoc != newLoc) {
//                draggableSplitPane.setMinMaxState(LEFT_MIN);
//            }
        }
        else {
            // TODO horizontal split pane minimize left
        }
        return newLoc;
    }
    
    Cursor savedDividerCursor = null;
    
    private void minimizeLeftDividerLocation(int currentLoc, int newLoc, int width, int height) {
        splitPane.setDividerLocation(newLoc);
        splitPane.setLastDividerLocation(currentLoc);
//        disableDivider();
        Component c = splitPane.getLeftComponent();
        if (c != null && c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            setLeftComponentMinimumSize(width, height, jc);
            Dimension s = jc.getSize();
            jc.setSize(jc.getMinimumSize());
            savedLeftSize = s;
        }
        if (savedLeftResizeWeight < 0) {
            double savedResizeWeight = splitPane.getResizeWeight();
            if (savedRightResizeWeight < 0) {
                savedLeftResizeWeight = savedResizeWeight;
                splitPane.setResizeWeight(0.0D);
            }
            else {
                // -2 means that right was minimized already and is holding saved weight
                savedLeftResizeWeight = -2;
                splitPane.setResizeWeight(0.5D);
            }
            // TODO handle case wherein both left and right are minimized
        }
    }

//    protected void disableDivider() {
//        divider.setEnabled(false);
//        if (savedDividerCursor == null) {
//            savedDividerCursor = divider.getCursor();
//            divider.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//        }
//    }

    public void restoreLeft() {
        // TODO: restore parent left or right if it was minimized by me
        int minMaxState = draggableSplitPane.getMinMaxState();
        if ((minMaxState & LEFT_MAX) != 0) {
        }
        else if ((minMaxState & LEFT_MIN) != 0) {
            if ((minMaxState & RIGHT_MIN) != 0) {
                Container p = draggableSplitPane.getParent();
                if (p instanceof DraggableTabbedPane.SplitPane) {
                    DraggableTabbedPane.SplitPane parentSplitPane = (DraggableTabbedPane.SplitPane) p;
                    if (draggableSplitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
                        if (parentSplitPane.getLeftComponent() == draggableSplitPane) {
                            parentSplitPane.restoreLeft();
                            draggableSplitPane.setLeftMinimized(false);
                        }
                        else if (parentSplitPane.getRightComponent() == draggableSplitPane) {
                            parentSplitPane.restoreRight();
                            draggableSplitPane.setLeftMinimized(false);
                        }
                    }
                    else {
                        draggableSplitPane.clearMinMaxSelection(DraggableTabbedPane.SplitPane.LEFT_MIN);
                    }
                }
            }
            // TODO: this may need to be smart about right's min state...
            int lastLoc = draggableSplitPane.getLastDividerMinMaxLocation();
            int currentLoc = getDividerLocation(splitPane);
            int newLoc = lastLoc;
            if (newLoc != -1) {
                Dimension leftSize = splitPane.getLeftComponent().getSize();
                Dimension rightSize = splitPane.getRightComponent().getSize();
                int minDividerLocation = getLeftMinimizedDividerLocation(currentLoc, leftSize);
                int maxDividerLocation = getRightMinimizedDividerLocation(currentLoc, rightSize);
                if (newLoc < minDividerLocation) newLoc = minDividerLocation;
                else if (newLoc > maxDividerLocation) newLoc = maxDividerLocation;
            }
            
            if (currentLoc != newLoc && newLoc != -1) {
                splitPane.setDividerLocation(newLoc);
                splitPane.setLastDividerLocation(currentLoc);
                restoreLeftResizeWeight();
//              draggableSplitPane.setLastDividerMinMaxLocation(-1);
            }
            draggableSplitPane.setLeftMinimized(false);
        }
        draggableSplitPane.clearMinMaxSelection(DraggableTabbedPane.SplitPane.LEFT_MIN);
        draggableSplitPane.clearMinMaxSelection(DraggableTabbedPane.SplitPane.LEFT_MAX);
    }
    
    private void restoreLeftResizeWeight() {
        if (savedLeftResizeWeight >= 0) {
            if (savedRightResizeWeight == -2) {
                savedRightResizeWeight = savedLeftResizeWeight;
                splitPane.setResizeWeight(1.0D);
            }
            else { // if (savedRightResizeWeight == -1) {
                splitPane.setResizeWeight(savedLeftResizeWeight);
//                enableDivider();
            }
        }
        else if (savedLeftResizeWeight == -2) {
            splitPane.setResizeWeight(1.0D);
        }
        savedLeftResizeWeight = -1;
    }

//    protected void enableDivider() {
////        divider.setEnabled(true);
//        if (savedDividerCursor != null) {
//            divider.setCursor(savedDividerCursor);
//            savedDividerCursor = null;
//        }
//    }
    
    private void restoreRightResizeWeight() {
        if (savedRightResizeWeight >= 0) {
            if (savedLeftResizeWeight == -2) {
                savedLeftResizeWeight = savedRightResizeWeight;
                splitPane.setResizeWeight(0.0D);
            }
            else { // if (savedLeftResizeWeight == -1) {
                splitPane.setResizeWeight(savedRightResizeWeight);
//                enableDivider();
            }
        }
        else if (savedRightResizeWeight == -2) {
            splitPane.setResizeWeight(0.0D);
        }
        savedRightResizeWeight = -1;
    }
    
    protected void setLeftComponentMinimumSize(int width, int height, JComponent component) {
        resetLeftComponentMinimumSize();
        Dimension d = component.getMinimumSize();
        Dimension m = new Dimension(width, height);
        component.setMinimumSize(m);
        savedLeftMinimumSize = d;
        savedLeftComponent = component;
    }
    protected void resetLeftComponentMinimumSize() {
        if (savedLeftComponent != null && savedLeftMinimumSize != null)
            savedLeftComponent.setMinimumSize(savedLeftMinimumSize);
        savedLeftComponent = null;
        savedLeftMinimumSize = null;
    }
    
    public void maximizeLeft() {
        int minMaxState = draggableSplitPane.getMinMaxState();
        if ((minMaxState & LEFT_MIN) != 0) {
            restoreLeft();
        }
    }

    //
    // RIGHT
    //
    
    private void minimizeRightDividerLocation(int currentLoc, int newLoc, int width, int height) {
        splitPane.setDividerLocation(newLoc);
        splitPane.setLastDividerLocation(currentLoc);
//        disableDivider();
        Component c = splitPane.getRightComponent();
        if (c != null && c instanceof JComponent) {
            JComponent jc = (JComponent) c;

            setRightComponentMinimumSize(width, height, jc);
            Dimension s = jc.getSize();
            jc.setSize(jc.getMinimumSize());
            savedRightSize = s;
        }
        if (savedRightResizeWeight < 0) {
            double savedResizeWeight = splitPane.getResizeWeight();
            if (savedRightResizeWeight < 0) {
                savedRightResizeWeight = savedResizeWeight;
                splitPane.setResizeWeight(1.0D);
            }
            // TODO handle case wherein both left and right are minimized
        }
    }

    protected void setRightComponentMinimumSize(int width, int height, JComponent component) {
        resetRightComponentMinimumSize();
        Dimension d = component.getMinimumSize();
        Dimension m = new Dimension(width, height);
        component.setMinimumSize(m);
        savedRightMinimumSize = d;
        savedRightComponent = component;
    }

    protected void resetRightComponentMinimumSize() {
        if (savedRightComponent != null && savedRightMinimumSize != null)
            savedRightComponent.setMinimumSize(savedRightMinimumSize);
        savedRightComponent = null;
        savedRightMinimumSize = null;
    }

    public void minimizeRight() {
        int minMaxState = draggableSplitPane.getMinMaxState();
        if ((minMaxState & RIGHT_MIN) != 0) {
            restoreRight();
            return;
        }
        if ((minMaxState & LEFT_MIN) != 0) {
            Container p = draggableSplitPane.getParent();
            if (p instanceof DraggableTabbedPane.SplitPane) {
                DraggableTabbedPane.SplitPane parentSplitPane = (DraggableTabbedPane.SplitPane) p;
                if (draggableSplitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
                    if (parentSplitPane.getLeftComponent() == draggableSplitPane) {
                        parentSplitPane.minimizeLeft();
                        draggableSplitPane.setRightMinimized(true);
                    }
                    else if (parentSplitPane.getRightComponent() == draggableSplitPane) {
                        parentSplitPane.minimizeRight();
                        draggableSplitPane.setRightMinimized(true);
                    }
                    int currentLoc = getDividerLocation(splitPane);
                    Dimension size = splitPane.getRightComponent().getSize();
//                    int minimalHeight = size.height = draggableSplitPane.getMinimizedHeight(true);
//                    Insets insets = splitPane.getInsets();
//                    newLoc = insets.top + minimalHeight;
                    int newLoc = getRightMinimizedDividerLocation(currentLoc, size);
                    if (currentLoc != newLoc) {
                        draggableSplitPane.setRightMinimized(true);
                        minimizeRightDividerLocation(currentLoc, newLoc, size.width, size.height);
                    }
                }
//                else {
//                    draggableSplitPane.clearMinMaxSelection(DraggableTabbedPane.SplitPane.RIGHT_MIN);
//                }
            }
            return;
        }
        /*else*/ {
//            int currentLoc = getDividerLocation(splitPane);
//            int newLoc = currentLoc;
//            Dimension size = splitPane.getRightComponent().getSize();
//            newLoc = getRightMinimizedDividerLocation(newLoc, size);
            int currentLoc = getDividerLocation(splitPane);
            Dimension size = splitPane.getRightComponent().getSize();
            int newLoc = getRightMinimizedDividerLocation(currentLoc, size);
            if (currentLoc != newLoc) {
                draggableSplitPane.setRightMinimized(true);
                minimizeRightDividerLocation(currentLoc, newLoc, size.width, size.height);
//              if (draggableSplitPane.getLastDividerMinMaxLocation() != -1)
                draggableSplitPane.setLastDividerMinMaxLocation(currentLoc);
            }
            draggableSplitPane.clearMinMaxSelection(DraggableTabbedPane.SplitPane.RIGHT_MAX);
        }
    }

    private int getRightMinimizedDividerLocation(int newLoc, Dimension size) {
        Insets insets = splitPane.getInsets();
        if (splitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
            int minimalHeight = size.height = draggableSplitPane.getMinimizedHeight(false);
            newLoc = splitPane.getHeight() - divider.getHeight() - insets.bottom - minimalHeight;
//                if (currentLoc != newLoc) {
//                    draggableSplitPane.setMinMaxState(RIGHT_MIN);
//                }
        }
        else {
            // TODO horizontal split pane minimize right
        }
        return newLoc;
    }
    public void maximizeRight() {
        int minMaxState = draggableSplitPane.getMinMaxState();
        if ((minMaxState & RIGHT_MIN) != 0) {
            restoreRight();
        }
    }
    public void restoreRight() {
        int minMaxState = draggableSplitPane.getMinMaxState();
        if ((minMaxState & RIGHT_MAX) != 0) {
        }
        else if ((minMaxState & RIGHT_MIN) != 0) {
            if ((minMaxState & LEFT_MIN) != 0) {
                Container p = draggableSplitPane.getParent();
                if (p instanceof DraggableTabbedPane.SplitPane) {
                    DraggableTabbedPane.SplitPane parentSplitPane = (DraggableTabbedPane.SplitPane) p;
                    if (draggableSplitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
                        if (parentSplitPane.getLeftComponent() == draggableSplitPane) {
                            parentSplitPane.restoreLeft();
                            draggableSplitPane.setRightMinimized(false);
                        }
                        else if (parentSplitPane.getRightComponent() == draggableSplitPane) {
                            parentSplitPane.restoreRight();
                            draggableSplitPane.setRightMinimized(false);
                        }
                    }
                    else {
                        draggableSplitPane.clearMinMaxSelection(DraggableTabbedPane.SplitPane.RIGHT_MIN);
                    }
                }
            }
//            int lastLoc = draggableSplitPane.getLastDividerMinMaxLocation();
//            int currentLoc = getDividerLocation(splitPane);
//            int newLoc = lastLoc;
            int lastLoc = draggableSplitPane.getLastDividerMinMaxLocation();
            int currentLoc = getDividerLocation(splitPane);
            int newLoc = lastLoc;
            if (newLoc != -1) {
                Dimension leftSize = splitPane.getLeftComponent().getSize();
                Dimension rightSize = splitPane.getRightComponent().getSize();
                int minDividerLocation = getLeftMinimizedDividerLocation(currentLoc, leftSize);
                int maxDividerLocation = getRightMinimizedDividerLocation(currentLoc, rightSize);
                if (newLoc < minDividerLocation) newLoc = minDividerLocation;
                else if (newLoc > maxDividerLocation) newLoc = maxDividerLocation;
            }
            if (currentLoc != newLoc && newLoc != -1) {
                splitPane.setDividerLocation(newLoc);
                splitPane.setLastDividerLocation(currentLoc);
                restoreRightResizeWeight();
//              draggableSplitPane.setLastDividerMinMaxLocation(-1);
            }
            draggableSplitPane.setRightMinimized(false);
        }
        draggableSplitPane.clearMinMaxSelection(DraggableTabbedPane.SplitPane.RIGHT_MIN);
        draggableSplitPane.clearMinMaxSelection(DraggableTabbedPane.SplitPane.RIGHT_MAX);
    }

    protected BasicDraggableHorizontalLayoutManager draggableLayoutManager;
    
    protected void resetLayoutManager() {
        super.resetLayoutManager();
        if (getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
            draggableLayoutManager = new BasicDraggableHorizontalLayoutManager(layoutManager);
        } else {
            draggableLayoutManager = new BasicDraggableVerticalLayoutManager(layoutManager);
        }
        splitPane.setLayout(draggableLayoutManager);
//        layoutManager.updateComponents();
        splitPane.revalidate();
        splitPane.repaint();
    }
    
    
}