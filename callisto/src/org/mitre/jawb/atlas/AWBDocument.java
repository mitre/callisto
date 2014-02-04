
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

import gov.nist.atlas.type.AnnotationType;
import gov.nist.atlas.Analysis;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.atlas.AnnotationModelEvent.EventType;
import org.mitre.jawb.io.ATLASHelper;
import org.mitre.jawb.io.URLUtils;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.tasks.Task;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Container for all information associated with a document's data. Whereas
 * the JawbDocument maintains GUI information for a document, the AWBDocument
 * maintains no GUI information.<p>
 *
 * This class implements the AnnotationModel, and as such can be listened to
 * for insertion and removal of annotaitons from the document.
 *
 * <h5>ATLAS Workarounds</h5> 
 *
 * There are two issues that Callisto has with the ATLAS library:
 *
 * <ol><li>Opening files requires internet access, as there is no means of
 *     setting an entityresolver for the ATLAS DTD's, or the MAIA schemes.
 *
 *     <li> Signal locations are recorded as URL's: absolute, which includes
 *     full file paths.  Thus moving signal files from machine to machine, or
 *     to different locations on one machine will break .aif files.
 * </ol>
 *
 * This workaround is in two parts. Instead of hacking the ATLAS library, we
 * trick it, by modifiing the .aif files before opening and after saving.<p>
 *
 * Before opening, the .aif file is written to a temp file, with certain URLs
 * replaced. If the signal reference is relative, it's replaced with an
 * absolute one (resolved based on .aif location) since ATLAS expects it to be
 * absolute. Also, the MAIA DTD URL and MAIA scheme URL are replaced with URLs
 * to local copys so that network access isn't requred. The temp file is
 * deleted immidiately after it is used to open the .aif.  The temp file is
 * used only so that the original needn't be left in a bad state while
 * editing.<p>
 *
 * After saving, the output is also modified. ATLAS writes the local URLs for
 * the MAIA DTD and scheme, so those are replaced with cononical versions.
 * Also the signal URL may be replaced with a relative URI, if the .aif and
 * signal files are in the same directory.  The rules for when the signal
 * reference is made a URI may change to require user intervention.<p>
 *
 * @author <a href="mailto:laurel@mitre.org">Laurel D. Riek</a>
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */
public class AWBDocument implements AnnotationModel {
  
  /** Key to store the signals (ignored) sgml tags in the clientProperties */
  public static final String SGML_TAG_LIST_KEY = "sgmlTagList";
  
  private static final int DEBUG = 0; /*the level of debug messages */

  /** Set of listeners for global document events */
  private Set annotListeners = new HashSet ();

  /** assists in propogating change events */
  private AnnotationModelListener[] listArray = new AnnotationModelListener[0];

  /** Instance of inner class to propogate change events to annotListeners */
  private PropertyChangeListener annotListener = new AnnotChangeListener ();

  /** Client specific properties, not saved to AIF */
  private transient Map clientProperties;

  /** Reference to back-end ATLAS stuff */
  private JAWB jawb;

  /**
   * Deterimine the Tasks which support the specified AIF file, and return
   * references to them as a List. Generally this will be only one of the
   * tasks supplied (if any!), but nothing restricts multiple Tasks from
   * working with the same MAIA scheme. If the file is not an AIF file, null
   * is returned to indicate such.<p>
   *
   * This method reuses the List object returned, so don't store reference to
   * it, and don't expect this to be thread safe.<p>
   *
   * TODO: does this belong in the atlas stuff to let those object read the
   * file?
   *
   * @param aifURI an <i>absolute</i> URI for an aif file.  URI is used to
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
   *                     Scheme from the .aif file, or the 
   * @throws RuntimeException if there was an error reading the document,
   *                          which would also indicate a non AIF file
   */
  public static List getSupportingTasks (URI aifURI)
    throws IOException {
    
    // ATLASHelper looks directly to the JAWB.getTasks () methods for
    // tasks, so it's ignored
    return ATLASHelper.getSupportingTasks (aifURI);
  }
  
