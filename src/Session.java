
/**
 * Class Session
 *   
 * @author rTunes team
 */
public class Session {

	//CONSTANTS
	public static final String storagePath = "C:\\rtunes\\";
	public static final String musicPath = "C:\\rtunes\\songs\\";
	public static final String usersPath = "C:\\rtunes\\users\\";
	public static final String logsInRPC = "C:\\rtunes\\logsInRPC\\";	
	public static final String logsOutRPC = "C:\\rtunes\\logsOutRPC\\";	
	public static final String logsExecRPC = "C:\\rtunes\\logsExecRPC\\";	
	
	//GLOBAL VARIABLES
	private static MediaObjectList catalog=null;
	
	//Load the catalog
	public static MediaObjectList getCatalog()
	{
		if (catalog == null)
		{
			catalog = new MediaObjectList("catalog");
			catalog.load(Session.storagePath);
		}
		
		return(catalog);
	}
}


