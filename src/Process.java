import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author rTunes team
 *
 */
public class Process implements Runnable {

	//CONSTANTS: OPERATIONS FOR CLIENT
	private final int ACCESS_DENIED = 101;
	private final int ACCESS_PERMITTED = 102;
	private final int PLAYLIST_CREATED = 103;
	private final int PLAYLIST_EXISTS = 104;	
	private final int PLAYLIST_DELETED = 105;	
	private final int PLAYLIST_DSNT_EXIST = 106;	
	private final int SONG_ADDED = 107;
	private final int SONG_EXISTS = 108;	
	private final int SONG_DELETED = 109;	
	private final int SONG_DSNT_EXIST = 110;	
	private final int RETRIEVED_PLAYLIST = 111;	
	private final int SEGMENT = 112;	
	
	//CONSTANTS: OPERATIONS FOR SERVER
	private final int NULL_OPERATION = 0;
	private final int LOGIN = 201;
	private final int NEW_PLAYLIST = 202;
	private final int DELETE_PLAYLIST = 203;
	private final int ADD_SONG = 204;	
	private final int DELETE_SONG = 205;	
	private final int GET_PLAYLIST = 206;	
	private final int SEARCH = 207;	
	private final int GET_SEGMENT = 208;		
	
	//CONSTANTS
	//------------------------
	private final int SEGMENT_LENGTH = 1024 * 8;  //Important: Same size declared in AudioInputStream of Client
	
	//Attributes
	//--------------------
	private RPCDescriptor rpc;
	private boolean debugMode = false;
	private boolean logsOn = false;
	
	//Methods
	//--------------------	
	
	//Initialize the process	
	public Process(RPCDescriptor rpc, boolean logsOn)
	{
		this.rpc = rpc;
		this.logsOn = logsOn;
	}
	
	//Executes the process according its ID
	@Override
	public void run() {
		
		System.out.println("Running process: " + rpc.getOperationId());
		 switch (rpc.getOperationId())
		{			
			case LOGIN				: 	op_login(); break;
			case NEW_PLAYLIST		:	op_newPlaylist(); break;
			case DELETE_PLAYLIST	:	op_deletePlaylist(); break;
			case ADD_SONG			:	op_addSong(); break;
			case DELETE_SONG		:	op_deleteSong(); break;
			case GET_PLAYLIST		:	op_getPlaylist(); break;			
			case SEARCH				:	op_search(); break;
			case GET_SEGMENT		:	op_getSegment(); break;			
								
			default: System.out.println("Operation not valid: ");
					 System.out.println("ID: " + rpc.getOperationId() + " parameters: " + rpc.getArguments() );
					 break;		
		}				
	}	
	
	//***************************************************************************
	//OPERATIONS LIBRARY
	//***************************************************************************
	
	//Return the address of whom sent the RPC
	private String sourceAddress()
	{
		if (debugMode) 
			return("192.168.43.73");
		else
			return(rpc.getSourceAddress());
	}
	
	//Return the port of whom sent the RPC
	private int sourcePort()
	{
		if (debugMode) 
			return(2048);
		else
			return(rpc.getSourcePort());
	}
	
	
	//Get a string from a JSON format
	private String getStringAttribute(String attribute, String jsonFormat)
	{	String value = "";
			
		try {
				//Get the user name and password received		
				JSONObject json = new JSONObject (jsonFormat);
				value = json.getString(attribute);
		} catch (JSONException e) {
				e.printStackTrace();
		}		
			
		return(value);
	}	


