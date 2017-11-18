package com.jetbrains.python.inspections;

import com.google.common.collect.ImmutableSet;
import com.intellij.codeInspection.*;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.codeInspection.ui.ListEditForm;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.JDOMExternalizableStringList;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import com.intellij.util.PlatformUtils;
import com.jetbrains.cython.CythonLanguageDialect;
import com.jetbrains.cython.CythonNames;
import com.jetbrains.cython.psi.CythonFile;
import com.jetbrains.cython.types.CythonBuiltinType;
import com.jetbrains.cython.types.CythonType;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.actions.*;
import com.jetbrains.python.codeInsight.PyCodeInsightSettings;
import com.jetbrains.python.codeInsight.controlflow.ScopeOwner;
import com.jetbrains.python.codeInsight.dataflow.scope.ScopeUtil;
import com.jetbrains.python.codeInsight.imports.AutoImportHintAction;
import com.jetbrains.python.codeInsight.imports.AutoImportQuickFix;
import com.jetbrains.python.codeInsight.imports.OptimizeImportsQuickFix;
import com.jetbrains.python.codeInsight.imports.PythonReferenceImporter;
import com.jetbrains.python.console.PydevConsoleRunner;
import com.jetbrains.python.documentation.DocStringParameterReference;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.impl.PyImportReferenceImpl;
import com.jetbrains.python.psi.impl.PyImportStatementNavigator;
import com.jetbrains.python.psi.impl.PyOperatorReferenceImpl;
import com.jetbrains.python.psi.resolve.ImportedResolveResult;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.resolve.ResolveImportUtil;
import com.jetbrains.python.psi.types.*;
import com.jetbrains.python.sdk.PythonSdkType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

/**
 * Marks references that fail to resolve. Also tracks unused imports and provides "optimize imports" support.
 * User: dcheryasov
 * Date: Nov 15, 2008
 */
public class PyUnresolvedReferencesInspection extends PyInspection {
  private static Key<Visitor> KEY = Key.create("PyUnresolvedReferencesInspection.Visitor");

  public JDOMExternalizableStringList ignoredIdentifiers = new JDOMExternalizableStringList();

  public static PyUnresolvedReferencesInspection getInstance(PsiElement element) {
    final InspectionProfile inspectionProfile = InspectionProjectProfileManager.getInstance(element.getProject()).getInspectionProfile();
    final LocalInspectionToolWrapper profileEntry =
      (LocalInspectionToolWrapper)inspectionProfile.getInspectionTool(PyUnresolvedReferencesInspection.class.getSimpleName(), element);
    return (PyUnresolvedReferencesInspection)profileEntry.getTool();
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return PyBundle.message("INSP.NAME.unresolved.refs");
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder,
                                        final boolean isOnTheFly,
                                        @NotNull final LocalInspectionToolSession session) {
    final Visitor visitor = new Visitor(holder, session, ignoredIdentifiers);
    // buildVisitor() will be called on injected files in the same session - don't overwrite if we already have one
    final Visitor existingVisitor = session.getUserData(KEY);
    if (existingVisitor == null) {
      session.putUserData(KEY, visitor);
    }
    return visitor;
  }

  @Override
  public void inspectionFinished(LocalInspectionToolSession session, ProblemsHolder holder) {
    final Visitor visitor = session.getUserData(KEY);
    assert visitor != null;
    if (PyCodeInsightSettings.getInstance().HIGHLIGHT_UNUSED_IMPORTS) {
      visitor.highlightUnusedImports();
    }
    session.putUserData(KEY, null);
  }

  @Override
  public JComponent createOptionsPanel() {
    ListEditForm form = new ListEditForm("Ignore identifiers", ignoredIdentifiers);
    return form.getContentPanel();
  }

  public static class Visitor extends PyInspectionVisitor {
    private Set<PsiElement> myUsedImports = Collections.synchronizedSet(new HashSet<PsiElement>());
    private Set<NameDefiner> myAllImports = Collections.synchronizedSet(new HashSet<NameDefiner>());
    private final ImmutableSet<String> myIgnoredIdentifiers;

