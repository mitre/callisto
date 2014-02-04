
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
import gov.nist.atlas.impl.AnnotationImpl;
import gov.nist.atlas.impl.AnnotationInitializer;
import gov.nist.atlas.ref.AnnotationRef;
import gov.nist.atlas.spi.ImplementationDelegate;
import gov.nist.atlas.type.ATLASType;
import gov.nist.atlas.type.AnnotationType;
import gov.nist.atlas.type.RegionType;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.CharConversionException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sun.io.Converters;

/**
 * This basic implementation of AWBAnnotation allows simple Attribute/Value
 * Parameter content, and role-identified subordinate annotations.
 */
public class AWBAnnotationImpl extends AnnotationImpl 
    implements AWBAnnotation {

  private static final Pattern ENCODED_ENTITY_PATTERN = Pattern.compile("&#(x?)([0-9a-z])+;", Pattern.CASE_INSENSITIVE);

  private static final int DEBUG = 0;

  /// HACK -- FIXME
  private JAWB jawbObj = null;

  public final void setJawb(JAWB jawb) {
    if (jawbObj == null) {
      jawbObj = jawb;
    } else {
      throw new IllegalStateException("setJawb may only be called once.");
    }
  }

  public final JAWB getJawb() {
    if (jawbObj == null) {
      throw new IllegalStateException("setJawb has never been called on this AWBAnnotation");
    } else {
      return jawbObj;
    }
  }

  /// end HACK

    private ArrayList parameterKeys;
    private ArrayList regionKeys;
    private HashMap possibleValues;
    // subordinateRoles is a subset of regionKeys consisting of those
    // regionKeys that refer to role-identified subordinates
    private HashSet subordinateRoles;

    /* Note: We might not need this stuff anymore
       if we end up going with the ATLAS event handling */
    private PropertyChangeSupport theListeners = 
	new PropertyChangeSupport(this);  /* the change listeners */
    /* IDs for our Annotation Modification Events */
    public static final String ChangedAnnotation = "ChangedAnnotation";

    /*
     * Creates an AWBAnnotation with the given information.
     *
     * This implementation creates an Annotation and then calls the
     * initialize method to initialize the paramerKeys and regionKeys.
     * Subclasses must call this initialze method first, then add any
     * subclass-specific additional region-based keys using the
     * addRegionKey() method.
     */
    protected AWBAnnotationImpl(ATLASType type, ATLASElement parent, 
				Id id, ImplementationDelegate delegate, 
				AnnotationInitializer initializer) {
	super(type, parent, id, delegate, initializer);
	if (DEBUG > 0)
	  System.out.println("AWBAnnot: called AnnotationImpl's constructor successfully for " + type.getName());
	initialize();
    }

    protected void initialize() {
	/* initialize parameter keys, if any */
	Iterator roleIterator = 
	    getContent().getContentType().getDefinedRolesForSubordinates();
	String role;
	if (DEBUG > 0)
	  System.out.println("AWBAnnot.init: got iterator");
	parameterKeys = new ArrayList();
	if (DEBUG > 0)
	  System.out.println("AWBAnnot.init: created empty parameterKeys");
	while (roleIterator.hasNext()) {
	    role = roleIterator.next().toString();
	    parameterKeys.add(role);
	    if (DEBUG > 0)
	      System.out.println("AWBAnnot.init: added role " + role);
	}
	/* initialize region keys */
	regionKeys = new ArrayList();
	if (DEBUG > 0)
	  System.out.println("AWBAnnot.init: created empty regionKeys");
	possibleValues = new HashMap();
	/* initialize subordinateRoles and add region keys corresponding
	 * to subordinate annotations */
	RegionType type = getRegion().getRegionType();
	Iterator rolesIter = type.getDefinedRolesForSubordinates();
	subordinateRoles = new HashSet();
	// TODO -- perhaps should forward back to Task?
	while (rolesIter.hasNext()) {
	    role = (String)rolesIter.next();
	    if (type.getTypeOfSubordinateWith(role) instanceof AnnotationType){
	      addRegionKey(role);
	      subordinateRoles.add(role);
	    }
	}
    }
 
    /**
     * Provides a list of the attributes an Annotation supports
     * This will include attributes relating to content and to region
     *
     * This method is final.  Subclasses may modify the set of
     * AtributeKeys that get returned by this method by adding
     * regionKeys in the initialize method.
     *
     * The set of parameterKeys that is returned may not be modified.
     * perhaps this does not allow a wide enough variety of conent
     * structures?? FIXME
     */
    public final String[] getAttributeKeys () {
	String[] attributeKeys = 
	    new String[parameterKeys.size()+regionKeys.size()];
	
	System.arraycopy(parameterKeys.toArray(), 0, attributeKeys, 0,
			 parameterKeys.size());
	System.arraycopy(regionKeys.toArray(), 0, 
			 attributeKeys, parameterKeys.size(),
			 regionKeys.size());
	return attributeKeys;	
    }

    /**
     * Returns a java Class object specifying the type of data an 
     * attribute contains. 
     *
     * Simple Parameter attributes must have String values, so this
     * base method always returns java.lang.String.class for the
     * default implementation
     *
     */
    public Class getAttributeType (String attributeKey) {
	if (subordinateRoles.contains(attributeKey)) {
	    // get it and ask for its class
	    // TODO -- what if it doesn't exist yet??
	  //        -- RegionType can give me AnnotationType and then
	  //        -- use Task.getAnnotationClass (through JAWB pointer?)
	  return getJawb().getTask().getAnnotationClass(getAnnotationType().getTypeOfSubordinateWith(attributeKey));
	    //return getRegion().getAnnotationWithRole(attributeKey).getClass();
	} else {
          return getJawb().getTask().getAttributeType(getAnnotationType(),
                                                      attributeKey);
	}
    }

    /**
     * Returns the value for the attribute specified. The returned
     * object can be safely cast to the class Type returned by the
     * getAttributeType method. Requests for the value of
     * unsupported keys will get a return value of null. Some keys may
     * return null as their value, so it's up to the widget to be sure
     * the key exists.
     */
    public Object getAttributeValue (String attributeKey){
	if (parameterKeys.contains(attributeKey)) {
          Parameter p = getContent().getParameterWithRole(attributeKey);
          String value = p.getValueAsString();
          if (value != null) {
            value = value.intern();
          }
          Class attributeType = getAttributeType(attributeKey);
          if (attributeType.equals(Boolean.class))
            return Boolean.valueOf(value);
          else if (attributeType.equals(String.class) && value != null) {
            Object decoded = DECODED_CACHE.get(value);
            if (decoded != null) {
              return decoded;
            }
            Matcher m = ENCODED_ENTITY_PATTERN.matcher(value);
            if (!m.find()) {
              DECODED_CACHE.put(value, value);
//              ENCODED_CACHE.put(value, value);
              return value;
            }
            StringBuffer sb = new StringBuffer();
            char[] buf = { 0 };
            do {
              int radix = 10;
              if (m.group(1).length() == 1) {
                radix = 16;
              }
              buf[0] = (char) Integer.parseInt(m.group(2), radix);
              String repl = new String(buf);
              m.appendReplacement(sb, repl);
            } while (m.find());
            m.appendTail(sb);
            decoded = sb.toString().intern();
            DECODED_CACHE.put(value, decoded);
            ENCODED_CACHE.put(decoded, value);
            return decoded;
          }
          else {
            return value;
          }
	} else if (subordinateRoles.contains(attributeKey)) {
	    Annotation subordinate =
	      getRegion().getAnnotationWithRole(attributeKey);
	    if (subordinate instanceof AnnotationRef) {
              if (DEBUG > 0)
                System.out.println("AWBAnnot.gAttrVal: attribute " + attributeKey +
                                   " is a Reference: dereferencing");
	      return ((AnnotationRef)subordinate).getElement();
	    } else {
	      return subordinate;
	    }
	} else {
	    return null;
	}
    }	       

    private static final Map DECODED_CACHE = Collections.synchronizedMap(new IdentityHashMap());
    private static final Map ENCODED_CACHE = Collections.synchronizedMap(new IdentityHashMap());
    
    /**
     * Changes the value of the specified attribute to the specified value
     * Returns true if the change is successful, and false otherwise.
     *
     * We might eventually wish to provide a means of checking if the
     * value is modifiable before trying to set it, and then changing
     * this to a void return value with the possibility of throwing
     * exceptions for unmodifiableAttributes and/or incorrectValueType 
     * and/or disallowedValue (see below)
     * 
     * May throw an invalidCastException (?) if the value passed in
     * cannot be cast to String, as Parameter values must be of type
     * String 
     * [In general, must be castable to the type returned by
     * getAttributeType(attributeKey)]
     */
    public boolean setAttributeValue (String attributeKey, Object value)
	throws UnmodifiableAttributeException {
        Object old = getAttributeValue (attributeKey);
	if (parameterKeys.contains(attributeKey)) {
	    /* Note: We might not need this anymore
	       if we end up going with the ATLAS event handling */
          Parameter p = getContent().getParameterWithRole(attributeKey);
          String encodedToString = getEncodedAttributeValue(attributeKey, value);
          boolean success = p.setValue(value==null ? (String)value :
                                       encodedToString);
          
          if (success) {
	    firePropertyChange(new AnnotPropertyChange (this, attributeKey,
                                                        old, value));
          }
	  if (DEBUG > 2)
	    System.err.println("AWBAnnotImpl.setAV returning success of " +
			       "parameter.setValue for " + attributeKey +
			       " from " + old + " to " + value + 
			       " success: " + success);

          return success;
	} else if (subordinateRoles.contains(attributeKey)) {
          boolean success =
            getRegion().setAnnotationWithRole((Annotation)value,
                                              attributeKey);
          if (success)
	    firePropertyChange(new AnnotPropertyChange (this, attributeKey,
                                                        old, value));
          return success;
	} else {
	  if (DEBUG > 2)
	    System.err.println ("AWBAnnotImpl.setAV returning false: " +
				attributeKey + " not in parameterKeys: " +
				parameterKeys.toString());
	  return false;
	}
    }

    String getEncodedAttributeValue(String attributeKey, Object value) {
      if (value == null)
        return null;
      Object encoded = value;
      if (value != null && value instanceof String && getAttributeType(attributeKey).equals(String.class)) {
        encoded = getEncodedValue(value);
      }
      return encoded != null ? encoded.toString() : value.toString();
    }

    static Object getEncodedValue(Object value) {
      Object encoded;
      String v = ((String) value).intern();
      encoded = ENCODED_CACHE.get(v);
      if (encoded == null) {
        String defaultEncodingName = "US-ASCII"; //Converters.getDefaultEncodingName();
        Charset charset = Charset.forName(defaultEncodingName);
        ByteBuffer testEncoded = charset.encode(v);
        CharBuffer testDecoded = charset.decode(testEncoded);
        String testDecodedString = testDecoded.toString();
        if (! v.equals(testDecodedString)) {
          char[] chars = v.toCharArray();
          char[] echars = testDecodedString.toCharArray();
          if (chars.length != echars.length) {
            throw new RuntimeException(new CharConversionException(v+".length ("+chars.length+") != "+testDecodedString+".length ("+echars.length+")"));
          }
          StringBuffer sb = new StringBuffer();
          for (int i = 0; i < chars.length; i++) {
            if (chars[i] == echars[i]) {
              sb.append(chars[i]);
            }
            else {
              sb.append("&#x").append(Integer.toHexString(chars[i])).append(";");
            }
          }
          encoded = sb.toString().intern();
        }
        else {
          encoded = v;
        }
        ENCODED_CACHE.put(value, encoded);
        DECODED_CACHE.put(encoded, value);
        if (DEBUG < 0) {
          System.err.println("AWBAnnotationImpl: caching encoded value "+value+" as "+encoded);
        }
      }
      return encoded;
    }
    
    /**
     * this method allows subclasses and special feature
     * implementations to add region keys.  This should have package
     * only access, but I had to make it public because I had to put
     * it in the AWBAnnotation Interface in order that special feature
     * implementations could refer to AWBAnnotation objects rather
     * than AWBAnnotationImpl objects, though maybe I should change
     * that -- FIXME
     */
    public final void addRegionKey(String key) {
	regionKeys.add(key);
    }

  /**
   * Returns a Set of all Annotation subordinates in this Annotation's
   * Region, or subRegions thereof that do not have any other
   * super-Annotations.
   */
  public final Set getExclusiveSubAnnotations() {
    HashSet exclusives = new HashSet();
    Iterator subsIter = getRegionSubAnnotations(getRegion()).iterator();
    while (subsIter.hasNext()) {
      Annotation candidate = (Annotation)subsIter.next();
      if (candidate.getReferentElements().size() == 1) {
	exclusives.add(candidate);
      }
    }
    return exclusives;
  }

  
  /*
   * returns a Set of all subAnnotations of this region,
   * including those found by recursing through subRegions.
   */
  private Set getRegionSubAnnotations(Region r) {
    HashSet subs = new HashSet();
    Iterator immediateSubsIter = r.getAllAnnotations().iterator();
    while (immediateSubsIter.hasNext()) {
      subs.add((AWBAnnotation)immediateSubsIter.next());
    }
    Iterator immediateRegionsIter = r.getAllRegions().iterator();
    while (immediateRegionsIter.hasNext()) {
      subs.addAll(getRegionSubAnnotations((Region)immediateRegionsIter.next()));
    }
    return subs;
  }

  /**
   * Returns a Set of all Regions that are subElements of this Annotation,
   * either directly or recursively as subRegions of this Annotation's Region.
   */
  public final Set getAllRecursiveRegions() {
    HashSet regions = new HashSet();
    Region mainRegion = getRegion();
    regions.add(mainRegion);
    regions.addAll(getRecursiveSubRegions(mainRegion));
    return regions;
  }

  /**
   * Recursively returns all subRegions of Region r
   */

  private Set getRecursiveSubRegions(Region r) {
    HashSet regions = new HashSet();
    Iterator directSubRegionsIter = r.getAllRegions().iterator();
    while (directSubRegionsIter.hasNext()) {
      Region subRegion = (Region)directSubRegionsIter.next();
      regions.add(subRegion);
      regions.addAll(getRecursiveSubRegions(subRegion));
    }
    return regions;
  }

  /** Returns the first annotation it finds of type superType that has 
   *  this annotation as a subordinate, or null if it does not find one
   */
  public AWBAnnotation getSuperAnnotation(AnnotationType superType) {

    Iterator referIterator = this.getReferentElements().iterator();
    while (referIterator.hasNext()) {
      ATLASElement referElement = (ATLASElement)referIterator.next();
      if (referElement instanceof Region) {
        if (DEBUG > 2) 
          System.err.println ("AWBAnnotImpl.getSuper found region: " +
                              referElement);
	Iterator refer2Iterator = 
	  ((Region)referElement).getReferentElements().iterator();
	while (refer2Iterator.hasNext()) {
	  ATLASElement possibleAnnot = 
	    (ATLASElement)refer2Iterator.next();
	  if (possibleAnnot instanceof AWBAnnotation &&
              ((AWBAnnotation)possibleAnnot).getAnnotationType().equals(superType)) {
            if (DEBUG > 2)
              System.err.println ("AWBAnnotImpl.getSuper " +
                                  "found and returning annotation: " +
                                  possibleAnnot);
	    return (AWBAnnotation)possibleAnnot;
	  }
	}
      }
    }
    // fall through and return null if none found
    if (DEBUG > 2)
      System.err.println("AWBAnnotImpl.getSuper found no parent annot");
    return null;
  }    

    /**
     * Name: addPropertyChangeListener
     * Purpose: Basic support for "something changed here"
     * @param l Listener to be added from Annotation
     */     
    public void addPropertyChangeListener(PropertyChangeListener l) {
	theListeners.addPropertyChangeListener(l);
    }
    
    /**
     * Name: removePropertyChangeListener
     * Purpose: Makes a PropertyChangeListener goes poof
     * @param l Listener to be removed from Annotation
     */    
    public void removePropertyChangeListener(PropertyChangeListener l) {
	theListeners.removePropertyChangeListener(l); 
    }

    /**
     * Name: removeAllPropertyChangeListeners
     * Purpose: Removes all the PropertyChangeListeners on this annotation
     */
    public void removeAllPropertyChangeListeners() {
      PropertyChangeListener[] pcl =
        theListeners.getPropertyChangeListeners();
      for (int i=0; i<pcl.length; i++)
	theListeners.removePropertyChangeListener (pcl[i]);
    }

    /**
     * Name: firePropertyChange
     * Purpose: Subclasses notify by calling this
     * @param e Event to be fired to listeners
     */    
    public void firePropertyChange(PropertyChangeEvent e) {
	theListeners.firePropertyChange(e); 
    }

  /**********************************************************************/
  /* PropertyChange/AnnotationChange implementations */
  /**********************************************************************/

  /**
   * Implementation of AnnotationChange that is only good for CHANGE
   * events. Extends PropertyChangeEvent so we can use the bean architecture
   * with more info.<p>
   *
   * TODO: What if the Annotation in question doesn't subclass
   * AWBAnnotationImpl (and implements AWBAnnotation from scratch)? I'd like
   * to see these as package private inner classes of a
   * 'DefaultAnnotationModel' class in this package.
   */
  protected static class AnnotPropertyChange extends PropertyChangeEvent
    implements AnnotationModelEvent.AnnotationChange {
    
    public AnnotPropertyChange (AWBAnnotation src, String propertyName,
                                Object oldValue, Object newValue) {
      super (src, propertyName, oldValue, newValue);
    }
    
    // getNewValue()...
    // getOldValue()...     :-D
    // getPropertyName()...
    
    public AWBAnnotation getAnnotation () {
      return (AWBAnnotation) getSource ();
    }
    public AWBAnnotation[] getAnnotationsInserted () { return null; }
    public AWBAnnotation[] getAnnotationsRemoved () { return null; }
    
  } // AnnotPropertyChange

  
  /**********************************************************************/

  /**
   * Implmentation of AnnotationChange that is only good for INSERT or REMOVE
   * events. Protected on purpose, since the only thing that will need to
   * instantiate one is an AWBAnnotation! Extends PropertyChangeEvent so we can
   * use the bean architecture with more info.<p>
   *
   * TODO: What if the Annotation in question doesn't subclass
   * AWBAnnotationImpl (and implements AWBAnnotation from scratch)? I'd like
   * to see these as package private inner classes of a
   * 'DefaultAnnotationModel' class in this package.
   */
  protected static class SubAnnotChange extends PropertyChangeEvent
    implements AnnotationModelEvent.AnnotationChange {
    
    private AWBAnnotation source;
    private AWBAnnotation[] subAnnots;
    private boolean insert;
    
    /** Makes a duplicate of the array, so feel free to reuse it */
    public SubAnnotChange (AWBAnnotation source, AWBAnnotation[] subAnnots,
                           boolean insert) {
      super (source, null, null, null);
      this.insert = insert;
      this.subAnnots = new AWBAnnotation[subAnnots.length];
      System.arraycopy (subAnnots, 0, this.subAnnots, 0, subAnnots.length);
    }
    
    // getNewValue()...
    // getOldValue()...     :-D
    // getPropertyName()...
    
    public AWBAnnotation getAnnotation () {
      return (AWBAnnotation) getSource ();
    }
    public AWBAnnotation[] getAnnotationsInserted () {
      if (insert)
        return subAnnots;
      return null;
    }
    public AWBAnnotation[] getAnnotationsRemoved () {
      if (! insert)
        return subAnnots;
      return null;
    }
    public boolean isInsert () { return insert; }
  } // end SubAnnotChange

  
}
