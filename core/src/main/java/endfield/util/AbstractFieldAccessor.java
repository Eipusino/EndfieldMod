package endfield.util;

import java.lang.reflect.Field;

public abstract class AbstractFieldAccessor implements FieldAccessor {
	protected final Field field;

	protected AbstractFieldAccessor(Field f) {
		field = f;
	}

	@Override
	public Field getField() {
		return field;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj instanceof AbstractFieldAccessor other && other.getField().equals(field);
	}

	@Override
	public int hashCode() {
		return field.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + field.toString();
	}
}
