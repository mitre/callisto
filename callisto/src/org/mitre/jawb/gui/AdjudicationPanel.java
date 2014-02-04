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

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputAdapter;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.Font;
import java.awt.FontMetrics;


import org.mitre.jawb.*;
import org.mitre.jawb.atlas.*;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.ToocaanTask;
import org.mitre.jawb.tasks.Exporter;
import org.mitre.jawb.tasks.DefaultInlineExporter;

import gov.nist.atlas.type.AnnotationType;

/** A panel for allowing voting among different possible sets of
  * annotations for a given segment of the document */

public class AdjudicationPanel extends JPanel implements ActionListener {
  
  // debugging 0==off
  private static int DEBUG = 0;
  
  /** The task */
  private Task task;

  /** The main JawbDocument (for the entire signal) */
  private JawbDocument baseDocument;

  /** The segment-extent annotation for which adjudication needs to be
   * performed */
  private PhraseTaggingAnnotation segment;

  /** The segment parent annotation for which adjudication needs to be
   * performed */
  private SubordinateSetsAnnotation parent;

  /** The userid of the adjudicator */
  private String userid;

  /** for convenience, fetch segment start and end indices once and store */
  private int segStart;
  private int segEnd;

  /** for convenience, fetch text extent content once and store */
  private String miniSignal;

  /** signal file for miniJDs */
  private File mjdSignal;
  private URI mjdSignalURI;

  // JSON is always encoded in UTF-8.  That doesn't necessarily tell
  // me how to encode/decode the minisignals, but I think this is ok
  // anyhow -- TODO check on this!
  private static String encoding = "UTF-8";

  /** a map from a VOTE annotation to the mini JawbDocument for that
   * user for the segment to be adjudicated.  
   */
  private Map<AWBAnnotation,JawbDocument> jdMap;

  /** a map from Vote id to the corresponding VOTE annotation */
  private Map<String,AWBAnnotation> voteAnnotMap;

  /** A map from userid to the VOTE annotation which contains
   *  userid in its list of annotators.
   *  There will also be an entry from "__discard" to a VOTE
   *  with "ignore" set to true, if such a vote exists for this
   *  segment.
   *  This reflects the original state of the segment, before
   *  the user votes.
   */
  private Map<String,AWBAnnotation> userVoteMap;

  /** a map from an annotation in a miniJD to the corresponding original
   * annotation in the main jd 
   */
  //  private Map<PhraseTaggingAnnotation,PhraseTaggingAnnotation> annotMap;

  /** extra miniJD for customization */
  private JawbDocument customMiniJD;

  /** extra JD for context pane */
  private JawbDocument contextJD;
  /** signal file for contextJD */
  private File contextSignal;
  private URI contextSignalURI;
  private String contextSignalText;

  /** The user's vote */
  private String vote;

  /** A list of users who voted to ignore */
  private String ignorers = null;
  /** A list of users who voted for badbounds */
  private String badbounders = null;


  /** The parent dialog */
  AdjudicationDialog dialog;

  /** The adjucication document */
  AdjudicationDocument adjdoc;

  /** an instance of the DefaultInlineExporter used to allow inspection
   *  of a miniJD's annotations */
  private Exporter sgmlExporter;

