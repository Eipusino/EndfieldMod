package endfield.ui.markdown.elemdraw

import arc.graphics.Color
import arc.graphics.g2d.DistanceFieldFont
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Font
import arc.graphics.g2d.FontCache
import arc.graphics.g2d.GlyphLayout
import arc.math.Affine2
import arc.math.Mat
import arc.util.Align
import arc.util.pooling.Pools
import endfield.graphics.Shaders2
import endfield.ui.markdown.Markdown
import endfield.ui.markdown.RendererContext
import mindustry.ui.Fonts

open class DrawStr internal constructor() : Markdown.MarkdownDraw() {
	companion object {
		fun get(
			str: String,
			font: Font,
			fontOffsetX: Float,
			fontOffsetY: Float,
			italic: Boolean,
			color: Color,
			scl: Float,
		): DrawStr = Pools.obtain(DrawStr::class.java) { DrawStr() }.apply {
			this.text = str
			this.font = font
			this.fontOffX = fontOffsetX
			this.fontOffY = fontOffsetY
			this.italic = italic
			this.scl = scl
			this.color = color
		}
	}

	var text: String = ""
	var font: Font = Fonts.def
	var fontOffX: Float = 0f
	var fontOffY: Float = 0f
	var italic: Boolean = false
	var scl: Float = 0f
	var color: Color = Color.white

	private var isDistanceField: Boolean = false
	private var cache: FontCache? = null
	private var layout: GlyphLayout? = null

	override fun reset() {
		super.reset()
		text = ""
		font = Fonts.def
		fontOffX = 0f
		fontOffY = 0f
		italic = false
		scl = 0f
		color = Color.white
	}

	override fun prefWidth(): Float = layout?.width ?: 0f
	override fun prefHeight(): Float = layout?.height ?: 0f

	override fun setup(scope: RendererContext.Scope) {
		val data = font.getData()
		val lastScl = data.scaleX
		data.setScale(scl * lastScl)
		isDistanceField = font is DistanceFieldFont
		cache = font.newFontCache()
		layout = cache!!.setText(
			text,
			0f, 0f,
			0, text.length,
			0f,
			Align.topLeft,
			false,
		)
		data.setScale(lastScl)
	}

	private val lastTrans = Mat()
	private val affineTrans = Mat()
	private val transform = Mat()
	private val affine2 = Affine2()
	override fun draw(x: Float, y: Float) {
		val data = font.data
		val cache = cache!!
		cache.tint(tmp1.set(color).mul(Draw.getColor()))

		val shouldDistanceField = isDistanceField
		val lastSclX = data.scaleX
		val lastSclY = data.scaleY
		data.setScale(scl)

		if (shouldDistanceField) {
			Draw.shader(Shaders2.distance)
			Shaders2.distance.bind()
			Shaders2.distance.setUniformf("u_smoothing", 0.5f * font.scaleX)
		}
		if (italic) {
			val last = lastTrans.set(Draw.trans())
			Draw.trans(
				transform.set(last)
					.translate(x + offsetX, y - offsetY)
					.mul(affineTrans.set(affine2.idt().shear(0.25f, 0f)))
			)
			cache.setPosition(fontOffX, fontOffY)
			cache.draw()
			Draw.trans(last)
		} else {
			cache.setPosition(x + offsetX + fontOffX, y - offsetY + fontOffY)
			cache.draw()
		}
		if (shouldDistanceField) Draw.shader()

		data.setScale(lastSclX, lastSclY)
	}
}