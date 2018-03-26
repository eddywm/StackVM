package io.osm.vm;

class Bytecode {
	public static class Instruction {
		String name; // E.g., "iadd", "call"
		int n = 0;
		Instruction(String name) { this(name,0); }
		Instruction(String name, int nargs) {
			this.name = name;
			this.n = nargs;
		}
	}

	// INSTRUCTION BYTECODES (byte is signed; use a short to keep 0..255)
	static final short IADD = 1;     // int add
	static final short ISUB = 2;
	static final short IMUL = 3;
	static final short ILT  = 4;     // int less than
	static final short IEQ  = 5;     // int equal
	static final short BR   = 6;     // branch
	static final short BRT  = 7;     // branch if true
	static final short BRF  = 8;     // branch if true
	static final short ICONST = 9;   // push constant integer
	static final short LOAD   = 10;  // load from local context
	static final short GLOAD  = 11;  // load from global memory
	static final short STORE  = 12;  // store in local context
	static final short GSTORE = 13;  // store in global memory
	static final short PRINT  = 14;  // print stack top
	static final short POP  = 15;    // throw away top of stack
	static final short CALL = 16;
	static final short RET  = 17;    // return with/without value

	static final short HALT = 18;

	static Instruction[] instructions = new Instruction[] {
		null, // <INVALID>
		new Instruction("iadd"), // index is the opcode
		new Instruction("isub"),
		new Instruction("imul"),
		new Instruction("ilt"),
		new Instruction("ieq"),
		new Instruction("br", 1),
		new Instruction("brt", 1),
		new Instruction("brf", 1),
		new Instruction("iconst", 1),
		new Instruction("load", 1),
		new Instruction("gload", 1),
		new Instruction("store", 1),
		new Instruction("gstore", 1),
		new Instruction("print"),
		new Instruction("pop"),
		new Instruction("call", 1), // call index of function in meta-info table
		new Instruction("ret"),
		new Instruction("halt")
	};
}
