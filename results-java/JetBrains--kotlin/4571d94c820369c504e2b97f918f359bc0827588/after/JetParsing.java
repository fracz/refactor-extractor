/*
 * @author max
 */
package org.jetbrains.jet.lang.parsing;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.jet.JetNodeType;
import org.jetbrains.jet.lexer.JetKeywordToken;

import java.util.HashMap;
import java.util.Map;

import static org.jetbrains.jet.JetNodeTypes.*;
import static org.jetbrains.jet.lexer.JetTokens.*;

/**
 * @author max
 * @author abreslav
 */
public class JetParsing extends AbstractJetParsing {
    // TODO: token sets to constants, including derived methods
    public static final Map<String, IElementType> MODIFIER_KEYWORD_MAP = new HashMap<String, IElementType>();
    static {
        for (IElementType softKeyword : MODIFIER_KEYWORDS.getTypes()) {
            MODIFIER_KEYWORD_MAP.put(((JetKeywordToken) softKeyword).getValue(), softKeyword);
        }
    }

    private static final TokenSet TOPLEVEL_OBJECT_FIRST = TokenSet.create(TYPE_KEYWORD, CLASS_KEYWORD,
                FUN_KEYWORD, VAL_KEYWORD, NAMESPACE_KEYWORD);
    private static final TokenSet ENUM_MEMBER_FIRST = TokenSet.create(TYPE_KEYWORD, CLASS_KEYWORD,
                FUN_KEYWORD, VAL_KEYWORD, IDENTIFIER);

    private static final TokenSet CLASS_NAME_RECOVERY_SET = TokenSet.orSet(TokenSet.create(LT, WRAPS_KEYWORD, LPAR, COLON, LBRACE), TOPLEVEL_OBJECT_FIRST);
    private static final TokenSet TYPE_PARAMETER_GT_RECOVERY_SET = TokenSet.create(WHERE_KEYWORD, WRAPS_KEYWORD, LPAR, COLON, LBRACE, GT);
    private static final TokenSet PARAMETER_NAME_RECOVERY_SET = TokenSet.create(COLON, EQ, COMMA, RPAR);
    private static final TokenSet NAMESPACE_NAME_RECOVERY_SET = TokenSet.create(DOT, EOL_OR_SEMICOLON);
    /*package*/ static final TokenSet TYPE_REF_FIRST = TokenSet.create(LBRACKET, IDENTIFIER, FUN_KEYWORD, LPAR, CAPITALIZED_THIS_KEYWORD);
    private static final TokenSet RECEIVER_TYPE_TERMINATORS = TokenSet.create(DOT, SAFE_ACCESS);

    public static JetParsing createForTopLevel(SemanticWhitespaceAwarePsiBuilder builder) {
        builder.setDebugMode(true);
        JetParsing jetParsing = new JetParsing(builder);
        jetParsing.myExpressionParsing = new JetExpressionParsing(builder, jetParsing);
        return jetParsing;
    }

    public static JetParsing createForByClause(final SemanticWhitespaceAwarePsiBuilder builder) {
        final SemanticWhitespaceAwarePsiBuilderForByClause builderForByClause = new SemanticWhitespaceAwarePsiBuilderForByClause(builder);
        JetParsing jetParsing = new JetParsing(builderForByClause);
        jetParsing.myExpressionParsing = new JetExpressionParsing(builderForByClause, jetParsing) {
            @Override
            protected boolean parseCallWithClosure() {
                if (builderForByClause.getStackSize() > 0) {
                    return super.parseCallWithClosure();
                }
                return false;
            }

            @Override
            protected JetParsing create(SemanticWhitespaceAwarePsiBuilder builder) {
                return createForByClause(builder);
            }
        };
        return jetParsing;
    }

    private JetExpressionParsing myExpressionParsing;

    private JetParsing(SemanticWhitespaceAwarePsiBuilder builder) {
        super(builder);
    }

    /*
     * [start] jetlFile
     *   : preamble toplevelObject[| import]* [eof]
     *   ;
     */
    public void parseFile() {
        PsiBuilder.Marker fileMarker = mark();
        PsiBuilder.Marker namespaceMarker = mark();

        parsePreamble();

        parseToplevelDeclarations(false);

        namespaceMarker.done(NAMESPACE);
        fileMarker.done(JET_FILE);
    }

    /*
     * toplevelObject[| import]*
     */
    private void parseToplevelDeclarations(boolean insideBlock) {
        while (!eof() && (!insideBlock || !at(RBRACE))) {
            if (at(IMPORT_KEYWORD)) {
                parseImportDirective();
            }
            else {
                parseTopLevelObject();
            }
        }
    }

    /*
     *preamble
     *  : namespaceHeader? import*
     *  ;
     */
    private void parsePreamble() {
        /*
         * namespaceHeader
         *   : modifiers "namespace" SimpleName{"."} SEMI?
         *   ;
         */
        PsiBuilder.Marker firstEntry = mark();
        parseModifierList(MODIFIER_LIST, true);

        if (at(NAMESPACE_KEYWORD)) {
            advance(); // NAMESPACE_KEYWORD

            parseNamespaceName();

            if (at(LBRACE)) {
                firstEntry.rollbackTo();
                return;
            }
            firstEntry.drop();

            consumeIf(SEMICOLON);
        } else {
            firstEntry.rollbackTo();
        }

        while (at(IMPORT_KEYWORD)) {
            parseImportDirective();
        }
    }

    /* SimpleName{"."} */
    private void parseNamespaceName() {
        PsiBuilder.Marker nsName = mark();
        expect(IDENTIFIER, "Expecting qualified name", NAMESPACE_NAME_RECOVERY_SET);
        while (!eol() && at(DOT)) {
            advance(); // DOT
            expect(IDENTIFIER, "Namespace name must be a '.'-separated identifier list", NAMESPACE_NAME_RECOVERY_SET);
        }
        nsName.done(NAMESPACE_NAME);
    }

