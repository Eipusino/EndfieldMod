package endfield.util.aspector;

import endfield.util.aspector.accesses.PackageAccessHandler;
import endfield.util.aspector.classes.ClassAccessor;

public interface AspectHelper {
	PackageAccessHandler packageAccessHandler(ClassAccessor classAccessor);
}
