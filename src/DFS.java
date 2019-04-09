
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.math.BigInteger;
import java.security.*;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Distributed file system that utilizes chord peer to peer
 * 
 * @author nanaeaubry
 *
 */

public class DFS {

	// METADATA CLASSES---------------------------

	/**
	 * Music.json will be distributed into pages to be held on each node of the
	 * chord
	 * 
	 * @author nanaeaubry
	 *
	 */
	public class PagesJson {
		Long guid;
		int size;

		public PagesJson() {
			guid = (long) 0;
			size = 0;
		}

		// getters
		public Long getGUID() {
			return guid;
		}

		public int getSize() {
			return size;
		}

		// setters
		public void setGUID(Long guid) {
			this.guid = guid;
		}

		public void setSize(int size) {
			this.size = size;
		}

	};

	/**
	 * File will be an arraylist of all the pages found on the chord for that file
	 * 
	 * @author nanaeaubry
	 *
	 */

	public class FileJson {
		String name;
		Long size;
		int numberOfItems;
		int itemsPerPage;
		ArrayList<PagesJson> pages;

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

		/**
		 * Add a page to the file
		 * 
		 * @param page the page to be added
		 */
		public void addPage(PagesJson page) {
			this.pages.add(page);
			this.size += page.getSize();
		}

		/**
		 * Add a page based on its id and size
		 * 
		 * @param guid      id of the page
		 * @param page_size size that page is
		 */
		public void addPage(Long guid, int page_size) {
			PagesJson page = new PagesJson(); // metadata
			page.setGUID(guid); // metadata
			page.setSize(page_size); // metadata

			this.addPage(page);
		}
	};

	/**
	 * All files that are on the chord
	 * 
	 * @author nanaeaubry
	 *
	 */
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

		/**
		 * Delete a file from the files
		 * 
		 * @param fileName name of the file to be deleted
		 */
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

	ArrayList<CatalogItem> catalogItems;
	int port;
	Chord chord;

	// END DFS Variables

