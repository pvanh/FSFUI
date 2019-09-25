package skymine;

/**
 * This class represents an Element of a utility list as used by the HUI-Miner algorithm.
 * 
 * @author Jerry Chun-Wei Lin, Lu Yang, Philippe Fournier-Viger
 */

class ExtentElement {
	// The three variables as described in the paper:
	/** transaction id */
	final int tid ;   
	/** itemset utility */
	final int itemSetutils; 
          // Bo sung item util
        int itemUtils; 
	/** remaining utility */
	final int rutils; 
	
	/**
	 * Constructor.
	 * @param tid  the transaction id
	 * @param itemSetutils  the itemset utility
	 * @param rutils  the remaining utility
	 */
	public ExtentElement(int tid, int itemSetutils, int rutils){
		this.tid = tid;
		this.itemSetutils = itemSetutils;
		this.rutils = rutils;
	}
         public ExtentElement(int tid, int itemSetutils, int itemUtils, int rutils){
		this.tid = tid;
		this.itemSetutils = itemSetutils;                
                this.itemUtils=itemUtils;
		this.rutils = rutils;
	}
}

