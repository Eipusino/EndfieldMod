package aspector.classes;

public interface BytecodeLoader {
	void declareClass(String name, byte[] bytecode);

	Class<?> loadClass(String name);
}
