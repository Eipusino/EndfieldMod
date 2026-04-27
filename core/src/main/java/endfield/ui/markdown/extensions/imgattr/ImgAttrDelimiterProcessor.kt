package endfield.ui.markdown.extensions.imgattr

import org.commonmark.ext.image.attributes.ImageAttributes
import org.commonmark.ext.image.attributes.internal.ImageAttributesDelimiterProcessor
import org.commonmark.node.Image
import org.commonmark.node.Node
import org.commonmark.node.Nodes
import org.commonmark.node.Text
import org.commonmark.parser.delimiter.DelimiterRun

class ImgAttrDelimiterProcessor : ImageAttributesDelimiterProcessor() {
	override fun process(openingRun: DelimiterRun, closingRun: DelimiterRun): Int {
		if (openingRun.length() != 1) return 0

		val opener = openingRun.opener
		val nodeToStyle = opener.previous
		if (nodeToStyle !is Image) return 0

		val toUnlink = ArrayList<Node>()
		val content = StringBuilder()

		for (node in Nodes.between(opener, closingRun.closer)) {
			if (node is Text) {
				content.append(node.literal)
				toUnlink.add(node)
			} else return 0
		}

		val attributesMap = mutableMapOf<String, String>()
		val attributes = content.toString()
		for (s in attributes.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
			val attribute = s.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
			if (attribute.size > 1) {
				attributesMap[attribute[0]] = attribute[1]
			} else return 0
		}

		for (node in toUnlink) node.unlink()

		if (attributesMap.isNotEmpty()) {
			val imageAttributes = ImageAttributes(attributesMap)

			nodeToStyle.appendChild(imageAttributes)
		}

		return 1
	}
}