  /**
   * Factory method to load a previous annotations session from an AIF file
   * using the given task. If the AIF file is not suported by the task, an
   * exception is thrown.<p>
   *
   * <p>TODO: a special exception may be in order for when task
   * doesn't support the aif file. probably not checked though since it
   * <i>should</i> be avoidable using {@link #getSupportingTasks}
   *
   * @param uri an <i>absolute</i> URI for an aif file.  URI is used to
   *            ensure proper encoding, and to access the data it is
   *            converted to URL.
   * @param task resources needed to create and manipulate the AIF file.
   *
   * @throws something when the task does not support the aif file
   * @throws IOException when there are errors loading the AIF file.
   * @throws IllegalArgumentException If the AIF URI is not absolute
   * @throws MalformedURLException If a protocol handler for the URL could
   *         not be found, or if some other error occurred while
   *         constructing a URL from the AIF URL
   */
  public static AWBDocument fromAIF (URI uri, Task task)
    throws IOException, MalformedURLException {
    
    if (DEBUG > 0)
      System.err.println ("AWBDoc.fromAIF: uri="+uri+" task="+task.getName());

    // See the javadoc comments for this Class.  Specifically the "ATLAS
    // Workaround" section.
    
    // 'localize' the AIF to a temp file, and open that.
    File aifFile = new File (uri);
    File tmpFile = null;
    JAWB jawb = null;
    AWBDocument doc = null;
    try {
      tmpFile = ATLASHelper.createTempFile (aifFile);
      if (DEBUG > 0)
        System.err.println ("AWBDoc.fromAIF: creating tmpFile in "+tmpFile);
      
      FileOutputStream out = new FileOutputStream (tmpFile);
      Map cheatMap = new HashMap ();
      ATLASHelper.localize (uri, out, cheatMap);
      out.close ();
      
      // note that we're sending the modified .aif to be opened (though to
      // save, a new file must be specified)
      jawb = JAWB.fromAIF (tmpFile.toURI(), task);

      // incorporate all the things localization read into atlasCheats
      String mimeType = (String) cheatMap.get("mimeType");
      // null is not ok -- set to "text" if null
      if (mimeType == null || "null".equals (mimeType))
        mimeType = "text";
      jawb.getSignal ().setMIMEType (mimeType); 

      String encoding = (String) cheatMap.get("encoding");
      if (encoding == null || "null".equals (encoding))
        encoding = "UTF-8";
      jawb.getSignal ().setEncoding (encoding);

      byte[] signalData = (byte[]) cheatMap.get(ATLASHelper.SIGNAL_DATA);
      jawb.getSignal().setSignalData(signalData); // null is ok

      
      doc = new AWBDocument(jawb);
      
      // Store any sgml tags that will be dumped back out when exporting
      doc.putClientProperty(SGML_TAG_LIST_KEY, jawb.getSignal().getSgmlTags());
    
      /* Not calculating checksum any longer, since we embed the signal
      // TODO: this has to get back to the user somehow, doesn't it?
      String checksum = (String) cheatMap.get(ATLASHelper.SIGNAL_CHECKSUM);
      String digest   = new String (checksum); ERROR: NPE!!!
      if (checksum == null || checksum.equals (""))
        System.err.println ("Document: Unable to verify signal: no checksum.");
      else if (! checksum.equals (digest)) {
        System.err.println ("Document: Invalid signal, checksum failed!\n"+
                           "  Were line-terminators converted in transit?\n"+
                           "  see http://callisto.mitre.org/faq.html#offsets");
      } else
        System.err.println ("Document: Original signal verified.");
      */

    } finally {
      tmpFile.delete ();      // there's no more need for this
    }
    return doc;
  }

  /**
   * Signal with default encoding
   * @deprecated use {@link #fromSignal(URI,Task,String,String)}
   */
  public static AWBDocument fromSignal (URI uri, String encoding, Task task)
    throws IOException, MalformedURLException {
    // default to "text/plain" mimetype -- null is not ok
    return fromSignal (uri, task, "text/plain", encoding);
  }

