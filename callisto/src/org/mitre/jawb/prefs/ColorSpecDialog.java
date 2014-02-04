
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

package org.mitre.jawb.prefs;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * ColorSpec editing dialog.
 */
public class ColorSpecDialog extends JDialog {

  /**
   * Create a dialog from components other than the parent frame.
   *
   * @param comp The componenet requesting the dialog.
   *
   * @return A new ColorSpecDialog.
   */
  public static ColorSpecDialog createDialog(Component comp) {
    while ((comp!=null)&&(!(comp instanceof Frame))) {
      comp = comp.getParent();
    }
    if ((comp!=null)&&(comp instanceof Frame)) {
      return new ColorSpecDialog((Frame)comp);
    }
    return new ColorSpecDialog(null);
  }

  /**
   * The working ColorSpec.
   */
  private ColorSpec colorSpec = new ColorSpec();
  /**
   * The original ColorSpec.  Used for cancel and revert operations.
   */
  private ColorSpec original = new ColorSpec();
  /**
   * The label for window titles.  This should be set to the property name
   * being edited.
   */
  private String label = null;
  /**
   * The background editor button.
   */
  private JButton bgButton = null;
  /**
   * The foreground editor button.
   */
  private JButton fgButton = null;
  /**
   * Display color next to 'setForground' button
   */
  private ColorSwatch fgSwatch = null;
  /**
   * Display color next to 'setBackground' button
   */
  private ColorSwatch bgSwatch = null;
  /**
   * The preview component.
   */
  //private JLabel previewButton = null;
  private JTextPane preview = null;
  private Style previewStyle;
  private static final String PREVIEW_STRING = "This is a Sample of your tag.";
  private static final int PREVIEW_START = 10;
  private static final int PREVIEW_LENGTH = 6;
  /**
   * The okay button.
   */
  private JButton okayButton = null;
  /**
   * The revert button.
   */
  private JButton revertButton = null;
  /**
   * The cancel button.
   */
  private JButton cancelButton = null;   
  /**
   * The okay action.
   */
  private Action okAction = null;
  /**
   * The okay action.
   */
  private Action revertAction = null;
  /**
   * The cancel action.
   */
  private Action cancelAction = null;
  /**
   * Listener for the buttons, inaccessable outside the class.
   */
  private ActionListener buttonListener = new ButtonListener ();
  
  /**
   * Constructor.
   * Constructs the modal ColorSpec editing dialog.
   */
  public ColorSpecDialog (Frame owner) {
    super (owner, "ColorSpec Editor", true);
    initActions();
    JPanel cp = new JPanel (new BorderLayout(0,17));
    cp.add(BorderLayout.CENTER, getColorPanel());
    cp.add(BorderLayout.SOUTH, getControlPanel());
    cp.setBorder (BorderFactory.createEmptyBorder (12,12,11,11));
    setContentPane (cp);
    pack ();
    //setSize(new Dimension(400,150));
    setLocationRelativeTo(owner);
    addWindowListener(new WindowAdapter () {
        /** Closing is treated like pressing the cancel button. */
        public void windowClosing(WindowEvent wevt) {
          cancel();
        }
      });
  }
  
  /**
   * Override show to set the default button each time.
   */
  public void show() {
    getRootPane().setDefaultButton(okayButton);
    super.show();
  }

