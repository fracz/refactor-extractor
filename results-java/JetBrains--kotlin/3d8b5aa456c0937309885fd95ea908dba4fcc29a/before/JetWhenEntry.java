package org.jetbrains.jet.lang.psi;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lexer.JetTokens;

/**
 * @author abreslav
 */
public class JetWhenEntry extends JetElement {
    public JetWhenEntry(@NotNull ASTNode node) {
        super(node);
    }

    public boolean isElse() {
        return findChildByType(JetTokens.ELSE_KEYWORD) != null;
    }

    @Nullable
    public JetExpression getExpression() {
        return findChildByClass(JetExpression.class);
    }

    @Override
    public void accept(@NotNull JetVisitorVoid visitor) {
        visitor.visitWhenEntry(this);
    }

    @Override
    public <R, D> R visit(@NotNull JetVisitor<R, D> visitor, D data) {
        return visitor.visitWhenEntry(this, data);
    }

    @NotNull
    public JetWhenCondition[] getConditions() {
        return findChildrenByClass(JetWhenCondition.class);
    }
}