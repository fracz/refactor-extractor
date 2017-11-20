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

package org.antlr.v4.semantics;

import org.antlr.runtime.tree.TreeNodeStream;
import org.antlr.v4.parse.GrammarTreeVisitor;
import org.antlr.v4.tool.GrammarAST;

import java.util.*;

public class RewriteRefs extends GrammarTreeVisitor {
	List<GrammarAST> shallow = new ArrayList<GrammarAST>();
	List<GrammarAST> deep = new ArrayList<GrammarAST>();
	public int desiredShallowLevel;

	public RewriteRefs(TreeNodeStream input, int desiredShallowLevel) {
		super(input);
		this.desiredShallowLevel = desiredShallowLevel;
	}

	public void track(GrammarAST t) {
		deep.add(t);
		if ( rewriteEBNFLevel==desiredShallowLevel ) shallow.add(t);
	}

	@Override
	public void rewriteTokenRef(GrammarAST ast, GrammarAST options, GrammarAST arg) {
		track(ast);
	}

	@Override
	public void rewriteStringRef(GrammarAST ast, GrammarAST options) {
		track(ast);
	}

	@Override
	public void rewriteRuleRef(GrammarAST ast) {
		track(ast);
	}

	@Override
	public void rewriteLabelRef(GrammarAST ast) {
		track(ast);
	}
}