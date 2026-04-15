package endfield.async;

import arc.Core;
import arc.func.Prov;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
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

	public static <T> T get(Future<T> future) {
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public static void postWait(Runnable runSync) {
		Semaphore flag = new Semaphore(0);
		Core.app.post(() -> {
			try {
				runSync.run();
			} finally {
				flag.release();
			}
		});

		try {
			flag.acquire();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T postWait(Prov<T> runSync) {
		Semaphore flag = new Semaphore(0);
		AtomicReference<T> out = new AtomicReference<>();
		Core.app.post(() -> {
			try {
				out.set(runSync.get());
			} finally {
				flag.release();
			}
		});

		try {
			flag.acquire();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		return out.get();
	}

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
