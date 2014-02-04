
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
import java.awt.event.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.gui.*;
import org.mitre.jawb.tasks.Task;

/**
 * Dialog to display heirarchal preferences.
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @author <a href="mailto:laurel@mitre.org">Laurel D. Riek</a>
 *
 * @version 1.0
 */
public class PreferenceDialog extends JDialog implements TreeSelectionListener {
  private JTree prefsTree;
  private JLabel currentLabel;
  /** a single scrollpane to display the PreferenceItems Selected */
  private JScrollPane preferenceScroller;
  /** Map of preference Items to the component to display (not simply
   * PreferenceItem.getPreferenceComponent()) so that selecting an item in
   * tree will display it. */
  private Map preferenceComponentMap;
  private PreferenceGroup jawbGroup, taskGroup;

  private Action okAction = null;
  private Action cancelAction = null;
  private Action applyAction = null;

  /** Prefs backing this dialog */
  private Preferences prefs;

  /**
   * Preference dialog which uses the specified preference object. This
   * dialog, and it's descendants will retrieve values from the specified
   * object, NOT look to the global Jawb.getPreferences() method to get them,
   * although it should be the same thing (intended to make this a slightly
   * more general class).
   */
  public PreferenceDialog (JFrame owner, Preferences prefs) {
    super(owner, "Preferences", true);
    this.prefs = prefs;
      
    JPanel cp = (JPanel) getContentPane ();
    cp.setLayout (new BorderLayout());
    cp.setBorder (BorderFactory.createEmptyBorder(4, 4, 4, 4));
      
    JPanel stage = new JPanel(new BorderLayout(4, 8));
    stage.setBorder (BorderFactory.createEmptyBorder(4, 4, 0, 4));
    cp.add (stage, BorderLayout.CENTER);

    // currentLabel displays the path of the currently selected
    // PreferencePane at the top of the stage area
    currentLabel = new JLabel ();
    currentLabel.setHorizontalAlignment (JLabel.LEFT);
    currentLabel.setBorder (BorderFactory.createMatteBorder(0, 0, 1, 0,
                                                            Color.black));
    stage.add (currentLabel, BorderLayout.NORTH);

    preferenceScroller = new JScrollPane ();
    preferenceComponentMap = new HashMap ();
    stage.add(preferenceScroller, BorderLayout.CENTER);

    prefsTree = new JTree (createPrefsTreeModel ());
    prefsTree.setCellRenderer (new PreferenceItemRenderer ());
    prefsTree.putClientProperty ("JTree.lineStyle", "Angled");
    prefsTree.setShowsRootHandles (true);
    prefsTree.setRootVisible (false);
    
    TreeSelectionModel treeModel = prefsTree.getSelectionModel();
    treeModel.setSelectionMode (TreeSelectionModel.SINGLE_TREE_SELECTION);
    
    // register as Listener before selecting first panel for startup
    prefsTree.getSelectionModel ().addTreeSelectionListener (this);

    // try to compute TreePath to first preferences Panel
    if (jawbGroup.getMemberCount () > 0) {
      TreePath firstNode =
        new TreePath(new Object[] {prefsTree.getModel().getRoot(),
                                   jawbGroup, jawbGroup.getMember (0)});
      prefsTree.setSelectionPath (firstNode);
    }
    prefsTree.setBorder (BorderFactory.createEmptyBorder(0,0,0,4));
    // register the MouseHandler to colapase groups on single click
    prefsTree.addMouseListener (new TreeMouseHandler ());

    JScrollPane sp = new JScrollPane (prefsTree,
                                      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    cp.add (sp, BorderLayout.WEST);

    // Finally, specify a default size.  don't like doing this, but since
    // I took out the 'cardLayout', the initial size is only big enough for
    // what ever PreferenceItem is displayed first.
    Dimension d = new Dimension (480, 360);
    cp.setPreferredSize (d);
    
    /*** All things actionable ***/
    initActions ();

    // Buttons
    JPanel buttons = new JPanel(new FlowLayout (FlowLayout.RIGHT));

    JButton ok = new JButton ("OK");
    ok.setMnemonic ('O');
    ok.addActionListener (okAction);
    buttons.add (ok);
    getRootPane ().setDefaultButton (ok); // highlight

    JButton cancel = new JButton ("Cancel");
    cancel.setMnemonic ('C');
    cancel.addActionListener (cancelAction);
    buttons.add (cancel);

    JButton apply = new JButton ("Apply");
    apply.setMnemonic ('A');
    apply.addActionListener (applyAction);
    buttons.add (apply);

    cp.add (buttons, BorderLayout.SOUTH);

    // Keystrokes
    
    ActionMap actionMap = cp.getActionMap ();
    actionMap.put ("ok", okAction);
    actionMap.put ("cancel", cancelAction);

    InputMap inputMap = cp.getInputMap (cp.WHEN_IN_FOCUSED_WINDOW);
    inputMap.put (KeyStroke.getKeyStroke (KeyEvent.VK_ENTER,0), "ok");
    inputMap.put (KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE,0),"cancel");

    // Window Closing
    addWindowListener (new WindowAdapter() {
        public void windowClosing (WindowEvent e) {
          cancel ();
        }
      });
    
    pack ();
  }
    
