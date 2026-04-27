package endfield.graphics

import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin

abstract class Expression {
	abstract fun diff(variable: Variable): Expression
	open fun simplify(): Expression = this
	abstract override fun toString(): String
}

operator fun Expression.plus(expression: Expression): Expression = Plus(this, expression).simplify()
operator fun Expression.minus(expression: Expression): Expression = Minus(this, expression).simplify()
operator fun Expression.times(variable: Expression): Expression = Times(this, variable).simplify()
operator fun Expression.div(variable: Expression): Expression = Division(this, variable).simplify()

operator fun Expression.unaryMinus(): Expression = UnaryMinus(this)

fun constant(value: Double) = Constant(value)
fun constant(value: Float) = Constant(value.toDouble())
fun constant(value: Int) = Constant(value.toDouble())

fun variable(name: String) = Variable(name)

fun ln(expression: Expression) = Ln(expression).simplify()
fun exp(base: Expression) = Exp(base).simplify()
fun sin(expression: Expression) = Sin(expression).simplify()
fun cos(expression: Expression) = Cos(expression).simplify()

fun pow(base: Expression, exponent: Constant) = Power(base, exponent).simplify()
fun exp(base: Constant, exponent: Expression) = Exponential(base, exponent).simplify()

fun vec2(v1: Expression, v2: Expression) = Vec2(v1, v2).simplify()
fun vec3(v1: Expression, v2: Expression, v3: Expression) = Vec3(v1, v2, v3).simplify()
fun vec4(v1: Expression, v2: Expression, v3: Expression, v4: Expression) = Vec4(v1, v2, v3, v4).simplify()

class Constant(
	@JvmField
	val value: Double
) : Expression() {
	override fun diff(variable: Variable): Expression = constant(0.0)

	override fun toString(): String = "$value"
}

open class Variable(
	@JvmField
	val name: String
) : Expression() {
	override fun diff(variable: Variable): Expression =
		if (name == variable.name) constant(1.0) else constant(0.0)

	override fun toString(): String = name
}

class Vec2(
	@JvmField
	val v1: Expression,
	@JvmField
	val v2: Expression
) : Expression() {
	override fun diff(variable: Variable): Expression =
		vec2(v1.diff(variable), v2.diff(variable))

	override fun simplify(): Expression = Vec2(v1.simplify(), v2.simplify())

	override fun toString(): String = if (v1 == v2) "vec2($v1)" else "vec2($v1, $v2)"
}

class Vec3(
	@JvmField
	val v1: Expression,
	@JvmField
	val v2: Expression,
	@JvmField
	val v3: Expression
) : Expression() {
	override fun diff(variable: Variable): Expression =
		vec3(v1.diff(variable), v2.diff(variable), v3.diff(variable))

	override fun simplify(): Expression = Vec3(v1.simplify(), v2.simplify(), v3.simplify())

	override fun toString(): String = if (v1 == v2) "vec3($v1)" else "vec3($v1, $v2, $v3)"
}

class Vec4(
	@JvmField
	val v1: Expression,
	@JvmField
	val v2: Expression,
	@JvmField
	val v3: Expression,
	@JvmField
	val v4: Expression
) : Expression() {
	override fun diff(variable: Variable): Expression =
		vec4(v1.diff(variable), v2.diff(variable), v3.diff(variable), v4.diff(variable))

	override fun simplify(): Expression = Vec4(v1.simplify(), v2.simplify(), v3.simplify(), v4.simplify())

	override fun toString(): String = if (v1 == v2) "vec4($v1)" else "vec4($v1, $v2, $v3, $v4)"
}

class Plus(
	@JvmField
	val expLeft: Expression,
	@JvmField
	val expRight: Expression,
): Expression() {
	override fun diff(variable: Variable) =
		expLeft.diff(variable) + expRight.diff(variable)

	override fun simplify(): Expression {
		val l = expLeft.simplify()
		val r = expRight.simplify()
		return when {
			l is Constant && l.value == 0.0 -> r
			r is Constant && r.value == 0.0 -> l
			l is Constant && r is Constant -> constant(l.value + r.value)
			l is UnaryMinus && r is UnaryMinus -> -(l.exp + r.exp)
			l is UnaryMinus -> r - l.exp
			r is UnaryMinus -> l - r.exp
			else -> this
		}
	}

	override fun toString(): String = "($expLeft + $expRight)"
}

class Minus(
	@JvmField
	val expLeft: Expression,
	@JvmField
	val expRight: Expression,
): Expression() {
	override fun diff(variable: Variable): Expression =
		expLeft.diff(variable) - expRight.diff(variable)

	override fun simplify(): Expression {
		val l = expLeft.simplify()
		val r = expRight.simplify()
		return when {
			l is Constant && l.value == 0.0 -> -r
			r is Constant && r.value == 0.0 -> l
			l is Constant && r is Constant -> constant(l.value - r.value)
			l is UnaryMinus && r is UnaryMinus -> r.exp - l.exp
			l is UnaryMinus -> -(l.exp + r)
			r is UnaryMinus -> l + r.exp
			else -> this
		}
	}

	override fun toString(): String = "($expLeft - $expRight)"
}

