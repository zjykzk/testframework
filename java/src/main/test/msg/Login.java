package test.msg;

import java.io.DataInputStream;
import java.util.List;

public class Login extends AbastractMsg {
	public Login(String udid,
			String os, String device) {
		super((short)30000, 0, 0);
		this.udid = udid;
		this.os = os;
		this.device = device;
	}

	@Override
	protected void appendData() {
		write(udid);
		write(os);
		write(device);
		write("TW");
		write("");
	}

	@Override
	protected void parseData(DataInputStream input, List<Object> out) throws Exception {
		short errCode = input.readShort();
		if (errCode != 0) {
			System.out.println("login error code " + errCode);
			return;
		}
		out.add(input.readInt()); // id
		out.add(input.readUTF()); // name
		out.add(input.readUTF()); // email
		out.add(input.readInt()); // verify code
		out.add(input.readInt()); // version
	}
	

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Login [udid=");
		builder.append(udid);
		builder.append(", os=");
		builder.append(os);
		builder.append(", device=");
		builder.append(device);
		builder.append("]");
		return builder.toString();
	}

	private String udid;
	private String os;
	private String device;
}
