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

package org.mitre.jawb.tasks;

import org.mitre.jawb.atlas.AWBATLASImplementation;

import java.awt.Component;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.text.Highlighter;

import gov.nist.atlas.type.AnnotationType;
import gov.nist.atlas.type.CorpusType;
import gov.nist.atlas.type.ATLASType;
import gov.nist.maia.MAIAScheme;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.gui.JawbComponent;
import org.mitre.jawb.gui.JawbFrame;
import org.mitre.jawb.prefs.PreferenceItem;

/**
 * Definition of the capabilities and properties of an annotation Task.<p>
 *
 * This Class provides the interface for plugable modules to the Annotation
 * tool. It provides the structure of the annotations in this task, the data
 * requirements and restrictions, GUI components used to manipulate the
 * annotaions, Actions to create, and otherwise manipulate them, and any
 * preferences with which to manipulate all their behavior.<p>
 *
 * The Primary configuration file to be written is the MAIA scheme
 * definition.  Documentation can be found on the <a
 * href="http://www.nist.gov/speech/atlas">NIST ATLAS</a> website.<p>
 *
 * Actions retrieved are checked, and any text actions are added as context
 * menu items in the main text pane.<p>
 *
 *
 * OUTDATED: this documentation needs to be rewritten. It's no longer
 * specifically encouraged to make this a singleton, but the wording has to be
 * rethought.<p>
 *
 * Implementations must also be sure that the methods {@link
 * Object#equals(Object)} and {@link Object#hashCode()} as defined in {@link
 * Object}, maitain the contract defined there. This is to ensure proper
 * behavior, since Tasks objects are used internally as keys in hash
 * maps. Tasks implemented as singletons may safely ignore this, as the
 * contract is maintained by virtue of the singleton pattern.
 *
 * should be implemented as well.  This cannot be enforced through interfaces
 * though (as they cannot define static methods), and using an abstract class
 * to do the same would simply be a seris of hacks anyway. For more
 * information and common errors implementing singletons, see <a href="http://developer.java.sun.com/developer/technicalArticles/Programming/singletons">When
 * is a Singleton not a Singleton</a><p>
 */
public interface Task {
  
  /**
   * Get the name of this task as a human-readable string. This may be
   * an i18n string if the task has been i18nized
   */
  public String getTitle ();

  /**
   * Get the name of this Task as a language independant ID, used to
   * differentiate tasks internally at runtime. This string must conform to
   * java 'name' specification.  This requirement is verified when the task is
   * initially loaded, and loading fails if the Name fails this
   * requirement.<p>
   *
   * In short the name consists of identifiers separated by the '.' token.
   * Identifiers consist of an unlimited length of <i>Java letters</i> and
   * <i>Java digits</i> the first of which must be a Java letter. An
   * identifier cannot have the same spelling as a Java keyword, boolean
   * literal (<i>true</i> or <i>false</i>) or the null
   * literal(<i>null</i>).<p>
   *
   * See The Java Language Specification: <a href=
   * "http://java.sun.com/docs/books/jls/second_edition/html/names.doc.html">
   * Names</a>. and <a href=
   * "http://java.sun.com/docs/books/jls/second_edition/html/lexical.doc.html#40625">
   * Identifiers</a> for more detailed description.<p>
   *
   * Because there is strong potential for name overlap, Task implementors are
   * encouraged to follow the java naming conventions when naming
   * Tasks. Specifically, Tasks should begin with a 'packages' name, follwed
   * by an identifier for the class.  For example:<p> <ul> <li>org.mitre.rdc
   * <li>org.mitre.edt </ul>
   * 
   * These names do <i>not</i> have to correspond to the class name of any
   * java object, though that is certainly allowed.  Following this
   * convention, it falls to task implementors within the same group to
   * maintain uniqe names amongst their tasks.
   */
  public String getName ();

  /**
   * A version identifier for the task.
   */
  public String getVersion ();

  /**
   * A description string used to describe the Task. This should be kept to
   * 3-4 short sentances <i>at most</i>. Used to describe the task to
   * users. Note that the first sentance (or a fragment of it) may be used in
   * certain places where a short description is needed.
   */
  public String getDescription ();

  /**
   * Get the URL (as an absolute URI) of the tasks home page, where updates and
   * latest information can be found. May be null.
   */
  public URI getHomePage ();

  /**
   * A URI of files distributed locally with the task. May be null.
   */
  public URI getLocalDocs ();

  /**
   * Get the cononical URL (as an absolute URI) of a MAIA document which
   * defines the structure of annotation for the task. A local copy of the
   * MAIA document must also be distributed with the task, and its (absolute)
   * URI provided by {@link #getLocalMaiaURI}.
   */
  public URI getMaiaURI ();

  /**
   * Get the URL (as an absolute URI) for a local copy of this Tasks MAIA
   * document.
   * @see #getMaiaURI
   */
  public URI getLocalMaiaURI ();
  
