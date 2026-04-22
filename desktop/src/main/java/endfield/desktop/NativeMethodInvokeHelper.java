package endfield.desktop;

import endfield.util.CollectionObjectMap;
import endfield.util.FunctionType;
import endfield.util.NoSuchFunctionException;
import endfield.util.ReflectionMethodInvokeHelper;
import endfield.util.holder.ObjectHolder;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

import static endfield.desktop.DesktopClassHelper.ctypes;
import static endfield.desktop.DesktopClassHelper.function5;
import static endfield.desktop.DesktopClassHelper.function6;
import static endfield.desktop.DesktopClassHelper.mtypes;
import static endfield.desktop.MethodHandleMethodInvokeHelper.from;

public class NativeMethodInvokeHelper extends ReflectionMethodInvokeHelper {
	@Override
	protected Method getMethod(Class<?> clazz, String name, FunctionType types) {
		CollectionObjectMap<FunctionType, Method> map = methodPool.get(clazz, prov2).get(name, prov3);

		FunctionType type = FunctionType.inst(types);
		Method res = map.get(type);

		if (res != null) return res;

		for (ObjectHolder<FunctionType, Method> entry : map) {
			if (entry.key.match(types)) return entry.value;
		}

		Class<?> curr = clazz;

		while (curr != null) {
			res = findMethod(curr, name, types.paramType());
			if (res != null) {
				res.setAccessible(true);
				map.put(from(res), res);
				return res;
			}

			curr = curr.getSuperclass();
		}

		curr = clazz;

		while (curr != null) {
			for (Method method : methodsMap.computeIfAbsent(curr, function2)) {
				if (!method.getName().equals(name)) continue;

				FunctionType t;
				if ((t = from(method)).match(types)) {
					method.setAccessible(true);
					res = method;
					map.put(t, res);
					return res;
				}
				t.recycle();
			}

			curr = curr.getSuperclass();
		}

		throw new NoSuchFunctionException("no such method " + name + " in class: " + clazz + " with assignable parameter: " + types);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> Constructor<T> getConstructor(Class<T> clazz, FunctionType types) {
		CollectionObjectMap<FunctionType, Constructor<?>> map = constructorPool.get(clazz, prov4);

		Constructor<T> res = (Constructor<T>) map.get(types);
		if (res != null) return res;

		for (ObjectHolder<FunctionType, Constructor<?>> entry : map) {
			if (entry.key.match(types)) return (Constructor<T>) entry.value;
		}

		res = (Constructor<T>) findConstructor(clazz, types.paramType());
		if (res != null) {
			res.setAccessible(true);
			map.put(from(res), res);
			return res;
		}

		for (Constructor<?> constructor : constructorsMap.computeIfAbsent(clazz, function3)) {
			FunctionType functionType;
			if ((functionType = from(constructor)).match(types)) {
				map.put(functionType, constructor);
				res = (Constructor<T>) constructor;
				res.setAccessible(true);

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

	@Override
	public <T> T invoke(Object object, String name, Object... args) {
		FunctionType type = FunctionType.inst(args);
		try {
			return NativeMethodAccessor.invoke(getMethod(object.getClass(), name, type), object, args);
		} catch (RuntimeException | Error e) {
			throw e;
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
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	@Override
	public <T> T newInstance(Class<T> clazz, Object... args) {
		FunctionType funcType = FunctionType.inst(args);
		try {
			return NativeConstructorAccessor.newInstance(getConstructor(clazz, funcType), args);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			funcType.recycle();
		}
	}

	@Override
	public <T> T invokeTyped(Object object, String name, Class<?>[] parameterTypes, Object... args) {
		FunctionType type = FunctionType.inst(parameterTypes);
		try {
			return NativeMethodAccessor.invoke(getMethod(object.getClass(), name, type), object, args);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	@Override
	public <T> T invokeStaticTyped(Class<?> clazz, String name, Class<?>[] parameterTypes, Object... args) {
		FunctionType type = FunctionType.inst(parameterTypes);
		try {
			return NativeMethodAccessor.invoke(getMethod(clazz, name, type), null, args);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	@Override
	public <T> T newInstanceTyped(Class<T> clazz, Class<?>[] parameterTypes, Object... args) {
		FunctionType funcType = FunctionType.inst(parameterTypes);
		try {
			return NativeConstructorAccessor.newInstance(getConstructor(clazz, funcType), args);
		} catch (RuntimeException | Error e) {
			throw e;
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
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> T invokeStatic(Method method, Object... args) {
		try {
			return NativeMethodAccessor.invoke(method, null, args);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> T newInstance(Constructor<T> constructor, Object... args) {
		try {
			return NativeConstructorAccessor.newInstance(constructor, args);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
