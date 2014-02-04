
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

package org.mitre.jawb.io;

import java.io.*;
import java.net.URI;
import java.net.URL;

import java.util.*;

/* This code opens a named input SGML file and transforms
 * it into a standard Atlas Interchange Format (AIF) output
 * file.
 *
 * Author: David Day
 * Edit History:
 * 2003/01/25 Began development.
 * 2003/06/17 Split ParseSgmlRDC SgmlTag and SgmlEntity from ParseSGML
 * 2008/06/18 Added debugging (robyn)
 */

/** Generic parser/balancer for SGML markup */
public class SgmlDocument {

  static final String VERSION = "2.2";
  static final String LASTMOD = "2003-09-10";

  public int          debugLevel = 0;
  static final int    DEBUG = 0;

  /* Constants: Default tag array size and increments.
   * Note: This is merely the parse (stack) depth of the Sgml tags in a file,
   * not the number of tags in the file, which are maintained in an arrayList. */
  static final int TAGDEPTHINCREMENT = 50;
  protected int maxTagDepth = TAGDEPTHINCREMENT;

  /* Create standoff model for all SGML tags. */
  protected ArrayList            sgmlTags = null;
  /* Elements at the root of the document (found by balancing) */
  protected SgmlElement       rootElement  = null;
  protected StringBuffer textSignalBuffer = null;
  protected int                  tagCount = 0;
  /** make sure we only balance once */
  protected boolean balanced;

  /** Create an empty sgml document. */
  public SgmlDocument (String text) {
    this (text, 0);
  }
  
  /** Create an empty sgml document with debugging turned on. */
  public SgmlDocument (String text, int debugLevel) {
    this.debugLevel  = debugLevel;
    textSignalBuffer = new StringBuffer (text);
    sgmlTags         = new ArrayList ();
    rootElement      = new SgmlElement (textSignalBuffer.length ());
    if (DEBUG > 0) {
      System.err.println("  New SgmlDocument from text: signal chars = " + 
                         textSignalBuffer.length ());
    }
     
  }
  
  public SgmlDocument (Reader in) throws IOException {
    this (in, 0);
  }
  
  public SgmlDocument (Reader in, int debugLevel) throws IOException {
    this.debugLevel = debugLevel;
    parseSgmlTags(in);
    balanceSgmlTags();
    if (debugLevel > 0) {
      System.err.println("  signal characters = " + textSignalBuffer.length () +
                         ";  sgml tags = " + sgmlTags.size () +
                         ";  sgml elements = " + rootElement.childCount () +
                         ";  root elements = " + rootElement.children.size ());
    }
  }
  
  /* ******************************************************************* *
   * Create more tags and balance as we go *
   * ******************************************************************* */

  /**
   * Boilerplate work of adding a new element. Returns the element inserted
   * @throws IndexOutOfBoundsException
   * @throws OverlappingElementException
   */
  private boolean insertElement (SgmlElement element, boolean nested) {
    if (DEBUG > 1)
      System.err.println("SGMLdoc.insertElement 1 " + element.getGid() + 
                         " signal chars = " + textSignalBuffer.length());
    try {
      // must check non-surrounding for root
      if (! rootElement.contains (element, false)) {
        throw new IndexOutOfBoundsException
          ("tag: ["+element.start+","+element.end+"] out of range ["+
           rootElement.start+","+rootElement.end+"]");
      }
    } catch (OverlappingElementException x) {
      IndexOutOfBoundsException e = new IndexOutOfBoundsException ();
      e.initCause (x);
      throw e;
    }

    if (DEBUG > 1)
      System.err.println("SGMLdoc.insertElement 2 " + element.getGid() + 
                         " signal chars = " + textSignalBuffer.length());

    // it will go in unless overlap exception
    boolean result = false;
    try {
      result = rootElement.addElement (element, nested);
      if (result) {
        if (element.openTag != null)
          sgmlTags.add (element.openTag);// TODO: order?
        if (element.closeTag != null)
          sgmlTags.add (element.closeTag);// TODO: order?
      }
    } catch (OverlappingElementException x) {
      if (DEBUG > 1)
        System.err.println("SGMLdoc.insertElement overlap " + element.getGid() + 
                           " signal chars = " + textSignalBuffer.length());

      throw new OverlappingElementException(x.getMessage()+
                                            " \""+getSignalText(element)+"\"");
      
    }
    if (DEBUG > 1)
      System.err.println("SGMLdoc.insertElement 3 " + element.getGid() + 
                         " signal chars = " + textSignalBuffer.length());

    return result;
  }

