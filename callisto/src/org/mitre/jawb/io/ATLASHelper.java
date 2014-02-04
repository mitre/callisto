
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.gui.GUIUtils;
import org.mitre.jawb.tasks.Task;

/**
 * Utilities to help with ATLAS files. Note that the methods of this class are
 * <strong>not</strong> thread safe.
 */
public class ATLASHelper {

  public static final String SIGNAL_DATA = "CALLISTO::SIGNAL::DATA";
  public static final String SIGNAL_CHECKSUM = "CALLISTO::SIGNAL::CHECKSUM";
  public static final int DEBUG = 0;

  /** Set cleared and re-used on each call to {@link #getSupportingTasks} */
  private static List supportingTasks = null;
  /** SAX Handler that retrieves the URI of the MAIA Scheme of an .aif file */
  private static ATLASSaxHandler saxHandler = null;
  /** SAX parser that retrieves the URI of the MAIA Scheme of an .aif file */
  private static SAXParser saxParser = null;

  /**
   * Deterimine the Tasks which support the specified AIF file, based on the
   * MAIA schema it uses, and return references to those Tasks as a
   * List. Generally this will only return one of the tasks supplied (if
   * any!), but nothing restricts multiple Tasks from working with the same
   * MAIA scheme. If the file is not an AIF file, <code>null</code> is
   * returned to indicate such.<p>
   *
   * This method reuses the List object returned, so don't store reference to
   * it, and don't expect this to be thread safe.<p>
   *
   * @param aifURI the <i>absolute</i> URI of an aif file.  URI is used to
   *            ensure proper encoding, and to access the data it is
   *            converted to URL.
   *
   * @return A List of Task objects which support the specified AIF file. It
   *         may be empty if no Task support the AIF file, or
   *         <code>null</code> to indicate that the file is not an AIF
   *         file.
   *
   * @throws IllegalArgumentException If aifURI is null or relative (non-URL)
   * @throws IOException if there was an error retrieving the URL of the MAIA
   *                     Scheme from the .aif file
   *
   * @see URI#toURL
   */
  public static List getSupportingTasks (URI aifURI)
    throws IOException {

    if (aifURI == null || ! aifURI.isAbsolute ())
      throw new IllegalArgumentException ("AIF URI is not absolute: "+aifURI);

    if (saxHandler == null)
      initSAXHandler ();

    saxParse (aifURI.toString (), saxHandler);

    String maiaString = saxHandler.getMaiaURIString ();
    if (DEBUG > 0)
      System.err.println ("ATHelp.getSupTask: maia="+maiaString);
    if (maiaString == null) // invalid .aif file
      return null;

    supportingTasks.clear ();
    //    Task task = findTask (maiaString, EXTERNAL);
    //
    //if (task != null)
    //  supportingTasks.add (task);
    //
    //return supportingTasks;

    // RK 10/14/05 Actually find all tasks instead of just one
    return findTasks (maiaString, EXTERNAL);
  }

  public static void saxParse (String uriString, ATLASSaxHandler saxHandler)
    throws IOException {

    saxHandler.reset ();
    try { // Parse the input URI
      if (DEBUG > 0)
        System.err.println ("ATHelp.saxParse: parsing...");

      saxParser.parse (uriString, saxHandler);

    } catch (EndOfProcessingException eope) {
      // done in good form! (exception forces parser to quit early)

    } catch (SAXException sxe) {
      // Error generated by this application
      Exception  x = sxe;
      if (sxe.getException() != null)
        x = sxe.getException();
      throw new RuntimeException (sxe.getMessage (), x);
    }
  }