  public AdjudicationPanel (JawbDocument doc, Task task, 
                            SubordinateSetsAnnotation segmentParent, 
                            String userid, AdjudicationDialog dialog,
                            AdjudicationDocument adjdoc) {
    
    this.baseDocument = doc;
    this.task = task;
    this.dialog = dialog;
    this.adjdoc = adjdoc;

    // Here "segment" is the SEGMENT-EXTENT annotation
    // the SEGMENT parent will be refered to as "parent" or "segmentParent"
    this.segment = 
      (PhraseTaggingAnnotation)segmentParent.getAttributeValue("extent");
    this.parent = segmentParent;
    this.userid = userid;
    segStart = segment.getTextExtentStart();
    segEnd = segment.getTextExtentEnd();
    sgmlExporter = new DefaultInlineExporter(task, "MUC-MI SGML");
    dialog.enableOkButton(false); // ok button starts out as disabled unless
                                  // we select an initial vote

    miniSignal = segment.getTextExtent();
    try {
      // Create temp file.
      mjdSignal = File.createTempFile("mjd", "sgm");

      // Delete temp file when program exits.
      mjdSignal.deleteOnExit();

      // Write the mini-signal to the temp file
      BufferedWriter out = new BufferedWriter(new FileWriter(mjdSignal));
      out.write(miniSignal);
      out.close();
      
      // construct a file: URI for this temp file
      mjdSignalURI = mjdSignal.toURI();
    } catch (IOException e) {
      // TODO do what?
    }

    // create miniJDs
    createMiniJawbDocs();

    // create the context JD
    AWBSimpleSignal signal = (AWBSimpleSignal)segment.getJawb().getSignal();
    int contextStart = Math.max(0, segStart-50);
    try {
      String s = signal.getCharsAt(contextStart, segEnd+50);
      contextSignalText = "..." + s + "...";
    } catch (IndexOutOfBoundsException e) {
      String s = signal.getCharsAt(contextStart);
      contextSignalText = "..." + s;
    }

    // create the signal file for the context JD
    try {
      // Create temp file.
      contextSignal = File.createTempFile("context", "sgm");

      // Delete temp file when program exits.
      contextSignal.deleteOnExit();

      // Write the context signal text to the temp file
      BufferedWriter out = new BufferedWriter(new FileWriter(contextSignal));
      out.write(contextSignalText);
      out.close();
      
      // construct a file: URI for this temp file
      contextSignalURI = contextSignal.toURI();
    } catch (IOException e) {
      // TODO do what?
    }

    // create the context JD
    contextJD = null;
    try {
      contextJD = JawbDocument.fromSignal(contextSignalURI, task, encoding);
      contextJD.setLogAnnotationCreation(false);
    } catch (Exception x) {
      // TODO -- DO SOMETHING!
    }

    // add the segment extent annotation
    AdjudicationDocument.copyAnnotation(segment, contextJD, 
                                        task, 0-contextStart+3);
    


    // fill the panel with text boxes to display the miniJDs
    // and radio buttons to select them.
    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.BOTH;
    c.gridy = 0;

    // add the context in a text pane
    c.gridx = 0;
    add(new JLabel("Context:"),c);
    c.gridx = 1;
    JTextPane contextTextPane = new JTextPane();
    contextTextPane.setStyledDocument (contextJD.getStyledDocument());
    contextTextPane.setEditable(false);
    // constrain the width of the context pane:
    // I want it to be 3 lines, so I need to divide its preferred width
    // by 3 (less a bit) and multiply the preferred height by 3 (plus a bit)
    // and make that the preferred size for that component
    // If there are newlines in the text I want it to be longer but this is 
    // a PITA to compute...
    // Dimension wideSize = contextTextPane.getPreferredSize();
    //double wideW = wideSize.getWidth();
    //double wideH = wideSize.getHeight();
    //double narrowW = Math.ceil(wideW / 2.6);
    //Dimension contextDim = new Dimension();
    //contextDim.setSize(narrowW, wideH*3.1);
    //contextTextPane.setPreferredSize(contextDim);
    // contextTextPane.setSize((int)narrowW,Integer.MAX_VALUE);
    JScrollPane sp = new JScrollPane(contextTextPane);
    // use FontMetrics to decide on the right size for the scrollpane
    FontMetrics fm = contextTextPane.getFontMetrics(contextTextPane.getFont());
    String preString = contextSignalText.substring(0,53);
    // I see no reason why I *should* have to multiply the width by
    // anything to get this to work out right, but apparently I do...
    Dimension scrollDim = new Dimension((int)(fm.stringWidth(preString)*1.8),
                                        (int)(fm.getHeight()*5.5));
    sp.setPreferredSize(scrollDim);
    add(sp, c);
    c.gridy++;
    
    // create a button group for the voting radio buttons
    ButtonGroup group = new ButtonGroup();

    // for each miniJD set constraints and add to the panel
    // add a button to panel and the ButtonGroup
    for (Iterator i = jdMap.keySet().iterator(); i.hasNext(); ) {
      AWBAnnotation v = (AWBAnnotation)i.next();
      JawbDocument jd = jdMap.get(v);
      String annotators = (String) v.getAttributeValue("annotator");
      String id = (String) v.getAttributeValue("id");
      // System.err.println("ADJPanel.constr add row for " + annotators);
      JTextPane textPane = new JTextPane();
      textPane.setStyledDocument (jd.getStyledDocument());
      JTextArea sgmlPane = new JTextArea();
      sgmlPane.setEditable (false);
      sgmlPane.setFocusable (false);
      sgmlPane.setLineWrap (true);
      sgmlPane.setWrapStyleWord (true);
      JRadioButton b = new JRadioButton(annotators);
      b.setActionCommand(id);
      b.addActionListener(this);
      JButton sgmlBut = new JButton("View SGML");
      sgmlBut.setActionCommand(id);
      sgmlBut.addActionListener(new InspectionListener(sgmlPane));
      // TODO be a bit more careful about this -- need to check if
      // userid is in the list as a whole element, not just contained
      // in the string
      if (annotators.contains(userid)) {
        b.setSelected(true);
        // a vote is now selected; enable the OK button in the dialog
        dialog.enableOkButton(true);
        vote = id; // set default vote to id, unless the selection changes
        System.err.println("AdjPanel.constr vote for voteId: " + id);
      }
      // add a mouse listener that will catch mouseClicked events in the
      // textpane and select the associated radio button
      textPane.addMouseListener (new MiniJDListener(b));
      // place the textpane and button into the panel
      c.gridx = 0;
      add(b, c);
      c.gridx = 2;
      add(sgmlBut, c);
      c.gridx = 1;
      add(textPane, c);
      group.add(b);
      // move down a row and add the inspection pane right under the textPane
      c.gridy++;
      add(sgmlPane, c);
      // move down to next row for next button/textpane pair
      c.gridy++;
    }
    // add custom miniJD
    System.err.println("ADJPanel.constr add row for custom");
    // this creates a new copy of the TaskToolKit -- this is desired
    // and necessary, as I need the "core" actions in this mini MTP
    // and not the reconcilation ones
    MainTextPane textPane = new MainTextPane(task.getToolKit());
    textPane.setAnnotationInspectorVisible(false);
    textPane.setJawbDocument (customMiniJD);
    JRadioButton b = new JRadioButton("custom");
    b.setActionCommand("__custom");
    b.addActionListener(this);
    // add a mouse listener that will catch mouseClicked events in the
    // textpane and select the associated radio button
    // textPane.addMouseListener (new MiniJDListener(b));
    // here a mouse listener will not work because MTP seems to consume 
    // the events (?)
    textPane.getJawbDocument().getAnnotationModel ()
      .addAnnotationModelListener (new CMJDModelListener(b));
    c.gridx = 0;
    add(b, c);
    c.gridx = 1;
    add(textPane, c);
    group.add(b);
    c.gridy++;
    // add vote for empty region if anyone has already voted for this
    // else I think users can just use the custom miniJD to register such
    // a vote
    // RK 5/4/2010 there should already be a miniJD for empty automatically
    // if there was a vote with no content subordinates.

    // add vote to discard
    String ignoreString = "ignore this segment" + 
      (ignorers == null?"":" ("+ignorers+")");
    JRadioButton b2 = new JRadioButton(ignoreString);
    b2.setActionCommand("__discard");
    b2.addActionListener(this);
    c.gridx = 0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor    = GridBagConstraints.WEST;
    add(b2, c);
    group.add(b2);
    c.gridy++;

    // add vote for bad boundaries
    String badBoundsString = "segment boundaries prevent correct annotation" +
      (badbounders == null?"":" ("+badbounders+")");
    JRadioButton b3 = new JRadioButton(badBoundsString);
    b3.setActionCommand("__badbounds");
    b3.addActionListener(this);
    c.gridx = 0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.anchor    = GridBagConstraints.WEST;
    add(b3, c);
    group.add(b3);

  }

