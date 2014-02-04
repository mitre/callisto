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

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Arrays;
import javax.swing.*;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.AWBATLASImplementation;
import org.mitre.jawb.atlas.HasSubordinates;
import org.mitre.jawb.atlas.SubordinateSetsAnnotation;
import org.mitre.jawb.atlas.NamedExtentRegions;
import org.mitre.jawb.atlas.MultiPhraseAnnotation;
import org.mitre.jawb.atlas.PhraseTaggingAnnotation;

import org.mitre.jawb.gui.GUIUtils;
import org.mitre.jawb.tasks.AbstractTask;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;
import org.mitre.jawb.tasks.Autotagger;
import org.mitre.jawb.tasks.Exporter;
import org.mitre.jawb.tasks.Importer;
import org.mitre.jawb.prefs.PreferenceItem;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.swing.AutoSelectCaret;

import gov.nist.atlas.ATLASElement;
import gov.nist.atlas.Region;
import gov.nist.atlas.Corpus;
import gov.nist.atlas.Annotation;
import gov.nist.atlas.ATLASAccessException;
import gov.nist.atlas.type.*;
import gov.nist.maia.MAIAScheme;
import gov.nist.maia.MAIALoader;

import org.mitre.jawb.Jawb;
import org.mitre.ace2004.callisto.config.RNGParser;


/**
 * This is the place for any necessary hard-coded information about
 * the task to reside. This class is NOT intended to be extended.
 */
public class ACE2004Task extends AbstractTask {

  /* This version of the ACE Events task has been updated to reflect
   * Jade's requests of late 2006 
   *
   * Major changes:
   *  - Annotator can control which mention of an entity is the primary mention
   *  - Support for "Unmentioned Relations" which do not have a text extent or
   * any relation-mention subordinates.  Args are all Entities (not mentions)
   *  - Support for any relation mention having args that are entities rather
   * than entity mentions as usual.  The implementation for this involves
   * setting the entity-mention argument slot in the relation-mention to null,
   * and putting the entity argument into the correct slot in the parent
   * relation.  Therefore whenever null is found in an argument slot, we must
   * look up the chain to the parent relation to find the argument entity.
   * (This is currently supported for arg1 and arg2 -- coming soon support
   * for entities in the other arguments of both relations and events, where
   * before entity mentions were required.)
   */
  

  private static final int DEBUG = 0;
  
  public static final String ENTITY_TYPE_NAME = "ace_entity";
  public static final String ENTITY_MENTION_TYPE_NAME = "ace_entity-mention";
  
  public static final String RELATION_TYPE_NAME = "ace_relation";
  public static final String RELATION_MENTION_TYPE_NAME = "ace_relation-mention";
  public static final String RELATION_MENTION_EXTENT_TYPE_NAME = "ace_relation-mention-extent";
  
  public static final String QUANTITY_TYPE_NAME = "ace_quantity";
  public static final String QUANTITY_MENTION_TYPE_NAME = "ace_quantity-mention";
  
    public static final String TIMEX2_TYPE_NAME = "timex2";
  
  public static final String EVENT_TYPE_NAME = "ace_event";
  public static final String EVENT_MENTION_TYPE_NAME = "ace_event-mention";
  public static final String EVENT_MENTION_EXTENT_TYPE_NAME = "ace_event-mention-extent";

  public static final String ARGUMENT_TYPE_NAME = "ace_argument";
  public static final String ARGUMENT_MENTION_TYPE_NAME = "ace_argument-mention";

  public static final String UNMENTIONED_RELATION_STR = "unmentioned_ace_relation";

  /** Constant for doc runtime properties. Set false when relations change */
  public static final String RELATIONS_COREFERENCED = "Relations-Coreferenced";
  
  public static final String TASK_TITLE = "ACE Event Task";
  public static final String TASK_NAME = "org.mitre.ace2004";
  public static final String TASK_VERSION = "5.0.3.0";
  public static final String TASK_DESCRIPTION = 
    "Exploratory ACE Event Annotation Task.";

  public static final String ENTITY_ARGS_PREFERENCE_KEY =
    "org.mitre.ace2004.entityargs";

  /** used several times, so I made them constants */
  private static final String[] HIGHLIGHT_KEYS = {"mention.head",          // 0
                                                  "mention.full",          // 1
                                                  "mention.head.assigned", // 2
                                                  "quantity",              // 3
                                                  "timex2.extent",         // 4
                                                  "event.anchor",          // 5
                                                  "event.extent",          // 6
                                                  "relation.extent"};      // 7
  
  private static final String[] COLORS =         {"#33ffff",               // 0
                                                  "#9999ff",               // 1
                                                  "#6666ff",               // 2
                                                  "#66ff66",               // 3
                                                  "#332266",               // 4
                                                  "#ff33cc",               // 5
                                                  "#ff99ff",               // 6
                                                  "#332277"};              // 7

  /** Cononical reference for the MAIA scheme. */
  private static final String MAIA_SCHEME =
    "http://callisto.mitre.org/maia/ace2004.maia.xml";

  /** Local copy of MAIA Scheme, relative to the resources directory. */
  private static final String LOCAL_MAIA_SCHEME = "/resource/ace2004.maia.xml";
  



  /** Instantiate lazily, after of all static fields. */
  private static ACE2004Task task;

  private static File localDir = null;
  
  /* 
   * Hash table of next index for each known corpus
   */
  private HashMap rdcRelationIndex = new HashMap();

  private Set highlights = null;
  private Map defaultPrefs = null;
  private Boolean allowEntityArgs = null;
  
  private Importer[] importers = null;
  private Exporter[] exporters = null;

