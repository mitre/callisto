
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.*;

import gov.nist.atlas.Annotation;
import gov.nist.atlas.type.AnnotationType;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.JawbLogger;
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.TextExtentRegion;
import org.mitre.jawb.atlas.NamedExtentRegions;
import org.mitre.jawb.atlas.AnnotationModelEvent;
import org.mitre.jawb.atlas.AnnotationModelListener;
import org.mitre.jawb.gui.ActionModel.ActionCollection;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.swing.AutoSelectCaret;
import org.mitre.jawb.swing.ItemExchanger;
import org.mitre.jawb.swing.LineHighlightPainter;
import org.mitre.jawb.swing.SetModel;
import org.mitre.jawb.swing.LinkedHashSetModel;
import org.mitre.jawb.swing.event.SetDataEvent;
import org.mitre.jawb.swing.event.SetDataListener;
import org.mitre.jawb.tasks.TaskToolKit;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.ToocaanTask;
import org.mitre.jawb.tasks.ToocaanToolKit;
import org.mitre.jawb.tasks.Autotagger;

/**
 * Panel which Wraps a JTextPane and acts as a veiw on a JawbDocument.<p>
 *
 * TODO: UndoableEditListener
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @author Laurel D. Riek <laurel@mitre.org>
 * @version 8/21/2002
 */
public class MainTextPane extends JPanel implements JawbComponent {

  private static int DEBUG = 0;

  /** Key for selection mode used to annotate new documents with */
  public static final String SELECTION_MODE_KEY =
    "callisto.text.selection.mode";
  /** Key for showing cursor in text pane */
  public static final String CARET_VISIBLE_KEY =
    "callisto.text.caret.visible";
  /** Key for showing cursor in text pane */
  public static final String ANNOTATION_INSPECTOR_REQUIRE_SHIFT_KEY =
    "callisto.text.annotation.inspector.require.shift";
  public static final String ANNOTATION_INSPECTOR_REQUIRE_VISIBLE =
    "callisto.text.annotation.inspector.require.visible";
  /** Key for showing available actions for text pane */
  public static final String ACTIONS_DIALOG_VISIBLE_KEY =
    "callisto.text.actions.dialog.visible";
  /** Key for the preferred color for reconcilation segment highlights */
  public static final String RECONCILIATION_HIGHLIGHT_KEY = 
    "callisto.text.reconciliation.highlight";


  /** Cursor to indicate when selecting by character */
  private static final Cursor CHAR_CURSOR;
  /** Cursor to indicate when selecting by word */
  private static final Cursor WORD_CURSOR;

  static {
    Toolkit tk = Toolkit.getDefaultToolkit();
    Point p = new Point(6,12);
    CHAR_CURSOR = tk.createCustomCursor
      (Jawb.getImage ("cursors/charmode.gif", null), p, "CharMode");
    WORD_CURSOR = tk.createCustomCursor
      (Jawb.getImage ("cursors/wordmode.gif", null), p, "WordMode");
  }
  
  /** To Display our document, but wrapped within a JScrollPane. */
  private JTextPane textPane;

  /** Contains the textpane, will be swapped for noDoc when appropriate. */
  private JScrollPane scrollPane;
  
  /** View Annotations under the mouse. */
  private JTextPane annotationInspector;

  /** Should we display the annotation inspector? */
  private boolean annotationInspectorVisible = true;
  
  /** Our data source. */
  private JawbDocument document;

  /** Our popup mention menu. */
  private MainTextContextMenu contextMenu;
  
  /** Our popup mention menu when it's disabled. */
  private MainTextContextMenu disabledContextMenu;

  /** Action palette to track current doc, selected annots, and caret. */
  private MainTextPalette palette;
  
  /** Dialog to show the palette */
  private JDialog paletteDialog;

  /** Displays and maintains find parameters. */
  private MainTextFinder finder;
  
  /** Dialog to show the text find dialog */
  private JDialog findDialog;

  /** Reference to blank JPanel to display when no files open. */
  private JLabel noDoc = null;

  /** place holder so we don't have to recreate textPane when no files open */
  private StyledDocument emptyDoc = new DefaultStyledDocument();

  /** Utilities for the task we work with */
  private TaskToolKit kit;

  /** Lexical tags, tracked separately from the Documents selectionModel */
  private LinkedHashSetModel selectedLexAnnots = new LinkedHashSetModel ();

  /** Listener for changes to the list of 'document' selected annots. */
  private SetDataListener aSelectionListener = new AnnotSelectionListener ();

  /** Listener for additions and removals of, and changes to, annotations. */
  private AnnotationModelListener aModelListener = new AnnotModelListener ();

  /** Listen for changes to the Preferences */
  private PropertyChangeListener prefListener = new PreferencesListener ();
  
  /** Listen for changes to the current document's properties  */
  private PropertyChangeListener docPropListener = new DocPropertyListener ();

  /** Observes mouse events on Annotations in the table */
  private AnnotationMouseListener annotMouseListener = null;


  /** For mapping annotations to their lexical counterparts */
  private HashMap exchangers = new HashMap ();;

  /** Current (custom) cursor when selecting text. */
  private Cursor modeCursor = WORD_CURSOR;

  private ActionModel actionModel;

