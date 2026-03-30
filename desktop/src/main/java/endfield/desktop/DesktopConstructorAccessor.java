package endfield.desktop;

import endfield.util.AbstractConstructorAccessor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;

import static endfield.desktop.DesktopImpl.lookup;

public final class DesktopConstructorAccessor extends AbstractConstructorAccessor {
	final MethodHandle spreadHandle;

	public DesktopConstructorAccessor(Constructor<?> cons) {
		super(cons);

		try {
			MethodHandle target = lookup.unreflectConstructor(cons);

			int paramCount = target.type().parameterCount();
			MethodHandle spread = target.asSpreader(Object[].class, paramCount);
			spreadHandle = spread.asType(MethodType.methodType(Object.class, Object[].class));
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T newInstance(Object... args) {
		try {
			return (T) spreadHandle.invokeExact(args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof DesktopConstructorAccessor other && other.getConstructor().equals(constructor);
	}
}
