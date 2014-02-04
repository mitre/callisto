/*
 * Copyright (c) 2002-2010 The MITRE Corporation
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

import java.util.*;
import javax.swing.*;
import java.io.*;
import java.net.URI;

import org.mitre.jawb.*;
import org.mitre.jawb.atlas.*;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.ToocaanTask;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import gov.nist.atlas.type.AnnotationType;

/** This object keeps track of the adjudication of a single JawbDocument 
    which should be a document that should be an imported adjudication
    document from a Workspace */
public class AdjudicationDocument {
  
  // debugging 0==off
  private static int DEBUG = 0;
  
  /** The task */
  private ToocaanTask task;

  /** The main JawbDocument (for the entire signal) */
  private JawbDocument doc;

  /** The JawbFrame */
  private JawbFrame jf;

  /** Map from segment start to segment extent, for each 
   * segment requiring adjudication */
  private TreeMap<Integer,PhraseTaggingAnnotation> segMap = new TreeMap();

  /** Map from a SEGMENT Annotation to the SET of VOTE Annotations relevant
   *  to it */
  private HashMap<AWBAnnotation,Set<AWBAnnotation>> voteMap = new HashMap();

  /** Adjudication Dialog */
  private AdjudicationDialog dialog;

  /** The user's userid */
  private String userid;

  /** The current segment being adjudicated */
  private PhraseTaggingAnnotation segment;

  /** The current segment's start index in doc */
  private int start;

  /** The current segment's end index in doc */
  private int end;

  /** The last id used for constructing Callisto Ids for new votes and
   * annotations */
  private int lastId;

  /** Indicates whether a set of segments was passed in.  If false,
   * this means that this AdjDoc was meant to review all segments that
   * require review in the JawbDocument */
  private boolean providedSegments = false;

  public AdjudicationDocument (JawbDocument doc, ToocaanTask task, 
                               JawbFrame jf) {
    this(doc, task, jf, null);
  }

  public AdjudicationDocument (JawbDocument doc, ToocaanTask task, 
                               JawbFrame jf, String inUserid) {
    this (doc, task, jf, inUserid, null);

  }

  public AdjudicationDocument (JawbDocument doc, ToocaanTask task, 
                               JawbFrame jf, String inUserid, Set annots) {

    this.doc = doc;
    this.task = task;
    this.jf = jf;
    this.userid = inUserid;
    this.lastId = 0;

    if (userid == null) {
      userid = jf.getMATWorkspaceUserid();
    }

    if (DEBUG > 0)
      System.err.println("AdjDoc.init userid: " + userid);

    if (annots == null) {
      // no set of annots passed in -- usual case
      // set up to do all segments that need review
      for (Iterator iter = doc.getAnnotationModel().getAllAnnotations();
           iter.hasNext(); ) {
        AWBAnnotation annot = (AWBAnnotation) iter.next();
        if (DEBUG > 2)
          System.err.println("AdjDoc.init checking annot: " + annot + 
                             "type: " + annot.getAnnotationType().getName());
        // check if it's a segment extent
        if (task.getSegmentName().equals(annot.getAnnotationType().getName())) {
          // check if it needs review
          PhraseTaggingAnnotation extent = (PhraseTaggingAnnotation)
            annot.getAttributeValue(task.getSegmentExtentAttrName());
          if (task.segmentNeedsReview(extent, userid)) {
            System.err.println ("Found a segment requiring review");
            int start = extent.getTextExtentStart();
            System.err.println("Putting " + start + " -> " + extent +
                               " in segMap");
            segMap.put(new Integer(start), extent);
          }
        }
      }
    } else {
      // a set of segment extents has been passed in 
      // set up to do only these segments
      providedSegments = true;
      for (Iterator iter = annots.iterator(); iter.hasNext(); ) {
        PhraseTaggingAnnotation extent = (PhraseTaggingAnnotation)iter.next();
        int start = extent.getTextExtentStart();
        System.err.println("Putting specified" + start + " -> " + extent +
                           " in segMap");
        segMap.put(new Integer(start), extent);
      }
    }

    // set up the voteMap 
    AWBDocument awbdoc = (AWBDocument)doc.getAnnotationModel();
    Iterator voteIter = awbdoc.getAnalysisWithRole("vote-set").getAllAnnotations().iterator();
    for ( ; voteIter.hasNext(); ) {
      AWBAnnotation vote = (AWBAnnotation) voteIter.next();
      AWBAnnotation segment = (AWBAnnotation)vote.getAttributeValue("segment");
      Set voteSet = voteMap.get(segment);
      if (voteSet == null) {
        voteSet = new HashSet<AWBAnnotation>();
      }
      voteSet.add(vote);
      voteMap.put(segment, voteSet);
      if (DEBUG > 1)
        System.err.println("AdjDoc.init adding " + segment + " -> " + vote +
                           " to voteMap.  voteSet for this segment now has " +
                           voteSet.size() + " entries.");
    }

    ActionListener adjListener = new AdjListener(userid);
    WorkspaceDashboard dash = jf.getMATWorkspaceDash();
    String phase = dash.getMATPhase();
    dialog = new AdjudicationDialog(jf, phase+": "+userid, false, doc,
                                    task, userid, adjListener, null, this);
    
    if (nextSegment())
      dialog.show();
  }

