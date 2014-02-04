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

package org.mitre.jawb.tasks.generic;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import javax.swing.*;

import org.mitre.jawb.io.*;
import org.mitre.jawb.tasks.*;
import org.mitre.jawb.Jawb;
import org.mitre.jawb.gui.*;

public class GenericTaskPanel extends JPanel {

  private static final int DEBUG = 0;
  private static final String GENERIC_LAST_DIR_KEY = "callisto.generic.dir.last";
  private File dtdFile;

  private JTextField dtdField;
  private JFileChooser chooser;
  private JFrame frame;
  private JDialog dialog;

  private JTextField nameField;
  private JTextField titleField;
  private JTextField versionField;
  private JTextArea descriptionField;
  private JCheckBox installNow;

  private JLabel nameLabel;
  private JLabel titleLabel;
  private JLabel versionLabel;
  private JLabel descriptionLabel;
  private JLabel installLabel;

  private Action compileAction;
  private Action closeAction;

  private DTDTaskCompiler compiler;

  /** cached here because we may get from Jawb object or sys props. */
  private File callistoHome = null;
  
  public GenericTaskPanel() {
    callistoHome = Jawb.getCallistoHome();
    if (callistoHome == null) {
      String home =  System.getProperty ("callisto.home");
      if (home != null)
        callistoHome = new File(home);
    }
    init();
  }
  
  public void showFrame(Component parent) {
    if (frame == null) {
      frame = new JFrame("DTD Task Compiler"); // HIDE_ON_CLOSE by default
      frame.getContentPane().add(this);
      
      frame.pack();
      frame.setSize(new Dimension(550, frame.getSize().height));
      //dialog.validate();
      GUIUtils.centerComponent(parent, frame);
    }
    frame.show();
  }

  public void showDialog(Component parent) {
    if (dialog == null) {
      Window window = getWindowForComponent(parent);
      dialog = new JDialog((JFrame)window, "DTD Task Compiler", true);
      dialog.getContentPane().add(this);
      
      dialog.pack();
      dialog.setSize(new Dimension(550, dialog.getSize().height));
      //dialog.validate();
    }
    dialog.show();
  }

  public void open(File dtdFile) {

    if (DEBUG > 0)
      System.err.println("RefEdit.open: "+dtdFile);

    this.dtdFile = dtdFile;
    
    if (dtdFile == null) {
      clear();
      return;
    }

    Jawb.getPreferences().setPreference(GENERIC_LAST_DIR_KEY, dtdFile.getPath());

    try { // Parse the dtdFile
      if (DEBUG > 0)
        System.err.println("GTPanel: parsing...");

      dtdField.setText(dtdFile.toString());

      compiler = new DTDTaskCompiler();
      compiler.read(dtdFile);
      
      nameField.setText(compiler.getName());
      titleField.setText(compiler.getTitle());
      versionField.setText(compiler.getVersion());
      descriptionField.setText(compiler.getDescription());

      enableCompiling(true);
      
    } catch (IOException ioe) {
      GUIUtils.showWarning(ioe.getMessage());
      clear();
      return;
    }
  }

  private void enableCompiling(boolean enable) {
    nameField.setEnabled(enable);
    titleField.setEnabled(enable);
    versionField.setEnabled(enable);
    descriptionField.setEnabled(enable);
    
    nameLabel.setEnabled(enable);
    titleLabel.setEnabled(enable);
    versionLabel.setEnabled(enable);
    descriptionLabel.setEnabled(enable);
    installNow.setEnabled(enable);

    compileAction.setEnabled(enable);
  }
  
  private void compile() {
    File temp = null;
    // check our values
    if (dtdFile == null) {
      GUIUtils.beep();
      return;
    }
    try {
      File installedJar = null;

      // simply set's it back to what was read from DTD if unchanged
      compiler.setName(nameField.getText());
      compiler.setTitle(titleField.getText());
      compiler.setVersion(versionField.getText());
      compiler.setDescription(descriptionField.getText());
      
      compiler.compile();
      
      if (installNow.isSelected()) {
        File jar = compiler.getOutput();
        if (! jar.isFile())
          throw new IllegalStateException("Was expecting a .jar: "+jar);

        installedJar = new File(callistoHome, "tasks/"+jar.getName());
        if (installedJar.exists() && ! installedJar.canWrite()) {
          GUIUtils.showWarning("Unable to install jar: "+installedJar);
          installedJar = null;
        } else {
          int c;
          byte[] bytes = new byte[1024];
          InputStream in = new BufferedInputStream(new FileInputStream(jar));
          OutputStream out =
            new BufferedOutputStream(new FileOutputStream(installedJar));
          while ( (c = in.read(bytes)) != -1)
            out.write(bytes, 0, c);
          in.close();
          out.close();
        }
      }
      
      // once successfully compiled
      String message = "Task written to: " + compiler.getOutput();
      if (installedJar != null)
        message += "\nand installed to " + installedJar +
          "\n\n Restart Callisto to make the task available.";

      GUIUtils.showMessage(message);
      
    } catch (Exception x) {
      GUIUtils.showError("Error compiling task from:\n"+dtdFile+
                         "\n\n"+x.getMessage());
      x.printStackTrace();
    }
  }
  
  private void close() {
    if (dialog != null)
      dialog.hide();
    else
      frame.hide();
    // else WTF!?!?

    dtdFile = null;
    dtdField.setText("");
    clear();
  }

  private void clear() {
    if (DEBUG > 0)
      System.err.println("RefEdit.clear");

    nameField.setText("");
    titleField.setText("");
    versionField.setText("");
    descriptionField.setText("");

    compiler = null;
    
    enableCompiling(false);
  }

