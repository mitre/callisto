package org.mitre.jawb.tasks.preannotate;

import java.io.*;

public class LPTools {
  protected static String runLocalCommand(String command) throws LPException  {
    Process p;
    String stderr = "";

    System.out.println("callisto: executing: \"" + command + "\"");

    try { 
      p = Runtime.getRuntime().exec(command);

      BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));

      System.out.println("callisto: waiting for child process to terminate...");

      boolean cont = true;
      int ev;
      while(cont) {
	  try {
	      ev = p.exitValue();
	      cont = false;
	  }
	  catch(IllegalThreadStateException e) {
	      System.err.println("java still sucks");
	      while(err.ready()) {
		  String s = err.readLine();
		  System.out.println("* " + s);
		  stderr += s + "\n";
	      }
	      Thread.sleep(1000);
	  }
      }

      while(err.ready()) {
	String s = err.readLine();
	System.out.println("* " + s);
	stderr += s + "\n";
      }
    }
    catch(InterruptedException e) { throw new LPException("execution error: " + e.getMessage()); }
    catch(IOException e) { throw new LPException("execution error: " + e.getMessage()); }

    if(p.exitValue() != 0) throw new LPException("non-zero exit code from command \"" + command + "\": \"" + stderr + "\"");
    
    return stderr;
  }
  
  public static File fileFromString(String s, File fileOrDir) throws IOException {
    File ret;

    if((fileOrDir == null) || fileOrDir.isDirectory()) {
      ret = File.createTempFile("callisto", ".tmp", fileOrDir);
    }
    else {
      ret = fileOrDir;
    }
    
    Writer w = new FileWriter(ret);

    System.out.println("writing string \"" + s + "\" to " + ret.toString());
    w.write(s);
    w.close();
    return ret;
  }

  public static String stringFromFile(File f) throws IOException {
    BufferedReader r = new BufferedReader(new FileReader(f));
    String ret = "";

    String s;
    while((s = r.readLine()) != null) { ret += s + "\n"; }
    r.close();
    return ret;
  }

}
