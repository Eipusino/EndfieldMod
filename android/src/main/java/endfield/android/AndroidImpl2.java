package endfield.android;

import static endfield.android.InternalUnsafer.internalUnsafe;

// Sdk_version>=33
public class AndroidImpl2 extends AndroidImpl {
	@Override
	public void put(long srcAddress, long destAddress, long bytes) {
		internalUnsafe.copyMemory(srcAddress, destAddress, bytes);
	}

	@Override
	public void put(Object src, int srcOffset, Object dst, int dstOffset, long bytes) {
		internalUnsafe.copyMemory(src, srcOffset, dst, dstOffset, bytes);
	}
}
