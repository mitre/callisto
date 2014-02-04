
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

package org.mitre.jawb;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.lang.reflect.Constructor;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.mitre.jawb.gui.*;
import org.mitre.jawb.io.BrowserLauncher;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.ToocaanTask;
import org.mitre.jawb.tasks.TaskManager;
import org.mitre.jawb.tasks.preannotate.PreAnnotateLocalImporter;
import org.mitre.jawb.tasks.preannotate.TallalSystem;
import org.mitre.jawb.atlas.PhraseTaggingAnnotation;
import org.mitre.jawb.atlas.AWBAnnotation;

import org.mitre.jawb.services.CallistoMATClient;
// TODO-MAT need to get rid of this
// import org.mitre.jawb.services.CallistoMATClientImpl;

/**
 * Annotator 'Main' class: Launches application, handles multiple frames and
 * manages with global resources.
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 */
public class Jawb {
  
  public static final ByteArrayOutputStream DEBUG_OUTPUT_STREAM = new ByteArrayOutputStream();


  public static org.mitre.jawb.gui.JawbFrame ednaRaceConditionFilledHack = null;
  
  // debugging 0==off
  private static int DEBUG = 0;
  
  // some image loading stuff needs this first
  private static HashMap imageCache = new HashMap();
  private static MediaTracker tracker = new MediaTracker(new EmptyComponent());
  private static int mediaTrackerID = 0;
  
  /***********************************************************************/
  /* PUBLIC CONSTANTS
  /***********************************************************************/
  
  /* Current version and copyright is now maintained in Version.java by the
   * build process set the VERSION variable in the build.xml file. 
   */
  
  /** Developers, Contributors, Collaborators */
  public static final String DEVELOPERS    = "Developers: Chad McHenry, Robyn Kozierok, Laurel Riek";
  public static final String CONTRIBUTORS  = "Contributors: David Day, William Morgan, James Van Guilder, Ed Slavich";
  public static final String COLLABORATORS = "Collaborators: Lisa Ferro, Janet Hitzeman, Marcia Lazo, Marc Vilain";
  public static final String CONTACT       = "Contact: David Day, day@mitre.org, 781-271-2854";
  
  /** Icon all frames can/should use */
  public static final Image ICON_IMAGE = getImage("awb_icon_32.gif",null);
  
  /***********************************************************************/
  /* PRIVATE CONSTANTS
  /***********************************************************************/
  
  /** Default name of users settings directory */
  private static final String USER_DIR = ".callisto";
  
  /** Default name of tasks subdir in user and system directories */
  private static final String TASKS_DIR = "tasks";
  
  /** Default name of resources subdir in jar, user, and system directories */
  private static final String RESOURCE_DIR = "resources";
  
  /** Default name of preferences file in home and system directories */
  private static final String PREFS_FILE = "callisto.prefs";
  
  /** Resource in jar, relative to Jawb.class */
  private static final String JAR_PREFS = RESOURCE_DIR + "/" + PREFS_FILE;
  
  /***********************************************************************/
  /* Runtime constants for this invocation of Callisto */
  /***********************************************************************/
  
  /** Local, system wide Callisto settings directory. */
  private static File callistoHome = null;
  /** Local, system wide Callisto preferences. (may be null)*/
  private static File callistoPrefsFile = null;
  /** Local, system wide Callisto task directory. */
  private static File callistoTasksDir = null;
  
  /** User settings directory. */
  private static File userHome = null;
  /** User preferences file (may be null). */
  private static File userPrefsFile = null;
  /** Users default directory to load tasks from. */
  private static File userTasksDir = null;
  /** Indicates that no user preferences are to be used */
  private static boolean noUserPrefs = false;
  
  // global default preferences
  private static Properties defaultProps = null;
  // default preferences of tasks
  private static Properties taskProps = null;
  // global user properties
  private static Properties userProps = null;
  // global preferences backed by userProps @see #initPreferences
  private static Preferences prefs = null;
  
  // splash screen
  private static org.mitre.jawb.gui.SplashScreen splash;
  // loads and maintains tasks
  private static TaskManager taskManager;
  
  /** known actions @see addAction () */
  private static HashMap actionMap = null;
  
  /** determines whether or not to do the GUI-related actions in initTasks */
  private static boolean doInitGUI = false;
  
  /** SAM 10/11/05: added logging */
  private static JawbLogger jawbLogger = null;

  /** indicates whether or not the TooCAAn extensions are installed **/
  private static boolean includesToocaan = false;

  /** constructor used to construct new instances of the CallistoMATClient **/
  private static Constructor callistoMATClientConstructor;

  /** It's a singleton! */
  private Jawb() {}
  
  /**
   * Home directory of callisto containing its jar file, libs and default task.
   */
  public static File getCallistoHome() {
    return callistoHome;
  }
  
  /**
   * Main entry point for the Annotator.<p>
   *
   * Usage: Main [-p prefs]* [-q] [files...]
   * <ul>
   * <li> -p: use the next argument as a preferences file. Loaded in the order
   * specified, later prefs files will override earlier preferences. User
   * preferences are loaded last.
   * <li> -q: do not load user preferences
   * <li> files will be loaded at startup
   * </ul>
   * @param args Invocation parameters.
   * @throws IOException
   * @throws SecurityException
   */
  public static void main(String[] args) {
    try {
    
      final PrintStream debugOutputStream = new PrintStream(DEBUG_OUTPUT_STREAM);
      final PrintStream sysOut = System.out;
      final PrintStream sysErr = System.err;
      class DebugStream extends FilterOutputStream {
        DebugStream(PrintStream out) {
          super(out);
        }

        public void close() throws IOException {
          synchronized (debugOutputStream) {
            debugOutputStream.close();
            super.close();
          }
        }

        public synchronized void flush() throws IOException {
          synchronized (debugOutputStream) {
            debugOutputStream.flush();
            super.flush();
          }
        }

        public void write(byte[] b) throws IOException {
          synchronized (debugOutputStream) {
            super.write(b);
          }
        }

        public void write(byte[] buf, int off, int len) throws IOException {
          synchronized (debugOutputStream) {
//            debugOutputStream.write(buf, off, len);
            super.write(buf, off, len);
          }
        }

        public void write(int b) throws IOException {
          synchronized (debugOutputStream) {
            debugOutputStream.write(b);
            super.write(b);
          }
        }

      };
      System.setOut(new PrintStream(new DebugStream(System.out), true, "UTF-8"));
      System.setErr(new PrintStream(new DebugStream(System.err), true, "UTF-8"));
  } catch (UnsupportedEncodingException e1) {
      e1.printStackTrace();
  }
    
    // remove the parameters, and pass the file names to the newFrame call Any
    // more complex, and we should really use the gnu.getopt package.
    for (int i=0; i<args.length; i++) {
      if (args[i].charAt(0) != '-')
        break; // assumes the remaining arguments are files to load
      
      if (args[i].equals("-p")) {
        // OK, OK... I'm ignoring it.
        ++i;
        
      } else if (args[i].equals("-q")) {
        noUserPrefs = true; // causes not to be loaded @see initLocations
        
      } else {
        System.err.println("Unrecognized flag: "+args[i]);
      }
    }
    
    System.err.println("Callisto v" + Version.version() +
    " - (c) " + Version.copyright() +"\n");
    if (! Version.isJREValid()) {
      String msg = "Callisto requires a java version " + Version.minJREVersion() +
      " or higher\nCurrent version: "+System.getProperty("java.version");
      System.err.println(msg);
      JOptionPane.showMessageDialog(null, msg, "Java Version Error", JOptionPane.ERROR_MESSAGE);
      System.exit(-1);
    }
    System.err.println("Java version: "+System.getProperty("java.version"));
    System.setProperty("java.util.logging.manager", "org.mitre.jawb.JawbLogManager");
    
    initLocations();
    
    showSplashScreen(true);
    
    setSplashProgress(20, "Loading Preferences");
    initPreferences();
    
    initLog(args);

    initToocaan();
    
    setSplashProgress(40, "Initializing Tasks");
    doInitGUI = true;
    initTasks();
    
    // Currently, if no tasks are defined, a RuntimeExceptionIs is thrown
    try {
      // pass remaining args in as fileNames... later
      setSplashProgress(80, "User Interface");
      
      // this will catch any uncaught exceptions originating from an awt event
      // Should be done using UncaughtExceptionGroup, but that wasn't working
      new UncaughtExceptionQueue();
      JawbFrame frame = new JawbFrame();
      
      GUIUtils.initFileChooser();
      
      Runnable splashAway = new Runnable() {
        public void run() {
          showSplashScreen(false);
        }};
        SwingUtilities.invokeLater(splashAway);
        
        frame.setVisible(true);
        frame.setStatus("Ready");
        
    } catch (Exception e) {
      String error = "Abnormal Termination: "+e.getMessage();
      GUIUtils.showError(error);
      e.printStackTrace();
      System.exit(-1);
    }
  }
  
