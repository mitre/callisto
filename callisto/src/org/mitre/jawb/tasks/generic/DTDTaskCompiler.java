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

package org.mitre.jawb.tasks.generic;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

import com.wutka.dtd.*;

import org.mitre.jawb.Jawb;

/**
 */
public class DTDTaskCompiler {

  public static String DEFAULT_DTD_NAME = "generic.dtd";

  private static int DEBUG = 0;
  private static List tlDomains = null;
  
  DTD dtd = null;
  String dtdContent = null;
  
  String taskName = null;
  String taskTitle = null;
  String taskVersion = null;
  String taskDescription = null;

  File dtdFile = null;
  File jarFile = null;
  JarOutputStream jarStream = null;


  static {
    // List of Top Level Domains from http://www.iana.org/
    String[] tlds = new String[] {
      // Generic TLD's
      "com", "aero", "biz", "com", "coop", "info", "museum",
      "name", "net", "org", "pro", "gov", "edu", "mil", "int",
      // cc TLD's
      "ac", "ad", "ae", "af", "ag", "ai", "al", "am", "an", "ao", "aq", "ar",
      "as", "at", "au", "aw", "ax", "az", "ba", "bb", "bd", "be", "bf", "bg",
      "bh", "bi", "bj", "bm", "bn", "bo", "br", "bs", "bt", "bv", "bw", "by",
      "bz", "ca", "cc", "cd", "cf", "cg", "ch", "ci", "ck", "cl", "cm", "cn",
      "co", "cr", "cs", "cu", "cv", "cx", "cy", "cz", "de", "dj", "dk", "dm",
      "do", "dz", "ec", "ee", "eg", "eh", "er", "es", "et", "fi", "fj", "fk",
      "fm", "fo", "fr", "ga", "gb", "gd", "ge", "gf", "gg", "gh", "gi", "gl",
      "gm", "gn", "gp", "gq", "gr", "gs", "gt", "gu", "gw", "gy", "hk", "hm",
      "hn", "hr", "ht", "hu", "id", "ie", "il", "im", "in", "io", "iq", "ir",
      "is", "it", "je", "jm", "jo", "jp", "ke", "kg", "kh", "ki", "km", "kn",
      "kp", "kr", "kw", "ky", "kz", "la", "lb", "lc", "li", "lk", "lr", "ls",
      "lt", "lu", "lv", "ly", "ma", "mc", "md", "mg", "mh", "mk", "ml", "mm",
      "mn", "mo", "mp", "mq", "mr", "ms", "mt", "mu", "mv", "mw", "mx", "my",
      "mz", "na", "nc", "ne", "nf", "ng", "ni", "nl", "no", "np", "nr", "nu",
      "nz", "om", "pa", "pe", "pf", "pg", "ph", "pk", "pl", "pm", "pn", "pr",
      "ps", "pt", "pw", "py", "qa", "re", "ro", "ru", "rw", "sa", "sb", "sc",
      "sd", "se", "sg", "sh", "si", "sj", "sk", "sl", "sm", "sn", "so", "sr",
      "st", "sv", "sy", "sz", "tc", "td", "tf", "tg", "th", "tj", "tk", "tl",
      "tm", "tn", "to", "tp", "tr", "tt", "tv", "tw", "tz", "ua", "ug", "uk",
      "um", "us", "uy", "uz", "va", "vc", "ve", "vg", "vi", "vn", "vu", "wf",
      "ws", "ye", "yt", "yu", "za", "zm", "zw" };
    
    tlDomains = Arrays.asList(tlds);
  }
  
  /** */
  public DTDTaskCompiler() throws IOException {
    reset();
  }
  