  public void actionPerformed(ActionEvent e) {
    vote = e.getActionCommand();
    // a vote is now selected so enable the OK button in the containing dialog
    dialog.enableOkButton(true);
    System.err.println("AdjPanel.ap vote for: " + vote);
  }

  private void createMiniJawbDocs () {
    
    // map from VOTE to miniJD
    jdMap = new HashMap();
    // map from VOTE id to VOTE
    voteAnnotMap = new HashMap();
    // map from userID to VOTE
    userVoteMap = new HashMap();

    Set<AWBAnnotation> votes = adjdoc.getVotes(parent);
    if (DEBUG > 0)
      System.err.println("AdjPanel.createMiniJDs got " + votes.size() + 
                         " votes.");

    for (Iterator i = votes.iterator(); i.hasNext(); ) {
      // don't call the annotation "vote" here because "vote" is defined
      // globally for the user's choice
      SubordinateSetsAnnotation v = (SubordinateSetsAnnotation)i.next();
      
      // if this is a special vote, just attribute it to all listed annotators
      // and return -- no miniJD is needed
      Boolean ignore = (Boolean) v.getAttributeValue("ignore");
      String annotators = (String) v.getAttributeValue("annotator");
      Boolean badBounds = (Boolean) v.getAttributeValue("bad_bounds");

      if (ignore.booleanValue()) {
        userVoteMap.put("__discard", v);
        mapUserVotes(annotators, v);
        ignorers = annotators;
        continue;
      }
      if (badBounds.booleanValue()) {
        userVoteMap.put("__badbounds", v);
        mapUserVotes(annotators, v);
        badbounders = annotators;
        continue;
      }

      JawbDocument miniJD = null;
      try {
        miniJD = JawbDocument.fromSignal(mjdSignalURI, task, encoding);
        miniJD.setLogAnnotationCreation(false);

      } catch (Exception x) {
        // TODO -- DO SOMETHING!
      }
      jdMap.put(v, miniJD);
      String id = (String) v.getAttributeValue("id");
      voteAnnotMap.put(id, v);
      miniJD.putClientProperty(ToocaanTask.RECON_KEY, id);
      mapUserVotes(annotators, v);
      AWBAnnotation[] annots = 
        v.getSubordinates(task.getAnnotationType("ENTITY"));

      for (int j=0; j<annots.length; j++) {
        TextExtentRegion annot = (TextExtentRegion) annots[j];
        TextExtentRegion newAnnot = 
          AdjudicationDocument.copyAnnotation(annot, miniJD, task, 0-segStart);
      }
    }

    // create custom miniJD with no annots
    try {
      customMiniJD = JawbDocument.fromSignal(mjdSignalURI, task, encoding);
      // oops yeah, for the custom one we DO want to log annotation creations
      // since we don't create any when initializing it, and we do want to 
      // know when the user adds them
      // customMiniJD.setLogAnnotationCreation(false);
    } catch (Exception e) {
      // TODO do what??
    }
    customMiniJD.putClientProperty(ToocaanTask.RECON_KEY, "custom");

  }

