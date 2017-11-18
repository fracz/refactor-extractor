package nokogiri;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Hashtable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.RubyBoolean;
import org.jruby.RubyClass;
import org.jruby.RubyFixnum;
import org.jruby.RubyHash;
import org.jruby.RubyObject;
import org.jruby.RubyString;
import org.jruby.anno.JRubyMethod;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.util.RuntimeHelpers;
import org.jruby.runtime.Arity;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.Visibility;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.ByteList;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



public class XmlNode extends RubyObject {

    private XmlNodeImpl internalNode;
    protected Hashtable<Node,IRubyObject> internalCache;
    protected NokogiriNamespaceCache nsCache;

    /*
     * Taken from http://ejohn.org/blog/comparing-document-position/
     * Used for compareDocumentPosition.
     * <ironic>Thanks to both java api and w3 doc for its helpful documentation</ironic>
     */

    protected static final int IDENTICAL_ELEMENTS = 0;
    protected static final int IN_DIFFERENT_DOCUMENTS = 1;
    protected static final int SECOND_PRECEDES_FIRST = 2;
    protected static final int FIRST_PRECEDES_SECOND = 4;
    protected static final int SECOND_CONTAINS_FIRST = 8;
    protected static final int FIRST_CONTAINS_SECOND = 16;

    public XmlNode(Ruby ruby, RubyClass cls){
        this(ruby,cls,null);
    }

    public XmlNode(Ruby ruby, RubyClass cls, Node node) {
        super(ruby, cls);
        this.internalCache = new Hashtable<Node,IRubyObject>();
        this.nsCache = new NokogiriNamespaceCache();
        this.internalNode = new XmlNodeImpl(ruby, node);
    }

    protected void assimilateXmlNode(ThreadContext context, IRubyObject otherNode) {
        XmlNode toAssimilate = asXmlNode(context, otherNode);

        this.internalNode = toAssimilate.internalNode;
    }

    private XmlNode asXmlNode(ThreadContext context, IRubyObject node) {
        if (!(node instanceof XmlNode)) {
            throw context.getRuntime().newTypeError(node, (RubyClass) context.getRuntime().getClassFromPath("Nokogiri::XML::Node"));
        }

        return (XmlNode) node;
    }

    /**
     * Coalesce to adjacent TextNodes.
     * @param context
     * @param prev Previous node to cur.
     * @param cur Next node to prev.
     */
    protected void coalesceTextNodes(ThreadContext context, IRubyObject prev, IRubyObject cur) {
        XmlNode p = asXmlNode(context, prev);
        XmlNode c = asXmlNode(context, cur);

        Node pNode = p.node();
        Node cNode = c.node();

        pNode.setNodeValue(pNode.getNodeValue()+cNode.getNodeValue());
        p.internalNode.resetContent();

        c.assimilateXmlNode(context, p);
    }

    /**
     * Given three nodes such that firstNode is previousSibling of secondNode
     * and secondNode is previousSibling of third node, this method coalesces
     * two subsequent TextNodes.
     * @param context
     * @param firstNode
     * @param secondNode
     * @param thirdNode
     */
    protected void coalesceTextNodesInteligently(ThreadContext context, IRubyObject firstNode,
            IRubyObject secondNode, IRubyObject thirdNode) {

        Node first = (firstNode.isNil()) ? null : asXmlNode(context, firstNode).node();
        Node second = asXmlNode(context, secondNode).node();
        Node third = (thirdNode.isNil()) ? null : asXmlNode(context, thirdNode).node();

        if(second.getNodeType() == Node.TEXT_NODE) {
            if(first != null && first.getNodeType() == Node.TEXT_NODE) {
                coalesceTextNodes(context, firstNode, secondNode);
            } else if(third != null && third.getNodeType() == Node.TEXT_NODE) {
                coalesceTextNodes(context, secondNode, thirdNode);
            }
        }

    }