  /**
   * Insert an element (sans children) into the document. This will copy the
   * specified element, without it's children, and insert in the element
   * heirarchy.
   *
   * @throws IndexOutOfBoundsException
   * @throws OverlappingElementException
   * @return true if the element successfully fit in the document.
   */
  public boolean addElement (SgmlElement element, boolean nested) {
    return insertElement (new SgmlElement (element), nested);
  }
  
  /**
   * Creates an XML style empty tag. Equivalent to
   * {@link #createEmptyTag(int,String,boolean,boolean)}.
   * @throws IllegalArgumentException if offset < 0 || > length of text
   */
  public SgmlElement createEmptyTag (int offset, String name, boolean nested) {
    return createEmptyTag (offset,name,nested,true);
  }

  /**
   * Creates an empty tag either XML style (trailing '/') or a unpaired SGML
   * open tag, based on value of <code>xml</code>
   * @throws IndexOutOfBoundsException  if offset < 0 || > length of text
   */
  public SgmlElement createEmptyTag (int offset, String name,
                                  boolean nested, boolean xml) {
    if (debugLevel > 1)
      System.err.println("Entering createEmptyTag..."); 
    SgmlTag tag;
    if (xml)
      tag = SgmlTag.createEmptyTag (tagCount++, offset, name);
    else
      tag = SgmlTag.createOpenTag (tagCount++, offset, name);

    SgmlElement element = new SgmlElement (tag);
    addElement (element, nested);

    return element;
  }

  /**
   * Creates a comment tag. The 'comment' will appear between the "<!--" and
   * "-->" of the comment. You'll need to add any additional whitespace.
   * @param comment text of comment. may be null.
   * @throws IndexOutOfBoundsException  if offset < 0 || > length of text
   */
  public SgmlElement createCommentTag (int offset, String comment, boolean nested) {
    if (debugLevel > 1)
      System.err.println("Entering createCommentTag..."); 
    SgmlTag tag = SgmlTag.createCommentTag (tagCount++, offset, comment);

    SgmlElement element = new SgmlElement (tag);
    addElement (element, nested);

    return element;
  }

  /**
   * Creates a declaration tag. If content is non-null. The gid must begin with
   * the '!' or '?' character. The content tag (if non-null) should
   * include any whitespace following the gid (a space will be prepended if
   * not), and the trailing '?' if the gid began with one.
   * @param gid name of the Declaration tag <i>with</i> initial '!' or
   * '?'. If starting w/ '?', and the content is null, the gid must end w/
   * '?' also.
   * @param content text after gid. if gid starts w/ a '?' and content is
   * not null, content must end with a '?'.
   * @throws IndexOutOfBoundsException  if offset < 0 || > length of text
   */
  public SgmlElement createDeclarationTag (int offset, String gid, String content,
                                        boolean nested) {
    if (debugLevel > 1)
      System.err.println("Entering createDeclarationTag..."); 
    SgmlTag tag = SgmlTag.createDeclarationTag (tagCount++, offset, gid, content);

    SgmlElement element = new SgmlElement (tag);
    addElement (element, nested);

    return element;
  }

  /**
   * @throws IndexOutOfBoundsException  if offset < 0 || > length of text
   */
  public SgmlElement createContentTag (int start, int end, String gid, boolean nested) {
    if (debugLevel > 1)
      System.err.println("Entering createContentTag..."); 
    SgmlTag openTag = SgmlTag.createOpenTag (tagCount++, start, gid);
    SgmlTag closeTag = SgmlTag.createCloseTag (tagCount++, end, gid);

    SgmlElement element = new SgmlElement (openTag, closeTag);
    addElement (element, nested);

    return element;
  }

  
  /** ******************************************************************
   *  parseSgmlTags
   *  Read through input text finding all Sgml tags and creating
   *  SgmlTag objects for each one.  The SgmlTags are maintained in an
   *  arrayList.
   ********************************************************************* */

