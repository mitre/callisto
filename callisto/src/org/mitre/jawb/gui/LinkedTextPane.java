
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

import org.mitre.jawb.tasks.TaskToolKit;
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.AnnotationModelEvent;
import org.mitre.jawb.swing.LinkedHashSetModel;

import gov.nist.atlas.type.AnnotationType;
import gov.nist.atlas.Annotation;
import gov.nist.atlas.Region;

import javax.swing.JTextPane;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.text.BadLocationException;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Collections;

/**
 * Panel which Wraps a JTextPanel and acts as a view on a
 * JawbDocument.<p> 
 *
 * This subclass of MainTextPane supports
 * creating/showing links between text tokens.
 *
 * TODO: UndoableEditListener, 

 * TODO: There is a bug where opening and closing document multiple
 * times duplicates link arrows.  This is probably due to storing
 * things in instance variables here that should be stored per
 * JawbDocument or something like that.
 *

 * @author Robyn Kozierok <robyn@mitre.org>
 */
public class LinkedTextPane extends MainTextPane {

  private static int DEBUG = 0;
  
  // adjustment distance so arrows don't collide
  private static int ADJUST_RIGHT = 4;
  private static int ADJUST_LEFT = -4;
  // todo -- could make this proportional to the text size, etc...
  // this is really half the width of the arrowhead
  private static int ARROW_SIZE = 4;


  private JLayeredPane layeredPane;
  private JTextPane mainTextPane;

  // subclass SpecialTextPane in GrammarLinks tasks accesses this
  protected List arrows;
  /** Link tags, tracked separately from the Documents selectionModel */
  private LinkedHashSetModel selectedLinkAnnots = new LinkedHashSetModel ();

  private List linkAnnotTypes;
  // These hashtables map from annotation type to attribute names
  private Map fromAttr;
  private Map toAttr;
  private Map labelAttr;
  // This hashtable maps from an annot type to a set of label Strings
  // that correspond to annotations that belong below the text
  private Map belowLinks;
  // This hashtable maps from an annot to the LinkArrow object
  // associated with it
  private Map linkForAnnot;

  private LinkAnnotListener linkListener;
  /** Observes mouse events on Annotations */
  private AnnotationMouseListener linkAnnotMouseListener = null;

  
  public static Integer TEXT_LAYER  = new Integer(0);
  public static Integer ARROW_LAYER = new Integer(1);
  public static Integer LABEL_LAYER = new Integer(2);

  /**
   * Create a text panel which can display and edit a JawbDocument and
   * associated links between words or other text tokens.
   */
  public LinkedTextPane (TaskToolKit kit) {
    super(kit);
    mainTextPane = super.getTextPane();
    layeredPane.setBackground(Color.WHITE);
    layeredPane.add(mainTextPane, TEXT_LAYER);

    linkListener = new LinkAnnotListener();
    linkAnnotMouseListener = new LinkAnnotMouseListener();
    arrows = new ArrayList();
    linkAnnotTypes = new ArrayList();
    fromAttr = new HashMap();
    toAttr = new HashMap();
    labelAttr = new HashMap();
    linkForAnnot = new HashMap();
    belowLinks = new HashMap();
  }

  protected Component getTextView() {
    if (layeredPane == null) {
      layeredPane = new LinkLayeredPane();
      if (DEBUG > 1) 
	System.err.println("LTP.getTextView created layeredPane");
    }      
    return layeredPane;
  }


  /**
   * 
   * Declares a specific annotation type as an annotation to be
   * visualized with a LinkArrow.  The caller must provide the
   * AnnotationType along with the names of attributes that specify
   * the Annotations to link from and to, and the text to be displayed
   * on the link. 
   *
   * An AnnotationModelListener will listen for creation of such an
   * Annotation, and will display it in the text widget by drawing a
   * link between the two named subordinates.  The link will be
   * labelled according to the typeAttr attribute.
   *
   * @param fromAttr is the String name of the named subordinate from
   * which the link should begin.
   * @param toAttr is the String name of the named subrodinate to
   * which the link should be made.
   * @param typeAttr is the String name of the content attribute whose
   * value is the label to place on the link.  If no label is
   * required, this may be null.
   */

