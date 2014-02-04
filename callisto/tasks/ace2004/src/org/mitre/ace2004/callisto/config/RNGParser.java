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

package org.mitre.ace2004.callisto.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Document;
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

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.gui.GUIUtils;
import org.mitre.jawb.tasks.Task;

// ensures jaxen (required for XPATH) is availabile at comple time
import org.jaxen.SimpleNamespaceContext;


/**
 * Utilities to help with ATLAS files. Note that the methods of this class are
 * <strong>not</strong> thread safe.
 */
public class RNGParser {

  public static final int DEBUG = 0;

  private Map uris = null;
  private SAXReader reader = null;

  private Map elt2attrMap = null;
  private Set relationConstraintSet = null;
  private Set unmentionedRelationConstraintSet = null;
  private Map argumentConstraintMap = null;
  
  public RNGParser () {
    uris = new HashMap();
    uris.put("rng", "http://relaxng.org/ns/structure/1.0");
    uris.put("aif", "http://callisto.mitre.org/ns/aif/1.0");

    DocumentFactory factory = new DocumentFactory();
    factory.setXPathNamespaceURIs(uris);

    reader = new SAXReader();
    reader.setDocumentFactory(factory);
    reader.setIncludeExternalDTDDeclarations (false);
  }

  public Map getConstraintTree() {
    return elt2attrMap;
  }
  public Set getRelationConstraints() {
    return relationConstraintSet;
  }
  public Set getRelationConstraints(boolean isUnmentioned) {
    if (isUnmentioned)
      return unmentionedRelationConstraintSet;
    else
      return relationConstraintSet;
  }

  public Map getArgumentConstraints() {
    return argumentConstraintMap;
  }
  
  /**
   * Read a file in <i>pseudo</i> RELAX NG format, and return a map which is
   * very speicific to RDC.  It has the following structure
   * <pre>
   *    map.get(annotName) -> Map
   *    map.get(annotName).get(attrName) -> Set | Map
   *      returns a map if the attribute's set of possible values is dependant
   *      on another attribute.  The map contains a mapping from the depended
   *      attribute value to the dependant attributes possible values
   *        map.get(annotName).get(attrName).get(attrName) -> Set
   * </pre>
   *
   * Because it is so specific to RDC, there is a lot of error checking to
   * verify that it /is/ specific to RDC.
   *
   * @param rngURI location of rng file
   */
  public void read(URI rngURI) throws IOException {
    if (DEBUG > 0)
      System.err.println ("RNGParser.read: rngURI="+rngURI);

    elt2attrMap = new HashMap();
    relationConstraintSet = null;
    unmentionedRelationConstraintSet = null;
    argumentConstraintMap = null;

    Document doc = parse(rngURI); // namespaces are set in docfactory

    List elements = doc.selectNodes("/rng:grammar/rng:define/rng:element");
    Iterator elts = elements.iterator();
    while (elts.hasNext()) {
      Element element = (Element) elts.next();
      String elementName = element.attributeValue("name");
      if (DEBUG > 0)
        System.err.println("Element: "+elementName);

      Map attr2valueMap = new HashMap();
      elt2attrMap.put(elementName, attr2valueMap); // insert so other functions have access

      // only handle <empty>|<choice>|<attribute>
      List subElements = element.elements();
      if (subElements.size() == 0)
        parseError(element, "Elements must have at least 1 sub-element");
      
      Iterator subs = subElements.iterator();
      while (subs.hasNext()) {
        Element sub = (Element) subs.next();
        String subName = sub.getName();
        
        if (subName.equals("empty")) {
          continue;
          
        } else if (subName.equals("choice")) {
          
          if (DEBUG > 0)
            System.err.println("  Handling choice");
          Element choice = sub;

          readElementChoice(element, choice);
          
        } else if (subName.equals ("attribute")) {

          if (DEBUG > 0)
            System.err.println("  Attribute: "+sub.attributeValue("name"));

          Set valueSet = readAttributeChoices(sub);
          if (valueSet != null) {
            valueSet.add(null); // allows attribute to be 'cleared'
            
            attr2valueMap.put(sub.attributeValue("name"),
                              Collections.unmodifiableSet(valueSet));
          }
        }
      }
      
      // HACK!
      if (elementName.equals("ace_relation"))
        readRelationConstraints(element, false);
      if (elementName.equals("unmentioned_ace_relation"))
        readRelationConstraints(element, true);
      if (elementName.equals("ace_argument-mention"))
        readArgumentConstraints(element);
      
      elt2attrMap.put(elementName, Collections.unmodifiableMap(attr2valueMap));
    }
    elt2attrMap = Collections.unmodifiableMap(elt2attrMap);
  }

