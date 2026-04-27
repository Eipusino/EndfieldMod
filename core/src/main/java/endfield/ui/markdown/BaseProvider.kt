package endfield.ui.markdown

import arc.Core
import arc.graphics.Pixmap
import arc.graphics.Texture
import arc.graphics.g2d.TextureRegion
import arc.math.Mathf
import arc.scene.event.ClickListener
import arc.scene.style.BaseDrawable
import arc.scene.style.Drawable
import arc.scene.style.TextureRegionDrawable
import arc.scene.ui.layout.Scl
import arc.scene.ui.layout.Table
import arc.util.Align
import arc.util.Log
import arc.util.Scaling
import endfield.ui.markdown.elemdraw.DrawCodeBlock
import endfield.ui.markdown.elemdraw.DrawCurtain
import endfield.ui.markdown.elemdraw.DrawImg
import endfield.ui.markdown.elemdraw.DrawStr
import endfield.ui.markdown.elemdraw.DrawTable
import endfield.ui.markdown.elemdraw.DrawThematicBreak
import endfield.ui.markdown.elemdraw.DrawUrl
import endfield.ui.markdown.elemdraw.drawTextWrap
import endfield.ui.markdown.extensions.curtain.Curtain
import endfield.ui.markdown.extensions.curtain.CurtainExtension
import endfield.ui.markdown.extensions.curtain.CurtainProvider
import endfield.ui.markdown.extensions.imgattr.ImgAttrExtension
import endfield.ui.markdown.extensions.ins.InsExtension
import endfield.ui.markdown.extensions.ins.InsProvider
import endfield.ui.markdown.extensions.strikethrough.StrikethroughExtension
import endfield.ui.markdown.extensions.strikethrough.StrikethroughProvider
import endfield.ui.markdown.extensions.table.CellShadowBox
import endfield.ui.markdown.extensions.table.TableProvider
import endfield.ui.markdown.extensions.table.TablesExtension
import endfield.ui.markdown.url.AtlasHandler
import endfield.ui.markdown.url.DataHandler
import endfield.ui.markdown.url.HttpHandler
import endfield.ui.markdown.url.LocalFileHandler
import endfield.ui.markdown.url.ResourceHandler
import endfield.util.handler.FieldHandler
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.ext.image.attributes.ImageAttributes
import org.commonmark.ext.ins.Ins
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Document
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.Image
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.LinkReferenceDefinition
import org.commonmark.node.ListItem
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak
import kotlin.concurrent.thread

/*private var clickListenerField = Button::class.java.getDeclaredField("clickListener")
	.also { it.isAccessible = true }*/

open class BaseProvider : MarkdownProvider, CurtainProvider, InsProvider, StrikethroughProvider, TableProvider {
	override fun extensions() = listOf(
		ImgAttrExtension.create(),
		TablesExtension.create(),
		InsExtension.create(),
		StrikethroughExtension.create(),
		CurtainExtension.create()
	)

	override fun urlHandlers() = listOf(
		HttpHandler(),
		AtlasHandler(),
		LocalFileHandler(),
		ResourceHandler(),
		DataHandler(),
	)

	override fun handleLayoutException(exception: Throwable) {
		Log.err("Markdown layout error, detail info: ", exception)
	}

	override fun RendererContext.add(node: Document) {
		//always create a root scope, set the max width.
		withScope(
			boundX = mdWidth
		) {
			mdStyle.textFont.applyFont()
			renderChildren(node)
		}
	}

	override fun RendererContext.add(node: Heading) {
		val box = mdStyle.headBox
			.ifEmpty { null }
			?.let { list -> list[Mathf.clamp(node.level - 1, 0, list.size - 1)] }

		fun RendererContext.Scope.draw() {
			mdStyle.headFonts
				.ifEmpty { throw IllegalArgumentException("Markdown style must provide least one HeadFonts") }
				.let { list -> list[Mathf.clamp(node.level - 1, 0, list.size - 1)] }
				.applyFont()

			renderChildren(node)

			val firstText = node.findDescendants { it is Text } as? Text

			pushChapterEntry(
				firstText?.literal ?: "capter-${captureCount()}",
				offsetX,
				offsetY,
				node.level
			)
		}

		box?.also { b ->
			withScope(box = b) { draw() }
		} ?: run {
			withScope { draw() }
		}
		row(Scl.scl(mdStyle.paragraphPadding))
	}

	override fun RendererContext.add(node: Paragraph) {
		renderChildren(node)
		row(Scl.scl(mdStyle.paragraphPadding))
	}

	override fun RendererContext.add(node: BlockQuote) {
		withScope(
			box = mdStyle.quoteBox,
			fillX = true
		) {
			renderChildren(node)
		}

		row(Scl.scl(mdStyle.paragraphPadding))
	}

