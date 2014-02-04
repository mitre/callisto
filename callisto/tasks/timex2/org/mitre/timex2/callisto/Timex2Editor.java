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

package org.mitre.timex2.callisto;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.swing.*;
import javax.swing.text.Position.Bias;
import javax.swing.border.EtchedBorder;
import javax.swing.event.*;

import org.mitre.jawb.swing.CalendarPanel;
import org.mitre.jawb.swing.EnableLabel;
import org.mitre.jawb.swing.PopupWindow;

/**
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */
public class Timex2Editor extends JPanel {

  private static final int DEBUG = 0;

  /***********************************************************************/
  /*
   * Implementation Notes:
   * All SubEditors must use the same Calendar in the DateFormatters to print
   * the corrrect values. Specifically, the Java default calendar doesn't
   * figure WEEK_OF_YEAR like the iso standard.
   *
   * Generally I refer to a Date object as 'time' since the Calendar object
   * treats it similarly (see Calendar.getTime()).
   *
   * The Timex2 annotation manual (June 2002 Ferro, et. al.) states that
   * ambiguous Point based dates may be specified by week, but also include
   * the month:
   *    1998-FA-WXX-5TNI -> One Friday night in fall 1998.
   * I've allowed the regular expressions to accept this, though haven't added
   * a widget for it. Instead, there's extra logic to append it to the
   * year, in the text field, and allow the user to specify it there.
   */

  /**
   * Determines if TOD editors are enabled when initially editing an empty
   * Timex2 tag. If false, opening an empty tag will have the 'No Value
   * Specified' check turned on, and all editors will all be disabled.
   */
  public static boolean ENABLE_EDITING_BY_DEFAULT = true;

  /********************/
  /* Code in this component relies on the fact that for all these 'setfills'
   * the first item is going to be the 'unset' value */
  private static final String[] monthValues = new String[]
  {"  ","01","02","03","04","05","06","07","08","09","10","11","12",
   "H1","H2","HX","Q1","Q2","Q3","Q4","QX","WI","SP","SU","FA","XX"};
  private static final String[] weekValues = new String[]
  {"  ","01","02","03","04","05","06","07","08","09",
   "10","11","12","13","14","15","16","17","18","19",
   "20","21","22","23","24","25","26","27","28","29",
   "30","31","32","33","34","35","36","37","38","39",
   "40","41","42","43","44","45","46","47","48","49",
   "50","51","52","53","XX"};
  private static final String[] mDayValues = new String[]
  {"  ","01","02","03","04","05","06","07","08","09",
   "10","11","12","13","14","15","16","17","18","19",
   "20","21","22","23","24","25","26","27","28","29",
   "30","31","XX",};
  private static final String[] wDayValues = new String[]
  {" ","1","2","3","4","5","6","7","WE","X"};
  private static final String[] hourValues = new String[]
  {"  ","00","01","02","03","04","05","06","07","08","09","10","11",
   "12","13","14","15","16","17","18","19","20","21","22","23","24",
   "AF","DT","EV","MI","MO","NI","XX"};
  private static final String[] minSecValues = new String[]
  {"  ","00","01","02","03","04","05","06","07","08","09",
   "10","11","12","13","14","15","16","17","18","19",
   "20","21","22","23","24","25","26","27","28","29",
   "30","31","32","33","34","35","36","37","38","39",
   "40","41","42","43","44","45","46","47","48","49",
   "50","51","52","53","54","55","56","57","58","59","XX"};
  private static final String[] timezoneValues = new String[]
  {" ","-12","-11","-10","-09","-08","-07","-06","-05",
   "-04","-03","-02","-01","Z","+01","+02","+03","+04",
   "+05","+06","+07","+08","+09","+10","+11","+12"};
  private static final String[] zoneStrings = new String []
  {" ","-12","-11","-10 (Hawaii ST)","-9 (AST, Hawaii DT)",
   "-8 (PST, Alaska DT)","-7 (MST, Pacific DT)",
   "-6 (CST, Mountain DT)","-5 (EST, Central DT)",
   "-4 (Atlantic ST, EDT)","-3 (Atlantic DT)", "-2","-1",
   "Z (GMT)","+1 (Central Europe)","+2 (Eastern Europe)",
   "+3 (E.Europe Summer)","+4","+5","+6","+7","+8 (China,W.Australia)",
   "+9","+10 (E.Australia ST)","+11 (E.Australia DT)","+12"};
  private static final String longestZoneString = 
    // new String("+10 (E.Australia ST)");
    new String("+8 (China,W.Australia)");


  private static final String[] durationLCEYValues = new String[]
  {" ","0","1","2","3","4","5","6","7","8","9"}; // duration needs no 'X'
  private static final String[] durationYTypeValues = new String[]
  {"Y","FY"};
  private static final String[] durationMValues = new String[]
  {"  ","01","02","03","04","05","06","07","08","09","10","11","12"};
  private static final String[] durationDValues = new String[]
  {"  ","01","02","03","04","05","06","07","08","09",
   "10","11","12","13","14","15","16","17","18","19",
   "20","21","22","23","24","25","26","27","28","29",
   "30","31"};
  private static final String[] durationHValues = new String[]
  {"  ","00","01","02","03","04","05","06","07","08","09","10","11","12"};
  private static final String[] durationMSValues = minSecValues;
  private static final String[] durWTypeValues = new String[]
  {"W","DE","CE","ML","WI","SP","SU","FA","H1","H2","HX","Q1","Q2","Q3","Q4","QX",
   "WE","MO","MI","AF","EV","NI","DT"};

  private static final String[] prefixValues = new String[]
  {" ","FY","BC","KA","MA","GA"};
  
  private static final String[] tokenValues = new String[]
  {"PRESENT_REF","FUTURE_REF","PAST_REF"};

  private static final String[] modPDValues = new String[]
  {"","START","MID","END","APPROX"};
  private static final String[] modPointValues = new String[]
  {"BEFORE","AFTER","ON_OR_BEFORE","ON_OR_AFTER"};
  private static final String[] modDurationValues = new String[]
  {"LESS_THAN","MORE_THAN","EQUAL_OR_LESS","EQUAL_OR_MORE"};
  
  private static final String[] anchorDirValues = new String[]
  {"WITHIN","STARTING","ENDING","AS_OF","BEFORE","AFTER"};

  private static final String[] pastRefAnchorDirValues = new String[]
    {"BEFORE"};
  
  private static final String[] futureRefAnchorDirValues = new String[]
    {"AFTER"};
  
  // components of header panel
  private JTextField textField;
  private JCheckBox noValueCheck;

  // means of accessing attribute values, and their panels
  private ValEditor valEditor;
  private JPanel nonSpecificPanel;
  private JCheckBox nonSpecificCheck;
  private SetEditor setEditor;
  private AnchorEditor anchorEditor;
  private JPanel commentPanel;
  private JTextArea commentText;
  
  // listener that modifies highlighting of the Anchor panel and may
  // set AnchorDir according to the current Value SubEditor selected,
  // the value token chosen (if applicable) and whether NonSpecific is
  // checked
  private static AnchorRequiredListener anchorRequiredListener;
  
  // Date manipulation
  Calendar calendar;

  // used in toString
  Timex2Data timex2Data = new Timex2Data ();
  
  /**
   * Creates and returns a new dialog containing the specified
   * <code>Timex2Editor</code> pane along with "OK", "Cancel", and "Reset"
   * buttons. If the "OK" or "Cancel" buttons are pressed, the dialog is
   * automatically hidden (but not disposed).  If the "Reset" button is
   * pressed, the timex2s values will be reset to the value which was set the
   * last time <code>show</code> was invoked on the dialog and the dialog will
   * remain showing.
   *
   * @param c              the parent component for the dialog
   * @param title          the title for the dialog
   * @param modal          a boolean. When true, the remainder of the program
   *                       is inactive until the dialog is closed.
   * @param chooserPane    the Timex2Editor to be placed inside the dialog
   * @param okListener     the ActionListener invoked when "OK" is pressed
   * @param cancelListener the ActionListener invoked when "Cancel" is pressed
   * @return a new dialog containing the Timex2 editor pane
   * @exception HeadlessException if GraphicsEnvironment.isHeadless()
   * returns true.
   * @see java.awt.GraphicsEnvironment#isHeadless
   */
  public static JDialog createDialog(Component c, String title, boolean modal,
                                     Timex2Editor chooserPane,
                                     ActionListener okListener,
                                     ActionListener cancelListener)
    throws HeadlessException {
    
    return new Timex2EditorDialog(c, title, modal, chooserPane,
                                   okListener, cancelListener);
  }


  /**
   * Construct a Timex2 Panel.
   */
  public Timex2Editor() {
    setLayout (new BorderLayout ());
    setBorder (BorderFactory.createEmptyBorder (5,2,2,2));
    
    JPanel masterPanel = new JPanel ();
    masterPanel.setLayout (new BoxLayout (masterPanel, BoxLayout.Y_AXIS));
    add (masterPanel, BorderLayout.NORTH);

    boolean noValToStart = false;
    
    valEditor = new ValEditor ();
    setEditor = new SetEditor ();
    anchorEditor = new AnchorEditor ((ValEditor)valEditor);
    nonSpecificPanel = createNonSpecificPanel ();
    commentPanel = createCommentPanel ();

    anchorRequiredListener = new AnchorRequiredListener(anchorEditor,
							valEditor,
							nonSpecificCheck);

    valEditor.addTokenListener(anchorRequiredListener);    
    valEditor.addPropertyChangeListener(anchorRequiredListener);
    nonSpecificCheck.addChangeListener(anchorRequiredListener);



    // when creating the header panel, pass in the components that will be
    // enabled/disabled by the 'no value specified' check box.
    masterPanel.add (createHeaderPanel (new Component[] {valEditor,
                                                         nonSpecificCheck,
                                                         setEditor,
                                                         anchorEditor}));
    masterPanel.add (Box.createVerticalStrut (8));
    masterPanel.add (valEditor);
    masterPanel.add (Box.createVerticalStrut (8));
    Box b = Box.createHorizontalBox ();
        b.add (nonSpecificPanel);
        b.add (Box.createHorizontalStrut (8));
        b.add (setEditor);
        masterPanel.add (b);
    masterPanel.add (Box.createVerticalStrut (8));
    masterPanel.add (anchorEditor);
    masterPanel.add (Box.createVerticalStrut (8));
    masterPanel.add (commentPanel);
  }

