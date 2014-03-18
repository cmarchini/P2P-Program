/**
 * 
 */

/**
 * @author Chris
 * "
 * Quantity 
 * does 
 * not 
 * equal 
 * quality.
 * "
 * 
 * So
 * Said
 * Abraham 
 * Lincoln
 * 1863
 * Gettysburg
 * PA
 */
public class HelloMessage {
	
	private int length;
	private int type;
	private String payload;
	/**
	 * 
	 */
	public HelloMessage(int length, int type, String payload) {
		// The message class constructs
		// a normal message which is 
		// passed between peers
		// the structure is defined to match the specification
		// for this project.
		
		//TODO: calculate the length in bytes based off 
		//the length of type (1 byte) plus the length
		//of the payload in bytes
		
		this.setLength(length);
		this.setType(type);
		this.setPayload(payload);
		
	}
	
	public String getMessageString()
	{
		// This function returns 
		// the byte string representing
		// the message to be passed into
		// the TCP connection.  
		
		// TODO: convert to actual bytes
		// instead of using a string
		
		return "" + length + "" + type + payload;
		
	}
	
	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}
	/**
	 * @param length the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}
	/**
	 * @return the payload
	 */
	public String getPayload() {
		return payload;
	}
	/**
	 * @param payload the payload to set
	 */
	public void setPayload(String payload) {
		this.payload = payload;
	}

}
