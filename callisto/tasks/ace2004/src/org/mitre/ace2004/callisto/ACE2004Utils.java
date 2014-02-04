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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.URI;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.*;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleConstants;

import gov.nist.atlas.*;
import gov.nist.atlas.ref.AnnotationRef;
import gov.nist.atlas.type.*;
import gov.nist.atlas.util.ATLASElementSet;

import org.mitre.jawb.atlas.*;
import org.mitre.jawb.gui.*;
import org.mitre.jawb.swing.*;
import org.mitre.jawb.tasks.Task;

/**
 * Common code for manipulating ACE2004 annotations.
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */
public class ACE2004Utils  {

  private static final int DEBUG = 0;
  
  private static final String LAST_MENTION_MESSAGE =
    "You are removing the last Mention of an Entity\n"+
    "This will delete the Entity.";

  static ACE2004Task task = ACE2004Task.getInstance ();
  
  public static final AnnotationType ENTITY_TYPE =
    task.getAnnotationType (task.ENTITY_TYPE_NAME);
  public static final AnnotationType ENTITY_MENTION_TYPE =
    task.getAnnotationType(task.ENTITY_MENTION_TYPE_NAME);

  public static final AnnotationType RELATION_TYPE =
    task.getAnnotationType (task.RELATION_TYPE_NAME);
  public static final AnnotationType RELATION_MENTION_TYPE =
    task.getAnnotationType (task.RELATION_MENTION_TYPE_NAME);
  public static final AnnotationType RELATION_MENTION_EXTENT_TYPE =
    task.getAnnotationType (task.RELATION_MENTION_EXTENT_TYPE_NAME);

  public static final AnnotationType QUANTITY_TYPE =
    task.getAnnotationType (task.QUANTITY_TYPE_NAME);
  public static final AnnotationType QUANTITY_MENTION_TYPE =
    task.getAnnotationType (task.QUANTITY_MENTION_TYPE_NAME);
  
  public static final AnnotationType TIMEX2_TYPE =
    task.getAnnotationType (task.TIMEX2_TYPE_NAME);
  
  public static final AnnotationType EVENT_TYPE =
    task.getAnnotationType (task.EVENT_TYPE_NAME);
  public static final AnnotationType EVENT_MENTION_TYPE =
    task.getAnnotationType (task.EVENT_MENTION_TYPE_NAME);
  public static final AnnotationType EVENT_MENTION_EXTENT_TYPE =
    task.getAnnotationType (task.EVENT_MENTION_EXTENT_TYPE_NAME);

  public static final AnnotationType ARGUMENT_TYPE =
    task.getAnnotationType (task.ARGUMENT_TYPE_NAME);
  public static final AnnotationType ARGUMENT_MENTION_TYPE =
    task.getAnnotationType (task.ARGUMENT_MENTION_TYPE_NAME);

  /** convience method */
  public static Component getFocusOwner () {
    return KeyboardFocusManager.getCurrentKeyboardFocusManager()
      .getPermanentFocusOwner();
  }

  /**
   * Throws an IllegalArgumentException if the annot is not of the specified
   * type.
   */
  public static final void verifyAnnotationType (AWBAnnotation annot,
                                                 AnnotationType type) {
    AnnotationType annotType = annot.getAnnotationType();
    if (! type.equals (annotType))
      throw new IllegalArgumentException ("annot type="+type);
  }

