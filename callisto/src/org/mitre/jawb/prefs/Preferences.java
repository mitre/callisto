
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

package org.mitre.jawb.prefs;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Properties;
import javax.swing.event.SwingPropertyChangeSupport;

/**
 * Wrapper around java.util.Properties, which includes ChangeEventHandling,
 * and has several Convience methods for accessing properties. This also
 * maintains a set of keys used throughout Jawb to access preferences.
 */
public class Preferences {

  private static int DEBUG = 0;

  /***********************************************************************/
  /* KEYS to access Preferences
  /***********************************************************************/

  /** Key for location of last successful directory access. */
  public static final String LAST_DIR_KEY = "callisto.dir.last";
  /** Key for remembering last task used in 'new' dialog. */
  public static final String LAST_TASK_KEY = "callisto.task.last";
  /** Key for remembering last task used in 'import' dialog. */
  public static final String LAST_IMPORTER_KEY = "callisto.task.last.importer";
  /** Key for remembering last workspace key used in 'open workspace' dialog */
  public static final String LAST_WS_KEY_KEY = "callisto.workspace.key";
  /** Key for remembering last workspace name typed-in in 'open ws' dialog */
  public static final String LAST_WS_NAME_KEY = "callisto.workspace.name";
  /** Key for remembering last workspace userid used in 'open workspace' dialog */
  public static final String LAST_WS_URL_KEY = "callisto.workspace.url";
  /** Key for remembering use_picker status from 'open workspace' dialog */
  public static final String LAST_WS_USE_PICKER_KEY = 
    "callisto.workspace.usepicker";
  /** Key for remembering open_next status from 'open workspace' dialog */
  public static final String LAST_WS_OPEN_NEXT_KEY = 
    "callisto.workspace.opennext";
  /** Key for remembering folder chosen in 'open workspace' dialog */
  public static final String LAST_WS_FOLDER_KEY = 
    "callisto.workspace.folder";
  /** Key for remembering last workspace userid selected */
  public static final String LAST_WS_USER_KEY = 
    "callisto.workspace.userid";


  // *** used in General *** //
  /** Key for flag to have file chooser follow the current file */
  public static final String FOLLOW_FILE_KEY = "callisto.dir.followFile";
  /** Key for integer number of files to remember across invocations */
  public static final String HISTORY_MAX_KEY = "history.max";
  /** Base key for sequence of absolute URI's remembered across invocations */
  public static final String HISTORY_LIST_KEY = "history.list";
  /** Key for flag to show full path in title bar */
  public static final String SHOW_FULL_PATH_KEY = "windows.callisto.showFullPath";
  /** Key for flag to store geometry of supporting windows */
  public static final String STORE_WINDOW_GEOMETRY_KEY = "windows.storeGeometry";
  /** Key for number of backups to keep*/
  public static final String BACKUP_COUNT_KEY = "backup.count";
  /** Key to remember whether to parse new signals as sgml (or other) */
  public static final String TEXT_MIME_TYPE_KEY = "callisto.text.type";
  /** Key to remember which text encoding was last used */
  public static final String TEXT_ENCODING_KEY = "callisto.text.encoding";


  // *** used in Fonts *** //
  /** Key to reference the style used to color un-recognized text tags. */
  public static final String FONTS_AUTO_DETECTED_KEY = "fonts.autodetected";
  /** Key for the font family last specified by user */
  public static final String FONTS_LAST_FAMILY_KEY = "fonts.last.family";
  /** Key to specify that last font family is used for new docs. */
  public static final String FONTS_LAST_FAMILY_ENABLED_KEY = "fonts.last.family.enabled";
  /** Key for the font size last specified by user. */
  public static final String FONTS_LAST_SIZE_KEY = "fonts.last.size";
  /** Key to specify that last font size is used for new docs. */
  public static final String FONTS_LAST_SIZE_ENABLED_KEY = "fonts.last.size.enabled";
  /** Key to specify that table font size follows main text font size. */
  public static final String FONTS_TABLE_FOLLOW_SIZE_KEY = "fonts.table.follow.size";
  /** Key for the font size last specified by user. */
  public static final String PARA_LAST_LINE_SPACING_KEY = "para.last.line.spacing";
  /** Key to specify that last font size is used for new docs. */
  public static final String PARA_LAST_LINE_SPACING_ENABLED_KEY = "para.last.line.spacing.enabled";

  /** Key to specify if sorting is case sensitive */
  public static final String SORT_CASE_SENSITIVE_KEY = "sort.case.sensitive";
  
