/*
 [The "BSD license"]
  Copyright (c) 2011 Terence Parr
  All rights reserved.

  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions
  are met:

  1. Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
  2. Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
  3. The name of the author may not be used to endorse or promote products
     derived from this software without specific prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.antlr.v4.runtime;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.misc.IntervalSet;

/** This is the default error handling mechanism for ANTLR parsers
 *  and tree parsers.
 */
public class DefaultANTLRErrorStrategy implements ANTLRErrorStrategy {
	/** This is true after we see an error and before having successfully
	 *  matched a token. Prevents generation of more than one error message
	 *  per error.
	 */
	protected boolean errorRecoveryMode = false;

	/** The index into the input stream where the last error occurred.
	 * 	This is used to prevent infinite loops where an error is found
	 *  but no token is consumed during recovery...another error is found,
	 *  ad naseum.  This is a failsafe mechanism to guarantee that at least
	 *  one token/tree node is consumed for two errors.
	 */
	protected int lastErrorIndex = -1;

	protected IntervalSet lastErrorStates;

	@Override
	public void beginErrorCondition(BaseRecognizer recognizer) {
		errorRecoveryMode = true;
	}

	@Override
	public boolean inErrorRecoveryMode(BaseRecognizer recognizer) {
		return errorRecoveryMode;
	}

	@Override
	public void endErrorCondition(BaseRecognizer recognizer) {
		errorRecoveryMode = false;
		lastErrorStates = null;
		lastErrorIndex = -1;
	}

	@Override
	public void reportError(BaseRecognizer recognizer,
							RecognitionException e)
		throws RecognitionException
	{
		// if we've already reported an error and have not matched a token
		// yet successfully, don't report any errors.
		if (errorRecoveryMode) {
//			System.err.print("[SPURIOUS] ");
			return; // don't count spurious errors
		}
		recognizer.syntaxErrors++;
		beginErrorCondition(recognizer);
		if ( e instanceof NoViableAltException ) {
			reportNoViableAlternative(recognizer, (NoViableAltException) e);
		}
		else if ( e instanceof InputMismatchException ) {
			reportInputMismatch(recognizer, (InputMismatchException)e);
		}
		else if ( e instanceof FailedPredicateException ) {
			reportFailedPredicate(recognizer, (FailedPredicateException)e);
		}
		else {
			System.err.println("unknown recognition error type: "+e.getClass().getName());
			if ( recognizer!=null ) {
				recognizer.notifyListeners(e.offendingToken, e.getMessage(), e);
			}
		}
	}

	/** Recover from NoViableAlt errors. Also there could be a mismatched
	 *  token that the match() routine could not recover from.
	 */
	@Override
	public void recover(BaseRecognizer recognizer) {
//		System.out.println("recover in "+recognizer.getRuleInvocationStack()+
//						   " index="+recognizer.getInputStream().index()+
//						   ", lastErrorIndex="+
//						   lastErrorIndex+
//						   ", states="+lastErrorStates);
		if ( lastErrorIndex==recognizer.getInputStream().index() &&
		lastErrorStates.contains(recognizer._ctx.s) ) {
			// uh oh, another error at same token index and previously-visited
			// state in ATN; must be a case where LT(1) is in the recovery
			// token set so nothing got consumed. Consume a single token
			// at least to prevent an infinite loop; this is a failsafe.
//			System.err.println("seen error condition before index="+
//							   lastErrorIndex+", states="+lastErrorStates);
//			System.err.println("FAILSAFE consumes "+recognizer.getTokenNames()[recognizer.getInputStream().LA(1)]);
			recognizer.consume();
		}
		lastErrorIndex = recognizer.getInputStream().index();
		if ( lastErrorStates==null ) lastErrorStates = new IntervalSet();
		lastErrorStates.add(recognizer._ctx.s);
		IntervalSet followSet = getErrorRecoverySet(recognizer);
		consumeUntil(recognizer, followSet);
	}

	/** Make sure that the current lookahead symbol is consistent with
	 *  what were expecting at this point in the ATN.
	 *  sync() differs fundamentally from the recoverInline() method.
	 *  In this case, we throw out a token that's not in the set of what
	 *  were expecting at this point. recoverInline() only deletes this
	 *  token if LT(2) (token after the current token) is what were expecting;
	 *  i.e., we have an extra token sitting on the input stream.  sync()
	 *  simply consumes until it finds something that can start whatever
	 *  follows the call to sync().
 	 */
	@Override
	public void sync(BaseRecognizer recognizer) {
		// If already recovering, don't try to sync
		if ( errorRecoveryMode ) return;
		// TODO: CACHE THESE RESULTS!!
		IntervalSet expecting = getExpectedTokens(recognizer);
		// TODO: subclass this class for treeparsers
		TokenStream tokens = (TokenStream)recognizer.getInputStream();
		Token la = tokens.LT(1);
		// Return but don't end recovery. only do that upon valid token match
		if ( la.getType()==Token.EOF || expecting.contains(la.getType()) ) return;
		reportUnwantedToken(recognizer);
		consumeUntil(recognizer, expecting);
	}

