package peer;

import java.io.DataOutputStream;
import java.io.IOException;

public interface Message {
		public String getMessageString();
		
		public void writeTo(DataOutputStream os) throws IOException ;
}
