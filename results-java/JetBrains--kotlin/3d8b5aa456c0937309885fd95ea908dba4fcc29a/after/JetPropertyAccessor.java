package org.jetbrains.jet.lang.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.JetNodeTypes;
import org.jetbrains.jet.lexer.JetTokens;

import java.util.Collections;
import java.util.List;

/**
 * @author max
 */
public class JetPropertyAccessor extends JetDeclaration implements JetDeclarationWithBody, JetModifierListOwner {
    public JetPropertyAccessor(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull JetVisitorVoid visitor) {
        visitor.visitPropertyAccessor(this);
    }

    @Override
    public <R, D> R accept(@NotNull JetVisitor<R, D> visitor, D data) {
        return visitor.visitPropertyAccessor(this, data);
    }

    public boolean isSetter() {
        return findChildByType(JetTokens.SET_KEYWORD) != null;
    }

    public boolean isGetter() {
        return findChildByType(JetTokens.GET_KEYWORD) != null;
    }

    @Nullable
    public JetParameter getParameter() {
        JetParameterList parameterList = (JetParameterList) findChildByType(JetNodeTypes.VALUE_PARAMETER_LIST);
        if (parameterList == null) return null;
        List<JetParameter> parameters = parameterList.getParameters();
        if (parameters.isEmpty()) return null;
        return parameters.get(0);
    }

    @NotNull
    @Override
    public List<JetParameter> getValueParameters() {
        JetParameter parameter = getParameter();
        if (parameter == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(parameter);
    }

    @Nullable
    @Override
    public JetExpression getBodyExpression() {
        return findChildByClass(JetExpression.class);
    }

    @Override
    public boolean hasBlockBody() {
        return findChildByType(JetTokens.EQ) == null;
    }

    @Override
    public boolean hasDeclaredReturnType() {
        return true;
    }

    @NotNull
    @Override
    public JetElement asElement() {
        return this;
    }

    @Nullable
    public JetTypeReference getReturnTypeReference() {
        return findChildByClass(JetTypeReference.class);
    }

    @NotNull
    public PsiElement getNamePlaceholder() {
        PsiElement get = findChildByType(JetTokens.GET_KEYWORD);
        if (get != null) {
            return get;
        }
        return findChildByType(JetTokens.SET_KEYWORD);
    }
}