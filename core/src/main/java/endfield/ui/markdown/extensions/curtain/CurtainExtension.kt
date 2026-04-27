package endfield.ui.markdown.extensions.curtain

import endfield.ui.markdown.MDLayoutRenderer
import org.commonmark.Extension
import org.commonmark.parser.Parser
import org.commonmark.parser.Parser.ParserExtension

class CurtainExtension : ParserExtension, MDLayoutRenderer.DrawRendererExtension {
	companion object {
		fun create(): Extension = CurtainExtension()
	}

	override fun extend(parserBuilder: Parser.Builder) {
		parserBuilder.customInlineContentParserFactory(CurtainParser.Factory())
	}

	override fun extend(rendererBuilder: MDLayoutRenderer.Builder) {
		rendererBuilder.nodeRendererFactory(::CurtainRenderer)
	}
}