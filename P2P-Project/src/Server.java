
import java.io.*;
import java.net.*;



public class Server implements Runnable
{
	ServerSocket echoServer = null;
	Socket clientSocket = null;
	int numConnections = 0;
	int port;
	int myPeerID;
	Peer peer;

	public Server(int port, int peerID, Peer peer) {
		this.port = port;
		this.myPeerID = peerID;
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

		System.out.println( "The Server of peer " + myPeerID + " has started and is waiting for connections." );

		// Whenever a connection is received, start a new thread to process the connection
		// and wait for the next connection.

		while ( true ) {
			try {
				clientSocket = echoServer.accept();
				numConnections ++;
				ServerConnection oneconnection = new ServerConnection(clientSocket, numConnections, this, myPeerID, peer);
				new Thread(oneconnection).start();
			}   
			catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}

class ServerConnection implements Runnable {
	//BufferedReader is; // don't need
	PrintStream os;
	Socket clientSocket;
	int id;
	Server server;
	int myPeerID;
	int neighborPeerID;
	Peer peer;
	
  DataInputStream dis;

	public ServerConnection(Socket clientSocket, int id, Server server, int peerID, Peer peer) {
		this.clientSocket = clientSocket;
		this.id = id;
		this.server = server;
		this.myPeerID = peerID;
		this.peer = peer;
		System.out.println( "I am Peer " + myPeerID + " and a new client has just connected to my server.  Connection " + id + " established with: " + clientSocket );
		try {
			// is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			dis = new DataInputStream(clientSocket.getInputStream());
			os = new PrintStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void run() {
		String line;
		try {
			boolean serverStop = false;

			while (true) 
			{
				parseInput();
			}

			// TODO figure out later
			/*
			System.out.println( "Connection " + id + " closed." );
			dis.close();
			os.close();
			clientSocket.close();

			if ( serverStop ) server.stopServer();
			*/
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	private void parseInput() throws IOException {
	    int len = dis.readInt();				//len is the length of the payload AND the one byte for the type
	    byte type = dis.readByte();
	    
	    if (type == 'O') 
	    {
	      for (int i=0; i<23; i++) {
	      	dis.readByte();
	      }
	      
	      neighborPeerID = dis.readInt();
	
	      HandshakeMessage handshake = new HandshakeMessage(neighborPeerID);
	      
	      System.out.println( "I am the server of Peer " + myPeerID + " and I just received the following message: " + handshake.getMessageString());
	      
	      peer.receivedhandshake(handshake);
	    } 
	    else 
	    {
	    	if(type == 7) // PieceMessage
	    	{
	    		int pieceIndex = dis.readInt();
	    		
	      	ReceivingPieceMessage m = new ReceivingPieceMessage(len, peer.pieceSize, pieceIndex, dis);
	      	System.out.println( "I am the server of Peer " + myPeerID + " and I just received the following piece message from Peer " + neighborPeerID + ": " + m.getMessageString() + " (length " + len + " and piece index: " + pieceIndex + ")");
      
	      	peer.receiveNormalMessage(neighborPeerID, m);
	    	}
	    	else if(len - 1 > 0)
	    	{
	  	      	byte[] data = new byte[len - 1];		//array of length len-1 since we exclude the byte for type
	  	      	dis.read(data);
		      
	  	      	NormalMessage m = new NormalMessage(len, type, data);
	  	      	System.out.println( "I am the server of Peer " + myPeerID + " and I just received the following message from Peer " + neighborPeerID + ": " + m.getMessageString() + " (length " + len + " and type " + type + " and data: " + new String(data) + ")");
		      
	  	      	peer.receiveNormalMessage(neighborPeerID, m);
	    	}
	    	else 
	    	{
	    		NormalMessage m = new NormalMessage(len, type);
			    System.out.println( "I am the server of Peer " + myPeerID + " and I just received the following message from Peer " + neighborPeerID + ": " + m.getMessageString() + " (length " + len + " and type " + type + ")");
			      
			    peer.receiveNormalMessage(neighborPeerID, m);
	    	}

	  

	    }
    
	}
	

}

