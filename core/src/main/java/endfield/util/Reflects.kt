package endfield.util

import endfield.Vars2.platformImpl
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodType
import java.lang.invoke.VarHandle

fun findSetter(clazz: Class<*>, name: String, type: Class<*>): MethodHandle {
	val lookup = platformImpl.lookup(clazz)

	return lookup.findSetter(clazz, name, type)
}

fun findGetter(clazz: Class<*>, name: String, type: Class<*>): MethodHandle {
	val lookup = platformImpl.lookup(clazz)

	return lookup.findGetter(clazz, name, type)
}

fun findVarHandle(clazz: Class<*>, name: String, type: Class<*>): VarHandle {
	val lookup = platformImpl.lookup(clazz)

	return lookup.findVarHandle(clazz, name, type)
}

fun findVirtual(clazz: Class<*>, name: String, returnType: Class<*>, parameterTypes: Array<Class<*>>): MethodHandle {
	val lookup = platformImpl.lookup(clazz)
	val methodType = MethodType.methodType(returnType, parameterTypes)

	return lookup.findVirtual(clazz, name, methodType)
}

fun findStatic(clazz: Class<*>, name: String, returnType: Class<*>, parameterTypes: Array<Class<*>>): MethodHandle {
	val lookup = platformImpl.lookup(clazz)
	val methodType = MethodType.methodType(returnType, parameterTypes)

	return lookup.findStatic(clazz, name, methodType)
}