package endfield.util.handler;

import java.lang.reflect.Field;

import static endfield.Vars2.fieldAccessHelper;

/**
 * A collection of static methods for field operations, including read, write, and other operations. All
 * checked exceptions thrown by references are caught and encapsulated in {@link RuntimeException}, without the
 * need for manual try or throw.
 *
 * @since 1.0.9
 */
public class FieldHandler<T> {
	public final Class<T> clazz;

	@Deprecated
	public FieldHandler(Class<T> c) {
		clazz = c;
	}

	/**
	 * Construct a processor object using default rules and cache it, and use this default processor to
	 * perform {@code setValue} operations.
	 *
	 * @see FieldHandler#set(Object, String, Object)
	 */
	public static void setDefault(Object obj, String key, Object value) {
		fieldAccessHelper.set(obj, key, value);
	}

	/**
	 * Construct a processor object using default rules and cache it, using this default processor to perform
	 * static {@code getValue} operations.
	 *
	 * @see FieldHandler#set(Object, String, Object)
	 */
	public static void setDefault(Class<?> clazz, String key, Object value) {
		fieldAccessHelper.setStatic(clazz, key, value);
	}

	/**
	 * Construct a processor object using default rules and cache it, and use this default processor to
	 * perform the {@code getValue} operation.
	 *
	 * @see FieldHandler#get(Object, String)
	 */
	public static <T> T getDefault(Object obj, String key) {
		return fieldAccessHelper.get(obj, key);
	}

	/**
	 * Construct a processor object using default rules and cache it, and use this default processor to
	 * perform static {@code getValue} operations.
	 *
	 * @see FieldHandler#get(Object, String)
	 */
	public static <T> T getDefault(Class<?> clazz, String key) {
		return fieldAccessHelper.getStatic(clazz, key);
	}

	public static void setObjectDefault(Object obj, String key, Object value) {
		fieldAccessHelper.setObject(obj, key, value);
	}

	public static void setObjectDefault(Class<?> clazz, String key, Object value) {
		fieldAccessHelper.setObjectStatic(clazz, key, value);
	}

	public static <T> T getObjectDefault(Object obj, String key) {
		return fieldAccessHelper.getObject(obj, key);
	}

	public static <T> T getObjectDefault(Class<?> clazz, String key) {
		return fieldAccessHelper.getObjectStatic(clazz, key);
	}

	public static void setByteDefault(Object obj, String key, byte value) {
		fieldAccessHelper.setByte(obj, key, value);
	}

	public static void setByteDefault(Class<?> clazz, String key, byte value) {
		fieldAccessHelper.setByteStatic(clazz, key, value);
	}

	public static byte getByteDefault(Object obj, String key) {
		return fieldAccessHelper.getByte(obj, key);
	}

	public static byte getByteDefault(Class<?> clazz, String key) {
		return fieldAccessHelper.getByteStatic(clazz, key);
	}

	public static void setShortDefault(Object obj, String key, short value) {
		fieldAccessHelper.setShort(obj, key, value);
	}

	public static void setShortDefault(Class<?> clazz, String key, short value) {
		fieldAccessHelper.setShortStatic(clazz, key, value);
	}

	public static short getShortDefault(Object obj, String key) {
		return fieldAccessHelper.getShort(obj, key);
	}

	public static short getShortDefault(Class<?> clazz, String key) {
		return fieldAccessHelper.getShortStatic(clazz, key);
	}

	public static void setIntDefault(Object obj, String key, int value) {
		fieldAccessHelper.setInt(obj, key, value);
	}

	public static void setIntDefault(Class<?> clazz, String key, int value) {
		fieldAccessHelper.setIntStatic(clazz, key, value);
	}

	public static int getIntDefault(Object obj, String key) {
		return fieldAccessHelper.getInt(obj, key);
	}

	public static int getIntDefault(Class<?> clazz, String key) {
		return fieldAccessHelper.getIntStatic(clazz, key);
	}

	public static void setFloatDefault(Object obj, String key, float value) {
		fieldAccessHelper.setFloat(obj, key, value);
	}

	public static void setFloatDefault(Class<?> clazz, String key, float value) {
		fieldAccessHelper.setFloatStatic(clazz, key, value);
	}

