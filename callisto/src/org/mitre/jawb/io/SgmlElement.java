
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
 * 2003/05/17 Separated into class files (red)
 */

/** Generic SGML Element */
public final class SgmlElement {
    
  private static final int DEBUG = 0;

    /* Element types are in SgmlElement.Type class */
    /* Tag types are in SgmlTag.Type class */

    /* Object fields: */
    protected SgmlTag openTag;
    protected SgmlTag closeTag;
    protected int     start;
    protected int     end;
    protected Type    type;
    protected ArrayList children  = new ArrayList();
  //public ArrayList containingElements = new ArrayList();
  //public String  textContent;

    /* Here is the SgmlElement object constructor method for CONTENT elements. */
    public SgmlElement(SgmlTag startTag, SgmlTag endTag) {
	//System.err.println("Entering SgmlElement constructor (CONTENT): "+startTag);
	openTag  = startTag;
	closeTag = endTag;
	start    = openTag.tagCharOffset;
	end      = closeTag.tagCharOffset;
	type     = Type.CONTENT;
	// System.err.println("Leaving SgmlElement constructor");
    }

    /* Here is the SgmlElement object constructor method for EMPTY elements. */
    public SgmlElement(SgmlTag startTag) {
	//System.err.println("Entering SgmlElement constructor (EMPTY): "+startTag);
	openTag  = startTag;
	start    = end = openTag.tagCharOffset;
	if (startTag.tagType == SgmlTag.Type.OPEN) {
	    type     = Type.SGML_EMPTY;
	} else if (startTag.tagType == SgmlTag.Type.EMPTY) {
	    type     = Type.XML_EMPTY;
	} else {
	    type     = Type.UNKNOWN;
	}
	//System.err.println("Leaving SgmlElement constructor");
    }

  /** SgmlElement copy constructor that copies tags and attributes, not children */
  public SgmlElement (SgmlElement element) {
    openTag  = element.openTag;
    closeTag = element.closeTag;
    start    = element.start;
    end      = element.end;
    type     = element.type;
  }

  /**
   * SgmlElement as a Root element: no tags, but start = 0, end = length
   * specified.
   */
  protected SgmlElement(int length) {
    //System.err.println("Entering SgmlElement constructor (UNKNOWN)");
    start    = 0;
    end      = length;
    type     = Type.ROOT;
    //System.err.println("Leaving SgmlElement constructor");
  }


  /** Get the starting offset of this element */
  public int getStart () { return start; }

  /** Get the ending offset of this element */
  public int getEnd () { return end; }

  /** Get the opening tag of this element if one exists. */
  public SgmlTag getOpenTag () { return openTag; }
  
  /** Get the closing tag of this element if one exists. */
  public SgmlTag getCloseTag () { return closeTag; }

  
  /** Get the gid of this element */
  public String getGid () {
    return (openTag==null) ? null : openTag.getGid();
  }

  /** Children Elements of this node */
  public List getChildren () {
    return Collections.unmodifiableList (children);
  }

  /** Return count of all elements below this one. */
  public int childCount () {
    // TODO: this isn't very efficient at all :(
    int count = 0;
    Iterator iter = iterator ();
    while (iter.hasNext ()) {
      iter.next ();
      count++;
    }
    return count;
  }

  /**
   * Iterator which will walk over all elements below this node, in order. You
   * do not need to call 'iterator()' on the elements returned. Adding
   * elements to the elements returned by the iterator will cause a
   * ConcurrentModificationException as would the iterator from an {@link
   * AbstractList}. The remove method is not supported.
   */
  public Iterator iterator () {
    return new Iterator () {
        Iterator childIter = children.iterator ();
        Iterator subIter = null;
        public boolean hasNext () {
          if (subIter != null && subIter.hasNext())
            return true;
          subIter = null;
          return childIter.hasNext ();
        }
        public Object next () {
          if (subIter != null)
            return subIter.next ();
          SgmlElement child = (SgmlElement)childIter.next ();
          subIter = child.iterator ();
          return child;
        }
        public void remove () {
          throw new UnsupportedOperationException ("remove");
        }
      };
  }
  
  /**
   * Convience method to get attribute of openTag.
   * @see SgmlTag#putAttribute(String)
   */
  public String getAttribute (String attrName) {
    return openTag.getAttribute (attrName);
  }
  