    /*
     * import
     *   : "import" ("namespace" ".")? SimpleName{"."} ("." "*" | "as" SimpleName)? SEMI?
     *   ;
     */
    private void parseImportDirective() {
        assert _at(IMPORT_KEYWORD);
        PsiBuilder.Marker importDirective = mark();
        advance(); // IMPORT_KEYWORD

        PsiBuilder.Marker qualifiedName = mark();
        if (at(NAMESPACE_KEYWORD)) {
            advance(); // NAMESPACE_KEYWORD
            expect(DOT, "Expecting '.'", TokenSet.create(IDENTIFIER, MUL, SEMICOLON));
        }

        PsiBuilder.Marker reference = mark();
        expect(IDENTIFIER, "Expecting qualified name", TokenSet.create(DOT, AS_KEYWORD));
        reference.done(REFERENCE_EXPRESSION);
        while (at(DOT) && lookahead(1) != MUL) {
            advance(); // DOT

            reference = mark();
            expect(IDENTIFIER, "Qualified name must be a '.'-separated identifier list", TokenSet.create(AS_KEYWORD, DOT, SEMICOLON));
            reference.done(REFERENCE_EXPRESSION);

            PsiBuilder.Marker precede = qualifiedName.precede();
            qualifiedName.done(DOT_QUALIFIED_EXPRESSION);
            qualifiedName = precede;
        }
        qualifiedName.drop();

        if (at(DOT)) {
            advance(); // DOT
            assert _at(MUL);
            advance(); // MUL
            handleUselessRename();
        }
        if (at(AS_KEYWORD)) {
            advance(); // AS_KEYWORD
            expect(IDENTIFIER, "Expecting identifier", TokenSet.create(SEMICOLON));
        }
        consumeIf(SEMICOLON);
        importDirective.done(IMPORT_DIRECTIVE);
    }

    private void handleUselessRename() {
        if (at(AS_KEYWORD)) {
            PsiBuilder.Marker as = mark();
            advance(); // AS_KEYWORD
            consumeIf(IDENTIFIER);
            as.error("Cannot rename a all imported items to one identifier");
        }
    }

    /*
     * toplevelObject
     *   : namespace
     *   : class
     *   : extension
     *   : function
     *   : property
     *   : typedef
     *   : object
     *   ;
     */
    private void parseTopLevelObject() {
        PsiBuilder.Marker decl = mark();

        TokenDetector detector = new TokenDetector(ENUM_KEYWORD);
        parseModifierList(MODIFIER_LIST, detector, true);

        IElementType keywordToken = tt();
        JetNodeType declType = null;
        if (keywordToken == NAMESPACE_KEYWORD) {
            declType = parseNamespaceBlock();
        }
        else if (keywordToken == CLASS_KEYWORD) {
            declType = parseClass(detector.isDetected());
        }
        else if (keywordToken == FUN_KEYWORD) {
            declType = parseFunction();
        }
        else if (keywordToken == VAL_KEYWORD || keywordToken == VAR_KEYWORD) {
            declType = parseProperty();
        }
        else if (keywordToken == TYPE_KEYWORD) {
            declType = parseTypeDef();
        }
        else if (keywordToken == OBJECT_KEYWORD) {
            parseObject(true, true);
            declType = OBJECT_DECLARATION;
        }

        if (declType == null) {
            errorAndAdvance("Expecting namespace or top level declaration");
            decl.drop();
        }
        else {
            decl.done(declType);
        }
    }

    /*
     * (modifier | attribute)*
     */
    public boolean parseModifierList(JetNodeType nodeType, boolean allowShortAnnotations) {
        return parseModifierList(nodeType, null, allowShortAnnotations);
    }

    /**
     * (modifier | attribute)*
     *
     * Feeds modifiers (not attributes) into the passed consumer, if it is not null
     */
    public boolean parseModifierList(JetNodeType nodeType, Consumer<IElementType> tokenConsumer, boolean allowShortAnnotations) {
        PsiBuilder.Marker list = mark();
        boolean empty = true;
        while (!eof()) {
            if (atSet(MODIFIER_KEYWORDS)) {
                if (tokenConsumer != null) tokenConsumer.consume(tt());
                advance(); // MODIFIER
            }
            else if (at(LBRACKET) || (allowShortAnnotations && at(IDENTIFIER))) {
                parseAnnotation(allowShortAnnotations);
            }
            else {
                break;
            }
            empty = false;
        }
        if (empty) {
            list.drop();
        } else {
            list.done(nodeType);
        }
        return !empty;
    }

    /*
     * annotations
     *   : annotation*
     *   ;
     */
    public void parseAnnotations(boolean allowShortAnnotations) {
        while (parseAnnotation(allowShortAnnotations));
    }

    /*
     * annotation
     *   : "[" annotationEntry+ "]"
     *   : annotationEntry
     *   ;
     */
    private boolean parseAnnotation(boolean allowShortAnnotations) {
        if (at(LBRACKET)) {
            PsiBuilder.Marker annotation = mark();

            myBuilder.disableNewlines();
            advance(); // LBRACKET

            if (!at(IDENTIFIER)) {
                error("Expecting a list of attributes");
            }
            else {
                parseAnnotationEntry();
                while (at(COMMA)) {
                    errorAndAdvance("No commas needed to separate attributes");
                }

                while (at(IDENTIFIER)) {
                    parseAnnotationEntry();
                    while (at(COMMA)) {
                        errorAndAdvance("No commas needed to separate attributes");
                    }
                }
            }

            expect(RBRACKET, "Expecting ']' to close an attribute annotation");
            myBuilder.restoreNewlinesState();

            annotation.done(ANNOTATION);
            return true;
        }
        else if (allowShortAnnotations && at(IDENTIFIER)) {
            parseAnnotationEntry();
            return true;
        }
        return false;
    }

    /*
     * annotationEntry
     *   : SimpleName{"."} typeArguments? valueArguments?
     *   ;
     */
    private void parseAnnotationEntry() {
        assert _at(IDENTIFIER);

        PsiBuilder.Marker attribute = mark();

        PsiBuilder.Marker reference = mark();
        PsiBuilder.Marker typeReference = mark();
        parseUserType();
        typeReference.done(TYPE_REFERENCE);
        reference.done(CONSTRUCTOR_CALLEE);

        parseTypeArgumentList(-1);

        if (at(LPAR)) {
            myExpressionParsing.parseValueArgumentList();
        }
        attribute.done(ANNOTATION_ENTRY);
    }

