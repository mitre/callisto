
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelEvent;

// Imports for picking up mouse events from the JTable. 
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.HasSubordinates;
import org.mitre.jawb.atlas.AnnotationModelEvent;
import org.mitre.jawb.atlas.AnnotationModelListener;
import org.mitre.jawb.swing.TableMap;
import org.mitre.jawb.tasks.Task;

import gov.nist.atlas.type.AnnotationType;
import gov.nist.atlas.Annotation;

/**
 * This is a fairly simple (and not super robust) table model, that tries to
 * be generic. Give it the Task it's being used on, an array of 'column
 * sources' (Object) which will correspond directly to the values displayed in
 * the columns. These can currently be either <code>String</code> or
 * <code>AnnotationType</code> objects, which correspond to simple
 * 'attributes' and 'subordinateSets' respectively.  The 'headings' are simply
 * the strings to display in the table header.<p>
 *
 * If any one of the columns must do something more interesting than simply
 * displaying an attribute value, use a custom TableCellRenderer/Editor.<p>
 *
 * 
 *
 * This Class will also handle setting of the JawbDocument, and extract all
 * the interesting attributes from it, as well as being a listener to
 * updates, removals, and additions for the specified AnnotationType.<p>
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 */
public class AnnotationTableModel extends AbstractTableModel {

  //public static final String ANNOTATION_FILTER_CHANGED_PROPERTY =
  //  "AnnotationFilterChangedProperty";

  private static final int DEBUG = 0;
  
  private JawbDocument document = null;
  
  private Task task;
  private AnnotationType type;
  private Object[] columnSrc;
  private String[] headings;
  private List annotations;
  private AnnotationModelListener aModelListener = new AnnotModelListener ();

  private AnnotationFilter filter = null;
  private AnnotationFilter acceptAnnotationsOfTypeFilter = null;
  
  /**
   * Construct a table model to display annotations from the given task, of
   * the specified type. The columnSrc array must contain <code>String</code>
   * or <code>AnnotationType</code> objects corresponding to 'attributes' or
   * 'subordinateSets' respectively.<p>
   *
   * The value returned for columns is defined by the columnSrc parameter.
   * This is an array of Strings, AnnotationTypes, or null, which specifies
   * what is returned for the respective rows.  A string should name an
   * attribute, an AnnotationType should name a subordinate annotation type,
   * and null will return the annotation for that row itself.  If null is used,
   * a task is expected to use a custom TableCellRenderer, and likely set the
   * column to be non-editable.
   */
  public AnnotationTableModel (Task task, AnnotationType type,
                               Object[] columnSrc, String[] headings) {
    // sanity check
    if (columnSrc.length != headings.length)
      throw new RuntimeException("Mismatch columns and headings for "+
                                 type.getName()+" table");
    this.task = task;
    this.type = type;
    this.columnSrc = new Object[columnSrc.length];
    this.headings = new String[headings.length];
    System.arraycopy (columnSrc, 0, this.columnSrc, 0, columnSrc.length);
    System.arraycopy (headings, 0, this.headings, 0, headings.length);

    acceptAnnotationsOfTypeFilter = new AnnotationTypeFilter(type);
    filter = acceptAnnotationsOfTypeFilter;
    // what the heck... check now for valid columnSrc objects

    for (int i=0; i<columnSrc.length; i++) {
      if (! (columnSrc[i] == null ||
             columnSrc[i] instanceof String ||
             columnSrc[i] instanceof AnnotationType))
        throw new IllegalArgumentException ("columnSrc["+i+"]="+columnSrc[i]);
    }
    annotations = new ArrayList();
  }

  /***********************************************************************/
  /* AnnotationTableModel specific methods */
  /***********************************************************************/

  /** Get the task this model was constructed with. */
  public Task getTask () {
    return task;
  }

  /** Get the AnnotationType this model displays. */ 
  public AnnotationType getType () {
    return type;
  }
  
  /**
   * Remove all annotations from the model.
   */
  public void clearAnnotations () {
    annotations.clear ();
    fireTableDataChanged ();
  }

  /**
   * Add an annotation to the model if its type is the same this model was
   * constructed with.
   */
  public void addAnnotation (AWBAnnotation annot) {
    if (filter.accept (annot)) {
      annotations.add (annot);
      int row = annotations.size ()-1;
      fireTableRowsInserted (row, row);
    }
  }

  /**
   * Remove an annotation if it is in the model.
   */
  public void removeAnnotation (AWBAnnotation annot) {
    int row = annotations.indexOf (annot);
    if (row != -1) {
      annotations.remove (row);
      fireTableRowsDeleted (row, row);
    }
  }

  /**
   * Retrieve the annotation at the specified row.
   * @throws IndexOutOfBoundsException if the specified index is is out of
   * range (index &lt; 0 || index &gt;= getRowCount()).
   */
  public AWBAnnotation getAnnotation (int row) {
    return (AWBAnnotation) annotations.get (row);
  }

  /**
   * Return the object used to retrieve the columns value.
   *
   * @see AnnotationTableModel
   * @throws IndexOutOfBoundsException
   */
  public Object getColumnSource (int col) {
    return columnSrc [col];
  }

