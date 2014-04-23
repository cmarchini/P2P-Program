

public class peerProcess {

	public static void main( String[] args ) 
	{
		if (args.length >= 1) try {
			int peerIDArg = Integer.parseInt(args[0]);
			new Peer(peerIDArg).start(); // start with integer port specified in first argument
			return;
		} catch (NumberFormatException e) {
			
		}
		
		// if we got to this point a legal port was not specified
		new Peer().start();
	}

}
