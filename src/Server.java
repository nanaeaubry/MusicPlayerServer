
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Main Class for server. Will provide menu with options from terminal
 * 
 * @author nanaeaubry
 *
 */

public class Server {

	// CONSTANTS
	private static final int SERVER_PORT = 2048;
	private static final int MAX_DATAGRAM_SIZE = 60000; // same as the client
	
	private RPCManager rpcManager;
	private Sender sender;
	private Receiver receiver;
	private UDPConnection connection;
	
	
	// Constructor
	public Server(DFS dfs) {

		// Open a connection
		connection = new UDPConnection();
		connection.open(SERVER_PORT, MAX_DATAGRAM_SIZE);
		System.out.println("Server IP: " + connection.getLocalAddress() + " Server port: " + SERVER_PORT);

		// Dispatcher system
		Dispatcher dispatcher = new Dispatcher();
		dispatcher.registerService("LoginService", new LoginService());
		dispatcher.registerService("CatalogService", new CatalogService(dfs));
		dispatcher.registerService("UserService", new UserService());
		dispatcher.registerService("SongService", new SongService());

		// Create the receiver and sender
		receiver = new Receiver(connection);
		sender = new Sender(connection);
		rpcManager = new RPCManager(dispatcher);

		// Create the services
		Thread taskReceiver = new Thread(receiver);
		Thread taskSender = new Thread(sender);
		Thread taskRPCManager = new Thread(rpcManager);

		// Run the services as threads
		taskReceiver.start();
		taskSender.start();
		taskRPCManager.start();

	}

	public void stop() {
		// close resources
		rpcManager.stop();
		receiver.stop();
		sender.stop();
		connection.close();
	}

}
