/*
 * Copyright 2000-2007 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.groovy.refactoring;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiNameHelperImpl;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyLexer;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;

/**
 * @author ilyas
 */
public class GroovyPsiNameHelperImpl extends PsiNameHelperImpl {

  private final PsiManager myManager;
  private final Lexer myLexer;

  private final Object VEN = new Object();

  public GroovyPsiNameHelperImpl(PsiManager manager) {
    super(manager);
    myManager = manager;
    myLexer = new GroovyLexer();
  }


  public boolean isIdentifier(String text) {
    ApplicationManager.getApplication().assertReadAccessAllowed();
    if (text == null) return false;

    synchronized (VEN) {
      myLexer.start(text, 0, text.length(), 0);
      if (myLexer.getTokenType() != GroovyTokenTypes.mIDENT) return false;
      myLexer.advance();
      return myLexer.getTokenType() == null;
    }
  }

  public boolean isKeyword(String text) {
    ApplicationManager.getApplication().assertReadAccessAllowed();

    synchronized (VEN) {
      myLexer.start(text,0,text.length(),0);
      if (myLexer.getTokenType() == null || !GroovyTokenTypes.KEYWORDS.contains(myLexer.getTokenType())) return false;
      myLexer.advance();
      return myLexer.getTokenType() == null;
    }
  }





}