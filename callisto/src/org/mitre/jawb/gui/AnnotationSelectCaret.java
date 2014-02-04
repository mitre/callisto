
/* ----------------------------------------------------------------------
 * 
 * Copyright (c) 2002-2009 The MITRE Corporation
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
 * 
 * ----------------------------------------------------------------------
 * 
 * NOTICE
 * 
 * This software was produced for the U. S. Government
 * under Contract No. W15P7T-09-C-F600, and is
 * subject to the Rights in Noncommercial Computer Software
 * and Noncommercial Computer Software Documentation
 * Clause 252.227-7014 (JUN 1995).
 * 
 * (c) 2009 The MITRE Corporation. All Rights Reserved.
 * 
 * ----------------------------------------------------------------------
 *
 */
/*
 * Created on Jun 18, 2007 at 12:18:26 PM by Galen B. Williamson
 */
package org.mitre.jawb.gui;

import java.util.*;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.TextExtentRegion;
import org.mitre.jawb.swing.AutoSelectCaret;

/**
 * @author <a href="mailto:gwilliam@mitre.org">Galen B. Williamson</a>
 *
 */
public class AnnotationSelectCaret extends AutoSelectCaret {

  Set selectionTypes;

  /**
   * 
   */
  public AnnotationSelectCaret(Collection selectionTypes) {
    super();
    this.selectionTypes = new LinkedHashSet(selectionTypes);
  }

  /**
   * @param mode
   */
  public AnnotationSelectCaret(Mode mode, Collection selectionTypes) {
    super(mode);
    this.selectionTypes = new LinkedHashSet(selectionTypes);
  }

  protected int getWordEnd(JTextComponent target, int offset) throws BadLocationException {
    TextExtentRegion theExtent = getTextExtent(target, offset);
    if (theExtent != null) {
      return theExtent.getTextExtentStart();
    }
    return super.getWordEnd(target, offset);
  }
  
  protected int getWordStart(JTextComponent target, int offset) throws BadLocationException {
    TextExtentRegion theExtent = getTextExtent(target, offset);
    if (theExtent != null) {
      return theExtent.getTextExtentEnd();
    }
    return super.getWordStart(target, offset);
  }

  protected TextExtentRegion getTextExtent(JTextComponent target, int offset) {
    TextExtentRegion theExtent = null;
    JawbDocument jawbDoc = GUIUtils.getJawbDocument(target);
    List annots = jawbDoc.getAnnotationsAt(offset);
    for (Iterator it = annots.iterator(); theExtent == null && it.hasNext();) {
      AWBAnnotation annot = (AWBAnnotation) it.next();
      if (selectionTypes.contains(annot.getAnnotationType())) {
        if (annot instanceof TextExtentRegion) {
          TextExtentRegion extent = (TextExtentRegion) annot;
          if (offset >= extent.getTextExtentStart() && offset < extent.getTextExtentEnd()) {
            theExtent = extent;
          }
        }
      }
    }
    return theExtent;
  }

  
}