  /* Tree of possible values for attributes.
   *  constTree = (annotName* -> attrMap)
   *  attrMap   = (attriName* -> (valSet | constrMap ))
   *  valSet    = (value*)
   *  constMap  = (qualifyingAttrName -> (valSet))
   */
  Map constraintTree = null;
  /* Set of 'constraints' for valid relation types
   *  each value is: (e1Type+","+e2Type+","+relType+","+relSubtype);
   *    e1Type: entity typeName of the arg1 mention
   *    e2Type: entity typeName of the arg2 mention
   *    relType: an allowed relation between the entities
   *    relSubtype: an allowed subtype for the relType
   */
  Set relationConstraints = null;
  Set unmentionedRelationConstraints = null;

  /* Map of "constraints" for valid argument roles depending
   * on it's grandparent's (relation or event) type and subtype.
   *    (Map) Key:grand-parent-annottype-name -->
   *       (Map) Key:gp-type --> (Map) Key:gp-subtype --> (Set) role
   * Null is used as a key for "global values" so to get the full set of
   * possible values combine them. See getPossibleValues
   * ARGUMENT_MENTION_TYPE_NAME for more use.
   */
  Map argumentConstraints = null;

  /** Retrieve the singleton instance of this class. HACK: passing in the
   * 'localDir here, though this isn't a good place for it.  The TaskManager
   * and AbstractTask were edited too. */
  public static synchronized final ACE2004Task getInstance () {
    return getInstance(null);
  }
  public static synchronized final ACE2004Task getInstance (File file) {
    if (task == null) {
      if (file == null)
        throw new IllegalStateException
          ("Initial 'getInstance' call must include temp dir");
      ACE2004Task.localDir = file;
      task = new ACE2004Task ();
    }
    return task;
  }

  private ACE2004Task () {
    super (TASK_TITLE, TASK_NAME, TASK_VERSION, TASK_DESCRIPTION,
           MAIA_SCHEME, LOCAL_MAIA_SCHEME);

    initConstraints ();
    initHighlightKeys ();
    initDefaultPrefs ();
    initIO ();
  }
  
  /** Returns a new instance of {@link ACE2004ToolKit} on each invocation. */
  public TaskToolKit getToolKit () {
    return new ACE2004ToolKit (this);
  }

  public Importer[] getImporters () {
    return importers;
  }

  public Exporter[] getExporters () {
    return exporters;
  }

  public boolean entityArgsOK () {
    return allowEntityArgs.booleanValue();
  }

  /**
   * Returns a task-specific autotagger
   */
  public Autotagger getAutotagger() {
    if (autotagger == null)
      autotagger = new EntityMentionAutotagger();
    return autotagger;
  }


  /** A preference! */
  public PreferenceItem getPreferenceItem () {
    if (preferences == null)
      preferences = new ACE2004Prefs(this);
    return preferences;
  }

  /**
   * Default colors.
   */
  public Map getDefaultPreferences () {
    return defaultPrefs;
  }

