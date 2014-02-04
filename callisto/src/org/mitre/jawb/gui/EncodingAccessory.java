
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

import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.nio.charset.Charset;

import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JPanel;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.prefs.Preferences;

/**
 * Sub widget for other accessories.
 */
public class EncodingAccessory extends JPanel {

  private static final String separator = "-------";
  private JComboBox encodingBox; // combo box for allowed encodings
  private PropertyChangeListener pcListener;
  
  /**
   * Creates the components for a drop menu of available encodings, and
   * 'default" buttons.
   */
  public EncodingAccessory() {
    super (new BorderLayout (5,5));

    Preferences prefs = Jawb.getPreferences();
    String encoding = prefs.getPreference(Preferences.TEXT_ENCODING_KEY);
    try { // validate
      Charset.forName (encoding);
    } catch (Exception illegalOrUnsupported) {
      encoding = "UTF-8";
    }
    
    String defaultCharset =
      Charset.forName (System.getProperty ("file.encoding")).toString();

    pcListener = new PreferenceChangeListener();
    prefs.addPropertyChangeListener(pcListener);
    // TODO: will we ever need to remove it?

    // add combobox containing known encodings.
    encodingBox = new JComboBox ();
    encodingBox.addItem ("UTF-8");
    encodingBox.addItem (defaultCharset);
    encodingBox.addItem (separator);

    // availableCharsets returns SortedMap, so keys are sorted too
    Iterator charsetIter = Charset.availableCharsets().keySet().iterator();
    while (charsetIter.hasNext ())
      encodingBox.addItem (charsetIter.next ());
    
    encodingBox.addItemListener (new ItemListener () {
        public void itemStateChanged (ItemEvent e) {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (e.getItem ().equals (separator))
              encodingBox.setSelectedItem (GUIUtils.getSelectedEncoding());
            else {
              Preferences prefs = Jawb.getPreferences();
              String encoding = (String) e.getItem();
              prefs.removePropertyChangeListener(pcListener);
              GUIUtils.setSelectedEncoding (encoding);
              prefs.setPreference(Preferences.TEXT_ENCODING_KEY, encoding);
              Jawb.storePreferences();
              prefs.addPropertyChangeListener(pcListener);
            }
          }
        }});
    encodingBox.setSelectedItem (encoding);

    JLabel label = new JLabel ("File Encoding", JLabel.LEFT);
    
    this.add (encodingBox, BorderLayout.WEST);
    this.add (label, BorderLayout.CENTER);

    // initialize at startup
    GUIUtils.setSelectedEncoding (encoding);
  }

  /**
   * Returns the selected task
   */
  public String getEncoding () {
    return (String) encodingBox.getSelectedItem ();
  }
  
  /**
   * Returns the selected task
   */
  public boolean setEncoding (String encoding) {
    if (! Charset.isSupported (encoding))
      return false;
    encodingBox.setSelectedItem (encoding);
    return true;
  }

  /**
   * Listener for updateing default mime type when selected document changes
   */
  private class PreferenceChangeListener implements PropertyChangeListener {
    public void propertyChange (PropertyChangeEvent e) {
      String prop = e.getPropertyName();
      Object value = e.getNewValue();
      if (Preferences.TEXT_ENCODING_KEY.equals(prop) && value != null)
        encodingBox.setSelectedItem(e.getNewValue());
    }
  }
}