  public Set<AWBAnnotation> getVotes(AWBAnnotation segment) {
    // if it's not a SEGMENT, return null
    // otherwise return the Set of votes associated with the segment
    // which may be an empty set
    if (!segment.getAnnotationType().getName().equals(ToocaanTask.SEGMENT_NAME))
      return null;

    Set votes = voteMap.get(segment);
    if (votes == null)
      return Collections.EMPTY_SET;
    else
      return votes;
  }

  /** grabs the next segment and creates an AdjPanel for it, and
      inserts this into the adjDialog
      Scrolls the MainTextPane to show the segment in context
  */ 
  public boolean nextSegment() {
    // pollFirstEntry removes and returns the entry with the lowest key
    Map.Entry<Integer,PhraseTaggingAnnotation> entry = segMap.pollFirstEntry();
    // int start = entry.getKey().intValue();
    if (entry == null) {

      // if segments were provided and you've finished reviewing them,
      // just exit the dialog with no further action
      if (providedSegments)
        return false; // indicates that there was no next segment

      // Ask the user what they want to do now that they're done
      System.err.println("No more Segments to Adjudicate");
      String [] options = {"Save & Next", 
                           "Save & Close", 
                           "Return to the Document Without Saving"};
      int choice = 
        JOptionPane.showOptionDialog(jf, "No more segments to adjudicate",
                                     "Done Adjudicating",
                                     JOptionPane.YES_NO_CANCEL_OPTION,
                                     JOptionPane.INFORMATION_MESSAGE,
                                     null, 
                                     options,
                                     options[0]);
      if (DEBUG > 0)
        System.err.println("AdjDoc done dialog: user choice = " + choice);

      JawbLogger jawbLogger = Jawb.getLogger();
      jawbLogger.info(JawbLogger.LOG_DIALOG_CHOICE, 
                      new Object[] {"DoneAdjudicating", new Integer(choice)});

      if (choice == JOptionPane.CANCEL_OPTION ||
          choice == JOptionPane.CLOSED_OPTION) {
        // return to the document without saving
        return false;
      }
      // in either remaining case we save and close the current
      // document
      WorkspaceDashboard dash = jf.getMATWorkspaceDash();
      // get basename and lockId for the log
      String basename = dash.getMATDocBasename();
      String lockId = jf.getMATLockId();
      // Save current document & close the transaction:
      // release the lock, do not mark as done, 
      // do mark as reconciliation document 
      dash.saveWorkspaceDocument(true, false, true);
      //  log workspace save and lock release
      jawbLogger.logSave(JawbLogger.LOG_WORKSPACE_SAVE, basename);
      jawbLogger.info(JawbLogger.LOG_RELEASE_LOCK, new Object[] {lockId});

      // close the current document
      jf.close(doc, dash);
      // log AFTER, because the effect of closing might log some
      // stuff (in fact, it does).
      String p = doc == null ? null : doc.getPath();
      if (p != null) {
        jawbLogger.info(JawbLogger.LOG_CLOSE_FILE, new Object [] { p });
      }
      dash.uploadLog();
      jf.updateWSMenuOptions();

      
      if (choice == JOptionPane.YES_OPTION) {
        // move to Next document (and log it)
        String workspaceFolder = dash.getMATFolder();
        // will log itself before calling finish method
        jf.importNextWorkspaceDoc (dash, workspaceFolder);
        /***
        basename = dash.getMATDocBasename();
        jawbLogger.info(JawbLogger.LOG_WORKSPACE_IMPORT, 
                        new Object [] { workspaceFolder, basename });
        ****/
      }
      return false; // indicates that there is no next segment

    }
    PhraseTaggingAnnotation foundSegment = entry.getValue();
    if (DEBUG > 1)
      System.err.println("AdjDoc.nextSeg() found: " + foundSegment);
    SubordinateSetsAnnotation parent = (SubordinateSetsAnnotation)
      foundSegment.getSuperAnnotation(task.getAnnotationType("SEGMENT"));
    dialog.setSegment(parent);
    this.segment = foundSegment;
    this.start = segment.getTextExtentStart();
    this.end = segment.getTextExtentEnd();
    Jawb.getLogger().info(JawbLogger.LOG_ADJUDICATION_SEGMENT,
                          new Object[] {start, end, 
                                        foundSegment.getTextExtent()});
    // highlight the segment  
    jf.highlightInMainComponent(task, start, end);
    // scroll the MainTextPane to show the segment in context
    jf.makeVisibleInMainComponent(task, start, end);
    return true;
  }

