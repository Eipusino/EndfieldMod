package endfield.ui.markdown.extensions.curtain

import org.commonmark.node.Text
import org.commonmark.parser.beta.InlineContentParser
import org.commonmark.parser.beta.InlineContentParserFactory
import org.commonmark.parser.beta.InlineParserState
import org.commonmark.parser.beta.ParsedInline
import org.commonmark.text.Characters

class CurtainParser internal constructor() : InlineContentParser {
	companion object {
		private const val DELIMITER = '$'
	}

	override fun tryParse(inlineParserState: InlineParserState): ParsedInline {
		val scanner = inlineParserState.scanner()
		val start = scanner.position()
		val openingTicks = scanner.matchMultiple(DELIMITER)
		val afterOpening = scanner.position()

		while (scanner.find(DELIMITER) > 0) {
			val beforeClosing = scanner.position()
			val count = scanner.matchMultiple(DELIMITER)
			if (count == openingTicks) {
				val node = Curtain()

				var content = scanner.getSource(afterOpening, beforeClosing).content
				content = content.replace('\n', ' ')

				if (content.length >= 3 && content[0] == ' ' && content[content.length - 1] == ' ' &&
					Characters.hasNonSpace(content)
				) {
					content = content.substring(1, content.length - 1)
				}

				node.literal = content
				return ParsedInline.of(node, scanner.position())
			}
		}

		val source = scanner.getSource(start, afterOpening)
		val text = Text(source.content)
		return ParsedInline.of(text, afterOpening)
	}

	class Factory : InlineContentParserFactory {
		override fun getTriggerCharacters(): MutableSet<Char> {
			return mutableSetOf(DELIMITER)
		}

		override fun create(): InlineContentParser {
			return CurtainParser()
		}
	}
}
