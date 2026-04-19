package aspector;

import aspector.accesses.PackageAccessHandler;
import aspector.classes.ClassAccessor;

public interface AspectHelper {
	PackageAccessHandler packageAccessHandler(ClassAccessor classAccessor);
}
