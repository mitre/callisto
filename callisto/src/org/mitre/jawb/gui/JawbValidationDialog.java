
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
package org.mitre.jawb.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import edu.jhu.hall.PrintUtilities;

// SAM 2/8/06: Much of this is taken from Galen's FLEX importer
// dialog, along with considerable amounts of his advice. And then
// there's some black magic that I just copied, because I don't
// really understand it...

public class JawbValidationDialog extends JDialog {
  
  private boolean saveAnyway = false;
  private boolean cancelled = false;
  
  public JawbValidationDialog(JFrame f, String validationMessage) {
    super(f, "Document Validation Failed", true);
    setSize(300, 300);
    saveAnyway = false;
    Container contentPane = getContentPane();
    this.setResizable(true);
    this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    this.setContentPane(getJContentPane());
    this.addWindowListener(new java.awt.event.WindowAdapter() { 
      public void windowClosing(java.awt.event.WindowEvent e) {    
        // If the user closes the window from the WM.
        dialogComplete(true);
      }
    });
    this.getMessagePane().setText(validationMessage);
    JRootPane rootPane = getRootPane();
    rootPane.setDefaultButton(getNoButton());
    setLocationRelativeTo(f);
    setVisible(true);
  }
  
  protected void dialogComplete(boolean cancelled) {
    setVisible(false);
    this.cancelled = cancelled;
    dispose();
  }
  
  private JPanel jContentPane;
  
  private javax.swing.JPanel getJContentPane() {
    if (jContentPane == null) {
      jContentPane = new JPanel();
      jContentPane.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
      jContentPane.setLayout(new BorderLayout());
      jContentPane.add(getValidationHeader(), BorderLayout.NORTH);
      jContentPane.add(getMainScrollPane(), BorderLayout.CENTER);
      jContentPane.add(getButtonBlock(), BorderLayout.SOUTH);
    }
    return jContentPane;
  }
  
  private JLabel validationHeader;
  
  private JLabel getValidationHeader() {
    if (validationHeader == null) {
      validationHeader = new JLabel("<html><p>Validation of the document failed.<p>Details:</html>");
      validationHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
      // validationHeader.setEditable(false);
    }
    return validationHeader;
  }
  
  private JScrollPane mainScrollPane;
  
  JScrollPane getMainScrollPane() {
    if (mainScrollPane == null) {
      mainScrollPane = new JScrollPane(getMessagePane(),
          ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
    return mainScrollPane;
  }
  
  private JTextPane messagePane;
  
  private JTextPane getMessagePane() {
    if (messagePane == null) {
      messagePane = new JTextPane();
    }
    return messagePane;
  }
  
  private JPanel buttonBlock;
  
  private JPanel getButtonBlock() {
    if (buttonBlock == null) {
      buttonBlock = new JPanel(new GridLayout(2, 1));
      JPanel printBlock = new JPanel();
      buttonBlock.add(printBlock);
      printBlock.add(getPrintButton());
      printBlock.add(new JLabel("Print this validation report"));
      JPanel buttonRow = new JPanel(new FlowLayout());
      buttonBlock.add(buttonRow);
      JPanel realButtonRow = new JPanel(new GridLayout());
      buttonRow.add(realButtonRow);
      realButtonRow.add(getYesButton());
      realButtonRow.add(getNoButton());
    }
    return buttonBlock;
  }
  
  private JButton yesButton;
  
  private JButton getYesButton() {
    if (yesButton == null) {
      yesButton = new JButton("Save anyway");
      yesButton.addActionListener(new ActionListener () {
        public void actionPerformed (ActionEvent ev) {
          saveAnyway = true;
          dialogComplete(false);
        }
      });
    }
    return yesButton;
  }
  
  private JButton noButton;
  
  private JButton getNoButton() {
    if (noButton == null) {
      noButton = new JButton("Don't save");
      noButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          saveAnyway = false;
          dialogComplete(false);
        }
      });
    }
    return noButton;
  }
  
  public boolean getSaveResult() {
    return saveAnyway;
  }
  
  private JButton printButton;
  
  private JButton getPrintButton() {
    if (printButton == null) {
      printButton = new JButton("Print");
      printButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          PrintUtilities.printComponent(getMessagePane());
        }
      });
    }
    return printButton;
  }

}
