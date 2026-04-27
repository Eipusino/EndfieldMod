package endfield.ui.markdown

import arc.func.Cons
import arc.graphics.Color
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Font
import arc.scene.Element
import arc.scene.event.Touchable
import arc.scene.style.BaseDrawable
import arc.scene.style.Drawable
import arc.scene.ui.ScrollPane
import arc.scene.ui.layout.WidgetGroup
import arc.struct.Seq
import arc.util.Log
import arc.util.pooling.Pool.Poolable
import arc.util.pooling.Pools
import mindustry.Vars
import mindustry.ui.Fonts
import org.commonmark.Extension
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.parser.Parser.ParserExtension

open class Markdown : WidgetGroup {
	var wrapContent: Boolean = true

	private val markdownDraws = Seq<MarkdownDraw>()
	private var drawList = listOf<MarkdownDraw>()

	private var node: Node
	private var style: MarkdownStyle?

	private var prefInvalid: Boolean = true
	private var buildActObjs: Boolean = false

	private var lastPrefHeight: Float = 0f
	private var prefWidth: Float = 0f
	private var prefHeight: Float = 0f

	internal val parser: Parser?
	internal val renderer: MDLayoutRenderer

	internal var provider: MarkdownProvider
	internal var rendererContext: RendererContext

	@JvmOverloads
	constructor(content: String, style: MarkdownStyle, provider: MarkdownProvider = BaseProvider()) {
		val extensions = provider.extensions()
		checkExtensions(extensions)

		this.provider = provider
		this.style = style

		parser = Parser.builder()
			.extensions(extensions)
			.build()
		renderer = MDLayoutRenderer.builder()
			.extensions(extensions)
			.build()

		rendererContext = renderer.createContext(this)

		node = parser.parse(content)
		touchable = Touchable.childrenOnly
	}

	/**internal usage
	 *
	 * @hidden
	 */
	internal constructor(parent: Markdown, node: Node) {
		this.provider = parent.provider
		parser = null
		renderer = MDLayoutRenderer.builder().extensions(provider.extensions()).build()
		rendererContext = renderer.createContext(this)

		this.node = node
		touchable = Touchable.childrenOnly

		this.style = parent.getStyle()
	}

	private fun checkExtensions(extensions: List<Extension>) {
		for (extension in extensions) {
			require(extension is MDLayoutRenderer.DrawRendererExtension || extension is ParserExtension) {
				"extension must be a DrawRendererExtension or a ParserExtension"
			}
		}
	}

	fun getContext() = rendererContext

	fun setProvider(provider: MarkdownProvider) {
		this.provider = provider
		this.rendererContext = renderer.createContext(this)
		invalidate()
	}

	fun getProvider(): MarkdownProvider = provider

	fun setDocument(string: String) {
		node = parser!!.parse(string)
		invalidate()
	}

	fun setStyle(style: MarkdownStyle) {
		this.style = style
		invalidate()
	}

	fun getStyle(): MarkdownStyle {
		return style!!
	}

	fun directlyOpenUrl() {
		urlClicked { url ->
			try {
				rendererContext.openUrl(url)
			} catch (e: Exception) {
				Log.err(e)
				Vars.ui.showException(e)
			}
		}
	}

	fun urlClicked(callback: Cons<String>) {
		addListener { e ->
			if (e is UrlClickedEvent) {
				callback.get(e.clickedUrl)
				return@addListener true
			}

			false
		}
	}

	override fun layout() {
		if (wrapContent) {
			val prefHeight = getPrefHeight()
			if (prefHeight != lastPrefHeight) {
				lastPrefHeight = prefHeight
				invalidateHierarchy()
			}
		}

		for (obj in markdownDraws) {
			obj.free()
		}
		markdownDraws.clear()

		try {
			renderer.renderLayout(node)
		} catch (e: Throwable) {
			provider.handleLayoutException(e)
		}

		markdownDraws.addAll(rendererContext.renderResult())
		drawList = markdownDraws
			.filter { it.drawTiming != DrawTiming.NEVER }
			.sortedBy { it.drawTiming }

		buildActObjs = true
		clearChildren()
		markdownDraws.forEach { obj ->
			if (obj.drawTiming != DrawTiming.NEVER && obj is ActivityDrawer) {
				val element = obj.activeElement
				addChild(element)
				element.setBounds(
					obj.offsetX,
					height - obj.offsetY - obj.height,
					obj.width,
					obj.height
				)
				element.validate()
			}
		}
		buildActObjs = false
	}

	override fun childrenChanged() {
		if (!buildActObjs) invalidateHierarchy()
	}

	override fun invalidate() {
		super.invalidate()
		prefInvalid = true
	}

