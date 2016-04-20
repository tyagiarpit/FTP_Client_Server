

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import utils.FileSegments;
import utils.ProgressBar;
import utils.Segment;
import utils.Timer;
import utils.UDPHeader;
import utils.UDPPacket;
import utils.Utils;

public class Simple_ftp_client {

	private final static String USAGE = "USAGE: Simple_ftp_client server-host-name server-port# file-name N MSS [SR]";
	private static int TIMEOUT = 50;
	private static DatagramSocket clientSocket;
	private static String hostname;
	private static int port;
	public static void main(String[] args) throws IOException, InterruptedException {
		if(args.length<5)
		{
			System.out.println(USAGE);
			System.exit(1);
		}

		/*
		String hostname = "localhost";
		int port = port;
		String filename = "/Users/Xeon/Desktop/ss1.png";
		int N = 5;
		int MSS = 5000;
		 */
		int mode = 1; //0- Normal, 1-Go back N, 2-Selective Repeat

		if(args.length>5){
			if(args[5].equalsIgnoreCase("SR"))
				mode=2;
		}

		boolean showResent = false;
		boolean showProgress = false;

		if(showProgress)
			showResent = false;

		hostname = args[0];
		port = Integer.parseInt(args[1]);
		String filename = args[2];
		int N = Integer.parseInt(args[3]);;
		int MSS = Integer.parseInt(args[4]);

		TIMEOUT = Utils.getRTT(hostname, 20)*2;
		if(TIMEOUT<=0)
			TIMEOUT=50;
		else if(TIMEOUT<10)
			TIMEOUT=10;
		
		File file = new File(filename);

		if(!file.exists())
		{
			System.out.println("File does not exists");
			//System.exit(1);
		}

		byte[] data = null;
		try {
			data = Utils.getFileData(file);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Some error occured while reading file...");
			System.exit(1);
		}

		FileSegments fileSegments = new FileSegments(data, MSS);

		if(showProgress)
		{
			ProgressBar.init(fileSegments.getSegments().length);
		}

		int client_port = (int) (50000+Math.random()*5000);
		clientSocket = new DatagramSocket(null);
		clientSocket.bind(new InetSocketAddress(InetAddress.getByName(InetAddress.getLocalHost().getHostName()),client_port));
		
		long start = System.nanoTime();

		if(mode==0){
			for (int i = 0; i < fileSegments.getSegments().length; i++) {

				clientSocket.setSoTimeout(TIMEOUT);

				byte b[] = fileSegments.getSegments()[i].getData();

				UDPHeader header = new UDPHeader(i,Utils.checksum(b),UDPHeader.HeaderType.DATA);
				UDPPacket packet = new UDPPacket(header,b);
				DatagramPacket p = new DatagramPacket(packet.getBytes(), packet.getBytes().length, InetAddress.getByName(hostname), port);

				clientSocket.send(p);

				byte[] buffer = new byte[2048]; 
				DatagramPacket receivePacket =new DatagramPacket(buffer, buffer.length);

				try{
					clientSocket.receive(receivePacket);
					if(showProgress)
					{	
						ProgressBar.tick();
					}
				}
				catch(SocketTimeoutException e){
					if(showResent)
						System.out.println("Packet loss, Sequence number="+i);
					i--;
				}
				byte[] receivedData = new byte[receivePacket.getLength()];

				System.arraycopy(receivePacket.getData(), 0, receivedData, 0, receivedData.length);

				UDPHeader ack = new UDPHeader(receivedData);

				fileSegments.getSegments()[ack.getSequence()].setAcknowledged(true);
			}
		}
		else if(mode==1){ //Go back N
			LinkedBlockingQueue<Segment> buffer = new LinkedBlockingQueue<Segment>(N);
			int i = 0;
			while(true){
				//Send N packets
					if(buffer.size()>0){

						for (Iterator<Segment> iterator = buffer.iterator(); iterator.hasNext();) {
							
							Segment segment = (Segment) iterator.next();
							if(showResent)
								System.out.println("Packet resent, Sequence number="+segment.getIndex());
							rdt_send(segment, buffer, false);
						}
					}

					while(true){
						if(buffer.size()==N)
							break;
						if(i>=fileSegments.getSegments().length)
							break;
						Segment segment = fileSegments.getSegments()[i++];
						
						rdt_send(segment, buffer);	
					}

					while(true){

						clientSocket.setSoTimeout(TIMEOUT);
						
						byte[] receivedBuffer = new byte[2048]; 
						DatagramPacket receivePacket =new DatagramPacket(receivedBuffer, receivedBuffer.length);
						try{
							clientSocket.receive(receivePacket);
						}
						catch(SocketTimeoutException e)
						{
							break;
						}
						byte[] receivedData = new byte[receivePacket.getLength()];
						System.arraycopy(receivePacket.getData(), 0, receivedData, 0, receivedData.length);
						UDPHeader ack = new UDPHeader(receivedData);



						while(buffer.size()>0&&buffer.element().getIndex() <= ack.getSequence()){
							Segment s = buffer.remove(); 
							s.getTimerThread().interrupt();
							if(showProgress)
							{	
								ProgressBar.tick();
							}
							//Add one more
							if(i>=fileSegments.getSegments().length)
								continue;
							
							Segment segment = fileSegments.getSegments()[i++];
							rdt_send(segment, buffer);
						}

						if(buffer.size()==0)
							break;

					}
					if(i==fileSegments.getSegments().length && buffer.isEmpty())
						break;
				

			}
		}
		else if(mode==2){ //Selective Repeat
			LinkedBlockingQueue<Segment> buffer = new LinkedBlockingQueue<Segment>(N);
			int i = 0;
			while(true){
				//Send N packets
				if(buffer.size()>0){
					for (Iterator<Segment> iterator = buffer.iterator(); iterator.hasNext();) {
						Segment segment = (Segment) iterator.next();
						if(showResent)
							System.out.println("Packet resent, Sequence number="+segment.getIndex());
						rdt_send(segment, buffer, false);
					}
				}

				while(true){
					if(buffer.size()==N)
						break;
					if(i>=fileSegments.getSegments().length)
						break;
					Segment segment = fileSegments.getSegments()[i++];

					rdt_send(segment, buffer);
				}

				while(true){

					clientSocket.setSoTimeout(TIMEOUT);

					byte[] receivedBuffer = new byte[2048]; 
					DatagramPacket receivePacket =new DatagramPacket(receivedBuffer, receivedBuffer.length);

					try{
						clientSocket.receive(receivePacket);
					}
					catch(SocketTimeoutException e){
						if(showResent)
							System.out.println("Packet lost");
						break;
					}

					byte[] receivedData = new byte[receivePacket.getLength()];
					System.arraycopy(receivePacket.getData(), 0, receivedData, 0, receivedData.length);
					UDPHeader ack = new UDPHeader(receivedData);

					for (Iterator<Segment> iterator = buffer.iterator(); iterator.hasNext();) {
						Segment _segment = (Segment) iterator.next();
						if(_segment.getIndex()==ack.getSequence())
						{
							_segment.setAcknowledged(true);
							buffer.remove(_segment);
							if(showProgress)
							{	
								ProgressBar.tick();
							}
							//Add one more
							if(i>=fileSegments.getSegments().length)
								break;
							Segment segment = fileSegments.getSegments()[i++];

							rdt_send(segment, buffer);

							break;
						}

					}

					if(buffer.size()==0)
						break;

				}
				if(i==fileSegments.getSegments().length && buffer.isEmpty())
					break;
			}
		}

		long end = System.nanoTime();

		UDPHeader header = new UDPHeader(0,0,UDPHeader.HeaderType.FIN);
		DatagramPacket p = new DatagramPacket(header.getHeaderData(), 8, InetAddress.getByName(hostname), port);
		clientSocket.send(p);
		if(showProgress)
			ProgressBar.finish();
		Thread.sleep(2*TIMEOUT);
		System.out.println("\n\nTransfer Time : "+(end-start)/1000000+"ms\n\n");

		clientSocket.close();
	}
	private static void rdt_send(Segment segment, LinkedBlockingQueue<Segment> buffer) throws IOException{
		rdt_send(segment, buffer, true);
	}	
	private static void rdt_send(Segment segment, LinkedBlockingQueue<Segment> buffer, boolean addToBuffer) throws IOException{
		if(addToBuffer)
			buffer.add(segment);

		byte b[] = segment.getData();

		UDPHeader header = new UDPHeader(segment.getIndex(),Utils.checksum(b),UDPHeader.HeaderType.DATA);
		UDPPacket packet = new UDPPacket(header,b);
		DatagramPacket p = new DatagramPacket(packet.getBytes(), packet.getBytes().length, InetAddress.getByName(hostname), port);
		clientSocket.send(p);
		segment.setTimerThread(new Timer(segment.getIndex(),TIMEOUT));
		segment.getTimerThread().start();
	}

}


