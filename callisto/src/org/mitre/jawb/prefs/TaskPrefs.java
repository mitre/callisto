
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

package org.mitre.jawb.prefs;

import java.awt.*;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.tasks.Task;

/**
 * Sets up the Task Preferences Pane. Currently this only includes
 * Highlighting Prefs, but some day might have other task prefs.
 *
 * @author <a href="mailto:laurel@mitre.org">Laurel D. Riek</a>
 */
public class TaskPrefs extends AbstractPreferencePane {

  private ColorTable.ColorTableModel colorModel;
  private ColorTable colorTable;
  private Task task;
  private PreferenceItem taskPrefItem;
  private static int DEBUG = 0;
  private JCheckBox unknownVisible;
  private String unknownVisibleKey;
  private JCheckBox selectInvisible;
  private String selectInvisibleKey;

  /**
   * Constructor
   *
   * @param task the actual task name
   */
  public TaskPrefs(Task task) {
    super (task.getTitle ());
    this.task = task;
        
    JPanel panel = new JPanel (new BorderLayout());
    panel.add (BorderLayout.NORTH, new JLabel ("Task Highlighting"));
    //  jawb.getPreference ("options.styles.colors")));
    panel.add (BorderLayout.CENTER, createHighlightEditor ());
    panel.add (BorderLayout.SOUTH, createHighlightAllButtons());
    addComponent (panel, GridBagConstraints.HORIZONTAL);

    addComponent (unknownVisible = new JCheckBox
      ("Display Unknown highlights?"));
    // cache this so it isn't repeatedly computed
    unknownVisibleKey = "task."+task.getName()+"."+
      Preferences.UNKNOWN_HIGHLIGHT_KEY+
      ".visible";

    addComponent (selectInvisible = new JCheckBox
                  ("Allow Selection of Non-visible Annotations?"));
    selectInvisibleKey = "task."+task.getName()+".selectinvisible";


    // add task define preferences
    taskPrefItem = task.getPreferenceItem ();
    if (taskPrefItem != null) {
      addComponent (taskPrefItem.getPreferenceComponent (),
                    GridBagConstraints.HORIZONTAL);
    }
  }

  public void save() {
    Preferences prefs = Jawb.getPreferences ();
    colorModel.save ();
    if (taskPrefItem != null)
      taskPrefItem.save ();
    prefs.setPreference (unknownVisibleKey,unknownVisible.isSelected ());
    prefs.setPreference (selectInvisibleKey, selectInvisible.isSelected());
  }

  public void reset() {
    Preferences prefs = Jawb.getPreferences ();
    colorModel.reset ();
    if (taskPrefItem != null)
      taskPrefItem.reset ();
    unknownVisible.setSelected (prefs.getBoolean (unknownVisibleKey, true));
    selectInvisible.setSelected (prefs.getBoolean (selectInvisibleKey, false));
  }   

  public Task getTask () {
    return task;
  }
    
  private JScrollPane createHighlightEditor () {
    colorModel = createHighlightModel ();
    colorTable = new ColorTable (colorModel);
    JScrollPane scroller = 
      new JScrollPane (colorTable,
                       JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                       JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    Dimension d = scroller.getPreferredSize ();
    // scroll panes tend to get out of hand
    d.width = Math.min (d.width, 200);
    d.height = Math.min (d.height, 100);
    scroller.setPreferredSize (d);
    return scroller;
  }

  private JPanel createHighlightAllButtons () {
    JButton allButton = new JButton(new HighlightsVisibleAction("All", true));
    JButton noneButton = new JButton(new HighlightsVisibleAction("None", false));
    // make 'em smaller
    Border b = allButton.getBorder();
    if (b instanceof CompoundBorder) {
      CompoundBorder c = (CompoundBorder) b;
      Border empty = BorderFactory.createEmptyBorder(0,2,0,2);
      c = BorderFactory.createCompoundBorder(c.getOutsideBorder(), empty);
      allButton.setBorder(c);
      
      c = (CompoundBorder) noneButton.getBorder();
      empty = BorderFactory.createEmptyBorder(0,2,0,2);
      c = BorderFactory.createCompoundBorder(c.getOutsideBorder(), empty);
      noneButton.setBorder(c);
    }
    
    Font font = UIManager.getFont("TextField.font");
    allButton.setFont(font);
    noneButton.setFont(font);

    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.add(allButton);
    panel.add(noneButton);

    return panel;
  }

  private ColorTable.ColorTableModel createHighlightModel () {
    ColorTable.ColorTableModel model = 
      new ColorTable.ColorTableModel(false);
        
    // for anything unrecognized
    model.addColorChoice ("Unknown", Preferences.UNKNOWN_HIGHLIGHT_KEY);

    // next, iterate through the tasks highlight keys.
    Set keys = task.getHighlightKeys ();
    String namespace = "task."+task.getName ()+".";
    Iterator keyIter = keys.iterator ();
    while (keyIter.hasNext ()) {
      String simpleName = (String)keyIter.next();
      model.addColorChoice(simpleName, namespace+simpleName );
    }
    return model;
  }

    /*
     * Class to set visibility en-mass
     */
    private class HighlightsVisibleAction extends AbstractAction {
      boolean visible;
      public HighlightsVisibleAction(String name, boolean v) {
        super(name);
        visible = v;
      }
      public void actionPerformed(ActionEvent e) {
        colorModel.setAllVisible(visible);
      }
    }

}//end class TaskPrefs
