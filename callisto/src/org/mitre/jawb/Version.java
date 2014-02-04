
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
package org.mitre.jawb;

import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 * Package level version file used to keep packaging and codebase in sync. The
 * file Version.java.in is filtered to create Version.java when Callisto is
 * built using <a href="http://ant.apache.org">ant</a>.  If you are not using
 * ant to build Callisto you can do this manually by copying Version.java.in
 * to Version.java, replacing "@ VERSION @" with the "version" property value
 * in the file build.xml.
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 */
public class Version {

  public static String VERSION   = "callisto.version";
  public static String COPYRIGHT = "callisto.copyright";
  
  public static String JRE_MIN_MAJOR = "callisto.jre.min.major";
  public static String JRE_MIN_MINOR = "callisto.jre.min.minor";
  public static String JRE_MIN_EDIT  = "callisto.jre.min.edit";
  
  /** Current version info */
  private static Properties versionInfo = null;

  private static String jreMinVersion = null;
  private static int jreMinMajor = 1;
  private static int jreMinMinor = 4;
  private static int jreMinEdit = 0;

  static {
    versionInfo = new Properties();
    try {
      InputStream in = Version.class.getResourceAsStream ("resources/version.info");
      versionInfo.load (in);
      in.close ();
      
      minJREVersion(); // prime these values

    } catch (Exception io) {
      System.err.println ("Unable to load version information.");
      io.printStackTrace ();
    }
  }

  /** Return the current version string. */
  public static String version() {
    return versionInfo.getProperty(VERSION, "-error-");
  }

  /** Return the copyright string. */
  public static String copyright() {
    return versionInfo.getProperty(COPYRIGHT, "-error-");
  }

  /** Return the minimum required jre. */
  public static String minJREVersion() {
    if (jreMinVersion == null) {
      jreMinMajor = Integer.parseInt(versionInfo.getProperty(JRE_MIN_MAJOR,"1"));
      jreMinMinor = Integer.parseInt(versionInfo.getProperty(JRE_MIN_MINOR,"4"));
      jreMinEdit  = Integer.parseInt(versionInfo.getProperty(JRE_MIN_EDIT,"0"));
      jreMinVersion = jreMinMajor+"."+jreMinMinor+"."+jreMinEdit;
    }
    return jreMinVersion;
  }

  /** Check for sufficient version of the JRE. */
  static boolean isJREValid () {
    int[] version = parseVersion(System.getProperty("java.version"));

    // assumes at "java.version" has at least major and minor 
    return (version[0] > jreMinMajor ||
            (version[0] == jreMinMajor && (version[1] > jreMinMinor ||
                 (version[1] == jreMinMinor &&
                  (version.length < 3 || (version[2] >= jreMinEdit))))));
  }

  private static int[] parseVersion(String v) {
    StringTokenizer st = new StringTokenizer(v, "._, \t|/\\;:");
    int[] version = new int[st.countTokens()];
    for(int i=0; i<version.length; i++) {
      try {
        version[i] = Integer.parseInt(st.nextToken(), 10);
      } catch(Exception e) {
        version[i] = -1;
      }
    }
    return version;
  }
}
