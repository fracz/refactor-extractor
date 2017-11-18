package org.jetbrains.jet.lang.psi;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.JetNodeTypes;

import java.util.List;

/**
 * @author max
 */
public class JetTupleType extends JetTypeElement {
    public JetTupleType(@NotNull ASTNode node) {
        super(node);
    }

    @NotNull
    @Override
    public List<JetTypeReference> getTypeArgumentsAsTypes() {
        return getComponentTypeRefs();
    }

    @Override
    public void accept(@NotNull JetVisitorVoid visitor) {
        visitor.visitTupleType(this);
    }

    @Override
    public <R, D> R accept(@NotNull JetVisitor<R, D> visitor, D data) {
        return visitor.visitTupleType(this, data);
    }

    @NotNull
    public List<JetTypeReference> getComponentTypeRefs() {
        return findChildrenByType(JetNodeTypes.TYPE_REFERENCE);
    }
}