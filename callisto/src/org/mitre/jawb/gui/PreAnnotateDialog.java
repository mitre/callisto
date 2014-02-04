
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

package org.mitre.jawb.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.mitre.jawb.*;

public class PreAnnotateDialog {
  private JDialog dialog;
  private JPanel main;
  private JComboBox lpChooser;
  private JButton annotateButton;
  private JButton urlButton;
  private JPanel urlPanel;

  private boolean happy;
  
  public PreAnnotateDialog() {
    main = new JPanel(new BorderLayout(5, 5));
    main.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

    /* urlField only shown if you call showURLField() 
       and that's deprecated at this point -wm
    */
    JTextField urlField = new JTextField("http://kali.mitre.org:8080/LPMServlet/lpm/");
    urlField.getDocument().addDocumentListener(new DocumentListener() {
	private void disableStuff() {
	  lpChooser.setEnabled(false);
	  annotateButton.setEnabled(false);
	}
	public void changedUpdate(DocumentEvent e) { disableStuff(); }
	public void insertUpdate(DocumentEvent e) { disableStuff(); }
	public void removeUpdate(DocumentEvent e) { disableStuff(); }
      });
    JButton urlButton = new JButton("Load");

    urlPanel = new JPanel(new BorderLayout(5, 5));
    urlPanel.add(BorderLayout.WEST, new JLabel("Server URL:"));
    urlPanel.add(BorderLayout.CENTER, urlField);
    urlPanel.add(BorderLayout.EAST, urlButton);

    JPanel lpPanel = new JPanel(new BorderLayout(5, 5));
    lpChooser = new JComboBox();
    lpChooser.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  annotateButton.setEnabled(true);
	}
      });
    
    lpPanel.add(BorderLayout.WEST, new JLabel("TALLAL System:"));
    lpPanel.add(BorderLayout.CENTER, lpChooser);
    main.add(BorderLayout.CENTER, lpPanel);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    annotateButton = new JButton(new AbstractAction("Go!") {
	public void actionPerformed(ActionEvent e) {
	  happy = true;
	  dialog.hide();
	}
      });
    JButton cancelButton = new JButton(new AbstractAction("Cancel") {
	public void actionPerformed(ActionEvent e) {
	  happy = false;
	  dialog.hide();
	}
      });

    annotateButton.setEnabled(false);
    
    buttonPanel.add(cancelButton);
    buttonPanel.add(annotateButton);

    main.add(BorderLayout.SOUTH, buttonPanel);
  }

  public void setAskForURL(boolean b) {
    if(b) {
      main.add(BorderLayout.NORTH, urlPanel);
      lpChooser.setEnabled(false);
    }

    else {
      main.remove(urlPanel);
      lpChooser.setEnabled(true);
    }
  }

  public void addURLButtonActionListener(ActionListener e) {
    urlButton.addActionListener(e);
  }

  public void addTallalSystem(String name) {
    lpChooser.addItem(name);
  }

  public void clearTallalSystems() {
    lpChooser.removeAllItems();
  }

  /* returns the selected LP or null if canceled */
  public String show(Component parent) {
    if(dialog == null) {
      Window window = MainTextPalette.getWindowForComponent(parent);
      dialog = new JDialog((JFrame)window, "Choose Pre-Annotator", true);
      dialog.getContentPane().add(main);
      
      dialog.pack();
      dialog.setSize(new Dimension(550, dialog.getSize().height));
      //dialog.validate ();
    }
    
    dialog.show();

    System.out.println("happy? " + happy + ", sel item " + ((String)lpChooser.getSelectedItem()));
    
    if(happy) return (String)lpChooser.getSelectedItem();
    else return null;
  }
}
