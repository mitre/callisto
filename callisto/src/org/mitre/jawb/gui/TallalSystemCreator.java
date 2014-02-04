
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
import java.io.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.tasks.*;
import org.mitre.jawb.tasks.preannotate.*;

import java.rmi.RemoteException;


public class TallalSystemCreator extends JDialog {
    // Interface components
    private JPanel panel;

    private JLabel nameLabel;
    private JTextField nameField;
    
    private JLabel descLabel;
    private JTextArea descTextArea;
    
    /*private JLabel tagLabel;
    private JTable tagTable;
    private AbstractButton addButton;
    private AbstractButton removeButton;
    private AbstractButton modifyButton;*/
    
    private JLabel taskLabel;
    private JComboBox taskCombo;
    private Hashtable taskHash;

    private JLabel lpLabel;
    private JComboBox lpCombo;
    private Hashtable lpHash;
    
    private JLabel dataLabel;
    private JTextField dataField;
    private AbstractButton browseButton;
    private JFileChooser chooser;

    private AbstractButton createButton;
    private AbstractButton cancelButton;

    // Custom data model for tagset table
    //private TagTableData dataModel;
    
    private TallalSystem mySys;    

    boolean modifying;
    
    //private TagAddDialog tad;
    
    // main method for testing purposes
    /*
    public static void main (String[] args) {

	JFrame parent = new JFrame();
	
	TallalProjectDialog tpc = new TallalProjectDialog(parent);
	
	tpc.setVisible(true);

	System.exit(0);

    }
    */
    
    public TallalSystemCreator (JDialog parent, TallalSystem sys) throws RemoteException {

	super(parent, "Modify TALLAL System", true);
	
	mySys = sys;
	modifying = true;

	initInterface();
	setContentPane(panel);
	pack();
    }
  
    public TallalSystemCreator() throws RemoteException {
	new TallalSystemCreator(new JDialog());
    }

    public TallalSystemCreator (JDialog parent) throws RemoteException {
	super(parent, "Create TALLAL System", true);

	modifying = false;
	
       	initInterface();
	setContentPane(panel);
	pack();

    }

    public TallalSystem getSystem() {

	return mySys;

    }

