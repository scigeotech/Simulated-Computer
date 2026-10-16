public class Multiplier {
    public static void multiply(Word32 a, Word32 b, Word32 result) {
    	Bit mult = new Bit(false);
    	for (int i=31; i >= 0; i--) {
    		b.getBitN(i, mult); //pick current multiplier
    		if (mult.getValue() == Bit.boolValues.TRUE) { //if it is 1 (can be multiplied)
    			Word32 shifted = new Word32(), temp = new Word32();
    			Shifter.LeftShift(a, 31 - i, shifted); //shift by row
    			result.copy(temp); //to temporarily preserve "result"
    			Adder.add(temp, shifted, result);
    		}
    	}
    }
}