  /**
   * Deletes an entity, deleting it's mentions, and referant arguments.
   */
  public static boolean deleteEntityMention (AWBAnnotation annot,
                                             AWBDocument doc) {
    if (DEBUG > 0)
      System.err.println ("ACE2004Utils.deleteEntityMent");
    
    boolean lastMention = false;
    
    // this cast should never fail, else it's an error in use
    MultiPhraseAnnotation mention = (MultiPhraseAnnotation) annot;
    HasSubordinates entity = task.getMentionParent(mention);
    HasSubordinates[] relations = task.getMentionRelations(mention);
    
    if (entity != null) {
      lastMention = entity.getSubordinates(ENTITY_MENTION_TYPE).length == 1;
    }
    
    // prompt for all the deletions
    if (lastMention || relations.length > 0) {
      StringBuffer message = new StringBuffer();
      StringBuffer title = new StringBuffer("Delete Mention's ");
      if (lastMention) {
        message.append("You are removing the last Mention of an Entity,\n")
          .append("which will be deleted as well.");
        title.append("Entity");
      }
      if (relations.length > 0) {
        message.append(lastMention?"\n\n":"")
          .append("This Mention").append(lastMention?" also":"")
          .append(" participates in ").append(relations.length)
          .append(" Relation Mention").append(relations.length>1?"s":"")
          .append(" which will also be deleted.");
        title.append(lastMention?" and ":"").append("Relations");
      }

      message.append("\n\nDo you wish to continue?");
      
      int choice =
        JOptionPane.showConfirmDialog (getFocusOwner(), message.toString(),
                                       title.toString(),
                                       JOptionPane.OK_CANCEL_OPTION,
                                       JOptionPane.QUESTION_MESSAGE);
      if (choice != JOptionPane.OK_OPTION)
        return false;
    }

    // deleting arg-mention won't propogate and delete others (except arg)
    AWBAnnotation[] arguments =
      ACE2004Task.getParents(annot, ACE2004Task.ARGUMENT_MENTION_TYPE_NAME);
    for (int i=0; i<arguments.length; i++)
      deleteArgumentMention(arguments[i], doc);
    
    // remove eMention from relMentions and delete them
    for (int i=0; i<relations.length; i++) {
      //relations[i].removeSubordinate(mention); // not needed
      deleteRelationMention(relations[i], doc);
    }

    // remove from entity and delete if last
    if (entity != null) {
      entity.removeSubordinate(mention);

      if (lastMention) {
        // removingSubordinates clears 'primary-mention'
        deleteEntity(entity, doc);
      } else {
        // set primary to next available
        AWBAnnotation next = entity.getSubordinates(ENTITY_MENTION_TYPE)[0];
        try {
          entity.setAttributeValue("primary-mention", next);
        } catch (UnmodifiableAttributeException x) {}
      }
    }

    // finally kill the mention
    return doc.deleteAnnotation(annot, false);
  }

  /**
   * Deletes an entity, prompting the user to delete the mentions contained
   * within it.
   */
  public static boolean deleteEntity (AWBAnnotation annot, AWBDocument doc) {
    if (DEBUG > 0)
      System.err.println ("ACE2004Utils.delEntity");
    
    verifyAnnotationType (annot, ENTITY_TYPE);
    boolean delMentions = false;
    boolean delRelations = false;

    HasSubordinates entity = (HasSubordinates) annot;
    AWBAnnotation[] mentions = entity.getSubordinates (ENTITY_MENTION_TYPE);
    AWBAnnotation[] relations = task.getParents(entity, task.RELATION_TYPE_NAME);
      
    if (mentions.length > 0) {
      StringBuffer buf = new StringBuffer ();
      String plural = (mentions.length == 1) ? "" : "s";
      buf.append("You are deleting an Entity with ");
      buf.append(mentions.length).append (" mention").append (plural);
      buf.append("\nWould you like to delete the mention").append (plural);
      buf.append(" also?");
      int choice =
        JOptionPane.showConfirmDialog (getFocusOwner (), buf.toString (),
                                       "Delete Mentions of Entity?",
                                       JOptionPane.YES_NO_CANCEL_OPTION,
                                       JOptionPane.QUESTION_MESSAGE);

      if (choice == JOptionPane.YES_OPTION) {
        delMentions = true;
      }
      else if (choice != JOptionPane.YES_OPTION) { //CANCEL || CLOSE
        return false;
      }
    }

    if (relations.length > 0) {
      StringBuffer buf = new StringBuffer ();
      String plural = (mentions.length == 1) ? "" : "s";
      buf.append ("You are deleting an Entity with ");
      buf.append (relations.length).append (" Relation").append (plural);
      buf.append ("\nWould you like to delete the relation").append (plural);
      buf.append (" also?");
      int choice =
        JOptionPane.showConfirmDialog (getFocusOwner (), buf.toString (),
                                       "Delete Relations of Entity?",
                                       JOptionPane.YES_NO_CANCEL_OPTION,
                                       JOptionPane.QUESTION_MESSAGE);

      if (choice == JOptionPane.YES_OPTION) {
        delRelations = true;
      }
      else if (choice != JOptionPane.NO_OPTION) { // CANCEL || CLOSE
        return false;
      }
    }

    // now do the deeds:
    if (delMentions) {
      entity.clearSubordinates(ENTITY_MENTION_TYPE);
      // clearing subs also clears role identified subs (primary-mention)
      for (int i=0; i<mentions.length; i++) {
        deleteEntityMention(mentions[i], doc);
      }
    }
    if (delRelations) {
      for (int i=0; i<relations.length; i++) {
        //relations[i].removeSubordinate(entity); not needed
        deleteRelation(relations[i], doc);
      }
    }

    // kill the arguments referring to this entity first
    AWBAnnotation[] arguments =
      ACE2004Task.getParents(entity, ACE2004Task.ARGUMENT_TYPE_NAME);
    for (int i=0; i<arguments.length; i++) {
      deleteArgument(arguments[i], doc);
    }
      
    return doc.deleteAnnotation(entity, false);
  }
  
