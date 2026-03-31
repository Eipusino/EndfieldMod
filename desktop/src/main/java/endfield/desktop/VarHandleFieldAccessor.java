package endfield.desktop;

import endfield.util.AbstractFieldAccessor;
import endfield.util.FieldAccessor;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static endfield.desktop.DesktopImpl.lookup;

public sealed class VarHandleFieldAccessor extends AbstractFieldAccessor {
	final VarHandle handle;

	VarHandleFieldAccessor(Field f) {
		super(f);

		try {
			handle = lookup.unreflectVarHandle(f);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	// Setter final fields is not supported.
	public static FieldAccessor getVarHandleFieldAccessor(Field f) {
		int modifiers = f.getModifiers();

		if ((modifiers & Modifier.STATIC) != 0)
			if ((modifiers & Modifier.VOLATILE) != 0) return new VarHandleStaticQualifiedFieldAccessor(f);
			else return new VarHandleStaticFieldAccessor(f);
		else if ((modifiers & Modifier.VOLATILE) != 0) return new VarHandleQualifiedFieldAccessor(f);
		else return new VarHandleFieldAccessor(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) handle.get(object);
	}

	@Override
	public void set(Object object, Object value) {
		handle.set(object, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object) {
		return (T) handle.get(object);
	}

	@Override
	public void setObject(Object object, Object value) {
		handle.set(object, value);
	}

	@Override
	public boolean getBoolean(Object object) {
		return (boolean) handle.get(object);
	}

	@Override
	public void setBoolean(Object object, boolean value) {
		handle.set(object, value);
	}

	@Override
	public byte getByte(Object object) {
		return (byte) handle.get(object);
	}

	@Override
	public void setByte(Object object, byte value) {
		handle.set(object, value);
	}

	@Override
	public char getChar(Object object) {
		return (char) handle.get(object);
	}

	@Override
	public void setChar(Object object, char value) {
		handle.set(object, value);
	}

	@Override
	public short getShort(Object object) {
		return (short) handle.get(object);
	}

	@Override
	public void setShort(Object object, short value) {
		handle.set(object, value);
	}

	@Override
	public int getInt(Object object) {
		return (int) handle.get(object);
	}

	@Override
	public void setInt(Object object, int value) {
		handle.set(object, value);
	}

	@Override
	public long getLong(Object object) {
		return (long) handle.get(object);
	}

	@Override
	public void setLong(Object object, long value) {
		handle.set(object, value);
	}

	@Override
	public float getFloat(Object object) {
		return (float) handle.get(object);
	}

	@Override
	public void setFloat(Object object, float value) {
		handle.set(object, value);
	}

	@Override
	public double getDouble(Object object) {
		return (double) handle.get(object);
	}

	@Override
	public void setDouble(Object object, double value) {
		handle.set(object, value);
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof VarHandleFieldAccessor other && other.getField().equals(field);
	}
}

final class VarHandleQualifiedFieldAccessor extends VarHandleFieldAccessor {
	VarHandleQualifiedFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) handle.getVolatile(object);
	}

	@Override
	public void set(Object object, Object value) {
		handle.setVolatile(object, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object) {
		return (T) handle.getVolatile(object);
	}

	@Override
	public void setObject(Object object, Object value) {
		handle.setVolatile(object, value);
	}

	@Override
	public boolean getBoolean(Object object) {
		return (boolean) handle.getVolatile(object);
	}

	@Override
	public void setBoolean(Object object, boolean value) {
		handle.setVolatile(object, value);
	}

	@Override
	public byte getByte(Object object) {
		return (byte) handle.getVolatile(object);
	}

	@Override
	public void setByte(Object object, byte value) {
		handle.setVolatile(object, value);
	}

	@Override
	public char getChar(Object object) {
		return (char) handle.getVolatile(object);
	}

	@Override
	public void setChar(Object object, char value) {
		handle.setVolatile(object, value);
	}

	@Override
	public short getShort(Object object) {
		return (short) handle.getVolatile(object);
	}

	@Override
	public void setShort(Object object, short value) {
		handle.setVolatile(object, value);
	}

	@Override
	public int getInt(Object object) {
		return (int) handle.getVolatile(object);
	}

	@Override
	public void setInt(Object object, int value) {
		handle.setVolatile(object, value);
	}

	@Override
	public long getLong(Object object) {
		return (long) handle.getVolatile(object);
	}

	@Override
	public void setLong(Object object, long value) {
		handle.setVolatile(object, value);
	}

	@Override
	public float getFloat(Object object) {
		return (float) handle.getVolatile(object);
	}

	@Override
	public void setFloat(Object object, float value) {
		handle.setVolatile(object, value);
	}

	@Override
	public double getDouble(Object object) {
		return (double) handle.getVolatile(object);
	}

	@Override
	public void setDouble(Object object, double value) {
		handle.setVolatile(object, value);
	}
}

sealed class VarHandleStaticFieldAccessor extends VarHandleFieldAccessor {
	VarHandleStaticFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) handle.get();
	}

