
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

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URI;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.SwingPropertyChangeSupport;

import gov.nist.atlas.Annotation;
import gov.nist.atlas.type.AnnotationType;

import org.mitre.jawb.*;

import org.mitre.jawb.atlas.AWBDocument;
import org.mitre.jawb.atlas.AnnotationModel;
import org.mitre.jawb.atlas.AnnotationModelEvent;
import org.mitre.jawb.atlas.AnnotationModelListener;

import org.mitre.jawb.io.URLUtils;

import org.mitre.jawb.swing.FontSupport;
import org.mitre.jawb.swing.SetModel;
import org.mitre.jawb.swing.LinkedHashSetModel;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.TextExtentRegion;
import org.mitre.jawb.atlas.NamedExtentRegions;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.prefs.ColorSpec;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.ToocaanTask;
import org.mitre.jawb.tasks.Exporter;
import org.mitre.jawb.tasks.Importer;

/**
 * This object maintains a document with Standoff annotation with all the
 * neccissary information to display it and edit the annotations of an ATLAS
 * document. It is the model for Jawb that GUI views and controllers
 * access. The annotation information is stored using the ATLAS Library, and
 * a StyledDocument is kept updated with display information. JawbComponent
 * objects must refresh their display completely if the JawbDocument is
 * changed.<p>
 *
 * Factory methods are provided for creating JawbDocuments from specified
 * files. A JawbDocument can be created {@link #fromSignal}, where the user
 * will later have to specify the location to store the standoff annotaion,
 * or {@link #fromAIF} which will read in existing annotations from an ATLAS
 * Interchange Format file. Annotations are stored using {@link #save} or
 * undone using {@link #revert}<p>
 *
 * The information it maintatins can be broken into three sets: 1) Information
 * about the whole document, which PropertyChangeListeners are notified of, 2)
 * Annotation creation/deletion and modification events, which can be
 * JawbDocumentListeners are notified of, and 3) a List of selected annotions
 * is maintained which a {@link org.mitre.jawb.swing.event.SetDataListener} may
 * register to be notified of<p>
 *
 * <h5>JawbDocument Properties</h5>
 *
 * Properties are acessed using bean like methods in this class. Examples
 * are {@link #getComponentOrientation}, and {@link #getEncoding}.  Several
 * are read only properties such as {@link #getTask} and {@link
 * #isDirty}. PropertyChangeListeners can register to be notified of updates
 * usin the {@link #addPropertyChangeListener(PropertyChangeListener)}. and
 * {@link #addPropertyChangeListener(String,PropertyChangeListener)}
 * methods.<p>
 *
 * <h5>JawbDocument Task Properties</h5>
 *
 * Documents may have information associated with them at run time, using
 * arbitrary key/value pairs. These properties do not conflict with the
 * JawbDocument Properties, nor are events triggered when these properties
 * change. Methods to access these properties are:
 * <ul>
 *   <li>{@link #getClientProperties}
 *   <li>{@link #setClientProperties}
 *   <li>{@link #getClientProperty}
 *   <li>{@link #putClientProperty}
 * </ul>
 *
 * <h5>SelectedAnnotationModel</h5>
 *
 * Currently Selected annotations are also maintained, and a standalone
 * {@link SetModel} is provided to track it since multiple annotations may
 * be selected at a time. Users listening to the SetModel provided by
 * {@link #getSelectedAnnotationModel} must be carefull that a change event
 * does not trigger an ininite loop of updates!<p>
 *
 * <h5>StyledDocument</h5>
 *
 * Visual elements maintined by the JawbDocument are provided via a
 * StyledDocument. It is kept updated with the actual highlight preferences
 * of the users for the {@link Task#getHighlightKeys()} provided by a
 * task. An 'Unknown' color is also provided for times when a Task object
 * mis-behaves and returns an invalid key from {@link
 * Task#getHighlightKey(AWBAnnotation annot, Object constraint)} Task
 * maintainers should avoid this.<p>
 *
 * Annotations at a point can have the Z ordering cycled, with {@link
 * #raiseAnnotation} (The only Z ordering change notification is through the
 * highlight changes in the styled document for display update).<p>
 *
 * Annotations can be created ({@link #createAnnotation}) and deleted
 * ({@link #deleteAnnotation}) and cycle the Z ordering of annotations at a
 * point {@link #raiseAnnotation} (The only Z ordering change notification is
 * through the highlight changes in the styled document for display update).<p>
 *
 * &nbsp;<p>
 * <i>This class is not intended to be serialized.</i>
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */
public class JawbDocument {
  
  // debugging 0==off
  private static int DEBUG = 0;

  /** Key to reference ATLAS URI changes. */
  public static final String ATLAS_URI_KEY = "atlasURI";
  
  /** Key to reference name changes caused when saving. */
  public static final String NAME_KEY = "name";
  
  /** Key to reference encoding changes. */
  public static final String ENCODING_PROPERTY_KEY = "encoding";
  
  /** Key to reference orientation of the document. */
  public static final String ORIENTATION_PROPERTY_KEY = "orientation";
  
  /** Key to reference font of entire document. */
  public static final String FONT_FAMILY_PROPERTY_KEY = "fontFamily";

  /** Key to reference font size of entire document. */
  public static final String FONT_SIZE_PROPERTY_KEY = "fontSize";

  /** Key to reference 'dirty' flag of the document. */
  public static final String DIRTY_FLAG_KEY = "dirty";

  /**
   * Attribute key for the ordered list of annotations in an
   * AttributeSet. Each element in the StyleDocument has an attributeset. If
   * an element has annotations which span it, they will be in the annotation
   * attribute.
   */
  private static final Object ANNOTATION_ATTRIBUTE = "annotations";

  /** Tracks documents of same name, and sets unique ids for every instance */
  private static HashMap documentIDMap = new HashMap ();
  
  /** The location of the signal this document represents */
  private URL signalURL = null;
  /** Location of the signal, as a URI. */
  private URI signalURI = null;
  
  /** Location of the original file when imported, as a URI. */
  private URI externalURI = null;
  
  /** The current [One of the?] atlas file[s?] this document represents */
  private URI atlasURI = null;
  
  /** Maintainer for services for this document */
  private AWBDocument awbDoc = null;
  
  /** The task this document annotates with. */
  private Task task = null;

  /** Importer to be used when 'reloading' document. */
  private Importer importer = null;
  
  /** Exporter to be used when saving document to non AIF. */
  private Exporter exporter = null;
  
  /** This caches so we needn't rebuild the string constantly. */
  private String namespace = null;
  
  /** Central location for the StyledDocument of this ATLAS file. */
  private DefaultStyledDocument styledDocument = null;
  
  /** The orientation components wishing to display text should use */
  private ComponentOrientation orientation = ComponentOrientation.UNKNOWN;

  /** Listens to Annotations for offset changes, and updates highlights. */
  private TextRangeTracker textRangeTracker = new TextRangeTracker ();

  /** Listener for changes to properties */
  private PropertyChangeListener pcListener;
  
  /** Listener for general annotation changes such as additions and removal. */
  private AnnotationModelListener amListener;

  /** Show tags with null styleKey or not? */
  private boolean showUnknowns;

  /** Log annotation creation, or not.  Defaults to true */
  private boolean logAnnotationCreation = true;

  /** Sort unselected annots so that smallest are on top or not?
   *  Defaults to true and should normally be left as true.  Can be
   *  changed using setSortAnnots if needed. */
  private boolean sortAnnots = true;
  
  /** A change in highlight visibility will set this untill the update had been
   * complete. This avoids multiple updates. */
  private boolean updateHighlightsScheduled = false;

  private Comparator annotLengthComparator = new AnnotLengthComparator();

  /**
   * Style to set the paragraph style (line height) all at once.
   * @see alignmentStyle
   */
  private Style paraStyle;
  /**
   * Style to set the alignment of the whole document at once. Anonymous (has
   * no name) so that it's name never overrides the name of the highlight
   * `style. Anonymous style's can not be looked up again, so we must keep
   * reference with this field.
   */
  private Style alignmentStyle;
  /**
   * Style to set the font family all at once.
   * @see alignmentStyle
   */
  private Style familyStyle;
  /**
   * Style to set the list of annotations covering an element of text.
   * @see alignmentStyle
   */
  private Style defaultStyle;

  /**
   * Unique name amongst documents in the parent frame, which reflects dirty
   * state
   * @see #getDisplayName
   * @see #getSessionID
   */
  private String displayName = null;
  private String fileName = null;
  private int sessionID = -1;

  /** If the JawbDocument was imported from a Workspace, this will be the
   *  document's basename, which should be used for display.  Otherwise it
   *  will be null
   */
  private String basename = null;
  
  /** Set to true when unsaved edits have been made */
  private boolean dirty = false;
  
  /** If false, new annotations won't be selected by {@link #finishCreatingAnnotation(AWBAnnotation, boolean)} */
  private boolean selectNewAnnotations = true;

  /** only used with Toocaan extensions */
  private String docMode = null;
  
  /***********************************************************************/
  /* CTORS & ACTIONS */
  /**********************************************************************/

  /**
   * Reparse an aif document reading the file.
   * @param task Task used to annotate this document.
   * @param aifURI saved ATLAS file which will be parsed for annotations and
   *               signal location
   */
  public static JawbDocument fromAIF (URI aifURI, Task task)
    throws IOException, BadLocationException {

    if (DEBUG > 0)
      System.err.println ("JD.fromAIF: uri="+aifURI+" task="+task);
    
    AWBDocument awbDoc = AWBDocument.fromAIF (aifURI, task);
    JawbDocument doc = new JawbDocument (awbDoc);
    doc.atlasURI = aifURI;
    
    return doc;
  }
  
