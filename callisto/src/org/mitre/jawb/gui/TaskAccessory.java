
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
import java.nio.charset.Charset;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.tasks.Task;

/**
 * GUI Component for the side of our filechooser that
 * displays information about the tasks and a task selection
 * mechanism. Contains:
 * <ul>
 *   <li>A "Please Select a Task" label</li>
 *   <li>A drop down menu of available tasks</li>
 *   <li>A small box containing a description of each task</li>
 * </ul>
 *
 * @see GUIUtils
 * @author <a href="mailto:laurel@mitre.org">Laurel Riek</a>
 * @version 1.0
 */
public class TaskAccessory extends JPanel {

  static final String NO_TASK_NAME = "<No Tasks specified>";
  static final String NO_TASK_DESC = "Select a task from the menu.";
  
  private JComboBox taskCombo;   //a combobox that lists the tasks
  private JTextArea descTextArea;  //desc for the tasks
  private HashMap taskWrapperMap; // task to its wrapper for external setting
  private EncodingAccessory encoding;
  private MIMETypeAccessory mimeType;
  //TODO: private PreAnnotateAccessory preAnnotate;
  
  /**
   * Create a new TaskAccesory with the specified tasks available to the
   * user.
   *
   * @param taskList A list containing possible tasks
   */
  public TaskAccessory(List taskList) {
    super (new BorderLayout (5,5));

    taskWrapperMap = new HashMap ();

    // Panel for task selection
    JPanel taskPanel = new JPanel (new BorderLayout (5,5));

    taskCombo = new JComboBox();
    taskCombo.addItemListener (new ItemListener () {
        public void itemStateChanged(ItemEvent evt) {
          if (evt.getStateChange() == ItemEvent.SELECTED) {
            updateComponents ((MyTaskWrapper) evt.getItem());
          }}});

    taskPanel.add(taskCombo, BorderLayout.NORTH);

    // Panel for description of current task
    descTextArea = new JTextArea(5,20);
    descTextArea.setLineWrap(true);
    descTextArea.setWrapStyleWord(true);
    descTextArea.setBorder (BorderFactory.createEmptyBorder (2,2,2,2));
    descTextArea.setEditable (false);
    JScrollPane descScrollPane = new JScrollPane(descTextArea);
        
    taskPanel.add(descScrollPane);
    taskPanel.setBorder (BorderFactory.createCompoundBorder
                         (BorderFactory.createTitledBorder ("Task"),
                          BorderFactory.createEmptyBorder (5,5,5,5)));

    encoding = new EncodingAccessory ();
    mimeType = new MIMETypeAccessory ();
    //TODO: preAnnotate = new PreAnnotateAccessory ();
        
    JPanel bottom = new JPanel (new BorderLayout ());
    //TODO: bottom.add (preAnnotate, BorderLayout.NORTH);
    bottom.add (mimeType, BorderLayout.CENTER);
    bottom.add (encoding, BorderLayout.SOUTH);

    this.add (taskPanel, BorderLayout.CENTER);
    this.add (bottom, BorderLayout.SOUTH);

    // try to pre-select last task used
    Task lastTask = null;
    String lastTaskName =
      Jawb.getPreferences().getPreference (Preferences.LAST_TASK_KEY);
    
    // create a list Sorted on title
    List sortedList = new LinkedList(taskList);
    Collections.sort(sortedList, new MyTaskSorter());
    
    // Populate the combobox.
    Iterator iter = sortedList.iterator ();
    while (iter.hasNext ()) { // assumes all are non-null
      Task task = (Task) iter.next ();
      MyTaskWrapper wrapper = new MyTaskWrapper (task);
      taskCombo.addItem (wrapper);
      taskWrapperMap.put (task, wrapper);
      if (task.getName().equals (lastTaskName))
	  lastTask = task;
    }

    // empty tasklist reported elsewhere anywy. null not visible by default
    taskWrapperMap.put(null, new MyTaskWrapper (null));
    if (lastTask == null)
      taskCombo.addItem(taskWrapperMap.get(null));
    setSelectedTask (lastTask);
      
    // finish initialization
    updateComponents ((MyTaskWrapper) taskCombo.getSelectedItem());
  }
  
  /**
   * Change the selected task externally.
   */
  public void setSelectedTask (Task task) {
    // if null, the combobox simply won't change.
    // fires an event which propogates
    taskCombo.setSelectedItem (taskWrapperMap.get (task)); 
  }

  /**
   * Returns the selected task
   */
  public Task getSelectedTask() {
    MyTaskWrapper wrapper = (MyTaskWrapper) taskCombo.getSelectedItem ();
    Task task = wrapper.getTask ();
    if (task != null) {
      Jawb.getPreferences().setPreference (Preferences.LAST_TASK_KEY,
                                           task.getName ());
    }
    return task;
  }

  /**
   * Returns the selected task
   */
  public String getEncoding () {
    return encoding.getEncoding ();
  }
  /*//TODO: 
  public PreAnnotateAccessory getPreAnnotateAccessory () {
    return preAnnotate;
  }
  */
  /**
   * Returns the MIME Type accessory
   */
  public MIMETypeAccessory getMIMETypeAccessory () {
    return mimeType;
  }

  /**
   * Propogate changes for the selected item in the combobox.
   */
  private void updateComponents (MyTaskWrapper wrapper) {
    
      descTextArea.setText (wrapper.getDescription ());
      descTextArea.setCaretPosition (0);
      
      //TODO: preAnnotate.setTask(wrapper.getTask());
      
  }
  
  /** Wrapper to show a tasks Title in combobox */
  private static class MyTaskWrapper {
    Task task;
    MyTaskWrapper (Task t) { task = t; }
    Task getTask () { return task; }
    String getDescription () {
      return (task==null) ? NO_TASK_DESC : task.getDescription();
    }
    public String toString () {
      return (task==null) ? NO_TASK_NAME : task.getTitle ();
    }
  }

  private static class MyTaskSorter implements Comparator {
    public int compare(Object o1, Object o2) {
      Task t1 = (Task)o1;
      Task t2 = (Task)o2;
      return t1.getTitle().compareTo(t2.getTitle());
    }
  }
}