    private void initInterface () throws RemoteException {

	//tad = new TagAddDialog(this);
	
	panel = new JPanel();

	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

	Font labelFont = (Font)UIManager.get("TextField.font");

	JPanel projPanel = new JPanel();
	projPanel.setLayout(new BoxLayout(projPanel, BoxLayout.Y_AXIS));
	projPanel.setBorder(BorderFactory.createTitledBorder("Project Info:"));
	
	JPanel namePanel = new JPanel(new BorderLayout());
	namePanel.setBorder(BorderFactory.createEmptyBorder(0,2,2,2));
	nameLabel = new JLabel("Name:");
	nameField = new JTextField();
	nameLabel.setFont(labelFont);
	namePanel.add(nameLabel, BorderLayout.NORTH);
	namePanel.add(nameField, BorderLayout.CENTER);
	
	JPanel descPanel = new JPanel(new BorderLayout());
	descPanel.setBorder(BorderFactory.createEmptyBorder(0,2,2,2));
	descLabel = new JLabel("Description:");
	descLabel.setFont(labelFont);
	descTextArea = new JTextArea(4,20);
	descTextArea.setLineWrap(true);
	descTextArea.setWrapStyleWord(true);
	JScrollPane descScrollPane = new JScrollPane(descTextArea);
	descPanel.add(descLabel, BorderLayout.NORTH);
	descPanel.add(descScrollPane, BorderLayout.CENTER);

	JPanel lpTaskPanel = new JPanel();
	lpTaskPanel.setLayout(new BoxLayout(lpTaskPanel, BoxLayout.X_AXIS));
		
	JPanel taskPanel = new JPanel(new BorderLayout());
	taskPanel.setBorder(BorderFactory.createEmptyBorder(0,2,2,2));
	taskLabel = new JLabel("Task:");
	taskLabel.setFont(labelFont);

	// Obtain real tasks someday
	java.util.List tasks = Jawb.getTaskManager().getTasks();
	String[] taskNames = new String[tasks.size()];
	taskHash = new Hashtable();

	for(int i = 0; i < tasks.size(); i++) {
	    taskNames[i] = ((Task)tasks.get(i)).getTitle();
	    taskHash.put(taskNames[i], tasks.get(i));
	}

	taskCombo = new JComboBox(taskNames);
	taskPanel.add(taskLabel, BorderLayout.NORTH);
	taskPanel.add(taskCombo, BorderLayout.SOUTH);
		
	JPanel lpPanel = new JPanel(new BorderLayout());
	lpPanel.setBorder(BorderFactory.createEmptyBorder(0,2,2,2));
	lpLabel = new JLabel("LP:");
	lpLabel.setFont(labelFont);

	lpHash = new Hashtable();
	LP[] lps = TallalController.getController().getLPs();
	String[] lpNames = new String[lps.length];

	for(int i = 0; i < lps.length; i++) {
	    lpNames[i] = lps[i].getDesc();
	    lpHash.put(lpNames[i], lps[i]);
	}

	lpCombo = new JComboBox(lpNames);
	lpPanel.add(lpLabel, BorderLayout.NORTH);
	lpPanel.add(lpCombo, BorderLayout.SOUTH);
		
	lpTaskPanel.add(taskPanel);
	lpTaskPanel.add(lpPanel);

	projPanel.add(namePanel);
	projPanel.add(descPanel);
	projPanel.add(lpTaskPanel);

	JPanel dataPanel = new JPanel(new BorderLayout());
	dataPanel.setBorder(BorderFactory.createTitledBorder("Training Data Path:"));
	
	JPanel datafPanel = new JPanel(new BorderLayout());
	datafPanel.setBorder(BorderFactory.createEmptyBorder(0,2,2,2));
	dataField = new JTextField();
	datafPanel.add(dataField, BorderLayout.CENTER);
	
	JPanel browsePanel = new JPanel(new BorderLayout());
	browsePanel.setBorder(BorderFactory.createEmptyBorder(0,2,2,2));
	
	chooser = new JFileChooser();
	chooser.setMultiSelectionEnabled(false);
      
	browseButton = new JButton(new AbstractAction("Browse") {
		
		public void actionPerformed(ActionEvent e) {

		    chooser.setDialogTitle("Choose directory");
		    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    int rval = chooser.showDialog(TallalSystemCreator.this, "Select");
		    if (rval == JFileChooser.APPROVE_OPTION) {
			File dir = chooser.getSelectedFile();
			dataField.setText(dir.getAbsolutePath());
		    }
	
		}

	    });

	browsePanel.add(browseButton, BorderLayout.CENTER);

	dataPanel.add(datafPanel, BorderLayout.CENTER);
	dataPanel.add(browsePanel, BorderLayout.EAST);
	
	//JPanel tagPanel = new JPanel(new BorderLayout());
	//tagPanel.setBorder(BorderFactory.createTitledBorder("Tags:"));

	
	//JPanel tablePanel = new JPanel();
	//tablePanel.setBorder(BorderFactory.createEmptyBorder(0,2,2,2));
	
	//tagTable = new JTable() {
		
	//	public static final int nameSize = 100;
	//	public static final int argSize = 100;
		
	//	public void columnAdded(TableColumnModelEvent e) {
		    
	//	    super.columnAdded(e);
		    
	//	    if (e.getToIndex() == 0)
	//		columnModel.getColumn(0).setPreferredWidth(nameSize);
	//	    else
	//		columnModel.getColumn(e.getToIndex()).setPreferredWidth(argSize);
	//	    
	//	}

		// Refer tool-tip requests to TagTableData
		/*public String getToolTipText(MouseEvent e) {

		    Point p = e.getPoint();
		    
		    int row = rowAtPoint(p);
		    int col = columnAtPoint(p);

		    if (row > -1 && col > -1)
			return ((TagTableData)dataModel).getToolTipText(row, col);
					
		    return null;

		    }*/

	//	    };
	
	//tagTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	//tagTable.setModel(dataModel);
	//tagTable.setPreferredScrollableViewportSize(new Dimension(400,70));
	
	//tablePanel.add(new JScrollPane(tagTable));
	
	//JPanel tagButtonPanel = new JPanel();
	//tagButtonPanel.setLayout(new BoxLayout(tagButtonPanel, BoxLayout.Y_AXIS));
	//tagButtonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,2,2));

	// Use TagAddDialog to add a tag to the table
	/*
	addButton = new JButton(new AbstractAction("Add") {
		public void actionPerformed(ActionEvent e) {
		    
		    tad.reset();

		    tad.setVisible(true);
		    
		    if (tad.getCancelled() == false) {
			
			dataModel.addRow(tad.getTag());
			
		    }
		    
		}

	    });
	*/
	// Remove the selected tag from the table
	/*removeButton = new JButton(new AbstractAction("Remove") {
		public void actionPerformed(ActionEvent e) {
		    
		    int[] rsel = tagTable.getSelectedRows();
		    
		    for (int i = 0; i < rsel.length; i++)
			dataModel.removeRow(rsel[i]);
		}
	    });
	*/
	/*modifyButton = new JButton(new AbstractAction("Modify") {
		public void actionPerformed(ActionEvent e) {

		    int rsel = tagTable.getSelectedRow();
		    
		    if (rsel > -1 ) {
			
			TagData tag = dataModel.getRow(rsel);
			
			tad.reset(tag);
			
			tad.setVisible(true);
			
			if (tad.getCancelled() == false) {

			    dataModel.removeRow(rsel);
			    dataModel.addRow(tad.getTag());

			}

		    }
			
		}
	    });
	*/
	//tagButtonPanel.add(addButton);
	//tagButtonPanel.add(modifyButton);
	//tagButtonPanel.add(removeButton);
	
	//tagPanel.add(tablePanel, BorderLayout.CENTER);
	//tagPanel.add(tagButtonPanel, BorderLayout.EAST);
	
	JPanel buttonPanel = new JPanel(new FlowLayout());
	buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,2,2,2));

	if (modifying == false) {

	    createButton = new JButton(new AbstractAction("Create") {
		    
		    public void actionPerformed(ActionEvent e) {
			
			if (validateData() == true) {
			    mySys = new TallalSystem(nameField.getText());
			    mySys.setDesc(descTextArea.getText());
			    
			    mySys.setTask((Task)taskHash.get(taskCombo.getSelectedItem()));
			    mySys.setLP((LP)lpHash.get(lpCombo.getSelectedItem()));
			    mySys.setDataOrURL(dataField.getText());
			    mySys.setTags(new String[] { "not", "ready", "yet" });
			    
			    setVisible(false);
			    
			}
			
		    }
		    
		});

	} else {

	    createButton = new JButton(new AbstractAction("Modify") {

		    public void actionPerformed(ActionEvent e) {

			if (validateData() == true) {

			    mySys.setDesc(descTextArea.getText());
						    
			    mySys.setTask((Task)taskHash.get(taskCombo.getSelectedItem()));
			    mySys.setLP((LP)lpHash.get(lpCombo.getSelectedItem()));
			    mySys.setDataOrURL(dataField.getText());
			    mySys.setTags(new String[] { "not", "ready", "yet" });
			    
			    setVisible(false);

			}

		    }

		});

	}

	cancelButton = new JButton(new AbstractAction("Cancel") {

		public void actionPerformed(ActionEvent e) {
		    
		    setVisible(false);

		}

	    });
	
	buttonPanel.add(createButton);
	buttonPanel.add(cancelButton);

	panel.add(projPanel);
	panel.add(dataPanel);
	//	panel.add(tagPanel);
	panel.add(buttonPanel);

	if (modifying) {

	    nameField.setEditable(false);
	    nameField.setText(mySys.getName());
	    
	    descTextArea.setText(mySys.getDesc());
	    
	    taskCombo.setSelectedItem(mySys.getTask().getTitle());
	    lpCombo.setSelectedItem(mySys.getLP().getDesc());
	    
	    dataField.setText(mySys.getDataOrURL());
	    
	}
   
    }

    // Give user entries the once-over
    private boolean validateData() {
	
	// Check name field
	String name = nameField.getText();
	if (name.equals("")) {
	    
	    JOptionPane.showMessageDialog(this, "Please enter a project name.", "Invalid Entry", JOptionPane.OK_OPTION, null);
	    
	    nameField.requestFocus();

	    return false;

	}

	// Ensure that directory exists
	// Removed on William's request

	/* File dir = new File(dataField.getText());
	if (!dir.exists()) {

	    JOptionPane.showMessageDialog(this, "Data path directory unavailable; please enter another.", "Invalid Entry", JOptionPane.OK_OPTION, null);

	    dataField.requestFocus();

	    return false;

	}
	*/
	
	return true;

    }
    
    // Create the .DTD file as a String
    /* public String createDTD () {
	
	Vector data = dataModel.getData();
       	TagData td;
	String dtd = "";
	
	for (int i = 0; i < data.size(); i++) {
	    
	    td = (TagData)data.get(i);
	    
	    dtd = dtd.concat("<!ELEMENT ").concat(td.name).concat(">\n");
	    
	    if (td.args.length > 0) {

		dtd = dtd.concat("<!ATTLIST ").concat(td.name).concat("\n");
		
		for (int j = 0; j < td.args.length; j++) {

		    //dtd = dtd.concat("\t").concat(td.args[j].name).concat("\t").concat(td.args[j].type).concat("\t").concat(td.args[j].valueType);
		    dtd = dtd.concat("\t").concat(td.args[j].name).concat("\tCDATA\t#FIXED\t").concat(td.args[j].value).concat("\n");

		    //if (!td.args[j].value.equals(""))
		    //	dtd = dtd.concat("\t").concat(td.args[j].value);
		    
		    // dtd = dtd.concat("\n");

		}

		dtd = dtd.concat(">\n");

	    }
	    
	}
	
	return dtd;
	
    }
    
    // Create the .xml file as a String
    public String createProject () {

	String project = new String("<xml>\n\t<tallal-project>\n").concat("\t\t<name>").concat(nameField.getText()).concat("</name>\n").concat("\t\t<description>").concat(descTextArea.getText()).concat("</description>\n").concat("\t\t<tagset>").concat("test.dtd").concat("</tagset>\n").concat("\t\t<preannotate-lp>").concat((String)(lpCombo.getSelectedItem())).concat("-tagger</preannotate-lp>\n").concat("\t\t<training-lp>").concat((String)(lpCombo.getSelectedItem())).concat("-learner</training-lp>\n").concat("\t\t<training-data-path>").concat(dataField.getText()).concat("</training-data-path>\n").concat("\t</tallal-project>\n</xml>\n");

	return project;
	
    }
    
    */
}
    
