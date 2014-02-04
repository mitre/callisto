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


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Dialog;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.BevelBorder;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import javax.swing.text.TextAction;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;

//import gov.nist.atlas.type.AnnotationType;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.JawbLogger;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.ToocaanTask;
import org.mitre.jawb.tasks.Exporter;
// import org.mitre.jawb.tasks.JSonExporter;
import org.mitre.jawb.tasks.Importer;
//import org.mitre.jawb.tasks.JSonImporter;
import org.mitre.jawb.atlas.AWBDocument;

/*** bet I don't need any of this
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.NamedExtentRegions;
import org.mitre.jawb.atlas.TextExtentRegion;
import org.mitre.jawb.gui.ActionModel.ActionCollection;
import org.mitre.jawb.gui.ActionModel.ActionDelegate;
import org.mitre.jawb.gui.ActionModel.ActionGroup;
import org.mitre.jawb.gui.ActionModel.ActionProxy;
import org.mitre.jawb.prefs.ColorSpec;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;
import org.mitre.jawb.swing.SetModel;
import org.mitre.jawb.swing.event.SetDataListener;
import org.mitre.jawb.swing.event.SetDataEvent;
***/

import org.mitre.jawb.prefs.Preferences;


import org.mitre.jawb.services.CallistoMATClient;


/**
 * JPanel which displays Workspace options.
 * Use the {@link #createFrame} method to create a frame on a
 * frame. 
 *
 * based loosely on MainTextPalette
 *
 * @author <a href="mailto:robyn@mitre.org"></a>
 * @version 1.0
 */
public class WorkspaceDashboard extends JPanel implements ActionListener {

  private static final int DEBUG = 0;
  // private static final boolean ENABLE_USER_SWITCHING = false;
  
  // TODO should/can the WorkspaceDashboard keep track of which
  // ToocaanTask we're using?
  
  /** place for the actual buttons, etc. */
  private JPanel mainPane;

  /* static workspace details */
  private static String workspaceURL;
  private static String workspaceDir;
  private static String workspaceKey;
  private static boolean isWorkspaceActive;
  private static String priClass;
  private static CallistoMATClient client;
  // private static String[] allUsers;
  // in MAT 2.0 we cannot change users on the fly, so this is going away

  /** pointer back to the JawbFrame this workspace is associated with */
  private static JawbFrame jf;

  /** current workspace details */
  private String curUser;
  private String curFolder;
  private String curPhase;
  private String curBasename;
  private String curPriority;
  private String curLockId;
  private String curDocStatus;
  private JawbDocument curJawbDoc;
  private String curView;

  /* preference key prefixes */
  private final String geomKey = "workspaceDashboard";
  private final String visibleKey = "windows.workspaceDashboard.visible";
  private String tableKey = "table.workspaceDashboard";


  /* components whose contents will change */
  private JLabel curBasenameLabel;
  private JLabel curPhaseLabel;
  private JPanel tablePanel;
  private JCheckBox activeCheck;

  /* components that must be disabled when a file is open */
  // private JComboBox userList;
  private JButton nextButton;
  private JButton openButton; // now not only for non-active learning
  private JRadioButton coreButton;
  private JRadioButton reconButton;
  private JRadioButton listButton;
  private JRadioButton queueButton;

  /* Scroll Pane containing core table */
  private JScrollPane coreScrollPane;
  /* Scroll Pane containing recon table */
  private JScrollPane reconScrollPane;
     
  private JTable coreTable;
  private JTable reconTable;

  /* action command strings (some with other uses as well) */
  private static String coreString = "core";
  private static String reconString = "reconciliation";
  // private static String changeUserString = "choose_user";
  private static String openNextString = "open_next";
  private static String openSelectedString = "open_selected";
  private static String closeWSString = "close_ws";
  private static String listString = "file_listing";
  private static String queueString = "priority_queue";

  /** Jawb close action to be called to close the current file when
   *  closing the workspace
   */
  private static Action closeAction = Jawb.getAction ("close");


  /** the parent JFrame */
  private static JFrame theJFrame = null;

