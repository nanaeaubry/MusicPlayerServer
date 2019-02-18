
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Server {

	// CONSTANTS
	private static final int SERVER_PORT = 2048;
	private static final int MAX_DATAGRAM_SIZE = 60000; // same as the client

	// Main Program
	public static void main(String[] args) {
		
		// Open a connection
		UDPConnection connection = new UDPConnection();
		connection.open(SERVER_PORT, MAX_DATAGRAM_SIZE);
		System.out.println("Server IP: " + connection.getLocalAddress() + " Server port: " + SERVER_PORT);

		// Dispatcher system
		Dispatcher dispatcher = new Dispatcher();
		dispatcher.registerService("LoginService", new LoginService());
		dispatcher.registerService("CatalogService", new CatalogService());
		dispatcher.registerService("UserService", new UserService());

		// Create the services
		Receiver receiver = new Receiver(connection);
		Sender sender = new Sender(connection);
		RPCManager rpcManager = new RPCManager(dispatcher);

		// Create the services
		Thread taskReceiver = new Thread(receiver);
		Thread taskSender = new Thread(sender);
		Thread taskRPCManager = new Thread(rpcManager);

		// Run the services as threads
		taskReceiver.start();
		taskSender.start();
		taskRPCManager.start();

		// Show a Menu for interaction with the user
		char key;
		Scanner keyboard = new Scanner(System.in);

		do {
			System.out.println("\n\n");
			System.out.println("********** Menu Server *************");
			System.out.println("1. Stop/Start Receiver.             ");
			System.out.println("2. Stop/Start Sender.               ");
			System.out.println("3. Stop/Start RPC Manager.      ");
			System.out.println("------------------------------------");
			System.out.println("x. Shut down the server and quit.   ");
			System.out.println("************************************");
			System.out.println("Select an option: ");

			// read the first character from the keyboard's buffer
			String buffer = keyboard.nextLine();
			key = 0;
			if (buffer.length() > 0) {
				key = buffer.toLowerCase().charAt(0);
				switch (key) {
				case '1':
					if (receiver.isRunning())
						receiver.stop();
					else {
						taskReceiver = new Thread(receiver);
						taskReceiver.start();
					}
					break;

				case '2':
					if (sender.isRunning())
						sender.stop();
					else {
						taskSender = new Thread(sender);
						taskSender.start();
					}
					break;

				case '3':
					if (rpcManager.isRunning())
						rpcManager.stop();
					else {
						taskRPCManager = new Thread(rpcManager);
						taskRPCManager.start();
					}
					break;

				}
			}
		} while (key != 'x'); // until it is a valid key

		// close resources
		keyboard.close();
		rpcManager.stop();
		receiver.stop();
		sender.stop();
		connection.close();

	}// End of Main

}
