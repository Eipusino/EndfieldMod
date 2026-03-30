package endfield.async;

import arc.func.Prov;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Provides multithreading utilities, primarily synchronizations from threads to the main thread for OpenGL
 * purposes.
 *
 * @since 1.0.7
 */
public final class Asyncs {
	private Asyncs() {}

	/*@JvmStatic
	fun <T> get(future: Future<T>): T {
		return future.get()
	}

	@JvmStatic
	fun postWait(runSync: Runnable) {
		val flag = Semaphore(0)
		Core.app.post {
			try {
				runSync.run()
			} finally {
				flag.release()
			}
		}

		flag.acquire()
	}

	@JvmStatic
	fun <T> postWait(runSync: Prov<T>): T {
		val flag = Semaphore(0)
		val out = AtomicReference<T>()
		Core.app.post {
			try {
				out.set(runSync.get())
			} finally {
				flag.release()
			}
		}

		flag.acquire()

		return out.get()
	}*/

	public static <T> T lock(Lock lock, Prov<T> prov) {
		lock.lock();
		T out = prov.get();

		lock.unlock();
		return out;
	}

	public static void lock(Lock lock, Runnable run) {
		lock.lock();
		run.run();
		lock.unlock();
	}

	public static <T> T read(ReadWriteLock lock, Prov<T> prov) {
		return lock(lock.readLock(), prov);
	}

	public static void read(ReadWriteLock lock, Runnable run) {
		lock(lock.readLock(), run);
	}

	public static <T> T write(ReadWriteLock lock, Prov<T> prov) {
		return lock(lock.writeLock(), prov);
	}

	public static void write(ReadWriteLock lock, Runnable run) {
		lock(lock.writeLock(), run);
	}
}
