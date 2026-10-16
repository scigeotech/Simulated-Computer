
public class InstructionCache {
	private Word32[] cache = new Word32[8];
	private Word32 first = new Word32(); //an additional word – the address represented by the first of these 8 words
	Memory memory;
	L2Cache l2;
	Processor processor;
	
	public InstructionCache(Memory memory, L2Cache l2, Processor processor) {
        this.memory = memory;
        this.l2 = l2;
        this.processor = processor;
        for (int i = 0; i < 8; i++) {
        	cache[i] = new Word32();
        }
    }
	
	public Word32 read(Word32 address) {
        int addrInt = Processor.toInt(address.toString(), false);
        int addrCachePos = cachePos(address);
        if (addrCachePos == -1) { //addr not found
        	//If the address is NOT in the cache, fill the cache (all 8 words) 
        	//from main memory so that the address IS in the cache. Then return the result.
        	for (int i=0; i<8; i++) {//fill slots 1-8 starting with address
        		Processor.fromInt(addrInt + i, memory.address);
        		memory.read();
        		memory.value.copy(cache[i]);
        	}
        	cache[0].copy(first);
        	Processor.incrementClock(350); //A cache miss takes 350 clock cycles. 
        	return first;
        } else { //addr found - When a read happens, if the address that is read is in the cache, return the result. 
        	Processor.incrementClock(10); //A cache hit takes 10 clock cycles.
        	return cache[addrCachePos];
        }
    }
	
	private int cachePos(Word32 addr) {
		for (int i=0; i<8; i++) {
			if (addr.equals(cache[i])) {
				return i;
			}
		}
		return -1;
	}
}
