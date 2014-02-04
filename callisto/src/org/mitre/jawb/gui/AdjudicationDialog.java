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
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.ToocaanTask;

import gov.nist.atlas.type.AnnotationType;
/*
 * Class which builds an Adjudication dialog consisting of an
 * AdjudicationPanel with "Ok" and "Cancel" buttons. 
 *
 * This is based on Timex2EditorDialog.java
 */

public class AdjudicationDialog extends JDialog {

  private AdjudicationPanel adjPanel = null;
  private AdjudicationDocument adjdoc;
  private JPanel adjPanelHolder = null;
  private Container contentPane;
  private JawbDocument doc;
  private ToocaanTask task;
  private String userid;
  private JButton okButton; // need long-term access to enable/disable

  // I believe we will always want modal to be true here -- we don't
  // want users messing with the main document during adjudication
  public AdjudicationDialog(Component c, String title, boolean modal,
                            JawbDocument doc, ToocaanTask task,
                            String userid,
                            ActionListener okListener,
                            ActionListener cancelListener,
                            AdjudicationDocument adjdoc)
    throws HeadlessException {

    super(JOptionPane.getFrameForComponent(c), title, modal);
    
    this.doc = doc;
    this.task = task;
    this.userid = userid;
    this.adjdoc = adjdoc;

    String okString = "Done & Next";
    String cancelString = "Cancel";
	
    contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());

    adjPanelHolder = new JPanel();
    adjPanelHolder.setLayout(new FlowLayout(FlowLayout.CENTER));
    contentPane.add(adjPanelHolder, BorderLayout.CENTER);      
    
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
          Jawb.getLogger().info(JawbLogger.LOG_ADJUDICATION_CLOSE);
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

  public void setSegment(SubordinateSetsAnnotation segment) {
    if (adjPanel != null) {                                     
      adjPanelHolder.remove(adjPanel);
    }
    adjPanel = new AdjudicationPanel(doc, task, segment, userid, this, adjdoc);
    adjPanelHolder.add(adjPanel);
    
    // make dialog resize to accommodate new size of AdjPanel
    setVisible(false);
    pack();
    setVisible(true);
  }

  public void enableOkButton(boolean val) {
    okButton.setEnabled(val);
  }

  public String getVote() {
    if (adjPanel == null)
      return null;
    return adjPanel.getVote();
  }

  public AWBAnnotation getVoteAnnot(String userid) {
    if (adjPanel == null)
      return null;
    return adjPanel.getVoteAnnot(userid);
  }

  public AWBAnnotation getIgnoreVote() {
    if (adjPanel == null)
      return null;
    return adjPanel.getIgnoreVote();
  }

  public AWBAnnotation getBadBoundsVote() {
    if (adjPanel == null)
      return null;
    return adjPanel.getBadBoundsVote();
  }

  public AWBAnnotation getVoteFromId(String voteId) {
    if (adjPanel == null)
      return null;
    return adjPanel.getVoteFromId(voteId);
  }

  public JawbDocument getMiniJD(String id) {
    if (adjPanel == null)
      return null;
    return adjPanel.getMiniJD(id);
  }
  
  public AWBAnnotation getSegment() {
    if (adjPanel == null)
      return null;
    return adjPanel.getSegment();
  }

  public PhraseTaggingAnnotation getSegmentExtent() {
    if (adjPanel == null)
      return null;
    return adjPanel.getSegmentExtent();

  }

  public AdjudicationPanel getAdjPanel() {
    return adjPanel;
  }
}

