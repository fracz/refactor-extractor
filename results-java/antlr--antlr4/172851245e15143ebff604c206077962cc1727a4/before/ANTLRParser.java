// $ANTLR 3.2.1-SNAPSHOT Jan 26, 2010 15:12:28 ANTLRParser.g 2010-02-11 11:14:47

/*
 [The "BSD licence"]
 Copyright (c) 2005-2009 Terence Parr
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
package org.antlr.v4.parse;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.v4.tool.*;

import java.util.ArrayList;
import java.util.List;

/** The definitive ANTLR v3 grammar to parse ANTLR v4 grammars.
 *  The grammar builds ASTs that are sniffed by subsequent stages.
 */
public class ANTLRParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "SEMPRED", "FORCED_ACTION", "DOC_COMMENT", "SRC", "NLCHARS", "COMMENT", "DOUBLE_QUOTE_STRING_LITERAL", "DOUBLE_ANGLE_STRING_LITERAL", "ACTION_STRING_LITERAL", "ACTION_CHAR_LITERAL", "ARG_ACTION", "NESTED_ACTION", "ACTION", "ACTION_ESC", "WSNLCHARS", "OPTIONS", "TOKENS", "SCOPE", "IMPORT", "FRAGMENT", "LEXER", "PARSER", "TREE", "GRAMMAR", "PROTECTED", "PUBLIC", "PRIVATE", "RETURNS", "THROWS", "CATCH", "FINALLY", "TEMPLATE", "COLON", "COLONCOLON", "COMMA", "SEMI", "LPAREN", "RPAREN", "IMPLIES", "LT", "GT", "ASSIGN", "QUESTION", "BANG", "STAR", "PLUS", "PLUS_ASSIGN", "OR", "ROOT", "DOLLAR", "DOT", "RANGE", "ETC", "RARROW", "TREE_BEGIN", "AT", "NOT", "RBRACE", "TOKEN_REF", "RULE_REF", "INT", "WSCHARS", "ESC_SEQ", "STRING_LITERAL", "HEX_DIGIT", "UNICODE_ESC", "WS", "ERRCHAR", "RULE", "RULES", "RULEMODIFIERS", "RULEACTIONS", "BLOCK", "REWRITE_BLOCK", "OPTIONAL", "CLOSURE", "POSITIVE_CLOSURE", "SYNPRED", "CHAR_RANGE", "EPSILON", "ALT", "ALTLIST", "RESULT", "ID", "ARG", "ARGLIST", "RET", "COMBINED", "INITACTION", "LABEL", "GATED_SEMPRED", "SYN_SEMPRED", "BACKTRACK_SEMPRED", "WILDCARD", "LIST", "ELEMENT_OPTIONS", "ST_RESULT", "ALT_REWRITE"
    };
    public static final int LT=43;
    public static final int COMBINED=91;
    public static final int STAR=48;
    public static final int BACKTRACK_SEMPRED=96;
    public static final int DOUBLE_ANGLE_STRING_LITERAL=11;
    public static final int FORCED_ACTION=5;
    public static final int ARGLIST=89;
    public static final int ALTLIST=85;
    public static final int NOT=60;
    public static final int EOF=-1;
    public static final int SEMPRED=4;
    public static final int ACTION=16;
    public static final int TOKEN_REF=62;
    public static final int RULEMODIFIERS=74;
    public static final int ST_RESULT=100;
    public static final int RPAREN=41;
    public static final int RET=90;
    public static final int IMPORT=22;
    public static final int STRING_LITERAL=67;
    public static final int ARG=88;
    public static final int ARG_ACTION=14;
    public static final int DOUBLE_QUOTE_STRING_LITERAL=10;
    public static final int COMMENT=9;
    public static final int GRAMMAR=27;
    public static final int ACTION_CHAR_LITERAL=13;
    public static final int WSCHARS=65;
    public static final int RULEACTIONS=75;
    public static final int INITACTION=92;
    public static final int ALT_REWRITE=101;
    public static final int IMPLIES=42;
    public static final int RBRACE=61;
    public static final int RULE=72;
    public static final int ACTION_ESC=17;
    public static final int PRIVATE=30;
    public static final int SRC=7;
    public static final int THROWS=32;
    public static final int INT=64;
    public static final int CHAR_RANGE=82;
    public static final int EPSILON=83;
    public static final int LIST=98;
    public static final int COLONCOLON=37;
    public static final int WSNLCHARS=18;
    public static final int WS=70;
    public static final int LEXER=24;
    public static final int OR=51;
    public static final int GT=44;
    public static final int CATCH=33;
    public static final int CLOSURE=79;
    public static final int PARSER=25;
    public static final int DOLLAR=53;
    public static final int PROTECTED=28;
    public static final int ELEMENT_OPTIONS=99;
    public static final int NESTED_ACTION=15;
    public static final int FRAGMENT=23;
    public static final int ID=87;
    public static final int TREE_BEGIN=58;
    public static final int LPAREN=40;
    public static final int AT=59;
    public static final int ESC_SEQ=66;
    public static final int ALT=84;
    public static final int TREE=26;
    public static final int SCOPE=21;
    public static final int ETC=56;
    public static final int COMMA=38;
    public static final int WILDCARD=97;
    public static final int DOC_COMMENT=6;
    public static final int PLUS=49;
    public static final int REWRITE_BLOCK=77;
    public static final int DOT=54;
    public static final int RETURNS=31;
    public static final int RULES=73;
    public static final int RARROW=57;
    public static final int UNICODE_ESC=69;
    public static final int HEX_DIGIT=68;
    public static final int RANGE=55;
    public static final int TOKENS=20;
    public static final int GATED_SEMPRED=94;
    public static final int RESULT=86;
    public static final int BANG=47;
    public static final int ACTION_STRING_LITERAL=12;
    public static final int ROOT=52;
    public static final int SEMI=39;
    public static final int RULE_REF=63;
    public static final int NLCHARS=8;
    public static final int OPTIONAL=78;
    public static final int SYNPRED=81;
    public static final int COLON=36;
    public static final int QUESTION=46;
    public static final int FINALLY=34;
    public static final int LABEL=93;
    public static final int TEMPLATE=35;
    public static final int SYN_SEMPRED=95;
    public static final int ERRCHAR=71;
    public static final int BLOCK=76;
    public static final int PLUS_ASSIGN=50;
    public static final int ASSIGN=45;
    public static final int PUBLIC=29;
    public static final int POSITIVE_CLOSURE=80;
    public static final int OPTIONS=19;

    // delegates
    // delegators


        public ANTLRParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public ANTLRParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);

        }

    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return ANTLRParser.tokenNames; }
    public String getGrammarFileName() { return "ANTLRParser.g"; }


    public static class grammarSpec_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "grammarSpec"
    // ANTLRParser.g:138:1: grammarSpec : ( DOC_COMMENT )? grammarType id SEMI ( prequelConstruct )* rules EOF -> ^( grammarType id ( DOC_COMMENT )? ( prequelConstruct )* rules ) ;
    public final ANTLRParser.grammarSpec_return grammarSpec() throws RecognitionException {
        ANTLRParser.grammarSpec_return retval = new ANTLRParser.grammarSpec_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token DOC_COMMENT1=null;
        Token SEMI4=null;
        Token EOF7=null;
        ANTLRParser.grammarType_return grammarType2 = null;

        ANTLRParser.id_return id3 = null;

        ANTLRParser.prequelConstruct_return prequelConstruct5 = null;

        ANTLRParser.rules_return rules6 = null;


        GrammarAST DOC_COMMENT1_tree=null;
        GrammarAST SEMI4_tree=null;
        GrammarAST EOF7_tree=null;
        RewriteRuleTokenStream stream_DOC_COMMENT=new RewriteRuleTokenStream(adaptor,"token DOC_COMMENT");
        RewriteRuleTokenStream stream_EOF=new RewriteRuleTokenStream(adaptor,"token EOF");
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");
        RewriteRuleSubtreeStream stream_prequelConstruct=new RewriteRuleSubtreeStream(adaptor,"rule prequelConstruct");
        RewriteRuleSubtreeStream stream_grammarType=new RewriteRuleSubtreeStream(adaptor,"rule grammarType");
        RewriteRuleSubtreeStream stream_rules=new RewriteRuleSubtreeStream(adaptor,"rule rules");
        try {
            // ANTLRParser.g:139:5: ( ( DOC_COMMENT )? grammarType id SEMI ( prequelConstruct )* rules EOF -> ^( grammarType id ( DOC_COMMENT )? ( prequelConstruct )* rules ) )
            // ANTLRParser.g:143:7: ( DOC_COMMENT )? grammarType id SEMI ( prequelConstruct )* rules EOF
            {
            // ANTLRParser.g:143:7: ( DOC_COMMENT )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==DOC_COMMENT) ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // ANTLRParser.g:143:7: DOC_COMMENT
                    {
                    DOC_COMMENT1=(Token)match(input,DOC_COMMENT,FOLLOW_DOC_COMMENT_in_grammarSpec459); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_DOC_COMMENT.add(DOC_COMMENT1);


                    }
                    break;

            }

            pushFollow(FOLLOW_grammarType_in_grammarSpec496);
            grammarType2=grammarType();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_grammarType.add(grammarType2.getTree());
            pushFollow(FOLLOW_id_in_grammarSpec498);
            id3=id();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_id.add(id3.getTree());
            SEMI4=(Token)match(input,SEMI,FOLLOW_SEMI_in_grammarSpec500); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_SEMI.add(SEMI4);

            // ANTLRParser.g:161:7: ( prequelConstruct )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>=OPTIONS && LA2_0<=IMPORT)||LA2_0==AT) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // ANTLRParser.g:161:7: prequelConstruct
            	    {
            	    pushFollow(FOLLOW_prequelConstruct_in_grammarSpec544);
            	    prequelConstruct5=prequelConstruct();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_prequelConstruct.add(prequelConstruct5.getTree());

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            pushFollow(FOLLOW_rules_in_grammarSpec572);
            rules6=rules();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rules.add(rules6.getTree());
            EOF7=(Token)match(input,EOF,FOLLOW_EOF_in_grammarSpec615); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_EOF.add(EOF7);



            // AST REWRITE
            // elements: DOC_COMMENT, id, grammarType, rules, prequelConstruct
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 180:7: -> ^( grammarType id ( DOC_COMMENT )? ( prequelConstruct )* rules )
            {
                // ANTLRParser.g:180:10: ^( grammarType id ( DOC_COMMENT )? ( prequelConstruct )* rules )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot(stream_grammarType.nextNode(), root_1);

                adaptor.addChild(root_1, stream_id.nextTree());
                // ANTLRParser.g:182:14: ( DOC_COMMENT )?
                if ( stream_DOC_COMMENT.hasNext() ) {
                    adaptor.addChild(root_1, stream_DOC_COMMENT.nextNode());

                }
                stream_DOC_COMMENT.reset();
                // ANTLRParser.g:183:14: ( prequelConstruct )*
                while ( stream_prequelConstruct.hasNext() ) {
                    adaptor.addChild(root_1, stream_prequelConstruct.nextTree());

                }
                stream_prequelConstruct.reset();
                adaptor.addChild(root_1, stream_rules.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "grammarSpec"

    public static class grammarType_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "grammarType"
    // ANTLRParser.g:216:1: grammarType : (t= LEXER g= GRAMMAR -> GRAMMAR[$g, \"LEXER_GRAMMAR\"] | t= PARSER g= GRAMMAR -> GRAMMAR[$g, \"PARSER_GRAMMAR\"] | t= TREE g= GRAMMAR -> GRAMMAR[$g, \"TREE_GRAMMAR\"] | g= GRAMMAR -> GRAMMAR[$g, \"COMBINED_GRAMMAR\"] ) ;
    public final ANTLRParser.grammarType_return grammarType() throws RecognitionException {
        ANTLRParser.grammarType_return retval = new ANTLRParser.grammarType_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token t=null;
        Token g=null;

        GrammarAST t_tree=null;
        GrammarAST g_tree=null;
        RewriteRuleTokenStream stream_TREE=new RewriteRuleTokenStream(adaptor,"token TREE");
        RewriteRuleTokenStream stream_PARSER=new RewriteRuleTokenStream(adaptor,"token PARSER");
        RewriteRuleTokenStream stream_LEXER=new RewriteRuleTokenStream(adaptor,"token LEXER");
        RewriteRuleTokenStream stream_GRAMMAR=new RewriteRuleTokenStream(adaptor,"token GRAMMAR");

        try {
            // ANTLRParser.g:221:5: ( (t= LEXER g= GRAMMAR -> GRAMMAR[$g, \"LEXER_GRAMMAR\"] | t= PARSER g= GRAMMAR -> GRAMMAR[$g, \"PARSER_GRAMMAR\"] | t= TREE g= GRAMMAR -> GRAMMAR[$g, \"TREE_GRAMMAR\"] | g= GRAMMAR -> GRAMMAR[$g, \"COMBINED_GRAMMAR\"] ) )
            // ANTLRParser.g:221:7: (t= LEXER g= GRAMMAR -> GRAMMAR[$g, \"LEXER_GRAMMAR\"] | t= PARSER g= GRAMMAR -> GRAMMAR[$g, \"PARSER_GRAMMAR\"] | t= TREE g= GRAMMAR -> GRAMMAR[$g, \"TREE_GRAMMAR\"] | g= GRAMMAR -> GRAMMAR[$g, \"COMBINED_GRAMMAR\"] )
            {
            // ANTLRParser.g:221:7: (t= LEXER g= GRAMMAR -> GRAMMAR[$g, \"LEXER_GRAMMAR\"] | t= PARSER g= GRAMMAR -> GRAMMAR[$g, \"PARSER_GRAMMAR\"] | t= TREE g= GRAMMAR -> GRAMMAR[$g, \"TREE_GRAMMAR\"] | g= GRAMMAR -> GRAMMAR[$g, \"COMBINED_GRAMMAR\"] )
            int alt3=4;
            switch ( input.LA(1) ) {
            case LEXER:
                {
                alt3=1;
                }
                break;
            case PARSER:
                {
                alt3=2;
                }
                break;
            case TREE:
                {
                alt3=3;
                }
                break;
            case GRAMMAR:
                {
                alt3=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }

            switch (alt3) {
                case 1 :
                    // ANTLRParser.g:221:9: t= LEXER g= GRAMMAR
                    {
                    t=(Token)match(input,LEXER,FOLLOW_LEXER_in_grammarType809); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_LEXER.add(t);

                    g=(Token)match(input,GRAMMAR,FOLLOW_GRAMMAR_in_grammarType813); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_GRAMMAR.add(g);



                    // AST REWRITE
                    // elements: GRAMMAR
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 221:28: -> GRAMMAR[$g, \"LEXER_GRAMMAR\"]
                    {
                        adaptor.addChild(root_0, new GrammarRootAST(GRAMMAR, g, "LEXER_GRAMMAR"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // ANTLRParser.g:223:6: t= PARSER g= GRAMMAR
                    {
                    t=(Token)match(input,PARSER,FOLLOW_PARSER_in_grammarType846); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_PARSER.add(t);

                    g=(Token)match(input,GRAMMAR,FOLLOW_GRAMMAR_in_grammarType850); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_GRAMMAR.add(g);



                    // AST REWRITE
                    // elements: GRAMMAR
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 223:25: -> GRAMMAR[$g, \"PARSER_GRAMMAR\"]
                    {
                        adaptor.addChild(root_0, new GrammarRootAST(GRAMMAR, g, "PARSER_GRAMMAR"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // ANTLRParser.g:226:6: t= TREE g= GRAMMAR
                    {
                    t=(Token)match(input,TREE,FOLLOW_TREE_in_grammarType877); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_TREE.add(t);

                    g=(Token)match(input,GRAMMAR,FOLLOW_GRAMMAR_in_grammarType881); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_GRAMMAR.add(g);



                    // AST REWRITE
                    // elements: GRAMMAR
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 226:25: -> GRAMMAR[$g, \"TREE_GRAMMAR\"]
                    {
                        adaptor.addChild(root_0, new GrammarRootAST(GRAMMAR, g, "TREE_GRAMMAR"));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // ANTLRParser.g:229:6: g= GRAMMAR
                    {
                    g=(Token)match(input,GRAMMAR,FOLLOW_GRAMMAR_in_grammarType908); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_GRAMMAR.add(g);



                    // AST REWRITE
                    // elements: GRAMMAR
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 229:25: -> GRAMMAR[$g, \"COMBINED_GRAMMAR\"]
                    {
                        adaptor.addChild(root_0, new GrammarRootAST(GRAMMAR, g, "COMBINED_GRAMMAR"));

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

              	if ( t!=null ) ((GrammarRootAST)((GrammarAST)retval.tree)).grammarType = (t!=null?t.getType():0);
              	else ((GrammarRootAST)((GrammarAST)retval.tree)).grammarType=COMBINED;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "grammarType"

    public static class prequelConstruct_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "prequelConstruct"
    // ANTLRParser.g:236:1: prequelConstruct : ( optionsSpec | delegateGrammars | tokensSpec | attrScope | action );
    public final ANTLRParser.prequelConstruct_return prequelConstruct() throws RecognitionException {
        ANTLRParser.prequelConstruct_return retval = new ANTLRParser.prequelConstruct_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        ANTLRParser.optionsSpec_return optionsSpec8 = null;

        ANTLRParser.delegateGrammars_return delegateGrammars9 = null;

        ANTLRParser.tokensSpec_return tokensSpec10 = null;

        ANTLRParser.attrScope_return attrScope11 = null;

        ANTLRParser.action_return action12 = null;



        try {
            // ANTLRParser.g:237:2: ( optionsSpec | delegateGrammars | tokensSpec | attrScope | action )
            int alt4=5;
            switch ( input.LA(1) ) {
            case OPTIONS:
                {
                alt4=1;
                }
                break;
            case IMPORT:
                {
                alt4=2;
                }
                break;
            case TOKENS:
                {
                alt4=3;
                }
                break;
            case SCOPE:
                {
                alt4=4;
                }
                break;
            case AT:
                {
                alt4=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }

            switch (alt4) {
                case 1 :
                    // ANTLRParser.g:238:4: optionsSpec
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_optionsSpec_in_prequelConstruct972);
                    optionsSpec8=optionsSpec();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, optionsSpec8.getTree());

                    }
                    break;
                case 2 :
                    // ANTLRParser.g:242:7: delegateGrammars
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_delegateGrammars_in_prequelConstruct998);
                    delegateGrammars9=delegateGrammars();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, delegateGrammars9.getTree());

                    }
                    break;
                case 3 :
                    // ANTLRParser.g:249:7: tokensSpec
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_tokensSpec_in_prequelConstruct1048);
                    tokensSpec10=tokensSpec();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, tokensSpec10.getTree());

                    }
                    break;
                case 4 :
                    // ANTLRParser.g:254:7: attrScope
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_attrScope_in_prequelConstruct1084);
                    attrScope11=attrScope();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, attrScope11.getTree());

                    }
                    break;
                case 5 :
                    // ANTLRParser.g:260:7: action
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_action_in_prequelConstruct1127);
                    action12=action();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, action12.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "prequelConstruct"

    public static class optionsSpec_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "optionsSpec"
    // ANTLRParser.g:264:1: optionsSpec : OPTIONS ( option SEMI )* RBRACE -> ^( OPTIONS[$OPTIONS, \"OPTIONS\"] ( option )+ ) ;
    public final ANTLRParser.optionsSpec_return optionsSpec() throws RecognitionException {
        ANTLRParser.optionsSpec_return retval = new ANTLRParser.optionsSpec_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token OPTIONS13=null;
        Token SEMI15=null;
        Token RBRACE16=null;
        ANTLRParser.option_return option14 = null;


        GrammarAST OPTIONS13_tree=null;
        GrammarAST SEMI15_tree=null;
        GrammarAST RBRACE16_tree=null;
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleTokenStream stream_OPTIONS=new RewriteRuleTokenStream(adaptor,"token OPTIONS");
        RewriteRuleSubtreeStream stream_option=new RewriteRuleSubtreeStream(adaptor,"rule option");
        try {
            // ANTLRParser.g:265:2: ( OPTIONS ( option SEMI )* RBRACE -> ^( OPTIONS[$OPTIONS, \"OPTIONS\"] ( option )+ ) )
            // ANTLRParser.g:265:4: OPTIONS ( option SEMI )* RBRACE
            {
            OPTIONS13=(Token)match(input,OPTIONS,FOLLOW_OPTIONS_in_optionsSpec1144); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_OPTIONS.add(OPTIONS13);

            // ANTLRParser.g:265:12: ( option SEMI )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==TEMPLATE||(LA5_0>=TOKEN_REF && LA5_0<=RULE_REF)) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // ANTLRParser.g:265:13: option SEMI
            	    {
            	    pushFollow(FOLLOW_option_in_optionsSpec1147);
            	    option14=option();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_option.add(option14.getTree());
            	    SEMI15=(Token)match(input,SEMI,FOLLOW_SEMI_in_optionsSpec1149); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_SEMI.add(SEMI15);


            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

            RBRACE16=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_optionsSpec1153); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE16);



            // AST REWRITE
            // elements: OPTIONS, option
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 265:34: -> ^( OPTIONS[$OPTIONS, \"OPTIONS\"] ( option )+ )
            {
                // ANTLRParser.g:265:37: ^( OPTIONS[$OPTIONS, \"OPTIONS\"] ( option )+ )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(OPTIONS, OPTIONS13, "OPTIONS"), root_1);

                if ( !(stream_option.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_option.hasNext() ) {
                    adaptor.addChild(root_1, stream_option.nextTree());

                }
                stream_option.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "optionsSpec"

    public static class option_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "option"
    // ANTLRParser.g:268:1: option : id ASSIGN optionValue ;
    public final ANTLRParser.option_return option() throws RecognitionException {
        ANTLRParser.option_return retval = new ANTLRParser.option_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token ASSIGN18=null;
        ANTLRParser.id_return id17 = null;

        ANTLRParser.optionValue_return optionValue19 = null;


        GrammarAST ASSIGN18_tree=null;

        try {
            // ANTLRParser.g:269:5: ( id ASSIGN optionValue )
            // ANTLRParser.g:269:9: id ASSIGN optionValue
            {
            root_0 = (GrammarAST)adaptor.nil();

            pushFollow(FOLLOW_id_in_option1190);
            id17=id();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, id17.getTree());
            ASSIGN18=(Token)match(input,ASSIGN,FOLLOW_ASSIGN_in_option1192); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ASSIGN18_tree = (GrammarAST)adaptor.create(ASSIGN18);
            root_0 = (GrammarAST)adaptor.becomeRoot(ASSIGN18_tree, root_0);
            }
            pushFollow(FOLLOW_optionValue_in_option1195);
            optionValue19=optionValue();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, optionValue19.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "option"

    public static class optionValue_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "optionValue"
    // ANTLRParser.g:277:1: optionValue : ( qid | STRING_LITERAL | INT | STAR );
    public final ANTLRParser.optionValue_return optionValue() throws RecognitionException {
        ANTLRParser.optionValue_return retval = new ANTLRParser.optionValue_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token STRING_LITERAL21=null;
        Token INT22=null;
        Token STAR23=null;
        ANTLRParser.qid_return qid20 = null;


        GrammarAST STRING_LITERAL21_tree=null;
        GrammarAST INT22_tree=null;
        GrammarAST STAR23_tree=null;

        try {
            // ANTLRParser.g:278:5: ( qid | STRING_LITERAL | INT | STAR )
            int alt6=4;
            switch ( input.LA(1) ) {
            case TEMPLATE:
            case TOKEN_REF:
            case RULE_REF:
                {
                alt6=1;
                }
                break;
            case STRING_LITERAL:
                {
                alt6=2;
                }
                break;
            case INT:
                {
                alt6=3;
                }
                break;
            case STAR:
                {
                alt6=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }

            switch (alt6) {
                case 1 :
                    // ANTLRParser.g:282:7: qid
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_qid_in_optionValue1245);
                    qid20=qid();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, qid20.getTree());

                    }
                    break;
                case 2 :
                    // ANTLRParser.g:286:7: STRING_LITERAL
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    STRING_LITERAL21=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_optionValue1269); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    STRING_LITERAL21_tree = new TerminalAST(STRING_LITERAL21) ;
                    adaptor.addChild(root_0, STRING_LITERAL21_tree);
                    }

                    }
                    break;
                case 3 :
                    // ANTLRParser.g:290:7: INT
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    INT22=(Token)match(input,INT,FOLLOW_INT_in_optionValue1295); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT22_tree = (GrammarAST)adaptor.create(INT22);
                    adaptor.addChild(root_0, INT22_tree);
                    }

                    }
                    break;
                case 4 :
                    // ANTLRParser.g:294:7: STAR
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    STAR23=(Token)match(input,STAR,FOLLOW_STAR_in_optionValue1324); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    STAR23_tree = (GrammarAST)adaptor.create(STAR23);
                    adaptor.addChild(root_0, STAR23_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "optionValue"

    public static class delegateGrammars_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "delegateGrammars"
    // ANTLRParser.g:299:1: delegateGrammars : IMPORT delegateGrammar ( COMMA delegateGrammar )* SEMI -> ^( IMPORT ( delegateGrammar )+ ) ;
    public final ANTLRParser.delegateGrammars_return delegateGrammars() throws RecognitionException {
        ANTLRParser.delegateGrammars_return retval = new ANTLRParser.delegateGrammars_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token IMPORT24=null;
        Token COMMA26=null;
        Token SEMI28=null;
        ANTLRParser.delegateGrammar_return delegateGrammar25 = null;

        ANTLRParser.delegateGrammar_return delegateGrammar27 = null;


        GrammarAST IMPORT24_tree=null;
        GrammarAST COMMA26_tree=null;
        GrammarAST SEMI28_tree=null;
        RewriteRuleTokenStream stream_IMPORT=new RewriteRuleTokenStream(adaptor,"token IMPORT");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_delegateGrammar=new RewriteRuleSubtreeStream(adaptor,"rule delegateGrammar");
        try {
            // ANTLRParser.g:300:2: ( IMPORT delegateGrammar ( COMMA delegateGrammar )* SEMI -> ^( IMPORT ( delegateGrammar )+ ) )
            // ANTLRParser.g:300:4: IMPORT delegateGrammar ( COMMA delegateGrammar )* SEMI
            {
            IMPORT24=(Token)match(input,IMPORT,FOLLOW_IMPORT_in_delegateGrammars1340); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_IMPORT.add(IMPORT24);

            pushFollow(FOLLOW_delegateGrammar_in_delegateGrammars1342);
            delegateGrammar25=delegateGrammar();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_delegateGrammar.add(delegateGrammar25.getTree());
            // ANTLRParser.g:300:27: ( COMMA delegateGrammar )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==COMMA) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // ANTLRParser.g:300:28: COMMA delegateGrammar
            	    {
            	    COMMA26=(Token)match(input,COMMA,FOLLOW_COMMA_in_delegateGrammars1345); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA26);

            	    pushFollow(FOLLOW_delegateGrammar_in_delegateGrammars1347);
            	    delegateGrammar27=delegateGrammar();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_delegateGrammar.add(delegateGrammar27.getTree());

            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);

            SEMI28=(Token)match(input,SEMI,FOLLOW_SEMI_in_delegateGrammars1351); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_SEMI.add(SEMI28);



            // AST REWRITE
            // elements: IMPORT, delegateGrammar
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 300:57: -> ^( IMPORT ( delegateGrammar )+ )
            {
                // ANTLRParser.g:300:60: ^( IMPORT ( delegateGrammar )+ )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot(stream_IMPORT.nextNode(), root_1);

                if ( !(stream_delegateGrammar.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_delegateGrammar.hasNext() ) {
                    adaptor.addChild(root_1, stream_delegateGrammar.nextTree());

                }
                stream_delegateGrammar.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "delegateGrammars"

    public static class delegateGrammar_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "delegateGrammar"
    // ANTLRParser.g:305:1: delegateGrammar : ( id ASSIGN id | id );
    public final ANTLRParser.delegateGrammar_return delegateGrammar() throws RecognitionException {
        ANTLRParser.delegateGrammar_return retval = new ANTLRParser.delegateGrammar_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token ASSIGN30=null;
        ANTLRParser.id_return id29 = null;

        ANTLRParser.id_return id31 = null;

        ANTLRParser.id_return id32 = null;


        GrammarAST ASSIGN30_tree=null;

        try {
            // ANTLRParser.g:306:5: ( id ASSIGN id | id )
            int alt8=2;
            switch ( input.LA(1) ) {
            case RULE_REF:
                {
                int LA8_1 = input.LA(2);

                if ( ((LA8_1>=COMMA && LA8_1<=SEMI)) ) {
                    alt8=2;
                }
                else if ( (LA8_1==ASSIGN) ) {
                    alt8=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 1, input);

                    throw nvae;
                }
                }
                break;
            case TOKEN_REF:
                {
                int LA8_2 = input.LA(2);

                if ( ((LA8_2>=COMMA && LA8_2<=SEMI)) ) {
                    alt8=2;
                }
                else if ( (LA8_2==ASSIGN) ) {
                    alt8=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 2, input);

                    throw nvae;
                }
                }
                break;
            case TEMPLATE:
                {
                int LA8_3 = input.LA(2);

                if ( ((LA8_3>=COMMA && LA8_3<=SEMI)) ) {
                    alt8=2;
                }
                else if ( (LA8_3==ASSIGN) ) {
                    alt8=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 3, input);

                    throw nvae;
                }
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }

            switch (alt8) {
                case 1 :
                    // ANTLRParser.g:306:9: id ASSIGN id
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_id_in_delegateGrammar1378);
                    id29=id();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, id29.getTree());
                    ASSIGN30=(Token)match(input,ASSIGN,FOLLOW_ASSIGN_in_delegateGrammar1380); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ASSIGN30_tree = (GrammarAST)adaptor.create(ASSIGN30);
                    root_0 = (GrammarAST)adaptor.becomeRoot(ASSIGN30_tree, root_0);
                    }
                    pushFollow(FOLLOW_id_in_delegateGrammar1383);
                    id31=id();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, id31.getTree());

                    }
                    break;
                case 2 :
                    // ANTLRParser.g:307:9: id
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_id_in_delegateGrammar1393);
                    id32=id();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, id32.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "delegateGrammar"

    public static class tokensSpec_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tokensSpec"
    // ANTLRParser.g:310:1: tokensSpec : TOKENS ( tokenSpec )+ RBRACE -> ^( TOKENS ( tokenSpec )+ ) ;
    public final ANTLRParser.tokensSpec_return tokensSpec() throws RecognitionException {
        ANTLRParser.tokensSpec_return retval = new ANTLRParser.tokensSpec_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token TOKENS33=null;
        Token RBRACE35=null;
        ANTLRParser.tokenSpec_return tokenSpec34 = null;


        GrammarAST TOKENS33_tree=null;
        GrammarAST RBRACE35_tree=null;
        RewriteRuleTokenStream stream_TOKENS=new RewriteRuleTokenStream(adaptor,"token TOKENS");
        RewriteRuleTokenStream stream_RBRACE=new RewriteRuleTokenStream(adaptor,"token RBRACE");
        RewriteRuleSubtreeStream stream_tokenSpec=new RewriteRuleSubtreeStream(adaptor,"rule tokenSpec");
        try {
            // ANTLRParser.g:317:2: ( TOKENS ( tokenSpec )+ RBRACE -> ^( TOKENS ( tokenSpec )+ ) )
            // ANTLRParser.g:317:4: TOKENS ( tokenSpec )+ RBRACE
            {
            TOKENS33=(Token)match(input,TOKENS,FOLLOW_TOKENS_in_tokensSpec1409); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_TOKENS.add(TOKENS33);

            // ANTLRParser.g:317:11: ( tokenSpec )+
            int cnt9=0;
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==TEMPLATE||(LA9_0>=TOKEN_REF && LA9_0<=RULE_REF)) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // ANTLRParser.g:317:11: tokenSpec
            	    {
            	    pushFollow(FOLLOW_tokenSpec_in_tokensSpec1411);
            	    tokenSpec34=tokenSpec();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_tokenSpec.add(tokenSpec34.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt9 >= 1 ) break loop9;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(9, input);
                        throw eee;
                }
                cnt9++;
            } while (true);

            RBRACE35=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_tokensSpec1414); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_RBRACE.add(RBRACE35);



            // AST REWRITE
            // elements: TOKENS, tokenSpec
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 317:29: -> ^( TOKENS ( tokenSpec )+ )
            {
                // ANTLRParser.g:317:32: ^( TOKENS ( tokenSpec )+ )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot(stream_TOKENS.nextNode(), root_1);

                if ( !(stream_tokenSpec.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_tokenSpec.hasNext() ) {
                    adaptor.addChild(root_1, stream_tokenSpec.nextTree());

                }
                stream_tokenSpec.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "tokensSpec"

    public static class tokenSpec_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "tokenSpec"
    // ANTLRParser.g:320:1: tokenSpec : ( id ( ASSIGN STRING_LITERAL -> ^( ASSIGN id STRING_LITERAL ) | -> id ) SEMI | RULE_REF );
    public final ANTLRParser.tokenSpec_return tokenSpec() throws RecognitionException {
        ANTLRParser.tokenSpec_return retval = new ANTLRParser.tokenSpec_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token ASSIGN37=null;
        Token STRING_LITERAL38=null;
        Token SEMI39=null;
        Token RULE_REF40=null;
        ANTLRParser.id_return id36 = null;


        GrammarAST ASSIGN37_tree=null;
        GrammarAST STRING_LITERAL38_tree=null;
        GrammarAST SEMI39_tree=null;
        GrammarAST RULE_REF40_tree=null;
        RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleTokenStream stream_ASSIGN=new RewriteRuleTokenStream(adaptor,"token ASSIGN");
        RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");
        try {
            // ANTLRParser.g:321:2: ( id ( ASSIGN STRING_LITERAL -> ^( ASSIGN id STRING_LITERAL ) | -> id ) SEMI | RULE_REF )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==RULE_REF) ) {
                int LA11_1 = input.LA(2);

                if ( (LA11_1==SEMI||LA11_1==ASSIGN) ) {
                    alt11=1;
                }
                else if ( (LA11_1==TEMPLATE||(LA11_1>=RBRACE && LA11_1<=RULE_REF)) ) {
                    alt11=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 11, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA11_0==TEMPLATE||LA11_0==TOKEN_REF) ) {
                alt11=1;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // ANTLRParser.g:321:4: id ( ASSIGN STRING_LITERAL -> ^( ASSIGN id STRING_LITERAL ) | -> id ) SEMI
                    {
                    pushFollow(FOLLOW_id_in_tokenSpec1434);
                    id36=id();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_id.add(id36.getTree());
                    // ANTLRParser.g:322:3: ( ASSIGN STRING_LITERAL -> ^( ASSIGN id STRING_LITERAL ) | -> id )
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==ASSIGN) ) {
                        alt10=1;
                    }
                    else if ( (LA10_0==SEMI) ) {
                        alt10=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 10, 0, input);

                        throw nvae;
                    }
                    switch (alt10) {
                        case 1 :
                            // ANTLRParser.g:322:5: ASSIGN STRING_LITERAL
                            {
                            ASSIGN37=(Token)match(input,ASSIGN,FOLLOW_ASSIGN_in_tokenSpec1440); if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_ASSIGN.add(ASSIGN37);

                            STRING_LITERAL38=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_tokenSpec1442); if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_STRING_LITERAL.add(STRING_LITERAL38);



                            // AST REWRITE
                            // elements: STRING_LITERAL, id, ASSIGN
                            // token labels:
                            // rule labels: retval
                            // token list labels:
                            // rule list labels:
                            // wildcard labels:
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (GrammarAST)adaptor.nil();
                            // 322:27: -> ^( ASSIGN id STRING_LITERAL )
                            {
                                // ANTLRParser.g:322:30: ^( ASSIGN id STRING_LITERAL )
                                {
                                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                                root_1 = (GrammarAST)adaptor.becomeRoot(stream_ASSIGN.nextNode(), root_1);

                                adaptor.addChild(root_1, stream_id.nextTree());
                                adaptor.addChild(root_1, new TerminalAST(stream_STRING_LITERAL.nextToken()));

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }
                            break;
                        case 2 :
                            // ANTLRParser.g:323:11:
                            {

                            // AST REWRITE
                            // elements: id
                            // token labels:
                            // rule labels: retval
                            // token list labels:
                            // rule list labels:
                            // wildcard labels:
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (GrammarAST)adaptor.nil();
                            // 323:11: -> id
                            {
                                adaptor.addChild(root_0, stream_id.nextTree());

                            }

                            retval.tree = root_0;}
                            }
                            break;

                    }

                    SEMI39=(Token)match(input,SEMI,FOLLOW_SEMI_in_tokenSpec1477); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI39);


                    }
                    break;
                case 2 :
                    // ANTLRParser.g:326:4: RULE_REF
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    RULE_REF40=(Token)match(input,RULE_REF,FOLLOW_RULE_REF_in_tokenSpec1482); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RULE_REF40_tree = (GrammarAST)adaptor.create(RULE_REF40);
                    adaptor.addChild(root_0, RULE_REF40_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "tokenSpec"

    public static class attrScope_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "attrScope"
    // ANTLRParser.g:332:1: attrScope : SCOPE id ACTION -> ^( SCOPE id ACTION ) ;
    public final ANTLRParser.attrScope_return attrScope() throws RecognitionException {
        ANTLRParser.attrScope_return retval = new ANTLRParser.attrScope_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token SCOPE41=null;
        Token ACTION43=null;
        ANTLRParser.id_return id42 = null;


        GrammarAST SCOPE41_tree=null;
        GrammarAST ACTION43_tree=null;
        RewriteRuleTokenStream stream_SCOPE=new RewriteRuleTokenStream(adaptor,"token SCOPE");
        RewriteRuleTokenStream stream_ACTION=new RewriteRuleTokenStream(adaptor,"token ACTION");
        RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");
        try {
            // ANTLRParser.g:333:2: ( SCOPE id ACTION -> ^( SCOPE id ACTION ) )
            // ANTLRParser.g:333:4: SCOPE id ACTION
            {
            SCOPE41=(Token)match(input,SCOPE,FOLLOW_SCOPE_in_attrScope1497); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_SCOPE.add(SCOPE41);

            pushFollow(FOLLOW_id_in_attrScope1499);
            id42=id();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_id.add(id42.getTree());
            ACTION43=(Token)match(input,ACTION,FOLLOW_ACTION_in_attrScope1501); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ACTION.add(ACTION43);



            // AST REWRITE
            // elements: ACTION, id, SCOPE
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 333:20: -> ^( SCOPE id ACTION )
            {
                // ANTLRParser.g:333:23: ^( SCOPE id ACTION )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot(stream_SCOPE.nextNode(), root_1);

                adaptor.addChild(root_1, stream_id.nextTree());
                adaptor.addChild(root_1, stream_ACTION.nextNode());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "attrScope"

    public static class action_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "action"
    // ANTLRParser.g:339:1: action : AT ( actionScopeName COLONCOLON )? id ACTION -> ^( AT ( actionScopeName )? id ACTION ) ;
    public final ANTLRParser.action_return action() throws RecognitionException {
        ANTLRParser.action_return retval = new ANTLRParser.action_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token AT44=null;
        Token COLONCOLON46=null;
        Token ACTION48=null;
        ANTLRParser.actionScopeName_return actionScopeName45 = null;

        ANTLRParser.id_return id47 = null;


        GrammarAST AT44_tree=null;
        GrammarAST COLONCOLON46_tree=null;
        GrammarAST ACTION48_tree=null;
        RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
        RewriteRuleTokenStream stream_COLONCOLON=new RewriteRuleTokenStream(adaptor,"token COLONCOLON");
        RewriteRuleTokenStream stream_ACTION=new RewriteRuleTokenStream(adaptor,"token ACTION");
        RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");
        RewriteRuleSubtreeStream stream_actionScopeName=new RewriteRuleSubtreeStream(adaptor,"rule actionScopeName");
        try {
            // ANTLRParser.g:341:2: ( AT ( actionScopeName COLONCOLON )? id ACTION -> ^( AT ( actionScopeName )? id ACTION ) )
            // ANTLRParser.g:341:4: AT ( actionScopeName COLONCOLON )? id ACTION
            {
            AT44=(Token)match(input,AT,FOLLOW_AT_in_action1527); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_AT.add(AT44);

            // ANTLRParser.g:341:7: ( actionScopeName COLONCOLON )?
            int alt12=2;
            switch ( input.LA(1) ) {
                case RULE_REF:
                    {
                    int LA12_1 = input.LA(2);

                    if ( (LA12_1==COLONCOLON) ) {
                        alt12=1;
                    }
                    }
                    break;
                case TOKEN_REF:
                    {
                    int LA12_2 = input.LA(2);

                    if ( (LA12_2==COLONCOLON) ) {
                        alt12=1;
                    }
                    }
                    break;
                case TEMPLATE:
                    {
                    int LA12_3 = input.LA(2);

                    if ( (LA12_3==COLONCOLON) ) {
                        alt12=1;
                    }
                    }
                    break;
                case LEXER:
                case PARSER:
                    {
                    alt12=1;
                    }
                    break;
            }

            switch (alt12) {
                case 1 :
                    // ANTLRParser.g:341:8: actionScopeName COLONCOLON
                    {
                    pushFollow(FOLLOW_actionScopeName_in_action1530);
                    actionScopeName45=actionScopeName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_actionScopeName.add(actionScopeName45.getTree());
                    COLONCOLON46=(Token)match(input,COLONCOLON,FOLLOW_COLONCOLON_in_action1532); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_COLONCOLON.add(COLONCOLON46);


                    }
                    break;

            }

            pushFollow(FOLLOW_id_in_action1536);
            id47=id();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_id.add(id47.getTree());
            ACTION48=(Token)match(input,ACTION,FOLLOW_ACTION_in_action1538); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ACTION.add(ACTION48);



            // AST REWRITE
            // elements: ACTION, AT, id, actionScopeName
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 341:47: -> ^( AT ( actionScopeName )? id ACTION )
            {
                // ANTLRParser.g:341:50: ^( AT ( actionScopeName )? id ACTION )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot(stream_AT.nextNode(), root_1);

                // ANTLRParser.g:341:55: ( actionScopeName )?
                if ( stream_actionScopeName.hasNext() ) {
                    adaptor.addChild(root_1, stream_actionScopeName.nextTree());

                }
                stream_actionScopeName.reset();
                adaptor.addChild(root_1, stream_id.nextTree());
                adaptor.addChild(root_1, stream_ACTION.nextNode());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "action"

    public static class actionScopeName_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "actionScopeName"
    // ANTLRParser.g:344:1: actionScopeName : ( id | LEXER -> ID[$LEXER] | PARSER -> ID[$PARSER] );
    public final ANTLRParser.actionScopeName_return actionScopeName() throws RecognitionException {
        ANTLRParser.actionScopeName_return retval = new ANTLRParser.actionScopeName_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token LEXER50=null;
        Token PARSER51=null;
        ANTLRParser.id_return id49 = null;


        GrammarAST LEXER50_tree=null;
        GrammarAST PARSER51_tree=null;
        RewriteRuleTokenStream stream_PARSER=new RewriteRuleTokenStream(adaptor,"token PARSER");
        RewriteRuleTokenStream stream_LEXER=new RewriteRuleTokenStream(adaptor,"token LEXER");

        try {
            // ANTLRParser.g:348:2: ( id | LEXER -> ID[$LEXER] | PARSER -> ID[$PARSER] )
            int alt13=3;
            switch ( input.LA(1) ) {
            case TEMPLATE:
            case TOKEN_REF:
            case RULE_REF:
                {
                alt13=1;
                }
                break;
            case LEXER:
                {
                alt13=2;
                }
                break;
            case PARSER:
                {
                alt13=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }

            switch (alt13) {
                case 1 :
                    // ANTLRParser.g:348:4: id
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_id_in_actionScopeName1566);
                    id49=id();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, id49.getTree());

                    }
                    break;
                case 2 :
                    // ANTLRParser.g:349:4: LEXER
                    {
                    LEXER50=(Token)match(input,LEXER,FOLLOW_LEXER_in_actionScopeName1571); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_LEXER.add(LEXER50);



                    // AST REWRITE
                    // elements:
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 349:10: -> ID[$LEXER]
                    {
                        adaptor.addChild(root_0, (GrammarAST)adaptor.create(ID, LEXER50));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // ANTLRParser.g:350:9: PARSER
                    {
                    PARSER51=(Token)match(input,PARSER,FOLLOW_PARSER_in_actionScopeName1586); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_PARSER.add(PARSER51);



                    // AST REWRITE
                    // elements:
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 350:16: -> ID[$PARSER]
                    {
                        adaptor.addChild(root_0, (GrammarAST)adaptor.create(ID, PARSER51));

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "actionScopeName"

    public static class rules_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rules"
    // ANTLRParser.g:353:1: rules : ( rule )* -> ^( RULES ( rule )* ) ;
    public final ANTLRParser.rules_return rules() throws RecognitionException {
        ANTLRParser.rules_return retval = new ANTLRParser.rules_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        ANTLRParser.rule_return rule52 = null;


        RewriteRuleSubtreeStream stream_rule=new RewriteRuleSubtreeStream(adaptor,"rule rule");
        try {
            // ANTLRParser.g:354:5: ( ( rule )* -> ^( RULES ( rule )* ) )
            // ANTLRParser.g:354:7: ( rule )*
            {
            // ANTLRParser.g:354:7: ( rule )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==DOC_COMMENT||LA14_0==FRAGMENT||(LA14_0>=PROTECTED && LA14_0<=PRIVATE)||LA14_0==TEMPLATE||(LA14_0>=TOKEN_REF && LA14_0<=RULE_REF)) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // ANTLRParser.g:354:7: rule
            	    {
            	    pushFollow(FOLLOW_rule_in_rules1605);
            	    rule52=rule();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_rule.add(rule52.getTree());

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);



            // AST REWRITE
            // elements: rule
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 358:7: -> ^( RULES ( rule )* )
            {
                // ANTLRParser.g:358:9: ^( RULES ( rule )* )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(RULES, "RULES"), root_1);

                // ANTLRParser.g:358:17: ( rule )*
                while ( stream_rule.hasNext() ) {
                    adaptor.addChild(root_1, stream_rule.nextTree());

                }
                stream_rule.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rules"

    public static class rule_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rule"
    // ANTLRParser.g:370:1: rule : ( DOC_COMMENT )? ( ruleModifiers )? id ( ARG_ACTION )? ( ruleReturns )? ( rulePrequel )* COLON altListAsBlock SEMI exceptionGroup -> ^( RULE id ( DOC_COMMENT )? ( ruleModifiers )? ( ARG_ACTION )? ( ruleReturns )? ( rulePrequel )* altListAsBlock ( exceptionGroup )* ) ;
    public final ANTLRParser.rule_return rule() throws RecognitionException {
        ANTLRParser.rule_return retval = new ANTLRParser.rule_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token DOC_COMMENT53=null;
        Token ARG_ACTION56=null;
        Token COLON59=null;
        Token SEMI61=null;
        ANTLRParser.ruleModifiers_return ruleModifiers54 = null;

        ANTLRParser.id_return id55 = null;

        ANTLRParser.ruleReturns_return ruleReturns57 = null;

        ANTLRParser.rulePrequel_return rulePrequel58 = null;

        ANTLRParser.altListAsBlock_return altListAsBlock60 = null;

        ANTLRParser.exceptionGroup_return exceptionGroup62 = null;


        GrammarAST DOC_COMMENT53_tree=null;
        GrammarAST ARG_ACTION56_tree=null;
        GrammarAST COLON59_tree=null;
        GrammarAST SEMI61_tree=null;
        RewriteRuleTokenStream stream_DOC_COMMENT=new RewriteRuleTokenStream(adaptor,"token DOC_COMMENT");
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleTokenStream stream_ARG_ACTION=new RewriteRuleTokenStream(adaptor,"token ARG_ACTION");
        RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");
        RewriteRuleSubtreeStream stream_ruleModifiers=new RewriteRuleSubtreeStream(adaptor,"rule ruleModifiers");
        RewriteRuleSubtreeStream stream_rulePrequel=new RewriteRuleSubtreeStream(adaptor,"rule rulePrequel");
        RewriteRuleSubtreeStream stream_exceptionGroup=new RewriteRuleSubtreeStream(adaptor,"rule exceptionGroup");
        RewriteRuleSubtreeStream stream_ruleReturns=new RewriteRuleSubtreeStream(adaptor,"rule ruleReturns");
        RewriteRuleSubtreeStream stream_altListAsBlock=new RewriteRuleSubtreeStream(adaptor,"rule altListAsBlock");
        try {
            // ANTLRParser.g:371:5: ( ( DOC_COMMENT )? ( ruleModifiers )? id ( ARG_ACTION )? ( ruleReturns )? ( rulePrequel )* COLON altListAsBlock SEMI exceptionGroup -> ^( RULE id ( DOC_COMMENT )? ( ruleModifiers )? ( ARG_ACTION )? ( ruleReturns )? ( rulePrequel )* altListAsBlock ( exceptionGroup )* ) )
            // ANTLRParser.g:372:7: ( DOC_COMMENT )? ( ruleModifiers )? id ( ARG_ACTION )? ( ruleReturns )? ( rulePrequel )* COLON altListAsBlock SEMI exceptionGroup
            {
            // ANTLRParser.g:372:7: ( DOC_COMMENT )?
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==DOC_COMMENT) ) {
                alt15=1;
            }
            switch (alt15) {
                case 1 :
                    // ANTLRParser.g:372:7: DOC_COMMENT
                    {
                    DOC_COMMENT53=(Token)match(input,DOC_COMMENT,FOLLOW_DOC_COMMENT_in_rule1684); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_DOC_COMMENT.add(DOC_COMMENT53);


                    }
                    break;

            }

            // ANTLRParser.g:378:7: ( ruleModifiers )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==FRAGMENT||(LA16_0>=PROTECTED && LA16_0<=PRIVATE)) ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // ANTLRParser.g:378:7: ruleModifiers
                    {
                    pushFollow(FOLLOW_ruleModifiers_in_rule1728);
                    ruleModifiers54=ruleModifiers();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ruleModifiers.add(ruleModifiers54.getTree());

                    }
                    break;

            }

            pushFollow(FOLLOW_id_in_rule1751);
            id55=id();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_id.add(id55.getTree());
            // ANTLRParser.g:392:4: ( ARG_ACTION )?
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==ARG_ACTION) ) {
                alt17=1;
            }
            switch (alt17) {
                case 1 :
                    // ANTLRParser.g:392:4: ARG_ACTION
                    {
                    ARG_ACTION56=(Token)match(input,ARG_ACTION,FOLLOW_ARG_ACTION_in_rule1784); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ARG_ACTION.add(ARG_ACTION56);


                    }
                    break;

            }

            // ANTLRParser.g:394:4: ( ruleReturns )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==RETURNS) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // ANTLRParser.g:394:4: ruleReturns
                    {
                    pushFollow(FOLLOW_ruleReturns_in_rule1794);
                    ruleReturns57=ruleReturns();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ruleReturns.add(ruleReturns57.getTree());

                    }
                    break;

            }

            // ANTLRParser.g:409:7: ( rulePrequel )*
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( (LA19_0==OPTIONS||LA19_0==SCOPE||LA19_0==THROWS||LA19_0==AT) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // ANTLRParser.g:409:7: rulePrequel
            	    {
            	    pushFollow(FOLLOW_rulePrequel_in_rule1832);
            	    rulePrequel58=rulePrequel();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_rulePrequel.add(rulePrequel58.getTree());

            	    }
            	    break;

            	default :
            	    break loop19;
                }
            } while (true);

            COLON59=(Token)match(input,COLON,FOLLOW_COLON_in_rule1848); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_COLON.add(COLON59);

            pushFollow(FOLLOW_altListAsBlock_in_rule1877);
            altListAsBlock60=altListAsBlock();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_altListAsBlock.add(altListAsBlock60.getTree());
            SEMI61=(Token)match(input,SEMI,FOLLOW_SEMI_in_rule1892); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_SEMI.add(SEMI61);

            pushFollow(FOLLOW_exceptionGroup_in_rule1901);
            exceptionGroup62=exceptionGroup();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_exceptionGroup.add(exceptionGroup62.getTree());


            // AST REWRITE
            // elements: exceptionGroup, ruleReturns, rulePrequel, ARG_ACTION, DOC_COMMENT, ruleModifiers, altListAsBlock, id
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 421:7: -> ^( RULE id ( DOC_COMMENT )? ( ruleModifiers )? ( ARG_ACTION )? ( ruleReturns )? ( rulePrequel )* altListAsBlock ( exceptionGroup )* )
            {
                // ANTLRParser.g:421:10: ^( RULE id ( DOC_COMMENT )? ( ruleModifiers )? ( ARG_ACTION )? ( ruleReturns )? ( rulePrequel )* altListAsBlock ( exceptionGroup )* )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot(new RuleAST(RULE), root_1);

                adaptor.addChild(root_1, stream_id.nextTree());
                // ANTLRParser.g:421:30: ( DOC_COMMENT )?
                if ( stream_DOC_COMMENT.hasNext() ) {
                    adaptor.addChild(root_1, stream_DOC_COMMENT.nextNode());

                }
                stream_DOC_COMMENT.reset();
                // ANTLRParser.g:421:43: ( ruleModifiers )?
                if ( stream_ruleModifiers.hasNext() ) {
                    adaptor.addChild(root_1, stream_ruleModifiers.nextTree());

                }
                stream_ruleModifiers.reset();
                // ANTLRParser.g:421:58: ( ARG_ACTION )?
                if ( stream_ARG_ACTION.hasNext() ) {
                    adaptor.addChild(root_1, stream_ARG_ACTION.nextNode());

                }
                stream_ARG_ACTION.reset();
                // ANTLRParser.g:422:9: ( ruleReturns )?
                if ( stream_ruleReturns.hasNext() ) {
                    adaptor.addChild(root_1, stream_ruleReturns.nextTree());

                }
                stream_ruleReturns.reset();
                // ANTLRParser.g:422:22: ( rulePrequel )*
                while ( stream_rulePrequel.hasNext() ) {
                    adaptor.addChild(root_1, stream_rulePrequel.nextTree());

                }
                stream_rulePrequel.reset();
                adaptor.addChild(root_1, stream_altListAsBlock.nextTree());
                // ANTLRParser.g:422:50: ( exceptionGroup )*
                while ( stream_exceptionGroup.hasNext() ) {
                    adaptor.addChild(root_1, stream_exceptionGroup.nextTree());

                }
                stream_exceptionGroup.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rule"

    public static class exceptionGroup_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "exceptionGroup"
    // ANTLRParser.g:432:1: exceptionGroup : ( exceptionHandler )* ( finallyClause )? ;
    public final ANTLRParser.exceptionGroup_return exceptionGroup() throws RecognitionException {
        ANTLRParser.exceptionGroup_return retval = new ANTLRParser.exceptionGroup_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        ANTLRParser.exceptionHandler_return exceptionHandler63 = null;

        ANTLRParser.finallyClause_return finallyClause64 = null;



        try {
            // ANTLRParser.g:433:5: ( ( exceptionHandler )* ( finallyClause )? )
            // ANTLRParser.g:433:7: ( exceptionHandler )* ( finallyClause )?
            {
            root_0 = (GrammarAST)adaptor.nil();

            // ANTLRParser.g:433:7: ( exceptionHandler )*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( (LA20_0==CATCH) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // ANTLRParser.g:433:7: exceptionHandler
            	    {
            	    pushFollow(FOLLOW_exceptionHandler_in_exceptionGroup1993);
            	    exceptionHandler63=exceptionHandler();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, exceptionHandler63.getTree());

            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);

            // ANTLRParser.g:433:25: ( finallyClause )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==FINALLY) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // ANTLRParser.g:433:25: finallyClause
                    {
                    pushFollow(FOLLOW_finallyClause_in_exceptionGroup1996);
                    finallyClause64=finallyClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, finallyClause64.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "exceptionGroup"

    public static class exceptionHandler_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "exceptionHandler"
    // ANTLRParser.g:438:1: exceptionHandler : CATCH ARG_ACTION ACTION -> ^( CATCH ARG_ACTION ACTION ) ;
    public final ANTLRParser.exceptionHandler_return exceptionHandler() throws RecognitionException {
        ANTLRParser.exceptionHandler_return retval = new ANTLRParser.exceptionHandler_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token CATCH65=null;
        Token ARG_ACTION66=null;
        Token ACTION67=null;

        GrammarAST CATCH65_tree=null;
        GrammarAST ARG_ACTION66_tree=null;
        GrammarAST ACTION67_tree=null;
        RewriteRuleTokenStream stream_CATCH=new RewriteRuleTokenStream(adaptor,"token CATCH");
        RewriteRuleTokenStream stream_ACTION=new RewriteRuleTokenStream(adaptor,"token ACTION");
        RewriteRuleTokenStream stream_ARG_ACTION=new RewriteRuleTokenStream(adaptor,"token ARG_ACTION");

        try {
            // ANTLRParser.g:439:2: ( CATCH ARG_ACTION ACTION -> ^( CATCH ARG_ACTION ACTION ) )
            // ANTLRParser.g:439:4: CATCH ARG_ACTION ACTION
            {
            CATCH65=(Token)match(input,CATCH,FOLLOW_CATCH_in_exceptionHandler2013); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_CATCH.add(CATCH65);

            ARG_ACTION66=(Token)match(input,ARG_ACTION,FOLLOW_ARG_ACTION_in_exceptionHandler2015); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ARG_ACTION.add(ARG_ACTION66);

            ACTION67=(Token)match(input,ACTION,FOLLOW_ACTION_in_exceptionHandler2017); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ACTION.add(ACTION67);



            // AST REWRITE
            // elements: CATCH, ACTION, ARG_ACTION
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 439:28: -> ^( CATCH ARG_ACTION ACTION )
            {
                // ANTLRParser.g:439:31: ^( CATCH ARG_ACTION ACTION )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot(stream_CATCH.nextNode(), root_1);

                adaptor.addChild(root_1, stream_ARG_ACTION.nextNode());
                adaptor.addChild(root_1, stream_ACTION.nextNode());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "exceptionHandler"

    public static class finallyClause_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "finallyClause"
    // ANTLRParser.g:444:1: finallyClause : FINALLY ACTION -> ^( FINALLY ACTION ) ;
    public final ANTLRParser.finallyClause_return finallyClause() throws RecognitionException {
        ANTLRParser.finallyClause_return retval = new ANTLRParser.finallyClause_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token FINALLY68=null;
        Token ACTION69=null;

        GrammarAST FINALLY68_tree=null;
        GrammarAST ACTION69_tree=null;
        RewriteRuleTokenStream stream_FINALLY=new RewriteRuleTokenStream(adaptor,"token FINALLY");
        RewriteRuleTokenStream stream_ACTION=new RewriteRuleTokenStream(adaptor,"token ACTION");

        try {
            // ANTLRParser.g:445:2: ( FINALLY ACTION -> ^( FINALLY ACTION ) )
            // ANTLRParser.g:445:4: FINALLY ACTION
            {
            FINALLY68=(Token)match(input,FINALLY,FOLLOW_FINALLY_in_finallyClause2040); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_FINALLY.add(FINALLY68);

            ACTION69=(Token)match(input,ACTION,FOLLOW_ACTION_in_finallyClause2042); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ACTION.add(ACTION69);



            // AST REWRITE
            // elements: ACTION, FINALLY
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 445:19: -> ^( FINALLY ACTION )
            {
                // ANTLRParser.g:445:22: ^( FINALLY ACTION )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot(stream_FINALLY.nextNode(), root_1);

                adaptor.addChild(root_1, stream_ACTION.nextNode());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "finallyClause"

    public static class rulePrequel_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rulePrequel"
    // ANTLRParser.g:451:1: rulePrequel : ( throwsSpec | ruleScopeSpec | optionsSpec | ruleAction );
    public final ANTLRParser.rulePrequel_return rulePrequel() throws RecognitionException {
        ANTLRParser.rulePrequel_return retval = new ANTLRParser.rulePrequel_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        ANTLRParser.throwsSpec_return throwsSpec70 = null;

        ANTLRParser.ruleScopeSpec_return ruleScopeSpec71 = null;

        ANTLRParser.optionsSpec_return optionsSpec72 = null;

        ANTLRParser.ruleAction_return ruleAction73 = null;



        try {
            // ANTLRParser.g:452:5: ( throwsSpec | ruleScopeSpec | optionsSpec | ruleAction )
            int alt22=4;
            switch ( input.LA(1) ) {
            case THROWS:
                {
                alt22=1;
                }
                break;
            case SCOPE:
                {
                alt22=2;
                }
                break;
            case OPTIONS:
                {
                alt22=3;
                }
                break;
            case AT:
                {
                alt22=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;
            }

            switch (alt22) {
                case 1 :
                    // ANTLRParser.g:452:7: throwsSpec
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_throwsSpec_in_rulePrequel2069);
                    throwsSpec70=throwsSpec();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, throwsSpec70.getTree());

                    }
                    break;
                case 2 :
                    // ANTLRParser.g:453:7: ruleScopeSpec
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_ruleScopeSpec_in_rulePrequel2077);
                    ruleScopeSpec71=ruleScopeSpec();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, ruleScopeSpec71.getTree());

                    }
                    break;
                case 3 :
                    // ANTLRParser.g:454:7: optionsSpec
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_optionsSpec_in_rulePrequel2085);
                    optionsSpec72=optionsSpec();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, optionsSpec72.getTree());

                    }
                    break;
                case 4 :
                    // ANTLRParser.g:455:7: ruleAction
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_ruleAction_in_rulePrequel2093);
                    ruleAction73=ruleAction();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, ruleAction73.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rulePrequel"

    public static class ruleReturns_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ruleReturns"
    // ANTLRParser.g:464:1: ruleReturns : RETURNS ARG_ACTION ;
    public final ANTLRParser.ruleReturns_return ruleReturns() throws RecognitionException {
        ANTLRParser.ruleReturns_return retval = new ANTLRParser.ruleReturns_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token RETURNS74=null;
        Token ARG_ACTION75=null;

        GrammarAST RETURNS74_tree=null;
        GrammarAST ARG_ACTION75_tree=null;

        try {
            // ANTLRParser.g:465:2: ( RETURNS ARG_ACTION )
            // ANTLRParser.g:465:4: RETURNS ARG_ACTION
            {
            root_0 = (GrammarAST)adaptor.nil();

            RETURNS74=(Token)match(input,RETURNS,FOLLOW_RETURNS_in_ruleReturns2113); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RETURNS74_tree = (GrammarAST)adaptor.create(RETURNS74);
            root_0 = (GrammarAST)adaptor.becomeRoot(RETURNS74_tree, root_0);
            }
            ARG_ACTION75=(Token)match(input,ARG_ACTION,FOLLOW_ARG_ACTION_in_ruleReturns2116); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ARG_ACTION75_tree = (GrammarAST)adaptor.create(ARG_ACTION75);
            adaptor.addChild(root_0, ARG_ACTION75_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ruleReturns"

    public static class throwsSpec_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "throwsSpec"
    // ANTLRParser.g:479:1: throwsSpec : THROWS qid ( COMMA qid )* -> ^( THROWS ( qid )+ ) ;
    public final ANTLRParser.throwsSpec_return throwsSpec() throws RecognitionException {
        ANTLRParser.throwsSpec_return retval = new ANTLRParser.throwsSpec_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token THROWS76=null;
        Token COMMA78=null;
        ANTLRParser.qid_return qid77 = null;

        ANTLRParser.qid_return qid79 = null;


        GrammarAST THROWS76_tree=null;
        GrammarAST COMMA78_tree=null;
        RewriteRuleTokenStream stream_THROWS=new RewriteRuleTokenStream(adaptor,"token THROWS");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_qid=new RewriteRuleSubtreeStream(adaptor,"rule qid");
        try {
            // ANTLRParser.g:480:5: ( THROWS qid ( COMMA qid )* -> ^( THROWS ( qid )+ ) )
            // ANTLRParser.g:480:7: THROWS qid ( COMMA qid )*
            {
            THROWS76=(Token)match(input,THROWS,FOLLOW_THROWS_in_throwsSpec2141); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_THROWS.add(THROWS76);

            pushFollow(FOLLOW_qid_in_throwsSpec2143);
            qid77=qid();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_qid.add(qid77.getTree());
            // ANTLRParser.g:480:18: ( COMMA qid )*
            loop23:
            do {
                int alt23=2;
                int LA23_0 = input.LA(1);

                if ( (LA23_0==COMMA) ) {
                    alt23=1;
                }


                switch (alt23) {
            	case 1 :
            	    // ANTLRParser.g:480:19: COMMA qid
            	    {
            	    COMMA78=(Token)match(input,COMMA,FOLLOW_COMMA_in_throwsSpec2146); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA78);

            	    pushFollow(FOLLOW_qid_in_throwsSpec2148);
            	    qid79=qid();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_qid.add(qid79.getTree());

            	    }
            	    break;

            	default :
            	    break loop23;
                }
            } while (true);



            // AST REWRITE
            // elements: THROWS, qid
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 480:31: -> ^( THROWS ( qid )+ )
            {
                // ANTLRParser.g:480:34: ^( THROWS ( qid )+ )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot(stream_THROWS.nextNode(), root_1);

                if ( !(stream_qid.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_qid.hasNext() ) {
                    adaptor.addChild(root_1, stream_qid.nextTree());

                }
                stream_qid.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "throwsSpec"

    public static class ruleScopeSpec_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ruleScopeSpec"
    // ANTLRParser.g:487:1: ruleScopeSpec : ( SCOPE ACTION -> ^( SCOPE ACTION ) | SCOPE id ( COMMA id )* SEMI -> ^( SCOPE ( id )+ ) );
    public final ANTLRParser.ruleScopeSpec_return ruleScopeSpec() throws RecognitionException {
        ANTLRParser.ruleScopeSpec_return retval = new ANTLRParser.ruleScopeSpec_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token SCOPE80=null;
        Token ACTION81=null;
        Token SCOPE82=null;
        Token COMMA84=null;
        Token SEMI86=null;
        ANTLRParser.id_return id83 = null;

        ANTLRParser.id_return id85 = null;


        GrammarAST SCOPE80_tree=null;
        GrammarAST ACTION81_tree=null;
        GrammarAST SCOPE82_tree=null;
        GrammarAST COMMA84_tree=null;
        GrammarAST SEMI86_tree=null;
        RewriteRuleTokenStream stream_SCOPE=new RewriteRuleTokenStream(adaptor,"token SCOPE");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleTokenStream stream_ACTION=new RewriteRuleTokenStream(adaptor,"token ACTION");
        RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");
        try {
            // ANTLRParser.g:488:2: ( SCOPE ACTION -> ^( SCOPE ACTION ) | SCOPE id ( COMMA id )* SEMI -> ^( SCOPE ( id )+ ) )
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==SCOPE) ) {
                int LA25_1 = input.LA(2);

                if ( (LA25_1==ACTION) ) {
                    alt25=1;
                }
                else if ( (LA25_1==TEMPLATE||(LA25_1>=TOKEN_REF && LA25_1<=RULE_REF)) ) {
                    alt25=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 25, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;
            }
            switch (alt25) {
                case 1 :
                    // ANTLRParser.g:488:4: SCOPE ACTION
                    {
                    SCOPE80=(Token)match(input,SCOPE,FOLLOW_SCOPE_in_ruleScopeSpec2179); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_SCOPE.add(SCOPE80);

                    ACTION81=(Token)match(input,ACTION,FOLLOW_ACTION_in_ruleScopeSpec2181); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ACTION.add(ACTION81);



                    // AST REWRITE
                    // elements: ACTION, SCOPE
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 488:17: -> ^( SCOPE ACTION )
                    {
                        // ANTLRParser.g:488:20: ^( SCOPE ACTION )
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot(stream_SCOPE.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_ACTION.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // ANTLRParser.g:489:4: SCOPE id ( COMMA id )* SEMI
                    {
                    SCOPE82=(Token)match(input,SCOPE,FOLLOW_SCOPE_in_ruleScopeSpec2194); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_SCOPE.add(SCOPE82);

                    pushFollow(FOLLOW_id_in_ruleScopeSpec2196);
                    id83=id();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_id.add(id83.getTree());
                    // ANTLRParser.g:489:13: ( COMMA id )*
                    loop24:
                    do {
                        int alt24=2;
                        int LA24_0 = input.LA(1);

                        if ( (LA24_0==COMMA) ) {
                            alt24=1;
                        }


                        switch (alt24) {
                    	case 1 :
                    	    // ANTLRParser.g:489:14: COMMA id
                    	    {
                    	    COMMA84=(Token)match(input,COMMA,FOLLOW_COMMA_in_ruleScopeSpec2199); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA84);

                    	    pushFollow(FOLLOW_id_in_ruleScopeSpec2201);
                    	    id85=id();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_id.add(id85.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop24;
                        }
                    } while (true);

                    SEMI86=(Token)match(input,SEMI,FOLLOW_SEMI_in_ruleScopeSpec2205); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_SEMI.add(SEMI86);



                    // AST REWRITE
                    // elements: id, SCOPE
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 489:30: -> ^( SCOPE ( id )+ )
                    {
                        // ANTLRParser.g:489:33: ^( SCOPE ( id )+ )
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot(stream_SCOPE.nextNode(), root_1);

                        if ( !(stream_id.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_id.hasNext() ) {
                            adaptor.addChild(root_1, stream_id.nextTree());

                        }
                        stream_id.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ruleScopeSpec"

    public static class ruleAction_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ruleAction"
    // ANTLRParser.g:500:1: ruleAction : AT id ACTION -> ^( AT id ACTION ) ;
    public final ANTLRParser.ruleAction_return ruleAction() throws RecognitionException {
        ANTLRParser.ruleAction_return retval = new ANTLRParser.ruleAction_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token AT87=null;
        Token ACTION89=null;
        ANTLRParser.id_return id88 = null;


        GrammarAST AT87_tree=null;
        GrammarAST ACTION89_tree=null;
        RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
        RewriteRuleTokenStream stream_ACTION=new RewriteRuleTokenStream(adaptor,"token ACTION");
        RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");
        try {
            // ANTLRParser.g:502:2: ( AT id ACTION -> ^( AT id ACTION ) )
            // ANTLRParser.g:502:4: AT id ACTION
            {
            AT87=(Token)match(input,AT,FOLLOW_AT_in_ruleAction2235); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_AT.add(AT87);

            pushFollow(FOLLOW_id_in_ruleAction2237);
            id88=id();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_id.add(id88.getTree());
            ACTION89=(Token)match(input,ACTION,FOLLOW_ACTION_in_ruleAction2239); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ACTION.add(ACTION89);



            // AST REWRITE
            // elements: ACTION, AT, id
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 502:17: -> ^( AT id ACTION )
            {
                // ANTLRParser.g:502:20: ^( AT id ACTION )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot(stream_AT.nextNode(), root_1);

                adaptor.addChild(root_1, stream_id.nextTree());
                adaptor.addChild(root_1, stream_ACTION.nextNode());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ruleAction"

    public static class ruleModifiers_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ruleModifiers"
    // ANTLRParser.g:510:1: ruleModifiers : ( ruleModifier )+ -> ^( RULEMODIFIERS ( ruleModifier )+ ) ;
    public final ANTLRParser.ruleModifiers_return ruleModifiers() throws RecognitionException {
        ANTLRParser.ruleModifiers_return retval = new ANTLRParser.ruleModifiers_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        ANTLRParser.ruleModifier_return ruleModifier90 = null;


        RewriteRuleSubtreeStream stream_ruleModifier=new RewriteRuleSubtreeStream(adaptor,"rule ruleModifier");
        try {
            // ANTLRParser.g:511:5: ( ( ruleModifier )+ -> ^( RULEMODIFIERS ( ruleModifier )+ ) )
            // ANTLRParser.g:511:7: ( ruleModifier )+
            {
            // ANTLRParser.g:511:7: ( ruleModifier )+
            int cnt26=0;
            loop26:
            do {
                int alt26=2;
                int LA26_0 = input.LA(1);

                if ( (LA26_0==FRAGMENT||(LA26_0>=PROTECTED && LA26_0<=PRIVATE)) ) {
                    alt26=1;
                }


                switch (alt26) {
            	case 1 :
            	    // ANTLRParser.g:511:7: ruleModifier
            	    {
            	    pushFollow(FOLLOW_ruleModifier_in_ruleModifiers2277);
            	    ruleModifier90=ruleModifier();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_ruleModifier.add(ruleModifier90.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt26 >= 1 ) break loop26;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(26, input);
                        throw eee;
                }
                cnt26++;
            } while (true);



            // AST REWRITE
            // elements: ruleModifier
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 511:21: -> ^( RULEMODIFIERS ( ruleModifier )+ )
            {
                // ANTLRParser.g:511:24: ^( RULEMODIFIERS ( ruleModifier )+ )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(RULEMODIFIERS, "RULEMODIFIERS"), root_1);

                if ( !(stream_ruleModifier.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_ruleModifier.hasNext() ) {
                    adaptor.addChild(root_1, stream_ruleModifier.nextTree());

                }
                stream_ruleModifier.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ruleModifiers"

    public static class ruleModifier_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ruleModifier"
    // ANTLRParser.g:520:1: ruleModifier : ( PUBLIC | PRIVATE | PROTECTED | FRAGMENT );
    public final ANTLRParser.ruleModifier_return ruleModifier() throws RecognitionException {
        ANTLRParser.ruleModifier_return retval = new ANTLRParser.ruleModifier_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token set91=null;

        GrammarAST set91_tree=null;

        try {
            // ANTLRParser.g:521:5: ( PUBLIC | PRIVATE | PROTECTED | FRAGMENT )
            // ANTLRParser.g:
            {
            root_0 = (GrammarAST)adaptor.nil();

            set91=(Token)input.LT(1);
            if ( input.LA(1)==FRAGMENT||(input.LA(1)>=PROTECTED && input.LA(1)<=PRIVATE) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (GrammarAST)adaptor.create(set91));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ruleModifier"

    public static class altList_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "altList"
    // ANTLRParser.g:527:1: altList : alternative ( OR alternative )* -> ( alternative )+ ;
    public final ANTLRParser.altList_return altList() throws RecognitionException {
        ANTLRParser.altList_return retval = new ANTLRParser.altList_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token OR93=null;
        ANTLRParser.alternative_return alternative92 = null;

        ANTLRParser.alternative_return alternative94 = null;


        GrammarAST OR93_tree=null;
        RewriteRuleTokenStream stream_OR=new RewriteRuleTokenStream(adaptor,"token OR");
        RewriteRuleSubtreeStream stream_alternative=new RewriteRuleSubtreeStream(adaptor,"rule alternative");
        try {
            // ANTLRParser.g:528:5: ( alternative ( OR alternative )* -> ( alternative )+ )
            // ANTLRParser.g:528:7: alternative ( OR alternative )*
            {
            pushFollow(FOLLOW_alternative_in_altList2353);
            alternative92=alternative();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_alternative.add(alternative92.getTree());
            // ANTLRParser.g:528:19: ( OR alternative )*
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( (LA27_0==OR) ) {
                    alt27=1;
                }


                switch (alt27) {
            	case 1 :
            	    // ANTLRParser.g:528:20: OR alternative
            	    {
            	    OR93=(Token)match(input,OR,FOLLOW_OR_in_altList2356); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_OR.add(OR93);

            	    pushFollow(FOLLOW_alternative_in_altList2358);
            	    alternative94=alternative();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_alternative.add(alternative94.getTree());

            	    }
            	    break;

            	default :
            	    break loop27;
                }
            } while (true);



            // AST REWRITE
            // elements: alternative
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 528:37: -> ( alternative )+
            {
                if ( !(stream_alternative.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_alternative.hasNext() ) {
                    adaptor.addChild(root_0, stream_alternative.nextTree());

                }
                stream_alternative.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "altList"

    public static class altListAsBlock_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "altListAsBlock"
    // ANTLRParser.g:537:1: altListAsBlock : altList -> ^( BLOCK altList ) ;
    public final ANTLRParser.altListAsBlock_return altListAsBlock() throws RecognitionException {
        ANTLRParser.altListAsBlock_return retval = new ANTLRParser.altListAsBlock_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        ANTLRParser.altList_return altList95 = null;


        RewriteRuleSubtreeStream stream_altList=new RewriteRuleSubtreeStream(adaptor,"rule altList");
        try {
            // ANTLRParser.g:538:5: ( altList -> ^( BLOCK altList ) )
            // ANTLRParser.g:538:7: altList
            {
            pushFollow(FOLLOW_altList_in_altListAsBlock2388);
            altList95=altList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_altList.add(altList95.getTree());


            // AST REWRITE
            // elements: altList
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 538:15: -> ^( BLOCK altList )
            {
                // ANTLRParser.g:538:18: ^( BLOCK altList )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot(new BlockAST(BLOCK), root_1);

                adaptor.addChild(root_1, stream_altList.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "altListAsBlock"

    public static class alternative_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "alternative"
    // ANTLRParser.g:543:1: alternative : ( elements ( rewrite -> ^( ALT_REWRITE elements rewrite ) | -> elements ) | rewrite -> ^( ALT_REWRITE ^( ALT EPSILON ) rewrite ) | -> ^( ALT EPSILON ) );
    public final ANTLRParser.alternative_return alternative() throws RecognitionException {
        ANTLRParser.alternative_return retval = new ANTLRParser.alternative_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        ANTLRParser.elements_return elements96 = null;

        ANTLRParser.rewrite_return rewrite97 = null;

        ANTLRParser.rewrite_return rewrite98 = null;


        RewriteRuleSubtreeStream stream_rewrite=new RewriteRuleSubtreeStream(adaptor,"rule rewrite");
        RewriteRuleSubtreeStream stream_elements=new RewriteRuleSubtreeStream(adaptor,"rule elements");
        try {
            // ANTLRParser.g:544:5: ( elements ( rewrite -> ^( ALT_REWRITE elements rewrite ) | -> elements ) | rewrite -> ^( ALT_REWRITE ^( ALT EPSILON ) rewrite ) | -> ^( ALT EPSILON ) )
            int alt29=3;
            switch ( input.LA(1) ) {
            case SEMPRED:
            case ACTION:
            case TEMPLATE:
            case LPAREN:
            case DOT:
            case TREE_BEGIN:
            case NOT:
            case TOKEN_REF:
            case RULE_REF:
            case STRING_LITERAL:
                {
                alt29=1;
                }
                break;
            case RARROW:
                {
                alt29=2;
                }
                break;
            case EOF:
            case SEMI:
            case RPAREN:
            case OR:
                {
                alt29=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 29, 0, input);

                throw nvae;
            }

            switch (alt29) {
                case 1 :
                    // ANTLRParser.g:544:7: elements ( rewrite -> ^( ALT_REWRITE elements rewrite ) | -> elements )
                    {
                    pushFollow(FOLLOW_elements_in_alternative2418);
                    elements96=elements();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_elements.add(elements96.getTree());
                    // ANTLRParser.g:545:6: ( rewrite -> ^( ALT_REWRITE elements rewrite ) | -> elements )
                    int alt28=2;
                    int LA28_0 = input.LA(1);

                    if ( (LA28_0==RARROW) ) {
                        alt28=1;
                    }
                    else if ( (LA28_0==EOF||LA28_0==SEMI||LA28_0==RPAREN||LA28_0==OR) ) {
                        alt28=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 28, 0, input);

                        throw nvae;
                    }
                    switch (alt28) {
                        case 1 :
                            // ANTLRParser.g:545:8: rewrite
                            {
                            pushFollow(FOLLOW_rewrite_in_alternative2427);
                            rewrite97=rewrite();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_rewrite.add(rewrite97.getTree());


                            // AST REWRITE
                            // elements: rewrite, elements
                            // token labels:
                            // rule labels: retval
                            // token list labels:
                            // rule list labels:
                            // wildcard labels:
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (GrammarAST)adaptor.nil();
                            // 545:16: -> ^( ALT_REWRITE elements rewrite )
                            {
                                // ANTLRParser.g:545:19: ^( ALT_REWRITE elements rewrite )
                                {
                                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                                root_1 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(ALT_REWRITE, "ALT_REWRITE"), root_1);

                                adaptor.addChild(root_1, stream_elements.nextTree());
                                adaptor.addChild(root_1, stream_rewrite.nextTree());

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }
                            break;
                        case 2 :
                            // ANTLRParser.g:546:10:
                            {

                            // AST REWRITE
                            // elements: elements
                            // token labels:
                            // rule labels: retval
                            // token list labels:
                            // rule list labels:
                            // wildcard labels:
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (GrammarAST)adaptor.nil();
                            // 546:10: -> elements
                            {
                                adaptor.addChild(root_0, stream_elements.nextTree());

                            }

                            retval.tree = root_0;}
                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // ANTLRParser.g:548:7: rewrite
                    {
                    pushFollow(FOLLOW_rewrite_in_alternative2465);
                    rewrite98=rewrite();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rewrite.add(rewrite98.getTree());


                    // AST REWRITE
                    // elements: rewrite
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 548:16: -> ^( ALT_REWRITE ^( ALT EPSILON ) rewrite )
                    {
                        // ANTLRParser.g:548:19: ^( ALT_REWRITE ^( ALT EPSILON ) rewrite )
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(ALT_REWRITE, "ALT_REWRITE"), root_1);

                        // ANTLRParser.g:548:33: ^( ALT EPSILON )
                        {
                        GrammarAST root_2 = (GrammarAST)adaptor.nil();
                        root_2 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(ALT, "ALT"), root_2);

                        adaptor.addChild(root_2, (GrammarAST)adaptor.create(EPSILON, "EPSILON"));

                        adaptor.addChild(root_1, root_2);
                        }
                        adaptor.addChild(root_1, stream_rewrite.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // ANTLRParser.g:549:10:
                    {

                    // AST REWRITE
                    // elements:
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 549:10: -> ^( ALT EPSILON )
                    {
                        // ANTLRParser.g:549:13: ^( ALT EPSILON )
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(ALT, "ALT"), root_1);

                        adaptor.addChild(root_1, (GrammarAST)adaptor.create(EPSILON, "EPSILON"));

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "alternative"

    public static class elements_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "elements"
    // ANTLRParser.g:552:1: elements : (e+= element )+ -> ^( ALT ( $e)+ ) ;
    public final ANTLRParser.elements_return elements() throws RecognitionException {
        ANTLRParser.elements_return retval = new ANTLRParser.elements_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        List list_e=null;
        RuleReturnScope e = null;
        RewriteRuleSubtreeStream stream_element=new RewriteRuleSubtreeStream(adaptor,"rule element");
        try {
            // ANTLRParser.g:553:5: ( (e+= element )+ -> ^( ALT ( $e)+ ) )
            // ANTLRParser.g:553:7: (e+= element )+
            {
            // ANTLRParser.g:553:8: (e+= element )+
            int cnt30=0;
            loop30:
            do {
                int alt30=2;
                int LA30_0 = input.LA(1);

                if ( (LA30_0==SEMPRED||LA30_0==ACTION||LA30_0==TEMPLATE||LA30_0==LPAREN||LA30_0==DOT||LA30_0==TREE_BEGIN||LA30_0==NOT||(LA30_0>=TOKEN_REF && LA30_0<=RULE_REF)||LA30_0==STRING_LITERAL) ) {
                    alt30=1;
                }


                switch (alt30) {
            	case 1 :
            	    // ANTLRParser.g:553:8: e+= element
            	    {
            	    pushFollow(FOLLOW_element_in_elements2518);
            	    e=element();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_element.add(e.getTree());
            	    if (list_e==null) list_e=new ArrayList();
            	    list_e.add(e.getTree());


            	    }
            	    break;

            	default :
            	    if ( cnt30 >= 1 ) break loop30;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(30, input);
                        throw eee;
                }
                cnt30++;
            } while (true);



            // AST REWRITE
            // elements: e
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels: e
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_e=new RewriteRuleSubtreeStream(adaptor,"token e",list_e);
            root_0 = (GrammarAST)adaptor.nil();
            // 553:19: -> ^( ALT ( $e)+ )
            {
                // ANTLRParser.g:553:22: ^( ALT ( $e)+ )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(ALT, "ALT"), root_1);

                if ( !(stream_e.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_e.hasNext() ) {
                    adaptor.addChild(root_1, stream_e.nextTree());

                }
                stream_e.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "elements"

    public static class element_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "element"
    // ANTLRParser.g:556:1: element : ( labeledElement ( ebnfSuffix -> ^( ebnfSuffix ^( BLOCK ^( ALT labeledElement ) ) ) | -> labeledElement ) | atom ( ebnfSuffix -> ^( ebnfSuffix ^( BLOCK ^( ALT atom ) ) ) | -> atom ) | ebnf | ACTION | SEMPRED ( IMPLIES -> GATED_SEMPRED[$IMPLIES] | -> SEMPRED ) | treeSpec ( ebnfSuffix -> ^( ebnfSuffix ^( BLOCK ^( ALT treeSpec ) ) ) | -> treeSpec ) );
    public final ANTLRParser.element_return element() throws RecognitionException {
        ANTLRParser.element_return retval = new ANTLRParser.element_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token ACTION104=null;
        Token SEMPRED105=null;
        Token IMPLIES106=null;
        ANTLRParser.labeledElement_return labeledElement99 = null;

        ANTLRParser.ebnfSuffix_return ebnfSuffix100 = null;

        ANTLRParser.atom_return atom101 = null;

        ANTLRParser.ebnfSuffix_return ebnfSuffix102 = null;

        ANTLRParser.ebnf_return ebnf103 = null;

        ANTLRParser.treeSpec_return treeSpec107 = null;

        ANTLRParser.ebnfSuffix_return ebnfSuffix108 = null;


        GrammarAST ACTION104_tree=null;
        GrammarAST SEMPRED105_tree=null;
        GrammarAST IMPLIES106_tree=null;
        RewriteRuleTokenStream stream_IMPLIES=new RewriteRuleTokenStream(adaptor,"token IMPLIES");
        RewriteRuleTokenStream stream_SEMPRED=new RewriteRuleTokenStream(adaptor,"token SEMPRED");
        RewriteRuleSubtreeStream stream_atom=new RewriteRuleSubtreeStream(adaptor,"rule atom");
        RewriteRuleSubtreeStream stream_ebnfSuffix=new RewriteRuleSubtreeStream(adaptor,"rule ebnfSuffix");
        RewriteRuleSubtreeStream stream_treeSpec=new RewriteRuleSubtreeStream(adaptor,"rule treeSpec");
        RewriteRuleSubtreeStream stream_labeledElement=new RewriteRuleSubtreeStream(adaptor,"rule labeledElement");
        try {
            // ANTLRParser.g:557:2: ( labeledElement ( ebnfSuffix -> ^( ebnfSuffix ^( BLOCK ^( ALT labeledElement ) ) ) | -> labeledElement ) | atom ( ebnfSuffix -> ^( ebnfSuffix ^( BLOCK ^( ALT atom ) ) ) | -> atom ) | ebnf | ACTION | SEMPRED ( IMPLIES -> GATED_SEMPRED[$IMPLIES] | -> SEMPRED ) | treeSpec ( ebnfSuffix -> ^( ebnfSuffix ^( BLOCK ^( ALT treeSpec ) ) ) | -> treeSpec ) )
            int alt35=6;
            alt35 = dfa35.predict(input);
            switch (alt35) {
                case 1 :
                    // ANTLRParser.g:557:4: labeledElement ( ebnfSuffix -> ^( ebnfSuffix ^( BLOCK ^( ALT labeledElement ) ) ) | -> labeledElement )
                    {
                    pushFollow(FOLLOW_labeledElement_in_element2545);
                    labeledElement99=labeledElement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_labeledElement.add(labeledElement99.getTree());
                    // ANTLRParser.g:558:3: ( ebnfSuffix -> ^( ebnfSuffix ^( BLOCK ^( ALT labeledElement ) ) ) | -> labeledElement )
                    int alt31=2;
                    int LA31_0 = input.LA(1);

                    if ( (LA31_0==QUESTION||(LA31_0>=STAR && LA31_0<=PLUS)) ) {
                        alt31=1;
                    }
                    else if ( (LA31_0==EOF||LA31_0==SEMPRED||LA31_0==ACTION||LA31_0==TEMPLATE||(LA31_0>=SEMI && LA31_0<=RPAREN)||LA31_0==OR||LA31_0==DOT||(LA31_0>=RARROW && LA31_0<=TREE_BEGIN)||LA31_0==NOT||(LA31_0>=TOKEN_REF && LA31_0<=RULE_REF)||LA31_0==STRING_LITERAL) ) {
                        alt31=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 31, 0, input);

                        throw nvae;
                    }
                    switch (alt31) {
                        case 1 :
                            // ANTLRParser.g:558:5: ebnfSuffix
                            {
                            pushFollow(FOLLOW_ebnfSuffix_in_element2551);
                            ebnfSuffix100=ebnfSuffix();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_ebnfSuffix.add(ebnfSuffix100.getTree());


                            // AST REWRITE
                            // elements: labeledElement, ebnfSuffix
                            // token labels:
                            // rule labels: retval
                            // token list labels:
                            // rule list labels:
                            // wildcard labels:
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (GrammarAST)adaptor.nil();
                            // 558:16: -> ^( ebnfSuffix ^( BLOCK ^( ALT labeledElement ) ) )
                            {
                                // ANTLRParser.g:558:19: ^( ebnfSuffix ^( BLOCK ^( ALT labeledElement ) ) )
                                {
                                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                                root_1 = (GrammarAST)adaptor.becomeRoot(stream_ebnfSuffix.nextNode(), root_1);

                                // ANTLRParser.g:558:33: ^( BLOCK ^( ALT labeledElement ) )
                                {
                                GrammarAST root_2 = (GrammarAST)adaptor.nil();
                                root_2 = (GrammarAST)adaptor.becomeRoot(new BlockAST(BLOCK), root_2);

                                // ANTLRParser.g:558:51: ^( ALT labeledElement )
                                {
                                GrammarAST root_3 = (GrammarAST)adaptor.nil();
                                root_3 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(ALT, "ALT"), root_3);

                                adaptor.addChild(root_3, stream_labeledElement.nextTree());

                                adaptor.addChild(root_2, root_3);
                                }

                                adaptor.addChild(root_1, root_2);
                                }

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }
                            break;
                        case 2 :
                            // ANTLRParser.g:559:8:
                            {

                            // AST REWRITE
                            // elements: labeledElement
                            // token labels:
                            // rule labels: retval
                            // token list labels:
                            // rule list labels:
                            // wildcard labels:
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (GrammarAST)adaptor.nil();
                            // 559:8: -> labeledElement
                            {
                                adaptor.addChild(root_0, stream_labeledElement.nextTree());

                            }

                            retval.tree = root_0;}
                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // ANTLRParser.g:561:4: atom ( ebnfSuffix -> ^( ebnfSuffix ^( BLOCK ^( ALT atom ) ) ) | -> atom )
                    {
                    pushFollow(FOLLOW_atom_in_element2593);
                    atom101=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_atom.add(atom101.getTree());
                    // ANTLRParser.g:562:3: ( ebnfSuffix -> ^( ebnfSuffix ^( BLOCK ^( ALT atom ) ) ) | -> atom )
                    int alt32=2;
                    int LA32_0 = input.LA(1);

                    if ( (LA32_0==QUESTION||(LA32_0>=STAR && LA32_0<=PLUS)) ) {
                        alt32=1;
                    }
                    else if ( (LA32_0==EOF||LA32_0==SEMPRED||LA32_0==ACTION||LA32_0==TEMPLATE||(LA32_0>=SEMI && LA32_0<=RPAREN)||LA32_0==OR||LA32_0==DOT||(LA32_0>=RARROW && LA32_0<=TREE_BEGIN)||LA32_0==NOT||(LA32_0>=TOKEN_REF && LA32_0<=RULE_REF)||LA32_0==STRING_LITERAL) ) {
                        alt32=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 32, 0, input);

                        throw nvae;
                    }
                    switch (alt32) {
                        case 1 :
                            // ANTLRParser.g:562:5: ebnfSuffix
                            {
                            pushFollow(FOLLOW_ebnfSuffix_in_element2599);
                            ebnfSuffix102=ebnfSuffix();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_ebnfSuffix.add(ebnfSuffix102.getTree());


                            // AST REWRITE
                            // elements: ebnfSuffix, atom
                            // token labels:
                            // rule labels: retval
                            // token list labels:
                            // rule list labels:
                            // wildcard labels:
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (GrammarAST)adaptor.nil();
                            // 562:16: -> ^( ebnfSuffix ^( BLOCK ^( ALT atom ) ) )
                            {
                                // ANTLRParser.g:562:19: ^( ebnfSuffix ^( BLOCK ^( ALT atom ) ) )
                                {
                                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                                root_1 = (GrammarAST)adaptor.becomeRoot(stream_ebnfSuffix.nextNode(), root_1);

                                // ANTLRParser.g:562:33: ^( BLOCK ^( ALT atom ) )
                                {
                                GrammarAST root_2 = (GrammarAST)adaptor.nil();
                                root_2 = (GrammarAST)adaptor.becomeRoot(new BlockAST(BLOCK), root_2);

                                // ANTLRParser.g:562:51: ^( ALT atom )
                                {
                                GrammarAST root_3 = (GrammarAST)adaptor.nil();
                                root_3 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(ALT, "ALT"), root_3);

                                adaptor.addChild(root_3, stream_atom.nextTree());

                                adaptor.addChild(root_2, root_3);
                                }

                                adaptor.addChild(root_1, root_2);
                                }

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }
                            break;
                        case 2 :
                            // ANTLRParser.g:563:8:
                            {

                            // AST REWRITE
                            // elements: atom
                            // token labels:
                            // rule labels: retval
                            // token list labels:
                            // rule list labels:
                            // wildcard labels:
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (GrammarAST)adaptor.nil();
                            // 563:8: -> atom
                            {
                                adaptor.addChild(root_0, stream_atom.nextTree());

                            }

                            retval.tree = root_0;}
                            }
                            break;

                    }


                    }
                    break;
                case 3 :
                    // ANTLRParser.g:565:4: ebnf
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_ebnf_in_element2642);
                    ebnf103=ebnf();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, ebnf103.getTree());

                    }
                    break;
                case 4 :
                    // ANTLRParser.g:566:6: ACTION
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    ACTION104=(Token)match(input,ACTION,FOLLOW_ACTION_in_element2649); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ACTION104_tree = (GrammarAST)adaptor.create(ACTION104);
                    adaptor.addChild(root_0, ACTION104_tree);
                    }

                    }
                    break;
                case 5 :
                    // ANTLRParser.g:567:6: SEMPRED ( IMPLIES -> GATED_SEMPRED[$IMPLIES] | -> SEMPRED )
                    {
                    SEMPRED105=(Token)match(input,SEMPRED,FOLLOW_SEMPRED_in_element2656); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_SEMPRED.add(SEMPRED105);

                    // ANTLRParser.g:568:3: ( IMPLIES -> GATED_SEMPRED[$IMPLIES] | -> SEMPRED )
                    int alt33=2;
                    int LA33_0 = input.LA(1);

                    if ( (LA33_0==IMPLIES) ) {
                        alt33=1;
                    }
                    else if ( (LA33_0==EOF||LA33_0==SEMPRED||LA33_0==ACTION||LA33_0==TEMPLATE||(LA33_0>=SEMI && LA33_0<=RPAREN)||LA33_0==OR||LA33_0==DOT||(LA33_0>=RARROW && LA33_0<=TREE_BEGIN)||LA33_0==NOT||(LA33_0>=TOKEN_REF && LA33_0<=RULE_REF)||LA33_0==STRING_LITERAL) ) {
                        alt33=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 33, 0, input);

                        throw nvae;
                    }
                    switch (alt33) {
                        case 1 :
                            // ANTLRParser.g:568:5: IMPLIES
                            {
                            IMPLIES106=(Token)match(input,IMPLIES,FOLLOW_IMPLIES_in_element2662); if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_IMPLIES.add(IMPLIES106);



                            // AST REWRITE
                            // elements:
                            // token labels:
                            // rule labels: retval
                            // token list labels:
                            // rule list labels:
                            // wildcard labels:
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (GrammarAST)adaptor.nil();
                            // 568:14: -> GATED_SEMPRED[$IMPLIES]
                            {
                                adaptor.addChild(root_0, (GrammarAST)adaptor.create(GATED_SEMPRED, IMPLIES106));

                            }

                            retval.tree = root_0;}
                            }
                            break;
                        case 2 :
                            // ANTLRParser.g:569:8:
                            {

                            // AST REWRITE
                            // elements: SEMPRED
                            // token labels:
                            // rule labels: retval
                            // token list labels:
                            // rule list labels:
                            // wildcard labels:
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (GrammarAST)adaptor.nil();
                            // 569:8: -> SEMPRED
                            {
                                adaptor.addChild(root_0, stream_SEMPRED.nextNode());

                            }

                            retval.tree = root_0;}
                            }
                            break;

                    }


                    }
                    break;
                case 6 :
                    // ANTLRParser.g:571:6: treeSpec ( ebnfSuffix -> ^( ebnfSuffix ^( BLOCK ^( ALT treeSpec ) ) ) | -> treeSpec )
                    {
                    pushFollow(FOLLOW_treeSpec_in_element2690);
                    treeSpec107=treeSpec();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_treeSpec.add(treeSpec107.getTree());
                    // ANTLRParser.g:572:3: ( ebnfSuffix -> ^( ebnfSuffix ^( BLOCK ^( ALT treeSpec ) ) ) | -> treeSpec )
                    int alt34=2;
                    int LA34_0 = input.LA(1);

                    if ( (LA34_0==QUESTION||(LA34_0>=STAR && LA34_0<=PLUS)) ) {
                        alt34=1;
                    }
                    else if ( (LA34_0==EOF||LA34_0==SEMPRED||LA34_0==ACTION||LA34_0==TEMPLATE||(LA34_0>=SEMI && LA34_0<=RPAREN)||LA34_0==OR||LA34_0==DOT||(LA34_0>=RARROW && LA34_0<=TREE_BEGIN)||LA34_0==NOT||(LA34_0>=TOKEN_REF && LA34_0<=RULE_REF)||LA34_0==STRING_LITERAL) ) {
                        alt34=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 34, 0, input);

                        throw nvae;
                    }
                    switch (alt34) {
                        case 1 :
                            // ANTLRParser.g:572:5: ebnfSuffix
                            {
                            pushFollow(FOLLOW_ebnfSuffix_in_element2696);
                            ebnfSuffix108=ebnfSuffix();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_ebnfSuffix.add(ebnfSuffix108.getTree());


                            // AST REWRITE
                            // elements: ebnfSuffix, treeSpec
                            // token labels:
                            // rule labels: retval
                            // token list labels:
                            // rule list labels:
                            // wildcard labels:
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (GrammarAST)adaptor.nil();
                            // 572:16: -> ^( ebnfSuffix ^( BLOCK ^( ALT treeSpec ) ) )
                            {
                                // ANTLRParser.g:572:19: ^( ebnfSuffix ^( BLOCK ^( ALT treeSpec ) ) )
                                {
                                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                                root_1 = (GrammarAST)adaptor.becomeRoot(stream_ebnfSuffix.nextNode(), root_1);

                                // ANTLRParser.g:572:33: ^( BLOCK ^( ALT treeSpec ) )
                                {
                                GrammarAST root_2 = (GrammarAST)adaptor.nil();
                                root_2 = (GrammarAST)adaptor.becomeRoot(new BlockAST(BLOCK), root_2);

                                // ANTLRParser.g:572:51: ^( ALT treeSpec )
                                {
                                GrammarAST root_3 = (GrammarAST)adaptor.nil();
                                root_3 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(ALT, "ALT"), root_3);

                                adaptor.addChild(root_3, stream_treeSpec.nextTree());

                                adaptor.addChild(root_2, root_3);
                                }

                                adaptor.addChild(root_1, root_2);
                                }

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }
                            break;
                        case 2 :
                            // ANTLRParser.g:573:8:
                            {

                            // AST REWRITE
                            // elements: treeSpec
                            // token labels:
                            // rule labels: retval
                            // token list labels:
                            // rule list labels:
                            // wildcard labels:
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (GrammarAST)adaptor.nil();
                            // 573:8: -> treeSpec
                            {
                                adaptor.addChild(root_0, stream_treeSpec.nextTree());

                            }

                            retval.tree = root_0;}
                            }
                            break;

                    }


                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "element"

    public static class labeledElement_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "labeledElement"
    // ANTLRParser.g:577:1: labeledElement : id ( ASSIGN | PLUS_ASSIGN ) ( atom | block ) ;
    public final ANTLRParser.labeledElement_return labeledElement() throws RecognitionException {
        ANTLRParser.labeledElement_return retval = new ANTLRParser.labeledElement_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token ASSIGN110=null;
        Token PLUS_ASSIGN111=null;
        ANTLRParser.id_return id109 = null;

        ANTLRParser.atom_return atom112 = null;

        ANTLRParser.block_return block113 = null;


        GrammarAST ASSIGN110_tree=null;
        GrammarAST PLUS_ASSIGN111_tree=null;

        try {
            // ANTLRParser.g:577:16: ( id ( ASSIGN | PLUS_ASSIGN ) ( atom | block ) )
            // ANTLRParser.g:577:18: id ( ASSIGN | PLUS_ASSIGN ) ( atom | block )
            {
            root_0 = (GrammarAST)adaptor.nil();

            pushFollow(FOLLOW_id_in_labeledElement2745);
            id109=id();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, id109.getTree());
            // ANTLRParser.g:577:21: ( ASSIGN | PLUS_ASSIGN )
            int alt36=2;
            int LA36_0 = input.LA(1);

            if ( (LA36_0==ASSIGN) ) {
                alt36=1;
            }
            else if ( (LA36_0==PLUS_ASSIGN) ) {
                alt36=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                throw nvae;
            }
            switch (alt36) {
                case 1 :
                    // ANTLRParser.g:577:22: ASSIGN
                    {
                    ASSIGN110=(Token)match(input,ASSIGN,FOLLOW_ASSIGN_in_labeledElement2748); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ASSIGN110_tree = (GrammarAST)adaptor.create(ASSIGN110);
                    root_0 = (GrammarAST)adaptor.becomeRoot(ASSIGN110_tree, root_0);
                    }

                    }
                    break;
                case 2 :
                    // ANTLRParser.g:577:30: PLUS_ASSIGN
                    {
                    PLUS_ASSIGN111=(Token)match(input,PLUS_ASSIGN,FOLLOW_PLUS_ASSIGN_in_labeledElement2751); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    PLUS_ASSIGN111_tree = (GrammarAST)adaptor.create(PLUS_ASSIGN111);
                    root_0 = (GrammarAST)adaptor.becomeRoot(PLUS_ASSIGN111_tree, root_0);
                    }

                    }
                    break;

            }

            // ANTLRParser.g:577:44: ( atom | block )
            int alt37=2;
            int LA37_0 = input.LA(1);

            if ( (LA37_0==TEMPLATE||LA37_0==DOT||LA37_0==NOT||(LA37_0>=TOKEN_REF && LA37_0<=RULE_REF)||LA37_0==STRING_LITERAL) ) {
                alt37=1;
            }
            else if ( (LA37_0==LPAREN) ) {
                alt37=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 37, 0, input);

                throw nvae;
            }
            switch (alt37) {
                case 1 :
                    // ANTLRParser.g:577:45: atom
                    {
                    pushFollow(FOLLOW_atom_in_labeledElement2756);
                    atom112=atom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, atom112.getTree());

                    }
                    break;
                case 2 :
                    // ANTLRParser.g:577:50: block
                    {
                    pushFollow(FOLLOW_block_in_labeledElement2758);
                    block113=block();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, block113.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "labeledElement"

    public static class treeSpec_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "treeSpec"
    // ANTLRParser.g:583:1: treeSpec : TREE_BEGIN element ( element )+ RPAREN -> ^( TREE_BEGIN ( element )+ ) ;
    public final ANTLRParser.treeSpec_return treeSpec() throws RecognitionException {
        ANTLRParser.treeSpec_return retval = new ANTLRParser.treeSpec_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token TREE_BEGIN114=null;
        Token RPAREN117=null;
        ANTLRParser.element_return element115 = null;

        ANTLRParser.element_return element116 = null;


        GrammarAST TREE_BEGIN114_tree=null;
        GrammarAST RPAREN117_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_TREE_BEGIN=new RewriteRuleTokenStream(adaptor,"token TREE_BEGIN");
        RewriteRuleSubtreeStream stream_element=new RewriteRuleSubtreeStream(adaptor,"rule element");
        try {
            // ANTLRParser.g:584:5: ( TREE_BEGIN element ( element )+ RPAREN -> ^( TREE_BEGIN ( element )+ ) )
            // ANTLRParser.g:584:7: TREE_BEGIN element ( element )+ RPAREN
            {
            TREE_BEGIN114=(Token)match(input,TREE_BEGIN,FOLLOW_TREE_BEGIN_in_treeSpec2780); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_TREE_BEGIN.add(TREE_BEGIN114);

            pushFollow(FOLLOW_element_in_treeSpec2821);
            element115=element();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_element.add(element115.getTree());
            // ANTLRParser.g:591:10: ( element )+
            int cnt38=0;
            loop38:
            do {
                int alt38=2;
                int LA38_0 = input.LA(1);

                if ( (LA38_0==SEMPRED||LA38_0==ACTION||LA38_0==TEMPLATE||LA38_0==LPAREN||LA38_0==DOT||LA38_0==TREE_BEGIN||LA38_0==NOT||(LA38_0>=TOKEN_REF && LA38_0<=RULE_REF)||LA38_0==STRING_LITERAL) ) {
                    alt38=1;
                }


                switch (alt38) {
            	case 1 :
            	    // ANTLRParser.g:591:10: element
            	    {
            	    pushFollow(FOLLOW_element_in_treeSpec2852);
            	    element116=element();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_element.add(element116.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt38 >= 1 ) break loop38;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(38, input);
                        throw eee;
                }
                cnt38++;
            } while (true);

            RPAREN117=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_treeSpec2861); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN117);



            // AST REWRITE
            // elements: TREE_BEGIN, element
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 593:7: -> ^( TREE_BEGIN ( element )+ )
            {
                // ANTLRParser.g:593:10: ^( TREE_BEGIN ( element )+ )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot(stream_TREE_BEGIN.nextNode(), root_1);

                if ( !(stream_element.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_element.hasNext() ) {
                    adaptor.addChild(root_1, stream_element.nextTree());

                }
                stream_element.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "treeSpec"

    public static class ebnf_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ebnf"
    // ANTLRParser.g:598:1: ebnf : block ( blockSuffixe -> ^( blockSuffixe block ) | -> block ) ;
    public final ANTLRParser.ebnf_return ebnf() throws RecognitionException {
        ANTLRParser.ebnf_return retval = new ANTLRParser.ebnf_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        ANTLRParser.block_return block118 = null;

        ANTLRParser.blockSuffixe_return blockSuffixe119 = null;


        RewriteRuleSubtreeStream stream_blockSuffixe=new RewriteRuleSubtreeStream(adaptor,"rule blockSuffixe");
        RewriteRuleSubtreeStream stream_block=new RewriteRuleSubtreeStream(adaptor,"rule block");
        try {
            // ANTLRParser.g:599:5: ( block ( blockSuffixe -> ^( blockSuffixe block ) | -> block ) )
            // ANTLRParser.g:599:7: block ( blockSuffixe -> ^( blockSuffixe block ) | -> block )
            {
            pushFollow(FOLLOW_block_in_ebnf2895);
            block118=block();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_block.add(block118.getTree());
            // ANTLRParser.g:603:7: ( blockSuffixe -> ^( blockSuffixe block ) | -> block )
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==IMPLIES||(LA39_0>=QUESTION && LA39_0<=PLUS)||LA39_0==ROOT) ) {
                alt39=1;
            }
            else if ( (LA39_0==EOF||LA39_0==SEMPRED||LA39_0==ACTION||LA39_0==TEMPLATE||(LA39_0>=SEMI && LA39_0<=RPAREN)||LA39_0==OR||LA39_0==DOT||(LA39_0>=RARROW && LA39_0<=TREE_BEGIN)||LA39_0==NOT||(LA39_0>=TOKEN_REF && LA39_0<=RULE_REF)||LA39_0==STRING_LITERAL) ) {
                alt39=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;
            }
            switch (alt39) {
                case 1 :
                    // ANTLRParser.g:603:9: blockSuffixe
                    {
                    pushFollow(FOLLOW_blockSuffixe_in_ebnf2930);
                    blockSuffixe119=blockSuffixe();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_blockSuffixe.add(blockSuffixe119.getTree());


                    // AST REWRITE
                    // elements: block, blockSuffixe
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 603:22: -> ^( blockSuffixe block )
                    {
                        // ANTLRParser.g:603:25: ^( blockSuffixe block )
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot(stream_blockSuffixe.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_block.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // ANTLRParser.g:604:13:
                    {

                    // AST REWRITE
                    // elements: block
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 604:13: -> block
                    {
                        adaptor.addChild(root_0, stream_block.nextTree());

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ebnf"

    public static class blockSuffixe_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "blockSuffixe"
    // ANTLRParser.g:610:1: blockSuffixe : ( ebnfSuffix | ROOT | IMPLIES | BANG );
    public final ANTLRParser.blockSuffixe_return blockSuffixe() throws RecognitionException {
        ANTLRParser.blockSuffixe_return retval = new ANTLRParser.blockSuffixe_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token ROOT121=null;
        Token IMPLIES122=null;
        Token BANG123=null;
        ANTLRParser.ebnfSuffix_return ebnfSuffix120 = null;


        GrammarAST ROOT121_tree=null;
        GrammarAST IMPLIES122_tree=null;
        GrammarAST BANG123_tree=null;

        try {
            // ANTLRParser.g:611:5: ( ebnfSuffix | ROOT | IMPLIES | BANG )
            int alt40=4;
            switch ( input.LA(1) ) {
            case QUESTION:
            case STAR:
            case PLUS:
                {
                alt40=1;
                }
                break;
            case ROOT:
                {
                alt40=2;
                }
                break;
            case IMPLIES:
                {
                alt40=3;
                }
                break;
            case BANG:
                {
                alt40=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 40, 0, input);

                throw nvae;
            }

            switch (alt40) {
                case 1 :
                    // ANTLRParser.g:611:7: ebnfSuffix
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_ebnfSuffix_in_blockSuffixe2981);
                    ebnfSuffix120=ebnfSuffix();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, ebnfSuffix120.getTree());

                    }
                    break;
                case 2 :
                    // ANTLRParser.g:614:7: ROOT
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    ROOT121=(Token)match(input,ROOT,FOLLOW_ROOT_in_blockSuffixe2995); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ROOT121_tree = (GrammarAST)adaptor.create(ROOT121);
                    adaptor.addChild(root_0, ROOT121_tree);
                    }

                    }
                    break;
                case 3 :
                    // ANTLRParser.g:615:7: IMPLIES
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    IMPLIES122=(Token)match(input,IMPLIES,FOLLOW_IMPLIES_in_blockSuffixe3003); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IMPLIES122_tree = (GrammarAST)adaptor.create(IMPLIES122);
                    adaptor.addChild(root_0, IMPLIES122_tree);
                    }

                    }
                    break;
                case 4 :
                    // ANTLRParser.g:616:7: BANG
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    BANG123=(Token)match(input,BANG,FOLLOW_BANG_in_blockSuffixe3014); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    BANG123_tree = (GrammarAST)adaptor.create(BANG123);
                    adaptor.addChild(root_0, BANG123_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "blockSuffixe"

    public static class ebnfSuffix_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ebnfSuffix"
    // ANTLRParser.g:619:1: ebnfSuffix : ( QUESTION -> OPTIONAL[op] | STAR -> CLOSURE[op] | PLUS -> POSITIVE_CLOSURE[op] );
    public final ANTLRParser.ebnfSuffix_return ebnfSuffix() throws RecognitionException {
        ANTLRParser.ebnfSuffix_return retval = new ANTLRParser.ebnfSuffix_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token QUESTION124=null;
        Token STAR125=null;
        Token PLUS126=null;

        GrammarAST QUESTION124_tree=null;
        GrammarAST STAR125_tree=null;
        GrammarAST PLUS126_tree=null;
        RewriteRuleTokenStream stream_PLUS=new RewriteRuleTokenStream(adaptor,"token PLUS");
        RewriteRuleTokenStream stream_STAR=new RewriteRuleTokenStream(adaptor,"token STAR");
        RewriteRuleTokenStream stream_QUESTION=new RewriteRuleTokenStream(adaptor,"token QUESTION");


        	Token op = input.LT(1);

        try {
            // ANTLRParser.g:623:2: ( QUESTION -> OPTIONAL[op] | STAR -> CLOSURE[op] | PLUS -> POSITIVE_CLOSURE[op] )
            int alt41=3;
            switch ( input.LA(1) ) {
            case QUESTION:
                {
                alt41=1;
                }
                break;
            case STAR:
                {
                alt41=2;
                }
                break;
            case PLUS:
                {
                alt41=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 41, 0, input);

                throw nvae;
            }

            switch (alt41) {
                case 1 :
                    // ANTLRParser.g:623:4: QUESTION
                    {
                    QUESTION124=(Token)match(input,QUESTION,FOLLOW_QUESTION_in_ebnfSuffix3033); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_QUESTION.add(QUESTION124);



                    // AST REWRITE
                    // elements:
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 623:13: -> OPTIONAL[op]
                    {
                        adaptor.addChild(root_0, (GrammarAST)adaptor.create(OPTIONAL, op));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // ANTLRParser.g:624:6: STAR
                    {
                    STAR125=(Token)match(input,STAR,FOLLOW_STAR_in_ebnfSuffix3045); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_STAR.add(STAR125);



                    // AST REWRITE
                    // elements:
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 624:13: -> CLOSURE[op]
                    {
                        adaptor.addChild(root_0, (GrammarAST)adaptor.create(CLOSURE, op));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // ANTLRParser.g:625:7: PLUS
                    {
                    PLUS126=(Token)match(input,PLUS,FOLLOW_PLUS_in_ebnfSuffix3060); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_PLUS.add(PLUS126);



                    // AST REWRITE
                    // elements:
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 625:14: -> POSITIVE_CLOSURE[op]
                    {
                        adaptor.addChild(root_0, (GrammarAST)adaptor.create(POSITIVE_CLOSURE, op));

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ebnfSuffix"

    public static class atom_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "atom"
    // ANTLRParser.g:628:1: atom : ( range ( ROOT | BANG )? | {...}? id DOT ruleref -> ^( DOT id ruleref ) | {...}? id DOT terminal -> ^( DOT id terminal ) | terminal | ruleref | notSet ( ROOT | BANG )? );
    public final ANTLRParser.atom_return atom() throws RecognitionException {
        ANTLRParser.atom_return retval = new ANTLRParser.atom_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token ROOT128=null;
        Token BANG129=null;
        Token DOT131=null;
        Token DOT134=null;
        Token ROOT139=null;
        Token BANG140=null;
        ANTLRParser.range_return range127 = null;

        ANTLRParser.id_return id130 = null;

        ANTLRParser.ruleref_return ruleref132 = null;

        ANTLRParser.id_return id133 = null;

        ANTLRParser.terminal_return terminal135 = null;

        ANTLRParser.terminal_return terminal136 = null;

        ANTLRParser.ruleref_return ruleref137 = null;

        ANTLRParser.notSet_return notSet138 = null;


        GrammarAST ROOT128_tree=null;
        GrammarAST BANG129_tree=null;
        GrammarAST DOT131_tree=null;
        GrammarAST DOT134_tree=null;
        GrammarAST ROOT139_tree=null;
        GrammarAST BANG140_tree=null;
        RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
        RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");
        RewriteRuleSubtreeStream stream_terminal=new RewriteRuleSubtreeStream(adaptor,"rule terminal");
        RewriteRuleSubtreeStream stream_ruleref=new RewriteRuleSubtreeStream(adaptor,"rule ruleref");
        try {
            // ANTLRParser.g:628:5: ( range ( ROOT | BANG )? | {...}? id DOT ruleref -> ^( DOT id ruleref ) | {...}? id DOT terminal -> ^( DOT id terminal ) | terminal | ruleref | notSet ( ROOT | BANG )? )
            int alt44=6;
            alt44 = dfa44.predict(input);
            switch (alt44) {
                case 1 :
                    // ANTLRParser.g:628:7: range ( ROOT | BANG )?
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_range_in_atom3077);
                    range127=range();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, range127.getTree());
                    // ANTLRParser.g:628:13: ( ROOT | BANG )?
                    int alt42=3;
                    int LA42_0 = input.LA(1);

                    if ( (LA42_0==ROOT) ) {
                        alt42=1;
                    }
                    else if ( (LA42_0==BANG) ) {
                        alt42=2;
                    }
                    switch (alt42) {
                        case 1 :
                            // ANTLRParser.g:628:14: ROOT
                            {
                            ROOT128=(Token)match(input,ROOT,FOLLOW_ROOT_in_atom3080); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            ROOT128_tree = (GrammarAST)adaptor.create(ROOT128);
                            root_0 = (GrammarAST)adaptor.becomeRoot(ROOT128_tree, root_0);
                            }

                            }
                            break;
                        case 2 :
                            // ANTLRParser.g:628:22: BANG
                            {
                            BANG129=(Token)match(input,BANG,FOLLOW_BANG_in_atom3085); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            BANG129_tree = (GrammarAST)adaptor.create(BANG129);
                            root_0 = (GrammarAST)adaptor.becomeRoot(BANG129_tree, root_0);
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // ANTLRParser.g:634:6: {...}? id DOT ruleref
                    {
                    if ( !((
                    	    	input.LT(1).getCharPositionInLine()+input.LT(1).getText().length()==
                    	        input.LT(2).getCharPositionInLine() &&
                    	        input.LT(2).getCharPositionInLine()+1==input.LT(3).getCharPositionInLine()
                    	    )) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "atom", "\n\t    \tinput.LT(1).getCharPositionInLine()+input.LT(1).getText().length()==\n\t        input.LT(2).getCharPositionInLine() &&\n\t        input.LT(2).getCharPositionInLine()+1==input.LT(3).getCharPositionInLine()\n\t    ");
                    }
                    pushFollow(FOLLOW_id_in_atom3132);
                    id130=id();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_id.add(id130.getTree());
                    DOT131=(Token)match(input,DOT,FOLLOW_DOT_in_atom3134); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_DOT.add(DOT131);

                    pushFollow(FOLLOW_ruleref_in_atom3136);
                    ruleref132=ruleref();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ruleref.add(ruleref132.getTree());


                    // AST REWRITE
                    // elements: id, DOT, ruleref
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 640:6: -> ^( DOT id ruleref )
                    {
                        // ANTLRParser.g:640:9: ^( DOT id ruleref )
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot(stream_DOT.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_id.nextTree());
                        adaptor.addChild(root_1, stream_ruleref.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // ANTLRParser.g:642:6: {...}? id DOT terminal
                    {
                    if ( !((
                    	    	input.LT(1).getCharPositionInLine()+input.LT(1).getText().length()==
                    	        input.LT(2).getCharPositionInLine() &&
                    	        input.LT(2).getCharPositionInLine()+1==input.LT(3).getCharPositionInLine()
                    	    )) ) {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        throw new FailedPredicateException(input, "atom", "\n\t    \tinput.LT(1).getCharPositionInLine()+input.LT(1).getText().length()==\n\t        input.LT(2).getCharPositionInLine() &&\n\t        input.LT(2).getCharPositionInLine()+1==input.LT(3).getCharPositionInLine()\n\t    ");
                    }
                    pushFollow(FOLLOW_id_in_atom3170);
                    id133=id();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_id.add(id133.getTree());
                    DOT134=(Token)match(input,DOT,FOLLOW_DOT_in_atom3172); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_DOT.add(DOT134);

                    pushFollow(FOLLOW_terminal_in_atom3174);
                    terminal135=terminal();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_terminal.add(terminal135.getTree());


                    // AST REWRITE
                    // elements: id, terminal, DOT
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 648:6: -> ^( DOT id terminal )
                    {
                        // ANTLRParser.g:648:9: ^( DOT id terminal )
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot(stream_DOT.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_id.nextTree());
                        adaptor.addChild(root_1, stream_terminal.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // ANTLRParser.g:649:9: terminal
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_terminal_in_atom3199);
                    terminal136=terminal();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, terminal136.getTree());

                    }
                    break;
                case 5 :
                    // ANTLRParser.g:650:9: ruleref
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_ruleref_in_atom3209);
                    ruleref137=ruleref();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, ruleref137.getTree());

                    }
                    break;
                case 6 :
                    // ANTLRParser.g:651:7: notSet ( ROOT | BANG )?
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_notSet_in_atom3217);
                    notSet138=notSet();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, notSet138.getTree());
                    // ANTLRParser.g:651:14: ( ROOT | BANG )?
                    int alt43=3;
                    int LA43_0 = input.LA(1);

                    if ( (LA43_0==ROOT) ) {
                        alt43=1;
                    }
                    else if ( (LA43_0==BANG) ) {
                        alt43=2;
                    }
                    switch (alt43) {
                        case 1 :
                            // ANTLRParser.g:651:15: ROOT
                            {
                            ROOT139=(Token)match(input,ROOT,FOLLOW_ROOT_in_atom3220); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            ROOT139_tree = (GrammarAST)adaptor.create(ROOT139);
                            root_0 = (GrammarAST)adaptor.becomeRoot(ROOT139_tree, root_0);
                            }

                            }
                            break;
                        case 2 :
                            // ANTLRParser.g:651:21: BANG
                            {
                            BANG140=(Token)match(input,BANG,FOLLOW_BANG_in_atom3223); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            BANG140_tree = (GrammarAST)adaptor.create(BANG140);
                            root_0 = (GrammarAST)adaptor.becomeRoot(BANG140_tree, root_0);
                            }

                            }
                            break;

                    }


                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "atom"

    public static class notSet_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "notSet"
    // ANTLRParser.g:662:1: notSet : ( NOT notTerminal -> ^( NOT notTerminal ) | NOT block -> ^( NOT block ) );
    public final ANTLRParser.notSet_return notSet() throws RecognitionException {
        ANTLRParser.notSet_return retval = new ANTLRParser.notSet_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token NOT141=null;
        Token NOT143=null;
        ANTLRParser.notTerminal_return notTerminal142 = null;

        ANTLRParser.block_return block144 = null;


        GrammarAST NOT141_tree=null;
        GrammarAST NOT143_tree=null;
        RewriteRuleTokenStream stream_NOT=new RewriteRuleTokenStream(adaptor,"token NOT");
        RewriteRuleSubtreeStream stream_notTerminal=new RewriteRuleSubtreeStream(adaptor,"rule notTerminal");
        RewriteRuleSubtreeStream stream_block=new RewriteRuleSubtreeStream(adaptor,"rule block");
        try {
            // ANTLRParser.g:663:5: ( NOT notTerminal -> ^( NOT notTerminal ) | NOT block -> ^( NOT block ) )
            int alt45=2;
            int LA45_0 = input.LA(1);

            if ( (LA45_0==NOT) ) {
                int LA45_1 = input.LA(2);

                if ( (LA45_1==LPAREN) ) {
                    alt45=2;
                }
                else if ( (LA45_1==TOKEN_REF||LA45_1==STRING_LITERAL) ) {
                    alt45=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 45, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 45, 0, input);

                throw nvae;
            }
            switch (alt45) {
                case 1 :
                    // ANTLRParser.g:663:7: NOT notTerminal
                    {
                    NOT141=(Token)match(input,NOT,FOLLOW_NOT_in_notSet3256); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_NOT.add(NOT141);

                    pushFollow(FOLLOW_notTerminal_in_notSet3258);
                    notTerminal142=notTerminal();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_notTerminal.add(notTerminal142.getTree());


                    // AST REWRITE
                    // elements: notTerminal, NOT
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 663:23: -> ^( NOT notTerminal )
                    {
                        // ANTLRParser.g:663:26: ^( NOT notTerminal )
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot(stream_NOT.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_notTerminal.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // ANTLRParser.g:664:7: NOT block
                    {
                    NOT143=(Token)match(input,NOT,FOLLOW_NOT_in_notSet3274); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_NOT.add(NOT143);

                    pushFollow(FOLLOW_block_in_notSet3276);
                    block144=block();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_block.add(block144.getTree());


                    // AST REWRITE
                    // elements: block, NOT
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 664:19: -> ^( NOT block )
                    {
                        // ANTLRParser.g:664:22: ^( NOT block )
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot(stream_NOT.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_block.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "notSet"

    public static class notTerminal_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "notTerminal"
    // ANTLRParser.g:673:1: notTerminal : ( TOKEN_REF | STRING_LITERAL );
    public final ANTLRParser.notTerminal_return notTerminal() throws RecognitionException {
        ANTLRParser.notTerminal_return retval = new ANTLRParser.notTerminal_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token set145=null;

        GrammarAST set145_tree=null;

        try {
            // ANTLRParser.g:674:5: ( TOKEN_REF | STRING_LITERAL )
            // ANTLRParser.g:
            {
            root_0 = (GrammarAST)adaptor.nil();

            set145=(Token)input.LT(1);
            if ( input.LA(1)==TOKEN_REF||input.LA(1)==STRING_LITERAL ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (GrammarAST)adaptor.create(set145));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "notTerminal"

    public static class block_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "block"
    // ANTLRParser.g:685:1: block : LPAREN ( optionsSpec )? ( (ra+= ruleAction )* ( ACTION )? COLON )? altList RPAREN -> ^( BLOCK ( optionsSpec )? ( $ra)* ( ACTION )? altList ) ;
    public final ANTLRParser.block_return block() throws RecognitionException {
        ANTLRParser.block_return retval = new ANTLRParser.block_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token LPAREN146=null;
        Token ACTION148=null;
        Token COLON149=null;
        Token RPAREN151=null;
        List list_ra=null;
        ANTLRParser.optionsSpec_return optionsSpec147 = null;

        ANTLRParser.altList_return altList150 = null;

        RuleReturnScope ra = null;
        GrammarAST LPAREN146_tree=null;
        GrammarAST ACTION148_tree=null;
        GrammarAST COLON149_tree=null;
        GrammarAST RPAREN151_tree=null;
        RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_ACTION=new RewriteRuleTokenStream(adaptor,"token ACTION");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_optionsSpec=new RewriteRuleSubtreeStream(adaptor,"rule optionsSpec");
        RewriteRuleSubtreeStream stream_altList=new RewriteRuleSubtreeStream(adaptor,"rule altList");
        RewriteRuleSubtreeStream stream_ruleAction=new RewriteRuleSubtreeStream(adaptor,"rule ruleAction");
        try {
            // ANTLRParser.g:686:5: ( LPAREN ( optionsSpec )? ( (ra+= ruleAction )* ( ACTION )? COLON )? altList RPAREN -> ^( BLOCK ( optionsSpec )? ( $ra)* ( ACTION )? altList ) )
            // ANTLRParser.g:686:7: LPAREN ( optionsSpec )? ( (ra+= ruleAction )* ( ACTION )? COLON )? altList RPAREN
            {
            LPAREN146=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_block3350); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN146);

            // ANTLRParser.g:691:10: ( optionsSpec )?
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==OPTIONS) ) {
                alt46=1;
            }
            switch (alt46) {
                case 1 :
                    // ANTLRParser.g:691:10: optionsSpec
                    {
                    pushFollow(FOLLOW_optionsSpec_in_block3396);
                    optionsSpec147=optionsSpec();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_optionsSpec.add(optionsSpec147.getTree());

                    }
                    break;

            }

            // ANTLRParser.g:693:10: ( (ra+= ruleAction )* ( ACTION )? COLON )?
            int alt49=2;
            int LA49_0 = input.LA(1);

            if ( (LA49_0==COLON||LA49_0==AT) ) {
                alt49=1;
            }
            else if ( (LA49_0==ACTION) ) {
                int LA49_2 = input.LA(2);

                if ( (LA49_2==COLON) ) {
                    alt49=1;
                }
            }
            switch (alt49) {
                case 1 :
                    // ANTLRParser.g:698:14: (ra+= ruleAction )* ( ACTION )? COLON
                    {
                    // ANTLRParser.g:698:16: (ra+= ruleAction )*
                    loop47:
                    do {
                        int alt47=2;
                        int LA47_0 = input.LA(1);

                        if ( (LA47_0==AT) ) {
                            alt47=1;
                        }


                        switch (alt47) {
                    	case 1 :
                    	    // ANTLRParser.g:698:16: ra+= ruleAction
                    	    {
                    	    pushFollow(FOLLOW_ruleAction_in_block3491);
                    	    ra=ruleAction();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_ruleAction.add(ra.getTree());
                    	    if (list_ra==null) list_ra=new ArrayList();
                    	    list_ra.add(ra.getTree());


                    	    }
                    	    break;

                    	default :
                    	    break loop47;
                        }
                    } while (true);

                    // ANTLRParser.g:699:14: ( ACTION )?
                    int alt48=2;
                    int LA48_0 = input.LA(1);

                    if ( (LA48_0==ACTION) ) {
                        alt48=1;
                    }
                    switch (alt48) {
                        case 1 :
                            // ANTLRParser.g:699:14: ACTION
                            {
                            ACTION148=(Token)match(input,ACTION,FOLLOW_ACTION_in_block3507); if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_ACTION.add(ACTION148);


                            }
                            break;

                    }

                    COLON149=(Token)match(input,COLON,FOLLOW_COLON_in_block3558); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_COLON.add(COLON149);


                    }
                    break;

            }

            pushFollow(FOLLOW_altList_in_block3611);
            altList150=altList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_altList.add(altList150.getTree());
            RPAREN151=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_block3629); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN151);



            // AST REWRITE
            // elements: ACTION, altList, ra, optionsSpec
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels: ra
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_ra=new RewriteRuleSubtreeStream(adaptor,"token ra",list_ra);
            root_0 = (GrammarAST)adaptor.nil();
            // 714:7: -> ^( BLOCK ( optionsSpec )? ( $ra)* ( ACTION )? altList )
            {
                // ANTLRParser.g:714:10: ^( BLOCK ( optionsSpec )? ( $ra)* ( ACTION )? altList )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot(new BlockAST(BLOCK), root_1);

                // ANTLRParser.g:714:28: ( optionsSpec )?
                if ( stream_optionsSpec.hasNext() ) {
                    adaptor.addChild(root_1, stream_optionsSpec.nextTree());

                }
                stream_optionsSpec.reset();
                // ANTLRParser.g:714:41: ( $ra)*
                while ( stream_ra.hasNext() ) {
                    adaptor.addChild(root_1, stream_ra.nextTree());

                }
                stream_ra.reset();
                // ANTLRParser.g:714:46: ( ACTION )?
                if ( stream_ACTION.hasNext() ) {
                    adaptor.addChild(root_1, stream_ACTION.nextNode());

                }
                stream_ACTION.reset();
                adaptor.addChild(root_1, stream_altList.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "block"

    public static class ruleref_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ruleref"
    // ANTLRParser.g:723:1: ruleref : RULE_REF ( ARG_ACTION )? ( (op= ROOT | op= BANG ) -> ^( $op ^( RULE_REF ( ARG_ACTION )? ) ) | -> ^( RULE_REF ( ARG_ACTION )? ) ) ;
    public final ANTLRParser.ruleref_return ruleref() throws RecognitionException {
        ANTLRParser.ruleref_return retval = new ANTLRParser.ruleref_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token op=null;
        Token RULE_REF152=null;
        Token ARG_ACTION153=null;

        GrammarAST op_tree=null;
        GrammarAST RULE_REF152_tree=null;
        GrammarAST ARG_ACTION153_tree=null;
        RewriteRuleTokenStream stream_BANG=new RewriteRuleTokenStream(adaptor,"token BANG");
        RewriteRuleTokenStream stream_ROOT=new RewriteRuleTokenStream(adaptor,"token ROOT");
        RewriteRuleTokenStream stream_RULE_REF=new RewriteRuleTokenStream(adaptor,"token RULE_REF");
        RewriteRuleTokenStream stream_ARG_ACTION=new RewriteRuleTokenStream(adaptor,"token ARG_ACTION");

        try {
            // ANTLRParser.g:724:5: ( RULE_REF ( ARG_ACTION )? ( (op= ROOT | op= BANG ) -> ^( $op ^( RULE_REF ( ARG_ACTION )? ) ) | -> ^( RULE_REF ( ARG_ACTION )? ) ) )
            // ANTLRParser.g:724:7: RULE_REF ( ARG_ACTION )? ( (op= ROOT | op= BANG ) -> ^( $op ^( RULE_REF ( ARG_ACTION )? ) ) | -> ^( RULE_REF ( ARG_ACTION )? ) )
            {
            RULE_REF152=(Token)match(input,RULE_REF,FOLLOW_RULE_REF_in_ruleref3702); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_RULE_REF.add(RULE_REF152);

            // ANTLRParser.g:724:16: ( ARG_ACTION )?
            int alt50=2;
            int LA50_0 = input.LA(1);

            if ( (LA50_0==ARG_ACTION) ) {
                alt50=1;
            }
            switch (alt50) {
                case 1 :
                    // ANTLRParser.g:724:16: ARG_ACTION
                    {
                    ARG_ACTION153=(Token)match(input,ARG_ACTION,FOLLOW_ARG_ACTION_in_ruleref3704); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ARG_ACTION.add(ARG_ACTION153);


                    }
                    break;

            }

            // ANTLRParser.g:725:3: ( (op= ROOT | op= BANG ) -> ^( $op ^( RULE_REF ( ARG_ACTION )? ) ) | -> ^( RULE_REF ( ARG_ACTION )? ) )
            int alt52=2;
            int LA52_0 = input.LA(1);

            if ( (LA52_0==BANG||LA52_0==ROOT) ) {
                alt52=1;
            }
            else if ( (LA52_0==EOF||LA52_0==SEMPRED||LA52_0==ACTION||LA52_0==TEMPLATE||(LA52_0>=SEMI && LA52_0<=RPAREN)||LA52_0==QUESTION||(LA52_0>=STAR && LA52_0<=PLUS)||LA52_0==OR||LA52_0==DOT||(LA52_0>=RARROW && LA52_0<=TREE_BEGIN)||LA52_0==NOT||(LA52_0>=TOKEN_REF && LA52_0<=RULE_REF)||LA52_0==STRING_LITERAL) ) {
                alt52=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 52, 0, input);

                throw nvae;
            }
            switch (alt52) {
                case 1 :
                    // ANTLRParser.g:725:5: (op= ROOT | op= BANG )
                    {
                    // ANTLRParser.g:725:5: (op= ROOT | op= BANG )
                    int alt51=2;
                    int LA51_0 = input.LA(1);

                    if ( (LA51_0==ROOT) ) {
                        alt51=1;
                    }
                    else if ( (LA51_0==BANG) ) {
                        alt51=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 51, 0, input);

                        throw nvae;
                    }
                    switch (alt51) {
                        case 1 :
                            // ANTLRParser.g:725:6: op= ROOT
                            {
                            op=(Token)match(input,ROOT,FOLLOW_ROOT_in_ruleref3714); if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_ROOT.add(op);


                            }
                            break;
                        case 2 :
                            // ANTLRParser.g:725:14: op= BANG
                            {
                            op=(Token)match(input,BANG,FOLLOW_BANG_in_ruleref3718); if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_BANG.add(op);


                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: ARG_ACTION, RULE_REF, op
                    // token labels: op
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleTokenStream stream_op=new RewriteRuleTokenStream(adaptor,"token op",op);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 725:23: -> ^( $op ^( RULE_REF ( ARG_ACTION )? ) )
                    {
                        // ANTLRParser.g:725:26: ^( $op ^( RULE_REF ( ARG_ACTION )? ) )
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot(stream_op.nextNode(), root_1);

                        // ANTLRParser.g:725:32: ^( RULE_REF ( ARG_ACTION )? )
                        {
                        GrammarAST root_2 = (GrammarAST)adaptor.nil();
                        root_2 = (GrammarAST)adaptor.becomeRoot(stream_RULE_REF.nextNode(), root_2);

                        // ANTLRParser.g:725:43: ( ARG_ACTION )?
                        if ( stream_ARG_ACTION.hasNext() ) {
                            adaptor.addChild(root_2, stream_ARG_ACTION.nextNode());

                        }
                        stream_ARG_ACTION.reset();

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // ANTLRParser.g:726:10:
                    {

                    // AST REWRITE
                    // elements: RULE_REF, ARG_ACTION
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 726:10: -> ^( RULE_REF ( ARG_ACTION )? )
                    {
                        // ANTLRParser.g:726:13: ^( RULE_REF ( ARG_ACTION )? )
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot(stream_RULE_REF.nextNode(), root_1);

                        // ANTLRParser.g:726:24: ( ARG_ACTION )?
                        if ( stream_ARG_ACTION.hasNext() ) {
                            adaptor.addChild(root_1, stream_ARG_ACTION.nextNode());

                        }
                        stream_ARG_ACTION.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ruleref"

    public static class range_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "range"
    // ANTLRParser.g:739:1: range : rangeElement RANGE rangeElement ;
    public final ANTLRParser.range_return range() throws RecognitionException {
        ANTLRParser.range_return retval = new ANTLRParser.range_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token RANGE155=null;
        ANTLRParser.rangeElement_return rangeElement154 = null;

        ANTLRParser.rangeElement_return rangeElement156 = null;


        GrammarAST RANGE155_tree=null;

        try {
            // ANTLRParser.g:740:5: ( rangeElement RANGE rangeElement )
            // ANTLRParser.g:740:7: rangeElement RANGE rangeElement
            {
            root_0 = (GrammarAST)adaptor.nil();

            pushFollow(FOLLOW_rangeElement_in_range3783);
            rangeElement154=rangeElement();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, rangeElement154.getTree());
            RANGE155=(Token)match(input,RANGE,FOLLOW_RANGE_in_range3785); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RANGE155_tree = (GrammarAST)adaptor.create(RANGE155);
            root_0 = (GrammarAST)adaptor.becomeRoot(RANGE155_tree, root_0);
            }
            pushFollow(FOLLOW_rangeElement_in_range3788);
            rangeElement156=rangeElement();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, rangeElement156.getTree());

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "range"

    public static class rangeElement_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rangeElement"
    // ANTLRParser.g:751:1: rangeElement : ( STRING_LITERAL | RULE_REF | TOKEN_REF );
    public final ANTLRParser.rangeElement_return rangeElement() throws RecognitionException {
        ANTLRParser.rangeElement_return retval = new ANTLRParser.rangeElement_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token set157=null;

        GrammarAST set157_tree=null;

        try {
            // ANTLRParser.g:752:5: ( STRING_LITERAL | RULE_REF | TOKEN_REF )
            // ANTLRParser.g:
            {
            root_0 = (GrammarAST)adaptor.nil();

            set157=(Token)input.LT(1);
            if ( (input.LA(1)>=TOKEN_REF && input.LA(1)<=RULE_REF)||input.LA(1)==STRING_LITERAL ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (GrammarAST)adaptor.create(set157));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rangeElement"

    public static class terminal_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "terminal"
    // ANTLRParser.g:757:1: terminal : ( TOKEN_REF ( ARG_ACTION )? ( elementOptions )? -> ^( TOKEN_REF ( ARG_ACTION )? ( elementOptions )? ) | STRING_LITERAL ( elementOptions )? -> ^( STRING_LITERAL ( elementOptions )? ) | DOT ( elementOptions )? -> ^( WILDCARD[$DOT] ( elementOptions )? ) ) ( ROOT -> ^( ROOT $terminal) | BANG -> ^( BANG $terminal) )? ;
    public final ANTLRParser.terminal_return terminal() throws RecognitionException {
        ANTLRParser.terminal_return retval = new ANTLRParser.terminal_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token TOKEN_REF158=null;
        Token ARG_ACTION159=null;
        Token STRING_LITERAL161=null;
        Token DOT163=null;
        Token ROOT165=null;
        Token BANG166=null;
        ANTLRParser.elementOptions_return elementOptions160 = null;

        ANTLRParser.elementOptions_return elementOptions162 = null;

        ANTLRParser.elementOptions_return elementOptions164 = null;


        GrammarAST TOKEN_REF158_tree=null;
        GrammarAST ARG_ACTION159_tree=null;
        GrammarAST STRING_LITERAL161_tree=null;
        GrammarAST DOT163_tree=null;
        GrammarAST ROOT165_tree=null;
        GrammarAST BANG166_tree=null;
        RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
        RewriteRuleTokenStream stream_BANG=new RewriteRuleTokenStream(adaptor,"token BANG");
        RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
        RewriteRuleTokenStream stream_ROOT=new RewriteRuleTokenStream(adaptor,"token ROOT");
        RewriteRuleTokenStream stream_TOKEN_REF=new RewriteRuleTokenStream(adaptor,"token TOKEN_REF");
        RewriteRuleTokenStream stream_ARG_ACTION=new RewriteRuleTokenStream(adaptor,"token ARG_ACTION");
        RewriteRuleSubtreeStream stream_elementOptions=new RewriteRuleSubtreeStream(adaptor,"rule elementOptions");
        try {
            // ANTLRParser.g:758:5: ( ( TOKEN_REF ( ARG_ACTION )? ( elementOptions )? -> ^( TOKEN_REF ( ARG_ACTION )? ( elementOptions )? ) | STRING_LITERAL ( elementOptions )? -> ^( STRING_LITERAL ( elementOptions )? ) | DOT ( elementOptions )? -> ^( WILDCARD[$DOT] ( elementOptions )? ) ) ( ROOT -> ^( ROOT $terminal) | BANG -> ^( BANG $terminal) )? )
            // ANTLRParser.g:758:9: ( TOKEN_REF ( ARG_ACTION )? ( elementOptions )? -> ^( TOKEN_REF ( ARG_ACTION )? ( elementOptions )? ) | STRING_LITERAL ( elementOptions )? -> ^( STRING_LITERAL ( elementOptions )? ) | DOT ( elementOptions )? -> ^( WILDCARD[$DOT] ( elementOptions )? ) ) ( ROOT -> ^( ROOT $terminal) | BANG -> ^( BANG $terminal) )?
            {
            // ANTLRParser.g:758:9: ( TOKEN_REF ( ARG_ACTION )? ( elementOptions )? -> ^( TOKEN_REF ( ARG_ACTION )? ( elementOptions )? ) | STRING_LITERAL ( elementOptions )? -> ^( STRING_LITERAL ( elementOptions )? ) | DOT ( elementOptions )? -> ^( WILDCARD[$DOT] ( elementOptions )? ) )
            int alt57=3;
            switch ( input.LA(1) ) {
            case TOKEN_REF:
                {
                alt57=1;
                }
                break;
            case STRING_LITERAL:
                {
                alt57=2;
                }
                break;
            case DOT:
                {
                alt57=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 57, 0, input);

                throw nvae;
            }

            switch (alt57) {
                case 1 :
                    // ANTLRParser.g:759:7: TOKEN_REF ( ARG_ACTION )? ( elementOptions )?
                    {
                    TOKEN_REF158=(Token)match(input,TOKEN_REF,FOLLOW_TOKEN_REF_in_terminal3882); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_TOKEN_REF.add(TOKEN_REF158);

                    // ANTLRParser.g:759:17: ( ARG_ACTION )?
                    int alt53=2;
                    int LA53_0 = input.LA(1);

                    if ( (LA53_0==ARG_ACTION) ) {
                        alt53=1;
                    }
                    switch (alt53) {
                        case 1 :
                            // ANTLRParser.g:759:17: ARG_ACTION
                            {
                            ARG_ACTION159=(Token)match(input,ARG_ACTION,FOLLOW_ARG_ACTION_in_terminal3884); if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_ARG_ACTION.add(ARG_ACTION159);


                            }
                            break;

                    }

                    // ANTLRParser.g:759:29: ( elementOptions )?
                    int alt54=2;
                    int LA54_0 = input.LA(1);

                    if ( (LA54_0==LT) ) {
                        alt54=1;
                    }
                    switch (alt54) {
                        case 1 :
                            // ANTLRParser.g:759:29: elementOptions
                            {
                            pushFollow(FOLLOW_elementOptions_in_terminal3887);
                            elementOptions160=elementOptions();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_elementOptions.add(elementOptions160.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: elementOptions, ARG_ACTION, TOKEN_REF
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 759:45: -> ^( TOKEN_REF ( ARG_ACTION )? ( elementOptions )? )
                    {
                        // ANTLRParser.g:759:48: ^( TOKEN_REF ( ARG_ACTION )? ( elementOptions )? )
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot(new TerminalAST(stream_TOKEN_REF.nextToken()), root_1);

                        // ANTLRParser.g:759:73: ( ARG_ACTION )?
                        if ( stream_ARG_ACTION.hasNext() ) {
                            adaptor.addChild(root_1, stream_ARG_ACTION.nextNode());

                        }
                        stream_ARG_ACTION.reset();
                        // ANTLRParser.g:759:85: ( elementOptions )?
                        if ( stream_elementOptions.hasNext() ) {
                            adaptor.addChild(root_1, stream_elementOptions.nextTree());

                        }
                        stream_elementOptions.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // ANTLRParser.g:760:7: STRING_LITERAL ( elementOptions )?
                    {
                    STRING_LITERAL161=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_terminal3911); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_STRING_LITERAL.add(STRING_LITERAL161);

                    // ANTLRParser.g:760:22: ( elementOptions )?
                    int alt55=2;
                    int LA55_0 = input.LA(1);

                    if ( (LA55_0==LT) ) {
                        alt55=1;
                    }
                    switch (alt55) {
                        case 1 :
                            // ANTLRParser.g:760:22: elementOptions
                            {
                            pushFollow(FOLLOW_elementOptions_in_terminal3913);
                            elementOptions162=elementOptions();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_elementOptions.add(elementOptions162.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: elementOptions, STRING_LITERAL
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 760:41: -> ^( STRING_LITERAL ( elementOptions )? )
                    {
                        // ANTLRParser.g:760:44: ^( STRING_LITERAL ( elementOptions )? )
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot(new TerminalAST(stream_STRING_LITERAL.nextToken()), root_1);

                        // ANTLRParser.g:760:74: ( elementOptions )?
                        if ( stream_elementOptions.hasNext() ) {
                            adaptor.addChild(root_1, stream_elementOptions.nextTree());

                        }
                        stream_elementOptions.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // ANTLRParser.g:766:7: DOT ( elementOptions )?
                    {
                    DOT163=(Token)match(input,DOT,FOLLOW_DOT_in_terminal3960); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_DOT.add(DOT163);

                    // ANTLRParser.g:766:11: ( elementOptions )?
                    int alt56=2;
                    int LA56_0 = input.LA(1);

                    if ( (LA56_0==LT) ) {
                        alt56=1;
                    }
                    switch (alt56) {
                        case 1 :
                            // ANTLRParser.g:766:11: elementOptions
                            {
                            pushFollow(FOLLOW_elementOptions_in_terminal3962);
                            elementOptions164=elementOptions();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_elementOptions.add(elementOptions164.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: elementOptions
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 766:34: -> ^( WILDCARD[$DOT] ( elementOptions )? )
                    {
                        // ANTLRParser.g:766:37: ^( WILDCARD[$DOT] ( elementOptions )? )
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot(new TerminalAST(WILDCARD, DOT163), root_1);

                        // ANTLRParser.g:766:67: ( elementOptions )?
                        if ( stream_elementOptions.hasNext() ) {
                            adaptor.addChild(root_1, stream_elementOptions.nextTree());

                        }
                        stream_elementOptions.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }

            // ANTLRParser.g:768:3: ( ROOT -> ^( ROOT $terminal) | BANG -> ^( BANG $terminal) )?
            int alt58=3;
            int LA58_0 = input.LA(1);

            if ( (LA58_0==ROOT) ) {
                alt58=1;
            }
            else if ( (LA58_0==BANG) ) {
                alt58=2;
            }
            switch (alt58) {
                case 1 :
                    // ANTLRParser.g:768:5: ROOT
                    {
                    ROOT165=(Token)match(input,ROOT,FOLLOW_ROOT_in_terminal3993); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ROOT.add(ROOT165);



                    // AST REWRITE
                    // elements: terminal, ROOT
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 768:19: -> ^( ROOT $terminal)
                    {
                        // ANTLRParser.g:768:22: ^( ROOT $terminal)
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot(stream_ROOT.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_retval.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // ANTLRParser.g:769:5: BANG
                    {
                    BANG166=(Token)match(input,BANG,FOLLOW_BANG_in_terminal4017); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_BANG.add(BANG166);



                    // AST REWRITE
                    // elements: BANG, terminal
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 769:19: -> ^( BANG $terminal)
                    {
                        // ANTLRParser.g:769:22: ^( BANG $terminal)
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot(stream_BANG.nextNode(), root_1);

                        adaptor.addChild(root_1, stream_retval.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "terminal"

    public static class elementOptions_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "elementOptions"
    // ANTLRParser.g:779:2: elementOptions : LT elementOption ( COMMA elementOption )* GT -> ^( ELEMENT_OPTIONS ( elementOption )+ ) ;
    public final ANTLRParser.elementOptions_return elementOptions() throws RecognitionException {
        ANTLRParser.elementOptions_return retval = new ANTLRParser.elementOptions_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token LT167=null;
        Token COMMA169=null;
        Token GT171=null;
        ANTLRParser.elementOption_return elementOption168 = null;

        ANTLRParser.elementOption_return elementOption170 = null;


        GrammarAST LT167_tree=null;
        GrammarAST COMMA169_tree=null;
        GrammarAST GT171_tree=null;
        RewriteRuleTokenStream stream_GT=new RewriteRuleTokenStream(adaptor,"token GT");
        RewriteRuleTokenStream stream_LT=new RewriteRuleTokenStream(adaptor,"token LT");
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_elementOption=new RewriteRuleSubtreeStream(adaptor,"rule elementOption");
        try {
            // ANTLRParser.g:780:5: ( LT elementOption ( COMMA elementOption )* GT -> ^( ELEMENT_OPTIONS ( elementOption )+ ) )
            // ANTLRParser.g:782:7: LT elementOption ( COMMA elementOption )* GT
            {
            LT167=(Token)match(input,LT,FOLLOW_LT_in_elementOptions4081); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_LT.add(LT167);

            pushFollow(FOLLOW_elementOption_in_elementOptions4083);
            elementOption168=elementOption();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_elementOption.add(elementOption168.getTree());
            // ANTLRParser.g:782:24: ( COMMA elementOption )*
            loop59:
            do {
                int alt59=2;
                int LA59_0 = input.LA(1);

                if ( (LA59_0==COMMA) ) {
                    alt59=1;
                }


                switch (alt59) {
            	case 1 :
            	    // ANTLRParser.g:782:25: COMMA elementOption
            	    {
            	    COMMA169=(Token)match(input,COMMA,FOLLOW_COMMA_in_elementOptions4086); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA169);

            	    pushFollow(FOLLOW_elementOption_in_elementOptions4088);
            	    elementOption170=elementOption();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_elementOption.add(elementOption170.getTree());

            	    }
            	    break;

            	default :
            	    break loop59;
                }
            } while (true);

            GT171=(Token)match(input,GT,FOLLOW_GT_in_elementOptions4092); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_GT.add(GT171);



            // AST REWRITE
            // elements: elementOption
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 782:50: -> ^( ELEMENT_OPTIONS ( elementOption )+ )
            {
                // ANTLRParser.g:782:53: ^( ELEMENT_OPTIONS ( elementOption )+ )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(ELEMENT_OPTIONS, "ELEMENT_OPTIONS"), root_1);

                if ( !(stream_elementOption.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_elementOption.hasNext() ) {
                    adaptor.addChild(root_1, stream_elementOption.nextTree());

                }
                stream_elementOption.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "elementOptions"

    public static class elementOption_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "elementOption"
    // ANTLRParser.g:787:1: elementOption : ( qid | id ASSIGN ( qid | STRING_LITERAL ) );
    public final ANTLRParser.elementOption_return elementOption() throws RecognitionException {
        ANTLRParser.elementOption_return retval = new ANTLRParser.elementOption_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token ASSIGN174=null;
        Token STRING_LITERAL176=null;
        ANTLRParser.qid_return qid172 = null;

        ANTLRParser.id_return id173 = null;

        ANTLRParser.qid_return qid175 = null;


        GrammarAST ASSIGN174_tree=null;
        GrammarAST STRING_LITERAL176_tree=null;

        try {
            // ANTLRParser.g:788:5: ( qid | id ASSIGN ( qid | STRING_LITERAL ) )
            int alt61=2;
            switch ( input.LA(1) ) {
            case RULE_REF:
                {
                int LA61_1 = input.LA(2);

                if ( (LA61_1==COMMA||LA61_1==GT||LA61_1==DOT) ) {
                    alt61=1;
                }
                else if ( (LA61_1==ASSIGN) ) {
                    alt61=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 61, 1, input);

                    throw nvae;
                }
                }
                break;
            case TOKEN_REF:
                {
                int LA61_2 = input.LA(2);

                if ( (LA61_2==ASSIGN) ) {
                    alt61=2;
                }
                else if ( (LA61_2==COMMA||LA61_2==GT||LA61_2==DOT) ) {
                    alt61=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 61, 2, input);

                    throw nvae;
                }
                }
                break;
            case TEMPLATE:
                {
                int LA61_3 = input.LA(2);

                if ( (LA61_3==ASSIGN) ) {
                    alt61=2;
                }
                else if ( (LA61_3==COMMA||LA61_3==GT||LA61_3==DOT) ) {
                    alt61=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 61, 3, input);

                    throw nvae;
                }
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 61, 0, input);

                throw nvae;
            }

            switch (alt61) {
                case 1 :
                    // ANTLRParser.g:789:7: qid
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_qid_in_elementOption4127);
                    qid172=qid();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, qid172.getTree());

                    }
                    break;
                case 2 :
                    // ANTLRParser.g:792:7: id ASSIGN ( qid | STRING_LITERAL )
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_id_in_elementOption4149);
                    id173=id();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, id173.getTree());
                    ASSIGN174=(Token)match(input,ASSIGN,FOLLOW_ASSIGN_in_elementOption4151); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ASSIGN174_tree = (GrammarAST)adaptor.create(ASSIGN174);
                    root_0 = (GrammarAST)adaptor.becomeRoot(ASSIGN174_tree, root_0);
                    }
                    // ANTLRParser.g:792:18: ( qid | STRING_LITERAL )
                    int alt60=2;
                    int LA60_0 = input.LA(1);

                    if ( (LA60_0==TEMPLATE||(LA60_0>=TOKEN_REF && LA60_0<=RULE_REF)) ) {
                        alt60=1;
                    }
                    else if ( (LA60_0==STRING_LITERAL) ) {
                        alt60=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 60, 0, input);

                        throw nvae;
                    }
                    switch (alt60) {
                        case 1 :
                            // ANTLRParser.g:792:19: qid
                            {
                            pushFollow(FOLLOW_qid_in_elementOption4155);
                            qid175=qid();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, qid175.getTree());

                            }
                            break;
                        case 2 :
                            // ANTLRParser.g:792:25: STRING_LITERAL
                            {
                            STRING_LITERAL176=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_elementOption4159); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            STRING_LITERAL176_tree = new TerminalAST(STRING_LITERAL176) ;
                            adaptor.addChild(root_0, STRING_LITERAL176_tree);
                            }

                            }
                            break;

                    }


                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "elementOption"

    public static class rewrite_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rewrite"
    // ANTLRParser.g:795:1: rewrite : ( predicatedRewrite )* nakedRewrite -> ( predicatedRewrite )* nakedRewrite ;
    public final ANTLRParser.rewrite_return rewrite() throws RecognitionException {
        ANTLRParser.rewrite_return retval = new ANTLRParser.rewrite_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        ANTLRParser.predicatedRewrite_return predicatedRewrite177 = null;

        ANTLRParser.nakedRewrite_return nakedRewrite178 = null;


        RewriteRuleSubtreeStream stream_predicatedRewrite=new RewriteRuleSubtreeStream(adaptor,"rule predicatedRewrite");
        RewriteRuleSubtreeStream stream_nakedRewrite=new RewriteRuleSubtreeStream(adaptor,"rule nakedRewrite");
        try {
            // ANTLRParser.g:796:2: ( ( predicatedRewrite )* nakedRewrite -> ( predicatedRewrite )* nakedRewrite )
            // ANTLRParser.g:796:4: ( predicatedRewrite )* nakedRewrite
            {
            // ANTLRParser.g:796:4: ( predicatedRewrite )*
            loop62:
            do {
                int alt62=2;
                int LA62_0 = input.LA(1);

                if ( (LA62_0==RARROW) ) {
                    int LA62_1 = input.LA(2);

                    if ( (LA62_1==SEMPRED) ) {
                        alt62=1;
                    }


                }


                switch (alt62) {
            	case 1 :
            	    // ANTLRParser.g:796:4: predicatedRewrite
            	    {
            	    pushFollow(FOLLOW_predicatedRewrite_in_rewrite4177);
            	    predicatedRewrite177=predicatedRewrite();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_predicatedRewrite.add(predicatedRewrite177.getTree());

            	    }
            	    break;

            	default :
            	    break loop62;
                }
            } while (true);

            pushFollow(FOLLOW_nakedRewrite_in_rewrite4180);
            nakedRewrite178=nakedRewrite();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_nakedRewrite.add(nakedRewrite178.getTree());


            // AST REWRITE
            // elements: nakedRewrite, predicatedRewrite
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 796:36: -> ( predicatedRewrite )* nakedRewrite
            {
                // ANTLRParser.g:796:39: ( predicatedRewrite )*
                while ( stream_predicatedRewrite.hasNext() ) {
                    adaptor.addChild(root_0, stream_predicatedRewrite.nextTree());

                }
                stream_predicatedRewrite.reset();
                adaptor.addChild(root_0, stream_nakedRewrite.nextTree());

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rewrite"

    public static class predicatedRewrite_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "predicatedRewrite"
    // ANTLRParser.g:799:1: predicatedRewrite : RARROW SEMPRED rewriteAlt -> {$rewriteAlt.isTemplate}? ^( ST_RESULT[$RARROW] SEMPRED rewriteAlt ) -> ^( RESULT[$RARROW] SEMPRED rewriteAlt ) ;
    public final ANTLRParser.predicatedRewrite_return predicatedRewrite() throws RecognitionException {
        ANTLRParser.predicatedRewrite_return retval = new ANTLRParser.predicatedRewrite_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token RARROW179=null;
        Token SEMPRED180=null;
        ANTLRParser.rewriteAlt_return rewriteAlt181 = null;


        GrammarAST RARROW179_tree=null;
        GrammarAST SEMPRED180_tree=null;
        RewriteRuleTokenStream stream_SEMPRED=new RewriteRuleTokenStream(adaptor,"token SEMPRED");
        RewriteRuleTokenStream stream_RARROW=new RewriteRuleTokenStream(adaptor,"token RARROW");
        RewriteRuleSubtreeStream stream_rewriteAlt=new RewriteRuleSubtreeStream(adaptor,"rule rewriteAlt");
        try {
            // ANTLRParser.g:800:2: ( RARROW SEMPRED rewriteAlt -> {$rewriteAlt.isTemplate}? ^( ST_RESULT[$RARROW] SEMPRED rewriteAlt ) -> ^( RESULT[$RARROW] SEMPRED rewriteAlt ) )
            // ANTLRParser.g:800:4: RARROW SEMPRED rewriteAlt
            {
            RARROW179=(Token)match(input,RARROW,FOLLOW_RARROW_in_predicatedRewrite4198); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_RARROW.add(RARROW179);

            SEMPRED180=(Token)match(input,SEMPRED,FOLLOW_SEMPRED_in_predicatedRewrite4200); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_SEMPRED.add(SEMPRED180);

            pushFollow(FOLLOW_rewriteAlt_in_predicatedRewrite4202);
            rewriteAlt181=rewriteAlt();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rewriteAlt.add(rewriteAlt181.getTree());


            // AST REWRITE
            // elements: rewriteAlt, SEMPRED, SEMPRED, rewriteAlt
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 801:3: -> {$rewriteAlt.isTemplate}? ^( ST_RESULT[$RARROW] SEMPRED rewriteAlt )
            if ((rewriteAlt181!=null?rewriteAlt181.isTemplate:false)) {
                // ANTLRParser.g:801:32: ^( ST_RESULT[$RARROW] SEMPRED rewriteAlt )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(ST_RESULT, RARROW179), root_1);

                adaptor.addChild(root_1, stream_SEMPRED.nextNode());
                adaptor.addChild(root_1, stream_rewriteAlt.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }
            else // 802:3: -> ^( RESULT[$RARROW] SEMPRED rewriteAlt )
            {
                // ANTLRParser.g:802:6: ^( RESULT[$RARROW] SEMPRED rewriteAlt )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(RESULT, RARROW179), root_1);

                adaptor.addChild(root_1, stream_SEMPRED.nextNode());
                adaptor.addChild(root_1, stream_rewriteAlt.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "predicatedRewrite"

    public static class nakedRewrite_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "nakedRewrite"
    // ANTLRParser.g:805:1: nakedRewrite : RARROW rewriteAlt -> {$rewriteAlt.isTemplate}? ^( ST_RESULT[$RARROW] rewriteAlt ) -> ^( RESULT[$RARROW] rewriteAlt ) ;
    public final ANTLRParser.nakedRewrite_return nakedRewrite() throws RecognitionException {
        ANTLRParser.nakedRewrite_return retval = new ANTLRParser.nakedRewrite_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token RARROW182=null;
        ANTLRParser.rewriteAlt_return rewriteAlt183 = null;


        GrammarAST RARROW182_tree=null;
        RewriteRuleTokenStream stream_RARROW=new RewriteRuleTokenStream(adaptor,"token RARROW");
        RewriteRuleSubtreeStream stream_rewriteAlt=new RewriteRuleSubtreeStream(adaptor,"rule rewriteAlt");
        try {
            // ANTLRParser.g:806:2: ( RARROW rewriteAlt -> {$rewriteAlt.isTemplate}? ^( ST_RESULT[$RARROW] rewriteAlt ) -> ^( RESULT[$RARROW] rewriteAlt ) )
            // ANTLRParser.g:806:4: RARROW rewriteAlt
            {
            RARROW182=(Token)match(input,RARROW,FOLLOW_RARROW_in_nakedRewrite4242); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_RARROW.add(RARROW182);

            pushFollow(FOLLOW_rewriteAlt_in_nakedRewrite4244);
            rewriteAlt183=rewriteAlt();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rewriteAlt.add(rewriteAlt183.getTree());


            // AST REWRITE
            // elements: rewriteAlt, rewriteAlt
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 806:22: -> {$rewriteAlt.isTemplate}? ^( ST_RESULT[$RARROW] rewriteAlt )
            if ((rewriteAlt183!=null?rewriteAlt183.isTemplate:false)) {
                // ANTLRParser.g:806:51: ^( ST_RESULT[$RARROW] rewriteAlt )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(ST_RESULT, RARROW182), root_1);

                adaptor.addChild(root_1, stream_rewriteAlt.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }
            else // 807:10: -> ^( RESULT[$RARROW] rewriteAlt )
            {
                // ANTLRParser.g:807:13: ^( RESULT[$RARROW] rewriteAlt )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(RESULT, RARROW182), root_1);

                adaptor.addChild(root_1, stream_rewriteAlt.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "nakedRewrite"

    public static class rewriteAlt_return extends ParserRuleReturnScope {
        public boolean isTemplate;
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rewriteAlt"
    // ANTLRParser.g:812:1: rewriteAlt returns [boolean isTemplate] options {backtrack=true; } : ( rewriteTemplate | rewriteTreeAlt | ETC | -> EPSILON );
    public final ANTLRParser.rewriteAlt_return rewriteAlt() throws RecognitionException {
        ANTLRParser.rewriteAlt_return retval = new ANTLRParser.rewriteAlt_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token ETC186=null;
        ANTLRParser.rewriteTemplate_return rewriteTemplate184 = null;

        ANTLRParser.rewriteTreeAlt_return rewriteTreeAlt185 = null;


        GrammarAST ETC186_tree=null;

        try {
            // ANTLRParser.g:814:5: ( rewriteTemplate | rewriteTreeAlt | ETC | -> EPSILON )
            int alt63=4;
            alt63 = dfa63.predict(input);
            switch (alt63) {
                case 1 :
                    // ANTLRParser.g:815:7: rewriteTemplate
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_rewriteTemplate_in_rewriteAlt4308);
                    rewriteTemplate184=rewriteTemplate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rewriteTemplate184.getTree());
                    if ( state.backtracking==0 ) {
                      retval.isTemplate =true;
                    }

                    }
                    break;
                case 2 :
                    // ANTLRParser.g:821:7: rewriteTreeAlt
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_rewriteTreeAlt_in_rewriteAlt4347);
                    rewriteTreeAlt185=rewriteTreeAlt();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rewriteTreeAlt185.getTree());

                    }
                    break;
                case 3 :
                    // ANTLRParser.g:823:7: ETC
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    ETC186=(Token)match(input,ETC,FOLLOW_ETC_in_rewriteAlt4356); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ETC186_tree = (GrammarAST)adaptor.create(ETC186);
                    adaptor.addChild(root_0, ETC186_tree);
                    }

                    }
                    break;
                case 4 :
                    // ANTLRParser.g:825:27:
                    {

                    // AST REWRITE
                    // elements:
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 825:27: -> EPSILON
                    {
                        adaptor.addChild(root_0, (GrammarAST)adaptor.create(EPSILON, "EPSILON"));

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rewriteAlt"

    public static class rewriteTreeAlt_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rewriteTreeAlt"
    // ANTLRParser.g:828:1: rewriteTreeAlt : ( rewriteTreeElement )+ -> ^( ALT ( rewriteTreeElement )+ ) ;
    public final ANTLRParser.rewriteTreeAlt_return rewriteTreeAlt() throws RecognitionException {
        ANTLRParser.rewriteTreeAlt_return retval = new ANTLRParser.rewriteTreeAlt_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        ANTLRParser.rewriteTreeElement_return rewriteTreeElement187 = null;


        RewriteRuleSubtreeStream stream_rewriteTreeElement=new RewriteRuleSubtreeStream(adaptor,"rule rewriteTreeElement");
        try {
            // ANTLRParser.g:829:5: ( ( rewriteTreeElement )+ -> ^( ALT ( rewriteTreeElement )+ ) )
            // ANTLRParser.g:829:7: ( rewriteTreeElement )+
            {
            // ANTLRParser.g:829:7: ( rewriteTreeElement )+
            int cnt64=0;
            loop64:
            do {
                int alt64=2;
                int LA64_0 = input.LA(1);

                if ( (LA64_0==ACTION||LA64_0==LPAREN||LA64_0==DOLLAR||LA64_0==TREE_BEGIN||(LA64_0>=TOKEN_REF && LA64_0<=RULE_REF)||LA64_0==STRING_LITERAL) ) {
                    alt64=1;
                }


                switch (alt64) {
            	case 1 :
            	    // ANTLRParser.g:829:7: rewriteTreeElement
            	    {
            	    pushFollow(FOLLOW_rewriteTreeElement_in_rewriteTreeAlt4387);
            	    rewriteTreeElement187=rewriteTreeElement();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_rewriteTreeElement.add(rewriteTreeElement187.getTree());

            	    }
            	    break;

            	default :
            	    if ( cnt64 >= 1 ) break loop64;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(64, input);
                        throw eee;
                }
                cnt64++;
            } while (true);



            // AST REWRITE
            // elements: rewriteTreeElement
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 829:27: -> ^( ALT ( rewriteTreeElement )+ )
            {
                // ANTLRParser.g:829:30: ^( ALT ( rewriteTreeElement )+ )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(ALT, "ALT"), root_1);

                if ( !(stream_rewriteTreeElement.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_rewriteTreeElement.hasNext() ) {
                    adaptor.addChild(root_1, stream_rewriteTreeElement.nextTree());

                }
                stream_rewriteTreeElement.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rewriteTreeAlt"

    public static class rewriteTreeElement_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rewriteTreeElement"
    // ANTLRParser.g:832:1: rewriteTreeElement : ( rewriteTreeAtom | rewriteTreeAtom ebnfSuffix -> ^( ebnfSuffix ^( REWRITE_BLOCK ^( ALT rewriteTreeAtom ) ) ) | rewriteTree ( ebnfSuffix -> ^( ebnfSuffix ^( REWRITE_BLOCK ^( ALT rewriteTree ) ) ) | -> rewriteTree ) | rewriteTreeEbnf );
    public final ANTLRParser.rewriteTreeElement_return rewriteTreeElement() throws RecognitionException {
        ANTLRParser.rewriteTreeElement_return retval = new ANTLRParser.rewriteTreeElement_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        ANTLRParser.rewriteTreeAtom_return rewriteTreeAtom188 = null;

        ANTLRParser.rewriteTreeAtom_return rewriteTreeAtom189 = null;

        ANTLRParser.ebnfSuffix_return ebnfSuffix190 = null;

        ANTLRParser.rewriteTree_return rewriteTree191 = null;

        ANTLRParser.ebnfSuffix_return ebnfSuffix192 = null;

        ANTLRParser.rewriteTreeEbnf_return rewriteTreeEbnf193 = null;


        RewriteRuleSubtreeStream stream_rewriteTree=new RewriteRuleSubtreeStream(adaptor,"rule rewriteTree");
        RewriteRuleSubtreeStream stream_rewriteTreeAtom=new RewriteRuleSubtreeStream(adaptor,"rule rewriteTreeAtom");
        RewriteRuleSubtreeStream stream_ebnfSuffix=new RewriteRuleSubtreeStream(adaptor,"rule ebnfSuffix");
        try {
            // ANTLRParser.g:833:2: ( rewriteTreeAtom | rewriteTreeAtom ebnfSuffix -> ^( ebnfSuffix ^( REWRITE_BLOCK ^( ALT rewriteTreeAtom ) ) ) | rewriteTree ( ebnfSuffix -> ^( ebnfSuffix ^( REWRITE_BLOCK ^( ALT rewriteTree ) ) ) | -> rewriteTree ) | rewriteTreeEbnf )
            int alt66=4;
            alt66 = dfa66.predict(input);
            switch (alt66) {
                case 1 :
                    // ANTLRParser.g:833:4: rewriteTreeAtom
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_rewriteTreeAtom_in_rewriteTreeElement4411);
                    rewriteTreeAtom188=rewriteTreeAtom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rewriteTreeAtom188.getTree());

                    }
                    break;
                case 2 :
                    // ANTLRParser.g:834:4: rewriteTreeAtom ebnfSuffix
                    {
                    pushFollow(FOLLOW_rewriteTreeAtom_in_rewriteTreeElement4416);
                    rewriteTreeAtom189=rewriteTreeAtom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rewriteTreeAtom.add(rewriteTreeAtom189.getTree());
                    pushFollow(FOLLOW_ebnfSuffix_in_rewriteTreeElement4418);
                    ebnfSuffix190=ebnfSuffix();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_ebnfSuffix.add(ebnfSuffix190.getTree());


                    // AST REWRITE
                    // elements: rewriteTreeAtom, ebnfSuffix
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 834:31: -> ^( ebnfSuffix ^( REWRITE_BLOCK ^( ALT rewriteTreeAtom ) ) )
                    {
                        // ANTLRParser.g:834:34: ^( ebnfSuffix ^( REWRITE_BLOCK ^( ALT rewriteTreeAtom ) ) )
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot(stream_ebnfSuffix.nextNode(), root_1);

                        // ANTLRParser.g:834:48: ^( REWRITE_BLOCK ^( ALT rewriteTreeAtom ) )
                        {
                        GrammarAST root_2 = (GrammarAST)adaptor.nil();
                        root_2 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(REWRITE_BLOCK, "REWRITE_BLOCK"), root_2);

                        // ANTLRParser.g:834:64: ^( ALT rewriteTreeAtom )
                        {
                        GrammarAST root_3 = (GrammarAST)adaptor.nil();
                        root_3 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(ALT, "ALT"), root_3);

                        adaptor.addChild(root_3, stream_rewriteTreeAtom.nextTree());

                        adaptor.addChild(root_2, root_3);
                        }

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // ANTLRParser.g:835:6: rewriteTree ( ebnfSuffix -> ^( ebnfSuffix ^( REWRITE_BLOCK ^( ALT rewriteTree ) ) ) | -> rewriteTree )
                    {
                    pushFollow(FOLLOW_rewriteTree_in_rewriteTreeElement4443);
                    rewriteTree191=rewriteTree();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rewriteTree.add(rewriteTree191.getTree());
                    // ANTLRParser.g:836:3: ( ebnfSuffix -> ^( ebnfSuffix ^( REWRITE_BLOCK ^( ALT rewriteTree ) ) ) | -> rewriteTree )
                    int alt65=2;
                    int LA65_0 = input.LA(1);

                    if ( (LA65_0==QUESTION||(LA65_0>=STAR && LA65_0<=PLUS)) ) {
                        alt65=1;
                    }
                    else if ( (LA65_0==EOF||LA65_0==ACTION||(LA65_0>=SEMI && LA65_0<=RPAREN)||LA65_0==OR||LA65_0==DOLLAR||(LA65_0>=RARROW && LA65_0<=TREE_BEGIN)||(LA65_0>=TOKEN_REF && LA65_0<=RULE_REF)||LA65_0==STRING_LITERAL) ) {
                        alt65=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 65, 0, input);

                        throw nvae;
                    }
                    switch (alt65) {
                        case 1 :
                            // ANTLRParser.g:836:5: ebnfSuffix
                            {
                            pushFollow(FOLLOW_ebnfSuffix_in_rewriteTreeElement4449);
                            ebnfSuffix192=ebnfSuffix();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_ebnfSuffix.add(ebnfSuffix192.getTree());


                            // AST REWRITE
                            // elements: rewriteTree, ebnfSuffix
                            // token labels:
                            // rule labels: retval
                            // token list labels:
                            // rule list labels:
                            // wildcard labels:
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (GrammarAST)adaptor.nil();
                            // 837:4: -> ^( ebnfSuffix ^( REWRITE_BLOCK ^( ALT rewriteTree ) ) )
                            {
                                // ANTLRParser.g:837:7: ^( ebnfSuffix ^( REWRITE_BLOCK ^( ALT rewriteTree ) ) )
                                {
                                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                                root_1 = (GrammarAST)adaptor.becomeRoot(stream_ebnfSuffix.nextNode(), root_1);

                                // ANTLRParser.g:837:20: ^( REWRITE_BLOCK ^( ALT rewriteTree ) )
                                {
                                GrammarAST root_2 = (GrammarAST)adaptor.nil();
                                root_2 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(REWRITE_BLOCK, "REWRITE_BLOCK"), root_2);

                                // ANTLRParser.g:837:36: ^( ALT rewriteTree )
                                {
                                GrammarAST root_3 = (GrammarAST)adaptor.nil();
                                root_3 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(ALT, "ALT"), root_3);

                                adaptor.addChild(root_3, stream_rewriteTree.nextTree());

                                adaptor.addChild(root_2, root_3);
                                }

                                adaptor.addChild(root_1, root_2);
                                }

                                adaptor.addChild(root_0, root_1);
                                }

                            }

                            retval.tree = root_0;}
                            }
                            break;
                        case 2 :
                            // ANTLRParser.g:838:5:
                            {

                            // AST REWRITE
                            // elements: rewriteTree
                            // token labels:
                            // rule labels: retval
                            // token list labels:
                            // rule list labels:
                            // wildcard labels:
                            if ( state.backtracking==0 ) {
                            retval.tree = root_0;
                            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                            root_0 = (GrammarAST)adaptor.nil();
                            // 838:5: -> rewriteTree
                            {
                                adaptor.addChild(root_0, stream_rewriteTree.nextTree());

                            }

                            retval.tree = root_0;}
                            }
                            break;

                    }


                    }
                    break;
                case 4 :
                    // ANTLRParser.g:840:6: rewriteTreeEbnf
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_rewriteTreeEbnf_in_rewriteTreeElement4488);
                    rewriteTreeEbnf193=rewriteTreeEbnf();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rewriteTreeEbnf193.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rewriteTreeElement"

    public static class rewriteTreeAtom_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rewriteTreeAtom"
    // ANTLRParser.g:843:1: rewriteTreeAtom : ( TOKEN_REF ( elementOptions )? ( ARG_ACTION )? -> ^( TOKEN_REF ( elementOptions )? ( ARG_ACTION )? ) | RULE_REF | STRING_LITERAL ( elementOptions )? -> ^( STRING_LITERAL ( elementOptions )? ) | DOLLAR id -> LABEL[$DOLLAR,$id.text] | ACTION );
    public final ANTLRParser.rewriteTreeAtom_return rewriteTreeAtom() throws RecognitionException {
        ANTLRParser.rewriteTreeAtom_return retval = new ANTLRParser.rewriteTreeAtom_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token TOKEN_REF194=null;
        Token ARG_ACTION196=null;
        Token RULE_REF197=null;
        Token STRING_LITERAL198=null;
        Token DOLLAR200=null;
        Token ACTION202=null;
        ANTLRParser.elementOptions_return elementOptions195 = null;

        ANTLRParser.elementOptions_return elementOptions199 = null;

        ANTLRParser.id_return id201 = null;


        GrammarAST TOKEN_REF194_tree=null;
        GrammarAST ARG_ACTION196_tree=null;
        GrammarAST RULE_REF197_tree=null;
        GrammarAST STRING_LITERAL198_tree=null;
        GrammarAST DOLLAR200_tree=null;
        GrammarAST ACTION202_tree=null;
        RewriteRuleTokenStream stream_DOLLAR=new RewriteRuleTokenStream(adaptor,"token DOLLAR");
        RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
        RewriteRuleTokenStream stream_TOKEN_REF=new RewriteRuleTokenStream(adaptor,"token TOKEN_REF");
        RewriteRuleTokenStream stream_ARG_ACTION=new RewriteRuleTokenStream(adaptor,"token ARG_ACTION");
        RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");
        RewriteRuleSubtreeStream stream_elementOptions=new RewriteRuleSubtreeStream(adaptor,"rule elementOptions");
        try {
            // ANTLRParser.g:844:5: ( TOKEN_REF ( elementOptions )? ( ARG_ACTION )? -> ^( TOKEN_REF ( elementOptions )? ( ARG_ACTION )? ) | RULE_REF | STRING_LITERAL ( elementOptions )? -> ^( STRING_LITERAL ( elementOptions )? ) | DOLLAR id -> LABEL[$DOLLAR,$id.text] | ACTION )
            int alt70=5;
            switch ( input.LA(1) ) {
            case TOKEN_REF:
                {
                alt70=1;
                }
                break;
            case RULE_REF:
                {
                alt70=2;
                }
                break;
            case STRING_LITERAL:
                {
                alt70=3;
                }
                break;
            case DOLLAR:
                {
                alt70=4;
                }
                break;
            case ACTION:
                {
                alt70=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 70, 0, input);

                throw nvae;
            }

            switch (alt70) {
                case 1 :
                    // ANTLRParser.g:844:9: TOKEN_REF ( elementOptions )? ( ARG_ACTION )?
                    {
                    TOKEN_REF194=(Token)match(input,TOKEN_REF,FOLLOW_TOKEN_REF_in_rewriteTreeAtom4504); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_TOKEN_REF.add(TOKEN_REF194);

                    // ANTLRParser.g:844:19: ( elementOptions )?
                    int alt67=2;
                    int LA67_0 = input.LA(1);

                    if ( (LA67_0==LT) ) {
                        alt67=1;
                    }
                    switch (alt67) {
                        case 1 :
                            // ANTLRParser.g:844:19: elementOptions
                            {
                            pushFollow(FOLLOW_elementOptions_in_rewriteTreeAtom4506);
                            elementOptions195=elementOptions();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_elementOptions.add(elementOptions195.getTree());

                            }
                            break;

                    }

                    // ANTLRParser.g:844:35: ( ARG_ACTION )?
                    int alt68=2;
                    int LA68_0 = input.LA(1);

                    if ( (LA68_0==ARG_ACTION) ) {
                        alt68=1;
                    }
                    switch (alt68) {
                        case 1 :
                            // ANTLRParser.g:844:35: ARG_ACTION
                            {
                            ARG_ACTION196=(Token)match(input,ARG_ACTION,FOLLOW_ARG_ACTION_in_rewriteTreeAtom4509); if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_ARG_ACTION.add(ARG_ACTION196);


                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: elementOptions, TOKEN_REF, ARG_ACTION
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 844:47: -> ^( TOKEN_REF ( elementOptions )? ( ARG_ACTION )? )
                    {
                        // ANTLRParser.g:844:50: ^( TOKEN_REF ( elementOptions )? ( ARG_ACTION )? )
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot(new TerminalAST(stream_TOKEN_REF.nextToken()), root_1);

                        // ANTLRParser.g:844:75: ( elementOptions )?
                        if ( stream_elementOptions.hasNext() ) {
                            adaptor.addChild(root_1, stream_elementOptions.nextTree());

                        }
                        stream_elementOptions.reset();
                        // ANTLRParser.g:844:91: ( ARG_ACTION )?
                        if ( stream_ARG_ACTION.hasNext() ) {
                            adaptor.addChild(root_1, stream_ARG_ACTION.nextNode());

                        }
                        stream_ARG_ACTION.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // ANTLRParser.g:845:9: RULE_REF
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    RULE_REF197=(Token)match(input,RULE_REF,FOLLOW_RULE_REF_in_rewriteTreeAtom4536); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RULE_REF197_tree = (GrammarAST)adaptor.create(RULE_REF197);
                    adaptor.addChild(root_0, RULE_REF197_tree);
                    }

                    }
                    break;
                case 3 :
                    // ANTLRParser.g:846:6: STRING_LITERAL ( elementOptions )?
                    {
                    STRING_LITERAL198=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_rewriteTreeAtom4543); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_STRING_LITERAL.add(STRING_LITERAL198);

                    // ANTLRParser.g:846:21: ( elementOptions )?
                    int alt69=2;
                    int LA69_0 = input.LA(1);

                    if ( (LA69_0==LT) ) {
                        alt69=1;
                    }
                    switch (alt69) {
                        case 1 :
                            // ANTLRParser.g:846:21: elementOptions
                            {
                            pushFollow(FOLLOW_elementOptions_in_rewriteTreeAtom4545);
                            elementOptions199=elementOptions();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_elementOptions.add(elementOptions199.getTree());

                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: elementOptions, STRING_LITERAL
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 846:40: -> ^( STRING_LITERAL ( elementOptions )? )
                    {
                        // ANTLRParser.g:846:43: ^( STRING_LITERAL ( elementOptions )? )
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot(new TerminalAST(stream_STRING_LITERAL.nextToken()), root_1);

                        // ANTLRParser.g:846:73: ( elementOptions )?
                        if ( stream_elementOptions.hasNext() ) {
                            adaptor.addChild(root_1, stream_elementOptions.nextTree());

                        }
                        stream_elementOptions.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 4 :
                    // ANTLRParser.g:847:6: DOLLAR id
                    {
                    DOLLAR200=(Token)match(input,DOLLAR,FOLLOW_DOLLAR_in_rewriteTreeAtom4568); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_DOLLAR.add(DOLLAR200);

                    pushFollow(FOLLOW_id_in_rewriteTreeAtom4570);
                    id201=id();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_id.add(id201.getTree());


                    // AST REWRITE
                    // elements:
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 847:16: -> LABEL[$DOLLAR,$id.text]
                    {
                        adaptor.addChild(root_0, (GrammarAST)adaptor.create(LABEL, DOLLAR200, (id201!=null?input.toString(id201.start,id201.stop):null)));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 5 :
                    // ANTLRParser.g:848:4: ACTION
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    ACTION202=(Token)match(input,ACTION,FOLLOW_ACTION_in_rewriteTreeAtom4581); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ACTION202_tree = (GrammarAST)adaptor.create(ACTION202);
                    adaptor.addChild(root_0, ACTION202_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rewriteTreeAtom"

    public static class rewriteTreeEbnf_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rewriteTreeEbnf"
    // ANTLRParser.g:851:1: rewriteTreeEbnf : lp= LPAREN rewriteTreeAlt RPAREN ebnfSuffix -> ^( ebnfSuffix ^( REWRITE_BLOCK[$lp] rewriteTreeAlt ) ) ;
    public final ANTLRParser.rewriteTreeEbnf_return rewriteTreeEbnf() throws RecognitionException {
        ANTLRParser.rewriteTreeEbnf_return retval = new ANTLRParser.rewriteTreeEbnf_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token lp=null;
        Token RPAREN204=null;
        ANTLRParser.rewriteTreeAlt_return rewriteTreeAlt203 = null;

        ANTLRParser.ebnfSuffix_return ebnfSuffix205 = null;


        GrammarAST lp_tree=null;
        GrammarAST RPAREN204_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_rewriteTreeAlt=new RewriteRuleSubtreeStream(adaptor,"rule rewriteTreeAlt");
        RewriteRuleSubtreeStream stream_ebnfSuffix=new RewriteRuleSubtreeStream(adaptor,"rule ebnfSuffix");

            Token firstToken = input.LT(1);

        try {
            // ANTLRParser.g:859:2: (lp= LPAREN rewriteTreeAlt RPAREN ebnfSuffix -> ^( ebnfSuffix ^( REWRITE_BLOCK[$lp] rewriteTreeAlt ) ) )
            // ANTLRParser.g:859:4: lp= LPAREN rewriteTreeAlt RPAREN ebnfSuffix
            {
            lp=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_rewriteTreeEbnf4604); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_LPAREN.add(lp);

            pushFollow(FOLLOW_rewriteTreeAlt_in_rewriteTreeEbnf4606);
            rewriteTreeAlt203=rewriteTreeAlt();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rewriteTreeAlt.add(rewriteTreeAlt203.getTree());
            RPAREN204=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_rewriteTreeEbnf4608); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN204);

            pushFollow(FOLLOW_ebnfSuffix_in_rewriteTreeEbnf4610);
            ebnfSuffix205=ebnfSuffix();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ebnfSuffix.add(ebnfSuffix205.getTree());


            // AST REWRITE
            // elements: ebnfSuffix, rewriteTreeAlt
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 859:47: -> ^( ebnfSuffix ^( REWRITE_BLOCK[$lp] rewriteTreeAlt ) )
            {
                // ANTLRParser.g:859:50: ^( ebnfSuffix ^( REWRITE_BLOCK[$lp] rewriteTreeAlt ) )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot(stream_ebnfSuffix.nextNode(), root_1);

                // ANTLRParser.g:859:63: ^( REWRITE_BLOCK[$lp] rewriteTreeAlt )
                {
                GrammarAST root_2 = (GrammarAST)adaptor.nil();
                root_2 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(REWRITE_BLOCK, lp), root_2);

                adaptor.addChild(root_2, stream_rewriteTreeAlt.nextTree());

                adaptor.addChild(root_1, root_2);
                }

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
            if ( state.backtracking==0 ) {

              	((GrammarAST)retval.tree).getToken().setLine(firstToken.getLine());
              	((GrammarAST)retval.tree).getToken().setCharPositionInLine(firstToken.getCharPositionInLine());

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rewriteTreeEbnf"

    public static class rewriteTree_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rewriteTree"
    // ANTLRParser.g:862:1: rewriteTree : TREE_BEGIN rewriteTreeAtom ( rewriteTreeElement )* RPAREN -> ^( TREE_BEGIN rewriteTreeAtom ( rewriteTreeElement )* ) ;
    public final ANTLRParser.rewriteTree_return rewriteTree() throws RecognitionException {
        ANTLRParser.rewriteTree_return retval = new ANTLRParser.rewriteTree_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token TREE_BEGIN206=null;
        Token RPAREN209=null;
        ANTLRParser.rewriteTreeAtom_return rewriteTreeAtom207 = null;

        ANTLRParser.rewriteTreeElement_return rewriteTreeElement208 = null;


        GrammarAST TREE_BEGIN206_tree=null;
        GrammarAST RPAREN209_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_TREE_BEGIN=new RewriteRuleTokenStream(adaptor,"token TREE_BEGIN");
        RewriteRuleSubtreeStream stream_rewriteTreeAtom=new RewriteRuleSubtreeStream(adaptor,"rule rewriteTreeAtom");
        RewriteRuleSubtreeStream stream_rewriteTreeElement=new RewriteRuleSubtreeStream(adaptor,"rule rewriteTreeElement");
        try {
            // ANTLRParser.g:863:2: ( TREE_BEGIN rewriteTreeAtom ( rewriteTreeElement )* RPAREN -> ^( TREE_BEGIN rewriteTreeAtom ( rewriteTreeElement )* ) )
            // ANTLRParser.g:863:4: TREE_BEGIN rewriteTreeAtom ( rewriteTreeElement )* RPAREN
            {
            TREE_BEGIN206=(Token)match(input,TREE_BEGIN,FOLLOW_TREE_BEGIN_in_rewriteTree4634); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_TREE_BEGIN.add(TREE_BEGIN206);

            pushFollow(FOLLOW_rewriteTreeAtom_in_rewriteTree4636);
            rewriteTreeAtom207=rewriteTreeAtom();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rewriteTreeAtom.add(rewriteTreeAtom207.getTree());
            // ANTLRParser.g:863:31: ( rewriteTreeElement )*
            loop71:
            do {
                int alt71=2;
                int LA71_0 = input.LA(1);

                if ( (LA71_0==ACTION||LA71_0==LPAREN||LA71_0==DOLLAR||LA71_0==TREE_BEGIN||(LA71_0>=TOKEN_REF && LA71_0<=RULE_REF)||LA71_0==STRING_LITERAL) ) {
                    alt71=1;
                }


                switch (alt71) {
            	case 1 :
            	    // ANTLRParser.g:863:31: rewriteTreeElement
            	    {
            	    pushFollow(FOLLOW_rewriteTreeElement_in_rewriteTree4638);
            	    rewriteTreeElement208=rewriteTreeElement();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_rewriteTreeElement.add(rewriteTreeElement208.getTree());

            	    }
            	    break;

            	default :
            	    break loop71;
                }
            } while (true);

            RPAREN209=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_rewriteTree4641); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN209);



            // AST REWRITE
            // elements: rewriteTreeElement, rewriteTreeAtom, TREE_BEGIN
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 864:3: -> ^( TREE_BEGIN rewriteTreeAtom ( rewriteTreeElement )* )
            {
                // ANTLRParser.g:864:6: ^( TREE_BEGIN rewriteTreeAtom ( rewriteTreeElement )* )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot(stream_TREE_BEGIN.nextNode(), root_1);

                adaptor.addChild(root_1, stream_rewriteTreeAtom.nextTree());
                // ANTLRParser.g:864:35: ( rewriteTreeElement )*
                while ( stream_rewriteTreeElement.hasNext() ) {
                    adaptor.addChild(root_1, stream_rewriteTreeElement.nextTree());

                }
                stream_rewriteTreeElement.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rewriteTree"

    public static class rewriteTemplate_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rewriteTemplate"
    // ANTLRParser.g:867:1: rewriteTemplate : ( TEMPLATE LPAREN rewriteTemplateArgs RPAREN (str= DOUBLE_QUOTE_STRING_LITERAL | str= DOUBLE_ANGLE_STRING_LITERAL ) -> ^( TEMPLATE[$TEMPLATE,\"TEMPLATE\"] ( rewriteTemplateArgs )? $str) | rewriteTemplateRef | rewriteIndirectTemplateHead | ACTION );
    public final ANTLRParser.rewriteTemplate_return rewriteTemplate() throws RecognitionException {
        ANTLRParser.rewriteTemplate_return retval = new ANTLRParser.rewriteTemplate_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token str=null;
        Token TEMPLATE210=null;
        Token LPAREN211=null;
        Token RPAREN213=null;
        Token ACTION216=null;
        ANTLRParser.rewriteTemplateArgs_return rewriteTemplateArgs212 = null;

        ANTLRParser.rewriteTemplateRef_return rewriteTemplateRef214 = null;

        ANTLRParser.rewriteIndirectTemplateHead_return rewriteIndirectTemplateHead215 = null;


        GrammarAST str_tree=null;
        GrammarAST TEMPLATE210_tree=null;
        GrammarAST LPAREN211_tree=null;
        GrammarAST RPAREN213_tree=null;
        GrammarAST ACTION216_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_DOUBLE_QUOTE_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token DOUBLE_QUOTE_STRING_LITERAL");
        RewriteRuleTokenStream stream_TEMPLATE=new RewriteRuleTokenStream(adaptor,"token TEMPLATE");
        RewriteRuleTokenStream stream_DOUBLE_ANGLE_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token DOUBLE_ANGLE_STRING_LITERAL");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_rewriteTemplateArgs=new RewriteRuleSubtreeStream(adaptor,"rule rewriteTemplateArgs");
        try {
            // ANTLRParser.g:878:2: ( TEMPLATE LPAREN rewriteTemplateArgs RPAREN (str= DOUBLE_QUOTE_STRING_LITERAL | str= DOUBLE_ANGLE_STRING_LITERAL ) -> ^( TEMPLATE[$TEMPLATE,\"TEMPLATE\"] ( rewriteTemplateArgs )? $str) | rewriteTemplateRef | rewriteIndirectTemplateHead | ACTION )
            int alt73=4;
            alt73 = dfa73.predict(input);
            switch (alt73) {
                case 1 :
                    // ANTLRParser.g:879:3: TEMPLATE LPAREN rewriteTemplateArgs RPAREN (str= DOUBLE_QUOTE_STRING_LITERAL | str= DOUBLE_ANGLE_STRING_LITERAL )
                    {
                    TEMPLATE210=(Token)match(input,TEMPLATE,FOLLOW_TEMPLATE_in_rewriteTemplate4673); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_TEMPLATE.add(TEMPLATE210);

                    LPAREN211=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_rewriteTemplate4675); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN211);

                    pushFollow(FOLLOW_rewriteTemplateArgs_in_rewriteTemplate4677);
                    rewriteTemplateArgs212=rewriteTemplateArgs();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rewriteTemplateArgs.add(rewriteTemplateArgs212.getTree());
                    RPAREN213=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_rewriteTemplate4679); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN213);

                    // ANTLRParser.g:880:3: (str= DOUBLE_QUOTE_STRING_LITERAL | str= DOUBLE_ANGLE_STRING_LITERAL )
                    int alt72=2;
                    int LA72_0 = input.LA(1);

                    if ( (LA72_0==DOUBLE_QUOTE_STRING_LITERAL) ) {
                        alt72=1;
                    }
                    else if ( (LA72_0==DOUBLE_ANGLE_STRING_LITERAL) ) {
                        alt72=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 72, 0, input);

                        throw nvae;
                    }
                    switch (alt72) {
                        case 1 :
                            // ANTLRParser.g:880:5: str= DOUBLE_QUOTE_STRING_LITERAL
                            {
                            str=(Token)match(input,DOUBLE_QUOTE_STRING_LITERAL,FOLLOW_DOUBLE_QUOTE_STRING_LITERAL_in_rewriteTemplate4687); if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_DOUBLE_QUOTE_STRING_LITERAL.add(str);


                            }
                            break;
                        case 2 :
                            // ANTLRParser.g:880:39: str= DOUBLE_ANGLE_STRING_LITERAL
                            {
                            str=(Token)match(input,DOUBLE_ANGLE_STRING_LITERAL,FOLLOW_DOUBLE_ANGLE_STRING_LITERAL_in_rewriteTemplate4693); if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_DOUBLE_ANGLE_STRING_LITERAL.add(str);


                            }
                            break;

                    }



                    // AST REWRITE
                    // elements: TEMPLATE, rewriteTemplateArgs, str
                    // token labels: str
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleTokenStream stream_str=new RewriteRuleTokenStream(adaptor,"token str",str);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 881:3: -> ^( TEMPLATE[$TEMPLATE,\"TEMPLATE\"] ( rewriteTemplateArgs )? $str)
                    {
                        // ANTLRParser.g:881:6: ^( TEMPLATE[$TEMPLATE,\"TEMPLATE\"] ( rewriteTemplateArgs )? $str)
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(TEMPLATE, TEMPLATE210, "TEMPLATE"), root_1);

                        // ANTLRParser.g:881:39: ( rewriteTemplateArgs )?
                        if ( stream_rewriteTemplateArgs.hasNext() ) {
                            adaptor.addChild(root_1, stream_rewriteTemplateArgs.nextTree());

                        }
                        stream_rewriteTemplateArgs.reset();
                        adaptor.addChild(root_1, stream_str.nextNode());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // ANTLRParser.g:884:3: rewriteTemplateRef
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_rewriteTemplateRef_in_rewriteTemplate4719);
                    rewriteTemplateRef214=rewriteTemplateRef();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rewriteTemplateRef214.getTree());

                    }
                    break;
                case 3 :
                    // ANTLRParser.g:887:3: rewriteIndirectTemplateHead
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    pushFollow(FOLLOW_rewriteIndirectTemplateHead_in_rewriteTemplate4728);
                    rewriteIndirectTemplateHead215=rewriteIndirectTemplateHead();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rewriteIndirectTemplateHead215.getTree());

                    }
                    break;
                case 4 :
                    // ANTLRParser.g:890:3: ACTION
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    ACTION216=(Token)match(input,ACTION,FOLLOW_ACTION_in_rewriteTemplate4737); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    ACTION216_tree = (GrammarAST)adaptor.create(ACTION216);
                    adaptor.addChild(root_0, ACTION216_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rewriteTemplate"

    public static class rewriteTemplateRef_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rewriteTemplateRef"
    // ANTLRParser.g:893:1: rewriteTemplateRef : id LPAREN rewriteTemplateArgs RPAREN -> ^( TEMPLATE[$LPAREN,\"TEMPLATE\"] id ( rewriteTemplateArgs )? ) ;
    public final ANTLRParser.rewriteTemplateRef_return rewriteTemplateRef() throws RecognitionException {
        ANTLRParser.rewriteTemplateRef_return retval = new ANTLRParser.rewriteTemplateRef_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token LPAREN218=null;
        Token RPAREN220=null;
        ANTLRParser.id_return id217 = null;

        ANTLRParser.rewriteTemplateArgs_return rewriteTemplateArgs219 = null;


        GrammarAST LPAREN218_tree=null;
        GrammarAST RPAREN220_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");
        RewriteRuleSubtreeStream stream_rewriteTemplateArgs=new RewriteRuleSubtreeStream(adaptor,"rule rewriteTemplateArgs");
        try {
            // ANTLRParser.g:895:2: ( id LPAREN rewriteTemplateArgs RPAREN -> ^( TEMPLATE[$LPAREN,\"TEMPLATE\"] id ( rewriteTemplateArgs )? ) )
            // ANTLRParser.g:895:4: id LPAREN rewriteTemplateArgs RPAREN
            {
            pushFollow(FOLLOW_id_in_rewriteTemplateRef4750);
            id217=id();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_id.add(id217.getTree());
            LPAREN218=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_rewriteTemplateRef4752); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN218);

            pushFollow(FOLLOW_rewriteTemplateArgs_in_rewriteTemplateRef4754);
            rewriteTemplateArgs219=rewriteTemplateArgs();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rewriteTemplateArgs.add(rewriteTemplateArgs219.getTree());
            RPAREN220=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_rewriteTemplateRef4756); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN220);



            // AST REWRITE
            // elements: rewriteTemplateArgs, id
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 896:3: -> ^( TEMPLATE[$LPAREN,\"TEMPLATE\"] id ( rewriteTemplateArgs )? )
            {
                // ANTLRParser.g:896:6: ^( TEMPLATE[$LPAREN,\"TEMPLATE\"] id ( rewriteTemplateArgs )? )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(TEMPLATE, LPAREN218, "TEMPLATE"), root_1);

                adaptor.addChild(root_1, stream_id.nextTree());
                // ANTLRParser.g:896:40: ( rewriteTemplateArgs )?
                if ( stream_rewriteTemplateArgs.hasNext() ) {
                    adaptor.addChild(root_1, stream_rewriteTemplateArgs.nextTree());

                }
                stream_rewriteTemplateArgs.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rewriteTemplateRef"

    public static class rewriteIndirectTemplateHead_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rewriteIndirectTemplateHead"
    // ANTLRParser.g:899:1: rewriteIndirectTemplateHead : lp= LPAREN ACTION RPAREN LPAREN rewriteTemplateArgs RPAREN -> ^( TEMPLATE[$lp,\"TEMPLATE\"] ACTION ( rewriteTemplateArgs )? ) ;
    public final ANTLRParser.rewriteIndirectTemplateHead_return rewriteIndirectTemplateHead() throws RecognitionException {
        ANTLRParser.rewriteIndirectTemplateHead_return retval = new ANTLRParser.rewriteIndirectTemplateHead_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token lp=null;
        Token ACTION221=null;
        Token RPAREN222=null;
        Token LPAREN223=null;
        Token RPAREN225=null;
        ANTLRParser.rewriteTemplateArgs_return rewriteTemplateArgs224 = null;


        GrammarAST lp_tree=null;
        GrammarAST ACTION221_tree=null;
        GrammarAST RPAREN222_tree=null;
        GrammarAST LPAREN223_tree=null;
        GrammarAST RPAREN225_tree=null;
        RewriteRuleTokenStream stream_RPAREN=new RewriteRuleTokenStream(adaptor,"token RPAREN");
        RewriteRuleTokenStream stream_ACTION=new RewriteRuleTokenStream(adaptor,"token ACTION");
        RewriteRuleTokenStream stream_LPAREN=new RewriteRuleTokenStream(adaptor,"token LPAREN");
        RewriteRuleSubtreeStream stream_rewriteTemplateArgs=new RewriteRuleSubtreeStream(adaptor,"rule rewriteTemplateArgs");
        try {
            // ANTLRParser.g:901:2: (lp= LPAREN ACTION RPAREN LPAREN rewriteTemplateArgs RPAREN -> ^( TEMPLATE[$lp,\"TEMPLATE\"] ACTION ( rewriteTemplateArgs )? ) )
            // ANTLRParser.g:901:4: lp= LPAREN ACTION RPAREN LPAREN rewriteTemplateArgs RPAREN
            {
            lp=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_rewriteIndirectTemplateHead4785); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_LPAREN.add(lp);

            ACTION221=(Token)match(input,ACTION,FOLLOW_ACTION_in_rewriteIndirectTemplateHead4787); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ACTION.add(ACTION221);

            RPAREN222=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_rewriteIndirectTemplateHead4789); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN222);

            LPAREN223=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_rewriteIndirectTemplateHead4791); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_LPAREN.add(LPAREN223);

            pushFollow(FOLLOW_rewriteTemplateArgs_in_rewriteIndirectTemplateHead4793);
            rewriteTemplateArgs224=rewriteTemplateArgs();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rewriteTemplateArgs.add(rewriteTemplateArgs224.getTree());
            RPAREN225=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_rewriteIndirectTemplateHead4795); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_RPAREN.add(RPAREN225);



            // AST REWRITE
            // elements: rewriteTemplateArgs, ACTION
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 902:3: -> ^( TEMPLATE[$lp,\"TEMPLATE\"] ACTION ( rewriteTemplateArgs )? )
            {
                // ANTLRParser.g:902:6: ^( TEMPLATE[$lp,\"TEMPLATE\"] ACTION ( rewriteTemplateArgs )? )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(TEMPLATE, lp, "TEMPLATE"), root_1);

                adaptor.addChild(root_1, stream_ACTION.nextNode());
                // ANTLRParser.g:902:40: ( rewriteTemplateArgs )?
                if ( stream_rewriteTemplateArgs.hasNext() ) {
                    adaptor.addChild(root_1, stream_rewriteTemplateArgs.nextTree());

                }
                stream_rewriteTemplateArgs.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rewriteIndirectTemplateHead"

    public static class rewriteTemplateArgs_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rewriteTemplateArgs"
    // ANTLRParser.g:905:1: rewriteTemplateArgs : ( rewriteTemplateArg ( COMMA rewriteTemplateArg )* -> ^( ARGLIST ( rewriteTemplateArg )+ ) | );
    public final ANTLRParser.rewriteTemplateArgs_return rewriteTemplateArgs() throws RecognitionException {
        ANTLRParser.rewriteTemplateArgs_return retval = new ANTLRParser.rewriteTemplateArgs_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token COMMA227=null;
        ANTLRParser.rewriteTemplateArg_return rewriteTemplateArg226 = null;

        ANTLRParser.rewriteTemplateArg_return rewriteTemplateArg228 = null;


        GrammarAST COMMA227_tree=null;
        RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
        RewriteRuleSubtreeStream stream_rewriteTemplateArg=new RewriteRuleSubtreeStream(adaptor,"rule rewriteTemplateArg");
        try {
            // ANTLRParser.g:906:2: ( rewriteTemplateArg ( COMMA rewriteTemplateArg )* -> ^( ARGLIST ( rewriteTemplateArg )+ ) | )
            int alt75=2;
            int LA75_0 = input.LA(1);

            if ( (LA75_0==TEMPLATE||(LA75_0>=TOKEN_REF && LA75_0<=RULE_REF)) ) {
                alt75=1;
            }
            else if ( (LA75_0==RPAREN) ) {
                alt75=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 75, 0, input);

                throw nvae;
            }
            switch (alt75) {
                case 1 :
                    // ANTLRParser.g:906:4: rewriteTemplateArg ( COMMA rewriteTemplateArg )*
                    {
                    pushFollow(FOLLOW_rewriteTemplateArg_in_rewriteTemplateArgs4820);
                    rewriteTemplateArg226=rewriteTemplateArg();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rewriteTemplateArg.add(rewriteTemplateArg226.getTree());
                    // ANTLRParser.g:906:23: ( COMMA rewriteTemplateArg )*
                    loop74:
                    do {
                        int alt74=2;
                        int LA74_0 = input.LA(1);

                        if ( (LA74_0==COMMA) ) {
                            alt74=1;
                        }


                        switch (alt74) {
                    	case 1 :
                    	    // ANTLRParser.g:906:24: COMMA rewriteTemplateArg
                    	    {
                    	    COMMA227=(Token)match(input,COMMA,FOLLOW_COMMA_in_rewriteTemplateArgs4823); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_COMMA.add(COMMA227);

                    	    pushFollow(FOLLOW_rewriteTemplateArg_in_rewriteTemplateArgs4825);
                    	    rewriteTemplateArg228=rewriteTemplateArg();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_rewriteTemplateArg.add(rewriteTemplateArg228.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop74;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: rewriteTemplateArg
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 907:3: -> ^( ARGLIST ( rewriteTemplateArg )+ )
                    {
                        // ANTLRParser.g:907:6: ^( ARGLIST ( rewriteTemplateArg )+ )
                        {
                        GrammarAST root_1 = (GrammarAST)adaptor.nil();
                        root_1 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(ARGLIST, "ARGLIST"), root_1);

                        if ( !(stream_rewriteTemplateArg.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_rewriteTemplateArg.hasNext() ) {
                            adaptor.addChild(root_1, stream_rewriteTemplateArg.nextTree());

                        }
                        stream_rewriteTemplateArg.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // ANTLRParser.g:909:2:
                    {
                    root_0 = (GrammarAST)adaptor.nil();

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rewriteTemplateArgs"

    public static class rewriteTemplateArg_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rewriteTemplateArg"
    // ANTLRParser.g:911:1: rewriteTemplateArg : id ASSIGN ACTION -> ^( ARG[$ASSIGN] id ACTION ) ;
    public final ANTLRParser.rewriteTemplateArg_return rewriteTemplateArg() throws RecognitionException {
        ANTLRParser.rewriteTemplateArg_return retval = new ANTLRParser.rewriteTemplateArg_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token ASSIGN230=null;
        Token ACTION231=null;
        ANTLRParser.id_return id229 = null;


        GrammarAST ASSIGN230_tree=null;
        GrammarAST ACTION231_tree=null;
        RewriteRuleTokenStream stream_ACTION=new RewriteRuleTokenStream(adaptor,"token ACTION");
        RewriteRuleTokenStream stream_ASSIGN=new RewriteRuleTokenStream(adaptor,"token ASSIGN");
        RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");
        try {
            // ANTLRParser.g:912:2: ( id ASSIGN ACTION -> ^( ARG[$ASSIGN] id ACTION ) )
            // ANTLRParser.g:912:6: id ASSIGN ACTION
            {
            pushFollow(FOLLOW_id_in_rewriteTemplateArg4854);
            id229=id();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_id.add(id229.getTree());
            ASSIGN230=(Token)match(input,ASSIGN,FOLLOW_ASSIGN_in_rewriteTemplateArg4856); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ASSIGN.add(ASSIGN230);

            ACTION231=(Token)match(input,ACTION,FOLLOW_ACTION_in_rewriteTemplateArg4858); if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_ACTION.add(ACTION231);



            // AST REWRITE
            // elements: ACTION, id
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 912:23: -> ^( ARG[$ASSIGN] id ACTION )
            {
                // ANTLRParser.g:912:26: ^( ARG[$ASSIGN] id ACTION )
                {
                GrammarAST root_1 = (GrammarAST)adaptor.nil();
                root_1 = (GrammarAST)adaptor.becomeRoot((GrammarAST)adaptor.create(ARG, ASSIGN230), root_1);

                adaptor.addChild(root_1, stream_id.nextTree());
                adaptor.addChild(root_1, stream_ACTION.nextNode());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rewriteTemplateArg"

    public static class id_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "id"
    // ANTLRParser.g:919:1: id : ( RULE_REF -> ID[$RULE_REF] | TOKEN_REF -> ID[$TOKEN_REF] | TEMPLATE -> ID[$TEMPLATE] );
    public final ANTLRParser.id_return id() throws RecognitionException {
        ANTLRParser.id_return retval = new ANTLRParser.id_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token RULE_REF232=null;
        Token TOKEN_REF233=null;
        Token TEMPLATE234=null;

        GrammarAST RULE_REF232_tree=null;
        GrammarAST TOKEN_REF233_tree=null;
        GrammarAST TEMPLATE234_tree=null;
        RewriteRuleTokenStream stream_TEMPLATE=new RewriteRuleTokenStream(adaptor,"token TEMPLATE");
        RewriteRuleTokenStream stream_RULE_REF=new RewriteRuleTokenStream(adaptor,"token RULE_REF");
        RewriteRuleTokenStream stream_TOKEN_REF=new RewriteRuleTokenStream(adaptor,"token TOKEN_REF");

        try {
            // ANTLRParser.g:920:5: ( RULE_REF -> ID[$RULE_REF] | TOKEN_REF -> ID[$TOKEN_REF] | TEMPLATE -> ID[$TEMPLATE] )
            int alt76=3;
            switch ( input.LA(1) ) {
            case RULE_REF:
                {
                alt76=1;
                }
                break;
            case TOKEN_REF:
                {
                alt76=2;
                }
                break;
            case TEMPLATE:
                {
                alt76=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 76, 0, input);

                throw nvae;
            }

            switch (alt76) {
                case 1 :
                    // ANTLRParser.g:920:7: RULE_REF
                    {
                    RULE_REF232=(Token)match(input,RULE_REF,FOLLOW_RULE_REF_in_id4887); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_RULE_REF.add(RULE_REF232);



                    // AST REWRITE
                    // elements:
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 920:17: -> ID[$RULE_REF]
                    {
                        adaptor.addChild(root_0, (GrammarAST)adaptor.create(ID, RULE_REF232));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // ANTLRParser.g:921:7: TOKEN_REF
                    {
                    TOKEN_REF233=(Token)match(input,TOKEN_REF,FOLLOW_TOKEN_REF_in_id4900); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_TOKEN_REF.add(TOKEN_REF233);



                    // AST REWRITE
                    // elements:
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 921:17: -> ID[$TOKEN_REF]
                    {
                        adaptor.addChild(root_0, (GrammarAST)adaptor.create(ID, TOKEN_REF233));

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // ANTLRParser.g:922:7: TEMPLATE
                    {
                    TEMPLATE234=(Token)match(input,TEMPLATE,FOLLOW_TEMPLATE_in_id4912); if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_TEMPLATE.add(TEMPLATE234);



                    // AST REWRITE
                    // elements:
                    // token labels:
                    // rule labels: retval
                    // token list labels:
                    // rule list labels:
                    // wildcard labels:
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (GrammarAST)adaptor.nil();
                    // 922:17: -> ID[$TEMPLATE]
                    {
                        adaptor.addChild(root_0, (GrammarAST)adaptor.create(ID, TEMPLATE234));

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "id"

    public static class qid_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "qid"
    // ANTLRParser.g:925:1: qid : id ( DOT id )* -> ID[$qid.start, $text] ;
    public final ANTLRParser.qid_return qid() throws RecognitionException {
        ANTLRParser.qid_return retval = new ANTLRParser.qid_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token DOT236=null;
        ANTLRParser.id_return id235 = null;

        ANTLRParser.id_return id237 = null;


        GrammarAST DOT236_tree=null;
        RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
        RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");
        try {
            // ANTLRParser.g:925:5: ( id ( DOT id )* -> ID[$qid.start, $text] )
            // ANTLRParser.g:925:7: id ( DOT id )*
            {
            pushFollow(FOLLOW_id_in_qid4935);
            id235=id();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_id.add(id235.getTree());
            // ANTLRParser.g:925:10: ( DOT id )*
            loop77:
            do {
                int alt77=2;
                int LA77_0 = input.LA(1);

                if ( (LA77_0==DOT) ) {
                    alt77=1;
                }


                switch (alt77) {
            	case 1 :
            	    // ANTLRParser.g:925:11: DOT id
            	    {
            	    DOT236=(Token)match(input,DOT,FOLLOW_DOT_in_qid4938); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_DOT.add(DOT236);

            	    pushFollow(FOLLOW_id_in_qid4940);
            	    id237=id();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_id.add(id237.getTree());

            	    }
            	    break;

            	default :
            	    break loop77;
                }
            } while (true);



            // AST REWRITE
            // elements:
            // token labels:
            // rule labels: retval
            // token list labels:
            // rule list labels:
            // wildcard labels:
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (GrammarAST)adaptor.nil();
            // 925:20: -> ID[$qid.start, $text]
            {
                adaptor.addChild(root_0, (GrammarAST)adaptor.create(ID, ((Token)retval.start), input.toString(retval.start,input.LT(-1))));

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "qid"

    public static class alternativeEntry_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "alternativeEntry"
    // ANTLRParser.g:927:1: alternativeEntry : alternative EOF ;
    public final ANTLRParser.alternativeEntry_return alternativeEntry() throws RecognitionException {
        ANTLRParser.alternativeEntry_return retval = new ANTLRParser.alternativeEntry_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token EOF239=null;
        ANTLRParser.alternative_return alternative238 = null;


        GrammarAST EOF239_tree=null;

        try {
            // ANTLRParser.g:927:18: ( alternative EOF )
            // ANTLRParser.g:927:20: alternative EOF
            {
            root_0 = (GrammarAST)adaptor.nil();

            pushFollow(FOLLOW_alternative_in_alternativeEntry4956);
            alternative238=alternative();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, alternative238.getTree());
            EOF239=(Token)match(input,EOF,FOLLOW_EOF_in_alternativeEntry4958); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            EOF239_tree = (GrammarAST)adaptor.create(EOF239);
            adaptor.addChild(root_0, EOF239_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "alternativeEntry"

    public static class elementEntry_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "elementEntry"
    // ANTLRParser.g:928:1: elementEntry : element EOF ;
    public final ANTLRParser.elementEntry_return elementEntry() throws RecognitionException {
        ANTLRParser.elementEntry_return retval = new ANTLRParser.elementEntry_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token EOF241=null;
        ANTLRParser.element_return element240 = null;


        GrammarAST EOF241_tree=null;

        try {
            // ANTLRParser.g:928:14: ( element EOF )
            // ANTLRParser.g:928:16: element EOF
            {
            root_0 = (GrammarAST)adaptor.nil();

            pushFollow(FOLLOW_element_in_elementEntry4967);
            element240=element();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, element240.getTree());
            EOF241=(Token)match(input,EOF,FOLLOW_EOF_in_elementEntry4969); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            EOF241_tree = (GrammarAST)adaptor.create(EOF241);
            adaptor.addChild(root_0, EOF241_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "elementEntry"

    public static class ruleEntry_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "ruleEntry"
    // ANTLRParser.g:929:1: ruleEntry : rule EOF ;
    public final ANTLRParser.ruleEntry_return ruleEntry() throws RecognitionException {
        ANTLRParser.ruleEntry_return retval = new ANTLRParser.ruleEntry_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token EOF243=null;
        ANTLRParser.rule_return rule242 = null;


        GrammarAST EOF243_tree=null;

        try {
            // ANTLRParser.g:929:11: ( rule EOF )
            // ANTLRParser.g:929:13: rule EOF
            {
            root_0 = (GrammarAST)adaptor.nil();

            pushFollow(FOLLOW_rule_in_ruleEntry4977);
            rule242=rule();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, rule242.getTree());
            EOF243=(Token)match(input,EOF,FOLLOW_EOF_in_ruleEntry4979); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            EOF243_tree = (GrammarAST)adaptor.create(EOF243);
            adaptor.addChild(root_0, EOF243_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "ruleEntry"

    public static class blockEntry_return extends ParserRuleReturnScope {
        GrammarAST tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "blockEntry"
    // ANTLRParser.g:930:1: blockEntry : block EOF ;
    public final ANTLRParser.blockEntry_return blockEntry() throws RecognitionException {
        ANTLRParser.blockEntry_return retval = new ANTLRParser.blockEntry_return();
        retval.start = input.LT(1);

        GrammarAST root_0 = null;

        Token EOF245=null;
        ANTLRParser.block_return block244 = null;


        GrammarAST EOF245_tree=null;

        try {
            // ANTLRParser.g:930:12: ( block EOF )
            // ANTLRParser.g:930:14: block EOF
            {
            root_0 = (GrammarAST)adaptor.nil();

            pushFollow(FOLLOW_block_in_blockEntry4987);
            block244=block();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, block244.getTree());
            EOF245=(Token)match(input,EOF,FOLLOW_EOF_in_blockEntry4989); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            EOF245_tree = (GrammarAST)adaptor.create(EOF245);
            adaptor.addChild(root_0, EOF245_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (GrammarAST)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (GrammarAST)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "blockEntry"

    // $ANTLR start synpred1_ANTLRParser
    public final void synpred1_ANTLRParser_fragment() throws RecognitionException {
        // ANTLRParser.g:815:7: ( rewriteTemplate )
        // ANTLRParser.g:815:7: rewriteTemplate
        {
        pushFollow(FOLLOW_rewriteTemplate_in_synpred1_ANTLRParser4308);
        rewriteTemplate();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_ANTLRParser

    // $ANTLR start synpred2_ANTLRParser
    public final void synpred2_ANTLRParser_fragment() throws RecognitionException {
        // ANTLRParser.g:821:7: ( rewriteTreeAlt )
        // ANTLRParser.g:821:7: rewriteTreeAlt
        {
        pushFollow(FOLLOW_rewriteTreeAlt_in_synpred2_ANTLRParser4347);
        rewriteTreeAlt();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_ANTLRParser

    // Delegated rules

    public final boolean synpred1_ANTLRParser() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_ANTLRParser_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred2_ANTLRParser() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_ANTLRParser_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA35 dfa35 = new DFA35(this);
    protected DFA44 dfa44 = new DFA44(this);
    protected DFA63 dfa63 = new DFA63(this);
    protected DFA66 dfa66 = new DFA66(this);
    protected DFA73 dfa73 = new DFA73(this);
    static final String DFA35_eotS =
        "\12\uffff";
    static final String DFA35_eofS =
        "\1\uffff\2\4\7\uffff";
    static final String DFA35_minS =
        "\3\4\1\55\6\uffff";
    static final String DFA35_maxS =
        "\3\103\1\66\6\uffff";
    static final String DFA35_acceptS =
        "\4\uffff\1\2\1\3\1\4\1\5\1\6\1\1";
    static final String DFA35_specialS =
        "\12\uffff}>";
    static final String[] DFA35_transitionS = {
            "\1\7\13\uffff\1\6\22\uffff\1\3\4\uffff\1\5\15\uffff\1\4\3\uffff"+
            "\1\10\1\uffff\1\4\1\uffff\1\2\1\1\3\uffff\1\4",
            "\1\4\11\uffff\1\4\1\uffff\1\4\22\uffff\1\4\3\uffff\3\4\3\uffff"+
            "\1\11\4\4\1\11\2\4\1\uffff\2\4\1\uffff\2\4\1\uffff\1\4\1\uffff"+
            "\2\4\3\uffff\1\4",
            "\1\4\11\uffff\1\4\1\uffff\1\4\22\uffff\1\4\3\uffff\3\4\1\uffff"+
            "\1\4\1\uffff\1\11\4\4\1\11\2\4\1\uffff\2\4\1\uffff\2\4\1\uffff"+
            "\1\4\1\uffff\2\4\3\uffff\1\4",
            "\1\11\4\uffff\1\11\3\uffff\1\4",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA35_eot = DFA.unpackEncodedString(DFA35_eotS);
    static final short[] DFA35_eof = DFA.unpackEncodedString(DFA35_eofS);
    static final char[] DFA35_min = DFA.unpackEncodedStringToUnsignedChars(DFA35_minS);
    static final char[] DFA35_max = DFA.unpackEncodedStringToUnsignedChars(DFA35_maxS);
    static final short[] DFA35_accept = DFA.unpackEncodedString(DFA35_acceptS);
    static final short[] DFA35_special = DFA.unpackEncodedString(DFA35_specialS);
    static final short[][] DFA35_transition;

    static {
        int numStates = DFA35_transitionS.length;
        DFA35_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA35_transition[i] = DFA.unpackEncodedString(DFA35_transitionS[i]);
        }
    }

    class DFA35 extends DFA {

        public DFA35(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 35;
            this.eot = DFA35_eot;
            this.eof = DFA35_eof;
            this.min = DFA35_min;
            this.max = DFA35_max;
            this.accept = DFA35_accept;
            this.special = DFA35_special;
            this.transition = DFA35_transition;
        }
        public String getDescription() {
            return "556:1: element : ( labeledElement ( ebnfSuffix -> ^( ebnfSuffix ^( BLOCK ^( ALT labeledElement ) ) ) | -> labeledElement ) | atom ( ebnfSuffix -> ^( ebnfSuffix ^( BLOCK ^( ALT atom ) ) ) | -> atom ) | ebnf | ACTION | SEMPRED ( IMPLIES -> GATED_SEMPRED[$IMPLIES] | -> SEMPRED ) | treeSpec ( ebnfSuffix -> ^( ebnfSuffix ^( BLOCK ^( ALT treeSpec ) ) ) | -> treeSpec ) );";
        }
    }
    static final String DFA44_eotS =
        "\26\uffff";
    static final String DFA44_eofS =
        "\1\uffff\1\10\2\5\3\uffff\1\10\2\uffff\1\5\13\uffff";
    static final String DFA44_minS =
        "\1\43\3\4\1\66\2\uffff\1\4\2\uffff\1\4\1\66\10\0\2\uffff";
    static final String DFA44_maxS =
        "\4\103\1\66\2\uffff\1\103\2\uffff\2\103\10\0\2\uffff";
    static final String DFA44_acceptS =
        "\5\uffff\1\4\1\6\1\uffff\1\5\1\1\12\uffff\1\3\1\2";
    static final String DFA44_specialS =
        "\14\uffff\1\7\1\6\1\5\1\3\1\4\1\1\1\2\1\0\2\uffff}>";
    static final String[] DFA44_transitionS = {
            "\1\4\22\uffff\1\5\5\uffff\1\6\1\uffff\1\2\1\1\3\uffff\1\3",
            "\1\10\11\uffff\1\10\1\uffff\1\10\22\uffff\1\10\3\uffff\3\10"+
            "\4\uffff\4\10\1\uffff\2\10\1\uffff\1\7\1\11\1\uffff\2\10\1\uffff"+
            "\1\10\1\uffff\2\10\3\uffff\1\10",
            "\1\5\11\uffff\1\5\1\uffff\1\5\22\uffff\1\5\3\uffff\3\5\1\uffff"+
            "\1\5\2\uffff\4\5\1\uffff\2\5\1\uffff\1\12\1\11\1\uffff\2\5\1"+
            "\uffff\1\5\1\uffff\2\5\3\uffff\1\5",
            "\1\5\13\uffff\1\5\22\uffff\1\5\3\uffff\3\5\1\uffff\1\5\2\uffff"+
            "\4\5\1\uffff\2\5\1\uffff\1\5\1\11\1\uffff\2\5\1\uffff\1\5\1"+
            "\uffff\2\5\3\uffff\1\5",
            "\1\13",
            "",
            "",
            "\1\10\13\uffff\1\10\22\uffff\1\10\3\uffff\3\10\1\uffff\1\10"+
            "\2\uffff\4\10\1\uffff\2\10\1\uffff\1\17\2\uffff\2\10\1\uffff"+
            "\1\10\1\uffff\1\15\1\14\3\uffff\1\16",
            "",
            "",
            "\1\5\13\uffff\1\5\22\uffff\1\5\3\uffff\3\5\1\uffff\1\5\2\uffff"+
            "\4\5\1\uffff\2\5\1\uffff\1\23\2\uffff\2\5\1\uffff\1\5\1\uffff"+
            "\1\21\1\20\3\uffff\1\22",
            "\1\24\7\uffff\1\24\1\25\3\uffff\1\24",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA44_eot = DFA.unpackEncodedString(DFA44_eotS);
    static final short[] DFA44_eof = DFA.unpackEncodedString(DFA44_eofS);
    static final char[] DFA44_min = DFA.unpackEncodedStringToUnsignedChars(DFA44_minS);
    static final char[] DFA44_max = DFA.unpackEncodedStringToUnsignedChars(DFA44_maxS);
    static final short[] DFA44_accept = DFA.unpackEncodedString(DFA44_acceptS);
    static final short[] DFA44_special = DFA.unpackEncodedString(DFA44_specialS);
    static final short[][] DFA44_transition;

    static {
        int numStates = DFA44_transitionS.length;
        DFA44_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA44_transition[i] = DFA.unpackEncodedString(DFA44_transitionS[i]);
        }
    }

    class DFA44 extends DFA {

        public DFA44(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 44;
            this.eot = DFA44_eot;
            this.eof = DFA44_eof;
            this.min = DFA44_min;
            this.max = DFA44_max;
            this.accept = DFA44_accept;
            this.special = DFA44_special;
            this.transition = DFA44_transition;
        }
        public String getDescription() {
            return "628:1: atom : ( range ( ROOT | BANG )? | {...}? id DOT ruleref -> ^( DOT id ruleref ) | {...}? id DOT terminal -> ^( DOT id terminal ) | terminal | ruleref | notSet ( ROOT | BANG )? );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 :
                        int LA44_19 = input.LA(1);


                        int index44_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((
                        	    	input.LT(1).getCharPositionInLine()+input.LT(1).getText().length()==
                        	        input.LT(2).getCharPositionInLine() &&
                        	        input.LT(2).getCharPositionInLine()+1==input.LT(3).getCharPositionInLine()
                        	    )) ) {s = 20;}

                        else if ( (true) ) {s = 5;}


                        input.seek(index44_19);
                        if ( s>=0 ) return s;
                        break;
                    case 1 :
                        int LA44_17 = input.LA(1);


                        int index44_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((
                        	    	input.LT(1).getCharPositionInLine()+input.LT(1).getText().length()==
                        	        input.LT(2).getCharPositionInLine() &&
                        	        input.LT(2).getCharPositionInLine()+1==input.LT(3).getCharPositionInLine()
                        	    )) ) {s = 20;}

                        else if ( (true) ) {s = 5;}


                        input.seek(index44_17);
                        if ( s>=0 ) return s;
                        break;
                    case 2 :
                        int LA44_18 = input.LA(1);


                        int index44_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((
                        	    	input.LT(1).getCharPositionInLine()+input.LT(1).getText().length()==
                        	        input.LT(2).getCharPositionInLine() &&
                        	        input.LT(2).getCharPositionInLine()+1==input.LT(3).getCharPositionInLine()
                        	    )) ) {s = 20;}

                        else if ( (true) ) {s = 5;}


                        input.seek(index44_18);
                        if ( s>=0 ) return s;
                        break;
                    case 3 :
                        int LA44_15 = input.LA(1);


                        int index44_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((
                        	    	input.LT(1).getCharPositionInLine()+input.LT(1).getText().length()==
                        	        input.LT(2).getCharPositionInLine() &&
                        	        input.LT(2).getCharPositionInLine()+1==input.LT(3).getCharPositionInLine()
                        	    )) ) {s = 20;}

                        else if ( (true) ) {s = 8;}


                        input.seek(index44_15);
                        if ( s>=0 ) return s;
                        break;
                    case 4 :
                        int LA44_16 = input.LA(1);


                        int index44_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((
                        	    	input.LT(1).getCharPositionInLine()+input.LT(1).getText().length()==
                        	        input.LT(2).getCharPositionInLine() &&
                        	        input.LT(2).getCharPositionInLine()+1==input.LT(3).getCharPositionInLine()
                        	    )) ) {s = 21;}

                        else if ( (true) ) {s = 5;}


                        input.seek(index44_16);
                        if ( s>=0 ) return s;
                        break;
                    case 5 :
                        int LA44_14 = input.LA(1);


                        int index44_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((
                        	    	input.LT(1).getCharPositionInLine()+input.LT(1).getText().length()==
                        	        input.LT(2).getCharPositionInLine() &&
                        	        input.LT(2).getCharPositionInLine()+1==input.LT(3).getCharPositionInLine()
                        	    )) ) {s = 20;}

                        else if ( (true) ) {s = 8;}


                        input.seek(index44_14);
                        if ( s>=0 ) return s;
                        break;
                    case 6 :
                        int LA44_13 = input.LA(1);


                        int index44_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((
                        	    	input.LT(1).getCharPositionInLine()+input.LT(1).getText().length()==
                        	        input.LT(2).getCharPositionInLine() &&
                        	        input.LT(2).getCharPositionInLine()+1==input.LT(3).getCharPositionInLine()
                        	    )) ) {s = 20;}

                        else if ( (true) ) {s = 8;}


                        input.seek(index44_13);
                        if ( s>=0 ) return s;
                        break;
                    case 7 :
                        int LA44_12 = input.LA(1);


                        int index44_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((
                        	    	input.LT(1).getCharPositionInLine()+input.LT(1).getText().length()==
                        	        input.LT(2).getCharPositionInLine() &&
                        	        input.LT(2).getCharPositionInLine()+1==input.LT(3).getCharPositionInLine()
                        	    )) ) {s = 21;}

                        else if ( (true) ) {s = 8;}


                        input.seek(index44_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 44, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA63_eotS =
        "\16\uffff";
    static final String DFA63_eofS =
        "\1\10\1\uffff\2\6\12\uffff";
    static final String DFA63_minS =
        "\1\20\1\uffff\1\20\1\16\1\20\1\0\3\uffff\3\20\1\16\1\50";
    static final String DFA63_maxS =
        "\1\103\1\uffff\3\103\1\0\3\uffff\4\103\1\61";
    static final String DFA63_acceptS =
        "\1\uffff\1\1\4\uffff\1\2\1\3\1\4\5\uffff";
    static final String DFA63_specialS =
        "\5\uffff\1\0\10\uffff}>";
    static final String[] DFA63_transitionS = {
            "\1\5\22\uffff\1\1\3\uffff\1\10\1\4\1\10\11\uffff\1\10\1\uffff"+
            "\1\6\2\uffff\1\7\1\10\1\6\3\uffff\1\3\1\2\3\uffff\1\6",
            "",
            "\1\6\26\uffff\1\6\1\11\1\6\4\uffff\1\6\1\uffff\2\6\1\uffff"+
            "\1\6\1\uffff\1\6\3\uffff\2\6\3\uffff\2\6\3\uffff\1\6",
            "\1\6\1\uffff\1\6\26\uffff\1\6\1\11\1\6\1\uffff\1\6\2\uffff"+
            "\1\6\1\uffff\2\6\1\uffff\1\6\1\uffff\1\6\3\uffff\2\6\3\uffff"+
            "\2\6\3\uffff\1\6",
            "\1\12\27\uffff\1\6\14\uffff\1\6\4\uffff\1\6\3\uffff\2\6\3\uffff"+
            "\1\6",
            "\1\uffff",
            "",
            "",
            "",
            "\1\6\22\uffff\1\1\4\uffff\1\6\1\1\13\uffff\1\6\4\uffff\1\6"+
            "\3\uffff\1\14\1\13\3\uffff\1\6",
            "\1\6\27\uffff\1\6\1\15\4\uffff\1\6\1\uffff\2\6\3\uffff\1\6"+
            "\4\uffff\1\6\3\uffff\2\6\3\uffff\1\6",
            "\1\6\27\uffff\2\6\3\uffff\1\1\1\6\1\uffff\2\6\3\uffff\1\6\4"+
            "\uffff\1\6\3\uffff\2\6\3\uffff\1\6",
            "\1\6\1\uffff\1\6\27\uffff\2\6\1\uffff\1\6\1\uffff\1\1\1\6\1"+
            "\uffff\2\6\3\uffff\1\6\4\uffff\1\6\3\uffff\2\6\3\uffff\1\6",
            "\1\1\5\uffff\1\6\1\uffff\2\6"
    };

    static final short[] DFA63_eot = DFA.unpackEncodedString(DFA63_eotS);
    static final short[] DFA63_eof = DFA.unpackEncodedString(DFA63_eofS);
    static final char[] DFA63_min = DFA.unpackEncodedStringToUnsignedChars(DFA63_minS);
    static final char[] DFA63_max = DFA.unpackEncodedStringToUnsignedChars(DFA63_maxS);
    static final short[] DFA63_accept = DFA.unpackEncodedString(DFA63_acceptS);
    static final short[] DFA63_special = DFA.unpackEncodedString(DFA63_specialS);
    static final short[][] DFA63_transition;

    static {
        int numStates = DFA63_transitionS.length;
        DFA63_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA63_transition[i] = DFA.unpackEncodedString(DFA63_transitionS[i]);
        }
    }

    class DFA63 extends DFA {

        public DFA63(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 63;
            this.eot = DFA63_eot;
            this.eof = DFA63_eof;
            this.min = DFA63_min;
            this.max = DFA63_max;
            this.accept = DFA63_accept;
            this.special = DFA63_special;
            this.transition = DFA63_transition;
        }
        public String getDescription() {
            return "812:1: rewriteAlt returns [boolean isTemplate] options {backtrack=true; } : ( rewriteTemplate | rewriteTreeAlt | ETC | -> EPSILON );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 :
                        int LA63_5 = input.LA(1);


                        int index63_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_ANTLRParser()) ) {s = 1;}

                        else if ( (synpred2_ANTLRParser()) ) {s = 6;}


                        input.seek(index63_5);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 63, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA66_eotS =
        "\124\uffff";
    static final String DFA66_eofS =
        "\1\uffff\3\13\1\uffff\1\13\3\uffff\1\13\3\uffff\3\13\10\uffff\1"+
        "\13\4\uffff\1\13\66\uffff";
    static final String DFA66_minS =
        "\1\20\1\16\2\20\1\43\1\20\2\uffff\1\43\1\20\2\uffff\1\43\3\20\6"+
        "\46\2\43\1\16\4\43\1\20\24\46\6\43\24\46\2\43\6\46";
    static final String DFA66_maxS =
        "\4\103\1\77\1\103\2\uffff\1\77\1\103\2\uffff\1\77\3\103\6\66\2\77"+
        "\3\103\2\77\1\103\11\66\1\54\3\66\1\54\6\66\1\77\1\103\3\77\1\103"+
        "\6\66\1\54\14\66\1\54\2\77\6\66";
    static final String DFA66_acceptS =
        "\6\uffff\1\3\1\4\2\uffff\1\2\1\1\110\uffff";
    static final String DFA66_specialS =
        "\124\uffff}>";
    static final String[] DFA66_transitionS = {
            "\1\5\27\uffff\1\7\14\uffff\1\4\4\uffff\1\6\3\uffff\1\1\1\2\3"+
            "\uffff\1\3",
            "\1\11\1\uffff\1\13\26\uffff\3\13\1\uffff\1\10\2\uffff\1\12"+
            "\1\uffff\2\12\1\uffff\1\13\1\uffff\1\13\3\uffff\2\13\3\uffff"+
            "\2\13\3\uffff\1\13",
            "\1\13\26\uffff\3\13\4\uffff\1\12\1\uffff\2\12\1\uffff\1\13"+
            "\1\uffff\1\13\3\uffff\2\13\3\uffff\2\13\3\uffff\1\13",
            "\1\13\26\uffff\3\13\1\uffff\1\14\2\uffff\1\12\1\uffff\2\12"+
            "\1\uffff\1\13\1\uffff\1\13\3\uffff\2\13\3\uffff\2\13\3\uffff"+
            "\1\13",
            "\1\17\32\uffff\1\16\1\15",
            "\1\13\26\uffff\3\13\4\uffff\1\12\1\uffff\2\12\1\uffff\1\13"+
            "\1\uffff\1\13\3\uffff\2\13\3\uffff\2\13\3\uffff\1\13",
            "",
            "",
            "\1\22\32\uffff\1\21\1\20",
            "\1\13\26\uffff\3\13\4\uffff\1\12\1\uffff\2\12\1\uffff\1\13"+
            "\1\uffff\1\13\3\uffff\2\13\3\uffff\2\13\3\uffff\1\13",
            "",
            "",
            "\1\25\32\uffff\1\24\1\23",
            "\1\13\26\uffff\3\13\4\uffff\1\12\1\uffff\2\12\1\uffff\1\13"+
            "\1\uffff\1\13\3\uffff\2\13\3\uffff\2\13\3\uffff\1\13",
            "\1\13\26\uffff\3\13\4\uffff\1\12\1\uffff\2\12\1\uffff\1\13"+
            "\1\uffff\1\13\3\uffff\2\13\3\uffff\2\13\3\uffff\1\13",
            "\1\13\26\uffff\3\13\4\uffff\1\12\1\uffff\2\12\1\uffff\1\13"+
            "\1\uffff\1\13\3\uffff\2\13\3\uffff\2\13\3\uffff\1\13",
            "\1\27\5\uffff\1\30\1\31\10\uffff\1\26",
            "\1\27\5\uffff\1\30\1\31\10\uffff\1\26",
            "\1\27\5\uffff\1\30\1\31\10\uffff\1\26",
            "\1\34\5\uffff\1\35\1\32\10\uffff\1\33",
            "\1\34\5\uffff\1\35\1\32\10\uffff\1\33",
            "\1\34\5\uffff\1\35\1\32\10\uffff\1\33",
            "\1\40\32\uffff\1\37\1\36",
            "\1\43\32\uffff\1\42\1\41",
            "\1\11\1\uffff\1\13\26\uffff\3\13\4\uffff\1\12\1\uffff\2\12"+
            "\1\uffff\1\13\1\uffff\1\13\3\uffff\2\13\3\uffff\2\13\3\uffff"+
            "\1\13",
            "\1\46\32\uffff\1\45\1\44\3\uffff\1\47",
            "\1\52\32\uffff\1\51\1\50\3\uffff\1\53",
            "\1\56\32\uffff\1\55\1\54",
            "\1\61\32\uffff\1\60\1\57",
            "\1\13\26\uffff\3\13\4\uffff\1\12\1\uffff\2\12\1\uffff\1\13"+
            "\1\uffff\1\13\3\uffff\2\13\3\uffff\2\13\3\uffff\1\13",
            "\1\27\5\uffff\1\30\11\uffff\1\26",
            "\1\27\5\uffff\1\30\11\uffff\1\26",
            "\1\27\5\uffff\1\30\11\uffff\1\26",
            "\1\27\5\uffff\1\30\1\63\10\uffff\1\62",
            "\1\27\5\uffff\1\30\1\63\10\uffff\1\62",
            "\1\27\5\uffff\1\30\1\63\10\uffff\1\62",
            "\1\27\5\uffff\1\30\11\uffff\1\64",
            "\1\27\5\uffff\1\30\11\uffff\1\64",
            "\1\27\5\uffff\1\30\11\uffff\1\64",
            "\1\27\5\uffff\1\30",
            "\1\34\5\uffff\1\35\11\uffff\1\65",
            "\1\34\5\uffff\1\35\11\uffff\1\65",
            "\1\34\5\uffff\1\35\11\uffff\1\65",
            "\1\34\5\uffff\1\35",
            "\1\34\5\uffff\1\35\11\uffff\1\33",
            "\1\34\5\uffff\1\35\11\uffff\1\33",
            "\1\34\5\uffff\1\35\11\uffff\1\33",
            "\1\34\5\uffff\1\35\1\67\10\uffff\1\66",
            "\1\34\5\uffff\1\35\1\67\10\uffff\1\66",
            "\1\34\5\uffff\1\35\1\67\10\uffff\1\66",
            "\1\72\32\uffff\1\71\1\70",
            "\1\75\32\uffff\1\74\1\73\3\uffff\1\76",
            "\1\101\32\uffff\1\100\1\77",
            "\1\104\32\uffff\1\103\1\102",
            "\1\107\32\uffff\1\106\1\105",
            "\1\112\32\uffff\1\111\1\110\3\uffff\1\113",
            "\1\27\5\uffff\1\30\11\uffff\1\62",
            "\1\27\5\uffff\1\30\11\uffff\1\62",
            "\1\27\5\uffff\1\30\11\uffff\1\62",
            "\1\27\5\uffff\1\30\11\uffff\1\114",
            "\1\27\5\uffff\1\30\11\uffff\1\114",
            "\1\27\5\uffff\1\30\11\uffff\1\114",
            "\1\27\5\uffff\1\30",
            "\1\27\5\uffff\1\30\11\uffff\1\64",
            "\1\27\5\uffff\1\30\11\uffff\1\64",
            "\1\27\5\uffff\1\30\11\uffff\1\64",
            "\1\34\5\uffff\1\35\11\uffff\1\65",
            "\1\34\5\uffff\1\35\11\uffff\1\65",
            "\1\34\5\uffff\1\35\11\uffff\1\65",
            "\1\34\5\uffff\1\35\11\uffff\1\66",
            "\1\34\5\uffff\1\35\11\uffff\1\66",
            "\1\34\5\uffff\1\35\11\uffff\1\66",
            "\1\34\5\uffff\1\35\11\uffff\1\115",
            "\1\34\5\uffff\1\35\11\uffff\1\115",
            "\1\34\5\uffff\1\35\11\uffff\1\115",
            "\1\34\5\uffff\1\35",
            "\1\120\32\uffff\1\117\1\116",
            "\1\123\32\uffff\1\122\1\121",
            "\1\27\5\uffff\1\30\11\uffff\1\114",
            "\1\27\5\uffff\1\30\11\uffff\1\114",
            "\1\27\5\uffff\1\30\11\uffff\1\114",
            "\1\34\5\uffff\1\35\11\uffff\1\115",
            "\1\34\5\uffff\1\35\11\uffff\1\115",
            "\1\34\5\uffff\1\35\11\uffff\1\115"
    };

    static final short[] DFA66_eot = DFA.unpackEncodedString(DFA66_eotS);
    static final short[] DFA66_eof = DFA.unpackEncodedString(DFA66_eofS);
    static final char[] DFA66_min = DFA.unpackEncodedStringToUnsignedChars(DFA66_minS);
    static final char[] DFA66_max = DFA.unpackEncodedStringToUnsignedChars(DFA66_maxS);
    static final short[] DFA66_accept = DFA.unpackEncodedString(DFA66_acceptS);
    static final short[] DFA66_special = DFA.unpackEncodedString(DFA66_specialS);
    static final short[][] DFA66_transition;

    static {
        int numStates = DFA66_transitionS.length;
        DFA66_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA66_transition[i] = DFA.unpackEncodedString(DFA66_transitionS[i]);
        }
    }

    class DFA66 extends DFA {

        public DFA66(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 66;
            this.eot = DFA66_eot;
            this.eof = DFA66_eof;
            this.min = DFA66_min;
            this.max = DFA66_max;
            this.accept = DFA66_accept;
            this.special = DFA66_special;
            this.transition = DFA66_transition;
        }
        public String getDescription() {
            return "832:1: rewriteTreeElement : ( rewriteTreeAtom | rewriteTreeAtom ebnfSuffix -> ^( ebnfSuffix ^( REWRITE_BLOCK ^( ALT rewriteTreeAtom ) ) ) | rewriteTree ( ebnfSuffix -> ^( ebnfSuffix ^( REWRITE_BLOCK ^( ALT rewriteTree ) ) ) | -> rewriteTree ) | rewriteTreeEbnf );";
        }
    }
    static final String DFA73_eotS =
        "\23\uffff";
    static final String DFA73_eofS =
        "\11\uffff\1\2\11\uffff";
    static final String DFA73_minS =
        "\1\20\1\50\3\uffff\1\43\3\55\1\12\1\20\1\uffff\1\46\1\43\3\55\1"+
        "\20\1\46";
    static final String DFA73_maxS =
        "\1\77\1\50\3\uffff\1\77\3\55\1\71\1\20\1\uffff\1\51\1\77\3\55\1"+
        "\20\1\51";
    static final String DFA73_acceptS =
        "\2\uffff\1\2\1\3\1\4\6\uffff\1\1\7\uffff";
    static final String DFA73_specialS =
        "\23\uffff}>";
    static final String[] DFA73_transitionS = {
            "\1\4\22\uffff\1\1\4\uffff\1\3\25\uffff\2\2",
            "\1\5",
            "",
            "",
            "",
            "\1\10\5\uffff\1\11\24\uffff\1\7\1\6",
            "\1\12",
            "\1\12",
            "\1\12",
            "\2\13\33\uffff\1\2\1\uffff\1\2\11\uffff\1\2\5\uffff\1\2",
            "\1\14",
            "",
            "\1\15\2\uffff\1\11",
            "\1\20\32\uffff\1\17\1\16",
            "\1\21",
            "\1\21",
            "\1\21",
            "\1\22",
            "\1\15\2\uffff\1\11"
    };

    static final short[] DFA73_eot = DFA.unpackEncodedString(DFA73_eotS);
    static final short[] DFA73_eof = DFA.unpackEncodedString(DFA73_eofS);
    static final char[] DFA73_min = DFA.unpackEncodedStringToUnsignedChars(DFA73_minS);
    static final char[] DFA73_max = DFA.unpackEncodedStringToUnsignedChars(DFA73_maxS);
    static final short[] DFA73_accept = DFA.unpackEncodedString(DFA73_acceptS);
    static final short[] DFA73_special = DFA.unpackEncodedString(DFA73_specialS);
    static final short[][] DFA73_transition;

    static {
        int numStates = DFA73_transitionS.length;
        DFA73_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA73_transition[i] = DFA.unpackEncodedString(DFA73_transitionS[i]);
        }
    }

    class DFA73 extends DFA {

        public DFA73(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 73;
            this.eot = DFA73_eot;
            this.eof = DFA73_eof;
            this.min = DFA73_min;
            this.max = DFA73_max;
            this.accept = DFA73_accept;
            this.special = DFA73_special;
            this.transition = DFA73_transition;
        }
        public String getDescription() {
            return "867:1: rewriteTemplate : ( TEMPLATE LPAREN rewriteTemplateArgs RPAREN (str= DOUBLE_QUOTE_STRING_LITERAL | str= DOUBLE_ANGLE_STRING_LITERAL ) -> ^( TEMPLATE[$TEMPLATE,\"TEMPLATE\"] ( rewriteTemplateArgs )? $str) | rewriteTemplateRef | rewriteIndirectTemplateHead | ACTION );";
        }
    }


    public static final BitSet FOLLOW_DOC_COMMENT_in_grammarSpec459 = new BitSet(new long[]{0x000000000F000000L});
    public static final BitSet FOLLOW_grammarType_in_grammarSpec496 = new BitSet(new long[]{0xC000000800000000L});
    public static final BitSet FOLLOW_id_in_grammarSpec498 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_SEMI_in_grammarSpec500 = new BitSet(new long[]{0xC800000870F80040L});
    public static final BitSet FOLLOW_prequelConstruct_in_grammarSpec544 = new BitSet(new long[]{0xC800000870F80040L});
    public static final BitSet FOLLOW_rules_in_grammarSpec572 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_grammarSpec615 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEXER_in_grammarType809 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_GRAMMAR_in_grammarType813 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PARSER_in_grammarType846 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_GRAMMAR_in_grammarType850 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TREE_in_grammarType877 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_GRAMMAR_in_grammarType881 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GRAMMAR_in_grammarType908 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_optionsSpec_in_prequelConstruct972 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_delegateGrammars_in_prequelConstruct998 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tokensSpec_in_prequelConstruct1048 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attrScope_in_prequelConstruct1084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_action_in_prequelConstruct1127 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OPTIONS_in_optionsSpec1144 = new BitSet(new long[]{0xE000000800000000L});
    public static final BitSet FOLLOW_option_in_optionsSpec1147 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_SEMI_in_optionsSpec1149 = new BitSet(new long[]{0xE000000800000000L});
    public static final BitSet FOLLOW_RBRACE_in_optionsSpec1153 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_option1190 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ASSIGN_in_option1192 = new BitSet(new long[]{0xC001000800000000L,0x0000000000000009L});
    public static final BitSet FOLLOW_optionValue_in_option1195 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qid_in_optionValue1245 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_optionValue1269 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_optionValue1295 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_optionValue1324 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORT_in_delegateGrammars1340 = new BitSet(new long[]{0xC000000800000000L});
    public static final BitSet FOLLOW_delegateGrammar_in_delegateGrammars1342 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_COMMA_in_delegateGrammars1345 = new BitSet(new long[]{0xC000000800000000L});
    public static final BitSet FOLLOW_delegateGrammar_in_delegateGrammars1347 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_SEMI_in_delegateGrammars1351 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_delegateGrammar1378 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ASSIGN_in_delegateGrammar1380 = new BitSet(new long[]{0xC000000800000000L});
    public static final BitSet FOLLOW_id_in_delegateGrammar1383 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_delegateGrammar1393 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TOKENS_in_tokensSpec1409 = new BitSet(new long[]{0xC000000800000000L});
    public static final BitSet FOLLOW_tokenSpec_in_tokensSpec1411 = new BitSet(new long[]{0xE000000800000000L});
    public static final BitSet FOLLOW_RBRACE_in_tokensSpec1414 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_tokenSpec1434 = new BitSet(new long[]{0x0000208000000000L});
    public static final BitSet FOLLOW_ASSIGN_in_tokenSpec1440 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_tokenSpec1442 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_SEMI_in_tokenSpec1477 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_REF_in_tokenSpec1482 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SCOPE_in_attrScope1497 = new BitSet(new long[]{0xC000000800000000L});
    public static final BitSet FOLLOW_id_in_attrScope1499 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_ACTION_in_attrScope1501 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_in_action1527 = new BitSet(new long[]{0xC000000803000000L});
    public static final BitSet FOLLOW_actionScopeName_in_action1530 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_COLONCOLON_in_action1532 = new BitSet(new long[]{0xC000000800000000L});
    public static final BitSet FOLLOW_id_in_action1536 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_ACTION_in_action1538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_actionScopeName1566 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LEXER_in_actionScopeName1571 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PARSER_in_actionScopeName1586 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule_in_rules1605 = new BitSet(new long[]{0xC000000870800042L});
    public static final BitSet FOLLOW_DOC_COMMENT_in_rule1684 = new BitSet(new long[]{0xC000000870800000L});
    public static final BitSet FOLLOW_ruleModifiers_in_rule1728 = new BitSet(new long[]{0xC000000800000000L});
    public static final BitSet FOLLOW_id_in_rule1751 = new BitSet(new long[]{0x0800001180284000L});
    public static final BitSet FOLLOW_ARG_ACTION_in_rule1784 = new BitSet(new long[]{0x0800001180280000L});
    public static final BitSet FOLLOW_ruleReturns_in_rule1794 = new BitSet(new long[]{0x0800001100280000L});
    public static final BitSet FOLLOW_rulePrequel_in_rule1832 = new BitSet(new long[]{0x0800001100280000L});
    public static final BitSet FOLLOW_COLON_in_rule1848 = new BitSet(new long[]{0xD648010800010010L,0x0000000000000008L});
    public static final BitSet FOLLOW_altListAsBlock_in_rule1877 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_SEMI_in_rule1892 = new BitSet(new long[]{0x0000000600000000L});
    public static final BitSet FOLLOW_exceptionGroup_in_rule1901 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_exceptionHandler_in_exceptionGroup1993 = new BitSet(new long[]{0x0000000600000002L});
    public static final BitSet FOLLOW_finallyClause_in_exceptionGroup1996 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CATCH_in_exceptionHandler2013 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_ARG_ACTION_in_exceptionHandler2015 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_ACTION_in_exceptionHandler2017 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FINALLY_in_finallyClause2040 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_ACTION_in_finallyClause2042 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_throwsSpec_in_rulePrequel2069 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleScopeSpec_in_rulePrequel2077 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_optionsSpec_in_rulePrequel2085 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAction_in_rulePrequel2093 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURNS_in_ruleReturns2113 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_ARG_ACTION_in_ruleReturns2116 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_THROWS_in_throwsSpec2141 = new BitSet(new long[]{0xC000000800000000L});
    public static final BitSet FOLLOW_qid_in_throwsSpec2143 = new BitSet(new long[]{0x0000004000000002L});
    public static final BitSet FOLLOW_COMMA_in_throwsSpec2146 = new BitSet(new long[]{0xC000000800000000L});
    public static final BitSet FOLLOW_qid_in_throwsSpec2148 = new BitSet(new long[]{0x0000004000000002L});
    public static final BitSet FOLLOW_SCOPE_in_ruleScopeSpec2179 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_ACTION_in_ruleScopeSpec2181 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SCOPE_in_ruleScopeSpec2194 = new BitSet(new long[]{0xC000000800000000L});
    public static final BitSet FOLLOW_id_in_ruleScopeSpec2196 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_COMMA_in_ruleScopeSpec2199 = new BitSet(new long[]{0xC000000800000000L});
    public static final BitSet FOLLOW_id_in_ruleScopeSpec2201 = new BitSet(new long[]{0x000000C000000000L});
    public static final BitSet FOLLOW_SEMI_in_ruleScopeSpec2205 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_in_ruleAction2235 = new BitSet(new long[]{0xC000000800000000L});
    public static final BitSet FOLLOW_id_in_ruleAction2237 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_ACTION_in_ruleAction2239 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleModifier_in_ruleModifiers2277 = new BitSet(new long[]{0x0000000070800002L});
    public static final BitSet FOLLOW_set_in_ruleModifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_alternative_in_altList2353 = new BitSet(new long[]{0x0008000000000002L});
    public static final BitSet FOLLOW_OR_in_altList2356 = new BitSet(new long[]{0xD648010800010010L,0x0000000000000008L});
    public static final BitSet FOLLOW_alternative_in_altList2358 = new BitSet(new long[]{0x0008000000000002L});
    public static final BitSet FOLLOW_altList_in_altListAsBlock2388 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_elements_in_alternative2418 = new BitSet(new long[]{0x0200000000000002L});
    public static final BitSet FOLLOW_rewrite_in_alternative2427 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rewrite_in_alternative2465 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_element_in_elements2518 = new BitSet(new long[]{0xD440010800010012L,0x0000000000000008L});
    public static final BitSet FOLLOW_labeledElement_in_element2545 = new BitSet(new long[]{0x0003400000000002L});
    public static final BitSet FOLLOW_ebnfSuffix_in_element2551 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atom_in_element2593 = new BitSet(new long[]{0x0003400000000002L});
    public static final BitSet FOLLOW_ebnfSuffix_in_element2599 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ebnf_in_element2642 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ACTION_in_element2649 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SEMPRED_in_element2656 = new BitSet(new long[]{0x0000040000000002L});
    public static final BitSet FOLLOW_IMPLIES_in_element2662 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_treeSpec_in_element2690 = new BitSet(new long[]{0x0003400000000002L});
    public static final BitSet FOLLOW_ebnfSuffix_in_element2696 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_labeledElement2745 = new BitSet(new long[]{0x0004200000000000L});
    public static final BitSet FOLLOW_ASSIGN_in_labeledElement2748 = new BitSet(new long[]{0xD040010800000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_PLUS_ASSIGN_in_labeledElement2751 = new BitSet(new long[]{0xD040010800000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_atom_in_labeledElement2756 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_block_in_labeledElement2758 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TREE_BEGIN_in_treeSpec2780 = new BitSet(new long[]{0xD440010800010010L,0x0000000000000008L});
    public static final BitSet FOLLOW_element_in_treeSpec2821 = new BitSet(new long[]{0xD440010800010010L,0x0000000000000008L});
    public static final BitSet FOLLOW_element_in_treeSpec2852 = new BitSet(new long[]{0xD440030800010010L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_treeSpec2861 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_block_in_ebnf2895 = new BitSet(new long[]{0x0013C40000000002L});
    public static final BitSet FOLLOW_blockSuffixe_in_ebnf2930 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ebnfSuffix_in_blockSuffixe2981 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ROOT_in_blockSuffixe2995 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPLIES_in_blockSuffixe3003 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BANG_in_blockSuffixe3014 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_QUESTION_in_ebnfSuffix3033 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_ebnfSuffix3045 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_ebnfSuffix3060 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_range_in_atom3077 = new BitSet(new long[]{0x0010800000000002L});
    public static final BitSet FOLLOW_ROOT_in_atom3080 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BANG_in_atom3085 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_atom3132 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_DOT_in_atom3134 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_ruleref_in_atom3136 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_atom3170 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_DOT_in_atom3172 = new BitSet(new long[]{0x4040000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_terminal_in_atom3174 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_terminal_in_atom3199 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleref_in_atom3209 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_notSet_in_atom3217 = new BitSet(new long[]{0x0010800000000002L});
    public static final BitSet FOLLOW_ROOT_in_atom3220 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BANG_in_atom3223 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_notSet3256 = new BitSet(new long[]{0x4000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_notTerminal_in_notSet3258 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_notSet3274 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_block_in_notSet3276 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_notTerminal0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_block3350 = new BitSet(new long[]{0xDE48011900290010L,0x0000000000000008L});
    public static final BitSet FOLLOW_optionsSpec_in_block3396 = new BitSet(new long[]{0xDE48011900290010L,0x0000000000000008L});
    public static final BitSet FOLLOW_ruleAction_in_block3491 = new BitSet(new long[]{0x0800001100290000L});
    public static final BitSet FOLLOW_ACTION_in_block3507 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_COLON_in_block3558 = new BitSet(new long[]{0xD648010800010010L,0x0000000000000008L});
    public static final BitSet FOLLOW_altList_in_block3611 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_RPAREN_in_block3629 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_REF_in_ruleref3702 = new BitSet(new long[]{0x0010800000004002L});
    public static final BitSet FOLLOW_ARG_ACTION_in_ruleref3704 = new BitSet(new long[]{0x0010800000000002L});
    public static final BitSet FOLLOW_ROOT_in_ruleref3714 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BANG_in_ruleref3718 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rangeElement_in_range3783 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_RANGE_in_range3785 = new BitSet(new long[]{0xC000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_rangeElement_in_range3788 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_rangeElement0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TOKEN_REF_in_terminal3882 = new BitSet(new long[]{0x0010880000004002L});
    public static final BitSet FOLLOW_ARG_ACTION_in_terminal3884 = new BitSet(new long[]{0x0010880000000002L});
    public static final BitSet FOLLOW_elementOptions_in_terminal3887 = new BitSet(new long[]{0x0010800000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_terminal3911 = new BitSet(new long[]{0x0010880000000002L});
    public static final BitSet FOLLOW_elementOptions_in_terminal3913 = new BitSet(new long[]{0x0010800000000002L});
    public static final BitSet FOLLOW_DOT_in_terminal3960 = new BitSet(new long[]{0x0010880000000002L});
    public static final BitSet FOLLOW_elementOptions_in_terminal3962 = new BitSet(new long[]{0x0010800000000002L});
    public static final BitSet FOLLOW_ROOT_in_terminal3993 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BANG_in_terminal4017 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LT_in_elementOptions4081 = new BitSet(new long[]{0xC000000800000000L});
    public static final BitSet FOLLOW_elementOption_in_elementOptions4083 = new BitSet(new long[]{0x0000104000000000L});
    public static final BitSet FOLLOW_COMMA_in_elementOptions4086 = new BitSet(new long[]{0xC000000800000000L});
    public static final BitSet FOLLOW_elementOption_in_elementOptions4088 = new BitSet(new long[]{0x0000104000000000L});
    public static final BitSet FOLLOW_GT_in_elementOptions4092 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qid_in_elementOption4127 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_elementOption4149 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ASSIGN_in_elementOption4151 = new BitSet(new long[]{0xC000000800000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_qid_in_elementOption4155 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_elementOption4159 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_predicatedRewrite_in_rewrite4177 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_nakedRewrite_in_rewrite4180 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RARROW_in_predicatedRewrite4198 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_SEMPRED_in_predicatedRewrite4200 = new BitSet(new long[]{0xC520010800010000L,0x0000000000000008L});
    public static final BitSet FOLLOW_rewriteAlt_in_predicatedRewrite4202 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RARROW_in_nakedRewrite4242 = new BitSet(new long[]{0xC520010800010000L,0x0000000000000008L});
    public static final BitSet FOLLOW_rewriteAlt_in_nakedRewrite4244 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rewriteTemplate_in_rewriteAlt4308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rewriteTreeAlt_in_rewriteAlt4347 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ETC_in_rewriteAlt4356 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rewriteTreeElement_in_rewriteTreeAlt4387 = new BitSet(new long[]{0xC420010000010002L,0x0000000000000008L});
    public static final BitSet FOLLOW_rewriteTreeAtom_in_rewriteTreeElement4411 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rewriteTreeAtom_in_rewriteTreeElement4416 = new BitSet(new long[]{0x0003400000000000L});
    public static final BitSet FOLLOW_ebnfSuffix_in_rewriteTreeElement4418 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rewriteTree_in_rewriteTreeElement4443 = new BitSet(new long[]{0x0003400000000002L});
    public static final BitSet FOLLOW_ebnfSuffix_in_rewriteTreeElement4449 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rewriteTreeEbnf_in_rewriteTreeElement4488 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TOKEN_REF_in_rewriteTreeAtom4504 = new BitSet(new long[]{0x0000080000004002L});
    public static final BitSet FOLLOW_elementOptions_in_rewriteTreeAtom4506 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_ARG_ACTION_in_rewriteTreeAtom4509 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_REF_in_rewriteTreeAtom4536 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_rewriteTreeAtom4543 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_elementOptions_in_rewriteTreeAtom4545 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOLLAR_in_rewriteTreeAtom4568 = new BitSet(new long[]{0xC000000800000000L});
    public static final BitSet FOLLOW_id_in_rewriteTreeAtom4570 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ACTION_in_rewriteTreeAtom4581 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_rewriteTreeEbnf4604 = new BitSet(new long[]{0xC420010000010000L,0x0000000000000008L});
    public static final BitSet FOLLOW_rewriteTreeAlt_in_rewriteTreeEbnf4606 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_RPAREN_in_rewriteTreeEbnf4608 = new BitSet(new long[]{0x0003400000000000L});
    public static final BitSet FOLLOW_ebnfSuffix_in_rewriteTreeEbnf4610 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TREE_BEGIN_in_rewriteTree4634 = new BitSet(new long[]{0xC020000000010000L,0x0000000000000008L});
    public static final BitSet FOLLOW_rewriteTreeAtom_in_rewriteTree4636 = new BitSet(new long[]{0xC420030000010000L,0x0000000000000008L});
    public static final BitSet FOLLOW_rewriteTreeElement_in_rewriteTree4638 = new BitSet(new long[]{0xC420030000010000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_rewriteTree4641 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEMPLATE_in_rewriteTemplate4673 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_LPAREN_in_rewriteTemplate4675 = new BitSet(new long[]{0xC000020800000000L});
    public static final BitSet FOLLOW_rewriteTemplateArgs_in_rewriteTemplate4677 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_RPAREN_in_rewriteTemplate4679 = new BitSet(new long[]{0x0000000000000C00L});
    public static final BitSet FOLLOW_DOUBLE_QUOTE_STRING_LITERAL_in_rewriteTemplate4687 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLE_ANGLE_STRING_LITERAL_in_rewriteTemplate4693 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rewriteTemplateRef_in_rewriteTemplate4719 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rewriteIndirectTemplateHead_in_rewriteTemplate4728 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ACTION_in_rewriteTemplate4737 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_rewriteTemplateRef4750 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_LPAREN_in_rewriteTemplateRef4752 = new BitSet(new long[]{0xC000020800000000L});
    public static final BitSet FOLLOW_rewriteTemplateArgs_in_rewriteTemplateRef4754 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_RPAREN_in_rewriteTemplateRef4756 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_rewriteIndirectTemplateHead4785 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_ACTION_in_rewriteIndirectTemplateHead4787 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_RPAREN_in_rewriteIndirectTemplateHead4789 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_LPAREN_in_rewriteIndirectTemplateHead4791 = new BitSet(new long[]{0xC000020800000000L});
    public static final BitSet FOLLOW_rewriteTemplateArgs_in_rewriteIndirectTemplateHead4793 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_RPAREN_in_rewriteIndirectTemplateHead4795 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rewriteTemplateArg_in_rewriteTemplateArgs4820 = new BitSet(new long[]{0x0000004000000002L});
    public static final BitSet FOLLOW_COMMA_in_rewriteTemplateArgs4823 = new BitSet(new long[]{0xC000000800000000L});
    public static final BitSet FOLLOW_rewriteTemplateArg_in_rewriteTemplateArgs4825 = new BitSet(new long[]{0x0000004000000002L});
    public static final BitSet FOLLOW_id_in_rewriteTemplateArg4854 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_ASSIGN_in_rewriteTemplateArg4856 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_ACTION_in_rewriteTemplateArg4858 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_REF_in_id4887 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TOKEN_REF_in_id4900 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEMPLATE_in_id4912 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_id_in_qid4935 = new BitSet(new long[]{0x0040000000000002L});
    public static final BitSet FOLLOW_DOT_in_qid4938 = new BitSet(new long[]{0xC000000800000000L});
    public static final BitSet FOLLOW_id_in_qid4940 = new BitSet(new long[]{0x0040000000000002L});
    public static final BitSet FOLLOW_alternative_in_alternativeEntry4956 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_alternativeEntry4958 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_element_in_elementEntry4967 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_elementEntry4969 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule_in_ruleEntry4977 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_ruleEntry4979 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_block_in_blockEntry4987 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_blockEntry4989 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rewriteTemplate_in_synpred1_ANTLRParser4308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rewriteTreeAlt_in_synpred2_ANTLRParser4347 = new BitSet(new long[]{0x0000000000000002L});

}