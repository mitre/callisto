
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
 * Copyright (c) 2006 The MITRE Corporation
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

import gov.nist.atlas.Annotation;
import gov.nist.atlas.type.AnnotationType;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.swing.TableSorter;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;

/**
 * Generalization of SimpleTypeAnnotEditor that has been copied to many tasks,
 * that can render and allow user to edit Entity annotations from the text, and
 * can be set up with any number of attribute ("type") value filters, which can
 * be applied conjunctively (AND or "match all") or disjunctively (OR or "match
 * any").
 * <p>
 * This particular version was extended from the version in the SuperNE task,
 * and carried the following note in the header comments:
 * <blockquote>
 * Modified by Sam to take multiple attributes. Still only one filter.
 * </blockquote>
 * Now it takes multiple filters as well.
 *
 *
 * @author <a href="mailto:gwilliam@mitre.org">Galen B. Williamson</a>
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 *
 */
public class MultiFilterTypeAnnotEditor extends JPanel implements JawbComponent {

  public static final String YES = "yes";
  public static final Insets ZERO_INSETS = new Insets(0, 0, 0, 0);

  public interface AnnotationFilterComponent extends AnnotationFilter, JawbComponent {
    public void setTable(MultiFilterTypeAnnotEditor table);
  }
  
  protected interface ValueAnnotationFilter extends AnnotationFilter {
    Object getFilterValue();
  }

  protected class TextMatchFilter extends FixedAnnotationFilter implements ActionListener, DocumentListener {
    private JTextField field;
    private JComboBox combo;

    protected TextMatchFilter(String typeName, String attrib, JTextField field, JComboBox combo) {
      super(typeName, attrib, null);
      this.field = field;
      this.combo = combo;
      combo.addActionListener(this);
      field.addActionListener(this);
      field.getDocument().addDocumentListener(this);
    }

    public void reset() {
      field.setText(null);
    }
    
    public boolean accept(Annotation annot) {
      if (super.accept(annot)) {
        String text = field.getText();
        if (text == null || text.length() == 0) {
          if (fieldSaveColor != null) {
            field.setBackground(fieldSaveColor);
            field.repaint();
            fieldSaveColor = null;
          }
          return true;
        }
        AWBAnnotation awbAnnot = (AWBAnnotation) annot;
        Object value = awbAnnot.getAttributeValue(attrib);
        if (value == null) {
          return true;
        }
        String string = value.toString();
        boolean regExact = false;
        switch (combo.getSelectedIndex()) {
        case 0:
          return string.toLowerCase().indexOf(text.toLowerCase()) >= 0;
        case 1:
          return text.equalsIgnoreCase(string);
        case 2:
          return string.toLowerCase().startsWith(text.toLowerCase());
        case 3:
          return string.toLowerCase().endsWith(text.toLowerCase());
        case 4:
          regExact = true;
          // fall-through okay
        case 5: {
          try {
            Pattern pattern = text.length() > 0 ? Pattern.compile(text, Pattern.CASE_INSENSITIVE) : null;
            if (fieldSaveColor != null) {
              field.setBackground(fieldSaveColor);
              field.repaint();
              fieldSaveColor = null;
            }
            if (pattern == null)
              return true;
            Matcher matcher = pattern.matcher(string);
            return regExact ? matcher.matches() : matcher.find();
          } catch (RuntimeException e) {
            if (fieldSaveColor == null) {
              fieldSaveColor = field.getBackground();
              field.setBackground(Color.RED);
              field.repaint();
            }
            return false;
          }
        }
        }
      }
      return false;
    }

    Color fieldSaveColor = null;
    
    protected void update() {
      updateAnnotationFilter();
    }

    public void actionPerformed(ActionEvent e) {
      update();
    }

    public void changedUpdate(DocumentEvent e) {
      update();
    }

    public void insertUpdate(DocumentEvent e) {
      update();
    }

    public void removeUpdate(DocumentEvent e) {
      update();
    }
  }

  private static Map modelsMap = new HashMap();
  public static Map MODELS = Collections.unmodifiableMap(modelsMap); 
  
  public final Set getDynamicFilterCBModels(String typeName, String attributeName) {
    String key = typeName + '\0' + attributeName;
    Set s = null;
    synchronized (modelsMap) {
      s = (Set) modelsMap.get(key);
      if (s == null) {
        modelsMap.put(key, s = new LinkedHashSet());
      }
    }
    return s;
  }
  
  private final void addDynamicFilterCBModel(String typeName, String attributeName, DynamicFilterCBModel model) {
    synchronized (modelsMap) {
      getDynamicFilterCBModels(typeName, attributeName).add(model);
    }
  }

  public final class DynamicFilterCBModel extends AbstractListModel implements ComboBoxModel, ValueAnnotationFilter {
    private static final String ALL = "<ALL>";
    
    private JawbDocument jawbDoc = null;
    private String attrib;
    private int selectedIndex = 0;
    private LinkedHashSet valuesSet = new LinkedHashSet();
    private Vector values = new Vector();
    protected final String typeName;
    private boolean resettable;
    
    protected DynamicFilterCBModel(String typeName, String attrib) {
      this.typeName = typeName;
      this.attrib = attrib;
      addDynamicFilterCBModel(typeName, attrib, this);
    }