    public Visitor(@Nullable ProblemsHolder holder, @NotNull LocalInspectionToolSession session, List<String> ignoredIdentifiers) {
      super(holder, session);
      myIgnoredIdentifiers = ImmutableSet.copyOf(ignoredIdentifiers);
    }

    @Override
    public void visitPyFile(PyFile node) {
      super.visitPyFile(node);
      if (PlatformUtils.isPyCharm()) {
        final Module module = ModuleUtil.findModuleForPsiElement(node);
        if (module != null && PythonSdkType.findPythonSdk(module) == null && PlatformUtils.isPyCharm()) {
          registerProblem(node, "No Python interpreter configured for the project", new ConfigureInterpreterFix());
        }
      }
    }

    @Override
    public void visitPyTargetExpression(PyTargetExpression node) {
      checkSlots(node);
    }

    private void checkSlots(PyQualifiedExpression node) {
      final PyExpression qualifier = node.getQualifier();
      if (qualifier != null) {
        final PyType type = myTypeEvalContext.getType(qualifier);
        if (type instanceof PyClassType) {
          final PyClass pyClass = ((PyClassType)type).getPyClass();
          if (pyClass != null && pyClass.isNewStyleClass()) {
            final List<String> slots = pyClass.getSlots();
            if (slots != null && !slots.contains(node.getReferencedName()) && !slots.contains("__dict__")) {
              final ASTNode nameNode = node.getNameElement();
              final PsiElement e = nameNode != null ? nameNode.getPsi() : node;
              registerProblem(e, "'" + pyClass.getName() + "' object has no attribute '" + node.getReferencedName() + "'");
            }
          }
        }
      }
    }

    @Override
    public void visitPyImportElement(PyImportElement node) {
      super.visitPyImportElement(node);
      final PyFromImportStatement fromImport = PsiTreeUtil.getParentOfType(node, PyFromImportStatement.class);
      if (fromImport == null || !fromImport.isFromFuture()) {
        myAllImports.add(node);
      }
    }

    @Override
    public void visitPyStarImportElement(PyStarImportElement node) {
      super.visitPyStarImportElement(node);
      myAllImports.add(node);
    }

    @Nullable
    private static PyExceptPart getImportErrorGuard(PyElement node) {
      final PyImportStatementBase importStatement = PsiTreeUtil.getParentOfType(node, PyImportStatementBase.class);
      if (importStatement != null) {
        final PyTryPart tryPart = PsiTreeUtil.getParentOfType(node, PyTryPart.class);
        if (tryPart != null) {
          final PyTryExceptStatement tryExceptStatement = PsiTreeUtil.getParentOfType(tryPart, PyTryExceptStatement.class);
          if (tryExceptStatement != null) {
            for (PyExceptPart exceptPart : tryExceptStatement.getExceptParts()) {
              final PyExpression expr = exceptPart.getExceptClass();
              if (expr != null && "ImportError".equals(expr.getName())) {
                return exceptPart;
              }
            }
          }
        }
      }
      return null;
    }