  /**
   * A String representation as an XML open tag.
   */
  public String toString () {
    return getTimex2 (timex2Data).toString ();
  }

  public void setTimex2 (Timex2Data timex2) {
    if (DEBUG > 0)
      System.err.println ("Timex2Ed.setT2: " + timex2);
    noValueCheck.setSelected (false);
    textField.setText (timex2.text);
    valEditor.setTimeValue (timex2.val);
    valEditor.setModifier (timex2.mod);
    setEditor.setValue (timex2.set);
    if (timex2.nonSpecific != null) {
      nonSpecificCheck.setSelected (timex2.nonSpecific.equals ("YES"));
    } else {
      nonSpecificCheck.setSelected (false);
    }
    anchorEditor.setTimeValue (timex2.anchorVal);
    anchorEditor.setModifier (timex2.anchorDir);
    commentText.setText (timex2.comment);
  }
  
  /**
   * The value to be displayed by the editor.
   */
  public Timex2Data getTimex2 () {
    return getTimex2 (new Timex2Data ());
  }
  
  /**
   * The value to be displayed by the editor.
   * @see Timex2Data#toString
   */
  public Timex2Data getTimex2 (Timex2Data data) {
    // ensures known values to start
    data.clear ();
    boolean b = false;
    String tmp = null;

    data.text = textField.getText ();
    
    if (! noValueCheck.isSelected ()) {
      // VAL, MOD
      tmp = valEditor.getTimeValue ();
      if (tmp != null && ! tmp.equals ("")) {
        data.val = tmp;
        tmp = valEditor.getModifier ();
        if (tmp != null)
          data.mod = tmp;
      }

      // SET, etc
      tmp = setEditor.getValue ();
      if (tmp != null) {
	if (DEBUG > 0) {
	  System.err.println("Setting SET to " + tmp);
	}
        data.set = tmp;
      }
      
      // NON_SPECIFIC
      if (nonSpecificCheck.isSelected ())
        data.nonSpecific = "YES";

      // ANCHOR_VAL, ANCHOR_DIR
      tmp = anchorEditor.getTimeValue ();
      if (tmp != null && ! tmp.equals ("")) {
        data.anchorVal = tmp;
        tmp = anchorEditor.getModifier ();
        if (tmp != null)
          data.anchorDir = tmp;
      }
    }

    // COMMENT
    String comment = commentText.getText().trim ();
    // replace " with ' throughout
    comment = comment.replace('"', '\'');
    if (comment.length() > 0)
      data.comment = comment;

    return data;
  }

  /** Set Font of text field displaying text of annotation */
  public void setTimex2Font(Font f) {
    textField.setFont(f);
  }

  /** Retrieve Font of text field displaying text of annotation */
  public Font getTimex2Font() {
    return textField.getFont();
  }

  /**
   * Publicly accessible method to set the ValDate to the stored date
   * value without showing the widget.
   */
  public Timex2Data setValToStoredDate() {
    valEditor.setTimeValue(valEditor.dateStore);
    return getTimex2 (new Timex2Data ());
  }

  static Timex2Data data;
  static Timex2Editor editor;
  static JDialog dialog;
  static ActionListener okShowListener;
  static JTextArea text;
  static JTextField tField;
  static JButton renderComponent;
  static JButton editorComponent;
  
  /** Testing */
  public static void main (String s[]) {

    renderComponent = new JButton ();
    renderComponent.setBackground (Color.white);

    editorComponent = new JButton (" ");
    editorComponent.setBackground (Color.white);

    editor = new Timex2Editor ();
    okShowListener = new ActionListener () {
        public void actionPerformed (ActionEvent e) {
          if (e.getSource () == editorComponent) {
            if (dialog == null) {
              dialog = Timex2Editor.createDialog (text, "Edit Timex2",
                                                   true, editor,
                                                   okShowListener, null);
              data = new Timex2Data ();
            }
            data.text = tField.getText ();
            // ...
            editor.setTimex2 (data);

            dialog.setTitle ("Set Timex2: "+data.text);
            dialog.show ();
                        
          } else { // e.getSource () from ok listener in dialog
            data = editor.getTimex2 ();
            text.setText ("");
            text.append ("text: "+data.text+"\n");
            text.append ("val:  "+data.val+"\n");
            text.append ("mod:  "+data.mod+"\n");
            text.append ("set:  "+data.set+"\n");
            text.append ("nonSpecific: "+data.nonSpecific+"\n");
            text.append ("anchorVal:   "+data.anchorVal+"\n");
            text.append ("anchorDir:   "+data.anchorDir+"\n");
            text.append ("comment:     "+data.comment+"\n");
          }
        }};

    tField = new JTextField ();
    text = new JTextArea ();
    JScrollPane scroller = new JScrollPane (text);
    
    editorComponent.addActionListener (okShowListener);
    
    JFrame frame = new JFrame("Timex2Editor Demo");
    JPanel ePane = new JPanel ();
    ePane.add (editorComponent);
    
    frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
    frame.getContentPane ().add (scroller, BorderLayout.CENTER);
    frame.getContentPane ().add (ePane, BorderLayout.EAST);
    frame.getContentPane ().add (tField, BorderLayout.SOUTH);
    frame.pack ();
    frame.setBounds (300,300,450,150);
    frame.setVisible (true);
  }
  
  /***********************************************************************/

