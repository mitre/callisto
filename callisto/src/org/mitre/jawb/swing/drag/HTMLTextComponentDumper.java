
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
 * Created on Feb 25, 2005
 */
package org.mitre.jawb.swing.drag;

import java.awt.Window;
import java.awt.event.*;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.AbstractDocument.AbstractElement;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


/**
 * TODO Describe type
 * 
 * @author Galen B. Williamson
 * @version Feb 25, 2005
 */
public class HTMLTextComponentDumper extends MouseAdapter {
    protected JTextPane textPane;
    protected String title;
    
    /**
     * 
     */
    public HTMLTextComponentDumper(JTextPane textPane, String title) {
        this.textPane = textPane;
        this.title = title;
        textPane.addMouseListener(this);
    }
    public void mouseClicked(MouseEvent e) {
        int mods = e.getModifiersEx();
        int onmask = MouseEvent.CTRL_DOWN_MASK | MouseEvent.ALT_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK;
        int offmask = 0;
        if (e.getClickCount() > 1
                && (mods & (onmask | offmask)) == onmask) {
            dump();
        }
    }
    public void dump() {
        HTMLDocument sdoc = (HTMLDocument) textPane.getStyledDocument(); 
        int dLen = sdoc.getLength();
        String text = textPane.getText();
        
        JTextPane t = new JTextPane();
        t.setContentType("text/html");
        t.setText(text);
        final HTMLDocument tdoc = (HTMLDocument) t.getStyledDocument();
        StyleSheet ts = tdoc.getStyleSheet();
        ts.addStyleSheet(sdoc.getStyleSheet());
        t.setCaretPosition(0);
        
        JTextArea b = new JTextArea();
        b.setText(text);

        JTextArea c = new JTextArea();
        c.setText(sdoc.getStyleSheet()+"");
        
        DefaultTreeModel tm = new DefaultTreeModel((AbstractElement) tdoc.getDefaultRootElement());
        JTree tree = new JTree(tm);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        JSplitPane tsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        tsp.setTopComponent(new JScrollPane(tree));
        final JTextArea tta = new JTextArea();
        tta.setEditable(false);
        tsp.setBottomComponent(new JScrollPane(tta));
        tree.addTreeSelectionListener(new TreeSelectionListener() {

        	final StringBuffer sb = new StringBuffer();

        	public void valueChanged(TreeSelectionEvent e) {
				TreePath path = e.getPath();
				if (path == null) {
					tta.setText(null);
				}
				else {
					AbstractElement elt = (AbstractElement) path.getLastPathComponent();
					sb.setLength(0);
					sb.append(elt.toString());
					sb.append("\n").append(elt.getClass());
					AttributeSet attributes = elt.getAttributes();
					if (attributes != null) {
						sb.append("\nAttributes:\n\t").append(attributes);
						Enumeration attributeNames = attributes.getAttributeNames();
						boolean hasContent = false;
						while (attributeNames.hasMoreElements()) {
							Object next = attributeNames.nextElement();
							Object attribute = attributes.getAttribute(next);
							if ("content".equalsIgnoreCase(""+attribute))
								hasContent = true;
							sb.append("\n\t\t").append(next).append(": ").append(attribute);
						}
						if (true) {
							try {
								int start = elt.getStartOffset();
								int end = elt.getEndOffset();
								int len = end - start;
								sb.append("\nContent (").append(start).append("-").append(end).append("=").append(len).append("): \"").append(tdoc.getText(start, len));
								sb.append("\"\nEnd Content");
							} catch (BadLocationException e1) {
								 e1.printStackTrace();
							}
						}
					}
					if (elt.getAllowsChildren()) {
						int childCount = elt.getChildCount();
						if (childCount == 0) {
							sb.append("\n\nNo children");
						}
						else {
							sb.append("\n\n").append(childCount).append(" children.");
						}
					}
					tta.setText(sb.toString());
				}
			}
        	
        });
        
        final JFrame f = new JFrame(title);
        DraggableTabbedPane tp = new DraggableTabbedPane();
        InputMap inputMap = tp.getInputMap(tp.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = tp.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "Close");
        AbstractAction closeAction = new AbstractAction("Close") {
          public void actionPerformed(ActionEvent e) {
            f.dispose();
          }
        };
        actionMap.put("Close", closeAction);
        JMenuBar mb = new JMenuBar();
        JMenu m = new JMenu("HTML Dump");
        JMenuItem closeItem = new JMenuItem(closeAction);
        m.add(closeItem);
        mb.add(m);
        f.setJMenuBar(mb);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.getContentPane().add(tp);
        tp.add("Sour&ce", new JScrollPane(b));
        tp.add("&View", new JScrollPane(t));
        tp.add("&StyleSheet", new JScrollPane(c));
        tp.add("Doc&Tree", new JScrollPane(tsp));
        f.pack();
        f.setSize(1000, 1000);
        f.setVisible(true);
        
        Window windowAncestor = SwingUtilities.getWindowAncestor(textPane);
        windowAncestor.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				closed(e);
			}

			public void windowClosed(WindowEvent e) {
				closed(e);
			}

			private void closed(WindowEvent e) {
				((Window) e.getSource()).removeWindowListener(this);
				f.dispose();
			}
        });
        
        System.err.println("Doc size = "+sdoc.getLength());
        System.err.println("text length = "+text.length());
    }
}

