
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
package org.mitre.jawb.gui;

import gov.nist.atlas.type.AnnotationType;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.TextAction;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.NamedExtentRegions;
import org.mitre.jawb.atlas.TextExtentRegion;
import org.mitre.jawb.prefs.ColorSpec;
import org.mitre.jawb.swing.SetModel;
import org.mitre.jawb.swing.event.SetDataEvent;
import org.mitre.jawb.swing.event.SetDataListener;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;

// The idea of the action model is that it will tell you what
// actions are available at any given time. The
// available actions differ depending on the task
// (what actions are available), and on which selections
// are annotated - this latter class falls into two categories, 
// annotation modifiers and annotation "actions". Examples 
// of the former are delete and modify extent; examples of
// the latter are currently task-specific. Finally, 
// there should be a notion of "actions used", so
// that any subscribers can present shortcuts if 
// desired. 

// The right thing would be for the action to report 
// what actions were used, but unfortunately, the actions
// don't really lend themselves to that - they're scattered
// throughout the tasks. So there needs to be a "markUsed"
// method on the action model. Better yet, let's proxy all the 
// actions that come out of the model - they're kind of special
// anyway, because they're GUI-visible.

// The notion of an active type should also be included,
// at least based on what's currently in the MainTextPalette.
// This is used to ensure that the model doesn't have to change
// much. We should steal all this stuff from the current MainTextPalette.

// The action model is owned by the MainTextPane. It should really be
// owned by the main Jawb frame, but it's pretty much impossible
// to make that happen: too many people instantiate the MainTextPane,
// and the toolkit is an interface which can't really be changed, and
// that's the only available hook, and it's not even the right thing -
// there can be multiple frames, and thus multiple action models, per
// toolkit.

public class ActionModel implements CaretListener, SetDataListener {
  
  private TaskToolKit kit;
  
  private static final int DEBUG = 0;
  
  /** current document */
  private JawbDocument document = null;
  
  /** kept up to date with a listener added to MainTextPane */
  private boolean textSwiped = false;
  /** can't get 'last selected annot' from List events, so using this to know
   * which modifier actions to remove when we add new ones */
  private AnnotationType activeAnnotationType;
  /** cache for setting enabled state of modifier buttons. Only set when
   * JD!=null, lastType!=null, and selectedAnnotModel.size()==1 */
  private AWBAnnotation activeSingleAnnotation;
  
  /* For each task, we need a map from actions to their action proxies. */
  private Map actionToProxyMap = new HashMap ();

  /* We have a single extent modifier map, and one for each
   * extent name. We have a single extent modifier for each
   * type of extent, not for each annotation type.
   */
  
  private ActionProxy modifyExtentProxy;
  
  private Map extentToProxyMap = new HashMap();

  /* We have a table of sets of annotation-specific types,
   * as well as a set of annotation-specific types which are general.
   * The table of proxy sets says whether the relevant type supports
   * extent proxy annotations, and which ones.
   */
  
  private class AnnotationTypeExtentInfo {
    // null can be one of these. The idea is that
    // when we activate this type, we activate the 
    // supported extents.
    private HashSet extentNamesSupported;
    private AnnotationType aType;
    
    public Set getExtentsSupported() {
      if ((extentNamesSupported == null) || (extentNamesSupported.size() == 0)) {
        return Collections.EMPTY_SET;
      } else {
        return Collections.unmodifiableSet (extentNamesSupported);
      }
    }
    
    public AnnotationTypeExtentInfo(AnnotationType type, Task task) {
      aType = type;
      Class tc = task.getAnnotationClass (type);
      
      if (DEBUG > 0)
        System.err.println("ActionModel.AnnotTypeExtInfo: constructor for " 
            + type + " = " + 
            (tc==null?"null":tc.toString()));
    
      if (TextExtentRegion.class.isAssignableFrom (tc)) {
        // simple modifer for TextExtentRegion
        if (extentNamesSupported == null) {
          extentNamesSupported = new HashSet();
        }
        extentNamesSupported.add(null);
        if (modifyExtentProxy == null) {
          modifyExtentProxy = new ActionProxy(new ModifyExtentAction ("Modify Extent", null));
          modificationActions.add(modifyExtentProxy);
        }
      } else if (NamedExtentRegions.class.isAssignableFrom (tc)) {
        // one modifier for each extent of a NamedExtentRegions
        Iterator attribIter = task.getAttributes (type).iterator ();
        while (attribIter.hasNext ()) {
          String attrib = (String) attribIter.next();
          if (attrib.endsWith ("TextExtent")) {
            if (extentNamesSupported == null) {
              extentNamesSupported = new HashSet();
            }
            String x = attrib.substring (0,attrib.indexOf ('.')).intern();
            extentNamesSupported.add(x);
            if (extentToProxyMap.get(x) == null) {
              ActionProxy ap = new ActionProxy(new ModifyExtentAction ("Modify "+
                  x.toUpperCase()+
                  " Extent",x));
              extentToProxyMap.put(x, ap);
              modificationActions.add(ap);
            }
          }
        }
      }
    }
  }
  
  private Map typeToAnnotInfo = new LinkedHashMap();
  
  /* These three are all permanently ordered, so that they always come back in
   * the same order when requested. This will support consistency across the 
   * GUI views. Not good for searching, obviously, but we want the order
   * to be fixed (and controllable by the ActionModel).
   */
  
  /* We have the full set of annotation actions. */

  private Vector annotationActions = new Vector();
  
  /* We also have the set of text actions. */
  
  private Vector textActions = new Vector();
  
  /* And finally, the set of extent modifier actions. */
  
  private Vector modificationActions = new Vector();
  
