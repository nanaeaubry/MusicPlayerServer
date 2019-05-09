import java.rmi.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.math.BigInteger;
import java.security.*;
import java.io.InputStream;
import java.util.*;

import java.util.TreeMap;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/* Metadata JSON Format
{"file":
  [
     {"name":"MyFile",
      "size":128000000,
      "pages":
      [
         {
            "guid":11,
            "size":64000000
            "letter":"A"
            "last_letter"B"
         },
         {
            "guid":13,
            "size":64000000
            "letter":"B"
            "last_letter"C"
         }
      ]
      }
   ]
} 
*/

public class DFS {

	// METADATA CLASSES---------------------------

	public class PagesJson {
		Long guid;
		int size;
		String letter;
		String last_letter;

		public PagesJson() {
			guid = (long) 0;
			size = 0;
		}

		// getters
		public Long getGUID() {
			return this.guid;
		}

		public int getSize() {
			return this.size;
		}

		public String getLetter() {
			return this.letter;
		}

		public String getLastLetter() {
			return this.last_letter;
		}

		// setters
		public void setGUID(Long guid) {
			this.guid = guid;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public void setLetter(String l) {
			this.letter = l;
		}

		public void setLastLetter(String l) {
			this.last_letter = l;
		}

	};

	public class FileJson {
		String name;
		Long size;
		int numberOfItems;
		int itemsPerPage;
		ArrayList<PagesJson> pages;
		// future improvement: add a hashmap with (letter -> page number) (key->value)

		public FileJson() {
			this.name = "not set";
			this.size = (long) 0;
			this.numberOfItems = 0;
			this.itemsPerPage = 0;
			this.pages = new ArrayList<PagesJson>();
		}

		// getters
		public String getName() {
			return this.name;
		}

		public Long getSize() {
			return this.size;
		}

		public int getNumberOfItems() {
			return this.numberOfItems;
		}

		public int getItemsPerPage() {
			return this.itemsPerPage;
		}

		public int getNumberOfPages() {
			return this.pages.size();
		}

		public ArrayList<PagesJson> getPages() {
			return this.pages;
		}

		public PagesJson getPage(int i) {
			return pages.get(i);
		}

		// setters
		public void setName(String name) {
			this.name = name;
		}

		public void setSize(Long size) {
			this.size = size;
		}

		public void setPages(ArrayList<PagesJson> pages) {
			this.pages = new ArrayList<PagesJson>();
			for (int i = 0; i < pages.size(); i++) {
				this.pages.add(pages.get(i));
			}
		}

		public void addPage(PagesJson page) {
			this.pages.add(page);
			this.size += page.getSize();
		}

		public void addPage(Long guid, int page_size, String letter) {
			PagesJson page = new PagesJson(); // metadata
			page.setGUID(guid); // metadata
			page.setSize(page_size); // metadata
			page.setLetter(letter); // metadata

			this.addPage(page);
		}
	};

	public class FilesJson {
		List<FileJson> files;

		public FilesJson() {
			files = new ArrayList<FileJson>();
		}

		// getters
		public FileJson getFile(int i) {
			return this.files.get(i);
		}

		// setters
		public void addFile(FileJson file) {
			this.files.add(file);
		}

		public int size() {
			return files.size();
		}

		public void deleteFile(String fileName) {
			int index_to_remove = 0;
			for (int i = 0; i < files.size(); i++) {
				if (files.get(i).getName().equals(fileName)) {
					index_to_remove = i;
				}
			}

			files.remove(index_to_remove);
		}
	};
	// END METADATA CLASSES---------------------------

