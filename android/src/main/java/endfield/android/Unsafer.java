package endfield.android;

import endfield.android.util.field.AndroidField;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@SuppressWarnings("removal")
public final class Unsafer {
	static final Unsafe unsafe;

	static {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private Unsafer() {}

	public static String getGetMessage(Field field, String type) {
		return "Attempt to get " + field.getType().getName() + " field \"" +
				field.getDeclaringClass().getName() + "." + field.getName() + "\" with illegal data type conversion to " + type;
	}

	public static String getSetMessage(Field field, @Nullable Object object) {
		return getSetMessage(field, object == null ? "" : object.getClass().getName(), "");
	}

	public static String getSetMessage(Field field, String attemptedType, String attemptedValue) {
		int modifiers = field.getModifiers();

		StringBuilder err = new StringBuilder().append("Can not set");
		if ((modifiers & Modifier.STATIC) != 0)
			err.append(" static");
		if ((modifiers & Modifier.FINAL) != 0)
			err.append(" final");
		err.append(" ").append(field.getType().getName()).append(" field ").append(field.getDeclaringClass().getName()).append(".").append(field.getName()).append(" to ");
		if (!attemptedValue.isEmpty()) {
			err.append("(").append(attemptedType).append(")").append(attemptedValue);
		} else {
			if (!attemptedType.isEmpty())
				err.append(attemptedType);
			else
				err.append("null value");
		}
		return err.toString();
	}

	public static void setByte(Field field, Object object, byte value) {
		long offset = AndroidField.fieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			unsafe.putByteVolatile(object, offset, value);
		else
			unsafe.putByte(object, offset, value);
	}

	public static void setByteStatic(Field field, byte value) {
		long offset = AndroidField.fieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			unsafe.putByteVolatile(field.getDeclaringClass(), offset, value);
		else
			unsafe.putByte(field.getDeclaringClass(), offset, value);
	}

	public static byte getByte(Field field, Object object) {
		long offset = AndroidField.fieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				unsafe.getByteVolatile(object, offset) :
				unsafe.getByte(object, offset);
	}

	public static byte getByteStatic(Field field) {
		long offset = AndroidField.fieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				unsafe.getByteVolatile(field.getDeclaringClass(), offset) :
				unsafe.getByte(field.getDeclaringClass(), offset);
	}

	public static void setShort(Field field, Object object, short value) {
		long offset = AndroidField.fieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			unsafe.putShortVolatile(object, offset, value);
		else
			unsafe.putShort(object, offset, value);
	}

	public static void setShortStatic(Field field, short value) {
		long offset = AndroidField.fieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			unsafe.putShortVolatile(field.getDeclaringClass(), offset, value);
		else
			unsafe.putShort(field.getDeclaringClass(), offset, value);
	}

	public static short getShort(Field field, Object object) {
		long offset = AndroidField.fieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				unsafe.getShortVolatile(object, offset) :
				unsafe.getShort(object, offset);
	}

	public static short getShortStatic(Field field) {
		long offset = AndroidField.fieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				unsafe.getShortVolatile(field.getDeclaringClass(), offset) :
				unsafe.getShort(field.getDeclaringClass(), offset);
	}

	public static void setInt(Field field, Object object, int value) {
		long offset = AndroidField.fieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			unsafe.putIntVolatile(object, offset, value);
		else
			unsafe.putInt(object, offset, value);
	}

	public static void setIntStatic(Field field, int value) {
		long offset = AndroidField.fieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			unsafe.putIntVolatile(field.getDeclaringClass(), offset, value);
		else
			unsafe.putInt(field.getDeclaringClass(), offset, value);
	}

	public static int getInt(Field field, Object object) {
		long offset = AndroidField.fieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				unsafe.getIntVolatile(object, offset) :
				unsafe.getInt(object, offset);
	}

	public static int getIntStatic(Field field) {
		long offset = AndroidField.fieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				unsafe.getIntVolatile(field.getDeclaringClass(), offset) :
				unsafe.getInt(field.getDeclaringClass(), offset);
	}

	public static void setLong(Field field, Object object, long value) {
		long offset = AndroidField.fieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			unsafe.putLongVolatile(object, offset, value);
		else
			unsafe.putLong(object, offset, value);
	}

	public static void setLongStatic(Field field, long value) {
		long offset = AndroidField.fieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			unsafe.putLongVolatile(field.getDeclaringClass(), offset, value);
		else
			unsafe.putLong(field.getDeclaringClass(), offset, value);
	}

	public static long getLong(Field field, Object object) {
		long offset = AndroidField.fieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				unsafe.getLongVolatile(object, offset) :
				unsafe.getLong(object, offset);
	}

	public static long getLongStatic(Field field) {
		long offset = AndroidField.fieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				unsafe.getLongVolatile(field.getDeclaringClass(), offset) :
				unsafe.getLong(field.getDeclaringClass(), offset);
	}

	public static void setFloat(Field field, Object object, float value) {
		long offset = AndroidField.fieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			unsafe.putFloatVolatile(object, offset, value);
		else
			unsafe.putFloat(object, offset, value);
	}

	public static void setFloatStatic(Field field, float value) {
		long offset = AndroidField.fieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0) {
			unsafe.putFloatVolatile(field.getDeclaringClass(), offset, value);
		} else unsafe.putFloat(field.getDeclaringClass(), offset, value);
	}

