import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  RPC Manager will manage the remote protocols from server to client
 * @author nanaeaubry
 *
 */
public class RPCManager implements Runnable {

	// Attributes
	// --------------------
	private boolean running; // True when running
	private Dispatcher dispatcher;

	// Methods
	// --------------------

	// Initialize the manager
	public RPCManager(Dispatcher dispatcher) {
		this.running = false;
		this.dispatcher = dispatcher;
	}

	// Returns True if the thread is running
	public boolean isRunning() {
		return (running);
	}

	// stop the service
	public void stop() {
		running = false;
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
				rpc.unmarshall(datagram);

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
