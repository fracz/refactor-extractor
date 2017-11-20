package org.antlr.v4.codegen.model;

import org.antlr.v4.codegen.OutputModelFactory;
import org.antlr.v4.tool.ActionAST;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.Rule;

import java.util.Collection;

/** A model object representing a parse tree listener file.
 *  These are the rules specific events triggered by a parse tree visitor.
 */
public class ListenerFile extends OutputModelObject {
	public String fileName;
	public String grammarName;
	public String parserName;
	public Collection<Rule> rules;

	@ModelElement public Action header;

	public ListenerFile(OutputModelFactory factory, String fileName) {
		super(factory);
		this.fileName = fileName;
		Grammar g = factory.getGrammar();
		parserName = g.getRecognizerName();
		grammarName = g.name;
		rules = g.rules.values();
		ActionAST ast = g.namedActions.get("header");
		if ( ast!=null ) header = new Action(factory, ast);
	}
}