    protected static IRubyObject constructNode(Ruby ruby, Node node) {
        if (node == null) return ruby.getNil();
        // this is slow; need a way to cache nokogiri classes/modules somewhere
        switch (node.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                return new XmlAttr(ruby, node);
            case Node.TEXT_NODE:
                return new XmlText(ruby, (RubyClass)ruby.getClassFromPath("Nokogiri::XML::Text"), node);
            case Node.COMMENT_NODE:
                return new XmlNode(ruby, (RubyClass)ruby.getClassFromPath("Nokogiri::XML::Comment"), node);
            case Node.ELEMENT_NODE:
                return new XmlElement(ruby, (RubyClass)ruby.getClassFromPath("Nokogiri::XML::Element"), node);
            case Node.ENTITY_NODE:
                return new XmlNode(ruby, (RubyClass)ruby.getClassFromPath("Nokogiri::XML::EntityDeclaration"), node);
            case Node.CDATA_SECTION_NODE:
                return new XmlCdata(ruby, (RubyClass)ruby.getClassFromPath("Nokogiri::XML::CDATA"), node);
            case Node.DOCUMENT_TYPE_NODE:
                return new XmlDtd(ruby, (RubyClass)ruby.getClassFromPath("Nokogiri::XML::DTD"), node);
            default:
                return new XmlNode(ruby, (RubyClass)ruby.getClassFromPath("Nokogiri::XML::Node"), node);
        }
    }

    protected IRubyObject getFromInternalCache(ThreadContext context, Node node) {

        if(node == null) return context.getRuntime().getNil();

        IRubyObject res = this.internalCache.get(node);

        if(res == null) {
            res = XmlNode.constructNode(context.getRuntime(), node);
            this.internalCache.put(node, res);
        }

        return res;
    }

    protected RubyArray getNsDefinitions(Ruby ruby) {
        return this.internalNode.getNsDefinitions(ruby);
    }

    public Node getNode() {
        return this.internalNode.getNode();
    }

    public static Node getNodeFromXmlNode(ThreadContext context, IRubyObject xmlNode) {
        Ruby ruby = context.getRuntime();
        if (!(xmlNode instanceof XmlNode)) throw ruby.newTypeError(xmlNode, (RubyClass)ruby.getClassFromPath("Nokogiri::XML::Node"));
        return ((XmlNode)xmlNode).node();
    }

    protected Node getNodeToCompare() {
        return this.getNode();
    }

    /*
     * A more rubyist way to get the internal node.
     */

    protected Node node() {
        return this.getNode();
    }

    protected void relink_namespace(ThreadContext context) {
        if(this.node().getNodeType() == Node.ELEMENT_NODE) {
            Element e = (Element) this.node();
            e.getOwnerDocument().renameNode(e, e.lookupNamespaceURI(e.getPrefix()), e.getNodeName());

            if(e.hasAttributes()) {
                NamedNodeMap attrs = e.getAttributes();

                for(int i = 0; i < attrs.getLength(); i++) {
                    Attr attr = (Attr) attrs.item(i);
                    e.getOwnerDocument().renameNode(attr, attr.lookupNamespaceURI(attr.getPrefix()), attr.getNodeName());
                }
            }
        }

        ((XmlNodeSet) this.children(context)).relink_namespace(context);
    }

    protected void setDocument(IRubyObject doc) {
        this.internalNode.setDocument(doc);
    }

    protected void setNode(Node node) {
        this.internalNode.setNode(node);
    }

    protected void updateNodeNamespaceIfNecessary(ThreadContext context, XmlNamespace ns) {
        String oldPrefix = this.node().getPrefix();
        String uri = ns.href(context).convertToString().asJavaString();
        String newPrefix;

        /*
         * Update if both prefixes are null or equal
         */
        boolean update = (oldPrefix == null && ns.prefix(context).isNil()) ||
                            (oldPrefix != null && !ns.prefix(context).isNil()
                && oldPrefix.equals(ns.prefix(context).convertToString().asJavaString()));

        if(update) {
            this.node().getOwnerDocument().renameNode(this.node(), uri, this.node().getNodeName());
            this.internalNode.setNamespace(ns);
        }
    }

    @JRubyMethod(name = "new", meta = true)
    public static IRubyObject rbNew(ThreadContext context, IRubyObject cls, IRubyObject name, IRubyObject doc) {

        if(!(doc instanceof XmlDocument)) {
            throw context.getRuntime().newArgumentError("document must be an instance of Nokogiri::XML::Document");
        }

        XmlDocument xmlDoc = (XmlDocument)doc;
        Document document = xmlDoc.getDocument();
        Element element = document.createElementNS(null, name.convertToString().asJavaString());

        XmlNode node = new XmlNode(context.getRuntime(), (RubyClass)cls, element);
        node.internalNode.setDocument(doc);

        RuntimeHelpers.invoke(context, xmlDoc, "decorate", node);

        xmlDoc.cacheNode(element, node);

        return node;
    }

