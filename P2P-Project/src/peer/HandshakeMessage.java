package peer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

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
	
	public void writeTo(DataOutputStream os) throws IOException {
		try {
			os.write("HELLO\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0".getBytes("US-ASCII"));
			os.writeInt(peerID);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
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
