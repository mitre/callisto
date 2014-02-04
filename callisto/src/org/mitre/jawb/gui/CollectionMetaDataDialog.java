
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

import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.SpringLayout.Constraints;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicCheckBoxUI;
import javax.swing.plaf.basic.BasicRadioButtonUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.mitre.jawb.gui.CollectionMetaDataDialog.FormField.Option;

/**
 * <pre>
 *  callisto.metadata.question.0.name=language
 *  callisto.metadata.question.0.text=Language
 *  callisto.metadata.question.0.type=menu
 *  callisto.metadata.question.0.mandatory=true
 *  callisto.metadata.question.0.numvals=4
 *  callisto.metadata.question.0.values.0=English
 *  callisto.metadata.question.0.values.1=Arabic
 *  callisto.metadata.question.0.values.2=Chinese
 *  callisto.metadata.question.0.values.3=Pashto

 *  callisto.metadata.question.1.text=Difficult?
 *  callisto.metadata.question.1.type=singlecheckbox
 *
 *  callisto.metadata.question.2.text=Topic:
 *  callisto.metadata.question.2.type=multicheckbox
 *  callisto.metadata.question.2.maxsel=2
 *  callisto.metadata.question.2.numvals=4
 *  #include a 'none of the above' option which must be selected if
 *  nothing else is 
 *  callisto.metadata.question.2.none=true
 *  callisto.metadata.question.2.values.0=travel
 *  callisto.metadata.question.2.values.1=meeting
 *  callisto.metadata.question.2.values.2=acquisition
 *  callisto.metadata.question.2.values.3=other * </pre>
 *
 * @author <a href="mailto:gwilliam@mitre.org">Galen B. Williamson</a>
 *
 */
public class CollectionMetaDataDialog extends JDialog {
  
  private static final int DEBUG = 0;

  private static final String PROP_LABEL = "label";
  private static final String PROP_PREFIX = "callisto.metadata.";
  private static final String PROP_INSTRUCTIONS = PROP_PREFIX + "instructions";
  private static final String PROP_MANDATORY = "mandatory";
  private static final String PROP_NONE = "none";
  private static final String PROP_MAXSEL = "maxsel";
  private static final String PROP_MAXCHARS = "maxchars";
  private static final String PROP_TEXT = "text";
  private static final String PROP_NAME = "name";
  private static final String DOT = ".";
  private static final String VALUES = "values";
  private static final String PROP_TYPE = "type";
  private static final String PROP_PREFIX_QUESTION = PROP_PREFIX + "question.";

  private static class SmartSizeScrollPane extends JScrollPane {
    public Dimension getMaximumSize() {
      return getPreferredSize();
    }

    Scrollable scrollableView;

    boolean hCheckingPreferredSize = false;
    boolean vCheckingPreferredSize = false;

    public SmartSizeScrollPane() {
      super();
    }

    public SmartSizeScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
      super(view, vsbPolicy, hsbPolicy);
    }

    public SmartSizeScrollPane(int vsbPolicy, int hsbPolicy) {
      super(vsbPolicy, hsbPolicy);
    }

    public SmartSizeScrollPane(Component view) {
      super(view);
    }

    public void setViewportView(Component view) {
      super.setViewportView(view);
      if (view instanceof Scrollable) {
        scrollableView = (Scrollable) view;
      }
      else {
        scrollableView = null;
      }
    }

    public int getVerticalScrollBarPolicy() {
      if (vCheckingPreferredSize) {
        return VERTICAL_SCROLLBAR_ALWAYS;
      }
      return super.getVerticalScrollBarPolicy();
    }

    public int getHorizontalScrollBarPolicy() {
      if (hCheckingPreferredSize) {
        return HORIZONTAL_SCROLLBAR_ALWAYS;
      }
      return super.getHorizontalScrollBarPolicy();
    }

    public Dimension getMinimumSize() {
      startCheckingSize();
      try {
        return super.getMinimumSize();
      } finally {
        stopCheckingSize();
      }
    }

    public Dimension getPreferredSize() {
      startCheckingSize();
      try {
//      Insets insets = getInsets();
//      Dimension ps = getViewport().getView().getPreferredSize();
        Dimension sps = super.getPreferredSize();
//      if (hCheckingPreferredSize) {
//      sps.height = Math.max(ps.height + insets.top + insets.bottom, sps.height);
//      }
//      if (vCheckingPreferredSize) {
//      sps.width = Math.max(ps.width + insets.left + insets.right, sps.width);
//      }
        return sps;
      } finally {
        stopCheckingSize();
      }
      //        return sps;
    }

    private void stopCheckingSize() {
      hCheckingPreferredSize = vCheckingPreferredSize = false;
    }

    private void startCheckingSize() {
      if (! vCheckingPreferredSize) {
        if (getVerticalScrollBarPolicy() != VERTICAL_SCROLLBAR_ALWAYS) {
          vCheckingPreferredSize = scrollableView == null ||
          !scrollableView.getScrollableTracksViewportHeight() ||
          getHorizontalScrollBarPolicy() == HORIZONTAL_SCROLLBAR_NEVER;
        }
      }
      if (! hCheckingPreferredSize) {
        if (getHorizontalScrollBarPolicy() != HORIZONTAL_SCROLLBAR_ALWAYS) {
          hCheckingPreferredSize = scrollableView == null ||
          !scrollableView.getScrollableTracksViewportWidth() ||
          getVerticalScrollBarPolicy() == VERTICAL_SCROLLBAR_NEVER;
        }
      }
    }

    protected String paramString() {
      String viewString = getViewport().getView().toString();
      return //super.paramString()+","+
      "view="+viewString;
    }

