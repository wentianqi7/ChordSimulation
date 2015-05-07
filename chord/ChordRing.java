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
			int suc = (i + 1) % nodeList.size();
			int suc2 = (i + 2) % nodeList.size();
			nodeList.get(i).setPredecessor(nodeList.get(pre));
			nodeList.get(i).setSuccessor(nodeList.get(suc));
			nodeList.get(i).setSecondSuccessor(nodeList.get(suc2));
		}
	}
	
	public int getCurNodeNum() {
		return nodeHeap.size();
	}
	
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
}

class NodeComparator implements Comparator<ChordNode> {
	@Override
	public int compare(ChordNode cn1, ChordNode cn2) {
		return cn1.getHashKey().compareTo(cn2.getHashKey());
	}
}
