package com.isencia.util.charops;


public abstract class ASTNode{

	public int sourceStart, sourceEnd;

	// storage for internal flags (32 bits)				BIT USAGE
	public final static int Bit1 = 0x1;					// return type (operator) | name reference kind (name ref) | add assertion (type decl) | useful empty statement (empty statement)
	public final static int Bit2 = 0x2;					// return type (operator) | name reference kind (name ref) | has local type (type, method, field decl)
	public final static int Bit3 = 0x4;					// return type (operator) | name reference kind (name ref) | implicit this (this ref)
	public final static int Bit4 = 0x8;					// return type (operator) | first assignment to local (name ref,local decl) | undocumented empty block (block, type and method decl)
	public final static int Bit5 = 0x10;					// value for return (expression) | has all method bodies (unit) | supertype ref (type ref) | resolved (field decl)
	public final static int Bit6 = 0x20;					// depth (name ref, msg) | ignore need cast check (cast expression) | error in signature (method declaration/ initializer) | is recovered (annotation reference)
	public final static int Bit7 = 0x40;					// depth (name ref, msg) | operator (operator) | need runtime checkcast (cast expression) | label used (labelStatement) | needFreeReturn (AbstractMethodDeclaration)
	public final static int Bit8 = 0x80;					// depth (name ref, msg) | operator (operator) | unsafe cast (cast expression) | is default constructor (constructor declaration)
	public final static int Bit9 = 0x100;				// depth (name ref, msg) | operator (operator) | is local type (type decl)
	public final static int Bit10= 0x200;				// depth (name ref, msg) | operator (operator) | is anonymous type (type decl)
	public final static int Bit11 = 0x400;				// depth (name ref, msg) | operator (operator) | is member type (type decl)
	public final static int Bit12 = 0x800;				// depth (name ref, msg) | operator (operator) | has abstract methods (type decl)
	public final static int Bit13 = 0x1000;			// depth (name ref, msg) | is secondary type (type decl)
	public final static int Bit14 = 0x2000;			// strictly assigned (reference lhs) | discard enclosing instance (explicit constr call) | hasBeenGenerated (type decl)
	public final static int Bit15 = 0x4000;			// is unnecessary cast (expression) | is varargs (type ref) | isSubRoutineEscaping (try statement) | superAccess (javadoc allocation expression/javadoc message send/javadoc return statement)
	public final static int Bit16 = 0x8000;			// in javadoc comment (name ref, type ref, msg)
	public final static int Bit17 = 0x10000;			// compound assigned (reference lhs) | unchecked (msg, alloc, explicit constr call)
	public final static int Bit18 = 0x20000;			// non null (expression) | onDemand (import reference)
	public final static int Bit19 = 0x40000;			// didResolve (parameterized qualified type ref/parameterized single type ref)  | empty (javadoc return statement) | needReceiverGenericCast (msg/fieldref)
	public final static int Bit20 = 0x80000;
	public final static int Bit21 = 0x100000;
	public final static int Bit22 = 0x200000;			// parenthesis count (expression) | used (import reference)
	public final static int Bit23 = 0x400000;			// parenthesis count (expression)
	public final static int Bit24 = 0x800000;			// parenthesis count (expression)
	public final static int Bit25 = 0x1000000;		// parenthesis count (expression)
	public final static int Bit26 = 0x2000000;		// parenthesis count (expression)
	public final static int Bit27 = 0x4000000;		// parenthesis count (expression)
	public final static int Bit28 = 0x8000000;		// parenthesis count (expression)
	public final static int Bit29 = 0x10000000;		// parenthesis count (expression)
	public final static int Bit30 = 0x20000000;		// elseif (if statement) | try block exit (try statement) | fall-through (case statement) | ignore no effect assign (expression ref) | needScope (for statement) | isAnySubRoutineEscaping (return statement) | blockExit (synchronized statement)
	public final static int Bit31 = 0x40000000;		// local declaration reachable (local decl) | ignore raw type check (type ref) | discard entire assignment (assignment) | isSynchronized (return statement) | thenExit (if statement)
	public final static int Bit32 = 0x80000000;		// reachable (statement)

	public final static long Bit32L = 0x80000000L;
	public final static long Bit33L = 0x100000000L;
	public final static long Bit34L = 0x200000000L;
	public final static long Bit35L = 0x400000000L;
	public final static long Bit36L = 0x800000000L;
	public final static long Bit37L = 0x1000000000L;
	public final static long Bit38L = 0x2000000000L;
	public final static long Bit39L = 0x4000000000L;
	public final static long Bit40L = 0x8000000000L;
	public final static long Bit41L = 0x10000000000L;
	public final static long Bit42L = 0x20000000000L;
	public final static long Bit43L = 0x40000000000L;
	public final static long Bit44L = 0x80000000000L;
	public final static long Bit45L = 0x100000000000L;
	public final static long Bit46L = 0x200000000000L;
	public final static long Bit47L = 0x400000000000L;
	public final static long Bit48L = 0x800000000000L;
	public final static long Bit49L = 0x1000000000000L;
	public final static long Bit50L = 0x2000000000000L;
	public final static long Bit51L = 0x4000000000000L;
	public final static long Bit52L = 0x8000000000000L;
	public final static long Bit53L = 0x10000000000000L;
	public final static long Bit54L = 0x20000000000000L;
	public final static long Bit55L = 0x40000000000000L;
	public final static long Bit56L = 0x80000000000000L;
	public final static long Bit57L = 0x100000000000000L;
	public final static long Bit58L = 0x200000000000000L;
	public final static long Bit59L = 0x400000000000000L;
	public final static long Bit60L = 0x800000000000000L;
	public final static long Bit61L = 0x1000000000000000L;
	public final static long Bit62L = 0x2000000000000000L;
	public final static long Bit63L = 0x4000000000000000L;
	public final static long Bit64L = 0x8000000000000000L;

