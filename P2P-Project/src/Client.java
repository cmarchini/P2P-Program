import java.io.*;
import java.net.*;

public class Client implements Runnable{
	
	private int port;
	
	public Client(int port){
		this.port = port;
	}
	
    public void run (){
	
	String hostname = "localhost";
	boolean tryToConnect = true;

	// declaration section:
	// clientSocket: our client socket
	// os: output stream
	// is: input stream
	
        Socket clientSocket = null;  
        DataOutputStream os = null;
        BufferedReader is = null;
	
	// Initialization section:
	// Try to open a socket on the given port
	// Try to open input and output streams
    	while(tryToConnect){
        try {
            clientSocket = new Socket(hostname, port);
            os = new DataOutputStream(clientSocket.getOutputStream());
            is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + hostname);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + hostname);
        }
	
	// If everything has been initialized then we want to write some data
	// to the socket we have opened a connection to on the given port
	
	if (clientSocket == null || os == null || is == null) {
	    System.err.println( "Something is wrong. One variable is null." );
	    tryToConnect = true;
	}
	else
	{
		tryToConnect = false;
	}
	}

	try {
	    while ( true ) {
		System.out.print( "Enter an integer (0 to stop connection, -1 to stop server): " );
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String keyboardInput = br.readLine();
		os.writeBytes( keyboardInput + "\n" );

		int n = Integer.parseInt( keyboardInput );
		if ( n == 0 || n == -1 ) {
		    break;
		}
		
		String responseLine = is.readLine();
		System.out.println("Server returns its square as: " + responseLine);
	    }
	    
	    // clean up:
	    // close the output stream
	    // close the input stream
	    // close the socket
	    
	    os.close();
	    is.close();
	    clientSocket.close();   
	} catch (UnknownHostException e) {
	    System.err.println("Trying to connect to unknown host: " + e);
	} catch (IOException e) {
	    System.err.println("IOException:  " + e);
	}
    }           
}
