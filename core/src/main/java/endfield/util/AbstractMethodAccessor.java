package endfield.util;

import java.lang.reflect.Method;
import java.util.Arrays;

public abstract class AbstractMethodAccessor implements MethodAccessor {
	protected final Method method;

	protected int hash;

	protected AbstractMethodAccessor(Method met) {
		method = met;
	}

	@Override
	public Method getMethod() {
		return method;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof AbstractMethodAccessor other && other.method.equals(method);
	}

	@Override
	public int hashCode() {
		int hc = hash;

		if (hc == 0) {
			hc = hash = method.getDeclaringClass().getName().hashCode() ^
					method.getName().hashCode() ^
					Arrays.hashCode(method.getParameterTypes());
		}

		return hc;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + method.toString();
	}
}