	public int bits = IsReachable; 				// reachable by default

	// for operators
	public static final int ReturnTypeIDMASK = Bit1|Bit2|Bit3|Bit4;
	public static final int OperatorSHIFT = 6;	// Bit7 -> Bit12
	public static final int OperatorMASK = Bit7|Bit8|Bit9|Bit10|Bit11|Bit12; // 6 bits for operator ID

	// for binary expressions
	public static final int IsReturnedValue = Bit5;

	// for cast expressions
	public static final int UnnecessaryCast = Bit15;
	public static final int DisableUnnecessaryCastCheck = Bit6;
	public static final int GenerateCheckcast = Bit7;
	public static final int UnsafeCast = Bit8;

	// for name references
	public static final int RestrictiveFlagMASK = Bit1|Bit2|Bit3;

	// for name refs or local decls
	public static final int FirstAssignmentToLocal = Bit4;

	// for msg or field references
	public static final int NeedReceiverGenericCast = Bit19;
	
	// for this reference
	public static final int IsImplicitThis = Bit3;

	// for single name references
	public static final int DepthSHIFT = 5;	// Bit6 -> Bit13
	public static final int DepthMASK = Bit6|Bit7|Bit8|Bit9|Bit10|Bit11|Bit12|Bit13; // 8 bits for actual depth value (max. 255)

	// for statements
	public static final int IsReachable = Bit32;
	public static final int LabelUsed = Bit7;
	public static final int DocumentedFallthrough = Bit30;

	// local decls
	public static final int IsLocalDeclarationReachable = Bit31;

	// try statements
	public static final int IsSubRoutineEscaping = Bit15;
	public static final int IsTryBlockExiting = Bit30;

	// for type declaration
	public static final int ContainsAssertion = Bit1;
	public static final int IsLocalType = Bit9;
	public static final int IsAnonymousType = Bit10; // used to test for anonymous
	public static final int IsMemberType = Bit11; // local member do not know it is local at parse time (need to look at binding)
	public static final int HasAbstractMethods = Bit12; // used to promote abstract enums
	public static final int IsSecondaryType = Bit13; // used to test for secondary
	public static final int HasBeenGenerated = Bit14;

	// for type, method and field declarations
	public static final int HasLocalType = Bit2; // cannot conflict with AddAssertionMASK
	public static final int HasBeenResolved = Bit5; // field decl only (to handle forward references)

	// for expression
	public static final int ParenthesizedSHIFT = 21; // Bit22 -> Bit29
	public static final int ParenthesizedMASK = Bit22|Bit23|Bit24|Bit25|Bit26|Bit27|Bit28|Bit29; // 8 bits for parenthesis count value (max. 255)
	public static final int IgnoreNoEffectAssignCheck = Bit30;

	// for references on lhs of assignment
	public static final int IsStrictlyAssigned = Bit14; // set only for true assignments, as opposed to compound ones
	public static final int IsCompoundAssigned = Bit17; // set only for compound assignments, as opposed to other ones

	// for explicit constructor call
	public static final int DiscardEnclosingInstance = Bit14; // used for codegen

	// for all method/constructor invocations (msg, alloc, expl. constr call)
	public static final int Unchecked = Bit17;
	
	// for empty statement
	public static final int IsUsefulEmptyStatement = Bit1;

	// for block and method declaration
	public static final int UndocumentedEmptyBlock = Bit4;
	public static final int OverridingMethodWithSupercall = Bit5;

	// for initializer and method declaration
	public static final int ErrorInSignature = Bit6;

	// for abstract method declaration
	public static final int NeedFreeReturn = Bit7; // abstract method declaration

	// for constructor declaration
	public static final int IsDefaultConstructor = Bit8;

	// for compilation unit
	public static final int HasAllMethodBodies = Bit5;
	public static final int IsImplicitUnit = Bit1;

	// for references in Javadoc comments
	public static final int InsideJavadoc = Bit16;

	// for javadoc allocation expression/javadoc message send/javadoc return statement
	public static final int SuperAccess = Bit15;

	// for javadoc return statement
	public static final int Empty = Bit19;

	// for if statement
	public static final int IsElseIfStatement = Bit30;
	public static final int ThenExit = Bit31;

	// for type reference
	public static final int IsSuperType = Bit5;
	public static final int IsVarArgs = Bit15;
	public static final int IgnoreRawTypeCheck = Bit31;

	// for array initializer
	public static final int IsAnnotationDefaultValue = Bit1;

	// for null reference analysis
	public static final int IsNonNull = Bit18;

	// for for statement
	public static final int NeededScope = Bit30;

	// for import reference
	public static final int OnDemand = Bit18;
	public static final int Used = Bit2;

	// for parameterized qualified/single type ref
	public static final int DidResolve = Bit19;

	// for return statement
	public static final int IsAnySubRoutineEscaping = Bit30;
	public static final int IsSynchronized = Bit31;

	// for synchronized statement
	public static final int BlockExit = Bit30;

	// for annotation reference
	public static final int IsRecovered = Bit6;

	// constants used when checking invocation arguments
	public static final int INVOCATION_ARGUMENT_OK = 0;
	public static final int INVOCATION_ARGUMENT_UNCHECKED = 1;
	public static final int INVOCATION_ARGUMENT_WILDCARD = 2;

}
