import java.io.FileReader;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

/**
 * CatalogService Class will communicate the song list to the client. It reads
 * in the json of music from the assets folder and will capture the information
 * needed to give to the client.
 * 
 * @author nanaeaubry
 *
 */

public class CatalogService {

	private static final String ASSETS_MUSIC_JSON = "./assets/music.json";

	private DFS dfs;

	public CatalogService(DFS dfs) {
		this.dfs = dfs;

		try {
			dfs.create(ASSETS_MUSIC_JSON);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JsonObject getSongs(JsonObject param) {
		JsonElement element = param.get("startIndex");

		element = param.get("count");
		int count = 100;
		if (element != null) {
			count = element.getAsInt();
		}

		element = param.get("filter");
		String filter = null;
		if (element != null) {
			filter = element.getAsString();
		}

		if (filter != null) {
			filter = filter.stripLeading().toLowerCase();
			JsonObject ret = dfs.search(filter, count);
			System.out.println(ret);
			return ret;
		}

		JsonArray ret = new JsonArray();
		for (int i = 0; i < 2; i++) {
			// Remote Input File Stream
			try {
				RemoteInputFileStream dataraw = this.dfs.read(ASSETS_MUSIC_JSON, i);
				dataraw.connect();

				// Scanner
				Scanner scan = new Scanner(dataraw);
				scan.useDelimiter("\\A");
				String data = scan.next();

				// Convert from json to ArrayList
				CatalogPage page = new CatalogPage();
				Gson gson = new Gson();
				page = gson.fromJson(data, CatalogPage.class);

				// Adding results to return
				for (int j = 0; j < page.size(); j++) {
					ret.add(page.getItem(j).getJson());
				}
				scan.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		JsonObject response = new JsonObject();
		response.add("ret", ret);
		return response;
	}

}