// Class to hold argument information 
//class ArgData {

//    public String name;
//    public String value;
    //public String valueType;
    //public String value;

//    public ArgData(String argName, String argValue/*, String argValueType, String argValue*/) {

//	name = argName;
//	value = argValue;
	//valueType = argValueType;
	//value = argValue;

//    }

    // String to be displayed as tool-tip-text
    /*public String getParamString() {

	String str = new String("");
	
	str = str.concat("Type: ").concat(type).concat(" Value: ").concat(valueType);
	
	if (!value.equals(""))
	    str = str.concat(" ").concat(value);
	
	return str;
	
	}*/

//}

// Class to hold tag information
//class TagData {

//    public String name;
//    public ArgData[] args;
    
//    public TagData(String tagName, int nArgs) {

//	name = tagName;
	
//	args = new ArgData[nArgs];

//    }    

//}

// Custom model for tagset table
//class TagTableData extends AbstractTableModel {

//    protected Vector tags;
//    protected int nColumns;
   
//    public TagTableData() {

//	tags = new Vector();
//	nColumns = 1;

//    }

    /*public String getToolTipText(int row, int col) {

	TagData td = (TagData)tags.get(row);
      
	if (col > 0 && col < td.args.length + 1)
	    return td.args[col-1].getParamString();
	else
	    return null;
	
	    }*/

