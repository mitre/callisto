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

/**
 * 
 */
package org.mitre.spatialml.callisto;

import gov.nist.atlas.ref.AnnotationRef;
import gov.nist.atlas.type.AnnotationType;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.mitre.jawb.atlas.AWBAnnotation;

/**
 * 
 * A set of utilities and data values for the SpatialML Schema.
 * 
 * @author jricher
 * 
 */
public class SpatialMLUtils {

  private static Logger log = Logger.getLogger(SpatialMLUtils.class.getName());
  
  //
  // These all start with a "null" value to facilitate their usage in dropdown
  // selection boxes, filters, and other things that require a "none" or "any"
  // entry.
  //

  public static String[] continents = { null, "AF", "AN", "AI", "AU", "EU", "NA", "SA" };
  public static Set continentSet = setFromArray(continents);

  public static String[] continentNames = { null, "Africa", "Antarctica", "Asia", "Australia", "Europe", "North America", "South America" };
  public static Map continentNameMap = zip(continents, continentNames);
  
  // These are the ISO-3166 two-letter country codes in alphabetical order
  public static String[] countryCodes = { null, "AD", "AE", "AF", "AG", "AI",
      "AL", "AM", "AN", "AO", "AQ", "AR", "AS", "AT", "AU", "AW", "AX", "AZ",
      "BA", "BB", "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BM", "BN", "BO",
      "BR", "BS", "BT", "BV", "BW", "BY", "BZ", "CA", "CC", "CD", "CF", "CG",
      "CH", "CI", "CK", "CL", "CM", "CN", "CO", "CR", "CU", "CV", "CX", "CY",
      "CZ", "DE", "DJ", "DK", "DM", "DO", "DZ", "EC", "EE", "EG", "EH", "ER",
      "ES", "ET", "FI", "FJ", "FK", "FM", "FO", "FR", "GA", "GB", "GD", "GE",
      "GF", "GG", "GH", "GI", "GL", "GM", "GN", "GP", "GQ", "GR", "GS", "GT",
      "GU", "GW", "GY", "HK", "HM", "HN", "HR", "HT", "HU", "ID", "IE", "IL",
      "IM", "IN", "IO", "IQ", "IR", "IS", "IT", "JE", "JM", "JO", "JP", "KE",
      "KG", "KH", "KI", "KM", "KN", "KP", "KR", "KW", "KY", "KZ", "LA", "LB",
      "LC", "LI", "LK", "LR", "LS", "LT", "LU", "LV", "LY", "MA", "MC", "MD",
      "ME", "MG", "MH", "MK", "ML", "MM", "MN", "MO", "MP", "MQ", "MR", "MS",
      "MT", "MU", "MV", "MW", "MX", "MY", "MZ", "NA", "NC", "NE", "NF", "NG",
      "NI", "NL", "NO", "NP", "NR", "NU", "NZ", "OM", "PA", "PE", "PF", "PG",
      "PH", "PK", "PL", "PM", "PN", "PR", "PS", "PT", "PW", "PY", "QA", "RE",
      "RO", "RS", "RU", "RW", "SA", "SB", "SC", "SD", "SE", "SG", "SH", "SI",
      "SJ", "SK", "SL", "SM", "SN", "SO", "SR", "ST", "SV", "SY", "SZ", "TC",
      "TD", "TF", "TG", "TH", "TJ", "TK", "TL", "TM", "TN", "TO", "TR", "TT",
      "TV", "TW", "TZ", "UA", "UG", "UM", "US", "UY", "UZ", "VA", "VC", "VE",
      "VG", "VI", "VN", "VU", "WF", "WS", "YE", "YT", "ZA", "ZM", "ZW", "OTHER" };
  public static Set countryCodeSet = setFromArray(countryCodes);

