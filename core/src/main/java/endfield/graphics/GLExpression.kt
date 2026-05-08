package endfield.graphics

import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

abstract class GLExpression {
	abstract fun diff(variable: GLVariable): GLExpression
	open fun simplify(): GLExpression = this
	abstract override fun toString(): String
}

operator fun GLExpression.plus(expression: GLExpression): GLExpression = GLPlus(this, expression).simplify()
operator fun GLExpression.minus(expression: GLExpression): GLExpression = GLMinus(this, expression).simplify()
operator fun GLExpression.times(variable: GLExpression): GLExpression = GLTimes(this, variable).simplify()
operator fun GLExpression.div(variable: GLExpression): GLExpression = GLDivision(this, variable).simplify()

operator fun GLExpression.unaryMinus(): GLExpression = GLUnaryMinus(this)

fun constant(value: Double) = GLConstant(value)
fun constant(value: Float) = GLConstant(value.toDouble())
fun constant(value: Int) = GLConstant(value.toDouble())

fun variable(name: String) = GLVariable(name)

fun ln(expression: GLExpression) = GLLn(expression).simplify()
fun exp(base: GLExpression) = GLExp(base).simplify()
fun sin(expression: GLExpression) = GLSin(expression).simplify()
fun cos(expression: GLExpression) = GLCos(expression).simplify()

fun pow(base: GLExpression, exponent: GLConstant) = GLPower(base, exponent).simplify()
fun exp(base: GLConstant, exponent: GLExpression) = GLExponential(base, exponent).simplify()

fun vec2(v1: GLExpression, v2: GLExpression) = GLVec2(v1, v2).simplify()
fun vec3(v1: GLExpression, v2: GLExpression, v3: GLExpression) = GLVec3(v1, v2, v3).simplify()
fun vec4(v1: GLExpression, v2: GLExpression, v3: GLExpression, v4: GLExpression) = GLVec4(v1, v2, v3, v4).simplify()

class GLConstant(
	@JvmField
	val value: Double
) : GLExpression() {
	override fun diff(variable: GLVariable): GLExpression = constant(0.0)

	override fun toString(): String = "$value"
}

open class GLVariable(
	@JvmField
	val name: String
) : GLExpression() {
	override fun diff(variable: GLVariable): GLExpression =
		if (name == variable.name) constant(1.0) else constant(0.0)

	override fun toString(): String = name
}

class GLVec2(
	@JvmField
	val v1: GLExpression,
	@JvmField
	val v2: GLExpression
) : GLExpression() {
	override fun diff(variable: GLVariable): GLExpression =
		vec2(v1.diff(variable), v2.diff(variable))

	override fun simplify(): GLExpression = GLVec2(v1.simplify(), v2.simplify())

	override fun toString(): String = if (v1 == v2) "vec2($v1)" else "vec2($v1, $v2)"
}

class GLVec3(
	@JvmField
	val v1: GLExpression,
	@JvmField
	val v2: GLExpression,
	@JvmField
	val v3: GLExpression
) : GLExpression() {
	override fun diff(variable: GLVariable): GLExpression =
		vec3(v1.diff(variable), v2.diff(variable), v3.diff(variable))

	override fun simplify(): GLExpression = GLVec3(v1.simplify(), v2.simplify(), v3.simplify())

	override fun toString(): String = if (v1 == v2) "vec3($v1)" else "vec3($v1, $v2, $v3)"
}

class GLVec4(
	@JvmField
	val v1: GLExpression,
	@JvmField
	val v2: GLExpression,
	@JvmField
	val v3: GLExpression,
	@JvmField
	val v4: GLExpression
) : GLExpression() {
	override fun diff(variable: GLVariable): GLExpression =
		vec4(v1.diff(variable), v2.diff(variable), v3.diff(variable), v4.diff(variable))

	override fun simplify(): GLExpression = GLVec4(v1.simplify(), v2.simplify(), v3.simplify(), v4.simplify())

	override fun toString(): String = if (v1 == v2) "vec4($v1)" else "vec4($v1, $v2, $v3, $v4)"
}

