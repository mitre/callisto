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
 * Display and editing widget for Entity Annotations. Displays vaild
 * information in a JTable.
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 */
public class MentionEditor extends JPanel implements JawbComponent {

  private static final int DEBUG = 0;

  private static final int ID_COL = 0;

  private static final String COMPONENT_ID = "mentionEditor";

  private static final String[] ATTRIBS =
    new String[] {null /*ace_id*/, "full.TextExtent", "head.TextExtent",
                  "type", "role",
                  "ldctype", "metonymy", "ldcatr"};
  private static final String[] HEADINGS =
    new String[] {"ID", "Mention", "Head",
                  "Type", "Role", "ldctype", "Metonymy",
                  "ldcatr"};//, "Primary?"};

  ACE2004ToolKit toolkit;

  AnnotationTable table = null;

  /**
   * Creates a new <code>MentionEditor</code> instance.
   */
  public MentionEditor (TaskToolKit toolkit) {
    this.toolkit = (ACE2004ToolKit) toolkit;
    Task task = toolkit.getTask();
    init(task, ACE2004Utils.ENTITY_MENTION_TYPE);
    setName(COMPONENT_ID);
  }

  private void init (Task task, AnnotationType type) {

    // layout manager
    setLayout (new BorderLayout());

    // create and add table (with multiple column sorting)
    AnnotationTableModel atm =
      new AnnotationTableModel (task, type, ATTRIBS, HEADINGS);
    table = new AnnotationTable (toolkit, atm);
    table.setName(COMPONENT_ID);
    table.setAutoResizeMode(table.AUTO_RESIZE_NEXT_COLUMN);

    table.setSelectedAnnotationExchanger(ACE2004Utils.ENTITY_TYPE,
                                         toolkit.entity2Mentions);
    table.setSelectedAnnotationExchanger(ACE2004Utils.RELATION_MENTION_TYPE,
                                         toolkit.relation2Mentions);

    TableColumn column = table.getColumnModel().getColumn(ID_COL);
    column.setCellRenderer(new IdRenderer());

    add(new JScrollPane(table), BorderLayout.CENTER);
  }
  
  /**
   * Get the AnnotationTable this widget supports.
   */
  public AnnotationTable getTable () {
    return table;
  }

  /***********************************************************************/
  /* Implementing JawbComponent */
  /***********************************************************************/
  
  public void setJawbDocument (JawbDocument doc) {
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
}
