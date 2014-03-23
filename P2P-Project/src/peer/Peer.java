package peer;
import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Peer {
	Map<Integer, Client> clients = new HashMap<Integer, Client>();
	int peerID;

	int serverPort;

	public static void main( String[] args ) 
	{
		if (args.length >= 1) try {
			int serverPortArg = Integer.parseInt(args[0]);
			new Peer(serverPortArg).start(); // start with integer port specified in first argument
			return;
		} catch (NumberFormatException e) {
			
		}
		
		// if we got to this point a legal port was not specified
		new Peer().start();
	}

	public Peer(int serverPort) {
		this.serverPort = serverPort;
	}
	
	public Peer() {
		this.serverPort = 6008;
	}
	
	public void start() {

		peerID = 1003;

		Server newPeerServer = new Server(serverPort, peerID, this);
		new Thread(newPeerServer).start();

		System.out.println(generateBitField());
		byte[] payload = generateBitField();
		NormalMessage bitfieldMsg = new NormalMessage(10, 5, payload);

		System.out.println("Payload: " + bitfieldMsg.getPayload());
		System.out.println("Length: " + bitfieldMsg.getLength());
		System.out.println("Type: " + bitfieldMsg.getType());
		System.out.println("MessageString: " + bitfieldMsg.getMessageString());

		//have peer's clients connect to other peers' servers
		try 
		{
			Mailbox mail = new Mailbox();
			
			Scanner in = new Scanner(new FileReader("PeerInfo.cfg"));

			while(in.hasNext())
			{
				int inPeerID = in.nextInt();
				in.next();
				int clientPort = in.nextInt();
				if(serverPort != clientPort)
				{
					mail.addMailbox(inPeerID);
					mail.placeMessage(inPeerID, new NormalMessage(60, 1, "This is a test".getBytes()));
					
					
					Client newPeerClient = new Client(clientPort, inPeerID, mail);
					clients.put(inPeerID, newPeerClient);
					new Thread(newPeerClient).start();
					//sendMessage(inPeerID, new HandshakeMessage(peerID));
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

	public byte[] generateBitField()
	{
		byte[] bitfield = {};
		String bitfieldString = "";

		try 
		{
			Scanner in = new Scanner(new FileReader("peer_" + peerID + "/Alphabet.txt"));
			int i=0;

			while(in.hasNext())
			{
				if(!in.next().equals("0"))
				{
					bitfieldString += "1";
				}
				else
				{
					bitfieldString += "0";
				}
				i++;

			}

			bitfield = bitfieldString.getBytes("US-ASCII");
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bitfield;
	}

	public void sendMessage(int peerID, Message msg) 
	{
		System.out.println("Sending to " + peerID + ":" + clients.get(peerID));
		clients.get(peerID).sendMessage(peerID, msg);
	}

	public void processMessage(Message msg)
	{
    System.out.println( "Received message!" );
		// call different methods inside Peer depending on message
	}

}
