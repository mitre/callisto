
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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class extends the <code>java.util.LinkedHashSet</code> class while
 * implementing  the {@link SetModel} interface.  It adds to the SetModel, the
 * stipulation that the elements in the notifications will always be returned
 * in the order which they were added to the model.
 */
public class LinkedHashSetModel extends AbstractSetModel implements Set {

  private static final int DEBUG = 0;
  
  private Set data = new LinkedHashSet ();
  
  public LinkedHashSetModel () {
    data = new LinkedHashSet ();
  }
  public LinkedHashSetModel (Collection c) {
    data = new LinkedHashSet (c);
  }
  public LinkedHashSetModel (int initialCapacity) {
    data = new LinkedHashSet (initialCapacity);
  }
  public LinkedHashSetModel (int initialCapacity, float loadFactor) {
    data = new LinkedHashSet (initialCapacity, loadFactor);
  }

  public boolean add (Object o) {
    if (DEBUG > 0)
      System.err.println ("LHSM.add ("+o+")");
    boolean success = data.add (o);
    if (success)
      fireAddedEvent (this,o);
    return success;
  }

  public void clear () {
    if (DEBUG > 0)
      System.err.println ("LHSM.clear");
    Set oldData = data;
    data = new LinkedHashSet ();
    fireRemovedSetEvent (this,oldData);
  }

  /* ?? public Object clone ().. */

  public boolean contains (Object o) {
    return data.contains (o);
  }

  public boolean containsAll (Collection c) {
    return data.containsAll (c);
  }

  public boolean isEmpty () {
    return data.isEmpty ();
  }

  /** Unmodifiable */
  public Iterator iterator () {
    if (DEBUG > 0)
      System.err.println ("LHSM.iterator");
    return Collections.unmodifiableSet (data).iterator ();
  }

  public boolean remove (Object o) {
    if (DEBUG > 0)
      System.err.println ("LHSM.remove ("+o+")");
    boolean success = data.remove (o);
    if (success)
      fireRemovedEvent (this, o);
    return success;
  }

  public int size () {
    return data.size ();
  }

  public Object[] toArray () {
    return data.toArray ();
  }
    
  public Object[] toArray (Object[] os) {
    return data.toArray (os);
  }


  /** This operation is expensive */
  public boolean addAll (Collection c) {
    if (DEBUG > 0)
      System.err.println ("LHSM.addAll ("+c+")");
    Set added = new LinkedHashSet (c);
    added.removeAll (data);
    boolean success = data.addAll (added);
    if (success)
      fireAddedSetEvent (this, added);
    return success;
  }

  /** This operation is expensive. */
  public boolean removeAll (Collection c) {
    if (DEBUG > 0)
      System.err.println ("LHSM.removeAll ("+c+")");
    Set removed = new LinkedHashSet (data);
    boolean success = data.removeAll (c);
    if (success) {
      removed.retainAll (c);
      fireRemovedSetEvent (this, removed);
    }
    return success;
  }

  /** This operation is expensive. */
  public boolean retainAll (Collection c) {
    if (DEBUG > 0)
      System.err.println ("LHSM.retainAll ("+c+")");
    Set removed = new LinkedHashSet (data);
    boolean success = data.retainAll (c);
    if (success) {
      removed.removeAll (c);
      fireRemovedSetEvent (this, removed);
    }
    return success;
  }
  
}
