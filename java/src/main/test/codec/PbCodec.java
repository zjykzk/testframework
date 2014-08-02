package test.codec;

import test.util.Pair;

import com.google.protobuf.MessageLite;

/**
 * +--------------+------------+-------------------+
 * | 2 bytes      | 2 bytes    |      ...          |
 * +--------------+------------+-------------------+
 * |packet length | message id | message bytes     |
 * +--------------+------------+-------------------+
 * packet length does not include self's length
 * @author zenk
 *
 */
public class PbCodec implements Codec {

	public PbCodec(ProtoIdMap protoIdMap) {
		this.protoIdMap = protoIdMap;
	}

	@Override
	public byte[] encode(Object obj) {
		assert obj instanceof MessageLite;
		
		byte[] data = ((MessageLite) obj).toByteArray();
		byte[] ret = new byte[data.length + HEAD_SIZE];
		writeShortBigLitten(ret, 0, (short)(data.length + ID_SIZE));
		writeShortBigLitten(ret, PACKET_LEN_SIZE, 
				protoIdMap.getProtoId(((MessageLite) obj).getClass()));
		return ret;
	}

	@Override
	public Pair<Object, Integer> decode(byte[] data) {
		int length = data.length;
		if (length < HEAD_SIZE) return null;
		short packLen = readShortBigLitten(data, 0);
		if (packLen >= length - PACKET_LEN_SIZE) return null;
		
		short id = readShortBigLitten(data, PACKET_LEN_SIZE);
		byte[] msgData = new byte[packLen - HEAD_SIZE];
		System.arraycopy(data, HEAD_SIZE, msgData, 0, msgData.length);
		ProtoParser parser = protoIdMap.getProtoParser(id);
		if (parser == null) {
			throw new RuntimeException("cannot know protocal id " + id);
		}
		return new Pair<Object, Integer>(parser.parse(msgData), packLen + PACKET_LEN_SIZE);
	}
	
	private short readShortBigLitten(byte[] buf, int offset) {
		return (short) ((buf[1] << 8) | buf[0]);
	}
	
	private void writeShortBigLitten(byte[] buf, int offset, short data) {
		buf[offset + 1] = (byte)(data & 0xff);
		buf[offset] = (byte)(data >>> 8);
	}
	
	private static final int PACKET_LEN_SIZE = 2;
	private static final int ID_SIZE = 2;
	private static final int HEAD_SIZE = PACKET_LEN_SIZE + ID_SIZE;
	
	private final ProtoIdMap protoIdMap;

	public interface ProtoIdMap {
		short getProtoId(Class<?> cls);
		ProtoParser getProtoParser(short id);
	}
	
	public interface ProtoParser {
		Object parse(byte[] datas);
	}
}
