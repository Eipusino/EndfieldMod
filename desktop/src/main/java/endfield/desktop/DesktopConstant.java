package endfield.desktop;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static endfield.desktop.DesktopImpl.lookup;

public final class DesktopConstant {
	static final MethodHandle clone;

	static {
		try {
			clone = lookup.findVirtual(Object.class, "clone", MethodType.methodType(Object.class));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
