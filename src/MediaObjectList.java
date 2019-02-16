
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class MediaObjectList
 *   
 * @author rTunes team
 */
public class MediaObjectList {

	//Attributes
	//------------------------
	private String name;						//Name of the list
	private ArrayList<MediaObject> container;	//List of objects
		
		
	//Methods
	//-----------------------------

	//constructor	
	public MediaObjectList (String containerName)
	{
			name = containerName;
			container = new ArrayList<MediaObject>();
	}
				
		
	//Gets
	//  methods to get each attribute
	public String getName()
	{
		return (name);
	}			
		
	public ArrayList<MediaObject> getContainer()
	{
		return (container);
	}
	
		
	public int getSize()
	{
		return(container.size());
	}
	
	public MediaObject getSong (int index)
	{
		return (container.get(index));
	}
	
	//Sets
	public void setName(String newName)
	{
		name = newName;
	}
	
	//Search an Object in the container
	//  returns the index if the object is in the list
	//  otherwise -1
	public int search (MediaObject theObject)
	{
		return(container.indexOf(theObject));
	}
		
			
	//Adds an object to the container
	//  postcondition: the object is not added if it already exists in the container
	public int add(MediaObject newObject)
	{		
		int index = search(newObject);
		
		//Added if the search returns a negative index
		if (index < 0)
		{
			container.add(newObject);
			index = container.size()-1;
		}
		
		return (index);
	}
		
		
	//Remove an Object from the container
	public boolean remove(MediaObject theObject)
	{
		return(container.remove(theObject));		
	}
		

	//Get the objects that contains a certain string (filter string)
	//  postconditions:
	//      The name of the list returned is the filter string      
	public MediaObjectList filter(String lookingFor, int limit)
	{
		MediaObjectList result = new MediaObjectList(lookingFor);
		
		for (int i=0, j=0; i <container.size() && j<limit; i++)
		{
			if (container.get(i).contains(lookingFor))
			{	++j;
				result.add(container.get(i));
			}
		}
		return(result);
	}
	
	
	//Load the List
	//   read the information from a text file
	//   preconditions:
	//      - The file should exist
	//   postconditions
	//      - Duplicated objects are loaded just once
	public void load(String path)
	{					
		
		try {
			//Initialize variables
			String info = "";
			container.clear();		//remove previous elements in the list
						
			//Open the file for reading
			File file = new File(path + name + ".json"); 		  
			BufferedReader buffer = new BufferedReader(new FileReader(file));
			
			//load the media object			
			while ((info = buffer.readLine()) != null)
			{
				JSONObject json = new JSONObject (info);
				MediaObject song = new MediaObject(json);
				add(song);	//Add the object to the list				
			}			

			//Close the file
			buffer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
		

	//Save the List
	//   Save the information in a text file
	//   preconditions:
	//      - The file will be overwrite if it exists
	public void save(String path)
	{						
		try {			
			//Prepares the file for writing
			File file = new File(path + name + ".json"); 		
			if (!file.exists()) file.createNewFile();			
			BufferedWriter buffer = new BufferedWriter(new FileWriter(file, false));						
			
			//save each object represented only by the tag
			for (int i=0; i<container.size(); i++)
			{
				JSONObject json = container.get(i).toJSON();				
				buffer.write(json.toString() + "\r\n");				
			}						
			//Close the file
			buffer.flush();
			buffer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
			

	//Returns the Objectlist in a JSON format
	public String toJSON()
	{						
		String buffer = "";
		for (int i=0; i<container.size(); i++)
		{
			JSONObject json = container.get(i).toJSON();
			buffer = buffer + json.toString() + "\r\n";
		}		
		return(buffer);
	}
	
		
	//Converts the list of Objects to a list of Strings
	public ArrayList<String> toArrayListString()
	{
		ArrayList<String> list = new ArrayList<String>();
		for (int i=0; i <container.size(); i++)
		{
			list.add(container.get(i).getTitle());
		}
		
		return (list);
	}
	


} //End of the Class