  /**
   * creates the JPanel
   */ 
  public WorkspaceDashboard (String wsURL, String wsDir, String wsKey, 
                             boolean isWsActive,
                             JawbFrame jf, CallistoMATClient client, 
                             // String[] users, 
                             String userid, String folder,
                             String basename) {

    this.workspaceURL = wsURL;
    this.workspaceDir = wsDir;
    this.workspaceKey = wsKey;
    this.isWorkspaceActive = isWsActive;
    this.jf = jf;
    this.client = client;
    //    this.allUsers = users;
    this.curUser = userid;
    this.curFolder = folder;
    this.curView = listString; // always start in list view
    // can't use setBasename yet because the relevant components
    // have not yet been created, so set it directly here and call
    // setBasename at the end of this constructor
    this.curBasename = basename;

    if (curFolder.equals("core")) {
      curPhase = ToocaanTask.HAND_ANNOTATION;
    } else {
      curPhase = ToocaanTask.RECONCILIATION_UNKNOWN;
    }
    if (DEBUG > 0)
      System.err.println("WSDash init: curPhase set to " + curPhase);

    
    setLayout (new BorderLayout ());
    // keep it from stretching the buttons.
    mainPane = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    add (mainPane, BorderLayout.CENTER);
    setBorder (BorderFactory.createCompoundBorder
               (BorderFactory.createEtchedBorder (),
                BorderFactory.createEmptyBorder (4,3,4,3)));

    // Current Basename and Status
    JPanel statusPane = new JPanel(new FlowLayout());
    statusPane.add(new JLabel("Current Basename: "));
    // text will be set using setBasename once all components are created
    curBasenameLabel = new JLabel(); 
    statusPane.add(curBasenameLabel);
    statusPane.add(new JLabel(" Status: "));
    curPhaseLabel = new JLabel(curPhase);
    statusPane.add(curPhaseLabel);
    c.gridx=0;
    c.gridwidth=3; // center it across all 3 columns
    c.gridy=0;
    mainPane.add(statusPane,c);

    // radio buttons to select which folder to view
    coreButton = new JRadioButton(coreString);
    coreButton.setActionCommand(coreString);
    if (curFolder.equals(coreString))
      coreButton.setSelected(true);
    reconButton = new JRadioButton(reconString);
    reconButton.setActionCommand(reconString);
    if (curFolder.equals(reconString))
      reconButton.setSelected(true);
    ButtonGroup folderGroup = new ButtonGroup();
    folderGroup.add(coreButton);
    folderGroup.add(reconButton);

    coreButton.addActionListener(this);
    reconButton.addActionListener(this);

    JPanel radioPanel = new JPanel(new FlowLayout());
    radioPanel.add(new JLabel("Folder: "));
    radioPanel.add(coreButton);
    radioPanel.add(reconButton);
    radioPanel.setBorder (BorderFactory.createCompoundBorder
                          (BorderFactory.createEtchedBorder (),
                           BorderFactory.createEmptyBorder (4,3,4,3)));
    if (isWorkspaceActive)
      c.gridwidth=1;
    else
      c.gridwidth=2;
    c.gridx=0;
    c.weightx=0.25;
    c.gridy=1;
    c.fill=GridBagConstraints.BOTH;
    c.anchor=GridBagConstraints.LINE_START;
    mainPane.add(radioPanel, c);

    // radio buttons to select list versus queue
    if (isWorkspaceActive) {
      listButton = new JRadioButton(listString);
      queueButton = new JRadioButton(queueString);
      listButton.setSelected(true); // always start in list view
      // TODO consider saving last view as a preference
      listButton.setActionCommand(listString);
      queueButton.setActionCommand(queueString);
      ButtonGroup viewGroup = new ButtonGroup();
      viewGroup.add(listButton);
      viewGroup.add(queueButton);
      listButton.addActionListener(this);
      queueButton.addActionListener(this);
      JPanel viewPanel = new JPanel(new FlowLayout());
      viewPanel.add(new JLabel("View: "));
      viewPanel.add(listButton);
      viewPanel.add(queueButton);
      viewPanel.setBorder (BorderFactory.createCompoundBorder
                           (BorderFactory.createEtchedBorder (),
                            BorderFactory.createEmptyBorder (4,3,4,3)));
      c.gridwidth=1;
      c.gridx=1;
      c.weightx=1;
      c.anchor=GridBagConstraints.CENTER;
      mainPane.add(viewPanel,c);
    }

    // user name (menu for debugging, label otherwise);
    // for the moment just building the menu version
    // for 2.0 we can't have this as a menu even for debuggin
    // so now this is just a label

    JLabel userLabel = new JLabel("User ID: ");
    /***************
    userList = new JComboBox(allUsers);
    userList.setSelectedItem(curUser);
    userList.setActionCommand(changeUserString);
    userList.addActionListener(this);
    userList.setEnabled(ENABLE_USER_SWITCHING);
    *************/
    JLabel useridLabel = new JLabel(userid);
    JPanel userPanel = new JPanel(new GridLayout(1,0));
    userPanel.add(userLabel);
    userPanel.add(useridLabel);
    userPanel.setBorder (BorderFactory.createCompoundBorder
                         (BorderFactory.createEtchedBorder (),
                          BorderFactory.createEmptyBorder (4,3,4,3)));
    c.gridx=2;
    c.weightx=0.25;
    c.anchor=GridBagConstraints.LINE_END;
    mainPane.add(userPanel,c);

    // list of files or chunks in the project


    c.gridx=0;
    c.gridwidth=3;
    c.gridy=2;
    c.weighty=1;
    c.anchor=GridBagConstraints.CENTER;
    c.fill=GridBagConstraints.BOTH;
    tablePanel = new JPanel(new GridLayout(1,1));
    updateTables();

    mainPane.add(tablePanel,c);
    c.weighty=0;

    // action buttons
    // TODO: ALL should be disabled if a file is open
    JPanel leftButtonPane = new JPanel(new FlowLayout());
    nextButton = new JButton("Open Next Item");
    nextButton.setActionCommand(openNextString);
    nextButton.addActionListener(this);
    leftButtonPane.add(nextButton);
    // if (!isWorkspaceActive) {
    // let's try always allowing the user to open a selected item
    // in that case Calliso should behave as if it is in bootstrap
    // mode and lightly color all segments
      openButton = new JButton("Open Selected Item");
      openButton.setActionCommand(openSelectedString);
      openButton.addActionListener(this);
      leftButtonPane.add(openButton);
      if (!curFolder.equals("core"))
        openButton.setEnabled(false);
      //     }
    c.gridx=0;
    c.gridy=3;
    c.gridwidth=1;
    c.fill=GridBagConstraints.VERTICAL;
    c.anchor=GridBagConstraints.LINE_START;
    mainPane.add(leftButtonPane,c);

    JPanel rightButtonPane = new JPanel(new FlowLayout());
    JButton closeButton = new JButton("Close Workspace");
    closeButton.setActionCommand(closeWSString);
    closeButton.addActionListener(this);
    rightButtonPane.add(closeButton);
    c.gridx=2;
    c.anchor=GridBagConstraints.LINE_END;
    mainPane.add(rightButtonPane,c);
    

    // workspace info
    JLabel urlLabel = new JLabel("Workspace: " + workspaceDir);
    JLabel keyLabel = new JLabel("Key: " + workspaceKey);
    activeCheck = new JCheckBox("Prioritization");
    activeCheck.setSelected(isWorkspaceActive);
    activeCheck.setEnabled(false);
    JPanel wsinfoPanel = new JPanel(new GridBagLayout());
    GridBagConstraints infoConstraints = new GridBagConstraints();
    infoConstraints.gridx=0;
    infoConstraints.gridy=0;
    infoConstraints.weightx=0.5;
    infoConstraints.ipadx=25;
    infoConstraints.fill=GridBagConstraints.BOTH;
    infoConstraints.anchor=GridBagConstraints.LINE_START;
    wsinfoPanel.add(urlLabel, infoConstraints);
    infoConstraints.gridx++;
    infoConstraints.anchor=GridBagConstraints.CENTER;
    wsinfoPanel.add(keyLabel, infoConstraints);
    infoConstraints.gridx++;
    infoConstraints.anchor=GridBagConstraints.LINE_END;
    wsinfoPanel.add(activeCheck, infoConstraints);
    wsinfoPanel.setBorder (BorderFactory.createCompoundBorder
                           (BorderFactory.createEtchedBorder (),
                            BorderFactory.createEmptyBorder (4,3,4,3)));
    c.gridx=0;
    c.gridy=4;
    c.gridwidth=3;
    c.fill=GridBagConstraints.BOTH;
    mainPane.add(wsinfoPanel,c);
    
    // fill in the basename and enables/disables components according to
    // whether it is null or not
    setBasename(curBasename);

  }

