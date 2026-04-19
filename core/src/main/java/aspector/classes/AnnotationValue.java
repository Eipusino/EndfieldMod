package aspector.classes;

import endfield.util.Pair;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.NoSuchElementException;

import static endfield.util.Arrays2.map;

public abstract class AnnotationValue<T, R> {
	AnnotationValue() {}

	public abstract ClassName getType();

	/**
	 * Will load some class or instance some object.
	 * <p>Shouldn't be call most of the time when a pure compile-time or mixin environment.
	 */
	public abstract T value();

	/**
	 * Get the annotation value raw declare, use pure compile indicated.
	 * <p>Don't need to load target class. use for mixin or compile plugin.
	 */
	public abstract R rawValue();

	public static class Value<T> extends AnnotationValue<T, T> {
		public final T value;

		public Value(T v) {
			value = v;
		}

		@Override
		public ClassName getType() {
			return ClassName.jClass;
		}

		@Override
		public T value() {
			return value;
		}

		@Override
		public T rawValue() {
			return value;
		}
	}

	public static class TypeValue extends AnnotationValue<Class<?>, ClassName> {
		public final ClassName className;

		public TypeValue(ClassName cn) {
			className = cn;
		}

		@Override
		public ClassName getType() {
			return ClassName.jClass;
		}

		@Override
		public Class<?> value() {
			try {
				return Class.forName(className.name());
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public ClassName rawValue() {
			return className;
		}
	}

	public static class ArrayValue<V, T extends AnnotationValue<V, ?>> extends AnnotationValue<List<V>, List<T>> {
		public final List<V> value;
		public final List<T> rawValue;

		public ArrayValue(List<T> values) {
			value = map(values, t -> t.value());
			rawValue = values;
		}

		@Override
		public ClassName getType() {
			return ClassName.jClass.arrayName();
		}

		@Override
		public List<V> value() {
			return value;
		}

		@Override
		public List<T> rawValue() {
			return rawValue;
		}
	}

	public static class NestedAnnotationValue extends AnnotationValue<Annotation, EAnnotation> {
		public final EAnnotation rawValue;

		Annotation value;

		public NestedAnnotationValue(EAnnotation anno) {
			rawValue = anno;
		}

		@Override
		public ClassName getType() {
			return ClassName.jClass;
		}

		@Override
		public Annotation value() {
			if (value == null) {
				try {
					Class<?> annoType = Class.forName(rawValue.type.name());
					value = (Annotation) java.lang.reflect.Proxy.newProxyInstance(
							annoType.getClassLoader(),
							new Class[]{annoType},
							(obj, method, args) -> {
								AnnotationValue<?, ?> v = rawValue().getValue(method.getName());
								return v != null ? v.value() : method.invoke(obj, args);
							}
					);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
			return value;
		}

		@Override
		public EAnnotation rawValue() {
			return rawValue;
		}
	}

	public static class EnumValue<T extends Enum<T>> extends AnnotationValue<T, Pair<ClassName, String>> {
		public final ClassName enumClassName;
		public final String enumConstName;

		public EnumValue(ClassName className, String constName) {
			enumClassName = className;
			enumConstName = constName;
		}

		@Override
		public ClassName getType() {
			return enumClassName;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T value() {
			try {
				for (Object o : Class.forName(enumClassName.name()).getEnumConstants()) {
					if (o instanceof Enum<?> e && e.name().equals(enumConstName)) return (T) o;
				}

				throw new NoSuchElementException(enumConstName);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Pair<ClassName, String> rawValue() {
			return Pair.of(enumClassName, enumConstName);
		}
	}
}
