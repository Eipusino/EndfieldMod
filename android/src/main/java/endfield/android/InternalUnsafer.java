package endfield.android;

import jdk.internal.misc.Unsafe;

import java.lang.reflect.Field;

// Sdk_version>=33
public final class InternalUnsafer {
	static Unsafe internalUnsafe;

	static {
		try {
			Field field = null;

			try {
				// Sdk_version>=36.1
				field = sun.misc.Unsafe.class.getDeclaredField("theInternalUnsafe");
			} catch (NoSuchFieldException ignored) {
			}

			if (field == null) field = Unsafe.class.getDeclaredField("theUnsafe");

			field.setAccessible(true);
			internalUnsafe = (Unsafe) field.get(null);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
