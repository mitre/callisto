
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
 * Created on Jan 29, 2007 at 2:50:50 PM by Galen B. Williamson
 */
package org.mitre.jawb.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

public class DebugGBL extends GridBagLayout {
    private boolean addingDebugLabels = false;
    private HashMap debugLabels = new HashMap();

    public final static Field[] GBC_FIELDS;
    public final static Map GBC_CONSTANTS;
    
    static {
      Field[] gbcFields = GridBagConstraints.class.getFields();
      Map fields = new TreeMap();
      Map constants = new HashMap();
      int mods = Modifier.FINAL | Modifier.STATIC;
      for (int i = 0; i < gbcFields.length; i++) {
        Field f = gbcFields[i];
        String name = f.getName();
        int modifiers = f.getModifiers();
        if ((modifiers & mods) == 0) {
          fields.put(name, f);
        }
        else {
          try {
            constants.put(f.get(null), name);
          } catch (IllegalArgumentException e) {
            e.printStackTrace();
          } catch (IllegalAccessException e) {
            e.printStackTrace();
          }
        }
      }
      GBC_FIELDS = (Field[]) fields.values().toArray(new Field[0]);
      GBC_CONSTANTS = Collections.unmodifiableMap(constants);
    }
    
    private class DBLabel extends JLabel implements ActionListener {
      Timer timer = new Timer(0, this);
      private Component comp;
      DBLabel(Component comp) {
        super();
        this.comp = comp;
//        setFont(getFont().deriveFont(10.0f));
        setForeground(Color.RED);
        setBackground(new Color(1, 1, 0, 0.5f));
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        addMouseListener(new MouseAdapter() {
          boolean clicked = false;
          public void mouseClicked(MouseEvent e) {
              setToolTipText(null);
              if (clicked) {
                timer.stop();
                clicked = false;
                actionPerformed(null);
              }
              else {
//                setFont(new Font(getFont().getFamily(), Font.PLAIN, 14));
                clicked = true;
                restartTimer(1.0f);
              }
          }

          private void restartTimer(float trans) {
            clicked = false;
            Color c = getBackground();
            setBackground(new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.round(255 * trans)));
            timer.setRepeats(false);
            timer.setInitialDelay(5000);
            timer.restart();
//            if (DBLabel.this.comp instanceof JComponent) {
//              JComponent jc = (JComponent) DBLabel.this.comp;
//              Border b = jc.getBorder();
//              if (b != null) {
//                LineBorder lb = null;
//                if (b instanceof CompoundBorder) {
//                  b = ((CompoundBorder) b).getOutsideBorder();
//                }
//                if (b instanceof LineBorder) {
//                  lb = (LineBorder) b;
//                }
//                else {
//                  lb = (LineBorder) BorderFactory.createLineBorder(Color.RED);
//                  jc.setBorder(BorderFactory.createCom)
//                }
//              }
//              else {
//                b = BorderFactory.createLineBorder(Color.RED);
//                jc.setBorder(b);
//              }
//            }
            repaint();
          }

          int tooltipSavedTime = -1;
          public void mouseEntered(MouseEvent e) {
            if ((e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0) {
              showToolTip(e);
            }
            setFont(new Font(getFont().getFamily(), Font.PLAIN, 12));
            repaint();
            if (! clicked) {
              restartTimer(0.75f);
            }
          }

          private void showToolTip(MouseEvent e) {
            setToolTipText(createToolTipText());
            ToolTipManager ttm = ToolTipManager.sharedInstance();
            int delay = ttm.getDismissDelay();
            if (tooltipSavedTime == -1) {
              tooltipSavedTime = delay;
              ttm.setDismissDelay(100000);
            }
            ttm.mouseMoved(e);
          }

          public void mouseMoved(MouseEvent e) {
            if ((e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0) {
//              if (getToolTipText() == null) {
                showToolTip(e);
//              }
            }
          }
          
          public void mouseExited(MouseEvent e) {
            if (tooltipSavedTime != -1) {
              ToolTipManager.sharedInstance().setDismissDelay(tooltipSavedTime);
              tooltipSavedTime = -1;
            }
            setToolTipText(null);
//            if (! clicked) {
//              timer.stop();
//              actionPerformed(null);
//            }
          }

        });
      }

      StringBuffer sb = new StringBuffer();
      protected String createToolTipText() {
        sb.setLength(0);
        sb.append("<html><body style=\"font-size: 14pt\">");
        GridBagConstraints gbc = getConstraints(comp);

        for (int i = 0; i < GBC_FIELDS.length; i++) {
          Field f = GBC_FIELDS[i];
          sb.append("<dd><b>").append(f.getName())
          .append(":</b>&nbsp;");
          try {
            Object value = f.get(gbc);
            if (value instanceof Integer && !(f.getName().matches(".*?(?:x|y|height|width)$"))) {
              sb.append('(').append(value).append(')');
              int v = ((Integer) value).intValue();
              boolean once = false;
              for (Iterator it = GBC_CONSTANTS.keySet().iterator(); it.hasNext();) {
                Object key = it.next();
                if (key instanceof Integer) {
                  String name = (String) GBC_CONSTANTS.get(key);
                  int keyValue = ((Integer) key).intValue();
                  if (v == keyValue) {
                    if (once) {
                      sb.append(" | ");
                    }
                    else {
                      once = true;
                    }
                    sb.append(name);
                  }
                }
              }
            }
            else {
              sb.append(value);
            }
          } catch (IllegalArgumentException e) {
            e.printStackTrace();
          } catch (IllegalAccessException e) {
            e.printStackTrace();
          }
          sb.append("</dd><br>");
        }
        sb.append("</body></html>");
        return sb.toString();
      }

      public void actionPerformed(ActionEvent e) {
        Color c = getBackground();
        setBackground(new Color(c.getRed(), c.getGreen(), c.getBlue(), 128));
//        setFont(new Font(getFont().getFamily(), Font.PLAIN, 10));
//        setSize(getPreferredSize());
        invalidate();
        revalidate();
        repaint();
      }

      public boolean isOpaque() {
        return true;
      }

      public Dimension getMaximumSize() {
        return getPreferredSize();
      }

      public Dimension getMinimumSize() {
        return getPreferredSize();
      }

      public Dimension getPreferredSize() {
        return new Dimension(0, 0);
      }

      protected Dimension getSuperPreferredSize() {
        return super.getPreferredSize();
      }
    }

