import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Processor {
    private Memory mem;
    public List<String> output = new LinkedList<>();
    boolean instrStart = true; //is it first half of instruction duo? (fetch)
    Word32 instrDuo = new Word32(); //hold two instructions (from one read)
    Word16 instr = new Word16();
    Word32 pc = new Word32(); //program counter
    Word32[] registers;
    //ALU data
    private Word32 op1 = new Word32();
    private Word32 op2 = new Word32();
    private Word32 result = new Word32();
    private Bit less = new Bit(false);
    private Bit equal = new Bit(false);
    private static ALU alu;
    private Stack<Word32> stack;
    private static int currentClockCycle = 0;
    
    int immediate, bigImmediate;
    String[] formats = {"none", "gen", "gen", "gen", "gen", "gen", "gen", "gen", "Call", "Call", "Call", //0-10, gen = 2R/immediate
    		"gen", "Call", "Call", "Call", "Call", "Call", "Call", "gen", "gen", "gen"};
    
    public Processor(Memory m) {
        mem = m;
        registers = new Word32[32];
        for (int i = 0; i < 32; i++) {
        	registers[i] = new Word32();
        }
        alu = new ALU();
        stack = new Stack<>();
    }
    
    boolean notHalted = true;
    public void run() {
    	notHalted = true;
    	while (notHalted) {
    		fetch();
    		decode();
    		execute();
    		store();
    	}
    }

    private void fetch() { //Fetch reads an instruction from memory.
    	if (instrStart) { //instrDuo old or empty
    		pc.copy(mem.address);
    		mem.read();
    		mem.value.copy(instrDuo);
    		instrDuo.getTopHalf(instr);
    		instrStart = false;
    	} else { //instrDuo in progress, take second half
    		instrDuo.getBottomHalf(instr);
    		instrStart = true;
    	}
    }

    private void decode() {
        String src, dest;
    	Word16 inst = new Word16();
    	instr.copy(inst);
    	//System.out.println(instrDuo.toString());
    	//System.out.println(instr.toString());
    	int opCode = toInt(inst.toString().substring(0, 5), false);
    	if (formats[opCode].equals("gen")) { //if "generic" (2R/immediate), check format
    		//For instructions that use registers, copy() from the register into op1/op2
    		if (inst.bits[5].getValue() == Bit.boolValues.FALSE) { //2R
    			src = binaryToRegister(inst.toString().substring(6, 11));
    			fromRegister(src, op1);
    			dest = binaryToRegister(inst.toString().substring(11, 16));
    			fromRegister(dest, op2);
    			System.out.println("2R logged " + opCode + " and " + toInt(op1.toString(), true) + " with " + toInt(op2.toString(), true));
    		} else { //Immediate
    			immediate = toInt(inst.toString().substring(6, 11), true); //signed immediate value
    			fillOp(inst, op1, 6, 10);
    			dest = binaryToRegister(inst.toString().substring(11, 16));
    			fromRegister(dest, op2);
    			System.out.println("imm logged " + opCode + " and " + op1.toString() + " for " + immediate + " with " + toInt(op2.toString(), true));
    		}
    	} else if (formats[opCode].equals("Call")) { //Call/Return
    		bigImmediate = toInt(inst.toString().substring(5, 16), true); //signed immediate value
    		fillOp(inst, op1, 5, 15);
			System.out.println("Call logged " + opCode + " and " + op1.toString() + " for " + bigImmediate);
			//System.out.println(toInt(op1.toString(), true));
    	} else {
    		System.out.println("halt logged " + opCode);
    	}
    }

    private void execute() {
    	Word16 inst = new Word16();
    	Word32 hold = new Word32(); //for temp word32 values
    	String src, dest;
    	instr.copy(inst);
    	inst.copy(alu.instruction);
    	int opCode = toInt(inst.toString().substring(0, 5), false);
    	Word32 temp = new Word32();
    	switch (opCode) { //if applicable to current ALU functions
    	case 0:
    		notHalted = false;
    		break;
    	case 1: case 2: case 3: case 6: case 7:
    		op1.copy(alu.op1); //extract and send data
    		op2.copy(alu.op2);
    		alu.doInstruction(); //do work
    		alu.result.copy(result); //extract and gather results
    		System.out.println(op1 + " and " + op2 + " for " + toInt(op1.toString(), true) + " and " + toInt(op2.toString(), true));
    		System.out.println("result " + toInt(result.toString(), true) + " as " +result.toString() + " for " + opCode);
    		break;
    	case 4: case 5: //Subtracts source from destination (order is flipped)
			op1.copy(alu.op2); //extract and send data
			op2.copy(alu.op1);
			alu.doInstruction(); //do work
			alu.result.copy(result); //extract and gather results
			System.out.println(op1 + " and " + op2 + " for " + toInt(op1.toString(), true) + " and " + toInt(op2.toString(), true));
			System.out.println("result " + toInt(result.toString(), true) + " as " +result.toString() + " for " + opCode);
			break;
    	case 8: //syscall
    		op1.copy(result); //there is no kernel mode
			Word32 tempy = new Word32();
			if (op1.equals(tempy)) {
				printReg();
			} else {
				fromInt(1, tempy);
				if (tempy.equals(op1)) {
					printMem();
				}
			}
    		break;
    	case 9: //call -> Pushes the current address + 1 on the stack. Sets PC to PC + Immediate
    		fromInt(1, temp); //hold 1
    		Adder.add(pc, temp, temp); //add pc + 1 together
    		stack.push(temp);
    		break;
    	case 10: //return -> Pops from the stack and sets the PC to the popped value.
    		//not performed here, see store()
    		break;
    	case 11:
    		op1.copy(alu.op1); //extract and send data
    		op2.copy(alu.op2);
    		alu.doInstruction(); //do work
    		alu.result.copy(result); //extract and gather results
    		System.out.println(op1.toString() + " compared to " + op2.toString());
    		break;
    	case 12: //If status register LESS or EQUAL are set, PC = PC + Immediate
    		less.assign(alu.less.getValue()); //extract and gather data
    		equal.assign(alu.equal.getValue());
    		break;
    	case 13: //If status register LESS is set
    		less.assign(alu.less.getValue()); //extract and gather data
    		break;
    	case 14: //If status register GREATER or EQUAL are set,
    		less.assign(alu.less.getValue()); //extract and gather data
    		equal.assign(alu.equal.getValue());
    		break;
    	case 15: //If status register GREATER is set
    		less.assign(alu.less.getValue()); //extract and gather data
    		break;
    	case 16: //If status register EQUAL is set
    		equal.assign(alu.equal.getValue());
    		break;
    	case 17: //If status register EQUAL is NOT set, PC = PC + Immediate
    		equal.assign(alu.equal.getValue());
    		break;
    	case 18:
    		if (inst.bits[5].getValue() == Bit.boolValues.FALSE) { //2R, Loads from memory address source into destination
        		op1.copy(mem.address);
        		mem.read();
            	mem.value.copy(result);
            	dest = binaryToRegister(inst.toString().substring(11, 16));
            	toRegister(dest, result);
            	System.out.println("loaded1 " + toInt(result.toString(), true) + " from " + toInt(op1.toString(), true) + " to register " + dest);
    		} else { //immediate, Loads from memory address (immediate + destination) into destination register
               	dest = binaryToRegister(inst.toString().substring(11, 16));
               	fromRegister(dest, hold); //hold dest value
               	Adder.add(op1, hold, mem.address);
               	//System.out.println(toInt(mem.address.toString(), true));
            	//fromInt(immediate + toInt(hold.toString(), true), mem.address);
            	mem.read();
            	mem.value.copy(result);
            	toRegister(dest, result);
            	System.out.println(hold.toString());
            	System.out.println("loaded2 " + toInt(result.toString(), true) + " from " + toInt(mem.address.toString(), true)+mem.address.toString() + " to register " + dest);
    		}
    		break;
    	case 19: //address stored in dest, STR operation: stores the value found in OP1 to the memory address found in OP2.
    		op1.copy(mem.value);
    		op2.copy(mem.address);
    		mem.write();
        	mem.value.copy(result);
    		System.out.println("stored " + toInt(result.toString(), true) +" into " + mem.address.toString() + " for " + toInt(mem.address.toString(), true));
    		break;
    	case 20:
    		if (inst.bits[5].getValue() == Bit.boolValues.FALSE) { //2R
    			src = binaryToRegister(inst.toString().substring(6, 11));
        		dest = binaryToRegister(inst.toString().substring(11, 16));
        		fromRegister(src, result); //copy from src register into "hold"
    		} else { //immediate
    			op1.copy(result);
        		dest = binaryToRegister(inst.toString().substring(11, 16));
    		}
    		System.out.println("copy " + inst.bits[5].getValue() + result.toString() + toInt(result.toString(), true) + " to " + dest);
    		break;
    	}
    }

    private void printReg() {
        for (int i = 0; i < 32; i++) {
            var line = "r"+ i + ":" + commaString(registers[i]); //added the register value here...
            output.add(line);
            //System.out.println(line);
        }
    }

    private void printMem() {
        for (int i = 0; i < 1000; i++) {
            Word32 addr = new Word32();
            Word32 value = new Word32();
            fromInt(i, addr); // Convert i to Word32 here...
            addr.copy(mem.address);
            mem.read();
            mem.value.copy(value);
            var line = i + ":" + value + "(" + TestConverter.toInt(value) + ")";
            output.add(line);
            //System.out.println(line);
        }
    }

    private void store() {
    	Word16 inst = new Word16();
    	Word32 temp = new Word32();
    	instr.copy(inst);
    	int opCode = toInt(inst.toString().substring(0, 5), false);
		String dest;
		boolean pcChanged = false;
		//Bit.boolValues format = inst.bits[5].getValue(); //false 2R, true immediate
		switch (opCode) {
        case 0: 
        	break; //halt
        case 1: case 2: case 3: case 4: case 5: case 6: case 7: //arithmetic
        	dest = binaryToRegister(inst.toString().substring(11, 16));
        	toRegister(dest, result);
    		if (opCode == 3) {
    			incrementClock(10);
    		} else {
    			incrementClock(2);
    		}
            break;
        case 8:
            //syscall, not stored
            break;
        case 9: //call -> Pushes the current address + 1 on the stack. Sets PC to PC + Immediate
    		op1.copy(temp); //set tempAddr to immediate
    		Adder.add(pc, temp, temp); //pc + immediate
    		temp.copy(pc);
    		System.out.println("pushed to " + pc.toString());
    		pcChanged = true;
    		break;
    	case 10: //return -> Pops from the stack and sets the PC to the popped value.
    		Word32 popped = stack.pop();
    		popped.copy(pc);
    		instrStart = true;
    		System.out.println("returned to " + pc.toString());
    		pcChanged = true;
    		break;
    	case 11:
    		less.assign(alu.less.getValue()); //extract values
    		equal.assign(alu.equal.getValue());
    		incrementClock(2);
    		break;
    	case 12: //If status register LESS or EQUAL are set, PC = PC + Immediate
    		if (less.getValue() == Bit.boolValues.TRUE || equal.getValue() == Bit.boolValues.TRUE) {
    			op1.copy(temp); //set tempAddr to immediate
        		Adder.add(pc, temp, temp); //pc + immediate
        		temp.copy(pc);
        		pcChanged = true;
    		}
    		break;
    	case 13: //If status register LESS is set
    		if (less.getValue() == Bit.boolValues.TRUE) {
    			op1.copy(temp); //set tempAddr to immediate
        		Adder.add(pc, temp, temp); //pc + immediate
        		temp.copy(pc);
        		pcChanged = true;
    		}
    		break;
    	case 14: //If status register GREATER or EQUAL are set,
    		if (less.getValue() == Bit.boolValues.FALSE || equal.getValue() == Bit.boolValues.TRUE) {
    			op1.copy(temp); //set tempAddr to immediate
        		Adder.add(pc, temp, temp); //pc + immediate
        		temp.copy(pc);
        		pcChanged = true;
    		}
    		break;
    	case 15: //If status register GREATER is set
    		if (less.getValue() == Bit.boolValues.FALSE) {
    			op1.copy(temp); //set tempAddr to immediate
        		Adder.add(pc, temp, temp); //pc + immediate
        		temp.copy(pc);
        		pcChanged = true;
    		}
    		break;
    	case 16: //If status register EQUAL is set
    		if (equal.getValue() == Bit.boolValues.TRUE) {
    			op1.copy(temp); //set tempAddr to immediate
        		Adder.add(pc, temp, temp); //pc + immediate
        		temp.copy(pc);
        		pcChanged = true;
    		}
    		break;
    	case 17: //If status register EQUAL is NOT set, PC = PC + Immediate
    		if (equal.getValue() == Bit.boolValues.FALSE) {
    			op1.copy(temp); //set tempAddr to immediate
        		Adder.add(pc, temp, temp); //pc + immediate
        		temp.copy(pc);
        		pcChanged = true;
    		}
    		break;
        case 18: //load
        	dest = binaryToRegister(inst.toString().substring(11, 16));
        	toRegister(dest, result);
        	incrementClock(300);
            break;
        case 19: //store
        	dest = binaryToRegister(inst.toString().substring(11, 16));
    		incrementClock(2);
            break;
        case 20: //copy
        	dest = binaryToRegister(inst.toString().substring(11, 16));
        	toRegister(dest, result);
        	System.out.println(toInt(result.toString(), true));
    		incrementClock(2);
            break;
		}
		if (!pcChanged && instrStart) {
			Word32 addMe = new Word32();
    		fromInt(1, addMe);
    		Adder.add(pc, addMe, pc); //add 1 to pc
		}
		//printReg();
    }
    
    //-------------------- HELPER FUNCTIONS --------------------
    
    public void fromRegister(String register, Word32 deposit) { //copy from register into deposit
    	int reg = Integer.parseInt(register.substring(1)); //remove 'r' char, extract num
		registers[reg].copy(deposit);
    }
    
    public void toRegister(String register, Word32 source) { //copy from source into register
    	int reg = Integer.parseInt(register.substring(1)); //remove 'r' char, extract num
    	source.copy(registers[reg]);
    }
    
    public static String binaryToRegister(String binary) { //handle 5 digits, append to 'r'
        return "r" + toInt(binary, false); //registers are unsigned;
    }
    
    public static int toInt(String binaryInput, boolean signed) { //ttfftfft...
    	String binary = binaryInput;
    	binary = binary.replace('f', '0');
    	binary = binary.replace('t', '1');
    	
    	boolean negative = false;
    	if (binary.charAt(0) == '1' && signed) { //can it be negative?
    		binary = binary.replace('0', 'f'); //swap 0's and 1's
    		binary = binary.replace('1', '0');
    		binary = binary.replace('f', '1');
    		binary = addOne(binary);
    		negative = true;
    	}
    	
    	int result = 0, position = 0;
    	for (int i = binary.length() - 1; i >= 0; i--) {
    		if (binary.charAt(i) == '1') {
    			result += (int)Math.pow(2, position);
    		}
    		position++;
    	}
    	
    	if (negative) {
    		result = 0 - result;
    	}
    	
        return result;
    }
    
    private static String addOne(String binary) { //it was getting very messy up there
        StringBuilder myBinary = new StringBuilder(binary);
        boolean carry = true;
        for (int i = myBinary.length() - 1; i >= 0; i--) {
            if (myBinary.charAt(i) == '0' && carry) {
                myBinary.setCharAt(i, '1');
                carry = false;
                break;
            } else if (myBinary.charAt(i) == '1' && carry) {
                myBinary.setCharAt(i, '0');
            }
        }
        return myBinary.toString();
    }
    
    private void fillOp(Word16 src, Word32 op, int start, int end) {
    	for (int i=0; i<32; i++) {
    		op.bits[i].assign(Bit.boolValues.FALSE);
    	}
        int immBits = end - start + 1; //how many bits are in the immediate value
        for (int i=0; i<immBits; i++) { //src bit index: start + i, dest bit index: 32 - immBits + i
            op.bits[32 - immBits + i].assign(src.bits[start + i].getValue()); //copy from src to op
        } //For immediate values, copy and sign extend the immediate value
        if (src.bits[start].getValue() == Bit.boolValues.TRUE) { //if very first is false (overall negative)
            for (int i=0; i<32 - immBits; i++) { //sign extend with 1/true
                op.bits[i].assign(Bit.boolValues.TRUE);
            }
        }
        /*
    	for (int i=end; i>=start; i--) {
    		op.bits[31 - end + i].assign(src.bits[i].getValue()); //For immediate values, copy and sign extend the immediate value
		}
    	if (src.bits[0].getValue() == Bit.boolValues.TRUE) { //if very first is false (overall negative)
    		for (int i=0; i<32 - start; i++) {
    			op.bits[i].assign(Bit.boolValues.TRUE);
    		}
    	} */
    }
    
    public static void fromInt(int value, Word32 result) {
    	for (int i = 31; i >= 0; i--) {
    		if ((value & (1 << i)) != 0) {
    			result.setBitN(31 - i, new Bit(true));
    		} else {
    			result.setBitN(31 - i, new Bit(false));
    		}
    	}
    }
    
    public String commaString(Word32 input) {
        StringBuilder sb = new StringBuilder();
        for (Bit bit : input.bits) {
            sb.append(bit.toString() + ",");
        }
        return sb.toString();
    }
    
    public static void incrementClock(int add) {
    	currentClockCycle += add;
    }
}