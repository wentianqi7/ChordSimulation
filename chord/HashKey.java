import java.util.*;

public class HashKey implements Comparable {
	private String kid;		// hash key identifier
	private byte[] hashKey;
	private static final int BYTE_LENGTH = 8;

	/**
	 * Constructers
	 * @param hashKey
	 */
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
		long val2 = ((HashKey) arg0).toDecimal();
		return Long.compare(val1, val2);
	}

	public byte[] getValue() {
		return this.hashKey;
	}

	/**
	 * convert byte[] to hex string
	 * @return hex string of the hash key
	 */
	public String toHex() {
		Formatter formatter = new Formatter();
		for (byte b : this.hashKey) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

	/**
	 * convert byte[] to decimal
	 * @return decimal format of the hash key
	 */
	public long toDecimal() {
		String hex = this.toHex();
		return Long.parseLong(hex, 16);
	}

	/**
	 * calculate the start hash key value of ith finger
	 * 
	 * @param i
	 * @return the start hash key
	 */
	public HashKey createStart(int i) {
		byte[] newKey = new byte[Hash.getByteNum()];
		System.arraycopy(hashKey, 0, newKey, 0, hashKey.length);
		int carry = 0;
		for (int j = (Hash.getHashLength() - 1) / BYTE_LENGTH; j >= 0; j--) {
			int temp = hashKey[j] & 0xff;
			temp += (1 << (i % BYTE_LENGTH)) + carry;
			newKey[j] = (byte) temp;
			if (temp <= 0xff) {
				break;
			}
			carry = (temp >> BYTE_LENGTH) & 0xff;
		}
		return new HashKey(newKey);
	}

	/**
	 * check if this lies in interval (left, right)
	 * 
	 * @param left
	 *            current node hashkey
	 * @param right
	 *            successor node hashkey
	 * @return true if this in (left, right). otherwise return false
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
