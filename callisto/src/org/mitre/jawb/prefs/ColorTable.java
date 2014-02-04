
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

package org.mitre.jawb.prefs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.mitre.jawb.Jawb;

/**
 * Describe class <code>ColorTable</code> here.
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */
public class ColorTable extends JTable {

    private static int DEBUG = 0;
    private static final int VISIBLE_COL = 0;
    private static final int KEY_COL = 1;
    private static final int HIGHLIGHT_COL = 2;

    private static final String LABEL = ".label";
    private static final String VISIBLE = ".visible";
  
    /**
     * Construct a <code>ColorTable</code> with an empty, extendable model,
     * which is backed by, and saved to, the specified Preferences.
     */
    public ColorTable() {
	this (new ColorTableModel (true));
    }
  
    /**
     * Construct a <code>ColorTable</code> with the specified model, which is
     * backed by, and saved to, the specified Preferences.
     * @param model the <code>ColorTableModel</code> to use.
     */
    public ColorTable (ColorTableModel model) {
	super (model);
      
	// set width of column 2 so "highlight" col isn't too wide
	TableColumn column = getColumnModel ().getColumn (HIGHLIGHT_COL);
	Component comp =
	    getDefaultRenderer(model.getColumnClass(HIGHLIGHT_COL)).
	    getTableCellRendererComponent(this, "Highlight",
					  false, false, 0, HIGHLIGHT_COL);
	column.setPreferredWidth(comp.getPreferredSize().width);
      
	column = getColumnModel ().getColumn (HIGHLIGHT_COL);
	column.setPreferredWidth(-1);
      
	getTableHeader ().setReorderingAllowed(false);
	//getSelectionModel ().addListSelectionListener (new ListHandler ());
      
	// set appropriate color editor
	ColorRenderEditor cre = new ColorRenderEditor (this);
	column.setCellEditor (cre);
	column.setCellRenderer (cre);
    }

     /**
     * {@link TableModel} for the <code>ColorTable</code>.
     */
    public static class ColorTableModel extends AbstractTableModel {
	
	private static final String[] columnNames
	    = { "Visible", "Highlight ID", "Highlight Color" };
	// use a tool tip or something here
	
	private boolean allowAdd = false;
	private ArrayList colorChoices;
    
	/**
	 * Construct an empty <code>ColorTableModel</code>.
	 */
	public ColorTableModel (boolean allowAdd) {
	    this.allowAdd = allowAdd;
	    colorChoices = new ArrayList(24);
            initPrefsListener();
	}
	
	/**
	 * Construct a <code>ColorTableModel</code> and initialize it using the
	 * keys in the specified {@link List} to retrieve values from the specified
	 * {@link Preferences}.
	 * @param allowAdd Are users allowed to add values.
	 * @param choices List containing the initial choices for this
	 * <code>ColorTableMode</code>. This List must contain {@link String}
	 * Objects. Each map entry will be added to the Model as if by
	 * {@link #addColorChoice}
	 * @exception ClastCastException if the choices contains other than
	 * Strings.
	 * @see #addColorChoice
	 */
	public ColorTableModel (boolean allowAdd, List choices) {
          this (allowAdd);
          
          Preferences prefs = Jawb.getPreferences ();
          Iterator iter = choices.iterator ();
          while (iter.hasNext ()) {
            String property = (String) iter.next ();
            ColorSpec color = prefs.getColorSpec(property);
            boolean visible = prefs.getBoolean(property + VISIBLE, true);
            String label = prefs.getPreference (property + LABEL, property);
            colorChoices.add(new ColorChoice(label, property, color, visible));
          }
	}




      // RK 2/17/09 listen for preference changes so we can reset 
      // the ColorTableModel if a relevant preference is changed
      private void initPrefsListener() {
        Preferences prefs = Jawb.getPreferences ();
        prefs.addPropertyChangeListener(new MyPrefsChangeListener(this));
      }

      public void setAllVisible(boolean visible) {
        int rows = colorChoices.size();
        for (int i=0; i<rows; i++) {
          setValueAt(Boolean.valueOf(visible), i, VISIBLE_COL);
        }
      }

