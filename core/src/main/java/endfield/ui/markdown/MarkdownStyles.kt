package endfield.ui.markdown

import arc.Core
import arc.freetype.FreeTypeFontGenerator
import arc.graphics.Color
import arc.graphics.Texture
import arc.graphics.g2d.DistanceFieldFont
import arc.graphics.g2d.Draw
import arc.graphics.g2d.Fill
import arc.graphics.g2d.Lines
import arc.scene.style.BaseDrawable
import arc.scene.style.TextureRegionDrawable
import arc.scene.ui.layout.Scl
import arc.util.Log
import arc.util.Tmp
import endfield.Vars2
import mindustry.gen.Tex
import mindustry.graphics.Pal
import mindustry.ui.Fonts
import mindustry.ui.Styles

object MarkdownStyles {
	private val strong = FreeTypeFontGenerator(Core.files.internal("fonts/font.woff"))
		.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
			size = Scl.scl(18f).toInt()
			borderWidth = Scl.scl(0.5f)
			incremental = true
			borderColor = color
		})
	private val defDistanced = UnkFontGenerator(Core.files.internal("fonts/font.woff"))
		.generateFont(UnkFontGenerator.UnkFontParameter().apply {
			size = Scl.scl(36f).toInt()
			incremental = true

			genMipMaps = true
			minFilter = Texture.TextureFilter.linear
			magFilter = Texture.TextureFilter.linear

			distanceFieldDownscale = 1
			distanceFieldSpread = 6
		}).also { f ->
			f.data.setScale(0.5f)
			(f as DistanceFieldFont).distanceFieldSmoothing = 1f
		}
	private val mono = try {
		Vars2.internalTree.child("fonts/jetbrainsmonomedium.ttf").let { fi ->
			val gen = FreeTypeFontGenerator(fi)
			gen.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
				size = Scl.scl(19f).toInt()
				incremental = true
			})
		}
	} catch (e: Exception) {
		Log.err(e)
		Fonts.def
	}

	val defaultMD: Markdown.MarkdownStyle = makeDefault()

	fun makeDefault(): Markdown.MarkdownStyle = Markdown.MarkdownStyle().apply {
		loadingImg = Tex.nomap

		linesPadding = 16f
		paragraphPadding = 32f

		lineColor = Color.gray
		lineStroke = 2f

		textFont = Markdown.FontEntry(
			fontModifier = Fonts.def,
			colorModifier = Color.white,
			scaleModifier = 1f
		)
		subFont = Markdown.FontEntry(
			colorModifier = Color.lightGray,
		)
		strongFont = Markdown.FontEntry(
			fontModifier = strong,
		)
		emFont = Markdown.FontEntry(
			isItalic = true,
		)
		headFonts = Array(6) { i ->
			Markdown.FontEntry(
				fontModifier = defDistanced,
				colorModifier = if (i == 5) Color.gray else Color.white,
				scaleModifier = if (i < 5) 5f / (i + 1) else 1f,
			)
		}
		quoteBox = Markdown.Box(
			Tex.paneLeft,
			marginX = 16f,
			marginY = 16f,
		)
		curtainBox = Markdown.Box(
			(Tex.whiteui as TextureRegionDrawable).tint(Pal.darkestestGray),
			paddingX = 0f,
			paddingY = -6f,
			marginX = 6f,
			marginY = 6f,
		)
		underLine = object : BaseDrawable() {
			override fun draw(x: Float, y: Float, width: Float, height: Float) {
				val stroke = Scl.scl(2f)
				Lines.stroke(stroke)
				Lines.line(x, y - stroke, x + width, y - stroke)
			}
		}
		strikethrough = object : BaseDrawable() {
			override fun draw(x: Float, y: Float, width: Float, height: Float) {
				Lines.stroke(Scl.scl(2f))
				Lines.line(x, y + height / 2f, x + width, y + height / 2f)
			}
		}

		codeFont = Markdown.FontEntry(
			fontModifier = mono,
			colorModifier = Color.lightGray,
		)
		codeBox = Markdown.Box(
			(Tex.whiteui as TextureRegionDrawable).tint(Tmp.c1.set(Pal.darkestGray).a(0.7f)),
			paddingX = 0f,
			paddingY = -6f,
			marginX = 6f,
			marginY = 6f,
		)
		codeBlockBox = Markdown.Box(
			(Tex.whiteui as TextureRegionDrawable).tint(Tmp.c1.set(Pal.darkestGray)),
			marginX = 16f,
			marginY = 16f,
		)
		sliderStyle = Styles.noBarPane

		linkFont = Markdown.FontEntry(
			fontModifier = Fonts.outline,
			colorModifier = Pal.place,
		)
		linkOverColor = Pal.accent

		listOrderFont = Markdown.FontEntry(
			fontModifier = Fonts.def,
			colorModifier = Color.white,
		)
		listItemBox = Markdown.Box(
			paddingLeft = 16f
		)
		listItemHeadBox = Markdown.Box(
			paddingLeft = 16f,
			paddingRight = 8f,
		)
		bulletListMarks = arrayOf(
			object : BaseDrawable() {
				override fun draw(x: Float, y: Float, width: Float, height: Float) {
					Fill.square(x + width / 2, y + height / 2, width / 2.2f, 45f)
				}
			}.also { it.minWidth = 12f; it.minHeight = 12f },
			object : BaseDrawable() {
				override fun draw(x: Float, y: Float, width: Float, height: Float) {
					Fill.circle(x + width / 2, y + height / 2, width / 2f)
				}
			}.also { it.minWidth = 12f; it.minHeight = 12f },
			object : BaseDrawable() {
				override fun draw(x: Float, y: Float, width: Float, height: Float) {
					Lines.stroke(Scl.scl(1f))
					Lines.circle(x + width / 2, y + height / 2, width / 2f)
				}
			}.also { it.minWidth = 12f; it.minHeight = 12f }
		)
		orderedListFormatters = arrayOf(
			{ it.toString() },
			{ ('a'.code + (it - 1) % 26).toChar().toString() },
			{ romeDigitize(it) }
		)

		tableBack1 = Markdown.Box(
			makeTableCellBack(
				Pal.darkestGray.cpy().a(0.7f),
				Color.lightGray,
				Scl.scl(2f),
			),
			marginX = 32f,
			marginY = 32f,
		)
		tableBack2 = Markdown.Box(
			makeTableCellBack(
				Pal.darkerGray.cpy().a(0.7f),
				Color.lightGray,
				Scl.scl(2f),
			),
			marginX = 32f,
			marginY = 32f,
		)
	}

	fun makeTableCellBack(
		backColor: Color,
		lineColor: Color,
		lineStroke: Float,
	) = object : BaseDrawable() {
		override fun draw(x: Float, y: Float, width: Float, height: Float) {
			Draw.color(backColor)
			Fill.rect(x + width / 2, y + height / 2, width, height)
			Lines.stroke(lineStroke, lineColor)
			Lines.line(x, y, x + width, y)
			Lines.line(x, y + height, x + width, y + height)
		}
	}

	private val values = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
	private val romeSymbols = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
	private fun romeDigitize(n: Int): String {
		var num = n
		val result = StringBuilder()

		for (i in values.indices) {
			while (num >= values[i]) {
				result.append(romeSymbols[i])
				num -= values[i]
			}
		}

		return result.toString()
	}
}