    private class Filter implements ValueAnnotationFilter {

      private String value;

      private Filter(Object value) {
        this.value = value == null ? null : ""+value;
      }

      public boolean accept(Annotation annot) {
        return DynamicFilterCBModel.this.accept(annot);
      }

      public String getDescription() {
        return toString();
      }

      public String toString() {
        return value == null ? ALL : value;
      }

      public boolean equals(Object o) {
        if (o != null && o instanceof Filter) {
          String v = ((Filter) o).value;
          if (value == null)
            return value == v;
          return value == v || value.equals(v);
        }
        return false;
      }

      public Object getFilterValue() {
        return value;
      }

    }

    public Object getElementAt(int index) {
      if (index == -1) {
        return null;
      }
      if (index == 0) {
        return new Filter(null);
      }
      return new Filter(values.elementAt(index - 1));
    }

    public int getSize() {
      return values.size() + 1;
    }

    public Object getSelectedItem() {
      return getElementAt(selectedIndex);
    }

    public String getFilterAttribute() {
      return attrib;
    }
    
    public void setSelectedItem(Object anItem) {
      int index = -1;
      if (anItem != null) {
        if (ALL == anItem || ALL.equals(anItem)) {
          index = 0;
        }
        else {
          if (anItem instanceof Filter) {
            anItem = ((Filter) anItem).value;
            if (anItem == null) {
              index = 0;
            }
          }
          if (index == -1) {
            index = values.indexOf(anItem);
            if (index != -1) {
              index++;
            }
          }
        }
      }
      if (index != selectedIndex) {
        selectedIndex = index;
        fireContentsChanged(this, -1, -1);
      }
    }

    public boolean accept(Annotation annot) {
      if (jawbDoc != getJawbDocument()) {
        jawbDoc = getJawbDocument();
        values.clear();
        valuesSet.clear();
        setSelectedItem(ALL);
      }
      AWBAnnotation awbAnnot = (AWBAnnotation) annot;
      AnnotationType type = awbAnnot.getAnnotationType();
      boolean accept = typeName.equals(type.getName());
      if (accept) {
        Object value = selectedIndex <= 0 ? null : values.elementAt(selectedIndex - 1);
        Object attributeValue = awbAnnot.getAttributeValue(attrib);
        if (attributeValue != null && valuesSet.add(attributeValue)) {
          values.add(attributeValue);
          int added = values.size();
          fireIntervalAdded(attributeValue, added, added);
        }
        else if (value != null && accept) {
          accept = value.equals(attributeValue);
        }
      }
      return accept;
    }

    public String getDescription() {
      return null;
    }

    public Object getFilterValue() {
      return selectedIndex == 0 ? null : values.elementAt(selectedIndex - 1);
    }

    public boolean isResettable() {
      return resettable;
    }

    public void setResettable(boolean resettable) {
      this.resettable = resettable;
    }
  }

  protected static final String[] MATCH_TYPES = new String[] {
    "Substring",
    "Exact",
    "Prefix",
    "Suffix",
    "Regex",
    "Regex All",
  };

  public static final String MATCH_ALL = "all";

  public static final String MATCH_ANY = "any";

  public static final String[] FILTER_COMBINERS = new String[] { MATCH_ANY, MATCH_ALL };

  private static final int DEBUG = 0;

  protected final TaskToolKit toolkit;

  public AnnotationTable getAnnotationTable() {
    return table;
  }

  public EnhancedAnnotationTableModel getAnnotationTableModel() {
    TableModel tableModel = getTable().getModel();
    while (tableModel instanceof TableSorter) {
      TableSorter sorter = (TableSorter) tableModel;
      tableModel = sorter.getModel();
    }
    return (EnhancedAnnotationTableModel) tableModel;
  }

  protected AnnotationTable table = null;
  protected JScrollPane scrollPane = null;
  protected final boolean useSelectionModel;

  protected final String typeName;

  protected JComboBox[] filterComboBoxes = {};
  protected AnnotationFilter[] fixedFilters = {};

  protected final int idNum = idCount++;

  private static int idCount = 0;
  public String toString () { return "MultiFilterTypeAnnotEd["+idCount+"] "; }


  /**
   * Construct a {@link MultiFilterTypeAnnotEditor} with no attribute-value filters.
   * @param toolkit the {@link TaskToolKit} creating this {@link MultiFilterTypeAnnotEditor}
   * @param typeName the {@link AnnotationType} name
   * @param attributesAndHeadings as in {@link #MultiFilterTypeAnnotEditor(TaskToolKit, String, Object[][], Object[])}
   */
  public MultiFilterTypeAnnotEditor(TaskToolKit toolkit,
      String typeName,
      Object[][] attributesAndHeadings) {
    this(toolkit, typeName, attributesAndHeadings, null, false);
  }

  public MultiFilterTypeAnnotEditor(TaskToolKit toolkit,
      String typeName,
      Object[][] attributesAndHeadings, boolean useSelectionModel) {
    this(toolkit, typeName, attributesAndHeadings, null, useSelectionModel);
  }
  