	public DFS(int port) throws Exception {
		catalogItems = new ArrayList<CatalogItem>();

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

	/**
	 * Join the chord as a node
	 * 
	 * @param Ip   ip of joining server
	 * @param port port to join
	 * @throws Exception if there is nothing to join
	 */
	public void join(String Ip, int port) throws Exception {
		chord.joinRing(Ip, port);
		chord.print();
	}

	/**
	 * Leave the chord
	 * 
	 * @throws Exception
	 */
	public void leave() throws Exception {
		chord.leave();
	}

	/**
	 * Print the status of the peers in the chord
	 * 
	 * @throws Exception
	 */
	public void print() throws Exception {
		chord.print();
	}

	/**
	 * Read metadata from the chord
	 * 
	 * @return files that are on the chord
	 * @throws Exception
	 */
	public FilesJson readMetaData() throws Exception {

		FilesJson filesJson = null;
		try {
			Gson gson = new Gson();
			long guid = md5("Metadata");
			ChordMessageInterface peer = chord.locateSuccessor(guid);
			RemoteInputFileStream metadataraw = peer.get(guid);
			metadataraw.connect();
			Scanner scan = new Scanner(metadataraw);
			scan.useDelimiter("\\A");
			String strMetaData = scan.next();

			filesJson = gson.fromJson(strMetaData, FilesJson.class);
		} catch (NoSuchElementException ex) {
			filesJson = new FilesJson();
		}
		return filesJson;
	}

	/**
	 * Write metadata to the chord
	 * 
	 * @param filesJson files to write to
	 * @throws Exception
	 */
	public void writeMetaData(FilesJson filesJson) throws Exception {
		long guid = md5("Metadata");
		ChordMessageInterface peer = chord.locateSuccessor(guid);
		System.out.println("\tSaving Metadata to peer: " + peer.getId()); // DEBUG

		Gson gson = new Gson();
		peer.put(guid, gson.toJson(filesJson));
	}

	/**
	 * Write page data to the chord
	 * 
	 * @param catalogpage page that will be written in
	 * @param guid        unique id of the page
	 * @throws Exception
	 */
	public void writePageData(CatalogPage catalogpage, Long guid) throws Exception {
		ChordMessageInterface peer = chord.locateSuccessor(guid);
		System.out.println("\tSaving Page to peer: " + peer.getId()); // DEBUG

		Gson gson = new Gson();
		String jsonString = gson.toJson(catalogpage); // Convert CatalogPage to Json
		peer.put(guid, jsonString); // send page
	}

	/**
	 * Change the name of hte metadata
	 * 
	 * @param oldName old name of the metadata
	 * @param newName new name for the same metadata
	 * @throws Exception
	 */
	public void move(String oldName, String newName) throws Exception {
		// TODO: Change the name in Metadata
		// Write Metadata
	}

	/**
	 * List files in the system
	 * 
	 * @return String of file names
	 * @throws Exception
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

		if (files.size() == 0) {
			return "Empty";
		}

		return listOfFiles;
	}

	/**
	 * Generate global unique identifier for file
	 * @param fileName name of the file that needs a guid
	 * @return
	 */
	public Long generateGUID(String fileName) {
		Long timeStamp = System.currentTimeMillis();
		return md5(fileName + timeStamp);
	}

	/**
	 * Creat MP3 for a file
	 * @param fileName name of the file the MP3 will be created in
	 */
	public void createMP3(String fileName) {

	}

	/**
	 * Create an empty file
	 * @param fileName name of the empty file
	 * @throws Exception
	 */
	public void create(String fileName) throws Exception {

		// DEBUG
		String TAG = "DFS.create";
		System.out.println(TAG + "(" + fileName + ")");

		// LoadCatalog
		loadCatalog(fileName);
		System.out.println(TAG + ":catalogItems.size() = " + catalogItems.size()); // DEBUG

		// Variables
		int songs_per_page = 50;
		CatalogPage catalogpage = new CatalogPage();
		// OLD
		FileJson file = new FileJson(); // metadata
		FilesJson metadata = new FilesJson(); // metadata
		int page_size = 0;
		Long file_size = (long) 0;

		// Split file into n pages
		// for each item in catalog save it to a "page"
		for (int i = 0; i < catalogItems.size(); i++) {

			// Page groups of size "songs_per_page"
			page_size = page_size + 1;

			// get item from catalog and add it to the page
			catalogpage.addItem(catalogItems.get(i));

			// if page size reaches "songs_per_page" save the page
			if ((i + 1) % songs_per_page == 0) {

				// DEBUG
				// System.out.println("i + 1 = " + (i+1) );
				System.out.println("\tpage_size = " + page_size);

				// Hash each page (name + time stamp) to get its GUID
				Long timeStamp = System.currentTimeMillis();
				Long guid = md5(fileName + timeStamp);
				System.out.println("\tguid = " + guid); // DEBUG

				// Update MetaData
				file.addPage(guid, page_size); // metadata

				// Save page at its corresponding node
				writePageData(catalogpage, guid);

				// reset page
				catalogpage = new CatalogPage();
				page_size = 0; // metadata
			}

			// Save Last Page if its smaller than "songs_per_page"
			else if (i == catalogItems.size() - 1) {
				// DEBUG
				System.out.println("\tLast Page: smaller than " + songs_per_page); // DEBUG
				System.out.println("\tpage_size = " + page_size); // DEBUG
				// System.out.println("i + 1 = " + (i+1) ); // DEBUG

				// Hash each page (name + time stamp) to get its GUID
				Long timeStamp = System.currentTimeMillis();
				Long guid = md5(fileName + timeStamp);
				System.out.println("\tguid = " + guid); // DEBUG

				// Update MetaData
				file.addPage(guid, page_size); // metadata

				// Save page at its corresponding node
				writePageData(catalogpage, guid);

				// reset page
				catalogpage = new CatalogPage();
				page_size = 0;
			}
		}

		// Save metadata.json to Chord
		file.setName(fileName);
		file.setSize(file_size);
		metadata.addFile(file);
		writeMetaData(metadata);
	}

	/**
	 * Load the catalog
	 * @param fileName name of file to be loaded
	 */
	public void loadCatalog(String fileName) {

		// DEBUG
		String TAG = "loadCatalog";
		System.out.println(TAG + "(" + fileName + ")");

		// Try to open the file for reading
		try {

			// JSON
			JsonReader jsonReader = new JsonReader(new FileReader(fileName));

			// GSON
			Gson gson = new Gson();

			jsonReader.beginArray();

			while (jsonReader.hasNext()) {
				CatalogItem item = gson.fromJson(jsonReader, CatalogItem.class);
				catalogItems.add(item);
			}
			jsonReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Delte file
	 * @param fileName name of file to delete
	 * @throws Exception
	 */
	public void delete(String fileName) throws Exception {

		String TAG = "delete";

		FilesJson metadata = readMetaData();
		FileJson file = new FileJson();

		// find file
		for (int i = 0; i < metadata.size(); i++) {
			if (metadata.getFile(i).getName().equals(fileName)) {
				file = metadata.getFile(i);
				System.out.println(TAG + ": file size: " + file.getSize()); // DEBUG
				System.out.println(TAG + ": numberOfPages: " + file.getNumberOfPages()); // DEBUG

				// delete all pages of file
				for (int j = 0; j < file.getNumberOfPages() - 1; j++) {

					Long guid = file.getPage(j).getGUID();
					System.out.println("\tdeleting page: " + j); // DEBUG
					System.out.println("\tguid: " + guid); // DEBUG
					ChordMessageInterface peer = chord.locateSuccessor(guid); // locate successor
					peer.delete(guid);
				}

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
		
		// Debug
		String TAG = "read";
		// System.out.println(TAG + "(fileName, pageNumber)");
		// System.out.println(TAG + "(" + fileName + ", " + pageNumber + ")");

		// Read Metadata
		FilesJson metadata = readMetaData();
		long guid = (long) 0;

		// Find File in metadata
		// for(FileJson filejson : metadata)
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

	/**
	 * Search for a song in the pages
	 * @param filter filter to be applied
	 * @param count number of max results to return
	 * @return
	 */
	public JsonObject search(String filter, int count) {
		String TAG = "search"; // DEBUG

		// return variable
		JsonArray ret = new JsonArray();

		// Get Metadata
		try {
			// Read metadata
			System.out.println(TAG + ": read metadata"); // DEBUG
			FilesJson files = readMetaData();

			System.out.println(TAG + ": Use first file in metadata"); // DEBUG
			FileJson file = files.getFile(0);
			System.out.println(TAG + ": file.getNumberOfPages(): " + file.getNumberOfPages()); // DEBUG

			// Find count number of songs
			int songs_found = 0;

			// search page by page in file
			System.out.println(TAG + ": searching pages..."); // DEBUG
			for (int index = 0; index < file.getNumberOfPages(); index++) {
				System.out.println("\tpage: " + index); // DEBUG

				// request page
				CatalogPage catalogpage = getCatalogPage(file.name, index);// TODO: replace with direct reference

				// search each item in the catalogpage
				for (int j = 0; j < catalogpage.size(); j++) {
					CatalogItem ci = catalogpage.getItem(j);// TODO: replace by direct reference?

					// if item passes filter
					if (ci.passesFilter(filter)) {
						// add to response
						ret.add(ci.getJson());

						songs_found = songs_found + 1;

						if (songs_found >= count) {
							System.out.println("max matches found.");
							System.out.println("\tmatches found: " + songs_found);
							JsonObject response = new JsonObject();
							response.add("ret", ret);
							return response;
						}
					}

				}
			}
			// searched all pages.
			// return json array;
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
	 * Get a catalog page from a file
	 * @param filename name of the file the page is in
	 * @param pageNumber number of the page
	 * @return specific page
	 */
	public CatalogPage getCatalogPage(String filename, int pageNumber) {
		String TAG = "getCatalogPage";
		// System.out.println(TAG + "(pageNumber)" ); // DEBUG
		// System.out.println(TAG + "("+ pageNumber + ")" ); // DEBUG

		try {
			// Remote Input File Stream
			RemoteInputFileStream dataraw = this.read(filename, pageNumber);
			// System.out.println("\t"+ TAG+":connecting."); // DEBUG
			dataraw.connect();

			// Scanner
			// System.out.println("\t" + TAG+":scanning."); // DEBUG
			Scanner scan = new Scanner(dataraw);
			scan.useDelimiter("\\A");
			String data = scan.next();
			// System.out.println(data); // DEBUG

			// Convert from json to ArrayList
			// System.out.println("\t" + TAG + ":converting json to CatalogPage.");
			CatalogPage page = new CatalogPage();
			Gson gson = new Gson();
			page = gson.fromJson(data, CatalogPage.class);

			// System.out.println("\t" + TAG + ":Read Complete.");
			// System.out.println("\t page.size(): " + page.size());
			return page;
		} catch (Exception e) {
			return new CatalogPage();
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
}