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
import java.nio.charset.Charset;
import org.mitre.jawb.atlas.AWBDocument;

/**
 * Interface for tasks to define their own import methods for non AIF
 * annotation. Define a separate importer for each file format you wish to
 * handle.
 *
 * Importers should return the same string from {@link #toString} as from
 * {@link #getFormat}.  This is an implementation side effect in the import
 * dialog and may go away in the future. If this recommendation isn't
 * followed, your importer will just have a strange name in the dialog.
 */
public interface Importer {

  /**
   * A user readable String to identify the input format. This String should
   * also be returned from toString as it is used in GUI displays.
   */
  public String getFormat ();

  /**
   * Should return the same value from toString as from {@link #getFormat}.
   * This is an implementation side affect, and won't break anything, if not
   * Implementations which don't, will have a strange name in the dialog.
   */
  public String toString ();
  
  /**
   * A short description of the import function or <code>null</code>;
   */
  public String getDescription ();

  /**
   * Open and convert the data at the specified URI, to a new
   * AWBDocument. Implementations should verify that the URI specified is
   * absolute. URI's are used to avoid the ambiguities in the URL class.<p>
   *
   * Importers may choose to ignore the <code>charsetName</code> argument if
   * it conflicts with standards of the protocol, or meta information in the
   * document itself.
   * 
   * @see URI#isAbsolute
   * @param uri an absolute URI to import
   * @param encoding The name of a supported {@link Charset}.
   * @return an AWBDocument with all the data read in from the imported
   *         document.
   * @throws IOException on error reading the file.
   * @throws IllegalArgumentException implementations should throw an illegal
   *         argument exception if the uri specified is of an unsupported
   *         scheme, or if the preconditions of that scheme are not met (file:
   *         and http: protocols require the uri be absolute and heirarchal).
   * @throws UnsupportedEncodingException If the named charset is not supported
   */
  public AWBDocument importDocument (URI uri, String encoding) throws IOException;
}
