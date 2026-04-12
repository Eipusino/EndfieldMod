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

import arc.func.Prov;
import arc.util.Structs;
import endfield.util.handler.ClassHandler;
import mindustry.Vars;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
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

	public static Object def(Class<?> type) {
		if (type == boolean.class || type == Boolean.class) return false;
		if (type == int.class || type == Integer.class) return 0;
		if (type == float.class || type == Float.class) return 0f;
		if (type == long.class || type == Long.class) return 0l;
		if (type == byte.class || type == Byte.class) return (byte) 0;
		if (type == short.class || type == Short.class) return (short) 0;
		if (type == double.class || type == Double.class) return 0d;
		if (type == char.class || type == Character.class) return '\u0000';
		//if (type == void.class || type == Void.class) return null;
		return null;
	}

	public static Object def(String name) {
		return switch (name) {
			case "boolean", "java.lang.Boolean", "Z" -> false;
			case "int", "java.lang.Integer", "I" -> 0;
			case "float", "java.lang.Float", "F" -> 0f;
			case "long", "java.lang.Long", "J" -> 0l;
			case "byte", "java.lang.Byte", "B" -> (byte) 0;
			case "short", "java.lang.Short", "S" -> (short) 0;
			case "double", "java.lang.Double", "D" -> 0d;
			case "char", "java.lang.Character", "C" -> '\u0000';
			//case "void", "java.lang.Void", "V" -> null;
			default -> null;
		};
	}

	public static FieldAccessor newFieldAccessor(Field field) {
		return platformImpl.fieldAccessor(field);
	}

	public static MethodAccessor newMethodAccessor(Method method) {
		return platformImpl.methodAccessor(method);
	}

	public static <T> ConstructorAccessor<T> newConstructorAccessor(Constructor<T> constructor) {
		return platformImpl.constructorAccessor(constructor);
	}

	public static <T> Prov<T> supply(Class<T> type, String name, Class<?>[] parameterTypes, T object, Object... args) {
		Method method = ClassHandler.getMethod(type, name, parameterTypes);
		MethodAccessor accessor = newMethodAccessor(method);

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
		Constructor<T> constructor = ClassHandler.getConstructor(type, parameterTypes);
		ConstructorAccessor<T> accessor = newConstructorAccessor(constructor);

		if (!match(parameterTypes, args))
			throw new IllegalArgumentException(Arrays.toString(toTypes(args)) + " cannot be assigned to " + Arrays.toString(parameterTypes));

		return () -> accessor.newInstance(args);
	}

	public static VarHandle findVarHandle(Class<?> recv, String name, Class<?> type) {
		try {
			return platformImpl.lookup(recv).findVarHandle(recv, name, type);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
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
}
