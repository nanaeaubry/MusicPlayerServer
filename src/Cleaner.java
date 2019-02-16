import java.io.File;


public class Cleaner implements Runnable 
{	
	//Attributes
	//--------------------
	private boolean running;		  //True when running
	private long period = 60000 * 5;  //60000 = 1 minute	
	private long counter = 0;		  //count if the period was reached

	//Methods
	//--------------------		
	//Initialize the manager
	public Cleaner(long minutes)
	{
		if (minutes > 0) period = 60000 * minutes;
		running = false;
	}

	//Returns True if the thread is running
	public boolean isRunning()
	{
		return(running);
	}
	
	//stop the service
	public void stop()
	{
		running = false;
	}	
	

	//recover the information from disk
	public void maintenanceFolder(String path)
	{								
		File folder = new File(path); 		
		File[] listOfFiles = folder.listFiles();
		long current = System.currentTimeMillis();

		//check the folder content
		for (int i=0; i < listOfFiles.length; i++)
		{
			long timestamp = listOfFiles[i].lastModified();
			long transcurred = current - timestamp;
			if (transcurred > period)
			{
				listOfFiles[i].delete();
			}			
		}				
	}
	
	
	@Override
	public void run() {
		System.out.println("Cleaner running.");
		
		running = true;
		while (running)
		{			
			try {	//1 millisecond that is multiplied by the period
					Thread.sleep(1);
					
					//Sleep for a period
					if (++counter >= period)
					{
						counter =0;
						System.out.println(" Starting Cleaning log files ");					
						maintenanceFolder(Session.logsExecRPC);
						maintenanceFolder(Session.logsOutRPC);
						System.out.println(" Cleaning done .. ");
					}
					
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} //End of while loop
		
		//Service was stopped
		System.out.println("Cleaner stopped.");
		
	} //End Run
	
} //End Class