  /** Delegats to actions below */
  private void initActions () {
    okAction = new AbstractAction ("ok") {
        public void actionPerformed (ActionEvent e) {
          ok();
        }};
    revertAction = new AbstractAction ("revert") {
        public void actionPerformed (ActionEvent e) {
          revert();
        }};
    cancelAction = new AbstractAction ("cancel") {
        public void actionPerformed (ActionEvent e) {
          cancel();
        }};
    JPanel cp = (JPanel)getContentPane();
    ActionMap actionMap = cp.getActionMap ();
    actionMap.put ("ok", okAction);
    actionMap.put ("cancel", cancelAction);

    InputMap inputMap = cp.getInputMap (cp.WHEN_IN_FOCUSED_WINDOW);
    inputMap.put (KeyStroke.getKeyStroke (KeyEvent.VK_ENTER,0), "ok");
    inputMap.put (KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE,0),"cancel");
  }

  /**
   * Get the current working ColorSpec.
   *
   * @return The current working ColorSpec.
   */
  public ColorSpec getColorSpec() {
    return colorSpec;
  }

  /**
   * Set the original and current working ColorSpec.  Should be used to
   * initialize the dialog before showing it.
   *
   * @param colorSpec The initial ColorSpec object.
   */
  public void setColorSpec(ColorSpec colorSpec) {
    colorSpec = (colorSpec!=null)?colorSpec:new ColorSpec();
    original = colorSpec;
    this.colorSpec = new ColorSpec();
    this.colorSpec.setBackground(original.getBackground());
    this.colorSpec.setForeground(original.getForeground());
    setWorkingColors(colorSpec);
  }

  /**
   * Method to set the foreground and background colors of the preview
   * accordingly.
   *
   * @param working The desired working ColorSpec.
   */
  private void setWorkingColors(ColorSpec working) {
    working = (working!=null)?working:new ColorSpec();
    Color bgColor = working.getBackground();
    Color fgColor = working.getForeground();
    bgSwatch.setColor(bgColor);
    fgSwatch.setColor(fgColor);
    bgButton.repaint ();
    fgButton.repaint ();
    StyleConstants.setBackground (previewStyle,bgColor);
    StyleConstants.setForeground (previewStyle,fgColor);
    StyledDocument doc = preview.getStyledDocument();
    doc.setCharacterAttributes (PREVIEW_START,PREVIEW_LENGTH,previewStyle,true);
  }

  /**
   * Method to construct the panel containing the ColorSpec editing and
   * preview controls.
   *
   * @return The color editing panel.
   */
  private JPanel getColorPanel() {
    bgSwatch = new ColorSwatch (12, Color.RED);
    fgSwatch = new ColorSwatch (12, Color.BLUE);
    bgButton = new JButton("Set Background", bgSwatch);
    fgButton = new JButton("Set Foreground", fgSwatch);
    bgButton.addActionListener(buttonListener);
    fgButton.addActionListener(buttonListener);
    bgButton.setMnemonic('B');
    fgButton.setMnemonic('F');
    
    preview = new JTextPane ();
    preview.setEditable (false);
    preview.setText (PREVIEW_STRING);
    JScrollPane scroller = new JScrollPane (preview);
    previewStyle = preview.addStyle (null, null);
    
    setColorSpec(colorSpec);
    JPanel colorPanel = new JPanel(new BorderLayout(0, 11));
    Box tpanel = new Box (BoxLayout.X_AXIS);
    tpanel.add(Box.createHorizontalGlue ());
    tpanel.add(bgButton);
    tpanel.add(Box.createHorizontalStrut (5));
    tpanel.add(fgButton);
    tpanel.add(Box.createHorizontalGlue ());
    colorPanel.add(BorderLayout.NORTH,tpanel);
    colorPanel.add(BorderLayout.NORTH,tpanel);
    colorPanel.add(BorderLayout.CENTER,scroller);
    return colorPanel;
  }

  /**
   * Method to create the dialog controls.  Ok, revert and escape button panel.
   *
   * @return The component containing dialog control widgets.
   */
  private JPanel getControlPanel() {
    okayButton = new JButton("OK");
    revertButton = new JButton("Revert");
    cancelButton = new JButton("Cancel");
    okayButton.addActionListener(okAction);
    revertButton.addActionListener(revertAction);
    cancelButton.addActionListener(cancelAction);
    okayButton.setMnemonic('O');
    revertButton.setMnemonic('R');
    cancelButton.setMnemonic('C');
    JPanel controlPanel = new JPanel();
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
    //controlPanel.setLayout(new FlowLayout(FlowLayout.TRAILING,0,0));
    controlPanel.add(Box.createHorizontalGlue ());
    controlPanel.add(okayButton);
    controlPanel.add(Box.createHorizontalStrut (5));
    controlPanel.add(revertButton);
    controlPanel.add(Box.createHorizontalStrut (5));
    controlPanel.add(cancelButton);
    return controlPanel;
  }

  /**
   * Method to set the label for window titles.  This should be set to the
   * name of the property being edited and should be initialized before showing
   * the dialog.
   *
   * @param label The label for window titles.
   */
  public void setLabel(String label) {
    this.label = (label!=null)?label.trim():"";
    setTitle("Choose colors for "+label);
  }

  /**
   * Action to perform for ok.
   */
  private void ok() {
    dispose();
  }

  /**
   * Action to revert colors to original.
   */
  private void revert() {
    colorSpec.setBackground(original.getBackground());
    colorSpec.setForeground(original.getForeground());
    setWorkingColors(colorSpec);
  }

  /**
   * Action to perform for cancel.
   */
  private void cancel() {
    revert();
    dispose();
  }

  /**
   * Action to edit the specified color.
   *
   * @param background Flag indicating whether to edit the background or
   *         foreground color for the ColorSpec.
   */
  private void editColor(boolean background) {
    String xground = (background)?"background":"foreground";
    Color initc = (background)?colorSpec.getBackground():
        colorSpec.getForeground();
    Color c = JColorChooser.showDialog(this,"Choose "+xground+" for "+label,
        initc);
    if(c!=null) {
      if (background) {
        colorSpec.setBackground(c);
      } else {
        colorSpec.setForeground(c);
      }
      setWorkingColors(colorSpec);
    }
    return;
  }

  private class ButtonListener implements ActionListener {
  /**
   * Method to process primarily button action events.
   *
   * @param aevt The action event.
   */
  public void actionPerformed(ActionEvent aevt) {
    Object source = (aevt!=null)?aevt.getSource():null;
    if (source==bgButton) {
      editColor(true);
      return;
    }
    if (source==fgButton) {
      editColor(false);
      return;
    }
  }
  }

  public class ColorSwatch implements Icon {
    private Color color;
    private int size;
    
    public ColorSwatch (int size, Color c) {
      this.size = size;
      this.color = c;
    }
    public void setColor (Color c) { color = c; }
    public Color getColor (Color c) { return color; }
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor (color);
      g.fillRect (x+1, y+1, size-1, size-1);
      g.setColor (Color.BLACK);
      g.drawRect (x, y, size, size);
    }
    public int getIconWidth() {
      return size;
    }
    public int getIconHeight() {
      return size;
    }
  }
}