//    public Vector getData() {

//	return tags;

//    }

//    public void reset() {
	
//	nColumns = 0;
//	tags.removeAllElements();

//    }

//    public TagData getRow(int rnum) {

//	return (TagData)tags.get(rnum);	

//    }

//    public void addRow(TagData tag) {

//	if (tag.args.length + 1 > nColumns) {
//	    nColumns = tag.args.length + 1;
//	    fireTableStructureChanged();
//	}
	    
//	tags.add(tag);

//       	fireTableRowsInserted(tags.size()-1, tags.size()-1);

//    }

//    public void removeRow(int nRow) {

//	TagData tag;
//int cMax = 1;
	
//	if (nRow >= 0 && nRow < tags.size())	
//	    tags.remove(nRow);

//	fireTableRowsDeleted(nRow, nRow);

//	for (int i = 0; i < tags.size(); i++) {

//    tag = (TagData)tags.get(i);
	    
//	    if (tag.args.length + 1 > cMax)
//		cMax = tag.args.length + 1;
	    
//	}

//	if (cMax < nColumns) {

//	    nColumns = cMax;
//	    fireTableStructureChanged();

//	}

//    }
					     
//    public int getRowCount() {

//	return tags == null ? 0 : tags.size();

//    }

//    public int getColumnCount() {

