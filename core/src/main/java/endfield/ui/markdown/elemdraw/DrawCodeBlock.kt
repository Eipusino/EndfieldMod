package endfield.ui.markdown.elemdraw

import arc.Core
import arc.graphics.g2d.Font
import arc.scene.Element
import arc.scene.actions.Actions
import arc.scene.ui.Label
import arc.scene.ui.ScrollPane
import arc.scene.ui.layout.Stack
import arc.scene.ui.layout.Table
import arc.util.pooling.Pools
import endfield.ui.markdown.Markdown
import endfield.ui.markdown.RendererContext
import mindustry.Vars
import mindustry.gen.Icon
import mindustry.ui.Fonts
import mindustry.ui.Styles

open class DrawCodeBlock : Markdown.MarkdownDraw(), Markdown.ActivityDrawer {
	companion object {
		fun get(
			font: Font,
			fontScl: Float,
			code: String,
			tag: String,
			sliderStyle: ScrollPane.ScrollPaneStyle,
		): DrawCodeBlock = Pools.obtain(DrawCodeBlock::class.java) { DrawCodeBlock() }.apply {
			this.font = font
			this.fontScl = fontScl
			this.code = code
			this.tag = tag
			this.sliderStyle = sliderStyle
		}
	}

	var font: Font = Fonts.def
	var fontScl: Float = 0f
	var code: String = ""
	var tag: String = ""
	var sliderStyle: ScrollPane.ScrollPaneStyle = Styles.noBarPane

	private var realWidth: Float = 0f
	private var realHeight: Float = 0f

	private lateinit var label: Label
	private lateinit var pane: ScrollPane
	private lateinit var resElem: Stack

	override val activeElement: Element get() = resElem

	override fun reset() {
		super.reset()
		font = Fonts.def
		fontScl = 0f
		code = ""
		tag = ""
		sliderStyle = Styles.noBarPane
	}

	override fun prefWidth(): Float = realWidth
	override fun prefHeight(): Float = realHeight

	override fun setup(scope: RendererContext.Scope) {
		realWidth = scope.boundX - scope.marginRight - offsetX
		label = Label(code, Label.LabelStyle().also { it.font = font })
		label.setFontScale(fontScl)
		label.validate()
		realHeight = label.height

		pane = ScrollPane(label, sliderStyle).apply {
			isScrollingDisabledY = true
		}

		resElem = Stack(
			pane,
			Table { over ->
				over.top().right().button(
					Core.bundle["editor.copy"],
					Icon.copySmall,
					Styles.nonet,
				) {
					Core.app.clipboardText = code
					Vars.ui.showInfoFade(Core.bundle["copied"])
				}.get().apply {
					label.setWrap(false)
					color.a = 0.4f
					hovered { actions(Actions.alpha(1f, 0.3f)) }
					exited { actions(Actions.alpha(0.4f, 0.3f)) }
				}
			}
		)
		resElem.validate()
	}

	override fun draw(x: Float, y: Float) {}
}