  /**
   * Create a new document from a signal, reading the file
   * using the default encoding of UTF-8.
   * @param task Task used to annotate this document
   * @param signalURI raw signal file to be annotated
   */
  public static JawbDocument fromSignal (URI signalURI, Task task,
                                         String mimeType, String encoding)
    throws IOException, BadLocationException {

    if (DEBUG > 0)
      System.err.println ("JD.fromSignal: uri="+signalURI+" task="+task);

    AWBDocument awbDoc = AWBDocument.fromSignal (signalURI, task,
                                                 mimeType, encoding);
    JawbDocument doc = new JawbDocument (awbDoc);

    return doc;
  }
  /** @deprecated use {@link #fromSignal(URI,Task,String,String)} */
  public static JawbDocument fromSignal (URI signalURI, Task task,
                                         String encoding)
    throws IOException, BadLocationException {
    return fromSignal (signalURI, task, null, encoding);
  }
  
  /**
   * Open and convert a document from an external format. URI must be
   * absolute. All exceptions causing failure to save the file are caught and
   * displayed to the user as error messages.<p>
   *
   * @param externalURI an absolute URI to of the document in external form
   * @param awbDoc document which has been imported
   *
   * @deprecated Use importDocument() method instead.
   */
  public static JawbDocument fromImport (URI externalURI, AWBDocument awbDoc)
    throws IOException, BadLocationException {
    return JawbDocument.fromImport(externalURI, awbDoc, null);
  }

  public static JawbDocument fromImport (URI externalURI, AWBDocument awbDoc,
                                         String mode)
    throws IOException, BadLocationException {

    if (DEBUG > 0)
      System.err.println ("JD.fromImport: uri="+externalURI+
                          " signal="+awbDoc.getSignalLocation () +
                          " task="+awbDoc.getTask ());

    JawbDocument doc = new JawbDocument (awbDoc, mode);
    doc.externalURI = externalURI;
    
    return doc;
  }

  /**
   * Import a document from an external format. URI must be absolute.
   *
   * @param uri an absolute URI to of the document in a form understood by the
   * importer
   * @param importer mechanism which knows how to traslate the specified
   * document into an AWBDocument
   */
  public static JawbDocument importDocument (URI uri, Importer importer,
                                             String encoding)
    throws IOException, BadLocationException {

    AWBDocument awbDoc = importer.importDocument(uri, encoding);
    JawbDocument doc = new JawbDocument(awbDoc);
    doc.externalURI = uri;
    doc.importer = importer;
    
    return doc;
  }
  
  /**
   * Create a new document from a task and AWBDocument (guaranteed to have a
   * signal) specified, reading the signal using the default encoding of
   * UTF-8, and adding any know annotations from the aifURI
   *
   * @param awbDoc interface to all services
   */
  private JawbDocument (AWBDocument awbDoc)
    throws IOException, BadLocationException {
    this(awbDoc, null);
  }
  
  private JawbDocument (AWBDocument awbDoc, String mode)
    throws IOException, BadLocationException {

    this.awbDoc = awbDoc;
    this.task = awbDoc.getTask ();
    this.signalURL = awbDoc.getSignalLocation ();
    this.docMode = mode;

    if (DEBUG > 0)
      System.err.println ("JD<init>: Creating JawbDocument:"+
                          "\n\tsig="+signalURL);
    
    // create the styled document
    styledDocument = new DefaultStyledDocument () {
        // override so NO text is damaged, as all style updates are handled
        // manually anyway.
        protected void styleChanged(final Style style) {
          //Thread.dumpStack();
          //super.styleChanged(style);
        }
      };
    //styledDocument = new DefaultStyledDocument ();
    styledDocument.putProperty ("i18n", Boolean.TRUE);
    
    // add a styles for the family and alignment, these use null names so that
    // the highlight names are always kept for the various elements.
    paraStyle = styledDocument.addStyle (null, null);
    alignmentStyle = styledDocument.addStyle (null, paraStyle);
    familyStyle = styledDocument.addStyle (null, alignmentStyle);

    Preferences prefs = Jawb.getPreferences ();
    // set style based on last settings if desired
    if (prefs.getBoolean (Preferences.PARA_LAST_LINE_SPACING_ENABLED_KEY)) {
      float spacing = StyleConstants.getLineSpacing (paraStyle);
      spacing = prefs.getFloat (Preferences.PARA_LAST_LINE_SPACING_KEY, spacing);
      StyleConstants.setLineSpacing (paraStyle, spacing);
    }
    if (prefs.getBoolean (Preferences.FONTS_LAST_FAMILY_ENABLED_KEY)) {
      String family = StyleConstants.getFontFamily (familyStyle);
      family = prefs.getPreference (Preferences.FONTS_LAST_FAMILY_KEY, family);
      StyleConstants.setFontFamily (familyStyle, family);
    }
    if (prefs.getBoolean (Preferences.FONTS_LAST_SIZE_ENABLED_KEY)) {
      int size = StyleConstants.getFontSize (familyStyle);
      size = prefs.getInteger (Preferences.FONTS_LAST_SIZE_KEY, size);
      StyleConstants.setFontSize (familyStyle, size);
    }
    // get ShowUnknownHighlights key from preferences
    showUnknowns = prefs.getBoolean("task."+task.getName()+
				    ".highlights.<unknown>.visible",
				    true);

    // style for modifying lists of annotations spanning an element
    defaultStyle = styledDocument.addStyle (null, familyStyle);

    // read text and add using that style
    readSignal (styledDocument);
    
    // add styles and associate colors
    Style style =
      styledDocument.addStyle (Preferences.UNKNOWN_HIGHLIGHT_KEY, familyStyle);
    ColorSpec defaultColors =
      prefs.getColorSpec (Preferences.UNKNOWN_HIGHLIGHT_KEY, new ColorSpec());
    StyleConstants.setBackground (style, defaultColors.getBackground ());
    StyleConstants.setForeground (style, defaultColors.getForeground ());

    // styles from the Task
    namespace = "task."+task.getName()+".";
    Set keys = task.getHighlightKeys ();
    Iterator keyIter = keys.iterator ();
    while (keyIter.hasNext ()) {
      String key = (String)keyIter.next();
      if (DEBUG > 1)
        System.err.println ("JD<init>: Adding style key: "+key+
                            " prefKey="+namespace+key);
      style = styledDocument.addStyle (key, familyStyle);
      ColorSpec colors = prefs.getColorSpec (namespace+key);
      if (colors == null) {
        System.err.println ("JD<init>: No color for pref key: "+namespace+key);
        colors = defaultColors;
      }
      if (DEBUG > 1)
        System.err.println("JD<init>: setting colors for " + key + " to " +
                           colors);
      StyleConstants.setBackground (style, colors.getBackground());
      StyleConstants.setForeground (style, colors.getForeground());
    }

    // insert the annotations already known
    if (DEBUG > 0)
      System.err.println ("JD<init>: initial annotations");
    
    Iterator iter = awbDoc.getAllAnnotations();
    while (iter.hasNext()) {
      Annotation annot = (Annotation)iter.next();
      if (DEBUG > 1)
        System.err.println ("          adding:"+annot);
      if (annot instanceof TextExtentRegion || annot instanceof NamedExtentRegions)
        finishCreatingAnnotation ((AWBAnnotation) annot, false);
    }

    // before updating highlights, set current priority for TooCAAn tasks
    /************* no longer needed
    if (task instanceof ToocaanTask)
      ((ToocaanTask)task).setCurrentPriority(this);
    ************/

    if (sortAnnots)
      updateHighlights(0, styledDocument.getLength(), null, true);

    // now that all is successful, add listeners for changes
    pcListener = new PreferenceChangeListener ();
    amListener = new AnnotModelListener ();
    prefs.addPropertyChangeListener (pcListener);
    awbDoc.addAnnotationModelListener (amListener);

    // TODO: undoable edit events
  }

  /**
   * Read the signals stream, populating the specified
   * StyledDocument. Huristics are used to see which font is best suited to
   * display the text, and the display orientation to use (LEFT_TO_RIGHT
   * vs. RIGHT_TO_LEFT). The styled document and orientation are set in the
   * JawbDocument.
   * @throws IOException if an IO exception occurs while reading.
   */
  private void readSignal (DefaultStyledDocument doc) 
    throws IOException {

    String docString = awbDoc.getSignal ().getCharsAt (0);
    
    // decide best font, alignment, and orientation
    if (DEBUG > 0)
      System.err.print ("JD.readSignal: Checking Font: ");
    FontSupport fs = new FontSupport
      (docString.substring (0, Math.min (docString.length(), 500)));
    fs.addFontFamily (null);

    String family;
    String oldFamily = getFontFamily ();
    Preferences prefs = Jawb.getPreferences ();
    
    // autodetect for the common font's specified in prefs. ok if font not
    // installed
    if (! prefs.getBoolean (Preferences.FONTS_LAST_FAMILY_ENABLED_KEY)) {
      String autoDetected =
        prefs.getPreference (Preferences.FONTS_AUTO_DETECTED_KEY, "Default");
      StringTokenizer st = new StringTokenizer (autoDetected, ",");
      while (st.hasMoreTokens())
        fs.addFontFamily (st.nextToken());
      
      // autodetect and fill in the styledDocument
      family = autoDetect (fs);
    } else {
      setComponentOrientation(fs.getOrientation());
      family = StyleConstants.getFontFamily (familyStyle);
    }
    try {
      StyleConstants.setFontFamily (familyStyle, family);
      //doc.setCharacterAttributes (0, doc.getLength(),
      //                            familyStyle, false);
      doc.replace (0, doc.getLength (), docString, familyStyle);
      doc.setParagraphAttributes (0, doc.getLength (), paraStyle, false);
      support.firePropertyChange (FONT_FAMILY_PROPERTY_KEY, oldFamily, family);
    } catch (BadLocationException impossible) {
      throw new RuntimeException (impossible);
    }
  }

  public void setLogAnnotationCreation (boolean b) {
    logAnnotationCreation = b;
  }

