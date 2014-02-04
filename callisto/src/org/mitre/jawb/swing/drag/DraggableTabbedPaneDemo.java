
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
package org.mitre.jawb.swing.drag;
import java.util.Random;
import java.util.Vector;

import javax.swing.*;
/*
 * Created on Dec 13, 2004
 */

/**
 * TODO Describe type
 * 
 * @author Galen B. Williamson
 * @version Dec 13, 2004
 */
public class DraggableTabbedPaneDemo extends JFrame implements Runnable {
    
    private javax.swing.JPanel jContentPane = null;
    private DraggableTabbedPane draggableTabbedPane = null;
    private JPanel jPanel = null;
    private JToggleButton lfToggleButton = null;
    /**
     * @see java.lang.Runnable#run()
     * 
     */
    public void run() {}
    
    /**
     * This method initializes draggableTabbedPane1 
     *  
     * @return org.mitre.flex.swing.DraggableTabbedPane 
     */    
    private DraggableTabbedPane getDraggableTabbedPane() {
        if (draggableTabbedPane == null) {
            draggableTabbedPane = new DraggableTabbedPane();
            draggableTabbedPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(3,3,3,3));
            //draggableTabbedPane1.addTab("RPT0", null, getRightTextPane0(), null);
            for (int i = 0; i < 5; i++) {
                draggableTabbedPane.addTab("LTP"+i, null, getLtpScrollPane(i), null);
            }
            for (int i = 0; i < 5; i++) {
                draggableTabbedPane.addTab("RTP"+i, null, getRtpScrollPane(i), null);//, JSplitPane.RIGHT);
            }
        }
        return draggableTabbedPane;
    }
    /**
     * This method initializes jTextPane    
     *  
     * @return javax.swing.JTextPane    
     */    
    private JTextPane getRightTextPane(int i) {
        if (rightTextPane.size() <= i || rightTextPane.elementAt(i) == null) {
            rightTextPane.setSize(i+1);
            JTextPane rightTextPane0 = new JTextPane();
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < 50; j++)
                sb.append("right text pane ").append(i).append(' ');
            rightTextPane0.setText(sb.toString());
            rightTextPane.setElementAt(rightTextPane0, i);
        }
        return (JTextPane) rightTextPane.elementAt(i);
    }
    
    private JTextPane getLeftTextPane(int i) {
        if (leftTextPane.size() <= i || leftTextPane.elementAt(i) == null) {
            leftTextPane.setSize(i+1);
            JTextPane leftTextPane0 = new JTextPane();
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < 50; j++)
                sb.append("left text pane ").append(i).append(' ');
            leftTextPane0.setText(sb.toString());
            leftTextPane.setElementAt(leftTextPane0, i);
        }
        return (JTextPane) leftTextPane.elementAt(i);
    }
    /**
     * This method initializes jPanel   
     *  
     * @return javax.swing.JPanel   
     */    
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.add(getLfToggleButton(), null);
            jPanel.add(getJButtonSplitDemo(), null);
        }
        return jPanel;
    }
    
    final static boolean USE_NATIVE_LOOK = false;
    /**
     * This method initializes jToggleButton    
     *  
     * @return javax.swing.JToggleButton    
     */    
    private JToggleButton getLfToggleButton() {
        if (lfToggleButton == null) {
            lfToggleButton = new JToggleButton();
            lfToggleButton.setText("Native Look");
            lfToggleButton.setMnemonic(java.awt.event.KeyEvent.VK_N);
            lfToggleButton.setRequestFocusEnabled(false);
            lfToggleButton.setToolTipText("Toggle Native Look and Feel");
            lfToggleButton.setRolloverEnabled(true);
            lfToggleButton.setSelected(USE_NATIVE_LOOK);
            lfToggleButton.addItemListener(new java.awt.event.ItemListener() { 
                public void itemStateChanged(java.awt.event.ItemEvent e) {    
                    boolean sel = lfToggleButton.isSelected();
                    if (jCheckBoxMenuItemNativeLook.isSelected() != sel) {
                        jCheckBoxMenuItemNativeLook.setSelected(sel);
                    }
                    if (lfState != sel) {
                        lfState = lfToggleButton.isSelected();
                        setNativeLook(lfState);
                    }
                }
            });
        }
        return lfToggleButton;
    }

    static void setNativeLook(boolean lfState) {
        try {
            if (lfState) {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
                restart();
            }
            else {
                UIManager.setLookAndFeel(
                        UIManager.getCrossPlatformLookAndFeelClassName());
                restart();                        }   
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }
    }
    
    static boolean starting = false; 
    static DraggableTabbedPaneDemo theDemoPane = null;
    static void restart() {
        if (theDemoPane != null) {
//            theDemoPane.dispose();
//            theDemoPane = new DraggableTabbedPaneDemo();
//            theDemoPane.setVisible(true);
            SwingUtilities.updateComponentTreeUI(theDemoPane);
//            theDemoPane.pack();
        }
        else {
            theDemoPane = new DraggableTabbedPaneDemo();
            theDemoPane.setVisible(true);
        }
        /*
        starting = true;
        if (theDemoPane != null)
        theDemoPane = new DraggableTabbedPaneDemo();
        theDemoPane.setVisible(true);
        theDemoPane.setTitle("theDemoPane");
        starting = false;
        */
    }
    static boolean lfState = false;
    private JSplitPane jSplitPane1 = null;
    private JSplitPane jSplitPane2 = null;
    private JSplitPane jSplitPane3 = null;

    private Vector rtpScrollPane = new Vector();
    private Vector ltpScrollPane = new Vector();
    private Vector leftTextPane = new Vector();
    private Vector rightTextPane = new Vector();
    
	private JButton jButton = null;
	private JScrollPane jScrollPane = null;
	private JTextPane jTextPane = null;
	private JTabbedPane jTabbedPane = null;
	private JTabbedPane jTabbedPane1 = null;
	private JTextPane jTextPane1 = null;
	private JTextPane jTextPane2 = null;
	private JTextPane jTextPane3 = null;
	private JTextPane jTextPane4 = null;
	private JButton jButtonSplitDemo = null;
	private JMenuBar jJMenuBar = null;
	private JMenu jMenuFile = null;
	private JMenuItem jMenuItemExit = null;
	private JCheckBoxMenuItem jCheckBoxMenuItemNativeLook = null;
    /**
     * This method initializes jScrollPane  
     *  
     * @return javax.swing.JScrollPane  
     */    
    private JScrollPane getRtpScrollPane(int i) {
        if (rtpScrollPane.size() <= i || rtpScrollPane.elementAt(i) == null) {
            rtpScrollPane.setSize(i+1);
            JScrollPane rtp0ScrollPane = new JScrollPane();
            rtp0ScrollPane.setBorder(BorderFactory.createEmptyBorder());
            rtp0ScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);            
            rtp0ScrollPane.setViewportView(getRightTextPane(i));
            rtpScrollPane.setElementAt(rtp0ScrollPane, i);
        }
        return (JScrollPane) rtpScrollPane.elementAt(i);
    }
    
    /**
     * This method initializes jScrollPane  
     *  
     * @return javax.swing.JScrollPane  
     */    
    private JScrollPane getLtpScrollPane(int i) {
        if (ltpScrollPane.size() <= i || ltpScrollPane.elementAt(i) == null) {
            ltpScrollPane.setSize(i+1);
            JScrollPane ltp0ScrollPane = new JScrollPane();
            ltp0ScrollPane.setBorder(BorderFactory.createEmptyBorder());
            ltp0ScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            ltp0ScrollPane.setViewportView(getLeftTextPane(i));
            ltpScrollPane.setElementAt(ltp0ScrollPane, i);
        }
        return (JScrollPane) ltpScrollPane.elementAt(i);
    }
    /**
     * This method initializes jSplitPane1  
     *  
     * @return javax.swing.JSplitPane   
     */    
    private JSplitPane getJSplitPane1() {
        if (jSplitPane1 == null) {
            jSplitPane1 = new JSplitPane();
            jSplitPane1.setOneTouchExpandable(true);
            jSplitPane1.setDividerSize(30);
            jSplitPane1.setContinuousLayout(true);
            jSplitPane1.setLeftComponent(getJTabbedPane());
            jSplitPane1.setRightComponent(getJTabbedPane1());
            jSplitPane1.setResizeWeight(0.5D);
        }
        return jSplitPane1;
    }
    /**
     * This method initializes jSplitPane2  
     *  
     * @return javax.swing.JSplitPane   
     */    
    private JSplitPane getJSplitPane2() {
        if (jSplitPane2 == null) {
            jSplitPane2 = new JSplitPane();
            jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            jSplitPane2.setOneTouchExpandable(true);
            jSplitPane2.setDividerSize(20);
            jSplitPane2.setTopComponent(getJButton());
            jSplitPane2.setBottomComponent(getJScrollPane());
            jSplitPane2.setContinuousLayout(true);
        }
        return jSplitPane2;
    }
    /**
     * This method initializes jSplitPane3  
     *  
     * @return javax.swing.JSplitPane   
     */    
    private JSplitPane getJSplitPane3() {
        if (jSplitPane3 == null) {
            jSplitPane3 = new JSplitPane();
            jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
            jSplitPane3.setOneTouchExpandable(true);
            jSplitPane3.setContinuousLayout(false);
        }
        return jSplitPane3;
    }
	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Ignore me");
		}
		return jButton;
	}
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTextPane());
			jScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return jScrollPane;
	}
	/**
	 * This method initializes jTextPane	
	 * 	
	 * @return javax.swing.JTextPane	
	 */    
	private JTextPane getJTextPane() {
		if (jTextPane == null) {
			jTextPane = new JTextPane();
		}
		return jTextPane;
	}
	/**
	 * This method initializes jTabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */    
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			jTabbedPane.addTab("TP1", null, getJTextPane1(), null);
			jTabbedPane.addTab("TP2", null, getJTextPane3(), null);
		}
		return jTabbedPane;
	}
	/**
	 * This method initializes jTabbedPane1	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */    
	private JTabbedPane getJTabbedPane1() {
		if (jTabbedPane1 == null) {
			jTabbedPane1 = new JTabbedPane();
			jTabbedPane1.addTab("TP3", null, getJTextPane2(), null);
			jTabbedPane1.addTab("TP4", null, getJTextPane4(), null);
		}
		return jTabbedPane1;
	}
	/**
	 * This method initializes jTextPane1	
	 * 	
	 * @return javax.swing.JTextPane	
	 */    
	private JTextPane getJTextPane1() {
		if (jTextPane1 == null) {
			jTextPane1 = new JTextPane();
		}
		return jTextPane1;
	}
	/**
	 * This method initializes jTextPane2	
	 * 	
	 * @return javax.swing.JTextPane	
	 */    
	private JTextPane getJTextPane2() {
		if (jTextPane2 == null) {
			jTextPane2 = new JTextPane();
		}
		return jTextPane2;
	}
	/**
	 * This method initializes jTextPane3	
	 * 	
	 * @return javax.swing.JTextPane	
	 */    
	private JTextPane getJTextPane3() {
		if (jTextPane3 == null) {
			jTextPane3 = new JTextPane();
		}
		return jTextPane3;
	}
	/**
	 * This method initializes jTextPane4	
	 * 	
	 * @return javax.swing.JTextPane	
	 */    
	private JTextPane getJTextPane4() {
		if (jTextPane4 == null) {
			jTextPane4 = new JTextPane();
		}
		return jTextPane4;
	}
	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButtonSplitDemo() {
		if (jButtonSplitDemo == null) {
			jButtonSplitDemo = new JButton();
			jButtonSplitDemo.setText("Start Splitting");
			jButtonSplitDemo.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
				    SwingUtilities.invokeLater(new Runnable() {
				        int i = -1;
				        Random random = new Random();
				        String constraints = "";
				        Runnable runner;
				        public void run() {
				            runner = this;
				            if (i++ < 7) {
				                int n = random.nextInt(4);
				                String constraint = JSplitPane.TOP;
				                switch (n) {
				                case 0: constraint = JSplitPane.TOP; break;
				                case 1: constraint = JSplitPane.BOTTOM; break;
				                case 2: constraint = JSplitPane.LEFT; break;
				                case 3: constraint = JSplitPane.RIGHT; break;
				                }
				                constraints += "xxx" + constraint;
				                draggableTabbedPane.addTab("FTP"+(i+3), null, getLtpScrollPane(i+3), constraints, constraints);
				                new Thread() {
				                    public void run() {            
				                        JOptionPane.showMessageDialog(draggableTabbedPane, "Added with path: "+constraints, "Split Pane Demo", JOptionPane.PLAIN_MESSAGE);
				                        SwingUtilities.invokeLater(runner);
				                    }
				                }.start();
				            }
				        }
				    });
				    jButtonSplitDemo.setEnabled(false);
				}
			});
		}
		return jButtonSplitDemo;
	}
	/**
	 * This method initializes jJMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */    
	private JMenuBar getJJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getJMenuFile());
		}
		return jJMenuBar;
	}
	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */    
	private JMenu getJMenuFile() {
		if (jMenuFile == null) {
			jMenuFile = new JMenu();
			jMenuFile.setText("File");
			jMenuFile.setMnemonic(java.awt.event.KeyEvent.VK_F);
			jMenuFile.add(getJCheckBoxMenuItemNativeLook());
			jMenuFile.add(getJMenuItemExit());
		}
		return jMenuFile;
	}
	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private JMenuItem getJMenuItemExit() {
		if (jMenuItemExit == null) {
			jMenuItemExit = new JMenuItem();
			jMenuItemExit.setText("Exit");
			jMenuItemExit.setMnemonic(java.awt.event.KeyEvent.VK_X);
			jMenuItemExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.Event.CTRL_MASK, true));
			jMenuItemExit.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					System.exit(0);
				}
			});
		}
		return jMenuItemExit;
	}
	/**
	 * This method initializes jCheckBoxMenuItem	
	 * 	
	 * @return javax.swing.JCheckBoxMenuItem	
	 */    
	private JCheckBoxMenuItem getJCheckBoxMenuItemNativeLook() {
		if (jCheckBoxMenuItemNativeLook == null) {
			jCheckBoxMenuItemNativeLook = new JCheckBoxMenuItem();
			jCheckBoxMenuItemNativeLook.setText("Native Look");
			jCheckBoxMenuItemNativeLook.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.Event.CTRL_MASK, true));
			jCheckBoxMenuItemNativeLook.setSelected(USE_NATIVE_LOOK);
			jCheckBoxMenuItemNativeLook.setMnemonic(java.awt.event.KeyEvent.VK_N);
			jCheckBoxMenuItemNativeLook.addItemListener(new java.awt.event.ItemListener() { 
				public void itemStateChanged(java.awt.event.ItemEvent e) {    
//					System.out.println("itemStateChanged()"); // TODO Auto-generated Event stub itemStateChanged()
				    boolean sel = jCheckBoxMenuItemNativeLook.isSelected();
				    if (lfToggleButton.isSelected() != sel) {
				        lfToggleButton.setSelected(sel);
				    }
				    if (lfState != sel) {
				        lfState = lfToggleButton.isSelected();
				        setNativeLook(lfState);
				    }
				}
			});
		}
		return jCheckBoxMenuItemNativeLook;
	}
     	public static void main(String[] args) {
	    setNativeLook(USE_NATIVE_LOOK);
	}
    
    /**
     * This is the default constructor
     */
    public DraggableTabbedPaneDemo() {
        super();
        initialize();
    }
    /**
     * This method initializes this
     * 
     * @return void
     */ 
    private void initialize() {
        try {
         this.setJMenuBar(getJJMenuBar());
         this.setBounds(100, 100, 1024, 600);
         this.setContentPane(getJContentPane());
         this.setTitle("Draggable Tabbed Pane Demo");
         this.addWindowListener(new java.awt.event.WindowAdapter() { 
             public void windowClosing(java.awt.event.WindowEvent e) {    
                 System.out.println("windowClosing()");
                 System.exit(0);
             }
         });
        } catch (Throwable t) {
            t.printStackTrace();
        }
     }
     /**
      * This method initializes jContentPane
      * 
      * @return javax.swing.JPanel
      */
     private javax.swing.JPanel getJContentPane() {
         if(jContentPane == null) {
             jContentPane = new javax.swing.JPanel();
             jContentPane.setLayout(new java.awt.BorderLayout());
             jContentPane.add(getJPanel(), java.awt.BorderLayout.NORTH);
             jContentPane.add(getJSplitPane1(), java.awt.BorderLayout.SOUTH);
             jContentPane.add(getJSplitPane2(), java.awt.BorderLayout.EAST);
             jContentPane.add(getJSplitPane3(), java.awt.BorderLayout.WEST);
             jContentPane.add(getDraggableTabbedPane(), java.awt.BorderLayout.CENTER);
         }
         return jContentPane;
     }
}  //  @jve:decl-index=0:visual-constraint="-6,32"