    private static boolean isGuardedByHasattr(@NotNull final PyElement node, @NotNull final String name) {
      final String nodeName = node.getName();
      if (nodeName != null) {
        final ScopeOwner owner = ScopeUtil.getDeclarationScopeOwner(node, nodeName);
        PyElement e = PsiTreeUtil.getParentOfType(node, PyConditionalStatementPart.class, PyConditionalExpression.class);
        while (e != null && PsiTreeUtil.isAncestor(owner, e, true)) {
          final ArrayList<PyCallExpression> calls = new ArrayList<PyCallExpression>();
          PyExpression cond = null;
          if (e instanceof PyConditionalStatementPart) {
            cond = ((PyConditionalStatementPart)e).getCondition();
          }
          else if (e instanceof PyConditionalExpression && PsiTreeUtil.isAncestor(((PyConditionalExpression)e).getTruePart(), node, true)) {
            cond = ((PyConditionalExpression)e).getCondition();
          }
          if (cond instanceof PyCallExpression) {
            calls.add((PyCallExpression)cond);
          }
          if (cond != null) {
            final PyCallExpression[] callExprs = PsiTreeUtil.getChildrenOfType(cond, PyCallExpression.class);
            if (callExprs != null) {
              calls.addAll(Arrays.asList(callExprs));
            }
            for (PyCallExpression call : calls) {
              final PyExpression callee = call.getCallee();
              final PyExpression[] args = call.getArguments();
              // TODO: Search for `node` aliases using aliases analysis
              if (callee != null && "hasattr".equals(callee.getName()) && args.length == 2 &&
                  nodeName.equals(args[0].getName()) && args[1] instanceof PyStringLiteralExpression &&
                  ((PyStringLiteralExpression)args[1]).getStringValue().equals(name)) {
                return true;
              }
            }
          }
          e = PsiTreeUtil.getParentOfType(e, PyConditionalStatementPart.class);
        }
      }
      return false;
    }

    @Override
    public void visitPyElement(final PyElement node) {
      super.visitPyElement(node);
      final PsiFile file = node.getContainingFile();
      if (file instanceof CythonFile && ((CythonFile)file).isIncludeFile()) {
        return;
      }
      if (node instanceof PyReferenceOwner) {
        final PyResolveContext resolveContext = PyResolveContext.noImplicits().withTypeEvalContext(myTypeEvalContext);
        processReference(node, ((PyReferenceOwner)node).getReference(resolveContext));
      }
      else {
        for (final PsiReference reference : node.getReferences()) {
          processReference(node, reference);
        }
      }
    }

    private void processReference(PyElement node, @Nullable PsiReference reference) {
      if (reference == null || reference.isSoft()) return;
      HighlightSeverity severity = HighlightSeverity.ERROR;
      if (reference instanceof PsiReferenceEx) {
        severity = ((PsiReferenceEx) reference).getUnresolvedHighlightSeverity(myTypeEvalContext);
        if (severity == null) return;
      }
      PyExceptPart guard = getImportErrorGuard(node);
      if (guard != null) {
        processReferenceInImportGuard(node, guard);
        return;
      }
      if (node instanceof PyQualifiedExpression) {
        final PyQualifiedExpression qExpr = (PyQualifiedExpression)node;
        final PyExpression qualifier = qExpr.getQualifier();
        final String name = node.getName();
        if (qualifier != null && name != null && isGuardedByHasattr(qualifier, name)) {
          return;
        }
      }
      PsiElement target = null;
      boolean unresolved;
      if (reference instanceof PsiPolyVariantReference) {
        final PsiPolyVariantReference poly = (PsiPolyVariantReference)reference;
        final ResolveResult[] resolveResults = poly.multiResolve(false);
        unresolved = (resolveResults.length == 0);
        for (ResolveResult resolveResult : resolveResults) {
          if (target == null && resolveResult.isValidResult()) {
            target = resolveResult.getElement();
          }
          if (resolveResult instanceof ImportedResolveResult) {
            myUsedImports.addAll(((ImportedResolveResult)resolveResult).getNameDefiners());
          }
        }
      }
      else {
        target = reference.resolve();
        unresolved = (target == null);
      }
      if (unresolved) {
        registerUnresolvedReferenceProblem(node, reference, severity);
        // don't highlight unresolved imports as unused
        if (node.getParent() instanceof PyImportElement) {
          myAllImports.remove(node.getParent());
        }
      }
      else if (reference instanceof PyImportReferenceImpl &&
               target == reference.getElement().getContainingFile()) {
        final boolean insideFromImport = PsiTreeUtil.getParentOfType(node, PyFromImportStatement.class) != null;
        if ((insideFromImport && PyImportStatementNavigator.getImportStatementByElement(node) != null) || !insideFromImport) {
          registerProblem(node, "Import resolves to its containing file");
        }
      }
    }

