package endfield.android;

import arc.func.Prov;
import dynamilize.FunctionType;
import endfield.util.CollectionObjectMap;
import endfield.util.MethodInvokeHelper;
import endfield.util.Reflects;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static endfield.Vars2.platformImpl;

/**
 * @deprecated In fact, the efficiency of using method handles on Android does not seem to be as
 * good as reflection, Let's not consider handle implementation for now.
 */
@Deprecated
public class AndroidMethodInvokeHelper2 implements MethodInvokeHelper {
	static final CollectionObjectMap<Class<?>, CollectionObjectMap<String, CollectionObjectMap<FunctionType, MethodHandle>>> methodPool = new CollectionObjectMap<>(Class.class, CollectionObjectMap.class);

	static final Prov<CollectionObjectMap<String, CollectionObjectMap<FunctionType, MethodHandle>>> prov1 = () -> new CollectionObjectMap<>(String.class, CollectionObjectMap.class);
	static final Prov<CollectionObjectMap<FunctionType, MethodHandle>> prov2 = () -> new CollectionObjectMap<>(FunctionType.class, MethodHandle.class);

	protected MethodHandle getMethod(Class<?> clazz, String name, FunctionType argTypes) {
		CollectionObjectMap<FunctionType, MethodHandle> map = methodPool.get(clazz, prov1).get(name, prov2);

		FunctionType type = FunctionType.inst(argTypes);
		MethodHandle res = map.get(type);

		if (res != null) return res;

		for (var entry : map) {
			if (entry.key.match(argTypes.getTypes())) return entry.value;
		}

		Class<?> curr = clazz;

		while (curr != null) {
			try {
				Method method = curr.getDeclaredMethod(name, argTypes.getTypes());
				method.setAccessible(true);
				res = platformImpl.lookup(curr).unreflect(method);
				map.put(FunctionType.from(method), res);
			} catch (Throwable ignored) {}

			if (res != null) {
				map.put(FunctionType.inst(res.type().parameterArray()), res);
				break;
			}

			curr = curr.getSuperclass();
		}

		if (res != null) return res;

		curr = clazz;
		a:
		while (curr != null) {
			for (Method method : curr.getDeclaredMethods()) {
				if (!method.getName().equals(name)) continue;
				FunctionType t;
				if ((t = FunctionType.from(method)).match(argTypes.getTypes())) {
					method.setAccessible(true);
					try {
						res = platformImpl.lookup(curr).unreflect(method);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
					map.put(t, res);
					//methodMap.put(method, res);
					break a;
				}
				t.recycle();
			}

			curr = curr.getSuperclass();
		}

		if (res == null)
			throw new RuntimeException("no such method " + name + " in class: " + clazz + " with assignable parameter: " + argTypes);

		return res;
	}

	protected MethodHandle getConstructor(Class<?> type, FunctionType argsType) {
		CollectionObjectMap<FunctionType, MethodHandle> map = methodPool.get(type, prov1).get("<init>", prov2);

		MethodHandle res = map.get(argsType);
		if (res != null) return res;

		for (var entry : map) {
			if (entry.key.match(argsType.getTypes())) return entry.value;
		}

		try {
			Constructor<?> constructor = type.getConstructor(argsType.getTypes());
			constructor.setAccessible(true);
			res = platformImpl.lookup(type).unreflectConstructor(constructor);
			map.put(FunctionType.inst(res.type().parameterArray()), res);
		} catch (NoSuchMethodException | IllegalAccessException ignored) {}

		if (res != null) return res;

		for (Constructor<?> constructor : type.getDeclaredConstructors()) {
			FunctionType functionType;
			if ((functionType = FunctionType.from(constructor)).match(argsType.getTypes())) {
				try {
					constructor.setAccessible(true);
					res = platformImpl.lookup(type).unreflectConstructor(constructor);
					map.put(functionType, res);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}

				break;
			}
			functionType.recycle();
		}

		if (res != null) return res;

		throw new RuntimeException("no such constructor in class: " + type + " with assignable parameter: " + argsType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(Object object, String name, Object... args) {
		FunctionType type = FunctionType.inst(args);
		try {
			return (T) Reflects.invokeVirtual(object, getMethod(object.getClass(), name, type), args);
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
			return (T) Reflects.invokeStatic(getMethod(clazz, name, type), args);
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
			return (T) Reflects.invokeStatic(getConstructor(clazz, type), args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invokeWithAsType(Object object, String name, Class<?>[] parameterTypes, Object... args) {
		FunctionType type = FunctionType.inst(parameterTypes);
		try {
			return (T) Reflects.invokeVirtual(object, getMethod(object.getClass(), name, type), args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invokeStaticWithAsType(Class<?> clazz, String name, Class<?>[] parameterTypes, Object... args) {
		FunctionType type = FunctionType.inst(parameterTypes);
		try {
			return (T) Reflects.invokeStatic(getMethod(clazz, name, type), args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T newInstanceWithAsType(Class<T> clazz, Class<?>[] parameterTypes, Object... args) {
		FunctionType type = FunctionType.inst(parameterTypes);
		try {
			return (T) Reflects.invokeStatic(getConstructor(clazz, type), args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	protected MethodHandle getMethod(Method method, FunctionType argTypes) throws IllegalAccessException {
		CollectionObjectMap<FunctionType, MethodHandle> map = methodPool.get(method.getDeclaringClass(), prov1).get(method.getName(), prov2);

		FunctionType type = FunctionType.inst(argTypes);
		MethodHandle res = map.get(type);

		if (res != null) return res;

		for (var entry : map) {
			if (entry.key.match(argTypes.getTypes())) return entry.value;
		}

		res = platformImpl.lookup(method.getDeclaringClass()).unreflect(method);

		map.put(FunctionType.inst(res.type()), res);

		return res;
	}

	protected MethodHandle getConstructor(Constructor<?> constructor, FunctionType argsType) throws IllegalAccessException {
		CollectionObjectMap<FunctionType, MethodHandle> map = methodPool.get(constructor.getDeclaringClass(), prov1).get("<init>", prov2);

		MethodHandle res = map.get(argsType);
		if (res != null) return res;

		for (var entry : map) {
			if (entry.key.match(argsType.getTypes())) return entry.value;
		}

		res = platformImpl.lookup(constructor.getDeclaringClass()).unreflectConstructor(constructor);

		map.put(argsType, res);

		return res;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(Method method, Object object, Object... args) {
		FunctionType type = FunctionType.from(method);
		try {
			return (T) Reflects.invokeVirtual(object, getMethod(method, type), args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invokeStatic(Method method, Object... args) {
		FunctionType type = FunctionType.from(method);
		try {
			return (T) Reflects.invokeStatic(getMethod(method, type), args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T newInstance(Constructor<T> constructor, Object... args) {
		FunctionType type = FunctionType.from(constructor);
		try {
			return (T) Reflects.invokeStatic(getConstructor(constructor, type), args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}
}