  /**
   * Create a text panel which can display and edit a JawbDocument..
   */
  public MainTextPane (TaskToolKit kit) {
    this.kit = kit;
    
    // set a borderlayout
    setLayout (new BorderLayout ());

    textPane = createJTextPane ();
    textPane.setEditable (false);
    textPane.setCursor (modeCursor);
    textPane.setLocale (new Locale ("en", "US", "JAWB"));//for word iterator

    // actions

    // Setting TAB to raiseAnnotation didn't work immidiately. Swings focus
    // traversal Policy (introduced in 1.4) was in the way. Apparently when a
    // text component is set NOT-editable, it's focus traversal keys are reset
    // to the default values, which are TAB & C-TAB, as opposed to
    // JTextComponent default of just C-TAB. since FocusManageer consumes it,
    // the component never sees it See:
    // http://forum.java.sun.com/thread.sp?forum=57&thread=283320
    // http://java.sun.com/j2se/1.4/docs/api/java/awt/doc-files/FocusSpec.html#FocusTraversal
    // and:
    // http://java.sun.com/j2se/1.4.1/docs/api/java/awt/Component.html#setFocusTraversalKeys(int,java.util.Set)

    Set forwardTraversalKeys =
      Collections.singleton (KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                                                    InputEvent.CTRL_MASK));
    textPane.setFocusTraversalKeys (KeyboardFocusManager.
                                    FORWARD_TRAVERSAL_KEYS,
                                    forwardTraversalKeys);
    Set backwardTraversalKeys =
      Collections.singleton (KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                                                    InputEvent.SHIFT_MASK |
                                                    InputEvent.CTRL_MASK));
    textPane.setFocusTraversalKeys (KeyboardFocusManager.
                                    BACKWARD_TRAVERSAL_KEYS,
                                    backwardTraversalKeys);

    // scrollpane added first so we know the index to swap for noDoc
    scrollPane = new JScrollPane(getTextView(),
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    
    // unbound
    //inputMapA.put(KeyStroke.getKeyStroke(KeyEvent.VK_T,InputEvent.CTRL_MASK),
    //              "toggle-caret-visible");

    ActionMap actionMap = textPane.getActionMap ();
    actionMap.put ("toggle-ligature", new ToggleLigature());
    actionMap.put ("raise-annotation", new RaiseAnnotation());
    actionMap.put ("delete-annotation", new MainTextDeleteAnnotation(kit));
    actionMap.put ("clear-selection", new ClearSelection());

    // when a JTextComponent is not-editable, yet still focusable, for some
    // reason, putting a different mapping for VK_BACK_SPACE continues to fire
    // an event for the delete-previous. I'm masking with this
    actionMap.put ("delete-previous", new AbstractAction ("dummy"){
      public void actionPerformed (ActionEvent e) {;}});

    InputMap inputMap = textPane.getInputMap ();
    inputMap.put (KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),
                  "clear-selection");
    inputMap.put (KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0),
                  "delete-annotation");
    inputMap.put (KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,0),
                  "delete-annotation");
    inputMap.put (KeyStroke.getKeyStroke(KeyEvent.VK_TAB,0),
                  "raise-annotation");
    inputMap.put (KeyStroke.getKeyStroke(KeyEvent.VK_BACK_QUOTE,0),
                  "toggle-ligature");

    //System.err.println (Arrays.asList(actionMap.allKeys()));
    //System.err.println (Arrays.asList(inputMap.allKeys()));

    // special caret: will be set by frame if other mode desired
    AutoSelectCaret asc = new AutoSelectCaret ();
    asc.setSelectionPainter (new LineHighlightPainter (null, 10, 170));
    asc.setForcedVisible(Jawb.getPreferences().getBoolean(CARET_VISIBLE_KEY));
    textPane.setCaret (asc);
    
    // action model which will keep track of which actions are
    // available.
    actionModel = kit.getActionModel();
    
    // palette which will surplant all the other action means
    if (DEBUG > 0)
      System.err.println("MTPane constructor creating MTPalette");
    palette = new MainTextPalette (kit, selectedLexAnnots);
    textPane.addMouseListener (palette);
    textPane.addCaretListener (actionModel);

    // create a mention menu, add it to the text pane, and 
    // show it when relevant. the palett is key to manipulating it
    contextMenu = new MainTextContextMenu (true, "text context");
    disabledContextMenu = new MainTextContextMenu (false, "text context");
    
    // popup listener
    MouseInputAdapter mouser = new TextMouseListener ();
    textPane.addMouseListener(mouser);
    textPane.addMouseMotionListener(mouser);

    // annotation mouse listener
    annotMouseListener = new TextAnnotationMouseListener();

    annotationInspector = new JTextPane ();
    annotationInspector.setEditable (false);
    annotationInspector.setFocusable (false);
    annotationInspector.setBackground (Color.getColor ("control"));

    // something to show when we've got no doc!
    noDoc = new JLabel (Jawb.getIcon ("splash.gif", null));
    noDoc.setBackground (Color.white);
    noDoc.setOpaque (true);
    add (noDoc, BorderLayout.CENTER);
  }

  protected JTextPane createJTextPane() {
    return new JTextPane();
  }
  
  // RK 8/2/07 had to make public (was protected) for use in toolkits
  // wishing to create autotag Actions
  public JTextPane getTextPane() {
    return textPane;
  }

  protected Component getTextView() {
    if (DEBUG > 1)
      System.err.println("MTP.getTextView returning textPane");
    return textPane;
  }

  protected Dimension getViewSize() {
    return scrollPane.getViewport().getViewSize();
  }

  protected TaskToolKit getToolKit() {
    return kit;
  }

  protected AnnotationMouseListener getAnnotMouseListener() {
    return annotMouseListener;
  }

  // here to be overridden by subclasses
  protected int getApparentOffset (int realOffset) {
    return realOffset;
  }

  protected int getRealOffset (int apparentOffset) {
    return apparentOffset;
  }
  
  // RK 8/2/07 had to make public (was private) for use in toolkits
  // wishing to create autotag Actions
  public MainTextFinder getFinder() {
    // lazy creation of dialog, because we really want it anchored to the
    // MainTextPanes frame... something it doesn't have while initializing
    if (findDialog == null) {
      if (getRootPane() == null){ // can't yet!
        if (DEBUG > 2)
          System.err.println ("MTP.sFD: no root!");
        return null;
      }
      if (DEBUG > 2)
        System.err.println ("MTP.sFD: creating");
      finder = new MainTextFinder (this);
      findDialog = finder.createDialog (this, "Find in this Document");
    }
    return finder;
  }
  
  /**
   * @return Returns the annotationInspector.
   */
  protected JTextPane getAnnotationInspector() {
    return annotationInspector;
  }

  /**
   * @return Returns the scrollPane.
   */
  protected JScrollPane getScrollPane() {
    return scrollPane;
  }

  public void findAgain (boolean isForward) {
    if (getFinder() != null)
      finder.findText(isForward);
  }

  public void setFindDialogVisible(boolean visible) {
    if (DEBUG > 2)
      System.err.println ("MTP.sFD");
    getFinder();
    // don't show when there's no text! dealt with when setting JawbDocument
    findDialog.setVisible (this.isVisible () && visible);
  }

  /** Display or hide the text action palette. */
  public void setPaletteVisible (boolean visible) {
    if (DEBUG > 2)
      System.err.println ("MTP.sPV");
    // lazy creation of dialog, because we really want it anchored to the
    // MainTextPanes frame... something it doesn't have while initializing
    if (paletteDialog == null) {
      if (getRootPane () == null){ // can't yet!
        if (DEBUG > 2)
          System.err.println ("MTP.sPV: no root!");
        return;
      }
      if (DEBUG > 2)
        System.err.println ("MTP.sPV: creating");
      paletteDialog = palette.createDialog (this, "Available Actions");
    }

    // don't show when there's no text! dealt with when setting JawbDocument
    paletteDialog.setVisible (this.isVisible () && visible);
  }

  /** Get the current state of the paletts visibility. */
  public boolean isPaletteVisible () {
    return paletteDialog.isVisible ();
  }

  /**
   * Change the minimal amount of text iterated over when selecting. Pass
   * through method to the AutoSelectCaret used for the JTextPane.
   * @param mode WORD_SELECT or CHARACTER_SELECT
   */
  public void setSelectionMode (AutoSelectCaret.Mode mode) {
    // just let the ClassCastException fly: it'd be a bug
    AutoSelectCaret caret = (AutoSelectCaret) textPane.getCaret ();
    if (caret.getSelectionMode () != mode) {
      caret.setSelectionMode (mode);
      if (mode == AutoSelectCaret.Mode.WORD)
        modeCursor = WORD_CURSOR;
      else
        modeCursor = CHAR_CURSOR;
      setCursor (modeCursor);
    }
  }
  
  /**
   * Current minimal amount of text iterated over when selecting.
   * @see #setSelectionMode
   * @return AutoSelectCaret.Mode.WORD or AutoSelectCaret.Mode.CHARACTER
   */
  public AutoSelectCaret.Mode getSelectionMode () {
    // just let the ClassCastException fly: it'd be a bug
    AutoSelectCaret caret = (AutoSelectCaret) textPane.getCaret ();
    return caret.getSelectionMode ();
  }

  /**
   * Set the visibility of the caret in the text pane, which is respected, even
   * though the it is not editable.
   */
  public void setCaretVisible(boolean visible) {
    AutoSelectCaret caret = (AutoSelectCaret) textPane.getCaret ();
    caret.setForcedVisible(visible);
  }

  /**
   * Get the visibility of the caret in the text pane (despite not being
   * editable).
   */
  public boolean isCaretVisible() {
    AutoSelectCaret caret = (AutoSelectCaret) textPane.getCaret ();
    return caret.isForcedVisible();
  }
  
  /**
   * Change the cursor displayed when the mouse is over this component.
   */
  public void setCursor (Cursor c) {
    if (c == null ||
        c.equals (Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)))
      c = modeCursor;
    textPane.setCursor (c);
  }
  
  /**
   * Change the font and forward the change to the viewed JawbDocument.
   */
  public void setFont (Font f) {
    super.setFont (f);
    if (textPane != null)
      textPane.setFont (f);
  }
  
  /***********************************************************************/
  /* Implementing the JawbComponent interface */
  /***********************************************************************/

  /**
   * Set the source of the data displayed in this widget, if null is used, an
   * appropriate place holder is displayed.
   */
  public void setJawbDocument (JawbDocument doc) {

    /*
    System.err.println ("MTP.setJawbDocument who called me?");
    Thread.dumpStack();
    */

    if ( (doc != null) && doc.getTask () != kit.getTask ())
      throw new IllegalStateException ("doc.task != toolkit.task");
    
    if (DEBUG > 0)
      System.err.println ("MTP.setJD: displaying doc="+
                          (doc==null?null:doc.getDisplayName(false)));

    if (document != null) { // had a doc before call
      document.getAnnotationModel ()
        .removeAnnotationModelListener (aModelListener);
      document.getSelectedAnnotationModel ()
        .removeSetDataListener (aSelectionListener);
      document.removePropertyChangeListener (docPropListener);
      //      document.getAnnotationMouseModel()
      //	.removeAnnotationMouseListener(annotMouseListener);
      // TODO: this may not be appropriate
      clearAnnotationSelections ();
      resetAnnotationInspector ();
    }

    if (doc == null) { // request to display _no_ doc
      if (document != null) {
        Jawb.getPreferences ().removePropertyChangeListener (prefListener);
        removeComponents();
        textPane.setStyledDocument (emptyDoc);
        add (noDoc, BorderLayout.CENTER);
      }
      palette.setJawbDocument (doc);
      actionModel.setJawbDocument(doc);
      setPaletteVisible (false);
      document = null;
      
    } else { // doc != null ... request to display a doc
      if (document == null) {
        Jawb.getPreferences ().addPropertyChangeListener (prefListener);
        remove (noDoc);
        addComponents();
      }

      // call getActionModel again here in case the kit's context, and
      // thus its action model, has changed -- only needed if this is
      // a ToocaanTK
      if (DEBUG > 0)
        System.err.println("MTPane.setJD refreshing actionModel");
      if (kit instanceof ToocaanToolKit) {
        actionModel = kit.getActionModel();
      }

      palette.setJawbDocument (doc);
      actionModel.setJawbDocument(doc);
      setPaletteVisible (Jawb.getPreferences ()
                         .getBoolean (ACTIONS_DIALOG_VISIBLE_KEY));
      document = doc;
      
      doc.getAnnotationModel ()
        .addAnnotationModelListener (aModelListener);
      doc.getSelectedAnnotationModel ()
        .addSetDataListener (aSelectionListener);
      doc.addPropertyChangeListener (docPropListener);
      //      doc.getAnnotationMouseModel ()
      //	.addAnnotationMouseListener(annotMouseListener);

      if (doc.getComponentOrientation () != null)
        textPane.setComponentOrientation (doc.getComponentOrientation ());
      textPane.setStyledDocument (doc.getStyledDocument ());
      setAnnotationInspectorFont (doc.getFontFamily ());
    }
    validate ();
    repaint ();
  }

  /**
   * Add the default visual components: the scrollPane and the
   * annotationInspector. If {@link #isAnnotationInspectorVisible} is false,
   * however, don't add the annotationInspector.
   */
  protected void addComponents() {
    add (scrollPane, BorderLayout.CENTER);
    if (annotationInspectorVisible) {
      add (annotationInspector, BorderLayout.SOUTH);
    }
  }

  /**
   * Remove the visible components, usually the scrollPane and the
   * annotationInspector.
   */
  protected void removeComponents() {
    remove (scrollPane);
    remove (annotationInspector);
  }

  /**
   * Retrieve the current source of this Compoents data.
   */
  public JawbDocument getJawbDocument () {
    return document;
  }

  /**
   * Retrieve the currently selected lexical annotaions.
   */
  public Set getSelectedAnnots () {
    return Collections.unmodifiableSet (selectedLexAnnots);
  }
  
  /**
   * Returns 'this', which is the actual object displayed.
   */
  public Component getComponent () {
    return this;
  }

  /**
   * Map annotations which are selected in other Jawb Components, to
   * annotations which may be selected in this component.<p>
   *
   * For example, table "mentions" displays 'MENTION' annotations, which are
   * anchored in the text, and table "entities" displays 'ENTITY' annotations
   * which contain MENTIONS, but don't directly refrence the text. When an
   * ENTITY is selected in the "entities" table, to have it's MENTIONs
   * selected in the "mentions" table, you might set the following
   * ItemExchanger for the "mentions" table:
   *
   * <code> 
   *  class EntityExchanger extends ItemExchanger {
   *    LinkedHashSet results = new LinkedHashSet();
   *    public Set exchange (Object item) {
   *      AWBAnnotation annot = (AWBAnnotation) item;
   *      String typeName = annot.getAnnotationType ().getName ().equals;
   *      results.clear ();
   *      if (annot.getAnnotationType ().getName ().equals ("ENTITY")) {
   *        SubordinateSetsAnnotation parent = (SubordinateSetsAnnotation) annot;
   *        AWBAnnotation[] mentions = parent.getSubordinates (MENTION_TYPE);
   *        for (int i=0; i<mentions.length; i++)
   *          results.add (mentions[i]);
   *      }
   *      return results;
   *    }
   *  }
   * </code>
   *
   * @see #getSelectedAnnotationExchanger
   */
  public void setSelectedAnnotationExchanger (AnnotationType type,
                                              ItemExchanger exchanger) {
    exchangers.put (type, exchanger);
  }

  /**
   * Retrieve the ItemExchanger used to map the selections in other jawb
   * components to Annotaitons which may be selected in this component.
   *
   * @see #setSelectedAnnotationExchanger
   */
  public ItemExchanger getSelectedAnnotationExchanger (AnnotationType type) {
    return (ItemExchanger) exchangers.get (type);
  }

  /***********************************************************************/
  /* INSPECTING ANNOTATIONS
  /***********************************************************************/
  
  private List lastInspectedAnnots = null;
  private Point lastInspectedPoint = new Point();

  /**
   * This will force inspectAnnotations to be redisplayed, even if the
   * point inspected returns the same list as the last inspection. This was a
   * needed because our 'caching' mechanism was simply to save a reference to
   * the list returned from documet.getAnnotationsAt.  1) this would maintain
   * reference to a documents data after documents had been removed, and 2)
   * when raising annotation at point, the document only reorders lists for
   * each Element, thus equalit to the 'last' list was true, thus no
   * reinspection
   */
  private void resetAnnotationInspector () {
    lastInspectedAnnots = null;
  }
  
  private void setAnnotationInspectorFont (String family) {
    annotationInspector.setFont (new Font (family, Font.PLAIN, 11));
  }
  
  /**
   * @return Returns the showAnnotationInspector.
   */
  protected boolean isAnnotationInspectorVisible() {
    return annotationInspectorVisible;
  }

  /**
   * @param showAnnotationInspector The showAnnotationInspector to set.
   */
  protected void setAnnotationInspectorVisible(boolean showAnnotationInspector) {
    this.annotationInspectorVisible = showAnnotationInspector;
  }

  /**
   * Display the annotations under the specifiec point using the
   * TextComponent's viewToModel conversion. This caches data so that minimal
   * redundant work is performed.  If a null point is passed, the last point
   * passed will be reinspected.
   * @param removeOnly <code>true</code> if annotations should only be
   * removed from inspector
   */
  private void inspectAnnotations (Point point, boolean removeOnly) {
    if (! isAnnotationInspectorVisible())
      return;
    
    // check whether the annotation must be "visible" to be included in 
    // the list
    boolean visibleOnly = Jawb.getPreferences().getBoolean(ANNOTATION_INSPECTOR_REQUIRE_VISIBLE);

    if (point != null)
      lastInspectedPoint.setLocation (point);
    else
      point = lastInspectedPoint;
    int offset = textPane.viewToModel (point);
    List annots = null;

    // makes sure we're actually next to the point we're clicking (ie, in
    // text, not whitespace at end of lines)
    try {
      Rectangle r = textPane.modelToView(offset);
      // this should pay attention to font size to be correct
      int dist = Math.max (Math.abs(r.x - point.x),
                           Math.abs(r.y - point.y)-r.height);
      // here we check w/in tolerance, before searching, but '5' is arbitrary
      if (Math.abs(dist) <= 5)
        annots = document.getAnnotationsAt (offset);
      
    } catch (BadLocationException x) {/*annots is null*/ }

    if (annots == null || annots.isEmpty () || removeOnly) {
      if (lastInspectedAnnots != null || removeOnly) {
        lastInspectedAnnots = annots;
      }
      annotationInspector.setText ("");
    } else if (! annots.equals (lastInspectedAnnots)) {
      if (DEBUG > 1)
        System.err.println("MTP Inspect new annots");

      lastInspectedAnnots = annots;
      
      StringBuffer descriptions = new StringBuffer();
      Iterator iter = annots.iterator ();
      while (iter.hasNext ()) {
        // TODO: clean up this hack around TextExtentAttributes
        AWBAnnotation annot = (AWBAnnotation) iter.next ();
        // RK 9/21/09
        // if only showing visible annotations, then 
        // figure out if the annotation is visible according to the
        // user's task prefs (such as:)
        // task.org.mitre.muc.mixed.highlight.<unknown>.visible=false
        // task.org.mitre.muc.mixed.ANAPH.visible=false
        if (visibleOnly) {
          if (DEBUG > 1)
            System.err.println("MTP:inspect " + annot + " only if visible");
          JawbDocument jd = getJawbDocument(); // could just access document 
                                               // directly?
          Task task = jd.getTask();
          Object constraint;
          if (task instanceof ToocaanTask)
            constraint = jd;
          else
            constraint = null;
          String hk = task.getHighlightKey(annot, constraint);
          if (hk == null) {
            hk = "highlight.<unknown>";
          }
          String prefsKey = "task." + task.getName() + "." + hk + ".visible";
          boolean isVisible = Jawb.getPreferences().getBoolean(prefsKey, true);
          if (DEBUG > 1)
            System.err.println("MTP.inspAnnots checking " + prefsKey +
                               " result: " + (isVisible?"true":"false"));
          if (!isVisible) {
            // skip this annotation
            continue;
          }
        }

        descriptions.append ('<').append (annot.getATLASType ().getName());
        String text = null;

        if (annot instanceof TextExtentRegion) {
          text = getDisplayText(((TextExtentRegion)annot).getTextExtent ());
          descriptions.append (" \"").append (text).append ("\"");
          
        } else if (annot instanceof NamedExtentRegions) {
          text = 
            getDisplayText(((NamedExtentRegions)annot).getTextExtent ("full"));
          descriptions.append (" full=\"").append (text).append ("\"");
          text = 
            getDisplayText(((NamedExtentRegions)annot).getTextExtent ("head"));
          descriptions.append (" head=\"").append (text).append ("\"");
        }
        
        String[] keys = annot.getAttributeKeys ();
        for (int i=0; i<keys.length; i++) {
          Object value = null;
          // TODO: the first two work around a bug in PhraseAnnotation
          // TODO: the next three may indicate aproblem with the design
          // The last two skip empty attributes per request: RFE #408
          if (keys[i].equals ("end") || keys[i].equals ("start") ||
              keys[i].endsWith (TextExtentRegion.TEXT_EXTENT) ||
              keys[i].endsWith (TextExtentRegion.TEXT_EXTENT_START) ||
              keys[i].endsWith (TextExtentRegion.TEXT_EXTENT_END) ||
              (value = annot.getAttributeValue (keys[i])) == null ||
              value.equals (""))
            continue;
          
          descriptions.append (' ').append (keys[i]).append ("=\"");
          descriptions.append (value).append ('"');
        }
        descriptions.append (">");
        if (iter.hasNext ())
          descriptions.append ("\n");
      }
      annotationInspector.setText (descriptions.toString());
    }
  }
  
  private String getDisplayText(String input) {
    String text = input.replaceAll ("(\n\r|\n|\r)"," ");
    int len = text.length();
    if (len > 35) {
      text = text.substring(0,15) + "..." + text.substring(len-15, len);
    }
    return text;
  }

  
  /***********************************************************************/
  /* Tracking selected annotations
  /***********************************************************************/

  /** Paints the special highlights for selected annotations */
  private Highlighter.HighlightPainter selectedAnnotPainter =
    new TextAnnotationHighlightPainter (Color.black, Color.white);

  
  private Highlighter.HighlightPainter reconciliationSegmentPainter =
    new TextAnnotationHighlightPainter 
    (Jawb.getPreferences().getColor(RECONCILIATION_HIGHLIGHT_KEY,
                                    Color.red), 
     Color.black);

  public void setReconciliationHighlightColor(Color color) {
    reconciliationSegmentPainter =
      new TextAnnotationHighlightPainter (color, Color.black);
  }

    

  /** removes any existing highlights, and uses the
   * reconciliationSegmentPainter to add a new one from start to
   * end. */
  public void highlightReconciliationSegment(int start, int end) {
    Highlighter h =  textPane.getHighlighter ();
    try {
      h.removeAllHighlights ();
      h.addHighlight (start, end, reconciliationSegmentPainter);
    } catch (BadLocationException x) {
      // never mind then 
    }
  }

  private Set tempSelectionSet = new LinkedHashSet ();

  protected Highlighter.HighlightPainter getSelectedAnnotPainter() {
    return selectedAnnotPainter;
  }
  
  /** Added to the selection model of a document */
  private class AnnotSelectionListener implements SetDataListener {
    public void elementsAdded (SetDataEvent e) {
      updateAnnotationSelections (true);
    }
    public void elementsRemoved (SetDataEvent e) {
      if (DEBUG > 1)
        System.err.println("AnnSelList.eltsRemoved updAnnSels(false)");
      updateAnnotationSelections (false);
    }
  }

  /**
   * This should only be called by updateAnnotationSelections, or when there
   * is no document from which to get the selectedAnnotationsModel. Otherwise
   * the 'displayed' selection will be out of sync with the selection model.
   */
  private void clearAnnotationSelections () {
    // remove selection highlights
      if (DEBUG > 5)
	  Thread.dumpStack();
    textPane.getHighlighter ().removeAllHighlights ();
    selectedLexAnnots.clear ();
    repaint (); // otherwise some of the 'selection' doesn't repaint
  }
  
  private void updateAnnotationSelections (boolean jumpToAnnot) {
    if (DEBUG > 0)
      System.err.println ("MTP.updAnnotSelection jumpToAnnot=" + jumpToAnnot);
    if (DEBUG > 5)
      Thread.dumpStack();
    clearAnnotationSelections ();
    clearTextSelection ();

    Highlighter h =  textPane.getHighlighter ();
    SetModel model = document.getSelectedAnnotationModel();
    boolean oneVisible = false;
    int start = -1;
    int end = -1;

    // make necessary exchanges
    Iterator iter = model.iterator ();
    if (! exchangers.isEmpty ()) {
      tempSelectionSet.clear ();
      while (iter.hasNext()) {
        Annotation annot = (Annotation) iter.next ();
        if (DEBUG > 3)
          System.err.println("\t***MTP.updAnnotSel model interator contained " +
                             annot);                             
        ItemExchanger exchanger =
          (ItemExchanger) exchangers.get (annot.getAnnotationType());
        if (exchanger != null) {
	    if (DEBUG > 0)
		System.err.println("About to attempt exchange.exchange(annot) on annot = " + annot.toString());
	    tempSelectionSet.addAll (exchanger.exchange (annot));
	}
        else {
	    if (DEBUG > 0)
		System.err.println("Not attempting to exchange; tempSelectionSet.add annot = " + annot.toString());
	    tempSelectionSet.add (annot);
	}
        if (DEBUG > 3)
          System.err.println("\t*****MTP.updAnnotSel after exchangers, " +
                             "tempSelectionSet has " + 
                             tempSelectionSet.size() + " items");
      }
      iter = tempSelectionSet.iterator ();
    }
    while (iter.hasNext ()) {
      long tstart = System.currentTimeMillis();
      long t1=0,t2=0,t3=0,t4=0;
      AWBAnnotation annot = (AWBAnnotation) iter.next ();
      if (DEBUG > 2)
        System.err.println ("\tMTP.updAnnotSelection: select annot " + annot +
                            " of type " + annot.getAnnotationType());

      if (annot instanceof TextExtentRegion) {
       
        start = 
	  getApparentOffset(((TextExtentRegion)annot).getTextExtentStart ());
        end   = 
	  getApparentOffset(((TextExtentRegion)annot).getTextExtentEnd ());
        if (DEBUG > 3)
          System.err.println("\tis TextExtentRegion start=" + start + " end=" +
                             end);
      } else if (annot instanceof NamedExtentRegions) {
        // TODO: stop hard coding for RDC!
        if (DEBUG > 2)
          System.err.println ("\tMTP.updAnnotSelection: text="+
              annot.getAttributeValue("head.TextExtent"));
        start = 
	  getApparentOffset(((NamedExtentRegions)annot).getTextExtentStart("full"));
        end   = 
	  getApparentOffset(((NamedExtentRegions)annot).getTextExtentEnd  ("full"));
	if (DEBUG > 2)
	    System.err.println ("MTP.updAnnotSelection: NamedExtentRegions start =" +
				start);
        
      } else {
	if (DEBUG > 2)
	    System.err.println ("MTP.updAnnotSelection: neither!");
        continue;
      }
      // track just the lexical annots currently selected
      selectedLexAnnots.add (annot);
      
      try {
        if (DEBUG > 5) {
          System.err.println("h.addHighlight args:");
          System.err.println("  start = " + start);
          System.err.println("  end   = " + end);
          System.err.println("  getSelectedAnnotPainter() = " + getSelectedAnnotPainter());
          Thread.dumpStack();
        }
        t1 = System.currentTimeMillis();
        h.addHighlight (start, end, getSelectedAnnotPainter());
        t2 = System.currentTimeMillis();
        if (DEBUG > 2)
          System.err.println("\tMTP.updAnnotSel: h= " + h + " start= " +
                             start + " end=" + end + " painter=" +
                             getSelectedAnnotPainter());
        // TODO: this might futz up z-ordering is there a solution
        document.raiseAnnotation (annot, start, end);
        t3 = System.currentTimeMillis();

        if (DEBUG > 2)
          System.err.println("MTP.updAnnotSel: jumpToAnnot=" + jumpToAnnot +
                             " oneVisible=" + oneVisible);
        // Wait till all selected are checked, so we don't 'jump around' if
        // not needed
        // RK 07/07/09 check for oneVisible first so we don't have to bother
        // with all this if already one is visible
        if (jumpToAnnot && !oneVisible &&
            textPane.getVisibleRect ().contains (textPane.modelToView (start)))
          oneVisible = true;
        t4 = System.currentTimeMillis();
      } catch (BadLocationException x) {
        x.printStackTrace ();
      }
      if (DEBUG > 2)
        System.err.println("MTP.updAnnotSel: find: " +
                           String.valueOf(t1-tstart) +
                           " highlight: " + String.valueOf(t2-t1) +
                           " raise: " + String.valueOf(t3-t2) +
                           " checkVis: " + String.valueOf(t4-t3));
    }
    // if none were visible, scroll to the last
    if (jumpToAnnot && !oneVisible && start >= 0) {
      if (DEBUG > 0)
        System.err.println("MTP.updAnnotSel scroll to " + start);
      try {
        textPane.scrollRectToVisible (textPane.modelToView (start));
      } catch (BadLocationException x) { /* c'est la vie */ }
    }
    if (DEBUG > 2) {
      System.err.println("MTP.updAnnotSels: done");
    }
  }

  /***********************************************************************/
  /* Implementing the AnnotationModel Interface */
  /***********************************************************************/

  /** Invoked after an annotation has been created. */
  protected class AnnotModelListener implements AnnotationModelListener {

    // makes sure that autotag is not triggered recursively for each
    // annotation created by autotagging!
    private boolean doingAutotag = false;

    private void setDoingAutotag(boolean autotag) {
      System.err.println("setting doingAutotag to " + autotag);
      doingAutotag = autotag;
    }

    public void annotationCreated (AnnotationModelEvent e) {
      resetAnnotationInspector ();
      boolean shiftOnly = Jawb.getPreferences().getBoolean(ANNOTATION_INSPECTOR_REQUIRE_SHIFT_KEY);
      inspectAnnotations (null, shiftOnly);


      System.err.println("MTP.AnnotModelListener.annotCreated: " +
                         e.getAnnotation());

    }

    /** Invoked after an annotation has been deleted. */
    public void annotationDeleted (AnnotationModelEvent e) {
      if (DEBUG > 1)
        System.err.println("AnnotModList.annDel: resetting inspector");
      resetAnnotationInspector ();
      boolean shiftOnly = Jawb.getPreferences().getBoolean(ANNOTATION_INSPECTOR_REQUIRE_SHIFT_KEY);
      inspectAnnotations (null, shiftOnly);
    }

    /** Invoked after an annotation has been changed. */
    public void annotationChanged (AnnotationModelEvent e) {
      AnnotationModelEvent.AnnotationChange change = e.getChange();
      AWBAnnotation changedAnnot = e.getAnnotation();
      if (DEBUG > 2) {
        System.err.println("MTP.AnnotModelListener.annotChanged: " +
                           changedAnnot + " change: " +
                           change.getPropertyName() + " from: " +
                           change.getOldValue() + " to: " +
                           change.getNewValue());
      }
      String id = changedAnnot.getId().getAsString();
      String type = changedAnnot.getAnnotationType().getName();
      String property = change.getPropertyName();
      String oldVal = prettyPrint(change.getOldValue());
      String newVal = prettyPrint(change.getNewValue());
      // LOG iff oldVal is non-null and non-empty
      // if oldVal is null, this is normally the initial creation of the annot
      // TODO in tasks where a null value is valid and can be freely changed
      // to and from, this will miss some changes
      // Also in cases where default value is non-null, this will still record
      // a change from default to first real value when the annot is created
      // ideally would like to turn this off during annot creation, but that
      // is non-trivial
      if (oldVal != null && !oldVal.equals("")) {
        if (changedAnnot instanceof TextExtentRegion) {
          // also skip the case where oldVal is "[-1, -1]"
          if (!oldVal.equals("[-1, -1]")) {
            int start = ((TextExtentRegion)changedAnnot).getTextExtentStart();
            int end   = ((TextExtentRegion)changedAnnot).getTextExtentEnd();
            Jawb.getLogger().info(JawbLogger.LOG_CHANGE_ANNOT,
                                  new Object [] { type, id, start, end, 
                                                  property,
                                                  oldVal, newVal });
          }
        } else {
          Jawb.getLogger().info(JawbLogger.LOG_CHANGE_ANNOT,
                                new Object [] { type, id, property,
                                                oldVal, newVal });
        }
      }

      if (!doingAutotag) {
        clearTextSelection();
        if (selectedLexAnnots.contains (change.getAnnotation()))
          updateAnnotationSelections (false);
        if (DEBUG > 2)
          System.err.println("text selection cleared");
      } else {
        if (DEBUG > 2)
          System.err.println("doing autotag so NOT clearing text selection");
      }


      // invoke autotag here if appropriate
      // we do it here and not under annotation created because we need
      // to wait until the start and end anchors have been set

      // TODO -- need to figure out how to distinguish new annots
      // from real changes to annots!!

      // I don't understand whey the PropertyName is null when the
      // extents get set, but that does seem to be the right way to
      // make sure we're only triggering this on the "last" change
      // when the extents do get set.

      // Update 3/12/13 RK: Apparently the PropertyName thing got fixed
      // some time ago with the side effect of breaking autotag
      // changing this to check for TextExtents property change

      // BUG: Unfortunately this seems to be the case for both head
      // and full extent setting... we need a better way to trigger
      // for complicated annots, or perhaps we need to explicitly call
      // autotag from an action in these cases so that we can do it
      // after the attributes are set up  TODO 3/12/13 -- check if
      // this is still necessary?


      Preferences prefs = Jawb.getPreferences();
      String checkMode = prefs.getPreference(Preferences.AUTOTAG_MODE_KEY);
      if (DEBUG > 0)
        System.err.println("MTP checking autotag mode: " +
                           (checkMode == null?"null":checkMode));
      final String autotagMode = 
        (checkMode == null?"None":checkMode);
      if (DEBUG > 2)
        System.err.println("MTP autotagMode: " + autotagMode);
      if (checkMode == null) {
        if (DEBUG > 0)
          System.err.println ("MTP: Setting autotag mode to None");
        Jawb.getPreferences().setPreference(Preferences.AUTOTAG_MODE_KEY, 
                                            "None");
      }
      final boolean autotagForward = 
        prefs.getBoolean(Preferences.AUTOTAG_FORWARD_ONLY_KEY);
      final boolean autotagUntaggedOnly = 
        prefs.getBoolean(Preferences.AUTOTAG_UNTAGGED_ONLY_KEY);
      
      final AWBAnnotation annot = e.getAnnotation();
      final Autotagger autoTagger = kit.getTask().getAutotagger();

      if (DEBUG > 2) {
        System.err.println("Checking if we want to invoke autotag:");
        System.err.println("isTriggerAutoTaggable:" +
                           autoTagger.isTriggerAutoTaggable(annot));
        System.err.println("change property name: " +
                           change.getPropertyName());
        System.err.println("autotagMode: " + autotagMode);
        System.err.println("autotag forward only?: " + autotagForward);
        System.err.println("autotag untagged only?: " + autotagUntaggedOnly);
      }

      if (!doingAutotag && autoTagger.isTriggerAutoTaggable(annot) &&
          ("TextExtents".equals(change.getPropertyName())) &&
          !"None".equalsIgnoreCase(autotagMode)) {
        if (DEBUG > 2) {
          System.err.println("We want to invoke autotag -- creating runnable");
          System.err.println("property changed: " + change.getPropertyName());
          System.err.println("annot changed: " + annot);
        }

        Runnable invokeAutotag = new Runnable() {
            public void run() {
              // code to retrieve doc text taken from MainTextFinder
              if (DEBUG > 2)
                System.err.println("in invokeAutotag.run()");
              String docText = null;
              try {
                // I get the text from the document, because retrieveing from
                // tp.getText() seems to convert newlines to the current system.
                Document doc = getTextPane().getDocument();
                docText = doc.getText(0, doc.getLength());
              } catch (BadLocationException x) {
                return; // impossible?
              }

              setDoingAutotag(true);
              //Autotagger autoTagger = kit.getTask().getAutotagger();
              if (autoTagger != null) {
                System.err.println("Calling doAutotag from within runnable");
                autoTagger.doAutotag(getJawbDocument(), docText, getFinder(), 
                                     textPane, annot, autotagMode,
                                     autotagForward, autotagUntaggedOnly);
                System.err.println("doAutotag has returned in runnable");
              }
              setDoingAutotag(false);
            }
          };
        if (DEBUG > 2)
          System.err.println("runnable created -- invoking for later");
        SwingUtilities.invokeLater(invokeAutotag);
        // System.err.println("runnable created -- invoking in new thread");
        // new Thread(invokeAutotag).start();
      }


    }

    /** Invoked after an annotation has had subannotations added. */
    public void annotationInserted (AnnotationModelEvent e) {
      clearTextSelection();
    }

    /** Invoked after an annotation has had subannotations removed. */
    public void annotationRemoved (AnnotationModelEvent e) {
      if (DEBUG >1)
        System.err.println("AnnotModelListener.annotRemoved clearTextSel()");
      clearTextSelection();
    }

    private String prettyPrint(Object obj) {
      if (obj == null)
        return "null";

      if (!obj.getClass().isArray())
        return obj.toString();

      // if we get here it is an array
      return Arrays.toString((Object[])obj);
    }
  }
  
  /***********************************************************************/
  /* MOUSE LISTENER */
  /***********************************************************************/

  
  private Point tmpPt = new Point ();
  
  class MainTextContextMenu extends TextContextMenu {
    
    private static final int DEBUG = 0;

    protected boolean enabled = true;

    public MainTextContextMenu(boolean enabled) {
      super();
      this.enabled = enabled;
    }
    
    public MainTextContextMenu(boolean enabled, String s) {
      super(s);
      this.enabled = enabled;
    }

    public boolean addActionSets(ActionModel actionModel) {
      ActionCollection textActions = actionModel.getGroupedTextActions();
      // Add them all, enabled or not.
      boolean somethingAdded = addActionSet(textActions, false);
      if (enabled) {
        // Originally, we were only checking the extent actions,
        // but it seems that we really ought to look at the
        // annotation actions as well.
        ActionCollection annotActions = actionModel.getGroupedAnnotationActions();
        Iterator listIter = annotActions.iterator();
        while (listIter.hasNext()) {
          Action ap = (Action) listIter.next();
          if (ap.isEnabled()) {
            contextMenu.addSeparator();
            somethingAdded = true;
            addActionSet(annotActions);
            break;
          }
        }
        // We're going to add the enabled extent actions, but
        // we need to check first to see if any of the actions
        // are enabled, in which case we'll add a separator first.
        // This is kind of wasteful - I should be able to get
        // back a list of just the enabled ones, but I don't
        // want that to involve also having an iterator. Sigh...
        ActionCollection modActions = actionModel.getGroupedExtentModificationActions();
        listIter = modActions.iterator();
        while (listIter.hasNext()) {
          Action ap = (Action) listIter.next();
          if (ap.isEnabled()) {
            contextMenu.addSeparator();
            somethingAdded = true;
            addActionSet(modActions);
            break;
          }
        }
      }
      return somethingAdded;
    }

  }
  
  class TextMouseListener extends MouseInputAdapter {
    
    private static final int DEBUG = 0;
    
    /**
     * Configure the context menu for where the mouse is now, the text swiped,
     * and annotations selected, then display it at the point of the mouse
     * event.
     *
     * This changes with the advent of the AnnotationMouseEvent and Model.
     * 
     * If text is swiped, the Mouse Event should trigger the popup with
     * text-swipe-specific options.
     * 
     * Otherwise, if any Annotation(s) are selected,
     * AnnotationMouseEvent(s) will be triggered for each selected
     * Annotation.  Failing that, if the mouse is over any
     * Annotation(s), AnnotationMouseEvent(s) will be triggered for
     * each of them.
     *
     * The popup menu mediated here will contain only
     * text-swipe-specific options.  Annotation-specific options will
     * be handled by task-specific listeners to the
     * AnnotationMouseModel
     */
    private void showPopupMenu (MouseEvent e) {
      if (DEBUG > 2)
        System.err.println("MTP: Showing enabled popup menu - enabled: " +
                           contextMenu.isEnabled() + " subcomponents: " +
                           contextMenu.getComponents());
      if (DEBUG > 4) {
        Component[] actions = contextMenu.getComponents();
        for (int i=0; i<actions.length; i++) {
          System.err.println("\taction: " + actions[i]);
        }
      }
      contextMenu.showMenu(e, actionModel);
      
    }
    
    private void showDisabledPopupMenu (MouseEvent e) {
      if (DEBUG > 2)
        System.err.println("MTP: Showing disabled popup menu - enabled: " +
                           disabledContextMenu.isEnabled());
      disabledContextMenu.showMenu(e, actionModel);
    }
    
    
    public void mousePressed (MouseEvent e) {
      if (DEBUG > 0) 
        System.err.println ("MTP.mPressed:");
      JawbComponent component = MainTextPane.this;
      // save a snapshot of what annots are under the mouse at the
      // moment of the original mouse event
      tmpPt.x = e.getX();
      tmpPt.y = e.getY();
      int offset = textPane.viewToModel (tmpPt);
      // for subclasses where real and apparent offsets are not
      // identical, getAnnotationsAt requires an apparent offset
      // but, viewToModel converts to a real offset, so convert
      // back here? Let's make viewToModel leave well enough alone
      // instead
      List annots = getSelectableAnnotationsAt (document, offset);
      
      if (DEBUG > 2) {
        System.err.println ("MTP.mPressed over " + annots.size() + " annots");
        System.err.println ("MTP.mPressed with selection from " + 
                            textPane.getSelectionStart() + " to " +
                            textPane.getSelectionEnd());
      }

      // if text is selected, if this is the popup trigger, show the
      // annotation creation popup and consume the mouse event
      if (textPane.getSelectionStart() < textPane.getSelectionEnd()) {
        // text is selected
        if (e.isPopupTrigger()) {
          showPopupMenu (e);
          e.consume(); 
        }
      } 
      
      // send AMEs for Annots under the mouse when the mouse event occurred
      // or a null-annot AME if there were none
      if (annots != null && ! annots.isEmpty ()) {
        // mouse is over annotation(s)
        if (DEBUG > 1) 
          System.err.println ("MTP.mPressed: mouse over " +
              annots.size() + " annotations");
        document.getAnnotationMouseModel().fireAnnotationPressedEvents
        (e, annots, component);
      } else {
        // mouse is not in annotations
        document.getAnnotationMouseModel().fireAnnotationPressedEvent
        (e, null, component);
      }
      
      // see if we need to do a disabled popup menu -- this should
      // happen if this is the popup trigger and no popup has been
      // shown yet -- we will know if a popup has been shown by the
      // fact that the event is consumed
      if (e.isPopupTrigger() && !e.isConsumed()) {
        if (DEBUG > 2)
          System.err.println("MTP.mPressed displaying disabled popup");
        showDisabledPopupMenu (e);
        e.consume();
      }
      
    }
    
    public void mouseReleased (MouseEvent e) {
      
      if (DEBUG > 0) 
        System.err.println ("MTP.mReleased: (DEBUG=" + DEBUG + ") pop= " + e.isPopupTrigger());

      if (DEBUG > 5) {
          System.err.println ("MTP.mReleased who called me?");
          Thread.dumpStack();
      }

      JawbComponent component = MainTextPane.this;
      // save a snapshot of what annots are under the mouse at the
      // moment of the original mouse event
      tmpPt.x = e.getX();
      tmpPt.y = e.getY();
      if (DEBUG > 2) {
        System.err.println ("MTP.mReleased at (" + tmpPt.x + "," +
                            tmpPt.y + ")");
        System.err.println ("MTP.mReleased dot=" + 
                            textPane.getCaret().getDot() + " mark=" +
                            textPane.getCaret().getMark());
      }

      int offset = textPane.viewToModel (tmpPt);
      List annots = getSelectableAnnotationsAt (document, offset);
      
      if (DEBUG > 2) {
        System.err.println ("MTP.mReleased over " + annots.size() + " annots");
        System.err.println ("MTP.mReleased dot=" + 
                            textPane.getCaret().getDot() + " mark=" +
                            textPane.getCaret().getMark());
      }

      
      // if text is selected, if this is the popup trigger, show the
      // annotation creation popup and consume the mouse event
      if (textPane.getSelectionStart() < textPane.getSelectionEnd()) {
        // text is selected
        
        if (DEBUG >2)
          System.err.println("MTP.mReleased with text selected start: "+
                             textPane.getSelectionStart() + " end: " +
                             textPane.getSelectionEnd());
        if (e.isPopupTrigger()) {
          showPopupMenu (e);
          e.consume(); 
        }
      } 
      
      // send AMEs for Annots under the mouse when the mouse event occurred
      // or a null-annot AME if there were none
      if (annots != null && ! annots.isEmpty ()) {
        // mouse is over annotation(s)
        if (DEBUG > 1)
          System.err.println ("MTP.mReleased: mouse over " +
              annots.size() + " annotations");
        document.getAnnotationMouseModel().fireAnnotationReleasedEvents
        (e, annots, component);
      } else {
        // mouse is not in annotations
        document.getAnnotationMouseModel().fireAnnotationReleasedEvent
        (e, null, component);
      }
      
      if (DEBUG > 3)
        System.err.println ("MTP.mReleased: after firing AMEs: e.isConsumed= " +
            e.isConsumed());
      // see if we need to do a disabled popup menu -- this should
      // happen if this is the popup trigger and no popup has been
      // shown yet -- we will know if a popup has been shown by the
      // fact that the event is consumed
      if (e.isPopupTrigger() && !e.isConsumed()) {
        if (DEBUG > 2) 
          System.err.println("MTP.mReleased - show disabled popup");
        showDisabledPopupMenu (e);
        e.consume();
      }
      
    }


    public void mouseClicked (MouseEvent e) {
      
      if (DEBUG > 0) 
        System.err.println ("MTP.mClicked:");
      //     if (SwingUtilities.isLeftMouseButton(e)) {
      tmpPt.x = e.getX();
      tmpPt.y = e.getY();
      int offset = textPane.viewToModel (tmpPt);
      List annots = getSelectableAnnotationsAt (document, offset);
      
      // fire the appropriate annotation mouse event(s) 
      // use annotMouseListener as default if it was over any annots,
      // so that the annot will be selected if no other listener
      // consumes the AME (this allows, for example, a subordinate
      // assignor waiting for a click on a superordinate to grab the
      // click first)
      if (annots != null && ! annots.isEmpty ()) {
        document.getAnnotationMouseModel().fireAnnotationClickedEvents
        (e, annots, MainTextPane.this, getAnnotMouseListener());
      } else {
        document.getAnnotationMouseModel().fireAnnotationClickedEvent
        (e, null, MainTextPane.this);
      }
      
    }

    // TODO: to do this properly, mouseMoved Event should trigger
    // AnnotationMouseMoved plus AnnotationMouseEntered and 
    // AnnotationMouseExited events as applicable.
    public void mouseMoved (MouseEvent e) {
      tmpPt.x = e.getX();
      tmpPt.y = e.getY();
      int offset = textPane.viewToModel (tmpPt);
      List annots = document.getAnnotationsAt (offset);
      // gwilliam: Users got annoyed when trying to
      // view/swipe/annotate text at bottom of text pane -- if there
      // were any annotations present, the inspector would pop up and
      // obscure the text user was trying to interact with. So now we
      // offer the option in Edit menu to only inspect annotations
      // when the SHIFT key is held down.
      int onmask = MouseEvent.SHIFT_DOWN_MASK;
      int offmask = 0xffffffff;
      boolean shiftOnly = Jawb.getPreferences().getBoolean(ANNOTATION_INSPECTOR_REQUIRE_SHIFT_KEY);
      boolean removeOnly = shiftOnly && (e.getModifiersEx() & (onmask | offmask)) != onmask;
      // this should be done whether or not there are any annots in 
      // the set, so do it here and not in an AnnotMouseListener
      inspectAnnotations(tmpPt, removeOnly);
      
      if (annots == null || annots.isEmpty ())
        textPane.setCursor (modeCursor);
      else {
        textPane.setCursor
        (Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        // since there were annots, pass it on as well
        document.getAnnotationMouseModel().fireAnnotationMotionEvents
        //(e, annots, (JawbComponent)e.getComponent());
        (e, annots, MainTextPane.this);
      }
    }

    // All Annotations at the given offset are selectable if the
    // select invisible property is true.  Otherwise, only visible
    // annotations at the given offset are returned.
    // RK 12/18/2010 Now can set a .selectable property for any highlight
    // key independently as well
    private List getSelectableAnnotationsAt(JawbDocument document, int offset) {

      List annots = document.getAnnotationsAt (offset);

      // if we are not allowing users to select invisible annotations,
      // we must remove all non-visible annots from the list.
      if (annots != null && ! annots.isEmpty ()) {
      
        if (DEBUG > 1)
          System.err.println ("MTP.mClicked: "+annots.size()+" annots");
      
        // get Select Invisible key from preferences -- default to false
        Task task = kit.getTask();
        Preferences prefs = Jawb.getPreferences();
        boolean selectInvisible = prefs.getBoolean("task."+task.getName()+
                                                   ".selectinvisible",
                                                   false);

        // make a modifiable copy of annots, which we will modify and
        // then make unmodifiable again before passing into
        // fireAnnotationClickedEvents
        if (DEBUG > 1)
          System.err.println("MTP.getSelAnnsAt: checking for unselectables");
        if (DEBUG > 3)
          System.err.println("Prefs: " + prefs);
        ArrayList modifiableAnnotsList = new ArrayList(annots);
      
        Iterator annotsIter = annots.iterator();
        while (annotsIter.hasNext()) {
          AWBAnnotation annot = (AWBAnnotation) annotsIter.next();
          // for the constraint, we can put
          // in full for all since it will be ignored where
          // irrelevant
          // RK 7/24/10 in the case where the task is a ToocaanTask,
          // we need to pass in the JawbDocument as the constraint
          JawbDocument jd = getJawbDocument(); // could just access document 
          // variable directly?
          Object constraint;
          if (task instanceof ToocaanTask)
            constraint = jd;
          else
            constraint = "full";
          String hk = task.getHighlightKey(annot, constraint);
          if (hk == null)
            hk = Preferences.UNKNOWN_HIGHLIGHT_KEY;
          if (DEBUG > 3)
            System.err.println("MTP.getSelectableAnnsAt: hk=" + hk);
          String visibleKey = "task."+task.getName()+"."+hk+".visible";
          String selectableKey = "task."+task.getName()+"."+hk+".selectable";
          
          if ((!selectInvisible && !prefs.getBoolean(visibleKey, true)) ||
              !prefs.getBoolean(selectableKey, true)) {
            modifiableAnnotsList.remove(annot);
            if (DEBUG > 1)
              System.err.println("MTP.getSelectableAnnsAt: annot excluded: " 
                                 + annot + "\n\tvisible: " +
                                 prefs.getBoolean(visibleKey, true) +
                                 "\n\tselectable: " +
                                 prefs.getBoolean(selectableKey, true));
          }
        }
        return Collections.unmodifiableList(modifiableAnnotsList);
      } 

      // we get here if annots was null or empty  -- in these
      // cases, we return just what getAnnotationsAt returned
      return annots; 
    }
  }

  /***********************************************************************/
  /* Annotation Mouse Listener */
  /***********************************************************************/

  public class TextAnnotationMouseListener extends AnnotationMouseAdapter {
    
    public void mouseClicked(AnnotationMouseEvent e) {
      if (DEBUG > 0)
        System.err.println ("MTP.annotMouseClicked");
      MouseEvent me = e.getMouseEvent();
      if (!me.isConsumed() && (me.getButton() == MouseEvent.BUTTON1)) {
        // this must be the topmost of the clicked annotations
        // if this is a BUTTON1 click and
        // if it is not selected, clear any text selection and select it
        AWBAnnotation annot = e.getAnnotation();
        if (DEBUG > 1)
          System.err.println("MTP.annotMouseClicked: annot is " + annot);
        SetModel model = document.getSelectedAnnotationModel ();
        
        if ( ! model.contains (annot)) {
          clearTextSelection ();
          // if not a shift click, clear other annots
          if ((me.getModifiers() & ActionEvent.SHIFT_MASK) == 0)
            document.unselectAllAnnotations ();
          if (DEBUG > 1)
            System.err.println("MTP:annotMClicked: selecting annot: " + annot);
          document.selectAnnotation (annot);
        } 
        // regardless of whether we had to clear text selection and
        // select, we should now consume the events
        me.consume ();
        e.consume ();
      }
    }
  }

  /***********************************************************************/
  /* PROPERTYCHANGE LISTENER */
  /***********************************************************************/

  private class PreferencesListener implements PropertyChangeListener {
    public void propertyChange (PropertyChangeEvent evt) {
      String name = evt.getPropertyName ();
      Object value = evt.getNewValue ();
      if (ACTIONS_DIALOG_VISIBLE_KEY.equals (name))
        setPaletteVisible (((Boolean) value).booleanValue());
      else if (SELECTION_MODE_KEY.equals (name))
        setSelectionMode (AutoSelectCaret.Mode.decode ((String) value));
      else if (CARET_VISIBLE_KEY.equals (name))
        setCaretVisible(((Boolean) value).booleanValue());
      else if (RECONCILIATION_HIGHLIGHT_KEY.equals(name))
        setReconciliationHighlightColor((Color)value);
    }
  }

  private class DocPropertyListener implements PropertyChangeListener {
    public void propertyChange (PropertyChangeEvent evt) {
      String name = evt.getPropertyName ();
      if (JawbDocument.ENCODING_PROPERTY_KEY.equals (name)) {
        updateAnnotationSelections (false);
        textPane.setCaretPosition (0);
        
      } else if (JawbDocument.FONT_FAMILY_PROPERTY_KEY.equals (name)) {
        setAnnotationInspectorFont ((String) evt.getNewValue());

      } else if (JawbDocument.ORIENTATION_PROPERTY_KEY.equals (name)) {
        ComponentOrientation o = (ComponentOrientation) evt.getNewValue();
        textPane.setComponentOrientation(o);
      }
    }
  }

  /***********************************************************************/
  /* ACTIONS */
  /***********************************************************************/

  /**
   * Clear text selection.
   * @return false if there was no selected text to start with
   */
  public boolean clearTextSelection () {
    Caret caret = textPane.getCaret();
    int start = caret.getMark ();
    int end = caret.getDot ();
    
    if (start == end)
      return false;

    caret.setDot(end);
    return true;
  }

  /**
   * Clear any text selection that exists, or, if none, clear the annotation
   * selection.
   */
  public class ClearSelection extends AbstractAction {
    public ClearSelection () {
      super ("clearSelection");
    }
    public void actionPerformed (ActionEvent e) {
      if (! clearTextSelection ()) {
        document.unselectAllAnnotations();
        repaint (); // needed (windows at least) for proper refresh.
      }
    }
  }
  /**
   * If the annotations last inspected by mouseover are overlapping, this will
   * (if there are multiple at that point) bring the back one to the top
   * TODO: this should really be made static.
   */
  public class RaiseAnnotation extends TextAction {
    public RaiseAnnotation () {
      super ("raiseAnnotation");
    }
    public void actionPerformed (ActionEvent e) {
      List annots = lastInspectedAnnots;
      if (annots != null && annots.size () > 1) {
        AWBAnnotation annot = (AWBAnnotation) annots.get (annots.size ()-1);
        document.unselectAllAnnotationsDeferSort();
        document.selectAnnotation (annot);
        // if a ItemExchanger indicated selecting annot would /also/ select
        // annot', then annot' might always be on top unless this is done
        document.raiseAnnotation (annot);
        
        resetAnnotationInspector ();
        boolean shiftOnly = Jawb.getPreferences().getBoolean(ANNOTATION_INSPECTOR_REQUIRE_SHIFT_KEY);
        inspectAnnotations (null, shiftOnly);
      }
    }
  }
  /**
   * If the glyph under the mouse is a ligature, break or rejoin it.
   */
  public class ToggleLigature extends TextAction {
    public ToggleLigature () {
      super ("toggleLigature");
    }
    public void actionPerformed (ActionEvent e) {
      // probably should test the glyph for actually being a ligature, and send
      // the actual character range in to have the LIGATURE_BREAK_ATTRIBUTE
      // added to a specific range
      int offset = textPane.viewToModel (lastInspectedPoint);
      document.toggleLigature(offset);
    }
  }
  /**
   * Delete all currently selected annotations.
   */
  // I don't think this is even used anymore, and the inspector reset
  // is handled by a listener
  public class MainTextDeleteAnnotation extends DeleteAnnotAction {
    // TODO: this should really be made static: If Jawb Document were turned
    // into an actual Document derivative we could retrieve it cast and call
    // the 'getselectedAnnnotationModel.
    public MainTextDeleteAnnotation (TaskToolKit kit) {
      super (kit);
    }
    public void actionPerformed (ActionEvent e) {
      super.actionPerformed (e);
      if (DEBUG > 0)
        System.err.println("MTDelAnnot inspector reset");
      resetAnnotationInspector ();
      boolean shiftOnly = Jawb.getPreferences().getBoolean(ANNOTATION_INSPECTOR_REQUIRE_SHIFT_KEY);
      inspectAnnotations (null, shiftOnly);
    }
  }
  /**
   * Toggle the selection mode between 'word' and 'character' based.
   */
  public class ToggleSelectionMode extends AbstractAction {
    public ToggleSelectionMode () {
      super ("toggleSelectionMode");
    }
    public void actionPerformed (ActionEvent e) {
      AutoSelectCaret.Mode mode = getSelectionMode();
      if (mode == AutoSelectCaret.Mode.WORD)
        mode = AutoSelectCaret.Mode.CHARACTER;
      else
        mode = AutoSelectCaret.Mode.WORD;
      Jawb.getPreferences().setPreference(SELECTION_MODE_KEY, mode.toString());
    }
  }
  /**
   * Toggle cursor visiblity, since by default, uneditable panes get no
   * cursor.
   */
  public class ToggleCaretVisible extends AbstractAction {
    public ToggleCaretVisible () {
      super ("toggleCaretVisible");
    }
    public void actionPerformed (ActionEvent e) {
      boolean visible = isCaretVisible();
      setCaretVisible(!visible);
      Jawb.getPreferences().setPreference(CARET_VISIBLE_KEY, !visible);
    }
  }
}
