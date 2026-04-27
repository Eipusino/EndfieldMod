package endfield.ui.markdown

import arc.func.Boolf
import arc.func.Cons
import arc.func.Func
import arc.func.Prov
import arc.graphics.Color
import arc.graphics.g2d.Font
import arc.scene.ui.layout.Scl
import endfield.ui.markdown.elemdraw.DrawImg
import endfield.util.CollectionList
import endfield.util.CollectionObjectMap
import mindustry.ui.Fonts
import org.commonmark.node.Node
import kotlin.math.max

abstract class RendererContext protected constructor(
	private val element: Markdown
) {
	companion object {
		private val SCHEME_TYPE_PATTERN = "\\w+:".toRegex()
	}

	private val markdownDraws = CollectionList<Markdown.MarkdownDraw>(Markdown.MarkdownDraw::class.java)
	private val attachedVars = CollectionObjectMap<String, Any>(String::class.java, Any::class.java)
	private val chapterEntries = CollectionList<Markdown.ChapterEntry>(Markdown.ChapterEntry::class.java)

	private var currentScope: Scope? = null
	private var rootScope: Scope? = null

	private val resourceCache = mutableMapOf<String, Any>()
	private val urlHandlers: Map<String, UrlHandler> = element.provider.urlHandlers()
		.flatMap { h -> h.matchedSchemes().map { it to h } }
		.toMap()

	val prefWidth: Float get() = rootScope?.let { it.width + it.paddingLeft + it.paddingRight } ?: 0f
	val prefHeight: Float get() = rootScope?.let { it.height + it.paddingTop + it.paddingBottom } ?: 0f

	val mdStyle get() = element.getStyle()
	val mdWidth get() = element.width
	val mdHeight get() = element.height
	val mdShouldWrap get() = element.wrapContent

	fun mdInvalidate() {
		element.invalidate()
	}

	fun createSubMarkdown(node: Node) = Markdown(element, node)

	abstract fun render(node: Node)

	class Scope internal constructor(
		val drawProvider: Prov<Markdown.MarkdownDraw>? = null,
		val parent: Scope? = null,
		val paddingLeft: Float,
		val paddingRight: Float,
		val paddingTop: Float,
		val paddingBottom: Float,
		val marginLeft: Float,
		val marginRight: Float,
		val marginTop: Float,
		val marginBottom: Float,
		val boundX: Float,
		val fillX: Boolean,
		val inlineBreak: Boolean
	) {
		internal var tags = mutableSetOf<String>()

		val scopeDraw = drawProvider?.get()

		var offsetX: Float = (parent?.currOffsetX ?: 0f) + paddingLeft
		var offsetY: Float = (parent?.currOffsetY ?: 0f) + paddingTop

		var width: Float = if (fillX) boundX - offsetX else 0f
		var height: Float = 0f

		var currOffsetX: Float = offsetX + marginLeft
		var currOffsetY: Float = offsetY + marginTop

		var rowHeight: Float = 0f

		var font: Font = parent?.font ?: Fonts.def
		var fontOffsetX: Float = parent?.fontOffsetX ?: 0f
		var fontOffsetY: Float = parent?.fontOffsetY ?: 0f
		var fontIsItalic: Boolean = parent?.fontIsItalic ?: false
		var fontColor: Color = parent?.fontColor ?: Color.white
		var fontScale: Float = parent?.fontScale ?: 1f

		fun tags(vararg tags: String) {
			this.tags.addAll(tags)
		}

		fun hasTag(tag: String): Boolean = this.tags.contains(tag)

		fun getTags(): List<String> = this.tags.toList()

		fun eachAncestral(callback: Cons<Scope>) {
			callback.get(this)
			parent?.eachAncestral(callback)
		}

		fun findAncestral(callback: Boolf<Scope>): Scope? {
			if (callback.get(this)) return this
			return parent?.findAncestral(callback)
		}

		fun scopeDrawTiming(timing: Markdown.DrawTiming) {
			scopeDraw?.drawTiming = timing
		}

		fun Markdown.FontEntry.applyFont() {
			this@Scope.font = fontModifier ?: parent?.font ?: Fonts.def
			this@Scope.fontOffsetX = fontOffsetX ?: parent?.fontOffsetX ?: 0f
			this@Scope.fontOffsetY = fontOffsetY ?: parent?.fontOffsetY ?: 0f
			this@Scope.fontIsItalic = isItalic ?: parent?.fontIsItalic ?: false
			this@Scope.fontColor = colorModifier ?: parent?.fontColor ?: Color.white
			this@Scope.fontScale = scaleModifier ?: parent?.fontScale ?: 1f
		}
	}

	fun init() {
		rootScope = null
		currentScope = null
		markdownDraws.clear()
		attachedVars.clear()
		chapterEntries.clear()
	}

	private fun resolveUrlHandler(url: String): UrlHandler {
		val schemeMatch = SCHEME_TYPE_PATTERN.matchAt(url, 0)
		val scheme = schemeMatch?.value?.trimEnd(':') ?: "https"

		return urlHandlers[scheme] ?: throw IllegalArgumentException("Unknown scheme type: $scheme")
	}

	fun openUrl(url: String) {
		resolveUrlHandler(url).openUrl(url)
	}

	private fun getUrlResource(url: String): UrlHandler.ResourceHandle {
		return resolveUrlHandler(url).getResource(url)
	}

	@Suppress("UNCHECKED_CAST")
	fun <T : Any> resolveResource(
		url: String,
		resourceResolver: Func<UrlHandler.ResourceHandle, T>,
	): T {
		return resourceCache.computeIfAbsent(url) {
			val handle = getUrlResource(url)
			resourceResolver.get(handle)
		} as T
	}

	fun invalidResource(url: String) {
		resourceCache.remove(url)
	}

	fun putVar(name: String, value: Any) {
		attachedVars.put(name, value)
	}

	fun invalidVar(name: String) {
		attachedVars.remove(name)
	}

	@Suppress("UNCHECKED_CAST")
	fun <T> getVar(name: String): T? {
		return attachedVars.get(name) as? T
	}

	@Suppress("UNCHECKED_CAST")
	fun <T> getVar(name: String, def: T): T {
		val res = attachedVars.get(name)
		if (res == null) {
			attachedVars.put(name, def)
			return def
		}
		return res as T
	}

	@Suppress("UNCHECKED_CAST")
	fun <T> getVar(name: String, def: Prov<T>): T {
		return attachedVars.getOrPut(name) { def.get() } as T
	}

	fun getScope(): Scope = currentScope ?: insertScope()

	fun withScope(
		box: Markdown.Box,
		drawProvider: Prov<Markdown.MarkdownDraw>? = box.background?.let {
			Prov {
				DrawImg.get(box.background).also {
					it.drawTiming = Markdown.DrawTiming.PREVIOUSLY
				}
			}
		},
		boundX: Float = (currentScope?.boundX ?: 0f) - box.paddingRight,
		fillX: Boolean = false,
		inlineBreak: Boolean = false,
		callback: Scope.() -> Unit,
	) = withScope(
		drawProvider = drawProvider,
		paddingLeft = Scl.scl(box.paddingLeft),
		paddingRight = Scl.scl(box.paddingRight),
		paddingTop = Scl.scl(box.paddingTop),
		paddingBottom = Scl.scl(box.paddingBottom),
		marginLeft = Scl.scl(box.marginLeft),
		marginRight = Scl.scl(box.marginRight),
		marginTop = Scl.scl(box.marginTop),
		marginBottom = Scl.scl(box.marginBottom),
		boundX = boundX,
		fillX = fillX,
		inlineBreak = inlineBreak,
		callback = callback,
	)

	fun insertScope(
		box: Markdown.Box,
		drawProvider: Prov<Markdown.MarkdownDraw>? = box.background?.let {
			Prov {
				DrawImg.get(box.background).also {
					it.drawTiming = Markdown.DrawTiming.PREVIOUSLY
				}
			}
		},
		boundX: Float = (currentScope?.boundX ?: 0f) - box.paddingRight,
		fillX: Boolean = false,
		inlineBreak: Boolean = false,
	) = insertScope(
		drawProvider = drawProvider,
		paddingLeft = Scl.scl(box.paddingLeft),
		paddingRight = Scl.scl(box.paddingRight),
		paddingTop = Scl.scl(box.paddingTop),
		paddingBottom = Scl.scl(box.paddingBottom),
		marginLeft = Scl.scl(box.marginLeft),
		marginRight = Scl.scl(box.marginRight),
		marginTop = Scl.scl(box.marginTop),
		marginBottom = Scl.scl(box.marginBottom),
		boundX = boundX,
		fillX = fillX,
		inlineBreak = inlineBreak,
	)

	fun withScope(
		drawProvider: Prov<Markdown.MarkdownDraw>? = null,
		paddingLeft: Float = 0f,
		paddingRight: Float = 0f,
		paddingTop: Float = 0f,
		paddingBottom: Float = 0f,
		marginLeft: Float = 0f,
		marginRight: Float = 0f,
		marginTop: Float = 0f,
		marginBottom: Float = 0f,
		boundX: Float = (currentScope?.boundX ?: 0f) - paddingRight,
		fillX: Boolean = false,
		inlineBreak: Boolean = false,
		callback: Scope.() -> Unit
	): Scope {
		insertScope(
			drawProvider,
			paddingLeft, paddingRight, paddingTop, paddingBottom,
			marginLeft, marginRight, marginTop, marginBottom,
			boundX,
			fillX,
			inlineBreak,
		).callback()
		return popScope()
	}

	fun insertScope(
		drawProvider: Prov<Markdown.MarkdownDraw>? = null,
		paddingLeft: Float = 0f,
		paddingRight: Float = 0f,
		paddingTop: Float = 0f,
		paddingBottom: Float = 0f,
		marginLeft: Float = 0f,
		marginRight: Float = 0f,
		marginTop: Float = 0f,
		marginBottom: Float = 0f,
		boundX: Float = (currentScope?.boundX ?: 0f) - paddingRight,
		fillX: Boolean = false,
		inlineBreak: Boolean = false,
	): Scope {
		val scope = Scope(
			drawProvider,
			currentScope,
			paddingLeft, paddingRight, paddingTop, paddingBottom,
			marginLeft, marginRight, marginTop, marginBottom,
			boundX,
			fillX,
			inlineBreak,
		)

		if (currentScope == null) {
			rootScope = scope
		}

		scope.scopeDraw?.also { d ->
			d.setup(scope)
			markdownDraws.add(d)
		}

		currentScope = scope

		return scope
	}

	fun popScope(): Scope {
		val curr = currentScope ?: throw IllegalStateException("Current has no scope be set.")
		val last = curr.parent

		if (last != null) {
			last.width =
				max(last.width, curr.offsetX + curr.width - last.offsetX + curr.paddingRight + last.marginRight)
			last.height =
				max(last.height, curr.offsetY + curr.height - last.offsetY + curr.paddingBottom + last.marginBottom)
			last.rowHeight = max(last.rowHeight, curr.height + curr.paddingTop + curr.paddingBottom)
			last.currOffsetX += curr.width + curr.paddingLeft + curr.paddingRight
		}

		currentScope = last

		curr.scopeDraw?.also { draw ->
			draw.offsetX = curr.offsetX
			draw.offsetY = curr.offsetY
			draw.width = curr.width
			draw.height = curr.height
		}

		return curr
	}

	fun findChapter(title: String, level: Int = -1): Markdown.ChapterEntry? {
		return chapterEntries.find { (level == -1 || level == it.level) && it.title == title }
	}

	fun findChapter(pattern: Regex, level: Int = -1): Markdown.ChapterEntry? {
		return chapterEntries.find { (level == -1 || level == it.level) && it.title.matches(pattern) }
	}

	fun filterChapter(pattern: Regex, level: Int = -1): List<Markdown.ChapterEntry> {
		return chapterEntries.filter { (level == -1 || level == it.level) && it.title.matches(pattern) }
	}

	fun pushChapterEntry(title: String, offsetX: Float, offsetY: Float, level: Int) {
		chapterEntries.add(
			element.ChapterEntry(
				title,
				offsetX,
				offsetY,
				level
			)
		)
	}

	fun captureCount() = chapterEntries.size

	fun draw(markdownDraw: Markdown.MarkdownDraw) {
		val curr = getScope()

		markdownDraw.offsetX = curr.currOffsetX
		markdownDraw.offsetY = curr.currOffsetY

		markdownDraw.setup(curr)
		markdownDraw.width = markdownDraw.prefWidth()
		markdownDraw.height = markdownDraw.prefHeight()

		val boundX = curr.boundX - curr.marginRight

		if (curr.boundX > 0 && markdownDraw.offsetX + markdownDraw.width > boundX) {
			val srkW = boundX - markdownDraw.offsetX
			val ratio = srkW / markdownDraw.width

			markdownDraw.width = srkW
			markdownDraw.height *= ratio
		}

		markdownDraws.add(markdownDraw)

		curr.currOffsetX += markdownDraw.width
		curr.width = max(curr.width, markdownDraw.offsetX + markdownDraw.width - curr.offsetX + curr.marginRight)
		curr.height = max(curr.height, markdownDraw.offsetY + markdownDraw.height - curr.offsetY + curr.marginBottom)
		curr.rowHeight = max(curr.rowHeight, markdownDraw.height)
	}

	fun pad(padding: Float) {
		getScope().currOffsetX += padding
	}

	fun row(rowPadding: Float): Scope {
		val last = getScope()
		if (last.inlineBreak) {
			if (last.drawProvider != null && last.width <= last.marginLeft + last.marginRight) {
				markdownDraws.remove(last.scopeDraw)
			}
			popScope()
		}

		val curr = getScope()
		curr.currOffsetX = curr.offsetX + curr.marginLeft
		curr.currOffsetY += curr.rowHeight + rowPadding
		curr.rowHeight = 0f

		if (last.inlineBreak) {
			return insertScope(
				last.drawProvider,
				last.paddingLeft, last.paddingRight, last.paddingTop, last.paddingBottom,
				last.marginLeft, last.marginRight, last.marginTop, last.marginBottom,
				last.boundX,
			).apply {
				font = last.font
				fontColor = last.fontColor
				fontScale = last.fontScale
				tags = last.tags
			}
		}

		return curr
	}

	fun renderResult() = markdownDraws.toList()
}