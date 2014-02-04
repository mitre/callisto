package org.mitre.jawb.tasks.preannotate;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.RMISecurityManager;
import java.rmi.server.UnicastRemoteObject;

public class LPServer extends UnicastRemoteObject implements LP {
  public static int DEFAULT_PORT = 9999;
  public static LP[] lps = new LP[] {
    new Phrag("phrag-en-ne5",
	      Phrag.DEFAULT_SPEC_DIR + "/phrag-ne5.spec",
	      Phrag.DEFAULT_DB_DIR + "/phrag-en-ne5.db",
	      "",
	      "",
	      Phrag.DEFAULT_TRAIN_DIR,
	      "English named-entity (phrag)",
	      "org.mitre.muc"),
    new Carafe("carafe-muc",
	       Carafe.DEFAULT_LEX_DIR,
	       Carafe.DEFAULT_TAG_FILE,
	       Carafe.DEFAULT_MODEL_DIR+"/muc.model",
	       Carafe.DEFAULT_RUN_DIR,
	       Carafe.DEFAULT_TRAIN_DIR,
	       "English named-entity (carafe)",
	       "org.mitre.muc")
  };

  private LP lp;

  public String getTask() throws RemoteException { return lp.getTask(); }
  public String getDesc() throws RemoteException { return lp.getDesc(); }
  public String getName() throws RemoteException { return lp.getName(); }

  public String tag(String contents, String mimeType) throws LPException, RemoteException { return lp.tag(contents, mimeType); }
  public void train(String contents, String docID) throws LPException, RemoteException { lp.train(contents, docID); }

  
  public LPServer(LP lp, int port) throws RemoteException, java.net.MalformedURLException {
    super(port);
    this.lp = lp;

//    String id = "//chubby:" + port + "/" + lp.getName();
    String id = lp.getName();
    System.out.println("registering " + lp + " on " + id);
    Naming.rebind(id, lp);
  }
    
  public static void main(String args[]) {
    int port = DEFAULT_PORT;

    if(args.length == 1) try {
      port = Integer.parseInt(args[0]);
    }
    catch(NumberFormatException e) { }


    // Create and install a security manager
    /*
      if (System.getSecurityManager() == null) {
      System.setSecurityManager(new RMISecurityManager());
      }
    */

    for(int i = 0; i < lps.length; i++) {
      try {
	new LPServer(lps[i], port);
      }
      catch (Exception e) {
	System.out.println("LPServer error: " + e.getMessage());
	e.printStackTrace();
      }
    }
  }
}
