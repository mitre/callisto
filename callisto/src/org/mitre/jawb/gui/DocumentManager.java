
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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.prefs.Preferences;

/**
 * Maintains indefinately large list of currently opened documents, and a
 * limited history of documets recently opened.  Pass JMenu's into the
 * appropriate methods prior to expanding, to have them updated with current
 * items. Actions in the menus will switch-to the specified document.<p>
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 2.0
 */
public class DocumentManager implements PropertyChangeListener {

  private static final int DEBUG = 0;

  /** Default maximum for number of files to remember */
  private static final int DEFAULT_HISTORY_MAX = 8;

  /** These are the menus we keep up to date */
  private List documentMenus = new LinkedList ();
  /** These are the menus we keep up to date */
  private List historyMenus = new LinkedList ();
  
  /**
   * List of currently open documents, ordered most recently opened/switched
   * to, first. The DocumentManager listens for document 'chages' to maintain
   * that ordering.
   */
  private List documentList = new LinkedList ();

  /**
   * List of recently opened document URI's, ordered most recently opened
   * first. These names will also be updated when names of an open document
   * are changed.
   */
  private List historyList = new LinkedList ();

  /** history list size is limited by this number */
  private int maxHistory;
  
  /**
   * Map names to a list of documents which have the same name, in order of
   * their Document ID's (for assigning unique IDs).
   */
  private HashMap nameListMap = new HashMap ();

  /** 
   * Map JawbDocument to a JawbCollection if the document is opened in
   * this frame as part of a collection.
   */
  private HashMap collectionMap = new HashMap ();

  /** 
   * Map JawbDocument to the index of the document within its
   * JawbCollection, if the document is opened in this frame as part of
   * a collection.
   */
  private HashMap collIndexMap = new HashMap ();

  /** 
   * Map JawbDocument to the Date when this document was most recently
   * opened for editing.  (This will be updated by calls in Jawb.java and
   * JawbFrame.java, possibly among others.)
   */
  private HashMap timerStartMap = new HashMap ();

  /** 
   * Map JawbDocument to a Long representing the number of
   * milliseconds spent so far on this document.  (This will be
   * updated by calls in Jawb.java and JawbFrame.java, possibly among
   * others.)
   */
  private HashMap timeSpentMap = new HashMap ();



  /** Listner to do what's neccissary when the document's URI changes */
  private PropertyChangeListener nameListener = new DocNameListener ();

  /** Action to clears the history list, added at the end of history menu */
  private Action clearHistoryAction = new AbstractAction ("Clear History") {
        public void actionPerformed (ActionEvent e) {
          historyList.clear ();
          updateHistoryMenus ();
        }};
    

  /**
   * Construct a manager with nothing to do (no menus to manage!)
   */
  public DocumentManager () {
    if (DEBUG > 0)
      System.err.println ("Initializing Document Manager");

    Preferences prefs = Jawb.getPreferences ();
    // listen for changes to recent max and remove dead entries from prefs
    prefs.addPropertyChangeListener (Preferences.HISTORY_MAX_KEY, this);
    maxHistory = prefs.getInteger(Preferences.HISTORY_MAX_KEY,
                                  DEFAULT_HISTORY_MAX);
    String historyString = prefs.getPreference (Preferences.HISTORY_LIST_KEY);

    if (historyString != null) {
      // parse the history stored in preferences. whitespace is illegal in URI
      // specifically to use it in parsing!
      int num = 0;
      StringTokenizer st = new StringTokenizer (historyString);

      while (st.hasMoreTokens () && num++ < maxHistory) {
        String token = st.nextToken ();
        try {
          historyList.add (new URI (token));
          
        } catch (URISyntaxException e) {
          System.err.println ("Bad syntax in 'history file' in prefs: "+token);
        }
      }
    }      
  }

  /***********************************************************************/
  /** Menu manipulation **/

  /**
   * Create a Document menu which the document manager will keep updated with
   * name changes, and new documents as they are added to the manager.
   */
  public JMenu createDocumentMenu () {
    if (DEBUG > 0)
      System.err.println ("DM.createDocMenu");
    JMenu menu = new JMenu ("Document");
    menu.setMnemonic('D');
    documentMenus.add (menu);
    
    updateDocumentMenu (menu);
    return menu;
  }
  /**
   * No longer update the specified menu. You must pass menus created by
   * createDocumentMenu to this method or there will be a 'memory leak'
   */
  public void releaseDocumentMenu (JMenu menu) {
    if (DEBUG > 0)
      System.err.println ("DM.releaseDocMenu");
    documentMenus.remove (menu);
  }

