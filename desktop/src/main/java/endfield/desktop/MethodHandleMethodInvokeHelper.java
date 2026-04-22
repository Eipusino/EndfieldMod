package endfield.desktop;

import arc.func.Prov;
import endfield.util.CollectionObjectMap;
import endfield.util.FunctionType;
import endfield.util.MethodInvokeHelper;
import endfield.util.NoSuchFunctionException;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static endfield.desktop.DesktopClassHelper.ctypes;
import static endfield.desktop.DesktopClassHelper.function5;
import static endfield.desktop.DesktopClassHelper.function6;
import static endfield.desktop.DesktopClassHelper.mtypes;
import static endfield.desktop.DesktopClassHelper.ptypes;
import static endfield.desktop.DesktopImpl.lookup;

public class MethodHandleMethodInvokeHelper implements MethodInvokeHelper {
	protected static final CollectionObjectMap<Class<?>, CollectionObjectMap<String, CollectionObjectMap<FunctionType, MethodHandle>>> methodPool = new CollectionObjectMap<>(Class.class, CollectionObjectMap.class);
	protected static final CollectionObjectMap<Class<?>, Method[]> methodsMap = new CollectionObjectMap<>(Class.class, Method[].class);
	protected static final CollectionObjectMap<Class<?>, Constructor<?>[]> constructorsMap = new CollectionObjectMap<>(Class.class, Constructor[].class);

	protected static final Prov<CollectionObjectMap<String, CollectionObjectMap<FunctionType, MethodHandle>>> prov6 = () -> new CollectionObjectMap<>(String.class, CollectionObjectMap.class);
	protected static final Prov<CollectionObjectMap<FunctionType, MethodHandle>> prov7 = () -> new CollectionObjectMap<>(FunctionType.class, MethodHandle.class);

	protected MethodHandle getMethod(Class<?> clazz, String name, FunctionType types) throws IllegalAccessException {
		CollectionObjectMap<FunctionType, MethodHandle> map = methodPool.get(clazz, prov6).get(name, prov7);

		FunctionType type = FunctionType.inst(types);
		MethodHandle res = map.get(type);

		if (res != null) return res;

		for (var entry : map) {
			if (entry.key.match(types)) return entry.value;
		}

		Class<?> curr = clazz;

		while (curr != null) {
			Method method = findMethod(curr, name, types.paramType());

			if (method != null) {
				res = asSpreader(method);
				map.put(from(method), res);
				return res;
			}

			curr = curr.getSuperclass();
		}

		curr = clazz;

		while (curr != null) {
			for (Method method : methodsMap.computeIfAbsent(curr, function5)) {
				if (!method.getName().equals(name)) continue;

				FunctionType t;
				if ((t = from(method)).match(types)) {
					res = asSpreader(method);
					map.put(t, res);
					return res;
				}
				t.recycle();
			}

			curr = curr.getSuperclass();
		}

		throw new NoSuchFunctionException("no such method " + name + " in class: " + clazz + " with assignable parameter: " + types);
	}

	protected MethodHandle getConstructor(Class<?> clazz, FunctionType types) throws IllegalAccessException {
		CollectionObjectMap<FunctionType, MethodHandle> map = methodPool.get(clazz, prov6).get("<init>", prov7);

		MethodHandle res = map.get(types);
		if (res != null) return res;

		for (var entry : map) {
			if (entry.key.match(types)) return entry.value;
		}

		Constructor<?> find = findConstructor(clazz, types.paramType());
		if (find != null) {
			res = asSpreader(find);
			map.put(from(find), res);
		}

		if (res != null) return res;

		for (Constructor<?> constructor : constructorsMap.computeIfAbsent(clazz, function6)) {
			FunctionType functionType;
			if ((functionType = from(constructor)).match(types)) {
				res = asSpreader(constructor);
				map.put(functionType, res);

				break;
			}
			functionType.recycle();
		}

		if (res != null) return res;

		throw new NoSuchFunctionException("no such constructor in class: " + clazz + " with assignable parameter: " + types);
	}

	protected @Nullable Method findMethod(Class<?> type, String name, Class<?>[] paramType) {
		Method[] methods = methodsMap.computeIfAbsent(type, function5);
		for (Method method : methods) {
			if (method.getName().equals(name) && Arrays.equals((Class<?>[]) mtypes.get(method), paramType)) return method;
		}
		return null;
	}

