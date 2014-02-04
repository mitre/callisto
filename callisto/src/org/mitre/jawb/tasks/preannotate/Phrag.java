package org.mitre.jawb.tasks.preannotate;

import java.io.*;

/* this currently needs to be configured for a particular instance of phrag */

public class Phrag implements LP, Serializable {
  private static final String PHRAG = "/afs/rcf/lang/bin/perl /afs/rcf/project/tallal/wmorgan/callisto/src/org/mitre/jawb/tasks/preannotate/phrag-wrapper-run.pl";
  private static final String PHRAG_TRAIN = "/afs/rcf/lang/bin/perl /afs/rcf/project/tallal/wmorgan/callisto/src/org/mitre/jawb/tasks/preannotate/phrag-wrapper-train.pl";

  public static final String DEFAULT_SPEC_DIR = "/afs/rcf/project/tallal/wmorgan/phrag-1.5.3/share/phrag";
  public static final String DEFAULT_DB_DIR = "/afs/rcf/project/tallal/wmorgan/callisto/phrag/db";
  public static final String DEFAULT_TRAIN_DIR = "/afs/rcf/project/tallal/wmorgan/callisto/phrag/train";

  private String specFile, args, trainArgs, trainDir, db;
  private String desc, name;
  private String task;
  
  public Phrag(String name, String specFile, String db, String args, String trainArgs, String trainDir, String desc, String task) {
    this.specFile = specFile;
    this.args = args;
    this.trainArgs = trainArgs;
    this.db = db;
    this.task = task;
    this.desc = desc;
    this.trainDir = trainDir;
    this.name = name;
  }

  public String getDesc() { return desc; }
  public String getTask() { return task; }
  public String getName() { return name; }

  public synchronized String tag(String contents, String mimeType) throws LPException {
    File inFile, outFile;
    String ret = null;

    try {
      inFile = LPTools.fileFromString(contents, null);
      outFile = new File(inFile.toString() + ".phragout");

      String command = PHRAG + " " + inFile.toString() + " " + outFile.toString() + " " + mimeType + " " + specFile + " hmm.*.db " + db + " " + args;
      LPTools.runLocalCommand(command);

      ret = LPTools.stringFromFile(outFile);
    }
    catch(IOException e) { throw new LPException("error tagging file with phrag: " + e.getMessage()); }

    return ret;
  }

  public synchronized void train(String contents, String docID) throws LPException {
    System.out.println("phrag train: file contents in " + trainDir + "/" + docID + "phrag-train.sgml");
    
    try {
      File f = LPTools.fileFromString(contents, new File(trainDir, docID + ".phrag-train.sgml"));
      String command = PHRAG_TRAIN + " " + f.toString() + " " + trainDir + " " + specFile + " hmm.*.db " + db + " " + trainArgs;
      LPTools.runLocalCommand(command);
    }
    catch(IOException e) { e.printStackTrace(System.out); throw new LPException("error training file with phrag: " + e.getMessage()); }

  }
}
