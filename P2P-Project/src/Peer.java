
public class Peer {
	static int ports[] = { 6788, 6789 };
	static int peerID = 1001;
	
	Client client;
	
    public static void main( String[] args ) 
    {
    	new Peer().start();
    }
    
    public void start() {

    	int serverPort = 6788;
    	
    	Server newPeerServer = new Server(serverPort, peerID, this);
    	new Thread(newPeerServer).start();
    	
    	
    	for(int i = 0; i < ports.length; i++)
    	{
    		if(serverPort != ports[i])
    		{
	    		Client newPeerClient = new Client(ports[i], peerID);
	    		new Thread(newPeerClient).start();
    		}
    	}
    	//Client newPeerClient = new Client(6788);
    	
    	
    	//new Thread(newPeerClient).start();
    }
    
    /*public void handshake(int port)
    {
    	Client newPeerClient = new Client(port);
    	
    	new Thread(newPeerClient).start();
    }*/

    public void sendMessage() {
    	client.sendMessage();
    }
    
    public void parseMessage() {
    	// call different methods inside Peer depending on message
    }
    
}
