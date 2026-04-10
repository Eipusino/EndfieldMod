package endfield.desktop;

import arc.func.Prov;
import endfield.util.CollectionObjectMap;
import endfield.util.FieldAccessHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static endfield.Vars2.classHelper;
import static endfield.desktop.Unsafer.getGetMessage;
import static endfield.desktop.Unsafer.getSetMessage;

public class UnsafeFieldAccessHelper implements FieldAccessHelper {
	protected static final CollectionObjectMap<Class<?>, CollectionObjectMap<String, Field>> fieldMap = new CollectionObjectMap<>(Class.class, CollectionObjectMap.class);

	protected static final Prov<CollectionObjectMap<String, Field>> prov = () -> new CollectionObjectMap<>(String.class, Field.class);

	public Field getField(Class<?> clazz, String name, boolean isStatic) {
		CollectionObjectMap<String, Field> map = fieldMap.get(clazz, prov);
		Field res = map.get(name);
		if (res != null) return res;

		if (isStatic) {
			res = classHelper.findField(clazz, name);
			if (res != null && (res.getModifiers() & Modifier.STATIC) != 0) {
				map.put(name, res);
				return res;
			}
		} else {
			Class<?> curr = clazz;
			while (curr != Object.class) {
				res = classHelper.findField(curr, name);
				if (res != null && (res.getModifiers() & Modifier.STATIC) == 0) {
					map.put(name, res);
					return res;
				}

				curr = curr.getSuperclass();
			}
		}

		throw new RuntimeException("field " + name + " was not found in class: " + clazz);
	}

