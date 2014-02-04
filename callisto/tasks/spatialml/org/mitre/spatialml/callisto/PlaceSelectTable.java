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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 * A JTable for selecting places. needed to be pulled out to interact properly with listeners in the dialog.
 * @author jricher
 *
 */
public class PlaceSelectTable extends JTable {

  //private Map selected = null;
  
  private PlaceTableModel model;
  
  public PlaceSelectTable(List places) {
    super(new PlaceTableModel(places));
    
    model = (PlaceTableModel)getModel(); // save cast for ease of use later
    
    // set up selection
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setColumnSelectionAllowed(false);
    setRowSelectionAllowed(true);
    
    /*
    getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        //System.err.println("Selection changed by " + e.getSource());
        //System.err.println(" Rows: " + e.getFirstIndex() + " :: " + e.getLastIndex());
        //System.err.println(" Table: " + getSelectedRow());
        if (e.getSource() == getSelectionModel() && e.getFirstIndex() == e.getLastIndex()) {
          //int row = e.getLastIndex();
          int row = getSelectedRow();
          setSelected(model.getRow(row));
        }
      }
    });
     */
    
    // set up sorting headers
    JTableHeader header = getTableHeader();
    header.setUpdateTableInRealTime(true);
    header.addMouseListener(new MouseAdapter() {
    
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        
        int column = columnAtPoint(e.getPoint());
        String field = PlaceTableModel.COL_FIELDS[column];
        model.sortBy(field);
        
        // this is to repaint the headers
        for (int i = 0; i < getColumnCount(); i++) { 
          TableColumn cc = getColumnModel().getColumn(i);
          cc.setHeaderValue(getColumnName(cc.getModelIndex()));
        }
        getTableHeader().repaint();
        
        resizeAndRepaint();
      }
    
    });
    header.setReorderingAllowed(false);
    
  }
  
  /**
   * Get the currently selected place from this table, or return an empty Map if there is no selection.
   * @return
   */
  public Map getSelected() {
    //return selected;
    if (getSelectedRow() == -1) {
      return Collections.EMPTY_MAP;
    } else {
      return model.getRow(getSelectedRow());
    }
  }

  /*
  public void setSelected(Map selected) {
    if (((PlaceTableModel)getModel()).places.contains(selected)) {
      this.selected = selected;
    }
  }
*/
  private static class PlaceTableModel extends DefaultTableModel {
    
    public static final String[] COL_NAMES = new String[] { "Name", "Type", "Gaz Ref", "Lat/Long" };
    public static final String[] COL_FIELDS = new String[] { "name", "type", "gazref", "latLong" };
    
    private List places;
    private List original;
    private SortingComparator comp;
    
    public PlaceTableModel(List places) {
      super(COL_NAMES, places.size());
      this.original = places;
      this.places = new ArrayList(places); // keep a copy of natural ordering for later
      
      this.comp = new SortingComparator();
      
    }

    /**
     * Tells the model to sort by a given field. Successive calls with the same field will cycle through the behavior:
     * 
     *  1: Sort ascending by field
     *  2: Sort descending by field
     *  3: Undo sort (natural sorting)
     */
    public void sortBy(String field) {
      if (comp.getCompareField() == null || comp.getCompareField().equals("") || !comp.getCompareField().equals(field)) {
        // first call, sort ascending
        comp.setCompareField(field);
        comp.setAscending(true);
        Collections.sort(places, comp);
      } else if (comp.getCompareField().equals(field) && comp.isAscending()) {
        // second call, sort descending
        comp.setAscending(false);
        Collections.sort(places, comp);
      } else {
        // third call, no sorting
        comp.setCompareField(""); // undo sorter
        this.places = new ArrayList(original);
      }
      
      System.err.println("Sorter: " + comp);
      
    }
    
    public Object getValueAt(int row, int column) {
      Map place = (Map)places.get(row);
      return place.get(COL_FIELDS[column]);
    }
    
    public String getColumnName(int column) {
      String name = super.getColumnName(column);
      if (COL_FIELDS[column].equals(comp.getCompareField())) {
        if (comp.isAscending()) {
          return name + " ↑";
        } else {
          return name + " ↓";
        }
      } else {
        return name;
      }
    }

    public boolean isCellEditable(int row, int column) {
      return false;
    }
    
    public Map getRow(int row) {
      return (Map)places.get(row);
    }
    
    
  }

  private static class SortingComparator implements Comparator {

    private String compareField = "";
    private boolean ascend = true;
    
    public int compare(Object arg0, Object arg1) {
      
      if (arg0 instanceof Map && arg1 instanceof Map) {
        Map p0 = (Map)arg0;
        Map p1 = (Map)arg1;
        
        if (compareField != null && !compareField.equals("")) {
          if (p0.containsKey(compareField)) {
            if (p1.containsKey(compareField)) {
              return ((String)p0.get(compareField)).compareTo((String)p1.get(compareField)) * (ascend ? 1 : -1); // ascend flips things around
            } else {
              // "no key" always < key
              return -1;
            }
          } else {
            if (p1.containsKey(compareField)) {
              // "no key" always < key
              return 1;
            } else {
              // they both don't have this key, they're equal as far as we care
              return 0;
            }
          }
        } else {
          // there's no key to compare against, they're equal as far as we care
          return 0;
        }
        
      } else {
        throw new IllegalArgumentException("Can only compare two maps of strings");
      }
      
    }

    public String toString() {
      if (getCompareField() == null || getCompareField().equals("")) {
        return "SortingComparator: not sorting";
      } else {
        return "SortingComparator: sorting by " + getCompareField() + ", " + (isAscending() ? " ascending" : " descending");
      }
    }
    
    public boolean isAscending() {
      return ascend;
    }

    public void setAscending(boolean ascend) {
      this.ascend = ascend;
    }

    public String getCompareField() {
      return compareField;
    }

    public void setCompareField(String compareField) {
      this.compareField = compareField;
    }
    
  }
  
}
