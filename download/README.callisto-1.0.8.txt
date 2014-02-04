                     ----------------------------
                     The Callisto Annotation Tool
                     ----------------------------

                   (c) The MITRE Corporation, 2003

                   Installation and User Directions

                            Version 1.0.8
                            Sept 10, 2003


Overview.

The Callisto annotation tool was developed to support linguistic
annotation of textual sources for any Unicode-supported language.  The
initial development of the tool by the MITRE Corporation was funded by
the U. S. government.  The primary development team consisted of Chad
McHenry, Robyn Kozierok, and Laurel Riek, with contributions from
David Day.  Design and interface guidance was provided by Lisa Ferro,
Janet Hitzeman, Marcia Lazo, Marc Vilain and David Day.  The project
leader for this effort was David Day.

----------------------------------------------------------------

Contents.

I.  Installing and Running Callisto
    A. Java 1.4 or later
    B. Installing Callisto
    C. Running Callisto
    D. Installing Tasks Modules
    E. Language Support

II. Using Callisto
    A. Callisto Features
       1. Main Window
       2. Annotating Text
       3. Selected vs. Swiped vs. Highlighted
       4. Selecting and Deselecting
       5. Changing Tag Extents
       6. Overlapping Tags and the Inspection Panel
       7. Word vs. Character swiping
       8. Context Menus
       9. Text Palette and Default Action
      10. Tear off Tabs
      11. Font Size
      12. Foreign Texts
      13. User Preferences
      14. Annotation Highlights and Task Preferences
   B. Ace Annotation Task Tutorial
       1. Opening files for annotation
       2. Saving annotations
       3. The Callisto Interface for ACE
       4. Sample ACE data
   C. Known Bugs and Limitations
       1. Saving annotations to APF.

III. Feedback and Support

IV. Copyright and License

V. About the name "Callisto"

----------------------------------------------------------------

I. Installing Callisto.

Callisto is implemented in Java.  To run Callisto on any platform,
install Java 2 Runtime Environment 1.4 or later.  This can be obtained
for free from http://java.sun.com/ for all major computing platforms.

Callisto is distributed as a single compressed archive containing all
the necissary Java archive files (names ending in '.jar'). Additional
task modules may be distributed with the main archive, or as separate
compressed archives.

Callisto may be installed anywhere on your computer you choose. We
refer to that directory as the $(CALLISTO) directory.

Tasks may be distributed with the main archive (in a subdirectory
"tasks"), or as separate compressed archives. Tasks are separate jar
files which Callisto will automatically load. Callisto will recognize
valid task files in the installation "tasks" folder (a directory named
"tasks" immidiately next to the Callisto.jar file), or in your
personal "tasks" folder ($(HOME)/.callisto/tasks).

Here is how you can install and run the compiled version of
Callisto on various platforms:

A. Install java 2 Runtime Environment version 1.4 or later, available
   from <http://java.sun.com>.

B. Uncompress the main archive to some location on your machine. The
   top level .jar files must be kept together but may be placed
   anywhere. We'll refer to this as the $(CALLISTO) directory.

C. To run Callisto, Execute the "$(CALLISTO)/Callisto.jar" file.

   1. Windows 95, 98, ME, 2000, XP, MacOSX:

      Double-click the Callisto.jar file.  (You can also run Callisto
      from the Windows or Mac command line as described for Sun/Linux,
      below.)

   2. Sun Solaris, Linux:

      At a shell prompt, cd into the $(CALLISTO) directory run java,
      invoking the Callisto application, like this:

        $ java -jar Callisto.jar

D. Install a task module by placing it's .jar file in the appropriate
   "tasks" directory and it will be available automatically when
   Callisto starts up.

   There are two tasks directories "installation" and "personal". The
   installation tasks directory is named "tasks" in the $(CALLISTO)
   directory.

        $(CALLISTO)/Callisto.jar
        $(CALLISTO)/tasks/MyTask.jar

   The personal "tasks" directory is in "$(HOME)/.callisto/tasks" and
   is in a different location depending on your computers architecture.

   Personal task directory are "$(HOME)/.callisto/tasks", where $(HOME)
   on various systems is:

   Windows 95, 98, ME      These systems consider "C:\" the $(HOME)

   Windows 2000, XP        Each user has a personal directory in
                           "C:\Documents and Settings\$(USERNAME)".
   Solaris, Linux, MacOSX: These system simply use the $HOME variable
                           in your environment. Mac users cannot see
                           ".callisto" using the Finder, and must use
                           the command line.

   Tasks in the personal tasks directory will supersede those in the
   installation tasks directory if they have the same identifier (Each
   task has a hard coded ID which is independant of the name of the
   .jar file).

   Most users will find it easiest to install tasks to the
   installation tasks folder, though where an installation is shared,
   individuals can choose to test new versions of Tasks by using the
   personal task directory.