  /**
   * Returns a Set of Strings representing the allowable values for a
   * given attribute in a given Annotation, possibly taking into
   * consideration the values of other attributes that are already
   * set.
   *
   * If there is not enough information in the Annotation to determine
   * the allowed values for the requested attribute, all values that
   * might be allowed in that field, given the information already
   * present, are returned.
   *
   * Returns "null" if the attribute is a valid one for the given 
   * AnnotationType and there are no restrictions on the value (can
   * be any string).
   *
   * Returns an empty Set if there are no valid values (if, for
   * example, the attribute is not valid for the AnnotationType, or
   * the values of other attributes are not consistent.
   */
  public Set getPossibleValues (AWBAnnotation annot, String attr) {

    AnnotationType annotType = annot.getAnnotationType();
    String typeName = annotType.getName();
    if (typeName.equals(RELATION_TYPE_NAME)) {
      boolean isUnmentioned = isUnmentionedRelation(annot);
      String annotTypeKey;
      if (isUnmentioned)
        annotTypeKey = UNMENTIONED_RELATION_STR;
      else
        annotTypeKey = RELATION_TYPE_NAME;

      // Type and Subtype depend on Arg1 and Arg2's type/role
      // and also on each other
      String arg1Type = null;
      String arg2Type = null;
      try {
        AWBAnnotation entity = null;

        entity = (AWBAnnotation) annot.getAttributeValue("arg1");
        if (entity != null)
          arg1Type = (String) entity.getAttributeValue("type");

        entity = (AWBAnnotation)annot.getAttributeValue("arg2");
        if (entity != null)
          arg2Type = (String) entity.getAttributeValue("type");

      } catch (Exception e) {
        if (DEBUG > 0)
          System.out.println("ACE2004Task.gpv:Error getting argX type: " +
                             e.getMessage());
      }

      if (attr.equals("type")) {
        // no longer restrict type based on subtype. only vice-versa
        if (DEBUG > 0) 
          System.err.println("ACE2004Task.gpv: for relation.type with" +
                             " arg1Type = '" + arg1Type + 
                             "' arg2Type = '" + arg2Type + "'");
        // return all type consistent with any args set
        return typesForArgs(arg1Type, arg2Type, isUnmentioned);
        
      } else if (attr.equals("subtype")) {
	Set validSubtypes = new LinkedHashSet();
	String rdcType = (String) annot.getAttributeValue("type");
        if (DEBUG > 0) 
          System.err.println("ACE2004Task.gpv: for relation.type with" +
                             " arg1Type = '" + arg1Type + 
                             "' arg2Type = '" + arg2Type +
			     "' rdcType = '" + rdcType + "'");
	if (rdcType == null || rdcType.equals("")) {
	  // type is not set
	  // return the union of all sets of subtypes
	  // for all types consistent with whatever args are set
	  Iterator typeIter = 
            typesForArgs(null, null, isUnmentioned).iterator();
	  while (typeIter.hasNext()) {
	    String tempType = (String)typeIter.next();
            if (tempType != null) {
              Iterator subtypeIter =
                getSubtypesForType(annotTypeKey, "subtype",tempType).iterator();
              while (subtypeIter.hasNext()) {
                String subtype = (String)subtypeIter.next();
                if (subtype != null && rdcArgsMatchTypes(arg1Type, arg2Type,
                                                         tempType, subtype,
                                                         isUnmentioned)) {
                  validSubtypes.add(subtype);
                }
              }
            }
	  }
	} else {
	  // type is set
          Iterator subtypeIter =
            getSubtypesForType(annotTypeKey, "subtype",rdcType).iterator();
	  while (subtypeIter.hasNext()) {
	    String subtype = (String)subtypeIter.next();
            if (subtype != null && rdcArgsMatchTypes(arg1Type, arg2Type,
                                                     rdcType, subtype,
                                                     isUnmentioned)) {
	      validSubtypes.add(subtype);
	    }
	  }
	}
        validSubtypes.add (null);
	return validSubtypes; // may be empty if nothing consistent found
      }
      // for attributes other than type and subtype, defer to the
      // AnnotationType version of this method.
      
    } else if (typeName.equals(ENTITY_MENTION_TYPE_NAME)) {
      // ace_entity-mention.role depends on ace_entity.type:
      // only GPE has role; 
      if (attr.equals("role")) {
	AWBAnnotation entity = getMentionParent(annot);
	if (entity != null) {
          Object entityType = entity.getAttributeValue("type");
          if (! (entityType == null ||
                 entityType.equals("") ||
                 entityType.equals("GPE")))
            return Collections.EMPTY_SET;
        }
        return (Set) ((Map) constraintTree.get(typeName)).get(attr);
        
      } else if (attr.equals("reference")) {
        // mention-ref values (metonymy) is irrelevant if mention is a GPE
	AWBAnnotation entity = getMentionParent(annot);
        if (entity != null) {
          Object entityType = entity.getAttributeValue("type");
          if ("GPE".equals(entityType))
            return Collections.EMPTY_SET;
        }
        return (Set) ((Map) constraintTree.get(typeName)).get(attr);

      }
      // defer to the AnnotationType version of this method.

    } else if (typeName.equals(ARGUMENT_MENTION_TYPE_NAME)) {
      
      if (attr.equals("role")) {
        return getPossibleArgumentRoles(annot);
      }
      // defer to the AnnotationType version of this method.
      
    } else if (typeName.equals(ENTITY_TYPE_NAME) ||
               typeName.equals(QUANTITY_TYPE_NAME) ||
               typeName.equals(EVENT_TYPE_NAME)) {
      if (attr.equals("subtype")) {
        return getSubtypesForType(typeName, attr,
                                  (String) annot.getAttributeValue("type"));
      }
      // defer to the AnnotationType version of this method.
    }
    
    // for all other annotation type, there are no instance restrictions
    return getPossibleValues(annotType, attr);
  }

  /**
   * Returns a Set of Strings representing the allowable values for a
   * given attribute of a given AnnotationType.
   *
   * Returns "null" if the attribute is a valid one for the given 
   * AnnotationType and there are no restrictions on the value (can
   * be any string).
   *
   * Returns an empty Set if there are no valid values (if the
   * attribute is not valid for the AnnotationType) -- maybe this
   * should thrown an exeption instead??
   *
   * Throws an InsufficientInformationException if the values allowed
   * for the given attribute depend on the values already set for
   * other attributes.  In this case the caller must call the version
   * of getPossibleValues that takes an actual Annotation object
   * rather than just an AnnotationType object to determine the
   * allowable values in the context of the existing Annotation.
   *
   * TODO: should it indicate that an attribute is optional by
   * including an empty string in the returned set?  (What about an
   * optional value with no other restrictions??)
   *
   * TODO: how to indicate restrictions like "any integer"
   *
   * TODO: how to indicate restrictions like RELTIME Val formats (for
   * now assume interface only creates good ones -- in general maybe
   * need a way to indicate that a validation method is needed and
   * force user to call a getValueValidator() method or something like
   * that.
   *
   * TODO: what about attributes that are not Parameter Content??
   * (for now return EMPTY_SET -- only Parameter Content attributes 
   * are considered "valid" from the point-of-view of this method)
   */
  public Set getPossibleValues (AnnotationType annotType, String attr) {
    if (DEBUG > 3)
      System.err.println("A2K4.gPV: t="+annotType.getName()+" a="+attr);
    return (Set) ((Map) constraintTree.get(annotType.getName())).get(attr);
  }

  /** 
   * Returns the default value for the given attribute of the given
   * annotation type, if there is one, or null if there is not.
   */
  public String getDefaultValue (AnnotationType annotType, String attr) {
    // attributes which had default values in RDC are no longer used
    return null;
  }

  /** 
   * Returns the default value for the given attribute of the given
   * annotation (taking into account other values already set), if
   * there is one, or null if there is not.
   */
  public String getDefaultValue (AWBAnnotation annot, String attr) {
    AnnotationType annotType = annot.getAnnotationType();
    if (annotType.getName().equals(ENTITY_MENTION_TYPE_NAME) &&
        attr.equals("role")) {
      AWBAnnotation entity = getMentionParent(annot);
      if (entity != null) {
        String entityType = (String) entity.getAttributeValue("type");
        if ("GPE".equals (entityType)) {
          return "GPE";
        }
      }
      return null;
    } else if (annotType.getName().equals(RELATION_TYPE_NAME) &&
               isUnmentionedRelation(annot)) {
      // return the first item in the .rng file
      return (String) ((Set) ((Map) constraintTree.get(UNMENTIONED_RELATION_STR)).get(attr)).iterator().next();
    }

               
    // defer to the AnnotaitonType version of the method
    return getDefaultValue(annotType, attr);
  }

