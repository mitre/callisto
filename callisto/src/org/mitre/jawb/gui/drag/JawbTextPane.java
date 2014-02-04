
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
 * Created on Feb 15, 2005
 */
package org.mitre.jawb.gui.drag;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.EventListener;
import java.util.Set;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.text.*;

import org.mitre.jawb.Jawb;
import org.mitre.jawb.gui.JawbComponent;
import org.mitre.jawb.gui.JawbDocument;
import org.mitre.jawb.prefs.Preferences;
import org.mitre.jawb.swing.FontSupport;


public class JawbTextPane extends JTextPane implements JawbComponent {
    public final static String PROP_FONT_FAMILY_LINKED = "font family linked";
    public final static String PROP_FONT_SIZE_LINKED = "font size linked";
    public final static String PROP_LINE_SPACING_LINKED = "line spacing linked";
    public final static String PROP_USE_TRANSLATION_PANE_FONT_FAMILY = "use translation pane font family";
    public final static String PROP_USE_TRANSLATION_PANE_FONT_SIZE = "use translation pane font size";
//  public final static String PROP_ = "";
    
    EventListenerList jawbDocHandlers = new EventListenerList();
    
    public interface JawbDocumentHandler extends EventListener {
        void jawbDocumentChanged(JawbDocument doc);
    }
    
    public void addJawbDocumentHandler(JawbDocumentHandler h) {
        jawbDocHandlers.add(JawbDocumentHandler.class, h);
    }
    
    public void removeJawbDocumentHandler(JawbDocumentHandler h) {
        jawbDocHandlers.remove(JawbDocumentHandler.class, h);
    }
    
    protected void fireJawbDocumentChanged() {
        // Guaranteed to return a non-null array
        Object[] listeners = jawbDocHandlers.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==JawbDocumentHandler.class) {
//              // Lazily create the event:
//              if (fooEvent == null)
//              fooEvent = new FooEvent(this);
                ((JawbDocumentHandler)listeners[i+1]).jawbDocumentChanged(jawbDocument);
            }
        }
    }
    
    /**
     * Style to set the paragraph style (line height) all at once.
     */
    protected Style paraStyle;
    /**
     * Style to set the alignment of the whole document at once. Anonymous (has
     * no name) so that it's name never overrides the name of the highlight
     * `style. Anonymous style's can not be looked up again, so we must keep
     * reference with this field.
     */
    protected Style alignmentStyle;
    /**
     * Style to set the font family all at once.
     */
    protected Style familyStyle;
    /**
     * Style to set the list of annotations covering an element of text.
     */
    protected Style defaultStyle;
    
    private ComponentOrientation orientation;
    
    public JawbTextPane() {
        super();
        init();
    }
    
