import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Hash {
	private static final int BYTE_LENGTH = 8;
	private static int hash_length = 16;
	private static int bnum = 4;

	/**
	 * set the hash key length according to max node number
	 * @param node_num
	 */
	public static void setLengthInByte(int node_num) {
		hash_length = Math.max(hash_length, (int) Math.ceil(Math.log(node_num) / Math.log(2)));
		bnum = (int) Math.ceil(hash_length / (double)BYTE_LENGTH);
		System.out.println("HashKey size = " + bnum + "Bytes");
	}

	/**
	 * calculated SHA-1 hash
	 * @param input
	 * 		the identifier
	 * @return the hash value of the identifier
	 */
	public static byte[] computeHash(String input) {
		byte[] value = null;
		try {
			// calculate initial SHA-1 hash
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.reset();
			byte[] init_code = md.digest(input.getBytes());
			value = new byte[bnum];
			int shrink = init_code.length / value.length;
			int count = 1;
			// shrink the SHA-1 hash to bnum size
			for (int i = 0; i < init_code.length * BYTE_LENGTH; i++) {
				int cur = ((init_code[i / BYTE_LENGTH] & (1 << (i % BYTE_LENGTH))) >> i
						% BYTE_LENGTH);
				if (cur == 1) {
					count++;
				}
				if (((i + 1) % shrink) == 0) {
					int temp = (count % 2 == 0) ? 0 : 1;
					count = 1;
					value[i / shrink / BYTE_LENGTH] |= (temp << ((i / shrink) % BYTE_LENGTH));
				}
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	/**
	 * convert the byte[] hash to hex string
	 * @param hash
	 * @return hex string
	 */
	public static String hashToHex(byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}
	
	/**
	 * @return the length of the hash applying in the current system
	 */
	public static int getHashLength() {
		return hash_length;
	}
	
	/**
	 * @return the byte number of the hash key
	 */
	public static int getByteNum() {
		return bnum;
	}
}
