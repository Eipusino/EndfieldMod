/*
	Copyright (c) Eipusino 2021
	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package endfield.util;

import arc.func.Boolf;
import arc.func.Prov;
import arc.util.Structs;
import mindustry.Vars;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static endfield.Vars2.accessibleHelper;
import static endfield.Vars2.classHelper;
import static endfield.Vars2.platformImpl;

/**
 * Reflection utilities, mainly for wrapping reflective operations to eradicate checked exceptions.
 *
 * @author Eipusino
 * @since 1.0.6
 */
public final class Reflects {
	public static final Lookup publicLookup = MethodHandles.publicLookup();

	/** Don't let anyone instantiate this class. */
	private Reflects() {}

	public static String def(Class<?> type) {
		// boolean
		if (type == boolean.class || type == Boolean.class) return "false";
		// integer
		if (type == byte.class || type == Byte.class ||
				type == short.class || type == Short.class ||
				type == int.class || type == Integer.class ||
				type == long.class || type == Long.class ||
				type == char.class || type == Character.class) return "0";
		// float
		if (type == float.class || type == Float.class ||
				type == double.class || type == Double.class) return "0.0";
		// reference or void
		return "null";
	}

	public static <T> Prov<T> supply(Class<T> type, String name, Class<?>[] parameterTypes, T object, Object... args) {
		Method method = classHelper.getMethod(type, name, parameterTypes);
		MethodAccessor accessor = platformImpl.methodAccessor(method);

		if (!match(parameterTypes, args))
			throw new IllegalArgumentException(Arrays.toString(toTypes(args)) + " cannot be assigned to " + Arrays.toString(parameterTypes));

		return () -> accessor.invoke(object, args);
	}

	/**
	 * Reflectively instantiates a type without throwing exceptions.
	 *
	 * @throws RuntimeException Any exception that occurs in reflection.
	 */
	public static <T> Prov<T> supply(Class<T> type, Class<?>[] parameterTypes, Object... args) {
		Constructor<T> constructor = classHelper.getConstructor(type, parameterTypes);
		ConstructorAccessor<T> accessor = platformImpl.constructorAccessor(constructor);

		if (!match(parameterTypes, args))
			throw new IllegalArgumentException(Arrays.toString(toTypes(args)) + " cannot be assigned to " + Arrays.toString(parameterTypes));

		return () -> accessor.newInstance(args);
	}

