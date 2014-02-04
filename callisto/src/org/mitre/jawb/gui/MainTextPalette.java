
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

import java.awt.BorderLayout;
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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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

import gov.nist.atlas.type.AnnotationType;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.NamedExtentRegions;
import org.mitre.jawb.atlas.TextExtentRegion;
import org.mitre.jawb.gui.ActionModel.ActionCollection;
import org.mitre.jawb.gui.ActionModel.ActionDelegate;
import org.mitre.jawb.gui.ActionModel.ActionGroup;
import org.mitre.jawb.gui.ActionModel.ActionProxy;
import org.mitre.jawb.prefs.ColorSpec;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;
import org.mitre.jawb.tasks.ToocaanToolKit;
import org.mitre.jawb.swing.SetModel;
import org.mitre.jawb.swing.event.SetDataListener;
import org.mitre.jawb.swing.event.SetDataEvent;

/**
 * JPanel which displays actions as buttons in a vertical pane, but also acts
 * as a mouse listener to fire a default action when triggered by a middle
 * mouse click.  Use the {@link #createDialog} method to create a dialog on a
 * frame. Call setJawbDocument to configure the actions for the current
 * document, and add this component as both a MouseListener and a
 * CaretListener to the JTextComponent in question. Actions will be updated as
 * to the swiped texted and selected annotations (of the document). If Actions
 * from the documents task have the <code>Jawb.HIGHLIGHT_COLOR</code> values
 * set, it will be used as the background for that actions button.
 *
 * TODO: This has to stay synchronized with what the MainTextPane shows in
 * drop down menu: if we're each tracking separately it could get out of
 * sync.<p>
 *
 * Package private since it's only intended purpose is in MainTextPane
 *
 * @author <a href="mailto:red@mitre.org"></a>
 * @version 1.0
 */
