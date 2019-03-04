import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;


public class SongService {
	static final int FRAGMENT_SIZE = 8192;

	/*
	 * getSongChunk: Gets a chunk of a given song
	 * 
	 * @param key: Song ID. Each song has a unique ID
	 * 
	 * @param fragment: The chunk corresponds to [fragment * FRAGMENT_SIZE,
	 * FRAGMENT_SIZE]
	 */
	public JsonObject getSongChunk(JsonObject params) {
		String key = params.get("key").getAsString();
		Long fragment = params.get("fragment").getAsLong();

		File file = new File("./assets/" + key);
		Long total = file.length();

		Long offset = fragment * FRAGMENT_SIZE;
		if (offset >= total) {
			JsonObject response = new JsonObject();
			response.addProperty("ret", "");
			return response;	
		}
		
		int size = FRAGMENT_SIZE;
		int available = (int) (total - offset);
		if (available < size) {
			size = available;
		}
		
		byte buf[] = new byte[size];

		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
			inputStream.skip(offset);
			inputStream.read(buf);
			inputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JsonObject response = new JsonObject();
		response.addProperty("ret", Base64.getEncoder().encodeToString(buf));
		return response;
	}

	/*
	 * getFileSize: Gets a size of the file
	 * 
	 * @param key: Song ID. Each song has a unique ID
	 */
	public JsonObject getFileSize(JsonObject params) {
		String key = params.get("key").getAsString();
		File file = new File("./assets/" + key);
		Integer total = (int) file.length();
		JsonObject response = new JsonObject();
		response.addProperty("ret", total);
		return response;
	}

}