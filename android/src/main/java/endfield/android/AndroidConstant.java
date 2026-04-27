package endfield.android;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

public final class AndroidConstant {
	static final Field override, accessFlags;

	static {
		try {
			override = AccessibleObject.class.getDeclaredField("override");
			accessFlags = Class.class.getDeclaredField("accessFlags");
			override.setAccessible(true);
			accessFlags.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	private AndroidConstant() {}
}