    @JRubyMethod
    public IRubyObject add_child(ThreadContext context, IRubyObject child) {
        XmlNode childNode = asXmlNode(context, child);
        Node appended = childNode.node();

        if(appended.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {

            // Some magic for DocumentFragment
            Ruby ruby = context.getRuntime();
            XmlNodeSet children = (XmlNodeSet) childNode.children(context);

            long length = children.length();

            RubyArray childrenArray = children.convertToArray();

            if(length != 0) {
                for(int i = 0; i < length; i++) {
                    add_child(context, childrenArray.aref(ruby.newFixnum(i)));
                }
            }

        } else {

            if(childNode.document(context) != this.document(context)) {
                this.node().getOwnerDocument().adoptNode(appended);
                childNode.internalNode.setDocument(this.internalNode.getDocument(context));
            } else if(appended.getParentNode() != null) {
                childNode.unlink(context);
            }

            if(appended.getNodeType() == Node.TEXT_NODE) {
                RubyArray children = ((XmlNodeSet) this.children(context)).convertToArray();
                if(!children.isEmpty()) {
                    XmlNode last = (XmlNode) children.last();
                    this.coalesceTextNodes(context, last, child);
                    return child;
                }
            }

            try{
                node().appendChild(appended);
            } catch (Exception ex) {
                throw context.getRuntime().newRuntimeError(ex.toString());
            }

            childNode.relink_namespace(context);
        }

        return child;
    }

    @JRubyMethod
    public IRubyObject add_namespace_definition(ThreadContext context, IRubyObject prefix, IRubyObject href) {
        String prefixString = prefix.isNil() ? "" : prefix.convertToString().asJavaString();
        String hrefString = href.convertToString().asJavaString();
        XmlNamespace ns = this.nsCache.get(context, this, prefixString, hrefString);

        if(this.node().getNodeType() == Node.ELEMENT_NODE) {

            Element e = (Element) this.node();
            prefixString = (prefix.isNil()) ? "xmlns" : "xmlns:"+prefixString;
            e.setAttribute(prefixString, hrefString);

            updateNodeNamespaceIfNecessary(context, ns);

            this.internalNode.resetNamespaceDefinitions();
        }
        return ns;
    }

    @JRubyMethod
    public IRubyObject add_next_sibling(ThreadContext context, IRubyObject appendNode) {
        IRubyObject nextSibling = this.next_sibling(context);

        XmlNode otherNode = asXmlNode(context, appendNode);
        Node next = this.node().getNextSibling();
        if (next != null) {
            this.node().getParentNode().insertBefore(otherNode.node(), next);
        } else {
            this.node().getParentNode().appendChild(otherNode.node());
        }
        RuntimeHelpers.invoke(context, otherNode, "decorate!");

        coalesceTextNodesInteligently(context, this, appendNode, nextSibling);

        return otherNode;
    }

    @JRubyMethod
    public IRubyObject add_previous_sibling(ThreadContext context, IRubyObject node) {
        IRubyObject previousSibling = this.previous_sibling(context);
        XmlNode otherNode = asXmlNode(context, node);

        this.node().getParentNode().insertBefore(otherNode.node(), this.node());
        RuntimeHelpers.invoke(context , otherNode, "decorate!");

        coalesceTextNodesInteligently(context, previousSibling, otherNode, this);

        return node;
    }

    @JRubyMethod
    public IRubyObject attribute(ThreadContext context, IRubyObject name){
        NamedNodeMap attrs = this.node().getAttributes();
        Node attr = attrs.getNamedItem(name.convertToString().asJavaString());
        if(attr == null) {
            return  context.getRuntime().getNil();
        }
        return constructNode(context.getRuntime(), attr);
    }

    @JRubyMethod
    public IRubyObject attribute_nodes(ThreadContext context) {
        NamedNodeMap nodeMap = this.node().getAttributes();

        if(nodeMap == null){
            return context.getRuntime().newEmptyArray();
        }

        RubyArray attr = context.getRuntime().newArray();

        for(int i = 0; i < nodeMap.getLength(); i++) {
            attr.append(this.getFromInternalCache(context, nodeMap.item(i)));
        }

        return attr;
    }

    @JRubyMethod
    public IRubyObject attribute_with_ns(ThreadContext context, IRubyObject name, IRubyObject namespace) {
        String namej = name.convertToString().asJavaString();
        String nsj = (namespace.isNil()) ? null : namespace.convertToString().asJavaString();

        Node el = this.node().getAttributes().getNamedItemNS(nsj, namej);

        return this.getFromInternalCache(context, el);
    }

    @JRubyMethod
    public IRubyObject attributes(ThreadContext context) {
        Ruby ruby = context.getRuntime();
        RubyHash hash = RubyHash.newHash(ruby);
        NamedNodeMap attrs = node().getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            hash.op_aset(context, RubyString.newString(ruby, attr.getNodeName()), RubyString.newString(ruby, attr.getNodeValue()));
        }
        return hash;
    }