	override fun RendererContext.add(node: Link) {
		val text = (node.firstChild as? Text)?.literal ?: ""
		withScope(
			inlineBreak = true
		) {
			mdStyle.linkFont.applyFont()
			val clickListener = ClickListener()

			drawTextWrap(
				str = text,
			) { s ->
				val draw = DrawUrl.get(
					s.toString(), node.destination,
					font, fontOffsetX, fontOffsetY,
					fontIsItalic, fontColor, fontScale,
					mdStyle.linkOverColor
				)
				draw(draw)
				val elem = draw.button
				elem.removeListener(elem.clickListener)
				elem.addListener(clickListener)
				FieldHandler.setDefault(elem, "clickListener", clickListener)
			}
		}
	}

	override fun RendererContext.add(node: Text) {
		drawTextWrap(
			str = node.literal,
		)
	}

	override fun RendererContext.add(node: Code) {
		withScope(
			box = mdStyle.codeBox,
			inlineBreak = true,
		) {
			mdStyle.codeFont.applyFont()

			drawTextWrap(
				str = node.literal,
			)
		}
	}

	override fun RendererContext.add(node: BulletList) {
		withScope {
			var layer = 0
			eachAncestral { if (it.hasTag("list")) layer++ }

			tags("list")

			node.eachChildren { n ->
				val head = mdStyle.bulletListMarks.let { l -> l[Mathf.clamp(layer, 0, l.size - 1)] }

				withScope(
					box = mdStyle.listItemHeadBox
				) {
					draw(DrawImg.get(head))
				}

				render(n)
			}
		}

		row(Scl.scl(mdStyle.paragraphPadding))
	}

	override fun RendererContext.add(node: OrderedList) {
		withScope {
			var layer = 0
			eachAncestral { if (it.hasTag("list")) layer++ }

			tags("list")

			var n = node.markerStartNumber
			node.eachChildren { child ->
				withScope(
					box = mdStyle.listItemHeadBox
				) {
					mdStyle.listOrderFont.applyFont()
					val format = mdStyle.orderedListFormatters.let { list ->
						list[Mathf.clamp(layer, 0, list.size - 1)]
					}

					draw(
						DrawStr.get(
							"${format(n)}.",
							font,
							fontOffsetX,
							fontOffsetY,
							false,
							fontColor,
							fontScale
						)
					)
				}

				render(child)
				n++
			}
		}

		row(Scl.scl(mdStyle.paragraphPadding))
	}

	override fun RendererContext.add(node: ListItem) {
		withScope {
			renderChildren(node)
		}

		row(Scl.scl(mdStyle.paragraphPadding))
	}

	override fun RendererContext.add(node: IndentedCodeBlock) {
		withScope(
			box = mdStyle.codeBlockBox,
		) {
			mdStyle.codeFont.applyFont()

			draw(
				DrawCodeBlock.get(
					font,
					fontScale,
					node.literal.substring(0, node.literal.length - 1),
					"",
					mdStyle.sliderStyle,
				)
			)
		}
	}

	override fun RendererContext.add(node: FencedCodeBlock) {
		withScope(
			box = mdStyle.codeBlockBox,
		) {
			mdStyle.codeFont.applyFont()

			draw(
				DrawCodeBlock.get(
					font,
					fontScale,
					node.literal.substring(0, node.literal.length - 1),
					node.info,
					mdStyle.sliderStyle
				)
			)
		}
	}

	override fun RendererContext.add(node: ThematicBreak) {
		draw(
			DrawThematicBreak.get(
				mdStyle.lineColor,
				Scl.scl(mdStyle.lineStroke)
			)
		)
		row(Scl.scl(mdStyle.paragraphPadding))
	}

	override fun RendererContext.add(node: Image) {
		val attributes = node.findChild { it is ImageAttributes } as? ImageAttributes

		val url = node.destination
		val drawable = try {
			resolveResource(url) { input ->
				var resource: Drawable? = null
				val res = object : BaseDrawable() {
					override fun draw(x: Float, y: Float, width: Float, height: Float) {
						resource?.draw(x, y, width, height)
							?: mdStyle.loadingImg.draw(x, y, width, height)
					}
				}
				thread {
					try {
						val bytes = input.open().readBytes()

						Core.app.post {
							val pixmap = Pixmap(bytes)
							val texture = Texture(pixmap)
							resource = TextureRegionDrawable(TextureRegion(texture))
							mdInvalidate()
						}
					} catch (e: Exception) {
						Core.app.post { invalidResource(url) }
						Log.err(e)
					} finally {
						input.close()
					}
				}

				res
			}
		} catch (_: Exception) {
			mdStyle.loadingImg
		}

		val lastIsSoftBreak = node.previous is SoftLineBreak

		attributes?.let {
			val width = it.attributes["width"]?.toFloat() ?: 0f
			val height = it.attributes["height"]?.toFloat() ?: 0f
			val scaling = Scaling.entries.find { s -> s.name == it.attributes["scaling"] } ?: Scaling.stretch

			val size = scaling.apply(
				drawable.minWidth, drawable.minHeight,
				width, height
			)

			val scope = getScope()
			if (lastIsSoftBreak || scope.currOffsetX + size.x >= scope.boundX - scope.marginRight) {
				row(mdStyle.linesPadding)
			}

			draw(
				DrawImg.get(
					drawable,
					scaling = scaling,
					widthModifier = width,
					heightModifier = height,
				)
			)
		} ?: run {
			val scope = getScope()
			if (lastIsSoftBreak || scope.currOffsetX + drawable.minWidth >= scope.boundX - scope.marginRight) {
				row(mdStyle.linesPadding)
			}
			draw(DrawImg.get(drawable))
		}
	}

