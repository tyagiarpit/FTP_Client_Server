package ftp.common.utils;

import java.util.Iterator;
import java.util.List;

public class FileSegments {
	private Segment[] segments;
	private int fileSize;
	public Segment[] getSegments() {
		return segments;
	}

	public void setSegments(Segment[] segments) {
		this.segments = segments;
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public int getSegmentSize() {
		return segmentSize;
	}

	public void setSegmentSize(int segmentSize) {
		this.segmentSize = segmentSize;
	}

	private int segmentSize;
	
	public FileSegments(List<Segment> segments){
		Segment[] _segments = new Segment[segments.size()];
		segmentSize = segments.get(0).getLength();
		this.fileSize = 0;
		for (Iterator<Segment> iterator = segments.iterator(); iterator.hasNext();) {
			Segment segment = (Segment) iterator.next();
			_segments[segment.getIndex()] = segment;
		}
		
		for (int i = 0; i < _segments.length; i++) {
			if(_segments[i]==null){
				this.segments = new Segment[i];
				break;
			}
			fileSize+=_segments[i].getLength();
		}
		if(this.segments==null)
			this.segments = _segments;
		else
			System.arraycopy(_segments, 0, this.segments, 0, this.segments.length);
	}
	
	public FileSegments(byte[] data, int segmentSize) {
		fileSize = data.length;
		this.segmentSize = segmentSize;
		int number_of_segments = data.length/segmentSize;
		if((data.length%segmentSize)!=0)
			number_of_segments++;
		
		segments = new Segment[number_of_segments];
		
		
		for (int i = 0; i < number_of_segments; i++) {
			if(i<number_of_segments-1)
			{
				byte[] _data = new byte[segmentSize];
				System.arraycopy(data, i*segmentSize, _data, 0, segmentSize);
				segments[i] = new Segment(_data,i);
			}
			else //Last Segment
			{
				int remaining = data.length-segmentSize*i;
				byte[] _dataremaining = new byte[remaining];
				System.arraycopy(data, i*segmentSize, _dataremaining, 0, remaining);
				segments[i] = new Segment(_dataremaining,i);
			}
		}
	}
	
	public byte[] getFileData(){
		byte[] fileData = new byte[fileSize];
		for (int i = 0; i < segments.length; i++) {
			Segment s = segments[i];
			try{
				System.arraycopy(s.getData(), 0, fileData, i*segmentSize, s.getLength());
			}
			catch(Exception e){
				System.out.println(i);
			}
		}
		return fileData;
	}
	
	
}