	protected @Nullable Constructor<?> findConstructor(Class<?> type, Class<?>[] paramType) {
		Constructor<?>[] constructors = constructorsMap.computeIfAbsent(type, function6);
		for (Constructor<?> constructor : constructors) {
			if (Arrays.equals((Class<?>[]) ctypes.get(constructor), paramType)) return constructor;
		}
		return null;
	}

	protected final MethodHandle asSpreader(Method method) throws IllegalAccessException {
		MethodHandle target = lookup.unreflect(method);

		int paramCount = target.type().parameterCount();

		if ((method.getModifiers() & Modifier.STATIC) != 0) {
			return target.asSpreader(Object[].class, paramCount)
					.asType(MethodType.methodType(Object.class, Object[].class));
		} else {
			if (paramCount < 1)
				throw new IllegalArgumentException("Instance method must have e receiver");
			MethodHandle spread = target.asSpreader(Object[].class, paramCount -1);
			MethodType newType = spread.type()
					.changeParameterType(0, Object.class)
					.changeReturnType(Object.class);
			return spread.asType(newType);
		}
	}

	protected final MethodHandle asSpreader(Constructor<?> constructor) throws IllegalAccessException {
		MethodHandle target = lookup.unreflectConstructor(constructor);

		int paramCount = target.type().parameterCount();
		MethodHandle spread = target.asSpreader(Object[].class, paramCount);
		return spread.asType(MethodType.methodType(Object.class, Object[].class));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(Object object, String name, Object... args) {
		FunctionType type = FunctionType.inst(args);
		try {
			return (T) getMethod(object.getClass(), name, type).invokeExact(object, args);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invokeStatic(Class<?> clazz, String name, Object... args) {
		FunctionType type = FunctionType.inst(args);
		try {
			return (T) getMethod(clazz, name, type).invokeExact(args);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T newInstance(Class<T> clazz, Object... args) {
		FunctionType type = FunctionType.inst(args);
		try {
			return (T) getConstructor(clazz, type).invokeExact(args);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invokeTyped(Object object, String name, Class<?>[] parameterTypes, Object... args) {
		FunctionType type = FunctionType.inst(parameterTypes);
		try {
			return (T) getMethod(object.getClass(), name, type).invokeExact(object, args);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invokeStaticTyped(Class<?> clazz, String name, Class<?>[] parameterTypes, Object... args) {
		FunctionType type = FunctionType.inst(parameterTypes);
		try {
			return (T) getMethod(clazz, name, type).invokeExact(args);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T newInstanceTyped(Class<T> clazz, Class<?>[] parameterTypes, Object... args) {
		FunctionType type = FunctionType.inst(parameterTypes);
		try {
			return (T) getConstructor(clazz, type).invokeExact(args);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	protected MethodHandle getMethod(Method method, FunctionType types) throws IllegalAccessException {
		CollectionObjectMap<FunctionType, MethodHandle> map = methodPool.get(method.getDeclaringClass(), prov6).get(method.getName(), prov7);

		FunctionType type = FunctionType.inst(types);
		MethodHandle res = map.get(type);

		if (res != null) return res;

		for (var entry : map) {
			if (entry.key.match(types)) return entry.value;
		}

		res = asSpreader(method);

		map.put(inst(res.type()), res);

		return res;
	}

	protected MethodHandle getConstructor(Constructor<?> constructor, FunctionType types) throws IllegalAccessException {
		CollectionObjectMap<FunctionType, MethodHandle> map = methodPool.get(constructor.getDeclaringClass(), prov6).get("<init>", prov7);

		MethodHandle res = map.get(types);
		if (res != null) return res;

		for (var entry : map) {
			if (entry.key.match(types)) return entry.value;
		}

		res = asSpreader(constructor);

		map.put(types, res);

		return res;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(Method method, Object object, Object... args) {
		FunctionType type = from(method);
		try {
			return (T) getMethod(method, type).invokeExact(object, args);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invokeStatic(Method method, Object... args) {
		FunctionType type = from(method);
		try {
			return (T) getMethod(method, type).invokeExact(args);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T newInstance(Constructor<T> constructor, Object... args) {
		FunctionType type = from(constructor);
		try {
			return (T) getConstructor(constructor, type).invokeExact(args);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	public static FunctionType inst(MethodType methodType) {
		return FunctionType.inst((Class<?>[]) ptypes.get(methodType));
	}

	public static FunctionType from(Method method) {
		return FunctionType.inst((Class<?>[]) mtypes.get(method));
	}

	public static FunctionType from(Constructor<?> constructor) {
		return FunctionType.inst((Class<?>[]) ctypes.get(constructor));
	}
}
