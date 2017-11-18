package org.jetbrains.jet.codegen;

import com.intellij.psi.*;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.types.DeclarationDescriptor;
import org.jetbrains.jet.lang.types.FunctionDescriptor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.List;
import java.util.Stack;

/**
 * @author max
 */
public class ExpressionCodegen extends JetVisitor {
    private final Stack<Label> myLoopStarts = new Stack<Label>();
    private final Stack<Label> myLoopEnds = new Stack<Label>();

    private final InstructionAdapter v;
    private final FrameMap myMap;
    private final BindingContext bindingContext;

    public ExpressionCodegen(MethodVisitor v, BindingContext bindingContext, FrameMap myMap) {
        this.myMap = myMap;
        this.v = new InstructionAdapter(v);
        this.bindingContext = bindingContext;
    }

    private void gen(JetElement expr) {
        if (expr == null) throw new CompilationException();
        expr.accept(this);
    }

    @Override
    public void visitExpression(JetExpression expression) {
        throw new UnsupportedOperationException("Codegen for " + expression + " is not yet implemented");
    }

    @Override
    public void visitParenthesizedExpression(JetParenthesizedExpression expression) {
        gen(expression.getExpression());
    }

    @Override
    public void visitAnnotatedExpression(JetAnnotatedExpression expression) {
        gen(expression.getBaseExpression());
    }

    @Override
    public void visitIfExpression(JetIfExpression expression) {
        gen(expression.getCondition());
        unboxBoolean();

        JetExpression thenExpression = expression.getThen();
        JetExpression elseExpression = expression.getElse();

        if (thenExpression == null && elseExpression == null) {
            throw new CompilationException();
        }

        if (thenExpression == null) {
            generateSingleBranchIf(elseExpression, false);
            return;
        }

        if (elseExpression == null) {
            generateSingleBranchIf(thenExpression, true);
            return;
        }


        Label elseLabel = new Label();
        v.ifeq(elseLabel); // == 0, i.e. false

        gen(thenExpression);

        Label endLabel = new Label();
        v.goTo(endLabel);
        v.mark(elseLabel);

        gen(elseExpression);

        v.mark(endLabel);
    }

    @Override
    public void visitWhileExpression(JetWhileExpression expression) {
        Label condition = new Label();
        myLoopStarts.push(condition);
        v.mark(condition);

        Label end = new Label();
        myLoopEnds.push(end);

        gen(expression.getCondition());
        unboxBoolean();
        v.ifeq(end);

        genToUnit(expression.getBody());
        v.goTo(condition);

        v.mark(end);
        myLoopEnds.pop();
        myLoopStarts.pop();
    }

    @Override
    public void visitDoWhileExpression(JetDoWhileExpression expression) {
        Label condition = new Label();
        v.mark(condition);
        myLoopStarts.push(condition);

        Label end = new Label();
        myLoopEnds.push(end);

        genToUnit(expression.getBody());

        gen(expression.getCondition());
        unboxBoolean();
        v.ifne(condition);

        v.mark(end);

        myLoopEnds.pop();
        myLoopStarts.pop();
    }

    @Override
    public void visitBreakExpression(JetBreakExpression expression) {
        String labelName = expression.getLabelName();

        Label label = labelName == null ? myLoopEnds.peek() : null; // TODO:

        v.goTo(label);
    }

    @Override
    public void visitContinueExpression(JetContinueExpression expression) {
        String labelName = expression.getLabelName();

        Label label = labelName == null ? myLoopStarts.peek() : null; // TODO:

        v.goTo(label);
    }

    private void unboxBoolean() {
        v.invokevirtual("java/lang/Boolean", "booleanValue", "()Z");
    }

    private void generateSingleBranchIf(JetExpression expression, boolean inverse) {
        Label endLabel = new Label();

        if (inverse) {
            v.ifeq(endLabel);
        }
        else {
            v.ifne(endLabel);
        }

        genToUnit(expression);

        v.mark(endLabel);
    }

    private void genToUnit(JetExpression expression) {
        gen(expression);

        if (!isUnitType(expression)) {
            v.pop();
        }
    }

