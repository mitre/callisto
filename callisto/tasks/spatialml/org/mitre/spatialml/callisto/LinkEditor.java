/*
 * Copyright (c) 2002-2008 The MITRE Corporation
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

/**
 * 
 */
package org.mitre.spatialml.callisto;

import gov.nist.atlas.ATLASElement;
import gov.nist.atlas.Annotation;
import gov.nist.atlas.util.ATLASElementSet;
import gov.nist.atlas.util.MutableATLASElementSet;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.PhraseTaggingAnnotation;
import org.mitre.jawb.atlas.UnmodifiableAttributeException;
import org.mitre.jawb.gui.AnnotationFilter;
import org.mitre.jawb.gui.EnhancedMFTAnnotationEditor;
import org.mitre.jawb.gui.JawbDocument;
import org.mitre.jawb.gui.MultiFilterTypeAnnotEditor;
import org.mitre.jawb.gui.MultiFilterTypeAnnotEditor.SelectionAnnotationTableModel;
import org.mitre.jawb.swing.TableSorter;

/**
 * @author jricher
 *
 */
public class LinkEditor extends SpatialMLEditor {

  private Logger log = Logger.getLogger(SpatialMLEditor.class.getName());

  Set subordinates;
  
  MultiFilterTypeAnnotEditor signals;
  JComboBox source, target, linkType;
  JTextField comment;
  
  /**
   * @param annot
   */
  public LinkEditor(AWBAnnotation annot, Set subordinates, SpatialMLToolKit toolKit, JawbDocument jdoc) {
    super(annot);
    
    log.info("Subordinates: " + subordinates);
    
    this.subordinates = subordinates;

    Vector subVector = new Vector(); // needs to be a vector for combo boxes. WHY?
    
    subVector.add(null); // add a "none" capability

    Iterator it = subordinates.iterator();
    while (it.hasNext()) {
      AWBAnnotation n = (AWBAnnotation) it.next();
      if (n.getAnnotationType() != null && 
          (n.getAnnotationType().equals(SpatialMLUtils.PLACE_TYPE) || n.getAnnotationType().equals(SpatialMLUtils.RLINK_TYPE))) {
        subVector.add(n);
      } else {
        //it.remove(); // why was this even here?!?!
      }
    }
    
    String id = annot.getAttributeValue("id").toString();
    addLabelledRow("ID:", new JLabel(id));
    comment = addTextFieldRow("Comment:", "");
    source = addComboBoxRow("Source:", subVector);
    target = addComboBoxRow("Target:", subVector);
    source.setRenderer(new TextExtentComboBoxRenderer());
    target.setRenderer(new TextExtentComboBoxRenderer());
    signals = new EnhancedMFTAnnotationEditor(toolKit, SpatialMLTask.SIGNAL_NAME, new String[][]{
        {null, "Select", "yes", "yes"},
        {"id", "ID"},
        {null, "String"},
        {"comment", "Comment"}
    }, true) {
      
      protected AnnotationFilter createMasterAnnotationFilter() {
        
        return new AnnotationFilter() {
        
          public String getDescription() {
            return null;
          }
        
          public boolean accept(Annotation toFilter) {
            //if (true) return true;
            
            log.info("Filtering: " + toFilter + " against " + LinkEditor.this.subordinates);
            if (toFilter.getATLASType().equals(SpatialMLUtils.SIGNAL_TYPE)) {
              if (LinkEditor.this.subordinates.contains(toFilter)) {
                log.info("Passed!");
                return true;
              }
            }
            return false;
          }
        
        };        
      }
    };
    
    signals.getTable().getColumnModel().getColumn(2).setCellRenderer(new TextExtentTableCellRenderer());
    ((TableSorter)signals.getTable().getModel()).setClassComparator(PhraseTaggingAnnotation.class, new TextExtentComparator());
    
    signals.setJawbDocument(jdoc);
    
    addLabelledRow("Signals:", signals);
    
    linkType = addComboBoxRow("Link Type:", SpatialMLUtils.linkTypes);
    linkType.setRenderer(new NameMapComboBoxRenderer(SpatialMLUtils.linkTypeNameMap));
    
    // fill in the fields for us automatically
    reset();
    
  }
  /**
   * @see org.mitre.spatialml.callisto.SpatialMLEditor#clear()
   */
  public void clear() {
    super.clear();
    
    comment.setText("");
    source.setSelectedItem(null);
    target.setSelectedItem(null);
    ((SelectionAnnotationTableModel)signals.getAnnotationTableModel()).selectAll(false);
    linkType.setSelectedItem(null);
    
  }

