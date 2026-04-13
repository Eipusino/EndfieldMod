package endfield.util;

import arc.func.Boolf;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface ClassHelper {
	default @Nullable Field findField(Class<?> clazz, String name) {
		try {
			return clazz.getDeclaredField(name);
		} catch (NoSuchFieldException e) {
			return null;
		}
	}

	default @Nullable Method findMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		try {
			return clazz.getDeclaredMethod(name, parameterTypes);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	default <T> @Nullable Constructor<T> findConstructor(Class<T> clazz, Class<?>... parameterTypes) {
		try {
			return clazz.getDeclaredConstructor(parameterTypes);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	default Field getField(Class<?> clazz, String name) {
		try {
			return clazz.getDeclaredField(name);
		} catch (NoSuchFieldException e) {
			throw new NoSuchVariableException(e);
		}
	}

	default Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		try {
			return clazz.getDeclaredMethod(name, parameterTypes);
		} catch (NoSuchMethodException e) {
			throw new NoSuchFunctionException(e);
		}
	}

	default <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
		try {
			return clazz.getDeclaredConstructor(parameterTypes);
		} catch (NoSuchMethodException e) {
			throw new NoSuchFunctionException(e);
		}
	}

	default Field[] getFields(Class<?> clazz) {
		return clazz.getDeclaredFields();
	}

	default Method[] getMethods(Class<?> clazz) {
		return clazz.getDeclaredMethods();
	}

	@SuppressWarnings("unchecked")
	default <T> Constructor<T>[] getConstructors(Class<T> clazz) {
		return (Constructor<T>[]) clazz.getDeclaredConstructors();
	}

	default @Nullable Field findField(Class<?> clazz, Boolf<Field> filler) {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (filler.get(field)) return field;
		}
		return null;
	}

	default @Nullable Method findMethod(Class<?> clazz, Boolf<Method> filler) {
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			if (filler.get(method)) return method;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	default <T> @Nullable Constructor<T> findConstructor(Class<T> clazz, Boolf<Constructor<T>> filler) {
		Constructor<T>[] constructors = (Constructor<T>[]) clazz.getDeclaredConstructors();
		for (Constructor<T> constructor : constructors) {
			if (filler.get(constructor)) return constructor;
		}
		return null;
	}

	default Field getField(Class<?> clazz, Boolf<Field> filler) {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (filler.get(field)) return field;
		}
		throw new NoSuchVariableException("Field not found");
	}

	default Method getMethod(Class<?> clazz, Boolf<Method> filler) {
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			if (filler.get(method)) return method;
		}
		throw new NoSuchFunctionException("Method not found");
	}

	@SuppressWarnings("unchecked")
	default <T> Constructor<T> getConstructor(Class<T> clazz, Boolf<Constructor<T>> filler) {
		Constructor<T>[] constructors = (Constructor<T>[]) clazz.getDeclaredConstructors();
		for (Constructor<T> constructor : constructors) {
			if (filler.get(constructor)) return constructor;
		}
		throw new NoSuchFunctionException("Constructor not found");
	}

	<T> T allocateInstance(Class<? extends T> clazz);

	Class<?> defineClass(String name, byte[] bytes, ClassLoader loader);
}
