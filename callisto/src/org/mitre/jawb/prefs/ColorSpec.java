
/* ----------------------------------------------------------------------
 * 
 * Copyright (c) 2002-2009 The MITRE Corporation
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
 * 
 * ----------------------------------------------------------------------
 * 
 * NOTICE
 * 
 * This software was produced for the U. S. Government
 * under Contract No. W15P7T-09-C-F600, and is
 * subject to the Rights in Noncommercial Computer Software
 * and Noncommercial Computer Software Documentation
 * Clause 252.227-7014 (JUN 1995).
 * 
 * (c) 2009 The MITRE Corporation. All Rights Reserved.
 * 
 * ----------------------------------------------------------------------
 *
 */
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

package org.mitre.jawb.prefs;

import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * The ColorSpec class is a holder for a background/foreground color
 * specification along with utility methods for handling these specifications.
 */
public class ColorSpec implements Cloneable, Serializable {


  public static final int DEBUG = 0;

    /**
     * The internal list of english color names to java.awt.Color objects.
     */
    private static final Hashtable COLORS;

    /**
     * The static initializer is used to initialize the color name table.
     */
    static {
        COLORS = new Hashtable();
        COLORS.put("alice blue",new Color(0xF0,0xF8,0xFF));
        COLORS.put("antique white",new Color(0xFA,0xEB,0xD7));
        COLORS.put("aqua",new Color(0x00,0xFF,0xFF));
        COLORS.put("aquamarine",new Color(0x7F,0xFF,0xD4));
        COLORS.put("azure",new Color(0xF0,0xFF,0xFF));
        COLORS.put("beige",new Color(0xF5,0xF5,0xDC));
        COLORS.put("bisque",new Color(0xFF,0xE4,0xC4));
        COLORS.put("black",new Color(0x00,0x00,0x00));
        COLORS.put("blanched almond",new Color(0xFF,0xEB,0xCD));
        COLORS.put("blue",new Color(0x00,0x00,0xFF));
        COLORS.put("blue violet",new Color(0x8A,0x2B,0xE2));
        COLORS.put("brown",new Color(0xA5,0x2A,0x2A));
        COLORS.put("burlywood",new Color(0xDE,0xB8,0x87));
        COLORS.put("cadet blue",new Color(0x5F,0x9E,0xA0));
        COLORS.put("chartreuse",new Color(0x7F,0xFF,0x00));
        COLORS.put("chocolate",new Color(0xD2,0x69,0x1E));
        COLORS.put("coral",new Color(0xFF,0x7F,0x50));
        COLORS.put("cornflower blue",new Color(0x64,0x95,0xED));
        COLORS.put("cornsilk",new Color(0xFF,0xF8,0xDC));
        COLORS.put("crimson",new Color(0xDC,0x14,0x3C));
        COLORS.put("cyan",new Color(0x00,0xFF,0xFF));
        COLORS.put("dark blue",new Color(0x00,0x00,0x8B));
        COLORS.put("dark cyan",new Color(0x00,0x8B,0x8B));
        COLORS.put("dark goldenrod",new Color(0xB8,0x86,0x0B));
        COLORS.put("dark gray",new Color(0xA9,0xA9,0xA9));
        COLORS.put("dark green",new Color(0x00,0x64,0x00));
        COLORS.put("dark khaki",new Color(0xBD,0xB7,0x6B));
        COLORS.put("dark magenta",new Color(0x8B,0x00,0x8B));
        COLORS.put("dark olive green",new Color(0x55,0x6B,0x2F));
        COLORS.put("dark orange",new Color(0xFF,0x8C,0x00));
        COLORS.put("dark orchid",new Color(0x99,0x32,0xCC));
        COLORS.put("dark red",new Color(0x8B,0x00,0x00));
        COLORS.put("dark salmon",new Color(0xE9,0x96,0x7A));
        COLORS.put("dark sea green",new Color(0x8F,0xBC,0x8F));
        COLORS.put("dark slate blue",new Color(0x48,0x3D,0x8B));
        COLORS.put("dark slate gray",new Color(0x2F,0x4F,0x4F));
        COLORS.put("dark turquoise",new Color(0x00,0xCE,0xD1));
        COLORS.put("dark violet",new Color(0x94,0x00,0xD3));
        COLORS.put("deep pink",new Color(0xFF,0x14,0x93));
        COLORS.put("deep sky blue",new Color(0x00,0xBF,0xFF));
        COLORS.put("dim gray",new Color(0x69,0x69,0x69));
        COLORS.put("dodger blue",new Color(0x1E,0x90,0xFF));
        COLORS.put("fire brick",new Color(0xB2,0x22,0x22));
        COLORS.put("floral white",new Color(0xFF,0xFA,0xF0));
        COLORS.put("forest green",new Color(0x22,0x8B,0x22));
        COLORS.put("fuchsia",new Color(0xFF,0x00,0xFF));
        COLORS.put("gainsboro",new Color(0xDC,0xDC,0xDC));
        COLORS.put("ghost white",new Color(0xF8,0xF8,0xFF));
        COLORS.put("gold",new Color(0xFF,0xD7,0x00));
        COLORS.put("goldenrod",new Color(0xDA,0xA5,0x20));
        COLORS.put("gray",new Color(0xBE,0xBE,0xBE));
        COLORS.put("green",new Color(0x00,0xFF,0x00));
        COLORS.put("green yellow",new Color(0xAD,0xFF,0x2F));
        COLORS.put("honeydew",new Color(0xF0,0xFF,0xF0));
        COLORS.put("hot pink",new Color(0xFF,0x69,0xB4));
        COLORS.put("indian red",new Color(0xCD,0x5C,0x5C));
        COLORS.put("indigo",new Color(0x4B,0x00,0x82));
        COLORS.put("ivory",new Color(0xFF,0xFF,0xF0));
        COLORS.put("khaki",new Color(0xF0,0xE6,0x8C));
        COLORS.put("lavender",new Color(0xE6,0xE6,0xFA));
        COLORS.put("lavender blush",new Color(0xFF,0xF0,0xF5));
        COLORS.put("lawn green",new Color(0x7C,0xFC,0x00));
        COLORS.put("lemon chiffon",new Color(0xFF,0xFA,0xCD));
        COLORS.put("light blue",new Color(0xAD,0xD8,0xE6));
        COLORS.put("light coral",new Color(0xF0,0x80,0x80));
        COLORS.put("light cyan",new Color(0xE0,0xFF,0xFF));
        COLORS.put("light goldenrod",new Color(0xEE,0xDD,0x82));
        COLORS.put("light goldenrod yellow",new Color(0xFA,0xFA,0xD2));
        COLORS.put("light green",new Color(0x90,0xEE,0x90));
        COLORS.put("light gray",new Color(0xD3,0xD3,0xD3));
        COLORS.put("light pink",new Color(0xFF,0xB6,0xC1));
        COLORS.put("light salmon",new Color(0xFF,0xA0,0x7A));
        COLORS.put("light sea green",new Color(0x20,0xB2,0xAA));
        COLORS.put("light sky blue",new Color(0x87,0xCE,0xFA));
        COLORS.put("light slate blue",new Color(0x84,0x70,0xFF));
        COLORS.put("light slate gray",new Color(0x77,0x88,0x99));
        COLORS.put("light steel blue",new Color(0xB0,0xC4,0xDE));
        COLORS.put("light yellow",new Color(0xFF,0xFF,0xE0));
        COLORS.put("lime",new Color(0x00,0xFF,0x00));
        COLORS.put("lime green",new Color(0x32,0xCD,0x32));
        COLORS.put("linen",new Color(0xFA,0xF0,0xE6));
        COLORS.put("magenta",new Color(0xFF,0x00,0xFF));
        COLORS.put("maroon",new Color(0xB0,0x30,0x60));
        COLORS.put("medium aquamarine",new Color(0x66,0xCD,0xAA));
        COLORS.put("medium blue",new Color(0x00,0x00,0xCD));
        COLORS.put("medium orchid",new Color(0xBA,0x55,0xD3));
        COLORS.put("medium purple",new Color(0x93,0x70,0xDB));
        COLORS.put("medium sea green",new Color(0x3C,0xB3,0x71));
        COLORS.put("medium slate blue",new Color(0x7B,0x68,0xEE));
        COLORS.put("medium spring green",new Color(0x00,0xFA,0x9A));
        COLORS.put("medium turquoise",new Color(0x48,0xD1,0xCC));
        COLORS.put("medium violet red",new Color(0xC7,0x15,0x85));
        COLORS.put("midnight blue",new Color(0x19,0x19,0x70));
        COLORS.put("mint cream",new Color(0xF5,0xFF,0xFA));
        COLORS.put("misty rose",new Color(0xFF,0xE4,0xE1));
        COLORS.put("moccasin",new Color(0xFF,0xE4,0xB5));
        COLORS.put("navajo white",new Color(0xFF,0xDE,0xAD));
        COLORS.put("navy",new Color(0x00,0x00,0x80));
        COLORS.put("navy blue",new Color(0x00,0x00,0x80));
        COLORS.put("old lace",new Color(0xFD,0xF5,0xE6));
        COLORS.put("olive",new Color(0x80,0x80,0x00));
        COLORS.put("olive drab",new Color(0x6B,0x8E,0x23));
        COLORS.put("orange",new Color(0xFF,0xA5,0x00));
        COLORS.put("orange red",new Color(0xFF,0x45,0x00));
        COLORS.put("orchid",new Color(0xDA,0x70,0xD6));
        COLORS.put("pale goldenrod",new Color(0xEE,0xE8,0xAA));
        COLORS.put("pale green",new Color(0x98,0xFB,0x98));
        COLORS.put("pale turquoise",new Color(0xAF,0xEE,0xEE));
        COLORS.put("pale violet red",new Color(0xDB,0x70,0x93));
        COLORS.put("papaya whip",new Color(0xFF,0xEF,0xD5));
        COLORS.put("peach puff",new Color(0xFF,0xDA,0xB9));
        COLORS.put("peru",new Color(0xCD,0x85,0x3F));
        COLORS.put("pink",new Color(0xFF,0xC0,0xCB));
        COLORS.put("plum",new Color(0xDD,0xA0,0xDD));
        COLORS.put("powder blue",new Color(0xB0,0xE0,0xE6));
        COLORS.put("purple",new Color(0xA0,0x20,0xF0));
        COLORS.put("red",new Color(0xFF,0x00,0x00));
        COLORS.put("rosy brown",new Color(0xBC,0x8F,0x8F));
        COLORS.put("royal blue",new Color(0x41,0x69,0xE1));
        COLORS.put("saddle brown",new Color(0x8B,0x45,0x13));
        COLORS.put("salmon",new Color(0xFA,0x80,0x72));
        COLORS.put("sandy brown",new Color(0xF4,0xA4,0x60));
        COLORS.put("sea green",new Color(0x2E,0x8B,0x57));
        COLORS.put("seashell",new Color(0xFF,0xF5,0xEE));
        COLORS.put("sienna",new Color(0xA0,0x52,0x2D));
        COLORS.put("silver",new Color(0xC0,0xC0,0xC0));
        COLORS.put("sky blue",new Color(0x87,0xCE,0xEB));
        COLORS.put("slate blue",new Color(0x6A,0x5A,0xCD));
        COLORS.put("slate gray",new Color(0x70,0x80,0x90));
        COLORS.put("snow",new Color(0xFF,0xFA,0xFA));
        COLORS.put("spring green",new Color(0x00,0xFF,0x7F));
        COLORS.put("steel blue",new Color(0x46,0x82,0xB4));
        COLORS.put("tan",new Color(0xD2,0xB4,0x8C));
        COLORS.put("teal",new Color(0x00,0x80,0x80));
        COLORS.put("thistle",new Color(0xD8,0xBF,0xD8));
        COLORS.put("tomato",new Color(0xFF,0x63,0x47));
        COLORS.put("turquoise",new Color(0x40,0xE0,0xD0));
        COLORS.put("violet",new Color(0xEE,0x82,0xEE));
        COLORS.put("violet red",new Color(0xD0,0x20,0x90));
        COLORS.put("wheat",new Color(0xF5,0xDE,0xB3));
        COLORS.put("white",new Color(0xFF,0xFF,0xFF));
        COLORS.put("white smoke",new Color(0xF5,0xF5,0xF5));
        COLORS.put("yellow",new Color(0xFF,0xFF,0x00));
        COLORS.put("yellow green",new Color(0x9A,0xCD,0x32));
    }

