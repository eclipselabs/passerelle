# Makefile for the Ptolemy II vergil/actor/img directory
#
# Author: Christopher Hylands (makefile only)
# Version: $Id: makefile,v 1.5 2007/12/06 18:30:36 cxh Exp $
#
# Copyright (c) 2001-2007 The Regents of the University of California.
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
##########################################################################

ME =		ptolemy/vergil/gt/img

# Sub directories to run make in.
DIRS =

# Root of Ptolemy II directory
ROOT =		../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	img
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =	$(PTPACKAGE).jar


OTHER_FILES_TO_BE_JARED = \
	batchmatch_o.gif \
	batchmatch_on.gif \
	batchmatch_ov.gif \
	batchmatch.gif \
	close.gif \
	close_o.gif \
	close_on.gif \
	close_ov.gif \
	match_o.gif \
	match_on.gif \
	match_ov.gif \
	match.gif \
	next_o.gif \
	next_on.gif \
	next_ov.gif \
	next.gif \
	nextfile_o.gif \
	nextfile_on.gif \
	nextfile_ov.gif \
	nextfile.gif \
	previous_o.gif \
	previous_on.gif \
	previous_ov.gif \
	previous.gif \
	previousfile_o.gif \
	previousfile_on.gif \
	previousfile_ov.gif \
	previousfile.gif \
	transform_o.gif \
	transform_on.gif \
	transform_ov.gif \
	transform.gif \
	transformall_o.gif \
	transformall_on.gif \
	transformall_ov.gif \
	transformall.gif

EXTRA_SRCS = \
	$(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

include $(ROOT)/mk/ptno-compile.mk
