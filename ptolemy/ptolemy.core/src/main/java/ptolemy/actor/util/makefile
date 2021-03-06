# Makefile for Java Ptolemy concurrent functional classes
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id: makefile,v 1.58 2007/12/06 18:18:40 cxh Exp $
#
# @Copyright (c) 1998-2007 The Regents of the University of California.
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

# This directory contains classes used for dataflow and pn channels that
# should _not_ go into the kernel so that we can keep the kernel free
# of the notion of dataflow.  These classes are concurrent functional
# classes:
#  FIFOQueue
#  Star
#  Galaxy
#  Universe
# Note that none of these classes would be relevant to FSMs, for example.

ME =		ptolemy/actor/util

# Root of the Java directory
ROOT =		../../..

CLASSPATH = 	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

DIRS = 		test

# Used to build jar files
PTPACKAGE = 	util
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar


# Keep this list alphabetized.
JSRCS = \
	ActorTypeUtil.java \
	CalendarQueue.java \
	ConstVariableModelAnalysis.java \
	CQComparator.java \
	DFUtilities.java \
	DependencyDeclaration.java \
	ExplicitChangeContext.java \
	FIFOQueue.java \
	FreeVariableModelAnalysis.java \
	FunctionDependency.java \
	FunctionDependencyOfAtomicActor.java \
	FunctionDependencyOfCompositeActor.java \
	FuzzyDoubleComparator.java \
	GeneralComparator.java \
	SuperdenseTime.java \
	Time.java \
	TimedEvent.java \
	TotallyOrderedSet.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'ActorTypeUtil$$TypeableSizedArrayVariableTypeTerm.class' \
	'CalendarQueue$$CQCell.class' \
	'CalendarQueue$$CQEntry.class' \
	'CalendarQueue$$CQLinkedList.class' \
	'CalendarQueue$$EntryComparator.class' \
	'CalendarQueue$$SortedLinkedList.class' \
	'DFUtilities$$NamedObjComparator.class' \
	'TimedEvent$$TimeComparator.class'

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass subinstall $(PTCLASSJAR)


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
