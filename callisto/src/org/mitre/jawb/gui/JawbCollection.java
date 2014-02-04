
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

package org.mitre.jawb.gui;

import java.util.Properties;
import java.io.*;
import java.net.URI;

import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.io.FileUtils;
import org.mitre.jawb.Jawb;

/**
 *  A JawbCollection is a collection of files to be annotated in rapid
 *  succession.  The collection will keep track of the signal and AIF
 *  file URIs, and the "done" status of each file in the collection.
 *  The collection is backed by a preferences file, which is created
 *  when the collection is created, and is updated every time the properties
 *  of the collection (done status of a file, aif name of a file, etc.)
 *  are updated.
 */
public class JawbCollection {

  /** The Task associated with the collection */
  private Task task = null;

  /** The Properties object storing the properties of this collection */
  private Properties properties;

  /** The number of files in the collection */
  private static int numFiles;

  /** The properties File in which this collection is stored */
  private File propFile;

  /** The output stream for the properties file */
  //  private OutputStream propStream = null;
  // do not store this as a global -- create a new stream to use and
  // then discard each time so we don't keep appending to the props file

  /** the full path to the collection file */
  private String propPathStr;
  /** the display name for this collection */
  private String displayName = null;

  // TODO -- handle setting/storing these to other possible values
  /** Encoding of signal files in this collection */
  private static String encoding = null;
  /** MIME type of signal files in this collection */
  private static String mimeType = null;
  /** The name of the file containing the metadata specification,
      if applicable */
  private static String metadataStr = null;
  /** The File named by the metadataStr string */
  private static File metadataFile = null;
  /** The Properties object read in from the metadataFile */
  private static Properties metadataProperties = null;

  /** store the JawbFrame so that it can be used to anchor the 
   * dialog to choose the location of the collection file when needed
   */
  private static JawbFrame jf = null;



  public static String TASK_KEY = "callisto.collection.task";
  public static String MIME_KEY = "callisto.collection.mimetype";
  public static String ENCODING_KEY = "callisto.collection.encoding";
  public static String NUMFILES_KEY = "callisto.collection.numfiles";
  public static String METADATA_KEY = "callisto.collection.metadata";

  public static String COLLECTION_KEY_STR = "callisto.collection";
  
  /** creates the collection file and the JawbCollection from the given
   * set of files and the given Task */
  public JawbCollection (File[] files, Task task, JawbFrame jf, 
                         String mimeType, String encoding) {
    this.task = task;
    this.mimeType = mimeType;
    this.encoding = encoding;
    this.jf = jf;

    properties = new Properties();
    numFiles = files.length;
    properties.setProperty(NUMFILES_KEY, String.valueOf(numFiles));

    propFile = GUIUtils.chooseFile (jf, GUIUtils.SAVE_COLLECTION_DIALOG);
    // check if we're going to be able to create a FileOutputStream for this
    // propFile
    OutputStream propStream = null;
    while (propStream == null) {
      try {
        propStream = new FileOutputStream(propFile);
      } catch (FileNotFoundException x) {
        // TODO do something intelligent here
        System.err.println("File not found: " + propFile.getName());
        propFile = GUIUtils.chooseFile (jf, GUIUtils.SAVE_COLLECTION_DIALOG);
      }
    }
    propPathStr = propFile.getAbsolutePath();
    displayName = propFile.getName();

    // TODO -- optionally? according to preferences? 
    //         prompt for a metadata specification file
    metadataStr = null;
    metadataFile = null;

    properties.setProperty(TASK_KEY, task.getName());
    properties.setProperty(MIME_KEY, mimeType);
    properties.setProperty(ENCODING_KEY, encoding);
    
    for (int i=0; i<numFiles; i++) {
      // TODO this is absolute -- we want relative to the location
      // of the collection file, which we also have to specify
      try {
        properties.
          setProperty(getPropertyKey(i, "signal"),
                      FileUtils.canonicalize(FileUtils.relativize(files[i],
                                                                  propFile)));
      } catch (IOException x) {
        properties.setProperty(getPropertyKey(i, "signal"),
                               files[i].getAbsolutePath());
      }
      properties.setProperty(getPropertyKey(i, "done"),
                             "false");
    }

    storeProperties(propStream);
  }
  