  /**
   * Create a History menu which the document manager will keep updated with
   * name changes, and new documents as they are added to the manager.
   */
  public JMenu createHistoryMenu () {
    if (DEBUG > 0)
      System.err.println ("DM.createHistMenu");
    JMenu menu = new JMenu ("History");
    menu.setMnemonic('H');
    historyMenus.add (menu);

    updateHistoryMenu (menu);
    return menu;
  }
  /**
   * No longer update the specified menu. You must pass menus created by
   * createHistoryMenu to this method or there will be a 'memory leak'
   */
  public void releaseHistoryMenu (JMenu menu) {
    if (DEBUG > 0)
      System.err.println ("DM.releaseHistMenu");
    historyMenus.remove (menu);
  }

  /***********************************************************************/
  /* Updating the known menus (internal) */
  /***********************************************************************/

  /** Updates all document menus. */
  private void updateDocumentMenus () {
    if (DEBUG > 0)
      System.err.println ("DM.updateDocMenus");
    Iterator iter = documentMenus.iterator ();
    while (iter.hasNext())
      updateDocumentMenu ((JMenu) iter.next());
  }
  /** Updates all history menus and store history in prefs. */
  private void updateHistoryMenus () {
    if (DEBUG > 0)
      System.err.println ("DM.updateHistMenus");
    Iterator iter = historyMenus.iterator ();
    while (iter.hasNext())
      updateHistoryMenu ((JMenu) iter.next());
    
    // now actually store the new list
    Preferences prefs = Jawb.getPreferences ();
    StringBuffer sb = new StringBuffer (100);
    iter = historyList.iterator ();
    while (iter.hasNext ()) {
      sb.append (iter.next().toString()).append (' ');
    }
    prefs.setPreference (Preferences.HISTORY_LIST_KEY, sb.toString());
    Jawb.storePreferences ();
  }
  
  /**
   * Empties the specified JMenu and repopulates it with an items for each
   * currently open document. Selecting one of these will cause that document
   * to become the 'current' document in the current frame.
   */
  private void updateDocumentMenu (JMenu menu) {
    menu.removeAll ();
    
    // add the current documents, oldest at the top, and then down.
    Iterator iter = documentList.iterator ();
    while (iter.hasNext ())
      menu.add (new DocAction ((JawbDocument) iter.next()));

    menu.setEnabled (menu.getItemCount() != 0);
    
    if (DEBUG > 0)
      System.err.println ("DM.updateDoc: menu: "+menu+" size="+
                          documentList.size()+" items="+menu.getItemCount());
  }

  /**
   * Removes a document menu from being controlled by this manager. Otherwise,
   * the garbage collector will develop 'leaks'.
   */
  private void updateHistoryMenu (JMenu menu) {
    menu.removeAll ();
    
    // add the current documents, oldest at the top, and then down.
    Iterator iter = historyList.iterator ();
    while (iter.hasNext ())
      menu.add (new HistoryAction ((URI) iter.next()));

    int count = menu.getItemCount ();
    menu.setEnabled (count != 0);
    if (count > 0) {
      menu.addSeparator();
      menu.add (new JMenuItem (clearHistoryAction));
    }
    
    if (DEBUG > 0)
      System.err.println ("DM.updateHistory: menu: "+menu);
  }
  
  /***********************************************************************/
  /** URL work **/

  /**
   * Retuns the already open document the URI specifies, if it is the same
   * atlas, external, or signal uri.
   * @return the open document which the URI specifies, or null if not found.
   */
  public JawbDocument find (URI uri) {
    if (DEBUG > 0)
      System.err.println ("DM.findURI: "+uri);
    // I suppose I could make a hash of these, but I don't expect this O(3n)
    // search will be too brutal.  We'll change it later if people tend to
    // open a lot of documents and this prove's too expensive
    JawbDocument doc = null;
    String path = uri.getPath();
    if (path == null) {
      path = uri.getSchemeSpecificPart();
    }
    boolean isAtlasURI = path.endsWith (".aif");

    Iterator iter = documentList.iterator ();
    while (iter.hasNext ()) {
      doc = (JawbDocument) iter.next();
      if (uri.equals (doc.getAtlasURI ()) ||
          uri.equals (doc.getExternalURI ()) ||
          uri.equals (doc.getSignalURI ())) {
        break;
      }
      doc = null;
    }
    if (DEBUG > 0)
      System.err.println ("DM.findURI: returning "+doc);
    return doc;
  }