	public static float getFloat(Field field, Object object) {
		long offset = AndroidField.fieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				unsafe.getFloatVolatile(object, offset) :
				unsafe.getFloat(object, offset);
	}

	public static float getFloatStatic(Field field) {
		long offset = AndroidField.fieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				unsafe.getFloatVolatile(field.getDeclaringClass(), offset) :
				unsafe.getFloat(field.getDeclaringClass(), offset);
	}

	public static void setDouble(Field field, Object object, double value) {
		long offset = AndroidField.fieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			unsafe.putDoubleVolatile(object, offset, value);
		else
			unsafe.putDouble(object, offset, value);
	}

	public static void setDoubleStatic(Field field, double value) {
		long offset = AndroidField.fieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			unsafe.putDoubleVolatile(field.getDeclaringClass(), offset, value);
		else
			unsafe.putDouble(field.getDeclaringClass(), offset, value);
	}

	public static double getDouble(Field field, Object object) {
		long offset = AndroidField.fieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				unsafe.getDoubleVolatile(object, offset) :
				unsafe.getDouble(object, offset);
	}

	public static double getDoubleStatic(Field field) {
		long offset = AndroidField.fieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				unsafe.getDoubleVolatile(field.getDeclaringClass(), offset) :
				unsafe.getDouble(field.getDeclaringClass(), offset);
	}

	public static void setChar(Field field, Object object, char value) {
		long offset = AndroidField.fieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			unsafe.putCharVolatile(object, offset, value);
		else
			unsafe.putChar(object, offset, value);
	}

	public static void setCharStatic(Field field, char value) {
		long offset = AndroidField.fieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			unsafe.putCharVolatile(field.getDeclaringClass(), offset, value);
		else
			unsafe.putChar(field.getDeclaringClass(), offset, value);
	}

	public static char getChar(Field field, Object object) {
		long offset = AndroidField.fieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				unsafe.getCharVolatile(object, offset) :
				unsafe.getChar(object, offset);
	}

	public static char getCharStatic(Field field) {
		long offset = AndroidField.fieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				unsafe.getCharVolatile(field.getDeclaringClass(), offset) :
				unsafe.getChar(field.getDeclaringClass(), offset);
	}

	public static void setBoolean(Field field, Object object, boolean value) {
		long offset = AndroidField.fieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			unsafe.putBooleanVolatile(object, offset, value);
		else
			unsafe.putBoolean(object, offset, value);
	}

	public static void setBooleanStatic(Field field, boolean value) {
		long offset = AndroidField.fieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			unsafe.putBooleanVolatile(field.getDeclaringClass(), offset, value);
		else
			unsafe.putBoolean(field.getDeclaringClass(), offset, value);
	}

	public static boolean getBoolean(Field field, Object object) {
		long offset = AndroidField.fieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				unsafe.getBooleanVolatile(object, offset) :
				unsafe.getBoolean(object, offset);
	}

	public static boolean getBooleanStatic(Field field) {
		long offset = AndroidField.fieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				unsafe.getBooleanVolatile(field.getDeclaringClass(), offset) :
				unsafe.getBoolean(field.getDeclaringClass(), offset);
	}

	public static void setObject(Field field, Object object, Object value) {
		long offset = AndroidField.fieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			unsafe.putObjectVolatile(object, offset, value);
		else
			unsafe.putObject(object, offset, value);
	}

	public static void setObjectStatic(Field field, Object value) {
		long offset = AndroidField.fieldOffset(field);

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			unsafe.putObjectVolatile(field.getDeclaringClass(), offset, value);
		else
			unsafe.putObject(field.getDeclaringClass(), offset, value);
	}

	public static Object getObject(Field field, Object object) {
		long offset = AndroidField.fieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				unsafe.getObjectVolatile(object, offset) :
				unsafe.getObject(object, offset);
	}

	public static Object getObjectStatic(Field field) {
		long offset = AndroidField.fieldOffset(field);

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				unsafe.getObjectVolatile(field.getDeclaringClass(), offset) :
				unsafe.getObject(field.getDeclaringClass(), offset);
	}

