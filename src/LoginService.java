import java.util.ArrayList;

public class LoginService {
	
	private ArrayList<User> users;
	
	public LoginService() {
		JSONParser parser = new JSONParser();
Object obj = parser.parse(new FileReader("test.json"));
		JSONObject jsonObject = (JSONObject)obj;	}
	
	public String authenticate(String username, String password) {
		
	}

	protected class User {
		String username;
		String password;
		UserInfo info;
	}
	
	protected class UserInfo {
		String firstName;
		String lastName;
	}
}