  public void addLinkAnnotationType (AnnotationType type,
				     String fromAttr, String toAttr,
				     String typeAttr) {
    linkAnnotTypes.add(type);
    this.fromAttr.put(type, fromAttr);
    this.toAttr.put(type, toAttr);
    labelAttr.put(type,typeAttr);
  }

  /**
   * registers a certain annotation type and link type combination as
   * belonging below the text.
   *
   * @param annotType is the type of annotation for which some link
   * types go below the text.
   * @param linkTypes is a Set of Strings enumerating the link types
   * that go below the line for ths annotation type.
   */
  public void typesLinkBelow (AnnotationType annotType,
			      Set linkTypes) {
    belowLinks.put(annotType, linkTypes);
    
  }

  public Set getSelectedAnnots () {
    Set allAnnots = new HashSet (selectedLinkAnnots);
    allAnnots.addAll(super.getSelectedAnnots());
    return Collections.unmodifiableSet (allAnnots);
  }

  protected AnnotationMouseListener getAnnotMouseListener() {
    return linkAnnotMouseListener;
  }


  /**
   * 
   * Displays an annotation with two named subordinates in the text
   * widget by drawing a link between two subordinates.
   *
   * @param fromAttr is the String name of the named subordinate from
   * which the link should begin.
   * @param toAttr is the String name of the named subrodinate to
   * which the link should be made.
   * @param typeAttr is the String name of the content attribute whose
   * value is the label to place on the link.  If no label is
   * required, this may be null.
   */

  private boolean addLinkArrow (AWBAnnotation annot, 
				String fromAttr, String toAttr, 
				String typeAttr) {

    if (DEBUG > 1)
      System.err.println("LTP.addLinkArrow fromAttr: " + fromAttr +
			 "toAttr: " + toAttr);
    AnnotationType type = annot.getAnnotationType();
    String label = (String)annot.getAttributeValue(typeAttr);
    AWBAnnotation from = (AWBAnnotation)annot.getAttributeValue(fromAttr);
    AWBAnnotation to = (AWBAnnotation)annot.getAttributeValue(toAttr);
    boolean below;
    if (belowLinks.containsKey(type)) {
      Set linkBelowLabels = (Set)belowLinks.get(type);
      below = linkBelowLabels.contains(label);
    } else {
      below = false;
    }
    
    /*
     * OK, there's a little problem here.  I don't want to create the
     * link until the label has been specified.  But because ATLAS is
     * stupid and sets string attributes to empty strings instead of
     * null when they are not set, I can't tell if the user has set it
     * to an empty string, or just hasn't set it yet.  argh!  So the
     * HACK to get around this is to be sure to set the label
     * attribute in the annotation before setting the from and to
     * attributes (in SubordinateAssignor).  That way, if from and to
     * are non-null, I'll know that label is what it was intended by
     * the user to be. 
     */
    if (from == null || to == null || label == null) {
      if (DEBUG > 1 )
	System.err.println("LTP.addLinkArrow failed -- annotation incomplete");
      return false;
    }

    if (DEBUG > 2)
      System.err.println ("LTP.addLinkArrow: label is non null: <<" +
			  label + ">>");

    LinkArrow arrow = new LinkArrow(this, annot, label, from, to, below);

    layeredPane.add(arrow, ARROW_LAYER);
    arrows.add(arrow);
    linkForAnnot.put(annot, arrow);
    layeredPane.add(arrow.getLabel(), LABEL_LAYER);
    return true;
  }

  /**
   * 
   * Updates the content of a LinkArrow according to the recently
   * changed contents of the annotation passed in. 
   * 
   * @param fromAttr is the String name of the named subordinate from
   * which the link should begin.
   * @param toAttr is the String name of the named subrodinate to
   * which the link should be made.
   * @param typeAttr is the String name of the content attribute whose
   * value is the label to place on the link.  If no label is
   * required, this may be null.
   */

