package com.intellij.psi.impl.source;

import com.intellij.lexer.JavaLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.command.impl.DocumentReferenceByDocument;
import com.intellij.openapi.command.undo.DocumentReference;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.command.undo.UndoableAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.ElementType;
import com.intellij.psi.scope.ElementClassHint;
import com.intellij.psi.scope.NameHint;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.scope.util.PsiScopesUtil;
import com.intellij.psi.tree.IElementType;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

public class PsiCodeFragmentImpl extends PsiFileImpl implements PsiCodeFragment, PsiImportHolder {
  private PsiElement myContext;
  private boolean myPhysical;
  private PsiType myThisType;
  private LinkedHashMap<String, String> myPseudoImports = new LinkedHashMap<String, String>();
  private VisibilityChecker myVisibilityChecker;

  public PsiCodeFragmentImpl(Project project,
                             IElementType contentElementType,
                             boolean isPhysical,
                             String name,
                             CharSequence text) {
    super(project, CODE_FRAGMENT, contentElementType, name, text);
    myPhysical = isPhysical;
  }

  protected PsiCodeFragmentImpl clone() {
    PsiCodeFragmentImpl clone = (PsiCodeFragmentImpl)super.clone();
    clone.myPhysical = false;
    clone.myOriginalFile = this;
    clone.myPseudoImports = new LinkedHashMap<String, String>(myPseudoImports);
    return clone;
  }

  public boolean isValid() {
    if (!super.isValid()) return false;
    if (myContext != null && !myContext.isValid()) return false;
    return true;
  }

  public boolean canContainJavaCode() {
    return true;
  }

  public FileType getFileType() {
    return StdFileTypes.JAVA;
  }


  public PsiElement getContext() {
    return myContext;
  }

  public void setContext(PsiElement context) {
    this.myContext = context;
  }

  public void setEverythingAcessible(boolean value) {
    myVisibilityChecker = new VisibilityChecker() {
      public Visibility isDeclarationVisible(PsiElement declaration, PsiElement place) {
        return Visibility.VISIBLE;
      }
    };
  }

  public PsiType getThisType() {
    return myThisType;
  }

  public void setThisType(PsiType psiType) {
    myThisType = psiType;
  }

  public String importsToString() {
    StringBuffer buffer = new StringBuffer();
    for (Iterator<String> iterator = myPseudoImports.values().iterator(); iterator.hasNext();) {
      if (buffer.length() > 0) {
        buffer.append(",");
      }
      String importedQName = iterator.next();
      buffer.append(importedQName);
    }
    return buffer.toString();
  }

  public void addImportsFromString(String imports) {
    StringTokenizer tokenizer = new StringTokenizer(imports, ",");
    while(tokenizer.hasMoreTokens()){
      String qName = tokenizer.nextToken();
      String name = PsiNameHelper.getShortClassName(qName);
      myPseudoImports.put(name, qName);
    }
  }

  public void setVisibilityChecker(VisibilityChecker checker) {
    myVisibilityChecker = checker;
  }

  public VisibilityChecker getVisibilityChecker() {
    return myVisibilityChecker;
  }

  public boolean isPhysical() {
    return myPhysical;
  }

  /**
   * @fabrique
   */
  public void setPhysical(boolean physical) {
    myPhysical = physical;
  }

  public void accept(PsiElementVisitor visitor) {
    visitor.visitCodeFragment(this);
  }

  public boolean processDeclarations(PsiScopeProcessor processor,
                                     PsiSubstitutor substitutor,
                                     PsiElement lastParent,
                                     PsiElement place) {
    final ElementClassHint classHint = processor.getHint(ElementClassHint.class);

    if (classHint == null || classHint.shouldProcess(PsiClass.class)) {
      final NameHint nameHint = processor.getHint(NameHint.class);
      final String name = nameHint != null ? nameHint.getName() : null;
      if (name != null) {
        String qNameImported = myPseudoImports.get(name);
        if (qNameImported != null) {
          PsiClass imported = myManager.findClass(qNameImported, getResolveScope());
          if (imported != null) {
            if (!processor.execute(imported, substitutor)) return false;
          }
        }
      }
      else {
        Iterator<String> iter = myPseudoImports.values().iterator();
        while (iter.hasNext()) {
          String qNameImported = iter.next();
          PsiClass aClass = myManager.findClass(qNameImported, getResolveScope());
          if (aClass != null) {
            if (!processor.execute(aClass, substitutor)) return false;
          }
        }
      }

      PsiPackage langPackage = getManager().findPackage("java.lang");
      if (langPackage != null) {
        if (!langPackage.processDeclarations(processor, substitutor, null, place)) return false;
      }

      PsiPackage defaultPackage = getManager().findPackage("");
      if (defaultPackage != null) {
        if (!defaultPackage.processDeclarations(processor, substitutor, null, place)) return false;
      }
    }


    IElementType i = myContentElementType;
    if (i == ElementType.TYPE_TEXT || i == ElementType.EXPRESSION_TEXT || i == ElementType.REFERENCE_TEXT) {
      return true;
    } else {
      {
        processor.handleEvent(PsiScopeProcessor.Event.SET_DECLARATION_HOLDER, this);
        if (lastParent == null)
        // Parent element should not see our vars
        {
          return true;
        }

        return PsiScopesUtil.walkChildrenScopes(this, processor, substitutor, lastParent, place);
      }
    }
  }

  public String toString() {
    return "PsiCodeFragment:" + getName();
  }

  public Lexer createLexer() {
    final PsiManager manager = getManager();
    return new JavaLexer(manager.getEffectiveLanguageLevel());
  }

  public boolean importClass(PsiClass aClass) {
    final String className = aClass.getName();
    final String qName = aClass.getQualifiedName();
    if (qName == null) return false;
    //if (!myPseudoImports.containsKey(className)){
    myPseudoImports.put(className, qName);
    myManager.nonPhysicalChange(); // to clear resolve caches!
    if (isPhysical()) {
      final Project project = myManager.getProject();
      final Document document = PsiDocumentManager.getInstance(project).getDocument(this);
      UndoManager.getInstance(project).undoableActionPerformed(new ImportClassUndoableAction(className, qName, document, myPseudoImports));
    }
    return true;
    //}
    //else{
    //  return false;
    //}
  }

  private static class ImportClassUndoableAction implements UndoableAction {
    private final String myClassName;
    private final String myQName;
    private LinkedHashMap<String,String> myPseudoImports;
    private Document myDocument;

    public ImportClassUndoableAction(final String className,
                                     final String qName,
                                     final Document document,
                                     final LinkedHashMap<String, String> pseudoImportsMap) {
      myClassName = className;
      myQName = qName;
      myDocument = document;
      myPseudoImports = pseudoImportsMap;
    }

    public boolean isComplex() {
      return false;
    }

    public void undo() {
      myPseudoImports.remove(myClassName);
    }

    public void redo() {
      myPseudoImports.put(myClassName, myQName);
    }

    public DocumentReference[] getAffectedDocuments() {
      Document document = myDocument;
      return new DocumentReference[]{DocumentReferenceByDocument.createDocumentReference(document)};
    }
  }
}