  /**
   * Construct a {@link MultiFilterTypeAnnotEditor} with a specified set of
   * attributes and headings (columns), and a specified set of attribute-value
   * filters.
   * <p>
   * The <code>attributesAndHeadings</code> parameter should be a
   * two-dimensional {@link String} array with 1 sub-array for each column, and
   * where each sub-array has 2, 3, or 4 elements:
   * <ol>
   * <li>The annotation type's attribute name for the column's values
   * <li>The human-visible name to appear in the column's header
   * <li>(Optional) "yes" or "no" (default is "no") to indicate editability of
   * the column
   * <li>(Optional) "yes" or "no" (default is "no") to indicate whether the
   * column is binary valued (and should be rendered as a check box)
   * </ol>
   * For example:
   *
   * <pre>
   *             new String[][] {
   *                          {
   *                              &quot;TextExtent&quot; , &quot;Text&quot; , // just the text extent of the annotation, not editable
   *                          } , {
   *                              &quot;attribute1&quot; , &quot;Heading 1&quot; , &quot;no&quot; // value of &quot;attribute1&quot;, not editable
   *                          } , {
   *                              &quot;attribute2&quot; , &quot;Heading 2&quot; , &quot;yes&quot; // value of &quot;attribute2&quot;, editable
   *                          } , {
   *                              &quot;attribute3&quot; , &quot;Heading 3&quot; , &quot;no&quot; , &quot;yes&quot; // value of &quot;attribute3&quot;, not editable, but binary-valued
   *                          } , {
   *                              &quot;attribute4&quot; , &quot;Heading 4&quot; , &quot;yes&quot; , &quot;yes&quot; // value of &quot;attribute4&quot;, editable and binary-valued
   *                          } ,
   *                      };
   * </pre>
   *
   * <p>
   * The <code>filterValueLists</code> parameter should be a two-dimensional
   * {@link String} array with <code>n + 1</code> sub-arrays, where the first
   * sub-array is of length <code>2n</code>, and specifies the filter
   * attributes and labels, and the remaining <code>n</code> sub-arrays are of
   * arbitrary length, and specify the possible filter values for each of the
   * specified attributes. The first sub-array consists of alternating sequences
   * of attribute name and filter label. The filter value lists that follow may
   * contain <code>null</code> elements, which will be displayed as
   * &quot;&lt;ALL&gt;&quot;, and, when selected, will prevent that filter from
   * participating in the filtering of the annotations displayed in the table,
   * regardless of the match mode selected ("match any" or "match all").
   *
   * @param toolkit the {@link TaskToolKit} creating this {@link MultiFilterTypeAnnotEditor}
   * @param typeName the {@link AnnotationType} name
   * @param attributesAndHeadings see above
   * @param filterValueLists see above
   */
  public MultiFilterTypeAnnotEditor(TaskToolKit toolkit,
      String typeName,
      Object[][] attributesAndHeadings,
      Object[] filterValueLists) {
    this(toolkit, typeName, attributesAndHeadings, filterValueLists, false);
  }

  public MultiFilterTypeAnnotEditor(TaskToolKit toolkit,
      String typeName,
      Object[][] attributesAndHeadings,
      Object[] filterValueLists, boolean useSelectionModel) {
    this.toolkit = toolkit;
    this.typeName = typeName;
    this.useSelectionModel = useSelectionModel;
    Task task = toolkit.getTask();

    AnnotationType type = task.getAnnotationType (typeName);
    if (type == null)
      throw new IllegalArgumentException ("No such AnnotationType: "+typeName);
    init (task, type, attributesAndHeadings, filterValueLists);
    setName(typeName);
  }