  public String getUser() {    return userid;
  }

  /** use an instance of this class as the OK Listener on the
    * AdjudicationDialog
    */
  private class AdjListener implements ActionListener {
    private String userid;

    public AdjListener(String userid) {
      super();
      this.userid = userid;
    }

    public void actionPerformed (ActionEvent e) {
      // get the vote from the dialog
      // vote is a vote id, or "__discard" or "__custom"
      String vote = dialog.getVote();
      AWBAnnotation origVoteAnnot = dialog.getVoteAnnot(userid);
      if (DEBUG > 0)
        System.err.println("AdjListener.actionPerf vote: " + vote);
      Jawb.getLogger().info(JawbLogger.LOG_ADJUDICATION_VOTE,
                            new Object[] {vote});
      
      if (vote.equals("__discard")) {
        addIgnoreVote(userid);
        if (origVoteAnnot != null && !task.isIgnoreVote(origVoteAnnot)) {
          // user had not previously voted to discard, so remove
          // userid from original vote
          removeVote(origVoteAnnot, userid);
        }
      } else if (vote.equals("__badbounds")) {
        addBadBoundsVote(userid);
        if (origVoteAnnot != null && !task.isBadBoundsVote(origVoteAnnot)) {
          // user had not previously voted for bad boundaries, so remove
          // userid from original vote
          removeVote(origVoteAnnot, userid);
        }
      } else if (vote.equals("__custom")) {
        //createNewVote(segment, dialog.getMiniJD(vote), userid);
        if (DEBUG > 1)
          System.err.println("AdjListener.ap() segment: " + 
                             dialog.getSegment());
        createNewVote(dialog.getSegmentExtent(), dialog.getMiniJD(vote), 
                      userid);
        if (origVoteAnnot != null)
          removeVote(origVoteAnnot, userid);
      } else {
        AWBAnnotation selectedVoteAnnot = dialog.getVoteFromId(vote);
        if (selectedVoteAnnot == null) {
          GUIUtils.showError("Should never happen: selected vote is null");
          return; //TODO returning here is probably the wrong thing --
                  //what is the right thing?
        }
        if (!selectedVoteAnnot.equals(origVoteAnnot)) {
          // if the user voted for anything other than their original annots
          // remove their votes from their old annots, and add their vote to
          // their new annots
          addVote(selectedVoteAnnot, userid);
          removeVote(origVoteAnnot, userid);
        }
      }

      // in all cases, mark the user as having reviewed this segment
      addReviewed(dialog.getSegmentExtent(), userid);

      // go on to the next segment
      nextSegment();
    }
  }
  
  /** Adds userid to the list of annotators in the annotator attribute
   * of the voteAnnot.  Assumes userid is not already on the list.
   */
  private void addVote(AWBAnnotation voteAnnot, String userid) {
    String annotators = (String)voteAnnot.getAttributeValue("annotator");
    StringBuffer newAnnotatorsList = new StringBuffer(annotators);
    if (DEBUG > 1)
      System.err.println("addVote " + userid + " to " + newAnnotatorsList +
                         " orig length: " + newAnnotatorsList.length());
    if (newAnnotatorsList.length() > 0)
      newAnnotatorsList.append(",");
    newAnnotatorsList.append(userid);
    if (DEBUG > 1)
      System.err.println("\tannotators now: " + newAnnotatorsList);

    try {
      voteAnnot.setAttributeValue("annotator", newAnnotatorsList.toString());
    } catch (Exception x) { /* c'est la vie */ }

    // don't do this from here anymore, reviewed goes on the segment now
    // addReviewed(voteAnnot, userid);
  }

