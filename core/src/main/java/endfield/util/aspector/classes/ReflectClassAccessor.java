package endfield.util.aspector.classes;

import endfield.util.Arrays2;
import endfield.util.Collections2;
import endfield.util.Constant;
import endfield.util.Reflects;
import endfield.util.aspector.classes.AnnotationValue.ArrayValue;
import endfield.util.aspector.classes.AnnotationValue.EnumValue;
import endfield.util.aspector.classes.AnnotationValue.NestedAnnotationValue;
import endfield.util.aspector.classes.AnnotationValue.TypeValue;
import endfield.util.aspector.classes.AnnotationValue.Value;
import endfield.util.aspector.classes.ClassDecl.ArrayClassDecl;
import endfield.util.handler.MethodHandler;
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.jvm.functions.Function1;
import mindustry.Vars;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReflectClassAccessor implements ClassAccessor {
	static final Function1<Annotation, EAnnotation> asEAnnotation = ReflectClassAccessor::asEAnnotation;

	final List<ClassLoader> attachedClassLoader;
	final Map<ClassName, ClassDecl<?>> loadedDeclMap = new HashMap<>();

	public ReflectClassAccessor(ClassLoader... loaders) {
		attachedClassLoader = loaders.length == 0 ? Collections2.asList(Vars.mods.mainLoader()) : Collections2.asList(loaders);
	}

	public void attachClassLoader(ClassLoader classLoader) {
		if (!attachedClassLoader.contains(classLoader)) {
			attachedClassLoader.add(classLoader);
		}
	}

	public static EAnnotation asEAnnotation(Annotation anno) {
		Class<?> annoType = anno.getClass().getInterfaces()[0];
		Map<String, AnnotationValue<?, ?>> values = new HashMap<>();

		for (Method m : annoType.getMethods()) {
			if (m.getParameters().length != 0) continue;

			String name = m.getName();
			if (!name.equals("toString") && !name.equals("hashCode") && !name.equals("annotationType")) {
				values.put(name, handleAnnotationValue(MethodHandler.invoke(anno, m, Constant.EMPTY_OBJECT)));
			}
		}

		return new EAnnotation(ClassName.byClass(annoType), values);
	}

	static AnnotationValue<?, ?> handleAnnotationValue(Object value) {
		if (value instanceof byte[] || value instanceof short[] || value instanceof int[] || value instanceof long[]
				|| value instanceof float[] || value instanceof double[]
				|| value instanceof boolean[] || value instanceof char[]
				|| value instanceof String || value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long
				|| value instanceof Float || value instanceof Double || value instanceof Boolean || value instanceof Character) {
			return new Value<>(value);
		} else if (value instanceof Class<?> c) {
			return new TypeValue(ClassName.byClass(c));
		} else if (value instanceof Enum<?> e) {
			return new EnumValue<>(ClassName.byClass(value.getClass()), e.name());
		} else if (value instanceof Object[] a) {
			return new ArrayValue<>(ArraysKt.map(a, o -> (AnnotationValue<?, ?>) handleAnnotationValue(o)));
		} else if (value instanceof Annotation a) {
			return new NestedAnnotationValue(asEAnnotation(a));
		} else throw new IllegalArgumentException("Unsupported value type: " + value);
	}

	Class<?> loadClass(ClassName className) {
		return switch (className.descriptor) {
			case "V" -> Void.TYPE;
			case "B" -> Byte.class;
			case "S" -> Short.class;
			case "I" -> Integer.class;
			case "J" -> Long.class;
			case "F" -> Float.class;
			case "D" -> Double.class;
			case "C" -> Character.class;
			case "Z" -> Boolean.class;
			default -> {
				if (className.isArray()) {
					Class<?> componentType = loadClass(className.componentName());
					yield Array.newInstance(componentType, 0).getClass();
				}

				String name = className.name();

				Class<?> result = Collections2.firstNotNullOfOrNull(attachedClassLoader, loader -> {
					try {
						return loader.loadClass(name);
					} catch (ClassNotFoundException e) {
						return null;
					}
				});

				yield Objects.requireNonNull(result);
			}
		};
	}

	@Override
	public byte[] getBytes(ClassName className) {
		Class<?> clazz = loadClass(className);

		String path = clazz.getName().replace('.', '/') + ".class";

		return new byte[0];
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ClassDecl<T> getClassDecl(ClassName className) {
		return (ClassDecl<T>) switch (className.descriptor) {
			case "V" -> VOID;
			case "B" -> BYTE;
			case "S" -> SHORT;
			case "I" -> INT;
			case "J" -> LONG;
			case "F" -> FLOAT;
			case "D" -> DOUBLE;
			case "C" -> CHAR;
			case "Z" -> BOOLEAN;
			default -> loadedDeclMap.computeIfAbsent(className, cn -> {
				if (cn.isArray()) {
					return new ArrayClassDecl<>(loadClass(cn.componentName()));
				}

				Class<?> clazz = loadClass(cn);
				return new ReflectClassDecl<>(this, clazz);
			});
		};
	}

	static class ReflectClassDecl<T> extends ClassDecl<T> {
		final ClassAccessor accessor;
		final Class<T> clazz;

		ClassDecl<?> superClass;
		EAnnotatedType<?> annotatedSuperClass;
		List<ClassDecl<?>> interfaces;
		List<EAnnotatedType<?>> annotatedInterfaces;
		List<EField> fields;
		List<EConstructor<T>> constructors;
		List<EMethod> methods;
		List<EAnnotation> annotations;

		public ReflectClassDecl(ClassAccessor access, Class<T> type) {
			super(ClassName.byClass(type));
			accessor = access;
			clazz = type;
		}

		@Override
		public int flags() {
			return clazz.getModifiers();
		}

		@Override
		public @Nullable ClassDecl<?> superClass() {
			if (superClass == null) {
				Class<?> superclass = clazz.getSuperclass();
				superClass = superclass == null ? null : toDecl(superclass);
			}
			return superClass;
		}

		@Override
		public @Nullable EAnnotatedType<?> annotatedSuperClass() {
			if (annotatedSuperClass == null) annotatedSuperClass = getSuperClassWithAnnotations(clazz);
			return annotatedSuperClass;
		}

		@Override
		public List<ClassDecl<?>> interfaces() {
			if (interfaces == null) interfaces = ArraysKt.map(clazz.getInterfaces(), this::toDecl);
			return interfaces;
		}

		@Override
		public List<EAnnotatedType<?>> annotatedInterfaces() {
			if (annotatedInterfaces == null) {
				List<EAnnotatedType<?>> supertypes = getSuperTypesWithAnnotations(clazz);
				annotatedInterfaces = CollectionsKt.filter(supertypes, t -> t.type.isInterface());
			}
			return annotatedInterfaces;
		}

		@Override
		public List<EField> fields() {
			if (fields == null) fields = ArraysKt.map(clazz.getDeclaredFields(), f -> new EField(
					toDecl(f.getDeclaringClass()),
					f.getName(),
					toAnnoType(f.getAnnotatedType()),
					f.getModifiers(),
					null,
					ArraysKt.map(f.getAnnotations(), asEAnnotation)
			));
			return fields;
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<EConstructor<T>> constructors() {
			if (constructors == null) constructors = ArraysKt.map(clazz.getDeclaredConstructors(), c -> new EConstructor<>(
					toDecl((Class<T>) c.getDeclaringClass()),
					ArraysKt.map(c.getParameters(), p -> new Parameter(p.getName(), toAnnoType(p.getAnnotatedType()), ArraysKt.map(p.getAnnotations(), asEAnnotation))),
					c.getModifiers(),
					ArraysKt.map(c.getAnnotations(), asEAnnotation)
			));
			return constructors;
		}

		@Override
		public List<EMethod> methods() {
			if (methods == null) methods = ArraysKt.map(clazz.getDeclaredMethods(), m -> new EMethod(
					toDecl(m.getDeclaringClass()),
					m.getName(),
					ArraysKt.map(m.getParameters(), p -> new Parameter(p.getName(), toAnnoType(p.getAnnotatedType()), ArraysKt.map(p.getAnnotations(), asEAnnotation))),
					toAnnoType(m.getAnnotatedReturnType()),
					m.getModifiers(),
					ArraysKt.map(m.getAnnotations(), asEAnnotation)
			));
			return methods;
		}

		@Override
		public List<EAnnotation> annotations() {
			if (annotations == null) annotations = ArraysKt.map(clazz.getDeclaredAnnotations(), asEAnnotation);

			return annotations;
		}

		<U> ClassDecl<U> toDecl(Class<U> clazz) {
			return accessor.getClassDecl(ClassName.byClass(clazz));
		}

		@SuppressWarnings("unchecked")
		<U> EAnnotatedType<U> toAnnoType(AnnotatedType annotatedType) {
			return new EAnnotatedType<>(
					toDecl((Class<U>) asClass(annotatedType.getType())),
					ArraysKt.map(annotatedType.getAnnotations(), asEAnnotation)
			);
		}

		Class<?> asClass(Type type) {
			if (type instanceof ParameterizedType p) return (Class<?>) p.getRawType();
			if (type instanceof Class<?> c) return c;
			throw new UnsupportedOperationException("Unsupported type " + type.getClass().getName());
		}

		EAnnotatedType<?> getSuperClassWithAnnotations(Class<?> clazz) {
			Class<?> superclass = clazz.getSuperclass();

			if (superclass == null) return null;

			List<Annotation> list = new ArrayList<>();

			Class<?> superClass = Arrays2.find(Reflects.getDirectSuperclasses(clazz), c -> !c.isInterface());
			if (superClass != null) Collections.addAll(list, superClass.getAnnotations());

			var annotatedSuperclass = clazz.getAnnotatedSuperclass();
			if (annotatedSuperclass != null) Collections.addAll(list, annotatedSuperclass.getAnnotations());

			return new EAnnotatedType<>(toDecl(superclass), CollectionsKt.map(list, asEAnnotation));
		}

		List<EAnnotatedType<?>> getSuperTypesWithAnnotations(Class<?> clazz) {
			Map<Class<?>, List<Annotation>> typeAnnotations = new HashMap<>();

			for (Class<?> type : Reflects.getDirectSuperclasses(clazz)) {
				Collections.addAll(Collections2.getOrPut(typeAnnotations, type, ArrayList::new), type.getAnnotations());
			}

			List<AnnotatedType> javaTypes = new ArrayList<>();

			AnnotatedType annotatedSuperclass = clazz.getAnnotatedSuperclass();
			if (annotatedSuperclass != null) javaTypes.add(annotatedSuperclass);

			Collections.addAll(javaTypes, clazz.getAnnotatedInterfaces());

			for (AnnotatedType type : javaTypes) {
				Collections.addAll(Collections2.getOrPut(typeAnnotations, (Class<?>) type.getType(), ArrayList::new), type.getAnnotations());
			}

			return MapsKt.map(typeAnnotations, entry -> {
				Class<?> type = entry.getKey();
				List<EAnnotation> annotations = CollectionsKt.map(entry.getValue(), asEAnnotation);
				return new EAnnotatedType<>(toDecl(type), annotations);
			});
		}
	}
}