  /***********************************************************************/
  /* Actions! */
  /***********************************************************************/

  /** Delegats to actions below */
  private void initActions () {
    okAction = new AbstractAction ("ok") {
        public void actionPerformed (ActionEvent e) {
          ok ();
        }};
    cancelAction = new AbstractAction ("cancel") {
        public void actionPerformed (ActionEvent e) {
          cancel ();
        }};
    applyAction = new AbstractAction ("apply") {
        public void actionPerformed (ActionEvent e) {
          apply ();
        }};
  }
  
  private void ok () {
    apply ();
    setVisible (false);
    Jawb.storePreferences ();
  }
  
  private void cancel () {
    PreferenceTreeModel m = (PreferenceTreeModel) prefsTree.getModel ();
    ((PreferenceGroup) m.getRoot ()).reset ();
    setVisible (false);
  }
  
  private void apply () {
    PreferenceTreeModel m = (PreferenceTreeModel) prefsTree.getModel ();
    ((PreferenceGroup) m.getRoot ()).save ();
  }
  
  /***********************************************************************/
  /* Adding Preference Items */
  /***********************************************************************/

  /**
   * Tasks can add task specific preference panes, or groups of panes here.
   */
  public void addPreferenceItem (PreferenceItem item) {
    if (item instanceof PreferenceGroup)
      addPreferenceGroup ((PreferenceGroup)item, taskGroup);
    else
      addPreferencePane ((PreferenceItem)item, taskGroup);
  }

  private void addPreferenceGroup (PreferenceGroup group,
                                   PreferenceGroup grandParent) {
    Iterator iter = group.getMembers().iterator ();
    while (iter.hasNext ()) {
      Object item = iter.next ();
      if (item instanceof PreferenceGroup)
        addPreferenceGroup ((PreferenceGroup)item, group);
      else
        addPreferencePane ((PreferenceItem)item, group);
    }

    grandParent.add (group);
  }

  private void addPreferencePane (PreferenceItem item,
                                  PreferenceGroup group) {
    if (item.getName () == null) {
      System.err.println ("Cannot add PreferenceItem. Name must be set");
      return;
    }
    JPanel p = new JPanel (new BorderLayout ());
    p.add (item.getPreferenceComponent (), BorderLayout.NORTH);
    p.setBorder (BorderFactory.createEmptyBorder(5,5,5,5));
    
    // map the components name this panel for display in the
    // preferenceScroller. did this because cardLayout does not work well when
    // adding and removing Components.
    preferenceComponentMap.put (item.getName (), p);

    group.add (item);
  }
  
  private void removePreferencePane (PreferenceItem item,
                                     PreferenceGroup group) {
    // must be in the specified group
    if (group.getMemberIndex (item) != -1) {
      
      // set to a different view if the one being removed is current. Note:
      // not the same as item.getPreferenceComponent().
      Component comp = (Component)preferenceComponentMap.get (item.getName ());
      if (preferenceScroller.getViewport().getView () == comp) {
        
        // try to compute TreePath to first preferences Panel
        if (jawbGroup.getMemberCount () > 0) {
          TreePath firstNode =
            new TreePath(new Object[] {prefsTree.getModel().getRoot(),
                                       jawbGroup, jawbGroup.getMember (0)});
          // this will cause the tree model to change the viewports view
          prefsTree.setSelectionPath (firstNode);
          
        } else {
          throw new RuntimeException ("This should be an asert");
        }
      }
      // remove it from the group and map
      group.remove (item);
      preferenceComponentMap.remove (item.getName ());
    }
  }

  /**
   * Called by the tree listener, when a click on a preference item is
   * noticed.
   */
  private void displayPreferencePane (String label, String preferenceItemName) {
    Component comp = (Component) preferenceComponentMap.get (preferenceItemName);
    currentLabel.setText (label);
    preferenceScroller.getViewport ().setView (comp);
  }
    
