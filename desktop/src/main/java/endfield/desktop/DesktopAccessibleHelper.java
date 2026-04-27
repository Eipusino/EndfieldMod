package endfield.desktop;

import endfield.util.AccessibleHelper;

import java.lang.invoke.VarHandle;
import java.lang.reflect.AccessibleObject;

import static endfield.desktop.DesktopImpl.lookup;

public class DesktopAccessibleHelper implements AccessibleHelper {
	static VarHandle override;

	static {
		try {
			override = lookup.findVarHandle(AccessibleObject.class, "override", boolean.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void makeAccessible(AccessibleObject object) {
		override.set(object, true);
	}
}
