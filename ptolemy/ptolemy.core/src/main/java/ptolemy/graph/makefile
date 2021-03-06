# Makefile for the Ptolemy Java graph classes.
#
# @Authors: Shahrooz Shahparnia, Shuvra S. Bhattacharyya,
# Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id: makefile,v 1.63 2005/03/17 00:54:19 cxh Exp $
#
# @Copyright (c) 1998-2005 The Regents of the University of California.
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
#                       PT_COPYRIGHT_VERSION_2
#                       COPYRIGHTENDKEY

ME =        ptolemy/graph

DIRS =      analysis sched mapping test

# Root of the Java directory
ROOT =      ../..

CLASSPATH = $(ROOT)

# Get configuration info
CONFIG =    $(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE =     graph
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	analysis/analysis.jar \
	mapping/mapping.jar \
	sched/sched.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
    CPO.java \
    DirectedAcyclicGraph.java \
    DirectedGraph.java \
    Edge.java \
    Element.java \
    ElementList.java \
    Graph.java \
    GraphActionException.java \
    GraphConstructionException.java \
    GraphElementException.java \
    GraphException.java \
    GraphStateException.java \
    GraphTopologyException.java \
    GraphWeightException.java \
    LabeledList.java \
    Inequality.java \
    InequalitySolver.java \
    InequalityTerm.java \
    Node.java

EXTRA_SRCS =    $(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't list 'demo' here, or running 'make sources' will also run the demos
MISC_FILES =    analysis mapping sched test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
    doc \
    demo \
    'InequalitySolver$$1.class' \
    'InequalitySolver$$Info.class' \
    'LabeledList$$1.class' \
    'LabeledList$$SubList.class'

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
