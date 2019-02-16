
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class UserProfile
 *   
 * @author rTunes team
 */
public class UserProfile {

	//Constants
	//------------------------
	private static final String fileName = "profile.txt";  
	//private static final String imageFile = "picture.png";
	
	//Attributes
	//------------------------
	private String username;
	private String email;
	private String uid;
	private String profilePath;

	
	//Methods
	//-----------------------------
	
	//Default constructor
	public UserProfile(String path, String UID)
	{
		uid = UID;
		profilePath = path;
		loadProfile();		
	}	
	
		
	//Load the profile
	//   read the information from the file stored in the user folder
	//   preconditions:
	//      - The user folder's name is equal to the UID
	//      - The user folder exists
	//      - The profile is stored in a text file
	//      - The file name is predefine (constant for this class)
	//      - Each attribute occupies the whole line  
	private void loadProfile()
	{						
		try {
			//Open the file
			File file = new File(profilePath + fileName); 		  
			BufferedReader buffer = new BufferedReader(new FileReader(file));

			//load the attributes
			username = buffer.readLine();
			email  = buffer.readLine();

			//Close the file
			buffer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	
	//flat the Profile as a JSON string
	public String toJson()
	{
		try {						
				JSONObject json = new JSONObject();
				json.put("username", username);
				json.put("email", email);
				json.put("uid", uid);
				return (json.toString());				
				
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return (null);		
	}	
	
} //End of the Class
