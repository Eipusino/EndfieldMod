package endfield.android;

import endfield.android.util.field.AndroidField;
import endfield.util.AbstractFieldAccessor;
import endfield.util.FieldAccessor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static endfield.android.Unsafer.getGetMessage;
import static endfield.android.Unsafer.getSetMessage;
import static endfield.android.Unsafer.unsafe;

@SuppressWarnings("removal")
public abstract class UnsafeFieldAccessor extends AbstractFieldAccessor {
	protected final long offset;

	protected UnsafeFieldAccessor(Field f) {
		super(f);
		offset = AndroidField.fieldOffset(f);
	}

	public static FieldAccessor getUnsafeFieldAccessor(Field f) {
		Class<?> type = f.getType();
		int modifiers = f.getModifiers();

		if ((modifiers & Modifier.STATIC) != 0) {
			if ((modifiers & Modifier.VOLATILE) != 0) {
				if (type.isPrimitive()) {
					if (type == boolean.class) return new UnsafeQualifiedStaticBooleanFieldAccessor(f);
					else if (type == byte.class) return new UnsafeQualifiedStaticByteFieldAccessor(f);
					else if (type == char.class) return new UnsafeQualifiedStaticCharFieldAccessor(f);
					else if (type == short.class) return new UnsafeQualifiedStaticShortFieldAccessor(f);
					else if (type == int.class) return new UnsafeQualifiedStaticIntFieldAccessor(f);
					else if (type == long.class) return new UnsafeQualifiedStaticLongFieldAccessor(f);
					else if (type == float.class) return new UnsafeQualifiedStaticFloatFieldAccessor(f);
					else if (type == double.class) return new UnsafeQualifiedStaticDoubleFieldAccessor(f);
					else throw new IllegalArgumentException("unknown type of field " + f);
				} else return new UnsafeQualifiedStaticObjectFieldAccessor(f);
			} else {
				if (type.isPrimitive()) {
					if (type == boolean.class) return new UnsafeStaticBooleanFieldAccessor(f);
					else if (type == byte.class) return new UnsafeStaticByteFieldAccessor(f);
					else if (type == char.class) return new UnsafeStaticCharFieldAccessor(f);
					else if (type == short.class) return new UnsafeStaticShortFieldAccessor(f);
					else if (type == int.class) return new UnsafeStaticIntFieldAccessor(f);
					else if (type == long.class) return new UnsafeStaticLongFieldAccessor(f);
					else if (type == float.class) return new UnsafeStaticFloatFieldAccessor(f);
					else if (type == double.class) return new UnsafeStaticDoubleFieldAccessor(f);
					else throw new IllegalArgumentException("unknown type of field " + f);
				} else return new UnsafeStaticObjectFieldAccessor(f);
			}
		} else {
			if ((modifiers & Modifier.VOLATILE) != 0) {
				if (type.isPrimitive()) {
					if (type == boolean.class) return new UnsafeQualifiedBooleanFieldAccessor(f);
					else if (type == byte.class) return new UnsafeQualifiedByteFieldAccessor(f);
					else if (type == char.class) return new UnsafeQualifiedCharFieldAccessor(f);
					else if (type == short.class) return new UnsafeQualifiedShortFieldAccessor(f);
					else if (type == int.class) return new UnsafeQualifiedIntFieldAccessor(f);
					else if (type == long.class) return new UnsafeQualifiedLongFieldAccessor(f);
					else if (type == float.class) return new UnsafeQualifiedFloatFieldAccessor(f);
					else if (type == double.class) return new UnsafeQualifiedDoubleFieldAccessor(f);
					else throw new IllegalArgumentException("unknown type of field " + f);
				} else return new UnsafeQualifiedObjectFieldAccessor(f);
			} else {
				if (type.isPrimitive()) {
					if (type == boolean.class) return new UnsafeBooleanFieldAccessor(f);
					else if (type == byte.class) return new UnsafeByteFieldAccessor(f);
					else if (type == char.class) return new UnsafeCharFieldAccessor(f);
					else if (type == short.class) return new UnsafeShortFieldAccessor(f);
					else if (type == int.class) return new UnsafeIntFieldAccessor(f);
					else if (type == long.class) return new UnsafeLongFieldAccessor(f);
					else if (type == float.class) return new UnsafeFloatFieldAccessor(f);
					else if (type == double.class) return new UnsafeDoubleFieldAccessor(f);
					else throw new IllegalArgumentException("unknown type of field " + f);
				} else return new UnsafeObjectFieldAccessor(f);
			}
		}
	}

