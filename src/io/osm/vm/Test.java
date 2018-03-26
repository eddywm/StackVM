package io.osm.vm;


import static io.osm.vm.Bytecode.*;

public class Test {
	static int[] hello = {
		ICONST, 1,
		ICONST, 2,
		IADD,
		PRINT,
		HALT
	};

	static int[] loop = {
	// .GLOBALS 2; N, I
	// N = 10						ADDRESS
			ICONST, 10,				// 0
			GSTORE, 0,				// 2
	// I = 0
			ICONST, 0,				// 4
			GSTORE, 1,				// 6
	// WHILE I<N:
	// START (8):
			GLOAD, 1,				// 8
			GLOAD, 0,				// 10
			ILT,					// 12
			BRF, 24,				// 13
	//     I = I + 1
			GLOAD, 1,				// 15
			ICONST, 1,				// 17
			IADD,					// 19
			GSTORE, 1,				// 20
			BR, 8,					// 22
	// DONE (24):
	// PRINT "LOOPED "+N+" TIMES."
			HALT					// 24
	};
	static FuncMetaData[] loop_metadata = {
		new FuncMetaData("main", 0, 0, 0)
	};

	static int FACTORIAL_INDEX = 1;
	static int FACTORIAL_ADDRESS = 0;
	static int MAIN_ADDRESS = 21;
	static int[] factorial = {
//.def factorial: ARGS=1, LOCALS=0	ADDRESS
//	IF N < 2 RETURN 1
			LOAD, 0,				// 0
			ICONST, 2,				// 2
			ILT,					// 4
			BRF, 10,				// 5
			ICONST, 1,				// 7
			RET,					// 9
//CONT:
//	RETURN N * FACT(N-1)
			LOAD, 0,				// 10
			LOAD, 0,				// 12
			ICONST, 1,				// 14
			ISUB,					// 16
			CALL, FACTORIAL_INDEX,	// 17
			IMUL,					// 19
			RET,					// 20
//.DEF MAIN: ARGS=0, LOCALS=0
// PRINT FACT(1)
			ICONST, 5,				// 21    <-- MAIN METHOD!
			CALL, FACTORIAL_INDEX,	// 23
			PRINT,					// 25
			HALT					// 26
	};
	static FuncMetaData[] factorial_metadata = {
		//.def factorial: ARGS=1, LOCALS=0	ADDRESS
		new FuncMetaData("main", 0, 0, MAIN_ADDRESS),
		new FuncMetaData("factorial", 1, 0, FACTORIAL_ADDRESS)
	};

	static int[] f = {
	//								ADDRESS
	//.def main() { print f(10); }
		ICONST, 10,					// 0
		CALL, 1,					// 2
		PRINT,						// 4
		HALT,						// 5
	//.def f(x): ARGS=1, LOCALS=1
	//  a = x;
		LOAD, 0,					// 6	<-- start of f
		STORE, 1,
	// return 2*a
		LOAD, 1,
		ICONST, 2,
		IMUL,
		RET
	};
	static FuncMetaData[] f_metadata = {
		new FuncMetaData("main", 0, 0, 0),
		new FuncMetaData("f", 1, 1, 6)
	};


	public static void main(String[] args) {
		VM vm = new VM(factorial, 0, factorial_metadata);
		vm.trace = true;
		vm.exec(factorial_metadata[0].address);

		vm = new VM(f, 2, f_metadata);
		vm.exec(f_metadata[0].address);
		vm.dumpDataMemory();

		vm = new VM(loop, 2, loop_metadata);
		vm.exec(loop_metadata[0].address);
	}
}
