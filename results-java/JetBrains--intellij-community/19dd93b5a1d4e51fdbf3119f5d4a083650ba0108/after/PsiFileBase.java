package com.intellij.extapi.psi;

import com.intellij.lang.Language;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lang.impl.PsiBuilderImpl;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.psi.tree.IElementType;

/**
 * Created by IntelliJ IDEA.
 * User: max
 * Date: Jan 25, 2005
 * Time: 9:40:47 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class PsiFileBase extends PsiFileImpl {
  private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.source.PsiFileBase");

  private final static IElementType FILE_TEXT_CHAMELEON = new IElementType("FILE_TEXT_CHAMELEON", null); // Shouldn't happen to be used.
  private final Language myLanguage;

  protected PsiFileBase(Project project,
                        VirtualFile file,
                        final Language language) {
    super(project, language.getParserDefinition().getFileNodeType(), FILE_TEXT_CHAMELEON, file);
    myLanguage = language;
  }

  protected PsiFileBase(Project project,
                        String name,
                        CharSequence text,
                        final Language language) {
    super(project, _createFileElement(text, language), language.getParserDefinition().getFileNodeType(), FILE_TEXT_CHAMELEON, name);
    myLanguage = language;
  }

  public final Language getLanguage() {
    return myLanguage;
  }

  public final Lexer createLexer() {
    return myLanguage.getParserDefinition().createLexer();
  }

  protected final FileElement createFileElement(final CharSequence docText) {
    return _createFileElement(docText, myLanguage);
  }

  private static FileElement _createFileElement(final CharSequence docText, final Language language) {
    final ParserDefinition parserDefinition = language.getParserDefinition();
    final PsiParser parser = parserDefinition.createParser();
    final IElementType root = parserDefinition.getFileNodeType();
    final PsiBuilderImpl builder = new PsiBuilderImpl(language, null, docText);
    final FileElement fileRoot = (FileElement) parser.parse(root, builder);
    LOG.assertTrue(fileRoot.getElementType() == root, "Parsing file text returns rootElement with type different from declared in parser definition");
    return fileRoot;
  }

  public void accept(PsiElementVisitor visitor) {
    visitor.visitFile(this);
  }
}