    /*
     * namespace
     *   : "namespace" SimpleName{"."} "{"
     *        import*
     *        toplevelObject[| import]*
     *     "}"
     *   ;
     */
    private JetNodeType parseNamespaceBlock() {
        assert _at(NAMESPACE_KEYWORD);
        advance(); // NAMESPACE_KEYWORD

        if (at(LBRACE)) {
            error("Expecting namespace name");
        }
        else {
            parseNamespaceName();
        }

        if (!at(LBRACE)) {
            error("A namespace block in '{...}' expected");
            return NAMESPACE;
        }

        myBuilder.enableNewlines();
        advance(); // LBRACE
        PsiBuilder.Marker namespaceBody = mark();

        parseToplevelDeclarations(true);

        namespaceBody.done(NAMESPACE_BODY);
        expect(RBRACE, "Expecting '}'");
        myBuilder.restoreNewlinesState();

        return NAMESPACE;
    }

    /*
     * class
     *   : modifiers "class" SimpleName
     *       typeParameters?
     *       (
     *          ("wraps" "(" primaryConstructorParameter{","} ")") |
     *          (modifiers "(" primaryConstructorParameter{","} ")"))?
     *       (":" attributes delegationSpecifier{","})?
     *       typeConstraints
     *       (classBody? | enumClassBody)
     *   ;
     */
    public JetNodeType parseClass(boolean enumClass) {
        assert _at(CLASS_KEYWORD);
        advance(); // CLASS_KEYWORD

        expect(IDENTIFIER, "Class name expected", CLASS_NAME_RECOVERY_SET);
        boolean typeParametersDeclared = parseTypeParameterList(TYPE_PARAMETER_GT_RECOVERY_SET);

        if (at(WRAPS_KEYWORD)) {
            advance(); // WRAPS_KEYWORD
            parseValueParameterList(false, TokenSet.create(COLON, LBRACE));
        }
        else {
            if (parseModifierList(PRIMARY_CONSTRUCTOR_MODIFIER_LIST, false)) {
                parseValueParameterList(false, TokenSet.create(COLON, LBRACE));
            }
            else {
                if (at(LPAR)) {
                    parseValueParameterList(false, TokenSet.create(COLON, LBRACE));
                }
            }
        }

        if (at(COLON)) {
            advance(); // COLON
            parseDelegationSpecifierList();
        }

        parseTypeConstraintsGuarded(typeParametersDeclared);

        if (at(LBRACE)) {
            if (enumClass) {
                parseEnumClassBody();
            }
            else {
                parseClassBody();
            }
        }

        return CLASS;
    }

    /*
     * enumClassBody
     *   : "{" enumEntry* "}"
     *   ;
     */
    private void parseEnumClassBody() {
        if (!at(LBRACE)) return;

        PsiBuilder.Marker classBody = mark();

        myBuilder.enableNewlines();
        advance(); // LBRACE

        while (!eof() && !at(RBRACE)) {
            PsiBuilder.Marker entryOrMember = mark();

            TokenSet constructorNameFollow = TokenSet.create(SEMICOLON, COLON, LPAR, LT, LBRACE);
            int lastId = findLastBefore(ENUM_MEMBER_FIRST, constructorNameFollow, false);
            TokenDetector enumDetector = new TokenDetector(ENUM_KEYWORD);
            createTruncatedBuilder(lastId).parseModifierList(MODIFIER_LIST, enumDetector, false);

            IElementType type;
            if (at(IDENTIFIER)) {
                parseEnumEntry();
                type = ENUM_ENTRY;
            }
            else {
                type = parseMemberDeclarationRest(enumDetector.isDetected());
            }

            if (type == null) {
                errorAndAdvance("Expecting an enum entry or member declaration");
                entryOrMember.drop();
            }
            else {
                entryOrMember.done(type);
            }
        }

        expect(RBRACE, "Expecting '}' to close enum class body");
        myBuilder.restoreNewlinesState();

        classBody.done(CLASS_BODY);
    }

    /*
     * enumEntry
     *   : modifiers SimpleName typeParameters? primaryConstructorParameters? (":" initializer{","})? typeConstraints classBody?
     *   ;
     */
    private void parseEnumEntry() {
        assert _at(IDENTIFIER);

        PsiBuilder.Marker nameAsDeclaration = mark();
        advance(); // IDENTIFIER
        nameAsDeclaration.done(OBJECT_DECLARATION_NAME);

        boolean typeParametersDeclared = parseTypeParameterList(TokenSet.create(COLON, LPAR, SEMICOLON, LBRACE));

        if (at(LPAR)) {
            parseValueParameterList(false, TokenSet.create(COLON, SEMICOLON, LBRACE));
        }

        if (at(COLON)) {
            advance(); // COLON

            parseInitializerList();
        }

        parseTypeConstraintsGuarded(typeParametersDeclared);

        if (at(LBRACE)) {
            parseClassBody();
        }

        consumeIf(SEMICOLON);
    }

    /*
     * classBody
     *   : ("{" memberDeclaration "}")?
     *   ;
     */
    /*package*/ void parseClassBody() {
        PsiBuilder.Marker body = mark();

        myBuilder.enableNewlines();
        expect(LBRACE, "Expecting a class body", TokenSet.create(LBRACE));

        while (!eof()) {
            if (at(RBRACE)) {
                break;
            }
            parseMemberDeclaration();
        }
        expect(RBRACE, "Missing '}");
        myBuilder.restoreNewlinesState();

        body.done(CLASS_BODY);
    }

