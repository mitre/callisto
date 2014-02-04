
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

package org.mitre.jawb.swing;

import java.awt.Color;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import java.util.Date;
import java.util.Locale;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.IslamicCalendar;
import com.ibm.icu.text.DateFormatSymbols;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.NumberFormat;

import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;


/**
 * Panel which renders a Calendar (by default Gregorian) and listens for user
 * input. Listen to the "time" property for updates. This code was originally
 * posted to the java developer forums by dchsw (public domain), and
 * subsequently modified. 
 *
 * This is now a further modification of org.mitre.jawb.swing.CalendarPanel 
 * to a version which makes use of the com.ibm.icu package's date and calendar
 * classes.
 *
 * The following is an example of using the
 * ICUCalendarPanel in a PopupWindow, so that users can unobtrusively set the
 * time, much like a popup menu:<p>
 *
 * <pre>
 *
 *  static SimpleDateFormat sLongFormat= new SimpleDateFormat("EEEE MMMM d yyyy");
 *
 *  private ICUCalendarPanel mCalendarPanel;
 *  private Date mDate= new Date();
 *
 *  public PopupCalendar () {
 *    setLayout(new BorderLayout());
 *    final JLabel label= new JLabel(sLongFormat.format(mDate));
 *    final PopupWindow popup= new PopupWindow(this);
 *
 *    mCalendarPanel= new ICUCalendarPanel ();
 *    mCalendarPanel.addPropertyChangeListener
 *      ("time", new PropertyChangeListener() {
 *        public void propertyChange (PropertyChangeEvent e) {
 *          popup.hide();
 *          mDate= (Date)e.getNewValue();
 *          label.setText(sLongFormat.format(mDate));
 *        }
 *      });
 *    
 *    mCalendarPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
 *    popup.add(mCalendarPanel);
 *    
 *    final JButton button= new JButton("...");
 *    button.addActionListener(new ActionListener() {
 *        public void actionPerformed(ActionEvent e) {
 *          int left= label.getSize().width -
 *            mCalendarPanel.getPreferredSize().width +
 *            button.getSize().width;
 *          mCalendarPanel.setTime(mDate);
 *          popup.show(label, left,0);
 *        }
 *      });
 *    add(label, BorderLayout.CENTER);
 *    add(button, BorderLayout.EAST);
 *  }
 *
 * </pre>
 *
 * 
 *
 * @author <a href="http://forum.java.sun.com/profile.jsp?user=167984">dchsw</a>
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @see <a href="http://forum.java.sun.com/thread.jsp?forum=57&thread=230866">
 *       Java Forums: Jcombobox in a JPopupmenu</a>
 * @version 2.0
 */
public class ICUCalendarPanel extends JPanel {
  
  private com.ibm.icu.util.Calendar mCalendar;
  private com.ibm.icu.util.Calendar tCal;
  
  private JButton mDateButton;
  private Color mBackground;
  private JPanel mDayPanel;
  private JComboBox mMonthsCombo;
  private JLabel mYearLabel= new JLabel ();
  
  public ICUCalendarPanel () {
    this (new GregorianCalendar ());
  }

