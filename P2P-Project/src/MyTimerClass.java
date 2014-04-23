

class MyTimerTask extends java.util.TimerTask {
	String param;
	
	public MyTimerTask(String param) {
		this.param = param;
	}
	
	@Override
	public void run()
	{
		System.out.println("Peer: " + param + " this is called every 5 sec");
	}
}

