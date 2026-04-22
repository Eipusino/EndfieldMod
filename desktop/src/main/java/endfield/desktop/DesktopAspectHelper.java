package endfield.desktop;

import endfield.util.aspector.AspectHelper;
import endfield.util.aspector.accesses.PackageAccessHandler;
import endfield.util.aspector.classes.ClassAccessor;

public class DesktopAspectHelper implements AspectHelper {
	@Override
	public PackageAccessHandler packageAccessHandler(ClassAccessor classAccessor) {
		return new UnsafePackageAccessHandler(classAccessor);
	}
}