  /* And now, the versions which contain the groups. I 
   * originally had these as LinkedHashSets, but the problem
   * is that everybody else returns a list, and I don't know
   * whether I can coerce this into a list. */

  private ActionCollection groupedAnnotationActions;

  private ActionCollection groupedTextActions;
  
  private ActionCollection groupedModificationActions;
  
  private Vector listeners = new Vector (5);
  /**
   * Reusable array for safe listener iteration so that if one decides to
   * remove itself after recieving an event from us, we won't get a concurrent
   * modification exception.
   */
  private ActionExecutionListener[] array = new ActionExecutionListener[0];
  
  /* Configure whether overlap is required for extent modifications or not. */
  
  private boolean overlapRequired = false;
  
  public ActionModel(TaskToolKit kit) {
    if (DEBUG > 0)
      System.err.println("ActModel constructor - tk:" + kit);
    this.kit = kit;
    
    // We loop through the actions. Depending on what type of
    // action it is, we set it up differently. Depending on what kind
    // of action sequence we get, we also have to do something 
    // clever. If the action sequence is an ActionCollection, then
    // we're going to have information about groups, and we're going
    // to want to use the collection's group factory to make groups
    // out of the values we create.
    
    Set actions = kit.getActions();

    Iterator actionIter = actions.iterator();
    
    if (DEBUG > 1)
      System.err.println("Iterating through " + actions.size() + " actions:");
    while (actionIter.hasNext ()) {
      Action act = (Action)actionIter.next();
      if (DEBUG > 1)
        System.err.println("\tnext action: " + act);
      if (act instanceof TextAction) {
        instantiateTextActionProxy((TextAction) act);
      } else if (act instanceof AnnotationAction) {
        instantiateAnnotationActionProxy((AnnotationAction) act);
      }
      // So far, that's all we have.
    }
    initModifierMap(kit);
    
    // Finally, if actions is an ActionCollection, we populate
    // our own collection set. I create a dummy proxy collection because
    // it makes the rest of the code simpler.
    
    if (actions instanceof ActionCollection) {
      if (DEBUG > 0)
        System.err.println("ActMod<init>: handle a collection");
      ActionCollection c = (ActionCollection) actions;
      proxyCollection = new ActionCollection(c.groupFactory);
      initActionGroups((ActionCollection) actions);
    } else {
      proxyCollection = new ActionCollection();
    }

    // Now, let's populate the grouped sequences. Only the
    // root groups, please.
    groupedTextActions = populateGrouped(textActions);
    groupedAnnotationActions = populateGrouped(annotationActions);
    groupedModificationActions = populateGrouped(modificationActions);
  }
  
  public void setOverlapRequired(boolean required) {
    overlapRequired = required;
  }
  
  private void instantiateTextActionProxy(TextAction act) {
    textActions.add(new ActionProxy(act));
  }
  
  private void instantiateAnnotationActionProxy(AnnotationAction act) {
    
    // There was a bug in the consuming code. Previous implementation assumed that if there
    // was no value for the annotation type key, it wasn't an annotation
    // action. However, annotation actions can be general, in which case
    // the annotation type will be null. We have to be careful later, too,
    // because a null annotation type might have exclusions. 
    
    annotationActions.add(new ActionProxy(act));
    
  }
  
  /**
   * Retrieve a List of extent modifiers for each anntations type in the
   * task. The list is cached by Task object and reused on subsequent
   * invocations. When a single annotation is selected, and text is swiped,
   * shold be active. I'm hoping ATLAS is good enough that name conflicts of
   * types between different tasks (maia files) won't have 'equal' types.<p>
   */
  private void initModifierMap (TaskToolKit kit) {

    if (DEBUG > 0)
      System.err.println("ActionModel.initModMap for " + kit);
    Map type2Mod = new LinkedHashMap ();
    Task task = kit.getTask();
    
    // for all annotation types, initialize the hash table with a null
    // entry, which will get overwritten below for types with
    // modifiable text extent(s)
    Iterator allTypesIter = task.getAnnotationTypes().iterator();
    while (allTypesIter.hasNext()) {
      AnnotationType type = (AnnotationType)allTypesIter.next();
      type2Mod.put(type, null);
    }

    // cache a list of modifiers for each annotation type with
    // modifiable text extent
    // RK 8/4/10 switch to calling on the ToolKit to allow context-sensitivity
    Iterator typeIter = kit.getExtentModifiableAnnotationTypes().iterator ();
    while (typeIter.hasNext ()) {
      AnnotationType type = (AnnotationType) typeIter.next ();
      //Class tc = task.getAnnotationClass (type);
      type2Mod.put(type, new AnnotationTypeExtentInfo(type, task));
    }

    typeToAnnotInfo = type2Mod;
  }
  
  private ActionCollection proxyCollection;
  
