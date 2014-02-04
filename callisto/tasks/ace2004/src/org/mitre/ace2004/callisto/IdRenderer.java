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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.table.*;

import org.mitre.jawb.atlas.*;
import org.mitre.jawb.gui.*;

import gov.nist.atlas.*;
import gov.nist.atlas.ref.AnnotationRef;
import gov.nist.atlas.type.*;

/**
 * The editor/renderer button that brings up the dialog.  We extend
 * DefaultCellEditor for convenience, even though it means we have to
 * create a dummy check box.  Another approach would be to copy the
 * implementation of TableCellEditor methods from the source code for
 * DefaultCellEditor.
 */
public class IdRenderer extends DefaultTableCellRenderer {

  private static final int DEBUG = 0;
  
  private String idAttrName; 
  
  public IdRenderer (String idAttrName) {
    super();
    this.idAttrName = idAttrName;
  }

  public IdRenderer () {
    super();
    this.idAttrName = "ace_id";
  }
  
  public Component getTableCellRendererComponent (JTable table,
                                                  Object value,
                                                  boolean isSelected,
                                                  boolean hasFocus,
                                                  int row, int column) {
    if (DEBUG > 0)
      System.err.println ("IdRend.getTCRC: v='"+value+"'");
    
    String id = null;
    if (value instanceof AnnotationRef) {
      value = ((AnnotationRef) value).getElement();
    }
    if (value instanceof AWBAnnotation && table instanceof AnnotationTable) {
      JawbDocument doc = ((AnnotationTable) table).getJawbDocument();
      id = getShortId((AWBAnnotation) value, 
                      (AWBDocument) doc.getAnnotationModel(),
                      idAttrName);
    }
    return super.getTableCellRendererComponent (table, id,
                                                isSelected, hasFocus,
                                                row, column);
  }

  /* for backward compatibility... */
  static String getShortId(AWBAnnotation annot, AWBDocument doc) {
    return getShortId (annot, doc, "ace_id");
  }

  static String getShortId(AWBAnnotation annot, AWBDocument doc, String attr) {
    String id = (String) annot.getAttributeValue(attr);
    if (DEBUG > 0)
      System.err.println("IdRenderer.getShortId: attrName = " + attr +
                         " long id = " + id);
    if (id != null) {
      // What a hack!
      String docid = IdTracker.getDocId(doc);
      // More Hack!
      int suffix = 0;
      if (docid.endsWith(".sgm")) {
        suffix = 4;
        docid = docid.substring(0, docid.length()-suffix);
      }
      
      if (id.startsWith(docid)) {
        int length = docid.length();
        if (id.startsWith(".sgm", length))
          length += 4;
        if (id.length() > length && id.charAt(length) == '-') {
          length++;
        }
        id = id.substring(length);
      }
    }
    if (DEBUG > 0)
      System.err.println("\tshort id = " + id);
    return id;
  }
}