  // table name reflects the folder and view this is used when loading
  // and saving column widths, which depend on both 
  private void initTables() {
      coreTable = new JTable();
      coreTable.setName("core_" + curView);
      coreScrollPane = new JScrollPane(coreTable);
      coreTable.setFillsViewportHeight(true);
      coreTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      loadColumnWidths(coreTable);

      reconTable = new JTable();
      reconTable.setName("reconciliation_" + curView);
      reconScrollPane = new JScrollPane(reconTable);
      reconTable.setFillsViewportHeight(true);
      reconTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      loadColumnWidths(reconTable);
  }

  /** call when a change has occurred to the folders.  Both tables
   *  will be updated, and updateTablePanel is called to place the
   *  relevant one into the tablePanel.
   *  
   */ 
  private void updateTables() {
    if (DEBUG > 0)
      System.err.println("WSDash.updateTables: " + curView);

    // Using an ancestor listener to persist the column widths isn't
    // the right thing to do here becuase widths have to be saved whenever
    // the view changes, and the timing doesn't work out right on the
    // events.  So all column width saves/loads are handled explicitly
    // here.  Since updateTables will be called whenever a table is being
    // changed or removed, this works out fine.

    // init the tables if necessary
    if (coreTable == null || reconTable == null) {
      initTables();
    } else {
      // save the columns previously associated with each table
      // (if view has changed, we will change the name to reflect the
      // new view and get the new widths when we get the new info)
      if (DEBUG > 0) 
        System.err.println("\tsaving columns");
      saveColumnWidths(coreTable);
      saveColumnWidths(reconTable);
    }

    // put the current information into the tables according to the view
    Vector coreTableInfo = getTableInfo(coreString, curView);
    Vector coreTableHeadings = (Vector)coreTableInfo.remove(0);
    TableModel coreModel = 
      new DefaultTableModel(coreTableInfo, coreTableHeadings);
    coreTable.setModel(coreModel);
    coreTable.setName("core_" + curView);
    loadColumnWidths(coreTable);

    Vector reconTableInfo = getTableInfo(reconString, curView);
    Vector reconTableHeadings = (Vector)reconTableInfo.remove(0);
    TableModel reconModel = 
      new DefaultTableModel(reconTableInfo, reconTableHeadings);
    reconTable.setModel(reconModel);
    reconTable.setName("reconciliation_" + curView);
    loadColumnWidths(reconTable);

    updateTablePanel();
  }