    /**
     * This is a utility method to test if the given String is empty.
     * To be empty the String must be null, or its trimmed length must be zero.
     *
     * @param string The String being tested.
     *
     * @return true if the test String is null or trimmable to zero length.
     */
    private static boolean isEmpty(String string) {
        return (((string!=null)?string.trim().length():0)<=0);
    }

    /**
     * This is a utility method for non-exception generating integer conversion.
     *
     * @param string The String to be converted.
     * @param defaultValue The default value for the conversion.
     *
     * @return The converted value or the default value if the requested
     *         conversion cannot occur.
     */
    private static int intOf(String string, int defaultValue) {
        try {
            return Integer.valueOf(string).intValue();
        } catch (Throwable t) {
            return defaultValue;
        }
    }

    /**
     * This method looks up a color value assuming that the supplied String
     * is the english name of that color.
     *
     * @param cstring The name of the color desired. Note: This value
     *        is not case sensitive.
     * @param defaultValue The value desired if the name is unknown.
     *
     * @return The java.awt.Color representation of the supplied english
     *         color name, or the defaultValue supplied if that name is
     *         unknown.
     */
    public static Color colorByName(String cstring, Color defaultValue) {
        if (isEmpty(cstring)) {
            return defaultValue;
        }
        cstring = (cstring!=null)?cstring.trim().toLowerCase():"";
        Color value = (Color)COLORS.get(cstring);
        if (value==null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * This method normalizes the color formats to the "r,g,b" format, where
     * the numeric representations supported are "#hnhnhn", "hnhnhn", and
     * "r,g,b" where:
     * <ul>
     *     <li> "#hnhnhn" a standard format for COLORS on web pages where
     *          hn stands for a hex number from 0x00 to 0xff, and the sequence
     *          is read red, green and blue respectively.
     *     <li> "hnhnhn" a standard format for COLORS on web pages where
     *          hn stands for a hex number from 0x00 to 0xff, and the sequence
     *          is read red, green and blue respectively.
     *     <li> "r,g,b" where r, g and b represent an integer value from 
     *          0 to 255 and the sequence is read red, green and blue
     *          respectively.
     * </ul>
     *
     * <br> Note: Alpha values for COLORS are not supported at this time.
     * <br> Note: Unsupported formats are returned unchanged.
     *
     * @param cstring The numeric representation being normalized.
     *
     * @return The numeric representation in "r,g,b" format.  Unsupported
     *         formats return the originally supplied String value.
     */
    private static String rgbStringOf(String cstring) {
        if (isEmpty(cstring)) {
            return cstring;
        }
        String converted = (cstring!=null)?cstring.trim():"";
        if (converted.startsWith("#")) {
            converted = converted.substring(1);
        }
        if (converted.length()!=6) {
            return cstring;
        }
        try {
            int rInt = Integer.parseInt(converted.substring(0,2),16);
            int gInt = Integer.parseInt(converted.substring(2,4),16);
            int bInt = Integer.parseInt(converted.substring(4,6),16);
            StringBuffer csb = new StringBuffer();
            csb.append(rInt);
            csb.append(',');
            csb.append(gInt);
            csb.append(',');
            csb.append(bInt);
            converted = csb.toString();
        } catch (NumberFormatException nfe) {
            converted = cstring;
        }
        return converted;
    }

    /**
     * This method creates a color object given a numeric representation.
     * The numeric representations supported are "#hnhnhn", "hnhnhn", and
     * "r,g,b" where:
     * <ul>
     *     <li> "#hnhnhn" a standard format for COLORS on web pages where
     *          hn stands for a hex number from 0x00 to 0xff, and the sequence
     *          is read red, green and blue respectively.
     *     <li> "hnhnhn" a standard format for COLORS on web pages where
     *          hn stands for a hex number from 0x00 to 0xff, and the sequence
     *          is read red, green and blue respectively.
     *     <li> "r,g,b" where r, g and b represent an integer value from 
     *          0 to 255 and the sequence is read red, green and blue
     *          respectively.
     * </ul>
     *
     * <br> Note: Alpha values for COLORS are not supported at this time.
     *
     * @param cstring The numeric representation of the desired color.
     * @param defaultValue The value desired if number is not valid.
     *
     * @return The java.awt.Color representation of the supplied numeric
     *         representation, or the defaultValue supplied if that numeric 
     *         representation is invalid.
     */
    public static Color colorByNumber(String cstring, Color defaultValue) {
        if (isEmpty(cstring)) {
            return defaultValue;
        }
        Color value = defaultValue;
        cstring = rgbStringOf(cstring);
        StringTokenizer st = new StringTokenizer(cstring,",",false);
        String rString = (st.hasMoreTokens())?st.nextToken():null;
        String gString = (st.hasMoreTokens())?st.nextToken():null;
        String bString = (st.hasMoreTokens())?st.nextToken():null;
        int rInt = intOf(rString,-1);
        int gInt = intOf(gString,-1);
        int bInt = intOf(bString,-1);
        if ((rInt<0)||(rInt>255)||
            (gInt<0)||(gInt>255)||
            (bInt<0)||(bInt>255)) {
            value = defaultValue;
        } else {
            value = new Color(rInt,gInt,bInt);
        }
        return(value);
    }

    /**
     * This method creates a color object given a String representation.
     * This method will check first to see if the supplied String is a
     * known english color name.  If it is not it will then attempt to
     * create a color assuming the supplied String is a numeric representation.
     * The numeric representations supported are "#hnhnhn", "hnhnhn", and
     * "r,g,b" where:
     * <ul>
     *     <li> "#hnhnhn" a standard format for COLORS on web pages where
     *          hn stands for a hex number from 0x00 to 0xff, and the sequence
     *          is read red, green and blue respectively.
     *     <li> "hnhnhn" a standard format for COLORS on web pages where
     *          hn stands for a hex number from 0x00 to 0xff, and the sequence
     *          is read red, green and blue respectively.
     *     <li> "r,g,b" where r, g and b represent an integer value from 
     *          0 to 255 and the sequence is read red, green and blue
     *          respectively.
     * </ul>
     *
     * <br> Note: Alpha values for COLORS are not supported at this time.
     *
     * @param color The numeric representation of the desired color.
     * @param defaultValue The value desired if supplied representation is not
     *        a valid color name or numeric representation.
     *
     * @return The java.awt.Color representation of the supplied representation,
     *         or the defaultValue supplied if that representation is invalid.
     */
    public static Color colorOf(String color, Color defaultValue) {
      if (DEBUG > 0)
        System.err.println("CS.colorOf " + color + 
                           " default: " + defaultValue);
        if (isEmpty(color)) {
          if (DEBUG > 0)
            System.err.println("CS.colorOf: color: " + color + 
                               " is empty, returning default: " + 
                               defaultValue);
            return defaultValue;
        }
        Color value = (Color)COLORS.get(color);
        if (value==null) {
          // Callisto has always used the Color.decode(str) mechanism, which
          // colorByNumber doesn't support. decodeColor accepts the '#' or
          // "0x" prefixed hex values.
          if (DEBUG > 1)
            System.err.println("\tnot a color name, trying decodeColor");
          value = Preferences.decodeColor (color,null);
          // if neither of those work, try adding a # in front, to allow
          // the hnhnhn style color specification
          if (value == null) {
            String poundColor = "#" + color;
            if (DEBUG > 1)
              System.err.println("\tnot a hex-coded string, trying " +
                                 poundColor);
            value = Preferences.decodeColor(poundColor, defaultValue);
          }
          //value = colorByNumber(color,defaultValue);
        }
        return value;
    }

    /**
     * Method to convert a color part (red, green or blue) to its appropriate
     * hexadecimal representation.
     *
     * @param colorSpec The portion of the color to be converted to hex.
     *
     * @return The 2 digit hex representation of the given color spec.  Note
     *         that invalid color specs (outside of 0-255) will return "00".
     *         (So a totally invalid color will return essentially "black"
     *         when being converted into a String.
     */
    private static String hexStringOf(int colorSpec) {
        if ((colorSpec<0)||(colorSpec>255)) {
            return "00";
        }
        StringBuffer hsb = new StringBuffer(Integer.toHexString(colorSpec));
        while (hsb.length()<2) {
            hsb.insert(0,'0');
        }
        return hsb.toString();
    }

    /**
     * Utility method to generate a hexadecimal String representation of
     * the given Color object.
     * The output format of this method is:
     *     "hnhnhn" a standard format for COLORS on web pages where
     *     hn stands for a hex number from 0x00 to 0xff, and the sequence
     *     is read red, green and blue respectively.
     *
     * @param color The supplied color.  If null then the color will be
     *         assumed to be Color.black.
     *
     * @return The hexadecimal representation of the given color.
     */
    public static String hexStringOf(Color color) {
        color = (color!=null)?color:Color.black;
        StringBuffer hsb = new StringBuffer();
        hsb.append(hexStringOf(color.getRed()));
        hsb.append(hexStringOf(color.getGreen()));
        hsb.append(hexStringOf(color.getBlue()));
        return hsb.toString();
    }

    /**
     * Utility method to invert the specified color.
     * This color inversion is done by translating each color value portion
     * by translation into the color space.  I.E. each color element being
     * a number from 0 to 255 is translated into the equivalent number from
     * 255 to 0.
     *
     * @param color The original color.
     *
     * @return The inverted color.
     */
    public static Color invert(Color color) {
        if (color==null) {
            return null;
        }
        int red = 255-color.getRed();
        int green = 255-color.getGreen();
        int blue = 255-color.getBlue();
        return(new Color(red,green,blue));
    }

    /**
     * Method to create a ColorSpec object from the given String specification.
     *
     * @param spec The color specification as a String.
     *
     * @return The new ColorSpec generated by the supplied String
     *         representation.
     */
    public static ColorSpec valueOf(String spec) {
        return new ColorSpec(spec);
    }

    /**
     * The background color of the specification.
     */
    private Color background = Color.black;
    /**
     * The foreground color of the specification.
     */
    private Color foreground = Color.white;

    /**
     * Default constructor.
     */
    public ColorSpec() {
        super();
    }

    /**
     * Constructor.
     *
     * @param spec The color specification as a String.
     */
    public ColorSpec(String spec) {
        super();
        fromString(spec);
    }

    /**
     * Retrieve the background color.
     *
     * @return The background color of this ColorSpec.
     */
    public Color getBackground() {
        return background;
    }

    /**
     * Set the background color.
     * If the supplied background color is null, the background color will
     * be set to Color.black.
     *
     * @param background The new background color.
     */
    public void setBackground(Color background) {
        this.background = (background!=null)?background:Color.black;
    }

    /**
     * Determine if the foreground color was set.
     *
     * @return true if the foreground color is not null.
     */
    public boolean isForegroundSet() {
        return (foreground!=null);
    }

    /**
     * Retrieve the foreground color.
     *
     * @return The foreground color of this ColorSpec.
     */
    public Color getForeground() {
        return (foreground!=null)?foreground:getDefaultForeground();
    }

    /**
     * Set the foreground color.
     *
     * @param foreground The new foreground color.
     */
    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    /**
     * This method is used to get the foreground color based on the background
     * color and is used primarily when setting the foreground color when it is
     * unspecified in a String representation of the ColorSpec (such as
     * from a hand-edited property file).
     *
     * @return The preferred default foreground color based on the current
     *         background color.
     */
    private Color getDefaultForeground() {
        boolean bgIsLight = false;
        bgIsLight |= (background.getRed()>=128);
        bgIsLight |= (background.getGreen()>=128);
        bgIsLight |= (background.getBlue()>=128);
        return (bgIsLight)?Color.black:Color.white;
    }


    public static Color grayOut(Color color) {
      if (color==null) {
        return null;
      }

      int red = color.getRed();
      int green = color.getGreen();
      int blue = color.getBlue();
      int alpha = color.getAlpha();
      float weight = 0.5f;
      Color newColor = new Color (red, green, blue, (int)(weight * alpha));
      if (DEBUG > 1)
        System.err.println ("grayOut: " + color + "->" + newColor.darker());
      return newColor.darker();
    }

    /**
     * Set the foreground and background colors from the color specification.
     *
     * @param spec The color specification as a String.
     */
    public void fromString(String spec) {
        spec = (spec!=null)?spec.trim().toLowerCase():"";
        int specLen = spec.length();
        if (specLen<=0) {
            background = Color.black;
            foreground = Color.white;
            return;
        }
        if ((spec.charAt(0)=='{')&&(spec.charAt(specLen-1)=='}')) {
            spec = spec.substring(1,specLen-1);
        }
        StringTokenizer st = new StringTokenizer(spec,",",false);
        String backSpec = (st.hasMoreTokens())?st.nextToken():null;
        String foreSpec = (st.hasMoreTokens())?st.nextToken():null;
        setBackground(colorOf(backSpec,Color.black));
        setForeground(colorOf(foreSpec,null));
    }

    /**
     * Generate a String version of this object.
     * String format on the output is {background,foreground} where both
     * background and foreground will be in the form:
     *     "#hnhnhn" a standard format for COLORS on web pages where
     *     hn stands for a hex number from 0x00 to 0xff, and the sequence
     *     is read red, green and blue respectively.
     *
     * @return The String form of this object.
     */
    public String toString() {
        StringBuffer tsb = new StringBuffer();
        tsb.append('{');
        tsb.append('#');
        tsb.append(hexStringOf(background));
        if (foreground!=null) {
            tsb.append(',');
            tsb.append('#');
            tsb.append(hexStringOf(foreground));
        }
        tsb.append('}');
        return tsb.toString();
    }

    /**
     * Main method for testing only!
     *
     * @param args The array of String arguments passed by the command
     *         line interpreter.
     */
    public static void main(String[] args) {
        try {
            String test = "{red,blue}";
            System.out.println("test string = "+test);
            System.out.println("test ColorSpec = "+valueOf(test));
            test = "red,blue}";
            System.out.println("test string = "+test);
            System.out.println("test ColorSpec = "+valueOf(test));
            test = "{red,green,blue}";
            System.out.println("test string = "+test);
            System.out.println("test ColorSpec = "+valueOf(test));
            test = "red";
            System.out.println("test string = "+test);
            System.out.println("test ColorSpec = "+valueOf(test));
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);
    }
}
