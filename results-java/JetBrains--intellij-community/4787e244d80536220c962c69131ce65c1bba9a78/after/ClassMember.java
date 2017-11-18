package org.jetbrains.plugins.groovy.lang.parser.parsing.statements.typeDefinitions.members;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.plugins.groovy.GroovyBundle;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.parser.parsing.auxiliary.modifiers.ModifiersOptional;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.blocks.OpenBlock;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.constructor.ConstructorStart;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.declaration.Declaration;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.declaration.DeclarationStart;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.typeDefinitions.TypeDefinition;
import org.jetbrains.plugins.groovy.lang.parser.parsing.types.TypeDeclarationStart;
import org.jetbrains.plugins.groovy.lang.parser.parsing.util.ParserUtils;

/**
 * @author: Dmitry.Krasilschikov
 * @date: 20.03.2007
 */
public class ClassMember implements GroovyElementTypes {
  public static IElementType parse(PsiBuilder builder) {
    //constructor
    PsiBuilder.Marker constructorStartMarker = builder.mark();
    if (ConstructorStart.parse(builder)) {
      constructorStartMarker.rollbackTo();

      PsiBuilder.Marker cmMarker = builder.mark();

      if (WRONGWAY.equals(ModifiersOptional.parse(builder))) {
        cmMarker.drop();
        return WRONGWAY;
      }

      IElementType methodDef = MethodDefinition.parse(builder);
      if (WRONGWAY.equals(methodDef)) {
        cmMarker.drop();
        return WRONGWAY;
      }

      cmMarker.done(methodDef);
      return methodDef;
    }
    constructorStartMarker.rollbackTo();

    //declaration
    PsiBuilder.Marker declMarker = builder.mark();
    if (DeclarationStart.parse(builder)) {
      declMarker.rollbackTo();
      return Declaration.parse(builder);
    }
    declMarker.rollbackTo();

    //type definition
    PsiBuilder.Marker typeDeclStartMarker = builder.mark();
    if (TypeDeclarationStart.parse(builder)) {
      typeDeclStartMarker.rollbackTo();

      IElementType typeDef = TypeDefinition.parse(builder);
      if (WRONGWAY.equals(typeDef)) {
        return WRONGWAY;
      }
      return typeDef;
    }
    typeDeclStartMarker.rollbackTo();

    //static compound statement
    if (ParserUtils.getToken(builder, kSTATIC)) {
      if (!WRONGWAY.equals(OpenBlock.parse(builder))) {
        return STATIC_COMPOUND_STATEMENT;
      } else {
        builder.error(GroovyBundle.message("compound.statemenet.expected"));
        return WRONGWAY;
      }
    }

    if (!WRONGWAY.equals(OpenBlock.parse(builder))) {
      return COMPOUND_STATEMENT;
    }

    return WRONGWAY;
  }
}