  private void loadColumnWidths(JTable table) {
    String prefKeyPrefix = tableKey + "." + workspaceDir + "." + 
      table.getName() + ".";
    System.err.println("WSDash.loadColumnWidths " + prefKeyPrefix);

    Preferences prefs = Jawb.getPreferences ();
    Enumeration enumeration = table.getColumnModel().getColumns();
    while (enumeration.hasMoreElements()) {
      TableColumn column = (TableColumn) enumeration.nextElement();
      String key = prefKeyPrefix + noSpaces(column.getIdentifier().toString());
      int width = prefs.getInteger(key, column.getWidth());
      System.err.println("\t" + key + " = " + width);
      column.setPreferredWidth(width);
    }
  }

  private void saveColumnWidths(JTable table) {
    Preferences prefs = Jawb.getPreferences ();
    Enumeration enumeration = table.getColumnModel().getColumns();
    String keyPrefix = tableKey + "." + workspaceDir + "." + 
      table.getName() + ".";
    System.err.println("WSDash.saveColumnWidths " + keyPrefix);
    
    while (enumeration.hasMoreElements()) {
      TableColumn column = (TableColumn) enumeration.nextElement();
      String key = keyPrefix + noSpaces(column.getIdentifier().toString());
      System.err.println("\t" + key + " = " + column.getWidth());
      prefs.setPreference(key, column.getWidth());
    }
  }

  private String noSpaces (String str) {
    return str.replaceAll(" ","_");
  }

