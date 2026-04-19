package aspector.classes;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.List;

import static endfield.util.Arrays2.find;

public abstract class ClassDecl<T> {
	public static final int BRIDGE = 0x00000040;
	public static final int VARARGS = 0x00000080;
	public static final int SYNTHETIC = 0x00001000;
	public static final int ANNOTATION = 0x00002000;
	public static final int ENUM = 0x00004000;
	public static final int MANDATED = 0x00008000;

	public final ClassName name;

	public ClassDecl(ClassName cName) {
		name = cName;
	}

	public abstract int flags();

	public abstract @Nullable ClassDecl<?> superClass();

	public abstract @Nullable AnnotatedType<?> annotatedSuperClass();

	public abstract List<ClassDecl<?>> interfaces();

	public abstract List<AnnotatedType<?>> annotatedInterfaces();

	public abstract List<EField> fields();

	public abstract List<EConstructor<T>> constructors();

	public abstract List<EMethod> methods();

	public abstract List<EAnnotation> annotations();

	public EAnnotation getAnnotation(ClassName annoTypeName) {
		return find(annotations(), a -> a.type.equals(annoTypeName));
	}

	public final boolean isPublic() {
		return (flags() & Modifier.PUBLIC) != 0;
	}

	public final boolean isProtected() {
		return (flags() & Modifier.PROTECTED) != 0;
	}

	public final boolean isPrivate() {
		return (flags() & Modifier.PRIVATE) != 0;
	}

	public final boolean isInterface() {
		return (flags() & Modifier.INTERFACE) != 0;
	}

	public final boolean isAbstract() {
		return (flags() & Modifier.ABSTRACT) != 0;
	}

	public final boolean isFinal() {
		return (flags() & Modifier.FINAL) != 0;
	}

	public final boolean isPrimitive() {
		return name.isPrimitive();
	}

	public final boolean isArray() {
		return name.isArray();
	}

	public final boolean isEnum() {
		if ((flags() & ENUM) == 0) return false;

		ClassDecl<?> superClass = superClass();

		return superClass != null && superClass.name.equals(ClassName.jEnum);
	}
}
