import java.util.HashMap;
import java.util.Map;

public class Assembler {
	private static Map<String, String> opCodes = new HashMap<>(); 
	static { //initialize with opcodes
		opCodes.put("halt", "fffff"); //0
		opCodes.put("add", "fffft");
		opCodes.put("and", "ffftf");
		opCodes.put("multiply", "ffftt");
		opCodes.put("leftshift", "fftff");
		opCodes.put("subtract", "fftft"); //5
		opCodes.put("or", "ffttf");
		opCodes.put("rightshift", "ffttt");
		opCodes.put("syscall", "ftfff");
		opCodes.put("call", "ftfft");
		opCodes.put("return", "ftftf"); //10
		opCodes.put("compare", "ftftt");
		opCodes.put("ble", "fttff");
		opCodes.put("blt", "fttft");
		opCodes.put("bge", "ftttf");
		opCodes.put("bgt", "ftttt"); //15
		opCodes.put("beq", "tffff");
		opCodes.put("bne", "tffft");
		opCodes.put("load", "tfftf");
		opCodes.put("store", "tfftt");
		opCodes.put("copy", "tftff"); //20
	}
    public static String[] assemble(String[] input) {
    	String[] assembled = new String[input.length];
    	for (int i=0; i<input.length; i++) {
    		String[] parts = input[i].split(" "); //split "add r1 r2"
    		String opcode = opCodes.get(parts[0]);
    		String format, data1, destination, fullbinary = ""; //data1 is either source or immediate value
    		
    		if (parts.length <= 2 || opcode.equals("call") || opcode.equals("return")) { //call&return formats have 11bit immediate value
    			data1 = intToBinary(0, 11); //empty, padded to 11
    			if (parts.length == 2) {
    				data1 = intToBinary(Integer.parseInt(parts[1]), 11); 
    			}
    			fullbinary = opcode + data1;
        	} else if (parts[1].charAt(0) == 'r'){ //if register, then 2R format
        		format = "f"; //format
        		data1 = registerToBinary(parts[1]); //source register
        		destination = registerToBinary(parts[2]); //destination register
        		fullbinary = opcode + format + data1 + destination;
        	} else { //if neither, then immediate format
        		format = "t"; //format
        		data1 = intToBinary(Integer.parseInt(parts[1]), 5); //signed immediate value
        		destination = registerToBinary(parts[2]); //destination register
        		fullbinary = opcode + format + data1 + destination;
        	}
    		assembled[i] = fullbinary;
    	}
        return assembled;
    }

    public static String[] finalOutput(String[] input) {
    	String[] inputHolder = input; //copy to add halt if necessary
    	if (inputHolder.length % 2 == 1) { //if odd, append halt
    		String[] tempAppend = new String[inputHolder.length + 1];
    		System.arraycopy(inputHolder, 0, tempAppend, 0, inputHolder.length);
    		tempAppend[inputHolder.length] = intToBinary(0, 16); //16 f's
    		inputHolder = tempAppend;
    	}
    	String[] result = new String[inputHolder.length / 2];
    	
    	String hold;
    	for (int i=0; i<inputHolder.length; i+=2) { //0, 2, 4, 6, 8...
    		hold = inputHolder[i] + inputHolder[i + 1];
    		result[i / 2] = hold; //0, 1, 2, 3, 4...
    	}
        return result;
    }
    
    public static String intToBinary(int num, int size) {
    	StringBuilder result = new StringBuilder();
    	for (int i = size - 1; i >= 0; i--) { //size is max binary length
    		if ((num & (1 << i)) != 0) {
    			result.append("t");
    		} else {
    			result.append("f"); //if size exceeds true length, pad by f/0 (useful!)
    		}
    	}
    	return result.toString();
    }
    
    private static String registerToBinary(String input) { //5 bits max
    	int register = Integer.parseInt(input.substring(1)); //remove 'r' char, extract num
    	return intToBinary(register, 5);
    }
}
