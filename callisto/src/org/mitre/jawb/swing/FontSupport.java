
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

import java.awt.Font;
import java.awt.ComponentOrientation;
import java.awt.GraphicsEnvironment;
import java.util.*;
import java.text.*;

import java.io.*;

/**
 * Determines the amount of coverage, (number of characters) in a Font for a
 * given string. Can be used to rank available fonts, choosing the best to
 * display some text.
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 */
public class FontSupport {

  // move out to be user configurable?
  private static HashMap orientation = new HashMap ();

  private String string;
  private HashMap cachedScores;
  private List blockScores;
  private Set families;

  static {
    // set default  unicode block -> orientation mappings
    // overkill to do this for just HEBREW and ARABIC?
    
    // should default to RIGHT if not entered.
    orientation.put (Character.UnicodeBlock.ARABIC,
                     ComponentOrientation.RIGHT_TO_LEFT);
    orientation.put (Character.UnicodeBlock.HEBREW,
                     ComponentOrientation.RIGHT_TO_LEFT);
    // leaving CJK and Katakana LEFT_TO_RIGHT
  }

  /**
   * Create a new FontSupport for ranking validity of Fonts for a string. By
   * default, all fonts known to the system are used in ranking. By setting
   * specific family names using {@link #addFontFamily}, the number of font's
   * scored can be limited.
   */
  public FontSupport (String s) {
    string = s;
    cachedScores = new HashMap ();
  }

  /**
   * Clear the font families specified to be ranked so that all font's known
   * to the system are ranked.
   */
  public void clearfontFamilies () {
    families = null;
  }

  /**
   * Add a font to be ranked, but adding only one is pointless. By default all
   * fonts known to the system are ranked, but by adding specific fonts, you
   * can drastically reduce the time to rank. If <code>family</code> is
   * <code>null</code> the 'Default' font is used.  font.
   */
  public void addFontFamily (String family) {
    if (families == null)
      families = new HashSet ();
    if (family == null)
      families.add ("Default");
    else
      families.add (family);
  }
  
  /**
   * Determine the percent of a Strings characters a font supports. Result is
   * between 0.0 and 1.0, inclusive.
   */
  public static double getCoverageBy (Font font, String s) {
    
    int count = 0;
    CharacterIterator iter = new StringCharacterIterator (s);
    for (char c=iter.first (); c != CharacterIterator.DONE; c=iter.next ()) {
      if (font.canDisplay (c)) {
        count++;
      }
    }
    return (double)count/(double)s.length();
  }

  
  /**
   * Get the percent of the supported string which can be displayed by this
   * font, as a decimal beteween 0.0 and 1.0. 
   */
  public double getCoverageBy (Font f) {
    return getScore (f).score;
  }

  /**
   * Get the percent of the supported string which can be displayed by this
   * font, as a decimal beteween 0.0 and 1.0. 
   */
  public double getCoverageBy (String family) {
    return getCoverageBy (Font.decode (family));
  }

  /**
   * Return an array of available Family names in descending order of their
   * coverage
   */
  public FontScore[] rankFamilies () {
    if (families == null) {
      GraphicsEnvironment g =
        GraphicsEnvironment.getLocalGraphicsEnvironment();
      String fnames[] = g.getAvailableFontFamilyNames ();
      families = new HashSet (Arrays.asList (fnames));
    }

    Vector sorter = new Vector ();

    // score all the families with their 'representative'
    Iterator iter = families.iterator ();
    while (iter.hasNext ()) {
      String family = (String)iter.next ();
      Font font = Font.decode (family);
      sorter.add (getScore (font));
    }
    
    Collections.sort (sorter, new ScoreCompare ());

    return (FontScore[])sorter.toArray (new FontScore[0]);
  }

  /**
   * Return an array of {@link java.lang.Character.UnicodeBlock} objects in
   * decending order of their coverage.
   */
  public UnicodeBlockScore[] rankUnicodeBlocks () {
    if (blockScores == null) {
      
      HashMap blockMap = new HashMap ();

      // count the number of characters for each Unicode block represented
      CharacterIterator charIter = new StringCharacterIterator (string);
      for (char c=charIter.first ();
           c != CharacterIterator.DONE;
           c=charIter.next ()) {
        UnicodeBlockScore uniScore =
          (UnicodeBlockScore) blockMap.get (Character.UnicodeBlock.of (c));
        if (uniScore == null) {
          uniScore = new UnicodeBlockScore ();
          uniScore.block = Character.UnicodeBlock.of (c);
          blockMap.put (uniScore.block, uniScore);
        }
        uniScore.hits++;
      }

      // normalize and sort results
      blockScores = new ArrayList (blockMap.values ());
      double size = (double)string.length ();
      Iterator scoreIter = blockScores.iterator ();
      while (scoreIter.hasNext ()) {
        UnicodeBlockScore bs = (UnicodeBlockScore) scoreIter.next ();
        bs.score = (double)bs.hits/size;
      }
      Collections.sort (blockScores, new UniBlockCompare ());
    }
    return (UnicodeBlockScore[])
      blockScores.toArray(new UnicodeBlockScore[0]);
  }