  /** Create an AWBDocument from a signal. */
  public static AWBDocument fromSignal (URI uri, Task task,
                                        String mimeType, String encoding)
    throws IOException, MalformedURLException {
    
    if (DEBUG > 0)
      System.err.println ("AWBDoc.fromSignal: uri="+uri+
                          " task="+task.getName()+
                          " mime="+mimeType+" encoding="+encoding);
    
    JAWB jawb = JAWB.fromSignal (uri, task, mimeType, encoding);

    // jATLAS pays no attention to encoding so here we maintain it ourselves
    // in AWBSimpleSignal. JAWB calls ATLASElementFactory /with/ the encoding,
    // but it's ignored, and never set in the signal itself. We override the
    // 'getEncoding' method in AWBSimpleSignal, but that doesn't do us any
    // good, since jATLAS doesn't use our instance variable: it isn't saved to
    // .aif. Instead we use the "atlasCheats" map in 'localize' and
    // 'externalize'
    AWBDocument doc = new AWBDocument (jawb);
    // TODO: remove these when jATLAS pays attention to encoding/mimeType,
    // since it should be used in the above call.
    doc.setMIMEType (mimeType);
    doc.setEncoding (encoding);

    // Store any sgml tags that will be dumped back out when exporting
    doc.putClientProperty(SGML_TAG_LIST_KEY, jawb.getSignal().getSgmlTags());
      
    return doc;
  }

  /**
   * Private constructor used by factory method constructors to initiate an
   * AWBDocument from a JAWB object.
   */
  private AWBDocument (JAWB jawb) {
    this.jawb = jawb;

    try {
      // This tests the success of the ATLAS instantiation
      jawb.getCorpus();

      // add our listener to any pre-existing annots
      Iterator iter = getAllAnnotations ();
      while (iter.hasNext ()) {
        AWBAnnotation tag = (AWBAnnotation)iter.next();
        tag.addPropertyChangeListener(annotListener);
      }
      
    } catch (NullPointerException ne) {
      if (DEBUG > 0)
        System.err.println("AWBDoc.<ctor>: Error retrieving corpus: "+
                           ne.getMessage ());
      if (DEBUG > 1)
        ne.printStackTrace ();
    }
  }