  private void initActionGroups(ActionCollection actions) {
    // In order to reconstruct the groups, we need the action
    // group factory, etc. When I copy, I set the parent/child
    // group links on the copies, NOT on the original - the
    // basic actions don't support them. They're for convenience
    // in the action execution listener callbacks.
    // The groups themselves aren't linked - they're handled
    // by looking at what group the parent of a group
    // is in. 
    
    // In order to do this, I think I need to go through the
    // group info in the action collection, create a new action collection
    // with the relevant proxies, and then get the actions from there.
    // That should take care of all the parent/child juggling.
    
    // Each of the specialized sets will get an action collection
    // with the appropriate mapping info.

    Iterator actionIterator = actions.iterator();
    if (DEBUG > 0)
      System.err.println("initActionGroups: actions.iterator.hasNext?: " +
                         actionIterator.hasNext());

    
    while (actionIterator.hasNext()) {
      Action a = (Action) actionIterator.next();
      ActionProxy p = (ActionProxy) actionToProxyMap.get(a);
      if (p == null) {
        // Not sure how this would happen, but...
        p = new ActionProxy(a);
      }
      proxyCollection.add(p);
    }
    
    Iterator actionGroupIterator = actions.groups.iterator();
    
    if (DEBUG > 0)
      System.err.println("\tactions.groups.iterator.hasNext?: " +
                         actionGroupIterator.hasNext());

    while (actionGroupIterator.hasNext()) {
      ActionGroup a = (ActionGroup) actionGroupIterator.next();
      if (DEBUG > 1)
        System.err.println("initActionGroups: group: " + a);
      //    Collect the children.
      Vector subProxies = new Vector();
      Action[] children = a.getSubActions();
      for (int i = 0; i < children.length; i++) {
        ActionProxy childProxy = (ActionProxy) actionToProxyMap.get(children[i]);
        // RK 03/26/2010 I don't know how it ever worked without this
        // because the proxies all seem to be null when we get here
        if (childProxy == null) {
          childProxy = new ActionProxy(children[i]);
          actionToProxyMap.put(children[i], childProxy);
        }
          
        if (DEBUG > 1)
          System.err.println("\tsubAction: " + children[i] +
                             "proxy: " + childProxy);
        subProxies.add(childProxy);
      }
      // The group may have a dummy parent. What do we do in that
      // case? Do we proxy it? I'm not sure why we would, because it
      // will never fire, and we don't want to know if it does.
      if (a.isDummyParent()) {
        // Get the name from the dummy action, not from the group - the latter
        // will be transformed already.
        proxyCollection.addGroupNoCheck((String) a.getAction().getValue(Action.NAME), subProxies);
      } else {
        Action parentAction = a.getAction();
        if (DEBUG > 1)
          System.err.println("Non-dummy parent action: " + parentAction);
        ActionProxy parentProxy = (ActionProxy) actionToProxyMap.get(parentAction);
        // RK 03/26/2010 I don't know how it ever worked without this
        // because the proxies all seem to be null when we get here
        if (parentProxy == null) {
          parentProxy = new ActionProxy(parentAction);
          actionToProxyMap.put(parentAction, parentProxy);
        }
          
        if (DEBUG > 1)
          System.err.println("\tproxy: " + parentProxy);
        proxyCollection.addGroupNoCheck(parentProxy, subProxies);
      }
    }
  }
  
  private ActionCollection populateGrouped (Vector ungroupedActions) {
    Iterator proxyIterator = ungroupedActions.iterator();
    ActionCollection groupedActions = null;
    groupedActions = new ActionCollection(proxyCollection.actionsToGroups, 
        proxyCollection.actionsToChildGroups);
    while (proxyIterator.hasNext()) {
      Action p = (Action) proxyIterator.next();
      // We only want the toplevel.
      if (groupedActions.isStandaloneAction(p)) {
        groupedActions.add(p);
      } else {
        Action rootG = groupedActions.getRootAction(p);
        if (!groupedActions.contains(rootG)) {
          groupedActions.add(rootG);
        }
      }
    }
    return groupedActions;
  }
  
  public void setJawbDocument(JawbDocument doc) {
    // The selected annotation listener I want to use is the document's
    // annotation listener, not the main text pane's, because the main text
    // pane might not REALLY have annotations selected, just highlighted,
    // when the exchanges are involved, for instance. So the idea is that
    // when we load a new document, we should update the listeners
    // appropriately. 
    
    if (DEBUG > 0)
      System.err.println("ActMod.setJD old: " + document + " new: " + doc);

    if (document != null) { // had a doc before call
      document.getSelectedAnnotationModel ()
        .removeSetDataListener (this);
    }
    activeSingleAnnotation = null;
    document = doc;
    if (document != null) {
      document.getSelectedAnnotationModel().addSetDataListener(this);
    }
    updateAnnotationModifiers ();
  }
  
  /***********************************************************************/
  /*                     Retrieving the state                            */
  /***********************************************************************/
  
  // These methods are for retrieving the state for setting up GUI 
  // views of the available annotations. 
  
  // getTextActions returns the actions available when text is
  // swiped. THESE ARE ALL THE ACTIONS.
  
  public List getTextActions() {
    return Collections.unmodifiableList (textActions);
  }

  // getAnnotationActions returns the actions potentially available when annotations
  // are selected. Those which are currently available will be enabled.
  
  public List getAnnotationActions() {
    return Collections.unmodifiableList(annotationActions);
  }
  
  // getExtentModificationActions returns the actions which affect extents.
  // Those which are currently available will be enabled.
  
  public List getExtentModificationActions() {
    return Collections.unmodifiableList(modificationActions);
  }
  
  // And now, we provide the same list with a set of toplevel groups
  // and action proxies interspersed. No element in any group is also
  // in the list alone. 
  
  public ActionCollection getGroupedTextActions() {
    return groupedTextActions;
  }

  // getAnnotationActions returns the actions potentially available when annotations
  // are selected. Those which are currently available will be enabled.
  
  public ActionCollection getGroupedAnnotationActions() {
    return groupedAnnotationActions;
  }
  
  // getExtentModificationActions returns the actions which affect extents.
  // Those which are currently available will be enabled.
  
  public ActionCollection getGroupedExtentModificationActions() {
    return groupedModificationActions;
  }
  
  
  // Stole the event propagation code from the AnnotationMouseModel.
  
  /***********************************************************************/
  /*                         Event Propogation                           */
  /***********************************************************************/