	public void reportNoViableAlternative(BaseRecognizer recognizer,
										  NoViableAltException e)
		throws RecognitionException
	{
		// TODO: subclass this class for treeparsers
		TokenStream tokens = (TokenStream)recognizer.getInputStream();
		String input = tokens.toString(e.startToken, e.offendingToken);
		String msg = "no viable alternative at input "+escapeWSAndQuote(input);
		recognizer.notifyListeners(e.offendingToken, msg, e);
	}

	public void reportInputMismatch(BaseRecognizer recognizer,
									InputMismatchException e)
		throws RecognitionException
	{
		String msg = "mismatched input "+getTokenErrorDisplay(e.offendingToken)+
		" expecting "+e.getExpectedTokens().toString(recognizer.getTokenNames());
		recognizer.notifyListeners(e.offendingToken, msg, e);
	}

	public void reportFailedPredicate(BaseRecognizer recognizer,
									  FailedPredicateException e)
		throws RecognitionException
	{
		String ruleName = recognizer.getRuleNames()[recognizer._ctx.getRuleIndex()];
		String msg = "rule "+ruleName+" failed predicate: {"+
		e.predicateText+"}?";
		recognizer.notifyListeners(e.offendingToken, msg, e);
	}

	public void reportUnwantedToken(BaseRecognizer recognizer) {
		if (errorRecoveryMode) return;
		recognizer.syntaxErrors++;
		beginErrorCondition(recognizer);

		Token t = (Token)recognizer.getCurrentInputSymbol();
		String tokenName = getTokenErrorDisplay(t);
		IntervalSet expecting = getExpectedTokens(recognizer);
		String msg = "extraneous input "+tokenName+" expecting "+
			expecting.toString(recognizer.getTokenNames());
		recognizer.notifyListeners(t, msg, null);
	}

	public void reportMissingToken(BaseRecognizer recognizer) {
		if (errorRecoveryMode) return;
		recognizer.syntaxErrors++;
		beginErrorCondition(recognizer);

		Token t = (Token)recognizer.getCurrentInputSymbol();
		IntervalSet expecting = getExpectedTokens(recognizer);
		String msg = "missing "+expecting.toString(recognizer.getTokenNames())+
			" at "+getTokenErrorDisplay(t);

		recognizer.notifyListeners(t, msg, null);
	}

	/** Attempt to recover from a single missing or extra token.
	 *
	 *  EXTRA TOKEN
	 *
	 *  LA(1) is not what we are looking for.  If LA(2) has the right token,
	 *  however, then assume LA(1) is some extra spurious token.  Delete it
	 *  and LA(2) as if we were doing a normal match(), which advances the
	 *  input.
	 *
	 *  MISSING TOKEN
	 *
	 *  If current token is consistent with what could come after
	 *  ttype then it is ok to "insert" the missing token, else throw
	 *  exception For example, Input "i=(3;" is clearly missing the
	 *  ')'.  When the parser returns from the nested call to expr, it
	 *  will have call chain:
	 *
	 *    stat -> expr -> atom
	 *
	 *  and it will be trying to match the ')' at this point in the
	 *  derivation:
	 *
	 *       => ID '=' '(' INT ')' ('+' atom)* ';'
	 *                          ^
	 *  match() will see that ';' doesn't match ')' and report a
	 *  mismatched token error.  To recover, it sees that LA(1)==';'
	 *  is in the set of tokens that can follow the ')' token
	 *  reference in rule atom.  It can assume that you forgot the ')'.
	 */
	@Override
	public Object recoverInline(BaseRecognizer recognizer)
		throws RecognitionException
	{
		Object currentSymbol = recognizer.getCurrentInputSymbol();

		// SINGLE TOKEN DELETION
		// if next token is what we are looking for then "delete" this token
		int nextTokenType = recognizer.getInputStream().LA(2);
		IntervalSet expecting = getExpectedTokens(recognizer);
		if ( expecting.contains(nextTokenType) ) {
			reportUnwantedToken(recognizer);
			/*
			System.err.println("recoverFromMismatchedToken deleting "+
							   ((TokenStream)recognizer.getInputStream()).LT(1)+
							   " since "+((TokenStream)recognizer.getInputStream()).LT(2)+
							   " is what we want");
			*/
			recognizer.consume(); // simply delete extra token
//			recognizer.getInputStream().consume(); // simply delete extra token
			// we want to return the token we're actually matching
			Object matchedSymbol = recognizer.getCurrentInputSymbol();
			endErrorCondition(recognizer);  // we know next token is correct
			recognizer.consume(); // move past ttype token as if all were ok
//			recognizer.getInputStream().consume(); // move past ttype token as if all were ok
			return matchedSymbol;
		}

		// SINGLE TOKEN INSERTION
		// if current token is consistent with what could come after current
		// ATN state, then we know we're missing a token; error recovery
		// is free to conjure up and insert the missing token
		ATNState currentState = recognizer._interp.atn.states.get(recognizer._ctx.s);
		ATNState next = currentState.transition(0).target;
		IntervalSet expectingAtLL2 = recognizer._interp.atn.nextTokens(next, recognizer._ctx);
//		System.out.println("LT(2) set="+expectingAtLL2.toString(recognizer.getTokenNames()));
		if ( expectingAtLL2.contains(((Token)currentSymbol).getType()) ) {
			reportMissingToken(recognizer);
			return getMissingSymbol(recognizer);
		}
		// even that didn't work; must throw the exception
		throw new InputMismatchException(recognizer);
	}

