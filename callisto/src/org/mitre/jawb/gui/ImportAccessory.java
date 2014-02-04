
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

import java.lang.String;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.Importer;

// testing
import javax.swing.JFrame;
import java.util.LinkedList;

/**
 * For showing a panel on the side of our filechooser that
 * displays information about the tasks and their Importers
 */
public class ImportAccessory extends JPanel {

  /** Constant to indicat no known tasks have importers */
  private static String NO_IMPORTERS = "<No Importers>";
  
  private JComboBox taskCombo;      // lists the tasks
  private JComboBox importerCombo; // current one base on taskCombo
  private JPanel importerComboPane; // container for dynamic importerCombo
  private JTextArea taskDescText; // text of task description
  private JTextArea importerDescText;  // text of importer description
  private HashMap taskWrapperMap; // task to wrapper for external setting
  private EncodingAccessory encoding;
  //private MIMETypeAccessory mimeType;
  
  /**
   * Creates a new Task Accessory, allowing users to select a task and one
   * of its Importers when selecting a file. Has panels for descriptions as
   * well.
   *
   * @param taskList A list containing possible tasks
   */
  public ImportAccessory(List taskList) {
    setLayout(new BorderLayout(5,5));
        
    taskWrapperMap = new HashMap ();
    taskCombo = new JComboBox();
    taskCombo.addItemListener (new ItemListener () {
        public void itemStateChanged(ItemEvent evt) {
          if (evt.getStateChange() == ItemEvent.SELECTED) {
            updateComponents ((MyTaskWrapper) evt.getItem());
          }}});

    // Panel for task selection and importer on side
    JPanel taskPanel = new JPanel (new BorderLayout (5,5));
    
    taskDescText = new JTextArea(5,10);
    taskDescText.setLineWrap(true);
    taskDescText.setWrapStyleWord(true);
    taskDescText.setBorder (BorderFactory.createEmptyBorder (2,2,2,2));
    taskDescText.setEditable (false);
    JScrollPane scroller = new JScrollPane(taskDescText);
    
    taskPanel.add(taskCombo, BorderLayout.NORTH);
    taskPanel.add(scroller, BorderLayout.CENTER);
    
    // Panel for comboPane and description
    JPanel importerPane = new JPanel (new BorderLayout (5,5));
    // Panel holding just the combo box importer of current task
    importerComboPane = new JPanel (new BorderLayout());
    
    importerDescText = new JTextArea (5,10);
    importerDescText.setLineWrap(true);
    importerDescText.setWrapStyleWord(true);
    importerDescText.setBorder (BorderFactory.createEmptyBorder (2,2,2,2));
    importerDescText.setEditable (false);
    scroller = new JScrollPane(importerDescText);
    
    importerPane.add(importerComboPane, BorderLayout.NORTH);
    importerPane.add(scroller, BorderLayout.CENTER);
    
    taskPanel.add(importerPane, BorderLayout.EAST);
    taskPanel.setBorder (BorderFactory.createCompoundBorder
                     (BorderFactory.createTitledBorder ("Available Importers"),
                          BorderFactory.createEmptyBorder (5,5,5,5)));
    
    encoding = new EncodingAccessory ();
    //mimeType = new MIMETypeAccessory ();
    
    //JPanel bottom = new JPanel (new BorderLayout ());
    //bottom.add (mimeType, BorderLayout.CENTER);
    //bottom.add (encoding, BorderLayout.SOUTH);
    
    this.add (taskPanel, BorderLayout.CENTER);
    this.add (encoding, BorderLayout.SOUTH);
    //this.add (bottom, BorderLayout.SOUTH);

    // this is added to the Importer comboboxes via the TaskWrappers
    ItemListener impListener = new ItemListener () {
        public void itemStateChanged (ItemEvent evt) {
          if (evt.getStateChange() == ItemEvent.SELECTED) {
            updateComponents ((Importer) evt.getItem());
          }}};
    
    Iterator iter = taskList.iterator ();
    while (iter.hasNext ()) { // assumes all are non-null
      Task task = (Task) iter.next ();
      MyTaskWrapper wrapper = new MyTaskWrapper (task, impListener);
      // MUST be discarded if task is null! (see comments in MyTaskWrapper
      if (wrapper.getTask () != null) {
        taskCombo.addItem (wrapper);
        taskWrapperMap.put (task, wrapper);
      }
    }
    
    // Populate the combobox. isEmpty should have been reported elsewhere.
    if (taskWrapperMap.isEmpty())
      return;
    
    // finish initialization
    updateComponents ((MyTaskWrapper) taskCombo.getSelectedItem());

    // try to pre-select last importer used
    String lastImporter =
      Jawb.getPreferences().getPreference (Preferences.LAST_IMPORTER_KEY);
    if (lastImporter != null) {
      int hash = lastImporter.indexOf('#');
      if (hash > 0) {
        String taskName = lastImporter.substring (0, hash);
        String importerName = lastImporter.substring (hash+1);
        if (importerName.length () > 0) {
          Task task = null;
          Importer importer = null;
          iter = Jawb.getTasks ().iterator ();
          
          while (iter.hasNext () && importer == null) {
            task = (Task) iter.next();
            if (task.getName().equals (taskName)) {
              
              Importer[] imps = task.getImporters();
              for (int i=0; i<imps.length && importer == null; i++) {
                if (imps[i] != null && importerName.equals(imps[i].toString()))
                  importer = imps[i];
              }
            }
          }
          if (importer != null)
            setSelectedImporter (task, importer);
        }
      }
    }
  }