  /**
   * Determines whether a subordinate is a valid filler for 'role' in the
   * superordinate.  If role is null, determines whether or not the subordinate
   * is a valid member of a subordinate set of the superordinate.  If the role
   * provided is not a valid role for the superordinate, or does not take an
   * Annotation, this method will return false.
   */
  public boolean isValidSubordinate(AWBAnnotation superordinate, 
				    AWBAnnotation subordinate,
				    String role) {
    AnnotationType superType = superordinate.getAnnotationType();
    AnnotationType subType = subordinate.getAnnotationType();
    // Check whether or not the subType is appropriate for the
    // superType and role, if provided
    if (!super.isValidSubordinate (superordinate, subordinate, role))
      return false;
    
    // If superordinate is an Entity and subordinate is an Entity-Mention, the
    // Mention is a valid subordinate iff it is not already in another
    // Entity (role must be null or primary-mention, but I don't
    // need to check that because if I had a valid role above, it will
    // be one of those)
    if (superType.getName().equals(ENTITY_TYPE_NAME)) {
      //subType must be Mention Type or we wouldn't have made it this far
      AWBAnnotation currentEntity = getMentionParent(subordinate);
      if (currentEntity == null || currentEntity.equals(superordinate)) {
	return true;
      } else {
	return false;
      }
    }

    // If superordinate is a Mention Relation and subordinate is a
    // Mention, the Mention may be a valid subordinate for role arg1
    // or arg2, according to whether or not its type is consistent
    // with the other arg type, if set, and the relation type and
    // subtype, if set. 
    // 
    // TODO! (if type and subtype are not set, may
    // need to cycle through possible values to see if any combination
    // would be valid with this subordinate)

    // if no other restrictions and we got this far, it must be a
    // valid subordiante.
    return true;	
  }

  /***********************************************************************/
  /* GUI methods */
  /***********************************************************************/

  public Set getHighlightKeys () {
    return highlights;
  }

  /**
   * If the annot is an ace_mention, the object constraint is expected to be
   * 'head' or 'full', which should be the values returned from calling
   * 'getExtentNames().
   */
  public String getHighlightKey (AWBAnnotation annot, Object constraint) {
    String name = annot.getAnnotationType().getName();
    if (name.equals (ENTITY_MENTION_TYPE_NAME)) {
      if ("head".equals (constraint)) {
	// check if assigned
	if (getMentionParent(annot) == null) {
	  return HIGHLIGHT_KEYS[0];
	} else {
	  return HIGHLIGHT_KEYS[2];
	}
      } else if ("full".equals (constraint)) {
	return HIGHLIGHT_KEYS[1];
      }
      throw new IllegalArgumentException ("constraint="+constraint);
    } else if (name.equals (QUANTITY_MENTION_TYPE_NAME)) {
      return HIGHLIGHT_KEYS[3];
    } else if (name.equals (TIMEX2_TYPE_NAME)) {
      return HIGHLIGHT_KEYS[4];
    } else if (name.equals (EVENT_MENTION_TYPE_NAME)) {
      return HIGHLIGHT_KEYS[5];
    } else if (name.equals (EVENT_MENTION_EXTENT_TYPE_NAME)) {
      return HIGHLIGHT_KEYS[6];
    } else if (name.equals (RELATION_MENTION_EXTENT_TYPE_NAME)) {
      return HIGHLIGHT_KEYS[7];
    }
    return null;
  }

  /***********************************************************************/
  /* Helper methods for getPossibleValues                                */
  /***********************************************************************/

  private Set getPossibleArgumentRoles(AWBAnnotation annot) {

    AWBAnnotation parent = getParent(annot, null);
      
    if (parent != null) {
      String parentTypeName = parent.getAnnotationType().getName();
      if (parentTypeName.equals(EVENT_MENTION_TYPE_NAME) ||
          parentTypeName.equals(RELATION_MENTION_TYPE_NAME)) {
        
        AWBAnnotation grandParent = getMentionParent(parent);
        
        if (grandParent != null) {
          String gpType = grandParent.getAnnotationType().getName();
          String type = (String) grandParent.getAttributeValue("type");
          String subtype = (String) grandParent.getAttributeValue("subtype");
          
          if (DEBUG > 2) {
            System.err.println("A2k4.gPV:arg-ment: grand="+
                               grandParent.getAttributeValue("ace_id")+
                               "\n                   type="+type+
                               "  subtype="+subtype);
          }
          
          LinkedHashSet possible = new LinkedHashSet();
          Map type2subMap = (Map) argumentConstraints.get(gpType);
          if (type2subMap != null) {
            
            Map subMap = (Map) type2subMap.get(type);
            if ( !(type == null || type.equals("")) && subMap != null) {
              // checked for type == null since we use that as a key for
              // 'available to all types'
              
              Set roles = (Set) subMap.get(subtype);
              if ( ! (subtype == null || subtype.equals("")) && roles != null) {
                // checked for subtype == null since we use that as a key for
                // 'available to all subtypes'
                possible.addAll(roles);
                
                // we've added one subtype, now add roles available to all subtypes
                possible.addAll((Set) subMap.get(null));
              }
              else { // unspecified subtype? add 'em all (least dangerous)
                Iterator iter = subMap.values().iterator();
                while (iter.hasNext()) {
                  possible.addAll((Set) iter.next());
                }
              }
              
              // we've added one type, now add roles available to all types
              possible.addAll((Set) ((Map) type2subMap.get(null)).get(null));
            }
            else { // unspecified type? add 'em all (least dangerous)
              Iterator iter = type2subMap.values().iterator();
              while (iter.hasNext()) {
                Iterator subiter = ((Map) iter.next()).values().iterator();
                while (subiter.hasNext()) {
                  possible.addAll((Set) subiter.next());
                }
              }
            }
            
            possible.add(null); // allow "unsetting" of the role
            return possible;
          }
          else
            System.err.println("A2k4.gPV:arg-ment: Invalid grandparent: "+gpType);
        }
        else
          System.err.println("A2k4.gPV:arg-ment: No grandParent: "+parent);
      }
      else
        System.err.println("A2k4.gPV:arg-ment: Unrecognized parent: "+parent);
    }
    else
      System.err.println("A2k4.gPV:arg-ment: Unknown containing type: "+annot);
    
    return getPossibleValues(annot.getAnnotationType(), "role");
  }
  
