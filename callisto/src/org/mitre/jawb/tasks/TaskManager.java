
package org.mitre.jawb.tasks;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Responsible for loading and verifying correctness of Task classes.
 */
public class TaskManager {

  private static final int DEBUG = 0;

  /** Cache of loaded tasks mapped to task name */
  private Map tasks;
  /** Layer for task defaults */
  private Properties taskDefaultProps;
  /** Layer for setting task props while loading, which can be easily cleared */
  private Properties taskTempProps;
  /** Reader for spec files in task using directories, not jars */
  private SAXReader saxReader;

  /**
   * Init a task manager. Equivalent to {@link #TaskManager(Properties)} with a
   * null argument.
   */
  public TaskManager() {
    this(null);
  }
  
  /**
   * Initialize a task manager with a place to put task properties
   * @param taskProps task manager will store Task specified defaults
   * here, or create a new one if taskProps is null.
   * @see #getDefaultProperties
   */
  public TaskManager(Properties taskProps) {
    tasks = new LinkedHashMap();
    taskDefaultProps = (taskProps == null ? new Properties() : taskProps);
    taskTempProps = new Properties();
    
    // prepare to read config files
    //Map namespace = new HashMap();
    //namespace.put("foo", "http://callisto.mitre.org/ns/task/1.0");
    //DocumentFactory factory = new DocumentFactory();
    //factory.setXPathNamespaceURIs(namespace);
    saxReader = new SAXReader();
    //saxReader.setDocumentFactory(factory);
    saxReader.setIncludeExternalDTDDeclarations (false);
  }

  /**
   * Return an unmodifiable List of Task Objects currently available to the
   * application. These are all guaranteed to return unique Strings as their
   * names, see {@link Task#getName} for more information on Task names.
   * @see Task
   * @return Tasks objects available for annotating.
   */
  public List getTasks () {
    return Collections.unmodifiableList (new LinkedList(tasks.values()));
  }

  public Task getTaskByName(String name) {
    return (Task)tasks.get(name);
  }
  
  /**
   * Retrieve the properties that the tasks loaded by default.
   */
  public Properties getDefaultProperties () {
    return taskDefaultProps;
  }
  
  /**
   * Look through directories specified in a properties object for "*.jar"
   * files and try to load tasks from them.  Properties are specified by
   * appending an integer to <code>base</code> in the manner specified by
   * initTasks. Jar's in the specified directories are loaded in platform
   * dependant order, defined by the order they are returned from
   * <code>File.list()</code>, called on the file objects for the directory.<p>
   *
   * Note: this was moved from {@link org.mitre.jawb.Jawb} and the initTasks
   * method is still there.<p>
   *
   * @param base base name of the properties that will have increasing
   * integers appended to create the property names.
   * @param properties properties object to load the class names from
   *
   * @see File#list
   */
  public void scanTaskDirs (String base, Properties properties) {
    int num = 0;
    String dirName = null;
    if (DEBUG> 0)
      System.err.println ("Scanning Task Dirs: "+base);

    while ( (dirName = properties.getProperty (base+"."+num++)) != null)
      scanTaskDir (new File (dirName));
  }

  /**
   * Simply load the tasks specified in the properties from the default
   * classpath, with no other meta info.
   * @see #scanTaskDirs for the loading mechanism
   */
  public void loadTaskClasses (String base, Properties properties) {
    // TODO: combine with loadTaskJar since there's duplication
    int num = 0;
    String className = null;
    if (DEBUG > 0)
      System.err.println ("Loading Classes for: "+base);

    while ( (className = properties.getProperty (base+"."+num++)) != null) {
      System.err.println ("Loading Classes specified in: "+base+".x");
      TMTaskDef taskDef = new TMTaskDef();
      taskDef.classname = className;
      
      loadTask(taskDef); // no more verification
    }
  }

  public void scanTaskDir (File dir) {
    // don't bother if missing
    if (dir == null || ! dir.isDirectory () || ! dir.canRead ()) {
      if (DEBUG > 0)
        System.err.println ("    Can't read directory: "+dir);
      return;
    }

    File[] files = dir.listFiles (new FilenameFilter() {
        public boolean accept (File dir, String name) {
          return (name.toLowerCase().endsWith(".jar")
                  || new File(dir,name).isDirectory());
        }
      });
    if (files.length > 0 || DEBUG > 0)
      System.err.println ("Scanning Task Directory: "+dir);
    if (DEBUG > 0)
      System.err.println("    found "+files.length+" files");

    for (int i=0; i<files.length; i++) {
      File file = files[i];
      if (file.isDirectory())
        loadTaskDir(file);
      else
        loadTaskJar(file);
      if (DEBUG> 0)
        System.err.println ("\n-------------");
    }
  }