  private void updateLinkArrow (AWBAnnotation annot, 
				   String fromAttr, String toAttr, 
				   String typeAttr) {

    if (DEBUG > 1)
      System.err.println("LTP.updateLinkArrow fromAttr: " + fromAttr +
			 "toAttr: " + toAttr);
    String label = (String)annot.getAttributeValue(typeAttr);
    
    LinkArrow arrow = (LinkArrow)linkForAnnot.get(annot);
    if (!arrow.getLabel().equals(label)) {
      AnnotationType type = annot.getAnnotationType();
      boolean below;
      if (belowLinks.containsKey(type)) {
	Set linkBelowLabels = (Set)belowLinks.get(type);
	below = linkBelowLabels.contains(label);
      } else {
	below = false;
      }
      arrow.setLabel(label, below);
      // re-layout the arrows in case the label change changes the position
      //((LinkLayeredPane)layeredPane).layoutArrows();
    }

    if ((arrow.getFromAnnot() != 
	 (AWBAnnotation)annot.getAttributeValue(fromAttr)) ||
	(arrow.getToAnnot()  !=
	 (AWBAnnotation)annot.getAttributeValue(toAttr))) {
      arrow.updateAnnots(fromAttr, toAttr);
    }
    
    revalidate();
  }

  /** takes a linkAnnotation (that is about to be deleted,
   * presumably) and removes the linkArrow for it 
   */
  private boolean removeLinkArrow (AWBAnnotation annot) {
    LinkArrow arrow = (LinkArrow)linkForAnnot.get(annot);
    if (DEBUG > 1)
      System.err.println("LTP:removeLinkArrow for annot: " + annot +
			 " with " + (arrow == null?"no ":"") + "arrow");
    if (arrow != null) {
      if (DEBUG > 2)
	System.err.println("LTP.removeLinkArrow: " + arrow);
      layeredPane.remove(arrow);
      layeredPane.remove(arrow.getLabel());
      arrows.remove(arrows.indexOf(arrow));
      linkForAnnot.remove(annot);
      selectedLinkAnnots.remove(annot);
      return true;
    }
    return false;
  }

	

  public void setJawbDocument (JawbDocument doc) {
    JawbDocument oldDoc = getJawbDocument();
    if (oldDoc != null)
      oldDoc.getAnnotationModel().removeAnnotationModelListener(linkListener);

    super.setJawbDocument(doc);

    if (doc != null) {
      doc.getAnnotationModel().addAnnotationModelListener(linkListener);
      Iterator annotIter = doc.getAnnotationModel().getAllAnnotations();
      while (annotIter.hasNext()) {
	  AWBAnnotation annot = (AWBAnnotation)annotIter.next();
	  AnnotationType type = annot.getAnnotationType();
	  if (linkAnnotTypes.contains(type)) {
	      addLinkArrow(annot, 
			   (String)fromAttr.get(type),
			   (String)toAttr.get(type), 
			   (String)labelAttr.get(type));
	  }
      }
	  
    }
  } 


  // here to be overridden by subclasses
  protected int getArrowOffset (int offset) {
    if (DEBUG > 2)
      System.err.println("LTP.getArrowOffset called");
    return offset;
  }

  public class LinkLayeredPane extends JLayeredPane {
    public LinkedTextPane getLTP() {
      return LinkedTextPane.this;
    }

    public void doLayout() {
      super.doLayout();
      System.err.println("JLayeredPane.doLayout: this.size is "
			 + this.getSize());
      layoutText();
      System.err.println("JLayeredPane.doLayout: after layoutText this.size is "
			 + this.getSize());
      layoutArrows();
    }


