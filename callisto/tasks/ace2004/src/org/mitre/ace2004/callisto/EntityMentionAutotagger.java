/*
 * Copyright (c) 2002-2007 The MITRE Corporation
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

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.PhraseTaggingAnnotation;
import org.mitre.jawb.atlas.NamedExtentRegions;
import org.mitre.jawb.atlas.HasSubordinates;
import org.mitre.jawb.atlas.UnmodifiableAttributeException;
import org.mitre.jawb.gui.JawbDocument;
import org.mitre.jawb.gui.MainTextFinder;
import org.mitre.jawb.tasks.DefaultAutotagger;

import gov.nist.atlas.type.AnnotationType;

import javax.swing.JTextPane;


public class EntityMentionAutotagger extends DefaultAutotagger {

  private static int DEBUG = 0;

  /**
   * Auto-tag the given target tag according to the model: create a
   * new AWBAnnotation anchored at the given start and end locations,
   * that is of the same AnnotationType as the given model.
   *
   * Duplicates only PhraseTaggingAnnotation; for any other type,
   * returns null
   *
   * @param doc the JawbDocument in which the new annot should be created
   * @param model the tag whose annotation should be duplicated
   * @param start the start offset in the text for the new annotation
   * @param end the end offset in the text for the new annotation
   * @return the created AWBAnnotation, or null if the autotag failed 
   */
  public AWBAnnotation autoTag (JawbDocument doc, AWBAnnotation model, 
                                int start, int end) {
    if (DEBUG > 0)
      System.err.println("EMAutotagger.autotag start: " + start +
                         " end: " + end);
      
    AWBAnnotation newAnnot = null;
    if (model instanceof PhraseTaggingAnnotation) {
      if (DEBUG > 1)
        System.err.println("EMAutotagger.autotag model is PhraseTaggingAnnot");
      AnnotationType modelType = model.getAnnotationType();
      // creating in 2 steps is safer in case of odd structures
      newAnnot = doc.createAnnotation (modelType);
      ((PhraseTaggingAnnotation)newAnnot).setTextExtents (start, end);
      //      return doc.createAnnotation(modelType, start, end, null);      
    } else if (model instanceof NamedExtentRegions) {
      if (DEBUG > 1)
        System.err.println("EMAutotagger.autotag model is NamedExtentRegions");
      AnnotationType modelType = model.getAnnotationType();
      // create with new start and end "full" extent
      newAnnot = doc.createAnnotation(modelType, start, end, "full");
      NamedExtentRegions nerModel = (NamedExtentRegions)model;
      // set "full" extents (not sure why createAnnotation doesn't)
      ((NamedExtentRegions)newAnnot).setTextExtents("full", start, end);
      // set "head" extents by imitating the offsets from "full" as
      // found in the model
      int modelStartOffset = nerModel.getTextExtentStart("head") -
        nerModel.getTextExtentStart("full");
      int modelEndOffset = nerModel.getTextExtentEnd("full") -
        nerModel.getTextExtentEnd("head");
      ((NamedExtentRegions)newAnnot).
        setTextExtents("head", start+modelStartOffset, end-modelEndOffset);

    } else {
      // this wasn't really auto-taggable, so exit
      return null;
    }

    // now copy all other attribute values
    String[] keys = model.getAttributeKeys();
    if (DEBUG > 1) {
      System.err.println("EMAutotagger.autoTag attr keys: ");
      for (int j=0; j<keys.length; j++) {
        System.err.println(keys[j]);
      }
    }
    for (int i=0; i<keys.length; i++) {
      String key = keys[i];
      System.err.println("key = " + key);
      if (key.startsWith("head.") || key.startsWith("full.") ||
          key.startsWith("TextExtent") || key.equals("ace_id")) {
        // head and full attributes are already set above
        // ace_id should not be copied but generated, which will
        // happen automatically
        continue;
      }
      System.err.println("settable key=" + key);
      try {
        if (DEBUG > 1)
          System.err.println("EMAutotagger.autoTag setting attr val for " +
                             key);
        newAnnot.setAttributeValue(key,model.getAttributeValue(key));
      } catch (UnmodifiableAttributeException x) {
        System.err.println("EntityMentionAutotagger.autoTag unable to copy value of " + key + " attribute due to UnmodifiableAttrException " + x);
      }
    }

    // make a subordinate of the same parent, if applicable
    HasSubordinates parent = ACE2004Task.getMentionParent(model);
    if (parent != null) {
      parent.addSubordinate(newAnnot);
    }

    // finally, return the newly created Annotation
    return newAnnot;
  }


  /**
   * Searches through the document for strings identical to the
   * content of annot, and calls autoTag to create a new annotation
   * wherever an identical string is found
   *
   * This implementation works only for PhraseTaggingAnnotation.
   * For any other type of annotation it returns without doing anything.
   *
   * @param doc the JawbDocument in which matching strings should be
   * found, and in which the new annotations should be created
   * @param docText the text of the document, as a String
   * @param finder the MainTextFinder for this document
   * @param annot the annotation to be copied
   * @param autotagMode should be "Automatic" or "Query" (If it is
   * "None", doAutotag should not be called at all).  In query mode, 
   * the user should be queried prior to the creation of a new tag.
   * @param forwardOnly if true indicates that autotagging should
   * proceed forward only and not wrap back to the top
   * @param untaggedOnly if true indicates that autotagging should
   * only add a tag to a region of text with no other pre-existing tags
   * covering or overlapping it
   */
  public void doAutotag(JawbDocument doc, String docText,
                        MainTextFinder finder, JTextPane textPane,
                        AWBAnnotation annot, String autotagMode,
                        boolean forwardOnly, boolean untaggedOnly) {
    if (DEBUG > 0)
      System.err.println("EMAutotagger.doAutotag: " + annot);
  
    if (!autotagMode.equals("None")) {
      int modelStart = -1;
      int modelLen = -1;
      int modelEnd = -1;
      String content = null;

      if (annot instanceof PhraseTaggingAnnotation) {
        PhraseTaggingAnnotation ptannot = (PhraseTaggingAnnotation)annot;
        content = ptannot.getTextExtent();
        if (DEBUG > 0)
          System.err.println("EMAutotagger.doAutotag for content = >>" + 
                             content + "<<");
        modelStart = ptannot.getTextExtentStart();
        modelEnd = ptannot.getTextExtentEnd();
      } else if (annot instanceof NamedExtentRegions) {
        NamedExtentRegions neannot = (NamedExtentRegions)annot;
        content = neannot.getTextExtent("full");
        if (DEBUG > 0)
          System.err.println("EMAutotagger.doAutotag for NamedExtentRegions annot with content = >>" + content + "<<");
        modelStart = neannot.getTextExtentStart("full");
        modelEnd = neannot.getTextExtentEnd("full");
      } else {
        return;
      }

      modelLen = modelEnd - modelStart;
      if (modelLen == 0)
        return;

      findAndTagInstances(modelStart, modelLen, content, doc, docText,
                          finder, textPane, annot, autotagMode,
                          forwardOnly, untaggedOnly);
    } 
  }

  public boolean isAutoTaggable(AWBAnnotation annot) {
    return (((annot instanceof PhraseTaggingAnnotation) ||
             (annot instanceof NamedExtentRegions)) &&
            !(annot.getAnnotationType().equals(ACE2004Utils.EVENT_MENTION_TYPE)));
  }

  public boolean isTriggerAutoTaggable(AWBAnnotation annot) {
    return (annot instanceof PhraseTaggingAnnotation &&
            !(annot.getAnnotationType().equals(ACE2004Utils.EVENT_MENTION_TYPE)));

  }

}