  /**
   * Reset the parser, read the document, retrieving all tags. Using a
   * BufferedReader will often be more efficient.
   */
  private void parseSgmlTags (Reader in) throws IOException  {
    int     charAsInt;
    int     charCount = 0;
    SgmlTag tag;

    sgmlTags = new ArrayList ();
    textSignalBuffer = new StringBuffer ();

    if (debugLevel > 1)
      System.err.println("Entering parseSgmlTags...");
    for (int i = 0; (charAsInt = in.read()) != -1; i++) {
      /* System.err.println("translate: charCount = " + charCount + 
         " character = " + String.valueOf(character)); */
      if (charAsInt == '<') {
        tag = new SgmlTag(charCount, in);
        sgmlTags.add(tag);
      } else {
        /* Only count characters that are NOT SGML markup chars. */
        charCount++;
        /* Append non-SGML-markup char to text only signal string. */
        textSignalBuffer.append((char)charAsInt);
      }
    }
  }

  /* ******************************************************************* *
   * balanceSgmlTags                                                     *
   * ******************************************************************* */

  /**
   * Match tags into elments, and determine those w/ no close tag.
   *
   * @throws IllegalStateException if tags have not yet been parsed.
   */
  private void balanceSgmlTags() {
    if (balanced)
      return;

    if (sgmlTags == null)
      throw new IllegalStateException ("No tags parsed yet");
    
    rootElement  = new SgmlElement (textSignalBuffer.length ());

    int             stackPointer = -1;
    SgmlTag[]       tagStack = new SgmlTag[maxTagDepth];
    String          thisGid;
    SgmlElement     balancedElement;
    SgmlElement     emptyElement;
    SgmlElement     containedElement;
    Iterator        sgmlTagsIter = sgmlTags.iterator();

    // System.err.println("Entering balanceSgmlTags..."); 
    while (sgmlTagsIter.hasNext()) {
      SgmlTag tag = (SgmlTag)sgmlTagsIter.next();
      //System.err.println(" next tag " + tag);

      if (tag.tagType == SgmlTag.Type.OPEN) {
        /* An OPEN tag gets pushed onto the stack.
         * stackPointer == -1 means that stack is un-initialized. */
        //System.err.println(" pushing onto stack (stackPointer = " +
        // stackPointer + ") "+tag);
        if (stackPointer == -1) {
          stackPointer = 0;
          tagStack[stackPointer] = tag;
        } else {
          stackPointer++;
          tagStack[stackPointer] = tag;
          /* Grow size of stack if we are running out of room.  */
          if (stackPointer >= (maxTagDepth - 1)) {
            maxTagDepth += TAGDEPTHINCREMENT;
            SgmlTag[] newSgmlStack = new SgmlTag[maxTagDepth];
            for(int i = 0; i <= stackPointer; i++) {
              newSgmlStack[i] = tagStack[i];
            }
            tagStack = newSgmlStack;
          }
        }
      } else if (tag.tagType == SgmlTag.Type.CLOSE) {
        /* A CLOSE tag pops all tags off stack from here until balancing OPEN tag. */
        /* (1) Merge tags to create element, with content;
         * (2) Assign all intervening tags on stack to SGMLEMPTYELMNT;
         * (3) Pop all tags on stack up until j. */
        /* System.err.println(" looking for balancing open tag"); */
        int balancedStackPtr = -1;
        for (int j = stackPointer; j >= 0; j--) {
          //System.err.println(" tagStack[" + j + "] = "+ tagStack[j].gidText);
          if (tag.gidText.equals(tagStack[j].gidText)) {
            /* (1) Merge tags to create element, with content;
             * (2) Assign all intervening tags on stack to SGMLEMPTYELMNT;
             * (3) Pop all tags on stack up until j. */
            balancedStackPtr = j;
            balancedElement = new SgmlElement(tagStack[j], tag);
            //System.err.println("balanceSgmlTags: balanced tags " +
            //                 balancedElement.openTag); 
            if (j == 0) {
              //System.err.println("  adding to root elements: "+
              //               tagStack[j].getGid());
              rootElement.children.add (balancedElement);
            }
            tagStack[j] = null;

            /* Add postponed children (on sgmlTag) to this sgmlElement. */
            balancedElement.children.addAll(balancedElement.openTag.orphans);
            balancedElement.openTag.orphans.clear();

            /* Establish contained relationship to future sgmlElement (on sgmlTag). */
            if (j > 0)
              tagStack[j-1].orphans.add(balancedElement);

            /* If there are any intervening tags; treat them as empty tags */
            if (j < stackPointer) {
              // maintain order
              for (int k = j+1; k <= stackPointer; k++) {
                emptyElement = new SgmlElement(tagStack[k]);
                // intervening elements and elements postponed on them are
                // added to balanced element
                balancedElement.children.add(emptyElement);
                balancedElement.children.addAll(emptyElement.openTag.orphans);
                emptyElement.openTag.orphans.clear();
                if (debugLevel >= 2) {
                  System.err.println ("balanceSgmlTags(a): forcing empty element: "+
                                    emptyElement.openTag);
                }
                // intervening tags will never be rootElements!
                tagStack[k] = null;
              }
            }
            break;
          }
        }
        /* No matching tag was ever found; treat this as a rogue endtag. */
        if (balancedStackPtr == -1) {
          System.err.println("balanceSgmlTags: Dropping invalid closing tag: "
                             + tag);
        } else {
          stackPointer = balancedStackPtr - 1;
          //System.err.println(" -<<popping from stack (stackPointer = "+stackPointer+")");
        }
        
      } else if (tag.tagType == SgmlTag.Type.EMPTY ||
                 tag.tagType == SgmlTag.Type.DECLARATION ||
                 tag.tagType == SgmlTag.Type.COMMENT) {
        /* An empty tag gets added to list immediatly. */
        /* System.err.println("balanceSgmlTags: XML empty element " + tag.tagText); */
        emptyElement = new SgmlElement(tag);
        if (stackPointer >= 0) {
          //System.err.println("  adding as child to "+tagStack[stackPointer].getGid());
          tagStack[stackPointer].orphans.add(emptyElement);
        }
        
        if (stackPointer == -1)
          rootElement.children.add (emptyElement);

      } else {
        /* Should never get here... */
        System.err.println("balanceSgmlTags: Invalid tag type = " + tag);
      }
    }

    /* Any tags still on the stack must be treated as SGMLEMPTYELMNTs */
    for (int j = stackPointer; j >= 0; j--) {
      SgmlTag tag = tagStack[j];
      //System.err.println("balanceSgmlTags(b): forcing empty element"+
      //                 " (stackPointer = " + j + ")  " + tag);
      emptyElement = new SgmlElement(tag);
      tagStack[j] = null;
    }
    //System.err.println("Exiting balanceSgmlTags."); 
    balanced = true;
  }

