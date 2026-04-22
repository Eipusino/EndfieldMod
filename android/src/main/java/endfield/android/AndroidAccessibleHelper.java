package endfield.android;

import arc.util.Log;
import endfield.util.AccessibleHelper;

import java.lang.reflect.AccessibleObject;

import static endfield.android.AndroidConstant.ACCESS_FLAGS;
import static endfield.android.AndroidConstant.OVERRIDE;

public class AndroidAccessibleHelper implements AccessibleHelper {
	@Override
	public void makeAccessible(AccessibleObject object) {
		try {
			OVERRIDE.setBoolean(object, true);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void makeClassAccessible(Class<?> clazz) {
		try {
			int flags = ACCESS_FLAGS.getInt(clazz);
			ACCESS_FLAGS.setInt(clazz, 65535 & ((flags & 65535 & (-17) & (-3)) | 1));
		} catch (IllegalAccessException e) {
			Log.err(e);
		}
	}
}
