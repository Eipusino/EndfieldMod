package endfield.android;

import endfield.util.CollectionObjectMap;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.function.Function;

import static endfield.Vars2.platformImpl;

/**
 * @deprecated In fact, the efficiency of using method handles on Android does not seem to be as
 * good as reflection, Let's not consider handle implementation for now.
 */
@Deprecated
public class AndroidFieldAccessHelper2 extends AndroidFieldAccessHelper {
	static final CollectionObjectMap<Field, MethodHandle> getters = new CollectionObjectMap<>(Field.class, MethodHandle.class);
	static final CollectionObjectMap<Field, MethodHandle> setters = new CollectionObjectMap<>(Field.class, MethodHandle.class);

	static final Function<Field, MethodHandle> function1 = f -> {
		try {
			return platformImpl.lookup(f.getDeclaringClass()).unreflectGetter(f);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}, function2 = f -> {
		try {
			return platformImpl.lookup(f.getDeclaringClass()).unreflectSetter(f);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	};

	protected MethodHandle getter(Field field) {
		return getters.computeIfAbsent(field, function1);
	}

	protected MethodHandle setter(Field field) {
		return setters.computeIfAbsent(field, function2);
	}

	@Override
	public void setByte(Object object, String name, byte value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setByteStatic(Class<?> clazz, String name, byte value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte getByte(Object object, String name) {
		try {
			Field field = getField(object.getClass(), name, false);

			return (byte) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte getByteStatic(Class<?> clazz, String name) {
		try {
			Field field = getField(clazz, name, true);

			return (byte) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setShort(Object object, String name, short value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setShortStatic(Class<?> clazz, String name, short value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public short getShort(Object object, String name) {
		try {
			Field field = getField(object.getClass(), name, false);

			return (short) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public short getShortStatic(Class<?> clazz, String name) {
		try {
			Field field = getField(clazz, name, true);

			return (short) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setInt(Object object, String name, int value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setIntStatic(Class<?> clazz, String name, int value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getInt(Object object, String name) {
		try {
			Field field = getField(object.getClass(), name, false);

			return (int) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getIntStatic(Class<?> clazz, String name) {
		try {
			Field field = getField(clazz, name, true);

			return (int) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setLong(Object object, String name, long value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setLongStatic(Class<?> clazz, String name, long value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLong(Object object, String name) {
		try {
			Field field = getField(object.getClass(), name, false);

			return (long) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLongStatic(Class<?> clazz, String name) {
		try {
			Field field = getField(clazz, name, true);

			return (long) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setFloat(Object object, String name, float value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setFloatStatic(Class<?> clazz, String name, float value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float getFloat(Object object, String name) {
		try {
			Field field = getField(object.getClass(), name, false);

			return (float) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float getFloatStatic(Class<?> clazz, String name) {
		try {
			Field field = getField(clazz, name, true);

			return (float) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setDouble(Object object, String name, double value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setDoubleStatic(Class<?> clazz, String name, double value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getDouble(Object object, String name) {
		try {
			Field field = getField(object.getClass(), name, false);

			return (double) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getDoubleStatic(Class<?> clazz, String name) {
		try {
			Field field = getField(clazz, name, true);

			return (double) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setChar(Object object, String name, char value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setCharStatic(Class<?> clazz, String name, char value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public char getChar(Object object, String name) {
		try {
			Field field = getField(object.getClass(), name, false);

			return (char) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public char getCharStatic(Class<?> clazz, String name) {
		try {
			Field field = getField(clazz, name, true);

			return (char) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setBoolean(Object object, String name, boolean value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setBooleanStatic(Class<?> clazz, String name, boolean value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getBoolean(Object object, String name) {
		try {
			Field field = getField(object.getClass(), name, false);

			return (boolean) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getBooleanStatic(Class<?> clazz, String name) {
		try {
			Field field = getField(clazz, name, true);

			return (boolean) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setObject(Object object, String name, Object value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setObjectStatic(Class<?> clazz, String name, Object value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object, String name) {
		try {
			Field field = getField(object.getClass(), name, false);

			return (T) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObjectStatic(Class<?> clazz, String name) {
		try {
			Field field = getField(clazz, name, true);

			return (T) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void set(Object object, String name, Object value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setStatic(Class<?> clazz, String name, Object value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object, String name) {
		try {
			Field field = getField(object.getClass(), name, false);

			return (T) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getStatic(Class<?> clazz, String name) {
		try {
			Field field = getField(clazz, name, true);

			return (T) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setByte(Object object, Field field, byte value) {
		try {
			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setByteStatic(Field field, byte value) {
		try {
			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte getByte(Object object, Field field) {
		try {
			return (byte) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte getByteStatic(Field field) {
		try {
			return (byte) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setShort(Object object, Field field, short value) {
		try {
			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setShortStatic(Field field, short value) {
		try {
			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public short getShort(Object object, Field field) {
		try {
			return (short) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public short getShortStatic(Field field) {
		try {
			return (short) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setInt(Object object, Field field, int value) {
		try {
			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setIntStatic(Field field, int value) {
		try {
			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getInt(Object object, Field field) {
		try {
			return (int) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getIntStatic(Field field) {
		try {
			return (int) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setLong(Object object, Field field, long value) {
		try {
			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setLongStatic(Field field, long value) {
		try {
			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLong(Object object, Field field) {
		try {
			return (long) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLongStatic(Field field) {
		try {
			return (long) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setFloat(Object object, Field field, float value) {
		try {
			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setFloatStatic(Field field, float value) {
		try {
			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float getFloat(Object object, Field field) {
		try {
			return (float) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float getFloatStatic(Field field) {
		try {
			return (float) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setDouble(Object object, Field field, double value) {
		try {
			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setDoubleStatic(Field field, double value) {
		try {
			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getDouble(Object object, Field field) {
		try {
			return (double) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getDoubleStatic(Field field) {
		try {
			return (double) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setChar(Object object, Field field, char value) {
		try {
			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setCharStatic(Field field, char value) {
		try {
			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public char getChar(Object object, Field field) {
		try {
			return (char) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public char getCharStatic(Field field) {
		try {
			return (char) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setBoolean(Object object, Field field, boolean value) {
		try {
			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setBooleanStatic(Field field, boolean value) {
		try {
			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getBoolean(Object object, Field field) {
		try {
			return (boolean) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getBooleanStatic(Field field) {
		try {
			return (boolean) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void set(Object object, Field field, Object value) {
		try {
			setter(field).invoke(object, value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setStatic(Field field, Object value) {
		try {
			setter(field).invoke(value);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object, Field field) {
		try {
			return (T) getter(field).invoke(object);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getStatic(Field field) {
		try {
			return (T) getter(field).invoke();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
