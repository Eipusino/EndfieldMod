package endfield.util;

import arc.func.Prov;
import endfield.util.holder.ObjectHolder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class ReflectionMethodInvokeHelper implements MethodInvokeHelper {
	protected static final CollectionObjectMap<Class<?>, CollectionObjectMap<String, CollectionObjectMap<FunctionType, Method>>> methodPool = new CollectionObjectMap<>(Class.class, CollectionObjectMap.class);
	protected static final CollectionObjectMap<Class<?>, CollectionObjectMap<FunctionType, Constructor<?>>> constructorPool = new CollectionObjectMap<>(Class.class, CollectionObjectMap.class);

	protected static final CollectionObjectMap<Class<?>, Method[]> methodsMap = new CollectionObjectMap<>(Class.class, Method[].class);
	protected static final CollectionObjectMap<Class<?>, Constructor<?>[]> constructorsMap = new CollectionObjectMap<>(Class.class, Constructor[].class);

	protected static final Prov<CollectionObjectMap<String, CollectionObjectMap<FunctionType, Method>>> prov2 = () -> new CollectionObjectMap<>(String.class, CollectionObjectMap.class);
	protected static final Prov<CollectionObjectMap<FunctionType, Method>> prov3 = () -> new CollectionObjectMap<>(FunctionType.class, Method.class);
	protected static final Prov<CollectionObjectMap<FunctionType, Constructor<?>>> prov4 = () -> new CollectionObjectMap<>(FunctionType.class, Constructor.class);

	protected static final Function<Class<?>, Method[]> function2 = Class::getDeclaredMethods;
	protected static final Function<Class<?>, Constructor<?>[]> function3 = Class::getDeclaredConstructors;

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
			try {
				res = curr.getDeclaredMethod(name, types.paramType());
				res.setAccessible(true);
				map.put(FunctionType.from(res), res);
				return res;
			} catch (Throwable ignored) {}

			curr = curr.getSuperclass();
		}

		curr = clazz;

		while (curr != null) {
			for (Method method : methodsMap.computeIfAbsent(curr, function2)) {
				if (!method.getName().equals(name)) continue;

				FunctionType t;
				if ((t = FunctionType.from(method)).match(types)) {
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
	protected <T> Constructor<T> getConstructor(Class<T> clazz, FunctionType types) {
		CollectionObjectMap<FunctionType, Constructor<?>> map = constructorPool.get(clazz, prov4);

		Constructor<T> res = (Constructor<T>) map.get(types);
		if (res != null) return res;

		for (ObjectHolder<FunctionType, Constructor<?>> entry : map) {
			if (entry.key.match(types)) return (Constructor<T>) entry.value;
		}

		try {
			res = clazz.getConstructor(types.paramType());
		} catch (NoSuchMethodException ignored) {}

		if (res != null) {
			res.setAccessible(true);
			map.put(FunctionType.from(res), res);
			return res;
		}

		for (Constructor<?> constructor : constructorsMap.computeIfAbsent(clazz, function3)) {
			FunctionType functionType;
			if ((functionType = FunctionType.from(constructor)).match(types)) {
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(Object object, String name, Object... args) {
		FunctionType type = FunctionType.inst(args);
		try {
			return (T) getMethod(object.getClass(), name, type).invoke(object, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
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
			return (T) getMethod(clazz, name, type).invoke(null, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		} finally {
			type.recycle();
		}
	}

	@Override
	public <T> T newInstance(Class<T> type, Object... args) {
		FunctionType funcType = FunctionType.inst(args);
		try {
			return getConstructor(type, funcType).newInstance(args);
		} catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
			throw new RuntimeException(e);
		} finally {
			funcType.recycle();
		}
	}
}
