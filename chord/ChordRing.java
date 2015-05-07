import java.util.*;

public class ChordRing {
	public final boolean SIMPLE;
	// used for initialize the chord ring, no longer used in the later steps
	List<ChordNode> initList = new ArrayList<ChordNode>();
	PriorityQueue<String> nodeHeap = new PriorityQueue<String>(); 
	Map<String, ChordNode> nodeMap = new HashMap<String, ChordNode>();
	
	public ChordRing(boolean simple) {
		this.SIMPLE = simple;
	}
	
	public int getCurNodeNum() {
		return nodeHeap.size();
	}
	
	/**
	 * print all the information of the chord ring
	 * @param verbose
	 * 		whether print node identifier for each node
	 */
	public void printAllNode(boolean verbose) {
		ChordNode start = this.getRingStart(), cur = start;
		StringBuilder sb = new StringBuilder();
		do {
			sb.append(cur.getHashKey().toHex());
			sb.append("(" + cur.getSecondSuccessor().getHashKey().toHex() + ")");
			sb.append("==>");
			if (verbose) {
				cur.printInfo();
			}
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
	
	/**
	 * get the node wrt hash key
	 * used for testing and print (no practical usage)
	 * @param hash
	 * @return the node with hashKey equals to hash
	 */
	public ChordNode getNode(byte[] hash) {
		String hex = Hash.hashToHex(hash);
		if (nodeMap.containsKey(hex)) {
			return nodeMap.get(hex);
		} else {
			System.err.println("Node not in the Chord Ring");
			return null;
		}
	}
	
	/**
	 * find the smallest hash value in the ring
	 * used for iteration and print result (no practical usage)
	 * @return the node with smallest hash value in the chord ring
	 */
	public ChordNode getRingStart() {
		return nodeMap.get(nodeHeap.peek());
	}
	
	/**
	 * Join a chord ring containing newNode
	 * @param newNode
	 */
	public void join(ChordNode newNode) {
		newNode.setPredecessor(null);
		ChordNode successor = nodeMap.get(nodeHeap.peek()).lookup(newNode.getHashKey(), this.SIMPLE, false);
		newNode.setSuccessor(successor);
		newNode.setSecondSuccessor(successor.getSuccessor());
		nodeHeap.add(newNode.getHashKey().toHex());
		nodeMap.put(newNode.getHashKey().toHex(), newNode);
	}
	
	/**
	 * A node departure from the chord ring
	 * @param newNode
	 */
	public void leave(ChordNode node) {
		node.fail();
		nodeMap.remove(node.getHashKey().toHex());
		nodeHeap.remove(node.getHashKey().toHex());
	}
	
	/**
	 * Add a node to the init list, used in initialization step only
	 * use join instead in running time instead
	 * @param node
	 */
	public void addNode(ChordNode node) {
		if (nodeMap.containsKey(node.getHashKey().toHex())) {
			System.err.println("Duplicated hash key...");
		} else {
			nodeHeap.add(node.getHashKey().toHex());
			initList.add(node);
			nodeMap.put(node.getHashKey().toHex(), node);
		}
	}
	
	/**
	 * initialize the chord ring
	 * used join in running time instead
	 */
	public void initRing() {
		Collections.sort(initList, new NodeComparator());
		for (int i = 0; i < initList.size(); i++) {
			int pre = (i == 0) ? initList.size()-1 : i - 1;
			int suc = (i + 1) % initList.size();
			int suc2 = (i + 2) % initList.size();
			initList.get(i).setPredecessor(initList.get(pre));
			initList.get(i).setSuccessor(initList.get(suc));
			initList.get(i).setSecondSuccessor(initList.get(suc2));
		}
	}
}

class NodeComparator implements Comparator<ChordNode> {
	@Override
	public int compare(ChordNode cn1, ChordNode cn2) {
		return cn1.getHashKey().compareTo(cn2.getHashKey());
	}
}
