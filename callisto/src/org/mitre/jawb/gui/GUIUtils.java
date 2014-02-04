
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
import org.mitre.jawb.JawbLogger;
import org.mitre.jawb.io.URLUtils;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.Importer;
import org.mitre.jawb.tasks.Exporter;
import org.mitre.jawb.tasks.preannotate.*;


/**
 * Contains a bunch of methods common to gui components
 */
public final class GUIUtils {

    /** Specifies a dialog to open signal files */
    public static final int OPEN_SIGNAL_DIALOG = 0;
    /** Specifies a dialog to open annotation files */
    public static final int OPEN_ANNOTATION_DIALOG = 1;
    /** Specifies a dialog to save AIF files */
    public static final int SAVE_DIALOG = 2;
    /** Specifies a dialog to import annotation files from other format */
    public static final int IMPORT_DIALOG = 3;
    /** Specifies a dialog to export files to another format */
    public static final int EXPORT_DIALOG = 4;
    /** Specifies a dialog to save a collection file */
    public static final int SAVE_COLLECTION_DIALOG = 5;
    /** Specifies a dialog to open a collection file */
    public static final int OPEN_COLLECTION_DIALOG = 6;
    /** Specifies a dialog to open workspace */
    public static final int OPEN_WORKSPACE_DIALOG = 7;
  

    /**
     * Key to store a reference to a JComponent's JawbFrame in it's client
     * properties. Needed because certain GUI components may be separated as
     * floating windows, and thus the recursive 'getParent()' trick cannot be
     * used.
     */
    public static final Object JAWB_FRAME_KEY = "Jawb Frame";
  
    private static JFileChooser fileChooser = null;
    private static FileFilter aifFilter = null;
    private static FileFilter sgmlFilter = null;
    private static FileFilter htmlFilter = null;
    private static FileFilter plainTextFilter = null;
    private static FileFilter textFilter = null;
    private static ImportAccessory importAccessory;
    private static ExportAccessory exportAccessory;
    private static TaskAccessory taskAccessory;
    private static WorkspaceAccessory workspaceAccessory;
  
  /** Encoding last selected with encodingAccessory from open or import */
  private static String selectedEncoding = "UTF-8";
  /** MIME type last selected with mimeType accessory from open or import */
  private static String selectedMIMEType = "plain";

    /***********************************************************************/
    /* UTILITY */
    /***********************************************************************/

    /**
     * Converts a typical URL to URI, typical meaning it does not have properly
     * quoted spaces and other escape characters.
     *
     * @deprecated use {@link URLUtils#badURLToURI}
     */
    public static URI badURLToURI (URL url) {
      return URLUtils.badURLToURI (url);
    }
  
    /**
     * If a URL is created from a URI with 'toURL' it will have properly quoted
     * characters, which this will convert it back to URI.
     *
     * @deprecated use {@link URLUtils#goodURLToURI}
     */
    public static URI goodURLToURI (URL url) {
      return URLUtils.goodURLToURI (url);
    }

    /**
     * Turns a URI into a URL with unescaped characters. This is a bit silly,
     * but broken java classes rely upon the spaces being in the URL, even
     * though it's not valid. URI.toURL() leaves spaces escaped, but this will
     * remove them.
     *
     * @deprecated use {@link URLUtils#uriToBadURL}
     */
    public static URL uriToBadURL (URI uri) {
      return URLUtils.uriToBadURL (uri);
    }

    /***********************************************************************/
    /* DIALOGS */
    /***********************************************************************/
  
    /**
     * Display a simple message in a dialog box
     */
    public static final void showMessage (String message) {
	showMessage (message, "Message");
    }

