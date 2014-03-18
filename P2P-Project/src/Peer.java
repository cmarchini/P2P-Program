import java.io.*;
import java.net.ServerSocket;
import java.util.Scanner;

public class Peer {
	Client client;
	int peerID;
	//peer 1
	String [] alphabet = {"A"," ","C"," ","E"," ","G","H"};
	
	//peer 2
	//String [] alphabet = {" "," "," "," "," "," "," "," "};
	//String [] bitfield = {};
	
    public static void main( String[] args ) 
    {
    	new Peer().start();

    }
    
    public void start() {

    	int serverPort = 6009;
    	peerID = 1002;
    	
    	Server newPeerServer = new Server(serverPort, peerID, this);
    	new Thread(newPeerServer).start();
    	
    	System.out.println(generateBitField());
    	String payload = generateBitField();
    	Message bitfieldMsg = new Message(10, 5, payload);
    	
    	System.out.println("Payload: " + bitfieldMsg.getPayload());
    	System.out.println("Length: " + bitfieldMsg.getLength());
    	System.out.println("Type: " + bitfieldMsg.getType());
    	System.out.println("MessageString: " + bitfieldMsg.getMessageString());
    	
    	//have peer's clients connect to other peers' servers
    	try 
    	{
			Scanner in = new Scanner(new FileReader("PeerInfo.cfg"));
			
	    	while(in.hasNext())
	    	{
	    		in.next();
	    		in.next();
	    		int clientPort = in.nextInt();
	    		if(serverPort != clientPort)
	    		{
	    			Client newPeerClient = new Client(clientPort, peerID);
		    		new Thread(newPeerClient).start();
	    		}
	    		in.next();
	    	}
	
		} 
    	catch (FileNotFoundException e) 
    	{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    public String generateBitField()
    {
    	String bitfield = "";
    	
    	try 
    	{
			Scanner in = new Scanner(new FileReader("peer_" + peerID + "/Alphabet.txt"));
			int i=0;
			
	    	while(in.hasNext())
	    	{
	    		if(!in.next().equals("0"))
	    		{
	    			bitfield += "1";
	    		}
	    		else
	    		{
	    			bitfield += "0";
	    		}
	    		i++;

	    	}
	
		} 
    	catch (FileNotFoundException e) 
    	{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return bitfield;
    }
    
    /*public void handshake(int port)
    {
    	Client newPeerClient = new Client(port);
    	
    	new Thread(newPeerClient).start();
    }*/

    public void sendMessage(int peerID, Message msg) 
    {
    	client.sendMessage(peerID, msg);
    }
    
    public void parseMessage() 
    {
    	// call different methods inside Peer depending on message
    }
    
    /* public void stopServer() {
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
        } */
    
}

/**
 * @author Chris
 * "
 * Quantity 
 * does 
 * not 
 * equal 
 * quality.
 * "
 * 
 * So
 * Said
 * Abraham 
 * Lincoln
 * 1863
 * Gettysburg
 * PA
 */