  public void autoDetectFontAndOrientation () {
    try {
      DefaultStyledDocument doc = styledDocument;
      String family =
        autoDetect (new FontSupport (doc.getText (0, doc.getLength ())));
      setFontFamily(family);
    } catch (BadLocationException impossible) {
      throw new RuntimeException (impossible);
    }
  }

  /** Does the work of autodetecting the font, and orientation, but returns
   * the Font Family name used
   */
  private String autoDetect (FontSupport fs) {
    Font font = new Font (null, Font.PLAIN, 1);
    String family = "Default";
    if (fs.getCoverageBy ((String)null) < .97) {
      if (DEBUG > 0)
        System.err.print (" got="+fs.getCoverageBy ((String)null)+"   Searching: ");
      FontSupport.FontScore fscore = fs.rankFamilies ()[0];
      family = fscore.getFamily ();
    }
    if (DEBUG > 0)
      System.err.println ("   Accepted: "+family);
    setComponentOrientation (fs.getOrientation ());
    return family;
  }

  /** Release memory and resources from ATLAS for the backing Corpus. */
  public void close () {
    Preferences prefs = Jawb.getPreferences ();
    prefs.removePropertyChangeListener (pcListener);
    awbDoc.removeAnnotationModelListener (amListener);
    awbDoc.close ();
  }
  
  
  /**
   * Saves annotation changes to the ATLAS URI.
   *
   * @return true if file was saved, or was not dirty in the first
   * place. false if the atlasURI was unset 
   * @see  #getAtlasURI
   * @see  #isDirty
   *
   * @throws IOException after some failure
   */
  public boolean save () throws IOException {
        
    if (DEBUG > 0)
      System.err.println ("JD.save:"+
                          "\n\t   sig="+signalURL+
                          "\n\t   aif="+atlasURI+
                          "\n\tbadURI="+URLUtils.uriToBadURL (atlasURI));
    if (!dirty)
      return true;

    if (atlasURI != null && awbDoc.save (atlasURI, true)) {
      setDirty (false);
      return true;
    }
    return false;
  }
  
  /**
   * Save annotation changes to the ATLAS URI specified. URI will be stored,
   * and future calls to {@link #save} will be saved there too.
   *
   * @throws IOException after some failure
   */
  public boolean save (URI uri) throws IOException {
    setDirty (true);

    String oldName = getName ();
    URI oldURI = atlasURI;

    // causes regeneration of displayName at next request
    displayName = fileName = null;
    atlasURI = uri;

    support.firePropertyChange (ATLAS_URI_KEY, oldURI, uri);
    // the Document manager will catch the name change event and set a new
    // session ID. This won't even fire if old.equals(new)
    support.firePropertyChange (NAME_KEY, oldName, getName ());
    
    return save ();
  }

  /**
   * Export annotations using the specified exporter.<p>
   *
   * @throws NullPointerException if exporter is null
   */
  public boolean export (Exporter exporter, URI uri) throws IOException {
    // TODO: URI should be stored and some mechanism to quick export should be
    // implemented. not just 'save' though since we still want to allow
    // storing in AIF.
    if (DEBUG > 0 && exporter == null)
      System.err.println("JawbDoc.export called with null exporter");
    if (DEBUG > 0 && awbDoc == null)
      System.err.println("JawbDoc.export called when awbDoc == null");

    if (uri != null && exporter.exportDocument(awbDoc, uri)) {
      String oldName = getName ();

      // causes regeneration of displayName at next request
      displayName = fileName = null;
      externalURI = uri;

      // the Document manager will catch the name change event and set a new
      // session ID. This won't even fire if old.equals(new)
      support.firePropertyChange (NAME_KEY, oldName, getName ());

      setDirty (false);
      return true;
    }
    return false;
  }

  public void setImporter(Importer i) {
    importer = i;
    // fire an event?
  }
  public Importer getImporter() {
    return importer;
  }

  public void setExporter(Exporter e) {
    exporter = e;
    // fire an event?
  }
  public Exporter getExporter() {
    return exporter;
  }
  
  /**
   * Revert a file to it's last saved state.
   */
  public void revert () {
    // was data imported or loaded from aif
    if (dirty) {
      // TODO: remove undoable edits
      // TODO: AACK! it's not this easy, remember: the signal doesn't change
      // annotations do!
      //   readSignal (styledDocument);
      //   dirty = false;
      throw new UnsupportedOperationException ("not yet implemented!");
    }
  }

  /***********************************************************************/
  /* HIGHLIGHTS */
  /***********************************************************************/

  /**
   * This method traverses every content element and reapplies any style that
   * matches the style of the element. This will not affect the value of
   * ANNOTATION_ATTRIBUTE, thus location and ordering of Annotations is
   * maintained.
   */
  private void reapplyHighlight (Style style) {
    if (DEBUG > 2)
      System.err.println ("JD.reappHL: style="+style.getName());
    // Get section element
    Element root = styledDocument.getDefaultRootElement();
    
    for (int i=root.getElementCount()-1; i>=0; i--) {
      Element para = root.getElement(i);
      
      // Enumerate the content elements
      for (int j=para.getElementCount()-1; j>=0; j--) {
        Element element = para.getElement(j);
        AttributeSet attr = element.getAttributes();
        
        // Get the name of the style applied to this content element; may be null
        String sName = (String)attr.getAttribute(StyleConstants.NameAttribute);
        
        // Check if style name match
        if (style.getName().equals(sName)) {
          // Reapply the content style
          int start = element.getStartOffset();
          int length = element.getEndOffset() - start;
	  if (DEBUG > 3)
	    System.err.println("JD.reappHL: start=" + start + 
			       " length=" + length);
          styledDocument.setCharacterAttributes (start, length, style, false);
        }
      }
    }
  }

  /**
   * Highlight, or update the highlight of an annotation if it has text
   * extent, by setting character attributes in the StyledDocument.<p>
   * 
   * Currently handles TextExtentRegions and NamedExtentRegions<p>
   *
   * <h4>Start of docs to explain using list of annots as attribute</h4>
   * <ul>
   *   <li>Document Elements (java style) cannot overlap.
   *   <li>when an overlapping element is creeated, three elments result
   *   <li>search for all tags at point, requires in O(n) search
   *   <li>implementing our own EditorKit and or Document would eliminate
   * reuse of other implementations of those classes (like HTMLEditorKit, etc)
   *   <li>
   * </ul>
   * drawbacks
   * <ul>
   *   <li>Documents wouldn't understand that we need to append list from one
   * attribute to the list of another, so adding annots to a list requires
   * that we do it manually using Elment and MutableAttributeSet API.
   *   <li>
   * </ul>
   *
   * <h4>What I'm doing</h4>
   */