	/** Conjure up a missing token during error recovery.
	 *
	 *  The recognizer attempts to recover from single missing
	 *  symbols. But, actions might refer to that missing symbol.
	 *  For example, x=ID {f($x);}. The action clearly assumes
	 *  that there has been an identifier matched previously and that
	 *  $x points at that token. If that token is missing, but
	 *  the next token in the stream is what we want we assume that
	 *  this token is missing and we keep going. Because we
	 *  have to return some token to replace the missing token,
	 *  we have to conjure one up. This method gives the user control
	 *  over the tokens returned for missing tokens. Mostly,
	 *  you will want to create something special for identifier
	 *  tokens. For literals such as '{' and ',', the default
	 *  action in the parser or tree parser works. It simply creates
	 *  a CommonToken of the appropriate type. The text will be the token.
	 *  If you change what tokens must be created by the lexer,
	 *  override this method to create the appropriate tokens.
	 */
	protected Object getMissingSymbol(BaseRecognizer recognizer) {
		IntervalSet expecting = getExpectedTokens(recognizer);
		int expectedTokenType = expecting.getMinElement(); // get any element
		String tokenText = null;
		if ( expectedTokenType== Token.EOF ) tokenText = "<missing EOF>";
		else tokenText = "<missing "+recognizer.getTokenNames()[expectedTokenType]+">";
		CommonToken t = new CommonToken(expectedTokenType, tokenText);
		Token current = (Token)recognizer.getCurrentInputSymbol();
		if ( current.getType() == Token.EOF ) {
			current = ((TokenStream)recognizer.getInputStream()).LT(-1);
		}
		t.line = current.getLine();
		t.charPositionInLine = current.getCharPositionInLine();
		t.channel = Token.DEFAULT_CHANNEL;
		t.source = current.getTokenSource();
		return t;
	}

	public IntervalSet getExpectedTokens(BaseRecognizer recognizer) {
		return recognizer.getExpectedTokens();
	}

	/** How should a token be displayed in an error message? The default
	 *  is to display just the text, but during development you might
	 *  want to have a lot of information spit out.  Override in that case
	 *  to use t.toString() (which, for CommonToken, dumps everything about
	 *  the token). This is better than forcing you to override a method in
	 *  your token objects because you don't have to go modify your lexer
	 *  so that it creates a new Java type.
	 */
	public String getTokenErrorDisplay(Token t) {
		if ( t==null ) return "<no token>";
		String s = t.getText();
		if ( s==null ) {
			if ( t.getType()==Token.EOF ) {
				s = "<EOF>";
			}
			else {
				s = "<"+t.getType()+">";
			}
		}
		return escapeWSAndQuote(s);
	}

	protected String escapeWSAndQuote(String s) {
		s = s.replaceAll("\n","\\\\n");
		s = s.replaceAll("\r","\\\\r");
		s = s.replaceAll("\t","\\\\t");
		return "'"+s+"'";
	}