	@Override
	public void setByte(Object object, String name, byte value) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != byte.class) throw new IllegalArgumentException(getSetMessage(field, "byte", String.valueOf(value)));

		Unsafer.setByte(field, object, value);
	}

	@Override
	public void setByteStatic(Class<?> clazz, String name, byte value) {
		Field field = getField(clazz, name, true);

		if (field.getType() != byte.class) throw new IllegalArgumentException(getSetMessage(field, "byte", String.valueOf(value)));

		Unsafer.setByteStatic(field, value);
	}

	@Override
	public byte getByte(Object object, String name) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != byte.class) throw new IllegalArgumentException(getGetMessage(field, "byte"));

		return Unsafer.getByte(field, object);
	}

	@Override
	public byte getByteStatic(Class<?> clazz, String name) {
		Field field = getField(clazz, name, true);

		if (field.getType() != byte.class) throw new IllegalArgumentException(getGetMessage(field, "byte"));

		return Unsafer.getByteStatic(field);
	}

	@Override
	public void setShort(Object object, String name, short value) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != short.class) throw new IllegalArgumentException(getSetMessage(field, "short", String.valueOf(value)));

		Unsafer.setShort(field, object, value);
	}

	@Override
	public void setShortStatic(Class<?> clazz, String name, short value) {
		Field field = getField(clazz, name, true);

		if (field.getType() != short.class) throw new IllegalArgumentException(getSetMessage(field, "short", String.valueOf(value)));

		Unsafer.setShortStatic(field, value);
	}

	@Override
	public short getShort(Object object, String name) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != short.class) throw new IllegalArgumentException(getGetMessage(field, "short"));

		return Unsafer.getShort(field, object);
	}

	@Override
	public short getShortStatic(Class<?> clazz, String name) {
		Field field = getField(clazz, name, true);

		if (field.getType() != short.class) throw new IllegalArgumentException(getGetMessage(field, "short"));

		return Unsafer.getShortStatic(field);
	}

	@Override
	public void setInt(Object object, String name, int value) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != int.class) throw new IllegalArgumentException(getSetMessage(field, "int", String.valueOf(value)));

		Unsafer.setInt(field, object, value);
	}

	@Override
	public void setIntStatic(Class<?> clazz, String name, int value) {
		Field field = getField(clazz, name, true);

		if (field.getType() != int.class) throw new IllegalArgumentException(getSetMessage(field, "int", String.valueOf(value)));

		Unsafer.setIntStatic(field, value);
	}

	@Override
	public int getInt(Object object, String name) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != int.class) throw new IllegalArgumentException(getGetMessage(field, "int"));

		return Unsafer.getInt(field, object);
	}

	@Override
	public int getIntStatic(Class<?> clazz, String name) {
		Field field = getField(clazz, name, true);

		if (field.getType() != int.class) throw new IllegalArgumentException(getGetMessage(field, "int"));

		return Unsafer.getIntStatic(field);
	}

	@Override
	public void setLong(Object object, String name, long value) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != long.class) throw new IllegalArgumentException(getSetMessage(field, "long", String.valueOf(value)));

		Unsafer.setLong(field, object, value);
	}

	@Override
	public void setLongStatic(Class<?> clazz, String name, long value) {
		Field field = getField(clazz, name, true);

		if (field.getType() != long.class) throw new IllegalArgumentException(getSetMessage(field, "long", String.valueOf(value)));

		Unsafer.setLongStatic(field, value);
	}

	@Override
	public long getLong(Object object, String name) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != long.class) throw new IllegalArgumentException(getGetMessage(field, "long"));

		return Unsafer.getLong(field, object);
	}

	@Override
	public long getLongStatic(Class<?> clazz, String name) {
		Field field = getField(clazz, name, true);

		if (field.getType() != long.class) throw new IllegalArgumentException(getGetMessage(field, "long"));

		return Unsafer.getLongStatic(field);
	}

	@Override
	public void setFloat(Object object, String name, float value) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != float.class) throw new IllegalArgumentException(getSetMessage(field, "float", String.valueOf(value)));

		Unsafer.setFloat(field, object, value);
	}

	@Override
	public void setFloatStatic(Class<?> clazz, String name, float value) {
		Field field = getField(clazz, name, true);

		if (field.getType() != float.class) throw new IllegalArgumentException(getSetMessage(field, "float", String.valueOf(value)));

		Unsafer.setFloatStatic(field, value);
	}

	@Override
	public float getFloat(Object object, String name) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != float.class) throw new IllegalArgumentException(getGetMessage(field, "float"));

		return Unsafer.getFloat(field, object);
	}

	@Override
	public float getFloatStatic(Class<?> clazz, String name) {
		Field field = getField(clazz, name, true);

		if (field.getType() != float.class) throw new IllegalArgumentException(getGetMessage(field, "float"));

		return Unsafer.getFloatStatic(field);
	}

	@Override
	public void setDouble(Object object, String name, double value) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != double.class) throw new IllegalArgumentException(getSetMessage(field, "double", String.valueOf(value)));

		Unsafer.setDouble(field, object, value);
	}

	@Override
	public void setDoubleStatic(Class<?> clazz, String name, double value) {
		Field field = getField(clazz, name, true);

		if (field.getType() != double.class) throw new IllegalArgumentException(getSetMessage(field, "double", String.valueOf(value)));

		Unsafer.setDoubleStatic(field, value);
	}

	@Override
	public double getDouble(Object object, String name) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != double.class) throw new IllegalArgumentException(getGetMessage(field, "double"));

		return Unsafer.getDouble(field, object);
	}

	@Override
	public double getDoubleStatic(Class<?> clazz, String name) {
		Field field = getField(clazz, name, true);

		if (field.getType() != double.class) throw new IllegalArgumentException(getGetMessage(field, "double"));

		return Unsafer.getDoubleStatic(field);
	}

	@Override
	public void setChar(Object object, String name, char value) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != char.class) throw new IllegalArgumentException(getSetMessage(field, "char", String.valueOf(value)));

		Unsafer.setChar(field, object, value);
	}

	@Override
	public void setCharStatic(Class<?> clazz, String name, char value) {
		Field field = getField(clazz, name, true);

		if (field.getType() != char.class) throw new IllegalArgumentException(getSetMessage(field, "char", String.valueOf(value)));

		Unsafer.setCharStatic(field, value);
	}

	@Override
	public char getChar(Object object, String name) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != char.class) throw new IllegalArgumentException(getGetMessage(field, "char"));

		return Unsafer.getChar(field, object);
	}

	@Override
	public char getCharStatic(Class<?> clazz, String name) {
		Field field = getField(clazz, name, true);

		if (field.getType() != char.class) throw new IllegalArgumentException(getGetMessage(field, "char"));

		return Unsafer.getCharStatic(field);
	}

	@Override
	public void setBoolean(Object object, String name, boolean value) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != boolean.class) throw new IllegalArgumentException(getSetMessage(field, "boolean", String.valueOf(value)));

		Unsafer.setBoolean(field, object, value);
	}

	@Override
	public void setBooleanStatic(Class<?> clazz, String name, boolean value) {
		Field field = getField(clazz, name, true);

		if (field.getType() != boolean.class) throw new IllegalArgumentException(getSetMessage(field, "boolean", String.valueOf(value)));

		Unsafer.setBooleanStatic(field, value);
	}

	@Override
	public boolean getBoolean(Object object, String name) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != boolean.class) throw new IllegalArgumentException(getGetMessage(field, "boolean"));

		return Unsafer.getBoolean(field, object);
	}

	@Override
	public boolean getBooleanStatic(Class<?> clazz, String name) {
		Field field = getField(clazz, name, true);

		if (field.getType() != boolean.class) throw new IllegalArgumentException(getGetMessage(field, "boolean"));

		return Unsafer.getBooleanStatic(field);
	}

	@Override
	public void setObject(Object object, String name, Object value) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType().isPrimitive() || value != null && !field.getType().isInstance(value)) throw new IllegalArgumentException(getSetMessage(field, value));

		Unsafer.setObject(field, object, value);
	}

	@Override
	public void setObjectStatic(Class<?> clazz, String name, Object value) {
		Field field = getField(clazz, name, true);

		if (field.getType().isPrimitive() || value != null && !field.getType().isInstance(value)) throw new IllegalArgumentException(getSetMessage(field, value));

		Unsafer.setObjectStatic(field, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object, String name) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType().isPrimitive()) throw new IllegalArgumentException(getGetMessage(field, Object.class.getName()));

		return (T) Unsafer.getObject(field, object);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObjectStatic(Class<?> clazz, String name) {
		Field field = getField(clazz, name, true);

		if (field.getType().isPrimitive()) throw new IllegalArgumentException(getGetMessage(field, Object.class.getName()));

		return (T) Unsafer.getObjectStatic(field);
	}

	@Override
	public void set(Object object, String name, Object value) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));

		if (field.getType().isPrimitive() ?
				value == null :
				value != null && !field.getType().isInstance(value)) throw new IllegalArgumentException(getSetMessage(field, value));

		Unsafer.set(field, object, value);
	}

	@Override
	public void setStatic(Class<?> clazz, String name, Object value) {
		Field field = getField(clazz, name, true);

		if (field.getType().isPrimitive() ?
				value == null :
				value != null && !field.getType().isInstance(value)) throw new IllegalArgumentException(getSetMessage(field, value));

		Unsafer.setStatic(field, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object, String name) {
		Field field = getField(object.getClass(), name, false);

		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));

		return (T) Unsafer.get(field, object);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getStatic(Class<?> clazz, String name) {
		Field field = getField(clazz, name, true);

		return (T) Unsafer.getStatic(field);
	}

	@Override
	public void setByte(Object object, Field field, byte value) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != byte.class) throw new IllegalArgumentException(getSetMessage(field, "byte", String.valueOf(value)));

		Unsafer.setByte(field, object, value);
	}

	@Override
	public void setByteStatic(Field field, byte value) {
		if (field.getType() != byte.class) throw new IllegalArgumentException(getSetMessage(field, "byte", String.valueOf(value)));

		Unsafer.setByteStatic(field, value);
	}

	@Override
	public byte getByte(Object object, Field field) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != byte.class) throw new IllegalArgumentException(getGetMessage(field, "byte"));

		return Unsafer.getByte(field, object);
	}

	@Override
	public byte getByteStatic(Field field) {
		if (field.getType() != byte.class) throw new IllegalArgumentException(getGetMessage(field, "byte"));

		return Unsafer.getByteStatic(field);
	}

	@Override
	public void setShort(Object object, Field field, short value) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != short.class) throw new IllegalArgumentException(getSetMessage(field, "short", String.valueOf(value)));

		Unsafer.setShort(field, object, value);
	}

	@Override
	public void setShortStatic(Field field, short value) {
		if (field.getType() != short.class) throw new IllegalArgumentException(getSetMessage(field, "short", String.valueOf(value)));

		Unsafer.setShortStatic(field, value);
	}

	@Override
	public short getShort(Object object, Field field) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != short.class) throw new IllegalArgumentException(getGetMessage(field, "short"));

		return Unsafer.getShort(field, object);
	}

	@Override
	public short getShortStatic(Field field) {
		if (field.getType() != short.class) throw new IllegalArgumentException(getGetMessage(field, "short"));

		return Unsafer.getShortStatic(field);
	}

	@Override
	public void setInt(Object object, Field field, int value) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != int.class) throw new IllegalArgumentException(getSetMessage(field, "int", String.valueOf(value)));

		Unsafer.setInt(field, object, value);
	}

	@Override
	public void setIntStatic(Field field, int value) {
		if (field.getType() != int.class) throw new IllegalArgumentException(getSetMessage(field, "int", String.valueOf(value)));

		Unsafer.setIntStatic(field, value);
	}

	@Override
	public int getInt(Object object, Field field) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != int.class) throw new IllegalArgumentException(getGetMessage(field, "int"));

		return Unsafer.getInt(field, object);
	}

	@Override
	public int getIntStatic(Field field) {
		if (field.getType() != int.class) throw new IllegalArgumentException(getGetMessage(field, "int"));

		return Unsafer.getIntStatic(field);
	}

	@Override
	public void setLong(Object object, Field field, long value) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != long.class) throw new IllegalArgumentException(getSetMessage(field, "long", String.valueOf(value)));

		Unsafer.setLong(field, object, value);
	}

	@Override
	public void setLongStatic(Field field, long value) {
		if (field.getType() != long.class) throw new IllegalArgumentException(getSetMessage(field, "long", String.valueOf(value)));

		Unsafer.setLongStatic(field, value);
	}

	@Override
	public long getLong(Object object, Field field) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != long.class) throw new IllegalArgumentException(getGetMessage(field, "long"));

		return Unsafer.getLong(field, object);
	}

	@Override
	public long getLongStatic(Field field) {
		if (field.getType() != long.class) throw new IllegalArgumentException(getGetMessage(field, "long"));

		return Unsafer.getLongStatic(field);
	}

	@Override
	public void setFloat(Object object, Field field, float value) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != float.class) throw new IllegalArgumentException(getSetMessage(field, "float", String.valueOf(value)));

		Unsafer.setFloat(field, object, value);
	}

	@Override
	public void setFloatStatic(Field field, float value) {
		if (field.getType() != float.class) throw new IllegalArgumentException(getSetMessage(field, "float", String.valueOf(value)));

		Unsafer.setFloatStatic(field, value);
	}

	@Override
	public float getFloat(Object object, Field field) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != float.class) throw new IllegalArgumentException(getGetMessage(field, "float"));

		return Unsafer.getFloat(field, object);
	}

	@Override
	public float getFloatStatic(Field field) {
		if (field.getType() != float.class) throw new IllegalArgumentException(getGetMessage(field, "float"));

		return Unsafer.getFloatStatic(field);
	}

	@Override
	public void setDouble(Object object, Field field, double value) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != double.class) throw new IllegalArgumentException(getSetMessage(field, "double", String.valueOf(value)));

		Unsafer.setDouble(field, object, value);
	}

	@Override
	public void setDoubleStatic(Field field, double value) {
		if (field.getType() != double.class) throw new IllegalArgumentException(getSetMessage(field, "double", String.valueOf(value)));

		Unsafer.setDoubleStatic(field, value);
	}

	@Override
	public double getDouble(Object object, Field field) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != double.class) throw new IllegalArgumentException(getGetMessage(field, "double"));

		return Unsafer.getDouble(field, object);
	}

	@Override
	public double getDoubleStatic(Field field) {
		if (field.getType() != double.class) throw new IllegalArgumentException(getGetMessage(field, "double"));

		return Unsafer.getDoubleStatic(field);
	}

	@Override
	public void setChar(Object object, Field field, char value) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != char.class) throw new IllegalArgumentException(getSetMessage(field, "char", String.valueOf(value)));

		Unsafer.setChar(field, object, value);
	}

	@Override
	public void setCharStatic(Field field, char value) {
		if (field.getType() != char.class) throw new IllegalArgumentException(getSetMessage(field, "char", String.valueOf(value)));

		Unsafer.setCharStatic(field, value);
	}

	@Override
	public char getChar(Object object, Field field) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != char.class) throw new IllegalArgumentException(getGetMessage(field, "char"));

		return Unsafer.getChar(field, object);
	}

	@Override
	public char getCharStatic(Field field) {
		if (field.getType() != char.class) throw new IllegalArgumentException(getGetMessage(field, "char"));

		return Unsafer.getCharStatic(field);
	}

	@Override
	public void setBoolean(Object object, Field field, boolean value) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != boolean.class) throw new IllegalArgumentException(getSetMessage(field, "boolean", String.valueOf(value)));

		Unsafer.setBoolean(field, object, value);
	}

	@Override
	public void setBooleanStatic(Field field, boolean value) {
		if (field.getType() != boolean.class) throw new IllegalArgumentException(getSetMessage(field, "boolean", String.valueOf(value)));

		Unsafer.setBooleanStatic(field, value);
	}

	@Override
	public boolean getBoolean(Object object, Field field) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType() != boolean.class) throw new IllegalArgumentException(getGetMessage(field, "boolean"));

		return Unsafer.getBoolean(field, object);
	}

	@Override
	public boolean getBooleanStatic(Field field) {
		if (field.getType() != boolean.class) throw new IllegalArgumentException(getGetMessage(field, "boolean"));

		return Unsafer.getBooleanStatic(field);
	}

	@Override
	public void setObject(Object object, Field field, Object value) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType().isPrimitive() || value != null && !field.getType().isInstance(value)) throw new IllegalArgumentException(getSetMessage(field, value));

		Unsafer.setObject(field, object, value);
	}

	@Override
	public void setObjectStatic(Field field, Object value) {
		if (field.getType().isPrimitive() || value != null && !field.getType().isInstance(value)) throw new IllegalArgumentException(getSetMessage(field, value));

		Unsafer.setObjectStatic(field, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object, Field field) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));
		if (field.getType().isPrimitive()) throw new IllegalArgumentException(getGetMessage(field, Object.class.getName()));

		return (T) Unsafer.getObject(field, object);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObjectStatic(Field field) {
		if (field.getType().isPrimitive()) throw new IllegalArgumentException(getGetMessage(field, Object.class.getName()));

		return (T) Unsafer.getObjectStatic(field);
	}

	@Override
	public void set(Object object, Field field, Object value) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));

		if (field.getType().isPrimitive() ?
				value == null :
				value != null && !field.getType().isInstance(value)) throw new IllegalArgumentException(getSetMessage(field, value));

		Unsafer.set(field, object, value);
	}

	@Override
	public void setStatic(Field field, Object value) {
		if (field.getType().isPrimitive() ?
				value == null :
				value != null && !field.getType().isInstance(value)) throw new IllegalArgumentException(getSetMessage(field, value));

		Unsafer.setStatic(field, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object, Field field) {
		if (!field.getDeclaringClass().isInstance(object)) throw new IllegalArgumentException(getSetMessage(field, object));

		return (T) Unsafer.get(field, object);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getStatic(Field field) {
		return (T) Unsafer.getStatic(field);
	}
}
