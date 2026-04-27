package endfield.ui.markdown.elemdraw

import arc.graphics.Color
import arc.graphics.g2d.DistanceFieldFont
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Font
import arc.math.Affine2
import arc.math.Mat
import arc.scene.Element
import arc.scene.ui.TextButton
import arc.util.pooling.Pools
import endfield.graphics.Shaders2
import endfield.ui.markdown.Markdown
import endfield.ui.markdown.RendererContext
import endfield.ui.markdown.UrlClickedEvent
import mindustry.ui.Fonts
import mindustry.ui.Styles

open class DrawUrl : Markdown.MarkdownDraw(), Markdown.ActivityDrawer {
	companion object {
		fun get(
			str: String,
			url: String,
			font: Font,
			fontOffsetX: Float,
			fontOffsetY: Float,
			italic: Boolean = false,
			color: Color,
			scl: Float,
			overColor: Color = color,
		): DrawUrl = Pools.obtain(DrawUrl::class.java) { DrawUrl() }.apply {
			this.text = str
			this.url = url
			this.font = font
			this.fontOffX = fontOffsetX
			this.fontOffY = fontOffsetY
			this.italic = italic
			this.color = color
			this.scl = scl
			this.overColor = overColor
		}
	}

	var text: String = ""
	var url: String = ""
	var font: Font = Fonts.def
	var fontOffX: Float = 0f
	var fontOffY: Float = 0f
	var italic: Boolean = false
	var color: Color = Color.white
	var scl: Float = 0f
	var overColor: Color = color

	internal lateinit var button: TextButton

	override val activeElement: Element get() = button

	override fun reset() {
		super.reset()
		text = ""
		url = ""
		font = Fonts.def
		fontOffX = 0f
		fontOffY = 0f
		color = Color.white
		scl = 0f
		overColor = Color.white
	}

	override fun prefWidth(): Float = button.width + fontOffX * scl
	override fun prefHeight(): Float = button.height + fontOffY * scl

	override fun setup(scope: RendererContext.Scope) {
		button = object : TextButton(text, makeStyle()) {
			private val affineTrans = Mat()
			private val trans = Mat()
			private val affine2 = Affine2()

			override fun applyTransform(transform: Mat) {
				if (italic) {
					super.applyTransform(
						trans.set(transform)
							.mul(affineTrans.set(affine2.idt().shear(0.25f, 0f)))
					)
				} else {
					super.applyTransform(transform)
				}
			}

			override fun drawChildren() {
				val shouldDistanceField = font is DistanceFieldFont

				if (shouldDistanceField) {
					Draw.shader(Shaders2.distance)
					Shaders2.distance.bind()
					Shaders2.distance.setUniformf("u_smoothing", 0.5f * font.scaleX)
				}

				super.drawChildren()

				if (shouldDistanceField) Draw.shader()
			}
		}
		button.isTransform = italic
		button.label.setWrap(false)
		button.label.setFontScale(scl)
		button.clicked { button.fire(UrlClickedEvent(url)) }
		button.pack()
	}

	private fun makeStyle() = TextButton.TextButtonStyle().apply {
		font = this@DrawUrl.font
		fontColor = color
		overFontColor = overColor
		up = Styles.none
	}

	override fun draw(x: Float, y: Float) {}
}