package org.mitre.jawb.tasks.preannotate;

import java.rmi.Naming;
import java.rmi.RemoteException;

public class LPClient {
  public static String DEFAULT_HOST = "chubby";
  public static String DEFAULT_PORT = "9999";

  public static void main(String args[]) {
    String name = null, host, port;

    if(args.length > 0) { name = args[0]; }
    else {
      System.err.println("need at least one arg: the LP name");
      System.exit(-1);
    }

    if(args.length > 1) { host = args[1];  }
    else { host = DEFAULT_HOST; }
    
    if(args.length > 2) { port = args[2];  }
    else { port = DEFAULT_PORT; }

//    String id = "//" + host + ":" + port + "/" + name;
    String id = "//" + host + "/" + name;
    
    System.out.println("connecting to \"" + id + "\"...");
    LP lp = null;
    try {
      lp = (LP)Naming.lookup(id);
      
      System.out.println("name: " + lp.getName());
      System.out.println("description: " + lp.getDesc());
    } catch (Exception e) {
      System.out.println("LPClient exception: " + e.getMessage());
      e.printStackTrace();
    }
  }
}

