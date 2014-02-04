
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

package org.mitre.jawb.io;

import java.io.*;
import java.net.URI;
import java.net.URL;

import java.util.*;

/* This code opens a named input SGML file and transforms
 * it into a standard Atlas Interchange Format (AIF) output
 * file.
 *
 * Author: David Day
 * Edit History:
 * 2003/01/25 Began development.
 * 2003/05/17 Separated into class files (red)
 */

/**
 * Define the SgmlTag class and its constructor, and two methods that are used
 * to completely define the object from its string realization.
 * This is a class that will be used in the main method of the ParseSgml
 * class.<p>
 * 
 * This class is final and cannot be extended. This eliminates unfortunate
 * mistakes, but most of all makes access faster (since methods can be
 * inlined).
 */
public final class SgmlTag {

    /** Constant indicating a 'flag' attribute which is set with no value */
    public static final String VALUELESS_ATTR = "Sgml::AttributeIsPresent";
    
    /** Constant indicating a tag has no attributes since it is not parsed */
    public static final String UNPARSED_ATTRS = "Sgml::UnparsedAttributes";

    /* Object fields */

    /* Text from tags read from file. This is only to be used when parsing */
    private String tagText        = "";
    /* Everything before the first whitespace character (and after a
     * possible initial '/') */
    protected String gidText        = "";
    /* Everything following the first whitespace (and preceding any possible
     * trailing '/'). */
    protected String attrsText      = null;
    /** maintains case-sensitive order of attributes */
    protected List   attrsList      = new ArrayList();
    /** case-insensitive (lowercase) mapping of attributes to values */
    protected Map    attrsMap       = new HashMap ();
    /** type of tag */
    protected Type   tagType        = Type.UNKNOWN;
    protected int    gidEndLoc;
    protected int    tagCharOffset  = -1;
    protected int    tagIndex       = -1;
    protected ArrayList orphans = new ArrayList();

    /**
     * Here is the SgmlTag object constructor method used when reading in a
     * document. This should be called after the <code>in</code> Reader has
     * read a single '&lt;' character.
     *
     * @param offset the offset of the initial '&lt;' character.
     * @param in character stream from which to construct the tag.
     */
    public SgmlTag(int offset, Reader in) throws IOException {
	// System.err.println("Entering SgmlTag constructor: offset="+offset);
        tagCharOffset = offset;
        readSgmlTagText (in);
	// System.err.println("Leaving SgmlTag constructor");
    }

    /** Private constructor for factory method use only */
    private SgmlTag (Type type, int tagCount, int offset) {
        tagType       = type;
        tagIndex      = tagCount;
        tagCharOffset = offset;
    }

    /** Private constructor for factory method use only */
    private SgmlTag (Type type, int tagCount, int offset, String gid) {
        if (gid != null && ! gid.matches("[a-zA-Z]+[a-zA-Z0-9-_.:]*"))
          throw new IllegalArgumentException ("Bad ID: "+gid);
        
	// System.err.println("Entering SgmlTag "+type+" constructor:" +
        // " offset = " + offset + " gid = " + gid);
        tagType       = type;
        tagIndex      = tagCount;
        tagCharOffset = offset;
        gidText       = gid;
    };

  /** Get the Global ID of this tag, if it exists. */
  public String getGid () { return gidText; }
  
    /**
     * Factory method to create an OPEN tag at the specified offset in the
     * text, with the specified gid (name) and no attributes.<p>
     *
     * @param gid name of the tag to create
     * @param offset the offset of the initial '&lt;' character.
     * @throws IllegalArgumentException if the gid contains a '/' character
     */
    public static SgmlTag createOpenTag(int tagCount, int offset, String gid) {
        SgmlTag tag = new SgmlTag (Type.OPEN, tagCount, offset, gid);
        tag.tagText = gid;
        // System.err.println("Leaving SgmlTag constructor");
        return tag;
    }