  /** creates a JawbCollection from an existing collection file.
   *  if the collectionFile is not found, the JawbCollection returned
   *  will be an empty collection.
   *  TODO? throw an exception if file is not found or is empty?
   */
  public JawbCollection (File collectionFile) {
    InputStream inStream=null;
    try {
      inStream = new FileInputStream(collectionFile);
    } catch (FileNotFoundException x) {
      // TODO do something intelligent here
      System.err.println("File not found: " + collectionFile.getName());
    }
    properties = new Properties();
    if (inStream != null) {
      try {
        properties.load(inStream);
        // initialize these variables relating to the properties file
        propFile = collectionFile;
        propPathStr = collectionFile.getAbsolutePath();
        displayName = collectionFile.getName();
        // set this up now for later updates of the file
        // propStream = new FileOutputStream(collectionFile);
        // actually don't -- need to open the stream each time
        // so the file doesn't keep appending
      } catch (IOException x) {
        // TODO do something intelligent here
        System.err.println("unable to read collection file " +
                           collectionFile.getName());
      }
    }
    // set task, mimetype and encoding
    String taskStr = properties.getProperty(TASK_KEY);
    System.err.println ("Possible Tasks: " + Jawb.getTaskManager().getTasks());
    task = Jawb.getTaskManager().getTaskByName(taskStr);
    if (task == null) {
      // TODO do something intelligent here
      System.err.println ("JawbCollection: Bad task specification: " +
                          taskStr);
    }
    String tmp = properties.getProperty(MIME_KEY);
    if (tmp != null)
      mimeType = tmp;
    tmp = properties.getProperty(ENCODING_KEY);
    if (tmp != null)
      encoding = tmp;
    tmp = properties.getProperty(NUMFILES_KEY);
    if (tmp != null)
      numFiles = Integer.parseInt(tmp);
    tmp = properties.getProperty(METADATA_KEY);
    if (tmp != null) {
      metadataStr = tmp;
      System.err.println("JC: metadataStr = " + metadataStr);
      metadataFile = new File(collectionFile.getParentFile(),metadataStr);
      System.err.println("JC: metadataFile = " + metadataFile);
      metadataProperties = new Properties();
      try {
        InputStream metaStream = new FileInputStream(metadataFile);
        metadataProperties.load(metaStream);
        System.err.println("JC: metadataProps = " + metadataProperties);
      } catch (FileNotFoundException x) {
        // TODO do something intelligent here
        System.err.println("Metadata specification file not found: " 
                           + metadataFile.getName());
      } catch (IOException x) {
        // TODO do something intelligent here
        System.err.println("Unable to read metadata specification file " +
                           metadataFile.getName());
      } 
    }

  }

  /** returns the index of the first file whose "done" property is not
   *  yet set, and that does not have its "broken" property set; or -1
   *  if there is no such file. */
  public int getNextIndex () {
    System.out.println("JC.getNextIndex");
    for (int i=0; i<numFiles; i++) {
      System.out.println("\ti=" + i);
      boolean done = 
        new Boolean(properties.getProperty(getPropertyKey(i,"done"))).
        booleanValue();
      boolean broken = 
        new Boolean(properties.getProperty(getPropertyKey(i,"broken"))).
        booleanValue();
      System.out.println("done=" + done + "broken=" + broken);
      if (!done && !broken) {
        return i;
      }
    }
    return -1;
  }

  public File getSignalFile (int index) {
    String signalStr = properties.getProperty(getPropertyKey(index,"signal"));
    System.out.println("JC.getSignalFile("+index+") = " +
                       (signalStr == null ? "null" : signalStr));
    // interpret the signalStr path relative to the property file path
    return (signalStr==null? null : 
            new File(propFile.getParentFile(),signalStr));
  }

  public File getAIFFile (int index) {
    String aifStr = properties.getProperty(getPropertyKey(index,"aif"));
    System.out.println("JC.getAIFFile("+index+") = " +
                       (aifStr == null ? "null" : aifStr));
    // interpret the signalStr path relative to the property file path
    return (aifStr==null? null : new File(propFile.getParentFile(),aifStr));
  }

  public void setDone (int index, boolean isDone) {
    properties.setProperty(getPropertyKey(index,"done"), 
                           String.valueOf(isDone));
    storeProperties();
  }

  public void setFileBroken (int index, boolean isBroken) {
    properties.setProperty(getPropertyKey(index,"broken"),
                           String.valueOf(isBroken));
    storeProperties();
  }

