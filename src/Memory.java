public class Memory {
    public Word32 address= new Word32();
    public Word32 value = new Word32();

    private final Word32[] dram= new Word32[1000];

    public int addressAsInt() { //0 - 999, 999 in binary is 1111100111 (10 digits)
    	String binary = address.toString(); //ttfftfft...
    	binary = binary.replace('f', '0');
    	binary = binary.replace('t', '1');
    	
    	int result = 0, position = 0;
    	for (int i = binary.length() - 1; i >= 22; i--) { //32 - 10 = 22
    		if (binary.charAt(i) == '1') {
    			result += (int)Math.pow(2, position);
    		}
    		position++;
    	}
    	
        return result;
    }

    public Memory() {
    	for (int i=0; i<1000; i++) {
    		dram[i] = new Word32();
    	}
    }

    public void read() {
    	int memAddr = addressAsInt(); //read memory address as int
    	dram[memAddr].copy(value); //set value to memory at address
    }

    public void write() {
    	int memAddr = addressAsInt(); //read memory address as int
    	value.copy(dram[memAddr]); //set memory at address to value
    }

    public void load(String[] data) {
    	for (int i=0; i<data.length; i++) {
    		//int to address
    		for (int k = 31; k >= 0; k--) {
        		if ((i & (1 << k)) != 0) {
        			address.setBitN(31 - k, new Bit(true));
        		} else {
        			address.setBitN(31 - k, new Bit(false));
        		}
        	}
    		
    		//write
    		if (data[i].length() == 32) {
    			for (int j=0; j<32; j++) { //fill in "value" with data instance
    				switch (data[i].charAt(j)) {
    				case 't':
    					value.setBitN(j, new Bit(true));
    					break;
    				case 'f':
    					value.setBitN(j, new Bit(false));
    					break;
    				}
    			}
    		} else {
    			throw new IllegalArgumentException("string length not 32");
    		}
    		write(); //use existing write function
    	}
    }
}
