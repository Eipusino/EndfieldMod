package endfield.util.aspector.classes;

import endfield.util.aspector.classes.ClassDecl.PrimitiveClassDecl;

public interface ClassAccessor {
	ClassDecl<Void> VOID = new PrimitiveClassDecl<>(void.class);
	ClassDecl<Byte> BYTE = new PrimitiveClassDecl<>(byte.class);
	ClassDecl<Short> SHORT = new PrimitiveClassDecl<>(short.class);
	ClassDecl<Integer> INT = new PrimitiveClassDecl<>(int.class);
	ClassDecl<Long> LONG = new PrimitiveClassDecl<>(long.class);
	ClassDecl<Float> FLOAT = new PrimitiveClassDecl<>(float.class);
	ClassDecl<Double> DOUBLE = new PrimitiveClassDecl<>(double.class);
	ClassDecl<Character> CHAR = new PrimitiveClassDecl<>(char.class);
	ClassDecl<Boolean> BOOLEAN = new PrimitiveClassDecl<>(boolean.class);

	<T> ClassDecl<T> getClassDecl(ClassName className);

	byte[] getBytes(ClassName className);
}