  /**
   * Convience method to put attribute in openTag.
   * @see SgmlTag#putAttribute(String)
   */
  public String putAttribute (String attrName) {
    return openTag.putAttribute (attrName);
  }
  
  /**
   * Convience method to put attribute in openTag.
   * @see SgmlTag#putAttribute(String,String)
   */
  public String putAttribute (String attrName, String attrValue) {
    return openTag.putAttribute (attrName, attrValue);
  }
  
  /**
   * Convience method to remove attribute from openTag.
   * @see SgmlTag#removeAttribute(String)
   */
  public String removeAttribute (String attrName) {
    return openTag.removeAttribute (attrName);
  }

  /**
   * Convience method to get count of attributes in openTag.
   * @see SgmlTag#getAttributeCount
   */
  public int getAttributeCount () {
    return openTag.getAttributeCount();
  }

  /**
   * Convience method to iterator over the attributes in openTag.
   * @see SgmlTag#attributeIterator
   */
  public Iterator attributeIterator() {
    return openTag.attributeIterator();
  }
  
  /**
   * @param nested indicates that element should /not/ be considered
   *   contained if offsets are equal
   * @throws OverlappingElementException when overlap found.
   */
  protected boolean contains (SgmlElement element, boolean nested) {
    // empty tags can't contain anything!
    if (! (type == Type.ROOT || type == Type.CONTENT))
      return false;
    
    int eStart = element.start;
    int eEnd = element.end;
    boolean overlap = false;

    if (eStart < start) {
      if (start < eEnd  && eEnd < end)
        overlap = true;
      else
        return false;
    }
    if (eEnd > end) {
      if (end > eStart && eStart > start)
        overlap = true;
      else
        return false;
    }
    if (overlap) {
      throw new OverlappingElementException
        ("tag: ["+eStart+","+eEnd+
         "] overlaps ["+start+","+end+"]");
    }
    
    // elements always nest within ROOT
    if (type == Type.ROOT)
      return true;
    
    // empty tag hitting post, or matching extents, determined by 'nested'
    // flag //
    if ( ((eStart == eEnd) && (eStart == start || eEnd == end)) ||
         (eStart == start && eEnd == end) ) {
      return nested;
    }
    return true;
  }

  /**
   * @param element the element to add
   * @param nested true indicates that when encountering tags on the same
   *   extent, element should be nested within existing tags, otherwise, the
   *   existing tag will be nested within the new tag.
   * @throws OverlappingElementException when overlap found.
   */
  protected boolean addElement (SgmlElement element, boolean nested) {
    //System.err.println ("SgmlElement.addElement: parent: ("+start+","+end+
    //                    ") ["+hashCode()+"] type="+type);
    //System.err.println ("                       element: ("+element.start+","+element.end+
    //                    ") ["+element.hashCode()+"] type="+type);
    if (element == this)
      throw new IllegalArgumentException ("Attempt to insert element into itself: "+element);
    if (type != Type.ROOT || start == end)
      return false;
    if (element.start == element.end)
      return addEmptyElement (element, nested);
    else  
      return addContentElement (element, nested);
  }

  /**
   * Private so the decision is not left to your fingers.
   * @see #addElement(SgmlElment,boolean)
   */
  private boolean addEmptyElement (SgmlElement element, boolean nested) {
    boolean result = false;

    //System.err.println("Entering addEmptyElement ["+
    //                   element.start+","+element.end+"]"+element.openTag+
    //                   " w/ parent @ ["+start+","+end+"]"+openTag);
    if (this.contains (element, nested)) {
      //System.err.println("  --CHECKING...");
      // element definately goes in this tag.  if it isn't added to a child
      // be sure to add last
      int eStart = element.start;

      ListIterator childIter = children.listIterator ();
      while (childIter.hasNext ()) {
        SgmlElement child = (SgmlElement)childIter.next();
        //System.err.println("   checking child @ ["+
        //                   child.start+","+child.end+"]"+child.openTag);
        // definately before
        if (eStart < child.start) {
          childIter.previous ();
          childIter.add (element);
          result = true;
          //System.err.println("   --BEFORE");
          break;
        }
        // try within, this mechanism respects the 'nested' property
        if (eStart <= child.end) {
          if (child.addEmptyElement (element, nested)) {
            result = true;
            //System.err.println("   --ADDED TO CHILD");
            break;
          }
          else if (eStart == child.start && // surrounding content
                   eStart != child.end) {
            childIter.previous ();
            childIter.add (element);
            result = true;
            //System.err.println("   --POST - BEFORE");
            break;
          }
          // could be other tags at the same offset it want's to follow
        }
        // continue on...
      }
      if (! result) {
        //System.err.println("   --END");
        childIter.add (element);
      }
      result = true;
    }
    //System.err.println("... leaving addEmptyElement parent="+this);
    return result;
  }