    /**
     * Factory method to create an EMPTY tag at the specified offset in the
     * text, with the specified gid (name) and no attributes. The gid must not
     * have a trailing '/' char (which will cause duplicates) and may contain
     * extra whitespace after the actual gid.
     *
     * @param gid name of the tag to create
     * @param offset the offset of the initial '&lt;' character.
     * @throws IllegalArgumentException if the gid contains a '/' character
     */
    public static SgmlTag createEmptyTag(int tagCount, int offset, String gid) {
        SgmlTag tag = new SgmlTag (Type.EMPTY, tagCount, offset, gid);
        tag.tagText = gid+" /";
        // System.err.println("Leaving SgmlTag constructor");
        return tag;
    }

    /**
     * Factory method to create a CLOSE tag at the specified offset in the
     * text, with the specified gid (name). The gid must <i>not</i> begin with
     * a '/' char.
     *
     * @param gid name of the tag to create
     * @param offset the offset of the initial '&lt;' character.
     * @throws IllegalArgumentException if the gid contains a '/' character
     */
    public static SgmlTag createCloseTag(int tagCount, int offset, String gid) {
        SgmlTag tag = new SgmlTag (Type.CLOSE, tagCount, offset, gid);
        // System.err.println("Leaving SgmlTag constructor");
        return tag;
    }

    /**
     * Factory method to create a DECLARATION tag at the specified offset,
     * which will have a single {@link #UNPARSED_ATTRS} attrbute with value
     * <code>content</code>, if content is non-null. The gid must begin with
     * the '!' or '?' character. The content tag (if non-null) should
     * include any whitespace following the gid (a space will be prepended if
     * not), and the trailing '?' if the gid began with one.
     *
     * @param offset the offset of the initial '&lt;' character.
     * @param gid name of the Declaration tag <i>with</i> initial '!' or
     * '?'. If starting w/ '?', and the content is null, the gid must end w/
     * '?' also.
     * @param content text after gid. if gid starts w/ a '?' and content is
     * not null, content must end with a '?'.
     * @throws IllegalArgumentException if the gid is null or does not begin
     *         with '!' or '?'
     */
    public static SgmlTag createDeclarationTag(int tagCount, int offset,
                                               String gid, String content) {
        char c;
        if ( (gid == null ||
              ! ( (c=gid.charAt(0)) == '!' || c == '?')) || // not a declaration
             (c == '?' && // content or gid needs trailing '?'
              ! ( (content != null && content.charAt(content.length()-1) == '?') ||
                  (gid.charAt(gid.length()-1) == '?') )) ) {
            throw new IllegalArgumentException
              ("Invalid SGML declaration: "+gid+" "+content);
        }          
            
        SgmlTag tag = new SgmlTag (Type.DECLARATION, tagCount, offset);
        tag.gidText = gid;

        if (content != null) {
            if (! Character.isWhitespace (content.charAt(0)))
                content = " "+content;
            tag.insertAttribute (UNPARSED_ATTRS, content);
            tag.attrsText = content;
        }
        // System.err.println("Leaving SgmlTag constructor");
        return tag;
    }

    /**
     * Factory method to create a COMMENT tag at the specified offset, which
     * will have no attributes or gid.  Only the specified text wrapped in
     * "!--" and "--" strings. The resulting tags tagIndex will
     * remain at -1 to indicate that it was not in the original document. If
     * other tags are at this offset, resulting order is somewhat arbitrary,
     * as output only ensures proper nesting.<p>
     *
     * TODO: change this behavior to be able to specify where in relation to
     * other tags at this offset. This should be possible using a tagList in
     * the ParseSgml object, and removing the 'tagIndex' field all together.
     *
     * @param text text of the comment including any whitespace within
     * @param offset the offset of the initial '&lt;' character.
     */
    public static SgmlTag createCommentTag(int tagCount, int offset, String text) {
        SgmlTag tag = new SgmlTag (Type.COMMENT, tagCount, offset);
        tag.tagText = "!--"+(text==null ? "" : text)+"--";
        // System.err.println("Leaving SgmlTag constructor");
        return tag;
    }

    /***********************************************************************/
    /* Read/Parse methods when constructing a tag from an input stream */
    /***********************************************************************/

