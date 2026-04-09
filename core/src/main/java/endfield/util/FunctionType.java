package endfield.util;

import java.lang.invoke.MethodType;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static endfield.util.Reflects.toTypes;
import static endfield.util.Reflects.unwrapped;

/**
 * Function type encapsulates objects, recording the parameter types of functions for comparison and
 * search.
 *
 * @author EBwilson
 */
public class FunctionType {
	/**
	 * Reuse the recycling area capacity, changing the value usually does not require setting, but if you
	 * may need large-scale recursion or a large number of concurrent calls, you may need to set this limit
	 * to a higher value.
	 */
	public static int maxRecycle = 4096;

	static final Stack<FunctionType> recyclePool = new Stack<>();

	Class<?>[] paramType;
	int hash;

	FunctionType(Class<?>... types) {
		paramType = types;
		hash = Arrays.hashCode(types);
	}

	public static FunctionType inst(List<Class<?>> paramType) {
		return inst(paramType.toArray(Constant.EMPTY_CLASS));
	}

	public static synchronized FunctionType inst(Class<?>... paramType) {
		if (recyclePool.isEmpty()) return new FunctionType(paramType);

		FunctionType res = recyclePool.pop();
		res.paramType = paramType;
		res.hash = Arrays.hashCode(paramType);
		return res;
	}

	public static FunctionType inst(Method method) {
		return inst(method.getParameterTypes());
	}

	public static FunctionType inst(Object... param) {
		return inst(unwrapped(toTypes(param)));
	}

	public static FunctionType inst(FunctionType type) {
		return inst(type.paramType);
	}

	public static FunctionType from(MethodType type) {
		return inst(type.parameterArray());
	}

	public static FunctionType from(Executable method) {
		return inst(method.getParameterTypes());
	}

	public static FunctionType generic(int argCount) {
		Class<?>[] types = new Class[argCount];
		Arrays.fill(types, void.class);
		return inst(types);
	}

	public static String signature(Method method) {
		return method.getName() + from(method);
	}

	public static String signature(String name, Class<?>... types) {
		return name + inst(types);
	}

	public static String signature(String name, FunctionType type) {
		return name + type;
	}

	public boolean match(Object... args) {
		return match(unwrapped(toTypes(args)));
	}

	public boolean match(Class<?>... types) {
		return Reflects.match(paramType, types);
	}

	public boolean match(FunctionType type) {
		return match(type.paramType);
	}

	public Class<?>[] paramType() {
		return paramType;
	}

	public void recycle() {
		if (recyclePool.size() >= maxRecycle) return;

		paramType = Constant.EMPTY_CLASS;
		hash = -1;
		recyclePool.push(this);
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || obj instanceof FunctionType that && paramType.length == that.paramType.length && hashCode() == obj.hashCode();
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();

		int max = paramType.length;

		buf.append('(');
		int i = 0;
		while (true) {
			buf.append(paramType[i++].getName());
			if (i == max) return buf.append(')').toString();
			buf.append(',');
		}
	}
}
