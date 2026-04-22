package endfield.desktop;

import endfield.util.Collections2;
import endfield.util.aspector.accesses.PackageAccessHandler;
import endfield.util.aspector.classes.ClassAccessor;
import endfield.util.aspector.classes.ClassName;
import endfield.util.aspector.classes.MethodSignature;
import endfield.util.aspector.classes.ClassElement;
import endfield.util.aspector.classes.EConstructor;
import endfield.util.aspector.classes.EMethod;
import jdk.internal.misc.Unsafe;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import static endfield.util.aspector.generate.AspectMaker.returnValue;

public class UnsafePackageAccessHandler extends PackageAccessHandler {
	static final Unsafe unsafe = Unsafe.getUnsafe();

	public static final ClassName packageAccessor = ClassName.byClass(PackageAccessor.class);

	public UnsafePackageAccessHandler(ClassAccessor access) {
		super(access);
	}

	@Override
	protected byte[] genPackageAccessClass(AccessBuilder builder) {
		ClassName className = builder.className;
		ClassName targetName = builder.accessTarget;

		List<ClassElement> elements = builder.enhanceElements;
		List<EMethod> methods = new ArrayList<>();
		List<EConstructor<?>> constructors = new ArrayList<>();
		for (ClassElement e : elements) {
			if (e instanceof EMethod m) {
				methods.add(m);
			} else if (e instanceof EConstructor<?> c) {
				constructors.add(c);
			}
		}

		ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		classWriter.visit(
				Opcodes.V1_8,
				Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
				className.internalName(),
				null,
				targetName.internalName(),
				null
		);
		classWriter.visitAnnotation(
				packageAccessor.descriptor,
				true
		).visitEnd();

		for (EMethod m : methods) {
			MethodVisitor methodVisitor = classWriter.visitMethod(
					m.flags & ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED) | Opcodes.ACC_PROTECTED,
					m.name,
					m.descriptor().jvmDescriptor(),
					null,
					null
					);
			methodVisitor.visitCode();

			invokeMethod(
					methodVisitor,
					targetName,
					m.descriptor(),
					false
			);
			returnValue(methodVisitor, m);

			methodVisitor.visitMaxs(0, 0);
			methodVisitor.visitEnd();
		}

		for (EConstructor<?> c : constructors) {
			MethodVisitor methodVisitor = classWriter.visitMethod(
					c.flags & ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED) | Opcodes.ACC_PROTECTED,
					"<init>",
					c.descriptor().jvmDescriptor(),
					null,
					null
			);
			methodVisitor.visitCode();

			invokeMethod(
					methodVisitor,
					targetName,
					c.descriptor(),
					false
			);
			methodVisitor.visitInsn(Opcodes.RETURN);

			methodVisitor.visitMaxs(0, 0);
			methodVisitor.visitEnd();
		}
		classWriter.visitEnd();

		return classWriter.toByteArray();
	}

	@Override
	protected Class<?> loadClass(ClassName className, byte[] bytecode, Class<?> accessTarget) {
		ProtectionDomain accessTargetDomain = accessTarget.getProtectionDomain();
		return unsafe.defineClass(className.name(), bytecode, 0, bytecode.length, accessTarget.getClassLoader(), accessTargetDomain);
	}

	static void invokeMethod(MethodVisitor write, ClassName owner, MethodSignature method, boolean isInterface) {
		write.visitVarInsn(Opcodes.ALOAD, 0);
		Collections2.forEachIndexed(method.paramTypes, (n, param) -> {
			int varIndex = n + 1;
			switch (param.descriptor) {
				case "B", "S", "I", "Z", "C" -> write.visitVarInsn(Opcodes.ILOAD, varIndex);
				case "J" -> write.visitVarInsn(Opcodes.LLOAD, varIndex);
				case "F" -> write.visitVarInsn(Opcodes.FLOAD, varIndex);
				case "D" -> write.visitVarInsn(Opcodes.DLOAD, varIndex);
				default -> write.visitVarInsn(Opcodes.ALOAD, varIndex);
			}
		});
		write.visitMethodInsn(
				Opcodes.INVOKESPECIAL,
				owner.internalName(),
				method.methodName,
				method.jvmDescriptor(),
				isInterface
		);
	}
}
