package org.mitre.jawb.tasks.preannotate;

import java.rmi.UnknownHostException;
import java.util.Vector;

import org.mitre.jawb.tasks.*;
import org.mitre.jawb.Jawb;

public class TallalController {
    private static TallalController controller = null;
    
    private Vector systems;
    private Vector availableLPs;

    public static TallalController getController() {
	if(controller == null) {
	    controller = new TallalController();
	}

	return controller;
    }

    public TallalController() {
	systems = new Vector();
        availableLPs = new Vector();

        // TODO Retrieve from preferences
        String rmiHost = "chubby.mitre.org";

	try {
	  System.out.println("trying to connect to LPs on " + rmiHost + "...");

	  LP phrag = (LP)java.rmi.Naming.lookup("//"+rmiHost+"/phrag-en-ne5");
	  LP carafe = (LP)java.rmi.Naming.lookup("//"+rmiHost+"/carafe-muc");

	  availableLPs.add(phrag);
	  availableLPs.add(carafe);

	  System.out.println("Success! LPs retrieved via RMI");
	}
	catch(UnknownHostException e) {
	  System.out.println("Unable to find host: " + rmiHost);
          initializeLocalLPs();
        }
	catch(Exception e) {
	  System.out.println("Error doing RMI: " + e.getMessage());
	  e.printStackTrace();
          initializeLocalLPs();
	}

	TallalSystem sys = new TallalSystem("enamex-en");
	sys.setDesc("english muc5 (hmm)");
	sys.setTask(Jawb.getTaskManager().getTaskByName("org.mitre.muc"));
	sys.setLP((LP)availableLPs.get(0));
	sys.setDataOrURL("this would be a path, if phrag actually paid attention to this");
	sys.setTags(new String[] { "one", "two", "three" });
	systems.add(sys);

	sys = new TallalSystem("enamex-en-ben");
	sys.setDesc("english muc5 (crf)");
	sys.setTask(Jawb.getTaskManager().getTaskByName("org.mitre.muc"));
	sys.setLP((LP)availableLPs.get(1));
	sys.setDataOrURL("this would be a path, if carafe actually paid attention to this");
	sys.setTags(new String[] { "one", "two", "three" });
	systems.add(sys);
    }

  private void initializeLocalLPs() {
    System.out.println("Falling back to local system.");

    LP phrag = new Phrag("phrag-ne5-local",
                         Phrag.DEFAULT_SPEC_DIR + "/phrag-ne5.spec",
                         Phrag.DEFAULT_DB_DIR + "/phrag-en-ne5.db",
                         "",
                         "",
                         Phrag.DEFAULT_TRAIN_DIR,
                         "English named-entity (phrag)",
                         "org.mitre.muc");
    
    LP carafe = new Carafe("carafe-ne5-local",
                           Carafe.DEFAULT_LEX_DIR,
                           Carafe.DEFAULT_TAG_FILE,
                           Carafe.DEFAULT_MODEL_DIR+"/muc.model",
                           Carafe.DEFAULT_RUN_DIR,
                           Carafe.DEFAULT_TRAIN_DIR,
                           "English named-entity (carafe)",
                           "org.mitre.muc");
    availableLPs.add(phrag);
    availableLPs.add(carafe);
  }

    public void addSystem(TallalSystem sys) {
	systems.add(sys);
    }

    public void removeSystem(TallalSystem sys) {

	systems.removeElement(sys);

    }

    public Vector getSystems() { return systems; }

    public LP[] getLPs() {
      return (LP[]) availableLPs.toArray(new LP[0]);
    }
}
	