  /**
   * Update the color highlights and 'annotations' list of the specified
   * region. If annot is non-null, it will be forced to the top or added to
   * the region. If it is non-null and 'remove' is true, it will be removed
   * and the top remaining annot used to highlight (or highlight cleared)
   *
   * TODO: Carefull kiddies.. there's a LOT of hackish stuff that takes
   * advantage of the fact that NamedExtentsRegions currently always have two
   * extents, head and full, and head will always end up nested within full
   * (even if for a brief moment, while updating, it is not). All in the name
   * of speed. Someone more clever than I shall have the pleasure of
   * optomizing the general case.
   *
   * @param start
   * @param end
   * @param annot
   * @param keepAnnot remove annot from range if false
   */
  private void updateHighlights (int start, int end, AWBAnnotation annot,
                                 boolean keepAnnot) {
    if (DEBUG> 1)
      System.err.println ("JD.updateHighlights: ("+start+","+end+") "+
                          (annot==null?"null":annot.getId().toString())+" "+
                          (keepAnnot?"keep":"drop"));
    if (start < 0 || end < 0) {
      if (DEBUG > 1)
        System.err.println ("JD.updateHighlights: ("+start+","+end+
                            "): bad range");
      return;
    }

    // to see if highlights are enabled
    Preferences prefs = Jawb.getPreferences();

    // caching for MultpleExtentRegions
    AWBAnnotation lastElementsAnnot = null;
    String extents[]  = new String[] {"head","full"};
    int extentStart[] = new int[2];
    int extentEnd[]   = new int[2];
    
    StyledDocument sd = getStyledDocument();
    Element element;
    int current = start;
    int length = 0;

    // step through each element in the range. If an element is being forced
    // (added or raised) then make sure it gets put on top of the annotation
    // list.  For each element, set the color attributes for either the forced
    // element or the topmost one.
    for ( ; current < end; current += length) {
      element = sd.getCharacterElement (current);
      int currentEnd = Math.min (end, element.getEndOffset());
      length = currentEnd - current;

      // lovely: if a text signal is converted from CRLF to CR new-lines, and
      // that causes a tag to now be /outside/ the document (since it is
      // shorter) the range being updated could be [n,n+x] whereas the current
      // element will be [n-s,n-t] (the last element of the doc). This will
      // manifest as an infinite loop because the length gets set to 0, and
      // 'current' cannot advance.
      if (length == 0) {
        String msg = "Low level error (zero length element!).\n"+
          "This may indicate a bad annotation who's span is outside the\n"+
          "signal. This could have been caused by converting a text\n"+
          "signal from DOS to UNIX style new-lines.";
        throw new IllegalArgumentException (msg);
      }
      
      if (DEBUG > 2) {
        System.err.print ("      updating ("+start+","+end+") sub-elt=");
        dumpElement (element);
        System.err.println("("+current+","+(current+length)+")");
        
        System.err.println ();
      }
      
      MutableAttributeSet eltAttribs = (MutableAttributeSet)
        element.getAttributes ();
      
      // this is a list of the Annotations at the current location
      LinkedList annotList = (LinkedList)
        eltAttribs.getAttribute (ANNOTATION_ATTRIBUTE);

      // if we're told to work with a specific annot, get ready
      if (annot != null) {
        // must copy so that if an Element is result of splitting others, a
        // modification to one half doesn't modify both
        if (annotList != null) {
          if (end < element.getEndOffset() || keepAnnot)
            annotList = new LinkedList (annotList);
          annotList.remove (annot); // neccissary to move to top anyway
        }
        if (keepAnnot) {
          if (annotList == null)
            annotList = new LinkedList ();
	  // if we are supposed to be raising it, only do so if it has
	  // a non-null highlightKey or we are supposed to show unknowns
          Object constraint;
          if (task instanceof ToocaanTask)
            constraint = this;
          else
            constraint = null;
	  if (showUnknowns || task.getHighlightKey(annot,constraint) != null) {
	    annotList.add (0, annot); // only allowed when adding/raising
	  }
        } 
      } else {
        // if we're not told to work with a specific annot, order
        // annotList by length
        if (sortAnnots && annotList != null) {
          if (DEBUG > 3)
            System.err.println("JD.updateHL: sorting annotList");
          Collections.sort(annotList, annotLengthComparator);
        }
      }

      // **********
      // if nothing left in element, clear highlights
      if (annotList == null || annotList.isEmpty ()) {
        
        if (annotList == null )
          defaultStyle.removeAttribute (ANNOTATION_ATTRIBUTE);
        else 
          defaultStyle.addAttribute (ANNOTATION_ATTRIBUTE, annotList);

        // set the annotList of our reusable style to the (empty) list,
        // already in this element, (keeping fontFamily,Alignment, etc...)
        // then set the attributes, clearing color info
        sd.setCharacterAttributes (current, length, defaultStyle, true);
        if (DEBUG > 1)
          System.err.println ("    -- All cleared!");
        continue;
      }

      // **********
      // retrieve the style key for for the annot
      // loop until a non clear one is found

      String styleKey = null;
      AWBAnnotation topAnnot = null;

      Iterator iter = annotList.iterator();
      while (iter.hasNext()) {
        topAnnot = (AWBAnnotation) iter.next();

        if (topAnnot instanceof TextExtentRegion) {          // easy case
          Object constraint;
          if (task instanceof ToocaanTask)
            constraint = this;
          else
            constraint = null;
          styleKey = task.getHighlightKey (topAnnot, constraint);
          if (DEBUG > 2) {
            System.err.println("JD.updateHighlights got HK: " + styleKey +
                               " for " + topAnnot);
          }
        } else if (topAnnot instanceof NamedExtentRegions) { // difficult case
          int i;
          if (topAnnot != lastElementsAnnot) {
            // ordered so that as soon as one is found, break out of
            // loop. fills all extents before checking, so that it can be
            // cached
            lastElementsAnnot = topAnnot;
            NamedExtentRegions ner = (NamedExtentRegions)topAnnot;
            for (i=0; i<2; i++) {
              extentStart[i] = ner.getTextExtentStart (extents[i]);
              extentEnd[i]   = ner.getTextExtentEnd (extents[i]);
            }
          }
          for (i=0; i<2; i++) {
            if (current >= extentStart[i] && current < extentEnd[i])
              break;
          }
          if (i==2) {
            System.err.println ("ERROR: Annot has no extent in Element range");
            continue; // next element rather than death.
          }
          // if the extent breaks midway through element, we need to as well
          if (extentEnd[i] < currentEnd) {
            currentEnd = extentEnd[i];
            length = currentEnd - current;
          }
          // TODO: HACK: works around other hacks that rely on the fact that
          // NamedExtentRegions will always have 'head' nested w/in 'full'
          // if the head starts midway through this full, we need to end there
          if (i==1 && extentStart[0] > current && extentStart[0] < currentEnd) {
            currentEnd = extentStart[0];
            length = currentEnd - current;
          }
          styleKey = task.getHighlightKey (topAnnot, extents[i]);
	
        
        } else {                                          // painfull case
          throw new IllegalArgumentException ("annot is unrecognized: '"+
                                              topAnnot+"'");
        }

        if (styleKey == null)
          styleKey = Preferences.UNKNOWN_HIGHLIGHT_KEY;

        String visibleKey = "task."+task.getName()+"."+styleKey+".visible";
        if (DEBUG > 2)
          System.err.println(" visiblekey="+visibleKey+ " = " +
                             prefs.getBoolean(visibleKey, true));
      
        // if stylekey is visible, then we've got our style
        if (prefs.getBoolean(visibleKey, true)) {
          break;
        }
        
        // otherwise, keep looking
        styleKey = null;
      } // while

      if (DEBUG > 2)
        System.err.println(" styleKey="+styleKey);
      
      
      // Individual styles were put in the document when first
      // created. If a style key isn't found... someone must have added the
      // ability to create dynamic styles!!! yay! (so now fix this)
      Style style = null;
      if (styleKey != null)
	style = styledDocument.getStyle (styleKey);
      if (style == null) {
        if (DEBUG > 2)
          System.err.println ("      No highlight found in '"+
                              task.getTitle()+"' for annot '"+topAnnot.getId()+
                              " styleKey="+styleKey);
          defaultStyle.addAttribute(ANNOTATION_ATTRIBUTE, annotList);

        // set the annotList of our reusable style to the (empty) list,
        // already in this element, (keeping fontFamily,Alignment, etc...)
        // then set the attributes, clearing color info
        sd.setCharacterAttributes (current, length, defaultStyle, true);
        // We're not to show it. Next element
        continue;
      }
      if (DEBUG > 2)
        System.err.println ("      key="+styleKey+" style="+style);

      // make sure annotlist for element accurately reflects any
      // addition/reorder/removal and highlight style by setting them in our
      // default style, since defaultStyle backs all color styles, we can then
      // just set the attributes using style
      // momentarily set the annotlist on 'style' for just this range
      style.addAttribute (ANNOTATION_ATTRIBUTE, annotList);
      if (DEBUG > 2)
	System.err.println("JD.updHL: setCharacterAttributes from: " +
			   current + " length=" + length);
      sd.setCharacterAttributes (current, length, style, true);
      style.removeAttribute (ANNOTATION_ATTRIBUTE);
    }
  }

  /** Debugging only */
  private static void dumpElement (Element elt) {
    if (DEBUG > 3) {
    System.err.print ("[element("+
                      elt.getStartOffset()+","+
                      elt.getEndOffset()+") ");
    AttributeSet att = elt.getAttributes();
    System.err.println (ANNOTATION_ATTRIBUTE+"="+
                      att.getAttribute (ANNOTATION_ATTRIBUTE)+"]");
    } else {
      System.err.print ("["+elt.getStartOffset()+
                        ","+elt.getEndOffset()+"] ");
    }
  }


  /***********************************************************************/
  /* Ligature  */
  /***********************************************************************/

  /**
   * If there the specified point is, or could be part of a ligature, toggle
   * rendering between the one (ligature) and two glyph representations.
   */
  public void toggleLigature (int offset) {
    DefaultStyledDocument sd = (DefaultStyledDocument) getStyledDocument();
    Element element = sd.getCharacterElement (offset);
    AttributeSet attributes = element.getAttributes();

    // single char element means possibly already split
    if (element.getEndOffset() - element.getStartOffset() == 1) {

      int start = element.getStartOffset()-1;
      int end = element.getEndOffset()+1;
      removeLigatureBreak(start, end);

      // trigges redraw of selection, which could look pretty nasty by now
      if (! annotSelectionModel.isEmpty()) {
        LinkedHashSet selected = new LinkedHashSet(annotSelectionModel);
        annotSelectionModel.clear();
        annotSelectionModel.addAll(selected);
      }      
    } else {
      // force the Element to be broken.
      sd.setCharacterAttributes(offset, 1, attributes, false);
    }
  }

    /**
     * Recombine the underlying view elements in the specified range to remove
     * ligatures. Call this with annotation.start()+1, annotation.end()+1' to
     * combine ligatures a the end points.
     */
  public void removeLigatureBreak(int begin, int end) {
    DefaultStyledDocument sd = (DefaultStyledDocument) getStyledDocument();

    int offset = Math.max(begin, 0);
    end = Math.min(end, sd.getLength());

    // walk the elements in the range, re-inserting any adjacent ones with the
    // same attributes, to create a contiguous element, which will rejoin
    // ligatures. Setting the entire spans attributes all at once (even with
    // 'replace=true') doesn't work.

    Element element = sd.getCharacterElement (offset);
    
    for (offset = element.getEndOffset();
         offset < end;
         offset = element.getEndOffset()) {

      AttributeSet attributes = element.getAttributes();

      Element adjacent = sd.getCharacterElement(offset);
      AttributeSet adjAttributes = adjacent.getAttributes();

      Object eltAnnots = attributes.getAttribute(ANNOTATION_ATTRIBUTE);
      Object adjAnnots = adjAttributes.getAttribute(ANNOTATION_ATTRIBUTE);

      // if annots of the elements are equal, merge them using same attribs
      if (eltAnnots==null ? adjAnnots==null : eltAnnots.equals(adjAnnots)) {
        try {
          int start = element.getStartOffset();
          int len = adjacent.getEndOffset() - start;
          sd.replace(start, len, sd.getText(start, len), attributes);
        } catch (BadLocationException ignore) {}
      }
      
      element = sd.getCharacterElement(offset);
    }
  }


  /***********************************************************************/
  /* ANNOTATIONS (makes heavy use of highlights) */
  /***********************************************************************/

  /**
   * Get an unmodifiable list of annotaions at the specified location in the
   * document. The List is ordered from top to bottom.
   */
  public List getAnnotationsAt (int i) {
    StyledDocument sd = getStyledDocument();
    Element element = sd.getCharacterElement (i);
    MutableAttributeSet eltAttribs = 
      (MutableAttributeSet) element.getAttributes ();
    
    List annotList = (List)eltAttribs.getAttribute (ANNOTATION_ATTRIBUTE);
    if (annotList == null)
      return Collections.EMPTY_LIST;
    return Collections.unmodifiableList (annotList);
  }

