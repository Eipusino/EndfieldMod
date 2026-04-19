package aspector.classes;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.List;

public interface ClassAccessor {
	ClassDecl<Void> voidDecl = new PrimitiveClassDecl<>(void.class);
	ClassDecl<Byte> byteDecl = new PrimitiveClassDecl<>(byte.class);
	ClassDecl<Short> shortDecl = new PrimitiveClassDecl<>(short.class);
	ClassDecl<Integer> intDecl = new PrimitiveClassDecl<>(int.class);
	ClassDecl<Long> longDecl = new PrimitiveClassDecl<>(long.class);
	ClassDecl<Float> floatDecl = new PrimitiveClassDecl<>(float.class);
	ClassDecl<Double> doubleDecl = new PrimitiveClassDecl<>(double.class);
	ClassDecl<Character> charDecl = new PrimitiveClassDecl<>(char.class);
	ClassDecl<Boolean> booleanDecl = new PrimitiveClassDecl<>(boolean.class);

	<T> ClassDecl<T> getClassDecl(ClassName className);

	byte[] getBytes(ClassName className);

	class PrimitiveClassDecl<T> extends ClassDecl<T> {
		public final Class<T> clazz;

		PrimitiveClassDecl(Class<T> type) {
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
		public @Nullable AnnotatedType<?> annotatedSuperClass() {
			return null;
		}

		@Override
		public List<ClassDecl<?>> interfaces() {
			return List.of();
		}

		@Override
		public List<AnnotatedType<?>> annotatedInterfaces() {
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
}
