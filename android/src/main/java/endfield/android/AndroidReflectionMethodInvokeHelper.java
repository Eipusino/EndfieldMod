package endfield.android;

import dynamilize.FunctionType;
import endfield.util.CollectionObjectMap;
import endfield.util.ReflectionMethodInvokeHelper;
import endfield.util.holder.ObjectHolder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class AndroidReflectionMethodInvokeHelper extends ReflectionMethodInvokeHelper {
	protected Method getMethod(Class<?> clazz, String name, FunctionType argTypes) throws NoSuchMethodException {
		CollectionObjectMap<FunctionType, Method> map = methodPool.get(clazz, prov1).get(name, prov2);

		FunctionType type = FunctionType.inst(argTypes);
		Method res = map.get(type);

		if (res != null) return res;

		for (ObjectHolder<FunctionType, Method> entry : map) {
			if (entry.key.match(argTypes.getTypes())) return entry.value;
		}

		Class<?> curr = clazz;

		while (curr != null) {
			try {
				res = curr.getDeclaredMethod(name, argTypes.getTypes());
				res.setAccessible(true);
				map.put(FunctionType.from(res), res);
			} catch (Throwable ignored) {}

			if (res != null) break;

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
					res = method;
					map.put(t, res);
					break a;
				}
				t.recycle();
			}

			curr = curr.getSuperclass();
		}

		if (res == null)
			throw new NoSuchMethodException("no such method " + name + " in class: " + clazz + " with assignable parameter: " + argTypes);

		return res;
	}

	@SuppressWarnings("unchecked")
	protected <T> Constructor<T> getConstructor(Class<T> clazz, FunctionType argsType) throws NoSuchMethodException {
		CollectionObjectMap<FunctionType, Constructor<?>> map = constructorMap.get(clazz, prov3);

		Constructor<T> res = (Constructor<T>) map.get(argsType);
		if (res != null) return res;

		for (ObjectHolder<FunctionType, Constructor<?>> entry : map) {
			if (entry.key.match(argsType.getTypes())) return (Constructor<T>) entry.value;
		}

		try {
			res = clazz.getConstructor(argsType.getTypes());
			res.setAccessible(true);
			map.put(FunctionType.from(res), res);
		} catch (NoSuchMethodException ignored) {}

		if (res != null) return res;

		for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			FunctionType functionType;
			if ((functionType = FunctionType.from(constructor)).match(argsType.getTypes())) {
				map.put(functionType, constructor);
				res = (Constructor<T>) constructor;
				res.setAccessible(true);

				break;
			}
			functionType.recycle();
		}

		if (res != null) return res;

		throw new NoSuchMethodException("no such constructor in class: " + clazz + " with assignable parameter: " + argsType);
	}
}
