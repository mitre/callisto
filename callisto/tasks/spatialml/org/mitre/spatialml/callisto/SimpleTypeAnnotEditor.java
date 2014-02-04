/*
 * Copyright (c) 2002-2008 The MITRE Corporation
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

package org.mitre.spatialml.callisto;

import gov.nist.atlas.Annotation;
import gov.nist.atlas.type.AnnotationType;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.gui.AnnotationFilter;
import org.mitre.jawb.gui.AnnotationTable;
import org.mitre.jawb.gui.AnnotationTableModel;
import org.mitre.jawb.gui.AnnotationTypeFilter;
import org.mitre.jawb.gui.JawbComponent;
import org.mitre.jawb.gui.JawbDocument;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;

/**
 * Render and allow user to edit Entity annotations from the text.
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 * 
 * Modified by Sam to take multiple attributes. Still only one filter.
 */
public class SimpleTypeAnnotEditor extends JPanel implements JawbComponent {

  private static final int DEBUG = 0;
  
  private static final String[][] ALL_HEADINGS = 
  { { "TextExtent", "Text", "no", "no" },
    { "type", "Type", "yes", "no" }
  };
  
  private static final String[][] SHORT_HEADINGS = 
  { { "TextExtent", "Text", "no", "no" } };

  TaskToolKit toolkit;
  
  AnnotationTable table = null;
  JScrollPane scrollPane = null;

  JComboBox typeCombo = null;
  JLabel typeLabel = null;
  String typeName;


  int idNum = idCount++;
  static int idCount = 0;
  public String toString () { return "SimpTAnnotEd["+idCount+"] "; }
  
  /**
   * Creates a new simple editor which knows (assumes) that only displays
   * annotations of <code>type</code> and can filter on the 'type' attribute
   * using <code>filterValues</code>. filterValues should include
   * <code>null</code> if you want a 'no filter' option.
   */
  
  public SimpleTypeAnnotEditor (TaskToolKit toolkit,
      String typeName,
      String[] filterValues) {
    this(toolkit, typeName, 
        filterValues != null ? ALL_HEADINGS : SHORT_HEADINGS,
        filterValues != null ? "type" : null, 
        filterValues);
  }
  
  /**
   * 
   * @param toolkit
   * @param typeName
   * @param attributesAndHeadings array of arrays with inner elements: {attribute (from maia), heading (human-readable), editable ("yes"/"no", boolean ("yes"/"no")}
   * @param filterAttribute
   * @param filterValues
   */
  public SimpleTypeAnnotEditor(TaskToolKit toolkit,
      String typeName,
      String[][] attributesAndHeadings,
      String filterAttribute,
      String[] filterValues) {
 
    this.toolkit = toolkit;
    this.typeName = typeName;
    Task task = toolkit.getTask();
    
    AnnotationType type = task.getAnnotationType (typeName);
    if (type == null)
      throw new IllegalArgumentException ("No such AnnotationType: "+typeName);
    init (task, type, attributesAndHeadings, filterAttribute, filterValues);
    setName(typeName);
  }

