import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Hash {
	private static final int BYTE_LENGTH = 8;
	private static int hash_length = 8;
	private static int bnum = 4;

	public static void setLengthInByte(int node_num) {
		hash_length = Math.max(hash_length, (int) Math.ceil(Math.log(node_num) / Math.log(2)));
		bnum = (int) Math.ceil(hash_length / 8.0);
		System.out.println("HashKey size = " + bnum + "Bytes");
	}

	// calculated SHA-1 hash
	public static byte[] computeHash(String input) {
		byte[] value = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.reset();
			byte[] code = md.digest(input.getBytes());
			value = new byte[bnum];
			int shrink = code.length / value.length;
			int bitCount = 1;
			for (int j = 0; j < code.length * BYTE_LENGTH; j++) {
				int currBit = ((code[j / BYTE_LENGTH] & (1 << (j % BYTE_LENGTH))) >> j
						% BYTE_LENGTH);
				if (currBit == 1)
					bitCount++;
				if (((j + 1) % shrink) == 0) {
					int shrinkBit = (bitCount % 2 == 0) ? 0 : 1;
					value[j / shrink / BYTE_LENGTH] |= (shrinkBit << ((j / shrink) % BYTE_LENGTH));
					bitCount = 1;
				}
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public static String hashToHex(byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}
	
	public static int getHashLength() {
		return hash_length;
	}
}
