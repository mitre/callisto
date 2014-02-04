
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

import java.util.EventObject;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Set;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import gov.nist.atlas.type.AnnotationType;
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;

/**
 * Class to define an Action property key for AnnotationType, and allow
 * Actions to be programmatically tested to see if they work on a particular
 * type of annotation. While calls to AnnotationActions may called from
 * TextActions in a chain, Actions are purposefully not able to subclass
 * both. 
 */
public abstract class AnnotationAction extends AbstractAction {

  public static final int DEBUG = 0;

  /**
   * This is the Key for the AnnotationType this action acts on. The value for
   * this key is set automatically in the constructor with the {@link
   * AnnotationType}. This value must remain set for integration into the
   * MainTextPane.
   * @see Action#getValue
   * @see TaskToolKit#getActions
   */
  public static final String ANNOTATION_TYPE_KEY = "AnnotationType";

  public static final String MAX_TARGETS_KEY = "MaxTargets";
  
  /**
   * Create an action with the name specified, which advertises that it
   * manipulastes annotations of type <code>type</code> and operates on an
   * unlimited number of annotations.
   */
  public AnnotationAction (String name, AnnotationType type) {
    super (name);
    putValue (ANNOTATION_TYPE_KEY, type);
    putValue (MAX_TARGETS_KEY, new Integer (-1));
  }

  /**
   * Create an action with the name specified, which advertises that it
   * manipulastes annotations of type <code>type</code>.
   */
  public AnnotationAction (String name, AnnotationType type, int max) {
    super (name);
    putValue (ANNOTATION_TYPE_KEY, type);
    putValue (MAX_TARGETS_KEY, new Integer (max));
  }

  /**
   * Convenience method to get the annotation type this action works on.
   * Equivalent to getValue (AnnotationAction.ANNOTATION_TYPE_KEY)).
   */
  public AnnotationType getAnnotationType () {
    return (AnnotationType) getValue (ANNOTATION_TYPE_KEY);
  }
  
  /* Add the capability of setting exclusions for annotatation actions
   * which don't have annotation types. See AnnotationPopupListener.
   */
  
  private LinkedHashSet exclusions;
  
  public void setAnnotationExclusion(AnnotationType t) {
    // Fail silently if there's an annotation type.
    if (getValue(ANNOTATION_TYPE_KEY) != null) {
      return;
    }
    if (exclusions == null) {
      exclusions = new LinkedHashSet();
    }
    if (!exclusions.contains(t)) {
      exclusions.add(t);
    }
  }
  
  public void removeAnnotationExclusions(AnnotationType t) {
    if (exclusions != null) {
      exclusions.remove(t);
    }
  }

  // Check the action eligibility for a unique type.
  public boolean isActionEligible(AnnotationType type, int size) {
    int maxTargets = getMaxTargets();
    if (maxTargets == -1 || maxTargets >= size) {
      AnnotationType actType = getAnnotationType();
      if (DEBUG > 2)
        System.err.println("actionEligible: AnnotationAction " +
            getValue(Action.NAME) + 
            " maxTargets = " + maxTargets +
            " actType = " + 
            (actType==null?"null":actType.getName()));
      if (actType == null) {
        // No restrictions. Check the exclusion.
        if ((exclusions == null) || (exclusions.size() == 0) ||
            (type == null) || !exclusions.contains(type)) {
          return true;
        }
      } else if (type != null && actType.equals(type)) {
        // There is a restriction and it has to match.
        return true;
      }
    }
    return false;
  }
  
  // Check the action eligibility for a set of annotations, for which
  // a possibly common type has been computed. Similar to above, except
  // that in the exclusion case, we need to check each annotation if
  // there are multiple types.
  
  public boolean isActionEligible(Set annots, AnnotationType commonType) {
    int maxTargets = getMaxTargets();
    int size = annots.size();
    boolean multiTypes = (size > 0) && (commonType == null);
    if (maxTargets == -1 || maxTargets >= annots.size()) {
      AnnotationType actType = getAnnotationType();
      if (DEBUG > 2)
        System.err.println("actionEligible: AnnotationAction " +
            getValue(Action.NAME) + 
            " maxTargets = " + maxTargets +
            " actType = " + 
            (actType==null?"null":actType.getName()));
      if (actType == null) {
        // No restrictions. Check the exclusion. If multiTypes is true,
        // the we need to check each annot.
        if (exclusions == null || (exclusions.size() == 0)) {
          // If there are no exclusions, we're fine.
          return true;
        } else if (commonType != null) {
          // If there are exclusions, but there's a common type,
          // we're fine as long as the common type isn't in the exclusions.
          if (!exclusions.contains(commonType)) {
            return true;
          }
        } else if (!multiTypes) {
          // Exclusions and no common type. If multiTypes is false,
          // we're golden.
          return true;
        } else {
          // Otherwise, we're at the hard part. Exclusions, multiple types,
          // no common type. Check each annotation. Grrrr.
          Iterator annotIter = annots.iterator();
          while (annotIter.hasNext()) {
            AnnotationType nextType = 
              ((AWBAnnotation)annotIter.next()).getAnnotationType();
            if (exclusions.contains(nextType)) {
              return false;
            }
          }
          // If they're all OK, return true.
          return true;
        }
      } else if ((!multiTypes) && commonType != null && actType.equals(commonType)) {
        // There is a restriction and it has to match. Multiple types
        // are right out.
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the maximum number of annotations this action may be performed
   * upon. Default is -1, indicating there is no limit.
   *
   * @return maximum number of annotations which may be affected, or -1 if
   *   there is no limit.
   */
  public int getMaxTargets () {
    return ((Integer) getValue (MAX_TARGETS_KEY)).intValue ();
  }
  
  /**
   * Returns the document being acted upon, indicated by the source event.
   * @param evt The source event
   */
  public static JawbDocument getJawbDocument (EventObject evt) {
    return GUIUtils.getJawbDocument (evt);
  }

  /**
   * Means for subclasses to retrieve the currently selected annotations. If
   * the event passed in (from actionPerformed) was fired from an
   * AnnotationTable, only the annotations selected in that table are
   * returned, otherwise all annotations in the current documents
   * 'annotationSelectionModel are returned<p>
   *
   * This is neccissary since a standalone list of operand actions is required
   * as an action has potential to change the list it's trying to work on,
   * thereby doing more that it intended, or causing outright failure.
   */
  protected Set getSelectedAnnots (EventObject e) {
    JawbComponent comp = GUIUtils.getJawbComponent (e);
    if (comp == null) {
      System.err.println ("No JawbComponent For Action");
      Thread.dumpStack();
      return Collections.EMPTY_SET;
    }
    return comp.getSelectedAnnots ();
  }
}
