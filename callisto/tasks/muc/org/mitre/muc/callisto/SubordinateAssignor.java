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

package org.mitre.muc.callisto;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import gov.nist.atlas.type.AnnotationType;

import org.mitre.jawb.atlas.*;
import org.mitre.jawb.gui.AnnotationAction;
import org.mitre.jawb.gui.AnnotationTable;
import org.mitre.jawb.gui.DeleteAnnotAction;
import org.mitre.jawb.gui.GUIUtils;
import org.mitre.jawb.gui.JawbComponent;
import org.mitre.jawb.gui.JawbDocument;
import org.mitre.jawb.gui.AnnotationMouseAdapter;
import org.mitre.jawb.gui.AnnotationMouseEvent;
import org.mitre.jawb.gui.AnnotationPopupListener;
import org.mitre.jawb.tasks.TaskToolKit;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.swing.SetModel;

/**
 * Initial version of a mouse listener that has a popup window to associate
 * annotations with other annotations. It adds a mouse
 * listener to the AnnotationTable of the Components specified in the
 * constructor. Listening for popup-triggers, it will display an appropriate
 * context menu when the mouse is clicked within those tables.<p>
 *
 * Actions within the popup menu may set a 'state' so that users may switch to
 * another table and 'select' a new annotation to complete the
 * action. Instances of this object will also listent to the JawbDocument and
 * it's annotations (via PropertyChangeListener: they are destined to be
 * rolled together) for changes/additions which causes immidiate state
 * reset.<p>
 *
 * The popup menus for the Tables are built here because the state needs to be
 * maintained between 'clicks'.<p>
 *
 * This class is not thread safe, but it is expected that it will be run in
 * the GUI thread only.
 */
public class SubordinateAssignor implements AnnotationModelListener {

  public static final int DEBUG = 0;
  

  /**
   * Indicates 'state' of the Assignor
   * @see SAState
   */
  SAState state = SAState.READY;
  
  //  EntityEditor entities;
  ChainEditor chains;
  TaskToolKit toolkit;
  MUCCustomTask task;

  private JPopupMenu contextMenu = new JPopupMenu ();
  private SubAssignorPopupListener popupListener = null;

  /** Dialog to popup while in non READY state, as aid to user. */
  private StateDialog stateDialog;
  
  private JawbDocument currentDoc = null;
  /** Set of AWBAnnotations selected before an action was performed. TODO:
   * Perhaps this ought to be juse merged with the JawbDocument selection
   * mechanism */
  private Set selectedAnnots = new LinkedHashSet ();
  private AnnotationType selectedType = null;

  private HashMap actionMap;
  /* Actions that are used with the context menu */
  JMenuItem nullAction = new JMenuItem ("No Action");
  
  Action deleteAnnotAction = null;

  /** Temp object to reduce allocations. */
  private Point pt = new Point ();


  public SubordinateAssignor (// EntityEditor entities,
                              ChainEditor chains,
                              TaskToolKit toolkit) {
    // initialize actions
    //    this.entities = entities; 
    this.chains = chains;
    this.toolkit = toolkit;
    this.task = (MUCCustomTask)toolkit.getTask();

    popupListener = new SubAssignorPopupListener (toolkit);
    //entities.getTable ().addMouseListener (popupListener);
    //    chains.getTable ().addMouseListener (popupListener);

    // here we retrieve the 'delete' actions from the tables themselves.
    // if a custom table doesn't set it, the AnnotationTable default is used
    // (note that all EELD Tables do have delete customized)
    nullAction.setEnabled (false);
    deleteAnnotAction = new DeleteAnnotAction (toolkit);
  }

  private void initActions () {
    actionMap = new HashMap ();
    // hash the actions based on their action command key
    Iterator iter = toolkit.getActions().iterator ();
    while (iter.hasNext ()) {
      Action act = (Action) iter.next();
      Object key = act.getValue (act.ACTION_COMMAND_KEY);
      if (act instanceof AnnotationAction && (key != null ))
        actionMap.put (key, act);
    }
  }