	/** This method reads from the input stream until the end of the
	 *  SGML tag is encountered.  This method respects quotes within the
	 *  tag's attribute values, which means that upon entering a quoted
	 *  passage, all '>' chars will be ignored until exiting the quotation
	 *  context (that is, finding the matching/closing quote character).
	 *  For example, in this tag:
	 *      <foo bar="x->y">
	 *  the right angle bracket ('>') character embedded in the bar
	 *  attribute value will be ignored, as it is within the context of a
	 *  quotation.
	 *
	 * This method returns the string that constitutes the SgmlTag itself.
	 */
    private String readSgmlTagText(Reader in) throws IOException {
	
	StringBuffer stringFound = new StringBuffer ();
	int    insideQuote    = 0;
	int    untilChar      = '>';
	int    quoteChar      = '"';
	int    charAsInt;
	char   character;
        boolean maybeComment1 = false;
        boolean maybeComment2 = false;
        boolean maybeExComment1 = false;
        boolean maybeExComment2 = false;
        boolean insideComment = false;
	
	//System.err.println("Entering readSgmlTagText...");
	for (int i = 0; (charAsInt = in.read()) != -1; i++) {
	    character = (char) charAsInt;
	    //System.err.println("i = " + i + ": "+character);

            // Adds a crap load to processing, but gotta handle comments now
            // and we've got no lookahead or pushback... JavaCC here I come.
            // ...and it looks like hell
            if (i==0 && character == '!') {
                maybeComment1 = true;
            } else {
              if (insideComment) {
                if (character == '-') {
                  insideComment = false;
                  maybeExComment1 = true;
                } // else still in comment
              } else if (maybeComment1) {
                maybeComment1 = false;
                if (character == '-') {
                  maybeComment2 = true;
                }
              } else if (maybeComment2) {
                maybeComment2 = false;
                if (character == '-') {
                  insideComment = true;
                }
              } else if (maybeExComment1) {
                maybeExComment1 = false;
                if (character == '-') {
                  maybeExComment2 = true;
                } else {
                  insideComment = true;
                }
              } else if (maybeExComment2) {
                maybeExComment2 = false;
                if (character == '>') {
                    // no parsing of comments
                    insideComment = false;
                    tagText = stringFound.toString();
                    tagType       = Type.COMMENT;
                    return tagText;
                } else if (character == '-') {// probalbly not well formed,
                    maybeExComment2 = true;   // but we'll forgive it
                } else {
                    insideComment = true;
                }
              }
            } 

            // How do we specify single forward quote literal? ('\'')?
	    if (!insideComment) {
                if (character == '"' || character == '\'') {
                    if (insideQuote == 0) {
                        insideQuote = 1;
                        quoteChar   = character;
                    } else if (insideQuote == 1 && character == quoteChar) {
                        insideQuote = 0;
                    }
                }
	    
                else if (character == untilChar && insideQuote == 0) {
                    //System.err.println("Leaving readSgmlTagText/1 stringFound = " + stringFound);
                    tagText = stringFound.toString(); /* Get string that constitutes body of sgmlTag.              */
                    parseSgmlTag();        /* Identify major constituents of tag (slashes, gid, attrs). */
                    parseTagAttributes();  /* Map attribute/value string into list representation.      */
                    return stringFound.toString();
                }
            }
            stringFound.append (String.valueOf(character));

	}
	//System.err.println("Leaving readSgmlTagText/2 stringFound = " + stringFound);
	tagText = stringFound.toString (); /* Get string that constitutes body of sgmlTag.              */
	parseSgmlTag();        /* Identify major constituents of tag (slashes, gid, attrs). */
	parseTagAttributes();  /* Map attribute/value string into list representation.      */
	return tagText;
    }


