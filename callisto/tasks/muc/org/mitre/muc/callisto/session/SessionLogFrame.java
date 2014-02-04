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

package org.mitre.muc.callisto.session;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import javax.swing.*;
import javax.swing.border.Border;


/**
 * Dialog to display state of a {@link Timer} object. Assumes that if the
 * 'timer' is set to null, that it is done so on the event dispatch thread.
 * Otherwise null pointer exceptions could occur due to race conditions.
 */
public class SessionLogFrame extends JFrame {

  /** Session attribute. */
  public static final String SESSION_ID = "session";
  /** Session attribute. */
  public static final String FILE_NAME = "fileName";
  /** Session attribute. */
  public static final String LOCATION = "location";

  private static final String START = "Play";
  private static final String PAUSE = "Pause";
  private static final String STOP  = "Stop";

  private static final String STOPPABLE_TT = "Stop";
  private static final String UNSTOPPABLE_TT = "Stoping the timer is disabled";

  private static SimpleDateFormat isoHms;

  private static HashMap smallIcons = new HashMap(3);
  private static HashMap largeIcons = new HashMap(3);

  static {
    isoHms = new SimpleDateFormat ("HH:mm:ss");
    isoHms.setTimeZone(TimeZone.getTimeZone("GMT"));
  }
  
  private Timer timer;
  private Date date = new Date();

  private JButton startPauseButton;
  private JButton stopButton;
  private JTextField durationField;
  private HashMap attribMap;

  private TimeWatcher timeWatcher = new TimeWatcher();
  
  private TimerChangeListener timerChangeListener;
  private boolean smallSize = true;
  private boolean stoppable = true;

  public SessionLogFrame() {
    init();
  }
  public SessionLogFrame(Timer t) {
    this();
    setTimer(t);
  }

  /** Set up the GUI components */
  private void init() {
    ActionListener al = new TimeButtonListener();
    timerChangeListener = new TimerChangeListener();

    // components specifically for timer issues.
    startPauseButton = new JButton();
    startPauseButton.addActionListener(al);

    stopButton = new JButton();
    stopButton.addActionListener(al);
    stopButton.setIcon(getIcon(STOP));
    stopButton.setToolTipText(STOPPABLE_TT);
    stopButton.setText("Stop");

    durationField = new JTextField(7);
    durationField.setEditable(false);
    //durationField.setFocusable(false);
    durationField.setHorizontalAlignment(JTextField.CENTER);
    durationField.setFont(durationField.getFont().deriveFont(Font.BOLD));
    durationField.setToolTipText("Duration");
    
    JPanel timerPane = new JPanel (new FlowLayout(FlowLayout.LEADING));
    timerPane.add(startPauseButton);
    timerPane.add(stopButton);
    timerPane.add(durationField);

    /* testing 
    timerPane.add(new JButton(new AbstractAction("timer"){
        public void actionPerformed(ActionEvent e) {
          setTimer( (getTimer() == null) ? new Timer() : null);
        }}));
    timerPane.add(new JButton(new AbstractAction("size"){
        public void actionPerformed(ActionEvent e) {
          setSmallSize(!isSmallSize());
        }}));
    */
    
    // attributes of this session are displayed up top
    JPanel attribPane = new JPanel (new GridBagLayout());
    Border inner = BorderFactory.createEmptyBorder(5,12,7,12);
    Border outer = BorderFactory.createTitledBorder("Session Attributes");
    inner = BorderFactory.createCompoundBorder(outer,inner);
    outer = BorderFactory.createEmptyBorder(5,5,5,5);
    inner = BorderFactory.createCompoundBorder(outer,inner);

    attribPane.setBorder(inner);
    attribMap = new HashMap();

    createAttributeLabel("File Name:", FILE_NAME, attribMap, attribPane);
    createAttributeLabel("Session ID:", SESSION_ID, attribMap, attribPane);
    createAttributeLabel("Location:", LOCATION, attribMap, attribPane);
    
    // all together now
    JPanel cp = (JPanel) getContentPane();
    cp.setLayout(new BorderLayout());
    cp.setBorder (BorderFactory.createEmptyBorder(7, 7, 6, 6));
    cp.add(attribPane, BorderLayout.NORTH);
    cp.add(timerPane, BorderLayout.CENTER);

    adjustSize();

    // thread to update gui
    Thread t = new Thread(timeWatcher);
    t.setDaemon(true);
    t.start();
  }
  /** Utility to put key and value labels in a gridbagged panel and store the
   * valueLabel in a map. Used only by init() */
  private void createAttributeLabel(String name, String key,
                                    HashMap map, JPanel panel) {
    GridBagConstraints cons = new GridBagConstraints();
    GridBagLayout gridBag = (GridBagLayout) panel.getLayout();
    JLabel keyLabel = new JLabel(name);
    JLabel valueLabel = new JLabel();

    cons.anchor = GridBagConstraints.LINE_START;
    cons.ipadx = 12;
    cons.weightx = 0;
    gridBag.setConstraints(keyLabel,cons);
    panel.add(keyLabel);

    cons.weightx = 1;
    cons.gridwidth = GridBagConstraints.REMAINDER;
    gridBag.setConstraints(valueLabel,cons);
    panel.add(valueLabel);

    map.put(key, valueLabel);
  }
  /** Update all components which are changed by setting 'smallSize'. Includes
   * icons of buttons, some fonts, etc. Used after init and setSmallSize. */
  private void adjustSize() {
    
    float fontSize = UIManager.getFont("TextField.font").getSize() +
      (smallSize ? 0 : 8F);
    durationField.setFont(durationField.getFont().deriveFont(fontSize));
    
    fontSize = UIManager.getFont("Button.font").getSize()+(smallSize?0:8F);
    Font font = durationField.getFont().deriveFont(fontSize);
    startPauseButton.setFont(font);
    stopButton.setFont(font);
    stopButton.setIcon(getIcon(STOP)); // otherwise, may not be resized
    
    int maxWidth = 0;
    startPauseButton.setPreferredSize(null);
    setStarted();
    maxWidth = getButtonMaxSize(maxWidth);
    setPaused();
    maxWidth = getButtonMaxSize(maxWidth);
    setStoped();
    maxWidth = getButtonMaxSize(maxWidth);
    Dimension d = new Dimension (maxWidth, startPauseButton.getHeight());

    startPauseButton.setPreferredSize(d);
    
    updateTimerState(); // back to where it should be

    pack();
  }
  /** Utility to check the best width for a button. Used only by adjustSize. */
  private int getButtonMaxSize(int prevMaxWidth) {
    super.pack();
    int width = startPauseButton.getWidth();
    return (width > prevMaxWidth) ? width : prevMaxWidth;
  }
  /** Utility to change timer buttons when timer started. */
  private void setStarted() { setStartStop(PAUSE,"Pause"); }
  /** Utility to change timer buttons when timer paused. */
  private void setPaused() { setStartStop(START,"Continue"); }
  /** Utility to change timer buttons when timer stopped. */
  private void setStoped() { setStartStop(START,"Start"); }
  /** Utility to change timer buttons on startStop button only. */
  private void setStartStop(String iconName, String text) {
    startPauseButton.setIcon(getIcon(iconName));
    startPauseButton.setText(text);
    startPauseButton.setToolTipText(text);
  }
  
