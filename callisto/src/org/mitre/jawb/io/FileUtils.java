
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
 * Except as permitted below ALL RIGHTS RESERVED
 *
 * The MITRE Corporation (MITRE) provides this software to you without charge to
 * use for your internal purposes only. Any copy you make for such purposes is
 * authorized provided you reproduce MITRE's copyright designation and this
 * License in any such copy. You may not give or sell this software to any other
 * party without the prior written permission of the MITRE Corporation.
 *
 * The government of the United States of America may make unrestricted use of
 * this software.
 *
 * This software is the copyright work of MITRE. No ownership or other
 * proprietary interest in this software is granted you other than what is
 * granted in this license.
 *
 * Any modification or enhancement of this software must inherit this license,
 * including its warranty disclaimers. You hereby agree to provide to MITRE, at
 * no charge, a copy of any such modification or enhancement without limitation.
 *
 * MITRE IS PROVIDING THE PRODUCT "AS IS" AND MAKES NO WARRANTY, EXPRESS OR
 * IMPLIED, AS TO THE ACCURACY, CAPABILITY, EFFICIENCY, MERCHANTABILITY, OR
 * FUNCTIONING OF THIS SOFTWARE AND DOCUMENTATION. IN NO EVENT WILL MITRE BE
 * LIABLE FOR ANY GENERAL, CONSEQUENTIAL, INDIRECT, INCIDENTAL, EXEMPLARY OR
 * SPECIAL DAMAGES, EVEN IF MITRE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGES.
 *
 * You accept this software on the condition that you indemnify and hold
 * harmless MITRE, its Board of Trustees, officers, agents, and employees, from
 * any and all liability or damages to third parties, including attorneys' fees,
 * court costs, and other related costs and expenses, arising out of your use of
 * this software irrespective of the cause of said liability.
 *
 * The export from the United States or the subsequent reexport of this software
 * is subject to compliance with United States export control and munitions
 * control restrictions. You agree that in the event you seek to export this
 * software you assume full responsibility for obtaining all necessary export
 * licenses and approvals and for assuring compliance with applicable reexport
 * restrictions.
 */

package org.mitre.jawb.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

public class FileUtils {

  public static File relativize(File path, File relative) throws IOException {
    if (!relative.isDirectory()) {
      relative = relative.getParentFile();
    }
    String aPath = (path.isDirectory() ? path : path.getParentFile())
        .getCanonicalPath();
    String aRel = relative.getCanonicalPath();
    aPath = fixSeparatorChars(aPath);
    aRel = fixSeparatorChars(aRel);
    String[] aPaths = aPath.split("/");
    String[] aRels = aRel.split("/");
    String common = "";
    for (int i = 0; i < aPaths.length || i < aRels.length; i++) {
      if (i >= aPaths.length || i >= aRels.length
          || !aPaths[i].equals(aRels[i])) {
        for (int j = aRels.length; j > i; j--) {
          common += "../";
        }
        while (i < aPaths.length) {
          common += aPaths[i++] + '/';
        }
        break;
      }
    }
    String rel = common + path.getName();
    return new File(rel);
  }

  public static String fixSeparatorChars(String path) {
    if (File.separatorChar != '/') {
      path = path.replace(File.separatorChar, '/');
    }
    return path;
  }

  public static String canonicalize(File file) {
    if (File.separatorChar == '/') {
      return file.getPath();
    }
    return file.getPath().replace(File.separatorChar, '/');
  }

  public static String canonicalize(String pathstr) {
    if (File.separatorChar == '/') {
      return pathstr;
    }
    return pathstr.replace(File.separatorChar, '/');
  }

  public static boolean copyFile(File src, File dest) {
    boolean success = false;
    try{
      InputStream in = new FileInputStream(src);
      OutputStream out = new FileOutputStream(dest);
    
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0){
        out.write(buf, 0, len);
      }
      in.close();
      out.close();
      System.out.println("File copied.");
      success = true;
    }
    catch(FileNotFoundException ex){
      System.out.println(ex.getMessage() + " in the specified directory.");
    }
    catch(IOException e){
      System.out.println(e.getMessage());      
    }
    return success;
  }
  
  public static boolean renameFile(File src, File dest) {
    if (dest.exists()) {
      System.err.println("FileUtils.renameFile: dest exists -- deleting");
      dest.delete();
    }
    boolean success = src.renameTo(dest);
    if (!success) {
      success = copyFile(src, dest);
      if (success) {
        src.delete();
      }
    }
    return success;
  }
}