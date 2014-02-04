
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

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import gov.nist.atlas.*;
import gov.nist.atlas.impl.SimpleSignalImpl;
import gov.nist.atlas.spi.ImplementationDelegate;
import gov.nist.atlas.type.ATLASType;

import org.mitre.jawb.io.SgmlDocument;
import org.mitre.jawb.io.SgmlElement;

/** 
 * This implementation of SimpleSignal caches a copy of the text
 * signal file in a String for quick access.  It implements a
 * getSubSignal method that uses that cached value.<p>
 *
 * Supported MIME types are are 'sgml', and 'plain'. Plain is the default when
 * MIME type is unrecognized, or unspecified.
 */
public class AWBSimpleSignal extends SimpleSignalImpl {

  private static final int DEBUG = 0;

  /** Flag to indicate that content is embedded in the ATLAS data */
  private byte[] signalData = null;
  /** Content after accounting for encoding and possible mime filtering. */
  private String signalContent = null;
  /** SimpleSignalImpl.encoding is private and ignored :( */
  private String myEncoding;
  /** SimpleSignalImpl.mimeType is private and ignored :( */
  private String myMimeType;
  /** Checksum for signal verification */
  private String md5sum;

  /** SGML tags that were dropped from the signal when reading. These tags may
   * change if the document is reread with a different encoding. */
  private List sgmlTags = null;

  private static final char[] HEX_CHAR = {
    '0' , '1' , '2' , '3' ,
    '4' , '5' , '6' , '7' ,
    '8' , '9' , 'a' , 'b' ,
    'c' , 'd' , 'e' , 'f' };
  
  protected AWBSimpleSignal(ATLASType type, ATLASElement parent, Id id,
                            ImplementationDelegate delegate, URL url,
                            String track) {
    super (type, parent, id, delegate, url, track);
    // don't read until neccissary, which will give time for Callisto hacks
    // to set the encoding and the actual signal if it was embedded.
  }

  private final static int ONE_KILO = 1024;
  /**
   * Allows lazy reading of signal, and re-reading when encoding changes.
   * Expects that the MIMEClass is "text", and honors "sgml" as a mime type
   * (ie. text/sgml) reading everything else as "plain" (ie. text aka
   * text/plain).<p>
   *
   * Now that we 'embed' the signal in AIF, this method will store the raw data
   * in a private variable for caching. 
   *
   * @see #setSignalData(byte[])
   * @see #encoding(String)
   * @see #setMIMEType(String)
   *
   * @throws IllegalStateException which wrappers all errors, including
   * "UnsupportedEncodingException" if the named charset is not supported, and
   * "IOException" when there are errors reading.
   */
  private void readSignal () {

    MessageDigest digest = null;
    InputStream iStream;
    Reader reader = null;

    try {
      // cache the raw stream (if it wasn't embedded or was reset)
      if (signalData == null) {
        if (DEBUG > 0)
          System.err.println ("Signal: Reading from URL");
        // read it into our local cache.
        iStream = getLocation ().openStream ();
        byte bytes[] = new byte [iStream.available()];
        iStream.read(bytes);

        signalData = bytes;
      } else {
        if (DEBUG > 0)
          System.err.println ("Signal: Reading from embedded data");
      }
      
      iStream = new ByteArrayInputStream(signalData);

      reader  = new InputStreamReader (iStream, getEncoding());
      reader  = new BufferedReader (reader);
      
      sgmlTags = null;
      
      if ("sgml".equalsIgnoreCase (getMIMEType())) {
        if (DEBUG > 0)
          System.out.println ("Signal: Reading SGML Data");

        // TODO: we could incorporate XSLT here for generic sgml import ability
        SgmlDocument sgmlDoc = new SgmlDocument(reader);

        LinkedList spamList = new LinkedList();
        for (Iterator i = sgmlDoc.iterator(); i.hasNext(); ) {
          SgmlElement element  = (SgmlElement) i.next();
          String gidText = element.getOpenTag().getGid();
          if (DEBUG > 0)
            System.err.println(" Remember: " +
                               element.getOpenTag()+" "+element.getCloseTag());
          // add a copy, sans children
          spamList.add (new SgmlElement (element));
        }
        sgmlTags = spamList;
        signalContent = sgmlDoc.getSignalText();
        
      } else {
        // can't use the 'new byte[x.available()]' trick since bytes != chars
        int len;
        char buf[] = new char[ONE_KILO];
        StringBuffer buffer = new StringBuffer(ONE_KILO);

        while ((len = reader.read(buf, 0, ONE_KILO)) != -1)
          buffer.append(buf, 0, len);
        signalContent = buffer.toString();
      }
    } catch (IOException e) {
      IllegalStateException x =
        new IllegalStateException("Unable to read signal");
      x.initCause(e);
      throw x;
    } finally {
      try {
        if (reader != null)
          reader.close ();
      } catch (IOException x) {}
    }
  }

