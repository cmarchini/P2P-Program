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
		this.serverPort = 6010;
	}
	
	public void start() {

		peerID = 1003;
		
		//have peer's clients connect to other peers' servers
		try 
		{
				Scanner in = new Scanner(new FileReader("PeerInfo.cfg"));

			while(in.hasNext())
			{
				int inPeerID = in.nextInt();
				in.next();
				int clientPort = in.nextInt();
				if(serverPort != clientPort)
				{
					Client newPeerClient = new Client(clientPort, peerID, inPeerID, this);
					new Thread(newPeerClient).start();										

					clients.put(inPeerID, newPeerClient);
				}
				in.next();
			}

		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//turn on peer's server
		Server newPeerServer = new Server(serverPort, peerID, this);
		new Thread(newPeerServer).start();
	}

	public void receivedhandshake(HandshakeMessage msg)
	{
		//If Peer A sends a handshake message to me, then I will tell my client that is associated with Peer A that we received a handshake from Peer A
		clients.get(msg.getPeerID()).handshake();
	}
	
	public void sendBitfieldMessage(int neighborPeerID)
	{
		byte[] bitfield = generateBitfield();
		clients.get(neighborPeerID).sendMessage(new NormalMessage(bitfield.length,5,bitfield));
	}	
	public byte[] generateBitfield()
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

	public void sendMessage(int neighborPeerID, Message msg) 
	{

	}

	public void processMessage(Message msg)
	{

	}

}