  /**
   * Save the underlying corpus to the specified URI. URI must be absolute,
   * and currently, only 'file' URI are supported. Backups are made if the
   * user preference "backup.count" is > 0 and save is successful.
   */
  public boolean save (URI aifURI, boolean relativize) throws IOException {
    //make sure this isn't null
    if (aifURI == null || ! aifURI.isAbsolute ())
      throw new IllegalArgumentException ("Destination URI is <null>"+
                                          " or non-absolute:\n    "+aifURI);
    
    // See the javadoc comments for this Class.  Specifically the "ATLAS
    // Workaround" section.

    // Files with '#' in it are truncated because java URL believes it to be
    // the fragment delimiter. Converting to "%23" will get that litteraly
    // jATLAS only takes URLs, thus we're stuck. We just don't allow it.
    // we'll probably have to keep adding to this until jATLAS is fixed
    if (aifURI.toString().indexOf("%23") > 0)
      throw new IOException("Callisto can not save to file names including '#'");

    // Backup AIF, write AIF to a temp file, then 'externalize' it to the desired name
    File aifFile = new File (aifURI);
    File backupFile = ATLASHelper.createTempFile (aifFile);
    File tmpFile = ATLASHelper.createTempFile (aifFile);
    
    if (DEBUG > 0)
      System.err.println ("AWBDoc.save:"+
                          "\n  saving to    ("+(aifFile.exists()?"exists":"named ")+") "+aifFile+
                          "\n  backup to    ("+(backupFile.exists()?"exists":"named ")+") "+backupFile+
                          "\n  tmp ATLAS to ("+(tmpFile.exists()?"exists":"named ")+") "+tmpFile);
    
    // TODO: re-work JAWB.java to use jATLAS in a different way when it
    // doesn't make backups too. createTempFile will 'touch' the file and lock
    // it, which is a good security measure, but jATLAS will see that it
    // already exists, and create a backup: not what we want for a temp
    // file. Deleteing opens a momentary security hole, but we're gonna do it
    // anyway
    tmpFile.delete ();
    
    backupFile.delete ();
    if (aifFile.isFile ())
      aifFile.renameTo (backupFile);
    
    // TODO:  REMOVE THIS EXTRA CRAP WHEN ATLAS IS FIXED
    
    // Apologies for the utter crap.  Here's the skinny: certain invalidities
    // will cause XMLExport.save(corpus) to fail, eg. a relation requiring two
    // mentions (by MAIA), which only has one.  This raises an exception deep
    // withing the save stack, and the save fails after writing little or
    // nothing to file. XMLExport.save(corpus) will catch the exception at a
    // higher level, print the stack trace and return, making no indication
    // that the save failed.  The file remaining has only an 'xml' and a
    // 'doctype' declaration. jATLAS tries to be smart before saving, and they
    // rename the target file (if it exists) and if they catch an exception,
    // they rename it back, theoretically saving the day.  I've yet to see it
    // work in my tests.
    
    // Our 'externalization hack' to get around jATLAS' deficiencies requires
    // that we tell jATLAS to save to a temp file, then munge that temp file
    // into the real location. Since there's no way for us to tell if the save
    // failed (the file isn't even non-zero!), other than that the
    // 'externalize' will throw an exception, we now:
    
    //   1. rename any existing <aif> to <backup>
    //   2. have ATLAS save to <tmp> (may or may not succede)
    //   3. externalize <tmp> to <aif>
    //   4. if externalize fails (likely a failure in 2)
    //         rename <backup> <aif>: throw exception
    //      else
    //         cycle history, moving <backup> to history~.0~

    // Currently if the save fails, there's no way to know until later
    try {
      jawb.saveAsAIF (tmpFile.toURL()); // May have spaces, but not '#'
    } catch (MalformedURLException impossible) {
      throw new RuntimeException (impossible);
    }
    
    // AtlasCheats is another to get around things.  ATLAS doesn't store the
    // signal encoding, and it does happen to make a difference. Keep this
    // crap current with the 'localize' and 'externalize' hacks in
    // ATLASHelper.
    Map cheatMap = new HashMap ();
    cheatMap.put ("encoding", getEncoding ());
    cheatMap.put ("mimeType", getMIMEType ());
    if (getSignal ().getDigest() != null)
      cheatMap.put (ATLASHelper.SIGNAL_CHECKSUM, getSignal().getDigest());
    // We now embed the raw signal (eliminating user transfer problems), which
    // also must be done via ATLASHelper
    if (getSignal().getSignalData() != null)
      cheatMap.put (ATLASHelper.SIGNAL_DATA, getSignal().getSignalData());
    
    if (DEBUG > 0)
      System.err.println ("AWBDoc.save: externalizing to "+tmpFile);

    // If externalization fails, it may indicate falure in jawb.saveAsAIF()
    FileOutputStream out = null;
    try {
      out = new FileOutputStream (aifFile);
      ATLASHelper.externalize (tmpFile.toURI(), out, true, cheatMap);
      out.close();
      if (aifFile.length() < 1)
        throw new IOException ("ZERO-LENGTH FILE");
      
    } catch (IOException x) {
      if (DEBUG > 0)
        System.err.println ("AWBDoc.save: FAILED: "+x+"\n\tRenaming backup");
      out.close ();
      aifFile.delete ();
      
      if (backupFile.isFile ())
        backupFile.renameTo (aifFile);
      throw x;

    } finally {
      out.close ();
      if (DEBUG > 0) {
        tmpFile.deleteOnExit();
      }
      else {
        if (! tmpFile.delete()) {
          tmpFile.deleteOnExit();
        }
      }
    }

    // successfully saved!! cycle backups, w/ <backup> going to history~0~

    // make up to 'backupMax' file backups, cycling all and deleting the last
    int backupMax = 5;
    Preferences prefs = Jawb.getPreferences ();
    if (prefs != null)
      backupMax = prefs.getInteger (Preferences.BACKUP_COUNT_KEY, backupMax);
    backupMax = Math.max(0,backupMax); // saw it negative once
    
    if (DEBUG > 2)
      System.err.println ("AWBDoc.save: Cycling backups: max="+backupMax+
                          "\n\tbackup exists: "+backupFile.exists());
    
    // prepare to backup and 'cycle' the files in the history so that
    // history~0~ always indicates most recent backup
    File history[] = new File [backupMax+1]; // backupMax + original
    history[0] = backupFile;
    for (int i=1; i<=backupMax && history[i-1].exists(); i++) {
      history[i] = new File (aifFile.getAbsolutePath()+".~"+(i-1)+".~");
    }
    // inv: for file names history[0:n], those which are non null exists,
    // except n, which might exist only if n==(backupMax+1).
    // history[backupMax] must be deleted if it exists, and every other
    // history[x] must be renamed to history[x+1]
    
    // history[0], is now /copied/ to history[1] file, so that if the save
    // fails, the original file will still be valid.
    for (int i=backupMax; i>0; i--) {
      if (history[i] != null) {
        if (history[i].delete () && DEBUG > 1)//ignore failure (most will)
          System.err.println ("AWBDoc.save: Deleted\n    "+history[i]);
        
        if (DEBUG > 1)
          System.err.println ("AWBDoc.save: Renaming\n    "+history[i-1]+
                              "\n to "+history[i]);
        history[i-1].renameTo (history[i]);
      }
    }

    return true;
  }
  
