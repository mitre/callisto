
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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import javax.swing.*;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.*;

import org.dom4j.Document;
import org.dom4j.DocumentType;
import org.dom4j.Element;

import org.mitre.jawb.io.*;
import org.mitre.jawb.tasks.*;
import org.mitre.jawb.Jawb;

public class ReferenceEditor extends JPanel {

  private static final int DEBUG = 0;
  
  private static final String AIF_DTD =
    "http://www.nist.gov/speech/atlas/aif.dtd";
  
  private static final String UNKNOWN_TASK = "-Unknown-";
  
  /** SAX Handler that retrieves the URI of the MAIA Scheme of an .aif file */
  private static ATLASSaxHandler saxHandler = null;
  /** SAX parser that retrieves the URI of the MAIA Scheme of an .aif file */
  private static SAXParser saxParser = null;

  private URI aifURI;
  private URI signalURI;

  private JLabel aifLabel;
  private JFileChooser chooser;
  private JFrame frame;
  private JDialog dialog;
  
  private JTextField signalField;
  private AbstractButton relativeSignal;
  private AbstractButton absoluteSignal;

  private JTextField maiaField;
  private JComboBox maiaCombo;
  private AbstractButton localMaia;
  private AbstractButton cononicalMaia;

  private JTextField dtdField;

  private Action saveAction;
  private Action closeAction;
  
  public ReferenceEditor () {
    init ();
    initSAXHandler ();
  }
  
  public void showFrame (Component parent) {
    if (frame == null) {
      frame = new JFrame ("AIF Reference Editor"); // HIDE_ON_CLOSE by default
      frame.getContentPane ().add (this);
      
      frame.pack ();
      frame.setSize (new Dimension (550, frame.getSize ().height));
      //dialog.validate ();
      GUIUtils.centerComponent (parent, frame);
    }
    frame.show ();
  }

  public void showDialog (Component parent) {
    if (dialog == null) {
      Window window = MainTextPalette.getWindowForComponent (parent);
      dialog = new JDialog ((JFrame)window, "AIF Reference Editor", true);
      dialog.getContentPane ().add (this);
      
      dialog.pack ();
      dialog.setSize (new Dimension (550, dialog.getSize ().height));
      //dialog.validate ();
    }
    dialog.show ();
  }

  public void open (URI uri) {

    if (DEBUG > 0)
      System.err.println ("RefEdit.open: "+aifURI);

    if ( (aifURI = uri) == null) {
      clear ();
      return;
    }
    
    saxHandler.reset ();
    try { // Parse the input URI
      if (DEBUG > 0)
        System.err.println ("RefEdit.saxParse: parsing...");
      
      saxParser.parse (aifURI.toString (), saxHandler);
      
    } catch (EndOfProcessingException eope) {
      // done in good form! (exception forces parser to quit early)

    } catch (IOException ioe) {
      GUIUtils.showWarning (ioe.getMessage ());
      return;
      
    } catch (SAXException sxe) {
      // Error generated by this application
      Exception  x = sxe;
      if (sxe.getException() != null)
        x = sxe.getException();
      GUIUtils.showWarning (sxe.getMessage ());
      return;
    }

    aifLabel.setText (aifURI.getPath ());
    
    // update GUI's signal location info
    try {
      signalURI = new URI (saxHandler.getSignalURIString ());
    } catch (URISyntaxException x) {
      GUIUtils.showError ("Bad signal URL in aif file:\n"+x.getMessage ());
      clear ();
      return;
    }
    signalField.setText (signalURI.toString ());
    AbstractButton state = (signalURI.isAbsolute () ?
                            absoluteSignal : relativeSignal);
    state.setSelected (true);

    // update GUI's maia location info
    String maia = saxHandler.getMaiaURIString ();
    maiaField.setText (maia);
    Task task = ATLASHelper.findTask (maia, ATLASHelper.EXTERNAL);
    if (task != null) {
      maiaCombo.setSelectedItem (task);
      cononicalMaia.setSelected (true);
      
    } else {
      task = ATLASHelper.findTask (maia, ATLASHelper.LOCAL);
      if (task != null) {
        maiaCombo.setSelectedItem (task);
        localMaia.setSelected (true);
        
      } else
        maiaCombo.setSelectedItem (UNKNOWN_TASK);
    }
    
    // update GUI's DTD location
    dtdField.setText (saxHandler.getDTDURIString ());

    setDirty (false);
  }