	public void ensureObject(Object object) {
		if (!field.getDeclaringClass().isInstance(object))
			throw new IllegalArgumentException(getSetMessage(field, object));
	}

	public void ensureValue(Object value) {
		if (value != null && !field.getType().isInstance(value))
			throw new IllegalArgumentException(getSetMessage(field, value));
	}

	@Override
	public <T> T get(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, Object.class.getName()));
	}

	@Override
	public void set(Object object, Object value) {
		throw new IllegalArgumentException(getSetMessage(field, value));
	}

	@Override
	public <T> T getObject(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, Object.class.getName()));
	}

	@Override
	public void setObject(Object object, Object value) {
		throw new IllegalArgumentException(getSetMessage(field, value));
	}

	@Override
	public boolean getBoolean(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, "boolean"));
	}

	@Override
	public void setBoolean(Object object, boolean value) {
		throw new IllegalArgumentException(getSetMessage(field, "boolean", String.valueOf(value)));
	}

	@Override
	public byte getByte(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, "boolean"));
	}

	@Override
	public void setByte(Object object, byte value) {
		throw new IllegalArgumentException(getSetMessage(field, "byte", String.valueOf(value)));
	}

	@Override
	public char getChar(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, "char"));
	}

	@Override
	public void setChar(Object object, char value) {
		throw new IllegalArgumentException(getSetMessage(field, "char", String.valueOf(value)));
	}

	@Override
	public short getShort(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, "short"));
	}

	@Override
	public void setShort(Object object, short value) {
		throw new IllegalArgumentException(getSetMessage(field, "short", String.valueOf(value)));
	}

	@Override
	public int getInt(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, "int"));
	}

	@Override
	public void setInt(Object object, int value) {
		throw new IllegalArgumentException(getSetMessage(field, "int", String.valueOf(value)));
	}

	@Override
	public long getLong(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, "long"));
	}

	@Override
	public void setLong(Object object, long value) {
		throw new IllegalArgumentException(getSetMessage(field, "long", String.valueOf(value)));
	}

	@Override
	public float getFloat(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, "float"));
	}

	@Override
	public void setFloat(Object object, float value) {
		throw new IllegalArgumentException(getSetMessage(field, "float", String.valueOf(value)));
	}

	@Override
	public double getDouble(Object object) {
		throw new IllegalArgumentException(getGetMessage(field, "double"));
	}

	@Override
	public void setDouble(Object object, double value) {
		throw new IllegalArgumentException(getSetMessage(field, "double", String.valueOf(value)));
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof UnsafeFieldAccessor other && other.getField().equals(field);
	}
}

@SuppressWarnings("removal")
class UnsafeObjectFieldAccessor extends UnsafeFieldAccessor {
	public UnsafeObjectFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public <T> T get(Object object) {
		return getObject(object);
	}