	@Override
	public void set(Object object, Object value) {
		handle.set(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object) {
		return (T) handle.get();
	}

	@Override
	public void setObject(Object object, Object value) {
		handle.set(value);
	}

	@Override
	public boolean getBoolean(Object object) {
		return (boolean) handle.get();
	}

	@Override
	public void setBoolean(Object object, boolean value) {
		handle.set(value);
	}

	@Override
	public byte getByte(Object object) {
		return (byte) handle.get();
	}

	@Override
	public void setByte(Object object, byte value) {
		handle.set(value);
	}

	@Override
	public char getChar(Object object) {
		return (char) handle.get();
	}

	@Override
	public void setChar(Object object, char value) {
		handle.set(value);
	}

	@Override
	public short getShort(Object object) {
		return (short) handle.get();
	}

	@Override
	public void setShort(Object object, short value) {
		handle.set(value);
	}

	@Override
	public int getInt(Object object) {
		return (int) handle.get();
	}

	@Override
	public void setInt(Object object, int value) {
		handle.set(value);
	}

	@Override
	public long getLong(Object object) {
		return (long) handle.get();
	}

	@Override
	public void setLong(Object object, long value) {
		handle.set(value);
	}

	@Override
	public float getFloat(Object object) {
		return (float) handle.get();
	}

	@Override
	public void setFloat(Object object, float value) {
		handle.set(value);
	}

	@Override
	public double getDouble(Object object) {
		return (double) handle.get();
	}

	@Override
	public void setDouble(Object object, double value) {
		handle.set(value);
	}
}

final class VarHandleStaticQualifiedFieldAccessor extends VarHandleStaticFieldAccessor {
	VarHandleStaticQualifiedFieldAccessor(Field f) {
		super(f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object object) {
		return (T) handle.getVolatile();
	}

	@Override
	public void set(Object object, Object value) {
		handle.setVolatile(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getObject(Object object) {
		return (T) handle.getVolatile();
	}

	@Override
	public void setObject(Object object, Object value) {
		handle.setVolatile(value);
	}

	@Override
	public boolean getBoolean(Object object) {
		return (boolean) handle.getVolatile();
	}

	@Override
	public void setBoolean(Object object, boolean value) {
		handle.setVolatile(value);
	}

	@Override
	public byte getByte(Object object) {
		return (byte) handle.getVolatile();
	}

	@Override
	public void setByte(Object object, byte value) {
		handle.setVolatile(value);
	}

	@Override
	public char getChar(Object object) {
		return (char) handle.getVolatile();
	}

	@Override
	public void setChar(Object object, char value) {
		handle.setVolatile(value);
	}

	@Override
	public short getShort(Object object) {
		return (short) handle.getVolatile();
	}

	@Override
	public void setShort(Object object, short value) {
		handle.setVolatile(value);
	}

	@Override
	public int getInt(Object object) {
		return (int) handle.getVolatile();
	}

	@Override
	public void setInt(Object object, int value) {
		handle.setVolatile(value);
	}

	@Override
	public long getLong(Object object) {
		return (long) handle.getVolatile();
	}

	@Override
	public void setLong(Object object, long value) {
		handle.setVolatile(value);
	}

	@Override
	public float getFloat(Object object) {
		return (float) handle.getVolatile();
	}

	@Override
	public void setFloat(Object object, float value) {
		handle.setVolatile(value);
	}

	@Override
	public double getDouble(Object object) {
		return (double) handle.getVolatile();
	}

	@Override
	public void setDouble(Object object, double value) {
		handle.setVolatile(value);
	}
}