  // arg1Type and/or arg2Type may be null, in which case all types
  // which may possibly be valid are returned
  private Set typesForArgs(String arg1Type, String arg2Type) {
    return typesForArgs (arg1Type, arg2Type, false);
  }

  private Set typesForArgs(String arg1Type, String arg2Type, 
                           boolean isUnmentioned) {  
    
    Set validTypes = new LinkedHashSet();
    Map attr2valuesMap = null;
    String annotTypeKey;
      if (isUnmentioned)
        annotTypeKey = UNMENTIONED_RELATION_STR;
      else
        annotTypeKey = RELATION_TYPE_NAME;

    if (isUnmentioned) {
      attr2valuesMap = (Map) constraintTree.get(UNMENTIONED_RELATION_STR);
      if (DEBUG > 1)
        System.err.println("A2k4Task.types4args " + arg1Type + ", " 
                           + arg2Type + " unmentioned -- map is\n" +
                           attr2valuesMap);
    }
    else
      attr2valuesMap = (Map) constraintTree.get(RELATION_TYPE_NAME);
    
    Set typeSet = (Set) attr2valuesMap.get("type");
    if (DEBUG > 1) 
      System.err.println("A2k4Task.types4args typeSet= " + typeSet);

    if ((arg1Type == null || arg1Type.equals("")) && 
	(arg2Type == null || arg2Type.equals(""))) {
      return typeSet;
      
    } else {
      // check all possible types for any valid combinations and
      // add those that have valid combination to validTypes
      Iterator typeIter = typeSet.iterator();
      while (typeIter.hasNext()) {
	String type = (String)typeIter.next();
	if (type != null) {
          // check if the type and args are valid with empty subtype
          if (rdcArgsMatchTypes (arg1Type, arg2Type, type, null,
                                 isUnmentioned)) {
            validTypes.add(type);
          } else {
            // otherwise check the argtypes with the current type and
            // each possible subtype for that type
            Iterator subIter = 
              getSubtypesForType(annotTypeKey, "subtype", type).iterator();
            while (subIter.hasNext()) {
              String subtype = (String)subIter.next();
              if (rdcArgsMatchTypes(arg1Type, arg2Type, type, subtype,
                                    isUnmentioned)) {
                validTypes.add(type);
                break;
              }
            }
          }
	}
      }
      validTypes.remove (null);
      validTypes.add (null); // make sure it's last
    }
    // if args were inconsistent, this set will be empty
    return validTypes;
  }

  private Set getSubtypesForType(String annotName, String subAttrName,
                                 String keyValue) {
    if (DEBUG > 0) 
      System.err.println("A2K4Task.gSubs4Type: annot=" + annotName+
                         " sub" + subAttrName + " key="+keyValue);

    if (keyValue != null && keyValue.length() == 0)
      keyValue = null;

    Map attr2valuesMap = (Map) constraintTree.get(annotName);
    Map type2subtypesMap = (Map) attr2valuesMap.get(subAttrName);

    if (DEBUG > 1)
      System.err.println("a2k4Task.gSubs4Type type2subsMap = "
                         + type2subtypesMap);
    // TODO: used to return empty set when type==null, now returns all...
    //if (keyValue != null)
    //keyValue = keyValue.toUpperCase();
    if (type2subtypesMap == null)
      // return null; RK 1/26/07 -- returning null here can cause NPE in 
      // typesForArgs above, so return an empty set instead...
      // not sure if this will cause a problem with cases where all
      // subtypes should be allowed....
      return Collections.EMPTY_SET;
    
    Set subtypes = (Set) type2subtypesMap.get(keyValue);
    if (subtypes == null)
      subtypes = Collections.EMPTY_SET;
    return subtypes;
  }

