
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

package org.mitre.jawb.gui;

import java.util.EventObject;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.Icon;

import org.mitre.jawb.gui.*;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;

/**
 * Implements an <code>ActionListener</code> to handle events fired by
 * <code>JMenuItem</code>s and <code>JButton</code>s. Includes two methods
 * specific to Jawb which gets the <code>JawbDocument</code> and
 * <code>JawbFrame</code> the event was fired from. Also has a name property
 * that is used to retrieve the action from the static method
 * <code>Jawb.getAction()</code>. <p>
 *
 * JawbActions will be instantiated once, and that single action used
 * throughout the code base. Do not attempt to store information regarding a
 * particular document or frame, as these are not guaranteed to be consistent
 * across invocations.
 */
public abstract class JawbAction extends AbstractAction {
  /**
   * This is the Key for the Color associated with this action. If the actions
   * HIGHLIGHT_KEY is equal to a hightlight key (from {@link
   * Task#getHighlightKeys}) this value will be updated as the preference for
   * the hightlight key is changed.
   * @see TaskToolKit#getActions
   */
  public static final String HIGHLIGHT_COLOR = "Highlight Color";
  // see Jawb#loadTask (private so javadoc won't work

  /**
   * This is the Key that specifies the name of the actions highlight. If an
   * actions HIGHLIGHT_KEY is equal to a tasks hightlight key (from {@link
   * Task#getHighlightKeys}) the HIGHLIGHT_COLOR value will be updated as the
   * preference for the hightlight key is changed.
   */
  public static final String HIGHLIGHT_KEY = "Highlight Key";


  /**
   * Defines a new <code>Action</code> with default description and icon.
   */
  public JawbAction () {
    super ();
  }
    
  /**
   * Defines a new <code>Action</code> with specified name.
   * @param name Internal name of action
   */
  public JawbAction (String name) {
    super (name);
    if (name == null)
      throw new IllegalArgumentException ();
  }
  
  /**
   * Defines a new <code>Action</code> with specified name.
   * @param name Internal name of action
   */
  public JawbAction (String name, Icon icon) {
    super (name, icon);
    if (name == null)
      throw new IllegalArgumentException ();
  }
  
  /**
   * This methods returns the selected text area in the window
   * which fired the event.
   * @param evt The source event
   */
  public static JawbDocument getJawbDocument (EventObject evt) {
    return GUIUtils.getJawbDocument (evt);
  }

  /**
   * Returns the window which fired the event.
   * @param evt The source event
   */
  public static JawbFrame getJawbFrame (EventObject evt) {
    return GUIUtils.getJawbFrame (evt);
  }
}

// End of MenuAction.java
