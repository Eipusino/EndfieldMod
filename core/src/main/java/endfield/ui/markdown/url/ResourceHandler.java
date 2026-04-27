package endfield.ui.markdown.url;

import endfield.ui.markdown.Markdown;
import endfield.ui.markdown.UrlHandler;

import java.io.InputStream;
import java.util.List;

public class ResourceHandler implements UrlHandler {
	@Override
	public List<String> matchedSchemes() {
		return List.of("resource");
	}

	@Override
	public void openUrl(String url) {
		throw new UnsupportedOperationException("open jar resource was not supported");
	}

	@Override
	public ResourceHandle getResource(String url) {
		String path = url.replaceFirst("resource://", "");

		return new ResourceHandle() {
			@Override
			public InputStream openStream() {
				InputStream stream = Markdown.class.getClassLoader().getResourceAsStream(path);

				if (stream == null) throw new RuntimeException("no such jar resource found in " + path);

				return stream;
			}
		};
	}
}
