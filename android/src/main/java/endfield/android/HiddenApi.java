package endfield.android;

import arc.util.OS;
import dalvik.system.VMRuntime;

import java.lang.reflect.Method;

import static endfield.android.Unsafer.unsafe;

/** Only For Android */
@SuppressWarnings("removal")
public final class HiddenApi {
	public static final long intBytes = Integer.BYTES;
	/**
	 * <a href="https://cs.android.com/android/platform/superproject/main/+/main:art/runtime/mirror/executable.h;bpv=1;bpt=1;l=73?q=executable&ss=android&gsn=art_method_&gs=KYTHE%3A%2F%2Fkythe%3A%2F%2Fandroid.googlesource.com%2Fplatform%2Fsuperproject%2Fmain%2F%2Fmain%3Flang%3Dc%252B%252B%3Fpath%3Dart%2Fruntime%2Fmirror%2Fexecutable.h%23GLbGh3aGsjxEudfgKrvQvNcLL3KUjmUaJTc4nCOKuVY">
	 * uint64_t Executable::art_method_</a>
	 */
	public static final long artMethodOffset = 24l;

	static String[] values = {
			"Ldalvik/system/",
			"Ljava/lang/Class;",
			"Ljava/lang/Enum;",
			"Ljava/lang/Object;->internalClone()Ljava/lang/Object;",
			"Ljava/lang/invoke/",
			"Ljava/lang/reflect/",
			"Ljava/nio/",
			"Ljdk/internal/misc/Unsafe;",
			"Llibcore/io/Memory;",
			"Lsun/misc/Unsafe;",
			"Lsun/nio/ch/DirectBuffer;"
	};
	static final VMRuntime runtime = VMRuntime.getRuntime();
	// Not using the 'L' wildcard is to ensure basic security and prevent strange issues caused by things we don't want to call in certain parts of the program.
	//static final String[] values = {"L"};

	static Method method;

	static Object[] oneArray;

	static long offset;

	private HiddenApi() {}

	static void setup() throws Throwable {
		if (AndroidProperties.setup()) return;

		oneArray = (Object[]) runtime.newNonMovableArray(Object.class, 1);
		int[] intArray = (int[]) runtime.newNonMovableArray(int.class, 0);
		offset = runtime.addressOf(intArray) - vaddressOf(intArray);

		// In higher versions, the setHiddenApiExertions method cannot be directly reflected to obtain it, so the artMethod needs to be modified
		// Sdk_version>28 (exact number unknown)
		method = findMethod();

		method.setAccessible(true);
		method.invoke(runtime, (Object) values);
	}

	private static Method findMethod() throws NoSuchMethodException {
		Method[] methods = VMRuntime.class.getDeclaredMethods();
		if (methods[0].getName().equals("setHiddenApiExemptions")) {
			return methods[0];
		}

		int length = methods.length;
		Method[] array = (Method[]) runtime.newNonMovableArray(Method.class, length);
		System.arraycopy(methods, 0, array, 0, length);

		long address = addressOf(array);
		long min = Long.MAX_VALUE, minSecond = Long.MAX_VALUE, max = Long.MIN_VALUE;
		/* Find artMethod  */
		for (int k = 0; k < length; ++k) {
			final long addressKBs = address + k * intBytes;
			final long addressMethod = unsafe.getInt(addressKBs);
			final long addressArtMethod = unsafe.getLong(addressMethod + artMethodOffset);
			if (min >= addressArtMethod) {
				min = addressArtMethod;
			} else if (minSecond >= addressArtMethod) {
				minSecond = addressArtMethod;
			}
			if (max <= addressArtMethod) {
				max = addressArtMethod;
			}
		}

		// The difference between two artMethods (due to continuity)
		final long sizeArtMethod = minSecond - min;

		//Log.debug("sizeArtMethod: " + sizeArtMethod);

		if (sizeArtMethod > 0 && sizeArtMethod < 100) {
			for (long artMethod = minSecond; artMethod < max; artMethod += sizeArtMethod) {
				// This obtains the * Method of array [0], with a size of 32 bits
				final long addressMethod = unsafe.getInt(address);
				// Modify the artMethod of the first method
				unsafe.putLong(addressMethod + artMethodOffset, artMethod);
				// Android's getName is a native implementation, and by modifying artMethod, the name will naturally change
				if ("setHiddenApiExemptions".equals(array[0].getName())) {
					//Log.debug("Got: " + array[0]);

					return array[0];
				}
			}
		}

		throw new NoSuchMethodException();
	}

	public static long addressOf(Object obj) {
		return vaddressOf(obj) + offset;
	}

	// ---------Address/Memory Operation---------
	public static long vaddressOf(Object object) {
		if (object == null) throw new IllegalArgumentException("object is null.");
		oneArray[0] = object;
		long baseOffset = unsafe.arrayBaseOffset(Object[].class);
		return switch (unsafe.arrayIndexScale(Object[].class)) {
			case 4 -> (unsafe.getInt(oneArray, baseOffset) & 0xffffffffl) * (OS.is64Bit ? 8 : 1);
			case 8 -> unsafe.getLong(oneArray, baseOffset);
			default -> throw new UnsupportedOperationException("Unsupported address size: " + unsafe.arrayIndexScale(Object[].class));
		};
	}
}