  /**
   * Lazy instantiate the state dialog, so that we can use one of the editors
   * JawbFrame to anchor the dialog.
   */ 
  void setStateDialogVisible (boolean visible) {
    if (stateDialog == null) {
      //stateDialog = new StateDialog (GUIUtils.getJawbFrame (entities));
      //GUIUtils.centerComponent (entities, stateDialog);
      stateDialog = new StateDialog (GUIUtils.getJawbFrame (chains));
      GUIUtils.centerComponent (chains, stateDialog);
    }
    stateDialog.setVisible (visible);
  }

  /**
   * Change the state, and hide/show the stateDialog if neccissary. All
   * actions and other which need to perform multi-part actions need to use
   * this method change state.
   */
  void setState (SAState newState) {
    state = newState;
    if (DEBUG > 2)
      System.err.println ("SA.setState: "+newState);

    if (state == SAState.READY) {
      selectedType = null;
      selectedAnnots.clear ();
      setStateDialogVisible (false);
      
    } else if (state == SAState.ADJUSTING) {
      setStateDialogVisible (false);

    } else if (state == SAState.SELECT_DESTINATION_CHAIN ||
               state == SAState.SELECT_DEST_CHAIN_AGAIN) {
      String title = ((currentDoc==null) ? null :
                      "MUCCustomTask - "+currentDoc.getDisplayName (false));
      if (stateDialog == null)
        setStateDialogVisible (false);
      stateDialog.setState (state, title);
      setStateDialogVisible (true);
    }
  }
  
  /**
   * Set the state and the selection in one fell swoop.
   * TODO: get rid of the version the non annot version!
   */
  void setState (SAState newState, AnnotationType type,
                 Set annots, JawbDocument doc) {

    if (state != SAState.READY) {
      System.err.println ("SubAssignor.setState (state, Set): "+
                          "assigning a set of annots when not ready");
      Thread.dumpStack ();
    }

    setJawbDocument (doc);

    Iterator iter = annots.iterator ();
    while (iter.hasNext ()) {
      if (! (((AWBAnnotation)iter.next ()).getAnnotationType () == type)) {
        GUIUtils.showError ("All annots must be of type "+type);
        return;
      }
    }
    selectedType = type;
    selectedAnnots.clear ();
    selectedAnnots.addAll (annots);

    setState (newState);
  }

  void setJawbDocument(JawbDocument doc) {

    if (DEBUG > 0) 
      System.err.println ("SA.setJD: doc= " + 
			  (doc == null ? "null" : doc.getDisplayName(false)));

    // if the "old" currentDoc was non-null, deregister the popup
    // listener from it
    if (currentDoc != null) {
      if (DEBUG > 0)
	System.err.println ("SA.setJD: deregister AnnotationMouseListener");
      currentDoc.getAnnotationMouseModel().removeAnnotationMouseListener(popupListener);
    }
    
    currentDoc = doc;

    // if doc is non-null, register the popup listener on its 
    // AnnotationMouseModel
    if (doc != null) {
      if (DEBUG > 0)
	System.err.println ("SA.setJD: registering AnnotationMouseListener");
      doc.getAnnotationMouseModel().addAnnotationMouseListener(popupListener);
    }
  }
  
  Set getSelectedAnnots () {
    return selectedAnnots;
  }

  AnnotationType getSelectedType () {
    return selectedType;
  }