  /** Set active state of certain components only. Has no effect on the Frame,
   * unlike 'setEnabled'. Used by updateTimerState(). */
  private void enableComponents(boolean enabled) {
    startPauseButton.setEnabled(enabled);
    stopButton.setEnabled(enabled && stoppable); // always able to disable
    stopButton.setToolTipText(stoppable ? STOPPABLE_TT : UNSTOPPABLE_TT);
    Color fg = (enabled ? UIManager.getColor ("TextField.foreground") :
                UIManager.getColor ("TextField.inactiveforeground"));
    Color bg = (enabled ? UIManager.getColor ("TextField.background") :
                UIManager.getColor ("TextField.inactiveBackground"));
    durationField.setForeground(fg);
    durationField.setBackground(bg);
  }

  /** Changes the timer components based on the Timer state (if any). */
  private void updateTimerState() {
    if (timer != null) {
      if (timer.isActive()) {
        if (timer.isPaused()) {
          setPaused();
        } else {
          setStarted();
        }
      } else { // !active
        setStoped();
      }
      enableComponents(true);
      timeWatcher.kick();
    } else { // timer == null
      enableComponents(false);
      setStoped();
    }
    updateDisplay();
  }

  /**
   * Updates components of the display after any changes. Ensures updates
   * occurs on event dispatch thread (otherwise an NPE may occur accessing
   * timer). Not important unless it goes public later really.
   */
  private void updateDisplay() {
    Runnable displayUpdater = new Runnable() {
        public void run() {
          StringBuffer sb = new StringBuffer("Session ");
          if (timer == null) {
            date.setTime(0);
          } else {
            date.setTime(timer.getDuration());
          }
          String duration = isoHms.format(date);
          durationField.setText(duration);
          sb.append(duration);
          String fName = getAttribute(FILE_NAME);
          if (! "".equals(fName))
            sb.append(" - ").append(fName);
          setTitle(sb.toString());
        }
      };
    // ensures it's always run on dispatch thread.
    if (SwingUtilities.isEventDispatchThread())
      displayUpdater.run();
    else
      SwingUtilities.invokeLater(displayUpdater);
  }
  
