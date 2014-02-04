
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

import org.mitre.jawb.Jawb;

public class MainTextFinder extends JPanel {

  private static final int DEBUG = 0;

  MainTextPane mainText;
  
  JTextField textField;
  JCheckBox ignoreCaseCheck;
  //JCheckBox regexCheck;
  JCheckBox wrapAroundCheck;
  //JCheckBox backwardCheck;
  JButton previousButton;
  JButton nextButton;
  JButton cancelButton;
  
  public MainTextFinder(MainTextPane mtp) {
    super(new BorderLayout());
    mainText = mtp;
    init();
  }

  private void init() {
    // text to search for
    JLabel findLabel = new JLabel("Find text: ");
    textField = new JTextField(15);

    JPanel textPanel = new JPanel(new FlowLayout());
    textPanel.add(textField);
    textPanel.add(findLabel);

    // check boxes for various things
    ignoreCaseCheck = new JCheckBox("Case insensitive");
    //regexCheck = new JCheckBox("Regular expression");
    wrapAroundCheck = new JCheckBox("Wrap at end of file");
    //backwardCheck = new JCheckBox("Search backwards");
    
    // default case insensitive and wrap to true
    ignoreCaseCheck.setSelected(true);
    wrapAroundCheck.setSelected(true);


    JPanel checkBoxes = new JPanel(new GridLayout(2,2));
    checkBoxes.add(ignoreCaseCheck);
    //checkBoxes.add(regexCheck);
    checkBoxes.add(wrapAroundCheck);
    //checkBoxes.add(backwardCheck);

    // buttons to do the search and leave
    Action previousAction = new AbstractAction("Previous") {
        public void actionPerformed(ActionEvent e) {
          findText(false);
        }
      };
    previousAction.putValue(Action.MNEMONIC_KEY, new Integer('P'));
    previousButton = new JButton(previousAction);
    
    Action nextAction = new AbstractAction("Next") {
        public void actionPerformed(ActionEvent e) {
          findText(true);
        }
      };
    nextAction.putValue(Action.MNEMONIC_KEY, new Integer('N'));
    nextButton = new JButton(nextAction);
    
    Action cancelAction = new AbstractAction("Cancel") {
        public void actionPerformed(ActionEvent e) {
          mainText.setFindDialogVisible(false);
        }
      };
    cancelAction.putValue(Action.MNEMONIC_KEY, new Integer('C'));
    cancelButton = new JButton(cancelAction);

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttons.add(previousButton);
    buttons.add(nextButton);
    buttons.add(cancelButton);

    // Keystrokes
    ActionMap actionMap = getActionMap ();
    actionMap.put ("next", nextAction);
    actionMap.put ("previous", previousAction);
    actionMap.put ("cancel", cancelAction);

    InputMap inputMap = getInputMap (WHEN_IN_FOCUSED_WINDOW);
    //inputMap.put (KeyStroke.getKeyStroke (KeyEvent.VK_ENTER,0), "next");
    inputMap.put (KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE,0),"cancel");

