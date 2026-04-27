package endfield.ui.markdown.elemdraw

import arc.func.Cons
import arc.graphics.g2d.Font
import arc.scene.ui.layout.Scl
import endfield.ui.markdown.RendererContext

private const val MAX_SPLITTABLE_WIDTH = 32 * 16
private val wordSplitMatcher = Regex("[^a-zA-Z0-9_]")

fun RendererContext.drawTextWrap(
	str: String,
	font: Font = getScope().font,
	offsetX: Float = getScope().fontOffsetX,
	offsetY: Float = getScope().fontOffsetY,
	italic: Boolean = getScope().fontIsItalic,
	scl: Float = getScope().fontScale,
	doDraw: Cons<CharSequence> = Cons { s ->
		draw(
			DrawStr.get(
				s.toString(),
				font,
				offsetX,
				offsetY,
				italic,
				getScope().fontColor,
				scl,
			)
		)
	}
) {
	if (mdShouldWrap) {
		val data = font.getData()

		var lastIndex = 0
		var splitIndex = 0
		var currWidth = 0f
		var splitWidth = 0f

		var currScope = getScope()
		str.forEachIndexed { index, c ->
			val glyph = data.getGlyph(c)

			if (wordSplitMatcher.matches(c.toString())) {
				splitIndex = index
				splitWidth = 0f
			}

			if (splitWidth + glyph.xadvance > MAX_SPLITTABLE_WIDTH) {
				splitIndex = index
			}

			if (currWidth + glyph.xadvance * scl > currScope.boundX - currScope.currOffsetX - currScope.marginRight) {
				val appendText = str.substring(lastIndex, splitIndex)
				val remText = str.substring(splitIndex, index).trimStart()

				doDraw.get(appendText)
				currScope = row(Scl.scl(mdStyle.linesPadding))

				lastIndex = splitIndex
				splitIndex = index
				splitWidth = remText.sumOf { data.getGlyph(it).xadvance }.toFloat()
				currWidth = splitWidth * scl
			}

			currWidth += glyph.xadvance * scl
			splitWidth += glyph.xadvance
		}

		if (lastIndex < str.length) {
			doDraw.get(str.substring(lastIndex))
		}
	} else {
		doDraw.get(str)
	}
}
