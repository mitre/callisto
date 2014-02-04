package org.mitre.jawb.tasks.preannotate;

import java.io.*;

/* this currently needs to be configured for a particular instance of carafe */

public class Carafe implements LP, Serializable {
  private static final String CARAFE = "/afs/rcf/lang/bin/perl /afs/rcf/project/tallal/wellner/callisto/src/org/mitre/jawb/tasks/preannotate/carafe-wrapper-run.pl";
  private static final String CARAFE_TRAIN = "/afs/rcf/lang/bin/perl /afs/rcf/project/tallal/wellner/callisto/src/org/mitre/jawb/tasks/preannotate/carafe-wrapper-train.pl";

  public static final String DEFAULT_TRAIN_DIR = "/afs/rcf/project/tallal/wellner/callisto/carafe/train/";
	public static final String DEFAULT_RUN_DIR = "/afs/rcf/project/tallal/wellner/callisto/carafe/run";
	public static final String DEFAULT_MODEL_DIR = "/afs/rcf/project/tallal/wellner/callisto/carafe/model/";
	public static final String DEFAULT_LEX_DIR = "/afs/rcf/project/rcii/resources/carafe-wordlists/";
	public static final String DEFAULT_TAG_FILE = "/afs/rcf/project/rcii/resources/carafe-tagsets/tagset.muc2";

	public static final String DEFAULT_SUFFIX = ".carafe-out";

  private String lexDir, tagFile, runDir, trainDir, model;
  private String desc, name;
  private String task;

  
  public Carafe(String name, String lexDir, String tagFile, String model, String runDir, String trainDir, String desc, String task) {
		this.runDir = runDir;
		this.lexDir = lexDir;
		this.tagFile = tagFile;
		this.trainDir = trainDir;
		this.model = model;
		this.desc = desc;
		this.task = task;
		this.name = name;
  }

  public String getDesc() { return desc; }
  public String getTask() { return task; }
  public String getName() { return name; }

  public synchronized String tag(String contents, String mimeType) throws LPException {
    File inFile, outFile;
    String ret = null;

    try {
      inFile = LPTools.fileFromString(contents, new File(runDir+"/"+"callisto.tmp"));
      outFile = new File(inFile.toString() + DEFAULT_SUFFIX);

      String command = CARAFE + " " + DEFAULT_RUN_DIR + " " + "callisto.tmp" + " " + model + " " + lexDir +"/"+ " "+DEFAULT_SUFFIX;
      LPTools.runLocalCommand(command);

      ret = LPTools.stringFromFile(outFile);
    }
    catch(IOException e) { throw new LPException("error tagging file with carafe: " + e.getMessage()); }

    return ret;
  }

  public synchronized void train(String contents, String docID) throws LPException {
    System.out.println("carafe train: file contents in " + trainDir + "/" + docID + "carafe-train.sgml");
    
    try {
      File f = LPTools.fileFromString(contents, new File(trainDir, docID + ".carafe-train.sgml"));
      String command = CARAFE_TRAIN + " " + trainDir + " " + model + " " + tagFile + " " + lexDir+"/";
      LPTools.runLocalCommand(command);
    }
    catch(IOException e) { e.printStackTrace(System.out); throw new LPException("error training file with carafe: " + e.getMessage()); }

  }
}
