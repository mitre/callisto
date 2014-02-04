
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


package org.mitre.jawb.swing.event;

import java.util.Collections;
import java.util.EventObject;
import java.util.Set;

import org.mitre.jawb.swing.SetModel;

/**
 * Defines an event that encapsulates changes to a list.<p>
 */
public class SetDataEvent extends EventObject {

  /**
   * As long as we also have the final references in the SetDataEvent class,
   * this class need not be public. It's just implementation details.
   */
  private static class Type {
    public static final Object ADDED       = new Type("Added");
    public static final Object ADDED_SET   = new Type("Added Set");
    public static final Object REMOVED     = new Type("Removed");
    public static final Object REMOVED_SET = new Type("Removed Set");
    private String name;
    private Type (String name) { this.name = name; }
    public String toString () { return name; }
  }

  /** Identifies an event where one element was added */
  public static final Object ADDED       = Type.ADDED;
  /** Identifies an event where elements were added */
  public static final Object ADDED_SET   = Type.ADDED_SET;
  /** Identifies an event where one elmement was removed */
  public static final Object REMOVED     = Type.REMOVED;
  /** Identifies an event where elmements were removed */
  public static final Object REMOVED_SET = Type.REMOVED_SET;

  private Object type    = null;
  private Object element = null;
  private Set elements   = null;

  private SetDataEvent (Object source, Object type, Object element) {
    super (source);
    this.type    = type;
    this.element = element;
  }
  private SetDataEvent (Object source, Object type, Set elements) {
    super (source);
    this.type    = type;
    this.elements = Collections.unmodifiableSet (elements);
  }
  
  /** Create a SetDataEvent for added elements. */
  public static SetDataEvent createAddedEvent (Object source, Object element) {
    return new SetDataEvent (source, ADDED, element);
  }
  /** Create a SetDataEvent for added elements. */
  public static SetDataEvent createAddedSetEvent (Object source, Set elements){
    return new SetDataEvent (source, ADDED_SET, elements);
  }
  /** Create a SetDataEvent for removed elements. */
  public static SetDataEvent createRemovedEvent (Object source,Object element){
    return new SetDataEvent (source, REMOVED, element);
  }
  /** Create a SetDataEvent for removed elements. */
  public static SetDataEvent createRemovedSetEvent(Object source,Set elements){
    return new SetDataEvent (source, REMOVED_SET, elements);
  }
  
  /**
   * Returns the element added or removed from the model if the event is of
   * type ADDED or REMOVED, or null otherwise.
   */
  public Object element () { return element; }
  
  /**
   * Returns a Set of the annotations that were added or removed to the model
   * if the event is of type ADDED_SET or REMOVED_SET, or null otherwise. The
   * Set returned is unmodifiable.
   */
  public Set elementSet () { return elements; }

  /**
   * Returns the event type. The possible values are:
   * <ul>
   *   <li>{@link #ADDED}
   *   <li>{@link #ADDED_SET}
   *   <li>{@link #REMOVED}
   *   <li>{@link #REMOVED_SET}
   * </ul>
   *
   * @return an object representing the type of event
   */
  public Object getType () { return type; }

  /**
   * Returns a string representation of this ListDataEvent. This method 
   * is intended to be used only for debugging purposes, and the 
   * content and format of the returned string may vary between      
   * implementations. The returned string may be empty but may not 
   * be <code>null</code>.
   */
  public String toString () {
    StringBuffer sb = new StringBuffer ();
    sb.append ("[").append (getClass().getName());
    sb.append ("type=").append (type);
    if (type == ADDED || type == REMOVED)
      sb.append (" obj=").append(element);
    else
      sb.append (" set=").append (elements);
    sb.append ("]");
    return sb.toString();
  }
}
