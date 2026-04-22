package endfield.util.aspector.classes;

import endfield.util.Arrays2;
import endfield.util.Collections2;
import endfield.util.Maps2;
import endfield.util.aspector.classes.ClassDecl.ArrayClassDecl;
import kotlin.Metadata;
import kotlin.collections.CollectionsKt;
import kotlin.collections.MapsKt;
import kotlin.metadata.KmAnnotation;
import kotlin.metadata.KmAnnotationArgument;
import kotlin.metadata.KmClass;
import kotlin.metadata.KmClassifier;
import kotlin.metadata.KmFunction;
import kotlin.metadata.KmProperty;
import kotlin.metadata.KmType;
import kotlin.metadata.jvm.JvmExtensionsKt;
import kotlin.metadata.jvm.JvmFieldSignature;
import kotlin.metadata.jvm.JvmMethodSignature;
import kotlin.metadata.jvm.KotlinClassMetadata;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypeReference;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASMClassAccessor implements ClassAccessor {
	List<ClassLoader> loaderPath = new ArrayList<>();
	List<File> filePath = new ArrayList<>();

	Map<ClassName, ClassDecl<?>> loadedDeclMap = new HashMap<>();

	public ASMClassAccessor(Object... paths) {
		if (paths.length == 0) {
			loaderPath.add(ASMClassAccessor.class.getClassLoader());
		} else {
			for (Object o : paths) {
				if (o instanceof ClassLoader cl) {
					loaderPath.add(cl);
				} else if (o instanceof File f) {
					filePath.add(f);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ClassDecl<T> getClassDecl(ClassName className) {
		return (ClassDecl<T>) switch (className.descriptor) {
			case "V" -> ClassAccessor.VOID;
			case "B" -> ClassAccessor.BYTE;
			case "S" -> ClassAccessor.SHORT;
			case "I" -> ClassAccessor.INT;
			case "J" -> ClassAccessor.LONG;
			case "F" -> ClassAccessor.FLOAT;
			case "D" -> ClassAccessor.DOUBLE;
			case "C" -> ClassAccessor.CHAR;
			case "Z" -> ClassAccessor.BOOLEAN;
			default -> MapsKt.getOrPut(loadedDeclMap, className, () -> {
				if (className.isArray()) {
					ClassDecl<Object> componentType = getClassDecl(className.componentName());
					return new ArrayClassDecl<>(componentType);
				}

				byte[] bytecode = getBytes(className);
				return new BytecodeClassDecl<>(className, this, bytecode);
			});
		};
	}

	@Override
	public byte[] getBytes(ClassName className) {
		String path = className.internalName() + ".class";

		return new byte[0];
	}

	static class BytecodeClassDecl<T> extends ClassDecl<T> {
		static final ClassName METADATA = ClassName.byClass(Metadata.class);

		final ClassAccessor accessor;
		final byte[] bytecode;

		boolean initialized;

		int flags;
		ClassDecl<?> superClass;
		EAnnotatedType<?> annotatedSuperClass;
		List<ClassDecl<?>> interfaces = List.of();
		List<EAnnotatedType<?>> annotatedInterfaces = List.of();
		List<EAnnotation> annotations = List.of();
		List<EField> fields = List.of();
		List<EMethod> methods = List.of();
		List<EConstructor<T>> constructors = List.of();

		@Override
		public int flags() {
			initialize();
			return flags;
		}

		@Override
		public @Nullable ClassDecl<?> superClass() {
			initialize();
			return superClass;
		}

		@Override
		public @Nullable EAnnotatedType<?> annotatedSuperClass() {
			initialize();
			return annotatedSuperClass;
		}

		@Override
		public List<ClassDecl<?>> interfaces() {
			initialize();
			return interfaces;
		}

		@Override
		public List<EAnnotation> annotations() {
			initialize();
			return annotations;
		}

		@Override
		public List<EAnnotatedType<?>> annotatedInterfaces() {
			initialize();
			return annotatedInterfaces;
		}

		@Override
		public List<EField> fields() {
			initialize();
			return fields;
		}

		@Override
		public List<EConstructor<T>> constructors() {
			initialize();
			return constructors;
		}

		@Override
		public List<EMethod> methods() {
			initialize();
			return methods;
		}

		@SuppressWarnings("unchecked")
		public void initialize() {
			if (initialized) return;

			List<EField> fields0 = new ArrayList<>();
			List<EMethod> methods0 = new ArrayList<>();
			List<EConstructor<T>> constructors0 = new ArrayList<>();

			ClassReader classReader = new ClassReader(bytecode);
			ClassNode classRoot = new ClassNode(Opcodes.ASM9);
			classReader.accept(
					classRoot,
					ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES
			);

			annotations = handleAnnotations(CollectionsKt.plus(classRoot.visibleAnnotations, classRoot.invisibleAnnotations));
			EAnnotation ktMetadata = CollectionsKt.firstOrNull(annotations, it -> it.type.equals(METADATA));
			Metadata metadata = ktMetadata == null ? null : new Metadata() {
				@Override
				public int k() {
					AnnotationValue<?, ?> value = ktMetadata.getValue("k");
					return value == null ? 0 : (int) value.value();
				}

				@Override
				public int[] mv() {
					AnnotationValue<?, ?> value = ktMetadata.getValue("mv");
					return value == null ? new int[0] : (int[]) value.value();
				}

				@Override
				public String[] d1() {
					AnnotationValue<?, ?> value = ktMetadata.getValue("d1");
					return value == null ? new String[0] : (String[]) value.value();
				}

				@Override
				public String[] d2() {
					AnnotationValue<?, ?> value = ktMetadata.getValue("d2");
					return value == null ? new String[0] : (String[]) value.value();
				}

				@Override
				public String xs() {
					AnnotationValue<?, ?> value = ktMetadata.getValue("xs");
					return value == null ? "" : (String) value.value();
				}

				@Override
				public String pn() {
					AnnotationValue<?, ?> value = ktMetadata.getValue("pn");
					return value == null ? "" : (String) value.value();
				}

				@Override
				public int xi() {
					AnnotationValue<?, ?> value = ktMetadata.getValue("k");
					return value == null ? 0 : (int) value.value();
				}

				@Override
				public int[] bv() {
					return new int[]{1, 0, 3};
				}

				@Override
				public Class<? extends Annotation> annotationType() {
					return getClass();
				}
			};
			KmClass kmClass = metadata == null ? null : ((KotlinClassMetadata.Class) KotlinClassMetadata.readLenient(metadata)).getKmClass();

			Map<Integer, List<EAnnotation>> typeRefAnnoMap = handleTypeAnnotations(CollectionsKt.plus(classRoot.visibleTypeAnnotations, classRoot.invisibleTypeAnnotations));
			if (kmClass != null) {
				for (KmType kmType : kmClass.getSupertypes()) {
					KmClassifier cf = kmType.classifier;
					ClassName className = cf instanceof KmClassifier.Class kc ? ClassName.byName(kc.getName()) : null;

					if (className == null) continue;

					int index = classRoot.superName.equals(className.internalName()) ? -1 : CollectionsKt.indexOfFirst(classRoot.interfaces, it -> it.equals(className.internalName()));

					for (KmAnnotation kmAnnotation : JvmExtensionsKt.getAnnotations(kmType)) {
						EAnnotation annotation = handleKmAnnotation(kmAnnotation);
						MapsKt.getOrPut(typeRefAnnoMap, TypeReference.newSuperTypeReference(index).getValue(), ArrayList::new).add(annotation);
					}
				}
			}

			flags = classRoot.access;
			superClass = accessor.getClassDecl(classRoot.superName != null ? ClassName.byInternalName(classRoot.superName) : ClassName.OBJECT);
			annotatedSuperClass = new EAnnotatedType<>(superClass, Maps2.getOrElse(typeRefAnnoMap, TypeReference.newSuperTypeReference(-1).getValue(), ArrayList::new));
			interfaces = classRoot.interfaces != null ? CollectionsKt.map(CollectionsKt.toList(classRoot.interfaces), it -> accessor.getClassDecl(ClassName.byInternalName(it))) : List.of();
			annotatedInterfaces = CollectionsKt.mapIndexed(interfaces, (i, type) -> new EAnnotatedType<>(type, Maps2.getOrElse(typeRefAnnoMap, TypeReference.newSuperTypeReference(i).getValue(), ArrayList::new)));

			Map<String, Map<Integer, List<EAnnotation>>> kmFieldAnnoRef = new HashMap<>();
			Map<MethodSignature, Map<Integer, List<EAnnotation>>> kmMethodAnnoRef = new HashMap<>();
			if (kmClass != null) {
				for (KmProperty property : kmClass.getProperties()) {
					JvmFieldSignature fieldSignature = JvmExtensionsKt.getFieldSignature(property);
					if (fieldSignature != null) {
						String name = fieldSignature.getName();
						KmType type = property.getReturnType();
						MapsKt.getOrPut(MapsKt.getOrPut(kmFieldAnnoRef, name, HashMap::new), TypeReference.newTypeReference(TypeReference.FIELD).getValue(), ArrayList::new).addAll(CollectionsKt.map(JvmExtensionsKt.getAnnotations(type), BytecodeClassDecl::handleKmAnnotation));
					}
					JvmMethodSignature getterSignature = JvmExtensionsKt.getGetterSignature(property);
					if (getterSignature != null) {
						MethodSignature signature = MethodSignature.parse(getterSignature.getName(), getterSignature.getDescriptor());
						MapsKt.getOrPut(MapsKt.getOrPut(kmMethodAnnoRef, signature, HashMap::new), TypeReference.newTypeReference(TypeReference.METHOD_RETURN).getValue(), ArrayList::new).addAll(CollectionsKt.map(JvmExtensionsKt.getAnnotations(property.getReturnType()), BytecodeClassDecl::handleKmAnnotation));
					}
					JvmMethodSignature setterSignature = JvmExtensionsKt.getSetterSignature(property);
					if (setterSignature != null) {
						MethodSignature signature = MethodSignature.parse(setterSignature.getName(), setterSignature.getDescriptor());
						MapsKt.getOrPut(MapsKt.getOrPut(kmMethodAnnoRef, signature, HashMap::new), TypeReference.newFormalParameterReference(0).getValue(), ArrayList::new).addAll(CollectionsKt.map(JvmExtensionsKt.getAnnotations(property.getReturnType()), BytecodeClassDecl::handleKmAnnotation));
					}
				}
			}
			for (FieldNode field : classRoot.fields) {
				Map<Integer, List<EAnnotation>> typeRefAnnoMap0 = handleTypeAnnotations(CollectionsKt.plus(field.visibleTypeAnnotations, field.invisibleTypeAnnotations));

				Map<Integer, List<EAnnotation>> map = kmFieldAnnoRef.get(field.name);
				if (map != null) {
					for (var entry : map.entrySet()) MapsKt.getOrPut(typeRefAnnoMap0, entry.getKey(), ArrayList::new).addAll(entry.getValue());
				}

				fields0.add(new EField(this, field.name, new EAnnotatedType<>(accessor.getClassDecl(ClassName.byDescriptor(field.desc)), Maps2.getOrElse(typeRefAnnoMap0, TypeReference.newTypeReference(TypeReference.FIELD).getValue(), ArrayList::new)), field.access, field.value, handleAnnotations(CollectionsKt.plus(classRoot.visibleAnnotations, classRoot.invisibleAnnotations))));
			}

			if (kmClass != null) {
				for (KmFunction function : kmClass.getFunctions()) {
					JvmMethodSignature funcSign = JvmExtensionsKt.getSignature(function);
					if (funcSign == null) continue;
					MethodSignature sign = MethodSignature.parse(funcSign.getName(), funcSign.getDescriptor());
					Map<Integer, List<EAnnotation>> map = MapsKt.getOrPut(kmMethodAnnoRef, sign, HashMap::new);

					for (KmAnnotation kmAnnotation : JvmExtensionsKt.getAnnotations(function.getReturnType())) {
						EAnnotation annotation = handleKmAnnotation(kmAnnotation);
						MapsKt.getOrPut(map, TypeReference.newTypeReference(TypeReference.METHOD_RETURN).getValue(), ArrayList::new).add(annotation);
					}

					Collections2.forEachIndexed(CollectionsKt.plus(CollectionsKt.listOfNotNull(function.getReceiverParameterType()), CollectionsKt.map(function.getValueParameters(), it -> it.type)), (i, paramType) -> {
						List<KmAnnotation> annotations = JvmExtensionsKt.getAnnotations(paramType);
						MapsKt.getOrPut(map, TypeReference.newFormalParameterReference(i).getValue(), ArrayList::new).addAll(CollectionsKt.map(annotations, BytecodeClassDecl::handleKmAnnotation));
					});
				}
			}
			for (MethodNode method : classRoot.methods) {
				MethodSignature signature = MethodSignature.parse(method.name, method.desc);
				Map<Integer, List<EAnnotation>> typeRefAnnoMap1 = handleTypeAnnotations(CollectionsKt.plus(method.visibleTypeAnnotations, method.invisibleTypeAnnotations));
				List<EAnnotation>[] paramAnnotations = new List[signature.paramTypes.size()];
				for (int i = 0; i < paramAnnotations.length; i++) paramAnnotations[i] = new ArrayList<>();

				if (method.visibleParameterAnnotations != null) Arrays2.forEachIndexed(method.visibleParameterAnnotations, (i, annotations) -> paramAnnotations[i].addAll(handleAnnotations(annotations)));
				if (method.invisibleParameterAnnotations != null) Arrays2.forEachIndexed(method.invisibleParameterAnnotations, (i, annotations) -> paramAnnotations[i].addAll(handleAnnotations(annotations)));

				Map<Integer, List<EAnnotation>> map = kmMethodAnnoRef.get(signature);
				if (map != null) {
					for (var entry : map.entrySet()) MapsKt.getOrPut(typeRefAnnoMap1, entry.getKey(), ArrayList::new).addAll(entry.getValue());
				}

				List<String> paramNames = method.parameters == null ? null : CollectionsKt.map(method.parameters, it -> it.name);
				List<Parameter> params = CollectionsKt.mapIndexed(signature.paramTypes, (i, param) -> new Parameter(paramNames == null ? "arg" + i : paramNames.get(i), new EAnnotatedType<>(accessor.getClassDecl(signature.paramTypes.get(i)), Maps2.getOrElse(typeRefAnnoMap1, TypeReference.newFormalParameterReference(i).getValue(), ArrayList::new)), CollectionsKt.toList(paramAnnotations[i])));

				if (!method.name.equals("<init>")) {
					methods0.add(new EMethod(this, method.name, params, new EAnnotatedType<>(accessor.getClassDecl(signature.returnType), Maps2.getOrElse(typeRefAnnoMap1, TypeReference.newTypeReference(TypeReference.METHOD_RETURN).getValue(), ArrayList::new)), method.access, handleAnnotations(CollectionsKt.plus(method.visibleAnnotations, method.invisibleAnnotations))));
				} else {
					constructors0.add(new EConstructor<>(this, params, method.access, handleAnnotations(CollectionsKt.plus(method.visibleAnnotations, method.invisibleAnnotations))));
				}
			}

			fields = fields0;
			methods = methods0;
			constructors = constructors0;

			initialized = true;
		}

		public BytecodeClassDecl(ClassName name, ClassAccessor accessor1, byte[] bytes) {
			super(name);

			accessor = accessor1;
			bytecode = bytes;
		}

		static Map<Integer, List<EAnnotation>> handleTypeAnnotations(List<TypeAnnotationNode> nodes) {
			Map<Integer, List<EAnnotation>> typeRefAnnoMap = new HashMap<>();
			for (TypeAnnotationNode annotation : nodes) {
				int ref = annotation.typeRef;
				MapsKt.getOrPut(typeRefAnnoMap, ref, ArrayList::new);
			}

			return typeRefAnnoMap;
		}

		static List<EAnnotation> handleAnnotations(List<AnnotationNode> nodes) {
			return CollectionsKt.map(nodes, BytecodeClassDecl::handleAnnotation);
		}

		static EAnnotation handleAnnotation(AnnotationNode node) {
			ClassName annotationName = ClassName.byDescriptor(node.desc);

			Map<String, AnnotationValue<?, ?>> annoValues = new HashMap<>();
			List<Object> valueList = node.values == null ? List.of() : node.values;
			for (int i = 0; i < valueList.size(); i += 2) {
				String name = (String) valueList.get(i);
				Object raw = valueList.get(i + 1);
				AnnotationValue<?, ?> value = handleAnnotationValue(raw);

				annoValues.put(name, value);
			}

			return new EAnnotation(annotationName, annoValues);
		}

		static EAnnotation handleKmAnnotation(KmAnnotation kmAnnotation) {
			ClassName annotationName = ClassName.byInternalName(kmAnnotation.getClassName());

			Map<String, AnnotationValue<?, ?>> annoValues = new HashMap<>();
			for (var arg : kmAnnotation.getArguments().entrySet()) {
				String name = arg.getKey();
				KmAnnotationArgument raw = arg.getValue();
				AnnotationValue<?, ?> value = handleKmAnnoArg(raw);

				annoValues.put(name, value);
			}

			return new EAnnotation(annotationName, annoValues);
		}

		@SuppressWarnings("unchecked")
		static AnnotationValue<?, ?> handleKmAnnoArg(KmAnnotationArgument argument) {
			if (argument instanceof KmAnnotationArgument.LiteralValue<?> literalValue)
				return new AnnotationValue.Value<>(literalValue.getValue());
			if (argument instanceof KmAnnotationArgument.EnumValue enumValue)
				return new AnnotationValue.EnumValue<>(ClassName.byName(enumValue.getEnumClassName()), enumValue.getEnumEntryName());
			if (argument instanceof KmAnnotationArgument.KClassValue kClassValue)
				return new AnnotationValue.TypeValue(ClassName.byName(kClassValue.getClassName()));
			if (argument instanceof KmAnnotationArgument.AnnotationValue annotationValue)
				return new AnnotationValue.NestedAnnotationValue(handleKmAnnotation(annotationValue.getAnnotation()));
			if (argument instanceof KmAnnotationArgument.ArrayValue arrayValue) {
				List<KmAnnotationArgument> elements = arrayValue.getElements();
				KmAnnotationArgument arrayArgument = elements.get(0);
				if (arrayArgument instanceof KmAnnotationArgument.ByteValue)
					return new AnnotationValue.Value<>(CollectionsKt.toByteArray(CollectionsKt.map(elements, it -> ((KmAnnotationArgument.ByteValue) it).getValue())));
				if (arrayArgument instanceof KmAnnotationArgument.ShortValue)
					return new AnnotationValue.Value<>(CollectionsKt.toShortArray(CollectionsKt.map(elements, it -> ((KmAnnotationArgument.ShortValue) it).getValue())));
				if (arrayArgument instanceof KmAnnotationArgument.IntValue)
					return new AnnotationValue.Value<>(CollectionsKt.toIntArray(CollectionsKt.map(elements, it -> ((KmAnnotationArgument.IntValue) it).getValue())));
				if (arrayArgument instanceof KmAnnotationArgument.LongValue)
					return new AnnotationValue.Value<>(CollectionsKt.toLongArray(CollectionsKt.map(elements, it -> ((KmAnnotationArgument.LongValue) it).getValue())));
				if (arrayArgument instanceof KmAnnotationArgument.FloatValue)
					return new AnnotationValue.Value<>(CollectionsKt.toFloatArray(CollectionsKt.map(elements, it -> ((KmAnnotationArgument.FloatValue) it).getValue())));
				if (arrayArgument instanceof KmAnnotationArgument.DoubleValue)
					return new AnnotationValue.Value<>(CollectionsKt.toDoubleArray(CollectionsKt.map(elements, it -> ((KmAnnotationArgument.DoubleValue) it).getValue())));
				if (arrayArgument instanceof KmAnnotationArgument.BooleanValue)
					return new AnnotationValue.Value<>(CollectionsKt.toBooleanArray(CollectionsKt.map(elements, it -> ((KmAnnotationArgument.BooleanValue) it).getValue())));
				if (arrayArgument instanceof KmAnnotationArgument.CharValue)
					return new AnnotationValue.Value<>(CollectionsKt.toCharArray(CollectionsKt.map(elements, it -> ((KmAnnotationArgument.CharValue) it).getValue())));
				return new AnnotationValue.ArrayValue<>(CollectionsKt.map(elements, it -> (AnnotationValue<Object, ?>) handleKmAnnoArg(it)));
			}
			if (argument instanceof KmAnnotationArgument.ArrayKClassValue arrayKClassValue) {
				ClassName c = ClassName.byName(arrayKClassValue.getClassName());
				for (int i = 0; i < arrayKClassValue.getArrayDimensionCount(); i++) c = c.arrayName();
				return new AnnotationValue.TypeValue(c);
			}

			throw new IllegalArgumentException(String.valueOf(argument));
		}

		@SuppressWarnings("unchecked")
		static AnnotationValue<?, ?> handleAnnotationValue(Object raw) {
			if (raw instanceof Type type)
				return new AnnotationValue.TypeValue(ClassName.byInternalName(type.getInternalName()));
			if (raw instanceof Object[] objects)
				return new AnnotationValue.EnumValue<>(ClassName.byDescriptor((String) objects[0]), (String) objects[1]);
			if (raw instanceof List<?>) {
				Object first = CollectionsKt.firstOrNull((List<?>) raw);
				if (first instanceof Byte)
					return new AnnotationValue.Value<>(CollectionsKt.toByteArray((List<Byte>) raw));
				if (first instanceof Short)
					return new AnnotationValue.Value<>(CollectionsKt.toShortArray((List<Short>) raw));
				if (first instanceof Integer)
					return new AnnotationValue.Value<>(CollectionsKt.toIntArray((List<Integer>) raw));
				if (first instanceof Long)
					return new AnnotationValue.Value<>(CollectionsKt.toLongArray((List<Long>) raw));
				if (first instanceof Float)
					return new AnnotationValue.Value<>(CollectionsKt.toFloatArray((List<Float>) raw));
				if (first instanceof Double)
					return new AnnotationValue.Value<>(CollectionsKt.toDoubleArray((List<Double>) raw));
				if (first instanceof Boolean)
					return new AnnotationValue.Value<>(CollectionsKt.toBooleanArray((List<Boolean>) raw));
				if (first instanceof Character)
					return new AnnotationValue.Value<>(CollectionsKt.toCharArray((List<Character>) raw));
				if (first == null) return new AnnotationValue.ArrayValue<>(Collections.emptyList());
				return new AnnotationValue.ArrayValue<>(CollectionsKt.map((List<?>) raw, it -> (AnnotationValue<Object, ?>) handleAnnotationValue(it)));
			}
			if (raw instanceof AnnotationNode annotationNode)
				return new AnnotationValue.NestedAnnotationValue(handleAnnotation(annotationNode));
			return new AnnotationValue.Value<>(raw);
		}
	}
}