  /**
   * Private so the decision is not left to your mistyping fingers: use addElement().
   * @see #addElement(SgmlElment,boolean)
   */
  private boolean addContentElement (SgmlElement element, boolean nested) {
    boolean result = false;

    if (DEBUG > 0)
      System.err.println("Entering addContentElement, parent="+openTag +
                         ", element= " + element.getGid());
    if (this.contains (element, nested)) {
      if (DEBUG > 0)
        System.err.println("  --CHECKING...");
      // element definately goes in this tag.  if it isn't added to a child
      // be sure to add last
      int eStart = element.start;
      int eEnd = element.end;
      boolean consuming = false;
    
      // walk through the kids, if element falls between two, add via
      // listIterator, if within one, recurse, if it straddles some, remove them
      // from this element, and add to the new element. If it overlapps, throw
      // an exception.
      ListIterator childIter = children.listIterator ();
      while (childIter.hasNext ()) {
        SgmlElement child = (SgmlElement)childIter.next();
        if (DEBUG > 1)
          System.err.println("   checking child "+child);
        // before
        if (eEnd <= child.start) {
          if (DEBUG > 1)
            System.err.println("   --BEFORE");
          childIter.previous ();
          childIter.add (element);
          result = true;
          break;
        }

        // try to consume the child w/ element adding, fixing exception
        try {
          if (element.contains (child, !nested)) {
            if (DEBUG > 0)
              System.err.println("   --CONSUMING");
            element.children.add(child);  // Add to end of children... assumes
                                          // there were none before entering!
            // do this second so failre above leaves us in a recoverable state.
            childIter.remove(); // removes child from this
            consuming = true;
            // delay adding to parent until all possible have been consumed
          }  
        } catch (OverlappingElementException x) {
          // The preveiously consumed elements are getting added back in
          // after the overlapping element instead of before it -- I think
          // doing a childIter.previous() here will fix that - Robyn 6/18/08
          childIter.previous();
          Iterator errorIter = element.children.iterator();
          while (errorIter.hasNext ()) {
            childIter.add (errorIter.next());
          }
          element.children.clear();
          throw x;
        }

        // try to nest within child
        if (! consuming && child.addContentElement (element, nested)) {
          if (DEBUG > 0)
            System.err.println("   --ADDED TO CHILD");
          result = true;
          break;
          
        } else {
          if (DEBUG > 0)
            System.err.println("   --NOT WITHIN");
        }
        // continue on...
      }
      if (result == false) {
        if (DEBUG > 0)
          System.err.println("   --END");
        childIter.add (element);
      }
      result = true;
    }
    if (DEBUG > 0)
      System.err.println("leaving addContentElement...");
    return result;
  }