  /** update views when task is changed */
  private void updateComponents (MyTaskWrapper wrapper) {
    taskDescText.setText(wrapper.getDescription ());
    taskDescText.setCaretPosition (0);

    JComboBox impCombo = wrapper.getImporterCombo ();
    importerComboPane.removeAll ();
    importerComboPane.add (impCombo, BorderLayout.NORTH);
    importerComboPane.validate ();
    
    updateComponents ((Importer) impCombo.getSelectedItem ());
  }

  /** update views when importer is changed */
  private void updateComponents (Importer importer) {
    importerDescText.setText (importer.getDescription());
    importerDescText.setCaretPosition (0);
  }

  /***********************************************************************/
  /* External access */
  /***********************************************************************/
  
  /**
   * Returns the selected task
   */
  public Task getSelectedTask() {
    MyTaskWrapper wrapper = (MyTaskWrapper) taskCombo.getSelectedItem ();
    return wrapper.getTask ();
  }
  
  /**
   * Returns the selected task
   */
  public String getEncoding () {
    return encoding.getEncoding ();
  }

  /**
   * Returns the MIME Type accessory
  public MIMETypeAccessory getMIMETypeAccessory () {
    return mimeType;
  }
   */

  /**
   * Change the selected task externally.
   */
  public void setSelectedImporter (Task task, Importer importer) {
    // if either is null, the comboboxes simply won't change.

    // each fires an event which propogates
    taskCombo.setSelectedItem (taskWrapperMap.get (task));
    
    MyTaskWrapper wrapper = (MyTaskWrapper) taskCombo.getSelectedItem ();
    wrapper.getImporterCombo ().setSelectedItem (importer);
  }

  /**
   * Returns the selected importer if any are known. Also remembers importer
   * in user prefs.
   * @return Importer last selected when accessory was shown, or null if there
   *   are no tasks which have importers.
   */
  public Importer getSelectedImporter() {
    MyTaskWrapper wrapper = (MyTaskWrapper) taskCombo.getSelectedItem ();
    Importer importer = (wrapper==null)? null : wrapper.getSelectedImporter ();
    if (importer != null) {
      String taskName = getSelectedTask().getName ();
      Jawb.getPreferences().setPreference (Preferences.LAST_IMPORTER_KEY,
                                           taskName + "#" + importer);
    }
    return importer;
  }

  /***********************************************************************/
  /* Internal utility
  /***********************************************************************/

  /** Wrapper to show a tasks in combobox and maintain data */
  private static class MyTaskWrapper {
    
    Task task;
    // if task is null, or has no importers, this should never be set, and the
    // wrapper must not be used, as NPE's will abound.
    JComboBox importerCombo;
    
    MyTaskWrapper (Task t, ItemListener listener) {
      // validate importer count before initialization as flag
      // easy way of removing duplicates and null
      Importer[] imps = t.getImporters ();
      if (imps == null) {
        //System.err.println ("ImportAccessory: null imports!");
        return; // Whatever instantiated this wraper must DISCARD it!
      }
      LinkedHashSet importers = new LinkedHashSet (Arrays.asList (imps));
      importers.remove (null);
      if (importers.isEmpty ()) {
        //System.err.println ("ImportAccessory: empty imports!");
        return;
      }
      task = t;
      importerCombo = new JComboBox ();
      importerCombo.addItemListener (listener);
      
      Iterator iter = importers.iterator ();
      while (iter.hasNext ())
        importerCombo.addItem (iter.next ());
    }
    
    /** returns null if no valid importers */
    Task getTask () { return task; }
    String getDescription () {
      return task.getDescription();
    }
    JComboBox getImporterCombo () {
      return importerCombo;
    }
    String getImporterDescription () {
      return getSelectedImporter ().getDescription ();
    }
    Importer getSelectedImporter () {
      return (Importer) importerCombo.getSelectedItem ();
    }
    public String toString () {
      return task.getTitle ();
    }
  }
  /*
  public static void main (String[] args) {
    JFrame frame = new JFrame ("Import accessory test");
    List taskList = new LinkedList ();
    taskList.add (org.mitre.jawb.tasks.rdc.RDCTask.getInstance ());
    taskList.add (new org.mitre.jawb.tasks.phrase.PhraseTask ());
    
    ImportAccessory a = new ImportAccessory (taskList);
    frame.getContentPane().add (a);
    frame.setDefaultCloseOperation (frame.EXIT_ON_CLOSE);
    frame.pack ();
    frame.setVisible (true);
  }
  */
}

