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

import arc.func.Cons;
import arc.func.Prov;
import endfield.Vars2;
import endfield.util.handler.ClassHandler;
import endfield.util.handler.FieldHandler;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * A utility assembly for objects.
 *
 * @author Eipusino
 * @since 1.0.8
 */
public final class Objects2 {
	private Objects2() {}

	public static <T> T let(T obj, Cons<? super T> cons) {
		cons.get(obj);
		return obj;
	}

	/**
	 * Returns the first argument if it is non-{@code null} and otherwise
	 * returns the non-{@code null} value of {@code supplier.get()}.
	 *
	 * @param obj      an object
	 * @param supplier of a non-{@code null} object to return if the first argument
	 *                 is {@code null}
	 * @param <T>      the type of the first argument and return type
	 * @return the first argument if it is non-{@code null} and otherwise
	 * the value from {@code supplier.get()} if it is non-{@code null}
	 * @throws NullPointerException if both {@code obj} is null and
	 *                              either the {@code supplier} is {@code null} or
	 *                              the {@code supplier.get()} value is {@code null}
	 * @since 1.0.8
	 */
	public static <T> T requireNonNullElseGet(T obj, Prov<? extends T> supplier) {
		return obj != null ? obj : Objects.requireNonNull(Objects.requireNonNull(supplier, "supplier").get(), "supplier.get()");
	}

	/**
	 * Checks that the specified object reference is not {@code null} and
	 * throws a customized {@link NullPointerException} if it is.
	 *
	 * <p>Unlike the method {@link Objects#requireNonNull(Object, String)},
	 * this method allows creation of the message to be deferred until
	 * after the null check is made. While this may confer a
	 * performance advantage in the non-null case, when deciding to
	 * call this method care should be taken that the costs of
	 * creating the message supplier are less than the cost of just
	 * creating the string message directly.
	 *
	 * @param obj             the object reference to check for nullity
	 * @param messageSupplier supplier of the detail message to be
	 *                        used in the event that a {@code NullPointerException} is thrown
	 * @param <T>             the type of the reference
	 * @return {@code obj} if not {@code null}
	 * @throws NullPointerException if {@code obj} is {@code null}
	 * @since 1.0.8
	 */
	public static <T> T requireNonNull(T obj, Prov<String> messageSupplier) {
		if (obj == null)
			throw new NullPointerException(messageSupplier == null ?
					null : messageSupplier.get());
		return obj;
	}

	@SuppressWarnings("unchecked")
	public static <T, E extends Throwable> T thrower(Throwable t) throws E {
		throw (E) t;
	}

	public static String toString(Object object) {
		return toString(object, true, true);
	}

	/**
	 * Returns a string reporting the value of each declared field, via reflection.
	 * <p>Static fields are automatically skipped. Produces output like:
	 * <p>{@code "SimpleClassName{integer=1234, string=hello, character=c, intArray=[1, 2, 3], object=java.lang.Object@1234abcd, none=null}"}.
	 * <p>If there is an exception in obtaining the value of a certain field, it will result in:
	 * <p>{@code "SimpleClassName{unknown=???}"}.
	 *
	 * @param parent Should the fields of the super class be retrieved.
	 * @param deep   If true, convert the array contents to a String. Otherwise, directly convert the array
	 *               object to a String.
	 */
	public static String toString(Object object, final boolean parent, final boolean deep) {
		if (object == null) return "null";

		Class<?> type = object.getClass();
		String name = type.getSimpleName();

		if (type.isArray()) return name + "{length=" + Array.getLength(object) + '}';

		StringBuilder buf = new StringBuilder();
		buf.append(name).append('{');
		int i = 0;
		while (type != Object.class) {
			for (Field field : ClassHandler.getFields(type)) {
				if ((field.getModifiers() & Modifier.STATIC) != 0) continue;

				if (i++ > 0) buf.append(", ");

				buf.append(field.getName()).append('=');

				try {
					Object value = FieldHandler.get(object, field);

					if (value == null) {
						buf.append("null");
					} else if (deep && value.getClass().isArray()) {
						// I think using instanceof would be better.
						if (value instanceof float[] floats) {
							Arrays2.floatToString(buf, floats);
						} else if (value instanceof int[] ints) {
							Arrays2.intToString(buf, ints);
						} else if (value instanceof boolean[] booleans) {
							Arrays2.booleanToString(buf, booleans);
						} else if (value instanceof byte[] bytes) {
							Arrays2.byteToString(buf, bytes);
						} else if (value instanceof char[] chars) {
							Arrays2.charToString(buf, chars);
						} else if (value instanceof double[] doubles) {
							Arrays2.doubleToString(buf, doubles);
						} else if (value instanceof long[] longs) {
							Arrays2.longToString(buf, longs);
						} else if (value instanceof short[] shorts) {
							Arrays2.shortToString(buf, shorts);
						} else if (value instanceof Object[] objects) {
							Arrays2.deepToString(objects, buf, Arrays2.arraySet);
						} else {
							// It shouldn't have happened...
							buf.append("???");
						}
					} else {
						buf.append(value);
					}
				} catch (Exception e) {
					buf.append("???");
				}
			}

			if (!parent) continue;

			type = type.getSuperclass();
		}

		return buf.append('}').toString();
	}
}
