public class Bit {
    public enum boolValues { FALSE, TRUE }
    private boolValues bool;

    public Bit(boolean value) {  //this is one bit, with bool representing 1 or 0.
    	if (value) {
    		this.bool = boolValues.TRUE; //here it is 1.
    	} else {
    		this.bool = boolValues.FALSE; //here it is 0.	
    	}
    }

    public boolValues getValue() {
        return this.bool;
    }

    public void assign(boolValues value) {
    	this.bool = value;
    }

    public void and(Bit b2, Bit result) {
    	and(this, b2, result); //use function below
    }

    public static void and(Bit b1, Bit b2, Bit result) {
    	if (b1.bool == boolValues.TRUE) {
			if (b2.bool == boolValues.TRUE) {
				result.assign(boolValues.TRUE);
			}
		} else {
			result.assign(boolValues.FALSE);
		}
    }

    public void or(Bit b2, Bit result) {
    	or(this, b2, result); //use function below
    }

    public static void or(Bit b1, Bit b2, Bit result) {
    	if (b1.bool == boolValues.TRUE) {
    		result.assign(boolValues.TRUE);
		} else if (b2.bool == boolValues.TRUE) {
			result.assign(boolValues.TRUE);
		} else {
			result.assign(boolValues.FALSE);
		}
    }

    public void xor(Bit b2, Bit result) {
    	xor(this, b2, result); //use function below
    }

    public static void xor(Bit b1, Bit b2, Bit result) {
    	if (b1.bool == boolValues.TRUE) {
			if (b2.bool == boolValues.FALSE) {
				result.assign(boolValues.TRUE); //if a true, b false
			} else {
				result.assign(boolValues.FALSE); //both true
			}
		} else if (b1.bool == boolValues.FALSE) { //if a false, b true
			if (b2.bool == boolValues.TRUE) {
				result.assign(boolValues.TRUE);
			} else {
				result.assign(boolValues.FALSE); //both false
			}
		}
    }

    public static void not(Bit b2, Bit result) {
    	if (b2.bool == boolValues.FALSE) {
    		result.assign(boolValues.TRUE);
		} else {
			result.assign(boolValues.FALSE);
		}
    }

    public void not(Bit result) {
    	not(this, result);
    }

    public String toString() {
    	if (this.bool == boolValues.TRUE) {
			return "t";
		}
		return "f";
    }
}