    public void addLayoutComponent(Component comp, Object constraints) {
      if (! (comp instanceof DBLabel)) {
        if (comp instanceof JComponent) {
          JComponent c = (JComponent) comp;
          Border b = c.getBorder();
          if (b == null) {
            c.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
          }
          else {
            c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GREEN, 1), b));
          }
        }
        super.addLayoutComponent(comp, constraints);
        DBLabel label = new DBLabel(comp);
        debugLabels.put(comp, label);
//        comp.getParent().add(label);
      }
    }

    public void removeLayoutComponent(Component comp) {
      if (! (comp instanceof DBLabel)) {
        super.removeLayoutComponent(comp);
        comp.getParent().remove((Component) debugLabels.remove(comp));
      }
    }

    public void layoutContainer(Container parent) {
      if (addingDebugLabels) {
        return;
      }
      addingDebugLabels = true;
      try {
        for (Iterator it = debugLabels.keySet().iterator(); it.hasNext(); parent.remove((Component) debugLabels.get(it.next())));
        super.layoutContainer(parent);
        Component[] cs = parent.getComponents();
        for (int i = 0; i < cs.length; i++) {
          if (! (cs[i] instanceof DBLabel)) {
            int x = cs[i].getX();
            int y = cs[i].getY();
            Point p = location(x, y);
            String name = i+":("+p.x+","+p.y+")";
            DBLabel label = (DBLabel) debugLabels.get(cs[i]);
//            System.err.println(name);
            label.setFont(new Font(label.getFont().getFamily(), Font.PLAIN, 10));
            label.setText(name);
            Dimension d = label.getSuperPreferredSize();
            parent.add(label, null, 0);
            label.setBounds(x, y, d.width, d.height);
          }
        }
      } finally {
        addingDebugLabels = false;
      }
    }

  }