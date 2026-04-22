package endfield.util.aspector;

import endfield.util.aspector.classes.AnnotationValue.EnumValue;
import endfield.util.aspector.classes.ClassDecl;
import endfield.util.aspector.classes.ClassName;
import endfield.util.aspector.classes.EAnnotatedType;
import endfield.util.aspector.classes.EAnnotation;
import endfield.util.aspector.classes.EAspectMethod;
import endfield.util.aspector.classes.EConstructor;
import endfield.util.aspector.classes.EField;
import endfield.util.aspector.classes.EMethod;
import endfield.util.aspector.generate.AspectDeclaringException;
import endfield.util.aspector.generate.ClassMaker;
import kotlin.collections.CollectionsKt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Aspector {
	public static final ClassName STUB = ClassName.byClass(Stub.class);
	public static final ClassName ASPECT_ELEMENT = ClassName.byClass(AspectElement.class);

	final ClassMaker classMaker;

	public Aspector(ClassMaker maker) {
		classMaker = maker;
	}

	public <T> AspectDecl<T> applyAspect(ClassDecl<?> aspectDeclare, ClassDecl<T> targetClass) {
		checkAspectable(aspectDeclare, targetClass);

		return classMaker.makeClass(aspectDeclare, targetClass, b -> {
			// Register Stub spec
			for (ClassDecl<?> decl : findStub(aspectDeclare)) b.registerStubSpec(decl);

			// Register implement interfaces
			for (EAnnotatedType<?> type : aspectDeclare.annotatedInterfaces()) {
				if (type.getAnnotation(STUB) != null) continue;

				b.registerInterfaces(type.type);
			}

			// Register aspect methods
			for (EMethod m : aspectDeclare.methods()) {
				if ((m.flags & (Modifier.PRIVATE | Modifier.STATIC)) != 0) continue;

				EAnnotation anno = m.getAnnotation(ASPECT_ELEMENT);
				Using using = null;
				if (anno != null) {
					EnumValue<Using> value = anno.getValue("using");
					if (value != null) using = value.value();
				}

				b.registerAspectMethod(new EAspectMethod(
						m.declaring,
						m.name,
						m.parameters,
						m.annotatedReturnType,
						m.flags,
						using == null ? Using.OVERRIDE : using,
						m.annotations
				));
			}

			// Register fields
			for (EField f : aspectDeclare.fields()) b.registerImplField(f);

			// Register non-aspect methods
			for (EMethod m : aspectDeclare.methods()) {
				if ((m.flags & (Modifier.PRIVATE | Modifier.STATIC)) == 0) continue;

				b.registerImplMethod(m);
			}

			// Register constructor
			for (EConstructor<?> c : aspectDeclare.constructors()) b.registerImplConstructor(c);
		});
	}

	void checkAspectable(ClassDecl<?> aspectImpl, ClassDecl<?> sourceClass) {
		// Check source type
		if (sourceClass.isPrimitive() || sourceClass.isEnum() || sourceClass.isArray() || sourceClass.isInterface())
			throw new IllegalArgumentException("Source class " + aspectImpl.name + " must be a normal class");

		// Check implement type
		if (aspectImpl.isPrimitive() || aspectImpl.isEnum() || aspectImpl.isArray() || aspectImpl.isInterface())
			throw new IllegalArgumentException("Aspect implement class " + aspectImpl.name + " must be a normal class");

		// Check stub, super class must be Stub
		EAnnotatedType<?> superClass = aspectImpl.annotatedSuperClass();
		if (superClass != null && !superClass.type.name.equals(ClassName.OBJECT) && !CollectionsKt.any(superClass.annotations, a -> a.type.equals(STUB)))
			throw new AspectDeclaringException("Super class of aspect implement must be annotated by @Stub");
	}

	Set<ClassDecl<?>> findStub(ClassDecl<?> aspectImpl) {
		List<EAnnotatedType<?>> types = aspectImpl.annotatedInterfaces();
		EAnnotatedType<?> superclass = aspectImpl.annotatedSuperClass();
		if (superclass != null) types.add(superclass);
		return new HashSet<>(CollectionsKt.map(CollectionsKt.filter(types, t -> CollectionsKt.any(t.annotations, a -> a.type.equals(STUB))), t -> t.type));
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.FIELD})
	public @interface AspectElement {
		Using using() default Using.OVERRIDE;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	public @interface Stub {
	}
}