  /** Add an observer to this model. */
  public void addActionExecutionListener (ActionExecutionListener l) {
    if (! (l == null || listeners.contains(l))) {
      listeners.add(l);
    }
  }

  /**
   * Remove an observer from this model
   * @see #addActionExecutionListener
   */
  public void removeActionExecutionListener (ActionExecutionListener l) {
    listeners.remove(l);
    // null out the array since it will need to be smaller next time
    Arrays.fill(array,null);
  }

  public void fireActionFiredEvent (ActionProxy a) {

    array = (ActionExecutionListener[]) listeners.toArray(array);

    for (int i=0; i<array.length && array[i] != null; i++) {
      array[i].actionFired(a, proxyCollection);
    }
  }

  /***********************************************************************/
  /* Listener Implementations */
  /***********************************************************************/

  // Implementation of CaretListener interface
  
  // For reasons I don't quite understand, the caretUpdate is called twice
  // when I select an annotation. In the first case, it has a span; in 
  // the second case, it has a 0 span. So the text actions get enabled and
  // then disabled again. But crucially, the modifier events get enabled,
  // but not disabled.
  
  public void caretUpdate (CaretEvent e) {
    int dot = e.getDot();
    int mark = e.getMark ();
    boolean swiped = (dot != mark);
    
    if (swiped != textSwiped) {
      if (DEBUG > 1)
        System.err.println ("ActionModel: caretU ("+e.getDot()+","+e.getMark()+
                            ") is="+swiped+" was="+textSwiped);
      textSwiped = swiped;
      // deal with the enabled for the swiped actions.
      Iterator actionIter = textActions.iterator();
      while (actionIter.hasNext ()) {
        ActionProxy act = (ActionProxy) actionIter.next();
        act.setEnabled(swiped);
      }
    }
    
    // modification actions only available when overlapping single. Otherwise,
    // they weren't available in the first place. Overlap might not actually
    // be required.
    boolean overlap = false;
    if (activeAnnotationType != null && activeSingleAnnotation != null) {
      if (!overlapRequired) {
        overlap = swiped;
      } else {
        int start=0, end=0;
        if (activeSingleAnnotation instanceof TextExtentRegion) {
          start = ((TextExtentRegion)activeSingleAnnotation).getTextExtentStart ();
          end   = ((TextExtentRegion)activeSingleAnnotation).getTextExtentEnd ();
        } else if (activeSingleAnnotation instanceof NamedExtentRegions) {
          // TODO: stop hard coding for RDC!
          start = ((NamedExtentRegions)activeSingleAnnotation).getTextExtentStart ("full");
          end   = ((NamedExtentRegions)activeSingleAnnotation).getTextExtentEnd ("full");
        }
        if (dot>mark) {
          int tmp = dot;
          dot = mark;
          mark = tmp;
        }
        if (DEBUG > 1)
          System.err.println ("ActMod: caretU caret=("+e.getDot()+","+e.getMark()+
                              ") swiped="+swiped+" annot=("+start+","+end+") "+
                              " overlap="+overlap);
        overlap = swiped && ((dot<start && mark>end) ||
            (dot>=start && dot<end) ||
            (mark>start && mark<=end));
      }

      // Find the extent modification annotations for the active annotation type.
      setExtentModActionStatus(activeAnnotationType, overlap);
    }
  }

  private void setExtentModActionStatus(AnnotationType atype, boolean status) {
    AnnotationTypeExtentInfo modInfo = (AnnotationTypeExtentInfo) typeToAnnotInfo.get (atype);

    if (modInfo == null) 
      return;

    Set extentNamesSupported = modInfo.getExtentsSupported();
    
    if (extentNamesSupported != null) {
      Iterator iter = extentNamesSupported.iterator ();
      while (iter.hasNext ()) {
        String item = (String) iter.next();
        
        if (item == null) {
          // This is the simple extent.
          modifyExtentProxy.setEnabled(status);
        } else {
          ActionProxy ap = (ActionProxy) extentToProxyMap.get(item);
          ap.setEnabled(status);
        }
      }
    }
  }
  
  /* Implementation of SetDataListener. */
  // these fire whenever the set of selected annotations changes
  
