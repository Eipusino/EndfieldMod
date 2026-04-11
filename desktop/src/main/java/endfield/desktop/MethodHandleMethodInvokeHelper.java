package endfield.desktop;

import arc.func.Prov;
import endfield.util.CollectionObjectMap;
import endfield.util.FunctionType;
import endfield.util.MethodInvokeHelper;
import endfield.util.Reflects;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static endfield.Vars2.classHelper;
import static endfield.desktop.DesktopClassHelper.ctypes;
import static endfield.desktop.DesktopClassHelper.mtypes;
import static endfield.desktop.DesktopClassHelper.ptypes;
import static endfield.desktop.DesktopImpl.lookup;

public class MethodHandleMethodInvokeHelper implements MethodInvokeHelper {
	protected static final CollectionObjectMap<Class<?>, CollectionObjectMap<String, CollectionObjectMap<FunctionType, MethodHandle>>> methodPool = new CollectionObjectMap<>(Class.class, CollectionObjectMap.class);

	protected static final Prov<CollectionObjectMap<String, CollectionObjectMap<FunctionType, MethodHandle>>> prov1 = () -> new CollectionObjectMap<>(String.class, CollectionObjectMap.class);
	protected static final Prov<CollectionObjectMap<FunctionType, MethodHandle>> prov2 = () -> new CollectionObjectMap<>(FunctionType.class, MethodHandle.class);

	protected MethodHandle getMethod(Class<?> clazz, String name, FunctionType types) throws IllegalAccessException {
		CollectionObjectMap<FunctionType, MethodHandle> map = methodPool.get(clazz, prov1).get(name, prov2);

		FunctionType type = FunctionType.inst(types);
		MethodHandle res = map.get(type);

		if (res != null) return res;

		for (var entry : map) {
			if (entry.key.match(types)) return entry.value;
		}

		Class<?> curr = clazz;

		while (curr != null) {
			Method method = classHelper.findMethod(curr, name, types.paramType());

			if (method != null) {
				res = lookup.unreflect(method);
				map.put(inst(res.type()), res);
				return res;
			}

			curr = curr.getSuperclass();
		}

		curr = clazz;

		while (curr != null) {
			for (Method method : classHelper.getMethods(curr)) {
				if (!method.getName().equals(name)) continue;

				FunctionType t;
				if ((t = from(method)).match(types)) {
					res = lookup.unreflect(method);
					map.put(t, res);
					return res;
				}
				t.recycle();
			}

			curr = curr.getSuperclass();
		}

		throw new RuntimeException("no such method " + name + " in class: " + clazz + " with assignable parameter: " + types);
	}

	protected MethodHandle getConstructor(Class<?> clazz, FunctionType types) throws IllegalAccessException, NoSuchMethodException {
		CollectionObjectMap<FunctionType, MethodHandle> map = methodPool.get(clazz, prov1).get("<init>", prov2);

		MethodHandle res = map.get(types);
		if (res != null) return res;

		for (var entry : map) {
			if (entry.key.match(types)) return entry.value;
		}

		Constructor<?> cons = classHelper.findConstructor(clazz, types.paramType());
		if (cons != null) {
			res = lookup.unreflectConstructor(cons);
			map.put(from(cons), res);
		}

		if (res != null) return res;

		for (Constructor<?> constructor : classHelper.getConstructors(clazz)) {
			FunctionType functionType;
			if ((functionType = from(constructor)).match(types)) {
				res = lookup.unreflectConstructor(constructor);
				map.put(functionType, res);

				break;
			}
			functionType.recycle();
		}

		if (res != null) return res;

		throw new NoSuchMethodException("no such constructor in class: " + clazz + " with assignable parameter: " + types);
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

	protected MethodHandle getMethod(Method method, FunctionType types) throws IllegalAccessException {
		CollectionObjectMap<FunctionType, MethodHandle> map = methodPool.get(method.getDeclaringClass(), prov1).get(method.getName(), prov2);

		FunctionType type = FunctionType.inst(types);
		MethodHandle res = map.get(type);

		if (res != null) return res;

		for (var entry : map) {
			if (entry.key.match(types)) return entry.value;
		}

		res = lookup.unreflect(method);

		map.put(inst(res.type()), res);

		return res;
	}

	protected MethodHandle getConstructor(Constructor<?> constructor, FunctionType types) throws IllegalAccessException {
		CollectionObjectMap<FunctionType, MethodHandle> map = methodPool.get(constructor.getDeclaringClass(), prov1).get("<init>", prov2);

		MethodHandle res = map.get(types);
		if (res != null) return res;

		for (var entry : map) {
			if (entry.key.match(types)) return entry.value;
		}

		res = lookup.unreflectConstructor(constructor);

		map.put(types, res);

		return res;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(Method method, Object object, Object... args) {
		FunctionType type = from(method);
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
		FunctionType type = from(method);
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
		FunctionType type = from(constructor);
		try {
			return (T) Reflects.invokeStatic(getConstructor(constructor, type), args);
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