  /**
   * Simply displays string and a checkbox to enable/disable the elements in
   * the components array passed into method
   */
  private JComponent createHeaderPanel (Component[] components) {
    JLabel textLabel = new JLabel ("Text of TIMEX2: ");
    
    textField = new JTextField ();
    textField.setEditable (false);
    
    noValueCheck = new JCheckBox ("No Value Specified");
    noValueCheck.setSelected (! ENABLE_EDITING_BY_DEFAULT);
    for (int i=0; i < components.length; i++) {
      noValueCheck.addItemListener (new EnableAction (components[i], false));
      components[i].setEnabled (ENABLE_EDITING_BY_DEFAULT);
    }
    
    JPanel textPanel = new JPanel (new BorderLayout ());
    JPanel textFieldPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));

    textFieldPanel.add (textLabel);
    textFieldPanel.add (textField);

    textPanel.add (textFieldPanel, BorderLayout.WEST);
    textPanel.add (noValueCheck, BorderLayout.EAST);
    
    return textPanel;
  }

  /**
   * Simply displays string and a checkbox to enable/disable the elements in
   * the components array passed into method
   */
  private JPanel createNonSpecificPanel () {

    GridBagLayout bag = new GridBagLayout ();
    JPanel panel = new JPanel (bag);
    panel.setBorder (BorderFactory.createTitledBorder ("NON_SPECIFIC"));
    panel.addPropertyChangeListener (new EnablePropogater ());
    
    GridBagConstraints cons = new GridBagConstraints ();
    cons.fill = cons.NONE;

    nonSpecificCheck = new JCheckBox ("Non Specific");

    bag.setConstraints (nonSpecificCheck, cons);
    panel.add (nonSpecificCheck);
    
    return panel;
  }
  
  /**
   * Simply displays string and a checkbox to enable/disable the elements in
   * the components array passed into method
   */
  private JPanel createCommentPanel () {
    JPanel panel = new JPanel (new BorderLayout ());
    panel.setBorder (BorderFactory.createTitledBorder ("COMMENT"));
    
    commentText = new JTextArea ("", 2,20);
    JScrollPane scroller = new JScrollPane (commentText);
    panel.add (scroller);
    
    return panel;
  }

  /***********************************************************************/

  /**
   * Superclass of ValEditor and AnchorEditor and popups in SetEditor, holding
   * thier shared code. Each should call setCurrentSubEditor after adding all
   * the sub editors, to ensure one is selected.
   */
  private static abstract class TimeEditorPanel extends JPanel {
    
    ButtonGroup editorGroup;
    TimeSubEditor currentSubEditor;

    JPanel buttonPanel;
    
    GridBagLayout gridBag;
    GridBagConstraints cons;

    /** allow saving and recall of date between DateEditorPanels */
    static String dateStore = null;

    /** set up the panel */
    TimeEditorPanel () {
      // ********************
      // get ready to lay out the grid bag:
      // the checkboxes are one column, and the editors are each two columns
      // wide. the MOD panel is two columns wide and the save/restore buttons
      // are in a single column.
      gridBag = new GridBagLayout ();
      cons = new GridBagConstraints ();
      setLayout (gridBag);

      // checkbox group for editors added later
      editorGroup = new ButtonGroup ();
    }

    /**
     * Set the specified date value (VAL or ANCHOR depending on subclass)
     * returning true if the value is valid and was set during the call.
     */
    abstract boolean setTimeValue (String val);
    
    /**
     * Returns value for currently selected editor.
     * @throws IllegalStateException if editor has not been displayed, or
     * set enabled, (which selects the current editor)
     */
    String getTimeValue () {
      if (currentSubEditor == null)
        throw new IllegalStateException
          ("Must set an editor, or diaplay widget before retrieving values");
      return currentSubEditor.getValue ();
    }
    
    /**
     * Both VAL and ANCHOR_VAL have secondary attributes which modify them
     * (MOD and ANCHOR_DIR) which can be set here. Returns true if the value
     * was appropriate and set.
     */
    abstract boolean setModifier (String val);

    /**
     * Returns value of this attributes modifier attribute (must be detailed
     * by subclass)
     */
    abstract String getModifier ();

    /**
     * Easy means for retrieving a point date, or null if user is currently
     * using a duration editor.
     */
    String getPointTime () {
      if (! (currentSubEditor instanceof PointSubEditor))
        return null;

      return currentSubEditor.getValue ();
    }
    
    
    /** overridden to enable/disable the editor checkboxes and the correct
     * editor. */
    public void setEnabled (boolean enabled) {
      Enumeration buttons = editorGroup.getElements ();
      while (buttons.hasMoreElements ()) {
        Component b = (Component)buttons.nextElement ();
        b.setEnabled (enabled);
      }
      
      if (currentSubEditor != null) {
        if (enabled)
          setCurrentSubEditor (currentSubEditor);
        else
          currentSubEditor.setEnabled (enabled);
      }

      if (buttonPanel != null)
        buttonPanel.setEnabled (enabled);
    }

    /**
     * Set the current TimeSubEditor.
     */
    protected void setCurrentSubEditor (TimeSubEditor editor) {
      // currently this retrieves the editors associated checkbox, and clicks
      // it, thus getting the benifit of makeing that the selected checkbox,
      // and the enabling/setting of 'currentSubEditor' is done in the
      // checkboxes action.  This should be easily switched to a 'combobox w/
      // cardlayout' for the editors if that's desired.
      currentSubEditor = editor;
      currentSubEditor.setEnabled (true);

      JCheckBox check = (JCheckBox)editor.getClientProperty (EDITOR_CHECK);
      check.setSelected (true);
    }

    protected TimeSubEditor getCurrentSubEditor () {
      return currentSubEditor;
    }

    static final String EDITOR_CHECK = "editorCheck";

    /**
     * creates panel with the default set of buttons on it. ANCHOR will add to
     * it, but SET won't even use it.
     */
    protected JPanel createButtonPanel () {
      Action clearAction;
      Action storeAction;
      Action recallAction;
    
      // init the button panel that sub classes can add later
      clearAction = new AbstractAction ("Clear") {
          public void actionPerformed (ActionEvent e) {
            setTimeValue (null);
            setModifier (null);
          }};
      storeAction = new StoreDateAction (true);
      recallAction = new StoreDateAction (false);
      
      JButton store = new JButton (storeAction);
      JButton recall = new JButton (recallAction);
      JButton clear = new JButton (clearAction);
      
      JPanel buttonPanel = new JPanel ();
      buttonPanel.addPropertyChangeListener (new EnablePropogater ());
      buttonPanel.add (clear);
      buttonPanel.add (store);
      buttonPanel.add (recall);

      return buttonPanel;
    }
    
    protected void addEditor (String title, TimeSubEditor editor, int gridy) {

      JCheckBox check = new JCheckBox (title);
      check.addItemListener (new EnableAction (editor));
      check.addActionListener (new SetCurrentSubEditor (editor));
      editorGroup.add (check);

      // all start off disabled
      editor.setEnabled (false);

      // since we may change the mechanics of this, associate the editor w/
      // the checkbox using a client property, so that all mucking about with
      // it can be contained within this abstract class.
      editor.putClientProperty (EDITOR_CHECK, check);
      
      cons.fill = cons.NONE;
      cons.anchor = cons.WEST;
      cons.weightx = 1;
      cons.weighty = 0;
      
      cons.gridy = gridy;
      cons.gridx = 0;
      cons.gridwidth = 1;
      gridBag.setConstraints (check, cons);
      add (check);
      
      cons.gridx = cons.RELATIVE;
      cons.gridwidth = cons.REMAINDER;
      gridBag.setConstraints (editor, cons);
      add (editor);
    }
    
    /**
     * Listener added to checkboxes to set the current subeditor (so we know
     * where to get the value from.
     */
    protected class SetCurrentSubEditor implements ActionListener {
      TimeSubEditor editor;
      
      SetCurrentSubEditor (TimeSubEditor ed) {
        editor = ed;
      }
      public void actionPerformed (ActionEvent e) {
        setCurrentSubEditor (editor);
      }
    }
    
    /**
     * Action added to buttons to store or retrieve a date. This is NOT thread
     * safe: it's assumed to only run on the GUI thread.
     */
    protected class StoreDateAction extends AbstractAction {
      boolean store;
      
      StoreDateAction (boolean store) {
        if (store)
          putValue (NAME, "Store Date");
        else
          putValue (NAME, "Retrieve Date");
        
        this.store = store;
      }
      public void actionPerformed(ActionEvent e) {
        if (store) {
	  // Use any time value, not only a point Time value
	  //          String s = getPointTime ();
	  String s = getTimeValue ();
          if (s != null && s.length() > 0) {
            dateStore = s;
          }
        } else {
          setTimeValue (dateStore);
        }
      }
    }
  }
  
  /***********************************************************************/

  /**
   * Specific panel for editing VAL and MOD attribute.
   */
  private static class ValEditor extends TimeEditorPanel {

    // sub editors
    PointSubEditor valDate;
    PointSubEditor valWeek;
    DurationSubEditor valDateDur;
    DurationSubEditor valWeekDur;
    TokenEditor valToken;

    JComboBox modCombo;

    ValEditor () {
      init ();
      setCurrentSubEditor (valDate);
    }

    /** overridden to enable/disable certain components and labels too. */
    public void setEnabled (boolean enabled) {
      // get the basics
      super.setEnabled (enabled);
      // and also our modifier
      modCombo.setEnabled (enabled);
    }
    
    /**
     * Display the specified value, or clear/reset if null.
     * @deprecated foo
     */
    boolean setTimeValue (String value) {
      // All this makes sure that when an editor sees a valid string, the rest
      // are passed null to reset. finally, a warning is given if it's bad
      boolean valid = false;
      if (valDate.setValue (value)) {
        setCurrentSubEditor (valDate);
        value = null;
        valid = true;
      }
      if (valWeek.setValue (value)) {
        setCurrentSubEditor (valWeek);
        value = null;
        valid = true;
      }
      if (valDateDur.setValue (value)) {
        setCurrentSubEditor (valDateDur);
        value = null;
        valid = true;
      }
      if (valWeekDur.setValue (value)) {
        setCurrentSubEditor (valWeekDur);
        value = null;
        valid = true;
      }
      if (valToken.setValue (value)) {
        setCurrentSubEditor (valToken);
        value = null;
        valid = true;
      }
      if (! valid) {
        setCurrentSubEditor (valDate);
        // don't warn on empty string, only bad ones
        if (value != null && ! value.equals ("")) {
          System.err.println ("Timex2Editor.setTimeValue: " + 
			      "Unable to parse Val: '"+value+"'");
          Toolkit.getDefaultToolkit().beep();
        }
      }
      return valid;
    }
    
    /** Display the specified modifier or clear/reset if null. */
    boolean setModifier (String mod) {
      // set the dir value
      boolean valid = true;
      if (mod == null || mod.equals ("")) {
        modCombo.setSelectedIndex (0);
      } else {
        // since there are restrictions on what the modifier can be based on
        // the type of val, invalid modifiers make no change
        boolean found=false;
    
	// for now, check all three possible sets of values
	// one of these sets will be invalid, so this isn't quite
	// right, but should behave well on valid data at least
	// TODO fix this so ony valid values are considered
        for (int i=0; i<modPDValues.length; i++) {
          if (found = modPDValues[i].equals (mod)) {
            modCombo.setSelectedItem (mod);
            break;
          }
        }
	if (!found) {
	  for (int i=0; i<modPointValues.length; i++) {
	    if (DEBUG > 2)
	      System.err.println("compare " + mod + " to " + 
				 modPointValues[i]);
	    if (found = modPointValues[i].equals(mod)) {
	      modCombo.setSelectedItem(mod);
	      break;
	    }
	  }
	  if (!found) {
	    for (int i=0; i<modDurationValues.length; i++) {
	      if (found = modDurationValues[i].equals(mod)) {
		modCombo.setSelectedItem(mod);
		break;
	      }
	    }
	  }
	}
        if (!found) {
          System.err.println ("Unable to parse MOD: '"+mod+"'");
          Toolkit.getDefaultToolkit().beep();
	  // if unable to parse, unset, rather than using an old value!
	  modCombo.setSelectedIndex(0);
          valid = false;
        }
      }
      return valid;
    }

    String getTimeValue () {
      return currentSubEditor.getValue ();
    }

    String getModifier () {
      String s = (String)modCombo.getSelectedItem ();
      if ( !modCombo.isEnabled () || s.trim().equals(""))
        return null;
      return s;
    }

    void addTokenListener (ActionListener l) {
      valToken.addActionListener(l);
    }

    
    /**
     * Set the current TimeSubEditor, and modify the modCombo to match
     * it's type.  Also set highlighting of anchor pane according to
     * the requirements of the current TimeSubEditor.
     */
    protected void setCurrentSubEditor (TimeSubEditor editor) {
      //System.err.println("ValEditor.setCurrentSubEditor");
      TimeSubEditor prev = currentSubEditor;
      super.setCurrentSubEditor (editor);

      if (editor instanceof PointSubEditor) {
        if (! (prev instanceof PointSubEditor)) {
          modCombo.setEnabled (true);
          // remove duration items, add point items
          for (int i=0; i<modDurationValues.length; i++)
            modCombo.removeItem (modDurationValues[i]);
          for (int i=0; i<modPointValues.length; i++)
            modCombo.addItem (modPointValues[i]);
        }
      } else if (editor instanceof DurationSubEditor) {
        if (! (prev instanceof DurationSubEditor)) {
          modCombo.setEnabled (true);
          // remove point items, add duration items
          for (int i=0; i<modPointValues.length; i++)
            modCombo.removeItem (modPointValues[i]);
          for (int i=0; i<modDurationValues.length; i++)
            modCombo.addItem (modDurationValues[i]);
        }
      } else {
        modCombo.setEnabled (false);
        // remove both point and duration items
        for (int i=0; i<modPointValues.length; i++)
          modCombo.removeItem (modPointValues[i]);
        for (int i=0; i<modDurationValues.length; i++)
          modCombo.removeItem (modDurationValues[i]);
      }
      
      modCombo.validate ();

      // fire property change to handle highlighting in AnchorEditor
      firePropertyChange("subEditor", prev, editor);
      


    }

    public TimeSubEditor getCurrentSubEditor () {
      return super.getCurrentSubEditor();
    }

    /** set up the panel */
    private void init () {

      setBorder (BorderFactory.createTitledBorder ("VAL"));

      // handler for setting val for two components
      PropertyChangeListener dateChanger =
        new PropertyChangeListener () {
          public void propertyChange (PropertyChangeEvent e) {
            if (e.getPropertyName ().equals ("time")) {
              CalendarPanel calPanel = (CalendarPanel)e.getSource ();
              // only set date, since calendar can't specify time!
              valDate.setDate (calPanel.getTime ());
              valWeek.setDate (calPanel.getTime ());
            }
          }};
      
      // create subcomponents, and add changer so that settin date in one
      // modifies both.
      valDate = new PointSubEditor (true);
      valDate.addDateChangeListener (dateChanger);
      valWeek = new PointSubEditor (false);
      valWeek.addDateChangeListener (dateChanger);
      valDateDur = new DurationSubEditor ('P', true);
      valWeekDur = new DurationSubEditor ('P', false);
      valToken = new TokenEditor ();


      // create mod combo
      JPanel modPanel = new JPanel (new FlowLayout(FlowLayout.LEFT));
      JLabel modLabel = new EnableLabel("Modifier (MOD)");
      // see 'setCurrentSubEditor()' for how the combobox items are populated
      modCombo = new JComboBox ();
      for (int i=0; i<modPDValues.length; i++) // shared by point & duration
        modCombo.addItem (modPDValues[i]);
      modLabel.setLabelFor (modCombo);
      modPanel.add (modLabel);
      modPanel.add (modCombo);

      // create basic button panel
      buttonPanel = createButtonPanel ();
      
      int gridy = 0;
      addEditor ("Calendar Based", valDate, gridy++);
      addEditor ("Week Based", valWeek, gridy++);
      addEditor ("Calendar Based Duration", valDateDur, gridy++);
      addEditor ("Week/Token Based Duration", valWeekDur, gridy++);
      addEditor ("Token Only", valToken, gridy++);
    
      // some separation
      cons.fill=cons.HORIZONTAL;
      cons.gridx = 1;
      cons.gridy++;
      cons.gridwidth = 1;
      Component strut = Box.createVerticalStrut (10);
      gridBag.setConstraints (strut, cons);
      this.add (strut);

      cons.gridx = 0;
      cons.gridy++;
      cons.gridwidth=cons.RELATIVE;
      cons.fill=cons.HORIZONTAL;
      gridBag.setConstraints (modPanel, cons);
      this.add (modPanel);
      
      // add button panel created in TimeEditorPanel
      cons.gridx = 2;
      cons.gridwidth=1;
      cons.weightx=0;
      cons.fill=cons.NONE;
      gridBag.setConstraints (buttonPanel, cons);
      this.add (buttonPanel);
    }
  }
  
  /***********************************************************************/

  /**
   * Specific panel for editing ANCHOR_VAL and ANCHOR_DIR attributes.
   */
  private static class AnchorEditor extends TimeEditorPanel {

    // sub editors
    PointSubEditor anchorDate;
    PointSubEditor anchorWeek;

    JComboBox dirCombo;

    private final Color normalBackground = this.getBackground();


    /** passed in to the constructor, this is where we can copy a PointTime
     * value from */
    ValEditor valEditor;
    
    AnchorEditor (ValEditor vp) {
      valEditor = vp;
      init ();
      setCurrentSubEditor (anchorDate);
    }

    /** OVERRIDDEN to enable/disable certain components and labels too. */
    public void setEnabled (boolean enabled) {
      super.setEnabled (enabled);

      dirCombo.setEnabled (enabled);
    }

    /**
     * Reset the editor to display the specified value, or clear it if null
     */
    boolean setTimeValue (String anchorVal) {
      boolean valid = false;
      // All this makes sure that when an editor sees a valid string, the rest
      // are passed null to reset. finally, a warning is given if it's bad
      if (anchorDate.setValue (anchorVal)) {
        setCurrentSubEditor (anchorDate);
        anchorVal = null;
        valid = true;
      }
      if (anchorWeek.setValue (anchorVal)) {
        setCurrentSubEditor (anchorWeek);
        anchorVal = null;
        valid = true;
      }        
      if (! valid) {
        setCurrentSubEditor (anchorDate);
        // don't warn on empty string, only bad ones
        if (anchorVal != null && ! anchorVal.equals ("")) {
          System.err.println ("Unable to parse AnchorVal: '"+anchorVal+"'");
          Toolkit.getDefaultToolkit().beep();
        }
      }
      return valid;
    }
    
    boolean setModifier (String dir) {
      boolean valid = true;
      // set the dir value
      if (dir == null || dir.equals ("")) {
        dirCombo.setSelectedIndex (0);
      } else {
        boolean found=false;
        for (int i=0; i<anchorDirValues.length; i++) {
          if (found = anchorDirValues[i].equals (dir)) {
            dirCombo.setSelectedItem (dir);
            break;
          }
        }
        if (!found) {
          System.err.println ("Unable to parse AnchorDir: '"+dir+"'");
          Toolkit.getDefaultToolkit().beep();
          valid = false;
        }
      }
      return valid;
    }

    // TODO -- set to red if there is no value yet and one is expected
    // set to green if one is expected and present
    // set to neutral if none is expected, or if highlight == false
    void setHighlight (boolean highlight) {
      if (DEBUG > 1)
	System.err.println("AnchorEditor.setHighlight: " +
			   (highlight?"true":"false"));
      if (highlight) {
	// turn red highlighting on to indicate that anchor value and
	// direction are required
	setBackground(Color.RED);
      } else {
	// turn red highlighting off
	setBackground(normalBackground);
      }
    }
    
    String getTimeValue () {
      return currentSubEditor.getValue ();
    }

    String getModifier () {
      String s = (String)dirCombo.getSelectedItem ();
      if (s.trim().equals(""))
        return null;
      return s;
    }

    /*
    TimeSubEditor getValSubEditor() {
      return valEditor.getCurrentSubeditor();
      } */
    
    /** set up the panel */
    private void init () {

      setBorder (BorderFactory.createTitledBorder ("ANCHOR_VAL"));

      // handler for setting val for two components
      PropertyChangeListener dateChanger =
        new PropertyChangeListener () {
          public void propertyChange (PropertyChangeEvent e) {
            if (e.getPropertyName ().equals ("time")) {
              CalendarPanel calPanel = (CalendarPanel)e.getSource ();
              // only set date, since calendar can't specify time!
              anchorDate.setDate (calPanel.getTime ());
              anchorWeek.setDate (calPanel.getTime ());
            }
          }};

      // create subcomponents, and add changer so that settin date in one
      // modifies both.
      anchorDate = new PointSubEditor (true);
      anchorDate.addDateChangeListener (dateChanger);
      anchorWeek = new PointSubEditor (false);
      anchorWeek.addDateChangeListener (dateChanger);

      // create dir combo
      JPanel dirPanel = new JPanel (new FlowLayout(FlowLayout.LEFT));
      JLabel dirLabel = new EnableLabel("Direction (ANCHOR_DIR)");
      dirCombo = new JComboBox (anchorDirValues);
      dirLabel.setLabelFor (dirCombo);
      dirPanel.add (dirLabel);
      dirPanel.add (dirCombo);

      // create a button panel and add another button
      buttonPanel = createButtonPanel ();
      /** action to copy PointTime from valEditor (passed into constructor) */
      Action copyAction = new AbstractAction ("Copy VAL") {
          public void actionPerformed (ActionEvent e) {
            // retrieve date from enclosing Timex2Editor's instance variable
            // valEditor
            anchorDate.setValue (valEditor.valDate.getValue());
            anchorWeek.setValue (valEditor.valWeek.getValue());
          }};
      JButton copyVal = new JButton (copyAction);
      // add it to the front (left)
      buttonPanel.add (copyVal, 0);
      
      int gridy = 0;
      addEditor ("Calendar Based", anchorDate, gridy++);
      addEditor ("Week Based", anchorWeek, gridy++);
    
      // some separation
      cons.fill=cons.HORIZONTAL;
      cons.gridx = 1;
      cons.gridy++;
      cons.gridwidth = 1;
      Component strut = Box.createVerticalStrut (10);
      gridBag.setConstraints (strut, cons);
      this.add (strut);
      
      cons.gridx = 0;
      cons.gridy++;
      cons.gridwidth=cons.RELATIVE;
      cons.fill=cons.HORIZONTAL;
      gridBag.setConstraints (dirPanel, cons);
      this.add (dirPanel);

      // add button panel created in TimeEditorPanel
      cons.gridx = 2;
      cons.gridwidth=1;
      cons.weightx=0;
      cons.fill=cons.NONE;
      gridBag.setConstraints (buttonPanel, cons);
      this.add (buttonPanel);
    }
  }

  /***********************************************************************/

  /**
   * Superclass of editor subpanels which deals w/ component layout
   */
  private static abstract class TimeSubEditor extends JPanel {
    protected GridBagLayout bag;
    protected GridBagConstraints cons;

    protected TimeSubEditor () {
      // all subcomponents are enabled when this panel is enabled
      addPropertyChangeListener (new EnablePropogater ());
      
      bag = new GridBagLayout ();
      cons = new GridBagConstraints ();
      setLayout (bag);
      
      cons.fill = cons.HORIZONTAL;
      cons.anchor = cons.CENTER;
      cons.weightx = cons.weighty = 0;
    }
    
    /**
     * Adds a component at the specified x, y coords in the gridbag, anchored
     * centrally, filled horizontally, with 0 weight on either axis.
     */
    protected void addComponent (Component c, int gridx, int gridy) {
      cons.gridx = gridx;
      cons.gridy = gridy;
      bag.setConstraints (c, cons);
      this.add (c);
    }

    /**
     * Adds a component at the specified x, y coords in the gridbag,
     * anchored centrally, filled horizontally, with 0 weight on
     * either axis, with the specified gridwidth.
     */
    protected void addComponent (Component c, int gridx, int gridy, 
				 int gridwidth) {
      cons.gridx = gridx;
      cons.gridy = gridy;
      cons.gridwidth = gridwidth;
      bag.setConstraints (c, cons);
      this.add (c);
      cons.gridwidth = 1;
    }

    /** Parse and set the value, retuning true if successfull */
    abstract boolean setValue (String value);
    /** Return the string encoding of the editors value */
    abstract String getValue ();
  }

  /***********************************************************************/

  /**
   * Create a panel in which users can specify a date in standard ISO
   * format. there are two in the entire widget, each referring to a different
   * date. Each will have a button to trigger a popup
   */
  private static class PointSubEditor extends TimeSubEditor {

    // bunch of regex stuff
    static final String prefixR = "(FY|BC|KA|MA|GA)?";
    static final String yearR = "(-?[0-9X]+)";
    static final String monthR = "([0-9]{2}|H[12X]|Q[1234X]|WI|SP|SU|FA|XX)";
    static final String dayMonthR = "([0-9]{2}|WE|XX)";
    static final String weekR = "([0-9]{2}|XX)";
    static final String dayWeekR = "([1-7]|WE|X)";
    static final String hourR = "([0-9]{2}|AF|DT|EV|MI|MO|NI|XX)";
    static final String minR = "([0-9]{2}|XX)";
    static final String secR = "([0-9X\\.]+)";
    static final String zoneR = "([\\+\\-][0-9]{2}|Z)";
    // we don't want msR to have a gr.oup because we're not using a widget
    // for it, so no parens
    //    static final String msR = "[0-9]+";

    static final String datePointR =
      "(?:"+prefixR+"(?:"+yearR+"(?:-"+monthR+"(?:-"+dayMonthR+")?)?)?)?";
    static final String weekPointR = // note optional month, as per Timex2
      "(?:"+prefixR+"(?:"+yearR+"(?:-"+monthR+")?(?:-W"+weekR+"(?:-"+dayWeekR+
      ")?)?)?)?";
    static final String timePointR =
      "(?:T"+hourR+"(?::"+minR+"(?::"+secR+")?)?("+zoneR+
      ")?)?";

    // Both of these erroneously claim null strings are valid, but I can't see
    // it without duplicating the whole time block
    static final String dateRegex = datePointR + timePointR;
    static final String weekRegex = weekPointR + timePointR;
    
    boolean dateBased;
    
    // calendar/popup which is triggered by buttons in VAL section
    CalendarPanel popupPanel;
    PopupWindow popupWindow;

    // extract the correct value and format correctly for comboboxes.
    Calendar calendar;
    
    //SimpleDateFormat prefixFormat;
    SimpleDateFormat yearFormat;
    SimpleDateFormat monthFormat;
    SimpleDateFormat dayMonthFormat;
    SimpleDateFormat weekFormat;
    SimpleDateFormat hourFormat;
    SimpleDateFormat minFormat;
    SimpleDateFormat secFormat;
    // this dateFormat is simply for testing
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    // components of the date panel
    JTextField year;
    JComboBox prefix, month, dayMonth, week, dayWeek;
    JComboBox hour, min;
    JTextField sec;
    JComboBox zone;
    JComboBox[] boxes;
    JTextField[] fields;
    
    Matcher matcher;

    PointSubEditor (boolean dateBased) {
      this.dateBased = dateBased;
      // Set the calendar proper
      calendar = new GregorianCalendar ();
      calendar.setMinimalDaysInFirstWeek (4); // ISO 8601 (wrt weeks)
      calendar.setFirstDayOfWeek (Calendar.MONDAY); // ISO 8601 (first day)
      // create all the gui widgets
      init ();
      
      // specify calendar it to the formatters, and prepare regexes
      dateFormat = new SimpleDateFormat("yyyy-MM-dd|-'W'ww-F");
      dateFormat.setCalendar (calendar);
      
      yearFormat = new SimpleDateFormat("yyyy");
      yearFormat.setCalendar (calendar);
      if (dateBased) {
        monthFormat = new SimpleDateFormat("MM");
        monthFormat.setCalendar (calendar);
        dayMonthFormat = new SimpleDateFormat("dd");
        dayMonthFormat.setCalendar (calendar);
        
        matcher = Pattern.compile (dateRegex).matcher ("");
        
      } else { // weekBased
        weekFormat = new SimpleDateFormat("ww");
        weekFormat.setCalendar (calendar);
        // dayOfWeek maps ints to Names, but doesn't do the math of "day of
        // week from beginning of week"
        // I've submitted an RFE to developer.java.com
        
        matcher = Pattern.compile (weekRegex).matcher ("");
      }
      
      hourFormat = new SimpleDateFormat("HH");
      hourFormat.setCalendar (calendar);
      minFormat = new SimpleDateFormat("mm");
      minFormat.setCalendar (calendar);
      secFormat = new SimpleDateFormat("ss.SSS");
      secFormat.setCalendar (calendar);
    }

    /**
     * Sets the year, month, week and day widgets in the Val panel to display
     * the specified date
     */
    void setDate (Date d) {
      if (DEBUG > 0) {
	System.err.println ("Timex2Ed.PointSubEditor.setDate: " + d);
      }
      calendar.setTime (d);
      year.setText (yearFormat.format (d));
      
      if (dateBased) {
        month.setSelectedItem (monthFormat.format (d));
        dayMonth.setSelectedItem (dayMonthFormat.format (d));

      } else { // weekBased
        week.setSelectedItem (weekFormat.format (d));
        int dow = calendar.get(Calendar.DAY_OF_WEEK);
        int fdow = calendar.getFirstDayOfWeek();
        int val = ((dow-fdow+7)%7)+1;
        dayWeek.setSelectedItem (String.valueOf(val));
      }
      
      //System.err.println ("RTP.PtSubEd.setTime: "+dateFormat.format (d));
    }

    /**
     * Set the year, month, week, day, hour, min, and sec wigets to the
     * specified date.
     */
    void setTime (Date d) {
      setDate (d);
      hour.setSelectedItem (hourFormat.format (d));
      min.setSelectedItem (minFormat.format (d));
      //sec.setSelectedItem (secFormat.format (d));
      sec.setText(secFormat.format (d));
    }
    
    Date getTime () {
      return calendar.getTime();
    }

    void addDateChangeListener (PropertyChangeListener listener) {
      popupPanel.addPropertyChangeListener (listener);
    }

    private void validateValue () {

      // clean up seconds 
      String secStr = fields[1].getText().trim().toUpperCase();
      // allow only digits, X's and .'s (replace everything else with X)
      secStr = secStr.replaceAll("[^0-9X\\.]", "X");
      // remove second and subsequent .'s
      int j;
      if ( (j = secStr.indexOf(".")) >= 0) {
	secStr = secStr.substring(0,j+1) + 
	  secStr.substring(j+1).replaceAll("\\.", "");
      }
      fields[1].setText(secStr);

      // if sec is set, must set min
      //if (sec.getSelectedIndex () != 0 && min.getSelectedIndex () == 0)
      if (secStr.length() > 0 && min.getSelectedIndex () == 0)
        min.setSelectedItem ("XX");
      // if min is set, must set hour
      if (min.getSelectedIndex () != 0 && hour.getSelectedIndex () == 0)
        hour.setSelectedItem ("XX");
      
      // if day is set, must set month/week
      if (dateBased &&
          dayMonth.getSelectedIndex()!=0 && month.getSelectedIndex()==0) {
        month.setSelectedItem ("XX");
      } else if (! dateBased &&
                 dayWeek.getSelectedIndex()!=0 && week.getSelectedIndex()==0) {
        week.setSelectedItem ("XX");
      }
      
      // clean up year if neccissary
      String yearV = this.year.getText().trim().toUpperCase();
      boolean neg = false;
      int monthIdx;
      String monthV = null;
      if (yearV.length() > 0 && (neg = yearV.charAt (0) == '-') )
        yearV = yearV.substring (1);
      // allow for a month spec in weekBased (rare: use year field)
      if (! dateBased && (monthIdx = yearV.indexOf ('-')) > 0) {
        monthV = yearV.substring (monthIdx);
        yearV = yearV.substring (0,monthIdx);
      }
      // if month is set 4 year digits are required
      if (yearV.length () < 4 &&
          ((dateBased && month.getSelectedIndex()!=0) ||
           (!dateBased && week.getSelectedIndex()!=0)) ) {
        yearV = yearV + "XXXX".substring (yearV.length());
      }
      // all must be integers or X
      yearV = yearV.replaceAll ("[^0-9X]", "X");
      if (neg)
        yearV="-"+yearV;
      if (monthV != null) // only if !dateBased
        yearV += monthV;
      year.setText (yearV);
    }
    
    /** Return the string encoding of the editors value */
    String getValue () {
      validateValue ();
      
      StringBuffer sb = new StringBuffer ();
      if (prefix.getSelectedIndex () > 0)
	sb.append (prefix.getSelectedItem ());

      if (year.getText ().length() > 0)
        sb.append (year.getText());
      
      if (dateBased) {
        if (month.getSelectedIndex () > 0)
          sb.append ("-").append (month.getSelectedItem ());
        if (dayMonth.getSelectedIndex () > 0)
          sb.append ("-").append (dayMonth.getSelectedItem ());
        
      } else { // weekBased
        if (week.getSelectedIndex () > 0)
          sb.append ("-W").append (week.getSelectedItem ());
        if (dayWeek.getSelectedIndex () > 0)
          sb.append ("-").append (dayWeek.getSelectedItem ());
      }
      if (hour.getSelectedIndex () > 0)
        sb.append ("T").append (hour.getSelectedItem ());
      if (min.getSelectedIndex () > 0)
        sb.append (":").append (min.getSelectedItem ());
      //      if (sec.getSelectedIndex () > 0)
      if (sec.getText().length() > 0)
        sb.append (":").append (sec.getText ());
      if (zone.getSelectedIndex () > 0)
	sb.append (zone.getSelectedItem ());

      //System.err.println ("RTP.getValue: "+sb.toString ());
      if (sb.length() == 0)
        return null;
      return sb.toString();
    }
    
    /** Parse and set the value, retuning true if successfull */
    boolean setValue (String value) {
      if (DEBUG > 0) {
	System.err.println ("Timex2Ed.PointSubEditor.setValue: " + value);
	if (value== null) 
	  System.err.println("\tsetValue to null");
	else if (value.equals(""))
	  System.err.println("\tsetValue to \"\"");
      }
      
      // null is automatic reset, non matching will fall through to reset
      if (value != null) {
        // use the regex matcher to validate and parse the value
        matcher.reset (value);
        if (matcher.matches ()) {
          String tmp;
          String yearV;
          int box2GroupIndex = (dateBased ? 2 : 3);
	  
	  if (DEBUG > 2) {
	    for (int i=0; i<matcher.groupCount(); i++) {
	      System.err.println("Timex2Ed.PointSubEditor.SetValue: " +
				 "matcher group " + i + " = " +
				 matcher.group(i));
	    }
	  }

	  // plop prefix from regex into first ComboBox
	  if ( (tmp = matcher.group(1)) != null) {
	    boxes[0].setSelectedItem(tmp);
	  } else {
	    boxes[0].setSelectedIndex (0);
	  }
          
          if ( (yearV = matcher.group(2)) != null) {
            year.setText (yearV);
          
            // accordign to spec, month specifier is also allowed, (in week
            // based) but since rare, I'm just displaying it in the year field.
            // TODO: I think it's required to be _with_ the year, yes?
            if (! dateBased && (tmp = matcher.group(3)) != null) 
              year.setText (yearV+tmp);
            
          } else {
            year.setText ("");
          }
          
          // plop post-year groups from regex into ComboBoxes
	  // up through next to last box
	  int i;
          for (i=1; i<boxes.length-1; i++) {
            if ( (tmp = matcher.group(i+box2GroupIndex)) != null)
              boxes[i].setSelectedItem (tmp);
            else
              boxes[i].setSelectedIndex (0);
          }
	  
	  if ( (tmp = matcher.group(i+box2GroupIndex)) != null)
	    fields[1].setText (tmp);
	  else
	    fields[1].setText ("");

	  if ( (tmp = matcher.group(i+box2GroupIndex+1)) != null)
	    boxes[i].setSelectedItem (tmp);
	  else
	    boxes[i].setSelectedIndex (0);
	  
          return true;
        }
      }
      for (int i=0; i<fields.length; i++)
	fields[i].setText("");
      for (int i=0; i<boxes.length; i++)
        boxes[i].setSelectedIndex (0);
      return false;
    }
    
    private void init () {      
      int gridx = 0;
      int Y_EDIT = 0;
      int Y_TITLE = 1;
      // prefix (FY, BC, Geologic Eras)
      prefix = new JComboBox (prefixValues);
      addComponent (prefix, gridx, Y_EDIT);
      addComponent (new JLabel ("Prefix", JLabel.CENTER), gridx, Y_TITLE);

      // year
      year = new JTextField (4);
      year.setFont(year.getFont().deriveFont (Font.BOLD));
      addComponent (year, ++gridx,Y_EDIT);
      addComponent (new JLabel ("Year", JLabel.CENTER), gridx,Y_TITLE);
      addComponent (new JLabel ("-", JLabel.CENTER), ++gridx,Y_EDIT);

      if (dateBased) {
        // month
        month = new JComboBox (monthValues);
        addComponent (month, ++gridx,Y_EDIT);
        addComponent (new JLabel ("Month", JLabel.CENTER), gridx,Y_TITLE);
        addComponent (new JLabel ("-", JLabel.CENTER), ++gridx,Y_EDIT);
        
        // day
        dayMonth = new JComboBox (mDayValues);
        addComponent (dayMonth, ++gridx,Y_EDIT);
        addComponent (new JLabel ("Day", JLabel.CENTER), gridx,Y_TITLE);
        addComponent (new JLabel (" ", JLabel.CENTER), ++gridx,Y_EDIT);

      } else { // weekBased
        // week
        week = new JComboBox (weekValues);
        addComponent (week, ++gridx, Y_EDIT);
        addComponent (new JLabel ("Week", JLabel.CENTER), gridx,Y_TITLE);
        addComponent (new JLabel ("-", JLabel.CENTER), ++gridx,Y_EDIT);
        
        // day
        dayWeek = new JComboBox (wDayValues);
        addComponent (dayWeek, ++gridx, Y_EDIT);
        addComponent (new JLabel ("Day", JLabel.CENTER), gridx,Y_TITLE);
        addComponent (new JLabel (" ", JLabel.CENTER), ++gridx,Y_EDIT);
      }

      // add only a listener to hide the popup, another will be added which
      // will be the same one added to the PointSubEditor and do other stuff
      // too
      popupPanel = new CalendarPanel (calendar);
      popupWindow = new PopupWindow (this);
      popupPanel.setBorder (new EtchedBorder(EtchedBorder.LOWERED));
      popupWindow.add (popupPanel);
    
      final JButton calButton = new JButton ("Calendar...");
      calButton.addActionListener (new ActionListener () {
          public void actionPerformed (ActionEvent e) {
            popupPanel.setTime (getTime());
            popupWindow.show(calButton, 0, calButton.getHeight());
          }
        });
      popupPanel.addPropertyChangeListener (new PropertyChangeListener () {
          public void propertyChange (PropertyChangeEvent e) {
            if (e.getPropertyName ().equals ("time")) {
              popupWindow.hide();
	      requestFocus();
            }
          }
        });
      addComponent (calButton, ++gridx,Y_EDIT);

      // - T -
      addComponent (new JLabel (" T ", JLabel.CENTER), ++gridx,Y_EDIT);

      // hour
      hour = new JComboBox (hourValues);
      addComponent (hour, ++gridx,Y_EDIT);
      addComponent (new JLabel ("Hour", JLabel.CENTER), gridx,Y_TITLE);
      addComponent (new JLabel (":", JLabel.CENTER), ++gridx,Y_EDIT);
      
      // min
      min = new JComboBox (minSecValues);
      addComponent (min, ++gridx,Y_EDIT);
      addComponent (new JLabel ("Min", JLabel.CENTER), gridx,Y_TITLE);
      addComponent (new JLabel (":", JLabel.CENTER), ++gridx,Y_EDIT);
      
      // sec
      sec = new JTextField (3);
      addComponent (sec, ++gridx,Y_EDIT);
      addComponent (new JLabel ("Sec", JLabel.CENTER), gridx,Y_TITLE);

      // time zone
      zone = new JComboBox (timezoneValues);
      ZoneRenderer renderer = new ZoneRenderer();
      renderer.setPreferredSize((new JLabel(longestZoneString)).
      				getPreferredSize());
      //renderer.setPreferredSize(new Dimension(200,40));
      zone.setRenderer(renderer);
      addComponent (zone, ++gridx, Y_EDIT);
      addComponent (new JLabel ("Zone", JLabel.CENTER), gridx, Y_TITLE);

      if (dateBased)
        boxes = new JComboBox[] {prefix,month,dayMonth,hour,min,zone};
      else
        boxes = new JComboBox[] {prefix,week,dayWeek,hour,min,zone};
      fields = new JTextField[] {year,sec};
    }

    private static class ZoneRenderer extends JLabel 
      implements ListCellRenderer {
      
      public ZoneRenderer () {
        setOpaque(true);
	setHorizontalAlignment(LEFT);
	setVerticalAlignment(CENTER);
      }
      
      public Component getListCellRendererComponent(JList list,
						    Object value,
						    int index,
						    boolean isSelected,
						    boolean cellHasFocus) {
	
	if (isSelected) {
	  setBackground(list.getSelectionBackground());
	  setForeground(list.getSelectionForeground());
	} else {
	  setBackground(list.getBackground());
	  setForeground(list.getForeground());
	}
	if (index < 0) {
	  index = list.getNextMatch(value.toString(),0,
				    javax.swing.text.Position.Bias.Forward);
	}
	setText(zoneStrings[index]);

	return this;
      }
      
    }
  }
  
  /***********************************************************************/
  
  /**
   * This sub editor handles durations, either calendar based (with
   * many substring qualifiers) or week/token based.
   */
  private static class DurationSubEditor extends TimeSubEditor {

    // bunch of regex stuff
    static final String yearDurP =
      // "(?:([0-9X\\.]+)L)?(?:([0-9X\\.]+)C)?(?:([0-9X\\.]+)E)?(?:([0-9X\\.]+)(?:(Y|FY)))?";
      "(?:([0-9X\\.]+)(?:(Y|FY)))?";
    static final String dateDurP =
      "(?:([0-9X\\.]+)M)?(?:([0-9X\\.]+)D)?";
    static final String weekDurP =
      "(?:([0-9X\\.]+))(W|DE|CE|ML|SP|SU|FA|WI|Q[1234X]|H[12X]|WE|MO|MI|AF|EV|NI|DT)";
    static final String timeDurP =
      "(?:T(?:([0-9X\\.]+)H)?(?:([0-9X\\.]+)M)?(?:(([0-9X\\.]+))S)?)?";

    static final String dateRegex = yearDurP + dateDurP + timeDurP;
    static final String weekRegex = weekDurP;
    
    char prefix;
    boolean dateBased;

    JLabel prefixLabel = new JLabel ("  ", JLabel.CENTER);
    
    // JTextField lDur, cDur, eDur;
    JTextField yDur;
    JComboBox yDurType;
    JTextField moDur, dDur;
    JTextField hDur, mnDur, sDur, wDur;
    JComboBox wDurType;
    JComboBox[] boxes;
    JTextField[] fields;
    
    Matcher matcher;

    DurationSubEditor (char prefix, boolean dateBased) {
      this.dateBased = dateBased;
      init ();
      // must come post
      setPrefix (prefix);
    }

    final void setPrefix (char prefix) {
      this.prefix = prefix;
      String regex = prefix + (dateBased ? dateRegex : weekRegex);
      matcher = Pattern.compile (regex).matcher ("");

      prefixLabel.setText (prefix+" ");
      prefixLabel.validate ();
    }

    char getPrefix () {
      return prefix;
    }

    private void validateValue() {
      if (DEBUG > 0) 
	System.err.println ("T2Editor.DSE.validateValue()");

      String tmp;
      int j;
      for (int i=0; i<fields.length; i++) {
	tmp = fields[i].getText().trim().toUpperCase();
	// allow only digits, X's and .'s (replace everything else with X)
	tmp = tmp.replaceAll("[^0-9X\\.]", "X");
	// remove second and subsequent .'s
	if ( (j = tmp.indexOf(".")) >= 0) {
	  tmp = tmp.substring(0,j+1) + 
	    tmp.substring(j+1).replaceAll("\\.", "");
	}
	// replace multiple X's with single X
	tmp = tmp.replaceAll("XX+", "X");
	// set the field with the newly validated text
	fields[i].setText(tmp);
      }
    }

    /** Return the string encoding of the editors value */
    String getValue () {
      // validation will just clean up the strings input and make
      // sure they are numeric (and eventually allow a decimal)
      validateValue();

      StringBuffer sb = new StringBuffer ();
      if (dateBased) {
	/* L, C, and E have been replaced by
	 * ML, CE, and DE in the token-based section 
        if (lDur.getText().length() > 0)
          sb.append (lDur.getText()).append ("L");
        if (cDur.getText().length () > 0)
          sb.append (cDur.getText ()).append ("C");
        if (eDur.getText().length () > 0)
	  sb.append (eDur.getText ()).append ("E"); */
        if (yDur.getText().length () > 0)
          sb.append (yDur.getText ()).append 
	    (yDurType.getSelectedItem());
        if (moDur.getText().length () > 0)
          sb.append (moDur.getText ()).append ("M");
        if (dDur.getText().length () > 0)
          sb.append (dDur.getText ()).append ("D");
        
        if (hDur.getText().length () +
            mnDur.getText().length () +
            sDur.getText().length ()  > 0)
          sb.append ("T");
        
        if (hDur.getText().length () > 0)
          sb.append (hDur.getText ()).append ("H");
        if (mnDur.getText().length () > 0)
          sb.append (mnDur.getText ()).append ("M");
        if (sDur.getText().length () > 0)
          sb.append (sDur.getText ()).append ("S");
        
      } else { // week or season Based
        if (wDur.getText().length () > 0)
          sb.append (wDur.getText ()).append 
	    (wDurType.getSelectedItem());
      }
      
      if (sb.length() == 0)
        return null;

      // insert the prefix at the beginning
      sb.insert (0, prefix);
      return sb.toString();
    }
    
    /** Parse and set the value, retuning true if successfull */
    // TODO: complete the DateEditor parser
    boolean setValue (String value) {
      if (DEBUG > 0)
	System.err.println("T2Editor.DSE.setValue: " + value);
      
      // null is automatic reset, non matching will fall through to reset
      if (value != null) {
        // use the regex matcher to parse the value
        matcher.reset (value);
	String tmp;
        if (matcher.matches ()) {
	  if (fields.length > 1) {
	    // for calendar based 
	    // first  matcher group is a field
	    if ( (tmp = matcher.group(1)) != null)
	      fields[0].setText (tmp);
	    else
	      fields[0].setText("");
	  
	    // Second matcher item is year type (boxes[0])
	    if ( (tmp = matcher.group(2)) != null)
	      boxes[0].setSelectedItem (tmp);
	    else
	      boxes[0].setSelectedIndex (0);

	    // remaining matcher items are fields
	    for (int i=3; i<fields.length+2; i++) {
	      if ( (tmp = matcher.group(i)) != null)
		fields[i-2].setText(tmp);
	      else 
		fields[i-2].setText("");
	    }
	  } else {
	    // for week or token based, first matcher group is 
	    // a field, and second is a box
	    if ( (tmp = matcher.group(1)) != null)
	      fields[0].setText (tmp);
	    else
	      fields[0].setText ("");
	    if ( (tmp = matcher.group(2)) != null)
	      boxes[0].setSelectedItem (tmp);
	    else 
	      boxes[0].setSelectedIndex (0);
	  }
	      
          return true;
        }
      }
      //reset
      if (DEBUG > 0) {
	System.err.println 
	  ("T2Editor.DSE.SetValue: Resetting Duration Fields");
      }
      for (int i=0; i<boxes.length; i++)
        boxes[i].setSelectedIndex (0);
      for (int i=0; i<fields.length; i++)
	fields[i].setText("");
      return false;
    }
    
    private void init () {
      int gridx = 0;
      int Y_EDIT = 0;
      int Y_TITLE = 1;

      addComponent (prefixLabel, ++gridx,Y_EDIT);

      if (dateBased) {
	/*
        // L (millennia)
        lDur = new JTextField (3);
        addComponent (lDur, ++gridx, Y_EDIT);
        addComponent (new JLabel ("Mill", JLabel.CENTER), gridx,Y_TITLE);
        addComponent (new JLabel ("L ", JLabel.CENTER), ++gridx,Y_EDIT);
        
        // C (centuries)
        cDur = new JTextField (3);
        addComponent (cDur, ++gridx, Y_EDIT);
        addComponent (new JLabel ("Cent", JLabel.CENTER), gridx,Y_TITLE);
        addComponent (new JLabel ("C ", JLabel.CENTER), ++gridx,Y_EDIT);
        
        // E (decades)
        eDur = new JTextField (3);
        addComponent (eDur, ++gridx, Y_EDIT);
        addComponent (new JLabel ("Dec", JLabel.CENTER), gridx,Y_TITLE);
        addComponent (new JLabel ("E ", JLabel.CENTER), ++gridx,Y_EDIT);
	*/
        
        // Y (years)
        yDur = new JTextField (3);
        addComponent (yDur, ++gridx, Y_EDIT);
	yDurType = new JComboBox (durationYTypeValues);
	addComponent (yDurType, ++gridx, Y_EDIT);
        addComponent (new JLabel ("(Fiscal) Year", JLabel.CENTER), 
		      (gridx-1), Y_TITLE, 2);
        
        // M (months)
        moDur = new JTextField (3);
        addComponent (moDur, ++gridx, Y_EDIT);
        addComponent (new JLabel ("Month", JLabel.CENTER), gridx,Y_TITLE);
        addComponent (new JLabel ("M ", JLabel.CENTER), ++gridx,Y_EDIT);
        
        // D (days)
        dDur = new JTextField (3);
        addComponent (dDur, ++gridx, Y_EDIT);
        addComponent (new JLabel ("Day", JLabel.CENTER), gridx,Y_TITLE);
        addComponent (new JLabel ("D ", JLabel.CENTER), ++gridx,Y_EDIT);

        // - T -
        addComponent (new JLabel ("T ", JLabel.CENTER), ++gridx,Y_EDIT);
        
        // H (hours)
        hDur = new JTextField (3);
        addComponent (hDur, ++gridx, Y_EDIT);
        addComponent (new JLabel ("Hour", JLabel.CENTER), gridx,Y_TITLE);
        addComponent (new JLabel ("H ", JLabel.CENTER), ++gridx,Y_EDIT);
        
        // M (minutes)
        mnDur = new JTextField (3);
        addComponent (mnDur, ++gridx, Y_EDIT);
        addComponent (new JLabel ("Min", JLabel.CENTER), gridx,Y_TITLE);
        addComponent (new JLabel ("M ", JLabel.CENTER), ++gridx,Y_EDIT);
        
        // S (seconds)
        sDur = new JTextField (3);
        addComponent (sDur, ++gridx, Y_EDIT);
        addComponent (new JLabel ("Sec", JLabel.CENTER), gridx,Y_TITLE);
        addComponent (new JLabel ("S ", JLabel.CENTER), ++gridx,Y_EDIT);
        
        boxes =
          new JComboBox[] {yDurType};
	fields =
	  new JTextField[] {yDur,moDur,dDur,hDur,mnDur,sDur};
        
      } else { // week/season Based
        // W (weeks)
        wDur = new JTextField (3);
        addComponent (wDur, ++gridx, Y_EDIT);
        addComponent (new JLabel ("Week/Token", JLabel.CENTER), 
		      gridx,Y_TITLE,2);
	wDurType = new JComboBox (durWTypeValues);
        addComponent (wDurType, ++gridx,Y_EDIT);
        
        boxes = new JComboBox[] {wDurType};
	fields = new JTextField[] {wDur};
      }
    }
  }

  /***********************************************************************/

  /**
   * Chincy TimeSubEditor which simply wrappers a JComboBox. Making this a
   * subclass of TimeSubEditor allows easier swapping of components in the
   * ValEditor.
   */
  private static class TokenEditor extends TimeSubEditor {
    
    JComboBox token;
    
    /** duplication of array in Timex2Editor, usefull for validation */
    HashSet valTokens;

    TokenEditor () {
      init ();
      valTokens = new HashSet (Arrays.asList(tokenValues));
    }
    
    boolean setValue (String value) {
      if (value != null && valTokens.contains (value)) {
        token.setSelectedItem (value);
        return true;
      }
      token.setSelectedIndex (0);
      return false;
    }
    String getValue () {
      String s = ((String)token.getSelectedItem ()).trim();
      if (s.equals (""))
        s = null;
      return s;
    }
    
    void addActionListener (ActionListener l) {
      token.addActionListener(l);
    }

    private void init () {
      token = new JComboBox (tokenValues);
      // ignoreing all the GridPanel goodness from TimeSubEditor
      add (token);
    }
  }

  /**
   * listens for changes to the Token, the NonSpecific checkbox,
   * and is called when the value subeditor is changed
   *
   * TODO -- need to also listen for the anchor value getting set or unset
   */
  private class AnchorRequiredListener 
    implements ActionListener, ChangeListener, PropertyChangeListener {

    private AnchorEditor theAnchorEditor;
    private ValEditor theValEditor;
    private JCheckBox theNonSpecificCheck;

    public AnchorRequiredListener (AnchorEditor anchor,
				   ValEditor val,
				   JCheckBox ns) {
      theAnchorEditor = anchor;
      theValEditor = val;
      theNonSpecificCheck = ns;
    }

    /* 
     * listens for an ActionEvent on the token comboBox
     */
    public void actionPerformed (ActionEvent e) {
      if (DEBUG > 1)
	System.err.println("AnchorRequiredListener.actionPerformed");
      JComboBox token = (JComboBox)(e.getSource());
      // if FUTURE_REF or PAST_REF is chosen, set
      // ANCHOR_DIR accordingly
      // TODO -- make this less hardcoded
      String selToken = (String)(token.getSelectedItem());
      if (DEBUG > 1)
	System.err.println("TL.ap: selected token = <<" + 
			   selToken + ">>");
      if (selToken.equals("FUTURE_REF")) {
	if (DEBUG > 1)
	  System.err.println("TL.ap: found FUTURE_REF");
	theAnchorEditor.setModifier ("AFTER");
      } else if (selToken.equals("PAST_REF")) {
	theAnchorEditor.setModifier ("BEFORE");
      } else if (selToken.equals("PRESENT_REF")) {
	theAnchorEditor.setModifier ("AS_OF");
      }
      // if this is enabled, it must be the current subEditor
      // set highlighting accordingly
      // TODO -- I think this is now redundant
      // theAnchorEditor.setHighlight (token.isEnabled());
    }

    /* 
     * listens for a ChangeEvent on the nonSpecific checkbox
     */
    public void stateChanged (ChangeEvent e) {
      if (DEBUG > 1)
	System.err.println("AnchorRequiredListener.stateChanged");
      TimeSubEditor vse = theValEditor.getCurrentSubEditor();
      // ignore changes to nonSpecific if ValSubEditor is Token
      if (vse instanceof TokenEditor) 
	return;
      // highlight if not nonSpecific if ValSubEditor is a duration
      if (vse instanceof DurationSubEditor) {
	if (DEBUG > 1) 
	  System.err.println("\tcurrent subeditor is a DurationSubEditor");
	theAnchorEditor.setHighlight(!theNonSpecificCheck.isSelected());
      }
    }

    /* 
     * listens for the PropertyChangeEvent fired by the valEditor when
     * the current subeditor changes
     */
    public void propertyChange (PropertyChangeEvent e) {
      // only deal with subEditor property changes
      if (!e.getPropertyName().equals("subEditor"))
	return;

      Object theEditor = e.getNewValue();
      boolean token = (theEditor instanceof TokenEditor);
      if (token) {
	TokenEditor theTokenEditor = (TokenEditor)theEditor;
	// reset the value to the current value to trigger an
	// ActionPerformed event on the combobox to set the anchordir
	// appropriately
	theTokenEditor.setValue(theTokenEditor.getValue());
      }
      boolean duration = (theEditor instanceof DurationSubEditor);
      boolean specific = !theNonSpecificCheck.isSelected();
      if (DEBUG > 1)
	System.err.println("AnchorRequiredListener.propertyChange:" +
			   " name: subEditor" + 
			   " token: " + (token?"yes":"no") +
			   " duration: " + (duration?"yes":"no") +
			   " specific: " + (specific?"yes":"no"));
      theAnchorEditor.setHighlight(token || (duration && specific));
    }

  }


  /***********************************************************************/

  private static class SetEditor extends JPanel {
    final static String UNSET_VALUE = "...";
    JCheckBox setCheck;
    
    // propertychange action needs to know which one was edited
    JButton lastButton;
    
    //    Component[] components;
    
    SetEditor () {
      init ();
    }

    void setValue (String value) {
      if (value != null && value.equals("YES"))
        setCheck.setSelected (true);
      else
        setCheck.setSelected (false);
    }

    String getValue () {
      if (setCheck.isSelected ())
        return "YES";
      return null;
    }

    boolean isSelected () {
      return setCheck.isSelected ();
    }

    void setSelected (boolean set) {
      setCheck.setSelected (set);
    }

    
    /**
     * overridden to enable/disable the contained components and labels too.
     */
    public void setEnabled (boolean enabled) {
      super.setEnabled (enabled);
      setCheck.setEnabled (enabled);
    }

    private void init () {
      setBorder (BorderFactory.createTitledBorder ("SET"));

      setCheck = new JCheckBox ("Set");
      
      this.add (setCheck);
      
    }
  }

  
  /***********************************************************************/

  /**
   * Propogate 'enabled' state of a Container, to all the Components within,
   * whenever the Containers 'enabled' state changes.
   */
  public static class EnablePropogater implements PropertyChangeListener { 
    public void propertyChange (PropertyChangeEvent e) {
      if (e.getPropertyName () == "enabled") {
        Object source = e.getSource ();
        boolean enabled = ((Boolean)e.getNewValue()).booleanValue();

        if (source instanceof Container) {
          Component[] components = ((Container)source).getComponents ();
          for (int i=0; i<components.length; i++) {
            components[i].setEnabled (enabled);
          }
        }
      }
    }// propertyChange
  }
  
  /***********************************************************************/

  /**
   * Add to an item to link it's 'selected' state to the 'enabled' state of a
   * specific component
   */
  public static class EnableAction  implements ItemListener {
    Component comp;
    boolean deselect;
    
    EnableAction (Component component) {
      this (component, true);
    }
    
    /**
     * components are enabled when item listened to is selected, if 'selected'
     * is true, else they are enabled when item is deselected.
     */
    EnableAction (Component component, boolean selected) {
      comp = component;
      deselect = !selected;
    }
    
    public void itemStateChanged (ItemEvent e) {
      comp.setEnabled (deselect ^ e.getStateChange () == ItemEvent.SELECTED);
    }
  }
  
  /***********************************************************************/

  /**
   * I'm not to keen on tying this _only_ to ATLAS work so here's the
   * 'shuttle' to get data in and out of here without passing AWBAnnotations.
   */
  public static class Timex2Data {

    public String text;
    
    public String val;
    public String mod;
    
    public String set;
    
    public String nonSpecific;
    
    public String anchorVal;
    public String anchorDir;
    public String comment;
    
    public void clear () {
      if (DEBUG > 0)
	System.err.println("Timex2Data.clear()");

      text = val = mod =
        set = 
        nonSpecific = anchorVal = anchorDir =
        comment = null;
    }
    /** debugging */
    public String toString () {
      
      StringBuffer sb = new StringBuffer ();
      String tmp = null;
      sb.append ("<TIMEX2");
      
      // VAL, MOD
      tmp = val;
      if (tmp != null && ! tmp.equals ("")) {
        sb.append (" VAL=\"").append (tmp).append ("\"");
        tmp = mod;
        if (tmp != null)
          sb.append (" MOD=\"").append (tmp).append ("\"");
      }
      
      // SET, etc
      if (DEBUG > 0) 
	System.err.println("SET is " + set);
      tmp = set;
      if (tmp != null && ! tmp.equals ("")) {
        sb.append (" SET=\"YES\"");
      }
      
      // NON_SPECIFIC
      tmp = nonSpecific;
      if (tmp != null && ! tmp.equals (""))
        sb.append (" NON_SPECIFIC=\"YES\"");
      
      // ANCHOR_VAL, ANCHOR_DIR
      tmp = anchorVal;
      if (tmp != null && ! tmp.equals ("")) {
        sb.append (" ANCHOR_VAL=\"").append (tmp).append ("\"");
        tmp = anchorDir;
        if (tmp != null)
          sb.append (" ANCHOR_DIR=\"").append (tmp).append ("\"");
      }
      
      // COMMENT
      tmp = comment;
      if (tmp != null && ! tmp.equals (""))
        sb.append (" COMMENT=\"").append (tmp).append ("\"");

      // TEXT
      sb.append (">").append (text).append ("</TIMEX2>");
      
      return sb.toString ();
    }
  } // Timex2Data

  
}// Timex2Editor


