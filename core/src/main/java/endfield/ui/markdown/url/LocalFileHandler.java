package endfield.ui.markdown.url;

import arc.Core;
import endfield.ui.markdown.UrlHandler;

import java.io.File;
import java.util.List;

public class LocalFileHandler implements UrlHandler {
	@Override
	public List<String> matchedSchemes() {
		return List.of("file");
	}

	@Override
	public void openUrl(String url) {
		String fi = url.replaceFirst("file://", "");
		Core.app.openFolder(fi);
	}

	@Override
	public ResourceHandle getResource(String url) {
		String fi = url.replaceFirst("file://", "");
		File file = new File(fi);
		return new FileHandle(file);
	}
}
