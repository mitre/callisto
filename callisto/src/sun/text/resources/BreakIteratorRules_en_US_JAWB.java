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


package sun.text.resources;

import java.text.*;
import java.util.*;

/**
 * This ResourceBundle implements a slightly relaxed definition of a
 * WordBreakIterator. Specifically, word strings may not be separated by
 * various punctuation, which for this resource bundle are separate words.
 * All other iterators are the same as default. Requires java 1.4<p>
 *
 * In order to get a JTextComponent to use this word iterator, set the locale
 * of the component to en.US.JAWB:<p>
 * 
 * <code>target.setLocale (new Locale ("en","US","JAWB"));</code><p>
 *
 * After that word selection methods will use this instead of the java
 * defaults. This is dynamic, and can be changed on the fly.
 *
 * WARNING: This class makes use of functionality provided by package-private
 * classes of the Java Development Kit.  Because there is no guarantee the
 * implementation of those classes will remain consistent over time, this
 * class may fail with future versions.  This class works as of Java 2
 * v1.4.<p>
 *
 * A safe use of this would create a separate BreakIterator for the word
 * boundaries, and call that specifically from the classes that need the
 * special functionality, rather than messing with the local of a component.
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */
public class BreakIteratorRules_en_US_JAWB extends ListResourceBundle {

  public Object[][] getContents() {
    return contents;
  }
  private static Object[][] contents;
  
  static {
    String rbi = "RuleBasedBreakIterator";
    String orig = "sun.text.resources.BreakIteratorRules";
    String rules;

    contents = new Object[5][];
    contents[0] = new Object[] {"BreakIteratorClasses",
                                new String[] {rbi, rbi, rbi, rbi}};
    rules = "CharacterBreakRules";
    contents[1] = new Object[] {rules,
                                ResourceBundle.getBundle(orig).getString(rules)};
    rules = "SentenceBreakRules";
    contents[2] = new Object[] {rules,
                                ResourceBundle.getBundle(orig).getString(rules)};
    rules = "LineBreakRules";
    contents[3] = new Object[] {rules,
                                ResourceBundle.getBundle(orig).getString(rules)};
    rules = "CharacterBreakRules";
    contents[4] = new Object[] {"WordBreakRules",
        "<ignore>=[:Cf:];<enclosing>=[:Mn::Me:];<danda>=[\u0964\u0965];<kanji>=[\u3005\u4e00-\u9fa5\uf900-\ufa2d];<kata>=[\u30a1-\u30fa\u30fd\u30fe];<hira>=[\u3041-\u3094\u309d\u309e];<cjk-diacrit>=[\u3099-\u309c\u30fb\u30fc];<letter-base>=[:L::Mc:^[<kanji><kata><hira><cjk-diacrit>]];<let>=(<letter-base><enclosing>*);<digit-base>=[:N:];<dgt>=(<digit-base><enclosing>*);<mid-word>=[:Pd::Pc:\u00ad\u2027\\\"\\'\\.];<mid-num>=[\\\"\\'\\,\u066b\\.];<pre-num>=[:Sc:\\#\\.^\u00a2];<post-num>=[\\%\\&\u00a2\u066a\u2030\u2031];<ls>=[\n\u000c\u2028\u2029];<ws-base>=[:Zs:\u0009];<ws>=(<ws-base><enclosing>*);<word>=((<let><let>*){<danda>});<number>=(<dgt><dgt>*(<mid-num><dgt><dgt>*)*);.;{<word>}(<number><word>)*{<number>{<post-num>}};<pre-num>(<number><word>)*{<number>{<post-num>}};<ws>*{\r}{<ls>};[<kata><cjk-diacrit>]*;[<hira><cjk-diacrit>]*;<kanji>*;<base>=[^<enclosing>^[:Cc::Cf::Zl::Zp:]];<base><enclosing><enclosing>*;"};
    // ORIGINAL: "<ignore>=[:Cf:];<enclosing>=[:Mn::Me:];<danda>=[\u0964\u0965];<kanji>=[\u3005\u4e00-\u9fa5\uf900-\ufa2d];<kata>=[\u30a1-\u30fa\u30fd\u30fe];<hira>=[\u3041-\u3094\u309d\u309e];<cjk-diacrit>=[\u3099-\u309c\u30fb\u30fc];<letter-base>=[:L::Mc:^[<kanji><kata><hira><cjk-diacrit>]];<let>=(<letter-base><enclosing>*);<digit-base>=[:N:];<dgt>=(<digit-base><enclosing>*);<mid-word>=[:Pd::Pc:\u00ad\u2027\\\"\\'\\.];<mid-num>=[\\\"\\'\\,\u066b\\.];<pre-num>=[:Sc:\\#\\.^\u00a2];<post-num>=[\\%\\&\u00a2\u066a\u2030\u2031];<ls>=[\n\u000c\u2028\u2029];<ws-base>=[:Zs:\u0009];<ws>=(<ws-base><enclosing>*);<word>=((<let><let>*(<mid-word><let><let>*)*){<danda>});<number>=(<dgt><dgt>*(<mid-num><dgt><dgt>*)*);.;{<word>}(<number><word>)*{<number>{<post-num>}};<pre-num>(<number><word>)*{<number>{<post-num>}};<ws>*{\r}{<ls>};[<kata><cjk-diacrit>]*;[<hira><cjk-diacrit>]*;<kanji>*;<base>=[^<enclosing>^[:Cc::Cf::Zl::Zp:]];<base><enclosing><enclosing>*;" },
  }
}