    @JRubyMethod(name = "blank?")
    public IRubyObject blank_p(ThreadContext context) {
        return RubyBoolean.newBoolean(context.getRuntime(), node() instanceof Text && ((Text)node()).isElementContentWhitespace());
    }

    @JRubyMethod
    public IRubyObject child(ThreadContext context) {
        return constructNode(context.getRuntime(), node().getFirstChild());
    }

    @JRubyMethod
    public IRubyObject children(ThreadContext context) {
       return new XmlNodeSet(context.getRuntime(), (RubyClass) context.getRuntime().getClassFromPath("Nokogiri::XML::NodeSet"), this.node().getChildNodes());
    }

    @JRubyMethod
    public IRubyObject compare(ThreadContext context, IRubyObject otherNode) {
        if(!(otherNode instanceof XmlNode)) {
            return context.getRuntime().newFixnum(-2);
        }

        Node on = ((XmlNode) otherNode).getNodeToCompare();

        // Do not touch this if, if it's not for a good reason.
        if(getNodeToCompare().getNodeType() == Node.DOCUMENT_NODE ||
                on.getNodeType() == Node.DOCUMENT_NODE) {
            return context.getRuntime().newFixnum(-1);
        }

        try{
            int res = getNodeToCompare().compareDocumentPosition(on);
            if( (res & FIRST_PRECEDES_SECOND) == FIRST_PRECEDES_SECOND) {
                return context.getRuntime().newFixnum(-1);
            } else if ( (res & SECOND_PRECEDES_FIRST) == SECOND_PRECEDES_FIRST) {
                return context.getRuntime().newFixnum(1);
            } else if ( res == IDENTICAL_ELEMENTS) {
                return context.getRuntime().newFixnum(0);
            }

            return context.getRuntime().newFixnum(-2);
        } catch (Exception ex) {
            return context.getRuntime().newFixnum(-2);
        }
    }

    @JRubyMethod
    public IRubyObject content(ThreadContext context) {
        return this.internalNode.getContent(context);
    }

    @JRubyMethod
    public IRubyObject document(ThreadContext context) {
        return this.internalNode.getDocument(context);
    }

    @JRubyMethod
    public IRubyObject dup(ThreadContext context) {
        return this.dup_implementation(context, true);
    }

    @JRubyMethod
    public IRubyObject dup(ThreadContext context, IRubyObject depth) {
        boolean deep = depth.convertToInteger().getLongValue() != 0;

        return this.dup_implementation(context, deep);
    }

    protected IRubyObject dup_implementation(ThreadContext context, boolean deep) {
        Node newNode = node().cloneNode(deep);

        return new XmlNode(context.getRuntime(), this.getType(), newNode);
    }

    @JRubyMethod
    public IRubyObject encode_special_chars(ThreadContext context, IRubyObject string) {
        String s = string.convertToString().asJavaString();
        // From entities.c
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        s = s.replaceAll("\"", "&quot;");
        s = s.replaceAll("\r", "&#13;");
        return RubyString.newString(context.getRuntime(), s);
    }

    @JRubyMethod(visibility = Visibility.PRIVATE)
    public IRubyObject get(ThreadContext context, IRubyObject attribute) {
        String key = attribute.convertToString().asJavaString();
        if (node() instanceof Element) {
            Element element = (Element)node();
            String value = element.getAttribute(key);
            if(!value.equals("")){
                return RubyString.newString(context.getRuntime(), value);
            }
        }
        return context.getRuntime().getNil();
    }