  protected void init (Task task, AnnotationType type, Object[][] attributesAndHeadings,
      Object[] filterValueLists) {

    // layout manager
    setLayout (new BorderLayout());
    AnnotationTableModel atm = null;
    Object[] attributes = new Object[attributesAndHeadings.length];
    String[] headings = new String[attributesAndHeadings.length];
    boolean[] editable = new boolean[attributesAndHeadings.length];
    boolean[] booleanColumns = new boolean[attributesAndHeadings.length];

    // Populate the arrays of attributes and headings and editable.

    for (int i = 0; i < attributesAndHeadings.length; i++) {
      attributes[i] = attributesAndHeadings[i][0];
      headings[i] = (String) attributesAndHeadings[i][1];
      if (attributesAndHeadings[i].length > 2 && 
          YES.equalsIgnoreCase((String) attributesAndHeadings[i][2])) {
        editable[i] = true;
      } else {
        editable[i] = false;
      }
      if (attributesAndHeadings[i].length > 3 && 
          YES.equalsIgnoreCase((String) attributesAndHeadings[i][3])) {
        booleanColumns[i] = true;
      } else {
        booleanColumns[i] = false;
      }
    }

    // Filter choosers
    if (filterValueLists != null && filterValueLists.length > 1) {
      filterComboBoxes = new JComboBox[filterValueLists.length - 1];
      fixedFilters = new AnnotationFilter[filterValueLists.length - 1];
      JPanel filteringPanel = new JPanel();
      filteringPanel.setLayout(new GridBagLayout());
//      filteringPanel.setLayout(new DebugGBL());
      JLabel filteringLabel = new JLabel("Filtering:  ");
      addFilterPanelComponent(filteringLabel, filteringPanel, -1);
      JPanel combinerPanel = null;
      Vector filterPanels = new Vector();
      JComboBox firstFilterComboBox = null;
      Vector filterValueAttrHeadings = new Vector();
      Stack fvs = new Stack();
      fvs.push(filterValueLists[0]);
      while (! fvs.isEmpty()) { // this loop just flattens out any nested arrays in the headings/attributes list (convenience feature)
        Object o = fvs.pop();
        if (o instanceof Object[]) {
          Object[] os = (Object[]) o;
          for (int i = os.length - 1; i >= 0; i--) {
            fvs.push(os[i]);
          }
        }
        else if (o instanceof String || o == null) {
          filterValueAttrHeadings.add(o);
        }
        else {
          throw new RuntimeException("Invalid filter attribute or heading type ("+o.getClass()+") for value: "+o);
        }
      }
      String[] filterValueHeadings = (String[]) filterValueAttrHeadings.toArray(new String[0]);
      int filterCombos = 0;
      for (int i = 0; i < filterValueLists.length - 1; i++) {
        String labelText = filterValueHeadings[(2 * i) + 1];
        if (labelText != null) {
          labelText = " "+labelText+": ";
        }
        Object[] filterTypes = null;
        Object o = filterValueLists[i + 1];
        // if (filterValueLists[i + 1] instanceof JawbComponent) {
        //
        // }
        // else
        if (o != null) {
          if (o instanceof Object[]) {
            filterTypes = (Object[]) o;
          }
          else if (o instanceof AnnotationFilterComponent) {
            AnnotationFilterComponent c = (AnnotationFilterComponent) o;
            fixedFilters[i] = c;
            JPanel p = createFilterPanel(new Component[] {
                labelText == null || labelText.length() == 0 ? null : new JLabel(labelText),
                c.getComponent(),
            }, SwingConstants.LEFT);
            filterPanels.add(p);
            c.setTable(this);
            continue;
          }
          else if (o instanceof AnnotationFilter) {
            fixedFilters[i] = (AnnotationFilter) o;
            continue;
          }
        }
        if (o == null || filterTypes != null) {
          String attribute = filterValueHeadings[2 * i];
          JPanel p = initFilter(type, filterTypes, filteringPanel,
              labelText, i,
              attribute);
          if (p != null) {
            filterPanels.add(p);
            if (filterComboBoxes[i] != null) {
              filterCombos++;
            }
          }
        }
      }
      if (filterCombos > 1) {
        setFilterCombiner(new JComboBox(FILTER_COMBINERS));
        combinerPanel = createFilterPanel(new JComponent[] {
            new JLabel(" Match on: ") , getFilterCombiner()
        });
        filterPanels.insertElementAt(combinerPanel, 0);
        if (getFilterCombiner() != null) {
          getFilterCombiner().addActionListener(
              new FilterAction(firstFilterComboBox));
        }
      }
      for (Iterator it = filterPanels.iterator(); it.hasNext();) {
        JPanel panel = (JPanel) it.next();
//        filteringPanel.add(panel);
        addFilterPanelComponent(panel, filteringPanel, -1);
      }
      if (filteringPanel.getComponentCount() > 2) {
        add(filteringPanel, BorderLayout.NORTH);
        this.filteringPanel = filteringPanel;
      }
    }

    masterFilter = createMasterAnnotationFilter();

    // create and add table (with multiple column sorting)
    atm = createTableModel(task, type, attributes, headings, booleanColumns);

    table = createTable(atm);
    table.setName(typeName);
    table.setAutoResizeMode(table.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    for (int i = 0; i < editable.length; i++) {
      if (!editable[i])
        table.setColumnEditable (i, false);
    }
    TableColumnModel columnModel = table.getColumnModel();
    JTableHeader tableHeader = getTable().getTableHeader();
    for (int i = 0; i < booleanColumns.length; i++) {
      if (booleanColumns[i]) {
        TableColumn column = columnModel.getColumn(i);
        column.sizeWidthToFit();
        column.setMaxWidth(column.getPreferredWidth());
        column.setMinWidth(column.getPreferredWidth());
      }
    }

    scrollPane = createScrollPane();
    if (scrollPane != null) {
      add (scrollPane, BorderLayout.CENTER);
    }
    else {
      add(table, BorderLayout.CENTER);
    }
  }

  private int filterGridX = 0;
  private GridBagConstraints labelGBC = null;
  private JLabel fpLabel = null;

  protected void addFilterPanelComponent(JComponent comp, JPanel filteringPanel, int gridx) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.LAST_LINE_END;
    gbc.gridwidth = 1;
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridx = gridx == -1 ? filterGridX++ : gridx;
    gbc.insets = ZERO_INSETS;
//    gbc.gridx = GridBagConstraints.RELATIVE;
    gbc.gridy = 1;
    gbc.gridheight = 1;
    boolean filler = false;
    boolean fullHeight = false;
    if (comp instanceof JPanel) {
      Component[] cs = comp.getComponents();
      if (cs.length == 0) {
        filler = true;
//        comp.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
//        gbc.gridx = GridBagConstraints.RELATIVE;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = 1;
        gbc.weighty = 1.0;
      }
      else {
        Stack s = new Stack();
        s.push(cs);
        while (! fullHeight && ! s.isEmpty()) {
          cs = (Component[]) s.pop();
          for (int i = 0; i < cs.length; i++) {
            if (cs[i] instanceof JTextField) {
              fullHeight = true;
              gbc.weighty = 0.0;
              gbc.gridy = 0;
              gbc.gridheight = 2;
              break;
            }
            if (cs[i] instanceof Container) {
              Container c = (Container) cs[i];
              s.push(c.getComponents());
            }
          }
        }
      }
    }
    if (! fullHeight && ! filler) { //comp instanceof JLabel) {
      if (fpLabel != null) {
        GridBagLayout gl = (GridBagLayout) filteringPanel.getLayout();
        GridBagConstraints g = gl.getConstraints(fpLabel);
        g.weighty = 0.0;
        g.weightx = 1.0;
        g.gridx = 0;
        ((GridBagLayout) filteringPanel.getLayout()).setConstraints(fpLabel, g);
        fpLabel = null;
      }
      addFilterPanelComponent(new JPanel(), filteringPanel, gbc.gridx);
      if (comp instanceof JLabel) {
        gbc.weighty = 1.0;
        if (fpLabel == null) {
          fpLabel = (JLabel) comp;
        }
        else {
        }
        gbc.anchor = GridBagConstraints.LINE_END;
//        comp.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
//        comp.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
        gbc.fill = GridBagConstraints.VERTICAL;
      }
      else {
        gbc.fill = GridBagConstraints.HORIZONTAL;
      }
      gbc.gridheight = 1;
    }
    filteringPanel.add(comp, gbc);
  }


