package endfield.android;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

public final class AndroidConstant {
	static final Field OVERRIDE, ACCESS_FLAGS;

	static {
		try {
			OVERRIDE = AccessibleObject.class.getDeclaredField("override");
			ACCESS_FLAGS = Class.class.getDeclaredField("accessFlags");
			OVERRIDE.setAccessible(true);
			ACCESS_FLAGS.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	private AndroidConstant() {}
}