    /*
     * memberDeclaration
     *   : modifiers memberDeclaration'
     *   ;
     *
     * memberDeclaration'
     *   : classObject
     *   : constructor
     *   : function
     *   : property
     *   : class
     *   : extension
     *   : typedef
     *   : anonymousInitializer
     *   : object
     *   ;
     */
    private void parseMemberDeclaration() {
        PsiBuilder.Marker decl = mark();

        TokenDetector enumDetector = new TokenDetector(ENUM_KEYWORD);
        parseModifierList(MODIFIER_LIST, enumDetector, true);

        JetNodeType declType = parseMemberDeclarationRest(enumDetector.isDetected());

        if (declType == null) {
            errorAndAdvance("Expecting member declaration");
            decl.drop();
        }
        else {
            decl.done(declType);
        }
    }

    private JetNodeType parseMemberDeclarationRest(boolean isEnum) {
        IElementType keywordToken = tt();
        JetNodeType declType = null;
        if (keywordToken == CLASS_KEYWORD) {
            if (lookahead(1) == OBJECT_KEYWORD) {
                declType = parseClassObject();
            }
            else {
                declType = parseClass(isEnum);
            }
        }
        else if (keywordToken == FUN_KEYWORD) {
            declType = parseFunction();
        }
        else if (keywordToken == VAL_KEYWORD || keywordToken == VAR_KEYWORD) {
            declType = parseProperty();
        }
        else if (keywordToken == TYPE_KEYWORD) {
            declType = parseTypeDef();
        }
        else if (keywordToken == THIS_KEYWORD) {
            declType = parseConstructor();
        }
        else if (keywordToken == OBJECT_KEYWORD) {
            parseObject(true, true);
            declType = OBJECT_DECLARATION;
        } else if (keywordToken == LBRACE) {
            parseBlock();
            declType = ANONYMOUS_INITIALIZER;
        }
        return declType;
    }

    /*
     * object
     *   : "object" SimpleName? ":" delegationSpecifier{","}? classBody?
     *   ;
     */
    public void parseObject(boolean named, boolean optionalBody) {
        assert _at(OBJECT_KEYWORD);

        advance(); // OBJECT_KEYWORD

        if (named) {
            PsiBuilder.Marker propertyDeclaration = mark();
            expect(IDENTIFIER, "Expecting object name", TokenSet.create(LBRACE));
            propertyDeclaration.done(OBJECT_DECLARATION_NAME);
        }
        else {
            if (at(IDENTIFIER)) {
                error("An object expression cannot bind a name");
            }
        }

        if (optionalBody) {
            if (at(COLON)) {
                advance(); // COLON
                parseDelegationSpecifierList();
            }
            if (at(LBRACE)) {
                parseClassBody();
            }
        }
        else {
            if (at(LBRACE)) {
                parseClassBody();
            }
            else {
                expect(COLON, "Expecting ':'", TokenSet.create(IDENTIFIER, NAMESPACE_KEYWORD));
                parseDelegationSpecifierList();
                parseClassBody();
            }
        }
    }

    /*
     * constructor
     *   : modifiers "this" functionParameters (":" initializer{","}) block?
     *   ;
     */
    private JetNodeType parseConstructor() {
        assert _at(THIS_KEYWORD);

        advance(); // THIS_KEYWORD

        parseValueParameterList(false, TokenSet.create(COLON, LBRACE, SEMICOLON));

        if (at(COLON)) {
            advance(); // COLON

            parseInitializerList();
        }

        if (at(LBRACE)) {
            parseBlock();
        }
        else {
            consumeIf(SEMICOLON);
        }

        return CONSTRUCTOR;
    }

    /*
     * initializer{","}
     */
    private void parseInitializerList() {
        PsiBuilder.Marker list = mark();
        while (true) {
            if (at(COMMA)) errorAndAdvance("Expecting a this or super constructor call");
            parseInitializer();
            if (!at(COMMA)) break;
            advance(); // COMMA
        }
        list.done(INITIALIZER_LIST);
    }

    /*
     * initializer
     *   : attributes "this" valueArguments
     *   : attributes constructorInvocation // type parameters may (must?) be omitted
     *   ;
     */
    private void parseInitializer() {
        PsiBuilder.Marker initializer = mark();
        parseAnnotations(false);

        IElementType type;
        if (at(THIS_KEYWORD)) {
            PsiBuilder.Marker mark = mark();
            advance(); // THIS_KEYWORD
            mark.done(THIS_CONSTRUCTOR_REFERENCE);
            type = THIS_CALL;
        }
        else if (atSet(TYPE_REF_FIRST)) {
            PsiBuilder.Marker reference = mark();
            parseTypeRef();
            reference.done(CONSTRUCTOR_CALLEE);
            type = DELEGATOR_SUPER_CALL;
        } else {
            errorWithRecovery("Expecting constructor call (this(...)) or supertype initializer", TokenSet.create(LBRACE, COMMA));
            initializer.drop();
            return;
        }
        myExpressionParsing.parseValueArgumentList();

        initializer.done(type);
    }

    /*
     * classObject
     *   : modifiers "class" object
     *   ;
     */
    private JetNodeType parseClassObject() {
        assert _at(CLASS_KEYWORD) && lookahead(1) == OBJECT_KEYWORD;

        advance(); // CLASS_KEYWORD

        final PsiBuilder.Marker objectDeclaration = mark();
        parseObject(false, true);
        objectDeclaration.done(OBJECT_DECLARATION);

        return CLASS_OBJECT;
    }

    /*
     * typedef
     *   : modifiers "type" SimpleName (typeParameters typeConstraints)? "=" type
     *   ;
     */
    public JetNodeType parseTypeDef() {
        assert _at(TYPE_KEYWORD);

        advance(); // TYPE_KEYWORD

        expect(IDENTIFIER, "Type name expected", TokenSet.orSet(TokenSet.create(LT, EQ, SEMICOLON), TOPLEVEL_OBJECT_FIRST));

        if (parseTypeParameterList(TYPE_PARAMETER_GT_RECOVERY_SET)) {
            parseTypeConstraints();
        }

        expect(EQ, "Expecting '='", TokenSet.orSet(TOPLEVEL_OBJECT_FIRST, TokenSet.create(SEMICOLON)));

        parseTypeRef();

        consumeIf(SEMICOLON);

        return TYPEDEF;
    }

