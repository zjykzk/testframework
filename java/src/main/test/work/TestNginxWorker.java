package test.work;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import test.stati.Stati;
import test.util.HttpUtils;

public class TestNginxWorker extends Worker {

	public TestNginxWorker(Stati stati, CountDownLatch cdLatch,
			String intervel, final String url) {
		super(stati, cdLatch, intervel);
		requests = new ArrayList<>(1);
		requests.add(new Callable<Boolean>() {
			
			@SuppressWarnings("unused")
			@Override
			public Boolean call() {
				try {
					byte[] bytes = HttpUtils.httpPost(url, null);
//					System.out.println(new String(bytes, "UTF-8"));
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}
		});
	}
	
	@Override
	protected List<Callable<Boolean>> getRequests() {
		return this.requests;
	}
	private final List<Callable<Boolean>> requests;
}
