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

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;

import org.mitre.jawb.atlas.AWBAnnotation;

/**
 * This class inherits from JList in order to render a cell value which is an
 * array. This is implemented after {@link DefaultTableCellRenderer}, as such
 * it has modified behaviour from usual JLists. See the <a
 * href="DefaultTableCellRenderer#override">Implementation Note</a> of
 * DefaultTableCellRenderer for more details.
 */
public class SubAnnotSetTableCellRenderer extends JList
  implements TableCellRenderer {

  private static final int DEBUG = 0;

  // We need a place to store the color the JList should be returned 
  // to after its foreground and background colors have been set 
  // to the selection background color. 
  private Color unselectedForeground; 
  private Color unselectedBackground;

  private static final ListModel emptyListModel = new AbstractListModel () {
      public int getSize() { return 1; }
      public Object getElementAt (int i) { return null; };
    };
  
  /**
   * Creates a TableCellRenderer which uses the default means of rendering the
   * individual items of the arrary. The default means is to use a JLabel to
   * render each cell of the list.
   */
  public SubAnnotSetTableCellRenderer () {
    this (null);
  }
  
  /**
   * Creates a TableCellRenderer for SubAnnotSets which uses the specified
   * renderer for each item in the list.
   */
  public SubAnnotSetTableCellRenderer (ListCellRenderer itemRenderer) {
    super ();
    if (itemRenderer != null)
      setCellRenderer (itemRenderer);
    setOpaque (true);

    // list model allows _no_ selection
//    setSelectionModel (new EmptyListSelectionModel ());
  }

  /**
   * Overrides <code>JComponent.setForeground</code> to assign
   * the unselected-foreground color to the specified color.
   * 
   * @param c set the foreground color to this value
   */
  public void setForeground(Color c) {
    super.setForeground(c); 
    unselectedForeground = c; 
  }
  
  /**
   * Overrides <code>JComponent.setBackground</code> to assign
   * the unselected-background color to the specified color.
   *
   * @param c set the background color to this value
   */
  public void setBackground(Color c) {
    super.setBackground(c); 
    unselectedBackground = c; 
  }
  
  // implements javax.swing.table.TableCellRenderer
  /**
   * Returns the JList table cell renderer.
   *
   * @param table  the <code>JTable</code>
   * @param value  the value to assign to the cell at
   *			<code>[row, column]</code>
   * @param isSelected true if cell is selected
   * @param hasFocus true if cell has focus
   * @param row  the row of the cell to render
   * @param column the column of the cell to render
   * @return the default table cell renderer
   */
  public Component getTableCellRendererComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row, int column) {
    
    if (isSelected) {
      super.setForeground(table.getSelectionForeground());
      super.setBackground(table.getSelectionBackground());
    }
    else {
      super.setForeground((unselectedForeground != null) ?
                          unselectedForeground : table.getForeground());
      super.setBackground((unselectedBackground != null) ?
                          unselectedBackground : table.getBackground());
    }
    
    setFont(table.getFont());
    // nothing special for focus just now
    /*
      if (hasFocus) {
      setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
      if (table.isCellEditable(row, column)) {
      super.setForeground( UIManager.getColor("Table.focusCellForeground") );
      super.setBackground( UIManager.getColor("Table.focusCellBackground") );
      }
      } else {
      setBorder(noFocusBorder);
      }
    */
    setValue(value, table, row); 

    return this;
  }
  
  private void setValue (Object value, JTable table, int row) {
    if (value == null)
      setModel (emptyListModel);
    else {  // can we cache a data model object for less allocation?
      if (DEBUG > 0)
        System.err.println ("SubAnnotTCR.setVal: value Class is " +
                            value.getClass().getName());
      Object[] annots = (Object[]) value;
      //List annots = Arrays.asList ((Object[])value);
      if (DEBUG > 0)
        System.err.println ("SubAnnotTCR.setVal: size="+annots.length+
                            " val="+Arrays.asList(annots));
      setListData ((AWBAnnotation[]) value);
 
      // get the current default height for all rows
      int baseHeight = table.getRowHeight();
      // if more than one value, re-set table row height accordingly
      if (annots.length > 1) {
	int intercellHeight = table.getRowMargin();
	int desiredHeight = ((baseHeight + intercellHeight) * annots.length)
	  + (2 * intercellHeight);
	if (DEBUG > 0)
          System.err.println ("desired height is now " + desiredHeight);
	// for now, can only increase row height (because another
	// column might require more height than this one)
	if (table.getRowHeight(row) < desiredHeight) {
	  table.setRowHeight(row, desiredHeight);
	}
	// for now, can only increase row height
	//} else { // if necessary, re-set row height to default
	//if (table.getRowHeight(row) != baseHeight) {
	//  table.setRowHeight(row, baseHeight);
	//}
      }
    }
  }

    /*
     * The following methods are overridden as a performance measure to 
     * to prune code-paths are often called in the case of renders
     * but which we know are unnecessary.  Great care should be taken
     * when writing your own renderer to weigh the benefits and 
     * drawbacks of overriding methods like these.
     */

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public boolean isOpaque() { 
	Color back = getBackground();
	Component p = getParent(); 
	if (p != null) { 
	    p = p.getParent(); 
	}
	// p should now be the JTable. 
	boolean colorMatch = (back != null) && (p != null) && 
	    back.equals(p.getBackground()) && 
			p.isOpaque();
	return !colorMatch && super.isOpaque(); 
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void validate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void revalidate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void repaint(long tm, int x, int y, int width, int height) {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void repaint(Rectangle r) { }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {	
	// Strings get interned...
	if (propertyName=="text") {
	    super.firePropertyChange(propertyName, oldValue, newValue);
	}
    }
     */

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { }


  public class EmptyListSelectionModel implements ListSelectionModel {
    public void setSelectionInterval(int index0, int index1) {}
    public void addSelectionInterval(int index0, int index1) {}
    public void removeSelectionInterval(int index0, int index1) {}
    public int getMinSelectionIndex() { return -1; }
    public int getMaxSelectionIndex() { return -1; }
    public boolean isSelectedIndex(int index)  { return false; }
    public int getAnchorSelectionIndex()  { return -1; }
    public void setAnchorSelectionIndex(int index) {}
    public int getLeadSelectionIndex()  { return -1; }
    public void setLeadSelectionIndex(int index) {}
    public void clearSelection() {}
    public boolean isSelectionEmpty() { return true; }
    public void insertIndexInterval(int index, int length, boolean before) {}
    public void removeIndexInterval(int index0, int index1) {}
    public void setValueIsAdjusting(boolean valueIsAdjusting) {}
    public boolean getValueIsAdjusting()  { return false; }
    public void setSelectionMode(int selectionMode) {}
    public int getSelectionMode() { return SINGLE_SELECTION; }
    public void addListSelectionListener(ListSelectionListener x) {}
    public void removeListSelectionListener(ListSelectionListener x) {}
  }
  
}
