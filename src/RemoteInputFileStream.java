/**
* RemoteInputFileStream Implements an Input Stream for big
* files. It creates a server and return the address
* The client must call connect() before reading
*
* @author  Oscar Morales-Ponce
* @version 0.15
* @since   03-3-2019
*/

import java.io.*;
import java.nio.*;
import java.net.*;
import java.util.concurrent.Semaphore;


public class RemoteInputFileStream extends InputStream implements Serializable {

    private String TAG = "RemoteInputFileStream";
    public InetAddress IP;
    public int port;
    public int total;
    public int pos;
    public InputStream input;
    public Semaphore sem;
    private static int BUFFER_LENGTH = 2 << 15;
    /**
     * It stores a buffer with FRAGMENT_SIZE bytes for the current reading.
     * This variable is useful for UDP sockets. Thus bur is the datagram
     */
    protected byte buf[];
    /**
     * It prepares for the nuext buffer. In UDP sockets you can read nextbufer
     * while buf is in use
     */
    protected byte nextBuf[];
     /**
     * It is used to read the buffer
     */
    protected int fragment = 0;

/**
 * Connects to the server to provide the file
 */
    public void connect()
    {
        this.buf  = new byte[BUFFER_LENGTH];
        this.nextBuf  = new byte[BUFFER_LENGTH];
        pos = 0;
        try
        {
            Socket socket = new Socket(IP.getLoopbackAddress(), port);
            input = socket.getInputStream();
            sem = new Semaphore(1);
            sem.acquire();
            getBuff(fragment);
            fragment++;
        } catch (Exception exc) {
            System.out.println(exc);
        }
    }

    public  RemoteInputFileStream()  throws FileNotFoundException {


    }



/**
 * Starts a server to provide the file
 */
    public  RemoteInputFileStream(String pathName, boolean deleteAfter) throws FileNotFoundException, IOException    {
        File file = new File(pathName);
        total = (int)file.length();
        pos = 0;

        try
        {
            ServerSocket serverSocket = new ServerSocket(0);
            port = serverSocket.getLocalPort();
            IP = serverSocket.getInetAddress();

             new Thread() {
                public void run() {
                    try {
                        Socket socket = serverSocket.accept();
                        OutputStream socketOutputStream = socket.getOutputStream();
                        FileInputStream is = new FileInputStream(pathName);
                        while (is.available() > 0)
                            socketOutputStream.write(is.read());
                        is.close();
                        if (deleteAfter)
                        {
                          file.delete();
                        }
                    } catch(Exception v) {
                        System.out.println(v);
                    }
                }
            }.start();
        } catch (Exception exc) {
            System.out.println(exc);
        }
    }

    public  RemoteInputFileStream(String pathName) throws FileNotFoundException, IOException
    {
       this(pathName, false);
    }

     /**
     * getNextBuff reads the buffer. It gets the data using
     * the remote method getSongChunk
    */
    protected void getBuff(int fragment) throws IOException
    {
        new Thread()
        {
            public void run() {
                try
                {
                    Thread.sleep(500);
                    input.read(nextBuf);
                    sem.release();
                }
                catch (Exception e)
                {

                }
            }
        }.start();

     }



/**
     * Reads the next byte of data from the input stream.
    */
    @Override
    public synchronized int read() throws IOException {


	  if (pos >= total)
	  {
            pos = 0;
            return -1;
	  }
	  int posmod = pos % BUFFER_LENGTH;
	  if (posmod == 0)
	  {
          try
          {
            sem.acquire();
          }catch (InterruptedException exc)
          {
                System.out.println(exc);
          }
	      for (int i=0; i< BUFFER_LENGTH; i++)
		      buf[i] = nextBuf[i];

	      getBuff(fragment);
	      fragment++;
	  }
	  int p = pos % BUFFER_LENGTH;
	  pos++;
      return buf[p] & 0xff;
    }

    /**
     * Reads some number of bytes from the input stream and stores them
     * into the buffer array b.
    */
    @Override
    public synchronized int read(byte b[], int off, int len)  throws IOException{
        if (b == null) {
            throw new NullPointerException();
	    } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
	    }

	    if (pos >= total) {
     	    return -1;
	    }
	    int avail = total - pos;
	    if (len > avail) {
		  len = avail;
	    }
	    if (len <= 0) {
		  return 0;
	    }
	    for (int i = off; i< off+len;  i++)
		    b[i] = (byte)read();
	    return len;
    }

    public int available() throws IOException
    {
	return total - pos;
    }


}
