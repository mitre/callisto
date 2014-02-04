
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
 * Created on Mar 6, 2005
 */
package org.mitre.jawb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * TODO Describe type
 * 
 * @author Galen B. Williamson
 * @version Mar 6, 2005
 */
public final class DebugLevel {
    public final static int SHOW_STACK_TRACES = 1;
    public final static int SHOW_FOUND_KEYS = SHOW_STACK_TRACES << 1;
    public final static int QUIET_ON_ZERO_LEVEL = SHOW_FOUND_KEYS << 1;
    public final static int DEBUG = SHOW_FOUND_KEYS | QUIET_ON_ZERO_LEVEL;

    private final static HashMap propsMap = new HashMap();

    public static final int getDebugLevel(Class clazz, int defaultLevel) {
        String className = clazz.getName();
        String pkgName = clazz.getPackage().getName().toString();
        Integer noPropsLevel = null;
        ResourceBundle[] props = null;
        synchronized (propsMap) {
            if (propsMap.containsKey(pkgName)) {
                Object o = propsMap.get(pkgName);
                if (o != null && o instanceof ResourceBundle)
                    props = (ResourceBundle[]) o;
                else if (o != null && o instanceof Integer)
                    noPropsLevel = (Integer) o;
            }
            else
                propsMap.put(pkgName, null);
        }
        if (props == null) {
            if (noPropsLevel != null)
                return defaultLevel; //noPropsLevel.intValue();
            {
                Object put = null;
                Vector v = new Vector();
                
                try {
                  try {
                    // look in root first
                    ResourceBundle root2 = ResourceBundle.getBundle("DEBUG", Locale.getDefault(), ClassLoader.getSystemClassLoader());
                    v.add(root2);
                  }
                  catch (MissingResourceException e) {
                    if ((DEBUG & SHOW_STACK_TRACES) != 0)
                      e.printStackTrace();
                  }
                  try {
                    // then look in package
                    ResourceBundle pkg2 = ResourceBundle.getBundle(pkgName+".DEBUG", Locale.getDefault(), ClassLoader.getSystemClassLoader());
                    v.add(pkg2);
                  }
                  catch (MissingResourceException ee) {
                    if ((DEBUG & SHOW_STACK_TRACES) != 0)
                      ee.printStackTrace();
                  }
                    try {
                        // look in root first
                        ResourceBundle root = ResourceBundle.getBundle("DEBUG", Locale.getDefault(), clazz.getClassLoader());
                        v.add(root);
                    }
                    catch (MissingResourceException e) {
                        if ((DEBUG & SHOW_STACK_TRACES) != 0)
                            e.printStackTrace();
                    }
                    try {
                        // then look in package
                      ResourceBundle pkg = ResourceBundle.getBundle(pkgName+".DEBUG", Locale.getDefault(), clazz.getClassLoader());
                      v.add(pkg);
                    }
                    catch (MissingResourceException ee) {
                        if ((DEBUG & SHOW_STACK_TRACES) != 0)
                            ee.printStackTrace();
                    }
                    try {
                      InputStream is = clazz.getResourceAsStream("DEBUG");
                      if (is != null) {
                        PropertyResourceBundle bundle = new PropertyResourceBundle(is);
                        v.add(bundle);
                      }
                    } catch (IOException e) {
                      if ((DEBUG & SHOW_STACK_TRACES) != 0)
                        e.printStackTrace();
                    }
                    try {
                      InputStream is = clazz.getResourceAsStream("/DEBUG");
                      if (is != null) {
                        PropertyResourceBundle bundle = new PropertyResourceBundle(is);
                        v.add(bundle);
                      }
                    } catch (IOException e) {
                      if ((DEBUG & SHOW_STACK_TRACES) != 0)
                        e.printStackTrace();
                    }
                    try {
                      File f = new File("DEBUG.properties").getAbsoluteFile();
                      if (f.exists()) {
                        InputStream is = new FileInputStream(f);
                        if (is != null) {
                          PropertyResourceBundle bundle = new PropertyResourceBundle(is);
                          v.add(bundle);
                        }
                      }
                    } catch (IOException e) {
                      if ((DEBUG & SHOW_STACK_TRACES) != 0)
                        e.printStackTrace();
                    }
                    if (v.size() > 0)
                        props = (ResourceBundle[]) v.toArray(new ResourceBundle[0]);
                    else
                        put = new Integer(defaultLevel);
                }
                finally {
                    synchronized (propsMap) {
                        propsMap.put(pkgName, put);
                    }
                }
            }
        }
        if (props != null) {
            String level = null;
            for (int i = 0; i < props.length; i++) {
                String check = null;
                if (level == null) {
                    try {
                        check = props[i].getString("default");
                        level = check;
                        if ((level != null && Integer.parseInt(level) > 0) || (DEBUG & QUIET_ON_ZERO_LEVEL) == 0)
                            if ((DEBUG & SHOW_FOUND_KEYS) != 0)
                                System.err.println("DebugLevel("+className+"): found level for default: "+level);
                    } catch (MissingResourceException e) {
                        if ((DEBUG & SHOW_STACK_TRACES) != 0)
                            e.printStackTrace();
                    }
                }
                if (level == null) {
                    try {
                        String key = pkgName+".default";
                        check = props[i].getString(key);
                        level = check;
                        if ((level != null && Integer.parseInt(level) > 0) || (DEBUG & QUIET_ON_ZERO_LEVEL) == 0)
                            if ((DEBUG & SHOW_FOUND_KEYS) != 0)
                                System.err.println("DebugLevel("+className+"): found level for "+key+": "+level);
                    } catch (MissingResourceException e) {
                        if ((DEBUG & SHOW_STACK_TRACES) != 0)
                            e.printStackTrace();
                    }
                }
                try {
                    String key = className.substring(pkgName.length() + 1);
                    check = props[i].getString(key);
                    level = check;
                    if ((level != null && Integer.parseInt(level) > 0) || (DEBUG & QUIET_ON_ZERO_LEVEL) == 0)
                        if ((DEBUG & SHOW_FOUND_KEYS) != 0)
                            System.err.println("DebugLevel("+className+"): found level for "+key+": "+level);
                } catch (MissingResourceException e) {
                    if ((DEBUG & SHOW_STACK_TRACES) != 0)
                        e.printStackTrace();
                }
                try {
                    check = props[i].getString(className);
                    level = check;
                    if ((level != null && Integer.parseInt(level) > 0) || (DEBUG & QUIET_ON_ZERO_LEVEL) == 0)
                        if ((DEBUG & SHOW_FOUND_KEYS) != 0)
                            System.err.println("DebugLevel("+className+"): found level for "+className+": "+level);
                } catch (MissingResourceException e) {
                    if ((DEBUG & SHOW_STACK_TRACES) != 0)
                        e.printStackTrace();
                }
                try {
                    String key = pkgName+".override";
                    check = props[i].getString(key);
                    level = check;
                    if ((level != null && Integer.parseInt(level) > 0) || (DEBUG & QUIET_ON_ZERO_LEVEL) == 0)
                        if ((DEBUG & SHOW_FOUND_KEYS) != 0)
                            System.err.println("DebugLevel("+className+"): found level for "+key+": "+level);
                } catch (MissingResourceException e) {
                    if ((DEBUG & SHOW_STACK_TRACES) != 0)
                        e.printStackTrace();
                }
                try {
                    check = props[i].getString("override");
                    level = check;
                    if ((level != null && Integer.parseInt(level) > 0) || (DEBUG & QUIET_ON_ZERO_LEVEL) == 0)
                        if ((DEBUG & SHOW_FOUND_KEYS) != 0)
                            System.err.println("DebugLevel("+className+"): found level for override: "+level);
                } catch (MissingResourceException e) {
                    if ((DEBUG & SHOW_STACK_TRACES) != 0)
                        e.printStackTrace();
                }
            }
            if (level != null) {
                try {
                    return Integer.parseInt(level);
                }
                catch (NumberFormatException e) {
                    if ((DEBUG & SHOW_STACK_TRACES) != 0)
                        e.printStackTrace();
                }
            }
        }
        return defaultLevel;
    }
}
