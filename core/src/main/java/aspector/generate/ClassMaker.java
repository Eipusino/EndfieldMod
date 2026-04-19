package aspector.generate;

import arc.func.Cons;
import aspector.AspectDecl;
import aspector.classes.BytecodeLoader;
import aspector.classes.ClassAccessor;
import aspector.classes.ClassDecl;
import aspector.classes.ClassName;
import aspector.classes.ClassElement;
import aspector.classes.EAspectMethod;
import aspector.classes.EConstructor;
import aspector.classes.EField;
import aspector.classes.EMethod;

import java.util.ArrayList;
import java.util.List;

public abstract class ClassMaker {
	public final ClassAccessor bytesAccessor;

	public ClassMaker(ClassAccessor accessor) {
		bytesAccessor = accessor;
	}

	public <T> AspectResult<T> makeClass(ClassDecl<?> aspectDecl, ClassDecl<T> targetClass, Cons<AspectBuilder> scope) {
		AspectBuilder builder = new AspectBuilder(generateClassName(aspectDecl, targetClass), targetClass.flags(), targetClass.name, aspectDecl.name);
		scope.get(builder);

		return new AspectResult<>(builder);
	}

	public abstract ClassName generateClassName(ClassDecl<?> aspectImpl, ClassDecl<?> targetClass);

	public abstract byte[] generateBytecode(AspectBuilder builder);

	public abstract Class<?> loadClass(BytecodeLoader loader, ClassName className, byte[] bytecode);

	public final class AspectResult<T> extends AspectDecl<T> {
		private byte[] bytecode;

		public AspectResult(AspectBuilder builder) {
			super(builder);
		}

		@Override
		public ClassName getClassName() {
			return context.className;
		}

		@Override
		public byte[] getBytecode() {
			if (bytecode == null) bytecode = generateBytecode(context);

			return bytecode;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<T> load(BytecodeLoader loader) {
			return (Class<T>) loadClass(loader, getClassName(), getBytecode());
		}
	}

	public static final class AspectBuilder {
		public final ClassName className;
		public final int accessFlags;
		public final ClassName superClass;
		public final ClassName aspectDecl;

		public final List<ClassName> stubTypes = new ArrayList<>();
		public final List<ClassName> interfaces = new ArrayList<>();

		public final List<EAspectMethod> aspectElements = new ArrayList<>();
		public final List<ClassElement> implElements = new ArrayList<>();
		public final List<ClassElement> superElements = new ArrayList<>();

		public AspectBuilder(ClassName classn, int flags, ClassName superc, ClassName aspectd) {
			className = classn;
			accessFlags = flags;
			superClass = superc;
			aspectDecl = aspectd;
		}

		public void registerStubSpec(ClassDecl<?> stub) {
			stubTypes.add(stub.name);
		}

		public void registerInterfaces(ClassDecl<?> inter) {
			interfaces.add(inter.name);
		}

		public void registerAspectMethod(EAspectMethod method) {
			aspectElements.add(method);
		}

		public void registerImplField(EField field) {
			implElements.add(field);
		}

		public void registerImplMethod(EMethod method) {
			implElements.add(method);
		}

		public void registerImplConstructor(EConstructor<?> constructor) {
			implElements.add(constructor);
		}

		public void registerSuperField(EField field) {
			superElements.add(field);
		}

		public void registerSuperMethod(EMethod method) {
			superElements.add(method);
		}

		public void registerSuperConstructor(EConstructor<?> constructor) {
			superElements.add(constructor);
		}
	}
}