	public static void set(Field field, Object object, Object value) {
		long offset = AndroidField.fieldOffset(field);
		Class<?> clazz = field.getType();
		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			putVolatile(value, object, offset, clazz);
		else
			put(value, object, offset, clazz);
	}

	public static void setStatic(Field field, Object value) {
		Object base = field.getDeclaringClass();
		long offset = AndroidField.fieldOffset(field);
		Class<?> clazz = field.getType();

		if ((field.getModifiers() & Modifier.VOLATILE) != 0)
			putVolatile(value, base, offset, clazz);
		else
			put(value, base, offset, clazz);
	}

	public static Object get(Field field, Object object) {
		long offset = AndroidField.fieldOffset(field);
		Class<?> clazz = field.getType();

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				getVolatile(object, offset, clazz) :
				get(object, offset, clazz);
	}

	public static Object getStatic(Field field) {
		Object base = field.getDeclaringClass();
		long offset = AndroidField.fieldOffset(field);
		Class<?> clazz = field.getType();

		return (field.getModifiers() & Modifier.VOLATILE) != 0 ?
				getVolatile(base, offset, clazz) :
				get(base, offset, clazz);
	}

	static void put(Object value, Object object, long offset, Class<?> type) {
		if (type.isPrimitive()) {
			if (type == int.class) unsafe.putInt(object, offset, (int) value);
			else if (type == float.class) unsafe.putFloat(object, offset, (float) value);
			else if (type == boolean.class) unsafe.putBoolean(object, offset, (boolean) value);
			else if (type == byte.class) unsafe.putByte(object, offset, (byte) value);
			else if (type == double.class) unsafe.putDouble(object, offset, (double) value);
			else if (type == long.class) unsafe.putLong(object, offset, (long) value);
			else if (type == char.class) unsafe.putChar(object, offset, (char) value);
			else if (type == short.class) unsafe.putShort(object, offset, (short) value);
			else throw new IllegalArgumentException("unknown type of field " + type);
		} else {
			if (value != null && !type.isInstance(value)) throw new IllegalArgumentException();

			unsafe.putObject(object, offset, value);
		}
	}

	static void putVolatile(Object value, Object object, long offset, Class<?> type) {
		if (type.isPrimitive()) {
			if (type == int.class) unsafe.putIntVolatile(object, offset, (int) value);
			else if (type == float.class) unsafe.putFloatVolatile(object, offset, (float) value);
			else if (type == boolean.class) unsafe.putBooleanVolatile(object, offset, (boolean) value);
			else if (type == byte.class) unsafe.putByteVolatile(object, offset, (byte) value);
			else if (type == long.class) unsafe.putLongVolatile(object, offset, (long) value);
			else if (type == double.class) unsafe.putDoubleVolatile(object, offset, (double) value);
			else if (type == char.class) unsafe.putCharVolatile(object, offset, (char) value);
			else if (type == short.class) unsafe.putShortVolatile(object, offset, (short) value);
			else throw new IllegalArgumentException("unknown type of field " + type);
		} else {
			if (value != null && !type.isInstance(value)) throw new IllegalArgumentException();

			unsafe.putObjectVolatile(object, offset, value);
		}
	}

	static Object get(Object object, long offset, Class<?> type) {
		if (type.isPrimitive()) {
			if (type == int.class) return unsafe.getInt(object, offset);
			else if (type == float.class) return unsafe.getFloat(object, offset);
			else if (type == boolean.class) return unsafe.getBoolean(object, offset);
			else if (type == byte.class) return unsafe.getByte(object, offset);
			else if (type == long.class) return unsafe.getDouble(object, offset);
			else if (type == double.class) return unsafe.getLong(object, offset);
			else if (type == char.class) return unsafe.getChar(object, offset);
			else if (type == short.class) return unsafe.getShort(object, offset);
			else throw new IllegalArgumentException("unknown type of field " + type);
		} else {
			return unsafe.getObject(object, offset);
		}
	}

	static Object getVolatile(Object object, long offset, Class<?> type) {
		if (type.isPrimitive()) {
			if (type == int.class) return unsafe.getIntVolatile(object, offset);
			else if (type == float.class) return unsafe.getFloatVolatile(object, offset);
			else if (type == boolean.class) return unsafe.getBooleanVolatile(object, offset);
			else if (type == byte.class) return unsafe.getByteVolatile(object, offset);
			else if (type == long.class) return unsafe.getLongVolatile(object, offset);
			else if (type == double.class) return unsafe.getDoubleVolatile(object, offset);
			else if (type == char.class) return unsafe.getCharVolatile(object, offset);
			else if (type == short.class) return unsafe.getShortVolatile(object, offset);
			else throw new IllegalArgumentException("unknown type of field " + type);
		} else {
			return unsafe.getObjectVolatile(object, offset);
		}
	}
}