	/**
	 * Finds class with the specified name using Mindustry's mod class loader.
	 *
	 * @param name The class' binary name, as per {@link Class#getName()}.
	 * @return The class, or {@code null} if not found.
	 */
	@SuppressWarnings("unchecked")
	public static <T> @UnknownNullability Class<T> findClass(String name) {
		try {
			return name == null ? null : (Class<T>) Class.forName(name, false, Vars.mods.mainLoader());
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

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

	public static String methodToString(Class<?> type, String name, Class<?>... argTypes) {
		StringBuilder buf = new StringBuilder();
		buf.append(type.getName()).append('.').append(name);

		if (argTypes == null || argTypes.length == 0) return buf.append("()").toString();

		int max = argTypes.length;

		buf.append('(');
		int i = 0;
		while (true) {
			buf.append(argTypes[i++].getName());
			if (i == max) return buf.append(')').toString();
			buf.append(',');
		}
		// The approach of Java. But I don't like using Stream here.
		/*return type.getName() + '.' + name +
				((argTypes == null || argTypes.length == 0) ?
						"()" :
						Arrays.stream(argTypes)
						.map(c -> c == null ? "null" : c.getName())
						.collect(Collectors.joining(",", "(", ")")));*/
	}

	public static boolean isInstanceButNotSubclass(Object obj, Class<?> type) {
		if (type.isInstance(obj)) {
			Class<?> cur = obj.getClass().getSuperclass();
			while (cur != null) {
				Class<?>[] interfaces = cur.getInterfaces();
				if (Structs.contains(interfaces, type)) return false;

				cur = cur.getSuperclass();
			}

			return true;
		}

		return false;
	}

	public static Set<Class<?>> getClassSubclassHierarchy(Class<?> clazz) {
		Class<?> curr = clazz.getSuperclass();
		CollectionObjectSet<Class<?>> hierarchy = new CollectionObjectSet<>(Class.class);
		while (curr != null) {
			hierarchy.add(curr);
			Class<?>[] interfaces = curr.getInterfaces();
			hierarchy.addAll(interfaces);

			curr = curr.getSuperclass();
		}
		return hierarchy;
	}

	public static boolean isAssignable(Field sourceType, Field targetType) {
		return sourceType != null && targetType != null && targetType.getType().isAssignableFrom(sourceType.getType());
	}

	/**
	 * Compare two parameter types and check if they can be assigned to another parameter, without
	 * considering the primitive type and its corresponding wrapper class.
	 *
	 * @param sourceTypes Source parameter type
	 * @param targetTypes Parameter type to be assigned
	 * @throws NullPointerException If the parameter array is {@code null} and the elements in the array are
	 *                              {@code null}, This is normal and should not happen
	 * @since 1.0.9
	 */
	public static boolean isAssignable(Class<?>[] sourceTypes, Class<?>[] targetTypes) {
		if (sourceTypes.length != targetTypes.length) return false;

		for (int i = 0; i < sourceTypes.length; i++) {
			if (sourceTypes[i] != targetTypes[i] && !targetTypes[i].isAssignableFrom(sourceTypes[i])) return false;
		}

		return true;
	}

	/**
	 * This type of data has frequent calls, small data volume, and unnecessary performance costs when
	 * using stream processing. Using for traversal instead of stream processing.
	 */
	public static Class<?>[] wrapper(Class<?>[] clazz) {
		for (int i = 0; i < clazz.length; i++) {
			clazz[i] = wrapper(clazz[i]);
		}
		return clazz;
	}

	public static Class<?>[] unwrapped(Class<?>[] clazz) {
		for (int i = 0; i < clazz.length; i++) {
			clazz[i] = unwrapped(clazz[i]);
		}
		return clazz;
	}

	public static Class<?> wrapper(Class<?> clazz) {
		if (clazz == int.class) return Integer.class;
		if (clazz == float.class) return Float.class;
		if (clazz == long.class) return Long.class;
		if (clazz == double.class) return Double.class;
		if (clazz == byte.class) return Byte.class;
		if (clazz == short.class) return Short.class;
		if (clazz == boolean.class) return Boolean.class;
		if (clazz == char.class) return Character.class;
		if (clazz == void.class) return Void.class;
		return clazz;
	}

	public static Class<?> unwrapped(Class<?> clazz) {
		if (clazz == Integer.class) return int.class;
		if (clazz == Float.class) return float.class;
		if (clazz == Long.class) return long.class;
		if (clazz == Double.class) return double.class;
		if (clazz == Byte.class) return byte.class;
		if (clazz == Short.class) return short.class;
		if (clazz == Boolean.class) return boolean.class;
		if (clazz == Character.class) return char.class;
		if (clazz == Void.class) return void.class;
		return clazz;
	}

	public static Class<?>[] toTypes(Object... args) {
		if (args == null) return Constant.EMPTY_CLASS;

		Class<?>[] types = new Class[args.length];

		for (int i = 0; i < types.length; i++) {
			Object object = args[i];
			types[i] = object == null ? void.class : object.getClass();
		}

		return types;
	}

	public static Class<?>[] toTypes(List<?> args) {
		if (args == null) return Constant.EMPTY_CLASS;

		Class<?>[] types = new Class[args.size()];

		for (int i = 0; i < types.length; i++) {
			Object object = args.get(i);
			types[i] = object == null ? void.class : object.getClass();
		}

		return types;
	}

	public static int typeNameHash(Class<?>[] types) {
		int result = 1;

		for (Class<?> type : types) {
			result = 31 * result + type.getName().hashCode();
		}

		return result;
		//return Arrays.hashCode(Arrays.stream(types).map(Class::getName).toArray());
	}

	public static boolean match(Class<?>[] sourceTypes, Object... args) {
		return match(sourceTypes, unwrapped(toTypes(args)));
	}

	/**
	 * This method is compatible with primitive types and their corresponding wrapper classes, but the
	 * performance overhead may be slightly higher.
	 *
	 * @param sourceTypes Source parameter type
	 * @param targetTypes Parameter type to be assigned
	 * @throws NullPointerException If the array or any of its elements is null
	 * @since 1.0.9
	 */
	public static boolean match(Class<?>[] sourceTypes, Class<?>[] targetTypes) {
		if (targetTypes.length != sourceTypes.length) return false;

		for (int i = 0; i < sourceTypes.length; i++) {
			Class<?> sourceType = sourceTypes[i];
			Class<?> targetType = targetTypes[i];

			if (targetType == void.class) continue;
			if (!sourceType.isAssignableFrom(targetType)) return false;
		}

		return true;
	}

	/**
	 * Set the {@code accessible} flag of the reflection object to {@code true}.
	 *
	 * @return Has it been successfully set as accessible
	 * @since 1.0.9
	 */
	@SuppressWarnings("deprecation")
	public static <T extends AccessibleObject & Member> boolean setAccessible(T object) {
		if (object.isAccessible()) return true;

		if (object instanceof Constructor<?>) {
			Class<?> dec = object.getDeclaringClass();
			if (dec == Class.class || dec == Field.class || dec == Method.class || dec == Constructor.class)
				return false;
		}

		try {
			accessibleHelper.makeAccessible(object);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	// Only on Android.
	public static <T> Class<T> setClassAccessible(Class<T> clazz) {
		int modifiers = clazz.getModifiers();
		if ((modifiers & Modifier.PUBLIC) == 0 || (modifiers & Modifier.FINAL) != 0) {
			accessibleHelper.makeClassAccessible(clazz);
		}

		return clazz;
	}

	/**
	 * Call {@code MethodHandle.invoke(Object...)} using a parameter array.
	 * <p>The constructor also uses this method.
	 *
	 * @since 1.0.9
	 */
	public static Object invokeStatic(MethodHandle handle, Object... args) throws Throwable {
		return switch (args.length) {
			case 0 -> handle.invoke();
			case 1 -> handle.invoke(args[0]);
			case 2 -> handle.invoke(args[0], args[1]);
			case 3 -> handle.invoke(args[0], args[1], args[2]);
			case 4 -> handle.invoke(args[0], args[1], args[2], args[3]);
			case 5 -> handle.invoke(args[0], args[1], args[2], args[3], args[4]);
			case 6 -> handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5]);
			case 7 -> handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
			case 8 -> handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
			case 9 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
			case 10 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9]);
			case 11 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10]);
			case 12 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11]);
			case 13 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12]);
			case 14 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13]);
			case 15 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14]);
			case 16 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15]);
			case 17 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16]);
			case 18 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17]);
			case 19 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18]);
			case 20 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19]);
			case 21 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20]);
			case 22 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21]);
			case 23 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22]);
			case 24 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22], args[23]);
			case 25 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22], args[23], args[24]);
			case 26 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22], args[23], args[24], args[25]);
			case 27 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26]);
			case 28 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26], args[27]);
			case 29 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26], args[27], args[28]);
			case 30 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26], args[27], args[28],
							args[29]);
			case 31 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26], args[27], args[28],
							args[29], args[30]);
			case 32 ->
					handle.invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26], args[27], args[28],
							args[29], args[30], args[31]);
			default -> handle.invokeWithArguments(args);
		};
	}

	/**
	 * Call {@code MethodHandle.invoke(Object...)} using a parameter array.
	 *
	 * @since 1.0.9
	 */
	public static Object invokeVirtual(Object object, MethodHandle handle, Object... args) throws Throwable {
		return switch (args.length) {
			case 0 -> handle.invoke(object);
			case 1 -> handle.invoke(object, args[0]);
			case 2 -> handle.invoke(object, args[0], args[1]);
			case 3 -> handle.invoke(object, args[0], args[1], args[2]);
			case 4 -> handle.invoke(object, args[0], args[1], args[2], args[3]);
			case 5 -> handle.invoke(object, args[0], args[1], args[2], args[3], args[4]);
			case 6 -> handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5]);
			case 7 -> handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
			case 8 -> handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
			case 9 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
			case 10 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9]);
			case 11 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10]);
			case 12 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11]);
			case 13 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12]);
			case 14 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13]);
			case 15 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14]);
			case 16 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15]);
			case 17 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16]);
			case 18 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17]);
			case 19 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18]);
			case 20 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19]);
			case 21 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20]);
			case 22 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21]);
			case 23 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22]);
			case 24 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22], args[23]);
			case 25 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22], args[23], args[24]);
			case 26 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22], args[23], args[24], args[25]);
			case 27 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26]);
			case 28 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26], args[27]);
			case 29 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26], args[27], args[28]);
			case 30 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26], args[27], args[28],
							args[29]);
			case 31 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26], args[27], args[28],
							args[29], args[30]);
			case 32 ->
					handle.invoke(object, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8],
							args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18],
							args[19], args[20], args[21], args[22], args[23], args[24], args[25], args[26], args[27], args[28],
							args[29], args[30], args[31]);
			default -> handle.asSpreader(1, Object[].class, args.length).invoke(object, args);
		};
	}
}