	//Executes the login
	private void op_login()
	{		
		AccessControl control = new AccessControl();
		String username = getStringAttribute("username", rpc.getArguments());
		String password = getStringAttribute("password", rpc.getArguments());
		int op = ACCESS_DENIED;  //by default
		String parameter = "{}";
		
		//Attempt to login
		String uid = control.login(username, password);
		if (!uid.isEmpty())
		{	//Load the profile and the list of playlists of the user
			op = ACCESS_PERMITTED;
			UserProfile profile = new UserProfile(Session.usersPath + uid + "\\", uid );
			Library library = new Library (Session.usersPath + uid + "\\playlists\\");
			
			//Put them in the parameters
			JSONObject json = new JSONObject();
			try {			
				json.put("profile", profile.toJson());
				json.put("library", library.toJson());				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			//authorized user. Send operation: Access Permitted. Parameters = profile			
			parameter = json.toString();
		}
		
		//Add to the sender's queue
		RPCDescriptor reply = new RPCDescriptor(sourceAddress(), sourcePort(), RPCDescriptor.REPLY, rpc.getRequestId(), rpc.getUserId(), op , parameter); 
		Sender.insert(reply);
		ProcessManager.removeRPC(rpc);
		
		//Update logs
		if (logsOn)
		{
			reply.save(Session.logsOutRPC);		
			rpc.save(Session.logsExecRPC);
			rpc.delete(Session.logsInRPC);
		}		
	} // End operation 201 login

	
	//Create a new playlist
	private void op_newPlaylist()
	{
		String uid = String.valueOf(rpc.getUserId());
		String playlistName = getStringAttribute("playlistName", rpc.getArguments());
		Library library = new Library (Session.usersPath + uid + "\\playlists\\");
		int op = 0;  //by default an error occurred
		
		int result = library.create(playlistName);
		if (result == 0) op = PLAYLIST_CREATED;
		if (result == 1) op = PLAYLIST_EXISTS;
		
		//Add to the sender's queue
		RPCDescriptor reply = new RPCDescriptor(sourceAddress(), sourcePort(), RPCDescriptor.REPLY, rpc.getRequestId(), rpc.getUserId(), op, rpc.getArguments()); 
		Sender.insert(reply);
		ProcessManager.removeRPC(rpc);
		
		//Update logs
		if (logsOn)
		{
			reply.save(Session.logsOutRPC);		
			rpc.save(Session.logsExecRPC);
			rpc.delete(Session.logsInRPC);
		}			
	}
	
	//Delete playlist
	private void op_deletePlaylist()
	{
		String uid = String.valueOf(rpc.getUserId());
		String playlistName = getStringAttribute("playlistName", rpc.getArguments());
		Library library = new Library (Session.usersPath + uid + "\\playlists\\");
		int op = PLAYLIST_DSNT_EXIST;  //by default
		
		if (library.contains(playlistName))
		{
			library.delete(playlistName);
			op = PLAYLIST_DELETED;			
		}

		//Add to the sender's queue
		RPCDescriptor reply = new RPCDescriptor(sourceAddress(), sourcePort(), RPCDescriptor.REPLY, rpc.getRequestId(), rpc.getUserId(), op, rpc.getArguments()); 
		Sender.insert(reply);								
		ProcessManager.removeRPC(rpc);
		
		//Update logs
		if (logsOn)
		{
			reply.save(Session.logsOutRPC);		
			rpc.save(Session.logsExecRPC);
			rpc.delete(Session.logsInRPC);
		}				
	}
	
	//Add a Song to a play list
	private void op_addSong()
	{
		String uid = String.valueOf(rpc.getUserId());
		String playlistName = getStringAttribute("playlistName", rpc.getArguments());
		String jsonSong = getStringAttribute("song", rpc.getArguments());
		int op = PLAYLIST_DSNT_EXIST;  //by default
		
		Library library = new Library (Session.usersPath + uid + "\\playlists\\");
		if (library.contains(playlistName))
		{
			//Open the user playlist
			MediaObject song = new MediaObject(jsonSong);
			MediaObjectList playlist = new MediaObjectList(playlistName);
			playlist.load(Session.usersPath + uid + "\\playlists\\");
			
			op = SONG_EXISTS;
			if (playlist.search(song) < 0)				
			{	playlist.add(song);
				playlist.save(Session.usersPath + uid + "\\playlists\\");
				op = SONG_ADDED;
			}
		}
		
		//Add to the sender's queue
		RPCDescriptor reply = new RPCDescriptor(sourceAddress(), sourcePort(), RPCDescriptor.REPLY, rpc.getRequestId(), rpc.getUserId(), op, rpc.getArguments());
		Sender.insert(reply);
		ProcessManager.removeRPC(rpc);
		
		//Update logs
		if (logsOn)
		{
			reply.save(Session.logsOutRPC);		
			rpc.save(Session.logsExecRPC);
			rpc.delete(Session.logsInRPC);
		}				
	}	
	
	
	//Add a Song to a play list
	private void op_deleteSong()
	{
		String uid = String.valueOf(rpc.getUserId());
		String playlistName = getStringAttribute("playlistName", rpc.getArguments());
		String jsonSong = getStringAttribute("song", rpc.getArguments());
		int op = PLAYLIST_DSNT_EXIST;  //by default
		
		Library library = new Library (Session.usersPath + uid + "\\playlists\\");
		if (library.contains(playlistName))
		{
			//Open the user playlist
			MediaObject song = new MediaObject(jsonSong);
			MediaObjectList playlist = new MediaObjectList(playlistName);
			playlist.load(Session.usersPath + uid + "\\playlists\\");
			
			op = SONG_DSNT_EXIST;
			if (playlist.remove(song))
			{	playlist.save(Session.usersPath + uid + "\\playlists\\");
				op = SONG_DELETED;
			}
		}
		
		//Add to the sender's queue
		RPCDescriptor reply = new RPCDescriptor(sourceAddress(), sourcePort(), RPCDescriptor.REPLY, rpc.getRequestId(), rpc.getUserId(), op, rpc.getArguments()); 
		Sender.insert(reply);
		ProcessManager.removeRPC(rpc);
		
		//Update logs
		if (logsOn)
		{
			reply.save(Session.logsOutRPC);		
			rpc.save(Session.logsExecRPC);
			rpc.delete(Session.logsInRPC);
		}						
	}		
	
	

	//return a play list
	private void op_getPlaylist()
	{
		String uid = String.valueOf(rpc.getUserId());
		String playlistName = getStringAttribute("playlistName", rpc.getArguments());
		int op = PLAYLIST_DSNT_EXIST;  //by default
		String parameter = rpc.getArguments();
		
		Library library = new Library (Session.usersPath + uid + "\\playlists\\");
		if (library.contains(playlistName))
		{
			//Open the user playlist
			MediaObjectList playlist = new MediaObjectList(playlistName);
			playlist.load(Session.usersPath + uid + "\\playlists\\");			
			op = RETRIEVED_PLAYLIST;
			
			//Put it in the parameters
			JSONObject json = new JSONObject();
			try {			
				json.put("request", rpc.getArguments());
				json.put("playlist", playlist.toJSON());				
			} catch (JSONException e) {
				e.printStackTrace();
			}			
			parameter = json.toString();
		}
		
		//Add to the sender's queue
		RPCDescriptor reply = new RPCDescriptor(sourceAddress(), sourcePort(), RPCDescriptor.REPLY, rpc.getRequestId(), rpc.getUserId(), op, parameter); 
		Sender.insert(reply);
		ProcessManager.removeRPC(rpc);
		
		//Update logs
		if (logsOn)
		{
			reply.save(Session.logsOutRPC);		
			rpc.save(Session.logsExecRPC);
			rpc.delete(Session.logsInRPC);
		}
	}	
		
	//return a sublist from the catalog
	private void op_search()
	{
		String criteria = getStringAttribute("criteria", rpc.getArguments());			
		int op = RETRIEVED_PLAYLIST;  //by default
		
		//Open the catalog
		MediaObjectList catalog = Session.getCatalog();
		MediaObjectList searchResult = catalog.filter(criteria, 10);
		searchResult.setName("Search: <" + criteria + " >");
		
		//Put it in the parameters
		JSONObject json = new JSONObject();
		try {			
			json.put("request", rpc.getArguments());
			json.put("playlist", searchResult.toJSON());				
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		String parameter = json.toString();
		
		//Add to the sender's queue
		RPCDescriptor reply = new RPCDescriptor(sourceAddress(), sourcePort(), RPCDescriptor.REPLY, rpc.getRequestId(), rpc.getUserId(), op, parameter);
		Sender.insert(reply);								
		ProcessManager.removeRPC(rpc);
		
		//Update logs
		if (logsOn)
		{
			reply.save(Session.logsOutRPC);		
			rpc.save(Session.logsExecRPC);
			rpc.delete(Session.logsInRPC);
		}
	}		
	
	//Get a segment of a song from a file
	private void op_getSegment()
	{
		int index = Integer.parseInt(getStringAttribute("index", rpc.getArguments()));
		MediaObject song = new MediaObject(getStringAttribute("song", rpc.getArguments()));		
		String arguments = rpc.getArguments();
		int op = NULL_OPERATION;  //by default NULL operation
		
		try {								
				//Open the file for reading
				RandomAccessFile file = new RandomAccessFile(Session.storagePath + "\\songs\\" + song.getFileName(), "r");
				op = SEGMENT;				
				String segment = "";
				long offset = SEGMENT_LENGTH * index;
				
				//If segment's offset is valid
				if (offset < file.length())
				{
					byte[] buffer = new byte[SEGMENT_LENGTH];									
					file.seek(offset);		
					int length = file.read(buffer);
					
					//Encode the segment
					if (length > 0 )
						segment = java.util.Base64.getEncoder().encodeToString(Arrays.copyOfRange(buffer, 0, length));											
				}

				//Put it in the parameters
				JSONObject json = new JSONObject();					
				json.put("index", index);
				json.put("segment", segment);
				arguments = json.toString();				
				file.close();				
					
			} catch (FileNotFoundException e) {				
				op = SONG_DSNT_EXIST;  				
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}				
				
		//Add to the sender's queue
		RPCDescriptor reply = new RPCDescriptor(sourceAddress(), sourcePort(), RPCDescriptor.REPLY, rpc.getRequestId(), rpc.getUserId(), op, arguments);
		Sender.insert(reply);
		ProcessManager.removeRPC(rpc);
		
		//Update logs
		if (logsOn)
		{
		//	reply.save(Session.logsOutRPC);		
		//	rpc.save(Session.logsExecRPC);
			rpc.delete(Session.logsInRPC);
		}
		
	} //End getSegment			
		
}//End Class