  private Vector getTableInfo(String folder, String view) {
    Vector tableVector = new Vector();
    /***** The old way:  The Table column names 
    private static String [] columnNames = {"Basename","Status"};
    private static Vector columnVector = new Vector(Arrays.asList(columnNames));
    *************/


    List tableInfo;
    if (view.equals(listString)) {
      tableInfo = client.getBasenameInfoExtended(folder);
    } else {
      tableInfo = client.getQueueList(folder, curUser);      
    }

    if (DEBUG > 1)
      System.err.println("WSDash.getTableInfo(" + folder + "," + view +
                         ") -> " + tableInfo);

    // now the column names are the firest item in tableInfo
    Iterator i = tableInfo.iterator();
    while (i.hasNext()) {
      ArrayList rowInfo = (ArrayList)i.next();
      Vector rowVector = new Vector(rowInfo);
      tableVector.add(rowVector);
    }
    return tableVector;
  }

  /***************** never mind they're always the same
  private Vector getTableHeadings(String view) {
    if (view.equals(listString)) {
      return listColumnVector;
    } else {
      return queueColumnVector;
    }
  }

  *********************/

  /* called when we switch folders, or after updating the table contents
   * to show the appropriate table within the panel */
  private void updateTablePanel() {
    tablePanel.hide();
    tablePanel.removeAll();
    if (curFolder.equals(coreString)) {
      tablePanel.add(coreScrollPane);
    } else {
      tablePanel.add(reconScrollPane);
    }
    tablePanel.show();
    mainPane.revalidate();
  }

  public void actionPerformed (ActionEvent e) {
    System.err.println("action source: " +e.getSource());
    System.err.println("selected action: " + e.getActionCommand());
    String cmd = e.getActionCommand();
    JawbLogger logger = Jawb.getLogger();
    /**
    Object[] logArgs = null;
    String logType = null;
    Object[] logArgs2 = null;
    String logType2 = null;
    **/
    if (cmd.equals(coreString) || cmd.equals(reconString)) {
      ((JRadioButton)e.getSource()).setSelected(true);
      if (!curFolder.equals(cmd)) {
        curFolder = cmd;
        if (cmd.equals(reconString))
          openButton.setEnabled(false);
        else
          openButton.setEnabled(true);
        updateTablePanel();
        logger.info(JawbLogger.LOG_WORKSPACE_FOLDER, new String[] {cmd});
      }
    } else if (cmd.equals(listString) || cmd.equals(queueString)) {
      /**** no longer needed: this happens in updateTables and it saves
            into the right view's info
            because the view is encoded into the table name
      // before changing the view, save the old column widths
      if (DEBUG > 0)
        System.err.println("WSDash.actPerf change view - saving col widths");
      if (coreTable != null) {
        saveColumnWidths(coreTable);
      }
      if (reconTable != null) {
        saveColumnWidths(reconTable);
      }
      **************/

      if (DEBUG > 0) 
        System.err.println("WSDash.actPerf found table update action");
      curView = cmd;
      updateTables();
      /**********
    } else if (cmd.equals(changeUserString)) {
      curUser = (String) ((JComboBox)e.getSource()).getSelectedItem();
      logger.info(JawbLogger.LOG_WORKSPACE_USERID, 
                  new String[] {curUser});
      ***********/
    } else if (cmd.equals(openNextString)) {
      // have to call the JawbFrame version that forwards to here
      // so that the JF stuff gets updated too
      logger.info(JawbLogger.LOG_DASHBOARD_BUTTON,
                  new String[] {"Open Next Item", curFolder});
      if (DEBUG > 0)
        System.err.println("WSDash.ap: openNext in folder " + curFolder);
      // this will log itself before calling the finish method
      jf.importNextWorkspaceDoc(this, curFolder);
    } else if (cmd.equals(openSelectedString)) {
      // have to call the JawbFrame version that forwards to here
      // so that the JF stuff gets updated too
      if (DEBUG > 0)
        System.err.println("WSDash.ap: openSelected in folder " + curFolder);
      // TODO need to figure out the basename of the item selected
      JTable curTable;
      if (curFolder.equals("core")) {
        curTable = coreTable;
      }  else {
        curTable = reconTable;
      }
      try {
        int selRow = curTable.getSelectedRows()[0];
        String basename = (String)curTable.getModel().getValueAt(selRow, 0);
        logger.info(JawbLogger.LOG_DASHBOARD_BUTTON,
                    new Object[] {"Open Selected Item", curFolder, basename});
        if (DEBUG > 0)
          System.err.println("WSDash.ap: openSelected basename: " + basename);
        // this will log itself before calling the finish method
        jf.importSelectedWorkspaceDoc(this, basename, curFolder);
      } catch (java.lang.ArrayIndexOutOfBoundsException x) {
        GUIUtils.showError("no document selected");
      }
    } else if (cmd.equals(closeWSString)) {
      if (DEBUG > 1)
        System.err.println("WSDash.ap: closeWS with ActionEvent " + e);
      closeWorkspace(e);
      theJFrame.dispose();
      logger.info(JawbLogger.LOG_CLOSE_WORKSPACE);
      client.uploadLog();
    }
  }

