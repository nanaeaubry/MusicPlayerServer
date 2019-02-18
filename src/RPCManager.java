import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author nanaeaubry
 *
 */
public class RPCManager implements Runnable {

	// Attributes
	// --------------------
	private static List<RPCDescriptor> rpcList = null; // descriptors received
	private boolean running; // True when running
	private Dispatcher dispatcher;

	// Methods
	// --------------------

	// Initialize the manager
	public RPCManager(Dispatcher dispatcher) {
		this.running = false;
		this.dispatcher = dispatcher;

		if (rpcList == null)
			rpcList = Collections.synchronizedList(new ArrayList<RPCDescriptor>());
	}

	// Returns True if the thread is running
	public boolean isRunning() {
		return (running);
	}

	// stop the service
	public void stop() {
		running = false;
	}

	// remove a RPC from the queue
	public static void removeRPC(RPCDescriptor rpc) {
		synchronized (rpcList) {
			rpcList.remove(rpc);
		}
	}


	// Executes the RPC manager
	@Override
	public void run() {

		System.out.println("RPC manager running.");

		running = true;
		while (running) {
			try { // Sleep for a millisecond
				Thread.sleep(1);

				// Get datagram from receiver, if null then go back to waiting
				DatagramPacket datagram = Receiver.pop();
				if (datagram == null) {
					continue;
				}

				// Try to get the RPC from the datagram
				RPCDescriptor rpc = new RPCDescriptor();
				if (!rpc.unmarshall(datagram)) {
					System.out.println("RPC manager message: a invalid RPC was received");
					System.out.println("Datagram: " + new String(datagram.getData()));
					continue;
				}

				synchronized (rpcList) {
					// Discarding duplicate messages
					if (rpcList.contains(rpc)) {
						System.out.println("RPC manager message: a duplicated RPC was received");
						System.out.println("Discarted execute: " + rpc.getExecute());
						continue;
					}

					// Save this rpc to avoid duplicate eventually
					rpcList.add(rpc);
				}

				// Create a process to dispatch the RPC
				Thread task = new Thread(new Process(this.dispatcher, rpc));
				task.start();

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

		} // End of while loop

		// Service was stopped
		System.out.println("RPC manager stopped.");

	} // End of run

}// End of Class
