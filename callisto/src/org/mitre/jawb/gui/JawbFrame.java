
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

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Component;
import java.awt.Container;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

import javax.swing.*;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import javax.swing.text.*;
import javax.swing.border.BevelBorder;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.atlas.PhraseTaggingAnnotation;
import org.mitre.jawb.atlas.AWBDocument;
import org.mitre.jawb.atlas.AnnotationModel;
import org.mitre.jawb.atlas.AnnotationModelEvent;
import org.mitre.jawb.atlas.AnnotationModelListener;
import org.mitre.jawb.prefs.PreferenceDialog;
import org.mitre.jawb.prefs.ColorSpec;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.swing.*;

import org.mitre.jawb.tasks.Exporter;
//import org.mitre.jawb.tasks.JSonExporter;
//import org.mitre.jawb.tasks.JSonImporter;
import org.mitre.jawb.tasks.Importer;
import org.mitre.jawb.tasks.Validator;
import org.mitre.jawb.tasks.ValidationResult;
import org.mitre.jawb.tasks.preannotate.*;
import org.mitre.jawb.tasks.generic.GenericTaskPanel;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;
import org.mitre.jawb.tasks.ToocaanTask;
import org.mitre.jawb.tasks.ToocaanToolKit;
import org.mitre.jawb.gui.ActionModel.ActionGroup;
import org.mitre.jawb.io.*;
import org.mitre.jawb.JawbLogger;

/**
 * Main Frame for the application. Allows for multiple frames in an instance
 * coordinated through the {@link Jawb} object.
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */
public class JawbFrame extends JFrame {

  private static int DEBUG = 0;


  /** Key for listening for document changes */
  public static final String CURRENT_DOCUMENT_KEY = "currentDocument";
  
  /** Length of time to display unspecified status messages for */
  public static final int DEFAULT_STATUS_TIME = 5;

  /** Number of files to display */
  private static final int DEFAULT_RECENT_FILE_MAX = 10;
  
  /** Dialog to set preferences. All prefs are global, so this is static */
  private static PreferenceDialog prefsDialog = null;

  /** Dialog to edit AIF files. This is a stand alone tool, thus is static. */
  private static ReferenceEditor referenceEditor = null;

  /** Dialog to compile DTDs into tasks. This is stand alone, thus static. */
  private static GenericTaskPanel dtdTaskPanel = null;

  /** Active Frames */
  private static List frameList = new LinkedList ();

  /** Tracks what files are open independent of frames */
  private static DocumentManager docManager = null;

  /** Java doesn't work well w/ window managers. */
  private static Point lastLocation;

  
  /** Buttons for easy access */
  private JToolBar toolBar;
  
  /** Displays short messages to the user */
  private JLabel statusField;

  /** Indicates the selection mode */
  private JCheckBox selectByWord;

  /** Visual display of current font */
  private JLabel fontField;

  /** Visual display of current charset */
  private JLabel charsetField;

  /** Visual display of current task */
  private JLabel taskField;
  
  /** Thread which will clear status messages when they are not indefinate. */
  private Thread statusTimer = null;

  /** Main menubar for the Frame */
  private JMenuBar menuBar;

  /** Menu of recently opened files, managed by docmgr (must be released) */
  private JMenu historyMenu = null;
  
  /** Menu of currently open files, managed by docmgr (must be released) */
  private JMenu documentMenu = null;
  
  /** Menu of Toocan-specific actions */
  private JMenu toocaanMenu = null;

  /** toggle */
  private JMenuItem toggleItem = null;
  
  /** Current file being edited in this frame */
  private JawbDocument currentDoc = null;

  /** workspace information */
  private WorkspaceDashboard workspaceDash = null;

  // default
  private static String defaultWorkspaceURL = "http://localhost:7801";
  

  /** Current collection being edited in this frame, if applicable */
  //  private JawbCollection currentCollection = null;

  /** Index of the current file being edited, within its collection */
  //  private int currentCollIndex = -1;
  
  /** Current file being edited in this frame */
  private JawbDocument lastDoc = null;
  
  /** Access to menu which task may modify (static to all frames) */
  private JMenu taskMenu;
  private ButtonGroup taskGroup;

  /** Import documents from other format. shared among frames. */
  // TODO: this action needs to be individual, not shared
  private static Action importAction;

  /** Export documents to new format. sharedamong frames. */
  // TODO: this action needs to be individual, not shared
  private static Action exportAction;

  //TODO: private static Action trainAction;

    /** Close documents with this action. individual to frame, to follow doc. */
  // TODO: this action needs to be individual, not shared
  private Action closeAction;

  /** Save documents with this action. individual to frame, to follow doc. */
  // TODO: this action needs to be individual, not shared
  private Action saveAction;

  /** Save documents to new location. individual to frame, to follow doc. */
  // TODO: this action needs to be individual, not shared
  private Action saveAsAction;

  //  private Action ednaReleaseAction;
  private Action ednaImportAction;

  // actions for Mixed Initiative Workspace actions
  private Action openWorkspaceAction;
  private Action nextWSDocAction;
  // private Action saveWSDocAction; // no longer a separate action (just
                                     // use save and it will do the
                                     // right thing)

  private Action doneWSDocAction;
  // private Action adjWSDocAction;

  // action to review remaining to_review segments during adjudication
  private Action reviewAction;


  /** Additional actions for Collections */
  private Action newCollAction;
  private Action browseCollAction;
  private Action openCollAction;
  private Action importCollAction;
  private Action exportCollAction;
  private Action doneAction;
  private Action nextAction;

  /**
   * Count of threads 'waiting' for things to happen before window is supposed
   * to be active again TODO: waitCount isn't used
   */
  private int waitCount = 0;

  /** Current Task's 'mainComponent' is displayed here */
  private JawbComponentContainer mainContainer;
  /** A LabelJawbComponent to display when no document current */
  private LabelJawbComponent imageComponent;
  private int mainComponentSize = -1;             // remembers specified size
  private double mainComponentDefaultSize = 0.66; // default for mainContainer

  /** Current Tasks 'editorComponent' is displayed here */
  private JawbComponentContainer editorContainer;

  /** Create only one TaskToolKit per task for each frame and cache here. */
  private HashMap taskToToolKitMap = new HashMap ();
  
  /** Create a single JawbDocument, for use as main component, for each task. */
  private HashMap taskToMainCompMap = new HashMap ();
  
  /** Maintains user adjustable separation between Main and Editor views */
  private SplitPaneJawbComponent mainSplit;

  /** Updates GUI stuff when current doc, or any prefs change */
  private PropertyChangeListener docAndPrefsListener =
    new DocAndPrefsListener ();
  
  private static int id=0;
  private int myID = id++;
  public String toString () {return Integer.toString (myID);}

  private boolean addToocaan = false;
  
    /**
   * Returns the JawbFrame currently hosted the JawbDocument
   * @param jd the JawbDocument whose JawbFrame we require
   */
  public static JawbFrame getFrame(JawbDocument jd) {
    for (Iterator i = frameList.iterator(); i.hasNext();) {
      JawbFrame frame = (JawbFrame) i.next();
      if (frame.getJawbDocument().equals(jd))
	return frame;
    }
    return null;
  }

  
  /**
   * Initialize a new Frame for Jawb, but use the factory method createFrame.
   */
  public JawbFrame () {
    this (null);
  }
  