  public void closeWorkspace(ActionEvent e) {
    if (DEBUG > 0)
      System.err.println("WSDash.closeWorkspace()");
    // close the current document if necessary
    if (curJawbDoc != null) 
      closeAction.actionPerformed(e);
    jf.setMATWorkspaceDash(null);
  }

  // this is accessed by CallistoMATClient methods so must be public
  public void updatePhase (String p) {
    if (DEBUG > 0)
      System.err.println("WSDash updatePhase: " +p);
    this.curPhase = p;
    curPhaseLabel.setText(curPhase);
  }
    
  public void updatePrioritizationClass(String c) {
    priClass = c;
    isWorkspaceActive = (priClass != null);
    activeCheck.setSelected(isWorkspaceActive);
  }

  public void setBasename (String b) {
    this.curBasename = b;

    if (b == null) {
      if (DEBUG > 0)
        System.err.println("WSDash.setBasename: null; update phase to empty string");
      curBasenameLabel.setText("none");
      updatePhase("");
      // enable the buttons that make sense when nothing is open
      // userList.setEnabled(ENABLE_USER_SWITCHING);
      nextButton.setEnabled(true);
      coreButton.setEnabled(true);
      reconButton.setEnabled(true);
      // if (!isWorkspaceActive)
      if (curFolder.equals("core"))
        openButton.setEnabled(true);
    } else {
      curBasenameLabel.setText(b);
      // disable the buttons that only make sense when nothing is open
      // userList.setEnabled(false);
      nextButton.setEnabled(false);
      coreButton.setEnabled(false);
      reconButton.setEnabled(false);
      // if (!isWorkspaceActive)
      openButton.setEnabled(false);
      // for non-core the phase gets updated elsewhere
      if (curFolder.equals("core")) 
        updatePhase(ToocaanTask.HAND_ANNOTATION);
      if (DEBUG > 0) 
        System.err.println("WSDash.setBasename: " + b);
    }
    growPack();
  }


  /********************* Frame Handling Methods *********************/
  
  /**
   * Creates and returns a new {@link JFrame} wrapping
   * <code>this</code>.  <code>title</code> is the title of the
   * returned frame. The returned <code>JFrame</code> will be
   * resizable by the user, however programs can invoke setResizable
   * on the JFrame instance to change this property.
   *
   * @see JFrame
   * @see JOptionPane
   */
  public JFrame createFrame (Component parentComponent, String title)
    throws HeadlessException {
    
    final JFrame frame;

    //Window window = getWindowForComponent(parentComponent);
    frame = new JFrame(title);	

    // set this so that GUIUtils.getJawbFrame() can be called on events
    // originating from the Dashboard frame.
    // copied/modified from DetachableTabsJawbComponent
    JComponent c = frame.getRootPane ();
    c.putClientProperty (GUIUtils.JAWB_FRAME_KEY,jf);
    
    Container contentPane = frame.getContentPane();
    
    // what's this all about? copied from JOptionPane
    if (JFrame.isDefaultLookAndFeelDecorated()) {
      boolean supportsWindowDecorations = 
        UIManager.getLookAndFeel().getSupportsWindowDecorations();
      if (supportsWindowDecorations) {
        frame.setUndecorated(true);
      }
    }
   
    contentPane.setLayout(new BorderLayout());
    contentPane.add(this, BorderLayout.CENTER);

    // Load window location from user prefs
    if (! GUIUtils.loadGeometry (frame, geomKey))
      frame.setLocationRelativeTo(parentComponent);

    frame.setAlwaysOnTop(false);
    // use windowClosing listener below to close the workspace first
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.pack(); // keeps location, resizes if it likes

    // listen for movements and window hide/show (storing geometry/detached)
    // hiding a window (via toggle button in frame, MainTextPane hiding, or
    // closing all documents) is controlled by MainTextPane
    frame.addComponentListener (new ComponentAdapter () {
        public void componentMoved (ComponentEvent e) { storeGeometry(); }
        public void componentResized (ComponentEvent e) { storeGeometry(); }
        /** save location and dimension */
        private void storeGeometry () {
          GUIUtils.storeGeometry (frame, geomKey);
        }
      });
    frame.addWindowListener (new WindowAdapter () {
        public void windowClosing (WindowEvent e) {
          Jawb.getPreferences ().setPreference (visibleKey, false);
          closeWorkspace(new ActionEvent(e.getWindow(),e.getID(),
                                         closeWSString));
        }
      });
    this.theJFrame = frame;
    return frame;
  }

