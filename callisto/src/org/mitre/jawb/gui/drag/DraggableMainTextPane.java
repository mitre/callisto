
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
 * Created on Jan 7, 2005
 */
package org.mitre.jawb.gui.drag;

import gov.nist.atlas.type.AnnotationType;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.*;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.Position.Bias;
import javax.swing.text.html.HTML;

import org.mitre.jawb.DebugLevel;
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.AWBAnnotationImpl;
import org.mitre.jawb.atlas.NamedExtentRegions;
import org.mitre.jawb.atlas.TextExtentRegion;
import org.mitre.jawb.gui.AnnotationMouseListener;
import org.mitre.jawb.gui.JawbComponent;
import org.mitre.jawb.gui.JawbDocument;
import org.mitre.jawb.gui.MainTextPane;
import org.mitre.jawb.swing.AutoSelectCaret;
import org.mitre.jawb.swing.ItemExchanger;
import org.mitre.jawb.swing.AutoSelectCaret.Mode;
import org.mitre.jawb.swing.drag.DraggableTabbedPane;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;

/**
 * A wrapper around the {@link org.mitre.jawb.gui.MainTextPane MainTextPane}
 * class that puts the main text pane into the top left tab of a
 * {@link org.mitre.jawb.gui.drag.JawbDraggableTabbedPane JawbDraggableTabbedPane}.
 * It works just like the <code>MainTextPane</code> class, except that it also
 * has the tab-adding interface of <code>JawbDraggableTabbedPane</code>.
 * <p>
 * This class will not display the traditional "status bar" annotation
 * inspector. Instead, hovering over an annotation with the SHIFT key depressed
 * will pop up a floating annotation inspector window. This window shows all the
 * annotations present at the hovered-over text point in a structured
 * presentation. The type name of each annotation is a hyperlink, and clicking
 * one of these links will select the given annotation and close the inspector
 * window. The inspector window also closes if any of the following happens:
 * <ul>
 * <li>The mouse enters then exits the inspector window</li>
 * <li>The mouse moves a certain distance over the main text pane</li>
 * <li>The ESCAPE key is pressed</li>
 * <li>The close (X) button in the top right corner of the inspector window is
 * pressed</li>
 * </ul>
 * The inspector window has a thin drag bar at the top for moving it around and
 * it can be resized at any of its borders or corners. The arrow keys scroll one
 * line at a time (as opposed to moving an invisible cursor around the
 * inspector's text pane), and the page up and page down keys scroll one page at
 * a time.
 *
 * @author Galen B. Williamson
 * @version Jan 7, 2005
 */
public class DraggableMainTextPane extends MainTextPane {

  private static int DEBUG = DebugLevel.getDebugLevel(DraggableMainTextPane.class, 0);

  protected AnnotationInspectorMouseListener annotationInspectorMouseListener;

  protected final class ScrollSynchronizer implements ChangeListener {
    Runnable later = new Runnable() {
      public void run() {
        if (laters == 0) {
          System.out.println("No more laters!");
          return;
        }
        if (/*! getMainScrollPane().getViewport().isValid() ||*/ ! getMainTextPane().isShowing()) {
          if (laters-- > 0) {
//            System.out.println("Later: "+laters);
            SwingUtilities.invokeLater(later);
          }
          else {
            System.out.println(this+" sp not valid right now");
            laters = 0;
          }
        }
        else {
          laters = 0;
          TextExtentRegion extent = (TextExtentRegion) getTopVisibleAnnotation();
          String typeName = null;
          if (extent != null) {
            typeName = extent.getATLASType().getName() +"("+extent.getTextExtentStart()+","+extent.getTextExtentEnd()+")";
          }
          System.out.println("Top Visible Annotation: "+typeName);
        }
      }
    };
    int laters = 0;
    public void stateChanged(ChangeEvent e) {
      if (getMainScrollPane() != null && getMainScrollPane().getViewport() == e.getSource()) {
        if (laters <= 0) {
          SwingUtilities.invokeLater(later);
        }
        laters = 10;
      }
    }
  }

  protected class AnnotationSelectionOutliner
  extends JPanel
  {
    public void paintComponent(Graphics g) {
      Color c = g.getColor();
      if (offsetRect != null) {
        g.setColor(UIManager.getColor("TextPane.selectionBackground"));
        g.drawRect(offsetRect.x, offsetRect.y, offsetRect.width, offsetRect.height);
        g.setColor(c);
      }
    }
  }

  public class AnnotationInspectorMouseListener extends MouseInputAdapter {

    /**
     * The key mask to control when the annotation inspector should appear.
     */
    protected int onmask = 0
//    | MouseEvent.CTRL_DOWN_MASK
//    | MouseEvent.ALT_DOWN_MASK
      | MouseEvent.SHIFT_DOWN_MASK;

    /**
     * The key mask to control when the annotation inspector should <em>not</em>
     * appear, filters out other keys and all the mouse buttons.
     */
    protected int offmask = 0
      | MouseEvent.CTRL_DOWN_MASK
      | MouseEvent.ALT_DOWN_MASK
//    | MouseEvent.SHIFT_DOWN_MASK
      | MouseEvent.BUTTON1_DOWN_MASK
      | MouseEvent.BUTTON2_DOWN_MASK
      | MouseEvent.BUTTON3_DOWN_MASK;
    
    protected boolean shouldDisplayInspector(MouseEvent e) {
      int mods = e.getModifiersEx();
      if ((mods & (onmask | offmask)) == onmask) {
        return true;
      }
      return false;
    }

    protected boolean shouldHideInspector(MouseEvent e) {
      if (annotationInspector.isVisible() && lastInspectedPoint != null) {
        Point point = e.getPoint();
        double dist = point.distanceSq(lastInspectedPoint);
        Font f = getMainTextPane().getFont();
        Component source = (Component) e.getSource();
        FontMetrics fm = source.getFontMetrics(f);
        double w = fm.getMaxAdvance() / 2.0;
        double h = fm.getHeight();
        double tolerance = h * h + w * w;
        if (dist < tolerance)
          return false;
      }
      return true;
    }

    public void mouseMoved(MouseEvent e) {
      if (annotationInspector != null) {
        if (shouldDisplayInspector(e)) {
          annotationInspector.inspectAnnotations(e);
        }
        else if (shouldHideInspector(e)) {
          resetAnnotationInspector();
        }
      }
    }
    public void mouseDragged(MouseEvent e) {
      if (! shouldDisplayInspector(e) && annotationInspector != null) {
        resetAnnotationInspector();
      }
    }
    public void mousePressed(MouseEvent e) {
      if (! shouldDisplayInspector(e) && annotationInspector != null) {
        resetAnnotationInspector();
      }
    }
    public void mouseReleased(MouseEvent e) {
      if (! shouldDisplayInspector(e) && annotationInspector != null) {
        resetAnnotationInspector();
      }
    }
    public void mouseClicked(MouseEvent e) {
      if (! shouldDisplayInspector(e) && annotationInspector != null) {
        resetAnnotationInspector();
      }
    }
  }
  JComponent annotationSelectionOutliner = null;
  protected class AnnotationInspectorLayout implements LayoutManager, Serializable {
    protected AnnotationInspector annotationInspector;

    public Dimension preferredLayoutSize(Container target) {
      synchronized (target.getTreeLock()) {
        Component[] c = target.getComponents();
        for (int i = 0; i < c.length; i++) {
          if (c[i] == annotationInspector || c[i] instanceof AnnotationInspector) {
            annotationInspector = (AnnotationInspector) c[i];
          }
          else {
            Dimension size = c[i].getPreferredSize();
            Insets insets = target.getInsets();
            size.width += insets.left + insets.right;
            size.height += insets.top + insets.bottom;
            return size;
          }
        }
      }
      return new Dimension(0, 0);
    }

    public Dimension minimumLayoutSize(Container target) {
      return preferredLayoutSize(target);
    }

    Dimension scratchDimension = new Dimension(0, 0);
    public void layoutContainer(Container target) {
      synchronized (target.getTreeLock()) {
        Insets insets = target.getInsets();
        Rectangle targetBounds = target.getBounds();
        Component[] c = target.getComponents();
        int top = insets.top;
        int bottom = targetBounds.height - insets.bottom;
        int left = insets.left;
        int right = targetBounds.width - insets.right;
        int width = right - left;
        int height = bottom - top;
        for (int i = 0; i < c.length; i++) {
          try {
            if (c[i] == annotationInspector || c[i] instanceof AnnotationInspector) {
//            boolean ltr = target.getComponentOrientation().isLeftToRight();
              scratchDimension.width = (right - left) / 3;
              scratchDimension.height = (bottom - top) / 2;
              annotationInspector = (AnnotationInspector) c[i];
              annotationInspector.setPreferredSize(scratchDimension);
            }
            else if (c[i] == annotationSelectionOutliner) {
            }
            else {
              c[i].setBounds(left, top, width, height);
            }
          }
          catch( Exception e ) {
          }
        }
        if (annotationSelectionOutliner != null
            && getMainTextPane() != null) {
          Rectangle b = getMainTextPane().getBounds();
          boolean vis = //mainTextPane.isVisible() && mainTextPane.getParent() != null;
            getMainTextPane().isDisplayable() && getMainTextPane().isShowing();
          if (!vis)
            return;
          Point tPoint = target.getLocationOnScreen();
          Point mPoint = vis ? getMainTextPane().getLocationOnScreen()
              : getMainTextPane().getLocation();
          Insets mInsets = getMainTextPane().getInsets();
          b.x += mPoint.x - tPoint.x + mInsets.top;
          b.y += mPoint.y - tPoint.y + mInsets.left;
          annotationSelectionOutliner.setBounds(b);
        }
      }
    }


