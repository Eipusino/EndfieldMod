package endfield.util

import arc.util.UnsafeRunnable
import endfield.func.ProvT
import endfield.func.RunT

@JvmSynthetic
const val OH_NO = "oh no"

fun <T> thrower(e: Throwable): T = throw e

fun run(run: RunT<out Throwable>) = run.run()
fun run2(run: UnsafeRunnable) = run.run()

fun <T> prov(prov: ProvT<T, out Throwable>): T = prov.get()