  /**
   * Add the a document to the list of 'open' documents, and update the
   * 'history' list as well. If the document already exists in the lists,
   * there is no effect.<p>
   *
   * This will set the documents unique ID among opened documents which might
   * otherwise have the same display name.  This is only important when users
   * display document names without full paths, as files with the same name,
   * in different directories would be ambiguous.
   *
   * @return true if the document was added to the document list, false
   * if it was already open
   */
  public boolean add (JawbDocument doc) {
    if (DEBUG > 0)
      System.err.println ("DM.add: doc: "+doc.getDisplayName(false)+
                          "\n\tto: "+documentList);

    // See if we're already aware of it
    Iterator iter = documentList.iterator ();
    while (iter.hasNext ()) {
      if (iter.next().equals (doc)) {
        if (DEBUG > 0)
          System.err.println ("DM.add: already here : "+documentList);
        return false;
      }
    }

    documentList.add (doc);
    // make sure it has a unique name.
    setSessionID (doc);
    doc.addPropertyChangeListener (doc.NAME_KEY, nameListener);

    // will call update menus for us
    updateHistory (doc.getAtlasURI ());
    return true;
  }

  /**
   * Returns an {@link Collections#unmodifiableList Unmodifiable List} of
   * currently open documents.
   */
  public List documents () {
    return Collections.unmodifiableList (documentList);
  }
  
  /**
   * Called from a couple places with the ATLAS uri, since thats the only one
   * we can easily open. This may change.
   */
  private void updateHistory (URI uri) {
    if (uri != null) {
      // remember it
      if (historyList.contains (uri)) {
        historyList.remove (uri);
        historyList.add (0, uri);
      } else {
        historyList.add (0, uri);
      }
    }
    // make sure all frames get the changes
    updateDocumentMenus ();
    updateHistoryMenus ();
  }

  /**
   * Removes a document from the list of currently open documents, though it
   * remains in memory. Called by the closing frame.
   * @throws IllegalArgumentException if specified doc is not in the list
   * @throws NullException if specified doc is null
   */
  public void remove (JawbDocument doc) {
    // we can remove the action to switch to this document
    int index = documentList.indexOf (doc);
    if (index < 0)
      throw new IllegalArgumentException ("Document: "+
                                          doc.getDisplayName(false)+
                                          "\n  not in list"+documentList);
    if (DEBUG > 0)
      System.err.println ("DM.remove: index="+index);
    
    documentList.remove (index);
    releaseSessionID (doc, null);

    // if this document was part of a collection, remove those entries too
    collectionMap.remove(doc);
    collIndexMap.remove(doc);
    
    // remove timing information on this file
    timerStartMap.remove(doc);
    timeSpentMap.remove(doc);
    
    // make sure all frames get the changes
    updateDocumentMenus ();
    updateHistoryMenus ();
  }
  
  /** returns true if manager contains the specified document */
  public boolean contains (JawbDocument doc) {
    return documentList.contains (doc);
  }

  public void setCollection (JawbDocument doc, JawbCollection coll) {
    collectionMap.put(doc, coll);
  }

  public JawbCollection getCollection (JawbDocument doc) {
    return (JawbCollection) collectionMap.get(doc);
  }

  public void setCollectionIndex (JawbDocument doc, int index) {
    collIndexMap.put(doc, new Integer(index));
  }

  /* Returns the index of doc within its collection, or -1 if 
   * there is no value in the hash map (i.e., the item is not
   * part of a collection.
   */
  public int getCollectionIndex (JawbDocument doc) {
    Integer index = (Integer) collIndexMap.get(doc);
    if (index == null)
      return -1;
    else
      return index.intValue();
  }

  /** Number of currently open documents */
  public int documentCount () {
    return documentList.size ();
  }

  /** Number of items in the history */
  public int historyCount () {
    return historyList.size ();
  }

  /**
   * Set's the documents session ID with the first integer >= 0 not already
   * taken.
   */
  private void setSessionID (JawbDocument doc) {
    String name = doc.getName ();
    if (DEBUG > 0)
      System.err.println ("DM.setSessionID: "+doc.getPath());

    // create a new namelist if this is the first time we've seen this name
    List nameList = (List)nameListMap.get (name);
    if (nameList == null) {
      if (DEBUG > 0)
        System.err.println ("  creating new name list");
      nameList = new LinkedList ();
      nameListMap.put (name, nameList);
    }
    
    // find the first point where a doc in the list is out of order: that's
    // the id number assigned to this document.  Then insert the doc at that
    // point in the list.
    int count = 0;
    Iterator iter = nameList.iterator ();
    while (iter.hasNext ()) {
      JawbDocument namesake = (JawbDocument) iter.next ();
      if (namesake.getSessionID () != count)
        break;
      if (DEBUG > 0)
        System.err.println ("   existing: "+count);
      count++;
    }
    if (DEBUG > 0)
      System.err.println ("  setting ID: "+count);

    doc.setSessionID (count);
    nameList.add (count, doc);
  }

