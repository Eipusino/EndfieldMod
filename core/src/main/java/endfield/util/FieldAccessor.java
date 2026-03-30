package endfield.util;

import java.lang.reflect.Field;

/**
 * Create a FieldAccessor using the {@link PlatformImpl#fieldAccessor} method.
 * <p>All checked exceptions will be wrapped in a {@link RuntimeException} and do not need to be manually thrown.
 *
 * @since 1.0.9
 */
public interface FieldAccessor {
	/**
	 * Returns the value of the field represented by this {@code Field}.
	 *
	 * @param <T>    Return type, no need to manually cast, but be aware of {@code ClassCastException}
	 * @param object object from which the represented field's value is to be extracted
	 * @return the value of the represented field in object obj
	 */
	<T> T get(Object object);

	/**
	 * Sets the field represented by this {@code Field} object on the specified object argument to the specified
	 * new value.
	 *
	 * @param object the object whose field should be modified
	 * @param value the new value for the field of {@code object} being modified
	 */
	void set(Object object, Object value);

	/**
	 * Gets the value of a static or instance object field.
	 * <p>Primitive fields are not supported.
	 *
	 * @param object the object whose field should be modified
	 */
	<T> T getObject(Object object);

	void setObject(Object object, Object value);

	/**
	 * Gets the value of a static or instance {@code boolean} field.
	 *
	 * @param object the object to extract the {@code boolean} value from
	 * @return the value of the {@code boolean} field
	 */
	boolean getBoolean(Object object);

	/**
	 * Sets the value of a field as a {@code boolean} on the specified object.
	 *
	 * @param object the object whose field should be modified
	 * @param value the new value for the field of {@code object} being modified
	 */
	void setBoolean(Object object, boolean value);

	/**
	 * Gets the value of a static or instance {@code byte} field.
	 *
	 * @param object the object to extract the {@code byte} value from
	 * @return the value of the {@code byte} field
	 */
	byte getByte(Object object);

	void setByte(Object object, byte value);

	/**
	 * Gets the value of a static or instance {@code char} field.
	 *
	 * @param object the object to extract the {@code char} value from
	 * @return the value of the {@code char} field
	 */
	char getChar(Object object);

	void setChar(Object object, char value);

	/**
	 * Gets the value of a static or instance {@code short} field.
	 *
	 * @param object the object to extract the {@code short} value from
	 * @return the value of the {@code short} field
	 */
	short getShort(Object object);

	void setShort(Object object, short value);

	/**
	 * Gets the value of a static or instance {@code int} field.
	 *
	 * @param object the object to extract the {@code int} value from
	 * @return the value of the {@code int} field
	 */
	int getInt(Object object);

	void setInt(Object object, int value);

	/**
	 * Gets the value of a static or instance {@code long} field.
	 *
	 * @param object the object to extract the {@code long} value from
	 * @return the value of the {@code long} field
	 */
	long getLong(Object object);

	void setLong(Object object, long value);

	/**
	 * Gets the value of a static or instance {@code float} field.
	 *
	 * @param object the object to extract the {@code float} value from
	 * @return the value of the {@code float} field
	 */
	float getFloat(Object object);

	void setFloat(Object object, float value);

	/**
	 * Gets the value of a static or instance {@code double} field.
	 *
	 * @param object the object to extract the {@code double} value from
	 * @return the value of the {@code double} field
	 */
	double getDouble(Object object);

	void setDouble(Object object, double value);

	/**
	 * @return The field of definition. Accessible may not be set.
	 */
	Field getField();
}
