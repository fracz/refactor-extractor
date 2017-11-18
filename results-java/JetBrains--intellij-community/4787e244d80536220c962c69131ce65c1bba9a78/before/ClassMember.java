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

      if (tWRONG_SET.contains(ModifiersOptional.parse(builder))) {
        return WRONGWAY;
      }

      if (tWRONG_SET.contains(MethodDefinition.parse(builder))) {
        return WRONGWAY;
      }

      return METHOD_DEFINITION;
    }
    constructorStartMarker.drop();

    //declaration
    PsiBuilder.Marker declMarker = builder.mark();
    if (DeclarationStart.parse(builder)) {
      declMarker.rollbackTo();
      return Declaration.parse(builder);
    }
    declMarker.drop();

    //type definition
    PsiBuilder.Marker typeDeclStartMarker = builder.mark();
    if (TypeDeclarationStart.parse(builder)) {
      typeDeclStartMarker.rollbackTo();

      IElementType typeDef = TypeDefinition.parse(builder);
      if (tWRONG_SET.contains(typeDef)) {
        return WRONGWAY;
      }
      return typeDef;
    }
    typeDeclStartMarker.drop();

    //static compound statement
    if (ParserUtils.getToken(builder, kSTATIC)) {
      if (!tWRONG_SET.contains(OpenBlock.parse(builder))) {
        return STATIC_COMPOUND_STATEMENT;
      } else {
        builder.error(GroovyBundle.message("compound.statemenet.expected"));
        return WRONGWAY;
      }
    }

    if (!tWRONG_SET.contains(OpenBlock.parse(builder))) {
      return COMPOUND_STATEMENT;
    }

    return WRONGWAY;

  }
}