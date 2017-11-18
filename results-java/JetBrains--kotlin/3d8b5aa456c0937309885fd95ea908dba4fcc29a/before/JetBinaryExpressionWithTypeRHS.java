package org.jetbrains.jet.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.JetNodeTypes;

/**
 * @author max
 */
public class JetBinaryExpressionWithTypeRHS extends JetExpression {
    public JetBinaryExpressionWithTypeRHS(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull JetVisitorVoid visitor) {
        visitor.visitBinaryWithTypeRHSExpression(this);
    }

    @Override
    public <R, D> R visit(@NotNull JetVisitor<R, D> visitor, D data) {
        return visitor.visitBinaryWithTypeRHSExpression(this, data);
    }

    @NotNull
    public JetExpression getLeft() {
        JetExpression left = findChildByClass(JetExpression.class);
        assert left != null;
        return left;
    }

    @Nullable @IfNotParsed
    public JetTypeReference getRight() {
        ASTNode node = getOperationSign().getNode();
        while (node != null) {
            PsiElement psi = node.getPsi();
            if (psi instanceof JetTypeReference) {
                return (JetTypeReference) psi;
            }
            node = node.getTreeNext();
        }

        return null;
    }
    @NotNull
    public JetSimpleNameExpression getOperationSign() {
        return (JetSimpleNameExpression) findChildByType(JetNodeTypes.OPERATION_REFERENCE);
    }

}