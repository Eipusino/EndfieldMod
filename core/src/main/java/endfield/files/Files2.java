package endfield.files;

import arc.files.Fi;
import endfield.Vars2;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public final class Files2 {
	public static Fi otherDir = Vars2.internalTree.child("other");

	private Files2() {}

	/**
	 * Move the specified named subdirectories and their contents to the new subdirectories.
	 * <p>If the new subdirectories do not exist, they will be created.
	 *
	 * @param parent   Original parent directory object
	 * @param newName  Name of the new subdirectories
	 * @param oldNames Names of one or more old subdirectories to be moved
	 * @return Return a new subdirectories object, regardless of whether the move operation is successful
	 * or not
	 * @author I hope...
	 */
	public static Fi child(Fi parent, String newName, String... oldNames) {
		// Create or retrieve new subdirectories
		Fi child = parent.child(newName);

		// Traverse the array of old subdirectories' names
		for (String oldName : oldNames) {
			// Retrieve old subdirectories objects
			Fi old = parent.child(oldName);

			// If the old subdirectories exist and are directories, move them to the new subdirectories
			if (old.exists() && old.isDirectory()) old.moveTo(child);
		}

		// Return new subdirectories object
		return child;
	}

	/**
	 * @return {@code true} if the file be deleted successfully
	 * @author I hope...
	 */
	public static boolean delete(Fi fi) {
		return fi.exists() && (fi.isDirectory() ? fi.deleteDirectory() : fi.delete());
	}

	/**
	 * Read the specified JAR file, modify the versions of all. class files within it, and then write them back.
	 *
	 * @param sourceJarFile   Source JAR file.
	 * @param newClassVersion The new class version uses the constant Opcodes.V_XX.
	 * @throws IOException If an I/O error occurs.
	 * @author I hope...
	 */
	public static void processJar(File sourceJarFile, int newClassVersion) throws IOException {
		if (!sourceJarFile.exists()) {
			throw new FileNotFoundException("File not found: " + sourceJarFile.getAbsolutePath());
		}

		// Create a temporary file for writing modified content to avoid modifying the source file during reading.
		File tempJarFile = File.createTempFile(sourceJarFile.getName(), ".tmp");

		try (JarInputStream jis = new JarInputStream(new BufferedInputStream(new FileInputStream(sourceJarFile)));
		     JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(tempJarFile)))) {

			JarEntry entry;
			// Traverse every entry in the jar file
			while ((entry = jis.getNextJarEntry()) != null) {
				// Write the entry into a new jar file
				jos.putNextEntry(new JarEntry(entry.getName()));

				if (entry.getName().endsWith(".class")) {
					// If it is a class file, read the bytecode and convert it
					byte[] originalBytes = readAllBytes(jis);
					byte[] modifiedBytes = update(originalBytes, newClassVersion);
					jos.write(modifiedBytes);
				} else {
					// If it is not a class file (such as a resource file, MANIFEST. MF), copy the content directly
					copyStream(jis, jos);
				}
				jos.closeEntry();
			}
		}

		// After successful operation, replace the original file with the modified temporary file
		Files.move(tempJarFile.toPath(), sourceJarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	public static byte[] readAllBytes(InputStream is) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] data = new byte[4096];
		int read;
		while ((read = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, read);
		}
		return buffer.toByteArray();
	}

	/**
	 * Copy the content of the input stream to the output stream.
	 *
	 * @author I hope...
	 */
	public static void copyStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[4096];
		int bytesRead;
		while ((bytesRead = in.read(buffer)) != -1) {
			out.write(buffer, 0, bytesRead);
		}
	}

	/**
	 * Receive a byte array of a class file and return a new byte array with a modified version number.
	 *
	 * @param classBytes The original class bytecode.
	 * @param newVersion New target version (e.g. Opcodes. V1_8).
	 * @return The modified version of the class bytecode.
	 * @author I hope...
	 */
	public static byte[] update(byte[] classBytes, final int newVersion) {
		ClassReader cr = new ClassReader(classBytes);
		ClassWriter cw = new ClassWriter(cr, 0);

		ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
			@Override
			public void visit(int version, int access, String name, String signature, String superName,
			                  String[] interfaces) {
				// Call the parent class method, but pass in a new version number
				super.visit(newVersion, access, name, signature, superName, interfaces);
			}
		};

		cr.accept(cv, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

		return cw.toByteArray();
	}
}