  /**
   * Parse SGML tag text:
   * <ul>
   *   <li>distinguish between genericid and attributes
   *   <li>determine tag type (start, and
   *   <li>return tagType of tag (integer).
   * <ul>
   */
    private Type parseSgmlTag() {
	/* Local method variables. */
	char character;
	int whitespaceIndex;
	int tagTextLength = tagText.length();

	/* Initialize object fields. */
	tagType       = Type.OPEN;

	/* we begin parsing as if there were only a GID. */
	attrsText = null;

	/* System.err.println("Entering parseSgmlTag..."); */

	/* Everything before the first whitespace character (and after a
	 * possible initial '/') should be considered the GID;
	 * Everything following the first whitespace (and preceding
	 * any possible trailing '/') should be considered the attrsText.
	 */
	character = tagText.charAt(0);
	if (character == '/') {
	    tagType = Type.CLOSE;
	} else if (character == '?' || character == '!') {
	    tagType = Type.DECLARATION;
	}

	/* Find first whitespace char, if any. */
        whitespaceIndex = -1;
        for (int i=0; i<tagTextLength; i++) {
          if (Character.isWhitespace (tagText.charAt(i))) {
            whitespaceIndex = i;
            break;
          }
        }
	if (whitespaceIndex > 0) {
	    if (tagType == Type.CLOSE) {
		/* Skip the initial '/' character. */
		gidText   = tagText.substring(1,whitespaceIndex);
		attrsText = tagText.substring(whitespaceIndex,tagTextLength);
	    } else {
		gidText   = tagText.substring(0,whitespaceIndex);
		/* Now examine final character to determine if this is an Type.EMPTY tag. */
		character = tagText.charAt(tagTextLength - 1);
		if (character == '/') {
		    tagType = Type.EMPTY;
		    attrsText = tagText.substring(whitespaceIndex,tagTextLength - 1);
		} else {
		    attrsText = tagText.substring(whitespaceIndex,tagTextLength);
		}
	    }
	} else {
	    if (tagType == Type.CLOSE) {
		gidText = tagText.substring(1,tagTextLength);
	    } else {
		/* Examine final character to determine if this is an Type.EMPTY tag. */
		character = tagText.charAt(tagTextLength - 1);
		if (character == '/') {
		    tagType = Type.EMPTY;
		    gidText = tagText.substring(0,tagTextLength - 1);
		} else {
		    gidText = tagText.substring(0,tagTextLength);
		}
	    }
	}

	/* System.err.println("tagType = " + tagType + "; gidText = <<" +
           gidText + ">>; attrsText = <<" + attrsText + ">>."); */
	return tagType;
    }

