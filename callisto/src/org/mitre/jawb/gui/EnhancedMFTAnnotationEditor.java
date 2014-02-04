
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
 * Created on Feb 1, 2007 at 11:43:27 AM by Galen B. Williamson
 */
package org.mitre.jawb.gui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.MessageFormat;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.mitre.jawb.swing.FontIndependentCellRenderer;
import org.mitre.jawb.swing.TableSorter;
import org.mitre.jawb.tasks.TaskToolKit;

public class EnhancedMFTAnnotationEditor extends MultiFilterTypeAnnotEditor {
  /**
   *
   */
  private int displayedRows;

  protected class HoverExpandingAnnotationTable extends AnnotationTable {
    private final String TIP_FORMAT = "<html><body><font style=\"font-family: {1}; font-size: {2,number,integer}pt\">{0}</font></body></html>";

    protected HoverExpandingAnnotationTable(TaskToolKit kit, AnnotationTableModel model) {
      super(kit, model);
    }

    public String getToolTipText(MouseEvent event) {
      AnnotationTable table = getTable();
      Point point = event.getPoint();
      int row = table.rowAtPoint(point);
      int col = table.columnAtPoint(point);
      if (row < 0 || col < 0) {
        return null;
      }
      Object val = table.getValueAt(row, col);
      String text = null;
      if (val instanceof String) {
        text = (String) val;
        TableColumn column = table.getColumnModel().getColumn(col);
        TableCellRenderer renderer = column.getCellRenderer();
        boolean selected = table.isCellSelected(row, col);
        boolean hasFocus = table.hasFocus();
        Component c = renderer.getTableCellRendererComponent(table, text,
            selected, hasFocus, row, col);
        Dimension preferredSize = c.getPreferredSize();
        Rectangle cellRect = table.getCellRect(row, col, true);
        if (preferredSize.width <= cellRect.width) {
          text = null;
        }
        else {
          Font f = c.getFont();
          text = MessageFormat.format(TIP_FORMAT, new Object[] {
              text,
              f.getFamily() + "; font-weight: bold",
              new Integer(Math.max(16, f.getSize())),
          });
        }
      }
      return text;
    }

    public Dimension getPreferredScrollableViewportSize() {
      return getPreferredScrollableViewportSize(getDisplayedRows());
    }

    protected Dimension getPreferredScrollableViewportSize(int displayedRows) {
      Dimension d = super.getPreferredScrollableViewportSize();
      if (d == null) {
        d = getPreferredSize();
      }
      if (displayedRows >= 0) {
        d.height = displayedRows * getRowHeight();
      }
      else {
        int rowCount = getRowCount();
        d.height = Math.min(d.height, getRowHeight() * rowCount);
      }
      return d;
    }
  }