  /** */
  public void read(File file) throws IOException {
    Reader reader = new BufferedReader(new FileReader(file));
    read(reader);
    reader.close();
    dtdFile = file;
  }
  /** */
  public void read(Reader reader) throws IOException {
    reset();

    // because we want to copy it into the jar, make a copy in memory
    StringWriter buffer = new StringWriter();
    char[] buf = new char[2048];
    int c = 0;
    while ( (c = reader.read(buf,0,2048)) > 0)
      buffer.write(buf,0,c);

    dtdContent = buffer.toString();
    
    DTDParser parser = new DTDParser(new StringReader(dtdContent));
    dtd =  parser.parse(true);

    // Read task properties from ENTITY tags if available, but no failure yet
    DTDEntity taskNameEntity = (DTDEntity) dtd.entities.get("callisto_task_name");
    if (taskNameEntity != null)
      taskName = taskNameEntity.getValue();

    DTDEntity taskTitleEntity = (DTDEntity) dtd.entities.get("callisto_task_title");
    if (taskTitleEntity != null)
      taskTitle = taskTitleEntity.getValue();

    DTDEntity taskVersionEntity = (DTDEntity) dtd.entities.get("callisto_task_version");
    if (taskVersionEntity != null)
      taskVersion = taskVersionEntity.getValue();

    DTDEntity taskDescEntity = (DTDEntity)dtd.entities.get("callisto_task_description");
    if (taskDescEntity == null)
      taskDescription = "Automatically Generated Task";
    else
      taskDescription = taskDescEntity.getValue();
    
    //PrintWriter writer = new PrintWriter(System.err);
    //dtd.write(writer);
    //writer.flush();

    /*
    System.out.println("Elements:");
    Iterator iter = dtd.elements.keySet().iterator();
    while(iter.hasNext())
      System.out.println(iter.next());

    System.out.println("Entities:");
    iter = dtd.entities.keySet().iterator();
    while(iter.hasNext())
      System.out.println(iter.next());
    */
  }

  /**
   * Clears all current information.
   */
  public void reset() {
    dtd = null;
    dtdContent = null;
    
    taskName = null;
    taskTitle = null;
    taskVersion = null;
    taskDescription = null;
    
    dtdFile = null;
    jarStream = null;
  }

  /** May be null if a raw stream was parsed */
  public File getInput() {
    return dtdFile;
  }

  public void setOutput(File file) {
    jarFile = file;
    
  }
  /** Generated internally, if {@link #setOutput} not invoked. */
  public File getOutput() {
    return jarFile;
  }

  /**
   * Set the unique name of the Task to be created
   * @throws NullPointerException if title is null
   */
  public void setName(String name) {
    if (name == null)
      throw new NullPointerException("name");
    taskName = name;
  }

  /** Get the uniquie name of the task to be created. */
  public String getName() {
    return taskName;
  }
  
  /**
   * Set the title of the Task to be created
   * @throws NullPointerException if title is null
   */
  public void setTitle(String title) {
    if (title == null)
      throw new NullPointerException("title");
    taskTitle = title;
  }

  /** Get the title of the task to be created. */
  public String getTitle() {
    return taskTitle;
  }
  
  /**
   * Set the title of the Task to be created
   * @throws NullPointerException if title is null
   */
  public void setVersion(String version) {
    if (version == null)
      throw new NullPointerException("version");
    taskVersion = version;
  }

  /** Get the title of the task to be created. */
  public String getVersion() {
    return taskVersion;
  }
  
  /**
   * Set the description of the Task to be created
   * @throws NullPointerException if title is null
   */
  public void setDescription(String description) {
    if (description == null)
      throw new NullPointerException("description");
    taskDescription = description;
  }

  /** Get the uniquie name of the task to be created. */
  public String getDescription() {
    return taskDescription;
  }
  