  static Window getWindowForComponent(Component parentComponent)
    throws HeadlessException {
    if (parentComponent == null)
      return JOptionPane.getRootFrame();
    if (parentComponent instanceof Frame)
      return (Window)parentComponent;
    return getWindowForComponent(parentComponent.getParent());
  }

  /**
   * Resizes the components window _only_ if it's preferred size is larger
   * than it's current size.
   */
  private void growPack () {
    Window window = getWindowForComponent(this);
    if (window != null) {
      window.validate ();
      Dimension pref = window.getPreferredSize ();
      Dimension size = window.getSize ();
      // only pack if we need to enlarge
      if (pref.height > size.height || pref.width > size.width)
        window.pack ();
    }
  }
  
  private void shrinkPack () {
    Window window = getWindowForComponent(this);
    if (window != null) {
      window.validate ();
      Dimension pref = window.getPreferredSize ();
      Dimension size = window.getSize ();
      // only pack if we need to shrink
      if (pref.height < size.height || pref.width < size.width)
        window.pack ();
    }
  }

  /****************** JawbComponent Stuff **********************/
  /*************
  public void setJawbDocument (JawbDocument doc) {
    document = doc;
    if (doc == null) {
      // Closing down. Clear the actions fired table.
      Iterator removeIter = actionsFired.iterator();
      while (removeIter.hasNext()) {
        removeAction ((Action) removeIter.next());
      }
      actionsFired.clear();
    }
  }

  public JawbDocument getJawbDocument () {
    return document;
  }

  public Set getSelectedAnnots () {
    return Collections.unmodifiableSet ((Set)selectedAnnotModel);
  }

  public Component getComponent () {
    return this;
  }
  *****************/

  /******************** Action Manipulation ***********************/
  // won't use these directly but keep for now to show how to add/grow
  // and remove/shrink if necessary

  /************
  private void removeAction (Action act) {
    if (DEBUG > 1)
      System.err.println ("MTPal.removeAction: "+
                          act.getValue (Action.ACTION_COMMAND_KEY));
    // SAM 1/27/06: This called getActionData, but that was silly,
    // since that creates one if it's not found.
    ActionData data = (ActionData) actionToDataMap.get (act);
    if (data == null)
      return;

    // TODO: this will disable if the removing action is default, but can we
    // get it to select another? should we?
    data.getToggle().setSelected (false); 
    
    buttonGroup.remove (data.getToggle ());

    mainPane.remove (data.getPanel ());
    
    shrinkPack();
  }

  private void addAction (Action act, String defaultCmd, boolean subAction, int where) {
    if (DEBUG > 1)
      System.err.println ("MTPal.addAction: "+
                          act.getValue (Action.ACTION_COMMAND_KEY));

    ActionData data = getActionData (act, subAction);

    // add toggle to button group
    buttonGroup.add (data.getToggle ());

    // turn it on if it's the default
    if ( (defaultCmd != null) && defaultCmd.equals (data.getCommand ()))
      data.getToggle ().doClick ();
    
    mainPane.add (data.getPanel (), where);
    growPack ();
  }

  *******************/


  /**************** Workspace Operations *******************************/

  /**
   *  Either calls nextdoc or takes a random doc off the pile,
   *  according to whether or not there is active learning available
   *  in the workspace
   *
   *  returns the JawbDocument for the imported document,  as well as
   *  saving a pointer to it in the curJawbDoc variable
   */
  public JawbDocument importNextWorkspaceDoc (String folder) {

    JawbDocument newdoc = client.importNextWorkspaceDoc(this, folder, jf);
    this.curJawbDoc = newdoc;
    updateTables();
    return newdoc;
  }

