package endfield.ui.markdown;

import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Document;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Image;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.ListItem;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.node.ThematicBreak;
import org.commonmark.node.Visitor;

public abstract class NoActionVisitor implements Visitor {
	@Override
	public void visit(BlockQuote blockQuote) {}

	@Override
	public void visit(BulletList bulletList) {}

	@Override
	public void visit(Code code) {}

	@Override
	public void visit(Document document) {}

	@Override
	public void visit(Emphasis emphasis) {}

	@Override
	public void visit(FencedCodeBlock fencedCodeBlock) {}

	@Override
	public void visit(HardLineBreak hardLineBreak) {}

	@Override
	public void visit(Heading heading) {}

	@Override
	public void visit(ThematicBreak thematicBreak) {}

	@Override
	public void visit(HtmlInline htmlInline) {}

	@Override
	public void visit(HtmlBlock htmlBlock) {}

	@Override
	public void visit(Image image) {}

	@Override
	public void visit(IndentedCodeBlock indentedCodeBlock) {}

	@Override
	public void visit(Link link) {}

	@Override
	public void visit(ListItem listItem) {}

	@Override
	public void visit(OrderedList orderedList) {}

	@Override
	public void visit(Paragraph paragraph) {}

	@Override
	public void visit(SoftLineBreak softLineBreak) {}

	@Override
	public void visit(StrongEmphasis strongEmphasis) {}

	@Override
	public void visit(Text text) {}

	@Override
	public void visit(LinkReferenceDefinition linkReferenceDefinition) {}

	@Override
	public void visit(CustomBlock customBlock) {}

	@Override
	public void visit(CustomNode customNode) {}
}
