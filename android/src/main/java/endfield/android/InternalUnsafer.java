package endfield.android;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import jdk.internal.misc.Unsafe;

import java.lang.reflect.Field;

// Sdk_version>=33
@TargetApi(VERSION_CODES.TIRAMISU)
public final class InternalUnsafer {
	static final Unsafe internalUnsafe;

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
