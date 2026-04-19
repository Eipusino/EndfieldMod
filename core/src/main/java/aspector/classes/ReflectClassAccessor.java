package aspector.classes;

import arc.func.Func;
import aspector.classes.AnnotationValue.ArrayValue;
import aspector.classes.AnnotationValue.EnumValue;
import aspector.classes.AnnotationValue.NestedAnnotationValue;
import aspector.classes.AnnotationValue.TypeValue;
import aspector.classes.AnnotationValue.Value;
import endfield.util.Constant;
import endfield.util.handler.MethodHandler;
import mindustry.Vars;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static endfield.util.Arrays2.asList;
import static endfield.util.Arrays2.filter;
import static endfield.util.Arrays2.find;
import static endfield.util.Arrays2.firstNotNullOfOrNull;
import static endfield.util.Arrays2.getOrPut;
import static endfield.util.Arrays2.map;
import static endfield.util.Reflects.getDirectSuperclasses;

public class ReflectClassAccessor implements ClassAccessor {
	static final Func<Annotation, EAnnotation> asEAnnotation = ReflectClassAccessor::asEAnnotation;

	final List<ClassLoader> attachedClassLoader;
	final Map<ClassName, ClassDecl<?>> loadedDeclMap = new HashMap<>();

	public ReflectClassAccessor(ClassLoader... loaders) {
		attachedClassLoader = loaders.length == 0 ? asList(Vars.mods.mainLoader()) : asList(loaders);
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
			return new ArrayValue<>(map(a, o -> (AnnotationValue<?, ?>) handleAnnotationValue(o)));
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

				Class<?> result = firstNotNullOfOrNull(attachedClassLoader, loader -> {
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
		ClassLoader loader = clazz.getClassLoader();

		String path = clazz.getName().replace('.', '/') + ".class";

		try (InputStream stream = loader.getResourceAsStream(path)) {
			return Objects.requireNonNull(stream).readAllBytes();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ClassDecl<T> getClassDecl(ClassName className) {
		return (ClassDecl<T>) switch (className.descriptor) {
			case "V" -> voidDecl;
			case "B" -> byteDecl;
			case "S" -> shortDecl;
			case "I" -> intDecl;
			case "J" -> longDecl;
			case "F" -> floatDecl;
			case "D" -> doubleDecl;
			case "C" -> charDecl;
			case "Z" -> booleanDecl;
			default -> loadedDeclMap.computeIfAbsent(className, cn -> {
				if (cn.isArray()) {
					return new ArrayClassDecl<>(loadClass(cn.componentName()));
				}

				Class<?> clazz = loadClass(cn);
				return new ReflectClassDecl<>(this, clazz);
			});
		};
	}

	static class ArrayClassDecl<T> extends ClassDecl<T> {
		public final Class<?> clazz;

		public ArrayClassDecl(Class<?> type) {
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
			return List.of(new EField(
					this,
					"length",
					new AnnotatedType<>(intDecl, List.of()),
					Modifier.PUBLIC | Modifier.FINAL,
					null,
					List.of()
			));
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

	static class ReflectClassDecl<T> extends ClassDecl<T> {
		final ClassAccessor accessor;
		final Class<T> clazz;

		ClassDecl<?> superClass;
		AnnotatedType<?> annotatedSuperClass;
		List<ClassDecl<?>> interfaces;
		List<AnnotatedType<?>> annotatedInterfaces;
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
		public @Nullable AnnotatedType<?> annotatedSuperClass() {
			if (annotatedSuperClass == null) annotatedSuperClass = getSuperClassWithAnnotations(clazz);
			return annotatedSuperClass;
		}

		@Override
		public List<ClassDecl<?>> interfaces() {
			if (interfaces == null) interfaces = map(clazz.getInterfaces(), this::toDecl);
			return interfaces;
		}

		@Override
		public List<AnnotatedType<?>> annotatedInterfaces() {
			if (annotatedInterfaces == null) {
				List<AnnotatedType<?>> supertypes = getSuperTypesWithAnnotations(clazz);
				annotatedInterfaces = filter(supertypes, t -> t.type.isInterface());
			}
			return annotatedInterfaces;
		}

		@Override
		public List<EField> fields() {
			if (fields == null) fields = map(clazz.getDeclaredFields(), f -> new EField(
					toDecl(f.getDeclaringClass()),
					f.getName(),
					toAnnoType(f.getAnnotatedType()),
					f.getModifiers(),
					null,
					map(f.getAnnotations(), asEAnnotation)
			));
			return fields;
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<EConstructor<T>> constructors() {
			if (constructors == null) constructors = map(clazz.getDeclaredConstructors(), c -> new EConstructor<>(
					toDecl((Class<T>) c.getDeclaringClass()),
					map(c.getParameters(), p -> new Parameter(p.getName(), toAnnoType(p.getAnnotatedType()), map(p.getAnnotations(), asEAnnotation))),
					c.getModifiers(),
					map(c.getAnnotations(), asEAnnotation)
			));
			return constructors;
		}

		@Override
		public List<EMethod> methods() {
			if (methods == null) methods = map(clazz.getDeclaredMethods(), m -> new EMethod(
					toDecl(m.getDeclaringClass()),
					m.getName(),
					map(m.getParameters(), p -> new Parameter(p.getName(), toAnnoType(p.getAnnotatedType()), map(p.getAnnotations(), asEAnnotation))),
					toAnnoType(m.getAnnotatedReturnType()),
					m.getModifiers(),
					map(m.getAnnotations(), asEAnnotation)
			));
			return methods;
		}

		@Override
		public List<EAnnotation> annotations() {
			if (annotations == null) annotations = map(clazz.getDeclaredAnnotations(), asEAnnotation);

			return annotations;
		}

		<U> ClassDecl<U> toDecl(Class<U> clazz) {
			return accessor.getClassDecl(ClassName.byClass(clazz));
		}

		@SuppressWarnings("unchecked")
		<U> AnnotatedType<U> toAnnoType(java.lang.reflect.AnnotatedType annotatedType) {
			return new AnnotatedType<>(
					toDecl((Class<U>) asClass(annotatedType.getType())),
					map(annotatedType.getAnnotations(), asEAnnotation)
			);
		}

		Class<?> asClass(Type type) {
			if (type instanceof ParameterizedType p) return (Class<?>) p.getRawType();
			if (type instanceof Class<?> c) return c;
			throw new UnsupportedOperationException("Unsupported type " + type.getClass().getName());
		}

		AnnotatedType<?> getSuperClassWithAnnotations(Class<?> clazz) {
			Class<?> superclass = clazz.getSuperclass();

			if (superclass == null) return null;

			List<Annotation> list = new ArrayList<>();

			Class<?> superClass = find(getDirectSuperclasses(clazz), c -> !c.isInterface());
			if (superClass != null) Collections.addAll(list, superClass.getAnnotations());

			var annotatedSuperclass = clazz.getAnnotatedSuperclass();
			if (annotatedSuperclass != null) Collections.addAll(list, annotatedSuperclass.getAnnotations());

			return new AnnotatedType<>(toDecl(superclass), map(list, asEAnnotation));
		}

		List<AnnotatedType<?>> getSuperTypesWithAnnotations(Class<?> clazz) {
			Map<Class<?>, List<Annotation>> typeAnnotations = new HashMap<>();

			for (Class<?> type : getDirectSuperclasses(clazz)) {
				Collections.addAll(getOrPut(typeAnnotations, type, ArrayList::new), type.getAnnotations());
			}

			List<java.lang.reflect.AnnotatedType> javaTypes = new ArrayList<>();

			java.lang.reflect.AnnotatedType annotatedSuperclass = clazz.getAnnotatedSuperclass();
			if (annotatedSuperclass != null) javaTypes.add(annotatedSuperclass);

			Collections.addAll(javaTypes, clazz.getAnnotatedInterfaces());

			for (java.lang.reflect.AnnotatedType type : javaTypes) {
				Collections.addAll(getOrPut(typeAnnotations, (Class<?>) type.getType(), ArrayList::new), type.getAnnotations());
			}

			return map(typeAnnotations, entry -> {
				Class<?> type = entry.getKey();
				List<EAnnotation> annotations = map(entry.getValue(), asEAnnotation);
				return new AnnotatedType<>(toDecl(type), annotations);
			});
		}
	}
}
