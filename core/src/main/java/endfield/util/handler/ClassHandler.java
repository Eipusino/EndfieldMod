package endfield.util.handler;

import arc.func.Boolf;
import endfield.util.NoSuchFunctionException;
import endfield.util.NoSuchVariableException;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static endfield.Vars2.classHelper;

public final class ClassHandler {
	private ClassHandler() {}

	/**
	 * Return to search for fields in the class by name, including private ones. If not found, return {@code null}.
	 *
	 * @see Class#getDeclaredField(String)
	 */
	public static @Nullable Field findField(Class<?> type, String name) {
		return classHelper.findField(type, name);
	}

	/**
	 * Search and return the field based on custom criteria, and return null if not found.
	 */
	public static @Nullable Field findField(Class<?> type, Boolf<Field> filler) {
		return classHelper.findField(type, filler);
	}

	/**
	 * Search and return the method based on custom criteria, and return null if not found.
	 */
	public static @Nullable Method findMethod(Class<?> clazz, Boolf<Method> filler) {
		return classHelper.findMethod(clazz, filler);
	}

	/**
	 * Search and return the constructor based on custom criteria, and return null if not found.
	 */
	public static <T> @Nullable Constructor<T> findConstructor(Class<T> clazz, Boolf<Constructor<T>> filler) {
		return classHelper.findConstructor(clazz, filler);
	}

	/**
	 * Return to search for methods in the class based on name and parameter type, including private
	 * ones. If not found, return {@code null}.
	 *
	 * @see Class#getDeclaredMethod(String, Class[])
	 */
	public static @Nullable Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
		return classHelper.findMethod(type, name, parameterTypes);
	}

	/**
	 * Return the constructor function in the class based on the parameter type, including private ones. If
	 * it cannot be found, it will return {@code null}.
	 *
	 * @see Class#getDeclaredConstructor(Class[])
	 */
	public static <T> @Nullable Constructor<T> findConstructor(Class<T> type, Class<?>... args) {
		return classHelper.findConstructor(type, args);
	}

	/**
	 * Return to search for fields in the class by name, including private ones. If it cannot be found, a
	 * {@code RuntimeException} will be thrown.
	 *
	 * @throws NoSuchVariableException If no field can be found
	 * @see Class#getDeclaredField(String)
	 */
	public static Field getField(Class<?> type, String name) {
		return classHelper.getField(type, name);
	}

	/**
	 * Return to search for methods in the class based on name and parameter type, including private
	 * ones. If it cannot be found, a {@code RuntimeException} will be thrown.
	 *
	 * @throws NoSuchFunctionException If no method can be found
	 * @see Class#getDeclaredMethod(String, Class[])
	 */
	public static Method getMethod(Class<?> type, String name, Class<?>... parameterTypes) {
		return classHelper.getMethod(type, name, parameterTypes);
	}

	/**
	 * Return the constructor function in the class based on the parameter type, including private ones. If
	 * it cannot be found, a {@code RuntimeException} will be thrown.
	 *
	 * @throws NoSuchFunctionException If no constructor can be found
	 * @see Class#getDeclaredConstructor(Class[])
	 */
	public static <T> Constructor<T> getConstructor(Class<T> type, Class<?>... args) {
		return classHelper.getConstructor(type, args);
	}

	public static Field getField(Class<?> type, Boolf<Field> filler) {
		return classHelper.getField(type, filler);
	}

	public static Field[] getFields(Class<?> type) {
		return classHelper.getFields(type);
	}

	public static Method[] getMethods(Class<?> type) {
		return classHelper.getMethods(type);
	}

	public static <T> Constructor<T>[] getConstructors(Class<T> type) {
		return classHelper.getConstructors(type);
	}

	/**
	 * Create an instance object of a class directly by bypassing the constructor, where all field values
	 * within the object are in an uninitialized state. Cannot support primitive classes, abstract classes, and
	 * interfaces.
	 * <p><strong>If {@code null} is passed in, it will cause the JVM to crash.</strong>
	 */
	@SuppressWarnings("unchecked")
	public static <T> T allocateInstance(Class<? extends T> clazz) {
		Object result;

		if (clazz.isArray()) result = Array.newInstance(clazz.getComponentType(), 0);
		else if (clazz.isPrimitive()) {
			if (clazz == boolean.class) result = false;
			else if (clazz == int.class) result = 0;
			else if (clazz == float.class) result = 0f;
			else if (clazz == long.class) result = 0l;
			else if (clazz == byte.class) result = (byte) 0;
			else if (clazz == short.class) result = (short) 0;
			else if (clazz == double.class) result = 0d;
			else if (clazz == char.class) result = '\u0000';
			else if (clazz == void.class) result = null;
			else throw new IllegalArgumentException("unknown primitive type: " + clazz.getName());
		} else if (clazz == String.class) result = "";
		else result = classHelper.allocateInstance(clazz);

		return (T) result;
	}

	/**
	 * Create a class using a class file in the form of a given byte array. If it does not comply with the JVM's
	 * class specifications, an exception will be thrown.
	 */
	public static Class<?> defineClass(String name, byte[] bytes, ClassLoader loader) {
		return classHelper.defineClass(name, bytes, loader);
	}
}
