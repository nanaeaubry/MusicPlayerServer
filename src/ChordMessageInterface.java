import java.rmi.*;
import java.io.*;

public interface ChordMessageInterface extends Remote
{
    public ChordMessageInterface getPredecessor()  throws RemoteException;
    ChordMessageInterface locateSuccessor(long key) throws RemoteException;
    ChordMessageInterface closestPrecedingNode(long key) throws RemoteException;
    public void joinRing(String Ip, int port)  throws RemoteException;
    public void joinRing(ChordMessageInterface successor)  throws RemoteException;
    public void notify(ChordMessageInterface j) throws RemoteException;
    public boolean isAlive() throws RemoteException;
    public long getId() throws RemoteException;
    
    
    public void put(long guidObject, RemoteInputFileStream inputStream) throws IOException, RemoteException;
    public void put(long guidObject, String text) throws IOException, RemoteException;
    public RemoteInputFileStream get(long guidObject) throws IOException, RemoteException;   
    public byte[] get(long guidObject, long offset, int len) throws IOException, RemoteException;  
    public void delete(long guidObject) throws IOException, RemoteException;

    public void onChordSize(long source, int n) throws RemoteException;
    public void onPageCompleted(String file) throws RemoteException;
    //public void mapContext(int page, Mapper mapper, ChordMessageInterface coordinator, String file);
    //public void reduceContext(int page, Mapper reducer, ChordMessageInterface coordinator, String file);
    public void addKeyValue(long key, int value) throws RemoteException;
    public void emit(long key, int value, String file) throws RemoteException; // Sends
    public void bulk(int page) throws RemoteException;

    public void map(String fileName, long guid) throws RemoteException;
    public void store(RemoteInputFileStream rifs) throws RemoteException;
    public void arePagesMapped(long source, String fileName, Boolean state, int n) throws RemoteException;
    public void callSuccesorToSendAll(long source, int n) throws RemoteException;
	public void arePagesSent(long source, String fileName, Boolean sentState, int i) throws RemoteException;
	public void callSuccesorToBulk(long source, int i) throws RemoteException;
}
