# Makefile for Java Ptolemy polymorphic actor classes
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id: makefile,v 1.250.4.2 2008/03/25 22:31:30 cxh Exp $
#
# @Copyright (c) 1998-2008 The Regents of the University of California.
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

# This directory contains polymorphic actors.

ME =		ptolemy/actor/lib

# $PTII/configure looks for Colt and sets PTCOLT_DIR in
# $PTII/mk/ptII.mk if was found.  Otherwise, PTCOLT_DIR is empty.

# $PTII/configure looks for Java Advanced Imaging and sets PTJAI_DIR in
# $PTII/mk/ptII.mk if was found.  Otherwise, PTJAI_DIR is empty.
#
# $PTII/configure looks for Java Media Framework and sets PTJMF_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJMF_DIR is empty.
#
# $PTII/configure looks for the Joystick interface  and sets PTJOYSTICK_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJOYSTICK_DIR is empty.
#
# $PTII/configure looks for Jython and sets PTJYTHON_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJYTHON_DIR is empty.
#
# $PTII/configure looks for JXTA and sets PTJXTA_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJXTA_DIR is empty.
#
# $PTII/configure looks for X10 and sets PTX10_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTX10_DIR is empty.

DIRS = 		conversions gui logic hoc image javasound jni net io xslt \
		test comm security string tutorial vhdl \
		$(PTCOLT_DIR) $(PTJAI_DIR) \
		$(PTJMF_DIR) $(PTJOYSTICK_DIR) \
		$(PTJYTHON_DIR) $(PTJXTA_DIR) $(PTX10_DIR)

# Root of the Java directory
ROOT =		../../..

# Do not add diva.jar to the CLASSPATH in actor/lib/makefile
# instead, move any actors that require diva.jar into actor/lib/gui -cxh
CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	lib
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	comm/comm.jar \
	conversions/conversions.jar \
	gui/gui.jar \
	hoc/hoc.jar \
	image/image.jar \
	io/io.jar \
	logic/logic.jar \
	javasound/javasound.jar \
	jni/jni.jar \
	net/net.jar \
	security/security.jar \
	string/string.jar \
	vhdl/vhdl.jar \
	xslt/xslt.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	AddSubtract.java \
	AbsoluteValue.java \
	Accumulator.java \
	ArrayAppend.java \
	ArrayAverage.java \
	ArrayContains.java \
	ArrayElement.java \
	ArrayElementAsMatrix.java \
	ArrayExtract.java \
	ArrayLength.java \
	ArrayLevelCrossing.java \
	ArrayMaximum.java \
	ArrayMinimum.java \
	ArrayPeakSearch.java \
	ArrayRemoveElement.java \
	ArraySort.java \
	ArraySum.java \
	ArrayToElements.java \
	Average.java \
	Bernoulli.java \
	BooleanMultiplexor.java \
	BooleanSelect.java \
	BooleanSwitch.java \
	BusAssembler.java \
	BusDisassembler.java \
	ClassWrapper.java \
	Clock.java \
	CodegenActor.java \
	Commutator.java \
	ComputeHistogram.java \
	Const.java \
	Counter.java \
	CurrentTime.java \
	DelayStart.java \
	Differential.java \
	DoubleReader.java \
	DB.java \
	Discard.java \
	DiscreteRandomSource.java \
	Distributor.java \
	ElementsToArray.java \
	Exec.java \
	Exit.java \
	Expression.java \
	FileWriter.java \
	Gaussian.java \
	GradientAdaptiveLattice.java \
	IIR.java \
	Interpolator.java \
	Lattice.java \
	LevinsonDurbin.java \
	Limiter.java \
	LimitedFiringSource.java \
	LinearDifferenceEquationSystem.java \
	LookupTable.java \
	MathFunction.java \
	MaxIndex.java \
	Maximum.java \
	Minimum.java \
	MonitorValue.java \
	MovingAverage.java \
	Multiplexor.java \
	MultiplyDivide.java \
	NonStrictTest.java \
	OrderedMerge.java \
	PhaseUnwrap.java \
	PoissonClock.java \
	Publisher.java \
	PublisherNonStrictTest.java \
	PublisherTest.java \
	Pulse.java \
	Quantizer.java \
	Ramp.java \
	RandomSource.java \
	Reader.java \
	RecordAssembler.java \
	RecordDisassembler.java \
	RecordUpdater.java \
	Recorder.java \
	RecursiveLattice.java \
	Remainder.java \
	RemoveNilTokens.java \
	Rician.java \
	Scale.java \
	Select.java \
	Sequence.java \
	SequenceActor.java \
	Sequencer.java \
	SequenceSource.java \
	SequentialClock.java \
	SetVariable.java \
	SingleTokenCommutator.java \
	Sink.java \
	Sleep.java \
	Source.java \
	Stop.java \
	StringConst.java \
	SubMatrix.java \
	Subscriber.java \
	SubscriptionAggregator.java \
	Switch.java \
	Synchronizer.java \
	Test.java \
	TestExceptionHandler.java \
	ThrowException.java \
	ThrowModelError.java \
	TimedSource.java \
	Transformer.java \
	Triangular.java \
	TrigFunction.java \
	TriggeredClock.java\
	TrigFunction.java \
	TypeTest.java \
	UnaryMathFunction.java \
	Uniform.java \
	UnionDisassembler.java \
	URLReader.java \
	URLDirectoryReader.java \
	VariableClock.java \
	VariableSleep.java \
	VectorAssembler.java \
	VectorDisassembler.java \
	WallClockTime.java \
	Writer.java

OTHER_FILES_TO_BE_JARED = \
	BooleanSelectIcon.xml \
	BooleanSwitchIcon.xml \
	CommutatorIcon.xml \
	ComputeHistogramIcon.xml \
	DistributorIcon.xml \
	GaussianIcon.xml \
	TriangularIcon.xml \
	PowerEstimate.xml \
	SelectIcon.xml \
	Sinewave.xml \
	SwitchIcon.xml \
	TimedSine.xml \
	UniformIcon.xml \
	aggregators.xml \
	array.xml \
	booleanflowcontrol.xml \
	busses.xml \
	communications.xml \
	esoteric.xml \
	executioncontrol.xml \
	filtering.xml \
	flowcontrol.xml \
	genericsinks.xml \
	genericsources.xml \
	math.xml \
	matrix.xml \
	random.xml \
	randomNoColt.xml \
	realtime.xml \
	sequencecontrol.xml \
	SequenceIcon.xml \
	sequencesinks.xml \
	sequencesources.xml \
	signalprocessing.xml \
	sinks.xml \
	sources.xml \
	spectrum.xml \
	statistical.xml \
	test.xml \
	timedsinks.xml \
	timedsources.xml \

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	comm \
	doc \
	jai \
	javasound \
	jmf \
	jni \
	joystick \
	x10 \
	'AbsoluteValue$$1.class' \
	'AbsoluteValue$$FunctionTerm.class' \
	'Exec$$_StreamReaderThread.class' \
	'Expression$$1.class' \
	'Expression$$OutputTypeFunction.class' \
	'Expression$$VariableScope.class' \
	'RecordDisassembler$$1.class' \
	'RecordDisassembler$$PortFunction.class' \
	'RecordUpdater$$1.class' \
	'RecordUpdater$$FunctionTerm.class' \
	'Scale$$1.class' \
	'Scale$$PortParamFunction.class' \
	'Scale$$PortParameterFunction.class' \
	'SetVariable$$1.class' \
	'UnionDisassembler$$1.class' \
	'UnionDisassembler$$PortFunction.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