  public void elementsAdded (SetDataEvent e) {
    updateAnnotationModifiers ();
  }
  public void elementsRemoved (SetDataEvent e) {
    updateAnnotationModifiers ();
  }

/**
 * TODO: this is way too compute intensive to happen on every change to the
 * model. Optimization would be to make use of the 'intervaladded/removed'
 * capablities for scaling.
 */
  public void updateAnnotationModifiers () {
    
    if (DEBUG > 0)
      System.err.println("ActMod.updateAnnotationModifiers for " + document);

    SetModel selectedAnnotModel = null;
    
    if (document != null)
      selectedAnnotModel = document.getSelectedAnnotationModel();
    
    // This might be called when there is no document.
    
    int size = selectedAnnotModel == null ? 0 : selectedAnnotModel.size ();
    AnnotationType annotType = null;
    
    // make sure all selected annotations are of same type... this would be
    // more efficient if done using interval added/removed
    if (size > 0) {
      Iterator iter = selectedAnnotModel.iterator ();
      AWBAnnotation annot = (AWBAnnotation) iter.next();
      annotType = annot.getAnnotationType();
      while (iter.hasNext()) {
        AWBAnnotation a = (AWBAnnotation) iter.next();
        if (!annotType.equals(a.getAnnotationType ())) {
          annotType = null;
          break;
        }
      }
    }
    
    // enable/disable AnnotationActions available for selected annots
    // based on count and type selected. Make sure that we disable the
    // annotations which aren't relevant to the type. If there are no
    // annotations selected, disable all the annotation actions.
    // Otherwise, disable all the annotations which don't have the
    // given type, and check the eligibility of all the others.

    Iterator actionIter = annotationActions.iterator();
    while (actionIter.hasNext()) {
      ActionProxy ap = (ActionProxy) actionIter.next();
      AnnotationAction a = (AnnotationAction) ap.getAction();
      boolean enable = false;
      if (size > 0) {
        // Now that we have exclusions for the general annotations,
        // we need to pass in the set of selected annotations, in
        // case not all the exclusions are satisfied.
        if (a.isActionEligible(selectedAnnotModel, annotType)) {
          if (a instanceof ContextSensitiveAnnotationAction) {
            ContextSensitiveAnnotationAction thisAct =
              (ContextSensitiveAnnotationAction) a;
            thisAct.setJawbDocument(document); // do this before checking
                                               // so we can use jd in 
                                               // isValidForContext if needed
            if (thisAct.isValidForContext(selectedAnnotModel)) {
              thisAct.setPropertiesForContext(selectedAnnotModel);
              enable = true;
            }
          } else {
            enable = true;
          }
        }
      }
      // Now, we've determined the enabled state.
      ap.setEnabled(enable);
    }
    
    //**********************************************************************
    // now add remove the modifiers available if it's a single selected annot
    
    Map type2Mod = null;
    AWBAnnotation annot = null;
    AnnotationType type = null;
    Class tc = null;
    
    if (document != null) {
      annot = document.getSingleSelectedAnnotation ();
      type = (annot==null ? null : annot.getAnnotationType());
      tc = (type==null ? null :
        document.getTask().getAnnotationClass (type));
      
      if (DEBUG > 1)
        System.err.println ("ActMod.updateAnnotModifiers:"+
            "\n\tannot ="+(annot==null?null:annot.getId())+
            "\n\ttype ="+(type==null?null:type.getName())+
            "\n\tlast ="+(activeAnnotationType==null?null:activeAnnotationType.getName())+
            "\n\tclass="+(tc==null?null:tc.getName())+
            "\n\tsize ="+size);
    }
    
    // TODO: a current bug in jATLAS prevents us from sending null to the
    // equals test for AnnotationType, so we have to test that first too
    
    // remove old modifiers if old type is differnt from new (and only 1
    // new selected)
    if (activeAnnotationType != null &&
        ( (type == null) || ! type.equals (activeAnnotationType) )) {
      if (DEBUG > 1)
        System.err.println ("ActMod.updateAnnotSel: removing mods for "+
            activeAnnotationType.getName ());
      
      // Disable them all.
      setExtentModActionStatus(activeAnnotationType, false);
    }
    
    // if no selection, return 
    if (type == null) {
      activeAnnotationType = null;
      activeSingleAnnotation = null;
      if (DEBUG > 1)
        System.err.println ("ActMod.updateAnnotSel: null OR not a text type annotation; returning null");
      return;
    }
    
    // if the type is non null and not same as last, don't enter new modifiers here, unless
    // there's text swiped.
    if (type != null &&
        ( (activeAnnotationType == null) || ! type.equals (activeAnnotationType) )) {
      if (DEBUG > 1)
        System.err.println ("ActMod.updateAnnotSel: adding mods for "+
            type.getName());
      if (textSwiped) {
        setExtentModActionStatus(type, true);
      }
    }
    activeAnnotationType = type;
    activeSingleAnnotation = annot;
    
    // TODO: enable disable AnnotationActions based on type. this mehod should
    // be broken up into two parts for that though.
  }
  
  // ***********************************************************
  // 
  // From here down, we're describing the behavior of the objects
  // that the action model presents to the programmer. They are:
  //
  // (1) Action groups, which collect actions together for the purposes
  // of menu display, etc. These elements have no behavior, and sometimes
  // their own names. They relay all properties of their (optional)
  // parent action.
  // (2) Action collections, which are special action sequences -
  // specializations of the normal sequence type - which support 
  // construction of action groups.
  // (3) Action delegates, which are exactly what they sound like.
  // (4) Action proxies, which are a special kind of action delegate
  // which supports execution listeners and color changes.
  // (5) Action group factories (see below).
  //
  // All actions that the programmer sees when s/he retrieves the
  // text, annotation or modification actions are action proxies, or,
  // if the programmer has asked for the groups, possibly action
  // groups of action proxies. Because the groups need to be 
  // reconstructed from the non-proxied actions the programmer 
  // sets up, we have action group factories which the programmer
  // can specialize if a special kind of action group is required.
  //
  // The action groups don't care whether their elements are 
  // proxied or delegated or not, and the delegates don't care 
  // whether their delegates are delegates themselves or no. The
  // appropriate properties (color, label, enablement) should be
  // relayed to all the appropriate subscribers in any case.
  //
  // ***********************************************************
  
  // Action proxies. The action model is the only thing which sees the
  // actual actions; everything else happens through the action proxies. The
  // reason for this is that we need the proxies in order to know whether 
  // an action was fired. It looks like, except for the instanceof calls, everything
  // can be (and probably should be) in terms of the ActionProxy classes over
  // in MainTextPalette, and the instanceof calls should be here anyway. Ditto for
  // the instanceof calls in AnnotationPopupListener. The only crucial difference is the 
  // cascades, which is external. But I can add a method to the proxy to test.
  
  // NEW: the property stuff needs to be entirely a pass-through. So 
  // we'll add this listener to the component which is to be changed, and
  // accumulate the information here. Enabling gets passed through.
  // Names should also get passed through. Color is the only
  // thing I need to add here.
  
  // Note that I've added a public static method for use in places like
  // the menu stuff.
  
