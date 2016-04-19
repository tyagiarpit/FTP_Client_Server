

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Random;

import utils.FileSegments;
import utils.Segment;
import utils.UDPHeader;
import utils.UDPPacket;
import utils.Utils;

public class Simple_ftp_server {

	
	private final static String USAGE = "USAGE: Simple_ftp_server port# file-name p";
	
	private static DatagramSocket serverSocket;
	
	public static void main(String[] args) throws IOException {
		if(args.length<3)
		{
			System.out.println(USAGE);
			System.exit(1);
		}
		
		/*
		int port = 7783;
		String fileName = "/Users/Xeon/Desktop/ss_bhargav.png";
		double p = 0.2;
		*/
		
		int port = Integer.parseInt(args[0]);
		
		String fileName = args[1];
		
		double p = Double.parseDouble(args[2]);
		
		
		
		serverSocket = new DatagramSocket(port);

		ArrayList<Segment> segmentsList = new ArrayList<Segment>();
		
		Random randomGenerator = new Random();
		
		while(true){
			
			byte[] buffer = new byte[204800]; 
			DatagramPacket receivePacket =new DatagramPacket(buffer, buffer.length);
			
			serverSocket.receive(receivePacket);
			
			byte[] receivedData = new byte[receivePacket.getLength()];
			
			System.arraycopy(receivePacket.getData(), 0, receivedData, 0, receivedData.length);
			
			UDPPacket packet = new UDPPacket(receivedData);
			
			/*You can do better*/
			if(packet.getHeader().getType()==UDPHeader.HeaderType.FIN)
				break;
			
			int seq = packet.getHeader().getSequence();
			
			//Check drop status
			double random = randomGenerator.nextDouble();
			if(random<p)
			{
				System.out.println("Packet loss, Sequence number="+seq);
				continue;
			}
			
			//Check checksum
			short receivedChecksum = packet.getHeader().getChecksum();
			short computedChecksum = Utils.checksum(packet.getData());
			
			if(receivedChecksum!=computedChecksum){
				System.out.println("Packet courrpted, Sequence number="+seq);
				continue;
			}
			
			//System.out.println("Packet received, Sequence number="+seq);
			
			//Add segment to file segments
			segmentsList.add(new Segment(packet.getData(), seq));
			
			UDPHeader ackHeader = new UDPHeader(seq, 0, UDPHeader.HeaderType.ACK);
			
			DatagramPacket ackPacket = new DatagramPacket(ackHeader.getHeaderData(), ackHeader.getHeaderData().length, receivePacket.getAddress(),receivePacket.getPort());
			serverSocket.send(ackPacket);
		
		}
		
		//Reconstruct file
		FileSegments fs = new FileSegments(segmentsList);
		byte[] data = fs.getFileData();
		
		Utils.createFile(new File(fileName), data);
		
		
	}

}
