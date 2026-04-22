package endfield.util.aspector.classes;

import arc.util.OS;
import endfield.util.handler.ClassHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class BytecodeClassLoader extends ClassLoader implements BytecodeLoader {
	protected final String protocol = "byteloader-" + getClass().getSimpleName() + hashCode();

	protected final Map<String, byte[]> bytecodesMap = new HashMap<>();
	protected final Map<String, byte[]> bytecodesPaths = new HashMap<>();

	public BytecodeClassLoader(ClassLoader parent) {
		super(parent);
	}

	@Override
	public void declareClass(String name, byte[] bytecode) {
		if (bytecodesMap.containsKey(name))
			throw new IllegalArgumentException("Class $name is already registered");

		String path = name.replace(".", "/") + ".class";
		bytecodesMap.put(name, bytecode);
		bytecodesPaths.put(path, bytecode);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected URL findResource(String name) {
		byte[] bytecode = bytecodesPaths.get(name);
		if (bytecode == null) return null;

		URLStreamHandler handler = new URLStreamHandler() {
			@Override
			protected URLConnection openConnection(URL u) throws IOException {
				return new URLConnection(u) {
					public ByteArrayInputStream stream;

					@Override
					public void connect() throws IOException {
						stream = new ByteArrayInputStream(bytecode);
					}

					@Override
					public InputStream getInputStream() throws IOException {
						connect();
						return stream;
					}

					@Override
					public long getContentLengthLong() {
						return bytecode.length;
					}

					@Override
					public String getContentType() {
						return "application/octet-stream";
					}
				};
			}
		};

		try {
			return new URL(protocol, null, -1, name, handler);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		return new Enumeration<>() {
			private URL next = findResource(name);

			@Override
			public boolean hasMoreElements() {
				return next != null;
			}

			@Override
			public URL nextElement() {
				if (next == null) {
					throw new NoSuchElementException();
				}
				URL u = next;
				next = null;
				return u;
			}
		};
	}

	@Override
	public Class<?> loadClass(String name) {
		return loadClass(name, false);
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) {
		try {
			return super.loadClass(name, resolve);
		} catch (ClassNotFoundException e) {
			byte[] bytecode = bytecodesMap.get(name);

			if (bytecode == null)
				throw new NoSuchElementException(name);

			Class<?> result = OS.isAndroid ?
					ClassHandler.defineClass(name, bytecode, this) :
					super.defineClass(name, bytecode, 0, bytecode.length);
			if (resolve) resolveClass(result);
			return result;
		}
	}
}
