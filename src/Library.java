
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class Library
 *   
 * @author rTunes team
 */
public class Library {

	//Attributes
	//------------------------
	private ArrayList<String> container;	//List of lists
	private String libraryPath; 
	
	//Methods
	//-----------------------------	

	//constructor	
	public Library(String path)
	{
		container = new ArrayList<String>();
		libraryPath = path;
		load();
	}
	
	
	//Load the elements from a folder
	//   preconditions:
	//      - The folder contains only json files of MediaObjectList
	//      - Each file ends with the extension ".json"
	private void load()
	{						
		//remove previous elements in the list
		container.clear();
						
		//Get the folder's contain
		File folder = new File(libraryPath); 		
		String[] listOfFiles = folder.list();

		//load only names of files
		for (int i=0; i < listOfFiles.length; i++)
		{
			int index = listOfFiles[i].indexOf(".json");
			if (index >=0) 
				container.add(listOfFiles[i].substring(0, index).trim());			
		}
	}
		
	//flat the Library as a JSON string
	public String toJson()
	{
		try {						
				String jsonLibrary = "";				
				for (int i=0; i < container.size(); i++)
				{
					JSONObject json = new JSONObject();
					json.put("playlist", container.get(i));
					jsonLibrary = jsonLibrary + json.toString() + "\r\n"; 
				}
				
				if (jsonLibrary.isEmpty()) jsonLibrary = "{}";
				return (jsonLibrary);				
				
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return (null);		
	}	
		
	//Verifies if the list contains an element
	public boolean contains(String element)
	{						
		return (container.indexOf(element) >= 0);
	}		
	
	//Creates an element in the folder
	//   Creates a text file 
	//   preconditions:
	//      - The folder exists
	//   postconditions:
	//      - The file is created empty in the folder of the library 
	public int create(String element)
	{						
		try {			
				//Prepares the file for writing
				File file = new File(libraryPath + element + ".json");
				if (!file.exists()){					
					BufferedWriter buffer = new BufferedWriter(new FileWriter(file, false));
					buffer.close();
					return(0);  //file created
				}
				return(1);  //file already exists
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		return(-1); //an error ocurred
	}	
	
	
	//Delete an element from the library
	//   deletes the text file 
	//   preconditions:
	//      - The folder exists
	//   postconditions:
	//      - The file is removed from the folder of the library 
	public void delete(String element)
	{				
		File file = new File(libraryPath + element + ".json"); 		
		if (file.exists()) 
				file.delete();	
	}
		
				
	//Append a MediaObject String to an element of the library
	//   Append the information in a text file
	//   preconditions:
	//      - The file exists
	public void append(String element, MediaObject obj)
	{						
		try {
				File file = new File(libraryPath + element + ".json"); 		
				BufferedWriter buffer = new BufferedWriter(new FileWriter(file, true));
				buffer.write(obj.toJSON().toString() + "\r\n");
					
				//Close the file
				buffer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	

					
} // End of the Class
