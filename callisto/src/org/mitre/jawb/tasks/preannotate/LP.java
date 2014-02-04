package org.mitre.jawb.tasks.preannotate;

import java.io.File;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LP extends Remote {
  public String getTask() throws RemoteException;
  public String getDesc() throws RemoteException;
  public String getName() throws RemoteException;

  public String tag(String contents, String mimeType) throws LPException, RemoteException;
  public void train(String contents, String docID) throws LPException, RemoteException;
}
