
/**
* The Dispatcher implements DispatcherInterface. 
*
* @author  Oscar Morales-Ponce
* @version 0.15
* @since   02-11-2019 
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

		JsonObject response = new JsonObject();

		// Get service for the request
		Object service = listOfServices.get(request.get("serviceName").getAsString());

		try {

			// Get the list of methods on the service
			Method[] methods = service.getClass().getMethods();

			// Find the method requested
			String methodName = request.get("remoteMethod").getAsString();
			Method method = null;
			for (int i = 0; i < methods.length && method == null; i++) {
				if (methods[i].getName().equals(methodName))
					method = methods[i];
			}
			if (method == null) {
				response.addProperty("error", "Method does not exist");
				return response;
			}

			// Read params as String
			Class<?>[] types = method.getParameterTypes();
			String[] strParam = new String[types.length];
			JsonObject jsonParam = request.get("param").getAsJsonObject();
			int j = 0;
			for (Map.Entry<String, JsonElement> entry : jsonParam.entrySet()) {
				strParam[j++] = entry.getValue().getAsString();
			}

			// Convert string params to the correct type
			Object[] parameter = new Object[types.length];
			for (int i = 0; i < types.length; i++) {
				switch (types[i].getCanonicalName()) {
				case "java.lang.Long":
					parameter[i] = Long.parseLong(strParam[i]);
					break;
				case "java.lang.Integer":
					parameter[i] = Integer.parseInt(strParam[i]);
					break;
				case "String":
					parameter[i] = new String(strParam[i]);
					break;
				}
			}

			// Invoke the method
			Class<?> returnType = method.getReturnType();
			String ret = "";
			switch (returnType.getCanonicalName()) {
			case "java.lang.Long":
				ret = method.invoke(service, parameter).toString();
				break;
			case "java.lang.Integer":
				ret = method.invoke(service, parameter).toString();
				break;
			case "java.lang.String":
				ret = (String) method.invoke(service, parameter);
				break;
			}
			response.addProperty("ret", ret);

		} catch (InvocationTargetException | IllegalAccessException e) {
			response.addProperty("error",
					"Error on " + request.get("objectName").getAsString() + "." + request.get("remoteMethod").getAsString());
		}

		return response;
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

	/*
	 * Testing public static void main(String[] args) { // Instance of the
	 * Dispatcher Dispatcher dispatcher = new Dispatcher(); // Instance of the
	 * services that te dispatcher can handle SongDispatcher songDispatcher = new
	 * SongDispatcher();
	 * 
	 * dispatcher.registerObject(songDispatcher, "SongServices");
	 * 
	 * // Testing the dispatcher function // First we read the request. In the final
	 * implementation the jsonRequest // is obtained from the communication module
	 * try { String jsonRequest = new
	 * String(Files.readAllBytes(Paths.get("./getSongChunk.json"))); String ret =
	 * dispatcher.dispatch(jsonRequest); System.out.println(ret);
	 * 
	 * //System.out.println(jsonRequest); } catch (Exception e) {
	 * System.out.println(e); }
	 * 
	 * }
	 */
}
