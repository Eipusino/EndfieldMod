package endfield.util.aspector.classes;

import endfield.util.Arrays2;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.List;

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

	public abstract @Nullable EAnnotatedType<?> annotatedSuperClass();

	public abstract List<ClassDecl<?>> interfaces();

	public abstract List<EAnnotatedType<?>> annotatedInterfaces();

	public abstract List<EField> fields();

	public abstract List<EConstructor<T>> constructors();

	public abstract List<EMethod> methods();

	public abstract List<EAnnotation> annotations();

	public EAnnotation getAnnotation(ClassName annoTypeName) {
		return Arrays2.find(annotations(), a -> a.type.equals(annoTypeName));
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

		return superClass != null && superClass.name.equals(ClassName.ENUM);
	}

	public static class PrimitiveClassDecl<T> extends ClassDecl<T> {
		public final Class<T> clazz;

		public PrimitiveClassDecl(Class<T> type) {
			super(ClassName.byClass(type));
			clazz = type;
		}

		@Override
		public int flags() {
			return Modifier.PUBLIC | Modifier.FINAL;
		}

		@Override
		public @Nullable ClassDecl<?> superClass() {
			return null;
		}

		@Override
		public @Nullable EAnnotatedType<?> annotatedSuperClass() {
			return null;
		}

		@Override
		public List<ClassDecl<?>> interfaces() {
			return List.of();
		}

		@Override
		public List<EAnnotatedType<?>> annotatedInterfaces() {
			return List.of();
		}

		@Override
		public List<EField> fields() {
			return List.of();
		}

		@Override
		public List<EConstructor<T>> constructors() {
			return List.of();
		}

		@Override
		public List<EMethod> methods() {
			return List.of();
		}

		@Override
		public List<EAnnotation> annotations() {
			return List.of();
		}
	}

	public static class ArrayClassDecl<T> extends ClassDecl<T> {
		EField length;

		public ArrayClassDecl(Class<?> type) {
			super(ClassName.byClass(type));
		}

		public ArrayClassDecl(ClassName className) {
			super(className);
		}

		public ArrayClassDecl(ClassDecl<?> classDecl) {
			super(classDecl.name);
		}

		@Override
		public int flags() {
			return Modifier.PUBLIC | Modifier.FINAL;
		}

		@Override
		public @Nullable ClassDecl<?> superClass() {
			return null;
		}

		@Override
		public @Nullable EAnnotatedType<?> annotatedSuperClass() {
			return null;
		}

		@Override
		public List<ClassDecl<?>> interfaces() {
			return List.of();
		}

		@Override
		public List<EAnnotatedType<?>> annotatedInterfaces() {
			return List.of();
		}

		@Override
		public List<EField> fields() {
			if (length == null) length = new EField(
					this,
					"length",
					new EAnnotatedType<>(ClassAccessor.INT, List.of()),
					Modifier.PUBLIC | Modifier.FINAL,
					null,
					List.of()
			);

			return List.of(length);
		}

		@Override
		public List<EConstructor<T>> constructors() {
			return List.of();
		}

		@Override
		public List<EMethod> methods() {
			return List.of();
		}

		@Override
		public List<EAnnotation> annotations() {
			return List.of();
		}
	}
}