  protected JScrollPane createScrollPane() {
    return new JScrollPane (table);
  }


  protected AnnotationTable createTable(AnnotationTableModel atm) {
    return new AnnotationTable (toolkit, atm);
  }

  protected EnhancedAnnotationTableModel createTableModel(Task task, AnnotationType type, Object[] attributes, String[] headings, boolean[] booleanColumns) {
    if (useSelectionModel) {
      return new SelectionAnnotationTableModel(task, type, attributes, headings, booleanColumns);
    }
    else {
      return new EnhancedAnnotationTableModel(task, type, attributes, headings, booleanColumns);
    }
  }

  protected JPanel createFilterPanel(JComponent[] components) {
    return createFilterPanel(components, SwingConstants.LEFT);
  }

  protected JPanel createFilterPanel(Component[] components, int align) {
    LayoutManager layout = null;
    Object[] constraints = new Object[components.length];
    if (align == SwingConstants.LEFT) {
      layout = new FlowLayout(FlowLayout.CENTER, 4, 4);
    }
    else if (align == SwingConstants.TOP) {
      layout = new GridBagLayout();
//      layout = new DebugGBL();
      int gridx = 0;
      for (int i = 0; i < components.length; i++) {
        if (components[i] == null) {
          continue;
        }
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.gridx = gridx++;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(4, 2, 4, 2);
        if (i == 0) {
          gbc.insets.left = 4;
          gbc.weightx = 1.0;
          gbc.weighty = 0.0;
          gbc.fill = GridBagConstraints.HORIZONTAL;
          gbc.anchor = GridBagConstraints.FIRST_LINE_END;
        }
        else {
          gbc.fill = GridBagConstraints.VERTICAL;
          gbc.weightx = 0.0;
          gbc.weighty = 1.0;
          if (i == components.length - 1) {
            gbc.insets.right = 4;
          }
          if (components[i] instanceof Container
              && ((Container) components[i]).getComponentCount() > 1) {
            gbc.gridy = 0;
            gbc.gridheight = 2;
          }
        }
        constraints[i] = gbc;
      }
    }
    JPanel filterPanel = new JPanel(layout);
    filterPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    for (int i = 0; i < components.length; i++) {
      if (components[i] != null) {
        filterPanel.add(components[i], constraints[i]);
      }
    }
//    typePanel.add(filterPanel);
    return filterPanel;
  }

  private boolean resettingFilters = false;

  protected static class ParentAnnotationFilter implements AnnotationFilter {

    protected AnnotationFilter parent;

    public ParentAnnotationFilter(AnnotationFilter parent) {
      this.parent = parent;
    }

    public boolean accept(Annotation annot) {
      return parent.accept(annot);
    }

    public String getDescription() {
      return null;
    }

  }

  protected class MasterAnnotationFilter implements AnnotationFilter {
    public boolean accept(Annotation annot) {
      AWBAnnotation awbAnnot = (AWBAnnotation) annot;
      AnnotationType type = awbAnnot.getAnnotationType();
      boolean accept = typeName.equals(type.getName());
      if (!accept) {
        return false;
      }
      if (fixedFilters != null) {
        for (int i = 0; i < fixedFilters.length; i++) {
          AnnotationFilter f = fixedFilters[i];
          if (f != null && !f.accept(annot)) {
            return false;
          }
        }
      }
      boolean all = getFilterCombiner() != null && getFilterCombiner().getSelectedItem() == MATCH_ALL;
      int accepts = 0;
      int tried = 0;
      for (int i = 0; i < filterComboBoxes.length; i++) {
        JComboBox c = filterComboBoxes[i];
        ValueAnnotationFilter f;
        if (c != null
            && ((f = (ValueAnnotationFilter) c.getSelectedItem()) != null)) {
          tried++;
          if (! f.accept(annot)) {
            if (all) {
              return false;
            }
            else if (f.getFilterValue() == null) {
              accepts++;
            }
          } else {
            if (! all) {
              return true;
            }
          }
        }
      }
      return all || accepts == tried;
    }

