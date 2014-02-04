
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

//Category is log4j 1.1.3. Upgrade soon.

//Actually, we're probably going to need to set our
//own timestamps at times, and that means building a logging 
//record, and you can't pass a logging record in to
//a log call in log4j, not even in the newest version.
//So we need to change to java.util.logging.

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import org.mitre.jawb.gui.JawbFrame;
import org.mitre.jawb.gui.GUIUtils;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.tasks.Task;

//I extended the class to set up the stubs, but in the end,
//I don't want this to be a child of the logger class, I don't think.

public class JawbLogger {

  private final static int DEBUG = DebugLevel.getDebugLevel(JawbLogger.class, 0);

  private Logger logger = null;
  private boolean enabled = false;

  // the logging messages
  public final static String LOG_COMMANDLINE_ARGS = "command_line_args";
  public final static String LOG_OPEN_WORKSPACE = "open_workspace";
  public final static String LOG_OPENWS_CANCEL = "cancel_open_workspace";
  public final static String LOG_CLOSE_WORKSPACE = "close_workspace";
  public final static String LOG_WORKSPACE_USERID = "workspace_userid";
  public final static String LOG_WORKSPACE_FOLDER = "workspace_folder";
  public final static String LOG_USERNAME = "log_username";
  public final static String LOG_OPEN_AIF = "open_aif";
  public final static String LOG_CLOSE_FILE = "close_file";
  public final static String LOG_REALLY_CLOSE = "really_close";
  public final static String LOG_CLOSE_CANCELED = "close_canceled";
  public final static String LOG_SAVE_DECLINED = "save_declined";
  public final static String LOG_SAVE = "save";
  public final static String LOG_WORKSPACE_SAVE = "save_wsdoc";
  public final static String LOG_IMPORT = "import";
  public final static String LOG_WORKSPACE_IMPORT_NEXT = "import_next_wsdoc";
  public final static String LOG_WORKSPACE_IMPORT_SEL = "import_sel_wsdoc";
  public final static String LOG_SAVE_AS = "save_as";
  public final static String LOG_EXPORT = "export";
  public final static String LOG_CREATE_ANNOT = "create_annot";
  public final static String LOG_CREATE_ANNOT_FAIL = "create_annot_failed";
  public final static String LOG_CREATE_TEXT_ANNOT = "create_text_annot_details";
  public final static String LOG_DELETE_TEXT_ANNOT = "delete_text_annot_details";
  public final static String LOG_DELETE_ANNOT = "delete_annot";
  public final static String LOG_DELETE_ANNOT_FAIL = "delete_annot_failed";
  public final static String LOG_CHANGE_ANNOT = "change_annot";
  public final static String LOG_BEGIN_AUTOTAG = "begin_autotag";
  public final static String LOG_END_AUTOTAG = "end_autotag";
  public final static String LOG_RELEASE_LOCK = "release_lock";
  public final static String LOG_MARK_GOLD = "mark_gold";
  public final static String LOG_MENU_SELECTION = "menu_selection";
  public final static String LOG_DASHBOARD_BUTTON = "dashboard_button";
  public final static String LOG_SEGMENT_COMPLETE = "segment_complete";
  public final static String LOG_SEGMENT_INCOMPLETE = "segment_uncomplete";
  public final static String LOG_HIGH_PRIORITY_COMPLETE = 
    "high_priority_complete";
  /** args for dialog_choice are a description of the dialog, and the 
   *  JOptionPane integer return value */
  public final static String LOG_DIALOG_CHOICE = "dialog_choice";
  /** args for openws_dialog_options are the folder selected, and the
   *  boolean value for whether or not to open the nextdoc immediately */
  public final static String LOG_OPENWS_DIALOG_OPTIONS = 
    "openws_dialog_options";
  /** arg for adj_vote is the vote identifier (string) */
  public final static String LOG_ADJUDICATION_VOTE = "adj_vote";
  /** args for adj_segment are the start and end indices, and the text */
  public final static String LOG_ADJUDICATION_SEGMENT = "adj_segment";
  /** adj_close is logged when the user cancels out of the
   * adjudicaiton dialog.  There are no arguments */
  public final static String LOG_ADJUDICATION_CLOSE = "adj_close"; 
  public final static String LOG_SYNC = "sync";
  

  private static LogUploader uploader = null;

  private String logDir;

  public boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(boolean v) {
    enabled = v;
  }

  public String getAndFlushCache() {
    String cache;
    try {
      cache = 
        ((JawbXMLFormatter)getHandlers()[0].getFormatter()).getAndFlushCache();
    } catch (Exception x) {
      // if anything faile here -- there is no handler, no formatter,
      // it's not really a JawbXMLFormatter, etc.... just return an empty
      // string
      cache = "";
    }
    return cache;
  }

  public interface LogUploader {

    public void init(String uploadURL);

    // These constants need to match up with those in the Servlet
    // servlet code is found in little-cfle
    public final static String REQ_FILENAME = "client_file_name";
    public final static String REQ_USERID = "user_name";
    public final static String REQ_FILEUPLOAD = "file";
    public final static String UTF8 = "UTF-8";

    public void sendFileContents(String username, File file) throws IOException;