	public static float getFloatDefault(Object obj, String key) {
		return fieldAccessHelper.getFloat(obj, key);
	}

	public static float getFloatDefault(Class<?> clazz, String key) {
		return fieldAccessHelper.getFloatStatic(clazz, key);
	}

	public static void setDoubleDefault(Object obj, String key, double value) {
		fieldAccessHelper.setDouble(obj, key, value);
	}

	public static void setDoubleDefault(Class<?> clazz, String key, double value) {
		fieldAccessHelper.setDoubleStatic(clazz, key, value);
	}

	public static double getDoubleDefault(Object obj, String key) {
		return fieldAccessHelper.getDouble(obj, key);
	}

	public static double getDoubleDefault(Class<?> clazz, String key) {
		return fieldAccessHelper.getDoubleStatic(clazz, key);
	}

	public static void setBooleanDefault(Object obj, String key, boolean value) {
		fieldAccessHelper.setBoolean(obj, key, value);
	}

	public static void setBooleanDefault(Class<?> clazz, String key, boolean value) {
		fieldAccessHelper.setBooleanStatic(clazz, key, value);
	}

	public static boolean getBooleanDefault(Object obj, String key) {
		return fieldAccessHelper.getBoolean(obj, key);
	}

	public static boolean getBooleanDefault(Class<?> clazz, String key) {
		return fieldAccessHelper.getBooleanStatic(clazz, key);
	}

	/**
	 * Set the value of the field. Fields with inline values during compilation are invalid.
	 *
	 * @param object the object whose field should be modified, If the field is static, please set it to null
	 */
	public static void set(Object object, Field field, Object value) {
		if (object == null) fieldAccessHelper.setStatic(field, value);
		else fieldAccessHelper.set(object, field, value);
	}

	public static <T> T get(Object object, Field field) {
		return object == null ? fieldAccessHelper.getStatic(field) : fieldAccessHelper.get(object, field);
	}

	public static void setObject(Object object, Field field, Object value) {
		if (object == null) fieldAccessHelper.setObjectStatic(field, value);
		else fieldAccessHelper.setObject(object, field, value);
	}

	public static <T> T getObject(Object object, Field field) {
		return object == null ? fieldAccessHelper.getObjectStatic(field) : fieldAccessHelper.getObject(object, field);
	}

	public static void setBoolean(Object object, Field field, boolean value) {
		if (object == null) fieldAccessHelper.setBooleanStatic(field, value);
		else fieldAccessHelper.setBoolean(object, field, value);
	}

	public static boolean getBoolean(Object object, Field field) {
		return object == null ? fieldAccessHelper.getBooleanStatic(field) : fieldAccessHelper.getBoolean(object, field);
	}

	/**
	 * Set the value of the int field. Fields with inline values during compilation are invalid.
	 *
	 * @param field the object whose field should be modified, If the field is static, please set it to null
	 */
	public static void setInt(Object object, Field field, int value) {
		if (object == null) fieldAccessHelper.setIntStatic(field, value);
		else fieldAccessHelper.setInt(object, field, value);
	}

	public static int getInt(Object object, Field field) {
		return object == null ? fieldAccessHelper.getIntStatic(field) : fieldAccessHelper.getInt(object, field);
	}

	public static void setFloat(Object object, Field field, float value) {
		if (object == null) fieldAccessHelper.setFloatStatic(field, value);
		else fieldAccessHelper.setFloat(object, field, value);
	}

	public static float getFloat(Object object, Field field) {
		return object == null ? fieldAccessHelper.getFloatStatic(field) : fieldAccessHelper.getFloat(object, field);
	}

	/**
	 * Setting the selected property value of a specified object will ignore the access modifier and final
	 * modifier of that property. If the target object is null, the field set will be static.
	 * <p>Unless the field is static, passing an empty target object is not allowed.
	 *
	 * @param object Object to change attribute value
	 * @param key    The attribute name to be changed
	 * @param value  The value to be written
	 * @throws NullPointerException If the target object passed in is null and the field is not static
	 */
	public void set(T object, String key, Object value) {
		if (object == null) fieldAccessHelper.setStatic(clazz, key, value);
		else fieldAccessHelper.set(object, key, value);
	}

