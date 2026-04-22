package endfield.desktop;

import arc.func.Prov;
import endfield.util.CollectionObjectMap;
import endfield.util.FieldAccessHelper;
import endfield.util.NoSuchVariableException;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Function;

import static endfield.desktop.DesktopClassHelper.function4;
import static endfield.desktop.DesktopImpl.lookup;

public class MethodHandleFieldAccessHelper implements FieldAccessHelper {
	protected static final CollectionObjectMap<Class<?>, CollectionObjectMap<String, Field>> fieldMap = new CollectionObjectMap<>(Class.class, CollectionObjectMap.class);
	protected static final CollectionObjectMap<Class<?>, Field[]> fieldsMap = new CollectionObjectMap<>(Class.class, Field[].class);

	protected static final Prov<CollectionObjectMap<String, Field>> prov5 = () -> new CollectionObjectMap<>(String.class, Field.class);

	protected static final CollectionObjectMap<Field, MethodHandle> getters = new CollectionObjectMap<>(Field.class, MethodHandle.class);
	protected static final CollectionObjectMap<Field, MethodHandle> setters = new CollectionObjectMap<>(Field.class, MethodHandle.class);

	protected static final Function<Field, MethodHandle> function7 = field -> {
		try {
			String name = field.getName();
			Class<?> dec = field.getDeclaringClass(), type = field.getType();

			return (field.getModifiers() & Modifier.STATIC) != 0 ?
					lookup.findStaticGetter(dec, name, type) :
					lookup.findGetter(dec, name, type);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}, function8 = field -> {
		try {
			String name = field.getName();
			Class<?> dec = field.getDeclaringClass(), type = field.getType();

			return (field.getModifiers() & Modifier.STATIC) != 0 ?
					lookup.findStaticSetter(dec, name, type) :
					lookup.findSetter(dec, name, type);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	};

	public Field getField(Class<?> clazz, String name, boolean isStatic) {
		CollectionObjectMap<String, Field> map = fieldMap.get(clazz, prov5);
		Field res = map.get(name);
		if (res != null) return res;

		if (isStatic) {
			res = findField(clazz, name);
			if (res != null && (res.getModifiers() & Modifier.STATIC) != 0) {
				map.put(name, res);
				return res;
			}
		} else {
			Class<?> curr = clazz;
			while (curr != Object.class) {
				res = findField(curr, name);
				if (res != null && (res.getModifiers() & Modifier.STATIC) == 0) {
					map.put(name, res);
					return res;
				}

				curr = curr.getSuperclass();
			}
		}

		throw new NoSuchVariableException("field " + name + " was not found in class: " + clazz);
	}

	protected @Nullable Field findField(Class<?> clazz, String name) {
		Field[] fields = fieldsMap.computeIfAbsent(clazz, function4);
		for (Field field : fields) {
			if (field.getName().equals(name)) return field;
		}
		return null;
	}

	protected MethodHandle getter(Field field) {
		return getters.computeIfAbsent(field, function7);
	}

	protected MethodHandle setter(Field field) {
		return setters.computeIfAbsent(field, function8);
	}

	@Override
	public void setByte(Object object, String name, byte value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setByteStatic(Class<?> clazz, String name, byte value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte getByte(Object object, String name) {
		try {
			Field field = getField(object.getClass(), name, false);

			return (byte) getter(field).invoke(object);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte getByteStatic(Class<?> clazz, String name) {
		try {
			Field field = getField(clazz, name, true);

			return (byte) getter(field).invoke();
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setShort(Object object, String name, short value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setShortStatic(Class<?> clazz, String name, short value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public short getShort(Object object, String name) {
		try {
			Field field = getField(object.getClass(), name, false);

			return (short) getter(field).invoke(object);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public short getShortStatic(Class<?> clazz, String name) {
		try {
			Field field = getField(clazz, name, true);

			return (short) getter(field).invoke();
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setInt(Object object, String name, int value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setIntStatic(Class<?> clazz, String name, int value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getInt(Object object, String name) {
		try {
			Field field = getField(object.getClass(), name, false);

			return (int) getter(field).invoke(object);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getIntStatic(Class<?> clazz, String name) {
		try {
			Field field = getField(clazz, name, true);

			return (int) getter(field).invoke();
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setLong(Object object, String name, long value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setLongStatic(Class<?> clazz, String name, long value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLong(Object object, String name) {
		try {
			Field field = getField(object.getClass(), name, false);

			return (long) getter(field).invoke(object);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLongStatic(Class<?> clazz, String name) {
		try {
			Field field = getField(clazz, name, true);

			return (long) getter(field).invoke();
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setFloat(Object object, String name, float value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setFloatStatic(Class<?> clazz, String name, float value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float getFloat(Object object, String name) {
		try {
			Field field = getField(object.getClass(), name, false);

			return (float) getter(field).invoke(object);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float getFloatStatic(Class<?> clazz, String name) {
		try {
			Field field = getField(clazz, name, true);

			return (float) getter(field).invoke();
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setDouble(Object object, String name, double value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setDoubleStatic(Class<?> clazz, String name, double value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getDouble(Object object, String name) {
		try {
			Field field = getField(object.getClass(), name, false);

			return (double) getter(field).invoke(object);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getDoubleStatic(Class<?> clazz, String name) {
		try {
			Field field = getField(clazz, name, true);

			return (double) getter(field).invoke();
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setChar(Object object, String name, char value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setCharStatic(Class<?> clazz, String name, char value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public char getChar(Object object, String name) {
		try {
			Field field = getField(object.getClass(), name, false);

			return (char) getter(field).invoke(object);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public char getCharStatic(Class<?> clazz, String name) {
		try {
			Field field = getField(clazz, name, true);

			return (char) getter(field).invoke();
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setBoolean(Object object, String name, boolean value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setBooleanStatic(Class<?> clazz, String name, boolean value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getBoolean(Object object, String name) {
		try {
			Field field = getField(object.getClass(), name, false);

			return (boolean) getter(field).invoke(object);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getBooleanStatic(Class<?> clazz, String name) {
		try {
			Field field = getField(clazz, name, true);
			return (boolean) getter(field).invoke();
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setObject(Object object, String name, Object value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setObjectStatic(Class<?> clazz, String name, Object value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
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
		} catch (RuntimeException | Error e) {
			throw e;
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
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void set(Object object, String name, Object value) {
		try {
			Field field = getField(object.getClass(), name, false);

			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setStatic(Class<?> clazz, String name, Object value) {
		try {
			Field field = getField(clazz, name, true);

			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
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
		} catch (RuntimeException | Error e) {
			throw e;
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
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setByte(Object object, Field field, byte value) {
		try {
			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setByteStatic(Field field, byte value) {
		try {
			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte getByte(Object object, Field field) {
		try {
			return (byte) getter(field).invoke(object);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte getByteStatic(Field field) {
		try {
			return (byte) getter(field).invoke();
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setShort(Object object, Field field, short value) {
		try {
			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setShortStatic(Field field, short value) {
		try {
			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public short getShort(Object object, Field field) {
		try {
			return (short) getter(field).invoke(object);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public short getShortStatic(Field field) {
		try {
			return (short) getter(field).invoke();
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setInt(Object object, Field field, int value) {
		try {
			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setIntStatic(Field field, int value) {
		try {
			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getInt(Object object, Field field) {
		try {
			return (int) getter(field).invoke(object);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getIntStatic(Field field) {
		try {
			return (int) getter(field).invoke();
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setLong(Object object, Field field, long value) {
		try {
			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setLongStatic(Field field, long value) {
		try {
			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLong(Object object, Field field) {
		try {
			return (long) getter(field).invoke(object);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getLongStatic(Field field) {
		try {
			return (long) getter(field).invoke();
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setFloat(Object object, Field field, float value) {
		try {
			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setFloatStatic(Field field, float value) {
		try {
			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float getFloat(Object object, Field field) {
		try {
			return (float) getter(field).invoke(object);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public float getFloatStatic(Field field) {
		try {
			return (float) getter(field).invoke();
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setDouble(Object object, Field field, double value) {
		try {
			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setDoubleStatic(Field field, double value) {
		try {
			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getDouble(Object object, Field field) {
		try {
			return (double) getter(field).invoke(object);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getDoubleStatic(Field field) {
		try {
			return (double) getter(field).invoke();
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setChar(Object object, Field field, char value) {
		try {
			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setCharStatic(Field field, char value) {
		try {
			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public char getChar(Object object, Field field) {
		try {
			return (char) getter(field).invoke(object);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public char getCharStatic(Field field) {
		try {
			return (char) getter(field).invoke();
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setBoolean(Object object, Field field, boolean value) {
		try {
			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setBooleanStatic(Field field, boolean value) {
		try {
			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getBoolean(Object object, Field field) {
		try {
			return (boolean) getter(field).invoke(object);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getBooleanStatic(Field field) {
		try {
			return (boolean) getter(field).invoke();
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void set(Object object, Field field, Object value) {
		try {
			setter(field).invoke(object, value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setStatic(Field field, Object value) {
		try {
			setter(field).invoke(value);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object, Field field) {
		try {
			return (T) getter(field).invoke(object);
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getStatic(Field field) {
		try {
			return (T) getter(field).invoke();
		} catch (RuntimeException | Error e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
