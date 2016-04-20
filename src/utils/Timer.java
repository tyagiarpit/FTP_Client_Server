package utils;

public class Timer extends Thread {
	private int sequenceNo;
	private int TIMEOUT;
	public Timer(int s, int t){
		sequenceNo = s;
		TIMEOUT = t;
	}
	@Override
	public void run() {
		try {
			Thread.sleep(TIMEOUT);
			System.out.println("Timeout, sequence no = "+sequenceNo);
		} catch (InterruptedException e) {
			//System.out.println("Ack received: "+sequenceNo);
		}
	}
}