	/**
	 * Retrieve the specified field value and return it. If the field does not exist, an exception will be
	 * thrown. If the target object is null, the set field will be static.
	 * <p>Unless the field is static, passing an empty target object is not allowed.
	 *
	 * @param object If the field of the target object is not static, it is required not to be empty. If the
	 *               field is static, this parameter will be ignored.
	 * @param key    field name
	 * @return The value of the field, if it exists
	 * @throws NullPointerException If the target object passed in is null and the field is not static
	 */
	public <R> R get(T object, String key) {
		return object == null ? fieldAccessHelper.getStatic(clazz, key) : fieldAccessHelper.get(object, key);
	}

	public void setObject(T object, String key, Object value) {
		if (object == null) fieldAccessHelper.setObjectStatic(clazz, key, value);
		else fieldAccessHelper.setObject(object, key, value);
	}

	public <R> R getObject(T object, String key) {
		return object == null ? fieldAccessHelper.getObjectStatic(clazz, key) : fieldAccessHelper.getObject(object, key);
	}

	public void setByte(T object, String key, byte value) {
		if (object == null) fieldAccessHelper.setByteStatic(clazz, key, value);
		else fieldAccessHelper.setByte(object, key, value);
	}

	public byte getByte(T object, String key) {
		return object == null ? fieldAccessHelper.getByteStatic(clazz, key) : fieldAccessHelper.getByte(object, key);
	}

	public void setShort(T object, String key, short value) {
		if (object == null) fieldAccessHelper.setShortStatic(clazz, key, value);
		else fieldAccessHelper.setShort(object, key, value);
	}

	public short getShort(T object, String key) {
		return object == null ? fieldAccessHelper.getShortStatic(clazz, key) : fieldAccessHelper.getShort(object, key);
	}

	public void setInt(T object, String key, int value) {
		if (object == null) fieldAccessHelper.setIntStatic(clazz, key, value);
		else fieldAccessHelper.setInt(object, key, value);
	}

	public int getInt(T object, String key) {
		return object == null ? fieldAccessHelper.getIntStatic(clazz, key) : fieldAccessHelper.getInt(object, key);
	}

	public void setLong(T object, String key, long value) {
		if (object == null) fieldAccessHelper.setLongStatic(clazz, key, value);
		else fieldAccessHelper.setLong(object, key, value);
	}

	public long getLong(T object, String key) {
		return object == null ? fieldAccessHelper.getLongStatic(clazz, key) : fieldAccessHelper.getLong(object, key);
	}

	public void setFloat(T object, String key, float value) {
		if (object == null) fieldAccessHelper.setFloatStatic(clazz, key, value);
		else fieldAccessHelper.setFloat(object, key, value);
	}

	public float getFloat(T object, String key) {
		return object == null ? fieldAccessHelper.getFloatStatic(clazz, key) : fieldAccessHelper.getFloat(object, key);
	}

	public void setDouble(T object, String key, double value) {
		if (object == null) fieldAccessHelper.setDoubleStatic(clazz, key, value);
		else fieldAccessHelper.setDouble(object, key, value);
	}

	public double getDouble(T object, String key) {
		return object == null ? fieldAccessHelper.getDoubleStatic(clazz, key) : fieldAccessHelper.getDouble(object, key);
	}

	public void setChar(T object, String key, char value) {
		if (object == null) fieldAccessHelper.setCharStatic(clazz, key, value);
		else fieldAccessHelper.setChar(object, key, value);
	}

	public char getChar(T object, String key) {
		return object == null ? fieldAccessHelper.getCharStatic(clazz, key) : fieldAccessHelper.getChar(object, key);
	}

	public void setBoolean(T object, String key, boolean value) {
		if (object == null) fieldAccessHelper.setBooleanStatic(clazz, key, value);
		else fieldAccessHelper.setBoolean(object, key, value);
	}

	public boolean getBoolean(T object, String key) {
		return object == null ? fieldAccessHelper.getBooleanStatic(clazz, key) : fieldAccessHelper.getBoolean(object, key);
	}
}
