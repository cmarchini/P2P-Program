package peer;
import java.io.*;
import java.net.*;

public class Client implements Runnable
{

	private int port;
	private int peerID; 
	private Mailbox mail;


	Socket clientSocket = null;  
	DataOutputStream outputStream = null;
	BufferedReader inputStream = null;
	
	public Client(int port, int peerID, Mailbox mail)
	{
		this.port = port;
		this.peerID = peerID;
		this.mail = mail;
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

		try {
			sendMessage(peerID, new HandshakeMessage(peerID)); // sending to self: this line no longer necessary
						
			while ( true ) 
			{
				Message m = mail.getNextMessage(peerID);
				if(m != null){
					sendMessage(peerID, m);
				}
				
				String modifiedSentence = inputStream.readLine();
				System.out.println("FROM SERVER: " + modifiedSentence);
			}


		} catch (UnknownHostException e) {
			System.err.println("Trying to connect to unknown host: " + e);
		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		}
	}           

	/**TODO:
	 * 
	 * This message function
	 * takes a peerID and a
	 * Message and sends the
	 * message to the given
	 * peer.  It must parse 
	 * the message first.
	 */
	public void sendMessage(int peerID, Message msg) {
		//String sentence = msg.getMessageString();
		
		try {
			//outputStream.writeBytes(sentence + "\n");
			msg.writeTo(outputStream);
		} catch (IOException e) {
			System.out.println("IOEXCEPTION!!!!!!!!!!!!!!!!!!!!!!!");
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
