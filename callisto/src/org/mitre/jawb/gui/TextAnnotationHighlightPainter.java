
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

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * HighlightPainter which underlines text with a thick line rather than solid
 * box highlight.
 */
public class TextAnnotationHighlightPainter
  extends DefaultHighlighter.DefaultHighlightPainter {

  private static int DEBUG = 0;

  Color inner = null;
  
  /**
   * Creates a HighlightPainter with the two-color, lines (for contrast) at
   * the top and bottom of the region.  Unfortunately the HighlightPainter is
   * not given enough information to always render the sides properly,
   * otherwise this would be simply "border highlight painter"
   */
  public TextAnnotationHighlightPainter (Color outer, Color inner) {
    super (outer);
    if (inner == null)
      throw new NullPointerException ("top");
    this.inner = inner;
  }

  /**
   * Paint a line under one line of text, from r extending rightward to x2,
   * height is limited to height of r.
   */
  private void paintLine(Graphics g, Rectangle r, int x2, Color selectionColor) {
    // modifies 'drawn' in case this is called from paintLayer
    // TODO: this is now wrong
    int yLow = r.y + r.height -1;
    int y = drawn.y = r.y;
    x2--;

    // draw outer first
    Color co = getColor();
    if (co == null)
      co = selectionColor;
    g.setColor (co);
    int lineWidth = (yLow - y) / 5;
    if (DEBUG>0)
      System.err.println("TextAnnotHLPainter.paintLine: lineWidth = " +
			 lineWidth);
    //g.drawLine (r.x, y, x2, y);
    //g.drawLine (r.x, yLow, x2, yLow);
    g.fillRect (r.x, y-lineWidth+1, (x2 - r.x +1), lineWidth);
    g.fillRect (r.x, yLow, (x2 - r.x +1), lineWidth);
        
    // then inner
    yLow--;
    y++;
    g.setColor (inner);
    g.drawLine (r.x, y, x2, y);
    g.drawLine (r.x, yLow, x2, yLow);

    // TODO: this is now wrong
    drawn.y--;
    drawn.height = r.height;
  }
  
  /** Paint thick lines under a block of text */
  public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
    System.err.println ("\nPAINTING LINE\n");
    Rectangle r0 = null, r1 = null, rbounds = bounds.getBounds();
    int xmax = rbounds.x + rbounds.width; // x coordinate of right edge
    try {  // convert positions to pixel coordinates
      r0 = c.modelToView(p0);
      r1 = c.modelToView(p1);
    } catch (BadLocationException ex) { return; }
    if ((r0 == null) || (r1 == null)) return;

    // special case if p0 and p1 are on the same line
    if (r0.y == r1.y) {
      paintLine(g, r0, r1.x, c.getSelectionColor());
      return;
    }

    // first line, from p1 to end-of-line
    paintLine(g, r0, xmax, c.getSelectionColor());

    // all the full lines in between, if any (assumes that all lines have
    // the same height--not a good assumption with JEditorPane/JTextPane,
    // though they should call paintLayer, right?)
    r0.y += r0.height; // move r0 to next line 
    r0.x = rbounds.x; // move r0 to left edge
    while (r0.y < r1.y) {
      paintLine(g, r0, xmax, c.getSelectionColor());
      r0.y += r0.height; // move r0 to next line
    }

    // last line, from beginning-of-line to p1
    paintLine(g, r0, r1.x, c.getSelectionColor());
  }

  /** We don't need to keep reallocating rectangles within which we draw */
  private Rectangle drawn = new Rectangle ();
  /**
   * Paint thick lines under a block of text for a specific view (portion of
   * highlight) 
   *
   * @param g the graphics context
   * @param p0 the starting model offset >= 0
   * @param p1 the ending model offset >= p0
   * @param bounds the bounding box of the view, which is not
   *        necessarily the region to paint.
   * @param c the editor
   * @param view View painting for
   * @return region drawing occured in
   */
  public Shape paintLayer(Graphics g, int p0, int p1,
                          Shape bounds, JTextComponent c, View view) {
    // special case if p0 and p1 are contained in view
    if (p0 == view.getStartOffset() && p1 == view.getEndOffset()) {

      drawn.setBounds ((bounds instanceof Rectangle) ?
                       (Rectangle)bounds : bounds.getBounds());
      paintLine (g, drawn, drawn.x + drawn.width, c.getSelectionColor());
      return drawn;
    }
    else {
      // Should only render part of View.
      try {
        // --- determine locations ---
        Shape shape = view.modelToView(p0, Position.Bias.Forward,
                                       p1, Position.Bias.Backward,
                                       bounds);
        drawn.setBounds ((shape instanceof Rectangle) ?
                         (Rectangle)shape : shape.getBounds());

        paintLine (g, drawn, drawn.x + drawn.width, c.getSelectionColor());
        return drawn;
      } catch (BadLocationException e) {
        // can't render
      }
    }
    // Only if exception
    return null;
  }
}