  /**
   *
   */
  private ComponentOrientation determineOrientation () {
    // count the number of characters for each Unicode block represented
    CharacterIterator charIter = new StringCharacterIterator (string);
    int ltr = 0;
    for (char c=charIter.first ();
         c != CharacterIterator.DONE;
         c=charIter.next ()) {
      Character.UnicodeBlock block = Character.UnicodeBlock.of (c);
      ComponentOrientation o = (ComponentOrientation) orientation.get (block);
      if (o == null || o.isLeftToRight ())
        ltr++;
      else
        ltr--;
    }
    return (ltr < 0) ?
      ComponentOrientation.RIGHT_TO_LEFT : ComponentOrientation.LEFT_TO_RIGHT;
  }
  
  /**
   * Ranks unicode block coverage for the the string, and returns the best
   * orientation based on unicode block.
   * @return Alignment defined by: ComponentOrientation.
   */
  public ComponentOrientation getOrientation () {
    /*
      UnicodeBlockScore[] scores = rankUnicodeBlocks ();
    if (scores.length == 0)
      return ComponentOrientation.UNKNOWN;
    Object orient = orientation.get (scores[0].block);
    if (orient == null)
      return ComponentOrientation.LEFT_TO_RIGHT;

    return (ComponentOrientation) orient;
    */
    return determineOrientation ();
  }
  
  /**
   * Attempts to look up a score in a cache before calculating it.
   */
  private FontScore getScore (Font f) {
    FontScore fs = (FontScore)cachedScores.get (f.getFamily ());
    if (fs == null) {
      fs = new FontScore ();
      fs.score = getCoverageBy (f, string);
      fs.familyName = f.getFamily ();
      cachedScores.put (fs.familyName, fs);
    }
    return fs;
  }
  
  /** Class to store the score, name and family of a font.  Immutable because
   * it is used internally for caching as well.
   */
  public class FontScore {
    private String familyName;
    private double score;
    public String getFamily () { return familyName; }
    public double getScore () { return score; }
  }

  /* Compares FontScore objects for sorting */
  private class ScoreCompare implements Comparator {
    public int compare (Object a, Object b) {
      FontScore fa = (FontScore)a;
      FontScore fb = (FontScore)b;
      
      if (fa.score == fb.score)
        return 0;
      if (fa.score < fb.score)
        return 1;
      return -1;
    }
    public boolean equals (Object o) {
      return false;
    }
  }

  /** Store scores for character blocks while ranking */
  public class UnicodeBlockScore {
    private Character.UnicodeBlock block;
    private int hits = 0;
    private double score = 0;
    public Character.UnicodeBlock getUnicodeBlock () { return block; }
    public double getScore () { return score; }
  }

  private class UniBlockCompare implements Comparator {
    public int compare (Object a, Object b) {
      UnicodeBlockScore ba = (UnicodeBlockScore)a;
      UnicodeBlockScore bb = (UnicodeBlockScore)b;

      if (ba.score == bb.score)
        return 0;
      if (ba.score < bb.score)
        return 1;
      return -1;
    }
    public boolean equals (Object o) {
      return false;
    }
  }
  
  public static void main (String[] args) {

    String[] strings = {
      "\u4e2d\u56fd\u5916\u4ea4\u90e8\u957f\u6628\u5929\u4e0b\u5348\u4e09\u70b9\u949f\u5728\u5317\u4eac\u4f1a\u89c1\u4e86\u963f\u62c9\u4f2f\u5916\u957f\u3002",
      //"中国外交部长昨天下�?�三点钟在北京会�?了阿拉伯外长。", // zh
      "\u0635\u0644\u0627\u062d \u06a9\u0627\u0631 \u06a9\u062c\u0627 \u0648 \u0645\u0646 \u062e\u0631\u0627\u0628 \u06a9\u062c\u0627",
      //"صلاح کار کجا و من خراب کجا", // ar
      "\u053f\u0575\u0561\u0565\u0562\u057d \u0579\u057f\u0561\u0575\u056b \u056f\u0561\u057d\u056f\u0561\u056e\u056b \u0574\u0570\u0563\u056b\u0565"
      //"Կյաեբս չտայի կասկածի մհգիե"  // am
    };


    String[] families = new String[strings.length];
    
    for (int i=0; i<strings.length; i++) {
      FontSupport fs = new FontSupport (strings[i]);
      System.err.println ("String: "+strings[i]);
      
      FontScore[] scores = fs.rankFamilies ();
      families[i] = scores[0].getFamily ();
      for (int j=0; j<scores.length; j++) {
        System.err.println("  "+scores[j].getScore()+" \t"+scores[j].getFamily());
      }
      System.err.println ();
    }

    /*
    JFrame frame = new JFrame ();
    JTabbedPane tabs = new JTabbedPane ();
    frame.getContentPane ().add (tabs);

    for (int i=0; i<strings.length; i++) {
      JPanel p = new JPanel (new BorderLayout ());
      JTextPane tp = new JTextPane ();
      p.add (new JLabel (families[i]), BorderLayout.NORTH);
      p.add (tp, BorderLayout.CENTER);
      tabs.add (families[i], tp);

      Style fontFamily = tp.addStyle ("fontFamily", null);
      StyleConstants.setFontFamily (fontFamily, families[i]);
      tp.setCharacterAttributes (fontFamily, true);
      tp.setText (strings[i]);
    }
    frame.setDefaultCloseOperation (frame.EXIT_ON_CLOSE);
    frame.pack ();
    frame.setVisible (true);
    */
  }
  
}// FontSupport