  protected class MouseHandler implements MouseWheelListener {
    public void mouseWheelMoved(MouseWheelEvent e) {
      Point locationOnScreen = getScrollPane().getLocationOnScreen();
      Point p = e.getPoint();
      Point tableLocationOnScreen = getTable().getLocationOnScreen();
      Point checkPoint = new Point(p.x + locationOnScreen.x - tableLocationOnScreen.x,
          p.y + locationOnScreen.y - tableLocationOnScreen.y);
      final MouseEvent me = new MouseEvent(getTable(), e.getID(), e.getWhen(), e.getModifiers(), checkPoint.x, checkPoint.y, 0, false);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          ToolTipManager.sharedInstance().mouseMoved(me);
        }
      });
    }
  }

  private int maxDisplayedRows = -1;
  private int minDisplayedRows = -1;

  public Dimension getMinimumSize() {
    if (minDisplayedRows < 0)
      return super.getMinimumSize();
    int old = getDisplayedRows();
    try {
      setDisplayedRows(minDisplayedRows);
      return getPreferredSize();
    } finally {
      setDisplayedRows(old);
    }
  }

  public Dimension getMaximumSize() {
    if (maxDisplayedRows < 0)
      return super.getMaximumSize();
    int old = getDisplayedRows();
    try {
      setDisplayedRows(maxDisplayedRows);
      return getPreferredSize();
    } finally {
      setDisplayedRows(old);
    }
  }
  
  public EnhancedMFTAnnotationEditor(TaskToolKit toolkit, String name, Object[][] headings, boolean useSelectionModel) {
    this(toolkit, name, headings, null, useSelectionModel, -1, -1);
  }

  public EnhancedMFTAnnotationEditor(TaskToolKit toolkit, String name, Object[][] headings, 
      Object[] objects, 
      boolean useSelectionModel,
      int rows) {
    this(toolkit, name, headings, objects, useSelectionModel, rows, -1);
  }

  public EnhancedMFTAnnotationEditor(TaskToolKit toolkit, String name, Object[][] headings) {
    this(toolkit, name, headings, null, false, -1, -1);
  }

  public EnhancedMFTAnnotationEditor(TaskToolKit toolkit, String name, Object[][] headings,
      Object[] lists) {
    this(toolkit, name, headings, lists, false, -1, -1);
  }

  public EnhancedMFTAnnotationEditor(TaskToolKit toolkit, String name, Object[][] headings,
      Object[] objects, int rows) {
    this(toolkit, name, headings, objects, false, rows, -1);
  }

  public EnhancedMFTAnnotationEditor(TaskToolKit toolkit, String typeName, Object[][] attributesAndHeadings, Object[] filterValueLists, boolean useSelectionModel) {
    this(toolkit,
    	typeName,
    	attributesAndHeadings,
    	filterValueLists,
    	useSelectionModel, -1, -1);
  }
  public EnhancedMFTAnnotationEditor(TaskToolKit toolkit, String name, Object[][] headings,
      Object[] objects, int maxRows, int minRows) {
    this(toolkit, name, headings, objects, false, maxRows, minRows);
  }

  public EnhancedMFTAnnotationEditor(TaskToolKit toolkit, String name, Object[][] headings,
        Object[] objects, boolean useSelectionModel, int maxRows, int minRows) {
    super(toolkit, name, headings, objects, useSelectionModel);
    this.maxDisplayedRows = maxRows;
    this.minDisplayedRows = minRows;
    setDisplayedRows(maxRows);
    if (getDisplayedRows() < 0) {
      setDisplayedRows(minRows);
    }
    TableModel m = getTable().getModel();
    while (m instanceof TableSorter) {
      m = ((TableSorter) m).getModel();
    }
    AnnotationTableModel atm = null;
    if (m instanceof AnnotationTableModel) {
      atm = (AnnotationTableModel) m;
    }
    /*
    for (int i = 0; i < headings.length; i++) {
      Object[] heading = headings[i];
      if (heading != null && heading.length > 0) {
        if (heading[0] == null) {
          if (heading.length > 1 && "ID".equals(heading[1])) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setCellRenderer(new IdRenderer());
            continue;
          }
        }
        if (atm != null && atm.getColumnClass(i) == String.class) {
          TableColumn column = table.getColumnModel().getColumn(i);
          column.setCellRenderer(new FontIndependentCellRenderer());
        }
      }
    }
    //*/
    for (int i = 0; i < headings.length; i++) {
      Object[] heading = headings[i];
      if (heading != null && heading.length > 0) {
        TableCellRenderer renderer = getRenderer(heading);
        if (renderer != null) {
          TableColumn column = table.getColumnModel().getColumn(i);
          column.setCellRenderer(renderer);
        }
        if (atm != null && atm.getColumnClass(i) == String.class) {
          TableColumn column = table.getColumnModel().getColumn(i);
          column.setCellRenderer(new FontIndependentCellRenderer());
        }
      }
    }
    MouseHandler mouseInputAdapter = new MouseHandler();
    JScrollPane sp = getScrollPane();
    if (sp != null) {
      sp.addMouseWheelListener(mouseInputAdapter);
    }
    ToolTipManager.sharedInstance().registerComponent(getTable());
  }

  protected TableCellRenderer getRenderer(Object[] heading) {
    return null;
  }
  
  protected AnnotationTable createTable(AnnotationTableModel atm) {
    AnnotationTable table = new HoverExpandingAnnotationTable(toolkit, atm);
    return table;
  }

  public void setDisplayedRows(int displayedRows) {
    this.displayedRows = displayedRows;
  }

  public int getDisplayedRows() {
    return displayedRows;
  }
}