	override fun RendererContext.add(node: Emphasis) {
		withScope {
			mdStyle.emFont.applyFont()

			renderChildren(node)
		}
	}

	override fun RendererContext.add(node: StrongEmphasis) {
		withScope {
			mdStyle.strongFont.applyFont()

			renderChildren(node)
		}
	}

	override fun RendererContext.add(node: SoftLineBreak) {
		drawTextWrap(
			str = " ",
		)
	}

	override fun RendererContext.add(node: HardLineBreak) {
		row(Scl.scl(mdStyle.linesPadding))
	}

	override fun RendererContext.add(node: LinkReferenceDefinition) {
		putVar("link-def-${node.label}", node.destination)
	}

	override fun RendererContext.add(node: Curtain) {
		withScope(
			box = mdStyle.curtainBox,
			drawProvider = mdStyle.curtainBox.background?.let { { DrawCurtain.get(it) } },
			inlineBreak = true
		) {
			drawTextWrap(
				str = node.literal,
			)
		}
	}

	override fun RendererContext.add(node: Ins) {
		withScope(
			box = Markdown.Box(mdStyle.underLine),
			inlineBreak = true
		) {
			scopeDrawTiming(Markdown.DrawTiming.POST)

			renderChildren(node)
		}
	}

	override fun RendererContext.add(node: Strikethrough) {
		withScope(
			box = Markdown.Box(mdStyle.strikethrough),
			inlineBreak = true
		) {
			scopeDrawTiming(Markdown.DrawTiming.POST)

			renderChildren(node)
		}
	}

	override fun RendererContext.add(node: TableBlock) {
		val tableBuilder = TableBuilder(this)
		putVar("curr-table-builder", tableBuilder)
		renderChildren(node)
		invalidVar("curr-table-builder")

		draw(
			DrawTable.get(
				tableBuilder.result(),
				mdStyle.sliderStyle
			)
		)

		row(Scl.scl(mdStyle.paragraphPadding))
	}

	override fun RendererContext.add(node: TableHead) {
		val builder = getVar<TableBuilder>("curr-table-builder") {
			throw IllegalStateException("Incorrect table structure, no top level available.")
		}
		builder.makeRow()
		renderChildren(node)
	}

	override fun RendererContext.add(node: TableBody) {
		renderChildren(node)
	}

	override fun RendererContext.add(node: TableRow) {
		val builder = getVar<TableBuilder>("curr-table-builder") {
			throw IllegalStateException("Incorrect table structure, no top level available.")
		}
		builder.makeRow()
		renderChildren(node)
	}

	override fun RendererContext.add(node: TableCell) {
		val builder = getVar<TableBuilder>("curr-table-builder") {
			throw IllegalStateException("Incorrect table structure, no top level available.")
		}
		builder.pushCell(node)
	}

	override fun RendererContext.add(node: CellShadowBox) {
		withScope(
			box = node.cellBox,
			drawProvider = null,
			boundX = mdWidth,
			fillX = true
		) {
			renderChildren(node)
		}
	}

	private class TableBuilder(
		val context: RendererContext
	) {
		companion object {
			val transparent = BaseDrawable()
		}

		private val result = Table()
		private var currColumn = 0

		fun result() = result

		fun makeRow() {
			currColumn = 0
			result.row()
		}

		fun pushCell(node: TableCell) {
			context.apply {
				val back1 = mdStyle.tableBack1
				val back2 = mdStyle.tableBack2

				val back = if (currColumn % 2 == 0) back1 else back2

				result.table(back.background ?: transparent) { t ->
					t.align(
						when (node.alignment) {
							TableCell.Alignment.LEFT -> Align.left
							TableCell.Alignment.CENTER -> Align.center
							TableCell.Alignment.RIGHT -> Align.right
						}
					)
					t.add(
						createSubMarkdown(
							CellShadowBox(node, back)
						).also { it.wrapContent = false })
				}.fill()
					.marginTop(back.paddingTop)
					.marginLeft(back.paddingLeft)
					.marginRight(back.paddingRight)
					.marginBottom(back.paddingBottom)
			}
			currColumn++
		}
	}
}