    private void layoutText() {
      Dimension mtpd = mainTextPane.getPreferredSize();
      Dimension scrolld = getViewSize();
      int width = Math.max(mtpd.width, scrolld.width);
      int height = Math.max(mtpd.height, scrolld.height);
      if (DEBUG > 1) {
	System.err.println("LTP.layoutText: mtp preferred size: " +
			   mtpd);
	System.err.println("LTP.layoutText: mtp actual size: " +
			   mainTextPane.getSize());
      }
      mainTextPane.setBounds(0, 0, width, height);
      this.setPreferredSize(new Dimension(width, height));
      if (DEBUG > 2) {
	System.err.println("LTP.layoutText: after setBounds mtp actual size: " +
			   mainTextPane.getSize());
	System.err.println("LTP.layoutText: after setSize layeredPane size: " +
			   layeredPane.getSize());
      }
    }


    protected void layoutArrows() {
    

      if (DEBUG > 1)
	System.err.println("LTP.layoutArrows with " +
			   arrows.size() + " arrows");
      if (DEBUG > 5)
	Thread.dumpStack();

      for (int i = 0; i<arrows.size(); i++) {
	LinkArrow arrow = (LinkArrow)arrows.get(i);

	if (DEBUG > 2) {
	  System.err.println("LTP.layoutArrows for : " + arrow +
			     " This is " + this);
	}
      
	int fromStartOffset = getArrowOffset(arrow.getFromStart());
	int fromEndOffset = getArrowOffset(arrow.getFromEnd());
	int toStartOffset = getArrowOffset(arrow.getToStart());
	int toEndOffset = getArrowOffset(arrow.getToEnd());

	layoutArrow (arrow, fromStartOffset, fromEndOffset,
		     toStartOffset, toEndOffset);
      }
    }



    protected void layoutArrow (LinkArrow arrow,
				int fromStartOffset, int fromEndOffset,
				int toStartOffset,   int toEndOffset) {

      
      if (DEBUG > 2) 
	System.err.println("LTP.layoutArrow: fs: " + fromStartOffset +
			   " ts: " + toStartOffset +
			   " fe: " + fromEndOffset +
			   " te: " + toEndOffset);
      int length = 0;
      int height = 0;
      int offsetLength = arrow.getCenteredOffsetLength ();

      int fontHeight = getJawbDocument().getFontSize();
      //int minHeight = (3 * fontHeight) / 2;
      int minHeight = 2 * fontHeight;

      // this is wrong, but there's not a good way of getting at the
      // font metrics from here, and it really doesn't have to be
      // perfect, since it is only used in a rare case anyhow....
      int charWidth = fontHeight / 2;



      // TODO -- make this depend on nesting!!
      //int height = getJawbDocument().getLineSpacing();
      //int height = 3*fontHeight;

      int xloc = 0;
      int yloc = 0;
      int xend = 0;
      int textHeight = 0;

      
      try {
	// the endOffset is actually the character after the chunk, so
	// make sure it is not somewhere stupid, like on the next
	// line.  (If it is, use the offset of the previous character
	// plus <uh, something>.)  This will handle when there
	// is a newline right after the word, but not if there is one
	// in the xg tag.
	Rectangle fs = mainTextPane.modelToView(fromStartOffset);
	Rectangle fe = mainTextPane.modelToView(fromEndOffset);
	if (fs.y != fe.y) {
	  fe = mainTextPane.modelToView(fromEndOffset-1);
	  fe.x += charWidth;
	}
	Rectangle ts = mainTextPane.modelToView(toStartOffset);
	Rectangle te = mainTextPane.modelToView(toEndOffset);
	if (ts.y != te.y) {
	  te = mainTextPane.modelToView(toEndOffset-1);
	  te.x += charWidth;
	}
	if (DEBUG > 4) 
	  System.err.println("LTP.layoutArrow mainTextPane:" + mainTextPane +
			     "\n\tfs: " + fromStartOffset + " -> " + fs +
			     "\n\tfe: " + fromEndOffset   + " -> " + fe +
			     "\n\tts: " + toStartOffset + " -> " + ts +
			     "\n\tte: " + toEndOffset   + " -> " + te);
	int midxfr = fs.x + ((fe.x - fs.x) / 2);
	int midxto = ts.x + ((te.x - ts.x) / 2);
	if (DEBUG > 2)
	  System.err.println("LTP.layoutArrow: midxfr = " + midxfr +
			     " midxto = " + midxto);
	AWBAnnotation fromAnnot = arrow.getFromAnnot();
	AWBAnnotation toAnnot = arrow.getToAnnot();
	if (midxfr < midxto) {
	  xloc = adjustLeftPoint(arrow, midxfr, fe.x-1, fromAnnot);
	  xend = adjustRightPoint(arrow, midxto, ts.x, toAnnot);
	  height = computeHeight (arrow, fromAnnot, toAnnot, offsetLength,
				  fromStartOffset, toEndOffset,
				  1, minHeight, fontHeight);
	} else {
	  xloc = adjustLeftPoint(arrow, midxto, te.x-1, toAnnot);
	  xend = adjustRightPoint(arrow, midxfr, fs.x, fromAnnot);
	  height = computeHeight (arrow, fromAnnot, toAnnot, offsetLength,
				  toStartOffset, fromEndOffset,
				  -1, minHeight, fontHeight);
	}
	if (DEBUG > 2)
	  System.err.println("LTP.layoutArrow: xloc = " + xloc +
			     " xend = " + xend);
	length = xend - xloc;
	//height = Math.max(minHeight,length/2);
	if (arrow.isBelow()) {
	  yloc = fs.y + fontHeight + 8;
	} else {
	  yloc = fs.y - height - 2;
	}
      } catch (BadLocationException e) {
	System.err.println("LinkTextPane.layoutArrows: " + e.getMessage());
	return;
      }
      // TODO arrow-size parameter is a static int -- make dependent
      // on overall size
      if (fromStartOffset < toStartOffset)
	arrow.setBounds(xloc, yloc, length+ARROW_SIZE, height);
      else 
	arrow.setBounds(xloc-ARROW_SIZE, yloc, length+ARROW_SIZE, height);
      JLabel label = arrow.getLabel();
      Dimension labelDim = label.getPreferredSize();
      int labelStart = xloc + ((length - labelDim.width) / 2);
      if (DEBUG > 2)
	System.err.println("LTP.layoutArrow labelStart = " + labelStart +
			   " width = " + labelDim.width);
      if (arrow.isBelow()) {
	label.setBounds(labelStart, yloc+height-labelDim.height,
			labelDim.width, labelDim.height);
      } else {
	label.setBounds(labelStart, yloc, labelDim.width, labelDim.height);
      }
    }

