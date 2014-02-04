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

package org.mitre.timex2.callisto;

import org.mitre.jawb.io.*;
import org.mitre.jawb.gui.JawbDocument;
import org.mitre.jawb.tasks.*;
import org.mitre.jawb.atlas.AWBDocument;

import java.net.URI;
import java.util.List;
import java.io.File;

public class BatchExportTIMEX2 {
    public static void main(String[] args) {

      ExportTIMEX2 exp = new ExportTIMEX2();
      JawbDocument doc = null;
      URI uri, sgmlUri;
      File infile, absolute;
      
      for (int i=0; i<args.length; i++) {

	infile = new File(args[i]);
	try {
	  absolute = infile.getCanonicalFile();
	} catch (Exception e) {
	  System.err.println ("Cannot get canonical file: \n\t" +
			      e.getMessage());
	  continue; // go on to next file
	}
	System.err.println("Processing: " + args[i] + "->" + absolute);

	uri = absolute.toURI();

	if (!uri.isAbsolute()) {
	  System.err.println(uri.toString() + " is not absolute");
	} else {
	  System.err.println(uri.toString() + " is absolute");
	}
		  

	List supporting = null;
	try {
	  supporting = ATLASHelper.getSupportingTasks (uri);
	} catch (Exception e) {
	  System.err.println(e.getMessage());
	  // fall through to supporting == null
	}
      
	if (supporting == null) {
	  String error = uri.getPath() + " is not a valid AIF file";
	  System.err.println (error);
	  continue; // go on to next file
        
	} else if (supporting.isEmpty ()) {
	  // but there's no available task!
	  String error = uri.getPath() +
	    "\nwas created with an unknown task, unable to open";
	  System.err.println (error);
	  continue; // go on to next file
	}
	  
	for (int j=0; j<supporting.size(); j++) {
	  Task task = (Task)supporting.get(j);
	  if (task.getName().equals("org.mitre.timex2")) {
	    try {
	      doc = JawbDocument.fromAIF (uri, task);
	    } catch (Exception e) {
	      System.err.println ("unable to load " + uri.getPath());
	      System.err.println ("\t" + e.getMessage());
	      e.printStackTrace();
	    }
	    break;
	  }
	}
	if (doc == null) {
	  // TIMEX2 is not a supporting task
	  System.err.println (uri.getPath() + 
			      "\nis not compatible with the TIMEX2 task");
	  continue; // go on to next file
	}
	  
	// export it
	try {
	  //	  URI sgmlUri = new URI(args[i]+".sgml");
	  sgmlUri = (new File(absolute.getPath()+".sgml")).toURI();
	} catch (Exception e) {
	  System.err.println ("Unable to create output URI: " +
			      absolute.getPath()+".sgml\n\t" +
			      e.getMessage());
	  continue; // go on to next file
	}
	try {
	  AWBDocument awbDoc = (AWBDocument)doc.getAnnotationModel();
	  exp.exportDocument(awbDoc, sgmlUri);
	} catch (Exception e) {
	  System.err.println ("Unable to export " + uri.getPath() + ":");
	  System.err.println ("\t" + e.getMessage());
	}
      }
    }
}