class GLPlus(
	@JvmField
	val expLeft: GLExpression,
	@JvmField
	val expRight: GLExpression,
) : GLExpression() {
	override fun diff(variable: GLVariable) =
		expLeft.diff(variable) + expRight.diff(variable)

	override fun simplify(): GLExpression {
		val l = expLeft.simplify()
		val r = expRight.simplify()
		return when {
			l is GLConstant && l.value == 0.0 -> r
			r is GLConstant && r.value == 0.0 -> l
			l is GLConstant && r is GLConstant -> constant(l.value + r.value)
			l is GLUnaryMinus && r is GLUnaryMinus -> -(l.exp + r.exp)
			l is GLUnaryMinus -> r - l.exp
			r is GLUnaryMinus -> l - r.exp
			else -> this
		}
	}

	override fun toString(): String = "($expLeft + $expRight)"
}

class GLMinus(
	@JvmField
	val expLeft: GLExpression,
	@JvmField
	val expRight: GLExpression,
) : GLExpression() {
	override fun diff(variable: GLVariable): GLExpression =
		expLeft.diff(variable) - expRight.diff(variable)

	override fun simplify(): GLExpression {
		val l = expLeft.simplify()
		val r = expRight.simplify()
		return when {
			l is GLConstant && l.value == 0.0 -> -r
			r is GLConstant && r.value == 0.0 -> l
			l is GLConstant && r is GLConstant -> constant(l.value - r.value)
			l is GLUnaryMinus && r is GLUnaryMinus -> r.exp - l.exp
			l is GLUnaryMinus -> -(l.exp + r)
			r is GLUnaryMinus -> l + r.exp
			else -> this
		}
	}

	override fun toString(): String = "($expLeft - $expRight)"
}

class GLTimes(
	@JvmField
	val expLeft: GLExpression,
	@JvmField
	val expRight: GLExpression,
) : GLExpression() {
	override fun diff(variable: GLVariable): GLExpression =
		expLeft * expRight.diff(variable) + expLeft.diff(variable) * expRight

	private fun flowTimes(childFlow: (GLTimes) -> Unit) {
		childFlow(this)
		if (expLeft is GLTimes) expLeft.flowTimes(childFlow)
		if (expRight is GLTimes) expRight.flowTimes(childFlow)
	}

	override fun simplify(): GLExpression {
		val l = expLeft.simplify()
		val r = expRight.simplify()
		return when {
			(l is GLConstant && l.value == 0.0) || (r is GLConstant && r.value == 0.0) -> constant(0.0)
			l is GLConstant && l.value == 1.0 -> r
			r is GLConstant && r.value == 1.0 -> l
			l is GLConstant && r is GLConstant -> constant(l.value * r.value)
			r is GLConstant && l is GLTimes && l.expLeft is GLConstant ->
				GLTimes(constant(r.value * l.expLeft.value), l)

			l is GLConstant && r is GLTimes && r.expLeft is GLConstant ->
				GLTimes(constant(l.value * r.expLeft.value), r.expRight)

			l is GLUnaryMinus && r is GLUnaryMinus -> l.exp * r.exp
			l is GLUnaryMinus -> -(l.exp * r)
			r is GLUnaryMinus -> -(l * r.exp)
			else -> this
		}
	}

	override fun toString(): String = "$expLeft*$expRight"
}

class GLDivision(
	@JvmField
	val expLeft: GLExpression,
	@JvmField
	val expRight: GLExpression,
) : GLExpression() {
	override fun diff(variable: GLVariable): GLExpression =
		(expLeft.diff(variable) * expRight - expLeft * expRight.diff(variable)) / (expRight * expLeft)

	private fun flowDivision(childFlow: (GLDivision) -> Unit) {
		childFlow(this)
		if (expLeft is GLDivision) expLeft.flowDivision(childFlow)
		if (expRight is GLDivision) expRight.flowDivision(childFlow)
	}

	override fun simplify(): GLExpression {
		val l = expLeft.simplify()
		val r = expRight.simplify()
		return when {
			l is GLConstant && l.value == 0.0 -> constant(0.0)
			r is GLConstant && r.value == 1.0 -> l
			l is GLConstant && r is GLConstant -> constant(l.value / r.value)
			else -> this
		}
	}

	override fun toString(): String = "$expLeft/$expRight"
}

