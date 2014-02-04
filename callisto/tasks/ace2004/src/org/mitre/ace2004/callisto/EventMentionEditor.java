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

package org.mitre.ace2004.callisto;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.*;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleConstants;

import gov.nist.atlas.*;
import gov.nist.atlas.type.*;
import gov.nist.atlas.ref.*;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.atlas.AnnotationModelEvent;
import org.mitre.jawb.atlas.AnnotationModelListener;
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.NamedExtentRegions;
import org.mitre.jawb.atlas.SubordinateSetsAnnotation;
import org.mitre.jawb.gui.*;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.swing.*;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;

/**
 * Display and editing widget for Event Annotations. Displays vaild
 * information in a JTable.
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 */
public class EventMentionEditor extends JSplitPane implements JawbComponent {

  private static final int DEBUG = 0;

  private static final int ID_COL = 0;
  private static final int ANCHOR_COL = 1;
  private static final int EXTENT_COL = 2;
  private static final int LEVEL_COL = 3;

  private static final String COMPONENT_ID = "eventMentionEditor";

  // TODO: order of these attribs & headings is relied upon in EventModel
  //    getValue is overridden to deal with null (which isn't an attribute)
  private static final String[] ATTRIBS =
    new String[] {null/*ace_id*/, "TextExtent",// referrs to 'anchor'
                  "extent", "level"};
  private static final String[] HEADINGS =
    new String[] {"ID", "Anchor", "Extent", "Level"};
  
  ACE2004ToolKit toolkit;
  
  AnnotationTable table = null;
  ArgumentEditor argEditor = null;

  /**
   * Creates a new <code>EventEditor</code> instance.
   */
  public EventMentionEditor (TaskToolKit toolkit) {
    this.toolkit = (ACE2004ToolKit) toolkit;
    Task task = toolkit.getTask();
    init (task, task.getAnnotationType (ACE2004Task.EVENT_MENTION_TYPE_NAME));
    setName(COMPONENT_ID);
  }

  private void init (Task task, AnnotationType type) {

    // layout manager
    setLayout (new BorderLayout());

    // create and add table (with multiple column sorting)
    AnnotationTableModel atm =
      new EventMentionModel (task, type, ATTRIBS, HEADINGS);
    table = new AnnotationTable(toolkit, atm);
    table.setName(COMPONENT_ID);
    table.setColumnEditable(ID_COL, false);
    table.setAutoResizeMode(table.AUTO_RESIZE_NEXT_COLUMN);

    table.setSelectedAnnotationExchanger(ACE2004Utils.EVENT_TYPE,
                                         toolkit.event2EventLexicals);
    table.setSelectedAnnotationExchanger(ACE2004Utils.EVENT_MENTION_EXTENT_TYPE,
                                         toolkit.eventLexicals2Lexicals);
    table.setSelectedAnnotationExchanger(ACE2004Utils.ARGUMENT_MENTION_TYPE,
                                         toolkit.argMention2EventMention);

    TableColumn column = table.getColumnModel().getColumn(ID_COL);
    column.setCellRenderer(new IdRenderer());

    argEditor = new ArgumentEditor(toolkit, type);
    // Blech! how do we /know/ it's a BorderLayout JPanel?
    JComponent argComponent = (JComponent) argEditor.getComponent();
    argComponent.add(new JLabel(" Argument Mentions "),
                     BorderLayout.NORTH);

    // Listen to selections on table to update argEditor
    ListSelectionListener selectionListener = new MyTableSelectionListener();
    ListSelectionModel lsm = table.getSelectionModel();
    lsm.addListSelectionListener(selectionListener);

    // split pane configuration and persistence
    final Preferences prefs = Jawb.getPreferences();
    final String dividerLocationKey =
      "windows."+ACE2004Task.TASK_NAME+"."+getName()+".dividerLocation";

    this.addPropertyChangeListener(
      JSplitPane.DIVIDER_LOCATION_PROPERTY,
      new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
          prefs.setPreference(dividerLocationKey, getDividerLocation());
        }
      });
    int dividerLocation = -1;
    try {
      dividerLocation = prefs.getInteger(dividerLocationKey);
    } catch (NumberFormatException x) {}
    
    setDividerSize(7);
    setResizeWeight(0.8);
    setOneTouchExpandable(true);
    setDividerLocation(dividerLocation);

    setOrientation(JSplitPane.VERTICAL_SPLIT);
    setTopComponent(new JScrollPane(table));
    setBottomComponent(argComponent);
  }
    
  /**
   * Get the AnnotationTable this widget supports.
   */
  public AnnotationTable getTable () {
    return table;
  }

  /**
   * Events need specialized code to get at the primary-mention
   */
  private class EventMentionModel extends AnnotationTableModel {
    public EventMentionModel (Task task, AnnotationType type,
                              Object[] columnSrc, String[] headings) {
      super (task, type, columnSrc, headings);
    }

    /**
     * Overridden to display mention text extent properly, calculate
     * number of mentions, and allow checkboxes for generic.
     */
    public Object getValueAt (int row, int col) {
      if (col == EXTENT_COL) {
        Object annot = super.getValueAt (row, col);
        if (annot == null)
          return null;

        if (annot instanceof AnnotationRef)
          annot = ((AnnotationRef)annot).getElement();
	if (DEBUG > 0) {
	    System.err.println("DSD: evtMenEditor getValueAt TextExtent in EventMention; annot = " + annot);
	    System.err.println("DSD: evtMenEditor returning: annot = " + ((AWBAnnotation)annot).getAttributeValue("TextExtent"));
	}
        return ((AWBAnnotation)annot).getAttributeValue("TextExtent");
      }
      return super.getValueAt (row, col);
    }
  }

  /***********************************************************************/
  /* Implementing JawbComponent */
  /***********************************************************************/
  
  public void setJawbDocument (JawbDocument doc) {
    argEditor.setJawbDocument(doc);
    table.setJawbDocument(doc);
  }
  
  public JawbDocument getJawbDocument () {
    return table.getJawbDocument ();
  }

  public Set getSelectedAnnots () {
    return table.getSelectedAnnots ();
  }

  public Component getComponent () {
    return this;
  }

  /***********************************************************************/
  /* Table's List Selection Listener for selections */
  /***********************************************************************/

  /** Propogate changes in the selection of the list to the JawbDocument. */
  protected class MyTableSelectionListener implements ListSelectionListener {
    public void valueChanged (ListSelectionEvent e) {
      if (e.getValueIsAdjusting ())
        return;

      ListSelectionModel tableSM = (ListSelectionModel)e.getSource ();
      try {
        int min = tableSM.getMinSelectionIndex();
        int max = tableSM.getMaxSelectionIndex();

        // only interested when a single row is selected
        if (min < 0 || min != max) {
          /*
          JawbDocument doc = getJawbDocument();
          Iterator iter = doc.getSelectedAnnotationModel().iterator();
          while (iter.hasNext()) {
            Annotation annot = (AWBAnnotation) iter.next();
            if (annot.getAnnotatoinType()
                .equals(ACE2004Utils.ARGUMENT_MENTION_TYPE))
              return;
          }
          */
          argEditor.setCurrentParent(null);
        }
        else
          argEditor.setCurrentParent(table.getAnnotation(min));

      } catch (Exception x) { x.printStackTrace (); }
    }
  }
}
