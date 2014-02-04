
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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLDocument;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.io.BrowserLauncher;
import org.mitre.jawb.tasks.Task;


public class AboutTasksDialog {

  private static AboutTasksDialog dialog;
  
  private JOptionPane optionPane;
  private JEditorPane info;

  /**
   * Singleton
   */
  private AboutTasksDialog() {

    info = new JEditorPane("text/html","");
    info.setEditable(false);
    info.setBackground((Color) UIManager.get("JPanel.background"));
    info.addHyperlinkListener(new SimpleLinkListener());
    try {
      ((HTMLDocument) info.getDocument()).setBase(new URL("http://"));
    } catch (MalformedURLException x) {
      System.err.println(x);
    }

    JScrollPane scroller = new JScrollPane(info,
       JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
       JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    optionPane = new JOptionPane(scroller, JOptionPane.PLAIN_MESSAGE);
    optionPane.setPreferredSize(new Dimension(480, 360));
  }

  /**
   * Displays an about window with "ok" as only option.
   */
  public static void show (Component parent) {
    if (dialog == null) {
      dialog = new AboutTasksDialog();
    }

    // re-read all info, in case we allow dynamic tasks in the future
    dialog.readInfo();
    dialog.optionPane.createDialog(parent, "About Callisto Tasks").show();
  }

  private void readInfo() {
    // sort alphabetically by Title
    TreeSet ordered = new TreeSet(new Comparator() {
        public int compare(Object a, Object b) {
          Task ta = (Task) a;
          Task tb = (Task) b;
          return ta.getTitle().compareTo(tb.getTitle());
        }
      });
    ordered.addAll(Jawb.getTasks());

    StringBuffer buffer = new StringBuffer("<html><body>\n");
    buffer.append("<h2>Installed Tasks</h2>\n")
      .append("<table>");

    // table of two columns
    // iterater for column 2, advanced to half way
    Iterator c2 = ordered.iterator();
    int i=0;
    for (; i<(ordered.size()+1)/2; i++) {
      c2.next();
    }
    
    Iterator c1 = ordered.iterator();
    for (i=0; c1.hasNext() && i<(ordered.size()+1)/2; i++) {
      buffer.append("<tr style='font:small'><td width=200>");
      Task task = (Task) c1.next();
      buffer.append("<a href='#")
        .append(task.getName()).append("'>")
        .append(task.getTitle()).append("</a>");

      buffer.append("</td><td>");
      if (c2.hasNext()) {
        task = (Task) c2.next();
        buffer.append("<a href='#")
          .append(task.getName()).append("'>")
          .append(task.getTitle()).append("</a>");
      }
      buffer.append("</td></tr>");
    }

    buffer.append("</table><hr>\n<table>");

    Iterator iter = ordered.iterator();
    while (iter.hasNext()) {
      Task task = (Task) iter.next();
      buffer.append("  <tr style='font:large'><td colspan='2'><a name='")
        .append(task.getName()).append("'>")
        .append(task.getTitle()).append("</a> <font size='-1'>(")
        .append(task.getVersion()).append(")</font></td></tr>\n");

      URI homePage = task.getHomePage();
      URI localDocs = task.getLocalDocs();
      if (homePage != null || localDocs != null) {
        // TODO: make these links
        buffer.append("  <tr><td style='width:10'></td>\n    <td>");

        if (homePage != null) {
          buffer.append("Home Page: ").append(homePage);
        }
        if (localDocs != null) {
          // TODO: make this a link
          if (homePage != null) {
            buffer.append("<br>");
          }
          buffer.append("Documentation: ").append(localDocs);
        }
        buffer.append("</td></tr>\n");
      }
      
      buffer.append("  <tr><td style='width:10'></td><td>")
        .append(task.getDescription()).append("</td></tr>\n");
      
      if (iter.hasNext()) {
        buffer.append("  <tr><td colspan='2'></td></tr>\n");
      }
    }

    buffer.append("</table></body></html>");

    //DEBUG: System.err.println(buffer.toString());

    info.setText(buffer.toString());
    info.setCaretPosition(0);
  }


  // **********************************************************************
  // Helper Classes
  // **********************************************************************

  
  private static class SimpleLinkListener implements HyperlinkListener {
    
    public void hyperlinkUpdate(HyperlinkEvent he) {
      // We'll keep some basic debuggin information in here so you can
      // verify our new editor kit is working.
      
      HyperlinkEvent.EventType type = he.getEventType();
      JEditorPane pane = (JEditorPane) he.getSource();
      
      // Ok.  Decide which event we got...
      if (type == HyperlinkEvent.EventType.ENTERED) {
        // Enter event.  Go the the "hand" cursor and fill in the status bar
        pane.setCursor(((HTMLEditorKit) pane.getEditorKit()).getLinkCursor());
      }
      else if (type == HyperlinkEvent.EventType.EXITED) {
        // Exit event.  Go back to the default cursor and clear the status bar
        pane.setCursor(Cursor.getDefaultCursor());
      }
      else {
        // Jump event.  Get the url, and if it's not null, either scroll, or
        // launch browsere for the specified link
        try {
          URL base = ((HTMLDocument) pane.getDocument()).getBase();
          
          if (he.getURL().getPath().length() == 0) {
            pane.scrollToReference(he.getURL().getRef());
          }
          else {
            BrowserLauncher.openURL(he.getURL().toString());
          }
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
  
}// AboutTasksDialog
