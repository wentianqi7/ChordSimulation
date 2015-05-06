import java.util.*;

public class FingerTable {
	List<Finger> fingers;

	FingerTable(ChordNode node) {
		fingers = new ArrayList<Finger>();

		for (int i = 0; i < Hash.getHashLength(); i++) {
			HashKey start = node.getHashKey().createStartKey(i);
			ChordNode successor = node.lookup(start, true, false);
			fingers.add(new Finger(start, successor));
		}
	}

	public Finger getFinger(int i) {
		return fingers.get(i);
	}

	public void printAll() {
		System.out.println("index | start | successor");
		for (int i = 0; i < fingers.size(); i++) {
			Finger f = fingers.get(i);
			System.out.println(i + " | " + f.getStart().toHex() + " | "
					+ f.getSuccessor().getHashKey().toHex());
		}
	}

	public Finger search(HashKey hashKey) {
		for (int i = 0; i < fingers.size() - 1; i++) {
			if (hashKey.inCurInterval(fingers.get(i).start, fingers.get(i+1).start)) {
				return fingers.get(i);
			}
		}
		return fingers.get(fingers.size() - 1);
	}
}