    private void processReferenceInImportGuard(PyElement node, PyExceptPart guard) {
      final PyImportElement importElement = PsiTreeUtil.getParentOfType(node, PyImportElement.class);
      if (importElement != null) {
        Collection<PsiElement> allWrites = ScopeUtil.getReadWriteElements(importElement.getVisibleName(),
                                                                          ScopeUtil.getScopeOwner(importElement),
                                                                          false, true);
        Collection<PsiElement> writesInsideGuard = new ArrayList<PsiElement>();
        for (PsiElement write : allWrites) {
          if (PsiTreeUtil.isAncestor(guard, write, false)) {
            writesInsideGuard.add(write);
          }
        }
        if (writesInsideGuard.isEmpty()) {
          final PyTargetExpression asElement = importElement.getAsNameElement();
          final PyElement toHighlight = asElement != null ? asElement : node;
          registerProblem(toHighlight,
                          PyBundle.message("INSP.try.except.import.error",
                                           importElement.getVisibleName()),
                          ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, null);
        }
      }
    }

    private void registerUnresolvedReferenceProblem(final PyElement node, final PsiReference reference, HighlightSeverity severity) {
      final StringBuilder descriptionBuf = new StringBuilder(""); // TODO: clear descriptionBuf logic. maybe a flag is needed instead.
      final String text = reference.getElement().getText();
      final String ref_text = reference.getRangeInElement().substring(text); // text of the part we're working with
      final PsiElement element = reference.getElement();
      final boolean ref_is_importable = PythonReferenceImporter.isImportable(element);
      final List<LocalQuickFix> actions = new ArrayList<LocalQuickFix>(2);
      if (ref_text.length() <= 0) return; // empty text, nothing to highlight
      final String refname = (element instanceof PyQualifiedExpression) ? ((PyQualifiedExpression)element).getReferencedName() : ref_text;
      if (element instanceof PyReferenceExpression) {
        PyReferenceExpression refex = (PyReferenceExpression)element;
        if (myIgnoredIdentifiers.contains(refname) || PyNames.COMPARISON_OPERATORS.contains(refname)) {
          return;
        }
        if (CythonLanguageDialect.isInsideCythonFile(element) && CythonNames.BUILTINS.contains(text)) {
          return;
        }
        if (refex.getQualifier() != null) {
          final PyClassType object_type = PyBuiltinCache.getInstance(node).getObjectType();
          if ((object_type != null) && object_type.getPossibleInstanceMembers().contains(refname)) return;

        }
        else {
          if (LanguageLevel.forElement(node).isOlderThan(LanguageLevel.PYTHON26)) {
            if (refname.equals("with")) {
              actions.add(new UnresolvedRefAddFutureImportQuickFix(refex));
            }
          }
          PyClass containedClass = PsiTreeUtil.getParentOfType(node, PyClass.class);
          if (containedClass != null) {
            for (PyTargetExpression target : containedClass.getInstanceAttributes()) {
              if (Comparing.strEqual(node.getName(), target.getName())) {
                actions.add(new UnresolvedReferenceAddSelfQuickFix(refex));
              }
            }
            for (PyStatement statement : containedClass.getStatementList().getStatements()) {
              if (statement instanceof PyAssignmentStatement) {
                if (((PyAssignmentStatement)statement).getLeftHandSideExpression().getText().equals(refex.getText())) {
                  PyExpression callexpr = ((PyAssignmentStatement)statement).getAssignedValue();
                  if (callexpr instanceof PyCallExpression) {
                    PyType type = myTypeEvalContext.getType(callexpr);
                    if (type != null && type instanceof PyClassType) {
                      String name = ((PyCallExpression)callexpr).getCallee().getText();
                      if (name != null && name.equals("property"))
                        actions.add(new UnresolvedReferenceAddSelfQuickFix(refex));
                    }
                  }
                }
              }
            }
            for (PyFunction method : containedClass.getMethods()) {
              if (refex.getText().equals(method.getName())) {
                actions.add(new UnresolvedReferenceAddSelfQuickFix(refex));
              }
            }
          }
          PyCallExpression callExpression = PsiTreeUtil.getParentOfType(element, PyCallExpression.class);
          if (callExpression != null)
            actions.add(new UnresolvedRefCreateFunctionQuickFix(callExpression, refex));
        }
        // unqualified:
        // may be module's
        if (PyModuleType.getPossibleInstanceMembers().contains(refname)) return;
        // may be a "try: import ..."; not an error not to resolve
        if ((
          PsiTreeUtil.getParentOfType(
            PsiTreeUtil.getParentOfType(node, PyImportElement.class), PyTryExceptStatement.class, PyIfStatement.class
          ) != null
        )) {
          severity = HighlightSeverity.WEAK_WARNING;
          String errmsg = PyBundle.message("INSP.module.$0.not.found", ref_text);
          descriptionBuf.append(errmsg);
          // TODO: mark the node so that future references pointing to it won't result in a error, but in a warning
        }
      }
      if (reference instanceof DocStringParameterReference) {
        if (myIgnoredIdentifiers.contains(reference.getCanonicalText()))
          return;
      }
      if (reference instanceof PsiReferenceEx && descriptionBuf.length() == 0) {
        final String s = ((PsiReferenceEx)reference).getUnresolvedDescription();
        if (s != null) descriptionBuf.append(s);
      }
      if (descriptionBuf.length() == 0) {
        boolean marked_qualified = false;
        if (element instanceof PyQualifiedExpression) {
          final PyQualifiedExpression qexpr = (PyQualifiedExpression)element;
          if (myIgnoredIdentifiers.contains(ref_text) || PyNames.COMPARISON_OPERATORS.contains(qexpr.getReferencedName())) {
            return;
          }
          final PyExpression qualifier = qexpr.getQualifier();
          if (qualifier != null) {
            PyType qtype = myTypeEvalContext.getType(qualifier);
            if (qtype != null) {
              if (qtype instanceof PyNoneType || qtype instanceof PyTypeReference ||
                  (qtype instanceof PyUnionType && ((PyUnionType) qtype).isWeak()) ||
                  (qtype instanceof PyImportedModuleType)) {
                // this almost always means that we don't know the type, so don't show an error in this case
                return;
              }
              if (qtype instanceof PyClassType) {
                PyClass cls = ((PyClassType)qtype).getPyClass();
                if (cls != null) {
                  if (overridesGetAttr(cls)) {
                    return;
                  }
                  if (cls.findProperty(ref_text) != null) {
                    return; // a property exists but accessor is not found; other inspections handle this
                  }
                  if (! PyBuiltinCache.getInstance(node).hasInBuiltins(cls)) {
                    if (element.getParent() instanceof PyCallExpression) {
                      actions.add(new AddMethodQuickFix(ref_text, (PyClassType)qtype));
                    }
                    else if (!(reference instanceof PyOperatorReferenceImpl)) {
                      actions.add(new AddFieldQuickFix(ref_text, cls, "None"));
                    }
                  }
                }
                if (reference instanceof PyOperatorReferenceImpl) {
                  descriptionBuf.append(PyBundle.message("INSP.unresolved.operator.ref",
                                                          qtype.getName(), refname,
                                                          ((PyOperatorReferenceImpl)reference).getReadableOperatorName()));
                }
                else {
                  descriptionBuf.append(PyBundle.message("INSP.unresolved.ref.$0.for.class.$1", ref_text, qtype.getName()));
                }
                marked_qualified = true;
              }
              else if (qtype instanceof PyModuleType) {
                PsiFile file = ((PyModuleType)qtype).getModule();
                if (file instanceof PyFile) {
                  actions.add(new AddFunctionQuickFix(ref_text, (PyFile)file));
                }
              }
              else if (qtype instanceof CythonBuiltinType ||
                       (qtype instanceof CythonType && reference instanceof PyOperatorReferenceImpl)) {
                return;
              }
              else {
                descriptionBuf.append(PyBundle.message("INSP.cannot.find.$0.in.$1", ref_text, qtype.getName()));
                marked_qualified = true;
              }
            }
          }
        }
        if (! marked_qualified) {
          descriptionBuf.append(PyBundle.message("INSP.unresolved.ref.$0", ref_text));
          if (ref_text.equals("true") || ref_text.equals("false"))
            actions.add(new UnresolvedRefTrueFalseQuickFix(element));

          // look in other imported modules for this whole name
          if (ref_is_importable) {
            AutoImportQuickFix importFix = PythonReferenceImporter.proposeImportFix(node, reference, ref_text);
            if (importFix != null) {
              // if the context doesn't look like a function call and we only found imports of functions, suggest auto-import
              // as a quickfix but no popup balloon (PY-2312)
              if ((isCall(node) || !importFix.hasOnlyFunctions()) && PyCodeInsightSettings.getInstance().SHOW_IMPORT_POPUP) {
                final AutoImportHintAction autoImportHintAction = new AutoImportHintAction(importFix);
                actions.add(autoImportHintAction);
              }
              else {
                actions.add(importFix);
              }
            }
          }

          if (ref_text.length() > 2 && Character.isUpperCase(ref_text.charAt(0)) && !Character.isUpperCase(ref_text.charAt(1)) &&
              PsiTreeUtil.getParentOfType(element, PyImportStatementBase.class) == null) {
            PsiElement anchor = element;
            if (element instanceof PyQualifiedExpression) {
              final PyExpression qexpr = ((PyQualifiedExpression)element).getQualifier();
              if (qexpr != null) {
                final PyType type = myTypeEvalContext.getType(qexpr);
                if (type instanceof PyModuleType) {
                  anchor = ((PyModuleType) type).getModule();
                }
                else {
                  anchor = null;
                }
              }
              if (anchor != null) {
                actions.add(new CreateClassQuickFix(ref_text, anchor));
              }
            }
          }
        }
      }
      String description = descriptionBuf.toString();
      ProblemHighlightType hl_type;
      if (severity == HighlightSeverity.WARNING) {
        hl_type = ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
      }
      /*
      else if (severity == HighlightSeverity.ERROR) {
        hl_type = ProblemHighlightType.ERROR;
      }
      */
      else {
        hl_type = ProblemHighlightType.LIKE_UNKNOWN_SYMBOL;
      }

      if (GenerateBinaryStubsFix.isApplicable(reference)) {
        actions.add(new GenerateBinaryStubsFix(reference));
      }
      actions.add(new AddIgnoredIdentifierFix(ref_text));
      addPluginQuickFixes(reference, actions);

      PsiElement point = node.getLastChild(); // usually the identifier at the end of qual ref
      if (point == null) point = node;
      TextRange range = reference.getRangeInElement().shiftRight(-point.getStartOffsetInParent());
      registerProblem(point, description, hl_type, null, range, actions.toArray(new LocalQuickFix[actions.size()]));
    }

