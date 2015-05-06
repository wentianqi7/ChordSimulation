import java.util.*;

public class ChordNode {
	private String nid;
	private HashKey hashKey;
	private ChordNode predecessor, successor;
	private FingerTable fingerTable;

	public ChordNode(String nid) {
		this.nid = nid;
		this.hashKey = new HashKey(nid);
		this.predecessor = null;
		this.successor = this;
		this.fingerTable = new FingerTable(this);
	}

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
	
	public ChordNode getFinger (HashKey hashKey){
		return this.fingerTable.search(hashKey).successor;
	}

	/**
	 * periodically verifies n's immediate successor and tells the successor
	 * about n
	 */
	public void stabilize() {
		ChordNode oldPre = this.successor.predecessor;
		if (oldPre != null) {
			HashKey hashKey = oldPre.hashKey;
			// if key of oldPre in interval (this, successor)
			if (hashKey
					.inCurInterval(this.hashKey, this.successor.hashKey)) {
				this.successor = oldPre;
			}
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
		if (this.predecessor == null
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
	 * called periodically. checks whether predecessor has failed
	 */
	public void checkPredecessor() {
		// if predecessor failed
		this.predecessor = null;
	}
	
	public void printFingerTable() {
		fingerTable.printAll();
	}
}
