import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class RPCDescriptor
 * 
 * @author rTunes team
 *
 */

public class RPCDescriptor {

	//CONSTANTS
	//------------------------
	public final static int REQUEST = 0;
	public final static int REPLY = 1;
	public final static int ACKNOWLEDGMENT = 2;
	
	//Attributes
	//------------------------
	private String sourceAddress;		//Address who made the call 
	private int sourcePort;				//Port to send back the reply
	private int messageType;			//0=Request, 1=Reply, 2=Acknowledgment
	private int requestId;				//a value that identifies the request
	private int userId;					//user identification (0 if the user is not logged)
	private int operationId;			//Id of the procedure to be executed	
	private String arguments;			//a JSON string containing the arguments for the operation
	
	
	//Methods
	//-----------------------------	
	
	//creates an empty descriptor
	public RPCDescriptor()
	{
		reset();
	}
	
	//creates a descriptor with a predefined operation and arguments
	public RPCDescriptor (String address, int port, int type, int requestId, int userId, int operation, String arguments)
	{
		reset();
		this.sourceAddress = address;
		this.sourcePort = port;
		this.messageType = type;
		this.requestId =requestId;
		this.userId = userId;
		this.operationId= operation;		
		this.arguments = arguments;		
	}
	
	//creates a descriptor from a datagram
	//empty if the unmarshall operation fails
	public RPCDescriptor (DatagramPacket datagram)
	{
		if (!unmarshall(datagram))
			reset();
	}
	
	//Set to the default values the attributes of RPC
	public void reset()
	{
		sourceAddress = "";
		sourcePort =0;
		messageType = 0;
		requestId=0;
		userId = 0;
		operationId = 0;		
		arguments="{}";
	}	
	
	//return the address of the source
	public String getSourceAddress()
	{
		return (sourceAddress);
	}
	
	//return the port of the source
	public int getSourcePort()
	{
		return(sourcePort);
	}
	
	//return the arguments
	public String getArguments()
	{
		return arguments;
	}
	
	//return id operation
	public int getOperationId()
	{
		return operationId;
	}
	
	//return id of the request
	public int getRequestId()
	{
		return (requestId);
	}
	
	//return the userId that send the RPC
	public int getUserId()
	{
		return (userId);
	}
	
	//return the type of message
	public int getMessageType()
	{
		return(messageType);
	}
	
	//set the type of message
	public void setMessageType(int type)
	{
		messageType = type;
	}
	
	//Return true if both descriptors have same attributes that identifies it
	public boolean haveSameSignature(RPCDescriptor rpc)
	{
		if(	requestId == rpc.requestId && 
		    userId == rpc.userId)
			return true;
	
		//otherwise
		return false;		
	}
	
	//return true if the argument is replying this rpc
	public boolean isRepliedBy(RPCDescriptor rpc)
	{
		if(rpc.messageType==REPLY && haveSameSignature(rpc))
			return true;
		
		//otherwise
		return false;		
	}

	//return true if the argument is an acknowledgment of this rpc
	public boolean isAcknowledgedBy(RPCDescriptor rpc)
	{
		if(rpc.messageType==ACKNOWLEDGMENT && haveSameSignature(rpc))
			return true;
		
		//otherwise
		return false;		
	}
	
	//return true if the argument is a duplicated of this rpc
	public boolean isDuplicatedBy(RPCDescriptor rpc)
	{
		if(haveSameSignature(rpc) && messageType == rpc.messageType)
			return true;
			
		//otherwise
		return false;		
	}
	
