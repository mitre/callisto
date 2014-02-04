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

package org.mitre.muc.callisto.session;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.WeakHashMap;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.dom4j.Document;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.gui.JawbDocument;
import org.mitre.jawb.gui.GUIUtils;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.tasks.Task;


/**
 * Simple timer dialog which has a pause button can store data.
 */
public class SessionLogger {

  private static final int DEBUG = 0;

  private static final String SESSION_LOG_KEY = "callisto.session.log";

  public static final Object TIMER_KEY = SESSION_LOG_KEY+".timer";
  public static final String GEOMETRY_KEY = SESSION_LOG_KEY;

  public static final String SESSION_LOG_DIR_PREFERENCE =
    SESSION_LOG_KEY+".dir";
  public static final String SESSION_LOG_ENABLED_PREFERENCE =
    SESSION_LOG_KEY+".enabled";

  private static final String ROOT_NAME = "session-log";
  
  private static final SimpleDateFormat isoPoint;
  private static final SimpleDateFormat isoPeriod;

  static {
    isoPoint = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
    isoPeriod = new SimpleDateFormat ("'P'HH:mm:ss");
    isoPeriod.setTimeZone(TimeZone.getTimeZone("GMT"));
  }
  
  private Date tempDate;
  private ParsePosition tempPos;
  private MyDocListener docListener;
  private SessionLogFrame logFrame;
  private JawbDocument doc;

  private File logDir = null;
  private WeakHashMap logFileMap = new WeakHashMap();
  private SAXReader xmlReader = new SAXReader();

