package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

public class Utils {
	
	
	public static int getRTT(String hostname, int sampleSize){
		//System.out.println("Calculating RTT for host:"+hostname);
		int RTT = 0;
		double[] RTTSamples = new double[sampleSize+1];
		int client_port = (int) (55000+Math.random()*5000);
		DatagramSocket clientSocket = null;
		byte[] buffer = new byte[64];
		try {
			clientSocket = new DatagramSocket(null);
			clientSocket.bind(new InetSocketAddress(InetAddress.getByName(InetAddress.getLocalHost().getHostName()),client_port));
			clientSocket.setSoTimeout(1000);
			for (int i = 0; i < RTTSamples.length; i++) {
				try{
					long start = System.nanoTime();
					clientSocket.send(new DatagramPacket(buffer, 64, InetAddress.getByName(hostname), 57783));
					clientSocket.receive(new DatagramPacket(buffer, 64));
					long end = System.nanoTime();
					RTTSamples[i] = ((double)(end-start))/1000000d;
				}
				catch(SocketTimeoutException e){
					RTTSamples[i] = -1;
				}
			}
			
		} catch (Exception e) {
			return 0;
		}
		finally{
			clientSocket.close();
		}
		int packets = 0;
		double time = 0;
		for (int i = 1; i < RTTSamples.length; i++) {
			double r = RTTSamples[i];
			if(r<=0)
				continue;
			packets++;
			time+=r;
			
		}
		if(packets==0)
			packets++;
		RTT = (int) Math.ceil(time/packets);
		System.out.println("RTT: "+(time/packets)+"ms");
		return RTT;
	}
	
	public static void handleRTT(){
		//System.out.println("Sending RTT information to client...");
		int server_port = 57783;
		DatagramSocket serverSocket = null;

		try {
			serverSocket = new DatagramSocket(null);
			serverSocket.bind(new InetSocketAddress(InetAddress.getByName(InetAddress.getLocalHost().getHostName()),server_port));
			System.out.println("Listening on "+InetAddress.getLocalHost().getHostAddress());
			serverSocket.setSoTimeout(30000);
			while (true) {
				try{
					byte[] buffer = new byte[64];
					DatagramPacket receivePacket =new DatagramPacket(buffer, buffer.length);
					serverSocket.receive(receivePacket);
					serverSocket.send(new DatagramPacket(buffer, 64, receivePacket.getAddress(),receivePacket.getPort()));
					serverSocket.setSoTimeout(200);
				}
				catch (Exception e){
					break;
				}
			}
			
		} catch (Exception e) {
		}
		finally{
			serverSocket.close();
		}
	}
	
	public static String getBinarySequence(int i){
		return Integer.toBinaryString(i);
	}

	public static String getBinarySequence(int i,int length){
		String s = Integer.toBinaryString(i);
		if(s.length()<length){
			int diff = length - s.length();
			for(int j = 0;j<diff;j++){
				s="0"+s;
			}
		}
		return s;
	}

	public static String getBinarySequence(byte i,int length){
		String s = Integer.toBinaryString((i+256)%256);
		if(s.length()<length){
			int diff = length - s.length();
			for(int j = 0;j<diff;j++){
				s="0"+s;
			}
		}
		return s;
	}

	public static int toDecimal(String s){
		return Integer.parseInt(s);
	}

	public static int bytesToInt(byte[] bytes) 
	{
		return bytes[3] & 0xFF | (bytes[2] & 0xFF) << 8 | (bytes[1] & 0xFF) << 16 | (bytes[0] & 0xFF) << 24;
	}

	public static byte[] intToBytes(int i)
	{
		return new byte[] { (byte) ((i >> 24) & 0xFF), (byte) ((i >> 16) & 0xFF), (byte) ((i >> 8) & 0xFF), (byte) (i & 0xFF)};
	}

	public static short bytesToShort(byte[] bytes) 
	{
		return (short) (bytes[1] & 0xFF | (bytes[0] & 0xFF) << 8);
	}

	public static byte[] shortToBytes(short i)
	{
		return new byte[] {(byte) ((i >> 8) & 0xFF), (byte) (i & 0xFF)};
	}

	public static byte[] getFileData(File file) throws IOException {
		RandomAccessFile f = new RandomAccessFile(file, "r");
		byte[] b = new byte[(int)f.length()];
		f.read(b);
		f.close();
		return b;
	}

	public static void createFile(File file,byte[] bytes) throws IOException {
		FileOutputStream stream = new FileOutputStream(file);
		try {
			stream.write(bytes);
		} finally {
			stream.close();
		}
	}
	
	public static short checksum(byte[] buf) {
		int length = buf.length;
	    int i = 0;
	    short sum = 0;
	    while (length > 0) {
	        sum += (buf[i++]&0xff) << 8;
	        if ((--length)==0) break;
	        sum += (buf[i++]&0xff);
	        --length;
	    }
	    return (short) ((~((sum & 0xFFFF)+(sum >> 16)))&0xFFFF);
	}
}