  /**
   * Get the ATLAS MAIAScheme object that describes this task.
   */
  public MAIAScheme getMaiaScheme();


  /**
   * Get the AWBATLASImplementation that has been initialized with
   * the pointer to this Task object.
   */
  public AWBATLASImplementation getATLASImplementation();
 
  /**
   * Return an {@link TaskToolKit} used to edit documents with this task. From
   * the TaskToolKit, the GUI components will determined and instantiated. At
   * this time, a new kit should be returned on each invocation, though in the
   * future new kits may be created by cloning a prototype kit.<p>
   *
   * TODO: a 'default' editorkit would be just grand...
   */
  public TaskToolKit getToolKit ();


  /**
   * This allows the task to write it's own converters to read data in as
   * non-AIF documents.
   */
  public Importer[] getImporters ();

  /**
   * This allows the task to write it's own converters to save data out as
   * non-AIF documents.
   */
  public Exporter[] getExporters ();

  /**
   * getValidators should return an array of all the Validators
   * available for the task.
   */
  public Validator[] getValidators();

  /**
   * getAutoValidators should return a subset of what getValidators
   * returns, consisting of those Validators which must be run
   * automatically before saving or exporting any file.
   */
  public Validator[] getAutoValidators();  

  /** 
   * returns the Autotagger object for this task, or null if autotagging is
   * not to be enabled for this task.
   */
  public Autotagger getAutotagger();

  /**
   * Get an object to display a Preferences panel to configure and save task
   * specific properties. The componenent need not edit highlight preferences,
   * as this is handled by the Preferences dialog itself. This method will
   * likely only be called once, though regardless, it should return the same
   * object at each request.<p>
   *
   * Preferences configured by this widget should conform to the same
   * requirements described in {@link #getDefaultPreferences}.<p>
   * 
   * @return a PreferenceItem to set task specific preferences, or
   * <code>null</code> if there are no user configurable task preferences
   * other than highlights.
   * @see #getDefaultPreferences
   */
  public PreferenceItem getPreferenceItem ();

  /**
   * Get a map associating this components preference keys to their default
   * values. All keys must be Strings beginning with this tasks unique prefix
   * (namespace), and all values must be Strings.<p>
   *
   * Each task has it's own namespace for all its keys, user preferences,
   * highlight keys, and any hidden parameters not yet user configurable. This
   * way, preferences for various tasks will not interfere with each other.<p>
   *
   * The namespace is <code>"task."</code> followed by the tasks name (from
   * {@link #getName()}) and another separating dot, eg.
   * "<code>task.org.mitre.rdc.</code>" This prefix must begin each preference
   * for this task. Keys in the default map are checked when task is loaded,
   * and errors cause the Task load to fail, with a warning.
   *
   * <b>Note:</b> highlight keys (from {@link #getHighlightKeys} <i>do not</i>
   * specify the full namespace, but only the unique name within the
   * namspace. When specified in the default preferences, the keys must be
   * prefixed with the namespace. Example
   * 
   * <table>
   *   <tr><td>Highlight Key</td><td>Preference Key</td></tr>
   *   <tr><td><code>mention-head</code></td>
   *       <td><code>task.org.mitre.edt.mention-head</code></td></tr>
   *   <tr><td><code>mention-extent</code></td>
   *       <td><code>task.org.mitre.edt.mention-extent</code></td></tr>
   * </table>
   *
   * @see #getName
   * @see #getHighlightKeys
   * @see #getPreferenceItem
   */
  public Map getDefaultPreferences ();

  /**
   * Get a Set of String objects used as Keys used to specify annotation
   * highlight colors. These are related to, but <i>not</i> the same as
   * preference keys.
   *
   * Task implementors may set default color values using {@link
   * #getDefaultPreferences()} (be sure to prepend the tasks namespace to your
   * highlight preference key). Values should be hexadecimal color values
   * beginning with a '#' character, 2 characters for each RGB
   * components. Examples:
   *
   * <table>
   * <tr><td>Highlight Key</td><td>Preference Key</td><td>Value</td></tr>
   *   <tr><td><code>mention-head</code></td>
   *       <td><code>tags.org.mitre.rdc.mention-head</code></td>
   *       <td><code>#8bc6ff</code></td></tr>
   *   <tr><td><code>mention-extent</code></td>
   *       <td><code>tags.org.mitre.rdc.mention-extent</code></td>
   *       <td><code>#8ff7ff</code></td></tr>
   * </table>
   *
   * @see #getDefaultPreferences
   * @see #getName
   */
  public Set getHighlightKeys ();

