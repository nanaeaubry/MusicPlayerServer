import java.util.ArrayList;

/**
 * 
 * @author rTunes team
 *
 */
public class Sender implements Runnable {

	//Attributes
	//--------------------
	private static ArrayList<RPCDescriptor> queue = null;	//A global FIFO buffer for sending datagrams
	private boolean running;		//True when running
	UDPConnection connection;		//Connection binded to the port
	
	
	//Methods
	//--------------------	
	
	//Initialize the sender
	public Sender(UDPConnection conn)
	{
		this.running = false;
		this.connection = conn;
		
		if (queue == null)
			queue = new ArrayList<RPCDescriptor>();
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
	}

	//Add a valid RPC descriptor to the queue
	public static void insert(RPCDescriptor rpc)
	{
		if (rpc != null)
			queue.add(rpc);
	}
	
	@Override
	public void run() 
	{
		System.out.println("Sender running....");		
		running = true;			
		while (running)				
		{
			try {							
					//Sleep for a millisecond
					Thread.sleep(1);
				
					//If the queue has an element it is send
					if(queue.size()>0)
					{
						RPCDescriptor rpc = queue.remove(0);	
						if (rpc != null)
						{
							connection.sendData(rpc.marshall(), rpc.getSourceAddress(), rpc.getSourcePort());					
							System.out.println("Sender: RPC sent to " + rpc.getSourceAddress() + " port: " +
									rpc.getSourcePort() + " data: " + new String(rpc.marshall()));
						}
					}
					
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}					
		}// End of while
				
		System.out.println("Sender stopped.");
		
	} //End of run
	
} // End Class
