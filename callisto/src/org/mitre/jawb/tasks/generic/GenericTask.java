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

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Properties;
import javax.swing.*;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.io.URLUtils;
import org.mitre.jawb.atlas.AWBAnnotation;
import org.mitre.jawb.atlas.AWBATLASImplementation;
import org.mitre.jawb.atlas.PhraseTaggingAnnotation;
import org.mitre.jawb.tasks.AbstractTask;
import org.mitre.jawb.tasks.DefaultInlineImporter;
import org.mitre.jawb.tasks.DefaultInlineExporter;
import org.mitre.jawb.tasks.Exporter;
import org.mitre.jawb.tasks.Importer;
import org.mitre.jawb.tasks.Task;
import org.mitre.jawb.tasks.TaskToolKit;
import org.mitre.jawb.tasks.TaskToolKit;

import gov.nist.atlas.type.AnnotationType;
import gov.nist.atlas.type.ATLASType;
import gov.nist.atlas.type.CorpusType;
import gov.nist.maia.MAIAScheme;
import gov.nist.maia.MAIALoader;

import com.wutka.dtd.*;

/**
 * This is the place for any necessary hard-coded information about the task
 * to reside.
 */
public class GenericTask extends AbstractTask {

  private static final String GENERIC_PROPS = "generic.properties";

  /* JarFile from which task was loaded */
  private File baseJar = null;
  private Properties genericProperties = null;
  
  /* private mapping between annotation type name and Class */
  private Map annotationClass = null;

  private Set highlights = null;
  private Map defaultPrefs = null;
  
  private Importer[] importers = null;
  private Exporter[] exporters = null;

  /* Tree of possible values for attributes.
   *  constTree = (annotName* -> attrMap)
   *  attrMap   = (attriName* -> (valSet | null ))
   *  valSet    = (value*)
   */
  private Map constraintsTree = null;
  
  /** defaultsTree = (annotName* -> attrMap)
   *  attrMap      = (attriName* -> (value | null))
   */
  private Map defaultsTree = null;
  
  /**
   * Private constructor forcing creation through the getInstance(File)
   * method. This allows properties, maia, etc to be loaded from the jar
   * file. Previous Task files simply used
   * "this.getClass().getResourceAsStream(..)" but the .class file was loaded
   * from the defining jar: in the case of GenericTask, that won't work because
   * the .class is in the Callisto .jar.
   */
  private GenericTask (File file) {
    baseJar = file;

    // sanity check
    if (baseJar == null || ! baseJar.isFile())
      throw new IllegalStateException("Invalid task jar: " + baseJar);
    
    // load the generic task properties and check that they all exist
    genericProperties = new Properties();
    try {
      InputStream in = getResourceAsStream(GENERIC_PROPS);
      genericProperties.load(in);
      in.close();
    } catch (Exception io) {
      throw new RuntimeException("Error loading properties",io);
    }

    // AbstractTask requires these be set
    taskName = genericProperties.getProperty("task.name");
    taskTitle = genericProperties.getProperty("task.title");
    taskVersion = genericProperties.getProperty("task.version");
    taskDescription = genericProperties.getProperty("task.description");

    try { // test the value
      maiaURI = new URI (genericProperties.getProperty("task.maia"));
    } catch (URISyntaxException x) {
      throw new RuntimeException ("Invalid MAIA Scheme URL", x);
    }

    String localMaia = genericProperties.getProperty("task.maia.local");
    URL localURL = getResource(localMaia);
    if (localURL == null)
      throw new RuntimeException("Local MAIA reference for "+taskTitle+
                                 " not found: "+localMaia);
    localMaiaURI = URLUtils.badURLToURI (localURL);

    initializeATLASObjects();

    // other initializations here as needed
    initConstraints();
    initStyles();
    initIO();
  }

  /**
   * Factory method to create a new instance of the task, expecting to be given
   * the .jar file it was loaded from.
   */
  public static synchronized final Task getInstance (File file) {
    // the file is tested inside
    return new GenericTask(file);
  }

  /** Retrieves the URL of an entry in the Tasks defining jar. name is always
   * assumed to be relative to the root of the jar file. */
  protected URL getResource(String name) {
    URL url = null;
    try {
      url = baseJar.toURL();
      url = new URL("jar", "", url + "!/" + name);
    } catch (MalformedURLException nope) {
      url = null;
      // like class.getResource() we'll just return null
    }
    return url;
  }
  
  /** Get an InputStream for entry in the Tasks defining jar, or null. */
  protected InputStream getResourceAsStream(String name) throws IOException {
    return getResource(name).openStream();
  }
  
  /** Returns a new instance of the toolkit each time */
  public TaskToolKit getToolKit () {
    return new GenericToolKit(this);
  }

  public Importer[] getImporters () {
    return importers;
  }
    
  public Exporter[] getExporters () {
    return exporters;
  }

  /**
   * Default colors.
   */
  public Map getDefaultPreferences () {
    return defaultPrefs;
  }

  /* one with an object, one without for always true possibilities */
  public Set getPossibleValues (AWBAnnotation annot, String attr) {
    return getPossibleValues(annot.getAnnotationType(), attr);
  }
  public Set getPossibleValues (AnnotationType annotType, String attr) {
    Map eltConstraints = (Map) constraintsTree.get(annotType.getName());
    return (Set) eltConstraints.get(attr);
  }

  /** Returns <code>null</code> for all value without default set. */
  public String getDefaultValue (AnnotationType annotType, String attr) {
    String def = null;
    Map eltConstraints = (Map) defaultsTree.get(annotType.getName());
    if (eltConstraints != null)
      def = (String) eltConstraints.get(attr);
    return def;
  }

