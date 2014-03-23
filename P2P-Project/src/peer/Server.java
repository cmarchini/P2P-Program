package peer;
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
				ServerConnection oneconnection = new ServerConnection(clientSocket, numConnections, this, peerID, peer);
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
	int peerID;
	Peer peer;
	
  DataInputStream dis;

	public ServerConnection(Socket clientSocket, int id, Server server, int peerID, Peer peer) {
		this.clientSocket = clientSocket;
		this.id = id;
		this.server = server;
		this.peerID = peerID;
		this.peer = peer;
		System.out.println( "Connection " + id + " established with: " + clientSocket );
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

			while (true) {
				Message msg = parseInput();
				peer.processMessage(msg); // TODO
//				if ( line.equalsIgnoreCase("q") ) {
//					serverStop = true;
//					break;
//				}
//				if ( line.equalsIgnoreCase("u") ) break;


				//os.println("" + line.toUpperCase()); 
				peer.sendMessage(peerID, new HandshakeMessage(peerID));

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
	
	private Message parseInput() throws IOException {
		Message m = null;
		
    int len = dis.readInt();
    byte type = dis.readByte();
    
    if (type == 'O') {
      for (int i=0; i<23; i++) {
      	dis.readByte();
      }
      
      int peerID = dis.readInt();
      
      m = new HandshakeMessage(peerID);
    } else {
      byte[] data = new byte[len];
      if (len > 0) {
          dis.readFully(data);
      }
      
      m = new NormalMessage(len, type, data);
    }
    
    System.out.println( "Received message of length " + len + " and type " + type + " from Connection " + id + "." );
    
    return m;
		
		//line = is.readLine();
	}
}

