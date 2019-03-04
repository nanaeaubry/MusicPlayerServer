import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * User Service Class will display the playlists that each user has when they are logged in.
 * Class will get create, delete, and get playlists.
 * Playlists can have songs added, deleted, and displayed.
 * 
 * @author nanaeaubry
 *
 */

public class UserService {

	Object sync;

	public UserService() {
		sync = new Object();
	}

	public JsonObject getPlaylists(JsonObject param) {
		String userId = param.get("userId").getAsString();

		String folderPath = "./assets/users/" + userId;
		File folder = new File(folderPath);
		String[] listOfFiles = folder.list();

		JsonArray playlists = new JsonArray();

		for (int i = 0; i < listOfFiles.length; i++) {
			int index = listOfFiles[i].indexOf(".json");
			if (index >= 0) {
				JsonObject playlist = new JsonObject();
				playlist.addProperty("name", listOfFiles[i].substring(0, index).trim());
				playlists.add(playlist);
			}
		}

		JsonObject response = new JsonObject();
		response.add("ret", playlists);
		return response;
	}

	public JsonObject createPlaylist(JsonObject param) {
		String userId = param.get("userId").getAsString();
		String playlistName = param.get("name").getAsString();

		String filePath = playlistPath(userId, playlistName);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(filePath, false));
			writer.write("[]");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			if (writer != null) {
				writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		JsonObject response = new JsonObject();
		response.addProperty("ret", true);
		return response;
	}

	public JsonObject deletePlaylist(JsonObject param) {
		String userId = param.get("userId").getAsString();
		String playlistName = param.get("name").getAsString();

		String filePath = playlistPath(userId, playlistName);
		File file = new File(filePath);

		Boolean ret = file.delete();

		JsonObject response = new JsonObject();
		response.addProperty("ret", ret);
		return response;
	}

	public JsonObject addSongToPlaylist(JsonObject param) {
		String userId = param.get("userId").getAsString();
		String playlistName = param.get("name").getAsString();

		Gson gson = new Gson();
		JsonElement songJson = param.get("song");
		Song song = gson.fromJson(songJson, Song.class);

		ArrayList<Song> songs = readPlaylist(userId, playlistName);
		if (!songs.contains(song)) {
			songs.add(song);
			writePlaylist(userId, playlistName, songs);
		}

		JsonObject response = new JsonObject();
		response.addProperty("ret", true);
		return response;
	}

	public JsonObject deletePlaylistSong(JsonObject param) {
		String userId = param.get("userId").getAsString();
		String playlistName = param.get("name").getAsString();
		String songId = param.get("songId").getAsString();

		Song song = new Song();
		song.id = songId;

		ArrayList<Song> songs = readPlaylist(userId, playlistName);
		if (songs.remove(song)) {
			writePlaylist(userId, playlistName, songs);
		}

		JsonObject response = new JsonObject();
		response.addProperty("ret", true);
		return response;
	}

	public JsonObject getPlaylistSongs(JsonObject param) {
		String userId = param.get("userId").getAsString();
		String playlistName = param.get("name").getAsString();

		ArrayList<Song> songs = readPlaylist(userId, playlistName);
		JsonArray ret = new JsonArray();
		for (int i = 0; i < songs.size(); i++) {
			Song song = songs.get(i);

			JsonObject jSong = new JsonObject();
			jSong.addProperty("id", song.id);
			jSong.addProperty("title", song.title);
			jSong.addProperty("album", song.album);
			jSong.addProperty("artist", song.artist);

			ret.add(jSong);
		}

		JsonObject response = new JsonObject();
		response.add("ret", ret);
		return response;
	}

	private String playlistPath(String userId, String playlistName) {
		return "./assets/users/" + userId + "/" + playlistName + ".json";
	}

	private ArrayList<Song> readPlaylist(String userId, String playlistName) {
		synchronized (sync) {
			String filePath = playlistPath(userId, playlistName);
			ArrayList<Song> songs = new ArrayList<Song>();
			try {
				JsonReader jsonReader = new JsonReader(new FileReader(filePath));
				Gson gson = new Gson();

				jsonReader.beginArray();
				while (jsonReader.hasNext()) {
					Song song = gson.fromJson(jsonReader, Song.class);
					songs.add(song);
				}
				jsonReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return songs;
		}
	}

	private void writePlaylist(String userId, String playlistName, ArrayList<Song> songs) {

		synchronized (sync) {
			String filePath = playlistPath(userId, playlistName);
			Type type = new TypeToken<ArrayList<Song>>() {
			}.getType();

			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(filePath, false));
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String json = gson.toJson(songs, type);
				writer.write(json);
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected class Song {
		public String id;
		public String title;
		public String artist;
		public String album;

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj == null || obj.getClass() != this.getClass()) {
				return false;
			}
			Song song = (Song) obj;
			return this.id.equals(song.id);
		}
	}
}
