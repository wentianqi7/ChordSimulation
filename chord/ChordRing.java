import java.util.*;

public class ChordRing {
	public final boolean SIMPLE;
	List<ChordNode> nodeList = new ArrayList<ChordNode>();
	PriorityQueue<String> nodeHeap = new PriorityQueue<String>(); 
	Map<String, ChordNode> nodeMap = new HashMap<String, ChordNode>();
	
	public ChordRing(boolean simple) {
		this.SIMPLE = simple;
	}
	
	public void addNode(ChordNode node) {
		if (nodeMap.containsKey(node.getHashKey().toHex())) {
			System.err.println("Duplicated hash key...");
		} else {
			nodeHeap.add(node.getHashKey().toHex());
			nodeList.add(node);
			nodeMap.put(node.getHashKey().toHex(), node);
		}
	}
	
	public void initRing() {
		Collections.sort(nodeList, new NodeComparator());
		for (int i = 0; i < nodeList.size(); i++) {
			int pre = (i == 0) ? nodeList.size()-1 : i - 1;
			int suc = (i == nodeList.size()-1) ? 0 : i + 1;
			nodeList.get(i).setPredecessor(nodeList.get(pre));
			nodeList.get(i).setSuccessor(nodeList.get(suc));
		}
	}
	
	public ChordNode successor(String id) {
		HashKey hashKey = new HashKey(id);
		
		return new ChordNode(id);
	}
	
	public int getCurNodeNum() {
		return nodeHeap.size();
	}
	
	public void printAllNode() {
		ChordNode start = this.getRingStart(), cur = start;
		StringBuilder sb = new StringBuilder();
		do {
			sb.append(cur.getHashKey().toHex());
			sb.append("==>");
			cur.printInfo();
			cur.fixFingers();
			cur = cur.getSuccessor();
		} while (cur != start);
		sb.append("(to the head)\n");
		
		do {
			sb.append(cur.getHashKey().toHex());
			sb.append("==>");
			cur = cur.getPredecessor();
		} while (cur != start);
		sb.append("(to the head)");
		System.out.println(sb.toString());
	}
	
	public ChordNode getNode(byte[] hash) {
		String hex = Hash.hashToHex(hash);
		if (nodeMap.containsKey(hex)) {
			return nodeMap.get(hex);
		} else {
			System.err.println("Node not in the Chord Ring");
			return null;
		}
	}
	
	public ChordNode getRingStart() {
		return nodeMap.get(nodeHeap.peek());
	}
	
	/**
	 * Join a chord ring containing newNode
	 * @param newNode
	 */
	public void join(ChordNode newNode) {
		newNode.setPredecessor(null);
		newNode.setSuccessor(nodeMap.get(nodeHeap.peek()).lookup(newNode.getHashKey(), this.SIMPLE, true));
		nodeHeap.add(newNode.getHashKey().toHex());
		nodeMap.put(newNode.getHashKey().toHex(), newNode);
	}
	
	/**
	 * A node departure from the chord ring
	 * @param newNode
	 */
	public void leave(ChordNode node) {
		node.setSuccessor(null);
		node.setPredecessor(null);
		nodeMap.remove(node.getHashKey().toHex());
		nodeHeap.remove(node.getHashKey().toHex());
	}
}

class NodeComparator implements Comparator<ChordNode> {
	@Override
	public int compare(ChordNode cn1, ChordNode cn2) {
		return cn1.getHashKey().compareTo(cn2.getHashKey());
	}
}