  private Set readAttributeChoices(Element attribute) {

    // for now only choice and /empty/ allowed
    Element choice = attribute.element("choice");
    if (choice == null && attribute.elements().size() > 0) {
      parseError(attribute,"Only 'choice' allowed in 'attribute'");
      return null;
    }
    
    Set valueSet = new LinkedHashSet ();

    if (attribute.elements().size() > 0) {
      Iterator vals = choice.elements("value").iterator();
      while (vals.hasNext()) {
        Element value = (Element) vals.next();
        // datatype checking goes here
        String constraint = value.getTextTrim();
        if (DEBUG > 0)
          System.err.println("    Value: "+constraint);
        valueSet.add(constraint);
      }
    }

    return valueSet;
  }
  
  /**
   * Crippled reading of choices for attributes of an element.
   */
  private void readElementChoice(Element element, Element choice) {
    Map attr2valueMap = (Map) elt2attrMap.get(element.attributeValue("name"));
    Set keyAttrSet = new LinkedHashSet();
    Map keyAttr2SubAttrMap = new HashMap();
    Set fullSubAttrSet = new LinkedHashSet();

    keyAttr2SubAttrMap.put(null, fullSubAttrSet);

    // TODO: SOMETHING to tell gui this is the one!
    String keyAttrName = choice.attributeValue("key-attribute"); 
    if (keyAttrName == null) {
      parseError(choice, "<choice> tags within <element> tags require 'key-attribute' attribute");
      return;
    }
    
    attr2valueMap.put(keyAttrName, keyAttrSet);

    String subAttrName = null;

    // read key attributes 
    List keys = choice.selectNodes("rng:group/rng:attribute[@name='"+keyAttrName+"']");
    Iterator keyIter = keys.iterator();
    while (keyIter.hasNext()) {
      Element attribute = (Element) keyIter.next();

      // multiple values just means they controll the same subordinate attributes
      List keyValues = new LinkedList();
      Iterator valIter = attribute.selectNodes("rng:value").iterator();
      while (valIter.hasNext()) {
        String value = ((Element) valIter.next()).getTextTrim();
        keyAttrSet.add(value);
        keyValues.add(value);
      }
      
      // 1 attribute is subordinate to the key, or none, implying key value
      // doesn't use subordinate attr
      List subAttrs = attribute.selectNodes("../rng:attribute[@name!='"+keyAttrName+"']");
      Set subAttrSet = null;
      
      if (subAttrs.size() > 1) {
        parseError(attribute,"Only 1 subordinate attribute choice set per type");
        continue;
      }

      if (subAttrs.size() == 1) { // if 0, only null allowed in a subordinate attribute
        Element subAttr = (Element) subAttrs.get(0);
        String subAttrNm = subAttr.attributeValue("name");
        
        // ensure that all the subordinate attributes are the same
        if (subAttrName == null) {
          subAttrName = subAttrNm;
        } else if (! subAttrName.equals(subAttrNm)) {
          parseError(subAttr, "All subordinate attributes within an element's choice must be the same."+
                     " Expected: "+subAttrName);
          continue;
        }

        subAttrSet = readAttributeChoices(subAttr);
      }

      if (subAttrSet != null) {
        fullSubAttrSet.addAll(subAttrSet); // before adding null, so it's last
        subAttrSet.add(null); // allows attribute to be 'cleared'

        // map each of the key values to the set of subordinate values
        Iterator keyValueIter = keyValues.iterator();
        while (keyValueIter.hasNext())
          keyAttr2SubAttrMap.put((String) keyValueIter.next(),
                                 Collections.unmodifiableSet(subAttrSet));
      }
    }
    keyAttrSet.add(null); // allows attribute to be 'cleared'
    fullSubAttrSet.add(null); // allows attribute to be 'cleared'

    keyAttr2SubAttrMap.put(null, Collections.unmodifiableSet(fullSubAttrSet));
    
    attr2valueMap.put(keyAttrName, Collections.unmodifiableSet(keyAttrSet));
    attr2valueMap.put(subAttrName, Collections.unmodifiableMap(keyAttr2SubAttrMap));
  }

