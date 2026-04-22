package endfield.util.aspector.generate;

import arc.util.OS;
import endfield.util.CollectionList;
import endfield.util.Collections2;
import endfield.util.Constant;
import endfield.util.aspector.Using;
import endfield.util.aspector.classes.BytecodeLoader;
import endfield.util.aspector.classes.ClassAccessor;
import endfield.util.aspector.classes.ClassDecl;
import endfield.util.aspector.classes.ClassElement;
import endfield.util.aspector.classes.ClassName;
import endfield.util.aspector.classes.EAspectMethod;
import endfield.util.aspector.classes.EConstructor;
import endfield.util.aspector.classes.EField;
import endfield.util.aspector.classes.EMethod;
import endfield.util.aspector.classes.MethodSignature;
import kotlin.collections.CollectionsKt;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AspectMaker extends ClassMaker {
	static final CollectionList<String> SYSTEM_PACKAGES = new CollectionList<>(String.class);

	static {
		SYSTEM_PACKAGES.addAll("java.", "javax.", "jdk.", "sun.");

		if (OS.isAndroid) {
			SYSTEM_PACKAGES.addAll("android.", "dalvik.", "libcore.");
		}
	}

	public AspectMaker(ClassAccessor accessor) {
		super(accessor);
	}

	@Override
	public ClassName generateClassName(ClassDecl<?> aspectImpl, ClassDecl<?> targetClass) {
		String target = targetClass.name.name();

		for (String pack : SYSTEM_PACKAGES) {
			if (!target.startsWith(pack)) continue;

			return ClassName.byName(pack.substring(0, pack.length() - 1) + "_accessor." + target.substring(pack.length()));
		}

		return ClassName.byName(target + "$" + aspectImpl.name.simpleName() + "@" + Integer.toHexString(aspectImpl.name.hashCode()));
	}

	@Override
	public byte[] generateBytecode(AspectBuilder builder) {
		ClassName thisClass = builder.className;
		int accessFlags = builder.accessFlags;
		Set<ClassName> stubs = new HashSet<>(builder.stubTypes);
		ClassName superClass = builder.superClass;
		ClassName aspectDecl = builder.aspectDecl;
		List<ClassName> interfaces = builder.interfaces;

		byte[] declBytes = bytesAccessor.getBytes(aspectDecl);

		List<ClassElement> elements = builder.implElements;
		List<EAspectMethod> aspectElements = builder.aspectElements;

		List<EField> fields = new ArrayList<>();
		List<EMethod> methods = new ArrayList<>();
		List<EConstructor<?>> constructors = new ArrayList<>();
		for (ClassElement e : elements) {
			if (e instanceof EField f) {
				fields.add(f);
			} else if (e instanceof EMethod m) {
				methods.add(m);
			} else if (e instanceof EConstructor<?> c) {
				constructors.add(c);
			}
		}

		ClassReader cr = new ClassReader(declBytes);
		ClassNode implRoot = new ClassNode(Opcodes.ASM9);
		cr.accept(implRoot, ClassReader.SKIP_DEBUG);

		Map<MethodSignature, MethodNode> implMethods = CollectionsKt.associateBy(implRoot.methods, node -> MethodSignature.parse(node.name, node.desc));

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cw.visit(
				Opcodes.V1_8,
				accessFlags | Opcodes.ACC_SUPER,
				thisClass.internalName(),
				null,
				superClass.internalName(),
				CollectionsKt.map(interfaces, ClassName::internalName).toArray(Constant.EMPTY_STRING)
		);

		for (EField f : fields) {
			FieldVisitor fieldVisitor = cw.visitField(
					f.flags,
					f.name,
					f.type().name.descriptor,
					null,
					f.constant
			);
			fieldVisitor.visitEnd();
		}

		for (EMethod m : methods) {
			MethodVisitor methodVisitor = cw.visitMethod(
					m.flags,
					m.name,
					m.descriptor().jvmDescriptor(),
					null,
					null
			);

			MethodNode byMethod = implMethods.get(m.descriptor());
			if (byMethod == null)
				throw new AspectDeclaringException("Method " + m.name + " not found in aspect implementation");

			methodVisitor.visitCode();
			visitMethodBy(methodVisitor, byMethod, thisClass, superClass, aspectDecl, stubs);

			methodVisitor.visitEnd();
		}

		for (EConstructor<?> c : constructors) {
			MethodVisitor methodVisitor = cw.visitMethod(
					c.flags,
					"<init>",
					c.descriptor().jvmDescriptor(),
					null,
					null
			);
			MethodNode byMethod = implMethods.get(c.descriptor());
			if (byMethod == null)
				throw new AspectDeclaringException("Method " + c.name + " not found in aspect implementation");
			if (c.descriptor().paramTypes.isEmpty()) {
				methodVisitor.visitCode();
				invokeMethod(
						methodVisitor,
						Opcodes.INVOKESPECIAL,
						superClass,
						c.descriptor(),
						false
				);
				methodVisitor.visitInsn(Opcodes.RETURN);

				methodVisitor.visitMaxs(0, 0);
				methodVisitor.visitEnd();

				continue;
			}

			methodVisitor.visitCode();
			visitMethodBy(methodVisitor, byMethod, thisClass, superClass, aspectDecl, stubs);
			methodVisitor.visitInsn(Opcodes.RETURN);

			methodVisitor.visitMaxs(0, 0);
			methodVisitor.visitEnd();
		}

		for (EAspectMethod e : aspectElements) {
			var byMethod = implMethods.get(e.descriptor());
			if (byMethod != null) {
				MethodSignature bridge = e.using == Using.OVERRIDE
						? new MethodSignature("NONE", List.of(), ClassName.V)
						: buildBridgeMethod(cw, byMethod, thisClass, superClass, aspectDecl, stubs);
				MethodVisitor methodVisitor = cw.visitMethod(
						e.using != Using.OVERRIDE ? e.flags | ~Opcodes.ACC_BRIDGE : e.flags,
						e.name,
						e.descriptor().jvmDescriptor(),
						null,
						null
				);
				methodVisitor.visitCode();

				switch (e.using) {
					case BEFORE, BEFORE_RETURN -> {
						invokeMethod(methodVisitor, Opcodes.INVOKESPECIAL, thisClass, bridge, false);
						if (e.using == Using.BEFORE && !e.descriptor().returnType.equals(ClassName.V))
							methodVisitor.visitInsn(Opcodes.POP);
						invokeMethod(methodVisitor, Opcodes.INVOKESPECIAL, superClass, e.descriptor(), false);
						if (e.using == Using.BEFORE_RETURN && !e.descriptor().returnType.equals(ClassName.V))
							methodVisitor.visitInsn(Opcodes.POP);

						returnValue(methodVisitor, e);
					}
					case OVERRIDE -> {
						visitMethodBy(methodVisitor, byMethod, thisClass, superClass, aspectDecl, stubs);
					}
					case AFTER, AFTER_RETURN -> {
						invokeMethod(methodVisitor, Opcodes.INVOKESPECIAL, superClass, e.descriptor(), false);
						if (e.using == Using.AFTER_RETURN && !e.descriptor().returnType.equals(ClassName.V))
							methodVisitor.visitInsn(Opcodes.POP);
						invokeMethod(methodVisitor, Opcodes.INVOKESPECIAL, thisClass, bridge, false);
						if (e.using == Using.AFTER && !e.descriptor().returnType.equals(ClassName.V))
							methodVisitor.visitInsn(Opcodes.POP);

						returnValue(methodVisitor, e);
					}
				}

				methodVisitor.visitMaxs(0, 0);
				methodVisitor.visitEnd();
			}
		}

		cw.visitEnd();

		return cw.toByteArray();
	}

	@Override
	public Class<?> loadClass(BytecodeLoader loader, ClassName className, byte[] bytecode) {
		String name = className.name();

		/*try (FileOutputStream stream = new FileOutputStream(className.simpleName() + ".class")) {
			stream.write(bytecode);
		} catch (IOException e) {
			Log.err(e);
		}*/

		loader.declareClass(name, bytecode);
		return loader.loadClass(name);
	}

	public static void returnValue(MethodVisitor write, EMethod method) {
		ClassName c = method.descriptor().returnType;
		if (c.equals(ClassName.V)) {
			write.visitInsn(Opcodes.RETURN);
		} else if (c.equals(ClassName.B) || c.equals(ClassName.S) || c.equals(ClassName.I) || c.equals(ClassName.Z) || c.equals(ClassName.C)) {
			write.visitInsn(Opcodes.IRETURN);
		} else if (c.equals(ClassName.J)) {
			write.visitInsn(Opcodes.LRETURN);
		} else if (c.equals(ClassName.F)) {
			write.visitInsn(Opcodes.FRETURN);
		} else if (c.equals(ClassName.D)) {
			write.visitInsn(Opcodes.DRETURN);
		} else {
			write.visitInsn(Opcodes.ARETURN);
		}
	}

	public static void invokeMethod(MethodVisitor write, int opcode, ClassName owner, MethodSignature method, boolean isInterface) {
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
				opcode,
				owner.internalName(),
				method.methodName,
				method.jvmDescriptor(),
				isInterface
		);
	}

	public static MethodSignature buildBridgeMethod(ClassVisitor write, MethodNode byMethod, ClassName thisClass, ClassName superClass, ClassName aspectImpl, Set<ClassName> stubSpec) {
		MethodSignature methodSignature = MethodSignature.parse(
				byMethod.name + "$bridge",
				byMethod.desc
		);
		MethodVisitor methodVisitor = write.visitMethod(
				Opcodes.ACC_PRIVATE | Opcodes.ACC_BRIDGE,
				methodSignature.methodName,
				byMethod.desc,
				byMethod.signature,
				byMethod.exceptions.toArray(Constant.EMPTY_STRING)
		);
		methodVisitor.visitCode();
		visitMethodBy(methodVisitor, byMethod, thisClass, superClass, aspectImpl, stubSpec);
		methodVisitor.visitMaxs(0, 0);
		methodVisitor.visitEnd();

		return methodSignature;
	}

	public static void visitMethodBy(MethodVisitor write, MethodNode byMethod, ClassName thisClass, ClassName superClass, ClassName aspectImpl, Set<ClassName> stubSpec) {
		MethodVisitor swap = new MethodVisitor(Opcodes.ASM9, write) {
			@Override
			public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
				String realOwner = owner;
				boolean realInterface = isInterface;
				if (stubSpec.contains(ClassName.byInternalName(owner)) && opcode == Opcodes.INVOKESPECIAL) {
					realOwner = superClass.internalName();
					realInterface = false;
				}
				if (owner.equals(aspectImpl.internalName())) {
					realOwner = thisClass.internalName();
					realInterface = false;
				}
				write.visitMethodInsn(
						opcode,
						realOwner,
						name,
						descriptor,
						realInterface
				);
			}

			@Override
			public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
				var realOwner = owner;
				if (stubSpec.contains(ClassName.byInternalName(owner))) {
					realOwner = superClass.internalName();
				}
				if (owner.equals(aspectImpl.internalName())) {
					realOwner = thisClass.internalName();
				}
				write.visitFieldInsn(opcode, realOwner, name, descriptor);
			}

			@Override
			public void visitTypeInsn(int opcode, String type) {
				var realType = type;
				if (type.equals(aspectImpl.internalName())) {
					realType = thisClass.internalName();
				}
				write.visitTypeInsn(opcode, realType);
			}
		};
		byMethod.accept(swap);
	}
}