    /**
     * Adjusts the leftmost point of a link arrow toward the right in
     * an amount that depends upon how many longer arrows also enter
     * the leftmost subAnnotation.  Returns an x coordinate that is an
     * appropriately adjusted version of the midpoint passed in.
     */
    private int adjustLeftPoint(LinkArrow arrow, int mid, int max,
				AWBAnnotation sub) {
      return adjustPoint(arrow, mid, max, sub, ADJUST_RIGHT);
    }

    /**
     * Adjusts the rightmost point of a link arrow toward the left in
     * an amount that depends upon how many longer arrows also enter
     * the leftmost subAnnotation.  Returns an x coordinate that is an
     * appropriately adjusted version of the midpoint passed in.
     */
    private int adjustRightPoint(LinkArrow arrow, int mid, int min, 
				 AWBAnnotation sub) {
      return adjustPoint(arrow, mid, min, sub, ADJUST_LEFT);
    }

    private int adjustPoint (LinkArrow arrow, int mid, int minmax,
			     AWBAnnotation sub, int adjust) {
      int centeredLength = arrow.getCenteredOffsetLength();
      int numLonger = 0;
      boolean isBelow = arrow.isBelow();
      Iterator linkIter = sub.getReferentElements().iterator();
      while (linkIter.hasNext()) {
	Object e = linkIter.next();
	if (e instanceof Region) {
	  Iterator link2Iter = ((Region)e).getReferentElements().iterator();
	  while (link2Iter.hasNext()) {
	    Object e2 = link2Iter.next();
	    if (e2 instanceof AWBAnnotation &&
		linkAnnotTypes.contains(((Annotation)e2).getAnnotationType())) {
	      LinkArrow otherArrow = (LinkArrow)linkForAnnot.get(e2);
	      if (isBelow == otherArrow.isBelow()) {
		if (otherArrow.getCenteredOffsetLength() > centeredLength) {
		  numLonger++;
		}
	      }
	    }
	  }
	}
      }
      int newMid = (int) Math.round(mid + (numLonger + 0.5) * adjust);
      if (adjust > 0)
	newMid = Math.min(minmax, newMid);
      else 
	newMid = Math.max(minmax, newMid);

      return newMid;
    } 
  }


