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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.UnmodifiableAttributeException;


/**
 * @version 1.0
 */
public abstract class SpatialMLEditor extends JPanel {

	private static Logger log = Logger.getLogger(SpatialMLEditor.class.getName());
	
    private static Icon leftArrow = new ImageIcon("/resource/image/leftArrow.png");
    
	protected GridBagConstraints c;

	protected AWBAnnotation annot;
	
    protected List historyListeners;
    
	public SpatialMLEditor(AWBAnnotation annot) {
		super(new GridBagLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));
		
		c = new GridBagConstraints();
		// space things out by default
		//c.ipadx = 2;
		//c.ipady = 4;
		c.insets = new Insets(2, 2, 4, 2);
		
		this.annot = annot;
		
        this.historyListeners = new Vector();
        
	}
    
	/**
	 * Add a new labelled row by creating a text field with the given value. 
     * @param label
     * @param value
     * @return the created text field object
	 */
    protected JTextField addTextFieldRow(String label, String value) {
      JTextField field = new JTextField(value);
      addLabelledRow(label, field);
      return field;
    }
    
    /**
     * Add a new labelled row by creating a text field with the given value and
     * a button for automatically selecting the last-entered value.
     * 
     * @param label
     * @param value
     * @param historical
     * @return the created text field object
     */
    protected JTextField addTextFieldRow(String label, String value, final String historical) {
      final JTextField field = new JTextField(value);
      //JButton history = new JButton(leftArrow);
      JButton history = new JButton("<<");
      history.setToolTipText(historical);
      ActionListener l = new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                field.setText(historical);
              }
            };
      history.addActionListener(l);
      historyListeners.add(l);
      
      addLabelledRow(label, field, history);
      return field;
    }
    
    /**
     * Add a new labelled row by creating a combo box with the given array of values.
     * @param label
     * @param values
     * @return the created combo box object
     */
    protected JComboBox addComboBoxRow(String label, String[] values) {
      JComboBox comboBox = new JComboBox(values);
      addLabelledRow(label, comboBox);
      return comboBox;
    }
    
    /**
     * Add a new labelled row by creating a combo box with the given Vector of values.
     * @param label
     * @param values
     * @return the just-created combo box object
     */
    protected JComboBox addComboBoxRow(String label, Vector values) {
      JComboBox comboBox = new JComboBox(values);
      addLabelledRow(label, comboBox);
      return comboBox;
    }
    
    protected JComboBox addComboBoxRow(String label, String[] values, final String historical) {
      final JComboBox comboBox = new JComboBox(values);
      //JButton history = new JButton(leftArrow);
      JButton history = new JButton("<<");
      history.setToolTipText(historical);
      ActionListener l = new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                comboBox.setSelectedItem(historical);
              }
            };
      history.addActionListener(l);
      historyListeners.add(l);
      
      addLabelledRow(label, comboBox, history);
      return comboBox;
    }
    
    protected JComboBox addComboBoxRow(String label, Vector values, final String historical) {
      final JComboBox comboBox = new JComboBox(values);
      //JButton history = new JButton(leftArrow);
      JButton history = new JButton("<<");
      history.setToolTipText(historical);
      ActionListener l = new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                comboBox.setSelectedItem(historical);
              }
            };
      history.addActionListener(l);
      historyListeners.add(l);
      
      addLabelledRow(label, comboBox, history);
      return comboBox;
    }
    
	/**
	 * Add a new row to the panel with the given label and component
	 * @param label
	 * @param component
	 */
	protected void addLabelledRow(String label, JComponent component) {
		c.gridy++; // next row
		c.gridheight = 1;
		c.gridwidth = 1;
		
		c.anchor = c.NORTHEAST; // right-justify the label
		c.gridx = 0;
		c.fill = c.NONE;
		add(new JLabel(label), c);
		

		c.gridx++;
		c.anchor = c.NORTHWEST; // left-justify the component
		c.fill = c.HORIZONTAL;
		add(component, c);
		
		//log.info(Integer.toString(c.gridy));
	}
    
    /**
     * Add a labelled row (like above) but with two components. The middle
     * component is set to stretch out. This is mostly used for the
     * history-button option.
     * 
     * @param label
     * @param first
     * @param second
     */
    protected void addLabelledRow(String label, JComponent first, JComponent second) {
      addLabelledRow(label, first);
      
      c.gridx++;
      c.anchor = c.NORTHWEST;
      c.fill = c.NONE;
      add(second, c);
      
    }
	
    protected void addTotalHistoryRow(String label) {
      
      JButton history = new JButton("<<");
      // fire off all registered history items
      history.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Iterator it = historyListeners.iterator();
          while (it.hasNext()) {
            ActionListener l = (ActionListener) it.next();
            l.actionPerformed(e);
          }
        }
      });
      
      JLabel ll = new JLabel(label);
      ll.setHorizontalAlignment(SwingConstants.RIGHT);
      
      addLabelledRow("", ll, history);
      
    }
    
	protected void addSeparator() {
		c.gridy++; // next row
		c.gridx = 0;
		c.gridwidth = 99;
		c.fill = c.HORIZONTAL;
		c.anchor = c.CENTER;
		add(new JSeparator(), c);
	}
	
	/**
	 * Reset the form to an initial state.
	 *
	 */
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Clear the form to an empty state.
	 *
	 */
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Fill in the annotation according to the form and return it.
	 * @return the annotation
	 */
	public AWBAnnotation getAnnotation() {
		updateAnnotation();
		return annot;
	}

	/**
	 * Update the annotation according to the values in the form
	 */
	public void updateAnnotation() { }

	/**
	 * Set the annotation and reset the form.
	 * @param annot the new annotation to set
	 */
	public void setAnnotation(AWBAnnotation annot) {
		this.annot = annot;
		reset();
	}

  protected void setTextFromAttribute(String attrib, JTextField field) {
    Object attr = annot.getAttributeValue(attrib);
    if (attr != null) {
      field.setText(attr.toString());
    } else {
      field.setText("");
    }
  }

  protected void setSelectionFromAttribute(String attrib, JComboBox comboBox) {
    Object attr = annot.getAttributeValue(attrib);
    if (attr != null) {
      comboBox.setSelectedItem(attr);
    } else {
      // redundant, i know, but it makes it clearer to read
      comboBox.setSelectedItem(null);
    }
  }

  /**
   * @throws UnmodifiableAttributeException
   */
  protected void setAttributeFromText(JTextField field, String attrib) throws UnmodifiableAttributeException {
    if (!field.getText().equals("")) {
      annot.setAttributeValue(attrib, field.getText());
    } else {
      annot.setAttributeValue(attrib, null);
    }
  }

  protected void setAttributeFromSelection(JComboBox comboBox, String attrib) throws UnmodifiableAttributeException {
    if (comboBox.getSelectedItem() != null && !comboBox.getSelectedItem().equals("")) {
      annot.setAttributeValue(attrib, comboBox.getSelectedItem());
    } else {
      annot.setAttributeValue(attrib, null);
    }
  }

	
	
}

