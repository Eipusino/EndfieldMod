package endfield.util.aspector.accesses;

import endfield.util.aspector.classes.ClassAccessor;
import endfield.util.aspector.classes.ClassDecl;
import endfield.util.aspector.classes.ClassElement;
import endfield.util.aspector.classes.ClassName;
import endfield.util.aspector.classes.EConstructor;
import endfield.util.aspector.classes.EMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public abstract class PackageAccessHandler {
	protected final ClassAccessor classAccessor;

	public PackageAccessHandler(ClassAccessor access) {
		classAccessor = access;
	}

	public String genPackageAccessClassName(Class<?> accessTarget) {
		return accessTarget.getName() + "$PackageAccess";
	}

	@SuppressWarnings("unchecked")
	public <T> Class<T> getPackageAccessClass(Class<T> accessTarget) {
		if (accessTarget.getAnnotation(PackageAccessor.class) != null) return accessTarget;

		checkAccessible(accessTarget);

		ClassName name = ClassName.byName(genPackageAccessClassName(accessTarget));

		try {
			return (Class<T>) Class.forName(name.name());
		} catch (ClassNotFoundException e) {
			ClassName targetName = ClassName.byClass(accessTarget);
			ClassDecl<T> targetDecl = classAccessor.getClassDecl(targetName);

			AccessBuilder builder = new AccessBuilder(name, targetName);

			for (EMethod m : targetDecl.methods()) {
				if ((m.flags & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL)) == 0)
					builder.registerEnhanceMethod(m);
			}
			for (EConstructor<?> c : targetDecl.constructors()) {
				if ((c.flags & (Modifier.PRIVATE | Modifier.FINAL)) == 0)
					builder.registerEnhanceConstructor(c);
			}

			byte[] bytecode = genPackageAccessClass(builder);

			/*try (FileOutputStream stream = new FileOutputStream(name.simpleName() + ".class")) {
				stream.write(bytecode);
			} catch (IOException ex) {
				Log.err(ex);
			}*/

			return (Class<T>) loadClass(
					name,
					bytecode,
					accessTarget
			);
		}
	}

	protected <T> void checkAccessible(Class<T> accessTarget) {
		if (accessTarget.isPrimitive())
			throw new IllegalArgumentException("Cannot enhance a primitive type.");
		if (accessTarget.isInterface())
			throw new IllegalArgumentException("Cannot enhance an interface type: " + accessTarget + ".");
		if ((accessTarget.getModifiers() & (Modifier.FINAL | Modifier.PRIVATE)) != 0)
			throw new IllegalArgumentException("Cannot enhance access class with modifiers final or private.");
	}

	protected abstract byte[] genPackageAccessClass(AccessBuilder builder);

	protected abstract Class<?> loadClass(ClassName className, byte[] bytecode, Class<?> accessTarget);

	public static final class AccessBuilder {
		public final ClassName className;
		public final ClassName accessTarget;

		public final List<ClassElement> enhanceElements = new ArrayList<>();

		public AccessBuilder(ClassName cName, ClassName accTar) {
			className = cName;
			accessTarget = accTar;
		}

		public boolean registerEnhanceMethod(EMethod method) {
			return enhanceElements.add(method);
		}

		public boolean registerEnhanceConstructor(EConstructor<?> constructor) {
			return enhanceElements.add(constructor);
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface PackageAccessor {
	}
}