E. Language Support

   Java, the language in which Callisto has been written, can
   render any UTF-8 file for which your computer has the
   appropriate fonts.  Java 1.4 provides default Fonts for English,
   most European languages, and Arabic.  If you are having trouble
   viewing texts in the appropriate font, try first to view this same
   file from your web browser, setting the character encoding as
   appropriate.  If this succeeds and you are still having trouble
   getting Callisto/Java to render the text file as appropriate,
   please contact us (see section III, below).

   Not all languages are supported by the default font.  Callisto will
   automatically attempt to recognize most CJK documents, but will not
   do an exhaustive search of all fonts.  In the "Format" menu, select
   "Format->Font->Auto-Detect Font" to search all fonts for the one
   with the best support of the current document. If this is still not
   appropriate, you can manually select the font of your choice from
   the "Format->Font" menu.

   Note for users of Windows 95, 98, 2000:

     The following is from the microsoft web site:
     http://office.microsoft.com/assistance/2000/OSetUpForLangs.aspx

     Multilanguage system support in Windows 95, Windows 98, or
     Windows 2000

       1. On the Windows Start menu, point to Settings, and then click
          Control Panel.
       2. Do one of the following:
        - In Windows 95 or Windows 98, double-click the Add/Remove
          Programs icon, and then click the Windows Setup tab.
        - In Windows 2000, double-click the Regional Options icon, and
          then click the General tab.
       3. Do one of the following:
        - In Windows 95 or Windows 98, click Multilanguage Support in
          the Components list, and then click Details. Then select the
          check boxes next to the language you want to use.
        - In Windows 2000, select the check box next to the language
          group you want under Language settings for the system.
       4. Repeat step 3 for each language you want to use.


II. Using Callisto.

Callisto represents all annotations using the Atlas formalism, and
Callisto has been developed on top of the NIST implementation of this
formalis (see http://www.nist.gov/speech/atlas/ for more information
on jATLAS and Atlas).  

The Callisto architecture is designed to facilitate the customization
of the tool for multiple tasks by inserting "task modules." The
designers of these task modules will be free to tailor both the
interface components and the types of data formats to which the
annotations are stored.  Callisto ships with one task predefined: the
ACE annotation task incorporating mentions, entities, relation
mentions and relation entities (see
http://www.nist.gov/speech/tests/ace/index.htm for more information on
ACE). If you are interested in having Callisto customized for a
particular annotation task, please contact us (see section III for
contact information).

A. Callisto Features

  NOTE: Callisto is still under active development, and although basic
  functionality is fairly well defined, expect future versions to
  evolve.  While every effort will be made to keep documentation up to
  date, please bear with our discrepancies.  When in doubt, feel free
  to contact MITRE with questions.

1. Main Window.

Callisto's main window is broken into two parts, the
'Main Text Pane' on top, and the 'Editor Pane' on bottom.  The editor
pane is generally customized for the annotation task at hand, while
the main text pane is expected to remain fairly constant. The text in
the main text pane cannot be edited, only annotated.

2. Annotating Text.

To annotate text, swipe the text of interest, and
click the desired 'New annotation' action from the context menu or by
using the text palette/default action (See below). The new annotation
will become the single selected annotation (see Selecting and
De-selecting below).

3. Selected/Swiped Text vs. Selected Annotations.

Callisto provides visual indicators of annotations on regions of text
(by changing the associated background color).  To create a new
annotation on a given portion of text the user must "swipe" or
"select" the desired region of text.  This selection of text *also*
provides a visual feedback to the user.  Both of these kinds of visual
feedback we refer to generally as "highlighting."  The user of
Callisto needs to understand the distinction between selected/swiped
text and selected annotations.

