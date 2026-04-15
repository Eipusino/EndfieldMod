package endfield.util;

import static endfield.util.MockPlatformImpl.unsafe;

@SuppressWarnings("removal")
public class MockClassHelper implements ClassHelper {
	@SuppressWarnings("unchecked")
	@Override
	public <T> T allocateInstance(Class<? extends T> clazz) {
		try {
			return (T) unsafe.allocateInstance(clazz);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Class<?> defineClass(String name, byte[] bytes, ClassLoader loader) {
		throw new UnsupportedOperationException();
	}
}
