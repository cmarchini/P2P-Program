package peer;
import java.io.*;
import java.net.*;

public class Client implements Runnable
{

	private int port;
	private int myPeerID;
	private int neighborPeerID; 
	private Mailbox mail;
	Peer peer;
	
	private int handshake = 0; 			//When this variable equals 2, the handshake is complete


	Socket clientSocket = null;  
	DataOutputStream outputStream = null;
	BufferedReader inputStream = null;
	
	public Client(int port, int myPeerID, int neighborPeerID, Peer peer)
	{
		this.port = port;
		this.myPeerID = myPeerID;
		this.neighborPeerID = neighborPeerID;
		this.peer = peer;
		//this.mail = mail;
	}

	public void run ()
	{

		String hostname = "localhost";
		boolean tryToConnect = true;

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

		//try {
			handshake(); // If I got to this point, then I have connected to the neighbor peer.  I will send a handshake to that peer.  Attach my peerID so the peer knows who I am.
			//closeConnection();
						
			/*while ( true ) 
			{
				Message m = mail.getNextMessage(peerID);
				if(m != null){
					sendMessage(peerID, m);
				}
				
				String modifiedSentence = inputStream.readLine();
				System.out.println("FROM SERVER: " + modifiedSentence);
			}*/


			/*} catch (UnknownHostException e) {
			System.err.println("Trying to connect to unknown host: " + e);
		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		}*/
	}           

	//Keep track of the number of handshakes
	//TODO: make sure handshake gets complete eventually if first message was not received
	//			can implement with while loop
	public boolean handshakeComplete()
	{
		if(handshake < 2)
		{
			return false;
		}
		else if(handshake == 2)
		{
			return true;
		}
		else
		{
			System.err.println("I am a client of Peer " + myPeerID + " and it seems that more than 2 handshakes have been exchanged");
			return true;
		}
	}
	//Increment handshakes.  If the hanshake is complete, tell peer that I am ready to send a bitfield message
	public void incrementHandshake()
	{
		handshake++;
		
		if(handshakeComplete())		//when handshake is complete, send bitfield
		{
			System.out.println("I am a client of Peer " + myPeerID + ".  The handshake with Peer " + neighborPeerID + " has been completed");
			sendBitfield();
		}
	}

	//Generates Handshake Message
	public void handshake()
	{
		sendMessage(new HandshakeMessage(myPeerID));
		incrementHandshake();
	}
	
	//Generates Normal Messages
	public void sendChoke()
	{
		sendMessage(new NormalMessage(1,0));
	}
	public void sendUnchoke()
	{
		sendMessage(new NormalMessage(1,1));
	}
	public void sendInterested()
	{
		sendMessage(new NormalMessage(1,2));
	}
	public void sendNotInterested()
	{
		sendMessage(new NormalMessage(1,3));
	}
	public void sendHave()
	{
		sendMessage(new NormalMessage(1,4));
	}
	public void sendBitfield()
	{
		byte[] bitfield = peer.generateBitfield();
		sendMessage(new NormalMessage(bitfield.length + 1,5,bitfield));
	}
	public void sendRequest(byte[] pieceIndex)
	{
		sendMessage(new NormalMessage(pieceIndex.length + 1,6,pieceIndex));
	}
	public void sendPiece(String filename, int index)
	{
		int len = 4; // TODO PieceSize
		int off = len*index;
		
		byte[] piece = new byte[len];
		FileInputStream file;
		try {
			file = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		try {
			file.read(piece, off, len);
			file.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		sendMessage(new NormalMessage(piece.length + 1,7,piece));
		
		// TODO - might need to make more memory-efficient by not creating the huge piece array
		// either send in blocks (ideal?) or byte-by-byte
		// see http://stackoverflow.com/questions/7616776/sending-a-file-using-dataoutputstream-in-java
	}
	
	//Sends any type of message to a peer
	public void sendMessage(Message msg) {	
		try 
		{
			System.out.println("I am a client of Peer " + myPeerID + " and I am sending the following message to the server of Peer " + neighborPeerID + ": " + msg.getMessageString());
			msg.writeTo(outputStream);
		} 
		catch (IOException e) 
		{
			System.out.println("IOEXCEPTION!  There might be a problem with the client sending a message to the server ");
			e.printStackTrace();
		}
	}
	
	public void closeConnection() {
		// close the output stream
		// close the input stream
		// close the socket

		try {
			outputStream.close();
			inputStream.close();
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}   
	}
	
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
