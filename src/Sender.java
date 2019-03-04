import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  Sender Class will send messages to the client side
 * @author nanaeaubry
 *
 */
public class Sender implements Runnable {

	// Attributes
	// --------------------
	private static List<RPCDescriptor> queue = null; // A global FIFO buffer for sending datagrams
	private boolean running; // True when running
	UDPConnection connection; // Connection binded to the port

	// Methods
	// --------------------

	// Initialize the sender
	public Sender(UDPConnection conn) {
		this.running = false;
		this.connection = conn;

		if (queue == null)
			queue = Collections.synchronizedList(new ArrayList<RPCDescriptor>());
	}

	// Returns True if the thread is running
	public boolean isRunning() {
		return (running);
	}

	// stop the service
	public void stop() {
		running = false;
	}

	// Add a valid RPC descriptor to the queue
	public static void insert(RPCDescriptor rpc) {
		if (rpc == null) {
			return;
		}
		synchronized (queue) {
			queue.add(rpc);
		}
	}

	private RPCDescriptor pop() {
		synchronized (queue) {
			if (queue.size() > 0) {
				return queue.remove(0);
			}
			return null;
		}
	}

	@Override
	public void run() {
		System.out.println("Sender running....");
		running = true;
		while (running) {
			try {
				// Sleep for a millisecond
				Thread.sleep(1);

				// If the queue has an element it is send
				RPCDescriptor rpc = pop();
				if (rpc == null) {
					continue;
				}

				connection.sendData(rpc.marshall(), rpc.getSourceAddress(), rpc.getSourcePort());
				System.out.println("Datagram sent:" + rpc.getSourceId());

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		} // End of while

		System.out.println("Sender stopped.");

	} // End of run

} // End Class
