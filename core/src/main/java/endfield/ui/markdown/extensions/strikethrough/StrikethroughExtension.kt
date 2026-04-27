package endfield.ui.markdown.extensions.strikethrough

import endfield.ui.markdown.MDLayoutRenderer
import org.commonmark.Extension
import org.commonmark.ext.gfm.strikethrough.internal.StrikethroughDelimiterProcessor
import org.commonmark.parser.Parser

class StrikethroughExtension : Parser.ParserExtension, MDLayoutRenderer.DrawRendererExtension {
	companion object {
		fun create(): Extension = StrikethroughExtension()
	}

	override fun extend(parserBuilder: Parser.Builder) {
		parserBuilder.customDelimiterProcessor(StrikethroughDelimiterProcessor())
	}

	override fun extend(rendererBuilder: MDLayoutRenderer.Builder) {
		rendererBuilder.nodeRendererFactory(::StrikethroughRenderer)
	}
}