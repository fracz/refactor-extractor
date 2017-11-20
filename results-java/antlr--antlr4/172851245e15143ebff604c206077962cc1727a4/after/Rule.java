package org.antlr.v4.tool;

import org.antlr.runtime.Token;
import org.stringtemplate.v4.misc.MultiMap;

import java.util.*;

public class Rule implements SymbolSpace {
    /** Rule refs have a predefined set of attributes as well as
     *  the return values and args.
     */
    public static AttributeScope predefinedRulePropertiesScope =
        new AttributeScope() {{
            add(new Attribute("text"));
            add(new Attribute("start"));
            add(new Attribute("stop"));
            add(new Attribute("tree"));
            add(new Attribute("st"));
        }};

    public static AttributeScope predefinedTreeRulePropertiesScope =
        new AttributeScope() {{
            add(new Attribute("text"));
            add(new Attribute("start")); // note: no stop; not meaningful
            add(new Attribute("tree"));
            add(new Attribute("st"));
        }};

    public static AttributeScope predefinedLexerRulePropertiesScope =
        new AttributeScope() {{
            add(new Attribute("text"));
            add(new Attribute("type"));
            add(new Attribute("line"));
            add(new Attribute("index"));
            add(new Attribute("pos"));
            add(new Attribute("channel"));
            add(new Attribute("start"));
            add(new Attribute("stop"));
            add(new Attribute("int"));
        }};

    public String name;
    public GrammarASTWithOptions ast;
    public AttributeScope args;
    public AttributeScope retvals;
    public AttributeScope scope;
    /** A list of scope names used by this rule */
    public List<Token> useScopes;
    public Grammar g;

    /** Map a name to an action for this rule like @init {...}.
     *  The code generator will use this to fill holes in the rule template.
     *  I track the AST node for the action in case I need the line number
     *  for errors.
     */
    public Map<String, ActionAST> namedActions =
        new HashMap<String, ActionAST>();

    /** Track exception handlers, finally action */
    public List<ActionAST> exceptionActions = new ArrayList<ActionAST>();

    public int numberOfAlts;

    /** Labels are visible to all alts in a rule. Record all defs here.
     *  We need to ensure labels are used to track same kind of symbols.
     *  Tracks all label defs for a label.
    public MultiMap<String, LabelElementPair> labelDefs =
        new MultiMap<String, LabelElementPair>();
     */

    public Alternative[] alt;

    public Rule(Grammar g, String name, GrammarASTWithOptions ast, int numberOfAlts) {
        this.g = g;
        this.name = name;
        this.ast = ast;
        this.numberOfAlts = numberOfAlts;
        alt = new Alternative[numberOfAlts+1]; // 1..n
        for (int i=1; i<=numberOfAlts; i++) alt[i] = new Alternative(this);
    }

    public SymbolSpace getParent() { return g; }

    /** Is isolated x an arg, retval, predefined prop? */
    public boolean resolves(String x, ActionAST node) {
        if ( resolvesAsRetvalOrProperty(x) ) return true;
        if ( args.get(x)!=null ) return true;
        // resolve outside of an alt?
        if ( node.space instanceof Alternative ) return getParent().resolves(x, node);
        return getLabelNames().contains(x); // can see all labels if not in alt
    }

    /** For $x.y, is x an arg, retval, predefined prop, token/rule/label ref?
     *  If so, make sure y resolves within that perspective.
     */
    public boolean resolves(String x, String y, ActionAST node) {
        if ( x.equals(this.name) ) { // $x.y ref in rule x is same as $y
            return resolves(y, node);
        }

        MultiMap<String, LabelElementPair> labelDefs = null;
        if ( node.space instanceof Alternative) {
            labelDefs = ((Alternative)node.space).labelDefs;
        }
        else labelDefs = getLabelDefs();

        List<LabelElementPair> labels = labelDefs.get(x); // label?
        if ( labels!=null ) {
            // it's a label ref, compute scope from label type and grammar type
            LabelElementPair anyLabelDef = labels.get(0);
            // predefined?
            if ( getPredefinedScope(anyLabelDef.type).get(y)!=null) return true;
            if ( anyLabelDef.type==LabelType.RULE_LABEL ) {
                Rule ref = g.getRule(anyLabelDef.element.getText());
                return ref.resolvesAsRetvalOrProperty(y);
            }
        }
        return false;
    }