    public String getDescription() {
      return null;
    }
  }

  protected class FilterAction extends AbstractAction {
    private final JComboBox comboBox;

    protected FilterAction(JComboBox box) {
      this.comboBox = box;
    }

    public void actionPerformed(ActionEvent e) {
//      if (isResettingFilters())
//        return;
//      setResettingFilters(true);
//      try {
//        Object filter = comboBox.getSelectedItem();
//        table.setAnnotationFilter((AnnotationFilter) filter);
//      }
//      finally {
//        setResettingFilters(false);
//      }
//    }
      updateAnnotationFilter();
    }
  }

  public static class EnhancedAnnotationTableModel extends AnnotationTableModel {

    protected boolean[] booleanColumns;

    public EnhancedAnnotationTableModel(Task task, AnnotationType type, Object[] columnSrc,
        String[] headings, boolean[] booleanColumns) {
      super(task, type, columnSrc, headings);
      this.booleanColumns = booleanColumns;
    }

    /**
    *  Overridden for pseudo-boolean columns
    */
    public Object getValueAt (int row, int col) {
      Object oval = super.getValueAt (row, col);
      if (booleanColumns != null && booleanColumns[col]) {
        if (oval instanceof Boolean) {
          return oval;
        }
        String val = (String)oval;
        if (val != null && val.equals("YES")) {
          return Boolean.TRUE;
        } else {
          return Boolean.FALSE;
        }
      }
      return oval;
    }


   /**
    *  Overridden for pseudo-boolean columns
    */
    public void setValueAt(Object value, int row, int col) {
      if (booleanColumns != null && booleanColumns[col] && value instanceof Boolean) {
        Object oval = super.getValueAt(row, col);
        if (oval instanceof String) {
          if (((Boolean) value).booleanValue()) {
            value = "YES";
          }
          else {
            value = "";
          }
        }
      }
      super.setValueAt(value, row, col);
    }

    public Class getColumnClass (int col) {
      if (booleanColumns != null && booleanColumns[col]) {
        return Boolean.class;
      }
      return super.getColumnClass(col);
    }

  }

  /**
   * Get the AnnotationTable this widget supports.
   */
  public AnnotationTable getTable () {
    return table;
  }

  /***********************************************************************/
  /* Implementing JawbComponent */
  /***********************************************************************/

  public void setJawbDocument (JawbDocument doc) {
    table.setJawbDocument (doc);
//    if (filterComboBoxes != null) {
//      for (int i = 0; i < filterComboBoxes.length; i++) {
//        if (filterComboBoxes[i] != null && filterComboBoxes[i].getItemCount() == 1) {
//          filterComboBoxes[i].setSelectedIndex(0);
//        }
//      }
//    }
    updateAnnotationFilter();
  }


  public void updateAnnotationFilter() {
    if (! isResettingFilters() && table != null && masterFilterWrapper != null) {
      table.setAnnotationFilter(masterFilterWrapper);
    }
  }

  private final AnnotationFilter masterFilterWrapper = new AnnotationFilter() {

    public boolean accept(Annotation annot) {
      if (! isResettingFilters() && masterFilter != null) {
        setResettingFilters(true);
        try {
          return masterFilter.accept(annot);
        } finally {
          setResettingFilters(false);
        }
      }
      return false;
    }

    public String getDescription() {
      return null;
    }
    
  };
  
  protected AnnotationFilter masterFilter;

  protected AnnotationFilter createMasterAnnotationFilter() {
    return new MasterAnnotationFilter();
  }

  public JawbDocument getJawbDocument () {
    return table.getJawbDocument ();
  }

  public Set getSelectedAnnots () {
    return table.getSelectedAnnots ();
  }

  public Component getComponent () {
    return this;
  }