	//unflat the descriptor from a datagram
	//Pre-condition: The RPC comes in a JSON format  
	public boolean unmarshall(DatagramPacket datagram)
	{				
		boolean success = false;
		
		try {
				reset();
				String info = new String(datagram.getData()).substring(0, datagram.getLength());				
				JSONObject json = new JSONObject (info);
				
				sourceAddress = datagram.getAddress().getHostAddress();
				sourcePort = datagram.getPort();	
				messageType = json.getInt("messageType");
				requestId = json.getInt("requestId");
				userId = json.getInt("userId");
				operationId = json.getInt("operationId");				
				arguments = json.getString("arguments");
				success = true;
				
		} catch (JSONException e) {
			reset();
			e.printStackTrace();
		} catch (NullPointerException e) {
			reset();
			e.printStackTrace();
		}
		
		return (success);
	} //End of unmarshall 
	
	
	//flat the descriptor as an array of bytes containing
	//a JSON format string
	public byte[] marshall()
	{
		try {						
				JSONObject json = new JSONObject();
				json.put("messageType", messageType);				
				json.put("requestId", requestId);
				json.put("userId", userId);
				json.put("operationId", operationId);				
				json.put("arguments", arguments);				
				return (json.toString().getBytes());				
				
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return (null);		
	}	//End of marshall
			
	
	// Implements the method to compare if two objects are equal
	@Override
	public boolean equals(Object x)
	{
		if (x == this)
			return (true);
		else
		{
			if (x == null || x.getClass() != this.getClass())
				return (false);
			else
			{	RPCDescriptor ptr = (RPCDescriptor) x;
					return (this.isDuplicatedBy(ptr));
			}				
		}
	}		
	
	
	//Convert the RPC to a JSON string
	public String toJSON()
	{		
		try {						
				JSONObject json = new JSONObject();
				json.put("sourceAddress", sourceAddress);
				json.put("sourcePort", sourcePort);				
				json.put("messageType", messageType);				
				json.put("requestId", requestId);
				json.put("userId", userId);
				json.put("operationId", operationId);				
				json.put("arguments", arguments);				
				return (json.toString());				
				
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return ("");		
	}	//End of to JSON	

	
	//Set the attributes from a JSON string 
	public void setAttributes(String JSONString)
	{				
		try {
				reset();
				JSONObject json = new JSONObject (JSONString);
				sourceAddress = json.getString("sourceAddress");
				sourcePort = json.getInt("sourcePort");
				messageType = json.getInt("messageType");
				requestId = json.getInt("requestId");
				userId = json.getInt("userId");
				operationId = json.getInt("operationId");				
				arguments = json.getString("arguments");
				
			} catch (JSONException e) {
				reset();
				e.printStackTrace();
			}
	} //End of setAttributes 
	
	
	//Build a unique identifier which could be used as a file name
	public String uniqueIdentifier()
	{
		String id = String.valueOf(messageType)+ "_" + String.valueOf(userId) + "_" + String.valueOf(sourcePort) +"_" + String.valueOf(requestId);
		return(id);
	}
	

	//Load the RPC from disk (JSON format)
	//   preconditions:
	//      - The file should exist
	public void load(String fullPath, String name)
	{							
		try {
			
			//Open the file for reading
			File file = new File(fullPath + name); 		  
			BufferedReader buffer = new BufferedReader(new FileReader(file));
			
			//load the media object		
			String JSONString = buffer.readLine();
			setAttributes(JSONString);

			//Close the file
			buffer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	} //End load
		

	//Save the RPC in a file as a JSONString
	//   preconditions:
	//      - The file will be overwrite if it exists
	public void save(String fullPath)
	{						
		try {			
			//Prepares the file for writing
			File file = new File(fullPath + uniqueIdentifier() + ".json"); 		
			if (!file.exists()) file.createNewFile();			
			BufferedWriter buffer = new BufferedWriter(new FileWriter(file, false));						
			buffer.write(toJSON());

			//Close the file
			buffer.flush();
			buffer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	//End save
	
	
	//Delete a file saved as an RPC
	//   preconditions:
	//      - The folder exists
	//   postconditions:
	//      - The file is removed from the folder 
	public void delete(String fullPath)
	{				
		File file = new File(fullPath + uniqueIdentifier() + ".json"); 		
		if (file.exists()) 
			file.delete();	
	} //End of Delete	
	
	
} //End of Class