  public static boolean removeMentionFromEntity (AWBAnnotation mention,
                                                 AWBAnnotation entity,
                                                 AWBDocument doc) {
    if (DEBUG > 0)
      System.err.println ("ACE2004Utils.removeMentionFrom Entity");
    
    verifyAnnotationType (mention, ENTITY_MENTION_TYPE);
    verifyAnnotationType (entity, ENTITY_TYPE);

    AWBAnnotation[] mentions =
      ((HasSubordinates)entity).getSubordinates (ENTITY_MENTION_TYPE);
    if (DEBUG > 0)
      System.err.println ("ACE2004Utils.rmMfrE: mentionCnt="+mentions.length);
    
    if (mentions.length > 1) {
      // if this mention is the primary, set to an arbitrary other, or 'null'
      final String PRIMARY_MENTION = "primary-mention";
      AWBAnnotation primary =
        (AWBAnnotation)entity.getAttributeValue(PRIMARY_MENTION);
      
      if (mention.equals (primary)) {
        Object nextMention = null;
        for (int i=0; i<mentions.length; i++) { // break at max=2
          if (! (nextMention = mentions[i]).equals (mention))
            break;
        }
        try {
          if (DEBUG > 1)
            System.err.println ("ACE2004Utils.rmMfrE: setting entity "+
                                entity.getId ()+"'s primary to "+
                                ((AWBAnnotation)nextMention).getId());
          if (! entity.setAttributeValue (PRIMARY_MENTION, nextMention)) {
            primary=(AWBAnnotation) entity.getAttributeValue(PRIMARY_MENTION);
            System.err.println ("ACE2004Utils.rmMfrE: failed! entity="+
                                entity.getId ()+" primary="+
                                (primary==null?null:primary.getId()));
            // don't delete the mention since we can't replace it as primary
            return false;
          }
        } catch (UnmodifiableAttributeException x) {}
        
      } else if (DEBUG > 0) {
        System.err.println ("ACE2004Utils.rmMfrE: mention isn't primary of "+
                            entity.getId ()+": primary="+primary.getId()+
                            " mention="+mention.getId());
      }
    } else if (mentions.length == 1) {
      String msg = LAST_MENTION_MESSAGE;
      String title = "Delete Mention's Entity?";
      
      int choice =
        JOptionPane.showConfirmDialog (getFocusOwner(), msg, title,
                                       JOptionPane.OK_CANCEL_OPTION,
                                       JOptionPane.WARNING_MESSAGE);
      if (choice != JOptionPane.OK_OPTION)
        return false;

      // clear the primary mention, remove mention and delete. Use deleteEntity
      // method so relations are deleted, etc...
      try {
        if (! entity.setAttributeValue("primary-mention", null) ||
            ! ((HasSubordinates)entity).removeSubordinate (mention) ||
            ! deleteEntity(entity, doc)) {
          System.err.println("ACE2004Utils.rmMfrE: can't delete entity -- aborting");
          entity.setAttributeValue("primary-mention", mention);
          ((HasSubordinates)entity).addSubordinate(mention);
          GUIUtils.showWarning("Unable to delete entity");
          return false;
        }
        return true;
      } catch (UnmodifiableAttributeException x) {}
    }
    
    // happens if we needed to delete the entity or not
    return ((HasSubordinates)entity).removeSubordinate (mention);
  }
  
