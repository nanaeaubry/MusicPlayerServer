
/**
* The Dispatcher implements DispatcherInterface. 

*/

import java.util.HashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.lang.reflect.*;

public class Dispatcher implements DispatcherInterface {
	HashMap<String, Object> listOfServices;

	public Dispatcher() {
		listOfServices = new HashMap<String, Object>();
	}

	/*
	 * dispatch: Executes the remote method in the corresponding Object
	 * 
	 * @param request: Request: it is a Json file { "remoteMethod":"getSongChunk",
	 * "objectName":"SongServices", "param": { "song":490183, "fragment":2 } }
	 */
	public JsonObject dispatch(JsonObject request) {

		// Get service for the request
		Object service = listOfServices.get(request.get("serviceName").getAsString());
		if (service == null) {
			JsonObject response = new JsonObject();
			response.addProperty("error", "Service does not exist");
			return response;
		}

		try {

			// Get the list of methods on the service
			Method[] methods = service.getClass().getMethods();

			// Find the method requested
			String methodName = request.get("methodName").getAsString();
			Method method = null;
			for (int i = 0; i < methods.length && method == null; i++) {
				if (methods[i].getName().equals(methodName))
					method = methods[i];
			}
			if (method == null) {
				JsonObject response = new JsonObject();
				response.addProperty("error", "Method does not exist");
				return response;
			}

			// Read param object
			JsonObject jsonParam = request.get("param").getAsJsonObject();

			// Invoke the method
			return (JsonObject) method.invoke(service, jsonParam);

		} catch (InvocationTargetException | IllegalAccessException e) {
			JsonObject response = new JsonObject();
			response.addProperty("error", "Error on " + request.get("objectName").getAsString() + "."
					+ request.get("remoteMethod").getAsString());
			return response;
		}

	}

	/*
	 * registerObject: It register the objects that handle the request
	 * 
	 * @param remoteMethod: It is the name of the method that objectName implements.
	 * 
	 * @objectName: It is the main class that contains the remote methods each
	 * object can contain several remote methods
	 */
	public void registerService(String serviceName, Object service) {
		listOfServices.put(serviceName, service);
	}

}