  public JawbDocument importSelectedWorkspaceDoc (String basename, 
                                                  String folder) {

    JawbDocument newdoc = 
      client.importSelectedWorkspaceDoc(this, basename, folder, jf);
    this.curJawbDoc = newdoc;
    updateTables();
    return newdoc;
  }

  
  public boolean saveWorkspaceDocument(boolean releaseLock, boolean markGold) {
    return saveWorkspaceDocument(releaseLock, markGold, inReconciliation());
  }

  public boolean saveWorkspaceDocument(boolean releaseLock, boolean markGold,
                                       boolean markReconciliation) {
    boolean success = 
      client.saveWorkspaceDocument(releaseLock, markGold, markReconciliation,
                                   this);

    if (success) {
      if (releaseLock)
        clearCurrentDocInfo();
      updateTables();
    }
    return success;
  }

  public void releaseLock() {
    // if we have a client and a current LockId, release the lock
    // if curLockId is null then the lock was already released when
    // the file was saved and this is unnecessary
    if (client != null && curLockId != null) {
      client.releaseLock(curLockId, curFolder, curBasename);
      updateTables();
      clearCurrentDocInfo();
    }
  }

  // clears curPriority, curLockId, curDocStatus, curJawbDoc and
  // curMATDoc and calls setBasename(null) to clear the curBasename
  // and re-enable the items that are only available with no file open
  private void clearCurrentDocInfo() {
    curPriority = null;
    curLockId = null;
    curDocStatus = null;
    curJawbDoc = null;
    // curMATDoc = null;
    setBasename(null); 
  }
  
  /************************ Workspace Detail Accessors *********************/

  // first the static details, with only "get" methods

  /** returns the current MAT Workspace URL*/
  public String getMATWorkspaceURL() { return workspaceURL;}

  /** returns the current MAT Workspace Directory*/
  public String getMATWorkspaceDir() { return workspaceDir;}

  /** returns the current MAT Workspace Key*/
  public String getMATWorkspaceKey() { return workspaceKey;}

  /** returns whether the current MAT Workspace uses active learning*/
  public boolean getMATWorkspaceActive() { return isWorkspaceActive;}

  /** returns the current MATALCgiClient */
  public CallistoMATClient getMATClient() { return client;}


  // then the changeable details, with "get" and "set" methods
  // set methods may want to be private, not sure, let's try that
  // in fact if private is ok, maybe they can just go away

  /** returns the current MAT Workspace Userid*/
  public String getMATWorkspaceUserid() { return curUser;}

  /** set the current MAT Workspace Userid */
  private void setMATWorkspaceUserid(String k) { curUser = k; }

  /** returns the current MAT Folder */
  public String getMATFolder() { return curFolder;}

  /** set the current MAT Folder */
  private void setMATFolder(String b) { curFolder = b; }

  public boolean inReconciliation() {
    return (curFolder.equals("reconciliation"));
  }

  /** returns the current MAT DOC Basename */
  public String getMATDocBasename() { return curBasename;}

  /** set the current MAT DOC Basename */
  private void setMATDocBasename(String b) { setBasename(b); }

  /** returns the current MAT DOC Priority */
  public String getMATDocPriority() { return curPriority;}

  /** set the current MAT DOC Priority */
  private void setMATDocPriority(String b) { curPriority = b; }

  /** returns the current MAT Lock ID */
  public String getMATLockId() { return curLockId;}

  /** set the current MAT Lock ID */
  public void setMATLockId(String b) { curLockId = b; }

  /** returns the status of the current MAT Document */
  public String getMATDocStatus() { return curDocStatus;}

  /** set the status of the current MAT document */
  public void setMATDocStatus(String s) { curDocStatus = s;}

  /** returns the annotation/reconciliation phase */
  public String getMATPhase() { return curPhase;}

  /** set the current annotation/reconciliation phase */
  public void setMATPhase(String s) { curPhase = s;}

  /** get current jawb document */
  public JawbDocument getCurrentJawbDocument() { return curJawbDoc; }

  public boolean uploadLog() {
    return client.uploadLog();
  }

}