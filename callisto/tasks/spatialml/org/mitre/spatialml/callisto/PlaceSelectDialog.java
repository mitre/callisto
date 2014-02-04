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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.prefs.Preferences;

/**
 * A dialog for selecting a place from a list
 * @author jricher
 *
 */
public class PlaceSelectDialog extends JDialog {

  private String mapURL;
  
  private PlaceSelectTable table;

  public PlaceSelectDialog(Component c, String title, boolean modal, PlaceSelectTable table,
      final ActionListener okListener,
      ActionListener cancelListener) {
    super(JOptionPane.getFrameForComponent(c), title, modal);

    Preferences prefs = Jawb.getPreferences();
    mapURL = prefs.getPreference(SpatialMLTask.TASK_NAME + ".map.URL");
    if (mapURL == null) {
      // save out a default if none is set yet
      mapURL = "http://maps.google.com/?q=%s";
      prefs.setPreference(SpatialMLTask.TASK_NAME + ".map.URL", mapURL);
      Jawb.storePreferences();
    }
    
    Container cp = getContentPane();

    String okString = "OK";//UIManager.getString("ColorChooser.okText");
    String cancelString = "Cancel";// UIManager.getString("ColorChooser.cancelText");
    String mapString = "Map";
    
    this.table = table;

    table.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.getClickCount() == 2) {
          PlaceSelectTable table = PlaceSelectDialog.this.table; 
          int row = table.rowAtPoint(e.getPoint());
          okListener.actionPerformed(new ActionEvent(e.getSource(), e.getID(), "table clicked + row " + row));
          dispose();
        }
      }
    });
    
    JScrollPane scroller = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    //scroller.add(table);
    
    cp.add(scroller, BorderLayout.CENTER);
    
    /*
     * Create Lower button panel
     * (copied from SpatialMLEditorDialog)
     */
    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
    JButton okButton = new JButton(okString);
    getRootPane().setDefaultButton(okButton);
    okButton.setActionCommand("OK");
    if (okListener != null) {
      okButton.addActionListener(okListener);
    }
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    buttonPane.add(okButton);

    JButton cancelButton = new JButton(cancelString);

    // The following few lines are used to register esc to close the dialog
    Action cancelKeyAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        ((AbstractButton)e.getSource()).doClick();
      }
    }; 
    KeyStroke cancelKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0);
    InputMap inputMap = cancelButton.getInputMap(JComponent.
        WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = cancelButton.getActionMap();
    if (inputMap != null && actionMap != null) {
      inputMap.put(cancelKeyStroke, "cancel");
      actionMap.put("cancel", cancelKeyAction);
    }
    // end esc handling

    cancelButton.setActionCommand("cancel");
    if (cancelListener != null) {
      cancelButton.addActionListener(cancelListener);
    }
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    buttonPane.add(cancelButton);

    
    JButton mapButton = new JButton(mapString);
    mapButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Map place = PlaceSelectDialog.this.table.getSelected();
        String fullURLString;
        try {
          fullURLString = mapURL.replaceAll("%s", URLEncoder.encode((String)place.get("latLong"), "UTF-8"));
          if (place.containsKey("latLong")) {
            BareBonesBrowserLaunch.openURL(fullURLString);
          } else {
            JOptionPane.showMessageDialog(PlaceSelectDialog.this, "No lat/lon value for this place.");
          }
        } catch (UnsupportedEncodingException e1) {
          JOptionPane.showMessageDialog(PlaceSelectDialog.this, "There was an error fetching the map URL: " + e1);
        }
      }
    });
    buttonPane.add(mapButton);
    
    cp.add(buttonPane, BorderLayout.SOUTH);

    pack();
    setLocationRelativeTo(c);
   }

}