  /**
   * Use semicolon-separated names of SgmlElement gid strings to identify
   * a unique embedded SgmlElement within this element.  Return the identified
   * SgmlElement or null if specified element not found.<p>
   * given:
   * <pre>
   * <entity_mention TYPE="NAME" ID="VOA19980622.1800.1295.sgm-7-1">
   *  <extent>
   *   <charseq>
   *    <start>539</start>
   *    <end>557</end></charseq></extent></entity_mention>
   * </pre>
   * and the pattern "<b>extent;charseq;start;</b>" this method will return the
   * SGML element that is represented by "<start>539</start>".  The second
   * argument, an SgmlElement, is presumed to have the proper recursive
   * containment relationships (via the children field).  If not,
   * a null pointer is returned.
   */
  public SgmlElement getEmbeddedElement(String embeddingPattern) {
    /* System.err.println("entering getEmbeddedElement: pattern = "
       + embeddingPattern + " this element = " + this); */
    boolean foundNextEmbeddedElementP;
    int gidStart = 0;
    int gidEnd   = embeddingPattern.indexOf(";",gidStart);
    if (gidEnd<1 && embeddingPattern.length()>0)
      gidEnd = embeddingPattern.length();
    //System.err.println(" (start,end)=("+gidStart+","+gidEnd+")");
    
    SgmlElement nextEmbeddedElement = this;
    while (gidEnd > 0) {
      foundNextEmbeddedElementP = false;
      /* Begin looping over pattern and sgml elements in parallel. */
      String gidString = embeddingPattern.substring(gidStart,gidEnd);
      // System.err.println(" gidString = " + gidString); 
      // check all child elements until first match is found.
      Iterator childrenIter = nextEmbeddedElement.children.iterator();
      while (childrenIter.hasNext() && !foundNextEmbeddedElementP) {
        SgmlElement element = (SgmlElement) childrenIter.next();
        // System.err.println(" contained gidString = "+element.getGid());
        if (element.openTag.gidText.equalsIgnoreCase(gidString)) {
          foundNextEmbeddedElementP = true;
	  // System.err.println(" found something.");
          nextEmbeddedElement = element;
        }
      }
      if (! foundNextEmbeddedElementP) {
        //System.err.println("exiting getEmbeddedElement: can't find target.");
        return null;
      }
      
      gidStart = gidEnd + 1;
      gidEnd   = embeddingPattern.indexOf(";",gidStart);
      if (gidEnd<1 && embeddingPattern.length()>gidStart)
        gidEnd = embeddingPattern.length();
      //System.err.println(" (start,end)=("+gidStart+","+gidEnd+")");
    }
    /*
    System.err.println("exiting getEmbeddedElement: element = "
                       + nextEmbeddedElement);
    if (nextEmbeddedElement != null) {
      System.err.println("exiting getEmbeddedElement: tag = "
                         + nextEmbeddedElement.openTag);
    }
    */
    return nextEmbeddedElement;
  }

  
  public void writeSgml (Writer writer, String docText) throws IOException {

    int last = start;
    if (openTag != null)
      writer.write (openTag.toString());

    if (DEBUG > 1)
      System.err.println ("writeSgml entered s="+start+" e="+end+
                          " g="+getGid());
    Iterator iter = children.iterator ();
    while (iter.hasNext ()) {
      SgmlElement child = (SgmlElement) iter.next();
      if (DEBUG > 1)
        System.err.println ("  last = " +last+ " child: s="+child.start+" e="+child.end+" g="+child.getGid());
      if (last < child.start) {
        if (DEBUG > 1)
          System.err.println("writeSgml writing text from " + last + " to " +
                             child.start + ": " + 
                             docText.substring(last, child.start));
        writer.write (docText.substring(last, child.start));
      }
      
      child.writeSgml (writer, docText);
      last = child.end;
    }
    if (last < end)
      writer.write (docText.substring(last, end));
    if (closeTag != null)
      writer.write (closeTag.toString());
    if (DEBUG > 1)
      System.err.println ("writeSgml exit g="+getGid());    
  }

  public String toString () {
    StringBuffer sb = new StringBuffer ("[SgmlElement: "+type);
    if (openTag != null)
      sb.append ("\n\topen  = ").append(openTag.dumpString());
    if (closeTag != null)
      sb.append ("\n\tclose = ").append(closeTag.dumpString());
    sb.append ("]");
    return sb.toString();
  }
    
  /***********************************************************************/
  /* Type safe enumeration                                               */
  /***********************************************************************/
  
  public static final class Type {
    public static final Type ROOT        = new Type ("ROOT_ELMNT");
    public static final Type UNKNOWN     = new Type ("UNKNOWN_ELMNT");
    public static final Type SGML_EMPTY  = new Type ("SGML_EMPTY_ELMNT"); /* <foo> (with no balancing </foo>)  */
    public static final Type XML_EMPTY   = new Type ("XML_EMPTY_ELMNT");  /* <foo/>                            */
    public static final Type CONTENT     = new Type ("CONTENT_ELMNT");    /* <foo> ... </foo>                  */
    
    private String type;
    private Type (String type) { this.type = type; }
    public String toString () { return type; }
  }
}