  private void init (Task task, AnnotationType type, String[][] attributesAndHeadings,
      String filterAttribute, String[] filterValues) {

    // layout manager
    setLayout (new BorderLayout());
    AnnotationTableModel atm = null;
    String[] attributes = new String[attributesAndHeadings.length];
    String[] headings = new String[attributesAndHeadings.length];
    boolean[] editable = new boolean[attributesAndHeadings.length];
    boolean[] booleanColumns = new boolean[attributesAndHeadings.length];
    int filterAttributeIndex = -1;
    
    // Populate the arrays of attributes and headings and editable.
    
    for (int i = 0; i < attributesAndHeadings.length; i++) {
      attributes[i] = attributesAndHeadings[i][0];
      if ((filterAttribute != null) && (filterAttribute.equals(attributes[i]))) {
        filterAttributeIndex = i;
      }
      headings[i] = attributesAndHeadings[i][1];
      if (attributesAndHeadings[i].length > 2 && attributesAndHeadings[i][2].equals("yes")) {
        editable[i] = true;
      } else {
        editable[i] = false;
      }
      if (attributesAndHeadings[i].length > 3 && attributesAndHeadings[i][3].equals("yes")) {
        booleanColumns[i] = true;
      } else {
        booleanColumns[i] = false;
      }
    }
        
    // Filter chooser to display only one element of the filter attribute.
    // Make sure the filter attribute is one of the attributes, too.
    if ((filterValues != null) && (filterAttributeIndex > -1)) {
      typeCombo = new JComboBox ();
      for (int i=0; i<filterValues.length; i++) {
        if (filterValues[i] == null) {
          typeCombo.addItem (new AnnotationTypeFilter (type) {
              public String toString () { return "<ALL>"; }
            });
        } else {
          typeCombo.addItem (new MyAttributeValueFilter (filterAttribute,
                                                         filterValues[i]));
        }
      }
      typeCombo.addActionListener (new ActionListener () {
          public void actionPerformed (ActionEvent e) {
            // update the filter.
            Object filterType = typeCombo.getSelectedItem();
            table.setAnnotationFilter ((AnnotationFilter)filterType);
          }
        });
    
      // Show entity being displayed
      typeLabel= new JLabel ("Filter on " + headings[filterAttributeIndex], JLabel.RIGHT);
      typeLabel.setLabelFor (typeCombo);
    
      // panel for 'type filtering'
      JPanel typePanel = new JPanel ();
      typePanel.setLayout ( new BorderLayout (2,2));
      typePanel.add (typeLabel, BorderLayout.CENTER);
      typePanel.add (typeCombo,BorderLayout.EAST);
      
      add (typePanel, BorderLayout.NORTH);
    }
    // create and add table (with multiple column sorting)
    atm = new EnhancedAnnotationTableModel (task, type, attributes, 
        headings, booleanColumns);
    
    table = new AnnotationTable (toolkit, atm);
    table.setName(typeName);
    table.setAutoResizeMode(table.AUTO_RESIZE_NEXT_COLUMN);
    for (int i = 0; i < editable.length; i++) {
      if (!editable[i])
        table.setColumnEditable (i, false);
    }
    
    scrollPane = new JScrollPane (table);
    add (scrollPane, BorderLayout.CENTER);
  }
  
  // This class enabled Boolean columns. Stolen directly from the TIMEX task.
  
  private class EnhancedAnnotationTableModel extends AnnotationTableModel {

    private boolean[] booleanColumns;
    
    public EnhancedAnnotationTableModel(Task task, AnnotationType type, Object[] columnSrc, 
        String[] headings, boolean[] booleanColumns) {
      super(task, type, columnSrc, headings);
      this.booleanColumns = booleanColumns;
    }
    
    /**
    *  Overridden for pseudo-boolean columns
    */
    public Object getValueAt (int row, int col) {
      if ((booleanColumns != null) && booleanColumns[col]) {
        String val = (String)super.getValueAt (row, col);
        if (val != null && val.equals("YES")) {
          return Boolean.TRUE;
        } else {
          return Boolean.FALSE;
        }
      }
      return super.getValueAt (row, col);
    }
   

   /**
    *  Overridden for pseudo-boolean columns
    */
    public void setValueAt(Object value, int row, int col) {
      if ((booleanColumns != null) && booleanColumns[col]) {
        
        if (((Boolean)value).booleanValue()) {
          value = "YES";
        } else {
          value = "";
        }
      }
      super.setValueAt(value, row, col);
    }

    public Class getColumnClass (int col) {
      if ((booleanColumns != null) && booleanColumns[col]) {
        return Boolean.class;
      }
      return super.getColumnClass(col);
    }
    
  }
  
  /**
   * Get the AnnotationTable this widget supports.
   */
  public AnnotationTable getTable () {
    return table;
  }

  /***********************************************************************/
  /* Implementing JawbComponent */
  /***********************************************************************/
  
  public void setJawbDocument (JawbDocument doc) {
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
  /* For filtering annotations in the table */
  /***********************************************************************/

  protected class MyAttributeValueFilter implements AnnotationFilter {
    private String attrib;
    private String value;
    MyAttributeValueFilter (String attrib, String value) {
      if (attrib == null) throw new IllegalArgumentException ("attrib==null");
      if (value == null) throw new IllegalArgumentException ("value==null");
      this.attrib = attrib;
      this.value = value;
    }
    public boolean accept (Annotation annot) {
      AWBAnnotation awbAnnot = (AWBAnnotation) annot;
      return typeName.equals (awbAnnot.getAnnotationType().getName()) &&
        value.equals (awbAnnot.getAttributeValue (attrib));
    }
    public String getDescription () { return toString(); }
    public String toString () { return value; }
  }  
  
}