  public static JawbLogger getLogger() {
    // Tasks might need this in order to log to it.
    return jawbLogger;
  }

  private static void initLog(String[] args) {
    
    // This only happens once.
    
    if (jawbLogger != null)
      return;
    
    jawbLogger = new JawbLogger(args);
    
  }

  public static boolean includesToocaan() {
    return includesToocaan;
  }

  /** finds the CallistoMATClient implementation class and gets its 
   *  constructor.  If a constructor is successfully found, we use this
   *  as evidence that the TooCAAn package is available, and set
   *  the includesToocaan boolean accordingly
   */
  private static void initToocaan() {
    try {
      Class clazz = 
        Class.forName("org.mitre.jawb.toocaan.CallistoMATClientImpl");
      callistoMATClientConstructor =
        clazz.getConstructor(new Class[] {String.class, String.class,
                                          String.class, String.class});
      // if we haven't hit an exception yet, we must have the Toocaan
      // stuff installed
      includesToocaan = true;
    } catch (Exception ex) {
      callistoMATClientConstructor = null;
      includesToocaan = false;
    }
  }

  private static Constructor getCallistoMATClientConstructor() {
    return callistoMATClientConstructor;
  }
  
  /**
   * Finds the 'system wide' Callisto directory, and users personal
   * directories (possibly creating that structure), which may contain local
   * system resources such as preferences and tasks.  This tries to set the
   * System directory to be the directory which <i>contains</i> the
   * Callisto.jar file, or is the directory <i>above</i> the classpath
   * directory which parents the callisto classes
   * (ie. [callistoHome]/classes/org/mitre/...), and failing that, will fall
   * back to the JRE's "user.dir" property, which is generally the directory
   * the program was executed from.
   */
  private static void initLocations() {
    if (callistoHome != null)
      return;
    
    // Local system settings directory
    String homeName = System.getProperty("callisto.home");
    // Users can set the 'callisto.home' to override looking next to the .jar
    if (homeName != null) {
      callistoHome = new File(homeName);
      
    } else {
      // OK, they didn't.  The callisto home dir will be the one which
      // contains the Callisto.jar file (if run from jar), or, if run from
      // classfiles on the classpath, we try to go one level above the
      // directory of the classpath which contains jawb classes (ie.
      // foo/classes/org... would have foo/tasks/ next to it). In rare cases
      // (cp = "C:\" or "/") that's not possible.
      URL classRes = Jawb.class.getResource("Jawb.class");
      String proto = classRes.getProtocol();
      
      if (proto.equals("jar")) {
        String path = classRes.getPath(); // specifies jar file and class w/in
        path = path.substring(0, path.lastIndexOf('!')); // just the jar file
        callistoHome = new File(URI.create(path)).getParentFile();
        
      } else if (proto.equals("file")) {
        String cPath = "/"+Jawb.class.getName().replace('.','/')+".class";
        String path = classRes.toString();
        path = path.substring(0, path.lastIndexOf(cPath));
        File cpDir = new File(URI.create(path));
        
        // make callistoHome parent of classpath directory if possible, else
        // stay in cp
        callistoHome = cpDir.getParentFile();
        if (callistoHome == null)
          callistoHome = cpDir;
        
      } else {
        System.err.println("Unknown Protocol: "+proto+
        "\nCannot determine shared Task directory");
      }
    }
    // fall back to user's home
    if (callistoHome == null)
      callistoHome = new File(System.getProperty("user.dir"));
    
    // not found or un readable? ignore silently
    if (! callistoHome.isDirectory() || ! callistoHome.canRead())
      callistoHome = null;
    
    // now find tasks based on callistoHome
    if (callistoHome != null) {
      
      // Local system task directory
      callistoTasksDir = new File(callistoHome, TASKS_DIR);
      if (! callistoTasksDir.isDirectory() || ! callistoTasksDir.canRead()) {
        System.err.println("Callisto system task directory inaccessable: "+
        callistoTasksDir);
      }
      
      // Local system settings
      callistoPrefsFile = new File(callistoHome, PREFS_FILE);
      if (! callistoPrefsFile.canRead())
        callistoPrefsFile = null;
    }
    System.err.println("Callisto Home        = "+callistoHome);
    System.err.println("Callisto Tasks Dir   = "+callistoTasksDir);
    if (callistoPrefsFile != null)
      System.err.println("Callisto Preferences = "+callistoPrefsFile);
    
    // Now user directories containing system wide overrides
    if (! noUserPrefs) {
      
      // User settings directory
      // should vary based on OS and or cmdline? env: CALLISTO_USER_HOME ?
      userHome = new File(System.getProperty("user.home"), USER_DIR);
      // create directory structure if it doesn't exist
      if (! userHome.exists()) {
        System.err.println("Creating Personal Home directory for Callisto");
        try {
          userHome.mkdir();
        } catch (Exception e) {} // handle below
      }
      if (! userHome.isDirectory() || ! userHome.canRead()) {
        System.err.println("Personal directory inaccessable: " + userHome);
        userHome = null;
      }
      if (userHome != null) {
        
        // User task directory
        userTasksDir = new File(userHome, TASKS_DIR);
        if (! userTasksDir.exists()) {
          try {                    // create silently
            userTasksDir.mkdir();
          } catch (Exception e) {} // handle below
        }
        if (! userTasksDir.isDirectory() || ! userTasksDir.canRead()) {
          System.err.println("Personal tasks directory inaccessable: " +
          userTasksDir);
          userTasksDir = null;      // silently...
        }
        // User preferences file
        userPrefsFile = new File(userHome, PREFS_FILE);
        // don't set to null, if unreadable, since it will be created later.
      }
    }
    //System.err.println ("Personal Home        = "+userHome);
    //System.err.println ("Personal Tasks Dir   = "+userTasksDir);
    //System.err.println ("Personal Preferences = "+userPrefsFile);
    
    System.err.println(); // help visually parse stderr
  }
  
  /***********************************************************************/
  /* SPLASH SCREEN */
  /***********************************************************************/
  
  /**
   * Starts and stops the splash screen, called only as a result of starting
   * the app, and completing initialization.
   */
  private static void showSplashScreen(boolean show) {
    if (show && splash == null) {
      splash = new org.mitre.jawb.gui.SplashScreen(null);
    } else if (! (show || splash == null)) {
      splash.setVisible(false);
      splash.dispose();
      splash = null;
    }
  }
  
  /**
   * Set the splash screen progress value and text. Use -1 as a value to
   * modify only the text, and use null as a text to modify only the value (""
   * to clear the text value)
   * @param percent The new value
   * @param text The new text
   */
  public static void setSplashProgress(int percent, String text) {
    if (splash != null)
      splash.setProgress(percent, text);
  }
  
  /**
   * Wrapper which checks for the splash screens existence first.
   * @return percentage of progress shown on splash as an int between 0 and
   * 100
   */
  public static int getSplashProgress() {
    if (splash != null)
      return splash.getProgress();
    return -1;
  }
  
  /***********************************************************************/
  /* 'GLOBAL' PREFERENCES */
  /***********************************************************************/
  
  /**
   * Initialize the global preferences. Public so that it can be called from
   * any module for unit testing, but it will only execute once
   */
  public static Preferences initPreferences() {
    if (prefs != null) // only do this once
      return prefs;
    initLocations();  // returns if already done
    
    if (DEBUG > 0)
      System.err.println("Initializing Preferences");
    
    // load default preferences
    defaultProps = new Properties();
    try {
      if (DEBUG > 0) {
        System.err.println("Loading prefs file " + JAR_PREFS);
      }
      InputStream in = Jawb.class.getResourceAsStream(JAR_PREFS);
      defaultProps.load(in);
      in.close();
      
    } catch (Exception io) {
      System.err.println("Error loading default preferences");
      io.printStackTrace();
    }
    taskProps = new Properties(defaultProps);
    userProps = new Properties(taskProps);
    prefs = new Preferences(userProps);
    
    // if user wants prefs loaded, and it exists
    if (userPrefsFile != null && userPrefsFile.canRead()) {
      try {
        InputStream in = new FileInputStream(userPrefsFile);
        if (DEBUG > 0) {
          System.err.println("Loading prefs file " + userPrefsFile);
        }
        userProps.load(in);
        in.close();
        
      } catch (Exception io) {
        System.err.println("Error loading preferences");
        io.printStackTrace();
      }
    }
    
    // this used to be required, but now it's not really even neccissary
    if (prefs.getBoolean(Preferences.PROXY_SET_KEY)) {
      String host = prefs.getPreference(Preferences.PROXY_HOST_KEY);
      String port = prefs.getPreference(Preferences.PROXY_PORT_KEY);
      System.err.println("Setting http proxy: "+ host +":"+ port+"\n");
      System.getProperties().put(Preferences.PROXY_SET_KEY, "true");
      System.getProperties().put(Preferences.PROXY_HOST_KEY, host);
      System.getProperties().put(Preferences.PROXY_PORT_KEY, port);
    }
    
    return prefs;
  }
  
