public class Adder {
    public static void subtract(Word32 a, Word32 b, Word32 result) {
    	Word32 reverse = new Word32(); //reverse of b
    	for (int i = 0; i < 32; i++) {
            Bit bitB = new Bit(false); //hold the current bit
            b.getBitN(i, bitB);
            Bit.not(bitB, bitB);
            reverse.setBitN(i, bitB);
        }
    	Word32 justOne = new Word32(), twosComp = new Word32();
    	justOne.setBitN(31, new Bit(true));
    	add(reverse, justOne, twosComp); //add 1 to make it negative
    	add(a, twosComp, result);
    }

    public static void add(Word32 a, Word32 b, Word32 result) {
    	Bit carry = new Bit(false); //null carry, this is "static" relative to bit comparisons below
    	for (int i=31; i>=0; i--) { //implement half-addder per pair of bits
    		Bit bitOne = new Bit(false), bitTwo = new Bit(false), sum = new Bit(false);
    		a.getBitN(i, bitOne);
    		b.getBitN(i, bitTwo);
    		Bit.xor(bitOne, bitTwo, sum); //0,0 or 1,1 = 0, but 1,0 and 0,1 = 1
    		Bit.xor(sum, carry, sum); //check against previous carry
    		
    		Bit carryBeta = new Bit(false); //for internal processing (really don't want any messes)
    		Bit prevSum = new Bit(false); //sum before checking against previous carry
            Bit.and(bitOne, bitTwo, carryBeta); //carry from inputs
            Bit.xor(bitOne, bitTwo, prevSum);

            Bit carryGamma = new Bit(false); //carry from sum and previous carry, shown below
            Bit.and(prevSum, carry, carryGamma);
            Bit.or(carryBeta, carryGamma, carryBeta); // final carry for next iteration

            result.setBitN(i, sum); // Set the resulting bit
            carry = carryBeta;       // Update carry for the next iteration
    	}
    	System.out.println();
    }
}
