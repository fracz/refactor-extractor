package org.jetbrains.jet.lang.psi;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author abreslav
 */
public class JetTypePattern extends JetPattern {
    public JetTypePattern(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable @IfNotParsed
    public JetTypeReference getTypeReference() {
        return findChildByClass(JetTypeReference.class);
    }

    @Override
    public void accept(@NotNull JetVisitorVoid visitor) {
        visitor.visitTypePattern(this);
    }

    @Override
    public <R, D> R accept(@NotNull JetVisitor<R, D> visitor, D data) {
        return visitor.visitTypePattern(this, data);
    }
}