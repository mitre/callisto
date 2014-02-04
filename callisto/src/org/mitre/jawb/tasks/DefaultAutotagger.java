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

import org.mitre.jawb.JawbLogger;
import org.mitre.jawb.Jawb;
import org.mitre.jawb.gui.JawbDocument;
import org.mitre.jawb.gui.MainTextFinder;
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.PhraseTaggingAnnotation;
import org.mitre.jawb.atlas.UnmodifiableAttributeException;
import org.mitre.jawb.swing.LineHighlightPainter;

import gov.nist.atlas.type.AnnotationType;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;


import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import java.awt.Color;


/**
 * Default Autotagger implementation.  Duplicates only
 * PhraseTaggingAnnotations. 
 */

public class DefaultAutotagger implements Autotagger {

  private static int DEBUG = 0;
  private List deferredAnnotsList = new ArrayList();

  private final Highlighter autotagHighlighter;
  private final Highlighter.HighlightPainter autotagPainter;
  private final Highlighter.HighlightPainter grayPainter;

  //  private final Highlighter grayHighlighter;


  public DefaultAutotagger () {
    autotagHighlighter = new DefaultHighlighter();
    autotagPainter = 
      //      new DefaultHighlighter.DefaultHighlightPainter(Color.MAGENTA);
      new LineHighlightPainter(Color.MAGENTA, 9, 0);

    if (DEBUG > 0)
      System.err.println ("DefAutotag:initiated Magenta autotag highlighter");
    grayPainter = 
      new DefaultHighlighter.DefaultHighlightPainter(Color.GRAY);
    /***
    grayHighlighter = new DefaultHighlighter();
    grayPainter = 
      new DefaultHighlighter.DefaultHighlightPainter(Color.GRAY);
    ***/
  }
  

  /**
   * Auto-tag the given target tag according to the model: create a
   * new AWBAnnotation anchored at the given start and end locations,
   * that is of the same AnnotationType as the given model.
   *
   * Duplicates only PhraseTaggingAnnotation; for any other type,
   * returns null
   *
   * @param doc the JawbDocument in which the new annot should be created
   * @param model the tag whose annotation should be duplicated
   * @param start the start offset in the text for the new annotation
   * @param end the end offset in the text for the new annotation
   * @return the created AWBAnnotation, or null if the autotag failed 
   */
  public AWBAnnotation autoTag (JawbDocument doc, AWBAnnotation model, 
                                int start, int end) {
    if (model instanceof PhraseTaggingAnnotation) {
      AnnotationType modelType = model.getAnnotationType();
      AWBAnnotation newAnnot = 
        doc.createAnnotation(modelType, start, end, null);      
      // copy all non-extent-related attribute values
      String[] keys = model.getAttributeKeys();
      for (int i=0; i<keys.length; i++) {
        String key = keys[i];
        if (key.startsWith("TextExtent")) {
          break;
        }
        try {
          newAnnot.setAttributeValue(key, model.getAttributeValue(key));
        } catch (UnmodifiableAttributeException x) {
          System.err.println("EntityMentionAutotagger.autoTag unable to copy value of " + key + " attribute due to UnmodifiableAttrException " + x);
        }
      }
      return newAnnot;
    } else {
      return null;
    }
  }

