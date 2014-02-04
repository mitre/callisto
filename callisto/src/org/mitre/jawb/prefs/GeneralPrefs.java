
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

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.swing.IntegerDocument;
import org.mitre.jawb.gui.MainTextPane;

public class GeneralPrefs extends AbstractPreferencePane {

  private JCheckBox followFile, showFullPath, storeGeometry, sortCaseSensitive;
  private JTextField historyMax;

  private JComboBox cbBackupCount;

  private JComboBox cbAutotagType;
  private JCheckBox autotagForwardOnly, autotagOnlyUntagged;

  private JColorChooser reconciliationHighlight;

  //private ButtonGroup selectGroup;
  //private JRadioButton wordSelect, charSelect;
  
  public GeneralPrefs () {
    super("general");

    addComponent (followFile = new JCheckBox
      ("Select files starting at active file"));

    addComponent (showFullPath = new JCheckBox
      ("Show full path to file in title bar"));

    addComponent (storeGeometry = new JCheckBox
      ("Save window geometry"));

    addSeparator ();
    
    addComponent ("Document History Limit", (historyMax = new JTextField (3)));
    historyMax.setDocument (new IntegerDocument ());
    
    Object[] numberList = new String[] {"0","1","2","3","4","5","6"};
    cbBackupCount = new JComboBox(numberList);
    cbBackupCount.setEditable(true);
    ComboBoxEditor cbEditor = (ComboBoxEditor)cbBackupCount.getEditor();
    JTextField cbTextField = (JTextField)cbEditor.getEditorComponent();
    cbTextField.setColumns(3);
    cbTextField.setDocument(new IntegerDocument());
    addComponent("Number of file backups to keep (per file)", cbBackupCount);

    addSeparator ();

    Object[] autotypeList = new String[] {"None", "Automatic", 
                                          "SingleQuery", "Query"};
    cbAutotagType = new JComboBox(autotypeList);
    cbAutotagType.setEditable(false); 
    addComponent("Autotag Mode Preference",cbAutotagType);
    JLabel autotagLabel = new JLabel("(choose \"None\" for Manual Only)");
    addComponent(autotagLabel);
    addComponent(autotagForwardOnly = new JCheckBox
                 ("Autotag Forward Only (do not wrap around)"));
    addComponent(autotagOnlyUntagged = new JCheckBox
                 ("Only Autotag text with no other tags"));

    addSeparator ();

    addComponent (sortCaseSensitive = new JCheckBox
      ("Sort strings in case senstitive order"));

    addComponent (new JLabel("Choose a Highlight Color for the Current Reconciliation Segment (used by Toocaan Tasks only)"));
    addComponent (reconciliationHighlight = new JColorChooser());
    
    reset ();
    
    // Here's a ButtonGroup
    /* it's in the menus
    selectGroup = new ButtonGroup ();
    JPanel selectPanel = new JPanel (new GridLayout (0,1));
    selectPanel.setBorder (BorderFactory.createTitledBorder
                          ("Default Selection Mode"));

    selectGroup.add (wordSelect = new JRadioButton (AutoSelectCaret.Mode.
                                                    WORD.toString()));
    selectPanel.add (wordSelect);

    selectGroup.add (charSelect = new JRadioButton (AutoSelectCaret.Mode.
                                                    CHARACTER.toString()));
    selectPanel.add (charSelect);

    String mode = prefs.getPreference (Preferences.SELECTION_MODE_KEY);
    if (mode != null) {
      if (mode.equals (AutoSelectCaret.Mode.CHARACTER.toString()))
        charSelect.setSelected (true);
      // mode.equals(AutoSelectCaret.Mode.WORD.toString())
      else 
        wordSelect.setSelected (true);
    }
    addComponent (selectPanel, GridBagConstraints.HORIZONTAL);
    */
  }

  public final void save () {
    Preferences prefs = Jawb.getPreferences ();
    prefs.setPreference (Preferences.FOLLOW_FILE_KEY,
                         followFile.isSelected ());
    prefs.setPreference (Preferences.STORE_WINDOW_GEOMETRY_KEY,
                         storeGeometry.isSelected ());
    prefs.setPreference (Preferences.SHOW_FULL_PATH_KEY,
                         showFullPath.isSelected ());
    prefs.setPreference (Preferences.HISTORY_MAX_KEY,
                         historyMax.getText());
    Object backupCount = cbBackupCount.getSelectedItem();
    int count = Integer.parseInt((String)backupCount);//
    prefs.setPreference (Preferences.BACKUP_COUNT_KEY, count);
    Object autotagType = cbAutotagType.getSelectedItem();
    prefs.setPreference (Preferences.AUTOTAG_MODE_KEY, (String)autotagType);
    prefs.setPreference (Preferences.AUTOTAG_FORWARD_ONLY_KEY,
                         autotagForwardOnly.isSelected());
    prefs.setPreference (Preferences.AUTOTAG_UNTAGGED_ONLY_KEY,
                         autotagOnlyUntagged.isSelected());
    prefs.setPreference (Preferences.SORT_CASE_SENSITIVE_KEY,
			 sortCaseSensitive.isSelected());
    prefs.setPreference (MainTextPane.RECONCILIATION_HIGHLIGHT_KEY,
                         reconciliationHighlight.getColor());
  }

  public final void reset () {
    Preferences prefs = Jawb.getPreferences ();
    followFile.setSelected (prefs.getBoolean (Preferences.FOLLOW_FILE_KEY));
    showFullPath.setSelected (prefs.getBoolean
                              (Preferences.SHOW_FULL_PATH_KEY));
    storeGeometry.setSelected (prefs.getBoolean
                               (Preferences.STORE_WINDOW_GEOMETRY_KEY));
    try {
      historyMax.setText (String.valueOf (prefs.getInteger
                                          (Preferences.HISTORY_MAX_KEY)));
    } catch (Exception e) { /* do nothing */ }
    
    try {
      Integer backups = new Integer(prefs.getInteger
                                    (Preferences.BACKUP_COUNT_KEY, 10));
      cbBackupCount.setSelectedItem(backups.toString());
    } catch (NumberFormatException ne) {
    } catch (NullPointerException npe) {
    }
    cbAutotagType.setSelectedItem(prefs.getPreference
                                  (Preferences.AUTOTAG_MODE_KEY));
    sortCaseSensitive.setSelected (prefs.getBoolean
                                   (Preferences.SORT_CASE_SENSITIVE_KEY));
    autotagForwardOnly.setSelected (prefs.getBoolean
                                    (Preferences.AUTOTAG_FORWARD_ONLY_KEY));
    autotagOnlyUntagged.setSelected (prefs.getBoolean
                                     (Preferences.AUTOTAG_UNTAGGED_ONLY_KEY));
    reconciliationHighlight.setColor
      (prefs.getColor(MainTextPane.RECONCILIATION_HIGHLIGHT_KEY, Color.red));
  }
  
} // GeneralPrefs
