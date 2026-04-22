package endfield.util.aspector;

import endfield.util.aspector.accesses.PackageAccessHandler;
import endfield.util.aspector.classes.BytecodeClassLoader;
import endfield.util.aspector.classes.BytecodeLoader;
import endfield.util.aspector.classes.ClassAccessor;
import endfield.util.aspector.classes.ClassName;
import endfield.util.aspector.classes.ReflectClassAccessor;
import endfield.util.aspector.generate.ClassMaker;
import endfield.util.handler.MethodHandler;
import kotlin.jvm.functions.Function1;

public final class RuntimeAspector {
	private RuntimeAspector() {}

	public static AspectDelegate withMaker(Function1<? super ClassAccessor, ? extends ClassMaker> makerFactory, Function1<? super ClassAccessor, ? extends PackageAccessHandler> accessorFactory, ClassLoader... loaderPaths) {
		ReflectClassAccessor accessor = new ReflectClassAccessor(loaderPaths);

		Aspector aspector = new Aspector(makerFactory.invoke(accessor));
		PackageAccessHandler packageAccessor = accessorFactory.invoke(accessor);

		return new AspectDelegate(accessor, aspector, packageAccessor);
	}

	public static class AspectDelegate {
		private final ClassAccessor accessor;
		private final Aspector aspector;
		private final PackageAccessHandler packageAccessor;

		private BytecodeLoader aspectLoader = new BytecodeClassLoader(getClass().getClassLoader());

		public AspectDelegate(ClassAccessor access, Aspector aspect, PackageAccessHandler packageAccess) {
			accessor = access;
			aspector = aspect;
			packageAccessor = packageAccess;
		}

		public void use(BytecodeLoader loader) {
			aspectLoader = loader;
		}

		public <T> Class<T> open(Class<T> target) {
			Class<T> clazz = packageAccessor.getPackageAccessClass(target);
			if (clazz == null) throw new UnsupportedOperationException("Open package not found");
			return clazz;
		}

		public <T> DeclDelegate<T> applyAspect(Class<?> aspectDeclare, Class<T> targetClass) {
			return new DeclDelegate<>(aspector.applyAspect(
					accessor.getClassDecl(ClassName.byClass(aspectDeclare)),
					accessor.getClassDecl(ClassName.byClass(targetClass))
			));
		}

		public final class DeclDelegate<T> {
			AspectDecl<T> decl;
			Class<T> aspectClass;

			public DeclDelegate(AspectDecl<T> d) {
				decl = d;
			}

			public Class<T> aspectClass() {
				load();
				return aspectClass;
			}

			public void load(BytecodeLoader loader) {
				if (aspectClass == null) aspectClass = decl.load(loader);
			}

			public ClassName className() {
				return decl.getClassName();
			}

			public byte[] bytecode() {
				return decl.getBytecode();
			}

			public void load() {
				load(aspectLoader);
			}

			public T instance(Object... args) {
				return MethodHandler.newInstanceDefault(aspectClass(), args);
			}

			public T instanceTyped(Class<?>[] parameterTypes, Object... args) {
				return MethodHandler.newInstanceTypedDefault(aspectClass(), parameterTypes, args);
			}
		}
	}
}
