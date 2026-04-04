package endfield.android;

import endfield.util.ClassHelper;
import mindustry.android.AndroidRhinoContext.AndroidContextFactory;
import rhino.ContextFactory;
import rhino.GeneratedClassLoader;

import java.util.Objects;

import static endfield.android.Unsafer.unsafe;

public class AndroidClassHelper implements ClassHelper {
	@Override
	public Class<?> defineClass(String name, byte[] bytes, ClassLoader loader) {
		return ((GeneratedClassLoader) ((AndroidContextFactory) ContextFactory.getGlobal())
				.createClassLoader(loader))
				.defineClass(name, bytes);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T allocateInstance(Class<? extends T> clazz) {
		Objects.requireNonNull(clazz);

		try {
			return (T) unsafe.allocateInstance(clazz);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
	}
}