	// HASH FUNCTION
	private long md5(String objectName) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.reset();
			m.update(objectName.getBytes());
			BigInteger bigInt = new BigInteger(1, m.digest());
			return Math.abs(bigInt.longValue());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();

		}
		return 0;
	}

	// END HELPER CLASSES---------------------------------------

	// DFS Variables
	int port; //
	Chord chord; //
	ArrayList<CatalogItem> catalogItems; // Used in function loadCatalog().
	FilesJson local_metadata; // Used to reduce the number of times that metadata is requested from chord.
	Long expiration = (long) 10000; // If the metadata is older then the expiration time it gets reloaded at the
									// next search request.
	Long metadataTimeStamp = (long) 0; // Time when metadata was read from the chord.
										// Initial time is zero to ensure it gets updated the first time its requested.
	int items_per_page = 1000;
	int sleepTime = 500;

	TreeMap<String, CatalogPage> reverseIndex;
	// HashMap <String, CatalogPage> reverseIndex;
	ArrayList<String> sortedKeys;

	// END DFS Variables

	public DFS(int port) throws Exception {
		catalogItems = new ArrayList<CatalogItem>();
		local_metadata = new FilesJson();
		reverseIndex = new TreeMap<String, CatalogPage>(); // 3k is the number of words in common use.

		// reverseIndex = new HashMap<String, CatalogPage>(3000); //3k is the number of
		// words in common use.
		// Oxford english dictionary contains 171k words in current use.
		sortedKeys = new ArrayList<String>();

		this.port = port;
		long guid = md5("" + port);
		chord = new Chord(port, guid);
		Files.createDirectories(Paths.get(guid + "/repository"));
		Files.createDirectories(Paths.get(guid + "/tmp"));
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				chord.leave();
			}
		});

	}

	// Testing Chord size Function
	public void determineChordSize() throws Exception {
		// Print Initial Value //DEBUG
		// System.out.println("Initial Value: " + chord.getChordSize()); //DEBUG

		// Determine size
		chord.onChordSize(chord.getId(), 0);

		// Wait //DEBUG
		// System.out.println("Sleeping..."); //DEBUG
		// Thread.sleep(500); //DEBUG

		// Print new Value
		System.out.println("Chord Size: " + chord.getChordSize());
	}

	// Maps the file to a treemap
	public void map(String fileName) {

	}

	// tell every peer to emit their tree
	public void reduce(Long guid) {
		// tell successor to emit their tree

		// 1 locate successor

		// peer.emit()

	}

	/**
	 * Join the chord
	 *
	 */
	public void join(String Ip, int port) throws Exception {
		chord.joinRing(Ip, port);
		chord.print();
	}

	/**
	 * leave the chord
	 *
	 */
	public void leave() throws Exception {
		chord.leave();
	}

	/**
	 * print the status of the peer in the chord
	 *
	 */
	public void print() throws Exception {
		chord.print();
	}

	/**
	 * readMetaData read the metadata from the chord
	 *
	 */
	public FilesJson readMetaData() throws Exception {
		// DEBUG
		String TAG = "readMetaData";
		// System.out.println(TAG+"()");
		int readSleepTime = 300;// miliseconds

		Long currentTime = System.currentTimeMillis();

		// if metadata is not too old return itself ie: do not change
		Long difference = currentTime - metadataTimeStamp;
		// System.out.println(TAG + "(): time difference = " + difference); // DEBUG
		if ((currentTime - metadataTimeStamp) < expiration) {
			// System.out.println(TAG+"(): metadata has not expired"); // DEBUG
			return local_metadata;
		}
		// else metadata is too old, refresh it.
		// System.out.println(TAG+"(): refresshing metadata"); // DEBUG
		metadataTimeStamp = System.currentTimeMillis(); // update timestamp

		FilesJson filesJson = null;
		try {
			Gson gson = new Gson();
			long guid = md5("Metadata");
			ChordMessageInterface peer = chord.locateSuccessor(guid);
			RemoteInputFileStream metadataraw = peer.get(guid);
			metadataraw.connect(); // NEW RFIS
			Scanner scan = new Scanner(metadataraw);
			scan.useDelimiter("\\A");
			String strMetaData = scan.next();

			// System.out.println(strMetaData); // DEBUG
			filesJson = gson.fromJson(strMetaData, FilesJson.class);
		} catch (NoSuchElementException ex) {
			filesJson = new FilesJson();
		}
		local_metadata = filesJson;
		return filesJson;
	}

	/**
	 * writeMetaData write the metadata back to the chord
	 *
	 */
	public void writeMetaData(FilesJson filesJson) throws Exception {
		metadataTimeStamp = (long) 0; // zero to make system refresh metadata
		long guid = md5("Metadata");
		ChordMessageInterface peer = chord.locateSuccessor(guid);
		System.out.println("\n\tSaving Metadata to peer: " + peer.getId()); // DEBUG

		Gson gson = new Gson();
		peer.put(guid, gson.toJson(filesJson));
	}

	/**
	 * writePageData write the page data to the chord
	 *
	 */
	public void writePageData(CatalogPage catalogpage, Long guid) throws Exception {
		ChordMessageInterface peer = chord.locateSuccessor(guid);
		System.out.println("\tSaving Page to peer: " + peer.getId()); // DEBUG

		Gson gson = new Gson();
		String jsonString = gson.toJson(catalogpage); // Convert CatalogPage to Json
		peer.put(guid, jsonString); // send page
	}

	/**
	 * Change Name
	 *
	 */
	public void move(String oldName, String newName) throws Exception {
		// TODO: Change the name in Metadata
		// Write Metadata
	}

	/**
	 * List the files in the system
	 *
	 * @param filename Name of the file
	 */
	public String lists() throws Exception {
		// DEBUG
		String TAG = "lists";
		// System.out.println( TAG + "()");

		String listOfFiles = "";
		FilesJson files = readMetaData();
		for (int i = 0; i < files.size(); i++) {
			listOfFiles += files.getFile(i).name + "\n";
		}

		// System.out.println(TAG + ":files.size() == " + files.size());//DEBUG
		if (files.size() == 0) {
			return "Empty";
		}

		return listOfFiles;
	}

	public Long generateGUID(String fileName) {
		Long timeStamp = System.currentTimeMillis();
		return md5(fileName + timeStamp);
	}

	public void createMP3(String fileName) {

	}

	// TODO: Pre-process the music.json catalog and produce the sorted catalogs
	private void createSortedCatalogs(String fileName) {
		// Done in separate project
	}

	public void createIndex(String filename) throws Exception {
		System.out.println("createIndex()");

		// -----Outline-----

		// 1 Load Catalog

		// 2 For each song
		// For each word in (song name, artist name and album name)
		// if first 2 chars of word exists in Hash (Reverse Index)
		// add song to CatalogPage
		// else
		// add first 2 chars word to Hash ()
		// add song to CatalogPage

		// -----Implementation-----

		// 1 Load Catalog
		CatalogPage catalog = new CatalogPage();
		catalog.loadCatalog(filename);
		int start = 0;
		int end = catalog.size(); // size = 10k

		// DEBUG - Print Start and end Index
		System.out.println("start: " + start);
		System.out.println("end: " + end);

		// 2 For each song in catalog
		for (int i = start; i < end; i++)

		{

			// Get all words in artist, album and song title
			String line = catalog.getItem(i).artist.name;
			line = line + " " + catalog.getItem(i).release.name; // album
			line = line + " " + catalog.getItem(i).song.title;

			// System.out.println("line: (" + line +")"); // DEBUG - Print Line
			line = line.toLowerCase();

			// Separate line into words
			String[] words = line.split("\\s+");

			// TODO
			// Remove punctuation // Remove Special Symbols
			/**
			 * for (int j = 0; j < words.length; j++) { // You may want to check for a
			 * non-word character before blindly // performing a replacement // It may also
			 * be necessary to adjust the character class words[i] =
			 * words[i].replaceAll("[^\\w]", ""); }
			 **/

			// For each word
			for (int k = 0; k < words.length; k++) {
				// System.out.println("\t"+ "word: (" + words[k] +")"); // DEBUG - Print each
				// word

				// Get key
				String key = words[k];
				if (key.length() > 2) {
					key = key.substring(0, 2);// get first 2 characters only
				}

				// if first 2 chars of word exists in Hash (Reverse Index)
				if (reverseIndex.containsKey(key)) {
					// add song to CatalogPage
					// System.out.println("\t" + "add song to CatalogPage"); // DEBUG

					reverseIndex.get(key).addItem(catalog.getItem(i)); // Uses 2 chars as key
					// reverseIndex.get(words[k]).addItem(catalog.getItem(i)); //Uses Full word as
					// Key

				} else // this is a new key, add it to the HashMap
				{
					// System.out.println("\t"+ "add word to Hash ("+ words[k] +")"); // DEBUG
					sortedKeys.add(key);// used later when saving the files to the peers.

					// add song to new CatalogPage
					CatalogPage capa = new CatalogPage();
					capa.addItem(catalog.getItem(i));

					// add first two chars of word to Hash ()
					reverseIndex.put(key, capa);

				}

			}

		}

		System.out.println("Indexing Done.");
	}

	public void reverseIndexStats() {
		System.out.println("\n");
		System.out.println("reverseIndexStats()");
		System.out.println("reverseIndex Size: " + reverseIndex.size());// Number of words in hash

		// Average Variables
		Double CMA = 0.0; // Cummulative Moving Average
		int count = 0; // Used to calculate CMA

		// Get all keys and the size
		ArrayList<String> words = new ArrayList<String>();
		for (Map.Entry<String, CatalogPage> entry : reverseIndex.entrySet()) {
			int size = entry.getValue().size();
			// System.out.println("Key = " + entry.getKey() + ", Value.size() = " +
			// entry.getValue().size());
			words.add(entry.getKey() + " : " + size);

			CMA = CMA + ((size - CMA) / (count + 1));
			count = count + 1;
		}

		// Sort List
		Collections.sort(words);
		System.out.println("Sorted List:");
		for (String s : words) {
			System.out.println(s);
		}

		// Print Average number of Items per key
		System.out.println("\nCMA: " + CMA);
		System.out.println("\nwords.size(): " + words.size());

	}

	// Generic Method // Not used because I want to access .size() method on
	// CatalogPage
	public void printMap(Map mp) {
		Iterator it = mp.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			System.out.println(pair.getKey() + " = " + pair.getValue());
			it.remove(); // avoids a ConcurrentModificationException
		}
	}

	public void saveReverseIndexToPeers() {
		System.out.println("saveReverseIndexToPeers()"); // DEBUG

		Collections.sort(sortedKeys);
		System.out.println("\nsortedKeys.size(): " + sortedKeys.size());

		// -----Outline
		// for each key in HashMap
		// save it to a peer

		// -----Implementation
		// for each key in HashMap, get its value and save it to a peer
		for (String k : sortedKeys) {
			System.out.println("key: " + k);// DEBUG

			// generate guid
			// Long timeStamp = System.currentTimeMillis();
			Long guid = md5(k + "reverseIndex" + k); // + timeStamp);
			System.out.println("\tguid = " + guid); // DEBUG

			// save the page to a peer
			try {
				writePageData(reverseIndex.get(k), guid);
			} catch (Exception e) {
				System.out.println("Error while saving reverseIndex key: " + k); // DEBUG
			}
		}
		System.out.println("save done."); // DEBUG
	}

	// Assuming the given file is pre-sorted
	public void create(String fileName) throws Exception {
		System.out.println("createIndex(" + fileName + ")");

		CatalogPage catalog = new CatalogPage();
		catalog.loadCatalog(fileName); // use this if reading music.json.

		// Page Data
		CatalogPage page = new CatalogPage();

		// Metadata
		FileJson file = new FileJson();
		FilesJson metadata = new FilesJson();
		int page_size = 0;
		Long file_size = (long) 0;

		// Split file into n pages
		// for each item in catalog save it to a "page"
		for (int i = 0; i < catalog.size(); i++) {
			// Page groups of size "items_per_page"
			page_size = page_size + 1;

			// get item from catalog and add it to the page
			page.addItem(catalog.getItem(i));

			// if page size reaches "items_per_page" save the page
			if ((i + 1) % this.items_per_page == 0) {

				// DEBUG
				// System.out.println("i + 1 = " + (i+1) );
				System.out.println("\n\tpage_size = " + page_size);

				// Hash each page (name + time stamp) to get its GUID
				Long timeStamp = System.currentTimeMillis();
				Long guid = md5(fileName + timeStamp);
				System.out.println("\tguid = " + guid); // DEBUG

				// Update MetaData
				file.addPage(guid, page_size, page.getFirstLetter()); // metadata

				// Save page at its corresponding node
				writePageData(page, guid);

				// reset page
				page = new CatalogPage(); // metadata
				page_size = 0; // metadata
			}
			// Save Last Page if its smaller than "items_per_page"
			else if (i == catalogItems.size() - 1) {
				// DEBUG
				System.out.println("\n\tLast Page: smaller than " + this.items_per_page); // DEBUG
				System.out.println("\tpage_size = " + page_size); // DEBUG
				// System.out.println("i + 1 = " + (i+1) ); // DEBUG

				// Hash each page (name + time stamp) to get its GUID
				Long timeStamp = System.currentTimeMillis();
				Long guid = md5(fileName + timeStamp);
				System.out.println("\tguid = " + guid); // DEBUG

				// Update MetaData
				file.addPage(guid, page_size, page.getFirstLetter()); // metadata

				// Save page at its corresponding node
				writePageData(page, guid);

				// reset page
				page = new CatalogPage();
				page_size = 0; // metadata
			}
		}
		// All items have been saved to a page

		// Save metadata.json to Chord
		file.setName(fileName);
		file.setSize(file_size);
		metadata.addFile(file);
		writeMetaData(metadata);
	}

	/**
	 * delete file
	 *
	 * @param filename Name of the file
	 */
	public void delete(String fileName) throws Exception {
		// TODO:
		// Read metadata
		// find filename in metadata
		// for each page of file
		// delete page

		String TAG = "delete";

		FilesJson metadata = readMetaData();
		FileJson file = new FileJson();

		// find file
		for (int i = 0; i < metadata.size(); i++) {
			if (metadata.getFile(i).getName().equals(fileName)) {
				file = metadata.getFile(i);
				// System.out.println(TAG+": file size: " + file.getSize()); // DEBUG
				System.out.println(TAG + ": numberOfPages: " + file.getNumberOfPages()); // DEBUG

				// delete all pages of file
				for (int j = 0; j < file.getNumberOfPages() - 1; j++) {

					Long guid = file.getPage(j).getGUID();
					System.out.println("\tdeleting page: " + j); // DEBUG
					System.out.println("\tguid: " + guid); // DEBUG
					ChordMessageInterface peer = chord.locateSuccessor(guid); // locate successor
					peer.delete(guid);
				}

				// Update metadata
				// TODO
				metadata.deleteFile(fileName);
				System.out.println("delete done."); // DEBUG
				writeMetaData(metadata);
				return;
			}
		}
		System.out.println("file not found: " + fileName); // DEBUG
	}

	/**
	 * Read block pageNumber of fileName //read catalogpage
	 *
	 * @param filename   Name of the file
	 * @param pageNumber number of block.
	 */
	public RemoteInputFileStream read(String fileName, int pageNumber) throws Exception {
		// TODO:

		// DONE:
		// Read metadata
		// find filename in metadata
		// find guid of pageNumber in metadata
		// request page
		// TEST different pageNumbers

		// Debug
		String TAG = "read";
		// System.out.println(TAG + "(fileName, pageNumber)");
		// System.out.println(TAG + "(" + fileName + ", " + pageNumber + ")");

		// Read Metadata
		FilesJson metadata = readMetaData();
		long guid = (long) 0;

		// Find File in metadata
		for (int i = 0; i < metadata.size(); i++) {
			FileJson filejson = metadata.getFile(i);
			// System.out.println("\tfilejson.getName: " + filejson.getName()); // DEBUG

			// if x.getName == filename
			if (filejson.getName().equals(fileName)) {
				// System.out.println("name matched"); // DEBUG
				// get guid of page with "pageNumber"
				guid = filejson.getPage(pageNumber).getGUID();
				// System.out.println("guid retrieved"); // DEBUG
				break;
			}
		}

		ChordMessageInterface peer = chord.locateSuccessor(guid);
		return peer.get(guid);
	}

	// TODO timing
	public JsonObject indexSearch(String filter, int count) throws Exception {

		String TAG = "indexSearch";
		System.out.println(TAG + "(" + filter + ", " + count + ")"); // DEBUG

		// return variable
		JsonArray ret = new JsonArray();

		// get key out of filter // assuming one word for now // for multiple word
		// filter: split filter then do multiple searches
		String key = "?";
		if (filter.length() > 2) {
			key = filter.substring(0, 2);// get first 2 characters only
		}

		// re-generate guid from key
		Long guid = md5(key + "reverseIndex" + key);
		ChordMessageInterface peer = chord.locateSuccessor(guid);

		CatalogPage catalogPage = new CatalogPage();
		try {
			// Remote Input File Stream
			RemoteInputFileStream dataraw = peer.get(guid);// index = page number

			System.out.println("\t" + TAG + ":connecting."); // DEBUG
			dataraw.connect();// new RFIS

			// Scanner
			System.out.println("\t" + TAG + ":scanning."); // DEBUG
			Scanner scan = new Scanner(dataraw);
			scan.useDelimiter("\\A");
			String data = scan.next();
			// System.out.println(data); // DEBUG

			// Convert from json to ArrayList
			System.out.println("\t" + TAG + ":converting json to CatalogPage.");// DEBUG
			Gson gson = new Gson();
			catalogPage = gson.fromJson(data, CatalogPage.class);

			// System.out.println("\t" + TAG + ":Read Complete.");
			// System.out.println("\t page.size(): " + page.size());
			// return page;
		} catch (Exception e) {
			System.out.println(":error in indexSearch: ");
		}

		// search each item in the catalogPage
		System.out.println(TAG + ": searching page..."); // DEBUG
		int songs_found = 0; // Count number of songs found
		for (int j = 0; j < catalogPage.size(); j++) {
			// if item passes filter
			// if(ci.passesFilter(filter))
			if (catalogPage.getItem(j).passesFilter(filter)) {
				// add to response
				// ret.add(ci.getJson());
				ret.add(catalogPage.getItem(j).getJson());

				songs_found = songs_found + 1;
				// System.out.println("\t\tsearch page: " + index); // DEBUG
				System.out.println("\t\tfound so far: " + songs_found); // DEBUG

				if (songs_found >= count) {
					// DEBUG
					System.out.println("max matches found.");
					System.out.println("\tmatches found: " + songs_found);
					JsonObject response = new JsonObject();
					response.add("ret", ret);
					return response;
				}
			}

		}

		// return json array;
		System.out.println("\tmatches found: " + songs_found);
		JsonObject response = new JsonObject();
		response.add("ret", ret);
		return response;

	}

	public JsonObject search(String filter, int count) {
		String TAG = "search"; // DEBUG
		Long startTime = (long) 0; // DEBUG
		Long endTime = (long) 0; // DEBUG
		Long runTime = (long) 0; // DEBUG
		ArrayList<Long> getCatalogTimes = new ArrayList<Long>(); // DEBUG
		ArrayList<Long> searchTimes = new ArrayList<Long>(); // DEBUG

		// return variable
		JsonArray ret = new JsonArray();

		// Get Metadata
		try {
			// Read metadata
			System.out.println(TAG + ": read metadata"); // DEBUG
			FilesJson files = readMetaData();

			// Find music.json in metadata
			System.out.println(TAG + ": Find music.json in metadata"); // DEBUG
			FileJson file = files.getFile(0);// I know this is not good, I'm assuming that only music.json exists in
												// files.
			System.out.println(TAG + ": file.getNumberOfPages(): " + file.getNumberOfPages()); // DEBUG

			// Count number of songs
			int songs_found = 0;

			// search page by page in music.json
			System.out.println(TAG + ": searching pages..."); // DEBUG
			for (int index = 0; index < file.getNumberOfPages(); index++) {
				System.out.print("\tpage: " + index + " "); // DEBUG

				// request page
				startTime = System.currentTimeMillis(); // DEBUG
				CatalogPage catalogPage = new CatalogPage();
	
				try {
					// Remote Input File Stream
					RemoteInputFileStream dataraw = this.read(file.name, index);// index = page number
					// System.out.println("\t"+ TAG+":connecting."); // DEBUG
					dataraw.connect();// new RFIS

					// Scanner
					// System.out.println("\t" + TAG+":scanning."); // DEBUG
					Scanner scan = new Scanner(dataraw);
					scan.useDelimiter("\\A");
					String data = scan.next();
					// System.out.println(data); // DEBUG

					// Convert from json to ArrayList
					// System.out.println("\t" + TAG + ":converting json to CatalogPage.");
					Gson gson = new Gson();
					catalogPage = gson.fromJson(data, CatalogPage.class);

					// System.out.println("\t" + TAG + ":Read Complete.");
					// System.out.println("\t page.size(): " + page.size());
					// return page;
				} catch (Exception e) {
					System.out.println(TAG + ": error reading page: " + index);
					index = index - 1;
					this.sleepTime = 450;
				}
				endTime = System.currentTimeMillis(); // DEBUG
				runTime = endTime - startTime;
				getCatalogTimes.add(runTime);
				System.out.print(",\tread:" + runTime + "(milisec)"); // DEBUG //readTime
				if (this.sleepTime > 300) {

					this.sleepTime = this.sleepTime - 10;
				}

				// search each item in the catalogPage
				startTime = System.currentTimeMillis(); // DEBUG
				for (int j = 0; j < catalogPage.size(); j++) {
					// CatalogItem ci = catalogPage.getItem(j);//TODO: replace by direct reference?

					// if item passes filter
					// if(ci.passesFilter(filter))
					if (catalogPage.getItem(j).passesFilter(filter)) {
						// add to response
						// ret.add(ci.getJson());
						ret.add(catalogPage.getItem(j).getJson());

						songs_found = songs_found + 1;
						// System.out.println("\t\tsearch page: " + index); // DEBUG
						System.out.println("\t\tfound so far: " + songs_found); // DEBUG

						if (songs_found >= count) {
							// DEBUG
							Long sum = (long) 0;
							for (int i = 0; i < getCatalogTimes.size(); i++) {
								sum = sum + getCatalogTimes.get(i);
							}
							Long average = sum / (long) getCatalogTimes.size();
							System.out.println("Average page request: " + average);
							System.out.println("max matches found.");
							System.out.println("\tmatches found: " + songs_found);
							JsonObject response = new JsonObject();
							response.add("ret", ret);
							return response;
						}
					}

				}
				endTime = System.currentTimeMillis(); // DEBUG
				runTime = endTime - startTime;
				searchTimes.add(runTime);
				System.out.println(",\t" + runTime + "(milisec)"); // DEBUG // Search time
			}
			// searched all pages.
			// return json array;
			// DEBUG
			Long sum = (long) 0;
			for (int i = 0; i < getCatalogTimes.size(); i++) {
				sum = sum + getCatalogTimes.get(i);
			}
			Long searchSum = (long) 0;
			for (int i = 0; i < searchTimes.size(); i++) {
				searchSum = searchSum + searchTimes.get(i);
			}

			Long average = sum / (long) getCatalogTimes.size();
			Long searchAverage = sum / (long) getCatalogTimes.size();
			System.out.println("Average page request: " + average);
			System.out.println("Average page search: " + searchAverage);

			System.out.println("Searched all pages");
			System.out.println("\tmatches found: " + songs_found);
			JsonObject response = new JsonObject();
			response.add("ret", ret);
			return response;

		} catch (IOException e) {
			e.printStackTrace();

			// error happened in readmetadata?.
			// return empty json array;
			JsonObject response = new JsonObject();
			response.add("ret", ret);
			return response;
		} catch (Exception e) {
			// error happened in ????
			// return empty json array;
			JsonObject response = new JsonObject();
			response.add("ret", ret);
			return response;
		}
	}

	/**
	 * Add a page to the file
	 *
	 * @param fileName Name of the file
	 * @param data     RemoteInputStream.
	 */
	public void append(String fileName, RemoteInputFileStream data) throws Exception {
		// appending? mp3? or music.json CatalogItem?

		// generate guid
		Long timeStamp = System.currentTimeMillis();
		Long guid = md5(fileName + timeStamp);
		System.out.println("\tguid = " + guid); // DEBUG

		// update metadata
		// get metadata
		FilesJson metadata = readMetaData();

		// add data to page
		// TODO

		// locate peer
		ChordMessageInterface peer = chord.locateSuccessor(guid);
		System.out.println("\tSaving Page to peer: " + peer.getId()); // DEBUG

		// save data to peer
		peer.put(guid, data); // send page

	}

	public void generateKeyGuid(String word) {
		System.out.println("word: " + word); // DEBUG

		String key = word.substring(0, 2);
		System.out.println("key: " + key); // DEBUG

		Long guid = md5(key + "reverseIndex" + key);
		System.out.println("guid: " + guid); // DEBUG
	}

	// WIP: Work In Progress
	// Precondition: create(music.json) was called before.
	// All unsorted pages have been saved to the peers in the chord.
	public void runMapReduce(String fileName) throws Exception {
		String TAG = "runMapReduce()";
		// -----Outline-----
		// read music.json metadata

		// All peers map:
		// for each page in music.json
		// peer = locateSuccessor(page.guid)
		// peer.map(guid)

		// wait until all pages are mapped

		// All peers sendAll()

		// wait until all keys are stored at their proper peer.

		// All peers bulk() //Save all nodes in TreeMap as their own file).

		// -----Implementation-----
		// read music.json metadata
		FileJson file = new FileJson();
		try {

			FilesJson files = readMetaData();

			// Find music.json in metadata
			System.out.println(TAG + ": Find music.json in metadata"); // DEBUG
			file = files.getFile(0);// I know this is not good, I'm assuming that only music.json exists in files.
			System.out.println(TAG + ": file.getNumberOfPages(): " + file.getNumberOfPages()); // DEBUG

		} catch (Exception e) {
			System.out.println(TAG + ": error reading metadata");
		}

		// for each page in music.json
		// get guid
		// locateSuccessor(page.guid)
		// map(guid)
		System.out.println(TAG + ": mapping pages..."); // DEBUG
		for (int index = 0; index < file.getNumberOfPages(); index++) {
			// get guid
			Long guid = file.getPage(index).getGUID();

			try {

				// locate successor
				ChordMessageInterface peer = chord.locateSuccessor(guid);

				// map(guid)
				peer.map(fileName, guid);

			} catch (Exception e) {
				System.out.println("Error: runMapReduce: trying to locateSuccessor() failed");
			}

		}

		// wait until all pages are mapped
		// Currently Debugging

		// WIP: Work In Progress

		/**
		 * 
		 * //All peers sendAll() System.out.println("Calling Peers to sendAll()");
		 * chord.callSuccesorToSendAll(chord.getId(), 0);
		 * 
		 * //wait until all keys are stored at their proper peer.
		 * System.out.println("waiting on peers to finish sending."); done = false;
		 * while(!done) { Thread.sleep(2000); // Sleep to prevent sending too many
		 * messages while checking the chord state
		 * 
		 * try { Long id =chord.getId(); chord.arePagesMapped(id,"music.json", true, 0
		 * ); Thread.sleep(500); // Sleep to prevent sending too many messages while
		 * checking the chord state done = chord.mappedState; } catch(Exception e) {
		 * System.out.println("Error: arePagesMapped: chord.getId(): "); } }
		 * 
		 * 
		 * //All peers bulk() //Save all nodes in TreeMap as their own file).
		 * System.out.println("Calling Peers to bulk()");
		 * 
		 * 
		 * //Done System.out.println("runMapReduce: done");
		 * 
		 **/
	}

	public void setChordState() {
		chord.mappedState = true;
	}

	public void resetChordState() {
		chord.mappedState = false;
	}
}