    /**
     * Display a simple message in a dialog box, with the specified title.
     */ 
    public static final void showMessage (String message, String title) {
	JOptionPane.showMessageDialog (null, message, title,
				       JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Display a simple warning message in a dialog box.
     */ 
    public static final void showWarning (String message) {
	JOptionPane.showMessageDialog (null, message, "Warning",
				       JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Display a simple error message in a dialog box.
     */ 
    public static final void showError (String message) {
	JOptionPane.showMessageDialog (null, message, "Error",
				       JOptionPane.ERROR_MESSAGE);
    }

    /***********************************************************************/
    /* WINDOW SIZING */
    /***********************************************************************/
  
    /**
     * Get screen dimensions.
     */
    public static final Dimension getScreenSize () {
	return Toolkit.getDefaultToolkit().getScreenSize();
    }

    /**
     * Center a component within (or over) a rectangular region.
     */
    private static final void centerComponent (Rectangle enclosure,
					       Component child) {
	Rectangle cr = child.getBounds();
	Point p = new Point ();
	p.x = enclosure.x + (enclosure.width - cr.width) / 2;
	p.y = enclosure.y + (enclosure.height - cr.height) / 2;
	child.setLocation (p);
    }

    /**
     * Center a (frame) component on the screen.
     */
    public static final void centerComponent (Component comp) {
	Rectangle enclosure =
	    new Rectangle (0,0, getScreenSize().width, getScreenSize().height);
	centerComponent (enclosure, comp);
    }

    /**
     * Center a (dialog) component with regards to the specified parent (frame).
     */
    public static final void centerComponent (Component parent,
					      Component child) {
	centerComponent (parent.getBounds(), child);
    }

    /**
     * Store bounds for a window for later invocations
     */
    public static final void storeGeometry (Window win, String name) {
	Preferences prefs = Jawb.getPreferences ();
	if (! prefs.getBoolean (Preferences.STORE_WINDOW_GEOMETRY_KEY))
	    return;
	Rectangle b = win.getBounds ();
	name = "windows."+name+".geometry";
	String value = "{"+b.x+","+b.y+","+b.width+","+b.height+"}";
	Jawb.getPreferences ().setPreference (name, value);
    }

    /**
     * Lookup bounds for a window and apply it to that window. Users of this
     * method should be prepared to either pack the window or set it with
     * defaults if this method returns false.
     * @return true if the windows geometry was set.
     */
    public static final boolean loadGeometry (Window win, String name) {
	Preferences prefs = Jawb.getPreferences ();
	if (! prefs.getBoolean (Preferences.STORE_WINDOW_GEOMETRY_KEY))
	    return false;
    
	name = "windows."+name+".geometry";
	String value = prefs.getPreference (name);
	if (value != null) {
      
	    StringTokenizer stoke = new StringTokenizer (value, "{},");
	    if (stoke.countTokens () == 4) {

		try {
		    Rectangle b = new Rectangle ();
		    b.x = Integer.parseInt (stoke.nextToken ());
		    b.y = Integer.parseInt (stoke.nextToken ());
		    b.width = Integer.parseInt (stoke.nextToken ());
		    b.height = Integer.parseInt (stoke.nextToken ());
          
                    // make sure it's not too far offscreen
                    Rectangle bounds =
                      GraphicsEnvironment.getLocalGraphicsEnvironment()
                      .getMaximumWindowBounds();

                    b.width = Math.min (b.width, bounds.width);
                    b.height = Math.min (b.height, bounds.height);
                    b.x = Math.max (b.x, bounds.x-5);
                    b.x = Math.min (b.x, bounds.width-b.width+5);
                    b.y = Math.max (b.y, bounds.y-5);
                    b.y = Math.min (b.y, bounds.height-b.height+5);

                    win.setBounds (b);
		    //win.validate ();
		    return true;
      
		} catch (Exception e) {
		    System.err.println ("Invalid bounds for "+name);
		    e.printStackTrace ();
		}
	    } else {
		System.err.println ("Invalid bounds for "+name+": "+
				    stoke.countTokens ()+" tokens");
	    }
	} // else (value == null)

	return false;
    }

    /***********************************************************************/
    /* NOTIFICATION */
    /***********************************************************************/
  
    /**
     * Alert the user audibly!
     */
    public static final void beep () {
	Toolkit.getDefaultToolkit().beep();
    }
  
    /**
     * Display a message in the Jawb Frames for <code>seconds</code> amount of
     * time. If seconds is less than 1, message will be displayed untill
     * another message overwrites it.
     */
    public static final void setStatusMessage (Component comp,
					       String message,
					       int seconds) {
	getJawbFrame (comp).setStatus (message, seconds);
    }

    /***********************************************************************/
    /* CURRENT FRAME / DOCUMENT */
    /***********************************************************************/
  
    public static final JawbFrame getJawbFrame (Component comp) {
      while (comp != null) {
        if (comp instanceof JawbFrame)
          return (JawbFrame) comp;
        
        if (comp instanceof JPopupMenu)
          comp = ((JPopupMenu) comp).getInvoker ();
        
        else if (comp instanceof JToolBar)
          return (JawbFrame)((JComponent)comp).getClientProperty(JAWB_FRAME_KEY);
        
        else if (comp instanceof JFrame) {
          // auxilliary frames need to set this, already done for
          // DetachableTabsJawbComponent...
          JComponent c = ((JFrame)comp).getRootPane();
          return (JawbFrame)c.getClientProperty (JAWB_FRAME_KEY);
        } else
          comp = comp.getParent();
      }
      return null;
    }
  
    public static final JawbComponent getJawbComponent (Component comp) {
      while (comp != null) {
        if (comp instanceof JawbComponent)
          return (JawbComponent)comp;

        if (comp instanceof JPopupMenu)
          comp = ((JPopupMenu) comp).getInvoker ();
        
        if (comp instanceof JFrame)
          return null;

        else
          comp = comp.getParent ();
      }
      return null;
    }

    public static final JawbDocument getJawbDocument (Component comp) {
      while (comp != null) {
        if (comp instanceof JawbComponent)
          return ((JawbComponent)comp).getJawbDocument ();

        if (comp instanceof JawbFrame)
          return ((JawbFrame)comp).getJawbDocument ();

        if (comp instanceof JPopupMenu)
          comp = ((JPopupMenu) comp).getInvoker ();
        
        if (comp instanceof JFrame)
          comp = getJawbFrame (comp);
        else
          comp = comp.getParent ();
      }
      return null;
    }

  /**
   * Returns the JawbFrame which is the base of the ancestory of the component
   * which fired the event. If <code>evt.getSource()</code> is not AWT
   * derivative, <code>null</code> is returned.
   *
   * @param evt The source event
   * @see #getJawbFrame (Component)
   */
  public static final JawbFrame getJawbFrame (EventObject evt) {
    Object source = evt.getSource ();
    if (source instanceof Component)
      return getJawbFrame ((Component) source);
    return null;
  }
   
  /**
   * Returns the JawbFrame which is the base of the ancestory of the component
   * which fired the event. If <code>evt.getSource()</code> is not AWT
   * derivative, <code>null</code> is returned.
   *
   * @param evt The source event
   * @see #getJawbComponent (Component)
   */
  public static final JawbComponent getJawbComponent (EventObject evt) {
    Object source = evt.getSource ();
    if (source instanceof Component)
      return getJawbComponent ((Component) source);
    return null;
  }
 /**
   * Returns the selected JawbDocument for the component which fired the
   * event.  If <code>evt.getSource()</code> is not AWT
   * derivative, <code>null</code> is returned.
   *
   * @param evt The source event
   * @see #getJawbDocument (Component)
   */
  public static final JawbDocument getJawbDocument (EventObject evt) {
    Object source = evt.getSource ();
    if (source instanceof Component)
      return getJawbDocument ((Component) source);
    return null;
  }

  /***********************************************************************/
  /* FILE CHOOSING */
  /***********************************************************************/

  /**
   * Initialize the file chooser, so initial 'open' or 'new' doesn't take so
   * long.
   */
  public static final void initFileChooser () {
    Preferences prefs = Jawb.getPreferences ();

    if (fileChooser != null)
      return;
    fileChooser = new JFileChooser ();
    fileChooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
    
    // start up where user left off last time (or default=user home)
    String last = prefs.getPreference (Preferences.LAST_DIR_KEY);
    if (last != null)
      fileChooser.setCurrentDirectory (new File (last));
    
    // some file filters
    aifFilter = new DefaultFileFilter
      (new String[] {".aif",".aif.xml"},
       "AIF (ATLAS Interchange Format) Files (*.aif, *.aif.xml)");
    sgmlFilter = new DefaultFileFilter
      (new String[] {".sgm",".sgml",".xml"},
       "SGML Files (*.sgm, *.sgml, *.xml)");
    htmlFilter = new DefaultFileFilter
      (new String[] {".htm",".html"},
       "HTML Files (*.htm *.html)");
    plainTextFilter = new DefaultFileFilter
      (new String[] {".txt",".text",".utf8",".utf-8"},
       "Plain Text Files (*.txt, *.utf8)");
    textFilter = new DefaultFileFilter
      (new String[] {".sgm",".sgml",".xml",".htm",".html",
                     ".txt",".text",".utf8",".utf-8"},
       "Text Based Files (*.sgml,*.xml,*.html,*.txt,*.utf8)");
    
    // task selector for 'new annots'
    taskAccessory = new TaskAccessory(Jawb.getTasks());
    taskAccessory.setBorder
      (BorderFactory.createEmptyBorder (0,5,0,0));
    
    // workspace specification accessory
    workspaceAccessory = new WorkspaceAccessory();
    workspaceAccessory.setBorder
      (BorderFactory.createEmptyBorder (0,5,0,0));
    
    // import selector
    importAccessory = new ImportAccessory (Jawb.getTasks ());
    importAccessory.setBorder
      (BorderFactory.createEmptyBorder (0,5,0,0));
    
    // export selector
    exportAccessory = new ExportAccessory (Jawb.getTasks ());
    exportAccessory.setBorder
      (BorderFactory.createEmptyBorder (0,5,0,0));
  }
  
    /**
     * Display a file chooser allowing multiple files to be selected.
     * @param comp component to be the modal owner of the dialog
     * @param mode either <code>OPEN_DIALOG</code> or <code>SAVE_DIALOG</code>
     * @return array of files selected, or null.
     */
    public static final File[] chooseFiles (Component comp, int mode) {
	return (File[]) chooseFiles (comp, null, mode, true);
    }

    /**
     * Display a file chooser allowing only a single file to be selected.
     * @param comp component to be the modal owner of the dialog
     * @param mode either <code>OPEN_DIALOG</code> or <code>SAVE_DIALOG</code>
     *             or <code>SAVE_COLLECTION_DIALOG</code>
     * @return file selected, or null.
     */
    public static final File chooseFile (Component comp, int mode) {
	return (File) chooseFiles (comp, null, mode, false);
    }
  
    /**
     * Display a file chooser allowing only a single file to be selected,
     * initialized with the specified file name (need not be an actual file).
     * @param comp component to be the modal owner of the dialog
     * @param mode either <code>OPEN_DIALOG</code> or <code>SAVE_DIALOG</code>
     *             or <code>SAVE_COLLECTION_DIALOG</code>
     * @return file selected, or null.
     */
    public static final File chooseFile (Component comp, URI suggestion,
                                         int mode) {
	return (File) chooseFiles (comp, suggestion, mode, false);
    }
  
  
    /**
     * Maintain a single file chooser, for all uses.  This chooses and returns
     * either a single File, or an array of files, determined by
     * <code>multiFile</code>
     * @param comp component to be the modal owner of the dialog
     * @param mode either <code>OPEN_DIALOG</code> or <code>SAVE_DIALOG</code>
     *             or <code>SAVE_COLLECTION_DIALOG</code>
     * @param multiFile if true, dialog allows multiple files to be selected.
     * @return a single file or multiple files (or null if canceled) depending
     *         on the value of <code>multiFile</code>
     */
    private static final Object chooseFiles (Component comp,
                                             URI suggestion, int mode,
                                             boolean multiFile) {
	Preferences prefs = Jawb.getPreferences ();
    
	if (fileChooser == null)
          initFileChooser ();
	
	// perhaps user wants to follow the current file
	if (prefs.getBoolean (Preferences.FOLLOW_FILE_KEY)) {
	    JawbFrame jf = getJawbFrame (comp);
	    JawbDocument doc = null;
	    if (jf != null) {
		doc = jf.getCurrentDocument ();
		if (doc != null) {
		    URI uri = doc.getAtlasURI ();
		    if (uri != null) {
			File currentDir = new File (uri).getParentFile ();
			fileChooser.setCurrentDirectory (currentDir);
		    }
		}
	    }
	}

        // 'reset' the chooser
        File suggested = (suggestion == null ?
                          new File ("") : new File (suggestion));
        fileChooser.setSelectedFile (suggested);
        // yay!! "" clears both filename and selection!... Don't delete
        // comments below just yet tho
        
	//fileChooser.setSelectedFile (null);
	// bug in JFileChooser: setSelectedFile doesen't clear list-selection
	// cycling multiselection mode does
	//fileChooser.setMultiSelectionEnabled (false);

	fileChooser.setMultiSelectionEnabled (multiFile);
	fileChooser.rescanCurrentDirectory ();
        fileChooser.setAccessory (null);
        fileChooser.resetChoosableFileFilters ();
    
	switch (mode) {
      
	case GUIUtils.SAVE_DIALOG: default:
	    fileChooser.setDialogType (JFileChooser.SAVE_DIALOG);
	    fileChooser.setApproveButtonText ("Save");
	    fileChooser.setDialogTitle ("Save as AIF");
            fileChooser.addChoosableFileFilter (aifFilter);

	    // TODO: add listener to the OK button to popup a warning if:
	    //    file exists and will be overwritten. see JawbFrame.save
	    //    file is not writeable. see JawbFrame.save
	    break;

	case GUIUtils.SAVE_COLLECTION_DIALOG:
	    fileChooser.setDialogType (JFileChooser.SAVE_DIALOG);
	    fileChooser.setApproveButtonText ("Save");
	    fileChooser.setDialogTitle ("Save Collection");
            // fileChooser.addChoosableFileFilter (aifFilter);

	    // TODO: add listener to the OK button to popup a warning if:
	    //    file exists and will be overwritten. see JawbFrame.save
	    //    file is not writeable. see JawbFrame.save
	    break;

	case GUIUtils.EXPORT_DIALOG:
            fileChooser.setDialogType (JFileChooser.CUSTOM_DIALOG);
	    fileChooser.setApproveButtonText ("Export");
	    fileChooser.setDialogTitle ("Export as");
            fileChooser.setAccessory (exportAccessory);
	    break;

	case GUIUtils.OPEN_SIGNAL_DIALOG:
            fileChooser.setDialogType (JFileChooser.CUSTOM_DIALOG);
	    fileChooser.setApproveButtonText ("Annotate");
	    fileChooser.setDialogTitle ("Annotate File");
	    fileChooser.setAccessory (taskAccessory);
            fileChooser.setFileFilter (textFilter);
	    break;
            
	case GUIUtils.OPEN_ANNOTATION_DIALOG:
	    fileChooser.setDialogType (JFileChooser.OPEN_DIALOG);
	    fileChooser.setApproveButtonText ("Open");
	    fileChooser.setDialogTitle ("Open AIF File");
            fileChooser.setFileFilter (aifFilter);
	    break;
            
	case GUIUtils.OPEN_COLLECTION_DIALOG:
	    fileChooser.setDialogType (JFileChooser.OPEN_DIALOG);
	    fileChooser.setApproveButtonText ("Open");
	    fileChooser.setDialogTitle ("Open Collection File");
	    break;
            
	case GUIUtils.OPEN_WORKSPACE_DIALOG:
            fileChooser.setDialogType (JFileChooser.CUSTOM_DIALOG);
	    fileChooser.setApproveButtonText ("Open Workspace");
	    fileChooser.setDialogTitle ("Specify Workspace Details");
	    fileChooser.setAccessory (workspaceAccessory);
            fileChooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
	    break;
            
	case GUIUtils.IMPORT_DIALOG:
            fileChooser.setDialogType (JFileChooser.CUSTOM_DIALOG);
	    fileChooser.setApproveButtonText ("Import");
	    fileChooser.setDialogTitle ("Import File");
            fileChooser.setAccessory (importAccessory);
	    break;
	}
    
	if (fileChooser.showDialog (comp, null)==JFileChooser.APPROVE_OPTION) {
	    File dir = fileChooser.getCurrentDirectory();
	    // save directory
	    prefs.setPreference (Preferences.LAST_DIR_KEY, dir.getPath ());
            if (mode == GUIUtils.OPEN_WORKSPACE_DIALOG) {
              // save the user selections
              prefs.setPreference(Preferences.LAST_WS_KEY_KEY,
                                  getWorkspaceKey());
              prefs.setPreference(Preferences.LAST_WS_NAME_KEY,
                                  getWorkspaceNameFromTypeIn());
              prefs.setPreference(Preferences.LAST_WS_URL_KEY,
                                  getWorkspaceURL());
              prefs.setPreference(Preferences.LAST_WS_OPEN_NEXT_KEY,
                                  new Boolean(getOpenNextdoc()));
              System.err.println("setting " + Preferences.LAST_WS_FOLDER_KEY +
                                 " to " + getWorkspaceFolder());
              prefs.setPreference(Preferences.LAST_WS_FOLDER_KEY,
                                  getWorkspaceFolder());
              Jawb.getLogger().
                info(JawbLogger.LOG_OPENWS_DIALOG_OPTIONS,
                     new Object[] {getWorkspaceFolder(),
                                   new Boolean(getOpenNextdoc())});
            }
      
	    if (multiFile)
              return fileChooser.getSelectedFiles ();
	    else
              return fileChooser.getSelectedFile ();
	}
	return null;
    }

    /**
     * Specifies the signal encoding for importing/creating files. Intended
     * only for EncodingAccessory.
     */
    static void setSelectedEncoding (String encoding) {
      selectedEncoding = encoding;
    }

    /**
     * Gets encoding to be used for importing/creating files.
     *
     * @return the encoding to be used.
     */
    public static String getSelectedEncoding () {
      return selectedEncoding;
    }

    /**
     * Gets MIME type (not MIME Class) to be used for importing/creating files.
     *
     * @return the MIME type to be used.
     */
    public static String getSelectedMIMEType () {
      return selectedMIMEType;
    }

    /**
     * Specifies the signals MIME type (not MIME Class) for importing/creating
     * files. Intended only for the MIMETypeAccessory.
     */
    static void setSelectedMIMEType (String mimeType) {
      selectedMIMEType = mimeType;
    }

    /**
     * Gets the tallal system the user selected.
     *
     * @return the selected task, null if there is none
    public static TallalSystem getSelectedSystem() {
	if (taskAccessory == null)
	    initFileChooser();
	return taskAccessory.getPreAnnotateAccessory().getSelectedSystem();
    }
     */

    /**
     * Gets the task the user selected. 
     *
     * @return the selected task, null if there is none
     */
    public static Task getSelectedTask() {
      if (taskAccessory == null)
        initFileChooser();
      return taskAccessory.getSelectedTask();
    }

    /**
     * Gets the workspace key the user specified. 
     *
     * @return the workspace key, null if there is none
     */
    public static String getWorkspaceKey() {
      if (workspaceAccessory == null)
        initFileChooser();
      return workspaceAccessory.getWorkspaceKey();
    }

    /**
     * Gets the workspace name the user specified via typeIn field
     *
     * @return the workspace name, null if there is none
     */
    public static String getWorkspaceNameFromTypeIn() {
      if (workspaceAccessory == null)
        initFileChooser();
      return workspaceAccessory.getWorkspaceNameFromTypeIn();
    }

    /**
     * Gets the workspace folder the user specified. 
     *
     * @return the workspace folder, null if there is none
     */
    public static String getWorkspaceFolder() {
      if (workspaceAccessory == null)
        initFileChooser();
      return workspaceAccessory.getWorkspaceFolder();
    }

    /**
     * Gets the workspace url the user specified. 
     *
     * @return the workspace url, null if there is none
     */
    public static String getWorkspaceURL() {
      if (workspaceAccessory == null)
        initFileChooser();
      return workspaceAccessory.getWorkspaceURL();
    }

    /**
     * Gets the value of the openNextdoc checkbox
     *
     * @return whether user wants to get nextdoc when opening
     * workspace, null if not set
     */
    public static boolean getOpenNextdoc() {
      if (workspaceAccessory == null)
        initFileChooser();
      return workspaceAccessory.getOpenNextdoc();
    }

    /**
     * Gets the workspace active learning status the user specified. 
     *
     * @return the workspace active learning status, null if there is none
     */
  /************
    public static boolean getWorkspaceActive() {
      if (workspaceAccessory == null)
        initFileChooser();
      return workspaceAccessory.getWorkspaceActive();
    }
  **************/

    /**
     * Gets the importer the user selected.
     * @return the importer selected when last the Import accessory was
     *   shown, or null
     */
    public static Importer getSelectedImporter() {
      if (importAccessory == null)
        initFileChooser();
      return importAccessory.getSelectedImporter();
    }

    /**
     * Gets the exporter the user selected.
     * @return the exporter selected when last the Export accessory was
     *   shown, or null
     */
    public static Exporter getSelectedExporter() {
      if (exportAccessory == null)
        initFileChooser();
      return exportAccessory.getSelectedExporter();
    }

    /** This method is an artifact of implementation bound to dissappear */
    public static ExportAccessory getExportAccessory () {
      if (exportAccessory == null)
        initFileChooser();
      return exportAccessory;
    }
    /**
     * Return the jawb icon for frames and various widgets
     */
    public static final Image getJawbIconImage () {
	return Jawb.ICON_IMAGE;
    }
}