  public static void main(String args[]) {
    GenericTaskPanel relocator = new GenericTaskPanel();
    relocator.showDialog(new JFrame());
    System.exit(0);
  }

  /***********************************************************************/
  /* Initialization */
  /***********************************************************************/

  private void init() {
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

    // file chooser used by 'browse' button
    chooser = new JFileChooser();
    chooser.setMultiSelectionEnabled(false);

    // start up where user left off last time (or default=user home)
    String last = Jawb.getPreferences().getPreference(GENERIC_LAST_DIR_KEY);
    if (last != null)
      chooser.setCurrentDirectory(new File(last));
    
    // description of this dialog
    JTextArea text = new JTextArea
      ("Select a DTD to generate a Callisto task definition");
    text.setEditable(false);
    text.setBackground(this.getBackground());
    text.setBorder(BorderFactory.createEmptyBorder(0,0,8,0));

    // name, title, description, browse field/button, and a checkbox
    nameField = new JTextField();
    titleField = new JTextField();
    versionField = new JTextField();
    descriptionField = new JTextArea(5,20);
    descriptionField.setWrapStyleWord(true);
    dtdField = new JTextField();
    dtdField.addActionListener(new AbstractAction("dtdField") {
        public void actionPerformed(ActionEvent e) {
          File file = new File(dtdField.getText());
          open(file);
        }
      });
    Action browseAction = new AbstractAction("Browse") {
        public void actionPerformed(ActionEvent e) {
          chooser.setDialogTitle("Choose DTD File");
          chooser.showDialog(GenericTaskPanel.this, "Select");
          open(chooser.getSelectedFile());
        }
      };
    browseAction.putValue (Action.MNEMONIC_KEY, new Integer ('B'));
    JButton choose = new JButton(browseAction);
    
    // only available if CALLISTO_HOME is known
    installNow = new JCheckBox("Install Now");
    installNow.setEnabled(callistoHome != null);

    // layout of all above
    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    JPanel taskInfo = new JPanel(gb);
    
    c.anchor = GridBagConstraints.FIRST_LINE_START;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridy = 0;
    c.weighty = 0;
    c.weightx = 0;

    JLabel label = new JLabel("DTD File: ");
    c.gridwidth = 1;
    c.weightx = 0;
    gb.setConstraints(label, c);
    taskInfo.add(label);

    c.anchor = GridBagConstraints.LINE_START;
    c.gridy++;
    c.weighty = 0;
    c.gridwidth = 2;
    gb.setConstraints(dtdField, c);
    taskInfo.add(dtdField);
    
    c.gridwidth = 1;
    gb.setConstraints(choose, c);
    taskInfo.add(choose);

    // spacer
    Component spacer = Box.createVerticalStrut(7);
    c.gridy++;
    c.gridwidth = 3;
    gb.setConstraints(spacer, c);
    taskInfo.add(spacer);

    nameLabel = new JLabel("Task Name (as in \"org.mitre.rdc\"): ");
    c.gridy++;
    c.gridwidth = 1;
    gb.setConstraints(nameLabel, c);
    taskInfo.add(nameLabel);
    
    c.gridwidth = 1;
    c.weightx = 1;
    gb.setConstraints(nameField, c);
    taskInfo.add(nameField);
    
    titleLabel = new JLabel("Task Title (human readable): ");
    c.gridy++;
    c.gridwidth = 1;
    c.weightx = 0;
    gb.setConstraints(titleLabel, c);
    taskInfo.add(titleLabel);

    c.gridwidth = 1;
    gb.setConstraints(titleField, c);
    taskInfo.add(titleField);
    
    versionLabel = new JLabel("Task Version: ");
    c.gridy++;
    c.gridwidth = 1;
    gb.setConstraints(versionLabel, c);
    taskInfo.add(versionLabel);

    c.gridwidth = 1;
    gb.setConstraints(versionField, c);
    taskInfo.add(versionField);
    
    descriptionLabel = new JLabel("Task Description (optional): ");
    c.gridy++;
    c.gridwidth = 1;
    gb.setConstraints(descriptionLabel, c);
    taskInfo.add(descriptionLabel);

    c.gridwidth = 2;
    c.weighty = 1;
    JScrollPane scroller = new JScrollPane(descriptionField);
    gb.setConstraints(scroller, c);
    taskInfo.add(scroller);

    c.gridy++;
    c.gridwidth = 3;
    gb.setConstraints(installNow, c);
    taskInfo.add(installNow);
    

    // additional buttons
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    compileAction = new AbstractAction("Compile") {
        public void actionPerformed(ActionEvent e) { compile(); }
      };
    closeAction = new AbstractAction("Close") {
        public void actionPerformed(ActionEvent e) { close(); }
      };
    buttons.add(new JButton(compileAction));
    buttons.add(new JButton(closeAction));
    
    // all together now
    JPanel fileEditor = new JPanel(new BorderLayout());
    fileEditor.add(taskInfo, BorderLayout.NORTH);

    add(text, BorderLayout.NORTH);
    add(fileEditor, BorderLayout.CENTER);
    add(buttons, BorderLayout.SOUTH);

    clear();
  }

  static Window getWindowForComponent(Component parentComponent)
    throws HeadlessException {
    if (parentComponent == null)
      return JOptionPane.getRootFrame();
    if (parentComponent instanceof Frame || parentComponent instanceof Dialog)
      return (Window) parentComponent;
    return getWindowForComponent(parentComponent.getParent());
  }
}
