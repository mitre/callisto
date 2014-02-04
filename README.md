# The Callisto Annotation Tool

### Copyright (c) 2002-2014 The MITRE Corporation:
### Version 1.8
### October 21, 2013

**Note:** Most of the documentation is for an earlier version (1.5).  It should
mostly still apply to 1.8, but documentation has not been updated since the
1.5 release.

## Overview


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


## Release Contents

Callisto is distributed as in installer program, or as a source distribution, both to be available here shortly.


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


## Requirements

Callisto is implemented in Java, requiring Java version 1.5 or later. Other
required libraries are included with the distribution of Callisto, as
mentioned in the Acknowledgements.

Java 1.6 is recommended.

## Installation

1. Install java 2 Runtime Environment version 1.5 or later.

   Note that java is already installed in many systems.  

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


## Support
MITRE is no longer able to offer support for Callisto.

A mailing list has been created for Callisto users to interact.

    Callisto Users <callisto-users-list@lists.mitre.org>

To join, send an email message to <listserv@mitre.org> with the following line in the body of the email (The subject line is ignored by the listserv). 
    SUBSCRIBE callisto-users-list yourname

Bug reports and technical questions can be posted to the mailing list; however, as Callisto is not currently being actively supported, you may not receive a reply from the development team. 


## Documentation

The Callisto license can be found in the file LICENSE.txt.
Installation instructions can be found in the file INSTALL.txt

Detailed installation installation instructions and a user manual can
be found in the $CALLISTO_HOME/docs/index.html


## Acknowledgements

The primary development team for Callisto consisted of Chad McHenry,
Robyn Kozierok, and Laurel Riek, with contributions from David Day,
Samuel Bayer, Galen Williamson, Keith Crouch and Justin Richer.
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
    icu4j: copyright International Business Machines Corporation and others
	(http://www.icu-project.org)
    persiancalendar: copyright Ghasem Kiani 
	(http://sourceforge.net/projects/persiancalendar)

This product includes software developed by Mark Wutka
(http://www.wutka.com/) to parse DTDs; and software developed
by Marty Hall (http://www.apl.jhu.edu/~hall/java) to print
an arbitrary component.