  /**
   * Parse SGML attribute values into a list.
   * @return number of attributes found.
   */
    private int parseTagAttributes() {

        /* Constants: Components of attribute values list. */
        final int NOTHING      = 0;
        final int ATTRNAME     = 1;
        final int EQUALSSIGN   = 2;
        final int ATTRVALUE    = 3;
        final int QUOTECONTEXT = 4;

        /* Constants: Various significant characters. */
        final char DBLQUOTE   = '"';
        final char SNGLFQUOTE = '\'';
        final char SNGLBQUOTE = '`';
        final char BACKSLASH  = '\\';

        /* *********************************************************************
	 * Loop through the string of attribute value pairs.  The pattern
	 * of the string must satisfy either: attrName = attrValue, where
	 * the attrValue may be quoted, or a lone attrName.  (Should look
	 * at SGML and HTML documentation to see if there are other situations
	 * that fall outside these two very general cases.).
	 *
	 * attrName1 = attrValue1 attrName2 attrName3="attrValue3" ...
	 * 11111111124433333333332111111111211111111125555555555552
	 * 1 = insideOf: ATTRNAME, waitingFor: EQUALSSIGN
	 * 2 = insideOf: NOTHING, waitingFor: EQUALSSIGN
	 * 3 = insideOf: ATTRVALUE
	 * 4 = insideOf: NOTHING, waitingFor: ATTRVALUE
	 * 5 = insideOf: QUOTECONTEXT, waitingFor: ATTRNAME
	 * 
	 * Constants defined earlier -- Components of attribute values list.
	 * static final int NOTHING      = 0;
	 * static final int ATTRNAME     = 1;
	 * static final int EQUALSSIGN   = 2;
	 * static final int ATTRVALUE    = 3;
	 * static final int QUOTECONTEXT = 4;
	 *
	 *********************************************************************** */
	String attrName  = "";
	String attrValue = "";
	char character;
	char quoteChar  = '"';
	int  offsetMark = 0;
	int  waitingFor = ATTRNAME;
	int  insideOf   = NOTHING;
	int  errorFoundP = 0;
        boolean charIsEscaped = false;

	/* System.err.println("entering parseTagAttributes: attrsText = " + attrsText); */

	if (attrsText == null) {
	    // System.err.println("leaving parseTagAttributes: no attrsText (null)");
	    return 0;
	}


	if (tagType == Type.DECLARATION) {
            insertAttribute (UNPARSED_ATTRS, attrsText);
	    return getAttributeCount ();
	}
	
        int  attrsTextLength = attrsText.length();

	for (int i = 0; i < attrsTextLength; i++) {
	    character = attrsText.charAt(i);
	    /* System.err.println("parseTagAttributes: char = " + character +
               " insideOf = " + insideOf + " quoteChar = " + quoteChar +
               " offsetMark = " + offsetMark + " waitingFor = " + waitingFor); */

	    /* Character type 1: quote */
	    if (character == DBLQUOTE || character == SNGLFQUOTE || character == SNGLBQUOTE) {
		if (insideOf != QUOTECONTEXT) {
		    if (waitingFor == ATTRVALUE) {
			/* System.err.println("found start of quote");			 */
			insideOf    = QUOTECONTEXT;
			quoteChar   = character;
			offsetMark  = i;
		    } else {
			System.err.println("parseTagAttributes error: not expecting quote in this place");
			System.err.println(" i=" + i + " attrsText=<<" + attrsText + ">>");
			errorFoundP = 1;
			/* Throw exception with parsing error here! */
		    }
		} else if (character == quoteChar && ! charIsEscaped) {
                    /* System.err.println("found end of quote"); */
                    /* The only valid QUOTECONTEXT is when it has been entered in
                     * the ATTRVALUE context.  Thus, upon exiting this context, we can
                     * presume that we are ending the ATTRVALUE context.
                     */
                    insideOf   = NOTHING;
                    waitingFor = ATTRNAME;
                    attrValue  = attrsText.substring(offsetMark + 1, i);
                    /* Add attribute/value pair to tag */
                    insertAttribute (attrName, attrValue);
                } else {
                  charIsEscaped = false;
                  /* Do nothing --- we are still within a quoted context. */
		}
	    } else if (insideOf == QUOTECONTEXT) {
              if (! charIsEscaped && character == '/')
                charIsEscaped = true;
              else
                charIsEscaped = false;

            /* Character type 2: whitespace */
	    } else if (Character.isWhitespace (character)) {
		if (insideOf == NOTHING) {
		    /* Don't have to do anything... */
		} else if (insideOf == ATTRNAME) {
		    insideOf   = NOTHING;
		    waitingFor = EQUALSSIGN;
		    attrName   = attrsText.substring(offsetMark,i);
		    /* System.err.println("attrName = " + attrName); */
		    offsetMark = i;
		} else if (insideOf == ATTRVALUE) {
		    insideOf   = NOTHING;
		    waitingFor = ATTRNAME;
		    attrValue = attrsText.substring(offsetMark,i);
		    offsetMark = i;
                    /* Add attribute/value pair to tag */
                    insertAttribute (attrName, attrValue);
		} else {
		    /* Shouldn't ever reach here.... */
		    System.err.println("parseTagAttributes error: not expecting to reach this whitespace condition");
		    System.err.println(" i=" + i + " attrsText=<<" + attrsText + ">>");
		    errorFoundP = 1;
		}
	    /* Character type 3: equals sign */
	    } else if (character == '=') {
		if (insideOf == ATTRNAME) {
		    insideOf   = NOTHING;
		    waitingFor = ATTRVALUE;
		    attrName = attrsText.substring(offsetMark,i);
		    /* System.err.println("attrName = " + attrName); */
		} else if (waitingFor == EQUALSSIGN) {
		    insideOf   = NOTHING;
		    waitingFor = ATTRVALUE;
		} else {
		    System.err.println("parseTagAttributes error: not expecting equals sign in this place");
		    System.err.println(" i=" + i + " attrsText=<<" + attrsText + ">>");
		    errorFoundP = 1;
		    /* Throw exception with parsing error here! */
		}
	    /* Character type 4: everything else: Non whitespace; non-quote; non equals sign. */
	    } else {
		if (insideOf == ATTRNAME || insideOf == ATTRVALUE) {
		    /* Keep on trucking ... */
		} else if (waitingFor == ATTRVALUE) {
		    offsetMark = i;
		    insideOf   = ATTRVALUE;
		} else if (waitingFor == ATTRNAME) {
		    offsetMark = i;
		    insideOf   = ATTRNAME;
		    waitingFor = EQUALSSIGN;
		} else if (waitingFor == EQUALSSIGN) {
		    /* This must be situation in which no attribute value is specified.
		     * Create special attribute value to indicate this situation.
		     * AttrName has already been captured (triggered by whitespace).
		     */
                    /* Add attribute/value pair to tag */
                    insertAttribute (attrName, VALUELESS_ATTR);

		    offsetMark = i;
		    insideOf   = ATTRNAME;
		    waitingFor = EQUALSSIGN;
		} else {
		    /* Do nothing. */
		}
	    }
	}

	/* Have reached end of attrsText string.  Finalize any processing that may
	 * remain unfinished.
	 */
	if (waitingFor == EQUALSSIGN) {
	    /* This must be situation in which no attribute value is specified.
	     * Create special attribute value to indicate this situation.
	     */
	    attrName   = attrsText.substring(offsetMark,attrsTextLength);
            /* Add attribute/value pair to tag */
            insertAttribute (attrName, VALUELESS_ATTR);
	} else if (insideOf == ATTRVALUE) {
	    attrValue  = attrsText.substring(offsetMark,attrsTextLength);
            /* Add attribute/value pair to tag */
            insertAttribute (attrName, attrValue);
	} else if (insideOf == NOTHING) {
	    /* Fine situation; don't do anything. */
	} else {
	    /* Should never reach here; indicates incomplete parsing. */
	    System.err.println("parseTagAttributes error: not expecting end of attrsText here");
	    System.err.println(" attrsText=<<" + attrsText + ">>");
	    errorFoundP = 1;
	    /* Throw exception with parsing error here! */
	}

        // dump when there's an error
	if (errorFoundP == 1) {
	    System.err.print ("SgmlTag parseError: final parsed attr/values: ");
	    Iterator attrsIter = attrsList.iterator();
	    while (attrsIter.hasNext()) {
		attrName  = (String) attrsIter.next();
                attrValue = (String) attrsMap.get (attrName.toLowerCase());
                System.err.print (attrName + " = " +
                                  (attrValue==null ? "NOVALUE!!" : attrValue));
	    }
	}
	return getAttributeCount ();
    }

