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

import org.mitre.jawb.gui.JawbDocument;
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.gui.MainTextFinder;

import javax.swing.JTextPane;


/**
 * Interface for tasks to define their own autotagger methods.
 *
 */

public interface Autotagger {

  /**
   * Auto-tag the given target tag according to the model: create a
   * new AWBAnnotation anchored at the given start and end locations,
   * that is like the model annotation in some task-appropriate way.
   *
   * Implementations of autoTag are permitted to use as much or as
   * little as they choose of the annotation information in the model
   * annotation to create the target annotation at the given offsets.
   *
   * @param doc the JawbDocument in which the new annot should be created
   * @param model the tag whose annotation should be duplicated
   * @param start the start offset in the text for the new annotation
   * @param end the end offset in the text for the new annotation
   * @return the created AWBAnnotation, or null if the autotag failed 
   */
  public AWBAnnotation autoTag (JawbDocument doc, AWBAnnotation model, 
                                int start, int end);

  /**
   * Searches through the document for strings identical to the
   * content of annot, and calls autoTag to create a new annotation
   * wherever an identical string is found
   *
   * Implementations of doAutotag are permitted to use whatever
   * attributes of the annotation they like to determine the string to
   * search for in the text, however it is anticipated that the
   * textExtent or "full" extent will be used.
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
                        boolean forwardOnly, boolean untaggedOnly);


  /**
   * Returns true if the given annotation is of an autotaggable type
   * using this Autotagger, false otherwise.
   *
   * @param annot the annotation whose autotaggable status is being
   * assessed
   */
  public boolean isAutoTaggable(AWBAnnotation annot);

  /**
   * Returns true if the given annotation is of a type that can be
   * safely autogged by triggering on creation of the tag and setting
   * of the main text extent using this Autotagger, false otherwise.
   * The annotations accepted by this method should probably be a subset
   * of the annotations accepted by isAutoTaggable() but this is not enforced.
   *
   * @param annot the annotation whose autotaggable status is being
   * assessed
   */
  public boolean isTriggerAutoTaggable(AWBAnnotation annot);
}


