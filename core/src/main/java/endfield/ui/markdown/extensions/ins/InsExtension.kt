package endfield.ui.markdown.extensions.ins

import endfield.ui.markdown.MDLayoutRenderer
import org.commonmark.Extension
import org.commonmark.ext.ins.internal.InsDelimiterProcessor
import org.commonmark.parser.Parser

class InsExtension : Parser.ParserExtension, MDLayoutRenderer.DrawRendererExtension {
	companion object {
		fun create(): Extension = InsExtension()
	}

	override fun extend(parserBuilder: Parser.Builder) {
		parserBuilder.customDelimiterProcessor(InsDelimiterProcessor())
	}

	override fun extend(rendererBuilder: MDLayoutRenderer.Builder) {
		rendererBuilder.nodeRendererFactory(::InsNodeRenderer)
	}

}