    /***********************************************************************/
    /* Public information accessors                                        */
    /***********************************************************************/

    /**
     * Specifies that the attribute will appear in the tag as a flag: no
     * equals sign or value associated with it. If the attribute already
     * exists (case insensitive) its value will be replaced. This is
     * equivalent to {@link #putAttribute(String,String)} with {@link
     * #VALUELESS_ATTR} as the value.
     *
     * @param attrName named attribute which is stored by case, but compared
     * to existing tags case-insensitively
     * @see #putAttribute(String,String)
n     */
    public String putAttribute (String attrName) {
        return putAttribute (attrName, VALUELESS_ATTR);
    }

    /**
     * Add an attribute and assign it a value. If the attribute already exists
     * (case insensitively) then it's value is replaced with the one given.
     * Otherwise the attribute and it's value are added at the end of the
     * list.<p>
     *
     * If the value is <code>null</code> then the attribute is removed from
     * the tag. If the value is specified as {@link #VALUELESS_ATTR}, the
     * attribute will appear as a flag: no equals sign or value associated
     * with it.
     *
     * @param attrName named attribute which is stored by case, but compared
     * to existing tags case-insensitively
     * @param attrValue value to assign to attribute
     * @return previous value associated with specified attribute or null
     */
    public String putAttribute (String attrName, String attrValue) {
      if (tagType == Type.UNKNOWN || tagType == Type.CLOSE ||
          tagType == Type.DECLARATION || tagType == Type.COMMENT)
        throw new IllegalStateException
          ("Cannot add attributes to tags with type "+tagType);
      
      attrsText = null;
      return insertAttribute (attrName, attrValue);
    }

    /** Internal means which doesn't clear the attrsText */
    private String insertAttribute (String attrName, String attrValue) {
	/* System.err.println("DSDD: attrName = " + attrName + " attrValue = " + attrValue); */
        String attrLower = attrName.toLowerCase();
        String oldValue = (String) attrsMap.get (attrLower);
        if (attrValue == null) {
            attrsList.remove(attrName);
            attrsMap.remove(attrLower);
        } else {
            if (oldValue == null)
                attrsList.add(attrName);
            attrsMap.put(attrLower, attrValue);
        }
        return oldValue;
    }
  
