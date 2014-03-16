
public class Peer {
    public static void main( String[] args ) 
    {
    	Server2 newPeerServer = new Server2(6788);
    	Client newPeerClient = new Client(6789);
    	
    	new Thread(newPeerServer).start();
    	new Thread(newPeerClient).start();
    }
    
    /*public void handshake(int port)
    {
    	Client newPeerClient = new Client(port);
    	
    	new Thread(newPeerClient).start();
    }*/

}
