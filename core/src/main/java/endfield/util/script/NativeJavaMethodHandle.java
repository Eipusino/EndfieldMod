package endfield.util.script;

import rhino.BaseFunction;
import rhino.Context;
import rhino.Scriptable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import static endfield.Vars2.platformImpl;
import static endfield.util.script.Scripts2.convertArgs;

/**
 * @see rhino.NativeJavaMethod
 */
public class NativeJavaMethodHandle extends BaseFunction {
	protected final MethodHandle handle, spreadHandle;

	protected final int paramCount;
	protected final Class<?> returnType;
	protected final Class<?>[] parameterArray;

	public NativeJavaMethodHandle(Scriptable scope, Class<?> declaringClass, String name, Class<?>... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
		this(scope, declaringClass.getDeclaredMethod(name, parameterTypes));
	}

	public NativeJavaMethodHandle(Scriptable scope, Method method) throws IllegalAccessException {
		this(scope, platformImpl.lookup(method.getDeclaringClass()).unreflect(method));
	}

	public NativeJavaMethodHandle(Scriptable scope, MethodHandle method) {
		super(scope, null);

		handle = method;

		MethodType type = method.type();

		paramCount = type.parameterCount();
		returnType = type.returnType();
		parameterArray = type.parameterArray();

		spreadHandle = method.asSpreader(Object[].class, paramCount)
				.asType(MethodType.methodType(Object.class, Object[].class));
	}

	@Override
	public String toString() {
		return handle.toString();
	}

	@Override
	public Object get(Object key) {
		if ("__javaObject__".equals(key)) return handle;
		return super.get(key);
	}

	@Override
	public Object call(Context context, Scriptable scope, Scriptable scriptable, Object[] args) {
		try {
			return context.getWrapFactory().wrap(context, scope, spreadHandle.invokeExact(convertArgs(args, parameterArray)), returnType);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
