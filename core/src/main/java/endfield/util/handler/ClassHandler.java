package endfield.util.handler;

import arc.func.Boolf;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static endfield.Vars2.classHelper;

public final class ClassHandler {
	private ClassHandler() {}

	/**
	 * @since 1.0.8
	 */
	public static @Nullable Field findField(Class<?> type, String name) {
		return classHelper.findField(type, name);
	}

	/**
	 * A utility function to find a field without throwing exceptions.
	 *
	 * @return The field, or {@code null} if not found.
	 * @since 1.0.8
	 */
	public static @Nullable Field findField(Class<?> type, Boolf<Field> filler) {
		return classHelper.findField(type, filler);
	}

	/**
	 * A utility function to find a method without throwing exceptions.
	 *
	 * @return The method, or {@code null} if not found.
	 * @since 1.0.8
	 */
	public static @Nullable Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
		return classHelper.findMethod(type, name, parameterTypes);
	}

	/**
	 * A utility function to find a constructor without throwing exceptions.
	 *
	 * @return The constructor, or {@code null} if not found.
	 * @since 1.0.8
	 */
	public static <T> @Nullable Constructor<T> findConstructor(Class<T> type, Class<?>... args) {
		return classHelper.findConstructor(type, args);
	}

	public static Field getField(Class<?> type, String name) {
		return classHelper.getField(type, name);
	}

	public static Method getMethod(Class<?> type, String name, Class<?>... parameterTypes) {
		return classHelper.getMethod(type, name, parameterTypes);
	}

	public static <T> Constructor<T> getConstructor(Class<T> type, Class<?>... args) {
		return classHelper.getConstructor(type, args);
	}

	public static Field getField(Class<?> type, Boolf<Field> filler) {
		return classHelper.getField(type, filler);
	}

	@SuppressWarnings("unchecked")
	public static <T> T allocateInstance(Class<? extends T> clazz) {
		if (clazz.isArray()) return (T) Array.newInstance(clazz.getComponentType(), 0);
		if (clazz.isPrimitive()) return (T) def(clazz);

		return classHelper.allocateInstance(clazz);
	}

	static Object def(Class<?> type) {
		if (type == boolean.class) return false;
		if (type == int.class) return 0;
		if (type == float.class) return 0f;
		if (type == long.class) return 0l;
		if (type == byte.class) return (byte) 0;
		if (type == short.class) return (short) 0;
		if (type == double.class) return 0d;
		if (type == char.class) return '\u0000';
		if (type == void.class) return null;
		throw new IllegalArgumentException("unknown type " + type.getName());
	}
}
