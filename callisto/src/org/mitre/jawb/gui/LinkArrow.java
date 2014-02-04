
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

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.TextExtentRegion;
import org.mitre.jawb.atlas.NamedExtentRegions;


import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.Dimension;
import java.lang.IllegalArgumentException;
import java.lang.Math;
import java.util.ArrayList;


public class LinkArrow extends JComponent {

  private AWBAnnotation fromAnnot;
  private AWBAnnotation toAnnot;
  private AWBAnnotation annot;
  private LinkedTextPane ltp;
  private LinkLabel label;
  //  private String labelStr;
  private int direction;
  private int fromStartOffset;
  private int fromEndOffset;
  private int toStartOffset;
  private int toEndOffset;
  private boolean highlight;
  private boolean below;

  public static final int DEBUG = 0;

  public static final int LEFT_TO_RIGHT = 0;
  public static final int RIGHT_TO_LEFT = 1;


  /**
   * A LinkArrow is a component representing a link type annotation in
   * a LinkedTextPane.  
   *
   * @param ltp is the LinkedTextPane in which to display the LinkArrow
   * @param annot is the Annotation being represented by the LinkArrow
   * @param label is the String used to label the arc of the LinkArrow
   * @param from is the (text extent) Annotation the LinkArrow goes from
   * @param to is the (text extent) Annotation the LinkArrow goes to
   * @param below is a boolean representing whether the annotation
   * should be displayed below the text or not.
   */
  public LinkArrow (LinkedTextPane ltp, AWBAnnotation annot, String label,
		    AWBAnnotation from, AWBAnnotation to, boolean below) {


    setAnnotsAndOffsets(from, to);

    this.annot = annot;
    this.ltp = ltp;
    this.below = below;
    this.label = new LinkLabel(label, this);
    this.label.addMouseListener(new LinkMouseListener(ltp));

    
    setOpaque(false);
  }

  private void setAnnotsAndOffsets(AWBAnnotation from, AWBAnnotation to) {
    if (DEBUG > 1)
      System.err.println("LinkArrow.setAnnotsAndOffsets from: " + from +
			 " to: " + to);

    if (from instanceof TextExtentRegion) {
      this.fromAnnot = from;
      this.fromStartOffset = ((TextExtentRegion)from).getTextExtentStart();
      this.fromEndOffset   = ((TextExtentRegion)from).getTextExtentEnd();
    } else if (from instanceof NamedExtentRegions) {
      // assumes it will have a "full" extent
      this.fromAnnot = from;
      this.fromStartOffset = 
	((NamedExtentRegions)from).getTextExtentStart("full");
      this.fromEndOffset = 
	((NamedExtentRegions)from).getTextExtentEnd("full");
    } else {
      throw new IllegalArgumentException
	("LinkArrow may only be created between two text extent type annotations\nFrom annotation is of type " + 
	 fromAnnot.getAnnotationType().getName());
    }
    if (to instanceof TextExtentRegion) {
      this.toAnnot = to;
      this.toStartOffset = ((TextExtentRegion)to).getTextExtentStart();
      this.toEndOffset   = ((TextExtentRegion)to).getTextExtentEnd();
    } else if (to instanceof NamedExtentRegions) {
      this.toAnnot = to;
      this.toStartOffset = ((NamedExtentRegions)to).getTextExtentStart("full");
      this.toEndOffset   = ((NamedExtentRegions)to).getTextExtentEnd("full");
    } else {
      throw new IllegalArgumentException
	("LinkArrow may only be created between two text extent type annotations\nTo annotation is of type " +
	 toAnnot.getAnnotationType().getName());
    }
    if (fromStartOffset < toStartOffset) 
      direction = LEFT_TO_RIGHT;
    else
      direction = RIGHT_TO_LEFT;
 }

  public void updateAnnots(String fromAttr, String toAttr) {
    AWBAnnotation from =(AWBAnnotation)annot.getAttributeValue(fromAttr);
    AWBAnnotation to = (AWBAnnotation)annot.getAttributeValue(toAttr);
    if (DEBUG > 1) 
      System.err.println("LinkArrow.updateAnnots from: " + from +
			 " to " + to);
    // assume that if from == to, we are in the middle of updating the
    // Annotation, and do nothing yet
    if (from != to) {
      setAnnotsAndOffsets(from, to);
      repaint();
    }
  }

  public int getFromStart() {
    return fromStartOffset;
  }

  public int getFromEnd() {
    return fromEndOffset;
  }

  public int getToStart() {
    return toStartOffset;
  }

  public int getToEnd() {
    return toEndOffset;
  }