    public boolean resolveToRuleRef(String x, ActionAST node) {
        if ( x.equals(this.name) ) return true;
        MultiMap<String, LabelElementPair> labelDefs = null;
        if ( node.space instanceof Alternative) {
            labelDefs = ((Alternative)node.space).labelDefs;
        }
        else labelDefs = getLabelDefs();
        List<LabelElementPair> labels = labelDefs.get(x);
        if ( labels!=null ) {  // it's a label ref. is it a rule label?
            LabelElementPair anyLabelDef = labels.get(0);
            if ( anyLabelDef.type==LabelType.RULE_LABEL ) return true;
        }
        return false;
    }

    public boolean resolvesAsRetvalOrProperty(String y) {
        if ( retvals.get(y)!=null ) return true;
        AttributeScope s = getPredefinedScope(LabelType.RULE_LABEL);
        return s.get(y)!=null;
    }

    public Set<String> getRuleRefs() {
        Set<String> refs = new HashSet<String>();
        for (Alternative a : alt) refs.addAll(a.ruleRefs.keySet());
        return refs;
    }

    public Set<String> getLabelNames() {
        Set<String> refs = new HashSet<String>();
        for (int i=1; i<=numberOfAlts; i++) {
            refs.addAll(alt[i].labelDefs.keySet());
        }
        return refs;
    }

    public MultiMap<String, LabelElementPair> getLabelDefs() {
        MultiMap<String, LabelElementPair> defs =
            new MultiMap<String, LabelElementPair>();
        for (int i=1; i<=numberOfAlts; i++) {
            for (List<LabelElementPair> pairs : alt[i].labelDefs.values()) {
                for (LabelElementPair p : pairs) {
                    defs.map(p.label.getText(), p);
                }
            }
        }
        return defs;
    }

    /** Look up name from context of this rule and an alternative.
     *  Find an arg, retval, predefined property, or label/rule/token ref.
     */
//    public Attribute resolve(String name) {
//        Attribute a = args.get(name);   if ( a!=null ) return a;
//        a = retvals.get(name);          if ( a!=null ) return a;
//        AttributeScope properties = getPredefinedScope(LabelType.RULE_LABEL);
//        a = properties.get(name);
//        if ( a!=null ) return a;
//
//        //alt[currentAlt].tokenRefs
//        // not here? look in grammar for global scope
//        return null;
//    }

    /** Resolve x in $x.y to
     */
//    public AttributeScope resolveScope(String name, Alternative alt) {
//        AttributeScope s = null;
//        if ( this.name.equals(name) ) { // $r ref in rule r
//            s = resolveLocalAttributeScope(name);
//            if ( s!=null ) return s;
//        }
//
//        if ( alt.tokenRefs.get(name)!=null ) { // token ref in this alt?
//            return getPredefinedScope(LabelType.TOKEN_LABEL);
//        }
//        if ( alt.ruleRefs.get(name)!=null ) {  // rule ref in this alt?
//            s = getLocalAttributeScope(name);
//            if ( s!=null ) return s;
//        }
//        List<GrammarAST> labels = alt.labelDefs.get(name); // label
//        if ( labels!=null ) {
//            // it's a label ref, compute scope from label type and grammar type
//            LabelElementPair anyLabelDef = labels.get(0);
//            return getPredefinedScope(anyLabelDef.type);
//        }
//        return null;
//    }
//
//    /** Look for name in the arg, return value, predefined property,
//     *  or dynamic scope attribute list for this rule.
//     */
//    public AttributeScope resolveLocalAttributeScope(String name) {
//        if ( args.get(name)!=null ) return args;
//        if ( retvals.get(name)!=null ) return retvals;
//        AttributeScope s = getPredefinedScope(LabelType.RULE_LABEL);
//        if ( s.get(name)!=null ) return s;
//        if ( scope!=null && scope.get(name)!=null ) return scope;
//        return null;
//    }

    public AttributeScope getPredefinedScope(LabelType ltype) {
        String grammarLabelKey = g.getTypeString() + ":" + ltype;
        return Grammar.grammarAndLabelRefTypeToScope.get(grammarLabelKey);
    }

    @Override
    public String toString() {
        return "Rule{" +
               "name='" + name + '\'' +
               ", args=" + args +
               ", retvals=" + retvals +
               ", scope=" + scope +
               '}';
    }
}