  /**
   * Release memory and resources from ATLAS for the backing Corpus.
   */
  public void close () {
    jawb.close ();
  }

  
  /***********************************************************************/
  /*                         Document Properties                         */
  /***********************************************************************/

  /**
   * Retrieve the task used to annotate this document.
   */
  public final Task getTask() {
    return jawb.getTask ();
  }
  
  /**
   * Return the location of the underlying data signal as a URL.
   */
  public URL getSignalLocation () {
    return jawb.getSignal ().getLocation ();
  }

  /**
   * Return the location of the underlying data signal as a URL.
   */
  public URI getSignalURI () {
    return URLUtils.badURLToURI (getSignalLocation ());
  }

  /**
   * Return the underlying data signal.
   */
  public AWBSimpleSignal getSignal () {
    return jawb.getSignal ();
  }

  /**
   * Get the MIME type (not MIME Class) of the signal. The MIME Class is asumed
   * to be 'text'
   * @return mime type of signal or null if default is being used
   */
  public String getMIMEType () {
    return jawb.getSignal ().getMIMEType ();
  }

  /** Specifies the mime type of the signal in question. */
  public void setMIMEType (String mimeType) throws IOException {
    jawb.getSignal ().setMIMEType (mimeType);
  }

  /**
   * Return the name of the character encoding of this document's signal.<p>
   *
   * @return the encoding of the underlying data signal.
   * @see #setEncoding
   * @see java.nio.charset.Charset
   */
  public String getEncoding () {
    return jawb.getSignal ().getEncoding ();
  }
  
  /**
   * Set the character encoding of this document's signal. The encoding must
   * be supported by the java platform. Once setting this clients should
   * re-read the signal using the new encoding.
   * @see java.nio.charset.Charset
   * @throws IOException if there is an error re-reading the signal
   * @throws UnsupportedEncodingException if the named charset is not
   * supported
   */
  public void setEncoding (String encoding) throws IOException {
    jawb.getSignal ().setEncoding (encoding);
  }

  /**
   * Supports managing a set of properties. Callers can use the
   * <code>clientProperties</code> Map to store document-wide properties at
   * runtime.  These properties are transient, and not stored in the .aif
   * output.
   * 
   * @return a non-<code>null</code> <code>Map</code>
   * @see #setClientProperties
   */
  public Map getClientProperties () {
    if (clientProperties == null) {
      clientProperties = new HashMap (2);
    }
    return clientProperties;
  }

  /**
   * Replaces the document properties dictionary for this document. Setting
   * with <code>null</code> will clear all properties.
   * 
   * @param map the new property map
   * @see #getClientProperties
   */
  public void setClientProperties (Map map) {
    clientProperties = map;
  }

  /**
   * A convenience method for looking up a property value. It is
   * equivalent to:
   * <pre>
   * getClientProperties().get(key);
   * </pre>
   * 
   * @param key the non-<code>null</code> property key
   * @return the value of this property or <code>null</code>
   * @see #getClientProperties
   * @see #putClientProperty
   */
  public final Object getClientProperty (Object key) {
    return getClientProperties ().get (key);
  }

  /**
   * A convenience method for storing up a property value.  It is equivalent
   * to:
   * <pre>
   * getClientProperties().put(key, value);
   * </pre>
   * If <code>value</code> is <code>null</code> this method will remove the
   * property. Note that property change events are not fired for these
   * properties, only Properties of the JawbDocument itself which have get/set
   * methods.
   * 
   * @param key the non-<code>null</code> key
   * @param value the property value
   * @see #getClientProperties
   * @see #getClientProperty
   */
  public final void putClientProperty (Object key, Object value) {
    if (value == null) {
      getClientProperties().remove (key);
    } else {
      getClientProperties().put (key, value);
    }
  }

  
  /**********************************************************/
  /*              Annotation Model Methods                  */
  /**********************************************************/
  
