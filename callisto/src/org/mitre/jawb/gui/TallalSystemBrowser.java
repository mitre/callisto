
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
package org.mitre.jawb.gui;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.tasks.*;
import org.mitre.jawb.tasks.preannotate.*;

public class TallalSystemBrowser extends JDialog {
    
    private JPanel panel;

    private JList sysList;
        
    private JTextField nameField;
    private JTextArea descTextArea;
    private JTextField taskField;
    private JTextField lpField;
    private JTextField pathField;
    private TallalListModel listModel;

    public TallalSystemBrowser()  {

	new TallalSystemBrowser(new JFrame());

    }

  public TallalSystemBrowser(JFrame parent) {

	super(parent, "TALLAL Systems", true);
	
	initInterface();
	setContentPane(panel);
	pack();
	
    }

    private void initInterface() {

	Font labelFont = (Font)UIManager.get("TextField.font");
	
	panel = new JPanel();
	panel.setLayout(new BorderLayout());
	
	JPanel okPanel = new JPanel(new FlowLayout());
	okPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	JButton okButton = new JButton(new AbstractAction("OK") {
		public void actionPerformed(ActionEvent e) {
		    setVisible(false);
		}
	    });
	okPanel.add(okButton);
	
	JPanel contentPanel = new JPanel(new BorderLayout());
	contentPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	
	JPanel listPanel = new JPanel(new BorderLayout());
	listPanel.setBorder(BorderFactory.createTitledBorder("Systems:"));
	 
	listModel = new TallalListModel();
	sysList = new JList(listModel);

	Vector sysVec = TallalController.getController().getSystems();
	for (int i = 0; i < sysVec.size(); i++) {
	    
	    listModel.addSystem((TallalSystem)sysVec.get(i));

	}

	sysList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	sysList.setLayoutOrientation(JList.VERTICAL);
	sysList.setVisibleRowCount(-1);
	
	sysList.addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
			
		    TallalSystemWrapper wrap = (TallalSystemWrapper)sysList.getSelectedValue();
		    
		    if (wrap == null) {

			nameField.setText("");
			descTextArea.setText("");
			taskField.setText("");
			lpField.setText("");
			pathField.setText("");

		    } else {

			TallalSystem sys = wrap.getSystem();
		    
			nameField.setText(sys.getName());
			descTextArea.setText(sys.getDesc());
			taskField.setText(sys.getTask().getName());
			try { lpField.setText(sys.getLP().getDesc()); }
			catch(java.rmi.RemoteException e2) {
			  lpField.setText("** error connection to remote server:" + e2.getMessage());
			}
			pathField.setText(sys.getDataOrURL());
		    }

		}	
	    });

	JScrollPane listScroller = new JScrollPane(sysList);
	listScroller.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	
	JPanel listButtonPanel = new JPanel(new FlowLayout());
	listButtonPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	JButton createButton = new JButton(new AbstractAction("Create") {
		public void actionPerformed(ActionEvent e) {
		   
		    try {
		      TallalSystemCreator tsc = new TallalSystemCreator(TallalSystemBrowser.this);
		    
		      tsc.setVisible(true);

		      if (tsc.getSystem() != null) {
			TallalController.getController().addSystem(tsc.getSystem());
			listModel.addSystem(tsc.getSystem());
		      }
		    }
		    catch(java.rmi.RemoteException e2) {
		      GUIUtils.showError("Error connecting to remote server: " + e2.getMessage());
		    }

		}
	    });
	JButton modifyButton = new JButton(new AbstractAction("Modify") {
		public void actionPerformed(ActionEvent e) {

		    TallalSystemWrapper wrap = (TallalSystemWrapper)sysList.getSelectedValue();
		    int index = sysList.getSelectedIndex();
		    
		    if (wrap != null) {
			try {
			  TallalSystemCreator tsc = new TallalSystemCreator(TallalSystemBrowser.this, wrap.getSystem());
			  
			  tsc.setVisible(true);
			  
			  listModel.updateSystem(index);
			  
			  TallalSystem sys = wrap.getSystem();
			  
			  nameField.setText(sys.getName());
			  descTextArea.setText(sys.getDesc());
			  taskField.setText(sys.getTask().getName());
			  try { lpField.setText(sys.getLP().getDesc()); }
			  catch(java.rmi.RemoteException e2) {
			    lpField.setText("** error talking to remote server: " + e2.getMessage());
			  }
			  pathField.setText(sys.getDataOrURL());
			}
			catch(java.rmi.RemoteException e2) {
			  GUIUtils.showError("error talking to remote server: " + e2.getMessage());
			}

			
			
		    }

		}
	    });

	JButton deleteButton = new JButton(new AbstractAction("Delete") {
		public void actionPerformed(ActionEvent e) {

		    TallalSystemWrapper wrap = (TallalSystemWrapper)sysList.getSelectedValue();
		    int index = sysList.getSelectedIndex();

		    if (wrap != null) {
			
			TallalSystem sys = wrap.getSystem();
			
			String message = new String("Are you sure you want to delete\nthe system \"").concat(sys.getName()).concat("\" ?");
			
			int result = JOptionPane.showConfirmDialog(TallalSystemBrowser.this, message, "Delete system...", JOptionPane.OK_CANCEL_OPTION);
			
			if (result == JOptionPane.OK_OPTION) {

			    TallalController tsc = TallalController.getController();
			    tsc.removeSystem(sys);
			    listModel.removeSystem(index);				
				
			}
		    }
		}
	    });
	
	listButtonPanel.add(createButton);
	listButtonPanel.add(modifyButton);
	listButtonPanel.add(deleteButton);
	
	listPanel.add(listScroller, BorderLayout.CENTER);
	listPanel.add(listButtonPanel, BorderLayout.SOUTH);
	
	JPanel sysPanel = new JPanel();
	sysPanel.setLayout(new BoxLayout(sysPanel, BoxLayout.Y_AXIS));
	sysPanel.setBorder(BorderFactory.createTitledBorder("System Info:"));
	
	JPanel namePanel = new JPanel(new BorderLayout());
	namePanel.setBorder(BorderFactory.createEmptyBorder(0,2,2,2));
	JLabel nameLabel = new JLabel("Name:");
	nameField = new JTextField();
	nameField.setEditable(false);
	nameField.setBackground(Color.LIGHT_GRAY);
	nameLabel.setFont(labelFont);
	namePanel.add(nameLabel, BorderLayout.NORTH);
	namePanel.add(nameField, BorderLayout.CENTER);
	
	JPanel descPanel = new JPanel(new BorderLayout());
	descPanel.setBorder(BorderFactory.createEmptyBorder(0,2,2,2));
	JLabel descLabel = new JLabel("Description:");
	descLabel.setFont(labelFont);
	descTextArea = new JTextArea(4,20);
	descTextArea.setLineWrap(true);
	descTextArea.setWrapStyleWord(true);
	descTextArea.setEditable(false);
	descTextArea.setBackground(Color.LIGHT_GRAY);
	JScrollPane descScrollPane = new JScrollPane(descTextArea);
	descPanel.add(descLabel, BorderLayout.NORTH);
	descPanel.add(descScrollPane, BorderLayout.CENTER);

	JPanel taskPanel = new JPanel(new BorderLayout());
	taskPanel.setBorder(BorderFactory.createEmptyBorder(0,2,2,2));
	JLabel taskLabel = new JLabel("Task:");
	taskLabel.setFont(labelFont);
	taskField = new JTextField();
	taskField.setEditable(false);
	taskField.setBackground(Color.LIGHT_GRAY);
	taskPanel.add(taskLabel, BorderLayout.NORTH);
	taskPanel.add(taskField, BorderLayout.CENTER);
	
	JPanel lpPanel = new JPanel(new BorderLayout());
	lpPanel.setBorder(BorderFactory.createEmptyBorder(0,2,2,2));
	JLabel lpLabel = new JLabel("LP:");
	lpLabel.setFont(labelFont);
	lpField = new JTextField();
	lpField.setEditable(false);
	lpField.setBackground(Color.LIGHT_GRAY);
	lpPanel.add(lpLabel, BorderLayout.NORTH);
	lpPanel.add(lpField, BorderLayout.CENTER);

	JPanel pathPanel = new JPanel(new BorderLayout());
	pathPanel.setBorder(BorderFactory.createEmptyBorder(0,2,2,2));
	JLabel pathLabel = new JLabel("Training data path:");
	pathLabel.setFont(labelFont);
	pathField = new JTextField();
	pathField.setEditable(false);
	pathField.setBackground(Color.LIGHT_GRAY);
	pathPanel.add(pathLabel, BorderLayout.NORTH);
	pathPanel.add(pathField, BorderLayout.CENTER);

	
	sysPanel.add(namePanel);
	sysPanel.add(descPanel);
	sysPanel.add(taskPanel);
	sysPanel.add(lpPanel);
	sysPanel.add(pathPanel);

	contentPanel.add(listPanel, BorderLayout.EAST);
	contentPanel.add(sysPanel, BorderLayout.CENTER);

	panel.add(contentPanel, BorderLayout.CENTER);
	panel.add(okPanel, BorderLayout.SOUTH);
	
    }

    private class TallalSystemWrapper {
	TallalSystem sys;
	TallalSystemWrapper(TallalSystem s) { sys = s; }
	TallalSystem getSystem() { return sys; }
	public String toString() { return sys.getName(); }
    }

    private class TallalListModel extends AbstractListModel {
	
	Vector systems;
	
	public TallalListModel() {

	    systems = new Vector();

	}
	
	public int getSize() {

	    return systems.size();

	}	
	
	public Object getElementAt(int index) {

	    return systems.get(index);

	}
	
	public void removeSystem(int index) {

	    systems.removeElementAt(index);
	    fireIntervalRemoved(this, index, index);
	    
	}

	public void addSystem(TallalSystem sys) {

	    TallalSystemWrapper sysWrapper = new TallalSystemWrapper(sys);
	    systems.add(sysWrapper);
	    fireIntervalAdded(this, systems.size(), systems.size());

	}

	public TallalSystem getSystem(int index) {

	    return ((TallalSystemWrapper)systems.get(index)).getSystem();

	}

	public void updateSystem(int index) {

	    fireContentsChanged(this, index, index);

	    
	}

    }

       
}
