This is the fifth beta release of jATLAS 2.0.

It has been further modified by MITRE, the version indicated by an
incrementing letter appended to the version.

Change log at the end of this file.

---------------------------------------------
Thank you for trying jATLAS!

About jATLAS:
-------------

jATLAS is a Java implementation of the ATLAS framework. For more
information on ATLAS, please consult:
http://www.nist.gov/speech/atlas/

To learn more about jATLAS, please consult:
http://www.nist.gov/speech/atlas/jatlas/

Installation instructions and a tutorial can be found at:
http://www.nist.gov/speech/atlas/jatlas/tutorial.html

NOTES:

jATLAS requires Java 2 Standard Edition version 1.4 or better to
work.

The following command line argument needs to be passed to the
virtual machine:
-Djava.protocol.handler.pkgs=gov.nist.atlas.impl

jATLAS makes use of the dom4j XML framework for Java which is
published under an Apache-style open source license. For more
information (and sources) on dom4j, please refer to: http://dom4j.org/

jATLAS also uses the log4j logging framework for Java which is
published under the Apache open source license. For more information
(and sources) on log4j, please refer to:
http://jakarta.apache.org/log4j/

jATLAS also uses the jUnit unit testing framework for Java which
is published under IBM's Common Public License Version 0.5. For more
information (and sources) on jUnit, please refer to:
http://junit.org/


Feedback:
---------

As this package constitutes a beta distribution of jATLAS, it is bound
to have some problems associated to it. We are looking for feedback of
any kind so that we can improve the distribution. More specifically,
we are particularly interested in collecting comments and ideas about
the API (is it feature complete? are the names intuitive enough?, etc)
and about MAIA.

We will update the distribution as often as reasonable and provide
more information on the ATLAS web site as we make more available.


Organization of this package:
-----------------------------

README                        this file

build.xml                     ant script (http://ant.apache.org)

lib/                          contains the JAR files needed to use jATLAS

src/                          contains the source code for jATLAS
  tests/                      test files needed for jATLAS' test suite
  examples/                   contains examples

target/                       contains all generated files: including
                                jar file, and distributed tarballs


Changes:
--------

2.0beta5e 2004-04-02

  * AtlasRef now refers to it's ReusableATLASElement (if available)
    for idAsURL which ensures that if a corpus' location changes,
    References will continue to work. (ReusablesAE's cache idURLs)
  * ReusableATLASElements immediately cache upon creation, their
    idAsURL to avoid conflict when corpus location changes

2.0beta5d 2003-12-11

  * Parameters w/ null values are no longer written to AIF

2.0beta5c 2003-12-04

  * Changes copied from code never released on jATLAS SF pages.
  * fix for errors when signal track was unspecified
  * getDefinedRolesForChildren() added to ATLASType
  * iterateorOverTypesWith(ATLASClass) added to MAIAScheme

2.0beta5b 2003-03-01

  * Initial branching from jATLAS project sometime after the 2.0
    beta5a tag
