
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.Exporter;

// testing
import javax.swing.JFrame;
import java.util.LinkedList;

/**
 * For showing a panel on the side of our filechooser that
 * displays information about the tasks and their Exporters
 */
public class ExportAccessory extends JPanel {

  /** Constant to indicat no known tasks have exporters */
  private static String NO_KNOWN_EXPORTERS = "No Known Exporters";
  
  private JComboBox taskCombo;      // lists the tasks
  private JComboBox currentExporterCombo; // current one base on taskCombo
  private JPanel exporterComboPane; // container for dynamic exporterCombo
  private JTextArea taskDescText; // text of task description
  private JTextArea exporterDescText;  // text of exporter description
  private HashMap taskMap; //task names to task objects
  private HashMap exporterComboMap; // task obj to ComboBox
    /**
     * Constructor. Creates a new Task Accessory, which will
     * contain :
     *  - A drop down menu of available tasks
     *  - A small box containing a description of each task
     *
     * @param taskList A list containing possible tasks
     */
    public ExportAccessory(java.util.List taskList) {

        setLayout(new BorderLayout(5,5));
        // Exporters are expected to implement toString w/ human readable name

        if (taskList.isEmpty()) {// heh
          setEnabled (false);
          return;
        }
        
        exporterComboPane = new JPanel (new BorderLayout());

        exporterDescText = new JTextArea (5,20);
	exporterDescText.setLineWrap(true);
	exporterDescText.setWrapStyleWord(true);
        exporterDescText.setBorder (BorderFactory.createEmptyBorder (2,2,2,2));
        exporterDescText.setEditable (false);
	JScrollPane exporterDescScroller = new JScrollPane(exporterDescText);

        JPanel exportPanel = new JPanel (new BorderLayout (5,5));
        exportPanel.add(exporterComboPane, BorderLayout.NORTH);
        exportPanel.add(exporterDescScroller, BorderLayout.CENTER);
        exportPanel.setBorder (BorderFactory.createCompoundBorder
                              (BorderFactory.createTitledBorder("Exporters"),
                               BorderFactory.createEmptyBorder (5,5,5,5)));

        this.add (exportPanel, BorderLayout.CENTER);


        // Finally: collect tasks into a map from title to task, ready titles
        // for combo create comboboxes for the exports of each task, and a map
        // from task to those comboboxes.
        exporterComboMap = new HashMap ();

        ItemListener expListener = new ItemListener () {
            public void itemStateChanged (ItemEvent evt) {
              if (evt.getStateChange() == ItemEvent.SELECTED) {
                // expComboes have Exporters themselves
                Exporter exp = (Exporter) evt.getItem();
                exporterDescText.setText (exp.getDescription());
                exporterDescText.setCaretPosition (0);
              }
            }};
        Iterator iter = taskList.iterator ();
        while (iter.hasNext()) {
          Task task = (Task) iter.next ();
          JComboBox exporterCombo = new JComboBox ();
          Exporter[] exp = task.getExporters ();
          if (exp != null) {
            for (int i=0; i<exp.length; i++) {
              // hard core sanity check
              if (exp[i] != null) {
                exporterCombo.addItem (exp[i]);
              } else {
                System.err.println (task.getName()+
                                    " has invalid list of exporters: item "+i);
              }
            }

            // now we know the task has exporters and we've verified those
            if (exporterCombo.getItemCount() > 0) {
              exporterCombo.addItemListener (expListener);
              exporterComboMap.put (task, exporterCombo);
            }
          } // end (exp != null)
        }
        // prepare for a stripped down system, and and init the first task
        if (! taskList.isEmpty ()) {
          Task t = (Task)taskList.get (0);
          setTask (t);
        }
    }

  /** update views when task is changed */
  public void setTask (Task t) {
    JComboBox expCombo = (JComboBox) exporterComboMap.get (t);
    if (expCombo != null) {
      Exporter exp = (Exporter) expCombo.getSelectedItem ();
      if (currentExporterCombo != null) // initially it will be null
        exporterComboPane.remove (currentExporterCombo);
      exporterComboPane.add (expCombo, BorderLayout.NORTH);
      exporterDescText.setText (exp.getDescription());
      exporterDescText.setCaretPosition (0);
    }
    currentExporterCombo = expCombo;
  }

  /***********************************************************************/
  /* External access */
  /***********************************************************************/
  
  /**
   * Returns the selected exporter if any are known.
   * @return Exporter last selected when accessory was shown, or null if there
   *   are no tasks which have exporters.
   */
  public Exporter getSelectedExporter() {
    if (currentExporterCombo == null)
      return null;
    return (Exporter) currentExporterCombo.getSelectedItem();
  }
  /*
  public static void main (String[] args) {
    JFrame frame = new JFrame ("Export accessory test");
    List taskList = new LinkedList ();
    taskList.add (org.mitre.jawb.tasks.rdc.RDCTask.getInstance ());
    taskList.add (new org.mitre.jawb.tasks.phrase.PhraseTask ());
    
    ExportAccessory a = new ExportAccessory (taskList);
    frame.getContentPane().add (a);
    frame.setDefaultCloseOperation (frame.EXIT_ON_CLOSE);
    frame.pack ();
    frame.setVisible (true);
  }
  */
}
