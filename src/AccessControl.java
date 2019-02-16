
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Class AccessControl
 *   
 * @author rTunes team
 */
public class AccessControl {
	
	//Constants
	//------------------------
	private final String accountsFile = "accounts.txt";  

	//Attributes
	//------------------------
	private static ArrayList<String> accounts = null;	//list of valid accounts
	private static ArrayList<String> uids = null;		//list of valid UIDs

	//Methods
	//-----------------------------
	
	//Default constructor
	public AccessControl()
	{
		if (accounts == null)
		{
			accounts = new ArrayList<String>();
			uids = new ArrayList<String>();		
			loadAccounts();					
		}
	}
		
	
	//Read the file of user accounts
	private void loadAccounts()
	{ 
		try {
			//Initialize variables
			String line = "";
			accounts.clear();	//remove previous elements in the list
			uids.clear();		//remove previous elements in the list
						
			//Open the file for reading
			File file = new File(Session.storagePath + accountsFile); 		  
			BufferedReader buffer = new BufferedReader(new FileReader(file));
			
			//load the media object			
			while ((line = buffer.readLine()) != null)
			{
				char delimiter = ';';
				
				int index = line.indexOf(delimiter); 	//the username				
				index = line.indexOf(delimiter, index+1); //the password
				accounts.add(line.substring(0, index).trim());
				uids.add(line.substring(index+1).trim());
			}

			//Close the file
			buffer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
		
	//attempts to login a user
	// returns the UID if login has success, otherwise an empty string
	public String login(String username, String password)
	{
		String uid = "";	//by default empty uid, which means unauthorized user
		String attempt = username.trim() + ";" + password.trim(); //build the possible account		
		
		//Try to get the user from the list
		int index = accounts.indexOf(attempt);			
		if (index >= 0) {
			uid = uids.get(index);	//gets the UID if user is authorized
		}
		
		return(uid);
	}
						
	
	//Get the credentials in a JSON format 
	public static String credentialsToJson(String username, String password)
	{
		JSONObject json = new JSONObject();
		try {			
			json.put("username", username);
			json.put("password", password);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return (json.toString());		
	} 		
}
