
import java.io.*;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.io.IOException;  
import java.util.logging.FileHandler;  
import java.util.logging.Level;  
import java.util.logging.Logger;  
import java.util.logging.SimpleFormatter; 

public class Peer {
	// assumes the current class is called logger
	private final static Logger LOGGER = Logger.getLogger("MyLog"); 
	private static FileHandler fh;
	
	Map<Integer, Client> clients = new HashMap<Integer, Client>();
	Map<Integer, Integer> piecesReceived = new HashMap<Integer, Integer>();
	int peerID;
	int pieces = 5;
	boolean hasFile;     //Equal to 1 if peer has all pieces of file
	int fileSize = 167513;
	int serverPort;
	
	byte[] myBitfield;

	//configuration variables
	int numberOfPreferredNeighbors = 1;
	int unchokingInterval = 5000;
	int optimisticUnchokingInterval = 15000;
	String fileName = "alice.dat";
	//int pieceSize = 32768;
	int pieceSize = 10000;
	
	int currentNumberOfPreferredNeighbors = 0;
	
	//interested
	List<Integer> interestedClients = new ArrayList<Integer>();
	List<Integer> unchokedClients = new ArrayList<Integer>();
	int optimisticUnchokedClient = -1;
	
	// file path
	String filePath;
	
	// number of bits in bitfield
	long numPieces;
	
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
        
        try {  
              
            // This block configure the logger with handler and formatter  
            fh = new FileHandler("MyLogFile.log");  
            LOGGER.addHandler(fh);  
            //logger.setLevel(Level.ALL);  
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);  
              