  /**
   * Called from the popup listener to prepare a context menu, and store the
   * annotations being clicked, table, and anything else needed by secondary
   * actions. It is up to the actions to set the 'state' variable.
   */
  /* This has been replaced by a method in AnnotationPopupListener
  private void showContextMenu (MouseEvent e) {
    if (actionMap == null)
      initActions();
    
    pt.x = e.getX();
    pt.y = e.getY();

    // column selected may be interesting later
    JawbComponent jc = GUIUtils.getJawbComponent (e);
    //row = table.rowAtPoint (pt);
    //col = table.columnAtPoint (pt);


    selectedAnnots.clear ();
    selectedAnnots.addAll (jc.getSelectedAnnots ()); 

    AnnotationType annotType = null;
    boolean singleEntity = selectedAnnots.size () == 1;
    Iterator iter = selectedAnnots.iterator (); 
    if (selectedAnnots.size () > 0) {
      annotType = ((AWBAnnotation)iter.next ()).getAnnotationType ();
      while (iter.hasNext ()) {
        AnnotationType type = ((AWBAnnotation)iter.next()).getAnnotationType ();
        if (annotType != null && ! annotType.equals (type))
          annotType = null;
      }
    }
    // at this point, typename is <null> if all annots are not same type.. not
    // likely here, but who knows what crazyness will happen to the code
    // later.
    selectedType = annotType;

    contextMenu.removeAll ();
    
    // NOTE: if click is outside a column, the click source is not the table
    // so we don't even see it... it's likely to be the 'scrollPane'
    // containing the table.
    if (annotType == null) {
      // if not all selected rows are of same type... dunno what to do yet
      contextMenu.add (nullAction);
      

    } else if (annotType.equals (MUCCustomUtils.ENTITY_TYPE)) {
      
      contextMenu.add ((Action) actionMap.get ("entity-2-new-chain"));
      contextMenu.add ((Action) actionMap.get ("entity-2-chain"));
      
      contextMenu.addSeparator ();
      contextMenu.add (deleteAnnotAction);

      // disable adding to existing chains if there are none.
      int entCount = chains.getTable ().getModel ().getRowCount ();
      ((Action) actionMap.get ("entity-2-chain")).setEnabled (entCount > 0);
      
    } else if (annotType.equals (MUCCustomUtils.CHAIN_TYPE)) {
      contextMenu.add (deleteAnnotAction);
      
      
    } else { // TODO: complete the other types
      contextMenu.add (nullAction);
    }
    
    contextMenu.show (e.getComponent(), pt.x, pt.y);
  }
  */

  /**
   * Once an chain has been selected by creating a new one or getting the
   * selected one, add the entities to it!.
   */
  boolean addSubAnnotsToSuper (Set subAnnots,
                               HasSubordinates superAnnot,
                               JawbDocument doc) {
    if (DEBUG > 0) {
      System.err.print ("SA.addSubsToSuper: super="+superAnnot.getId ()+
                        " subs=[");
      Iterator subIter = subAnnots.iterator ();
      while (subIter.hasNext ()) {
        AWBAnnotation subAnnot = (AWBAnnotation)subIter.next ();
        System.err.print (" subannot:"+subAnnot.getId()+" ");
      }
      System.err.println("]");
    }
    
    Iterator subIter = subAnnots.iterator ();
    while (subIter.hasNext ()) {
      AWBAnnotation subAnnot = (AWBAnnotation)subIter.next ();
      if (! addSubAnnotToSuper (subAnnot, superAnnot, doc))
        return false;
    }
    return true;
  }

