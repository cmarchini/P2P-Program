
public class Peer {
	static int ports[] = { 6788, 6789 };
	static int peerID = 1001;
	
    public static void main( String[] args ) 
    {
    	int serverPort = 6789;
    	
    	Server newPeerServer = new Server(serverPort, peerID);
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

}