  /** Amazingly ugly hack... */
  private void readRelationConstraints(Element element, boolean isUnmentioned) {
    Set readConstraintSet = new LinkedHashSet();
    
    Map attr2valueMap = (Map) elt2attrMap.get(element.attributeValue("name"));
    Set keyAttrSet = (Set) attr2valueMap.get("type");
    Map keyAttr2SubAttrMap = (Map) attr2valueMap.get("subtype");
    
    // start with getting info already read, to validate constraints
    Object entityAttribs = elt2attrMap.get("ace_entity");
    if (entityAttribs == null) {
      parseError(element, "No entity attributes read before constraints");
      return;
    }
    Object ets = ((Map) entityAttribs).get("type");
    if (ets == null) {
      parseError(element, "No entity types read before constraints");
      return;
    }
    Set entityTypes = (Set) ets;
    
    List constraints = element.selectNodes("aif:constraints/aif:type");
    Iterator constIter = constraints.iterator();
    while (constIter.hasNext()) {
      Element typeConst = (Element) constIter.next();
      String e1Type = typeConst.attributeValue("e1Type");
      String e2Type = typeConst.attributeValue("e2Type");
      String relType = typeConst.attributeValue("relType");
      String relSubtype = typeConst.attributeValue("relSubtype");
      
      if (e1Type == null || ! (e1Type.equals("*") || entityTypes.contains(e1Type))) {
        parseError(typeConst, "Invalid e1Type");
        continue;
      }
      if (e2Type == null || ! (e2Type.equals("*") || entityTypes.contains(e2Type))) {
        parseError(typeConst, "Invalid e2Type");
        continue;
      }
      if (relType == null || ! keyAttrSet.contains(relType)) {
        parseError(typeConst, "Invalid relType for: "+keyAttrSet);
        continue;
      }
      // allow subtype to be "" in constraints when there are no
      // subtypes for a certain type
      Set subAttrSet;
      if (keyAttr2SubAttrMap == null)
        subAttrSet = null;
      else 
        subAttrSet = (Set) keyAttr2SubAttrMap.get(relType);
      if ((subAttrSet == null && !relSubtype.equals("")) || 
          (subAttrSet != null && !subAttrSet.contains(relSubtype))) {
        parseError(typeConst, "Invalid relSubtype for: "+relType+subAttrSet);
        continue;
      }

      // now that we know it's valid, add to the set in the 'old format'
      /*
      if ("*".equals(e1Type) && "*".equals(e2Type)) {
        Iterator e1Iter = entityTypes.iterator();
        while (e1Iter.hasNext()) {
          e1Type = (String) e1Iter.next();
          Iterator e2Iter = entityTypes.iterator();
          while (e2Iter.hasNext()) {
            e2Type = (String) e2Iter.next();
            String constraint = e1Type+","+e2Type+","+relType+","+relSubtype;
            relationConstraintSet.add(constraint);
          }
        }
      }
      else {*/
        String constraint = e1Type+","+e2Type+","+relType+","+relSubtype;
        readConstraintSet.add(constraint);
        //}
    }
    if (isUnmentioned)
      unmentionedRelationConstraintSet = 
        Collections.unmodifiableSet(readConstraintSet);
    else
      relationConstraintSet = Collections.unmodifiableSet(readConstraintSet);
  }
  
