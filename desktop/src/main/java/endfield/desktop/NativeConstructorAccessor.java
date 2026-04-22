package endfield.desktop;

import endfield.util.AbstractConstructorAccessor;
import endfield.util.Reflects;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.Objects;

import static endfield.desktop.DesktopImpl.lookup;

public class NativeConstructorAccessor<T> extends AbstractConstructorAccessor<T> {
	static final MethodHandle newInstance;

	static {
		try {
			Class<?> dec = Objects.requireNonNullElseGet(
					Reflects.findClass("jdk.internal.reflect.DirectConstructorHandleAccessor$NativeAccessor"), () ->
							Reflects.findClass("jdk.internal.reflect.NativeConstructorAccessorImpl")
			);
			newInstance = lookup.findStatic(dec, "newInstance0", MethodType.methodType(Object.class, Constructor.class, Object[].class));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public NativeConstructorAccessor(Constructor<T> cons) {
		super(cons);
	}

	@SuppressWarnings("unchecked")
	public static <T> T newInstance(Constructor<T> constructor, Object... args) throws Throwable {
		return (T) newInstance.invokeExact(constructor, args);
	}

	@Override
	public T newInstance(Object... args) {
		try {
			return newInstance(constructor, args);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof NativeConstructorAccessor<?> other && other.constructor.equals(constructor);
	}
}
