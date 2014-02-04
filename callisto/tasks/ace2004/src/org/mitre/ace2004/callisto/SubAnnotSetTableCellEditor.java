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

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Vector;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.EventObject;

import org.mitre.jawb.atlas.*;
import org.mitre.jawb.gui.*;


public class SubAnnotSetTableCellEditor extends JList
  implements TableCellEditor {

  private static final int DEBUG = 0;

  private DefaultCellEditor defaultEditor;

  /**
   * Create a TableCellEditor for SubAnnotation sets that uses the default
   * cell editor and renderer.
   */
  public SubAnnotSetTableCellEditor () {
    this (null, null);
  }
  /**
   * Create a JList that acts as an editor for the elements of a
   * subAnnotation (AWBAnnotation) array, using the specified renderer
   * to render each item in the list, and the specified editor to edit them
   */
  public SubAnnotSetTableCellEditor (ListCellRenderer renderer,
				     ListSelectionListener listener) {
    super();
    if (renderer != null) {
      setCellRenderer (renderer);
    }
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    if (listener != null) {
      addListSelectionListener(listener);
    }
    defaultEditor = new DefaultCellEditor(new JCheckBox());
    
  }

  /* implements TableCellEditor */
  public Component getTableCellEditorComponent(JTable table,
					       Object value,
					       boolean isSelected,
					       int row, int column) {
    if (DEBUG > 0)
      System.err.println("SASTCE.getTableCellEditorComponent called");
    setValue(value);
    return this;
  }
  
  private void setValue (Object value) {
    //    if (value == null)
    //  setModel (emptyListModel);
    //else {  // can we cache a data model object for less allocation?
    if (DEBUG > 0)
      System.err.println ("SubAnnotTCE.setVal: value Class is " +
                          value.getClass().getName());
    List annots = Arrays.asList ((Object[])value);
    if (DEBUG > 0)
      System.err.println ("SubAnnotTCE.setVal: size="+annots.size ()+
                          " val="+annots);
    setListData ((AWBAnnotation[]) value);
      //}
  }
  
  /* implements CellEditor (superinterface of TableCellEditor) */
  public void addCellEditorListener(CellEditorListener cel) {
    if (DEBUG > 0)
      System.err.println("SASTCE.addCellEditorListener(" + cel + ")");
    defaultEditor.addCellEditorListener(cel);
  }

  public void cancelCellEditing() {
    if (DEBUG > 0)
      System.err.println("SASTCE.cancelCellEditing()");
    defaultEditor.cancelCellEditing();
  }

  public Object getCellEditorValue() {
    if (DEBUG > 0)
      System.err.println("SASTCE.getCellEditorValue()");
    return defaultEditor.getCellEditorValue();
  }

  public boolean isCellEditable(EventObject anEvent) {
    if (DEBUG > 0)
      System.err.println("SASTCE.isCellEditable(" + anEvent + ")");
    return defaultEditor.isCellEditable(anEvent);
  }

  public void removeCellEditorListener(CellEditorListener cel) {
    if (DEBUG > 0)
      System.err.println("SASTCE.removeCellEditorListener(" + cel + ")");
    defaultEditor.removeCellEditorListener(cel);
  }

  public boolean shouldSelectCell(EventObject anEvent) {
    if (DEBUG > 0)
      System.err.println("SASTCE.shouldSelectCell(" + anEvent + ")");
    return defaultEditor.shouldSelectCell(anEvent);
  }
  public boolean stopCellEditing() {
    if (DEBUG > 0)
      System.err.println("SASTCE.stopCellEditing()");
    return defaultEditor.stopCellEditing();
  }

  
}