  private void save () {
    File temp = null;
    // check our values
    if (aifURI == null ||
        "".equals (dtdField.getText ()) ||
        "".equals (maiaField.getText ()) ||
        "".equals (signalField.getText())) {
      GUIUtils.beep ();
      return;
    }
    try {
      // make a temp copy replacing some URL's w/ external references
      Document doc = ATLASHelper.parse (aifURI);
      DocumentType doctype = doc.getDocType ();
      Element corpus = doc.getRootElement ();
      Element signal = corpus.element ("SimpleSignal"); // first and only
      
      doctype.setSystemID (dtdField.getText ());
      corpus.addAttribute ("schemeLocation", maiaField.getText ());
      signal.addAttribute ("href", signalField.getText ());

      temp = File.createTempFile ("callisto", ".aif.xml");
      if (DEBUG >= 0)
        System.err.println ("RefEdit.save: creating temp .aif in:\n    "+temp);

      OutputStream out = new FileOutputStream (temp);
      ATLASHelper.dump (doc, out);
      out.close ();
      
      // ...and replace the ATLAS generated version
      File aif = new File (new URI (aifURI.toASCIIString()));
      File backup = new File (aif.toString()+"~");
      if (! (aif.renameTo (backup) && temp.renameTo (aif))) {
        System.err.println ("RefEdit.Error rewriting aif: "+aifURI+
                            "\n Failed to replace w/: "+temp.getPath ());
        // renaming failed, without exception, so delete the temp file
        // catch exceptions so temp is nulled.
        try { temp.delete (); } catch (Exception x) { /* ignore */ }
      }
      temp = null;
      
      // this exception was caused by renameTo, or earlier, in which case
      // the user's saved file is not the externalized version... but temp
      // still exists, and should be deleted if it does. The 'catch' is just
      // a "heads-up"
    } catch (Exception x) {
      GUIUtils.showError ("Error rewriting aif:\n"+aifURI);
      x.printStackTrace ();
      
    } finally {
      if (temp != null) temp.delete (); // clean up in case of errors
    }
    setDirty (false);
  }
  
  private void close () {
    if (dialog != null)
      dialog.hide ();
    else
      frame.hide ();
    // else WTF!?!?
    clear ();
  }

  private void clear () {
    if (DEBUG > 0)
      System.err.println ("RefEdit.clear");
    aifURI = null;
    aifLabel.setText ("");
    
    signalField.setText ("");
    maiaField.setText ("");
    dtdField.setText ("");
    setDirty (false);
  }

  /** Uses the 'signalURI' variable */
  private void evalSignal () {
    if (DEBUG > 0)
      System.err.println ("RefEdit.evalSig:");
    if (signalURI == null) {
      if (DEBUG > 0)
        System.err.println ("RefEdit.evalSig: signalURI=null");
      String text = signalField.getText ().trim ();
      if ("".equals (text))
        return;
      try {
        signalURI = new URI (text);
      } catch (URISyntaxException x) {
        GUIUtils.beep ();
        return;
      }
      if (DEBUG > 0)
        System.err.println ("RefEdit.evalSig: uri="+signalURI);
      
      // keep the signal URI absolute, even if the display is relative
      String path = aifURI.getRawPath ();
      URI aifBase = aifURI.resolve
        (path.substring (0, path.lastIndexOf ('/')+1));
      signalURI = aifBase.resolve (signalURI);

      if (DEBUG > 0)
        System.err.println ("RefEdit.evalSig: uri="+signalURI);
      AbstractButton state = (signalURI.isAbsolute () ?
                             absoluteSignal : relativeSignal);
      state.setSelected (true);
      
    } else {
      String uri;
      if (relativeSignal.isSelected ()) {
        if (DEBUG > 0)
          System.err.println ("RefEdit.evalSig: has URI, and relative"+
                              "\n\taif: "+aifURI+"\n\tsig: "+signalURI);
        // keep the signal URI absolute, even if the display is relative
        String path = aifURI.getRawPath ();
        URI aifBase = aifURI.resolve
          (path.substring (0, path.lastIndexOf ('/')+1));
        URI relativeURI = aifBase.relativize (signalURI);

        if (relativeURI.isAbsolute ()) {
          absoluteSignal.setSelected (true);
          GUIUtils.showWarning ("Signal path cannot be relativized\nto the aif path");
        }
        uri = relativeURI.toString ();
      } else {
        if (! signalURI.isAbsolute ()) {
          signalURI = aifURI.resolve (signalURI);
        }
        uri = signalURI.toString ();
      }
      signalField.setText (uri);
    }
    setDirty (true);
  }