	@Override
	public void set(Object object, Object value) {
		setObject(object, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object) {
		ensureObject(object);
		return (T) unsafe.getObject(object, offset);
	}

	@Override
	public void setObject(Object object, Object value) {
		ensureObject(object);
		ensureValue(value);
		unsafe.putObject(object, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeBooleanFieldAccessor extends UnsafeFieldAccessor {
	public UnsafeBooleanFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Boolean.valueOf(getBoolean(object));
	}

	@Override
	public void set(Object object, Object value) {
		setBoolean(object, (boolean) value);
	}

	@Override
	public boolean getBoolean(Object object) {
		ensureObject(object);
		return unsafe.getBoolean(object, offset);
	}

	@Override
	public void setBoolean(Object object, boolean value) {
		ensureObject(object);
		unsafe.putBoolean(object, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeByteFieldAccessor extends UnsafeFieldAccessor {
	public UnsafeByteFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Byte.valueOf(getByte(object));
	}

	@Override
	public void set(Object object, Object value) {
		setByte(object, ((Number) value).byteValue());
	}

	@Override
	public byte getByte(Object object) {
		ensureObject(object);
		return unsafe.getByte(object, offset);
	}

	@Override
	public void setByte(Object object, byte value) {
		ensureObject(object);
		unsafe.putByte(object, offset, value);
	}

	@Override
	public short getShort(Object object) {
		return getByte(object);
	}

	@Override
	public int getInt(Object object) {
		return getByte(object);
	}

	@Override
	public long getLong(Object object) {
		return getByte(object);
	}

	@Override
	public float getFloat(Object object) {
		return getByte(object);
	}

	@Override
	public double getDouble(Object object) {
		return getByte(object);
	}
}

@SuppressWarnings("removal")
class UnsafeCharFieldAccessor extends UnsafeFieldAccessor {
	public UnsafeCharFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Character.valueOf(getChar(object));
	}

	@Override
	public void set(Object object, Object value) {
		setChar(object, (char) value);
	}

	@Override
	public char getChar(Object object) {
		ensureObject(object);
		return unsafe.getChar(object, offset);
	}

	@Override
	public void setChar(Object object, char value) {
		ensureObject(object);
		unsafe.putChar(object, offset, value);
	}

	@Override
	public int getInt(Object object) {
		return getChar(object);
	}

	@Override
	public long getLong(Object object) {
		return getChar(object);
	}

	@Override
	public float getFloat(Object object) {
		return getChar(object);
	}

	@Override
	public double getDouble(Object object) {
		return getChar(object);
	}
}

@SuppressWarnings("removal")
class UnsafeShortFieldAccessor extends UnsafeFieldAccessor {
	public UnsafeShortFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Short.valueOf(getShort(object));
	}

	@Override
	public void set(Object object, Object value) {
		setShort(object, ((Number) value).shortValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setShort(object, value);
	}

	@Override
	public short getShort(Object object) {
		ensureObject(object);
		return unsafe.getShort(object, offset);
	}

	@Override
	public void setShort(Object object, short value) {
		ensureObject(object);
		unsafe.putShort(object, offset, value);
	}

	@Override
	public int getInt(Object object) {
		return getShort(object);
	}

	@Override
	public long getLong(Object object) {
		return getShort(object);
	}

	@Override
	public float getFloat(Object object) {
		return getShort(object);
	}

	@Override
	public double getDouble(Object object) {
		return getShort(object);
	}
}

@SuppressWarnings("removal")
class UnsafeIntFieldAccessor extends UnsafeFieldAccessor {
	public UnsafeIntFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Integer.valueOf(getInt(object));
	}

	@Override
	public void set(Object object, Object value) {
		setInt(object, ((Number) value).intValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setInt(object, value);
	}

	@Override
	public void setChar(Object object, char value) {
		setInt(object, value);
	}

	@Override
	public void setShort(Object object, short value) {
		setInt(object, value);
	}

	@Override
	public int getInt(Object object) {
		ensureObject(object);
		return unsafe.getInt(object, offset);
	}

	@Override
	public void setInt(Object object, int value) {
		ensureObject(object);
		unsafe.putInt(object, offset, value);
	}

	@Override
	public long getLong(Object object) {
		return getInt(object);
	}

	@Override
	public float getFloat(Object object) {
		return getInt(object);
	}

	@Override
	public double getDouble(Object object) {
		return getInt(object);
	}
}

@SuppressWarnings("removal")
class UnsafeLongFieldAccessor extends UnsafeFieldAccessor {
	public UnsafeLongFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Long.valueOf(getLong(object));
	}

	@Override
	public void set(Object object, Object value) {
		setLong(object, ((Number) value).longValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setLong(object, value);
	}

	@Override
	public void setChar(Object object, char value) {
		setLong(object, value);
	}

	@Override
	public void setShort(Object object, short value) {
		setLong(object, value);
	}

	@Override
	public void setInt(Object object, int value) {
		setLong(object, value);
	}

	@Override
	public long getLong(Object object) {
		ensureObject(object);
		return unsafe.getLong(object, offset);
	}

	@Override
	public void setLong(Object object, long value) {
		ensureObject(object);
		unsafe.putLong(object, offset, value);
	}

	@Override
	public float getFloat(Object object) {
		return getLong(object);
	}

	@Override
	public double getDouble(Object object) {
		return getLong(object);
	}
}

@SuppressWarnings("removal")
class UnsafeFloatFieldAccessor extends UnsafeFieldAccessor {
	public UnsafeFloatFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Float.valueOf(getFloat(object));
	}

	@Override
	public void set(Object object, Object value) {
		setFloat(object, ((Number) value).floatValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setFloat(object, value);
	}

	@Override
	public void setChar(Object object, char value) {
		setFloat(object, value);
	}

	@Override
	public void setShort(Object object, short value) {
		setFloat(object, value);
	}

	@Override
	public void setInt(Object object, int value) {
		setFloat(object, value);
	}

	@Override
	public void setLong(Object object, long value) {
		setFloat(object, value);
	}

	@Override
	public float getFloat(Object object) {
		ensureObject(object);
		return unsafe.getFloat(object, offset);
	}

	@Override
	public void setFloat(Object object, float value) {
		ensureObject(object);
		unsafe.putFloat(object, offset, value);
	}

	@Override
	public double getDouble(Object object) {
		return getFloat(object);
	}
}

@SuppressWarnings("removal")
class UnsafeDoubleFieldAccessor extends UnsafeFieldAccessor {
	public UnsafeDoubleFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Double.valueOf(getDouble(object));
	}

	@Override
	public void set(Object object, Object value) {
		setDouble(object, ((Number) value).doubleValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setDouble(object, value);
	}

	@Override
	public void setChar(Object object, char value) {
		setDouble(object, value);
	}

	@Override
	public void setShort(Object object, short value) {
		setDouble(object, value);
	}

	@Override
	public void setInt(Object object, int value) {
		setDouble(object, value);
	}

	@Override
	public void setLong(Object object, long value) {
		setDouble(object, value);
	}

	@Override
	public void setFloat(Object object, float value) {
		setDouble(object, value);
	}

	@Override
	public double getDouble(Object object) {
		ensureObject(object);
		return unsafe.getDouble(object, offset);
	}

	@Override
	public void setDouble(Object object, double value) {
		ensureObject(object);
		unsafe.putDouble(object, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeQualifiedObjectFieldAccessor extends UnsafeObjectFieldAccessor {
	public UnsafeQualifiedObjectFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public <T> T get(Object object) {
		return getObject(object);
	}

	@Override
	public void set(Object object, Object value) {
		setObject(object, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object) {
		ensureObject(object);
		return (T) unsafe.getObjectVolatile(object, offset);
	}

	@Override
	public void setObject(Object object, Object value) {
		ensureObject(object);
		ensureValue(value);
		unsafe.putObjectVolatile(object, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeQualifiedBooleanFieldAccessor extends UnsafeBooleanFieldAccessor {
	public UnsafeQualifiedBooleanFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public boolean getBoolean(Object object) {
		ensureObject(object);
		return unsafe.getBooleanVolatile(object, offset);
	}

	@Override
	public void setBoolean(Object object, boolean value) {
		ensureObject(object);
		unsafe.putBooleanVolatile(object, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeQualifiedByteFieldAccessor extends UnsafeByteFieldAccessor {
	public UnsafeQualifiedByteFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public byte getByte(Object object) {
		ensureObject(object);
		return unsafe.getByteVolatile(object, offset);
	}

	@Override
	public void setByte(Object object, byte value) {
		ensureObject(object);
		unsafe.putByteVolatile(object, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeQualifiedCharFieldAccessor extends UnsafeCharFieldAccessor {
	public UnsafeQualifiedCharFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public char getChar(Object object) {
		ensureObject(object);
		return unsafe.getCharVolatile(object, offset);
	}

	@Override
	public void setChar(Object object, char value) {
		ensureObject(object);
		unsafe.putCharVolatile(object, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeQualifiedShortFieldAccessor extends UnsafeShortFieldAccessor {
	public UnsafeQualifiedShortFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public short getShort(Object object) {
		ensureObject(object);
		return unsafe.getShortVolatile(object, offset);
	}

	@Override
	public void setShort(Object object, short value) {
		ensureObject(object);
		unsafe.putShortVolatile(object, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeQualifiedIntFieldAccessor extends UnsafeIntFieldAccessor {
	public UnsafeQualifiedIntFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public int getInt(Object object) {
		ensureObject(object);
		return unsafe.getIntVolatile(object, offset);
	}

	@Override
	public void setInt(Object object, int value) {
		ensureObject(object);
		unsafe.putIntVolatile(object, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeQualifiedLongFieldAccessor extends UnsafeLongFieldAccessor {
	public UnsafeQualifiedLongFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public long getLong(Object object) {
		ensureObject(object);
		return unsafe.getLongVolatile(object, offset);
	}

	@Override
	public void setLong(Object object, long value) {
		ensureObject(object);
		unsafe.putLongVolatile(object, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeQualifiedFloatFieldAccessor extends UnsafeFloatFieldAccessor {
	public UnsafeQualifiedFloatFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public float getFloat(Object object) {
		ensureObject(object);
		return unsafe.getFloatVolatile(object, offset);
	}

	@Override
	public void setFloat(Object object, float value) {
		ensureObject(object);
		unsafe.putFloatVolatile(object, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeQualifiedDoubleFieldAccessor extends UnsafeDoubleFieldAccessor {
	public UnsafeQualifiedDoubleFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public double getDouble(Object object) {
		ensureObject(object);
		return unsafe.getDoubleVolatile(object, offset);
	}

	@Override
	public void setDouble(Object object, double value) {
		ensureObject(object);
		unsafe.putDoubleVolatile(object, offset, value);
	}
}

@SuppressWarnings("removal")
abstract class UnsafeStaticFieldAccessor extends UnsafeFieldAccessor {
	protected final Object base;

	protected UnsafeStaticFieldAccessor(Field f) {
		super(f);

		if ((f.getModifiers() & Modifier.STATIC) != 0) base = f.getDeclaringClass();
		else throw new IllegalArgumentException("This field is not a static field: " + f);
	}
}

@SuppressWarnings("removal")
class UnsafeStaticObjectFieldAccessor extends UnsafeStaticFieldAccessor {
	public UnsafeStaticObjectFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public <T> T get(Object object) {
		return getObject(object);
	}

	@Override
	public void set(Object object, Object value) {
		setObject(object, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object) {
		return (T) unsafe.getObject(base, offset);
	}

	@Override
	public void setObject(Object object, Object value) {
		ensureValue(value);
		unsafe.putObject(base, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeStaticBooleanFieldAccessor extends UnsafeStaticFieldAccessor {
	public UnsafeStaticBooleanFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Boolean.valueOf(getBoolean(object));
	}

	@Override
	public void set(Object object, Object value) {
		setBoolean(object, (boolean) value);
	}

	@Override
	public boolean getBoolean(Object object) {
		return unsafe.getBoolean(base, offset);
	}

	@Override
	public void setBoolean(Object object, boolean value) {
		unsafe.putBoolean(base, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeStaticByteFieldAccessor extends UnsafeStaticFieldAccessor {
	public UnsafeStaticByteFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Byte.valueOf(getByte(object));
	}

	@Override
	public void set(Object object, Object value) {
		setByte(object, ((Number) value).byteValue());
	}

	@Override
	public byte getByte(Object object) {
		return unsafe.getByte(base, offset);
	}

	@Override
	public void setByte(Object object, byte value) {
		unsafe.putByte(base, offset, value);
	}

	@Override
	public short getShort(Object object) {
		return getByte(object);
	}

	@Override
	public int getInt(Object object) {
		return getByte(object);
	}

	@Override
	public long getLong(Object object) {
		return getByte(object);
	}

	@Override
	public float getFloat(Object object) {
		return getByte(object);
	}

	@Override
	public double getDouble(Object object) {
		return getByte(object);
	}
}

@SuppressWarnings("removal")
class UnsafeStaticCharFieldAccessor extends UnsafeStaticFieldAccessor {
	public UnsafeStaticCharFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Character.valueOf(getChar(object));
	}

	@Override
	public void set(Object object, Object value) {
		setChar(object, (char) value);
	}

	@Override
	public char getChar(Object object) {
		return unsafe.getChar(base, offset);
	}

	@Override
	public void setChar(Object object, char value) {
		unsafe.putChar(base, offset, value);
	}

	@Override
	public int getInt(Object object) {
		return getChar(object);
	}

	@Override
	public long getLong(Object object) {
		return getChar(object);
	}

	@Override
	public float getFloat(Object object) {
		return getChar(object);
	}

	@Override
	public double getDouble(Object object) {
		return getChar(object);
	}
}

@SuppressWarnings("removal")
class UnsafeStaticShortFieldAccessor extends UnsafeStaticFieldAccessor {
	public UnsafeStaticShortFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Short.valueOf(getShort(object));
	}

	@Override
	public void set(Object object, Object value) {
		setShort(object, ((Number) value).shortValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setShort(object, value);
	}

	@Override
	public short getShort(Object object) {
		return unsafe.getShort(base, offset);
	}

	@Override
	public void setShort(Object object, short value) {
		unsafe.putShort(base, offset, value);
	}

	@Override
	public int getInt(Object object) {
		return getShort(object);
	}

	@Override
	public long getLong(Object object) {
		return getShort(object);
	}

	@Override
	public float getFloat(Object object) {
		return getShort(object);
	}

	@Override
	public double getDouble(Object object) {
		return getShort(object);
	}
}

@SuppressWarnings("removal")
class UnsafeStaticIntFieldAccessor extends UnsafeStaticFieldAccessor {
	public UnsafeStaticIntFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Integer.valueOf(getInt(object));
	}

	@Override
	public void set(Object object, Object value) {
		setInt(object, ((Number) value).intValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setInt(object, value);
	}

	@Override
	public void setChar(Object object, char value) {
		setInt(object, value);
	}

	@Override
	public void setShort(Object object, short value) {
		setInt(object, value);
	}

	@Override
	public int getInt(Object object) {
		return unsafe.getInt(base, offset);
	}

	@Override
	public void setInt(Object object, int value) {
		unsafe.putInt(base, offset, value);
	}

	@Override
	public long getLong(Object object) {
		return getInt(object);
	}

	@Override
	public float getFloat(Object object) {
		return getInt(object);
	}

	@Override
	public double getDouble(Object object) {
		return getInt(object);
	}
}

@SuppressWarnings("removal")
class UnsafeStaticLongFieldAccessor extends UnsafeStaticFieldAccessor {
	public UnsafeStaticLongFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Long.valueOf(getLong(object));
	}

	@Override
	public void set(Object object, Object value) {
		setLong(object, ((Number) value).longValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setLong(object, value);
	}

	@Override
	public void setChar(Object object, char value) {
		setLong(object, value);
	}

	@Override
	public void setShort(Object object, short value) {
		setLong(object, value);
	}

	@Override
	public void setInt(Object object, int value) {
		setLong(object, value);
	}

	@Override
	public long getLong(Object object) {
		return unsafe.getLong(base, offset);
	}

	@Override
	public void setLong(Object object, long value) {
		unsafe.putLong(base, offset, value);
	}

	@Override
	public float getFloat(Object object) {
		return getLong(object);
	}

	@Override
	public double getDouble(Object object) {
		return getLong(object);
	}
}

@SuppressWarnings("removal")
class UnsafeStaticFloatFieldAccessor extends UnsafeStaticFieldAccessor {
	public UnsafeStaticFloatFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Float.valueOf(getFloat(object));
	}

	@Override
	public void set(Object object, Object value) {
		setFloat(object, ((Number) value).floatValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setFloat(object, value);
	}

	@Override
	public void setChar(Object object, char value) {
		setFloat(object, value);
	}

	@Override
	public void setShort(Object object, short value) {
		setFloat(object, value);
	}

	@Override
	public void setInt(Object object, int value) {
		setFloat(object, value);
	}

	@Override
	public void setLong(Object object, long value) {
		setFloat(object, value);
	}

	@Override
	public float getFloat(Object object) {
		return unsafe.getFloat(base, offset);
	}

	@Override
	public void setFloat(Object object, float value) {
		unsafe.putFloat(base, offset, value);
	}

	@Override
	public double getDouble(Object object) {
		return getFloat(object);
	}
}

@SuppressWarnings("removal")
class UnsafeStaticDoubleFieldAccessor extends UnsafeStaticFieldAccessor {
	public UnsafeStaticDoubleFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) Double.valueOf(getDouble(object));
	}

	@Override
	public void set(Object object, Object value) {
		setDouble(object, ((Number) value).doubleValue());
	}

	@Override
	public void setByte(Object object, byte value) {
		setDouble(object, value);
	}

	@Override
	public void setChar(Object object, char value) {
		setDouble(object, value);
	}

	@Override
	public void setShort(Object object, short value) {
		setDouble(object, value);
	}

	@Override
	public void setInt(Object object, int value) {
		setDouble(object, value);
	}

	@Override
	public void setLong(Object object, long value) {
		setDouble(object, value);
	}

	@Override
	public void setFloat(Object object, float value) {
		setDouble(object, value);
	}

	@Override
	public double getDouble(Object object) {
		return unsafe.getDouble(base, offset);
	}

	@Override
	public void setDouble(Object object, double value) {
		unsafe.putDouble(base, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeQualifiedStaticObjectFieldAccessor extends UnsafeStaticObjectFieldAccessor {
	public UnsafeQualifiedStaticObjectFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object) {
		return (T) unsafe.getObjectVolatile(base, offset);
	}

	@Override
	public void setObject(Object object, Object value) {
		ensureValue(value);
		unsafe.putObjectVolatile(base, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeQualifiedStaticBooleanFieldAccessor extends UnsafeStaticBooleanFieldAccessor {
	public UnsafeQualifiedStaticBooleanFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public boolean getBoolean(Object object) {
		return unsafe.getBooleanVolatile(base, offset);
	}

	@Override
	public void setBoolean(Object object, boolean value) {
		unsafe.putBooleanVolatile(base, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeQualifiedStaticByteFieldAccessor extends UnsafeStaticByteFieldAccessor {
	public UnsafeQualifiedStaticByteFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public byte getByte(Object object) {
		return unsafe.getByteVolatile(base, offset);
	}

	@Override
	public void setByte(Object object, byte value) {
		unsafe.putByteVolatile(base, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeQualifiedStaticCharFieldAccessor extends UnsafeStaticCharFieldAccessor {
	public UnsafeQualifiedStaticCharFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public short getShort(Object object) {
		return unsafe.getShortVolatile(base, offset);
	}

	@Override
	public void setShort(Object object, short value) {
		unsafe.putShortVolatile(base, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeQualifiedStaticShortFieldAccessor extends UnsafeStaticShortFieldAccessor {
	public UnsafeQualifiedStaticShortFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public short getShort(Object object) {
		return unsafe.getShortVolatile(base, offset);
	}

	@Override
	public void setShort(Object object, short value) {
		unsafe.putShortVolatile(base, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeQualifiedStaticIntFieldAccessor extends UnsafeStaticShortFieldAccessor {
	public UnsafeQualifiedStaticIntFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public int getInt(Object object) {
		return unsafe.getIntVolatile(base, offset);
	}

	@Override
	public void setInt(Object object, int value) {
		unsafe.putIntVolatile(base, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeQualifiedStaticLongFieldAccessor extends UnsafeStaticLongFieldAccessor {
	public UnsafeQualifiedStaticLongFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public long getLong(Object object) {
		return unsafe.getLongVolatile(base, offset);
	}

	@Override
	public void setLong(Object object, long value) {
		unsafe.putLongVolatile(base, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeQualifiedStaticFloatFieldAccessor extends UnsafeStaticFloatFieldAccessor {
	public UnsafeQualifiedStaticFloatFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public float getFloat(Object object) {
		return unsafe.getFloatVolatile(base, offset);
	}

	@Override
	public void setFloat(Object object, float value) {
		unsafe.putFloatVolatile(base, offset, value);
	}
}

@SuppressWarnings("removal")
class UnsafeQualifiedStaticDoubleFieldAccessor extends UnsafeStaticDoubleFieldAccessor {
	public UnsafeQualifiedStaticDoubleFieldAccessor(Field f) {
		super(f);
	}

	@Override
	public double getDouble(Object object) {
		return unsafe.getDoubleVolatile(base, offset);
	}

	@Override
	public void setDouble(Object object, double value) {
		unsafe.putDoubleVolatile(base, offset, value);
	}
}