  public static boolean removeMentionFromEvent (AWBAnnotation mention,
                                                AWBAnnotation event,
                                                AWBDocument doc) {
    if (DEBUG > 0)
      System.err.println ("ACE2004Utils.removeMentionFrom Event");
    
    verifyAnnotationType (mention, EVENT_MENTION_TYPE);
    verifyAnnotationType (event, EVENT_TYPE);

    AWBAnnotation[] mentions =
      ((HasSubordinates)event).getSubordinates (EVENT_MENTION_TYPE);
    if (DEBUG > 0)
      System.err.println ("ACE2004Utils.rmMfrE: mentionCnt="+mentions.length);
    
    if (mentions.length > 1) {
      // if this mention is the primary, set to an arbitrary other, or 'null'
      final String PRIMARY_MENTION = "primary-mention";
      AWBAnnotation primary =
        (AWBAnnotation)event.getAttributeValue(PRIMARY_MENTION);
      
      if (mention.equals (primary)) {
        Object nextMention = null;
        for (int i=0; i<mentions.length; i++) { // break at max=2
          if (! (nextMention = mentions[i]).equals (mention))
            break;
        }
        try {
          if (DEBUG > 1)
            System.err.println ("ACE2004Utils.rmMfrE: setting event "+
                                event.getId ()+"'s primary to "+
                                ((AWBAnnotation)nextMention).getId());
          if (! event.setAttributeValue (PRIMARY_MENTION, nextMention)) {
            primary=(AWBAnnotation) event.getAttributeValue(PRIMARY_MENTION);
            System.err.println ("ACE2004Utils.rmMfrE: failed! event="+
                                event.getId ()+" primary="+
                                (primary==null?null:primary.getId()));
            // don't delete the mention since we can't replace it as primary
            return false;
          }
        } catch (UnmodifiableAttributeException x) {}
        
      } else if (DEBUG > 0) {
        System.err.println ("ACE2004Utils.rmMfrE: mention isn't primary of "+
                            event.getId ()+": primary="+primary.getId()+
                            " mention="+mention.getId());
      }
    } else if (mentions.length == 1) {
      //String msg = LAST_MENTION_MESSAGE;
      String msg =
        "You are removing the last Mention of an Event\n"+
        "This will delete the Event.";
      String title = "Delete Mention's Event?";
      
      int choice =
        JOptionPane.showConfirmDialog (getFocusOwner(), msg, title,
                                       JOptionPane.OK_CANCEL_OPTION,
                                       JOptionPane.WARNING_MESSAGE);
      if (choice != JOptionPane.OK_OPTION)
        return false;
      
      // clear the primary mention, remove mention and delete. Use deleteEntity
      // method so relations are deleted, etc...
      try {
        if (! event.setAttributeValue("primary-mention", null) ||
            ! ((HasSubordinates)event).removeSubordinate (mention) ||
            ! deleteEvent(event, doc)) {
          System.err.println("ACE2004Utils.rmMfrE: can't delete event -- aborting");
          event.setAttributeValue("primary-mention", mention);
          ((HasSubordinates)event).addSubordinate(mention);
          GUIUtils.showWarning("Unable to delete event");
          return false;
        }
        return true;
      } catch (UnmodifiableAttributeException x) {}
    }
    
    // happens if we needed to delete the event or not
    return ((HasSubordinates)event).removeSubordinate (mention);
  }

