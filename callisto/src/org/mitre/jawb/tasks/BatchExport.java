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

package org.mitre.jawb.tasks;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.io.*;
import org.mitre.jawb.gui.JawbDocument;
import org.mitre.jawb.tasks.*;
import org.mitre.jawb.atlas.AWBDocument;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.io.File;
import java.lang.reflect.Constructor;

/**
 *  BatchExport
 *
 *  The first argument is the name of an Exporter sublcass, such as
 *  org.mitre.timex2.callisto.ExportTimex2 or the name of a task, such
 *  as org.mitre.xg.callisto.XGTask.  If a task is provided instead of
 *  an exporter, the DefaultInlineExporter is used for that Task.
 *
 * TODO provide means to specify other Exporters that take Task and
 * potentially other args...
 *
 *  The remaining args are files to be exported using that exporter.
 *  They will be skipped if they are not AIF files supported by the
 *  task associated with the Exporter subclass (org.mitre.timex2 in
 *  the example above)
 *
 */
public class BatchExport {

  private static int DEBUG = 0;

  public static void usage() {
    String usage = 
      "USAGE: batch-export -t\n" +
      "  List available Tasks, and their exporters.\n" +
      "USAGE: batch-export [-f] [-x ext] {exporter-class} aif-file [...]\n" +
      "  Export .aif files using exporter specified.\n" +
      "USAGE: batch-export [-f] [-x ext] {task-class} aif-file [...]\n" +
      "  Export .aif files using the default exporter of the Task specified.\n\n" +
      "  -f       force creation of output files even if they already exist\n" +
      "           otherwise an error is printed on stderr and no file is written\n" +
      "  -x ext   extension appended to base of exported files. default=\"sgml\"\n" +
      "  Output format is always Exporter dependant.\n" +
      "  Exported files names are based on the input file name:\n" +
      "    in:  {base}[.aif.xml]\n" +
      "    out: {base}.{ext}\n";

    System.err.println(usage);
  }

