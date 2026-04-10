package endfield.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class ReflectionConstructorAccessor extends AbstractConstructorAccessor {
	public ReflectionConstructorAccessor(Constructor<?> cons) {
		super(cons);

		if (!Reflects.setAccessible(constructor)) throw new IllegalStateException("Unable to access constructor: " + cons);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T newInstance(Object... args) {
		try {
			return (T) constructor.newInstance(args);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof ReflectionConstructorAccessor other && other.constructor.equals(constructor);
	}
}
