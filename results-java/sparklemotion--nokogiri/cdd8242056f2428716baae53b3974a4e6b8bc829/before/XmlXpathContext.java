package nokogiri;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyObject;
import org.jruby.anno.JRubyMethod;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.w3c.dom.Node;

public class XmlXpathContext extends RubyObject {
    private Node context;
    private XPath xpath;

    public XmlXpathContext(Ruby ruby, RubyClass rubyClass, Node context) {
        super(ruby, rubyClass);
        this.context = context;
        this.xpath = XPathFactory.newInstance().newXPath();
        this.xpath.setNamespaceContext(new NokogiriNamespaceContext());
    }

    @JRubyMethod(name = "new", meta = true)
    public static IRubyObject rbNew(ThreadContext context, IRubyObject cls, IRubyObject node) {
        XmlNode xmlNode = (XmlNode)node;
        return new XmlXpathContext(context.getRuntime(), (RubyClass)cls, xmlNode.getNode());
    }

    @JRubyMethod
    public IRubyObject evaluate(ThreadContext context, IRubyObject expr, IRubyObject handler) {
        return evaluate(context, expr);
    }

    @JRubyMethod
    public IRubyObject evaluate(ThreadContext context, IRubyObject expr) {
        String src = expr.convertToString().asJavaString();
        try {
            XPathExpression xpathExpression = xpath.compile(src);
            return new XmlXpath(context.getRuntime(), (RubyClass)context.getRuntime().getClassFromPath("Nokogiri::XML::XPath"), xpathExpression, this.context);
        } catch (XPathExpressionException xpee) {
            throw new RaiseException(XmlSyntaxError.getXPathSyntaxError(context));
        }
    }

    @JRubyMethod
    public IRubyObject register_ns(ThreadContext context, IRubyObject prefix, IRubyObject uri) {
        ((NokogiriNamespaceContext) this.xpath.getNamespaceContext()).registerNamespace(prefix.convertToString().asJavaString(), uri.convertToString().asJavaString());
        return this;
    }
}