  public static void main(String[] args) {

    // Class exporterClass;
    // Constructor cons;
    Task task = null;
    Exporter exp = null;
    String spec = null;
    String extension = ".sgml";
    boolean listTasks = false;
    boolean force = false;
    String error = null;
    int index;

    // remove the parameters, and pass the file names to the newFrame call Any
    // more complex, and we should really use the gnu.getopt package.
    for (index = 0; index<args.length; index++) {
      if (args[index].charAt (0) != '-') {
        // must increment, as break doesn't allow loop to do so
        spec = args[index++];
        break; // first non-option is export spec, all others are files
      }
      else if (args[index].equals ("-f")) {
        force = true;
      }
      else if (args[index].equals ("-x")) {
        if (args.length < index+2) {
          error = "Missing argument to -x";
          break;
        }
        else {
          extension = args[++index];
          // ensure that we separate the extension!
          if (! extension.startsWith("."))
            extension = "." + extension;
        }
      }
      else if (args[index].equals ("-t")) {
        listTasks = true;
        break;
      }
      else {
        error = "Unrecognized flag: " + args[index];
        break;
      }
    }

    if (listTasks) {
      listTasks();
      return;
    }

    if (spec == null) {
      error = "Export specification required";
    }

    if (error != null) {
      System.err.println("ERROR: " + error + "\n");
      usage();
      System.exit(-1);
    }

    /** No need to validate syntax, if it's used to verify correct Task later.
    String[] classParts = args[0].split("\\.");
    if (classParts.length < 5) {
      System.err.println(args[0] + 
                         " is not a valid exporter specification.");
      return;
    }
    exportTask = classParts[0] + "." + classParts[1] + "." + classParts[2];
    */

    
    /**** This doesn't work because the classloaders for the tasks aren't
     * accessible here....
     try {
     exporterClass = Class.forName(args[0]);
     } catch (ExceptionInInitializerError ee) {
     System.err.println ("ExceptionInInitializerError: " + 
     "Unable to instantiate class named " + args[0]);
     return;
     } catch (LinkageError le) {
     System.err.println ("LinkageError: Unable to instantiate class named "
     + args[0]);
     return;
     } catch (ClassNotFoundException x) {
     System.err.println ("ClassNotFoundException: " + 
     "Unable to instantiate class named " + args[0]);
     return;
     }
    */

    // Jawb.initTasks(); 
    // instead do this, which inits the tasks, and gets them from the
    // taskManager initialized with them, then grabs an iterator.
    Iterator taskIter  = Jawb.getTasks().iterator();
    while (taskIter.hasNext() && exp == null) {
      // if no exporter is found, method returns so inconsistency not possible
      task = (Task) taskIter.next();
      
      System.err.println ("next task is " + task + ": " + 
                          task.getClass().getName());
      try {
        // see if user specified the task
        if (task.getClass().getName().equals(spec)) {
          System.err.println(spec + " is a Task");
          exp = new DefaultInlineExporter(task, "Default inline SGML");

        } else {
          // it's not the tasks, try the task's exporters
          Exporter[] exporters = task.getExporters();

          if (exporters == null)
            continue;

          for (int i = 0; i < exporters.length; i++) {
            if (exporters[i].getClass().getName().equals(spec)) {
              //System.out.println(spec+" is an Exporter of "+task.getName());
              exp = exporters[i];
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (exp == null) {
      System.err.println("No known Task or Exporter with class name: " + spec);
      return;
    }

    System.err.println("Exporting using: " + task.getName() +
                       ":" + exp.getFormat());
    

    /** only need to getConstructor first if we need to pass in args
        try {
        cons = exporterClass.getConstructor(new Class[0]);
        } catch (Exception x) {
        System.err.println ("Unable to access constructor for " +
        exporterClass);
        return;
        }

        try {
        exp = (Exporter) cons.newInstance(new Object[0]);
        } catch (Exception x) {
        System.err.println ("Unable to instantiate " + exporterClass + 
        " using " + cons);
        return;
        }
    **/


    JawbDocument doc = null;
      
    for (int i=index; i<args.length; i++) {

      String inPath = args[i];
      String outPath = null;
      File inFile = null;
      File outFile = null;

      // start with input
      inFile = new File(inPath);
      if (! inFile.exists()) {
        System.err.println("File not found: " + inPath);
        continue; // go on to next file
      }
      
      try {
        inFile = inFile.getCanonicalFile();
      } catch (Exception e) {
        System.err.println("Cannot get canonical file: \n\t" +
                           e.getMessage());
        continue; // go on to next file
      }
      URI inURI = inFile.toURI();

      // find output
      try {
        // because 'inFile' is built from 'inPath', and converstion to URI
        // could cause "%20" type substitutions, which royally screw up the
        // File(name) constructor, go from original inPath.
        outPath = inPath;

        if (outPath.endsWith(".aif.xml")) {
          outPath = inPath.substring(0, inPath.length() - 8);
        }

        outPath = outPath + extension;
        outFile = new File(outPath);
        
        if (outFile.exists() && !force) {
          System.err.println("Output file exists, ignoring: " + outPath);
          continue; // go on to next file
        }

      } catch (Exception e) {
        System.err.println("Unable to create output URI: " +
                           inFile.getPath()+".sgml\n\t" +
                           e.getMessage());
        continue; // go on to next file
      }
      URI outURI = outFile.toURI();

      System.err.println("Exporting: '" + inPath +
                         "'\n       ->  '" + outPath + "'");
      
      /*
        if (!inURI.isAbsolute()) {
        System.err.println(inURI.toString() + " is not absolute");
        } else {
        System.err.println(inURI.toString() + " is absolute");
        }
      */


      List supporting = null;
      try {
        supporting = ATLASHelper.getSupportingTasks (inURI);
      } catch (Exception e) {
        System.err.println(e.getMessage());
        // fall through to supporting == null
      }
      
      if (supporting == null) {
        String err = inURI.getPath() + " is not a valid AIF file";
        System.err.println(err);
        continue; // go on to next file
        
      } else if (supporting.isEmpty ()) {
        // but there's no available task!
        String err =
          "AIF was created with an unknown task, unable to open: " + inPath;
        System.err.println(err);
        continue; // go on to next file
      }

      System.err.println("\t1st supporting task: " +
                         ((Task)supporting.iterator().next()).getName());

      // RK 12/23/08
      // If the user specified the GenericTask, there may be multiple
      // user-generated generic tasks that match that specification,
      // and above one will be chosen randomly.  There may also be other
      // cases where the task selection above ends up with something
      // incompatible with the actual file.  
      //
      // So... here we check what tasks actually support the aif file
      // (generally just the one named in the aif at this point) and
      // if the task chosen above does not support the file we change
      // to the first (and usually only) task supporting the aif file,
      // and use the DefaultInlineExporter with it.

      Task useTask = task;
      Exporter useExp = exp;
      if (!supporting.contains(task)) {
        useTask = (Task)supporting.iterator().next();
        useExp = new DefaultInlineExporter(useTask, "Default inline SGML");
        System.err.println("\tspecified task doesn't support this file -- replacing with " + useTask.getName());
      }
      
      try {
        System.err.println("Reading " + inURI + " with task " + 
                           useTask.getName());
        doc = JawbDocument.fromAIF (inURI, useTask);
      } catch (Exception e) {
        System.err.println("Unable to load " + inPath +
                            "\n\t" + e.getMessage());
        e.printStackTrace();
      }

      if (doc == null) {
        // exportTask is not a supporting task
        System.err.println(inPath + " is not compatible with the " + 
                           useTask.getName() + " task");
        continue; // go on to next file
      }

      // export it
      try {
        AWBDocument awbDoc = (AWBDocument)doc.getAnnotationModel();
        useExp.exportDocument(awbDoc, outURI);
      } catch (Exception e) {
        System.err.println("Unable to export " + inURI.getPath() + ":");
        System.err.println("\t" + e.getMessage());
        continue; // go on to next file (for good measure)
      }
    }
  } // main()

  /**
   * List the Tasks available, and their exporters.
   * 
   * TODO: this currently shows task names, and Exporter format, not actuall
   * class names.
   */
  public static void listTasks() {
    System.out.println("Exporters:");

    Iterator taskIter  = Jawb.getTasks().iterator();
    while (taskIter.hasNext()) {
      Task task = (Task) taskIter.next();
      Exporter[] exporters = task.getExporters();

      if (exporters == null)
        continue;

      // stupid lack of printf
      String taskClass = task.getClass().getName();
      int len = 40 - taskClass.length();
      char[] pad = new char[(len > 0 ? len : 1)];
      Arrays.fill(pad, ' ');
      
      System.out.println(taskClass + new String(pad) + task.getTitle());
      for (int i = 0; i < exporters.length; i++) {
        String exporterName = exporters[i].getClass().getName();
        if (exporterName.equals(DefaultInlineExporter.class.getName()))
          exporterName = "<Default>";
        System.out.println("    " + exporterName);
      }
    }
  } // listTasks()
}
