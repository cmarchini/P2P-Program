package peer;

import java.io.DataOutputStream;
import java.io.IOException;

// TODO not yet implemented
public class PieceMessage extends NormalMessage {

	public PieceMessage(int pieceSize, int offset, String filename) {
		super(pieceSize, 7); // TODO find length = min(pieceSize, remaining bytes in file)
	}
	
	public void writeTo(DataOutputStream os) throws IOException {
		os.writeInt(length);
		os.writeByte(type);

		// TODO implement
	}

}
