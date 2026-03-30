package endfield.util;

import dynamilize.ArgumentList;
import dynamilize.Function;
import dynamilize.FunctionType;
import dynamilize.IFunctionEntry;

import java.lang.reflect.Method;

import static endfield.Vars2.platformImpl;

public class JavaMethodReference implements IFunctionEntry {
	final Method method;
	final MethodAccessor accessor;
	final String name;
	final Function<?, ?> func;
	final FunctionType type;

	public JavaMethodReference(Method met) {
		method = met;
		accessor = platformImpl.methodAccessor(met);
		name = met.getName();
		type = FunctionType.inst(met);

		Class<?>[] parameterTypes = met.getParameterTypes();
		if (parameterTypes.length == 1 && ArgumentList.class.isAssignableFrom(parameterTypes[0])) {
			func = accessor::invoke;
		} else {
			func = (self, args) -> accessor.invoke(self, args.args());
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S, R> Function<S, R> getFunc() {
		return (Function<S, R>) func;
	}

	@Override
	public FunctionType getType() {
		return type;
	}
}