  /**
   * @see org.mitre.spatialml.callisto.SpatialMLEditor#reset()
   */
  public void reset() {
    
    super.reset();
    
    setTextFromAttribute("comment", comment);
    AWBAnnotation srcAnnot = SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("source"));
    if (srcAnnot == null) {
      srcAnnot = SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("sourceRlink"));
    }
    AWBAnnotation tarAnnot = SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("target"));
    if (tarAnnot == null) {
      tarAnnot = SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("targetRlink"));
    }

    ATLASElementSet sigs = annot.getRegion().getSubordinateSet(SpatialMLUtils.SIGNAL_TYPE);
    SelectionAnnotationTableModel m = ((SelectionAnnotationTableModel)signals.getAnnotationTableModel());

    for (int i = 0; i < m.getRowCount(); i++) {
      AWBAnnotation ann = (AWBAnnotation) m.getValueAt(i, 2); // column 2 contains the annotation
      if (sigs.contains(ann)) {
        m.setValueAt(Boolean.TRUE, i, 0); // column 0 is the selection flag
      } else {
        m.setValueAt(Boolean.FALSE, i, 0); // column 0 is the selection flag
      }
    }
    
    //setSelectionFromAttribute("source", source);
    //setSelectionFromAttribute("target", target);
    source.setSelectedItem(srcAnnot);
    target.setSelectedItem(tarAnnot);
    setSelectionFromAttribute("linkType", linkType);
    
  }

  /**
   * @see org.mitre.spatialml.callisto.SpatialMLEditor#updateAnnotation()
   */
  public void updateAnnotation() {
    
    super.updateAnnotation();
    
    try {
      
      setAttributeFromText(comment, "comment");
      ATLASElement srcSel = (ATLASElement) source.getSelectedItem();
      if (srcSel != null) {
        if (srcSel.getATLASType().equals(SpatialMLUtils.PLACE_TYPE)) {
          annot.getRegion().setSubordinateWithRole(srcSel, "source");
        } else if (srcSel.getATLASType().equals(SpatialMLUtils.RLINK_TYPE)) {
          annot.getRegion().setSubordinateWithRole(srcSel, "sourceRlink");
        }
      }
      ATLASElement tarSel = (ATLASElement) target.getSelectedItem();
      if (tarSel != null) {
        if (tarSel.getATLASType().equals(SpatialMLUtils.PLACE_TYPE)) {
          annot.getRegion().setSubordinateWithRole(tarSel, "target");
        } else if (tarSel.getATLASType().equals(SpatialMLUtils.RLINK_TYPE)) {
          annot.getRegion().setSubordinateWithRole(tarSel, "targetRlink");
        }
      }

      MutableATLASElementSet subs = (MutableATLASElementSet) annot.getRegion().getSubordinateSet(SpatialMLUtils.SIGNAL_TYPE);
      subs.clear();
      
      Set selectedAnnotations = ((SelectionAnnotationTableModel)signals.getAnnotationTableModel()).getSelectedAnnotations();
      Iterator it = selectedAnnotations.iterator();
      while (it.hasNext()) {
        AWBAnnotation sub = (AWBAnnotation) it.next();
        annot.getRegion().addToSubordinateSet(sub);
      }      
      
      //setAttributeFromSelection(source, "source");
      //setAttributeFromSelection(target, "target");
      //annot.getRegion().setSubordinateWithRole(srcSel, "source");
      //annot.getRegion().setSubordinateWithRole(tarSel, "target");
      setAttributeFromSelection(linkType, "linkType");
      
    } catch (UnmodifiableAttributeException e) {
      // TODO Auto-generated catch block
      log.log(Level.WARNING, "Caught Exception", e);
    }
    
  }

  
  
}
