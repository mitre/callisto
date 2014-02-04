
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

package org.mitre.jawb.swing;

import java.awt.Component;
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

/**
 * A sorter for TableModels. The sorter has a model (conforming to TableModel)
 * and itself implements TableModel. TableSorter does not store or copy the
 * data in the TableModel, instead it maintains an array of integers which it
 * keeps the same size as the number of rows in its model. When the model
 * changes it notifies the sorter that something has changed eg. "rowsAdded"
 * so that its internal array of integers can be reallocated. As requests are
 * made of the sorter (like getValueAt(row, col) it redirects them to its
 * model via the mapping array. That way the TableSorter appears to hold
 * another copy of the table with the rows in a different order. The sorting
 * algorthm used is stable which means that it does not move around rows when
 * its comparison function returns 0 to denote that they are equivalent.  <p>
 *
 * Will sort any column in ascending order on when first clicking the
 * column. Second click will get descending order, and a third click. will get
 * original order. A different tableCellRrenderer is added to the tables <p>
 *
 * <code>
 *  TableSorter sorter = new TableSorter (myModel);
 *  table = new JTable (sorter);
 *  sorter.addMouseListenerToHeaderInTable (table);
 * </code>
 *
 * Note that this adds a <code>TableColumnModelListner</code> to the tables
 * TableColumnModel. Results will be undefined if you change the
 * TableColumnModel (other than setting it's column header values) after the
 * above proceedure.
 *
 * For further documentation, see <a href="http://java.sun.com/docs/books/tutorial/uiswing/components/table.html#data">Creating a Table Model</a>
 * in <em>The Java Tutorial</em>.
 * 
 * @version 1.5 12/17/97
 * @author Philip Milne
 *
 * @version 1.6 2001-10-14
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 */
public class TableSorter extends TableMap {

  private static final int DEBUG = 0;
  
  HashMap classComparators = new HashMap();
  int             indexes[];
  boolean         ascending = true;
  int compares;

  ListSelectionModel listSelector = null;
  int      sortColumn = -1;
  int      sortOrder = 0;

  // may have to reference this single object every time table is resized
  SortButtonCellRenderer sortedRenderer = new SortButtonCellRenderer ();
     
  public TableSorter() {
    indexes = new int[0]; // for consistency
  }

  public TableSorter(TableModel model) {
    this(model,null);
  }

  public TableSorter(TableModel model, ListSelectionModel listSelector) {
    setModel(model);
    setSelectionModel(listSelector);
  }

  /**
   * Return comparator used to sort the specified class, or null if sort is by
   * natural order.
   */
  public Comparator getClassComparator(Class columnClass) {
    return (Comparator) classComparators.get(columnClass);
  }

  /**
   * Set's the comparator used to sort a column of the specified class. If set
   * to null, sort is by natural order, which is the default.
   */
  public void setClassComparator(Class columnClass, Comparator comparator) {
    classComparators.put(columnClass, comparator);
    boolean ascend = (sortOrder == SortButtonCellRenderer.DOWN);
    sortByColumn(sortColumn, ascend);
  }

  public void setModel(TableModel model) {
    super.setModel(model);
    reallocateIndexes(); 
  }

  public void setSelectionModel(ListSelectionModel listSelector) {
      this.listSelector = listSelector;
  }

