
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class CatalogPage {
	List<CatalogItem> items;
	String key; // used when "emiting"

	public CatalogPage() {
		items = new ArrayList<CatalogItem>();
		key = "??";
	}

	// getters-----------------------------
	public CatalogItem getItem(int i) {
		return this.items.get(i);
	}

	public int size() {
		return items.size();
	}

	public String getKey() {
		return key;
	}

	// returns the first letter of the first item
	public String getFirstLetter() {
		return "?";
	}

	// returns the first letter of the last item
	public String getLastLetter() {
		return "?";
	}

	// setters-------------------------------
	public void addItem(CatalogItem item) {
		this.items.add(item);
	}

	public void clear() {
		items.clear();
	}

	public void setKey(String key) {
		this.key = key;
	}

	// FILE IO --------------------------------------------------

	// Write simplified catalog
	public void writeJsonFile(String fileName) {
		// String TAG = "writeJsonFile";

		Gson gson = new Gson();
		String jsonString = gson.toJson(this); // Convert CatalogPage to Json

		// peer.put(fileName, jsonString);
		try {
			// String fileName = "test_write_CatalogPage";
			FileOutputStream output = new FileOutputStream(fileName);
			output.write(jsonString.getBytes());
			output.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	// loads the original music.json file that has Professor Ponce's Format
	public void loadCatalog(String fileName) {
		String path = fileName;

		// Try to open the file for reading
		try {

			// JSON
			JsonReader jsonReader = new JsonReader(new FileReader(path));

			// GSON
			Gson gson = new Gson();

			jsonReader.beginArray();

			while (jsonReader.hasNext()) {
				CatalogItem item = gson.fromJson(jsonReader, CatalogItem.class);
				items.add(item);
			}
			jsonReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// PRINT --------------------------------------------------

	public void println() {
		this.println(items.size());
	}

	public void println(int count) {
		// Set limit of items to print
		int limit = items.size();
		if (count < limit) {
			limit = count;
		}

		// Print n items
		for (int i = 0; i < limit; i++) {
			System.out.println("Unsorted: " + items.get(i).artist.name);
		}
	}
};