import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;


public class UDPConnection {

	//Attributes
	//----------------------------------------------
	private String localAddress;	//Local Address
	private DatagramSocket socket;	//socket for connection
	private int maxDataLength;		//predefined max length of data received 
	    
	
	//Methods
	//----------------------------------------------
	
    //Constructor
    public UDPConnection()
    {
    	localAddress = "";
    	socket = null;
    	maxDataLength = 0;
    }
    
    //return the port binded
    public int getLocalPort()
    {
    	return(socket.getLocalPort());
    }
    
    //return the local IP 
    public String getLocalAddress()
    {
    	return(localAddress);
    }
    
	//Opens the socket for listening
	public String open(int localPort, int dataLength)
	{
		String status = "error";
    			
		try {						
			socket = new DatagramSocket(localPort);
			InetAddress ip = InetAddress.getLocalHost();
			
			this.maxDataLength = dataLength;
			this.localAddress = ip.getHostAddress().toString();			
			status = "Successful connection. Port: " + socket.getLocalPort();

		} catch (SocketException | UnknownHostException e) {
			status = e.getMessage();
			e.printStackTrace();
		}		
		
		return status;
	}
    
	
	//Close the port
	public void close()
	{
		if (socket != null)
		{
			localAddress = "";
			socket.disconnect();
			socket.close();
			socket = null;
		}
		System.out.println("Connection closed");
	}
			
	//receives a datagram from the opened port 
	public DatagramPacket getDatagram()
	{			
		try {
				//get the datagram from the port
				byte[] buffer = new byte[maxDataLength];	
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				socket.setSoTimeout(1000);
				socket.receive(request);   
				
				//Set the buffer to contain only the received data
				byte[] data = Arrays.copyOfRange(buffer, 0, request.getLength());
				request.setData(data);
				return request;				

	    } catch (SocketTimeoutException e) {
	    	return null;
		} catch (IOException e){
        	e.printStackTrace();
	    }
		
		return null;		
	} //End of getDatagram		
	
	
	//Generates a datagram and send the data  
	public void sendData(byte[] data, String clientAddress, int clientPort)
	{	
		try {				
				InetAddress address = InetAddress.getByName(clientAddress);				
	   	 		DatagramPacket reply = new DatagramPacket(data, data.length, address, clientPort);
	   	 		socket.send(reply);
		   	 	
	        } catch (IOException e){
	        	 System.out.println("an error");
	        	e.printStackTrace();
	        }
	}
		
}//End Class
