package endfield.desktop;

import arc.util.Log;
import endfield.util.AccessibleHelper;

import java.lang.invoke.VarHandle;
import java.lang.reflect.AccessibleObject;

import static endfield.desktop.DesktopImpl.lookup;

public class DesktopAccessibleHelper implements AccessibleHelper {
	static VarHandle override;

	@Override
	public void makeAccessible(AccessibleObject object) {
		if (override == null) {
			try {
				override = lookup.findVarHandle(AccessibleObject.class, "override", boolean.class);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				object.setAccessible(true);

				Log.err(e);
				return;
			}
		}

		override.set(object, true);
	}
}
