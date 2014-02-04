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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import javax.swing.JOptionPane;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import javax.swing.JTextPane;

import gov.nist.atlas.type.AnnotationType;
import gov.nist.atlas.ref.AnnotationRef;
import gov.nist.atlas.Annotation;
import gov.nist.atlas.util.ATLASElementSet;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.atlas.*;
import org.mitre.jawb.atlas.AnnotationModelEvent.AnnotationChange;
import org.mitre.jawb.gui.*;
import org.mitre.jawb.tasks.*;
import org.mitre.jawb.swing.ItemExchanger;
import org.mitre.jawb.prefs.Preferences;

import org.mitre.ace2004.callisto.SubordinateAssignor.SAState;

// import org.mitre.ace2004.callisto.ACE2004MainTextPane;

/**
 * A TaskToolKit is a pluggable module that allows for differnt editing
 * mechanisms for each of the tasks Callisto may use. All the gui specific
 * stuff is here.
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */
public class ACE2004ToolKit extends AbstractToolKit {

  public static final int DEBUG = 0;

  /* ID determination types for relation coreference */
  public static final int NEW_ID = 0;
  public static final int COMBINE_ID = 1;
  public static final int EXISTING_ID = 2;

  static ACE2004AnnotationListener annotListener;
  static ACE2004Task task;

  final ItemExchanger mention2Parent = new Mention2Parent();

  final ItemExchanger relationExtent2Mention = new RelationExtent2Mention();
  final ItemExchanger mention2Relations = new Mention2Relations();
  final ItemExchanger entity2Mentions = new Entity2Mentions();
  final ItemExchanger entity2Relations = new Entity2Relations();
  final ItemExchanger relation2Mentions = new Relation2Mentions();
  final ItemExchanger relation2Entities = new Relation2Entities();
  final ItemExchanger unmentionedRelation2Entities = 
    new UnmentionedRelation2Entities();
  final ItemExchanger relation2Lexicals = new Relation2Lexicals();
  final ItemExchanger eventLexicals2Event = new EventLexicals2Event();
  final ItemExchanger event2EventLexicals = new Event2EventLexicals();
  final ItemExchanger eventLexicals2Lexicals = new EventLexicals2Lexicals();
  final ItemExchanger argMention2Event = new ArgMention2Event();
  final ItemExchanger argMention2EventMention = new ArgMention2EventMention();
  final ItemExchanger argMention2Relation = new ArgMention2Relation();
  final ItemExchanger argMention2RelationMention = new ArgMention2RelationMention();
  final ItemExchanger argMention2Lexicals = new ArgMention2Lexicals();
  final ItemExchanger quantity2Lexicals = new Quantity2Lexicals();
  
  ACE2004ToolKit (ACE2004Task task) {
    synchronized (task) {
      if (this.task == null) {
        this.task = task;
        annotListener = new ACE2004AnnotationListener(task);
      }
    }
  }

  private MainTextPane mainComponent = null;
    // private ACE2004MainTextPane mainComponent = null;
  private DetachableTabsJawbComponent editorComponent = null;
  private SubordinateAssignor subAssignor = null;
  private AnnotationModelListener annotModel = null;
  private Set actions = null;

  private Timex2Table timex2Table;
  

  public Task getTask () {
    return task;
  }
  
  /**
   * Returns null, indicating this task uses the default main component.
   */
  public JawbComponent getMainComponent () {
    if (mainComponent == null) {
      mainComponent = new MainTextPane (this);
      mainComponent.setSelectedAnnotationExchanger
        (ACE2004Utils.RELATION_MENTION_TYPE, relation2Lexicals);
      /*
      mainComponent.setSelectedAnnotationExchanger
        (ACE2004Utils.RELATION_MENTION_EXTENT_TYPE, relation2Lexicals);
      */
      mainComponent.setSelectedAnnotationExchanger
        (ACE2004Utils.ENTITY_TYPE, entity2Mentions);
      mainComponent.setSelectedAnnotationExchanger
        (ACE2004Utils.EVENT_TYPE, event2EventLexicals);
      mainComponent.setSelectedAnnotationExchanger
        (ACE2004Utils.EVENT_MENTION_TYPE, eventLexicals2Lexicals);
      mainComponent.setSelectedAnnotationExchanger
        (ACE2004Utils.EVENT_MENTION_EXTENT_TYPE, eventLexicals2Lexicals);
      mainComponent.setSelectedAnnotationExchanger
        (ACE2004Utils.ARGUMENT_MENTION_TYPE, argMention2Lexicals);
      mainComponent.setSelectedAnnotationExchanger
        (ACE2004Utils.QUANTITY_TYPE, quantity2Lexicals);
    }
    return mainComponent;
  }
  
  /**
   * Returns the JawbComponent to edit this tasks annotations. Always the same
   * object for a given ACE2004EditorKit.
   */
  public JawbComponent getEditorComponent () {
    if (editorComponent == null)
      initEditorComponent ();
    return editorComponent;
  }

  /**
   * Some of these actions are tied to the SubordinateAssignor.
   */
  public Set getActions () {
    if (actions == null) { // lazy aren't we
      initActions ();
      if (editorComponent == null)
	initEditorComponent (); // some actions use subAssignor inited by this
    }
    return actions;
  }

  /*
   *
   */
  public boolean deleteAnnotation (AWBAnnotation annot, JawbDocument jdoc) {
    AWBDocument doc = (AWBDocument) jdoc.getAnnotationModel();
    AnnotationType type = annot.getAnnotationType ();
    if (DEBUG > 0)
      System.err.println ("ACE2004tk.delete: "+annot.getId()+
                          " type="+type.getName());
    
    if (type.equals (ACE2004Utils.ENTITY_TYPE))
      return ACE2004Utils.deleteEntity (annot, doc);
    else if (type.equals (ACE2004Utils.ENTITY_MENTION_TYPE))
      return ACE2004Utils.deleteEntityMention (annot, doc);
    else if (type.equals (ACE2004Utils.RELATION_TYPE))
      return ACE2004Utils.deleteRelation (annot, doc);
    else if (type.equals (ACE2004Utils.RELATION_MENTION_TYPE))
      return ACE2004Utils.deleteRelationMention (annot, doc);
    else if (type.equals (ACE2004Utils.RELATION_MENTION_EXTENT_TYPE))
      return ACE2004Utils.deleteRelationMentionExtent (annot, doc);

    // The only way to *delete* relation-mention-extents is to either
    // *replace* it with another value, or to delete relation mention.
    //
    // else if (type.equals (ACE2004Utils.RELATION_MENTION_EXTENT_TYPE))
    //   return ACE2004Utils.deleteRelationMentionExtent (annot, doc);

    else if (type.equals (ACE2004Utils.QUANTITY_TYPE))
      return ACE2004Utils.deleteQuantity(annot, doc);
    else if (type.equals (ACE2004Utils.QUANTITY_MENTION_TYPE))
      return ACE2004Utils.deleteQuantityMention(annot, doc);
    else if (type.equals (ACE2004Utils.EVENT_TYPE))
      return ACE2004Utils.deleteEvent(annot, doc);
    else if (type.equals (ACE2004Utils.TIMEX2_TYPE))
      return ACE2004Utils.deleteTimex2(annot, doc);
    else if (type.equals (ACE2004Utils.EVENT_MENTION_TYPE))
      return ACE2004Utils.deleteEventMention(annot, doc);
    else if (type.equals (ACE2004Utils.EVENT_MENTION_EXTENT_TYPE))
      return ACE2004Utils.deleteEventMentionExtent(annot, doc);
    else if (type.equals (ACE2004Utils.ARGUMENT_TYPE))
      return ACE2004Utils.deleteArgument(annot, doc);
    else if (type.equals (ACE2004Utils.ARGUMENT_MENTION_TYPE))
      return ACE2004Utils.deleteArgumentMention(annot, doc);

    throw new RuntimeException("Deletion of "+type.getName()+
                               " not yet implemented");
  }

  private void initEditorComponent() {
    editorComponent = new DetachableTabsJawbComponent (ACE2004Task.TASK_NAME) {
        public void setJawbDocument (JawbDocument doc) {
          JawbDocument old = getJawbDocument();
          if (old != null)
            old.getAnnotationModel().removeAnnotationModelListener(annotListener);
          
          super.setJawbDocument (doc);
	  subAssignor.setJawbDocument (doc);

          // make sure it's got an Id Tracker.
          // TODO: better would be to have a 'newDocument' hook in Callisto
          if (doc != null) {
            IdTracker.getIdTracker ((AWBDocument) doc.getAnnotationModel());
            doc.getAnnotationModel().addAnnotationModelListener(annotListener);
          }
        }
      };
    editorComponent.add ("Entities", new EntityEditor (this));
    editorComponent.add ("Entity Mentions", new MentionEditor (this));
    editorComponent.add ("Relation Mentions", new RelationEditor (this));
    editorComponent.add ("Events", new EventEditor (this));
    editorComponent.add ("Event Mentions", new EventMentionEditor (this));
    editorComponent.add ("Values", new QuantityEditor (this));
    editorComponent.add ("Timex2", new Timex2Table(this));
    editorComponent.add ("Special Relations", 
                         new UnmentionedRelationEditor (this));
    // editorComponent.add ("Relation Mention Extents", new RelMenExtentEditor(this));
    
    // actions connecting the editors
    subAssignor = new SubordinateAssignor(getMainComponent().getComponent(), this);
  }
  
  /***********************************************************************/
  /* Actions */
  /***********************************************************************/

  /* Currently all state-full actions which require secondary steps are
   * non-static inner-classes of the Toolkit, since they need reference to a
   * something which maintains that state between them (the
   * SubordinateAssignor)
   */