  /** Yet another Amazingly ugly hack... */
  private void readArgumentConstraints(Element element) {
    Map argGrandparent2TypeMap = new LinkedHashMap();

    Map argAttr2valueMap = (Map) elt2attrMap.get(element.attributeValue("name"));
    Set argRoleValueSet = (Set) argAttr2valueMap.get("role");

    //
    List constraints = element.selectNodes("aif:constraints/aif:type");
    Iterator constIter = constraints.iterator();
    while (constIter.hasNext()) {
      Element typeConst = (Element) constIter.next();
      String grandparentVal = typeConst.attributeValue("grandparent");
      String typeVal = typeConst.attributeValue("type");
      String subtypeVal = typeConst.attributeValue("subtype");
      String roleVal = typeConst.attributeValue("role");

      // ======== 1. GrandParents ========
      String[] grandparent = grandparentVal.split(",");
      for (int p=0; p<grandparent.length; p++) {

        // Ensure valid grandparent, Not allowing '*'
        Map grandparentAttr2ValueMap = (Map) elt2attrMap.get(grandparent[p]);
        if (grandparentAttr2ValueMap == null) {
          parseError(element, "No such grandparent known: "+grandparent[p]);
          continue;
        }
        // Ensure known set of types
        Set grandparentTypeValueSet = (Set) grandparentAttr2ValueMap.get("type");
        if (grandparentTypeValueSet == null) {
          parseError(element, "No types known for grandparent " + grandparent[p]);
          continue;
        }

        // prepare next level
        Map argGrandparentType2SubtypeMap = (Map) argGrandparent2TypeMap.get(grandparent[p]);
        if (argGrandparentType2SubtypeMap == null) {
          argGrandparentType2SubtypeMap = new LinkedHashMap();
          // ensure that there's a map for 'any' value
          argGrandparentType2SubtypeMap.put(null, new LinkedHashMap());
          argGrandparent2TypeMap.put(grandparent[p], argGrandparentType2SubtypeMap);
        }
        
        // ======== 2. Types ========
        String[] type = typeVal.split(",");
        for (int t=0; t<type.length; t++) {

          // Ensure valid type, allowing '*'
          if (type[t].equals("*")) {
            type[t] = null;
          }
          else {
            if (! grandparentTypeValueSet.contains(type[t])) {
              parseError(element, "Type unknown: "+grandparent[p]+"."+type[t]);
              continue;
            }
          }

          // Ensure known set of subtypes
          Set grandparentSubtypeValueSet = (Set) ((Map) grandparentAttr2ValueMap.get("subtype")).get(type[t]);
          if (type != null && grandparentSubtypeValueSet == null) {
            parseError(element, "No subtypes known for types "+grandparent[p]+"."+type[t]);
            continue;
          }
          
          // prepare next level
          Map argGrandparentSubtype2RoleMap = (Map) argGrandparentType2SubtypeMap.get(type[t]);
          if (argGrandparentSubtype2RoleMap == null) {
            argGrandparentSubtype2RoleMap = new LinkedHashMap();
            // ensure that there's a map for 'any' value
            argGrandparentSubtype2RoleMap.put(null, new LinkedHashSet());
            argGrandparentType2SubtypeMap.put(type[t], argGrandparentSubtype2RoleMap);
          }
          
          // ======== 3. Subtypes ========
          String[] subtype = subtypeVal.split(",");
          for (int s=0; s<subtype.length; s++) {
            
            // Ensure valid subtype, allowing '*'
            if (subtype[s].equals("*")) {
              subtype[s] = null;
            }
            else {
              if (! grandparentSubtypeValueSet.contains(subtype[s])) {
                parseError(element, "Subtype unknown: "+grandparent[p]+"."+type[t]+"."+subtype[s]);
                continue;
              }
            }

            // prepare next level
            Set argRoleSet = (Set) argGrandparentSubtype2RoleMap.get(subtype[s]);
            if (argRoleSet == null) {
              argRoleSet = new LinkedHashSet();
              argGrandparentSubtype2RoleMap.put(subtype[s], argRoleSet);
            }
            
            // ======== 4. Roles ========
            String[] role = roleVal.split(",");
            for (int r=0; r<role.length; r++) {

              // Ensure valid roles
              if (! argRoleValueSet.contains(role[r])) {
                parseError(element, "Argument Role unknown: "+role[r]);
                continue;
              }
              argRoleSet.add(role[r]);
            }
          }
        }
      }
    }
    argumentConstraintMap = Collections.unmodifiableMap(argGrandparent2TypeMap);
  }
  
