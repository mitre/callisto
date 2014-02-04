
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
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * Extends Default Caret, and auto selects text when using the mouse to place
 * the cursor. Can autoselect on word as well as character.  Use the method
 * {@link #setSelectionMode} to change selection method. The JTextComponents
 * default {@link BreakIterator} is used for all boundary definitions. Note
 * that Jawb comes with a relaxed WordBreakIterator by default, so that
 * words joined by punctuations are not considered 'words' for selecting.<p>
 *
 * In sun's java sources, see:
 * <code>sun.text.resources.BreakIteratorRules_en_US_JAWB</code>
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */
public class AutoSelectCaret extends DefaultCaret {

  private static final int DEBUG = 0;

  /**
   * A typesafe enumeration to indicate selection mode for the text.
   */
  public static class Mode {

    /**
     * Indicates selection by character or 'glyph'
     */
    public static final Mode CHARACTER = new Mode ("Character");

    /**
     * Indicates selection by word, as determinded by the JTextComponents
     * BreakIterator for words.
     */
    public static final Mode WORD = new Mode ("Word");

    /**
     * Return the Mode represented by the specified string.
     * @throws IllegalArgumentException
     */
    public static Mode decode (String mode) {
      if (mode != null) {
        if (mode.equals (CHARACTER.toString()))
          return CHARACTER;
        if (mode.equals (WORD.toString()))
          return WORD;
      }
      throw new IllegalArgumentException ("mode="+mode);
    }
    
    /**
     * String Representation
     */
    public String toString () {
      return name;
    }
    
    private Mode (String name) {
      this.name = name;
    }
    
    private String name;
  }
  
  /** A partial line, semi-transparent highlight */
  private Highlighter.HighlightPainter hp =
    new LineHighlightPainter(null, 7, 180);

  /** Current mode used to select regions */
  private Mode selectionMode;

  /** Force visiblity even if component is not editable */
  private boolean forcedVisible;

  /** Often used temp object to avoid creating new ones */
  private Point pt = new Point();
  /** Often used temp object to avoid creating new ones */
  private Position.Bias[] biasRet = new Position.Bias[1];

  
  /**
   * Word based autoselecting caret.
   */
  public AutoSelectCaret () {
    this (Mode.WORD);
  }

  /**
   * Specify the BreakIterator used when selecting text.
   */
  public AutoSelectCaret (Mode mode) {
    super ();
    selectionMode = mode;
  }
  
  /**
   * Change the minimal amount of text iterated over when selecting.  Default
   * is Mode.WORD. If set to NULL, selection behaves 'normally' without
   * autoselecting.
   * @see AutoSelectCaret.Mode#WORD
   * @see #getSelectionMode
   * @param mode the mode to select with, or null for non autoselect
   */
  public void setSelectionMode (Mode mode) {
    selectionMode = mode;
  }
  
  /**
   * Current minimal amount of text iterated over when selecting.
   * @see AutoSelectCaret.Mode#WORD
   * @see #setSelectionMode
   * @return Mode.WORD, Mode.CHARACTER, or null
   */
  public Mode getSelectionMode () {
    return selectionMode;
  }
  
  /** override getSelectionPainter to return the LineHighlightPainter */
  protected Highlighter.HighlightPainter getSelectionPainter() {
    return hp;
  }

  /** manually set the selection painter */
  public void setSelectionPainter(Highlighter.HighlightPainter hp) {
    this.hp = hp;
  }

  /**
   * Respects the value of selectMode single click either positions caret,
   * selects word, or selects character.
   */
  public void mouseClicked (MouseEvent e) {
    if (! e.isConsumed ()) {
      int nclicks = e.getClickCount();
      if (DEBUG > 2) 
        System.err.println("AutoSelCaret.mClicked nclicks=" + nclicks +
                           " dot=" + getDot() + " mark=" + getMark());
      if (DEBUG > 5) {
        System.err.println ("AutoSelCaret.mClicked who called me?");
        Thread.dumpStack();
      }
      
      if (SwingUtilities.isLeftMouseButton(e)) {
        
        // Instead of doing anything for click/doubleclick, I've simply
        // modified the 'positionCaret' and 'moveCaret' methods below, which
        // are called by many methods in the DefaultCaret
        JTextComponent target = getComponent();
        int offs = target.getCaretPosition();
        if (DEBUG > 2) 
          System.err.println("AutoSelCaret.mClicked left mouse offs=" + offs +
                             " dot=" + getDot() + " mark=" + getMark());

        try {
          if (nclicks == 2 && selectionMode == Mode.CHARACTER) {
            if (DEBUG > 2)
              System.err.println("AutoSelCarat.mclicked nclicks=2 mode=CHAR");
            positionCaret (e);
            moveCaret (e);
          
          } else if (nclicks == 3) {
            if (DEBUG > 2)
              System.err.println("AutoSelCarat.mclicked nclicks=3");
          
            setDot (Utilities.getRowStart (target, offs));
            moveDot (Utilities.getRowStart (target, offs));
            /*
              selectLineAction.actionPerformed
              (new ActionEvent(getComponent(), ActionEvent.ACTION_PERFORMED,
              null, e.getWhen(), e.getModifiers()));
            */
          }
        } catch (Exception x) {
          UIManager.getLookAndFeel().provideErrorFeedback(target);
          System.out.println( "Exception : "+x);
        }
      }
      // DefaultCaret pastes system clipboard on middle mouse
      // clicks, but we're not going to do that, are we...
      // middle mouse button
    }
  }

  /**
   * Tries to set the position of the caret and select the word at point from
   * the coordinates of a mouse event, using viewToModel(). This is called
   * only when setting position, _not_ when extending a selection.
   *
   * @param e the mouse event
   */
  protected void positionCaret(MouseEvent e) {
    if (DEBUG > 5) {
      System.err.println ("AutoSelCaret.pCaret who called me?");
      Thread.dumpStack();
    }
    JTextComponent target = getComponent();
    pt.x = e.getX();
    pt.y = e.getY();
    if (DEBUG > 3)
      System.err.println("AutoSelectCaret.positionCaret (x,y)=(" + pt.x +
                         "," + pt.y + ")\n");

    try {
      // don't position on whitespace, instead deselect current selection!
      int offset = target.getUI().viewToModel(target, pt, biasRet);
      if (DEBUG > 4) 
        System.err.println("AutoSelCaret.pCaret offset=" + offset);
      Rectangle r = target.getUI().modelToView(target, offset);
      int docLength = target.getDocument().getLength();
      int charIndex = offset;
      if (biasRet[0] == Position.Bias.Backward)
        charIndex--;
      if (pt.y <= r.y || pt.y > r.y+r.height ||  // vertical
          charIndex < 0 ||              // }
          charIndex > docLength ||      // } horizontal
          Character.isWhitespace(target.getText(charIndex,1).charAt(0))) {
        if (DEBUG > 4) 
          System.err.println("AutoSelCaret.pCaret set dot to offset & ret\n");
        setDot(offset);
        return;
      }

      if (selectionMode == Mode.CHARACTER) {
        // always set dot when setting position
        setDot (offset);

        if (biasRet[0] == Position.Bias.Backward) {
          if (offset > 0) {
            moveDot (offset-1);
          }
        } else { // Position.Bias.Forward
          if (offset < docLength) {
            moveDot (offset+1);
          }
        }
        
      } else if (selectionMode == Mode.WORD) {
        int start = getWordStart(target, offset);
        if (DEBUG > 3) {
          System.err.println("AutoSelCaret.pCaret WORD mode start=" + start);
          System.err.println("AutoSelCaret.pCaret WORD mode end=" +
                             getWordEnd(target, offset));
        }

        setDot (start);
        moveDot (getWordEnd(target, offset));
        if (DEBUG > 4)
          System.err.println("AutoSelCaret.PCaret WORD mode after setDot and moveDot now dot=" + getDot() + " mark=" + getMark());
        
      } else { // selectionMode == null?
        super.positionCaret (e);
      }
      
    } catch (Exception x) {
      UIManager.getLookAndFeel().provideErrorFeedback(target);
      System.out.println( "Exception : "+x);
    }
  }

  protected int getWordEnd(JTextComponent target, int offset) throws BadLocationException {
    return Utilities.getWordEnd(target, offset);
  }

  protected int getWordStart(JTextComponent target, int offset) throws BadLocationException {
    return Utilities.getWordStart(target, offset);
  }

   
  /***********************************************************************/
  /* MouseListener methods */
    
  /**
   * Tries to move the position of the caret from the coordinates of a mouse
   * event, using viewToModel().  This will cause a selection if the dot and
   * mark are different. Implemented to move word by word as dictated by
   * Utilities.wordIterator
   *
   * @param e the mouse event
   */
  protected void moveCaret(MouseEvent e) {
    if (DEBUG > 5) {
      System.err.println ("AutoSelCaret.mCaret who called me?");
      Thread.dumpStack();
    }
    JTextComponent target = getComponent ();
    pt.x = e.getX();
    pt.y = e.getY();
    if (DEBUG > 3) 
      System.err.println("AutoSelectCaret.moveCaret (x,y) = (" + pt.x +
                         "," + pt.y + ")\n");
    
    try {
      // don't position on whitespace, instead deselect current selection!
      int offset = target.getUI().viewToModel(target, pt, biasRet);
      boolean forwardBias = biasRet[0] == Position.Bias.Forward;
      int dot = getDot();
      int mark = getMark();
      if (DEBUG > 3)
        System.err.println("AutoSelCaret.mCaret: offset=" + offset +
                           " dot=" + dot + " mark=" + mark);
      
      // do nothing if cursor is out of text, or in whitespace
      Rectangle r = target.getUI().modelToView(target, offset);
      int docLength = target.getDocument().getLength();
      int charIndex = offset;
      if (forwardBias)
        charIndex--;
      if (pt.y <= r.y || pt.y > r.y+r.height ||  // vertical
          charIndex < 0 ||              // }
          charIndex > docLength ||      // } horizontal
          Character.isWhitespace(target.getText(charIndex,1).charAt(0))) {
        return;
      }

      if (selectionMode == Mode.CHARACTER) {

        if (mark == dot) {
          // nothing currently selected: select something
          if (DEBUG > 1)
            System.err.println("AutoSelCaret.mCaret: Char mode, nothing selected; call positionCaret\n");
          positionCaret (e);
          return;
        }

        // if (when?) this start's behaving strangely because there's a
        // Navigation filter in the text pane, use stuff like the following
        /*offset = filter.getNextVisualPositionFrom(target, offset,
                                                    Position.Bias.Backward,
                                                    backward, biasRet);*/
        if (mark < dot) {
          if (mark < offset) { // normal forward swipe
            // position mark after char under cursor to highlight
            if (forwardBias) {
              offset++;
              if (offset > docLength)
                offset = docLength;
            }
            moveDot(offset);
            
          } else if (offset < mark || (! forwardBias) ) { // jumped behind mark
            // keep 1 highlighted, and move mark before char under cursor
            setDot(mark+1);
            if (! forwardBias)
              offset--;
            moveDot(offset);
          }

        } else { // (mark > dot)
          if (mark > offset) { // normal backward swipe
            // position mark before char under cursor to highlight
            if (! forwardBias) {
              offset--;
              if (offset < 0)
                offset = 0;
            }
            moveDot(offset);
            
          } else if (offset > mark || forwardBias) { // jumped ahead of mark
            // keep 1 highlighted, and move mark before char under cursor
            setDot(mark-1);
            if (forwardBias)
              offset++;;
            moveDot(offset);
          }
        }

      } else if (selectionMode == Mode.WORD) {
        int start = getWordStart(target, offset);
        int end = getWordEnd(target, offset);

        char firstChar = target.getText(start,1).charAt (0);
        char lastChar = target.getText(end-1,1).charAt (0);

        if (DEBUG > 3) 
          System.err.println("AutoSelCaret.mCaret: WORD mode: start=" + start +
                             " end=" + end + " firstChar =" + firstChar +
                             " lastChar= " + lastChar + "\n");
        // Don't allow selecting whitespace at ends of words by never changing
        // selection if we're in whitespace
        if (Character.isWhitespace (firstChar)) {
          if (DEBUG > 4) 
            System.err.println("AutoSelCaret.mCaret: firstChar is whitespace; returning");
          return;
        }

        if (mark == dot) {
          if (DEBUG > 4)
            System.err.println("AutoSelCaret.mCaret mark==dot\n");
          setDot (start);
          moveDot (end);
          return;
        }

        if ( (start == mark && end == dot) ||
             (end == mark && start == dot) ) { // initial word
          if (DEBUG > 4)
            System.err.println("AutoSelCaret.mCaret initial word\n");
          return;
        }
        
        if (mark < dot) { // forward selection
          if (DEBUG > 4)
            System.err.println("AutoSelCaret.mCaret mark<dot\n");
          if (start < mark) { // backward, select more, select backward
            // if point jumps behind without passing through, need to set mark
            // to end of original word
            int newMark = getWordEnd(target, mark);
            setDot (newMark);
            moveDot (start);
            
          } else { // forward, select more or less, unless at whitespace
              moveDot (end);
          }
          
        } else { // backward selection
          if (end > mark) { // forward, select more, select forward
            // if point jumps ahead without passing through, need to set mark
            // to beginning of original word
            int newMark = Utilities.getWordStart(target, mark-1);
            setDot (newMark);
            moveDot (end);
            
          } else { // backward, select more or less, unless at whatespace
              moveDot (start);
          }
        }

      } else { // selectionMode == null?
        super.positionCaret (e);
      }

    } catch (Exception x) {
      UIManager.getLookAndFeel().provideErrorFeedback(target);
      System.out.println( "Exception : "+x);
      x.printStackTrace();
    }
  }

  /**
   * Called when the component containing the caret gains focus.  Overrides
   * DefaultCaret which only becomes visible again if component is
   * editable. Here we pay attention to our internal flag.
   *
   * @param e the focus event
   * @see FocusListener#focusGained
   */
  public void focusGained(FocusEvent e) {
    JTextComponent component = getComponent();
    if (component.isEnabled()) {
      if (forcedVisible || component.isEditable()) {
        setVisible(true);
      }
      setSelectionVisible(true);
    }
  }

  /**
   * Call to force the caret to be visible even in non-editable
   * components. Note this is does not force the caret to be visible when the
   * component is not in focus, or hide the caret when the component is
   * editable.
   */
  public void setForcedVisible(boolean vis) {
    forcedVisible = vis;
    setVisible(vis);
  }

  /**
   */
  public boolean isForcedVisible() {
    return forcedVisible;
  }
  
  /*
  public static void main (String args[]) {
    JFrame frame = new JFrame ();
    frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
    JTextComponent text = new JTextPane ();
    frame.getContentPane().add(text);

    text.setText ("This is just a sample of what's to come poppet!\n"+
                  "Here's a little more for you're bowl!! \n\n"+
                  "Stick that in yer pipe,\tAnd smoke it!...\n\n"+
                  "\tAVAST!!\t ...R!");
    text.setEditable (false);
    text.setCaret (new AutoSelectCaret());

    frame.setSize (400,600);
    frame.setLocation (250,300);
    frame.setVisible (true);
  }
  */
}