//	return nColumns;

//    }

//    public String getColumnName(int column) {

//	if (column == 0) return "Name";
	
//	return new String("Arg #").concat(String.valueOf(column));
	
//    }
    
//    public boolean isCellEditable(int nRow, int nCol) {

//	return false;
	
//    }

//  public Object getValueAt(int nRow, int nCol) {
/*
	if (nRow < 0 || nRow >= getRowCount())
	    return "";
	
	TagData row = (TagData)tags.elementAt(nRow);
	
	if (nCol == 0) return row.name;
	
	if (nCol > row.args.length) return "";

	return row.args[nCol-1].name.concat("=\"").concat(row.args[nCol-1].value).concat("\"");

    }

}
*/
// Dialog for adding a tag to the tagset table
/*class TagAddDialog extends JDialog {

    private JPanel panel;

    private JTextField nameField;

    private JButton addButton;
    private JButton cancelButton;
    private JButton newButton;

    private JPanel argumentPanel;
    private JPanel aPanel;
    private Vector argPanels;
  
    private boolean cancelled;

    private class ArgPanel extends JPanel {

	public JTextField nameField;
	// public JComboBox typeCombo;
	// public JComboBox valueCombo;
	// public JTextField valueField;
	public JTextField valueField;
	public JButton delButton;

	public ArgPanel(ArgData arg) {

	    this();
	    
	    boolean found = false;

	    nameField.setText(arg.name);
*/
	    /*	    for (int i = 0; i < typeCombo.getItemCount() && found == false; i++) {

		if (arg.type.equals((String)typeCombo.getItemAt(i))) {
		    typeCombo.setSelectedIndex(i);
		    found = true;
		}
		
		}

	    if (found == false) {

		typeCombo.setEditable(true);
		typeCombo.setSelectedItem(arg.type);

		}*/

	    /*found = false;

	    for (int i = 0; i < valueCombo.getItemCount() && found == false; i++) {

		if (arg.valueType.equals((String)valueCombo.getItemAt(i))) {
		    valueCombo.setSelectedIndex(i);
		    found = true;
		}
		
		}*/
/*
	    valueField.setText(arg.value);

	}

	public ArgPanel() {
		
	    super();
*/    
	    /*	    String[] argTypes = {"CDATA",
				 "(enumerated)",
				 "ID",
				 "IDREF",
				 "IDREFS",
				 "NMTOKEN",
				 "NMTOKENS",
				 "ENTITY",
				 "ENTITIES",
				 "NOTATION"};
	    
	    String[] argValues = {"#REQUIRED",
				  "#IMPLIED",
				  "#FIXED",
				  "#DEFAULT"};

	    */