  /**
   * Searches through the document for strings identical to the
   * content of annot, and calls autoTag to create a new annotation
   * wherever an identical string is found
   *
   * This implementation works only for PhraseTaggingAnnotation.
   * For any other type of annotation it returns without doing anything.
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
                        boolean forwardOnly, boolean untaggedOnly) {
    if (!autotagMode.equals("None")) {
      if (annot instanceof PhraseTaggingAnnotation) {
        PhraseTaggingAnnotation ptannot = (PhraseTaggingAnnotation)annot;
        String content = ptannot.getTextExtent();
        if (DEBUG > 0)
          System.err.println("DefAutotagger.doAutotag for content = >>" + 
                             content + "<<");
        int modelStart = ptannot.getTextExtentStart();
        int modelEnd = ptannot.getTextExtentEnd();
        int modelLen = modelEnd - modelStart;
        if (modelLen == 0)
          return;

        findAndTagInstances(modelStart, modelLen, content, doc, docText,
                            finder, textPane, annot, autotagMode,
                            forwardOnly, untaggedOnly);
      }
    }
  }

  /** This method find instances of the model string in the docText.
   * It also either automatically tags them or queries the user
   * before tagging them, addording to the autotagMode setting.
   */
  protected void findAndTagInstances (int modelStart, int modelLen,
                                      String content,
                                      JawbDocument doc, String docText, 
                                      MainTextFinder finder, 
                                      JTextPane textPane,
                                      AWBAnnotation annot, 
                                      String autotagMode,
                                      boolean forwardOnly, 
                                      boolean untaggedOnly) {
    // TODO might eventually want to make these user prefs
    boolean caseSensitive = true;
    boolean isForward = true;
    boolean queryMode = false;
    boolean onlyFullWords = true;
    int start = 0;
    if (forwardOnly) {
      start = modelStart + modelLen;
    }
    List startLocations = new ArrayList();
    final JawbLogger logger = Jawb.getLogger();
    
    if ("Query".equalsIgnoreCase(autotagMode)) {
      queryMode = true;
      deferredAnnotsList.clear();
    }

    // count instances and store start locations
    while (start >= 0) {
      int newStart = 
        finder.find(caseSensitive, docText, content, start, isForward);
      if (newStart >= 0) {
        if (newStart != modelStart) {
          int newEnd = newStart + modelLen;
          if (DEBUG > 0)
            System.err.println("DefAutotagger.findAndTag found instance from "
                               + newStart + " to " + newEnd);

          // only proceed if we've found a full word, or if we don't
          // require one
          if (!onlyFullWords || isFullWord(docText, newStart, newEnd)) {
            // and only proceed if the text is untagged, if required
            if (!untaggedOnly || isUntagged(doc, newStart, newEnd)) {
              startLocations.add(new Integer(newStart));
            }
          }
        }
        start = newStart + modelLen;
      } else {
        // no more instances, leave the loop
        break;
      }
    }

    System.err.println(startLocations.size() + " instances recorded");
    // clear the selection so that the last found instance doesn't get
    // selected if we cancel out of the query below
    // code copied from mainTextPane.clearTextSelection();
    Caret caret = textPane.getCaret();
    int sStart = caret.getMark ();
    int sEnd = caret.getDot ();
    
    if (sStart != sEnd)
    caret.setDot(sEnd);


    // if none found, exit
    if (startLocations.size() == 0)
      return;

    logger.info(JawbLogger.LOG_BEGIN_AUTOTAG, new String [] {autotagMode});
    
    // if Single Query mode, ask the user if they want to autotag all
    // of the instances, telling them how many were counted
    if ("SingleQuery".equalsIgnoreCase(autotagMode)) {
      // Ask user if they want to tag all instances
      // TODO add an option for querying each instance
      int choice = JOptionPane.showConfirmDialog(textPane, "Automatically duplicate tag for all (" + startLocations.size() + ") other instances of " + content + "?", "Auto-Tag All?", JOptionPane.YES_NO_OPTION);
      if (choice != JOptionPane.YES_OPTION) {
        logger.info(JawbLogger.LOG_END_AUTOTAG, new String [] {"canceled"});
        return;
      }
    }
      
    // Loop through all found start locations.  If in query mode, ask
    // the user if they want to tag it, but defer the tagging until
    // all the asking is done.  If not, tag them now and exit.
    Iterator startIter = startLocations.iterator();
    Highlighter oldHighlighter = textPane.getHighlighter();
    Highlighter.Highlight[] oldHighlights = oldHighlighter.getHighlights();
    textPane.setHighlighter(autotagHighlighter);
    Object tempHighlight = null;

    while (startIter.hasNext()) {
      int thisStart = ((Integer)startIter.next()).intValue();
      int thisEnd = thisStart + modelLen;
      System.err.println ("dealing with instance from " + thisStart + 
                          " to " + thisEnd);

      if (queryMode) {
        //textPane.select(thisStart,thisEnd);
        try {
          tempHighlight =
            autotagHighlighter.addHighlight(thisStart, thisEnd, autotagPainter);
        } catch (BadLocationException x) {
          System.err.println ("Bad Location Exception " + x);
        }
        System.err.println("selected from " + thisStart + " to " +
                           thisEnd);

        textPane.requestFocus();
        textPane.repaint();

        // TODO scroll to the location of the highlighted text
        textPane.setCaretPosition(thisStart);
                
        int choice = JOptionPane.showConfirmDialog(null, "Auto-Tag this instance of " + content + "?", "Auto-Tag Instance?", JOptionPane.YES_NO_CANCEL_OPTION);
        // no matter what they chose, it's now time to remove the temp
        // highlight
        autotagHighlighter.removeHighlight(tempHighlight);
        if (choice == JOptionPane.CANCEL_OPTION ||
            choice == JOptionPane.CLOSED_OPTION) {
          int n = deferredAnnotsList.size();
          String maybeS = (n==1?"":"s");
          // if there are no deferred annots, we don't ask, and just clean up
          // and exit as if there were annots and they said no to tagging them
          int tagAnywayChoice = JOptionPane.NO_OPTION;
          if (n > 0) {
            tagAnywayChoice = JOptionPane.showConfirmDialog(null, "Do you still want to tag the " + n + " instance" + maybeS + " you already approved?", "Tag " + n + " Instance" + maybeS + "?", JOptionPane.YES_NO_OPTION);
          }
          if (tagAnywayChoice == JOptionPane.YES_OPTION) {
            // just exit the loop, and go on to tag the deferred annots
            break;
          } else {
            // clean up and exit 
            autotagHighlighter.removeAllHighlights();
            textPane.setHighlighter(oldHighlighter);
            restoreOldHighlights(oldHighlighter, oldHighlights);
            logger.info(JawbLogger.LOG_END_AUTOTAG, new String [] {"canceled"});
            return;
          }
        } else if (choice == JOptionPane.YES_OPTION) {
          try {
            autotagHighlighter.addHighlight(thisStart, thisEnd, grayPainter);
          } catch (BadLocationException x) {
            System.err.println ("Bad Location Exception " + x);
          }
          addToList(thisStart, thisEnd);
        }
      } else { // not query mode, just tag it
        autoTag (doc, annot, thisStart, thisEnd);
      }
    } // while
    textPane.setHighlighter(oldHighlighter);

    
    // Now Autotag deffered annots from approved queries
    if (queryMode) {
      autotagDeferredAnnots(doc, annot);
    }

    logger.info(JawbLogger.LOG_END_AUTOTAG, new String [] {""});
  }

