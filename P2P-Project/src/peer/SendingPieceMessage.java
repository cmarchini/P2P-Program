package peer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

// TODO not yet implemented
public class SendingPieceMessage extends NormalMessage {
	
	public static final int ADDITIONAL_LENGTH = 5;  // in bytes, includes messageType and pieceIndex
	public static final int BLOCK_SIZE = 256; // I made up this number - could change
	
	protected int offset;
	protected int pieceIndex;
	protected File file;

	public SendingPieceMessage(int pieceSize, int pieceIndex, String filename, int fileSize) {
		super(0, 7);
		
		this.pieceIndex = pieceIndex;
		this.offset = pieceSize*pieceIndex;
		this.file = new File(filename);
		
		int lengthOfPiece = (int)Math.min(pieceSize, fileSize - offset); // length = min(pieceSize, remaining bytes in file)
		if (lengthOfPiece <= 0) throw new RuntimeException("Oops, nonpositive piece length");
		int lengthOfPayload = lengthOfPiece + ADDITIONAL_LENGTH;
		setLength(lengthOfPayload);
	}
	
	public void writeTo(DataOutputStream os) throws IOException {
		int remainingLengthOfPiece = length - ADDITIONAL_LENGTH; // bits left to send
		
		byte[] block = new byte[BLOCK_SIZE]; // block to send
		FileInputStream fis;
		try {
			fis = new FileInputStream(file); // input

			os.writeInt(length); // sending info to output
			os.writeByte(type);
			os.writeInt(pieceIndex);

			fis.skip(offset); // position in file is now at offset
			while (remainingLengthOfPiece > BLOCK_SIZE) {
				fis.read(block, 0, BLOCK_SIZE); // after this, position is += BLOCK_SIZE
				os.write(block); // write block to output stream
				
				remainingLengthOfPiece -= BLOCK_SIZE;
			} // complete last send with smaller block:
			fis.read(block, 0, remainingLengthOfPiece);
			os.write(block, 0, remainingLengthOfPiece);
			
			fis.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		

	}

}
