import java.io.*;
import java.net.*;

public class Client implements Runnable{
	
	private int port;
	private int peerID; 
	
	public Client(int port, int peerID){
		this.port = port;
		this.peerID = peerID;
	}
	
    public void run ()
    {
	
		String hostname = "localhost";
		boolean tryToConnect = true;
		
	    Socket clientSocket = null;  
	    DataOutputStream outputStream = null;
	    BufferedReader inputStream = null;
	    
	    boolean connected = false;
		
		// Try to open a socket on the given port
		// Try to open input and output streams
	    while(tryToConnect)
	    {
	        try 
	        {
	            clientSocket = new Socket(hostname, port);
	            outputStream = new DataOutputStream(clientSocket.getOutputStream());
	            inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	        } 
	        catch (UnknownHostException e) 
	        {
	            System.err.println("Couldn't find the following host: " + hostname);
	        } 
	        catch (IOException e) 
	        {
	            System.err.println("Couldn't get I/O for the connection to: " + hostname);
	        }
			
			if (clientSocket == null || outputStream == null || inputStream == null) 
			{
			    System.err.println( "Something is wrong. One variable is null." );
			    tryToConnect = true;
			}
			else
			{
				tryToConnect = false;
			}
		}
	
		try {
		    while ( true ) 
		    {
		    	String sentence = "";
		    	
				//takes user input from command line
				//System.out.print( "Enter a sentence (q to stop connection, q to stop server): " );
				//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				//String sentence = br.readLine();
				
		    	if(!connected)
		    	{
					sentence = "HELLO" + "00000000000000000000000" + peerID;
					
					outputStream.writeBytes( sentence + "\n" );
					
					connected = true;
		    	}

		
				if ( sentence.equalsIgnoreCase("q") ) 
				{
				    break;
				}
				
				String modifiedSentence = inputStream.readLine();
				System.out.println("FROM SERVER: " + modifiedSentence);
		    }
		    
		    // close the output stream
		    // close the input stream
		    // close the socket
		    
		    outputStream.close();
		    inputStream.close();
		    clientSocket.close();   
		} catch (UnknownHostException e) {
		    System.err.println("Trying to connect to unknown host: " + e);
		} catch (IOException e) {
		    System.err.println("IOException:  " + e);
		}
    }           
}
