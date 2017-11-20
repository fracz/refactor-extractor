// $ANTLR 3.2.1-SNAPSHOT Jan 26, 2010 15:12:28 ASTVerifier.g 2010-01-31 16:35:39

/*
 [The "BSD license"]
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
import org.antlr.v4.tool.*;
import org.antlr.v4.runtime.tree.CommonTree; // use updated v4 one not v3


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

/** The definitive ANTLR v3 tree grammar to parse ANTLR v4 grammars.
 *  Parses trees created in ANTLRParser.g.
 */
public class ASTVerifier extends TreeParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "SEMPRED", "FORCED_ACTION", "DOC_COMMENT", "SRC", "NLCHARS", "COMMENT", "DOUBLE_QUOTE_STRING_LITERAL", "DOUBLE_ANGLE_STRING_LITERAL", "ACTION_STRING_LITERAL", "ACTION_CHAR_LITERAL", "ARG_ACTION", "NESTED_ACTION", "ACTION", "ACTION_ESC", "WSNLCHARS", "OPTIONS", "TOKENS", "SCOPE", "IMPORT", "FRAGMENT", "LEXER", "PARSER", "TREE", "GRAMMAR", "PROTECTED", "PUBLIC", "PRIVATE", "RETURNS", "THROWS", "CATCH", "FINALLY", "TEMPLATE", "COLON", "COLONCOLON", "COMMA", "SEMI", "LPAREN", "RPAREN", "IMPLIES", "LT", "GT", "ASSIGN", "QUESTION", "BANG", "STAR", "PLUS", "PLUS_ASSIGN", "OR", "ROOT", "DOLLAR", "WILDCARD", "RANGE", "ETC", "RARROW", "TREE_BEGIN", "AT", "NOT", "RBRACE", "TOKEN_REF", "RULE_REF", "INT", "WSCHARS", "ESC_SEQ", "STRING_LITERAL", "HEX_DIGIT", "UNICODE_ESC", "WS", "ERRCHAR", "RULE", "RULES", "RULEMODIFIERS", "RULEACTIONS", "BLOCK", "OPTIONAL", "CLOSURE", "POSITIVE_CLOSURE", "SYNPRED", "CHAR_RANGE", "EPSILON", "ALT", "ALTLIST", "RESULT", "ID", "ARG", "ARGLIST", "RET", "LEXER_GRAMMAR", "PARSER_GRAMMAR", "TREE_GRAMMAR", "COMBINED_GRAMMAR", "INITACTION", "LABEL", "GATED_SEMPRED", "SYN_SEMPRED", "BACKTRACK_SEMPRED", "DOT", "LIST", "ELEMENT_OPTIONS", "ST_RESULT", "ALT_REWRITE"
    };
    public static final int LT=43;
    public static final int STAR=48;
    public static final int BACKTRACK_SEMPRED=98;
    public static final int DOUBLE_ANGLE_STRING_LITERAL=11;
    public static final int FORCED_ACTION=5;
    public static final int LEXER_GRAMMAR=90;
    public static final int ARGLIST=88;
    public static final int ALTLIST=84;
    public static final int NOT=60;
    public static final int EOF=-1;
    public static final int SEMPRED=4;
    public static final int ACTION=16;
    public static final int TOKEN_REF=62;
    public static final int RULEMODIFIERS=74;
    public static final int ST_RESULT=102;
    public static final int RPAREN=41;
    public static final int RET=89;
    public static final int IMPORT=22;
    public static final int STRING_LITERAL=67;
    public static final int ARG=87;
    public static final int ARG_ACTION=14;
    public static final int DOUBLE_QUOTE_STRING_LITERAL=10;
    public static final int COMMENT=9;
    public static final int ACTION_CHAR_LITERAL=13;
    public static final int GRAMMAR=27;
    public static final int RULEACTIONS=75;
    public static final int WSCHARS=65;
    public static final int INITACTION=94;
    public static final int ALT_REWRITE=103;
    public static final int IMPLIES=42;
    public static final int RULE=72;
    public static final int RBRACE=61;
    public static final int ACTION_ESC=17;
    public static final int PARSER_GRAMMAR=91;
    public static final int PRIVATE=30;
    public static final int SRC=7;
    public static final int THROWS=32;
    public static final int CHAR_RANGE=81;
    public static final int INT=64;
    public static final int EPSILON=82;
    public static final int LIST=100;
    public static final int COLONCOLON=37;
    public static final int WSNLCHARS=18;
    public static final int WS=70;
    public static final int COMBINED_GRAMMAR=93;
    public static final int LEXER=24;
    public static final int OR=51;
    public static final int GT=44;
    public static final int TREE_GRAMMAR=92;
    public static final int CATCH=33;
    public static final int CLOSURE=78;
    public static final int PARSER=25;
    public static final int DOLLAR=53;
    public static final int PROTECTED=28;
    public static final int ELEMENT_OPTIONS=101;
    public static final int NESTED_ACTION=15;
    public static final int FRAGMENT=23;
    public static final int ID=86;
    public static final int TREE_BEGIN=58;
    public static final int LPAREN=40;
    public static final int AT=59;
    public static final int ESC_SEQ=66;
    public static final int ALT=83;
    public static final int TREE=26;
    public static final int SCOPE=21;
    public static final int ETC=56;
    public static final int COMMA=38;
    public static final int WILDCARD=54;
    public static final int DOC_COMMENT=6;
    public static final int PLUS=49;
    public static final int DOT=99;
    public static final int RETURNS=31;
    public static final int RULES=73;
    public static final int RARROW=57;
    public static final int UNICODE_ESC=69;
    public static final int HEX_DIGIT=68;
    public static final int RANGE=55;
    public static final int TOKENS=20;
    public static final int GATED_SEMPRED=96;
    public static final int RESULT=85;
    public static final int BANG=47;
    public static final int ACTION_STRING_LITERAL=12;
    public static final int ROOT=52;
    public static final int SEMI=39;
    public static final int RULE_REF=63;
    public static final int NLCHARS=8;
    public static final int OPTIONAL=77;
    public static final int SYNPRED=80;
    public static final int COLON=36;
    public static final int QUESTION=46;
    public static final int FINALLY=34;
    public static final int TEMPLATE=35;
    public static final int LABEL=95;
    public static final int SYN_SEMPRED=97;
    public static final int ERRCHAR=71;
    public static final int BLOCK=76;
    public static final int ASSIGN=45;
    public static final int PLUS_ASSIGN=50;
    public static final int PUBLIC=29;
    public static final int POSITIVE_CLOSURE=79;
    public static final int OPTIONS=19;

    // delegates
    // delegators


        public ASTVerifier(TreeNodeStream input) {
            this(input, new RecognizerSharedState());
        }
        public ASTVerifier(TreeNodeStream input, RecognizerSharedState state) {
            super(input, state);

        }


    public String[] getTokenNames() { return ASTVerifier.tokenNames; }
    public String getGrammarFileName() { return "ASTVerifier.g"; }


    public String getErrorMessage(RecognitionException e,
                                  String[] tokenNames)
    {
        List stack = getRuleInvocationStack(e, this.getClass().getName());
        String msg = null;
        String inputContext =
            ((CommonTree)input.LT(-3)).token+" "+
            ((CommonTree)input.LT(-2)).token+" "+
            ((CommonTree)input.LT(-1)).token+" >>>"+
            ((CommonTree)input.LT(1)).token+"<<< "+
            ((CommonTree)input.LT(2)).token+" "+
            ((CommonTree)input.LT(3)).token;
        if ( e instanceof NoViableAltException ) {
           NoViableAltException nvae = (NoViableAltException)e;
           msg = " no viable alt; token="+e.token+
              " (decision="+nvae.decisionNumber+
              " state "+nvae.stateNumber+")"+
              " decision=<<"+nvae.grammarDecisionDescription+">>";
        }
        else {
           msg = super.getErrorMessage(e, tokenNames);
        }
        return stack+" "+msg+"\ncontext=..."+inputContext+"...";
    }
    public String getTokenErrorDisplay(Token t) {
        return t.toString();
    }
    public void traceIn(String ruleName, int ruleIndex)  {
       	System.out.print("enter "+ruleName+" "+
                         ((GrammarAST)input.LT(1)).token+" "+
                         ((GrammarAST)input.LT(2)).token+" "+
                         ((GrammarAST)input.LT(3)).token+" "+
                         ((GrammarAST)input.LT(4)).token);
    	if ( state.backtracking>0 ) {
    		System.out.print(" backtracking="+state.backtracking);
    	}
    	System.out.println();
    }



    // $ANTLR start "grammarSpec"
    // ASTVerifier.g:110:1: grammarSpec : ^( grammarType ID ( DOC_COMMENT )? ( prequelConstruct )* rules ) ;
    public final void grammarSpec() throws RecognitionException {
        try {
            // ASTVerifier.g:111:5: ( ^( grammarType ID ( DOC_COMMENT )? ( prequelConstruct )* rules ) )
            // ASTVerifier.g:111:9: ^( grammarType ID ( DOC_COMMENT )? ( prequelConstruct )* rules )
            {
            pushFollow(FOLLOW_grammarType_in_grammarSpec74);
            grammarType();

            state._fsp--;


            match(input, Token.DOWN, null);
            match(input,ID,FOLLOW_ID_in_grammarSpec76);
            // ASTVerifier.g:111:26: ( DOC_COMMENT )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==DOC_COMMENT) ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // ASTVerifier.g:111:26: DOC_COMMENT
                    {
                    match(input,DOC_COMMENT,FOLLOW_DOC_COMMENT_in_grammarSpec78);

                    }
                    break;

            }

            // ASTVerifier.g:111:39: ( prequelConstruct )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>=OPTIONS && LA2_0<=IMPORT)||LA2_0==AT) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // ASTVerifier.g:111:39: prequelConstruct
            	    {
            	    pushFollow(FOLLOW_prequelConstruct_in_grammarSpec81);
            	    prequelConstruct();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            pushFollow(FOLLOW_rules_in_grammarSpec84);
            rules();

            state._fsp--;


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "grammarSpec"


    // $ANTLR start "grammarType"
    // ASTVerifier.g:114:1: grammarType : ( LEXER_GRAMMAR | PARSER_GRAMMAR | TREE_GRAMMAR | COMBINED_GRAMMAR );
    public final void grammarType() throws RecognitionException {
        try {
            // ASTVerifier.g:115:5: ( LEXER_GRAMMAR | PARSER_GRAMMAR | TREE_GRAMMAR | COMBINED_GRAMMAR )
            // ASTVerifier.g:
            {
            if ( (input.LA(1)>=LEXER_GRAMMAR && input.LA(1)<=COMBINED_GRAMMAR) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "grammarType"


    // $ANTLR start "prequelConstruct"
    // ASTVerifier.g:118:1: prequelConstruct : ( optionsSpec | delegateGrammars | tokensSpec | attrScope | action );
    public final void prequelConstruct() throws RecognitionException {
        try {
            // ASTVerifier.g:119:2: ( optionsSpec | delegateGrammars | tokensSpec | attrScope | action )
            int alt3=5;
            switch ( input.LA(1) ) {
            case OPTIONS:
                {
                alt3=1;
                }
                break;
            case IMPORT:
                {
                alt3=2;
                }
                break;
            case TOKENS:
                {
                alt3=3;
                }
                break;
            case SCOPE:
                {
                alt3=4;
                }
                break;
            case AT:
                {
                alt3=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }

            switch (alt3) {
                case 1 :
                    // ASTVerifier.g:119:6: optionsSpec
                    {
                    pushFollow(FOLLOW_optionsSpec_in_prequelConstruct131);
                    optionsSpec();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // ASTVerifier.g:120:9: delegateGrammars
                    {
                    pushFollow(FOLLOW_delegateGrammars_in_prequelConstruct141);
                    delegateGrammars();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // ASTVerifier.g:121:9: tokensSpec
                    {
                    pushFollow(FOLLOW_tokensSpec_in_prequelConstruct151);
                    tokensSpec();

                    state._fsp--;


                    }
                    break;
                case 4 :
                    // ASTVerifier.g:122:9: attrScope
                    {
                    pushFollow(FOLLOW_attrScope_in_prequelConstruct161);
                    attrScope();

                    state._fsp--;


                    }
                    break;
                case 5 :
                    // ASTVerifier.g:123:9: action
                    {
                    pushFollow(FOLLOW_action_in_prequelConstruct171);
                    action();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "prequelConstruct"


    // $ANTLR start "optionsSpec"
    // ASTVerifier.g:126:1: optionsSpec : ^( OPTIONS ( option )+ ) ;
    public final void optionsSpec() throws RecognitionException {
        try {
            // ASTVerifier.g:127:2: ( ^( OPTIONS ( option )+ ) )
            // ASTVerifier.g:127:4: ^( OPTIONS ( option )+ )
            {
            match(input,OPTIONS,FOLLOW_OPTIONS_in_optionsSpec186);

            match(input, Token.DOWN, null);
            // ASTVerifier.g:127:14: ( option )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==ASSIGN) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // ASTVerifier.g:127:14: option
            	    {
            	    pushFollow(FOLLOW_option_in_optionsSpec188);
            	    option();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt4 >= 1 ) break loop4;
                        EarlyExitException eee =
                            new EarlyExitException(4, input);
                        throw eee;
                }
                cnt4++;
            } while (true);


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "optionsSpec"


    // $ANTLR start "option"
    // ASTVerifier.g:130:1: option : ^( ASSIGN ID optionValue ) ;
    public final void option() throws RecognitionException {
        try {
            // ASTVerifier.g:131:5: ( ^( ASSIGN ID optionValue ) )
            // ASTVerifier.g:131:9: ^( ASSIGN ID optionValue )
            {
            match(input,ASSIGN,FOLLOW_ASSIGN_in_option210);

            match(input, Token.DOWN, null);
            match(input,ID,FOLLOW_ID_in_option212);
            pushFollow(FOLLOW_optionValue_in_option214);
            optionValue();

            state._fsp--;


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "option"

    public static class optionValue_return extends TreeRuleReturnScope {
        public String v;
    };

    // $ANTLR start "optionValue"
    // ASTVerifier.g:134:1: optionValue returns [String v] : ( ID | STRING_LITERAL | INT | STAR );
    public final ASTVerifier.optionValue_return optionValue() throws RecognitionException {
        ASTVerifier.optionValue_return retval = new ASTVerifier.optionValue_return();
        retval.start = input.LT(1);

        retval.v = ((GrammarAST)retval.start).token.getText();
        try {
            // ASTVerifier.g:136:5: ( ID | STRING_LITERAL | INT | STAR )
            // ASTVerifier.g:
            {
            if ( input.LA(1)==STAR||input.LA(1)==INT||input.LA(1)==STRING_LITERAL||input.LA(1)==ID ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "optionValue"


    // $ANTLR start "delegateGrammars"
    // ASTVerifier.g:142:1: delegateGrammars : ^( IMPORT ( delegateGrammar )+ ) ;
    public final void delegateGrammars() throws RecognitionException {
        try {
            // ASTVerifier.g:143:2: ( ^( IMPORT ( delegateGrammar )+ ) )
            // ASTVerifier.g:143:6: ^( IMPORT ( delegateGrammar )+ )
            {
            match(input,IMPORT,FOLLOW_IMPORT_in_delegateGrammars299);

            match(input, Token.DOWN, null);
            // ASTVerifier.g:143:15: ( delegateGrammar )+
            int cnt5=0;
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==ASSIGN||LA5_0==ID) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // ASTVerifier.g:143:15: delegateGrammar
            	    {
            	    pushFollow(FOLLOW_delegateGrammar_in_delegateGrammars301);
            	    delegateGrammar();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt5 >= 1 ) break loop5;
                        EarlyExitException eee =
                            new EarlyExitException(5, input);
                        throw eee;
                }
                cnt5++;
            } while (true);


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "delegateGrammars"


    // $ANTLR start "delegateGrammar"
    // ASTVerifier.g:146:1: delegateGrammar : ( ^( ASSIGN ID ID ) | ID );
    public final void delegateGrammar() throws RecognitionException {
        try {
            // ASTVerifier.g:147:5: ( ^( ASSIGN ID ID ) | ID )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==ASSIGN) ) {
                alt6=1;
            }
            else if ( (LA6_0==ID) ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // ASTVerifier.g:147:9: ^( ASSIGN ID ID )
                    {
                    match(input,ASSIGN,FOLLOW_ASSIGN_in_delegateGrammar320);

                    match(input, Token.DOWN, null);
                    match(input,ID,FOLLOW_ID_in_delegateGrammar322);
                    match(input,ID,FOLLOW_ID_in_delegateGrammar324);

                    match(input, Token.UP, null);

                    }
                    break;
                case 2 :
                    // ASTVerifier.g:148:9: ID
                    {
                    match(input,ID,FOLLOW_ID_in_delegateGrammar335);

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "delegateGrammar"


    // $ANTLR start "tokensSpec"
    // ASTVerifier.g:151:1: tokensSpec : ^( TOKENS ( tokenSpec )+ ) ;
    public final void tokensSpec() throws RecognitionException {
        try {
            // ASTVerifier.g:152:2: ( ^( TOKENS ( tokenSpec )+ ) )
            // ASTVerifier.g:152:6: ^( TOKENS ( tokenSpec )+ )
            {
            match(input,TOKENS,FOLLOW_TOKENS_in_tokensSpec352);

            match(input, Token.DOWN, null);
            // ASTVerifier.g:152:15: ( tokenSpec )+
            int cnt7=0;
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==ASSIGN||(LA7_0>=TOKEN_REF && LA7_0<=RULE_REF)) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // ASTVerifier.g:152:15: tokenSpec
            	    {
            	    pushFollow(FOLLOW_tokenSpec_in_tokensSpec354);
            	    tokenSpec();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt7 >= 1 ) break loop7;
                        EarlyExitException eee =
                            new EarlyExitException(7, input);
                        throw eee;
                }
                cnt7++;
            } while (true);


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "tokensSpec"


    // $ANTLR start "tokenSpec"
    // ASTVerifier.g:155:1: tokenSpec : ( ^( ASSIGN TOKEN_REF STRING_LITERAL ) | TOKEN_REF | RULE_REF );
    public final void tokenSpec() throws RecognitionException {
        try {
            // ASTVerifier.g:156:2: ( ^( ASSIGN TOKEN_REF STRING_LITERAL ) | TOKEN_REF | RULE_REF )
            int alt8=3;
            switch ( input.LA(1) ) {
            case ASSIGN:
                {
                alt8=1;
                }
                break;
            case TOKEN_REF:
                {
                alt8=2;
                }
                break;
            case RULE_REF:
                {
                alt8=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }

            switch (alt8) {
                case 1 :
                    // ASTVerifier.g:156:4: ^( ASSIGN TOKEN_REF STRING_LITERAL )
                    {
                    match(input,ASSIGN,FOLLOW_ASSIGN_in_tokenSpec368);

                    match(input, Token.DOWN, null);
                    match(input,TOKEN_REF,FOLLOW_TOKEN_REF_in_tokenSpec370);
                    match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_tokenSpec372);

                    match(input, Token.UP, null);

                    }
                    break;
                case 2 :
                    // ASTVerifier.g:157:6: TOKEN_REF
                    {
                    match(input,TOKEN_REF,FOLLOW_TOKEN_REF_in_tokenSpec380);

                    }
                    break;
                case 3 :
                    // ASTVerifier.g:158:4: RULE_REF
                    {
                    match(input,RULE_REF,FOLLOW_RULE_REF_in_tokenSpec385);

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "tokenSpec"


    // $ANTLR start "attrScope"
    // ASTVerifier.g:161:1: attrScope : ^( SCOPE ID ACTION ) ;
    public final void attrScope() throws RecognitionException {
        try {
            // ASTVerifier.g:162:2: ( ^( SCOPE ID ACTION ) )
            // ASTVerifier.g:162:4: ^( SCOPE ID ACTION )
            {
            match(input,SCOPE,FOLLOW_SCOPE_in_attrScope397);

            match(input, Token.DOWN, null);
            match(input,ID,FOLLOW_ID_in_attrScope399);
            match(input,ACTION,FOLLOW_ACTION_in_attrScope401);

            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "attrScope"


    // $ANTLR start "action"
    // ASTVerifier.g:165:1: action : ^( AT ( ID )? ID ACTION ) ;
    public final void action() throws RecognitionException {
        try {
            // ASTVerifier.g:166:2: ( ^( AT ( ID )? ID ACTION ) )
            // ASTVerifier.g:166:4: ^( AT ( ID )? ID ACTION )
            {
            match(input,AT,FOLLOW_AT_in_action414);

            match(input, Token.DOWN, null);
            // ASTVerifier.g:166:9: ( ID )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==ID) ) {
                int LA9_1 = input.LA(2);

                if ( (LA9_1==ID) ) {
                    alt9=1;
                }
            }
            switch (alt9) {
                case 1 :
                    // ASTVerifier.g:166:9: ID
                    {
                    match(input,ID,FOLLOW_ID_in_action416);

                    }
                    break;

            }

            match(input,ID,FOLLOW_ID_in_action419);
            match(input,ACTION,FOLLOW_ACTION_in_action421);

            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "action"


    // $ANTLR start "rules"
    // ASTVerifier.g:169:1: rules : ^( RULES ( rule )* ) ;
    public final void rules() throws RecognitionException {
        try {
            // ASTVerifier.g:170:5: ( ^( RULES ( rule )* ) )
            // ASTVerifier.g:170:7: ^( RULES ( rule )* )
            {
            match(input,RULES,FOLLOW_RULES_in_rules437);

            if ( input.LA(1)==Token.DOWN ) {
                match(input, Token.DOWN, null);
                // ASTVerifier.g:170:15: ( rule )*
                loop10:
                do {
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==RULE) ) {
                        alt10=1;
                    }


                    switch (alt10) {
                	case 1 :
                	    // ASTVerifier.g:170:15: rule
                	    {
                	    pushFollow(FOLLOW_rule_in_rules439);
                	    rule();

                	    state._fsp--;


                	    }
                	    break;

                	default :
                	    break loop10;
                    }
                } while (true);


                match(input, Token.UP, null);
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "rules"


    // $ANTLR start "rule"
    // ASTVerifier.g:173:1: rule : ^( RULE ID ( DOC_COMMENT )? ( ruleModifiers )? ( ARG_ACTION )? ( ruleReturns )? ( rulePrequel )* altListAsBlock exceptionGroup ) ;
    public final void rule() throws RecognitionException {
        try {
            // ASTVerifier.g:173:5: ( ^( RULE ID ( DOC_COMMENT )? ( ruleModifiers )? ( ARG_ACTION )? ( ruleReturns )? ( rulePrequel )* altListAsBlock exceptionGroup ) )
            // ASTVerifier.g:173:9: ^( RULE ID ( DOC_COMMENT )? ( ruleModifiers )? ( ARG_ACTION )? ( ruleReturns )? ( rulePrequel )* altListAsBlock exceptionGroup )
            {
            match(input,RULE,FOLLOW_RULE_in_rule457);

            match(input, Token.DOWN, null);
            match(input,ID,FOLLOW_ID_in_rule459);
            // ASTVerifier.g:173:20: ( DOC_COMMENT )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==DOC_COMMENT) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // ASTVerifier.g:173:20: DOC_COMMENT
                    {
                    match(input,DOC_COMMENT,FOLLOW_DOC_COMMENT_in_rule461);

                    }
                    break;

            }

            // ASTVerifier.g:173:33: ( ruleModifiers )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==RULEMODIFIERS) ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // ASTVerifier.g:173:33: ruleModifiers
                    {
                    pushFollow(FOLLOW_ruleModifiers_in_rule464);
                    ruleModifiers();

                    state._fsp--;


                    }
                    break;

            }

            // ASTVerifier.g:173:48: ( ARG_ACTION )?
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==ARG_ACTION) ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // ASTVerifier.g:173:48: ARG_ACTION
                    {
                    match(input,ARG_ACTION,FOLLOW_ARG_ACTION_in_rule467);

                    }
                    break;

            }

            // ASTVerifier.g:174:11: ( ruleReturns )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==RETURNS) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // ASTVerifier.g:174:11: ruleReturns
                    {
                    pushFollow(FOLLOW_ruleReturns_in_rule480);
                    ruleReturns();

                    state._fsp--;


                    }
                    break;

            }

            // ASTVerifier.g:174:24: ( rulePrequel )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==OPTIONS||LA15_0==SCOPE||LA15_0==THROWS||LA15_0==AT) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // ASTVerifier.g:174:24: rulePrequel
            	    {
            	    pushFollow(FOLLOW_rulePrequel_in_rule483);
            	    rulePrequel();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);

            pushFollow(FOLLOW_altListAsBlock_in_rule486);
            altListAsBlock();

            state._fsp--;

            pushFollow(FOLLOW_exceptionGroup_in_rule488);
            exceptionGroup();

            state._fsp--;


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "rule"


    // $ANTLR start "exceptionGroup"
    // ASTVerifier.g:178:1: exceptionGroup : ( exceptionHandler )* ( finallyClause )? ;
    public final void exceptionGroup() throws RecognitionException {
        try {
            // ASTVerifier.g:179:5: ( ( exceptionHandler )* ( finallyClause )? )
            // ASTVerifier.g:179:7: ( exceptionHandler )* ( finallyClause )?
            {
            // ASTVerifier.g:179:7: ( exceptionHandler )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==CATCH) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // ASTVerifier.g:179:7: exceptionHandler
            	    {
            	    pushFollow(FOLLOW_exceptionHandler_in_exceptionGroup515);
            	    exceptionHandler();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);

            // ASTVerifier.g:179:25: ( finallyClause )?
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==FINALLY) ) {
                alt17=1;
            }
            switch (alt17) {
                case 1 :
                    // ASTVerifier.g:179:25: finallyClause
                    {
                    pushFollow(FOLLOW_finallyClause_in_exceptionGroup518);
                    finallyClause();

                    state._fsp--;


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "exceptionGroup"


    // $ANTLR start "exceptionHandler"
    // ASTVerifier.g:182:1: exceptionHandler : ^( CATCH ARG_ACTION ACTION ) ;
    public final void exceptionHandler() throws RecognitionException {
        try {
            // ASTVerifier.g:183:2: ( ^( CATCH ARG_ACTION ACTION ) )
            // ASTVerifier.g:183:4: ^( CATCH ARG_ACTION ACTION )
            {
            match(input,CATCH,FOLLOW_CATCH_in_exceptionHandler534);

            match(input, Token.DOWN, null);
            match(input,ARG_ACTION,FOLLOW_ARG_ACTION_in_exceptionHandler536);
            match(input,ACTION,FOLLOW_ACTION_in_exceptionHandler538);

            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "exceptionHandler"


    // $ANTLR start "finallyClause"
    // ASTVerifier.g:186:1: finallyClause : ^( FINALLY ACTION ) ;
    public final void finallyClause() throws RecognitionException {
        try {
            // ASTVerifier.g:187:2: ( ^( FINALLY ACTION ) )
            // ASTVerifier.g:187:4: ^( FINALLY ACTION )
            {
            match(input,FINALLY,FOLLOW_FINALLY_in_finallyClause551);

            match(input, Token.DOWN, null);
            match(input,ACTION,FOLLOW_ACTION_in_finallyClause553);

            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "finallyClause"


    // $ANTLR start "rulePrequel"
    // ASTVerifier.g:190:1: rulePrequel : ( throwsSpec | ruleScopeSpec | optionsSpec | ruleAction );
    public final void rulePrequel() throws RecognitionException {
        try {
            // ASTVerifier.g:191:5: ( throwsSpec | ruleScopeSpec | optionsSpec | ruleAction )
            int alt18=4;
            switch ( input.LA(1) ) {
            case THROWS:
                {
                alt18=1;
                }
                break;
            case SCOPE:
                {
                alt18=2;
                }
                break;
            case OPTIONS:
                {
                alt18=3;
                }
                break;
            case AT:
                {
                alt18=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;
            }

            switch (alt18) {
                case 1 :
                    // ASTVerifier.g:191:9: throwsSpec
                    {
                    pushFollow(FOLLOW_throwsSpec_in_rulePrequel570);
                    throwsSpec();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // ASTVerifier.g:192:9: ruleScopeSpec
                    {
                    pushFollow(FOLLOW_ruleScopeSpec_in_rulePrequel580);
                    ruleScopeSpec();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // ASTVerifier.g:193:9: optionsSpec
                    {
                    pushFollow(FOLLOW_optionsSpec_in_rulePrequel590);
                    optionsSpec();

                    state._fsp--;


                    }
                    break;
                case 4 :
                    // ASTVerifier.g:194:9: ruleAction
                    {
                    pushFollow(FOLLOW_ruleAction_in_rulePrequel600);
                    ruleAction();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "rulePrequel"


    // $ANTLR start "ruleReturns"
    // ASTVerifier.g:197:1: ruleReturns : ^( RETURNS ARG_ACTION ) ;
    public final void ruleReturns() throws RecognitionException {
        try {
            // ASTVerifier.g:198:2: ( ^( RETURNS ARG_ACTION ) )
            // ASTVerifier.g:198:4: ^( RETURNS ARG_ACTION )
            {
            match(input,RETURNS,FOLLOW_RETURNS_in_ruleReturns615);

            match(input, Token.DOWN, null);
            match(input,ARG_ACTION,FOLLOW_ARG_ACTION_in_ruleReturns617);

            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleReturns"


    // $ANTLR start "throwsSpec"
    // ASTVerifier.g:200:1: throwsSpec : ^( THROWS ( ID )+ ) ;
    public final void throwsSpec() throws RecognitionException {
        try {
            // ASTVerifier.g:201:5: ( ^( THROWS ( ID )+ ) )
            // ASTVerifier.g:201:7: ^( THROWS ( ID )+ )
            {
            match(input,THROWS,FOLLOW_THROWS_in_throwsSpec632);

            match(input, Token.DOWN, null);
            // ASTVerifier.g:201:16: ( ID )+
            int cnt19=0;
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( (LA19_0==ID) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // ASTVerifier.g:201:16: ID
            	    {
            	    match(input,ID,FOLLOW_ID_in_throwsSpec634);

            	    }
            	    break;

            	default :
            	    if ( cnt19 >= 1 ) break loop19;
                        EarlyExitException eee =
                            new EarlyExitException(19, input);
                        throw eee;
                }
                cnt19++;
            } while (true);


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "throwsSpec"


    // $ANTLR start "ruleScopeSpec"
    // ASTVerifier.g:204:1: ruleScopeSpec : ( ^( SCOPE ACTION ) | ^( SCOPE ( ID )+ ) );
    public final void ruleScopeSpec() throws RecognitionException {
        try {
            // ASTVerifier.g:205:2: ( ^( SCOPE ACTION ) | ^( SCOPE ( ID )+ ) )
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==SCOPE) ) {
                int LA21_1 = input.LA(2);

                if ( (LA21_1==DOWN) ) {
                    int LA21_2 = input.LA(3);

                    if ( (LA21_2==ACTION) ) {
                        alt21=1;
                    }
                    else if ( (LA21_2==ID) ) {
                        alt21=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 21, 2, input);

                        throw nvae;
                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 21, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;
            }
            switch (alt21) {
                case 1 :
                    // ASTVerifier.g:205:4: ^( SCOPE ACTION )
                    {
                    match(input,SCOPE,FOLLOW_SCOPE_in_ruleScopeSpec651);

                    match(input, Token.DOWN, null);
                    match(input,ACTION,FOLLOW_ACTION_in_ruleScopeSpec653);

                    match(input, Token.UP, null);

                    }
                    break;
                case 2 :
                    // ASTVerifier.g:206:4: ^( SCOPE ( ID )+ )
                    {
                    match(input,SCOPE,FOLLOW_SCOPE_in_ruleScopeSpec660);

                    match(input, Token.DOWN, null);
                    // ASTVerifier.g:206:12: ( ID )+
                    int cnt20=0;
                    loop20:
                    do {
                        int alt20=2;
                        int LA20_0 = input.LA(1);

                        if ( (LA20_0==ID) ) {
                            alt20=1;
                        }


                        switch (alt20) {
                    	case 1 :
                    	    // ASTVerifier.g:206:12: ID
                    	    {
                    	    match(input,ID,FOLLOW_ID_in_ruleScopeSpec662);

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt20 >= 1 ) break loop20;
                                EarlyExitException eee =
                                    new EarlyExitException(20, input);
                                throw eee;
                        }
                        cnt20++;
                    } while (true);


                    match(input, Token.UP, null);

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleScopeSpec"


    // $ANTLR start "ruleAction"
    // ASTVerifier.g:209:1: ruleAction : ^( AT ID ACTION ) ;
    public final void ruleAction() throws RecognitionException {
        try {
            // ASTVerifier.g:210:2: ( ^( AT ID ACTION ) )
            // ASTVerifier.g:210:4: ^( AT ID ACTION )
            {
            match(input,AT,FOLLOW_AT_in_ruleAction676);

            match(input, Token.DOWN, null);
            match(input,ID,FOLLOW_ID_in_ruleAction678);
            match(input,ACTION,FOLLOW_ACTION_in_ruleAction680);

            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleAction"


    // $ANTLR start "ruleModifiers"
    // ASTVerifier.g:213:1: ruleModifiers : ^( RULEMODIFIERS ( ruleModifier )+ ) ;
    public final void ruleModifiers() throws RecognitionException {
        try {
            // ASTVerifier.g:214:5: ( ^( RULEMODIFIERS ( ruleModifier )+ ) )
            // ASTVerifier.g:214:7: ^( RULEMODIFIERS ( ruleModifier )+ )
            {
            match(input,RULEMODIFIERS,FOLLOW_RULEMODIFIERS_in_ruleModifiers696);

            match(input, Token.DOWN, null);
            // ASTVerifier.g:214:23: ( ruleModifier )+
            int cnt22=0;
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( (LA22_0==FRAGMENT||(LA22_0>=PROTECTED && LA22_0<=PRIVATE)) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // ASTVerifier.g:214:23: ruleModifier
            	    {
            	    pushFollow(FOLLOW_ruleModifier_in_ruleModifiers698);
            	    ruleModifier();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt22 >= 1 ) break loop22;
                        EarlyExitException eee =
                            new EarlyExitException(22, input);
                        throw eee;
                }
                cnt22++;
            } while (true);


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleModifiers"


    // $ANTLR start "ruleModifier"
    // ASTVerifier.g:217:1: ruleModifier : ( PUBLIC | PRIVATE | PROTECTED | FRAGMENT );
    public final void ruleModifier() throws RecognitionException {
        try {
            // ASTVerifier.g:218:5: ( PUBLIC | PRIVATE | PROTECTED | FRAGMENT )
            // ASTVerifier.g:
            {
            if ( input.LA(1)==FRAGMENT||(input.LA(1)>=PROTECTED && input.LA(1)<=PRIVATE) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleModifier"


    // $ANTLR start "altList"
    // ASTVerifier.g:224:1: altList : ( alternative )+ ;
    public final void altList() throws RecognitionException {
        try {
            // ASTVerifier.g:225:5: ( ( alternative )+ )
            // ASTVerifier.g:225:7: ( alternative )+
            {
            // ASTVerifier.g:225:7: ( alternative )+
            int cnt23=0;
            loop23:
            do {
                int alt23=2;
                int LA23_0 = input.LA(1);

                if ( (LA23_0==ALT||LA23_0==ALT_REWRITE) ) {
                    alt23=1;
                }


                switch (alt23) {
            	case 1 :
            	    // ASTVerifier.g:225:7: alternative
            	    {
            	    pushFollow(FOLLOW_alternative_in_altList758);
            	    alternative();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt23 >= 1 ) break loop23;
                        EarlyExitException eee =
                            new EarlyExitException(23, input);
                        throw eee;
                }
                cnt23++;
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "altList"


    // $ANTLR start "altListAsBlock"
    // ASTVerifier.g:228:1: altListAsBlock : ^( BLOCK altList ) ;
    public final void altListAsBlock() throws RecognitionException {
        try {
            // ASTVerifier.g:229:5: ( ^( BLOCK altList ) )
            // ASTVerifier.g:229:7: ^( BLOCK altList )
            {
            match(input,BLOCK,FOLLOW_BLOCK_in_altListAsBlock777);

            match(input, Token.DOWN, null);
            pushFollow(FOLLOW_altList_in_altListAsBlock779);
            altList();

            state._fsp--;


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "altListAsBlock"


    // $ANTLR start "alternative"
    // ASTVerifier.g:232:1: alternative : ( ^( ALT_REWRITE alternative rewrite ) | ^( ALT EPSILON ) | elements );
    public final void alternative() throws RecognitionException {
        try {
            // ASTVerifier.g:233:5: ( ^( ALT_REWRITE alternative rewrite ) | ^( ALT EPSILON ) | elements )
            int alt24=3;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==ALT_REWRITE) ) {
                alt24=1;
            }
            else if ( (LA24_0==ALT) ) {
                int LA24_2 = input.LA(2);

                if ( (LA24_2==DOWN) ) {
                    int LA24_3 = input.LA(3);

                    if ( (LA24_3==EPSILON) ) {
                        alt24=2;
                    }
                    else if ( (LA24_3==SEMPRED||LA24_3==ACTION||LA24_3==IMPLIES||LA24_3==ASSIGN||LA24_3==BANG||LA24_3==PLUS_ASSIGN||LA24_3==ROOT||(LA24_3>=WILDCARD && LA24_3<=RANGE)||LA24_3==TREE_BEGIN||(LA24_3>=TOKEN_REF && LA24_3<=RULE_REF)||LA24_3==STRING_LITERAL||(LA24_3>=BLOCK && LA24_3<=POSITIVE_CLOSURE)||LA24_3==GATED_SEMPRED||LA24_3==DOT) ) {
                        alt24=3;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 24, 3, input);

                        throw nvae;
                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 24, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 24, 0, input);

                throw nvae;
            }
            switch (alt24) {
                case 1 :
                    // ASTVerifier.g:233:7: ^( ALT_REWRITE alternative rewrite )
                    {
                    match(input,ALT_REWRITE,FOLLOW_ALT_REWRITE_in_alternative798);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_alternative_in_alternative800);
                    alternative();

                    state._fsp--;

                    pushFollow(FOLLOW_rewrite_in_alternative802);
                    rewrite();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;
                case 2 :
                    // ASTVerifier.g:234:7: ^( ALT EPSILON )
                    {
                    match(input,ALT,FOLLOW_ALT_in_alternative812);

                    match(input, Token.DOWN, null);
                    match(input,EPSILON,FOLLOW_EPSILON_in_alternative814);

                    match(input, Token.UP, null);

                    }
                    break;
                case 3 :
                    // ASTVerifier.g:235:9: elements
                    {
                    pushFollow(FOLLOW_elements_in_alternative825);
                    elements();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "alternative"


    // $ANTLR start "elements"
    // ASTVerifier.g:238:1: elements : ^( ALT ( element )+ ) ;
    public final void elements() throws RecognitionException {
        try {
            // ASTVerifier.g:239:5: ( ^( ALT ( element )+ ) )
            // ASTVerifier.g:239:7: ^( ALT ( element )+ )
            {
            match(input,ALT,FOLLOW_ALT_in_elements843);

            match(input, Token.DOWN, null);
            // ASTVerifier.g:239:13: ( element )+
            int cnt25=0;
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( (LA25_0==SEMPRED||LA25_0==ACTION||LA25_0==IMPLIES||LA25_0==ASSIGN||LA25_0==BANG||LA25_0==PLUS_ASSIGN||LA25_0==ROOT||(LA25_0>=WILDCARD && LA25_0<=RANGE)||LA25_0==TREE_BEGIN||(LA25_0>=TOKEN_REF && LA25_0<=RULE_REF)||LA25_0==STRING_LITERAL||(LA25_0>=BLOCK && LA25_0<=POSITIVE_CLOSURE)||LA25_0==GATED_SEMPRED||LA25_0==DOT) ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // ASTVerifier.g:239:13: element
            	    {
            	    pushFollow(FOLLOW_element_in_elements845);
            	    element();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt25 >= 1 ) break loop25;
                        EarlyExitException eee =
                            new EarlyExitException(25, input);
                        throw eee;
                }
                cnt25++;
            } while (true);


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "elements"


    // $ANTLR start "element"
    // ASTVerifier.g:242:1: element : ( labeledElement | atom | ebnf | ACTION | SEMPRED | GATED_SEMPRED | treeSpec );
    public final void element() throws RecognitionException {
        try {
            // ASTVerifier.g:243:2: ( labeledElement | atom | ebnf | ACTION | SEMPRED | GATED_SEMPRED | treeSpec )
            int alt26=7;
            alt26 = dfa26.predict(input);
            switch (alt26) {
                case 1 :
                    // ASTVerifier.g:243:4: labeledElement
                    {
                    pushFollow(FOLLOW_labeledElement_in_element861);
                    labeledElement();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // ASTVerifier.g:244:4: atom
                    {
                    pushFollow(FOLLOW_atom_in_element866);
                    atom();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // ASTVerifier.g:245:4: ebnf
                    {
                    pushFollow(FOLLOW_ebnf_in_element871);
                    ebnf();

                    state._fsp--;


                    }
                    break;
                case 4 :
                    // ASTVerifier.g:246:6: ACTION
                    {
                    match(input,ACTION,FOLLOW_ACTION_in_element878);

                    }
                    break;
                case 5 :
                    // ASTVerifier.g:247:6: SEMPRED
                    {
                    match(input,SEMPRED,FOLLOW_SEMPRED_in_element885);

                    }
                    break;
                case 6 :
                    // ASTVerifier.g:248:4: GATED_SEMPRED
                    {
                    match(input,GATED_SEMPRED,FOLLOW_GATED_SEMPRED_in_element890);

                    }
                    break;
                case 7 :
                    // ASTVerifier.g:249:4: treeSpec
                    {
                    pushFollow(FOLLOW_treeSpec_in_element895);
                    treeSpec();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "element"


    // $ANTLR start "labeledElement"
    // ASTVerifier.g:252:1: labeledElement : ( ^( ASSIGN ID ( atom | block ) ) | ^( PLUS_ASSIGN ID ( atom | block ) ) );
    public final void labeledElement() throws RecognitionException {
        try {
            // ASTVerifier.g:253:2: ( ^( ASSIGN ID ( atom | block ) ) | ^( PLUS_ASSIGN ID ( atom | block ) ) )
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==ASSIGN) ) {
                alt29=1;
            }
            else if ( (LA29_0==PLUS_ASSIGN) ) {
                alt29=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 29, 0, input);

                throw nvae;
            }
            switch (alt29) {
                case 1 :
                    // ASTVerifier.g:253:4: ^( ASSIGN ID ( atom | block ) )
                    {
                    match(input,ASSIGN,FOLLOW_ASSIGN_in_labeledElement908);

                    match(input, Token.DOWN, null);
                    match(input,ID,FOLLOW_ID_in_labeledElement910);
                    // ASTVerifier.g:253:16: ( atom | block )
                    int alt27=2;
                    int LA27_0 = input.LA(1);

                    if ( (LA27_0==BANG||LA27_0==ROOT||(LA27_0>=WILDCARD && LA27_0<=RANGE)||(LA27_0>=TOKEN_REF && LA27_0<=RULE_REF)||LA27_0==STRING_LITERAL||LA27_0==DOT) ) {
                        alt27=1;
                    }
                    else if ( (LA27_0==BLOCK) ) {
                        alt27=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 27, 0, input);

                        throw nvae;
                    }
                    switch (alt27) {
                        case 1 :
                            // ASTVerifier.g:253:17: atom
                            {
                            pushFollow(FOLLOW_atom_in_labeledElement913);
                            atom();

                            state._fsp--;


                            }
                            break;
                        case 2 :
                            // ASTVerifier.g:253:22: block
                            {
                            pushFollow(FOLLOW_block_in_labeledElement915);
                            block();

                            state._fsp--;


                            }
                            break;

                    }


                    match(input, Token.UP, null);

                    }
                    break;
                case 2 :
                    // ASTVerifier.g:254:4: ^( PLUS_ASSIGN ID ( atom | block ) )
                    {
                    match(input,PLUS_ASSIGN,FOLLOW_PLUS_ASSIGN_in_labeledElement923);

                    match(input, Token.DOWN, null);
                    match(input,ID,FOLLOW_ID_in_labeledElement925);
                    // ASTVerifier.g:254:21: ( atom | block )
                    int alt28=2;
                    int LA28_0 = input.LA(1);

                    if ( (LA28_0==BANG||LA28_0==ROOT||(LA28_0>=WILDCARD && LA28_0<=RANGE)||(LA28_0>=TOKEN_REF && LA28_0<=RULE_REF)||LA28_0==STRING_LITERAL||LA28_0==DOT) ) {
                        alt28=1;
                    }
                    else if ( (LA28_0==BLOCK) ) {
                        alt28=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 28, 0, input);

                        throw nvae;
                    }
                    switch (alt28) {
                        case 1 :
                            // ASTVerifier.g:254:22: atom
                            {
                            pushFollow(FOLLOW_atom_in_labeledElement928);
                            atom();

                            state._fsp--;


                            }
                            break;
                        case 2 :
                            // ASTVerifier.g:254:27: block
                            {
                            pushFollow(FOLLOW_block_in_labeledElement930);
                            block();

                            state._fsp--;


                            }
                            break;

                    }


                    match(input, Token.UP, null);

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "labeledElement"


    // $ANTLR start "treeSpec"
    // ASTVerifier.g:257:1: treeSpec : ^( TREE_BEGIN ( element )+ ) ;
    public final void treeSpec() throws RecognitionException {
        try {
            // ASTVerifier.g:258:5: ( ^( TREE_BEGIN ( element )+ ) )
            // ASTVerifier.g:258:7: ^( TREE_BEGIN ( element )+ )
            {
            match(input,TREE_BEGIN,FOLLOW_TREE_BEGIN_in_treeSpec947);

            match(input, Token.DOWN, null);
            // ASTVerifier.g:258:20: ( element )+
            int cnt30=0;
            loop30:
            do {
                int alt30=2;
                int LA30_0 = input.LA(1);

                if ( (LA30_0==SEMPRED||LA30_0==ACTION||LA30_0==IMPLIES||LA30_0==ASSIGN||LA30_0==BANG||LA30_0==PLUS_ASSIGN||LA30_0==ROOT||(LA30_0>=WILDCARD && LA30_0<=RANGE)||LA30_0==TREE_BEGIN||(LA30_0>=TOKEN_REF && LA30_0<=RULE_REF)||LA30_0==STRING_LITERAL||(LA30_0>=BLOCK && LA30_0<=POSITIVE_CLOSURE)||LA30_0==GATED_SEMPRED||LA30_0==DOT) ) {
                    alt30=1;
                }


                switch (alt30) {
            	case 1 :
            	    // ASTVerifier.g:258:20: element
            	    {
            	    pushFollow(FOLLOW_element_in_treeSpec949);
            	    element();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt30 >= 1 ) break loop30;
                        EarlyExitException eee =
                            new EarlyExitException(30, input);
                        throw eee;
                }
                cnt30++;
            } while (true);


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "treeSpec"


    // $ANTLR start "ebnf"
    // ASTVerifier.g:261:1: ebnf : ( ^( blockSuffix block ) | block );
    public final void ebnf() throws RecognitionException {
        try {
            // ASTVerifier.g:261:5: ( ^( blockSuffix block ) | block )
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==IMPLIES||LA31_0==BANG||LA31_0==ROOT||(LA31_0>=OPTIONAL && LA31_0<=POSITIVE_CLOSURE)) ) {
                alt31=1;
            }
            else if ( (LA31_0==BLOCK) ) {
                alt31=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;
            }
            switch (alt31) {
                case 1 :
                    // ASTVerifier.g:261:7: ^( blockSuffix block )
                    {
                    pushFollow(FOLLOW_blockSuffix_in_ebnf964);
                    blockSuffix();

                    state._fsp--;


                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_block_in_ebnf966);
                    block();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;
                case 2 :
                    // ASTVerifier.g:262:5: block
                    {
                    pushFollow(FOLLOW_block_in_ebnf973);
                    block();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ebnf"


    // $ANTLR start "blockSuffix"
    // ASTVerifier.g:265:1: blockSuffix : ( ebnfSuffix | ROOT | IMPLIES | BANG );
    public final void blockSuffix() throws RecognitionException {
        try {
            // ASTVerifier.g:266:5: ( ebnfSuffix | ROOT | IMPLIES | BANG )
            int alt32=4;
            switch ( input.LA(1) ) {
            case OPTIONAL:
            case CLOSURE:
            case POSITIVE_CLOSURE:
                {
                alt32=1;
                }
                break;
            case ROOT:
                {
                alt32=2;
                }
                break;
            case IMPLIES:
                {
                alt32=3;
                }
                break;
            case BANG:
                {
                alt32=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;
            }

            switch (alt32) {
                case 1 :
                    // ASTVerifier.g:266:7: ebnfSuffix
                    {
                    pushFollow(FOLLOW_ebnfSuffix_in_blockSuffix990);
                    ebnfSuffix();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // ASTVerifier.g:267:7: ROOT
                    {
                    match(input,ROOT,FOLLOW_ROOT_in_blockSuffix998);

                    }
                    break;
                case 3 :
                    // ASTVerifier.g:268:7: IMPLIES
                    {
                    match(input,IMPLIES,FOLLOW_IMPLIES_in_blockSuffix1006);

                    }
                    break;
                case 4 :
                    // ASTVerifier.g:269:7: BANG
                    {
                    match(input,BANG,FOLLOW_BANG_in_blockSuffix1014);

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "blockSuffix"


    // $ANTLR start "ebnfSuffix"
    // ASTVerifier.g:272:1: ebnfSuffix : ( OPTIONAL | CLOSURE | POSITIVE_CLOSURE );
    public final void ebnfSuffix() throws RecognitionException {
        try {
            // ASTVerifier.g:273:2: ( OPTIONAL | CLOSURE | POSITIVE_CLOSURE )
            // ASTVerifier.g:
            {
            if ( (input.LA(1)>=OPTIONAL && input.LA(1)<=POSITIVE_CLOSURE) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ebnfSuffix"


    // $ANTLR start "atom"
    // ASTVerifier.g:278:1: atom : ( ^( ROOT range ) | ^( BANG range ) | ^( ROOT notSet ) | ^( BANG notSet ) | range | ^( DOT ID terminal ) | ^( DOT ID ruleref ) | terminal | ruleref );
    public final void atom() throws RecognitionException {
        try {
            // ASTVerifier.g:278:5: ( ^( ROOT range ) | ^( BANG range ) | ^( ROOT notSet ) | ^( BANG notSet ) | range | ^( DOT ID terminal ) | ^( DOT ID ruleref ) | terminal | ruleref )
            int alt33=9;
            alt33 = dfa33.predict(input);
            switch (alt33) {
                case 1 :
                    // ASTVerifier.g:278:7: ^( ROOT range )
                    {
                    match(input,ROOT,FOLLOW_ROOT_in_atom1054);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_range_in_atom1056);
                    range();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;
                case 2 :
                    // ASTVerifier.g:279:4: ^( BANG range )
                    {
                    match(input,BANG,FOLLOW_BANG_in_atom1063);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_range_in_atom1065);
                    range();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;
                case 3 :
                    // ASTVerifier.g:280:4: ^( ROOT notSet )
                    {
                    match(input,ROOT,FOLLOW_ROOT_in_atom1072);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_notSet_in_atom1074);
                    notSet();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;
                case 4 :
                    // ASTVerifier.g:281:4: ^( BANG notSet )
                    {
                    match(input,BANG,FOLLOW_BANG_in_atom1081);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_notSet_in_atom1083);
                    notSet();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;
                case 5 :
                    // ASTVerifier.g:282:4: range
                    {
                    pushFollow(FOLLOW_range_in_atom1089);
                    range();

                    state._fsp--;


                    }
                    break;
                case 6 :
                    // ASTVerifier.g:283:4: ^( DOT ID terminal )
                    {
                    match(input,DOT,FOLLOW_DOT_in_atom1095);

                    match(input, Token.DOWN, null);
                    match(input,ID,FOLLOW_ID_in_atom1097);
                    pushFollow(FOLLOW_terminal_in_atom1099);
                    terminal();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;
                case 7 :
                    // ASTVerifier.g:284:4: ^( DOT ID ruleref )
                    {
                    match(input,DOT,FOLLOW_DOT_in_atom1106);

                    match(input, Token.DOWN, null);
                    match(input,ID,FOLLOW_ID_in_atom1108);
                    pushFollow(FOLLOW_ruleref_in_atom1110);
                    ruleref();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;
                case 8 :
                    // ASTVerifier.g:285:9: terminal
                    {
                    pushFollow(FOLLOW_terminal_in_atom1121);
                    terminal();

                    state._fsp--;


                    }
                    break;
                case 9 :
                    // ASTVerifier.g:286:9: ruleref
                    {
                    pushFollow(FOLLOW_ruleref_in_atom1131);
                    ruleref();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "atom"


    // $ANTLR start "notSet"
    // ASTVerifier.g:289:1: notSet : ( ^( NOT notTerminal ) | ^( NOT block ) );
    public final void notSet() throws RecognitionException {
        try {
            // ASTVerifier.g:290:5: ( ^( NOT notTerminal ) | ^( NOT block ) )
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==NOT) ) {
                int LA34_1 = input.LA(2);

                if ( (LA34_1==DOWN) ) {
                    int LA34_2 = input.LA(3);

                    if ( (LA34_2==TOKEN_REF||LA34_2==STRING_LITERAL) ) {
                        alt34=1;
                    }
                    else if ( (LA34_2==BLOCK) ) {
                        alt34=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 34, 2, input);

                        throw nvae;
                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 34, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                throw nvae;
            }
            switch (alt34) {
                case 1 :
                    // ASTVerifier.g:290:7: ^( NOT notTerminal )
                    {
                    match(input,NOT,FOLLOW_NOT_in_notSet1149);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_notTerminal_in_notSet1151);
                    notTerminal();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;
                case 2 :
                    // ASTVerifier.g:291:7: ^( NOT block )
                    {
                    match(input,NOT,FOLLOW_NOT_in_notSet1161);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_block_in_notSet1163);
                    block();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "notSet"


    // $ANTLR start "notTerminal"
    // ASTVerifier.g:294:1: notTerminal : ( TOKEN_REF | STRING_LITERAL );
    public final void notTerminal() throws RecognitionException {
        try {
            // ASTVerifier.g:295:5: ( TOKEN_REF | STRING_LITERAL )
            // ASTVerifier.g:
            {
            if ( input.LA(1)==TOKEN_REF||input.LA(1)==STRING_LITERAL ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "notTerminal"


    // $ANTLR start "block"
    // ASTVerifier.g:299:1: block : ^( BLOCK ( optionsSpec )? ( ruleAction )* ( ACTION )? altList ) ;
    public final void block() throws RecognitionException {
        try {
            // ASTVerifier.g:300:5: ( ^( BLOCK ( optionsSpec )? ( ruleAction )* ( ACTION )? altList ) )
            // ASTVerifier.g:300:7: ^( BLOCK ( optionsSpec )? ( ruleAction )* ( ACTION )? altList )
            {
            match(input,BLOCK,FOLLOW_BLOCK_in_block1207);

            match(input, Token.DOWN, null);
            // ASTVerifier.g:300:15: ( optionsSpec )?
            int alt35=2;
            int LA35_0 = input.LA(1);

            if ( (LA35_0==OPTIONS) ) {
                alt35=1;
            }
            switch (alt35) {
                case 1 :
                    // ASTVerifier.g:300:15: optionsSpec
                    {
                    pushFollow(FOLLOW_optionsSpec_in_block1209);
                    optionsSpec();

                    state._fsp--;


                    }
                    break;

            }

            // ASTVerifier.g:300:28: ( ruleAction )*
            loop36:
            do {
                int alt36=2;
                int LA36_0 = input.LA(1);

                if ( (LA36_0==AT) ) {
                    alt36=1;
                }


                switch (alt36) {
            	case 1 :
            	    // ASTVerifier.g:300:28: ruleAction
            	    {
            	    pushFollow(FOLLOW_ruleAction_in_block1212);
            	    ruleAction();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop36;
                }
            } while (true);

            // ASTVerifier.g:300:40: ( ACTION )?
            int alt37=2;
            int LA37_0 = input.LA(1);

            if ( (LA37_0==ACTION) ) {
                alt37=1;
            }
            switch (alt37) {
                case 1 :
                    // ASTVerifier.g:300:40: ACTION
                    {
                    match(input,ACTION,FOLLOW_ACTION_in_block1215);

                    }
                    break;

            }

            pushFollow(FOLLOW_altList_in_block1218);
            altList();

            state._fsp--;


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "block"


    // $ANTLR start "ruleref"
    // ASTVerifier.g:303:1: ruleref : ( ^( ROOT RULE_REF ( ARG_ACTION )? ) | ^( BANG RULE_REF ( ARG_ACTION )? ) | ^( RULE_REF ( ARG_ACTION )? ) );
    public final void ruleref() throws RecognitionException {
        try {
            // ASTVerifier.g:304:5: ( ^( ROOT RULE_REF ( ARG_ACTION )? ) | ^( BANG RULE_REF ( ARG_ACTION )? ) | ^( RULE_REF ( ARG_ACTION )? ) )
            int alt41=3;
            switch ( input.LA(1) ) {
            case ROOT:
                {
                alt41=1;
                }
                break;
            case BANG:
                {
                alt41=2;
                }
                break;
            case RULE_REF:
                {
                alt41=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 41, 0, input);

                throw nvae;
            }

            switch (alt41) {
                case 1 :
                    // ASTVerifier.g:304:7: ^( ROOT RULE_REF ( ARG_ACTION )? )
                    {
                    match(input,ROOT,FOLLOW_ROOT_in_ruleref1237);

                    match(input, Token.DOWN, null);
                    match(input,RULE_REF,FOLLOW_RULE_REF_in_ruleref1239);
                    // ASTVerifier.g:304:23: ( ARG_ACTION )?
                    int alt38=2;
                    int LA38_0 = input.LA(1);

                    if ( (LA38_0==ARG_ACTION) ) {
                        alt38=1;
                    }
                    switch (alt38) {
                        case 1 :
                            // ASTVerifier.g:304:23: ARG_ACTION
                            {
                            match(input,ARG_ACTION,FOLLOW_ARG_ACTION_in_ruleref1241);

                            }
                            break;

                    }


                    match(input, Token.UP, null);

                    }
                    break;
                case 2 :
                    // ASTVerifier.g:305:7: ^( BANG RULE_REF ( ARG_ACTION )? )
                    {
                    match(input,BANG,FOLLOW_BANG_in_ruleref1252);

                    match(input, Token.DOWN, null);
                    match(input,RULE_REF,FOLLOW_RULE_REF_in_ruleref1254);
                    // ASTVerifier.g:305:23: ( ARG_ACTION )?
                    int alt39=2;
                    int LA39_0 = input.LA(1);

                    if ( (LA39_0==ARG_ACTION) ) {
                        alt39=1;
                    }
                    switch (alt39) {
                        case 1 :
                            // ASTVerifier.g:305:23: ARG_ACTION
                            {
                            match(input,ARG_ACTION,FOLLOW_ARG_ACTION_in_ruleref1256);

                            }
                            break;

                    }


                    match(input, Token.UP, null);

                    }
                    break;
                case 3 :
                    // ASTVerifier.g:306:7: ^( RULE_REF ( ARG_ACTION )? )
                    {
                    match(input,RULE_REF,FOLLOW_RULE_REF_in_ruleref1267);

                    if ( input.LA(1)==Token.DOWN ) {
                        match(input, Token.DOWN, null);
                        // ASTVerifier.g:306:18: ( ARG_ACTION )?
                        int alt40=2;
                        int LA40_0 = input.LA(1);

                        if ( (LA40_0==ARG_ACTION) ) {
                            alt40=1;
                        }
                        switch (alt40) {
                            case 1 :
                                // ASTVerifier.g:306:18: ARG_ACTION
                                {
                                match(input,ARG_ACTION,FOLLOW_ARG_ACTION_in_ruleref1269);

                                }
                                break;

                        }


                        match(input, Token.UP, null);
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleref"


    // $ANTLR start "range"
    // ASTVerifier.g:309:1: range : ^( RANGE rangeElement rangeElement ) ;
    public final void range() throws RecognitionException {
        try {
            // ASTVerifier.g:310:5: ( ^( RANGE rangeElement rangeElement ) )
            // ASTVerifier.g:310:7: ^( RANGE rangeElement rangeElement )
            {
            match(input,RANGE,FOLLOW_RANGE_in_range1289);

            match(input, Token.DOWN, null);
            pushFollow(FOLLOW_rangeElement_in_range1291);
            rangeElement();

            state._fsp--;

            pushFollow(FOLLOW_rangeElement_in_range1293);
            rangeElement();

            state._fsp--;


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "range"


    // $ANTLR start "rangeElement"
    // ASTVerifier.g:313:1: rangeElement : ( STRING_LITERAL | RULE_REF | TOKEN_REF );
    public final void rangeElement() throws RecognitionException {
        try {
            // ASTVerifier.g:314:5: ( STRING_LITERAL | RULE_REF | TOKEN_REF )
            // ASTVerifier.g:
            {
            if ( (input.LA(1)>=TOKEN_REF && input.LA(1)<=RULE_REF)||input.LA(1)==STRING_LITERAL ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "rangeElement"


    // $ANTLR start "terminal"
    // ASTVerifier.g:319:1: terminal : ( ^( STRING_LITERAL elementOptions ) | STRING_LITERAL | ^( TOKEN_REF elementOptions ) | TOKEN_REF | ^( WILDCARD elementOptions ) | WILDCARD | ^( ROOT terminal ) | ^( BANG terminal ) );
    public final void terminal() throws RecognitionException {
        try {
            // ASTVerifier.g:320:5: ( ^( STRING_LITERAL elementOptions ) | STRING_LITERAL | ^( TOKEN_REF elementOptions ) | TOKEN_REF | ^( WILDCARD elementOptions ) | WILDCARD | ^( ROOT terminal ) | ^( BANG terminal ) )
            int alt42=8;
            alt42 = dfa42.predict(input);
            switch (alt42) {
                case 1 :
                    // ASTVerifier.g:320:8: ^( STRING_LITERAL elementOptions )
                    {
                    match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_terminal1346);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_elementOptions_in_terminal1348);
                    elementOptions();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;
                case 2 :
                    // ASTVerifier.g:321:7: STRING_LITERAL
                    {
                    match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_terminal1357);

                    }
                    break;
                case 3 :
                    // ASTVerifier.g:322:7: ^( TOKEN_REF elementOptions )
                    {
                    match(input,TOKEN_REF,FOLLOW_TOKEN_REF_in_terminal1366);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_elementOptions_in_terminal1368);
                    elementOptions();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;
                case 4 :
                    // ASTVerifier.g:323:7: TOKEN_REF
                    {
                    match(input,TOKEN_REF,FOLLOW_TOKEN_REF_in_terminal1377);

                    }
                    break;
                case 5 :
                    // ASTVerifier.g:324:7: ^( WILDCARD elementOptions )
                    {
                    match(input,WILDCARD,FOLLOW_WILDCARD_in_terminal1386);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_elementOptions_in_terminal1388);
                    elementOptions();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;
                case 6 :
                    // ASTVerifier.g:325:7: WILDCARD
                    {
                    match(input,WILDCARD,FOLLOW_WILDCARD_in_terminal1397);

                    }
                    break;
                case 7 :
                    // ASTVerifier.g:326:7: ^( ROOT terminal )
                    {
                    match(input,ROOT,FOLLOW_ROOT_in_terminal1406);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_terminal_in_terminal1408);
                    terminal();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;
                case 8 :
                    // ASTVerifier.g:327:7: ^( BANG terminal )
                    {
                    match(input,BANG,FOLLOW_BANG_in_terminal1418);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_terminal_in_terminal1420);
                    terminal();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "terminal"


    // $ANTLR start "elementOptions"
    // ASTVerifier.g:330:1: elementOptions : ^( ELEMENT_OPTIONS ( elementOption )+ ) ;
    public final void elementOptions() throws RecognitionException {
        try {
            // ASTVerifier.g:331:5: ( ^( ELEMENT_OPTIONS ( elementOption )+ ) )
            // ASTVerifier.g:331:7: ^( ELEMENT_OPTIONS ( elementOption )+ )
            {
            match(input,ELEMENT_OPTIONS,FOLLOW_ELEMENT_OPTIONS_in_elementOptions1439);

            match(input, Token.DOWN, null);
            // ASTVerifier.g:331:25: ( elementOption )+
            int cnt43=0;
            loop43:
            do {
                int alt43=2;
                int LA43_0 = input.LA(1);

                if ( (LA43_0==ASSIGN||LA43_0==ID) ) {
                    alt43=1;
                }


                switch (alt43) {
            	case 1 :
            	    // ASTVerifier.g:331:25: elementOption
            	    {
            	    pushFollow(FOLLOW_elementOption_in_elementOptions1441);
            	    elementOption();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt43 >= 1 ) break loop43;
                        EarlyExitException eee =
                            new EarlyExitException(43, input);
                        throw eee;
                }
                cnt43++;
            } while (true);


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "elementOptions"


    // $ANTLR start "elementOption"
    // ASTVerifier.g:334:1: elementOption : ( ID | ^( ASSIGN ID ID ) | ^( ASSIGN ID STRING_LITERAL ) );
    public final void elementOption() throws RecognitionException {
        try {
            // ASTVerifier.g:335:5: ( ID | ^( ASSIGN ID ID ) | ^( ASSIGN ID STRING_LITERAL ) )
            int alt44=3;
            int LA44_0 = input.LA(1);

            if ( (LA44_0==ID) ) {
                alt44=1;
            }
            else if ( (LA44_0==ASSIGN) ) {
                int LA44_2 = input.LA(2);

                if ( (LA44_2==DOWN) ) {
                    int LA44_3 = input.LA(3);

                    if ( (LA44_3==ID) ) {
                        int LA44_4 = input.LA(4);

                        if ( (LA44_4==ID) ) {
                            alt44=2;
                        }
                        else if ( (LA44_4==STRING_LITERAL) ) {
                            alt44=3;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 44, 4, input);

                            throw nvae;
                        }
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 44, 3, input);

                        throw nvae;
                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 44, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 44, 0, input);

                throw nvae;
            }
            switch (alt44) {
                case 1 :
                    // ASTVerifier.g:335:7: ID
                    {
                    match(input,ID,FOLLOW_ID_in_elementOption1460);

                    }
                    break;
                case 2 :
                    // ASTVerifier.g:336:9: ^( ASSIGN ID ID )
                    {
                    match(input,ASSIGN,FOLLOW_ASSIGN_in_elementOption1471);

                    match(input, Token.DOWN, null);
                    match(input,ID,FOLLOW_ID_in_elementOption1473);
                    match(input,ID,FOLLOW_ID_in_elementOption1475);

                    match(input, Token.UP, null);

                    }
                    break;
                case 3 :
                    // ASTVerifier.g:337:9: ^( ASSIGN ID STRING_LITERAL )
                    {
                    match(input,ASSIGN,FOLLOW_ASSIGN_in_elementOption1487);

                    match(input, Token.DOWN, null);
                    match(input,ID,FOLLOW_ID_in_elementOption1489);
                    match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_elementOption1491);

                    match(input, Token.UP, null);

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "elementOption"


    // $ANTLR start "rewrite"
    // ASTVerifier.g:340:1: rewrite : ( predicatedRewrite )* nakedRewrite ;
    public final void rewrite() throws RecognitionException {
        try {
            // ASTVerifier.g:341:2: ( ( predicatedRewrite )* nakedRewrite )
            // ASTVerifier.g:341:4: ( predicatedRewrite )* nakedRewrite
            {
            // ASTVerifier.g:341:4: ( predicatedRewrite )*
            loop45:
            do {
                int alt45=2;
                int LA45_0 = input.LA(1);

                if ( (LA45_0==ST_RESULT) ) {
                    int LA45_1 = input.LA(2);

                    if ( (LA45_1==DOWN) ) {
                        int LA45_3 = input.LA(3);

                        if ( (LA45_3==SEMPRED) ) {
                            alt45=1;
                        }


                    }


                }
                else if ( (LA45_0==RESULT) ) {
                    int LA45_2 = input.LA(2);

                    if ( (LA45_2==DOWN) ) {
                        int LA45_4 = input.LA(3);

                        if ( (LA45_4==SEMPRED) ) {
                            alt45=1;
                        }


                    }


                }


                switch (alt45) {
            	case 1 :
            	    // ASTVerifier.g:341:4: predicatedRewrite
            	    {
            	    pushFollow(FOLLOW_predicatedRewrite_in_rewrite1506);
            	    predicatedRewrite();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop45;
                }
            } while (true);

            pushFollow(FOLLOW_nakedRewrite_in_rewrite1509);
            nakedRewrite();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "rewrite"


    // $ANTLR start "predicatedRewrite"
    // ASTVerifier.g:344:1: predicatedRewrite : ( ^( ST_RESULT SEMPRED rewriteAlt ) | ^( RESULT SEMPRED rewriteAlt ) );
    public final void predicatedRewrite() throws RecognitionException {
        try {
            // ASTVerifier.g:345:2: ( ^( ST_RESULT SEMPRED rewriteAlt ) | ^( RESULT SEMPRED rewriteAlt ) )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==ST_RESULT) ) {
                alt46=1;
            }
            else if ( (LA46_0==RESULT) ) {
                alt46=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 46, 0, input);

                throw nvae;
            }
            switch (alt46) {
                case 1 :
                    // ASTVerifier.g:345:4: ^( ST_RESULT SEMPRED rewriteAlt )
                    {
                    match(input,ST_RESULT,FOLLOW_ST_RESULT_in_predicatedRewrite1521);

                    match(input, Token.DOWN, null);
                    match(input,SEMPRED,FOLLOW_SEMPRED_in_predicatedRewrite1523);
                    pushFollow(FOLLOW_rewriteAlt_in_predicatedRewrite1525);
                    rewriteAlt();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;
                case 2 :
                    // ASTVerifier.g:346:4: ^( RESULT SEMPRED rewriteAlt )
                    {
                    match(input,RESULT,FOLLOW_RESULT_in_predicatedRewrite1532);

                    match(input, Token.DOWN, null);
                    match(input,SEMPRED,FOLLOW_SEMPRED_in_predicatedRewrite1534);
                    pushFollow(FOLLOW_rewriteAlt_in_predicatedRewrite1536);
                    rewriteAlt();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "predicatedRewrite"


    // $ANTLR start "nakedRewrite"
    // ASTVerifier.g:349:1: nakedRewrite : ( ^( ST_RESULT rewriteAlt ) | ^( RESULT rewriteAlt ) );
    public final void nakedRewrite() throws RecognitionException {
        try {
            // ASTVerifier.g:350:2: ( ^( ST_RESULT rewriteAlt ) | ^( RESULT rewriteAlt ) )
            int alt47=2;
            int LA47_0 = input.LA(1);

            if ( (LA47_0==ST_RESULT) ) {
                alt47=1;
            }
            else if ( (LA47_0==RESULT) ) {
                alt47=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                throw nvae;
            }
            switch (alt47) {
                case 1 :
                    // ASTVerifier.g:350:4: ^( ST_RESULT rewriteAlt )
                    {
                    match(input,ST_RESULT,FOLLOW_ST_RESULT_in_nakedRewrite1550);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_rewriteAlt_in_nakedRewrite1552);
                    rewriteAlt();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;
                case 2 :
                    // ASTVerifier.g:351:4: ^( RESULT rewriteAlt )
                    {
                    match(input,RESULT,FOLLOW_RESULT_in_nakedRewrite1559);

                    match(input, Token.DOWN, null);
                    pushFollow(FOLLOW_rewriteAlt_in_nakedRewrite1561);
                    rewriteAlt();

                    state._fsp--;


                    match(input, Token.UP, null);

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "nakedRewrite"


    // $ANTLR start "rewriteAlt"
    // ASTVerifier.g:354:1: rewriteAlt : ( rewriteTemplate | rewriteTreeAlt | ETC | EPSILON );
    public final void rewriteAlt() throws RecognitionException {
        try {
            // ASTVerifier.g:355:5: ( rewriteTemplate | rewriteTreeAlt | ETC | EPSILON )
            int alt48=4;
            switch ( input.LA(1) ) {
            case ACTION:
            case TEMPLATE:
                {
                alt48=1;
                }
                break;
            case ALT:
                {
                alt48=2;
                }
                break;
            case ETC:
                {
                alt48=3;
                }
                break;
            case EPSILON:
                {
                alt48=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 48, 0, input);

                throw nvae;
            }

            switch (alt48) {
                case 1 :
                    // ASTVerifier.g:355:7: rewriteTemplate
                    {
                    pushFollow(FOLLOW_rewriteTemplate_in_rewriteAlt1577);
                    rewriteTemplate();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // ASTVerifier.g:356:7: rewriteTreeAlt
                    {
                    pushFollow(FOLLOW_rewriteTreeAlt_in_rewriteAlt1585);
                    rewriteTreeAlt();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // ASTVerifier.g:357:7: ETC
                    {
                    match(input,ETC,FOLLOW_ETC_in_rewriteAlt1593);

                    }
                    break;
                case 4 :
                    // ASTVerifier.g:358:7: EPSILON
                    {
                    match(input,EPSILON,FOLLOW_EPSILON_in_rewriteAlt1601);

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "rewriteAlt"


    // $ANTLR start "rewriteTreeAlt"
    // ASTVerifier.g:361:1: rewriteTreeAlt : ^( ALT ( rewriteTreeElement )+ ) ;
    public final void rewriteTreeAlt() throws RecognitionException {
        try {
            // ASTVerifier.g:362:5: ( ^( ALT ( rewriteTreeElement )+ ) )
            // ASTVerifier.g:362:7: ^( ALT ( rewriteTreeElement )+ )
            {
            match(input,ALT,FOLLOW_ALT_in_rewriteTreeAlt1620);

            match(input, Token.DOWN, null);
            // ASTVerifier.g:362:13: ( rewriteTreeElement )+
            int cnt49=0;
            loop49:
            do {
                int alt49=2;
                int LA49_0 = input.LA(1);

                if ( (LA49_0==ACTION||LA49_0==TREE_BEGIN||(LA49_0>=TOKEN_REF && LA49_0<=RULE_REF)||LA49_0==STRING_LITERAL||(LA49_0>=OPTIONAL && LA49_0<=POSITIVE_CLOSURE)||LA49_0==LABEL) ) {
                    alt49=1;
                }


                switch (alt49) {
            	case 1 :
            	    // ASTVerifier.g:362:13: rewriteTreeElement
            	    {
            	    pushFollow(FOLLOW_rewriteTreeElement_in_rewriteTreeAlt1622);
            	    rewriteTreeElement();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt49 >= 1 ) break loop49;
                        EarlyExitException eee =
                            new EarlyExitException(49, input);
                        throw eee;
                }
                cnt49++;
            } while (true);


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "rewriteTreeAlt"


    // $ANTLR start "rewriteTreeElement"
    // ASTVerifier.g:365:1: rewriteTreeElement : ( rewriteTreeAtom | rewriteTree | rewriteTreeEbnf );
    public final void rewriteTreeElement() throws RecognitionException {
        try {
            // ASTVerifier.g:366:2: ( rewriteTreeAtom | rewriteTree | rewriteTreeEbnf )
            int alt50=3;
            switch ( input.LA(1) ) {
            case ACTION:
            case TOKEN_REF:
            case RULE_REF:
            case STRING_LITERAL:
            case LABEL:
                {
                alt50=1;
                }
                break;
            case TREE_BEGIN:
                {
                alt50=2;
                }
                break;
            case OPTIONAL:
            case CLOSURE:
            case POSITIVE_CLOSURE:
                {
                alt50=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 50, 0, input);

                throw nvae;
            }

            switch (alt50) {
                case 1 :
                    // ASTVerifier.g:366:4: rewriteTreeAtom
                    {
                    pushFollow(FOLLOW_rewriteTreeAtom_in_rewriteTreeElement1638);
                    rewriteTreeAtom();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // ASTVerifier.g:367:4: rewriteTree
                    {
                    pushFollow(FOLLOW_rewriteTree_in_rewriteTreeElement1643);
                    rewriteTree();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // ASTVerifier.g:368:6: rewriteTreeEbnf
                    {
                    pushFollow(FOLLOW_rewriteTreeEbnf_in_rewriteTreeElement1650);
                    rewriteTreeEbnf();

                    state._fsp--;


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "rewriteTreeElement"


    // $ANTLR start "rewriteTreeAtom"
    // ASTVerifier.g:371:1: rewriteTreeAtom : ( ^( TOKEN_REF ARG_ACTION ) | TOKEN_REF | RULE_REF | STRING_LITERAL | LABEL | ACTION );
    public final void rewriteTreeAtom() throws RecognitionException {
        try {
            // ASTVerifier.g:372:5: ( ^( TOKEN_REF ARG_ACTION ) | TOKEN_REF | RULE_REF | STRING_LITERAL | LABEL | ACTION )
            int alt51=6;
            switch ( input.LA(1) ) {
            case TOKEN_REF:
                {
                int LA51_1 = input.LA(2);

                if ( (LA51_1==DOWN) ) {
                    alt51=1;
                }
                else if ( (LA51_1==UP||LA51_1==ACTION||LA51_1==TREE_BEGIN||(LA51_1>=TOKEN_REF && LA51_1<=RULE_REF)||LA51_1==STRING_LITERAL||(LA51_1>=OPTIONAL && LA51_1<=POSITIVE_CLOSURE)||LA51_1==LABEL) ) {
                    alt51=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 51, 1, input);

                    throw nvae;
                }
                }
                break;
            case RULE_REF:
                {
                alt51=3;
                }
                break;
            case STRING_LITERAL:
                {
                alt51=4;
                }
                break;
            case LABEL:
                {
                alt51=5;
                }
                break;
            case ACTION:
                {
                alt51=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 51, 0, input);

                throw nvae;
            }

            switch (alt51) {
                case 1 :
                    // ASTVerifier.g:372:9: ^( TOKEN_REF ARG_ACTION )
                    {
                    match(input,TOKEN_REF,FOLLOW_TOKEN_REF_in_rewriteTreeAtom1667);

                    match(input, Token.DOWN, null);
                    match(input,ARG_ACTION,FOLLOW_ARG_ACTION_in_rewriteTreeAtom1669);

                    match(input, Token.UP, null);

                    }
                    break;
                case 2 :
                    // ASTVerifier.g:373:6: TOKEN_REF
                    {
                    match(input,TOKEN_REF,FOLLOW_TOKEN_REF_in_rewriteTreeAtom1677);

                    }
                    break;
                case 3 :
                    // ASTVerifier.g:374:9: RULE_REF
                    {
                    match(input,RULE_REF,FOLLOW_RULE_REF_in_rewriteTreeAtom1687);

                    }
                    break;
                case 4 :
                    // ASTVerifier.g:375:6: STRING_LITERAL
                    {
                    match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_rewriteTreeAtom1694);

                    }
                    break;
                case 5 :
                    // ASTVerifier.g:376:6: LABEL
                    {
                    match(input,LABEL,FOLLOW_LABEL_in_rewriteTreeAtom1701);

                    }
                    break;
                case 6 :
                    // ASTVerifier.g:377:4: ACTION
                    {
                    match(input,ACTION,FOLLOW_ACTION_in_rewriteTreeAtom1706);

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "rewriteTreeAtom"


    // $ANTLR start "rewriteTreeEbnf"
    // ASTVerifier.g:380:1: rewriteTreeEbnf : ^( ebnfSuffix ^( BLOCK rewriteTreeAlt ) ) ;
    public final void rewriteTreeEbnf() throws RecognitionException {
        try {
            // ASTVerifier.g:381:2: ( ^( ebnfSuffix ^( BLOCK rewriteTreeAlt ) ) )
            // ASTVerifier.g:381:4: ^( ebnfSuffix ^( BLOCK rewriteTreeAlt ) )
            {
            pushFollow(FOLLOW_ebnfSuffix_in_rewriteTreeEbnf1718);
            ebnfSuffix();

            state._fsp--;


            match(input, Token.DOWN, null);
            match(input,BLOCK,FOLLOW_BLOCK_in_rewriteTreeEbnf1721);

            match(input, Token.DOWN, null);
            pushFollow(FOLLOW_rewriteTreeAlt_in_rewriteTreeEbnf1723);
            rewriteTreeAlt();

            state._fsp--;


            match(input, Token.UP, null);

            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "rewriteTreeEbnf"


    // $ANTLR start "rewriteTree"
    // ASTVerifier.g:383:1: rewriteTree : ^( TREE_BEGIN rewriteTreeAtom ( rewriteTreeElement )* ) ;
    public final void rewriteTree() throws RecognitionException {
        try {
            // ASTVerifier.g:384:2: ( ^( TREE_BEGIN rewriteTreeAtom ( rewriteTreeElement )* ) )
            // ASTVerifier.g:384:4: ^( TREE_BEGIN rewriteTreeAtom ( rewriteTreeElement )* )
            {
            match(input,TREE_BEGIN,FOLLOW_TREE_BEGIN_in_rewriteTree1736);

            match(input, Token.DOWN, null);
            pushFollow(FOLLOW_rewriteTreeAtom_in_rewriteTree1738);
            rewriteTreeAtom();

            state._fsp--;

            // ASTVerifier.g:384:33: ( rewriteTreeElement )*
            loop52:
            do {
                int alt52=2;
                int LA52_0 = input.LA(1);

                if ( (LA52_0==ACTION||LA52_0==TREE_BEGIN||(LA52_0>=TOKEN_REF && LA52_0<=RULE_REF)||LA52_0==STRING_LITERAL||(LA52_0>=OPTIONAL && LA52_0<=POSITIVE_CLOSURE)||LA52_0==LABEL) ) {
                    alt52=1;
                }


                switch (alt52) {
            	case 1 :
            	    // ASTVerifier.g:384:33: rewriteTreeElement
            	    {
            	    pushFollow(FOLLOW_rewriteTreeElement_in_rewriteTree1740);
            	    rewriteTreeElement();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop52;
                }
            } while (true);


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "rewriteTree"


    // $ANTLR start "rewriteTemplate"
    // ASTVerifier.g:387:1: rewriteTemplate : ( ^( TEMPLATE ( rewriteTemplateArgs )? DOUBLE_QUOTE_STRING_LITERAL ) | ^( TEMPLATE ( rewriteTemplateArgs )? DOUBLE_ANGLE_STRING_LITERAL ) | rewriteTemplateRef | rewriteIndirectTemplateHead | ACTION );
    public final void rewriteTemplate() throws RecognitionException {
        try {
            // ASTVerifier.g:388:2: ( ^( TEMPLATE ( rewriteTemplateArgs )? DOUBLE_QUOTE_STRING_LITERAL ) | ^( TEMPLATE ( rewriteTemplateArgs )? DOUBLE_ANGLE_STRING_LITERAL ) | rewriteTemplateRef | rewriteIndirectTemplateHead | ACTION )
            int alt55=5;
            alt55 = dfa55.predict(input);
            switch (alt55) {
                case 1 :
                    // ASTVerifier.g:388:4: ^( TEMPLATE ( rewriteTemplateArgs )? DOUBLE_QUOTE_STRING_LITERAL )
                    {
                    match(input,TEMPLATE,FOLLOW_TEMPLATE_in_rewriteTemplate1755);

                    match(input, Token.DOWN, null);
                    // ASTVerifier.g:388:15: ( rewriteTemplateArgs )?
                    int alt53=2;
                    int LA53_0 = input.LA(1);

                    if ( (LA53_0==ARGLIST) ) {
                        alt53=1;
                    }
                    switch (alt53) {
                        case 1 :
                            // ASTVerifier.g:388:15: rewriteTemplateArgs
                            {
                            pushFollow(FOLLOW_rewriteTemplateArgs_in_rewriteTemplate1757);
                            rewriteTemplateArgs();

                            state._fsp--;


                            }
                            break;

                    }

                    match(input,DOUBLE_QUOTE_STRING_LITERAL,FOLLOW_DOUBLE_QUOTE_STRING_LITERAL_in_rewriteTemplate1760);

                    match(input, Token.UP, null);

                    }
                    break;
                case 2 :
                    // ASTVerifier.g:389:4: ^( TEMPLATE ( rewriteTemplateArgs )? DOUBLE_ANGLE_STRING_LITERAL )
                    {
                    match(input,TEMPLATE,FOLLOW_TEMPLATE_in_rewriteTemplate1767);

                    match(input, Token.DOWN, null);
                    // ASTVerifier.g:389:15: ( rewriteTemplateArgs )?
                    int alt54=2;
                    int LA54_0 = input.LA(1);

                    if ( (LA54_0==ARGLIST) ) {
                        alt54=1;
                    }
                    switch (alt54) {
                        case 1 :
                            // ASTVerifier.g:389:15: rewriteTemplateArgs
                            {
                            pushFollow(FOLLOW_rewriteTemplateArgs_in_rewriteTemplate1769);
                            rewriteTemplateArgs();

                            state._fsp--;


                            }
                            break;

                    }

                    match(input,DOUBLE_ANGLE_STRING_LITERAL,FOLLOW_DOUBLE_ANGLE_STRING_LITERAL_in_rewriteTemplate1772);

                    match(input, Token.UP, null);

                    }
                    break;
                case 3 :
                    // ASTVerifier.g:390:4: rewriteTemplateRef
                    {
                    pushFollow(FOLLOW_rewriteTemplateRef_in_rewriteTemplate1778);
                    rewriteTemplateRef();

                    state._fsp--;


                    }
                    break;
                case 4 :
                    // ASTVerifier.g:391:4: rewriteIndirectTemplateHead
                    {
                    pushFollow(FOLLOW_rewriteIndirectTemplateHead_in_rewriteTemplate1783);
                    rewriteIndirectTemplateHead();

                    state._fsp--;


                    }
                    break;
                case 5 :
                    // ASTVerifier.g:392:4: ACTION
                    {
                    match(input,ACTION,FOLLOW_ACTION_in_rewriteTemplate1788);

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "rewriteTemplate"


    // $ANTLR start "rewriteTemplateRef"
    // ASTVerifier.g:395:1: rewriteTemplateRef : ^( TEMPLATE ID ( rewriteTemplateArgs )? ) ;
    public final void rewriteTemplateRef() throws RecognitionException {
        try {
            // ASTVerifier.g:396:2: ( ^( TEMPLATE ID ( rewriteTemplateArgs )? ) )
            // ASTVerifier.g:396:4: ^( TEMPLATE ID ( rewriteTemplateArgs )? )
            {
            match(input,TEMPLATE,FOLLOW_TEMPLATE_in_rewriteTemplateRef1800);

            match(input, Token.DOWN, null);
            match(input,ID,FOLLOW_ID_in_rewriteTemplateRef1802);
            // ASTVerifier.g:396:18: ( rewriteTemplateArgs )?
            int alt56=2;
            int LA56_0 = input.LA(1);

            if ( (LA56_0==ARGLIST) ) {
                alt56=1;
            }
            switch (alt56) {
                case 1 :
                    // ASTVerifier.g:396:18: rewriteTemplateArgs
                    {
                    pushFollow(FOLLOW_rewriteTemplateArgs_in_rewriteTemplateRef1804);
                    rewriteTemplateArgs();

                    state._fsp--;


                    }
                    break;

            }


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "rewriteTemplateRef"


    // $ANTLR start "rewriteIndirectTemplateHead"
    // ASTVerifier.g:399:1: rewriteIndirectTemplateHead : ^( TEMPLATE ACTION ( rewriteTemplateArgs )? ) ;
    public final void rewriteIndirectTemplateHead() throws RecognitionException {
        try {
            // ASTVerifier.g:400:2: ( ^( TEMPLATE ACTION ( rewriteTemplateArgs )? ) )
            // ASTVerifier.g:400:4: ^( TEMPLATE ACTION ( rewriteTemplateArgs )? )
            {
            match(input,TEMPLATE,FOLLOW_TEMPLATE_in_rewriteIndirectTemplateHead1818);

            match(input, Token.DOWN, null);
            match(input,ACTION,FOLLOW_ACTION_in_rewriteIndirectTemplateHead1820);
            // ASTVerifier.g:400:22: ( rewriteTemplateArgs )?
            int alt57=2;
            int LA57_0 = input.LA(1);

            if ( (LA57_0==ARGLIST) ) {
                alt57=1;
            }
            switch (alt57) {
                case 1 :
                    // ASTVerifier.g:400:22: rewriteTemplateArgs
                    {
                    pushFollow(FOLLOW_rewriteTemplateArgs_in_rewriteIndirectTemplateHead1822);
                    rewriteTemplateArgs();

                    state._fsp--;


                    }
                    break;

            }


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "rewriteIndirectTemplateHead"


    // $ANTLR start "rewriteTemplateArgs"
    // ASTVerifier.g:403:1: rewriteTemplateArgs : ^( ARGLIST ( rewriteTemplateArg )+ ) ;
    public final void rewriteTemplateArgs() throws RecognitionException {
        try {
            // ASTVerifier.g:404:2: ( ^( ARGLIST ( rewriteTemplateArg )+ ) )
            // ASTVerifier.g:404:4: ^( ARGLIST ( rewriteTemplateArg )+ )
            {
            match(input,ARGLIST,FOLLOW_ARGLIST_in_rewriteTemplateArgs1836);

            match(input, Token.DOWN, null);
            // ASTVerifier.g:404:14: ( rewriteTemplateArg )+
            int cnt58=0;
            loop58:
            do {
                int alt58=2;
                int LA58_0 = input.LA(1);

                if ( (LA58_0==ARG) ) {
                    alt58=1;
                }


                switch (alt58) {
            	case 1 :
            	    // ASTVerifier.g:404:14: rewriteTemplateArg
            	    {
            	    pushFollow(FOLLOW_rewriteTemplateArg_in_rewriteTemplateArgs1838);
            	    rewriteTemplateArg();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt58 >= 1 ) break loop58;
                        EarlyExitException eee =
                            new EarlyExitException(58, input);
                        throw eee;
                }
                cnt58++;
            } while (true);


            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "rewriteTemplateArgs"


    // $ANTLR start "rewriteTemplateArg"
    // ASTVerifier.g:407:1: rewriteTemplateArg : ^( ARG ID ACTION ) ;
    public final void rewriteTemplateArg() throws RecognitionException {
        try {
            // ASTVerifier.g:408:2: ( ^( ARG ID ACTION ) )
            // ASTVerifier.g:408:6: ^( ARG ID ACTION )
            {
            match(input,ARG,FOLLOW_ARG_in_rewriteTemplateArg1854);

            match(input, Token.DOWN, null);
            match(input,ID,FOLLOW_ID_in_rewriteTemplateArg1856);
            match(input,ACTION,FOLLOW_ACTION_in_rewriteTemplateArg1858);

            match(input, Token.UP, null);

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "rewriteTemplateArg"

    // Delegated rules


    protected DFA26 dfa26 = new DFA26(this);
    protected DFA33 dfa33 = new DFA33(this);
    protected DFA42 dfa42 = new DFA42(this);
    protected DFA55 dfa55 = new DFA55(this);
    static final String DFA26_eotS =
        "\14\uffff";
    static final String DFA26_eofS =
        "\14\uffff";
    static final String DFA26_minS =
        "\1\4\1\uffff\2\2\6\uffff\2\57";
    static final String DFA26_maxS =
        "\1\143\1\uffff\2\2\6\uffff\2\114";
    static final String DFA26_acceptS =
        "\1\uffff\1\1\2\uffff\1\2\1\3\1\4\1\5\1\6\1\7\2\uffff";
    static final String DFA26_specialS =
        "\14\uffff}>";
    static final String[] DFA26_transitionS = {
            "\1\7\13\uffff\1\6\31\uffff\1\5\2\uffff\1\1\1\uffff\1\3\2\uffff"+
            "\1\1\1\uffff\1\2\1\uffff\2\4\2\uffff\1\11\3\uffff\2\4\3\uffff"+
            "\1\4\10\uffff\4\5\20\uffff\1\10\2\uffff\1\4",
            "",
            "\1\12",
            "\1\13",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\4\4\uffff\1\4\1\uffff\2\4\4\uffff\1\4\1\uffff\2\4\3\uffff"+
            "\1\4\10\uffff\1\5",
            "\1\4\4\uffff\1\4\1\uffff\2\4\4\uffff\1\4\1\uffff\2\4\3\uffff"+
            "\1\4\10\uffff\1\5"
    };

    static final short[] DFA26_eot = DFA.unpackEncodedString(DFA26_eotS);
    static final short[] DFA26_eof = DFA.unpackEncodedString(DFA26_eofS);
    static final char[] DFA26_min = DFA.unpackEncodedStringToUnsignedChars(DFA26_minS);
    static final char[] DFA26_max = DFA.unpackEncodedStringToUnsignedChars(DFA26_maxS);
    static final short[] DFA26_accept = DFA.unpackEncodedString(DFA26_acceptS);
    static final short[] DFA26_special = DFA.unpackEncodedString(DFA26_specialS);
    static final short[][] DFA26_transition;

    static {
        int numStates = DFA26_transitionS.length;
        DFA26_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA26_transition[i] = DFA.unpackEncodedString(DFA26_transitionS[i]);
        }
    }

    class DFA26 extends DFA {

        public DFA26(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 26;
            this.eot = DFA26_eot;
            this.eof = DFA26_eof;
            this.min = DFA26_min;
            this.max = DFA26_max;
            this.accept = DFA26_accept;
            this.special = DFA26_special;
            this.transition = DFA26_transition;
        }
        public String getDescription() {
            return "242:1: element : ( labeledElement | atom | ebnf | ACTION | SEMPRED | GATED_SEMPRED | treeSpec );";
        }
    }
    static final String DFA33_eotS =
        "\25\uffff";
    static final String DFA33_eofS =
        "\25\uffff";
    static final String DFA33_minS =
        "\1\57\2\2\1\uffff\1\2\2\uffff\2\57\1\126\4\uffff\1\57\2\2\2\uffff"+
        "\2\57";
    static final String DFA33_maxS =
        "\1\143\2\2\1\uffff\1\2\2\uffff\2\103\1\126\4\uffff\1\103\2\2\2\uffff"+
        "\2\103";
    static final String DFA33_acceptS =
        "\3\uffff\1\5\1\uffff\1\10\1\11\3\uffff\1\3\1\1\1\2\1\4\3\uffff\1"+
        "\7\1\6\2\uffff";
    static final String DFA33_specialS =
        "\25\uffff}>";
    static final String[] DFA33_transitionS = {
            "\1\2\4\uffff\1\1\1\uffff\1\5\1\3\6\uffff\1\5\1\6\3\uffff\1\5"+
            "\37\uffff\1\4",
            "\1\7",
            "\1\10",
            "",
            "\1\11",
            "",
            "",
            "\1\5\4\uffff\1\5\1\uffff\1\5\1\13\4\uffff\1\12\1\uffff\1\5"+
            "\1\6\3\uffff\1\5",
            "\1\5\4\uffff\1\5\1\uffff\1\5\1\14\4\uffff\1\15\1\uffff\1\5"+
            "\1\6\3\uffff\1\5",
            "\1\16",
            "",
            "",
            "",
            "",
            "\1\20\4\uffff\1\17\1\uffff\1\22\7\uffff\1\22\1\21\3\uffff\1"+
            "\22",
            "\1\23",
            "\1\24",
            "",
            "",
            "\1\22\4\uffff\1\22\1\uffff\1\22\7\uffff\1\22\1\21\3\uffff\1"+
            "\22",
            "\1\22\4\uffff\1\22\1\uffff\1\22\7\uffff\1\22\1\21\3\uffff\1"+
            "\22"
    };

    static final short[] DFA33_eot = DFA.unpackEncodedString(DFA33_eotS);
    static final short[] DFA33_eof = DFA.unpackEncodedString(DFA33_eofS);
    static final char[] DFA33_min = DFA.unpackEncodedStringToUnsignedChars(DFA33_minS);
    static final char[] DFA33_max = DFA.unpackEncodedStringToUnsignedChars(DFA33_maxS);
    static final short[] DFA33_accept = DFA.unpackEncodedString(DFA33_acceptS);
    static final short[] DFA33_special = DFA.unpackEncodedString(DFA33_specialS);
    static final short[][] DFA33_transition;

    static {
        int numStates = DFA33_transitionS.length;
        DFA33_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA33_transition[i] = DFA.unpackEncodedString(DFA33_transitionS[i]);
        }
    }

    class DFA33 extends DFA {

        public DFA33(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 33;
            this.eot = DFA33_eot;
            this.eof = DFA33_eof;
            this.min = DFA33_min;
            this.max = DFA33_max;
            this.accept = DFA33_accept;
            this.special = DFA33_special;
            this.transition = DFA33_transition;
        }
        public String getDescription() {
            return "278:1: atom : ( ^( ROOT range ) | ^( BANG range ) | ^( ROOT notSet ) | ^( BANG notSet ) | range | ^( DOT ID terminal ) | ^( DOT ID ruleref ) | terminal | ruleref );";
        }
    }
    static final String DFA42_eotS =
        "\14\uffff";
    static final String DFA42_eofS =
        "\14\uffff";
    static final String DFA42_minS =
        "\1\57\3\2\10\uffff";
    static final String DFA42_maxS =
        "\1\103\3\143\10\uffff";
    static final String DFA42_acceptS =
        "\4\uffff\1\7\1\10\1\1\1\2\1\3\1\4\1\5\1\6";
    static final String DFA42_specialS =
        "\14\uffff}>";
    static final String[] DFA42_transitionS = {
            "\1\5\4\uffff\1\4\1\uffff\1\3\7\uffff\1\2\4\uffff\1\1",
            "\1\6\2\7\13\uffff\1\7\31\uffff\1\7\2\uffff\1\7\1\uffff\1\7"+
            "\2\uffff\1\7\1\uffff\1\7\1\uffff\2\7\2\uffff\1\7\3\uffff\2\7"+
            "\3\uffff\1\7\10\uffff\4\7\20\uffff\1\7\2\uffff\1\7",
            "\1\10\2\11\13\uffff\1\11\31\uffff\1\11\2\uffff\1\11\1\uffff"+
            "\1\11\2\uffff\1\11\1\uffff\1\11\1\uffff\2\11\2\uffff\1\11\3"+
            "\uffff\2\11\3\uffff\1\11\10\uffff\4\11\20\uffff\1\11\2\uffff"+
            "\1\11",
            "\1\12\2\13\13\uffff\1\13\31\uffff\1\13\2\uffff\1\13\1\uffff"+
            "\1\13\2\uffff\1\13\1\uffff\1\13\1\uffff\2\13\2\uffff\1\13\3"+
            "\uffff\2\13\3\uffff\1\13\10\uffff\4\13\20\uffff\1\13\2\uffff"+
            "\1\13",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA42_eot = DFA.unpackEncodedString(DFA42_eotS);
    static final short[] DFA42_eof = DFA.unpackEncodedString(DFA42_eofS);
    static final char[] DFA42_min = DFA.unpackEncodedStringToUnsignedChars(DFA42_minS);
    static final char[] DFA42_max = DFA.unpackEncodedStringToUnsignedChars(DFA42_maxS);
    static final short[] DFA42_accept = DFA.unpackEncodedString(DFA42_acceptS);
    static final short[] DFA42_special = DFA.unpackEncodedString(DFA42_specialS);
    static final short[][] DFA42_transition;

    static {
        int numStates = DFA42_transitionS.length;
        DFA42_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA42_transition[i] = DFA.unpackEncodedString(DFA42_transitionS[i]);
        }
    }

    class DFA42 extends DFA {

        public DFA42(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 42;
            this.eot = DFA42_eot;
            this.eof = DFA42_eof;
            this.min = DFA42_min;
            this.max = DFA42_max;
            this.accept = DFA42_accept;
            this.special = DFA42_special;
            this.transition = DFA42_transition;
        }
        public String getDescription() {
            return "319:1: terminal : ( ^( STRING_LITERAL elementOptions ) | STRING_LITERAL | ^( TOKEN_REF elementOptions ) | TOKEN_REF | ^( WILDCARD elementOptions ) | WILDCARD | ^( ROOT terminal ) | ^( BANG terminal ) );";
        }
    }
    static final String DFA55_eotS =
        "\20\uffff";
    static final String DFA55_eofS =
        "\20\uffff";
    static final String DFA55_minS =
        "\1\20\1\2\1\uffff\1\12\2\uffff\1\2\2\uffff\1\127\1\2\1\126\1\20"+
        "\2\3\1\12";
    static final String DFA55_maxS =
        "\1\43\1\2\1\uffff\1\130\2\uffff\1\2\2\uffff\1\127\1\2\1\126\1\20"+
        "\1\3\1\127\1\13";
    static final String DFA55_acceptS =
        "\2\uffff\1\5\1\uffff\1\3\1\4\1\uffff\1\1\1\2\7\uffff";
    static final String DFA55_specialS =
        "\20\uffff}>";
    static final String[] DFA55_transitionS = {
            "\1\2\22\uffff\1\1",
            "\1\3",
            "",
            "\1\7\1\10\4\uffff\1\5\105\uffff\1\4\1\uffff\1\6",
            "",
            "",
            "\1\11",
            "",
            "",
            "\1\12",
            "\1\13",
            "\1\14",
            "\1\15",
            "\1\16",
            "\1\17\123\uffff\1\12",
            "\1\7\1\10"
    };

    static final short[] DFA55_eot = DFA.unpackEncodedString(DFA55_eotS);
    static final short[] DFA55_eof = DFA.unpackEncodedString(DFA55_eofS);
    static final char[] DFA55_min = DFA.unpackEncodedStringToUnsignedChars(DFA55_minS);
    static final char[] DFA55_max = DFA.unpackEncodedStringToUnsignedChars(DFA55_maxS);
    static final short[] DFA55_accept = DFA.unpackEncodedString(DFA55_acceptS);
    static final short[] DFA55_special = DFA.unpackEncodedString(DFA55_specialS);
    static final short[][] DFA55_transition;

    static {
        int numStates = DFA55_transitionS.length;
        DFA55_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA55_transition[i] = DFA.unpackEncodedString(DFA55_transitionS[i]);
        }
    }

    class DFA55 extends DFA {

        public DFA55(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 55;
            this.eot = DFA55_eot;
            this.eof = DFA55_eof;
            this.min = DFA55_min;
            this.max = DFA55_max;
            this.accept = DFA55_accept;
            this.special = DFA55_special;
            this.transition = DFA55_transition;
        }
        public String getDescription() {
            return "387:1: rewriteTemplate : ( ^( TEMPLATE ( rewriteTemplateArgs )? DOUBLE_QUOTE_STRING_LITERAL ) | ^( TEMPLATE ( rewriteTemplateArgs )? DOUBLE_ANGLE_STRING_LITERAL ) | rewriteTemplateRef | rewriteIndirectTemplateHead | ACTION );";
        }
    }


    public static final BitSet FOLLOW_grammarType_in_grammarSpec74 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_grammarSpec76 = new BitSet(new long[]{0x0800000000780040L,0x0000000000000200L});
    public static final BitSet FOLLOW_DOC_COMMENT_in_grammarSpec78 = new BitSet(new long[]{0x0800000000780040L,0x0000000000000200L});
    public static final BitSet FOLLOW_prequelConstruct_in_grammarSpec81 = new BitSet(new long[]{0x0800000000780040L,0x0000000000000200L});
    public static final BitSet FOLLOW_rules_in_grammarSpec84 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_set_in_grammarType0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_optionsSpec_in_prequelConstruct131 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_delegateGrammars_in_prequelConstruct141 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tokensSpec_in_prequelConstruct151 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attrScope_in_prequelConstruct161 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_action_in_prequelConstruct171 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OPTIONS_in_optionsSpec186 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_option_in_optionsSpec188 = new BitSet(new long[]{0x0000200000000008L});
    public static final BitSet FOLLOW_ASSIGN_in_option210 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_option212 = new BitSet(new long[]{0x0001000000000000L,0x0000000000400009L});
    public static final BitSet FOLLOW_optionValue_in_option214 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_set_in_optionValue0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORT_in_delegateGrammars299 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_delegateGrammar_in_delegateGrammars301 = new BitSet(new long[]{0x0000200000000008L,0x0000000000400000L});
    public static final BitSet FOLLOW_ASSIGN_in_delegateGrammar320 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_delegateGrammar322 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_ID_in_delegateGrammar324 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ID_in_delegateGrammar335 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TOKENS_in_tokensSpec352 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_tokenSpec_in_tokensSpec354 = new BitSet(new long[]{0xC000200000000008L});
    public static final BitSet FOLLOW_ASSIGN_in_tokenSpec368 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_TOKEN_REF_in_tokenSpec370 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_tokenSpec372 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_TOKEN_REF_in_tokenSpec380 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_REF_in_tokenSpec385 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SCOPE_in_attrScope397 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_attrScope399 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_ACTION_in_attrScope401 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_AT_in_action414 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_action416 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_ID_in_action419 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_ACTION_in_action421 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_RULES_in_rules437 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_rule_in_rules439 = new BitSet(new long[]{0x0000000000000008L,0x0000000000000100L});
    public static final BitSet FOLLOW_RULE_in_rule457 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_rule459 = new BitSet(new long[]{0x0800000180284040L,0x0000000000001400L});
    public static final BitSet FOLLOW_DOC_COMMENT_in_rule461 = new BitSet(new long[]{0x0800000180284040L,0x0000000000001400L});
    public static final BitSet FOLLOW_ruleModifiers_in_rule464 = new BitSet(new long[]{0x0800000180284040L,0x0000000000001400L});
    public static final BitSet FOLLOW_ARG_ACTION_in_rule467 = new BitSet(new long[]{0x0800000180284040L,0x0000000000001400L});
    public static final BitSet FOLLOW_ruleReturns_in_rule480 = new BitSet(new long[]{0x0800000180284040L,0x0000000000001400L});
    public static final BitSet FOLLOW_rulePrequel_in_rule483 = new BitSet(new long[]{0x0800000180284040L,0x0000000000001400L});
    public static final BitSet FOLLOW_altListAsBlock_in_rule486 = new BitSet(new long[]{0x0000000600000008L});
    public static final BitSet FOLLOW_exceptionGroup_in_rule488 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_exceptionHandler_in_exceptionGroup515 = new BitSet(new long[]{0x0000000600000002L});
    public static final BitSet FOLLOW_finallyClause_in_exceptionGroup518 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CATCH_in_exceptionHandler534 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ARG_ACTION_in_exceptionHandler536 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_ACTION_in_exceptionHandler538 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_FINALLY_in_finallyClause551 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ACTION_in_finallyClause553 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_throwsSpec_in_rulePrequel570 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleScopeSpec_in_rulePrequel580 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_optionsSpec_in_rulePrequel590 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleAction_in_rulePrequel600 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURNS_in_ruleReturns615 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ARG_ACTION_in_ruleReturns617 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_THROWS_in_throwsSpec632 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_throwsSpec634 = new BitSet(new long[]{0x0000000000000008L,0x0000000000400000L});
    public static final BitSet FOLLOW_SCOPE_in_ruleScopeSpec651 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ACTION_in_ruleScopeSpec653 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_SCOPE_in_ruleScopeSpec660 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_ruleScopeSpec662 = new BitSet(new long[]{0x0000000000000008L,0x0000000000400000L});
    public static final BitSet FOLLOW_AT_in_ruleAction676 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_ruleAction678 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_ACTION_in_ruleAction680 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_RULEMODIFIERS_in_ruleModifiers696 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ruleModifier_in_ruleModifiers698 = new BitSet(new long[]{0x0000000070800008L});
    public static final BitSet FOLLOW_set_in_ruleModifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_alternative_in_altList758 = new BitSet(new long[]{0x0000000000000002L,0x0000008000080000L});
    public static final BitSet FOLLOW_BLOCK_in_altListAsBlock777 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_altList_in_altListAsBlock779 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ALT_REWRITE_in_alternative798 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_alternative_in_alternative800 = new BitSet(new long[]{0x0000000000000000L,0x0000004000200000L});
    public static final BitSet FOLLOW_rewrite_in_alternative802 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ALT_in_alternative812 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_EPSILON_in_alternative814 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_elements_in_alternative825 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALT_in_elements843 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_element_in_elements845 = new BitSet(new long[]{0xC4D4A40000010018L,0x000000090000F008L});
    public static final BitSet FOLLOW_labeledElement_in_element861 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atom_in_element866 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ebnf_in_element871 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ACTION_in_element878 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SEMPRED_in_element885 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GATED_SEMPRED_in_element890 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_treeSpec_in_element895 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASSIGN_in_labeledElement908 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_labeledElement910 = new BitSet(new long[]{0xC0D0840000000000L,0x000000080000F008L});
    public static final BitSet FOLLOW_atom_in_labeledElement913 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_block_in_labeledElement915 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_PLUS_ASSIGN_in_labeledElement923 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_labeledElement925 = new BitSet(new long[]{0xC0D0840000000000L,0x000000080000F008L});
    public static final BitSet FOLLOW_atom_in_labeledElement928 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_block_in_labeledElement930 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_TREE_BEGIN_in_treeSpec947 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_element_in_treeSpec949 = new BitSet(new long[]{0xC4D4A40000010018L,0x000000090000F008L});
    public static final BitSet FOLLOW_blockSuffix_in_ebnf964 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_block_in_ebnf966 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_block_in_ebnf973 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ebnfSuffix_in_blockSuffix990 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ROOT_in_blockSuffix998 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPLIES_in_blockSuffix1006 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BANG_in_blockSuffix1014 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ebnfSuffix0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ROOT_in_atom1054 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_range_in_atom1056 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_BANG_in_atom1063 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_range_in_atom1065 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ROOT_in_atom1072 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_notSet_in_atom1074 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_BANG_in_atom1081 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_notSet_in_atom1083 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_range_in_atom1089 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_atom1095 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_atom1097 = new BitSet(new long[]{0x4050800000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_terminal_in_atom1099 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_DOT_in_atom1106 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_atom1108 = new BitSet(new long[]{0xC0D0800000000000L,0x0000000800000008L});
    public static final BitSet FOLLOW_ruleref_in_atom1110 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_terminal_in_atom1121 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ruleref_in_atom1131 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_notSet1149 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_notTerminal_in_notSet1151 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_NOT_in_notSet1161 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_block_in_notSet1163 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_set_in_notTerminal0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BLOCK_in_block1207 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_optionsSpec_in_block1209 = new BitSet(new long[]{0x0800000100290000L,0x0000008000080000L});
    public static final BitSet FOLLOW_ruleAction_in_block1212 = new BitSet(new long[]{0x0800000100290000L,0x0000008000080000L});
    public static final BitSet FOLLOW_ACTION_in_block1215 = new BitSet(new long[]{0x0800000100290000L,0x0000008000080000L});
    public static final BitSet FOLLOW_altList_in_block1218 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ROOT_in_ruleref1237 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_RULE_REF_in_ruleref1239 = new BitSet(new long[]{0x0000000000004008L});
    public static final BitSet FOLLOW_ARG_ACTION_in_ruleref1241 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_BANG_in_ruleref1252 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_RULE_REF_in_ruleref1254 = new BitSet(new long[]{0x0000000000004008L});
    public static final BitSet FOLLOW_ARG_ACTION_in_ruleref1256 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_RULE_REF_in_ruleref1267 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ARG_ACTION_in_ruleref1269 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_RANGE_in_range1289 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_rangeElement_in_range1291 = new BitSet(new long[]{0xC000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_rangeElement_in_range1293 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_set_in_rangeElement0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_terminal1346 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_elementOptions_in_terminal1348 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_terminal1357 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TOKEN_REF_in_terminal1366 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_elementOptions_in_terminal1368 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_TOKEN_REF_in_terminal1377 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WILDCARD_in_terminal1386 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_elementOptions_in_terminal1388 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_WILDCARD_in_terminal1397 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ROOT_in_terminal1406 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_terminal_in_terminal1408 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_BANG_in_terminal1418 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_terminal_in_terminal1420 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ELEMENT_OPTIONS_in_elementOptions1439 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_elementOption_in_elementOptions1441 = new BitSet(new long[]{0x0000200000000008L,0x0000000000400000L});
    public static final BitSet FOLLOW_ID_in_elementOption1460 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASSIGN_in_elementOption1471 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_elementOption1473 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_ID_in_elementOption1475 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ASSIGN_in_elementOption1487 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_elementOption1489 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_elementOption1491 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_predicatedRewrite_in_rewrite1506 = new BitSet(new long[]{0x0000000000000000L,0x0000004000200000L});
    public static final BitSet FOLLOW_nakedRewrite_in_rewrite1509 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ST_RESULT_in_predicatedRewrite1521 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_SEMPRED_in_predicatedRewrite1523 = new BitSet(new long[]{0x0100000800010000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_rewriteAlt_in_predicatedRewrite1525 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_RESULT_in_predicatedRewrite1532 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_SEMPRED_in_predicatedRewrite1534 = new BitSet(new long[]{0x0100000800010000L,0x00000000000C0000L});
    public static final BitSet FOLLOW_rewriteAlt_in_predicatedRewrite1536 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ST_RESULT_in_nakedRewrite1550 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_rewriteAlt_in_nakedRewrite1552 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_RESULT_in_nakedRewrite1559 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_rewriteAlt_in_nakedRewrite1561 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_rewriteTemplate_in_rewriteAlt1577 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rewriteTreeAlt_in_rewriteAlt1585 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ETC_in_rewriteAlt1593 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EPSILON_in_rewriteAlt1601 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALT_in_rewriteTreeAlt1620 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_rewriteTreeElement_in_rewriteTreeAlt1622 = new BitSet(new long[]{0xC400000000010008L,0x000000008000E008L});
    public static final BitSet FOLLOW_rewriteTreeAtom_in_rewriteTreeElement1638 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rewriteTree_in_rewriteTreeElement1643 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rewriteTreeEbnf_in_rewriteTreeElement1650 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TOKEN_REF_in_rewriteTreeAtom1667 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ARG_ACTION_in_rewriteTreeAtom1669 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_TOKEN_REF_in_rewriteTreeAtom1677 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RULE_REF_in_rewriteTreeAtom1687 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_rewriteTreeAtom1694 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LABEL_in_rewriteTreeAtom1701 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ACTION_in_rewriteTreeAtom1706 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ebnfSuffix_in_rewriteTreeEbnf1718 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_BLOCK_in_rewriteTreeEbnf1721 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_rewriteTreeAlt_in_rewriteTreeEbnf1723 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_TREE_BEGIN_in_rewriteTree1736 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_rewriteTreeAtom_in_rewriteTree1738 = new BitSet(new long[]{0xC400000000010008L,0x000000008000E008L});
    public static final BitSet FOLLOW_rewriteTreeElement_in_rewriteTree1740 = new BitSet(new long[]{0xC400000000010008L,0x000000008000E008L});
    public static final BitSet FOLLOW_TEMPLATE_in_rewriteTemplate1755 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_rewriteTemplateArgs_in_rewriteTemplate1757 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_DOUBLE_QUOTE_STRING_LITERAL_in_rewriteTemplate1760 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_TEMPLATE_in_rewriteTemplate1767 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_rewriteTemplateArgs_in_rewriteTemplate1769 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_DOUBLE_ANGLE_STRING_LITERAL_in_rewriteTemplate1772 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_rewriteTemplateRef_in_rewriteTemplate1778 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rewriteIndirectTemplateHead_in_rewriteTemplate1783 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ACTION_in_rewriteTemplate1788 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEMPLATE_in_rewriteTemplateRef1800 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_rewriteTemplateRef1802 = new BitSet(new long[]{0x0000000000000008L,0x0000000001000000L});
    public static final BitSet FOLLOW_rewriteTemplateArgs_in_rewriteTemplateRef1804 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_TEMPLATE_in_rewriteIndirectTemplateHead1818 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ACTION_in_rewriteIndirectTemplateHead1820 = new BitSet(new long[]{0x0000000000000008L,0x0000000001000000L});
    public static final BitSet FOLLOW_rewriteTemplateArgs_in_rewriteIndirectTemplateHead1822 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_ARGLIST_in_rewriteTemplateArgs1836 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_rewriteTemplateArg_in_rewriteTemplateArgs1838 = new BitSet(new long[]{0x0000000000000008L,0x0000000000800000L});
    public static final BitSet FOLLOW_ARG_in_rewriteTemplateArg1854 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_rewriteTemplateArg1856 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_ACTION_in_rewriteTemplateArg1858 = new BitSet(new long[]{0x0000000000000008L});

}