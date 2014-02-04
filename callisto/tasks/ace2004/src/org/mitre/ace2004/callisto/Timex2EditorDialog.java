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

package org.mitre.ace2004.callisto;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.swing.*;
import javax.swing.text.Position.Bias;
import javax.swing.border.EtchedBorder;
import javax.swing.event.*;

import org.mitre.jawb.swing.CalendarPanel;
import org.mitre.jawb.swing.EnableLabel;
import org.mitre.jawb.swing.PopupWindow;

/**
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */


/*
 * Class which builds a timex2 dialog consisting of a Timex2Editor with "Ok",
 * "Cancel", and "Reset" buttons. This is a modified version of what's in
 * JColorChooser java1.4
 *
 * Note: This needs to be fixed to deal with localization!
 */
public class Timex2EditorDialog extends JDialog {
    private Timex2Editor.Timex2Data initialTimex2;
    private Timex2Editor.Timex2Data clearTimex2 = 
      new Timex2Editor.Timex2Data(); // assume it starts out empty??
    private Timex2Editor editorPane;

    public Timex2EditorDialog(Component c, String title, boolean modal,
                               Timex2Editor editorPane,
                               ActionListener okListener,
                               ActionListener cancelListener)
        throws HeadlessException {
        super(JOptionPane.getFrameForComponent(c), title, modal);
        //setResizable(false);

        this.editorPane = editorPane;

	String okString = "OK";//UIManager.getString("ColorChooser.okText");
	String cancelString = "Cancel";// UIManager.getString("ColorChooser.cancelText");
	String resetString = "Reset";//UIManager.getString("ColorChooser.resetText");
	String clearString = "Clear Form";
	
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(editorPane, BorderLayout.CENTER);

        /*
         * Create Lower button panel
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
                hide();
            }
        });
        buttonPane.add(cancelButton);

        JButton resetButton = new JButton(resetString);
        resetButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               reset();
           }
        });
        buttonPane.add(resetButton);

	JButton clearButton = new JButton (clearString);
	clearButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
	      clearForm();
	    }
	  });
	buttonPane.add(clearButton);

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

    public void show() {
        initialTimex2 = editorPane.getTimex2();
	// set the text string for the "clear timex2" data
	clearTimex2.text = initialTimex2.text;
        super.show();
    }

    public void reset() {
        editorPane.setTimex2(initialTimex2);
    }

  public void clearForm() {
    editorPane.setTimex2(clearTimex2);
  }

}

