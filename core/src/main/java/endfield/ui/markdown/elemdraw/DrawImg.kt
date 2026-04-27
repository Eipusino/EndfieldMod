package endfield.ui.markdown.elemdraw

import arc.scene.style.Drawable
import arc.scene.ui.layout.Scl
import arc.util.Scaling
import arc.util.pooling.Pools
import endfield.ui.markdown.Markdown
import endfield.ui.markdown.RendererContext
import mindustry.gen.Tex

open class DrawImg internal constructor() : Markdown.MarkdownDraw() {
	companion object {
		fun get(
			drawable: Drawable,
			scaling: Scaling = Scaling.none,
			widthModifier: Float = 0f,
			heightModifier: Float = 0f,
		): DrawImg = Pools.obtain(DrawImg::class.java) { DrawImg() }.apply {
			this.drawable = drawable
			this.scaling = scaling
			this.widthModifier = widthModifier
			this.heightModifier = heightModifier
		}
	}

	var drawable: Drawable = Tex.nomap
	var scaling: Scaling = Scaling.none
	var widthModifier: Float = 0f
	var heightModifier: Float = 0f

	private var realWidth: Float = 0f
	private var realHeight: Float = 0f

	override fun reset() {
		super.reset()
		drawable = Tex.nomap
		scaling = Scaling.none
		widthModifier = 0f
		heightModifier = 0f
	}

	override fun prefWidth() = Scl.scl(realWidth)
	override fun prefHeight() = Scl.scl(realHeight)

	override fun setup(scope: RendererContext.Scope) {
		val drawableWidth = drawable.minWidth + drawable.leftWidth + drawable.rightWidth
		val drawableHeight = drawable.minHeight + drawable.topHeight + drawable.bottomHeight
		if (widthModifier > 0 || heightModifier > 0) {
			try {
				val res = scaling.apply(
					drawableWidth,
					drawableHeight,
					widthModifier,
					heightModifier
				)

				realWidth = res.x
				realHeight = res.y
			} catch (e: Exception) {
				realWidth = drawableWidth
				realHeight = drawableHeight
			}
		} else {
			realWidth = drawableWidth
			realHeight = drawableHeight
		}
	}

	override fun draw(x: Float, y: Float) {
		drawable.draw(
			x + offsetX,
			y - offsetY - height,
			width,
			height
		)
	}
}