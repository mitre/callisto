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
import org.mitre.jawb.prefs.PreferenceDialog;
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.services.CallistoMATClient;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/** Defines the additional methods that must be provided by a task
 * that is to work with the TooCAAn system.  */

public interface ToocaanTask extends Task { 

  /* Static values that cannot be overridden by an implementation */
  //public static final String HIGH_PRIORITY_KEY = "org.mitre.toocaan.priority";
  //public static final String USER_SELECTED_KEY = "org.mitre.toocaan.usersel";
  public static final String RECON_KEY = "org.mitre.toocaan.stage";
  public static final String PRIORITIZATION_MODE_KEY = "org.mitre.toocaan.primode";
  public static final String SEGMENT_IDS_KEY = "org.mitre.toocaan.segids";
  //public static final String DOCMODE_KEY = "org.mitre.toocaan.docmode";
  // document modes
  public static final String BOOTSTRAP_MODE = "bootstrap";
  public static final String USER_SELECTED_MODE = "user_selected";
  public static final String DOC_PRIORITIZATION_MODE = "document";
  public static final String SEG_PRIORITIZATION_MODE = "segment";


  // Segment statuses we can get from the annotation
  public static final String SEGMENT_COMPLETED_STATUS = "human gold";
  public static final String SEGMENT_PROPOSED_STATUS = "non-gold";
  public static final String SEGMENT_RECONCILED_STATUS = "reconciled";
  // Segment highlight keys based on status
  public static final String SEGMENT_COMPLETED_KEY = SEGMENT_COMPLETED_STATUS;
  public static final String SEGMENT_PROPOSED_KEY = SEGMENT_PROPOSED_STATUS; 
  // Segment highlight keys based on status plus additional information
  public static final String SEGMENT_LOWERPRI_KEY = "non-gold_low";
  public static final String SEGMENT_BOOTSTRAP_KEY = "bootstrap";

  /* Static values for names of the phases */
  public static final String HAND_ANNOTATION = "hand_annotation";
  // real reconciliation phases are retrieved from workspace
  // this is a placeholder string for before we get that
  public static final String RECONCILIATION_UNKNOWN = "reconciliation_unknown";

  // overrideable
  public static String SEGMENT_NAME = "SEGMENT";
  public static String SEGMENT_EXTENT_NAME = "SEGMENT-EXTENT";
  public static String VOTE_NAME = "VOTE";
  public static String TXT_NAME = "TXT";
  public static String UNTAG_NAME = "untaggable";
  public static String METADATA_NAME = "METADATA";


  /** returns the name of the annotation type for the segment extent
   * annotation */
  public String getSegmentExtentName();

  /** returns the name of the annotation type for the segment (container)
   * annotation */
  public String getSegmentName();

  /** returns the name of the priority attribute of segment extent
   * annotations */
  public String getSegmentPriorityAttrName();
 
  /** returns the name of the status attribute of segment extent
   * annotations */
  public String getSegmentStatusAttrName();
 
  /** returns the name of the extent attribute of segment (container)
   * annotations */
  public String getSegmentExtentAttrName();
 
  /** returns the status attribute value used to indicate human gold
   * status */
  public String getHumanGoldStatusName();
 
  /** returns the name of the reviewed_by attribute of segment 
   * annotations */
  public String getReviewedByAttrName();
 
  /** returns the name of the to_review attribute of segment 
   * annotations */
  public String getToReviewAttrName();
 
  /** Takes a SEGMENT_EXTENT annotation and returns the boolean
   *  value of its SEGMENT_TO_REVIEW_ATTR.  Returns false if the
   *  passed in annotation is not a SEGMENT_EXTENT.
   */
  public boolean segmentNeedsReview(AWBAnnotation segment);

  /** Takes a SEGMENT_EXTENT annotation and returns true IFF it is a
   *  SEGMENT EXTENT and its SEGMENT_TO_REVIEW_ATTR is true and the
   *  passed in userid is not already in its SEGMENT_REVIEWED_BY_ATTR
   *  list.
   */
  public boolean segmentNeedsReview(AWBAnnotation segment, String userid);

  /** Takes a SEGMENT_EXTENT annotation and a userid and returns true if the
   *  SEGMENT_REVIEWED_BY_ATTR list contains the userid.  Returns false if the
   *  passed in annotation is not a SEGMENT_EXTENT.
   */
  public boolean userHasReviewed(AWBAnnotation segment, String userid);

  /** Takes as input a comma-separated list in String format, and checks
   *  whether a passed-in item appears on the list
   */
  public boolean listContainsString(String list, String item);

  /** Takes a VOTE annotation and returns the boolean
   *  value of its VOTE_IGNORE_ATTR.  Returns false if the
   *  passed in annotation is not a VOTE.
   */
  public boolean isIgnoreVote(AWBAnnotation vote);

  /** Takes a VOTE annotation and returns the boolean
   *  value of its VOTE_BAD_BOUNDS_ATTR.  Returns false if the
   *  passed in annotation is not a VOTE.
   */
  public boolean isBadBoundsVote(AWBAnnotation vote);

  public void setIgnoreValue(AWBAnnotation vote, boolean b);
  public void setBadBoundsValue(AWBAnnotation vote, boolean b);

  /** determines, sets and returns the value of the current highest
   * segment priority */
  /****
  public String setCurrentPriority (JawbDocument jd);
  ****/

  /** returns the current highest segment priority */
  /*****
  public String getCurrentPriority(JawbDocument jd);
  *****/

  /** is used to initialize (or re-initialize) the Annotation Types
   *  based on the info received from the CallistoMATClient.  Must work 
   *  whether it has been called before or not.
   */
  public void setTagInfo(PreferenceDialog prefsDialog, 
                         CallistoMATClient client);


  public LinkedHashSet getCustomTypesSet();

  /** should return true if the maia file for the task contains a spam
   *  tag type with gid, attrlist and vallist attributes, and if all non-task
   *  tags should be stored in such tags upon import, and restored upon 
   *  export 
   */
  public boolean keepSpam();

}