A 'selected annotation' will appear in the text with black/white
highlights above and below.  'Swiped text' or 'selected text' is what
most editors and browsers refer to as 'selected' text, created by
clicking mouse-button-1 (usually the left button) and dragging over
some text to 'highlight' it.  When we use the term 'highlight' we
generally use it to refer to text which has either of these kinds of
non-standard coloring, such as visible annotations or swiped
text. 'Highlight' is an ambiguous word in Callisto, and we try to use
it within context. To 'de-swipe' or 'de-select' text, simply click in
an area of the text pane with no text, or press the escape key (while
the main text pane is active).  Different actions are available at
different times depending on what annotations are selected, and
whether or not text is swiped.  This is reflected by changes in the
context menus and Text Palette (see below).

4. Selecting and Deselecting.

More than one annotation can be selected at a time. An annotation
automatically becomes the single selected annotation immediately after
it has been created from text. Select a different annotation by
clicking the left mouse-button over it.  Additional annotations can be
selected by holding the SHIFT key down while clicking other text
annotations with the left button.  To de-select annotations press
escape (you need to do this twice if text is also swiped) or simply
select a single other annotation.

5. Changing Tag Extents.

When text annotations are created, all of its extents are set to the
same values.  As in the case with ACE EDT Mentions, some text
annotations can have multiple extents.  To change an extent of an
annotation, simply select the annotation to change, swipe the region
of text you wish the extent to be, and click the appropriate 'Modify'
action in the text palette, or context menu (see below).  If there is
only one extent (as in RDC Timex) there will be only one Modifier
option.  For multiple extent tags (such as EDT Mentions) there will be
one modifier for each extent.  Note that there may be special handling
operations associated with such actions.  For example, for EDT
Mentions changing the head extent will always be associated with a
check that the new head extent is contained within the full extent,
even if this requires changing the full extent at the same time.

6. Overlapping Tags and the Inspection Panel.

When annotations overlap, it can be very hard to tell what annotations
exist on that part of the text.  The annotations in the text panel can
be inspected by moving the mouse cursor over an annotation.  At the
bottom of the main text a gray 'panel' will display a text
representation of whatever annotations are under the mouse.  When more
than one annotation exists at a point, each will each appear on a
separate line.  To bring an annotation to the 'front', from behind,
press the tab key, while the mouse is over the overlapping tags in
question.

7.  Word vs. Character swiping.

Different languages can have vastly different glyphs and concepts of
tokenization.  To accommodate, Callisto allows text to be swiped in
units of characters or words.  The word segmenter is a modified
version of the standard Java WordBreakIterator which breaks hyphenated
words into multiple tokens.  Change the Swipe style (either Word or
Character) from the Callisto MenuBar: "Edit->Text Swipe Mode->..."

8.  Context Menus.

Right-click on items will often bring up a specialized pop-up menu of
actions available for the item under the cursor.  Swiped areas of
text, selected annotations, and rows in tables, all have context
menus.  The main text pane's context menu duplicates the actions
provided in the Text Palette (see below), providing a separate means
of working, depending on the annotator's preferred style.

9. Available Actions and Default Action.

The 'Avalable Actions' tool is a floating window that shows the
actions available to the main text pane, as buttons.  The 'default
action' is activated by clicking the middle mouse button in the main
text pane.  Buttons in the text palette duplicate the text pane's
context pop-up menu (see above), with a small round toggle button next
to each, indicating which is currently the default.  Actions will
become disabled depending on what the annotator is doing at any given
time (see Selected vs. Swiped above), and if the default action
becomes disabled, clicking the middle mouse button will have no
effect.

10. Tear off Tabs.

Some annotation tasks use multiple tables in tear off tabs.  These
tabs can 'float' separate from the main window.  To tear off a tab,
click the 'floating window' icon in the tab you wish to tear off.
Floating windows are automatically reattached by closing them.  Floating
windows remember their positions between annotation sessions.

11. Font Size.

The user can quickly change the font size in the main text pane from
the Callisto MenuBar "Format->Font Size->..."

12. Unicode Texts.