  /**
   * Convience method to create an annotation using a name String instead of
   * AnnotationType.
   *
   * @param typeName name of an annotation that can be retrieved from the Task
   * @param start offset of start of annotation. Documents offsets start at 0 
   * @param end offset of end of annotation. Documents offsets start at 0 
   * @param mainRole this should be null unless you're
   *                       creating a multi-span annotaiton.
   * @return the new annotation, or <code>null</code> on failure
   * @see #createAnnotation(AnnotationType, int, int, String)
   */
  public AWBAnnotation createAnnotation(String typeName,
                                        int start, int end,
                                        String mainRole) {
    AnnotationType type = jawb.getTask ().getAnnotationType (typeName);
    return createAnnotation (type, start, end, mainRole);
  }
  /**
   * Create a new text-based annotation and fires an event to all the
   * observers of this AnnotationModel.
   *
   * @param type of annotation that can be retrieved from the Task
   * @param start offset of start of annotation. Documents offsets start at 0 
   * @param end offset of end of annotation. Documents offsets start at 0 
   * @param mainRole this should be null unless you're
   *                       creating a multi-span annotaiton.
   * @return the new annotation, or <code>null</code> on failure
   */
  public AWBAnnotation createAnnotation(AnnotationType type,
                                        int start, int end,
                                        String mainRole) {
    if (type == null)
      throw new IllegalArgumentException ("type=null");
    
    AWBAnnotation tag = null;  //default
    
    //is this a mention? 
    if (mainRole == null) {
      tag = jawb.createAnnotation (type, start, end);
      
    } else {
      tag = jawb.createAnnotation (type);
      
      if (tag != null) {
        String startRole = mainRole.concat("TextExtentStart");
        String endRole = mainRole.concat("TextExtentEnd");
        try {
          tag.setAttributeValue(startRole, Integer.toString (start));
          tag.setAttributeValue(endRole, Integer.toString (end));
          
        } catch (UnmodifiableAttributeException uae) {
          System.err.println("AWBDoc.createAnnot: ("+start+","+end+
                             ") type="+type.getName()+" mainRole="+mainRole);
          //uae.printStackTrace();
        }
      }
    }
    
    if (tag != null) {
      tag.addPropertyChangeListener(annotListener); // listen for changes
      fireCreateEvent (tag); // spread the news about the new baby!
    }
    return tag;
  }

  /**
   * Convience method to create an annotation using name String instead of
   * AnnotationType.
   *
   * @param typeName name of an annotation that can be retrieved from the Task
   * @return the new annotation, or <code>null</code> on failure
   * @see #createAnnotation(AnnotationType)
   */
  public AWBAnnotation createAnnotation(String typeName) {
    AnnotationType type = jawb.getTask ().getAnnotationType (typeName);
    return createAnnotation (type);
  }
  /**
   * Creates an empty annotation and fires an event to all the
   * observers of this AnnotationModel.
   *
   * @param type of annotation that can be retrieved from the Task
   * @return the new annotation, or <code>null</code> on failure
   */
  public AWBAnnotation createAnnotation (AnnotationType type) {
    if (type == null) {
      System.err.println("AWBDoc.createAnnot type==null");
      throw new IllegalArgumentException ("type=null");
    }
    
    AWBAnnotation tag = jawb.createAnnotation (type);
    
    if (tag != null) {
      tag.addPropertyChangeListener (annotListener); // listen for changes
      fireCreateEvent (tag); // spread the news about the new baby!
    }
    return tag;
  }