  /** Call with name specified as the old value when the name has already
   * changed */
  private void releaseSessionID (JawbDocument doc, String name) {
    if (name == null)
      name = doc.getName ();
    List nameList = (List) nameListMap.get (name);
    nameList.remove (doc);
    
    doc.setSessionID (-1);
  }

  /***********************************************************************/
  /** Listens for changes to history size only */
  public void propertyChange (PropertyChangeEvent e) {
    String name = e.getPropertyName ();

    if (Preferences.HISTORY_MAX_KEY.equals (name)) {
      int max = DEFAULT_HISTORY_MAX;
      String maxString = (String) e.getNewValue ();
      
      if (! (maxString == null || maxString.equals("")))
        max = Integer.parseInt (maxString);

      if (historyList.size () > max) {
        Iterator iter = historyList.listIterator (max);
        while (iter.hasNext ()) {
          iter.next ();
          iter.remove ();
        }
      }
    }
  }

  /***********************************************************************/
  /* Methods for managing the time spent maps 
   * The timer may be started and stopped multiple times, in which case
   * all the chunks of time spent will be added into the timeSpent map
   * value.  When a document is "removed" from management in the
   * Document manager, all timer information is lost as well, so this
   * needs to be retrieved first.
   ***********************************************************************/
  
  /**
   *
   */
  public void setTimeSpent(JawbDocument doc, long millis) {
    timeSpentMap.put(doc, new Long(millis));
  }

  /**
   *
   */
  public long getTimeSpent(JawbDocument doc) {
    return ((Long)timeSpentMap.get(doc)).longValue();
  }

  /**
   *
   */
  public void incrementTimeSpent(JawbDocument doc, long millis) {
    long prior = getTimeSpent(doc);
    timeSpentMap.put(doc, new Long(prior+millis));
  }

  /**
   * TODO it ought to be an error to start the timer if it has already
   * been started?
   */
  public void startTimer (JawbDocument doc) {
    timerStartMap.put(doc, new Date());
  }

  /**
   *
   */
  public void stopTimer (JawbDocument doc) {
    long startMillis = 0;
    long endMillis = System.currentTimeMillis();
    Date startDate = (Date) timerStartMap.get(doc);
    if (startDate != null) {
      startMillis = startDate.getTime();
      incrementTimeSpent(doc, (endMillis - startMillis));
      timerStartMap.put(doc, null);
    }
  }

  /**
   *  clearTimer clears out both the timeSpent and the most recent
   *  "start" time, thus losing all timing information for this document
   */
  public void clearTimer (JawbDocument doc) {
    timerStartMap.put(doc, null);
    timeSpentMap.put(doc, null);
  }



  /***********************************************************************/
  /**
   * Action class which wraps a URI and opens the document (in triggering
   * Frame) the URI specifies when the actions button is triggered.
   */
  private static class DocAction extends JawbAction {
    
    JawbDocument doc;
    DocAction (JawbDocument doc) {
      super (doc.getDisplayName (false));
      this.doc = doc;
    }
    public void actionPerformed (ActionEvent e) {
      JMenuItem mi = (JMenuItem)e.getSource ();
        if (DEBUG > 0)
          System.err.println ("DM.ACTION: setcurrentdoc on frame="+
                              getJawbFrame (e));
      getJawbFrame (e).setCurrentDocument (doc);
    }
    public String toString () { return (String) getValue (NAME); }
  }

  /**
   * Action class which wraps a URI or a document, and opens the document (in
   * triggering Frame) the URI specifies when the actions button is triggered.
   */
  private class HistoryAction extends JawbAction {
    URI uri;
    HistoryAction (URI uri) {
      if (uri.getScheme().equals ("file"))
        putValue (NAME, new File (uri).toString()); // get the right look
      else
        putValue (NAME, uri.toString()); // get the right look
      this.uri = uri;
    }
    public void actionPerformed (ActionEvent e) {
      JMenuItem mi = (JMenuItem)e.getSource ();
      getJawbFrame (e).openAIF (uri);
    }
  }

  /***********************************************************************/
  /**
   * Listen to documents and update the lists, and the documents 'sessionID'
   * when it's uri's change.
   */
  private class DocNameListener implements PropertyChangeListener{
    /**
     * When this action's name changes, update it's Session ID, and change the
     * name in the menu.
     */
    public void propertyChange (PropertyChangeEvent e) {
      String propName = e.getPropertyName ();

      if (JawbDocument.NAME_KEY.equals (propName)) {
        JawbDocument doc = (JawbDocument) e.getSource ();

        releaseSessionID (doc, (String) e.getOldValue());
        setSessionID (doc);

        updateHistory (doc.getAtlasURI());
      }
    }
  }
  
}// DocumentManager.java