    public String toString() {
      return super.toString();
    }
    
  }

  public class AcceptAction extends AbstractAction {

    public AcceptAction() {
      super("Accept");
      putValue(Action.MNEMONIC_KEY, new Integer('A'));
    }

    public void actionPerformed(ActionEvent e) {
      for (Iterator it = fields.iterator(); it.hasNext();) {
        Field field = (Field) it.next();
        if (! field.isAcceptable()) {
          Component c = field.getComponent();
          UIManager.getLookAndFeel().provideErrorFeedback(c);
          JOptionPane.showMessageDialog(c,
              "Selection missing for mandatory field "+field.getField().getLabel(),
              "Mandatory Selection Missing...",
              JOptionPane.ERROR_MESSAGE);
          if (c instanceof JScrollPane) {
            c = ((JScrollPane) c).getViewport().getView();
          }
          c.requestFocus();
          return;
        }
      }
      canceled = false;
      dispose();
    }

  }

  public static class BooleanField extends Field {
    protected JCheckBox checkBox;

    public BooleanField(FormField f) {
      super(f);
      if (Boolean.valueOf(getFirstOption()).booleanValue()) {
        checkBox.setSelected(true);
      }
    }

    public void addToParent(Container parent) {
      parent.add(getComponent());
    }

    public Object getAnswer() {
      return new Boolean(checkBox.isSelected());
    }

    protected JComponent createComponent() {
      checkBox = new JCheckBox(getLabel().getText());
      checkBox.setMnemonic(getLabel().getDisplayedMnemonic());
      return checkBox;
    }
  }

  public static class SpringReference extends Spring {

    private static final Spring CONSTANT = Spring.constant(0);
    private Spring spring;
    private int value;

    public SpringReference() {
      this(CONSTANT);
    }
    
    public SpringReference copy() {
      return new SpringReference(spring);
    }
    
    public SpringReference(Spring spring) {
      this.spring = spring;
    }
    
    public int getMaximumValue() {
      return spring.getMaximumValue();
    }
    
    public int getMinimumValue() {
      return spring.getMinimumValue();
    }
    
    public int getPreferredValue() {
      return spring.getPreferredValue();
    }
    
    public int getValue() {
      return value == UNSET ? spring.getValue() : value;
    }
    
    public void setValue(int value) {
      this.value = value;
    }
    
    public SpringReference setReference(Spring spring) {
      this.spring = spring;
      return this;
    }
    
    public SpringReference setRefMax(Spring spring) {
      this.spring = Spring.max(this.spring, spring);
      return this;
    }
    
    public SpringReference setRefMin(Spring spring) {
      this.spring = Spring.minus(Spring.max(Spring.minus(this.spring), Spring.minus(spring)));
      return this;
    }

    public String toString() {
      return "SpringRef["+spring+"]";
    }
  }

  public static class ComponentSpring extends Spring implements Comparable {
    public static final int WIDTH = 0;
    public static final int HEIGHT = 1;
    private final JComponent comp;
    private final int orientation;
    private final int bound;

    protected int size = UNSET;
    public ComponentSpring(JComponent comp, int orientation) {
      this(comp, orientation, -1);
    }

    public ComponentSpring(JComponent comp, int orientation, int bound) {
      this.comp = comp;
      this.orientation = orientation;
      this.bound = bound > 0 ? bound : Integer.MAX_VALUE;
    }

    public int getMaximumValue() {
      return getSize(comp.getPreferredSize());
    }

    public int getMinimumValue() {
      return getSize(comp.getPreferredSize());
    }

    public int getPreferredValue() {
      return getSize(comp.getPreferredSize());
    }

    public int getValue() {
      return size != UNSET ? size : getPreferredValue();
    }

    public void setValue(int size) {
      this.size = size;
    }

    private int getSize(Dimension s) {
      switch (orientation) {
      case WIDTH:
        return Math.min(bound, s.width);
      case HEIGHT:
      default:
        return Math.min(bound, s.height);
      }
    }

    public String toString() {
      return "ComponentSpring["+(orientation == WIDTH ? "width" : "height")+": "+ getMinimumValue() + ", "
      + getPreferredValue() + ", " + getMaximumValue() + " on "
      + comp + "]";
    }

    public int compareTo(Object o) {
      return getValue() - ((ComponentSpring) o).getValue();
    }

    public boolean equals(Object obj) {
//      return super.equals(obj);
      return compareTo(obj) == 0;
    }

    public int hashCode() {
      return getValue();
    }
  }

  public static class Field  implements DocumentListener {
    protected JComponent component;
    protected JLabel label;
    protected FormField field;
    protected JTextComponent textField;

    public Field(FormField f) {
      field = f;
      String labelText = field.getLabel();
      label = new MnemonicLabel(labelText);
      if (field.isMandatory()) {
        int mnemonic = label.getDisplayedMnemonic();
        int index = label.getDisplayedMnemonicIndex();
        if (index >= 0) {
          String head = labelText.substring(0, index);
          char c = labelText.charAt(index);
          String tail = index + 1 < labelText.length() ? labelText.substring(index + 1) : "";
          labelText = head + "<u>"+c+"</u>" + tail;
        }
        labelText = "<html><body><font style='family: Dialog; weight: bold'>" + labelText;
        labelText += "&nbsp;(<font color=red>*</font>)</font></body></html>";
        label.setText(labelText);
        label.setDisplayedMnemonic(mnemonic);
      }
      component = createComponent();
      label.setLabelFor(component);
    }

    public void addToParent(Container parent) {
      parent.add(getLabel());
      parent.add(getComponent());
    }

    public boolean isAcceptable() {
      return ! field.isMandatory() || getAnswer() != null;
    }

    public Object getAnswer() {
      String text = textField.getText();
      return text == null || text.length() == 0 ? null : text;
    }

    public JComponent getComponent() {
      return component;
    }

    public FormField getField() {
      return field;
    }

    public String getFieldValue() {
      return null;
    }

    public JLabel getLabel() {
      return label;
    }

    public String getQuestionName() {
      return field.getName();
    }

    protected JComponent createComponent() {
      textField = new JTextField(20) {

        public Dimension getPreferredSize() {
          int maxTextLength = field.getMaxTextLength();
          if (maxTextLength > 0) {
            if (getColumns() > maxTextLength) {
              setColumns(maxTextLength);
            }
          }
          return super.getPreferredSize();
        }

      };
      textField.getDocument().addDocumentListener(this);
      return textField;
    }

    protected String getFirstOption() {
      Iterator it = field.getOptions();
      while (it != null && it.hasNext()) {
        return ((Option) it.next()).getValue();
      }
      return "";
    }

    public void changedUpdate(DocumentEvent e) {}

    public void insertUpdate(DocumentEvent e) {
      final int max;
      if ((max = field.getMaxSelection()) > 0) {
        final Document doc = e.getDocument();
        if (doc.getLength() > max) {
          UIManager.getLookAndFeel().provideErrorFeedback(getComponent());
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              try {
                doc.remove(max, doc.getLength() - max);
              } catch (BadLocationException e1) {
                e1.printStackTrace();
              }
            }
          });
        }
      }
    }

    public void removeUpdate(DocumentEvent e) {}

  } // field class

  public static class Form extends Properties {
    private LinkedHashSet keys = new LinkedHashSet();
    private static final String ANSWER = "answer.";

    public synchronized Object put(Object key, Object value) {
      keys.add(key);
      return super.put(key, value);
    }

    public synchronized Enumeration keys() {
      Set ks = keySet();
      keys.retainAll(ks);
      return Collections.enumeration(keys);
    }

    public void setAnswer(String variable, boolean b) {
      setProperty(ANSWER+variable, Boolean.toString(b));
    }

    public void setAnswer(String variable, List list) {
      String value;
      if (list == null || list.isEmpty()) {
        value = "none";
      }
      else {
        StringBuffer sb = new StringBuffer();
        for (Iterator it = list.iterator(); it.hasNext();) {
          sb.append(it.next());
          if (it.hasNext()) {
            sb.append(',');
          }
        }
        value = sb.toString();
      }
      setProperty(ANSWER + variable, value);
    }

    public void setAnswer(String variable, String string) {
      setProperty(ANSWER+variable, string);
    }

  }

  public static class ListField extends Field {
    Vector options;
    JComboBox comboBox;
    private static final Object unselected = new Object() { public String toString() { return "Please make a selection"; }};

    public ListField(FormField f) {
      super(f);
      Iterator it = field.getOptions();

      if (field.isMandatory()) {
        options.add(unselected);
      }
      while (it != null && it.hasNext()) {
        FormField.Option option = (FormField.Option) it.next();
        options.add(option.getValue());
        // TODO display option.getLabel()
      }
      if (options.size() > 0 && ! field.isMandatory()) {
        comboBox.setSelectedIndex(0);
      }
      else if (field.isMandatory()) {
        comboBox.setSelectedItem(unselected);
        comboBox.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            Object selectedItem = comboBox.getSelectedItem();
            if (selectedItem != unselected) {
              comboBox.removeItemListener(this);
              options.remove(unselected);
              comboBox.setSelectedItem(selectedItem);
            }
          }});
      }
    }

    public Object getAnswer() {
      Object selectedItem = comboBox.getSelectedItem();
      if (field.isMandatory() && (selectedItem == null || selectedItem == unselected)) {
        return null;
      }
      return Collections.singletonList(selectedItem);
    }

    protected JComponent createComponent() {
      comboBox = new JComboBox(options = new Vector());
      return comboBox;
    }
  }

  public static class MnemonicLabel extends JLabel {
    public MnemonicLabel(String text) {
      super(text, TRAILING);
    }

    public void setText(String text) {
      superSetText(text);
      if (text != null) {
        setDisplayedMnemonic(text.charAt(0));
      }
    }

    protected final void superSetText(String text) {
      super.setText(text);
    }
  }

  public static class MultiListField extends Field {
    private static final Object noneOfTheAbove = new Object() { public String toString() { return "None of the above"; } };

    private int noneIndex = -1;
    Vector options;
    JList list;

    public MultiListField(FormField f) {
      super(f);
      Iterator it = field.getOptions();

      while (it != null && it.hasNext()) {
        FormField.Option option = (FormField.Option) it.next();
        options.add(option.getValue());
        // TODO display option.getLabel()
      }
      if (field.isNoneOptionAvailable()) {
        noneIndex = options.size();
        options.add(noneOfTheAbove);
      }
      if (! field.isMandatory()) {
        list.setSelectedIndex(Math.max(0, noneIndex));
      }
      else {
        list.setSelectedIndex(-1);
      }
      list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      if (field.getMaxSelection() > 0 || field.isNoneOptionAvailable()) {
        list.addListSelectionListener(new ListSelectionListener() {
          int[] select = new int[field.getMaxSelection()];
          int[] clear = null;

          public void valueChanged(ListSelectionEvent e) {
            if (clear != null) {
              return;
            }
            if (select.length > 0) {
              int[] selected = list.getSelectedIndices();
              if (selected.length > select.length) {
                clear = new int[selected.length - select.length];
                for (int i = 0; i < selected.length; i++) {
                  if (i < select.length) {
                    select[i] = selected[i];
                  }
                  else {
                    clear[i - select.length] = selected[i];
                  }
                }
              }
            }
            if (clear != null || (noneIndex != -1
                && list.isSelectedIndex(noneIndex))) {
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  if (noneIndex != -1 && list.isSelectedIndex(noneIndex)) {
                    list.getSelectionModel().removeSelectionInterval(0, noneIndex - 1);
                    clear = null;
                  }
                  else if (clear != null) {
                    for (int i = 0; i < clear.length; i++) {
                      list.getSelectionModel().removeSelectionInterval(
                          clear[i], clear[i]);
                    }
                    clear = null;
                  }
                }
              });
            }
          }
        });
      }
    }

    public Object getAnswer() {
      if (field.isMandatory() && list.getSelectedIndex() == -1) {
        return null;
      }
      if (list.getSelectedValue() == noneOfTheAbove) {
        return Collections.EMPTY_LIST;
      }
      return Arrays.asList(list.getSelectedValues());
    }

    protected JComponent createComponent() {
      list = new JList(options = new Vector()) {
        public boolean getScrollableTracksViewportWidth() {
          return true;
        }

        public Dimension getPreferredScrollableViewportSize() {
          Dimension dimension = super.getPreferredScrollableViewportSize();
          return dimension;
        }

        public Dimension getPreferredSize() {
          Dimension dimension = super.getMinimumSize();
          return dimension;
        }

        public int getVisibleRowCount() {
          return Math.min(getModel().getSize(), super.getVisibleRowCount());
        }

//      public Dimension getPreferredSize() {
//      Dimension d = super.getPreferredSize();
//      d.height = options.size() * getFontMetrics(getFont()).getHeight();
////    getCellRenderer().getListCellRendererComponent(this, options.elementAt(0), 0, false, false).getPreferredSize().height;
//      return d;
//      }

      };
//    return new JScrollPane(list) {
//    public Dimension getMaximumSize() {
//    return getPreferredSize();
//    }

//    public Dimension getPreferredSize() {
//    Insets insets = getInsets();
//    Dimension ps = getViewport().getView().getPreferredSize();
//    Dimension sps = super.getPreferredSize();
////  Rectangle vbb = getViewportBorderBounds();
////  ps.width = Math.max(ps.width, vbb.width);
//    ps.height += insets.top + insets.bottom;
////  ps.width += insets.left + insets.right;
//    ps.width = sps.width;
//    return ps;
////  return sps;
//    }
//    };
      return new SmartSizeScrollPane(list,
          ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
  }
  public class MultiTextField extends Field {
    JTextArea textArea;

    public MultiTextField(FormField f) {
      super(f);
    }

    public Object getAnswer() {
      return textArea.getText();
    }

    protected JComponent createComponent() {
      textArea = new JTextArea(2, 20);
      textArea.setFont(new JTextField().getFont());
      return textArea;
    }
  }
  public static class ScrollablePanel extends JPanel implements Scrollable {

    private boolean tracksViewportHeight;
    private boolean tracksViewportWidth;

    public ScrollablePanel() {
      this(false, true);
    }

    public ScrollablePanel(boolean tracksViewportHeight, boolean tracksViewportWidth) {
      this(new FlowLayout(), tracksViewportHeight, tracksViewportWidth);
    }

    public ScrollablePanel(LayoutManager layout) {
      this(layout, false, true);
    }

    public ScrollablePanel(LayoutManager layout, boolean tracksViewportHeight, boolean tracksViewportWidth) {
      super(layout);
      this.tracksViewportHeight = tracksViewportHeight;
      this.tracksViewportWidth = tracksViewportWidth;
    }

    public Dimension getPreferredScrollableViewportSize() {
      return getPreferredSize();
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
        int orientation, int direction) {
      // Dimension size = getSize();
      Dimension vsize = visibleRect.getSize();
      return vsize.height;
    }

    public boolean getScrollableTracksViewportHeight() {
      return tracksViewportHeight;
    }

    public boolean getScrollableTracksViewportWidth() {
      return tracksViewportWidth;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
        int orientation, int direction) {
      Dimension size = getSize();
      Dimension vsize = visibleRect.getSize();
      FontMetrics fontMetrics = getFontMetrics(getFont());
      return (int) Math.ceil((size.getHeight() / vsize.getHeight())
          * fontMetrics.getHeight());
    }
  }
  protected class CancelAction extends AbstractAction {
    public CancelAction() {
      super("Cancel");
      putValue(Action.MNEMONIC_KEY, new Integer('C'));
    }

    public void actionPerformed(ActionEvent e) {
      dispose();
    }
  }
  static class FormField {

    public static final String TYPE_BOOLEAN = "singlecheckbox";
    public static final String TYPE_MULTI_CHECK = "multicheckbox";
    public static final String TYPE_LIST_SINGLE = "menu";
    public static final String TYPE_MULTILIST = "multicheckbox";
    public static final String TYPE_TEXTFIELD = "textfield";
    public static final String TYPE_TEXTBOX = "textbox";

    public static class Option {
      final String label;
      final String value;

      Option(String value) {
        this(null, value);
      }

      Option(String label, String value) {
        this.label = label;
        this.value = value;
      }

      public String getLabel() {
        return label;
      }

      public String getValue() {
        return value;
      }
    }
    private final String label;
    private final String type;
    private final String name;
    private final boolean mandatory;

    private Vector options = null;
    private int maxCount;
    private boolean noneOptionAvailable;

    public FormField(String type, String name, String text, boolean mandatory) {
      this.type = type;
      this.name = name;
      this.label = text;
      this.mandatory = mandatory;
    }

    public boolean isMandatory() {
      return mandatory;
    }

    public void addOption(String option) {
      if (options == null) {
        options = new Vector();
      }
      options.add(new Option(option));
    }

    public void addOption(String label, String option) {
      if (options == null) {
        options = new Vector();
      }
      options.add(new Option(label, option));
    }

    public String getLabel() {
      return label;
    }

    public int getMaxSelection() {
      return maxCount;
    }

    public int getMaxTextLength() {
      return maxCount;
    }

    public String getName() {
      return name;
    }

    public Iterator getOptions() {
      return options == null ? null : options.iterator();
    }

    public String getType() {
      return type;
    }

    public boolean isNoneOptionAvailable() {
      return noneOptionAvailable;
    }

    public int numOptions() {
      return options == null ? 0 : options.size();
    }

    public void setMaxSelection(int maxsel) {
      this.maxCount = maxsel;
    }

    public void setMaxTextSize(int maxsel) {
      this.maxCount = maxsel;
    }

    public void setNoneOption(boolean b) {
      noneOptionAvailable = b;
    }
  }

  public static class TextBoxField extends Field {

    public TextBoxField(FormField f) {
      super(f);
    }

    protected JComponent createComponent() {
      JTextArea textArea = new JTextArea() {
        public Dimension getPreferredScrollableViewportSize() {
//          Dimension size = super.getPreferredScrollableViewportSize();
//          int columns = getColumns();
//          int rows = getRows();
//          size = (size == null) ? new Dimension(400,400) : size;
//          size.width = (columns == 0) ? size.width : columns * getColumnWidth();
//          size.height = (rows == 0) ? size.height : rows * getRowHeight();
//          return size;
          return super.getPreferredScrollableViewportSize();
        }

        public Dimension getPreferredSize() {
          int maxTextLength = field.getMaxTextLength();
          if (maxTextLength <= 0) {
            maxTextLength = 200;
          }
          if (maxTextLength > 0) {
            double w = getColumnWidth();
            double h = getRowHeight();
            double prop = h / w;
            double s = Math.sqrt(maxTextLength);
            double cp = s * prop;
            double rp = s / prop;
            setRows((int) Math.ceil(rp / 2));
            setColumns((int) Math.ceil(cp / 2));
          }
          return super.getPreferredSize();
        }
        
      };
      textArea.setLineWrap(true);
      textArea.getDocument().addDocumentListener(this);
      textField = textArea;
      return new SmartSizeScrollPane(textArea,
          ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
  }

  private CancelAction cancelAction;
  private AcceptAction okAction;
  private String topCaption;

  protected boolean canceled = true;

  protected Vector fields = null;

  protected Properties form = null;

  protected JPanel mainPanel = null;

  protected Component buttonPanel;

  // public class FixedField extends Field {
  // public FixedField(FormField f) {
  // super(f);
  // textField.setText(f.getDescription());
  // textField.setEditable(false);
  // }
  //
  // public void addToParent(Container parent) {
  // String description = field.getDescription();
  // if (description != null && description.length() > 0) {
  // parent.add(getLabel());
  // parent.add(getComponent());
  // }
  // }
  //
  // }

  // public class HiddenField extends Field {
  // JPanel panel;
  //
  // public HiddenField(FormField f) {
  // super(f);
  // label.setText(null);
  // }
  //
  // public void addToParent(Container parent) {}
  //
  // protected JComponent createComponent() {
  // return panel = new JPanel();
  // }
  // }

  public CollectionMetaDataDialog(Component owner, String title,
      Properties configForm) {
    this((Frame) SwingUtilities.getWindowAncestor(owner), title, configForm, null);
  }

  public CollectionMetaDataDialog(Frame owner, String title,
      Properties configForm, String topCaption) {
    super(owner, title);
    form = configForm;
    this.topCaption = topCaption;
    init();
  }

  public CollectionMetaDataDialog(String title, Properties configForm, String topCaption) {
    this((Frame) null, title, configForm, topCaption);
  }

//public class PrivateTextField extends Field {
//JPasswordField password;

//public PrivateTextField(FormField f) {
//super(f);
//}

//protected JComponent createComponent() {
//return password = new JPasswordField(20);
//}

//public Object getAnswer() {
//char[] pword = password.getPassword();
//return new String(pword);
//}
//}

  public Form getAnswerForm() {
    Form answerForm = new Form();
    Iterator it = fields.iterator();
    while (it.hasNext()) {
      Field field = (Field) it.next();
      String questionName = field.getQuestionName();
      if (questionName != null && questionName != "") {
        Object answer = field.getAnswer();
        if (answer instanceof Boolean) {
          answerForm.setAnswer(questionName, ((Boolean) answer).booleanValue());
        }
        else if (answer instanceof java.util.List) {
          answerForm.setAnswer(questionName, (java.util.List) answer);
        }
        else {
          answerForm.setAnswer(questionName, (String) answer);
        }
      }
    }
    return answerForm;
  }

  public String getTopCaption() {
    return topCaption;
  }

  public synchronized boolean isCanceled() {
    return canceled;
  }

  private boolean getBooleanProperty(String key) {
    return Boolean.valueOf(form.getProperty(key)).booleanValue();
  }

  private int getPropertyInt(String key) {
    String intvalS = form.getProperty(key, Integer.toString(0));
    int intval = 0;
    try {
      intval = Integer.parseInt(intvalS);
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }
    return intval;
  }
  protected Component createButtonPanel() {
    JRootPane rootPane = getRootPane();
    InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = rootPane.getActionMap();
    cancelAction = new CancelAction();
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelAction
        .getValue(Action.NAME));
    actionMap.put(cancelAction.getValue(Action.NAME), cancelAction);
    JPanel buttonPanel = new JPanel();
    okAction = new AcceptAction();
    JButton okButton = new JButton(okAction);
    rootPane.setDefaultButton(okButton);
    buttonPanel.add(okButton);
    buttonPanel.add(new JButton(cancelAction));
    return buttonPanel;
  }

  protected void init() {
    setUndecorated(false);
    canceled = true;
    JPanel northPanel = new JPanel(new BorderLayout());
    String topCaption = getTopCaption();
    if (topCaption != null) {
      JLabel topCaptionLabel = new JLabel(topCaption);
      topCaptionLabel.setHorizontalTextPosition(SwingConstants.CENTER);
      northPanel.add(topCaptionLabel, BorderLayout.NORTH);
    }
    String instructions = form.getProperty(PROP_INSTRUCTIONS);
    JTextComponent instructionsTextComponent = null;
    if (instructions != null && instructions.length() > 0) {
      JTextArea instructionsTextArea = new JTextArea(instructions);
      instructionsTextArea.setWrapStyleWord(true);
      instructionsTextArea.setLineWrap(true);
      instructionsTextArea.setFont(new JLabel().getFont());
      instructionsTextComponent = instructionsTextArea;
    }
    else if ((instructions = form.getProperty(PROP_INSTRUCTIONS+".html")) != null && instructions.length() > 0) {
      JTextPane instructionsTextPane = new JTextPane() {
        public boolean getScrollableTracksViewportWidth() {
          return true;
        }
      };
      instructionsTextPane.setContentType("text/html");
      instructionsTextPane.setText(instructions);
      instructionsTextComponent = instructionsTextPane;
    }
    if (instructionsTextComponent != null) {
      instructionsTextComponent.setEditable(false);
//    instructionsTextComponent.setBackground(getBackground());
      northPanel.add(new JScrollPane(instructionsTextComponent), BorderLayout.CENTER);
    }
    mainPanel = new ScrollablePanel(false, false);
    SpringLayout springLayout = new SpringLayout();
    Container contentPane = getContentPane();
    mainPanel.setLayout(springLayout);

    JPanel debugBox = null;
    if (DEBUG > 0) {
      debugBox = new JPanel() {
        boolean painting = false;
        public void paint(Graphics g) {
          painting = true;
          try {
            super.paint(g);
          } finally {
            painting = false;
          }
        }
        public boolean isOpaque() {
          return painting; //super.isOpaque();
        }

        // public void invalidate() {
        // // super.invalidate();
        // mainPanel.invalidate();
        // }
        //
        // public void repaint(long tm, int x, int y, int width, int height) {
        // super.repaint(tm, x, y, width, height);
        // }
        //
        // public void repaint(Rectangle r) {
        //          super.repaint(r);
        //        }

      };
      Border b = null;
      for (int i = 0; i < 3; i++) {
        float[] f = { 0, 0, 0 };
//      f[i] = 1.0f;
//      Border cb = BorderFactory.createLineBorder(new Color(f[0], f[1], f[2], 0.5f));
        Color hsbColor = Color.getHSBColor(i * (1.0f / 3.0f), 1, 1);
        hsbColor.getColorComponents(f);
        Color color = new Color(f[0], f[1], f[2], 0.5f);
        Border cb = BorderFactory.createLineBorder(color);
        if (b == null) {
          b = cb;
        }
        else {
          b = BorderFactory.createCompoundBorder(cb, b);
        }
      }
      debugBox.setBorder(b);
      debugBox.setOpaque(false);
      debugBox.setBackground(new Color(0.0f, 0.0f, 0, 0.25f));
      mainPanel.add(debugBox);
    }
    // Iterator it = form.getFields();

    initFormFields();

    contentPane.setLayout(new BorderLayout());

    SpringReference height = new SpringReference();
    Spring checkWidth = null;
    Component[] components = mainPanel.getComponents();
    int space = 6;
    Font font = getFont();
    if (font == null) {
      font = UIManager.getFont("OptionPane.font");
    }
//  space = getFontMetrics(font).getHeight();
    Spring spacer = Spring.constant(space);
    for (int i = 0; i < components.length; i++) {
      JComponent comp = (JComponent) components[i];
      if (comp == debugBox) {
        continue;
      }
      SpringLayout.Constraints constraints = springLayout.getConstraints(comp);
      if (comp instanceof JLabel || comp instanceof AbstractButton) {
        height.setRefMax(constraints.getHeight());
      }
      if (comp instanceof JCheckBox) {
        constraints.setX(spacer);
        if (checkWidth == null) {
          JCheckBox checkBox = (JCheckBox) comp;
          String text = checkBox.getText();
          if (text == null) {
            continue;
          }
          int gap = checkBox.getIconTextGap();

          Icon icon = ((BasicRadioButtonUI) checkBox.getUI()).getDefaultIcon();

          Font cbFont = checkBox.getFont();
          FontMetrics fm = checkBox.getFontMetrics(cbFont);

          Rectangle iconR = new Rectangle();
          Rectangle textR = new Rectangle();
          Rectangle viewR = new Rectangle(Short.MAX_VALUE, Short.MAX_VALUE);

          SwingUtilities.layoutCompoundLabel(
              checkBox, fm, text, icon,
              checkBox.getVerticalAlignment(), checkBox.getHorizontalAlignment(),
              checkBox.getVerticalTextPosition(), checkBox.getHorizontalTextPosition(),
              viewR, iconR, textR, (text == null ? 0 : gap)
          );
          Insets insets = checkBox.getInsets();
          int w = textR.x + insets.left;
          checkWidth = Spring.constant(w);
        }
      }
    }
    if (checkWidth == null) {
      checkWidth = spacer;
    }
    Spring y = spacer;
    Spring x = checkWidth; //Spring.constant(0);
    Spring width = Spring.constant(0);
    Vector fieldComps = new Vector();
    SpringReference scrollWidth = new SpringReference(spacer);
    Vector scrollHeights = new Vector();
    Constraints debugConstraints = null;
    if (debugBox != null) {
      debugConstraints = new Constraints();
      debugConstraints.setY(Spring.constant(0));
      debugConstraints.setHeight(Spring.constant(Integer.MIN_VALUE));
      debugConstraints.setWidth(Spring.constant(0));
    }
    for (int i = 0; i < components.length; i++) {
      JComponent comp = (JComponent) components[i];
      if (comp == debugBox) {
        continue;
      }
      Constraints constraints = springLayout.getConstraints(comp);
      constraints.setHeight(height);
      constraints.setY(y);
      boolean newLine = true;
      if (comp instanceof JCheckBox) {
        x = Spring.sum(constraints.getWidth(), constraints.getX());
      }
      else if (comp instanceof JLabel) {
        constraints.setWidth(new ComponentSpring(comp, ComponentSpring.WIDTH));
//      constraints.setHeight(new ComponentSpring(comp, ComponentSpring.HEIGHT));
        constraints.setX(Spring.sum(checkWidth, spacer));
        x = Spring.sum(constraints.getWidth(), constraints.getX());
        newLine = false;
      }
      else if (comp instanceof JScrollPane) {
        constraints.setX(Spring.sum(x, spacer));
        constraints.setWidth(scrollWidth.setRefMax(new ComponentSpring(comp, ComponentSpring.WIDTH)));
        x = Spring.sum(constraints.getWidth(), constraints.getX());
        ComponentSpring heightSpring = new ComponentSpring(comp, ComponentSpring.HEIGHT);
        constraints.setHeight(new SpringReference(heightSpring));
        scrollHeights.add(heightSpring);
        fieldComps.add(comp);
      }
      else {
        constraints.setX(Spring.sum(x, spacer));
        constraints.setWidth(new ComponentSpring(comp, ComponentSpring.WIDTH));
        x = Spring.sum(constraints.getWidth(), constraints.getX());
        fieldComps.add(comp);
      }
      if (newLine) {
        y = Spring.sum(y, Spring.sum(constraints.getHeight(), spacer));
        width = Spring.max(Spring.sum(spacer, x), width);
        if (debugConstraints != null) {
          debugConstraints.setY(Spring.max(debugConstraints.getY(), Spring.minus(constraints.getY())));
          debugConstraints.setHeight(Spring.max(debugConstraints.getHeight(), Spring.sum(constraints.getY(), constraints.getHeight())));
          debugConstraints.setWidth(Spring.max(debugConstraints.getWidth(),
              width));
//        Spring.sum(constraints.getX(), constraints.getWidth())));
        }
      }
    }
    int scrollHeightsSum = 0;
    int scrollHeightsAvg = 0;
    TreeSet scrollHeightsSort = new TreeSet();
    for (Iterator it = scrollHeights.iterator(); it.hasNext();) {
      ComponentSpring key = (ComponentSpring) it.next();
      int value = key.getValue();
      scrollHeightsSum += value;
      scrollHeightsSort.add(new Integer(value));
    }
    if (! scrollHeights.isEmpty()) {
      scrollHeightsAvg = scrollHeightsSum / scrollHeights.size();
      SortedSet tailSet = scrollHeightsSort.tailSet(new Integer(scrollHeightsAvg));
      scrollHeightsAvg = ((Integer) tailSet.first()).intValue();
    }
    for (int i = 0; i < fieldComps.size(); i++) {
      JComponent comp = (JComponent) fieldComps.get(i);
      SpringLayout.Constraints constraints = springLayout.getConstraints(comp);
      constraints.setX(Spring.sum(width, Spring.minus(Spring.sum(constraints.getWidth(), spacer))));
      if (comp instanceof JScrollPane && !scrollHeights.isEmpty()) {
        SpringReference springHeight = (SpringReference) constraints.getHeight();
        springHeight.setRefMin(Spring.constant(scrollHeightsAvg));
//        Spring springHeight = constraints.getHeight();
//        constraints.setHeight(new SpringReference(springHeight).setRefMin(Spring.constant(scrollHeightsAvg)));
//        constraints.setHeight(Spring.minus(Spring.max(Spring.minus(constraints.getHeight()),
//            Spring.minus(scrollMaxCountSpring))));
      }
    }
    SpringLayout.Constraints parentConstraints = springLayout.getConstraints(mainPanel);

    parentConstraints.setConstraint(SpringLayout.SOUTH, y);
    parentConstraints.setConstraint(SpringLayout.EAST, width);

    if (debugBox != null) {
      Constraints c1 = springLayout.getConstraints(debugBox);
      c1.setX(Spring.sum(spacer, checkWidth));
      c1.setY(Spring.minus(debugConstraints.getY()));
//      c1.setHeight(debugConstraints.getHeight());
      c1.setHeight(parentConstraints.getConstraint(SpringLayout.SOUTH));
      c1.setWidth(Spring.sum(Spring.minus(Spring.sum(spacer, c1.getX())), debugConstraints.getWidth()));
    }

    buttonPanel = createButtonPanel();
    if (northPanel.getComponentCount() > 0) {
      contentPane.add(northPanel, BorderLayout.NORTH);
    }
    JScrollPane mainScrollPane = new SmartSizeScrollPane(mainPanel,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    contentPane.add(mainScrollPane, BorderLayout.CENTER);
    contentPane.add(buttonPanel, BorderLayout.SOUTH);
    System.out.println(".init(): packing");
    pack();
    System.out.println(".init(): packed");
    Rectangle maximumWindowBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    Rectangle bounds = getBounds();
    if (! maximumWindowBounds.contains(bounds)) {
      bounds = maximumWindowBounds.intersection(bounds);
      setBounds(bounds);
    }
    addComponentListener(new ComponentAdapter() {
      Rectangle oldBounds = getBounds();
      public void componentResized(ComponentEvent e) {
        Rectangle newBounds = getBounds();
        Point p = newBounds.getLocation();
        newBounds.setLocation(oldBounds.getLocation());
        newBounds = oldBounds.intersection(newBounds);
        newBounds.setLocation(p);
        if (! newBounds.isEmpty()) {
          setBounds(newBounds);
        }
      }});
  }

  private void initFormFields() {
    fields = new Vector();
    for (int i = 0; true; i++) {
      String prefix = PROP_PREFIX_QUESTION + i + DOT;
      if (!form.containsKey(prefix + PROP_TYPE)) {
        break;
      }
      String type = form.getProperty(prefix + PROP_TYPE);
      Field f = null;
      String name = form.getProperty(prefix + PROP_NAME);
      String text = form.getProperty(prefix + PROP_TEXT);
      boolean mandatory = getBooleanProperty(prefix + PROP_MANDATORY);
      FormField ff = new FormField(type, name, text, mandatory);
      ff.setMaxSelection(getPropertyInt(prefix+PROP_MAXSEL));
      ff.setMaxTextSize(getPropertyInt(prefix+PROP_MAXCHARS));
      String pprefix = prefix + VALUES + DOT;
      while (true) {
        String key = pprefix + ff.numOptions();
        if (!form.containsKey(key)) {
          break;
        }
        String value = form.getProperty(key);
        String label = form.getProperty(key + DOT + PROP_LABEL);
        ff.addOption(label, value);
      }
      if (type.equals(FormField.TYPE_LIST_SINGLE) || type.equals(FormField.TYPE_MULTILIST)) {
        if (type.equals(FormField.TYPE_LIST_SINGLE)) {
          f = new ListField(ff);
        }
        else if (type.equals(FormField.TYPE_MULTILIST)) {
          if (getBooleanProperty(prefix+PROP_NONE)) {
            ff.setNoneOption(true);
          }
          f = new MultiListField(ff);
        }
      }
      else if (type.equals(FormField.TYPE_BOOLEAN)) {
        f = new BooleanField(ff);
      }
      else if (type.equals(FormField.TYPE_TEXTFIELD)) {
        f = new Field(ff);
      }
      else if (type.equals(FormField.TYPE_TEXTBOX)) {
        f = new TextBoxField(ff);
      }
      else {
        continue;
      }
      fields.add(f);
      f.addToParent(mainPanel);
    }
  }

  protected synchronized void setCanceled(boolean canceled) {
    this.canceled = canceled;
  }

  /**
   * @param args
   * @throws IOException
   * @throws UnsupportedLookAndFeelException
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws ClassNotFoundException
   */
  public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
    String[] test_props = {
        "callisto.metadata.instructions.html=\\",
        "<html><body>\\",
        "<h2>Sample Instructions</h2>\\",
        "<h3>HTML Example</h3>\\",
        "<p>These are some example instructions in HTML.</p>\\",
        "<p>Please fill out the form below.</p>\\",
        "<p>Required field are indicated with <font color=red>*</font>.</p>\\",
        "</body></html>",

        "callisto.metadata.question.1.name=language",
        "callisto.metadata.question.1.type=menu",
        "callisto.metadata.question.1.text=Language",
        "callisto.metadata.question.1.mandatory=true",
        "callisto.metadata.question.1.values.0=English",
        "callisto.metadata.question.1.values.1=Arabic",
        "callisto.metadata.question.1.values.2=Chinese",
        "callisto.metadata.question.1.values.3=Pashto",

        "callisto.metadata.question.0.name=difficult",
        "callisto.metadata.question.0.type=singlecheckbox",
        "callisto.metadata.question.0.text=Difficult?",
//      "callisto.metadata.question.0.values.0=false",

        "callisto.metadata.question.2.name=easy",
        "callisto.metadata.question.2.type=singlecheckbox",
        "callisto.metadata.question.2.text=Easy?",
        "callisto.metadata.question.2.values.0=true",

        "callisto.metadata.question.8.name=name",
        "callisto.metadata.question.8.type=textfield",
        "callisto.metadata.question.8.mandatory=true",
        "callisto.metadata.question.8.text=Name:",
        "callisto.metadata.question.8.maxchars=25",

        "callisto.metadata.question.9.name=comments",
        "callisto.metadata.question.9.type=textbox",
        "callisto.metadata.question.9.mandatory=true",
        "callisto.metadata.question.9.text=Comments:",
        "callisto.metadata.question.9.maxchars=200",

        "callisto.metadata.question.3.name=topic_3_none",
        "callisto.metadata.question.3.type=multicheckbox",
//        "callisto.metadata.question.3.mandatory=true",
        "callisto.metadata.question.3.text=Topic 3 (no max):",
        "callisto.metadata.question.3.none=true",
        "callisto.metadata.question.3.values.0=travel",
        "callisto.metadata.question.3.values.1=meeting",
//        "callisto.metadata.question.3.values.2=acquisition",
//        "callisto.metadata.question.3.values.3=other",
//        "callisto.metadata.question.3.values.4=other2",
//        "callisto.metadata.question.3.values.5=other3",
//        "callisto.metadata.question.3.values.6=other4",
//        "callisto.metadata.question.3.values.7=other5",

        "callisto.metadata.question.4.name=topic_4_max2",
        "callisto.metadata.question.4.type=multicheckbox",
//        "callisto.metadata.question.4.mandatory=true",
        "callisto.metadata.question.4.text=Topic 4 (max 2):",
        "callisto.metadata.question.4.maxsel=2",
        "callisto.metadata.question.4.none=false",
        "callisto.metadata.question.4.values.0=travel",
        "callisto.metadata.question.4.values.1=meeting",
        "callisto.metadata.question.4.values.2=acquisition",
//        "callisto.metadata.question.4.values.3=other",
//        "callisto.metadata.question.4.values.4=other2",
//        "callisto.metadata.question.4.values.5=other3",
//        "callisto.metadata.question.4.values.6=other4",
//        "callisto.metadata.question.4.values.7=other5",

        "callisto.metadata.question.5.name=topic_5_max2",
        "callisto.metadata.question.5.type=multicheckbox",
        "callisto.metadata.question.5.text=Topic 5 (max 2):",
        "callisto.metadata.question.5.maxsel=2",
        "callisto.metadata.question.5.values.0=travel",
//        "callisto.metadata.question.5.values.1=meeting",
//        "callisto.metadata.question.5.values.2=acquisition",
//        "callisto.metadata.question.5.values.3=other",
//        "callisto.metadata.question.5.values.4=other2",
//        "callisto.metadata.question.5.values.5=other3",
//        "callisto.metadata.question.5.values.6=other4",
        "callisto.metadata.question.5.values.1=other5 is the name of the fish called fred",

        "callisto.metadata.question.6.name=topic_6",
        "callisto.metadata.question.6.type=multicheckbox",
        "callisto.metadata.question.6.text=Topic 6 (no max):",
        "callisto.metadata.question.6.values.0=travel",
        "callisto.metadata.question.6.values.1=meeting",
        "callisto.metadata.question.6.values.2=acquisition",
//        "callisto.metadata.question.6.values.3=other",
//        "callisto.metadata.question.6.values.4=other2",
//        "callisto.metadata.question.6.values.5=other3",
//        "callisto.metadata.question.6.values.6=other4",
//        "callisto.metadata.question.6.values.7=other5",

        "callisto.metadata.question.7.name=topic_7",
        "callisto.metadata.question.7.type=multicheckbox",
        "callisto.metadata.question.7.text=Topic 7 (max 2):",
        "callisto.metadata.question.7.maxsel=2",
        "callisto.metadata.question.7.none=true",
        "callisto.metadata.question.7.values.0=travel",
        "callisto.metadata.question.7.values.1=meeting",
        "callisto.metadata.question.7.values.2=acquisition",
        "callisto.metadata.question.7.values.3=other",
        "callisto.metadata.question.7.values.4=other2",
        "callisto.metadata.question.7.values.5=other3",
        "callisto.metadata.question.7.values.6=other4",
        "callisto.metadata.question.7.values.7=other5",
        "callisto.metadata.question.7.values.8=other6",
        "callisto.metadata.question.7.values.9=other7",
        "callisto.metadata.question.7.values.10=other8",
        "callisto.metadata.question.7.values.11=other9",
    };
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < test_props.length; i++) {
      sb.append(test_props[i]).append('\n');
    }
    ByteArrayInputStream bis = new ByteArrayInputStream(sb.toString().getBytes("US-ASCII"));
    Properties initProps = new Form();
    initProps.load(bis);
    initProps.store(System.out, "Test Properties");
//  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    System.out.println("CollectionMetaDataDialog.main(): creating dialog");
    CollectionMetaDataDialog dialog = new CollectionMetaDataDialog(CollectionMetaDataDialog.class.getName(), initProps, "No file");
    System.out.println("CollectionMetaDataDialog.main(): setting dialog to modal");
    dialog.setModal(true);
    System.out.println("CollectionMetaDataDialog.main(): showing dialog");
    dialog.setVisible(true);
    if (dialog.isCanceled()) {
      System.out.println("Canceled");
    }
    else {
      Form answer = dialog.getAnswerForm();
      answer.store(System.out, "Answer Form");
      main(args);
    }
    System.exit(0);
  }

}