class GLUnaryMinus(
	@JvmField
	val exp: GLExpression,
) : GLExpression() {
	override fun diff(variable: GLVariable): GLExpression =
		-exp.diff(variable)

	override fun simplify(): GLExpression {
		return when (val e = exp.simplify()) {
			is GLConstant -> constant(-e.value)
			is GLUnaryMinus -> exp.simplify()
			else -> this
		}
	}

	override fun toString(): String = "-$exp"
}

class GLSin(
	@JvmField
	val exp: GLExpression
) : GLExpression() {
	override fun diff(variable: GLVariable): GLExpression =
		GLCos(exp) * exp.diff(variable)

	override fun simplify(): GLExpression {
		return when (val e = exp.simplify()) {
			is GLConstant -> constant(sin(e.value))
			else -> this
		}
	}

	override fun toString(): String = "sin($exp)"
}

class GLCos(
	@JvmField
	val exp: GLExpression
) : GLExpression() {
	override fun diff(variable: GLVariable): GLExpression =
		-GLSin(exp) * exp.diff(variable)

	override fun simplify(): GLExpression {
		return when (val e = exp.simplify()) {
			is GLConstant -> constant(sin(e.value))
			else -> this
		}
	}

	override fun toString(): String = "cos($exp)"
}

class GLLn(
	@JvmField
	val exp: GLExpression
) : GLExpression() {
	override fun diff(variable: GLVariable): GLExpression =
		exp.diff(variable) / variable

	override fun simplify(): GLExpression {
		return when (val e = exp.simplify()) {
			is GLConstant if e.value == 1.0 -> constant(0.0)
			is GLConstant if e.value == 0.0 -> constant(Double.POSITIVE_INFINITY)
			else -> this
		}
	}

	override fun toString(): String = "log($exp)"
}

class GLPower(
	@JvmField
	val expBase: GLExpression,
	@JvmField
	val exponent: GLConstant,
) : GLExpression() {
	override fun diff(variable: GLVariable): GLExpression =
		exponent * (pow(expBase, constant(exponent.value - 1.0))) * expBase.diff(variable)

	override fun simplify(): GLExpression {
		val b = expBase.simplify()
		val e = exponent
		return when {
			b is GLConstant -> constant(b.value.pow(e.value))
			e.value == 0.0 -> constant(1.0)
			e.value == 1.0 -> b
			else -> this
		}
	}

	override fun toString(): String = "pow($expBase, $exponent)"
}

class GLExponential(
	@JvmField
	val base: GLConstant,
	@JvmField
	val exp: GLExpression
) : GLExpression() {
	override fun diff(variable: GLVariable): GLExpression =
		exp(base, exp) * ln(base) * exp.diff(variable)

	override fun simplify(): GLExpression {
		val e = exp.simplify()
		val eb = base
		return when {
			e is GLConstant -> constant(eb.value.pow(e.value))
			eb.value == 0.0 -> constant(0.0)
			eb.value == 1.0 -> constant(1.0)
			else -> this
		}
	}

	override fun toString(): String = "exponential($base, $exp)"
}

class GLExp(
	@JvmField
	val exp: GLExpression
) : GLExpression() {
	override fun diff(variable: GLVariable): GLExpression =
		GLExp(exp) * exp.diff(variable)

	override fun simplify(): GLExpression {
		return when (val e = exp.simplify()) {
			is GLConstant -> constant(exp(e.value))
			else -> this
		}
	}

	override fun toString(): String = "exp($exp)"
}
