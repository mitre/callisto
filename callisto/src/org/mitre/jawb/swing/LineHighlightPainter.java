
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

package org.mitre.jawb.swing;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * HighlightPainter which underlines text with a thick line rather than solid
 * box highlight.
 */
public class LineHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {

  /** Height of highlighting */
  private final static int DEFAULT_HEIGHT = 3;

  /** Height of highlighting */
  private final static int DEFAULT_ALPHA = 185;

  private int height;
  private int alpha;

  /**
   * Creates a highlighter of the specified color (or the components selection
   * color) with height of 3, and alpha of 185.
   */
  public LineHighlightPainter (Color c) {
    this (c, DEFAULT_HEIGHT, DEFAULT_ALPHA);
  }

  /**
   * Create a line highlight painter with specified color, height and alpha
   * channel.  If c is null, then the component's default selection color is
   * used. If height is 0, the the height is the full width of the line being
   * highlighted. If <code>height</code> is taller than the view/shape being
   * highlighted, the height of the view/shape is used. If <code>c</code> is
   * set, then <code>alpha</code> is ignmred.
   *
   * @param c the color to highlight with, if specified, alpha is ignored.
   * @param height height of highlight from bottom of shape/view (generally a
   *               line of text). if 0, use full height. if taller than view,
   *               excess ignored. values < 0 are treated as 0.
   * @param alpha Alpha chanel to render default highlight color with if
   *              color is unspecified. 0 <= alpha <= 255, out of bounds
   *              values are set to closest value: 0 or 255 
   */
  public LineHighlightPainter (Color c, int height, int alpha) {
    super (c);
    this.height = Math.max (0, height);
    this.alpha = Math.max (0, Math.min (255, alpha));
  }
  
  /**
   * Paint a line under one line of text, from r extending rightward to x2,
   * height is limited to height of r.
   */
  private void paintLine(Graphics g, Rectangle r, int x2) {
    // modifies 'drawn' in case this is called from paintLayer
    int h;
    if (height == 0)
      h = r.height;
    else
      h = Math.min (height, r.height);
    
    drawn.y = r.y + r.height - h;
    g.fillRect (r.x, drawn.y, x2 - r.x, h);
    drawn.height = h;
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

    Color co = getColor();
    if (co == null) {
      co = c.getSelectionColor();
      co = new Color (co.getRed(), co.getGreen(), co.getBlue(), alpha);
    }
    g.setColor (co);

    // special case if p0 and p1 are on the same line
    if (r0.y == r1.y) {
      paintLine(g, r0, r1.x);
      return;
    }

    // first line, from p1 to end-of-line
    paintLine(g, r0, xmax);

    // all the full lines in between, if any (assumes that all lines have
    // the same height--not a good assumption with JEditorPane/JTextPane,
    // though they should call paintLayer, right?)
    r0.y += r0.height; // move r0 to next line 
    r0.x = rbounds.x; // move r0 to left edge
    while (r0.y < r1.y) {
      paintLine(g, r0, xmax);
      r0.y += r0.height; // move r0 to next line
    }

    // last line, from beginning-of-line to p1
    paintLine(g, r0, r1.x);
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
    
    Color co = getColor();
    if (co == null) {
      co = c.getSelectionColor();
      co = new Color (co.getRed(), co.getGreen(), co.getBlue(), alpha);
    }
    g.setColor (co);
      
    // special case if p0 and p1 are contained in view
    if (p0 == view.getStartOffset() && p1 == view.getEndOffset()) {

      drawn.setBounds ((bounds instanceof Rectangle) ?
                       (Rectangle)bounds : bounds.getBounds());
      paintLine (g, drawn, drawn.x + drawn.width);
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

        paintLine (g, drawn, drawn.x + drawn.width);
        return drawn;
      } catch (BadLocationException e) {
        // can't render
      }
    }
    // Only if exception
    return null;
  }
}