    /**
     * Removes the specified attribute. This is equivalent to {@link
     * #putAttribute(String,String)} with the value specified as
     * <code>null</code>
     *
     * @return value of the attribute if it previously existed, or null
     */
    public String removeAttribute (String attrName) {
        return putAttribute (attrName, null);
    }
    
    /**
     * Return count of current attributes including mere 'flag' attributes.
     * @return count of attributes in tag
     */
    public int getAttributeCount () {
        return attrsList.size();
    }

  /**
   * Iterator over this tags attributes. This iterator supports the 'remove'
   * operation.
   */
  public Iterator attributeIterator() {
    return new Iterator() {
        private Object prev = null;
        private Iterator iter = attrsList.iterator();
        public boolean hasNext() { return iter.hasNext(); }
        public Object next() {
          prev = null;
          prev = iter.next();
          return prev;
        }
        public void remove() {
          if (prev == null)
            throw new IllegalStateException();
          String attrLower = ((String)prev).toLowerCase();
          iter.remove();
          attrsMap.remove(attrLower);
        }
      };
  }

    /**
     * Retrieve attributes current value, case-insensitively, {@link
     * #VALUELESS_ATTR} if it is merely a flag attribute, or <code>null</code>
     * if the attribute is not in the tag.
     *
     * @return attribute value, or <code>null</code> if attributeName not found.
     */
    public String getAttribute(String attributeName) {
	// System.err.println("DSD: "
	//    + attributeName + " --> " + attrsMap.get (attributeName.toLowerCase ()));
        return (String)attrsMap.get (attributeName.toLowerCase ());
    }

  public String toString () {
    StringBuffer sb = new StringBuffer();
    sb.append ("<");
    if (tagType == Type.COMMENT)
      sb.append (tagText);
    else {
      if (tagType == Type.CLOSE)
        sb.append ("/");
      sb.append(gidText);
      if (attrsText != null)
        sb.append (attrsText);
      else {
        Iterator iter = attrsList.iterator();
        while (iter.hasNext()) {
          String attr = (String)iter.next();
          if (attr == UNPARSED_ATTRS)
            break;
          sb.append(" ").append(attr);
          Object value = attrsMap.get(attr.toLowerCase());
          if (value != VALUELESS_ATTR)
            sb.append("=").append(value);
        }
      }
    }
    if (tagType == Type.EMPTY)
      sb.append ("/");
    sb.append(">");
    return sb.toString();
  }
  
    public String dumpString () {
      int len = (tagText==null?-1:tagText.length());
        StringBuffer s =
          new StringBuffer("[SgmlTag: "+tagType+" ["+tagCharOffset+
                           "] ("+len+
                           ") text='"+tagText+
                           "' gid='"+gidText+
                           "' attrs='"+attrsText+"']");
        if (attrsList.size()>0)
          s.append("\n\t++attrs = ");
        Iterator iter = attrsList.iterator();
        while (iter.hasNext()) {
          Object attr = iter.next();
          if (attr == UNPARSED_ATTRS)
            break;
          s.append(" ").append(attr);
          Object value = attrsMap.get(((String)attr).toLowerCase());
          if (value != VALUELESS_ATTR)
            s.append("=").append(value);
        }
        return s.toString();
    }

    /***********************************************************************/
    /* Type safe enumeration                                               */
    /***********************************************************************/

    public static final class Type {
        public static final Type UNKNOWN     = new Type ("UNKNOWN_TAG");
        public static final Type OPEN        = new Type ("OPEN_TAG");    /* <foo>  */
        public static final Type EMPTY       = new Type ("EMPTY_TAG");   /* <foo/> */
        public static final Type CLOSE       = new Type ("CLOSE_TAG");   /* </foo> */
        public static final Type DECLARATION = new Type ("DECLARATION"); /* <!DOCTYPE ...> or <?xml version...?> */
        public static final Type COMMENT     = new Type ("COMMENT");     /* <!-- <foo bar<> --> */

        private String type;
        private Type (String type) { this.type = type; }
        public String toString () { return type; }
    }
}