  /**
   *
   */
  private void createMAIA() throws IOException {

    // Create AnnotationTypes definitions
    if (DEBUG>0)
      System.err.println("Creating AnnotationTypes");
    StringBuffer annotDefs = new StringBuffer();
    Iterator iter = dtd.elements.values().iterator();
    while(iter.hasNext()) {
      DTDElement element = (DTDElement) iter.next();
      String elementName = element.getName();
      String contentName = "empty-content";
      if (DEBUG>0)
        System.err.println("\n    ELT: "+elementName);

      // validate type is PCData only, MAIA cannot represent tag nesting by
      // offset at this time.  This appears as a DTDMixed, containing a single
      // DTDPCData
      DTDItem content = element.getContent();
      if (! (content instanceof DTDMixed) ||
          !(((DTDMixed)content).getItems().length == 1) ||
          !(((DTDMixed)content).getItems()[0] instanceof DTDPCData)) {
        String err = "Only #PCDATA is allowed as content type\n"+elementName;
        throw new IOException(err);
      }
      if (DEBUG>0)
        System.err.println("    ELTContent: "+(((DTDMixed)content).getItems()[0]));
      
      // if there are attributes, content is not 'empty' and must be created
      if (element.attributes.size() > 0) {
        contentName = elementName+"-content";
        annotDefs.append("    <ContentType name=\""+contentName+"\">\n");
        
        Iterator attrIter = element.attributes.values().iterator();
        while (attrIter.hasNext()) {
          DTDAttribute attr = (DTDAttribute) attrIter.next();
          String attrName = attr.getName();
          String attrType = "String";  // default

          if (DEBUG>0)
            System.err.println("    ATTR: "+attrName);
          
          // validate possible values
          Vector possibleVals = null;
          Object type = attr.getType();
          if (DEBUG>0)
            System.err.println("    ATTRType: "+type);
          if (type instanceof DTDEnumeration) {
            possibleVals = ((DTDEnumeration)type).getItemsVec();
            if (DEBUG>0)
              System.err.println("     "+possibleVals);
            boolean hasTrue = false;
            boolean hasFalse = false;
            boolean hasOther = false;
            
            Iterator valIter = possibleVals.iterator();
            while (valIter.hasNext()) {
              String val = (String) valIter.next();
              if (val.equalsIgnoreCase("true"))
                hasTrue = true;
              else if (val.equalsIgnoreCase("false"))
                hasFalse = true;
              else
                hasOther = true;
            }
            if (hasTrue && hasFalse && !hasOther)
              attrType = "Boolean";
          }
          else if (! type.equals("CDATA")) {
            throw new IOException("NOTATION attributes not supported "+
                                  elementName+"."+attrName+": "+type);
          }
          
          // validate attribute specification (only VALUE and IMPLIED allowed)
          DTDDecl decl = attr.getDecl();
          if (DEBUG>0)
            System.err.println("    ATTRDec: "+decl.name+":"+attr.getDefaultValue());
          
          // Originally, if the default val wasn't in 'possibleVals', I threw
          // an exception here, killing the task creation. Now it just silently
          // isn't used
          if (decl == DTDDecl.VALUE) {
            // empty
          }
          else if (decl != DTDDecl.IMPLIED) {
            throw new IOException("Only IMPLIED and 'value' attributes supported:"+
                                  elementName+"."+attrName);
          }

          annotDefs.append("      <ParameterType ref=\""+attrType+"\" role=\""+attrName+"\"/>\n");
        }
        annotDefs.append("    </ContentType>\n");
      }
      
      annotDefs.append("    <AnnotationType name=\""+elementName+"\">\n");
      annotDefs.append("      <RegionType ref=\"text-extent\" role=\"text-extent\"/>\n");
      annotDefs.append("      <ContentType ref=\""+contentName+"\" role=\"content\"/>\n");
      annotDefs.append("    </AnnotationType>\n\n");
    }

    // Add Annotation types to analysis
    if (DEBUG>0)
      System.err.println("Adding Annotations to Analysis");
    StringBuffer analysisTypes = new StringBuffer();
    iter = dtd.elements.keySet().iterator();
    while(iter.hasNext()) {
      String elementName = (String) iter.next();
      analysisTypes.append("      <AnnotationType ref=\""+elementName+"\"/>\n");
    }

    HashMap replacements = new HashMap();
    replacements.put("\\$\\{callisto_task_name\\}", taskName);
    replacements.put("\\$\\{callisto_AnnotationTypes\\}", annotDefs.toString());
    replacements.put("\\$\\{callisto_AnalysisTypes\\}", analysisTypes.toString());

    InputStream inputStream =
      Jawb.getResourceAsStream("generic/generic.maia.xml.in");
    String entryName = taskName+".maia.xml";
    if (DEBUG>0)
      System.err.println("Writing "+entryName);
    
    populateTemplate(replacements, inputStream, entryName);
  }

  /**
   *
   */
  private void createPropertyFiles() throws IOException {
    
    // task.properties, used by Callisto to load the task
    Properties properties = null;
    OutputStream out = null;
    properties = new Properties();
    properties.setProperty("task.class.0",
                           "org.mitre.jawb.tasks.generic.GenericTask");

    jarStream.putNextEntry(new ZipEntry("task.properties"));
    properties.store(jarStream, "Auto Generated Callisto Task Properties");
    jarStream.closeEntry();

    // GenericTask properties, specific to this new task
    String maiaName = taskName+".maia.xml";
    String dtdName = DEFAULT_DTD_NAME;
    if (dtdFile != null)
      dtdName = dtdFile.getName();

    properties = new Properties();
    properties.setProperty("task.name",taskName);
    properties.setProperty("task.title",taskTitle);
    properties.setProperty("task.version",taskVersion);
    properties.setProperty("task.description",taskDescription);
    
    properties.setProperty("task.maia",
                           "http://callisto.mitre.org/maia/generic?"+maiaName);
    properties.setProperty("task.maia.local", maiaName);
    
    properties.setProperty("task.constraints.type", "dtd");
    properties.setProperty("task.constraints.local", dtdName);

    jarStream.putNextEntry(new ZipEntry("generic.properties"));
    properties.store(jarStream, "Generic Task Properties");
    jarStream.closeEntry();
  }