    public void sendData(byte[] input, String username, String filename)
    throws IOException;

  }

  // Class LoggingUpload -- Seamus Clancy
  public static class LogUploaderImpl implements LogUploader {

    protected String uploadURL;

    public LogUploaderImpl() {
    }

    public void init(String uploadURL) {
      this.uploadURL = uploadURL;
    }

    public void sendFileContents(String username, File file) throws IOException {

      String filename = file.getName();
      InputStream is = new FileInputStream(file);

      // Get the size of the file
      long length = file.length();

      // You cannot create an array using a long type.
      // It needs to be an int type.
      // Before converting to an int type, check
      // to ensure that file is not larger than Integer.MAX_VALUE.
      if (length > Integer.MAX_VALUE) {
        // File is too large
      }

      // Create the byte array to hold the data
      byte[] bytes = new byte[(int)length];

      // Read in the bytes
      int offset = 0;
      int numRead = 0;
      while (offset < bytes.length
          && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
        offset += numRead;
      }

      // Ensure all the bytes have been read in
      if (offset < bytes.length) {
        throw new IOException("Could not completely read file "+file.getName());
      }

      // Close the input stream and return bytes
      is.close();
      sendData(bytes, username, filename);
    }

    public void sendData (byte[] input, String username, String filename) throws IOException {
      //URL url = new URL(props.getProperty("flex.logging.webservice"));
      // URL url = new URL("http://sark.mitre.org:8080/LoggingUpload/LoggingUpload");
      URL url = new URL(uploadURL);

      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setDoInput(true);
      conn.setAllowUserInteraction(true);
      String boundary = initializeMultipartContentBoundary(conn);
      DataOutputStream out = new DataOutputStream(conn.getOutputStream());
      byte[] outBytes = input;
      writeBoundary(out, boundary);
      writeFileContentDisposition(out, REQ_FILEUPLOAD, filename, "text/plain; charset=utf-8");
      out.write(outBytes);
      writeBoundary(out, boundary);
      writeContentDisposition(out, REQ_USERID);
      out.write(username.getBytes());
      writeBoundary(out, boundary);
      writeContentDisposition(out, REQ_FILENAME);
      out.write(filename.getBytes());        
      writeEndBoundary(out, boundary);
      out.flush();
      out.close();

      BufferedInputStream in = null;

      int responseCode = conn.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        in = new BufferedInputStream(conn.getErrorStream());
        System.err.println("Upload http failure: "+responseCode);
      }
      else {
        in = new BufferedInputStream(conn.getInputStream());
      }

      byte[] buf = new byte[outBytes.length * 2];
      ByteArrayOutputStream inBytes = new ByteArrayOutputStream(outBytes.length * 2);
      int n;
      while ((n = in.read(buf)) >= 0) {
        if (n > 0)
          inBytes.write(buf, 0, n);
      }
      conn.disconnect();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        System.err.println(inBytes.toString());
        throw new IOException("Upload failed: "+responseCode);
      }
      System.out.println(inBytes.toString());