  /** Adds userid to the list of annotators in the reviewed attribute
   * of the segment extent.  Assumes userid is not already on the list.
   */
  private void addReviewed(AWBAnnotation extent, String userid) {
    String reviewedBy = task.getReviewedByAttrName();
    String reviewers = (String)extent.getAttributeValue(reviewedBy);
    if (reviewers == null)
      reviewers = "";
    StringBuffer newReviewersList = new StringBuffer(reviewers);
    if (newReviewersList.length() > 0)
      newReviewersList.append(",");
    newReviewersList.append(userid);

    try {
      extent.setAttributeValue(reviewedBy, newReviewersList.toString());
    } catch (Exception x) { /* c'est la vie */ }
  }

  /** gets the "ignore" vote annotation, and adds userid to it */
  private void addIgnoreVote(String userid) {
    AWBAnnotation ignoreVoteAnnot = dialog.getIgnoreVote();
    // if there is no ignore vote, create one
    if (ignoreVoteAnnot == null) {
      ignoreVoteAnnot = doc.createAnnotation(task.getAnnotationType("VOTE"));
      try {
        ignoreVoteAnnot.setAttributeValue("ignore", Boolean.TRUE);
        ignoreVoteAnnot.setAttributeValue("id", nextCallistoId());
        ignoreVoteAnnot.setAttributeValue("new", "yes");
        HasSubordinates segParent = (HasSubordinates)
          segment.getSuperAnnotation(task.getAnnotationType("SEGMENT"));
        ignoreVoteAnnot.setAttributeValue("segment", segParent);
      } catch (UnmodifiableAttributeException x) {
        /* c'est la vie */
      }
    }
    addVote(ignoreVoteAnnot, userid);
  }

  /** gets the "badBounds" vote annotation, and adds userid to it */
  private void addBadBoundsVote(String userid) {
    AWBAnnotation badBoundsVoteAnnot = dialog.getBadBoundsVote();
    // if there is no badBounds vote, create one
    if (badBoundsVoteAnnot == null) {
      badBoundsVoteAnnot = 
        doc.createAnnotation(task.getAnnotationType("VOTE"));
      try {
        badBoundsVoteAnnot.setAttributeValue("bad_bounds", Boolean.TRUE);
        badBoundsVoteAnnot.setAttributeValue("id", nextCallistoId());
        badBoundsVoteAnnot.setAttributeValue("new", "yes");
        HasSubordinates segParent = (HasSubordinates)
          segment.getSuperAnnotation(task.getAnnotationType("SEGMENT"));
        badBoundsVoteAnnot.setAttributeValue("segment", segParent);
      } catch (UnmodifiableAttributeException x) {
        /* c'est la vie */
      }
    }
    addVote(badBoundsVoteAnnot, userid);
  }

  /** Creates a new annotation in the main document for each
      annotation in the miniJD, and creates a new VOTE annotation
      referencing them all, adds userid as an annotator on that vote.

      RK 7/28/10 Segment is passed in from the Dialog because the value
      here in the AdjDoc seems to be null when this is called (not sure why)
  */
  private void createNewVote(PhraseTaggingAnnotation segment, 
                             JawbDocument miniJD,
                             String userid)  {
    // create the VOTE annotation
    HasSubordinates vote = 
      (HasSubordinates)doc.createAnnotation(task.getAnnotationType("VOTE"));
    try {
      vote.setAttributeValue("id", nextCallistoId());
      vote.setAttributeValue("new", "yes");
    } catch (UnmodifiableAttributeException x) {
      /* c'est la vie */
    }
    if (DEBUG > 1)
      System.err.println("AdjDoc.createNewVote() segment: " + segment);
    HasSubordinates segParent = (HasSubordinates)
      segment.getSuperAnnotation(task.getAnnotationType("SEGMENT"));
    // segParent.addSubordinate(vote);
    try {
      vote.setAttributeValue("segment", segParent);
    } catch (UnmodifiableAttributeException x) {
      /* c'est la vie */
    }

    // copy each annot from the miniJD into main JD and add as
    // subordinate to vote
    for (Iterator iter = miniJD.getAnnotationModel().getAllAnnotations();
         iter.hasNext(); ) {
      TextExtentRegion annot = (TextExtentRegion) iter.next();
      int segStart = segment.getTextExtentStart();
      TextExtentRegion newAnnot = copyAnnotation(annot, doc, task, segStart);
      try {
        newAnnot.setAttributeValue("id", nextCallistoId());
      } catch (UnmodifiableAttributeException x) {
        /* c'est la vie */
      }
      vote.addSubordinate(newAnnot);
    }
    addVote(vote, userid);
  }

