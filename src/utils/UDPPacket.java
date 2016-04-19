package utils;

public class UDPPacket {
	private UDPHeader header;
	private byte[] data;
	private byte[] bytes;
	
	public UDPPacket(UDPHeader header, byte[] data) {
		this.header = header;
		this.data = data;
		this.bytes = new byte[8+(this.data==null?0:this.data.length)];
		System.arraycopy(header.getHeaderData(), 0, bytes, 0,  8);
		if(this.data!=null)
			System.arraycopy(this.data, 0, bytes, 8,  data.length);
	}
	
	public UDPPacket(byte[] bytes) {
		byte[] headerBytes = new byte[8];
		System.arraycopy(bytes, 0, headerBytes, 0,  8);
		this.header = new UDPHeader(headerBytes);
		if(bytes.length>8){
			this.data = new byte[bytes.length-8];
			System.arraycopy(bytes, 8, this.data, 0,  this.data.length);
		}
	}
	
	public UDPHeader getHeader() {
		return header;
	}
	public void setHeader(UDPHeader header) {
		this.header = header;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
	public byte[] getBytes(){
		return bytes;
	}
}