  /** Key to reference the style used to color un-recognized text tags. */
  public static final String UNKNOWN_HIGHLIGHT_KEY = "highlight.<unknown>";
  
  /** Key to specify whether logging is enabled or not. */
  public static final String LOG_ENABLED_KEY = "log.enabled";
  /** Key to specify whether the log initialization should warn when it starts. */
  public static final String LOG_ENABLED_WARN_KEY = "log.enabled.warn";
  /** Key to indicate how the previous logs should be dispensed with:
   * two values are: email and delete, upload and delete, do nothing and keep. 
   * Default is to do nothing and delete.
   */
  public static final String LOG_DISPENSATION_KEY = "log.dispensation";
  public static final String LOG_DISPENSATION_EMAIL_VALUE = "email";
  public static final String LOG_DISPENSATION_UPLOAD_VALUE = "upload";
  public static final String LOG_DISPENSATION_KEEP_VALUE = "keep";
  /** Key to indicate, for email, where the log should be emailed. */
  public static final String LOG_DISPENSATION_EMAIL_ADDR_KEY = "log.dispensation.emailAddr";
  /** Key to indicate, for upload, where the log should be uploaded to. */
  public static final String LOG_DISPENSATION_UPLOAD_URL_KEY = "log.dispensation.uploadUrl";
  
  /** Key to indicate whether saved files should be uploaded as well. */
  public static final String LOG_DISPENSATION_UPLOAD_ANNOTATION_RESULTS = "log.dispensation.uploadAnnots";

  /** Key to indicate the preferred autotagging mode */
  public static final String AUTOTAG_MODE_KEY = "callisto.autotag.mode";
  /** Key to indicate whether autotagging should only proceed forward
      from the current point in the file */
  public static final String AUTOTAG_FORWARD_ONLY_KEY = "callisto.autotag.forward";
  /** Key to indicate whether autotagging should skip already tagged text */
  public static final String AUTOTAG_UNTAGGED_ONLY_KEY = "callisto.autotag.untagged";
  
  // *** used in Advanced *** //
  public static final String PROXY_SET_KEY = "proxySet";
  public static final String PROXY_HOST_KEY = "proxyHost";
  public static final String PROXY_PORT_KEY = "proxyPort";

  // *** used in Highlight *** //
  /* none */

  
  // used to indicate what type of value has been set by setXXX methods
  private static final int BOOLEAN = 0;
  private static final int INTEGER = 1;
  private static final int FLOAT = 2;
  private static final int STRING = 3;
  private static final int COLOR = 4;
  private static final int COLOR_SPEC = 5;

  // have prefs been changed since last save?
  private boolean dirty = false;

  // user and default prefs. Initialize for easier module testing
  private Properties prefs;

  /**
   * Initialize the preferences with set of properties which will be
   * modifiable by by the Preferences, although the <i>parent</i> object of
   * the Properties will not immodifieable.
   */
  public Preferences (Properties prefs) {
    this.prefs = prefs;
  }

  /***********************************************************************/
  /* UTILITY FUNCTIONS - encode/decode open to the masses */
  /***********************************************************************/

  /**
   * Convert a color to it's hex value prefixed with '#' like "#ff8800" that
   * can be reconverted to a color using decodeColor. Case insensitive.
   * @see #decodeColor
   */
  public static final String encodeColor (Color color) {
    String colString = Integer.toHexString(color.getRGB() & 0xFFFFFF);
    return "0x000000".substring (0, 8 - colString.length()).concat (colString);
  }

  /**
   * Converts a hex color value to a Color object. Returns the default color
   * specified if there is a problem decoding the string, and the Systems
   * "controlShadow" color default color is null.
   * TODO: allow and convert old unix color strings. That's why this is here
   * as opposed to implementing the java.awb.Color method.
   * @see #encodeColor
   */
  public static final Color decodeColor (String cString, Color def) {
    if (cString != null) {
      try {
        def = Color.decode (cString);
      } catch (NumberFormatException e) {}
    }
    return def;
  }

  /***********************************************************************/
  /* 
  /***********************************************************************/

  /**
   * Saves backing properties object to the output stream specified.  If out
   * is <code>null</code>, marks Preferences as not dirty, as though 'stored'
   * to <code>/dev/null</code>.
   * @param out stream to write preferences to, or <code>null</code> to mark
   * preferences as clean.
   * @param header a description of the property list.  */
  public void store (OutputStream out, String header) throws IOException {
    if (out != null)
      prefs.store (out, header);
    dirty = false;
  }
  
