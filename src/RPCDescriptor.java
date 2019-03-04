import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Class RPCDescriptor
 * 
 * @author nanaeaubry
 *
 */

public class RPCDescriptor {

	// Attributes
	// ------------------------
	private String sourceAddress; // Address who made the call
	private int sourcePort; // Port to send back the reply
	private long id;
	private JsonObject execute; // a JSON string containing the arguments for the operation

	// Methods
	// -----------------------------

	// creates an empty descriptor
	public RPCDescriptor() {
		reset();
	}

	// creates a descriptor with a predefined operation and arguments
	public RPCDescriptor(RPCDescriptor origin, JsonObject execute) {
		reset();
		this.sourceAddress = origin.getSourceAddress();
		this.sourcePort = origin.getSourcePort();
		this.id = origin.getSourceId();
		this.execute = execute;
	}

	// creates a descriptor from a datagram
	// empty if the unmarshall operation fails
	public RPCDescriptor(DatagramPacket datagram) {
		unmarshall(datagram);
	}

	// Set to the default values the attributes of RPC
	public void reset() {
		sourceAddress = "";
		sourcePort = 0;
		id = -1;
		execute = new JsonObject();
	}

	// return the address of the source
	public String getSourceAddress() {
		return (sourceAddress);
	}

	// return the port of the source
	public int getSourcePort() {
		return (sourcePort);
	}

	// return the arguments
	public JsonObject getExecute() {
		return execute;
	}
	
	public long getSourceId() {
		return id;
	}

	// unflat the descriptor from a datagram
	// Pre-condition: The RPC comes in a JSON format
	public void unmarshall(DatagramPacket datagram) {

		try {
			reset();
			String jsonString = new String(datagram.getData()).substring(0, datagram.getLength());

			JsonParser parser = new JsonParser();
			JsonObject message = parser.parse(jsonString).getAsJsonObject();

			sourceAddress = datagram.getAddress().getHostAddress();
			sourcePort = datagram.getPort();
			id = message.get("id").getAsLong();
			execute = message.get("execute").getAsJsonObject();

		} catch (JsonSyntaxException e) {
			reset();
			e.printStackTrace();
		} catch (NullPointerException e) {
			reset();
			e.printStackTrace();
		}

	} // End of unmarshall

	// flat the descriptor as an array of bytes containing
	// a JSON format string
	public byte[] marshall() {
		return toJson().getBytes();

	} // End of marshall


	// Convert the RPC to a JSON string
	public String toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("id", id);
		json.add("execute", execute);
		return json.toString();
	} // End of to JSON



} // End of Class