        public boolean areAdditionsAllowed() {
          return allowAdd;
        }
    
	/**
	 * @see TableModel#getColumnCount()
	 */     
	public int getColumnCount () {
	    return columnNames.length;
	}
    
	/**
	 * @see TableModel#getRowCount()
	 */
	public int getRowCount () {
	    return colorChoices.size() + (allowAdd ? 1 : 0);
	}
    
	/**
	 * @see TableModel#getColumnName(int)
	 */
	public String getColumnName (int index) {
	    return columnNames[index];
	}
    
	/**
	 * Overridden to return Boolean for 'visible' column
	 */
	public Class getColumnClass (int index) {
            if (index == VISIBLE_COL)
                return Boolean.class;
	    return super.getColumnClass(index);
	}
    
	/**
	 * @see TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable (int row, int col) {
	    // see setValueAt
	    if (allowAdd)
		return ((col != KEY_COL) ^ (row == colorChoices.size ()) );
	    else
		return col != KEY_COL;
	}
    
	/**
	 * @see TableModel#getValueAt(int, int)
	 */
	public Object getValueAt (int row, int col) {
	    if (row == colorChoices.size ())
		return null;

	    ColorChoice ch = (ColorChoice) colorChoices.get (row);
	    switch (col) {
              case VISIBLE_COL:
                return Boolean.valueOf(ch.visible);
              case KEY_COL:
                return ch.label;
              case HIGHLIGHT_COL:
		return ch.color;
              default:
		return null;
	    }
	}
    
	/**
	 * @see TableModel#setValueAt(Object, int, int)
	 */
	public void setValueAt (Object value, int row, int col) {
            if (DEBUG > 0)
                System.err.println ("setValue="+value+" row="+row+" col="+col);
	    //assert (! (row==1 && col==colorChoices.size())) : "isCellEditable failed";
	    if (row == colorChoices.size ()) {
		//for now null.. maybe this should be a nice name too?
		addColorChoice (null, (String) value);
		return;
	    }

	    ColorChoice ch = (ColorChoice) colorChoices.get(row);
	    switch (col) {
              case VISIBLE_COL:
                ch.visible = ((Boolean)value).booleanValue();
                break;
              case HIGHLIGHT_COL:
		ch.color = (ColorSpec) value;
                break;
              default:
		return;
	    }
	    fireTableCellUpdated(row, col);
	}
    
	/**
	 * Save the contents of this <code>ColorTableModel</code>
	 */
	public void save() {
          Preferences prefs = Jawb.getPreferences();
          for (int i = 0; i < colorChoices.size(); i++) {
            ColorChoice ch = (ColorChoice) colorChoices.get(i);
            if (DEBUG > 0)
              System.err.println ("CT.save: property="+ch.property);

            String oldColor = prefs.getPreference(ch.property, "");
            if (! oldColor.equals(ch.color.toString()))
              prefs.setPreference(ch.property, ch.color);
            
            boolean oldVisible = prefs.getBoolean(ch.property + VISIBLE, true);
            if (oldVisible != ch.visible)
              prefs.setPreference(ch.property + VISIBLE, ch.visible);
          }
	}
    
	/**
	 * Reset the contents of this <code>ColorTableModel</code>.
	 */
	public void reset () {
	    Preferences prefs = Jawb.getPreferences (); //get the prefs
	    for (int i = 0; i < colorChoices.size(); i++) {
		ColorChoice ch = (ColorChoice) colorChoices.get(i);
                ch.color = prefs.getColorSpec(ch.property);
                ch.visible = prefs.getBoolean(ch.property + VISIBLE, true);
	    }
	}
    