  /**
   * Deletes a relation mention, deleting the relation if it's final.
   */
  public static boolean deleteRelationMention (AWBAnnotation annot,
                                               AWBDocument doc) {
    if (DEBUG > 0)
      System.err.println ("ACE2004Utils.deleteRelMent");

    HasSubordinates mention = (HasSubordinates) annot;
    HasSubordinates parent = ACE2004Task.getMentionParent(annot);
    if (parent != null) {
      if (parent.getSubordinates(RELATION_MENTION_TYPE).length == 1)
        return deleteRelation(parent, doc);
      else
        parent.removeSubordinate(annot);
    }

    // remove arguments, then delete them
    AWBAnnotation[] arguments = mention.getSubordinates(ARGUMENT_MENTION_TYPE);
    mention.clearSubordinates(ARGUMENT_MENTION_TYPE);
    for (int i=0; i<arguments.length; i++) {
      deleteArgumentMention(arguments[i], doc);
    }
  
    // remove mention refs
    try {
      mention.setAttributeValue("arg1", null);
      mention.setAttributeValue("arg2", null);
    } catch (UnmodifiableAttributeException x) {}
  
    // Delete the single, required relation-mention-extent annotation
    try {
	TextExtentRegion relMenExtent = (TextExtentRegion) mention.getAttributeValue("relation-mention-extent");
	mention.setAttributeValue("relation-mention-extent", null);
	doc.deleteAnnotation(relMenExtent, false);
    } catch (UnmodifiableAttributeException x) {}

    // finally kill the mention
    return doc.deleteAnnotation(mention, false);
  }


  /**
   * Deletes a relation mention via its relation mention extent,
   * deleting the relation if it's final.
   */
  public static boolean deleteRelationMentionExtent (AWBAnnotation annot,
						     AWBDocument doc) {
    if (DEBUG > 0)
      System.err.println ("ACE2004Utils.deleteRelMentExtent");

    TextExtentRegion relMentionExtent = (TextExtentRegion) annot;
    HasSubordinates relMention = ACE2004Task.getMentionParent(annot);
    if (relMention != null &&
	relMention instanceof HasSubordinates &&
	((Annotation)relMention).getAnnotationType().getName().equals(ACE2004Task.RELATION_MENTION_TYPE_NAME)) {
	if (DEBUG > 0) {
	    System.err.println("(deleteRelationMentionExtent) found a relMention.");
	}
        return deleteRelationMention(relMention, doc);
    }
    System.err.println("deleteRelationMentionExtent) couldn't find a relMention; nothing deleted.");
    return false;
  }


  /**
   * Deletes a relation, deleting it's mentions, and referant arguments.
   */
  public static boolean deleteRelation (AWBAnnotation annot,
                                        AWBDocument doc) {
    if (DEBUG > 0)
      System.err.println ("ACE2004Utils.deleteRelation");

    verifyAnnotationType (annot, RELATION_TYPE);
    HasSubordinates relation = (HasSubordinates) annot;

    AWBAnnotation[] mentions = relation.getSubordinates(RELATION_MENTION_TYPE);
    AWBAnnotation[] arguments = relation.getSubordinates(ARGUMENT_TYPE);

    // delete arguments silently, delete relation silently if only one mention
    if (mentions.length > 1) {
      StringBuffer buf = new StringBuffer();
      buf.append("You are deleting a Relation with ")
        .append(mentions.length).append(" mentions.")
        .append("\nDeleting the Relation will delete them as well.")
        .append("\n\nDo you wish to proceed?");
      
      int choice =
        JOptionPane.showConfirmDialog(getFocusOwner(), buf.toString(),
                                      "Delete Mentions of Relation?",
                                      JOptionPane.OK_CANCEL_OPTION,
                                      JOptionPane.QUESTION_MESSAGE);
      if (choice != JOptionPane.OK_OPTION)
        return false;
    }

    // remove arguments, then delete
    relation.clearSubordinates(ARGUMENT_TYPE);
    for (int i=0; i<arguments.length; i++) {
      deleteArgument(arguments[i], doc);
    }
  
    // remove mentions so deletion of them doesn't come back here!
    relation.clearSubordinates(RELATION_MENTION_TYPE);
    for (int i=0; i<mentions.length; i++) {
      deleteRelationMention(mentions[i], doc);
    }
    
    // remove entity refs
    try {
      relation.setAttributeValue("arg1", null);
      relation.setAttributeValue("arg2", null);
    } catch (UnmodifiableAttributeException x) {}
  
    // finally kill the relation
    return doc.deleteAnnotation(relation, false);
  }
  
