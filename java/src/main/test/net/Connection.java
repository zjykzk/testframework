package test.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

final class Connection {
	public Connection(String host, int port) {
		super();
		this.host = host;
		this.port = port;
	}
	
	public void connect() throws Exception {
		socket.connect(new InetSocketAddress(host, port), 3000);
	}
	
	int request(byte[] data, byte[] revBuf, int offset, int len) throws Exception {
		socket.getOutputStream().write(data);
		socket.getOutputStream().flush();
		
		return socket.getInputStream().read(revBuf, offset, len);
	}
	
	public void disConnect() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final String host;
	private final int port;
	private Socket socket;
}
