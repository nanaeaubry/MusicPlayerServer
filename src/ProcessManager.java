import java.io.File;
import java.net.DatagramPacket;
import java.util.ArrayList;

/**
 * 
 * @author rTunes team
 *
 */
public class ProcessManager implements Runnable {

	// Attributes
	// --------------------
	private static ArrayList<RPCDescriptor> rpcList = null; // descriptors received
	private boolean running; // True when running
	private boolean logsOn; // False by default

	// Methods
	// --------------------

	// Initialize the manager
	public ProcessManager() {
		running = false;
		logsOn = false;

		if (rpcList == null)
			rpcList = new ArrayList<RPCDescriptor>();
	}

	// Returns True if the thread is running
	public boolean isRunning() {
		return (running);
	}

	// stop the service
	public void stop() {
		running = false;
	}

	// True if logs are on
	public boolean areLogsOn() {
		return (logsOn);
	}

	// Activate the logs
	public void turnLogsOn() {
		logsOn = true;
		System.out.println("Logs are " + (logsOn ? "ON" : "OFF"));
	}

	// Deactivate the logs
	public void turnLogsOff() {
		logsOn = false;
		System.out.println("Logs are " + (logsOn ? "ON" : "OFF"));
	}

	// remove a RPC from the queue
	public static void removeRPC(RPCDescriptor rpc) {
		rpcList.remove(rpc);
	}

	// recover the information from disk
	public void recoverFromDisk() {
		// remove previous elements in the list
		System.out.println("Recovering from disk. It removes previous RPCs in memory...");
		rpcList.clear();

		// Get the folder's contain
		File folder = new File(Session.logsInRPC);
		String[] listOfFiles = folder.list();

		// load only JSON files
		for (int i = 0; i < listOfFiles.length; i++) {
			// If it is a JSON file and is not a null RPC
			if (!listOfFiles[i].startsWith("0_0") && listOfFiles[i].indexOf(".json") > 0) {
				RPCDescriptor rpc = new RPCDescriptor();
				rpc.load(Session.logsInRPC, listOfFiles[i]);
				rpcList.add(rpc);
				Thread task = new Thread(new Process(rpc, logsOn));
				task.start();
				System.out.println("Recovered from disk RPC: " + listOfFiles[i]);
			}
		}
	}

	// Attempt to send the RPC previously executed
	public boolean atMostOnce(RPCDescriptor rpc) {
		// Verifies if was executed before
		File file = new File(Session.logsExecRPC + rpc.uniqueIdentifier() + ".json");
		if (file.exists()) {
			// Verifies if the output exists
			String replied = "1" + rpc.uniqueIdentifier().substring(1);
			file = new File(Session.logsOutRPC + replied + ".json");
			if (file.exists()) {
				RPCDescriptor rpcOut = new RPCDescriptor();
				rpcOut.load(Session.logsOutRPC, replied + ".json");
				Sender.insert(rpcOut);
				System.out.println("Sending previously executed RPC : " + replied);
				return (true);
			}
		}

		return (false);
	}

	// Executes the process manager
	@Override
	public void run() {

		System.out.println("Process manager running.");
		System.out.println("Logs are " + (logsOn ? "ON" : "OFF"));

		running = true;
		while (running) {
			try { // Sleep for a millisecond
				Thread.sleep(1);

				// If the queue has an element it is executed
				if (!Receiver.emptyQueue()) {
					// Get a datagram from the queue
					DatagramPacket datagram = Receiver.pop();
					RPCDescriptor rpc = new RPCDescriptor();

					// Try to get the RPC from the datagram
					if (rpc.unmarshall(datagram)) {
						// DUPLICATE FILTERING
						// Discarding duplicate messages
						if (!rpcList.contains(rpc)) {
							// Attempt to send a previous execution if exists
							// if (!atMostOnce(rpc))
							{
								rpcList.add(rpc);
								Thread task = new Thread(new Process(rpc, logsOn));
								task.start();

								// keep a log of the incoming RPCs
								if (logsOn)
									rpc.save(Session.logsInRPC);
							}
						} else {
							System.out.println("Process manager message: a duplicated RPC was received");
							System.out.println(
									"Discarted: " + rpc.getOperationId() + " arguments: " + rpc.getArguments());
						}
					} else {
						System.out.println("Process manager message: a invalid RPC was received");
						System.out.println("Datagram: " + new String(datagram.getData()));
					}
				} // End if

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

		} // End of while loop

		// Service was stopped
		System.out.println("Process manager stopped.");

	} // End of run

}// End of Class