    private static boolean isCall(PyElement node) {
      final PyCallExpression callExpression = PsiTreeUtil.getParentOfType(node, PyCallExpression.class);
      return callExpression != null && node == callExpression.getCallee();
    }

    private static boolean overridesGetAttr(PyClass cls) {
      PyFunction method = cls.findMethodByName(PyNames.GETATTR, true);
      if (method != null) {
        return true;
      }
      method = cls.findMethodByName(PyNames.GETATTRIBUTE, true);
      if (method != null && !PyBuiltinCache.getInstance(cls).hasInBuiltins(method)) {
        return true;
      }
      return false;
    }

    private static void addPluginQuickFixes(PsiReference reference, final List<LocalQuickFix> actions) {
      for(PyUnresolvedReferenceQuickFixProvider provider: Extensions.getExtensions(PyUnresolvedReferenceQuickFixProvider.EP_NAME)) {
        provider.registerQuickFixes(reference, new Consumer<LocalQuickFix>() {
          public void consume(LocalQuickFix localQuickFix) {
            actions.add(localQuickFix);
          }
        });
      }
    }

    public void highlightUnusedImports() {
      final List<PsiElement> unused = collectUnusedImportElements();
      for (PsiElement element : unused) {
        if (element.getTextLength() > 0) {
          registerProblem(element, "Unused import statement", ProblemHighlightType.LIKE_UNUSED_SYMBOL, null, new OptimizeImportsQuickFix());
        }
      }
    }

