package aspector;

import arc.func.Func;
import aspector.accesses.PackageAccessHandler;
import aspector.classes.BytecodeClassLoader;
import aspector.classes.BytecodeLoader;
import aspector.classes.ClassAccessor;
import aspector.classes.ClassName;
import aspector.classes.ReflectClassAccessor;
import aspector.generate.ClassMaker;
import endfield.util.handler.MethodHandler;

import java.util.List;

import static endfield.util.Arrays2.map;

public final class RuntimeAspector {
	private RuntimeAspector() {}

	public static AspectDelegate withMaker(Func<ClassAccessor, ClassMaker> makerFactory, Func<ClassAccessor, PackageAccessHandler> accessorFactory, ClassLoader... loaderPaths) {
		ReflectClassAccessor accessor = new ReflectClassAccessor(loaderPaths);

		Aspector aspector = new Aspector(makerFactory.get(accessor));
		var packageAccessor = accessorFactory.get(accessor);

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

			public T instance(Object... arg) {
				return MethodHandler.newInstanceDefault(aspectClass(), arg);
			}

			public T instanceTyped(TypePair<?>... args) {
				List<?> argsList = map(args, it -> it.value);
				List<Class<?>> argsTypes = map(args, it -> it.type);
				return MethodHandler.newInstanceDefault(aspectClass(), argsList.toArray());
			}
		}
	}
}
