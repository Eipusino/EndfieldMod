package endfield.desktop;

import arc.util.Log;
import endfield.util.AccessibleHelper;

import java.lang.invoke.VarHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static endfield.Vars2.classHelper;
import static endfield.desktop.DesktopImpl.lookup;

public class DesktopAccessibleHelper implements AccessibleHelper {
	static VarHandle override;
	static Field modifiers;

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

	@Override
	public void makeClassAccessible(Class<?> clazz) {
		try {
			if (modifiers == null) {
				modifiers = classHelper.getField(Class.class, "modifiers");
				modifiers.setAccessible(true);
			}

			char flags = modifiers.getChar(clazz);

			flags &= ~(Modifier.PRIVATE | Modifier.PROTECTED | Modifier.FINAL);
			flags |= Modifier.PUBLIC;

			modifiers.setChar(clazz, flags);
		} catch (Exception e) {
			Log.err("The currently running JVM's java.lang.Class does not contain a modifiers field, so modifiers cannot be modified", e);
		}
	}
}