  /**
   *
   */
  private void copyDTDs() throws IOException {
    // copy the DTD directly
    String dtdName = DEFAULT_DTD_NAME;
    if (dtdFile != null)
      dtdName = dtdFile.getName();
    copyResource(new ByteArrayInputStream(dtdContent.getBytes()), dtdName);

    // copy the maia dtd because the maia files refer to it
    copyResource(Jawb.getResourceAsStream("maia/maia.dtd"), "maia.dtd");
  }

  /**
   * @arg replacement is a map of reg-exe strings to replacement strings
   * @arg inputStream needs to be valid, it is buffered before reading
   * @arg outputFile can be anywhere, but should be in one of the task dirs
   */
  private void populateTemplate(Map replacements, InputStream inputStream,
                                String outputFile) throws IOException {
    // read the template into one string
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    
    InputStream stream = new BufferedInputStream(inputStream);
    byte bytes[] = new byte[inputStream.available()];
    int c = 0;
    while ( (c = stream.read(bytes)) != -1)
      buffer.write(bytes, 0, c);
    inputStream.close();
    String template = buffer.toString();

    // do all the replacing this is rather slow.
    Iterator iter = replacements.keySet().iterator();
    while (iter.hasNext()) {
      String regex = (String) iter.next();
      template = template.replaceAll(regex,(String) replacements.get(regex));
    }

    // Write the thing out
    jarStream.putNextEntry(new ZipEntry(outputFile));
    Writer writer = new BufferedWriter(new OutputStreamWriter(jarStream));
    writer.write(template);
    writer.flush();
    jarStream.closeEntry();
  }

  /**
   *
   */
  private void copyResource(InputStream inputStream, String entry)
    throws IOException {

    jarStream.putNextEntry(new ZipEntry(entry));
    
    InputStream stream = new BufferedInputStream(inputStream);
    byte bytes[] = new byte[1024];
    int c = 0;
    while ( (c = inputStream.read(bytes)) != -1)
      jarStream.write(bytes, 0, c);

    inputStream.close();
    jarStream.closeEntry();
  }

  /**
   * Create a dir next to DTD, or in system temp if built from stream.
   */
  private void initJarFile() throws IOException {
    if (jarFile == null) {
      File basedir = null;
      if (dtdFile != null)
        basedir = dtdFile.getParentFile();

      jarFile = new File(basedir, taskName+".jar");
    }
  }
  
  public void compile() throws IOException {
    // validate task name, title, and description
    if (taskName.length() < 1)
      throw new IOException ("Task name must be non empty");
    if (! Character.isJavaIdentifierStart(taskName.charAt(0)))
      throw new IOException ("Task  name is invalid (char 0)");
    for (int i=1; i<taskName.length(); i++) {
      char c = taskName.charAt(i);
      if (! Character.isJavaIdentifierPart(c) && c != '.')
        throw new IOException ("Task name is invalid (char "+i+")");
    }
    int dot = taskName.indexOf('.'); // verify name form: e.g. org.mitre.foo
    if (dot < 1 || ! tlDomains.contains(taskName.substring(0,dot))) {
      // TODO: it's a bad idea to put user instructions in an exception
      String err = "Task names must begin with the reverse of your domain name:\n"+
        "e.g. \"com.example.task\", \"com.mitre.rdc\", and \"com.mitre.eeld\"\n\n"+
        "This is because it is used as a unique identifier.  Name parts after\n"+
        "your organizations name are up to your organization.\n\n"+
        "A list of top level domains can be found at http://www.iana.org/";
      throw new IllegalArgumentException(err);
    }

    if (taskTitle.length() < 1)
      throw new IOException ("Task title must be non empty");
    
    if (taskVersion.length() < 1)
      throw new IOException ("Task version must be non empty");
    
    if (taskDescription.length() < 1)
      throw new IOException ("Task description must be non empty");
    
    initJarFile();
    jarStream = new JarOutputStream(new FileOutputStream(jarFile));
    if (DEBUG>0)
      System.err.println("Creating task: "+jarFile);

    createMAIA();
    createPropertyFiles();
    copyDTDs();

    jarStream.close();
    jarStream = null;
  }
  
  public static void main(String[] args) throws Exception {
    File file = new File (System.getProperty ("user.home"),
                          "proj/callisto/callisto/data/generic.dtd");
    File file2 = new File (System.getProperty ("user.home"),
                           "proj/callisto/callisto/data/another.dtd");

    DTDTaskCompiler compiler = new DTDTaskCompiler();
    compiler.read(file);
    compiler.compile();
    compiler.reset();
    
    //compiler.read(file2);
    //compiler.compile();
  }
}