  public int compareRowsByColumn(int row1, int row2, int column) {
    TableModel data = model;

    // Check for nulls.

    Object o1 = data.getValueAt(row1, column);
    Object o2 = data.getValueAt(row2, column); 

    // If both values are null, return 0.
    if (o1 == null && o2 == null) {
      return 0; 
    } else if (o1 == null) { // Define null less than everything. 
      return -1; 
    } else if (o2 == null) { 
      return 1; 
    }

    Class type = model.getColumnClass(column);
    Comparator comparator = (Comparator) classComparators.get(type);

    /*
     * We copy all returned values from the getValue call in case an optimised
     * model is reusing one object to return many values.  Although we're not
     * going to clone, so when a comparator is used, 'optimized' model will
     * fail. The Number subclasses in the JDK are immutable and so will not be
     * used in this way but other subclasses of Number might want to do this to
     * save space and avoid unnecessary heap allocation.
     */

    if (type.getSuperclass() == java.lang.Number.class) {
      Number n1 = (Number)data.getValueAt(row1, column);
      double d1 = n1.doubleValue();
      Number n2 = (Number)data.getValueAt(row2, column);
      double d2 = n2.doubleValue();

      if (comparator != null)
        return comparator.compare(n1, n2);
    
      if (d1 < d2) {
        return -1;
      } else if (d1 > d2) {
        return 1;
      } else {
        return 0;
      }
    } else if (type == java.util.Date.class) {
      Date d1 = (Date)data.getValueAt(row1, column);
      long n1 = d1.getTime();
      Date d2 = (Date)data.getValueAt(row2, column);
      long n2 = d2.getTime();

      if (comparator != null)
        return comparator.compare(d1, d2);
    
      if (n1 < n2) {
        return -1;
      } else if (n1 > n2) {
        return 1;
      } else {
        return 0;
      }
    } else if (type == String.class) {
      String s1 = (String)data.getValueAt(row1, column);
      String s2 = (String)data.getValueAt(row2, column);
      
      if (comparator != null)
        return comparator.compare(s1, s2);
      
      int result = s1.compareTo(s2);
      
      if (result < 0) {
        return -1;
      } else if (result > 0) {
        return 1;
      } else {
        return 0;
      }
    } else if (type == Boolean.class) {
      Boolean bool1 = (Boolean)data.getValueAt(row1, column);
      boolean b1 = bool1.booleanValue();
      Boolean bool2 = (Boolean)data.getValueAt(row2, column);
      boolean b2 = bool2.booleanValue();

      if (comparator != null)
        return comparator.compare(bool1, bool2);
      
      if (b1 == b2) {
        return 0;
      } else if (b1) { // Define false < true
        return 1;
      } else {
        return -1;
      }
    } else {
      Object v1 = data.getValueAt(row1, column);
      String s1 = v1.toString();
      Object v2 = data.getValueAt(row2, column);
      String s2 = v2.toString();

      if (comparator != null)
        return comparator.compare(v1, v2);
      
      int result = s1.compareTo(s2);

      if (result < 0) {
        return -1;
      } else if (result > 0) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  public int compare(int row1, int row2) {
    if (sortColumn != -1) {
      compares++;
      int result = compareRowsByColumn(row1, row2, sortColumn);
      if (result != 0) {
        return ascending ? result : -result;
      }
    }
    return 0;
  }

  public void reallocateIndexes() {
    if (DEBUG > 3) System.err.println ("  TSorter.reallocateIndexes");

    int rowCount = model.getRowCount();

    // Set up a new array of indexes with the right number of elements
    // for the new data model.
    int firstNew = 0;
    if (true) {
      indexes = new int[rowCount];
      
    } else {
      // TODO: this has trouble when items are removed: some old values will be
      // out of range.
      int[] oldIndexes = indexes;
      indexes = new int[rowCount];
      firstNew = oldIndexes.length;
      System.arraycopy (oldIndexes, 0, indexes, 0, Math.min(rowCount,firstNew));
    }

    // Initialise with the identity mapping.
    for (int row = firstNew; row < rowCount; row++)
      indexes[row] = row;
    
    sort(this);
  }

  public void tableChanged(TableModelEvent e) {
    int eType = e.getType();
    int firstRow = e.getFirstRow();
    int lastRow = e.getLastRow();
    int column = e.getColumn();

    if (DEBUG > 0) {
      String eTypeString = "UNKNOWN";
      switch (eType) {
      case TableModelEvent.INSERT:
        eTypeString = "INSERT";
        break;
      case TableModelEvent.UPDATE:
        eTypeString = "UPDATE";
        break;
      case TableModelEvent.DELETE:
        eTypeString = "DELETE";
        break;
      }
      System.err.print("TSorter: tableChanged: ("+eTypeString+",("+
                       firstRow+"..."+lastRow+"),"+column+")"); 
      System.err.println(" [sortCol="+sortColumn+",ord="+sortOrder+"]");
      //Thread.dumpStack ();

      // don't do this!!!! nasty things happen!
      /*
        if (indexes.length > 0) {
        Object o = model.getValueAt(indexes[0], 0);
        System.err.println(" (1)="+o);
        }
      */
    }

    // do we need to resort on DELETE?
    // do we need to send an update event for _everything_?
    // reallocation now sorts
    reallocateIndexes();
    if ((eType==TableModelEvent.INSERT)||
        (eType==TableModelEvent.DELETE)) {
      if (sortColumn!=-1) {
        //e = new TableModelEvent(this,0,Integer.MAX_VALUE,column,type);
        //return;
      }
      
    } else if (eType==TableModelEvent.UPDATE) {
      if ((column!=-1) && (column==sortColumn)) {
        //   nope    sortByColumn(sortColumn, ascending);
        //sort(this);
        //e = new TableModelEvent(this,0,Integer.MAX_VALUE,column);
        /*
        if ((listSelector!=null)&&(firstRow==lastRow)) {
          int mRow = convertRowIndexToModel(firstRow);
          listSelector.setSelectionInterval(mRow,mRow);
          }*/
        e = new TableModelEvent(this,0,Integer.MAX_VALUE,column,eType);
        //return;
      }
    }
    super.tableChanged(e);
  }

  public void checkModel() {
    if (indexes.length != model.getRowCount()) {
      System.err.println("TSorter not informed of a change in model: "+
                         "indexLen="+indexes.length+
                         " rowCount="+model.getRowCount());
    }
  }

  public void sort(Object sender) {
    if (DEBUG > 0) {
      System.err.println("T-Sorting-: [col="+sortColumn+",ord="+sortOrder+"]");
      //Thread.dumpStack();
    }
    checkModel();

    compares = 0;
    // n2sort();
    // qsort(0, indexes.length-1);
    shuttlesort((int[])indexes.clone(), indexes, 0, indexes.length);
    if (DEBUG > 3) System.out.println("Compares: "+compares);
  }

  public void n2sort() {
    for (int i = 0; i < getRowCount(); i++) {
      for (int j = i+1; j < getRowCount(); j++) {
        if (compare(indexes[i], indexes[j]) == -1) {
          swap(i, j);
        }
      }
    }
  }

  // This is a home-grown implementation which we have not had time
  // to research - it may perform poorly in some circumstances. It
  // requires twice the space of an in-place algorithm and makes
  // NlogN assigments shuttling the values between the two
  // arrays. The number of compares appears to vary between N-1 and
  // NlogN depending on the initial order but the main reason for
  // using it here is that, unlike qsort, it is stable.
  public void shuttlesort(int from[], int to[], int low, int high) {
    if (high - low < 2) {
      return;
    }
    //if (DEBUG > 3) System.out.println("  TSorter.shuttle: col="+sortColumn);

    int middle = (low + high)/2;
    shuttlesort(to, from, low, middle);
    shuttlesort(to, from, middle, high);

    int p = low;
    int q = middle;

    /* This is an optional short-cut; at each recursive call,
       check to see if the elements in this subset are already
       ordered.  If so, no further comparisons are needed; the
       sub-array can just be copied.  The array must be copied rather
       than assigned otherwise sister calls in the recursion might
       get out of sinc.  When the number of elements is three they
       are partitioned so that the first set, [low, mid), has one
       element and and the second, [mid, high), has two. We skip the
       optimisation when the number of elements is three or less as
       the first compare in the normal merge will produce the same
       sequence of steps. This optimisation seems to be worthwhile
       for partially ordered lists but some analysis is needed to
       find out how the performance drops to Nlog(N) as the initial
       order diminishes - it may drop very quickly.  */

    if (high - low >= 4 && compare(from[middle-1], from[middle]) <= 0) {
      for (int i = low; i < high; i++) {
        to[i] = from[i];
      }
      return;
    }

    // A normal merge. 

    for (int i = low; i < high; i++) {
      if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) {
        to[i] = from[p++];
      }
      else {
        to[i] = from[q++];
      }
    }
  }

  public void swap(int i, int j) {
    if (DEBUG > 3) System.err.println ("  TSorter.swap["+i+","+j+"]");
    int tmp = indexes[i];
    indexes[i] = indexes[j];
    indexes[j] = tmp;
  }

  /**
   * Returns the row number in the model for an element, given it's row in the
   * view. Adjusts for sorting.
   * @throws IndexOutOfBoundException if row is not < table size.
   */
  public int convertRowIndexToModel (int aRow) {
    return indexes[aRow];
  }
  /**
   * Returns the view's row number for an element, given it's row in the
   * model. This adjusts for sorting.
   * @return index >= 0 if the row is valid, or <0 if not.
   */
  public int convertRowIndexToView (int aRow) {
    /*** You can't use binarySearch to search an unsorted array!!! 
    if (DEBUG > 0)
      System.err.println("TableSorter.convRowInd2View searching " +
                         Arrays.toString(indexes) + " for " + aRow +
                         " returns " + Arrays.binarySearch (indexes, aRow));
    return Arrays.binarySearch (indexes, aRow);
    *****/
    if (DEBUG > 0)
      System.err.println("TableSorter.convRowInd2View searching " +
                         Arrays.toString(indexes) + " for " + aRow +
                         " returns " + getIndexInArray (indexes, aRow));
    return getIndexInArray (indexes, aRow);
  }

  private int getIndexInArray (int[] array, int value) {
    for (int i = 0; i<array.length; i++) {
      if (array[i] == value) {
        return i;
      }
    }
    return -1;
  }

  // The mapping only affects the contents of the data rows.
  // Pass all requests to these rows through the mapping array: "indexes".
/*
  public int getModelRow(int aRow) {
      return indexes[aRow];
  }
*/
  public Object getValueAt(int aRow, int aColumn) {
    checkModel();
    if (DEBUG > 3)
      System.err.println ("  TSort.getValue("+aRow+","+aColumn+
                          ") -> ("+indexes[aRow]+","+aColumn+")");
    if (aRow >= 0 && aRow < indexes.length)
      return model.getValueAt(indexes[aRow], aColumn);
    return null;
  }

  public boolean isCellEditable(int row, int column) {
    return model.isCellEditable(indexes[row], column);
  }

  public void setValueAt(Object aValue, int aRow, int aColumn) {
    if (DEBUG > 3)
      System.err.println ("  TSorter.setValuesAt("+aValue+","+aRow+","+aColumn+")");
    checkModel();
    if (aRow >= 0 && aRow < indexes.length) {
      model.setValueAt(aValue, indexes[aRow], aColumn);
    }
  }

  public void sortByColumn(int column) {
    if (DEBUG > 3) System.err.println ("  TSorter.sortByColumn");
    sortByColumn(column, true);
  }

  public void sortByColumn(int column, boolean ascending) {
    if (DEBUG > 3) System.err.println ("  TSorter.sortByColumn()");
    if (column == -1) {
      if (DEBUG > 1)
        System.err.println ("sorting indices");
      checkModel();
      Arrays.sort (indexes);
    }
    else {
      this.ascending = ascending;
      sortColumn = column;
      sort(this);
    }
    super.tableChanged(new TableModelEvent(this)); 
  }

  // There is no-where else to put this. 
  // Add a mouse listener to the Table to trigger a table sort 
  // when a column heading is clicked in the JTable.
  // add your table to this after setting font colors/sizes
  public void addMouseListenerToHeaderInTable(JTable table) {
    final TableSorter sorter = this; 
    final JTable tableView = table; 
    tableView.setColumnSelectionAllowed(false);

    TableColumnModelListener columnListener = new TableColumnModelListener () {

        /** make added columns use the SortButtonCellRenderer */
        public void columnAdded(TableColumnModelEvent e) {
          TableColumnModel columnModel = (TableColumnModel)e.getSource();
          for (int i=e.getFromIndex(); i<e.getToIndex(); i++) {
            TableColumn column = columnModel.getColumn(i);
            column.setHeaderRenderer(sortedRenderer);
          }
        }
        /** unsort if currently sorted column is removed, and watch
         * indices. is this really needed? */
        public void columnRemoved(TableColumnModelEvent e) {
          TableColumnModel columnModel = (TableColumnModel)e.getSource();
          for (int i=e.getFromIndex(); i<e.getToIndex(); i++) {
            if (i == sortColumn) {
              sortColumn = -1;
              sorter.sortByColumn(-1, true);
            }
            else if (i < sortColumn)
              sortColumn--;
          }
        }
        public void columnMoved(TableColumnModelEvent e) {}
        public void columnMarginChanged(ChangeEvent e) {}
        public void columnSelectionChanged(ListSelectionEvent e) {}
      };
    
    TableColumnModel columnModel = tableView.getColumnModel();
    columnModel.addColumnModelListener(columnListener);
    
    // make all initial columns use the SortButtonCellRenderer
    for (int i=0; i<model.getColumnCount (); i++)
      columnModel.getColumn(i).setHeaderRenderer(sortedRenderer);
    
    
    MouseAdapter listMouseListener = new MouseAdapter() {
        
        /** set the headerValue so sort order icon is displayed */
        protected void setHeaderIcon (int viewColumn, int sortOrder) {
          // column is model index
          TableColumnModel columnModel = tableView.getColumnModel();
          TableColumn tColumn = columnModel.getColumn(viewColumn);
          Object headerValue = tColumn.getHeaderValue ();
          SortButtonCellRenderer.SortValue sValue;
          
          if (headerValue instanceof SortButtonCellRenderer.SortValue)
            sValue = (SortButtonCellRenderer.SortValue)headerValue;
          else {
            String name = getColumnName
              (tableView.convertColumnIndexToModel(viewColumn));
            sValue = new SortButtonCellRenderer.SortValue (name);
            tColumn.setHeaderValue (sValue);
          }
          sValue.setSortOrder (sortOrder);
        }
        
        public void mouseClicked(MouseEvent e) {
          if (e.getClickCount() != 1 || e.getButton() != e.BUTTON1)
            return;
          
          TableColumnModel columnModel = tableView.getColumnModel();
          int viewColumn = columnModel.getColumnIndexAtX(e.getX()); 
          int column = tableView.convertColumnIndexToModel(viewColumn);
          
          if (column == -1)
            return;
          
          // cycling if same column again, reset for new column
          if (sortColumn == column) {
            sortOrder = (sortOrder+1) % SortButtonCellRenderer.SORT_ORDER_MOD;
            sortColumn = ((sortOrder == 0) ? -1 : column);
          }
          else {
            // make sure previously sorted column displays no icon
            if (sortColumn != -1) {
              int oldViewColumn =
                tableView.convertColumnIndexToView (sortColumn);
              setHeaderIcon (oldViewColumn, SortButtonCellRenderer.NONE);
            }
            sortOrder = 1;
            sortColumn = column;
          }
          // make sure the clicked column displays proper (or no) icon
          setHeaderIcon (viewColumn, sortOrder);
          tableView.getTableHeader().repaint();
          
          // sort! if sortColumn was set to -1, original order reset
          boolean ascend = (sortOrder == SortButtonCellRenderer.DOWN);
          sorter.sortByColumn(sortColumn, ascend);
        }
      };
        
    JTableHeader th = tableView.getTableHeader();
    th.addMouseListener(listMouseListener);
  }
  
}
