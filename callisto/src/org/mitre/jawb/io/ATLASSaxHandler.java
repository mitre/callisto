
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

import java.io.IOException;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Only retrieves the urls of the AIF DTD, MAIA schema and signal used in an
 * .aif document.
 */
public class ATLASSaxHandler extends DefaultHandler
  implements LexicalHandler {

  public static final int DEBUG = 0;
  
  String dtdURIString;
  String maiaURIString;
  String signalURIString;
  EntityResolver resolver = new ATLASResolver ();
    
  public void setMaiaURIString (String uri) { maiaURIString = uri; }
  public String getMaiaURIString () { return maiaURIString; }

  public void setSignalURIString (String uri) { signalURIString = uri; }
  public String getSignalURIString () { return signalURIString; }
    
  public void setDTDURIString (String uri) { dtdURIString = uri; }
  public String getDTDURIString () { return dtdURIString; }

  public void reset () {
    setMaiaURIString (null);
    setDTDURIString (null);
    setSignalURIString (null);
  }
    
  /* Implmenting the LexicalHandler interface */
  public void comment(char[] ch, int start, int length) {}
  public void endCDATA() {}
  public void endDTD() {}
  public void endEntity(String name) {}
  public void startCDATA() {}
  public void startEntity(String name) {}


  /** get the location of the AIF DTD */
  public void startDTD (String name, String publicId, String systemId) {
    // ATLAS doesn't use the publicId, only the systemId. We want to get
    // this before resolving.
    if (DEBUG > 1)
      System.err.println ("ATLASHelp.startDTD: pub="+publicId+
                          " sys="+systemId);
    dtdURIString = systemId;
  }

  /** Handle External references offlint */
  public InputSource resolveEntity (String publicId, String systemId)
    throws SAXException {
    try {
      return resolver.resolveEntity (publicId, systemId);
    } catch (IOException x) {
      throw new SAXException ("Error resolving: "+systemId, x);
    }
  }

  /** Catch the Corpus and SimpleSignal elements and get the attributes */
  public void startElement(String namespaceURI,
                           String sName, // simple name
                           String qName, // qualified name
                           Attributes attrs)
    throws SAXException {
      
    String eName = sName;
    if ("".equals (eName)) eName = qName; // not namespaceAware

    if (eName.equals("Corpus")) {
      maiaURIString = attrs.getValue ("schemeLocation");
        
    } else if (eName.equals("SimpleSignal")) {
      signalURIString = attrs.getValue ("xlink:href");
      throw new EndOfProcessingException("Done.");
    }
  }
}
