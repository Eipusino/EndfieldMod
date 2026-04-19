package aspector.desktop;

import aspector.AspectHelper;
import aspector.accesses.PackageAccessHandler;
import aspector.classes.ClassAccessor;

public class DesktopAspectHelper implements AspectHelper {
	@Override
	public PackageAccessHandler packageAccessHandler(ClassAccessor classAccessor) {
		return new UnsafePackageAccessHandler(classAccessor);
	}
}
