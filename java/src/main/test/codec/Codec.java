package test.codec;

import test.util.Pair;

public interface Codec {
	public byte[] encode(Object obj);
	/**
	 * decode the raw bytes
	 * @param data
	 * @return target object and the data length consumed
	 */
	public Pair<Object, Integer> decode(byte[] data);
}
