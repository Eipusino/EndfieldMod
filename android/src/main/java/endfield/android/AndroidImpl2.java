package endfield.android;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;

import static endfield.android.InternalUnsafer.internalUnsafe;

// Sdk_version>=33
@TargetApi(VERSION_CODES.TIRAMISU)
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