  /** copies annot into the given doc, offsetting its start/end
   * anchors by offset, and returns the new annotation.  */
  public static TextExtentRegion
    copyAnnotation(TextExtentRegion annot, JawbDocument doc,
                   Task task, int offset) {
    int start = annot.getTextExtentStart();
    int end = annot.getTextExtentEnd();

    AnnotationType type = annot.getAnnotationType();
    TextExtentRegion newAnnot = 
      (TextExtentRegion)doc.createAnnotation(type);
    String[] attrs = annot.getAttributeKeys();
    for (int i=0; i<attrs.length; i++) {
      String key = attrs[i];
      Class attrType = task.getAttributeType(type, key);
      if ((attrType.equals(Boolean.class) || attrType.equals(String.class)) &&
	  ! key.equals(TextExtentRegion.TEXT_EXTENT) ) {
        try {
          newAnnot.setAttributeValue(key, annot.getAttributeValue(key));
        } catch (UnmodifiableAttributeException x) {
          // should never happen!
        }
      }
    }
    // in the newAnnot, the text extents are offset by +/- segStart since 
    // the start of the segment is at location 0 in the miniJD
    if (DEBUG > 0)
      System.err.println("ADJPanel.copyAn: setting extents to " + 
                         (start + offset) + ", " + (end + offset));
    newAnnot.setTextExtents(start + offset, end + offset);
    return newAnnot;
  }


  /** Removes userid from the list of annotators in the annotator
   * attribute of the voteAnnot.
   * OK, this is a gross way that could get fubar'd by userids that were
   * substring of other userids -- replaced with version below
  private void removeVote(AWBAnnotation voteAnnot, String userid) {
    if (voteAnnot == null)
      return;
    String oldVal= (String)voteAnnot.getAttributeValue("annotator");
    String commaId = ","+userid;
    String idComma = userid+",";
    String newVal = null;
    if (oldVal.indexOf(commaId) >= 0) {
      newVal = oldVal.replace(commaId, "");
    } else if (oldVal.indexOf(idComma) >= 0) {
      newVal = oldVal.replace(idComma, "");
    } else if (oldVal.indexOf(userid) >= 0) {
      newVal = oldVal.replace(userid, "");
    }
    if (newVal != null) {
      try {
        voteAnnot.setAttributeValue("annotator", newVal);
      } catch (Exception x) { 
        // c'est la vie  
      }
    }
  }
  **/

  private void removeVote(AWBAnnotation voteAnnot, String userid) {
    if (voteAnnot == null)
      return;
    String votersString= (String)voteAnnot.getAttributeValue("annotator");
    if (DEBUG > 1)
      System.err.println("AdjDoc.removeVote " + userid + " from " + 
                         votersString);
    String[] voters = votersString.split(",");
    StringBuffer newVoters = new StringBuffer();
    boolean first = true;
    for (int i=0; i<voters.length; i++) {
      if (DEBUG > 3)
        System.err.println("\tcheck: " + voters[i]);
      if (userid.equals(voters[i])) {
        // don't copy this user into the new list but keep going
        // through the loop
        if (DEBUG > 3)
          System.err.println("\t\tskipping " + voters[i]);
        continue;
      }
      // if we got here we have a userid we need to copy
      // if not first, we need a comma before it also
      if (first) {
        first = false;
      } else {
        newVoters.append(",");
      }
      if (DEBUG > 3)
        System.err.println("\t\tappending " + voters[i]);
      newVoters.append(voters[i]);
    }
    if (DEBUG > 0)
      System.err.println("AdjDoc.removeVote " + userid + ": " + 
                         votersString + " -> " + newVoters.toString()); 
    try {
      voteAnnot.setAttributeValue("annotator", newVoters.toString());
    } catch (Exception x) { 
      // c'est la vie  
    }

  }



  private String nextCallistoId() {
    String id = "C"+lastId;
    lastId++;
    return id;
  }

  /** Use an instance of this class for the cancel listener on the
   * adjudication dialog.  When the user closes/cancels the
   * adjudication dialog, do nothing.  User can choose to review or
   * save work so far */
  private class CancelAdjListener implements ActionListener {

    public void actionPerformed (ActionEvent e) {

    }

  }
}