    /*
     * property
     *   : modifiers ("val" | "var")
     *       typeParameters? (type "." | attributes)?
     *       SimpleName (":" type)?
     *       typeConstraints
     *       ("=" element SEMI?)?
     *       (getter? setter? | setter? getter?) SEMI?
     *   ;
     */
    public JetNodeType parseProperty() {
        return parseProperty(false);
    }

    public JetNodeType parseProperty(boolean local) {
        if (at(VAL_KEYWORD) || at(VAR_KEYWORD)) {
            advance(); // VAL_KEYWORD or VAR_KEYWORD
        } else {
            errorAndAdvance("Expecting 'val' or 'var'");
        }

        boolean typeParametersDeclared = at(LT) ? parseTypeParameterList(TokenSet.create(IDENTIFIER, EQ, COLON, SEMICOLON)) : false;

        TokenSet propertyNameFollow = TokenSet.create(COLON, EQ, LBRACE, SEMICOLON);

        // TODO: extract constant
        int lastDot = matchTokenStreamPredicate(new FirstBefore(
                new AtSet(DOT, SAFE_ACCESS),
                new AbstractTokenStreamPredicate() {
                    @Override
                    public boolean matching(boolean topLevel) {
                        if (topLevel && (at(EQ) || at(COLON))) return true;
                        if (topLevel && at(IDENTIFIER)) {
                            IElementType lookahead = lookahead(1);
                            return lookahead != LT && lookahead != DOT && lookahead != SAFE_ACCESS;
                        }
                        return false;
                    }
                }));

        parseReceiverType("property", propertyNameFollow, lastDot);

        if (at(COLON)) {
            advance(); // COLON
            parseTypeRef();
        }

        parseTypeConstraintsGuarded(typeParametersDeclared);

        if (local) {
            if (at(EQ)) {
                advance(); // EQ
                myExpressionParsing.parseExpression();
                // "val a = 1; b" must not be an infix call of b on "val ...;"
            }
        }
        else {
            if (at(EQ)) {
                advance(); // EQ
                myExpressionParsing.parseExpression();
                consumeIf(SEMICOLON);
            }

            if (parsePropertyGetterOrSetter()) {
                parsePropertyGetterOrSetter();
            }
            if  (!atSet(EOL_OR_SEMICOLON, RBRACE)) {
                if (getLastToken() != SEMICOLON) {
                    errorUntil("Property getter or setter expected", TokenSet.create(EOL_OR_SEMICOLON));
                }
            }
            else {
                consumeIf(SEMICOLON);
            }
        }


        return PROPERTY;
    }

    /*
     * getterOrSetter
     *   : modifiers ("get" | "set")
     *   :
     *        (     "get" "(" ")"
     *           |
     *              "set" "(" modifiers parameter ")"
     *        ) functionBody
     *   ;
     */
    private boolean parsePropertyGetterOrSetter() {
        PsiBuilder.Marker getterOrSetter = mark();

        parseModifierList(MODIFIER_LIST, false);

        if (!at(GET_KEYWORD) && !at(SET_KEYWORD)) {
            getterOrSetter.rollbackTo();
            return false;
        }

        boolean setter = at(SET_KEYWORD);
        advance(); // GET_KEYWORD or SET_KEYWORD

        if (!at(LPAR)) {
            // Account for Jet-114 (val a : int get {...})
            TokenSet ACCESSOR_FIRST_OR_PROPERTY_END = TokenSet.orSet(MODIFIER_KEYWORDS, TokenSet.create(LBRACKET, GET_KEYWORD, SET_KEYWORD, EOL_OR_SEMICOLON, RBRACE));
            if (!atSet(ACCESSOR_FIRST_OR_PROPERTY_END)) {
                errorUntil("Accessor body expected", TokenSet.orSet(ACCESSOR_FIRST_OR_PROPERTY_END, TokenSet.create(LBRACE, LPAR, EQ)));
            }
            else {
                getterOrSetter.done(PROPERTY_ACCESSOR);
                return true;
            }
        }

        myBuilder.disableNewlines();
        expect(LPAR, "Expecting '('", TokenSet.create(RPAR, IDENTIFIER, COLON, LBRACE, EQ));
        if (setter) {
            PsiBuilder.Marker parameterList = mark();
            PsiBuilder.Marker setterParameter = mark();
            parseModifierListWithShortAnnotations(MODIFIER_LIST, TokenSet.create(IDENTIFIER), TokenSet.create(RPAR, COMMA, COLON));
            expect(IDENTIFIER, "Expecting parameter name", TokenSet.create(RPAR, COLON, LBRACE, EQ));

            if (at(COLON)) {
                advance();

                parseTypeRef();
            }
            setterParameter.done(VALUE_PARAMETER);
            parameterList.done(VALUE_PARAMETER_LIST);
        }
        if (!at(RPAR)) errorUntil("Expecting ')'", TokenSet.create(RPAR, COLON, LBRACE, EQ, EOL_OR_SEMICOLON));
        expect(RPAR, "Expecting ')'", TokenSet.create(RPAR, COLON, LBRACE, EQ));
        myBuilder.restoreNewlinesState();

        if (at(COLON)) {
            advance();

            parseTypeRef();
        }

        parseFunctionBody();

        getterOrSetter.done(PROPERTY_ACCESSOR);

        return true;
    }

