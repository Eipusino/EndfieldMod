package endfield.graphics

import arc.graphics.gl.Shader

abstract class MathShaderBuilder {
	companion object;

	private val uniforms = mutableListOf<Uniform>()
	private val namedFunctions = mutableListOf<NamedFunction>()
	private lateinit var function: GLExpression
	private lateinit var gradient: GLExpression

	fun makeUniform(name: String): Uniform = Uniform(name).also { uniforms.add(it) }
	fun addNamedFunc(function: NamedFunction) = also { namedFunctions.add(function) }

	fun setFunction(function: GLExpression) = also { it.function = function }
	fun setGradient(function: GLExpression) = also { it.gradient = function }

	fun build(): Shader {
		val varBuilder = StringBuilder()
		namedFunctions.forEach {
			varBuilder.append("${it.type} ${it.name} = ${it.expression};\n")
			it.getDiffFunctions().forEach { diff ->
				varBuilder.append("${diff.type} ${diff.name} = ${diff.expression};\n")
			}
			varBuilder.append("\n")
		}

		val function = function
		if (this::gradient.isInitialized) gradient
		else vec2(function.diff(variable("x")), function.diff(variable("y")))

		return Shader(
			"""
attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;
attribute vec4 a_mix_color;
uniform mat4 u_projTrans;
varying vec4 v_color;
varying vec4 v_mix_color;
varying vec2 v_texCoords;

void main() {
	v_color = a_color;
	v_color.a = v_color.a * (255.0/254.0);
	v_mix_color = a_mix_color;
	v_mix_color.a *= (255.0/254.0);
	v_texCoords = a_texCoord0;
	gl_Position = u_projTrans * a_position;
}
""", """
varying vec2 v_texCoords;
varying vec4 v_mix_color;
varying vec4 v_texCoords;
""")
	}

	open class NamedFunction(
		name: String,
		val expression: GLExpression,
		val type: String = predictType(expression),
		vararg val args: GLVariable,
	) : GLVariable(name) {
		companion object {
			private fun predictType(expression: GLExpression): String = when (expression) {
				is GLUnaryMinus -> predictType(expression.exp)
				is GLPlus -> predictType(expression.expLeft)
				is GLMinus -> predictType(expression.expLeft)
				is GLTimes -> {
					val l = predictType(expression.expLeft)
					val r = predictType(expression.expRight)

					if (l == r) l
					else if (l.startsWith("vec") && r == "float") l
					else if (r.startsWith("vec") && l == "float") r
					else throw IllegalArgumentException("Cannot determine type of expression: $expression")
				}

				is GLDivision -> {
					val l = predictType(expression.expLeft)
					val r = predictType(expression.expRight)

					if (l == r) l
					else if (l.startsWith("vec") && r == "float") l
					else throw IllegalArgumentException("Cannot determine type of expression: $expression")
				}

				is GLVec2 -> "vec2"
				is GLVec3 -> "vec3"
				is GLVec4 -> "vec4"
				else -> "float"
			}
		}

		fun getDiffFunctions(): List<NamedFunction> = args.map {
			val f = expression.diff(it).simplify()

			NamedFunction(name + "_diff_" + it.name, f, type, *args)
		}

		override fun diff(variable: GLVariable): GLExpression {
			return if (args.any { it.name == variable.name }) GLVariable(name + "_diff_" + variable.name)
			else constant(0.0)
		}

		override fun toString(): String = name
	}

	class Uniform(
		name: String,
	) : GLVariable(name) {
		lateinit var shader: Shader

		fun set(int: Int) {
			shader.bind()
			shader.setUniformi(name, int)
		}

		fun set(float: Float) {
			shader.bind()
			shader.setUniformf(name, float)
		}
	}
}