import com.google.gson.JsonObject;

/**
 * A process is created
 * @author nanaeaubry
 *
 */

public class Process implements Runnable {


	private RPCDescriptor rpc;
	private Dispatcher dispatcher;
		
	//Initialize the process	
	public Process(Dispatcher disp, RPCDescriptor rpc)
	{
		this.dispatcher = disp;
		this.rpc = rpc;
	}

	//Executes the process according its ID
	@Override
	public void run() {
		
		// Ask the dispatcher to dispatch this rpc and wait for the response
		JsonObject response = dispatcher.dispatch(rpc.getExecute());
		
		// Make rpc for the reply
		RPCDescriptor reply = new RPCDescriptor(rpc.getSourceAddress(), rpc.getSourcePort(), RPCDescriptor.REPLY, rpc.getRequestId(), rpc.getUserId(), response);
		
		// Push the reply rpc in the sender queue
		Sender.insert(reply);
		
		// Inform the manager that this rpc has been executed by removing it from the current rpc list
		RPCManager.removeRPC(rpc);
	}	
	
	
}//End Class