  /**
   * Return the users preferences which come from users "callisto.prefs", or a
   * file specified at run time. Changes are saved when program exits.
   */
  public static Preferences getPreferences() {
    return initPreferences();
  }
  

  public static void updateTaskPreference (String key, String newValue) {
    taskProps.setProperty(key, newValue);
  }
    

  /**
   * Store preferences to the users preference file.
   */
  public static void storePreferences() {
    if (userPrefsFile == null)
      return;
    
    try {
      if (prefs.isDirty()) {
        OutputStream out = new FileOutputStream(userPrefsFile);
        prefs.store(out, "Callisto v"+Version.version());
        out.close();
      }
      
    } catch (Exception io) {
      System.err.println("Error storing preferences");
      io.printStackTrace();
    }
  }
  
  /***********************************************************************/
  /* ACTIONS */
  /***********************************************************************/
  
  /**
   * Initializes global actions known to the Jawb environment.
   */
  private static void initActions() {
    actionMap = new HashMap();
    
    Action act;
    
    // sure would be nice to move this elsewhere
    // all this can eventually go into preferences
    act = new JawbAction("New...") {
      // 'Data' sounds better than 'Signal' for users
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "File", "New..." });
        JawbFrame jf = getJawbFrame(e);
        File[] files =
          GUIUtils.chooseFiles(jf, GUIUtils.OPEN_SIGNAL_DIALOG);
        if (files != null) {
          Task task = GUIUtils.getSelectedTask();
          if (task == null) {
            GUIUtils.showWarning("You must select a task when opening signals.");
            return;
          }
          
          String encoding = GUIUtils.getSelectedEncoding();
          String mimeType = GUIUtils.getSelectedMIMEType();
          
          TallalSystem sys = null; //GUIUtils.getSelectedSystem ();
          PreAnnotateLocalImporter imp = null;
          if (sys != null) {
            System.out.println("using system " + sys + " (lp " +
            sys.getLP() + ")");
            
            imp = new PreAnnotateLocalImporter();
            imp.setEncoding(encoding);
            imp.setMIMEType(mimeType);
            imp.setLP(sys.getLP());
          }
          
          for (int i=0; i<files.length; i++) {
            if (files[i] == null)
              ; // ignore
            else if (!files[i].exists() || !files[i].canRead())
              GUIUtils.showError(files[i]+"\ncan not be read.");
            else {
              try {
                if (imp != null)
                  jf.importDocument(files[i].toURI(), encoding, imp);
                else
                  jf.openSignal(files[i].toURI(), task, encoding, mimeType);
              } catch (Exception x) {
                GUIUtils.showError(files[i]+
                "\ncould not be opened for annotation:\n"+
                x.getMessage());
              }
            }
          }
        }
      }
    };
    addAction(act, "new", 'N', "ctrl N");
    
    act = new JawbAction("Open...") {
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "File", "Open..." });
        JawbFrame jf = getJawbFrame(e);
        File[] files =
          GUIUtils.chooseFiles(jf, GUIUtils.OPEN_ANNOTATION_DIALOG);
        if (files != null) {
          for (int i=0; i<files.length; i++) {
            if (files[i] == null)
              ; // ignore
            else if (!files[i].exists() || !files[i].canRead())
              GUIUtils.showError(files[i]+"\ncan not be read.");
            else {
              jawbLogger.info(JawbLogger.LOG_OPEN_AIF, new Object [] { files[i].toURI() });
              jf.openAIF(files[i].toURI());
            }
          }
        }
      }
    };
    addAction(act, "open", 'O', "ctrl O");
    
    act = new JawbAction("Close") {
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "File", "Close" });
        JawbFrame jf = getJawbFrame(e);
        if (DEBUG > 1)
          System.err.println("Close actionPerformed: " + e + " jf= " + jf);
        JawbDocument jd = jf.getJawbDocument();
        JawbCollection jc = jf.getJawbCollection();
        int index = jf.getJawbCollectionIndex();
        String p = jd == null ? null : jd.getPath();
        long timeSpent = 0;
        // stop timer BEFORE closing, so jf. still has it as current document
        // only do this in collections 
        if (jc != null) {
          jf.stopTimer();
          timeSpent = jf.getTimeSpent();
        }

        WorkspaceDashboard dash = jf.getMATWorkspaceDash();

        /************** NO, wait until after the file is saved, if it
                        is saved, because we don't want to clear out
                        anything before then, AND we don't need to
                        separately close the transaction if the user
                        saves, as the transaction will be closed as
                        part of the save operation
                      
        // If the current document is a Workspace Document:
        // Close the Transaction
        if (dash != null) {
          dash.closeTransaction();

        } else {
          if (DEBUG > 0)
            System.err.println("Jawb.java close with null client, not closing transaction");
        }
        ******************************/

        // CLOSE the file, and if a Workspace Document, close the transaction
        jf.close(jd, dash);

        // Log AFTER, because the effect of closing might log some
        // stuff (in fact, it does).
        /**** log in reallyClose now
        if (p != null) {
          jawbLogger.info(JawbLogger.LOG_CLOSE_FILE, new Object [] { p });
        }
        // if a workspace doc, upload the log
        if (dash != null)
          dash.uploadLog();
        *****/
        // Save the timeSpent in the Collection
        if (jc != null)
          jc.setTimeSpent(index, timeSpent);

        // TODO once we have browsing available
        //if (jc != null)
        //  jc.browse();

        jf.updateWSMenuOptions();
        // do the updateTables from within the dash whenever
        // operations are performed
        //if (dash != null) {
        //  dash.updateTables();
        //}
      }
    };
    addAction(act, "close", 'C', "ctrl W");

    act = new JawbAction("New Collection...") {
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "File", "New Collection..." });
        JawbFrame jf = getJawbFrame(e);
        JawbDocument jd = jf.getJawbDocument();
        File[] files =
          GUIUtils.chooseFiles(jf, GUIUtils.OPEN_SIGNAL_DIALOG);
        if (files != null) {
          Task task = GUIUtils.getSelectedTask();
          if (task == null) {
            GUIUtils.showWarning("You must select a task when opening signals.");
            return;
          }
          String encoding = GUIUtils.getSelectedEncoding();
          String mimeType = GUIUtils.getSelectedMIMEType();

          JawbCollection coll = new JawbCollection(files, task, jf,
                                                   mimeType, encoding);
          jf.setJawbCollection(coll);
          // TODO coll.browse() here rather than opening first file?

          // openNextFile will open the first readable file and will
          // associate the JawbCollection with it in the document manager
          openNextFile(coll, jf);

          /*****
          // open the first readable file for annotation
          for (int i=0; i<files.length; i++) {
            if (files[i] == null)
              ; // ignore
            else if (!files[i].exists() || !files[i].canRead())
              GUIUtils.showError(files[i]+"\ncan not be read.");
            else {
              try {
                jf.openSignal(files[i].toURI(), task, encoding, mimeType);
                jf.setJawbCollectionIndex(i);
                break;
              } catch (Exception x) {
                GUIUtils.showError(files[i]+
                "\ncould not be opened for annotation:\n"+
                x.getMessage());
              }
            }
          }
          ***/

        }
      }
    };
    addAction(act, "newColl", 'N', null);
    
    act = new JawbAction("Browse Collection...") {
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "File" , "Browse Collection..." });
        JawbFrame jf = getJawbFrame(e);
        JawbDocument jd = jf.getJawbDocument();
      }
    };
    addAction(act, "browseColl", 'B', null);
    
    act = new JawbAction("Continue Collection...") {
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "File" , "Continue Collection..." });
        JawbFrame jf = getJawbFrame(e);
        JawbDocument jd = jf.getJawbDocument();
        File file =
          GUIUtils.chooseFile(jf, GUIUtils.OPEN_COLLECTION_DIALOG);
        JawbCollection coll = new JawbCollection(file);
          
        // OPEN NEXT FILE -- openNextFile will also set the 
        // JawbCollection for the opened JawbDocument to coll
        openNextFile(coll, jf);
      }
    };
    addAction(act, "openColl", 'C', "ctrl L");
    
    act = new JawbAction("Save") {
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "File", "Save" });
        System.err.println("save actionPerformed");
        JawbFrame jf = getJawbFrame(e);
        WorkspaceDashboard dash = jf.getMATWorkspaceDash();
        System.err.println("dash = " + dash);
        if (dash != null) {
          // this is a workspace document:
          // Save current document without marking as done, and without 
          // closing the transaction
          // (save the basename first, for use in logging)
          String basename = dash.getMATDocBasename();
          dash.saveWorkspaceDocument(false, false);
          // log workspace save
          jawbLogger.logSave(JawbLogger.LOG_WORKSPACE_SAVE, basename);
          jf.updateWSMenuOptions();
          // dash.updateTables();
        } else {
          JawbDocument jd = jf.getJawbDocument();
          JawbCollection jc = jf.getJawbCollection();
          URI uri = jf.save(jd, false);
          // If this file is part of a collection, and had been
          // previously marked "done" unmark it, since this represents a
          // save of changes since then without re-marking it done
          if (jc != null) {
            int index = jf.getJawbCollectionIndex();
            jc.setDone(index, false);
          }
          // Log after, just in case something flushes.
          if (uri != null)
            jawbLogger.logSave(JawbLogger.LOG_SAVE, uri.getPath() );
        }
      }
    };
    addAction(act, "save", 'S', "ctrl S");
    
    act = new JawbAction("Save As...") {
        public void actionPerformed(ActionEvent e) {
          jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                          new Object [] { "File", "Save As..." });
          System.err.println("saveas actionPerformed");
          JawbFrame jf = getJawbFrame(e);
          JawbDocument jd = jf.getJawbDocument();
          URI uri = jf.save(jd, true);
          // Log after, just in case something flushes.
          if (uri != null)
            jawbLogger.logSave(JawbLogger.LOG_SAVE_AS, uri.getPath());
        }
      };
    addAction(act, "saveas", 'A', null);
    
    act = new JawbAction("Open Workspace...") {
        public void actionPerformed(ActionEvent e) {
          jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                          new Object [] { "File" , "Open Workspace..." });
          JawbFrame jf = getJawbFrame(e);
          // here's where we ask for things like the host, dir and key 
          // from the user
          /******** old way using a file picker
          File wsnameFromPicker =
            GUIUtils.chooseFile(jf, GUIUtils.OPEN_WORKSPACE_DIALOG);
          if (wsnameFromPicker == null)
            return;
          
          String workspaceURL = GUIUtils.getWorkspaceURL();
          String workspaceKey = GUIUtils.getWorkspaceKey();
          // boolean isWorkspaceActive = GUIUtils.getWorkspaceActive();
          boolean openNextdoc = GUIUtils.getOpenNextdoc();
          String workspaceFolder = GUIUtils.getWorkspaceFolder();

          boolean usePickerName = GUIUtils.useWorkspaceNameFromPicker();
          String workspaceName;
          if (usePickerName)
            workspaceName = wsnameFromPicker.getAbsolutePath();
          else
            workspaceName = GUIUtils.getWorkspaceNameFromTypeIn();
          System.err.println("OpenWorkspace wsname= " + workspaceName +
                             " from " +
                             (usePickerName?"picker":"type-in"));

          *****/ 

          /*** new way without a picker ****/
          OpenWorkspaceDialog openws = 
            new OpenWorkspaceDialog(jf, "Open Workspace", true, null, null);
          openws.show();

          // if user canceled, return
          if (!openws.isOK())
            return;

          String workspaceURL = openws.getWorkspaceURL();
          String workspaceKey = openws.getWorkspaceKey();
          String workspaceUser = openws.getWorkspaceUser();
          boolean openNextdoc = openws.getOpenNextDoc();
          String workspaceFolder = openws.getWorkspaceFolder();
          String workspaceName = openws.getWorkspaceNameFromTypeIn();

          // save the user selections
          prefs.setPreference(Preferences.LAST_WS_URL_KEY, workspaceURL);
          prefs.setPreference(Preferences.LAST_WS_KEY_KEY, workspaceKey);
          prefs.setPreference(Preferences.LAST_WS_USER_KEY, workspaceUser);
          prefs.setPreference(Preferences.LAST_WS_FOLDER_KEY, workspaceFolder);
          prefs.setPreference(Preferences.LAST_WS_NAME_KEY, workspaceName);
          prefs.setPreference(Preferences.LAST_WS_OPEN_NEXT_KEY, 
                              new Boolean(openNextdoc));

          //MATALCgiClient client = new MATALCgiClient(workspaceURL);
          //CallistoMATClient client = 
          //  new CallistoMATClientImpl(workspaceURL, workspaceName,
          //                            workspaceKey);

          CallistoMATClient client;
          try {
            Constructor constr = getCallistoMATClientConstructor();
            client = (CallistoMATClient)constr.newInstance(workspaceURL, 
                                                           workspaceName,
                                                           workspaceKey, 
                                                           workspaceUser);
            // catch any exceptions the newInstance raises - 
            // we should never get here with constr == null but if we
            // somehow do the catch below would also catch the NPE 
          } catch (Exception ex) {
            client = null;
            ex.printStackTrace();
          }
          
          if (client == null || !client.openSuccessful()) {
            return;
          }


          // if we get here the workspace was successfully opened, log it
          jawbLogger.info(JawbLogger.LOG_OPEN_WORKSPACE,
                          new Object[] { workspaceURL, workspaceName, 
                                         workspaceKey, 
                                         client.getWorkspaceDir(), 
                                         client.getTaskName() });
          
          
          // Find all available ToocaanTasks -- if there is more than
          // one ask the user to choose (or can we get Sam's file to
          // specify this too somehow?) then call the appropriate
          // ToocanTask's initializeTagTypes method (TODO)
          // for now just assume org.mitre.muc.mixed
          ToocaanTask theTask = (ToocaanTask)
            taskManager.getTaskByName("org.mitre.muc.mixed");
          theTask.setTagInfo(jf.getPreferenceDialog(), client);

          /************* No longer do this for MAT 2.0 because 
           *  we have to ask the user for their id before opening
          // get the list of users from the workspace
          String [] userStrs = client.listUsers();
          if (userStrs == null) {
            // TODO-MAT is this still useful? client = null;
            return;
          }

          String lastuid = prefs.getPreference(Preferences.LAST_WS_USER_KEY,
                                               userStrs[0]);
          String userid = "";
          userid = (String)
            JOptionPane.showInputDialog(jf,
                                        "Please select your userid:",
                                        "User Identification",
                                        JOptionPane.PLAIN_MESSAGE,
                                        null, userStrs, lastuid);
          *****************************/

          jawbLogger.info(JawbLogger.LOG_WORKSPACE_USERID,
                          new Object[] { workspaceUser });
          if (workspaceUser == null) {
            GUIUtils.showError("Open Workspace Cancelled");
            // TODO-MAT is this still useful? client = null;
            return;
          }
          // for 2.0 this happens in the workspace dialog:
          // prefs.setPreference(Preferences.LAST_WS_USER_KEY, userid);
            

          // get the Active Learning status from the workspace
          boolean isWorkspaceActive = 
            client.isActiveLearningEnabled();

          // Display the workspace Dashboard
          System.err.println("creating workspace dashboard");
          WorkspaceDashboard dash =
            new WorkspaceDashboard(workspaceURL, client.getWorkspaceDir(), 
                                   workspaceKey, isWorkspaceActive, jf, 
                                   client, workspaceUser, workspaceFolder, 
                                   null);
          jf.setMATWorkspaceDash(dash);
          jf.updateWSMenuOptions();

          System.err.println("creating workspace dashboard frame");
          JFrame dashFrame = dash.createFrame(jf, "Workspace Dashboard");
          dashFrame.setVisible(true);
          
          System.err.println("done creating workspace dashboard frame");

          jf.updateTKTagInfo(theTask);

          // now get the next document from that workspace, if desired
          if (openNextdoc) {
            // this will log itself before calling the finish method
            jf.importNextWorkspaceDoc(dash, workspaceFolder);


          }
          

        }
      };
    addAction(act, "openWS", 'W', null);
  
    act = new JawbAction("Done & Next Workspace Document") {
        // Save current document, mark it as done, and get next
        // With (segment-based) active Learning we never mark a *document*
        // as done, just a segment.  So this action is only for non-active
        // workspaces. 
        public void actionPerformed(ActionEvent e) {
          jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                          new Object [] { "File" , 
                                          "Done & Next Workspace Document" });
          JawbFrame jf = getJawbFrame(e);
          WorkspaceDashboard dash = jf.getMATWorkspaceDash();
          String folder = dash.getMATFolder();
          JawbDocument jd = jf.getJawbDocument();

          doDoneAndNext(jf, dash, folder, jd);


          /************** move to doDoneAndNext *************************
          // get basename and lockid for logging
          String basename = dash.getMATDocBasename();
          String lockId = jf.getMATLockId();
          // Save current document, mark as done and close transaction
          dash.saveWorkspaceDocument(true, true);
          //  log workspace save
          jawbLogger.logSave(JawbLogger.LOG_WORKSPACE_SAVE, basename);
          jawbLogger.info(JawbLogger.LOG_RELEASE_LOCK, new Object[] {lockId});
          jawbLogger.info(JawbLogger.LOG_MARK_GOLD, new Object[] {basename});

          // CLOSE the file
          String p = jd == null ? null : jd.getPath();
          jf.close(jd, dash);
          // TODO log something different for WS Close?
          if (p != null) {
            jawbLogger.info(JawbLogger.LOG_CLOSE_FILE, new Object [] { p });
          }

          // now get the next document from that workspace
          jf.importNextWorkspaceDoc(dash, folder);
          // get new basename for logging
          basename = dash.getMATDocBasename();
          jawbLogger.info(JawbLogger.LOG_WORKSPACE_IMPORT, 
                          new Object [] { "core", basename });
          ******************************************************************/
        }
      };
    addAction(act, "doneWSdoc", 'D', "ctrl D");
  
    /*******
    act = new JawbAction("Save Workspace Document") {
        public void actionPerformed(ActionEvent e) {
          JawbFrame jf = getJawbFrame(e);
          WorkspaceDashboard dash = jf.getMATWorkspaceDash();
          JawbDocument jd = jf.getJawbDocument();

          // Save current document without marking as done, and without 
          // closing the transaction
          dash.saveWorkspaceDocument(false, false);
        }
      };
    addAction(act, "saveWSdoc", 'S', null);
    *******/
    
  
    act = new JawbAction("Save & Next Workspace Document") {
        public void actionPerformed(ActionEvent e) {
          jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                          new Object [] { "File" , 
                                          "Save & Next Workspace Document" });
          JawbFrame jf = getJawbFrame(e);
          JawbDocument jd = jf.getJawbDocument();
          WorkspaceDashboard dash = jf.getMATWorkspaceDash();
          String folder = dash.getMATFolder();

          doSaveAndNext(jf, dash, folder, jd);
        }
      };
    addAction(act, "nextWSdoc", 'X', null);
    

    act = new JawbAction("Review Remaining Segments") {
        public void actionPerformed(ActionEvent e) {
          jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                          new Object [] { "Toocaan" , 
                                          "Review Remaining Segments" });
          JawbFrame jf = getJawbFrame(e);
          JawbDocument jd = jf.getJawbDocument();
          ToocaanTask task = (ToocaanTask)jd.getTask();      
          //WorkspaceDashboard dash = jf.getMATWorkspaceDash();
          //String folder = dash.getMATFolder();

          // open an adjudication document with no segments specified
          AdjudicationDocument adjDoc = 
            new AdjudicationDocument(jd, task, jf);


        }
      };
    addAction(act, "reviewSegments", 'R', "ctrl R");
    
  
    act = new JawbAction("Done...") {
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "File", "Done" });
        JawbFrame jf = getJawbFrame(e);
        JawbDocument jd = jf.getJawbDocument();
        long timeSpent = 0;
        // SAVE THE DOCUMENT to a default .aif.xml filename
        URI uri = jf.save(jd, false, false, 
                          URI.create(jd.getSignalURI().toString()+".aif.xml"));
        if (uri != null)
          jawbLogger.logSave(JawbLogger.LOG_SAVE, uri.getPath() );
        // save the aif name saved to in the collections file
        int idx = jf.getJawbCollectionIndex();
        JawbCollection jc = jf.getJawbCollection();
        // jc should not be null if "Done...' is available, but still safer
        // to check...
        if (jc != null) {
          jc.setAIF(idx, uri);
          // stop timer BEFORE closing, so jf. still has it as current document
          jf.stopTimer();
          timeSpent = jf.getTimeSpent();
        }
        // CLOSE THE FILE
        String p = jd == null ? null : jd.getPath();
        jf.close(jd);
        // log AFTER, because the effect of closing might log some
        // stuff (in fact, it does).
        /**** log in reallyClose now
        if (p != null) {
          jawbLogger.info(JawbLogger.LOG_CLOSE_FILE, new Object [] { p });
        }
        // if a workspace doc, upload the log
        WorkspaceDashboard dash = jf.getMATWorkspaceDash();
        if (dash != null)
        dash.uploadLog();
        ****/
        // Save the timeSpent in the Collection
        if (jc != null) { // should never be null here
          jc.setTimeSpent(idx, timeSpent);

          // COLLECT METADATA 
          boolean metadataOK = 
            writeMetadata(jd.getSignalURI().toString(), jc, idx, timeSpent);

          // MARK IT DONE
          if (metadataOK)
            jc.setDone(idx, true);

          // BROWSE COLLECTION
          // jf.getJawbCollection().browse();
        }


      }
    };
    addAction(act, "done", 'D', null);
    
    act = new JawbAction("Done & Next") {
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "File", "Done & Next" });
        JawbFrame jf = getJawbFrame(e);
        JawbDocument jd = jf.getJawbDocument();
        JawbCollection jc = jf.getJawbCollection();
        long timeSpent = 0;
        // SAVE THE DOCUMENT to a default .aif.xml filename
        URI uri = jf.save(jd, false, false, 
                          URI.create(jd.getSignalURI().toString()+".aif.xml"));
        if (uri != null)
          jawbLogger.logSave(JawbLogger.LOG_SAVE, uri.getPath() );
        int idx = jf.getJawbCollectionIndex();
        // save the aif name saved to in the collections file
        if (jc != null) { // should never be null here
          jc.setAIF(idx, uri);
          // stop timer BEFORE closing, so jf. still has it as current document
          jf.stopTimer();
          timeSpent = jf.getTimeSpent();
        }
        // CLOSE THE FILE
        String p = jd == null ? null : jd.getPath();
        jf.close(jd);
        // log AFTER, because the effect of closing might log some
        // stuff (in fact, it does).
        /**** log in reallyClose now
        if (p != null) {
          jawbLogger.info(JawbLogger.LOG_CLOSE_FILE, new Object [] { p });
        }

        // if a workspace doc, upload the log
        WorkspaceDashboard dash = jf.getMATWorkspaceDash();
        if (dash != null)
          dash.uploadLog();
        *****/
        if (jc != null) { // should never be null here
          // Save the timeSpent in the Collection
          jc.setTimeSpent(idx, timeSpent);
          
          // COLLECT METADATA
          boolean metadataOK = 
            writeMetadata(jd.getSignalURI().toString(), jc, idx, timeSpent);
          
          // MARK IT DONE
          if (metadataOK)
            jc.setDone(idx, true);

          // OPEN NEXT FILE
          openNextFile(jc, jf);
        }
      }
    };
    addAction(act, "next", 'X', "ctrl D");
    
    act = new JawbAction("Import...") {
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "File", "Import..." });
        JawbFrame jf = getJawbFrame(e);
        File[] files =
        GUIUtils.chooseFiles(jf, GUIUtils.IMPORT_DIALOG);
        if (files != null) {
          for (int i=0; i<files.length; i++) {
            if (files[i] == null)
              ; // ignore
            else if (!files[i].exists() || !files[i].canRead())
              GUIUtils.showError(files[i]+"\ncan not be read.");
            else {
              jawbLogger.info(JawbLogger.LOG_IMPORT, new Object [] { files[i].toURI() });
              jf.importDocument(files[i].toURI(),
              GUIUtils.getSelectedEncoding(),
              GUIUtils.getSelectedImporter());
            }
          }
        }
      }
    };
    addAction(act, "import", 'I', null);
    /*//TODO:
    act = new JawbAction ("TALLAL Systems") {
      public void actionPerformed(ActionEvent e) {
    JawbFrame jf = getJawbFrame (e);
    // TallalSystemCreator tpc = new TallalSystemCreator(jf);
    TallalSystemBrowser tsb = new TallalSystemBrowser(jf);
    tsb.setVisible(true);
      }
  };
    addAction (act, "tallal", 'T', null);
     */
    act = new JawbAction("Export...") {
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "File", "Export..." });
        JawbFrame jf = getJawbFrame(e);
        JawbDocument jd = jf.getJawbDocument();
        URI uri = jf.save(jd, true, true);
        // Log after, just in case something flushes.
        if (uri != null)
          jawbLogger.logSave(JawbLogger.LOG_EXPORT, uri.getPath());
      }
    };
    addAction(act, "export", 'E', null);

    /**** TODO -- create the actions at a later time
    act = new JawbAction("Import Collection...") {
      public void actionPerformed(ActionEvent e) {
        JawbFrame jf = getJawbFrame(e);
        JawbDocument jd = jf.getJawbDocument();
      }
    };
    addAction(act, "importColl", 'I', null);
    
    act = new JawbAction("Export Collection...") {
      public void actionPerformed(ActionEvent e) {
        JawbFrame jf = getJawbFrame(e);
        JawbDocument jd = jf.getJawbDocument();
      }
    };
    addAction(act, "exportColl", 'E', null);
    ****/
    
    /*//TODO:
    act = new JawbAction ("Submit for training...") {
  public void actionPerformed(ActionEvent e) {
    JawbFrame jf = getJawbFrame (e);
    JawbDocument doc = jf.getCurrentDocument();
     
    PreAnnotateLocalExporter exp = new PreAnnotateLocalExporter();
    PreAnnotateDialog dial = new PreAnnotateDialog();
     
    for(Enumeration systems = TallalController.getController().getSystems().elements(); systems.hasMoreElements(); ) {
        TallalSystem sys = (TallalSystem)systems.nextElement();
        String desc = sys.getName() + " (" + sys.getDesc() + ")";
        dial.addTallalSystem(desc);
    }
     
    String sysName = dial.show(jf);
    if(sysName == null) { // user pressed cancel
      return;
    }
    else {
      TallalSystem sys = null;
      for(Enumeration systems = TallalController.getController().getSystems().elements(); systems.hasMoreElements(); ) {
    TallalSystem thisSys = (TallalSystem)systems.nextElement();
    String desc = thisSys.getName() + " (" + thisSys.getDesc() + ")";
    if(desc.equals(sysName)) sys = thisSys;
      }
     
      exp.setLP(sys.getLP());
     
      boolean success = false;
      try {
        success = doc.export(exp, doc.getSignalURI());
        if(!success) {
    GUIUtils.showError("Training failed and I don't know why.");
        }
      }
/ *
      catch(java.net.URISyntaxException e2) {
        GUIUtils.showError("Error constructing export URI from original URI " + doc.getExternalURI().toString() + ": " + e2.getMessage());
      }
     * /
      catch(java.io.IOException e2) {
        GUIUtils.showError("Error exporting document for training: " + e2.getMessage());
      }
    }
  }
      };
    addAction (act, "train", 'T', null);
     */
    act = new JawbAction("New Window") {
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "File", "New Window" });
        JawbFrame jf = getJawbFrame(e);
        // Frames keep track of themselves (see JawbFrame.frameList)
        JawbFrame frame = new JawbFrame();
        frame.setVisible(true);
        frame.setCurrentDocument(jf.getCurrentDocument());
      }
    };
    addAction(act, "newFrame", 'N', null);
    
    act = new JawbAction("Close Window") {
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "File", "Close Window" });
        getJawbFrame(e).closeFrame();
      }
    };
    addAction(act, "closeFrame", 'W', null);
    
    act = new JawbAction("Preferences") {
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "Edit", "Preferences" });
        getJawbFrame(e).showPreferences();
      }
    };
    addAction(act, "preferences", 'E', null);
    
    // TODO this does not save time spent for collection files yet
    act = new JawbAction("Exit") {
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "File", "Exit" });
        getJawbFrame(e).exit();
      }
    };
    addAction(act, "exit", 'X', null);
    
    act = new JawbAction("About Callisto") {
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "Help", "About Callisto" });
        AboutDialog.show(getJawbFrame(e));
      }
    };
    addAction(act, "about", 'A', null);
    
    act = new JawbAction("About Tasks") {
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "Help", "About Tasks" });
        AboutTasksDialog.show(getJawbFrame(e));
      }
    };
    addAction(act, "aboutTasks", 'T', null);
    
    act = new JawbAction("Help Contents") {
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "Help", "Help Contents" });
        try {
          String url = getCallistoHome().toURL().toString();
          if (! url.endsWith("/"))
            url += "/";
          url += "docs/manual/index.html";
          BrowserLauncher.openURL(url);
        } catch (IOException x) {
          GUIUtils.showError("Error opening browser: " + x.getMessage());
        }
      }
    };
    addAction(act, "help", 'H', null);
    
    act = new JawbAction("Get Console Log") {
      private JTextArea textArea;
      private JDialog dumpDialog;
      final String COPY_ALL = "Copy All";
      final String SAVE_LOG = "Save Log...";
      final String CLOSE = "Close";
      JawbFrame jawbFrame;
      byte[] bytes;
      String dump;
    
      public void actionPerformed(ActionEvent e) {
        jawbLogger.info(JawbLogger.LOG_MENU_SELECTION, 
                        new Object [] { "Help", "Get Content Log" });
        try {
          String cmd = e.getActionCommand();
          if (cmd.equals("getConsoleLog")) {
            jawbFrame = getJawbFrame(e);
            bytes = DEBUG_OUTPUT_STREAM.toByteArray();
            dump = new String(bytes, "UTF-8");
            textArea = new JTextArea(dump);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            dumpDialog = new JDialog(jawbFrame, "Console Log", true);
            JComponent cp = (JComponent) dumpDialog.getContentPane();
            cp.setLayout(new BorderLayout());
            cp.add(new JScrollPane(textArea), BorderLayout.CENTER);
            JPanel buttonsPanel = new JPanel(new GridLayout(1, 0, 4, 4));
            final Action action = this;
            class ButtonAction extends AbstractAction {
              ButtonAction(String name) {
                super(name);
              }
              public void actionPerformed(ActionEvent e) {
                action.actionPerformed(e);
              }
            };
            buttonsPanel.add(new JButton(new ButtonAction(COPY_ALL)));
            buttonsPanel.add(new JButton(new ButtonAction(SAVE_LOG)));
            ButtonAction closeAction = new ButtonAction(CLOSE);
            buttonsPanel.add(new JButton(closeAction));
            JPanel bp = new JPanel(new FlowLayout());
            bp.add(buttonsPanel);
            cp.add(bp, BorderLayout.NORTH);
            
            InputMap inputMap = cp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CLOSE);
            inputMap = cp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CLOSE);
            ActionMap actionMap = cp.getActionMap();
            actionMap.put(CLOSE, closeAction);
            
            dumpDialog.pack();
            dumpDialog.setBounds(jawbFrame.getBounds());
            dumpDialog.show();
          }
          else if (cmd == CLOSE) {
            dumpDialog.dispose();
          }
          else if (cmd == COPY_ALL) {
            textArea.selectAll();
            textArea.copy();
          }
          else if (cmd == SAVE_LOG) {
            JFileChooser chooser = new JFileChooser(".");
            chooser.setFileFilter(new FileFilter() {

              public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".log");
              }

              public String getDescription() {
                return "(*.log) Debug Log Files";
              }

            });

            int choice = chooser.showSaveDialog(dumpDialog);
            if (choice == JFileChooser.APPROVE_OPTION) {
              File selectedFile = chooser.getSelectedFile();
              if (! selectedFile.getName().toLowerCase().endsWith(".log")) {
                selectedFile = new File(selectedFile.getPath()+".log");
              }
              FileChannel fc = new FileOutputStream(selectedFile, false).getChannel();
              fc.write(ByteBuffer.wrap(bytes));
              fc.close();
            }
          }
        } catch (Exception e1) {
          e1.printStackTrace();
          JOptionPane.showMessageDialog(jawbFrame, "Error saving or showing debug log file: "+e1, "Error in Debug Log Display", JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    addAction(act, "getConsoleLog", 'L', null);
    
    
    
    // EDNA
    act = new JawbAction("Import from Server") {
      public void actionPerformed(ActionEvent e) {
        ednaRaceConditionFilledHack = getJawbFrame(e);
        try {
          // Something weird is happening with timing and class loaders. Grr.
          Task ednaTask = Jawb.getTaskManager().getTaskByName("org.mitre.edna");
          if (ednaTask == null)
            return;
          Class servImp = ednaTask.getClass().getClassLoader().loadClass("org.mitre.edna.ui.ServerImporterDialog");
          JDialog dialog = (JDialog)servImp.newInstance();
          dialog.show();
        } catch(ClassNotFoundException cnfe) {
          cnfe.printStackTrace();
          return;
        } catch(InstantiationException ie) { 
          ie.printStackTrace();
          return;
        } catch(IllegalAccessException iae) {
          iae.printStackTrace();
          return;
        } catch(RuntimeException cnfe) {
          cnfe.printStackTrace();
          return;
        }
      }
    };
    addAction(act, "ednaImport", 'V', null);
    
    Task task = Jawb.getTaskManager().getTaskByName("org.mitre.aceET");
    if (task != null) {
      act = new JawbAction("Import for ACE ET") {
        public void actionPerformed(ActionEvent e) {
          ednaRaceConditionFilledHack = getJawbFrame(e);
          try {
            // Something weird is happening with timing and class loaders. Grr.
            Task task = Jawb.getTaskManager().getTaskByName("org.mitre.aceET");
            if (task == null)
              return;
            Class dialogClass = task.getClass().getClassLoader().loadClass("org.mitre.aceET.callisto.ACEETImportDialog");
            JDialog dialog = (JDialog)dialogClass.newInstance();
            dialog.show();
          } catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
          } catch(InstantiationException ie) { 
            ie.printStackTrace();
            return;
          } catch(IllegalAccessException iae) {
            iae.printStackTrace();
            return;
          } catch(RuntimeException cnfe) {
            cnfe.printStackTrace();
            return;
          }
        }
      };
      addAction(act, "aceETImport", 'T', null);
    }
    
    /*** explicit Release Lock action is no longer required
     *   whenever a releasable file is closed, the user will be prompted to
     *   also release it.

    act = new JawbAction("Release Lock") {
      public void actionPerformed(ActionEvent e) {
        JawbFrame jf = getJawbFrame(e);
        if(jf.getCurrentDocument() == null)
          return;
        ednaRaceConditionFilledHack = jf;
        try {
          // Something weird is happening with timing and class loaders. Grr.
          Task ednaTask = Jawb.getTaskManager().getTaskByName("org.mitre.edna");
          if (ednaTask == null)
            return;
          Class releaseFileDlg = ednaTask.getClass().getClassLoader().loadClass("org.mitre.edna.ui.ReleaseFileDialog");
          releaseFileDlg.newInstance();
        } catch(ClassNotFoundException cnfe) {
          cnfe.printStackTrace();
          return;
        } catch(InstantiationException ie) { 
          ie.printStackTrace();
          return;
        } catch(IllegalAccessException iae) {
          iae.printStackTrace();
          return;
        } catch(RuntimeException cnfe) {
          cnfe.printStackTrace();
          return;
        }
      }
    };
    addAction(act, "ednaRelease", -1, null);  

    ***************/
  }

  // make this public so that the task toolkit can call it as well as the
  // user doing it via a menu action.  This allows streamlining the annotation
  // process
  public static void doSaveAndNext(JawbFrame jf, WorkspaceDashboard dash, 
                                   String folder, JawbDocument jd) {
    // Save current document and close the transaction without
    // marking as done (Save basename and lockId for logging)
    String basename = dash.getMATDocBasename();
    String lockId = jf.getMATLockId();
    if (DEBUG > 0)
      System.err.println("Jawb.action:Save&Next calling saveWSDoc");
    dash.saveWorkspaceDocument(true, false);
    if (DEBUG > 1)
      System.err.println("\tafter WSDoc Saved, dirty=" + jd.isDirty());
    //  log workspace save
    jawbLogger.logSave(JawbLogger.LOG_WORKSPACE_SAVE, basename);
    jawbLogger.info(JawbLogger.LOG_RELEASE_LOCK, new Object[] {lockId});

    // jf.updateWSMenuOptions(); // not needed, jf.close will do this

    // CLOSE the file
    String p = jd == null ? null : jd.getPath();
    jf.close(jd, dash);

    // log AFTER, because the effect of closing might log some
    // stuff (in fact, it does).
    /**** log in reallyClose now */

    // repaint now
    final JawbFrame thejf = jf;
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          thejf.repaint();
        }
      });

    final WorkspaceDashboard theDash = dash;
    final String theFolder = folder;
    // now get the next document from that workspace
    // this will log itself before calling the finish method
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          thejf.importNextWorkspaceDoc (theDash, theFolder);
        }
      });
  }

  public static void doDoneAndNext(JawbFrame jf, WorkspaceDashboard dash, 
                                   String folder, JawbDocument jd) {
    // get basename and lockid for logging
    String basename = dash.getMATDocBasename();
    String lockId = jf.getMATLockId();
    // Save current document, mark as done and close transaction
    dash.saveWorkspaceDocument(true, true);
    //  log workspace save
    jawbLogger.logSave(JawbLogger.LOG_WORKSPACE_SAVE, basename);
    jawbLogger.info(JawbLogger.LOG_RELEASE_LOCK, new Object[] {lockId});
    jawbLogger.info(JawbLogger.LOG_MARK_GOLD, new Object[] {basename});

    // CLOSE the file
    String p = jd == null ? null : jd.getPath();
    jf.close(jd, dash);
    // TODO log something different for WS Close?
    /**** log in reallyClose now
    if (p != null) {
      jawbLogger.info(JawbLogger.LOG_CLOSE_FILE, new Object [] { p });
    }

    // upload the log
    if (dash != null)
      dash.uploadLog();
    ******/
    // now get the next document from that workspace
    // this will log itself before calling the finish method
    jf.importNextWorkspaceDoc(dash, folder);

    /****
    // get new basename for logging
    basename = dash.getMATDocBasename();
    jawbLogger.info(JawbLogger.LOG_WORKSPACE_IMPORT, 
                    new Object [] { "core", basename });
    ****/
  }
  
  /**
   * Adds the keystroke and mnemonic to the action, and make action
   * retrievable via the actions name.
   * @see javax.swing.Action
   * @see javax.swing.KeyStroke
   */
  private static void addAction(Action action, String command,
  int mnemonic, String keyStroke) {
    action.putValue(Action.ACTION_COMMAND_KEY, command);
    action.putValue(Action.MNEMONIC_KEY, new Integer(mnemonic));
    action.putValue(Action.ACCELERATOR_KEY,
    KeyStroke.getKeyStroke(keyStroke));
    Jawb.addAction(action);
  }
  
  /**
   * Add an Action to the the list of available tasks. The action must have a
   * valid name, unique command key. Other keys defined in java.swing.Action
   * are optional but will be used. accessable through
   * <code>Action.getValue(Action.ACTION_COMMAND_KEY)</code>.
   */
  public static void addAction(Action action) {
    String name = (String) action.getValue(Action.NAME);
    String command = (String) action.getValue(Action.ACTION_COMMAND_KEY);
    if (name != null || command == null) {
      Object o = actionMap.put(command, action);
      if (o != null)
        System.err.println("Replacing action \""+command+"\"");
    } else {
      System.err.println("Action defines no 'name' or 'command' value: "+
      action);
      Thread.dumpStack();
    }
  }
  
  /**
   * Add a JawbAction to the the list of available tasks.
   */
  public static JawbAction getAction(String action) {
    if (actionMap == null)
      initActions();
    return (JawbAction)actionMap.get(action);
  }

  /**
   * helper function to open the next available file that is not
   * done or broken.  If the file has been edited before, open the
   * AIF, otherwise open the signal for editing in the appropriate
   * task. 
   */
  private static void openNextFile(JawbCollection jc, JawbFrame jf) {
    int idx = jc.getNextIndex();
    File file = jc.getAIFFile(idx);
    boolean isAIF = true;
    if (file == null) {
      file = jc.getSignalFile(idx);
      isAIF = false;
    }

    while (idx >= 0 && 
           (file == null || !file.exists() || !file.canRead())) {
      // mark file as broken and move to next
      System.err.println("broken file idx=" + idx + 
                         "\n\tnull? " +
                         (file == null ? "yes" : "no"));
      if (file != null) 
        System.err.println("\tPath: " + file.getPath() +
                           "\n\texists? " + (file.exists() ? "yes" : "no") +
                           "\n\tcanRead? " + (file.canRead()? "yes" : "no"));
      jc.setFileBroken(idx, true);
      writeBrokenMetadata(jc.getSignalFile(idx).toURI().toString(), isAIF);
      idx = jc.getNextIndex();
      file = jc.getAIFFile(idx);
      isAIF = true;
      if (file == null) {
        file = jc.getSignalFile(idx);
        isAIF = false;
      }
    }

    if (idx < 0) {
      // all files are done or broken
      // TODO Browse the collection
      GUIUtils.showMessage("Done annotating all files in this collection");
      // TODO set the collection as no longer active??
      return;
    }

    try {
      if (isAIF) {
        jf.openAIF(file.toURI());
      } else {
        Task task = jc.getTask();
        // TODO handle null task?
        String encoding = jc.getEncoding();
        String mimeType = jc.getMIMEType();
        jf.openSignal(file.toURI(), task, encoding, mimeType);
      }
      // if we get here, we managed to open the file, so set
      // the collection and the collection index to this file
      System.err.println("setting collection to " + jc);
      jf.setJawbCollection(jc);
      System.err.println("setting index to " + idx);
      jf.setJawbCollectionIndex(idx);
      // now, if this document has been worked on before, set the 
      // time spent in the doc manager (via the jf)
      System.err.println("getting time spent from collection");
      long timeSpent = jc.getTimeSpent(idx);
      System.err.println("saving time spent to frame");
      jf.setTimeSpent(timeSpent);
      // and then start the timer
      System.err.println("starting timer via frame");
      jf.startTimer();
    } catch (Exception x) {
      GUIUtils.showError(file+
                         "\ncould not be opened for annotation:\n"+
                         x.getMessage());
      Thread.dumpStack();
      jc.setFileBroken(idx, true);
      // TODO loop and try next file?  hope we already caught all cases
      // earlier
    }
  }

  /**
    * Called when a broken signal or AIF file is encountered, to write
    * out a line indicating this as the metadata for this file.  
    */
  private static void writeBrokenMetadata (String signalStr, boolean isAIF) {
    System.err.println("Jawb.writeBrokenMetadata " + signalStr + " " + 
                       isAIF);
    URI metaUri = null;
    Properties broken = new Properties();
    broken.setProperty("callisto.collection." + (isAIF?"aif":"signal") + 
                       ".broken", "true");
    try {
      metaUri = URI.create(signalStr+".meta.txt");
      OutputStream metaStream = new FileOutputStream(new File(metaUri));
      broken.store(metaStream, "Answer Form - Broken File");
    } catch (IOException x) {
      // TODO do something intelligent here
        System.err.println("Unable to store metadata to " +
                           (metaUri==null?"null":metaUri.toString()));
    }
  }

  /**
    * Bring up the metadata collection dialog, and write the answers
    * out to a file whose name is derived by adding ".meta.txt" to the
    * signalStr
    */
  private static boolean writeMetadata(String signalStr, JawbCollection jc,
                                       int index, long timeSpent) {
    Properties metadataProps = jc.getMetadataProperties();
    if (metadataProps == null) {
      // no metadata collection needed
      return true;
    }

    long priorTimeSpent = jc.getMetadataTimeSpent(index);
    long startMillis = System.currentTimeMillis();
    long endMillis = 0;
    CollectionMetaDataDialog dialog = 
      new CollectionMetaDataDialog("Enter Metadata", metadataProps, signalStr);
    dialog.setModal(true);
    dialog.setVisible(true);
    if (dialog.isCanceled()) {
      endMillis = System.currentTimeMillis();
      jc.setMetadataTimeSpent(index, priorTimeSpent+(endMillis-startMillis));
      System.out.println("Jawb: Metadata Collection Cancelled");
      return false;
    } else {
      Properties answer = dialog.getAnswerForm();
      endMillis = System.currentTimeMillis();
      long auditTimeSpent = priorTimeSpent+(endMillis-startMillis);
      jc.setMetadataTimeSpent(index, auditTimeSpent);
      // add time spent to the properties retrieved from the form
      answer.setProperty("callisto.metadata.annot.timespent", 
                         String.valueOf(timeSpent));
      answer.setProperty("callisto.metadata.audit.timespent", 
                         String.valueOf(auditTimeSpent));
      URI metaUri = null;
      try {
        metaUri = URI.create(signalStr+".meta.txt");
        OutputStream metaStream = new FileOutputStream(new File(metaUri));
        answer.store(metaStream, "Answer Form");
      } catch (IOException x) {
        // TODO do something intelligent here
        System.err.println("Unable to store metadata to " +
                           (metaUri==null?"null":metaUri.toString()));
      }
      // must return true to allow user to move on to next file
      return true;
    }
  }
  
  /***********************************************************************/
  /* TASK INITIALIZATION */
  /***********************************************************************/
  
  /**
   * This method initializes class objects for tasks that can be used by
   * Jawb. Only the first task loaded with a specified name is kept in memory.
   * User specified tasks are loaded first to override system settings.
   * Tasks specified via class name are loaded before tasks loaded from jar
   * files in specified directories.<p>
   *
   * First, tasks specified in the properties file (if any specified) are
   * loaded first.  Properties of "user.task.class.[n]", where '[n]' is an
   * integer, and the value is a fully qualified class name, are loaded
   * first. The class and any of it's required classes are expected to be on
   * the classpath.  These properties will be loaded in order, starting at n=0
   * and continuing until the "user.task.class.n" returns <code>null</code>.<p>
   *
   * Second, the same process is used for "user.task.dir.[n]", where all "*.jar"
   * files and direct subdirs (non-recursive) of those directories are checked
   * for Callisto tasks.<p>
   *
   * Third, tasks from *.jar files and direct subdirs of the users personal
   * 'tasks' directory (usually in "${HOME}/.callisto/tasks") are loaded.<p>
   *
   * The three steps are then repeated using the local system properties
   * for "task.class.[n]" properties, "task.dir.[n]" properties, and finally,
   * the local system 'tasks' directory (usually the "tasks" directory in the
   * same directory as the Callisto.jar file).<p>
   *
   * This method will only execute once.
   *
   */
  public static void initTasks() {
    if (taskManager != null) // only done once
      return;
    
    // make sure locations, userProps & defaultProps initiated
    initPreferences();
    
    if (DEBUG > 0)
      System.err.println("Initializing Tasks");
    
    TaskManager tm = new TaskManager(taskProps);
    
    int progress = 0;
    if (doInitGUI)
      progress = getSplashProgress();
    
    // user specified classes, then directories, then default dir override
    //setSplashProgress (progress+=2, null);
    //tm.loadTaskClasses ("user.task.class", userProps, null);
    
    if (doInitGUI)
      setSplashProgress(progress+=2, null);
    tm.scanTaskDirs("user.task.dir", userProps);
    
    if (doInitGUI)
      setSplashProgress(progress+=2, null);
    tm.scanTaskDir(userTasksDir);
    
    // now local system specified classes, directories, and default dir
    if (doInitGUI)
      setSplashProgress(progress+=2, null);
    tm.loadTaskClasses("task.class", defaultProps);
    
    if (doInitGUI)
      setSplashProgress(progress+=2, null);
    tm.scanTaskDirs("task.dir", defaultProps);
    
    if (doInitGUI)
      setSplashProgress(progress+=2, null);
    tm.scanTaskDir(callistoTasksDir);
    
    if (tm.getTasks().isEmpty()) {
      String err = "No Tasks were found. Task jar files should be found in:\n"+
      "  " + callistoTasksDir;
      if (doInitGUI) {
        showSplashScreen(false);
        GUIUtils.showError(err);
      }
      System.err.println(err);
      System.exit(-1);
    }
    
    taskManager = tm;
    System.err.println(); // help visually parse stderr
  }
  
  /**
   * Return an unmodifiable List of Task Objects currently
   * available. Convienience method for <code>{@link #getTaskManager}.getTasks()</code>
   * @see Task
   * @see TaskManager#getTasks()
   * @return Tasks objects available for annotating.
   */
  public static List getTasks() {
    return getTaskManager().getTasks();
  }
  
  /** Retrieve the task manager for this instance of Callisto */
  public static TaskManager getTaskManager() {
    initTasks();
    return taskManager;
  }
  
  /***********************************************************************/
  /* RESOURCES */
  /***********************************************************************/
  
  /**
   * Get a resource from the resource directory in the jar file.
   * (Callisto.jar://org/mitre/jawb/resources)
   * @see java.lang.Class#getResource
   */
  public static final URL getResource(String name) {
    return Jawb.class.getResource(RESOURCE_DIR + "/" + name);
  }
  
  /**
   * Get a resource from the resource directory in the jar file, as a stream.
   * (Callisto.jar://org/mitre/jawb/resources)
   * @see java.lang.Class#getResource
   */
  public static final InputStream getResourceAsStream(String name) {
    return Jawb.class.getResourceAsStream(RESOURCE_DIR + "/" + name);
  }
  
  /**
   * Load and cache image. Returns null if image not found. If source is null,
   * loads from the Jawb resource directory
   * (Callisto.jar://org/mitre/jawb/resources)
   */
  public static final Image getImage(String image, Class source) {
    if (source == null) {
      image = RESOURCE_DIR + "/" + image;
      source = Jawb.class;
    }
    
    String key = source.getName() + image;
    Image img = (Image)imageCache.get(key);
    if (img == null) {
      URL resource = source.getResource(image);
      if (resource == null) {
        throw new NullPointerException
        (image+" not found in relation to "+source);
      }
      img = Toolkit.getDefaultToolkit().getImage(resource);
      loadImage(img);
      imageCache.put(key, img);
    }
    return img;
  }
  
  /**
   * Load and cache image as an icon. If source is null, loads from the
   * Callisto resource directory (Callisto.jar://org/mitre/jawb/resources)
   */
  public static final ImageIcon getIcon(String image, Class source) {
    return new ImageIcon(getImage(image, source));
  }
  
  /** Same as method used in javax.swing.ImageIcon */
  private static final void loadImage(Image image) {
    synchronized (tracker) {
      int id = ++mediaTrackerID;
      
      tracker.addImage(image, id);
      try {
        tracker.waitForID(id, 0);
      } catch (InterruptedException e) {
        System.out.println("INTERRUPTED while loading Image");
      }
      tracker.removeImage(image, id);
    }
  }
  
  /** Used for MediaTracker when loading images */
  private static class EmptyComponent extends Component {};
  
}// Jawb

