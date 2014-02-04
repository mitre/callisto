
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

import javax.swing.*;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.swing.IntegerDocument;

public class FontPrefs extends AbstractPreferencePane {

  private JTextArea explanation;
  private JCheckBox enableLastSize, enableLastFamily, tablesFollow;
  private JCheckBox enableLastLineSpacing;
  private LinkedHashMap fontBoxMap = new LinkedHashMap();
  
  private JComboBox cbBackupCount;
  
  public FontPrefs () {
    super("fonts");

    addComponent (enableLastSize = new JCheckBox
      ("Remember last font size"));

    addComponent (enableLastFamily = new JCheckBox
      ("Remember last font used"));

    addComponent (enableLastLineSpacing = new JCheckBox
      ("Remember line spacing"));

    addComponent (tablesFollow = new JCheckBox
      ("Tables use same font size as Main Text Panel"));

    addSeparator ();

    explanation = new JTextArea ();
    explanation.setText ("To open documents faster, Font Auto-Detection does not test all available fonts.  Instead it only tries those selected below.  To auto-detect against all available fonts, use the \"Auto-Detect Font\" item in the Format->Font menu.");
    explanation.setLineWrap (true);
    explanation.setWrapStyleWord (true);
    explanation.setEditable (false);
    explanation.setBackground(getBackground());
    addComponent (explanation, GridBagConstraints.HORIZONTAL);

    GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
    String[] families = g.getAvailableFontFamilyNames ();
    for (int i=0; i<families.length; i++) {
      JCheckBox box = new JCheckBox (families[i]);
      fontBoxMap.put (families[i], box);
      addComponent (box);
    }
    
    reset ();
  }

  public final void save () {
    Preferences prefs = Jawb.getPreferences ();
    prefs.setPreference (Preferences.FONTS_LAST_SIZE_ENABLED_KEY,
                         enableLastSize.isSelected ());
    prefs.setPreference (Preferences.FONTS_LAST_FAMILY_ENABLED_KEY,
                         enableLastFamily.isSelected ());
    prefs.setPreference (Preferences.PARA_LAST_LINE_SPACING_KEY,
                         enableLastLineSpacing.isSelected ());
    prefs.setPreference (Preferences.FONTS_TABLE_FOLLOW_SIZE_KEY,
                         tablesFollow.isSelected ());
    
    StringBuffer sb = new StringBuffer("");
    for (Iterator i=fontBoxMap.values().iterator(); i.hasNext(); ) {
      JCheckBox box =(JCheckBox) i.next();
      if (box.isSelected())
        sb.append(box.getText()).append(",");
    }
    prefs.setPreference (Preferences.FONTS_AUTO_DETECTED_KEY, sb.toString());
  }

  public final void reset () {
    Preferences prefs = Jawb.getPreferences ();
    enableLastSize.setSelected (prefs.getBoolean
                                (Preferences.FONTS_LAST_SIZE_ENABLED_KEY));
    enableLastFamily.setSelected (prefs.getBoolean
                                  (Preferences.FONTS_LAST_FAMILY_ENABLED_KEY));
    enableLastLineSpacing.setSelected
      (prefs.getBoolean (Preferences.PARA_LAST_LINE_SPACING_ENABLED_KEY));
    tablesFollow.setSelected (prefs.getBoolean
                              (Preferences.FONTS_TABLE_FOLLOW_SIZE_KEY));

    HashSet unselectedNames = new HashSet (fontBoxMap.keySet());
    String autoDetected =
      prefs.getPreference (Preferences.FONTS_AUTO_DETECTED_KEY, "Default");

    StringTokenizer st = new StringTokenizer (autoDetected, ",");
    while (st.hasMoreTokens()) {
      String family = st.nextToken();
      if (fontBoxMap.containsKey(family)) {
        ((JCheckBox) fontBoxMap.get (family)).setSelected (true);
        unselectedNames.remove (family);
      }
    }
    // make sure only specified families are turned on
    Iterator iter = unselectedNames.iterator();
    for (Iterator i=unselectedNames.iterator(); i.hasNext(); )
      ((JCheckBox) fontBoxMap.get (i.next())).setSelected (false);
  }
  
} // FontPrefs
