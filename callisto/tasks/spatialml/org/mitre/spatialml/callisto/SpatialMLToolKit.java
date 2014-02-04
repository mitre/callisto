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

package org.mitre.spatialml.callisto;

import gov.nist.atlas.type.AnnotationType;
import gov.nist.atlas.type.RegionType;
import gov.nist.atlas.util.ATLASElementSet;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.Caret;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.AWBDocument;
import org.mitre.jawb.atlas.HasSubordinates;
import org.mitre.jawb.atlas.PhraseTaggingAnnotation;
import org.mitre.jawb.atlas.SubordinateSetsAnnotation;
import org.mitre.jawb.atlas.TextExtentRegion;
import org.mitre.jawb.atlas.UnmodifiableAttributeException;
import org.mitre.jawb.gui.AnnotationAction;
import org.mitre.jawb.gui.AnnotationPopupListener;
import org.mitre.jawb.gui.CreateTextAnnotAction;
import org.mitre.jawb.gui.DeleteAnnotAction;
import org.mitre.jawb.gui.DetachableTabsJawbComponent;
import org.mitre.jawb.gui.EnhancedMFTAnnotationEditor;
import org.mitre.jawb.gui.GUIUtils;
import org.mitre.jawb.gui.JawbComponent;
import org.mitre.jawb.gui.JawbDocument;
import org.mitre.jawb.gui.MainTextPane;
import org.mitre.jawb.gui.MultiFilterTypeAnnotEditor;
import org.mitre.jawb.swing.ItemExchanger;
import org.mitre.jawb.swing.TableSorter;
import org.mitre.jawb.tasks.AbstractToolKit;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;

/**
 * A TaskToolKit is a pluggable module that allows for differnt editing
 * mechanisms for each of the tasks Callisto may use. All the gui specific
 * stuff is here.
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */
public class SpatialMLToolKit extends AbstractToolKit {


  private static final Logger log = Logger.getLogger(SpatialMLToolKit.class.getName());

  private SpatialMLTask task;

  private Set actions = null;
  private JawbComponent editorComponent;

  private MultiFilterTypeAnnotEditor placeTable;
  private MultiFilterTypeAnnotEditor linkTable;
  private MultiFilterTypeAnnotEditor signalTable;
  private MultiFilterTypeAnnotEditor rlinkTable;

  SpatialMLToolKit (SpatialMLTask task) {
    this.task = task;
  }

  public Task getTask () {
    return task;
  }

  /**
   *
   */
  public JawbComponent getMainComponent () {
    mainTextPane = new MyMainTextPane(this);
    // wire up selection listeners
    AbstractAnnotationExchanger rlink2subs = new Ancestor2Subordinates(SpatialMLUtils.RLINK_TYPE, true);
    mainTextPane.setSelectedAnnotationExchanger(rlink2subs.fromType, rlink2subs);
    AbstractAnnotationExchanger link2subs = new Ancestor2Subordinates(SpatialMLUtils.LINK_TYPE, true);
    mainTextPane.setSelectedAnnotationExchanger(link2subs.fromType, link2subs);
    //AbstractAnnotationExchanger place2refs = new Subordinate2Ancestors(SpatialMLUtils.PLACE_TYPE, true);
    //mainTextPane.setSelectedAnnotationExchanger(place2refs.fromType, place2refs);
    //AbstractAnnotationExchanger signal2refs = new Subordinate2Ancestors(SpatialMLUtils.SIGNAL_TYPE, true);
    //mainTextPane.setSelectedAnnotationExchanger(signal2refs.fromType, signal2refs);
    
    return mainTextPane;
  }

  /**
   * Returns the JawbComponent to edit this tasks annotations. Always the same
   * object for a given RDCEditorKit.
   */
  public JawbComponent getEditorComponent () {
    if (editorComponent == null)
      initEditorComponent ();
    return editorComponent;
  }

  /**
   * Some of these actions are tied to the SubordinateAssignor.
   */
  public Set getActions () {
    if (actions == null)  // lazy aren't we
      initActions ();
    return actions;
  }

  /**
   * Safely delete the annotation, taking care of any cascades.
   * @see org.mitre.jawb.tasks.AbstractToolKit#deleteAnnotation(org.mitre.jawb.atlas.AWBAnnotation, org.mitre.jawb.gui.JawbDocument)
   */
  
  public boolean deleteAnnotation (AWBAnnotation annot, JawbDocument doc) {

    // do any special processing that we need to in order to make the delete actually happen

    // ALWAYS fall through and delete the annotation we were asked about to begin with
    // unless we've already returned with an error above
    
    return doc.deleteAnnotation (annot);
  }


  // holders for auto-complete
  private TextExtentRegion lastCreatedPlace = null;

  private MyMainTextPane mainTextPane;
  
