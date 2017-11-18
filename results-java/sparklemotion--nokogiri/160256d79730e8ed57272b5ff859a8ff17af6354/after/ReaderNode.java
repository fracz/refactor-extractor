package nokogiri;

import org.jruby.Ruby;
import org.jruby.RubyBoolean;
import org.jruby.runtime.builtin.IRubyObject;
import org.xml.sax.Attributes;

abstract class ReaderNode {

    Ruby ruby;
    RubyBoolean value_p;
    IRubyObject uri, localName, qName, value;
    Attributes attrs;

    // Construct a Text Node.
    public static ReaderNode createTextNode(Ruby ruby, String content) {
        return new TextNode(ruby, content);
    }

    // Construct an Element Node. Maybe, if this go further, I should make subclasses.
    public static ReaderNode createElementNode(Ruby ruby, String uri, String localName, String qName, Attributes attrs) {
        return new ElementNode(ruby, uri, localName, qName, attrs);
    }

    public boolean fits(String uri, String localName, String qName) {
        return this.uri.asJavaString().equals(uri) &&
                this.localName.asJavaString().equals(localName) &&
                this.qName.asJavaString().equals(qName);
    }

    public static ReaderNode getEmptyNode(Ruby ruby) {
        return new EmptyNode(ruby);
    }

    public IRubyObject getLocalName() {
        return this.localName;
    }

    public IRubyObject getName() {
        return this.qName;
    }

    public IRubyObject getQName() {
        return this.qName;
    }

    public IRubyObject getUri() {
        return this.uri;
    }

    public IRubyObject getValue() {
        return this.value;
    }

    public abstract RubyBoolean hasValue();

    public RubyBoolean isDefault(){
        // TODO Implement.
        return ruby.getFalse();
    }

    protected IRubyObject toRubyString(String string) {
        return (string == null) ? this.ruby.newString() : this.ruby.newString(string);
    }
}

class ElementNode extends ReaderNode {

    public ElementNode(Ruby ruby, String uri, String localName, String qName, Attributes attrs) {
        this.ruby = ruby;
        this.uri = toRubyString(uri);
        this.localName = toRubyString(localName);
        this.qName = toRubyString(qName);
        this.value_p = ruby.getFalse();
        this.attrs = attrs; // I don't know what to do with you yet, my friend.
    }

    @Override
    public RubyBoolean hasValue() {
        return ruby.getFalse();
    }

}

class EmptyNode extends ReaderNode {

    public EmptyNode(Ruby ruby) {
        this.ruby = ruby;
        this.uri = this.value = this.localName = this.qName = ruby.getNil();
    }

    @Override
    public RubyBoolean hasValue() {
        return ruby.getFalse();
    }
}
class TextNode extends ReaderNode {

    public TextNode(Ruby ruby, String content) {
        this.ruby = ruby;
        this.uri = ruby.newString();
        this.value = toRubyString(content);
        this.localName = toRubyString("#text");
        this.qName = toRubyString("#text");
    }

    @Override
    public RubyBoolean hasValue() {
        return ruby.getTrue();
    }
}