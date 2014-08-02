package test.work;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import test.stati.Stati;

public abstract class Worker implements Runnable {

	public Worker(Stati stati,CountDownLatch cdLatch, String intervel) {
		this.stati = stati;
		this.cdLatch = cdLatch;
		this.interval = Long.parseLong(intervel);
	}
	
	private final Stati stati;
	private final CountDownLatch cdLatch;
	private boolean isClose = false;
	private final long interval;

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (!isClose) {
			try {
				for (Callable<Boolean> r : getRequests()) {
					long now = System.currentTimeMillis();
					boolean ret = r.call();
					long cost = System.currentTimeMillis() - now;
					stati.updateRespTime(cost);
					if (!ret) {
						stati.incReqFailedCount();
						break;
					}
					Thread.sleep(this.interval);
				}
			} catch (Exception e) {
				stati.incReqFailedCount();
				System.out.println(e.getMessage());
				try { Thread.sleep(3000); } catch (Exception ex) {}
			}
		}
		cdLatch.countDown();
	}
	
	protected abstract List<Callable<Boolean>> getRequests();

	public void close() {
		isClose = true;
	}
}
