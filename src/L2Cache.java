public class L2Cache {
	//This cache will be 32 Words in size – 4 groups of 8.
    private Word32[][] cache = new Word32[4][8]; 
    //4 Words to indicate what addresses are in the cache, much like the instruction cache. 
    private Word32[] cacheAddr = new Word32[4]; //starting addresses

    private Memory memory;
    private Processor processor;

    public L2Cache(Memory memory, Processor processor) {
        this.memory = memory;
        this.processor = processor;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 8; j++) {
                cache[i][j] = new Word32();
            }
            cacheAddr[i] = cache[i][0]; //reference first
        }
    }

    //When data is in L2, but not the InstructionCache, we can fill the InstructionCache from L2 for the cost of 50 cycles.
    public boolean readBlock(Word32 addr, Word32[] dest) {
        int addrInt = Processor.toInt(addr.toString(), false);
    	int[] coords = cachePos(addr);
    	int pos = 0;
    	if (coords[0] == -1) { //not found
    		for (int i=0; i<4; i++) {//fill cache starting with address
    			for (int j = 0; j < 8; j++) {
    				pos = (i * 8) + j;
    				Processor.fromInt(addrInt + pos, memory.address);
    				memory.read();
    				memory.value.copy(cache[coords[i]][j]);
        		}
    			cache[i][0].copy(cacheAddr[i]);
        	}
        	Processor.incrementClock(350); //A cache miss takes 350 clock cycles. 
    		return false;
    	} else {
    		for (int j = 0; j < 8; j++) {
    			cache[coords[0]][j].copy(dest[j]);
    		}
    		Processor.incrementClock(50);
    		return true;
    	}
    }
    
    private int[] cachePos(Word32 addr) {
    	int[] coords = new int[2];
		for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 8; j++) {
            	if (addr.equals(cache[i][j])) {
            		coords[0] = i;
            		coords[1] = j;
            		return coords;
            	}
            }
        }
		coords[0] = -1;
		coords[1] = -1;
		return coords;
	}
}