  public ICUCalendarPanel (com.ibm.icu.util.Calendar calendar) {
    super (new BorderLayout());
    if (calendar == null)
      throw new IllegalArgumentException ("calendar=null");

    Font mFont= new Font ("SansSerif", Font.PLAIN, 10);

    JPanel navigatePanel= new JPanel (new BorderLayout());

    mCalendar = calendar;
    tCal = (Calendar) mCalendar.clone ();

    // Month combo
    mMonthsCombo = new JComboBox ();
    ULocale loc = calendar.getLocale(ULocale.VALID_LOCALE);
    SimpleDateFormat dateFormat = (SimpleDateFormat) 
      DateFormat.getInstance(calendar, loc);
    DateFormatSymbols dateSymbols = dateFormat.getDateFormatSymbols();
    String[] monthNames = dateSymbols.getMonths();
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMinimumIntegerDigits(2);

    // DateFormatSymbols dateSymbols = new DateFormatSymbols(calendar, loc);
    SimpleDateFormat monthFormat= 
      new SimpleDateFormat("MM - MMMM", dateSymbols);

    tCal.set (Calendar.MONTH, 0);
    tCal.set (Calendar.DAY_OF_MONTH, 1);
    for (int i=0; i<monthNames.length; i++) {
      // tCal.set (Calendar.MONTH, i);
      /*      System.err.println("adding month " + i + ": " +
                         monthFormat.format(tCal.getTime()));
      */
      //mMonthsCombo.addItem (monthFormat.format (tCal.getTime()));
      mMonthsCombo.addItem (nf.format(i+1) + " - " + monthNames[i]);
      
      //tCal.roll (Calendar.MONTH, 1);
    }


    /* for debugging purposes only */
    /*******
    ULocale iLoc = new ULocale ("ar@calendar=islamic");
    IslamicCalendar iCal = new IslamicCalendar(iLoc);
    SimpleDateFormat dateFormatter = (SimpleDateFormat) 
      DateFormat.getInstance(iCal, iLoc);
    DateFormatSymbols iDateSymbols = dateFormatter.getDateFormatSymbols();
    SimpleDateFormat iMonthFormat = 
      new SimpleDateFormat("MM - MMMM", iDateSymbols);
    SimpleDateFormat iDateFormat =
      new SimpleDateFormat("dd/MM/YYYY -- DD MMMM YYYY", iDateSymbols);
    iCal.set (Calendar.MONTH, 0);
    iCal.set(Calendar.DAY_OF_MONTH, 1);
    for (int i=0; i<25; i++) {
      System.err.println(i + ": month=" +
                         iCal.get(Calendar.MONTH) +
                         " day=" +
                         iCal.get(Calendar.DAY_OF_MONTH) +
                         " month format = " + 
                         iMonthFormat.format(iCal.getTime()) +
                         " date format = " +
                         iDateFormat.format(iCal.getTime()) +
                         "( " + iCal.getTime() + " )");
      iCal.add (Calendar.DAY_OF_MONTH, 1);
    }


    DateFormatSymbols dfs = dateFormatter.getDateFormatSymbols();
    for (int i=0; i<monthNames.length; i++) {
      System.err.println("month " + i + ": " + monthNames[i]);
    }
    
    iCal.set (Calendar.MONTH, 0);
    iCal.set(Calendar.DAY_OF_MONTH, 2);
    System.err.println ("Month 0 day 2 is in month: " 
                        + iMonthFormat.format(iCal.getTime()) + " date: " +
                        dateFormatter.format(iCal.getTime()) + "Gregorian: " +
                        iCal.getTime());
    ******/
    /* end debugging section */


    // number returned for month is zero based... add one!
    mMonthsCombo.setMaximumRowCount(tCal.getMaximum(Calendar.MONTH)+1);
    mMonthsCombo.setFont(mFont);
    mMonthsCombo.setFocusable (false);
    mMonthsCombo.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setMonth (mMonthsCombo.getSelectedIndex());
        }
      });

    // year changer
    JPanel yearTicker = new JPanel (new BorderLayout());
      
    JButton decrease = new JButton (new ArrowIcon (ArrowIcon.LEFT));
    decrease.setFocusable (false);
    decrease.setBorder (new EmptyBorder (3,6,3,6));
    decrease.addActionListener (new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setYear (mCalendar.get(Calendar.YEAR)-1);
        }
      });

    JButton increase = new JButton (new ArrowIcon (ArrowIcon.RIGHT));
    increase.setFocusable (false);
    increase.setBorder (new EmptyBorder (3,6,3,6));
    increase.addActionListener (new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          setYear (mCalendar.get(Calendar.YEAR)+1);
        }
      });

    mYearLabel.setText(Integer.toString(mCalendar.get (Calendar.YEAR)));
    mYearLabel.setFont(mFont);

    yearTicker.add (decrease, BorderLayout.WEST);
    yearTicker.add (mYearLabel, BorderLayout.CENTER);
    yearTicker.add (increase, BorderLayout.EAST);

    // month/year
    navigatePanel.add(mMonthsCombo, BorderLayout.CENTER);
    navigatePanel.add(yearTicker, BorderLayout.EAST);

    // Create the grid of days
    EmptyBorder border= new EmptyBorder(2,2,2,2);
    
    // action performed when a day is clicked
    ActionListener dayListener= new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JButton btn= (JButton) e.getSource();
          mCalendar.set(Calendar.DAY_OF_MONTH,
                        Integer.parseInt(btn.getText()));
            
          btn.setBackground(Color.yellow);
          mDateButton.setBackground(mBackground);
          mDateButton= btn;

          // if we give old time and it's equal to new time, no event is fired
          firePropertyChange ("time", null, mCalendar.getTime());
        }
      };
    
    // action performed when a week number is clicked
    ActionListener weekListener= new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JButton btn= (JButton) e.getSource();

          int current = mCalendar.get(Calendar.WEEK_OF_YEAR);
          int selected = Integer.parseInt(btn.getText());
          int diff = selected - current;

          if (Math.abs (diff) < 6) {
            mCalendar.add (Calendar.WEEK_OF_YEAR, diff);

          } else {
            if (diff > 0)  { // selected week 52-53 of last year
              // we know week will always be week '0' as it has at least one
              // day in 'current' month, so just subtract current weeks.
              mCalendar.add (Calendar.WEEK_OF_YEAR, -current);
              
            } else { // selected week 1-2 of next year
              // add enough to get to end of year, plus whatever selected
              int max = mCalendar.getActualMaximum(Calendar.WEEK_OF_YEAR);
              mCalendar.add (Calendar.WEEK_OF_YEAR, max-current + selected);
            }
          }
          mDateButton.setBackground(mBackground);
          mDateButton= btn;
          
          // if we give old time and it's equal to new time, no event is fired
          firePropertyChange ("time", null, mCalendar.getTime());
        }
      };
    Font bold = new Font (mFont.getFamily(), Font.BOLD, 12);
    Color weekColor = Color.RED.darker ().darker ();
    SimpleDateFormat weekFormat = new SimpleDateFormat("E");

    GridBagLayout bag = new GridBagLayout ();
    mDayPanel = new JPanel (bag);
    GridBagConstraints cons = new GridBagConstraints ();
    cons.insets = new Insets (0,2,0,2);
    cons.gridy = 0;
    cons.weighty = 1; // all rows are same height


    // Label the days of the week starting with 'week' label
    JLabel label;
    label = new JLabel("Wk");
    label.setHorizontalAlignment(JLabel.CENTER);
    label.setForeground (weekColor);
    label.setFont(bold);
    cons.weightx = 1;
    bag.setConstraints (label, cons);
    mDayPanel.add(label);

    tCal.set (Calendar.DAY_OF_WEEK, tCal.getFirstDayOfWeek ());
    for (int i= 0; i<7; i++) {
      String dow = weekFormat.format (tCal.getTime());
      label = new JLabel (dow.substring(0,1));
      label.setHorizontalAlignment(JLabel.CENTER);
      label.setFont(bold);
      bag.setConstraints (label, cons);
      mDayPanel.add(label);

      tCal.roll (Calendar.DAY_OF_WEEK, true);
    }

    // create buttons for each day and week square
    cons.weightx = 0; // column width is set by names above
    cons.gridx = GridBagConstraints.RELATIVE;

    for (int row= 0; row < 6; row++) {
      cons.gridy++;
      for (int col= 0; col< 8; col++) {
        JButton btn = new JButton (Integer.toString (row*7+col));
        if (col == 0) { // weeks
          btn.setForeground (weekColor);
          btn.addActionListener (weekListener);
          
        } else {
          btn.addActionListener(dayListener);
        }
        if (mBackground == null) 
          mBackground= btn.getBackground();
        btn.setBorder(border);
        btn.setFont(mFont);

        bag.setConstraints (btn, cons);
        mDayPanel.add(btn);
      }
    }

    // All together
    this.setBorder(new EmptyBorder(2,4,2,4));
    this.add(navigatePanel, BorderLayout.NORTH);
    this.add(mDayPanel, BorderLayout.CENTER);
  }
    
  private void setYear (int year) {
    mCalendar.set (Calendar.YEAR, year);
    mYearLabel.setText (Integer.toString (year));
    updateDayPanel ();
  }

  private void setMonth (int month) {
    mCalendar.set(Calendar.MONTH, month);
    updateDayPanel();
  }

  private void updateDayPanel () {
    tCal.setTime (mCalendar.getTime ());

    int mon = mCalendar.get(Calendar.MONTH);
    int dom = mCalendar.get(Calendar.DAY_OF_MONTH);

    tCal.set (Calendar.DAY_OF_MONTH, 1);
    int fdowDiffDow =
      tCal.getFirstDayOfWeek() - tCal.get(Calendar.DAY_OF_WEEK);
    tCal.add (Calendar.DAY_OF_MONTH, fdowDiffDow);
    if (fdowDiffDow > 0)
      tCal.add (Calendar.DATE, -7);
               
    if (mDateButton != null)
      mDateButton.setBackground (mBackground);

    int btnIndex= 8;// component index of the first 'week' btn
    for (int row= 0; row < 6; row++) {
      for (int col= 0; col< 8; col++) {
        JButton btn= (JButton)mDayPanel.getComponent(btnIndex++);
        
        if (col == 0) { // week
          btn.setText (Integer.toString (tCal.get (Calendar.WEEK_OF_YEAR)));
          
        } else {        // day
          int day = tCal.get (Calendar.DAY_OF_MONTH);
          btn.setText (Integer.toString (day));
          
          if (tCal.get (Calendar.MONTH) == mon) { // within month
            btn.setEnabled (true);
            if (day == dom)
              mDateButton= btn;
            
          } else {                                // not within month
            btn.setEnabled (false);
          }
          tCal.add (Calendar.DAY_OF_MONTH, 1);
        }
      }
    }
    mDateButton.setBackground(Color.yellow);
    mDayPanel.repaint ();
  }
  
  public Date getTime () {
    return mCalendar.getTime ();
  }
    
  public void setTime (Date time) {
    mCalendar.setTime (time);
    mYearLabel.setText (Integer.toString (mCalendar.get (Calendar.YEAR)));
    mMonthsCombo.setSelectedIndex (mCalendar.get (Calendar.MONTH));
    updateDayPanel ();
  }

  
  static SimpleDateFormat longFormat= new SimpleDateFormat("EEEE MMMM d yyyy");
  static SimpleDateFormat isoDateFormat= new SimpleDateFormat("yyyy-MM-dd");
  static SimpleDateFormat isoOrdinalFormat= new SimpleDateFormat("yyyy-DDD");
  
  // There is no way to get SimpleDateFormat to display day of week as digit
  // static SimpleDateFormat isoWeekFormat=
  //                                   new SimpleDateFormat("yyyy-'W'ww-??");
  // I've submitted an RFE to the Java Developer Connection.
  // workaround:
  //   isoCalendar.setTime (mDate);
  //   int dow = isoCalendar.get(Calendar.DAY_OF_WEEK);
  //   int fdow = isoCalendar.getFirstDayOfWeek();
  //   int val = ((dow-fdow+7)%7)+1;
  //   System.err.println ("\nDAY_OF_WEEK="+val);

  /*********** this is just test code   *********
  
  public static void main (String[] args) {
    JFrame frame = new JFrame ("ICUCalendarPanel Test");
    frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
    
    frame.getContentPane ().add (new PopupCalendar ());
    
    frame.setLocation (640, 480);
    frame.pack ();
    frame.setSize (frame.getSize().width+20, frame.getSize().height);
    frame.show ();
  }

  private static class PopupCalendar extends JPanel {
    ICUCalendarPanel mCalendarPanel;
    Date mDate = new Date();
    
    public PopupCalendar () {
      setLayout(new BorderLayout());
      final JLabel label= new JLabel(isoDateFormat.format(mDate));
      final PopupWindow popup= new PopupWindow(this);

      final Calendar isoCalendar = new GregorianCalendar ();
      isoCalendar.setMinimalDaysInFirstWeek (4); // ISO 8601 (wrt weeks)
      isoCalendar.setFirstDayOfWeek (Calendar.MONDAY); // ISO 8601 (first day)
      isoDateFormat.setCalendar (isoCalendar);
      
      mCalendarPanel= new ICUCalendarPanel (isoCalendar);
      mCalendarPanel.addPropertyChangeListener
        ("time", new PropertyChangeListener() {
            public void propertyChange (PropertyChangeEvent e) {
              popup.hide();
              mDate= (Date)e.getNewValue();
              label.setText(isoDateFormat.format(mDate));
            }
          });
     
      mCalendarPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
      popup.add(mCalendarPanel);
     
      final JButton button= new JButton("...");
      button.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            int left= label.getSize().width -
              mCalendarPanel.getPreferredSize().width +
              button.getSize().width;
            mCalendarPanel.setTime (mDate);
            popup.show(label, left,0);
          }
        });
      add(label, BorderLayout.CENTER);
      add(button, BorderLayout.EAST);
    }
  }
  *************/

}
