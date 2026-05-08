package endfield.desktop;

import endfield.util.AbstractMethodAccessor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import static endfield.desktop.DesktopImpl.lookup;

public final class MethodHandleStaticMethodAccessor extends AbstractMethodAccessor {
	final MethodHandle spreadHandle;

	public MethodHandleStaticMethodAccessor(Method met) {
		super(met);

		try {
			MethodHandle target = lookup.unreflect(met).asFixedArity();

			int paramCount = target.type().parameterCount();

			spreadHandle = target.asSpreader(Object[].class, paramCount)
					.asType(MethodType.methodType(Object.class, Object[].class));
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(Object object, Object... args) {
		try {
			return (T) spreadHandle.invokeExact(args);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
