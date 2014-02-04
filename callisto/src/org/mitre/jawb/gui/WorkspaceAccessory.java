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

import java.awt.event.*;
import java.awt.BorderLayout;

import java.lang.String;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.BoxLayout;

import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.Jawb;


// testing
import javax.swing.JFrame;
import java.util.LinkedList;

/**
 * For showing a panel on the side of our filechooser that
 * displays information about the tasks and their Workspaceers
 */
public class WorkspaceAccessory extends JPanel implements ActionListener {

  private JLabel urlLabel; 
  private JTextField urlField;      // input box for Workspace URL
  private JLabel nameLabel;         
  private JTextField nameField;     // type-in for the name of the workspace
  private JLabel keyLabel; 
  private JTextField keyField;      // input box for Workspace Key
  private JLabel userLabel;
  private JTextField userField;     // type-in for name of user
  // private JCheckBox activeCheck;    // checkbox for active learning
  private JCheckBox openNextCheck;  // checkbox for whether user wants
                                    // to immediately open the nextdoc
  private JRadioButton coreFolder;  // radio button for core folder
  private JRadioButton reconFolder; // radio button for reconciliation folder

  private JPanel workspacePanel;

  private String defaultURL = "http://localhost:7801";
  private String defaultWSName = "workspace";

    /**
     * Constructor. Creates a new Workspace Accessory, which will
     * contain :
     * an input box for Workspace URL
     * an input box for Workspace Key
     */
    public WorkspaceAccessory() {

      // setLayout(new BorderLayout(5,5));

      Preferences prefs = Jawb.getPreferences();

      workspacePanel = new JPanel();
      workspacePanel.setLayout(new BoxLayout(workspacePanel, BoxLayout.Y_AXIS));

        urlLabel = new JLabel("Enter Workspace URL");
        String prefURL = 
          prefs.getPreference(Preferences.LAST_WS_URL_KEY, defaultURL);
        urlField = new JTextField(prefURL, 35);

        nameLabel = new JLabel("Enter Workspace Name");
        nameField = 
          new JTextField(prefs.getPreference(Preferences.LAST_WS_NAME_KEY,
                                             defaultWSName));

        keyLabel = new JLabel("Enter Workspace Key");
        String prefKey = prefs.getPreference(Preferences.LAST_WS_KEY_KEY,"");
        keyField = new JTextField(prefKey,35);

        userLabel = new JLabel("Enter Userid");
        String prefUser = prefs.getPreference(Preferences.LAST_WS_USER_KEY,"");
        userField = new JTextField(prefUser,35);

        // activeCheck = new JCheckBox("Active Learning", true);
        openNextCheck = 
          new JCheckBox("Open Next Document Immediately from:", 
                        prefs.getBoolean(Preferences.LAST_WS_OPEN_NEXT_KEY,
                                         true));
        openNextCheck.addActionListener(this);
        
        coreFolder = new JRadioButton("Hand Annotation Queue");
        reconFolder = new JRadioButton("Reconciliation Queue");
        if (prefs.getPreference(Preferences.LAST_WS_FOLDER_KEY, "core").
            equals("reconciliation")) {
          reconFolder.setSelected(true);
        } else {
          coreFolder.setSelected(true);
        }
        ButtonGroup folderGroup = new ButtonGroup();
        folderGroup.add(coreFolder);
        folderGroup.add(reconFolder);

        workspacePanel.add(urlLabel);
        workspacePanel.add(urlField);
        workspacePanel.add(nameLabel);
        workspacePanel.add(nameField);
        workspacePanel.add(keyLabel);
        workspacePanel.add(keyField);
        workspacePanel.add(userLabel);
        workspacePanel.add(userField);
        // workspacePanel.add(activeCheck);
        workspacePanel.add(openNextCheck);
        workspacePanel.add(coreFolder);
        workspacePanel.add(reconFolder);

        workspacePanel.setBorder (BorderFactory.createCompoundBorder
                                  (BorderFactory.createTitledBorder("Workspace Details"),
                                   BorderFactory.createEmptyBorder (5,5,5,5)));

        this.add (workspacePanel);

    }


  /***********************************************************************/
  /* External access */
  /***********************************************************************/
  
  /**
   * Returns the specified workspace key if entered
   * @return Workspace key last entered when accessory was shown
   * or null if it has never been shown
   */
  public String getWorkspaceKey() {
    if (keyField == null)
      return null;
    return  keyField.getText();
  }
  
  /**
   * Returns the specified userid if entered
   * @return userid last entered when accessory was shown
   * or null if it has never been shown
   */
  public String getWorkspaceUser() {
    if (userField == null)
      return null;
    return  userField.getText();
  }
  
  /**
   * Returns the specified workspace name 
   * @return Workspace name last entered when accessory was shown
   * or null if it has never been shown 
   */
  public String getWorkspaceNameFromTypeIn() {
    return  nameField.getText();
  }


  /**
   * Returns the specified workspace URL if entered
   * @return Workspace URL last entered when accessory was shown
   * or null if it has never been shown
   */
  public String getWorkspaceURL() {
    if (urlField == null)
      return null;
    return  urlField.getText();
  }
  /**
   * Returns the value of the boolean Active Learning checkbox
   * @return Active Learning checkbox state when accessory was shown
   * or false if it has never been shown (cannot return null for a boolean)
   */
  /**********
  public boolean getWorkspaceActive() {
    if (activeCheck == null)
      return false;
    return  activeCheck.isSelected();
  }
  ***********/
  /**
   * Returns the "core" or "reconciliation" according to which radio button
   * is selected. 
   * @return the String "core" or "reconciliation" according to which
   * radio button is selected, or null if it has never been shown
   */ public String getWorkspaceFolder() {
    if (coreFolder == null || reconFolder == null)
      return null;
    if (coreFolder.isSelected()) {
      return "core";
    } else if (reconFolder.isSelected()) {
      return "reconciliation";
    } else {
      // should never happen
      return null;
    }
  }
  /**
   * Returns the value of the boolean Open Next Document checkbox
   * @return Open Next Document Learning checkbox state when accessory
   * was shown or false if it has never been shown (cannot return null
   * for a boolean)
   */
  public boolean getOpenNextdoc() {
    if (openNextCheck == null)
      return false;
    return  openNextCheck.isSelected();
  }

  /************ Action Listener **************************************/
  public void actionPerformed (ActionEvent e) {
    if (openNextCheck.isSelected()) {
      coreFolder.setEnabled(true);
      reconFolder.setEnabled(true);
    } else {
      coreFolder.setEnabled(false);
      reconFolder.setEnabled(false);
    }

  }
}