  /* ******************************************************************* *
   * Write SGML file                                                     *
   * ******************************************************************* */

  public void writeSgml (Writer writer) throws IOException {
    if (DEBUG > 1)
      System.err.println("SGMLdoc.writeSgml signal chars = " + 
                         textSignalBuffer.length());
    if (DEBUG > 2)
      System.err.println("SGMLdoc.writeSgml signal = " + 
                         textSignalBuffer.toString());
    rootElement.writeSgml (writer, textSignalBuffer.toString());
    writer.flush ();
  }
  
    
  /* ******************************************************************* *
   * getSignalText                                                       *
   * ******************************************************************* */

  /**
   * Get the raw text of the entire document.
   *
   * @throws IllegalStateException if tags have not yet been parsed.
   */
  public String getSignalText () {
    if (textSignalBuffer == null)
      throw new IllegalStateException ("No tags parsed yet");
    return textSignalBuffer.toString ();
  }

  /**
   * Get the raw text of and individual SgmlElement.
   *
   * @throws IllegalStateException if tags have not yet been parsed.
   * @throws StringIndexOutOfBoundsException if the SgmlTags in the
   *    SgmlElement have offsets that are negative or greater than the length
   *    of the raw text. This should not happen if you only use elements
   *    parsed from since last calling parseSgmlTags.
   */
  public String getSignalText(SgmlElement sgmlElement) {
    if (textSignalBuffer == null)
      throw new IllegalStateException ("No tags parsed yet");
    
    if (sgmlElement.type == SgmlElement.Type.CONTENT) {
      return (textSignalBuffer.substring(sgmlElement.start, sgmlElement.end));
    } else {
      System.err.println("getSignalText: sgmlElement is not of type XMLCONTENTELMNT");
      return "";
    }
  }

  public int getStart () {
    return 0;
  }

  public int getEnd () {
    return textSignalBuffer.length();
  }