  /**
   * Parses the annotation into a key used to highlight it, using the
   * constraint to specify which key if multiple are
   * available. Implementations need not handle <code>null</code> for annot,
   * but should not cause exception for unspecified constraint.
   */
  public String getHighlightKey (AWBAnnotation annot, Object constraint);

  
  /**
   * Returns an <code>ATLASType</code> object which can be used to
   * create a corpus for this task. While a MaiaScheme may have more than one
   * corpus, Jawb only works with one.
   */
  public CorpusType getCorpusType ();

  /**
   * Returns a set of <code>ATLASType</code> objects which can be used to
   * create annotations for this task.
   */
  public Set getAnnotationTypes ();
  
  /**
   * Returns a subset of the <code>ATLASType</code> objects which can
   * be used to create annotations for this task, consisting of only
   * those for which there should be a "Modify Extent" or one or more
   * "Modify <named> Extent" action(s).
   */
  public Set getExtentModifiableAnnotationTypes ();
  
  /**
   * Returns the <code>AnnotationType</code> object which can be used to
   * create and compare annotations for this task.
   */
  public AnnotationType getAnnotationType (String typeName);

  /**
   * Returns the String names of attributes (including single subordinates)
   * available for a particular type of annotation. Does not need an instance.
   */
  public Set getAttributes (AnnotationType type);

  /**
   * Returns a Set of the String names of content attributes available
   * for a particular type of annotation. Does not need an instance.
   */
  public Set getContentAttributes (AnnotationType annotType);

  /**
   * Returns a Set of the String names of any role-identified (single)
   * subordinate attributes available for a particular type of
   * annotation. Does not need an instance.
   */
    public Set getSubordinateAttributes (AnnotationType annotType);

  /**
   * Return the reflected Class type of an attribute value.  This is ambiguous
   * for sub-annotations (single cardinality annotation regions which have
   * been given an attribute name (ie RDC-relation.arg1)), as the class name
   * is the same though the AnnotationType allowed may be different. For
   * better specification, use {@link #getSubordinateType}.
   */
  public Class getAttributeType (AnnotationType type, String attribute);

  /**
   * Returns a Set of possible values for a type of annotations attribute, if
   * known. Same as {@link #getPossibleValues(AWBAnnotation,String)} but does
   * not require an instance of the annotation.
   * @return null, if the possible values are unspecified
   */
  public Set getPossibleValues (AnnotationType type, String attribute);

  /**
   * Returns a Set of possible values for the attribute, if known.
   * <code>null</code> implies there are no limits on values, and an empty Set
   * is reserved for errors. Clients of the Task interface should check the
   * attributes type to determine what kind of object the value may be set
   * with.
   */
  public Set getPossibleValues (AWBAnnotation annot, String attribute);

  /** 
   * Returns the default value for the given attribute of the given
   * annotation type, if there is one, or null if there is not.
   */
  public String getDefaultValue (AnnotationType annotType, String attr);

  /** 
   * Returns the default value for the given attribute of the given
   * annotation (taking into account other values already set), if
   * there is one, or null if there is not.
   */
  public String getDefaultValue (AWBAnnotation annot, String attr);

  /**
   * Determines whether or not the given subordinate Annotation is a
   * valid filler of the given role in the given superordinate
   * Annotation.  If role is null, determines whether or not the given
   * subordinate Annotation is a valid member of a subordinate set of
   * the given superordinate Annotation.  If the role provided is not
   * a valid role for the superordinate, or does not take an
   * Annotation, this method will return false.
   */
  public boolean isValidSubordinate(AWBAnnotation superordinate, 
				    AWBAnnotation subordinate,
				    String role);


  /**
   * Returns the AnnotationType of the role-identified subordinate
   * with the given role for the given type of the superordinate
   * Annotation.
   */
  public AnnotationType getSubordinateType(AnnotationType type, String role);

  /**
   * Returns the runtime Class object for the object used to create the
   * specified ATLASType.  Generally <i>type</i> will be an AnnotationType.
   */
  public Class getAnnotationClass(ATLASType type);

  /********************************************************************
   Additional methods needed by server-based tasks, and perhaps others
  ********************************************************************/

  /**
   * Returns true if this task saves out changes immediately as the
   * happen, and false otherwise.
   */
  public boolean savesContinuously(); 

  /**
   * Does anything task-specific that needs to be done before closing
   * a JawbDocument.  The JawbFrame passed in is the last unclosed
   * JawbFrame displaying that JawbDocument.  The JawbDocument can be
   * retrieved by calling jf.getJawbDocument().  Returns true if the
   * close should continue, false if the pre-close activites result in
   * the close being cancelled.
   */
  public boolean documentClosing(JawbFrame jf);  

  /**
   * Does anything task-specific that needs to be done before closing
   * a JawbDocument.  The JawbFrame passed in is the last unclosed
   * JawbFrame displaying that JawbDocument.  The JawbDocument can be
   * retrieved by calling jf.getJawbDocument(). Returns true if
   * successful or if there is nothing to do, false otherwise.
   */
  public boolean documentClosed(JawbFrame jf);  
}