  /**
   * Loads all tasks in a jar file.<p>
   *
   * @param taskJar '*.jar' file which contains Jawb tasks. Each task file
   * is expected to have a file "task.properties" which will describe the
   * contents of the jar file.
   * @see #initTasks
   * @return true on success, false otherwise
   */
  private boolean loadTaskJar (File taskJar) {
    // TODO: There is a small problem with this mechanism: if directories are
    // on the classpath which contain a file 'task.properties' then it will be
    // returned by 'getResource', NOT the task.properties that is in the jar
    // file.  This is because of the delegation model used by ClassLoaders.  One
    // possible solution is to use 'findResources' and compare the URL's,
    // loading the appropriate one. Another mechanism is to create a ClassLoader
    // subclass which loads looks in the jar file first, /then/ delegates to the
    // parent.
    if (DEBUG > 0)
      System.err.println ("TaskMgr.loadTaskJar: "+taskJar);
    ClassLoader loader;
    Properties props;

    try {
      // load the reference file in the 'root path' of the jar file and look
      // for tasks specified in it.
      TMTaskDef taskDef = new TMTaskDef();
      taskDef.basejar = taskJar;
      taskDef.loader = new URLClassLoader (new URL[] {taskJar.toURL()});
      props = new Properties ();
      
      if (DEBUG>0) {
        System.err.println("TaskMgr.loadTaskJar: task.properties is\n    " +
                           taskDef.loader.getResource("task.properties"));
        System.err.println("     loader="+taskDef.loader.hashCode());
      }

      InputStream in = taskDef.loader.getResourceAsStream ("task.properties");
      if (in == null) {
        System.err.println ("No Callisto tasks defined in: "+taskJar);
        return false;
      }
      props.load (in);
      in.close ();
      
      if (DEBUG > 0) {
	System.err.println("jarProps:");
	props.list(System.out);
      }

      int num = 0;
      int startCount = tasks.size();
      String base = "task.class";

      for (; (taskDef.classname=props.getProperty (base+"."+num)) != null; num++) {
        if (DEBUG > 0)
          System.err.println ("TaskMgr.loadTaskJar: prop="+base+"."+num+
                              " classname="+taskDef.classname);
        
        loadTask(taskDef); // no more verification
      }
      // taskList may have already had some, can't rely on its size
      return startCount < tasks.size();
          
    } catch (Exception io) {
      System.err.println ("Error loading tasks in: "+taskJar);
      io.printStackTrace ();
    }
    // Only reachable by catching an exception
    return false;
  }
      

  /**
   * Loads a task from a 'plugin' directory by parsing its "./plugin.xml" file.
   * May include various locations of class files, config files, and native
   * code.
   */
  public boolean loadTaskDir(File taskDir) {
    // See the comment at the beginning of loadTaskJar
    if (DEBUG > 0)
      System.err.println ("TaskMgr.loadTaskDir: "+taskDir);

    File spec = new File(taskDir, "plugin.xml");
    if (! spec.exists()) {
      System.err.println("Error: Cannot find file: "+spec);
      return false;
    }
    
    try {
      int startCount = tasks.size();
      Document doc = saxReader.read(spec);

      Iterator iter = doc.selectNodes("//task").iterator();
      while (iter.hasNext()) {
        Element taskElement = (Element) iter.next();
        try {
          TMTaskDef taskDef = new TMTaskDef();
          taskDef.pluginSpec = spec;
          taskDef.basedir = taskDir;
          readTaskSpec (taskElement, taskDef);
          
          URL[] cp = (URL[]) taskDef.classpathURLs.toArray(new URL[0]);
          taskDef.loader = new URLClassLoader (cp);
          
          Task task = loadTask(taskDef);
          
        } catch (Exception x) {
          System.err.println(x.getMessage());
          x.printStackTrace();
        }
      }
      // taskList may have already had some, can't rely on its size
      return startCount < tasks.size();
      
    } catch (Exception x) {
      System.err.println("Error loading: "+spec+"\n"+x.getMessage());
      x.printStackTrace ();
    }
    return false;
  }

