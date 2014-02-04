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
import java.util.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.*;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleConstants;

import gov.nist.atlas.*;
import gov.nist.atlas.type.*;
import gov.nist.atlas.ref.*;
import gov.nist.atlas.ref.AnnotationRef;
import gov.nist.atlas.util.ATLASElementSet;

import org.mitre.jawb.atlas.*;
import org.mitre.jawb.gui.GUIUtils;
import org.mitre.jawb.io.*;
import org.mitre.jawb.tasks.*;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.atlas.AnnotationModelEvent;
import org.mitre.jawb.atlas.AnnotationModelListener;
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.AWBDocument;
import org.mitre.jawb.atlas.HasSubordinates;
import org.mitre.jawb.atlas.MultiPhraseAnnotation;
import org.mitre.jawb.atlas.NamedExtentRegions;
import org.mitre.jawb.atlas.SubordinateSetsAnnotation;
import org.mitre.jawb.atlas.UnmodifiableAttributeException;
import org.mitre.jawb.atlas.AnnotationModelEvent;
import org.mitre.jawb.atlas.AnnotationModel;
import org.mitre.jawb.gui.*;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.swing.*;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;

// testing/debugging
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

/**
 * Render and allow user to edit Entity annotations from the text.
 *
 * @author <a href="mailto:robyn@mitre.org">Robyn Kozierok</a>
 * @version 1.0
 */
public class UnmentionedRelationEditor extends JPanel implements JawbComponent {

  // RK 1/29/07 I don't think we need a JSplitPane here, but if we do
  // later, we may need to change the JPanel above back to JSplitPane

  private static final int DEBUG = 0;

  // TODO: order of these attribs & headings is relied upon in RelationModel
  // and all over... these constants may help
  private static final int ID_COL = 0;
  private static final int ARG1_COL = 1;
  private static final int TYPE_COL = 2;
  private static final int SUBTYPE_COL = 3;
  private static final int ARG2_COL = 4;
  //  private static final int MODALITY_COL = 5;
  //  private static final int TENSE_COL = 6;
  //  private static final int LEXICAL_COND_COL = 7;
  //  private static final int EXTENT_COL = 8;

  private static final String COMPONENT_ID = "unmentionedRelationEditor";

  // ID uses null as the attribute, so that the model will return the
  // relation annot itself.  That is passed to the IdRenderer to
  // generate the correct output for that column.
  private static final Object[] ATTRIBS =
    new Object[] {null/*"ace_id"*/, "arg1", "type", "subtype", "arg2"};

  private static final String[] HEADINGS =
    new String[] {"Relation ID", "Arg1", "Type", "Subtype", "Arg2"};

  ACE2004ToolKit toolkit;
  
  AnnotationTable table = null;
  //  ArgumentEditor argEditor = null;
  
  String typeName;

  /** Observes changes to annots, watching for mention insertion/removal. */
  AnnotModelListener annotModelListener = null;

  /**
   * Creates a new <code>UnmentionedRelationEditor</code> instance.
   */
  public UnmentionedRelationEditor (TaskToolKit toolkit) {
    this.toolkit = (ACE2004ToolKit) toolkit;
    Task task = toolkit.getTask();
    init (task, ACE2004Utils.RELATION_TYPE);
    this.typeName = ACE2004Task.RELATION_TYPE_NAME;
    setName(COMPONENT_ID);
  }