  /**
   * Read in an .aif file from the specified URI, and write it out with
   * localized refernces to the output stream.
   * @param aifURI location of input .aif file. <strong>MUST BE ABSOLUTE.</strong>
   * @param out stream that localized version of input is written to
   * @param cheatMap A map of undocumented values that we use in Callisto
   *                 to store data in the AIF which ATLAS won't.
   */
  public static void localize (URI aifURI, OutputStream out, Map cheatMap)
    throws IOException {
    if (DEBUG > 0)
      System.err.println ("ATHelp.localize: aifURI="+aifURI);

    Document doc = parse (aifURI);
    DocumentType doctype = doc.getDocType ();
    Element corpus = doc.getRootElement ();
    Element signal = getTextSignal(corpus);

    // Replace external ATLAS DTD reference w/ local reference
    URI localDTD = URLUtils.badURLToURI(Jawb.getResource ("aif.dtd"));
    doctype.setSystemID (localDTD.toString ());

    // Retrieve 'Cononical' MAIA Scheme and replace w/ local reference
    String maiaString = corpus.attributeValue ("schemeLocation");
    Task task = findTask (maiaString, EXTERNAL);
    if (task == null) {
      // it could be an old file that still has a local MAIA URL
      String escapedMaia = maiaString.replaceAll (" ","%20");
      if ( (task = findTask (escapedMaia, LOCAL)) == null)
        throw new RuntimeException ("Unrecognized Task: MAIA URI="+maiaString);
    }
    corpus.addAttribute ("schemeLocation", task.getLocalMaiaURI().toString ());

    // It's possible to /not/ have a text signal referenced
    if (signal != null) {
      // If signal is relative URI, convert to absolute,
      // resolving against .aif file
      String signalHREF = signal.attributeValue ("href");
      try {
        String path = aifURI.getRawPath ();
        URI aifBase = aifURI.resolve
          (path.substring (0, path.lastIndexOf('/')+1));
        URI signalURI = new URI (signalHREF);
        URI resolvedURI = aifBase.resolve (signalURI);

        if (DEBUG > 0) {
          System.err.println ("ATHelp.localize:\n        base= "+aifBase+
                              "\n      signal= "+signalURI+
                              "\n    resolved= "+resolvedURI);
        }
        signal.addAttribute ("href", resolvedURI.toString());

      } catch (URISyntaxException x) {
        System.err.println ("WARNING: aif file specifies invalid signal URI:"+
                            " not resolving:\n    aifURI=   "+aifURI+
                            "\n    signalURI="+signalHREF);
        System.err.println (x.getMessage ());
      }
      // ATLAS ignores encoding so use the cheats
      cheatMap.put ("encoding", signal.attributeValue ("encoding"));
      cheatMap.put ("mimeType", signal.attributeValue ("mimeType"));
      cheatMap.put (SIGNAL_CHECKSUM, signal.attributeValue ("checksum"));

      Element body = signal.element("body");
      if (body != null) {
        String signalEncoding = body.attributeValue("encoding");
        if (! "Base64".equalsIgnoreCase(signalEncoding))
          System.err.println("Unrecognized embeded signal encoding: '"+signalEncoding+"'");
        else {
          String embedded = body.getText();
          cheatMap.put(SIGNAL_DATA, Base64.decode(embedded));
        }
      }
    } // if (signal != null)

    dump (doc, out);
  }

  /**
   * Create a temp file with similar name, and in the same directory as the
   * specified URI. This is just a wrapper around {@link File#createTempFile}
   * with some added fluff. Prefix is the name of the 'file' URI, and the
   * suffix is the suffix of the input file or "~".
   *
   * @throws IllegalArgumentException if uri is not absolute or a 'file' URI
   */
  public static final File createTempFile (URI uri) throws IOException {
      return createTempFile (new File (uri));
  }

  /** @see #createTempFile (URI) */
  public static final File createTempFile (File base) throws IOException {

    String name = base.getName ();
    int extPos = name.lastIndexOf ('.');
    if (extPos < 0)
      extPos = name.length ();

    File tmp = File.createTempFile (name.substring (0,extPos),
                                    name.substring (extPos)+"~",
                                    base.getParentFile ());
    return tmp;
  }

  /**
   * Read in an .aif file from the specified URI, and write it out with
   * localized refernces to the output stream.
   * @param aifURI location of input .aif file. <strong>MUST BE ABSOLUTE.</strong>
   * @param out stream that localized version of input is written to
   * @param relativize rewrite the absolute signal URI as relative based on
   *                   input
   * @param cheatMap A map of undocumented values that we use in Callisto
   *                    to store data in the AIF which ATLAS won't.
   */
  public static void externalize (URI aifURI, OutputStream out,
                                  boolean relativize, Map cheatMap)
    throws IOException {
    if (DEBUG > 0)
      System.err.println ("ATHelp.externalize: aifURI="+aifURI);

    Document doc = parse (aifURI);
    DocumentType doctype = doc.getDocType ();
    Element corpus = doc.getRootElement ();
    Element signal = getTextSignal(corpus);

    // Replace local ATLAS DTD reference w/ external reference
    doctype.setSystemID ("http://www.nist.gov/speech/atlas/aif.dtd");

    // Replace local MAIA Scheme w/ external reference
    String maiaString = corpus.attributeValue ("schemeLocation");
    Task task = findTask (maiaString, LOCAL);
    if (task == null)
      System.err.println ("Unable to extern Maia: Unknown:\n  "+maiaString);
    else
      corpus.addAttribute ("schemeLocation", task.getMaiaURI().toString ());

    // It's possible to /not/ have a text signal referenced
    if (signal != null) {
      // Perhaps replace absolute URI with relative URI
      if (relativize) {
        String signalHREF = signal.attributeValue ("href");
        try {
          String path = aifURI.getRawPath ();
          URI aifBase = aifURI.resolve (path.substring
                                        (0, path.lastIndexOf ('/')+1));
          URI signalURI = new URI (signalHREF);
          URI relativeURI = aifBase.relativize (signalURI);

          if (DEBUG > 0) {
            System.err.println ("ATHelp.extern:\n      base= "+aifBase+
                                "\n      signal= "+signalURI+
                                "\n    relative= "+relativeURI);
          }
          signal.addAttribute ("href", relativeURI.toString());

        } catch (URISyntaxException x) {
          System.err.println ("WARNING: aif file specifies invalid signal URI:"+
                              " not relativizing:\n    aifURI=   "+aifURI+
                              "\n    signalURI="+signalHREF);
          System.err.println (x.getMessage ());
        }
      }

      // ATLAS ignores encoding so use the cheats
      signal.addAttribute ("encoding", (String)cheatMap.get ("encoding"));
      signal.addAttribute ("mimeType", (String)cheatMap.get ("mimeType"));

      if (cheatMap.get (SIGNAL_DATA) != null) {
        String embedded = Base64.encode((byte[])cheatMap.get(SIGNAL_DATA));
        Element body = signal.addElement("body");
        body.addAttribute("encoding", "Base64");
        body.addText(embedded);
      }
    } // if (signal != null)

    dump (doc, out);
  }

