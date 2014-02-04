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
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */
public class RelationEditor extends JSplitPane implements JawbComponent {

  private static final int DEBUG = 0;

  // TODO: order of these attribs & headings is relied upon in RelationModel
  // and all over... these constants may help
  private static final int ID_COL = 0;
  private static final int ARG1_COL = 1;
  private static final int TYPE_COL = 2;
  private static final int SUBTYPE_COL = 3;
  private static final int ARG2_COL = 4;
  private static final int MODALITY_COL = 5;
  private static final int TENSE_COL = 6;
  private static final int LEXICAL_COND_COL = 7;
  private static final int EXTENT_COL = 8;

  private static final String COMPONENT_ID = "relationEditor";

  // ID type and subtype use null as the attribute, so that the model will
  // return the relation-mention annot itself. That is used to retrieve the
  // relation, and the attributes values from there.
  private static final Object[] ATTRIBS =
  new Object[] {null/*ace_id*/, "arg1", null /*type*/, null /*subtype*/,
                "arg2", null/*modality*/, null/*tense*/, "lexicalcondition", "relation-mention-extent"};

  private static final String[] HEADINGS =
    new String[] {"Relation ID", "Arg1", "Type", "Subtype", "Arg2",
                  "Modality", "Tense", "Lexical Cond.", "Extent"};
  ACE2004ToolKit toolkit;
  
  AnnotationTable table = null;
  ArgumentEditor argEditor = null;

  /** Observes changes to annots, watching for mention insertion/removal. */
  AnnotModelListener annotModelListener = null;

  /**
   * Creates a new <code>RelationEditor</code> instance.
   */
  public RelationEditor (TaskToolKit toolkit) {
    this.toolkit = (ACE2004ToolKit) toolkit;
    Task task = toolkit.getTask();
    init (task, ACE2004Utils.RELATION_MENTION_TYPE);
    setName(COMPONENT_ID);
  }