  public static String[] countries = { null, "Andorra", "United Arab Emirates",
      "Afghanistan", "Antigua And Barbuda", "Anguilla", "Albania", "Armenia",
      "Netherlands Antilles", "Angola", "Antarctica", "Argentina",
      "American Samoa", "Austria", "Australia", "Aruba", "\uc385land Islands",
      "Azerbaijan", "Bosnia And Herzegovina", "Barbados", "Bangladesh",
      "Belgium", "Burkina Faso", "Bulgaria", "Bahrain", "Burundi", "Benin",
      "Bermuda", "Brunei Darussalam", "Bolivia", "Brazil", "Bahamas", "Bhutan",
      "Bouvet Island", "Botswana", "Belarus", "Belize", "Canada",
      "Cocos (Keeling) Islands", "Congo, The Democratic Republic Of The",
      "Central African Republic", "Congo", "Switzerland", "C\uc394Te D'Ivoire",
      "Cook Islands", "Chile", "Cameroon", "China", "Colombia", "Costa Rica",
      "Cuba", "Cape Verde", "Christmas Island", "Cyprus", "Czech Republic",
      "Germany", "Djibouti", "Denmark", "Dominica", "Dominican Republic",
      "Algeria", "Ecuador", "Estonia", "Egypt", "Western Sahara", "Eritrea",
      "Spain", "Ethiopia", "Finland", "Fiji", "Falkland Islands (Malvinas)",
      "Micronesia, Federated States Of", "Faroe Islands", "France", "Gabon",
      "United Kingdom", "Grenada", "Georgia", "French Guiana", "Guernsey",
      "Ghana", "Gibraltar", "Greenland", "Gambia", "Guinea", "Guadeloupe",
      "Equatorial Guinea", "Greece", "South Georgia And The South Sandwich Islands", 
      "Guatemala", "Guam", "Guinea-Bissau", "Guyana", "Hong Kong",
      "Heard Island And Mcdonald Islands", "Honduras", "Croatia", "Haiti",
      "Hungary", "Indonesia", "Ireland", "Israel", "Isle Of Man", "India",
      "British Indian Ocean Territory", "Iraq", "Iran, Islamic Republic Of",
      "Iceland", "Italy", "Jersey", "Jamaica", "Jordan", "Japan", "Kenya",
      "Kyrgyzstan", "Cambodia", "Kiribati", "Comoros", "Saint Kitts And Nevis",
      "Korea, Democratic People's Republic Of", "Korea, Republic Of", "Kuwait",
      "Cayman Islands", "Kazakhstan", "Lao People's Democratic Republic",
      "Lebanon", "Saint Lucia", "Liechtenstein", "Sri Lanka", "Liberia",
      "Lesotho", "Lithuania", "Luxembourg", "Latvia", "Libyan Arab Jamahiriya",
      "Morocco", "Monaco", "Moldova, Republic Of", "Montenegro", "Madagascar",
      "Marshall Islands", "Macedonia, The Former Yugoslav Republic Of", "Mali",
      "Myanmar", "Mongolia", "Macao", "Northern Mariana Islands", "Martinique",
      "Mauritania", "Montserrat", "Malta", "Mauritius", "Maldives", "Malawi",
      "Mexico", "Malaysia", "Mozambique", "Namibia", "New Caledonia", "Niger",
      "Norfolk Island", "Nigeria", "Nicaragua", "Netherlands", "Norway",
      "Nepal", "Nauru", "Niue", "New Zealand", "Oman", "Panama", "Peru",
      "French Polynesia", "Papua New Guinea", "Philippines", "Pakistan",
      "Poland", "Saint Pierre And Miquelon", "Pitcairn", "Puerto Rico",
      "Palestinian Territory, Occupied", "Portugal", "Palau", "Paraguay",
      "Qatar", "R\uc389union", "Romania", "Serbia", "Russian Federation",
      "Rwanda", "Saudi Arabia", "Solomon Islands", "Seychelles", "Sudan",
      "Sweden", "Singapore", "Saint Helena", "Slovenia",
      "Svalbard And Jan Mayen", "Slovakia", "Sierra Leone", "San Marino",
      "Senegal", "Somalia", "Suriname", "Sao Tome And Principe", "El Salvador",
      "Syrian Arab Republic", "Swaziland", "Turks And Caicos Islands", "Chad",
      "French Southern Territories", "Togo", "Thailand", "Tajikistan",
      "Tokelau", "Timor-Leste", "Turkmenistan", "Tunisia", "Tonga", "Turkey",
      "Trinidad And Tobago", "Tuvalu", "Taiwan, Province Of China",
      "Tanzania, United Republic Of", "Ukraine", "Uganda",
      "United States Minor Outlying Islands", "United States", "Uruguay",
      "Uzbekistan", "Holy See (Vatican City State)",
      "Saint Vincent And The Grenadines", "Venezuela",
      "Virgin Islands, British", "Virgin Islands, U.S.", "Viet Nam", "Vanuatu",
      "Wallis And Futuna", "Samoa", "Yemen", "Mayotte", "South Africa",
      "Zambia", "Zimbabwe", "Other" };
  public static Map countryNameMap = zip(countryCodes, countries);

  public static String[] mods = { null, "BOTTOM", "BORDER", "CENTRAL", "LEFT", "NEAR", "RIGHT", "TOP", 
                                  "N", "NNE", "NE", "ENE", 
                                  "E", "ESE", "SE", "SSE", 
                                  "S", "SSW", "SW", "WSW",
                                  "W", "WNW", "NW", "NNW" };
  
  public static Set modSet = setFromArray(mods);

  public static String[] modNames = { null, "Bottom", "Border", "Central", "Left", "Near", "Right", "Top",
                                      "North", "North North East", "North East", "East North East", 
                                      "East", "East South East", "South East", "South South East", 
                                      "South", "South South West", "South West", "West South West",
                                      "West", "West North West", "North West", "North North West" };
  public static Map modNameMap = zip(mods, modNames);
  
  public static String[] placeTypes = { null, "CELESTIAL", "CIVIL",
      "CONTINENT", "COUNTRY", "FAC", "GRID", "LATLONG", "MTN", "MTS", "PPL",
      "PPLA", "PPLC", "POSTBOX", "POSTALCODE", "RGN", "ROAD", "UTM", "WATER", "VEHICLE" };
  public static Set placeTypeSet = setFromArray(placeTypes);

