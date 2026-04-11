package endfield.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ReflectionConstructorAccessor<T> extends AbstractConstructorAccessor<T> {
	public ReflectionConstructorAccessor(Constructor<T> cons) {
		super(cons);

		if (!Reflects.setAccessible(constructor)) throw new IllegalStateException("Unable to access constructor: " + cons);
	}

	@Override
	public T newInstance(Object... args) {
		try {
			return constructor.newInstance(args);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof ReflectionConstructorAccessor<?> other && other.constructor.equals(constructor);
	}
}
