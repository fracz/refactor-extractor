package org.antlr.v4.codegen;

import org.antlr.v4.automata.DFA;
import org.antlr.v4.codegen.src.*;
import org.antlr.v4.misc.IntervalSet;
import org.antlr.v4.parse.ANTLRParser;
import org.antlr.v4.tool.Alternative;
import org.antlr.v4.tool.BlockAST;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.GrammarAST;

import java.util.List;
import java.util.Stack;

/** Create output objects wthin rule functions */
public abstract class OutputModelFactory {
	public Grammar g;
	public CodeGenerator gen;

	// Context ptrs
	public OutputModelObject file; // root
	public Stack<RuleFunction> currentRule = new Stack<RuleFunction>();
	public Alternative currentAlt;

	protected OutputModelFactory(CodeGenerator gen) {
		this.gen = gen;
		this.g = gen.g;
	}

	public abstract OutputModelObject buildOutputModel();

	public CodeBlock epsilon() { return new CodeBlock(this); }

	public CodeBlock alternative(List<SrcOp> elems) { return new CodeBlock(this, elems); }

	public SrcOp action(GrammarAST ast) { return new Action(this, ast); }

	public SrcOp sempred(GrammarAST ast) { return new SemPred(this, ast); }

	public abstract List<SrcOp> ruleRef(GrammarAST ID, GrammarAST label, GrammarAST args);

	public abstract List<SrcOp> tokenRef(GrammarAST ID, GrammarAST label, GrammarAST args);

	public abstract List<SrcOp> stringRef(GrammarAST ID, GrammarAST label);

	public Choice getChoiceBlock(BlockAST blkAST, GrammarAST ebnfRoot, List<CodeBlock> alts) {
		// TODO: assumes LL1
		int ebnf = 0;
		if ( ebnfRoot!=null ) ebnf = ebnfRoot.getType();
		Choice c = null;
		switch ( ebnf ) {
			case ANTLRParser.OPTIONAL :
				if ( alts.size()==1 ) c = new LL1OptionalBlockSingleAlt(this, ebnfRoot, alts);
				else c = new LL1OptionalBlock(this, ebnfRoot, alts);
				break;
			case ANTLRParser.CLOSURE :
				if ( alts.size()==1 ) c = new LL1StarBlockSingleAlt(this, ebnfRoot, alts);
				else c = new LL1StarBlock(this, ebnfRoot, alts);
				break;
			case ANTLRParser.POSITIVE_CLOSURE :
				if ( alts.size()==1 ) c = new LL1PlusBlockSingleAlt(this, ebnfRoot, alts);
				else c = new LL1PlusBlock(this, ebnfRoot, alts);
				break;
			default :
				c = new LL1Choice(this, blkAST, alts);
				break;
		}
		return c;
	}


	public abstract void defineBitSet(BitSetDecl b);

	public OutputModelObject getLL1Test(IntervalSet look, GrammarAST blkAST) {
		OutputModelObject expr;
		if ( look.size() < gen.target.getInlineTestsVsBitsetThreshold() ) {
			expr = new TestSetInline(this, blkAST, look);
		}
		else {
			expr = new TestSet(this, blkAST, look);
		}
		return expr;
	}

	public DFADecl defineDFA(GrammarAST ast, DFA dfa) {
		return null;
//		DFADef d = new DFADef(name, dfa);
//		outputModel.dfaDefs.add(d);
	}

	public BitSetDecl createFollowBitSet(GrammarAST ast, IntervalSet set) {
		String inRuleName = ast.nfaState.rule.name;
		String elementName = ast.getText(); // assume rule ref
		if ( ast.getType() == ANTLRParser.STRING_LITERAL ) {
			elementName = gen.target.getTokenTypeAsTargetLabel(g, g.stringLiteralToTypeMap.get(elementName));
		}
		String name = "FOLLOW_"+elementName+"_in_"+inRuleName+"_"+ast.token.getTokenIndex();
		BitSetDecl b = new BitSetDecl(this, name, set);
		return b;
	}

	public BitSetDecl createExpectingBitSet(GrammarAST ast, int decision, IntervalSet set, String position) {
		String inRuleName = ast.nfaState.rule.name;
		String name = "EXPECTING_in_"+inRuleName+"_"+position+"_"+decision;
		//System.out.println("!!!!!!!! create "+name);
		BitSetDecl b = new BitSetDecl(this, name, set);
		return b;
	}

	public BitSetDecl createTestBitSet(GrammarAST ast, IntervalSet set) {
		String inRuleName = ast.nfaState.rule.name;
		String name = "LOOK_in_"+inRuleName+"_"+ast.token.getTokenIndex();
		BitSetDecl b = new BitSetDecl(this, name, set);
		return b;
	}
}