class MainTextPalette extends JPanel
  implements MouseListener, ActionExecutionListener, JawbComponent {

  private static final int DEBUG = 0;
  
  /** key to store the default action in preferences. */
  private String defaultActionKey;
  /** From our Task. */
  private TaskToolKit kit;
  
  /** current document */
  private JawbDocument document = null;
  
  /** caches the gui widgets for each action and helps us track. The keys
   * in this table are the action proxies, and the entries are ActionData elements. */
  private Map actionToDataMap = new HashMap ();

  /** track the 'default' with a buttongroup */
  private ButtonGroup buttonGroup = new ButtonGroup ();
  /** track the 'default' action */
  private Action defaultAction = null;
  /** place for the actual buttons, etc. */
  private Box mainPane;

  /** List of all the actions in the panel, for easy removal of all */
  private LinkedList<Action> paletteActions;

  /** separated used repeatedly */
  private Separator modifierSep = new MainTextPalette.Separator();

  /** The model that maintains our slection from MainTextPane */
  private SetModel selectedAnnotModel;
  
  private ActionModel actionModel;
  
  private Hashtable actionToCollection = new Hashtable();
     
  /**
   *
   * @param kit the toolkit for the frame our MainTextPane is a member of
   * @param model the MainTextPane's selected Lexicals Set, which we will
   * listen to to know which annotations are currently selected
   */ 
  public MainTextPalette (TaskToolKit kit, SetModel annotModel) {
    this.kit = kit;

    paletteActions = new LinkedList<Action>();
    actionModel = kit.getActionModel();
    if (DEBUG > 0)
      System.err.println("MTPalette.constr got actionModel: " + actionModel);
    selectedAnnotModel = annotModel;
    
    defaultActionKey = "windows."+kit.getTask().getName()+
      ".mainTextPalette.default".intern ();
    
    setLayout (new BorderLayout ());
    // keep it from stretching the buttons.
    mainPane = new Box(BoxLayout.Y_AXIS);
    JScrollPane scrollPane = new JScrollPane(mainPane,
       ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    add (scrollPane, BorderLayout.CENTER);
    setBorder (BorderFactory.createCompoundBorder
               (BorderFactory.createEtchedBorder (),
                BorderFactory.createEmptyBorder (4,3,4,3)));

    // create the 'action panels' with toggle buttons next to 'em
    //  -- populate the panel with the appropriate actions
    populatePalette();

    // Now that it's set up, add the listener, and pray that nothing's changed
    // since the actions were originally accessed.
    actionModel.addActionExecutionListener(this);
  }

  private void populatePalette() {
    
    // find the default command
    Preferences prefs = Jawb.getPreferences ();
    String defaultCmd = prefs.getPreference (defaultActionKey);

    // Which actions do we want? First, we want all the text actions,
    // and we want to add them all. And, because we need the
    // action data to be recovered at random times, we need a hash table.
    ActionCollection textActions = actionModel.getGroupedTextActions();
    Iterator actionIter = textActions.iterator();
    while (actionIter.hasNext ()) {
      Action act = (Action)actionIter.next();
      actionToCollection.put(act, textActions);
      addAction (act, defaultCmd);
    }
    
    // Next up, we want to add the annotation actions. But NOT
    // all of them; just the ones which are for TextExtentRegion or NamedExtentRegions,
    // and the general ones. 
    
    ActionCollection annotationActions = actionModel.getGroupedAnnotationActions();
    actionIter = annotationActions.iterator();
    
    //  First, we have to sort the actions.
    
    Map typesToActSets = new LinkedHashMap();
    Set generalActSet = null;
    
    // And now, we set up a list of sets, and loop through it.
    // There are probably easier ways to do this, but I'm not going to
    // worry about it.
    
    Vector allSets = new Vector();
    
    // These elements might be grouped. If they are, we need to get
    // a representative element of the group, which might not be
    // the parent, if there isn't any parent. The groups might be heterogeneous
    // in their annotation type, but not in their conceptual type;
    // that was checked when the groups were created.
    
    Map actionsToTypes = new LinkedHashMap();
    
    while (actionIter.hasNext()) {
      Action actProxy = (Action) actionIter.next();
      actionToCollection.put(actProxy, annotationActions);
      AnnotationType type = annotationTypeForAction(actProxy, annotationActions, null);
      actionsToTypes.put(actProxy, type);
      
      if (type == null) {
        if (generalActSet == null) {
          generalActSet = new LinkedHashSet();
          allSets.insertElementAt(generalActSet, 0);
        }
        generalActSet.add(actProxy);
      } else {
        
        // find the runtime class type
        Class annotClass = kit.getTask().getAnnotationClass(type);
        
        // The text palette will restrict itself to just those 
        // annotation types which are TextExtentRegion or NamedExtentRegion,
        // but that restriction doesn't interest us here. The type of 
        // the element in the typesToActSets mapping is an ActionProxySet,
        // which knows some stuff about the annotation type properties.
        
        Set actions = (Set) typesToActSets.get(type);
        if (actions == null) {
          actions = new LinkedHashSet();
          typesToActSets.put(type, actions);
          allSets.add(actions);
        }
        actions.add(actProxy);
      }
    }
    
    Iterator setIter = allSets.iterator();
    boolean first = true;
    
    while (setIter.hasNext()) {
      LinkedHashSet curSet = (LinkedHashSet) setIter.next();
      // I have the types in the hash table, but I'm really not
      // about to bother saving them away right now.
      actionIter = curSet.iterator(); 
      boolean newType = true;
      while (actionIter.hasNext ()) {
        Action ap = (Action) actionIter.next();
        AnnotationType type = (AnnotationType) actionsToTypes.get(ap);
        
        // type is null for general annotations, and the general
        // ones (type is null) will come first. Between each
        // group, we add a separator.
        
        boolean addIt = false;
        
        if (type == null) {
          addIt = true;
        } else {
          // find the runtime class type
          Class annotClass = kit.getTask().getAnnotationClass(type);
          
          if (TextExtentRegion.class.isAssignableFrom(annotClass) ||
              NamedExtentRegions.class.isAssignableFrom(annotClass)) {
            addIt = true;
          }
        }
        
        // Only change the menu if you're about to add something.
        
        if (addIt) {
          
          if (first) {
            addSeparator();
            first = false;
          } else if (newType) {
            addSeparator();
          }
          newType = false;
          addAction(ap, defaultCmd);
        }
      }
    }

    // And finally, we add the items from the extent modification list.
    // No cascade, even if requested. For now.
    
    List modActions = actionModel.getExtentModificationActions();
    actionIter = modActions.iterator();
    if (modActions.size() > 0) {
        mainPane.add (modifierSep);
    }
    while (actionIter.hasNext()) {
      addAction((ActionProxy) actionIter.next(), null);
    }
    
  }
  
  private AnnotationType annotationTypeForAction(Action actProxy, ActionCollection c, AnnotationType typeSoFar) {
    //  Not taking any chances. Although the code is written so
    // that this will always be a proxy or a group of proxies, we
    // should write it so that if it's a group of annotation actions
    // or an annotation action, it will work.
    
    ActionGroup g = c.getActionGroup(actProxy);
    
    if (g != null) {
      return annotationTypeForActionGroup(g, c, typeSoFar);
    } else {
      AnnotationType type;
      if (actProxy instanceof ActionProxy) {
        AnnotationAction act = (AnnotationAction) ((ActionProxy) actProxy).getAction();
        type = act.getAnnotationType();
      } else {
        type = ((AnnotationAction) actProxy).getAnnotationType();
      }
      if (typeSoFar == null) {
        return type;
      } else if (!typeSoFar.equals(type)) {
        return null;
      } else {
        return typeSoFar;
      }
    }
  }
  
  private AnnotationType annotationTypeForActionGroup(ActionGroup group, ActionCollection c, AnnotationType typeSoFar) {
    AnnotationType type;
    if (!group.isDummyParent()) {
      Action parent = group.getAction();
      if (parent instanceof ActionProxy) {
        AnnotationAction act = (AnnotationAction) ((ActionProxy) parent).getAction();
        type = act.getAnnotationType();
      } else {
        type = ((AnnotationAction) parent).getAnnotationType();
      }
      if (typeSoFar == null) {
        typeSoFar = type;
      } else if (!typeSoFar.equals(type)) {
        return null;
      }
    }
    Action[] subActions = group.getSubActions();
    for (int i = 0; i < subActions.length; i++) {
      typeSoFar = annotationTypeForAction(subActions[i], c, typeSoFar);
      if (typeSoFar == null) {
        return null;
      }
    }
    return typeSoFar;
  }

  /********************* Dialog Handling Methods *********************/
  
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
    final String geomKey = "mainTextPalette";
    final String visibleKey = "windows.mainTextPalette.visible";
    if (! GUIUtils.loadGeometry (dialog, geomKey))
      dialog.setLocationRelativeTo(parentComponent);

    dialog.pack(); // keeps location, resizes if it likes

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
  
  public void setJawbDocument (JawbDocument doc) {
    if (DEBUG > 0) 
      System.err.println("MTPal.setJD from: " + document + " to: " + doc);

    document = doc;
    if (doc == null) {
      // Closing down. Clear the actions fired table.
      Iterator removeIter = actionsFired.iterator();
      while (removeIter.hasNext()) {
        Action act = (Action) removeIter.next();
        if (DEBUG > 3)
          System.err.println("MTPal.setJD to null; removing fired action: " 
                             + act);
        removeAction (act);
      }
      actionsFired.clear();
    } else {
      if (kit instanceof ToocaanToolKit) {
        // context may have changed, refresh action model for each
        // new document
        ActionModel newActionModel = kit.getActionModel();
        if (DEBUG > 2)
          System.err.println("MTPal.setJD actionModel from ToocaanTk: " + 
                             newActionModel);
        if (!newActionModel.equals(actionModel)) {
          // clear and repopulate actions
          System.err.println("MTPal.setJD action model has changed, need to repopulate palette.");

          actionModel.removeActionExecutionListener(this);
          clearPalette();
                    
          actionModel = newActionModel;
          populatePalette();
          actionModel.addActionExecutionListener(this);
          
        }
      }
    }
  }

  private void clearPalette() {
    while (paletteActions.size() > 0) {
      Action act = paletteActions.pop();
      removeAction(act);
    }
    // remove anything remaining in the pane, i.e., separators
    mainPane.removeAll();
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
  
  /******************** Action Manipulation ***********************/

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

  private void addSeparator() {
    mainPane.add (new MainTextPalette.Separator());
  }
  
  private void addAction (Action act, String defaultCmd) {
    addAction(act, defaultCmd, false, -1);
  }
  
  private void addSubAction (Action act, String defaultCmd, int where) {
    addAction(act, defaultCmd, true, where);
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

    // add to the list of actions in the palette
    paletteActions.add(act);
    
    // add to the main pane
    mainPane.add (data.getPanel (), where);
    growPack ();
  }

  /************************* Caching **********************************/
  
  /** Creates all the widgets we need for an action and hooks up listeners,
   * caches for speed on subsequent lookups. SAM 1/27/06: Moved the 
   * cache update into the constructor because different things need
   * to be cached depending on whether we have a cascade or not.  */
  private ActionData getActionData (Action act, boolean isSubAction) {

    ActionData data = (ActionData) actionToDataMap.get (act);
    if (data == null) {
      data = new ActionData (act, isSubAction);
    }
    return data;
  }


  /***********************************************************************/
  /* Listener Implementations */
  /***********************************************************************/

  // Implementation of MouseListener interface

  /**
   * Cause the default action to be fired when users middle click on a window
   * which uses this object as a mouse listener.
   */
  public void mouseClicked(MouseEvent e) {
    if ( (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON2) ||
         (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) ) {
      ActionEvent act = new ActionEvent (e.getSource (),
                                         ActionEvent.ACTION_PERFORMED,
                                         "Middle Click");
      if (DEBUG > 1) {
        JTextComponent component = (JTextComponent) e.getSource();
        Caret caret = component.getCaret ();
        System.err.println("MTPalette.mClicked caret dot=" + caret.getDot() +
                           " mark=" + caret.getMark());
      }
      if (defaultAction != null && defaultAction.isEnabled ())
        defaultAction.actionPerformed (act);
    }
  }

  public void mousePressed(MouseEvent e) {}
  public void mouseReleased(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}


  // Action execution listener.
  
  private LinkedHashSet actionsFired = new LinkedHashSet();

  public void actionFired(ActionProxy act, ActionCollection c) {
    if (DEBUG > 3)
      System.err.println("MTPal.actFired: " + act + " coll: " + c);
    // Here's where we'll add the actions we've used. If it's 
    // been fired, and it's the top of a cascade OR not
    // toplevel, we need to insert it into the panel in
    // the appropriate place, under the toplevel one. So we 
    // look through the components, and we find their
    // hashes in the actiontodatamap.
    if (!c.isStandaloneAction(act)) {
      Action a = c.getRootAction(act);
      ActionGroup ag = c.getActionGroup(a);
      // We've indexed the group, not the action, of course.
      ActionData d = (ActionData) actionToDataMap.get(ag);
      if (d != null) {
        // There might not be a mapping. If not, just ignore it.
        // Here, we create a new button for this action proxy. It's
        // not going to be a group under any circumstances, so 
        // we're safe in putting it in a new button. But we have to 
        // figure out where it goes. Well, first we have to figure
        // out whether I've placed it or not.
        if (!actionsFired.contains(act)) {
          // Place it, because it hasn't been placed yet.
          Component[] components = mainPane.getComponents();
          JPanel p = d.getPanel();
          // Where's the parent? 
          int i = 0;
          for (; i < components.length; i++) {
            if (p.equals(components[i])) {
              // put it AFTER the component.
              i++;
              break;
            }
          }
          addSubAction(act, null, i);
          actionsFired.add(act);
        }
      }
    }
  }

  
  /***********************************************************************/
  /* Inner classes */
  /***********************************************************************/

  public static class Separator extends JPanel {
    public Separator() {
      super(new BorderLayout());
      add (new JSeparator(JSeparator.HORIZONTAL));
      setBorder(BorderFactory.createEmptyBorder (4,1,4,1));
    }
  }
  
  /**
   * Private class to hold the several widgets we use for an actions button
   */
  private class ActionData {

    private static final int DEBUG = 0;

    Action action;
    String command;
    JPanel panel;
    JButton button;
    JToggleButton toggle;
    TextPaletteContextMenu cascadeMenu;
    
    private class TextPaletteContextMenu extends TextContextMenu {
      
    }
    
    public ActionData (Action a) {
      this(a, false);
    }

    public ActionData (Action a, boolean isSubAction) {
      action = a;
      command = (String) action.getValue (Action.ACTION_COMMAND_KEY);
      panel = new JPanel (new BorderLayout ());
      ActionCollection c = (ActionCollection) actionToCollection.get(a);
      // A button for a cascade action is quite different than
      // a regular button. For the cascade action, I need to make
      // sure that the label does all the right things, which means
      // that if I give it an action, the action needs to proxy the
      // action passed in - that is, it must be a proxy of a proxy.
      // I think. Or not. ActionProxy instances are defined in
      // the context of an ActionModel instance, so we can't use
      // it here. Perhaps I should define an action delegate and
      // specialize it to ActionProxy? Yes.
      if ((!isSubAction) && (c != null) && (!c.isStandaloneAction(a))) {
        cascadeMenu = new TextPaletteContextMenu();
        button = new JButton();
        final ActionGroup ag = c.getActionGroup(a);
        final ActionCollection finalC = c;
        ActionDelegate ap = new ActionDelegate (c.getActionGroup(a)) {
          public void actionPerformed (ActionEvent e) {
            cascadeMenu.showMenu(button, button.getWidth(), 0, ag, finalC, false);
          }
        };
        button.setAction(ap);
        ag.handleColorChanges(button);
        toggle = new JRadioButton();
        toggle.setEnabled(false);
        actionToDataMap.put (ag, this);
      } else {
        button = new JButton (action);
        // toggle button which
        //   1) stores this action as default (for this task) in prefs, and
        //   2) set's the button for it as the default for our rootpane
        toggle = new JRadioButton (new AbstractAction () {
          public void actionPerformed (ActionEvent e) {
            if (command != null)
              Jawb.getPreferences().setPreference(defaultActionKey, command);
            
            else {
              System.err.println ("BUG: no action command for "+action);
              Thread.dumpStack ();
            }
            defaultAction = action;
            if (getRootPane () != null)
              getRootPane().setDefaultButton (button);
          }
        });
        toggle.setToolTipText ("Bind action to <middle-mouse-click>");
        if (a instanceof ActionProxy) {
          ((ActionProxy) a).handleColorChanges(button);
        }
        actionToDataMap.put (action, this);
      }
      button.setHorizontalAlignment (JButton.LEADING);

      // layout a simple panel that will go in the Box
      panel.add (toggle, BorderLayout.WEST);
      if (isSubAction) {
        // Wrap it around another panel.
        JPanel wrapPanel = new JPanel (new BorderLayout ());
        wrapPanel.add(Box.createRigidArea(new Dimension(10, 0)), BorderLayout.WEST);
        wrapPanel.add(button, BorderLayout.CENTER);
        panel.add (wrapPanel, BorderLayout.CENTER);
      } else {
        panel.add (button, BorderLayout.CENTER);
      }
    }
    
    public Action getAction () { return action; }
    public String getCommand () { return command; }
    public JPanel getPanel () { return panel; }
    public JButton getButton () { return button; }
    public JToggleButton getToggle () { return toggle; }
  }

}
