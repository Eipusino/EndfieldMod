package endfield.util;

import java.lang.reflect.Constructor;
import java.util.Arrays;

public abstract class AbstractConstructorAccessor<T> implements ConstructorAccessor<T> {
	protected final Constructor<T> constructor;

	protected int hash;

	protected AbstractConstructorAccessor(Constructor<T> cons) {
		constructor = cons;
	}

	@Override
	public Constructor<T> getConstructor() {
		return constructor;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof AbstractConstructorAccessor<?> other && other.constructor.equals(constructor);
	}

	@Override
	public int hashCode() {
		int hc = hash;

		if (hc == 0) {
			hc = hash = constructor.getDeclaringClass().getName().hashCode() ^
					Arrays.hashCode(constructor.getParameterTypes());
		}

		return hc;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + constructor.toString();
	}
}
