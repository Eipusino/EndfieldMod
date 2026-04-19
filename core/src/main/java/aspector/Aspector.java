package aspector;

import aspector.annotations.AspectElement;
import aspector.annotations.Stub;
import aspector.classes.AnnotatedType;
import aspector.classes.AnnotationValue.EnumValue;
import aspector.classes.ClassDecl;
import aspector.classes.ClassName;
import aspector.classes.EAnnotation;
import aspector.classes.EAspectMethod;
import aspector.classes.EConstructor;
import aspector.classes.EField;
import aspector.classes.EMethod;
import aspector.generate.AspectDeclaringException;
import aspector.generate.ClassMaker;
import endfield.util.Pair;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static endfield.util.Arrays2.any;
import static endfield.util.Arrays2.filter;
import static endfield.util.Arrays2.map;

public class Aspector {
	public static final ClassName stub = ClassName.byClass(Stub.class);
	public static final ClassName aspectElement = ClassName.byClass(AspectElement.class);

	private final ClassMaker classMaker;

	public Aspector(ClassMaker maker) {
		classMaker = maker;
	}

	public <T> AspectDecl<T> applyAspect(ClassDecl<?> aspectDeclare, ClassDecl<T> targetClass) {
		checkAspectable(aspectDeclare, targetClass);

		return classMaker.makeClass(aspectDeclare, targetClass, b -> {
			// Register Stub spec
			for (ClassDecl<?> decl : findStub(aspectDeclare)) b.registerStubSpec(decl);

			// Register implement interfaces
			for (ClassDecl<?> decl : map(filter(aspectDeclare.annotatedInterfaces(),
							type -> type.getAnnotation(stub) == null),
					d -> d.type)) b.registerInterfaces(decl);

			// Register aspect methods
			for (var pair : map(filter(aspectDeclare.methods(),
							m -> (m.flags & (Modifier.PRIVATE | Modifier.STATIC)) == 0),
					m -> {
						EAnnotation a = m.getAnnotation(aspectElement);
						if (a != null) {
							EnumValue<Using> v = a.getValue("using");
							if (v != null) return new Pair<>(m, v.value());
						}
						return new Pair<>(m, Using.OVERRIDE);
					})) {
				EMethod m = pair.getFirst();
				Using u = pair.getSecond();
				b.registerAspectMethod(new EAspectMethod(
						m.declaring,
						m.name,
						m.parameters,
						m.annotatedReturnType,
						m.flags,
						u,
						m.annotations
				));
			}

			// Register fields
			for (EField f : aspectDeclare.fields()) b.registerImplField(f);

			// Register non-aspect methods
			for (EMethod m : filter(aspectDeclare.methods(),
					m -> (m.flags & (Modifier.PRIVATE | Modifier.STATIC)) != 0)) b.registerImplMethod(m);

			// Register constructor
			for (EConstructor<?> c : aspectDeclare.constructors()) b.registerImplConstructor(c);
		});
	}

	private void checkAspectable(ClassDecl<?> aspectImpl, ClassDecl<?> sourceClass) {
		// Check source type
		if (sourceClass.isPrimitive() || sourceClass.isEnum() || sourceClass.isArray() || sourceClass.isInterface())
			throw new IllegalArgumentException("Source class " + aspectImpl.name + " must be a normal class");

		// Check implement type
		if (aspectImpl.isPrimitive() || aspectImpl.isEnum() || aspectImpl.isArray() || aspectImpl.isInterface())
			throw new IllegalArgumentException("Aspect implement class " + aspectImpl.name + " must be a normal class");

		// Check stub, super class must be Stub
		AnnotatedType<?> superClass = aspectImpl.annotatedSuperClass();
		if (superClass != null && !superClass.type.name.equals(ClassName.jObject) && !any(superClass.annotations, a -> a.type.equals(stub)))
			throw new AspectDeclaringException("Super class of aspect implement must be annotated by @Stub");
	}

	private Set<ClassDecl<?>> findStub(ClassDecl<?> aspectImpl) {
		List<AnnotatedType<?>> types = aspectImpl.annotatedInterfaces();
		AnnotatedType<?> superclass = aspectImpl.annotatedSuperClass();
		if (superclass != null) types.add(superclass);
		return new HashSet<>(map(filter(types, t -> any(t.annotations, a -> a.type.equals(stub))), t -> t.type));
	}
}
