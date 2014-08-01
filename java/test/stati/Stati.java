package test.stati;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Stati {
	public Stati() {
		totalReqCount = new AtomicLong();
		totalRespTime = new AtomicLong();
		totalReqFailedCount = new AtomicLong();
	}
	
	public void incReqFailedCount() {
		totalReqFailedCount.incrementAndGet();
	}
	
	public void updateRespTime(long respTime) {
		updateTimeLock.lock();
		try {
			if (minRespTime > respTime) {
				minRespTime = respTime;
				return;
			}
			
			if (maxRespTime < respTime) {
				maxRespTime = respTime;
			}
		} finally {
			updateTimeLock.unlock();
		}
		totalReqCount.incrementAndGet();
		totalRespTime.addAndGet(respTime);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append("total request count:").append(totalReqCount)
			.append(", total failed request count:").append(totalReqFailedCount)
			.append(", total request cost time:").append(totalRespTime).append("(milis)")
			.append(",averge request cost time:")
			.append(totalRespTime.doubleValue() /
					(totalReqCount.doubleValue() - totalReqFailedCount.doubleValue()))
			.append("(milis)")
			.append(", max response cost time:").append(maxRespTime).append("(milis)")
			.append(", min response cost time:").append(minRespTime).append("(milis)");
		return sb.toString();
	}
	
	private final AtomicLong totalReqCount;
	private final AtomicLong totalReqFailedCount;
	private final AtomicLong totalRespTime;
	private long minRespTime = Long.MAX_VALUE, maxRespTime = Long.MIN_VALUE;
	private final Lock updateTimeLock = new ReentrantLock();
	
	public static void main(String[] args) {
		System.out.println(new Stati());
	}
}
