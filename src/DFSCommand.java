
import java.util.*;

/**
 * Class that acts as the main for the program. 
 * @author nanaeaubry
 *
 */

public class DFSCommand {
	DFS dfs;
	Boolean initialized = false;
	Boolean running = true;
	String TAG = "DFSCommand";

	static public void main(String args[]) throws Exception {
		if (args.length < 1) {
			throw new IllegalArgumentException("Parameter: <port> <portToJoin>");
		}
		if (args.length > 1) {
			DFSCommand dfsCommand = new DFSCommand(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
		} else {
			DFSCommand dfsCommand = new DFSCommand(Integer.parseInt(args[0]), 0);
		}
	}

	public DFSCommand(int p, int portToJoin) throws Exception {
		dfs = new DFS(p);
		Server server = null;

		if (portToJoin > 0) {
			// A node in the chord
			System.out.println("Joining " + portToJoin);
			dfs.join("127.0.0.1", portToJoin);
		} else {
			// Controller
			// Start server
			server = new Server(dfs);
		}

		// Show a Menu for interaction with the user
		int run = 1;
		Scanner keyboard = new Scanner(System.in);

		do {
			System.out.println("\n\n");
			System.out.println("********** Menu Server **************");
			System.out.println("x. Shut down the client and quit.    ");
			System.out.println("*************************************");
			System.out.println("Select an option: ");

			// read the first character from the keyboard's buffer
			String buffer = keyboard.nextLine();
			if (buffer.length() > 0) {
				run = 0;
			}
		} while (run == 1);
		
		keyboard.close();

		if (server != null) {
			server.stop();
		}
		
		dfs.leave();
		
		System.out.println("Bye Bye");
	
	}
}
