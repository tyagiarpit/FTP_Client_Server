package ftp.common.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Utils {
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