  /** Utility to retrive a known icon of correct size. */
  private Icon getIcon(String name) {
    HashMap iconMap = (smallSize ? smallIcons : largeIcons);
    Icon icon = (Icon) iconMap.get(name);
    if (icon == null) {
      name = "/resource/" + name + (smallSize ? "16" : "24") + ".gif";
      URL resource = SessionLogFrame.class.getResource(name);
      if (resource == null)
        throw new NullPointerException ("'"+name+"' not found");
      icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage (resource));
      iconMap.put(icon, name);
    }
    return icon;
  }

  /***********************************************************************/
  /* public accessors */

  /** True if Frame is using small icons and fonts in timer. */
  public boolean isSmallSize() {
    return smallSize;
  }
  /** Set size of icons and fonts in timer. */
  public void setSmallSize(boolean small) {
    smallSize = small;
    adjustSize();
  }
  /** True if the Stop button is enabled to manually stop. */
  public boolean isStopEnabled() {
    return stoppable;
  }
  /** Enable the ability to manually stop the timer. */
  public void setStopEnabled(boolean enabled) {
    stoppable = enabled;
    enableComponents(timer != null);
  }
  /**
   * Get the value of an attribute (see public constants) for the session.
   * Attributes which are displayed in the Frame are amongst the public
   * constants. If the value requested has not been set, 
   * @see #setAttribute
   */
  public String getAttribute(String key) {
    JLabel label = (JLabel) attribMap.get(key);
    if (label == null)
      return null;
    return label.getText();
  }
  /**
   * Set the value of an attribute (see public constants) for the session.
   * @see #getAttribute
   */
  public void setAttribute(String key, String value) {
    JLabel label = (JLabel) attribMap.get(key);
    if (label == null)
      return;
    label.setText ( (label == null) ? "" : value);
  }
  /** Get the timer currently in use. */
  public final Timer getTimer() {
    return timer;
  }
  /** Set a different timer to drive the time display. */
  public final void setTimer(Timer t) {
    if (t != timer) {
      if (timer != null)
        timer.removePropertyChangeListener(timerChangeListener);
      
      if (t != null)
        t.addPropertyChangeListener(timerChangeListener);
      
      timer = t;
      timeWatcher.setTimer(timer);
      
      updateTimerState();
    }
  }

  /***********************************************************************/
  /* private helper classes */

  /** Handle actions from button presses in the timer components. */
  private class TimeButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      //assert (timer != null): "timer is null";
      Object source = e.getSource();
      if (source == startPauseButton) {
        if (timer.isActive()) {
          timer.pause(!timer.isPaused());
        } else {
          timer.start();
        }
      } else if (source == stopButton) {
        timer.stop();
      }
    }
  }

  /** Listenes to bound attributes of timers to see state changes */
  private class TimerChangeListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent e) {
      //assert (timer != null): "timer is null";
      String property = e.getPropertyName();

      // desingned to not worry about the timer stopping
      if (Timer.ACTIVE_PROPERTY.equals(property) ||
          Timer.PAUSED_PROPERTY.equals(property)) {
        updateTimerState();
      } else if (Timer.ID_PROPERTY.equals(property)) {
        setAttribute(SessionLogFrame.SESSION_ID, (String) e.getNewValue());
      }
    }
  }

  /** Runnable which is kicked off while the timer is active. Causes the
   * display to update and dies when timer is deactivated or removed. */
  private class TimeWatcher implements Runnable {
    Timer watchedTimer;
    boolean paused, active;
    public void run() {
      while (true) {
        checkWatchedStates();
        while (active) {
          // blink effect when paused
          if (paused && ! getTitle().equals("")) {
            setTitle("");
            durationField.setText("");
          } else {
            updateDisplay();
          }
          try { Thread.sleep(500); } catch (InterruptedException e) {}
          checkWatchedStates();
        }
        updateDisplay();
        
        try {
          synchronized(this) { wait(); }
        } catch (InterruptedException e) {}
      }
    }
    private synchronized void checkWatchedStates () {
      active = (watchedTimer != null) && watchedTimer.isActive();
      paused = active && watchedTimer.isPaused();
    }
    synchronized void setTimer(Timer t) {
      watchedTimer = t;
      notify(); // kick the baby!
    }
    synchronized void kick() {
      notify();
    }
  }

  /** Testing only */
  public static void main(String[] args) {
    //SessionLogFrame t = new SessionLogFrame();
    SessionLogFrame t = new SessionLogFrame(new Timer());
    t.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    t.setStopEnabled(false);
    t.setVisible(true);

    t.setAttribute(FILE_NAME, "flubberoff.foo");
    t.setAttribute(SESSION_ID, "20030108T105513");
    t.setAttribute(LOCATION, "C:\\foo\\barbaz\\flubberoff.foo");

    /*
    for (int i=0; true; i++) {
      t.setTimer( (t.getTimer() == null) ? new Timer() : null);
      for (int j=0; j<3; j++) {
        try { Thread.sleep(5000); } catch (Exception e) {}
        t.setSmallSize(!t.isSmallSize());
      }
    }
    */
  }
}
