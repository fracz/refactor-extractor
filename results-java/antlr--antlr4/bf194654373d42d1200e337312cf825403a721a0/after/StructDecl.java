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

package org.antlr.v4.codegen.model.decl;

import org.antlr.v4.codegen.OutputModelFactory;
import org.antlr.v4.codegen.model.ModelElement;
import org.antlr.v4.codegen.model.SwitchedVisitorDispatchMethod;
import org.antlr.v4.codegen.model.VisitorDispatchMethod;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import org.antlr.v4.tool.Attribute;
import org.antlr.v4.tool.Rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** */
public class StructDecl extends Decl {
	@ModelElement public OrderedHashSet<Decl> attrs = new OrderedHashSet<Decl>();
	@ModelElement public Collection<Attribute> ctorAttrs;
	@ModelElement public List<VisitorDispatchMethod> visitorDispatchMethods;

	public StructDecl(OutputModelFactory factory, Rule r) {
		super(factory, null);
		List<String> labels = r.getAltLabels();
		boolean multiAlts = labels!=null && labels.size()>1;
		visitorDispatchMethods = new ArrayList<VisitorDispatchMethod>();
		VisitorDispatchMethod enter = multiAlts ?
			new SwitchedVisitorDispatchMethod(factory, r, true) :
			new VisitorDispatchMethod(factory, r, true);
		visitorDispatchMethods.add(enter);
		VisitorDispatchMethod exit = multiAlts ?
			new SwitchedVisitorDispatchMethod(factory, r, false) :
			new VisitorDispatchMethod(factory, r, false);
		visitorDispatchMethods.add(exit);
	}

	public void addDecl(Decl d) { attrs.add(d); }

	public void addDecl(Attribute a) {
		addDecl(new AttributeDecl(factory, a.name, a.decl));
	}

	public void addDecls(Collection<Attribute> attrList) {
		for (Attribute a : attrList) addDecl(a);
	}

	public boolean isEmpty() { return attrs.size()==0; }
}