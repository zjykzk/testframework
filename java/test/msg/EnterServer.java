package test.msg;

import java.io.DataInputStream;
import java.util.List;

public class EnterServer extends AbastractMsg {

	public EnterServer(int playerId, int verifyCode, String udid) {
		super((short)8, playerId, verifyCode);
		this.udid = udid;
	}
	
	

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EnterServer [udid=");
		builder.append(udid);
		builder.append("]");
		return builder.toString();
	}

	@Override
	protected void appendData() {
		write(udid);
		write("ch");
	}

	@Override
	protected void parseData(DataInputStream input, List<Object> out)
			throws Exception {
		int errorCode = input.readShort();
		out.add(errorCode);
//		if (0 == errorCode) {
//			out.add(input.readBoolean());
//			out.add(input.readBoolean());
//			out.add(input.readLong());
//			out.add(input.readLong());
//		}
		// omit the results
	}
	private final String udid;
}