  public static String[] placeTypeNames = { null, "Celestial Body",
      "Political/Administrative Region (sub-national)", "Contintent",
      "Country", "Facility", "Grid coordinate", "Latitude/Longitude", "Mountain",
      "Mountain Range", "Populated Place",
      "Capital of a first-order administrative division",
      "Capital of a country", "Post box", "Postal Code", "Region other than political/administrative",
      "Road", "Universal Transverse Mercator coordinate", "Body of Water", "Vehicle" };
  public static Map placeTypeNameMap = zip(placeTypes, placeTypeNames); 
  
  public static String[] frames = { null, "VIEWER", "INTRINSIC", "EXTRINSIC" };
  public static Set frameSet = setFromArray(frames);

  public static String[] directions = { null, "BEHIND", "ABOVE", "BELOW", "FRONT",
                                        "N", "NNE", "NE", "ENE", 
                                        "E", "ESE", "SE", "SSE", 
                                        "S", "SSW", "SW", "WSW",
                                        "W", "WNW", "NW", "NNW" };

  public static Set directionSet = setFromArray(directions);

  public static String[] directionNames = { null, "Behind", "Above", "Below", "In Front",
                                            "North", "North North East", "North East", "East North East", 
                                            "East", "East South East", "South East", "South South East", 
                                            "South", "South South West", "South West", "West South West",
                                            "West", "West North West", "North West", "North North West" };
  public static Map directionNameMap = zip(directions, directionNames);
  
  public static String[] linkTypes = { null, "EQ", "IN", "DC", "EC", "PO"};
  public static Set linkTypeSet = setFromArray(linkTypes);

  public static String[] linkTypeNames = { null, "Equivalence", "Inclusion",
      "Discrete Connection", "External Connection", 
      "Partial Overlap"};
  public static Map linkTypeNameMap = zip(linkTypes, linkTypeNames);
  
  public static String[] ctv = { null, "CITY", "TOWN", "VILLAGE" };
  public static Set ctvSet = setFromArray(ctv);
  
  public static String[] forms = { null, "NAM", "NOM" };
  public static Set formSet = setFromArray(forms);
  
  public static String[] signalTypes = { null, "DISTANCE", "DIRECTION" };
  public static Set signalTypeSet = setFromArray(signalTypes);
  
  // private copy of the task just for making types
  private static SpatialMLTask task = new SpatialMLTask();
  
  public static AnnotationType PLACE_TYPE = task.getAnnotationType(SpatialMLTask.PLACE_NAME);
  public static AnnotationType SIGNAL_TYPE = task.getAnnotationType(SpatialMLTask.SIGNAL_NAME);
  public static AnnotationType RLINK_TYPE = task.getAnnotationType(SpatialMLTask.RLINK_NAME);
  public static AnnotationType LINK_TYPE = task.getAnnotationType(SpatialMLTask.LINK_NAME);
  public static AnnotationType LINK_SUBORDINATES_TYPE = task.getAnnotationType(SpatialMLTask.LINK_SUBORDINATES_NAME);
  //public static AnnotationType PATH_EXTENT_TYPE = task.getAnnotationType(SpatialMLTask.PATH_EXTENT_NAME);
  
  /**
   * Helper function to pack an array into a new TreeSet object sorted
   * using the NullComparator.
   * 
   * @param array
   * @return
   */
  private static Set setFromArray(String[] array) {
    Set s = new TreeSet(new NullComparator());
    for (int i = 0; i < array.length; i++) {
      s.add(array[i]);
    }
    return s;
  }

  /**
   * Helper function to zip up parallel arrays of keys and values into a map.
   * @param keys
   * @param values
   * @return
   */
  private static Map zip(String[] keys, String[] values) {
    Map m = new LinkedHashMap();
    for (int i = 0; i < keys.length; i++) {
      m.put(keys[i], values[i]);
    }
    return m;
  }

  /**
   * Comparator where a 'null' is less than any object but equal to
   * another 'null'.
   * 
   * @author jricher
   * 
   */
  private static class NullComparator implements Comparator {
    public int compare(Object arg0, Object arg1) {
      if (arg0 == null) {
        if (arg1 == null) {
          // equal
          return 0;
        } else {
          // null < not null
          return -1;
        }
      } else {
        if (arg1 == null) {
          // not null > null
          return 1;
        } else {
          // null < not null
          return ((Comparable)arg0).compareTo(arg1);
        }
      }
    }

  }

  public static AWBAnnotation convertAnnotation(Object o) {
    if (o instanceof AWBAnnotation) {
      return (AWBAnnotation)o;
    } else if (o instanceof AnnotationRef) {
      return (AWBAnnotation)((AnnotationRef)o).getElement();
    } else {
      if (o != null) {
        log.warning("Don't know how to deal with " + o);
      }
      return null;
    }
  }

}