  /**
   *
   * Computes the appropriate height of the link arrow based on how
   * many arrows it overlaps or includes within its bounds that are
   * smaller than it.  If two arrows coincide, the one whose label has
   * the smaller sort order will be considered to be smaller than the
   * other.  If the labels are the same, the one going from left to
   * right will be considered larger than the one going from right to
   * left.
   *
   * Assumes that there is one analysis that contains all the link
   * type annotations.
   *
   * @param direction is +1 for left-to-right, and -1 for right-to-left
   */

  private int computeHeight (LinkArrow arrow, AWBAnnotation from,
			     AWBAnnotation to, int offsetLength,
			     int minOffset, int maxOffset,
			     int direction, int minHeight, int incrHeight) {
    if (DEBUG > 4)
      System.err.println ("LTP.computeHeight: " + arrow.toString() + "...");  
    int numSmaller = 0;
    String label = arrow.getLabel().getText();
    Iterator linkIter = 
      //arrow.getAnnot().getDefiningCorpus().getAllAnnotations().iterator();
      getOverlappingAnnots(minOffset, maxOffset).iterator();
    while (linkIter.hasNext()) {
      AWBAnnotation nextLink = (AWBAnnotation) linkIter.next();
      LinkArrow nextArrow = (LinkArrow)linkForAnnot.get(nextLink);
      if (DEBUG > 4)
	System.err.println ("...compare to " +
			    (nextArrow==null?"null":nextArrow.toString()));
      if (nextArrow != null && arrow.overlaps(nextArrow)) {
	int nextLength = nextArrow.getCenteredOffsetLength();
	if (nextLength < offsetLength) {
	  numSmaller++;
	} else if (nextLength == offsetLength) {
	  String nextLabel = nextArrow.getLabel().getText();
	  int labelCompare = nextLabel.compareTo(label);
	  if (labelCompare < 0) {
	    numSmaller++;
	  } else if (labelCompare == 0 && direction == 1 && 
		     nextArrow.getFromStart() > nextArrow.getToStart()) {
	    // labels are the same but 
	    // arrow is left-to-right and nextArrow is right-to-left
	    numSmaller++;
	  }
	}
      }
    }
    return minHeight + numSmaller * incrHeight; 
  }

  private Set getOverlappingAnnots (int minOffset, int maxOffset) {
    JawbDocument doc = getJawbDocument();
    Set xgs = new LinkedHashSet();
    Set links = new LinkedHashSet();

    if (DEBUG > 1)
      System.err.println("LTP.getOverlappingAnnots from " + minOffset +
			 " to " + maxOffset);
    
    for (int i=minOffset; i<=maxOffset; i++) {

      if (DEBUG > 3) {
	Iterator annotsAtiIter = doc.getAnnotationsAt(i).iterator();
	System.err.println ("LTP.getOverlap: Annots at " + i + ":");
	while (annotsAtiIter.hasNext())
	  System.err.println("\t" + annotsAtiIter.next().toString());
      }
       
      xgs.addAll(doc.getAnnotationsAt(i));
    }
    
    if (DEBUG > 3) 
      System.err.println("LTP.getOverlap: Links into overlapping XGs:");
    Iterator xgIter = xgs.iterator();
    while (xgIter.hasNext()) {
      Iterator linkIter = 
	((AWBAnnotation)xgIter.next()).getReferentElements().iterator();
      while (linkIter.hasNext()) {
	Object e = linkIter.next();
	if (e instanceof Region) {
	  Iterator link2Iter = ((Region)e).getReferentElements().iterator();
	  while (link2Iter.hasNext()) {
	    Object e2 = link2Iter.next();
	    if (e2 instanceof AWBAnnotation &&
		linkAnnotTypes.contains(((Annotation)e2).getAnnotationType())) {
	      if (DEBUG > 3)
		System.err.println("\t" + e2.toString());
	      links.add(e2);
	    }
	  }
	}
      }
    }

    return links;
  }    

