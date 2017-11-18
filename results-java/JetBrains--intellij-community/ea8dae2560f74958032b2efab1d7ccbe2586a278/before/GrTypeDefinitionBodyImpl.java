package org.jetbrains.plugins.groovy.lang.psi.impl.statements.typedef.bodies;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariableDeclaration;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrClassInitializer;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinitionBody;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMembersDeclaration;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.psi.impl.GroovyPsiElementImpl;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.GrFieldImpl;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * @author: Dmitry.Krasilschikov, ilyas
 * @date: 04.05.2007
 */
public class GrTypeDefinitionBodyImpl extends GroovyPsiElementImpl implements GrTypeDefinitionBody {
  private GrField[] myFields;
  public GrTypeDefinitionBodyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void subtreeChanged() {
    super.subtreeChanged();
    myFields = null;
    for (GrField field : getFields()) {
      ((GrFieldImpl) field).clearCaches();
    }
  }

  public void accept(GroovyElementVisitor visitor) {
    visitor.visitTypeDefinitionBody(this);
  }

  public String toString() {
    return "Type definition body";
  }

  public GrField[] getFields() {
    if (myFields == null) {
      GrVariableDeclaration[] declarations = findChildrenByClass(GrVariableDeclaration.class);
      if (declarations.length == 0) return GrField.EMPTY_ARRAY;
      List<GrField> result = new ArrayList<GrField>();
      for (GrVariableDeclaration declaration : declarations) {
        GrVariable[] variables = declaration.getVariables();
        for (GrVariable variable : variables) {
          result.add((GrField) variable);
        }
      }
      myFields = result.toArray(new GrField[result.size()]);
    }

    return myFields;
  }

  public GrMethod[] getGroovyMethods() {
    return findChildrenByClass(GrMethod.class);
  }

  public PsiMethod[] getMethods() {
    GrMethod[] groovyMethods = getGroovyMethods();
    GrField[] fields = getFields();
    if (fields.length == 0) return groovyMethods;
    List<PsiMethod> result = new ArrayList<PsiMethod>();
    result.addAll(Arrays.asList(groovyMethods));
    for (GrField field : fields) {
      if (field.isProperty()) {
        PsiMethod getter = field.getGetter();
        if (getter != null) result.add(getter);
        PsiMethod setter = field.getSetter();
        if (setter != null) result.add(setter);
      }
    }

    return result.toArray(new PsiMethod[result.size()]);
  }

  public GrMembersDeclaration[] getMemberDeclarations() {
    return findChildrenByClass(GrMembersDeclaration.class);
  }

  @Nullable
  public PsiElement getLBrace() {
    return findChildByType(GroovyTokenTypes.mLCURLY);
  }

  @Nullable
  public PsiElement getRBrace() {
    return findChildByType(GroovyTokenTypes.mRCURLY);
  }

  @NotNull
  public GrClassInitializer[] getInitializers() {
    return findChildrenByClass(GrClassInitializer.class);
  }


}