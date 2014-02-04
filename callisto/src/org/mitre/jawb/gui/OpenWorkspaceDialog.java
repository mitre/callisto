/*
 * Copyright (c) 2002-2010 The MITRE Corporation
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

import java.util.*;
import javax.swing.*;
import java.io.*;
import java.net.URI;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import org.mitre.jawb.*;
import org.mitre.jawb.atlas.*;

import gov.nist.atlas.type.AnnotationType;
/*
 * Class which builds an OpenWorkspace dialog consisting of an
 * WorkspaceAccessory with "Ok" and "Cancel" buttons. 
 *
 * This is based on Timex2EditorDialog.java
 */

public class OpenWorkspaceDialog extends JDialog {

  private WorkspaceAccessory wsPanel = null;
  private JPanel wsPanelHolder = null;
  private Container contentPane;
  private JButton okButton; // need long-term access to enable/disable
  private boolean isOK = false; // will be true if the OK button is pressed

  // I believe we will always want modal to be true here
  public OpenWorkspaceDialog(Component c, String title, boolean modal,
                            ActionListener okListener,
                            ActionListener cancelListener)
    throws HeadlessException {

    super(JOptionPane.getFrameForComponent(c), title, modal);
    

    String okString = "Open Workspace";
    String cancelString = "Cancel";
	
    contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());

    wsPanelHolder = new JPanel();
    wsPanelHolder.setLayout(new FlowLayout(FlowLayout.CENTER));
    contentPane.add(wsPanelHolder, BorderLayout.CENTER); 
    setupPanel();
    
    /*
     * Create Lower button panel
     */
    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
    okButton = new JButton(okString);
    getRootPane().setDefaultButton(okButton);
    okButton.setActionCommand("OK");
    if (okListener != null) {
      okButton.addActionListener(okListener);
    }
    okButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setOK(true);
          hide();
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
          Jawb.getLogger().info(JawbLogger.LOG_OPENWS_CANCEL);
          setOK(false);
          hide();
        }
      });
    buttonPane.add(cancelButton);

    contentPane.add(buttonPane, BorderLayout.SOUTH);

    if (JDialog.isDefaultLookAndFeelDecorated()) {
      boolean supportsWindowDecorations = 
        UIManager.getLookAndFeel().getSupportsWindowDecorations();
      if (supportsWindowDecorations) {
        getRootPane().setWindowDecorationStyle(JRootPane.COLOR_CHOOSER_DIALOG);
      }
    }
    applyComponentOrientation(((c == null) ? getRootPane() : c).getComponentOrientation());

    pack();
    setLocationRelativeTo(c);
  }

  private void setupPanel() {
    if (wsPanel != null) {                                     
      wsPanelHolder.remove(wsPanel);
    }
    wsPanel = new WorkspaceAccessory();
    wsPanelHolder.add(wsPanel);
  }


  public void enableOkButton(boolean val) {
    okButton.setEnabled(val);
  }

  public String getWorkspaceKey() {
    if (wsPanel == null)
      return null;
    return wsPanel.getWorkspaceKey();
  }

  public String getWorkspaceUser() {
    if (wsPanel == null)
      return null;
    return wsPanel.getWorkspaceUser();
  }

  public String getWorkspaceNameFromTypeIn() {
    if (wsPanel == null)
      return null;
    return wsPanel.getWorkspaceNameFromTypeIn();
  }

  public String getWorkspaceURL() {
    if (wsPanel == null)
      return null;
    return wsPanel.getWorkspaceURL();
  }

  public String getWorkspaceFolder() {
    if (wsPanel == null)
      return null;
    return wsPanel.getWorkspaceFolder();
  }

  public boolean getOpenNextDoc() {
    if (wsPanel == null)
      return false;
    return wsPanel.getOpenNextdoc();
  }

  public boolean isOK() {
    return isOK;
  }

  private void setOK(boolean b) {
    isOK = b;
  }

  public WorkspaceAccessory getWorkspacePanel() {
    return wsPanel;
  }
}

