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


import java.io.IOException;
import java.net.URI;
import org.mitre.jawb.atlas.AWBDocument;

/**
 * Interface for tasks to define their own validation methods.
 *
 * Validators should return the same string from {@link #toString} as
 * from {@link #getFormat}.  This is an implementation side effect in
 * the validate dialog and may go away in the future. If this
 * recommendation isn't followed, your validator will just have a
 * strange name in the dialog.
 */
public interface Validator {

  /**
   * A user readable String to identify the Validator. This String should
   * also be returned from toString as it is used in GUI displays.
   */
  public String getFormat ();
  
  /**
   * Should return the same value from toString as from {@link #getFormat}.
   * This is an implementation side affect, and won't break anything, if not.
   * Implementations which don't, will have a strange name in the dialog.
   */
  public String toString ();
  
  /**
   * A short description of the validation function or <code>null</code>.
   */
  public String getDescription ();

  /**
   * Validate the given AWBDocument
   *
   * @param doc the data to be validate
   * @return true if the validate succeded, false otherwise.
   */
  public ValidationResult validateDocument (AWBDocument doc);
}
