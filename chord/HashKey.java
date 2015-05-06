import java.util.*;

public class HashKey implements Comparable {
	private String kid;
	private byte[] hashKey;
	
	public HashKey(byte[] hashKey) {
		this.hashKey = hashKey;
	}
	
	public HashKey(String kid) {
		this.kid = kid;
		this.hashKey = Hash.computeHash(kid);
	}

	@Override
	public int compareTo(Object arg0) {
		long val1 = this.toDecimal();
		long val2 = ((HashKey)arg0).toDecimal();
		return Long.compare(val1, val2);
	}
	
	public byte[] getValue() {
		return this.hashKey;
	}
	
	public String toHex() {
		Formatter formatter = new Formatter();
		for (byte b : this.hashKey) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

	public long toDecimal() {
		String hex = this.toHex();
		return Long.parseLong(hex, 16);
	}
	
	public HashKey createStartKey(int index) {
		byte[] newKey = new byte[hashKey.length];
		System.arraycopy(hashKey, 0, newKey, 0, hashKey.length);
		int carry = 0;
		for (int i = (Hash.getHashLength() - 1) / 8; i >= 0; i--) {
			int value = hashKey[i] & 0xff;
			value += (1 << (index % 8)) + carry;
			newKey[i] = (byte) value;
			if (value <= 0xff) {
				break;
			}
			carry = (value >> 8) & 0xff;
		}
		return new HashKey(newKey);
	}
	
	/**
	 * check if this lies in interval (left, right)
	 * @param left
	 * 		current node hashkey
	 * @param right
	 * 		successor node hashkey
	 * @return
	 * 		true if this in (left, right). otherwise return false
	 */
	public boolean inCurInterval(HashKey left, HashKey right) {
		long curVal = left.toDecimal();
		long sucVal = right.toDecimal();
		long tarVal = this.toDecimal();
		
		if (curVal >= sucVal) {
			// reach end of the ring
			if (curVal < tarVal || sucVal >= tarVal) {
				return true;
			}
		} else if (curVal < tarVal && sucVal >= tarVal) {
			// find the successor
			return true;
		}
		
		return false;
	}
}
