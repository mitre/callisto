                     ----------------------------
                     The Callisto Annotation Tool
                     ----------------------------

             Copyright (c) 2002-2006 The MITRE Corporation

                            Version 1.4.0
			    Feb. 21, 2006

Overview
--------

Callisto is an annotation tool developed for linguistic annotation of
textual data. Any Unicode-supported language can be annotated, and
files encoded as UTF-8 and several other character encodings are
accepted. Callisto stores annotations in a stand-off format using the
ATLAS data model, and can support importing/exporting of inline
annotation such as XML or SGML. The initial development of Callisto by
the MITRE Corporation was funded by the U. S. government.

 - Release Contents
 - Requirements
 - Installation
 - Support
 - Documentation
 - Acknowledgements


Release Contents
----------------

Callisto is distributed as in installer program, available at
http://callisto.mitre.org/.

By default, the installer will create a "Callisto" directory where
programs on your operating system normally go.  You may also change
this.

If you are using Windows, a "Callisto" program group will be created
in the "Start->Programs" menu.

The directory structure looks like this.

Callisto/                  (aka $CALLISTO_HOME)
    bin/                   (scripts to launch callisto and other tasks)
    data/                  (sample data)
    docs/
        index.html         (detailed installation and use)
        [files with licenses]
    lib/                   (third party libraries
        dom4j-1.5.2.jar
        jATLAS.jar
        jaxen-1.1-beta-4.jar
        junit.jar
        log4j.jar
        dtdparser121.jar
    tasks/                 (annotation task plugins)
    Callisto.jar
    INSTALL.txt
    LICENSE.txt            (Callisto license)
    README.txt             (this File)
    Uninstaller/


Requirements
------------

Callisto is implemented in Java, requiring Java version 1.4 or later,
obtainable from Sun Microsystems at http://java.sun.com/. Other
required libraries are included with the distribution of Callisto, as
mentioned in the Acknowledgements.

Note that Java Table behavior has changed in java 1.5, causing
problems with certain tables (only ace2004 known to be affected). The
only workaround at this time is to use java 1.4 for those tasks.


Installation
------------

1. Install java 2 Runtime Environment version 1.4 or later.

   Note that java is already installed in many systems.  See the
   manual at http://callisto.mitre.org/manual/install.html for more
   information.

2. Double click the callisto-<version>-installer.jar icon to start the
   installer. You can also start the installer from the command line
   with the command:

   $ > java -jar callisto-v.v.v-installer.jar

   You will be asked to choose a directory to install to we will refer
   to as $CALLISTO_HOME.  If you put $CALLISTO_HOME/bin in your
   shell's PATH variable, Callisto may be invoked from the command
   line.

3. To run Callisto:

    1. Windows 95, 98, ME, 2000, XP

          The installer should create icons in your Start Menu. Look
          for "Start->Programs->Callisto" group, and select the
          Callisto icon. You may also have chosen to put an icon on
          your desktop.

          In addition, you can click on the Callisto.jar file itself.

    2. Sun Solaris, Linux, MacOSX:

          If you put $CALLISTO_HOME/bin on your path, invoke Callist
          with:

             $ callisto

          At a shell prompt, cd into the $CALLISTO_HOME directory
          (where the installer placed Callisto).  Then run java,
          invoking the Callisto application, like this:

              $ java -jar Callisto.jar

          The installer currently cannot create icons on these
          systems.

          On MacOSX, you can click on the Callisto.jar file itself.
          Note that running on MacOSX is known to have problems.

For more information on installing Task plugins refer to the user
documentation in $CALLISTO_HOME/docs/index.html


Support
-------

Mailing lists, documentation, FAQs, and other information is available
at http://callisto.mitre.org/.  Please join the mailing lists to
recieve announcements and discuss issues with Callisto. For direct
questions on how your group can support further enhancements of
Callisto, contact David Day:

    Dr. David Day
    day@mitre.org
    781-271-2854
    The MITRE Corporation
    M/S K309
    202 Burlington Road,
    Bedford, MA 01730-1420

A mailing list has been created for Callisto users to interact. You
may sign up to recieve messages at http://callisto.mitre.org

    Callisto Users <callisto-users-list@lists.mitre.org>

Bug reports and technical questions can be sent directly to the
developers, or any of the main developers:

    Callisto Developers <callisto-dev-list@lists.mitre.org>


Documentation
-------------

The Callisto license can be found in the file LICENSE.txt.
Installation instructions can be found in the file INSTALL.txt

Detailed installation installation instructions and a user manual can
be found in the $CALLISTO_HOME/docs/index.html


Acknowledgements
----------------

The primary development team for Callisto consisted of Chad McHenry,
Robyn Kozierok, and Laurel Riek, with contributions from David Day,
Samuel Bayer, Galen Williamson, and Keith Crouch.
Design and interface guidance was provided by Lisa Ferro, Janet
Hitzeman, Marcia Lazo, Marc Vilain and David Day.  The project leader
for this effort was David Day.

ATLAS was created by NIST (http://www.nist.gov/speech/atlas), MITRE
(http://www.mitre.org), and the LDC (http://www.ldc.upenn.edu/) and
jATLAS was originally developed by NIST:

   jATLAS: http://sourceforge.net/projects/jatlas/

The following copyrighted works make up portions of Callisto:

    dom4j: copyright MetaStuff, Ltd. (http://dom4j.org/)
    jaxen: copyright The Werken Company, Ltd. (http://jaxen.org/)
    log4j, dtdparser: copyright The Apache Software Foundation 
        (http://www.apache.org/)

This product includes software developed by Mark Wutka
(http://www.wutka.com/) to parse DTDs.