  /** */
  private void initActions () {
    
    actions = new LinkedHashSet ();
    // Annotation creators:
    actions.add (new CreateTextAnnotAction ("New Mention", task,
                                            ACE2004Utils.ENTITY_MENTION_TYPE,
                                            "mention.head"));
    actions.add (new CreateMentionAndEntityAction (true));
    actions.add (new CreateMentionAndEntityAction (false));

    actions.add (new CreateRelationMentionExtentAndRelationAction (true));
    actions.add (new CreateRelationMentionExtentAndRelationAction (false));

    actions.add (new CreateEventMentionAndEventAction (true));
    actions.add (new CreateEventMentionAndEventAction (false));
    actions.add (new CreateEventMentionExtentAction());

    actions.add (new CreateTextAnnotAction("New Value Mention", task,
                                           ACE2004Utils.QUANTITY_MENTION_TYPE,
                                           "quantity"));

    actions.add (new CreateTextAnnotAction("New Timex2", task,
                                           ACE2004Utils.TIMEX2_TYPE,
                                           "timex2"));


    // Mention modifiers
    actions.add (new Mentions2EntityAction (true));
    actions.add (new Mentions2EntityAction (false));
    // DSD 2005/07/28: actions.add (new AddMentionToRelationMentionAction (true, "arg1"));
    actions.add (new PrimaryMention2EntityAction ());
    actions.add (new Mention2PrimaryAction());

    actions.add (new AddMentionToRelationMentionAction (false, "arg1"));
    actions.add (new AddMentionToRelationMentionAction (false, "arg2"));

    // Entities as subordinates to Relations
    actions.add (new AddEntityToRelationAction (task, true, "arg1"));
    actions.add (new AddEntityToRelationAction (task, false, "arg1"));
    actions.add (new AddEntityToRelationAction (task, false, "arg2"));

    // Event modifiers
    actions.add (new EventMentions2EventAction (true));
    actions.add (new EventMentions2EventAction (false));
    
    // Relation-mention modifiers
    actions.add (new CreateSeparateRelationAction ());
    actions.add (new CoreferenceRelationMentionsAction());
    
    // argument creation actions
    actions.add (new SelectArgumentMentionValueAction());
    actions.add (new UseAsArgumentValueAction(ACE2004Utils.ENTITY_MENTION_TYPE));
    actions.add (new UseParentAsArgumentValueAction(ACE2004Utils.ENTITY_MENTION_TYPE));
    actions.add (new UseAsArgumentValueAction(ACE2004Utils.ENTITY_TYPE));
    actions.add (new UseAsArgumentValueAction(ACE2004Utils.QUANTITY_MENTION_TYPE));
    //    actions.add (new UseAsArgumentValueAction(ACE2004Utils.QUANTITY_TYPE));
    actions.add (new UseAsArgumentValueAction(ACE2004Utils.TIMEX2_TYPE));

    // values work
    actions.add (new SetSavedValueAction (ACE2004Utils.QUANTITY_TYPE));
    // timex2 work
    actions.add (new SetSavedValueAction (ACE2004Utils.TIMEX2_TYPE));

    // autotag -- with and without query mode (RK aug 2007)
    actions.add (new AutotagAction(task, false));
    actions.add (new AutotagAction(task, true));

    // universal delete
    actions.add (new DeleteAnnotAction (this));

  }

  /**
   * Adds selected reltime to a relation; either a new one created from
   * scratch, or by changing state and adding to the next entity selected
   */
  class ValueMention2RelationAction extends AnnotationAction {
    public ValueMention2RelationAction () {
      super ("Add Value Mention to Relation Mention...", ACE2004Utils.QUANTITY_MENTION_TYPE);
      putValue (ACTION_COMMAND_KEY, "value-mention-2-relation-mention");
    }
    
    public void actionPerformed (ActionEvent e) {
      if (ACE2004ToolKit.this.DEBUG > 0)
        System.err.println ("ACE2004tk.R2RAct: waiting for relation choice.");
      
      Set selected = getSelectedAnnots (e);
      subAssignor.setState (SAState.SELECT_DESTINATION_RELATION_ARGUMENT,
                            ACE2004Utils.QUANTITY_MENTION_TYPE,
                            selected, getJawbDocument(e));
    }
  }

  /**
   * An action to create a new mention and either add it to an
   * existing entity or create a new entity for it according to the
   * value of createEntity in the constructor.
   *
   */
  class CreateMentionAndEntityAction extends CreateTextAnnotAction {

    boolean createEntity;
    
    public CreateMentionAndEntityAction (boolean createEntity) {
      super ((createEntity ? "New Mention for New Entity" :
	      "New Mention for Existing Entity..."), 
	     task, ACE2004Utils.ENTITY_MENTION_TYPE, "mention.head.assigned");
      if (createEntity)
        putValue (ACTION_COMMAND_KEY, "createTextAnnot-mention-and-entity");
      else
        putValue (ACTION_COMMAND_KEY, "createTextAnnot-mention-to-entity");
      this.createEntity = createEntity;
    }

    public void actionPerformed (ActionEvent e) {
      AWBAnnotation mention = super.createAnnotation(e);

      JawbDocument doc = GUIUtils.getJawbDocument(e);
      AWBDocument awbDoc = (AWBDocument) doc.getAnnotationModel();
        
      if (createEntity) {
	HasSubordinates entity = (HasSubordinates)
	  doc.createAnnotation(ACE2004Utils.ENTITY_TYPE);
	// if addition of mentions fails, remove entity
	if (! subAssignor.addSubAnnotsToSuper (Collections.singleton(mention),
					       entity, awbDoc) &&
	    entity.getSubordinates (ACE2004Utils.ENTITY_MENTION_TYPE).length == 0) {
	  doc.deleteAnnotation (entity);
	} else {
	  // force reselection of annots.
	  doc.unselectAllAnnotations ();
	  doc.selectAnnotation (entity);
	}
	subAssignor.setState (SAState.READY);

      } else {
	subAssignor.setState (SAState.SELECT_DESTINATION_ENTITY,
			      ACE2004Utils.ENTITY_MENTION_TYPE,
			      Collections.singleton(mention),
			      doc);
      }
    }
  }


  /**
   * Adds selected mentions to an entity; either a new one created from
   * scratch, or by changing state and adding to the next entity selected
   */
  class Mentions2EntityAction extends AnnotationAction {
    SAState gotoState;
    public Mentions2EntityAction (boolean create) {
      super ("", ACE2004Utils.ENTITY_MENTION_TYPE);
      if (create) {
        putValue (NAME, "New Entity");
        putValue (ACTION_COMMAND_KEY, "mention-2-new-entity");
        gotoState = null;
      } else {
        putValue (NAME, "Add To Entity...");
        putValue (ACTION_COMMAND_KEY, "mention-2-entity");
        gotoState = SAState.SELECT_DESTINATION_ENTITY;
      }
    }
    
    public void actionPerformed (ActionEvent e) {
      if (ACE2004ToolKit.this.DEBUG > 0)
        System.err.println ("ACE2004tk.M2EAct: waiting for entity choice.");
      
      Set selected = getSelectedAnnots (e);
      
      JawbDocument doc = getJawbDocument(e);
      AWBDocument awbDoc = (AWBDocument) doc.getAnnotationModel();

      if (gotoState != null) {
        subAssignor.setState (gotoState, ACE2004Utils.ENTITY_MENTION_TYPE,
                              selected, doc);
        return;
      }

      HasSubordinates entity = (HasSubordinates)
        doc.createAnnotation (ACE2004Utils.ENTITY_TYPE);
      // if addition of all (or at least first) mentions fails, remove entity
      if (! subAssignor.addSubAnnotsToSuper (selected, entity, awbDoc) &&
          entity.getSubordinates (ACE2004Utils.ENTITY_MENTION_TYPE).length == 0) {
        doc.deleteAnnotation (entity);
      } else {
        // force reselection of annots.
        doc.unselectAllAnnotations ();
        doc.selectAnnotation (entity);
      }
      subAssignor.setState (SAState.READY);
    }
  }


  /***********************************************************************
   * methods to give user control of which mention is 
   * primary in an entity
   * RK 12/27/06
   ***********************************************************************/

  class PrimaryMention2EntityAction extends AnnotationAction {
    SAState gotoState;
    public PrimaryMention2EntityAction () {
      super ("Add To Entity as Primary...", ACE2004Utils.ENTITY_MENTION_TYPE, 1);
      putValue (ACTION_COMMAND_KEY, "primary-mention-2-entity");
      gotoState = SAState.SELECT_DESTINATION_ENTITY_PRIMARY;
    }
    
    public void actionPerformed (ActionEvent e) {
      if (ACE2004ToolKit.this.DEBUG > 0)
        System.err.println ("ACE2004tk.PE2CAct: waiting for entity choice.");
      
      Set selected = getSelectedAnnots (e);
      
      JawbDocument doc = getJawbDocument(e);
      if (gotoState != null) {
        subAssignor.setState (gotoState, ACE2004Utils.ENTITY_MENTION_TYPE, 
                              selected, doc);
        return;
      }
    }
  }


