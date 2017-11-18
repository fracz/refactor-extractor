package com.jetbrains.python.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBuilder;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.tree.IStubFileElementType;
import com.jetbrains.python.lexer.PythonIndentingLexer;
import com.jetbrains.python.parsing.PyParser;
import com.jetbrains.python.parsing.StatementParsing;
import com.jetbrains.python.psi.impl.stubs.PyFileStubBuilder;
import com.jetbrains.python.psi.impl.stubs.PyFileStubImpl;
import com.jetbrains.python.psi.stubs.PyFileStub;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
* @author yole
*/
public class PyFileElementType extends IStubFileElementType<PyFileStub> {
  public PyFileElementType(Language language) {
    super(language);
  }

  @Override
  public StubBuilder getBuilder() {
    return new PyFileStubBuilder();
  }

  @Override
  public int getStubVersion() {
    return 30;
  }

  @Override
  public ASTNode parseContents(ASTNode chameleon) {
    final FileElement node = (FileElement)chameleon;
    final LanguageLevel languageLevel = getLanguageLevel(node.getPsi());
    final Lexer lexer = new PythonIndentingLexer();

    final Project project = chameleon.getPsi().getProject();
    final PsiBuilderFactory factory = PsiBuilderFactory.getInstance();

    final PsiBuilder builder = factory.createBuilder(project, chameleon, lexer, getLanguage(), chameleon.getChars());

    final PyParser parser = new PyParser(languageLevel);
    if (languageLevel == LanguageLevel.PYTHON26 &&
        node.getPsi().getContainingFile().getName().equals("__builtin__.py")) {
      parser.setFutureFlag(StatementParsing.FUTURE.PRINT_FUNCTION);
    }

    return parser.parse(this, builder).getFirstChildNode();
  }

  private static LanguageLevel getLanguageLevel(PsiElement psi) {
    final PsiFile file = psi.getContainingFile();
    if (!(file instanceof PyFile)) {
      final PsiElement context = file.getContext();
      if (context != null) return getLanguageLevel(context);
      return LanguageLevel.getDefault();
    }
    return ((PyFile)file).getLanguageLevel();
  }

  @Override
  public String getExternalId() {
    return "python.FILE";
  }

  @Override
  public void serialize(PyFileStub stub, StubOutputStream dataStream) throws IOException {
    writeNullableList(dataStream, stub.getDunderAll());
    dataStream.writeBoolean(stub.isAbsoluteImportEnabled());
  }

  @Override
  public PyFileStub deserialize(StubInputStream dataStream, StubElement parentStub) throws IOException {
    List<String> all = readNullableList(dataStream);
    boolean isAbsoluteImportEnabled = dataStream.readBoolean();
    return new PyFileStubImpl(all, isAbsoluteImportEnabled);
  }

  public static void writeNullableList(StubOutputStream dataStream, final List<String> names) throws IOException {
    if (names == null) {
      dataStream.writeBoolean(false);
    }
    else {
      dataStream.writeBoolean(true);
      dataStream.writeVarInt(names.size());
      for(String name: names) {
        dataStream.writeName(name);
      }
    }
  }

  @Nullable
  public static List<String> readNullableList(StubInputStream dataStream) throws IOException {
    boolean hasNames = dataStream.readBoolean();
    List<String> names = null;
    if (hasNames) {
      int size = dataStream.readVarInt();
      names = new ArrayList<String>(size);
      for(int i=0; i<size; i++) {
        names.add(dataStream.readName().getString());
      }
    }
    return names;
  }
}