  /**
   * Returns signal element who's type is "text". May return null.
   */
  private static Element getTextSignal(Element corpus) {
    Element signal = null;

    // Do not assume there is only one signal
    Iterator signalElts = corpus.elementIterator("SimpleSignal");
    while (signalElts.hasNext()) {
      // find the signal with type attribute 'text'
      Element signalElt = (Element) signalElts.next();
      String signalName = signalElt.attributeValue("type");
      if (signalName.equalsIgnoreCase("text")) {
        signal = signalElt;
        break;
      }
    }
    // TODO: if there is no signal with type 'text', we're in trouble...

    return signal;
  }

  public static Document parse (URI aifURI) throws IOException {

    SAXReader reader = new SAXReader();
    // actually, this is ok, if we use the entity resolver
    reader.setEntityResolver (new ATLASResolver ());
    reader.setIncludeExternalDTDDeclarations (false);

    try {
      // URI.toURL() fails when opaque. don't expect an opaque here, but...
      if (DEBUG > 0)
        System.err.println ("ATHelp.parse: aifURI="+aifURI);
      return reader.read(new URL (aifURI.toString ()));
    } catch (DocumentException x) {
      IOException ex = new IOException ("Unable to parse input aif");
      ex.initCause (x);
      throw ex;
    }
  }

  public static Document parse (InputStream in) throws IOException {

      SAXReader reader = new SAXReader();
      // actually, this is ok, if we use the entity resolver
      reader.setEntityResolver (new ATLASResolver ());
      reader.setIncludeExternalDTDDeclarations (false);

      try {
	  // URI.toURL() fails when opaque. don't expect an opaque here, but...
	  return reader.read(in);
      } catch (DocumentException x) {
	  IOException ex = new IOException ("Unable to parse input aif");
	  ex.initCause (x);
	  throw ex;
      }
  }

  public static final boolean EXTERNAL = true;
  public static final boolean LOCAL = false;


  /** Look up a task by the specified MAIA URI, checking against the Tasks
   * advertized MAIA URI: either external (cononical) or local, as specified
   */
  public static Task findTask (String uri, boolean external) {
    if (DEBUG > 1)
      System.err.println ("ATHelp.findTask: finding task (" +
                          (external?"EXT":"LOCAL") + ")\n  "+uri);
    // O(n)... just hope you don't have that many tasks. If it get's to be a
    // real issue, we'll use a hash tree
    Iterator iter = Jawb.getTasks ().iterator ();
    while (iter.hasNext ()) {
      Task task = (Task) iter.next ();
      URI taskURI = external ? task.getMaiaURI() : task.getLocalMaiaURI ();
      if (taskURI.toString ().equals (uri))
        return task;
    }
    return null;
  }

  public static List findTasks (String uri, boolean external) {
    List tasks = new LinkedList();
    if (DEBUG > 1)
      System.err.println ("ATHelp.findTasks: finding tasks (" +
                          (external?"EXT":"LOCAL") + ")\n  "+uri);
    // O(n)... just hope you don't have that many tasks. If it get's to be a
    // real issue, we'll use a hash tree
    Iterator iter = Jawb.getTasks ().iterator ();
    while (iter.hasNext ()) {
      Task task = (Task) iter.next ();
      URI taskURI = external ? task.getMaiaURI() : task.getLocalMaiaURI ();
      if (taskURI.toString ().equals (uri))
        tasks.add(task);
    }
    return tasks;
  }

  public static void dump (Document doc, OutputStream out)
    throws IOException {

    // Pretty print the document to System.out
    OutputFormat format = OutputFormat.createPrettyPrint ();
    format.setEncoding("US-ASCII");
    Writer writer = new OutputStreamWriter (out, "US-ASCII");
    XMLWriter xmlWriter = new XMLWriter (writer, format);
    xmlWriter.write (doc);
    xmlWriter.close ();
  }

