package endfield.ui.markdown.extensions.imgattr

import org.commonmark.Extension
import org.commonmark.parser.Parser

class ImgAttrExtension : Parser.ParserExtension {
	companion object {
		fun create(): Extension = ImgAttrExtension()
	}

	override fun extend(parserBuilder: Parser.Builder) {
		parserBuilder.customDelimiterProcessor(ImgAttrDelimiterProcessor())
	}
}