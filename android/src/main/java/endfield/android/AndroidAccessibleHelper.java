package endfield.android;

import arc.util.Log;
import endfield.util.AccessibleHelper;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

public class AndroidAccessibleHelper implements AccessibleHelper {
	static Field override, accessFlags;

	@Override
	public void makeAccessible(AccessibleObject object) {
		try {
			if (override == null) {
				override = AccessibleObject.class.getDeclaredField("override");
				override.setAccessible(true);
			}

			override.setBoolean(object, true);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			object.setAccessible(true);

			Log.err(e);
		}
	}

	@Override
	public void makeClassAccessible(Class<?> clazz) {
		try {
			if (accessFlags == null) {
				accessFlags = Class.class.getDeclaredField("accessFlags");
				accessFlags.setAccessible(true);
			}

			int flags = accessFlags.getInt(clazz);
			accessFlags.setInt(clazz, 65535 & ((flags & 65535 & (-17) & (-3)) | 1));
		} catch (IllegalAccessException | NoSuchFieldException e) {
			Log.err(e);
		}
	}
}
