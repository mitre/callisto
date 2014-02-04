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

package org.mitre.ace2004.callisto;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.*;

import org.mitre.jawb.atlas.TextExtentRegion;
import org.mitre.jawb.atlas.AnnotationModelListener;
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.AWBDocument;
import org.mitre.jawb.atlas.AWBATLASImplementation;
import org.mitre.jawb.atlas.HasSubordinates;
import org.mitre.jawb.atlas.SubordinateSetsAnnotation;
import org.mitre.jawb.atlas.NamedExtentRegions;
import org.mitre.jawb.atlas.MultiPhraseAnnotation;
import org.mitre.jawb.atlas.PhraseTaggingAnnotation;
import org.mitre.jawb.gui.GUIUtils;
import org.mitre.jawb.tasks.AbstractTask;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;
import org.mitre.jawb.tasks.TaskManager;
import org.mitre.jawb.tasks.Exporter;
import org.mitre.jawb.tasks.Importer;
import org.mitre.jawb.prefs.PreferenceItem;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.swing.AutoSelectCaret;

import gov.nist.atlas.ATLASElement;
import gov.nist.atlas.Region;
import gov.nist.atlas.Corpus;
import gov.nist.atlas.Annotation;
import gov.nist.atlas.ATLASAccessException;
import gov.nist.atlas.type.*;
import gov.nist.maia.MAIAScheme;
import gov.nist.maia.MAIALoader;

import org.mitre.jawb.Jawb;
import org.mitre.ace2004.callisto.config.RNGParser;


/**
 * This is the place for any necessary hard-coded information about
 * the task to reside. This class is NOT intended to be extended.
 */
public class Timex2Merger {

  public static final String TIMEX2_ID = "org.mitre.timex2";
  public static final String ACE_ID = "org.mitre.ace2004";

  public static void usage() {
    System.err.println(
      "USAGE: Timex2Merger (timex2.aif.xml ace.aif.xml out.aif.xml)*\n");
  }
  public static void main(String[] args) throws IOException {

    if (args.length < 3) {
      usage();
      System.exit(-1);
    }

    if (args.length %3 != 0) {
      System.err.println("Args must be multiple of 3");
      usage();
      System.exit(-1);
    }

    // Class importerClass;
    // Constructor cons;
    TaskManager tm = Jawb.getTaskManager();
    Task timex2Task = tm.getTaskByName(TIMEX2_ID);
    Task aceTask = tm.getTaskByName(ACE_ID);

    int index = 0;

    while (index < args.length) {

      String timexFile = args[index++];
      String aceFile = args[index++];
      String outFile = args[index++];

      System.err.println("Merging: " + timexFile + "\n" +
                         "         " + aceFile + "\n" +
                         "      -> " + outFile);
    
    AWBDocument timex2Doc = null;
    AWBDocument aceDoc = null;
    
    try {
      timex2Doc = openAIF(timexFile, timex2Task);
    } catch (IOException x) {
      System.err.println("Error opening " + args[0] +
                         " with task " + TIMEX2_ID);
      throw x;
    }
    
    try {
      aceDoc = openAIF(aceFile, aceTask);
      AnnotationModelListener listener =
        new ACE2004ToolKit.ACE2004AnnotationListener((ACE2004Task) aceTask);
      aceDoc.addAnnotationModelListener(listener);
    } catch (IOException x) {
      System.err.println("Error opening " + args[1] +
                         " with task " + ACE_ID);
      throw x;
    }

    int count = 0;

    Iterator iter = timex2Doc.getAllAnnotations();
    while (iter.hasNext()) {
      TextExtentRegion timex2 = (TextExtentRegion) iter.next();
      if (timex2.getAnnotationType().getName().equals("TIMEX2")) {
        int start = timex2.getTextExtentStart();
        int end = timex2.getTextExtentEnd();

        TextExtentRegion qMention = (TextExtentRegion)
          aceDoc.createAnnotation(ACE2004Task.QUANTITY_MENTION_TYPE_NAME);
        qMention.setTextExtents(start, end);

        AWBAnnotation quantity = ACE2004Task.getMentionParent(qMention);
        try {
          quantity.setAttributeValue("type", "TIMEX2");
          quantity.setAttributeValue("timex2_val",
                                     timex2.getAttributeValue("val"));
          quantity.setAttributeValue("timex2_mod",
                                     timex2.getAttributeValue("mod"));
          quantity.setAttributeValue("timex2_anchor_val",
                                     timex2.getAttributeValue("anchor-val"));
          quantity.setAttributeValue("timex2_anchor_dir",
                                     timex2.getAttributeValue("anchor-dir"));
          quantity.setAttributeValue("timex2_set",
                                     timex2.getAttributeValue("set"));
          quantity.setAttributeValue("timex2_non_specific",
                                     timex2.getAttributeValue("non-specific"));
          quantity.setAttributeValue("timex2_comment",
                                     timex2.getAttributeValue("comment"));
          count++;
        } catch (Exception why) {
          why.printStackTrace();
        }
      }
    }


    File out = new File(outFile);
    aceDoc.save(out.toURI(), false);

    System.err.println("   tags: " + count);
    
    }
  } // main()

  public static AWBDocument openAIF(String name, Task task)
    throws IOException {
    
    AWBDocument doc = null;
    File file = null;
    URI uri = null;
      
    try {
      file = new File(name).getCanonicalFile();
      uri = file.toURI();
    } catch (IOException x) {
      System.err.println("Error creating file: " + name);
      throw x;
    }

    try {
      doc = AWBDocument.fromAIF(uri, task);
    } catch (IOException x) {
      System.err.println("Error opening document: " + name);
      throw x;
    }

    return doc;
  }
}