	/*  Compute the error recovery set for the current rule.  During
	 *  rule invocation, the parser pushes the set of tokens that can
	 *  follow that rule reference on the stack; this amounts to
	 *  computing FIRST of what follows the rule reference in the
	 *  enclosing rule. See LinearApproximator.FIRST().
	 *  This local follow set only includes tokens
	 *  from within the rule; i.e., the FIRST computation done by
	 *  ANTLR stops at the end of a rule.
	 *
	 *  EXAMPLE
	 *
	 *  When you find a "no viable alt exception", the input is not
	 *  consistent with any of the alternatives for rule r.  The best
	 *  thing to do is to consume tokens until you see something that
	 *  can legally follow a call to r *or* any rule that called r.
	 *  You don't want the exact set of viable next tokens because the
	 *  input might just be missing a token--you might consume the
	 *  rest of the input looking for one of the missing tokens.
	 *
	 *  Consider grammar:
	 *
	 *  a : '[' b ']'
	 *    | '(' b ')'
	 *    ;
	 *  b : c '^' INT ;
	 *  c : ID
	 *    | INT
	 *    ;
	 *
	 *  At each rule invocation, the set of tokens that could follow
	 *  that rule is pushed on a stack.  Here are the various
	 *  context-sensitive follow sets:
	 *
	 *  FOLLOW(b1_in_a) = FIRST(']') = ']'
	 *  FOLLOW(b2_in_a) = FIRST(')') = ')'
	 *  FOLLOW(c_in_b) = FIRST('^') = '^'
	 *
	 *  Upon erroneous input "[]", the call chain is
	 *
	 *  a -> b -> c
	 *
	 *  and, hence, the follow context stack is:
	 *
	 *  depth     follow set       start of rule execution
	 *    0         <EOF>                    a (from main())
	 *    1          ']'                     b
	 *    2          '^'                     c
	 *
	 *  Notice that ')' is not included, because b would have to have
	 *  been called from a different context in rule a for ')' to be
	 *  included.
	 *
	 *  For error recovery, we cannot consider FOLLOW(c)
	 *  (context-sensitive or otherwise).  We need the combined set of
	 *  all context-sensitive FOLLOW sets--the set of all tokens that
	 *  could follow any reference in the call chain.  We need to
	 *  resync to one of those tokens.  Note that FOLLOW(c)='^' and if
	 *  we resync'd to that token, we'd consume until EOF.  We need to
	 *  sync to context-sensitive FOLLOWs for a, b, and c: {']','^'}.
	 *  In this case, for input "[]", LA(1) is ']' and in the set, so we would
	 *  not consume anything. After printing an error, rule c would
	 *  return normally.  Rule b would not find the required '^' though.
	 *  At this point, it gets a mismatched token error and throws an
	 *  exception (since LA(1) is not in the viable following token
	 *  set).  The rule exception handler tries to recover, but finds
	 *  the same recovery set and doesn't consume anything.  Rule b
	 *  exits normally returning to rule a.  Now it finds the ']' (and
	 *  with the successful match exits errorRecovery mode).
	 *
	 *  So, you can see that the parser walks up the call chain looking
	 *  for the token that was a member of the recovery set.
	 *
	 *  Errors are not generated in errorRecovery mode.
	 *
	 *  ANTLR's error recovery mechanism is based upon original ideas:
	 *
	 *  "Algorithms + Data Structures = Programs" by Niklaus Wirth
	 *
	 *  and
	 *
	 *  "A note on error recovery in recursive descent parsers":
	 *  http://portal.acm.org/citation.cfm?id=947902.947905
	 *
	 *  Later, Josef Grosch had some good ideas:
	 *
	 *  "Efficient and Comfortable Error Recovery in Recursive Descent
	 *  Parsers":
	 *  ftp://www.cocolab.com/products/cocktail/doca4.ps/ell.ps.zip
	 *
	 *  Like Grosch I implement context-sensitive FOLLOW sets that are combined
	 *  at run-time upon error to avoid overhead during parsing.
	 */
	protected IntervalSet getErrorRecoverySet(BaseRecognizer recognizer) {
		ATN atn = recognizer._interp.atn;
		RuleContext ctx = recognizer._ctx;
		IntervalSet recoverSet = new IntervalSet();
		while ( ctx!=null && ctx.invokingState>=0 ) {
			// compute what follows who invoked us
			ATNState invokingState = atn.states.get(ctx.invokingState);
			RuleTransition rt = (RuleTransition)invokingState.transition(0);
			IntervalSet follow = atn.nextTokens(rt.followState, null);
			recoverSet.addAll(follow);
			ctx = ctx.parent;
		}
//		System.out.println("recover set "+recoverSet.toString(recognizer.getTokenNames()));
		return recoverSet;
	}

	/** Consume tokens until one matches the given token set */
	public void consumeUntil(BaseRecognizer recognizer, IntervalSet set) {
//		System.out.println("consumeUntil("+set.toString(recognizer.getTokenNames())+")");
		int ttype = recognizer.getInputStream().LA(1);
		while (ttype != Token.EOF && !set.contains(ttype) ) {
			//System.out.println("consume during recover LA(1)="+getTokenNames()[input.LA(1)]);
//			recognizer.getInputStream().consume();
			recognizer.consume();
			ttype = recognizer.getInputStream().LA(1);
		}
	}
}