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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Simple timer dialog which has a pause button can store data.
 */
public class Timer {

  public static final String ACTIVE_PROPERTY = "active";
  public static final String PAUSED_PROPERTY = "paused";
  public static final String ID_PROPERTY = "id";

  private static final int DEBUG = 0;
  private static final SimpleDateFormat iso8061 =
    new SimpleDateFormat ("yyyyMMdd'T'HHmmss");

  private long start;
  private long stop;
  private long pause;

  private boolean active = false;
  private boolean paused = false;

  private String id;

  private PropertyChangeSupport changeSupport;

  public Timer () {
    changeSupport = new PropertyChangeSupport(this);
    /*
    PropertyChangeListener pcl = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
          System.err.println("Timer: "+e.getPropertyName()+
                             "="+e.getNewValue());
        }
      };
    changeSupport.addPropertyChangeListener(pcl);
    */
  }
  
  public synchronized long getStartTime() {
    return start;
  }
  public synchronized long getStopTime() {
    if (active) {
      return System.currentTimeMillis();
    } else {
      return stop;
    }
  }
  public synchronized long getPauseDuration() {
    if (paused) {
      long now = System.currentTimeMillis();
      return pause + now-stop;
    }
    return pause;
  }
  public synchronized long getDuration() {
    return getStopTime()-getStartTime()-getPauseDuration();
  }

  
  public synchronized void start() {
    long now = System.currentTimeMillis();
    if (paused) {
      pause(false);
      
    } else if (! active) {
      start = now;
      stop = pause = 0;
      if (id == null)
        setId(iso8061.format(new Date(now)));
    }
    setPaused(false);
    setActive(true);
  }
  public synchronized void pause(boolean p) {
    if (active) {
      if (paused && !p) {
        long now = System.currentTimeMillis();
        pause += now-stop;
        stop = 0;

      } else if (!paused && p) {
        stop = System.currentTimeMillis();
      }
      setPaused(p);
    }
  }
  public synchronized void stop() {
    if (active) {
      if (!paused)
        stop = System.currentTimeMillis();
      setPaused(false);
      setActive(false);
    }
  }
  
  private void setActive(boolean a) {
    boolean old = active;
    active = a;
    changeSupport.firePropertyChange(ACTIVE_PROPERTY, old, active);
  }
  public boolean isActive() {
    return active;
  }

  private void setPaused(boolean p) {
    boolean old = paused;
    paused = p;
    changeSupport.firePropertyChange(PAUSED_PROPERTY, old, paused);
  }
  public boolean isPaused() {
    return paused;
  }

  public String getId() {
    return id;
  }
  public void setId(String ident) {
    String old = id;
    id = ident;
    changeSupport.firePropertyChange(ID_PROPERTY, old, id);
  }

  public void addPropertyChangeListener(PropertyChangeListener l) {
    changeSupport.addPropertyChangeListener(l);
  }
  public void removePropertyChangeListener(PropertyChangeListener l) {
    changeSupport.removePropertyChangeListener(l);
  }

  public String toString() {
    return "[Timer: "+
      (active?"+":"!")+"active "+(paused?"+":"!")+"paused"+ 
      " start="+getStartTime()+" stop="+getStopTime()+
      " pause="+getPauseDuration()+"]";
  }
}