  /**
   * Have preferences been altered since last save?
   */
  public boolean isDirty () {
    return dirty;
  }
  
  /***********************************************************************/
  /* SET METHODS and convience derivatives */
  /***********************************************************************/
  
  /* Set Preferences. These methods should 1) get old value from 'prefs' 2)
   * get value from 'defaultprefs' 3) compare default value to new values
   * string counterpart (checking for nulls) 4) if default and new are equal,
   * clear and _remove_ the value in 'prefs', 5) fire a property change event
   * with appropriate objects (doing conversion from string if appropriate) */

  /* TODO: Note that none of these _do_ what is outlined above right now... */
  
  /**
   * Change or set a boolean value.
   */
  public void setPreference (String name, boolean value) {
    if (name == null)
      return;
    setPreferenceImpl (name, Boolean.valueOf (value),
                       Boolean.toString (value), BOOLEAN);
  }

  /**
   * Change or set an intager value.
   */
  public void setPreference (String name, int value) {
    if (name == null)
      return;
    setPreferenceImpl (name, new Integer (value),
                       Integer.toString (value), INTEGER);
  }

  /**
   * Change or set an intager value.
   */
  public void setPreference (String name, float value) {
    if (name == null)
      return;
    setPreferenceImpl (name, new Float (value),
                       Float.toString (value), FLOAT);
  }

  /**
   * Change or set a color value.
   */
  public void setPreference (String name, Color value) {
      if (name == null) 
	  return;
      if (value != null) {
	  setPreferenceImpl (name, value, encodeColor (value), COLOR);
      } else {
	  setPreferenceImpl (name, value, null, COLOR); 
      }
  }

  /**
   * Change or set a ColorSpec value.
   */
  public void setPreference (String name, ColorSpec value) {
      if (name == null) 
      return;
      if (value != null) {
      setPreferenceImpl (name, value ,value.toString(), COLOR_SPEC);
      } else {
      setPreferenceImpl (name, value, null, COLOR_SPEC);
      }
  }

  /**
   * Change or set a String value.
   */
  public void setPreference (String name, String value) {
    if (name == null)
      return;
    setPreferenceImpl (name, value, value, STRING);
  }

  private void setPreferenceImpl (String name, Object value,
                                  String stringValue, int type) {
    // keep user prefs to minimum by removing the value if it's null, but not
    // "" becuause that implies masking the default vaules, right?
    Object old = null;
    if (value == null)
      prefs.remove (name);
    else {
      old = prefs.setProperty (name, stringValue);
      dirty = true;
      if (DEBUG > 0)
        System.err.println (" -> setPref("+name+","+stringValue+")  was:"+old);
    }

    // fire property change through the right type
    switch (type) {
    case BOOLEAN:
      old = new Boolean ((String)old);
      break;
    case INTEGER:
      if (old != null) {
        try { old = new Integer ((String)old); // could still be non-number
        } catch (NumberFormatException e) { old = null; }
      }
      break;
    case FLOAT:
      if (old != null) {
        try { old = new Float ((String)old); // could still be non-number
        } catch (NumberFormatException e) { old = null; }
      }
      break;
    case COLOR:
      old = decodeColor ((String)old, null);
      break;
    case COLOR_SPEC:
      old = (old!=null)?ColorSpec.valueOf((String)old):null;
      break;
    /* case STRING: empty */
    }
    // SwingPr..Support will fire when old == new == <null>. Don't let it.
    if (old != value)
      support.firePropertyChange (name, old, value);
  }
  
  /***********************************************************************/
  /* GET METHODS and convience derivatives */
  /***********************************************************************/
  
  /**
   * Returns true if the preference value matches (case insensitive) "true", or
   * false if the preference is not defined.
   */
  public boolean getBoolean (String name) {
    String p = getPreference (name);
    return Boolean.valueOf (p).booleanValue(); /* valueOf () accepts null */
  }
  
  /**
   * Returns true if the preference value matches (case insensitive) "true", or
   * <code>def</code> if <code>value</code> is not defined.
   */
  public boolean getBoolean (String name, boolean def) {
    String p = getPreference (name);
    if (p == null)
      return def;
    else
      return Boolean.valueOf (p).booleanValue();
  }

  /**
   * Returns the preference specified, as an integer.
   * @throws NumberFormatException if the value cannot be parsed as an integer.
   */
  public int getInteger (String name) {
    String p = getPreference (name);
    return Integer.valueOf (p).intValue();
  }
  
