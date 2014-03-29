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
	int pieces = 5;
	int hasFile = 0;     //Equal to 1 if peer has all pieces of file

	int serverPort;

	public static void main( String[] args ) 
	{
		if (args.length >= 1) try {
			int peerIDArg = Integer.parseInt(args[0]);
			new Peer(peerIDArg).start(); // start with integer port specified in first argument
			return;
		} catch (NumberFormatException e) {
			
		}
		
		// if we got to this point a legal port was not specified
		new Peer().start();
	}

	public Peer(int peerID) {
		this.peerID = peerID;
	}
	
	public Peer() {
		this.peerID = 1001;
	}
	
	public void start() {
		//have peer's clients connect to other peers' servers
		try 
		{
			Scanner in = new Scanner(new FileReader("PeerInfo.cfg"));

			while(in.hasNext())
			{
				int inPeerID = in.nextInt();
				in.next();
				int inPort = in.nextInt();
				if(inPeerID != peerID)														//I have discovered one of my neighbors in the config file
				{
					Client newPeerClient = new Client(inPort, peerID, inPeerID, this);
					new Thread(newPeerClient).start();										

					clients.put(inPeerID, newPeerClient);
				}
				else																		//I have discovered myself in the config file
				{
					serverPort = inPort;
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
	
	//logic to handle a normal message received from another peer
	public void receiveNormalMessage(int neighborPeerID, NormalMessage m){
		if(m.getType() == 0)			//choke
		{
			//TODO: choke
		}
		else if(m.getType() == 1)		//unchoke
		{
			//TODO: unchoke
		}
		else if(m.getType() == 2)		//interested
		{
			//TODO: interested
			System.out.println("I am Peer " + peerID + " and I just received an Interested message from Peer " + neighborPeerID);

		}
		else if(m.getType() == 3)		//not interested
		{
			//TODO: not interested
			System.out.println("I am Peer " + peerID + " and I just received a Not Interested message from Peer " + neighborPeerID);
		}
		else if(m.getType() == 4)		//have
		{
			//TODO: have
		}
		else if(m.getType() == 5)		//bitfield
		{
			System.out.println("I am Peer " + peerID + " and I just received a Bitfield message from Peer " + neighborPeerID + ".  I will determine if they have any interesting pieces.");
			determineInterest(neighborPeerID, m.getPayload());
		}
		else if(m.getType() == 6)		//request
		{
			//TODO: request
		}
		else if(m.getType() == 7)		//piece
		{
			//TODO: piece
		}
		else
		{
			System.err.println("I am peer " + peerID + " and I just received a message of an invalid type!");
		}
	}
	
	public void sendBitfieldMessage(int neighborPeerID)
	{
		byte[] bitfield = generateBitfield();
		clients.get(neighborPeerID).sendMessage(new NormalMessage(bitfield.length + 1,5,bitfield));
	}	
	public byte[] generateBitfield()
	{
		byte[] bitfield = {};
		String bitfieldString = "";

		try 
		{
			for(int i = 1; i <= pieces; i++)
			{
				File f = new File("peer_" + peerID + "/output_" + i + ".dat");
				if(f.exists() && !f.isDirectory())
				{
					bitfieldString += "1";
				}
				else
				{
					bitfieldString += "0";
					
				}
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
	public void determineInterest(int neighborPeerID, byte[] neighborBitfield)	//determine interest based upon neighbor's bitfield
	{
		if(hasFile != 1)														//if peer already has all the pieces, don't even bother checking neighbor's bitfield
		{
			byte[] myBitfield = generateBitfield();
			
			String neighborBitfieldString = new String(neighborBitfield);
			String myBitfieldString = new String(myBitfield);
			
			for(int i = 0; i < neighborBitfieldString.length(); i++)
			{
				if(myBitfieldString.charAt(i) == '0')
				{
					if(neighborBitfieldString.charAt(i) == '1')
					{
						System.out.println("I am Peer " + peerID + " and I am interested in " + neighborPeerID);
						clients.get(neighborPeerID).interested();				//Neighbor peer has a piece that I don't have!
						return;
					}
				}
			}
		}

		System.out.println("I am Peer " + peerID + " and I am not interested in " + neighborPeerID);
		clients.get(neighborPeerID).notInterested();							//I already have all the pieces that this neighbor has
	}
	public void request(int neighborPeerID, byte[] pieceIndex)
	{
		clients.get(neighborPeerID).sendMessage(new NormalMessage(pieceIndex.length,6,pieceIndex));
	}

	public void sendMessage(int neighborPeerID, Message msg) 
	{

	}

	public void processMessage(Message msg)
	{

	}

}
