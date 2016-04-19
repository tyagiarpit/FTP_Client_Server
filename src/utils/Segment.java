package utils;

public class Segment {
	private byte[] data;
	private int length;
	private boolean acknowledged;
	
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	private int index;
	
	public Segment(byte[] data, int index) {
		this.data = data;
		this.index = index;
		this.acknowledged = false;
		length = this.data.length;
	}

	public boolean isAcknowledged() {
		return acknowledged;
	}

	public void setAcknowledged(boolean acknowledged) {
		this.acknowledged = acknowledged;
	}
}