  /**
   * Check dynamically loaded object from its string name, to make sure it
   * implements the Task interface.  Once verified, an instance is created and
   * returned. To load, it is first checked for a <code>getInstance()</code>
   * method which returns a Task object, which is used if found.  If not
   * found, the object is instantiated using a no arg constructor. If both
   * fail, no object is created and <code>null</code> is retruned.<p>
   *
   * @param taskDef Structure of data used to loadTask
   *
   * @see #initTasks
   *
   * @return Task object created from named task, or <code>null</code> on
   * error.
   *
   */
  private Task loadTask(TMTaskDef taskDef) {
    if (DEBUG > 0)
      System.err.println ("TaskMgr.loadTask: "+taskDef.classname);

    // all the throws w/in the same block seem excessive, but also the easiest
    // way to get the same behavior from all load errors
    try {
      // TODO: put this back in somehow: setSplashProgress (-1, className);
      ClassLoader loader = taskDef.loader;
      if (loader == null)
        loader = this.getClass().getClassLoader();
      if (DEBUG > 0)
        System.err.println ("    loader="+loader.hashCode()+
                            " classname="+taskDef.classname);
      
      Class taskClass = Class.forName (taskDef.classname, true, loader);
      if (DEBUG > 0)
        System.err.println ("    class for name= " + taskClass);
      Method getInstance = null;
      Task task = null;
      
      if (! Task.class.isAssignableFrom (taskClass))
        throw new RuntimeException (taskDef.classname+" is not a Task object");

      // TODO: this needs to be removed if we can get the taskTempProps to back
      // the default props. it would allow us to remove the badness easily on
      // failure now they just end up staying if there's a problem
      taskDefaultProps.putAll(taskTempProps);

      // TODO: This is a pretty hackish way of setting the temp dir: during
      // initialization we give the task either it's base directory, or it's
      // Jar file, which it can use as it see's fit. Potential for danger if a
      // task changes from one to the other.
      try {
        Class[] args = new Class[] { File.class } ;
        getInstance = taskClass.getMethod ("getInstance", args);
                                           
      } catch (NoSuchMethodException nsme) { /* use default constructor */ }
      
      System.err.println ("  Loading "+taskClass.getName());

      if (getInstance != null) {
        // if loaded from directory, basejar will be null.
        File base = taskDef.basejar;
        if (base == null)
          base = taskDef.basedir;
        task = (Task)getInstance.invoke(null, new Object[]{base});
      }
      else
        task = (Task)taskClass.newInstance();

      if (task == null) // a return of null from getInstance is failure
        throw new RuntimeException ("Loading Task failed for unknown cause");

      // if taskDef.name is set, verify that runtime name matches
      if (taskDef.name != null && ! task.getName().equals(taskDef.name))
        throw new RuntimeException("Spec name doesn't match Task name: "+
                                   "\n\tSpecified: "+task.getName()+
                                   "\n\tFound:     "+taskDef.name);
      
      // check for name conflicts
      if (tasks.get(task.getName()) != null)
        throw new RuntimeException("Duplicate task names: "+task.getName());
      
      // test validity of MAIA URIs
      URI uri = task.getMaiaURI ();
      if (uri == null || ! uri.isAbsolute ())
        throw new RuntimeException("Invalid external MAIA URI in Task: "+uri);
      uri = task.getLocalMaiaURI ();
      if (uri == null || ! uri.isAbsolute ())
        throw new RuntimeException("Invalid local MAIA URI in Task: "+uri);

      // add preferences, making sure they're in the tasks namespace.
      String namespace = "task."+task.getName()+".";
      Map taskPrefs = task.getDefaultPreferences ();
      if (DEBUG > 5) 
        System.err.println("TaskManager.loadTask: defaultPrefs: " +
                           taskPrefs);
      Iterator prefKeys = taskPrefs.keySet ().iterator ();
      while (prefKeys.hasNext ()) {
        String key = (String)prefKeys.next();
        if (! key.startsWith (namespace))
          throw new IllegalStateException
            ("Task Preference keys must start with the task namespace:"+
             "\n Task="+task.getName()+
             "\n Namespace="+namespace+"\n Key="+key);
        // add to temp properties
        taskTempProps.setProperty (key, (String)taskPrefs.get (key));
      }
      if (DEBUG > 5) 
        System.err.println("TaskManager.loadTask: taskTempProps: " +
                           taskTempProps.toString());
      // store any spec specified properties permanently
      taskDefaultProps.putAll(taskTempProps);

      if (DEBUG > 0)
        System.err.println("TaskMgr.loadTaskDir: Success");
      System.err.println ("   Ready: "+task.getTitle ()+" ("+
                          task.getVersion()+") ["+task.getName ()+"]");
      
      tasks.put(task.getName(), task);
      
    } catch (Exception e) {
      if (DEBUG > 0)
        System.err.print("TaskMgr.loadTaskJar: ");
      System.err.println ("\nFailed to load Task: "+taskDef.classname);
      
      if (e.getCause () != null)
        e.getCause ().printStackTrace ();
      else
        e.printStackTrace ();

    } catch (LinkageError e) { // Numerous development problems caught here
      if (DEBUG > 0)
        System.err.print("TaskMgr.loadTaskJar: ");
      System.err.println ("\nFailed to load Task: "+taskDef.classname);
      
      if (e.getCause () != null)
        e.getCause ().printStackTrace ();
      else
        e.printStackTrace ();
    }
    // ensure these are cleared
    taskTempProps.clear();

    return null;
  }

