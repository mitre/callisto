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

package org.mitre.timex2.callisto;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleConstants;

import gov.nist.atlas.*;
import gov.nist.atlas.type.*;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.TextExtentRegion;
import org.mitre.jawb.atlas.SubordinateSetsAnnotation;
import org.mitre.jawb.gui.*;
import org.mitre.jawb.swing.*;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;

// testing/debugging
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

/**
 * Render and allow user to edit Entity annotations from the text.
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */
public class SimpleTypeAnnotEditor extends JPanel implements JawbComponent {

  private static final int DEBUG = 0;
  
  private static final int TEXT_COL = 0;
  
  private static final String[] ATTRIBS = new String[] {"TextExtent", "type"};
  private static final String[] HEADINGS = new String[] {"Text", "Type"};

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
   * using <code>filterTypes</code>. filterTypes should include
   * <code>null</code> if you want a 'no filter' option.
   */
  public SimpleTypeAnnotEditor (TaskToolKit toolkit,
                                String typeName,
                                String[] filterTypes) {
    this.toolkit = toolkit;
    this.typeName = typeName;
    Task task = toolkit.getTask();
    
    AnnotationType type = task.getAnnotationType (typeName);
    if (type == null)
      throw new IllegalArgumentException ("No such AnnotationType: "+typeName);
    init (task, type, filterTypes);
  }

  private void init (Task task, AnnotationType type, String[] filterTypes) {

    // layout manager
    setLayout (new BorderLayout());

    // Filter chooser to display only one Type
    typeCombo = new JComboBox ();
    for (int i=0; i<filterTypes.length; i++) {
      if (filterTypes[i] == null) {
        typeCombo.addItem (new AnnotationTypeFilter (type) {
            public String toString () { return "<ALL>"; }
            });
      } else {
        typeCombo.addItem (new MyAttributeValueFilter ("type",filterTypes[i]));
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
    typeLabel= new JLabel ("Filter on Type", JLabel.RIGHT);
    typeLabel.setLabelFor (typeCombo);
    
    // panel for 'type filtering'
    JPanel typePanel = new JPanel ();
    typePanel.setLayout ( new BorderLayout (2,2));
    typePanel.add (typeLabel, BorderLayout.CENTER);
    typePanel.add (typeCombo,BorderLayout.EAST);
    
    add (typePanel, BorderLayout.NORTH);
    
    // create and add table (with multiple column sorting)
    AnnotationTableModel atm =
      new AnnotationTableModel (task, type, ATTRIBS, HEADINGS);
    table = new AnnotationTable (toolkit, atm);
    // table.setSelectedAnnotationExchanger (new ItemExchanger...);
    table.setAutoResizeMode(table.AUTO_RESIZE_NEXT_COLUMN);

    table.setColumnEditable (TEXT_COL, false);
    
    scrollPane = new JScrollPane (table);
    add (scrollPane, BorderLayout.CENTER);
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
