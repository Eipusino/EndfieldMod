package endfield.ui.markdown.elemdraw

import arc.scene.Element
import arc.scene.event.ClickListener
import arc.scene.event.Touchable
import arc.scene.style.Drawable
import arc.scene.ui.Image
import arc.util.pooling.Pools
import endfield.ui.markdown.Markdown
import endfield.ui.markdown.RendererContext
import mindustry.gen.Tex

open class DrawCurtain : Markdown.MarkdownDraw(), Markdown.ActivityDrawer {
	companion object {
		fun get(
			curtainDraw: Drawable,
		): DrawCurtain = Pools.obtain(DrawCurtain::class.java) { DrawCurtain() }.apply {
			this.curtainDraw = curtainDraw
		}
	}

	var curtainDraw: Drawable = Tex.nomap

	private lateinit var curtain: CurtainElem

	override val activeElement: Element get() = curtain

	override fun reset() {
		super.reset()
		curtainDraw = Tex.nomap
	}

	override fun prefWidth(): Float = curtain.prefWidth
	override fun prefHeight(): Float = curtain.prefHeight

	override fun setup(scope: RendererContext.Scope) {
		curtain = CurtainElem(curtainDraw).apply {
			touchable = Touchable.enabled
		}
	}

	override fun draw(x: Float, y: Float) {
		curtainDraw.draw(x + offsetX, y - offsetY - height, width, height)
	}

	class CurtainElem(drawable: Drawable) : Image(drawable) {
		private var clickListener = ClickListener()

		init {
			addListener(clickListener)
			update { color.a = if (clickListener.isOver || clickListener.isPressed) 0f else 1f }
		}

		fun setClickListener(listener: ClickListener) {
			removeListener(clickListener)
			clickListener = listener
			addListener(listener)
		}
	}
}