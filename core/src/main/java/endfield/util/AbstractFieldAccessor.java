package endfield.util;

import java.lang.reflect.Field;

public abstract class AbstractFieldAccessor implements FieldAccessor {
	protected final Field field;

	protected int hash;

	protected AbstractFieldAccessor(Field f) {
		field = f;
	}

	@Override
	public Field getField() {
		return field;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || obj.getClass() == getClass() && ((FieldAccessor) obj).getField().equals(field);
	}

	@Override
	public int hashCode() {
		int hc = hash;

		if (hc == 0) hc = hash = getClass().hashCode() ^ field.hashCode();

		return hc;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": " + field.toString();
	}
}
