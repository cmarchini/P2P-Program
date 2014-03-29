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

	//configuration variables
	int numberOfPreferredNeighbors = 1;
	int unchokingInterval;
	int optimisticUnchokingInterval;
	
	int currentNumberOfPreferredNeighbors = 0;
	
	//interested
	List<Integer> interestedClients = new ArrayList<Integer>();
	List<Integer> unchokedClients = new ArrayList<Integer>();
	
	//The bitfields for all neighbor peers
	Map<Integer, byte[]> bitfields = new HashMap<Integer, byte[]>();
	
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
		this.peerID = 1003;
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

	//logic to handle a handshake message received from another peer
	public void receivedhandshake(HandshakeMessage msg)
	{
		//If Peer A sends a handshake message to me, then I will tell my client that is associated with Peer A that we received a handshake from Peer A
		clients.get(msg.getPeerID()).incrementHandshake();
	}
	
	//logic to handle a normal message received from another peer
	public void receiveNormalMessage(int neighborPeerID, NormalMessage m){
		if(m.getType() == 0)			//choke
		{
			//TODO: choke
		}
		else if(m.getType() == 1)		//received unchoke: now I can send request messages to this neighbor for each piece that I need
		{
			//TODO: unchoke
		}
		else if(m.getType() == 2)		//received interested: now I want to determine if I want to choke or unchoke that peer
		{
			System.out.println("I am Peer " + peerID + " and I just received an Interested message from Peer " + neighborPeerID);
			interestedClients.add(neighborPeerID);
			determineChoking(neighborPeerID);

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
		else if(m.getType() == 5)		//received bitfield: now I want to determine if I am interested in that peer
		{
			System.out.println("I am Peer " + peerID + " and I just received a Bitfield message from Peer " + neighborPeerID + ".  I will determine if they have any interesting pieces.");
			bitfields.put(neighborPeerID, m.getPayload());
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

	//Type 0/1: choke methods
	public void determineChoking(int neighborPeerID)
	{
		if(hasFile == 1)		//if peer has all pieces, then it chooses neighbor peers to unchoke/choke randomly
		{
			while(interestedClients.size() > 0 && unchokedClients.size() < numberOfPreferredNeighbors)
			{
				int newUnchokedClient = interestedClients.remove(0);
				unchokedClients.add(newUnchokedClient);
				System.out.println("Peer " + peerID + " is unchoking Peer " + newUnchokedClient);
				clients.get(newUnchokedClient).sendUnchoke();
			}
			
			for(int i = 0; i < interestedClients.size(); i++)
			{
				System.out.println("Peer " + peerID + " is choking Peer " + interestedClients.get(i));
				clients.get(interestedClients.get(i)).sendChoke();
			}
		}
		else
		{
			//TODO: Determine who is a preferred neighbor based upon their download speed
			System.out.println("NOT YET IMPLEMENTED: I need to determine who is a preferred neighbor based upon their download speed");
		}
	}
	//Type 2/3: interest methods
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
						clients.get(neighborPeerID).sendInterested();				//Neighbor peer has a piece that I don't have!
						return;
					}
				}
			}
		}

		System.out.println("I am Peer " + peerID + " and I am not interested in " + neighborPeerID);
		clients.get(neighborPeerID).sendNotInterested();							//I already have all the pieces that this neighbor has
	}
	//Type 5: bitfield methods
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
	//Type 6: request methods
	public void determineRequests(int neighborPeerID)
	{
		byte[] myBitfield = generateBitfield();
		byte[] neighborBitfield = bitfields.get(neighborPeerID);
		
		String neighborBitfieldString = new String(neighborBitfield);
		String myBitfieldString = new String(myBitfield);
		
		for(int i = 0; i < neighborBitfieldString.length(); i++)
		{
			if(myBitfieldString.charAt(i) == '0')
			{
				if(neighborBitfieldString.charAt(i) == '1')
				{
					System.out.println("I am Peer " + peerID + " and I am going to request piece " + i + " from " + neighborPeerID);
					clients.get(neighborPeerID).sendInterested();				//Neighbor peer has a piece that I don't have!
					return;
				}
			}
		}
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