  public boolean overlaps (LinkArrow otherArrow) {
    // TODO
    int thisLeft = Math.min(getFromStart(), getToStart());
    int thisRight = Math.max(getFromEnd(), getToEnd());
    int otherLeft = 
      Math.min(otherArrow.getFromStart(), otherArrow.getFromEnd());
    int otherRight =
      Math.max(otherArrow.getFromEnd(), otherArrow.getToEnd());
    if (isBelow() == otherArrow.isBelow()) {
      // case 1: otherArrow's leftmost point is within this arrow
      if (thisLeft <= otherLeft && otherLeft < thisRight)
	return true;
      // case 2: this Arrow's leftomst point is within otherArrow
      if (otherLeft <= thisLeft && thisLeft < otherRight)
	return true;
    }

    // otherwise, they do not overlap
    return false;
  }

  public int getCenteredOffsetLength() {
    return Math.abs(toStartOffset + ((toEndOffset-toStartOffset) / 2) -
		    (fromStartOffset + ((fromEndOffset-fromStartOffset) / 2)));
  }

  public AWBAnnotation getAnnot() {
    return annot;
  }

  public AWBAnnotation getFromAnnot() {
    return fromAnnot;
  }

  public AWBAnnotation getToAnnot() {
    return toAnnot;
  }

  public JLabel getLabel() {
    return label;
  }

  public void setLabel(String labelstr, boolean below) {
    this.label.setText(labelstr);
    this.below = below;
  }

  public void setHighlight(boolean h) {
    if (DEBUG > 1)
      System.err.println("Set highlight: " + (h?"ON":"OFF"));
    highlight = h;
    repaint();
  }

  public boolean isBelow () {
    return below;
  }

  protected void paintComponent (Graphics g) {

    Dimension size = getSize();
    // TODO make this depend on overall size;
    int arrowSize = 4;
    int length = size.width - arrowSize;
    int height = size.height;

    // TODO use label height instead
    FontMetrics f = g.getFontMetrics();
    int textHeight = f.getAscent();
    //    int stringWidth = f.stringWidth(labelStr);
    //System.err.println("length = " + length + " stringWidth = " + stringWidth);

    int arcHeight = height - textHeight - 2;

    //    int textHeight = ltp.getJawbDocument().getFontSize();

    //    int startText = (length/2) - (stringWidth/2);
    //System.err.println("current paint color is " + g.getColor());
    //System.err.println("labelstr is " + labelStr + " startText at " +
    //	       startText);
    int arc;
    int boxY;

    if (below) {
      arc = 180;
      boxY = 0 - height + textHeight;
    } else {
      arc = -180;
      boxY = textHeight;
    }
      
    if (direction == LEFT_TO_RIGHT) {
      g.drawArc(0, boxY, length, 2*arcHeight, 180, arc);
      if (below) {
	g.fillPolygon (new int[] {length, length-arrowSize, length+arrowSize}, 
		       new int[] {0,      arrowSize,        arrowSize},
		       3);
     } else {
	g.fillPolygon(new int[] {length, length-arrowSize, length+arrowSize}, 
		      new int[] {height, height-arrowSize, height-arrowSize},
		      3);
      }
    } else {
      g.drawArc(arrowSize, boxY, length, 2*arcHeight, 180, arc);
      if (below) {
	g.fillPolygon (new int[] {0,         2*arrowSize, arrowSize},
		       new int[] {arrowSize, arrowSize,   0},
		       3);
      } else {
	g.fillPolygon(new int[] {0,                2*arrowSize,    arrowSize}, 
		      new int[] {height-arrowSize, height-arrowSize, height},
		      3);
      }
    }
    //    g.drawString(labelStr, startText, textHeight-2);

    if (highlight)
      g.drawRect(0, 0, size.width-1, height-1);
    
  }

  class LinkMouseListener extends MouseInputAdapter {

    private LinkedTextPane ltp;
    private JawbDocument doc;
    private AWBAnnotation annot;
    

    LinkMouseListener (LinkedTextPane ltp) {
      this.ltp = ltp;
      this.doc = ltp.getJawbDocument();
    }

    public void mousePressed (MouseEvent e) {
      LinkArrow arrow = LinkArrow.this;
      annot = arrow.getAnnot();
      doc.getAnnotationMouseModel().fireAnnotationPressedEvent
	(e, annot, ltp);
    }
    public void mouseReleased (MouseEvent e) {
      LinkArrow arrow = LinkArrow.this;
      annot = arrow.getAnnot();
      doc.getAnnotationMouseModel().fireAnnotationReleasedEvent
	(e, annot, ltp);
    }

    // send ltp's annotationMouseListener as default, so that
    // annotation will be selected if click is not consumed
    public void mouseClicked (MouseEvent e) {
      LinkArrow arrow = LinkArrow.this;
      annot = arrow.getAnnot();
      ArrayList annotList = new ArrayList(1);
      annotList.add(annot);
      if (DEBUG > 1)
	System.err.println("LinkArrow.mClicked: annot is " + annot);
      doc.getAnnotationMouseModel().fireAnnotationClickedEvents
	(e, annotList, ltp, ltp.getAnnotMouseListener());
    }

  }
      
  class LinkLabel extends JLabel {
    private LinkArrow arrow;

    public LinkLabel (String labelStr, LinkArrow arrow) {
      super (labelStr);
      this.arrow = arrow;
    }
  }

}
