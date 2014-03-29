package peer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * 
 */

public class NormalMessage implements Message {
	
	private int length;
	private int type;
	private byte[] payload;
	/**
	 * 
	 */
	public NormalMessage(int length, int type, byte[] payload) 
	{
		this.setLength(length);
		this.setType(type);
		this.setPayload(payload);
		
	}
	public NormalMessage(int length, int type) 
	{
		this.setLength(length);
		this.setType(type);
		this.setPayload(null);
		
	}
	
	//accessors
	public int getLength() {
		return length;
	}

	public int getType() {
		return type;
	}

	public byte[] getPayload() {
		return payload;
	}

	//mutators
	public void setLength(int length) {
		this.length = length;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}
	
	//methods
	public String getMessageString()
	{
		if(payload != null)
			return length + "" + type + payload;	
		else
			return length + "" + type;
	}
	
	public void writeTo(DataOutputStream os) throws IOException {
			os.writeInt(length);
			os.writeByte(type);
			if(payload != null)
				os.write(payload);
	}

}
