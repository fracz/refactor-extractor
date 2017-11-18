package org.jetbrains.jet.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.JetNodeTypes;

import java.util.List;

/**
 * @author max
 */
public class JetClassBody extends JetElement {
    public JetClassBody(@NotNull ASTNode node) {
        super(node);
    }

    public List<JetDeclaration> getDeclarations() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, JetDeclaration.class);
    }

    public List<JetConstructor> getSecondaryConstructors() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, JetConstructor.class);
    }

    @Override
    public void accept(@NotNull JetVisitorVoid visitor) {
        visitor.visitClassBody(this);
    }

    @Override
    public <R, D> R accept(@NotNull JetVisitor<R, D> visitor, D data) {
        return visitor.visitClassBody(this, data);
    }

    @NotNull
    public List<JetClassInitializer> getAnonymousInitializers() {
        return findChildrenByType(JetNodeTypes.ANONYMOUS_INITIALIZER);
    }

    @NotNull
    public List<JetProperty> getProperties() {
        return findChildrenByType(JetNodeTypes.PROPERTY);
    }

    @Nullable
    public JetClassObject getClassObject() {
        return (JetClassObject) findChildByType(JetNodeTypes.CLASS_OBJECT);
    }
}