    public void addLayoutComponent(Component comp, Object constraints) {
      if (comp instanceof AnnotationInspector) {
        annotationInspector = (AnnotationInspector) comp;
      }
    }

    public void addLayoutComponent(String name, Component comp) {
      if (comp instanceof AnnotationInspector) {
        annotationInspector = (AnnotationInspector) comp;
      }
    }

    public void removeLayoutComponent(Component comp) {
      if (comp instanceof AnnotationInspector) {
        annotationInspector = null;
      }
    }
  }

  /** Listen for changes to the current document's properties */
  private PropertyChangeListener docPropListener = new DocPropertyListener();

  protected WeakHashMap textPanes; // new Vector();
//WeakHashMap jawbComponents; // new Vector();
  private JTextPane mainTextPane;
  protected JawbDraggableTabbedPane tabPane;
  protected JLayeredPane layerPane;
  protected JPanel mainTextPanePanel;
  Rectangle offsetRect = null;

  public Font getMainTextPaneFont() {
    return getMainTextPane().getFont();
  }

  public DraggableTabbedPane getTabPane() {
    return tabPane;
  }

  public Point getLastMouseLocation() {
    return lastMouseLocation.getLocation();
  }

  public Component getLastMouseComponent() {
    return lastMouseComponent;
  }

  private Point lastMouseLocation = new Point();
  private Component lastMouseComponent = null;

  private MouseInputListener lastMouseLocationListener = new MouseInputAdapter() {
    public void mouseMoved(MouseEvent e) {
      handleEvent(e);
    }

    public void mouseDragged(MouseEvent e) {
      handleEvent(e);
    }

    private void handleEvent(MouseEvent e) {
      Component c = (Component) e.getSource();
      lastMouseComponent = c;
      lastMouseLocation.x = e.getX();
      lastMouseLocation.y = e.getY();
    }
  };

  private AncestorListener ancestorMouseListenerUpdater = new AncestorListener() {
    private void handleEvent(AncestorEvent event) {
      JComponent component = event.getComponent();
      if (component instanceof AnnotationInspector)
        return;
      Container c = event.getAncestor();
      while (c != null && !(c instanceof Frame)
          && c != DraggableMainTextPane.this) {
        c = c.getParent();
      }
      if (c != DraggableMainTextPane.this) {
        c.removeMouseMotionListener(lastMouseLocationListener);
      }
      else {
        c.addMouseMotionListener(lastMouseLocationListener);
      }
    }

    public void ancestorAdded(AncestorEvent event) {
      handleEvent(event);
    }

    public void ancestorRemoved(AncestorEvent event) {
      handleEvent(event);
    }

    public void ancestorMoved(AncestorEvent event) {}
  };

  public DraggableMainTextPane(TaskToolKit kit) {
    this(kit, null, "&Main", null);
  }

  public DraggableMainTextPane(TaskToolKit kit, String namespace) {
    this(kit, namespace, "&Main", null);
  }
  
  public DraggableMainTextPane(TaskToolKit kit, String namespace, String mainTitle, String mainToolTip) {
    super(kit);
    layerPane = new JLayeredPane();
    layerPane.setLayout(new AnnotationInspectorLayout());
    tabPane = new JawbDraggableTabbedPane(kit, namespace);
    setBackground(Color.RED);
    tabPane.fillSplitConstraintPath(null, 1.0D);
    mainTextPanePanel = new JPanel(new BorderLayout());
    tabPane.addTab(mainTitle, null, mainTextPanePanel, mainToolTip, null, -1, 0, true);
    setAnnotationInspectorVisible(false);
  }

  public boolean clearTextSelection() {
    for (Iterator i = textPanes.keySet().iterator(); i.hasNext();) {
      Caret caret = ((JTextPane) i.next()).getCaret();
      int start = caret.getMark();
      int end = caret.getDot();

      if (start == end)
        return false;

      caret.setDot(end);
    }
    return true;
  }

