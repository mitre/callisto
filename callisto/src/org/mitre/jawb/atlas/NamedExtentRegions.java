
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

/**
 * AWBAnnotation subclasses that implement this interface must also
 * provide "TextExtentNames" "TextExtent", "TextExtentStart" &
 * "TextExtentEnd" attributes, though setAttribute("TextExtent",
 * "whatever"); should throw an ImmodifiableAttributeException - FIXME
 */
public interface NamedExtentRegions extends AWBAnnotation {

    /** Region text key */
    public static final String TEXT_EXTENT = "TextExtent";

    /** Region start key */
    public static final String TEXT_EXTENT_START = "TextExtentStart";

    /** Region end key */
    public static final String TEXT_EXTENT_END = "TextExtentEnd";

    /** index to specify an unspecified anchor location */
    public static final int UNSPECIFIED = -1;

  /** string version of index to specify an unspecified anchor location */
  public static String UNSPECIFIED_STRING = "-1";



    /** 
     * Returns an array of Strings naming the text extent regions
     * available for this Annotation.
     */
    String[] getExtentNames ();

    /** 
     * Returns the String that is the text in the Signal file
     * indicated by this Annotation's text-extent region with the
     * provided name.
     */
    String getTextExtent (String name);

    /**
     * Returns the integer offset of the start anchor of this
     * Annotation's text-extent region with the provided name.
     */
    int getTextExtentStart (String name);

    /**
     * Sets the integer offset for the start anchor of this
     * Annotation's text-extent region's with the provided name to the
     * provided start value.  
     * @return true if the operation was successful, false otherwise.
     * @deprecated Use {@link #setTextExtents(String,int,int)} instead. See the
     * deprecation comment for {@link #setTextExtentEnd(String,int)}
     */
    boolean setTextExtentStart (String name, int start);

    /**
     * Returns the integer offset of the end anchor of this
     * Annotation's text-extent region with the provided name.
     */
    int getTextExtentEnd (String name);

    /**
     * Sets the integer offset for the end anchor of this Annotation's
     * text-extent region's with the provided name to the provided
     * start value.  
     * @return true if the operation was successful, false otherwise.
     * @deprecated Use {@link #setTextExtents(String,int,int)} instead.  If the
     * annotation is currently being displayed, setting the start or end could
     * cause a NullPointerException from within the Swing TextComponent.  If
     * getTextExtentEnd is ever less than getTextExtentStart, the error will
     * occur. Setting both at the same time (thereby triggering only one
     * change event) is more efficient anyway.
     */
    boolean setTextExtentEnd (String name, int end);

    /**
     * Sets offsets of this Annotations text-extent region for both start and
     * end anchors.
     * @return true if successful
     */
    boolean setTextExtents (String name, int start, int end);
}
