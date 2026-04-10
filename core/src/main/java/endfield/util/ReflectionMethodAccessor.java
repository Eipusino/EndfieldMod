package endfield.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionMethodAccessor extends AbstractMethodAccessor {
	public ReflectionMethodAccessor(Method met) {
		super(met);

		if (!Reflects.setAccessible(met)) throw new IllegalStateException("Unable to access method: " + met);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(Object object, Object... args) {
		try {
			return (T) method.invoke(object, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof ReflectionMethodAccessor other && other.method.equals(method);
	}
}