  /** Uses the selected item in the maiaCombo */
  private void evalMaiaScheme () {
    if (aifURI == null)
      return;
    
    Object selected = maiaCombo.getSelectedItem ();
    if (selected instanceof Task) {
      String maia = null;
      if (localMaia.isSelected ())
        maiaField.setText (((Task)selected).getLocalMaiaURI ().toString ());
      else
        maiaField.setText (((Task)selected).getMaiaURI ().toString ());
      setDirty (true);
    }
  }

  private void setDirty (boolean dirty) {
    saveAction.setEnabled (dirty);
  }

  
  public static void main (String args[]) {
    ReferenceEditor relocator = new ReferenceEditor();
    relocator.showDialog (new JFrame ());
    System.exit (0);
  }

  /***********************************************************************/
  /* Initialization */
  /***********************************************************************/

  private static void initSAXHandler () {
    if (DEBUG > 0)
      System.err.println ("RefEdit.initSAXHandler");

    saxHandler = new ATLASSaxHandler ();
    
    try {
      // Use the default (non-validating) parser
      SAXParserFactory factory = SAXParserFactory.newInstance ();
      saxParser = factory.newSAXParser ();
      
      XMLReader xmlReader = saxParser.getXMLReader();
      xmlReader.setProperty ("http://xml.org/sax/properties/lexical-handler",
                             saxHandler);     
    } catch (Exception x) {
      throw
        new RuntimeException ("Unable to create parser to Retrieve MAIA", x);
    }
    if (DEBUG > 0)
      System.err.println ("RefEdit.initSAXHandler: initialized");
  }