  /**
  private PhraseTaggingAnnotation copyAnnotation(PhraseTaggingAnnotation annot,
                                                 JawbDocument doc) {
    int start = annot.getTextExtentStart();
    int end = annot.getTextExtentEnd();

    AnnotationType type = annot.getAnnotationType();
    PhraseTaggingAnnotation newAnnot = 
      (PhraseTaggingAnnotation)doc.createAnnotation(type);
    String[] attrs = annot.getAttributeKeys();
    for (int i=0; i<attrs.length; i++) {
      String key = attrs[i];
      Class attrType = task.getAttributeType(type, key);
      if (attrType.equals(String.class) &&
	  ! key.equals(TextExtentRegion.TEXT_EXTENT) ) {
        try {
          newAnnot.setAttributeValue(key, annot.getAttributeValue(key));
        } catch (UnmodifiableAttributeException x) {
          // should never happen!
        }
      }
    }
    // in the newAnnot, the text extents are offset by -segStart since 
    // the start of the segment is at location 0 in the miniJD
    if (DEBUG > 0)
      System.err.println("ADJPanel.copyAn: setting extents to " + 
                         (start - segStart) + ", " + (end - segStart));
    newAnnot.setTextExtents(start - segStart, end - segStart);
    return newAnnot;
  }
  **/

  private void mapUserVotes(String annotators, AWBAnnotation v) {
    String[] userList = annotators.split(",");
    for (int i=0; i<userList.length; i++) {
      userVoteMap.put(userList[i],v);
    }
  }

  public String getVote() {
    return vote;
  }

  public AWBAnnotation getVoteAnnot(String userid) {
    if (userVoteMap == null)
      return null;
    return userVoteMap.get(userid);
  }

