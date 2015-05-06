
public class Finger {
	HashKey start;
	ChordNode successor;
	
	Finger(HashKey start, ChordNode successor) {
		this.start = start;
		this.successor = successor;
	}
	
	public void setStart(HashKey start) {
		this.start = start;
	}
	
	public HashKey getStart() {
		return this.start;
	}
	
	public void setSuccessor(ChordNode successor) {
		this.successor = successor;
	}
	
	public ChordNode getSuccessor() {
		return this.successor;
	}
}