  /**
   * Delete an annotation.
   *
   * @param annot an annotation to remove
   * @param doSubs indicator whether or not to remove sub annotations of the
   *               specified annotation as well
   * @return true on complete success, false if deleting the specified or any
   *         of it's sub annotations failed.
   */
  public boolean deleteAnnotation(AWBAnnotation annot, boolean doSubs) {
    // get an iterator for the subordinates before deleting.
    Iterator subsIter = null;
    if (doSubs)
      subsIter = annot.getExclusiveSubAnnotations().iterator();
    
    boolean success = jawb.removeAnnotation(annot);
    if (success) {
      annot.removeAllPropertyChangeListeners();
      fireDeleteEvent (annot);
      if (doSubs) {
        // remove subAnnotations
        // TODO: this will need hooks back into gui code, if user needs
        // input on the subannots. perhaps all the subannot checking belongs
        // in the document (which has access to gui components).
        // TODO: should or shouldn't we remove subannots from here?
        while (subsIter.hasNext()) {
          success &= deleteAnnotation((AWBAnnotation)subsIter.next(), false);
        }
      }
    }
    return success;
  }

  
  /**********************************************************/
  /*         ATLAS Data Structure Accessor Methods          */
  /**********************************************************/
  
  /**
   * Returns all the analyses for a given document.
   *
   * @return Iterator of analyses
   */
  public Iterator getAllAnalyses() {
    return jawb.getCorpus().getAllAnalyses().iterator ();
  }

  /**
   * Returns the Analysis subordinate identified with the specified role.
   *
   * @param role - the role identifying the Analysis to retrieve
   * @return Analysis with specified role
   */
  public Analysis getAnalysisWithRole(java.lang.String role) {
    return jawb.getCorpus().getAnalysisWithRole(role);
  }

  /**
   * Returns all the anchors for a given document.
   *
   * @return Iterator of anchors
   */
  public Iterator getAllAnchors() {
    return jawb.getCorpus().getAllAnchors().iterator ();
  }
  
  /**
   * Returns all the annotations for a given document. When iteratoring
   * through this, be sure to cast to the right thing
   *
   * @return Iterator of Annotation objects
   */    
  public Iterator getAllAnnotations() {
    return jawb.getCorpus().getAllAnnotations().iterator();
  }

  /**
   * This method returns all the regions for the open document. This is
   * package-private, because only the SignalHandler should ever need this
   * method.
   *
   * @return Iterator of Region objects
   */
  Iterator getAllRegions() {
    return jawb.getCorpus().getAllRegions().iterator();
  }

  
  /***********************************************************************/
  /*                  Arbitrary Properties of the Model?                 */
  /***********************************************************************/

  // public Object getProperty (Object key);
  // public void putProperty (Object key, Object value);

  
  /***********************************************************************/
  /*                         Event Propogation                           */
  /***********************************************************************/

  /**
   * Add an observer to this model.
   */
  public void addAnnotationModelListener (AnnotationModelListener l) {
    //System.err.println ("AWBDoc.addAML: "+listener);
    annotListeners.add (l);
  }

  /**
   * Remove an observer from this model.
   */
  public void removeAnnotationModelListener (AnnotationModelListener l) {
    //System.err.println ("AWBDoc.removeAML: "+listener);
    annotListeners.remove (l);
  }

  /***********************************************************************/

  /**
   * Inform this models observers of an annotation removal.
   */
  protected void fireCreateEvent (AWBAnnotation annot) {
    
    if (DEBUG > 2)
      System.err.println ("AWBDoc.fireAMCreateEvent: "+this+" "+annot);
    AnnotationModelEvent event = new AnnotEvent (annot,
                                                 EventType.CREATE, null);
    
    // don't want concurrent exception if they remove themselves
    listArray = (AnnotationModelListener[])annotListeners.toArray (listArray);
    for (int i=0; i<listArray.length && listArray[i] != null; i++)
      listArray[i].annotationCreated (event);
  }

  /**
   * Inform this models observers of an annotation removal.
   */
  protected void fireDeleteEvent (AWBAnnotation annot) {
    
    if (DEBUG > 2)
      System.err.println ("AWBDoc.fireAMDeleteEvent: "+annot);
    AnnotationModelEvent event = new AnnotEvent (annot,
                                                 EventType.DELETE, null);
    
    // don't want concurrent exception if they remove themselves
    listArray = (AnnotationModelListener[])annotListeners.toArray (listArray);
    for (int i=0; i<listArray.length && listArray[i] != null; i++)
      listArray[i].annotationDeleted (event);
  }

  /***********************************************************************/
  