	/**
	 * Add the specified color choice to this <code>ColorTableModel</code>.
	 * @param label    <code>String</code> user friendly property name
	 *                  describing the Annotation
	 * @param property <code>String</code> 
	 */
	public void addColorChoice (String label, String property) {
          if (DEBUG > 1) {
            System.out.println("Adding color choice - Label: " 
                               + label + " Prop: " + property);
          }
          Preferences prefs = Jawb.getPreferences (); //get the prefs
          
          //if we didn't get passed in a label, grab from the properties
          if (label == null)
            label = prefs.getPreference(property + LABEL, property);
          boolean visible = prefs.getBoolean(property + VISIBLE, true);
          ColorSpec color = prefs.getColorSpec(property);
          
          colorChoices.add(new ColorChoice(label, property, color, visible)); 
	}

      public class MyPrefsChangeListener implements PropertyChangeListener{
        
        ColorTableModel model;
        
        public MyPrefsChangeListener(ColorTableModel model) {
          super();
          this.model = model;
        }

        public void propertyChange (PropertyChangeEvent evt) {
          String property = evt.getPropertyName();
          if (property.endsWith(VISIBLE) ||
              property.endsWith(LABEL)) {
            model.reset();
          }
        }
      }


    }//ColorTableModel
  
    /***********************************************************************/
    /* Inner Utility Classes */
    /***********************************************************************/
  
    /**
     * Structure (ok, ok, class) to hold data while it's being edited
     */
    public static class ColorChoice {
	String label;
	String property;
	ColorSpec color;
        boolean visible;
      
	ColorChoice (String label, String property, ColorSpec color) {
            this(label, property, color, true);
	}
	ColorChoice (String label, String property,
                     ColorSpec color, boolean visible) {
          this.label = label;
          this.property = property;
          this.color = color;
          this.visible = visible;
	}
    }
  
    /**
     * The editor/renderer button that brings up the dialog.  We extend
     * DefaultCellEditor for convenience, even though it means we have to
     * create a dummy check box.  Another approach would be to copy the
     * implementation of TableCellEditor methods from the source code for
     * DefaultCellEditor.
     */
    public class ColorRenderEditor extends DefaultCellEditor
	implements TableCellRenderer {
    
    final Component comp;
	ColorSpec currentColor = null;
	Component renderComponent = null;
	ColorSpecDialog colorChooser = null;
	ActionListener ok_showListener = null;
	int currentRow;
    
	public ColorRenderEditor (Component component) {
          super(new JCheckBox ());
          comp = component;
          // Unfortunately, the constructor expects a check box, 
          //combo box, or text field.
          setClickCountToStart (1); //This is usually 1 or 2.

          renderComponent = new JButton ("Sample Text");
          renderComponent.setFont (UIManager.getFont("TextField.font"));

          editorComponent = new JButton ("Sample Text");
          editorComponent.setFont (UIManager.getFont("TextField.font"));
      
          ok_showListener = new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                
                //Here's the code that brings up the dialog.
                if (e.getSource () == editorComponent) {
                  if (colorChooser==null) {
                    colorChooser = ColorSpecDialog.createDialog (comp);
                  }
                  colorChooser.setColorSpec (currentColor);
                  colorChooser.setLabel ( (String)getValueAt (currentRow, KEY_COL));
                  colorChooser.show ();
                  currentColor = colorChooser.getColorSpec();
                  fireEditingStopped ();
                }
              }};
          colorChooser = ColorSpecDialog.createDialog (comp);
          ((JButton)editorComponent).addActionListener (ok_showListener);
	}
    
	public Object getCellEditorValue () {
	    return currentColor;
	}
    
	public Component getTableCellRendererComponent (JTable table,
							Object colorValue,
							boolean isSelected,
							boolean hasFocus,
							int row, int column) {
	    renderComponent.setBackground (((ColorSpec)colorValue).getBackground());
	    renderComponent.setForeground (((ColorSpec)colorValue).getForeground());
	    return renderComponent;
	}
	public Component getTableCellEditorComponent (JTable table, 
						      Object colorValue,
						      boolean isSelected,
						      int row, int column) {
	    currentColor = (ColorSpec) colorValue;
	    currentRow = row;
	    editorComponent.setBackground (currentColor.getBackground());
	    editorComponent.setForeground (currentColor.getForeground());
	    return editorComponent;
	}
    }


}// ColorTable

