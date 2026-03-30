package endfield.util;

import dynamilize.DynamicObject;
import dynamilize.IVariable;

import java.lang.reflect.Field;

import static endfield.Vars2.platformImpl;

public class JavaFieldReference implements IVariable {
	final Field field;
	final FieldAccessor accessor;
	final String name;

	public JavaFieldReference(Field f) {
		field = f;
		accessor = platformImpl.fieldAccessor(f);
		name = f.getName();
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public <T> T get(DynamicObject<?> obj) {
		return accessor.get(obj);
	}

	@Override
	public void set(DynamicObject<?> obj, Object value) {
		accessor.set(obj, value);
	}

	@Override
	public boolean get(DynamicObject<?> obj, boolean def) {
		return accessor.getBoolean(obj);
	}

	@Override
	public byte get(DynamicObject<?> obj, byte def) {
		return accessor.getByte(obj);
	}

	@Override
	public short get(DynamicObject<?> obj, short def) {
		return accessor.getShort(obj);
	}

	@Override
	public int get(DynamicObject<?> obj, int def) {
		return accessor.getInt(obj);
	}

	@Override
	public long get(DynamicObject<?> obj, long def) {
		return accessor.getLong(obj);
	}

	@Override
	public float get(DynamicObject<?> obj, float def) {
		return accessor.getFloat(obj);
	}

	@Override
	public double get(DynamicObject<?> obj, double def) {
		return accessor.getDouble(obj);
	}

	@Override
	public char get(DynamicObject<?> obj, char def) {
		return accessor.getChar(obj);
	}

	@Override
	public void set(DynamicObject<?> obj, boolean value) {
		accessor.setBoolean(obj, value);
	}

	@Override
	public void set(DynamicObject<?> obj, byte value) {
		accessor.setByte(obj, value);
	}

	@Override
	public void set(DynamicObject<?> obj, short value) {
		accessor.setShort(obj, value);
	}

	@Override
	public void set(DynamicObject<?> obj, int value) {
		accessor.setInt(obj, value);
	}

	@Override
	public void set(DynamicObject<?> obj, long value) {
		accessor.setLong(obj, value);
	}

	@Override
	public void set(DynamicObject<?> obj, float value) {
		accessor.setFloat(obj, value);
	}

	@Override
	public void set(DynamicObject<?> obj, double value) {
		accessor.setDouble(obj, value);
	}

	@Override
	public void set(DynamicObject<?> obj, char value) {
		accessor.setChar(obj, value);
	}
}
