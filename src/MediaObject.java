import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class Media Object
 *   
 * @author rTunes team
 */
public class MediaObject {

	//Attributes
	//------------------------
	private String title;		//Name 
	private String genre;		//Rock, HipHop, Drama, Science, etc
	private String author;		//Artist(s), company, etc
	private String collection;	//Name of the album, or single, or LP, or Disc, etc
	private String fileName;	//file's name 
	private String tags;		//tags for searching
	
	//Methods
	//-----------------------------	

	//constructor
	public MediaObject(JSONObject json)
	{
		setAttributes(json);
	}

	//read from a JSON string
	public MediaObject(String jsonString)
	{
		try {
			
			JSONObject json = new JSONObject(jsonString);
			setAttributes(json);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	//Gets
	//  methods to get each attribute
	public String getTitle()
	{
		return (title);
	}
	
	public String getGenre()
	{
		return (genre);
	}
	
	public String getAuthor()
	{
		return (author);
	}
	
	public String getCollection()
	{
		return (collection);
	}
	
	public String getFileName()
	{
		return (fileName);
	}
	
	public String getTags()
	{
		return (tags);
	}
	
		
	//Returns the object as a JSON Object
	public JSONObject toJSON()
	{
		JSONObject json = new JSONObject();
		try {			
			json.put("author", author);
			json.put("title", title);
			json.put("genre", genre);
			json.put("collection", collection);
			json.put("filename", fileName);
			json.put("tags", tags);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return (json);		
	}
	
	//Set the object's attributes from a JSONObject
	// the procedure parses the input string to get the data 
	// to fill up the attributes of the object
	public void setAttributes(JSONObject json)
	{			
		try {
			author = json.getString("author");
			title= json.getString("title");		
			genre = json.getString("genre");
			collection = json.getString("collection");
			fileName = json.getString("filename");
			tags = json.getString("tags");			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	// Search a coincidence in the tags
	//   return:
	//      true if there is a coincidence
	//      false: the tags does not contains the substring
	public boolean contains (String lookingFor)
	{		
		return (getTags().toUpperCase().indexOf(lookingFor.toUpperCase()) >= 0);
	}	
	
	
	
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
			{	MediaObject ptr = (MediaObject) x;
				return (this.getTags().toUpperCase().equals(ptr.getTags().toUpperCase()));
			}				
		}
	}	
	
	// Implements the method to convert the object as an String
	@Override
	public String toString()
	{
		return ("Title: " + title + "\nAuthor: " + author + "\nGenre: " + genre + "\nCollection: " + 
	            collection + "\nFile: " + fileName + "\nTags: " + tags);
	}	
	

	
} //End of class