    /*
     * function
     *   : modifiers "fun" typeParameters?
     *       (type "." | attributes)?
     *       SimpleName
     *       typeParameters? functionParameters (":" type)?
     *       typeConstraints
     *       functionBody?
     *   ;
     */
    public JetNodeType parseFunction() {
        assert _at(FUN_KEYWORD);

        advance(); // FUN_KEYWORD

        // Recovery for the case of class A { fun| }
        if (at(RBRACE)) {
            error("Function body expected");
            return FUN;
        }

        boolean typeParameterListOccurred = false;
        if (at(LT)) {
            parseTypeParameterList(TokenSet.create(LBRACKET, LBRACE, LPAR));
            typeParameterListOccurred = true;
        }

        int lastDot = findLastBefore(RECEIVER_TYPE_TERMINATORS, TokenSet.create(LPAR), true);
        parseReceiverType("function", TokenSet.create(LT, LPAR, COLON, EQ), lastDot);

        TokenSet valueParametersFollow = TokenSet.create(COLON, EQ, LBRACE, SEMICOLON, RPAR);

        if (at(LT)) {
            PsiBuilder.Marker error = mark();
            parseTypeParameterList(TokenSet.orSet(TokenSet.create(LPAR), valueParametersFollow));
            if (typeParameterListOccurred) {
                error.error("Only one type parameter list is allowed for a function"); // TODO : discuss
            }
            else {
                error.drop();
            }
            typeParameterListOccurred = true;
        }

        parseValueParameterList(false, valueParametersFollow);

        if (at(COLON)) {
            advance(); // COLON

            parseTypeRef();
        }

        parseTypeConstraintsGuarded(typeParameterListOccurred);

        if (at(SEMICOLON)) {
            advance(); // SEMICOLON
        }
        else if (at(EQ) || at(LBRACE)) {
            parseFunctionBody();
        }

        return FUN;
    }

    /*
     * :
     *   (type "." | attributes)?
     */
    private void parseReceiverType(String title, TokenSet nameFollow, int lastDot) {

        if (lastDot == -1) { // There's no explicit receiver type specified
            parseAnnotations(false);
            expect(IDENTIFIER, "Expecting " + title + " name or receiver type", nameFollow);
        } else {
            PsiBuilder.Marker typeRefMarker = mark();
            PsiBuilder.Marker nullableType = mark();
            typeRefMarker = createTruncatedBuilder(lastDot).parseTypeRefContents(typeRefMarker);
            if (at(SAFE_ACCESS)) {
                nullableType.done(NULLABLE_TYPE);
            }
            else {
                nullableType.drop();
            }
            typeRefMarker.done(TYPE_REFERENCE);

            if (atSet(RECEIVER_TYPE_TERMINATORS)) {
                advance(); // expectation
            }
            else {
                errorWithRecovery("Expecting '.' before a " + title + " name", nameFollow);
            }

            expect(IDENTIFIER, "Expecting " + title + " name", nameFollow);
        }
    }

    /*
     * functionBody
     *   : block
     *   : "=" element
     *   ;
     */
    private void parseFunctionBody() {
        if (at(LBRACE)) {
            parseBlock();
        }
        else if (at(EQ)) {
            advance(); // EQ
            myExpressionParsing.parseExpression();
            consumeIf(SEMICOLON);
        }
        else {
            errorAndAdvance("Expecting function body");
        }
    }

    /*
     * block
     *   : "{" (expressions)* "}"
     *   ;
     */
    public void parseBlock() {
        PsiBuilder.Marker block = mark();

        myBuilder.enableNewlines();
        expect(LBRACE, "Expecting '{' to open a block");

        myExpressionParsing.parseStatements();

        expect(RBRACE, "Expecting '}");
        myBuilder.restoreNewlinesState();

        block.done(BLOCK);
    }

    /*
     * delegationSpecifier{","}
     */
    /*package*/ void parseDelegationSpecifierList() {
        PsiBuilder.Marker list = mark();

        while (true) {
            if (at(COMMA)) {
                errorAndAdvance("Expecting a delegation specifier");
                continue;
            }
            parseDelegationSpecifier();
            if (!at(COMMA)) break;
            advance(); // COMMA
        }

        list.done(DELEGATION_SPECIFIER_LIST);
    }

    /*
     * attributes delegationSpecifier
     *
     * delegationSpecifier
     *   : constructorInvocation // type and constructor arguments
     *   : explicitDelegation
     *   ;
     *
     * explicitDelegation
     *   : userType "by" element
     *   ;
     */
    private void parseDelegationSpecifier() {
        PsiBuilder.Marker delegator = mark();
        parseAnnotations(false);

        PsiBuilder.Marker reference = mark();
        parseTypeRef();

        if (at(BY_KEYWORD)) {
            reference.drop();
            advance(); // BY_KEYWORD
            createForByClause(myBuilder).myExpressionParsing.parseExpression();
            delegator.done(DELEGATOR_BY);
        }
        else if (at(LPAR)) {
            reference.done(CONSTRUCTOR_CALLEE);
            myExpressionParsing.parseValueArgumentList();
            delegator.done(DELEGATOR_SUPER_CALL);
        }
        else {
            reference.drop();
            delegator.done(DELEGATOR_SUPER_CLASS);
        }
    }

    /*
     * typeParameters
     *   : ("<" typeParameter{","} ">"
     *   ;
     */
    private boolean parseTypeParameterList(TokenSet recoverySet) {
        PsiBuilder.Marker list = mark();
        boolean result = false;
        if (at(LT)) {

            myBuilder.disableNewlines();
            advance(); // LT

            while (true) {
                if (at(COMMA)) errorAndAdvance("Expecting type parameter declaration");
                parseTypeParameter();

                if (!at(COMMA)) break;
                advance(); // COMMA
            }

            expect(GT, "Missing '>'", recoverySet);
            myBuilder.restoreNewlinesState();
            result = true;
        }
        list.done(TYPE_PARAMETER_LIST);
        return result;
    }

    /*
     * typeConstraints
     *   : ("where" typeConstraint{","})?
     *   ;
     */
    private void parseTypeConstraintsGuarded(boolean typeParameterListOccurred) {
        PsiBuilder.Marker error = mark();
        boolean constraints = parseTypeConstraints();
        if (constraints && !typeParameterListOccurred) {
            error.error("Type constraints are not allowed when no type parameters declared");
        }
        else {
            error.drop();
        }
    }

    private boolean parseTypeConstraints() {
        if (at(WHERE_KEYWORD)) {
            parseTypeConstraintList();
            return true;
        }
        return false;
    }

