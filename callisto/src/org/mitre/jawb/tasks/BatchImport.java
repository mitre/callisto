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
 *  BatchImport
 *
 *  The first argument is the name of an Importer sublcass, such as
 *  org.mitre.timex2.callisto.ImportTimex2 or the name of a task, such
 *  as org.mitre.xg.callisto.XGTask.  If a task is provided instead of
 *  an importer, the DefaultInlineImporter is used for that Task.
 *
 * TODO provide means to specify other Importers that take Task and
 * potentially other args...
 *
 *  The remaining args are files to be imported using that importer.
 *  They will be skipped if they are not AIF files supported by the
 *  task associated with the Importer subclass (org.mitre.timex2 in
 *  the example above)
 *
 */
public class BatchImport {

  private static int DEBUG = 0;

  public static void usage() {
    String usage = 
      "USAGE: batch-import -t\n" +
      "  List available Tasks, and their importers.\n" +
      "USAGE: batch-import [-f] [-x ext] {importer-class} aif-file [...]\n" +
      "  Import .aif files using importer specified.\n" +
      "USAGE: batch-import [-f] [-x ext] {task-class} aif-file [...]\n" +
      "  Import .aif files using the default importer of the Task specified.\n\n" +
      "  -f       force creation of output files even if they already exist\n" +
      "           otherwise an error is printed on stderr and no file is written\n" +
      "  -x ext   extension appended to base of imported files. default=\"aif.xml\"\n" +
      "  Input format is always Importer dependant.\n" +
      "  Imported files names are based on the input file name:\n" +
      "    in:  {base}[.sgml]\n" +
      "    out: {base}.{ext}\n";

    System.err.println(usage);
  }

  public static void main(String[] args) {

    // Class importerClass;
    // Constructor cons;
    Task task = null;
    Importer imp = null;
    String spec = null;
    String extension = ".aif.xml";
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
        break; // first non-option is import spec, all others are files
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
      error = "Import specification required";
    }

    if (error != null) {
      System.err.println("ERROR: " + error + "\n");
      usage();
      System.exit(-1);
    }

    // Jawb.initTasks(); 
    // instead do this, which inits the tasks, and gets them from the
    // taskManager initialized with them, then grabs an iterator.
    Iterator taskIter  = Jawb.getTasks().iterator();
    while (taskIter.hasNext() && imp == null) {
      // if no importer is found, method returns so inconsistency not possible
      task = (Task) taskIter.next();
      
      //System.err.println ("next task is " + task);
      try {
        // see if user specified the task
        if (task.getClass().getName().equals(spec)) {
          //System.err.println(spec + " is a Task");
          imp = new DefaultInlineImporter(task, "Default inline SGML");

        } else {
          // it's not the tasks, try the task's importers
          Importer[] importers = task.getImporters();

          if (importers == null)
            continue;

          for (int i = 0; i < importers.length; i++) {
            if (importers[i].getClass().getName().equals(spec)) {
              //System.out.println(spec+" is an Importer of "+task.getName());
              imp = importers[i];
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (imp == null) {
      System.err.println("No known Task or Importer with class name: " + spec);
      return;
    }

    System.err.println("Importing using: " + task.getName() +
                       ":" + imp.getFormat());
    

    AWBDocument doc = null;
      
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

        if (outPath.endsWith(".sgml")) {
          outPath = inPath.substring(0, inPath.length() - 5);
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

      System.err.println("Importing: '" + inPath +
                         "'\n       ->  '" + outPath + "'");
      

      /*** not needed for Import
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
        }  ***/
      

      /**************************************************************
       *  Import the Document
       **************************************************************/

      // TODO add command line flag for encoding, instead of
      // hardcoding UTF-8 here
      try {
        doc = imp.importDocument(inURI, "UTF-8");
      } catch (Exception e) {
        System.err.println("Unable to import " + inPath +
                           "\n\t" + e.getMessage());
        e.printStackTrace();
      }

      /**************************************************************
       *  Save it as AIF
       **************************************************************/

      try {
        boolean success = doc.save(outURI, false);
      } catch (Exception e) {
        System.err.println("Unable to save " + outURI.getPath() +
                           "\n\t" + e.getMessage());
        e.printStackTrace();
      }

    }
  } // main()

  /**
   * List the Tasks available, and their importers.
   * 
   * TODO: this currently shows task names, and Importer format, not actuall
   * class names.
   */
  public static void listTasks() {
    System.out.println("Importers:");

    Iterator taskIter  = Jawb.getTasks().iterator();
    while (taskIter.hasNext()) {
      Task task = (Task) taskIter.next();
      Importer[] importers = task.getImporters();

      if (importers == null)
        continue;

      // stupid lack of printf
      String taskClass = task.getClass().getName();
      int len = 40 - taskClass.length();
      char[] pad = new char[(len > 0 ? len : 1)];
      Arrays.fill(pad, ' ');
      
      System.out.println(taskClass + new String(pad) + task.getTitle());
      for (int i = 0; i < importers.length; i++) {
        String importerName = importers[i].getClass().getName();
        if (importerName.equals(DefaultInlineImporter.class.getName()))
          importerName = "<Default>";
        System.out.println("    " + importerName);
      }
    }
  } // listTasks()
}
