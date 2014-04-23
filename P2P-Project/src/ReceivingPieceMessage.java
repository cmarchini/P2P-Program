

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.*;

public class ReceivingPieceMessage extends NormalMessage {
	
	public static final int ADDITIONAL_LENGTH = 5;  // in bytes, includes messageType and pieceIndex
	public static final int BLOCK_SIZE = 256; // Note this number can be different from Sending

	protected int offset;
	protected int pieceIndex;
	protected DataInputStream dis;

	public ReceivingPieceMessage(int length, int pieceSize, int pieceIndex, DataInputStream dis) {
		super(length, 7);
		
		this.pieceIndex = pieceIndex;
		this.offset = pieceSize*pieceIndex;
		this.dis = dis;
	}
	
	public void writeToFile(String fileName) throws IOException {
		int remainingLengthOfPiece = length - ADDITIONAL_LENGTH;
		
  	byte[] data = new byte[BLOCK_SIZE];
 
  	ByteBuffer out;


		try (FileChannel fc = (FileChannel.open(new File(fileName).toPath(), READ, WRITE, CREATE))) {
		    fc.position(offset);
				while (remainingLengthOfPiece > BLOCK_SIZE) {
					// read data from network
			  	dis.read(data);
			  	
			  	// write data to file.  Honestly I'm not sure how this works
					out = ByteBuffer.wrap(data);
			    while (out.hasRemaining())
		        fc.write(out);
					
					remainingLengthOfPiece -= BLOCK_SIZE;
				}
				data = new byte[remainingLengthOfPiece];
		  	dis.read(data);
		  	
				out = ByteBuffer.wrap(data);
		    while (out.hasRemaining())
	        fc.write(out);
		    
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
	}
	
	public int getPieceIndex() {
		return pieceIndex;
	}

}