  boolean addSubAnnotToSuper (AWBAnnotation subAnnot,
                              HasSubordinates superAnnot,
                              JawbDocument doc) {
    AnnotationType subType = subAnnot.getAnnotationType ();
    AnnotationType superType = superAnnot.getAnnotationType ();
    if (DEBUG > 0)
      System.err.println ("SA.addSub2Super: "+subAnnot.getId()+
                          " to "+superAnnot.getId());
    
    /*** This is not necessary for this version of "coreference" that
     *** we want to use in the MUC Custom task
    if (subType.equals (MUCCustomUtils.ENTITY_TYPE) &&
        superType.equals (MUCCustomUtils.CHAIN_TYPE)) {
      AWBAnnotation oldParent = MUCCustomUtils.getChain (subAnnot);
      if (oldParent != null &&
          ! MUCCustomUtils.removeEntityFromChain (subAnnot, oldParent, doc))
        return false;
      
    }
    ***/
    
    if (DEBUG > 0)
      System.err.println ("SA.addSub2Super: "+subAnnot.getId()+
                          " into "+superAnnot.getId());
    
    boolean t = superAnnot.addSubordinate (subAnnot);
    if (DEBUG > 0)
      System.err.println ("  .addSub2Super: success="+t);

    if (subType.equals (MUCCustomUtils.ENTITY_TYPE) &&
        superType.equals (MUCCustomUtils.CHAIN_TYPE)) {
      // as long as we're adding some, make sure there's a primary
      // TODO: is this necessary, or should it be part of some
      // 'addSubordinate' handler/listener.
      try {
        if (superAnnot.getAttributeValue ("chain_head") == null)
          superAnnot.setAttributeValue ("chain_head", subAnnot);
      } catch (Exception x) { x.printStackTrace (); }
    } 
    return true;
  }
  
  /***********************************************************************/
  /* Implementing the AnnotationModelListener Interface */
  /***********************************************************************/

  /** Invoked after an annotation has been created. */
  public void annotationCreated (AnnotationModelEvent e) {
    if (state != SAState.ADJUSTING)
      setState (SAState.READY);
  }
  
  /** Invoked after an annotation has been deleted. */
  public void annotationDeleted (AnnotationModelEvent e) {
    if (state != SAState.ADJUSTING)
      setState (SAState.READY);
  }
  
  /** Invoked after an annotation has been changed. */
  public void annotationChanged (AnnotationModelEvent e) {
    if (state != SAState.ADJUSTING)
      setState (SAState.READY);
  }
  
  /** Invoked after an annotation has had subannotations added. */
  public void annotationInserted (AnnotationModelEvent e) {
    if (state != SAState.ADJUSTING)
      setState (SAState.READY);
  }
  
  /** Invoked after an annotation has had subannotations removed. */
  public void annotationRemoved (AnnotationModelEvent e) {
    if (state != SAState.ADJUSTING)
      setState (SAState.READY);
  }

  
  /***********************************************************************/
  /* Actions */
  /***********************************************************************/


  
  /***********************************************************************/
  /* ANNOTATION MOUSE LISTENERS */
  /***********************************************************************/

  /**
   * This class listens for 'popup triggers' on annotations in any
   * view of the AnnotationMouseModel and configures the context menu
   * for it. 
   * 
   * I'm not sure if this statement still applies: This listener is
   * not thread safe, but as long as it's used with the GUI thread,
   * you're fine.
   */
  private class SubAssignorPopupListener extends AnnotationPopupListener {

    public SubAssignorPopupListener (TaskToolKit toolkit) {
      super(toolkit);
    }
    
    public void mouseReleased (AnnotationMouseEvent e) {
      if (DEBUG > 0)
	System.err.println("SA.annotMouseReleased: pop=" + e.isPopupTrigger());
      if (state == SAState.READY)
	super.mouseReleased(e);
    }


    public void mousePressed (AnnotationMouseEvent e) {
      if (DEBUG > 0)
	System.err.println ("SA.annotMousePressed: pop=" + e.isPopupTrigger());
      if (state == SAState.READY) 
	super.mousePressed(e);
    }

