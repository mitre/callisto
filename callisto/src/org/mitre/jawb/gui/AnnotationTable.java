
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

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JTable;
import javax.swing.JComboBox;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.AncestorEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import gov.nist.atlas.type.AnnotationType;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.NamedExtentRegions;
import org.mitre.jawb.atlas.TextExtentRegion;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.swing.TableSorter;
import org.mitre.jawb.swing.ItemExchanger;
import org.mitre.jawb.swing.SetModel;
import org.mitre.jawb.swing.FontIndependentCellRenderer;
import org.mitre.jawb.swing.event.SetDataEvent;
import org.mitre.jawb.swing.event.SetDataListener;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;

/**
 * Table display of annotations that can be used in conjunction with {@link
 * AnnotationTableModel} to display Annotaions in fairly easily. Knows of
 * TextExtentRegion and NamedTextExtentRegions so that if the attribute
 * displayed by a column ends with "TextExtent" it is marked as 'uneditable'
 * and is displayed in the font of the current document. 
 */
public class AnnotationTable extends JTable implements JawbComponent {

  private static final int DEBUG = 0;
  
  /**   */
  private boolean[] editable;
  
  /** ComboBox to edit cells with choices, rebuilt for every edit. */
  private JComboBox comboBox;
  /** Editor with referemce to comboBox to edit cells with choices. */
  private TableCellEditor comboEditor;

  /** renderer which has the font updated based on JawbDocument */
  private FontIndependentCellRenderer fontFielder;

  private TaskToolKit toolkit;
  private AnnotationTableModel annotModel;
  private String prefKeyPrefix;

  /** Observe changes in Preferences. */
  private PropertyChangeListener prefChangeListener = null;
  /** Observe changes in document properties. */
  private PropertyChangeListener docPropListener = null;
  /** Observes selection changes in doc itself. Suspended occaisionally */
  private DocumentSelectionListener annotSelectionListener = null;
  /** Observes selection changes in the table. Suspended occasionally */
  private ListSelectionListener tableSelectionListener = null;
  /** Observes raw mouse events in the table and converts them to
   * AnnotationMouseEvents */
  private MouseListener tableMouseListener = null;
  /** Observes mouse events on Annotations in the table */
  private AnnotationMouseListener annotMouseListener = null;
  /** Current document's annotation mouse model */
  private AnnotationMouseModel annotMouseModel = null;
  /** Observes component to stop editing cells, and persist gui state */
  private AncestorListener ancestorListener = null;
  
  /** Map other annotation selections to Mention selections */
  private HashMap selectionExchangers = new HashMap ();
  /** Indicates that the last change left no items selected in the table. */
  private boolean tableSelectionWasEmpty = false;