  /***********************************************************************/
  /* Initialization */
  /***********************************************************************/

  private static void initSAXHandler () {
    if (DEBUG > 0)
      System.err.println ("ATHelp.initSAXHandler");

    supportingTasks = new LinkedList ();
    saxHandler = new ATLASSaxHandler ();

    try {
      // Use the default (non-validating) parser
      SAXParserFactory factory = SAXParserFactory.newInstance ();
      saxParser = factory.newSAXParser ();

      XMLReader xmlReader = saxParser.getXMLReader();
      xmlReader.setProperty ("http://xml.org/sax/properties/lexical-handler",
                             saxHandler);
    } catch (Exception x) {
      throw
        new RuntimeException ("Unable to create parser to Retrieve MAIA", x);
    }
    if (DEBUG > 0)
      System.err.println ("ATHelp.initSAXHandler: initialized");
  }

  public static void main (String args[]) throws Exception {

    //String uri = args[0];
    //Writer writer = new FileWriter ("C:/cygwin/tmp/regurgitation.aif");
    String inSpec = "file:/C:/cygwin/tmp/example.aif.xml";
    String outSpec = "file:/C:/cygwin/tmp/regurgitated.aif.xml";

    URI aifURI = new URI (inSpec);
    URI outURI = new URI (outSpec);
    OutputStream out = new FileOutputStream (new File (outURI));

    Map cheatMap = new HashMap ();
    //localize (aifURI, out, cheatMap);
    externalize (aifURI, out, true, cheatMap);
    out.close ();

    List supporting = getSupportingTasks (outURI);
    System.err.println ("Supporting Tasks: "+supporting);
  }

/**
   * Read in an .aif file from the specified URI, and write it out with
   * localized refernces to the output stream.
   * @param aifURI location of input .aif file. <strong>MUST BE ABSOLUTE.</strong>
   * @param out stream that localized version of input is written to
   * @param relativize rewrite the absolute signal URI as relative based on
   *                   input
   * @param cheatMap A map of undocumented values that we use in Callisto
   *                    to store data in the AIF which ATLAS won't.
   */
  public static void externalize (URI aifURI, InputStream in, OutputStream out,
                                  boolean relativize, Map cheatMap)
    throws IOException {
//    if (DEBUG > 0)
//      System.err.println ("ATHelp.externalize: aifURI="+aifURI);

    Document doc = parse (in);
    DocumentType doctype = doc.getDocType ();
    Element corpus = doc.getRootElement ();
    Element signal = getTextSignal(corpus);

    // Replace local ATLAS DTD reference w/ external reference
    doctype.setSystemID ("http://www.nist.gov/speech/atlas/aif.dtd");

    // Replace local MAIA Scheme w/ external reference
    String maiaString = corpus.attributeValue ("schemeLocation");
    Task task = findTask (maiaString, LOCAL);
    if (task == null)
      System.err.println ("Unable to extern Maia: Unknown:\n  "+maiaString);
    else
      corpus.addAttribute ("schemeLocation", task.getMaiaURI().toString ());

    // It's possible to /not/ have a text signal referenced
    if (signal != null) {
      // Perhaps replace absolute URI with relative URI
      if (relativize) {
        String signalHREF = signal.attributeValue ("href");
        try {
          String path = aifURI.getRawPath ();
          URI aifBase = aifURI.resolve (path.substring (0, path.lastIndexOf ('/')+1));
          URI signalURI = new URI (signalHREF);
          URI relativeURI = aifBase.relativize (signalURI);

          if (DEBUG > 0) {
            System.err.println ("ATHelp.extern:\n      base= "+aifBase+
                                "\n      signal= "+signalURI+
                                "\n    relative= "+relativeURI);
          }
          signal.addAttribute ("href", relativeURI.toString());

        } catch (URISyntaxException x) {
          System.err.println ("WARNING: aif file specifies invalid signal URI:"+
                              " not relativizing:\n    aifURI=   "+aifURI+
                              "\n    signalURI="+signalHREF);
          System.err.println (x.getMessage ());
        }
      }

      // ATLAS ignores encoding so use the cheats
      signal.addAttribute ("encoding", (String)cheatMap.get ("encoding"));
      signal.addAttribute ("mimeType", (String)cheatMap.get ("mimeType"));

      if (cheatMap.get (SIGNAL_DATA) != null) {
        String embedded = Base64.encode((byte[])cheatMap.get(SIGNAL_DATA));
        Element body = signal.addElement("body");
        body.addAttribute("encoding", "Base64");
        body.addText(embedded);
      }
    } // if (signal != null)

    dump (doc, out);
  }
}