//    public JawbTextPane(StyledDocument doc) {
//        super(doc);
//        init();
//    }
    
    protected void init() {
        paraStyle = addStyle (null, null);
        alignmentStyle = addStyle (null, paraStyle);
        familyStyle = addStyle (null, alignmentStyle);
        updateStylesLater();
        Preferences prefs = Jawb.getPreferences();
        propertyChangeListener = createPropertyChangeListener();
        prefs.addPropertyChangeListener(Preferences.PARA_LAST_LINE_SPACING_KEY, propertyChangeListener);
        prefs.addPropertyChangeListener(Preferences.FONTS_LAST_FAMILY_KEY, propertyChangeListener);
        prefs.addPropertyChangeListener(Preferences.FONTS_LAST_SIZE_KEY, propertyChangeListener);
//      prefs.addPropertyChangeListener(, l);
    }

    protected void updateStylesLater() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateStyles();                    
            }
        });
    }

    protected PropertyChangeListener createPropertyChangeListener() {
        return new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        updateStyles();
                    }
                };
    }

    protected EditorKit createDefaultEditorKit() {
        return new JawbTextPaneEditorKit();
    }
    protected static class JawbTextPaneEditorKit extends StyledEditorKit {
        public Document createDefaultDocument() {
            return new JawbTextPaneDoc();
        }
    }
    
    protected static class JawbTextPaneDoc extends DefaultStyledDocument {
        public Position[] appendText(String text, AttributeSet a)
        throws BadLocationException {
            Position[] p = new Position[] { null, null };
            writeLock();
            int offs = -2;
            try {
                offs = Math.max(getEndPosition().getOffset() - 1, 0); //Math.max(getLength() - 1, 0);
                super.insertString(offs, text, a);
                p[0] = createPosition(offs);
                p[1] = createPosition(getEndPosition().getOffset()); //offs+text.length());
System.err.println("appendText: offs: "+offs+", p0: "+p[0].getOffset()+", p1: "+p[1].getOffset()+"len: "+text.length()+"\n\t"+text);                
            }
            catch (BadLocationException e) {
                System.err.println("Exception in appendText at "+offs+" ("+e.offsetRequested()+") in thread "+Thread.currentThread());
                throw e;
            }
            finally {
                writeUnlock();
            }
            return p;
        }
    }
    
    protected JawbDocument jawbDocument;
    protected PropertyChangeListener propertyChangeListener;
    protected boolean familyLinked;
    protected boolean sizeLinked;
    
    public Set getSelectedAnnots() {
        return Collections.EMPTY_SET;
    }
    
    public Component getComponent() {
        return this;
    }
    
    public JawbDocument getJawbDocument() {
        return jawbDocument;
    }
    
    public void setJawbDocument(JawbDocument jawbDocument) {
        this.jawbDocument = jawbDocument;
        if (jawbDocument == null)
            setStyledDocument((StyledDocument) getStyledEditorKit()
                    .createDefaultDocument());
        else {
            updateStyles();
        }
        fireJawbDocumentChanged();
    }
    
    public void insertText(int offs, String text) {
        DefaultStyledDocument doc = (DefaultStyledDocument) getStyledDocument();
        try {
            doc.insertString(offs, text, familyStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    public void replaceText(int offs, String text) {
        replaceText(offs, text.length(), text);
    }
    public void replaceText(int offs, int length, String text) {
        DefaultStyledDocument doc = (DefaultStyledDocument) getStyledDocument();
        try {
            doc.replace(offs, length, text, familyStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    public Position[] appendText(String text) {
        JawbTextPaneDoc doc = (JawbTextPaneDoc) getStyledDocument();
        int len = doc.getLength();
        try {
//            new Exception(Thread.currentThread()+": ("+len+")"+text).printStackTrace();
            return doc.appendText(text, familyStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void removeText(int offs, int len) {
        DefaultStyledDocument doc = (DefaultStyledDocument) getStyledDocument();
        try {
            doc.remove(offs, len);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    public void removeText(int offs) {
        DefaultStyledDocument doc = (DefaultStyledDocument) getStyledDocument();
        try {
            doc.remove(offs, doc.getLength() - offs);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    public int getLength() {
        DefaultStyledDocument doc = (DefaultStyledDocument) getStyledDocument();
        return doc.getLength();
    }
    
    protected void updateStyles() {
        autoDetectOrientation();
        Preferences prefs = Jawb.getPreferences();
        familyLinked = isFamilyLinked(prefs);
        sizeLinked = isFontSizeLinked(prefs);
        String family = null;
        int size = -1;
        // set style based on last settings if desired
        if (isLineSpacingLinked()
//                && prefs.getBoolean (Preferences.PARA_LAST_LINE_SPACING_ENABLED_KEY)
                ) {
            float spacing = StyleConstants.getLineSpacing (paraStyle);
            spacing = prefs.getFloat (Preferences.PARA_LAST_LINE_SPACING_KEY, spacing);
            StyleConstants.setLineSpacing (paraStyle, spacing);
        }
        if (familyLinked) {
            if (true || prefs.getBoolean (Preferences.FONTS_LAST_FAMILY_ENABLED_KEY)) {
                family = StyleConstants.getFontFamily (familyStyle);
                family = prefs.getPreference (Preferences.FONTS_LAST_FAMILY_KEY, family);
            }
            if (/*family == null && */jawbDocument != null) {
                family = jawbDocument.getFontFamily();
            }
        }
        if (sizeLinked) {
            if (true || prefs.getBoolean (Preferences.FONTS_LAST_SIZE_ENABLED_KEY)) {
                size = StyleConstants.getFontSize (familyStyle);
                size = prefs.getInteger (Preferences.FONTS_LAST_SIZE_KEY, size);
            }
            if (/*size == -1 &&*/ jawbDocument != null) {
                size = jawbDocument.getFontSize();
            }
        }
        if (family != null) {
            setFontFamily(family);
        }
        if (size != -1) {
            setFontSize(size);
        }
    }

    protected boolean isLineSpacingLinked() {
        return Boolean.valueOf((""+getClientProperty((PROP_LINE_SPACING_LINKED))).toString()).booleanValue();
    }

    protected boolean isFontSizeLinked(Preferences prefs) {
        boolean sizeLinked =
            Boolean.valueOf((""+getClientProperty((PROP_FONT_SIZE_LINKED))).toString()).booleanValue();
        return sizeLinked;
    }

    protected boolean isFamilyLinked(Preferences prefs) {
        boolean familyLinked =
            Boolean.valueOf((""+getClientProperty((PROP_FONT_FAMILY_LINKED))).toString()).booleanValue();
        return familyLinked;
    }
    
    public StyledDocument getUpdatableStyledDocument() {
        return getStyledDocument();
    }
    
    /** Set the Font family all the text in the document */
    public String getFontFamily () {
        return StyleConstants.getFontFamily (familyStyle);
    }
    
    /**
     * Set the font family for the text of this document. Widgets which use the
     * same font (not applicable to JTextComponents using the same
     * StyledDocument) will want to listen for changes to this bound property.
     */
    protected void setFontFamily (String family) {
        Object old = StyleConstants.getFontFamily (familyStyle);
        
        StyleConstants.setFontFamily (familyStyle, family);
        StyledDocument doc = getUpdatableStyledDocument();
        if (doc != null)
            doc.setCharacterAttributes (0, getLength(),
                    familyStyle, false);
//      support.firePropertyChange(JawbDocument.FONT_FAMILY_PROPERTY_KEY, old, family);
    }
    
    /** Set the Font family all the text in the document */
    public int getFontSize () {
        return StyleConstants.getFontSize (familyStyle);
    }
    
    /**
     * Set the font family for the text of this document. Widgets which use the
     * same font (not applicable to JTextComponents using the same
     * StyledDocument) will want to listen for changes to this bound property.
     */
    protected void setFontSize (int size) {
        int old = StyleConstants.getFontSize (familyStyle);
        
        StyleConstants.setFontSize (familyStyle, size);
        StyledDocument doc = getUpdatableStyledDocument();
        if (doc != null)
            doc.setCharacterAttributes (0, getLength(),
                familyStyle, false);
//      support.firePropertyChange(JawbDocument.FONT_SIZE_PROPERTY_KEY, old, size);
    }
    
    /** Set the Font family all the text in the document */
    public float getLineSpacing () {
        return StyleConstants.getLineSpacing (paraStyle);
    }
    
    /**
     * Set the font family for the text of this document. Widgets which use the
     * same font (not applicable to JTextComponents using the same
     * StyledDocument) will want to listen for changes to this bound property.
     */
    public void setLineSpacing (float spacing) {
        // not propogating a change event just yet
        //float old = StyleConstants.getLineSpacing (paraStyle);
        
        StyleConstants.setLineSpacing (paraStyle, spacing);
        getStyledDocument().setParagraphAttributes (0, getLength(),
                paraStyle, false);
    }
    
    public ComponentOrientation getComponentOrientation () {
        if (orientation == null) {
            return super.getComponentOrientation();
        }
        return orientation;
    }
    
    public void setComponentOrientation (ComponentOrientation o) {
        int alignment = StyleConstants.ALIGN_LEFT;
        if (o == ComponentOrientation.RIGHT_TO_LEFT)
            alignment = StyleConstants.ALIGN_RIGHT;
        
        StyleConstants.setAlignment(alignmentStyle, alignment);
        getStyledDocument().setParagraphAttributes (0, getLength (),
                alignmentStyle, false);
        Object old = orientation;
        orientation = o;
//      support.firePropertyChange(JawbDocument.ORIENTATION_PROPERTY_KEY, old, orientation);
    }
    
    public void autoDetectOrientation () {
        try {
            String family =
                autoDetect (new FontSupport(getText(0, getLength())));
        } catch (BadLocationException impossible) {
            throw new RuntimeException (impossible);
        }
    }
    
    /** Does the work of autodetecting the font, and orientation, but returns
     * the Font Family name used
     */
    private String autoDetect (FontSupport fs) {
        Font font = new Font (null, Font.PLAIN, 1);
        String family = "Default";
        if (fs.getCoverageBy ((String)null) < .97) {
//          if (DEBUG > 0)
//          System.err.print (" got="+fs.getCoverageBy ((String)null)+"   Searching: ");
            FontSupport.FontScore fscore = fs.rankFamilies ()[0];
            family = fscore.getFamily ();
        }
//      if (DEBUG > 0)
//      System.err.println ("   Accepted: "+family);
        setComponentOrientation (fs.getOrientation ());
        return family;
    }
    
}