  public static void addColor(Action action, JComponent c) {
    ColorSpec color = (ColorSpec)action.getValue(JawbAction.HIGHLIGHT_COLOR);
    
    if (DEBUG > 0)
      System.err.println ("AM.aC: " + 
          action.getValue (Action.NAME) +
          " color = " + 
          (color == null?"null":color.toString()));
    if (color != null) {
      c.setBackground(color.getBackground());
      c.setForeground(color.getForeground());
    }
  }

  public static class ColorChangeListener implements PropertyChangeListener {
    /**
     * Listen for changes for color keys of actions added here. Ditto the
     * names. 
     */
    private static final int DEBUG = 0;
    private JComponent component;
    
    public ColorChangeListener(JComponent c) {
      component = c;
    }

    public void propertyChange (PropertyChangeEvent evt) {
      String name = evt.getPropertyName ();
      if (JawbAction.HIGHLIGHT_COLOR.equals (name)) {
        Object o = evt.getSource ();
        ColorSpec cs = (ColorSpec)evt.getNewValue();
        if (cs != null) {
          component.setBackground (cs.getBackground());
          component.setForeground (cs.getForeground());
        }
      }
    }
  }
  
  // I need the notion of an action delegate in more than one
  // place. I might as well define it here, and also specialize
  // the action proxy.
  // These HAVE to implement Action, otherwise I won't be able to add them to
  // menus. 
  // Note that the right thing should happen if the action happens
  // to be an action delegate itself; in particular, there will be
  // no recursion through subactions here, but only in the delegated call.
  
  public static class ActionDelegate implements Action {
    
    protected Action action;
    protected List subDelegates;
    
    // The action can be null (see action group below).
    
    public ActionDelegate(Action a) {
      action = a;
    }
    
    public Action getAction() {
      return action;
    }

    // The following methods implement the delegate.
    
    public Object getValue(String arg0) {
      return action.getValue(arg0);
    }

    public void putValue(String arg0, Object arg1) {
        action.putValue(arg0, arg1);
    }

    public void setEnabled(boolean b) {
        action.setEnabled(b);
    }

    public boolean isEnabled() {
        return action.isEnabled();
    }

    public void addPropertyChangeListener(PropertyChangeListener arg0) {
        action.addPropertyChangeListener(arg0);
    }

    public void removePropertyChangeListener(PropertyChangeListener arg0) {
        action.removePropertyChangeListener(arg0);
    }

    public void actionPerformed(ActionEvent e) {
        action.actionPerformed(e);
    }

  }
  
  // This class serves as an action group. What happens is the user 
  // creates an action group recursively. For the user, the group
  // is the delegate for actions directly; when the GUI elements
  // ask for them. they need to be proxies. The trick is that this
  // means that we need to recreate them with the proxies
  // inside, ultimately. Since people might want to customize
  // the behavior of the groups, we need to set up a creator
  // which builds the groups.
  
  public static class ActionGroupFactory {
    
    public ActionGroup createGroup(Action parentAction, 
        List subActions) {
      return new ActionGroup(parentAction, subActions);
    }
    
    public ActionGroup createGroup(String label, 
        List subActions) {
      return new ActionGroup(label, subActions);
    }
    
  }
  
  // This used to be a child of ActionDelegate, and then I realized that
  // (a) I don't want to delegate the property handling (which means
  // I don't want to delegate much of anything) and (b) I need to 
  // use the property handling to do things like monitor the name
  // changes.
  
  // The children of action groups can also be action groups. The 
  // code which sets up these groups tracks the mapping from actions
  // to the groups they're the children of, and when an action is
  // made a parent, the action it's in, if any, has the group substituted.
  
  public static class ActionGroup extends AbstractAction {
    
    private class ActionGroupListener implements PropertyChangeListener {

      public void propertyChange(PropertyChangeEvent evt) {
        String key = evt.getPropertyName ();
        putNewValue(key, evt.getNewValue());
      }
    }
    

    private void putNewValue(String key, Object o) {
      if (key.equals(Action.NAME)) {
        putValue(key, transformName((String) o));
      } else if (key.equals("enabled")) {
        // If we're making this enabled, then do it. 
        // Otherwise, if we're making it disabled, only
        // disable it if everybody is disabled. The parent
        // is sometimes ignored for the purposes of this calculation.
        Boolean val = (Boolean) o;
        if (val.equals(Boolean.TRUE)) {
          setEnabled(true);
        } else if (membersDisabled()) {
          setEnabled(false);
        }
      } else {
        putValue(key, o);
      }
    }
    
    private Action parentAction;
    private Action[] subActions;
    private static Action[] subActionModel = new Action[] {};
    private boolean dummyParent = true;
    
    public boolean isDummyParent() {
      return dummyParent;
    }
    
    // The parents need to be listening to enable settings from
    // the children.
    
    private boolean membersDisabled() {
      if ((!dummyParent) && parentAction.isEnabled()) {
        return false;
      }
      for (int i = 0; i < subActions.length; i++) {
        if (subActions[i].isEnabled()) {
          return false;
        }
      }
      return true;
    }
    
    public ActionGroup(Action parentAction, List subActions) {
      this(parentAction, (Action[]) subActions.toArray(subActionModel), false);
    }
    
    public ActionGroup(Action parentAction, Action[] subActions) {
      this(parentAction, subActions, false);
    }
    
    public ActionGroup(String label, List subActions) {
      // Create it with a dummy action to hang stuff off.
      this(new AbstractAction(label) {
        public void actionPerformed(ActionEvent e) {
        }
      }, (Action[]) subActions.toArray(subActionModel), true);
    }
    