  /**
   * Inform this models observers of an annotation change.
   */
  protected void fireChangeEvent (AnnotationModelEvent.AnnotationChange change) {
    
    if (DEBUG > 2) {
      System.err.println ("AWBDoc.fireAMChangeEvent: "+change.getAnnotation());
      System.err.println ("---change: " + change.getPropertyName() +
                          " from: " + change.getOldValue() +
                          " to: " + change.getNewValue());
    }
    AnnotationModelEvent event = new AnnotEvent (change.getAnnotation(),
                                                 EventType.CHANGE, change);
    
    // don't want concurrent exception if they remove themselves
    listArray = (AnnotationModelListener[])annotListeners.toArray (listArray);
    for (int i=0; i<listArray.length && listArray[i] != null; i++)
      listArray[i].annotationChanged (event);
  }

  /**
   * Inform this models observers of an annotation addition.
   */
  protected void fireInsertEvent (AnnotationModelEvent.AnnotationChange change) {

    if (DEBUG > 2)
      System.err.println ("AWBDoc.fireAMInsertEvent: "+change.getAnnotation());
    AnnotationModelEvent event = new AnnotEvent (change.getAnnotation(),
                                                 EventType.INSERT, change);
    
    // don't want concurrent exception if they remove themselves
    listArray = (AnnotationModelListener[])annotListeners.toArray (listArray);
    for (int i=0; i<listArray.length && listArray[i] != null; i++)
      listArray[i].annotationInserted (event);
  }
  
  /**
   * Inform this models observers of an annotation removal.
   */
  protected void fireRemoveEvent (AnnotationModelEvent.AnnotationChange change) {
    
    if (DEBUG > 2)
      System.err.println ("AWBDoc.fireAMRemoveEvent: "+change.getAnnotation());
    AnnotationModelEvent event = new AnnotEvent (change.getAnnotation(),
                                                 EventType.REMOVE, change);
    
    // don't want concurrent exception if they remove themselves
    listArray = (AnnotationModelListener[])annotListeners.toArray (listArray);
    for (int i=0; i<listArray.length && listArray[i] != null; i++)
      listArray[i].annotationRemoved (event);
  }

  
  /***********************************************************************/
  /*              Implementaions of Events to Propogate                  */
  /***********************************************************************/

  /**
   * Implementation of the annottationmodel event, which will perhaps one day
   * be 'undoable'. Instantiated it with an instance of {@link
   * AnnotationModelEvent.AnnotationChange}.
   *
   * @see AnnotPropertyChange
   * @see SubAnnotChange
   */
  private class AnnotEvent implements AnnotationModelEvent {
    
    private EventType type;
    private AWBAnnotation annot;
    private AnnotationChange change;

    public AnnotEvent (AWBAnnotation annot, EventType type,
                       AnnotationChange change) {
      this.annot = annot;
      this.type = type;
      this.change = change;
    }

    public AnnotationChange getChange () { return change; };
    public EventType getType () { return type; }
    public AnnotationModel getModel () { return AWBDocument.this; }
    public AWBAnnotation getAnnotation () { return annot; }
  }

  
  /***********************************************************************/
  /*                Change listener for Annotations                      */
  /***********************************************************************/

  /**
   * Object to recivee property change events from the Annotations and
   * propogates them to the model observers, wrapped in an
   * AnnotationModelEvent.  The Annotations have been made to use hybrid
   * PropertyChangeEvents, which also propogate information about
   * SubAnnotation modifications. The PropertyChange event is cast to
   * <code>AnnotationModelEvent.AnnotationChange</code> which is expected to
   * succede.
   */
  private class AnnotChangeListener implements PropertyChangeListener {
    /** recieve a change event from an annotation */
    public void propertyChange (PropertyChangeEvent e) {
      
      // Would be nicer if we could test for 'instanceof', (I moved
      // implementations to AWBAnnotationImpl as package private) or maybe we
      // should have a notion of 'type' for the AnnotationChange.
      AnnotationModelEvent.AnnotationChange ac =
        (AnnotationModelEvent.AnnotationChange)e;
      
      if (ac.getAnnotationsInserted () != null)
        fireInsertEvent (ac);
      else if (ac.getAnnotationsRemoved () != null)
        fireRemoveEvent (ac);
      else // better be for a Change event!
        fireChangeEvent (ac);
    }
  }

}
