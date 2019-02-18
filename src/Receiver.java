import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class Receiver Opens a connection port and waits for datagrams. It runs in a
 * thread. The received datagrams are stored in a static FIFO queue The
 * datagrams can be retrieved from the queue by the static function pop()
 * 
 * @author nanaeaubry
 *
 */
public class Receiver implements Runnable {

	// Attributes
	// --------------------
	private static List<DatagramPacket> queue = null; // A global FIFO buffer for coming datagrams
	private boolean running; // True when running
	UDPConnection connection; // Connection binded to the port

	// Methods
	// --------------------

	// Initialize the receiver
	public Receiver(UDPConnection conn) {
		this.running = false;
		this.connection = conn;

		if (queue == null)
			queue = Collections.synchronizedList(new ArrayList<DatagramPacket>());
	}

	// Returns True if the thread is running
	public boolean isRunning() {
		return (running);
	}

	// stop the service
	public void stop() {
		running = false;
	}

	// Pop a datagram from the FIFO queue
	public static DatagramPacket pop() {
		synchronized (queue) {
			if (queue.size() > 0) {
				return queue.remove(0);
			}

			return null;
		}
	}

	// Wait for datagrams from the listening port
	@Override
	public void run() {
		running = true;
		System.out.println("Receiver running. Waiting for a datagram...");
		while (running) {
			DatagramPacket received = connection.getDatagram();
			if (received == null) {
				continue;
			}
			synchronized (queue) {
				queue.add(received);
			}
			System.out.println("Datagram received: " + new String(received.getData()));
		}

		System.out.println("Receiver stopped.");

	} // End of run

} // End of class
