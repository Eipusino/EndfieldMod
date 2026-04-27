package endfield.ui.markdown.url;

import arc.Core;
import arc.util.Http;
import endfield.ui.markdown.UrlHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class HttpHandler implements UrlHandler {
	@Override
	public List<String> matchedSchemes() {
		return List.of("https", "http");
	}

	@Override
	public void openUrl(String url) {
		Core.app.openURI(url);
	}

	@Override
	public ResourceHandle getResource(String url) {
		try {
			return new HttpsHandle(new URI(url).toURL());
		} catch (URISyntaxException | MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public static class HttpsHandle extends ResourceHandle {
		public final URL url;
		public final int timeout;

		HttpURLConnection currentConnection;

		public HttpsHandle(URL url1, int timeout1) {
			url = url1;
			timeout = timeout1;
		}

		public HttpsHandle(URL url1) {
			this(url1, 2000);
		}

		@Override
		public InputStream openStream() {
			try {
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				currentConnection = connection;

				connection.setDoOutput(false);
				connection.setDoInput(true);
				connection.setRequestMethod("GET");
				HttpURLConnection.setFollowRedirects(true);

				connection.setConnectTimeout(timeout);
				connection.setReadTimeout(timeout);

				connection.connect();

				int code = connection.getResponseCode();

				if (code >= 400) {
					Http.HttpStatus status = Http.HttpStatus.byCode(code);
					throw new ConnectFailedException(
							"HTTP request failed with error: " + code + " (" + status + ", URL = " + url + ")",
							status
					);
				} else {
					return connection.getInputStream();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void close() throws IOException {
			super.close();

			currentConnection.disconnect();
		}
	}

	static class ConnectFailedException extends RuntimeException {
		public ConnectFailedException(String message, Http.HttpStatus code) {
			super(message);
		}
	}
}
