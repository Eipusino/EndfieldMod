package endfield.core;

import arc.util.Log;
import endfield.util.CollectionObjectMap;
import endfield.util.ExtraVariable;
import org.jetbrains.annotations.TestOnly;

import java.lang.invoke.MethodHandle;
import java.util.Map;

import static endfield.Vars2.platformImpl;

/** Classes for testing purposes only, do not use. */
@TestOnly
public class Test implements Cloneable, ExtraVariable {
	private static short count;

	public Map<String, Object> extraVar = new CollectionObjectMap<>(String.class, Object.class);
	public short id;

	public Object target;
	public int index;

	public Test() {
		id = count++;
	}

	public static void test() throws Throwable {}

	public Test copy() {
		try {
			return (Test) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public Map<String, Object> extra() {
		return extraVar;
	}

	/*@SuppressWarnings("removal")
	@Override
	protected void finalize() throws Throwable {
		Log.info(this + " destroyed");
	}*/
}