  protected JPanel initFilter(AnnotationType type, Object[] filterTypes,
      JPanel filterPanel, String labelText, int n, String attr) {
    int align = SwingConstants.LEFT;
    JComponent filterComponent = null;
    JComponent labelIsFor = null;
    JPanel panel = null;
    // filterTypes == null ==> text field filter
    if (filterTypes == null) {
      align = SwingConstants.TOP;
      JPanel matchPanel = new JPanel(new GridLayout(0, 1, 2, 2));
      filterComponent = matchPanel;
      GridBagConstraints gbc;
      final JTextField matchField = new JTextField(15);
      labelIsFor = matchField;
      matchPanel.add(matchField);
      final JComboBox matchTypeCombo = new JComboBox(MATCH_TYPES);
      matchPanel.add(matchTypeCombo);
      fixedFilters[n] = new TextMatchFilter(typeName, attr, matchField, matchTypeCombo);
    }
    // filterTypes.length > 1 ==> combo box with given list of filter values
    // filterTypes.length == 0 ==> combo box with dynamic list of filter values based on what values were found
    else if (filterTypes.length > 1 || filterTypes.length == 0) {
      JComboBox comboBox = new JComboBox ();
      if (filterTypes.length == 0) {
        comboBox.setModel(new DynamicFilterCBModel(typeName, attr));
      }
      else {
        for (int i = 0; i < filterTypes.length; i++) {
          comboBox.addItem(new AttributeValueCombiningFilter(attr, filterTypes[i],
              comboBox));
        }
      }
      comboBox.addActionListener(new FilterAction(comboBox));
      labelIsFor = filterComponent = filterComboBoxes[n] = comboBox;
    }
    // filterTypes.length == 1 ==> fixed filter (attribute must match given single value)
    else if (filterTypes.length == 1) {
      fixedFilters[n] = new FixedAnnotationFilter(typeName, attr, filterTypes[0]);
    }
    if (filterComponent != null) {
      JLabel label = new JLabel(labelText, JLabel.TRAILING);
      label.setLabelFor(labelIsFor);
      panel = createFilterPanel(new JComponent[] { label, filterComponent }, align);
    }
    return panel;
  }

  protected void setFilterCombiner(JComboBox filterCombiner) {
    this.filterCombiner = filterCombiner;
  }

  protected JComboBox getFilterCombiner() {
    return filterCombiner;
  }

  protected void setResettingFilters(boolean resettingFilters) {
    this.resettingFilters = resettingFilters;
  }

  protected boolean isResettingFilters() {
    return resettingFilters;
  }

  public void resetAllFilters() {
    setResettingFilters(true);
    for (int i = 0; i < filterComboBoxes.length; i++) {
      JComboBox cb = filterComboBoxes[i];
      if (cb != null) {
        ComboBoxModel cbModel = cb.getModel();
        if (cbModel instanceof DynamicFilterCBModel) {
          if (! ((DynamicFilterCBModel) cbModel).isResettable()) {
            continue;
          }
        }
        cb.setSelectedIndex(0);
      }
    }
    for (int i = 0; i < fixedFilters.length; i++) {
      if (fixedFilters[i] != null && fixedFilters[i] instanceof TextMatchFilter) {
        ((TextMatchFilter) fixedFilters[i]).reset();
      }
    }
    setResettingFilters(false);
    updateAnnotationFilter();
  }
  
  /***********************************************************************/
  /* For filtering annotations in the table */
  /***********************************************************************/

  protected boolean filtering = false;

  private JComboBox filterCombiner = null;

  protected class AttributeValueCombiningFilter implements ValueAnnotationFilter {
    private String attrib;
    private Object value;
    private JComboBox comboBox;
    protected AttributeValueCombiningFilter (String attrib, Object object, JComboBox comboBox) {
      if (attrib == null) throw new IllegalArgumentException ("attrib==null");
      this.attrib = attrib;
      this.value = object;
      this.comboBox = comboBox;
    }
    public boolean accept (Annotation annot) {
      AWBAnnotation awbAnnot = (AWBAnnotation) annot;
      AnnotationType type = awbAnnot.getAnnotationType();
      boolean accept = typeName.equals (type.getName());
      if (! accept) {
        return false;
      }
      boolean all = getFilterCombiner() != null && getFilterCombiner().getSelectedItem() == MATCH_ALL;
      if (value == null) {
        return all;
      }
      return value.equals(awbAnnot.getAttributeValue(attrib));
//      if (accept) {
//        if (value == null) {
////          accept = all;
//          accept = true;
//        }
//        else {
//          accept = value.equals(awbAnnot.getAttributeValue(attrib));
//        }
//      }
//      else {
//        // it's not the right type...we only support single-type tables, for now
//        return false;
//      }
//      if (filtering)
//        allFiltersNull &= (value == null);
//      if (filtering || (all && ! accept)) // || (! all && accept))
//        return accept;
//      filtering = true;
//      allFiltersNull = (value == null);
//      try {
//        for (int i = 0; i < fixedFilters.length; i++) {
//          AnnotationFilter f = fixedFilters[i];
//          if (f != null) {
//            if (! f.accept(awbAnnot)) {
//              return false;
//            }
//          }
//        }
//        for (int i = 0; ((all && accept) || (! all && ! accept)) && i < filterComboBoxes.length; i++) {
//          JComboBox f = filterComboBoxes[i];
//          if (f != comboBox && f != null) {
//            AnnotationFilter filter = (AnnotationFilter) f.getSelectedItem();
//            if (filter != null) {
//              accept = filter.accept(annot);
//            }
//          }
//        }
//        if (! all && ! accept)
//          accept = allFiltersNull;
//        return accept;
//      } finally {
//        filtering = false;
//      }
    }
    public String getDescription () { return toString(); }
    public String toString () { return value == null ? "<ALL>" : ""+value; }
    public Object getFilterValue() {
      return value;
    }
  }
  private boolean allFiltersNull = false;

  protected JPanel filteringPanel;

  public static class FixedAnnotationFilter implements AnnotationFilter {
    protected String attrib;
    protected Object value;
    protected String typeName;
    protected boolean acceptFlag = true;