    /**
     * Handle the second half of actions here, not in the 'pressed' or
     * 'released' methods, so a user can 'cancel out' of a click by dragging
     * away.
     */
    public void mouseClicked (AnnotationMouseEvent e) {
      if (DEBUG > 0)
        System.err.println ("SA.ATPopListener.mClicked: state="+state);
      
      JawbDocument doc = GUIUtils.getJawbDocument(e.getMouseEvent());
        
      if (state == SAState.READY) {
        /* empty */
        
      } else if (state == SAState.ADJUSTING) {
        /* empty */      // what's going on?
        
      } else if (state == SAState.SELECT_DESTINATION_CHAIN ||
                 state == SAState.SELECT_DEST_CHAIN_AGAIN) {
        
        if (DEBUG > 1)
          System.err.println ("SA.mClicked: state: Select Chain: entity");
        
        // if a chain annot has been selected, go on,
	// if a entity annot has been selected, find its chain,
	// otherwise skip this event
        AWBAnnotation annot = e.getAnnotation ();
        if (annot == null) {
          return;
        }
	AWBAnnotation chain = null;
	if (DEBUG > 1) 
	  System.err.println ("SA.mClicked: annot = " + annot +
			      "type = " + annot.getAnnotationType());
        if (annot.getAnnotationType ().equals (MUCCustomUtils.CHAIN_TYPE)) {
	  chain = annot;
	} else if (annot.getAnnotationType().equals 
		   (MUCCustomUtils.ENTITY_TYPE)) {
	  chain = task.getChain((PhraseTaggingAnnotation)annot);
	} else {
	  return;
	}
	if (chain == null) {
          //GUIUtils.showWarning("Select a chain, or already assigned entity");
          // RK 8/26/2010 second dialog triggers a windows/java bug I
          // can't figure out so just change the first one instead:
          setState (SAState.SELECT_DEST_CHAIN_AGAIN);
          return;
        }
        
        addSubAnnotsToSuper (selectedAnnots, (HasSubordinates) chain, doc);

        // force reselection of annots.
        doc.unselectAllAnnotations ();
        //doc.selectAnnotation (chain);
        doc.selectAnnotation((AWBAnnotation)selectedAnnots.iterator().next());

	e.consume();
        
        setState (SAState.READY);
        
      }
    }
  }

  /** Dialog to show the current state for multi-step actions */
  private class StateDialog extends JDialog {
    JLabel stateLabel = new JLabel ("____________________", JLabel.CENTER);
    
    public StateDialog (Frame frame) {
      super (frame, true);
      Action cancel = new AbstractAction ("Cancel") {
          public void actionPerformed (ActionEvent e) {
            SubordinateAssignor.this.setState (SAState.READY);
          }
        };

      JPanel buttons = new JPanel ();
      buttons.add (new JButton (cancel));

      getContentPane ().add (stateLabel, BorderLayout.CENTER);
      getContentPane ().add (buttons, BorderLayout.SOUTH);
      // cannot be closed by user.
      setDefaultCloseOperation (JDialog.DO_NOTHING_ON_CLOSE);

      ActionMap actionMap = stateLabel.getActionMap ();
      actionMap.put ("cancel", cancel);
      
      InputMap inputMap = stateLabel.getInputMap ();
      inputMap.put (KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), "cancel");
    }

    public void setState (SAState state, String title) {
      stateLabel.setText (state.toString ());
      pack ();
    }

    /**
     * Overridden to use the 'modal after visible' hack to keep it on top of
     * at least the jawb frame (though it will only float over one
     * window). Turns non modal on hiding so same hack works again later.
     */
    public void setVisible (boolean visible) {
      super.setVisible(visible);
      setModal(visible);
    }
  }

  /** Typesafe enumeration for state. */
  static class SAState {

    // TODO: be sure to add new states to 'setState' methods decision tree
    
    /** Initial state, ready to start an action. */
    public static final SAState READY = new SAState ("Ready");
    /**
     * Used during actions which modify annotations, so the change event fired
     * by the modification won't trigger a state change to READY.
     */
    public static final SAState ADJUSTING = new SAState ("Adjusting");
    public static final SAState SELECT_DESTINATION_CHAIN =
      new SAState ("Select Chain");
    public static final SAState SELECT_DEST_CHAIN_AGAIN =
      new SAState ("Select a chain, or already assigned entity");

    private String name;
    private SAState (String name) {
      this.name = name;
    }
    public String toString () {
      return name;
    }
  }
}