  /**
   * Deletes an quantity, deleting it's mentions, and referant arguments.
   */
  public static boolean deleteQuantityMention (AWBAnnotation annot,
                                               AWBDocument doc) {
    if (DEBUG > 0)
      System.err.println ("ACE2004Utils.deleteQuantMent");

    // delete all coreferenat Quantities by deleting the quantity... each
    // mention will end up back here with /no/ parent.
    HasSubordinates parent = ACE2004Task.getMentionParent(annot);
    if (parent != null) {
      if (parent.getSubordinates(QUANTITY_MENTION_TYPE).length == 1)
        return deleteQuantity(parent, doc);
      else
        parent.removeSubordinate(annot);
    }

    // delete the arguments referring to this quantity
    AWBAnnotation[] arguments =
      ACE2004Task.getParents(annot, ACE2004Task.ARGUMENT_MENTION_TYPE_NAME);
    if (DEBUG > 0)
	System.err.println("   delQMent: args="+Arrays.asList(arguments));
    for (int i=0; i<arguments.length; i++) {
      deleteArgumentMention(arguments[i], doc);
    }
    
    // finally kill the quantity
    return doc.deleteAnnotation(annot, false);
  }

  /**
   * Deletes an quantity, deleting it's mentions, and referant arguments.
   */
  public static boolean deleteQuantity (AWBAnnotation annot, AWBDocument doc) {
    if (DEBUG > 0)
      System.err.println ("ACE2004Utils.deleteQuantity");

    verifyAnnotationType (annot, QUANTITY_TYPE);
    HasSubordinates quantity = (HasSubordinates) annot;

    AWBAnnotation[] mentions = quantity.getSubordinates(QUANTITY_MENTION_TYPE);
    AWBAnnotation[] arguments =
      ACE2004Task.getParents(annot, ACE2004Task.ARGUMENT_TYPE_NAME);

    // delete arguments silently, delete mentions silently if only one
    if (mentions.length > 1) {
      
      StringBuffer buf = new StringBuffer();
      buf.append("You are deleting a Value with ")
        .append(mentions.length).append(" mention")
        .append(mentions.length>1?"s":"")
        .append("\nwhich will also be deleted.")
        .append("\n\nDo you wish to proceed?");
        
      int choice =
        JOptionPane.showConfirmDialog (getFocusOwner (), buf.toString (),
                                       "Delete Mentions and Referring Arguments of Value?",
                                       JOptionPane.OK_CANCEL_OPTION,
                                       JOptionPane.QUESTION_MESSAGE);
      if (choice != JOptionPane.OK_OPTION)
        return false;
    }
      
    // delete the arguments referring to this quantity
    for (int i=0; i<arguments.length; i++) {
      deleteArgument(arguments[i], doc);
    }
      
    // remove mentions so deletion of them doesn't come back here!
    quantity.clearSubordinates(QUANTITY_MENTION_TYPE);
    // also clears primary-mention
    for (int i=0; i<mentions.length; i++) {
      deleteQuantityMention(mentions[i], doc);
    }

    // finally kill the quantity
    return doc.deleteAnnotation(quantity, false);
  }

  /**
   * Deletes a timex2.
   */
  public static boolean deleteTimex2 (AWBAnnotation annot, AWBDocument doc) {
    if (DEBUG > 0)
      System.err.println ("ACE2004Utils.deleteTimex2");

    verifyAnnotationType (annot, TIMEX2_TYPE);
    // TextExtentRegion timex2 = (TextExtentRegion) annot;

    AWBAnnotation[] arguments =
      ACE2004Task.getParents(annot, ACE2004Task.ARGUMENT_MENTION_TYPE_NAME);

    Iterator referants = annot.getReferentElements().iterator();
    while (referants.hasNext()) {
	ATLASElement referant = (ATLASElement)referants.next();
        if (DEBUG > 0)
	    System.err.println("ACE2004Utils.deleteTimex2: referant: "+referant);
    }
      

    // delete the arguments referring to this quantity
    for (int i=0; i<arguments.length; i++) {
	if (DEBUG > 0)
	    System.err.println ("ACE2004Utils.deleteTimex2: deleteArgument");
	if (DEBUG > 0)
	    System.err.println ("ACE2004Utils.deleteTimex2: arg[i] = " + arguments[i]);
      deleteArgumentMention(arguments[i], doc);
    }
      
    // finally kill the quantity
    return doc.deleteAnnotation(annot, false);
  }