  /* ***********************************************************
   * XML Parsing routines... ant like. Break them out somewhere
   * ***********************************************************/
  
  /** Data passing struct. */
  private class TMTaskDef {
    File pluginSpec;
    File basejar; // null when loaded from directory
    File basedir; // null when loaded from jar
    String name;
    String classname;
    List classpathURLs = new LinkedList(); // force existence
    ClassLoader loader;
  }
  
  private void readTaskSpec(Element taskElement, TMTaskDef taskDef) {
    // when task is loaded, this is verified against the runtime name
    taskDef.name = taskElement.attributeValue("name");

    taskDef.classname = taskElement.attributeValue("classname");
    if (taskDef.classname == null)
      parseError (taskElement,"classname attribute required");
    
    readClassPath(taskElement, taskDef);
    readProperties(taskElement, taskDef);
  }

  private void readProperties(Element element, TMTaskDef taskDef) {
    Iterator iter = element.selectNodes("property").iterator();
    while (iter.hasNext()) {
      Element prop = (Element) iter.next();
      String name = prop.attributeValue("name");
      String value = prop.attributeValue("value");
      if (name == null || value == null)
        parseError (prop,"<property> requires 'name' and 'value'");
      taskTempProps.setProperty(name, value);
    }
  }
  
  private void readClassPath(Element taskElement, TMTaskDef taskDef) {
    List pathDirList = new LinkedList();
    String classPath = taskElement.attributeValue("classpath");

    if (classPath != null) {
      StringTokenizer st = new StringTokenizer(classPath);
      while(st.hasMoreTokens()) {
        String path = st.nextToken();
        try {
          File file = null;
          URI uri = new URI (path);
          if (uri.isAbsolute())
            file = new File(uri);
          else
            file = new File(taskDef.basedir, path);


          if (! file.exists())
            parseWarn(taskElement,"classpath element "+
                      file+" not found");
          else
            taskDef.classpathURLs.add(file.toURL());

        } catch (Exception x) {
          System.err.println("Error reading classpath elements of "
                             + taskDef.pluginSpec + ": " + x.getMessage());
          x.printStackTrace();
        }
      }
    }
    
    // later, support multiple entries as an ant fileset
    if (taskElement.element("classpath") != null)
      parseWarn(taskElement,"<classpath> is not yet supported as sub-element");
  }
  
  private void parseError(Element element, String msg) {
    throw new RuntimeException ("Error: "+elementXPath(element)+"\n"+msg);
  }
  private void parseWarn(Element element, String msg) {
    System.err.println ("Warning: "+elementXPath(element)+"\n"+msg);
  }
  private String elementXPath(Element element) {
    String path = element.getPath();
    if ("task".equals(element.getName()))
      path = path+"[@name='"+element.attributeValue("name")+"']";
    return path;
  }
  
  /* ugh, one of these days we'll start using jUnit... */
  public static void main(String[] args) {
    TaskManager tm = new TaskManager();

    System.err.println ("\n============");
    tm.loadTaskJar(new File("C:\\Docume~1\\red\\.callisto\\tasks\\EDCTask.jar"));

    System.err.println ("\n============");
    tm.loadTaskDir(new File("C:\\Docume~1\\red\\.callisto\\tasks\\RDCTask"));;
    /*
    System.err.println ("\n============");
    Properties props = new Properties();
    props.put("base.0","C:\\docume~1\\red\\proj\\callisto\\callisto\\target\\tasks");
    tm.scanTaskDirs("base",props);
    */
  }
}