  public void setTimeSpent (int index, long millis) {
    properties.setProperty(getPropertyKey(index,"time"),
                           String.valueOf(millis));
    storeProperties();
  }

  public long getTimeSpent (int index) {
    String tmp = properties.getProperty(getPropertyKey(index,"time"));
    System.err.println("getTimeSpent for " + index + " found " + tmp);
    return (tmp==null?0:(new Long(tmp)).longValue());
  }

  /** 
   * sets the amount of time spent so far on collecting metadata for
   * a given file
   */
  public void setMetadataTimeSpent (int index, long millis) {
    properties.setProperty(getPropertyKey(index,"metadata.time"),
                           String.valueOf(millis));
    storeProperties();
  }

  public long getMetadataTimeSpent (int index) {
    String tmp = properties.getProperty(getPropertyKey(index,"metadata.time"));
    return (tmp==null?0:(new Long(tmp)).longValue());
  }


  public void setAIF (int index, URI uri) {
    System.err.println ("JC.setAIF(URI) AIF file is " + new File(uri));
    try {
      properties.
        setProperty(getPropertyKey(index,"aif"),
                    FileUtils.
                    canonicalize(FileUtils.relativize(new File(uri),
                                                      propFile)));
    } catch (IOException x) {
      // can't properly relativize it, just canonicalize it
      System.err.println("JC.setAIF: IOException when relativizing " +
                         uri + " to " + propFile);
      properties.setProperty(getPropertyKey(index,"aif"),
                             FileUtils.canonicalize(new File(uri)));
    };
    storeProperties();
  }

  public void setAIF (int index, String aifStr) {
    System.err.println ("JC.setAIF AIF file is " + new File(aifStr));
    try {
      properties.
        setProperty(getPropertyKey(index,"aif"),
                    FileUtils.
                    canonicalize(FileUtils.relativize(new File(aifStr),
                                                      propFile)));
    } catch (IOException x) {
      // can't properly relativize it, just canonicalize it
      System.err.println("JC.setAIF: IOException when relativizing " +
                         aifStr + " to " + propFile);
      properties.setProperty(getPropertyKey(index,"aif"),
                             FileUtils.canonicalize(aifStr));
    };
    storeProperties();
  }

  public Task getTask () {
    return task;
  }

  public String getMIMEType () {
    return mimeType;
  }

  public String getEncoding () {
    return encoding;
  }

  public Properties getMetadataProperties() {
    return metadataProperties;
  }

  /** Pops up a browser that displays all of the files and their 
   *  done status, and their last-edited timestamp.  Returns the 
   *  selected file if one is selected.  (TODO maybe this method 
   *  should be void and different methods should be called according
   *  to what buttons are pressed.)
   */
  public File browse () {
    // TODO -- really make a browser
    // for now....
    int i = getNextIndex();
    File file = getAIFFile(i);
    if (file == null)
      file = getSignalFile(i);
    return file;
  }

  /**
   * Returns a string suitable for display to user in the gui.
   */
  public final String getDisplayName () {
    return displayName;
  }

  private String getPropertyKey(int i, String detail) {
    return COLLECTION_KEY_STR+"."+i+"."+detail;
  }
 
 /** Saves the properties out to the properties file propFile */
  private void storeProperties () {
    storeProperties(null);
  }

  /** Saves the properties out to the properties file propFile.  If
   * propStream is non-null it must be an already opened OutputStream
   * to the propFile.  Otherwise a new stream is created. */
  private void storeProperties (OutputStream propStream) {

    // create the stream if needed
    while (propStream == null) {
      try {
        propStream = new FileOutputStream(propFile);
      } catch (FileNotFoundException x) {
        // TODO do something intelligent here
        System.err.println("File not found: " + propFile.getName());
        propFile = GUIUtils.chooseFile (jf, GUIUtils.SAVE_COLLECTION_DIALOG);
      }
    }
    // store the properties
    try {
      properties.store(propStream, "Callisto Collection Properties");
    } catch (IOException x) {
      // TODO do something intelligent here
      System.err.println("unable to write collection file " + 
                         propFile.getName());
    }
    // close the stream
    try {
      propStream.close();
    } catch (IOException x) {
      // TODO do something intelligent here
      System.err.println("unable to close collection file stream");
    }
  }
}