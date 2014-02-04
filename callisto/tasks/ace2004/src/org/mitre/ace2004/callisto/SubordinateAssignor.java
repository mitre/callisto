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

package org.mitre.ace2004.callisto;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
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
import org.mitre.jawb.gui.JawbFrame;
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
 * action. Instances of this object will also listen to the JawbDocument and
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
  
  Component component;
  TaskToolKit toolkit;
  ACE2004Task task;

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

  /* Actions that are used with the context menu */
  JMenuItem nullAction = new JMenuItem ("No Action");
  
  Action deleteAnnotAction = null;

  /** Temp object to reduce allocations. */
  private Point pt = new Point ();


  public SubordinateAssignor (Component component, TaskToolKit toolkit) {
    this.component = component;
    this.toolkit = toolkit;
    this.task = (ACE2004Task)toolkit.getTask();

    if (DEBUG > 1)
	System.err.println("MEM: SA constructor allocating SubAssignorPopupListener");
    popupListener = new SubAssignorPopupListener (toolkit);

    // here we retrieve the 'delete' actions from the tables themselves.
    // if a custom table doesn't set it, the AnnotationTable default is used
    // (note that all EELD Tables do have delete customized)
    nullAction.setEnabled (false);
    deleteAnnotAction = new DeleteAnnotAction (toolkit);
  }

  /**
   * Lazy instantiate the state dialog, so that we can use one of the editors
   * JawbFrame to anchor the dialog.
   */ 
  void setStateDialogVisible (boolean visible) {
    JawbFrame frame = GUIUtils.getJawbFrame (component);
    if (stateDialog == null) {
      stateDialog = new StateDialog (frame);
      stateDialog.pack();
    }
    GUIUtils.centerComponent (frame, stateDialog);
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

    } else if (state == SAState.SELECT_DESTINATION_ENTITY ||
               state == SAState.SELECT_DESTINATION_ENTITY_PRIMARY ||
               state == SAState.SELECT_DESTINATION_EVENT ||
               state == SAState.SELECT_DESTINATION_RELATION_ARG1 ||
               state == SAState.SELECT_DESTINATION_RELATION_ARG2 ||
               state == SAState.SELECT_DESTINATION_RELATION_ENTITY_ARG1 ||
               state == SAState.SELECT_DESTINATION_RELATION_ENTITY_ARG2 ||
               state == SAState.SELECT_DEST_UNMENT_RELATION_ENTITY_ARG1 ||
               state == SAState.SELECT_DEST_UNMENT_RELATION_ENTITY_ARG2 ||
               state == SAState.SELECT_DESTINATION_RELATION_MENTION ||
               state == SAState.ASSIGN_EVENT_MENTION_EXTENT ||
               state == SAState.ASSIGN_RELATION_MENTION_EXTENT ||
               state == SAState.SELECT_ARGUMENT_MENTION_VALUE ||
               state == SAState.ADD_ARGUMENT_TO_ANNOT ||
               state == SAState.ADD_PARENT_ENTITY_ARGUMENT_TO_ANNOT) {
      String title = ((currentDoc==null) ? null :
                      "ACE2004Task - "+currentDoc.getDisplayName (false));
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
	AWBAnnotation foo = (AWBAnnotation) iter.next ();
      if (! (foo.getAnnotationType() == type)) {
        GUIUtils.showError ("All annots must be of type " + type + "(but was " +
			    foo.getAnnotationType());
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
   * Once an entity has been selected by creating a new one or getting the
   * selected one, add the mentions to it!.
   */
  static boolean addSubAnnotsToSuper (Set subAnnots,
                                      HasSubordinates superAnnot,
                                      AWBDocument doc) {
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

  /**
   * Need this as a separate method so we can remove from the 'old'
   * superordinate before it's changed (thus a 'listener' won't work).
   */
  static boolean addSubAnnotToSuper (AWBAnnotation subAnnot,
                                     HasSubordinates superAnnot,
                                     AWBDocument doc) {
    AnnotationType subType = subAnnot.getAnnotationType ();
    AnnotationType superType = superAnnot.getAnnotationType ();

    // may need to remove the entity from some relations, then add the new
    // entity in it's place below. This maps the relations to the arg name.
    HashMap relationsToUpdate = new HashMap();

    if (DEBUG > 0)
      System.err.println ("SA.addSub2Super: "+subAnnot.getId()+
                          " to "+superAnnot.getId());
    
    if (subType.equals (ACE2004Utils.ENTITY_MENTION_TYPE) &&
        superType.equals (ACE2004Utils.ENTITY_TYPE)) {
      AWBAnnotation oldParent = ACE2004Task.getMentionParent(subAnnot);

      if (oldParent != null) {

        // I can't think of a better way to do this just now :(
        // ..... if the mention is in a relation-mention, then the
        // entity should also be in the relation, thus changing the
        // mentions entity would require changing the relations argX
        // attribute to match.  Unfortunately if the relation has more
        // than 1 relation-mention, it could all get out of whack if
        // the other relation-mentions referred to mentions of the old
        // entity.
        
        // Instead of trying to deal with it the mention is not
        // allowed to be moved to a different entity until it has been
        // removed from all mention-relationships whose relations have
        // multiple relation-mentions
        boolean isInMultiMentionRelation = false;
        
        // get list of relation-mentions that we need to check
        // RK 3/22/07 TODO -- I think this is wrong -- getEventRelations
        // takes an entity and returns null if anything else is passed,
        // such as the entity mention passed here
        HasSubordinates[] rMentions = ACE2004Task.getEventRelations(subAnnot);
        for (int i=0; i<rMentions.length; i++) {
          HasSubordinates relation = ACE2004Task.getMentionParent(rMentions[i]);
          if (relation == null) {
            System.err.println("SA.addSub2Super: relMention without rel: "+
                              rMentions[i].getId());
            continue;
          }

          int corefRMentionCount = relation.getRegion()
            .getSubordinateSet(ACE2004Utils.RELATION_MENTION_TYPE).size();
          if (corefRMentionCount > 1) {
            if (DEBUG > 0)
              System.err.println("SA.addSub2Super: refusing to reassign mention with coreferant relation-mentions: ment="+subAnnot.getId()+" ent="+oldParent.getId()+" rel="+relation.getId()+" rMents="+corefRMentionCount);
            
            String msg =
              "The Mention you are moving participates in a relation which\n"+
              "has multiple mentions. Changing the Entity of this mention\n"+
              "would corrupt that relation.\n\n"+
              "Remove the entity_mention from these relation_mentions first.";
            GUIUtils.showError (msg);
            return false;
          }

          // ok, it can be reassigned... which arg?
          if (oldParent.equals(relation.getAttributeValue("arg1")))
            relationsToUpdate.put(relation, "arg1");
          else
            relationsToUpdate.put(relation, "arg2");
        } // while rMentions.length...

        // ok, there can be no conflict by reassigning the mentions (other than
        // in relation argument constraints ... ughh) Remove the entity from
        // the relations (allowing it to be deleted).  Later, after reassigning
        // the mention, put the new entity in the right args
        Iterator entries = relationsToUpdate.entrySet().iterator();
        while (entries.hasNext()) {
          Map.Entry entry = (Map.Entry) entries.next();
          AWBAnnotation relation = (AWBAnnotation) entry.getKey();
          try {
            relation.setAttributeValue((String)entry.getValue(), null);
          } catch (Exception x) {}
        }

        boolean removed =
          ACE2004Utils.removeMentionFromEntity (subAnnot, oldParent, doc);

        if (! removed) { // add it back before returning false
          Iterator failedIter = relationsToUpdate.entrySet().iterator();
          while (failedIter.hasNext()) {
            Map.Entry entry = (Map.Entry) failedIter.next();
            AWBAnnotation relation = (AWBAnnotation) entry.getKey();
            try {
              relation.setAttributeValue((String)entry.getValue(), null);
            } catch (Exception x) {}
          }
          return false;
        }
      } // (if oldParent != null)
    } // if ENTITY...
    else if (subType.equals (ACE2004Utils.EVENT_MENTION_TYPE) &&
             superType.equals (ACE2004Utils.EVENT_TYPE)) {
      AWBAnnotation oldParent = ACE2004Task.getMentionParent(subAnnot);
      if (oldParent != null &&
          ! ACE2004Utils.removeMentionFromEvent (subAnnot, oldParent, doc)) {
        System.err.println("------------Failed to remove evt.ment from evt!");
        return false;
      }
    }
    else if (subType.equals (ACE2004Utils.RELATION_MENTION_TYPE) &&
             superType.equals (ACE2004Utils.RELATION_TYPE)) {
      HasSubordinates oldParent = ACE2004Task.getMentionParent(subAnnot);
      if (oldParent == superAnnot)
        return true;
      if (oldParent != null && ! oldParent.removeSubordinate(subAnnot))
        return false;
      
      // Another issue: when adding a relation mention to a relation, the
      // arg1&2 values must match. Currently we only allow adding rmention to
      // relation in 3 ways:
      //  1) new relation adding new, arg-less mention.
      //  2) new relation adding existing arg-having mention
      //  3) by algorithm, which has already checked the args to be equal
      // Here, in case 2 we set the relation args, to the value of the mention
      // args, and I'm putting a sanity check in, in case another unchecked
      // mechanism is added later.
      AWBAnnotation em1 = (AWBAnnotation) subAnnot.getAttributeValue("arg1");
      AWBAnnotation em2 = (AWBAnnotation) subAnnot.getAttributeValue("arg2");

      // RK 2/6/07 -- if the entity mention is not set in the relation mention
      // it inherits it from its oldParent
      AWBAnnotation emp1, emp2;
      if (em1 == null) 
        emp1 = (AWBAnnotation) oldParent.getAttributeValue("arg1");
      else 
        emp1 = ACE2004Task.getMentionParent(em1);
      if (em2 == null)
        emp2 = (AWBAnnotation) oldParent.getAttributeValue("arg2");
      else
        emp2 = ACE2004Task.getMentionParent(em2);

      AWBAnnotation e1 = (AWBAnnotation) superAnnot.getAttributeValue("arg1");
      AWBAnnotation e2 = (AWBAnnotation) superAnnot.getAttributeValue("arg2");
      // also deals with symmetric, though doesn't validate
      if ((e1 != null && ! (e1.equals(emp1) || e1.equals(emp2))) ||
          (e2 != null && ! (e2.equals(emp2) || e2.equals(emp1)))) {
        // can't set the r-mention's e-mention if e has multiple... so punt
        if (e1 != emp1) {
          System.err.println("Relation args don't match Relation: "+
                             IdRenderer.getShortId(superAnnot, doc)+" arg1="+
                             IdRenderer.getShortId(e1, doc)+",arg2="+
                             IdRenderer.getShortId(e2, doc)+
                             "\n                  Relation-Mention: "+
                             IdRenderer.getShortId(subAnnot, doc)+".arg1="+
                             IdRenderer.getShortId(em1, doc)+"("+
                             IdRenderer.getShortId(emp1, doc)+"), arg2="+
                             IdRenderer.getShortId(em2, doc)+"("+
                             IdRenderer.getShortId(emp2, doc)+")");
          boolean added = oldParent.addSubordinate(subAnnot);
          System.err.println("Adding to old parent: "+added);
          return false;
        }
      }
      if (e1 == null && emp1 != null) {
        try {
          superAnnot.setAttributeValue("arg1", emp1);
        } catch (Exception x) {}
      }
      if (e2 == null && emp2 != null) {
        try {
          superAnnot.setAttributeValue("arg2", emp2);
        } catch (Exception x) {}
      }
    }
    
    if (DEBUG > 0)
      System.err.println ("SA.addSub2Super: "+subAnnot.getId()+
                          " into "+superAnnot.getId());

    if (superAnnot.addSubordinate (subAnnot)) {
      // finally update the relations of this mentions relation-mentions to use
      // the new entity (only relevant for ENTITY subassignments

      Iterator entries = relationsToUpdate.entrySet().iterator();
      while (entries.hasNext()) {
        Map.Entry entry = (Map.Entry) entries.next();
        AWBAnnotation relation = (AWBAnnotation) entry.getKey();
        try {
          relation.setAttributeValue((String)entry.getValue(), superAnnot);
        } catch (Exception x) {}
      }
      return true;
    }
    return false;
  }
  
  /***********************************************************************/
  /* Implementing the AnnotationModelListener Interface */
  /***********************************************************************/

  /** Invoked after an annotation has been created. */
  public void annotationCreated (AnnotationModelEvent e) {
  }
  
  /** Invoked after an annotation has been deleted. */
  public void annotationDeleted (AnnotationModelEvent e) {
  }
  
  /** Invoked after an annotation has been changed. */
  public void annotationChanged (AnnotationModelEvent e) {
  }
  
  /** Invoked after an annotation has had subannotations added. */
  public void annotationInserted (AnnotationModelEvent e) {
  }
  
  /** Invoked after an annotation has had subannotations removed. */
  public void annotationRemoved (AnnotationModelEvent e) {
  }

  
  /***********************************************************************/
  /* MOUSE LISTENERS */
  /***********************************************************************/

  /**
   * This class listens for 'popup triggers' on Tables and configures the
   * context menu for it. This listener is not thread safe, but as long as
   * it's used with the GUI thread, you're fine.
   */
  private class SubAssignorPopupListener extends AnnotationPopupListener {

    private int DEBUG = 0;
    
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
      AWBDocument awbDoc = (AWBDocument) doc.getAnnotationModel();
        
      if (state == SAState.READY) {
        /* empty */
        
      } else if (state == SAState.ADJUSTING) {
        /* empty */      // what's going on?
        
      } else if ((state == SAState.SELECT_DESTINATION_ENTITY) ||
                 (state == SAState.SELECT_DESTINATION_ENTITY_PRIMARY)) {

	// if an entity annot has been selected, go on,
	// if a mention annot has been selected, find its entity
	// otherwise skip this event
        AWBAnnotation annot = e.getAnnotation ();
        if (annot == null)
          return;
        
	SubordinateSetsAnnotation entity = null;
	if (DEBUG > 1) 
	  System.err.println ("SA.mClicked: annot = " + annot +
			      "type = " + annot.getAnnotationType());
        if (annot.getAnnotationType ().equals (ACE2004Utils.ENTITY_TYPE)) {
	  entity = (SubordinateSetsAnnotation) annot;
	} else if (annot.getAnnotationType().equals 
		   (ACE2004Utils.ENTITY_MENTION_TYPE)) {
	  entity = (SubordinateSetsAnnotation) task.getMentionParent(annot);
	} else {
          return;
        }

        if (entity == null) {
	  GUIUtils.showWarning ("Select an Entity (or Mention of an Entity)");
          return;
        }

        addSubAnnotsToSuper (selectedAnnots, entity, awbDoc);

        // if the "primary" version was used, selectedAnnots must have exactly
        // one entry -- set it to be the primary mention for the entity now
        if (state == SAState.SELECT_DESTINATION_ENTITY_PRIMARY) {
        
          AWBAnnotation primary = 
            (AWBAnnotation) selectedAnnots.iterator().next();
          AWBAnnotation oldPrimary = (AWBAnnotation)
            entity.getAttributeValue("primary-mention");
          try {
            entity.setAttributeValue("primary-mention", primary);
          } catch (Exception x) {
            // should not happen
            return;
          }
          // if that worked, due to a bug in jATLAS, the oldPrimary is
          // still listed as a subordinate in the entity, but does not
          // have the entity as a referentElement anymore, so getParent
          // doesn't work!  So HACK: remove from entity and add it back
          // in.
          if (oldPrimary != null) {
            entity.removeSubordinate(oldPrimary);
            entity.addSubordinate(oldPrimary);
          }
        }

        // force reselection of annots.
        doc.unselectAllAnnotations ();
        doc.selectAnnotation (entity);

	e.consume();
        
        setState (SAState.READY);

      } else if (state == SAState.SELECT_DESTINATION_EVENT) {
        
	  // if an event annot has been selected, go on,
	// if a mention annot has been selected, find its entity
	// otherwise skip this event
        AWBAnnotation annot = e.getAnnotation ();
        if (annot == null)
          return;
        
	AWBAnnotation event = null;
	if (DEBUG > 1) 
	  System.err.println ("SA.mClicked: annot = " + annot +
			      "type = " + annot.getAnnotationType());
        if (annot.getAnnotationType ().equals (ACE2004Utils.EVENT_TYPE)) {
	  event = annot;
	} else if (annot.getAnnotationType().equals 
		   (ACE2004Utils.EVENT_MENTION_TYPE)) {
	  event = task.getMentionParent(annot);
	} else {
          return;
        }

        if (event == null) {
	  GUIUtils.showWarning ("Select an Event (or Mention of an Event)");
          return;
        }
        addSubAnnotsToSuper (selectedAnnots, (HasSubordinates) event, awbDoc);

        // force reselection of annots.
        doc.unselectAllAnnotations ();
        doc.selectAnnotation (event);

	e.consume();
        
        setState (SAState.READY);
        /*
      } else if (state == SAState.SELECT_DESTINATION_RELATION_RELTIME ||
		 state == SAState.SELECT_DESTINATION_RELATION_TIME_RANGE) {
        
        // if a mention relation annot has been selected, go on,
	// otherwise skip this event
        AWBAnnotation rel = e.getAnnotation ();
        if (rel == null)
          return;
        
	if (DEBUG > 1) 
	  System.err.println ("SA.mClicked: annot = " + rel +
			      "type = " + rel.getAnnotationType());
        if (!rel.getAnnotationType ().equals (ACE2004Utils.RELATION_MENTION_TYPE))
          return;

        addSubAnnotsToSuper (selectedAnnots, (HasSubordinates) rel, doc);
        
        // force reselection of annots.
        doc.unselectAllAnnotations ();
        doc.selectAnnotation (rel);
        
        setState (SAState.READY);
        */
        
      } else if (state == SAState.SELECT_DESTINATION_RELATION_ENTITY_ARG1 ||
                 state == SAState.SELECT_DESTINATION_RELATION_ENTITY_ARG2 ||
                 state == SAState.SELECT_DEST_UNMENT_RELATION_ENTITY_ARG1 ||
                 state == SAState.SELECT_DEST_UNMENT_RELATION_ENTITY_ARG2) {
        
        // make _sure_ something was selected
	AWBAnnotation relation = e.getAnnotation();
        if (relation == null)
          return;

        // set up a string for the desired attribute
        String attr;
        if (state == SAState.SELECT_DESTINATION_RELATION_ENTITY_ARG1 ||
            state == SAState.SELECT_DEST_UNMENT_RELATION_ENTITY_ARG1)
          attr = "arg1";
        else
          attr = "arg2";

        // if this is one of the UNMENT versions, confirm that this is
        // an unmentioned relation
        if (state == SAState.SELECT_DEST_UNMENT_RELATION_ENTITY_ARG1 ||
            state == SAState.SELECT_DEST_UNMENT_RELATION_ENTITY_ARG2) {
          if (!ACE2004Task.isUnmentionedRelation(relation)) {
            return;
          }
        } else {
          // if it's a relation mention, get the parent relation, because
          // that is where Entity arguments get put
          AWBAnnotation relMention = null;
          AnnotationType type = relation.getAnnotationType ();
          if (type.equals(ACE2004Utils.RELATION_MENTION_TYPE)) {
            relMention = relation;

            // if we are adding the entity arg to a relation mention, we
            // must clear out the relation mention's arg1 or arg2, if previously
            // set, so that the relation mention will refer up to its parent
            // relation to read the entity argument
            // when this happens, AnnotationChanged will be triggered and
            // the relation mention's parent may be changed to a new mention
            // SOOOO... we must do this here before we grab the parent relation
            try {
              relMention.setAttributeValue (attr, null);
            } catch (UnmodifiableAttributeException x) {}

            relation = ACE2004Task.getMentionParent(relMention);
            type = relation.getAnnotationType();
          }

          // TODO might want to allow selection via the extent in the
          // text window, in which case we need another conversion here

          if (! type.equals (ACE2004Utils.RELATION_TYPE)) 
            return;
        }

        // assert: selectedAnnots.length == 1
        AWBAnnotation entity =
          (AWBAnnotation) selectedAnnots.iterator().next();
        try {

          relation.setAttributeValue (attr, entity);

        } catch (UnmodifiableAttributeException x) {}
        
        // force reselection of annots.
        // for now select newly added entity
        // TODO: decide if selecting relation and using exchangers makes sense
        doc.unselectAllAnnotations ();
        doc.selectAnnotation (entity);
       
        setState (SAState.READY);

      } else if (state == SAState.SELECT_DESTINATION_RELATION_ARG1 ||
                 state == SAState.SELECT_DESTINATION_RELATION_ARG2) {
        
        // make _sure_ its a relation
	AWBAnnotation relation = e.getAnnotation();
        if (relation == null)
          return;

        AnnotationType type = relation.getAnnotationType ();
	if (! type.equals (ACE2004Utils.RELATION_MENTION_TYPE)) 
          return;
        
        // assert: selectedAnnots.length == 1
        AWBAnnotation mention =
          (AWBAnnotation) selectedAnnots.iterator().next();
        try {
          if (state == SAState.SELECT_DESTINATION_RELATION_ARG1)
            relation.setAttributeValue ("arg1", mention);
          else
            relation.setAttributeValue ("arg2", mention);
        } catch (UnmodifiableAttributeException x) {}
        
        // force reselection of annots.
        doc.unselectAllAnnotations ();
        doc.selectAnnotation (relation);
       
        setState (SAState.READY);
        
	/*
      } else if (state == SAState.SELECT_DESTINATION_RELATION_MENTION) {
        
	  if (DEBUG > 0)
	      System.err.println("state = SELECT_DESTINATION_RELATION_MENTION");
	  // make _sure_ its a relation
	  HasSubordinates relMention = (HasSubordinates) e.getAnnotation();
	  if (relMention == null)
	      return;

	  if (DEBUG > 0)
	      System.err.println("annot is non null");

	  AnnotationType type = relMention.getAnnotationType ();
	  if (! type.equals (ACE2004Utils.RELATION_MENTION_TYPE)) 
	      return;
        
	  if (DEBUG > 0)
	      System.err.println("annot is of type relMention; relMention = " + relMention);

	  // assert: selectedAnnots.length == 1
	  TextExtentRegion selectedExtentRegion =
	      (TextExtentRegion) selectedAnnots.iterator().next();
	  // Steal the start/end values from the "mention" annotation
	  if (DEBUG > 0)
	      System.err.println("selectedExtentRegion = " + selectedExtentRegion);
	  TextExtentRegion relMentExtent =
	      (TextExtentRegion) relMention.getAttributeValue("relation-mention-extent");
	  if (DEBUG > 0)
	      System.err.println("relMentExtent = " + relMentExtent);
	  relMentExtent.setTextExtents(selectedExtentRegion.getTextExtentStart(),
				       selectedExtentRegion.getTextExtentEnd());
	  if (DEBUG > 0)
	      System.err.println("selectedExtentRegion.getTextExtentEnd() = " + selectedExtentRegion.getTextExtentEnd());
	  // Since the current relation mention has now obtained the new text extent,
	  // go ahead and delete the mention annotation.
	  doc.deleteAnnotation(selectedExtentRegion);

	  // force reselection of annots.
	  doc.unselectAllAnnotations ();
	  doc.selectAnnotation (relMention);
       
	  setState (SAState.READY);
	*/
        
      } else if (state == SAState.ASSIGN_EVENT_MENTION_EXTENT) {
        
        AWBAnnotation annot = e.getAnnotation ();
        if (annot == null)
          return;
        
	if (DEBUG > 1) 
	  System.err.println ("SA.mClicked: annot = " + annot +
			      "type = " + annot.getAnnotationType());
        AnnotationType type = annot.getAnnotationType();
        if (! type.equals(ACE2004Utils.EVENT_MENTION_TYPE)) {
          return;
        }

        // assert: selectedAnnots.length == 1
        AWBAnnotation extent = (AWBAnnotation)selectedAnnots.iterator().next();
        try {
          annot.setAttributeValue("extent", extent);
        } catch (UnmodifiableAttributeException x) {}
        
        // force reselection of annots.
        doc.unselectAllAnnotations ();
        doc.selectAnnotation (annot);

	e.consume();
        
        setState (SAState.READY);
        
      } else if (state == SAState.ASSIGN_RELATION_MENTION_EXTENT) {
        
        AWBAnnotation annot = e.getAnnotation ();
        if (annot == null)
          return;
        
	if (DEBUG > 1) 
	  System.err.println ("SA.mClicked: annot = " + annot +
			      "type = " + annot.getAnnotationType());
        AnnotationType type = annot.getAnnotationType();
        if (! type.equals(ACE2004Utils.RELATION_MENTION_TYPE)) {
          return;
        }

        // assert: selectedAnnots.length == 1
        AWBAnnotation extent = (AWBAnnotation)selectedAnnots.iterator().next();
        try {
	    if (DEBUG > 0) {
		System.err.println("DSD: setting extent attribute on annot = " + annot);
		System.err.println("DSD: extent = " + extent);
	    }
	    // annot.setAttributeValue("start",extent.getAttributeValue(
	    annot.setAttributeValue("extent", extent);
        } catch (UnmodifiableAttributeException x) {}
        
        // force reselection of annots.
        doc.unselectAllAnnotations ();
        doc.selectAnnotation (annot);

	e.consume();
        
        setState (SAState.READY);
        
      } else if (state == SAState.SELECT_ARGUMENT_MENTION_VALUE) {
        
        // make sure the value is mention type
        AWBAnnotation value = e.getAnnotation ();
        AnnotationType type = value.getAnnotationType();
        if (value == null ||
            ! (type.equals(ACE2004Utils.QUANTITY_MENTION_TYPE) ||
               type.equals(ACE2004Utils.ENTITY_MENTION_TYPE))) {
          return;
        }

	if (DEBUG > 1) 
	  System.err.println ("SA.mClicked: value = " + value.getId() +
			      "type = " + value.getAnnotationType().getName());

        // assert: selectedAnnots.length == 1
        AWBAnnotation argument = (AWBAnnotation)selectedAnnots.iterator().next();
        try {
          if (type.equals(ACE2004Utils.QUANTITY_MENTION_TYPE)) {
            argument.setAttributeValue("quantity-value", value);
            argument.setAttributeValue("entity-value", null);
            argument.setAttributeValue("timex2-value", null);
          } else if (type.equals(ACE2004Utils.TIMEX2_TYPE)) {
	      argument.setAttributeValue("quantity-value", null);
	      argument.setAttributeValue("timex2-value", value);
	      argument.setAttributeValue("entity-value", null);
          } else {
            argument.setAttributeValue("quantity-value", null);
            argument.setAttributeValue("timex2-value", null);
            argument.setAttributeValue("entity-value", value);
          }
        } catch (UnmodifiableAttributeException x) {}
        
        // force reselection of annots.
        doc.unselectAllAnnotations();
        AWBAnnotation parent =
          ACE2004Task.getParent(argument, ACE2004Task.RELATION_MENTION_TYPE_NAME);
        if (parent == null)
          parent = ACE2004Task.getParent(argument,
                                         ACE2004Task.EVENT_MENTION_TYPE_NAME);
        if (parent != null)
          doc.selectAnnotation(parent);

	e.consume();
        
        setState (SAState.READY);
        
      } else if (state == SAState.ADD_ARGUMENT_TO_ANNOT ||
                 state == SAState.ADD_PARENT_ENTITY_ARGUMENT_TO_ANNOT) {

        AWBAnnotation augmentee = e.getAnnotation ();
        if (augmentee == null)
          return;

        // make sure we've the correct target!
        AnnotationType augmenteeType = augmentee.getAnnotationType();

        if (augmenteeType.equals(ACE2004Utils.EVENT_TYPE) ||
            augmenteeType.equals(ACE2004Utils.RELATION_TYPE)) {
          // Be kind: if the selected annot has only one mention, go ahead and
          // augment it with a warning.
          AWBAnnotation[] subs;
          if (augmenteeType.equals(ACE2004Utils.EVENT_TYPE)) {
            subs = ((HasSubordinates) augmentee)
              .getSubordinates(ACE2004Utils.EVENT_MENTION_TYPE);
          }
          else {
            subs = ((HasSubordinates) augmentee)
              .getSubordinates(ACE2004Utils.RELATION_MENTION_TYPE);
          }
          StringBuffer msg = new StringBuffer()
            .append("Arguments can not be added directly to an ")
            .append(augmenteeType.getName()).append(".\n");

          if (subs.length != 1) {
            msg.append("Choose one of its mentions.");
            GUIUtils.showWarning (msg.toString());
            return;
          }

          msg.append("The selected ").append(augmenteeType.getName());

          augmentee = subs[0];
          augmenteeType = augmentee.getAnnotationType();

          msg.append(" has only 1 mention")
            .append(".\nThe Argument will be added to the ")
            .append(augmenteeType.getName()).append(".");
          GUIUtils.showWarning(msg.toString());
        }
        if (! (augmenteeType.equals(ACE2004Utils.EVENT_MENTION_TYPE) ||
               augmenteeType.equals(ACE2004Utils.RELATION_MENTION_TYPE)))
          return;
        
        // assert: selectedAnnots.length == 1
        AWBAnnotation value = (AWBAnnotation)selectedAnnots.iterator().next();
        AnnotationType valueType = value.getAnnotationType();

        if (state == SAState.ADD_PARENT_ENTITY_ARGUMENT_TO_ANNOT) {
          if (valueType.equals(ACE2004Utils.ENTITY_MENTION_TYPE)) {
            // should always be true for the PARENT_ENTITY version of the 
            // SAState
            // need to make the "value" annot be the parent entity
            value = ACE2004Task.getMentionParent(value);
            valueType = value.getAnnotationType();
            System.err.println("SA.mClicked: foo Using parent of selected value" +
                               " - parent = " + value);
            if (DEBUG > 0)
              System.err.println("SA.mclicked Debug is on");

          }
        }
            

        // Sanity check
        if (! (valueType.equals(ACE2004Utils.QUANTITY_MENTION_TYPE) ||
               valueType.equals(ACE2004Utils.TIMEX2_TYPE) ||
               valueType.equals(ACE2004Utils.ENTITY_TYPE) ||
               valueType.equals(ACE2004Utils.ENTITY_MENTION_TYPE)))
          throw new RuntimeException("Invalid value to be augmenting with");

        if (DEBUG > 0)
          System.err.println("SA.mClick.ADD_ARG: "+
                             "aug="+augmentee.getAttributeValue("ace_id")+":"+
                             augmenteeType.getName()+":"+augmentee.getId()+
                             ":"+augmentee.getAttributeValue("TextExtent")+
                             "\n                   "+
                             "val="+value.getAttributeValue("ace_id")+":"+
                             valueType.getName()+":"+value.getId()+
                             ":"+value.getAttributeValue("TextExtent"));
        
        /****************
        if (valueType.equals(ACE2004Utils.ENTITY_TYPE)) {
          AWBAnnotation argumentAnnot =
            doc.createAnnotation(ACE2004Utils.ARGUMENT_TYPE);
          try {
            argumentAnnot.setAttributeValue("entity-value", value);
          } catch (UnmodifiableAttributeException x) {
            System.err.println("SA.mClick.ADD_ARG: setting arg value");
          }
          // entity argument gets added to the parent Event or Relation
          augmentee = (HasSubordinates)ACE2004Task.getMentionParent(augmentee);
          ((HasSubordinates)augmentee).addSubordinate(argumentAnnot);
        } else {
        **************************/
          AWBAnnotation argumentAnnot =
            doc.createAnnotation(ACE2004Utils.ARGUMENT_MENTION_TYPE);
          // set the value!
          try {
	    if (valueType.getName().startsWith("ace_quantity")) {
              argumentAnnot.setAttributeValue("quantity-value", value);
	    } else if (valueType.getName().startsWith("timex2")) {
              argumentAnnot.setAttributeValue("timex2-value", value);
            } else if (valueType.equals(ACE2004Utils.ENTITY_TYPE)) {
              argumentAnnot.setAttributeValue("unmentioned-entity-value",
                                              value);
	    } else {
              argumentAnnot.setAttributeValue("entity-value", value);
	    }
          } catch (UnmodifiableAttributeException x) {
            System.err.println("SA.mClick.ADD_ARG: setting arg value");
          }
          // Grrr!  foiled by event-mention not being HasSubordinates again!
          if (! (augmentee instanceof HasSubordinates)) {
            // no event is fired :(
            augmentee.getRegion().addToSubordinateSet(argumentAnnot);
          } else {
            ((HasSubordinates)augmentee).addSubordinate(argumentAnnot);
          }
          /**
        }    
          **/    
        // force reselection of annots.
        doc.unselectAllAnnotations();
	if (DEBUG > 0)
	    System.err.println(" selecting annot augmentee = " + augmentee);
        doc.selectAnnotation(augmentee);

	e.consume();
        
        setState (SAState.READY);

      } else {
        if (DEBUG > 1)
          System.err.println ("SA.mClicked: no state");
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
            if (state == SAState.ASSIGN_EVENT_MENTION_EXTENT ||
		state == SAState.ASSIGN_RELATION_MENTION_EXTENT) {
              // assert: selectedAnnots.length == 1
              AWBAnnotation extent =
                (AWBAnnotation) selectedAnnots.iterator().next();
              
              JawbDocument doc = GUIUtils.getJawbDocument(e);
              doc.deleteAnnotation(extent);
            }
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
      super.setVisible (visible);
      setModal (visible);
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
    public static final SAState SELECT_DESTINATION_ENTITY =
      new SAState ("Select Entity");
    public static final SAState SELECT_DESTINATION_ENTITY_PRIMARY =
      new SAState ("Select Entity");
    public static final SAState SELECT_DESTINATION_EVENT =
      new SAState ("Select Event");
    public static final SAState SELECT_DESTINATION_RELATION_ARG1 =
      new SAState ("Select Relation for Arg1");
    public static final SAState SELECT_DESTINATION_RELATION_ARG2 =
      new SAState ("Select Relation for Arg2");
    // adding Entity arguments to Relations (possibly unmentioned)
    public static final SAState SELECT_DESTINATION_RELATION_ENTITY_ARG1 =
      new SAState ("Select Relation for Entity Arg1");
    public static final SAState SELECT_DESTINATION_RELATION_ENTITY_ARG2 =
      new SAState ("Select Relation for Entity Arg2");
    // adding Entity arguments to Relations (must be unmentioned)
    public static final SAState SELECT_DEST_UNMENT_RELATION_ENTITY_ARG1 =
      new SAState ("Select Unmentioned Relation for Entity Arg1");
    public static final SAState SELECT_DEST_UNMENT_RELATION_ENTITY_ARG2 =
      new SAState ("Select Unmentioned Relation for Entity Arg2");
    public static final SAState SELECT_DESTINATION_RELATION_MENTION =
      new SAState ("Select Relation Mention for Text Extent");
    public static final SAState SELECT_DESTINATION_RELATION_ARGUMENT =
      new SAState ("Select Relation Argument");
    public static final SAState ASSIGN_EVENT_MENTION_EXTENT =
      new SAState ("Select Event Mention");
    public static final SAState ASSIGN_RELATION_MENTION_EXTENT =
      new SAState ("Select Relation Mention");
    public static final SAState SELECT_ARGUMENT_MENTION_VALUE =
      new SAState ("Select an Entity Mention, Value Mention or Timex2");
    public static final SAState ADD_ARGUMENT_TO_ANNOT =
      new SAState ("Select an Event Mention or Relation Mention");
    public static final SAState ADD_PARENT_ENTITY_ARGUMENT_TO_ANNOT =
      new SAState ("Select an Event Mention or Relation Mention");

    private String name;
    private SAState (String name) {
      this.name = name;
    }
    public String toString () {
      return name;
    }
  }
}