  /**
   * Brings an annotations to front, it's highlight overriding any which it
   * overlaps.
   */
  public void raiseAnnotation (AWBAnnotation annot) {
    if (DEBUG > 1)
      System.err.println ("JD.raise: "+annot);
    if (DEBUG>5)
      Thread.dumpStack();

    int start, end;
    if (annot instanceof TextExtentRegion) {
      start = ((TextExtentRegion)annot).getTextExtentStart ();
      end   = ((TextExtentRegion)annot).getTextExtentEnd ();
      
    } else if (annot instanceof NamedExtentRegions) {
      // TODO: stop hard coding for RDC!
      start = ((NamedExtentRegions)annot).getTextExtentStart ("full");
      end   = ((NamedExtentRegions)annot).getTextExtentEnd ("full");

    } else {
        if (DEBUG > 1) // in future there may be sub regions un annotated
          System.err.println ("  Unrecognized Annotation: "+annot);
      return;
    }
    updateHighlights (start, end, annot, true);
  }

  public void refreshRegion (int start, int end) {
    updateHighlights(start, end, null, true);
  }

  /** another version that allows start/end to be passed in in case
   * they are known, for efficiency, or in case they have been futzed
   * with, OR to handle different sub-regions not handled in the
   * version above
   */
  public void raiseAnnotation (AWBAnnotation annot, int start, int end) {
    if (DEBUG > 1)
      System.err.println ("JD.raise: " + annot + " start=" + start +
			  " end=" + end);
    updateHighlights (start, end, annot, true);
  }

  /**
   * Create a tag, and add it to the StyledDocument and it's selected tags, if
   * supported. Avoid this... it may be removed...
   * @see #createAnnotation (AnnotationType)
   */
  public AWBAnnotation createAnnotation (String type) {
    return createAnnotation (task.getAnnotationType (type));
  }

  /**
   * Create a tag, and add it to the StyledDocument and it's selected tags, if
   * supported.
   * @see #createAnnotation (AnnotationType, int, int)
   */
  public AWBAnnotation createAnnotation (AnnotationType type) {
    if (DEBUG > 1)
      System.err.println ("JD.createTag("+type.getName()+")");
    
    AWBAnnotation newAnnot = awbDoc.createAnnotation (type);
    logCreation (newAnnot, type.getName());
    return newAnnot;
  }

  /**
   * Create a tag, and add it to the StyledDocument and its selected tags, if
   * supported, unless select parameter is false.
   * @see #createAnnotation(AnnotationType)
   */
  // TODO verify that no annotations can be created while the call to
  // awbDoc.createAnnotation is pending. Should be okay, as long as everyone
  // only creates annotations on the event dispatch thread.
  public AWBAnnotation createAnnotation (AnnotationType type, boolean select) {
    if (DEBUG > 1)
      System.err.println ("JD.createTag("+type.getName()+", "+select+")");
    boolean oldSelect = selectNewAnnotations;
    try {
      if (oldSelect != select) {
        selectNewAnnotations = select;
      }
      AWBAnnotation newAnnot = awbDoc.createAnnotation (type);
      logCreation (newAnnot, type.getName());
      return newAnnot;
    }
    finally {
      if (oldSelect != select)
        selectNewAnnotations = oldSelect;
    }
  }
  
  /**
   * Create a tag, and add it to the StyledDocument and it's selected tags, if
   * supported.
   * @see #createAnnotation (AnnotationType)
   *
   * @param type object indicating type of annotation to create
   * @param start beginning offset
   * @param end end offset
   * @param mainExtentRole - only for mentions. pass in null if not needed
   */
  public AWBAnnotation createAnnotation (AnnotationType type,
                                         int start, int end, 
					 String mainExtentRole) {
    if (DEBUG > 1)
      System.err.println ("JD.createTag("+type.getName()+
                          ", "+start+", "+end+")");
    
    AWBAnnotation newAnnot =
      awbDoc.createAnnotation(type, start, end, mainExtentRole);
    logCreation(newAnnot, type.getName());
    return newAnnot;
  }

  private void logCreation (AWBAnnotation newAnnot, String type) {
    if (!logAnnotationCreation)
      return;

    if (newAnnot == null) {
      Jawb.getLogger().info(JawbLogger.LOG_CREATE_ANNOT_FAIL, 
                            new Object[] {type});
    } else {
      Jawb.getLogger().info(JawbLogger.LOG_CREATE_ANNOT, 
                            new Object[] {type, 
                                          newAnnot.getId().getAsString()});
    }
  }

  /**
   * Things need to be done for all text extent annotations added to the
   * document, irregardless of means of getting here, so not called directly
   * from the 'createAnnotation' methods, but rather as a result of getting an
   * annotation from the AnnotationModel.
   */
  private void finishCreatingAnnotation (final AWBAnnotation annot, boolean select) {
    if (annot instanceof TextExtentRegion ||
        annot instanceof NamedExtentRegions) {
      if (DEBUG > 1)
        System.err.println ("JD.AnnotCreated: "+annot);

      textRangeTracker.add (annot);

      if (select) {
        // once the action which created the annotation has completed, we can
        // select it... add 'select it' to the queue of GUI tasks
        Runnable selectNewestAnnot = new Runnable() {
            public void run() {
              if (DEBUG > 1)
                System.err.println ("JD.selectingNewestAnnot: "+annot);
              unselectAllAnnotations ();
              if (DEBUG > 1)
		  System.err.println("selectAnnotation annot = " + annot);
              selectAnnotation (annot);
            }
          };
        SwingUtilities.invokeLater(selectNewestAnnot);
      }
    }
  }


  /**
   * Delete an annotation from the corpus.
   * TODO: Specify the fall out of deleting an Annotation probably want a
   * couple methods.
   */
  public boolean deleteAnnotation (AWBAnnotation annot) {
    if (DEBUG > 1)
      System.err.println ("JD.deleteAnnotation calls AWBDoc.deleteAnnotation");

    // TODO -- for now passing in false for doSubs -- decide what to do when
    return awbDoc.deleteAnnotation (annot, false);
  }

  /***********************************************************************/
  /* Comparator to sort annotations according to length
  /***********************************************************************/

  // TODO must handle NamedExtentRegions too!

  private class AnnotLengthComparator implements Comparator {
    /** 
     * Compares its two TextExtentRegion arguments for length. Returns
     * a negative integer, zero, or a positive integer as the first
     * argument is shorter than, equal in length and location to, or
     * longer than the second.  For annotations equal in length,
     * returns a negative integer, zero, or a positive integer as the
     * first argument's start location is less than, equal to, or
     * greater than the second argument's start location.
     *
     * Note: this comparator imposes orderings that are inconsistent
     * with equals.  If o1.equals(o2) then compare(o1, o2) == 0 BUT
     * compare (o1, o2) may equal zero even when !o1.equals(o2) if
     * they have identical lengths and starting locations.
     */
    public int compare(Object o1, Object o2) {
      int start1, start2, len1, len2;
      if (o1 instanceof TextExtentRegion) {
        TextExtentRegion annot1 = (TextExtentRegion) o1;
        start1 = annot1.getTextExtentStart();
        len1 = annot1.getTextExtentEnd() - start1;
      } else if (o1 instanceof NamedExtentRegions) {
        NamedExtentRegions annot1 = (NamedExtentRegions) o1;
        // NOTE: hard-coded assumption of a "full" text extent
        start1 = annot1.getTextExtentStart("full");
        len1 = annot1.getTextExtentEnd("full") - start1;
      } else {
        throw new IllegalArgumentException("AnnotLengthComparator.compare must be passed TextExtentRegion or NamedExtentRegions Annotations");
      }
      if (o2 instanceof TextExtentRegion) {
        TextExtentRegion annot2 = (TextExtentRegion) o2;
        start2 = annot2.getTextExtentStart();
        len2 = annot2.getTextExtentEnd() - start2;
      } else if (o2 instanceof NamedExtentRegions) {
        NamedExtentRegions annot2 = (NamedExtentRegions) o2;
        // NOTE: hard-coded assumption of a "full" text extent
        start2 = annot2.getTextExtentStart("full");
        len2 = annot2.getTextExtentEnd("full") - start2;
      } else {
        throw new IllegalArgumentException("AnnotLengthComparator.compare must be passed TextExtentRegion or NamedExtentRegions Annotations");
      }

      if (len1 != len2)
        return (len1 - len2);
      return start1 - start2;
    }
  }
      
  
  /***********************************************************************/
  /* Handle modifications to the corpus */
  /**********************************************************************/

  /** Listen for changes to the annotations so we can update the display */
  private class AnnotModelListener implements AnnotationModelListener {

    /** Invoked after an annotation has been created. */
    public void annotationCreated (AnnotationModelEvent e) {
      finishCreatingAnnotation (e.getAnnotation (), selectNewAnnotations);
      setDirty (true);
    }
    
    /** Invoked after an annotation has been deleted. */
    public void annotationDeleted (AnnotationModelEvent e) {
      AWBAnnotation annot = e.getAnnotation ();
      if (DEBUG > 1)
        System.err.println("JD.AnnotModList.annotDeleted " + annot);
      unselectAnnotation (annot);
      textRangeTracker.remove (annot);
      setDirty (true);
      
      int start, end;
      if (annot instanceof TextExtentRegion) {
        TextExtentRegion ter = (TextExtentRegion)annot;
        start = ter.getTextExtentStart ();
        end   = ter.getTextExtentEnd ();
      
      } else if (annot instanceof NamedExtentRegions) {
        // TODO: stop hard coding for RDC!
        NamedExtentRegions ner = (NamedExtentRegions)annot;
        start = ner.getTextExtentStart ("full");
        end   = ner.getTextExtentEnd ("full");

      } else {
        // we'll see many changes of annots that aren't textual, just ignore
        return;
      }
      System.err.println("JD.AnnotModelListener.annotDeleted skip removeLigBreak");
      //removeLigatureBreak(start -1, end+1);


      updateHighlights (start, end, null, true);
    }
    
