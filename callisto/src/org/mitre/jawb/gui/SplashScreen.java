
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
import java.io.*;
import java.util.*;

import java.net.URL;

import javax.swing.*;
import javax.swing.border.*;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.Version;

/**
 * Splash screen. This class can also load classes listed in
 * resource file "classlist".
 */
public class SplashScreen extends JDialog implements Runnable {

  // private fields
  private Thread thread;
  private boolean finished = false;
  private String[] classes;
  private JProgressBar progress;
  
  /**
   * Creates a new splash screen which displays a picture, copyright and a
   * progress bar used to indicate the loading progress of the application.
   */
  public SplashScreen (Frame owner) {
    super (owner, false);
    setUndecorated (true);
    setBackground(Color.lightGray);

    JPanel pane = new JPanel (new BorderLayout());

    pane.setBorder (new EtchedBorder (EtchedBorder.RAISED));
    pane.add (BorderLayout.NORTH,
              new JLabel (Jawb.getIcon ("splash.gif", null)));

    progress = new JProgressBar (0, 100);
    progress.setStringPainted(true);
    progress.setFont (new Font ("SansSerif", Font.PLAIN, 10));
    progress.setString ("");
    progress.setBorder(BorderFactory.createMatteBorder(0,0,1,0, Color.black));
    pane.add (BorderLayout.CENTER, progress);

    JPanel labels = new JPanel(new BorderLayout());
    JLabel version = new JLabel("Callisto v" + Version.version(),
                                  JLabel.CENTER);
    JLabel copyright = new JLabel("Copyright " + Version.copyright(),
                                  JLabel.CENTER);
    labels.add(version, BorderLayout.NORTH);
    labels.add(copyright, BorderLayout.SOUTH);
    
    Font font = (Font)UIManager.get("TextField.font");
    version.setFont (font.deriveFont (10.0f));
    copyright.setFont (font.deriveFont (10.0f));

    Color color = (Color) UIManager.get("TextField.foreground");
    version.setForeground (color);
    copyright.setForeground (color);
    
    color = (Color)UIManager.get ("JLabel.background");
    version.setBackground (color);
    copyright.setBackground (color);
    
    pane.add (BorderLayout.SOUTH, labels);
    
    getContentPane ().add (pane);
    pack ();

    GUIUtils.centerComponent (this);
    setCursor (Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));

    // Once upon a time, a 'well known' hack lived here, which turned this
    // dialog modal _after_ making it visible, so that it would remain on top
    // of the owner (initial JawbFrame) whilst the AWT thread continued to
    // build the cathedral. Sadness befell the princess MacOS when she saw
    // this misuse of the mighty API, and the dialog remained 'modal' even
    // after lifting the spell (setModal(false)), hiding, and disposing of it
    // entirely. Some day, a handsome prince will come and learn the secret of
    // waking the princess' thread, so the hack may return for all to
    // enjoy. Untill then it has been banished from the land
    
    setVisible (true);
    //setModal (true);
    
    if (createClassArray () ) {
      
      thread = new Thread (this);
      thread.setDaemon (true);
      thread.setPriority (Thread.NORM_PRIORITY);
      thread.start ();
      try {
        thread.join ();
      } catch (Exception e) {}
      
    } else {
      setProgress (100, "Loading");
      finished = true;
    }
  }

  // get the classes to be loaded from the file 'classlist'.

  private boolean createClassArray () {
    Vector buf = new Vector(30);
    InputStream iStream = Jawb.getResourceAsStream("classlist");
    if (iStream == null)
      return false;

    BufferedReader in = new BufferedReader (new InputStreamReader(iStream));
    String buffer;
    try {
      while ((buffer = in.readLine()) != null)
        buf.add (buffer);
      in.close();
    } catch (IOException ioe) {
      return false;
    }

    classes = new String[buf.size()];
    buf.toArray (classes);
    return true;
  }

  /**
   * Loads the classes dinamycally from the list.
   */
  public void run() {
    String packs = getClass().getName();
    int i = packs.lastIndexOf('.');
    if (i >= 0)
      packs = packs.substring(0, i + 1);
    else
      packs = "";

    for (i = 0; i < classes.length; i++) {
      String n = classes[i];
      int j = n.lastIndexOf('.');
      if (j < 0)
        n = packs + n;
      progress.setString(n);

      try {
        Class c = Class.forName(n);
      } catch(Exception e) { }
      progress.setValue(100 * (i + 1) / classes.length);
    }
    finished = true;
    setProgress (-1, "Startup Loading");
    stop ();
  }

  /**
   * Set the splash screen progress value and text, only if the loading of
   * classes is finished. Use -1 as a value to modify only the text, and use
   * null as a text to modify only the value ("" to clear the text value)
   * @param percent percent complete 0 - 100
   * @param text The new text
   */
  public void setProgress (int percent, String text) {
    if (! isVisible ())
      setVisible (true);
    if (finished) {
      if (percent != -1)
        progress.setValue(percent);
      if (text != null)
        progress.setString(text);
    }
  }

  /**
   * Return the integer percentage the SplashScreen is currently set at, as an
   * int between 0 and 100.
   */
  public int getProgress () {
    return progress.getValue ();
  }
  
  /**
   * Stop the loading process.
   */
  public void stop() {
    thread = null;
  }

} //SplashScreen
