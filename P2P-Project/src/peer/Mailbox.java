package peer;

import java.util.ArrayList;
import java.util.List;

public class Mailbox {
	
	List<Pair<Integer, List<Message>>> mailboxes = new ArrayList<Pair<Integer, List<Message>>>();
	
	public Mailbox() {
		// TODO Auto-generated constructor stub
		
	}
	
	public void addMailbox(int PeerID)
	{
		List<Message> m = new ArrayList<Message>();
		Pair<Integer, List<Message>> pair = new Pair<Integer, List<Message>>(PeerID, m);
		mailboxes.add(pair);
	}
	
	public void placeMessage(int PeerID, Message m)
	{
		List<Message> l;
		for(int i = 0; i < mailboxes.size(); i++)
		{
			if(mailboxes.get(i).getL() == PeerID)
			{
				l = mailboxes.get(i).getR();
				l.add(m);
				return;
			}
		}
		
		System.err.println("PeerID not found in list of mailboxes");
	}
	
	public Message getNextMessage(int PeerID)
	{
		List<Message> l;
		for(int i = 0; i < mailboxes.size(); i++)
		{
			if(mailboxes.get(i).getL() == PeerID)
			{
				l = mailboxes.get(i).getR();
				if(l.size() > 0)
				{
					Message m = l.get(0);
					mailboxes.get(i).getR().remove(0);
					return m;
				} else {
					return null;
				}
			}
		}
		
		return null;
	}

}
