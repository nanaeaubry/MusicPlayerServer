import java.util.Comparator;
import com.google.gson.JsonObject;


public class CatalogItem {
	public Release release;
	public Artist artist;
	public Song song;
	
	/*Comparator for sorting the list by ArtistName*/
    public static Comparator<CatalogItem> ArtistNameComparator = new Comparator<CatalogItem>() {

	public int compare(CatalogItem s1, CatalogItem s2) {
	   String ArtistName1 = s1.artist.name.toUpperCase();
	   String ArtistName2 = s2.artist.name.toUpperCase();

	   //ascending order
	   return ArtistName1.compareTo(ArtistName2);

	   //descending order
	   //return ArtistName2.compareTo(ArtistName1);
    }};
    
    
    
	/*Comparator for sorting the list by AlbumName*/
    public static Comparator<CatalogItem> AlbumNameComparator = new Comparator<CatalogItem>() {

	public int compare(CatalogItem s1, CatalogItem s2) {
	   String AlbumName1 = s1.release.name.toUpperCase();
	   String AlbumName2 = s2.release.name.toUpperCase();

	   //ascending order
	   return AlbumName1.compareTo(AlbumName2);

	   //descending order
	   //return AlbumName2.compareTo(AlbumName1);
    }};
    
	/*Comparator for sorting the list by SongName*/
    public static Comparator<CatalogItem> SongNameComparator = new Comparator<CatalogItem>() {

	public int compare(CatalogItem s1, CatalogItem s2) {
	   String SongName1 = s1.song.title.toUpperCase();
	   String SongName2 = s2.song.title.toUpperCase();

	   //ascending order
	   return SongName1.compareTo(SongName2);

	   //descending order
	   //return SongName2.compareTo(SongName1);
    }};

    public boolean passesFilter(String filter)
    {
		//String id = this.song.id;
		String title = this.song.title;
		String album = this.release.name;
		String artist = this.artist.name;

		if (filter != null && filter.length() > 0) 
		{
			
			// Skip song if filter keyword is not found in 
			// title or artist or album
			if (title.toLowerCase().indexOf(filter) < 0 && artist.toLowerCase().indexOf(filter) < 0
					&& album.toLowerCase().indexOf(filter) < 0) 
			{
				return false;
			}

		}
		return true;
    }

    public JsonObject getJson()
    {
		JsonObject song = new JsonObject();
		song.addProperty("id", this.song.id);
		song.addProperty("title", this.song.title);
		song.addProperty("album", this.release.name);
		song.addProperty("artist", this.artist.name);
		return song;
    }
}
