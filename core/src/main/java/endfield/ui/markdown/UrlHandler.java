package endfield.ui.markdown;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface UrlHandler {
	List<String> matchedSchemes();

	void openUrl(String url);

	ResourceHandle getResource(String url);

	abstract class ResourceHandle implements Closeable {
		InputStream currStream;

		public InputStream open() {
			if (currStream != null)
				throw new IllegalStateException("Can't open a handle twice at same time");
			currStream = openStream();

			return currStream;
		}

		@Override
		public void close() throws IOException {
			if (currStream != null) {
				currStream.close();
				currStream = null;
			}
		}

		public abstract InputStream openStream();
	}

	class ByteArrayHandle extends ResourceHandle {
		byte[] bytes;

		public ByteArrayHandle(byte[] bytes1) {
			bytes = bytes1;
		}

		@Override
		public InputStream openStream() {
			return new ByteArrayInputStream(bytes);
		}
	}

	class FileHandle extends ResourceHandle {
		File file;

		public FileHandle(File file1) {
			file = file1;
		}

		@Override
		public InputStream openStream() {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