    protected FixedAnnotationFilter(String typeName, String attrib, Object object) {
      if (attrib == null)
        throw new IllegalArgumentException("attrib==null");
      this.attrib = attrib;
      this.value = object;
      this.typeName = typeName;
      // if value starts with !, remove the leading !, and negate acceptance if
      // there is NOT another !
      if (object != null && object instanceof String && ((String) object).startsWith("!")) {
        String string = (String) object;
        object = string = string.substring(1);
        acceptFlag = string.startsWith("!");
      }
    }

    public boolean accept(Annotation annot) {
      AWBAnnotation awbAnnot = (AWBAnnotation) annot;
      AnnotationType type = awbAnnot.getAnnotationType();
      boolean accept = typeName.equals(type.getName());
      if (value != null && accept) {
        Object attributeValue = awbAnnot.getAttributeValue(attrib);
        accept = value.equals(attributeValue) == acceptFlag;
      }
      return accept;
    }

    public String getDescription() {
      return toString();
    }

    public String toString() {
      return ""+value;
    }
  }

  public static class SelectionAnnotationTableModel extends
      EnhancedAnnotationTableModel {
    public SelectionAnnotationTableModel(Task task, AnnotationType type,
        Object[] src, String[] headings, boolean[] columns) {
      super(task, type, src, headings, columns);
    }

    LinkedHashSet selected = new LinkedHashSet();

    public Object getValueAt(int row, int col) {
      if (col == 0) {
        return selected.contains(getAnnotation(row)) ? Boolean.TRUE : Boolean.FALSE;
      }
      return super.getValueAt(row, col);
    }

    public void setValueAt(Object value, int row, int col) {
      if (col == 0) {
        boolean val = false;
        if (value != null) {
          if (value instanceof Boolean) {
            val = ((Boolean) value).booleanValue();
          }
          else if (value instanceof String) {
            val = YES.equalsIgnoreCase((String) value);
          }
          else {
            throw new ClassCastException(value.getClass()+" is not Boolean or String");
          }
        }
        AWBAnnotation annotation = getAnnotation(row);
        boolean wasSelected = selected.contains(annotation);
        if (val != wasSelected) {
          if (val) {
            selected.add(annotation);
          }
          else {
            selected.remove(annotation);
          }
          // can't rely on super to do this
          fireTableCellUpdated (row, col);
        }
      }
      super.setValueAt(value, row, col);
    }

    public void clearAnnotations() {
      selected.clear();
      super.clearAnnotations();
    }

    public void removeAnnotation(AWBAnnotation annot) {
      if (selected.contains(annot)) {
        selected.remove(annot);
      }
      super.removeAnnotation(annot);
    }

    public void resetAnnotationFilters() {
      LinkedHashSet selected = new LinkedHashSet(this.selected);
      this.selected.clear();
      super.resetAnnotationFilters();
      restoreSelectedSet(selected);
    }

    private void restoreSelectedSet(LinkedHashSet selected) {
      for (Iterator it = selected.iterator(); it.hasNext();) {
        AWBAnnotation annot = (AWBAnnotation) it.next();
        if (getRow(annot) == -1) {
          it.remove();
        }
      }
      this.selected.addAll(selected);
      if (! this.selected.isEmpty()) {
        fireTableDataChanged();
      }
    }

    public void setAnnotationFilter(AnnotationFilter filter) {
      LinkedHashSet selected = new LinkedHashSet(this.selected);
      this.selected.clear();
      super.setAnnotationFilter(filter);
      restoreSelectedSet(selected);
    }

    public boolean isSelected(int row) {
      AWBAnnotation annotation = getAnnotation(row);
      return annotation != null && selected.contains(annotation);
    }

    public void selectAll(boolean all) {
      if (! all) {
        selected.clear();
      }
      else {
        for (int i = 0; i < getRowCount(); i++) {
          selected.add(getAnnotation(i));
        }
      }
      fireTableDataChanged();
    }

    public int selectionSize() {
      return selected.size();
    }

    public Object getColumnSource(int col) {
      if (col == 0) {
        return null;
      }
      return super.getColumnSource(col);
    }

    public Set getSelectedAnnotations() {
      return Collections.unmodifiableSet(new LinkedHashSet(selected));
    }

  }

  public JScrollPane getScrollPane() {
    return scrollPane;
  }

  public JPanel getFilteringPanel() {
    return filteringPanel;
  }

  /**
   * @return the row number of the row at the center of the visible rectangle in the scroll pane's viewport
   */
  public int getVisibleRow() {
    JViewport viewport = getScrollPane().getViewport();
    Rectangle viewRect = viewport.getViewRect();
    viewRect.y += viewRect.height / 2;
    int row = getTable().rowAtPoint(viewRect.getLocation());
    return row;
  }

  /**
   * @return a 2-array containing the minimum and maximum visible row numbers
   */
  public int[] getVisibleRows() {
    JViewport viewport = getScrollPane()
    .getViewport();
    Rectangle viewRect = viewport.getViewRect();
    AnnotationTable table = getTable();
    int minRow = table.rowAtPoint(viewRect.getLocation());
    int maxRow = table.rowAtPoint(new Point(viewRect.x, viewRect.y + viewRect.height));
    return new int[] { minRow, maxRow };
  }

  public String getTypeName() {
    return typeName;
  }
}