    @Override
    public void visitConstantExpression(JetConstantExpression expression) {
        Object value = expression.getValue();
        v.aconst(value);

        if (value instanceof Integer) {
            v.invokestatic("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        }
        else if (value instanceof Boolean) {
            v.invokestatic("java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
        }
        else if (value instanceof Character) {
            v.invokestatic("java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
        }
        else if (value instanceof Short) {
            v.invokestatic("java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
        }
        else if (value instanceof Long) {
            v.invokestatic("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
        }
        else if (value instanceof Byte) {
            v.invokestatic("java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
        }
        else if (value instanceof Float) {
            v.invokestatic("java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
        }
        else if (value instanceof Double) {
            v.invokestatic("java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
        }
    }

    @Override
    public void visitBlockExpression(JetBlockExpression expression) {
        Label blockStart = new Label();
        v.mark(blockStart);

        List<JetElement> statements = expression.getStatements();
        for (JetElement statement : statements) {
            if (statement instanceof JetProperty) {
                myMap.enter(bindingContext.getPropertyDescriptor((JetProperty) statement));
            }
        }

        for (JetElement statement : statements) {
            gen(statement);
        }

        Label blockEnd = new Label();
        v.mark(blockEnd);

        for (JetElement statement : statements) {
            if (statement instanceof JetProperty) {
                JetProperty var = (JetProperty) statement;
                int index = myMap.leave(bindingContext.getPropertyDescriptor(var));
                v.visitLocalVariable(var.getName(), getType(var).getDescriptor(), null, blockStart, blockEnd, index);
            }
        }

    }

    @Override
    public void visitReturnExpression(JetReturnExpression expression) {
        final JetExpression returnedExpression = expression.getReturnedExpression();
        if (returnedExpression != null) {
            returnedExpression.accept(this);
            v.visitInsn(Opcodes.ARETURN);
        }
        else {
            v.visitInsn(Opcodes.RETURN);
        }
    }

    @Override
    public void visitSimpleNameExpression(JetSimpleNameExpression expression) {
        final DeclarationDescriptor descriptor = bindingContext.resolveReferenceExpression(expression);
        int index = myMap.getIndex(descriptor);
        if (index >= 0) {
            v.visitVarInsn(Opcodes.ALOAD, index);
        }
        else {
            throw new UnsupportedOperationException("don't know how to generate reference " + descriptor);
        }
    }

    @Override
    public void visitCallExpression(JetCallExpression expression) {
        List<JetArgument> args = expression.getValueArguments();
        for (JetArgument arg : args) {
            gen(arg.getArgumentExpression());
        }

        JetExpression callee = expression.getCalleeExpression();

        if (callee instanceof JetSimpleNameExpression) {
            DeclarationDescriptor funDescriptor = bindingContext.resolveReferenceExpression((JetSimpleNameExpression) callee);
            if (funDescriptor instanceof FunctionDescriptor) {
                PsiElement declarationPsiElement = bindingContext.getDeclarationPsiElement(funDescriptor);
                if (declarationPsiElement instanceof PsiMethod) {
                    PsiMethod method = (PsiMethod) declarationPsiElement;
                    if (method.hasModifierProperty(PsiModifier.STATIC)) {
                        PsiClass containingClass = method.getContainingClass();
                        String owner = containingClass.getQualifiedName().replace(".", "/");
                        v.visitMethodInsn(Opcodes.INVOKESTATIC, owner, method.getName(), getMethodDescriptor(method));
                        boxIfNeeded(method.getReturnType());
                    }
                    else {
                        throw new UnsupportedOperationException("don't know how to generate instance method calls");
                    }
                }
            }
            else {
                throw new CompilationException();
            }
        }
        else {
            throw new UnsupportedOperationException("Don't know how to generate a call");
        }
    }

    private void boxIfNeeded(PsiType type) {
        if (type == PsiType.LONG) {
            v.invokestatic("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
        }
        else {
            throw new UnsupportedOperationException("Don't know how to box type " + type);
        }
    }

    private String getMethodDescriptor(PsiMethod method) {
        Type returnType = psiTypeToAsm(method.getReturnType());
        PsiParameter[] parameters = method.getParameterList().getParameters();
        Type[] parameterTypes = new Type[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterTypes[i] = psiTypeToAsm(parameters [i].getType());
        }
        return Type.getMethodDescriptor(returnType, parameterTypes);
    }

    private Type psiTypeToAsm(PsiType type) {
        if (type instanceof PsiPrimitiveType) {
            if (type == PsiType.VOID) {
                return Type.VOID_TYPE;
            }
            if (type == PsiType.LONG) {
                return Type.LONG_TYPE;
            }
        }
        throw new UnsupportedOperationException("don't know how to map  type " + type + " to ASM");
    }

    @Override
    public void visitDotQualifiedExpression(JetDotQualifiedExpression expression) {
//        gen(expression.getReceiverExpression());
        expression.getSelectorExpression().accept(this);
    }

    /*
        @Override
        public void visitBinaryExpression(JetBinaryExpression expression) {
        }
    */

    private Type getType(JetProperty var) {
        return InstructionAdapter.OBJECT_TYPE;  // TODO:
    }

    private boolean isUnitType(JetExpression e) {
        return false; // TODO:!!!
    }

    @Override
    public void visitProperty(JetProperty property) {
        int index = myMap.getIndex(bindingContext.getPropertyDescriptor(property));

        assert index >= 0;

        JetExpression initializer = property.getInitializer();
        if (initializer != null) {
            gen(initializer);
            v.store(index, getType(property));
        }
    }

    private static class CompilationException extends RuntimeException {
    }
}