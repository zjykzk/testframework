package test.net;

final class RevBuf {
	public RevBuf(int size) {
		buf = new byte[size];
		this.dataLength = 0;
		this.size = size;
	}
	public int getSize() {
		return size;
	}
	
	public byte[] getBuf() {
		return buf;
	}
	
	public int getDataLength() {
		return dataLength;
	}
	
	public void incrDataLength(int count) {
		dataLength += count;
	}
	
	public void consumeData(int count) {
		assert count <= dataLength;
		if (dataLength == count) {
			dataLength = 0;
			return;
		}
		
		System.arraycopy(buf, count, buf, 0, dataLength - count);
		dataLength -= count;
	}
	
	private final int size;
	private int dataLength;
	private final byte[] buf;
}
