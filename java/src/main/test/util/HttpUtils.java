package test.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {

	private HttpUtils() {
	}
	
	public static byte[] httpReq(String urlstr, byte[] datas) throws Exception {
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
}
