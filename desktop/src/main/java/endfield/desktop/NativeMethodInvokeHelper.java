package endfield.desktop;

import endfield.util.FunctionType;
import endfield.util.ReflectionMethodInvokeHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class NativeMethodInvokeHelper extends ReflectionMethodInvokeHelper {
	@Override
	public <T> T invoke(Object object, String name, Object... args) {
		FunctionType type = FunctionType.inst(args);
		try {
			return NativeMethodAccessor.invoke(getMethod(object.getClass(), name, type), object, args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	@Override
	public <T> T invokeStatic(Class<?> clazz, String name, Object... args) {
		FunctionType type = FunctionType.inst(args);
		try {
			return NativeMethodAccessor.invoke(getMethod(clazz, name, type), null, args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	@Override
	public <T> T newInstance(Class<T> type, Object... args) {
		FunctionType funcType = FunctionType.inst(args);
		try {
			return NativeConstructorAccessor.newInstance(getConstructor(type, funcType), args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			funcType.recycle();
		}
	}

	@Override
	public <T> T invoke(Method method, Object object, Object... args) {
		try {
			return NativeMethodAccessor.invoke(method, object, args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> T invokeStatic(Method method, Object... args) {
		try {
			return NativeMethodAccessor.invoke(method, null, args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> T newInstance(Constructor<T> constructor, Object... args) {
		try {
			return NativeConstructorAccessor.newInstance(constructor, args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
