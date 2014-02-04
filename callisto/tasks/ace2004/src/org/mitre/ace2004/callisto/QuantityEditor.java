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

package org.mitre.ace2004.callisto;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.*;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleConstants;

import gov.nist.atlas.*;
import gov.nist.atlas.type.*;
import gov.nist.atlas.ref.*;

import org.mitre.jawb.atlas.AnnotationModelEvent;
import org.mitre.jawb.atlas.AnnotationModelListener;
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.NamedExtentRegions;
import org.mitre.jawb.atlas.SubordinateSetsAnnotation;
import org.mitre.jawb.gui.*;
import org.mitre.jawb.swing.*;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;

/**
 * Display and editing widget for Event Annotations. Displays vaild
 * information in a JTable.
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 */
public class QuantityEditor extends JPanel implements JawbComponent {

  private static final int DEBUG = 0;

  private static final int ID_COL = 0;
  private static final int PRIMARY_MENTION_COL = 1;
  private static final int MENTION_COUNT_COL = 2;
  private static final int TYPE_COL = 3;
  private static final int SUBTYPE_COL = 4;

  private static final String COMPONENT_ID = "quantityEditor";

  // TODO: order of these attribs & headings is relied upon in EventModel
  //    getValue is overridden to deal with null (which isn't an attribute)

    // null entries return the annotation object

  private static final String[] ATTRIBS =
    new String[] {null/*ace_id*/, "primary-mention",   null,       "type", "subtype"};
  private static final String[] HEADINGS =
    new String[] {"ID",           "Primary Reference", "Mentions", "Type", "Subtype"};
  
  ACE2004ToolKit toolkit;
  
  AnnotationTable table = null;

  /** Observes changes to annots, watching for mention insertion/removal. */
  AnnotModelListener annotModelListener = null;
  /** Observes changes to properties... for font */
  PropertyChangeListener docPropListener = new DocPropertyListener();

  /**
   * Creates a new <code>QuantityEditor</code> instance.
   */
  public QuantityEditor (TaskToolKit toolkit) {
    this.toolkit = (ACE2004ToolKit) toolkit;
    Task task = toolkit.getTask();
    init (task, task.getAnnotationType (ACE2004Task.QUANTITY_TYPE_NAME));
    setName(COMPONENT_ID);
  }

  private void init (Task task, AnnotationType type) {

    // layout manager
    setLayout (new BorderLayout());

    // create and add table (with multiple column sorting)
    AnnotationTableModel atm =
      new ValueModel (task, type, ATTRIBS, HEADINGS);
    table = new AnnotationTable(toolkit, atm);
    table.setName(COMPONENT_ID);
    table.setColumnEditable(MENTION_COUNT_COL, false);
    table.setAutoResizeMode(table.AUTO_RESIZE_NEXT_COLUMN);

    table.setSelectedAnnotationExchanger(ACE2004Utils.QUANTITY_MENTION_TYPE,
                                         toolkit.mention2Parent);

    TableColumn column = table.getColumnModel().getColumn(ID_COL);
    column.setCellRenderer(new IdRenderer());

    add(new JScrollPane(table), BorderLayout.CENTER);

    // a listener for mention insertion/removal from values
    annotModelListener = new AnnotModelListener();
  }
  
  /**
   * Get the AnnotationTable this widget supports.
   */
  public AnnotationTable getTable () {
    return table;
  }

  /**
   * Values need specialized code to get at the primary-mention
   */
  private class ValueModel extends AnnotationTableModel {
    public ValueModel (Task task, AnnotationType type,
                        Object[] columnSrc, String[] headings) {
      super (task, type, columnSrc, headings);
    }

    /**
     * Overridden to display mention text extent properly, calculate
     * number of metions, and allow checkboxes for generic.
     */
    public Object getValueAt (int row, int col) {
      if (col == PRIMARY_MENTION_COL) { // primary mention column
        Object annot = super.getValueAt (row, col);
        if (annot == null)
          return null;

        if (DEBUG > 2)
          System.err.println ("VE.ValueModel.getValAt: val class="+
                              annot.getClass()+" val="+annot);
        if (annot instanceof AnnotationRef)
          annot = ((AnnotationRef)annot).getElement();
        
        // TODO: should this be an RDC option? [head | extent]
        // TODO: what about a celleditor to select [head|extent] to show!

        return ((AWBAnnotation)annot).getAttributeValue("TextExtent");
        
      } else if (col == MENTION_COUNT_COL) {
        AWBAnnotation annot = (AWBAnnotation)getAnnotation(row);
        int size =
          annot.getRegion().getSubordinateSet(ACE2004Utils.QUANTITY_MENTION_TYPE).size ();
        return new Integer(size);
        
      }

      return super.getValueAt (row, col);
    }

    /** Overridden for mention count and generic columns. */
    public Class getColumnClass (int col) {
      if (col == MENTION_COUNT_COL)
        return Integer.class;
      return super.getColumnClass (col);
    }
  }

  /***********************************************************************/
  /* Implementing JawbComponent */
  /***********************************************************************/
  
  public void setJawbDocument (JawbDocument doc) {
    JawbDocument old = getJawbDocument();
    if (old != null) {
      old.getAnnotationModel().
        removeAnnotationModelListener(annotModelListener);
      old.removePropertyChangeListener (docPropListener);
    }
    if (doc != null) {
      doc.getAnnotationModel().
        addAnnotationModelListener(annotModelListener);
      doc.addPropertyChangeListener(docPropListener);
    }
    table.setJawbDocument (doc);
  }
  
  public JawbDocument getJawbDocument () {
    return table.getJawbDocument ();
  }

  public Set getSelectedAnnots () {
    return table.getSelectedAnnots ();
  }

  public Component getComponent () {
    return this;
  }

  /***********************************************************************/
  /* Implementing the AnnotationModelListener Interface */
  /***********************************************************************/

  /**
   * Update the 'mention-count' row of values when mentions are removed
   */
  private class AnnotModelListener implements AnnotationModelListener {
    public void annotationCreated (AnnotationModelEvent e) {}
    public void annotationDeleted (AnnotationModelEvent e) {}
    public void annotationChanged (AnnotationModelEvent e) {}
    public void annotationInserted (AnnotationModelEvent e) {
      AWBAnnotation annot = e.getAnnotation ();
      if (annot.getAnnotationType ().equals (ACE2004Utils.QUANTITY_TYPE)) {
        int row = table.getRow (annot);
        table.repaint (table.getCellRect(row, MENTION_COUNT_COL, true));
      }
    }
    public void annotationRemoved (AnnotationModelEvent e) {
      AWBAnnotation annot = e.getAnnotation ();
      if (annot.getAnnotationType ().equals (ACE2004Utils.QUANTITY_TYPE)) {
        int row = table.getRow (annot);
        table.repaint (table.getCellRect(row, MENTION_COUNT_COL, true));
      }
    }
  }
  
  /** Listen for Font changes and propogate to the Timex2Editor */
  private class DocPropertyListener implements PropertyChangeListener {
    public void propertyChange (PropertyChangeEvent evt) {
      String name = evt.getPropertyName ();
      if (JawbDocument.FONT_FAMILY_PROPERTY_KEY.equals (name)) {
      }
    }
  }
}