  /** Create a session logger, including a Frame to display state. Frame is
   * initially not visible */
  public SessionLogger () {
    tempDate = new Date();
    tempPos = new ParsePosition(0);
    docListener = new MyDocListener();
    logFrame = new SessionLogFrame();
    logFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    logFrame.setStopEnabled(false);
    // we can't disable close decoration so warn users who try.
    logFrame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          String msg = "Closing the session logger is disabled.\n"+
            "To disable: close document and turn off logging.";
          JOptionPane.showMessageDialog (logFrame, msg, "Session Logger",
                                         JOptionPane.INFORMATION_MESSAGE);
        }
      });
    // listen for movements and window hide/show to store geomerty
    logFrame.addComponentListener (new ComponentAdapter () {
        public void componentMoved (ComponentEvent e) { storeGeometry(); }
        public void componentResized (ComponentEvent e) { storeGeometry(); }
        private void storeGeometry () {
          GUIUtils.storeGeometry(logFrame, GEOMETRY_KEY);
        }
      });
    GUIUtils.loadGeometry(logFrame, GEOMETRY_KEY);
    
    Preferences prefs = Jawb.getPreferences();
    String dir = prefs.getPreference(SESSION_LOG_DIR_PREFERENCE);
    logDir = initLogDir(dir);
    prefs.setPreference(SESSION_LOG_DIR_PREFERENCE,
                        ((logDir == null) ? null : logDir.toURI().toString()));
  }

  /** Finds the appropriate directory for log files, creating it if needed. */
  private File initLogDir(String tryURI) {
    String dir = tryURI;
    File dirFile = null;
    String badDir = null;

    if (dir != null) {
      try {
        dirFile = new File(new URI (dir));
        if (isBadLocation(dirFile))
          dirFile = null;
      } catch (Exception e) {
        dirFile = null;
      }
    }
    if (dirFile == null) {
      badDir = dir;
      try {
        dir = System.getProperty("user.home") +
          File.separator + ".callisto" + File.separator + ROOT_NAME;
        dirFile = new File(dir);
        if (isBadLocation(dirFile))
          dirFile = null;
      } catch (Exception e) {
        dirFile = null;
      }
    }
    if (dirFile == null) {
      String badList = "director";
      if (badDir != null && ! badDir.equals(dir))
        badList = "ies:\n\n" + badDir + "\n" + dir;
      else
        badList = "y\n\n" + dir;
      System.err.println("Session Log Disabled: unable to create logs");
      GUIUtils.showError("Unable to create or use log "+badList+
                         "\n\nLog files will NOT be kept!");
    } else {
      System.err.println("Session Log Dir      = "+dirFile);
    }
    return dirFile;
  }
  /** Utility to check a dir or create it, returns true if dir exists and is
   * writable upon return. */
  private boolean isBadLocation(File dir) {
    return ( ( dir.exists() && (!dir.canWrite() || dir.isFile()))
             || (!dir.exists() && !dir.mkdirs()));
  }

  /** Update the SessionLogFrame from attributes in the document. The timer is
   * added to the log frame in case it is being changed or removed. */
  private void updateLogFrame() {
    String fileName = null;
    String location = null;
    String id = null;
    Timer timer = null;
    
    if (doc != null) {
      timer = (Timer) doc.getClientProperty(TIMER_KEY);
      fileName = doc.getName();
      // RK 2/6/04 this seems to be crashing so I added this test
      // but I think it is a bug that this is null when updateLF is called
      if (timer != null) 
	id = timer.getId();
      URI uri = doc.getAtlasURI();
      if (uri == null)
        uri = doc.getSignalURI();
      if (uri == null)
        uri = doc.getExternalURI();
      if (uri != null)
        location = uri.toString();
    }      
    logFrame.setAttribute(SessionLogFrame.FILE_NAME, fileName);
    logFrame.setAttribute(SessionLogFrame.SESSION_ID, id);
    logFrame.setAttribute(SessionLogFrame.LOCATION, location);

    logFrame.setTimer(timer);
    logFrame.pack();
    logFrame.setVisible(doc != null);
  }

  /** Return currently JawbDocument. */
  public JawbDocument getJawbDocument() { return doc; }

  /** Change the document this Logger is tracking. A listener is added to
   * the document and the log frame is updated. */
  public void setJawbDocument(JawbDocument doc) {
    Preferences prefs = Jawb.getPreferences();
    JawbDocument oldDoc = getJawbDocument ();
    
    if (oldDoc != null) {
      oldDoc.removePropertyChangeListener(docListener);
      logFrame.setVisible(false);
      Timer timer = (Timer) oldDoc.getClientProperty(TIMER_KEY);
      if (timer != null) {
        timer.pause(true);
        try {
          if (prefs.getBoolean(SESSION_LOG_ENABLED_PREFERENCE))
            updateLog(oldDoc, timer);
        } catch (IOException e) {
          Throwable x = e;
          if (e.getCause() != null)
            x = e.getCause();
          GUIUtils.showError ("Unable to log session:\n"+x.getMessage());
        }
      }
    }

    this.doc = doc;

    if (doc != null) {
      doc.addPropertyChangeListener(docListener);
      if (prefs.getBoolean(SESSION_LOG_ENABLED_PREFERENCE)) {
        Timer timer = (Timer) doc.getClientProperty(TIMER_KEY);
        if (timer == null) {
          timer = new Timer();
          doc.putClientProperty(TIMER_KEY, timer);
        }
        timer.start(); // equiv to timer.pause(false)
      }
    }
    updateLogFrame();
  }
  
  /** Utility to generate a log file in the right directory. */
  private File getLogFile(File dir, String taskName) {
    return new File (logDir, taskName.replaceAll("\\.","_")+".log.xml");
  }

  /*
   * Write the current session to the log. Updates the correct session entry
   * for the current file.  The 'file' is determined by the URLs available in
   * the doc: AtlasURI, SignalURI and ExportURI are tried in that order. Log
   * file is determined by task. If the file and or session entries are not
   * present, they are created.
   */
  private void updateLog(JawbDocument doc, Timer timer)
    throws IOException{
    
    if (doc == null || timer == null)
      return;

    URI uri = doc.getAtlasURI();
    if (uri == null)
      uri = doc.getSignalURI();
    if (uri == null)
      uri = doc.getExternalURI();

    File log = (File) logFileMap.get(uri);
    if (log == null)
      log = getLogFile (logDir, doc.getTask().getName());

    Document xdoc = parseXML(log);
    
    Element root = xdoc.getRootElement();
    Element file = getFileElement(root, uri);
    Element session = getSessionElement(file, timer);
    
    tempDate.setTime(timer.getStartTime());
    session.addAttribute("start", isoPoint.format(tempDate));
    tempDate.setTime(timer.getStopTime());
    session.addAttribute("stop", isoPoint.format(tempDate));
    tempDate.setTime(timer.getPauseDuration());
    session.addAttribute("pause", isoPeriod.format(tempDate));
    tempDate.setTime(timer.getDuration());
    session.addAttribute("duration", isoPeriod.format(tempDate));

    // sum the duration for the file element
    long duration = 0;
    for (Iterator i = getSessionIterator(file); i.hasNext(); ) {
      session = (Element) i.next();
      tempPos.setIndex(0);
      String durString = session.attributeValue("duration");
      if (durString != null) {
        Date dur = isoPeriod.parse(durString,tempPos);
        if (dur != null)
          duration += dur.getTime();
      }
    }
    tempDate.setTime(duration);
    file.addAttribute("duration",isoPeriod.format(tempDate));
    
    OutputStream out = new BufferedOutputStream(new FileOutputStream(log));
    dumpXML(xdoc,out);
    out.close();
  }

  /**
   * Load the specified document.
   * @throw IOException to wrapper any dom4j errors
   */
  private Document parseXML(File aFile) throws IOException {
    Document xdoc = null;
    if (aFile.canRead()) { 
      try {
        xdoc = xmlReader.read(aFile);
      } catch (DocumentException de) {
        IOException x = new IOException ();
        x.initCause(de);
        throw x;
      }
    } else {
      Element root = DocumentHelper.createElement(ROOT_NAME);
      xdoc = DocumentHelper.createDocument(root);
    }
    return xdoc;
  }

  /** Retrieves existing file element or creates a new one. */
  private Element getFileElement(Element root, URI uri) {
    XPath xpath = DocumentHelper.createXPath("file[@uri='"+uri.toString()+"']");
    List results = xpath.selectNodes(root);

    Element element = null;
    if (results.isEmpty()) {
      element = root.addElement("file");
      element.addAttribute("uri", uri.toString());
    } else {
      element = (Element) results.get(0);
    }

    return element;
  }

  /** Retrieves existing session element or creates a new one. */
  private Element getSessionElement(Element file, Timer timer) {
    XPath xpath = DocumentHelper.createXPath("session[@id='"+timer.getId()+"']");
    List results = xpath.selectNodes(file);

    Element element = null;
    if (results.isEmpty()) {
      element = file.addElement("session");
      element.addAttribute("id", String.valueOf(timer.getId()));
    } else {
      element = (Element) results.get(0);
    }

    return element;
  }

  /** Retrieves iterator over sessions elements */
  private Iterator getSessionIterator(Element file) {
    XPath xpath = DocumentHelper.createXPath("session");
    List results = xpath.selectNodes(file);
    return results.iterator();
  }
  /** Write xml file to specified stream. */
  private void dumpXML(Document xdoc, OutputStream out) throws IOException {
    OutputFormat outformat = OutputFormat.createPrettyPrint();
    outformat.setEncoding("UTF-8");
    XMLWriter writer = new XMLWriter(out, outformat);
    writer.write(xdoc);
    writer.flush();    
  }
  
  /**********************************************************************/

  /** Added to current JawbDocument to update the SessionLogFrame for doc
   * changes */
  private class MyDocListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent e) {
      updateLogFrame();
    }
  }

  /** testing only 
  public static void main(String[] args) throws IOException {
    List tasks = Jawb.getTasks();

    SessionLogger logger = new SessionLogger();
    URI base = new File(System.getProperty("user.home")).toURI();

    Task task = null;
    for (Iterator i=tasks.iterator(); i.hasNext(); ) {
      task = (Task) i.next();
      if (task.getName().indexOf("muc") > -1)
        break;
      task = null;
    }

    for (int i=0; i<10; i++) {
      URI aifuri = URI.create(base.toString()+"tmp/test."+i+".aif.xml");
      
      try {
        URI signal = URI.create(base.toString()+"tmp/test.txt");
        JawbDocument doc = JawbDocument.fromSignal(signal, task,
                                                   "text", "UTF-8");
        doc.save(aifuri);
        
        Timer timer = new Timer();
        timer.start();
        try { Thread.sleep(200); } catch (Exception e) {}
        timer.pause(true);

        logger.updateLog(doc,timer);
        
        doc.close();
      } catch (Exception e){
        System.err.println ("Error logging: "+e.getMessage());
        e.printStackTrace();
      }
    }

    System.exit(0);
  }
  */
}
