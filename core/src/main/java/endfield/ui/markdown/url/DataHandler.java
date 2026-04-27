package endfield.ui.markdown.url;

import endfield.ui.markdown.UrlHandler;
import endfield.util.Strings2;
import kotlin.text.Charsets;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class DataHandler implements UrlHandler {
	static final Pattern MIME_TYPE_PATTERN = Pattern.compile("\\w+(/\\w+)?;");
	static final Pattern DATA_TYPE_PATTERN = Pattern.compile("\\w+,");

	@Override
	public List<String> matchedSchemes() {
		return List.of("data");
	}

	@Override
	public void openUrl(String url) {
		throw new UnsupportedOperationException("Cannot open a data url directly.");
	}

	@Override
	public ResourceHandle getResource(String url) {
		url = url.replaceFirst("data:", "");
		MatchResult mimeMatch = Strings2.matchAt(MIME_TYPE_PATTERN, url, 0);
		int mimeEnd = mimeMatch == null ? 0 : mimeMatch.end();
		MatchResult dataMatch = Strings2.matchAt(DATA_TYPE_PATTERN, url, mimeEnd);
		int dataEnd = dataMatch == null ? 0 : dataMatch.end();

		if (dataMatch != null && dataMatch.group().equals("base64,")) {
			String base64 = url.substring(dataEnd);

			return new Base64Handle(base64);
		} else {
			String string = url.substring(dataEnd);

			return new StringHandle(string);
		}
	}

	public static class Base64Handle extends ResourceHandle {
		public final String base64;

		public Base64Handle(String base) {
			base64 = base;
		}

		@Override
		public InputStream openStream() {
			return Base64.getDecoder().wrap(new ByteArrayInputStream(base64.getBytes(Charsets.UTF_8)));
		}
	}

	public static class StringHandle extends ResourceHandle {
		public final String string;
		public final Charset charset;

		public StringHandle(String str) {
			this(str, Charsets.UTF_8);
		}

		public StringHandle(String str, Charset ch) {
			string = str;
			charset = ch;
		}

		@Override
		public InputStream openStream() {
			return new ByteArrayInputStream(string.getBytes(charset));
		}
	}
}
