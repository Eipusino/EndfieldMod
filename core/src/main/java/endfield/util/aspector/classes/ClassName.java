package endfield.util.aspector.classes;

import endfield.util.Strings2;
import kotlin.text.StringsKt;
import org.objectweb.asm.Type;

public class ClassName {
	public static final ClassName V = new ClassName("V");
	public static final ClassName Z = new ClassName("Z");
	public static final ClassName C = new ClassName("C");
	public static final ClassName B = new ClassName("B");
	public static final ClassName S = new ClassName("S");
	public static final ClassName I = new ClassName("I");
	public static final ClassName J = new ClassName("J");
	public static final ClassName F = new ClassName("F");
	public static final ClassName D = new ClassName("D");

	public static final ClassName OBJECT = byClass(Object.class);
	public static final ClassName STRING = byClass(String.class);
	public static final ClassName CLASS = byClass(Class.class);
	public static final ClassName ENUM = byClass(Enum.class);

	public final String descriptor;

	public ClassName(String name) {
		descriptor = name;
	}

	public static String descToInternal(String signatureName) {
		return switch (signatureName.charAt(0)) {
			case 'V' -> "void";
			case 'Z' -> "boolean";
			case 'C' -> "char";
			case 'B' -> "byte";
			case 'S' -> "short";
			case 'I' -> "int";
			case 'J' -> "long";
			case 'F' -> "float";
			case 'D' -> "double";
			case 'L' -> StringsKt.trimEnd(signatureName.substring(1), ';');
			case '[' -> descToInternal(signatureName.substring(1));
			default -> throw new IllegalArgumentException("Illegal class name: " + signatureName);
		};
	}

	public static String internalToSign(String internalName) {
		return switch (internalName) {
			case "void" -> "V";
			case "boolean" -> "Z";
			case "char" -> "C";
			case "byte" -> "B";
			case "short" -> "S";
			case "int" -> "I";
			case "long" -> "J";
			case "float" -> "F";
			case "double" -> "D";
			default -> internalName.startsWith("[") ? internalName : "L" + internalName + ";";
		};
	}

	public static ClassName byClass(Class<?> clazz) {
		return new ClassName(internalToSign(Type.getInternalName(clazz)));
	}

	public static ClassName byDescriptor(String descriptor) {
		return new ClassName(descriptor);
	}

	public static ClassName byInternalName(String internalName) {
		return new ClassName(internalToSign(internalName));
	}

	public static ClassName byName(String className) {
		return new ClassName(internalToSign(className.replace(".", "/")));
	}

	public String internalName() {
		return descToInternal(descriptor);
	}

	public String name() {
		return descToInternal(descriptor).replace('/', '.');
	}

	public String simpleName() {
		return Strings2.substringAfterLast(name(), ".");
	}

	public String packageName() {
		return Strings2.substringBeforeLast(name(), ".");
	}

	public boolean isPrimitive() {
		return descriptor.length() == 1;
	}

	public boolean isArray() {
		return descriptor.startsWith("[");
	}

	public ClassName componentName() {
		return new ClassName(Strings2.substringAfter(descriptor, "["));
	}

	public ClassName arrayName() {
		return new ClassName("[" + descriptor);
	}

	@Override
	public String toString() {
		return name();
	}

	@Override
	public int hashCode() {
		return descriptor.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || obj instanceof ClassName cn && cn.descriptor.equals(descriptor);
	}
}
