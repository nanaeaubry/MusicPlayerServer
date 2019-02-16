import java.net.DatagramPacket;
import java.util.ArrayList;

/**
 * Class Receiver
 *   Opens a connection port and waits for datagrams. It runs in a thread.
 *   The received datagrams are stored in a static FIFO queue
 *   The datagrams can be retrieved from the queue by the static function pop()
 *   
 * @author rTunes team
 *
 */
public class Receiver implements Runnable{
	
	//Attributes
	//--------------------
	private static ArrayList<DatagramPacket> queue = null;	//A global FIFO buffer for coming datagrams
	private boolean running;		//True when running
	UDPConnection connection;		//Connection binded to the port
	
	
	//Methods
	//--------------------	
	
	//Initialize the receiver
	public Receiver(UDPConnection conn)
	{
		this.running = false;
		this.connection = conn;
		
		if (queue == null)
			queue = new ArrayList<DatagramPacket>();
	}
		
	//Returns True if the thread is running
	public boolean isRunning()
	{
		return(running);
	}
	
	//stop the service
	public void stop()
	{
		running = false;
		RPCDescriptor rpc = new RPCDescriptor("", 0, 0, 0, 0, 0, "{Stop receiver}");
		connection.sendData(rpc.marshall(), connection.getLocalAddress(), connection.getLocalPort());
	}
	
	
	//return true if the queue is empty
	public static boolean emptyQueue()
	{
		return(queue.size()<=0);
	}
	
	//Pop a datagram from the FIFO queue
	public static DatagramPacket pop()
	{
		DatagramPacket datagram = null;
		if(queue.size() > 0 )
			datagram = queue.remove(0);

		return datagram;
	}		
	
	//Wait for datagrams from the listening port 
	@Override
	public void run() 
	{
		running = true;
		while (running)
		{			
			System.out.println("Receiver running. Waiting for a datagram...");
			DatagramPacket received = connection.getDatagram();
			queue.add(received);
			System.out.println("Datagram received: " + new String(received.getData()));					
		}			
				
		System.out.println("Receiver stopped.");
		
	} //End of run
	
} //End of class
