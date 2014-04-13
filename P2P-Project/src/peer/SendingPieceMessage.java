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

	public SendingPieceMessage(int pieceSize, int pieceIndex, String filename) {
		super(0, 7);
		
		this.pieceIndex = pieceIndex;
		this.offset = pieceSize*pieceIndex;
		this.file = new File(filename);
		
		long fileSize = file.length();
		int lengthOfPiece = (int)Math.min(pieceSize, fileSize - offset); // length = min(pieceSize, remaining bytes in file)
		if (lengthOfPiece <= 0) throw new RuntimeException("Oops, nonpositive piece length");
		int lengthOfPayload = lengthOfPiece + ADDITIONAL_LENGTH;
		setLength(lengthOfPayload);
	}
	
	public void writeTo(DataOutputStream os) throws IOException {
		int remainingLengthOfPiece = length - ADDITIONAL_LENGTH;
		int blockOffset = offset;
		
		byte[] block = new byte[BLOCK_SIZE];
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);

			os.writeInt(length);
			os.writeByte(type);
			os.writeInt(pieceIndex);

			while (remainingLengthOfPiece > BLOCK_SIZE) {
				fis.read(block, blockOffset, BLOCK_SIZE);
				os.write(block);
				
				remainingLengthOfPiece -= BLOCK_SIZE;
				blockOffset += BLOCK_SIZE;
			}
			fis.read(block, blockOffset, remainingLengthOfPiece);
			os.write(block, 0, remainingLengthOfPiece);
			
			fis.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		

	}

}