Java has the ability to display most languages of the world which have
character sets in the Unicode standard.  You must however have fonts
installed on your computer to view these other texts (see the Install
section above).  Callisto takes advantage of this by choosing the most
appropriate font for the text being annotated.  Currently Callisto can
only annotate files with UTF-8 or US-ASCII text (which is a proper
subset of UTF-8), although we intend to handle conversions from other
character sets in the future.  If Callisto fails to recognize a font
which should be rendered Right-to-Left, you can quickly change the
orientation by selecting "Right-to-Left Orientation" in the Callisto
"Format" MenuBar.

13. User Preferences.

The user can set various user preferences in the 'Preferences Dialog'
which appears by selecting "Edit->Preferences..."  in the Callisto
MenuBar.

14. Annotation Highlights and Task Preferences.

Each task defines it's own preferences and annotation highlight
colors.  These can also be edited in the preferences dialog.  In the
lower portion of the preference tree you see an item for each task
known to Callisto.  Each task's editor panel includes a table to edit
the highlight colors it knows of.


B.  ACE Annotation Task Tutorial

1.  Opening files for annotation.

There are three ways to begin annotating a file:

  1) File->New will create a new annotation file for a raw data file
  3) File->Open will open an AIF file previously created by Callisto.
  2) File->Import will use a Task specific mechanism to convert from some
     other data type to the task specified

Currently, there is one importer built into Callisto: the RDC Task
defines a single Importer for the ACE Pilot Format (APF),

All data annotated by this version of Callisto are saved in AIF
format.  Tasks may also define Exporters, which would convert to an
external format of the Tasks choosing.

Note that when loading an APF file, the "signal" file (or
"source document") annotated by the APF stand off annotations will be
a separate file.  This source document file annotated by the APF is
indicated within the APF itself and must (obviously) be present in
order to perform annotation.  The APF2AIF conversion process that
happens automatically when loading an APF file is able to find the
name of this source document file.  Since the source document file is
specified without any directory prefixes, it is assumed and required
that the source document is located within the same directory as the
APF file being opened.  The APF2AIF conversion process then creates a
raw text file (called $(source-document).txt, where $(source-document)
is the name of the source document annotated by the APF, *not* the APF
file itself).

In order to load annotations created by an earlier session with
Callisto, one needs to load the AIF file associated with those earlier
annotations.  Thus, even if one specified a raw text file "foo.txt" as
input to an earlier Callisto annotation session, all the annotations
associated with that session will be saved in an AIF file (for
example, "foo.txt.aif").  To see and update these earlier annotations
one must open the AIF file ("foo.txt.aif").  Since AIF, like APF, is a
stand off XML annotation format, there re mechanisms within the AIF
file that refer to the original raw text file (referred to in the
ATLAS annotation model as the "signal"), so Callisto will be able to
retrieve this original text file in order to properly display it along
with the annotations on that file.  (See the bug/limitations notes
below on moving or transporting AIF files.)

To make it easy to revert to earlier versions of a set of annotations,
Callisto generates backup copies of the annotations files that are
saved separately from the primary named annotation file.  The format
of these backup files is *.aif.$(integer).xml, where $(integer) takes on
ever increasing values as new versions are saved to disk.  The user
can select in the preferences window how many previous versions are to
be maintained at any given time.

2.  Saving annotations.

To save your annotation, you must choose a file name which will be
recognized as AIF, having an extension as listed above (*.aif,
*.aif.xml, *.aif.*.xml).  Once saved, do not move the signal file.
Callisto uses standoff annotation, and enforces a 'no change to the
signal' policy that includes the signal's location.  If the signal
file is moved in any way, (even if the annotation file moves with it)
you will no longer be able to reload the annotations (see Known Bugs
and Limitations #1 below, for more information).  This restriction can
be surmounted by carefully editing the AIF file itself.  If you need
to do this, please contact MITRE.

3.  The Callisto Interface for ACE

Upon opening a file for ACE annotation in Callisto, one is presented
with a main text screen in which the raw text of the original file is
displayed.  At the bottom of the main window are a number of distinct
"sheets" that can be displayed in the same space by selecting the
appropriate "tab" indicating their contents: Mention, Entities,
Relations.  These tables are used to display the various kinds of ACE
annotations.  A useful layout is to "tear off" the Entity and
Relations tables to enable them to be placed in a separate location on
the user's desktop.  This can be done by clicking on the icon (looks
like two overlapping rectangles) immediately to the left of the tab
labels.  Another useful "default" layout selection is to select (check
the box) of the "Text Palette" option underneath the "Edit" pull down
menu.