    // all together now
    setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    add(textPanel, BorderLayout.NORTH);
    add(checkBoxes, BorderLayout.CENTER);
    add(buttons, BorderLayout.SOUTH);
  }

  /**
   * Creates and returns a new non modal {@link JDialog} wrapping
   * <code>this</code> centered on the parentComponent in the
   * parentComponent's frame. <code>title</code> is the title of the returned
   * dialog. The returned <code>JDialog</code> will be resizable by the user,
   * however programs can invoke setResizable on the JDialog instance to
   * change this property.
   *
   * @see JDialog
   * @see JOptionPane
   */
  JDialog createDialog (Component parentComponent, String title)
    throws HeadlessException {
    
    final JDialog dialog;

    Window window = getWindowForComponent(parentComponent);
    if (window instanceof Frame) {
      dialog = new JDialog((Frame)window, title, false);	
    } else {
      dialog = new JDialog((Dialog)window, title, true);
    }
    Container contentPane = dialog.getContentPane();
    
    // TODO: what's this all about? copied from JOptionPane
    if (JDialog.isDefaultLookAndFeelDecorated()) {
      boolean supportsWindowDecorations = 
        UIManager.getLookAndFeel().getSupportsWindowDecorations();
      if (supportsWindowDecorations) {
        dialog.setUndecorated(true);
      }
    }
   
    contentPane.setLayout(new BorderLayout());
    contentPane.add(this, BorderLayout.CENTER);

    // Load window location from user prefs
    final String geomKey = "mainTextFinder";
    final String visibleKey = "windows.mainTextFinder.visible";
    if (! GUIUtils.loadGeometry (dialog, geomKey))
      dialog.setLocationRelativeTo(parentComponent);

    dialog.pack(); // keeps location, resizes if it likes
    dialog.setResizable(false);

    // listen for movements and window hide/show (storing geometry/detached)
    // hiding a window (via toggle button in frame, MainTextPane hiding, or
    // closing all documents) is controlled by MainTextPane
    dialog.addComponentListener (new ComponentAdapter () {
        public void componentMoved (ComponentEvent e) { storeGeometry(); }
        public void componentResized (ComponentEvent e) { storeGeometry(); }
        /** save location and dimension */
        private void storeGeometry () {
          GUIUtils.storeGeometry (dialog, geomKey);
        }
      });
    dialog.addWindowListener (new WindowAdapter () {
        public void windowClosing (WindowEvent e) {
          Jawb.getPreferences ().setPreference (visibleKey, false);
        }
      });
    
    dialog.getRootPane().setDefaultButton(nextButton);
    return dialog;
  }

  static Window getWindowForComponent(Component parentComponent)
    throws HeadlessException {
    if (parentComponent == null)
      return JOptionPane.getRootFrame();
    if (parentComponent instanceof Frame || parentComponent instanceof Dialog)
      return (Window)parentComponent;
    return getWindowForComponent(parentComponent.getParent());
  }

  public void findText(boolean isForward) {
    JTextPane tp = mainText.getTextPane();
    tp.requestFocus(); // highlight won't show unless TP has it's windows
                       // focus. is there a way to avoid this flicker?
    requestFocus(); // this bring focus back to us

    String findText = textField.getText();
    String docText = null;
    try {
      // I get the text from the document, because retrieveing from
      // tp.getText() seems to convert newlines to the current system.
      Document doc = tp.getDocument();
      docText = doc.getText(0, doc.getLength());
    } catch (BadLocationException x) {
      return; // impossible?
    }

    boolean caseSensitive = ! ignoreCaseCheck.isSelected();
    int offset = find(caseSensitive, docText, findText, -1, isForward);
    
    if (offset < 0 && wrapAroundCheck.isSelected()) {
      int start = 0;
      if (! isForward)
        start = docText.length()-1;
      offset = find(caseSensitive, docText, findText, start, isForward);
    }
  }

  /**
   * This works like indexOf (on docText) would work if it had a
   * case-insensitive option. This is private and implements the actual
   * search. Separate so that the  action method can call int multiple times
   * when wrapping. if start is -1, uses the current mark.
   *
   * Made public for use in Autotaggers RK 12/15/06
   */
  public int find(boolean caseSensitive, String docText, String findText,
                  int pos, boolean isForward) {
    if (DEBUG > 0)
      System.err.println("MTF.find " + findText + " from: " + pos +
                         " case sensitive: " + (caseSensitive?"yes":"no") +
                         " direction: " + (isForward?"forward":"backward"));
    JTextPane tp = mainText.getTextPane();
    Caret caret = tp.getCaret();
    
    int dot = caret.getDot();
    int mark = caret.getMark();
    if (dot > mark) {
      int temp = dot;
      dot = mark;
      mark = temp;
    }

    if (pos < 0) {
      if (isForward) { // forward searches start at end of current selection
        pos = mark;
      }
      else { // backward starts at current mark or len before current selection
        pos = dot;
        if (dot != mark) {
          pos -=  findText.length();
        }
      }
    }

    int offset = -1;
    if (caseSensitive) {
      if (isForward)
        offset = docText.indexOf(findText, pos);
      else
        offset = docText.lastIndexOf(findText, pos);
    }
    else {
      // bah, this is super slow, but the other option is to copy / paste /
      // reconstruct a 'indexOfIgnoreCase' out of pieces of String.java instead
      // use what they give us, starting from the end of the current selection
      int len = findText.length();
      int end = docText.length();
      if (DEBUG > 1)
        System.err.println("MTF.find docText.length() = " + end);
      int step = (isForward ? 1 : -1);
      if (isForward)
        end = end - len; // was: end = end - mark - len; but mark is
                         // irrelevant here RK 9/10/05
      else if (pos > end-len)
        pos = end-len;
      
      // step therough the string, searching a couple chars at a time
      while (pos<end && pos>=0) {
        if (docText.regionMatches(true, pos, findText, 0, len)) {
          offset = pos;
          break;
        }
        if (DEBUG > 4)
          System.err.println("MTF.find no match at: " + pos +
                             " len: " + len  + " text: " +
                             docText.substring(pos, pos+len));
        pos += step;
      }
    }
    if (offset >= 0) {
      int tail = findText.length() + offset;
      caret.setDot(isForward ? offset : tail);
      caret.moveDot(isForward ? tail : offset);
    }
    else {
      Toolkit.getDefaultToolkit().beep();
    }
    
    return offset;
  }
}