  /**
   *  Correspondence between AnnotationTypes and the Class of
   *  AWBAnnotation used to implement them
   */
  public Class getAnnotationClass(ATLASType type) {
    return PhraseTaggingAnnotation.class;
  }

  public Set getHighlightKeys () {
    return highlights;
  }

  /**
   * No 'constraint' is expected for simple TextExtentRegions
   */
  public String getHighlightKey (AWBAnnotation annot, Object constraint) {
    return (String) annot.getAnnotationType().getName();
  }

  /***********************************************************************/
  /* Init methods */
  /***********************************************************************/

  private static final String[] COLORS = {"{#4444ff,#000000}",
                                          "{#ff4444,#000000}",
                                          "{#44ff44,#000000}",
                                          "{#ff44ff,#000000}",
                                          "{#ffff44,#000000}",
                                          "{#44ffff,#000000}"};

  private void initStyles() {
    highlights = new HashSet ();
    defaultPrefs = new LinkedHashMap ();

    // only one key per annotation type... anything more get's complex
    // note that this will start reusing colors after COLORS.length
    Iterator iter = getMaiaScheme().iteratorOverAnnotationTypes();
    for (int i=0; iter.hasNext(); i++) {
      AnnotationType type = (AnnotationType) iter.next();
      String key = type.getName();
      String color = COLORS[i%COLORS.length];

      highlights.add(key);
      defaultPrefs.put("task."+getName()+"."+key, color);
    }    

    highlights = Collections.unmodifiableSet (highlights);
    defaultPrefs = Collections.unmodifiableMap (defaultPrefs);
  }

  private void initConstraints() {

    String type = genericProperties.getProperty("task.constraints.type");
    if (type == null) // no constraints?
      return;

    // only DTD's are currently supported
    if (! type.equals("dtd"))
      throw new RuntimeException("Unrecognized constraint type: "+type);

    initConstraintsFromDTD();
  }

  private void initConstraintsFromDTD() {
    String constraintsURL = genericProperties.getProperty("task.constraints.local");
    URL localURL = getResource(constraintsURL);
    if (localURL == null)
      throw new RuntimeException("Constraint reference for "+taskTitle+
                                 " not found: "+constraintsURL);

    try {
      InputStreamReader reader = new InputStreamReader(localURL.openStream());
      DTDParser parser = new DTDParser(reader);
      DTD dtd = parser.parse(true);

      // see the comment at the head of this file for structure of the trees
      constraintsTree = new HashMap();
      defaultsTree = new HashMap();
      
      Iterator iter = dtd.elements.values().iterator();
      while(iter.hasNext()) {
        DTDElement element = (DTDElement) iter.next();
        String eltName = element.getName();
        
        // add a map for it's constraints and defaults
        Map eltConstraints = (Map) constraintsTree.get(eltName);
        if (eltConstraints == null)
          constraintsTree.put(eltName, eltConstraints = new HashMap());
        Map attrDefaults = (Map) defaultsTree.get(eltName);
        if (attrDefaults == null)
          defaultsTree.put(eltName, attrDefaults = new HashMap());

        // if there are attributes, see if there are value constraints
        Iterator attrIter = element.attributes.values().iterator();
        while (attrIter.hasNext()) {
          DTDAttribute attr = (DTDAttribute) attrIter.next();
          String attrName = attr.getName();

          Set constraints = null;
          // validate possible values and store for lookup
          Object type = attr.getType();
          //System.err.println("    ATTRType: "+type+"("+type.getClass());
          if (type instanceof DTDEnumeration) {
            DTDEnumeration vals = (DTDEnumeration)type;
            constraints = new LinkedHashSet(vals.getItemsVec());
            eltConstraints.put(attrName, constraints);
          }
          else if (! type.equals("CDATA")) {
            System.err.println("NOTATION attributes not supported "+
                               eltName+"."+attrName+": "+type);
          }
          
          // validate attribute specification (only VALUE and IMPLIED allowed)
          DTDDecl decl = attr.getDecl();
          //System.err.println("    ATTRDec: "+decl.name);
          if (decl == DTDDecl.VALUE) {
            String def = attr.getDefaultValue();
            /* no longer an error... simply ignored
            if (constraints != null && ! constraints.contains(def))
              System.err.println("Value specified as default not allowed: "+
                               eltName+"."+attrName+": "+def);
            else
            */
              attrDefaults.put(attrName, def);
          }
          else if (decl == DTDDecl.IMPLIED) {
            // add null to constraints so user can clear value
            if (constraints != null)
              constraints.add(null);
          }
          // To do "REQUIRED" correctly, we need a 'validation' phase. The
          // point being that if there's no default value, then we should
          // initialize with null, and the data is simply invalid untill set.
          // Annotators like this beccause it forces them to specify the value,
          // otherwise it's easy to simply leave the default and have wrong
          // data. Defaults values are good when that value would be used in
          // the vast majority of cases.
          else {
            System.err.println("Only IMPLIED and 'value' attributes allowed: "+
                               eltName+"."+attrName+": "+decl.name);
          }
        }
      }

    } catch (Exception x) {
      RuntimeException e = new RuntimeException("Error parsing config file");
      e.initCause(x);
      throw e;
    }
  }
  
  private void initIO () {
    //ImportMUC (this)};
    importers = new Importer[] {new DefaultInlineImporter(this,"Generic SGML")};
    exporters = new Exporter[] {new DefaultInlineExporter(this,"Generic SGML")};
  }
  
}