  private void init (Task task, AnnotationType type) {

    // filter only unmentioned relations
    AnnotationFilter filter = new UnmentionedFilter();

    // layout manager
    setLayout (new BorderLayout());
    
    // create and add table (with multiple column sorting)
    AnnotationTableModel atm = new RelationModel(task, type, ATTRIBS, HEADINGS);
    table = new AnnotationTable (toolkit, atm);
    table.setName(COMPONENT_ID);
    table.setColumnEditable(ID_COL, false);
    table.setColumnEditable(ARG1_COL, false);
    table.setColumnEditable(ARG2_COL, false);
    //table.setColumnEditable(TYPE_COL, false); 
    //table.setColumnEditable(SUBTYPE_COL, false); 
    //    table.setColumnEditable(MODALITY_COL, true);
    //    table.setColumnEditable(TENSE_COL, true);
    //    table.setColumnEditable(EXTENT_COL, false);
    table.setAutoResizeMode(table.AUTO_RESIZE_NEXT_COLUMN);

    /**** Not sure what exchangers, if any, are appropriate
          Probably just entity2Relations ? ***/
    table.setSelectedAnnotationExchanger(ACE2004Utils.ENTITY_TYPE,
                                         toolkit.entity2Relations);
    /****
    table.setSelectedAnnotationExchanger(ACE2004Utils.ARGUMENT_MENTION_TYPE,
                                         toolkit.argMention2RelationMention);

    table.setSelectedAnnotationExchanger(ACE2004Utils.RELATION_MENTION_EXTENT_TYPE,
                                         toolkit.relation2Lexicals);
    table.setSelectedAnnotationExchanger(ACE2004Utils.RELATION_MENTION_EXTENT_TYPE,
                                         toolkit.relationExtent2Mention);

    ****/

    table.setAnnotationFilter (filter);

    TableColumn column = table.getColumnModel().getColumn(ID_COL);
    column.setCellRenderer(new IdRenderer());
    
    add(new JScrollPane(table), BorderLayout.CENTER);

    // ArgEditor is not currently used for Unmentioned Relations
    // commenting out in case needed in the future RK 1/19/2007
    /***************************************************************
    argEditor = new ArgumentEditor(toolkit, type);
    // Blech! how do we /know/ it's a BorderLayout JPanel?
    JComponent argComponent = (JComponent) argEditor.getComponent();
    argComponent.add(new JLabel(" Argument Mentions (From Relation Mentions) "),
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
    *******************************************************************/

    // a listener for mention insertion/removal from entities
    annotModelListener = new AnnotModelListener();
  }
  
  /**
   * Get the AnnotationTable this widget supports.
   */
  public AnnotationTable getTable () {
    return table;
  }

  /**
   * Entities need specialized code to get at the primary-mention
   */
  private class RelationModel extends AnnotationTableModel {
    public RelationModel (Task task, AnnotationType type,
                          Object[] attributes, String[] headings) {
      super (task, type, attributes, headings);
    }
    
    /** Overridden to display args properly. */
    public Object getValueAt (int row, int col) {

      if (col == ARG1_COL || col == ARG2_COL) {
        Object value = super.getValueAt (row, col);
        if (value != null) { 
          // value is an entity, display head of its primary mention
          AWBAnnotation primary = (AWBAnnotation)
            ((AWBAnnotation)value).getAttributeValue("primary-mention");
          System.err.println("found primary is " + primary);
          if (primary != null) {
            value = ((NamedExtentRegions)primary).getAttributeValue("head.TextExtent");
            System.err.println("found primary head extent is " + value);
          }
        }
        return value;
      } 

      return super.getValueAt (row, col);
    }
    