  public AWBAnnotation getIgnoreVote() {
    if (userVoteMap == null)
      return null;
    return userVoteMap.get("__discard");
  }

  public AWBAnnotation getBadBoundsVote() {
    if (userVoteMap == null)
      return null;
    return userVoteMap.get("__badbounds");
  }

  public AWBAnnotation getVoteFromId(String voteId) {
    if (voteAnnotMap == null)
      return null;
    return voteAnnotMap.get(voteId);
  }

  public AWBAnnotation getSegment() {
    return parent;
  }

  public PhraseTaggingAnnotation getSegmentExtent() {
    return segment;
  }


  /** returns the miniJD corresponding to a given VOTE id */
  public JawbDocument getMiniJD(String id) {
    if (id.equals("__custom"))
      return customMiniJD;
    if (jdMap == null || voteAnnotMap == null)
      return null;
    AWBAnnotation voteAnnot = (AWBAnnotation)voteAnnotMap.get(id);
    if (voteAnnot == null)
      return null;
    return jdMap.get(voteAnnot);
  }

  /**
  public PhraseTaggingAnnotation 
    getMainAnnot(PhraseTaggingAnnotation miniJDAnnot) {
    if (annotMap == null)
      return null;
    return annotMap.get(miniJDAnnot);
    }*/


  private class InspectionListener implements ActionListener {

    JTextArea pane;

    public InspectionListener (JTextArea pane) {
      this.pane = pane;
    }

    public void actionPerformed(ActionEvent e) {
      String id = e.getActionCommand();
      JButton b = (JButton)e.getSource();
      if (id.startsWith("__hide:")) {
        b.setText("Show SGML");
        b.setActionCommand(id.substring(7));
        pane.setText("");
      } else {
        b.setText("Hide SGML");
        b.setActionCommand("__hide:".concat(id));
        
        // get the miniJD for this vote id
        JawbDocument miniJD = jdMap.get(voteAnnotMap.get(id));
        StringBuffer buf = new StringBuffer();
        try {
          // create a temp URI
          File temp = File.createTempFile("CAL", null);
          // export the miniJD into the temp URI
          miniJD.export(sgmlExporter, temp.toURI());
          // read the contents of the URI
          FileReader fr = new FileReader(temp);
          BufferedReader in = new BufferedReader(fr);
          String line;
          while ((line = in.readLine()) != null) {
            buf.append(line);
          }
        } catch (IOException x1) {
          // TODO do what?
        }

        // place the read in text into the text pane
        pane.setText(buf.toString());
      }
      // repack the containing dialog to fit 
      dialog.pack();
    }
  }

  private class MiniJDListener extends MouseInputAdapter {

    private JRadioButton theButton;

    public MiniJDListener(JRadioButton b) {
      super();
      this.theButton = b;
    }

    public void mouseClicked(MouseEvent e) {
      System.err.println("MiniJDListener.mouseClicked");
      theButton.setSelected(true);
      vote = theButton.getActionCommand();
    }

    public void mousePressed(MouseEvent e) {
      System.err.println("MiniJDListener.mousePressed");
      theButton.setSelected(true);
      vote = theButton.getActionCommand();
    }
  }

  /** Custom Mini-JawbDocument Annotation Model Listener
   *  selects the provided radio button when an annotation is created
   *  in the miniJD to which the listener is attached
   */
  private class CMJDModelListener implements AnnotationModelListener {
  
    private JRadioButton theButton;

    public CMJDModelListener(JRadioButton b) {
      super();
      this.theButton = b;
    }

  /** Invoked after an annotation has been created. */
    public void annotationCreated (AnnotationModelEvent e) {
      System.err.println("CMJDModelListener: annot created");
      theButton.setSelected(true);
      // todo would it be better to create an ActionEvent and call
      // fireActionPerformed? 
      vote = theButton.getActionCommand();
    }

  /** Invoked after an annotation has been deleted. */
    public void annotationDeleted (AnnotationModelEvent e) {}
  
  /** Invoked after an annotation has been changed. */
    public void annotationChanged (AnnotationModelEvent e) {}
  
  /** Invoked after an annotation has had subannotations added. */
    public void annotationInserted (AnnotationModelEvent e) {}
  
  /** Invoked after an annotation has had subannotations removed. */
    public void annotationRemoved (AnnotationModelEvent e) {}
}

}