  /**
   * Deletes an event mention extent.
   */
  public static boolean deleteEventMentionExtent (AWBAnnotation annot,
                                                  AWBDocument doc) {
    if (DEBUG > 0)
      System.err.println ("ACE2004Utils.deleteEvtMentExtent");

    // delete all coreferenat Quantities by deleting the quantity... each
    // mention will end up back here with /no/ parent.
    AWBAnnotation mention = 
      ACE2004Task.getParent(annot, ACE2004Task.EVENT_MENTION_TYPE_NAME);
    if (mention != null) {
      try {
        mention.setAttributeValue("extent", null);
      } catch (UnmodifiableAttributeException x) {}
      deleteEventMention(mention, doc);
    }

    return doc.deleteAnnotation(annot, false);
  }

  /**
   * Deletes an event mention
   */
  public static boolean deleteEventMention (AWBAnnotation annot,
                                            AWBDocument doc) {
    if (DEBUG > 0)
      System.err.println ("ACE2004Utils.deleteEvtMent");

    verifyAnnotationType (annot, EVENT_MENTION_TYPE);

    // bah... because it has start/end anchors, the event mention becomes a
    // TextAnnot, not a 'has subordinates'
    HasSubordinates parent = ACE2004Task.getMentionParent(annot);
    
    if (parent != null) {
      if (parent.getSubordinates(EVENT_MENTION_TYPE).length == 1)
        return deleteEvent(parent, doc);
      else
        parent.removeSubordinate(annot);
    }

    // delete args silently (gah!... again with the non HasSubordinates)
    //   create an array so we don't get 'concurrent modification' exceptions
    //   by deleting while iterating
    Region region = annot.getRegion();
    ATLASElementSet sub = region.getSubordinateSet(ARGUMENT_MENTION_TYPE);
    //AWBAnnotation[] arguments = new AWBAnnotation[sub.size()];
    Iterator subIter = sub.iterator();
    while (subIter.hasNext()) {
      // we must turn into the AWBAnnot to use the deleteArgument()...
      Object next = subIter.next();
      if (next instanceof AnnotationRef)
	next = ((AnnotationRef) next).getElement();
      AWBAnnotation arg = (AWBAnnotation) next;
      //region.removeFromSubordinateSet((Annotation)subIter.next(
      subIter.remove();
      deleteArgumentMention(arg, doc);
    }
/*
    // gah!... worse, this doesn't propogate events! (we don't care here tho)
    //annot.clearSubordinates(ARGUMENT_MENTION_TYPE);
    for (int i=0; i<arguments.length; i++) {
      annot.getRegion().removeFromSubordinateSet(arguments[i]);
      deleteArgument(arguments[i], doc);
    }
*/  
    // finally kill the mention and it's extent (mention first so extent is not
    // referenced)
    boolean ok = doc.deleteAnnotation(annot, false);
    AWBAnnotation extent = (AWBAnnotation) annot.getAttributeValue("extent");
    if (extent != null)
      doc.deleteAnnotation(extent, false);

    return ok;
  }

  /**
   * Deletes an quantity, deleting it's mentions, and referant arguments.
   */
  public static boolean deleteEvent (AWBAnnotation annot, AWBDocument doc) {
    if (DEBUG > 0)
      System.err.println ("ACE2004Utils.deleteEvent");

    verifyAnnotationType (annot, EVENT_TYPE);
    HasSubordinates event = (HasSubordinates) annot;

    AWBAnnotation[] mentions = event.getSubordinates(EVENT_MENTION_TYPE);
    AWBAnnotation[] arguments = event.getSubordinates(ARGUMENT_TYPE);

    // delete arguments silently, delete silently if only one mention
    if (mentions.length > 1) {
      StringBuffer buf = new StringBuffer();
      buf.append("You are deleting an Event with ")
        .append(mentions.length).append(" mentions.")
        .append("\nDeleting the Event will delete them as well.")
        .append("\n\nDo you wish to proceed?");
      
      int choice =
        JOptionPane.showConfirmDialog (getFocusOwner (), buf.toString (),
                                       "Delete Mentions of Event?",
                                       JOptionPane.OK_CANCEL_OPTION,
                                       JOptionPane.QUESTION_MESSAGE);
      if (choice != JOptionPane.OK_OPTION)
        return false;
    }

    // remove mentions so deletion of them doesn't come back here!
    event.clearSubordinates(EVENT_MENTION_TYPE);
    for (int i=0; i<mentions.length; i++) {
      deleteEventMention(mentions[i], doc);
    }
    
    // remove arguments, then delete
    event.clearSubordinates(ARGUMENT_TYPE);
    for (int i=0; i<arguments.length; i++) {
      deleteArgument(arguments[i], doc);
    }
  
    // finally kill the event
    return doc.deleteAnnotation(event, false);
  }
  