  public JawbTextPane addTextPane(String title, Icon icon, String tip,
      String constraints) {
    final JawbTextPane tp = (JawbTextPane) addTextPane(new JawbTextPane(), title, icon, tip,
        constraints);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JawbDocument doc;
        if ((doc = getJawbDocument()) != null) {
          tp.setJawbDocument(doc);
        }
      }
    });
    return tp;
  }

  public JTextPane addTextPane(JTextPane textPane, String title, Icon icon, String tip, String constraints) {
    return addTextPane(textPane, title, icon, tip, constraints, -1);
  }
  public JTextPane addTextPane(JTextPane textPane, String title, Icon icon, String tip, String constraints, double weight) {
    if (textPanes == null)
      textPanes = new WeakHashMap();
    textPanes.put(textPane, null);
    JScrollPane sp = new JScrollPane(textPane);
    sp.setBorder(BorderFactory.createEmptyBorder());
    addComponent(sp, title, icon, tip, constraints, weight);
    textPane.addMouseMotionListener(lastMouseLocationListener);
    textPane.addAncestorListener(ancestorMouseListenerUpdater);
    return textPane;
  }

  public void fillSplitConstraintPath(String constraint, double weight) {
    tabPane.fillSplitConstraintPath(constraint, weight);
  }

  public void addJawbComponent(JawbComponent component, String title, String tip, String constraints, double weight) {
    addJawbComponent(component, title, null, tip, constraints, weight);
  }
  public void addJawbComponent(JawbComponent component, String title, String constraints) {
    addJawbComponent(component, title, null, null, constraints, -1);
  }
  public void addJawbComponent(JawbComponent component, String title, String constraints, double weight) {
    addJawbComponent(component, title, null, null, constraints, weight);
  }
  public void addJawbComponent(JawbComponent component, String title, String tip, String constraints) {
    addJawbComponent(component, title, null, tip, constraints, -1);
  }
  public void addJawbComponent(JawbComponent component, String title, Icon icon, String tip, String constraints) {
    addJawbComponent(component, title, icon, tip, constraints, -1);
  }
  public void addJawbComponent(final JawbComponent component, String title, Icon icon, String tip, String constraints, double weight) {
    tabPane.addJawbComponent(component, title, icon, tip, constraints, weight);
    Component theComponent = component.getComponent();
    Component c = theComponent;
    if (c instanceof JTextPane)
      textPanes.put(component, null);
    theComponent.addMouseMotionListener(lastMouseLocationListener);
    if (theComponent instanceof JComponent) {
      ((JComponent) theComponent)
      .addAncestorListener(ancestorMouseListenerUpdater);
    }
    SwingUtilities.invokeLater(new SetJawbDocumentRunnable(component, title));
  }

  protected class SetJawbDocumentRunnable implements Runnable {
    JawbComponent c;
    String title;
    protected SetJawbDocumentRunnable(JawbComponent c, String title) {
      this.c = c;
      this.title = title;
    }
    public void run() {
      handleInitialSetJawbDocumentInChild(c, title);
    }
  }

  protected Map getAllComponents() {
    return tabPane.getAllComponents();
  }

  public void addComponent(Component component, String title, Icon icon, String tip, String constraints) {
    addComponent(component, title, icon, tip, constraints, -1);
  }
  public void addComponent(Component component, String title, Icon icon, String tip, String constraints, double weight) {
    tabPane.addTab(title, icon, component, tip, constraints, weight, -1, false);
    component.addMouseMotionListener(lastMouseLocationListener);
    if (component instanceof JComponent) {
      ((JComponent) component)
      .addAncestorListener(ancestorMouseListenerUpdater);
    }
  }

  private class DocPropertyListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent evt) {
      String name = evt.getPropertyName();
      if (JawbDocument.FONT_FAMILY_PROPERTY_KEY.equals(name)) {
        setAnnotationInspectorFont((String) evt.getNewValue());
      }
    }
  }

  private Cursor modeCursor = null;

  public void setSelectionMode (AutoSelectCaret.Mode mode) {
    super.setSelectionMode(mode);
    modeCursor = super.getCursor();
  }

  protected class TextPane extends JTextPane {
    public String toString() {
      if (DEBUG > 0)
        return super.toString() + "\n" + getText();
      else
        return super.toString();
    }
    Point tmpPt = new Point();
    void handleMouseMoved(MouseEvent e) {
      // HACK: Don't allow the normal Callisto MainTextPane annotation inspector to do its thing...
      tmpPt.x = e.getX();
      tmpPt.y = e.getY();
      int offset = viewToModel (tmpPt);
      JawbDocument jawbDocument = getJawbDocument();
      List annots = jawbDocument != null ? jawbDocument.getAnnotationsAt (offset) : null;
      // this should be done whether or not there are any annots in
      // the set, so do it here and not in an AnnotMouseListener
//    inspectAnnotations (tmpPt);

      if (annots == null || annots.isEmpty ())
        setCursor (modeCursor);
      else {
        setCursor
        (Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        // since there were annots, pass it on as well
        jawbDocument.getAnnotationMouseModel().fireAnnotationMotionEvents
        //(e, annots, (JawbComponent)e.getComponent());
        (e, annots, DraggableMainTextPane.this);
      }
    }

    protected void processEvent(AWTEvent e) {
      super.processEvent(e);
      if (e instanceof MouseEvent) {
        MouseEvent me = (MouseEvent) e;
        if (e.getID() == MouseEvent.MOUSE_MOVED) {
          handleMouseMoved(me);
//        annotationInspectorMouseMotionListener.mouseMoved(me);
        }
        else if (annotationInspectorMouseMotionListener != null) {
          if (e.getID() == MouseEvent.MOUSE_DRAGGED)
            // a no-op for now, but who knows what the future holds...
            annotationInspectorMouseMotionListener.mouseDragged(me);
        }
      }
    }
    public synchronized void addMouseListener(MouseListener l) {
      super.addMouseListener(l);
    }
    MouseMotionListener annotationInspectorMouseMotionListener = null;
    public synchronized void addMouseMotionListener(MouseMotionListener l) {
      Class c = l.getClass();
      String n = c.getName();
      // HACK: Don't allow the normal Callisto MainTextPane annotation inspector to do its thing...
      if (n.equals("org.mitre.jawb.gui.MainTextPane$TextMouseListener")) {
        // Since that class is private, the only way to detect it is by this kind of abuse
        annotationInspectorMouseMotionListener = l;
      }
      else {
        super.addMouseMotionListener(l);
      }
    }
    public synchronized void removeMouseListener(MouseListener l) {
      super.removeMouseListener(l);
    }
    public synchronized void removeMouseMotionListener(MouseMotionListener l) {
      super.removeMouseMotionListener(l);
    }
  }

  protected JTextPane createJTextPane() {
    if (getMainTextPane() != null) {
      getMainTextPane().removeMouseMotionListener(annotationInspectorMouseListener);
      getMainTextPane().removeMouseListener(annotationInspectorMouseListener);
      if (textPanes != null) {
        textPanes.remove(getMainTextPane());
      }
    }
    setMainTextPane(new TextPane()); //super.createJTextPane();
    if (textPanes == null)
      textPanes = new WeakHashMap();
    textPanes.put(getMainTextPane(), null);
    if (annotationInspectorMouseListener == null)
      annotationInspectorMouseListener = createAnnotationInspectorMouseListener();
    getMainTextPane().addMouseMotionListener(annotationInspectorMouseListener);
    getMainTextPane().addMouseListener(annotationInspectorMouseListener);
    getMainTextPane().setDragEnabled(true);
    return getMainTextPane();
  }

  protected AnnotationInspectorMouseListener createAnnotationInspectorMouseListener() {
    return new AnnotationInspectorMouseListener();
  }

  public void findAgain(boolean isForward) {
    super.findAgain(isForward);
  }

  protected AnnotationMouseListener getAnnotMouseListener() {
    return super.getAnnotMouseListener();
  }

  protected int getApparentOffset(int realOffset) {
    int offs = super.getApparentOffset(realOffset);
    if (offs < 0)
      offs = 0;
    else {
      int len = getMainTextPane().getStyledDocument().getLength();
      if (offs >= len)
        offs = len - 1;
    }
    return offs;
  }

  public Component getComponent() {
    return super.getComponent();
  }

  public JawbDocument getJawbDocument() {
    return super.getJawbDocument();
  }

  protected int getRealOffset(int apparentOffset) {
    return super.getRealOffset(apparentOffset);
  }

  public ItemExchanger getSelectedAnnotationExchanger(AnnotationType type) {
    return super.getSelectedAnnotationExchanger(type);
  }

  protected HighlightPainter getSelectedAnnotPainter() {
    return super.getSelectedAnnotPainter();
  }

  public Set getSelectedAnnots() {
    return super.getSelectedAnnots();
  }

  public Mode getSelectionMode() {
    return super.getSelectionMode();
  }


  // had to change to public to match autotag changes in MTP -- RK
  public JTextPane getTextPane() {
    return getMainTextPane();
  }

  protected Component getTextView() {
    return super.getTextView();
  }

  public AWBAnnotation getTopVisibleAnnotation0() {
    floats = null;
    AWBAnnotationImpl result = null;
    if (getMainScrollPane() != null && getMainTextPane() != null) {
      JViewport vp = getMainScrollPane().getViewport();
      Rectangle viewRect = vp.getViewRect();
      Rectangle visibleRect = getMainTextPane().getVisibleRect();
      int offset = getMainTextPane().viewToModel(visibleRect.getLocation());
      Rectangle modelToView = null;
      try {
        modelToView = getMainTextPane().modelToView(offset);
      } catch (BadLocationException e) {
        e.printStackTrace();
      }
      if (modelToView != null) {
        List floats = null;
        if (DEBUG == 1)
          floats = new ArrayList();
        int[] actualOffset = { offset };
        List annots = null;
        while (annots == null) {
          annots = getAnnotationsUnder(null, getMainTextPane(), modelToView.getLocation(), 1, actualOffset);
          if (annots == null) {
            System.err.println("No annotations found (last loc: "+actualOffset[0]+") under "+modelToView);
            return null;
          }
          try {
            Rectangle m2v = getMainTextPane().modelToView(actualOffset[0]);
            if (! visibleRect.contains(m2v.getLocation())) {
              int y = m2v.y;
              while (m2v != null && m2v.y <= y) {
                actualOffset[0]++;
                try {
                  m2v = getMainTextPane().modelToView(actualOffset[0]);
                  if (m2v.y > y) {
                    if (! visibleRect.contains(m2v)) {
                      m2v = null;
                    }
                  }
                } catch (BadLocationException e) {
                  e.printStackTrace();
                  m2v = null;
                }
              }
              if (m2v != null) {
                // let's try again here
                modelToView = m2v;
                annots = null;
              }
              else {
                // annots are as good as we'll get, so give up
              }
            }
          } catch (BadLocationException e) {
            e.printStackTrace();
          }
        }
        Rectangle smallest = null;
        for (Iterator it = annots.iterator(); it.hasNext();) {
          AWBAnnotationImpl annot = (AWBAnnotationImpl) it.next();
          if (annot instanceof TextExtentRegion) {
            TextExtentRegion textAnnot = (TextExtentRegion) annot;
            int start = textAnnot.getTextExtentStart();
            int end = textAnnot.getTextExtentEnd();
            try {
              Rectangle startView = getMainTextPane().modelToView(start);
              Rectangle endView = getMainTextPane().modelToView(end);
              Rectangle union = startView.union(endView);
              if (smallest == null || smallest.contains(union)) {
                smallest = union;
                result = annot;
              }
              if (floats != null) {
                GeneralPath shape = new GeneralPath(); 
                for (int i = start; i <= end; i++) {
                  addOffsetToPath(shape, i);
                }
                shape.closePath();
                FloatingShape fs = new FloatingShape(shape, Color.RED, Color.BLACK, annot.getAnnotationType().getName(), DraggableMainTextPane.this);
                if (floats.isEmpty()) {
                  floats.add(fs);
                }
                else {
                  int pos = 0;
                  int size = floats.size() + 1;
                  float div = 1.0f / size;
                  float hue = 0;
                  float[] c = new float[4];
                  for (ListIterator lit = floats.listIterator(); lit.hasNext() && fs != null;) {
                    FloatingShape f = lit.hasNext() ? (FloatingShape) lit.next() : null;
                    if (f == null) {
                      lit.add(fs);
                      fs = null;
                    }
                    else if (fs != null && ! union.contains(f.shape.getBounds())) {
                      if (lit.hasPrevious())
                        lit.previous();
                      lit.add(fs);
                      lit.previous();
                      f = fs;
                      fs = null;
                    }
                    hue = pos * div;
                    Color fColor = Color.getHSBColor(hue, 1, 1);
                    fColor.getRGBColorComponents(c);
                    fColor = new Color(c[0], c[1], c[2], 0.25f + pos * (div / 4));
                    f.bg = fColor;
                    pos++;
                  }
                }
              }
            } catch (BadLocationException e) {
              e.printStackTrace();
            }
          }
        }
        this.floats = floats;
        if (DEBUG == 1) {
          System.out.println("Floats: "+floats);
          repaint();
        }
      }
    }
    return result;
  }

  protected TaskToolKit getToolKit() {
    return super.getToolKit();
  }

  protected Dimension getViewSize() {
    return super.getViewSize();
  }

  public boolean isCaretVisible() {
    return super.isCaretVisible();
  }

  public boolean isPaletteVisible() {
    return super.isPaletteVisible();
  }

  public void setCaretVisible(boolean visible) {
    super.setCaretVisible(visible);
  }

  public void setCursor(Cursor c) {
    super.setCursor(c);
  }

  public void setFindDialogVisible(boolean visible) {
    super.setFindDialogVisible(visible);
  }

  public void setFont(Font f) {
    super.setFont(f);
  }

  private JScrollPane mainScrollPane = null;
  private JTextPane mainAnnotationInspector = null;

  private List floats = null;

  private ScrollSynchronizer scrollSynchronizer;

  protected void paintChildren(Graphics g) {
    super.paintChildren(g);
    if (floats != null && floats.size() > 0) {
      for (Iterator it = floats.iterator(); it.hasNext();) {
        FloatingShape fs = (FloatingShape) it.next();
        fs.paintComponent(g);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.mitre.jawb.gui.JawbComponent#setJawbDocument(org.mitre.jawb.gui.JawbDocument)
   *
   * The complexity here is involved in finding the text pane created by
   * super.setJawbDocument, removing it from the container, and dropping it
   * into the top-most, left-most tabbed pane, or putting it back where it
   * was, if it was already there somewhere.
   *
   * There's also some mickey-mousing involved in putting the
   * AnnotationInspector and the AnnotationSelectionOutliner into the right
   * layer of the JLayeredPane.
   *
   * Finally, we have to make sure that and JawbComponent descendants we can
   * find about get their setJawbDocument methods called.
   */
  public void setJawbDocument(JawbDocument doc) {
    Task task = null;
    if (doc != null && (task = getToolKit().getTask()) != null && doc.getTask () != task)
      throw new IllegalStateException("doc.task ("+doc.getTask()+")!= toolkit.task ("+getToolKit().getTask()+")");
    JawbDocument old = getJawbDocument();
    super.setJawbDocument(doc);
    if (old != null) {
      old.removePropertyChangeListener(docPropListener);
    }
    tabPane.setJawbDocument(doc);
    if (doc == null) {
      if (annotationInspector != null)
        resetAnnotationInspector();
    }
    else {
      Component[] cs = getComponents();
      for (int i = 0; i < cs.length; i++) {
        if (cs[i] instanceof JScrollPane) {
          JScrollPane sp = (JScrollPane) cs[i];
          if (sp.getViewport().getView() == getMainTextPane()) {
            JScrollPane oldSP = null;
            if (getMainScrollPane() == null || getMainScrollPane() != sp) {
              oldSP = getMainScrollPane();
              if (oldSP != null && scrollSynchronizer != null) {
                oldSP.getViewport().removeChangeListener(scrollSynchronizer);
              }
              setMainScrollPane(sp);
              scrollSynchronizer = null; // new ScrollSynchronizer();
              getMainScrollPane().getViewport().addChangeListener(scrollSynchronizer);
              getMainScrollPane().setBorder(BorderFactory.createEmptyBorder());
            }
            if (oldSP != null && oldSP != getMainScrollPane() && tabPane.isAncestorOf(oldSP)) {
              oldSP.getParent().remove(oldSP);
            }
            Container mspParent = getMainScrollPane().getParent();
            if (mspParent != null && mspParent != mainTextPanePanel) {
              mspParent.remove(getMainScrollPane());
              mainTextPanePanel.add(getMainScrollPane(), BorderLayout.CENTER);
            }
            if (layerPane.getParent() != this) {
              add(layerPane, BorderLayout.CENTER);
            }
            if (tabPane.getParent() != layerPane) {
              Runnable runner = new Runnable() {
                public void run() {
                  try {
                    layerPane.add(tabPane, JLayeredPane.DEFAULT_LAYER);
                  } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                    if (DEBUG > 0)
                      e.printStackTrace();
                  }

                }
              };
              if (EventQueue.isDispatchThread()) {
                runner.run();
              }
              else {
                EventQueue.invokeLater(runner);
              }
            }
          }
        }
      }
      if (annotationSelectionOutliner == null) {
        annotationSelectionOutliner = new AnnotationSelectionOutliner();
        annotationSelectionOutliner.setOpaque(false);
        annotationSelectionOutliner.setBorder(BorderFactory.createEmptyBorder());
      }
      if (annotationSelectionOutliner.getParent() != layerPane) {
        layerPane.add(annotationSelectionOutliner, JLayeredPane.DRAG_LAYER);
        annotationSelectionOutliner.setVisible(false);
      }
      if (annotationInspector == null)
        annotationInspector = new AnnotationInspector();
      if (annotationInspector.getParent() != layerPane) {
        layerPane.add(annotationInspector, JLayeredPane.POPUP_LAYER);
      }
      setAnnotationInspectorFont(doc.getFontFamily());
      resetAnnotationInspector();
    }
    Map[] maps = new Map[] { textPanes, tabPane.jawbComponents };
    int totalJCs = maps[0].size() + maps[1].size();
    int progress = 0;
    for (int j = 0; j < maps.length; j++) {
      if (maps[j] != null)
        for (Iterator i = maps[j].keySet().iterator(); i.hasNext();) {
          Object o = i.next();
          Component c = null;
          if (o instanceof JawbComponent) {
            JawbComponent jc = (JawbComponent) o;
            setJawbDocument(jc, doc, totalJCs, progress++);
            c = jc.getComponent();
          }
          else if (o instanceof Component) {
            c = (Component) o;
          }
          if (c != null) {
            c.addMouseMotionListener(lastMouseLocationListener);
            if (c instanceof JComponent) {
              ((JComponent) c)
              .addAncestorListener(ancestorMouseListenerUpdater);
            }
          }
        }
    }
    invalidate();
  }

  protected void setJawbDocument(JawbComponent jc, JawbDocument doc, int totalJCs, int progress) {
    jc.setJawbDocument(doc);
  }

  protected void setMainScrollPane(final JScrollPane msp) {
    this.mainScrollPane = msp;
    if (mainScrollPane != null) {
      InputMap finputMap = mainScrollPane.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      ActionMap actionMap = mainScrollPane.getActionMap();
      actionMap.put("scroll-down", new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          JScrollBar bar = msp.getVerticalScrollBar();
          bar.setValue(Math.min(bar.getValue() + bar.getUnitIncrement(1), bar.getMaximum()));
        }
      });
      actionMap.put("scroll-up", new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          JScrollBar bar = msp.getVerticalScrollBar();
          bar.setValue(Math.max(bar.getValue() - bar.getUnitIncrement(-1), bar.getMinimum()));
        }
      });
      actionMap.put("slow-scroll-down", new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          JScrollBar bar = msp.getVerticalScrollBar();
          bar.setValue(Math.min(bar.getValue() + 1, bar.getMaximum()));
        }
      });
      actionMap.put("slow-scroll-up", new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          JScrollBar bar = msp.getVerticalScrollBar();
          bar.setValue(Math.max(bar.getValue() - 1, bar.getMinimum()));
        }
      });
      finputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK), "scroll-down");
      finputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, InputEvent.CTRL_DOWN_MASK), "scroll-down");
      finputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK), "scroll-up");
      finputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, InputEvent.CTRL_DOWN_MASK), "scroll-up");
      finputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "slow-scroll-down");
      finputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "slow-scroll-down");
      finputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "slow-scroll-up");
      finputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), "slow-scroll-up");
    }    
  }

  protected JScrollPane getMainScrollPane() {
    return mainScrollPane;
  }

  protected void setMainTextPane(JTextPane mainTextPane) {
    this.mainTextPane = mainTextPane;
  }

  protected JTextPane getMainTextPane() {
    return mainTextPane;
  }

  private static class FloatingShape extends JComponent {
    
    public final Shape shape;
    public Color bg;
    public Color fg;
    private final DraggableMainTextPane parent;

    FloatingShape(Shape shape, Color bg, Color fg, String name, DraggableMainTextPane parent) {
      this.shape = shape;
      this.bg = bg;
      this.fg = fg;
      setName(name);
      this.parent = parent;
    }

    public Rectangle getBounds(Rectangle rv) {
      Rectangle b = super.getBounds(rv);
      b.setBounds(shape.getBounds());
      return b;
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>getBounds()</code>.
     */
    public Rectangle bounds() {
        return shape.getBounds();
    }

    protected String paramString() {
      String thisName = getName();
      Rectangle b = getBounds();
      String str = (thisName != null? thisName + ","  : "")+ b.x + "," + b.y + "," + b.width + "x" + b.height
      + "," + fg + ":" + (100 * fg.getAlpha() / 255)
      + "," + bg + ":" + (100 * bg.getAlpha() / 255)
      ;
//    if (!isValid()) {
//    str += ",invalid";
//    }
//    if (!isVisible()) {
//    str += ",hidden";
//    }
//    if (!isEnabled()) {
//    str += ",disabled";
//    }
      return str;
    }
    
    public String toString() {
      String s = '\n' + super.toString();
      PathIterator it = shape.getPathIterator(null);
      float[] c = new float[6];
      if (! it.isDone()) {
        s += "\n{";
      }
      for (int i = 0; ! it.isDone(); i++) {
        switch (it.currentSegment(c)) {
        case PathIterator.SEG_MOVETO: s += " " + i + ":->("+ (int) c[0] + ',' + (int) c[1]+")"; break; 
        case PathIterator.SEG_LINETO: s += " " + i + ":+>("+ (int) c[0] + ',' + (int) c[1]+")"; break;
        case PathIterator.SEG_CLOSE: s += "\n}"; break;
        default:
          s += "\nuninteresting seg";
        }
        if ((i + 1) % 100 == 0) {
          s += "\n";
        }
        it.next();
      }
      return s;// + shape.getPathIterator(null).+'\n';
    }
    
    public boolean isOpaque() {
      return false;
    }

    static int strokeCount = 0;
    
    static Color[] strokeColors = { Color.BLUE, Color.ORANGE, Color.GREEN, Color.RED };
    
    protected void paintComponent(Graphics g) {
      JTextPane mainTextPane = parent.getMainTextPane();
      if (! mainTextPane.isVisible() || ! parent.isAncestorOf(mainTextPane))
        return;
      Rectangle visRect = mainTextPane.getVisibleRect();
      if (! shape.getBounds().intersects(visRect)) {
        return;
      }
      Rectangle mtpBounds = mainTextPane.getBounds();
      JScrollPane mainScrollPane = parent.getMainScrollPane();
      Rectangle bounds = mainScrollPane.getViewport().getBounds();//mtpBounds.getBounds();
      Container parent = this.parent;
      Container p = mainScrollPane;
      Rectangle viewRect = mainScrollPane.getViewport().getViewRect();
//      bounds.x += viewRect.x;
//      bounds.y += viewRect.y;
      while (p != null && p != parent) {
        Rectangle b = p.getBounds();
        bounds.x += b.x;
        bounds.y += b.y;
        p = p.getParent();
      }
      if (p == null) {
        new IllegalStateException("No common ancestor").printStackTrace();
        return;
      }
      Rectangle oldClipBounds = g.getClipBounds();
      g.setClip(bounds.intersection(oldClipBounds));
      Point trans = new Point(bounds.x-viewRect.x, bounds.y-viewRect.y);
      g.translate(trans.x, trans.y);
      if (g instanceof Graphics2D) {
        Graphics2D g2d = (Graphics2D) g;
        Color fillColor = bg; // new Color(c[0], c[1], c[2], 0.5f);
        if (bg.getAlpha() == 0 || bg.getAlpha() > 0.75 * 255) {
          float[] c = bg.getComponents(new float[4]);
          fillColor = new Color(c[0], c[1], c[2], 0.5f);
        }
        Color oldColor = g2d.getColor();
        Color oldBG = g2d.getBackground();
        Paint oldPaint = g2d.getPaint();
        Stroke oldStroke = g2d.getStroke();

        int strokes = strokeColors.length;
        //        BasicStroke bs = new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] { 1, 0 }, 0.0f);
//        BasicStroke bs = new BasicStroke(4, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] { strokes * 2, strokes * 4 }, strokeCount * 4);
        BasicStroke bs = new BasicStroke(1 + 2 * (this.parent.floats.size() - this.parent.floats.indexOf(this)), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] { 1, 0 }, 0);
        g2d.setStroke(bs);
        g2d.setPaint(fillColor);
        g2d.fill(shape);
//        g2d.setColor(fg);
        g2d.setColor(strokeColors[strokeCount]);
        strokeCount = (strokeCount + 1) % strokes;
        g2d.draw(shape);

        g2d.setPaint(oldPaint);
        g2d.setBackground(oldBG);
        g2d.setColor(oldColor);
        g2d.setStroke(oldStroke);
      }
      g.translate(-trans.x, -trans.y);
      g.setClip(oldClipBounds);
    }
    
  }
  
  public AWBAnnotation getTopVisibleAnnotation() {
    floats = null;
    AWBAnnotationImpl result = null;
    if (getMainScrollPane() != null && getMainTextPane() != null) {
      JViewport vp = getMainScrollPane().getViewport();
      Rectangle viewRect = vp.getViewRect();
      Rectangle visibleRect = getMainTextPane().getVisibleRect();
      int offset = getMainTextPane().viewToModel(visibleRect.getLocation());
      List floats = null;
      if (DEBUG == 1)
        floats = new ArrayList();
      int[] actualOffset = { offset };
      List annots = null;
      while (annots == null) {
        annots = getAnnotationsNear(1, actualOffset);
        //(null, getMainTextPane(), modelToView.getLocation(), 1, actualOffset);
        if (annots == null) {
          System.err.println("No annotations found near (last loc: "+actualOffset[0]+")");
          return null;
        }
        try {
          Rectangle m2v = getMainTextPane().modelToView(actualOffset[0]);
          if (! visibleRect.contains(m2v.getLocation())) {
            int y = m2v.y;
            while (m2v != null && m2v.y <= y) {
              actualOffset[0]++;
              try {
                m2v = getMainTextPane().modelToView(actualOffset[0]);
                if (m2v.y > y) {
                  if (! visibleRect.contains(m2v)) {
                    m2v = null;
                  }
                }
              } catch (BadLocationException e) {
                e.printStackTrace();
                m2v = null;
              }
            }
            if (m2v != null) {
              // let's try again here
              annots = null;
            }
            else {
              // annots are as good as we'll get, so give up
            }
          }
        } catch (BadLocationException e) {
          e.printStackTrace();
        }
      }
      Rectangle smallest = null;
      for (Iterator it = annots.iterator(); it.hasNext();) {
        AWBAnnotationImpl annot = (AWBAnnotationImpl) it.next();
        if (annot instanceof TextExtentRegion) {
          TextExtentRegion textAnnot = (TextExtentRegion) annot;
          int start = textAnnot.getTextExtentStart();
          int end = textAnnot.getTextExtentEnd();
          try {
            Rectangle startView = getMainTextPane().modelToView(start);
            Rectangle endView = getMainTextPane().modelToView(end);
            Rectangle union = startView.union(endView);
            if (smallest == null || smallest.contains(union)) {
              smallest = union;
              result = annot;
            }
            if (floats != null) {
              GeneralPath shape = new GeneralPath(); 
              for (int i = start; i <= end; i++) {
                addOffsetToPath(shape, i);
              }
              shape.closePath();
              FloatingShape fs = new FloatingShape(shape, Color.RED, Color.BLUE, annot.getAnnotationType().getName()+"["+start+","+end+"]", DraggableMainTextPane.this);
              if (floats.isEmpty()) {
                floats.add(fs);
              }
              else {
                int pos = 0;
                int size = floats.size() + 1;
                float div = 1.0f / size;
                float hue = 0;
                float[] c = new float[4];
                for (ListIterator lit = floats.listIterator(); lit.hasNext() && fs != null;) {
                  FloatingShape f = lit.hasNext() ? (FloatingShape) lit.next() : null;
                  if (f == null) {
                    lit.add(fs);
                    fs = null;
                  }
                  else if (fs != null && ! union.contains(f.shape.getBounds())) {
                    if (lit.hasPrevious())
                      lit.previous();
                    lit.add(fs);
                    lit.previous();
                    f = fs;
                    fs = null;
                  }
                  hue = pos * div;
                  Color fColor = Color.getHSBColor(hue, 1, 1);
                  fColor.getRGBColorComponents(c);
                  fColor = new Color(c[0], c[1], c[2], 0.25f + pos * (div / 4));
                  f.bg = fColor;
                  pos++;
                }
              }
            }
          } catch (BadLocationException e) {
            e.printStackTrace();
          }
        }
        else {
          annots.remove(annot);
        }
      }
      this.floats = floats;
      if (DEBUG == 1) {
        System.out.println("Floats ("+floats.size()+"): "+floats);
        repaint();
      }
    }
    if (DEBUG == 1) {
      System.out.println("getTopVisibleAnnotation: "+result);
    }
    return result;
  }

  private void addOffsetToPath0(GeneralPath shape, int offset) throws BadLocationException {
    {
    Shape s1 = getMainTextPane().getUI().modelToView(getMainTextPane(), offset, Bias.Forward);
    if (s1 != null) {
      Point2D currentPoint = shape.getCurrentPoint();
      boolean connect = currentPoint != null && currentPoint.getY() == s1.getBounds().getY();
      shape.append(s1, connect);
      //                    PathIterator pi = s.getPathIterator(null);
//                    float coords[] = new float[6];
//                    if (!pi.isDone()) {
//                      int type = pi.currentSegment(coords);
//
//                    }
    }
//    Shape s2 = getMainTextPane().getUI().modelToView(getMainTextPane(), offset, Bias.Forward);
//    if (s2 != null) {
//      shape.append(s2, false);
//      if (s1 != null) {
//        shape.append(s1, true);
//        shape.append(s2, true);
//      }
//    }
    }
  }
  
  private void addOffsetToPath(GeneralPath shape, int offset) throws BadLocationException {
    {
      Shape s1 = getMainTextPane().getUI().modelToView(getMainTextPane(), offset, Bias.Backward);
      if (s1 != null) {
        addShapeBoundsToPath(shape, s1);

//        shape.append(s1, connect);
        //                    PathIterator pi = s.getPathIterator(null);
//                    float coords[] = new float[6];
//                    if (!pi.isDone()) {
//                      int type = pi.currentSegment(coords);
//
//                    }
      }
      Shape s2 = getMainTextPane().getUI().modelToView(getMainTextPane(), offset, Bias.Forward);
      if (s2 != null) {
        addShapeBoundsToPath(shape, s2);
      }
    }
  }

  private void addShapeBoundsToPath(GeneralPath shape, Shape s1) {
    Point2D currentPoint = shape.getCurrentPoint();
    Rectangle bounds = s1.getBounds();
    float x = (float) bounds.getX();
    float y = (float) bounds.getY();
    float w = (float) bounds.getWidth();
    float h = (float) bounds.getHeight();
//    boolean connect = currentPoint != null && currentPoint.getY() == y;
//    if (connect) {
//      
//      shape.lineTo(x, y);
//    }
//    else {
//      if (currentPoint != null) {
//        shape.lineTo((float) currentPoint.getX(), y);
//        shape.lineTo(x, y);
//      }
//      else {
//        shape.moveTo(x, y);
//      }
//      shape.lineTo(x, y + h);
//      shape.moveTo(x, y);
//    }
    shape.moveTo(x, y);
    shape.lineTo(x + w, y);
    shape.moveTo(x, y + h);
    shape.lineTo(x + w, y + h);
    shape.moveTo(x + w, y);
    
    GeneralPath old = new GeneralPath(shape);
    shape.reset();
    PathIterator pi = old.getPathIterator(null);
    Point start = null;
    Map points = new TreeMap();
    float top = -1;
    float[] coords = new float[6];
    int i = 0;
    while (! pi.isDone()) {
      int type = pi.currentSegment(coords);
      pi.next();
      if (type != PathIterator.SEG_LINETO && type != PathIterator.SEG_MOVETO)
        continue;
//      System.err.println((i++) + " " + type+":"+coords[0]+","+coords[1]);
      float[] X = (float[]) points.get(new Float(coords[1]));
      if (X == null) {
        points.put(new Float(coords[1]), new float[] { coords[0], coords[0]});
      }
      else {
        if (coords[0] < X[0])
          X[0] = coords[0];
        if (coords[0] > X[1])
          X[1] = coords[0];
      }
    }
    float prevY = -1, prevX0 = -1, prevX1 = -1;
    boolean first = true;
    ArrayList ys = new ArrayList();
    for (Iterator it = points.entrySet().iterator(); it.hasNext();) {
      Map.Entry entry = (Map.Entry) it.next();
      float Y = ((Float) entry.getKey()).floatValue(); 
      float[] X = (float[]) entry.getValue(); 
      if (first) {
        shape.moveTo(X[0], Y);
        shape.lineTo(X[1], Y);
        first = false;
      }
      else {
        shape.lineTo(prevX1, Y);
        if (prevX1 != X[1]) {
          shape.lineTo(X[1], Y);
        }
      }
      prevY = Y;
      prevX0 = X[0];
      prevX1 = X[1];
      ys.add(0, new Float(Y));
    }
    first = true;
    for (Iterator it = ys.iterator(); it.hasNext();) {
      Float Y = (Float) it.next();
      float[] X = (float[]) points.get(Y);
      if (first) {
        shape.lineTo(X[0], Y.floatValue());
        first = false;
      }
      else {
        shape.lineTo(prevX0, Y.floatValue());
        if (prevX0 != X[0]) {
          shape.lineTo(X[0], Y.floatValue());
        }
      }
      prevY = Y.floatValue();
      prevX0 = X[0];
      prevX1 = X[1];
    }
//    Point2D currentPoint = shape.getCurrentPoint();
//    Rectangle bounds = s1.getBounds();
//    float x = (float) bounds.getX();
//    float y = (float) bounds.getY();
//    float w = (float) bounds.getWidth();
//    float h = (float) bounds.getHeight();
//    boolean connect = currentPoint != null && currentPoint.getY() == y;
//    if (connect) {
//      shape.lineTo(x, y);
//    }
//    else {
//      if (currentPoint != null) {
//        shape.lineTo((float) currentPoint.getX(), y);
//        shape.lineTo(x, y);
//      }
//      else {
//        shape.moveTo(x, y);
//      }
//      shape.lineTo(x, y + h);
//      shape.moveTo(x, y);
//    }
//    shape.lineTo(x + w, y);
//    shape.moveTo(x, y + h);
//    shape.lineTo(x + w, y + h);
//    shape.moveTo(x + w, y);
  }
  
  public void setPaletteVisible(boolean visible) {
    super.setPaletteVisible(visible);
  }

  public void setSelectedAnnotationExchanger(AnnotationType type,
      ItemExchanger exchanger) {
    super.setSelectedAnnotationExchanger(type, exchanger);
  }


  protected class AnnotationInspector extends JInternalFrame implements HyperlinkListener {

    protected class AnnotationInspectorUI extends BasicInternalFrameUI {
      boolean dragging = false;
      Point dragOffset = new Point(0, 0);
      Point dragPoint = new Point(0, 0);
      Point dragStart = new Point(0, 0);
      Point scratchPoint = new Point(0, 0);
      Rectangle bounds = new Rectangle(0, 0, 0, 0);
      Rectangle parentBounds = new Rectangle(0, 0, 0, 0);
      Insets insets = new Insets(0, 0, 0, 0);
      AnnotationInspectorMouseListener mouseListener = new AnnotationInspectorMouseListener();

      protected class AnnotationInspectorBorderListener extends BorderListener {
      }
      public class AnnotationInspectorMouseListener extends MouseInputAdapter {

        public void mouseDragged(MouseEvent e) {
          if (dragging) {
            getParent().getBounds(parentBounds);
            Insets insets = getParent().getInsets();
            Component c = (Component) e.getSource();
            Point sPoint = c.getLocationOnScreen();
            Point cPoint = getLocationOnScreen();
            dragPoint.x = e.getX() + sPoint.x - cPoint.x;
            dragPoint.y = e.getY() + sPoint.y - cPoint.y;
            dragPoint.x += dragOffset.x;
            dragPoint.y += dragOffset.y;
            getBounds(bounds);
            int maxX = parentBounds.width - insets.right - bounds.width;
            int maxY = parentBounds.height - insets.bottom - bounds.height;
            bounds.x = Math.max(insets.left,
                Math.min(dragPoint.x, maxX));
            bounds.y = Math.max(insets.top,
                Math.min(dragPoint.y, maxY));
//            setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
            getDesktopManager().dragFrame(frame, bounds.x, bounds.y);
          }
        }
        public void mouseMoved(MouseEvent e) {

        }
        public void mouseClicked(MouseEvent e) {

        }
        public void mouseEntered(MouseEvent e) {

        }
        public void mouseExited(MouseEvent e) {

        }
        public void mousePressed(MouseEvent e) {
          if (! dragging) {
            getInsets(insets);
            Insets i = insets;
            scratchPoint.setLocation(e.getX(), e.getY());
            Point ep = scratchPoint;
            int resizeCornerSize = 16;
            if (ep.x <= i.left) {
              return;
            } else if (ep.x >= frame.getWidth() - i.right) {
              return;
            } else if (ep.y <= i.top) {
              return;
            } else if (ep.y >= frame.getHeight() - i.bottom) {
              return;
            }
            getDesktopManager().beginDraggingFrame(frame);
            dragging = true;
            getLocation(dragStart);
            Component c = (Component) e.getSource();
            Point sPoint = c.getLocationOnScreen();
            Point cPoint = getLocationOnScreen();
            dragOffset.x = sPoint.x - cPoint.x - e.getX();
            dragOffset.y = sPoint.y - cPoint.y - e.getY();
          }
        }
        public void mouseReleased(MouseEvent e) {
          if (dragging) {
            getDesktopManager().endDraggingFrame(frame);
            dragging = false;
          }
        }
      }
      public AnnotationInspectorUI(JInternalFrame b) {
        super(b);
      }
      class TitlePane extends JPanel {
        TitlePane() {
          super();
          setBackground(UIManager.getColor("ScrollBar.thumb"));
        }
        public Dimension getPreferredSize() {
          return new Dimension(scrollBarSize / 2, scrollBarSize / 2);
        }
      }

      protected MouseInputAdapter createBorderListener(JInternalFrame w) {
        return new AnnotationInspectorBorderListener();
      }
      protected JComponent createNorthPane(JInternalFrame w) {
        minimalTitlePane = new TitlePane();
        return minimalTitlePane;
      }
      JPanel minimalTitlePane = null;
      public void installUI(JComponent c) {
        super.installUI(c);
      }

    }

    protected final JTextPane textPane;
    protected final JScrollPane scrollPane;

    class AIScrollBarUI extends BasicScrollBarUI {
      int scrollBarSize = 10;

      class ArrowButton extends BasicArrowButton {
        private Color shadow;
        private Color darkShadow;
        private Color highlight;

        public ArrowButton(int direction, Color background, Color shadow,
            Color darkShadow, Color highlight) {
          super(direction, background, shadow, darkShadow, highlight);
          this.shadow = shadow;
          this.darkShadow = darkShadow;
          this.highlight = highlight;
        }
        public Dimension getPreferredSize() {
          return new Dimension(scrollBarSize, scrollBarSize);
        }
        public void paint(Graphics g) {
          Color origColor;
          boolean isPressed, isEnabled;
          int w, h, size;

          w = getSize().width;
          h = getSize().height;
          origColor = g.getColor();
          isPressed = getModel().isPressed();
          isEnabled = isEnabled();

          g.setColor(getBackground());
          g.fillRect(1, 1, w-2, h-2);

          // Using the background color set above
          g.drawLine(0, 0, 0, h-1);
          g.drawLine(1, 0, w-2, 0);

          g.setColor(highlight);    // inner 3D border
          g.drawLine(1, 1, 1, h-3);
          g.drawLine(2, 1, w-3, 1);

          g.setColor(shadow);       // inner 3D border
          g.drawLine(1, h-2, w-2, h-2);
          g.drawLine(w-2, 1, w-2, h-3);

          g.setColor(shadow);     // black drop shadow  __|
          g.drawLine(0, h-1, w-1, h-1);
          g.drawLine(w-1, h-1, w-1, 0);

          g.setColor(darkShadow);
          switch (direction) {
          case NORTH:
            g.drawLine(0, h-1, w-1, h-1);
            break;
          case SOUTH:
            g.drawLine(0, 0, w-1, 0);
            break;
          case EAST:
            g.drawLine(0, 0, 0, h-1);
            break;
          case WEST:
            g.drawLine(w-1, 0, w-1, h-1);
            break;
          }
          // If there's no room to draw arrow, bail
          if(h < 5 || w < 5)      {
            g.setColor(origColor);
            return;
          }

          if (isPressed) {
            g.translate(1, 1);
          }

          // Draw the arrow
          size = Math.min((h - 4) / 3, (w - 4) / 3);
          size = Math.max(size, 2);
          paintTriangle(g, (w - size) / 2, (h - size) / 2,
              size, direction, isEnabled);

          // Reset the Graphics back to it's original settings
          if (isPressed) {
            g.translate(-1, -1);
          }
          g.setColor(origColor);

        }
      }
      public AIScrollBarUI(int scrollBarSize) {
        this.scrollBarSize = scrollBarSize;
      }
      public Dimension getPreferredSize(JComponent c) {
        return (scrollbar.getOrientation() == JScrollBar.VERTICAL) ? new Dimension(
            scrollBarSize, 48)
            : new Dimension(48, scrollBarSize);
      }
      protected void configureScrollBarColors() {
        super.configureScrollBarColors();
        thumbHighlightColor = UIManager.getColor("ScrollBar.thumb");
        thumbLightShadowColor = UIManager.getColor("ScrollBar.thumb");
        thumbDarkShadowColor = UIManager.getColor("ScrollBar.thumb");
        thumbColor = UIManager.getColor("ScrollBar.thumb");
        trackColor = UIManager.getColor("ScrollBar.track");
        trackHighlightColor = UIManager.getColor("ScrollBar.track");
      }
      protected JButton createDecreaseButton(int orientation)  {
        return new ArrowButton(orientation,
            UIManager.getColor("ScrollBar.thumb"),
            UIManager.getColor("ScrollBar.thumb"),
            UIManager.getColor("ScrollBar.thumbDarkShadow"),
            UIManager.getColor("ScrollBar.thumb"));
      }

      protected JButton createIncreaseButton(int orientation)  {
        return new ArrowButton(orientation,
            UIManager.getColor("ScrollBar.thumb"),
            UIManager.getColor("ScrollBar.thumb"),
            UIManager.getColor("ScrollBar.thumbDarkShadow"),
            UIManager.getColor("ScrollBar.thumb"));
      }
    }

    final int scrollBarSize = 10;

    AnnotationInspector() {
      super("", true);
      AnnotationInspectorUI ui = new AnnotationInspectorUI(this);
      setUI(ui);
      textPane = new JTextPane();
      textPane.setContentType("text/html");
      textPane.setEditable(false);
      textPane.addHyperlinkListener(this);
      Container c = getContentPane();
      c.setLayout(new BorderLayout());
      scrollPane = new JScrollPane(textPane,
          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
          JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("TextPane.background")));
      scrollPane.getVerticalScrollBar().setUI(new AIScrollBarUI(scrollBarSize));
      scrollPane.getHorizontalScrollBar().setUI(new AIScrollBarUI(scrollBarSize));
      c.add(scrollPane, BorderLayout.CENTER);
      setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createLineBorder(UIManager.getColor("InternalFrame.borderColor"), 1)
          ,
          BorderFactory.createMatteBorder(3, 3, 3, 3, UIManager.getColor("TextPane.background"))
      ));
      InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      ActionMap actionMap = getActionMap();
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
      actionMap.put("escape", new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          setVisible(false);
        }
      });
      InputMap finputMap = textPane.getInputMap(WHEN_FOCUSED);
      actionMap = textPane.getActionMap();
      actionMap.put("scroll-down", new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          JScrollBar bar = scrollPane.getVerticalScrollBar();
          bar.setValue(Math.min(bar.getValue() + bar.getUnitIncrement(1), bar.getMaximum()));
        }
      });
      actionMap.put("scroll-up", new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          JScrollBar bar = scrollPane.getVerticalScrollBar();
          bar.setValue(Math.max(bar.getValue() - bar.getUnitIncrement(-1), bar.getMinimum()));
        }
      });
      actionMap.put("scroll-right", new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          JScrollBar bar = scrollPane.getHorizontalScrollBar();
          bar.setValue(Math.min(bar.getValue() + bar.getUnitIncrement(1), bar.getMaximum()));
        }
      });
      actionMap.put("scroll-left", new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          JScrollBar bar = scrollPane.getHorizontalScrollBar();
          bar.setValue(Math.max(bar.getValue() - bar.getUnitIncrement(-1), bar.getMinimum()));
        }
      });
      finputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, /*InputEvent.CTRL_DOWN_MASK*/ 0), "scroll-down");
      finputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, /*InputEvent.CTRL_DOWN_MASK*/ 0), "scroll-down");
      finputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, /*InputEvent.CTRL_DOWN_MASK*/ 0), "scroll-up");
      finputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, /*InputEvent.CTRL_DOWN_MASK*/ 0), "scroll-up");
      finputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, /*InputEvent.CTRL_DOWN_MASK*/ 0), "scroll-right");
      finputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, /*InputEvent.CTRL_DOWN_MASK*/ 0), "scroll-right");
      finputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, /*InputEvent.CTRL_DOWN_MASK*/ 0), "scroll-left");
      finputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, /*InputEvent.CTRL_DOWN_MASK*/ 0), "scroll-left");
      setOpaque(true);
      textPane.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          int mods = e.getModifiersEx();
          int onmask = MouseEvent.CTRL_DOWN_MASK | MouseEvent.ALT_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK;
          int offmask = 0;
          if (e.getClickCount() > 1
              && (mods & (onmask | offmask)) == onmask) {
            dump();
          }
        }
      });
      annotRefs = new HashMap();
    }

    protected void dump() {
      StyledDocument sdoc = textPane.getStyledDocument();
      int dLen = sdoc.getLength();
      String text = textPane.getText();

      JEditorPane t = new JEditorPane("text/html", text);
      t.setCaretPosition(0);

      JTextArea b = new JTextArea();
      b.setText(text);

      StringBuffer sb;

      final JFrame f = new JFrame("Dictionary dump");
      JTabbedPane tp = new JTabbedPane();
      f.getContentPane().add(tp);
      tp.add("Source", new JScrollPane(b));
      tp.add("View", new JScrollPane(t));
      f.pack();
      f.setSize(1000, 1000);
      InputMap inputMap = tp.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      ActionMap actionMap = tp.getActionMap();
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
      actionMap.put("escape", new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          f.dispose();
        }
      });

      f.setVisible(true);

      System.err.println("Doc size = "+sdoc.getLength());
      System.err.println("text length = "+text.length());
    }

    public void setVisible(boolean aFlag) {
      super.setVisible(aFlag);
      if (DraggableMainTextPane.this != null
          && annotationSelectionOutliner != null) {
        if (annotationSelectionOutliner.isVisible())
          annotationSelectionOutliner.setVisible(false);
        if (aFlag)
          annotationSelectionOutliner.setVisible(aFlag);
      }
    }
    Rectangle bounds = null;//new Rectangle(0, 0, 0, 0);
    protected void setText(String text) {
      if (text != null) {
        textPane.setText(text);
        if (text.length() == 0 ) {
          if (isVisible()) {
            setVisible(false);
          }
          return;
        }
      }
      Dimension psize = getParent().getSize();
      if (bounds == null) {
        pack();
        bounds = getBounds();
        Dimension size = textPane.getPreferredSize();
        Dimension maxSize = getPreferredSize();
        bounds.width = Math.min(size.width, maxSize.width);
        bounds.height = Math.min(size.height, maxSize.height);
      }
      else {
        getBounds(bounds);
      }
      bounds.x = lastInspectorPoint.x;
      bounds.y = lastInspectorPoint.y;
      bounds.width = Math.min(psize.width - bounds.x, bounds.width);
      bounds.height = Math.min(psize.height - bounds.y, bounds.height);
      setVisible(true);
      setBounds(bounds);
      bounds = null;
      textPane.getCaret().setDot(0);
      textPane.requestFocusInWindow();
    }

    /**
     * Display the annotations under the specifiec point using the
     * TextComponent's viewToModel conversion. This caches data so that
     * minimal redundant work is performed. If a null point is passed, the
     * last point passed will be reinspected.
     */
    protected void inspectAnnotations(MouseEvent e) {
      if (e == null) {
        setVisible(false);
        return;
      }

      List annots = null;

      Point point = e.getPoint();
      StyledDocument doc = getMainTextPane().getStyledDocument();
      int offset = getMainTextPane().viewToModel(point);
      Component source = (Component) e.getSource();
      Point absLoc = source.getLocationOnScreen();
      Point parentLoc = getParent().getLocationOnScreen();
      try {
        Rectangle newOffset = getMainTextPane().modelToView(offset);
        if (offsetRect != null && newOffset.equals(offsetRect) && isVisible()) {
          return;
        }
        offsetRect = newOffset;
      } catch (BadLocationException e1) {
        if (DEBUG > 0) {
          e1.printStackTrace();
        }
      }
      annots = getAnnotationsUnder(e, lastInspectorPoint);
 
      Point previousInspectedPoint = new Point(lastInspectedPoint);
      lastInspectedPoint.setLocation(e.getPoint());
      if (annots == null || annots.isEmpty()) {
        setText("");
      }
      else if (annots.equals(lastInspectedAnnots)) {
//        setText(null); // causes inspector to redisplay with same text as before
      }
      else { //if (!annots.equals(lastInspectedAnnots)) {
        lastInspectedAnnots = annots;

        StringBuffer descriptions = new StringBuffer();
        Iterator iter = annots.iterator();
        descriptions.append("<html>\n")
        .append("<head>\n")
        .append("<style>\n")
        .append("body { font-family: \"")
        .append(getFont().getFamily())
        .append("\" }\n")
        .append(".source { color: red;")
        .append(" font-family: \"")
        .append(getForeignFont().getFamily())
        .append("\" }\n")
        .append(".annotation { color: blue; font-family: \"")
        .append(getFont().getFamily())
        .append("\" }\n")
        .append(".attribute { color: green; font-family: \"")
        .append(getFont().getFamily())
        .append("\" }\n")
        .append("a { text-decoration: none }\n")
        .append("div { margin-top: 0; margin-bottom: 5pt }\n")
        .append("pre { margin-top: 0; margin-bottom: 0 }\n")
        .append("</style>\n")
        .append("</head>\n")
        .append("<body>\n");

        annotRefs.clear();

        while (iter.hasNext()) {
          // TODO: clean up this hack around TextExtentAttributes
          AWBAnnotation annot = (AWBAnnotation) iter.next();
          Stack pending = new Stack();
          int indent = 0;
          pending.push(annot);
          while (! pending.isEmpty()) {
            annot = (AWBAnnotation) pending.pop();
            String href = "#" + annotRefs.size();
            annotRefs.put(href, annot);
            descriptions.append("<div style=\"margin-left: ").append((indent + 0) * 10)
            .append("pt\">\n")
            .append("Annotation Type:\n<a href=\"")
            .append(href)
            .append("\"<font class=\"annotation\" style=\"margin-left: ").append((indent + 0) * 10)
            .append("pt\">\n")
            .append(annot.getATLASType().getName())
            .append("</font>\n</a>\n</div>\n");
            String text = null;

            if (annot instanceof TextExtentRegion) {
              text = ((TextExtentRegion) annot).getTextExtent();
              descriptions.append("<div style=\"margin-left: ").append((indent+1) * 10)
              .append("pt\">\n")
              .append("Text:\n<pre class=\"source\" style=\"margin-left: ").append((indent+1) * 10)
              .append("pt\">\n")
              .append(text)
              .append("</pre>\n</div>\n");
            }
            else if (annot instanceof NamedExtentRegions) {
              text = ((NamedExtentRegions) annot)
              .getTextExtent("full");
              descriptions.append("<div style=\"margin-left: ").append((indent+1) * 10)
              .append("pt\">\n")
              .append("Full:\n<pre class=\"source\" style=\"margin-left: ").append((indent+1) * 10)
              .append("pt\">\n")
              .append(text)
              .append("</pre>\n</div>\n");

              text = ((NamedExtentRegions) annot)
              .getTextExtent("head");
              descriptions.append("<div style=\"margin-left: ").append((indent+1) * 10)
              .append("pt\">\n")
              .append("Head:\n<pre class=\"source\" style=\"margin-left: ").append((indent+1) * 10)
              .append("pt\">\n")
              .append(text)
              .append("</pre>\n</div>\n");
            }

            String[] keys = annot.getAttributeKeys();
            boolean headingShown = false;
            for (int i = 0; i < keys.length; i++) {
              Object value = null;
              // TODO: the first two work around a bug in
              // PhraseAnnotation
              // TODO: the next three may indicate aproblem with the
              // design
              // The last two skip empty attributes per request: RFE
              // #408
              if (keys[i].equals("end")
                  || keys[i].equals("start")
                  || keys[i].endsWith(TextExtentRegion.TEXT_EXTENT)
                  || keys[i].endsWith(TextExtentRegion.TEXT_EXTENT_START)
                  || keys[i].endsWith(TextExtentRegion.TEXT_EXTENT_END)
                  || (value = annot.getAttributeValue(keys[i])) == null
                  || value.equals(""))
                continue;
              if (! headingShown) {
                headingShown = true;
                descriptions.append("<div style=\"margin-left: ").append((indent+1) * 10)
                .append("pt\">\n")
                .append("Attributes:")
                .append("\n<dl style=\"margin-left: ").append((indent+2) * 10)
                .append("pt\">\n");
              }
              descriptions.append("<dt>\n")
              .append(keys[i])
              .append(":\n<font class=\"attribute\" style=\"margin-left: ").append((indent+4) * 10)
              .append("pt\">\n")
              .append(value)
              .append("</font>\n</dt>\n");
            }
            if (keys.length > 0)
              descriptions.append("</dl>\n</div>\n");
            if (iter.hasNext())
              descriptions.append("\n");
          }
        }
        descriptions.append("</body>\n")
        .append("</html>");

        setText(descriptions.toString());
      }
    }
    Font foreignFont = null;
    protected final HashMap annotRefs;
    private void setForeignFont(String family) {
      Font font = getFont();
      this.foreignFont = new Font(family, font.getStyle(), font.getSize());
    }
    public Font getForeignFont() {
      return foreignFont;
    }