Text in the main text viewing window can be selected by swiping the
mouse while depressing the left button.  The default behavior is to
have the tool itself automatically select words at what it deems are
word boundaries.  If this default behavior interferes with sub-word
selections, the user can change this preference from the "Selection
Mode" option underneath the "Edit" pull down menu (changing it to
Character mode).  Selecting text does not itself create a mention
annotation.  A mention annotation is created when text has been
selected and then either (a) the right button is selected and an
appropriate action is selected from the pop-up menu, (b) the desired
action is selected directly from the "Text Palette" widget, or (c) the
user presses the middle button, in which case the default action
associated with the "Text Palette" widget (indicated by the black
circle button in the right hand column) is taken.  It will often be
useful to have the default action be "New Mention", which will enable
the annotator to only rarely be forced to use either the text-centered
pop-up menu of actions or the text palette actions.

Mention annotations can be selected by clicking on them (by default
the most recently created mention is the current selection).   When
selected, a small line on top and below the annotated text appears to
indicate that it is the selection mention.   If a given bit of text has
multiple mention annotations, the user can place the mouse directly
over the multiply-annotated text and hit tab to "cycle" through the
various co-extensive annotations.

Creating a mention creates two phrases at once that are linked
together: the "full" extent of the mention, and the "head" of the
mention.   A new entry in the Mentions table is created in which the
various attributes of the mention are displayed explicitly.   By
default the head and full extent of a mention are co-extensive.   To
change the extent of either the head (the usual case when a
modification is necessary) or the full extent, one swipes the desired
text to create a new selection, then either (a) clicks the right
button and selects the "Modify Head Extent" entry of the pop-up menu,
(b) selects the same entry from the free-standing "Text Palette," or
(c) clicks the middle button if it so happens that the user has
selected "Modify Head Extent" as the default action in the Text
Palette window (indicated by the black circle to its left).
Attributes of mentions besides their textual extent can be modified
directly from within the mentions table.