  /**
   * Deletes an argument, removing it from parental annots
   */
  public static boolean deleteArgumentMention(AWBAnnotation annot,
                                              AWBDocument doc) {
    if (DEBUG > 0)
      System.err.println ("ACE2004Utils.deleteArgMent");

    verifyAnnotationType (annot, ARGUMENT_MENTION_TYPE);

    HasSubordinates parent = ACE2004Task.getMentionParent(annot);
    if (parent != null) {
      if (parent.getSubordinates(ARGUMENT_MENTION_TYPE).length == 1)
        return deleteArgument(parent, doc);
      else {
	  if (DEBUG > 0)
	      System.err.println ("ACE2004Utils.DeleteArguentMention removeSubordinate annot= "
				  + annot);
	  parent.removeSubordinate(annot);
      }
    }
    // remove from all eventmentions and relationmentions
    AWBAnnotation[] relations =
      ACE2004Task.getParents(annot, ACE2004Task.RELATION_MENTION_TYPE_NAME);
    for (int i=0; i<relations.length; i++)
      ((HasSubordinates) relations[i]).removeSubordinate(annot);
    
    AWBAnnotation[] events =
      ACE2004Task.getParents(annot, ACE2004Task.EVENT_MENTION_TYPE_NAME);
    for (int i=0; i<events.length; i++)
      events[i].getRegion().removeFromSubordinateSet(annot);

    // remove value refs
    try {
      annot.setAttributeValue("entity-value", null);
      annot.setAttributeValue("quantity-value", null);
      annot.setAttributeValue("timex2-value", null);
    } catch (UnmodifiableAttributeException x) {}
  
    // finally kill the quantity
    return doc.deleteAnnotation(annot, false);
  }

  /**
   * Deletes an argument, removing it from parental annots
   */
  public static boolean deleteArgument(AWBAnnotation annot, AWBDocument doc) {
    if (DEBUG > 0)
      System.err.println ("ACE2004Utils.deleteArgument");

    verifyAnnotationType (annot, ARGUMENT_TYPE);

    HasSubordinates argument = (HasSubordinates) annot;
    AWBAnnotation[] mentions = argument.getSubordinates(ARGUMENT_MENTION_TYPE);

    // remove mentions so deletion of them doesn't come back here! (no warn)
    argument.clearSubordinates(ARGUMENT_MENTION_TYPE);
    for (int i=0; i<mentions.length; i++) {
      deleteEventMention(mentions[i], doc);
    }
    
    // remove from all event and relation
    AWBAnnotation[] relations =
      ACE2004Task.getParents(annot, ACE2004Task.RELATION_TYPE_NAME);
    for (int i=0; i<relations.length; i++)
      ((HasSubordinates)relations[i]).removeSubordinate(annot);
    
    AWBAnnotation[] events =
      ACE2004Task.getParents(annot, ACE2004Task.EVENT_TYPE_NAME);
    for (int i=0; i<events.length; i++)
      ((HasSubordinates)events[i]).removeSubordinate(annot);
    
    // remove value refs
    try {
      annot.setAttributeValue("entity-value", null);
      annot.setAttributeValue("quantity-value", null);
      annot.setAttributeValue("timex2-value", null);
    } catch (UnmodifiableAttributeException x) {}
  
    // finally kill the quantity
    return doc.deleteAnnotation(annot, false);
  }
}

