// This is a generated file. Not intended for manual editing.
package com.goide.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.openapi.diagnostic.Logger;
import static com.goide.GoTypes.*;
import static com.goide.parser.GoParserUtil.*;
import com.intellij.lang.LighterASTNode;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class GoParser implements PsiParser {

  public static final Logger LOG_ = Logger.getInstance("com.goide.parser.GoParser");

  public ASTNode parse(IElementType root_, PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, EXTENDS_SETS_);
    Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    if (root_ == ADD_EXPR) {
      result_ = Expression(builder_, 0, 2);
    }
    else if (root_ == AND_EXPR) {
      result_ = Expression(builder_, 0, 0);
    }
    else if (root_ == ANONYMOUS_FIELD_DEFINITION) {
      result_ = AnonymousFieldDefinition(builder_, 0);
    }
    else if (root_ == ARGUMENT_LIST) {
      result_ = ArgumentList(builder_, 0);
    }
    else if (root_ == ARRAY_OR_SLICE_TYPE) {
      result_ = ArrayOrSliceType(builder_, 0);
    }
    else if (root_ == ASSIGNMENT_STATEMENT) {
      result_ = AssignmentStatement(builder_, 0);
    }
    else if (root_ == BLOCK) {
      result_ = Block(builder_, 0);
    }
    else if (root_ == BREAK_STATEMENT) {
      result_ = BreakStatement(builder_, 0);
    }
    else if (root_ == BUILTIN_ARGS) {
      result_ = BuiltinArgs(builder_, 0);
    }
    else if (root_ == BUILTIN_CALL_EXPR) {
      result_ = Expression(builder_, 0, 6);
    }
    else if (root_ == CALL_EXPR) {
      result_ = Expression(builder_, 0, 6);
    }
    else if (root_ == CHANNEL_TYPE) {
      result_ = ChannelType(builder_, 0);
    }
    else if (root_ == COMM_CASE) {
      result_ = CommCase(builder_, 0);
    }
    else if (root_ == COMM_CLAUSE) {
      result_ = CommClause(builder_, 0);
    }
    else if (root_ == COMPOSITE_LIT) {
      result_ = CompositeLit(builder_, 0);
    }
    else if (root_ == CONDITIONAL_EXPR) {
      result_ = Expression(builder_, 0, 1);
    }
    else if (root_ == CONST_DECLARATION) {
      result_ = ConstDeclaration(builder_, 0);
    }
    else if (root_ == CONST_DEFINITION) {
      result_ = ConstDefinition(builder_, 0);
    }
    else if (root_ == CONST_SPEC) {
      result_ = ConstSpec(builder_, 0);
    }
    else if (root_ == CONTINUE_STATEMENT) {
      result_ = ContinueStatement(builder_, 0);
    }
    else if (root_ == CONVERSION_EXPR) {
      result_ = ConversionExpr(builder_, 0);
    }
    else if (root_ == DEFER_STATEMENT) {
      result_ = DeferStatement(builder_, 0);
    }
    else if (root_ == ELEMENT) {
      result_ = Element(builder_, 0);
    }
    else if (root_ == ELEMENT_INDEX) {
      result_ = ElementIndex(builder_, 0);
    }
    else if (root_ == EXPR_CASE_CLAUSE) {
      result_ = ExprCaseClause(builder_, 0);
    }
    else if (root_ == EXPR_SWITCH_CASE) {
      result_ = ExprSwitchCase(builder_, 0);
    }
    else if (root_ == EXPR_SWITCH_STATEMENT) {
      result_ = ExprSwitchStatement(builder_, 0);
    }
    else if (root_ == EXPRESSION) {
      result_ = Expression(builder_, 0, -1);
    }
    else if (root_ == FALLTHROUGH_STATEMENT) {
      result_ = FallthroughStatement(builder_, 0);
    }
    else if (root_ == FIELD_DECLARATION) {
      result_ = FieldDeclaration(builder_, 0);
    }
    else if (root_ == FIELD_DEFINITION) {
      result_ = FieldDefinition(builder_, 0);
    }
    else if (root_ == FIELD_NAME) {
      result_ = FieldName(builder_, 0);
    }
    else if (root_ == FOR_CLAUSE) {
      result_ = ForClause(builder_, 0);
    }
    else if (root_ == FOR_STATEMENT) {
      result_ = ForStatement(builder_, 0);
    }
    else if (root_ == FUNCTION_DECLARATION) {
      result_ = FunctionDeclaration(builder_, 0);
    }
    else if (root_ == FUNCTION_LIT) {
      result_ = FunctionLit(builder_, 0);
    }
    else if (root_ == FUNCTION_TYPE) {
      result_ = FunctionType(builder_, 0);
    }
    else if (root_ == GO_STATEMENT) {
      result_ = GoStatement(builder_, 0);
    }
    else if (root_ == GOTO_STATEMENT) {
      result_ = GotoStatement(builder_, 0);
    }
    else if (root_ == IF_STATEMENT) {
      result_ = IfStatement(builder_, 0);
    }
    else if (root_ == IMPORT_DECLARATION) {
      result_ = ImportDeclaration(builder_, 0);
    }
    else if (root_ == IMPORT_SPEC) {
      result_ = ImportSpec(builder_, 0);
    }
    else if (root_ == IMPORT_STRING) {
      result_ = ImportString(builder_, 0);
    }
    else if (root_ == INDEX_EXPR) {
      result_ = Expression(builder_, 0, 6);
    }
    else if (root_ == INTERFACE_TYPE) {
      result_ = InterfaceType(builder_, 0);
    }
    else if (root_ == KEY) {
      result_ = Key(builder_, 0);
    }
    else if (root_ == LABEL_DEFINITION) {
      result_ = LabelDefinition(builder_, 0);
    }
    else if (root_ == LABEL_REF) {
      result_ = LabelRef(builder_, 0);
    }
    else if (root_ == LABELED_STATEMENT) {
      result_ = LabeledStatement(builder_, 0);
    }
    else if (root_ == LITERAL) {
      result_ = Literal(builder_, 0);
    }
    else if (root_ == LITERAL_TYPE_EXPR) {
      result_ = LiteralTypeExpr(builder_, 0);
    }
    else if (root_ == LITERAL_VALUE) {
      result_ = LiteralValue(builder_, 0);
    }
    else if (root_ == MAP_TYPE) {
      result_ = MapType(builder_, 0);
    }
    else if (root_ == METHOD_DECLARATION) {
      result_ = MethodDeclaration(builder_, 0);
    }
    else if (root_ == METHOD_EXPR) {
      result_ = MethodExpr(builder_, 0);
    }
    else if (root_ == METHOD_SPEC) {
      result_ = MethodSpec(builder_, 0);
    }
    else if (root_ == MUL_EXPR) {
      result_ = Expression(builder_, 0, 3);
    }
    else if (root_ == OR_EXPR) {
      result_ = Expression(builder_, 0, -1);
    }
    else if (root_ == PACKAGE_CLAUSE) {
      result_ = PackageClause(builder_, 0);
    }
    else if (root_ == PARAM_DEFINITION) {
      result_ = ParamDefinition(builder_, 0);
    }
    else if (root_ == PARAMETER_DECLARATION) {
      result_ = ParameterDeclaration(builder_, 0);
    }
    else if (root_ == PARAMETERS) {
      result_ = Parameters(builder_, 0);
    }
    else if (root_ == PARENTHESES_EXPR) {
      result_ = ParenthesesExpr(builder_, 0);
    }
    else if (root_ == POINTER_TYPE) {
      result_ = PointerType(builder_, 0);
    }
    else if (root_ == RANGE_CLAUSE) {
      result_ = RangeClause(builder_, 0);
    }
    else if (root_ == RECEIVER) {
      result_ = Receiver(builder_, 0);
    }
    else if (root_ == RECEIVER_TYPE) {
      result_ = ReceiverType(builder_, 0);
    }
    else if (root_ == RECV_STATEMENT) {
      result_ = RecvStatement(builder_, 0);
    }
    else if (root_ == REFERENCE_EXPRESSION) {
      result_ = ReferenceExpression(builder_, 0);
    }
    else if (root_ == RESULT) {
      result_ = Result(builder_, 0);
    }
    else if (root_ == RETURN_STATEMENT) {
      result_ = ReturnStatement(builder_, 0);
    }
    else if (root_ == SELECT_STATEMENT) {
      result_ = SelectStatement(builder_, 0);
    }
    else if (root_ == SELECTOR_EXPR) {
      result_ = Expression(builder_, 0, 6);
    }
    else if (root_ == SEND_STATEMENT) {
      result_ = SendStatement(builder_, 0);
    }
    else if (root_ == SHORT_VAR_DECLARATION) {
      result_ = ShortVarDeclaration(builder_, 0);
    }
    else if (root_ == SIGNATURE) {
      result_ = Signature(builder_, 0);
    }
    else if (root_ == SIMPLE_STATEMENT) {
      result_ = SimpleStatement(builder_, 0);
    }
    else if (root_ == STATEMENT) {
      result_ = Statement(builder_, 0);
    }
    else if (root_ == STRUCT_TYPE) {
      result_ = StructType(builder_, 0);
    }
    else if (root_ == SWITCH_START) {
      result_ = SwitchStart(builder_, 0);
    }
    else if (root_ == SWITCH_STATEMENT) {
      result_ = SwitchStatement(builder_, 0);
    }
    else if (root_ == TAG) {
      result_ = Tag(builder_, 0);
    }
    else if (root_ == TYPE) {
      result_ = Type(builder_, 0);
    }
    else if (root_ == TYPE_ASSERTION_EXPR) {
      result_ = Expression(builder_, 0, 6);
    }
    else if (root_ == TYPE_CASE_CLAUSE) {
      result_ = TypeCaseClause(builder_, 0);
    }
    else if (root_ == TYPE_DECLARATION) {
      result_ = TypeDeclaration(builder_, 0);
    }
    else if (root_ == TYPE_LIST) {
      result_ = TypeList(builder_, 0);
    }
    else if (root_ == TYPE_REFERENCE_EXPRESSION) {
      result_ = TypeReferenceExpression(builder_, 0);
    }
    else if (root_ == TYPE_SPEC) {
      result_ = TypeSpec(builder_, 0);
    }
    else if (root_ == TYPE_SWITCH_CASE) {
      result_ = TypeSwitchCase(builder_, 0);
    }
    else if (root_ == TYPE_SWITCH_GUARD) {
      result_ = TypeSwitchGuard(builder_, 0);
    }
    else if (root_ == TYPE_SWITCH_STATEMENT) {
      result_ = TypeSwitchStatement(builder_, 0);
    }
    else if (root_ == UNARY_EXPR) {
      result_ = UnaryExpr(builder_, 0);
    }
    else if (root_ == VALUE) {
      result_ = Value(builder_, 0);
    }
    else if (root_ == VAR_DECLARATION) {
      result_ = VarDeclaration(builder_, 0);
    }
    else if (root_ == VAR_DEFINITION) {
      result_ = VarDefinition(builder_, 0);
    }
    else if (root_ == VAR_SPEC) {
      result_ = VarSpec(builder_, 0);
    }
    else if (root_ == ASSIGN_OP) {
      result_ = assign_op(builder_, 0);
    }
    else {
      result_ = parse_root_(root_, builder_, 0);
    }
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
    return builder_.getTreeBuilt();
  }

  protected boolean parse_root_(final IElementType root_, final PsiBuilder builder_, final int level_) {
    return File(builder_, level_ + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(ADD_EXPR, AND_EXPR, BUILTIN_CALL_EXPR, CALL_EXPR,
      COMPOSITE_LIT, CONDITIONAL_EXPR, CONVERSION_EXPR, EXPRESSION,
      FUNCTION_LIT, INDEX_EXPR, LITERAL, LITERAL_TYPE_EXPR,
      METHOD_EXPR, MUL_EXPR, OR_EXPR, PARENTHESES_EXPR,
      REFERENCE_EXPRESSION, SELECTOR_EXPR, TYPE_ASSERTION_EXPR, UNARY_EXPR),
    create_token_set_(ASSIGNMENT_STATEMENT, BREAK_STATEMENT, CONTINUE_STATEMENT, DEFER_STATEMENT,
      EXPR_SWITCH_STATEMENT, FALLTHROUGH_STATEMENT, FOR_STATEMENT, GOTO_STATEMENT,
      GO_STATEMENT, IF_STATEMENT, LABELED_STATEMENT, RECV_STATEMENT,
      RETURN_STATEMENT, SELECT_STATEMENT, SEND_STATEMENT, SIMPLE_STATEMENT,
      STATEMENT, SWITCH_STATEMENT, TYPE_SWITCH_STATEMENT),
    create_token_set_(EXPR_SWITCH_STATEMENT, SWITCH_STATEMENT, TYPE_SWITCH_STATEMENT),
    create_token_set_(ARRAY_OR_SLICE_TYPE, CHANNEL_TYPE, FUNCTION_TYPE, INTERFACE_TYPE,
      MAP_TYPE, POINTER_TYPE, RECEIVER_TYPE, STRUCT_TYPE,
      TYPE, TYPE_LIST),
    create_token_set_(SHORT_VAR_DECLARATION, VAR_SPEC),
  };

  /* ********************************************************** */
  // '*'? TypeName
  public static boolean AnonymousFieldDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnonymousFieldDefinition")) return false;
    if (!nextTokenIs(builder_, "<anonymous field definition>", MUL, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<anonymous field definition>");
    result_ = AnonymousFieldDefinition_0(builder_, level_ + 1);
    result_ = result_ && TypeName(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, ANONYMOUS_FIELD_DEFINITION, result_, false, null);
    return result_;
  }

  // '*'?
  private static boolean AnonymousFieldDefinition_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AnonymousFieldDefinition_0")) return false;
    consumeToken(builder_, MUL);
    return true;
  }

  /* ********************************************************** */
  // '(' [ ExpressionList '...'? ','? ] ')'
  public static boolean ArgumentList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ArgumentList")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && ArgumentList_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, ARGUMENT_LIST, result_);
    return result_;
  }

  // [ ExpressionList '...'? ','? ]
  private static boolean ArgumentList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ArgumentList_1")) return false;
    ArgumentList_1_0(builder_, level_ + 1);
    return true;
  }

  // ExpressionList '...'? ','?
  private static boolean ArgumentList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ArgumentList_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ExpressionList(builder_, level_ + 1);
    result_ = result_ && ArgumentList_1_0_1(builder_, level_ + 1);
    result_ = result_ && ArgumentList_1_0_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '...'?
  private static boolean ArgumentList_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ArgumentList_1_0_1")) return false;
    consumeToken(builder_, TRIPLE_DOT);
    return true;
  }

  // ','?
  private static boolean ArgumentList_1_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ArgumentList_1_0_2")) return false;
    consumeToken(builder_, COMMA);
    return true;
  }

  /* ********************************************************** */
  // '[' ('...'|Expression?) ']' Type
  public static boolean ArrayOrSliceType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ArrayOrSliceType")) return false;
    if (!nextTokenIs(builder_, LBRACK)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, LBRACK);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, ArrayOrSliceType_1(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, RBRACK)) && result_;
    result_ = pinned_ && Type(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, ARRAY_OR_SLICE_TYPE, result_, pinned_, null);
    return result_ || pinned_;
  }

  // '...'|Expression?
  private static boolean ArrayOrSliceType_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ArrayOrSliceType_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, TRIPLE_DOT);
    if (!result_) result_ = ArrayOrSliceType_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // Expression?
  private static boolean ArrayOrSliceType_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ArrayOrSliceType_1_1")) return false;
    Expression(builder_, level_ + 1, -1);
    return true;
  }

  /* ********************************************************** */
  // ExpressionList assign_op ExpressionList
  public static boolean AssignmentStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AssignmentStatement")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<assignment statement>");
    result_ = ExpressionList(builder_, level_ + 1);
    result_ = result_ && assign_op(builder_, level_ + 1);
    pinned_ = result_; // pin = 2
    result_ = result_ && ExpressionList(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, ASSIGNMENT_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // '{' Statements? '}'
  public static boolean Block(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Block")) return false;
    if (!nextTokenIs(builder_, LBRACE)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, LBRACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, Block_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, BLOCK, result_, pinned_, null);
    return result_ || pinned_;
  }

  // Statements?
  private static boolean Block_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Block_1")) return false;
    Statements(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // break LabelRef?
  public static boolean BreakStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BreakStatement")) return false;
    if (!nextTokenIs(builder_, BREAK)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, BREAK);
    pinned_ = result_; // pin = 1
    result_ = result_ && BreakStatement_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, BREAK_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // LabelRef?
  private static boolean BreakStatement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BreakStatement_1")) return false;
    LabelRef(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // Type [ ',' ExpressionList '...'? ] | ExpressionList '...'?
  public static boolean BuiltinArgs(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BuiltinArgs")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<builtin args>");
    result_ = BuiltinArgs_0(builder_, level_ + 1);
    if (!result_) result_ = BuiltinArgs_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, BUILTIN_ARGS, result_, false, null);
    return result_;
  }

  // Type [ ',' ExpressionList '...'? ]
  private static boolean BuiltinArgs_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BuiltinArgs_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = Type(builder_, level_ + 1);
    result_ = result_ && BuiltinArgs_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [ ',' ExpressionList '...'? ]
  private static boolean BuiltinArgs_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BuiltinArgs_0_1")) return false;
    BuiltinArgs_0_1_0(builder_, level_ + 1);
    return true;
  }

  // ',' ExpressionList '...'?
  private static boolean BuiltinArgs_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BuiltinArgs_0_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && ExpressionList(builder_, level_ + 1);
    result_ = result_ && BuiltinArgs_0_1_0_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '...'?
  private static boolean BuiltinArgs_0_1_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BuiltinArgs_0_1_0_2")) return false;
    consumeToken(builder_, TRIPLE_DOT);
    return true;
  }

  // ExpressionList '...'?
  private static boolean BuiltinArgs_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BuiltinArgs_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ExpressionList(builder_, level_ + 1);
    result_ = result_ && BuiltinArgs_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '...'?
  private static boolean BuiltinArgs_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BuiltinArgs_1_1")) return false;
    consumeToken(builder_, TRIPLE_DOT);
    return true;
  }

  /* ********************************************************** */
  // ( chan [ '<-' ] | '<-' chan ) Type
  public static boolean ChannelType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ChannelType")) return false;
    if (!nextTokenIs(builder_, "<channel type>", SEND_CHANNEL, CHAN)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<channel type>");
    result_ = ChannelType_0(builder_, level_ + 1);
    result_ = result_ && Type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, CHANNEL_TYPE, result_, false, null);
    return result_;
  }

  // chan [ '<-' ] | '<-' chan
  private static boolean ChannelType_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ChannelType_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ChannelType_0_0(builder_, level_ + 1);
    if (!result_) result_ = ChannelType_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // chan [ '<-' ]
  private static boolean ChannelType_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ChannelType_0_0")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, CHAN);
    pinned_ = result_; // pin = chan
    result_ = result_ && ChannelType_0_0_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [ '<-' ]
  private static boolean ChannelType_0_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ChannelType_0_0_1")) return false;
    consumeToken(builder_, SEND_CHANNEL);
    return true;
  }

  // '<-' chan
  private static boolean ChannelType_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ChannelType_0_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEND_CHANNEL);
    result_ = result_ && consumeToken(builder_, CHAN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // case ( SendStatement | RecvStatement ) | default
  public static boolean CommCase(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CommCase")) return false;
    if (!nextTokenIs(builder_, "<comm case>", CASE, DEFAULT)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<comm case>");
    result_ = CommCase_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, DEFAULT);
    exit_section_(builder_, level_, marker_, COMM_CASE, result_, false, null);
    return result_;
  }

  // case ( SendStatement | RecvStatement )
  private static boolean CommCase_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CommCase_0")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, CASE);
    pinned_ = result_; // pin = 1
    result_ = result_ && CommCase_0_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // SendStatement | RecvStatement
  private static boolean CommCase_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CommCase_0_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = SendStatement(builder_, level_ + 1);
    if (!result_) result_ = RecvStatement(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // CommCase ':' Statements?
  public static boolean CommClause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CommClause")) return false;
    if (!nextTokenIs(builder_, "<comm clause>", CASE, DEFAULT)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<comm clause>");
    result_ = CommCase(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, consumeToken(builder_, COLON));
    result_ = pinned_ && CommClause_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, COMM_CLAUSE, result_, pinned_, null);
    return result_ || pinned_;
  }

  // Statements?
  private static boolean CommClause_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CommClause_2")) return false;
    Statements(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // <<enterMode "NO_EMPTY_LITERAL">> SimpleStatementOpt Expression? <<exitMode "NO_EMPTY_LITERAL">>
  static boolean Condition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Condition")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = enterMode(builder_, level_ + 1, "NO_EMPTY_LITERAL");
    result_ = result_ && SimpleStatementOpt(builder_, level_ + 1);
    result_ = result_ && Condition_2(builder_, level_ + 1);
    result_ = result_ && exitMode(builder_, level_ + 1, "NO_EMPTY_LITERAL");
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // Expression?
  private static boolean Condition_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Condition_2")) return false;
    Expression(builder_, level_ + 1, -1);
    return true;
  }

  /* ********************************************************** */
  // const ( ConstSpec | '(' ConstSpecs? ')' )
  public static boolean ConstDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstDeclaration")) return false;
    if (!nextTokenIs(builder_, CONST)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, CONST);
    pinned_ = result_; // pin = 1
    result_ = result_ && ConstDeclaration_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, CONST_DECLARATION, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ConstSpec | '(' ConstSpecs? ')'
  private static boolean ConstDeclaration_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstDeclaration_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ConstSpec(builder_, level_ + 1);
    if (!result_) result_ = ConstDeclaration_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '(' ConstSpecs? ')'
  private static boolean ConstDeclaration_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstDeclaration_1_1")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, LPAREN);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, ConstDeclaration_1_1_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RPAREN) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ConstSpecs?
  private static boolean ConstDeclaration_1_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstDeclaration_1_1_1")) return false;
    ConstSpecs(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // identifier
  public static boolean ConstDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstDefinition")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, CONST_DEFINITION, result_);
    return result_;
  }

  /* ********************************************************** */
  // ConstDefinition ( ',' ConstDefinition )*
  static boolean ConstDefinitionList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstDefinitionList")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = ConstDefinition(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && ConstDefinitionList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ( ',' ConstDefinition )*
  private static boolean ConstDefinitionList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstDefinitionList_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!ConstDefinitionList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ConstDefinitionList_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' ConstDefinition
  private static boolean ConstDefinitionList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstDefinitionList_1_0")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, COMMA);
    pinned_ = result_; // pin = 1
    result_ = result_ && ConstDefinition(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // ConstDefinitionList [ ('=' ExpressionList | Type '=' ExpressionList) ]
  public static boolean ConstSpec(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstSpec")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = ConstDefinitionList(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && ConstSpec_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, CONST_SPEC, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [ ('=' ExpressionList | Type '=' ExpressionList) ]
  private static boolean ConstSpec_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstSpec_1")) return false;
    ConstSpec_1_0(builder_, level_ + 1);
    return true;
  }

  // '=' ExpressionList | Type '=' ExpressionList
  private static boolean ConstSpec_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstSpec_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ConstSpec_1_0_0(builder_, level_ + 1);
    if (!result_) result_ = ConstSpec_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '=' ExpressionList
  private static boolean ConstSpec_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstSpec_1_0_0")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, ASSIGN);
    pinned_ = result_; // pin = 1
    result_ = result_ && ExpressionList(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // Type '=' ExpressionList
  private static boolean ConstSpec_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstSpec_1_0_1")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = Type(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, consumeToken(builder_, ASSIGN));
    result_ = pinned_ && ExpressionList(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // ConstSpec (semi ConstSpec)* semi?
  static boolean ConstSpecs(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstSpecs")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = ConstSpec(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, ConstSpecs_1(builder_, level_ + 1));
    result_ = pinned_ && ConstSpecs_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (semi ConstSpec)*
  private static boolean ConstSpecs_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstSpecs_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!ConstSpecs_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ConstSpecs_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // semi ConstSpec
  private static boolean ConstSpecs_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstSpecs_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = semi(builder_, level_ + 1);
    result_ = result_ && ConstSpec(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // semi?
  private static boolean ConstSpecs_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstSpecs_2")) return false;
    semi(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // continue LabelRef?
  public static boolean ContinueStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ContinueStatement")) return false;
    if (!nextTokenIs(builder_, CONTINUE)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, CONTINUE);
    pinned_ = result_; // pin = 1
    result_ = result_ && ContinueStatement_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, CONTINUE_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // LabelRef?
  private static boolean ContinueStatement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ContinueStatement_1")) return false;
    LabelRef(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // defer Expression
  public static boolean DeferStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "DeferStatement")) return false;
    if (!nextTokenIs(builder_, DEFER)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, DEFER);
    pinned_ = result_; // pin = 1
    result_ = result_ && Expression(builder_, level_ + 1, -1);
    exit_section_(builder_, level_, marker_, DEFER_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // [ Key ':' ] Value
  public static boolean Element(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Element")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<element>");
    result_ = Element_0(builder_, level_ + 1);
    result_ = result_ && Value(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, ELEMENT, result_, false, null);
    return result_;
  }

  // [ Key ':' ]
  private static boolean Element_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Element_0")) return false;
    Element_0_0(builder_, level_ + 1);
    return true;
  }

  // Key ':'
  private static boolean Element_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Element_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = Key(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // Expression
  public static boolean ElementIndex(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ElementIndex")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<element index>");
    result_ = Expression(builder_, level_ + 1, -1);
    exit_section_(builder_, level_, marker_, ELEMENT_INDEX, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // Element ( ',' Element? )*
  static boolean ElementList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ElementList")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = Element(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && ElementList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ( ',' Element? )*
  private static boolean ElementList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ElementList_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!ElementList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ElementList_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' Element?
  private static boolean ElementList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ElementList_1_0")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, COMMA);
    pinned_ = result_; // pin = 1
    result_ = result_ && ElementList_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // Element?
  private static boolean ElementList_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ElementList_1_0_1")) return false;
    Element(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // ExprSwitchCase ':' Statements?
  public static boolean ExprCaseClause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExprCaseClause")) return false;
    if (!nextTokenIs(builder_, "<expr case clause>", CASE, DEFAULT)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<expr case clause>");
    result_ = ExprSwitchCase(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && ExprCaseClause_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, EXPR_CASE_CLAUSE, result_, false, null);
    return result_;
  }

  // Statements?
  private static boolean ExprCaseClause_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExprCaseClause_2")) return false;
    Statements(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // case ExpressionList | default
  public static boolean ExprSwitchCase(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExprSwitchCase")) return false;
    if (!nextTokenIs(builder_, "<expr switch case>", CASE, DEFAULT)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<expr switch case>");
    result_ = ExprSwitchCase_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, DEFAULT);
    exit_section_(builder_, level_, marker_, EXPR_SWITCH_CASE, result_, false, null);
    return result_;
  }

  // case ExpressionList
  private static boolean ExprSwitchCase_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExprSwitchCase_0")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, CASE);
    pinned_ = result_; // pin = 1
    result_ = result_ && ExpressionList(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // Condition '{' ( ExprCaseClause )* '}'
  public static boolean ExprSwitchStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExprSwitchStatement")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, "<expr switch statement>");
    result_ = Condition(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LBRACE);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, ExprSwitchStatement_2(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, EXPR_SWITCH_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ( ExprCaseClause )*
  private static boolean ExprSwitchStatement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExprSwitchStatement_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!ExprSwitchStatement_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ExprSwitchStatement_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ( ExprCaseClause )
  private static boolean ExprSwitchStatement_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExprSwitchStatement_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ExprCaseClause(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // Expression ( ',' Expression )*
  static boolean ExpressionList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExpressionList")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = Expression(builder_, level_ + 1, -1);
    pinned_ = result_; // pin = 1
    result_ = result_ && ExpressionList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ( ',' Expression )*
  private static boolean ExpressionList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExpressionList_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!ExpressionList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ExpressionList_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' Expression
  private static boolean ExpressionList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExpressionList_1_0")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, COMMA);
    pinned_ = result_; // pin = 1
    result_ = result_ && Expression(builder_, level_ + 1, -1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // fallthrough
  public static boolean FallthroughStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FallthroughStatement")) return false;
    if (!nextTokenIs(builder_, FALLTHROUGH)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, FALLTHROUGH);
    exit_section_(builder_, marker_, FALLTHROUGH_STATEMENT, result_);
    return result_;
  }

  /* ********************************************************** */
  // (FieldDefinitionList Type | AnonymousFieldDefinition) Tag?
  public static boolean FieldDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldDeclaration")) return false;
    if (!nextTokenIs(builder_, "<field declaration>", MUL, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<field declaration>");
    result_ = FieldDeclaration_0(builder_, level_ + 1);
    result_ = result_ && FieldDeclaration_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, FIELD_DECLARATION, result_, false, null);
    return result_;
  }

  // FieldDefinitionList Type | AnonymousFieldDefinition
  private static boolean FieldDeclaration_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldDeclaration_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = FieldDeclaration_0_0(builder_, level_ + 1);
    if (!result_) result_ = AnonymousFieldDefinition(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // FieldDefinitionList Type
  private static boolean FieldDeclaration_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldDeclaration_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = FieldDefinitionList(builder_, level_ + 1);
    result_ = result_ && Type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // Tag?
  private static boolean FieldDeclaration_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldDeclaration_1")) return false;
    Tag(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // identifier
  public static boolean FieldDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldDefinition")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, FIELD_DEFINITION, result_);
    return result_;
  }

  /* ********************************************************** */
  // FieldDefinition (',' FieldDefinition)*
  static boolean FieldDefinitionList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldDefinitionList")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = FieldDefinition(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && FieldDefinitionList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (',' FieldDefinition)*
  private static boolean FieldDefinitionList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldDefinitionList_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!FieldDefinitionList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "FieldDefinitionList_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' FieldDefinition
  private static boolean FieldDefinitionList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldDefinitionList_1_0")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, COMMA);
    pinned_ = result_; // pin = 1
    result_ = result_ && FieldDefinition(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // identifier
  public static boolean FieldName(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldName")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, FIELD_NAME, result_);
    return result_;
  }

  /* ********************************************************** */
  // FieldDeclaration (semi FieldDeclaration)* semi?
  static boolean Fields(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Fields")) return false;
    if (!nextTokenIs(builder_, "", MUL, IDENTIFIER)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = FieldDeclaration(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, Fields_1(builder_, level_ + 1));
    result_ = pinned_ && Fields_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (semi FieldDeclaration)*
  private static boolean Fields_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Fields_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!Fields_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "Fields_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // semi FieldDeclaration
  private static boolean Fields_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Fields_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = semi(builder_, level_ + 1);
    result_ = result_ && FieldDeclaration(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // semi?
  private static boolean Fields_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Fields_2")) return false;
    semi(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // PackageClause semi (ImportDeclaration semi)* TopLevelDeclaration*
  static boolean File(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "File")) return false;
    if (!nextTokenIs(builder_, PACKAGE)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = PackageClause(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, semi(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, File_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && File_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (ImportDeclaration semi)*
  private static boolean File_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "File_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!File_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "File_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ImportDeclaration semi
  private static boolean File_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "File_2_0")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = ImportDeclaration(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && semi(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // TopLevelDeclaration*
  private static boolean File_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "File_3")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!TopLevelDeclaration(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "File_3", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  /* ********************************************************** */
  // SimpleStatement? ';' Expression? ';' SimpleStatement?
  public static boolean ForClause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForClause")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<for clause>");
    result_ = ForClause_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    result_ = result_ && ForClause_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    result_ = result_ && ForClause_4(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, FOR_CLAUSE, result_, false, null);
    return result_;
  }

  // SimpleStatement?
  private static boolean ForClause_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForClause_0")) return false;
    SimpleStatement(builder_, level_ + 1);
    return true;
  }

  // Expression?
  private static boolean ForClause_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForClause_2")) return false;
    Expression(builder_, level_ + 1, -1);
    return true;
  }

  // SimpleStatement?
  private static boolean ForClause_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForClause_4")) return false;
    SimpleStatement(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // for ([ForClause | RangeClause] Block | <<enterMode "NO_EMPTY_LITERAL">> Expression <<exitMode "NO_EMPTY_LITERAL">> Block)
  public static boolean ForStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForStatement")) return false;
    if (!nextTokenIs(builder_, FOR)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, FOR);
    pinned_ = result_; // pin = 1
    result_ = result_ && ForStatement_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, FOR_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [ForClause | RangeClause] Block | <<enterMode "NO_EMPTY_LITERAL">> Expression <<exitMode "NO_EMPTY_LITERAL">> Block
  private static boolean ForStatement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForStatement_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ForStatement_1_0(builder_, level_ + 1);
    if (!result_) result_ = ForStatement_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [ForClause | RangeClause] Block
  private static boolean ForStatement_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForStatement_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ForStatement_1_0_0(builder_, level_ + 1);
    result_ = result_ && Block(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [ForClause | RangeClause]
  private static boolean ForStatement_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForStatement_1_0_0")) return false;
    ForStatement_1_0_0_0(builder_, level_ + 1);
    return true;
  }

  // ForClause | RangeClause
  private static boolean ForStatement_1_0_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForStatement_1_0_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ForClause(builder_, level_ + 1);
    if (!result_) result_ = RangeClause(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // <<enterMode "NO_EMPTY_LITERAL">> Expression <<exitMode "NO_EMPTY_LITERAL">> Block
  private static boolean ForStatement_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForStatement_1_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = enterMode(builder_, level_ + 1, "NO_EMPTY_LITERAL");
    result_ = result_ && Expression(builder_, level_ + 1, -1);
    result_ = result_ && exitMode(builder_, level_ + 1, "NO_EMPTY_LITERAL");
    result_ = result_ && Block(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // func identifier Signature Block?
  public static boolean FunctionDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDeclaration")) return false;
    if (!nextTokenIs(builder_, FUNC)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokens(builder_, 2, FUNC, IDENTIFIER);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, Signature(builder_, level_ + 1));
    result_ = pinned_ && FunctionDeclaration_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, FUNCTION_DECLARATION, result_, pinned_, null);
    return result_ || pinned_;
  }

  // Block?
  private static boolean FunctionDeclaration_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDeclaration_3")) return false;
    Block(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // func Signature
  public static boolean FunctionType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionType")) return false;
    if (!nextTokenIs(builder_, FUNC)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, FUNC);
    pinned_ = result_; // pin = 1
    result_ = result_ && Signature(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, FUNCTION_TYPE, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // go Expression
  public static boolean GoStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "GoStatement")) return false;
    if (!nextTokenIs(builder_, GO)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, GO);
    pinned_ = result_; // pin = 1
    result_ = result_ && Expression(builder_, level_ + 1, -1);
    exit_section_(builder_, level_, marker_, GO_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // goto LabelRef
  public static boolean GotoStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "GotoStatement")) return false;
    if (!nextTokenIs(builder_, GOTO)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, GOTO);
    pinned_ = result_; // pin = 1
    result_ = result_ && LabelRef(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, GOTO_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // identifier ( ',' identifier )*
  static boolean IdentifierList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IdentifierList")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, IDENTIFIER);
    pinned_ = result_; // pin = 1
    result_ = result_ && IdentifierList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ( ',' identifier )*
  private static boolean IdentifierList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IdentifierList_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!IdentifierList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "IdentifierList_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' identifier
  private static boolean IdentifierList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IdentifierList_1_0")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, COMMA);
    pinned_ = result_; // pin = 1
    result_ = result_ && consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // ParamDefinition &(!('.' | ')')) (',' ParamDefinition)*
  static boolean IdentifierListNoPin(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IdentifierListNoPin")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ParamDefinition(builder_, level_ + 1);
    result_ = result_ && IdentifierListNoPin_1(builder_, level_ + 1);
    result_ = result_ && IdentifierListNoPin_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // &(!('.' | ')'))
  private static boolean IdentifierListNoPin_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IdentifierListNoPin_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _AND_, null);
    result_ = IdentifierListNoPin_1_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // !('.' | ')')
  private static boolean IdentifierListNoPin_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IdentifierListNoPin_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !IdentifierListNoPin_1_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // '.' | ')'
  private static boolean IdentifierListNoPin_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IdentifierListNoPin_1_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DOT);
    if (!result_) result_ = consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (',' ParamDefinition)*
  private static boolean IdentifierListNoPin_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IdentifierListNoPin_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!IdentifierListNoPin_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "IdentifierListNoPin_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' ParamDefinition
  private static boolean IdentifierListNoPin_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IdentifierListNoPin_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && ParamDefinition(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // if Condition Block [ else ( IfStatement | Block ) ]
  public static boolean IfStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IfStatement")) return false;
    if (!nextTokenIs(builder_, IF)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, IF);
    pinned_ = result_; // pin = if|else
    result_ = result_ && report_error_(builder_, Condition(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, Block(builder_, level_ + 1)) && result_;
    result_ = pinned_ && IfStatement_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, IF_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [ else ( IfStatement | Block ) ]
  private static boolean IfStatement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IfStatement_3")) return false;
    IfStatement_3_0(builder_, level_ + 1);
    return true;
  }

  // else ( IfStatement | Block )
  private static boolean IfStatement_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IfStatement_3_0")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, ELSE);
    pinned_ = result_; // pin = if|else
    result_ = result_ && IfStatement_3_0_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // IfStatement | Block
  private static boolean IfStatement_3_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IfStatement_3_0_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = IfStatement(builder_, level_ + 1);
    if (!result_) result_ = Block(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // import ( ImportSpec | '(' ImportSpecs? ')' )
  public static boolean ImportDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportDeclaration")) return false;
    if (!nextTokenIs(builder_, IMPORT)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, IMPORT);
    pinned_ = result_; // pin = 1
    result_ = result_ && ImportDeclaration_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, IMPORT_DECLARATION, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ImportSpec | '(' ImportSpecs? ')'
  private static boolean ImportDeclaration_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportDeclaration_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ImportSpec(builder_, level_ + 1);
    if (!result_) result_ = ImportDeclaration_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '(' ImportSpecs? ')'
  private static boolean ImportDeclaration_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportDeclaration_1_1")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, LPAREN);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, ImportDeclaration_1_1_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RPAREN) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ImportSpecs?
  private static boolean ImportDeclaration_1_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportDeclaration_1_1_1")) return false;
    ImportSpecs(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // [ '.' | identifier ] ImportString
  public static boolean ImportSpec(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportSpec")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<import spec>");
    result_ = ImportSpec_0(builder_, level_ + 1);
    result_ = result_ && ImportString(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, IMPORT_SPEC, result_, false, null);
    return result_;
  }

  // [ '.' | identifier ]
  private static boolean ImportSpec_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportSpec_0")) return false;
    ImportSpec_0_0(builder_, level_ + 1);
    return true;
  }

  // '.' | identifier
  private static boolean ImportSpec_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportSpec_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DOT);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ImportSpec (semi ImportSpec)* semi?
  static boolean ImportSpecs(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportSpecs")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = ImportSpec(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, ImportSpecs_1(builder_, level_ + 1));
    result_ = pinned_ && ImportSpecs_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (semi ImportSpec)*
  private static boolean ImportSpecs_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportSpecs_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!ImportSpecs_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ImportSpecs_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // semi ImportSpec
  private static boolean ImportSpecs_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportSpecs_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = semi(builder_, level_ + 1);
    result_ = result_ && ImportSpec(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // semi?
  private static boolean ImportSpecs_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportSpecs_2")) return false;
    semi(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // string
  public static boolean ImportString(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ImportString")) return false;
    if (!nextTokenIs(builder_, STRING)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, STRING);
    exit_section_(builder_, marker_, IMPORT_STRING, result_);
    return result_;
  }

  /* ********************************************************** */
  // (Expression? ':' Expression ':' Expression) | (Expression? ':' Expression?) | Expression
  static boolean IndexExprBody(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IndexExprBody")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = IndexExprBody_0(builder_, level_ + 1);
    if (!result_) result_ = IndexExprBody_1(builder_, level_ + 1);
    if (!result_) result_ = Expression(builder_, level_ + 1, -1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // Expression? ':' Expression ':' Expression
  private static boolean IndexExprBody_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IndexExprBody_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = IndexExprBody_0_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && Expression(builder_, level_ + 1, -1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && Expression(builder_, level_ + 1, -1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // Expression?
  private static boolean IndexExprBody_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IndexExprBody_0_0")) return false;
    Expression(builder_, level_ + 1, -1);
    return true;
  }

  // Expression? ':' Expression?
  private static boolean IndexExprBody_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IndexExprBody_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = IndexExprBody_1_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && IndexExprBody_1_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // Expression?
  private static boolean IndexExprBody_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IndexExprBody_1_0")) return false;
    Expression(builder_, level_ + 1, -1);
    return true;
  }

  // Expression?
  private static boolean IndexExprBody_1_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IndexExprBody_1_2")) return false;
    Expression(builder_, level_ + 1, -1);
    return true;
  }

  /* ********************************************************** */
  // interface '{' MethodSpecs? '}'
  public static boolean InterfaceType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InterfaceType")) return false;
    if (!nextTokenIs(builder_, INTERFACE)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, INTERFACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, consumeToken(builder_, LBRACE));
    result_ = pinned_ && report_error_(builder_, InterfaceType_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, INTERFACE_TYPE, result_, pinned_, null);
    return result_ || pinned_;
  }

  // MethodSpecs?
  private static boolean InterfaceType_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InterfaceType_2")) return false;
    MethodSpecs(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // FieldName | ElementIndex
  public static boolean Key(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Key")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<key>");
    result_ = FieldName(builder_, level_ + 1);
    if (!result_) result_ = ElementIndex(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, KEY, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // identifier
  public static boolean LabelDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LabelDefinition")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, LABEL_DEFINITION, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier
  public static boolean LabelRef(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LabelRef")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, LABEL_REF, result_);
    return result_;
  }

  /* ********************************************************** */
  // LabelDefinition ':' Statement
  public static boolean LabeledStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LabeledStatement")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = LabelDefinition(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    pinned_ = result_; // pin = 2
    result_ = result_ && Statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, LABELED_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // <<isModeOff "NO_EMPTY_LITERAL">> '{' '}' | '{' ElementList '}'
  public static boolean LiteralValue(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LiteralValue")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<literal value>");
    result_ = LiteralValue_0(builder_, level_ + 1);
    if (!result_) result_ = LiteralValue_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, LITERAL_VALUE, result_, false, null);
    return result_;
  }

  // <<isModeOff "NO_EMPTY_LITERAL">> '{' '}'
  private static boolean LiteralValue_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LiteralValue_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = isModeOff(builder_, level_ + 1, "NO_EMPTY_LITERAL");
    result_ = result_ && consumeToken(builder_, LBRACE);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '{' ElementList '}'
  private static boolean LiteralValue_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LiteralValue_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACE);
    result_ = result_ && ElementList(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // map '[' Type ']' Type
  public static boolean MapType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MapType")) return false;
    if (!nextTokenIs(builder_, MAP)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, MAP);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, consumeToken(builder_, LBRACK));
    result_ = pinned_ && report_error_(builder_, Type(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, RBRACK)) && result_;
    result_ = pinned_ && Type(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, MAP_TYPE, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // func Receiver identifier Signature Block?
  public static boolean MethodDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MethodDeclaration")) return false;
    if (!nextTokenIs(builder_, FUNC)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, FUNC);
    result_ = result_ && Receiver(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, IDENTIFIER);
    pinned_ = result_; // pin = 3
    result_ = result_ && report_error_(builder_, Signature(builder_, level_ + 1));
    result_ = pinned_ && MethodDeclaration_4(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, METHOD_DECLARATION, result_, pinned_, null);
    return result_ || pinned_;
  }

  // Block?
  private static boolean MethodDeclaration_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MethodDeclaration_4")) return false;
    Block(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // TypeName &(!'(') | identifier Signature
  public static boolean MethodSpec(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MethodSpec")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = MethodSpec_0(builder_, level_ + 1);
    if (!result_) result_ = MethodSpec_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, METHOD_SPEC, result_);
    return result_;
  }

  // TypeName &(!'(')
  private static boolean MethodSpec_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MethodSpec_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeName(builder_, level_ + 1);
    result_ = result_ && MethodSpec_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // &(!'(')
  private static boolean MethodSpec_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MethodSpec_0_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _AND_, null);
    result_ = MethodSpec_0_1_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // !'('
  private static boolean MethodSpec_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MethodSpec_0_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !consumeToken(builder_, LPAREN);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // identifier Signature
  private static boolean MethodSpec_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MethodSpec_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    result_ = result_ && Signature(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // MethodSpec (semi MethodSpec)* semi?
  static boolean MethodSpecs(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MethodSpecs")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = MethodSpec(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, MethodSpecs_1(builder_, level_ + 1));
    result_ = pinned_ && MethodSpecs_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (semi MethodSpec)*
  private static boolean MethodSpecs_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MethodSpecs_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!MethodSpecs_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "MethodSpecs_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // semi MethodSpec
  private static boolean MethodSpecs_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MethodSpecs_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = semi(builder_, level_ + 1);
    result_ = result_ && MethodSpec(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // semi?
  private static boolean MethodSpecs_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MethodSpecs_2")) return false;
    semi(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // ConstDeclaration
  //   | TypeDeclaration
  //   | VarDeclaration
  //   | FunctionDeclaration
  //   | MethodDeclaration
  static boolean OneOfDeclarations(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "OneOfDeclarations")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ConstDeclaration(builder_, level_ + 1);
    if (!result_) result_ = TypeDeclaration(builder_, level_ + 1);
    if (!result_) result_ = VarDeclaration(builder_, level_ + 1);
    if (!result_) result_ = FunctionDeclaration(builder_, level_ + 1);
    if (!result_) result_ = MethodDeclaration(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // package identifier
  public static boolean PackageClause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PackageClause")) return false;
    if (!nextTokenIs(builder_, PACKAGE)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokens(builder_, 1, PACKAGE, IDENTIFIER);
    pinned_ = result_; // pin = 1
    exit_section_(builder_, level_, marker_, PACKAGE_CLAUSE, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // identifier
  public static boolean ParamDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParamDefinition")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, PARAM_DEFINITION, result_);
    return result_;
  }

  /* ********************************************************** */
  // IdentifierListNoPin? '...'? Type
  public static boolean ParameterDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterDeclaration")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<parameter declaration>");
    result_ = ParameterDeclaration_0(builder_, level_ + 1);
    result_ = result_ && ParameterDeclaration_1(builder_, level_ + 1);
    result_ = result_ && Type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, PARAMETER_DECLARATION, result_, false, null);
    return result_;
  }

  // IdentifierListNoPin?
  private static boolean ParameterDeclaration_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterDeclaration_0")) return false;
    IdentifierListNoPin(builder_, level_ + 1);
    return true;
  }

  // '...'?
  private static boolean ParameterDeclaration_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterDeclaration_1")) return false;
    consumeToken(builder_, TRIPLE_DOT);
    return true;
  }

  /* ********************************************************** */
  // ParameterDeclaration ( ',' ParameterDeclaration )*
  static boolean ParameterList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterList")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = ParameterDeclaration(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && ParameterList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ( ',' ParameterDeclaration )*
  private static boolean ParameterList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterList_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!ParameterList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ParameterList_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' ParameterDeclaration
  private static boolean ParameterList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterList_1_0")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, COMMA);
    pinned_ = result_; // pin = 1
    result_ = result_ && ParameterDeclaration(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // '(' [ (ParameterList ','?| TypeListNoPin) ] ')'
  public static boolean Parameters(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Parameters")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, LPAREN);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, Parameters_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RPAREN) && result_;
    exit_section_(builder_, level_, marker_, PARAMETERS, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [ (ParameterList ','?| TypeListNoPin) ]
  private static boolean Parameters_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Parameters_1")) return false;
    Parameters_1_0(builder_, level_ + 1);
    return true;
  }

  // ParameterList ','?| TypeListNoPin
  private static boolean Parameters_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Parameters_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = Parameters_1_0_0(builder_, level_ + 1);
    if (!result_) result_ = TypeListNoPin(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ParameterList ','?
  private static boolean Parameters_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Parameters_1_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ParameterList(builder_, level_ + 1);
    result_ = result_ && Parameters_1_0_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ','?
  private static boolean Parameters_1_0_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Parameters_1_0_0_1")) return false;
    consumeToken(builder_, COMMA);
    return true;
  }

  /* ********************************************************** */
  // '*' Type
  public static boolean PointerType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PointerType")) return false;
    if (!nextTokenIs(builder_, MUL)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, MUL);
    pinned_ = result_; // pin = 1
    result_ = result_ && Type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, POINTER_TYPE, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // '.' identifier
  public static boolean QualifiedReferenceExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "QualifiedReferenceExpression")) return false;
    if (!nextTokenIs(builder_, DOT)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, null);
    result_ = consumeToken(builder_, DOT);
    result_ = result_ && consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, level_, marker_, REFERENCE_EXPRESSION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // '.' identifier
  public static boolean QualifiedTypeReferenceExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "QualifiedTypeReferenceExpression")) return false;
    if (!nextTokenIs(builder_, DOT)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, null);
    result_ = consumeToken(builder_, DOT);
    result_ = result_ && consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, level_, marker_, TYPE_REFERENCE_EXPRESSION, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ( ExpressionList '=' | VarDefinitionList ':=' ) range Expression
  public static boolean RangeClause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RangeClause")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<range clause>");
    result_ = RangeClause_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RANGE);
    pinned_ = result_; // pin = 2
    result_ = result_ && Expression(builder_, level_ + 1, -1);
    exit_section_(builder_, level_, marker_, RANGE_CLAUSE, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ExpressionList '=' | VarDefinitionList ':='
  private static boolean RangeClause_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RangeClause_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = RangeClause_0_0(builder_, level_ + 1);
    if (!result_) result_ = RangeClause_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ExpressionList '='
  private static boolean RangeClause_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RangeClause_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ExpressionList(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ASSIGN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // VarDefinitionList ':='
  private static boolean RangeClause_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RangeClause_0_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = VarDefinitionList(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, VAR_ASSIGN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '(' (identifier ReceiverTail | ReceiverTail) ')'
  public static boolean Receiver(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Receiver")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && Receiver_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, RECEIVER, result_);
    return result_;
  }

  // identifier ReceiverTail | ReceiverTail
  private static boolean Receiver_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Receiver_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = Receiver_1_0(builder_, level_ + 1);
    if (!result_) result_ = ReceiverTail(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // identifier ReceiverTail
  private static boolean Receiver_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Receiver_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    result_ = result_ && ReceiverTail(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // TypeReferenceExpression
  public static boolean ReceiverResultType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ReceiverResultType")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeReferenceExpression(builder_, level_ + 1);
    exit_section_(builder_, marker_, TYPE, result_);
    return result_;
  }

  /* ********************************************************** */
  // '*'? ReceiverResultType
  static boolean ReceiverTail(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ReceiverTail")) return false;
    if (!nextTokenIs(builder_, "", MUL, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ReceiverTail_0(builder_, level_ + 1);
    result_ = result_ && ReceiverResultType(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '*'?
  private static boolean ReceiverTail_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ReceiverTail_0")) return false;
    consumeToken(builder_, MUL);
    return true;
  }

  /* ********************************************************** */
  // TypeName | '(' '*' TypeName ')' | '(' ReceiverType ')'
  public static boolean ReceiverType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ReceiverType")) return false;
    if (!nextTokenIs(builder_, "<receiver type>", LPAREN, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, "<receiver type>");
    result_ = TypeName(builder_, level_ + 1);
    if (!result_) result_ = ReceiverType_1(builder_, level_ + 1);
    if (!result_) result_ = ReceiverType_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, RECEIVER_TYPE, result_, false, null);
    return result_;
  }

  // '(' '*' TypeName ')'
  private static boolean ReceiverType_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ReceiverType_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && consumeToken(builder_, MUL);
    result_ = result_ && TypeName(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '(' ReceiverType ')'
  private static boolean ReceiverType_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ReceiverType_2")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && ReceiverType(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // [ ExpressionList '=' | VarDefinitionList ':=' ] Expression
  public static boolean RecvStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecvStatement")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<recv statement>");
    result_ = RecvStatement_0(builder_, level_ + 1);
    result_ = result_ && Expression(builder_, level_ + 1, -1);
    exit_section_(builder_, level_, marker_, RECV_STATEMENT, result_, false, null);
    return result_;
  }

  // [ ExpressionList '=' | VarDefinitionList ':=' ]
  private static boolean RecvStatement_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecvStatement_0")) return false;
    RecvStatement_0_0(builder_, level_ + 1);
    return true;
  }

  // ExpressionList '=' | VarDefinitionList ':='
  private static boolean RecvStatement_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecvStatement_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = RecvStatement_0_0_0(builder_, level_ + 1);
    if (!result_) result_ = RecvStatement_0_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ExpressionList '='
  private static boolean RecvStatement_0_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecvStatement_0_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ExpressionList(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ASSIGN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // VarDefinitionList ':='
  private static boolean RecvStatement_0_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RecvStatement_0_0_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = VarDefinitionList(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, VAR_ASSIGN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier
  public static boolean ReferenceExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ReferenceExpression")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, REFERENCE_EXPRESSION, result_);
    return result_;
  }

  /* ********************************************************** */
  // '(' TypeListNoPin ')' | Type | Parameters
  public static boolean Result(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Result")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<result>");
    result_ = Result_0(builder_, level_ + 1);
    if (!result_) result_ = Type(builder_, level_ + 1);
    if (!result_) result_ = Parameters(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, RESULT, result_, false, null);
    return result_;
  }

  // '(' TypeListNoPin ')'
  private static boolean Result_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Result_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && TypeListNoPin(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // return ExpressionList?
  public static boolean ReturnStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ReturnStatement")) return false;
    if (!nextTokenIs(builder_, RETURN)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, RETURN);
    pinned_ = result_; // pin = 1
    result_ = result_ && ReturnStatement_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, RETURN_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ExpressionList?
  private static boolean ReturnStatement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ReturnStatement_1")) return false;
    ExpressionList(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // select '{' ( CommClause )* '}'
  public static boolean SelectStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SelectStatement")) return false;
    if (!nextTokenIs(builder_, SELECT)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, SELECT);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, consumeToken(builder_, LBRACE));
    result_ = pinned_ && report_error_(builder_, SelectStatement_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, SELECT_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ( CommClause )*
  private static boolean SelectStatement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SelectStatement_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!SelectStatement_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "SelectStatement_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ( CommClause )
  private static boolean SelectStatement_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SelectStatement_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = CommClause(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // Expression '<-' Expression
  public static boolean SendStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SendStatement")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<send statement>");
    result_ = Expression(builder_, level_ + 1, -1);
    result_ = result_ && consumeToken(builder_, SEND_CHANNEL);
    pinned_ = result_; // pin = 2
    result_ = result_ && Expression(builder_, level_ + 1, -1);
    exit_section_(builder_, level_, marker_, SEND_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // VarDefinitionList ':=' ExpressionList
  public static boolean ShortVarDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ShortVarDeclaration")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = VarDefinitionList(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, VAR_ASSIGN);
    pinned_ = result_; // pin = 2
    result_ = result_ && ExpressionList(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, SHORT_VAR_DECLARATION, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // Parameters Result?
  public static boolean Signature(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Signature")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = Parameters(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && Signature_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, SIGNATURE, result_, pinned_, null);
    return result_ || pinned_;
  }

  // Result?
  private static boolean Signature_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Signature_1")) return false;
    Result(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // AssignmentStatement
  //   | SendStatement
  //   | ShortVarDeclaration
  //   | Expression ['++' | '--']
  public static boolean SimpleStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SimpleStatement")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, "<simple statement>");
    result_ = AssignmentStatement(builder_, level_ + 1);
    if (!result_) result_ = SendStatement(builder_, level_ + 1);
    if (!result_) result_ = ShortVarDeclaration(builder_, level_ + 1);
    if (!result_) result_ = SimpleStatement_3(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, SIMPLE_STATEMENT, result_, false, null);
    return result_;
  }

  // Expression ['++' | '--']
  private static boolean SimpleStatement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SimpleStatement_3")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = Expression(builder_, level_ + 1, -1);
    result_ = result_ && SimpleStatement_3_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ['++' | '--']
  private static boolean SimpleStatement_3_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SimpleStatement_3_1")) return false;
    SimpleStatement_3_1_0(builder_, level_ + 1);
    return true;
  }

  // '++' | '--'
  private static boolean SimpleStatement_3_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SimpleStatement_3_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PLUS_PLUS);
    if (!result_) result_ = consumeToken(builder_, MINUS_MINUS);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // [SimpleStatement ';'?]
  static boolean SimpleStatementOpt(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SimpleStatementOpt")) return false;
    SimpleStatementOpt_0(builder_, level_ + 1);
    return true;
  }

  // SimpleStatement ';'?
  private static boolean SimpleStatementOpt_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SimpleStatementOpt_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = SimpleStatement(builder_, level_ + 1);
    result_ = result_ && SimpleStatementOpt_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ';'?
  private static boolean SimpleStatementOpt_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SimpleStatementOpt_0_1")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // ConstDeclaration
  //   | TypeDeclaration
  //   | VarDeclaration
  //   | LabeledStatement
  //   | SimpleStatement
  //   | GoStatement
  //   | ReturnStatement
  //   | BreakStatement
  //   | ContinueStatement
  //   | GotoStatement
  //   | FallthroughStatement
  //   | Block
  //   | IfStatement
  //   | SwitchStatement
  //   | SelectStatement
  //   | ForStatement
  //   | DeferStatement
  public static boolean Statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Statement")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, "<statement>");
    result_ = ConstDeclaration(builder_, level_ + 1);
    if (!result_) result_ = TypeDeclaration(builder_, level_ + 1);
    if (!result_) result_ = VarDeclaration(builder_, level_ + 1);
    if (!result_) result_ = LabeledStatement(builder_, level_ + 1);
    if (!result_) result_ = SimpleStatement(builder_, level_ + 1);
    if (!result_) result_ = GoStatement(builder_, level_ + 1);
    if (!result_) result_ = ReturnStatement(builder_, level_ + 1);
    if (!result_) result_ = BreakStatement(builder_, level_ + 1);
    if (!result_) result_ = ContinueStatement(builder_, level_ + 1);
    if (!result_) result_ = GotoStatement(builder_, level_ + 1);
    if (!result_) result_ = FallthroughStatement(builder_, level_ + 1);
    if (!result_) result_ = Block(builder_, level_ + 1);
    if (!result_) result_ = IfStatement(builder_, level_ + 1);
    if (!result_) result_ = SwitchStatement(builder_, level_ + 1);
    if (!result_) result_ = SelectStatement(builder_, level_ + 1);
    if (!result_) result_ = ForStatement(builder_, level_ + 1);
    if (!result_) result_ = DeferStatement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, STATEMENT, result_, false, StatementRecover_parser_);
    return result_;
  }

  /* ********************************************************** */
  // !('!' | '!=' | '%' | '%=' | '&&' | '&' | '&=' | '&^' | '&^=' | '(' | ')' | '*' | '*=' | '+' | '++' | '+=' | ',' | '-' | '--' | '-=' | '.' | '...' | '/' | '/=' | ':' | ';' | '<' | '<-' | '<<' | '<<=' | '<=' | '<NL>' | '=' | '==' | '>' | '>=' | '>>' | '>>=' | '[' | ']' | '^' | '^=' | '{' | '|' | '|=' | '||' | '}' | case | chan | char | decimali | default | else | float | floati | func | hex | identifier | imaginary | int | interface | map | oct | rune | string | struct)
  static boolean StatementRecover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StatementRecover")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !StatementRecover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // '!' | '!=' | '%' | '%=' | '&&' | '&' | '&=' | '&^' | '&^=' | '(' | ')' | '*' | '*=' | '+' | '++' | '+=' | ',' | '-' | '--' | '-=' | '.' | '...' | '/' | '/=' | ':' | ';' | '<' | '<-' | '<<' | '<<=' | '<=' | '<NL>' | '=' | '==' | '>' | '>=' | '>>' | '>>=' | '[' | ']' | '^' | '^=' | '{' | '|' | '|=' | '||' | '}' | case | chan | char | decimali | default | else | float | floati | func | hex | identifier | imaginary | int | interface | map | oct | rune | string | struct
  private static boolean StatementRecover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StatementRecover_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, NOT);
    if (!result_) result_ = consumeToken(builder_, NOT_EQ);
    if (!result_) result_ = consumeToken(builder_, REMAINDER);
    if (!result_) result_ = consumeToken(builder_, REMAINDER_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, COND_AND);
    if (!result_) result_ = consumeToken(builder_, BIT_AND);
    if (!result_) result_ = consumeToken(builder_, BIT_AND_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, BIT_CLEAR);
    if (!result_) result_ = consumeToken(builder_, BIT_CLEAR_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, LPAREN);
    if (!result_) result_ = consumeToken(builder_, RPAREN);
    if (!result_) result_ = consumeToken(builder_, MUL);
    if (!result_) result_ = consumeToken(builder_, MUL_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, PLUS);
    if (!result_) result_ = consumeToken(builder_, PLUS_PLUS);
    if (!result_) result_ = consumeToken(builder_, PLUS_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, COMMA);
    if (!result_) result_ = consumeToken(builder_, MINUS);
    if (!result_) result_ = consumeToken(builder_, MINUS_MINUS);
    if (!result_) result_ = consumeToken(builder_, MINUS_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, DOT);
    if (!result_) result_ = consumeToken(builder_, TRIPLE_DOT);
    if (!result_) result_ = consumeToken(builder_, QUOTIENT);
    if (!result_) result_ = consumeToken(builder_, QUOTIENT_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, COLON);
    if (!result_) result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = consumeToken(builder_, LESS);
    if (!result_) result_ = consumeToken(builder_, SEND_CHANNEL);
    if (!result_) result_ = consumeToken(builder_, SHIFT_LEFT);
    if (!result_) result_ = consumeToken(builder_, SHIFT_LEFT_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, LESS_OR_EQUAL);
    if (!result_) result_ = consumeToken(builder_, SEMICOLON_SYNTHETIC);
    if (!result_) result_ = consumeToken(builder_, ASSIGN);
    if (!result_) result_ = consumeToken(builder_, EQ);
    if (!result_) result_ = consumeToken(builder_, GREATER);
    if (!result_) result_ = consumeToken(builder_, GREATER_OR_EQUAL);
    if (!result_) result_ = consumeToken(builder_, SHIFT_RIGHT);
    if (!result_) result_ = consumeToken(builder_, SHIFT_RIGHT_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, LBRACK);
    if (!result_) result_ = consumeToken(builder_, RBRACK);
    if (!result_) result_ = consumeToken(builder_, BIT_XOR);
    if (!result_) result_ = consumeToken(builder_, BIT_XOR_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, LBRACE);
    if (!result_) result_ = consumeToken(builder_, BIT_OR);
    if (!result_) result_ = consumeToken(builder_, BIT_OR_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, COND_OR);
    if (!result_) result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = consumeToken(builder_, CASE);
    if (!result_) result_ = consumeToken(builder_, CHAN);
    if (!result_) result_ = consumeToken(builder_, CHAR);
    if (!result_) result_ = consumeToken(builder_, DECIMALI);
    if (!result_) result_ = consumeToken(builder_, DEFAULT);
    if (!result_) result_ = consumeToken(builder_, ELSE);
    if (!result_) result_ = consumeToken(builder_, FLOAT);
    if (!result_) result_ = consumeToken(builder_, FLOATI);
    if (!result_) result_ = consumeToken(builder_, FUNC);
    if (!result_) result_ = consumeToken(builder_, HEX);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = consumeToken(builder_, IMAGINARY);
    if (!result_) result_ = consumeToken(builder_, INT);
    if (!result_) result_ = consumeToken(builder_, INTERFACE);
    if (!result_) result_ = consumeToken(builder_, MAP);
    if (!result_) result_ = consumeToken(builder_, OCT);
    if (!result_) result_ = consumeToken(builder_, RUNE);
    if (!result_) result_ = consumeToken(builder_, STRING);
    if (!result_) result_ = consumeToken(builder_, STRUCT);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // Statement (semi Statement)* semi?
  static boolean Statements(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Statements")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = Statement(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, Statements_1(builder_, level_ + 1));
    result_ = pinned_ && Statements_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (semi Statement)*
  private static boolean Statements_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Statements_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!Statements_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "Statements_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // semi Statement
  private static boolean Statements_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Statements_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = semi(builder_, level_ + 1);
    result_ = result_ && Statement(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // semi?
  private static boolean Statements_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Statements_2")) return false;
    semi(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // struct '{' Fields? '}'
  public static boolean StructType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StructType")) return false;
    if (!nextTokenIs(builder_, STRUCT)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, STRUCT);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, consumeToken(builder_, LBRACE));
    result_ = pinned_ && report_error_(builder_, StructType_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, STRUCT_TYPE, result_, pinned_, null);
    return result_ || pinned_;
  }

  // Fields?
  private static boolean StructType_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StructType_2")) return false;
    Fields(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // switch
  public static boolean SwitchStart(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SwitchStart")) return false;
    if (!nextTokenIs(builder_, SWITCH)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SWITCH);
    exit_section_(builder_, marker_, SWITCH_START, result_);
    return result_;
  }

  /* ********************************************************** */
  // SwitchStart (TypeSwitchStatement | ExprSwitchStatement)
  public static boolean SwitchStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SwitchStatement")) return false;
    if (!nextTokenIs(builder_, SWITCH)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, null);
    result_ = SwitchStart(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && SwitchStatement_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, SWITCH_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // TypeSwitchStatement | ExprSwitchStatement
  private static boolean SwitchStatement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SwitchStatement_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeSwitchStatement(builder_, level_ + 1);
    if (!result_) result_ = ExprSwitchStatement(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // string
  public static boolean Tag(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Tag")) return false;
    if (!nextTokenIs(builder_, STRING)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, STRING);
    exit_section_(builder_, marker_, TAG, result_);
    return result_;
  }

  /* ********************************************************** */
  // OneOfDeclarations semi
  static boolean TopLevelDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TopLevelDeclaration")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = OneOfDeclarations(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && semi(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, TopLevelDeclarationRecover_parser_);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // !(';' |'type' | const | func | var)
  static boolean TopLevelDeclarationRecover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TopLevelDeclarationRecover")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !TopLevelDeclarationRecover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // ';' |'type' | const | func | var
  private static boolean TopLevelDeclarationRecover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TopLevelDeclarationRecover_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = consumeToken(builder_, TYPE_);
    if (!result_) result_ = consumeToken(builder_, CONST);
    if (!result_) result_ = consumeToken(builder_, FUNC);
    if (!result_) result_ = consumeToken(builder_, VAR);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // TypeName | TypeLit | '(' Type ')'
  public static boolean Type(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Type")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, "<type>");
    result_ = TypeName(builder_, level_ + 1);
    if (!result_) result_ = TypeLit(builder_, level_ + 1);
    if (!result_) result_ = Type_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, TYPE, result_, false, null);
    return result_;
  }

  // '(' Type ')'
  private static boolean Type_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Type_2")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && Type(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // TypeSwitchCase ':' Statements?
  public static boolean TypeCaseClause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeCaseClause")) return false;
    if (!nextTokenIs(builder_, "<type case clause>", CASE, DEFAULT)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<type case clause>");
    result_ = TypeSwitchCase(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && TypeCaseClause_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, TYPE_CASE_CLAUSE, result_, false, null);
    return result_;
  }

  // Statements?
  private static boolean TypeCaseClause_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeCaseClause_2")) return false;
    Statements(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // 'type' ( TypeSpec | '(' TypeSpecs? ')' )
  public static boolean TypeDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeDeclaration")) return false;
    if (!nextTokenIs(builder_, TYPE_)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, TYPE_);
    pinned_ = result_; // pin = 1
    result_ = result_ && TypeDeclaration_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, TYPE_DECLARATION, result_, pinned_, null);
    return result_ || pinned_;
  }

  // TypeSpec | '(' TypeSpecs? ')'
  private static boolean TypeDeclaration_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeDeclaration_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeSpec(builder_, level_ + 1);
    if (!result_) result_ = TypeDeclaration_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '(' TypeSpecs? ')'
  private static boolean TypeDeclaration_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeDeclaration_1_1")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, LPAREN);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, TypeDeclaration_1_1_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RPAREN) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // TypeSpecs?
  private static boolean TypeDeclaration_1_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeDeclaration_1_1_1")) return false;
    TypeSpecs(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // Type ( ',' Type )*
  public static boolean TypeList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeList")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, "<type list>");
    result_ = Type(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && TypeList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, TYPE_LIST, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ( ',' Type )*
  private static boolean TypeList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeList_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!TypeList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "TypeList_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' Type
  private static boolean TypeList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeList_1_0")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, COMMA);
    pinned_ = result_; // pin = 1
    result_ = result_ && Type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // Type ( ',' Type )*
  public static boolean TypeListNoPin(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeListNoPin")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<type list no pin>");
    result_ = Type(builder_, level_ + 1);
    result_ = result_ && TypeListNoPin_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, TYPE_LIST, result_, false, null);
    return result_;
  }

  // ( ',' Type )*
  private static boolean TypeListNoPin_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeListNoPin_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!TypeListNoPin_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "TypeListNoPin_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' Type
  private static boolean TypeListNoPin_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeListNoPin_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && Type(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ArrayOrSliceType
  //   | StructType
  //   | PointerType
  //   | FunctionType
  //   | InterfaceType
  //   | MapType
  //   | ChannelType
  static boolean TypeLit(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeLit")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ArrayOrSliceType(builder_, level_ + 1);
    if (!result_) result_ = StructType(builder_, level_ + 1);
    if (!result_) result_ = PointerType(builder_, level_ + 1);
    if (!result_) result_ = FunctionType(builder_, level_ + 1);
    if (!result_) result_ = InterfaceType(builder_, level_ + 1);
    if (!result_) result_ = MapType(builder_, level_ + 1);
    if (!result_) result_ = ChannelType(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // TypeReferenceExpression QualifiedTypeReferenceExpression?
  static boolean TypeName(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeName")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeReferenceExpression(builder_, level_ + 1);
    result_ = result_ && TypeName_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // QualifiedTypeReferenceExpression?
  private static boolean TypeName_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeName_1")) return false;
    QualifiedTypeReferenceExpression(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // identifier
  public static boolean TypeReferenceExpression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeReferenceExpression")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, TYPE_REFERENCE_EXPRESSION, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier Type
  public static boolean TypeSpec(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSpec")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, IDENTIFIER);
    pinned_ = result_; // pin = 1
    result_ = result_ && Type(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, TYPE_SPEC, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // TypeSpec (semi TypeSpec)* semi?
  static boolean TypeSpecs(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSpecs")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = TypeSpec(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, TypeSpecs_1(builder_, level_ + 1));
    result_ = pinned_ && TypeSpecs_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (semi TypeSpec)*
  private static boolean TypeSpecs_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSpecs_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!TypeSpecs_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "TypeSpecs_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // semi TypeSpec
  private static boolean TypeSpecs_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSpecs_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = semi(builder_, level_ + 1);
    result_ = result_ && TypeSpec(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // semi?
  private static boolean TypeSpecs_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSpecs_2")) return false;
    semi(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // case TypeList | default
  public static boolean TypeSwitchCase(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchCase")) return false;
    if (!nextTokenIs(builder_, "<type switch case>", CASE, DEFAULT)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<type switch case>");
    result_ = TypeSwitchCase_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, DEFAULT);
    exit_section_(builder_, level_, marker_, TYPE_SWITCH_CASE, result_, false, null);
    return result_;
  }

  // case TypeList
  private static boolean TypeSwitchCase_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchCase_0")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, CASE);
    pinned_ = result_; // pin = 1
    result_ = result_ && TypeList(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // [ identifier ':=' ] Expression '.' '(' 'type' ')'
  public static boolean TypeSwitchGuard(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchGuard")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<type switch guard>");
    result_ = TypeSwitchGuard_0(builder_, level_ + 1);
    result_ = result_ && Expression(builder_, level_ + 1, -1);
    result_ = result_ && consumeToken(builder_, DOT);
    result_ = result_ && consumeToken(builder_, LPAREN);
    result_ = result_ && consumeToken(builder_, TYPE_);
    pinned_ = result_; // pin = 5
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, level_, marker_, TYPE_SWITCH_GUARD, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [ identifier ':=' ]
  private static boolean TypeSwitchGuard_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchGuard_0")) return false;
    TypeSwitchGuard_0_0(builder_, level_ + 1);
    return true;
  }

  // identifier ':='
  private static boolean TypeSwitchGuard_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchGuard_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    result_ = result_ && consumeToken(builder_, VAR_ASSIGN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // (TypeSwitchGuard | SimpleStatement ';'? TypeSwitchGuard) '{' ( TypeCaseClause )* '}'
  public static boolean TypeSwitchStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchStatement")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _LEFT_, "<type switch statement>");
    result_ = TypeSwitchStatement_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, consumeToken(builder_, LBRACE));
    result_ = pinned_ && report_error_(builder_, TypeSwitchStatement_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, TYPE_SWITCH_STATEMENT, result_, pinned_, null);
    return result_ || pinned_;
  }

  // TypeSwitchGuard | SimpleStatement ';'? TypeSwitchGuard
  private static boolean TypeSwitchStatement_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchStatement_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeSwitchGuard(builder_, level_ + 1);
    if (!result_) result_ = TypeSwitchStatement_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // SimpleStatement ';'? TypeSwitchGuard
  private static boolean TypeSwitchStatement_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchStatement_0_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = SimpleStatement(builder_, level_ + 1);
    result_ = result_ && TypeSwitchStatement_0_1_1(builder_, level_ + 1);
    result_ = result_ && TypeSwitchGuard(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ';'?
  private static boolean TypeSwitchStatement_0_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchStatement_0_1_1")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  // ( TypeCaseClause )*
  private static boolean TypeSwitchStatement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchStatement_2")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!TypeSwitchStatement_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "TypeSwitchStatement_2", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ( TypeCaseClause )
  private static boolean TypeSwitchStatement_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeSwitchStatement_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeCaseClause(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // Expression | LiteralValue
  public static boolean Value(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Value")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<value>");
    result_ = Expression(builder_, level_ + 1, -1);
    if (!result_) result_ = LiteralValue(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, VALUE, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // var ( VarSpec | '(' VarSpecs? ')' )
  public static boolean VarDeclaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarDeclaration")) return false;
    if (!nextTokenIs(builder_, VAR)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, VAR);
    pinned_ = result_; // pin = 1
    result_ = result_ && VarDeclaration_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, VAR_DECLARATION, result_, pinned_, null);
    return result_ || pinned_;
  }

  // VarSpec | '(' VarSpecs? ')'
  private static boolean VarDeclaration_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarDeclaration_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = VarSpec(builder_, level_ + 1);
    if (!result_) result_ = VarDeclaration_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '(' VarSpecs? ')'
  private static boolean VarDeclaration_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarDeclaration_1_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && VarDeclaration_1_1_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // VarSpecs?
  private static boolean VarDeclaration_1_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarDeclaration_1_1_1")) return false;
    VarSpecs(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // identifier
  public static boolean VarDefinition(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarDefinition")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, VAR_DEFINITION, result_);
    return result_;
  }

  /* ********************************************************** */
  // VarDefinition ( ',' VarDefinition )*
  static boolean VarDefinitionList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarDefinitionList")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = VarDefinition(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && VarDefinitionList_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ( ',' VarDefinition )*
  private static boolean VarDefinitionList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarDefinitionList_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!VarDefinitionList_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "VarDefinitionList_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // ',' VarDefinition
  private static boolean VarDefinitionList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarDefinitionList_1_0")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, COMMA);
    pinned_ = result_; // pin = 1
    result_ = result_ && VarDefinition(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // VarDefinitionList ( Type [ '=' ExpressionList ] | '=' ExpressionList )
  public static boolean VarSpec(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarSpec")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = VarDefinitionList(builder_, level_ + 1);
    result_ = result_ && VarSpec_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, VAR_SPEC, result_);
    return result_;
  }

  // Type [ '=' ExpressionList ] | '=' ExpressionList
  private static boolean VarSpec_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarSpec_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = VarSpec_1_0(builder_, level_ + 1);
    if (!result_) result_ = VarSpec_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // Type [ '=' ExpressionList ]
  private static boolean VarSpec_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarSpec_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = Type(builder_, level_ + 1);
    result_ = result_ && VarSpec_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [ '=' ExpressionList ]
  private static boolean VarSpec_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarSpec_1_0_1")) return false;
    VarSpec_1_0_1_0(builder_, level_ + 1);
    return true;
  }

  // '=' ExpressionList
  private static boolean VarSpec_1_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarSpec_1_0_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ASSIGN);
    result_ = result_ && ExpressionList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '=' ExpressionList
  private static boolean VarSpec_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarSpec_1_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ASSIGN);
    result_ = result_ && ExpressionList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // VarSpec (semi VarSpec)* semi?
  static boolean VarSpecs(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarSpecs")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = VarSpec(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, VarSpecs_1(builder_, level_ + 1));
    result_ = pinned_ && VarSpecs_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (semi VarSpec)*
  private static boolean VarSpecs_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarSpecs_1")) return false;
    int pos_ = current_position_(builder_);
    while (true) {
      if (!VarSpecs_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "VarSpecs_1", pos_)) break;
      pos_ = current_position_(builder_);
    }
    return true;
  }

  // semi VarSpec
  private static boolean VarSpecs_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarSpecs_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = semi(builder_, level_ + 1);
    result_ = result_ && VarSpec(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // semi?
  private static boolean VarSpecs_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VarSpecs_2")) return false;
    semi(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // '+' | '-' | '|' | '^'
  static boolean add_op(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "add_op")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PLUS);
    if (!result_) result_ = consumeToken(builder_, MINUS);
    if (!result_) result_ = consumeToken(builder_, BIT_OR);
    if (!result_) result_ = consumeToken(builder_, BIT_XOR);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '=' | '+=' | '-=' | '|=' | '^=' | '*=' | '/=' | '%=' | '<<=' | '>>=' | '&=' | '&^='
  public static boolean assign_op(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assign_op")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<assign op>");
    result_ = consumeToken(builder_, ASSIGN);
    if (!result_) result_ = consumeToken(builder_, PLUS_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, MINUS_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, BIT_OR_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, BIT_XOR_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, MUL_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, QUOTIENT_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, REMAINDER_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, SHIFT_LEFT_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, SHIFT_RIGHT_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, BIT_AND_ASSIGN);
    if (!result_) result_ = consumeToken(builder_, BIT_CLEAR_ASSIGN);
    exit_section_(builder_, level_, marker_, ASSIGN_OP, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // '*' | '/' | '%' | '<<' | '>>' | '&' | '&^'
  static boolean mul_op(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mul_op")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MUL);
    if (!result_) result_ = consumeToken(builder_, QUOTIENT);
    if (!result_) result_ = consumeToken(builder_, REMAINDER);
    if (!result_) result_ = consumeToken(builder_, SHIFT_LEFT);
    if (!result_) result_ = consumeToken(builder_, SHIFT_RIGHT);
    if (!result_) result_ = consumeToken(builder_, BIT_AND);
    if (!result_) result_ = consumeToken(builder_, BIT_CLEAR);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '==' | '!=' | '<' | '<=' | '>' | '>='
  static boolean rel_op(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "rel_op")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EQ);
    if (!result_) result_ = consumeToken(builder_, NOT_EQ);
    if (!result_) result_ = consumeToken(builder_, LESS);
    if (!result_) result_ = consumeToken(builder_, LESS_OR_EQUAL);
    if (!result_) result_ = consumeToken(builder_, GREATER);
    if (!result_) result_ = consumeToken(builder_, GREATER_OR_EQUAL);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '<NL>' | ';' | <<eof>>
  static boolean semi(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "semi")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEMICOLON_SYNTHETIC);
    if (!result_) result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = eof(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '+' | '-' | '!' | '^' | '*' | '&' | '<-'
  static boolean unary_op(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unary_op")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PLUS);
    if (!result_) result_ = consumeToken(builder_, MINUS);
    if (!result_) result_ = consumeToken(builder_, NOT);
    if (!result_) result_ = consumeToken(builder_, BIT_XOR);
    if (!result_) result_ = consumeToken(builder_, MUL);
    if (!result_) result_ = consumeToken(builder_, BIT_AND);
    if (!result_) result_ = consumeToken(builder_, SEND_CHANNEL);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // Expression root: Expression
  // Operator priority table:
  // 0: BINARY(OrExpr)
  // 1: BINARY(AndExpr)
  // 2: BINARY(ConditionalExpr)
  // 3: BINARY(AddExpr)
  // 4: BINARY(MulExpr)
  // 5: PREFIX(UnaryExpr)
  // 6: PREFIX(ConversionExpr)
  // 7: ATOM(CompositeLit) ATOM(OperandName) POSTFIX(BuiltinCallExpr) POSTFIX(CallExpr) POSTFIX(TypeAssertionExpr) BINARY(SelectorExpr) ATOM(MethodExpr) POSTFIX(IndexExpr) ATOM(Literal) ATOM(LiteralTypeExpr) ATOM(FunctionLit)
  // 8: PREFIX(ParenthesesExpr)
  public static boolean Expression(PsiBuilder builder_, int level_, int priority_) {
    if (!recursion_guard_(builder_, level_, "Expression")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<expression>");
    result_ = UnaryExpr(builder_, level_ + 1);
    if (!result_) result_ = ConversionExpr(builder_, level_ + 1);
    if (!result_) result_ = CompositeLit(builder_, level_ + 1);
    if (!result_) result_ = OperandName(builder_, level_ + 1);
    if (!result_) result_ = MethodExpr(builder_, level_ + 1);
    if (!result_) result_ = Literal(builder_, level_ + 1);
    if (!result_) result_ = LiteralTypeExpr(builder_, level_ + 1);
    if (!result_) result_ = FunctionLit(builder_, level_ + 1);
    if (!result_) result_ = ParenthesesExpr(builder_, level_ + 1);
    pinned_ = result_;
    result_ = result_ && Expression_0(builder_, level_ + 1, priority_);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  public static boolean Expression_0(PsiBuilder builder_, int level_, int priority_) {
    if (!recursion_guard_(builder_, level_, "Expression_0")) return false;
    boolean result_ = true;
    while (true) {
      Marker left_marker_ = (Marker) builder_.getLatestDoneMarker();
      if (!invalid_left_marker_guard_(builder_, left_marker_, "Expression_0")) return false;
      Marker marker_ = builder_.mark();
      if (priority_ < 0 && consumeToken(builder_, COND_OR)) {
        result_ = report_error_(builder_, Expression(builder_, level_, 0));
        marker_.drop();
        left_marker_.precede().done(OR_EXPR);
      }
      else if (priority_ < 1 && consumeToken(builder_, COND_AND)) {
        result_ = report_error_(builder_, Expression(builder_, level_, 1));
        marker_.drop();
        left_marker_.precede().done(AND_EXPR);
      }
      else if (priority_ < 2 && rel_op(builder_, level_ + 1)) {
        result_ = report_error_(builder_, Expression(builder_, level_, 2));
        marker_.drop();
        left_marker_.precede().done(CONDITIONAL_EXPR);
      }
      else if (priority_ < 3 && add_op(builder_, level_ + 1)) {
        result_ = report_error_(builder_, Expression(builder_, level_, 3));
        marker_.drop();
        left_marker_.precede().done(ADD_EXPR);
      }
      else if (priority_ < 4 && mul_op(builder_, level_ + 1)) {
        result_ = report_error_(builder_, Expression(builder_, level_, 4));
        marker_.drop();
        left_marker_.precede().done(MUL_EXPR);
      }
      else if (priority_ < 7 && ((LighterASTNode)left_marker_).getTokenType() == REFERENCE_EXPRESSION && BuiltinCallExpr_0(builder_, level_ + 1)) {
        result_ = true;
        marker_.drop();
        left_marker_.precede().done(BUILTIN_CALL_EXPR);
      }
      else if (priority_ < 7 && ArgumentList(builder_, level_ + 1)) {
        result_ = true;
        marker_.drop();
        left_marker_.precede().done(CALL_EXPR);
      }
      else if (priority_ < 7 && TypeAssertionExpr_0(builder_, level_ + 1)) {
        result_ = true;
        marker_.drop();
        left_marker_.precede().done(TYPE_ASSERTION_EXPR);
      }
      else if (priority_ < 7 && SelectorExpr_0(builder_, level_ + 1)) {
        result_ = report_error_(builder_, Expression(builder_, level_, 7));
        marker_.drop();
        left_marker_.precede().done(SELECTOR_EXPR);
      }
      else if (priority_ < 7 && IndexExpr_0(builder_, level_ + 1)) {
        result_ = true;
        marker_.drop();
        left_marker_.precede().done(INDEX_EXPR);
      }
      else {
        exit_section_(builder_, marker_, null, false);
        break;
      }
    }
    return result_;
  }

  public static boolean UnaryExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UnaryExpr")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = unary_op(builder_, level_ + 1);
    pinned_ = result_;
    result_ = pinned_ && Expression(builder_, level_, 5);
    exit_section_(builder_, level_, marker_, UNARY_EXPR, result_, pinned_, null);
    return result_ || pinned_;
  }

  public static boolean ConversionExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConversionExpr")) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = ConversionExpr_0(builder_, level_ + 1);
    pinned_ = result_;
    result_ = pinned_ && Expression(builder_, level_, 6);
    result_ = pinned_ && report_error_(builder_, ConversionExpr_1(builder_, level_ + 1)) && result_;
    exit_section_(builder_, level_, marker_, CONVERSION_EXPR, result_, pinned_, null);
    return result_ || pinned_;
  }

  // &('*' | '<-' | '[' | chan | func | interface | map | struct) Type '('
  private static boolean ConversionExpr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConversionExpr_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ConversionExpr_0_0(builder_, level_ + 1);
    result_ = result_ && Type(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // &('*' | '<-' | '[' | chan | func | interface | map | struct)
  private static boolean ConversionExpr_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConversionExpr_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _AND_, null);
    result_ = ConversionExpr_0_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // '*' | '<-' | '[' | chan | func | interface | map | struct
  private static boolean ConversionExpr_0_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConversionExpr_0_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, MUL);
    if (!result_) result_ = consumeToken(builder_, SEND_CHANNEL);
    if (!result_) result_ = consumeToken(builder_, LBRACK);
    if (!result_) result_ = consumeToken(builder_, CHAN);
    if (!result_) result_ = consumeToken(builder_, FUNC);
    if (!result_) result_ = consumeToken(builder_, INTERFACE);
    if (!result_) result_ = consumeToken(builder_, MAP);
    if (!result_) result_ = consumeToken(builder_, STRUCT);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ','? ')'
  private static boolean ConversionExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConversionExpr_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = ConversionExpr_1_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ','?
  private static boolean ConversionExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConversionExpr_1_0")) return false;
    consumeToken(builder_, COMMA);
    return true;
  }

  // LiteralTypeExpr LiteralValue
  public static boolean CompositeLit(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CompositeLit")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<composite lit>");
    result_ = LiteralTypeExpr(builder_, level_ + 1);
    result_ = result_ && LiteralValue(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, COMPOSITE_LIT, result_, false, null);
    return result_;
  }

  // ReferenceExpression QualifiedReferenceExpression?
  public static boolean OperandName(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "OperandName")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, null);
    result_ = ReferenceExpression(builder_, level_ + 1);
    result_ = result_ && OperandName_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, REFERENCE_EXPRESSION, result_, false, null);
    return result_;
  }

  // QualifiedReferenceExpression?
  private static boolean OperandName_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "OperandName_1")) return false;
    QualifiedReferenceExpression(builder_, level_ + 1);
    return true;
  }

  // <<isBuiltin>> '(' [ BuiltinArgs ','? ] ')'
  private static boolean BuiltinCallExpr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BuiltinCallExpr_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = isBuiltin(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LPAREN);
    result_ = result_ && BuiltinCallExpr_0_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [ BuiltinArgs ','? ]
  private static boolean BuiltinCallExpr_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BuiltinCallExpr_0_2")) return false;
    BuiltinCallExpr_0_2_0(builder_, level_ + 1);
    return true;
  }

  // BuiltinArgs ','?
  private static boolean BuiltinCallExpr_0_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BuiltinCallExpr_0_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = BuiltinArgs(builder_, level_ + 1);
    result_ = result_ && BuiltinCallExpr_0_2_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ','?
  private static boolean BuiltinCallExpr_0_2_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BuiltinCallExpr_0_2_0_1")) return false;
    consumeToken(builder_, COMMA);
    return true;
  }

  // '.' '(' &(!'type') Type ')'
  private static boolean TypeAssertionExpr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeAssertionExpr_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DOT);
    result_ = result_ && consumeToken(builder_, LPAREN);
    result_ = result_ && TypeAssertionExpr_0_2(builder_, level_ + 1);
    result_ = result_ && Type(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // &(!'type')
  private static boolean TypeAssertionExpr_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeAssertionExpr_0_2")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _AND_, null);
    result_ = TypeAssertionExpr_0_2_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // !'type'
  private static boolean TypeAssertionExpr_0_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeAssertionExpr_0_2_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !consumeToken(builder_, TYPE_);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // '.' &(!('(' 'type'))
  private static boolean SelectorExpr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SelectorExpr_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DOT);
    result_ = result_ && SelectorExpr_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // &(!('(' 'type'))
  private static boolean SelectorExpr_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SelectorExpr_0_1")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _AND_, null);
    result_ = SelectorExpr_0_1_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // !('(' 'type')
  private static boolean SelectorExpr_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SelectorExpr_0_1_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NOT_, null);
    result_ = !SelectorExpr_0_1_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, null, result_, false, null);
    return result_;
  }

  // '(' 'type'
  private static boolean SelectorExpr_0_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SelectorExpr_0_1_0_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && consumeToken(builder_, TYPE_);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ReceiverType '.' identifier
  public static boolean MethodExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MethodExpr")) return false;
    if (!nextTokenIs(builder_, "<method expr>", LPAREN, IDENTIFIER)) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<method expr>");
    result_ = ReceiverType(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, DOT);
    result_ = result_ && consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, level_, marker_, METHOD_EXPR, result_, false, null);
    return result_;
  }

  // '[' IndexExprBody ']'
  private static boolean IndexExpr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IndexExpr_0")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACK);
    result_ = result_ && IndexExprBody(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // int
  //   | float
  //   | floati
  //   | decimali
  //   | hex
  //   | oct
  //   | imaginary
  //   | rune
  //   | string
  //   | char
  public static boolean Literal(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Literal")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, "<literal>");
    result_ = consumeToken(builder_, INT);
    if (!result_) result_ = consumeToken(builder_, FLOAT);
    if (!result_) result_ = consumeToken(builder_, FLOATI);
    if (!result_) result_ = consumeToken(builder_, DECIMALI);
    if (!result_) result_ = consumeToken(builder_, HEX);
    if (!result_) result_ = consumeToken(builder_, OCT);
    if (!result_) result_ = consumeToken(builder_, IMAGINARY);
    if (!result_) result_ = consumeToken(builder_, RUNE);
    if (!result_) result_ = consumeToken(builder_, STRING);
    if (!result_) result_ = consumeToken(builder_, CHAR);
    exit_section_(builder_, level_, marker_, LITERAL, result_, false, null);
    return result_;
  }

  // StructType
  //   | ArrayOrSliceType
  //   | MapType
  //   | TypeName
  public static boolean LiteralTypeExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LiteralTypeExpr")) return false;
    boolean result_ = false;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, "<literal type expr>");
    result_ = StructType(builder_, level_ + 1);
    if (!result_) result_ = ArrayOrSliceType(builder_, level_ + 1);
    if (!result_) result_ = MapType(builder_, level_ + 1);
    if (!result_) result_ = TypeName(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, LITERAL_TYPE_EXPR, result_, false, null);
    return result_;
  }

  // func Signature Block
  public static boolean FunctionLit(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionLit")) return false;
    if (!nextTokenIs(builder_, FUNC)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, FUNC);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, Signature(builder_, level_ + 1));
    result_ = pinned_ && Block(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, FUNCTION_LIT, result_, pinned_, null);
    return result_ || pinned_;
  }

  public static boolean ParenthesesExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParenthesesExpr")) return false;
    if (!nextTokenIs(builder_, "<expression>", LPAREN)) return false;
    boolean result_ = false;
    boolean pinned_ = false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeToken(builder_, LPAREN);
    pinned_ = result_;
    result_ = pinned_ && Expression(builder_, level_, -1);
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, RPAREN)) && result_;
    exit_section_(builder_, level_, marker_, PARENTHESES_EXPR, result_, pinned_, null);
    return result_ || pinned_;
  }

  final static Parser StatementRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return StatementRecover(builder_, level_ + 1);
    }
  };
  final static Parser TopLevelDeclarationRecover_parser_ = new Parser() {
    public boolean parse(PsiBuilder builder_, int level_) {
      return TopLevelDeclarationRecover(builder_, level_ + 1);
    }
  };
}