    @JRubyMethod
    public IRubyObject internal_subset(ThreadContext context) {
        if(this.node().getOwnerDocument() == null) {
            return context.getRuntime().getNil();
        }

        return XmlNode.constructNode(context.getRuntime(), this.node().getOwnerDocument().getDoctype());
    }

    @JRubyMethod(name = "key?")
    public IRubyObject key_p(ThreadContext context, IRubyObject k) {
        Ruby ruby = context.getRuntime();
        String key = k.convertToString().asJavaString();
        if (node().getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element)node();
            if (element.hasAttribute(key)) {
                return ruby.getTrue();
            }
        }
        return ruby.getFalse();
    }

    @JRubyMethod
    public IRubyObject namespace(ThreadContext context){
        return this.internalNode.getNamespace(context);
    }

    @JRubyMethod
    public IRubyObject namespace_definitions(ThreadContext context) {
        return this.getNsDefinitions(context.getRuntime());
    }

    @JRubyMethod(name="namespaced_key?")
    public IRubyObject namespaced_key_p(ThreadContext context, IRubyObject elementLName, IRubyObject namespaceUri) {
        return this.attribute_with_ns(context, elementLName, namespaceUri).isNil() ?
            context.getRuntime().getFalse() : context.getRuntime().getTrue();
    }

    @JRubyMethod
    public IRubyObject namespaces(ThreadContext context) {
        Ruby ruby = context.getRuntime();
        RubyHash hash = RubyHash.newHash(ruby);
        NamedNodeMap attrs = node().getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            hash.op_aset(context, RubyString.newString(ruby, attr.getNodeName()), RubyString.newString(ruby, attr.getNodeValue()));
        }
        return hash;
    }

    @JRubyMethod(name = "native_content=", visibility = Visibility.PRIVATE)
    public IRubyObject native_content_set(ThreadContext context, IRubyObject content) {
        RubyString newContent = content.convertToString();
        this.internalNode.setContent(newContent);
        this.node().setTextContent(newContent.asJavaString());
        return content;
    }

    @JRubyMethod(required=4, visibility=Visibility.PRIVATE)
    public IRubyObject native_write_to(ThreadContext context, IRubyObject[] args) {//IRubyObject io, IRubyObject encoding, IRubyObject indentString, IRubyObject options) {
        IRubyObject io = args[0];
        // Not used by now.
//        IRubyObject encoding = args[1];
//        IRubyObject indentString = args[2];
//        IRubyObject options = args[3];

        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.transform(new DOMSource(this.node()), new StreamResult(sw));
        } catch (TransformerException te) {
            throw context.getRuntime().newRuntimeError("couldn't transform the node back to string");
        }

        RuntimeHelpers.invoke(context, io, "write", context.getRuntime().newString(sw.toString()));

        return io;
    }

    @JRubyMethod
    public IRubyObject next_sibling(ThreadContext context) {
        return constructNode(context.getRuntime(), node().getNextSibling());
    }

    @JRubyMethod(meta = true, rest = true)
    public static IRubyObject new_from_str(ThreadContext context, IRubyObject cls, IRubyObject[] args) {
        // TODO: duplicating code from Document.read_memory
        Ruby ruby = context.getRuntime();
        Arity.checkArgumentCount(ruby, args, 4, 4);
        try {
            Document document;
            RubyString content = args[0].convertToString();
            ByteList byteList = content.getByteList();
            ByteArrayInputStream bais = new ByteArrayInputStream(byteList.unsafeBytes(), byteList.begin(), byteList.length());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String arg0, String arg1) throws SAXException, IOException {
                    return new InputSource(new ByteArrayInputStream(new byte[0]));
                }
            });
            document = db.parse(bais);
            return constructNode(ruby, document.getFirstChild());
        } catch (ParserConfigurationException pce) {
            throw RaiseException.createNativeRaiseException(ruby, pce);
        } catch (SAXException saxe) {
            throw RaiseException.createNativeRaiseException(ruby, saxe);
        } catch (IOException ioe) {
            throw RaiseException.createNativeRaiseException(ruby, ioe);
        }
    }

    @JRubyMethod
    public IRubyObject node_name(ThreadContext context) {
        return this.internalNode.getNodeName(context);
    }

    @JRubyMethod(name = "node_name=")
    public IRubyObject node_name_set(ThreadContext context, IRubyObject nodeName) {
        if(this.node().getNodeType() == Node.ATTRIBUTE_NODE ||
                this.node().getNodeType() == Node.ELEMENT_NODE) {
            String newName = nodeName.convertToString().asJavaString();
            this.node().getOwnerDocument().renameNode(this.node(), null, newName);
            this.internalNode.setName(nodeName);
        }
        return nodeName;
    }

    @JRubyMethod(name = "[]=")
    public IRubyObject op_aset(ThreadContext context, IRubyObject index, IRubyObject val) {
        String key = index.convertToString().asJavaString();
        String value = val.convertToString().asJavaString();
        if (node().getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element)node();
            element.setAttribute(key, value);
        }
        return val;
    }

    @JRubyMethod
    public IRubyObject parent(ThreadContext context) {
        return constructNode(context.getRuntime(), node().getParentNode());
    }

    @JRubyMethod(name = "parent=")
    public IRubyObject parent_set(ThreadContext context, IRubyObject parent) {
        Node otherNode = getNodeFromXmlNode(context, parent);
        otherNode.appendChild(node());
        return parent;
    }

    @JRubyMethod
    public IRubyObject path(ThreadContext context) {
        return RubyString.newString(context.getRuntime(), NokogiriHelpers.getNodeCompletePath(this.node()));
    }

    @JRubyMethod
    public IRubyObject pointer_id(ThreadContext context) {
        return RubyFixnum.newFixnum(context.getRuntime(), this.node().hashCode());
    }

    @JRubyMethod
    public IRubyObject previous_sibling(ThreadContext context) {
        return constructNode(context.getRuntime(), node().getPreviousSibling());
    }

    @JRubyMethod
    public IRubyObject remove_attribute(ThreadContext context, IRubyObject name) {
        String key = name.convertToString().asJavaString();
        if (node() instanceof Element) {
            Element element = (Element)node();
            element.removeAttribute(key);
        }
        return context.getRuntime().getNil();
    }

    @JRubyMethod(name="replace_with_node", visibility=Visibility.PROTECTED)
    public IRubyObject replace(ThreadContext context, IRubyObject newNode) {
        Node otherNode = getNodeFromXmlNode(context, newNode);
        node().getParentNode().replaceChild(otherNode, node());

        ((XmlNode) newNode).relink_namespace(context);

        return this;
    }

    @JRubyMethod(visibility=Visibility.PRIVATE)
    public IRubyObject set_namespace(ThreadContext context, IRubyObject namespace) {
        this.internalNode.setNamespace(namespace);
        this.internalNode.resetNamespaceDefinitions();
        XmlNamespace ns = (XmlNamespace) namespace;
        String prefix = ns.prefix(context).convertToString().asJavaString();
        String href = ns.href(context).convertToString().asJavaString();

        this.node().getOwnerDocument().renameNode(node(), href, NokogiriHelpers.newQName(prefix, node()));

        return this;
    }

    @JRubyMethod
    public IRubyObject to_xml(ThreadContext context) {
        try {
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            xformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            xformer.transform(new DOMSource(node()), new StreamResult(baos));
            return RubyString.newString(context.getRuntime(), baos.toByteArray());
        } catch (TransformerFactoryConfigurationError tfce) {
            throw RaiseException.createNativeRaiseException(context.getRuntime(), tfce);
        } catch (TransformerConfigurationException tce) {
            throw RaiseException.createNativeRaiseException(context.getRuntime(), tce);
        } catch (TransformerException te) {
            throw RaiseException.createNativeRaiseException(context.getRuntime(), te);
        }
    }

    @JRubyMethod
    public IRubyObject unlink(ThreadContext context) {
        if(this.node().getNodeType() == Node.ATTRIBUTE_NODE) {
            Attr attr = (Attr) this.node();
            Element parent = attr.getOwnerElement();
            parent.removeAttributeNode(attr);
        } else if(this.node().getParentNode() == null) {
            throw context.getRuntime().newRuntimeError("TYPE: "+this.node().getNodeType()+ " PARENT NULL");
        } else {
            node().getParentNode().removeChild(node());
        }
        return this;
    }

    @JRubyMethod(name = "node_type")
    public IRubyObject xmlType(ThreadContext context) {
        return RubyFixnum.newFixnum(context.getRuntime(), node().getNodeType());
    }
}