/*
	    delButton = new JButton(new AbstractAction("Delete") {
		    public void actionPerformed(ActionEvent e) {
						
			TagAddDialog.this.removeArg(ArgPanel.this);
			
		    }
		});
	    
	    nameField = new JTextField();
*/
	    /*
	    typeCombo = new JComboBox(argTypes);
	    typeCombo.addActionListener(new AbstractAction() {
		    public void actionPerformed(ActionEvent e) {
			
			String item = (String)(((JComboBox)e.getSource()).getSelectedItem());

			if (item.equals("(enumerated)"))
			    ((JComboBox)e.getSource()).setEditable(true);
			else
			    ((JComboBox)e.getSource()).setEditable(false);
			
		    }

		});

	    valueCombo = new JComboBox(argValues);
	    valueCombo.addActionListener(new AbstractAction() {
		    public void actionPerformed(ActionEvent e) {
			
			String item = (String)(((JComboBox)e.getSource()).getSelectedItem());
			
			if (item.equals("#DEFAULT") || item.equals("#FIXED")) {
			    
			    valueField.setEnabled(true);
			    valueField.setBackground(Color.WHITE);
			    valueField.requestFocus();
			    			    
			} else {

			    valueField.setEnabled(false);
			    valueField.setBackground(Color.LIGHT_GRAY);
			    valueField.setText("");
			    
			}
		    
		    }

		});

	    valueField = new JTextField();
	    valueField.setPreferredSize(new Dimension(40, valueField.getSize().height));
	    valueField.setEnabled(false);
	    valueField.setBackground(Color.LIGHT_GRAY);
	    valueField.setText("");
	    */