    /** Invoked after an annotation has been changed. */
    public void annotationChanged (AnnotationModelEvent e) {
      // TODO: the whole textRangeTracker stuff belongs here
      setDirty (true);
      int start, end;
      AWBAnnotation annot = e.getAnnotation ();
      if (DEBUG > 1)
        System.err.println("JD.AnnotModList.annotChanged " + annot);
      if (annot instanceof TextExtentRegion) {
        TextExtentRegion ter = (TextExtentRegion)annot;
        start = ter.getTextExtentStart ();
        end   = ter.getTextExtentEnd ();
      
      } else if (annot instanceof NamedExtentRegions) {
        // TODO: stop hard coding for RDC!
        NamedExtentRegions ner = (NamedExtentRegions)annot;
        start = ner.getTextExtentStart ("full");
        end   = ner.getTextExtentEnd ("full");

      } else {
        // we'll see a lot of changes of annots that aren't textual, just ignore
        return;
      }
      updateHighlights (start, end, null, true);
    }
    
    /** Invoked after an annotation has had subannotations added. */
    public void annotationInserted (AnnotationModelEvent e) {
      // TODO: This should call updateHighlight in case the highlight key is
      // changed by the addition of subannotations
      setDirty (true);
    }
    
    /** Invoked after an annotation has had subannotations removed. */
    public void annotationRemoved (AnnotationModelEvent e) {
      // TODO: This should call updateHghlight in case the highlight key is
      // changed by the addition of subannotations
      setDirty (true);
    }
  }
  
  /***********************************************************************/
  /* JawbDocument Property Change Notification */
  /**********************************************************************/

  /** This object does have some bound properties */
  private SwingPropertyChangeSupport support =
    new SwingPropertyChangeSupport (this);
  
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
  
  /***********************************************************************/
  /* List Data (for selection) */
  /**********************************************************************/

  LinkedHashSetModel annotSelectionModel = new LinkedHashSetModel ();

  /**
   * Listen to the list model to know which annotations are 'selected' at any
   * given time. For more information on using the list model, see <a href=
   * "http://java.sun.com/docs/books/tutorial/uiswing/components/list.html"
   * >How to Use Tables</a> as the ListModel is the same as used by the {@link
   * JList} object (the data model, not JLists's selectioin model)<p>
   * 
   * Do <i>not</i> have your listener modify the selection in anyway. If this
   * can't be avoided, remove your listener from the model before changing the
   * selection, then add it back once your seelection is complete. This will
   * avoid an infinite loop.<p>
   *
   * WARNING: an unfortunate side effect of using ListModel is that when an
   * item is removed from the list, a notification is sent out that #n was
   * removed... but it's no longer there, so you can never tell _which_ item
   * was removed, with maintaining your own version of the list<br>
   *
   * TODO: what's wrong with the annotSelectionModel?<p>
   *
   *
   * For now, the MainTextPane will pay attention only to text based
   * annotations. Perhaps there's a way to specify different behaviors for
   * each task. (rather than subclassing/reimplementing...)<p>
   *
   * Some Tasks will want to specify a single annotation be 'selected' so that
   * other tools can derive what that means.  If the selected entity has
   * subordinate annotations, perhaps those are displayted in a particular
   * component, but what is this tractable?<p>
   *
   * How might a compoenent specify some information to one tool, but other
   * information to another? allow multiple selections models (type based
   * perhaps)? blech!
   */
  public SetModel getSelectedAnnotationModel () {
    return annotSelectionModel;
  }

  /**
   * Add the specified annotation to the selection model
   */
  public void selectAnnotation (AWBAnnotation annot) {
    // System.err.println ("JD.selectAnnotation " + annot);
    // Thread.dumpStack();
    annotSelectionModel.add (annot);
  }

  private LinkedHashSet deferredUnselects = new LinkedHashSet();

  public void unselectAllAnnotationsDeferSort() {
    if (sortAnnots) {
      deferredUnselects.addAll(annotSelectionModel);
    }
    annotSelectionModel.clear();
  }

  public void unselectAllAnnotations () {
    // if we want to sort, and thus update highlights, do them one at
    // a time, so we only have to update highlights for the areas
    // where a selected annot was unselected, rather than for the
    // whole file every time.
    if (sortAnnots) {
      // have to copy the model because iterating directly through the
      // annotSelectionModel, removing entries one by one, would cause
      // a ConcurrentModificationException
      LinkedHashSet copyModel = new LinkedHashSet (annotSelectionModel);
      annotSelectionModel.clear();
      copyModel.addAll(deferredUnselects);
      Iterator selIter = copyModel.iterator();
      while (selIter.hasNext()) {
        updateHighlightsForUnselectedAnnot((AWBAnnotation)selIter.next());
      }
      deferredUnselects.clear();
    } else {
      annotSelectionModel.clear ();
    }
  }
  
  public void unselectAnnotation (AWBAnnotation annot) {
    annotSelectionModel.remove (annot);
    if (sortAnnots)
      updateHighlightsForUnselectedAnnot(annot);
  }

  private void updateHighlightsForUnselectedAnnot (AWBAnnotation annot) {
    int start;
    int end;
    if (annot instanceof TextExtentRegion) {
      TextExtentRegion ter = (TextExtentRegion)annot;
      start = ter.getTextExtentStart();
      end = ter.getTextExtentEnd();
    } else if (annot instanceof NamedExtentRegions) {
      NamedExtentRegions ner = (NamedExtentRegions)annot;
      start = ner.getTextExtentStart("full");
      end = ner.getTextExtentEnd("full");
    } else {
      return;
    }
    if (DEBUG > 1)
      System.err.println("JD.unselectAnnotation calling updateHL(" +
                         start + "," + end + ")");
    updateHighlights(start, end, null, true);
  }

  /**
   * Returns the annotation selected if there is only one, otherwise returns
   * null.
   */
  public AWBAnnotation getSingleSelectedAnnotation () {
    if (annotSelectionModel.size () == 1)
      return (AWBAnnotation) annotSelectionModel.iterator().next();
    return null;
  }

  /***********************************************************************/
  /* Annotation Mouse Model */
  /**********************************************************************/
  private AnnotationMouseModel annotMouseModel = new AnnotationMouseModel();

  public AnnotationMouseModel getAnnotationMouseModel() {
    return annotMouseModel;
  }


  /***********************************************************************/
  /* STATE INFORMATION */
  /**********************************************************************/

  /**
   * Set a unique ID for among opened documents which have the same file name
   * (but different paths) which may otherwise have the same display name. The
   * id is appended to the end of the <code>displayName</code> (enclosed in
   * angle brackets) in the result of {@link #getDisplayName}.
   * @see #getSessionID
   */
  final void setSessionID (int id) {
    displayName = null;
    sessionID = id;
  }
  
  /**
   * Get the unique ID this document has been given in this session.
   * @see #setSessionID
   */
  final int getSessionID () {
    return sessionID;
  }
  
  /**
   * Returns a string suitable for display to user in the gui. Equal to
   * <code>getName</code> unless the session id has been set.
   */
  public final String getDisplayName (boolean fullPath) {
    if (fullPath)
      return getPath ();

    // lazily generates displayName after creation or changing AtlasURI 
    if (displayName == null) {
      displayName = (basename==null?getName():basename);
      if (sessionID > 0)
        displayName += "<"+sessionID+">";
    }
    return displayName;
  }
  
  /**
   * Final segment of path (file name) of document. Name comes from AtlasURI
   * if specified (ie, annotations saved), otherwise from SignalURI.
   * @see #getDisplayName
   */
  public final String getName () {
    if (fileName == null) {
      String path = getPath ();
      int index = path.lastIndexOf ('/')+1; // 0 if not found!
      fileName = path.substring (index);
    }
    return fileName;
  }
  
  public String getBasename () {
    return basename;
  }

  public void setBasename (String b) {
    basename = b;
  }

  /**
   * Gets the known path of this document: the path of the atlasURI if known,
   * or the sourceURL if the file has never been saved. This is the unescaped
   * path from URI.getPath() or URL.getPath ().
   */
  public final String getPath () {
    String path;
    try {
      if (atlasURI != null)
        path = atlasURI.getPath();
      else if (externalURI != null) {
        path = externalURI.getPath();
        if (path == null) {
          path = new URI(externalURI.getSchemeSpecificPart().replaceAll(" ", "%20")).getPath();
        }
      }
      else if (getSignalURI() != null) {
        path = signalURI.getPath();
        if (path == null) {
          path = new URI(signalURI.getSchemeSpecificPart().replaceAll(" ", "%20")).getPath();
        }
      }
      else
        throw new IllegalStateException ("No Signal URL found");

      return path;
    } catch (URISyntaxException e) {
      e.printStackTrace();
      throw new IllegalStateException("No valid path found in signal URI");
    }
  }
  
  /**
   * Get the location  of the signal which is being annotated.
   */
  public URI getSignalURI () {
    // turn the signal URL into a URI. this ought to work flawlessly, but with
    // the disparity between URL/URI, who can say.  Other parts of this class
    // which convert signalURI to a URL should look to here to when there are
    // problems
    if (signalURI == null)
      signalURI = URLUtils.badURLToURI (signalURL);

    return signalURI;
  }

  /**
   * Get location of the AIF File which annotates the signal.
   */
  public URI getAtlasURI () {
    return atlasURI;
  }
  
  /**
   * Get location of the document when stored in an external format.
   */
  public URI getExternalURI () {
    return externalURI;
  }
  
  /**
   * Retrieve the task currently at hand.
   */
  public final Task getTask () {
    return awbDoc.getTask ();
  }

