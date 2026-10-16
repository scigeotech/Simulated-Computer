public class Shifter {
    public static void LeftShift(Word32 source, int amount, Word32 result) {
    	Bit hold = new Bit(false);
    	for (int i = 0; i < 32 - amount; i++) { //-amount to prevent overflow
    		source.getBitN(i + amount, hold); //get bit from [progress + shift amount]
    		result.setBitN(i, hold); //set i-th bit of result to that
    	}
    }

    public static void RightShift(Word32 source, int amount, Word32 result) {
    	Bit hold = new Bit(false);
    	for (int i = amount; i < 32; i++) {
    		source.getBitN(i - amount, hold); //get bit from [amount] bits behind [progress]
    		result.setBitN(i, hold); //set i-th bit to result of that
    	}
    }
}
