package endfield.android;

import arc.func.Prov;
import arc.util.Log;
import endfield.util.CollectionObjectMap;
import endfield.util.FieldAccessHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class AndroidFieldAccessHelper implements FieldAccessHelper {
	protected static final CollectionObjectMap<Class<?>, CollectionObjectMap<String, Field>> fieldMap = new CollectionObjectMap<>(Class.class, CollectionObjectMap.class);

	protected static final Prov<CollectionObjectMap<String, Field>> prov = () -> new CollectionObjectMap<>(String.class, Field.class);

	static Field accessFlags;

	static {
		try {
			accessFlags = Field.class.getDeclaredField("accessFlags");
			accessFlags.setAccessible(true);
		} catch (Throwable e) {
			Log.err(e);
		}
	}

	static void setAccessFlags(Field field) {
		if (accessFlags != null && (field.getModifiers() & Modifier.FINAL) != 0) {
			try {
				int flags = accessFlags.getInt(field);
				accessFlags.setInt(field, flags & ~Modifier.FINAL);
			} catch (IllegalAccessException e) {
				Log.err(e);
			}
		}
	}

	public Field getField(Class<?> clazz, String name, boolean isStatic) throws NoSuchFieldException {
		CollectionObjectMap<String, Field> map = fieldMap.get(clazz, prov);
		Field field = map.get(name);
		if (field != null) return field;

		if (isStatic) {
			Field f = getField(clazz, name);
			map.put(name, f);
			return f;
		} else {
			Class<?> curr = clazz;
			while (curr != Object.class) {
				try {
					Field f = getField(clazz, name);
					map.put(name, f);
					return f;
				} catch (NoSuchFieldException ignored) {}

				curr = curr.getSuperclass();
			}
		}

		throw new NoSuchFieldException("field " + name + " was not found in class: " + clazz);
	}

	protected Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
		Field field = clazz.getDeclaredField(name);
		field.setAccessible(true);

		setAccessFlags(field);

		return field;
	}

	@Override
	public void setByte(Object object, String name, byte value) {
		try {
			getField(object.getClass(), name, false).setByte(object, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setByteStatic(Class<?> clazz, String name, byte value) {
		try {
			getField(clazz, name, true).setByte(null, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte getByte(Object object, String name) {
		try {
			return getField(object.getClass(), name, false).getByte(object);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte getByteStatic(Class<?> clazz, String name) {
		try {
			return getField(clazz, name, true).getByte(null);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setShort(Object object, String name, short value) {
		try {
			getField(object.getClass(), name, false).setInt(object, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setShortStatic(Class<?> clazz, String name, short value) {
		try {
			getField(clazz, name, true).setShort(null, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public short getShort(Object object, String name) {
		try {
			return getField(object.getClass(), name, false).getShort(object);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public short getShortStatic(Class<?> clazz, String name) {
		try {
			return getField(clazz, name, true).getShort(null);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setInt(Object object, String name, int value) {
		try {
			getField(object.getClass(), name, false).setInt(object, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setIntStatic(Class<?> clazz, String name, int value) {
		try {
			getField(clazz, name, true).setInt(null, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getInt(Object object, String name) {
		try {
			return getField(object.getClass(), name, false).getInt(object);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getIntStatic(Class<?> clazz, String name) {
		try {
			return getField(clazz, name, true).getInt(null);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setLong(Object object, String name, long value) {
		try {
			getField(object.getClass(), name, false).setLong(object, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setLongStatic(Class<?> clazz, String name, long value) {
		try {
			getField(clazz, name, true).setLong(null, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLong(Object object, String name) {
		try {
			return getField(object.getClass(), name, false).getLong(object);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLongStatic(Class<?> clazz, String name) {
		try {
			return getField(clazz, name, true).getLong(null);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setFloat(Object object, String name, float value) {
		try {
			getField(object.getClass(), name, false).setFloat(object, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setFloatStatic(Class<?> clazz, String name, float value) {
		try {
			getField(clazz, name, true).setFloat(null, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float getFloat(Object object, String name) {
		try {
			return getField(object.getClass(), name, false).getFloat(object);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float getFloatStatic(Class<?> clazz, String name) {
		try {
			return getField(clazz, name, true).getFloat(null);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setDouble(Object object, String name, double value) {
		try {
			getField(object.getClass(), name, false).setDouble(object, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setDoubleStatic(Class<?> clazz, String name, double value) {
		try {
			getField(clazz, name, true).setDouble(null, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getDouble(Object object, String name) {
		try {
			return getField(object.getClass(), name, false).getDouble(object);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getDoubleStatic(Class<?> clazz, String name) {
		try {
			return getField(clazz, name, true).getDouble(null);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setChar(Object object, String name, char value) {
		try {
			getField(object.getClass(), name, false).setChar(object, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setCharStatic(Class<?> clazz, String name, char value) {
		try {
			getField(clazz, name, true).setChar(null, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public char getChar(Object object, String name) {
		try {
			return getField(object.getClass(), name, false).getChar(object);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public char getCharStatic(Class<?> clazz, String name) {
		try {
			return getField(clazz, name, true).getChar(null);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setBoolean(Object object, String name, boolean value) {
		try {
			getField(object.getClass(), name, false).setBoolean(object, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setBooleanStatic(Class<?> clazz, String name, boolean value) {
		try {
			getField(clazz, name, true).setBoolean(null, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getBoolean(Object object, String name) {
		try {
			return getField(object.getClass(), name, false).getBoolean(object);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getBooleanStatic(Class<?> clazz, String name) {
		try {
			return getField(clazz, name, true).getBoolean(null);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setObject(Object object, String name, Object value) {
		try {
			getField(object.getClass(), name, false).set(object, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setObjectStatic(Class<?> clazz, String name, Object value) {
		try {
			getField(clazz, name, true).set(null, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object, String name) {
		try {
			return (T) getField(object.getClass(), name, false).get(object);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObjectStatic(Class<?> clazz, String name) {
		try {
			return (T) getField(clazz, name, true).get(null);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void set(Object object, String name, Object value) {
		try {
			getField(object.getClass(), name, false).set(object, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setStatic(Class<?> clazz, String name, Object value) {
		try {
			getField(clazz, name, true).set(null, value);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object, String name) {
		try {
			return (T) getField(object.getClass(), name, false).get(object);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getStatic(Class<?> clazz, String name) {
		try {
			return (T) getField(clazz, name, true).get(null);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}
}