  /**
   * Internal convience method to set dirty flag, and fire event.
   * Made public to allow calling when saved to MAT Workspace.  RK 09/02/09
   * Use with caution!
   */
  public void setDirty (boolean d) {
    boolean old = dirty;
    dirty = d;
    support.firePropertyChange (DIRTY_FLAG_KEY, old, d);
  }
  /**
   * Returns true if the file has been modified since last save. To set clean,
   * you must call save.
   */
  public boolean isDirty () {
    return dirty;
  }

  public void setDirty () {
    setDirty(true);
  }

  /**
   * Retrieve reference the StyledDocument being maintained by Jawb, with
   * regard to annotations, styles, and current Tasks. If a widget wishes to
   * colorize differently, or use some other mechanism to dislay the text, it
   * should do so using a separate StyledDocument, as the one obtained here is
   * maintained by a standard Jawb component.
   */
  public StyledDocument getStyledDocument () {
    return styledDocument;
  }
  
  /**
   * Set the styled document to be used by components wishing to display
   * highlighted signal. Most users of a JawbDocument should not use this, as
   * it will be set by the system, when a file is opened, or re-read.
   * @see getStyledDocument
  public void setStyledDocument (StyledDocument doc) {
    styledDocument = doc;
  }
  */

  /**
   * Sets the sorting style for this document -- if true, unselected
   * annots will always be sorted so that smaller annots are displayed
   * on top of larger ones.  Sorting is temporarily deferred during
   * tab-cycling to allow cycled orderings -- user must hit escape to
   * re-sort the annots when finished. 
   */
  public void setSortAnnots (boolean s) {
    boolean old = sortAnnots;
    sortAnnots = s;
    // if setting to true after being false, must update highlights on the
    // whole document
    if (s && !old)
      updateHighlights(0, getStyledDocument().getLength(), null, true);
  }

  // RK 10/6/05 -- needed for when changing something in a table will
  // affect the highlight colors of multiple annots throughout the
  // document, such as they graying out feature in MSD
  public void updateAllHighlights () {
    updateHighlights (0, getStyledDocument().getLength(), null, true);
  }
  
  /***********************************************************************/
  /* MODIFIERS */
  /**********************************************************************/

  /** Set the Font family all the text in the document */
  public String getFontFamily () {
    return StyleConstants.getFontFamily (familyStyle);
  }

  /**
   * Set the font family for the text of this document. Widgets which use the
   * same font (not applicable to JTextComponents using the same
   * StyledDocument) will want to listen for changes to this bound property.
   */
  public void setFontFamily (String family) {
    Object old = StyleConstants.getFontFamily (familyStyle);

    StyleConstants.setFontFamily (familyStyle, family);
    styledDocument.setCharacterAttributes (0, styledDocument.getLength(),
                                           familyStyle, false);
    Preferences prefs = Jawb.getPreferences ();
    prefs.setPreference (Preferences.FONTS_LAST_FAMILY_KEY, family);
    support.firePropertyChange (FONT_FAMILY_PROPERTY_KEY, old, family);
  }
  
  /** Set the Font family all the text in the document */
  public int getFontSize () {
    return StyleConstants.getFontSize (familyStyle);
  }

  /**
   * Set the font family for the text of this document. Widgets which use the
   * same font (not applicable to JTextComponents using the same
   * StyledDocument) will want to listen for changes to this bound property.
   */
  public void setFontSize (int size) {
    int old = StyleConstants.getFontSize (familyStyle);

    StyleConstants.setFontSize (familyStyle, size);
    styledDocument.setCharacterAttributes (0, styledDocument.getLength(),
                                           familyStyle, false);
    Preferences prefs = Jawb.getPreferences ();
    prefs.setPreference (Preferences.FONTS_LAST_SIZE_KEY, size);
    support.firePropertyChange (FONT_SIZE_PROPERTY_KEY, old, size);
  }
  
  /** Set the Font family all the text in the document */
  public float getLineSpacing () {
    return StyleConstants.getLineSpacing (paraStyle);
  }

  /**
   * Set the font family for the text of this document. Widgets which use the
   * same font (not applicable to JTextComponents using the same
   * StyledDocument) will want to listen for changes to this bound property.
   */
  public void setLineSpacing (float spacing) {
    // not propogating a change event just yet
    //float old = StyleConstants.getLineSpacing (paraStyle);

    StyleConstants.setLineSpacing (paraStyle, spacing);
    styledDocument.setParagraphAttributes (0, styledDocument.getLength(),
                                           paraStyle, false);
    Preferences prefs = Jawb.getPreferences ();
    prefs.setPreference (Preferences.PARA_LAST_LINE_SPACING_KEY, spacing);
  }
  
  /**
   * The proper display, editing, and layout of text in a Document is also
   * dependant on the ComponentOrientation of the JTextComponent that it is
   * being displayed in. This method allows a component to retrieve the
   * orientation it should adapt, if it displays text using the
   * StyledDocument.
   *
   * @see Component
   * @see ComponentOrientation
   * @see Component#setComponentOrientation
   * @see Component#applyComponentOrientation
   */
  public ComponentOrientation getComponentOrientation () {
    return orientation;
  }

  /**
   * Set the orientation for the text of this document. Widgets which display
   * text from the signal (using the StyledDocument or not) will want to
   * listen for changes to this bound property, and set their
   * ComponentOrientation to match.<p>
   *
   * Calling this method will cause the alignment of all the text to be set
   * accordingly, though widgets which do not use the StyledDocument as their
   * model should also set their text's alignment. Should java ever become
   * more intellegent about how it deals with orientation and alignment, this
   * may become less of a burden in the future.<p>
   *
   * This method is package private as it should only be set by the
   * JawbDocument itself, (which reads the signal and decides which
   * orientation should be used).
   *
   * @param o ComponentOrientation.LEFT_TO_RIGHT, or
   *          ComponentOrientation.RIGHT_TO_LEFT.  ComponentOrientation.UNKNOWN
   *          is also allowed, which should cause listeners to behave as
   *          though LEFT_TO_RIGHT were specified, or the default for the
   *          current Locale.
   */
  public void setComponentOrientation (ComponentOrientation o) {
    int alignment = StyleConstants.ALIGN_LEFT;
    if (o == ComponentOrientation.RIGHT_TO_LEFT)
      alignment = StyleConstants.ALIGN_RIGHT;

    StyleConstants.setAlignment(alignmentStyle, alignment);
    styledDocument.setParagraphAttributes (0, styledDocument.getLength (),
                                           alignmentStyle, false);
    Object old = orientation;
    orientation = o;
    support.firePropertyChange (ORIENTATION_PROPERTY_KEY, old, orientation);
  }

  /**
   * The encoding of the signal for this document, which can be used to
   * read it in.  Default encoding is UTF-8
   * @see #setEncoding
   */
  public String getEncoding () {
    return awbDoc.getEncoding ();
  }

  /**
   * Set the encoding for the signal for this document. This will cause the
   * Signal to be re-read using the new encoding.
   * @see #getEncoding
   * @throws IOException if an IO exception occurs while reading.
   */
  public void setEncoding (String enc) throws IOException {
    String old = awbDoc.getEncoding ();
    awbDoc.setEncoding (enc);

    readSignal (styledDocument);
    textRangeTracker.resetHighlights ();

    support.firePropertyChange (ENCODING_PROPERTY_KEY, old, enc);
  }


