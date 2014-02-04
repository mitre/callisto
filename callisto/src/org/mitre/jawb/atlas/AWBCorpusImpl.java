
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

package org.mitre.jawb.atlas;

import gov.nist.atlas.*;
import gov.nist.atlas.impl.CorpusImpl;
import gov.nist.atlas.spi.ImplementationDelegate;
import gov.nist.atlas.type.ATLASType;

import gov.nist.atlas.event.ValueChangeListener;
import gov.nist.atlas.event.ValueChangeEvent;

import java.net.URL;
import java.util.HashMap;

/**
 * This implementation of Corpus adds an index of Anchors by Location
 * to allow text-point anchors to be re-used if we need another anchor
 * to point to the same location.  This is maintained by a
 * ValueChangeListener on every Anchor Parameter with role "char".
 *
 * <b>Note: This implementation makes the simplifying assumption that
 * Anchors with a "char" parameter will have only that parameter, and
 * will thus be reusable whenever an Anchor with the same "char" value
 * is needed.</b> Future users of this implementation should avoid
 * using the "char" role name for an Anchor Parameter in any Anchor that
 * does not meet this criterion.
 * 
 */
public class AWBCorpusImpl extends CorpusImpl {
    protected AWBCorpusImpl(ATLASType type, Id id, ImplementationDelegate delegate, URL location) {
	super(type, id, delegate, location);
	// added index for awb:
	awbAnchorsByLocation = new HashMap();
    }

    /** 
     * This is specialized to add the ValueChangeListener to any
     * Anchor Parameter with the role "char".
     */  
    public boolean addAnchor(Anchor anchor) {
	boolean result = addToSubordinateSet(anchor);
	if (result) {	    
	    Parameter charParm = anchor.getParameterWithRole("char");
	    if (charParm != null) {
		AWBAnchorValueChangeListener listener = 
		    new AWBAnchorValueChangeListener();
	        charParm.addValueChangeListener(listener);
	    }
	}
	return result;
    }

    /** 
     * This is specialized to remove the Anchor from the anchors by
     * location index when it is removed from the corpus.
     */
    public boolean removeAnchor(Anchor anchor) {
      //    return remove(anchor, ATLASClass.ANCHOR);
      boolean status = removeFromSubordinateSet(anchor);
      if (status) {
        // must specify anchor's type name in HashMap lookup
        awbAnchorsByLocation.remove(anchor.getAnchorType().getName()+":"+anchor.getParameterWithRole("char").getValueAsString());
      }
      return status;
    }



    /**
     ********************************************************************
     *   jAWB specific code begins here
     ********************************************************************
     *
     * additonal instance variables:
     * these get initialized upon instantiation, and filled upon import
     * or as new elements are added to the corpus
     */
    
    protected HashMap awbAnchorsByLocation;
    
    
    /**
     * getAnchorByOffset
     * 
     * @return the anchor with the given offset, if it exists, or null if no such 
     * anchor exists
     */
    
    public Anchor getAnchorByOffset(String offset) {
      return getAnchorByOffset("text-point", offset);
    }
    
    /**
     * Gets an anchor at a given offset if one exists, but only if it has the
     * specified ATLAS type name.
     * 
     * @param anchorType ATLAS type name of anchor to return
     * @param offset character offset in document of anchor to return
     * @return the anchor with the given ATLAS type name at the given offset, if
     *         it exists, or <code>null</code> if no such anchor exists
     */
    public Anchor getAnchorByOffset(String anchorType, String offset) {
      return (Anchor)(awbAnchorsByLocation.get(anchorType+":"+offset));
    } 
}

/**
 * 
 * Whenever a Parameter with this listener has its value changed, the
 * Anchor that is its parent will be removed from the anchors by
 * location index for the old value, and re-added for the new value.
 */
class AWBAnchorValueChangeListener implements ValueChangeListener {

    public void valueChange(ValueChangeEvent event) {
	String oldVal = (String)event.getOldValue();
	String newVal = (String)event.getNewValue();
	Parameter parm = (Parameter)event.getSource();
	Anchor anchor = (Anchor)parm.getParent();
        // use anchorType as part of HashMap key
        String anchorType = anchor.getAnchorType().getName();
	AWBCorpusImpl corpus = (AWBCorpusImpl)anchor.getParent();
	corpus.awbAnchorsByLocation.remove(anchorType+":"+oldVal);
	corpus.awbAnchorsByLocation.put(anchorType+":"+newVal,anchor);
    }
}