            // the following statement is used to log any messages  
            LOGGER.info("My first log");  
              
        } catch (SecurityException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
          
		
		// if we got to this point a legal port was not specified
		new Peer().start();
	}

	public Peer(int peerID) {
		this.peerID = peerID;
		
		
		
		//MyTimerTask test = new MyTimerTask(peerID + "");
		//test.run();
		
		initialize();
	}

	public Peer() {
		// HARDCODE STUFF HERE
		this.peerID = 1003;
		int hasFileInt = 0;
		
		hasFile = (hasFileInt == 1);
		/*
		new java.util.Timer().schedule( 
		        new MyTimerTask("" + peerID) 
		        , 
		        0,5000 
		);*/
		
		new java.util.Timer().schedule( 
		        new java.util.TimerTask() {
		            @Override
		            public void run() {
		            	System.out.println("This is happening every 5 seconds");
		               determineChoking();
		            }
		        },
		        0,
		        unchokingInterval
			);
			
			new java.util.Timer().schedule( 
		        new java.util.TimerTask() {
		            @Override	            
		            public void run() {
		            	System.out.println("This is happening every 15 seconds");
		               determineOptimisticUnchoking();
		            }
		        },
		        0,
		        optimisticUnchokingInterval
			);
		
		initialize();
	}
	
	// called inside every constructor
	public void initialize() {
		filePath = "peer_" + peerID + "/" + fileName;
		generateBitfield();
		createDirectory();
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
	
	//public void receivedPingMessage()
	
	//logic to handle a normal message received from another peer
	public void receiveNormalMessage(int neighborPeerID, NormalMessage m){
		if(m.getType() == 0)			//received choke:
		{
			System.out.println("I am Peer " + peerID + " and I just received an Choke message from Peer " + neighborPeerID);
		}
		else if(m.getType() == 1)		//received unchoke: now I can send request messages to this neighbor for each piece that I need
		{
			System.out.println("I am Peer " + peerID + " and I just received an Unchoke message from Peer " + neighborPeerID);
			determineRequests(neighborPeerID);
		}
		else if(m.getType() == 2)		//received interested: now I want to determine if I want to choke or unchoke that peer
		{
			System.out.println("I am Peer " + peerID + " and I just received an Interested message from Peer " + neighborPeerID);
			interestedClients.add(neighborPeerID);
			//determineChoking(neighborPeerID);

		}
		else if(m.getType() == 3)		//not interested
		{
			//TODO: not interested
			System.out.println("I am Peer " + peerID + " and I just received a Not Interested message from Peer " + neighborPeerID);
			if(interestedClients.size() > 0 && interestedClients.get(neighborPeerID) != null)
			{
				interestedClients.remove(neighborPeerID);
			}
		}
		else if(m.getType() == 4)		//have
		{
			System.out.println("I am Peer " + peerID + " and I just received a Have message from Peer " + neighborPeerID + ".  I will mark my bitfield with the piece they now have.");
			
			//retrieve the bitfield to manipulate
			byte[] bitField = bitfields.get(neighborPeerID);
			
			//get integer of bit to set to true
			byte[] bytes = m.getPayload();
			System.out.println(m.getPayload());
			int bitToSet = java.nio.ByteBuffer.wrap(bytes).getInt();
			
			System.out.println("The bitfield contained: " + bitField.toString());
			System.out.println("I need to set the bit number: " + bytes.toString());
			System.out.println("Which is the same as int: " + bitToSet);
			
			//get the index for the byte to shift and the bit for the index of the bit
			int byteIndex = bitToSet / 8;
			int bitIndex = (bitToSet % 8);
			
			System.out.println("The byte I'm going to set from the array is " + byteIndex);
			
			byte b = bitField[byteIndex];
			
			System.out.println("The byte contains: " + b);
			
			//set the bit in the byte
			b = (byte) (b | (byte) (0b1000000 >> bitIndex));
			
			bitField[byteIndex] = b;
			
			System.out.println("The byte is now: " + bitField[byteIndex]);
			
		}
		else if(m.getType() == 5)		//received bitfield: now I want to determine if I am interested in that peer
		{
			System.out.println("I am Peer " + peerID + " and I just received a Bitfield message from Peer " + neighborPeerID + ".  I will determine if they have any interesting pieces.");

			byte[] bitfield = m.getPayload();
			System.out.println("For example, the first byte is " + bitfield[0] + " and the last byte is " + bitfield[bitfield.length-1]);

			// just in case - because this is assumed later and errors might happen otherwise
			if (bitfield.length != myBitfield.length) {
				System.out.println("Payload size was " + bitfield.length + " but bitfield length is supposed to be " + myBitfield.length);
				return;
			}
			
			bitfields.put(neighborPeerID, bitfield);
			determineInterest(neighborPeerID, bitfield);
		}
		else if(m.getType() == 6)		//received request: now I will send the requested piece
		{
			//String s = new String(m.getPayload());
			//int index = Integer.parseInt(s);
			byte[] bytes = m.getPayload();
			int index = java.nio.ByteBuffer.wrap(bytes).getInt();
			
			System.out.println("I am Peer " + peerID + " and I just received a Request message with index " + index + " from Peer " + neighborPeerID);
			clients.get(neighborPeerID).sendPiece(filePath, index);
		}
		else if(m.getType() == 7)		//received piece: now I will add this piece to my directory. I will also send out a have message to let other peers know I know have this piece
		{
			ReceivingPieceMessage rpm = ((ReceivingPieceMessage)m);
			try {
				rpm.writeToFile(filePath);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println("Oh wow, look at this piece index I got from Peer " + neighborPeerID + ": " + rpm.getPieceIndex());
			
			// update the bitfields
			updateMyBitfield(rpm.getPieceIndex(), true);
			determineRequests(neighborPeerID);
			
			if(piecesReceived.get(neighborPeerID) == null)
			{
				piecesReceived.put(neighborPeerID, 1);
			}
			else
			{
				int received = piecesReceived.get(neighborPeerID);
				piecesReceived.put(neighborPeerID, ++received);
			}
			
			// send have message
			for(int nPeerID : clients.keySet())
			{
				System.out.println(clients.get(nPeerID).getNeighborPeerID());
				//System.out.println(clients.get(i).getNeighborPeerID() + " <----neighborpeerid  bitfield-----------> " + bitfields.get(clients.get(i).getNeighborPeerID()));
				
				if( bitfields.get(clients.get(nPeerID).getNeighborPeerID()) != null)
				{
						determineInterest(clients.get(nPeerID).getNeighborPeerID(), bitfields.get(clients.get(nPeerID).getNeighborPeerID()));
						clients.get(neighborPeerID).sendHave(rpm.getPieceIndex());
				}
			}
			
			// Is this what is supposed to happen next?
			// 			determineRequests(neighborPeerID);
			
			//redetermine interest in all peers
			//for(int i = 0; i < clients.size(); i++)
			//{
			//	System.out.println(clients.get(i).getNeighborPeerID() + " <----neighborpeerid  bitfield-----------> " + bitfields.get(clients.get(i).getNeighborPeerID()));
			//	determineInterest(clients.get(i).getNeighborPeerID(), bitfields.get(clients.get(i).getNeighborPeerID()));
			//}
			
		}
		else
		{
			System.err.println("I am peer " + peerID + " and I just received a message of an invalid type!");
		}
	}
	
	

	//Type 0/1: choke methods
	public void determineChoking()
	{
		//empty unchokedClients list by moving to interestedClients
		interestedClients.addAll(unchokedClients);
		unchokedClients.clear();
		
		if(hasFile || piecesReceived.size() == 0)		//if peer has all pieces, then it chooses neighbor peers to unchoke/choke randomly
		{
			while(interestedClients.size() > 0 && unchokedClients.size() <= numberOfPreferredNeighbors)
			{
				Random rand = new Random();
				int randPeer = rand.nextInt(interestedClients.size());
				int newUnchokedClient = interestedClients.remove(randPeer);
				unchokedClients.add(newUnchokedClient);
				System.out.println("Peer " + peerID + " is unchoking Peer " + newUnchokedClient);
				clients.get(newUnchokedClient).sendUnchoke();
			}
		}
		else  //does not have complete file
		{
			System.out.println("NOT YET IMPLEMENTED: I need to determine who is a preferred neighbor based upon their download speed");
			
			while(interestedClients.size() > 0 && unchokedClients.size() <= numberOfPreferredNeighbors)
			{
				//get the largest value in piecesReceived HashMap
				int bestPeer = 0;
				int bestValue = -1;
				
				for (Map.Entry<Integer, Integer> entry : piecesReceived.entrySet()) {
				    int thisPeerId = entry.getKey();
				    int value = entry.getValue();
				    
				    if(value > bestValue) {
				    	bestPeer = thisPeerId;
				    }
				}
				
				//add the peerID of that to the unchokedClients list
				unchokedClients.add(bestPeer);
				
				//unchoke the client
				System.out.println("Peer " + peerID + " is unchoking Peer " + bestPeer);
				clients.get(bestPeer).sendUnchoke();
				
				//remove that peer ID from the piecesReceived HashMap and the interested clients
				interestedClients.remove(bestPeer);
				piecesReceived.remove(bestPeer);
			}
		}
		
		// choke all of the remaining interested clients
		for(int i = 0; i < interestedClients.size(); i++)
		{
			System.out.println("Peer " + peerID + " is choking Peer " + interestedClients.get(i));
			clients.get(interestedClients.get(i)).sendChoke();
		}
		
		piecesReceived.clear();
	}
	
	public void determineOptimisticUnchoking()
	{
		if(optimisticUnchokedClient != -1)
		{
			interestedClients.add(optimisticUnchokedClient);
		}
		
		if(interestedClients.size() > 0)
		{
		
			Random rand = new Random();
			int randPeer = rand.nextInt(interestedClients.size());
			int newOptimisitcUnchokedClient = interestedClients.remove(randPeer);
			
			optimisticUnchokedClient = newOptimisitcUnchokedClient;
			System.out.println("Peer " + peerID + " is unchoking Peer " + optimisticUnchokedClient);
			clients.get(optimisticUnchokedClient).sendUnchoke();
			
			// choke all of the remaining interested clients
			for(int i = 0; i < interestedClients.size(); i++)
			{
				System.out.println("Peer " + peerID + " is choking Peer " + interestedClients.get(i));
				clients.get(interestedClients.get(i)).sendChoke();
			}
		}
	}
	
	
	
	
	//Type 2/3: interest methods
	public void determineInterest(int neighborPeerID, byte[] neighborBitfield)	//determine interest based upon neighbor's bitfield
	{
		if(!hasFile)														//if peer already has all the pieces, don't even bother checking neighbor's bitfield
		{
			//byte[] myBitfield = generateBitfield();
			
			for(int i = 0; i < numPieces; i++)
			{
				System.out.println("I have the piece: " + hasPiece(myBitfield, i) + ". My neighbor has the piece : " + hasPiece(neighborBitfield, i));
				if(hasPiece(neighborBitfield, i))
				{
					if(!hasPiece(myBitfield, i))
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
	public void generateBitfield()
	{
		if (hasFile) System.out.println("I have the file");
		else System.out.println("I don't have the file");
		System.out.println("The file size is " + fileSize);
		
		long size;
		File file = new File(filePath);
		size = fileSize / pieceSize;
		if(fileSize % pieceSize > 0)		//if you have 5 full pieces, and then a partial piece, that means you got 6 pieces.
		{
			size += 1;
		}

		System.out.println("The number of pieces (bits in the bitfield) is " + size);

		byte[] bitfield = new byte[(int)((size-1)/8+1)]; // size/8, rounded up
		System.out.println("The bitfield size is " + bitfield.length);
		
		int extra = (int)(7-((size-1)%8)); // extra bits being stored not corresponding to file
		byte repeatedByte = hasFile ? (byte)0xFF : (byte)0x00;
		byte extraByte = (byte)((hasFile ? 0xFF : 0x00) << extra);
		
		for (int i=0; i<bitfield.length-1; i++) {
			bitfield[i] = repeatedByte;
		}
		
		System.out.println("The bitfield is filled with bytes " + repeatedByte);

		bitfield[bitfield.length-1] = extraByte;
		
		System.out.println("The bitfield has the last byte " + bitfield[bitfield.length - 1]);
			
		numPieces = size;
		myBitfield = bitfield;
	}
	
	public void updateMyBitfield(int pieceIndex, boolean bit) {
		System.out.println("Updating my own bitfield");
		System.out.println("I need to set the bit number: " + pieceIndex);
		
		int byteIndex = pieceIndex / 8;
		int bitIndex = (pieceIndex % 8); //subtracting from seven because high bits are low bits in the bitfield
		
		System.out.println("The byte I'm going to set from the array is " + byteIndex);
		
		byte b = myBitfield[byteIndex];
		
		System.out.println("The byte contains: " + b);
		
		//set the bit in the byte
		//byte bitPosition = (byte)(0b1000_0000 >>> bitIndex); // 10000000 for first bit in segment, 00000001 for last
		//b = (byte)((b & ~bitPosition) | (bit ? 0xFF : 0x00 & bitPosition));
		//myBitfield[byteIndex] = b;
		
		myBitfield[byteIndex] |= (1 << (7-bitIndex));
		
		System.out.println("The byte is now: " + myBitfield[byteIndex]);
		
		
		for(int i =0; i < 8; i++)
		{
			System.out.println("Byte " + byteIndex + ": bit " + i + " is set: " + ((myBitfield[byteIndex] >> (7-i) & 1) == 1));
		}
		
		
		System.out.println("Let's see if I have all of the pieces yet");
		for(int i = 0; i < numPieces; i++)
		{
			if(!hasPiece(myBitfield, i))
			{
				System.out.println("I still need more pieces");
				return;
			}
		}
		System.out.println("I have all the pieces!");
		hasFile = true;
	}
	
	public boolean hasPiece(byte[] bitfield, int pieceIndex) {
		byte bitPosition = (byte)(0b1000_0000 >>> pieceIndex % 8); // 10000000 for first bit in segment, 00000001 for last
		
		return (bitfield[pieceIndex/8] & bitPosition) != 0;
	}
	
	//Type 6: request methods
	public void determineRequests(int neighborPeerID)
	{
		byte[] neighborBitfield = bitfields.get(neighborPeerID);
		
		byte[]pieceIndex = new byte[4];
		
		for(int i = 0; i < numPieces; i++)
		{
			System.out.println("I have the piece: " + hasPiece(myBitfield, i) + ". My neighbor has the piece : " + hasPiece(neighborBitfield, i));
			if(hasPiece(neighborBitfield, i))
			{
				if(!hasPiece(myBitfield, i))
				{
					System.out.println("I am Peer " + peerID + " and I am going to request piece " + pieceIndex + " from " + neighborPeerID);
					
					pieceIndex = ByteBuffer.allocate(4).putInt(i).array();
					
					clients.get(neighborPeerID).sendRequest(pieceIndex);				//Neighbor peer has a piece that I don't have!
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
	
	public void createDirectory() {
		File folder = new File("peer_" + peerID);
		if (!folder.exists()) {
			boolean created = folder.mkdir();
			if (created) System.out.println("I am Peer " + peerID + "; Folder created for this peer: " + "peer_" + peerID);
		}
	}
}