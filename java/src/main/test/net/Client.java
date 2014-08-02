package test.net;

import test.codec.Codec;
import test.util.Pair;

public final class Client {
	public Client(String host, int port, Codec codec, boolean isReconn, int bufSize) {
		this.codec = codec;
		this.con = new Connection(host, port);
		this.isReconn = isReconn;
		this.buf = new RevBuf(bufSize);
	}
	
	public boolean connect() {
		try {
			con.connect();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void disConnect() {
		con.disConnect();
	}
	
	public Object request(Object param) {
		byte[] reqData = codec.encode(param);
		try {
			byte[] revBuf = buf.getBuf();
			int revCount = con.request(reqData, revBuf, buf.getDataLength(), buf.getSize());
			buf.incrDataLength(revCount);
			
			Pair<? extends Object, Integer> pair = codec.decode(revBuf);
			while (pair.fisrt() == null) {
				pair = codec.decode(revBuf);
				try { Thread.sleep(100);} catch (Exception e) {}
			}
			buf.consumeData(pair.second());
			return pair.fisrt();
		} catch (Exception e) {
			e.printStackTrace();
			reConnect();
		}
		
		return null;
	}

	private void reConnect() {
		while (isReconn) {
			try {
				con.connect();
				Thread.sleep(3000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
	}

	private final Codec codec;
	private final Connection con;
	private final boolean isReconn;
	private final RevBuf buf;
}