To create an EDT entity, one selects a row in the mention table or
selects a mention annotation from within the text window pane, and
then clicks right to get the pop-up window.   This will present "Add to
Entity" and "Create new Entity" as two options.   If one selects "Add
to Entity", the tool expects the user to immediately follow that
selection with a left mouse click on the row in the entity table that
indicates the entity to which the mention should be attached.   A
similar action, but with a different action selected, allows one to
add a selected mention to a relation (either as an Arg1 or Arg2
filler.   The other attributes of entities and relations are set just
as in the mentions table --- these values are all drop-menus that are
set by clicking on the cell and selecting from the pop-up menu.

4. Sample ACE data.

Some sample ACE data is provided in the CallistoRoot/callisto/data/ACE/
directory.


C.  Known Bugs and Limitations

As with most software products there are bugs and limitations that
have eluded developers and testers alike. Time constraints also play a
factor in the decision to ship with known issues. Callisto is still
uder heavy development, and your feedback is welcome.  Here are a
couple of the glaring problems you should be aware of.

1. Saving annotations to APF.

In this version of Callisto, annotations can only be saved and
retained persistently in the Atlas Interchange Format (AIF) files,
though annotations can be imported from both AIF and APF (ACE Pilot
Format) file formats.  This version of Callisto can annotate files
that include SGML as well, but the SGML will not be converted to Atlas
annotations viewable by the user.  These limitations will change soon.
In future versions of Callisto, there will be support for generating
APF (ACE Pilot Format) as well.  Additional formats to be incorporated
in Callisto include: in-line XML (for certain kinds of phrase-level
tagging); relational databases to support very large collections of
corpus-wide annotations; simplified AIF for text signals in which all
text anchors or regions are specified directly (without recourse to
indirect linking mechanisms).


III. Feedback and Support

Feedback is an important part of making Callisto the useful, intuitive
tool we hope it will become. If you find bugs, omissions,
shortcomings, glaring inconsistencies, nice things, useful new
features, or innovative capabilites, please share it all! :-) Callisto
has an external presence (http://callisto.mitre.org) that is the
focal point for online documentation, and mailing lists.

Please join the mainling lists there to recieve announcements and
discuss issues with Callisto. For direct questions on how your group
can support further enhancements of Callisto, contact David Day:

    Dr. David Day
    day@mitre.org
    781-271-2854
    The MITRE Corporation
    M/S K309
    202 Burlington Road,
    Bedford, MA 01730-1420

Bug reports and technical questions can go to David as well, or any of
the main developers:

    Chad McHenry     <red@mitre.org>
    Robyn Kozierok   <robynk@mitre.org>
    Laurel Riek      <laurel@mitre.org>


IV. Copyright Notice

Callisto Annotation Tool License

Except as permitted below
ALL RIGHTS RESERVED

SOFTWARE LICENSE 

The MITRE Corporation (MITRE) provides this software to you without
charge to use for your internal purposes only. Any copy you make for
such purposes is authorized provided you reproduce MITRE's copyright
designation and this License in any such copy. You may not give or
sell this software to any other party without the prior written
permission of the MITRE Corporation.

The government of the United States of America may make unrestricted
use of this software.

This software is the copyright work of MITRE. No ownership or other
proprietary interest in this software is granted you other than what
is granted in this license.

Any modification or enhancement of this software must inherit this
license, including its warranty disclaimers. You hereby agree to
provide to MITRE, at no charge, a copy of any such modification or
enhancement without limitation.

MITRE IS PROVIDING THE PRODUCT "AS IS" AND MAKES NO WARRANTY, EXPRESS
OR IMPLIED, AS TO THE ACCURACY, CAPABILITY, EFFICIENCY,
MERCHANTABILITY, OR FUNCTIONING OF THIS SOFTWARE AND DOCUMENTATION. IN
NO EVENT WILL MITRE BE LIABLE FOR ANY GENERAL, CONSEQUENTIAL,
INDIRECT, INCIDENTAL, EXEMPLARY OR SPECIAL DAMAGES, EVEN IF MITRE HAS
BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.

You accept this software on the condition that you indemnify and hold
harmless MITRE, its Board of Trustees, officers, agents, and
employees, from any and all liability or damages to third parties,
including attorneys' fees, court costs, and other related costs and
expenses, arising out of your use of this software irrespective of the
cause of said liability.

The export from the United States or the subsequent reexport of this
software is subject to compliance with United States export control
and munitions control restrictions. You agree that in the event you
seek to export this software you assume full responsibility for
obtaining all necessary export licenses and approvals and for assuring
compliance with applicable reexport restrictions.


V. About the name "Callisto"

Under the auspices of the DARPA TIDES project, MITRE
(http://www.mitre.org/), NIST (http://www.nist.gov/) and LDC
(http://www.ldc.upenn.edu/) began working together to define a new
annotation formalism and related infrastructure tools to encourage the
interchange of linguistic annotations on a wide variety of "linguistic
signals" (text, audio, video, multi-modal signals, etc.).  The result
was ATLAS, which is an acronym for: Architecture and Tools for
Linguistic Analysis Systems (see http://www.nist.gov/speech/atlas/ for
more information on ATLAS).

Of course, Atlas is also one of the gods of ancient Greek mythology.
Subsequently NIST developed the jATLAS implementation of the ATLAS
formalism, and defined a new type definition language for ATLAS, which
they called MAIA.  In Greek mythology, Maia is one of the seven
daughters of Atlas, or Pleiades.

In keeping with this theme we have named our text annotation tool
after another Greek mythological figure, Callisto.  Callisto consorted
with Zeus (after he tricked her by disguising himself as Artemis).
Their tryst resulted in a child, Arcas 1, who came to rule Arcadia
later in life.  Callisto was punished for being Zeus' lover, and was
turned into a bear by Hera.  Zeus had pity on Callisto falling to this
fate, and so transformed her into the constellation Ursa Major (Great
Bear), thus allowing her to gain immortality.  He gave their child,
Arcas 1, to Hermes' mother, Maia, one of the Pleiades, who raised him
to adulthood.