    /*
     * typeConstraint{","}
     */
    private void parseTypeConstraintList() {
        assert _at(WHERE_KEYWORD);

        advance(); // WHERE_KEYWORD

        PsiBuilder.Marker list = mark();

        while (true) {
            if (at(COMMA)) errorAndAdvance("Type constraint expected");
            parseTypeConstraint();
            if (!at(COMMA)) break;
            advance(); // COMMA
        }

        list.done(TYPE_CONSTRAINT_LIST);
    }

    /*
     * typeConstraint
     *   : attributes SimpleName ":" type
     *   : attributes "class" "object" SimpleName ":" type
     *   ;
     */
    private void parseTypeConstraint() {
        PsiBuilder.Marker constraint = mark();

        parseAnnotations(false);

        if (at(CLASS_KEYWORD)) {
            advance(); // CLASS_KEYWORD

            expect(OBJECT_KEYWORD, "Expecting 'object'", TYPE_REF_FIRST);

        }

        PsiBuilder.Marker reference = mark();
        expect(IDENTIFIER, "Expecting type parameter name", TokenSet.orSet(TokenSet.create(COLON, COMMA), TYPE_REF_FIRST));
        reference.done(REFERENCE_EXPRESSION);

        expect(COLON, "Expecting ':' before the upper bound", TYPE_REF_FIRST);

        parseTypeRef();

        constraint.done(TYPE_CONSTRAINT);
    }

    /*
     * typeParameter
     *   : modifiers SimpleName (":" userType)?
     *   ;
     */
    private void parseTypeParameter() {
        if (atSet(TYPE_PARAMETER_GT_RECOVERY_SET)) {
            error("Type parameter declaration expected");
            return;
        }

        PsiBuilder.Marker mark = mark();

        parseModifierListWithShortAnnotations(MODIFIER_LIST, TokenSet.create(IDENTIFIER), TokenSet.create(COMMA, GT, COLON));

        expect(IDENTIFIER, "Type parameter name expected", TokenSet.EMPTY);

        if (at(COLON)) {
            advance(); // COLON
            parseTypeRef();
        }

        mark.done(TYPE_PARAMETER);

    }

    /*
     * type
     *   : attributes typeDescriptor
     *
     * typeDescriptor
     *   : selfType
     *   : functionType
     *   : userType
     *   : tupleType
     *   : nullableType
     *   ;
     *
     * nullableType
     *   : typeDescriptor "?"
     */
    public void parseTypeRef() {
        parseTypeRefContents(mark()).done(TYPE_REFERENCE);
    }

    private PsiBuilder.Marker parseTypeRefContents(PsiBuilder.Marker typeRefMarker) {
        parseAnnotations(false);

        if (at(IDENTIFIER) || at(NAMESPACE_KEYWORD)) {
            parseUserType();
        }
        else if (at(FUN_KEYWORD)) {
            parseFunctionType();
        }
        else if (at(LPAR)) {
            parseTupleType();
        }
        else if (at(CAPITALIZED_THIS_KEYWORD)) {
            parseSelfType();
        }
        else {
            errorWithRecovery("Type expected",
                    TokenSet.orSet(TOPLEVEL_OBJECT_FIRST,
                            TokenSet.create(EQ, COMMA, GT, RBRACKET, DOT, RPAR, RBRACE, LBRACE, SEMICOLON)));
        }

        while (at(QUEST)) {
            PsiBuilder.Marker precede = typeRefMarker.precede();

            advance(); // QUEST
            typeRefMarker.done(NULLABLE_TYPE);

            typeRefMarker = precede;
        }
        return typeRefMarker;
    }

    /*
     * selfType
     *   : "This"
     *   ;
     */
    private void parseSelfType() {
        assert _at(CAPITALIZED_THIS_KEYWORD);

        PsiBuilder.Marker type = mark();
        advance(); // CAPITALIZED_THIS_KEYWORD
        type.done(SELF_TYPE);
    }

    /*
     * userType
     *   : ("namespace" ".")? simpleUserType{"."}
     *   ;
     */
    private void parseUserType() {
        PsiBuilder.Marker userType = mark();

        if (at(NAMESPACE_KEYWORD)) {
            advance(); // NAMESPACE_KEYWORD
            expect(DOT, "Expecting '.'", TokenSet.create(IDENTIFIER));
        }

        PsiBuilder.Marker reference = mark();
        while (true) {
            expect(IDENTIFIER, "Type name expected", TokenSet.orSet(JetExpressionParsing.EXPRESSION_FIRST, JetExpressionParsing.EXPRESSION_FOLLOW));
            reference.done(REFERENCE_EXPRESSION);

            parseTypeArgumentList(-1);
            if (!at(DOT)) {
                break;
            }

            PsiBuilder.Marker precede = userType.precede();
            userType.done(USER_TYPE);
            userType = precede;

            advance(); // DOT
            reference = mark();
        }

        userType.done(USER_TYPE);
    }

    /*
     *  (optionalProjection type){","}
     */
    public PsiBuilder.Marker parseTypeArgumentList(int expectedGtOffset) {
        if (!at(LT)) return null;

        PsiBuilder.Marker list = mark();

        myBuilder.disableNewlines();
        advance(); // LT

        while (true) {
            PsiBuilder.Marker projection = mark();

            TokenSet lookFor = TokenSet.create(IDENTIFIER);
            TokenSet stopAt = TokenSet.create(COMMA, COLON, GT);
            parseModifierListWithShortAnnotations(MODIFIER_LIST, lookFor, stopAt);

            if (at(MUL)) {
                advance(); // MUL
            } else {
                parseTypeRef();
            }
            projection.done(TYPE_PROJECTION);
            if (!at(COMMA)) break;
            advance(); // COMMA
        }

        if (expectedGtOffset >= 0 && myBuilder.getCurrentOffset() < expectedGtOffset) {
            final PsiBuilder.Marker error = mark();
            while (myBuilder.getCurrentOffset() < expectedGtOffset) {
                advance();
            }
            error.error("Expecting a '>'");
        }

        expect(GT, "Expecting a '>'");
        myBuilder.restoreNewlinesState();

        list.done(TYPE_ARGUMENT_LIST);
        return list;
    }