  /**
   * Returns true iff the rdc type and subtype given are consistent
   * with the  edt types of arg1 and arg2.<p>
   *
   * arg1Type and/or arg2Type may be null, in which true is returned if
   * there exist values for these args that would make the relation
   * valid.<p>
   *
   * Type and subtype should always be set to valid relation type and
   * subtype values.  Subtype may be set to null, which will be accepted if
   * the given type does not have subtypes.
   */
  private boolean rdcArgsMatchTypes(String arg1Type, String arg2Type,
				    String type, String subtype,
                                    boolean isUnmentioned) {
      String annotTypeKey;
      if (isUnmentioned)
        annotTypeKey = UNMENTIONED_RELATION_STR;
      else
        annotTypeKey = RELATION_TYPE_NAME;

      // this was RELATION_TYPE_NAME (inside the first "get") but I
      // think that's wrong we want the various entity (arg) types to
      // rotate through in case no arg types are set yet -- RK 1/26/07
    Set edtTypes = (Set)((Map)constraintTree.get(ENTITY_TYPE_NAME)).get("type");
    if (DEBUG >1)
      System.err.println ("A2k4Task.argsMatch: edtTypes = " + edtTypes);
    if (arg1Type == null || arg1Type.equals("")) {
      if (arg2Type == null || arg2Type.equals("")) {
	// if both args are null return whether subtype is valid for type
        Set subtypes = getSubtypesForType(annotTypeKey, "subtype", type);
        return (subtypes.contains(subtype) || 
                (subtype == null && subtypes.isEmpty()));
      } else {
	// if only arg1 is null, return whether any arg1 makes a valid
	// combination with arg2, type and subtype
	//Iterator argIter = edtTypeValues.iterator();
        Iterator typeIter = edtTypes.iterator();
	while (typeIter.hasNext()) {
	  String maybeArg1Type = (String) typeIter.next();
	  if (maybeArg1Type != null &&
              rdcArgsMatchTypes(maybeArg1Type, arg2Type, type, subtype, isUnmentioned)) {
	    // found a valid combination:
	    return true;
          }
	}
	return false;
      }
    } else {
      if (arg2Type == null) {
	// if only arg2 is null, return whether any arg2 makes a valid
	// combination with arg1, type and subtype
	Iterator typeIter = edtTypes.iterator();
	while (typeIter.hasNext()) {
	  String maybeArg2Type = (String) typeIter.next();
	  if (maybeArg2Type != null &&
              rdcArgsMatchTypes(arg1Type, maybeArg2Type, type, subtype, isUnmentioned)) {
	    // found a valid combination:
	    return true;
	  }
	}
	return false;
      } else {
	// neither arg is null, lookup in table
	String candidate =
          arg1Type.toUpperCase() + "," +
          arg2Type.toUpperCase() + "," +
	  type.toUpperCase() + "," + (subtype==null?"":subtype);
        if (DEBUG > 0)
          System.out.println("rdcArgsMatchTypes: Candidate: " + candidate);

        if (isUnmentioned) {
          if (DEBUG > 4)
            System.err.println("rdcArgsMatchTypes: constraints = " +
                               unmentionedRelationConstraints);
          return unmentionedRelationConstraints.contains(candidate);
        } else {
          return relationConstraints.contains(candidate);
        }
      }
    }
  }

  /***********************************************************************/
  /* Init methods */
  /***********************************************************************/

  private void initHighlightKeys () {
    highlights = new LinkedHashSet ();
    for (int i=0; i<HIGHLIGHT_KEYS.length; i++)
      highlights.add (HIGHLIGHT_KEYS[i]);
    
    highlights = Collections.unmodifiableSet (highlights);
  }

  private void initDefaultPrefs () {
    defaultPrefs = new HashMap ();
    for (int i=0; i<HIGHLIGHT_KEYS.length; i++)
      defaultPrefs.put ("task."+TASK_NAME+"."+HIGHLIGHT_KEYS[i], COLORS[i]);
    
    defaultPrefs = Collections.unmodifiableMap (defaultPrefs);
  }

  /***
  private void initTaskPrefs () {
    Preferences prefs = Jawb.getPreferences();
    String entityArgs = 
      prefs.getPreference("org.mitre.ace2004.entityargs");
    allowEntityArgs = new Boolean(entityArgs);
  }
  ***/

  private void initIO () {
    // Re-ordered, to default to most recent:
       importers = new Importer[] {new ImportAPF5_1_5(), new ImportAPF5_1_1a(), new ImportAPF5_1_1(), new ImportAPF5_1_0(), new ImportAPF4_0_1(),  new ImportAPF2_0_1()};
    // importers = new Importer[] {new ImportAPF2_0_1(), new ImportAPF4_0_1(),  new ImportAPF5_1_0(), new ImportAPF5_1_1(), new ImportAPF5_1_1a(), new ImportAPF5_1_5()};

    // Re-ordered, to default to most recent:
       exporters = new Exporter[] {new ExportAPF5_1_5(),  new ExportAPF5_1_1a()};
    // exporters = new Exporter[] {new ExportAPF5_1_1a(), new ExportAPF5_1_5()};
    // Removed 2005/06/07: new ExportAPF5_0_2()
  }

  private PreferenceItem preferences = null;
  private Autotagger autotagger = null;

  private void initConstraints() {
    try {
      // test for existence and make absolute
      Preferences prefs = Jawb.getPreferences();
      String uri = prefs.getPreference("org.mitre.ace2004.constraints.rng");
      if (uri == null)
        throw new RuntimeException("No constraints file (*.rng) found");
      
      File file = new File (localDir, uri);
      if (DEBUG > 0)
        System.err.println("ACE2004Task.initConstraints: rng file: "+file);
      
      RNGParser parser = new RNGParser();
      parser.read (file.toURI());
    
      constraintTree = parser.getConstraintTree();
      relationConstraints = parser.getRelationConstraints(false);
      unmentionedRelationConstraints = parser.getRelationConstraints(true);
      argumentConstraints = parser.getArgumentConstraints();

    } catch (Exception x) {
      RuntimeException e = new RuntimeException("Error parsing config file");
      e.initCause(x);
      throw e;
    }
  }
  
  /***********************************************************************/
  /* RDC-specific ATLAS access helper methods */
  /***********************************************************************/