    /** Overridden for the sake of the type column, setting of which
        may necessitate clearing the subtype column. */
    public void setValueAt(Object value, int row, int col) {
      AWBAnnotation relation = (AWBAnnotation) getAnnotation(row);
      if (col == TYPE_COL) {
	// when type is set, clear subtype if it is inconsistent
        try {
          relation.setAttributeValue("type", value);
          
          String subtype = (String)relation.getAttributeValue("subtype");
          if (!getTask().getPossibleValues(relation, "subtype").contains(subtype)) {
            if (DEBUG > 0)
              System.err.println("RelEditor.setValAt: Clearing invalid subtype: "
                                 + subtype);
            relation.setAttributeValue("subtype", "");
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        return;
      }
      
      super.setValueAt(value, row, col);
    }

  }

  /***********************************************************************/
  /* Implementing JawbComponent */
  /***********************************************************************/
  
  public void setJawbDocument (JawbDocument doc) {
    JawbDocument old = getJawbDocument();
    if (old != null)
      old.getAnnotationModel().removeAnnotationModelListener(annotModelListener);
    if (doc != null)
      doc.getAnnotationModel().addAnnotationModelListener(annotModelListener);

    //    argEditor.setJawbDocument(doc); not needed for now RK 1/19/07
    table.setJawbDocument (doc);
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
  /* Implementing AnnotationModelListener */
  /***********************************************************************/

  /**
   * The AnnotationModel updates for additions to the list, we just need to
   * know when multiple timex, or time-range tags are in a relation so we can
   * increase the height of the row
   */
  private class AnnotModelListener implements AnnotationModelListener {

    /** Invoked after an annotation has been created. */
    public void annotationCreated (AnnotationModelEvent e) {}
    
    /** Invoked after an annotation has been deleted. */
    public void annotationDeleted (AnnotationModelEvent e) {}
    
    /** Invoked after an annotation has been changed. */
    public void annotationChanged (AnnotationModelEvent e) {
      AWBAnnotation annot = e.getAnnotation ();
      // redisplay the type/subtypes when they change, since they're derived.
      if (annot.getAnnotationType ().equals (ACE2004Utils.RELATION_TYPE)) {
        String name = e.getChange().getPropertyName();
        AWBAnnotation[] mentions = null;
        if ("type".equals(name)) {
          mentions = ((HasSubordinates)annot).getSubordinates(ACE2004Utils.RELATION_MENTION_TYPE);
          for (int i=0; i<mentions.length; i++) {
            int row = table.getRow (mentions[i]);
            table.repaint (table.getCellRect(row, TYPE_COL, true));
          }
        }
        else if ("subtype".equals(name)) {
          mentions = ((HasSubordinates)annot).getSubordinates(ACE2004Utils.RELATION_MENTION_TYPE);
          for (int i=0; i<mentions.length; i++) {
            int row = table.getRow (mentions[i]);
            table.repaint (table.getCellRect(row, SUBTYPE_COL, true));
          }
        }
        /***
        else if ("modality".equals(name)) {
          mentions = ((HasSubordinates)annot).getSubordinates(ACE2004Utils.RELATION_MENTION_TYPE);
          for (int i=0; i<mentions.length; i++) {
            int row = table.getRow (mentions[i]);
            table.repaint (table.getCellRect(row, MODALITY_COL, true));
          }
        }
        else if ("tense".equals(name)) {
          mentions = ((HasSubordinates)annot).getSubordinates(ACE2004Utils.RELATION_MENTION_TYPE);
          for (int i=0; i<mentions.length; i++) {
            int row = table.getRow (mentions[i]);
            table.repaint (table.getCellRect(row, TENSE_COL, true));
          }
        }
        ***/
      }
    }

    /** Invoked after an annotation has had subannotations inserted. */
    public void annotationInserted (AnnotationModelEvent e) {
      AWBAnnotation annot = e.getAnnotation ();
      // update ID of relMents who's relation had a relMent added. Only 1
      // really needs it tho
      if (annot.getAnnotationType ().equals (ACE2004Utils.RELATION_TYPE)) {
        AWBAnnotation[] relMentions = ((HasSubordinates)annot)
          .getSubordinates(ACE2004Utils.RELATION_MENTION_TYPE);
        for (int i=0; i<relMentions.length; i++) {
          int row = table.getRow (relMentions[i]);
          table.repaint (table.getCellRect(row, ID_COL, true));
        }
      }
    }
    
    /** Invoked after an annotation has had subannotations removed. */
    public void annotationRemoved (AnnotationModelEvent e) {}
  }// AnnotModelListener
  
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

        /***** argEditor not currently needed RK 1/19/07 ************
        // only interested when a single row is selected
        if (min < 0 || min != max)
          argEditor.setCurrentParent(null);
        else
          argEditor.setCurrentParent(table.getAnnotation(min));
        **********************************************************/


      } catch (Exception x) { x.printStackTrace (); }
    }
  }

  /***********************************************************************/
  /* For filtering annotations in the table */
  /***********************************************************************/

  // filters for annotations with the appropriate typeName who have no
  // subordinates of the type named by the subType parameter
  protected class UnmentionedFilter implements AnnotationFilter {

    public boolean accept (Annotation annot) {
      return ACE2004Task.isUnmentionedRelation((AWBAnnotation)annot);
    }
    public String getDescription () { return toString(); }
    public String toString () { return "Unmentioned Relations"; }
  }  
}