/*
	    valueField = new JTextField();
	    
	    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	    setBorder(BorderFactory.createEmptyBorder(0,2,2,2));

	    Font labelFont = (Font)UIManager.get("TextField.font");

	    JLabel nameLabel = new JLabel("Name:");
	    JLabel valueLabel = new JLabel("Value:");
	    
	    nameLabel.setFont(labelFont);
	    nameLabel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	    valueLabel.setFont(labelFont);
	    valueLabel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));

	    add(delButton);
	    add(nameLabel);
	    add(nameField);
	    add(valueLabel);
	    add(valueField);

	    //add(typeCombo);
	    //add(valueCombo);
	    //add(valueField);
    
	}

    }

    protected void removeArg(ArgPanel arg) {
		
	argumentPanel.remove(arg);
	argPanels.removeElement(arg);
	
	pack();
	repaint();

    }

    public boolean getCancelled() {

	return cancelled;

    }

    public TagAddDialog(JDialog parent) {

	super(parent, "Add tag...", true);
	
	cancelled = false;
	
	initInterface();
	
	setContentPane(panel);
	pack();

    }

    public TagData getTag() {
	
	TagData td = new TagData(nameField.getText(), argPanels.size());
	
	for (int i = 0; i < td.args.length; i++) {
	    
	    ArgPanel panel = (ArgPanel)argPanels.get(i);
    
	    //	    td.args[i] = new ArgData(panel.nameField.getText(), (String)panel.typeCombo.getSelectedItem(), 
	    //		 (String)panel.valueCombo.getSelectedItem(), panel.valueField.getText());
	
	    td.args[i] = new ArgData(panel.nameField.getText(), panel.valueField.getText());

	}

	return td;
	
    }

    private void initInterface() {

	panel = new JPanel();

	argPanels = new Vector();
	
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

	Font labelFont = (Font)UIManager.get("TextField.font");
	
	JPanel tempPanel, tagPanel;
	JLabel tempLabel;
			
	nameField = new JTextField();
	
	tagPanel = new JPanel(new BorderLayout());
	tagPanel.setBorder(BorderFactory.createTitledBorder("Tag:"));
	
	tempLabel = new JLabel("Name:");
	tempLabel.setFont(labelFont);
	tempPanel = new JPanel(new BorderLayout());
	tempPanel.add(tempLabel, BorderLayout.NORTH);
	tempPanel.add(nameField, BorderLayout.CENTER);
	tempPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	
	tagPanel.add(tempPanel, BorderLayout.CENTER);
	
	panel.add(tagPanel);

	tempPanel = new JPanel();
	tempPanel.setLayout(new BorderLayout());
	tempPanel.setBorder(BorderFactory.createTitledBorder("Arguments:"));
	
	newButton = new JButton(new AbstractAction("New Argument") {
		public void actionPerformed(ActionEvent e) {
		    
		    ArgPanel panel = new ArgPanel();
		    
		    argPanels.add(panel);
		    
		    argumentPanel.add(panel);
		    		 
		    setSize(getPreferredSize());
   		    
		    pack();
		    repaint();

		}

	    });
	   
	argumentPanel = new JPanel();
	argumentPanel.setLayout(new GridLayout(0,1));
	argumentPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));

	tempPanel.add(argumentPanel, BorderLayout.NORTH);
	tempPanel.add(newButton, BorderLayout.CENTER);

	panel.add(tempPanel);
	
	JPanel buttonPanel = new JPanel(new FlowLayout());
	buttonPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
	
	addButton = new JButton(new AbstractAction("Add") {
		public void actionPerformed(ActionEvent e) {
		    
		    if (validateData() == true)
			setVisible(false);
		    
		}

	    });
	
	cancelButton = new JButton(new AbstractAction("Cancel") {
		public void actionPerformed(ActionEvent e) {

		    cancelled = true;
		    setVisible(false);

		}
	    });    
	
	buttonPanel.add(addButton);
	buttonPanel.add(cancelButton);
	
	panel.add(buttonPanel);
	
    }

    public void reset() {

	argPanels.removeAllElements();
	argumentPanel.removeAll();
		
	addButton.setText("Add");
	this.setTitle("Add tag...");

	nameField.setText("");
	nameField.requestFocus();
	
	setSize(getPreferredSize());

    }

    public void reset(TagData tag) {

	argPanels.removeAllElements();
	argumentPanel.removeAll();
	
	addButton.setText("Modify");
	this.setTitle("Modify tag...");

	nameField.setText(tag.name);
	
	for (int i = 0; i < tag.args.length; i++) {

	    ArgPanel panel = new ArgPanel(tag.args[i]);
	    
	    argPanels.add(panel);
	    
	    argumentPanel.add(panel);
	    
	}
	
	nameField.requestFocus();
	
	setSize(getPreferredSize());

    }

    public Dimension getMinimumSize() {

	return (new Dimension(500, super.getMinimumSize().height));

    }

    public Dimension getPreferredSize() {

	return getMinimumSize();

    }

    private boolean validateData() {

	// Check name field;
	String name = nameField.getText();
	if (name.equals("")) {
	    
	    JOptionPane.showMessageDialog(this, "Please enter a tag name.", "Invalid Entry", JOptionPane.OK_OPTION, null);
	    
	    nameField.requestFocus();

	    return false;

	}
	if (!name.matches("[a-zA-Z0-9_]*")) {

	    JOptionPane.showMessageDialog(this, "Tag name must consist of letters, numbers, and underscores only.", "Invalid Entry", JOptionPane.OK_OPTION, null);

	    nameField.requestFocus();

	    return false;

	}

	// Check arguments
	for (int i = 0; i < argPanels.size(); i++) {
	    
	    ArgPanel panel = (ArgPanel)argPanels.get(i);

	    name = panel.nameField.getText();
	    if (name.equals("")) {
    
		JOptionPane.showMessageDialog(this, "Please enter an argument name.", "Invalid Entry", JOptionPane.OK_OPTION, null);
		
		panel.nameField.requestFocus();
		
		return false;
		
	    }
	    if (!name.matches("[a-zA-Z0-9_]*")) {
		
		JOptionPane.showMessageDialog(this, "Argument name must consist of letters, numbers, and underscores only.", "Invalid Entry", JOptionPane.OK_OPTION, null);
		
		panel.nameField.requestFocus();
		
		return false;
		
	    }
	    	    
	}
	
	return true;

    }
    
}
*/
