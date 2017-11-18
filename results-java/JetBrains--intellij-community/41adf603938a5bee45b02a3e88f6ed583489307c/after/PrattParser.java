/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.lang.pratt;

import com.intellij.lang.PsiParser;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
*/
public abstract class PrattParser implements PsiParser {
  @NotNull
  public final ASTNode parse(final IElementType root, final PsiBuilder builder) {
    //builder.setDebugMode(true);

    final PrattBuilderImpl prattBuilder = PrattBuilderImpl.createBuilder(builder);
    final MutableMarker marker = prattBuilder.mark();
    parse(prattBuilder);
    marker.finish(root);
    return builder.getTreeBuilt();
  }

  protected void parse(final PrattBuilderImpl builder) {
    builder.parse();
    while (!builder.isEof()) builder.advance();
  }
}