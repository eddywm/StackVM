package io.osm.vm;

import java.util.ArrayList;
import java.util.List;


import static io.osm.vm.Bytecode.*;

/** A simple stack-based interpreter */
class VM {
	private static final int DEFAULT_STACK_SIZE = 1000;
	private static final int DEFAULT_CALL_STACK_SIZE = 1000;
	private static final int FALSE = 0;
	private static final int TRUE = 1;

	// registers
	private int ip;             // instruction pointer register
	private int sp = -1;  		// stack pointer register

	// memory
	private int[] code;         // word-addressable code memory but still bytecodes.
	private int[] globals;      // global variable space
	private int[] stack;		// Operand stack, grows upwards
	private Context ctx;		// the active context

	/** Metadata about the functions allows us to refer to functions by
	 * 	their index in this table. It makes code generation easier for
	 * 	the bytecode compiler because it doesn't have to resolve
	 *  addresses for forward references. It can generate simply
	 *  "CALL i" where i is the index of the function. Later, the
	 *  compiler can store the function address in the metadata table
	 *  when the code is generated for that function.
	 */
	private FuncMetaData[] metadata;

	boolean trace = false;

	VM(int[] code, int nglobals, FuncMetaData[] metadata) {
		this.code = code;
		globals = new int[nglobals];
		stack = new int[DEFAULT_STACK_SIZE];
		this.metadata = metadata;
	}

	void exec(int startip) {
		ip = startip;
		ctx = new Context(null,0,metadata[0]); // simulate a call to main()
		cpu();
	}

	/** Simulate the fetch-decode execute cycle */
	private void cpu() {
		int opcode = code[ip];
		int a,b,addr,regnum;
		while (opcode!= HALT && ip < code.length) {
			if ( trace ) System.err.printf("%-35s", disInstr());
			ip++; //jump to next instruction or to operand
			switch (opcode) {
				case IADD:
					b = stack[sp--];   			// 2nd opnd at top of stack
					a = stack[sp--]; 			// 1st opnd 1 below top
					stack[++sp] = a + b;      	// push result
					break;
				case ISUB:
					b = stack[sp--];
					a = stack[sp--];
					stack[++sp] = a - b;
					break;
				case IMUL:
					b = stack[sp--];
					a = stack[sp--];
					stack[++sp] = a * b;
					break;
				case ILT :
					b = stack[sp--];
					a = stack[sp--];
					stack[++sp] = (a < b) ? TRUE : FALSE;
					break;
				case IEQ :
					b = stack[sp--];
					a = stack[sp--];
					stack[++sp] = (a == b) ? TRUE : FALSE;
					break;
				case BR :
					ip = code[ip++];
					break;
				case BRT :
					addr = code[ip++];
					if ( stack[sp--]==TRUE ) ip = addr;
					break;
				case BRF :
					addr = code[ip++];
					if ( stack[sp--]==FALSE ) ip = addr;
					break;
				case ICONST:
					stack[++sp] = code[ip++]; // push operand
					break;
				case LOAD : // load local or arg
					regnum = code[ip++];
					stack[++sp] = ctx.locals[regnum];
					break;
				case GLOAD :// load from global memory
					addr = code[ip++];
					stack[++sp] = globals[addr];
					break;
				case STORE :
					regnum = code[ip++];
					ctx.locals[regnum] = stack[sp--];
					break;
				case GSTORE :
					addr = code[ip++];
					globals[addr] = stack[sp--];
					break;
				case PRINT :
					System.out.println(stack[sp--]);
					break;
				case POP:
					--sp;
					break;
				case CALL :
					// expects all args on stack
					int findex = code[ip++];			// index of target function
					int nargs = metadata[findex].nargs;	// how many args got pushed
					ctx = new Context(ctx,ip,metadata[findex]);
					// copy args into new context
					int firstarg = sp-nargs+1;
					for (int i=0; i<nargs; i++) {
						ctx.locals[i] = stack[firstarg+i];
					}
					sp -= nargs;
					ip = metadata[findex].address;		// jump to function
					break;
				case RET:
					ip = ctx.returnip;
					ctx = ctx.invokingContext;			// pop
					break;
				default :
					throw new Error("invalid opcode: "+opcode+" at ip="+(ip-1));
			}
			if ( trace ) System.err.printf("%-22s %s\n", stackString(), callStackString());
			opcode = code[ip];
		}
		if ( trace ) System.err.printf("%-35s", disInstr());
		if ( trace ) System.err.println(stackString());
		if ( trace ) dumpDataMemory();
	}

	private String stackString() {
		StringBuilder buf = new StringBuilder();
		buf.append("stack=[");
		for (int i = 0; i <= sp; i++) {
			int o = stack[i];
			buf.append(" ");
			buf.append(o);
		}
		buf.append(" ]");
		return buf.toString();
	}

	private String callStackString() {
		List<String> stack = new ArrayList<String>();
		Context c = ctx;
		while ( c!=null ) {
			if ( c.metadata!=null ) {
				stack.add(0, c.metadata.name);
			}
			c = c.invokingContext;
		}
		return "calls="+stack.toString();
	}

	private String disInstr() {
		int opcode = code[ip];
		String opName = Bytecode.instructions[opcode].name;
		StringBuilder buf = new StringBuilder();
		buf.append(String.format("%04d:\t%-11s", ip, opName));
		int nargs = Bytecode.instructions[opcode].n;
		if ( opcode==CALL ) {
			buf.append(metadata[code[ip+1]].name);
		}
		else if ( nargs>0 ) {
			List<String> operands = new ArrayList<String>();
			for (int i=ip+1; i<=ip+nargs; i++) {
				operands.add(String.valueOf(code[i]));
			}
			for (int i = 0; i<operands.size(); i++) {
				String s = operands.get(i);
				if ( i>0 ) buf.append(", ");
				buf.append(s);
			}
		}
		return buf.toString();
	}

	protected void dumpDataMemory() {
		System.err.println("Data memory:");
		int addr = 0;
		for (int o : globals) {
			System.err.printf("%04d: %s\n", addr, o);
			addr++;
		}
		System.err.println();
	}
}