	open fun calculatePrefSize() {
		try {
			renderer.renderLayout(node)
		} catch (e: Throwable) {
			provider.handleLayoutException(e)
		}

		prefInvalid = false

		prefWidth = rendererContext.prefWidth
		prefHeight = rendererContext.prefHeight
	}

	override fun getPrefWidth(): Float {
		if (prefInvalid) calculatePrefSize()
		return if (wrapContent) 0f else prefWidth
	}

	override fun getPrefHeight(): Float {
		if (prefInvalid) calculatePrefSize()
		return prefHeight
	}

	override fun drawChildren() {
		for (obj in drawList) {
			if (obj is ActivityDrawer && cullingArea != null && !cullingArea.overlaps(
					obj.offsetX,
					height + obj.offsetY,
					obj.width,
					obj.height
				)
			) continue

			Draw.reset()
			Draw.alpha(parentAlpha)
			obj.draw(x, y + height)
		}
		super.drawChildren()
	}

	data class FontEntry(
		val fontModifier: Font? = null,
		val isItalic: Boolean? = null,
		val colorModifier: Color? = null,
		val scaleModifier: Float? = null,
	)

	data class Box(
		val background: Drawable? = null,
		val paddingLeft: Float = 0f,
		val paddingRight: Float = 0f,
		val paddingTop: Float = 0f,
		val paddingBottom: Float = 0f,
		val marginLeft: Float = 0f,
		val marginRight: Float = 0f,
		val marginTop: Float = 0f,
		val marginBottom: Float = 0f,
	) {
		constructor(
			background: Drawable? = null,
			paddingX: Float = 0f,
			paddingY: Float = 0f,
			marginX: Float = 0f,
			marginY: Float = 0f,
		) : this(
			background = background,
			paddingLeft = paddingX,
			paddingRight = paddingX,
			paddingTop = paddingY,
			paddingBottom = paddingY,
			marginLeft = marginX,
			marginRight = marginX,
			marginTop = marginY,
			marginBottom = marginY,
		)
	}

	class MarkdownStyle {
		private companion object {
			val defaultFont = FontEntry(
				Fonts.def,
				false,
				Color.white,
				1f
			)
			val defaultBox = Box()
			val defaultDraw = BaseDrawable()
		}

		var loadingImg: Drawable = defaultDraw

		//globals
		var linesPadding: Float = 0f
		var paragraphPadding: Float = 0f

		var lineColor: Color = Color.white
		var lineStroke: Float = 0f

		var sliderStyle: ScrollPane.ScrollPaneStyle = ScrollPane.ScrollPaneStyle()

		//normal
		var textFont: FontEntry = defaultFont
		var subFont: FontEntry = defaultFont
		var emFont: FontEntry = defaultFont
		var strongFont: FontEntry = defaultFont
		var headFonts: Array<FontEntry> = arrayOf()
		var headBox: Array<Box> = arrayOf()
		var quoteBox: Box = defaultBox
		var curtainBox: Box = defaultBox
		var underLine: Drawable = defaultDraw
		var strikethrough: Drawable = defaultDraw

		//code
		var codeFont: FontEntry = defaultFont
		var codeBox: Box = defaultBox
		var codeBlockBox: Box = defaultBox

		//link
		var linkFont: FontEntry = defaultFont
		var linkOverColor: Color = Color.white

		//list
		var listOrderFont: FontEntry = defaultFont
		var listItemBox: Box = defaultBox
		var listItemHeadBox: Box = defaultBox
		var bulletListMarks: Array<Drawable> = arrayOf()
		var orderedListFormatters: Array<(Int) -> String> = arrayOf()

		//table
		var tableBack1: Box = defaultBox
		var tableBack2: Box = defaultBox
	}

	abstract class MarkdownDraw : Poolable {
		var offsetX: Float = 0f
		var offsetY: Float = 0f

		var width: Float = 0f
		var height: Float = 0f

		var drawTiming: DrawTiming = DrawTiming.MAIN

		abstract fun prefWidth(): Float
		abstract fun prefHeight(): Float

		abstract fun setup(scope: RendererContext.Scope)
		abstract fun draw(x: Float, y: Float)

		override fun reset() {
			offsetY = 0f
			offsetX = 0f
		}

		fun free() {
			Pools.free(this)
		}

		companion object {
			@JvmStatic
			protected val tmp1: Color = Color()
			@JvmStatic
			protected val tmp2: Color = Color()
		}
	}

	inner class ChapterEntry(
		val title: String,
		val offsetX: Float,
		val offsetY: Float,
		val level: Int
	) {
		val drawX: Float get() = offsetX
		val drawY: Float get() = height - offsetY
	}

	interface ActivityDrawer {
		val activeElement: Element
	}

	enum class DrawTiming {
		PREVIOUSLY,
		MAIN,
		POST,
		NEVER
	}
}
