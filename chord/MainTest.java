import java.io.*;
import java.net.*;
import java.util.PriorityQueue;

public class MainTest {
	public static int max_node_num;
	public static int init_node_num;

	public static void main(String args[]) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String host = InetAddress.getLocalHost().getHostAddress();
		int port = 9000;
		String buffer = "";

		/* Parse parameter and do args checking */
		if (args.length < 3) {
			System.err
					.println("Usage: java MainTest <max_mode_num> <init_node_num> <lookup_mode>");
			System.exit(1);
		}

		try {
			max_node_num = Integer.parseInt(args[0]);
			init_node_num = Integer.parseInt(args[1]);
			if (init_node_num > max_node_num) {
				throw new Exception();
			}
		} catch (NumberFormatException e) {
			System.err.println("The two arguments should be integers");
		} catch (Exception e) {
			System.err
					.println("Initial node number should not be greater than the max.");
		}

		System.out.println("Max node number: " + max_node_num);
		System.out.println("Initial node number: " + init_node_num);
		Hash.setLengthInByte(max_node_num);

		// whether use simple key location or scalable key location
		boolean isSimple = false;
		if (args[2].equalsIgnoreCase("simple")) {
			isSimple = true;
		} else if (args[2].equalsIgnoreCase("scalable")) {
			isSimple = false;
		} else {
			System.err
				.println("Usage: java MainTest <max_mode_num> <init_node_num> <lookup_mode>");
			System.err.println("<lookup_mode> should be [\"simple\", \"scalable\"]");
			System.exit(1);
		}
		
		// init chord ring
		ChordRing chdring = new ChordRing(isSimple);

		for (int i = 0; i < init_node_num; i++) {
			URL url = new URL("http", host, port + i, "");
			ChordNode newNode = new ChordNode(url.toString());
			chdring.addNode(newNode);
		}
		chdring.initRing();
		System.out.println(chdring.getCurNodeNum() + " nodes are created.");
		chdring.printAllNode(true);

		// waiting for testing command
		while (true) {
			try {
				buffer = br.readLine();
				String[] input = buffer.trim().split("[\t ]");

				// dealing with command
				if (input[0].equalsIgnoreCase("exit")) {
					// exit the program
					System.out.println("Bye.");
					System.exit(0);
				} else if (input[0].equalsIgnoreCase("dump")) {
					// print all information
					System.out.println("Printing all information...");
					chdring.printAllNode(true);
				} else if (input.length == 2) {
					String id = input[1];
					if (input[0].equalsIgnoreCase("fingertable")
							|| input[0].equalsIgnoreCase("ft")) {
						// print finger table of node(id)
						System.out.println("Printing finger table of N" + id
								+ "...");
						chdring.getNode(Hash.computeHash(id))
								.printFingerTable();

					} else if (input[0].equalsIgnoreCase("join")) {
						// new node join the system
						ChordNode newNode = new ChordNode(id), cur = newNode;
						chdring.join(newNode);

						do {
							cur.stabilize();
							cur = cur.getSuccessor();
						} while (cur != newNode);

						System.out.println("N"
								+ Hash.hashToHex(Hash.computeHash(id))
								+ " joins...");
						chdring.printAllNode(false);
					} else if (input[0].equalsIgnoreCase("leave")) {
						// existing node leave the system

						ChordNode toDelete = chdring.getNode(Hash
								.computeHash(id));
						if (toDelete == null) {
							System.out.println("Node does not exist...");
						} else {
							chdring.leave(toDelete);
							ChordNode start = chdring.getRingStart(), cur = start;
							do {
								if (!cur.getSuccessor().checkValid()) {
									cur.setSuccessor(cur.getSecondSuccessor());
								}
								cur.stabilize();
								cur = cur.getSuccessor();
							} while (cur != start);

							System.out.println("N" + id + " leaves...");
							chdring.printAllNode(false);
						}
					}
				} else if (input.length == 3
						&& input[0].equalsIgnoreCase("lookup")) {
					// lookup key(id2) from node(id1)
					byte[] srcHash = Hash.computeHash(input[1]);
					byte[] destHash = Hash.computeHash(input[2]);
					ChordNode src = chdring.getNode(srcHash);
					if (src == null)
						continue;
					System.out.println("Identifier: " + src.getNodeId()
							+ ", Hash Key: " + src.getHashKey().toHex());
					System.out.println("Lookup for Identifier: " + input[2]
							+ ", Hash Key: " + Hash.hashToHex(destHash));

					src.lookup(new HashKey(destHash), isSimple, true).printInfo();
				} else {
					System.out.println("Command not found: " + buffer);
				}
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
		}
	}
}