  private void parseError(Element node, String msg) {
    System.err.println ("RNG Error: "+elementXPath(node)+"\n"+msg);
  }

  private StringBuffer elementXPath(Element elt) {
    StringBuffer sb = null;
    if (elt.getParent() == null)
      sb = new StringBuffer();
    else
      sb = elementXPath(elt.getParent());

    String name = elt.getName();
    sb.append("/").append(name);
    if (name.equals("element") || name.equals("attribute"))
      sb.append('[').append(attributeXPath(elt,"name")).append(']');
    if (name.equals("type")) { // aif:type really
      sb.append('[');
      sb.append(attributeXPath(elt, "e1Type")).append('|');
      sb.append(attributeXPath(elt, "e2Type")).append('|');
      sb.append(attributeXPath(elt, "relType")).append('|');
      sb.append(attributeXPath(elt, "relSubtype")).append(']');
    }
    return sb;
  }
  
  private String attributeXPath(Element elt, String attrib) {
    return "@"+attrib+"='"+elt.attributeValue(attrib)+"'";
  }

  public Document parse (URI rngURI) throws IOException {
    if (DEBUG > 0)
      System.err.println ("RNGParser.parse: rngURI="+rngURI);
    // URI.toURL() fails when opaque. don't expect an opaque here, but...
    try {
      return reader.read(rngURI.toURL ());
    } catch (Exception e) {
      IOException x = new IOException ("Unable to parse input file: "+rngURI);
      x.initCause (e);
      throw x;
    }
  }

  private void indent(int level) {
    for (int i=0; i<level; i++)
      System.err.print("  ");
  }
  private void dValue(Object value, int level) {
    if (value instanceof Map) {
      System.err.println();
      dump ((Map)value,level+1);
      
    } else if (value instanceof Set) {
      System.err.println();
      dump ((Set)value,level+1);
      
    } else {
      System.err.println (value);
    }
  }
  public void dump(Set branch, int level) {
    Iterator iter = branch.iterator();
    while (iter.hasNext()) {
      indent(level);
      dValue(iter.next(),level);
    }
  }
  public void dump(Map branch, int level) {
    Iterator iter = branch.keySet().iterator();
    while (iter.hasNext()) {
      Object key = iter.next();
      indent(level);
      System.err.print(key);
      dValue(branch.get(key),level);
    }
  }

  public static void main (String args[]) throws Exception {
    
    //String uri = args[0];
    String uri = "file:/C:/Docume~1/red/proj/callisto/callisto/tasks/ace2004/resource/ace2004.rng";
    URI rngURI = new URI (uri);

    RNGParser parser = new RNGParser();
    parser.read(rngURI);

    parser.dump(parser.getConstraintTree(), 0);
    parser.dump(parser.getRelationConstraints(), 0);
    parser.dump(parser.getArgumentConstraints(), 0);
  }
}
