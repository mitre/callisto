/*
 * Copyright (c) 2002-2008 The MITRE Corporation
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

package org.mitre.spatialml.callisto;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.prefs.Preferences;


public class IGDBLookup implements GazetterLookup {

  //private static String sourceUrl = "http://spacetime.mitre.org/IGDB/cgi-bin/GazLookup/spatialml.py?featuretype=0&place=%s";
  
  private String sourceUrl = "";
  
  public IGDBLookup() {
    
    Preferences prefs = Jawb.getPreferences();
    
    sourceUrl = prefs.getPreference(SpatialMLTask.TASK_NAME + ".IGDB.URL");
    if (sourceUrl == null) {
      // save out a default value if one's not yet set
      sourceUrl = "http://spacetime.mitre.org/IGDB/cgi-bin/GazLookup/spatialml.py?featuretype=0&place=%s";
      prefs.setPreference(SpatialMLTask.TASK_NAME + ".IGDB.URL", sourceUrl);
      Jawb.storePreferences();
    }
    
  }
  
  
  public List lookup(String name) {
    
    if (sourceUrl == null || sourceUrl.equals("")) {
      return Collections.EMPTY_LIST;
    }
    
    try {
      //Map attributes = new HashMap(); // ultimate return structure
      
      // build the url and open a connection to it
      String fullURLString = sourceUrl.replaceAll("%s", URLEncoder.encode(name, "UTF-8"));
      System.err.println(" Loading URL: " + fullURLString);
      URL fullUrl = new URL(fullURLString);
      System.err.println("  Opening connection...");
      URLConnection conn = fullUrl.openConnection();
      
      DocumentBuilderFactory f = DocumentBuilderFactory.newInstance(); // set up the parser
      DocumentBuilder p = f.newDocumentBuilder();

      
      System.err.println("  Parsing stream...");
      Document doc = p.parse(conn.getInputStream());
      
      List places = new ArrayList();  
      
      NodeList placeNodes = doc.getElementsByTagName("PLACE");
      System.err.println(" Pulling out " + placeNodes.getLength() + " PLACES");
      for (int i = 0; i < placeNodes.getLength(); i++) {
        Node n = placeNodes.item(i);
        
        if (n.getNodeType() == Node.ELEMENT_NODE) {
          
          Element place = (Element)n;
          
          // attributes.put(Integer.valueOf(i), place.getNodeValue());
        
          Map nodeAttr = new HashMap();
          nodeAttr.put("name", getTextContent(place));
          if (place.hasAttribute("gazref")) {
            nodeAttr.put("gazref", place.getAttribute("gazref"));
          }
          if (place.hasAttribute("latLong")) {
            nodeAttr.put("latLong", place.getAttribute("latLong"));
          }
          if (place.hasAttribute("type")) {
            nodeAttr.put("type", place.getAttribute("type"));
          }
        
          places.add(nodeAttr);
          
        }
      }
      /*
      if (false) throw new SAXException(); // JAVA ARE STUPIT
      
      
      BufferedInputStream buff = new BufferedInputStream(conn.getInputStream());
      byte bb[] = new byte[1024];
      //StringBuffer sb = new StringBuffer();
      
      while (buff.read(bb) != -1) {
        //sb.append(new String(bb));
        System.err.println(new String(bb));
      }
      */
      
      //System.err.println(places);
      
      return places;
      
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return Collections.EMPTY_LIST;
    } catch (IOException e) {
      e.printStackTrace();
      return Collections.EMPTY_LIST;
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
      return Collections.EMPTY_LIST;
    } catch (SAXException e) {
      e.printStackTrace();
      return Collections.EMPTY_LIST;
    }
    
    
  }

  private String getTextContent(Element el) {
    StringBuffer buff = new StringBuffer();
    
    NodeList children = el.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        buff.append(getTextContent((Element) child));
      } else if (child.getNodeType() == Node.TEXT_NODE || child.getNodeType() == Node.CDATA_SECTION_NODE) {
        buff.append(child.getNodeValue());
      }
    }
    
    return buff.toString();
  }
  
  public String getName() {
    return "IGDB";
  }
  
  

}
