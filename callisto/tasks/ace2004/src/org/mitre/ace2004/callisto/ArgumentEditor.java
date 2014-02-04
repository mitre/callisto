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
import gov.nist.atlas.util.ATLASElementSet;


import org.mitre.jawb.atlas.AnnotationModelEvent;
import org.mitre.jawb.atlas.AnnotationModelListener;
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.HasSubordinates;
import org.mitre.jawb.atlas.NamedExtentRegions;
import org.mitre.jawb.atlas.TextExtentRegion;
import org.mitre.jawb.gui.*;
import org.mitre.jawb.swing.*;
import org.mitre.jawb.swing.event.*;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;

/**
 * Display and editing widget for Event Annotations. Displays vaild
 * information in a JTable.
 *
 * TODO comments in this file all seem to refer to Event Annotations,
 * since this class was apparently derived from the code for
 * EventEditor -- these should be updated!
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 */
public class ArgumentEditor extends JPanel implements JawbComponent {

  private static final int DEBUG = 0;

  private static final int NAME_COL = 0;
  private static final int VALUE_ID_COL = 1;
  private static final int VALUE_COL = 2;

  // TODO: order of these attribs & headings is relied upon in ArgumentModel
  //    getValue is overridden to deal with null (which isn't an attribute)
  private static final String[] ATTRIBS =
    new String[] {"role", null, null};
  private static final String[] HEADINGS =
    new String[] {"Role", "Value ID", "Value"};
  
  ACE2004ToolKit toolkit;

  AnnotationType parentType = null;
  AnnotationTable table = null;
  AnnotModelListener annotModelListener = null;
  AWBAnnotation currentParent = null;
  
  /**
   * Creates a new <code>ArgumentEditor</code> instance.
   */
  public ArgumentEditor (TaskToolKit toolkit, AnnotationType parentType) {
    this.parentType = parentType;
    this.toolkit = (ACE2004ToolKit) toolkit;
    Task task = toolkit.getTask();
    init (task);
  }

