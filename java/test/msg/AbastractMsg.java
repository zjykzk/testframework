package test.msg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
abstract class AbastractMsg {
	public AbastractMsg(short msgId, int playerId, int verifyCode) {
		this.msgId = msgId;
		this.playerId = playerId;
		this.verifyCode = verifyCode;
	}
	
	public byte[] bytes() {
		ByteArrayOutputStream bytes =new ByteArrayOutputStream(0xffff);
		buffer = new DataOutputStream(bytes);
		write(playerId);
		write(verifyCode);
		write(msgId);
		
		DataOutputStream tmpBuffer = buffer;
		ByteArrayOutputStream byteStram = new ByteArrayOutputStream(0xffff);
		buffer = new DataOutputStream(byteStram);
		
		appendData();
		DataOutputStream tmpl = tmpBuffer;
		tmpBuffer = buffer;
		buffer = tmpl;
		write(byteStram.size());
		write(byteStram.toByteArray());
		
		return bytes.toByteArray();
	}
	
	private byte[] unCompress(byte[] input) {
		byte[] b = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(input);
			GZIPInputStream gzip = new GZIPInputStream(bis);
			byte[] buf = new byte[1024];
			int num = -1;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((num = gzip.read(buf, 0, buf.length)) != -1) {
				baos.write(buf, 0, num);
			}
			b = baos.toByteArray();
			baos.flush();
			baos.close();
			gzip.close();
			bis.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return b;
	}
	
	@SuppressWarnings("unused")
	public List<Object> response(byte[] datas) throws Exception {
		List<Object> ret = new ArrayList<>();
		ByteArrayInputStream byteArrInStream = new ByteArrayInputStream(datas);
		DataInputStream input = new DataInputStream(byteArrInStream);
		short errCode = input.readShort();
		byte msgCount = input.readByte();
		byte isCompress = input.readByte();
//		System.out.println("is compress " + isCompress);
		int msgDataLen = input.readInt();
//		System.out.println("message data length " + msgDataLen);
		String httpRet = input.readUTF();
		System.out.println("http result " + httpRet);
		
		if (errCode == 0) {
			if (isCompress != -1) {
				byte[] bytes = new byte[input.available()];
				int cnt = input.read(bytes);
				input.close();
				
				byte[] uncompressBytes = unCompress(bytes);
				input = new DataInputStream(new ByteArrayInputStream(uncompressBytes, 0, cnt));
			}
			
			while (msgCount-- > 0) {
				short messageId = input.readShort();
//				System.out.println("messsage id " + messageId);
				int dataLen = input.readInt();
//				System.out.println("data length " + dataLen); // data length
				parseData(input, ret);
			}
		}
		input.close();
		return ret;
	}
	
	protected void write(int i) {
		try {
			buffer.writeInt(i);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void write(short s) {
		try {
			buffer.writeShort(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void write(String str) {
		try {
			buffer.writeUTF(str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void write(long l) {
		try {
			buffer.writeLong(l);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void write(byte[] bs) {
		try {
			buffer.write(bs, 0, bs.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected abstract void appendData();
	protected abstract void parseData(DataInputStream input, List<Object> out) throws Exception;
	
	
	private short msgId;
	private int playerId;
	private int verifyCode;
	private DataOutputStream buffer = null;
}
