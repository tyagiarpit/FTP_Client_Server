package utils;

import java.util.Arrays;

public class UDPHeader {
	public enum HeaderType {DATA,ACK,FIN};
	private byte[] headerData = new byte[8];
	private int sequence;
	private short checksum;
	private HeaderType type;
	
	public UDPHeader(int sequence,int checksum, HeaderType type){
		this.sequence = sequence;
		this.checksum = (short)checksum;
		this.type = type;
		System.arraycopy(Utils.intToBytes(this.sequence), 0, headerData, 0,  4);
		System.arraycopy(Utils.shortToBytes(this.checksum), 0, headerData, 4,  2);
		if(this.type == HeaderType.ACK)
			headerData[7] = headerData[6] = (byte)(170 & 0xFF);
		else if(this.type == HeaderType.DATA)
			headerData[7] = headerData[6] = (byte)(85 & 0xFF);
		else if(this.type == HeaderType.FIN)
			headerData[7] = headerData[6] = (byte)(0 & 0xFF);
			
	}
	
	public UDPHeader(byte[] bytes){
		headerData = bytes;
		this.sequence = Utils.bytesToInt(Arrays.copyOfRange(bytes, 0, 4));
		this.checksum = Utils.bytesToShort(Arrays.copyOfRange(bytes, 4, 6));
		if(bytes[6]==(byte)170 && bytes[7]==(byte)170)
			this.type = HeaderType.ACK;
		else if(bytes[6]==(byte)85 && bytes[7]==(byte)85)
			this.type = HeaderType.DATA;
		else if(bytes[6]==(byte)0 && bytes[7]==(byte)0)
			this.type = HeaderType.FIN;
	}
	
	public byte[] getHeaderData() {
		return headerData;
	}
	public int getSequence() {
		return sequence;
	}
	public short getChecksum() {
		return checksum;
	}
	public HeaderType getType() {
		return type;
	}
	public void print(){
		for (int i = 0; i < headerData.length; i++) {
			byte b = headerData[i];
			System.out.print(Utils.getBinarySequence(b, 8));
			if(i==3)
				System.out.println();
			else if(i==1||i==5)
				System.out.print(" ");
		}
		System.out.println();
	}
	public void printReadable(){
		System.out.println("Sequence: " + sequence);
		System.out.println("Checksum: " + checksum);
		System.out.println("Type    : " + type.toString());
	}
	
}
