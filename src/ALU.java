public class ALU {
    public Word16 instruction = new Word16();
    public Word32 op1 = new Word32();
    public Word32 op2 = new Word32();
    public Word32 result = new Word32();
    public Bit less = new Bit(false);
    public Bit equal = new Bit(false);

    public void doInstruction(){
    	int addr = opcodeAsInt();
    	//System.out.println(addr);
    	switch(addr) {
    	case 1://add
    		Adder.add(op1, op2, result);
    		break;
    	case 2: //and
    		for (int i=0; i<32; i++) {
    			op1.bits[i].and(op2.bits[i], result.bits[i]);
    		}
    		break;
    	case 3: //multiply
    		result = new Word32(); //for whatever reason it doesn't work without this.
    		Multiplier.multiply(op1, op2, result);
    		break;
    	case 4: //left shift
    		int LShift = shiftAsInt(op2);
    		Shifter.LeftShift(op1, LShift, result);
    		break;
    	case 5: //subtract
    		Adder.subtract(op1, op2, result);
    		break;
    	case 6: //or
    		for (int i=0; i<32; i++) {
    			op1.bits[i].or(op2.bits[i], result.bits[i]);
    		}
    		break;
    	case 7: //right shift
    		int RShift = shiftAsInt(op2);
    		Shifter.RightShift(op1, RShift, result);
    		break;
    	case 11: //compare
    		Adder.subtract(op1, op2, result);
    		less.assign(result.bits[0].getValue()); //if first is 1, then negative
    		if (op1.equals(op2)) {
    			equal.assign(Bit.boolValues.TRUE);
    		} else {
    			equal.assign(Bit.boolValues.FALSE);
    		}
    		break;
    	}
    }
    
    private int opcodeAsInt() { //get first 5 ints
    	String binary = instruction.toString(); //ttfftfft...
    	binary = binary.replace('f', '0');
    	binary = binary.replace('t', '1');
    	
    	int result = 0, position = 0;
    	for (int i = 4; i >= 0; i--) { //32 - 5 = 27
    		if (binary.charAt(i) == '1') {
    			result += (int)Math.pow(2, position);
    		}
    		position++;
    		
    	}
    	return result;
    }
    
    public int shiftAsInt(Word32 data) { //0 - 31, 31 in binary is 11111 (5 digits)
    	String binary = data.toString(); //ttfftfft...
    	binary = binary.replace('f', '0');
    	binary = binary.replace('t', '1');
    	
    	int result = 0, position = 0;
    	for (int i = binary.length() - 1; i >= 27; i--) { //32 - 5 = 27
    		if (binary.charAt(i) == '1') {
    			result += (int)Math.pow(2, position);
    		}
    		position++;
    	}
    	
        return result;
    }
}
