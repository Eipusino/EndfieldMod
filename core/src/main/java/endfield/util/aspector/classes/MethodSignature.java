package endfield.util.aspector.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MethodSignature {
	static final Pattern NAMED_SIGNATURE_MATCHER = Pattern.compile("^[^.;()]+\\(([BCDFIJSZ]|\\[+[BCDFIJSZ]|\\[*L[^;]+;)*\\)([BCDFIJSZ]|\\[+[BCDFIJSZ]|V|\\[*L[^;]+;)$");
	static final Pattern SIGNATURE_MATCHER = Pattern.compile("^\\(([BCDFIJSZ]|\\[+[BCDFIJSZ]|\\[*L[^;]+;)*\\)([BCDFIJSZ]|\\[+[BCDFIJSZ]|V|\\[*L[^;]+;)$");

	public String methodName;
	public List<ClassName> paramTypes;
	public ClassName returnType;

	int hash = -1;

	public MethodSignature(String name, List<ClassName> pTypes, ClassName rType) {
		methodName = name;
		paramTypes = pTypes;
		returnType = rType;
	}

	public static MethodSignature parse(String methodName, String signature) {
		if (!SIGNATURE_MATCHER.matcher(signature).matches())
			throw new IllegalArgumentException("Invalid signature string: " + signature);
		if (methodName.contains("(") || methodName.contains(")") || methodName.contains(";"))
			throw new IllegalArgumentException("Invalid method name: " + methodName);
		return parse(methodName + signature);
	}

	public static MethodSignature parse(String signature) {
		if (!NAMED_SIGNATURE_MATCHER.matcher(signature).matches())
			throw new IllegalArgumentException("Invalid signature string: " + signature);

		StringBuilder builder = new StringBuilder();

		String name = "";
		boolean ret = false;
		List<ClassName> paramTypes = new ArrayList<>();
		ClassName returnType = null;
		for (char c : signature.toCharArray()) {
			switch (c) {
				case '(' -> {
					name = builder.toString();
					builder.setLength(0);
				}
				case ';' -> {
					builder.append(c);

					if (ret) {
						returnType = new ClassName(builder.toString());
						builder.setLength(0);
					} else {
						paramTypes.add(new ClassName(builder.toString()));
						builder.setLength(0);
					}
				}
				case ')' -> {
					builder.setLength(0);
					ret = true;
				}
				default -> builder.append(c);
			}
		}

		return new MethodSignature(name, paramTypes, returnType == null ? new ClassName(builder.toString()) : returnType);
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || obj instanceof MethodSignature sig
				&& methodName.equals(sig.methodName)
				&& paramTypes.equals(sig.paramTypes)
				&& returnType.equals(sig.returnType);
	}

	@Override
	public int hashCode() {
		if (hash != -1) return hash;

		int h = methodName.hashCode();
		h = 31 * h + paramTypes.hashCode();
		h = 31 * h + returnType.hashCode();
		hash = h;

		return h;
	}

	public String jvmDescriptor() {
		StringBuilder buf = new StringBuilder();
		buf.append('(');
		for (ClassName paramType : paramTypes) {
			buf.append(paramType.descriptor);
		}
		buf.append(')');
		buf.append(returnType.descriptor);
		return buf.toString();
	}
}
