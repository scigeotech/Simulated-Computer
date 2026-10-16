public class Word32 {
	public Bit[] bits;
	
    public Word32() {
    	bits = new Bit[32]; //store 32
		for (int i=0; i<32; i++) {
			bits[i] = new Bit(false);
		}
    }

    public Word32(Bit[] in) {
    	bits = new Bit[32];
    	for (int i=0; i<32; i++) {
			this.bits[i] = in[i];
		}
    }

    public void getTopHalf(Word16 result) { // sets result = bits 0-15 of this word. use bit.assign
    	for (int i=0; i<16; i++) {
    		result.bits[i].assign(this.bits[i].getValue()); //alternatively result.setBitN(i, this.bits[i]);
		} //the alternative solution means Word16's Bit[] does not have to be public.
    }

    public void getBottomHalf(Word16 result) { // sets result = bits 16-31 of this word. use bit.assign
    	for (int i=0; i<16; i++) {
    		result.bits[i].assign(this.bits[i + 16].getValue()); //alternatively result.setBitN(i, this.bits[i + 16]);
		} //the alternative solution means Word16's Bit[] does not have to be public.
    }

    public void copy(Word32 result) { // sets result's bit to be the same as this. use bit.assign
    	for (int i=0; i<32; i++) {
    		result.bits[i].assign(this.bits[i].getValue());
		}
    }

    public boolean equals(Word32 other) {
    	return equals(this, other);
    }

    public static boolean equals(Word32 a, Word32 b) {
    	boolean result = true;
    	for (int i=0; i<32; i++) {
			if (a.bits[i].getValue() != b.bits[i].getValue()) {
				result = false; //if even a single mismatch, then false
			}
		}
        return result;
    }

    public void getBitN(int n, Bit result) { // use bit.assign
    	result.assign(bits[n].getValue());
    }

    public void setBitN(int n, Bit source) { //  use bit.assign
    	bits[n].assign(source.getValue());
    }

    public void and(Word32 other, Word32 result) {
    	and(this, other, result);
    }

    public static void and(Word32 a, Word32 b, Word32 result) {
    	for (int i=0; i<32; i++) {
			Bit.and(a.bits[i], b.bits[i], result.bits[i]);
		}
    }

    public void or(Word32 other, Word32 result) {
    	or(this, other, result);
    }

    public static void or(Word32 a, Word32 b, Word32 result) {
    	for (int i=0; i<32; i++) {
			Bit.or(a.bits[i], b.bits[i], result.bits[i]);
		}
    }

    public void xor(Word32 other, Word32 result) {
    	xor(this, other, result);
    }

    public static void xor(Word32 a, Word32 b, Word32 result) {
    	for (int i=0; i<32; i++) {
			Bit.xor(a.bits[i], b.bits[i], result.bits[i]);
		}
    }

    public void not( Word32 result) {
    	not(this, result);
    }

    public static void not(Word32 a, Word32 result) {
    	for (int i=0; i<32; i++) {
			Bit.not(a.bits[i], result.bits[i]);
		}
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Bit bit : bits) {
            sb.append(bit.toString());
        }
        return sb.toString();
    }
}
