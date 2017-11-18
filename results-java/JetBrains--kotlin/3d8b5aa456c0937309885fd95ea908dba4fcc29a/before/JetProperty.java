package org.jetbrains.jet.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.JetNodeTypes;
import org.jetbrains.jet.lexer.JetTokens;

import java.util.List;

import static org.jetbrains.jet.JetNodeTypes.PROPERTY_ACCESSOR;
import static org.jetbrains.jet.lexer.JetTokens.EQ;
import static org.jetbrains.jet.lexer.JetTokens.VAL_KEYWORD;
import static org.jetbrains.jet.lexer.JetTokens.VAR_KEYWORD;

/**
 * @author max
 */
public class JetProperty extends JetTypeParameterListOwner implements JetModifierListOwner {
    public JetProperty(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull JetVisitorVoid visitor) {
        visitor.visitProperty(this);
    }

    @Override
    public <R, D> R visit(@NotNull JetVisitor<R, D> visitor, D data) {
        return visitor.visitProperty(this, data);
    }

    public boolean isVar() {
        return getNode().findChildByType(JetTokens.VAR_KEYWORD) != null;
    }

    @Nullable
    public JetTypeReference getReceiverTypeRef() {
        ASTNode node = getNode().getFirstChildNode();
        while (node != null) {
            IElementType tt = node.getElementType();
            if (tt == JetTokens.COLON) break;

            if (tt == JetNodeTypes.TYPE_REFERENCE) {
                return (JetTypeReference) node.getPsi();
            }
            node = node.getTreeNext();
        }

        return null;
    }

    @Nullable
    public JetTypeReference getPropertyTypeRef() {
        ASTNode node = getNode().getFirstChildNode();
        boolean passedColon = false;
        while (node != null) {
            IElementType tt = node.getElementType();
            if (tt == JetTokens.COLON) {
                passedColon = true;
            }
            else if (tt == JetNodeTypes.TYPE_REFERENCE && passedColon) {
                return (JetTypeReference) node.getPsi();
            }
            node = node.getTreeNext();
        }

        return null;
    }

    @NotNull
    public List<JetPropertyAccessor> getAccessors() {
        return findChildrenByType(PROPERTY_ACCESSOR);
    }

    @Nullable
    public JetPropertyAccessor getGetter() {
        for (JetPropertyAccessor accessor : getAccessors()) {
            if (accessor.isGetter()) return accessor;
        }

        return null;
    }

    @Nullable
    public JetPropertyAccessor getSetter() {
        for (JetPropertyAccessor accessor : getAccessors()) {
            if (accessor.isSetter()) return accessor;
        }

        return null;
    }

    @Nullable
    public JetExpression getInitializer() {
        PsiElement eq = findChildByType(EQ);
        return PsiTreeUtil.getNextSiblingOfType(eq, JetExpression.class);
    }

    @NotNull
    public ASTNode getValOrVarNode() {
        return getNode().findChildByType(TokenSet.create(VAL_KEYWORD, VAR_KEYWORD));
    }
}