    public ActionGroup(String label, Action[] subActions) {
      // Create it with a dummy action to hang stuff off.
      this(new AbstractAction(label) {
        public void actionPerformed(ActionEvent e) {
        }
      }, subActions, true);
    }
    
    private ActionGroup(Action parentAction, Action[] subActions, boolean dummyParent) {
      super();
      this.dummyParent = dummyParent;
      this.parentAction = parentAction;
      this.subActions = subActions;
      if (DEBUG > 0)
        System.err.println("ActionGroup<init> parent: " + parentAction +
                           " subs: " + subActions.length + " dummy: " + 
                           dummyParent);
      if (DEBUG >2)
        Thread.dumpStack();
      parentAction.addPropertyChangeListener(new ActionGroupListener());
      for (int i = 0; i < this.subActions.length; i++) {
        Action sub = this.subActions[i];
        if (DEBUG > 0)
          System.err.println("\ti=" + i);
        if (sub == null)
          System.err.println("UH OH! Subordinate is null!");
        sub.addPropertyChangeListener(new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) {
            String key = evt.getPropertyName ();
            if (key.equals("enabled")) {
              // The children's disable status may affect the parents.
              Boolean val = (Boolean) evt.getNewValue();
              if (val.equals(Boolean.TRUE)) {
                setEnabled(true);
              } else if (membersDisabled()) {
                setEnabled(false);
              }
            }
          }
        });
      }
      // Now, copy the relevant properties. The problem, of 
      // course, is that Action doesn't require getKeys(), 
      // so we just have to guess. Grrr.
      String[] keys = { Action.NAME, Action.ACCELERATOR_KEY, Action.ACTION_COMMAND_KEY,
          Action.LONG_DESCRIPTION, Action.MNEMONIC_KEY, Action.SHORT_DESCRIPTION,
          Action.SMALL_ICON, JawbAction.HIGHLIGHT_COLOR };
      for (int i = 0; i < keys.length; i++) {
        putNewValue(keys[i], parentAction.getValue(keys[i]));
      }
      setEnabled(!membersDisabled());
    }
    
    public Action[] getSubActions() {
      return subActions;
    }

    public Action getAction() {
      return parentAction;
    }
    
    // This can be overridden by a child in order to
    // change the naming behavior.
    
    public String transformName(String name) {
      return name + "...";
    }

    // This action does nothing.
    
    public void actionPerformed(ActionEvent e) {
      return;
    }
    
    // This method has to be duplicated in ActionProxy. Because
    // we're already keeping our own properties, we should
    // look at us, just in case we have no parent action, but
    // someone set our colors anyway.
    
    public ColorChangeListener handleColorChanges(JComponent c) {
      ColorChangeListener ccl = new ColorChangeListener(c);
      addColor(this, c);
      parentAction.addPropertyChangeListener(ccl);
      return ccl;
    }    
  }
  
  // Finally, we need a class which will collect the groups. It
  // should specialize LinkedHashSet, because that's what all the action
  // sets are at the moment. The default collection uses the 
  // action group factory to build the action groups. 
  
  // A collection is a set of actions with group information.
  // The groups themselves all have child actions; the way
  // to find out whether a child itself corresponds to a group
  // is to ask the collection.
  
  public static class ActionCollection extends LinkedHashSet {
    
    private ActionGroupFactory groupFactory;
    private Hashtable actionsToGroups;
    private Hashtable actionsToChildGroups;
    private Vector groups;
    
    public ActionCollection() {
      this(null);
    }
    
    // This private constructor is for use when constructing the subset sequences.
    private ActionCollection(Hashtable actionsToGroups, Hashtable actionsToChildGroups) {
      this.actionsToGroups = actionsToGroups;
      this.actionsToChildGroups = actionsToChildGroups;
    }
    
    public ActionCollection(ActionGroupFactory factory) {
      if (factory == null) {
        groupFactory = new ActionGroupFactory();
      } else {
        groupFactory = factory;
      }
      actionsToGroups = new Hashtable();
      actionsToChildGroups = new Hashtable();
    }

    // We return the parent of the resulting group, which may be a dummy.
    
    public Action addGroup(Action parentAction, List subActions) {
      if (!doubleCheckSubactions(subActions, parentAction)) {
        return null;
      }
      ActionGroup g = addGroupNoCheck(parentAction, subActions);
      return g.getAction();
    }
    
    public Action addGroup(String label, List subActions) {
      if (!doubleCheckSubactions(subActions, null)) {
        return null;
      }
      ActionGroup g = addGroupNoCheck(label, subActions);
      return g.getAction();
    }
    
    // Don't throw an error for the bookkeeping; this is a programming 
    // mistake, not a runtime mistake. If there are groups for the
    // children, put them in. If all the actions in the group are not
    // of the same conceptual type (text actions, annotation actions,
    // modification actions), complain.  Actions can also only be in one group.
    // The reason for this is that the actions, when fired, need to know
    // what toplevel group they were fired from, and we want a single
    // proxy for each real action.
    
    private String getActionType(Action a) {

      if (a instanceof TextAction) {
        return "TextAction";
      } else if (a instanceof AnnotationAction) {
        return "AnnotationAction";
      } else if (a instanceof ModifyExtentAction) {
        return "ModifyExtentAction";
      } else {
        return "Action";
      }

    }
    
    public boolean doubleCheckSubactions(List subActions, Action parentAction) {
      String groupClass = null;
      if (parentAction != null) {
        if (actionsToGroups.get(parentAction) != null) {
          System.err.println("Not creating group; parent already has a group.");
          return false;
        }
        groupClass = getActionType(parentAction);
      }
      Iterator aIter = subActions.iterator();
      while (aIter.hasNext()) {
        Action a = (Action) aIter.next();
        if ((groupClass != null) && (!groupClass.equals(getActionType(a)))) {
          System.err.println("Not creating group; elements not all of the same conceptual type.");
          return false;
        } else {
          groupClass = getActionType(a);
        }
        if (actionsToChildGroups.get(a) != null) {
          System.err.println("Not creating group; child element already in a group.");
          return false;
        }
      }
      if (DEBUG > 0)
        System.err.println("ActMod.2checkSubs returning true");
      return true;
    }
    
    //  These two things should only be called by the proxy constructor.
    
    private ActionGroup addGroupNoCheck(Action parentAction, List subActions) {
      ActionGroup g = storeGroup(groupFactory.createGroup(parentAction, subActions));
      actionsToGroups.put(parentAction, g);
      Iterator aIter = subActions.iterator();
      while (aIter.hasNext()) {
        actionsToChildGroups.put(aIter.next(), g);
      }
      return g;
    }
    
    private ActionGroup addGroupNoCheck(String label, List subActions) {
      ActionGroup g = storeGroup(groupFactory.createGroup(label, subActions));
      //    A dummy will be created.
      actionsToGroups.put(g.parentAction, g);
      Iterator aIter = subActions.iterator();
      while (aIter.hasNext()) {
        actionsToChildGroups.put(aIter.next(), g);
      }
      return g;
    }
    
    private ActionGroup storeGroup(ActionGroup g) {
      if (groups == null) {
        groups = new Vector();
      }
      groups.add(g);
      return g;
    }
    
    // These are the public methods for traversing the tree.
    
    public boolean isStandaloneAction(Action a) {
      // It's a standalone action if it is neither the child nor
      // parent of a group.
      return (!actionsToChildGroups.containsKey(a)) && (!actionsToGroups.containsKey(a));
    }
    
    public ActionGroup getActionGroup(Action a) {
      // Returns the group the action heads. This may be a dummy action.
      return (ActionGroup) actionsToGroups.get(a);
    }
    
    public Action getRootAction(Action a) {
      // Returns the action at the root of the action tree.
      ActionGroup g = (ActionGroup) actionsToChildGroups.get(a);
      if (g == null) {
        return a;
      } else {
        return getRootAction(g.parentAction);
      }
    }
    
  }
  
  // This class specializes the action delegate, and adds support for 
  // colors, etc. To create the action proxy, we're going to need to
  // be sensitive to whether something is in an action group or not.
  
  // ABOUT PROXIES AND PROPERTIES: When a JMenu adds an action to 
  // itself, it installs a property change listener AND populates
  // itself from the properties on the action. The ActionProxy, of course,
  // has no properties. It specializes ActionDelegate, which delegates
  // all the property manipulation stuff: adding and getting values,
  // adding and removing property change listeners. So when the JMenu
  // asks for properties, it gets relayed to the underlying action,
  // and when it sets/removes change listeners, that happens on the 
  // underlying action as well. This is why the buttons in the 
  // Available Actions pane get renamed even though I don't do anything
  // in fireNameChanged: essentially, the proxy is being bypassed 
  // entirely. I could do the same with the color changes, if I 
  // wanted, and perhaps I should.
  
  // This is a good idea, the right idea, EXCEPT for what happens
  // with ActionGroups of ActionProxies. We DON'T want these to
  // just be bypassed, because the whole idea is that they may 
  // have different properties than their parent, e.g., a different
  // label. This gets a little tricky. The proxy has a property change
  // listener on its head, which captures the color and name stuff 
  // and relays it to the action change listeners. So let's say we
  // want to capture a name and relay it. Well, now we have a problem.
  // The current mechanism works fine for proxies, because they're
  // essentially bypassed. But if we want to capture the name for the group,
  // and use the SAME mechanism to relay the name to the JMenu, we've
  // got a problem. The JMenu can't bypass the group, because the 
  // group might have a different name and that's the name we want
  // the menu to see. So the action group can't delegate the property
  // stuff, any of it, or you might get infinite loops. If you
  // don't delegate one part of the property change stuff, you can't delegate anything. 
  // So maybe, instead of inheriting from a delegate,
  // I ought to inherit from AbstractAction. So not all the elements
  // in the list will be ActionDelegates. That's not such a big deal.
  // Well, it really hoses some stuff I've done in the menu construction
  // stuff, which assume that every stage is an ActionDelegate. Keep thinking.
  
  // Now I know what to do. I should separate the color change stuff
  // into a child of the color change listener, and just pass that
  // along. The ActionProxy and ActionGroup should be completely out
  // of the color change business. The buttons and menus need the
  // color change. Ditto with the action available (that's enabled or not).
  // The only thing that's special is the listener for which actions
  // are fired. That's the one that should be separate. Everything 
  // else should be handled by property pass through (or delegation).
  
  public class ActionProxy extends ActionDelegate {

    // Constructor.
    
    public ActionProxy(Action a) {
      super(a);

      // Do the bookkeeping.
      actionToProxyMap.put(action, this);
      
      // The initial state of all actions should be disabled.
      setEnabled(false);
      
    }
    
    // This method reports that actions were fired.

    public void actionPerformed(ActionEvent arg0) {
      super.actionPerformed(arg0);
      fireActionFiredEvent(this);
    }
    
    // This method has to be duplicated in ActionGroup.
    
    public ColorChangeListener handleColorChanges(JComponent c) {
      ColorChangeListener ccl = new ColorChangeListener(c);
      addColor(action, c);
      action.addPropertyChangeListener(ccl);
      return ccl;
      
    }
    
  }

}

