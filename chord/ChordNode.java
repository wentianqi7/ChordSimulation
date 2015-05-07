
public class ChordNode {
	private String nid;
	private HashKey hashKey;
	private ChordNode predecessor, successor, secondSuccessor;
	private FingerTable fingerTable;
	private boolean valid;

	public ChordNode(String nid) {
		this.nid = nid;
		this.hashKey = new HashKey(nid);
		this.predecessor = null;
		this.successor = this;
		this.fingerTable = new FingerTable(this);
		this.valid = true;
	}

	public void printInfo() {
		System.out.println("Node ID: " + this.nid + ", Hash Key: "
				+ this.hashKey.toHex());
	}

	/**
	 * This method is used to lookup successor(hashKey) in the chord ring
	 * 
	 * @param hashKey
	 *            the hashKey of target's identifier
	 * @param simple
	 *            if true, use single key location. otherwise, use scalable key
	 *            location
	 * @return successor(hashKey)
	 */
	public ChordNode lookup(HashKey hashKey, boolean simple, boolean verbose) {
		if (verbose) {
			System.out.println("Tracing... " + this.nid);
		}
		if (hashKey.inCurInterval(this.hashKey, this.successor.hashKey)) {
			return this.successor;
		}
		if (simple) {
			// successor of current node not match, move to the next
			return this.successor.lookup(hashKey, simple, verbose);
		} else {
			ChordNode temp = this.getFinger(hashKey);
			if (temp.hashKey.toDecimal() == hashKey.toDecimal()) {
				return temp;
			} else {
				return temp.lookup(hashKey, simple, verbose);
			}
		}
	}
	
	/**
	 * @param hashKey
	 * @return the result of searching finger table 
	 */
	public ChordNode getFinger (HashKey hashKey){
		return this.fingerTable.search(hashKey).successor;
	}

	/**
	 * periodically verifies n's immediate successor and tells the successor
	 * about n
	 */
	public void stabilize() {
		ChordNode newAdd = this.successor.predecessor;
		if (newAdd != null && newAdd.checkValid()) {
			HashKey hashKey = newAdd.hashKey;
			// if key of oldPre in interval (this, successor)
			if (hashKey
					.inCurInterval(this.hashKey, this.successor.hashKey)) {
				this.successor = newAdd;
				this.secondSuccessor = newAdd.successor;
				this.predecessor.secondSuccessor = this.successor;
			}
		} else if (!newAdd.checkValid()) {
			this.secondSuccessor = this.successor.successor;
			this.predecessor.secondSuccessor = this.successor;
		}
		this.successor.notify(this);
	}

	/**
	 * node thinks it might be our predecessor
	 * 
	 * @param node
	 */
	public void notify(ChordNode node) {
		HashKey hashKey = node.getHashKey();
		if (this.predecessor == null || !this.predecessor.checkValid()
				|| hashKey.inCurInterval(this.predecessor.hashKey,
						this.hashKey)) {
			this.predecessor = node;
		}
	}

	/**
	 * called periodically, refreshes finger table entries next stores the index
	 * of the next finger to fix
	 */
	public void fixFingers() {
		fingerTable = new FingerTable(this);
	}

	/**
	 * called periodically. checks whether the node has failed
	 */
	public boolean checkValid() {
		// if predecessor failed
		return this.valid;
	}
	
	public void printFingerTable() {
		fingerTable.printAll();
	}
	
	/*
	 * getters and setters
	 */
	
	public void setSuccessor(ChordNode successor) {
		this.successor = successor;
	}

	public ChordNode getSuccessor() {
		return this.successor;
	}

	public void setPredecessor(ChordNode predecessor) {
		this.predecessor = predecessor;
	}

	public ChordNode getPredecessor() {
		return this.predecessor;
	}

	public HashKey getHashKey() {
		return this.hashKey;
	}

	public String getNodeId() {
		return this.nid;
	}
	
	public ChordNode getSecondSuccessor() {
		return this.secondSuccessor;
	}
	
	public void setSecondSuccessor(ChordNode secondSuccessor) {
		this.secondSuccessor = secondSuccessor;
	}
	
	/**
	 * set the current node to fail
	 */
	public void fail() {
		this.successor = null;
		this.secondSuccessor = null;
		this.predecessor = null;
		this.valid = false;
	}
}