      return;
    }

    private String initializeMultipartContentBoundary(HttpURLConnection conn) {
      String boundary = "---------------------------" + new Random(System.currentTimeMillis()).nextLong();
      conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
      return boundary;
    }

    private void writeEndBoundary(DataOutputStream out, String boundary) throws IOException {
      out.writeBytes("\r\n--"+boundary+"--\r\n");
    }

    private void writeFileContentDisposition(DataOutputStream out, String fieldName, String filename, String contentType) throws IOException {
      out.writeBytes("Content-Disposition: form-data; name=\""+fieldName+"\"; filename=\""+filename+"\"\r\n");
      out.writeBytes("Content-Type: "+contentType+"\r\n");
      out.writeBytes("\r\n");
    }

    private void writeContentDisposition(DataOutputStream out, String fieldName) throws IOException {
      out.writeBytes("Content-Disposition: form-data; name=\""+fieldName+"\"\r\n");
      out.writeBytes("\r\n");
    }

    private void writeBoundary(DataOutputStream out, String boundary) throws IOException {
      out.writeBytes("\r\n--"+boundary+"\r\n");
    }


  } // End LoggingUpload Class


  // Stole the guts of this from XMLFormatter.

  private class JawbXMLFormatter extends XMLFormatter {

    /** Cache log entries until the next time getAndFlushCache() is called.
      * This is used to allow emitting partial logs on demand. */
    private StringBuffer logCache = new StringBuffer();
    private Handler handler;

    public JawbXMLFormatter (Handler h) {
      super();
      this.handler = h;
    }

    public String getAndFlushCache() {
      String retval = 
        getHead(handler) + logCache.toString() + getTail(handler);
      logCache.setLength(0); // flush the cache
      return retval;
    }

    // Append a two digit number.
    private void a2(StringBuffer sb, int x) {
      if (x < 10) {
        sb.append('0');
      }
      sb.append(x);
    }

    // Append the time and date in ISO 8601 format
    private void appendISO8601(StringBuffer sb, long millis) {
      Calendar c = Calendar.getInstance();

      c.setTimeInMillis(millis);
      sb.append(c.get(Calendar.YEAR));
      sb.append('-');
      a2(sb, c.get(Calendar.MONTH) + 1);
      sb.append('-');
      a2(sb, c.get(Calendar.DAY_OF_MONTH));
      sb.append('T');
      a2(sb, c.get(Calendar.HOUR_OF_DAY));
      sb.append(':');
      a2(sb, c.get(Calendar.MINUTE));
      sb.append(':');
      a2(sb, c.get(Calendar.SECOND));
    }

    // Append to the given StringBuffer an escaped version of the
    // given text string where XML special characters have been escaped.
    // For a null string we append "<null>".
    // SAM 11/09/05: Converted to use [CDATA.
    private void escape(StringBuffer sb, String text) {
      if (text == null) {
        sb.append("[null]");
      } else if ((text.indexOf('<') > -1) ||
          (text.indexOf('>') > -1) ||
          (text.indexOf('&') > -1)) {
        sb.append("<![CDATA[");
        sb.append(text);
        sb.append("]]>");
      } else {
        sb.append(text);
      }
    }

    /**
     * Format the given message to XML.
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public String format(LogRecord record) {
      StringBuffer sb = new StringBuffer(500);
      sb.append("<record>\n");

      sb.append("  <date>");
      appendISO8601(sb, record.getMillis());
      sb.append("</date>\n");

      sb.append("  <millis>");
      sb.append(record.getMillis());
      sb.append("</millis>\n");

      sb.append("  <sequence>");
      sb.append(record.getSequenceNumber());
      sb.append("</sequence>\n");

      String name = record.getLoggerName();
      if (name != null) {
        sb.append("  <logger>");
        escape(sb, name);
        sb.append("</logger>\n");
      }

      sb.append("  <level>"); 
      escape(sb, record.getLevel().toString());
      sb.append("</level>\n");

      if (record.getSourceClassName() != null) {
        sb.append("  <class>");
        escape(sb, record.getSourceClassName());
        sb.append("</class>\n");
      }

      if (record.getSourceMethodName() != null) {
        sb.append("  <method>");
        escape(sb, record.getSourceMethodName());
        sb.append("</method>\n");
      }

      sb.append("  <thread>");
      sb.append(record.getThreadID());
      sb.append("</thread>\n");

      if (record.getMessage() != null) {
        // Format the message string and its accompanying parameters.
        String message = formatMessage(record);
        sb.append("  <message>");
        escape(sb, message);
        sb.append("</message>");
        sb.append("\n");
      }

      // If the message is being localized, output the key, resource
      // bundle name, and params.
      ResourceBundle bundle = record.getResourceBundle();
      try {
        if (bundle != null && bundle.getString(record.getMessage()) != null) {
          sb.append("  <key>");
          escape(sb, record.getMessage());
          sb.append("</key>\n");
          sb.append("  <catalog>");
          escape(sb, record.getResourceBundleName());
          sb.append("</catalog>\n");
          Object parameters[] = record.getParameters();
          for (int i = 0; i < parameters.length; i++) {
            sb.append("  <param>");
            try {
              escape(sb, parameters[i].toString());
            } catch (Exception ex) {
              sb.append("???");
            }
            sb.append("</param>\n");
          }
        }
      } catch (Exception ex) {
        // The message is not in the catalog.  Drop through.
      }

      if (record.getThrown() != null) {
        // Report on the state of the throwable.
        Throwable th = record.getThrown();
        sb.append("  <exception>\n");
        sb.append("    <message>");
        escape(sb, th.toString());
        sb.append("</message>\n");
        StackTraceElement trace[] = th.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
          StackTraceElement frame = trace[i];
          sb.append("    <frame>\n");
          sb.append("      <class>");
          escape(sb, frame.getClassName());
          sb.append("</class>\n");
          sb.append("      <method>");
          escape(sb, frame.getMethodName());
          sb.append("</method>\n");
          // Check for a line number.
          if (frame.getLineNumber() >= 0) {
            sb.append("      <line>");
            sb.append(frame.getLineNumber());
            sb.append("</line>\n");
          }
          sb.append("    </frame>\n");
        }
        sb.append("  </exception>\n");
      }

      if (record instanceof JawbLogRecord) {
        JawbLogRecord r = (JawbLogRecord) record;


        Hashtable h = r.recordHash;
        String[] attrs = r.recordAttrs;
        Object[] args = r.recordArgs;

        // Look at either the attrs or the args, whichever
        // is longer. For each i, find the attr and the arg. 
        // If the attr is not null, get the val from the
        // hashtable, NOT the arg array. Otherwise, get the 
        // val from the arg array. After that, look at the 
        // vector of additional args and add them from the
        // hash table.

        int attrLen = attrs == null ? 0 : attrs.length;
        int argLen = args == null ? 0 : args.length;
        int maxLen = Math.max(attrLen, argLen);

        for (int i = 0; i < maxLen; i++) {
          String attr = attrLen > i ? attrs[i] : null;
          Object arg = argLen > i ? args[i] : null;
          if (attr != null) {
            arg = h.get(attr);
          }
          sb.append("  <arg");
          if (attr != null) {
            sb.append(" name=\"");
            sb.append(attr);
            sb.append("\"");
          }
          sb.append(">");
          if (arg == null) {
            sb.append("[null]");
          } else {
            escape(sb, arg.toString());
          }
          sb.append("</arg>\n");
        }

        if (r.moreRecordAttrs != null) {
          Object[] moreAttrs = r.moreRecordAttrs.toArray();

          for (int i = 0; i < moreAttrs.length; i++) {
            sb.append("  <arg name=\"");
            sb.append(moreAttrs[i].toString());
            sb.append("\">");
            Object arg = h.get(moreAttrs[i]);
            if (arg == null) {
              sb.append("[null]");
            } else {
              escape(sb, arg.toString());
            }
            sb.append("</arg>\n");
          }
        }
      }

      sb.append("</record>\n");
      logCache.append(sb);
      return sb.toString();
    }

  }

  private class LoggerFilenameFilter implements FilenameFilter {

    String logPrefix = "jawb_" + System.getProperty("user.name");
    private String extension;

    public LoggerFilenameFilter() {
      this(".log");
    }

    public LoggerFilenameFilter(String extension) {
      Preferences prefs = Jawb.initPreferences();
      String tag = prefs.getPreference(Preferences.LOG_FILENAME_TAG);
      if (tag != null) {
        logPrefix += "_"+tag;
      }
      this.extension = extension;
    }

    public boolean accept(File dir, String name) {
      if (name.startsWith(logPrefix) && name.endsWith(extension)) {
        return true;
      } else {
        return false;
      }
    }

  }


  // Initializer. Will have to be redone for later versions of log4j.

  private File logFile;
  private LinkedHashMap savedFileHash;

  private Task task = null;

  public JawbLogger(String[] args) {

    Preferences prefs = Jawb.initPreferences();

    // Step 0: see what log files there are, first. We can't 
    // ask the logger directly, dammit.

    // See if the user has specified a log path
    // if so set that to be the logDir
    // if not use either java.io.tmpdir or user.home
    // make sure that the desired path exists and is writable 
    
    logDir = null;
    String logDirError = "";

    String tryDir[] = {prefs.getPreference(Preferences.LOG_PATH),
                       System.getProperty("java.io.tmpdir"),
                       System.getProperty("user.home")};

    for (int i=0; (i<tryDir.length && logDir == null); i++) {
      try {
        //Create the directory if needed TODO: If they ever fix this bug
        //(http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6244047)
        //in the Java Logging API we wouldn't need this hack 
        File logDirFile = new File(tryDir[i]);
        if (!logDirFile.exists() ) {
          logDirFile.mkdirs();
          GUIUtils.showMessage("Creating missing logging directory: " + 
                               tryDir[i]);
        }
        // if nothing has blown a gasket so far, and we can write to this
        // directory, settle on this as our logDir
        if (logDirFile.canWrite()) {
          logDir = tryDir[i];
        }
      } catch(Exception e) {
        logDirError = logDirError + "Unable to log to directory: " +  
          tryDir[i] + "\n\t" + e + "\n";
        System.err.println(logDirError);
        e.printStackTrace();
      }
    }

    if (tryDir[0] != null && !tryDir[0].equals("") && 
        !tryDir[0].equals(logDir)) {
      // user specified a non-null non-empty directory for logging, but
      // that's not where we're logging 
      System.err.println("logging to directory: " + logDir);
      GUIUtils.showError(logDirError + "\nlogging to directory: " + logDir);
    }

    // We list the logDir, and check to see whether there are any 
    // elements which start with the log prefix for the given user.
    File [] oldLogs = {};
    if (logDir != null)
      oldLogs = new File(logDir).listFiles(new LoggerFilenameFilter());

    logger = Logger.getLogger("org.mitre.jawb.Jawb");

    // Ultimately, we'll look in the prefs to
    // figure out how to configure the logger. 
    // But right now, we just log stuff. There always
    // has to be a logger, since we're always calling
    // logger methods, so we've created one in the 
    // variable declarations.

    // Step 1: tell the logger not to use its
    // parent handlers. The default logging file
    // for Java assigns a console handler to the 
    // default global log, and the only way to turn
    // it off is to pass in a new log prefs file on
    // the command line (since the logger is initialized
    // before the prefs are read, trying to override
    // the log prefs location in the prefs file is
    // useless). 



    // We want the time, the username, and some unique number.
    Date d = new Date();
    String logFileName;
    if (logDir != null)
      logFileName = logDir+"/jawb_"+System.getProperty("user.name");
    else
      logFileName = "jawb_"+System.getProperty("user.name");

    String tag = prefs.getPreference(Preferences.LOG_FILENAME_TAG);
    if (tag != null) {
      logFileName += "_"+tag;
    }
    
    logFileName += "_" + d.toString().replace(' ', '_').replace(':', '_') + "_%u.log";
    boolean handlerAdded = false;


    savedFileHash = new LinkedHashMap();

    // Let's get more sophisticated. We introduce two keys in the
    // preferences, not just one: enabled and don't ask. If we're 
    // enabled, but don't ask isn't set, we check before we enable. 
    // Also, we want to dispense with the previous logs, either by 
    // deleting them, mailing them, or uploading them somewhere via HTTP. 
    // Problem is, you can't get the previous logs except by dealing 
    // with the pattern directly. 

    // Step 1: dispense with previous logs. We use the method
    // that the FileHandler uses to determine the value of the
    // pattern.

    if (prefs.getBoolean (Preferences.LOG_ENABLED_KEY)) {
      // true if the key is present and the value is true
      if (prefs.getBoolean(Preferences.LOG_ENABLED_WARN_KEY)) {
        // Pop up a JOptionPane here to warn and give people an opportunity
        // to disable and not to see this again. I can do that by setting
        // the preference in the prefs, and it'll get written out the
        // next opportunity to save. Or I can just call storePreferences().
        // Actually, I need more than that. I need a popup which allows
        // me to select an option not to show this dialog again.
        JCheckBox dontAsk = new JCheckBox("Don't ask me again", false);
        String msg = "<html><p>Callisto would like to collect some information about your<br>" +
        "session in a log file: " + 
        "<ul><li>when you open, import and close files;" +
        "<li>when you create and delete annotations;" +
        "<li>various task-specific actions</ul>" +
        (prefs.getBoolean(Preferences.LOG_DISPENSATION_UPLOAD_ANNOTATION_RESULTS) ?
            "<p>Your saved annotations will also be uploaded." : "") +
            "<p>If Callisto is configured appropriately, this record of your session will be sent to<br>" +
            "the Callisto developers for analysis when you exit Callisto. <br><br>OK?</html>";
        Object[] objs = {msg, dontAsk};
        int choice = JOptionPane.showConfirmDialog(JawbFrame
            .getFrames()[0], objs,
            "May we collect logs?",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
          enabled = true;
        }
        if (dontAsk.isSelected()) {
          // Make sure you write out all the changes.
          // If the answer is no, write no.
          if (choice == JOptionPane.NO_OPTION) {
            prefs.setPreference(Preferences.LOG_ENABLED_KEY, false);
          }
          prefs.setPreference(Preferences.LOG_ENABLED_WARN_KEY, false);
          Jawb.storePreferences();
        }
      } else {
        enabled = true;
      }
    }

    if (enabled) {
      try {
        Handler f = new FileHandler(logFileName);
        f.setEncoding("UTF-8");
        f.setFormatter(new JawbXMLFormatter(f));
        logger.addHandler(f);
        handlerAdded = true;
      } catch (SecurityException e1) {
        if (DEBUG > 0)
          e1.printStackTrace();
        System.err.println("Error during log initialization: can't create special XML formatter with log file " + 
            logFileName + "; logging to console.");
      } catch (IOException e1) {
        if (DEBUG > 0)
          e1.printStackTrace();
        System.err.println("Error during log initialization: can't create log file " + 
            logFileName + "; logging to console.");
      }
    }

    if (handlerAdded) {
      logger.setUseParentHandlers(false);
    }

    // Tell the manager (assuming it's a JawbLogManager) that the
    // logging has started.

    LogManager mgr = LogManager.getLogManager();
    if (mgr instanceof JawbLogManager) {
      ((JawbLogManager) mgr).setJawbLoggingStarted();
    }

    info(LOG_COMMANDLINE_ARGS, args);
    info(LOG_USERNAME, new Object[] { System.getProperty("user.name") });

    // Now that we've logged some stuff, check the logs again.

    if (handlerAdded) {
      File[] nowLogs = {};
      if (logDir != null)
        nowLogs = new File(logDir).listFiles(new LoggerFilenameFilter());
      if (nowLogs.length > oldLogs.length) {
        // If there's a new log (and there should be, if we're enabled),
        // then find the log name and save it.
        List logList = Arrays.asList(oldLogs);
        for (int i = 0; i < nowLogs.length; i++) {
          if (!logList.contains(nowLogs[i])) {
            logFile = nowLogs[i];
            break;
          }
        }
      }
    }
  }

  private void dischargeOldLogs(String logDir) {
    if (logDir == null)
      return;
    LogUploader uploader = getUploader();
    if (uploader != null) {
      File[] oldLogZips = new File(logDir).listFiles(new LoggerFilenameFilter(".zip"));
      if (oldLogZips != null && oldLogZips.length > 0) {
        String userName = System.getProperty("user.name");
        for (int i = 0; i < oldLogZips.length; i++) {
          File zip = oldLogZips[i];
          if (zip.exists() && zip.length() > 0) {
            try {
              uploader.sendFileContents(userName, zip);
              System.out.println("Uploaded old log "+zip.getName());
              zip.delete();
            } catch (IOException e) {
              if (zip.exists())
                System.err.println("Failed to upload old log "+zip.getName());
              e.printStackTrace();
            }
          }
        }
      }
      File[] nowLogs = new File(logDir).listFiles(new LoggerFilenameFilter());
      // If there's a new log (and there should be, if we're enabled),
      // then find the log name and save it.
      for (int i = 0; i < nowLogs.length; i++) {
        if (nowLogs[i] != logFile && ! new File(nowLogs[i].getAbsolutePath() + ".lck").exists()) {
          dischargeLog(nowLogs[i]);
        }
      }
    }
  }


  public void dischargeLog() {
    dischargeOldLogs(logDir);
    if (!enabled) return;
    dischargeLog(logFile);
  }

  private void dischargeLog(File logFile) {
    if (logFile == null) return;
    if (! logFile.exists() || logFile.length() == 0)
      return;

    System.err.println("Discharging log "+logFile);
    
    Preferences prefs = Jawb.initPreferences();
    // What happens in here depends on the preferences.
    String logDispensation = prefs.getPreference(Preferences.LOG_DISPENSATION_KEY);
    boolean delete = true;


    if (logDispensation == null) {
      // Do nothing.
    } else if (logDispensation.equals(Preferences.LOG_DISPENSATION_EMAIL_VALUE)) {
      // email the log to the appropriate folks.
      String emailAddr = prefs.getPreference(Preferences.LOG_DISPENSATION_EMAIL_ADDR_KEY);
      if (emailAddr != null) {

      }
    } else if (logDispensation.equals(Preferences.LOG_DISPENSATION_UPLOAD_VALUE)) {
      // upload the log to the appropriate place.
      LogUploader uploader = getUploader();
      if (uploader != null) {
        String userName = System.getProperty("user.name");
        try {
          // Galen has a good idea. What we want to do is add files
          // in subdirectories by millisecond, so that we can (a) keep
          // potentially identical files separate and (b) not have to
          // use absolute paths, which get grossly expanded.
          if (prefs.getBoolean(Preferences.LOG_DISPENSATION_UPLOAD_ANNOTATION_RESULTS)) {
            // Set up a zip file.
            // Code credit: http://javaalmanac.com/egs/java.util.zip/CreateZip.html.
            // Add the logfile first.
            savedFileHash.put(logFile.getAbsolutePath(), null);

            // Now, create an iterator.
            Iterator filenames = savedFileHash.keySet().iterator();

            // Create a buffer for reading the files
            byte[] buf = new byte[1024];


            // Create the ZIP file. But first, the pathname.
            // Stupid language doesn't have any pathname manipulation
            // tools.
            String outLog = logFile.getAbsolutePath();
            String outPath = outLog.substring(0, outLog.length() - 4);
            File outDir = new File(outPath);
            String outFilename = outPath + ".zip";
            // SAM: The outstream should be a byte stream; no need to save it,
            // at least I don't think there is. Hope this doesn't cause
            // memory overflow. Hm. Probably will. Better write a temp file
            // first. Fortunately, the outFilename is already in the
            // temp directory.
            try {

              ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));

              // Compress the files
              while (filenames.hasNext()) {
                String filename = (String) filenames.next();
                Long longVal = (Long) savedFileHash.get(filename);
                FileInputStream in = new FileInputStream(filename);

                // Add ZIP entry to output stream. The entry should be
                // the log directory name, followed by the timestamp, if
                // present, and then the name component.
                String tmpFile = new File(filename).getName();
                if (longVal == null) {
                  out.putNextEntry(new ZipEntry(new File(outDir.getName(), tmpFile).getPath()));
                } else {
                  out.putNextEntry(new ZipEntry(new File(outDir.getName(), 
                      new File(longVal.toString(), tmpFile).getPath()).getPath()));
                }

                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0) {
                  out.write(buf, 0, len);
                }

                // Complete the entry
                out.closeEntry();
                in.close();
              }              
              // Complete the ZIP file
              out.close();
            } catch (IOException e) {
            }
            File outFile = new File(outFilename);
            System.err.println("Uploading log "+outFile);
            uploader.sendFileContents(userName, outFile);
            // Clean up my mess.
            outFile.delete();
          } else {
            System.err.println("Uploading log "+logFile);
            uploader.sendFileContents(userName, logFile);
          }
        } catch (IOException e) {
//          if (DEBUG > 0)
            e.printStackTrace();
        }
      }
    } else if (logDispensation.equals(Preferences.LOG_DISPENSATION_KEEP_VALUE)) {
      delete = false;
    }

    // Unless the log is explicitly retained, delete it.
    if (delete) {
      logFile.delete();
    }
  }

  private LogUploader getUploader() {
    LogUploader uploader = null;
    Preferences prefs = Jawb.initPreferences(); 
    String uploadURL = prefs.getPreference(Preferences.LOG_DISPENSATION_UPLOAD_URL_KEY);
    if (uploadURL != null) {
      uploader = JawbLogger.uploader;
      if (uploader == null) {
        uploader = new LogUploaderImpl();
      }
      uploader.init(uploadURL);
    }
    return uploader;
  }


  // Now, my customized methods. We start with the customized save
  // methods, because those are the ones we need to monitor for
  // capturing saved files in our annotation experiments, e.g., in FLEX.

  public void logSave(String event, String path) {
    info(event, new Object[] { path });
    savedFileHash.put(path, new Long(System.currentTimeMillis()));
  }

  public void info(String event, Object[] args, Throwable arg1) {
    if (enabled && logger.isLoggable(Level.INFO)) {
      logStructured(Level.INFO, event, null, args, 0, false, arg1);
    }
  }

  public void info(String event, Object[] args) {
    if (enabled && logger.isLoggable(Level.INFO)) {
      logStructured(Level.INFO, event, null, args, 0, false, null);
    }
  }

  public void info(String event, String[] attrs, Object[] args, Throwable arg1) {
    if (enabled && logger.isLoggable(Level.INFO)) {
      logStructured(Level.INFO, event, attrs, args, 0, false, arg1);
    }
  }

  public void info(String event, String[] attrs, Object[] args) {
    if (enabled && logger.isLoggable(Level.INFO)) {
      logStructured(Level.INFO, event, attrs, args, 0, false, null);
    }
  }

  public void info(String event, String[] attrs, Object[] args, long timeStamp, Throwable arg1) {
    if (enabled && logger.isLoggable(Level.INFO)) {
      logStructured(Level.INFO, event, attrs, args, timeStamp, true, arg1);
    }
  }

  public void info(String event, String[] attrs, Object[] args, long timeStamp) {
    if (enabled && logger.isLoggable(Level.INFO)) {
      logStructured(Level.INFO, event, attrs, args, timeStamp, true, null);
    }
  }

  public void log(Level p, String event, String[] attrs, Object[] args) {
    if (enabled && logger.isLoggable(p)) {
      logStructured(p, event, attrs, args, 0, false, null);
    }

  }

  public void log(Level p, String event, String[] attrs, Object[] args, long timeStamp) {
    if (enabled && logger.isLoggable(p)) {
      logStructured(p, event, attrs, args, timeStamp, true, null);
    }
  }

  public JawbLogRecord createLogRecord(Level p, String event, String[] attrs, Object[] args) {

    if (enabled && logger.isLoggable(p)) {
      return createJawbLogRecord(p, event, attrs, args, 0, false, null);
    } else {
      return null;
    }
  }

  public JawbLogRecord createLogRecord(Level p, String event, String[] attrs, Object[] args, long timeStamp) {

    if (enabled && logger.isLoggable(p)) {
      return createJawbLogRecord(p, event, attrs, args, timeStamp, true, null);
    } else {
      return null;
    }
  }

  // Make this public because I think someone is going to want to 
  // hang on to it.

  public class JawbLogRecord extends LogRecord {

    private Hashtable recordHash;
    private Vector moreRecordAttrs;
    private String[] recordAttrs;
    private Object[] recordArgs;

    private JawbLogRecord(Level p, String event, String[] attrs, Object[] args, Throwable th) {
      super(p, event);
      if (th != null) {
        setThrown(th);
      }
      setLoggerName(logger.getName());

      // Remember, attrs can be null, or any particular attr can be null. 
      // They need to be in order. So we need to keep the vectors AND
      // keep the hash.

      recordAttrs = attrs;
      recordArgs = args;

      if ((attrs != null) && (attrs.length > 0)) {
        for (int i = 0; i < attrs.length; i++) {
          String a = attrs[i];
          Object o = null;
          if (a != null) {
            if (recordHash == null) {
              recordHash = new Hashtable();
            }
            if (args.length > i) {
              o = args[i];
            }
            if (o != null) {
              // For some bizarre reason, you can't put NULL in a hashtable.
              recordHash.put(a, o);
            } else {
              // And if it IS null, I have to make sure it's not there.
              recordHash.remove(a);
            }
          }
        }
      }
    }

    private JawbLogRecord(Level p, String event, String[] attrs, Object[] args, long timeStamp, Throwable th) {
      this(p, event, attrs, args, th);

      setMillis(timeStamp);
    }

    public void addLogAttribute(String attr, Object val) {
      // Don't deal with null attrs.
      if (attr == null) {
        return;
      }
      if (!recordHash.containsKey(attr)) {
        // Add the key. 
        if (moreRecordAttrs == null) {
          moreRecordAttrs = new Vector(0);
        }
        moreRecordAttrs.add(attr);
      }
      recordHash.put(attr, val);
    }
  }

  private JawbLogRecord createJawbLogRecord(Level p, String event, String[] attrs, Object[] args, 
      long timeStamp, boolean useTimeStamp, Throwable th) {

    if (useTimeStamp) {
      return new JawbLogRecord(p, event, attrs, args, timeStamp, th);
    } else {
      return new JawbLogRecord(p, event, attrs, args, th);
    }
  }

  private void logStructured(Level p, String event, String[] attrs, Object[] args, 
      long timeStamp, boolean useTimeStamp, Throwable th) {
    logger.log(createJawbLogRecord(p, event, attrs, args, timeStamp, useTimeStamp, th));
  }

  private JawbLogger(String parentName, Task task, boolean parentEnabled) {

    this.task  = task;

    logger = Logger.getLogger(parentName + "." + task.getName());

    Preferences prefs = Jawb.initPreferences();

    enabled = parentEnabled;

    String taskName = task == null ? "" : task.getName()+'.';
    String uploaderClassName = prefs.getPreference(taskName+Preferences.LOG_DISPENSATION_UPLOADER_CLASS);
    System.err.println("JawbLogger: uploaderClass for task "+task.getName()+": "+uploaderClassName);
    if (uploaderClassName != null) {
      try {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        if (task != null) {
          classLoader = task.getClass().getClassLoader();
        }
        else {
          System.err.println("JawbLogger: Task is null, using system class loader");
        }
        Class uploaderClass = classLoader.loadClass(uploaderClassName);
        uploader = (LogUploader) uploaderClass.newInstance();
        System.err.println("JawbLogger: initialized log uploader from task "+taskName);
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      } catch (InstantiationException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }

  }

  public JawbLogger JawbTaskLogger(Task task) {

    // Set up the instance variable to get a sublogger.
    // This is a method on the logger to ensure that
    // it already exists. Use a private constructor.

    return new JawbLogger(logger.getName(), task, enabled);

  }

  // Proxying for the real logger.

  public void addHandler(Handler arg0) throws SecurityException {
    logger.addHandler(arg0);
  }

  public void config(String arg0) {
    logger.config(arg0);
  }

  public void entering(String arg0, String arg1, Object arg2) {
    if (enabled)
      logger.entering(arg0, arg1, arg2);
  }

  public void entering(String arg0, String arg1, Object[] arg2) {
    if (enabled) logger.entering(arg0, arg1, arg2);
  }

  public void entering(String arg0, String arg1) {
    if (enabled) logger.entering(arg0, arg1);
  }

  public void exiting(String arg0, String arg1, Object arg2) {
    if (enabled) logger.exiting(arg0, arg1, arg2);
  }

  public void exiting(String arg0, String arg1) {
    if (enabled) logger.exiting(arg0, arg1);
  }

  public void fine(String arg0) {
    if (enabled) logger.fine(arg0);
  }

  public void finer(String arg0) {
    if (enabled) logger.finer(arg0);
  }

  public void finest(String arg0) {
    if (enabled) logger.finest(arg0);
  }

  public Filter getFilter() {
    return logger.getFilter();
  }

  public Handler[] getHandlers() {
    return logger.getHandlers();
  }

  public Level getLevel() {
    return logger.getLevel();
  }

  public String getName() {
    return logger.getName();
  }

  public Logger getParent() {
    return logger.getParent();
  }

  public ResourceBundle getResourceBundle() {
    return logger.getResourceBundle();
  }

  public String getResourceBundleName() {
    return logger.getResourceBundleName();
  }

  public boolean getUseParentHandlers() {
    return logger.getUseParentHandlers();
  }

  public int hashCode() {
    return logger.hashCode();
  }

  public void info(String arg0) {
    if (enabled) logger.info(arg0);
  }

  public boolean isLoggable(Level arg0) {
    return enabled && logger.isLoggable(arg0);
  }

  public void log(Level arg0, String arg1, Object arg2) {
    if (enabled) logger.log(arg0, arg1, arg2);
  }

  public void log(Level arg0, String arg1, Object[] arg2) {
    if (enabled) logger.log(arg0, arg1, arg2);
  }

  public void log(Level arg0, String arg1, Throwable arg2) {
    if (enabled) logger.log(arg0, arg1, arg2);
  }

  public void log(Level arg0, String arg1) {
    if (enabled) logger.log(arg0, arg1);
  }

  public void log(LogRecord arg0) {
    if (enabled) logger.log(arg0);
  }

  public void logp(Level arg0, String arg1, String arg2, String arg3, Object arg4) {
    if (enabled) logger.logp(arg0, arg1, arg2, arg3, arg4);
  }

  public void logp(Level arg0, String arg1, String arg2, String arg3, Object[] arg4) {
    if (enabled) logger.logp(arg0, arg1, arg2, arg3, arg4);
  }

  public void logp(Level arg0, String arg1, String arg2, String arg3, Throwable arg4) {
    if (enabled) logger.logp(arg0, arg1, arg2, arg3, arg4);
  }

  public void logp(Level arg0, String arg1, String arg2, String arg3) {
    if (enabled) logger.logp(arg0, arg1, arg2, arg3);
  }

  public void logrb(Level arg0, String arg1, String arg2, String arg3, String arg4, Object arg5) {
    if (enabled) logger.logrb(arg0, arg1, arg2, arg3, arg4, arg5);
  }

  public void logrb(Level arg0, String arg1, String arg2, String arg3, String arg4, Object[] arg5) {
    if (enabled) logger.logrb(arg0, arg1, arg2, arg3, arg4, arg5);
  }

  public void logrb(Level arg0, String arg1, String arg2, String arg3, String arg4, Throwable arg5) {
    if (enabled) logger.logrb(arg0, arg1, arg2, arg3, arg4, arg5);
  }

  public void logrb(Level arg0, String arg1, String arg2, String arg3, String arg4) {
    if (enabled) logger.logrb(arg0, arg1, arg2, arg3, arg4);
  }

  public void removeHandler(Handler arg0) throws SecurityException {
    logger.removeHandler(arg0);
  }

  public void setFilter(Filter arg0) throws SecurityException {
    logger.setFilter(arg0);
  }

  public void setLevel(Level arg0) throws SecurityException {
    logger.setLevel(arg0);
  }

  public void setParent(Logger arg0) {
    logger.setParent(arg0);
  }

  public void setUseParentHandlers(boolean arg0) {
    logger.setUseParentHandlers(arg0);
  }

  public void severe(String arg0) {
    if (enabled) logger.severe(arg0);
  }

  public void throwing(String arg0, String arg1, Throwable arg2) {
    if (enabled) logger.throwing(arg0, arg1, arg2);
  }

  public void warning(String arg0) {
    if (enabled) logger.warning(arg0);
  }

}
