package peer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class HandshakeMessage implements Message {

	int myPeerID;
	
	/**
	 * 
	 */
	public HandshakeMessage(int peerID) {
		this.setPeerID(peerID);
	}
	
	public String getMessageString()
	{
		return "HELLO\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0" + myPeerID;
	}
	
	public void writeTo(DataOutputStream os) throws IOException {
		try {
			os.write("HELLO\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0".getBytes("US-ASCII"));
			os.writeInt(myPeerID);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return peerID
	 */
	public int getPeerID() {
		return myPeerID;
	}
	/**
	 * @param peerID
	 */
	public void setPeerID(int peerID) {
		this.myPeerID = peerID;
	}
	
}