class Times(
	@JvmField
	val expLeft: Expression,
	@JvmField
	val expRight: Expression,
): Expression() {
	override fun diff(variable: Variable): Expression =
		expLeft * expRight.diff(variable) + expLeft.diff(variable)*expRight

	private fun flowTimes(childFlow: (Times) -> Unit) {
		childFlow(this)
		if (expLeft is Times) expLeft.flowTimes(childFlow)
		if (expRight is Times) expRight.flowTimes(childFlow)
	}

	override fun simplify(): Expression {
		val l = expLeft.simplify()
		val r = expRight.simplify()
		return when {
			(l is Constant && l.value == 0.0) || (r is Constant && r.value == 0.0) -> constant(0.0)
			l is Constant && l.value == 1.0 -> r
			r is Constant && r.value == 1.0 -> l
			l is Constant && r is Constant -> constant(l.value * r.value)
			r is Constant && l is Times && l.expLeft is Constant ->
				Times(constant(r.value*l.expLeft.value), l)
			l is Constant && r is Times && r.expLeft is Constant ->
				Times(constant(l.value*r.expLeft.value), r.expRight)
			l is UnaryMinus && r is UnaryMinus -> l.exp * r.exp
			l is UnaryMinus -> -(l.exp * r)
			r is UnaryMinus -> -(l * r.exp)
			else -> this
		}
	}

	override fun toString(): String = "$expLeft*$expRight"
}

class Division(
	@JvmField
	val expLeft: Expression,
	@JvmField
	val expRight: Expression,
): Expression() {
	override fun diff(variable: Variable): Expression =
		(expLeft.diff(variable)*expRight - expLeft*expRight.diff(variable))/(expRight*expLeft)

	private fun flowDivision(childFlow: (Division) -> Unit) {
		childFlow(this)
		if (expLeft is Division) expLeft.flowDivision(childFlow)
		if (expRight is Division) expRight.flowDivision(childFlow)
	}

	override fun simplify(): Expression {
		val l = expLeft.simplify()
		val r = expRight.simplify()
		return when {
			l is Constant && l.value == 0.0 -> constant(0.0)
			r is Constant && r.value == 1.0 -> l
			l is Constant && r is Constant -> constant(l.value / r.value)
			else -> this
		}
	}

	override fun toString(): String = "$expLeft/$expRight"
}

class UnaryMinus(
	@JvmField
	val exp: Expression,
): Expression() {
	override fun diff(variable: Variable): Expression =
		-exp.diff(variable)

	override fun simplify(): Expression {
		return when (val e = exp.simplify()) {
			is Constant -> constant(-e.value)
			is UnaryMinus -> exp.simplify()
			else -> this
		}
	}

	override fun toString(): String = "-$exp"
}

class Sin(
	@JvmField
	val exp: Expression
): Expression() {
	override fun diff(variable: Variable): Expression =
		Cos(exp) * exp.diff(variable)

	override fun simplify(): Expression {
		return when (val e = exp.simplify()) {
			is Constant -> constant(sin(e.value))
			else -> this
		}
	}

	override fun toString(): String = "sin($exp)"
}

class Cos(
	@JvmField
	val exp: Expression
): Expression() {
	override fun diff(variable: Variable): Expression =
		-Sin(exp) * exp.diff(variable)

	override fun simplify(): Expression {
		return when (val e = exp.simplify()) {
			is Constant -> constant(sin(e.value))
			else -> this
		}
	}

	override fun toString(): String = "cos($exp)"
}

class Ln(
	@JvmField
	val exp: Expression
): Expression() {
	override fun diff(variable: Variable): Expression =
		exp.diff(variable)/variable

	override fun simplify(): Expression {
		return when (val e = exp.simplify()) {
			is Constant if e.value == 1.0 -> constant(0.0)
			is Constant if e.value == 0.0 -> constant(Double.POSITIVE_INFINITY)
			else -> this
		}
	}

	override fun toString(): String = "log($exp)"
}

class Power(
	@JvmField
	val expBase: Expression,
	@JvmField
	val exponent: Constant,
): Expression() {
	override fun diff(variable: Variable): Expression =
		exponent * (pow(expBase, constant(exponent.value - 1.0))) * expBase.diff(variable)

	override fun simplify(): Expression {
		val b = expBase.simplify()
		val e = exponent
		return when {
			b is Constant -> constant(b.value.pow(e.value))
			e.value == 0.0 -> constant(1.0)
			e.value == 1.0 -> b
			else -> this
		}
	}

	override fun toString(): String = "pow($expBase, $exponent)"
}

class Exponential(
	@JvmField
	val base: Constant,
	@JvmField
	val exp: Expression
): Expression(){
	override fun diff(variable: Variable): Expression =
		exp(base, exp) * ln(base) * exp.diff(variable)

	override fun simplify(): Expression {
		val e = exp.simplify()
		val eb = base
		return when {
			e is Constant -> constant(eb.value.pow(e.value))
			eb.value == 0.0 -> constant(0.0)
			eb.value == 1.0 -> constant(1.0)
			else -> this
		}
	}

	override fun toString(): String = "exponential($base, $exp)"
}

class Exp(
	@JvmField
	val exp: Expression
): Expression() {
	override fun diff(variable: Variable): Expression =
		Exp(exp) * exp.diff(variable)

	override fun simplify(): Expression {
		return when (val e = exp.simplify()) {
			is Constant -> constant(exp(e.value))
			else -> this
		}
	}

	override fun toString(): String = "exp($exp)"
}
