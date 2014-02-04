
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
import gov.nist.atlas.type.RegionType;
import gov.nist.atlas.util.ATLASElementSet;

import java.beans.PropertyChangeEvent;

// TODO: all the java.io ought to go anyway @see PhraseTaggingAnnotation
import java.io.*;
import java.io.FileReader;
import java.io.File;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * This class of Annotation supports multiple text-extent regions,
 * plus optional simple string-valued attribute/value content<p>
 *
 * This implementation supports the following MAIA structure for
 * indicating multiple text extents:
 * <code>
 * &lt;RegionType name='text-extents'&gt;
 *   &lt;RegionType ref='text-extent' role='foo'/&gt;
 *   &lt;RegionType ref='text-extent' role='bar'/&gt;
 *   &lt;RegionType ref='text-extent' role='baz'/&gt;
 *   ...
 * &lt;/RegionType&gt;
 * </code>
 */
public class MultiPhraseAnnotation extends AWBAnnotationImpl 
    implements NamedExtentRegions {

    private static final int DEBUG = 0;

    protected MultiPhraseAnnotation(ATLASType type, ATLASElement parent, 
				    Id id, ImplementationDelegate delegate, 
				    AnnotationInitializer initializer) {
	super(type, parent, id, delegate, initializer);
    }

    /* The following methods implement the NamedExtentRegions interface */

    /** 
     * Returns an array of Strings naming the roles of the text extent
     * regions available for this Annotation.
     */
    public String[] getExtentNames () {
	Iterator rolesIter = 
	    getRegion().getRegionType().getDefinedRolesForSubordinates();
	ArrayList roles = new ArrayList();
	while (rolesIter.hasNext()) {
	    roles.add((String)rolesIter.next());
	}
	String[] rolesArray = new String[roles.size()];
	return (String[])roles.toArray(rolesArray);
    }

    public Id[] getExtentIds() {
	ATLASElementSet allRegions = getRegion().getAllRegions();
	Iterator allRegionsIter = allRegions.iterator();
	int numRegions = allRegions.size();
	Id[] ids = new Id[numRegions];
	int i=0;
	while (allRegionsIter.hasNext()) {
	    Region subRegion = (Region)allRegionsIter.next();
	    Id id = subRegion.getId();
	    ids[i++] = id;
	}
	return ids;
    }
    
    /** 
     * Returns the String that is the text in the Signal file
     * indicated by this Annotation's text-extent region.
     * Returns an empty string if any error occurs in trying to
     * access the text file or read the appropriate offsets.
     */
    public String getTextExtent (String role) {
      int start = getTextExtentStart(role);
      int end = getTextExtentEnd(role);
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
    public int getTextExtentStart (String name) {
      //System.err.println ("MultiPhrAnn.gTxtExtStart: extentName="+name);
      Region subregion = getSubregionWithRole (name);
      Anchor anchor = subregion.getAnchorWithRole("start");
      Parameter param = anchor.getParameterWithRole("char");
      String value = param.getValueAsString();
      return Integer.parseInt(value);
    }

    /** Sets the integer offset of this Annotation's text-extent
     * region's start anchor to the given value.  
     */
    public boolean setTextExtentStart (String name, int start) {
      Region region = getSubregionWithRole (name);
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
        firePropertyChange(new AnnotPropertyChange(this,
                                                   name+"."+TEXT_EXTENT_START,
                                                   oldValue, newValue));
      return result;
    }
    
    /** Returns the integer offset of this Annotation's text-extent
     * region's end anchor.
     */
    public int getTextExtentEnd (String name) {
      //System.err.println ("MultiPhrAnn.gTxtExtEnd: extentName="+name);
      Region subregion = getSubregionWithRole (name);
      Anchor anchor = subregion.getAnchorWithRole("end");
      Parameter param = anchor.getParameterWithRole("char");
      String value = param.getValueAsString();
      return Integer.parseInt(value);
    }

    /**
     * Sets the integer offset of this Annotation's text-extent
     * region's end anchor to the given value.
     */
    public boolean setTextExtentEnd (String name, int end) {
      Region region = getSubregionWithRole (name);
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
        firePropertyChange(new AnnotPropertyChange(this,
                                                   name+"."+TEXT_EXTENT_END,
                                                   oldValue, newValue));
      return result;
    }

  /**
   * Sets the integer offset of this Annotation's text-extent region's start
   * and end anchors to the given values.
   */
  public boolean setTextExtents (String name, int start, int end) {
    AWBCorpusImpl corpus = (AWBCorpusImpl)getDefiningCorpus();
    Region region = getSubregionWithRole (name);

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
    if (result)
      firePropertyChange(new AnnotPropertyChange(this, null, null, null));
    
    return result;
  }

  /**
   * Get's the region with specified role, lazily creating the region if
   * neccissary.<p>
   *
   * TODO: since anchored regions will always need to get some value
   * eventually, should there be a Jawb.newTextRegion which fills anchors to
   * -1 automatically?
   */
  protected Region getSubregionWithRole (String name) {
    Region subregion = getRegion().getRegionWithRole(name);

    if (subregion == null) {
      RegionType rtype = 
        (RegionType)getRegion().getRegionType().getTypeOfSubordinateWith(name);
      subregion = getJawb().newEmptyRegion(rtype);
      if (!getRegion().setRegionWithRole(subregion, name)) {
        throw new RuntimeException ("Cannot set subRegion role="+name+
                                    " to region: "+subregion+
                                    "\n\tin annotation: "+this);
      }
      
      // create default anchors with '-1' offsets here.
      // TODO: what if it the region is more complex than just a couple anchors?
      // TODO: should referr to some constant (static final..), not "-1"
      //      final String DEFAULT_LOCATION = "-1";

      AWBCorpusImpl corpus = (AWBCorpusImpl)getDefiningCorpus();
      Iterator roleIter =
        subregion.getRegionType().getDefinedRolesForSubordinates();
      while (roleIter.hasNext()) {
        String role = (String)roleIter.next();
        Anchor anchor = corpus.getAnchorByOffset(UNSPECIFIED_STRING);
        if (anchor == null) {
          anchor = getJawb().newTextAnchor(UNSPECIFIED_STRING);
        }
        subregion.setAnchorWithRole(anchor, role);
      }
    }
    return subregion;
  }

    /* The following methods extend AWBAnnotation to handle
     * named-extent-region-specific attribute keys */

    protected void initialize() {
	super.initialize();
	/* now initialize region keys */
	String[] regionNames = getExtentNames();
	for (int i=0; i<regionNames.length; i++) {
	    addRegionKey(regionNames[i] + "." + TEXT_EXTENT);
	    addRegionKey(regionNames[i] + "." + TEXT_EXTENT_START);
	    addRegionKey(regionNames[i] + "." + TEXT_EXTENT_END);
	}
    }

    public Class getAttributeType (String attributeKey) {
	String majorKey = attributeKey.substring(attributeKey.indexOf(".")+1);
	//System.out.println("MultiPhrAnn.gAttrType: attributeKey after . is " + majorKey);
	if (majorKey.equals(TEXT_EXTENT)) {
	    return java.lang.String.class;
	} else if (majorKey.equals(TEXT_EXTENT_START) ||
		   majorKey.equals(TEXT_EXTENT_END)) {
	    return java.lang.Integer.class;
	} else {
	    return super.getAttributeType(attributeKey);
	}
    }
	  
    public Object getAttributeValue (String attributeKey) {
	String[] keys = attributeKey.split("\\.",2);
	String name = keys[0];
        if (keys.length > 1) {
          String majorKey = keys[1];
          if (majorKey.equals(TEXT_EXTENT)) {
	    return getTextExtent(name);
          } else if (majorKey.equals(TEXT_EXTENT_START)) {
	    return new Integer(getTextExtentStart(name));
          } else if (majorKey.equals(TEXT_EXTENT_END)) {
	    return new Integer(getTextExtentEnd(name));
          }
          System.err.println ("MPA.gAttrVal: Unrecognized Key="+attributeKey);
          return null;
        }
        return super.getAttributeValue(attributeKey);
    }

    /* should I check here whether the value is an Integer before calling the
     * set methods? -- FIXME
     */
    public boolean setAttributeValue (String attributeKey, Object value)
	throws UnmodifiableAttributeException {
	String[] keys = attributeKey.split("\\.",2);
	String name = keys[0];
        if (keys.length > 1) {
          String majorKey = keys[1];
          if (majorKey.equals(TEXT_EXTENT)) {
	    throw new UnmodifiableAttributeException(attributeKey);
          } else if (majorKey.equals(TEXT_EXTENT_START)) {
	    return setTextExtentStart(name,((Integer)value).intValue());
          } else if (majorKey.equals(TEXT_EXTENT_END)) {
	    return setTextExtentEnd(name,((Integer)value).intValue());
          }
          System.err.println ("MPA.sAttrVal: Unrecognized Key="+attributeKey);
          return false;
        }
        return super.setAttributeValue(attributeKey, value);
    }
}
