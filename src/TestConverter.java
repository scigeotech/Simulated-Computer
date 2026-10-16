public class TestConverter {
    public static void fromInt(int value, Word32 result) {
    	for (int i = 31; i >= 0; i--) {
    		if ((value & (1 << i)) != 0) {
    			result.setBitN(31 - i, new Bit(true));
    		} else {
    			result.setBitN(31 - i, new Bit(false));
    		}
    	}
    }

    public static int toInt(Word32 value) {
    	String binary = value.toString(); //ttfftfft...
    	binary = binary.replace('f', '0');
    	binary = binary.replace('t', '1');
    	
    	boolean negative = false;
    	if (binary.charAt(0) == '1') { //negative?
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
}