  /**
   * Initialize a new Frame for Jawb opening specified file names.
   * @param fileNames array of strings from command line indicating files to
   *    open
   */
  public JawbFrame (String[] fileNames) {
    // TODO: do something with fileNames
    setIconImage (GUIUtils.getJawbIconImage ());

    // There is only ever one of these
    if (docManager == null)
      docManager = new DocumentManager ();
    
    Container contentPane = getContentPane();
    contentPane.setLayout (new BorderLayout());

    // this with the closing listener below will allow user to cancel close
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          closeFrame ();
        }
        public void windowOpened(WindowEvent e) {
          // the first frame to open, creates and centers the preferences
          if (prefsDialog == null) {
            prefsDialog =
              new PreferenceDialog (JawbFrame.this, Jawb.getPreferences ());
            GUIUtils.centerComponent (JawbFrame.this, prefsDialog);
          }
        }
      });

    addToocaan = Jawb.includesToocaan();
    
    /************* status message area **************************/

    JPanel statusPanel = new JPanel (new BorderLayout ());
    Box statusBox = new Box(BoxLayout.X_AXIS);
    Font textFieldFont = UIManager.getFont ("TextField.font");
    
    statusField = new JLabel();
    statusField.setFont(textFieldFont);
    statusField.setBorder(BorderFactory.createCompoundBorder
         (BorderFactory.createBevelBorder(BevelBorder.LOWERED),
          BorderFactory.createEmptyBorder (0,2,0,2)));
    statusPanel.add (statusField, BorderLayout.CENTER);

    selectByWord = initSelectModeCheckBox();
    selectByWord.setBorder(BorderFactory.createCompoundBorder
         (BorderFactory.createBevelBorder(BevelBorder.LOWERED),
          BorderFactory.createEmptyBorder (0,2,0,2)));
    statusBox.add (selectByWord);

    fontField = new JLabel ();
    fontField.setFont(textFieldFont);
    fontField.setBorder (BorderFactory.createCompoundBorder
         (BorderFactory.createBevelBorder(BevelBorder.LOWERED),
          BorderFactory.createEmptyBorder (0,2,0,2)));
    statusBox.add (fontField);
    
    charsetField = new JLabel ();
    charsetField.setFont(textFieldFont);
    charsetField.setBorder (BorderFactory.createCompoundBorder
         (BorderFactory.createBevelBorder(BevelBorder.LOWERED),
          BorderFactory.createEmptyBorder (0,2,0,2)));
    statusBox.add (charsetField);
    
    taskField = new JLabel ();
    taskField.setFont(textFieldFont);
    taskField.setBorder (BorderFactory.createCompoundBorder
         (BorderFactory.createBevelBorder(BevelBorder.LOWERED),
          BorderFactory.createEmptyBorder (0,2,0,2)));
    statusBox.add (taskField);

    statusPanel.add (statusBox, BorderLayout.EAST);
    contentPane.add (statusPanel, BorderLayout.SOUTH);

    /**************** menu setup **********************/
    
    menuBar = new JMenuBar ();
    Action action; // only if we need to do extra.
    JMenu menu;
    JMenu subMenu;
    JMenuItem mi;

    // these get turned on and off at various points so need reference
    closeAction = Jawb.getAction ("close");
    saveAction = Jawb.getAction ("save");
    saveAsAction = Jawb.getAction ("saveas");
    importAction = Jawb.getAction ("import");
    exportAction = Jawb.getAction ("export");
    
    ednaImportAction = Jawb.getAction("ednaImport");
    //    ednaReleaseAction = Jawb.getAction("ednaRelease");
    //TODO: trainAction = Jawb.getAction ("train");
    
    if (addToocaan) {
      openWorkspaceAction = Jawb.getAction("openWS");
      nextWSDocAction = Jawb.getAction("nextWSdoc");
      // saveWSDocAction = Jawb.getAction("saveWSdoc");
      doneWSDocAction = Jawb.getAction("doneWSdoc");
      // adjWSDocAction = Jawb.getAction("adjWSdoc");
    }

    newCollAction = Jawb.getAction ("newColl");
    browseCollAction = Jawb.getAction ("browseColl");
    openCollAction = Jawb.getAction ("openColl");

    doneAction = Jawb.getAction ("done");
    nextAction = Jawb.getAction ("next");
    
    importCollAction = Jawb.getAction ("importColl");
    exportCollAction = Jawb.getAction ("exportColl");

    //first, the file menu
    menu = new JMenu ("File");
    menu.setMnemonic('F');
    menu.add(new JMenuItem (Jawb.getAction ("new")));
    menu.add(new JMenuItem (Jawb.getAction ("open")));
    menu.add(new JMenuItem (closeAction));

    if (addToocaan) {
      menu.addSeparator();
      menu.add(new JMenuItem (openWorkspaceAction));
      menu.add(new JMenuItem (nextWSDocAction));
      // menu.add(new JMenuItem (saveWSDocAction));
      menu.add(new JMenuItem (doneWSDocAction));
      // menu.add(new JMenuItem (adjWSDocAction));
      // until a workspace is opened, disable WS save and next actions
      nextWSDocAction.setEnabled(false);
      doneWSDocAction.setEnabled(false);
    }

    menu.addSeparator();
    menu.add(new JMenuItem (newCollAction));
    // TODO add when this functionality is available
    //    menu.add(new JMenuItem (browseCollAction));
    menu.add(new JMenuItem (openCollAction));
    
    menu.addSeparator();
    menu.add(new JMenuItem (saveAction));
    menu.add(new JMenuItem (saveAsAction));
    menu.add(new JMenuItem (doneAction));
    menu.add(new JMenuItem (nextAction));

    menu.addSeparator(); // maybe these will become nested?
    menu.add(new JMenuItem (importAction));
    menu.add(new JMenuItem (exportAction));
    // TODO add these at a later time
    //menu.add(new JMenuItem (importCollAction));
    //menu.add(new JMenuItem (exportCollAction));
    
    Preferences p = Jawb.getPreferences();
    if(p.getBoolean("jawbFrame.showEdnaImport")) {
      menu.add(new JMenuItem(ednaImportAction));
      //      menu.add(new JMenuItem(ednaReleaseAction));
    }
    JawbAction aceETAction = Jawb.getAction("aceETImport");
    if (aceETAction != null) {
      menu.add(new JMenuItem(aceETAction));
    }
     
    //TODO: menu.add(new JMenuItem (trainAction));

    boolean haveImporter = (GUIUtils.getSelectedImporter() != null);
    importAction.setEnabled (haveImporter); // disable if none

    //menu.addSeparator();
    //menu.add(new JMenuItem (Jawb.getAction ("print")));

    // if no collection, disable Done and Next actions
    JawbCollection jc = getJawbCollection();
    if (jc == null) {
      doneAction.setEnabled(false);
      nextAction.setEnabled(false);
    }

    menu.addSeparator();
    menu.add (documentMenu = docManager.createDocumentMenu ());
    menu.add (historyMenu = docManager.createHistoryMenu ());
    
    menu.addSeparator();
    menu.add(new JMenuItem (Jawb.getAction ("newFrame")));
    menu.add(new JMenuItem (Jawb.getAction ("closeFrame")));

    menu.addSeparator();
    menu.add (new JMenuItem (Jawb.getAction ("exit")));

    menuBar.add (menu);
    
    //next, the edit menu
    menu = new JMenu ("Edit");
    menu.setMnemonic('E');

    menu.add(initActionsDialogToggle());
    menu.add(initCaretVisibleToggle());
    menu.add(initSelectModeMenu());
    menu.add(initAnnotationInspectorToggle());
    menu.add(initAnnotationInspectorVisibleRequired());

    menu.addSeparator ();
    menu.add(initFindString());
    menu.add(initFindAgain());
    menu.add(initFindPrevious());
    
    menu.addSeparator ();
    menu.add (new JMenuItem (Jawb.getAction ("preferences")));

    menuBar.add (menu);
    
    //next, the format menu
    menu = new JMenu ("Format");
    menu.setMnemonic ('O');
    menu.add (new JMenuItem (new ToggleComponentOrientationAction ()));

    subMenu = new JMenu ("Font Size");
    int sizes[] = new int[] {10, 12, 14, 16, 18, 22};
    for (int i=0; i<sizes.length; i++)
      subMenu.add (new JMenuItem (new FontSizeAction (new Integer(sizes[i]))));
    subMenu.add (new JMenuItem (new FontSizeAction (null)));
    menu.add (subMenu);

    subMenu = new JLongMenu ("Font");
    subMenu.add (new JMenuItem (new FontDetectAction ()));
    subMenu.addSeparator ();
    GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
    String[] families = g.getAvailableFontFamilyNames ();
    for (int i=0; i<families.length; i++)
      subMenu.add (new JMenuItem (new FontAction (families[i])));
    menu.add (subMenu);
    
    subMenu = new JMenu("Line Spacing");
    float space[] = new float[] {1.0f, 1.2f, 1.4f, 1.6f, 2f, 2.5f};
    for (int i=0; i<space.length; i++)
      subMenu.add(new JMenuItem(new LineSpacingAction(new Float(space[i]))));
    subMenu.add(new JMenuItem(new LineSpacingAction(null)));
    menu.add(subMenu);

    subMenu = new JLongMenu ("Character Encoding");
    Iterator charsetIter = Charset.availableCharsets().keySet().iterator();
    while (charsetIter.hasNext ()) {
      String encoding = (String)charsetIter.next ();
      subMenu.add (new JMenuItem (new SetEncodingAction (encoding)));
    }
    menu.add (subMenu);

    menuBar.add (menu);

    //next, the options menu
    menu = new JMenu ("Tools");
    menu.setMnemonic ('T');
    menu.add(new JMenuItem(new ReferenceEditorAction()));
    //TODO: menu.add(new JMenuItem(Jawb.getAction("tallal")));
    menu.add(new JMenuItem(new DTDCompilerAction()));
    
    menuBar.add (menu);

    //finally, the help menu
    menu = new JMenu ("Help");
    menu.setMnemonic ('H');
    menu.add (new JMenuItem (Jawb.getAction ("help")));

    menu.addSeparator ();
    menu.add (new JMenuItem (Jawb.getAction ("about")));
    menu.add (new JMenuItem (Jawb.getAction ("aboutTasks")));
    menu.addSeparator ();
    menu.add (new JMenuItem (Jawb.getAction ("getConsoleLog")));
    menuBar.add (menu);
    
    if (addToocaan) {
      // add the toocaanMenu which will be active/visible only when
      // setJawbDocument is called to display a document whose task
      // is a ToocaanTask
      toocaanMenu = new JMenu ("TooCAAn");
      toocaanMenu.setMnemonic ('T');
      toggleItem = initSegmentsSelectableToggle();
      toocaanMenu.add (toggleItem);
      reviewAction = Jawb.getAction ("reviewSegments");
      reviewAction.setEnabled(false);
      toocaanMenu.add (reviewAction);
      toocaanMenu.setEnabled(false); 

      menuBar.add (toocaanMenu);
    }
    
    //add the menubar to the right place within our GBL
    setJMenuBar (menuBar);

    /************* main window **************************/

    // since it's _possible_ there will be other JawbComponents, I use this so
    // I'll never have to add/remove from splitpane
    mainContainer = new JawbComponentContainer ();
    editorContainer = new JawbComponentContainer ();

    mainSplit = new SplitPaneJawbComponent (JSplitPane.VERTICAL_SPLIT);

    contentPane.add (mainSplit, BorderLayout.CENTER);
    
    mainSplit.setBottomComponent (editorContainer);
    mainSplit.setTopComponent (mainContainer);
    mainSplit.setDividerLocation (mainComponentDefaultSize);
    mainSplit.setResizeWeight (mainComponentDefaultSize);
    mainSplit.invalidate ();

    // hard coding seems better than:
    //mainSplit.setDividerSize (this.getInsets().bottom);
    mainSplit.setDividerSize (6);
    
    // TODO: I played with when to set divider size to get it to work and
    // that's why it's after the previous validation and then calls 'doLayout'
    // it was originally fine on Linux, but not on Win32. Is there a better
    // way?

    // shortly before v1.23 of this in CVS all the nested Tables were removed,
    // and no editors were shown until a document was opened. this seems to
    // have worked wonders with the layout so i nixed a lot of cruft and
    // reordered all this.

    /***************** final config *********************/
    imageComponent =
      new LabelJawbComponent (Jawb.getIcon ("splash.gif", null));
    imageComponent.setBackground (Color.white);
    imageComponent.setOpaque (true);

    setCurrentDocument (null);

    Jawb.getPreferences ().addPropertyChangeListener (docAndPrefsListener);
    
    if (! GUIUtils.loadGeometry (this, "jawb"))
      setSize (640, 700);
    
    // TODO: if java ever allows window managers to place frames. remove this.
    // umm... doesn't this conflict with loadGeometry above?
    if (frameList.size() == 0) {
      lastLocation = this.getLocation ();
    } else {
      Insets insets = ((JawbFrame)frameList.get (0)).getInsets ();
      lastLocation.x += insets.top;
      lastLocation.y += insets.top;
      this.setLocation (lastLocation);
    }
    frameList.add (this);
    Jawb.getAction ("closeFrame").setEnabled (frameList.size () > 1);
  }

  /***********************************************************************/
  /* Text Toggle */
  /***********************************************************************/
  
  /**
   * Initialize toggle menuitem which displays/hides the Available Actions.
   */
  private JMenuItem initActionsDialogToggle () {
    
    if (DEBUG > 0)
      System.err.println ("JF: Initializing Available Actions Visible Toggle");
    
    Preferences prefs = Jawb.getPreferences ();
    Action act = new AbstractAction ("Show Available Actions") {
        public void actionPerformed (ActionEvent e) {
          boolean visible = ((AbstractButton) e.getSource()).isSelected();
          Preferences prefs = Jawb.getPreferences ();
          prefs.setPreference(MainTextPane.ACTIONS_DIALOG_VISIBLE_KEY,visible);
        }
      };
    final JCheckBoxMenuItem mi = new JCheckBoxMenuItem (act);
    mi.setState (prefs.getBoolean(MainTextPane.ACTIONS_DIALOG_VISIBLE_KEY));
  
    // Updates toggle button when ACTIONS_DIALOG_VISIBLE_KEY changes
    PropertyChangeListener pcl = new PropertyChangeListener () {
        public void propertyChange (PropertyChangeEvent e) {
          mi.setState (((Boolean)e.getNewValue()).booleanValue ());
        }
      };
    prefs.addPropertyChangeListener(MainTextPane.ACTIONS_DIALOG_VISIBLE_KEY, pcl);

    return mi;
  }

  
  /***********************************************************************/
  /* Text Toggle */
  /***********************************************************************/
  
  /**
   * Initialize toggle menu item to displays/hides the Caret in MainTextPane.
   */
  private JMenuItem initCaretVisibleToggle () {
    
    if (DEBUG > 0)
      System.err.println ("JF: Initializing Caret Visible Toggle");

    Preferences prefs = Jawb.getPreferences ();
    Action act = new AbstractAction ("Show Cursor in Text") {
        public void actionPerformed (ActionEvent e) {
          boolean visible = ((AbstractButton) e.getSource()).isSelected ();
          Preferences prefs = Jawb.getPreferences ();
          prefs.setPreference(MainTextPane.CARET_VISIBLE_KEY, visible);
        }
      };

    final JCheckBoxMenuItem mi = new JCheckBoxMenuItem (act);
    mi.setState (prefs.getBoolean (MainTextPane.CARET_VISIBLE_KEY));
    
    // Updates toggle button if changed elsewhere
    PropertyChangeListener pcl = new PropertyChangeListener () {
        public void propertyChange (PropertyChangeEvent e) {
          mi.setState (((Boolean) e.getNewValue()).booleanValue());
        }
      };
    prefs.addPropertyChangeListener(MainTextPane.CARET_VISIBLE_KEY, pcl);
    
    return mi;
  }
  
  /***********************************************************************/
  /* Annotation Inspector Toggle */
  /***********************************************************************/
  
  /**
   * Initialize toggle menu item to displays/hides the Popup Annotation
   * inspector in MainTextPane.
   */
  private JMenuItem initAnnotationInspectorToggle () {
    
    if (DEBUG > 0)
      System.err.println ("JF: Initializing Annotation Inspector Toggle");
    
    Preferences prefs = Jawb.getPreferences ();
    Action act = new AbstractAction ("Show Annotations with SHIFT") {
      public void actionPerformed (ActionEvent e) {
        boolean visible = ((AbstractButton) e.getSource()).isSelected ();
        Preferences prefs = Jawb.getPreferences ();
        prefs.setPreference(MainTextPane.ANNOTATION_INSPECTOR_REQUIRE_SHIFT_KEY, visible);
      }
    };
    
    final JCheckBoxMenuItem mi = new JCheckBoxMenuItem (act);
    mi.setState (prefs.getBoolean (MainTextPane.ANNOTATION_INSPECTOR_REQUIRE_SHIFT_KEY));
    
    // Updates toggle button if changed elsewhere
    PropertyChangeListener pcl = new PropertyChangeListener () {
      public void propertyChange (PropertyChangeEvent e) {
        mi.setState (((Boolean) e.getNewValue()).booleanValue());
      }
    };
    prefs.addPropertyChangeListener(MainTextPane.ANNOTATION_INSPECTOR_REQUIRE_SHIFT_KEY, pcl);
    
    return mi;
  }

  /**
   * Initialize toggle menu item to toggle the display of non-visible
   * annotations in the annotaiton inspector in MainTextPane.
   */
  private JMenuItem initAnnotationInspectorVisibleRequired () {
    
    if (DEBUG > 0)
      System.err.println ("JF: Initializing Annotation Inspector VisibleRequired");
    
    Preferences prefs = Jawb.getPreferences ();
    Action act = new AbstractAction ("Show Only Visible Annotations in Inspector") {
      public void actionPerformed (ActionEvent e) {
        boolean requireVisible = ((AbstractButton) e.getSource()).isSelected ();
        Preferences prefs = Jawb.getPreferences ();
        prefs.setPreference(MainTextPane.ANNOTATION_INSPECTOR_REQUIRE_VISIBLE, requireVisible);
      }
    };
    
    final JCheckBoxMenuItem mi = new JCheckBoxMenuItem (act);
    mi.setState (prefs.getBoolean (MainTextPane.ANNOTATION_INSPECTOR_REQUIRE_VISIBLE));
    
    // Updates toggle button if changed elsewhere
    PropertyChangeListener pcl = new PropertyChangeListener () {
      public void propertyChange (PropertyChangeEvent e) {
        mi.setState (((Boolean) e.getNewValue()).booleanValue());
      }
    };
    prefs.addPropertyChangeListener(MainTextPane.ANNOTATION_INSPECTOR_REQUIRE_VISIBLE, pcl);
    
    return mi;
  }

  
  /***********************************************************************/
  /* Selection Mode */
  /***********************************************************************/
  
  /**
   * Initialize the selection mode menu. This is unique to each frame and
   * tracks the selection mode of individual documents.
   */
  private JMenuItem initSelectModeMenu () {

    if (DEBUG > 0)
      System.err.println ("JF: Initializing Select Mode Menu");
    
    AutoSelectCaret.Mode currentSelectMode = AutoSelectCaret.Mode.WORD;
    Preferences prefs = Jawb.getPreferences();
    String mName = prefs.getPreference (MainTextPane.SELECTION_MODE_KEY,
                                        currentSelectMode.toString());
    try {
      currentSelectMode = AutoSelectCaret.Mode.decode(mName);
    } catch (Exception ignore) {}
    Action act = new AbstractAction ("Text Swipe by Word") {
        public void actionPerformed (ActionEvent e) {
          boolean isWordMode = ((AbstractButton) e.getSource()).isSelected ();
          String mName = (isWordMode ? AutoSelectCaret.Mode.WORD.toString() :
                          AutoSelectCaret.Mode.CHARACTER.toString());
          Preferences prefs = Jawb.getPreferences ();
          prefs.setPreference(MainTextPane.SELECTION_MODE_KEY, mName);
        }
      };
    act.putValue(Action.ACTION_COMMAND_KEY, "toggle-selection-mode");
    act.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl T"));
    act.putValue(Action.MNEMONIC_KEY, new Integer('T'));
    
    final JCheckBoxMenuItem mi = new JCheckBoxMenuItem(act);
    mi.setState(currentSelectMode.equals(AutoSelectCaret.Mode.WORD));
    
    // Updates toggle button if changed elsewhere
    PropertyChangeListener pcl = new PropertyChangeListener () {
        public void propertyChange (PropertyChangeEvent e) {
          String mName = (String) e.getNewValue();
          AutoSelectCaret.Mode mode = AutoSelectCaret.Mode.decode(mName);
          mi.setState(mode.equals(AutoSelectCaret.Mode.WORD));
        }
      };
    prefs.addPropertyChangeListener(MainTextPane.SELECTION_MODE_KEY, pcl);
    
    return mi;
  }
  
  /**
   * Initialize the selection mode CheckBox. This is unique to each frame and
   * tracks the selection mode of individual documents.
   */
  private JCheckBox initSelectModeCheckBox () {

    if (DEBUG > 0)
      System.err.println ("JF: Initializing Select Mode CheckBox");
    
    AutoSelectCaret.Mode currentSelectMode = AutoSelectCaret.Mode.WORD;
    Preferences prefs = Jawb.getPreferences();
    String mName = prefs.getPreference (MainTextPane.SELECTION_MODE_KEY,
                                        currentSelectMode.toString());
    try {
      currentSelectMode = AutoSelectCaret.Mode.decode(mName);
    } catch (Exception ignore) {}

    Action act = new AbstractAction ("Text Swipe by Word") {
        public void actionPerformed (ActionEvent e) {
          boolean isWordMode = ((AbstractButton) e.getSource()).isSelected ();
          String mName = (isWordMode ? AutoSelectCaret.Mode.WORD.toString() :
                          AutoSelectCaret.Mode.CHARACTER.toString());
          Preferences prefs = Jawb.getPreferences ();
          prefs.setPreference(MainTextPane.SELECTION_MODE_KEY, mName);
        }
      };
    act.putValue(Action.ACTION_COMMAND_KEY, "toggle-selection-mode");
    act.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl T"));
    act.putValue(Action.MNEMONIC_KEY, new Integer('T'));
    
    final JCheckBox modeCheck = new JCheckBox(act);
    modeCheck.setSelected(currentSelectMode.equals(AutoSelectCaret.Mode.WORD));
    
    // Updates toggle button if changed elsewhere
    PropertyChangeListener pcl = new PropertyChangeListener () {
        public void propertyChange (PropertyChangeEvent e) {
          String mName = (String) e.getNewValue();
          AutoSelectCaret.Mode mode = AutoSelectCaret.Mode.decode(mName);
          modeCheck.setSelected(mode.equals(AutoSelectCaret.Mode.WORD));
        }
      };
    prefs.addPropertyChangeListener(MainTextPane.SELECTION_MODE_KEY, pcl);
    
    return modeCheck;
  }

  public AutoSelectCaret.Mode getSelectionMode () {
    JawbComponent jc = mainContainer.getJawbComponent ();
    if (jc != null) {
      if (jc instanceof MainTextPane)
        return ((MainTextPane)jc).getSelectionMode ();
      else 
        System.err.println ("JF.setSelMode: can't get mode of main component");
    }
    return null;
  }

  private JMenuItem initFindString() {
    Action action = new JawbAction("Find...") {
        public void actionPerformed(ActionEvent e) {
          JawbFrame jf = getJawbFrame(e);
          JawbComponent main = jf.mainContainer.getJawbComponent();
          if (main != null && main instanceof MainTextPane)
            ((MainTextPane)main).setFindDialogVisible(true);
        }
      };
    action.putValue(Action.ACTION_COMMAND_KEY, "find");
    action.putValue(Action.MNEMONIC_KEY, new Integer('F'));
    action.putValue(Action.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke("ctrl F"));
    return new JMenuItem(action);
  }

  private JMenuItem initFindAgain() {
    Action action = new JawbAction("Find Again") {
        public void actionPerformed(ActionEvent e) {
          JawbFrame jf = getJawbFrame(e);
          JawbComponent main = jf.mainContainer.getJawbComponent();
          if (main != null && main instanceof MainTextPane)
            ((MainTextPane)main).findAgain(true);
        }
      };
    action.putValue(Action.ACTION_COMMAND_KEY, "find-again");
    action.putValue(Action.MNEMONIC_KEY, new Integer('G'));
    action.putValue(Action.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke("ctrl G"));
    return new JMenuItem(action);
  }
  
  private JMenuItem initFindPrevious() {
    Action action = new JawbAction("Find Previous") {
        public void actionPerformed(ActionEvent e) {
          JawbFrame jf = getJawbFrame(e);
          JawbComponent main = jf.mainContainer.getJawbComponent();
          if (main != null && main instanceof MainTextPane)
            ((MainTextPane)main).findAgain(false);
        }
      };
    action.putValue(Action.ACTION_COMMAND_KEY, "find-previous");
    action.putValue(Action.MNEMONIC_KEY, new Integer('V'));
    action.putValue(Action.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke("ctrl shift G"));
    return new JMenuItem(action);
  }
  
  /**
   * Initialize toggle menuitem which allows or disallows selection of
   * SEGMENT annotations
   */
  private JMenuItem initSegmentsSelectableToggle () {
    
    if (DEBUG > 0)
      System.err.println ("JF: Initializing Segments Selectable Toggle");

    Preferences prefs = Jawb.getPreferences ();
    Action act = new JawbAction ("Make Segments Selectable") {
        public void actionPerformed (ActionEvent e) {
          boolean selectable = ((AbstractButton) e.getSource()).isSelected();
          String taskName = getJawbDocument(e).getTask().getName();
          adjustSelectionPreferences(taskName, selectable);
        }
      };
    final JCheckBoxMenuItem mi = new JCheckBoxMenuItem (act);
    // when this is getting initialized for the first time, there
    // is no document, thus no class and no selectable preferences
    // so just set the default state to false here
    mi.setState(false);

    /******* For now we are not allowing the user to modify the "selectable"
             property in any other way, so we don't need this (which would
             have to be modified to reflect the correct properties if used)
    // Updates toggle button when ACTIONS_DIALOG_VISIBLE_KEY changes
    PropertyChangeListener pcl = new PropertyChangeListener () {
        public void propertyChange (PropertyChangeEvent e) {
          mi.setState (((Boolean)e.getNewValue()).booleanValue ());
        }
      };
    prefs.addPropertyChangeListener(MainTextPane.ACTIONS_DIALOG_VISIBLE_KEY, pcl);
    **********************************************************************/

    return mi;
  }

  private void adjustSelectionPreferences(String taskName, 
                                          boolean selectable) {
    Preferences prefs = Jawb.getPreferences ();
    prefs.setPreference("task."+taskName+"."+
                        ToocaanTask.SEGMENT_COMPLETED_KEY+
                        ".selectable", selectable);
    prefs.setPreference("task."+taskName+"."+
                        ToocaanTask.SEGMENT_LOWERPRI_KEY+
                        ".selectable", selectable);
    prefs.setPreference("task."+taskName+"."+
                        ToocaanTask.SEGMENT_BOOTSTRAP_KEY+
                        ".selectable", selectable);
    prefs.setPreference("task."+taskName+"."+
                        ToocaanTask.SEGMENT_PROPOSED_KEY+
                        ".selectable", selectable);
  }

  /***********************************************************************/
  /* Display Functions
  /***********************************************************************/

  /** Accessed multiple times so use this to remain consistent. */
  private void updateTitle () {
    StringBuffer title = new StringBuffer ("Callisto");
    
    JawbCollection currentCollection = getJawbCollection();
    if (currentCollection != null) {
      title.append (" ~ ");
      title.append (currentCollection.getDisplayName ());
    }

    if (currentDoc != null) {
      title.append (" - ");
      boolean fullPath = 
        Jawb.getPreferences ().getBoolean (Preferences.SHOW_FULL_PATH_KEY);
      title.append (currentDoc.getDisplayName (fullPath));
      if (currentDoc.isDirty())
        title.append ("*");
    }
    super.setTitle (title.toString ());
  }

  /** Accessed multiple times so use this to remain consistent. */
  private void updateFontField () {
    String s = "Font: ";
    if (currentDoc != null)
      s += currentDoc.getFontSize()+"pt. "+currentDoc.getFontFamily();
    fontField.setText (s);
  }

  /** Accessed multiple times so use this to remain consistent. */
  private void updateCharsetField () {
    String s = "Charset: ";
    if (currentDoc != null)
      s += currentDoc.getEncoding();
    charsetField.setText (s);
  }

  /** Accessed multiple times so use this to remain consistent. */
  private void updateTaskField () {
    String s = "Task: ";
    if (currentDoc != null)
      s += currentDoc.getTask().getTitle();
    taskField.setText (s);
  }

  /**
   * Set the statusField text for the default time (5 seconds).
   */
  public void setStatus (String message) {
    int seconds = Jawb.getPreferences().getInteger ("jawb.status.time",
                                                    DEFAULT_STATUS_TIME);
    setStatus (message, seconds);
  }
  
  /**
   * Set the statusField text for specified amount of time.
   * if delay < 1, delay is indefinate
   */
  public void setStatus (String message, final int seconds) {
    if (statusField == null)
      return;
    // SAM 12/27/05: Added permanent notification for when
    // logging is enabled.
    boolean loggingEnabled = Jawb.getLogger().getEnabled();
    final String nullMsg;
    final String msg;
    
    if (loggingEnabled) {
      String htmlFrag = "<font color=\"red\"><b>[LOGGING]</b></font>";
      nullMsg = "<html>"+htmlFrag+"</html>";
      if (message.length() < 1) {
        msg = nullMsg;
      } else {
        msg = "<html>"+htmlFrag+"&nbsp;"+message+"</html>";
      }
    } else {
      nullMsg = " ";
      if (message.length() < 1) {
        msg = nullMsg;
      } else {
        msg = message;
      }
    }
    
    // do the following on the gui thread
    SwingUtilities.invokeLater (new Runnable () {
        public void run () {
          // if there's a statusTimer sleeping, interrupt and set new message
          // synchronized so one doesn't slip in unnoticed
          synchronized (statusField) {
            if (statusTimer != null)
              statusTimer.interrupt ();
            statusField.setText (msg);
          }

          // don't turn off if time < 1
          if (seconds > 0) {
            // just replace any previous ones
            synchronized (statusField) {
              statusTimer = new Thread () {
                  public void run () {
                    try {
                      sleep (seconds*1000); // sleep() wants millis!
                      statusField.setText (nullMsg);
                    } catch (Exception e) { }
                  }
                };
              statusTimer.start ();
            }
          }
        }
      });
  }

  /** update workspace-related menu items */
  //TODO review this using new mode info and prioritization info
  public void updateWSMenuOptions() { 
    if (!addToocaan)
      return;
    if (workspaceDash == null) {
      openWorkspaceAction.setEnabled(true);
    } else {
      openWorkspaceAction.setEnabled(false);
      if (getMATDocBasename() == null) {
        nextWSDocAction.setEnabled(false);
        doneWSDocAction.setEnabled(false);
      } else {
        if (getMATWorkspaceActive()) {
          nextWSDocAction.setEnabled(true);
          String mode = workspaceDash.getCurrentJawbDocument().getMode();
          if (mode.equals(ToocaanTask.BOOTSTRAP_MODE) ||
              mode.equals(ToocaanTask.DOC_PRIORITIZATION_MODE) ||
              mode.equals(ToocaanTask.USER_SELECTED_MODE)) {
            doneWSDocAction.setEnabled(true);
          }
        } else {
          doneWSDocAction.setEnabled(true);
        }
      }      
    }
  }

  /** returns the current MAT Workspace Dashboard */
  public WorkspaceDashboard getMATWorkspaceDash() { return workspaceDash;}

  /** sets the current MAT Workspace Dashboard */
  public void setMATWorkspaceDash(WorkspaceDashboard dash) { 
    this.workspaceDash = dash;
    updateWSMenuOptions();
  }

  /** returns the current MAT DOC Basename */
  public String getMATDocBasename() { 
    if (workspaceDash == null) return null;
    return workspaceDash.getMATDocBasename();
  }

  /** returns the current MAT Lock ID */
  public String getMATLockId() { 
    if (workspaceDash == null) return null;
    return workspaceDash.getMATLockId();
  }

  /** returns the current MAT Folder */
  public String getMATFolder() { 
    if (workspaceDash == null) return null;
    return workspaceDash.getMATFolder();
}

  /** returns the status of the current MAT Document */
  // TODO make that string a constant somewhere
  public String getMATDocStatus() { 
    if (workspaceDash == null) return null;
    return workspaceDash.getMATDocStatus();
  }
    
  /** returns the current MAT Workspace Key*/
  public String getMATWorkspaceKey() { 
    if (workspaceDash == null) return null;
    return workspaceDash.getMATWorkspaceKey();
  }

  /** returns the current MAT Workspace URL*/
  public String getMATWorkspaceURL() { 
    if (workspaceDash == null) return null;
    return workspaceDash.getMATWorkspaceURL();
  }

  /** returns the current MAT Workspace Directory*/
  public String getMATWorkspaceDir() {
    if (workspaceDash == null) return null;
    return workspaceDash.getMATWorkspaceDir();
  }

  /** returns whether the current MAT Workspace uses active learning*/
  public boolean getMATWorkspaceActive() { 
    if (workspaceDash == null) return false;
    return workspaceDash.getMATWorkspaceActive();
  }

  /** returns the current MAT Workspace Userid*/
  public String getMATWorkspaceUserid() { 
    if (workspaceDash == null)
      return("");
    return workspaceDash.getMATWorkspaceUserid();
  }

  public void updateTKActionContext(Task task, String phase) {
    if (DEBUG > 0)
      System.err.println("JF.updateTKActContext for phase: " + phase); 
    TaskToolKit tk = getToolKit(task);
    if (!(tk instanceof ToocaanToolKit))
      return;

    // TODO replace the hardcoded strings with constants from MAT packages
    if (phase.equals(ToocaanTask.HAND_ANNOTATION)) {
      ((ToocaanToolKit)tk).
        setActionContext(ToocaanToolKit.BASE_ACTION_CONTEXT);
    } else if (phase.equals("human_decision")) {
      ((ToocaanToolKit)tk).
        setActionContext(ToocaanToolKit.DECISION_ACTION_CONTEXT);
    } else {
      if (DEBUG > 0)
        System.err.println("JF.updateTKActContext defaulting to " +
                           ToocaanToolKit.ADJ_ACTION_CONTEXT);
      ((ToocaanToolKit)tk).
        setActionContext(ToocaanToolKit.ADJ_ACTION_CONTEXT);
    }
    // now we need to get the new set of actions from the tk for this context!
    updateActions(task, tk);

  }

  public void updateTKTagInfo(Task task) {
    TaskToolKit tk = getToolKit(task);
    if (!(tk instanceof ToocaanToolKit))
      return;

    ((ToocaanToolKit)tk).updateTagActions();

  }


  /** Legacy :) */
  public JawbDocument getCurrentDocument () { return getJawbDocument (); }

  /** Sets the specified document to be displayed. */
  public void setCurrentDocument (JawbDocument doc) { setJawbDocument (doc); }

  /**
   * Returns the document currently being edited.
   */
  public JawbDocument getJawbDocument () {
    return currentDoc;
  }

  /**
   * Displays the specified document in the main text pane, and updates Title
   * and other things as necessary. If null, displays the last file
   * displayed, (if still open) or the oldest file still open, or lastly, an
   * empty panel.
   */
  public void setJawbDocument (JawbDocument doc) {
     // don't return on 'initializing' call
    if (doc == currentDoc && doc != null)
      return;
    if (DEBUG > 1)
      System.err.println ("JF.setJD: changing from doc="+
                          (currentDoc ==null ? null :
                           currentDoc.getDisplayName(false))+
                          "  to="+ (doc==null ? null :
                                    doc.getDisplayName(false)));

    JawbDocument oldDoc = currentDoc;
    // go to most recent doc if closing current (and last exists)
    if (doc == null)
      doc = lastDoc;
    else
      setLastDoc (currentDoc);
    
    // stop listening for events on old doc, and listen for events on new doc
    if (currentDoc != null) {
      //currentDoc.getAnnotationModel ().removeAnnotationModelListener (--);
      currentDoc.removePropertyChangeListener (docAndPrefsListener);
    }
    if (doc != null) {
      //doc.getAnnotationModel ().addAnnotationModelListener (--);
      doc.addPropertyChangeListener (docAndPrefsListener);
    }

    // configure the frame for this document
    if (doc != null) {
      closeAction.setEnabled (true);
      saveAsAction.setEnabled (true);
      
      /*** can't do this here because the collection hasn't been set
       * in the docManager yet 
       if (docManager.getCollection(doc) != null) {
         doneAction.setEnabled(true);
         nextAction.setEnabled(true);
       } */

      Exporter[] exporters = doc.getTask().getExporters();
      if (exporters != null && exporters.length > 0) {
        exportAction.setEnabled (true);
	//TODO: trainAction.setEnabled (true);
      }
      if (doc.isDirty ())
        saveAction.setEnabled (true);
      
      Task task = doc.getTask();
      // enable or disable the Toocaan Menu
      if (this.addToocaan) {
        toocaanMenu.setEnabled(task instanceof ToocaanTask);
        // if the "selectable" preferences are not set, initialize them
        // to false -- all 3 should be the same so I only have to check
        // one.  Unfortunately not set looks just like false, so I'll have
        // to re-set to false even if currently false
        if (task instanceof ToocaanTask) {
          String taskName = task.getName();
          Preferences prefs = Jawb.getPreferences ();
          boolean selectable = 
            prefs.getBoolean("task."+taskName+"."+
                             ToocaanTask.SEGMENT_BOOTSTRAP_KEY+
                             ".selectable");
          if (!selectable)
            adjustSelectionPreferences(taskName, selectable);
        }
      }

      // set the task specific editing environment
      // NOTE: This CREATES a ToolKit of the appropriate type for this doc
      TaskToolKit kit = getToolKit(task);
      JawbComponent main = getMainComponent(doc.getTask());
      mainContainer.setJawbComponent (main);
      JawbComponent editor = kit.getEditorComponent();
      if (editor != null) {
        editorContainer.setJawbComponent (editor);
        editorContainer.setVisible (true);
        if (mainComponentSize < 0)
          mainSplit.setDividerLocation(mainComponentDefaultSize);
        else
          mainSplit.setDividerLocation(mainComponentSize);
      } else {
        mainComponentSize = mainSplit.getDividerLocation();
        editorContainer.setJawbDocument (null);
        editorContainer.setJawbComponent (null);
        editorContainer.setVisible (false);
      }

      // set userid from workspace if relevant
      // (will be "" if not set)
      doc.putClientProperty("MAT_userid", getMATWorkspaceUserid());

    } else { // doc == null
      closeAction.setEnabled (false);
      saveAction.setEnabled (false);
      saveAsAction.setEnabled (false);
      exportAction.setEnabled (false);
      //TODO: trainAction.setEnabled (false);
      doneAction.setEnabled(false);
      nextAction.setEnabled(false);
      if (addToocaan) {
        toocaanMenu.setEnabled(false);
      }

      mainComponentSize = mainSplit.getDividerLocation();

      mainContainer.setJawbComponent (imageComponent);
      
      // allow editors to clear themselves before removing
      editorContainer.setJawbDocument (null);
      editorContainer.setJawbComponent (null);
      editorContainer.setVisible (false);
    }

    requestFocus();
    
    if (DEBUG > 1)
      System.err.println ("JF.setJD: changing doc="+(doc==null ? null :
                                                   doc.getDisplayName(false)));

    currentDoc = doc;
    
    updateTitle ();
    updateFontField ();
    updateCharsetField ();
    updateTaskField ();
      
    
    // This will revalidate/repaint itself
    if (mainContainer.getParent() == mainSplit)
      mainSplit.setJawbDocument (doc);
    else
      mainContainer.setJawbDocument(doc);
    
    firePropertyChange(CURRENT_DOCUMENT_KEY, oldDoc, currentDoc);
  }

  /** get the current collection for this JawbFrame */
  public JawbCollection getJawbCollection () {
    if (DEBUG > 0) {
      System.err.println("JF.getJC for " + getJawbDocument() + 
                         " returning " + 
                         docManager.getCollection(getJawbDocument()));
    }
    return docManager.getCollection(getJawbDocument());
  }

  /** set the current collection for this JawbFrame and the document
   * currently open it it 
   */
  public void setJawbCollection (JawbCollection coll) {
    System.err.println("JF.setJC Setting JC for " + getJawbDocument() +
                       " to " + coll);
    docManager.setCollection(getJawbDocument(), coll);
    if (coll != null) {
      doneAction.setEnabled(true);
      nextAction.setEnabled(true);
    } else {
      doneAction.setEnabled(false);
      nextAction.setEnabled(false);
    }      
  }

  /** get the index of the current file within the current collection */
  public int getJawbCollectionIndex () {
    return docManager.getCollectionIndex(getJawbDocument());
  }

  /** set the index of the current file within the current collection */
  public void setJawbCollectionIndex (int index) {
    docManager.setCollectionIndex(getJawbDocument(), index);
  }
  
  /** start the timer for the current document */
  public void startTimer() {
    docManager.startTimer(getJawbDocument());
  }

  public void stopTimer() {
    docManager.stopTimer(getJawbDocument());
  }

  public long getTimeSpent() {
    return docManager.getTimeSpent(getJawbDocument());
  }

  public void setTimeSpent(long millis) {
    docManager.setTimeSpent(getJawbDocument(), millis);
  }

  /**
   * Retrieve the one editor for the Task for this frame. It is cached (by
   * task) and reused for each document using that task. After getting the new
   * instance, add it's actions as property change listeners the preferences
   * so their colors are updated.
   */
  private TaskToolKit getToolKit (Task task) {
    TaskToolKit kit = (TaskToolKit) taskToToolKitMap.get (task);
    if (kit == null) {
      kit = task.getToolKit();

      updateActions(task, kit);
      
      taskToToolKitMap.put (task, kit);
    }
    return kit;
  }

  // RK 8/12/2010 pulled this out from getToolKit so that it can be
  // called explicitly when the context changes
  private void updateActions(Task task, TaskToolKit kit) {
    if (DEBUG > 0)
      System.err.println("JF.updateActions: task=" + task + " toolkit=" + kit);
    // see Task.java javadoc for this.
    String namespace = "task."+task.getName()+".";
    Preferences prefs = Jawb.getPreferences ();
    
    // check actions and keep HIGHLIGHT_COLOR updated by user prefs if
    // HIGHLIGHT_KEY is a highlight key for the task.
    Set highlights = task.getHighlightKeys ();
    if (DEBUG > 4)
      System.err.println("JawbFrame.getTK got highlight keys: " + 
                         highlights);
    Iterator actions = kit.getActions ().iterator ();
    while (actions.hasNext ()) {
      final Action action = (Action) actions.next();
      
      updateActionColors(namespace, prefs, highlights, action);
    }
  }



  private void updateActionColors(String namespace, Preferences prefs, Set highlights, final Action action) {
    String key = (String) action.getValue (JawbAction.HIGHLIGHT_KEY);
    
    if (DEBUG > 1)
      System.err.println ("JF.updateActionColors: action '"+
                          action.getValue(Action.NAME)+"' key="+
                          (key==null?"null":key));

    // is it a highlight pref?
    if (key != null && highlights.contains (key)) {
      // by putting it in defaultProps earlier we can access through
      // prefs and take advantage of user overrides even at startup
      ColorSpec c = prefs.getColorSpec (namespace+key);
      if (c != null) {
        if (DEBUG > 1)
          System.err.println("JF.updateActionColors: got colorspec " +
                             c.toString() + " for " + namespace + key);
        action.putValue (JawbAction.HIGHLIGHT_COLOR, c);
      }
      final String keyS = namespace + key;
      final String colorS = c == null ? "null" : c.toString();
      PropertyChangeListener pcl = new PropertyChangeListener () {
          public void propertyChange (PropertyChangeEvent e) {
            // cast it, just to test value at runtime
            action.putValue (JawbAction.HIGHLIGHT_COLOR,
                             (ColorSpec)e.getNewValue ());
          } };
      
      prefs.addPropertyChangeListener (namespace+key, pcl);
    }
    
    if (action instanceof ActionGroup) {
      Action[] subActions = ((ActionGroup) action).getSubActions();
      for (int i = 0; i < subActions.length; i++) {
        Action act = subActions[i];
        updateActionColors(namespace, prefs, highlights, act);
      }
    }
  }

  /**
   * Retrieve the component used in the main pane. If the TaskToolKit doesn't
   * specify one, creates a new MainTextPane prepared for the task. Only one
   * MainComponent is created per task, per JawbFrame.
   */
  private JawbComponent getMainComponent (Task task) {
    JawbComponent main = (JawbComponent) taskToMainCompMap.get (task);
    AutoSelectCaret.Mode mode = AutoSelectCaret.Mode.WORD;
    try {
      Preferences prefs = Jawb.getPreferences();
      String mName = prefs.getPreference (MainTextPane.SELECTION_MODE_KEY,
                                          mode.toString());
      mode = AutoSelectCaret.Mode.decode (mName);
    } catch (Exception ignore) {}
    
    if (main == null) {
      TaskToolKit kit = getToolKit (task);
      main = kit.getMainComponent ();
      if (main == null)
        main = new MainTextPane (kit);
      taskToMainCompMap.put (task, main);
    }
    if (main instanceof MainTextPane)
      ((MainTextPane)main).setSelectionMode (mode);

    return main;
  }



  public void centerInMainComponent (Task task, int start, int end) {
    MainTextPane mtp = (MainTextPane) getMainComponent(task);
    JTextPane textpane = mtp.getTextPane();

    try {
      Rectangle r = textpane.modelToView(start);
      r.add(textpane.modelToView(end));
      centerInMainComponent(textpane, r);
    } catch (BadLocationException x) {
      /* c'est la vie */ 
      System.err.println("centerInMainComponent: BadLocationException: " + x);
    }
  }


  public void centerInMainComponent (JTextPane textpane, Rectangle r) {

    // centering code from http://www.chka.de/swing/components/scrolling.html
    Rectangle visible = textpane.getVisibleRect();

    visible.x = r.x - (visible.width - r.width) / 2;
    visible.y = r.y - (visible.height - r.height) / 2;

    Rectangle bounds = textpane.getBounds();
    Insets i = textpane.getInsets();
    bounds.x = i.left;
    bounds.y = i.top;
    bounds.width -= i.left + i.right;
    bounds.height -= i.top + i.bottom;

    if (visible.x < bounds.x)
        visible.x = bounds.x;

    if (visible.x + visible.width > bounds.x + bounds.width)
        visible.x = bounds.x + bounds.width - visible.width;

    if (visible.y < bounds.y)
        visible.y = bounds.y;

    if (visible.y + visible.height > bounds.y + bounds.height)
        visible.y = bounds.y + bounds.height - visible.height;

    textpane.scrollRectToVisible(visible);
  }



  /** scrolls the MainComponent to make the given offset visible.
   * will throw a class cast error if the MainComponent is not 
   * a MainTextPane
   */
  public void makeVisibleInMainComponent (Task task, int offset) {
    MainTextPane mtp = (MainTextPane) getMainComponent(task);
    JTextPane textpane = mtp.getTextPane();
    try {
      if (DEBUG > 0)
        System.err.println("makeVisibleInMainComponent: " + offset + 
                           " rect= " + textpane.modelToView(offset));
      textpane.scrollRectToVisible(textpane.modelToView(offset));
    } catch (BadLocationException x) {
      /* c'est la vie */ 
      System.err.println("makeVisibileInMainComponent: BadLocationException: "
                         + x);
    }
  }

  public void scrollMainComponentToBottom (Task task) {
    MainTextPane mtp = (MainTextPane) getMainComponent(task);
    JTextPane textpane = mtp.getTextPane();
    Rectangle r = new Rectangle(0, textpane.getBounds(null).height, 1, 1);
    if (DEBUG > 0)
      System.err.println("jf.scrollMainCompToBottom: " + r);
    textpane.scrollRectToVisible(r);
  }

  public void highlightInMainComponent(Task task, int start, int end) {
    MainTextPane mtp = (MainTextPane) getMainComponent(task);
    mtp.highlightReconciliationSegment(start, end);
  }


  /** scrolls the MainComponent to make the given range between the
   * start and end offsets visible.  will throw a class cast error if
   * the MainComponent is not a MainTextPane
   */
  public void makeVisibleInMainComponent (Task task, int start, int end) {
    MainTextPane mtp = (MainTextPane) getMainComponent(task);
    JTextPane textpane = mtp.getTextPane();

    if (end < 0) {
      // use the end of the document
      Document doc = textpane.getDocument();
      end = doc.getLength();
    }

    try {
      Rectangle r = textpane.modelToView(start);
      if (DEBUG > 0) {
        System.err.println("makeVisible start: " + r);
        System.err.println("makeVisible end: " +
                           textpane.modelToView(end));
      }
      r.add(textpane.modelToView(end));
      if (DEBUG > 0)
        System.err.println("makeVisible rect: " + r);
      textpane.scrollRectToVisible(r);
    } catch (BadLocationException x) {
      /* c'est la vie */ 
      System.err.println("makeVisibileInMainComponent: BadLocationException: "
                         + x);
    }
  }
  
  /***********************************************************************/
  /* Actions on this frame */
  /***********************************************************************/

  /**
   * Create a new document with the specified URI as its signal, annotating
   * with the specified task, reading the signal with the default encoding
   * (UTF-8).  URI must be absolute.  All exceptions causing failure to open
   * the file are caught and displayed to the user.
   *
   * @param uri an absolute URI to open.
   * @param task a task to annotate the file with.
   * @param encoding The name of a supported {@link Charset}. Uses "UTF-8" if
   * encoding is <code>null</code>
   * @param mimeType the MIME type of the signal. Its MIME class is assumed to
   * be "text".
   *
   * @throws InvalidArgumentException if uri is not absolute, or task is null.
   */
  public void openSignal (URI uri, Task task,
                          String encoding, String mimeType) {

    if (! uri.isAbsolute ())
      throw new IllegalArgumentException ("Signal location must be absolute");
    if (task == null)
      throw new IllegalArgumentException ("Unspecified task.");
    if (encoding == null)
      encoding = "UTF-8";
 
    // see if requested file is already open
    // TODO: should accept Task, so users can annotate same document w/
    // multiple tasks simultaneously
    JawbDocument doc = docManager.find (uri);

    if (doc != null) {
      // display it in this frame and possibly revert to saved version
      setCurrentDocument (doc);
      verifyRevert (doc);
      return;
    }

    try {
      if (DEBUG > 2)
        System.err.println ("JF.openSig: task="+task.getName());
      
      doc = JawbDocument.fromSignal (uri, task, mimeType, encoding);
      docManager.add (doc);
      
      //success! Switch to it!
      if (DEBUG > 0)
        System.err.println ("JF.openSig: changing current document");
      setCurrentDocument (doc);
      
    } catch (Exception e) {
      e.printStackTrace ();
      GUIUtils.showError ("Error Loading File:\n"+e.getMessage()+
                          "\n"+new File(uri).toString());
    }
  }

  /***********************************************************************/
  /***********************************************************************/
  /***********************************************************************/
  /***********************************************************************/
  /***********************************************************************/
  
  /**
   * Open an AIF file specified by URI. URI must be absolute. The AIF is
   * quicly parsed for the MAIA Scheme URL, and the corresponding Task is
   * used. All exceptions causing failure to open the file are caught and
   * displayed to the user as error messages.
   *
   * @param uri an absolute URI to open.
   */
  public void openAIF (URI uri) {
    
    // see if requested file is already open
    JawbDocument doc = docManager.find (uri);

    if (doc != null) {
      // display it in this frame.
      setCurrentDocument (doc);
      verifyRevert (doc);
      return;
    }
    
    try {
      List supporting = null;
      try {
        supporting = ATLASHelper.getSupportingTasks (uri);
      } catch (Exception e) {
        // fall through to supporting == null
      }
      
      Task task = null;

      if (supporting == null) {
        String error = new File(uri).toString() + "\nis not a valid AIF file";
        GUIUtils.showError (error);
        return;
        
      } else if (supporting.isEmpty ()) {
        // but there's no available task!
        String error = new File(uri).toString() +
          "\nwas created with an unknown task, unable to open";
        GUIUtils.showError (error);
        return;
        
      } else if (supporting.size () == 1) {
        task = (Task)supporting.get (0);
        
      } else if (supporting.size () > 1) {
        /*****
        StringBuffer warning = new StringBuffer (new File(uri).toString());
        warning.append ("\nis consistent with multiple tasks: ");
        
        Iterator iter = supporting.iterator ();
        while (iter.hasNext ())
          warning.append ("\n * ").append ((Task)iter.next());
        
        warning.append ("\nOpening with first: ");
        warning.append ((Task)supporting.get (0));
        GUIUtils.showWarning (warning.toString ());
        ******/
        StringBuffer info = new StringBuffer (new File(uri).toString());
        info.append ("\nis consistent with multiple tasks.");
        info.append ("\nChoose the task to use to open this file:");
        task = (Task)JOptionPane.showInputDialog(this, info, "Choose Task", 
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null, supporting.toArray(),
                                                 supporting.get(0));
        if (task == null)
          return;                                    
      }


      doc = JawbDocument.fromAIF (uri, task);
      docManager.add (doc);

      //success! Switch to it!
      if (DEBUG > 0)
        System.err.println ("JF.openAIF: changing current document");
      setCurrentDocument (doc);
      
    } catch (Exception e) {
      e.printStackTrace ();
      GUIUtils.showError ("Error Loading File:\n"+e.getMessage()+
                          "\n"+new File(uri).toString());
    }
  }
  
  /**
   * Open and convert a document from an external format. URI must be
   * absolute. All exceptions causing failure to save the file are caught and
   * displayed to the user as error messages.
   *
   * @param uri an absolute URI to import
   * @param encoding The name of a supported {@link Charset}. Uses "UTF-8" if
   * encoding is <code>null</code>
   * @param importer means of importing
   */
  public void importDocument (URI uri, String encoding, Importer importer) {
    
    // see if requested file is already open
    JawbDocument doc = docManager.find (uri);

    if (doc != null) {
      // display it in this frame.
      setCurrentDocument (doc);
      verifyRevert (doc);
      return;
    }
    try {
      doc = JawbDocument.importDocument(uri, importer, encoding);
      docManager.add (doc);

      //success! Switch to it!
      if (DEBUG > 0)
        System.err.println ("JF.openAIF: changing current document");
      setCurrentDocument (doc);
      
    } catch (IllegalArgumentException e) {
      String uriString = (uri.getScheme().equals("file") ?
                          new File(uri).toString() :
                          uri.toString());
      e.printStackTrace ();
      GUIUtils.showError ("Could not import File:\n"+e.getMessage()+
                          "\n"+uriString);
      
    } catch (Exception e) {
      e.printStackTrace ();
      String uriString = (uri.getScheme().equals("file") ?
          new File(uri).toString() :
          uri.toString());
      GUIUtils.showError ("Error Importing File:\n"+e.getMessage()+
                          "\n"+uriString);
    }
  }    
  
  public void importSelectedWorkspaceDoc(WorkspaceDashboard dash,
                                         String basename, String folder) {
    JawbDocument jd = dash.importSelectedWorkspaceDoc(basename, folder);
    if (jd == null) {
      GUIUtils.showMessage("Unable to open " + basename + " in " + folder);
      return;
    }
    // when the JD is created, the current priority is set automatically
    // in this case we want to override it, and put this doc into "bootstrap"
    // mode which ignores the segment priorities
    // setting the USER_SELECTED property means that if the high priority
    // key is ever recomputed, it will automatically get re-set to Bootstrap 
    if (DEBUG > 0)
      System.err.println("JF.impSelWSDoc setting mode to: " +
                         ToocaanTask.USER_SELECTED_MODE );
    /** now mode is set when importSelectedWorkspaceDoc is called
    jd.putClientProperty(ToocaanTask.DOCMODE_KEY,
                         ToocaanTask.USER_SELECTED_MODE);
    ***/
    // log it before calling the finish method
    Jawb.getLogger().info(JawbLogger.LOG_WORKSPACE_IMPORT_SEL, 
                          new Object [] { folder, basename });
    finishImport(dash, folder, jd, basename);
  }

  public void importNextWorkspaceDoc(WorkspaceDashboard dash,
                                     String folder) {
    JawbDocument jd = dash.importNextWorkspaceDoc(folder);
    if (jd == null) {
      GUIUtils.showMessage("Nothing to do for user " + 
      dash.getMATWorkspaceUserid());
      return;
    }
    // get new basename for logging
   String basename = dash.getMATDocBasename();
    Jawb.getLogger().info(JawbLogger.LOG_WORKSPACE_IMPORT_NEXT, 
                          new Object [] { folder, basename });
    // log it before calling the finish method
    finishImport(dash, folder, jd, basename);
  }

  private void finishImport(WorkspaceDashboard dash, String folder, 
                            JawbDocument jd, String basename) {
    // set the basename in the JawbDocument
    jd.setBasename(basename);

    ToocaanTask task = (ToocaanTask)jd.getTask();      
    docManager.add(jd);
    System.err.println("docManager added " + dash.getMATDocBasename());
    setCurrentDocument (jd);
    updateWSMenuOptions();

    // TODO fix this using new info about what phase we are really in
    if (folder.equals("reconciliation")) {
      // set this as the "main" reconciliation JawbDocument
      jd.putClientProperty(ToocaanTask.RECON_KEY, "main");
      // open adjudication document and dialog
      AdjudicationDocument adjDoc = 
        new AdjudicationDocument(jd, task, this);
      reviewAction.setEnabled(true);
      // hardcode for now (TODO use status returned by WS)
      dash.setMATDocStatus("human_vote");
    } else {
      reviewAction.setEnabled(false);
      // for main folder only, find current high priority and scroll
      // to first high priority segment (or first segment if bootstrapping)
      // String priority = task.getCurrentPriority(jd);
      ToocaanToolKit tk = (ToocaanToolKit)getToolKit(task);
      PhraseTaggingAnnotation firstSeg = 
        tk.findNextHighPrioritySegment(jd, 0);
      // firstseg may legitimately be null if a user opens a document
      // where all segments are already complete

      if (firstSeg != null) {
        int firstStart = firstSeg.getTextExtentStart();
        int firstEnd = firstSeg.getTextExtentEnd();
        String mode = jd.getMode();
        if (mode.equals(ToocaanTask.BOOTSTRAP_MODE) ||
            mode.equals(ToocaanTask.DOC_PRIORITIZATION_MODE) ||
            mode.equals(ToocaanTask.USER_SELECTED_MODE)) {
          // center just the beginning of the "segment" (annotatable
          // portion of the whole document) by setting firstEnd to firstStrat
          firstEnd = firstStart;
        }

        if (DEBUG > 2)
          System.err.println("First high priority segment from: " +  
                             firstStart + " to: " + 
                             firstEnd + " content: " + 
                             firstSeg.getTextExtent());
        // I have no idea why the make visible for the first segment
        // doesn't work unless I first make visible a lower rectangle,
        // but this is the current work-around
        // makeVisibleInMainComponent(task, firstEnd, -1);
        // above way is no good if firstEnd is the end of the doc
        /**
        makeVisibleInMainComponent(task, firstEnd-1, firstEnd);
        makeVisibleInMainComponent(task, firstStart, firstEnd);
        **/
        // first make the end of the document visible, then scroll back to the
        // hightlighted segment
        final Task theTask = task;
        final int showStart = firstStart;
        final int showEnd   = firstEnd;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              /******
              scrollMainComponentToBottom(theTask);
              makeVisibleInMainComponent(theTask, showEnd);
              makeVisibleInMainComponent(theTask, showStart);
              *********/
              centerInMainComponent(theTask, showStart, showEnd);
            }
          });
        
      }
    }
  }

  /**
   * Revert a file to it's last saved state.?
  public void revert (JawbDocument doc) {
    doc.revert ();
  }
   */

  private void verifyRevert (JawbDocument doc) {
    // if modified since opened, query before reverting
    if (doc.isDirty ()) {
      String message = doc +" has been modified\n"+
        "Do you want to revert to previous saved state?";
      int revert =
        JOptionPane.showConfirmDialog(this, message,
                                      "Revert Contents?",
                                      JOptionPane.YES_NO_OPTION,
                                      JOptionPane.QUESTION_MESSAGE);
      if (revert == JOptionPane.YES_OPTION)
        doc.revert ();
    }
  }

  /**
   * Save a file, prompting for location if unknown.
   * @param doc the document to save.
   * @param saveAs true forces selection of a new file to save into
   */
  public URI save (JawbDocument doc, boolean saveAs) {
    return save (doc, saveAs, false, null);
  }

  /**
   * Save a file, prompting for location if unknown.
   * @param doc the document to save.
   * @param saveAs true forces selection of a new file to save into
   * @param export true shows the export dialog, not the standard 'save'.
   */
  public URI save (JawbDocument doc, boolean saveAs, boolean export) {
    return save (doc, saveAs, export, null);
  }

  
  /**
   * Save a file, prompting for location if unknown.
   * @param doc the document to save.
   * @param saveAs true forces selection of a new file to save into
   * @param export true shows the export dialog, not the standard 'save'.
   * @param saveTo the URI that the file should be saved to if 
   *               doc.getAtlasURI() returns null -- if null use existing 
   *               methods to decide
   */
  // SAM 2/6/05: In various places, we really need to know what name
  // it was saved under, but that information is buried. I'm changing this
  // function to return it.
  // RK 4/26/07: added the saveTo parameter for use by the collection 
  // facility to allow it to force a save to a certain URI without 
  // prompting the user, even the first time.  (can be null to use the
  // usual methods to get the URI)
    public URI save (JawbDocument doc, boolean saveAs, boolean export,
                     URI saveTo) {

    // before saving or exporting, validate if any auto-validators are
    // available for this task
    if (!autoValidate(doc))
      return null;
    

    URI uri = doc.getAtlasURI ();
    if (uri == null) {
      uri = saveTo;
    }
    
    if (saveAs || uri == null) {
      File file = null;
      boolean found = false;
      while (! found) {
        if (export) {
          // for export, we pre-fill with last external name, if it exists
          uri = doc.getExternalURI();
          GUIUtils.getExportAccessory ().setTask (doc.getTask ());
          // TODO: we should prefill with the old import name with
          // an extension: is that part of an api?
          file = GUIUtils.chooseFile (this, uri, GUIUtils.EXPORT_DIALOG);
        } else {
          // for save-as, we pre-fill with old ATLAS name, or a default
          if (uri == null)
            uri = URI.create (doc.getSignalURI().toString() + ".aif.xml");
          file = GUIUtils.chooseFile (this, uri, GUIUtils.SAVE_DIALOG);
        }

        if (file == null) // canceled or escaped
          return null;

        if (file.exists ()) {
          if (! file.canWrite()) // try again...
            GUIUtils.showMessage ("File is not writeable\n"+file);
          else {
            String message = file + " already exists.\n" +
              "Do you want to replace it?";
            int kill=JOptionPane.showConfirmDialog(this, message,
                                                   "Replace File?",
                                                   JOptionPane.YES_NO_OPTION,
                                                   JOptionPane.QUESTION_MESSAGE);
            if (kill == JOptionPane.YES_OPTION) // whatever you say...
              found = true;
          }
        } else { // file != esists
          File dir = file.getParentFile ();
          if (! dir.exists())
            GUIUtils.showMessage ("Path does not exist\n" + dir.getPath ());
          else if (! dir.canWrite ())
            GUIUtils.showMessage ("Cannot create file in\n" + dir.getPath ());
          else // create new file!
            found = true;
        }
      }
      uri = file.toURI ();
    }

    String message = null;
    try {
      AnnotationModel annots = doc.getAnnotationModel();
      boolean saved = false;
      if (export) {
        // let the doc update it's state
        saved = doc.export (GUIUtils.getSelectedExporter(), uri);
      } else {
        saved = doc.save (uri);
      }
                       
      if (saved) {
        setStatus ((export?"Exported ":"Saved ") + new File(uri));
        return uri;
      }
      /* fall through to showError */
    } catch (Throwable e) {
      Throwable x = e.getCause ();
      if (x == null) x = e;
      message = x.getMessage ();
      x.printStackTrace ();
    }
    GUIUtils.showError ("Error "+(export?"Exporting":"Saving")+":\n"+
                        new File(uri)+(message==null?"":"\n"+message));
    return null;
  }

  /**
   * Executes all of the autoValidators defined for the given
   * JawbDocument's Task.  Returns true if:
   *   - there are no auto-validators defined for this task, or
   *   - all of the auto-validators returned a status of SUCCESS, or
   *   - some auto-validators returned another status, but the user
   *   chooses to save the file anyway.
   */  
  private boolean autoValidate (JawbDocument doc) {
    Task task = doc.getTask();
    AWBDocument awbDoc = (AWBDocument)doc.getAnnotationModel();
    Validator [] validators = task.getAutoValidators();
    if (validators == null)
      return true;

    boolean allSuccess = true;
    StringBuffer message = new StringBuffer();
    for (int i=0; i < validators.length; i++) {
      message.append(validators[i].getFormat());
      message.append(":\n");
      ValidationResult result = validators[i].validateDocument(awbDoc);
      int status = result.getStatus();
      if (status == ValidationResult.SUCCESS) {
        message.append(result.getSuccessMessage());
        message.append("\n\n");
      } else {
        allSuccess = false;
        if (status == ValidationResult.FAILURE) {
          message.append(result.getFailureMessage());
          message.append("\n\n");
        }
      }
    }

    if (allSuccess)
      return true;

    // one or more validation failed -- show the user the concatenated
    // result message, and ask if they want to save anyhow. 
    JawbValidationDialog d = new JawbValidationDialog(this, message.toString());

    return d.getSaveResult();
  }

          
  /**
   * Close a specified file, verifying close with user only if specified
   * @param doc the document to close
   * @return true if the document was closed (or even if the close was
   * attempted and failed), false if the closing was canceled.
   */
  public boolean close (JawbDocument doc) {
    return close(doc, null);
  }

  /**
   * Close a specified file, verifying close with user only if specified
   * @param doc the document to close
   * @return true if the document was closed (or even if the close was
   * attempted and failed), false if the closing was canceled.
   */
  public boolean close (JawbDocument doc, WorkspaceDashboard dash) {
    if (DEBUG > 1)
      System.err.println ("JF["+this+"].close("+(doc==null?null:doc.getName())+")");

    if (doc == null)
      return false;

    boolean continuousSaver = doc.getTask().savesContinuously();

    if (!continuousSaver && !verifyClose (doc, dash))
      return false;

    reallyClose (doc);
    return true;
  }

  /**
   * This method actually closes the document and informs the document
   * server, without doing any checking.  This method should only be
   * called by a method that does the relevant confirmation, such as
   * the close method above.  The task's documentClosing and
   * documentClosed methods are called immediately before and after
   * doing the actual close.
   * @param doc the document to close
   */
  public void reallyClose (JawbDocument doc) {
    String docName = doc.getName();
    String displayName = doc.getDisplayName(false);
    
    Task task = doc.getTask();
    System.err.println("JF.reallyClose(" + docName + ") called documentClosing(" + this.toString() +")");
    if (!task.documentClosing(this)) {
      // user cancelled close
      return;
    }

    // inform the doc manager of the documents closing
    try { // God forbid the catch actually gets used. How to handle?
      doc.close ();
    } catch (Exception e) {
      GUIUtils.showError ("Error Closing "+docName+"\n"+e.getMessage ());
    }
    task.documentClosed(this);

    try { // don't merge w/ above try/catch
      docManager.remove (doc);
    } catch (Exception e) { /* don't care for now! */ }

    // notify all frames (including this) of close, so document may be
    // switched if necessary
    Iterator iter = frameList.iterator ();
    while (iter.hasNext ())
      ((JawbFrame) iter.next()).closed (doc);
    // inv: doc, is last remaining reference to 'doc' in JVM
    
    // note: 'closed' has already been called on this frame, so we're done!
    setStatus ("Closed "+displayName);

    Jawb.getLogger().info(JawbLogger.LOG_REALLY_CLOSE, 
                          new Object [] { displayName });
    if (workspaceDash != null)
      workspaceDash.uploadLog();

    return;
  }

  /**
   * Verify that it is ok to close the document.  If the file is dirty, the
   * user is prompted to save. True is returned if the user agrees to save or
   * not, but false is returned if the user cancels.
   */
  private boolean verifyClose (JawbDocument doc) {
    return verifyClose(doc, null);
  }

  /**
   * Verify that it is ok to close the document.  If the file is dirty, the
   * user is prompted to save. True is returned if the user agrees to save or
   * not, but false is returned if the user cancels.
   * If dash is non-null, this is a workspace document -- if the user 
   * declines to save, we need to release the lock here anyhow
   *
   * If any loggable actions happen here (release_lock, save, etc.) they
   * must be logged
   */
  private boolean verifyClose (JawbDocument doc, WorkspaceDashboard dash) {
    if (DEBUG > 0)
      System.err.println ("JF["+this+"].verifyClose("+
                          (doc==null?null:doc.getName())+","+dash+")");
    if (! doc.isDirty ()) {
      // even if not dirty, if it's a ws doc need to release the lock
      if (dash != null) {
        String lockId = dash.getMATLockId();
        if (DEBUG > 0)
          System.err.println("non-dirty WS doc -- releasing lock");
        dash.releaseLock();
        Jawb.getLogger().info(JawbLogger.LOG_RELEASE_LOCK, 
                              new Object[] {lockId});
      }
      return true;  // not dirty so close is automatically verified
    }
    
    String message = doc.getDisplayName(false) +
      " has been modified,\nWould you like to save it?";
    
    int optionValue =
      JOptionPane.showConfirmDialog(this, message, "Save File?", 
                                   JOptionPane.YES_NO_CANCEL_OPTION);
    Jawb.getLogger().info(JawbLogger.LOG_DIALOG_CHOICE,
                          new Object[] {"SaveModifiedQuery", 
                                        new Integer(optionValue)});

    if (optionValue == JOptionPane.YES_OPTION) {
      String p = doc == null ? null : doc.getPath();
      boolean result = false;
      if (dash != null) {
        String lockId = dash.getMATLockId();
        String basename = dash.getMATDocBasename();
        // use workspace close operation
        // relase the lock, but do not mark as gold
        result = dash.saveWorkspaceDocument(true, false);
        Jawb.getLogger().info(JawbLogger.LOG_RELEASE_LOCK, 
                              new Object[] {lockId});
        Jawb.getLogger().info(JawbLogger.LOG_WORKSPACE_SAVE, 
                        new Object [] { basename });
      } else {
        // Don't prompt unless necessary, and
        // allow user cancel when choosing file
        URI uri = save (doc, false);
        result = uri != null; 
        Jawb.getLogger().info(JawbLogger.LOG_SAVE, new Object [] {uri});
      }
      return result;
    }
    if (optionValue == JOptionPane.NO_OPTION) {
      // for WS documents, release the lock even if we're not saving
      Jawb.getLogger().info(JawbLogger.LOG_SAVE_DECLINED);
      if (dash != null) {
        String lockId = dash.getMATLockId();
        dash.releaseLock();
        Jawb.getLogger().info(JawbLogger.LOG_RELEASE_LOCK, 
                              new Object[] {lockId});
      }
      return true; // user chooses to close, but not save
    }

    //else CANCEL_OPTION || CLOSE_OPTION
    return false;
  }

  /**
   * Forget about a file, and if it's currently displayed, display
   * another. Internal method called on each frame when any document is
   * closed.
   * @param doc must be non-null
   */
  private void closed (JawbDocument doc) {
    if (DEBUG > 1)
      System.err.println ("JF["+this+"].closed("+(doc==null?null:doc.getName())+")");
    
    if (doc == lastDoc)
      setLastDoc (null);
    if (doc == currentDoc)
      setCurrentDocument (lastDoc);
  }

  /**
   * Set the lastDoc to the one specified, unless it's already been closed, in
  which case ask the documentManager for another.
  */
  private void setLastDoc (JawbDocument doc) {
    // remember as 'last' unless already closed or null
    if (docManager.contains (doc)) {
      lastDoc = doc;
      return;
    }
    lastDoc = null;
    Iterator docIter = docManager.documents().iterator ();
    while (docIter.hasNext()) {
      JawbDocument d = (JawbDocument) docIter.next ();
      if (d != currentDoc && d != doc) {
        lastDoc = d;
        break;
      }
    }
  }

  
  /**
   * Should only be called if this is the last frame standing.
   */
  public boolean closeAll () {
    if (DEBUG > 1)
      System.err.println ("JF["+this+"].closeAll");
    while (currentDoc != null) {
      if (! close (currentDoc, workspaceDash))
        return false;
    }
    return true;
  }
  
  public boolean closeFrame () {
    if (DEBUG > 1)
      System.err.println ("JF["+this+"].closeFrame");
    // if it's the last frame to go, close all files
    if (frameList.size () == 1) {
      // user may cancel here (if any are left)
      if (! closeAll ())
        return false;
    }

    docManager.releaseHistoryMenu (historyMenu);
    docManager.releaseDocumentMenu (documentMenu);
    
    Jawb.getPreferences ().removePropertyChangeListener (docAndPrefsListener);
    GUIUtils.storeGeometry (this, "jawb");

    frameList.remove (this);
    dispose ();

    if (frameList.isEmpty ()) { 
      Jawb.storePreferences ();
      System.exit (0);
    } else {
      Jawb.getAction ("closeFrame").setEnabled (frameList.size () > 1);
    }
    return true;
  }

  /**
   * Close all files (verifying each with the user), then all frames, then
   * exit
   */
  public void exit () {
    // any uncaught exceptions here will prevent exits entirely, so handle
    // falures here as though the simply force complete exit.
    try {
      // on exit, close all files first, bail if user cancels
      if (! closeAll ())
        return;
      
      // close all frames, avoiding concurrent modifications w/ copy
      List frames = new LinkedList (frameList);
      Iterator iter = frames.iterator ();
      while (iter.hasNext())
        ((JawbFrame)iter.next()).closeFrame();
    } catch (Throwable e) {
      System.err.println ("Throwable caught during exit! Exiting in disgrace.");
      e.printStackTrace ();
      System.exit (0);
    }
  }

  /**
   * Display preferences dialog.
   */
  public void showPreferences () {
    // force it to update?
    prefsDialog.setVisible (true);
  }

  /**
   * Returns the PreferenceDialog.  This is needed to allow tasks to
   * update it when their tag set changes.
   */
  public PreferenceDialog getPreferenceDialog () {
    return prefsDialog;
  }

  /***********************************************************************/
  /* PROPERTY CHANGE NOTIFICATION */
  /***********************************************************************/

  /**
   * When this action's document's ATLAS URI changes, change the name in *
   * the menu.
   */
  private class DocAndPrefsListener implements PropertyChangeListener {
    public void propertyChange (PropertyChangeEvent e) {
      Object source = e.getSource ();
      String name = e.getPropertyName ();
      
      if (DEBUG > 2)
        System.err.println ("JF.propChange: name="+name+" src="+source);

      if (source == currentDoc) {
        if (name == null ||
            name.equals (JawbDocument.DIRTY_FLAG_KEY) ||
            name.equals (JawbDocument.ATLAS_URI_KEY)) {
          updateTitle ();
          saveAction.setEnabled (currentDoc.isDirty());
          
        } else if (name.equals (JawbDocument.FONT_FAMILY_PROPERTY_KEY) ||
                   name.equals (JawbDocument.FONT_SIZE_PROPERTY_KEY)) {
          updateFontField ();
            
        } else if (name.equals (JawbDocument.ENCODING_PROPERTY_KEY)) {
          updateCharsetField ();
        }
      } else if (source instanceof Preferences) {
        if (Preferences.SHOW_FULL_PATH_KEY.equals (name))
          updateTitle ();
      }
    }
  }
  
  /***********************************************************************/
  /* JawbDocument CHANGE NOTIFICATION */
  /***********************************************************************/

  /* It wasn't being used so I commented it out!
  private class AnnotListener implements AnnotationModelListener {
    /** Invoked after an annotation has been created. 
    public void annotationCreated (AnnotationModelEvent e) {/* empty }
    /** Invoked after an annotation has been deleted. 
    public void annotationDeleted (AnnotationModelEvent e) {/* empty }
    /** Invoked after an annotation has been changed. 
    public void annotationChanged (AnnotationModelEvent e) {/* empty }
    /** Invoked after an annotation has had subannotations added. 
    public void annotationInserted (AnnotationModelEvent e) {/* empty }
    /** Invoked after an annotation has had subannotations removed. 
    public void annotationRemoved (AnnotationModelEvent e) {/* empty }
  }
  */
  
  /***********************************************************************/
  /* UTILITY FUNCTIONS */
  /***********************************************************************/

  /**
   * Shows the wait cursor.
   */
  public void showWaitCursor (boolean show) {
    if (show) {
      if (waitCount++ == 0) {
        Cursor cursor = Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR);
        getGlassPane ().setCursor (cursor);
        //mainContainer.setCursor (cursor);
      }

    } else {
      if (waitCount > 0)
        waitCount--;
      
      if (waitCount == 0) {
        Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        getGlassPane ().setCursor(cursor);
        //cursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
        //JawbTextArea[] textAreas = getTextAreas();
        
        //for(int i = 0; i < textAreas.length; i++)
        //  textAreas[i].getPainter().setCursor(cursor);
      }
    }
  }
  
  /***********************************************************************/
  /* Non global actions */
  /***********************************************************************/

  /** Change the Font size */
  private static class FontSizeAction extends JawbAction {
    Integer size;
    // only needed for size= null (ie. custom)
    JPanel inputPanel;
    JTextField valueField;
    Runnable focusRequester;
    FontSizeAction (Integer size) {
      super ("");
      this.size = size;
      
      if (size != null) {
        putValue (NAME, size.toString());
        
      } else {
        putValue (NAME, "Other ...");

        valueField = new JTextField (3);
        valueField.setDocument (new IntegerDocument ());
        // hahaha! Cheat to force the value field to get focus even though it's
        // used in a JOptionPane dialog, where we never get access to it once
        // it's in the component heirarchy!
        focusRequester = new Runnable () {
            public void run () {
              valueField.selectAll();
              valueField.requestFocus();
            }
          };
        valueField.addAncestorListener (new AncestorListener () {
            public void ancestorAdded (AncestorEvent e){
              SwingUtilities.invokeLater (focusRequester);
            }
            public void ancestorMoved (AncestorEvent e){};
            public void ancestorRemoved (AncestorEvent e){};
          });

        inputPanel = new JPanel (new BorderLayout());
        inputPanel.add (valueField, BorderLayout.CENTER);
        inputPanel.add (new JLabel ("Specify new font size"),
                        BorderLayout.NORTH);
      }
    }
    public void actionPerformed (ActionEvent e) {
      JawbDocument doc = getJawbDocument (e);
      if (doc != null) {
        if (size != null) {
          doc.setFontSize (size.intValue());

        } else {
          // request size input
          valueField.setText (Integer.toString(doc.getFontSize()));
          int result =
            JOptionPane.showConfirmDialog (GUIUtils.getJawbFrame(e),
                                           inputPanel, "Font Size",
                                           JOptionPane.OK_CANCEL_OPTION,
                                           JOptionPane.PLAIN_MESSAGE);
          if (result == JOptionPane.OK_OPTION) {
            int size = Integer.parseInt (valueField.getText());
            doc.setFontSize (size);
          }
        }
      }
    }
  }

  /** Change the Font family */
  private static class FontAction extends JawbAction {
    String family;
    FontAction (String family) {
      super (family);
      this.family = family;
    }
    public void actionPerformed (ActionEvent e) {
      JawbDocument doc = getJawbDocument (e);
      if (doc != null)
        doc.setFontFamily (family);
    }
  }

  /** Auto detect and change the font in one fell swoop */
  private static class FontDetectAction extends JawbAction {
    FontDetectAction () {
      super ("Auto-Detect Font");
    }
    public void actionPerformed (ActionEvent e) {
      JawbDocument doc = getJawbDocument (e);
      if (doc != null)
        doc.autoDetectFontAndOrientation ();
    }
  }

  /** Change the Font size */
  private static class LineSpacingAction extends JawbAction {
    // *wince*.. did I really just copy this from FontSizeAction?
    //       Stop! ... Refactor Time! (it must be late)
    Float size;
    // only needed for size= null (ie. custom)
    JPanel inputPanel;
    JTextField valueField;
    Runnable focusRequester;
    LineSpacingAction (Float size) {
      super ("");
      this.size = size;
      
      if (size != null) {
        putValue (NAME, size.toString());
        
      } else {
        putValue (NAME, "Other ...");

        valueField = new JTextField (3);
        valueField.setDocument (new DecimalDocument ());
        // hahaha! Cheat to force the value field to get focus even though it's
        // used in a JOptionPane dialog, where we never get access to it once
        // it's in the component heirarchy!
        focusRequester = new Runnable () {
            public void run () {
              valueField.selectAll();
              valueField.requestFocus();
            }
          };
        valueField.addAncestorListener (new AncestorListener () {
            public void ancestorAdded (AncestorEvent e){
              SwingUtilities.invokeLater (focusRequester);
            }
            public void ancestorMoved (AncestorEvent e){};
            public void ancestorRemoved (AncestorEvent e){};
          });

        inputPanel = new JPanel (new BorderLayout());
        inputPanel.add (valueField, BorderLayout.CENTER);
        inputPanel.add (new JLabel ("Specify new line spacing"),
                        BorderLayout.NORTH);
      }
    }
    public void actionPerformed (ActionEvent e) {
      JawbDocument doc = getJawbDocument (e);
      if (doc != null) {
        if (size != null) {
          doc.setLineSpacing (size.floatValue()-1f);

        } else {
          // request size input
          valueField.setText (Integer.toString(doc.getFontSize()));
          int result =
            JOptionPane.showConfirmDialog (GUIUtils.getJawbFrame(e),
                                           inputPanel, "Line Spacing",
                                           JOptionPane.OK_CANCEL_OPTION,
                                           JOptionPane.PLAIN_MESSAGE);
          if (result == JOptionPane.OK_OPTION) {
            float size = Float.parseFloat (valueField.getText());
            if (size >= 1f)
              doc.setLineSpacing (size-1f);
            else
              GUIUtils.beep();
          }
        }
      }
    }
  }

  /** Change the encoding used to read the file */
  private static class SetEncodingAction extends JawbAction {
    String encoding;
    SetEncodingAction (String encoding) {
      super (encoding);
      this.encoding = encoding;
    }
    public void actionPerformed (ActionEvent e) {
      JawbDocument doc = getJawbDocument (e);
      if (doc != null) {
        try {
          doc.setEncoding (encoding);
        } catch (IOException x) {// not concerned.. choices are limited by gui
        }
      }
    }
  }

  /** Change selection mode */
  private static class SelectModeRadioButton extends JRadioButtonMenuItem
    implements PropertyChangeListener {
    
    AutoSelectCaret.Mode myMode;
    SelectModeRadioButton (AutoSelectCaret.Mode mode) {
      super ();
      myMode = mode;
      setAction (new AbstractAction (mode.toString ()) {
          public void actionPerformed (ActionEvent e) {
            Preferences prefs = Jawb.getPreferences();
            prefs.setPreference (MainTextPane.SELECTION_MODE_KEY,
                                 myMode.toString());
          }
        });
      Preferences prefs = Jawb.getPreferences();
      prefs.addPropertyChangeListener (MainTextPane.SELECTION_MODE_KEY, this);
    }
    public void propertyChange (PropertyChangeEvent e) {
      if (myMode.toString().equals (e.getNewValue ())) {
        setSelected (true);
      }
    }
  }

  /** Toggles the ComponentOrientation of a component. */
  private static class ToggleComponentOrientationAction extends JawbAction {
    ToggleComponentOrientationAction() {
      super("Toggle Orientation");
    }
    public void actionPerformed(ActionEvent e) {
      JawbDocument doc = getJawbDocument (e);
      if (doc != null) {
        ComponentOrientation next;
        if (doc.getComponentOrientation().isLeftToRight ())
          next = ComponentOrientation.RIGHT_TO_LEFT;
        else
          next = ComponentOrientation.LEFT_TO_RIGHT;
        doc.setComponentOrientation (next);
      }
    }
  }

  private static class ReferenceEditorAction extends JawbAction {
    ReferenceEditorAction () {
      super ("AIF Reference Editor");
      putValue (Action.MNEMONIC_KEY, new Integer ('A'));
    }
    public void actionPerformed (ActionEvent e) {
      if (referenceEditor == null)
        referenceEditor = new ReferenceEditor ();
      
      JawbDocument doc = getJawbDocument (e);
      JawbFrame jf = getJawbFrame (e);
      if (doc != null && doc.getAtlasURI () != null)
        referenceEditor.open (doc.getAtlasURI ());
      referenceEditor.showFrame (jf);
    }
  }
  
  private static class DTDCompilerAction extends JawbAction {
    DTDCompilerAction () {
      super ("DTD Task Compiler");
      putValue (Action.MNEMONIC_KEY, new Integer ('D'));
    }
    public void actionPerformed (ActionEvent e) {
      if (dtdTaskPanel == null)
        dtdTaskPanel = new GenericTaskPanel();
      dtdTaskPanel.showFrame(getJawbFrame(e));
    }
  }
}