  /* ******************************************************************* *
   * Tag Dumping                                                         *
   * ******************************************************************* */

  public static final int PRINT_RECURSE    = 1;
  public static final int PRINT_PAD        = 2;
  public static final int PRINT_CLEAN      = 4;
  public static final int PRINT_TEXT       = 8;
  public static final int PRINT_DEBUG      = 16;
  public static final int PRETTY_PRINT     = PRINT_RECURSE | PRINT_PAD | PRINT_CLEAN;

  /** Count of elements */
  public int childCount () {
    return rootElement.childCount ();
  }

  /** @see SgmlElement#iterator() */
  public Iterator iterator () {
    return rootElement.iterator ();
  }
  
  /** Pretty print all elements recursively to specified print writer. */
  public void printElements (PrintWriter writer) throws IOException {
    printElements (writer, PRETTY_PRINT);
  }
  
  /** Print all top level elements to specified print writer using flags. */
  public void printElements (PrintWriter writer, int flags) throws IOException {
    Iterator iter = rootElement.children.iterator();
    while (iter.hasNext()) {
      printElement (writer, (SgmlElement) iter.next(), flags);
    }
  }

  /** Print element to the specified print writer using flags. */
  public void printElement (PrintWriter writer, SgmlElement element, int flags) {
    printElement (writer, element, flags, "");
  }

  /** Called from printElement for recursively maintaining padding. */
  private void printElement (PrintWriter writer, SgmlElement element, int flags, String pad) {
    // TODO: doesn't clean up garbage on PRINT_CLEAN
    if ((flags & PRINT_PAD) > 0) writer.print(pad);

    if ((flags & PRINT_DEBUG) > 0)
      writer.println(element);
    else
      writer.println(element.openTag.toString());

    if ((flags & PRINT_TEXT) > 0) {
      if ((flags & PRINT_PAD) == 0) writer.print(pad);
      writer.println ("text: " + getSignalText(element));
    }

    if ((flags & PRINT_RECURSE) > 0) {
      Iterator iter = element.children.iterator();
      while (iter.hasNext()) {
        SgmlElement child = (SgmlElement)iter.next();
        printElement (writer, child, flags, pad+"  ");
      }
    }
  }

