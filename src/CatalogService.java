import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

public class CatalogService {
	private ArrayList<Item> items;

	public CatalogService() {
		items = new ArrayList<Item>();
		loadSongs();
	}

	public JsonObject getSongs(JsonObject param) {
		int startIndex = param.get("startIndex").getAsInt();
		int count = param.get("count").getAsInt();
		String filter = param.get("filter").getAsString();
		
		if (filter != null) {
			filter = filter.stripLeading().toLowerCase();
		}

		JsonArray ret = new JsonArray();
		for (int i = startIndex, j = 0; i < items.size() && j < count; i++) {
			Item item = items.get(i);
			String title = item.song.title;
			String album = item.release.name;
			String artist = item.artist.name;
			if (filter != null && filter.length() > 0) {
				if (title.toLowerCase().indexOf(filter) < 0 && artist.toLowerCase().indexOf(filter) < 0
						&& album.toLowerCase().indexOf(filter) < 0) {
					continue;
				}

			}

			JsonObject song = new JsonObject();
			song.addProperty("id", item.song.id);
			song.addProperty("title", title);
			song.addProperty("album", album);
			song.addProperty("artist", artist);

			ret.add(song);
			j++;
		}

		JsonObject response = new JsonObject();
		response.add("ret", ret);
		return response;
	}

	private void loadSongs() {
		try {
			// Open the file for reading
			JsonReader jsonReader = new JsonReader(new FileReader("./assets/music.json"));
			Gson gson = new Gson();

			jsonReader.beginArray();
			while (jsonReader.hasNext()) {
				Item item = gson.fromJson(jsonReader, Item.class);
				items.add(item);
			}
			jsonReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected class Item {
		public Release release;
		public Artist artist;
		public Song song;
	}

	protected class Release {
		public String name;
	}

	protected class Artist {
		public String name;
	}

	protected class Song {
		public String id;
		public String title;

	}
}
