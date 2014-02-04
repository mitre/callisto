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

/**
 * 
 */
package org.mitre.spatialml.callisto;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.atlas.TextExtentRegion;
import org.mitre.jawb.atlas.UnmodifiableAttributeException;
import org.mitre.jawb.prefs.Preferences;

/**
 * @author jricher
 *
 */
public class PlaceEditor extends SpatialMLEditor {

  private static Logger log = Logger.getLogger(SpatialMLEditor.class.getName());

  private JComboBox typeSelect, modSelect, continentSelect, countrySelect, formSelect, ctvSelect;

  private JTextField gazRef, comment, county, state, latLon, description;

  private JCheckBox nonLocUse, predicative;

  public PlaceEditor(TextExtentRegion annot, TextExtentRegion history) {
    //super(BoxLayout.Y_AXIS);
    super(annot);

    final String queryString = annot.getTextExtent();
    //JLabel text = new JLabel(queryString);
    
    JTextField text = new JTextField(queryString);
    text.setColumns(Math.min(queryString.length(), 25));
    text.setCaretPosition(0);
    text.setFocusable(false);
    text.setEditable(false);
    
    text.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(4, 4, 4, 4)));
    
    Box textLookup = new Box(BoxLayout.X_AXIS);
    textLookup.add(Box.createHorizontalStrut(4));
    textLookup.add(text);
    textLookup.add(Box.createHorizontalStrut(4));
    
    /*
    JButton lookupper = new JButton("Search IGDB...");
    lookupper.setMnemonic('I');
    lookupper.setToolTipText("Look up this phrase on IGDB. Will open in your system browser.");
    lookupper.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        BareBonesBrowserLaunch.openURL("http://spacetime.mitre.org/IGDB/cgi-bin/GazLookup/glook2.py?featuretype=0&place=" + queryString);
      }
    });
    
    JPopupMenu popup = new JPopupMenu("Look up in");
    
    lookupper.add(popup);
    */
    
    final GazetterLookup lookup = new IGDBLookup();
    final JButton lookupper = new JButton("Search " + lookup.getName());
    
    lookupper.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        
        final String txt = lookupper.getText();
        lookupper.setEnabled(false);
        lookupper.setText("Searching...");
        // hop off the swing thread for this one
        new Thread() {
          public void run() {
            try {
              List places = lookup.lookup(queryString);
              System.err.println(places);
              
              if (places.size() > 0) {
              
                final PlaceSelectTable table = new PlaceSelectTable(places);
                PlaceSelectDialog placesDialog = new PlaceSelectDialog(PlaceEditor.this, "Select a place", false, table,
                    new ActionListener() {
                      public void actionPerformed(ActionEvent e) {
                        Map place = table.getSelected();
                        //System.err.println("Place: " + place);
                        setAttributesFromMap(place);
                      }

                    },
                    null // no cancel listener
                );
                
                placesDialog.show();
                
              } else {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(PlaceEditor.this), "No matches found for \"" + queryString + "\"");
              }
              
            } finally {
              lookupper.setEnabled(true);
              lookupper.setText(txt);
            }
          }
        }.start();
        
      }
    });
    
    
    Preferences prefs = Jawb.getPreferences();
    
    if (prefs.getBoolean(SpatialMLTask.TASK_NAME + ".IGDB.enabled")) {
      textLookup.add(lookupper);
    } else if (prefs.getPreference(SpatialMLTask.TASK_NAME + ".IGDB.enabled") == null) {
      // if it's not set, save a default value of "false"
      prefs.setPreference(SpatialMLTask.TASK_NAME + ".IGDB.enabled", false);
      Jawb.storePreferences();
    }
    
    

    addLabelledRow("Text:", textLookup);

    addSeparator();

    // process historical annotation
    
    String id = annot.getAttributeValue("id").toString();
    addLabelledRow("ID:", new JLabel(id));
    
    String h = null; // re-usable placeholder for history text
    
    if (history != null) {
      h = (String)history.getAttributeValue("gazref");
    }
    if (h != null && !h.equals("")) {
      gazRef = addTextFieldRow("Gaz. Ref.:", "", h);
    } else {
      gazRef = addTextFieldRow("Gaz. Ref.:", "");
    }
    
    if (history != null) {
      h = (String)history.getAttributeValue("comment");
    }
    if (h != null && !h.equals("")) {
      comment = addTextFieldRow("Comment:", "", h);
    } else {
      comment = addTextFieldRow("Comment:", "");
    }

    if (history != null) {
      h = (String)history.getAttributeValue("type");
    }
    if (h != null && !h.equals("")) {
      typeSelect = addComboBoxRow("Type:", SpatialMLUtils.placeTypes, h);
    } else {
      typeSelect = addComboBoxRow("Type:", SpatialMLUtils.placeTypes);
    }
    typeSelect.setRenderer(new NameMapComboBoxRenderer(SpatialMLUtils.placeTypeNameMap));
    
    
    if (history != null) {
      h = (String)history.getAttributeValue("mod");
    }
    if (h != null && !h.equals("")) {
      modSelect = addComboBoxRow("Mod:", SpatialMLUtils.mods, h);
    } else {
      modSelect = addComboBoxRow("Mod:", SpatialMLUtils.mods);
    }
    modSelect.setRenderer(new NameMapComboBoxRenderer(SpatialMLUtils.modNameMap));

    if (history != null) {
      h = (String)history.getAttributeValue("continent");
    }
    if (h != null && !h.equals("")) {
      continentSelect = addComboBoxRow("Continent:", SpatialMLUtils.continents, h);
    } else {
      continentSelect = addComboBoxRow("Continent:", SpatialMLUtils.continents);
    }
    continentSelect.setRenderer(new NameMapComboBoxRenderer(SpatialMLUtils.continentNameMap));

    if (history != null) {
      h = (String)history.getAttributeValue("country");
    }
    if (h != null && !h.equals("")) {
      countrySelect = addComboBoxRow("Country:", SpatialMLUtils.countryCodes, h);
    } else {
      countrySelect = addComboBoxRow("Country:", SpatialMLUtils.countryCodes);
    }
    countrySelect.setRenderer(new NameMapComboBoxRenderer(SpatialMLUtils.countryNameMap));

    if (history != null) {
      h = (String)history.getAttributeValue("state");
    }
    if (h != null && !h.equals("")) {
      state = addTextFieldRow("State:", "", h);
    } else {
      state = addTextFieldRow("State:", "");
    }

    if (history != null) {
      h = (String)history.getAttributeValue("county");
    }
    if (h != null && !h.equals("")) {
      county = addTextFieldRow("County:", "", h);
    } else {
      county = addTextFieldRow("County:", "");
    }

    if (history != null) {
      h = (String)history.getAttributeValue("latLong");
    }
    if (h != null && !h.equals("")) {
      latLon = addTextFieldRow("Lat/Long:", "", h);
    } else {
      latLon = addTextFieldRow("Lat/Long:", "");
    }

    if (history != null) {
      h = (String)history.getAttributeValue("form");
    }
    if (h != null && !h.equals("")) {
      formSelect = addComboBoxRow("Form:", SpatialMLUtils.forms, h);
    } else {
      formSelect = addComboBoxRow("Form:", SpatialMLUtils.forms);
    }
    
    if (history != null) {
      h = (String)history.getAttributeValue("CTV");
    }
    if (h != null && !h.equals("")) {
      ctvSelect = addComboBoxRow("C/T/V:", SpatialMLUtils.ctv, h);
    } else {
      ctvSelect = addComboBoxRow("C/T/V:", SpatialMLUtils.ctv);
    }
    
    nonLocUse = new JCheckBox();
    addLabelledRow("Non-Loc Use:", nonLocUse);

    predicative = new JCheckBox();
    addLabelledRow("Predicative:", predicative);
    
    if (history != null) {
      h = (String)history.getAttributeValue("description");
    }
    if (h != null && !h.equals("")) {
      description = addTextFieldRow("Description:", "", h);
    } else {
      description = addTextFieldRow("Description:", "");
    }

    addSeparator();
    if (history != null) {
      addTotalHistoryRow("Copy all data from last PLACE");
    }
    
    //addSeparator();
    // set the form to an initialized state right away
    // this fills in the various fields for us
    reset();

  }
  
    

  /**
   * @see org.mitre.spatialml.callisto.SpatialMLEditor#clear()
   */
  public void clear() {
    super.clear();

    typeSelect.setSelectedItem("");
    modSelect.setSelectedItem("");
    countrySelect.setSelectedItem("");
    continentSelect.setSelectedItem("");
    formSelect.setSelectedItem("");
    ctvSelect.setSelectedItem("");

    gazRef.setText("");
    comment.setText("");
    county.setText("");
    state.setText("");
    latLon.setText("");
    description.setText("");

    nonLocUse.setSelected(false);
    predicative.setSelected(false);
  }

  /**
   * @see org.mitre.spatialml.callisto.SpatialMLEditor#reset()
   */
  public void reset() {
    super.reset();

    setSelectionFromAttribute("type", typeSelect);
    setSelectionFromAttribute("mod", modSelect);
    setSelectionFromAttribute("country", countrySelect);
    setSelectionFromAttribute("continent", continentSelect);
    setSelectionFromAttribute("form", formSelect);
    setSelectionFromAttribute("CTV", ctvSelect);
    
    setTextFromAttribute("gazref", gazRef);
    setTextFromAttribute("comment", comment);
    setTextFromAttribute("county", county);
    setTextFromAttribute("state", state);
    setTextFromAttribute("latLong", latLon);
    setTextFromAttribute("description", description);
    
    log.info("nonLocUse: " + annot.getAttributeValue("nonLocUse") + "(" + annot.getAttributeValue("nonLocUse").getClass() + ")");
    
    String nlu = (String)annot.getAttributeValue("nonLocUse");
    nonLocUse.setSelected((nlu != null && !nlu.equals("")));

    String pred = (String)annot.getAttributeValue("predicative");
    predicative.setSelected((pred != null && !pred.equals("")));

  }

  /**
   * @see org.mitre.spatialml.callisto.SpatialMLEditor#getAnnotation()
   */
  public void updateAnnotation() {

    try {
      setAttributeFromText(gazRef, "gazref");
      setAttributeFromText(comment, "comment");
      setAttributeFromText(county, "county");
      setAttributeFromText(state, "state");
      setAttributeFromText(latLon, "latLong");

      setAttributeFromSelection(typeSelect, "type");
      setAttributeFromSelection(modSelect, "mod");
      setAttributeFromSelection(countrySelect, "country");
      setAttributeFromSelection(continentSelect, "continent");
      setAttributeFromSelection(formSelect, "form");
      setAttributeFromSelection(ctvSelect, "CTV");
      
      if (nonLocUse.isSelected()) {
        annot.setAttributeValue("nonLocUse", "YES");
      } else {
        annot.setAttributeValue("nonLocUse", "");
      }
      
      if (predicative.isSelected()) {
        annot.setAttributeValue("predicative", "YES");
      } else {
        annot.setAttributeValue("predicative", "");
      }
      
    } catch (UnmodifiableAttributeException e) {
      log.log(Level.WARNING, "Couldn't set attribute on " + annot.toString(), e);
    }
  }

  /**
   * Set the values of the input form based on the given map.
   * @param place
   */
  private void setAttributesFromMap(Map place) {
    if (place == null) return; // should not be null!
    
    if (place.containsKey("latLong")) {
      latLon.setText((String)place.get("latLong"));
    }
    if (place.containsKey("gazref")) {
      gazRef.setText((String)place.get("gazref"));
    }
    if (place.containsKey("type")) {
      typeSelect.setSelectedItem((String)place.get("type"));
    }
  }
}
