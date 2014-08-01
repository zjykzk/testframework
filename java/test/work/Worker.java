package test.work;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import test.stati.Stati;

public class Worker implements Runnable {

	public Worker(Stati stati,CountDownLatch cdLatch, final String url, String intervel) {
		this.stati = stati;
		this.cdLatch = cdLatch;
		this.url = url;
		this.interval = Long.parseLong(intervel);
		
		requests = new ArrayList<>(1);
		requests.add(new Callable<Boolean>() {
			
			@SuppressWarnings("unused")
			@Override
			public Boolean call() {
				try {
					byte[] bytes = httpReq(url, null);
//					System.out.println(new String(bytes, "UTF-8"));
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}
		});
	}
	
	protected String getUrl() { return url; }

	private final Stati stati;
	private final CountDownLatch cdLatch;
	private boolean isClose = false;
	private final String url;
	private final List<Callable<Boolean>> requests;
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
	
	protected List<Callable<Boolean>> getRequests() {
		return requests;
	}
	
	private static byte[] httpReq(String urlstr, byte[] datas) throws Exception {
		URL url = new URL(urlstr);
		HttpURLConnection httpconnection = (HttpURLConnection) url
				.openConnection();
		httpconnection.setRequestMethod("POST");
		httpconnection.setRequestProperty("Content", "text/html;charset=utf-8");
		boolean hasData = datas != null;
		if (hasData) {
			httpconnection.setRequestProperty("Content-Length", "" + datas.length);
		}
		httpconnection.setDoInput(true);
		httpconnection.setDoOutput(true);
		if (hasData) {
			OutputStream outputStream = httpconnection.getOutputStream();
			outputStream.write(datas);
			outputStream.flush();
			outputStream.close();
		}

		int code = httpconnection.getResponseCode();
		if (code == HttpURLConnection.HTTP_OK) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(128);
			InputStream in = httpconnection.getInputStream();

			int rc = 0;
			while ((rc = in.read()) != -1) {
				bos.write(rc);
			}
			in.close();

			return bos.toByteArray();
		} else {
			throw new IOException(httpconnection.getResponseMessage());
		}
	}

	public void close() {
		isClose = true;
	}
}