  /**************************
   * Task add/removal methods
   **************************/
  /**
   * Adds a task to the preferences window. 
   *
   * @param task task who's preferences to add
   */
  public void addTask(Task task) {
    addPreferencePane(new TaskPrefs(task), taskGroup);
  }

  /**
   * Removes a task to the preferences window. 
   * @param task the task who's panel should be removed
   *
   * NOTES: This needs testing.
   */
  public void removeTask(Task task) {
    //first, figure out the taskitem with this name
    Iterator iter = taskGroup.getMembers().iterator ();
    while (iter.hasNext ()) {
      Object pi = iter.next ();
      if (pi instanceof TaskPrefs &&
          ((TaskPrefs) pi).getTask ().equals (task)) {
        //houston we have a match. remove it from the taskGroup
        removePreferencePane ( (TaskPrefs) pi, taskGroup);
      }
    }
  }

  /***********************************************************************/
  /* Tree Implementaion: listener, selection & tree model, actions, etc.. */
  /***********************************************************************/
    
  /** Create tree model and add the jawb preference panes */
  private PreferenceTreeModel createPrefsTreeModel () {
    PreferenceTreeModel prefsTreeModel = new PreferenceTreeModel();
    PreferenceGroup rootGroup = (PreferenceGroup) prefsTreeModel.getRoot();
        
    jawbGroup = new PreferenceGroup("General");
        
    addPreferencePane (new GeneralPrefs (), jawbGroup);
    addPreferencePane (new AdvancedPrefs (), jawbGroup);
    addPreferencePane (new FontPrefs (), jawbGroup);
        
    /**
     * I modified this to support each task having it's own panel and
     * respective preferences. {link TaskPrefs.java}. I also modified
     * it to have the inital list of tasks be generated automatically.
     *
     * @see TaskPrefs.java
     * @version 18 Feb 2003
     * @author <a href="mailto:laurel@mitre.org">Laurel D. Riek</a>
     */
        
    taskGroup = new PreferenceGroup ("Tasks");  //make a new task group

    //let's get all the tasks and iterate through, populating
    //our task group
    java.util.List tasks = Jawb.getTasks ();
    Iterator iter = tasks.iterator ();
    while (iter.hasNext ()) {
      Task task = (Task) iter.next ();
      addPreferencePane(new TaskPrefs(task), taskGroup);
    }      
        
    //now add the pref groups
    addPreferenceGroup (jawbGroup, rootGroup);
    addPreferenceGroup (taskGroup, rootGroup);
        
    return prefsTreeModel;
  }


  /** Change preference Panel when TreeSelectionListener fires */
  public void valueChanged(TreeSelectionEvent evt) {
    TreePath path = evt.getPath ();
    if (path == null ||
        ! (path.getLastPathComponent () instanceof PreferenceItem))
      return;

    Object[] nodes = path.getPath ();
    StringBuffer buf = new StringBuffer ();
    int lastIdx = nodes.length - 1;

    for (int i=(prefsTree.isRootVisible () ? 0 : 1); i<=lastIdx; i++) {
      buf.append (nodes[i].toString ());
      if (i != lastIdx)
        buf.append (": ");
    }
    String name = ((PreferenceItem)nodes[lastIdx]).getName ();

    displayPreferencePane (buf.toString (), name);
  }

  /** Allows single clicks to expand */
  class TreeMouseHandler extends MouseAdapter {
    public void mouseClicked(MouseEvent evt) {
      TreePath path = prefsTree.getPathForLocation(evt.getX(), evt.getY());
      if (path == null)
        return;

      Object node = path.getLastPathComponent();

      if (node instanceof PreferenceGroup) {
        if (prefsTree.isCollapsed(path)) {
          prefsTree.expandPath(path);
        } else {
          prefsTree.collapsePath(path);
        }
      }
    }
  }// TreeMouseHandler
  
  /** Render preference items in the tree */
  class PreferenceItemRenderer extends JLabel implements TreeCellRenderer {
    private Border noFocusBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
    private Border focusBorder =
      BorderFactory.createLineBorder(UIManager.getColor("Tree.selectionBorderColor"));
    
    private Font paneFont;
    private Font groupFont;

