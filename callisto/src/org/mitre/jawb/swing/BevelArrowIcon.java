
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

import java.awt.*;
import javax.swing.*;

/**
 * @version 1.0 02/26/99
 */
public class BevelArrowIcon implements Icon {
  public static final int UP    = 0;         // direction
  public static final int DOWN  = 1;
  
  private static final int DEFAULT_SIZE = 11;

  private Color edge1;
  private Color edge2;
  private Color fill;
  private int size;
  private int direction;

  public BevelArrowIcon(int direction, boolean isRaisedView, boolean isPressedView) {
    if (isRaisedView) {
      if (isPressedView) {
        init( UIManager.getColor("controlLtHighlight"),
              UIManager.getColor("controlDkShadow"),
              UIManager.getColor("controlShadow"),
              DEFAULT_SIZE, direction);
      } else {
        init( UIManager.getColor("controlHighlight"),
              UIManager.getColor("controlShadow"),
              UIManager.getColor("control"),
              DEFAULT_SIZE, direction);
      }
    } else {
      if (isPressedView) {
        init( UIManager.getColor("controlDkShadow"),
              UIManager.getColor("controlLtHighlight"),
              UIManager.getColor("controlShadow"),
              DEFAULT_SIZE, direction);
      } else {
        init( UIManager.getColor("controlShadow"),
              UIManager.getColor("controlHighlight"),
              UIManager.getColor("control"),
              DEFAULT_SIZE, direction);
      }
    }
  }

  public BevelArrowIcon(Color edge1, Color edge2, Color fill,
                        int size, int direction) {
    init(edge1, edge2, fill, size, direction);
  }


  public void paintIcon(Component c, Graphics g, int x, int y) {
    switch (direction) {
      case DOWN: drawDownArrow(g, x, y); break;
      case   UP: drawUpArrow(g, x, y);   break;
    }
  }

  public int getIconWidth() {
    return size;
  }

  public int getIconHeight() {
    return size;
  }
 

  private void init(Color edge1, Color edge2, Color fill,
                   int size, int direction) {
    this.edge1 = edge1;
    this.edge2 = edge2;
    this.fill = fill;
    this.size = size;
    this.direction = direction;
  }

  private void drawDownArrow(Graphics g, int xo, int yo) {
    g.setColor(edge1);
    g.drawLine(xo, yo,   xo+size-1, yo);
    g.drawLine(xo, yo+1, xo+size-3, yo+1);
    g.setColor(edge2);
    g.drawLine(xo+size-2, yo+1, xo+size-1, yo+1);
    int x = xo+1;
    int y = yo+2;
    int dx = size-6;      
    while (y+1 < yo+size) {
      g.setColor(edge1);
      g.drawLine(x, y,   x+1, y);
      g.drawLine(x, y+1, x+1, y+1);
      if (0 < dx) {
        g.setColor(fill);
        g.drawLine(x+2, y,   x+1+dx, y);
        g.drawLine(x+2, y+1, x+1+dx, y+1);
      }
      g.setColor(edge2);
      g.drawLine(x+dx+2, y,   x+dx+3, y);
      g.drawLine(x+dx+2, y+1, x+dx+3, y+1);
      x += 1;
      y += 2;
      dx -= 2;     
    }
    g.setColor(edge1);
    g.drawLine(xo+(size/2), yo+size-1, xo+(size/2), yo+size-1); 
  }

  private void drawUpArrow(Graphics g, int xo, int yo) {
    g.setColor(edge1);
    int x = xo+(size/2);
    g.drawLine(x, yo, x, yo); 
    x--;
    int y = yo+1;
    int dx = 0;
    while (y+3 < yo+size) {
      g.setColor(edge1);
      g.drawLine(x, y,   x+1, y);
      g.drawLine(x, y+1, x+1, y+1);
      if (0 < dx) {
        g.setColor(fill);
        g.drawLine(x+2, y,   x+1+dx, y);
        g.drawLine(x+2, y+1, x+1+dx, y+1);
      }
      g.setColor(edge2);
      g.drawLine(x+dx+2, y,   x+dx+3, y);
      g.drawLine(x+dx+2, y+1, x+dx+3, y+1);
      x -= 1;
      y += 2;
      dx += 2;     
    }
    g.setColor(edge1);
    g.drawLine(xo, yo+size-3,   xo+1, yo+size-3);
    g.setColor(edge2);
    g.drawLine(xo+2, yo+size-2, xo+size-1, yo+size-2);
    g.drawLine(xo, yo+size-1, xo+size, yo+size-1);
  }


}