  /**
   * Returns the preference specified as an integer, or <code>def</code> if
   * the value is not defined.
   */
  public int getInteger (String name, int def) {
    String p = getPreference (name);
    try {
      return Integer.valueOf (p).intValue();
    } catch (Exception  e) {
      return def;
    }
  }

  /**
   * Returns the preference specified, as a float.
   * @throws NumberFormatException if the value cannot be parsed as an float.
   */
  public float getFloat (String name) {
    String p = getPreference (name);
    return Float.valueOf (p).floatValue();
  }
  
  /**
   * Returns the preference specified as a float, or <code>def</code> if
   * the value is not defined.
   */
  public float getFloat (String name, float def) {
    String p = getPreference (name);
    try {
      return Float.valueOf (p).floatValue();
    } catch (Exception  e) {
      return def;
    }
  }

  /**
   * returns the preference specified, as a Color, or the systems
   * controlShadow
   * @throws NumberFormatException if the value cannot be parsed as an integer.
   * @see #decodeColor
   */
  public Color getColor (String name) {
    return getColor (name, null);
  }
  
  /**
   * Returns the preference specified, as a Color, or <code>def</code> if the
   * value is not defined, or conversion to Color fails.  If <code>def</code>
   * is null, the systems controlShadow color is returned.
   * @see #decodeColor
   */
  public Color getColor (String name, Color def) {
    String p = getPreference (name);
    return decodeColor (p, def);
  }

  /**
   * Returns the preference specified as a ColorSpec.
   *
   * @return The ColorSpec version of the specified property.
   *
   * @see ColorSpec
   */
  public ColorSpec getColorSpec(String name) {
    String p = getPreference (name);
    return ColorSpec.valueOf(p);
  }
  
  /**
   * Returns the preference specified as a ColorSpec, or <code>def</code> if
   * the value is not defined or conversion to color fails. If
   * <code>def</code> is null, the default ColorSpec is used.
   * @return The ColorSpec version of the specified property.
   * @see ColorSpec
   */
  public ColorSpec getColorSpec(String name, ColorSpec def) {
    String p = getPreference (name);
    if (p == null)
      return def;
    return ColorSpec.valueOf(p);
  }
  
  /**
   * Return the value of the preference (possibly null).
   */
  public String getPreference (String name) {
    String value = prefs.getProperty (name);
    if (DEBUG > 0)
      System.err.println (" <- getPref("+name+") = "+value);
    return value;
  }

  /**
   * Return the value of the preference, or <code>def</code> if
   * <code>name</code> is not defined.
   */
  public String getPreference (String name, String def) {
    String value = prefs.getProperty (name, def);
    if (DEBUG > 0)
      System.err.println (" <- getPref("+name+", def="+def+") = "+value);
    return value;
  }

  /**
   * Returns the string property with the specified name, after formatting it
   * with the {@link MessageFormat#format(String, Object[])} method.
   * @see MessageFormat
   */
  public String getPreference (String name, Object[] args) {
    if (name == null)
      return null;
    
    if (args == null)
      return prefs.getProperty (name, name);
    else
      return MessageFormat.format (prefs.getProperty (name, ""), args);
  }

  public void list(PrintStream out) {
    prefs.list(out);
  }
  
  /***********************************************************************/
  /* Preference Listeners */
  /***********************************************************************/

  /** Property Change Support to deal with preferences */
  private SwingPropertyChangeSupport support =
    new SwingPropertyChangeSupport (this);

  public static final String LOG_FILENAME_TAG = "log.filename_tag";
  public static final String LOG_DISPENSATION_UPLOADER_CLASS = "log.dispensation.uploaderClass";
  public static final String LOG_PATH = "log.path";
  
  public boolean hasListeners (String propertyName) {
    return support.hasListeners (propertyName);
  }
  public void addPropertyChangeListener (PropertyChangeListener listener) {
    support.addPropertyChangeListener (listener);
  }
  public void addPropertyChangeListener (String propertyName,
                                         PropertyChangeListener listener) {
    support.addPropertyChangeListener (propertyName, listener);
  }
  public void removePropertyChangeListener (PropertyChangeListener listener) {
    support.removePropertyChangeListener (listener);
  }
  public void removePropertyChangeListener (String propertyName,
                                             PropertyChangeListener listener) {
    support.removePropertyChangeListener (propertyName, listener);
  }
  public PropertyChangeListener[] getPropertyChangeListeners () {
   return support.getPropertyChangeListeners ();
  }
  public PropertyChangeListener[] getPropertyChangeListeners (String propertyName) {
    return support.getPropertyChangeListeners (propertyName);
  }
  
}
