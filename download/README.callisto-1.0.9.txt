                     ----------------------------
                     The Callisto Annotation Tool
                     ----------------------------

             Copyright (c) 2002-2003 The MITRE Corporation

                            Version 1.0.9
                            Nov. 07, 2003
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

Callisto is distributed as a tarball from the MITRE Corporation at
http://callisto.mitre.org/.  NOTE: due to temporary distribution
restrictions the web site is under password protection.  Contact your
group coordinator or the support team for more information.

The directory structure is as follows:

callisto-<version>/        (aka $CALLISTO_HOME)
    data/                  (sample data)
    doc/
        Documentation.txt  (detailed installation and use)
        api/               (javadoc API)
    tasks/                 (annotation task plugins)
    Callisto.jar
    LICENSE.txt            (Callisto license)
    README.txt             (this File)
    dom4j.jar
    jATLAS.jar
    junit.jar
    log4j.jar


Requirements
------------

Callisto is implemented in Java, requiring Java version 1.4 or later,
obtainable from Sun Microsystems at http://java.sun.com/. Other
required libraries are included with the distribution of Callisto, as
mentioned in the Acknowledgements.


Installation
------------

1. Install java 2 Runtime Environment version 1.4 or later.

2. Unpack the main archive file using tar and gzip, or winzip.  This
   creates a directory callisto-<version> which we'll refer to as
   $CALLISTO_HOME. It may be placed anywhere on your system.

3. To run Callisto:

    1. Windows 95, 98, ME, 2000, XP, MacOSX:
          Double-click the $CALLISTO_HOME/Callisto.jar file.  You can
          also run Callisto from the command line as described below

    2. Sun Solaris, Linux:
          At a shell prompt, cd into the $CALLISTO_HOME directory
          run java, invoking the Callisto application, like this:

              $ java -jar Callisto.jar

For more information on installing Task plugins refer to the user
documentation in $CALLISTO_HOME/doc/Documentation.txt


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

Bug reports and technical questions can be sent directly to the , or any of
the main developers:

    Callisto Developers <callisto-dev-list@lists.mitre.org>


Documentation
-------------

The Callisto license can be found in the file LICENSE.txt.
Installation instructions can be found in the file INSTALL.txt

Detailed installation installation instructions and a user manual can
be found in the $CALLISTO_HOME/doc/Documentaion.txt


Acknowledgements
----------------

The primary development team for Callisto consisted of Chad McHenry,
Robyn Kozierok, and Laurel Riek, with contributions from David Day.
Design and interface guidance was provided by Lisa Ferro, Janet
Hitzeman, Marcia Lazo, Marc Vilain and David Day.  The project leader
for this effort was David Day.

jATLAS was created by NIST (http://www.nist.gov/speech/atlas/)

The following copyrighted works make up portions of Callisto:

    dom4j: copyright MetaStuff, Ltd. (http://dom4j.org/)
    log4j: copyright The Apache Software Foundation (http://www.apache.org/)
