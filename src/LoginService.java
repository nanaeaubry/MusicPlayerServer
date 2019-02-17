import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class LoginService {

	private static ArrayList<String> accounts = null; // list of valid accounts
	private static ArrayList<String> uids = null; // list of valid UIDs

	public LoginService() {
		accounts = new ArrayList<String>();
		uids = new ArrayList<String>();
		loadAccounts();
	}

	private void loadAccounts() {
		try {
			String line = "";
			accounts.clear();
			uids.clear();

			// Open the file for reading
			File file = new File("./assets/accounts.txt");
			BufferedReader buffer = new BufferedReader(new FileReader(file));

			// load the media object
			while ((line = buffer.readLine()) != null) {
				String[] parts = line.split(";");
				accounts.add(parts[0] + ";" + parts[1]);
				uids.add(parts[2]);
			}

			// Close the file
			buffer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String login(String username, String password) {
		String uid = ""; // by default empty uid, which means unauthorized user
		String attempt = username.trim() + ";" + password.trim(); // build the possible account

		// Try to get the user from the list
		int index = accounts.indexOf(attempt);
		if (index >= 0) {
			uid = uids.get(index); // gets the UID if user is authorized
		}

		return uid;
	}
}