    private void parseModifierListWithShortAnnotations(JetNodeType modifierList, TokenSet lookFor, TokenSet stopAt) {
        int lastId = findLastBefore(lookFor, stopAt, false);
        createTruncatedBuilder(lastId).parseModifierList(modifierList, false);
    }

    /*
     * tupleType
     *   : "(" type{","}? ")"
     *   : "(" parameter{","} ")" // tuple with named entries, the names do not affect assignment compatibility
     *   ;
     */
    private void parseTupleType() {
        // TODO : prohibit (a)
        assert _at(LPAR);

        PsiBuilder.Marker tuple = mark();

        myBuilder.disableNewlines();
        advance(); // LPAR

        if (!at(RPAR)) {
            while (true) {
                if (at(COLON)) {
                    errorAndAdvance("Expecting a name for tuple entry");
                }

                if (at(IDENTIFIER) && lookahead(1) == COLON) {
                    PsiBuilder.Marker labeledEntry = mark();
                    advance(); // IDENTIFIER
                    advance(); // COLON
                    parseTypeRef();
                    labeledEntry.done(LABELED_TUPLE_TYPE_ENTRY);
                }
                else if (TYPE_REF_FIRST.contains(tt())) {
                    parseTypeRef();
                }
                else {
                    error("Type expected");
                    break;
                }
                if (!at(COMMA)) break;
                advance(); // COMMA
            }
        }

        expect(RPAR, "Expecting ')");
        myBuilder.restoreNewlinesState();

        tuple.done(TUPLE_TYPE);
    }

    /*
     * functionType
     *   : "fun" (type ".")? functionTypeContents
     *   ;
     */
    private void parseFunctionType() {
        assert _at(FUN_KEYWORD);

        PsiBuilder.Marker functionType = mark();

        advance(); // FUN_KEYWORD

        int lastLPar = findLastBefore(TokenSet.create(LPAR), TokenSet.create(COLON), false);
        if (lastLPar >= 0 && lastLPar > myBuilder.getCurrentOffset()) {
            // TODO : -1 is a hack?
            createTruncatedBuilder(lastLPar - 1).parseTypeRef();
            advance(); // DOT
        }

        parseFunctionTypeContents();

        functionType.done(FUNCTION_TYPE);
    }

    /*
     * functionTypeContents
     *   : "(" (parameter | type){","} ")" ":" type
     *   ;
     */
    private void parseFunctionTypeContents() {
        parseValueParameterList(true, TokenSet.EMPTY);

        expect(COLON, "Expecting ':' followed by a return type", TYPE_REF_FIRST);

        parseTypeRef();
    }

    /*
     * functionParameters
     *   : "(" functionParameter{","}? ")" // default values
     *   ;
     *
     * functionParameter
     *   : modifiers functionParameterRest
     *   ;
     *
     * functionParameterRest
     *   : parameter ("=" element)?
     *   ;
     */
    public void parseValueParameterList(boolean isFunctionTypeContents, TokenSet recoverySet) {
        PsiBuilder.Marker parameters = mark();

        myBuilder.disableNewlines();
        expect(LPAR, "Expecting '(", recoverySet);

        if (!at(RPAR)) {
            while (true) {
                if (at(COMMA)) {
                    errorAndAdvance("Expecting a parameter declaration");
                }
                else if (at(RPAR)) {
                    error("Expecting a parameter declaration");
                    break;
                }
                if (isFunctionTypeContents) {
                    if (!tryParseValueParameter()) {
                        PsiBuilder.Marker valueParameter = mark();
                        parseModifierList(MODIFIER_LIST, false); // lazy, out, ref
                        parseTypeRef();
                        valueParameter.done(VALUE_PARAMETER);
                    }
                } else {
                    parseValueParameter();
                }
                if (!at(COMMA)) break;
                advance(); // COMMA
            }
        }
        expect(RPAR, "Expecting ')'", recoverySet);
        myBuilder.restoreNewlinesState();

        parameters.done(VALUE_PARAMETER_LIST);
    }

    /*
     * functionParameter
     *   : modifiers ("val" | "var")? parameter ("=" element)?
     *   ;
     */
    private boolean tryParseValueParameter() {
        return parseValueParameter(true);
    }

    private void parseValueParameter() {
        parseValueParameter(false);
    }

    private boolean parseValueParameter(boolean rollbackOnFailure) {
        PsiBuilder.Marker parameter = mark();

        parseModifierListWithShortAnnotations(MODIFIER_LIST, TokenSet.create(IDENTIFIER), TokenSet.create(COMMA, RPAR, COLON));

        if (at(VAR_KEYWORD) || at(VAL_KEYWORD)) {
            advance(); // VAR_KEYWORD | VAL_KEYWORD
        }

        if (!parseFunctionParameterRest() && rollbackOnFailure) {
            parameter.rollbackTo();
            return false;
        }

        parameter.done(VALUE_PARAMETER);
        return true;
    }

    /*
     * functionParameterRest
     *   : parameter ("=" element)?
     *   ;
     */
    private boolean parseFunctionParameterRest() {
        expect(IDENTIFIER, "Parameter name expected", PARAMETER_NAME_RECOVERY_SET);

        if (at(COLON)) {
            advance(); // COLON
            parseTypeRef();
        }
        else {
            error("Parameters must have type annotation");
            return false;
        }

        if (at(EQ)) {
            advance(); // EQ
            myExpressionParsing.parseExpression();
        }
        return true;
    }

    @Override
    protected JetParsing create(SemanticWhitespaceAwarePsiBuilder builder) {
        return createForTopLevel(builder);
    }

    /*package*/ static class TokenDetector implements Consumer<IElementType> {

        private boolean detected = false;
        private final TokenSet tokens;

        public TokenDetector(JetKeywordToken token) {
            this.tokens = TokenSet.create(token);
        }

        public TokenDetector(TokenSet tokens) {
            this.tokens = tokens;
        }

        @Override
        public void consume(IElementType item) {
            if (tokens.contains(item)) {
                detected = true;
            }
        }

        public boolean isDetected() {
            return detected;
        }
    }
}