  /***********************************************************************/
  /* Client Properties */
  /**********************************************************************/
  
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
    return awbDoc.getClientProperties ();
  }

  /**
   * Replaces the document properties dictionary for this document. Setting
   * with <code>null</code> will clear all properties.
   * 
   * @param map the new property map
   * @see #getClientProperties
   */
  public void setClientProperties (Map map) {
    awbDoc.setClientProperties (map);
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
    return awbDoc.getClientProperty (key);
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
    awbDoc.putClientProperty (key, value);
  }

  
  /***********************************************************************/
  /* SERVICES */
  /**********************************************************************/
  
  public AnnotationModel getAnnotationModel () {
    return awbDoc;
  }

  public String toString () {
    return getPath ();
  }
  

  /***********************************************************************/
  /* PROPERTY CHANGE LISTENER for preferences */
  /**********************************************************************/

  /**
   * PropertyChangeListener which is to be added to the global preferences
   * object on createion, and removed when the document is closed.
   */
  private class PreferenceChangeListener implements PropertyChangeListener {
    public void propertyChange (PropertyChangeEvent e) {
      String name = e.getPropertyName ();
      if (name == null)
        return;

      String styleKey = null;
      boolean visibleKey = false;
      boolean selectableKey = false;
      if (name.startsWith (namespace)) {
        if (name.endsWith(".visible")) {
          styleKey = name.substring(namespace.length(), name.length()-7);
          visibleKey = true;
        } else if (name.endsWith(".selectable")) {
          styleKey = name.substring(namespace.length(), name.length()-10);
          selectableKey = true;
        } else {
          styleKey = name.substring (namespace.length());
        }
      }
      else if (name.equals (Preferences.UNKNOWN_HIGHLIGHT_KEY))
        styleKey = Preferences.UNKNOWN_HIGHLIGHT_KEY;
      
      if (DEBUG > 2)
        System.err.println ("JD.propChange: name="+name+
                            " val="+e.getNewValue()+" styleKey="+styleKey);
      if (styleKey != null) {
        if (visibleKey || selectableKey) {
          // do this on the gui thread later so multiple changes don't cause
          // mulitple updates.
          if (updateHighlightsScheduled) {
            return;
          }
          updateHighlightsScheduled = true;
          SwingUtilities.invokeLater (new Runnable () {
              public void run () {
                // only update within annotations
                // TODO: would be better if we knew exactly /which/ annots
                // needed to be updated rather than doing all.
                Iterator iter = awbDoc.getAllAnnotations();
                while (iter.hasNext()) {
                  int start, end;
                  Annotation annot = (Annotation)iter.next();
                  if (annot instanceof TextExtentRegion) {
                    TextExtentRegion ter = (TextExtentRegion)annot;
                    start = ter.getTextExtentStart ();
                    end   = ter.getTextExtentEnd ();
                    
                  } else if (annot instanceof NamedExtentRegions) {
                    // TODO: stop hard coding for RDC!
                    NamedExtentRegions ner = (NamedExtentRegions)annot;
                    start = ner.getTextExtentStart ("full");
                    end   = ner.getTextExtentEnd ("full");
                    
                  } else {
                    // may annots are non-textual, just ignore
                    continue;
                  }
                  updateHighlights (start, end, null, false);
                }
                updateHighlightsScheduled = false;
            }});
        }
        else { // currenly only colorspec properties left
          try {
            ColorSpec colors = (ColorSpec) e.getNewValue ();
            Style style = styledDocument.getStyle (styleKey);
            if (style != null && colors != null) {
              StyleConstants.setBackground (style, colors.getBackground());
              StyleConstants.setForeground (style, colors.getForeground());
              reapplyHighlight (style);
            } else {
              System.err.println ("JD.propChange: name="+name+
                                  " style="+(style==null?null:style.getName())+
                                  " value="+e.getNewValue());
            }
          } catch (ClassCastException x) {
            if (DEBUG > 0) {
              System.err.println ("JD.propChange: name="+name+
                                  " (Color)value="+e.getNewValue());
              x.printStackTrace ();
            }
          }
        }
      }
    }
  }
  
  /***********************************************************************/
  /* PropertyChangeListener for annotations */
  /***********************************************************************/


  
  /**
   * PropertyChangeListener which is to be added to Text Annotations. Needed
   * to see when they have their extents changed (could be one of multiple
   * 'extents') so that highlighting can be redone.
   *
   * Since we now maintain a mapping of annotations we're listening to, to a
   * struct maintaining the offsets we last saw them at, we no longer need to
   * get the 'old value' from change events. This was actually brought on by a
   * bug in "ModifyExtentActions". It changed one offset at a time, and kept
   * putting the end before the start (or start after end) before setting the
   * second off set and correcting the discrepancy. The initial change event
   * from the first offset caused text panes to redisplay, and the discrepancy
   * caused an NPE in the swing.text.??? class trying to display it. Since I
   * liked the idea of having only 1 update fired per extent change, I changed
   * the 'ModifyTextExtent' to just change them all at once and not set the
   * name, old or new values.  The javadoc for PropertyChangeEvent states that
   * a 'null' name may be used to indicate multiple properties of an object
   * have changed under one event, (thus setting precidence) so it seemed
   * fine.<p>
   *
   * Of course that propted a rewrite of this class, which now couldn't rely
   * on getting the old values back (in order to clear the old highlight) so I
   * needed to map the annots to a small struct maintaing old offsets. A
   * better way would be nice, but java Documents break up thier Entities into
   * multiple parts so I can't map to them, and I could think of no better way
   * to clear the old highlight (other than reparsing all known
   * annots... uggh!). If you're reading this and can show us a better idea
   * we'll put it in!<p>
   *
   * This will only deal with basic text annotations (start,end) and RDC Style
   * annots which have 'head' and 'full' extents.
   */
  private class TextRangeTracker implements PropertyChangeListener {

    /** I'm just getting silly with this nested annotation crap, no?!? */
    private class OffsetMemento {
      int start, end;
      int headStart,headEnd;
    }

    private Map oldOffsetMap = new HashMap ();
    
    /**
     * This inner class is private so just don't call this with the wrong
     * kind of annotations!!!
     */
    public void add (AWBAnnotation annot) {
      if (DEBUG > 1) {
        System.err.println ("JD.trTracker: adding: "+
                            (annot == null?null:annot.getId()));
        //x.printStackTrace ();
      }
      OffsetMemento memento = new OffsetMemento ();
      if (annot instanceof TextExtentRegion) {
        TextExtentRegion ter = (TextExtentRegion)annot;
        memento.start = ter.getTextExtentStart();
        memento.end   = ter.getTextExtentEnd();
        // put in the highlight!
        updateHighlights (memento.start, memento.end, annot, true);
        
      } else if (annot instanceof NamedExtentRegions) {
        NamedExtentRegions ner = (NamedExtentRegions)annot;
        memento.start     = ner.getTextExtentStart("full");
        memento.end       = ner.getTextExtentEnd("full");
        memento.headStart = ner.getTextExtentStart("head");
        memento.headEnd   = ner.getTextExtentEnd("head");
        // put in the highlight!
        // TODO: HACK: commenting the head action relies on NamedExtentRegions
        // making sure the head extent eventually nests w/in  the full extent
        updateHighlights (memento.start, memento.end, annot, true);
        //updateHighlights (memento.headStart, memento.headEnd, annot, true);

      } else {
        throw new RuntimeException ("Can't track non-text annotations. Bug in source.");
      }
      oldOffsetMap.put (annot, memento);
      annot.addPropertyChangeListener (this);

      // finally add the actual highlight
    }
    
    public void remove (AWBAnnotation annot) {
      if (DEBUG > 1)
        System.err.println ("JD.trTracker: removing: "+
                            (annot==null ? null : annot.getId()));

      OffsetMemento memento = (OffsetMemento)oldOffsetMap.get (annot);
      // shouldn't be null so let the error fly
      if (annot instanceof TextExtentRegion) {
        updateHighlights (memento.start, memento.end, annot, false);
        
      } else if (annot instanceof NamedExtentRegions) {
        // if the head is outside the full, do both
        // TODO: HACK: commenting the head action relies on NamedExtentRegions
        // making sure the head extent eventually nests w/in  the full extent
        //if (memento.headStart < memento.start || memento.headEnd > memento.end)
        //updateHighlights (memento.headStart, memento.headEnd, annot, false);
        updateHighlights (memento.start, memento.end, annot, false);
      }
      oldOffsetMap.remove (annot);
      annot.removePropertyChangeListener (this);
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
      // if this throws a ClassCastEx.. someone added this as a listener to an
      // annot they shouldn't have.
      AWBAnnotation annot = (AWBAnnotation)evt.getSource ();
      String name = evt.getPropertyName ();
      int oldValue;

      if (DEBUG > 1)
        System.err.println ("JD.TRL.propChng: prop="+name+
                            " old="+evt.getOldValue()+
                            " new="+evt.getNewValue()+
                            "\n\tsource="+annot);

      // remove a range from old location then add to new
      if (annot instanceof TextExtentRegion) {
        TextExtentRegion ter = (TextExtentRegion) annot;
        
        int start = ter.getTextExtentStart();
        int end   = ter.getTextExtentEnd();
        
        if (start > end) { // non fatal to ignore (I hope)
          System.err.println("ERROR: offsets start>end. Set extents atomically");
          Thread.dumpStack();
          return;
        }
        
        OffsetMemento memento = (OffsetMemento) oldOffsetMap.get (ter);
        if (start != memento.start || end != memento.end) {
          updateHighlights (memento.start, memento.end, ter, false);// remove
          updateHighlights (start, end, ter, true); // add
          memento.start = start;
          memento.end = end;
        }

      } else if (annot instanceof NamedExtentRegions) {
        NamedExtentRegions ner = (NamedExtentRegions) annot;
        int start     = ner.getTextExtentStart("full");
        int end       = ner.getTextExtentEnd("full");
        int headStart = ner.getTextExtentStart("head");
        int headEnd   = ner.getTextExtentEnd("head");
        boolean doHead = false;
        OffsetMemento memento = (OffsetMemento)oldOffsetMap.get (ner);
        if (DEBUG > 2) {
          System.err.println ("(["+memento.headStart+","+memento.headEnd+"],"+
                              memento.start+","+memento.end+")");
          System.err.println ("(["+headStart+","+headEnd+"],"+start+","+end+")");
        }
        
        if (start > end) { // non fatal to ignore (I hope)
          System.err.println("ERROR: offsets start>end. Set extents atomically");
          Thread.dumpStack();
          return;
        }

        // always do the 'full' extent for now... optimize later
        // TODO: HACK: commenting the head action relies on NamedExtentRegions
        // making sure the head extent eventually nests w/in  the full extent
        //if (headStart != memento.headStart || headEnd != memento.headEnd)
        //updateHighlights (memento.headStart, memento.headEnd, ner, false);// rem
        updateHighlights (memento.start, memento.end, ner, false);// rem
        updateHighlights (start, end, ner, true); // add
        //if (headStart != memento.headStart || headEnd != memento.headEnd)
        //updateHighlights (headStart, headEnd, ner, true); // add
        
        memento.headStart = headStart;
        memento.headEnd = headEnd;
        memento.start = start;
        memento.end = end;
        
      } else {
        if (DEBUG > 0)
          System.err.println ("JD.TRL.propChng: unknown annotation:\n\tannot="+
                              annot);
        return;
      }
    }

    /**
     * Intended to bue used when re-reading the text, and the highlights get
     * cleared (such as when changing the character-encoding).
     */
    public void resetHighlights () {
      // clear anything remaining
      StyledDocument sd = getStyledDocument();
      defaultStyle.removeAttribute (ANNOTATION_ATTRIBUTE);
      sd.setCharacterAttributes (0, sd.getLength(), defaultStyle, true);
      if (DEBUG > 1)
        System.err.println ("    -- All cleared!");

      // now put all the highlights back
      Iterator iter = oldOffsetMap.keySet().iterator ();
      while (iter.hasNext ()) {
        AWBAnnotation annot = (AWBAnnotation) iter.next();
        OffsetMemento memento = (OffsetMemento) oldOffsetMap.get (annot);

        updateHighlights (memento.start, memento.end, annot, true);
      }
    }
  }
  
  public String getMode() {return docMode;}

  public int getLength() {
    DefaultStyledDocument sd = (DefaultStyledDocument) getStyledDocument();

    return sd.getLength();
  }

  
}// JawbDocument