    private List<PsiElement> collectUnusedImportElements() {
      if (myAllImports.isEmpty()){
        return Collections.emptyList();
      }
      // PY-1315 Unused imports inspection shouldn't work in python repl console
      final NameDefiner first = myAllImports.iterator().next();
      if (first.getContainingFile() instanceof PyExpressionCodeFragment || PydevConsoleRunner.isInPydevConsole(first)){
        return Collections.emptyList();
      }
      List<PsiElement> result = new ArrayList<PsiElement>();

      Set<NameDefiner> unusedImports = new HashSet<NameDefiner>(myAllImports);
      unusedImports.removeAll(myUsedImports);
      Set<String> usedImportNames = new HashSet<String>();
      for (PsiElement usedImport: myUsedImports) {
        if (usedImport instanceof NameDefiner) {
          for (PyElement e : ((NameDefiner)usedImport).iterateNames()) {
            usedImportNames.add(e.getName());
          }
        }
      }

      Set<PyImportStatementBase> unusedStatements = new HashSet<PyImportStatementBase>();
      final PyUnresolvedReferencesInspection suppressableInspection = new PyUnresolvedReferencesInspection();
      for (NameDefiner unusedImport : unusedImports) {
        PyImportStatementBase importStatement = PsiTreeUtil.getParentOfType(unusedImport, PyImportStatementBase.class);
        if (importStatement != null && !unusedStatements.contains(importStatement) && !myUsedImports.contains(importStatement)) {
          if (suppressableInspection.isSuppressedFor(importStatement)) {
            continue;
          }
          // don't remove as unused imports in try/except statements
          if (PsiTreeUtil.getParentOfType(importStatement, PyTryExceptStatement.class) != null) {
            continue;
          }
          // Don't report conditional imports as unused
          if (PsiTreeUtil.getParentOfType(unusedImport, PyIfStatement.class) != null) {
            boolean isUsed = false;
            for (PyElement e : unusedImport.iterateNames()) {
              if (usedImportNames.contains(e.getName())) {
                isUsed = true;
              }
            }
            if (isUsed) {
              continue;
            }
          }
          if (unusedImport instanceof PyImportElement) {
            if (ResolveImportUtil.resolveImportElement((PyImportElement)unusedImport) == null) {
              continue;
            }
          }
          else {
            assert importStatement instanceof PyFromImportStatement;
            if (ResolveImportUtil.resolveFromImportStatementSource((PyFromImportStatement)importStatement) == null) {
              continue;
            }
          }
          if (unusedImport instanceof PyStarImportElement || areAllImportsUnused(importStatement, unusedImports)) {
            unusedStatements.add(importStatement);
            result.add(importStatement);
          }
          else {
            result.add(unusedImport);
          }
        }
      }
      return result;
    }

    private static boolean areAllImportsUnused(PyImportStatementBase importStatement, Set<NameDefiner> unusedImports) {
      final PyImportElement[] elements = importStatement.getImportElements();
      for (PyImportElement element : elements) {
        if (!unusedImports.contains(element)) {
          return false;
        }
      }
      return true;
    }

    public void optimizeImports() {
      final List<PsiElement> elementsToDelete = collectUnusedImportElements();
      for (PsiElement element : elementsToDelete) {
        element.delete();
      }
    }
  }

  private static class ConfigureInterpreterFix implements LocalQuickFix {
    @NotNull
    @Override
    public String getName() {
      return "Configure Python Interpreter";
    }

    @NotNull
    @Override
    public String getFamilyName() {
      return "Configure Python Interpreter";
    }

    @Override
    public void applyFix(@NotNull final Project project, @NotNull ProblemDescriptor descriptor) {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        @Override
        public void run() {
          // outside of read action
          ShowSettingsUtil.getInstance().showSettingsDialog(project, "Project Interpreter");
        }
      });
    }
  }
}