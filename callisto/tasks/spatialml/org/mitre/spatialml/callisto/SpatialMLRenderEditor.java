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

import javax.swing.table.DefaultTableCellRenderer;

/**
 * The editor/renderer button that brings up the dialog.  We extend
 * DefaultCellEditor for convenience, even though it means we have to
 * create a dummy check box.  Another approach would be to copy the
 * implementation of TableCellEditor methods from the source code for
 * DefaultCellEditor.
 */


public class SpatialMLRenderEditor extends DefaultTableCellRenderer {

  
  
}

//public class SpatialMLRenderEditor extends DefaultCellEditor
//  implements TableCellRenderer, ListCellRenderer {
//
//  private static final int DEBUG = 0;
//  
//  TextExtentRegion annot;
//  SpatialMLEditor.Timex2Data data;
//
//  SpatialMLEditor editor = null;
//  DefaultTableCellRenderer tableRenderComponent = null;
//  DefaultListCellRenderer listRenderComponent = null;
//  JButton editorComponent = null;
//  ActionListener okShowListener = null;
//  ActionListener cancelListener = null;
//  JDialog dialog = null;
//    
//  public SpatialMLRenderEditor () {
//    // Unfortunately, the constructor expects a check box, 
//    //combo box, or text field.
//    super(new JCheckBox ());
//    setClickCountToStart (1); //This is usually 1 or 2.
//
//    tableRenderComponent = new DefaultTableCellRenderer ();
//    listRenderComponent = new DefaultListCellRenderer ();
//    editorComponent = new JButton ();
//
//    cancelListener = new ActionListener() {
//	public void actionPerformed (ActionEvent e) {
//	  fireEditingStopped();
//	}
//      };
//	  
//    
//    okShowListener = new ActionListener() {
//        public void actionPerformed(ActionEvent e) {
//            
//          if (e.getSource () == editorComponent) {
//            //Here's the code that brings up the dialog.
//
//            //Without the following line, the dialog comes up
//            //in the middle of the screen. but it takes forever.
//            //dialog.setLocationRelativeTo (editorComponent);
//            if (dialog == null) {
//              dialog = SpatialMLEditor.createDialog ((Component)e.getSource (),
//                                                   "Edit Timex2",
//                                                   true, editor,
//                                                   okShowListener, 
//						   cancelListener);
//            }
//            data.text = annot.getTextExtent ();
//            data.val = (String) annot.getAttributeValue ("val");
//            data.mod = (String) annot.getAttributeValue ("mod");
//            data.set = (String) annot.getAttributeValue ("set");
//	    //data.granularity = (String)annot.getAttributeValue ("granularity");
//            //data.periodicity = (String)annot.getAttributeValue ("periodicity");
//            data.nonSpecific = (String)annot.getAttributeValue ("non-specific");
//            data.anchorVal = (String)annot.getAttributeValue ("anchor-val");
//            data.anchorDir = (String)annot.getAttributeValue ("anchor-dir");
//            data.comment = (String)annot.getAttributeValue ("comment");
//
//            if (DEBUG > 0)
//              System.err.println ("Timex2RE.launchEditor:\n  "+data);
//            
//            editor.setTimex2 (data);
//            editor.updateUI();
//
//            dialog.setTitle ("Enter Tag Values for: "+data.text);
//	    
//            //dialog.show ();
//
//	    // queue showing the dialog for later
//	     SwingUtilities.invokeLater(new Runnable(){
//		public void run() {
//		  dialog.show();
//		  }});
//
//
//
//          } else {
//            // here's the code that responds to an ok press
//            // put the edit results back into the annotation
//            try {
//              data = editor.getTimex2(data);
//
//              if (DEBUG > 0)
//                System.err.println ("Timex2RE.editActionOK:\n  "+data);
//              // TODO: check for null values and deal with them?
//              annot.setAttributeValue ("val",data.val);
//              annot.setAttributeValue ("mod",data.mod);
//              annot.setAttributeValue ("set",data.set);
//              //annot.setAttributeValue ("granularity",data.granularity);
//              //annot.setAttributeValue ("periodicity",data.periodicity);
//              annot.setAttributeValue ("non-specific",data.nonSpecific);
//              annot.setAttributeValue ("anchor-val",data.anchorVal);
//              annot.setAttributeValue ("anchor-dir",data.anchorDir);
//              annot.setAttributeValue ("comment",data.comment);
//            } catch (Exception x) {
//              System.err.println ("Timex2RE: error storing changes");
//              x.printStackTrace ();
//            }
//            fireEditingStopped ();
//          }
//        }};
//    data = new SpatialMLEditor.Timex2Data ();
//    editor = new SpatialMLEditor ();
//    editorComponent.addActionListener (okShowListener);
//  }
//
//  public Object getCellEditorValue () {
//    return annot;
//  }
//
//  /** Convience method for the renderers */
//  private String getRenderValue (Object value) {
//    TextExtentRegion annot;
//    String v = null;
//    if (value != null) {
//      annot = (TextExtentRegion) value;
//      v = (String)annot.getAttributeValue ("val");
//      if (v == null || v.length()==0)
//        v = annot.getTextExtent ();
//    }
//    return v;
//  }
//  
//  public Component getListCellRendererComponent (JList list,
//                                                 Object value,
//                                                 int index,
//                                                 boolean isSelected,
//                                                 boolean hasFocus) {
//    if (DEBUG > 0)
//      System.err.println ("Timex2RE.getLCRC: v='"+value+"'");
//    return listRenderComponent.getListCellRendererComponent
//      (list, getRenderValue (value), index, isSelected, hasFocus);
//  }
//  
//  public Component getTableCellRendererComponent (JTable table,
//                                                  Object value,
//                                                  boolean isSelected,
//                                                  boolean hasFocus,
//                                                  int row, int column) {
//    // TODO: could we do a tooltip for renderercomponent to show the details?
//    // I doubt because of the 'rubber stamp' approach, but should confirm.
//    // also: don't set the class' annot: it'll screw up the editor
//    if (DEBUG > 0)
//      System.err.println ("Timex2RE.setTCRC: v='"+value+"'");
//    return tableRenderComponent.getTableCellRendererComponent
//      (table, getRenderValue (value), isSelected, hasFocus, row, column);
//  }
//  public Component getTableCellEditorComponent (JTable table, 
//                                                Object value,
//                                                boolean isSelected,
//                                                int row, int column) {
//    // you can't create one from here, it shouldn't be 'editable' but that's
//    // so much effort in the model...
//    if (value == null) {
//      if (DEBUG > 0)
//        System.err.println ("Timex2RE.getTCEC:....");
//      return getTableCellRendererComponent
//        (table, value, isSelected, true, row, column);
//    }
//    if (DEBUG > 0)
//      System.err.println ("Timex2RE.getTCEC: v='"+getRenderValue(value)+"'");
//
//    annot = (TextExtentRegion) value;
//    editorComponent.setText (getRenderValue (value));
//    return editorComponent;
//  }
//
//  /** Set Font of text field displaying text of annotation */
//  public void setTimex2Font(Font f) {
//    editor.setTimex2Font(f);
//  }
//
//  /** Retrieve Font of text field displaying text of annotation */
//  public Font getTimex2Font() {
//    return editor.getTimex2Font();
//  }
//
//  public SpatialMLEditor.Timex2Data getSavedDate() {
//    return editor.setValToStoredDate();
//  }
//}
