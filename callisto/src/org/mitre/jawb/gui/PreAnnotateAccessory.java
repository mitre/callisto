
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

import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;


import java.util.Iterator;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.*;
import org.mitre.jawb.tasks.preannotate.*;

/**
 * Sub widget for other accessories.
 */
public class PreAnnotateAccessory extends JPanel {

    static final String NO_SYSTEM_NAME = "<none>";
    static final String NO_SYSTEM_DESC = "";
    
    private JComboBox lpCombo;
    private JTextArea descTextArea;
  
    public PreAnnotateAccessory() {
	
	super (new BorderLayout (5,5));
	this.setBorder(BorderFactory.createCompoundBorder
		       (BorderFactory.createTitledBorder("TALLAL System:"),
			BorderFactory.createEmptyBorder(5,5,5,5)));
	
	lpCombo = new JComboBox();
	lpCombo.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	
	lpCombo.addItemListener(new ItemListener() {
		public void itemStateChanged(ItemEvent evt) {
		    if (evt.getStateChange() == ItemEvent.SELECTED) {
			
			descTextArea.setText (((MySystemWrapper)evt.getItem()).getDescription());
			descTextArea.setCaretPosition (0);
			
		    }
		}
	    });

	this.add(lpCombo, BorderLayout.CENTER);

	descTextArea = new JTextArea(5,20);
	descTextArea.setLineWrap(true);
	descTextArea.setWrapStyleWord(true);
	descTextArea.setBorder (BorderFactory.createEmptyBorder (2,2,2,2));
	descTextArea.setEditable (false);
	JScrollPane descScrollPane = new JScrollPane(descTextArea);

	this.add(descScrollPane, BorderLayout.SOUTH);	

	setTask(null);
	    
    }

    public void setTask(Task task) {

	lpCombo.removeAllItems();

	if (task == null) {

	    lpCombo.addItem(new MySystemWrapper(null));
	    lpCombo.setSelectedIndex(0);

	} else {

	    lpCombo.addItem(new MySystemWrapper(null));
	    lpCombo.setSelectedIndex(0);

            Iterator systems =
              TallalController.getController().getSystems().iterator();
            
            while (systems.hasNext()) {
		
		TallalSystem sys = (TallalSystem)systems.next();
		if (sys.getTask() != null &&
                    sys.getTask().getTitle() != null &&
                    task.getTitle() != null &&
                    sys.getTask().getTitle().equals(task.getTitle())) {
		    lpCombo.addItem(new MySystemWrapper(sys));
		}

	    }
    
	}
	
    }
	
    public TallalSystem getSelectedSystem() {
	
	return ((MySystemWrapper)(lpCombo.getSelectedItem())).getSystem();
		
    }
    
    /** Wrapper to show a TallalSystem's title in combobox */
    private static class MySystemWrapper {
	TallalSystem sys;
	MySystemWrapper (TallalSystem s) { sys = s; }
	TallalSystem getSystem () { return sys; }
	String getDescription () {
	    return (sys==null) ? NO_SYSTEM_DESC : sys.getDesc();
	}
	public String toString () {
	    return (sys==null) ? NO_SYSTEM_NAME : sys.getName();
	}
    }

}