  /**
   * Get the row of the specified annotation.
   * @return the row in this model of the first occurrence of the specified
   *         annotation, or -1 if the model does not contain the annotaion.
   */
  protected int getRow (AWBAnnotation annot) {
    return annotations.indexOf (annot);
  }

  /***********************************************************************/
  /* Allow filtering to display only certain annotaitons */
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
    return filter;
  }
  
  /**
   * Specify the <code>AnnotationFilter</code> used to display annotations. If
   * <code>null</code> is specified, the filter which accepts the
   * AnnotationType specified when this table model was initialized is
   * returned.
   * 
   * @param filter the <code>AnnotationFilter</code> object used to determine
   * which annotations are visible
   *
   * @see #getAnnotationFilter
   * @see #resetAnnotationFilters
   */ 
  public void setAnnotationFilter (AnnotationFilter filter) {
    if (filter == null)
      filter = getAcceptAnnotationsOfTypeFilter();

    //AnnotationFilter oldValue = filter;
    this.filter = filter;
    updateAnnotations ();
    /*
    firePropertyChange(ANNOTATION_FILTER_CHANGED_PROPERTY,
                       oldValue, getAnnotationFilter());
    */
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
    //AnnotationFilter oldValue = filter;
    this.filter = getAcceptAnnotationsOfTypeFilter ();
    updateAnnotations ();
    /*
    firePropertyChange(ANNOTATION_FILTER_CHANGED_PROPERTY,
                       oldValue, getAnnotationFilter());
    */
  }
  
  /**
   * Returns the <code>AnnotationFilter</code> for the
   * <code>AnnotaitonType</code> used to create this Model.
   */
  public AnnotationFilter getAcceptAnnotationsOfTypeFilter() {
    return acceptAnnotationsOfTypeFilter;
  }

  /***********************************************************************/
  /* Implementation of the TableModel */
  /***********************************************************************/

  public int getRowCount () {
    return annotations.size ();
  }

  public int getColumnCount () {
    return headings.length;
  }

  public String getColumnName (int col) {
    return headings [col];
  }

  // TODO: set default editor for AWBAnnotations
  public Class getColumnClass (int col) {
    Class c = Object.class;
    Object src = getColumnSource (col);
    
    if (src == null)
      c = task.getAnnotationClass(type);
    else if (src instanceof String)
      c = task.getAttributeType(type, (String) src);
    else if (src instanceof AnnotationType)
      c = task.getAnnotationClass((AnnotationType) src);
    
    return c;
  }

  /**
   * Provides access to the annotation's columnSrc using the standard
   * getAttributeValue, means, using the nth (col) element of the 'columnSrc'
   * array passed to the constructor.
   */
  public Object getValueAt (int row, int col) {
    Object src = getColumnSource (col);
    AWBAnnotation annot = getAnnotation (row);

    if (DEBUG > 2) {  // yes, it's true... this is only for debugging
      System.err.print ("ATM.getValueAt ("+row+","+col+")"+
                        ") annot=["+annot.getAnnotationType().getName()+
                        "] attr="+src+
                        " val=");
      if (src == null)
        System.err.println(annot);
      else if (src instanceof String)
        System.err.println (annot.getAttributeValue ((String) src));
      else if (src instanceof AnnotationType)
        System.err.println (((HasSubordinates)annot).getSubordinates ((AnnotationType) src));
    }
    
    // the default assumes each value is an 'attribute' or 'subordinateset'
    // value, exceptions should be handled in subclasses handing the rest to
    // 'super.getValueAt'
    Object value = null;
    if (src == null)
      value = annot;
    else if (src instanceof String)
      value = annot.getAttributeValue ((String) src);
    else if (src instanceof AnnotationType)
      value = ((HasSubordinates)annot).getSubordinates ((AnnotationType) src);
    else 
      // Unrecognized or null src, which subclasses need to have dealt
      // with. If execution get's here, it's either a bug, or time to add new
      // funtionality to this class.
      throw new RuntimeException ("Invalid source type: "+src);
    return value;
  }
  
  /**
   * Provides access to annotation using standard TableModel
   * interface. Currently, just fails silently when the attribute is read
   * only, or who's columnSrc is an AnnotationType.
   */
  public void setValueAt (Object value, int row, int col) {
    if (DEBUG > 1) {
      System.err.println("ATM.setValAt row: " + row + " col: " + col + 
                         " value: " + value);
      Thread.dumpStack();
    }
    Object src = getColumnSource (col);

    // don't push SubAnnot arrays back into to the Array. Assume editors edit
    // values in them directly, and have another means of adding data to the
    // subAnnotation Sets. TableSubAnnotSetEditRenderer relies upon this.
    // Same goes for trying to change the annotation itself
    if (src == null || src instanceof AnnotationType)
      return;

    AWBAnnotation annot = getAnnotation (row);
    try {
      // HACK: jATLAS wants the empty string to clear an attribute :(
      if (value == null)
        value = "";
      annot.setAttributeValue ((String) src, value);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
    fireTableCellUpdated (row, col);
  }

  /**********************************************************************/
  /* Accessing the data (and unfortunately, storing a local copy) */
  /***********************************************************************/

  /**
   * Clear any existing annotations from the model, and retrieve those in the
   * new document (if it's non null).
   */
  public void setJawbDocument (JawbDocument doc) {
    
    if (DEBUG > 0)
      System.err.println ("ATM.setJD: type="+type.getName()+" displaying doc="+
                          (doc==null?null:doc.getDisplayName(false)));
    
    if (document != null) {
      document.getAnnotationModel ()
        .removeAnnotationModelListener (aModelListener);

      annotations.clear ();
    }
    
    document = doc;

    if (doc != null) {
      if (! doc.getTask ().equals (task))
        throw new IllegalStateException ("Document's task != TableModel task");
      
      doc.getAnnotationModel ()
        .addAnnotationModelListener (aModelListener);
      
      // insert the annotations already known
      if (DEBUG > 0)
        System.err.println ("ATM.setJD: type="+type.getName()+" inital annots");
      updateAnnotations ();
      
    } else { // updateAnnotations will fire this itself.
      fireTableDataChanged ();
    }
  }

  /**
   * Separate from setJawbDocument so that it can be called from {@link
   * #setAnnotationFilter}.
   */
  private void updateAnnotations () {
    if (DEBUG > 0)
      System.err.println ("ATM.updating");
    annotations.clear ();

    JawbDocument doc = getJawbDocument ();
    if (doc != null) {
      AnnotationFilter filter = getAnnotationFilter();

      Iterator iter = doc.getAnnotationModel ().getAllAnnotations();
      while (iter.hasNext()) {
        Annotation annot = (Annotation)iter.next();
        if (DEBUG > 0)
          System.err.println ("          adding:"+annot);
        if (filter.accept (annot)) {
          annotations.add (annot);
        }
      }
    }
    fireTableDataChanged ();
  }
  
  public JawbDocument getJawbDocument () {
    return document;
  }

  /** Convience method for getting the Possible values from the Task. */
  public Set getPossibleValues (AWBAnnotation annot, int col) {
    Object src = getColumnSource(col);
    Set set = null;

    if (src instanceof String)
      set = task.getPossibleValues (annot, (String) src);
    //else if (src instanceof AnnotationType)
    //  set = null;
    
    return set;
  }

  /***********************************************************************/
  /* Implementation of AnnotationModelListener */
  /***********************************************************************/

  private class AnnotModelListener implements AnnotationModelListener {
  
    /** Invoked after an annotation has been created. */
    public void annotationCreated (AnnotationModelEvent e) {
      Annotation annot = e.getAnnotation();
      if (DEBUG > 1 && type.equals(annot.getAnnotationType()))
        System.err.println ("ATM.annotCreated: "+annot.getAnnotationType().getName()+
                            " "+annot.getId());
      // If an AnnotationModelListener receives the CREATED event before we do,
      // and in response modifies the annotation, we will recieve the CHANGED
      // event before the CREATED event, so the annot may already be in the table
      if (! annotations.contains(annot) && filter.accept (annot))
        addAnnotation ((AWBAnnotation)annot);
    }
  
    /** Invoked after an annotation has been deleted. */
    public void annotationDeleted (AnnotationModelEvent e) {
      Annotation annot = e.getAnnotation();
      if (DEBUG > 1 && type.equals(annot.getAnnotationType()))
        System.err.println ("ATM.annotDeleted: "+annot.getAnnotationType().getName()+
                            " "+annot.getId());
      removeAnnotation ((AWBAnnotation)annot);
    }
    
    /** Invoked after an annotation has been changed. */
    public void annotationChanged (AnnotationModelEvent e) {
      Annotation annot = e.getAnnotation();
      int index = annotations.indexOf (annot);
      if (index != -1) {
        AnnotationModelEvent.AnnotationChange change = e.getChange ();
        if (DEBUG > 1) {
          String name = change.getPropertyName (); // could be null
          System.err.println ("   ---ATM.propChng: name="+name+
                              " val="+change.getNewValue());
        }
        if (! filter.accept (annot))
          removeAnnotation ((AWBAnnotation) annot);
        else// TODO: here's lazy: recalculate the whole thing!
          fireTableRowsUpdated (index, index);
        
      } else if (filter.accept (annot)) {
        if (DEBUG > 1)
          System.err.println ("   ---not found in model, so adding!");
        addAnnotation ((AWBAnnotation) annot);
      }
    }
    
    /** Invoked after an annotation has had subannotations added. */
    public void annotationInserted (AnnotationModelEvent e) {
      if (DEBUG > 1)
        System.err.println ("ATM.annotInserted: "+e);
    }
    
    /** Invoked after an annotation has had subannotations removed. */
    public void annotationRemoved (AnnotationModelEvent e) {
      if (DEBUG > 1)
        System.err.println ("ATM.annotRemoved: "+e);
      
    }
    
  }// AnnotationModelListener
}
