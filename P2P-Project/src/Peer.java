import java.io.*;
import java.net.ServerSocket;
import java.util.Scanner;

public class Peer {
	Client client;
	int peerID;

	public static void main( String[] args ) 
	{
		new Peer().start();

	}

	public void start() {

		int serverPort = 6009;
		peerID = 1001;

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

}
