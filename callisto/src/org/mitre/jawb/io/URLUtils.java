
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

package org.mitre.jawb.io;

import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
/*
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.Importer;
import org.mitre.jawb.tasks.Exporter;
*/

/**
 * Contains a bunch of methods common to gui components
 */
public final class URLUtils {

  /**
   * Converts a typical URL to URI, typical meaning it does not have properly
   * quoted spaces and other escape characters.
   *
   * @throws RuntimeException undeclared version of {@link URISyntaxException}
   * thrown upon error converting, which should never happen.
   */
  public static URI badURLToURI (URL url) {
    URI uri = null;
    try {
      uri = new URI (url.toString ().replaceAll (" ","%20"));
      /*
        uri = new URI (url.getProtocol(),
        url.getAuthority (),
        url.getPath (), url.getQuery(), url.getRef());
      */
    } catch (URISyntaxException e) {
      throw new RuntimeException ("Converting URL to URI", e);
    }
    return uri;
  }
  
  /**
   * If a URL is created from a URI with 'toURL' it will have properly quoted
   * characters, which this will convert it back to URI.
   *
   * @throws RuntimeException undeclared version of {@link URISyntaxException}
   * thrown upon error converting, which should only happen if the url
   * contains spaces (see {@link #badURLToURI(URL)}).
   */
  public static URI goodURLToURI (URL url) {
    URI uri = null;
    try {
      // uses the single arg constructor so URI doesn't further quote, already
      // quoted characters
      StringBuffer sb = new StringBuffer ();
      sb.append (url.getProtocol()).append (':');
      String tmp;
      tmp = url.getUserInfo();
      if (tmp != null)
        sb.append (tmp).append('@');

      tmp = url.getHost();
      if (tmp != null)
        sb.append (tmp);
      
      int p = url.getPort();
      if (p > -1)
        sb.append (':').append (tmp);
      
      tmp = url.getPath();
      if (tmp != null)
        sb.append (tmp);
      
      tmp = url.getQuery();
      if (tmp != null)
        sb.append ('?').append (tmp);
      
      tmp = url.getRef();
      if (tmp != null)
        sb.append ('#').append (tmp);
  
      uri = new URI (sb.toString());
      
    } catch (URISyntaxException e) {
      throw new RuntimeException ("Converting URL to URI", e);
    }
    return uri;
  }

  /**
   * Turns a URI into a URL with unescaped characters. This is a bit silly,
   * but broken java classes rely upon the spaces being in the URL, even
   * though it's not valid. URI.toURL() leaves spaces escaped, but this will
   * remove them.
   *
   * @throws RuntimeException undeclared version of MalformedURLException,
   * which should never happen.
   */
  public static URL uriToBadURL (URI uri) {
    if (!uri.isAbsolute ())
      throw new IllegalArgumentException("URI is not absolute");
    URL url = null;
    try {
      StringBuffer sb = new StringBuffer ();
      sb.append (uri.getScheme()).append (':');

      if (uri.isOpaque ())
        sb.append (uri.getSchemeSpecificPart ());
      else {
        String tmp;
        tmp = uri.getAuthority();
        if (tmp != null)
          sb.append (tmp);
        
        tmp = uri.getPath();
        if (tmp != null)
          sb.append (tmp);
        tmp = uri.getQuery();
        if (tmp != null)
          sb.append ('?').append (tmp);
        
        tmp = uri.getFragment();
        if (tmp != null)
          sb.append ('#').append (tmp);
      }
      url = new URL (sb.toString());
    } catch (MalformedURLException e) {
      throw new RuntimeException ("Converting URI to URL", e);
    }
    return url;
  }
}