  private class LinkAnnotMouseListener extends TextAnnotationMouseListener {

    public void mouseClicked (AnnotationMouseEvent e) {
      MouseEvent me = e.getMouseEvent();
      if (!me.isConsumed()) {
	AWBAnnotation annot = e.getAnnotation();
	AnnotationType type = annot.getAnnotationType();
	// if this was not a shift click, unselect and unhighlight all
	// link annotations (other annotations will be handled by the
	// parent method)
	if ((me.getModifiers() & ActionEvent.SHIFT_MASK) == 0) {
	  Iterator iter = selectedLinkAnnots.iterator();
	  while (iter.hasNext()) {
	    ((LinkArrow)linkForAnnot.get((AWBAnnotation)iter.next())).
	      setHighlight(false);
	  }
	  selectedLinkAnnots.clear();
	}
	// if it was a link annot that was clicked on, select and
	// highlight it
	if (linkAnnotTypes.contains(type)) {
	  if (DEBUG > 1) 
	    System.err.println("LTP.annotMClicked: selecting link annot: " +
			       annot);
	  selectedLinkAnnots.add(annot);
	  ((LinkArrow)linkForAnnot.get(annot)).setHighlight(true);
	  
	} 
	// parent method handles non-link annotations, changes to the
	// doucments annotation selection model, etc.
	super.mouseClicked(e);
      }
    }
  }
	  

  private class LinkAnnotListener extends AnnotModelListener {
    
    public void annotationCreated (AnnotationModelEvent e) {
      super.annotationCreated(e);
      // if it's a link, try to draw an arrow for it
      AWBAnnotation annot = e.getAnnotation();
      AnnotationType type = annot.getAnnotationType();
      if (linkAnnotTypes.contains(type)) {
	if (DEBUG > 1)
	  System.err.println("LTP.annotationCreated: " + annot +
			     "link type " + type);
	addLinkArrow(annot, 
		     (String)fromAttr.get(type),
		     (String)toAttr.get(type), 
		     (String)labelAttr.get(type));
      }
    }

    public void annotationDeleted (AnnotationModelEvent e) {
      super.annotationDeleted(e);
      // if it's a link, remove the arrow for it
      AWBAnnotation annot = e.getAnnotation();
      AnnotationType type = annot.getAnnotationType();
      if (linkAnnotTypes.contains(type)) {
	removeLinkArrow(annot);
      }
    }
	

    public void annotationChanged (AnnotationModelEvent e) {
      super.annotationChanged(e);
      // if it's a link, try to draw an arrow for it
      AWBAnnotation annot = e.getAnnotation();
      AnnotationType type = annot.getAnnotationType();
      if (linkAnnotTypes.contains(type)) {
	if (linkForAnnot.containsKey(annot)) {
	  System.err.println("LTP.annotChanged: " + annot + 
			     " already has a link -- updating.");
	  updateLinkArrow(annot, (String)fromAttr.get(type),
			  (String)toAttr.get(type),
			  (String)labelAttr.get(type));
	} else {
	if (DEBUG > 1)
	  System.err.println("LTP.annotChanged: " + annot + 
			     "link type " + type);
	  addLinkArrow(annot, 
		       (String)fromAttr.get(type),
		       (String)toAttr.get(type), 
		       (String)labelAttr.get(type));
	}
      }
    }
  }

  
}
