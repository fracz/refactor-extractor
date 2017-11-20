package org.antlr.v4.tool;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/** Track the attributes within a scope.  A named scoped has just its list
 *  of attributes.  Each rule has potentially 3 scopes: return values,
 *  parameters, and an implicitly-named scope (i.e., a scope defined in a rule).
 *  Implicitly-defined scopes are named after the rule; rules and scopes then
 *  must live in the same name space--no collisions allowed.
 */
public class AttributeScope {
    /** The scope name */
    public String name;
    public GrammarAST ast;
//	public Type type;

    /** All token scopes (token labels) share the same fixed scope of
     *  of predefined attributes.  I keep this out of the runtime.Token
     *  object to avoid a runtime type leakage.
     */
    public static AttributeScope predefinedTokenScope = new AttributeScope() {{
        add(new Attribute("text"));
        add(new Attribute("type"));
        add(new Attribute("line"));
        add(new Attribute("index"));
        add(new Attribute("pos"));
        add(new Attribute("channel"));
        add(new Attribute("tree"));
        add(new Attribute("int"));
    }};

//    public static enum Type {
//        ARG, RET, TOKEN, PREDEFINED_RULE, PREDEFINED_LEXER_RULE,
//        GLOBAL_SCOPE,   // scope symbols { ...}
//        RULE_SCOPE;     // scope { int i; int j; }
//    }

    /** The list of Attribute objects */

    public LinkedHashMap<String, Attribute> attributes =
        new LinkedHashMap<String, Attribute>();

    public Attribute add(Attribute a) { return attributes.put(a.name, a); }
    public Attribute get(String name) { return attributes.get(name); }

    public String getName() {
//        if ( isParameterScope ) {
//            return name+"_parameter";
//        }
//        else if ( isReturnScope ) {
//            return name+"_return";
//        }
        return name;
    }

    public int size() { return attributes==null?0:attributes.size(); }

    /** Return the set of keys that collide from
     *  this and other.
     */
    public Set intersection(AttributeScope other) {
        if ( other==null || other.size()==0 || size()==0 ) {
            return null;
        }
        Set inter = new HashSet();
        Set thisKeys = attributes.keySet();
        for (Iterator it = thisKeys.iterator(); it.hasNext();) {
            String key = (String) it.next();
            if ( other.attributes.get(key)!=null ) {
                inter.add(key);
            }
        }
        if ( inter.size()==0 ) {
            return null;
        }
        return inter;
    }

    public String toString() {
        return getName()+":"+attributes;
    }
}