  /**
   * Create an AnnotationTableModel which uses the specified toolkit and
   * model. Actions from the Toolkit may maintain state.
   */
  public AnnotationTable (TaskToolKit kit, AnnotationTableModel model) {
    toolkit = kit;
    annotModel = model;

    // work-around for java bug #4274963 "Typing into a JTable cell
    // fails to transfer focus to the cell (cf. 4256006)"
    setSurrendersFocusOnKeystroke(true);
    
    // Initialize Listeners:
    // I am calling protected methods to init these so that subclasses
    // can, if necessary, replace them with alternates. RK 1/30/06
    initPrefChangeListener();
    initDocPropListener();
    initAnnotSelectionListener();
    initTableSelectionListener();
    initTableMouseListener();
    initAnnotMouseListener();
    initAncestorListener();

    // create a 'sorting' wrapper for the model, and add it's listening
    // capabilities to this tables header
    
    // NOTE: Because we create it here, yet maintain reference to the
    // annotModel, it's our responsibility to remember to remap row numbers
    // via the sorter, when referencing directly into the annotModel
    TableSorter sorter = new TableSorter (model,getSelectionModel());
    setModel (sorter);
    sorter.addMouseListenerToHeaderInTable (this);
    // check for case sensitivity for strings. prefs listener will update
    Preferences prefs = Jawb.getPreferences();
    Comparator comparator = null;
    if (prefs.getBoolean(Preferences.SORT_CASE_SENSITIVE_KEY))
      comparator = String.CASE_INSENSITIVE_ORDER;
    sorter.setClassComparator(String.class, comparator);
    
    // Listen to selections on table and propogate back to JawbDocument
    ListSelectionModel lsm = getSelectionModel ();
    lsm.addListSelectionListener (tableSelectionListener);

    // Listen for mouse events on the table, and propagate as 
    // AnnotationMouseEvents where applicable
    this.addMouseListener(tableMouseListener);

    // create the dynamic combo editor used for multiple choice attributes
    comboBox = new JComboBox ();
    comboEditor = new DefaultCellEditor (comboBox);

    // TODO: AWBAnnotation columns should get a different editor. Currently
    // just making them non editable, and all others editable.
    editable = new boolean[model.getColumnCount ()];
    Arrays.fill (editable, true);
    
    // Create the dynamic font cell renderer used when rendering text from the
    // signal. Knows about 'TextExtent' attributes of both TextExtentRegion
    // and NamedExtentRegions, and assumes columns displaying AWBAnnotations
    // will fit that category as well.
    TableColumn column;
    fontFielder = new FontIndependentCellRenderer ();
    
    Class c = model.getTask().getAnnotationClass (model.getType());
    if (DEBUG > 0) {
      System.err.println ("AT<init> for class " + c.getName()); 
    }
    boolean isTextExtent = (TextExtentRegion.class.isAssignableFrom (c) ||
                            NamedExtentRegions.class.isAssignableFrom (c));

    for (int i=0; i<model.getColumnCount (); i++) {
      Object src = model.getColumnSource (i);
      // still in initialization, so no need to remap
      Class columnClass = annotModel.getColumnClass (i);
      if (DEBUG > 1)
        System.err.print ("AT<init>: type="+model.getType ().getName ()+
                          " col="+i+" colSrc="+src+
                          " colClass="+(columnClass==null?
                                        null+"\n":columnClass.getName()));
      
      // AWBAnnotation attributes and SubordinateSets are read-only and follow
      // JawbDocument font
      // Tables who's annotations are text extents show those 'TextExtent'
      // attributes as read-only cells which follow JawbDocument font
      if (AWBAnnotation.class.isAssignableFrom (columnClass) ||
          (isTextExtent && (src instanceof String) &&
           ((String) src).endsWith ("TextExtent"))) {
        column = getColumnModel ().getColumn (i);
        column.setCellRenderer (fontFielder);

        editable[i] = false;
      }
      if (DEBUG > 1)
        System.err.println (" editable="+editable[i]);
    }

    // install key bindings for 'delete' and 'clear'ing the selection. These
    // fire when _table_ focused (not cell editors)
    String delete = "delete-annotations";
    String clear = "clear-selection";
    
    ActionMap actionMap = getActionMap ();
    actionMap.put (delete, new DeleteAnnotAction (toolkit));
    actionMap.put (clear, new ClearSelectionAction ());

    InputMap inputMap = getInputMap ();
    inputMap.put (KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), clear);
    inputMap.put (KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0), delete);
    inputMap.put (KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,0), delete);
    
    // Removes editor when table is no longer visible (or in current frame) and
    // keeps colum widths persistent.
    addAncestorListener(ancestorListener);
  }

  /************************************************************************
   *  Protected methods that subclasses can use to set the various
   *  listeners to specialty listeners.  Proceed with caution.
   ************************************************************************/

  protected void initPrefChangeListener () {
    setPrefChangeListener(new PreferencePropertyListener ());
  }

  protected void initDocPropListener() {
    setDocPropListener(new DocumentPropertyListener ());
  }

  protected void initAnnotSelectionListener() {
    setAnnotSelectionListener(new DocumentSelectionListener ());
  }

  protected void initTableSelectionListener() {
    setTableSelectionListener(new TableSelectionListener ());
  }

  protected void initTableMouseListener() {
    setTableMouseListener(new AnnotationTableMouseListener ());
  }

  protected void initAnnotMouseListener() {
    setAnnotMouseListener(new AnnotTableAnnotMouseListener ());
  }

  protected void initAncestorListener() {
    setAncestorListener(new TableAncestorListener ());
  }


  /*************************************************************************
   * Subclasses may use these methods to set the listeners, but should
   * ONLY do so when initializing the listener.  Doing this later
   * won't work, as the listener won't really get registered.
   *************************************************************************/
  protected void setPrefChangeListener (PropertyChangeListener listener) {
    prefChangeListener = listener;
  }

  protected void setDocPropListener(PropertyChangeListener listener) {
    docPropListener = listener;
  }

  protected void setAnnotSelectionListener(DocumentSelectionListener listener) {
    annotSelectionListener = listener;
  }

  protected void setTableSelectionListener(ListSelectionListener listener) {
    tableSelectionListener = listener;
  }

  protected void setTableMouseListener(MouseListener listener) {
    tableMouseListener = listener;
  }

  protected void setAnnotMouseListener(AnnotationMouseListener listener) {
    annotMouseListener = listener;
  }

  protected void setAncestorListener(AncestorListener listener) {
    ancestorListener = listener;
  }


  /** 
   * Set the maximum row count (size before it will start to scroll) of
   * the JComboBox used to implement the menu-based cell editors used for
   * attribute value selection.  
   *
   * This method was added to allow Tasks the ability to customize this
   * feature.  TODO it would be cool if this were customizable by user
   * preferences.
   */
  public void setComboBoxMaxRowCount(int n) {
    comboBox.setMaximumRowCount(n);
  }

  /**
   * The (language independant) name is used to store preferences. Load them
   * when the name is set, since it is not required as part of ctor.
   */
  public void setName(String name) {
    super.setName(name);

    // reload table column widths
    if (name == null)
      return;

    // changing name changes prefix
    String namespace = toolkit.getTask().getName();
    prefKeyPrefix = "annotTable."+namespace+"."+getName()+".";

    Preferences prefs = Jawb.getPreferences ();
    Enumeration enumeration = getColumnModel().getColumns();
    while (enumeration.hasMoreElements()) {
      TableColumn column = (TableColumn) enumeration.nextElement();
      String key = prefKeyPrefix + column.getIdentifier();
      int width = prefs.getInteger(key, column.getWidth());
      column.setPreferredWidth(width);
    }
  }

  /**
   * A simple means of setting editablility of an entire column. More advanced
   * means should override isCellEditable.
   */
  public void setColumnEditable (int col, boolean edit) {
    if (DEBUG > 0)
      System.err.println ("AT.setColEditable: ("+col+") = "+editable[col]);
    col = getColumnModel().getColumn(col).getModelIndex();
    editable [col] = edit;
  }

  public boolean isCellEditable (int row, int col) {
    if (DEBUG > 0)
      System.err.println ("AT.isEditable: ("+row+","+col+
                          ") = "+editable[col]);
    col = getColumnModel().getColumn(col).getModelIndex();
    return editable[col];
  }

  /** Allows dynamic comboboxes or simply a text field as editors */
  public TableCellEditor getCellEditor (int row, int col) {
    if (DEBUG > 0)
      System.err.println ("AT.getCellEditor: ("+row+","+col+")");
    
    TableColumn tableColumn = getColumnModel().getColumn(col);
    TableCellEditor editor = tableColumn.getCellEditor();

    if (editor != null)
      return editor;
    
    TableSorter sorter = (TableSorter)getModel();
    int modelRow = sorter.convertRowIndexToModel (row);
    int modelCol = getColumnModel().getColumn(col).getModelIndex();
    if (sorter.getColumnClass(modelCol).equals(Boolean.class))
      return super.getCellEditor (row, col);
    
    AWBAnnotation annot = annotModel.getAnnotation (modelRow);
    Set values = annotModel.getPossibleValues (annot, modelCol);

    if (DEBUG > 0)
      System.err.println ("AT.gCellEditor: ("+modelRow+","+modelCol+
                          ") attName="+annotModel.getColumnSource (modelCol)+
                          " annot=["+annot+"] values="+
                          (values==null?"null":values.toString()));
    // unspecified value.
    if (values == null)
      return super.getCellEditor (row, col);

    // rebuild the combobox for what is allowed
    comboBox.removeAllItems ();
    Iterator iter = values.iterator ();
    while (iter.hasNext ())
      comboBox.addItem(iter.next ());

    return comboEditor;
  }
  
  public void clearAnnotations () {
    annotModel.clearAnnotations ();
  }

  public void addAnnotation (AWBAnnotation annot) {
    annotModel.addAnnotation (annot);
    // if the table is in a scroll pane, scroll to the new annot
    if (DEBUG > 0)
      System.err.println("AnnotationTable.addAnnot scroll to new anot row");
    scrollRectToVisible (getCellRect (getRow(annot), 0, true));
  }

  public void removeAnnotation (AWBAnnotation annot) {
    annotModel.removeAnnotation (annot);
  }

  public int getRow (AWBAnnotation annot) {
    int row = annotModel.getRow (annot);
    TableSorter sorter = (TableSorter)getModel();
    if (DEBUG > 1)
      System.err.println("AT.getRow for " + annot + " annotModel gives row: " +
                         row + " sorter gives row " + 
                         sorter.convertRowIndexToView(row));
    return sorter.convertRowIndexToView (row);
  }

  public AWBAnnotation getAnnotation (int row) {
    // since this class is the one that created the TableSorter and keeps re
    TableSorter sorter = (TableSorter)getModel();
    row = sorter.convertRowIndexToModel(row);
    return annotModel.getAnnotation (row);
  }

  /***********************************************************************/
  /* Proxy methods to AnnotationTableModel for filtering */
  /***********************************************************************/
  
  /**
   * Return the visible AnnotationFilter in use.
   *
   * @return the <code>AnnotationFilter</code> object used to determine which
   * annotations are visible.
   *
   * @see #setAnnotationFilter
   * @see #resetAnnotationFilters
   */ 
  public AnnotationFilter getAnnotationFilter () {
    return annotModel.getAnnotationFilter ();
  }
  
  /**
   * Specify the <code>AnnotationFilter</code> used to display annotations. If
   * <code>null</code> is specified, the filter which accepts the
   * AnnotationType specified when this table's model was initialized is
   * returned.
   * 
   * @param filter the <code>AnnotationFilter</code> object used to determine
   * which annotations are visible
   *
   * @see #getAnnotationFilter
   * @see #resetAnnotationFilters
   */ 
  public void setAnnotationFilter (AnnotationFilter filter) {
    annotModel.setAnnotationFilter (filter);
  }
  
  /**
   * Resets the visible annotation filter to its starting state. Normally,
   * this is the <code>AnnotationsFilter</code> which accepts all annotations
   * of the AnnotationType used in the constructor.
   *
   * @see #setAnnotationFilter
   * @see #getAnnotationFilter
   */
  public void resetAnnotationFilters() {
    annotModel.resetAnnotationFilters();
  }
  
  /***********************************************************************/
  /* Implementing JawbComponent */
  /***********************************************************************/
  
  public void setJawbDocument (JawbDocument doc) {
    JawbDocument old = annotModel.getJawbDocument ();
    Preferences prefs = Jawb.getPreferences ();
    SetModel annotSM;
    if (old != null) {// remove listeners from old documents
      old.removePropertyChangeListener (docPropListener);
      prefs.removePropertyChangeListener (prefChangeListener);
      annotSM = old.getSelectedAnnotationModel ();
      annotSM.removeSetDataListener (annotSelectionListener);
      annotMouseModel.removeAnnotationMouseListener(annotMouseListener);
      annotMouseModel = null;

      Jawb.storePreferences();
    }
    if (doc != null) {   // get current font and register for changes
      String family = doc.getFontFamily ();
      String ffFamily = fontFielder.getFont ().getFamily ();
      Comparator comparator = null;
      // update in case changes were made while unregistered
      setComponentOrientation(doc.getComponentOrientation());
      if (! ffFamily.equals (family))
        setFont (family);
      if (prefs.getBoolean(Preferences.FONTS_TABLE_FOLLOW_SIZE_KEY))
        setFontSize (doc.getFontSize ());
      if (! prefs.getBoolean(Preferences.SORT_CASE_SENSITIVE_KEY))
        comparator = String.CASE_INSENSITIVE_ORDER;
      TableSorter sorter = (TableSorter)getModel();
      sorter.setClassComparator(String.class, comparator);

      prefs.addPropertyChangeListener (prefChangeListener);
      doc.addPropertyChangeListener (docPropListener);
      annotSM = doc.getSelectedAnnotationModel ();
      annotSM.addSetDataListener (annotSelectionListener);
      annotMouseModel = doc.getAnnotationMouseModel();
      annotMouseModel.addAnnotationMouseListener(annotMouseListener);
    }
    // pass it on...
    annotModel.setJawbDocument (doc);
  }

  private void setFont (String family) {
    Font font = getFont ();
    fontFielder.setFont (new Font (family, font.getStyle(),
                                   font.getSize()));
  }

  private void setFontSize (int size) {
    Font font = getFont ();
    font = new Font (font.getFamily(), font.getStyle(), size);
    setFont (font);
    // fontFielder may have different font
    font = fontFielder.getFont();
    font = new Font (font.getFamily(), font.getStyle(), size);
    fontFielder.setFont (font);
    // recalculate row height
    int maxHeight = 1;
    TableColumnModel columns = getColumnModel();
    for (int cols=columns.getColumnCount(), i=0; i<cols; i++) {
      TableColumn column = columns.getColumn (i);
      TableCellRenderer renderer = column.getCellRenderer();
      if (renderer != null) {
        try {
          Component c = renderer.getTableCellRendererComponent
            (this," ",false,false,0,i);
          maxHeight = Math.max (maxHeight, c.getPreferredSize().height);
        } catch (Exception e) {}
      }
    }
    setRowHeight (maxHeight);
  }

  public void setComponentOrientation (ComponentOrientation orientation) {
    fontFielder.setComponentOrientation(orientation);
    repaint();
  }

  public JawbDocument getJawbDocument () {
    if (annotModel != null)
      return annotModel.getJawbDocument ();
    return null;
  }

  public Set getSelectedAnnots () {
    Set selected = Collections.EMPTY_SET;
    JawbDocument doc = getJawbDocument();
    if (doc == null)
      return selected;
    
    ListSelectionModel lsm = getSelectionModel ();
    int min = lsm.getMinSelectionIndex ();
    int max = lsm.getMaxSelectionIndex ()+1;
    if (DEBUG > 0) {
      System.err.println ("AnnotAct.getSelAnnots: min = " + min +
                          " max = " + max);
    }
    if (min != -1) {
      selected = new LinkedHashSet ();
      for (int i=min; i<max; i++) {
        if (lsm.isSelectedIndex (i))
          selected.add (getAnnotation (i));
      }
    }
    return selected;
  }

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
   *  class EntityToMention extends ItemExchanger {
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
    selectionExchangers.put (type, exchanger);
  }

  /**
   * Retrieve the ItemExchanger used to map the selections in other jawb
   * components to Annotaitons which may be selected in this component.
   *
   * @see #setSelectedAnnotationExchanger
   */
  public ItemExchanger getSelectedAnnotationExchanger (AnnotationType type) {
    return (ItemExchanger) selectionExchangers.get (type);
  }

  /***********************************************************************/
  /* Overriding JTable methods to change the basic UI policy */
  /***********************************************************************/

  /**
   *
   * Changes the table UI so that if an already-selected row is
   * clicked in an editable column, no change to the rows selected
   * occurs.
   */
  public void changeSelection(int rowIndex, int columnIndex, 
                              boolean toggle, boolean extend) { 
    boolean editable = isCellEditable(rowIndex, columnIndex); 
    if (editable && getSelectionModel().getValueIsAdjusting()) { 
      boolean selected = isCellSelected(rowIndex, columnIndex); 
      int[] rows = getSelectedRows(); 
      int[] cols = getSelectedColumns(); 
      if (rows.length > 0 || (! getRowSelectionAllowed() && cols.length > 0)) { 
        if (selected) 
          return; 
      } 
    } 
    super.changeSelection(rowIndex, columnIndex, toggle, extend); 
  } 
 
  /**
   *
   * When editing stops, set the value for all selected rows in the
   * editing column.
   */
  public void editingStopped(ChangeEvent e) {

    // Take in the new value
    TableCellEditor editor = getCellEditor();
    if (editor != null) {
      Object value = editor.getCellEditorValue();
      // set new value for all the rows that are highlighted
      int[] rows = getSelectedRows(); 
      if (DEBUG > 1)
        System.err.println("AT.editingStopped " + rows.length + " rows");
      for (int i=0; i<rows.length; i++) {
        if (DEBUG > 2)
          System.err.println("\t" + i + ": set value at row: " + rows[i] +
                             " col: " + editingColumn + " to: " + value);
        setValueAt(value, rows[i], editingColumn);
      }
    }

    // the original cell's value should have got set above
    // call this just in case, and to remove the editor
    // super.editingStopped(e);
    // that is a bad idea -- can result in an adjacent row
    // being changed when the row just changed above moves.  
    // And really all we need to do is remove the editor
    removeEditor();
  }

 
  /***********************************************************************/
  /* Property Change Listeners */
  /***********************************************************************/

  private class DocumentPropertyListener implements PropertyChangeListener {
    public void propertyChange (PropertyChangeEvent e) {
      String name = e.getPropertyName ();
      if (JawbDocument.ORIENTATION_PROPERTY_KEY.equals (name))
        setComponentOrientation((ComponentOrientation) e.getNewValue());
      if (JawbDocument.FONT_FAMILY_PROPERTY_KEY.equals (name)) {
        setFont ((String)e.getNewValue ());
        repaint (); // TODO: is this necessary?
      }
      if (JawbDocument.FONT_SIZE_PROPERTY_KEY.equals (name)) {
        Preferences prefs = Jawb.getPreferences ();
        if (prefs.getBoolean(Preferences.FONTS_TABLE_FOLLOW_SIZE_KEY))
          setFontSize (((JawbDocument) e.getSource()).getFontSize ());
      }
    }
  }

  private class PreferencePropertyListener implements PropertyChangeListener {
    public void propertyChange (PropertyChangeEvent e) {
      String name = e.getPropertyName ();
      if (Preferences.FONTS_TABLE_FOLLOW_SIZE_KEY.equals (name) &&
          ((Boolean) e.getNewValue()).booleanValue()) {
        JawbDocument doc = getJawbDocument();
        if (doc != null)
          setFontSize (doc.getFontSize());
      }
      else if (Preferences.SORT_CASE_SENSITIVE_KEY.equals(name)) {
        Comparator comparator = null;
        if (! ((Boolean) e.getNewValue()).booleanValue())
          comparator = String.CASE_INSENSITIVE_ORDER;
        TableSorter sorter = (TableSorter)getModel();
        sorter.setClassComparator(String.class, comparator);
      }
    }
  }

  /***********************************************************************/
  /* Table's List Selection Listener for selections */
  /***********************************************************************/

  /** Used for delayed selection w/in document of annots selected in table */
  private Set selectAnnots = new HashSet();
  private Set unselectAnnots = new HashSet();
  
  /** Propogate changes in the selection of the list to the JawbDocument. */
  protected class TableSelectionListener implements ListSelectionListener {
    public void valueChanged (ListSelectionEvent e) {
      if (e.getValueIsAdjusting ())
        return;

      final JawbDocument doc = getJawbDocument();
      if (doc == null)
        return;
      
      // suspend listening to the annotation selection while we update it
      SetModel annotSM = doc.getSelectedAnnotationModel ();
      annotSM.removeSetDataListener (annotSelectionListener);
      
      ListSelectionModel tableSM = (ListSelectionModel)e.getSource ();
      try {

        int first = e.getFirstIndex();
        int last = e.getLastIndex();
        int size = getRowCount();
        if (! tableSM.isSelectionEmpty ()) {
          first = Math.min (first, tableSM.getMinSelectionIndex ());
          last = Math.max (last, tableSM.getMaxSelectionIndex ());
        }
        
        // create sets of annots which should be
        // select/unselected. These actions are put on the GUI thread queue to
        // be done later, after all other side effects of "clicking" a table
        // row have taken place.
        selectAnnots.clear();
        for (int i=first; i<=last && i<size; i++) {
          if (tableSM.isSelectedIndex(i))
            selectAnnots.add(getAnnotation(i));
        }

        unselectAnnots.clear();
        unselectAnnots.addAll(doc.getSelectedAnnotationModel());
        unselectAnnots.removeAll(selectAnnots);

        if (DEBUG > 2) {
          System.err.println("AT.sel.ValueChanged: sel="+selectAnnots+
                             " rem="+unselectAnnots);
          Thread.dumpStack();
        }
        
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
              // NOTE: must unselect first, or cell focus drawing is screwed up
              // (Confirmed on Windows only)
              Iterator i;
              for (i=unselectAnnots.iterator(); i.hasNext(); )
                doc.unselectAnnotation ((AWBAnnotation) i.next());
              for (i=selectAnnots.iterator(); i.hasNext(); )
                doc.selectAnnotation ((AWBAnnotation) i.next());
            }
          });

      } catch (Exception x) { x.printStackTrace (); }

      // check...
      tableSelectionWasEmpty = tableSM.isSelectionEmpty();

      // put annotation selection listener back in
      annotSM.addSetDataListener (annotSelectionListener);
    }
  }

  /***********************************************************************/
  /* For selected annotations in the JawbDocument */
  /***********************************************************************/

  protected class DocumentSelectionListener implements SetDataListener {

    public void elementsAdded (SetDataEvent e) {
      if (DEBUG > 0)
        System.err.println ("AT.docSelListen.elementsAdded: "+e);

      if (getColumnCount () == 0)
        return;

      ListSelectionModel tableSM = getSelectionModel();
      Object eventType = e.getType ();

      // suspend listening to table selection while we update it.
      tableSM.removeListSelectionListener (tableSelectionListener);
      
      // loop over added annots, selecting in the table's selection list
      Iterator iter = null;
      if (eventType == SetDataEvent.ADDED) {
        iter = exchangeAnnots (e.element()).iterator ();
      } else if (eventType == SetDataEvent.ADDED_SET) {
        iter = exchangeAnnots (e.elementSet()).iterator ();
      }

      // used to scroll to the last selected annotation if none in view
      boolean oneVisible = false;
      Rectangle lastRect = null;
      Rectangle visibleRect = getVisibleRect();

      // highlight and check annot for visibility
      while (iter.hasNext ()) {
        AWBAnnotation annot = (AWBAnnotation) iter.next();
        int row = getRow (annot);
        if (DEBUG > 0)
          System.err.println("\tannot " + annot + " is in row " + row);
        if (row >= 0) {
          if (DEBUG > 0)
            System.err.println ("\tselect row " + row);
          tableSM.addSelectionInterval (row,row);
          if (! oneVisible) {
            lastRect = getCellRect (row, 0, true);
            if (visibleRect.contains (lastRect)) {
              oneVisible = true;
              lastRect = null;
            }
          }
        }
      }
      // scroll if necessary
      if (lastRect != null) {
        if (DEBUG > 0)
          System.err.println("AnnotationTable.DocSelListener scrolling to " +
                             lastRect);
        scrollRectToVisible (lastRect);
      }
      
      // record state and put table selection listener back in
      tableSelectionWasEmpty = tableSM.isSelectionEmpty();
      tableSM.addListSelectionListener (tableSelectionListener);
    }
    public void elementsRemoved (SetDataEvent e) {
      if (DEBUG > 0)
        System.err.println ("AT.docSelListen.elementsRemoved: "+e);

      if (getColumnCount () == 0)
        return;

      ListSelectionModel tableSM = getSelectionModel();
      SetModel docSM = (SetModel) e.getSource ();
      Object eventType = e.getType ();

      // suspend listening to table selection while we update it.
      tableSM.removeListSelectionListener (tableSelectionListener);
      
      // loop over removed annots, deselecting in the table's selection list
      Iterator iter = null;
      if (eventType == SetDataEvent.REMOVED) {
        iter = exchangeAnnots (e.element()).iterator ();
      } else if (eventType == SetDataEvent.REMOVED_SET) {
        iter = exchangeAnnots (e.elementSet()).iterator ();
      }

      while (iter.hasNext ()) {
        AWBAnnotation annot = (AWBAnnotation) iter.next();
        int row = getRow (annot);
        if (row >= 0)
          tableSM.removeSelectionInterval (row,row);
      }
      // TODO: this is a temporary fix for most times when a subordinate has
      // been removed from a superordinate, relying on the fact that I usually
      // just call 'JawbDocument#unselectAllAnnotations()". When the 'remove'
      // event get's here, the mapping from sub to super won't work sincs
      // the sub is no longer child of the super! How to fix correctly?
      // goingToRemove event? (blech)
      if (((Set)e.getSource ()).isEmpty ())
        tableSM.clearSelection ();
      
      // record state and put table selection listener back in
      tableSelectionWasEmpty = tableSM.isSelectionEmpty();
      tableSM.addListSelectionListener (tableSelectionListener);
    }

    private Set tempSet = new LinkedHashSet ();
    private Set exchangeAnnots (Object o) {
      AWBAnnotation a = (AWBAnnotation) o;
      ItemExchanger exchanger =
        (ItemExchanger) selectionExchangers.get (a.getAnnotationType());
      if (exchanger == null) {
        tempSet.clear();
        tempSet.add (a);
        return tempSet;
      } else
        return exchanger.exchange (a);
    }
    private Set exchangeAnnots (Set s) {
      if (selectionExchangers.isEmpty ())
        return s;
      tempSet.clear ();
      Iterator i = s.iterator();
      while (i.hasNext()) {
        AWBAnnotation annot = (AWBAnnotation) i.next();
        ItemExchanger exchanger =
          (ItemExchanger) selectionExchangers.get (annot.getAnnotationType());
        if (exchanger == null)
          tempSet.add (annot);
        else
          tempSet.addAll (exchanger.exchange (annot));
      }
      return tempSet;
    }
  }


  /***********************************************************************/
  /* Mouse Listener */
  /***********************************************************************/
  private class AnnotationTableMouseListener extends MouseInputAdapter {

    //private AnnotationMouseModel annotMouseModel;
    private JawbComponent component;

    
    /**
     * If the mouse press occurred in any annotation, trigger the
     * appropriate AnnotationMouseEvent
     */
    public void mousePressed (MouseEvent e) {
      AnnotationTable table = (AnnotationTable) e.getComponent ();
      if (table == null) {
	System.err.println ("ATMouseListener.mPressed component = null");
	return;
      }

      // trigger an AME if over an annot.
      int row = rowAtPoint(e.getPoint());
      if (row >= 0) {
        AWBAnnotation annot = table.getAnnotation(row);
        annotMouseModel.fireAnnotationPressedEvent(e, annot, table);
        if (e.isPopupTrigger())
          table.removeEditor ();
      }
    }

    public void mouseReleased (MouseEvent e) {
      AnnotationTable table = (AnnotationTable) e.getComponent ();
      if (table == null) {
	System.err.println ("ATMouseListener.mReleased component = null");
	return;
      }

      // trigger an AME if over an annot.
      int row = rowAtPoint(e.getPoint());
      if (row >= 0) {
        AWBAnnotation annot = table.getAnnotation(row);
        if (DEBUG > 1)
          System.err.println ("AT.mReleased over " + annot);
        annotMouseModel.fireAnnotationReleasedEvent(e, annot, table);
        if (e.isPopupTrigger())
          table.removeEditor ();
      }
    }

    public void mouseClicked (MouseEvent e) {
      AnnotationTable table = (AnnotationTable) e.getComponent ();
      if (table == null) {
	System.err.println ("ATMouseListener.mClicked component = null");
	return;
      }
      
      // trigger an AME if over an annot.
      int row = rowAtPoint(e.getPoint());
      if (row >= 0) {
        AWBAnnotation annot = table.getAnnotation(row);
        if (DEBUG > 0) {
          System.err.println ("AT.mClicked over " + annot);
        }
        annotMouseModel.fireAnnotationClickedEvent(e, annot, table);
        //table.removeEditor ();
      }
    }
  }

  /***********************************************************************/
  /* Annotation Mouse Listener */
  /***********************************************************************/

  // Hmm, for the moment I can't think of any task-indifferent table-specific
  // actions that need to be taken when a mouse event occurs in an Annotation,
  // but I'll go ahead and register this anyhow, for later use :)
  private class AnnotTableAnnotMouseListener extends AnnotationMouseAdapter {


  }
  
  /***********************************************************************/
  /* AncestorListenr */
  /***********************************************************************/

  private class TableAncestorListener implements AncestorListener {

    /** Observes when our parent window looses focus to remove editor */
    private WindowListener editorRemover;

    public TableAncestorListener() {
      editorRemover = new WindowAdapter () {
          public void windowDeactivated (WindowEvent e) {
            removeEditor ();
          }};
    }

    /**
     * When the table is added to a gui tree, find the containing window, and
     * add to it a listener that will /stop/ editing any cell currently being
     * edited when the window is deactiviated/looses focus.
     */
    public void ancestorAdded (AncestorEvent e) {
      Container c = e.getAncestor ();
      while (! (c instanceof Window))
        c = c.getParent ();
      ((Window)c).addWindowListener (editorRemover);
    }

    /** Ignored */
    public void ancestorMoved(AncestorEvent e) {}

    /**
     * When table is removed from a gui tree, remove the wondow listener added
     * in ancestorAdded(), and stop editing any cell currently being
     * edited. Also persists column widths.
     */
    public void ancestorRemoved (AncestorEvent e) {
      Container c = e.getAncestor ();
      while (! (c instanceof Window))
        c = c.getParent ();
      ((Window)c).removeWindowListener (editorRemover);
      removeEditor ();

      // persist table column widths
      if (getName() == null) {
        System.err.println("Not saving column widths");
        return;
      }

      Preferences prefs = Jawb.getPreferences ();
      Enumeration enumeration = getColumnModel().getColumns();
      while (enumeration.hasMoreElements()) {
        TableColumn column = (TableColumn) enumeration.nextElement();
        // currently results in language dependant pref keys, but it's OK
        String key = prefKeyPrefix + column.getIdentifier();
        prefs.setPreference(key, column.getWidth());
      }
    }
  }
  
  /***********************************************************************/
  /* Special actions */
  /***********************************************************************/

  public static class ClearSelectionAction extends AbstractAction {
    public ClearSelectionAction () {
      super ("clearSelection");
    }
    public void actionPerformed (ActionEvent e) {
      if (e.getSource () instanceof AnnotationTable) {
        ((AnnotationTable) e.getSource ()).clearSelection ();

      } else {
        //if (DEBUG > 1)
          System.err.println ("AnnotTable.DelActions: not an AnnotTable");
      }
    }
  }

}