  private void initActions () {
    actions = new LinkedHashSet ();
    actions.add (new CreatePlaceTagAction(task));
    actions.add (new CreateSignalTagAction(task));
    
    actions.add(new CreateLinkTagAction(task));
    actions.add(new CreateRLinkTagAction(task));
    
    actions.add(new EditPlaceTagAction(task));
    actions.add(new EditRLinkTagAction(task));
    actions.add(new EditLinkTagAction(task));
    
    actions.add(new UpdateIDsAction(task, SpatialMLUtils.PLACE_TYPE));
    actions.add(new UpdateIDsAction(task, SpatialMLUtils.SIGNAL_TYPE));
    actions.add(new UpdateIDsAction(task, SpatialMLUtils.RLINK_TYPE));
    actions.add(new UpdateIDsAction(task, SpatialMLUtils.LINK_TYPE));
    
    //actions.add (new SetSavedValueAction (task.TIMEX2_NAME));
    actions.add (new DeleteAnnotAction(this));
    
  }

  /* initialize the editor component and the annotation popup listener */

  private void initEditorComponent () {
    final AnnotationPopupListener popupListener = 
      new AnnotationPopupListener(this);
    // override the setJawbDocument method to handle registration of the
    // popupListener
    JTabbedPane tp = new DetachableTabsJawbComponent (task.getName()) {
      public void setJawbDocument (JawbDocument doc) {
        JawbDocument old = getJawbDocument();
        super.setJawbDocument(doc);
        if (old != null)
          old.getAnnotationMouseModel().
          removeAnnotationMouseListener(popupListener);
        if (doc != null)
          doc.getAnnotationMouseModel().
          addAnnotationMouseListener(popupListener);
      }
    };
    
    /*
    AnnotationType type = task.getAnnotationType(task.PATH_NAME);
    Set s = task.getAttributes(type);
    
    System.err.println(s.toString());
    
    System.exit(1);
  */
    
    placeTable = new EnhancedMFTAnnotationEditor(this, SpatialMLTask.PLACE_NAME, new String[][]{
        {"id", "ID"}, 
        {null, "String"},  
        {"gazref", "Gaz. Ref.", "yes"},
        {"comment", "Comment", "yes"},
        {"type", "Type", "yes"},
        {"mod", "Mod", "yes"},
        {"continent", "Continent", "yes"},
        {"country", "Country", "yes"},
        {"county", "County", "yes"},
        {"state", "State", "yes"},
        {"form", "Form", "yes"},
        {"CTV", "C/T/V", "yes"},
        {"latLong", "Lat/Long", "yes"},
        {"nonLocUse", "Non Loc. Use", "yes", "yes"},
        {"predicative", "Predicative", "yes", "yes"},
        {"description", "Description", "yes"}
    }, new String[][]{
        {"continent", "Continent", "country", "Country", "type", "Type", "mod", "Mod", "CTV", "C/T/V", "form", "Form"},
        SpatialMLUtils.continents,
        SpatialMLUtils.countryCodes,
        SpatialMLUtils.placeTypes,
        SpatialMLUtils.mods,
        SpatialMLUtils.ctv,
        SpatialMLUtils.forms
    });
    
    placeTable.getTable().getColumnModel().getColumn(1).setCellRenderer(new TextExtentTableCellRenderer());
    ((TableSorter)placeTable.getTable().getModel()).setClassComparator(PhraseTaggingAnnotation.class, new TextExtentComparator());

    signalTable = new EnhancedMFTAnnotationEditor(this, SpatialMLTask.SIGNAL_NAME, new String[][]{
        {"id", "ID"},
        {null, "String"},
        {"comment", "Comment", "yes"},
        {"signalType", "Type", "yes"}
    }, new String[][]{
        {"signalType", "Type"},
        SpatialMLUtils.signalTypes
    });
    
    signalTable.getTable().getColumnModel().getColumn(1).setCellRenderer(new TextExtentTableCellRenderer());
    ((TableSorter)signalTable.getTable().getModel()).setClassComparator(PhraseTaggingAnnotation.class, new TextExtentComparator());    
    
    rlinkTable = new EnhancedMFTAnnotationEditor(this, SpatialMLTask.RLINK_NAME, new String[][]{
        {"id", "ID"},
        {"comment", "Comment", "yes"},
        {"source", "Source", "yes"},
        {"target", "Target", "yes"},
        {null, "Signals"},
        {"frame", "Frame", "yes"},
        {"direction", "Direction", "yes"},
        {"distance", "Distance", "yes"},
    }, new String[][]{
        {"direction", "Direction", "frame", "Frame"},
        SpatialMLUtils.directions,
        SpatialMLUtils.frames
    });
    
    // custom renderers for subordinate IDs
    rlinkTable.getTable().getColumnModel().getColumn(2).setCellRenderer(new AnnotationIdTableCellRenderer());
    rlinkTable.getTable().getColumnModel().getColumn(3).setCellRenderer(new AnnotationIdTableCellRenderer());
    
    // custom renderer for subordinate sets
    rlinkTable.getTable().getColumnModel().getColumn(4).setCellRenderer(new SignalSetTableCellRenderer());
    
    linkTable = new EnhancedMFTAnnotationEditor(this, SpatialMLTask.LINK_NAME, new String[][]{
        {"id", "ID"},
        {"comment", "Comment", "yes"},
        {null, "Source"},
        {null, "Target"},
        {null, "Signals"},
        {"linkType", "Link Type", "yes"}
    }, new String[][]{});
    
    //linkTable.getTable().getColumnModel().getColumn(2).setCellRenderer(new AnnotationIdTableCellRenderer());
    //linkTable.getTable().getColumnModel().getColumn(3).setCellRenderer(new AnnotationIdTableCellRenderer());
    // custom renderer for "source" column
    linkTable.getTable().getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
      /**
       * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
       */
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value != null && value instanceof AWBAnnotation) {
          AWBAnnotation annot = (AWBAnnotation)value;
          
          AWBAnnotation src = SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("source"));
          if (src == null) {
            src = SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("sourceRlink"));
          }
          
          String label = ""; // nulls get a blank
          if (src != null) {
          
            label = String.valueOf(src.getAttributeValue("id"));
            
            if (src instanceof TextExtentRegion) {
              label += ": " + ((TextExtentRegion)src).getTextExtent();
            }
          }
          return super.getTableCellRendererComponent(table, label, isSelected, hasFocus, row, column);
        } else {
          return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
      }
      
    });
    
    // custom renderer for 'target' column
    linkTable.getTable().getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
      /**
       * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
       */
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value != null && value instanceof AWBAnnotation) {
          AWBAnnotation annot = (AWBAnnotation)value;
          
          AWBAnnotation target = SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("target"));
          if (target == null) {
            target = SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("targetRlink"));
          }
          
          String label = ""; // nulls get a blank
          if (target != null) {
          
            label = String.valueOf(target.getAttributeValue("id"));
            
            if (target instanceof TextExtentRegion) {
              label += ": " + ((TextExtentRegion)target).getTextExtent();
            }
          }
          return super.getTableCellRendererComponent(table, label, isSelected, hasFocus, row, column);
        } else {
          return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
      }
      
    });
    
    linkTable.getTable().getColumnModel().getColumn(4).setCellRenderer(new SignalSetTableCellRenderer());

    // wire up selection listeners
    AbstractAnnotationExchanger rlink2subs = new Ancestor2Subordinates(SpatialMLUtils.RLINK_TYPE, true);
    placeTable.getTable().setSelectedAnnotationExchanger(rlink2subs.fromType, rlink2subs);
    signalTable.getTable().setSelectedAnnotationExchanger(rlink2subs.fromType, rlink2subs);
    
    AbstractAnnotationExchanger link2subs = new Ancestor2Subordinates(SpatialMLUtils.LINK_TYPE, true);
    placeTable.getTable().setSelectedAnnotationExchanger(link2subs.fromType, link2subs);
    
    AbstractAnnotationExchanger place2refs = new Subordinate2Ancestors(SpatialMLUtils.PLACE_TYPE, true);
    rlinkTable.getTable().setSelectedAnnotationExchanger(place2refs.fromType, place2refs);
    linkTable.getTable().setSelectedAnnotationExchanger(place2refs.fromType, place2refs);
    
    AbstractAnnotationExchanger signal2refs = new Subordinate2Ancestors(SpatialMLUtils.SIGNAL_TYPE, true);
    rlinkTable.getTable().setSelectedAnnotationExchanger(signal2refs.fromType, signal2refs);

    
    
    tp.add("Place", placeTable);
    tp.add("Signal", signalTable);
    tp.add("RLink", rlinkTable);
    tp.add("Link", linkTable);

    editorComponent = (JawbComponent)tp;


  }

  private class SignalSetTableCellRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      
      if (value instanceof AWBAnnotation) {
        AWBAnnotation annot = (AWBAnnotation) value;
        
        ATLASElementSet subs = annot.getRegion().getSubordinateSet(SpatialMLUtils.SIGNAL_TYPE);
        
        //log.info("Subs:" + subs + " :: " + subs.asAIFString());
        //subs.
        
        Iterator it = subs.iterator();
        if (it.hasNext()) {
          
          AWBAnnotation el = SpatialMLUtils.convertAnnotation(it.next());

          StringBuffer buf = new StringBuffer(); 
          if (el != null) {
          
            buf.append(el.getAttributeValue("id").toString());
            
            el = null;
            
            while (it.hasNext()) {
              el = SpatialMLUtils.convertAnnotation(it.next());

              if (el != null) {
                buf.append(", ");
                buf.append(el.getAttributeValue("id").toString());
              }
              el = null;
            }
          }
          return super.getTableCellRendererComponent(table, buf.toString(), isSelected, hasFocus, row, column);
        } else {
          // no subordinates
          return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
        }
        
      } else {
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      }
    }
  }

  class MyCreateTagAction extends CreateTextAnnotAction {
    private String attrType;
    public MyCreateTagAction (Task task, String type, String attrType) {
      super (attrType+" Tag", task, task.getAnnotationType (type), attrType);
      this.attrType = attrType;
    }
    public void actionPerformed (ActionEvent e) {
      AWBAnnotation annot = super.createAnnotation (e);
      try {
        annot.setAttributeValue ("type", attrType);
      } catch (Exception x) {};
    }
  }

  private class CreatePlaceTagAction extends CreateTextAnnotAction {
    
    public CreatePlaceTagAction (Task task) {
      super ("Place", task, task.getAnnotationType(SpatialMLTask.PLACE_NAME), SpatialMLTask.PLACE_NAME);
    }
    public void actionPerformed (ActionEvent e) {
      final TextExtentRegion annot = (TextExtentRegion)super.createAnnotation (e);
      final JawbDocument jdoc = GUIUtils.getJawbDocument(e);

      String id = IdTracker.getIdTracker((AWBDocument)jdoc.getAnnotationModel()).getId(annot);
      try {
        annot.setAttributeValue("id", id);
      } catch (UnmodifiableAttributeException e1) {
        log.log(Level.WARNING, "Caught Exception", e1);
      }

      final SpatialMLEditor editor = new PlaceEditor(annot, lastCreatedPlace);
      SpatialMLEditorDialog dialog = new SpatialMLEditorDialog(JOptionPane.getFrameForComponent(getEditorComponent().getComponent()), 
          "Place Editor", false,
          editor,
          new ActionListener() { // OK Action
            public void actionPerformed(ActionEvent e) {
              // save the annotation updates from the form
              editor.updateAnnotation();
              // save the annotation for the next time around
              lastCreatedPlace = annot;
            }
          },
          new ActionListener() { // Cancel Action
            public void actionPerformed(ActionEvent e) {
              deleteAnnotation(annot, jdoc);
            }
          });

      dialog.show();
    }
  }

  private class EditPlaceTagAction extends AnnotationAction {

    public EditPlaceTagAction(Task task) {
      super("Edit Place", task.getAnnotationType(SpatialMLTask.PLACE_NAME), 1);
    }
    
    public void actionPerformed(ActionEvent e) {
      Set annots = getSelectedAnnots(e);
      
      if (annots.size() == 1) {
        TextExtentRegion annot = (TextExtentRegion) annots.iterator().next();
        
        final SpatialMLEditor editor = new PlaceEditor(annot, annot); // the annotation is its own history!
        SpatialMLEditorDialog dialog = new SpatialMLEditorDialog(JOptionPane.getFrameForComponent(getEditorComponent().getComponent()),
            "Place Editor", false,
            editor,
            new ActionListener() { // OK Action
              public void actionPerformed(ActionEvent e) {
                // save the annotation updates from the form
                editor.updateAnnotation();
                // save the annotation for the next time around
                //lastCreatedPlace = annot;
              }
            },
            new ActionListener() { // Cancel Action
              public void actionPerformed(ActionEvent e) {
                //deleteAnnotation(annot, jdoc);
              }
            });

        dialog.show();
      }
    }
    
  }
  
  /**
   * @author jricher
   *
   */
  private class CreateSignalTagAction extends CreateTextAnnotAction {
    public CreateSignalTagAction(Task task) {
      super("Signal", task, task.getAnnotationType(SpatialMLTask.SIGNAL_NAME), SpatialMLTask.SIGNAL_NAME);
    }
    public void actionPerformed (ActionEvent e) {
      final TextExtentRegion annot = (TextExtentRegion)super.createAnnotation (e);
      final JawbDocument jdoc = GUIUtils.getJawbDocument(e);

      String id = IdTracker.getIdTracker((AWBDocument)jdoc.getAnnotationModel()).getId(annot);
      try {
        annot.setAttributeValue("id", id);
      } catch (UnmodifiableAttributeException e1) {
        log.log(Level.WARNING, "Caught Exception", e1);
      }
    }
  }
  
  private class CreateLinkTagAction extends CreateTextAnnotAction {
    public CreateLinkTagAction(Task task) {
      super ("Link", task, task.getAnnotationType(SpatialMLTask.LINK_EXTENT_NAME), SpatialMLTask.LINK_EXTENT_NAME);
    }
    
    public void actionPerformed(ActionEvent e) {
      
      // make a fake extent to get at what text was swiped
      final TextExtentRegion extent = (TextExtentRegion)super.createAnnotation (e);
      final JawbDocument jdoc = GUIUtils.getJawbDocument(e);

      
      
      // sift through the extent that was swiped and grab all annotations in there
      
      int start = extent.getTextExtentStart();
      int end = extent.getTextExtentEnd();
      
      Set subs = new LinkedHashSet();
      
      for (int i = start; i < end; i++) {
        subs.addAll(jdoc.getAnnotationsAt(i));
      }
      System.err.println("Annotations in range " + start + ", " + end + ":\n\t" + subs);
      
      // add in any currently selected annotations
      subs.addAll(jdoc.getSelectedAnnotationModel());
      
      if (subs.size() > 0) {
        // if we have sub-annotations, we can build a new, "real" annotation
        
        
        final AWBAnnotation annot = jdoc.createAnnotation(task.getAnnotationType(SpatialMLTask.LINK_NAME));
        String id = IdTracker.getIdTracker((AWBDocument)jdoc.getAnnotationModel()).getId(annot);
        
        try {
          annot.setAttributeValue("id", id);
        } catch (UnmodifiableAttributeException e1) {
          log.log(Level.WARNING, "Caught Exception", e1);
        }

        final LinkEditor editor = new LinkEditor(annot, subs, SpatialMLToolKit.this, jdoc);
        SpatialMLEditorDialog dialog = new SpatialMLEditorDialog(JOptionPane.getFrameForComponent(getEditorComponent().getComponent()), 
            "Link Editor", false,
            editor,
            new ActionListener() { // OK Action
              public void actionPerformed(ActionEvent e) {
                // save the annotation updates from the form
                editor.updateAnnotation();
                deleteAnnotation(extent, jdoc);
              }
            },
            new ActionListener() { // Cancel Action
              public void actionPerformed(ActionEvent e) {
                deleteAnnotation(annot, jdoc);
                deleteAnnotation(extent, jdoc);
              }
            });
        
        dialog.show();
        
      }
      
      
    }
  }
    
  
  private class EditLinkTagAction extends AnnotationAction {

    public EditLinkTagAction(Task task) {
      super("Edit Link", SpatialMLUtils.LINK_TYPE, 1);
    }
    
    public void actionPerformed(ActionEvent e) {
      Set annots = getSelectedAnnots(e);
      JawbDocument jdoc = GUIUtils.getJawbDocument(e);
      
      if (annots.size() == 1) {
        HasSubordinates annot = (HasSubordinates) annots.iterator().next();
        
        Set subs = new HashSet();
        
        // this is a bit of a hack. it grabs all annotations in between the two furthest ones or uses the current highlight
        
        
        TextExtentRegion src = (TextExtentRegion)SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("source"));
        TextExtentRegion target = (TextExtentRegion)SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("target"));
        
        int start = -1;
        int end = -1;
        
        if (src != null) {
          subs.add(src);
          if (target != null) {
            subs.add(target);
            start = Math.min(src.getTextExtentStart(), target.getTextExtentStart());
            end = Math.max(src.getTextExtentEnd(), target.getTextExtentEnd());
          } else {
            start = src.getTextExtentStart();
            end = src.getTextExtentEnd();
          }
        } else {
          if (target != null) {
            subs.add(target);
            start = target.getTextExtentStart();
            end = target.getTextExtentEnd();
          } else {
            // both null ... we're screwed?
          }
        }
        
        HasSubordinates srcRlink = (HasSubordinates)SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("sourceRlink"));
        HasSubordinates tarRlink = (HasSubordinates)SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("targetRlink"));
        
        // TODO: scan for subordinates here, too
        
        if (srcRlink != null) {
          subs.add(srcRlink);
          
        }
        
        if (tarRlink != null) {
          subs.add(tarRlink);
        }
        
        // check for a highlight in the document
        Caret c = mainTextPane.getCaret();
        if (c.getDot() != c.getMark()) {
          if (start != -1) {
            start = Math.min(start, c.getDot());
            start = Math.min(start, c.getMark());
          } else {
            start = Math.min(c.getDot(), c.getMark());
          }
          if (end != -1) {
            end = Math.max(end, c.getDot());
            end = Math.max(end, c.getMark());
          } else {
            end = Math.min(c.getDot(), c.getMark());
          }
        }
        
        // grab all sub-annotations that we've seen so far
        if (start != -1 && end != -1) {
          for (int i = start; i < end; i++) {
            subs.addAll(jdoc.getAnnotationsAt(i));
          }
        }

        // add in any currently selected annotations
        subs.addAll(jdoc.getSelectedAnnotationModel());
                
        final SpatialMLEditor editor = new LinkEditor(annot, subs, SpatialMLToolKit.this, jdoc);
        SpatialMLEditorDialog dialog = new SpatialMLEditorDialog(JOptionPane.getFrameForComponent(getEditorComponent().getComponent()), 
            "Link Editor", false,
            editor,
            new ActionListener() { // OK Action
              public void actionPerformed(ActionEvent e) {
                // save the annotation updates from the form
                editor.updateAnnotation();
                // save the annotation for the next time around
                //lastCreatedPlace = annot;
              }
            },
            new ActionListener() { // Cancel Action
              public void actionPerformed(ActionEvent e) {
                //deleteAnnotation(annot, jdoc);
              }
            });

        dialog.show();
      }
    }
    
  }

  
  private class CreateRLinkTagAction extends CreateTextAnnotAction {
    public CreateRLinkTagAction(Task task) {
      super ("RLink", task, task.getAnnotationType(SpatialMLTask.RLINK_EXTENT_NAME), SpatialMLTask.RLINK_EXTENT_NAME);
    }

    public void actionPerformed(ActionEvent e) {
      // make a fake extent to get at what text was swiped
      final TextExtentRegion extent = (TextExtentRegion)super.createAnnotation(e);
      final JawbDocument jdoc = GUIUtils.getJawbDocument(e);

      
      
      // sift through the extent that was swiped and grab all annotations in there
      
      int start = extent.getTextExtentStart();
      int end = extent.getTextExtentEnd();
      
      Set subs = new LinkedHashSet();
      
      for (int i = start; i < end; i++) {
        subs.addAll(jdoc.getAnnotationsAt(i));
      }
      System.err.println("Annotations in range " + start + ", " + end + ":\n\t" + subs);
      
      if (subs.size() > 0) {
        // if we have sub-annotations, we can build a new, "real" annotation
        
        
        final AWBAnnotation annot = jdoc.createAnnotation(task.getAnnotationType(SpatialMLTask.RLINK_NAME));
        
        //annot.getRegion().setAnnotationWithRole(extent, "path-extent");
        
        String id = IdTracker.getIdTracker((AWBDocument)jdoc.getAnnotationModel()).getId(annot);
        
        try {
          annot.setAttributeValue("id", id);
        } catch (UnmodifiableAttributeException e1) {
          log.log(Level.WARNING, "Caught Exception", e1);
        }

        final RLinkEditor editor = new RLinkEditor(annot, subs, SpatialMLToolKit.this, jdoc);
        SpatialMLEditorDialog dialog = new SpatialMLEditorDialog(JOptionPane.getFrameForComponent(getEditorComponent().getComponent()), 
            "Relative Link Editor", false,
            editor,
            new ActionListener() { // OK Action
              public void actionPerformed(ActionEvent e) {
                // save the annotation updates from the form
                editor.updateAnnotation();
                //jdoc.deleteAnnotation(extent);
                deleteAnnotation(extent, jdoc);
              }
            },
            new ActionListener() { // Cancel Action
              public void actionPerformed(ActionEvent e) {
                deleteAnnotation(annot, jdoc);
                //jdoc.deleteAnnotation(extent);
                deleteAnnotation(extent, jdoc);
              }
            });
        
        dialog.show();
        
      }
      
    }

  }
  
  private class EditRLinkTagAction extends AnnotationAction {

    public EditRLinkTagAction(Task task) {
      super("Edit RLink", SpatialMLUtils.RLINK_TYPE, 1);
    }
    
    public void actionPerformed(ActionEvent e) {
      Set annots = getSelectedAnnots(e);
      final JawbDocument jdoc = GUIUtils.getJawbDocument(e);
      
      if (annots.size() == 1) {
        HasSubordinates annot = (HasSubordinates) annots.iterator().next();
        
        Set subs = new HashSet();
        
        TextExtentRegion src = (TextExtentRegion)SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("source"));
        TextExtentRegion dest = (TextExtentRegion)SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole("target"));
        
        // this is a bit of a hack. it grabs all annotations in between the two furthest ones, and checks the current document highlight, too
        
        int start = -1;
        int end = -1;
        
        if (src != null) {
          subs.add(src);
          if (dest != null) {
            subs.add(dest);
            start = Math.min(src.getTextExtentStart(), dest.getTextExtentStart());
            end = Math.max(src.getTextExtentEnd(), dest.getTextExtentEnd());
          } else {
            start = src.getTextExtentStart();
            end = src.getTextExtentEnd();
          }
        } else {
          if (dest != null) {
            subs.add(dest);
            start = dest.getTextExtentStart();
            end = dest.getTextExtentEnd();
          } else {
            // both null ... we're (potentially) screwed!
          }
        }
               
        ATLASElementSet sigs = annot.getRegion().getSubordinateSet(SpatialMLUtils.SIGNAL_TYPE);

        Iterator it = sigs.iterator();
        while (it.hasNext()) {
          TextExtentRegion signal = (TextExtentRegion)SpatialMLUtils.convertAnnotation(it.next());
          
          // might as well add this to our set
          subs.add(signal);
          
          // stretch out our net if needed
          if (start != -1) {
            start = Math.min(start, signal.getTextExtentStart());
          } else {
            start = signal.getTextExtentStart();
          }
          if (end != -1) {
            end = Math.max(end, signal.getTextExtentEnd());
          } else {
            end = signal.getTextExtentEnd();
          }
        }
        
        // check for a highlight in the document
        Caret c = mainTextPane.getCaret();
        if (c.getDot() != c.getMark()) {
          if (start != -1) {
            start = Math.min(start, c.getDot());
            start = Math.min(start, c.getMark());
          } else {
            start = Math.min(c.getDot(), c.getMark());
          }
          if (end != -1) {
            end = Math.max(end, c.getDot());
            end = Math.max(end, c.getMark());
          } else {
            end = Math.min(c.getDot(), c.getMark());
          }
        }
        
        // grab all sub-annotations that we've seen so far
        if (start != -1 && end != -1) {
          for (int i = start; i < end; i++) {
            subs.addAll(jdoc.getAnnotationsAt(i));
          }
        }
        
        final SpatialMLEditor editor = new RLinkEditor(annot, subs, SpatialMLToolKit.this, jdoc);
        SpatialMLEditorDialog dialog = new SpatialMLEditorDialog(JOptionPane.getFrameForComponent(getEditorComponent().getComponent()), 
            "Relative Link Editor", false,
            editor,
            new ActionListener() { // OK Action
              public void actionPerformed(ActionEvent e) {
                // save the annotation updates from the form
                editor.updateAnnotation();
                // save the annotation for the next time around
                //lastCreatedPlace = annot;
              }
            },
            new ActionListener() { // Cancel Action
              public void actionPerformed(ActionEvent e) {
                //deleteAnnotation(annot, jdoc);
              }
            });

        dialog.show();
      }
    }
    
  }

  private class UpdateIDsAction extends AnnotationAction {
    
    public UpdateIDsAction(Task task, AnnotationType type) {
      super("Fill in Blank IDs", type);
    }
    
    public void actionPerformed(ActionEvent e) {
      Set annots = getSelectedAnnots(e);
      JawbDocument jdoc = GUIUtils.getJawbDocument(e);
      
      Iterator it = annots.iterator();
      while (it.hasNext()) {
        AWBAnnotation annot = SpatialMLUtils.convertAnnotation(it.next());
        
        String id = (String)annot.getAttributeValue("id");
        if (id == null || id.equals("")) {
          try {
            String newId = IdTracker.getIdTracker((AWBDocument)jdoc.getAnnotationModel()).getId(annot);
            annot.setAttributeValue("id", newId);
          } catch (UnmodifiableAttributeException e1) {
            // TODO Auto-generated catch block
            log.log(Level.WARNING, "Caught Exception", e1);
          }
        }
        
      }

    }
    
  }
  
  
  ////
  //// Exchangers
  ////
  
  
  static abstract class AbstractAnnotationExchanger implements ItemExchanger {
    protected final Set results = new LinkedHashSet();
    public final AnnotationType fromType;
    protected final boolean includeAll;
    protected AbstractAnnotationExchanger(AnnotationType fromType) {
      this(fromType, false);
    }
    protected AbstractAnnotationExchanger(AnnotationType fromType, boolean includeAll) {
      this.fromType = fromType;
      this.includeAll = includeAll;
    }
  }
  
  static class Parent2Subordinates extends AbstractAnnotationExchanger {
    protected final Object[] subordinateTypes;
    Parent2Subordinates(AnnotationType parent, Object subordinates) {
      this(parent, new Object[] { subordinates });
    }
    Parent2Subordinates(AnnotationType parent, Object[] subordinates) {
      super(parent);
      this.subordinateTypes = subordinates;
    }
    public Set exchange(Object item) {
      results.clear();
      AWBAnnotation annot = SpatialMLUtils.convertAnnotation(item);
      String type = annot.getAnnotationType().getName();
      if (type.equals(fromType.getName())) {
        SubordinateSetsAnnotation parent = (SubordinateSetsAnnotation) annot;
        for (int i = 0; i < subordinateTypes.length; i++) {
          if (subordinateTypes[i] instanceof AnnotationType) {
            AWBAnnotation[] components =
              parent.getSubordinates ((AnnotationType) subordinateTypes[i]);
            results.addAll(Arrays.asList(components));
          }
          else if (subordinateTypes[i] instanceof String) {
            results.add(parent.getRegion().getSubordinateWithRole((String) subordinateTypes[i]));
          }
        }
      }
      return Collections.unmodifiableSet(results);
    }
  }
  
  static class Subordinate2Ancestors extends AbstractAnnotationExchanger {
    protected final Object[] referents;
    Subordinate2Ancestors(AnnotationType fromType, boolean includeAll) {
      this(fromType, (Object[]) null, includeAll);
    }
    Subordinate2Ancestors(AnnotationType fromType, Object referent, boolean includeAll) {
      this(fromType, new Object[] { referent }, includeAll);
    }
    Subordinate2Ancestors(AnnotationType fromType, Object[] referents, boolean includeAll) {
      super(fromType, includeAll);
      this.referents = referents;
    }
    public Set exchange(Object item) {
      results.clear();
      AWBAnnotation annot = SpatialMLUtils.convertAnnotation(item);
      String type = annot.getAnnotationType().getName();
      if (type.equals(fromType.getName())) {
        exchange(annot);
      }
      
      log.fine("Exchange set (" + fromType.getName() + "):" + results);
      
      return Collections.unmodifiableSet(results); 
    }
    
    private void exchange(AWBAnnotation annot) {
      HasSubordinates[] components = null;
      if (referents == null) {
        components = SpatialMLTask.getReferentParents(annot);
        if (includeAll) {
          if (components != null) {
            results.addAll(Arrays.asList(components));
          }
        }
        else if (components == null || components.length == 0) {
          results.add(annot);
        }
      }
      else {
        for (int i = 0; i < referents.length; i++) {
          if (referents[i] instanceof AnnotationType) {
            components = SpatialMLTask.getReferentParents(annot, (AnnotationType) referents[i]);
            if (includeAll) {
              if (components != null) {
                results.addAll(Arrays.asList(components));
              }
            }
            else if (components == null || components.length == 0) {
              results.add(annot);
            }
          }
        }
//      else if (referents[i] instanceof String) {
//      results.add(parent.getRegion().getSubordinateWithRole((String) referents[i]));
//      }
      }
      if (components != null) {
        for (int i = 0; i < components.length; i++) {
          exchange(components[i]);
        }
      }
    }
  }
  
  static class Ancestor2Subordinates extends AbstractAnnotationExchanger {
    protected final Object[][] subordinateTypes;

    Ancestor2Subordinates(AnnotationType parent, Object[] subordinates,
        boolean includeAll) {
      this(parent, new Object[][] {
        subordinates
      }, includeAll);
    }

    public Ancestor2Subordinates(AnnotationType annotationType, boolean b) {
      this(annotationType, (Object[][]) null, b);
    }

    Ancestor2Subordinates(AnnotationType parent, Object[][] subordinates,
        boolean includeAll) {
      super(parent, includeAll);
      this.subordinateTypes = subordinates;
    }

    public Set exchange(Object item) {
      results.clear();
      AWBAnnotation annot = SpatialMLUtils.convertAnnotation(item);
      String type = annot.getAnnotationType().getName();
      if (type.equals(fromType.getName())) {
        exchange(annot, 0);
      }
      return Collections.unmodifiableSet(results);
    }

    private void exchange(AWBAnnotation annot, int i) {
      SubordinateSetsAnnotation parent = (SubordinateSetsAnnotation) annot;
      if (subordinateTypes == null || subordinateTypes.length > i) {
        Object[] current = subordinateTypes == null ? null : subordinateTypes[i];
        AnnotationType type = annot.getAnnotationType();
          for (int j = 0; subordinateTypes == null || j < current.length; j++) {
            Object o = current == null ? null : current[j];
            if (o == null) {
              RegionType typeForRegion = type.getTypeForRegion();
              for (Iterator definedRolesForSubordinates = typeForRegion.getDefinedRolesForSubordinates(); definedRolesForSubordinates.hasNext();) {
                String role = (String) definedRolesForSubordinates.next();
                exchangeForRole(annot, role, i);
              }
              for (Iterator containedTypesInSubordinateSets = typeForRegion.getContainedTypesInSubordinateSets(); containedTypesInSubordinateSets.hasNext();) {
                AnnotationType subordinateType = (AnnotationType) containedTypesInSubordinateSets.next();
                exchangeForType(annot, subordinateType, i);
              }
            }
            else if (o instanceof String) {
              String role = (String) o;
              exchangeForRole(annot, role, i);
            }
            else if (o instanceof AnnotationType) {
              AnnotationType subordinateType = (AnnotationType) o;
              exchangeForType(annot, subordinateType, i);
            }
            if (subordinateTypes == null) {
              break;
            }
          }
        } else {

        }
      }

    private void exchangeForType(AWBAnnotation annot, AnnotationType subordinateType, int i) {
      AWBAnnotation[] subordinateSet = ((HasSubordinates)annot).getSubordinates(subordinateType);
      for (int j = 0; j < subordinateSet.length; j++) {
        AWBAnnotation subordinateWithRole = subordinateSet[j];
        boolean hasSubordinates = subordinateWithRole instanceof HasSubordinates;
        if (includeAll || ! hasSubordinates) {
          results.add(subordinateWithRole);
        }
        if (hasSubordinates) {
          exchange(subordinateWithRole, i + 1);
        }
      }
    }

    private void exchangeForRole(AWBAnnotation annot, String role, int i) {
      AWBAnnotation subordinateWithRole = SpatialMLUtils.convertAnnotation(annot.getRegion().getSubordinateWithRole(role));
      boolean hasSubordinates = subordinateWithRole instanceof HasSubordinates;
      if (includeAll || ! hasSubordinates) {
        results.add(subordinateWithRole);
      }
      if (hasSubordinates) {
        exchange(subordinateWithRole, i + 1);
      }
    }
  }

  // text panel override to let us get the caret
  private class MyMainTextPane extends MainTextPane {
    
    public MyMainTextPane(TaskToolKit kit) {
      super(kit);
    }

    public Caret getCaret() {
      return getTextPane().getCaret();
    }
  }
  
}