    public PreferenceItemRenderer() {
      setOpaque(true);

      paneFont = UIManager.getFont("Tree.font");
      groupFont = paneFont.deriveFont (Font.BOLD);
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean selected,
                                                  boolean expanded,
                                                  boolean leaf, int row,
                                                  boolean hasFocus) {
      if (selected) {
        this.setBackground(UIManager.getColor("Tree.selectionBackground"));
        this.setForeground(UIManager.getColor("Tree.selectionForeground"));

      } else {
        this.setBackground(tree.getBackground());
        this.setForeground(tree.getForeground());
      }
      
      if (value instanceof PreferenceGroup)
        this.setFont(groupFont);
      else
        this.setFont(paneFont);
      
      setText (value.toString ());
      
      setBorder(hasFocus ? focusBorder : noFocusBorder);
      return this;
    }
  }// PreferenceItemRenderer

  class PreferenceTreeModel implements TreeModel {
    
    private PreferenceGroup root = new PreferenceGroup("root");
    private EventListenerList listenerList = new EventListenerList ();

    public void addTreeModelListener (TreeModelListener l) {
      listenerList.add (TreeModelListener.class, l);
    }

    public void removeTreeModelListener (TreeModelListener l) {
      listenerList.remove (TreeModelListener.class, l);
    }

    public Object getChild (Object parent, int index) {
      if (parent instanceof PreferenceGroup) {
        return ((PreferenceGroup) parent).getMember(index);
      } else {
        return null;
      }
    }

    public int getChildCount (Object parent) {
      if (parent instanceof PreferenceGroup) {
        return ((PreferenceGroup) parent).getMemberCount();
        
      } else {
        return 0;
      }
    }

    public int getIndexOfChild (Object parent, Object child) {
      if (parent instanceof PreferenceGroup) {
        return ((PreferenceGroup) parent).getMemberIndex ((PreferenceItem)child);
        
      } else {
        return -1;
      }
    }

    public Object getRoot () {
      return root;
    }

    public boolean isLeaf (Object node) {
      return ! (node instanceof PreferenceGroup);
    }

    public void valueForPathChanged (TreePath path, Object newValue) {
      // this model may not be changed by the TableCellEditor
    }

    protected void fireNodesChanged (Object source, Object[] path,
                                     int[] childIndices,
                                     Object[] children) {
      Object[] listeners = listenerList.getListenerList();

      TreeModelEvent modelEvent = null;
      for (int i = listeners.length - 2; i >= 0; i -= 2) {
        if (listeners[i] != TreeModelListener.class)
          continue;

        if (modelEvent == null)
          modelEvent = new TreeModelEvent (source, path, childIndices, children);

        ((TreeModelListener) listeners[i + 1]).treeNodesChanged (modelEvent);
      }
    }

    protected void fireNodesInserted (Object source, Object[] path,
                                      int[] childIndices,
                                      Object[] children) {
      Object[] listeners = listenerList.getListenerList();

      TreeModelEvent modelEvent = null;
      for (int i = listeners.length - 2; i >= 0; i -= 2) {
        if (listeners[i] != TreeModelListener.class)
          continue;

        if (modelEvent == null)
          modelEvent = new TreeModelEvent (source, path, childIndices, children);

        ((TreeModelListener)listeners[i + 1]).treeNodesInserted(modelEvent);
      }
    }

    protected void fireNodesRemoved (Object source, Object[] path,
                                     int[] childIndices,
                                     Object[] children) {
      Object[] listeners = listenerList.getListenerList();

      TreeModelEvent modelEvent = null;
      for (int i = listeners.length - 2; i >= 0; i -= 2) {
        if (listeners[i] != TreeModelListener.class)
          continue;

        if (modelEvent == null)
          modelEvent = new TreeModelEvent(source, path, childIndices, children);

        ((TreeModelListener) listeners[i + 1]).treeNodesRemoved(modelEvent);
      }
    }

    protected void fireTreeStructureChanged (Object source, Object[] path,
                                             int[] childIndices,
                                             Object[] children) {
      Object[] listeners = listenerList.getListenerList();

      TreeModelEvent modelEvent = null;
      for (int i = listeners.length - 2; i >= 0; i -= 2) {
        if (listeners[i] != TreeModelListener.class)
          continue;

        if (modelEvent == null)
          modelEvent = new TreeModelEvent(source, path, childIndices, children);

        ((TreeModelListener) listeners[i + 1]).treeStructureChanged(modelEvent);
      }
    }
  }// PREFERENCETREEMODEL

  public static void main (String[] args) {
    String title = "Preference Test";
    JFrame frame = new JFrame (title);
    JLabel label = new JLabel (title);

    frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
    frame.getContentPane ().add (label);
    frame.pack ();
    GUIUtils.centerComponent (frame);
    frame.setVisible (true);

    JDialog dialog = new PreferenceDialog (frame, new Preferences (null));
    dialog.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
    
    dialog.setVisible (true);
  }

}// PreferenceDialog
