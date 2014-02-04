
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

package org.mitre.jawb.atlas;

import gov.nist.atlas.*;
import gov.nist.atlas.spi.ImplementationDelegate;
import gov.nist.atlas.impl.AnnotationInitializer;
import gov.nist.atlas.type.ATLASType;
import gov.nist.atlas.util.ATLASElementFactory;

import java.beans.PropertyChangeEvent;

// TODO: all the java.io ought to go anyway @see MultiPhraseAnnotation
import java.io.*;
import java.io.FileReader;
import java.io.File;


public class PhraseTaggingAnnotation extends AWBAnnotationImpl 
    implements TextExtentRegion {

    protected PhraseTaggingAnnotation(ATLASType type, ATLASElement parent, 
				Id id, ImplementationDelegate delegate, 
				AnnotationInitializer initializer) {
	super(type, parent, id, delegate, initializer);
    }
  
    /** 
     * Returns the String that is the text in the Signal file
     * indicated by this Annotation's text-extent region.
     * Returns an empty string if any error occurs in trying to
     * access the text file or read the appropriate offsets.
     */
    public String getTextExtent () {
      int start = getTextExtentStart();
      int end = getTextExtentEnd();
      AWBSimpleSignal signal =
        (AWBSimpleSignal)getJawb().getSignal();
      try {
	return signal.getCharsAt(start, end);
      } catch (IndexOutOfBoundsException e) {
	return "";
      }
    }

    /** Returns the integer offset of this Annotation's text-extent
     * region's start anchor.
     */
    public int getTextExtentStart () {
	return Integer.parseInt(getExtentRegion().getAnchorWithRole("start").getParameterWithRole("char").getValueAsString());
    }

    /** Sets the integer offset of this Annotation's text-extent
     * region's start anchor to the given value.  
     *
     * We may have other annotations pointing to this Anchor
     * so don't change its start parameter, change the whole Anchor being
     * referenced, by either finding one pointing to the right place or
     * creating a new one
     */
    public boolean setTextExtentStart (int start) {
      Region region = getExtentRegion();
      Integer newValue = new Integer (start);
      Integer oldValue = new Integer(region.getAnchorWithRole("start")
                                     .getParameterWithRole("char")
                                     .getValueAsString());
      
      String startStr = newValue.toString();
      AWBCorpusImpl corpus = (AWBCorpusImpl)getDefiningCorpus();

      Anchor startAnchor = corpus.getAnchorByOffset(startStr);
      if (startAnchor == null) {
	startAnchor = getJawb().newTextAnchor(startStr);
      }
      boolean result = region.setAnchorWithRole(startAnchor, "start");
      /* Note: We might not need this stuff anymore if we end up going with
       * the ATLAS event handling */
      if (result)
        firePropertyChange(new AnnotPropertyChange(this, TEXT_EXTENT_START,
                                                   oldValue, newValue));
      return result;
    }
    

    /** Returns the integer offset of this Annotation's text-extent
     * region's end anchor.
     */
    public int getTextExtentEnd () {
	return Integer.parseInt(getExtentRegion().getAnchorWithRole("end").getParameterWithRole("char").getValueAsString());
    }

    /**
     * Sets the integer offset of this Annotation's text-extent
     * region's end anchor to the given value.
     */
    public boolean setTextExtentEnd (int end) {
      Region region = getExtentRegion();
      Integer newValue = new Integer (end);
      Integer oldValue = new Integer(region.getAnchorWithRole("end")
                                     .getParameterWithRole("char")
                                     .getValueAsString());
      
      String endStr = newValue.toString();
      AWBCorpusImpl corpus = (AWBCorpusImpl)getDefiningCorpus();

      Anchor endAnchor = corpus.getAnchorByOffset(endStr);
      if (endAnchor == null) {
	endAnchor = getJawb().newTextAnchor(endStr);
      }
      boolean result = region.setAnchorWithRole(endAnchor, "end");
      /* Note: We might not need this stuff anymore if we end up going with
       * the ATLAS event handling */
      if (result)
        firePropertyChange(new  AnnotPropertyChange(this, TEXT_EXTENT_END,
                                                    oldValue, newValue));
      return result;
    }

  /**
   * Sets the integer offset of this Annotation's text-extent region's start
   * and end anchors to the given values.
   *
   * Other annotations may point to these Anchors so the anchor's parameter
   * isn't changed, rather a different anchor is referenced.
   */
  public boolean setTextExtents (int start, int end) {
    Region region = getExtentRegion();
    // in case of falure at endAnchor
    Anchor oldStartAnchor = region.getAnchorWithRole("start");
    Integer oldStart = new Integer(oldStartAnchor
                                   .getParameterWithRole("char")
                                   .getValueAsString());
    Anchor oldEndAnchor = region.getAnchorWithRole("end");
    Integer oldEnd = new Integer(oldEndAnchor
                                 .getParameterWithRole("char")
                                 .getValueAsString());
    Integer newStart = new Integer(start);
    Integer newEnd = new Integer(end);

    AWBCorpusImpl corpus = (AWBCorpusImpl)getDefiningCorpus();

    String startStr = Integer.toString (start);
    String endStr = Integer.toString (end);
    
    Anchor startAnchor = corpus.getAnchorByOffset(startStr);
    if (startAnchor == null) {
      startAnchor = getJawb().newTextAnchor(startStr);
    }
    boolean result = region.setAnchorWithRole(startAnchor, "start");
    
    Anchor endAnchor = corpus.getAnchorByOffset(endStr);
    if (endAnchor == null) {
        endAnchor = getJawb().newTextAnchor(endStr);
    }
    result |= region.setAnchorWithRole(endAnchor, "end");
    
    /* Note: We might not need this stuff anymore if we end up going with
     * the ATLAS event handling */
    
    // null name indicates multiple attribute changes (thus null old,new)
    // however, I really need to know what changed, so I'm going to return
    // TEXT_EXTENTS with Integer start,end arrays for old and new
    // I think this coudl be a little fubar because if only one of the 
    // setAnchorWithRole actions above succeeds, one of the new values will
    // be wrong -- TODO fix that RK 8/13/09
    if (result)
      firePropertyChange(new AnnotPropertyChange(this, TEXT_EXTENTS, 
                                                 new Integer[] {oldStart,
                                                                oldEnd},
                                                 new Integer[] {newStart,
                                                                newEnd}));
    
    return result;
  }

  /**
   * get this Annotation's region; if necessary, set the start and end
   * Anchors to UNSPECIFIED_STRING
   */
  protected Region getExtentRegion() {
    Region region = getRegion();
    Anchor startAnchor = region.getAnchorWithRole("start");
    Anchor endAnchor = region.getAnchorWithRole("end");
    AWBCorpusImpl corpus = (AWBCorpusImpl)getDefiningCorpus();
    JAWB jawbObj = getJawb();
    if (startAnchor == null) {
      startAnchor = corpus.getAnchorByOffset(UNSPECIFIED_STRING);
      if (startAnchor == null) {
	startAnchor = jawbObj.newTextAnchor(UNSPECIFIED_STRING);
      }
      region.setAnchorWithRole(startAnchor, "start");
    }
    if (endAnchor == null) {
      endAnchor = corpus.getAnchorByOffset(UNSPECIFIED_STRING);
      if (endAnchor == null) {
	endAnchor = jawbObj.newTextAnchor(UNSPECIFIED_STRING);
      }
      region.setAnchorWithRole(endAnchor, "end");
    }
    return region;
  }

    /* The following methods extend AWBAnnotation to handle
     * text-extent-region-specific attribute keys */

    protected void initialize() {
	super.initialize();
	/* now initialize region keys */
	addRegionKey(TEXT_EXTENT);
	addRegionKey(TEXT_EXTENT_START);
	addRegionKey(TEXT_EXTENT_END);
    }

    public Class getAttributeType (String attributeKey) {
	if (attributeKey.equals(TEXT_EXTENT)) {
	    return java.lang.String.class;
	} else if (attributeKey.equals(TEXT_EXTENT_START) ||
		   attributeKey.equals(TEXT_EXTENT_END)) {
	    // must be a subclass of Object, so must use Integer here
	    // rather than int
	    return java.lang.Integer.class;
	} else {
	    return super.getAttributeType(attributeKey);
	}
    }
	  
    public Object getAttributeValue (String attributeKey) {
	if (attributeKey.equals(TEXT_EXTENT)) {
	    return getTextExtent();
	} else if (attributeKey.equals(TEXT_EXTENT_START)) {
	    return new Integer(getTextExtentStart());
	} else if (attributeKey.equals(TEXT_EXTENT_END)) {
	    return new Integer(getTextExtentEnd());
	} else {
	    return super.getAttributeValue(attributeKey);
	}
    }

    /* TODO: the property change events are not being created right. should
     * include: this(as source), name of property, old, and new value. --red
     * moved the named ones to their specialized methods, so it always get's
     * called.
     * 
     * should I check here whether the value is an Integer before calling the
     * set methods? -- FIXME
     */
    public boolean setAttributeValue (String attributeKey, Object value)
	throws UnmodifiableAttributeException {
	if (attributeKey.equals(TEXT_EXTENT)) {
	    throw new UnmodifiableAttributeException(attributeKey);
	} else if (attributeKey.equals(TEXT_EXTENT_START)) {
	    return setTextExtentStart(((Integer)value).intValue());
	} else if (attributeKey.equals(TEXT_EXTENT_END)) {
	    return setTextExtentEnd(((Integer)value).intValue());
	} else {
	    return super.setAttributeValue(attributeKey, value);
	}
    }
}