  private void restoreOldHighlights (Highlighter highlighter,
                                     Highlighter.Highlight[] highlights) {
    if (DEBUG > 0)
      System.err.println("DefAutotag.restOldHL restoring " +
                         highlights.length + " old highlights");
    for (int i=0; i<highlights.length; i++) {
      try {
        highlighter.addHighlight(highlights[i].getStartOffset(),
                                 highlights[i].getEndOffset(),
                                 highlights[i].getPainter());
      } catch (BadLocationException x) {
        System.err.println("DefAutoTag.restOldHL BadLocationException " + x);
      }
    }
  }

  public boolean isAutoTaggable(AWBAnnotation annot) {
    return isTriggerAutoTaggable(annot);
  }

  public boolean isTriggerAutoTaggable(AWBAnnotation annot) {
    return (annot instanceof PhraseTaggingAnnotation);
  }

  private void addToList(int start, int end) {
    List pair = new ArrayList(2);
    pair.add(0, new Integer(start));
    pair.add(1, new Integer(end));
    deferredAnnotsList.add(pair);
  }

  private void autotagDeferredAnnots(JawbDocument doc, AWBAnnotation annot) {
    Iterator pairIter = deferredAnnotsList.iterator();
    while (pairIter.hasNext()) {
      ArrayList pair = (ArrayList) pairIter.next();
      int start = ((Integer)pair.get(0)).intValue();
      int end   = ((Integer)pair.get(1)).intValue();
      autoTag(doc, annot, start, end);
    }
  }
  
  /**
   * Cheesy full-word determination that returns true as long as the
   * characters before and after the substring beging checked are not
   * letters.
   *
   * @param fullText the full text
   * @param start the start index of the checked "word" within fullText
   * @param end the end index of the checked "word" within fullText
   */
  private boolean isFullWord(String fullText, int start, int end) {
    boolean beforeIsLetter = false;
    boolean afterIsLetter = false;

    if (DEBUG > 0) {
      System.err.println("DefAutoTagger.isFullWord? " + 
                         fullText.substring(start, end) +
                         " from " + start + " to " + end);
    }

    // if the string being tested is not at the beginning of the file,
    // see if the character before it is a letter or not
    if (start > 0) {
      char before = fullText.charAt(start-1);
      beforeIsLetter = Character.isLetter(before);
      if (DEBUG > 1)
        System.err.println("Char before is: " + before);
    }

    // if the string being tested is not at the end of the file,
    // see if the character before it is a letter or not
    if (end < fullText.length()) {
      char after = fullText.charAt(end);
      afterIsLetter = Character.isLetter(after);
      if (DEBUG > 1)
        System.err.println("Char after is: " + after);
    }

    if (beforeIsLetter || afterIsLetter)
      return false;
    else
      return true;
  }

  private boolean isUntagged(JawbDocument doc, int start, int end) {
    for (int i=start; i<end; i++) {
      List annots = doc.getAnnotationsAt(i);
      if (!annots.isEmpty()) {
        return false;
      }
    }
    // no annots found
    return true;
  }

}