//  public synchronized void addMouseListener(MouseListener l) {
//  super.addMouseListener(l);
//  if (l instanceof AnnotationInspectorMouseListener)
//  scrollPane.addMouseListener(l);
//  }
//  public synchronized void addMouseMotionListener(MouseMotionListener l) {
//  super.addMouseMotionListener(l);
//  if (l instanceof AnnotationInspectorMouseListener)
//  scrollPane.addMouseMotionListener(l);
//  }
//  public synchronized void removeMouseListener(MouseListener l) {
//  super.removeMouseListener(l);
//  if (l instanceof AnnotationInspectorMouseListener)
//  scrollPane.removeMouseListener(l);
//  }
//  public synchronized void removeMouseMotionListener(MouseMotionListener l) {
//  super.removeMouseMotionListener(l);
//  if (l instanceof AnnotationInspectorMouseListener)
//  scrollPane.removeMouseMotionListener(l);
//  }

    public void hyperlinkUpdate(HyperlinkEvent e) {
      Element elt = e.getSourceElement();
      if (elt == null)
          return;
      HyperlinkEvent.EventType etype = e.getEventType();
      if (etype == HyperlinkEvent.EventType.ACTIVATED) {
          AttributeSet attrs = elt.getAttributes();
          AttributeSet attr = (AttributeSet) attrs.getAttribute(HTML.Tag.A);
          if (attr == null)
              return;
          String href = (String) attr.getAttribute(HTML.Attribute.HREF);
          AWBAnnotation annot = (AWBAnnotation) annotRefs.get(href);
          if (annot != null) {
            getJawbDocument().selectAnnotation(annot);
            setText("");
          }
      }
    }
  }

  protected AnnotationInspector annotationInspector;
  private List lastInspectedAnnots = null;
  private Point lastInspectedPoint = new Point();
  private Point lastInspectorPoint = new Point();

  /**
   * This will force inspectAnnotations to be redisplayed, even if the point
   * inspected returns the same list as the last inspection. This was a needed
   * because our 'caching' mechanism was simply to save a reference to the
   * list returned from documet.getAnnotationsAt. 1) this would maintain
   * reference to a documents data after documents had been removed, and 2)
   * when raising annotation at point, the document only reorders lists for
   * each Element, thus equalit to the 'last' list was true, thus no
   * reinspection
   */
  private void resetAnnotationInspector() {
    lastInspectedAnnots = null;
    annotationInspector.setText("");
  }

  private void setAnnotationInspectorFont(String family) {
    annotationInspector.setForeignFont(family);
  }

  protected void handleInitialSetJawbDocumentInChild(JawbComponent child, String title) {
    JawbDocument doc;
    if ((doc = getJawbDocument()) != null) {
      child.setJawbDocument(doc);
    }
  }

  public String getNamespace() {
    return tabPane.namespace;
  }

  public List getAnnotationsUnder(MouseEvent e) {
    return getAnnotationsUnder(e, null);
  }

  public List getAnnotationsUnder(MouseEvent e, Point lastInspectorPoint) {
    Component source = (Component) e.getSource();
    Point point = e.getPoint();
    return getAnnotationsUnder(lastInspectorPoint, source, point, 0, new int[1]);
  }

  public List getAnnotationsUnder(Point lastInspectorPoint, Component source, Point point, int searchDirection, int[] offset) {
    List annots;
    StyledDocument doc = getMainTextPane().getStyledDocument();
    offset[0] = getMainTextPane().viewToModel(point);
    Point absLoc = source.getLocationOnScreen();
    Point parentLoc = getParent().getLocationOnScreen();
    try {
      Rectangle offsetRect = getMainTextPane().modelToView(offset[0]);
      Element mouseElement = doc.getCharacterElement(offset[0]);
      AttributeSet mouseAttributes = mouseElement.getAttributes();
      int spaceBelow = (int) Math.ceil(StyleConstants.getSpaceBelow(mouseAttributes));
      int spaceAbove = (int) Math.ceil(StyleConstants.getSpaceAbove(mouseAttributes));
      Font font = doc.getFont(mouseAttributes);
      int fontHeight = font == null ? -1 : source.getFontMetrics(font).getHeight();
      Point targetPoint = new Point(point.x + absLoc.x - parentLoc.x,
          Math.max(offsetRect.y, point.y) + absLoc.y - parentLoc.y
          + (int) Math.ceil(0.5 * Math.max(fontHeight + spaceAbove + spaceBelow, offsetRect.getHeight())) + 1
      );
      int targetOffset = getMainTextPane().viewToModel(targetPoint);
      Element targetElement = doc.getCharacterElement(targetOffset);
      AttributeSet attributes = targetElement.getAttributes();
      spaceAbove = (int) Math.ceil(StyleConstants.getSpaceAbove(attributes));
      targetPoint.y += spaceAbove;
      if (lastInspectorPoint != null) {
        lastInspectorPoint.setLocation(targetPoint);
      }
    } catch (BadLocationException blx) {
      if (DEBUG > 1)
        blx.printStackTrace();
    }
    return getAnnotationsNear(searchDirection, offset);
  }

  protected List getAnnotationsNear(int searchDirection, int[] offset) {
    List annots;
    do {
      annots = getJawbDocument().getAnnotationsAt(offset[0]);
      if (annots == null || annots.size() == 0) {
        offset[0] += searchDirection;
      }
    } while (searchDirection != 0 && (annots == null || annots.size() == 0)
        && offset[0] >= 0
        && offset[0] < getJawbDocument().getStyledDocument().getLength());
    return annots;
  }

}