  /**
   * Returns an array of all Mention Relations containing the passed
   * in mention in either the arg1 or arg2 slot, or an empty array if
   * there are no such relations
   */
  public SubordinateSetsAnnotation[] getMentionRelations(Annotation annot) {
    Set relations = new HashSet();
    
    if (annot != null &&
        annot.getAnnotationType().getName().equals(ENTITY_MENTION_TYPE_NAME)) {
      MultiPhraseAnnotation mention = (MultiPhraseAnnotation) annot;
      Iterator referIterator = mention.getReferentElements().iterator();
      while (referIterator.hasNext()) {
	ATLASElement referElement = (ATLASElement)referIterator.next();
	if (referElement instanceof Region) {
	  Iterator refer2Iterator = 
	    ((Region)referElement).getReferentElements().iterator();
	  while (refer2Iterator.hasNext()) {
	    ATLASElement possibleRelation = 
	      (ATLASElement)refer2Iterator.next();
	    if (possibleRelation instanceof HasSubordinates &&
		((Annotation)possibleRelation).getAnnotationType().getName().equals(RELATION_MENTION_TYPE_NAME)) {
	      relations.add((SubordinateSetsAnnotation)possibleRelation);
	    }
	  }
	}
      }
      // return possibly empty set of found relations, in an array
      SubordinateSetsAnnotation[] mentionRelations =
	new SubordinateSetsAnnotation[relations.size()];
      return (SubordinateSetsAnnotation[])relations.toArray(mentionRelations);
    } else {
      return new SubordinateSetsAnnotation[0];
    }
  }

  /** 
   * getEventRelations below seems to be inappropriately named
   * I am changing it to getEntityRelations but putting this in
   * for backward compatibility 
   */
  public static SubordinateSetsAnnotation[] getEventRelations(Annotation annot) {
    return (getEntityRelations(annot));
  }

  /**
   * Returns an array of all Relations containing the passed in entity in
   * either the arg1 or arg2 slot, or an empty array if there are no such
   * relations
   */
  public static SubordinateSetsAnnotation[] getEntityRelations(Annotation annot) {
    
    if (annot != null &&
        annot.getAnnotationType().getName().equals(ENTITY_TYPE_NAME)) {
      Set relations = new HashSet();
      
      Iterator referants = annot.getReferentElements().iterator();
      while (referants.hasNext()) {
	ATLASElement referant = (ATLASElement)referants.next();
	if (referant instanceof Region) {
	  Iterator regionReferants =
            ((Region)referant).getReferentElements().iterator();
	  while (regionReferants.hasNext()) {
	    ATLASElement regionReferant = (ATLASElement)regionReferants.next();
	    if (regionReferant instanceof HasSubordinates &&
		((Annotation)regionReferant).getAnnotationType().getName().equals(RELATION_TYPE_NAME)) {
	      relations.add(regionReferant);
	    }
	  }
	}
      }
      // return possibly empty set of found relations, in an array
      return (SubordinateSetsAnnotation[])
        relations.toArray(new SubordinateSetsAnnotation[0]);
    } else {
      return new SubordinateSetsAnnotation[0];
    }
  }

  /**
   * Returns the superordinate Annotation containing the given annotation, if
   * the superordinate type is the same as the annot (sans "-mention"). The
   * subordinate must refer to the annot via an IndefinateCardinality
   * AnnotationType. Failure to find a suitable superordinate returns null.  If
   * multiple superordinates of the specified type, are found, it is undefined
   * which will be returned.
   */
  public static HasSubordinates getMentionParent(AWBAnnotation annot) {
    // get the containting type
    String typeName = annot.getAnnotationType().getName();
    int superTypeNameEnd = typeName.lastIndexOf("-mention");
    if (DEBUG > 3)
	System.err.println("A2K4T.getMC: typeName, superTypeNameEnd = " + typeName + ", " + superTypeNameEnd);
    // use 'regionMatches' below so no need to use 'substring'
    if (superTypeNameEnd < 0) {
	if (DEBUG > 3)
	    System.err.println("A2K4T.getMC: non-mention: "+annot);
      return null;
    }
    if (annot != null) {
	if (DEBUG > 3)
	    System.err.println("A2K4T.getMentionParent); annot = " + annot);
      Iterator referants = annot.getReferentElements().iterator();
      while (referants.hasNext()) {
	ATLASElement referant = (ATLASElement)referants.next();
	if (DEBUG > 3)
	    System.err.println("A2K4T.getMentionParent); referant = " + referant);
	if (referant instanceof Region) {
	  Iterator regionReferants = ((Region)referant).getReferentElements().iterator();
	  while (regionReferants.hasNext()) {
	    ATLASElement regionReferant = (ATLASElement)regionReferants.next();
	    if (DEBUG > 3)
		System.err.println("A2K4T.getMentionParent); regionReferant = " + regionReferant);
            String rrName = ((Annotation)regionReferant).getAnnotationType().getName();
	    if (DEBUG > 3)
		System.err.println("A2K4T.getMentionParent); regionReferantString = " + rrName);
	    if (regionReferant instanceof HasSubordinates) 
		if (DEBUG > 3)
		    System.err.println("A2K4T.getMentionParent); regionReferant instanceof HasSubordinates = T");
		
	    if (regionReferant instanceof HasSubordinates &&
                (rrName.length() == superTypeNameEnd &&
		 rrName.regionMatches(0,typeName,0,superTypeNameEnd)) ||
		(rrName.equals("ace_relation-mention") &&
		 typeName.equals("ace_relation-mention-extent"))) {
		if (DEBUG > 3)
		    System.err.println("A2K4T.getMentionParent); this was the HasSubordinates target");
		return (HasSubordinates) regionReferant;
	    }
	  }
	}
      }
      // haven't found a superordinate that contains it
    }
    if (DEBUG > 0)
      System.err.println("A2K4T.getMC: no parent found for: "+annot);
    return null;
  }

  /**
   * Returns the relation mention annotation that is uniquely pointing to this
   * relation mention extent. Failure to find a suitable superordinate returns null. 
   * Notice that this uses a recursive call, which assumes that relationMentionExtents
   * have a small, not circular set of "referring" objects pointing to them.
   */