/*
 * Class which builds a timex2 dialog consisting of a Timex2Editor with "Ok",
 * "Cancel", and "Reset" buttons. This is a modified version of what's in
 * JColorChooser java1.4
 *
 * Note: This needs to be fixed to deal with localization!
 */
class Timex2EditorDialog extends JDialog {
    private Timex2Editor.Timex2Data initialTimex2;
    private Timex2Editor.Timex2Data clearTimex2 = 
      new Timex2Editor.Timex2Data(); // assume it starts out empty??
    private Timex2Editor editorPane;

    public Timex2EditorDialog(Component c, String title, boolean modal,
                               Timex2Editor editorPane,
                               ActionListener okListener,
                               ActionListener cancelListener)
        throws HeadlessException {
        super(JOptionPane.getFrameForComponent(c), title, modal);
        //setResizable(false);

        this.editorPane = editorPane;

	String okString = "OK";//UIManager.getString("ColorChooser.okText");
	String cancelString = "Cancel";// UIManager.getString("ColorChooser.cancelText");
	String resetString = "Reset";//UIManager.getString("ColorChooser.resetText");
	String clearString = "Clear Form";
	
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(editorPane, BorderLayout.CENTER);

        /*
         * Create Lower button panel
         */
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton(okString);
	getRootPane().setDefaultButton(okButton);
        okButton.setActionCommand("OK");
        if (okListener != null) {
            okButton.addActionListener(okListener);
        }
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        buttonPane.add(okButton);

        JButton cancelButton = new JButton(cancelString);

	// The following few lines are used to register esc to close the dialog
	Action cancelKeyAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ((AbstractButton)e.getSource()).doClick();
            }
        }; 
	KeyStroke cancelKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0);
	InputMap inputMap = cancelButton.getInputMap(JComponent.
						     WHEN_IN_FOCUSED_WINDOW);
	ActionMap actionMap = cancelButton.getActionMap();
	if (inputMap != null && actionMap != null) {
	    inputMap.put(cancelKeyStroke, "cancel");
	    actionMap.put("cancel", cancelKeyAction);
	}
	// end esc handling

        cancelButton.setActionCommand("cancel");
        if (cancelListener != null) {
            cancelButton.addActionListener(cancelListener);
        }
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        buttonPane.add(cancelButton);

        JButton resetButton = new JButton(resetString);
        resetButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               reset();
           }
        });
        buttonPane.add(resetButton);

	JButton clearButton = new JButton (clearString);
	clearButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
	      clearForm();
	    }
	  });
	buttonPane.add(clearButton);

        contentPane.add(buttonPane, BorderLayout.SOUTH);

        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations = 
            UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations) {
                getRootPane().setWindowDecorationStyle(JRootPane.COLOR_CHOOSER_DIALOG);
            }
        }
        applyComponentOrientation(((c == null) ? getRootPane() : c).getComponentOrientation());

        pack();
        setLocationRelativeTo(c);
    }

    public void show() {
        initialTimex2 = editorPane.getTimex2();
	// set the text string for the "clear timex2" data
	clearTimex2.text = initialTimex2.text;
        super.show();
    }

    public void reset() {
        editorPane.setTimex2(initialTimex2);
    }

  public void clearForm() {
    editorPane.setTimex2(clearTimex2);
  }

}