  private void init (Task task) {

    // layout manager
    setLayout (new BorderLayout());

    // create and add table (with multiple column sorting)
    AnnotationTableModel atm =
      new ArgumentModel (task, ACE2004Utils.ARGUMENT_MENTION_TYPE,
                         ATTRIBS, HEADINGS);
    table = new AnnotationTable(toolkit, atm);
    table.setColumnEditable (VALUE_ID_COL, false);
    table.setColumnEditable (VALUE_COL, false);
    table.setAutoResizeMode(table.AUTO_RESIZE_NEXT_COLUMN);

    TableColumn column = table.getColumnModel().getColumn(VALUE_ID_COL);
    column.setCellRenderer(new IdRenderer());
    
    table.setAnnotationFilter(new MyArgumentFilter());

    setName("argumentEditor."+parentType.getName());
    table.setName("argumentEditor."+parentType.getName());

    add(new JScrollPane(table), BorderLayout.CENTER);

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
   * Events need specialized code to get at the primary-mention
   */
  private class ArgumentModel extends AnnotationTableModel {
    public ArgumentModel (Task task, AnnotationType type,
                        Object[] columnSrc, String[] headings) {
      super (task, type, columnSrc, headings);
    }

    /**
     * Overridden to display mention text extent properly, calculate
     * number of metions, and allow checkboxes for generic.
     */
    public Object getValueAt (int row, int col) {
	if (DEBUG > 0)
	    System.err.println("DSD: getValueAt row,col= " + row + ", " + col);
      if (col == VALUE_ID_COL || col == VALUE_COL) {
        AWBAnnotation annot = (AWBAnnotation) super.getValueAt (row, col);
        
        AWBAnnotation value =
          (AWBAnnotation) annot.getAttributeValue("entity-value");
        if (value == null)
          value = (AWBAnnotation) 
            annot.getAttributeValue("unmentioned-entity-value");
        if (value == null)
          value = (AWBAnnotation) annot.getAttributeValue("quantity-value");
        if (value == null)
          value = (AWBAnnotation) annot.getAttributeValue("timex2-value");
        if (value == null) {
	    if (DEBUG > 0)
		System.err.println("DSD: getValueAt returning null");
	    return "unrecognized argument value type (1)";
	}

	AnnotationType valType = value.getAnnotationType();
	
	if (DEBUG > 0)
	    System.err.println("DSD: getValueAt value= " + value.toString());

        if (col == VALUE_ID_COL) {
          return value;  //value.getAttributeValue("ace_id");
        } else { // VALUE_COL
          if (valType.equals(ACE2004Utils.QUANTITY_TYPE)) {
            return value.getAttributeValue("primary-mention").toString();
          } else if (valType.equals(ACE2004Utils.ENTITY_TYPE)) {
            NamedExtentRegions primary= 
              (NamedExtentRegions)value.getAttributeValue("primary-mention");
            return primary.getAttributeValue("head.TextExtent").toString();
          } else if (valType.equals(ACE2004Utils.TIMEX2_TYPE)) {
            return value.getAttributeValue("TextExtent").toString();
          } else if (valType.equals(ACE2004Utils.ENTITY_MENTION_TYPE)) {
            return value.getAttributeValue("head.TextExtent").toString();
          } else if (valType.equals(ACE2004Utils.QUANTITY_MENTION_TYPE)) {
            return value.getAttributeValue("TextExtent").toString();
          }
          return value.toString();
	}
      }
      if (DEBUG > 0)
	  System.err.println("DSD: super.getValueAt (row, col);");
      return super.getValueAt (row, col);
    }
  }

  /***********************************************************************/
  /* Implementing JawbComponent */
  /***********************************************************************/

  public void setJawbDocument (JawbDocument doc) {
    JawbDocument old = getJawbDocument();
    currentParent = null;

    if (old != null) {
      old.getAnnotationModel().removeAnnotationModelListener(annotModelListener);
    }
    if (doc != null) {
      doc.getAnnotationModel().addAnnotationModelListener(annotModelListener);
    }
    
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
  /*  */
  /***********************************************************************/

  /**
   * Called directly by editor for entities, entity-mentions, etc, when the
   * selection changes.
   */
  public void setCurrentParent(AWBAnnotation annot) {
      if (annot != null)
	if (DEBUG > 0)
	  System.err.println("DSD setCurrentParent: " + annot.toString());
    if (annot == currentParent)
      return;

    // if an annotation type being displayed here is selected, don't change the
    // table, this allows editing to continue here, even though the parent
    // isn't the selected annotation
    if (table.getSelectedRowCount() > 0)
      return;
    
    if (DEBUG > 0)
      System.err.println("ArgEditor.setCP: in "+getName()+"   "+
                         (annot==null?null:(annot.getId()+":"+
                                             annot.getAttributeValue("ace_id"))));
                                                     
    table.clearAnnotations();

    if (annot != null) {

      // sanity check
      AnnotationType type = annot.getAnnotationType();
      if (! type.equals(parentType))
        throw new IllegalArgumentException("Invalid type: "+type.getName()+
                                           " expected: "+parentType.getName());

      // set this now so the filter doesn't nix them!
      currentParent = annot;

      if (parentType.equals(ACE2004Utils.EVENT_MENTION_TYPE) ||
          parentType.equals(ACE2004Utils.RELATION_MENTION_TYPE)) {
        // collect the argument mentions of the parent
        addMentionArguments((Annotation)currentParent);
      }
      else if (parentType.equals(ACE2004Utils.EVENT_TYPE) ||
               parentType.equals(ACE2004Utils.RELATION_TYPE)) {
        ATLASElementSet subs;

        // find the mentions of the event or relation
        if (parentType.equals(ACE2004Utils.EVENT_TYPE)) {
          subs = currentParent.getRegion()
            .getSubordinateSet(ACE2004Utils.EVENT_MENTION_TYPE);
        }
        else {
          subs = currentParent.getRegion()
            .getSubordinateSet(ACE2004Utils.RELATION_MENTION_TYPE);
        }
        
        // collect the argument mentions of the mentions
        Iterator iter = subs.iterator();
        while (iter.hasNext())
          addMentionArguments((Annotation)iter.next());
      }
      else {
        currentParent = null;
        // sanity check
        throw new IllegalArgumentException("Unknown parent type: "+
                                           parentType.getName());
      }
      return;
    }
    currentParent = null;
  }

  /**
   * When the currentParent is set, the appropriate argument mentions are found
   * and added to the table automatically.
   */
  private void addMentionArguments(Annotation mention) {
    ATLASElementSet args = mention.getRegion()
      .getSubordinateSet(ACE2004Utils.ARGUMENT_MENTION_TYPE);
    Iterator argIter = args.iterator();
    while (argIter.hasNext()) {
      Object next = argIter.next();
      if (next instanceof AnnotationRef)
        next = ((AnnotationRef) next).getElement();
      if (DEBUG > 0)
	  System.err.println("DSD: addMentionArguments " + next.toString());
      table.addAnnotation((AWBAnnotation) next);
    }
  }
/*
  private String getDescription(AWBAnnotation annot) {
    if (annot == null)
      return "";
    
    StringBuffer descriptions = new StringBuffer();

    // TODO: clean up this hack around TextExtentAttributes
    // this is largely copied from MainTextPane.inspectAnnotations()
    AnnotationType type = annot.getAnnotationType();

    descriptions.append ('<').append (type.getName());

    if (type.equals(ACE2004Utils.EVENT_TYPE)) {
      annot =
        (AWBAnnotation) annot.getAttributeValue("primary-mention");
      if (annot != null)
        type = annot.getAnnotationType();
    }
    if (type.equals(ACE2004Utils.EVENT_MENTION_TYPE)) {
      String text = (String) annot.getAttributeValue("TextExtent");
      descriptions.append(" ").append(cleanText(text));
    }
    else if (type.equals(ACE2004Utils.RELATION_TYPE) ||
             type.equals(ACE2004Utils.RELATION_MENTION_TYPE)) {
      // retrieve the relations entities, and show primary mention values
      AWBAnnotation arg1 = (AWBAnnotation) annot.getAttributeValue("arg1");
      AWBAnnotation arg2 = (AWBAnnotation) annot.getAttributeValue("arg2");

      descriptions.append(" arg1=");
      String text = "<none>";
      if (arg1 != null) {
        if (arg1.getAnnotationType().equals(ACE2004Utils.ENTITY_TYPE))
          arg1 = (AWBAnnotation) arg1.getAttributeValue("primary-mention");
        if (arg1 != null)
          text = cleanText((String) arg1.getAttributeValue("head.TextExtent"));
      }
      descriptions.append(text);
      
      descriptions.append(" arg2=");
      text = "<none>";
      if (arg2 != null) {
        if (arg2.getAnnotationType().equals(ACE2004Utils.ENTITY_TYPE))
          arg2 = (AWBAnnotation) arg2.getAttributeValue("primary-mention");
        if (arg2 != null)
          text = cleanText((String) arg2.getAttributeValue("head.TextExtent"));
      }
      descriptions.append(text);
    }
    else {} // ???

    String[] keys = annot.getAttributeKeys ();
    for (int i=0; i<keys.length; i++) {
      Object value = null;
      // TODO: the first two work around a bug in PhraseAnnotation
      // TODO: the next three may indicate aproblem with the design
      // The last two skip empty attributes per request: RFE #408
      if (keys[i].equals ("end") || keys[i].equals ("start") ||
          keys[i].endsWith (TextExtentRegion.TEXT_EXTENT) ||
          keys[i].endsWith (TextExtentRegion.TEXT_EXTENT_START) ||
          keys[i].endsWith (TextExtentRegion.TEXT_EXTENT_END) ||
          (value = annot.getAttributeValue (keys[i])) == null ||
          value.equals (""))
        continue;
      
      descriptions.append (' ').append (keys[i]).append ("=\"");
      descriptions.append (value).append ('"');
    }
    descriptions.append (">");

    return descriptions.toString();
  }

  private String cleanText(String text) {
    text = text.replaceAll ("(\n\r|\n|\r)"," ");
    return "\""+text+"\"";
  }
*/
  /***********************************************************************/
  /* Filter for selected Properties */
  /***********************************************************************/
  
  private class MyArgumentFilter implements AnnotationFilter {
    public boolean accept(Annotation annot) {
      // TODO: this rebuilds the list for... every argument! don't do this

      if (currentParent != null) {
        
        if (parentType.equals(ACE2004Utils.EVENT_MENTION_TYPE) ||
            parentType.equals(ACE2004Utils.RELATION_MENTION_TYPE)) {

          ATLASElementSet args = currentParent.getRegion()
            .getSubordinateSet(ACE2004Utils.ARGUMENT_MENTION_TYPE);

          return args.contains(annot);
        }
        else if (parentType.equals(ACE2004Utils.EVENT_TYPE) ||
                 parentType.equals(ACE2004Utils.RELATION_TYPE)) {
          ATLASElementSet subs;
          
          // find the mentions of the event or relation
          if (parentType.equals(ACE2004Utils.EVENT_TYPE)) {
            subs = currentParent.getRegion()
              .getSubordinateSet(ACE2004Utils.EVENT_MENTION_TYPE);
          }
          else {
            subs = currentParent.getRegion()
              .getSubordinateSet(ACE2004Utils.RELATION_MENTION_TYPE);
          }
          
          Iterator iter = subs.iterator();
          while (iter.hasNext()) {
            Annotation sub = (Annotation) iter.next();
            ATLASElementSet subArgs = sub.getRegion()
              .getSubordinateSet(ACE2004Utils.ARGUMENT_MENTION_TYPE);

            if (subArgs.contains(annot))
              return true;
          }
        }
      }
      return false;
    }
    public String getDescription () { return "Properties of selected Event"; }
    public String toString () { return getDescription(); }
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
    public void annotationChanged (AnnotationModelEvent e) {}

    /** Invoked after an annotation has had subannotations inserted. */
    public void annotationInserted (AnnotationModelEvent e) {
      AWBAnnotation annot = e.getAnnotation ();
      if (currentParent != null && annot == currentParent) {
        AWBAnnotation[] inserted = e.getChange().getAnnotationsInserted();
        for (int i=0; i<inserted.length; i++)
          table.addAnnotation(inserted[i]);
      }
    }
    
    /** Invoked after an annotation has had subannotations removed. */
    public void annotationRemoved (AnnotationModelEvent e) {
      AWBAnnotation annot = e.getAnnotation ();
      if (currentParent != null && annot == currentParent) {
        AWBAnnotation[] removed = e.getChange().getAnnotationsRemoved();
        for (int i=0; i<removed.length; i++)
          table.removeAnnotation(removed[i]);
      }
    }
  }// AnnotModelListener
  
}