    /* 
       This turns out to have probably been a waste of time.  DSD.
  public static HasSubordinates getRelationMentionFromExtent (AWBAnnotation annot) {
    if (annot != null) {
	if (DEBUG > 0)
	    System.err.println("(A2K4T.getRelationMentionParent) annot = " + annot);
	Iterator referants = annot.getReferentElements().iterator();
	while (referants.hasNext()) {
	    ATLASElement referant = (ATLASElement)referants.next();
	    if (DEBUG > 0)
		System.err.println("(getRelationMentionFromExtent) referant = " + referant);
	    if (referant instanceof HasSubordinates &&
		((Annotation)referant).getAnnotationType().getName().equals(RELATION_MENTION_TYPE_NAME)) {
		return (HasSubordinates) referant;
	    } else if (referant instanceof HasSubordinates)
		return (HasSubordinates) referant;

	    {
	      getRelationMentionFromExtent((AWBAnnotation) referant);
	  }
      }
      // haven't found a relation mention that contains (points to) this relation mention extent.
    }
    if (DEBUG > 0)
      System.err.println("(A2K4T.getRelationMentionParent) no parent found for: "+annot);
    return null;
  }
    */


  /**
   * Same as above except with specifed superType instead of impled by
   * convention. Code not reused for optimization.
   */
  public static AWBAnnotation getParent(AWBAnnotation annot,
                                        String superTypeName) {
    if (annot != null) {
      Iterator referants = annot.getReferentElements().iterator();
      while (referants.hasNext()) {
	ATLASElement referant = (ATLASElement)referants.next();
	if (referant instanceof Region) {
	  Iterator rRefs = ((Region)referant).getReferentElements().iterator();
	  while (rRefs.hasNext()) {
	    ATLASElement rRef = (ATLASElement)rRefs.next();
	    if (rRef instanceof Annotation &&
                (superTypeName == null ||
                 ((Annotation)rRef).getATLASType().getName().equals(superTypeName))) {
	      return (AWBAnnotation) rRef;
	    }
	  }
	}
      }
      // haven't found a superordinate that contains it
    }
    if (DEBUG > 0)
      System.err.println("A2K4T.getParent: no parent found for: "+annot);
    return null;
  }

  /** reused */
  private static Vector parentsList = new Vector();
  /**
   * Once more except returning an array of all referants.
   */
  public static AWBAnnotation[] getParents(AWBAnnotation annot,
                                           String superTypeName) {
    parentsList.clear();
    if (annot != null) {
      Iterator referants = annot.getReferentElements().iterator();
      while (referants.hasNext()) {
	ATLASElement referant = (ATLASElement)referants.next();
	if (referant instanceof Region) {
	  Iterator rRefs = ((Region)referant).getReferentElements().iterator();
	  while (rRefs.hasNext()) {
	    ATLASElement rRef = (ATLASElement)rRefs.next();
	    if (rRef instanceof Annotation &&
                (superTypeName == null ||
                 ((Annotation)rRef).getATLASType().getName().equals(superTypeName))) {
	      parentsList.add(rRef);
	    }
	  }
	}
      }
    }
    return (AWBAnnotation[]) parentsList.toArray(new AWBAnnotation[0]);
  }

  /** 
   * Takes an annotation and returns true if it is an annotation of
   * type RELATION with no subordinates of type RELATION_MENTION, and
   * false otherwise.
   */
  public static boolean isUnmentionedRelation(AWBAnnotation annot) {

    if (!ACE2004Task.RELATION_TYPE_NAME.equals(annot.getAnnotationType().getName()))
      return false;

    AnnotationType subType = ACE2004Utils.RELATION_MENTION_TYPE;
    
    HasSubordinates relation = (HasSubordinates) annot;
    AWBAnnotation[] mentions = relation.getSubordinates(subType);
    return (mentions == null || mentions.length == 0);
  }

  /**
   * Takes a random relation mention that is a subordinate of the
   * given relation, and gets values for arg1 and arg2, finds their
   * parent entities, and returns those in an array (where arg1 is in
   * position 0, arg2 in position 1).  arg1 and arg2 should be the same
   * for all the relation mentions, so that is why it doesn't matter
   * which mention we use.
   *
   * Now that we have relation mentions that are allowed to look up to
   * their relation parent for the entity args if their own
   * entity-mention arg is null, we need to check for null and use the
   * relation's arg if found. RK 2/6/07
   */
  public static AWBAnnotation[] getArgEntitiesFromRelationMention(AWBAnnotation rel) {

    AWBAnnotation[] values = new AWBAnnotation[2];
    values[0] = null;
    values[1] = null;

    if (DEBUG > 0)
      System.err.println("A2KTask.getArgsFromRelMen " + rel);

    if (rel == null || !(rel instanceof SubordinateSetsAnnotation)) 
      return values; // filled with all null values

    // System.err.println("A2KTask.getArgsFromRelMen: rel is non-null SubordinateSetsAnnotation");
      
    AWBAnnotation relMention = ((SubordinateSetsAnnotation)rel).
      getSubordinates(ACE2004Utils.RELATION_MENTION_TYPE)[0];
    if (relMention == null)
      return values; // filled with all null values

    // System.err.println("A2KTask.getArgsFromRelMen: found mention: " +relMention);

    
    AWBAnnotation mention1 = 
      (AWBAnnotation) relMention.getAttributeValue("arg1");
    if (mention1 == null)
      values[0] = (AWBAnnotation)rel.getAttributeValue("arg1");
    else
      values[0] = getParent(mention1, ENTITY_TYPE_NAME);

    AWBAnnotation mention2 = 
      (AWBAnnotation) relMention.getAttributeValue("arg2");
    if (mention2 == null)
      values[1] = (AWBAnnotation)rel.getAttributeValue("arg2");
    else
      values[1] = getParent(mention2, ENTITY_TYPE_NAME);

    return values;
  }
      
      
    
}

