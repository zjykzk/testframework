package test.work;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import test.msg.EnterServer;
import test.msg.Login;
import test.stati.Stati;
import test.util.HttpUtils;

public class SnsWorker extends Worker {
	public static void initialize(String startId) {
		udidGenerator = new AtomicLong(Long.parseLong(startId));
	}
	
	public SnsWorker(Stati stati, CountDownLatch cdLatch, String url, String interval) {
		super(stati, cdLatch, url, interval);
	}
	
	@Override
	protected List<Callable<Boolean>> getRequests() {
		List<Callable<Boolean>> ret = new ArrayList<>();
		ret.add(new LoginReq());
		ret.add(new EnterServerReq());
		return ret;
	}
	
	private class LoginReq implements Callable<Boolean> {
		@Override
		public Boolean call() {
			try {
				SnsWorker.this.udid = "" + udidGenerator.incrementAndGet();
				Login loginReq = new Login(SnsWorker.this.udid, 
						"ios", SnsWorker.this.udid);
				System.out.println(Thread.currentThread().getId() + ": request login " + loginReq);
				byte[] resp = HttpUtils.httpReq(SnsWorker.this.getUrl(), loginReq.bytes());
				List<Object> out = loginReq.response(resp);
				if (out.size() < 2) return false;
				SnsWorker.this.verifyCode = (int) out.get(out.size() - 2);
				SnsWorker.this.playerId = (int) out.get(0);
				System.out.println("playerId : " + SnsWorker.this.playerId);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}
	
	private class EnterServerReq implements Callable<Boolean> {
		@Override
		public Boolean call() {
			try {
				EnterServer es = new EnterServer(SnsWorker.this.playerId,
						SnsWorker.this.verifyCode, "" + SnsWorker.this.udid);
				System.out.println(Thread.currentThread().getId() + ": request enter " + es);
				byte[] datas = HttpUtils.httpReq(SnsWorker.this.getUrl(), es.bytes());
				List<Object> out = es.response(datas);
				if (!out.isEmpty())
					System.out.println("EnterServerReq, errCode " + out.get(0));
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			
		}
	}
	private int verifyCode, playerId;
	private String udid;
	private static AtomicLong udidGenerator;
}