  /**
   * Mechanism to get the sgml tags that were stripped if the document was
   * parsed as sgml.
   */
  List getSgmlTags() {
    if (signalContent == null)
      readSignal();
    return sgmlTags;
  }

  /**
   * Setting the actual data being annotated. Package private because it's
   * really a hack around something that would be better off built into
   * ATLAS. Remember that these are raw bytes, not characters, which are
   * differenent due to character encodings.<p>
   *
   * Specifying <code>null</code> will cause this signal to revert to
   * traditional means of getting data from the url.<p>
   *
   * Because converting between bytes and chars is not always a direct mapping,
   * we store both in memory.  If we could guarantee that we could convert
   * characters back to the raw bytes without modification we wouldn't need to
   * store both. The obvious side effect is an increase in runtime memory
   * required. The increase varies depending on encoding and charset: chars are
   * 4 bytes, many chars are encoded in 1 byte, though some in as many as 4.
   * Combining chars are not an issues since that is a display issue.
   *
   * @see #setEncoding(String)
   */
  void setSignalData (byte[] signalBytes) {
    if (signalData != signalBytes)
      signalContent = null;
    signalData = signalBytes;
  }

  /**
   * @see #setSignalData(byte[])
   */
  byte[] getSignalData () {
    return signalData;
  }
  
  /**
   * Retrieves the String value of the MD5 hash of the bytes in the signal as
   * returned by the MD5 algorithim for {@link MessageDigest}.
   */
  public String getDigest () {
    return md5sum;
  }

  /**
   * Specify the charset encoding of this stream. Method created and allowed
   * for use by other classes in package since jATLAS ignores encoding
   * completely, neither saving nor providing means of retrieving. When jATLAS
   * is fixed, we can remove this and refrences to it.<p>
   *
   * This method must be called to avoid getting an IllegalStateException from
   * the getCharsAt methods.
   *
   * @see Charset
   * @throws IOException if there is an error re-reading the signal
   * @throws UnsupportedEncodingException if the named charset is not
   * supported
   */
  protected void setEncoding (String encoding) throws IOException {
    if (myEncoding != encoding ||
        (myEncoding != null && !myEncoding.equals (encoding))) {
      signalContent = null; // signal will be reread as needed
    }
    myEncoding = encoding;
  }
  
  /**
   * Retrieves the encoding of this signal specified as a string name. The
   * string returned should retrieve a valid Charset object from {@link
   * Charset#forName(String)}. Overrides SimpleSignalImpl getEncoding, since
   * that will _always_ be <code>null</code> (according to the code we
   * have... encoding isn't used).
   * @see Charset
   */
  public String getEncoding () {
    return myEncoding;
  }

  /**
   * Specify the MIME type (not Class) this signal uses to interpret the
   * stream.  See this class' documentation for supported types.If the
   * specified MIME type is different that the previous type, and the signal
   * has already been read, it will be re-read using the new type.
   *
   * @see AWBSimpleSignal
   * @throws IOException if the signal is re-read and an error
   */
  protected void setMIMEType (String mimeType) throws IOException {
    String oldMimeType = myMimeType;
    if (oldMimeType != mimeType ||
        (oldMimeType != null && !oldMimeType.equals (mimeType))) {
      signalContent = null; // signal will be reread as needed
    }
    myMimeType = mimeType;
  }
  
  /**
   * Retrieves the MIME type (nont Class) of this signal specified as a string
   * name. See the this class' documentation for supported types. Overrides
   * SimpleSignalImpl getEncoding, since that will _always_ be
   * <code>null</code> (according to the code we have... MIMEType isn't used).
   * @see AWBSimpleSignal
   */
  public String getMIMEType() {
    return myMimeType;
  }

  /**
   * returns the characters of the Signal beginning at the given start
   * offset and ending before the given end offset. 
   *
   * @throws IndexOutOfBoundsException if start is negative,
   * or end is larger than the length of the signalContent String, or
   * start is larger than end.
   * @throws IllegalStateException if the signal is unable to be read. The
   * cause exception is wrapped in the IllegalStateException
   */
  public String getCharsAt(int start, int end)
    throws IndexOutOfBoundsException, IllegalStateException {
    if (signalContent == null)
      readSignal();
    return signalContent.substring(start, end);
  }

  /**
   * returns the characters of the Signal beginning at the given start
   * offset and ending at the end of the Signal text.
   *
   * @throws IndexOutOfBoundsException if start is negative.
   * @throws IllegalStateException if the encoding for this signal has not
   * been set.
   */
  public String getCharsAt(int start) 
    throws IndexOutOfBoundsException, IllegalStateException {
    if (signalContent == null)
      readSignal();
    return signalContent.substring(start);
  }

  private static String toHexString (byte[] b) {
    StringBuffer sb = new StringBuffer( b.length * 2 );
    for ( int i=0 ; i<b.length ; i++ ) {
      sb.append( HEX_CHAR[ ( b[i] & 0xf0 ) >>> 4 ] );
      sb.append( HEX_CHAR[ b[i] & 0x0f ] );
    }
    return sb.toString() ;
  } 
}
