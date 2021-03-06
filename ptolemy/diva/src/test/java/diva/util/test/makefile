# Makefile for util tests classes - Ptolemy II specific version of Diva
#
# @Authors: Christopher Hylands (makefile only)
#
# @Version: $Id: makefile,v 1.7 2005/02/28 19:05:25 cxh Exp $
#
# @Copyright (c) 2003-2005 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY

# Location of this directory, relative to the Ptolemy II directory
ME =		diva/util/test

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Tcl sources that are part of the testing infrastructure
TCL_SRCS =

JSRCS = \
	All.java \
	ShapeUtilitiesTest.java \
	XMLElementTest.java \
	XMLParserTest.java


# Non-graphical Java tests written in Tcl
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JSIMPLE_TESTS) to EXTRA_SRCS
# NOTE: Please do not put tests that use diva in JSIMPLE_TESTS
# If you do, then the nightly build will report these tests as failing
# tests that use diva and graphical tests in general should go
# under JGRAPHICAL tests.
JSIMPLE_TESTS = \
	dummy

# Graphical Java tests that use Tcl.
JGRAPHICAL_TESTS = \
	dummy

# If we add non-graphical tests, add $(JSIMPLE_TESTS) to EXTRA_SRCS
# If we add graphical Tcl tests, add $(JGRAPHICAL_TESTS) to EXTRA_SRCS
EXTRA_SRCS =	\
	$(TCL_SRCS) $(JSRCS) \
	xml1.dtd \
	xml1.xml

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	#alljtests.tcl alljsimpletests.tcl

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'ShapeUtilitiesTest$$1.class' \
	'ShapeUtilitiesTest$$2.class' \
	'ShapeUtilitiesTest$$3.class' \
	'ShapeUtilitiesTest$$4.class' \
	'XMLElementTest$$1.class' \
	'XMLElementTest$$2.class' \
	'XMLElementTest$$3.class' \
	'XMLParserTest$$1.class'

JCLASS =	$(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.


test: $(EXTRA_SRCS) jclass test_java All

# The nightly build runs make tests, which in this case will not
# run the graphical tests
tests:: $(EXTRA_SRCS) jclass test_java test_jsimple

test_java: jclass

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl

# These tests do not require a graphics terminal, but do use Jacl and Java
test_jsimple: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljsimpletests.tcl
	$(JTCLSH) alljsimpletests.tcl

# These tests do require a graphics terminal, but do use Jacl and Java
test_jgraphical: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljtests.tcl
	$(JTCLSH) alljtests.tcl

# Rules for running individual tests
All:
	$(JAVA) -classpath $(CLASSPATH) diva.util.test.All

ShapeUtilities:
	$(JAVA) -classpath $(CLASSPATH) diva.util.test.ShapeUtilitiesTest

XMLElement:
	$(JAVA) -classpath $(CLASSPATH) diva.util.test.XMLElement

XMLParser:
	$(JAVA) -classpath $(CLASSPATH) diva.util.test.XMLParser

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