  /** This method has not been kept up to date */
  public static void main(String[] args) throws IOException {

    System.err.println("Running ParseSgml v." + VERSION + " Last Modified: " + LASTMOD+"\n");

    // constant to indicate stdin or stdout.
    String STD_IO = "-".intern();

    // Arguments
    int     help        = 0;
    String  sgmlArg     = null;
    String  outArg      = null;
    String  error       = null;
    List    outList     = new LinkedList ();

    String TEXT       = "text";
    String ATTRIBUTES = "attributes";
    String VERBOSE    = "verbose";
    String COPY       = "copy";
    String DEBUG      = "debug";
    
    // Parse command line arguments
    for (int i = 0; i < args.length; i++) {
      // use internalize so equality operator will work with STD_IO
      String argKey   = args[i].intern();
      String argValue = null;

      if (argKey == STD_IO || argKey.charAt(0) != '-') {
        if (sgmlArg != null) {
          error = "Invalid argument";
          break;
        }
        sgmlArg = argKey;
        continue;
      }
      
      // arg is a flag get argument if exists
      if ((i+1) < args.length) {
        argValue = args[i+1].intern();
        if (argValue.charAt(0) == '-' && argValue.length() > 1) {
          argValue = null;
        } else {
          i++;
        }
      }
      if (argKey.equals("-h")) {
        help = 1;
        break;
      } else if (argKey.equals("-help")) {
        help = 2;
        break;
      } else if (argKey.equals("-t")) {
        outArg = argValue;
        outList.add (TEXT);
      } else if (argKey.equals("-a")) {
        outArg = argValue;
        outList.add (ATTRIBUTES);
      } else if (argKey.equals("-v")) {
        outArg = argValue;
        outList.add (VERBOSE);
      } else if (argKey.equals("-c")) {
        outArg = argValue;
        outList.add (COPY);
      } else if (argKey.equals("-d")) {
        outArg = argValue;
        outList.add (DEBUG);
      } else {
        error = "Unrecognized flag "+argKey;
        break;
      }
    }
    
    // check basic conditions (remember we internalized arguments)
    if (sgmlArg == null)
      sgmlArg = STD_IO;
    if (sgmlArg != STD_IO) {
      if (outArg == null)
        outArg = sgmlArg+".out";
    }

    String h1= "\nUSAGE ParseSgml -h|-help\n";
    String h2= "      ParseSgml [in-sgml-file|-] [[-a -c -d -v -t] [out-file|-]\n";
    String x1= "          show cmd line args in simple or detailed form, and exit\n\n";
    String x2=("          read in-sgml and parse tags.  '-' indicates to use stdin.\n" +
               "          -t   write raw text to file, default is stdout or <in-sgml-file>.out\n" +
               "          -a   write tag structures out\n"+
               "          -v   write tag structures verbosly (all whitespace included)\n" +
               "          -d   write tag elements debuging information\n\n" +
               "          -c   copy the sgml document.\n"+
               "          If input is stdin and no output is specified,\n"+
               "              the input is parsed, with no output (use to check for errors)\n" +
               "          When multiple flags are specified for output, they are separated by\n" +
               "              a delimiting line of '=====' chars\n" +
               "          -t,-a & -v arbuments may be specified in any order, but cannot be\n" +
               "              combined (as in -tav)\n");
    
    if (error != null) {
      System.err.println (h1 + h2 + error);
      System.exit (-1);
    }
    if (help == 1) {
      System.err.println (h1 + h2);
      return;
    }
    if (help == 2) {
      System.err.println (h1 + x1 + h2 +x2);
      return;
    }
    
    // temp I/O streams
    InputStream  in;
    OutputStream out;

    // real I/O
    Reader sgmlIn  = null;
    Writer writer = null;
  
    /* All these throw exceptions if files can't be found */

    // always read input
    if (sgmlArg == STD_IO) {
      in = System.in;
    } else {
      in = new FileInputStream (sgmlArg);
    }
    sgmlIn = new BufferedReader (new InputStreamReader(in));

    // text output
    if (outArg != null) {
      if (outArg == STD_IO) {
        out = System.out;
      } else {
        out = new FileOutputStream (outArg);
      }
      writer = new BufferedWriter(new OutputStreamWriter(out));
    }
    
    // parse the document!
    SgmlDocument sgmlDocument = new SgmlDocument(sgmlIn);
    //sgmlDocument.createEmptyTag (0,"foo",true);
    //sgmlDocument.createEmptyTag (4,"out", false);
    //sgmlDocument.createEmptyTag (4,"in", true,false);
    //sgmlDocument.createCommentTag (2,"foo", false);
    //sgmlDocument.createDeclarationTag (2,"?WHAT!!!"," oh my goodness?", false);
    /*
    sgmlDocument.createContentTag(2,2,"a",true);
    sgmlDocument.createContentTag(2,2,"b",true);
    sgmlDocument.createContentTag(2,2,"c",false);
    
    sgmlDocument.createContentTag(2,4,"d",false);
    sgmlDocument.createContentTag(2,4,"e",true);
    sgmlDocument.createContentTag(2,4,"f",false);

    sgmlDocument.createContentTag(4,4,"g",true);
    sgmlDocument.createContentTag(4,4,"h",true);
    sgmlDocument.createContentTag(4,4,"i",false);
    */
    //sgmlDocument.createContentTag(4,5,"die",false);

    
    // write the output
    if (writer != null) {
      Iterator iter = outList.iterator();
      while ( iter.hasNext ()) {
        Object o = iter.next();
        if (TEXT.equals(o))
          writer.write (sgmlDocument.getSignalText());
        else if (ATTRIBUTES.equals(o))
          sgmlDocument.printElements (new PrintWriter(writer), PRETTY_PRINT);
        else if (VERBOSE.equals(o))
          sgmlDocument.printElements (new PrintWriter(writer), PRETTY_PRINT ^ PRINT_CLEAN);
        else if (DEBUG.equals(o))
          sgmlDocument.printElements (new PrintWriter(writer), PRETTY_PRINT | PRINT_DEBUG);
        else if (COPY.equals(o))
          sgmlDocument.writeSgml (new PrintWriter(writer));
      }
      if (iter.hasNext())
        writer.write ("\n=================================================================\n\n");
    }

    // close the streams
    sgmlIn.close();
    if (writer != null)
      writer.close();
  }
}
