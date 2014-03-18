import java.io.*;
import java.net.*;



public class Server implements Runnable
{
    ServerSocket echoServer = null;
    Socket clientSocket = null;
    int numConnections = 0;
    int port;
    int peerID;
    Peer peer;
	   
	public Server(int port, int peerID, Peer peer) {
	this.port = port;
	this.peerID = peerID;
	this.peer = peer;
	}
	
	public void run()
	{
		startServer();
	}
	
	/*public void handshake(int port)
    {
    	Client newPeerClient = new Client(port);
    	
    	new Thread(newPeerClient).start();
    }*/

    public void stopServer() {
	System.out.println( "Server cleaning up." );
	System.exit(0);
    }

    public void startServer() {
	// Try to open a server socket on the given port
	// Note that we can't choose a port less than 1024 if we are not
	// privileged users (root)
	
        try {
	    echoServer = new ServerSocket(port);
        }
        catch (IOException e) {
	    System.out.println(e);
        }   
	
	System.out.println( "Server is started and is waiting for connections." );
	System.out.println( "With multi-threading, multiple connections are allowed." );
	System.out.println( "Any client can send -1 to stop the server." );

	// Whenever a connection is received, start a new thread to process the connection
	// and wait for the next connection.
	
	while ( true ) {
	    try {
		clientSocket = echoServer.accept();
		numConnections ++;
		ServerConnection oneconnection = new ServerConnection(clientSocket, numConnections, this, peerID);
		new Thread(oneconnection).start();
	    }   
	    catch (IOException e) {
		System.out.println(e);
	    }
	}
    }
}

class ServerConnection implements Runnable {
    BufferedReader is;
    PrintStream os;
    Socket clientSocket;
    int id;
    Server server;
    int peerID;

    public ServerConnection(Socket clientSocket, int id, Server server, int peerID) {
	this.clientSocket = clientSocket;
	this.id = id;
	this.server = server;
	this.peerID = peerID;
	System.out.println( "Connection " + id + " established with: " + clientSocket );
	try {
	    is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	    os = new PrintStream(clientSocket.getOutputStream());
	} catch (IOException e) {
	    System.out.println(e);
	}
    }

    public void run() {
        String line;
	try {
	    boolean serverStop = false;

            while (true) {
                line = is.readLine();
		System.out.println( "Received " + line + " from Connection " + id + "." );
		if ( line.equalsIgnoreCase("q") ) {
		    serverStop = true;
		    break;
		}
		if ( line.equalsIgnoreCase("u") ) break;
		
		
                //os.println("" + line.toUpperCase()); 
				os.println("HELLO" + "00000000000000000000000" + peerID);
		
            }

	    System.out.println( "Connection " + id + " closed." );
            is.close();
            os.close();
            clientSocket.close();

	    if ( serverStop ) server.stopServer();
	} catch (IOException e) {
	    System.out.println(e);
	}
    }
}