  private void init (Task task, AnnotationType type) {

    // layout manager
    setLayout (new BorderLayout());
    
    // create and add table (with multiple column sorting)
    AnnotationTableModel atm = new RelationModel(task, type, ATTRIBS, HEADINGS);
    table = new AnnotationTable (toolkit, atm);
    table.setName(COMPONENT_ID);
    table.setColumnEditable(ID_COL, false);
    table.setColumnEditable(ARG1_COL, false);
    table.setColumnEditable(ARG2_COL, false);
    table.setColumnEditable(TYPE_COL, true);
    table.setColumnEditable(SUBTYPE_COL, true);
    table.setColumnEditable(MODALITY_COL, true);
    table.setColumnEditable(TENSE_COL, true);
    table.setColumnEditable(EXTENT_COL, false);
    table.setAutoResizeMode(table.AUTO_RESIZE_NEXT_COLUMN);

    table.setSelectedAnnotationExchanger(ACE2004Utils.ENTITY_MENTION_TYPE,
                                         toolkit.mention2Relations);
    table.setSelectedAnnotationExchanger(ACE2004Utils.RELATION_MENTION_EXTENT_TYPE,
                                         toolkit.relation2Lexicals);
    table.setSelectedAnnotationExchanger(ACE2004Utils.ARGUMENT_MENTION_TYPE,
                                         toolkit.argMention2RelationMention);
    table.setSelectedAnnotationExchanger(ACE2004Utils.RELATION_MENTION_EXTENT_TYPE,
                                         toolkit.relationExtent2Mention);

    TableColumn column = table.getColumnModel().getColumn(ID_COL);
    column.setCellRenderer(new IdRenderer());
    
    argEditor = new ArgumentEditor(toolkit, type);
    // sloppy -- how do we /know/ it's a BorderLayout JPanel?
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
    
    /** Overridden to display mention text extent properly. */
    public Object getValueAt (int row, int col) {
      if (col == ID_COL) {
        // overridden to give the idrenderer relation, not relation-mention
        AWBAnnotation mention = (AWBAnnotation)getAnnotation (row);
        return ACE2004Task.getMentionParent(mention);

      } else if (col == ARG1_COL || col == ARG2_COL) {
        Object value = super.getValueAt (row, col);
        boolean entityArg = false;
        if (value == null) {
          AWBAnnotation mention = (AWBAnnotation)getAnnotation (row);
          AWBAnnotation relation = ACE2004Task.getMentionParent(mention);
          // check and see if the parent relation has an entity argument 
          // if so, grab its primary mentiom here, so that further down
          // we can grab its head text extent to put in the table
          AWBAnnotation entity = null;
          if (relation == null)
            System.err.println("RelEd.getValAt: mention " + mention + 
                               " with id: " +
                               mention.getAttributeValue("ace_id") + 
                               " has null relation parent!");

          if (col == ARG1_COL) {
            entity = (AWBAnnotation)relation.getAttributeValue("arg1");
          } else {
            entity = (AWBAnnotation)relation.getAttributeValue("arg2");
          }
          if (entity == null)
            value = null;
          else {
            value = entity.getAttributeValue("primary-mention");
            entityArg = true;
          }
        }
          
        System.err.println("RelEd.getValueAt (" + row + "," + col +
                           ") value is " + value);

        if (value != null)
          value = ((NamedExtentRegions) value).getAttributeValue("head.TextExtent");

        if (entityArg && value != null)
          value = "*" + (String)value;

        return value;
  
      } else if (col == TYPE_COL) {
        AWBAnnotation mention = (AWBAnnotation)getAnnotation (row);
        AWBAnnotation relation = ACE2004Task.getMentionParent(mention);
        if (relation != null)
          return relation.getAttributeValue("type");
        
      } else if (col == SUBTYPE_COL) {
        AWBAnnotation mention = (AWBAnnotation)getAnnotation (row);
        AWBAnnotation relation = ACE2004Task.getMentionParent(mention);
        if (relation != null)
          return relation.getAttributeValue("subtype");
        
      } else if (col == MODALITY_COL) {
        AWBAnnotation mention = (AWBAnnotation)getAnnotation (row);
        AWBAnnotation relation = ACE2004Task.getMentionParent(mention);
        if (relation != null)
          return relation.getAttributeValue("modality");
        
      } else if (col == TENSE_COL) {
        AWBAnnotation mention = (AWBAnnotation)getAnnotation (row);
        AWBAnnotation relation = ACE2004Task.getMentionParent(mention);
        if (relation != null)
          return relation.getAttributeValue("tense");

      } else if (col == EXTENT_COL) {
	  AWBAnnotation relMention = (AWBAnnotation) getAnnotation (row);
	  if (DEBUG > 0)
	      System.err.println("relEditor getValueAt extent in relationMention; relMention = " + relMention);
	  if (relMention != null) {
	      TextExtentRegion extentRegion = (TextExtentRegion) relMention.getAttributeValue("relation-mention-extent");
	      if (DEBUG > 0)
		  System.err.println("relEditor extentRegion = " + extentRegion);
	      int start = extentRegion.getTextExtentStart();
	      int end   = extentRegion.getTextExtentEnd();
	      String text = extentRegion.getTextExtent();
	      if (DEBUG > 0)
		  System.err.println("relEditor start, end, text = " + start + "," + end + "," + text);
	      return text;
	      // return extentRegion;
	  } else {
	      return super.getValueAt (row, col);	  
	  }
      }

      return super.getValueAt (row, col);
    }
    
    /** Overridden for the sake of the explicit column. */
    public void setValueAt(Object value, int row, int col) {
      if (col == TYPE_COL) {
	// when type is set, clear subtype if it is inconsistent
	AWBAnnotation mention = (AWBAnnotation) getAnnotation(row);
        AWBAnnotation relation = ACE2004Task.getMentionParent(mention);
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
        
      } else if (col == SUBTYPE_COL) {
        AWBAnnotation mention = (AWBAnnotation)getAnnotation(row);
        AWBAnnotation relation = ACE2004Task.getMentionParent(mention);
        try {
          relation.setAttributeValue("subtype", value);
        } catch (Exception e) {
          e.printStackTrace();
        }
        return;
      } else if (col == MODALITY_COL) {
        AWBAnnotation mention = (AWBAnnotation)getAnnotation (row);
        AWBAnnotation relation = ACE2004Task.getMentionParent(mention);
        try {
          relation.setAttributeValue("modality", value);
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else if (col == TENSE_COL) {
        AWBAnnotation mention = (AWBAnnotation)getAnnotation (row);
        AWBAnnotation relation = ACE2004Task.getMentionParent(mention);
        try {
          relation.setAttributeValue("tense", value);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      
      super.setValueAt(value, row, col);
    }

    /** Overridden to get type and subtype, for relations, since
     * relation-mentions have no type subtype */
    public Set getPossibleValues (AWBAnnotation annot, int col) {
      Set set = null;
      if (col == TYPE_COL) {
        AWBAnnotation relation = ACE2004Task.getMentionParent(annot);
        set = getTask().getPossibleValues(relation, "type");
        
      } else if (col == SUBTYPE_COL) {
        AWBAnnotation relation = ACE2004Task.getMentionParent(annot);
        set = getTask().getPossibleValues(relation, "subtype");
        
      } else if (col == MODALITY_COL) {
        AWBAnnotation relation = ACE2004Task.getMentionParent(annot);
        set = getTask().getPossibleValues(relation, "modality");
        
      } else if (col == TENSE_COL) {
        AWBAnnotation relation = ACE2004Task.getMentionParent(annot);
        set = getTask().getPossibleValues(relation, "tense");
        
      } else {
        set = super.getPossibleValues(annot, col);
      }
      return set;
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

    argEditor.setJawbDocument(doc);
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

        // only interested when a single row is selected
        if (min < 0 || min != max)
          argEditor.setCurrentParent(null);
        else
          argEditor.setCurrentParent(table.getAnnotation(min));

      } catch (Exception x) { x.printStackTrace (); }
    }
  }
}