  private void init () {
    setLayout (new BorderLayout ());
    setBorder (BorderFactory.createEmptyBorder (5,5,5,5));

    JTextArea text = new JTextArea
      ("Choose an AIF file to verify it's reference documents.\nWARNING:\n"+
       "  * Manually entered values are not well validated!\n"+
       "  * MAIA Schema must conform to the Schema the AIF was created with\n"+
       "  * Saving an from within Callisto will overwrite changes made here\n"+
       "*** Use at your own risk. ***");
    text.setEditable (false);
    //text.setLineWrap (true);
    //text.setWrapStyleWord (true);
    text.setBackground (this.getBackground ());
    text.setBorder (BorderFactory.createEmptyBorder (0,0,8,0));

    Font labelFont = (Font)UIManager.get ("TextField.font");
    
    JPanel fileEditor = new JPanel (new BorderLayout ());
    chooser = new JFileChooser ();
    chooser.setMultiSelectionEnabled (false);
    
    JPanel aifPanel = new JPanel (new BorderLayout ());
    aifPanel.setBorder (BorderFactory.createTitledBorder ("AIF File:"));
    aifLabel = new JLabel ();
    aifLabel.setFont(labelFont);
    JButton choose = new JButton (new AbstractAction ("Browse") {
        public void actionPerformed (ActionEvent e) {
          chooser.setDialogTitle ("Choose AIF File");
          chooser.showDialog (ReferenceEditor.this, "Select");
          File aif = chooser.getSelectedFile ();
          open ((aif == null) ? null : aif.toURI ());
        }
      });
    aifPanel.add (aifLabel, BorderLayout.CENTER);
    aifPanel.add (choose, BorderLayout.EAST);

    Box refPanel = Box.createVerticalBox ();
    refPanel.setBorder (BorderFactory.createTitledBorder ("References"));

    JLabel label = new JLabel ("Signal Location:");
    label.setFont(labelFont);
    relativeSignal = new JRadioButton (new AbstractAction ("Relative") {
        public void actionPerformed (ActionEvent e) { evalSignal (); }
      });
    absoluteSignal = new JRadioButton (new AbstractAction ("Absolute") {
        public void actionPerformed (ActionEvent e) { evalSignal (); }
      });
    relativeSignal.setSelected (true);
    JPanel toggles = new JPanel (new FlowLayout ());
    ButtonGroup group = new ButtonGroup ();
    group.add (relativeSignal);
    group.add (absoluteSignal);
    toggles.add (relativeSignal);
    toggles.add (absoluteSignal);
    signalField = new JTextField ();
    // TODO: add key lisntener set dirty
    choose = new JButton (new AbstractAction ("Browse") {
        public void actionPerformed (ActionEvent e) {
          chooser.setDialogTitle ("Choose Signal");
          chooser.showDialog (ReferenceEditor.this, "Select");
          File signal = chooser.getSelectedFile ();
          if (signal != null) {
            signalURI = signal.toURI ();
            evalSignal ();
          }
        }
      });
    JPanel panel = new JPanel (new BorderLayout ());
    JPanel top = new JPanel (new BorderLayout ());
    top.add (label, BorderLayout.CENTER);
    top.add (toggles, BorderLayout.EAST);
    panel.add (signalField, BorderLayout.CENTER);
    panel.add (choose, BorderLayout.EAST);
    refPanel.add (top);
    refPanel.add (panel);
    refPanel.add (refPanel.createVerticalStrut (5));
    
    label = new JLabel ("MAIA Schema:");
    label.setFont(labelFont);
    localMaia = new JRadioButton (new AbstractAction ("Local") {
        public void actionPerformed (ActionEvent e) { evalMaiaScheme (); }
      });
    cononicalMaia = new JRadioButton (new AbstractAction ("Cononical") {
        public void actionPerformed (ActionEvent e) { evalMaiaScheme (); }
      });
    cononicalMaia.setSelected (true);
    toggles = new JPanel (new FlowLayout ());
    group = new ButtonGroup ();
    group.add (localMaia);
    group.add (cononicalMaia);
    toggles.add (localMaia);
    toggles.add (cononicalMaia);
    maiaField = new JTextField ();
    // TODO: add key lisntener set dirty
    // TODO: add key lisntener to clear the combo and research if anything is
    // manually entered
    Object[] tasks = Jawb.getTasks ().toArray ();
    maiaCombo = new JComboBox (tasks);
    maiaCombo.addItem (UNKNOWN_TASK);
    maiaCombo.addActionListener (new ActionListener () {
        public void actionPerformed (ActionEvent e) { evalMaiaScheme (); }
      });
    panel = new JPanel (new BorderLayout ());
    top = new JPanel (new BorderLayout ());
    top.add (label, BorderLayout.CENTER);
    top.add (toggles, BorderLayout.EAST);
    panel.add (maiaField, BorderLayout.CENTER);
    panel.add (maiaCombo, BorderLayout.EAST);
    refPanel.add (top);
    refPanel.add (panel);
    refPanel.add (refPanel.createVerticalStrut (5));
    
    label = new JLabel ("AIF DTD:");
    label.setFont((Font)UIManager.get ("TextField.font"));
    dtdField = new JTextField ();
    // TODO: add key lisntener set dirty
    JButton reset = new JButton (new AbstractAction ("Default") {
        public void actionPerformed (ActionEvent e) {
          if (! AIF_DTD.equals (dtdField.getText ())) {
            dtdField.setText (AIF_DTD);
            setDirty (true);
          }
        }
      });
    panel = new JPanel (new BorderLayout ());
    panel.add (label, BorderLayout.NORTH);
    panel.add (dtdField, BorderLayout.CENTER);
    panel.add (reset, BorderLayout.EAST);
    refPanel.add (panel);

    // buttons
    JPanel buttons = new JPanel (new FlowLayout (FlowLayout.TRAILING));
    saveAction = new AbstractAction ("Save") {
        public void actionPerformed (ActionEvent e) { save (); }
      };
    saveAction.setEnabled (false);
    closeAction = new AbstractAction ("Close") {
        public void actionPerformed (ActionEvent e) { close (); }
      };
    buttons.add (new JButton (saveAction));
    buttons.add (new JButton (closeAction));
    
    // all together now
    fileEditor.add (aifPanel, BorderLayout.NORTH);
    panel = new JPanel (new BorderLayout ());
    panel.add (refPanel, BorderLayout.NORTH);
    fileEditor.add (panel, BorderLayout.CENTER);

    add (text, BorderLayout.NORTH);
    add (fileEditor, BorderLayout.CENTER);
    add (buttons, BorderLayout.SOUTH);
  }
}
