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
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.*;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleConstants;

import gov.nist.atlas.*;
import gov.nist.atlas.type.*;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.MultiPhraseAnnotation;
import org.mitre.jawb.atlas.SubordinateSetsAnnotation;
import org.mitre.jawb.gui.*;
import org.mitre.jawb.swing.*;
import org.mitre.jawb.swing.event.*;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;

// testing/debugging
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;

/**
 * Render (and allow user to edit?) Timex2 annotations from the text.
 *
 * @author <a href="mailto:robyn@mitre.org">Robyn Kozierok</a>
 * @version 1.0
 */
public class Timex2Table extends JPanel implements JawbComponent {

  private static final int DEBUG = 0;

  private static final int TIMEX2_ID_COL = 0;
  private static final int PHRASE_COL = 1;
  private static final int CAL_COL = 2;
  private static final int VAL_COL = 3;
  private static final int MOD_COL = 4;
  private static final int ANCHOR_DIR_COL = 5;
  private static final int ANCHOR_VAL_COL = 6;
  private static final int COMMENT_COL = 7;
  private static final int SET_COL = 8;
  private static final int NON_SPECIFIC_COL = 9;
  private static final int NUM_COLS = 10;
  
  private static final String COMPONENT_ID = "simpleTypeEditor";

  private static final String[] ATTRIBS =
    new String[] {null/*timex2-id*/, "TextExtent", "cal", "val", "mod", 
                  "anchor-dir", 
                  "anchor-val", "comment", "set", "non-specific"};

  private static final String[] HEADINGS =
    new String[] {"Timex2 ID", "Phrase", "Calendar", "Value", "Mod", 
                  "Anchor Dir", 
                  "Anchor Val", "Comments", "Set", "Non Specific",};

    ACE2004ToolKit toolkit;

    AnnotationTable table = null;
    JScrollPane scrollPane = null;
    PropertyChangeListener docPropListener = new DocPropertyListener();
    /* Timex2ArgEditor  argEditor; */
    Timex2RenderEditor argEditor;
  
  /**
   * Creates a new <code>Timex2Table</code> instance.
   */
  public Timex2Table (TaskToolKit toolkit) {
    this.toolkit = (ACE2004ToolKit) toolkit;
    Task task = toolkit.getTask();
    init (task, task.getAnnotationType (ACE2004Task.TIMEX2_TYPE_NAME));
    setName(COMPONENT_ID);
  }

  private void init (Task task, AnnotationType type) {

    // layout manager
    setLayout (new BorderLayout());

    // create and add table (with multiple column sorting)
    AnnotationTableModel atm =
      new TimexModel (task, type, ATTRIBS, HEADINGS);
    table = new AnnotationTable (toolkit, atm);
    table.setName(COMPONENT_ID);
    table.setAutoResizeMode(table.AUTO_RESIZE_NEXT_COLUMN);

    scrollPane = new JScrollPane (table);
    add (scrollPane, BorderLayout.CENTER);

    argEditor = new Timex2ArgEditor ();
    for (int i=CAL_COL; i<=COMMENT_COL; i++) {
      TableColumn column = table.getColumnModel().getColumn(i);
      column.setCellEditor (argEditor);
    }

    TableColumn column = table.getColumnModel().getColumn(TIMEX2_ID_COL);
    column.setCellRenderer(new IdRenderer("timex2-id"));

  }

  private class TimexModel extends AnnotationTableModel {
    public TimexModel (Task task, AnnotationType type,
		       Object[] columnSrc, String[] headings) {
      super (task, type, columnSrc, headings);
    }

    /**
     *  Overridden for pseudo-boolean columns
     */
    public Object getValueAt (int row, int col) {
      if (col == SET_COL || col == NON_SPECIFIC_COL) {
	String val = (String)super.getValueAt (row, col);
	if (val != null && val.equals("YES")) {
	  return Boolean.TRUE;
	} else {
	  return Boolean.FALSE;
	}
      }
      return super.getValueAt (row, col);
    }
    

    /**
     *  Overridden for pseudo-boolean columns
     */
    public void setValueAt(Object value, int row, int col) {
      if (col == SET_COL || col == NON_SPECIFIC_COL) {
	if (((Boolean)value).booleanValue()) {
	  value = "YES";
	} else {
	  value = "";
	}
      }
      super.setValueAt(value, row, col);
    }

    public Class getColumnClass (int col) {
      if (col == SET_COL || col == NON_SPECIFIC_COL) {
	return Boolean.class;
      }
      return super.getColumnClass(col);
    }

  }   
  
  /**
   * Get the AnnotationTable this widget supports.
   */
  public AnnotationTable getTable () {
    return table;
  }

  public Timex2Editor.Timex2Data getSavedDate() {
    return argEditor.getSavedDate();
  }

  /***********************************************************************/
  /* Implementing JawbComponent */
  /***********************************************************************/
  
  public void setJawbDocument (JawbDocument doc) {
    JawbDocument old = table.getJawbDocument();
    if (old != null) // had a doc before call
      old.removePropertyChangeListener (docPropListener);
    if (doc != null) {
      doc.addPropertyChangeListener(docPropListener);
      Font font = argEditor.getFont();
      argEditor.setFont (new Font(doc.getFontFamily(), font.getStyle(), font.getSize()));
      /*
      Font font = argEditor.getTimex2Font ();
      argEditor.setTimex2Font (new Font(doc.getFontFamily(),
					font.getStyle(),font.getSize()));
      */
    }
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

  /*************************************************************************/
  /* Value Renderer -- TODO? render value as "<none>" when "no-val" is YES */
  /*************************************************************************/

  /** Listen for Font changes and propogate to the Timex2Editor */
  private class DocPropertyListener implements PropertyChangeListener {
    public void propertyChange (PropertyChangeEvent evt) {
      String name = evt.getPropertyName ();
      if (JawbDocument.FONT_FAMILY_PROPERTY_KEY.equals (name)) {
        argEditor.setFont (new Font((String) evt.getNewValue(), Font.PLAIN,Font.PLAIN));
      }
    }
  }
}
