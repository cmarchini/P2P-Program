package peer;

public class HandshakeMessage implements Message {

	int peerID;
	
	/**
	 * 
	 */
	public HandshakeMessage(int peerID) {
		this.setPeerID(peerID);
	}
	
	public String getMessageString()
	{
		// This function returns 
		// the byte string representing
		// the message to be passed into
		// the TCP connection.  
		
		// TODO: convert to actual bytes
		// instead of using a string
		
		return"HELLO" + "00000000000000000000000" + peerID;

	}
	
	/**
	 * @return peerID
	 */
	public int getPeerID() {
		return peerID;
	}
	/**
	 * @param peerID
	 */
	public void setPeerID(int peerID) {
		this.peerID = peerID;
	}
	
}