  class Mention2PrimaryAction
    extends ContextSensitiveAnnotationAction {

    public Mention2PrimaryAction () {
      super (ACE2004Utils.ENTITY_MENTION_TYPE, 1);
    }

    public boolean isValidForContext (Set annots) {
      // every annot in the set (should be only 1) must have an entity
      // superordinate
      Iterator annotsIter = annots.iterator();
      while (annotsIter.hasNext()) {
        AWBAnnotation annot = 
          (AWBAnnotation)annotsIter.next();
        SubordinateSetsAnnotation entity = (SubordinateSetsAnnotation)
          ACE2004Task.getParent(annot, ACE2004Task.ENTITY_TYPE_NAME);
        if (entity == null)
          return false;
      }
      return true;
    }
    
    public void setPropertiesForContext(Set annots) {
      putValue(Action.NAME, "Make Mention Primary in Current Entity");
    }

    public void actionPerformed (ActionEvent e) {
      Set selected = getSelectedAnnots (e);
      // must be one item selected
      AWBAnnotation annot = 
        (AWBAnnotation) selected.iterator().next();
      SubordinateSetsAnnotation entity = (SubordinateSetsAnnotation)
          ACE2004Task.getParent(annot, ACE2004Task.ENTITY_TYPE_NAME);
      if (entity == null) // should not happen
        return;
      
      AWBAnnotation oldPrimary = (AWBAnnotation)
        entity.getAttributeValue ("primary-mention");
      try {
        entity.setAttributeValue("primary-mention", annot);
      } catch (Exception x) {
        x.printStackTrace (); 
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
  }

  /***********************************************************************/

  // Changed name from AddMentionToRelationAction to
  // AddMentionToRelationMentionAction RK 12/29/06
  private class AddMentionToRelationMentionAction extends AnnotationAction {
    SAState gotoState;
    public AddMentionToRelationMentionAction (boolean create, 
                                              String argument) {
      super ("", ACE2004Utils.ENTITY_MENTION_TYPE);
      if (argument.equals("arg1")) {
        if (create) {
          putValue (NAME, "New Relation with Arg1");
          putValue (ACTION_COMMAND_KEY, "mention-2-new-relation-arg1");
          gotoState = null;
        } else {
          putValue (NAME, "Add to Relation as Arg1...");
          putValue (ACTION_COMMAND_KEY, "mention-2-relation-arg1");
          gotoState = SAState.SELECT_DESTINATION_RELATION_ARG1;
        }
      } else {
        putValue (NAME, "Add to Relation as Arg2...");
        putValue (ACTION_COMMAND_KEY, "mention-2-relation-arg2");
        gotoState = SAState.SELECT_DESTINATION_RELATION_ARG2;
      }
    }
    
    public void actionPerformed (ActionEvent e) {
      Set selected = getSelectedAnnots (e);
      if (selected.size () > 1) {
        GUIUtils.showError ("Add 1 mention to a relation at a time.");
        return;
      }
      
      JawbDocument doc = getJawbDocument (e);
      if (gotoState != null) {
        if (DEBUG > 0)
          System.err.println ("ACE2004tk.Men2NewEnt.ap: adding to existing...");
        subAssignor.setState (gotoState, ACE2004Utils.ENTITY_MENTION_TYPE,
                              selected, doc);
        return;
      }
      
      if (DEBUG > 0)
        System.err.println ("ACE2004tk.Men2Rel.ap: creating and adding!");
      
      // this action shouldn't be allowed to be selected on non mentions
      AWBAnnotation mention = (AWBAnnotation)selected.iterator().next();

      // create a new mention relation, and add single annot
      AWBAnnotation relMention = (AWBAnnotation)
        doc.createAnnotation (ACE2004Utils.RELATION_MENTION_TYPE);

      try {
        relMention.setAttributeValue ("arg1", mention);
      } catch (UnmodifiableAttributeException impossible) {}
      
      // force reselection of annots.
      doc.unselectAllAnnotations ();
      doc.selectAnnotation (relMention);

      subAssignor.setState (SAState.READY);
    }
  }

  // New class created 12/29/06 to support Subset Relations, which
  // contain Entity Args but no Mentions
  // Now used to support the addition of Entity arguments to any type of
  // Relation
  // 
  private class AddEntityToRelationAction 
    extends ContextSensitiveAnnotationAction {

    SAState gotoState;
    boolean create;
    String argument;
    Task task;
    public AddEntityToRelationAction (Task task, 
                                      boolean create, String argument) {
      super (ACE2004Utils.ENTITY_TYPE, 1);
      this.create = create;
      this.argument = argument;
      this.task = task;
    }
    
    public boolean isValidForContext (Set annots) {
      return true;
    }

    // We need context to set properties, because the value of a pref
    // is needed, and we want the value when we're trying to use it,
    // not when we originally created the action
    public void setPropertiesForContext(Set annots) {
      Preferences prefs = Jawb.getPreferences();
      boolean allowEntityArgs = 
        prefs.getBoolean(ACE2004Task.ENTITY_ARGS_PREFERENCE_KEY);
      if (argument.equals("arg1")) {
        if (create) {
          // if creating this way, it must be unmentioned
          // regular relations are created by selecting the text of a mention
          putValue (NAME, "New Unmentioned Relation with Arg1");
          putValue (ACTION_COMMAND_KEY, "entity-2-new-unmention-relation-arg1");
          gotoState = null;
        } else {
          if (allowEntityArgs) {
            putValue (NAME, "Add Entity to Relation as Arg1...");
            putValue (ACTION_COMMAND_KEY, "entity-2-relation-arg1");
            gotoState = SAState.SELECT_DESTINATION_RELATION_ENTITY_ARG1;
          } else {
            putValue (NAME, "Add Entity to Unmentioned Relation as Arg1...");
            putValue (ACTION_COMMAND_KEY, "entity-2-relation-arg1");
            gotoState = SAState.SELECT_DEST_UNMENT_RELATION_ENTITY_ARG1;
          }
        }
      } else {
        if (allowEntityArgs) {
          putValue (NAME, "Add Entity to Relation as Arg2...");
          putValue (ACTION_COMMAND_KEY, "entity-2-relation-arg2");
          gotoState = SAState.SELECT_DESTINATION_RELATION_ENTITY_ARG2;
        } else {
          putValue (NAME, "Add Entity to Unmentioned Relation as Arg2...");
          putValue (ACTION_COMMAND_KEY, "entity-2-relation-arg2");
          gotoState = SAState.SELECT_DEST_UNMENT_RELATION_ENTITY_ARG2;
        }
      }
    }

    public void actionPerformed (ActionEvent e) {
      Set selected = getSelectedAnnots (e);
      if (selected.size () > 1) { // should be impossible
        GUIUtils.showError ("Add 1 argument to a relation at a time.");
        return;
      }
      
      JawbDocument doc = getJawbDocument (e);
      if (gotoState != null) {
        if (DEBUG > 0)
          System.err.println ("ACE2004tk.Ent2UnmRel.ap: adding to existing...");
        subAssignor.setState (gotoState, ACE2004Utils.ENTITY_TYPE,
                              selected, doc);
        return;
      }
      
      if (DEBUG > 0)
        System.err.println ("ACE2004tk.Ent2UnmRel.ap: creating and adding!");
      
      // this action shouldn't be allowed to be selected on non-entities
      AWBAnnotation entity = (AWBAnnotation)selected.iterator().next();

      // create a new relation, and add arg1
      AWBAnnotation relation = (AWBAnnotation)
        doc.createAnnotation (ACE2004Utils.RELATION_TYPE);

      try {
        relation.setAttributeValue ("arg1", entity);
        //relation.setAttributeValue ("type", "MEMBER-OF-SET"); // hardcoded
                                                              // default
        // RK 4/10/07 don't hardcode, use getDefaultValue, which will
        // get the first option from the .rng file
        relation.setAttributeValue("type",
                                   task.getDefaultValue(relation, "type"));
      } catch (UnmodifiableAttributeException impossible) {}
      
      // force reselection of annots.
      // will select only the newly added entity in this case
      // TODO: consider selection of the relation and using appropriate
      // exchangers -- will come up with new table as well
      doc.unselectAllAnnotations ();
      doc.selectAnnotation (entity);

      subAssignor.setState (SAState.READY);
    }
  }

  private class CreateSeparateRelationAction extends AnnotationAction {
    
    public CreateSeparateRelationAction() {
      super ("Create Separate Relation", ACE2004Utils.RELATION_MENTION_TYPE);
      putValue (ACTION_COMMAND_KEY, "mention-rel-2-new-relation");
    }

    public void actionPerformed (ActionEvent e) {
      Set selected = getSelectedAnnots (e);
      JawbDocument doc = getJawbDocument(e);
      AWBDocument awbDoc = (AWBDocument) doc.getAnnotationModel();
      
      if (DEBUG > 1) {
	System.err.println("ACE2004tk.AddMR2R.actPerf: selected Annots:");
	Iterator saIter = selected.iterator();
	while (saIter.hasNext())
	  System.err.println(saIter.next());
      }

      if (DEBUG > 0)
        System.err.println ("ACE2004tk.AddMR2R.ap: creating and adding!");

      // create a new (super) relation, and add all selected mention relations
      HasSubordinates relation = (HasSubordinates)
        doc.createAnnotation (ACE2004Utils.RELATION_TYPE);
      
      // this removes subs from old parents if needed ;-)
      subAssignor.addSubAnnotsToSuper(selected, relation, awbDoc);
      subAssignor.setState (SAState.READY);
    }
  }

  /**
   * Corefer two Relation(Mention)s iff:
   * <ol><li>They have the same type and subtype
   *     <li>They have the same entities for both arg1 and arg2
   *     <ol><li>Respectively, for non-symmetric relations (Physical.Near, Personal-Social.*)
   *         <li>Irrespectively, for symmetric ones (all others)
   *     </ol>
   * </ol>
   */
  private class CoreferenceRelationMentionsAction extends AnnotationAction {
    public CoreferenceRelationMentionsAction() {
      super("Coreference Relation Mentions", ACE2004Utils.RELATION_MENTION_TYPE, -1);
      putValue(ACTION_COMMAND_KEY, "coreference-relations");
    }
    public void actionPerformed (ActionEvent e) {
      // this works on all relations, not just those selected, though it does
      // require one relation mention to be selected before invoking.
      JawbDocument doc = getJawbDocument(e);
      coreferenceRelations((AWBDocument) doc.getAnnotationModel());

      // this is not actually needed at this time because every time a
      // relation-mention changes relation, (see
      // IdTracker.AnnotModelListener.annotationRemoved() its ID is cleared.
      // That's nice for output, since it keeps the numbers consistent, but if
      // folks start referencing from externally, it could break.
      //
      // If external ref is re-instated, stop clearing the ID and maybe
      // reinstate this method.
      //reIdentifyRelationMentions(getJawbDocument(e), GUIUtils.getJawbFrame(e));
      subAssignor.setState(SAState.READY);
    }
  }

  static void coreferenceRelations(AWBDocument annots) {
    LinkedList relations = new LinkedList();

    // too bad there's no query mechanism
    // TODO? would be cleaner if all the RELATION annots were in a single
    // analysis with nothing else
    Iterator iter = annots.getAllAnnotations();
    while (iter.hasNext()) {
      AWBAnnotation annot = (AWBAnnotation) iter.next();
      if (annot.getAnnotationType().equals(ACE2004Utils.RELATION_TYPE)) {
        relations.add(annot);
      }
    }

    // remove the doc listener during the process, eles we'll step on our own
    // feet use a try-catch-finally to make sure it goes back
    annots.removeAnnotationModelListener(annotListener);
    try {
      
    // remove first from 'relations' as 'current'
    // iterate over remainder, coreferencing to 'current', and
    //     removing those coreferenced at the same time
    // wash rinse repeat
    while (! relations.isEmpty()) {
      HasSubordinates currentRelation = (HasSubordinates) relations.remove(0);
      
      if (currentRelation == null)
        continue;  // sanity check since it should never happen
      if (DEBUG > 0)
        System.err.println("A2KTK.corefRels currentRelation is " + 
                           currentRelation + " with ID: " +
                           currentRelation.getAttributeValue("ace_id"));
      /* this (getting arg1 and arg2 entities from the parent relation
       * instead of inferring it from the relation mentions) saves a
       * bit of computation, but is sometimes not set, might be
       * inconsistent, etc.  This relies on a redundancy in the MAIA
       * that I am trying to minimize reliance upon, so we are not
       * doing it this way anymore. RK 09/13/05
       *
       * RK 1/23/2007 .... Unless the relation is unmentioned, in which
       * case we have to do it this way...
       */
      AWBAnnotation currentE1 = null;
      AWBAnnotation currentE2 = null;
      if (ACE2004Task.isUnmentionedRelation(currentRelation)) {
        currentE1 = (AWBAnnotation) currentRelation.getAttributeValue("arg1");
        currentE2 = (AWBAnnotation) currentRelation.getAttributeValue("arg2");
      } else {
        AWBAnnotation[] curEntities = 
          ACE2004Task.getArgEntitiesFromRelationMention(currentRelation);
        currentE1 = curEntities[0];
        currentE2 = curEntities[1];
      }

      String currentType = (String) currentRelation.getAttributeValue("type");
      String currentSubtype = (String) currentRelation.getAttributeValue("subtype");
      String currentModality = (String) currentRelation.getAttributeValue("modality");
      String currentTense = (String) currentRelation.getAttributeValue("tense");
      boolean symmetric = // (Physical.Near, Personal-Social.*)
        "PER-SOC".equals(currentType) ||
        ("PHYS".equals(currentType) && "Near".equals(currentSubtype));
      
      if (DEBUG > 3) {
        System.err.println("A2k4Tk.corefRel: Rel("+IdRenderer.getShortId(currentRelation, annots)+
                           ") e1="+(currentE1 == null ? null : IdRenderer.getShortId(currentE1, annots))+
                           "  e2="+(currentE2 == null ? null : IdRenderer.getShortId(currentE2, annots))+
                           "  type="+currentType+"  sub="+currentSubtype+
                           "  mode="+currentModality+"  tense="+currentTense+"  sym="+symmetric);
      }
      
      // skip where anything is unspecified
      if (currentE1 == null || currentE2 == null || currentType == null || currentSubtype == null) {
        if (DEBUG > 3)
          System.err.println("  skipping (some entries unspecified)");
        continue;
      }
      
      Iterator relationIter = relations.iterator();
      while (relationIter.hasNext()) {
        HasSubordinates relation = (HasSubordinates) relationIter.next();
        // in general, we do not want to assume the entities are set
        // correctly in the relation, and should instead get them from
        // a mention; however, for unmentioned relations, we must get
        // the entities from the relation itself.  RK 1/23/07
        AWBAnnotation e1 = null;
        AWBAnnotation e2 = null;
        if (ACE2004Task.isUnmentionedRelation(relation)) {
          e1 = (AWBAnnotation) relation.getAttributeValue("arg1");
          e2 = (AWBAnnotation) relation.getAttributeValue("arg2");
        } else {
          AWBAnnotation[] entities = 
            ACE2004Task.getArgEntitiesFromRelationMention(relation);
          e1 = entities[0];
          e2 = entities[1];
        }

        String type = (String) relation.getAttributeValue("type");
        String subtype = (String) relation.getAttributeValue("subtype");
        String modality = (String) relation.getAttributeValue("modality");
        String tense = (String) relation.getAttributeValue("tense");
        
        if (DEBUG > 3) {
          System.err.println("  comparing: Rel("+IdRenderer.getShortId(relation, annots)+
                             ") e1="+(e1 == null ? null : IdRenderer.getShortId(e1, annots))+
                             "  e2="+(e2 == null ? null : IdRenderer.getShortId(e2, annots))+
                             "  type="+type+"  sub="+subtype+
                             "  modality="+modality+"  tense="+tense);
        }
        
        if (currentType.equals(type) && currentSubtype.equals(subtype) &&
            currentModality.equals(modality) && currentTense.equals(tense) &&
            ((currentE1 == e1 && currentE2 == e2) ||
             (symmetric && currentE1 == e2 && currentE2 == e1)) ) {
          // this removes mentions from old relation if needed ;-)
          // this re-orders arg1 and arg2 in symmetric relations if needed
          if (DEBUG > 0)
            System.err.println("      corefing: "+IdRenderer.getShortId(relation, annots));
          
          ATLASElementSet subs =
            relation.getRegion().getSubordinateSet(ACE2004Utils.RELATION_MENTION_TYPE);
          Set subSet = new LinkedHashSet();
          Iterator mentionIter = subs.iterator();
          while (mentionIter.hasNext()) {
            Object next = mentionIter.next();
            if (next instanceof AnnotationRef)
              next = ((AnnotationRef) next).getElement();
            subSet.add(next);
            // swap the args of symmetric ones if needed
            if (symmetric && currentE1 == e2) {
              AWBAnnotation mention = (AWBAnnotation) next;
              Object m1 = mention.getAttributeValue("arg1");
              Object m2 = mention.getAttributeValue("arg2");
              try {
                mention.setAttributeValue("arg1", m2);
                mention.setAttributeValue("arg2", m1);
              } catch (Exception x) {}
            }
          }
          // add all mentions to the new relation and delete the old
          if (SubordinateAssignor.addSubAnnotsToSuper(subSet, currentRelation, annots))
            annots.deleteAnnotation(relation, false);
          
          // don't try to coreference it again
          relationIter.remove();
        }
      } // while (relationIter...)
    } // while (! relations.isEmpty()

    } finally { // put the listneer back
      annots.addAnnotationModelListener(annotListener);
    }

    annots.putClientProperty(ACE2004Task.RELATIONS_COREFERENCED, Boolean.TRUE);
  }

  static void reIdentifyRelationMentions(JawbDocument doc, Component comp) {
    AWBDocument annots = (AWBDocument) doc.getAnnotationModel();

    // Rather redundant, but first check to see if any need to be re-identified
    boolean couldReIdentify = false;
    Iterator iter = annots.getAllAnnotations();
    while (iter.hasNext() && ! couldReIdentify) {
      AWBAnnotation annot = (AWBAnnotation) iter.next();
      if (annot.getAnnotationType().equals(ACE2004Utils.RELATION_TYPE)) {
        String relationId = (String) annot.getAttributeValue("ace_id");
        ATLASElementSet subs =
          annot.getRegion().getSubordinateSet(ACE2004Utils.RELATION_MENTION_TYPE);

        Iterator mentionIter = subs.iterator();
        while (mentionIter.hasNext()) {
          Object next = mentionIter.next();
          if (next instanceof AnnotationRef)
            next = ((AnnotationRef) next).getElement();
          String mentionId = (String) ((AWBAnnotation) next).getAttributeValue("ace_id");
          if (! (mentionId == null || mentionId.startsWith(relationId))) {
            couldReIdentify = true;
            break;
          }
        }
      }
    }
    if (! couldReIdentify)
      return;

    String message =
      "When coreferencing relation_mentions from saved data, their\n"+
      "identifiers may remain prefixed with the ID of their\n"+
      "old relation, as in the case of the last mention here,\n"+
      "which was in relation(R4) prior to the coreferencing:\n\n"+
      "     relation(R1) {\n"+
      "         relation_mention(R1-1),\n"+
      "         relation_mention(R1-2),\n"+
      "         relation_mention(R4-1)\n"+
      "     }\n\n"+
      "Re-identifying will renumber /all/ relation_mentions. External references to the old\n"+
      "relation mention ID's will fail.\n\n"+
      "Are you sure you want to re-identify the relation_mentions?";
    int result = JOptionPane.showConfirmDialog(comp, message,
                                               "Re-Identify Relation Mentions?",
                                               JOptionPane.YES_NO_OPTION,
                                               JOptionPane.QUESTION_MESSAGE);
    if (result != JOptionPane.YES_OPTION)
      return;

    iter = annots.getAllAnnotations();
    while (iter.hasNext()) {
      AWBAnnotation annot = (AWBAnnotation) iter.next();
      if (annot.getAnnotationType().equals(ACE2004Utils.RELATION_TYPE)) {
        String relationId = (String) annot.getAttributeValue("ace_id");
        ATLASElementSet subs =
          annot.getRegion().getSubordinateSet(ACE2004Utils.RELATION_MENTION_TYPE);

        Iterator mentionIter = subs.iterator();
        while (mentionIter.hasNext()) {
          Object next = mentionIter.next();
          if (next instanceof AnnotationRef)
            next = ((AnnotationRef) next).getElement();
          String mentionId = (String) ((AWBAnnotation) next).getAttributeValue("ace_id");
          if (! (mentionId == null) || ! mentionId.startsWith(relationId)) {
            try {
              ((AWBAnnotation) next).setAttributeValue("ace_id", null);
            } catch (Exception x) {}
            IdTracker.getIdTracker(annots).getAceId((AWBAnnotation) next);
          }
        }
      }
    }
  }
  
  /**
   * An action to create a new relation mention extent and either add it to an
   * existing relation mention or create a new relation mention for it according to the
   * value of createEntity in the constructor.
   *
   */
  class CreateRelationMentionExtentAndRelationAction extends CreateTextAnnotAction {

    boolean createParent;
    
    public CreateRelationMentionExtentAndRelationAction (boolean createParent) {
      super ((createParent ? "Text Extent for New Relation" :
	      "Text Extent for Existing Relation ..."), 
	     task, ACE2004Utils.RELATION_MENTION_EXTENT_TYPE, "relation.extent");
      if (createParent)
        putValue (ACTION_COMMAND_KEY, "createTextAnnot-extent-and-relation");
      else
        putValue (ACTION_COMMAND_KEY, "createTextAnnot-extent-to-relation");
      this.createParent = createParent;
    }

    public void actionPerformed (ActionEvent e) {
	if (ACE2004ToolKit.this.DEBUG > 0) {
	    System.err.println ("ACE2004tk.CreateRelationMentionExtentAndRelationAction.actionPerformed");
      	    System.err.println ("e = " + e);
	}
	AWBAnnotation relationMentionExtent = super.createAnnotation(e);
	if (ACE2004ToolKit.this.DEBUG > 0) {
	    System.err.println ("ACE2004tk.CreateRelationMentionExtentAndRelationAction.actionPerformed");
      	    System.err.println ("relationMentionExtent = " + relationMentionExtent);
	}

	JawbDocument doc = GUIUtils.getJawbDocument(e);
	AWBDocument awbDoc = (AWBDocument) doc.getAnnotationModel();
      
	if (createParent) {
	    if (ACE2004ToolKit.this.DEBUG > 0) {
		System.err.println ("ACE2004tk.CreateRelationMentionExtentAndRelationAction.actionPerformed");
		System.err.println ("  creating parent ...");
	    }
	    HasSubordinates relationMention = (HasSubordinates)
		doc.createAnnotation(ACE2004Utils.RELATION_MENTION_TYPE);

	    try {
		relationMention.setAttributeValue("relation-mention-extent", relationMentionExtent);
	    } catch (UnmodifiableAttributeException impossible) {}
	    doc.unselectAllAnnotations ();
	    doc.selectAnnotation (relationMention);
	    subAssignor.setState (SAState.READY);

	} else {
	    if (ACE2004ToolKit.this.DEBUG > 0) {
		System.err.println ("ACE2004tk.CreateRelationMentionExtentAndRelationAction.actionPerformed");
		System.err.println ("  NOT creating parent ...");
	    }
	    subAssignor.setState (SAState.SELECT_DESTINATION_RELATION_MENTION,
				  ACE2004Utils.RELATION_MENTION_EXTENT_TYPE,
				  Collections.singleton(relationMentionExtent),
				  doc);
      }
    }
  }

  /**
   * An action to create a new mention and either add it to an
   * existing entity or create a new entity for it according to the
   * value of createEntity in the constructor.
   *
   */
  class CreateEventMentionAndEventAction extends CreateTextAnnotAction {

    boolean createParent;
    
    public CreateEventMentionAndEventAction (boolean createParent) {
      super ((createParent ? "New Event Mention for New Event" :
	      "New Event Mention for Existing Event..."), 
	     task, ACE2004Utils.EVENT_MENTION_TYPE, "event.anchor");
      if (createParent)
        putValue (ACTION_COMMAND_KEY, "createTextAnnot-mention-and-event");
      else
        putValue (ACTION_COMMAND_KEY, "createTextAnnot-mention-to-event");
      this.createParent = createParent;
    }

    public void actionPerformed (ActionEvent e) {
      AWBAnnotation mention = super.createAnnotation(e);

      JawbDocument doc = GUIUtils.getJawbDocument(e);
      AWBDocument awbDoc = (AWBDocument) doc.getAnnotationModel();
      
      if (createParent) {
	HasSubordinates event = (HasSubordinates)
	  doc.createAnnotation(ACE2004Utils.EVENT_TYPE);
	// if addition of mentions fails, remove entity
	if (! subAssignor.addSubAnnotsToSuper (Collections.singleton(mention),
					       event, awbDoc) &&
	    event.getSubordinates (ACE2004Utils.EVENT_MENTION_TYPE).length == 0) {
	  doc.deleteAnnotation (event);
	} else {
	  // force reselection of annots.
	  doc.unselectAllAnnotations ();
	  doc.selectAnnotation (event);
	}
	subAssignor.setState (SAState.READY);

      } else {
	subAssignor.setState (SAState.SELECT_DESTINATION_EVENT,
			      ACE2004Utils.EVENT_MENTION_TYPE,
			      Collections.singleton(mention),
			      doc);
      }
    }
  }

  /**
   * Adds selected mentions to an entity; either a new one created from
   * scratch, or by changing state and adding to the next entity selected
   */
  class EventMentions2EventAction extends AnnotationAction {
    SAState gotoState;
    public EventMentions2EventAction (boolean create) {
      super ("", ACE2004Utils.EVENT_MENTION_TYPE);
      if (create) {
        putValue (NAME, "New Event");
        putValue (ACTION_COMMAND_KEY, "mention-2-new-event");
        gotoState = null;
      } else {
        putValue (NAME, "Add To Event...");
        putValue (ACTION_COMMAND_KEY, "mention-2-event");
        gotoState = SAState.SELECT_DESTINATION_EVENT;
      }
    }
    
    public void actionPerformed (ActionEvent e) {
      if (ACE2004ToolKit.this.DEBUG > 0)
        System.err.println ("ACE2004tk.M2EAct: waiting for entity choice.");
      
      Set selected = getSelectedAnnots (e);
      
      JawbDocument doc = getJawbDocument(e);
      AWBDocument awbDoc = (AWBDocument) doc.getAnnotationModel();
      
      if (gotoState != null) {
        subAssignor.setState (gotoState, ACE2004Utils.EVENT_MENTION_TYPE,
                              selected, doc);
        return;
      }

      HasSubordinates event = (HasSubordinates)
        doc.createAnnotation (ACE2004Utils.EVENT_TYPE);
      // if addition of all (or at least first) mentions fails, remove entity
      if (! subAssignor.addSubAnnotsToSuper (selected, event, awbDoc) &&
          event.getSubordinates (ACE2004Utils.EVENT_MENTION_TYPE).length == 0) {
        doc.deleteAnnotation (event);
      } else {
        // force reselection of annots.
        doc.unselectAllAnnotations ();
        doc.selectAnnotation (event);
      }
      subAssignor.setState (SAState.READY);
    }
  }

  private class CreateEventMentionExtentAction extends CreateTextAnnotAction {
    public CreateEventMentionExtentAction () {
      super ("Event Mention Extent", task,
             ACE2004Utils.EVENT_MENTION_EXTENT_TYPE, "event.extent");
    }
    public void actionPerformed (ActionEvent e) {
      AWBAnnotation annot = createAnnotation (e);
      subAssignor.setState (SAState.ASSIGN_EVENT_MENTION_EXTENT,
			    ACE2004Utils.EVENT_MENTION_EXTENT_TYPE,
			    Collections.singleton(annot),
			    GUIUtils.getJawbDocument(e));
      return;
    }
  }

  private class CreateRelationMentionExtentAction extends CreateTextAnnotAction {
    public CreateRelationMentionExtentAction () {
      super ("Relation Mention Extent", task,
             ACE2004Utils.RELATION_MENTION_EXTENT_TYPE, "relation.extent");
    }
    public void actionPerformed (ActionEvent e) {
      AWBAnnotation annot = createAnnotation (e);
      subAssignor.setState (SAState.ASSIGN_RELATION_MENTION_EXTENT,
			    ACE2004Utils.RELATION_MENTION_EXTENT_TYPE,
			    Collections.singleton(annot),
			    GUIUtils.getJawbDocument(e));
      return;
    }
  }

  /* arguments */
  private class SelectArgumentMentionValueAction extends AnnotationAction {
    public SelectArgumentMentionValueAction() {
      super("Select Argument Value", ACE2004Utils.ARGUMENT_MENTION_TYPE, 1);
      putValue(ACTION_COMMAND_KEY, "select-argument-value");
    }
    public void actionPerformed (ActionEvent e) {
      JawbDocument doc = getJawbDocument(e);
      AWBAnnotation argument =
        (AWBAnnotation) getSelectedAnnots(e).iterator().next();
      subAssignor.setState (SAState.SELECT_ARGUMENT_MENTION_VALUE,
			    ACE2004Utils.ARGUMENT_MENTION_TYPE,
			    Collections.singleton(argument),
			    GUIUtils.getJawbDocument(e));
    }
  }

  private class UseAsArgumentValueAction 
    extends ContextSensitiveAnnotationAction {
    AnnotationType type;
    public UseAsArgumentValueAction(AnnotationType type) {
      super (type, 1);
      this.type = type;
    }
    
    public boolean isValidForContext (Set annots) {
      if (ACE2004Utils.ENTITY_TYPE.equals(type)) {
        Preferences prefs = Jawb.getPreferences();
        return (prefs.getBoolean(ACE2004Task.ENTITY_ARGS_PREFERENCE_KEY));
      }
      return true;
    }

    public void setPropertiesForContext(Set annots) {
      if (ACE2004Utils.ENTITY_TYPE.equals(type)) {
        putValue (NAME, "Use Entity as Argument Value For...");
      } else {
        putValue (NAME, "Use as Argument Value For...");
      }
      putValue(ACTION_COMMAND_KEY, "use-as-arg-value-"+type.getName());
    }

    public void actionPerformed(ActionEvent e) {
      // assert (getSelectedAnnots().size() == 1)
      AWBAnnotation annot =
        (AWBAnnotation) getSelectedAnnots(e).iterator().next();
      AnnotationType valueType = annot.getAnnotationType();

      if (DEBUG > 0)
	  System.err.println("A2kTk.UseAsArgVal: val="+valueType.getName()+":"+annot.getId());

      subAssignor.setState (SAState.ADD_ARGUMENT_TO_ANNOT,
                            valueType, Collections.singleton(annot),
			    GUIUtils.getJawbDocument(e));
    }
  }

  // Assumed for now that Parent is an Entity (and type is an entity mention)
  private class UseParentAsArgumentValueAction 
    extends ContextSensitiveAnnotationAction {
    public UseParentAsArgumentValueAction(AnnotationType type) {
      super(type, 1);
      putValue(ACTION_COMMAND_KEY, "use-parent-as-arg-value-"+type.getName());
    }

    public boolean isValidForContext (Set annots) {
      Preferences prefs = Jawb.getPreferences();
      return (prefs.getBoolean(ACE2004Task.ENTITY_ARGS_PREFERENCE_KEY));
    }
    

    public void setPropertiesForContext(Set annots) {
      putValue (NAME, "Use Entity as Argument Value For...");
    }

    public void actionPerformed(ActionEvent e) {
      // assert (getSelectedAnnots().size() == 1)
      AWBAnnotation annot =
        (AWBAnnotation) getSelectedAnnots(e).iterator().next();
      AnnotationType valueType = annot.getAnnotationType();

      if (DEBUG > 0)
	  System.err.println("A2kTk.UseParentAsArgVal: val="+valueType.getName()+":"+annot.getId());

      subAssignor.setState (SAState.ADD_PARENT_ENTITY_ARGUMENT_TO_ANNOT,
                            valueType, Collections.singleton(annot),
			    GUIUtils.getJawbDocument(e));
    }
  }

/*
    actions.add (new CreateMentionArgumentAction());
    actions.add (new SelectMentionArgumentValueAction());
*/
  /***********************************************************************/
  /* Exchangers */
  /***********************************************************************/

  /* Mentions -> ?? */
  class Mention2Parent implements ItemExchanger {
    LinkedHashSet results = new LinkedHashSet();
    public Set exchange (Object item) {
      results.clear ();
      AWBAnnotation annot = (AWBAnnotation) item;
      annot = task.getMentionParent(annot);
      if (annot != null)
        results.add (annot);
      return results;
    }
  }
  
  class Mention2Relations implements ItemExchanger {
    LinkedHashSet results = new LinkedHashSet();
    public Set exchange (Object item) {
      results.clear ();
      AWBAnnotation annot = (AWBAnnotation) item;
      String type = annot.getAnnotationType ().getName ();
      if (type.equals (ACE2004Task.ENTITY_MENTION_TYPE_NAME)) {
        AWBAnnotation[] relations =
          task.getMentionRelations((MultiPhraseAnnotation)annot);
        for (int i=0; i<relations.length; i++)
          results.add (relations[i]);
      }
      return results;
    }
  }
  
  /* Entities */
  class Entity2Mentions implements ItemExchanger {
    LinkedHashSet results = new LinkedHashSet();
    public Set exchange (Object item) {
      /**
      System.err.println("Entity2Mentions.exchange(" + item + ")");
      Thread.dumpStack();
      **/
      results.clear ();
      AWBAnnotation annot = (AWBAnnotation) item;
      String type = annot.getAnnotationType ().getName ();
      if (type.equals (ACE2004Task.ENTITY_TYPE_NAME)) {
        SubordinateSetsAnnotation parent = (SubordinateSetsAnnotation) annot;
        AWBAnnotation[] mentions =
          parent.getSubordinates (ACE2004Utils.ENTITY_MENTION_TYPE);
        for (int i=0; i<mentions.length; i++)
          results.add (mentions[i]);
      }
      /**
      System.err.println("Entitye2Mentions.exchange returning " +
                         results.size() + " results.");
      **/
      return results;
    }
  }

  /* returns relations containing the Entities in arg1 or arg2 slots */
  class Entity2Relations implements ItemExchanger {
    LinkedHashSet results = new LinkedHashSet();
    public Set exchange (Object item) {
      results.clear ();
      AWBAnnotation annot = (AWBAnnotation) item;
      String type = annot.getAnnotationType ().getName ();
      if (type.equals (ACE2004Task.ENTITY_TYPE_NAME)) {
        AWBAnnotation[] relations =
          task.getEntityRelations(annot);
        for (int i=0; i<relations.length; i++)
          results.add (relations[i]);
      }
      return results;
    }
  }
  
  /* Relations */
  class Relation2Mentions implements ItemExchanger {
    LinkedHashSet results = new LinkedHashSet();
    public Set exchange (Object item) {
      results.clear ();
      AWBAnnotation annot = (AWBAnnotation) item;
      String type = annot.getAnnotationType ().getName ();
      if (type.equals (ACE2004Task.RELATION_MENTION_TYPE_NAME)) {
        Object arg = annot.getAttributeValue ("arg1");
        if (arg != null)
          results.add (arg);
        arg = annot.getAttributeValue ("arg2");
        if (arg != null)
          results.add (arg);
      }
      return results;
    }
  }
  
  class RelationExtent2Mention implements ItemExchanger {
    LinkedHashSet results = new LinkedHashSet();
    public Set exchange (Object item) {
      if (DEBUG > 0) {
	  System.err.println("(RelationExtent2Mention) item = " + item);
      }
      results.clear ();
      AWBAnnotation annot = (AWBAnnotation) item;
      // just for testing purposes: results.add (annot);
      String type = annot.getAnnotationType ().getName ();
      if (type.equals (ACE2004Task.RELATION_MENTION_EXTENT_TYPE_NAME)) {
	  // Object relMention = (Object) ACE2004Task.getRelationMentionFromExtent(annot);
	  Object relMention = (Object) ACE2004Task.getMentionParent(annot);
	  if (DEBUG > 0) {
	      System.err.println("(RelationExtent2Mention) relMention = " + relMention);
	  }
	  if (relMention != null &&
	      relMention instanceof HasSubordinates &&
	      ((Annotation)relMention).getAnnotationType().getName().equals(ACE2004Task.RELATION_MENTION_TYPE_NAME)) {
	      if (DEBUG > 0) {
		  System.err.println("(RelationExtent2Mention) found a relMention.");
	      }
	      results.add (relMention);
	  }
      }
      return results;
    }
  }

  
  class Relation2Entities implements ItemExchanger {
    LinkedHashSet results = new LinkedHashSet();
    public Set exchange (Object item) {
      results.clear();
      AWBAnnotation annot = (AWBAnnotation) item;
      String type = annot.getAnnotationType().getName();
      if (type.equals (ACE2004Task.RELATION_MENTION_TYPE_NAME)) {
        Object arg = annot.getAttributeValue("arg1");
        if (arg != null) {
          Object entity = task.getMentionParent((AWBAnnotation) arg);
          if (entity != null)
            results.add(entity);
        }
        arg = annot.getAttributeValue("arg2");
        if (arg != null) {
          Object entity = task.getMentionParent((AWBAnnotation) arg);
          if (entity != null)
            results.add(entity);
        }
      }
      return results;
    }
  }
  
  class UnmentionedRelation2Entities implements ItemExchanger {
    LinkedHashSet results = new LinkedHashSet();
    public Set exchange (Object item) {
      results.clear();
      AWBAnnotation annot = (AWBAnnotation) item;
      if (ACE2004Task.isUnmentionedRelation(annot)) {
        Object arg = annot.getAttributeValue("arg1");
        if (arg != null) {
          results.add(arg);
        }
        arg = annot.getAttributeValue("arg2");
        if (arg != null) {
          results.add(arg);
        }
      }
      return results;
    }
  }
  
  /* RelationMention: -> Mentions, Values */
  class Relation2Lexicals implements ItemExchanger {
    LinkedHashSet results = new LinkedHashSet();
    public Set exchange (Object item) {
      results.clear ();
      AWBAnnotation annot = (AWBAnnotation) item;
      String type = annot.getAnnotationType ().getName ();
      if (type.equals (ACE2004Task.RELATION_MENTION_TYPE_NAME)) {
        /* relation mention is now lexical. */
        results.add(annot);
        Object arg = annot.getAttributeValue ("arg1");
        if (arg != null)
          results.add (arg);
        arg = annot.getAttributeValue ("arg2");
        if (arg != null)
          results.add (arg);
        Object extent = annot.getAttributeValue("relation-mention-extent");
        if (extent != null)
          results.add(extent);
        ATLASElementSet args = annot.getRegion()
          .getSubordinateSet(ACE2004Utils.ARGUMENT_MENTION_TYPE);
        Iterator iter = args.iterator();
        while(iter.hasNext()) {
          Object next = iter.next();
          AWBAnnotation nextArg = null;
          if (next instanceof AnnotationRef)
            nextArg = (AWBAnnotation) ((AnnotationRef) next).getElement();
          else
            nextArg = (AWBAnnotation) next;
          arg = nextArg.getAttributeValue("value-value");
          if (arg == null)
            arg = nextArg.getAttributeValue("entity-value");
          if (arg == null)
            arg = nextArg.getAttributeValue("timex2-value");
          if (arg != null)
            results.add (arg);
        }
      }
      return results;
    }
  }
  
  /* events */
  class Event2EventLexicals implements ItemExchanger {
    LinkedHashSet results = new LinkedHashSet();
    public Set exchange (Object item) {
      results.clear ();
      AWBAnnotation annot = (AWBAnnotation) item;
      String type = annot.getAnnotationType ().getName ();
      if (type.equals(ACE2004Task.EVENT_TYPE_NAME)) {
        AWBAnnotation [] mentions =
          ((HasSubordinates) annot).getSubordinates(ACE2004Utils.EVENT_MENTION_TYPE);
        for (int i=0; i<mentions.length; i++) {
          AWBAnnotation extent =
            (AWBAnnotation) mentions[i].getAttributeValue("extent");
          if (extent != null)
            results.add (extent);
          results.add (mentions[i]);
        }
      }
      return results;
    }
  }
  
  class EventLexicals2Event implements ItemExchanger {
    LinkedHashSet results = new LinkedHashSet();
    public Set exchange (Object item) {
      results.clear ();
      AWBAnnotation annot = (AWBAnnotation) item;
      String type = annot.getAnnotationType().getName();
      if (type.equals(ACE2004Task.EVENT_MENTION_EXTENT_TYPE_NAME)) {
        annot = task.getParent(annot, ACE2004Task.EVENT_MENTION_TYPE_NAME);
        if (annot != null)
          type = annot.getAnnotationType().getName();
      }
      if (type.equals(ACE2004Task.EVENT_MENTION_TYPE_NAME)) {
        annot = task.getMentionParent(annot);
        if (annot != null)
          results.add(annot);
      }
      return results;
    }
  }
  
  class EventLexicals2Lexicals implements ItemExchanger {
    LinkedHashSet results = new LinkedHashSet();
    public Set exchange (Object item) {
      results.clear ();
      AWBAnnotation annot = (AWBAnnotation) item;
      AWBAnnotation other = null;
      String type = annot.getAnnotationType().getName ();
      if (type.equals(ACE2004Task.EVENT_MENTION_TYPE_NAME)) {
        Object extent = annot.getAttributeValue("extent");
        if (extent != null)
          results.add(extent);
        results.add(annot);
      }
      else if (type.equals(ACE2004Task.EVENT_MENTION_EXTENT_TYPE_NAME)) {
        results.add(annot);
        AWBAnnotation mention =
          ACE2004Task.getParent(annot, ACE2004Task.EVENT_MENTION_TYPE_NAME);
        if (mention != null)
          results.add(mention);
      }
      return results;
    }
  }

  /* Arguments */
  class ArgMention2Event implements ItemExchanger {
    LinkedHashSet results = new LinkedHashSet();
    public Set exchange (Object item) {
      results.clear ();

      if (DEBUG > 0)
	  System.err.println("DSD: ArgMention2Event item = " + item.toString());
      AWBAnnotation annot = (AWBAnnotation) item;
      AWBAnnotation mention =
        ACE2004Task.getParent(annot, ACE2004Task.EVENT_MENTION_TYPE_NAME);

      if (mention != null) {
        AWBAnnotation event = ACE2004Task.getMentionParent(mention);
        results.add(event);
      }

      return results;
    }
  }
  
  class ArgMention2EventMention implements ItemExchanger {
    LinkedHashSet results = new LinkedHashSet();
    public Set exchange (Object item) {
      results.clear ();

      AWBAnnotation annot = (AWBAnnotation) item;
      AWBAnnotation mention =
        ACE2004Task.getParent(annot, ACE2004Task.EVENT_MENTION_TYPE_NAME);

      if (mention != null)
        results.add(mention);

      return results;
    }
  }
  
  class ArgMention2Relation implements ItemExchanger {
    LinkedHashSet results = new LinkedHashSet();
    public Set exchange (Object item) {
      results.clear ();

      AWBAnnotation annot = (AWBAnnotation) item;
      AWBAnnotation mention =
        ACE2004Task.getParent(annot, ACE2004Task.RELATION_MENTION_TYPE_NAME);

      if (mention != null) {
        AWBAnnotation event = ACE2004Task.getMentionParent(mention);
        results.add(event);
      }

      return results;
    }
  }
  
  class ArgMention2RelationMention implements ItemExchanger {
    LinkedHashSet results = new LinkedHashSet();
    public Set exchange (Object item) {
      results.clear ();

      AWBAnnotation annot = (AWBAnnotation) item;
      AWBAnnotation mention =
        ACE2004Task.getParent(annot, ACE2004Task.RELATION_MENTION_TYPE_NAME);

      if (mention != null)
        results.add(mention);

      return results;
    }
  }
  
  class ArgMention2Lexicals implements ItemExchanger {
    LinkedHashSet results = new LinkedHashSet();
    public Set exchange (Object item) {
      results.clear ();

      AWBAnnotation annot = (AWBAnnotation) item;
      
      if (DEBUG > 0)
	  System.err.println("ArgMention2Lexicals item = " + item.toString());
      AWBAnnotation value =
        (AWBAnnotation) annot.getAttributeValue("entity-value");
      if (value == null)
        value = (AWBAnnotation) annot.getAttributeValue("quantity-value");
      if (value == null)
        value = (AWBAnnotation) annot.getAttributeValue("timex2-value");
      if (value == null)
	  return results;

      AnnotationType type = annot.getAnnotationType();
      if (type.equals(ACE2004Utils.ENTITY_TYPE) ||
          type.equals(ACE2004Utils.QUANTITY_TYPE)) {
        // in the future we may accept Arguments(not only ArgMentions) here
        System.err.println("Not designed to work with: "+type.getName());
        Thread.dumpStack();
      }
      else
        results.add(value);

      return results;
    }
  }

  /* Quantities */
  class Quantity2Lexicals implements ItemExchanger {
    LinkedHashSet results = new LinkedHashSet();
    public Set exchange (Object item) {
      results.clear ();
      AWBAnnotation annot = (AWBAnnotation) item;
      String type = annot.getAnnotationType ().getName ();
      if (type.equals (ACE2004Task.QUANTITY_TYPE_NAME)) {
        SubordinateSetsAnnotation parent = (SubordinateSetsAnnotation) annot;
        AWBAnnotation[] mentions =
          parent.getSubordinates (ACE2004Utils.QUANTITY_MENTION_TYPE);
        for (int i=0; i<mentions.length; i++)
          results.add (mentions[i]);
      }
      return results;
    }
  }

  /* Timex2 */
  // shamelessly copied from the timex2 task
  class SetSavedValueAction extends AnnotationAction {
    public SetSavedValueAction (AnnotationType type) {
      super ("Retrieve Date", type);
    }
    public void actionPerformed (ActionEvent e) {
      Set selected = getSelectedAnnots(e);
      Iterator iter = selected.iterator();
      Timex2Editor.Timex2Data data = timex2Table.getSavedDate();
      String dateVal = data.val;
      boolean oneChanged = false;
      while (iter.hasNext()) {
	AWBAnnotation annot = (AWBAnnotation)iter.next();
	try {
          String type = (String) annot.getAttributeValue("type");
          if ("timex2".equals(type)) {
            annot.setAttributeValue("val", dateVal);
            oneChanged = true;
          }
	} catch (UnmodifiableAttributeException ex) {
	  System.err.println("SetSavedValueAction.actionPerformed: " +
			     "This shouldn't happen!  Can't set \"val\" for " +
			     annot.toString());
	}
      }
      if (! oneChanged) {
        GUIUtils.beep();
      }
    }
  }

  class AutotagAction extends ContextSensitiveAnnotationAction {
    private Task task;
    private boolean query;
    public AutotagAction(Task task, boolean query) {
      super(null, 1);
      this.task=task;
      this.query = query;
    }

    public boolean isValidForContext (Set annots) {
      AWBAnnotation annot = (AWBAnnotation) annots.iterator().next();
      return (task.getAutotagger().isAutoTaggable(annot));
    }

    public void setPropertiesForContext(Set annots) {
      putValue (NAME, "Autotag matching strings" + 
                (query?" -- query mode...":""));
    }

    public void actionPerformed(ActionEvent e) {
      // assert (getSelectedAnnots().size() == 1)
      AWBAnnotation annot =
        (AWBAnnotation) getSelectedAnnots(e).iterator().next();
      JawbDocument jd = getJawbDocument(e);
      JTextPane textPane = mainComponent.getTextPane();
      MainTextFinder textFinder = mainComponent.getFinder();
      String docText = null;
      // Get the text from the document, because retrieveing from
      // tp.getText() seems to convert newlines to the current system.
      try {
        Document doc = textPane.getDocument();
        docText = doc.getText(0, doc.getLength());
      } catch (BadLocationException x) {
        return; // impossible?
      }

      Preferences prefs = Jawb.getPreferences();
      final boolean forward = 
        prefs.getBoolean(Preferences.AUTOTAG_FORWARD_ONLY_KEY);
      final boolean untagged = 
        prefs.getBoolean(Preferences.AUTOTAG_UNTAGGED_ONLY_KEY);

      // don't think this needs to be in an invokeLater because it isn't
      // being called in the middle of gui-heavy stuff as it is in MTP
      if (query) {
        task.getAutotagger().doAutotag(jd, docText, textFinder, textPane,
                                       annot, "Query", forward, untagged);
      } else {
        task.getAutotagger().doAutotag(jd, docText, textFinder, textPane,
                                       annot, "Automatic", forward, untagged);
      }
    }
        
  }
      
    

  /***********************************************************************/
  /* Annotation Event Handling */
  /***********************************************************************/

  /**
   * This listener handles any fallout of working with annotations <i>after</i>
   * the change has been made.  To deal with validity tests /before/ performing
   * an action, another mechanism must be used (see {@link
   * SubordinateAssignor}). Includes assigning unique ace id's, automatically
   * creating containing relations, etc... etc...
   *
   * 
   */
  static class ACE2004AnnotationListener implements AnnotationModelListener {

    ACE2004Task task;

    public ACE2004AnnotationListener(ACE2004Task t) {
      task = t;
    }
    
    /** Invoked after an annotation has been created. */
    public void annotationCreated(AnnotationModelEvent e) {

      AWBDocument doc = (AWBDocument) e.getModel();
      AWBAnnotation annot = e.getAnnotation();
      AnnotationType type = annot.getAnnotationType();

      if (type == ACE2004Utils.ENTITY_TYPE ||
          type == ACE2004Utils.RELATION_TYPE ||
          type == ACE2004Utils.QUANTITY_TYPE ||
          type == ACE2004Utils.EVENT_TYPE) {
        // assign ID
        // System.err.println("ACE2004AnnotListener.created entity type");
        IdTracker.getIdTracker(doc).getAceId(annot);

        // RK 2/3/06 initialize subtype to "" to avoid odd behavior of
        // that table column when first clicked if it is null
        try {
          annot.setAttributeValue("subtype", "");
        } catch (UnmodifiableAttributeException x) {};

      }
      else if (type == ACE2004Utils.ENTITY_MENTION_TYPE) {
        // the annotationInserted handler will deal with assigning ID.
      }
      else if (type == ACE2004Utils.RELATION_MENTION_TYPE) {
        // create relation and assign as subordinate before assigning ID
        HasSubordinates relation =
          (HasSubordinates) doc.createAnnotation (ACE2004Utils.RELATION_TYPE);

        // the annotationInserted handler will deal with assigning ID.
        
        relation.addSubordinate(annot);
      }
      else if (type == ACE2004Utils.EVENT_MENTION_TYPE) {
        // the annotationInserted handler will deal with assigning ID.
      }
      else if (type == ACE2004Utils.TIMEX2_TYPE) {
        //System.err.println("Creating ID for Timex2 entity...");
        // assign ID
        String mentionId = IdTracker.getIdTracker(doc).getAceId(annot);
        //System.err.println("ID is: " + annot.getAttributeValue("ace_id"));
        //System.err.println("ID is: " + mentionId);
        int finalHyphenPos = mentionId.lastIndexOf('-');
        //System.err.println ("  timex2: finalHyphenPos= " + finalHyphenPos);
        String timex2Id = mentionId.substring(0,finalHyphenPos);
        try {
          annot.setAttributeValue("timex2-id", timex2Id);
        } catch (UnmodifiableAttributeException impossible) {};
        if (DEBUG > 0)
          System.err.println ("  timex2-id: " + timex2Id);
        
      }  else if (type == ACE2004Utils.QUANTITY_MENTION_TYPE) {
        // create event and assign as subordinate before assigning ID
        HasSubordinates event =
          (HasSubordinates) doc.createAnnotation (ACE2004Utils.QUANTITY_TYPE);

        // this annotationInserted handler will deal with assigning ID.
        event.addSubordinate (annot);
      }
    }
    
    /** Invoked after an annotation has been deleted. */
    public void annotationDeleted(AnnotationModelEvent e) {
      
      AWBDocument doc = (AWBDocument) e.getModel();
      AWBAnnotation annot = e.getAnnotation();
      AnnotationType type = annot.getAnnotationType();

      if (type == ACE2004Utils.QUANTITY_TYPE) {
        // use ..Utils.deleteQuantity(...) so that warnings can be shown prior
      }
      if (type == ACE2004Utils.QUANTITY_MENTION_TYPE) {
        // use ..Utils.deleteQuantityMention(...) so that warnings can be shown prior
      }
      else if (type == ACE2004Utils.RELATION_TYPE) {
        // remove relation-mentions from relation first, then delete each
        HasSubordinates parent = (HasSubordinates) annot;
        AWBAnnotation[] mentions =
          parent.getSubordinates(ACE2004Utils.RELATION_MENTION_TYPE);
        parent.clearSubordinates(ACE2004Utils.RELATION_MENTION_TYPE);
        for (int i=0; i<mentions.length; i++) {
          doc.deleteAnnotation(mentions[i], false);
        }
        // the relation has already been 'deleted'
      }
      else if (type == ACE2004Utils.RELATION_MENTION_TYPE) {
        // clear it's subordinates. TODO: prompt to delete them
        // delete all "arguments" but not the mentions the reference
        ATLASElementSet args = annot.getRegion()
          .getSubordinateSet(ACE2004Utils.ARGUMENT_MENTION_TYPE);
        Iterator iter = args.iterator();
        while(iter.hasNext()) {
          Object next = iter.next();
          if (next instanceof AnnotationRef)
            next = ((AnnotationRef) next).getElement();
          doc.deleteAnnotation((AWBAnnotation) next, false);
        }
        // remove mention from it's relation
        HasSubordinates relation = task.getMentionParent(annot);
        if (relation != null)
          relation.removeSubordinate(annot);
        // If it's the last one removed, the relation is removed by the changed
        // handler here
      }
    }
    
    /** Invoked after an annotation has been changed. */
    public void annotationChanged(AnnotationModelEvent e) {
      
      AWBDocument doc = (AWBDocument) e.getModel();
      AnnotationChange change = e.getChange();
      AWBAnnotation annot = e.getAnnotation();
      AnnotationType type = annot.getAnnotationType();
      String propName = change.getPropertyName();

      if (ACE2004ToolKit.DEBUG > 0)
        System.err.println("A2k4TK.annotationChanged: annot: " +
                           IdRenderer.getShortId(annot, doc) +
                           " attr: " + propName + " changed from: " +
                           change.getOldValue() + " to: " +
                           change.getNewValue());

      if (type == ACE2004Utils.ENTITY_TYPE ||
          type == ACE2004Utils.RELATION_TYPE ||
          type == ACE2004Utils.QUANTITY_TYPE ||
          type == ACE2004Utils.EVENT_TYPE) {
        // when the type chages, make sure the subtype is valid, or clear it
        if ("type".equals(propName)) {
          String subtype = (String) annot.getAttributeValue("subtype");
          Set allowed = task.getPossibleValues(annot, "subtype");
          // don't worry about whether subtype is "allowed" if it is not set
          // to anything RK 2/2/06
          if (subtype != null && !subtype.equals("") &&
              !allowed.contains(subtype)) {
            if (ACE2004ToolKit.DEBUG > 0)
              System.err.println("A2Ktk.annotchanged: annot: "+IdRenderer.getShortId(annot, doc)+
                                 " bad subtype: "+subtype);
            try {
              // RK 2/2/06 changed from null to ""
              // to avoid odd behavior of that table column when first clicked
              annot.setAttributeValue("subtype", "");
            } catch (UnmodifiableAttributeException x) {};
          }
          if (subtype == null) {
            if (ACE2004ToolKit.DEBUG > 0)
              System.err.println("A2Ktk.annotchanged: annot: " +
                                 IdRenderer.getShortId(annot, doc) +
                                 " setting null subtype to empty string");

            try {
              // RK 2/2/06 change from null to ""
              // to avoid odd behavior of that table column when first clicked
              annot.setAttributeValue("subtype", "");
            } catch (UnmodifiableAttributeException x) {};
          }
        }
      }
      else if (type == ACE2004Utils.RELATION_MENTION_TYPE) {
        // when a referenced entity-mention changes, remove the rel-mention
        // from its relation
        // RK 2/6/07 ...but be careful -- if relation mention was looking
        // to parent relation for entity argument(s), they must be copied
        // to the new relation parent.
        if ("arg1".equals(propName) || "arg2".equals(propName)) {

          HasSubordinates relation = ACE2004Task.getMentionParent(annot);
          if (relation != null) {
            AWBAnnotation[] mentions =
              relation.getSubordinates(ACE2004Utils.RELATION_MENTION_TYPE);
          
            // changing one of the entity-mentions breaks co-referance
            if (mentions.length > 1) {
              relation.removeSubordinate(annot);
              AWBAnnotation oldRelation = relation;

              // forget the old one
              relation = (HasSubordinates)
                doc.createAnnotation(ACE2004Utils.RELATION_TYPE);

              // check for null entity-mention args that "look up" to 
              // the relation's entity args, and copy them to the new 
              // relation -- need to check both regardless of which
              // is being changed
              AWBAnnotation arg1 = 
                (AWBAnnotation)annot.getAttributeValue("arg1");
              if (arg1 == null) {
                arg1 = (AWBAnnotation)oldRelation.getAttributeValue("arg1");
                if (arg1 != null) {
                  try {
                    relation.setAttributeValue("arg1", arg1);
                  } catch (Exception x) {}
                }
              }
              AWBAnnotation arg2 = 
                (AWBAnnotation)annot.getAttributeValue("arg2");
              if (arg2 == null) {
                arg2 = (AWBAnnotation)oldRelation.getAttributeValue("arg2");
                if (arg2 != null) {
                  try {
                    relation.setAttributeValue("arg2", arg2);
                  } catch (Exception x) {}
                }
              }
            
              // the annotationInserted handler will deal with assigning ID.
              relation.addSubordinate(annot);
            }

            // set the relations entity
            AWBAnnotation mention = (AWBAnnotation) change.getNewValue();
            AWBAnnotation entity = null;
            if (mention != null)
              entity = ACE2004Task.getMentionParent(mention);

            if (DEBUG > 0) {
              System.err.println("A2k4TK.aml.annotChanged p="+propName+
                                 " m="+(mention==null?null:mention.getAttributeValue("ace_id"))+
                                 " e="+(entity==null?null:entity.getAttributeValue("ace_id")));
            }
            try {
              relation.setAttributeValue(propName, entity);
            } catch (UnmodifiableAttributeException x) {}

            // After this change coreferencing may be needed again.
            doc.putClientProperty(ACE2004Task.RELATIONS_COREFERENCED, Boolean.FALSE);
          }
        }
      }// RELATION_MENTION_TYPE
    }
    
    /** Invoked after an annotation has had subannotations added. */
    public void annotationInserted(AnnotationModelEvent e) {
      
      AWBDocument doc = (AWBDocument) e.getModel();
      HasSubordinates annot = (HasSubordinates) e.getAnnotation();
      AnnotationType type = annot.getAnnotationType();
      AWBAnnotation[] inserted = e.getChange().getAnnotationsInserted();

      
      // yay for common structure, naming conventions, and code reuse!
      if (type == ACE2004Utils.ENTITY_TYPE ||
          type == ACE2004Utils.EVENT_TYPE ||
          type == ACE2004Utils.QUANTITY_TYPE) {
        for (int i=0; i<inserted.length; i++) {
          if (DEBUG > 0) {
      AWBAnnotation ent = (AWBAnnotation)inserted[i].getAttributeValue("entity-value");
      AWBAnnotation qnt = (AWBAnnotation)inserted[i].getAttributeValue("quantity-value");
      
      if (DEBUG > 0)
	  System.err.println("A2kTK.anInsert: "+
			     annot.getId()+":"+annot.getAttributeValue("ace_id")+
			     "  <- "+
			     inserted[i].getId()+":"+
			     inserted[i].getAttributeValue("ace_id")+
			     " ent="+(ent==null?null:ent.getAttributeValue("ace_id"))+
			     " qnt="+(qnt==null?null:qnt.getAttributeValue("ace_id")));
	  }
          AnnotationType iType = inserted[0].getAnnotationType();
          //System.err.println("ACE2004AnnotListener.insert annot of type " +
          //                   iType.getName());
          if (iType.getName().startsWith("-mention", type.getName().length())) {
            // ensure there's a primary
            if (annot.getAttributeValue("primary-mention") == null) {
              try {
                annot.setAttributeValue("primary-mention", inserted[i]);
              } catch (UnmodifiableAttributeException impossible) {}
            }
            // Reassign ID
            IdTracker.getIdTracker(doc).getAceId(inserted[i]);
          }
        }
      }
    }
    
    /** Invoked after an annotation has had subannotations removed. */
    public void annotationRemoved(AnnotationModelEvent e) {
      
      HasSubordinates annot = (HasSubordinates) e.getAnnotation();
      AnnotationType type = annot.getAnnotationType();
      AWBAnnotation[] removed = e.getChange().getAnnotationsRemoved();
      AnnotationType iType = removed[0].getAnnotationType();
      
      if (type == ACE2004Utils.ENTITY_TYPE ||
          type == ACE2004Utils.EVENT_TYPE ||
          type == ACE2004Utils.QUANTITY_TYPE) {
        // if primary mention removed, update it if possible. update the primary mention if possible
        for (int i=0; i<removed.length; i++) {
          AWBAnnotation primary = (AWBAnnotation) annot.getAttributeValue("primary-mention");
          if (removed[i] == primary) {
            String mentionTypeName = annot.getAnnotationType().getName()+"-mention";
            AWBAnnotation mentions[] =
              annot.getSubordinates(task.getAnnotationType(mentionTypeName));

            if (mentions.length > 0) {
              primary = mentions[0];
            } else {
              primary = null;
            }
            try {
               annot.setAttributeValue("primary-mention", primary);
            } catch (UnmodifiableAttributeException x) {}
          }
        }
      }
      else if (type == ACE2004Utils.RELATION_TYPE) {
        // if all mentions removed, delete the relation
        if (((HasSubordinates) annot).getSubordinates(ACE2004Utils.RELATION_MENTION_TYPE).length == 0)
          ACE2004Utils.deleteRelation(annot, (AWBDocument) e.getModel());
      }
    }
  }
}
