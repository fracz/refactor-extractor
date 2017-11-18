package com.jetbrains.python.psi.resolve;

import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.HashSet;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyImportResolver;
import com.jetbrains.python.psi.impl.PyQualifiedName;
import com.jetbrains.python.psi.types.PyType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author dcheryasov
 */
public class ResolveImportUtil {
  private ResolveImportUtil() {
  }

  private static final ThreadLocal<Set<String>> ourBeingImported = new ThreadLocal<Set<String>>() {
    @Override protected Set<String> initialValue() {
      return new HashSet<String>();
    }
  };

  private static boolean isAbsoluteImportEnabledFor(PsiElement foothold) {
    if (foothold != null) {
      PsiFile file = foothold.getContainingFile();
      if (file instanceof PyFile) {
        final PyFile pyFile = (PyFile)file;
        if (pyFile.getLanguageLevel().isPy3K()) {
          return true;
        }
        final List<PyFromImportStatement> fromImports = pyFile.getFromImports();
        for (PyFromImportStatement fromImport : fromImports) {
          if (fromImport.isFromFuture()) {
            final PyImportElement[] pyImportElements = fromImport.getImportElements();
            for (PyImportElement element : pyImportElements) {
              final PyQualifiedName qName = element.getImportedQName();
              if (qName != null && qName.matches("absolute_import")) {
                return true;
              }
            }
          }
        }
      }
    }
    // if the relevant import is below the foothold, it is either legal or we've detected the offending statement already
    return false;
  }


  /**
   * Finds a directory that many levels above a given file, making sure that every level has an __init__.py.
   * @param base file that works as a reference.
   * @param depth must be positive, 1 means the dir that contains base, 2 is one dir above, etc.
   * @return found directory, or null.
   */
  @Nullable
  private static PsiDirectory stepBackFrom(PsiFile base, int depth) {
    PsiDirectory result;
    if (base != null) {
      base = base.getOriginalFile(); // just to make sure
      result = base.getContainingDirectory();
      int count = 1;
      while (result != null && result.findFile(PyNames.INIT_DOT_PY) != null) {
        if (count >= depth) return result;
        result = result.getParentDirectory();
        count += 1;
      }
    }
    return null;
  }

  @Nullable
  public static PsiElement resolveImportElement(PyImportElement import_element) {
    final PyQualifiedName qName = import_element.getImportedQName();
    if (qName == null) {
      return null;
    }

    final PsiFile file = import_element.getContainingFile();
    final PyStatement importStatement = import_element.getContainingImportStatement();

    boolean absolute_import_enabled = isAbsoluteImportEnabledFor(import_element);
    PyQualifiedName moduleQName = null;

    if (importStatement instanceof PyFromImportStatement) {
      PsiElement imported_from_module;
      PyFromImportStatement from_import_statement = (PyFromImportStatement)importStatement;
      moduleQName = from_import_statement.getImportSourceQName();
      final int relative_level = from_import_statement.getRelativeLevel();

      if (relative_level > 0 && moduleQName == null) { // "from ... import foo"
        return resolveChild(stepBackFrom(file, relative_level), qName.getComponents().get(0), file, false);
      }

      if (moduleQName != null) { // either "from bar import foo" or "from ...bar import foo"
        imported_from_module = resolveModule(moduleQName.getComponents(), file, absolute_import_enabled, relative_level);
        PsiElement result = resolveChild(imported_from_module, qName.getComponents().get(0), file, false);
        if (result != null) return result;
      }
    }
    else if (importStatement instanceof PyImportStatement) { // "import foo"
      PsiElement result = resolveModule(qName.getComponents(), file, absolute_import_enabled, 0);
      if (result != null) return result;
    }
    // in-python resolution failed
    if (moduleQName != null) {
      return resolveForeignImport(import_element, StringUtil.join(qName.getComponents(), "."), resolveModule(moduleQName.getComponents(), file, false, 0));
    }
    return null;
  }

  @Nullable
  public static PsiElement resolveImportReference(final PyReferenceExpression importRef) {
    // prerequisites
    if (importRef == null) return null;
    if (! importRef.isValid()) return null; // we often catch a reparse while in a process of resolution
    final String referencedName = importRef.getReferencedName(); // it will be the "foo" in later comments
    if (referencedName == null) return null;
    final PsiFile file = importRef.getContainingFile();
    if (file == null || !file.isValid()) return null;

    boolean absolute_import_enabled = isAbsoluteImportEnabledFor(importRef);

    final PsiElement parent = PsiTreeUtil.getParentOfType(importRef, PyImportElement.class, PyFromImportStatement.class); //importRef.getParent();
    if (parent instanceof PyImportElement) {
      PyImportElement import_element = (PyImportElement)parent;
      final PsiElement result = resolveImportElement(import_element);
      if (result != null) {
        return result;
      }
    }
    else if (parent instanceof PyFromImportStatement) { // "from foo import"
      PyFromImportStatement from_import_statement = (PyFromImportStatement)parent;
      PsiElement module = resolveModule(getQualifiedName(importRef), file, absolute_import_enabled, from_import_statement.getRelativeLevel());
      if (module != null) return module;
    }
    return null;
  }

  /**
   * Resolves a module reference in a general case.
   * @param module_reference possibly qualified module reference.
   * @param source_file where that reference resides; serves as PSI foothold to determine module, project, etc.
   * @param import_is_absolute if false, try old python 2.x's "relative first, absolute next" approach.
   * @param relative_level if > 0, step back from source_file and resolve from there (even if import_is_absolute is false!).
   * @return
   */
  @Nullable
  private static PsiElement resolveModule(@Nullable List<String> qualifiedName, PsiFile source_file,
                                          boolean import_is_absolute, int relative_level) {
    PsiElement imported_from_module;

    if (qualifiedName == null) return null;
    String marker = StringUtil.join(qualifiedName, ".") + "#" + Integer.toString(relative_level);
    Set<String> being_imported = ourBeingImported.get();
    if (being_imported.contains(marker)) return null; // break endless loop in import
    try {
      being_imported.add(marker);
      if (relative_level > 0) {
        // "from ...module import"
        imported_from_module = resolveModuleAt(stepBackFrom(source_file, relative_level), source_file, qualifiedName);
      }
      else { // "from module import"
        if (import_is_absolute) imported_from_module = resolveModuleInRoots(qualifiedName, source_file);
        else {
          imported_from_module = resolveModuleAt(source_file.getContainingDirectory(), source_file, qualifiedName);
          if (imported_from_module == null) imported_from_module = resolveModuleInRoots(qualifiedName, source_file);
        }
      }
      return imported_from_module;
    }
    finally {
      being_imported.remove(marker);
    }
  }

  @Nullable
  public static List<String> getQualifiedName(PyReferenceExpression reference) {
    if (reference == null) {
      return null;
    }
    List<String> result = new ArrayList<String>();
    final List<PyReferenceExpression> components = PyResolveUtil.unwindQualifiers(reference);
    if (components == null) {
      return null;
    }
    for (PyReferenceExpression component : components) {
      result.add(component.getReferencedName());
    }
    return result;
  }

  /**
   * Searches for a module at given directory, unwinding qualifiers and traversing directories as needed.
   *
   * @param directory where to start from; top qualifier will be searched for here.
   * @param sourceFile the file containing the import statement being resolved
   * @param qualifiedName the qualified name of the module to search
   * @return module's file, or null.
   */
  @Nullable
  private static PsiElement resolveModuleAt(PsiDirectory directory, PsiFile sourceFile, List<String> qualifiedName) {
    // prerequisites
    if (directory == null || ! directory.isValid()) return null;
    if (sourceFile == null || !sourceFile.isValid()) return null;

    PsiElement seeker = directory;
    for (String name : qualifiedName) {
      seeker = resolveChild(seeker, name, sourceFile, true);
    }
    return seeker;
  }

  @Nullable
  private static PsiElement resolveModuleInRoots(List<String> moduleQualifiedName, PsiElement foothold) {
    if (foothold == null || !foothold.isValid()) return null;
    PsiFile foothold_file = foothold.getContainingFile();
    if (foothold_file == null || !foothold_file.isValid()) return null;

    if (moduleQualifiedName.size() < 1) return null;

    Iterator<String> qualifier_sequence = moduleQualifiedName.iterator();
    String top_module_name = qualifier_sequence.next(); // guaranteed to be unqualified

    LookupRootVisitor visitor = new LookupRootVisitor(top_module_name, foothold.getManager());
    visitRoots(foothold, visitor);
    PsiElement module = visitor.getResult();
    while (module != null && qualifier_sequence.hasNext()) {
      module = resolveChild(module, qualifier_sequence.next(), foothold_file, false); // only files, we want a module
    }
    return module;
  }

  /**
   * Finds a named submodule file/dir under given root.
   */
  @Nullable
  private static PsiElement matchToFile(String name, PsiManager manager, VirtualFile root_file) {
    VirtualFile child_file = root_file.findChild(name);
    if (child_file != null) {
      if (name.equals(child_file.getName())) {
        VirtualFile initpy = child_file.findChild(PyNames.INIT_DOT_PY);
        if (initpy != null) {
          PsiFile initfile = manager.findFile(initpy);
          if (initfile != null) {
            initfile.putCopyableUserData(PyFile.KEY_IS_DIRECTORY, Boolean.TRUE); // we really resolved to the dir
            return initfile;
          }
        }
      }
    }
    return null;
  }

  // TODO: rewrite using resolveImportReference
  /**
   * Resolves either <tt>import foo</tt> or <tt>from foo import bar</tt>.
   * @param importRef refers to the name of the module being imported (the <tt>foo</tt>).
   * @param referencedName the name imported from the module (the <tt>bar</tt> in <tt>import from</tt>), or null (for just <tt>import foo</tt>).
   * @return element the name resolves to, or null.
   */
  @Nullable
  public static PsiElement resolvePythonImport2(final PyReferenceExpression importRef, final String referencedName) {
    if (! importRef.isValid()) return null; // we often catch a reparse while in a process of resolution
    final String the_name = referencedName != null? referencedName : importRef.getName();
    Set<String> being_imported = ourBeingImported.get();
    PsiFile containing_file = importRef.getContainingFile();
    PsiElement last_resolved;
    List<PyReferenceExpression> ref_path = PyResolveUtil.unwindQualifiers(importRef);
    if (ref_path == null) return null;
    // join path to form the FQN: (a, b, c) -> a.b.c.
    StringBuffer pathbuf = new StringBuffer();
    for (PyQualifiedExpression pathelt : ref_path) pathbuf.append(pathelt.getName()).append(".");
    if (referencedName != null) pathbuf.append(referencedName);
    final String import_fqname = pathbuf.toString();
    if (being_imported.contains(import_fqname)) return null; // already trying this path, unable to recurse
    try {
      being_imported.add(import_fqname); // mark
      // resolve qualifiers
      Iterator<PyReferenceExpression> it = ref_path.iterator();
      if (ref_path.size() > 1) { // it was a qualified name
        if (it.hasNext()) {
          last_resolved = it.next().resolve(); // our topmost qualifier, not ourselves for certain
        }
        else return null; // topmost qualifier not found
        while (it.hasNext()) {
          last_resolved =  resolveChild(last_resolved, it.next().getName(), containing_file, true);
          if (last_resolved == null) return null; // anything in the chain unresolved means that the whole chain fails
        }
        if (referencedName != null) {
          return resolveChild(last_resolved, referencedName, containing_file, false);
        }
        else return last_resolved;
      }

      // non-qualified name
      if (referencedName != null) {
        return resolveChild(importRef.resolve(), referencedName, containing_file, false);
        // the importRef.resolve() does not recurse infinitely because we're asked to resolve referencedName, not importRef itself
      }
      // unqualified import can be found:
      // in the same dir
      PsiElement root_elt = resolveInRoots(importRef, the_name);
      if (root_elt != null) return root_elt;
    }
    finally {
      being_imported.remove(import_fqname); // unmark
    }
    return null; // not resolved by any means
  }


  public static void visitRoots(final PsiElement elt, @NotNull final SdkRootVisitor visitor) {
    // real search
    final Module module = ModuleUtil.findModuleForPsiElement(elt);
    if (module != null) {
      // TODO: implement a proper module-like approach in PyCharm for "project's dirs on pythonpath", minding proper search order
      // Module-based approach works only in the IDEA plugin.
      ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
      // look in module sources
      boolean source_entries_missing = true;
      for (ContentEntry entry: rootManager.getContentEntries()) {
        VirtualFile root_file = entry.getFile();

        if (!visitor.visitRoot(root_file)) return;
        for (VirtualFile folder : entry.getSourceFolderFiles()) {
          source_entries_missing = false;
          if (!visitor.visitRoot(folder)) return;
        }
      }
      if (source_entries_missing) {
        // fallback for a case without any source entries: use project root
        VirtualFile project_root = module.getProject().getBaseDir();
        if (project_root != null && !visitor.visitRoot(project_root)) return;
      }
      // else look in SDK roots
      rootManager.processOrder(new SdkRootVisitingPolicy(visitor), null);
    }
    else {
      // no module, another way to look in SDK roots
      final PsiFile elt_psifile = elt.getContainingFile();
      if (elt_psifile != null) {  // formality
        final VirtualFile elt_vfile = elt_psifile.getVirtualFile();
        if (elt_vfile != null) { // reality
          for (OrderEntry entry: ProjectRootManager.getInstance(elt.getProject()).getFileIndex().getOrderEntriesForFile(elt_vfile)) {
            if (!visitGivenRoots(entry.getFiles(OrderRootType.SOURCES), visitor)) break;
            if (!visitGivenRoots(entry.getFiles(OrderRootType.CLASSES), visitor)) break;
          }
        }
      }
    }
  }


  private static boolean visitGivenRoots(final VirtualFile[] roots, SdkRootVisitor visitor) {
    for (VirtualFile root: roots) {
      if (! visitor.visitRoot(root)) return false;
    }
    return true;
  }

  // TODO: rewrite using resolveModuleInRoots
  /**
   * Looks for a name among element's module's roots; if there's no module, then among project's roots.
   * @param elt PSI element that defines the module and/or the project.
   * @param refName module name to be found among roots.
   * @return a PsiFile, a child of a root.
   */
  @Nullable
  public static PsiElement resolveInRoots(@NotNull final PsiElement elt, final String refName) {
    // NOTE: a quick and ditry temporary fix for "current dir" root path, which is assumed to be present first (but may be not).
    PsiFile pfile = elt.getContainingFile();
    VirtualFile vfile = pfile.getVirtualFile();
    if (vfile == null) { // we're probably within a copy, e.g. for completion; get the real thing
      pfile = pfile.getOriginalFile();
    }
    if (pfile != null) {
      PsiDirectory pdir = pfile.getContainingDirectory();
      if (pdir != null) {
        PsiElement child_elt = resolveChild(pdir, refName, pfile, true);
        if (child_elt != null) return child_elt;
      }

    }
    // real search
    final Module module = ModuleUtil.findModuleForPsiElement(elt);
    if (module != null) {
      // TODO: implement a proper module-like approach in PyCharm for "project's dirs on pythonpath", minding proper search order
      // Module-based approach works only in the IDEA plugin.
      ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
      // look in module sources
      boolean source_entries_missing = true;
      for (ContentEntry entry: rootManager.getContentEntries()) {
        VirtualFile root_file = entry.getFile();

        PsiElement ret = matchToFile(refName, elt.getManager(), root_file);
        if (ret != null) return ret;
        for (VirtualFile folder : entry.getSourceFolderFiles()) {
          source_entries_missing = false;
          ret = matchToFile(refName, elt.getManager(), folder);
          if (ret != null) return ret;
        }
      }
      if (source_entries_missing) {
        // fallback for a case without any source entries: use project root
        VirtualFile project_root = module.getProject().getBaseDir();
        PsiElement ret = matchToFile(refName, elt.getManager(), project_root);
        if (ret != null) return ret;
      }
      // else look in SDK roots
      LookupRootVisitor visitor = new LookupRootVisitor(refName, elt.getManager());
      rootManager.processOrder(new SdkRootVisitingPolicy(visitor), null);
      return visitor.getResult();
    }
    else {
      // no module, another way to look in SDK roots
      final PsiFile elt_psifile = elt.getContainingFile();
      if (elt_psifile != null) {  // formality
        final VirtualFile elt_vfile = elt_psifile.getVirtualFile();
        if (elt_vfile != null) { // reality
          for (OrderEntry entry: ProjectRootManager.getInstance(elt.getProject()).getFileIndex().getOrderEntriesForFile(elt_vfile)) {
            PsiElement root_elt = resolveWithinRoots(entry.getFiles(OrderRootType.SOURCES), refName, elt.getProject());
            if (root_elt != null) return root_elt;
          }
        }
      }
    }
    return null; // nothing matched
  }

  @Nullable
  private static PsiElement resolveForeignImport(final PyElement importElement, final String importText, final PsiElement importFrom) {
    for(PyImportResolver resolver: Extensions.getExtensions(PyImportResolver.EP_NAME)) {
      PsiElement result = resolver.resolveImportReference(importElement, importText, importFrom);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Nullable
  private static PsiElement resolveWithinRoots(final VirtualFile[] roots, final String referencedName, final Project project) {
    final PsiManager psi_mgr = PsiManager.getInstance(project);
    for(VirtualFile contentRoot: roots) {
      PsiElement result = null;
      final VirtualFile childFile = contentRoot.findChild(referencedName + PyNames.DOT_PY);
      if (childFile != null) result = psi_mgr.findFile(childFile);
      else {
        final VirtualFile childDir = contentRoot.findChild(referencedName);
        if (childDir != null) result = psi_mgr.findDirectory(childDir);
      }
      if (result != null) return result;
    }
    return null;
  }

  interface SdkRootVisitor {
    /**
     * @param root what we're visiting.
     * @return false when visiting must stop.
     */
    boolean visitRoot(VirtualFile root);
  }

  static class LookupRootVisitor implements SdkRootVisitor {
    String path;
    PsiManager psimgr;
    PsiElement result;

    public LookupRootVisitor(String name, PsiManager psimgr) {
      this.path = name.replace('.', '/');
      this.psimgr = psimgr;
      this.result = null;
    }

    public boolean visitRoot(final VirtualFile root) {
      if (result != null) return false;
      final VirtualFile childFile = root.findFileByRelativePath(path + PyNames.DOT_PY);
      if (childFile != null) {
        result = psimgr.findFile(childFile);
        return (result == null);
      }

      final VirtualFile childDir = root.findFileByRelativePath(path);
      if (childDir != null) {
        result = psimgr.findDirectory(childDir);
        return (result == null);
      }
      return true;
    }

    public PsiElement getResult() {
      return result;
    }
  }

  static class CollectingRootVisitor implements SdkRootVisitor {
    Set<String> result;
    PsiManager psimgr;

    static String cutExt(String name) {
      return name.substring(0, Math.max(name.length() - PyNames.DOT_PY.length(), 0));
    }

    public CollectingRootVisitor(PsiManager psimgr) {
      result = new HashSet<String>();
      this.psimgr = psimgr;
    }

    public boolean visitRoot(final VirtualFile root) {
      for (VirtualFile vfile : root.getChildren()) {
        if (vfile.getName().endsWith(PyNames.DOT_PY)) {
          PsiFile pfile = psimgr.findFile(vfile);
          if (pfile != null) result.add(cutExt(pfile.getName()));
        }
        else if (vfile.isDirectory() && (vfile.findChild(PyNames.INIT_DOT_PY) != null)) {
          PsiDirectory pdir = psimgr.findDirectory(vfile);
          if (pdir != null) result.add(pdir.getName());
        }
      }
      return true; // continue forever
    }

    public Collection<String> getResult() {
      return result;
    }
  }

  /**
  Tries to find referencedName under the parent element. Used to resolve any names that look imported.
  Parent might happen to be a PyFile(__init__.py), then it is treated <i>both</i> as a file and as ist base dir.
  For details of this ugly magic, see {@link com.jetbrains.python.psi.impl.PyReferenceExpressionImpl#resolve()}.
  @param parent element under which to look for referenced name; if null, null is returned.
  @param referencedName which name to look for.
  @param containingFile where we're in.
  @param fileOnly if true, considers only a PsiFile child as a valid result; non-file hits are ignored.
  @return the element the referencedName resolves to, or null.
  @todo: Honor module's __all__ value.
  @todo: Honor package's __path__ value (hard).
  */
  @Nullable
  public static PsiElement resolveChild(
    @Nullable final PsiElement parent, final String referencedName, final PsiFile containingFile, boolean fileOnly
  ) {
    PsiDirectory dir = null;
    PsiElement ret = null;
    ResolveProcessor processor = null;
    if (parent instanceof PyFile) {
      if (parent.getCopyableUserData(PyFile.KEY_IS_DIRECTORY) == Boolean.TRUE) {
        // the file was a fake __init__.py covering a reference to dir
        dir = ((PyFile)parent).getContainingDirectory();
      }
      else {
        // look for name in the file:
        //processor = new ResolveProcessor(referencedName);
        ret = resolveExportedName((PyFile) parent, referencedName);
        if (ret != null) return ret;
      }
    }
    else if (parent instanceof PsiDirectory) {
      dir = (PsiDirectory)parent;
    }
    else if (parent instanceof PsiDirectoryContainer) {
      final PsiDirectoryContainer container = (PsiDirectoryContainer)parent;
      for(PsiDirectory childDir: container.getDirectories()) {
        final PsiElement result = resolveInDirectory(referencedName, containingFile, childDir, processor, fileOnly);
        //if (fileOnly && ! (result instanceof PsiFile) && ! (result instanceof PsiDirectory)) return null;
        if (result != null) return result;
      }
    }
    if (dir != null) {
      final PsiElement result =  resolveInDirectory(referencedName, containingFile, dir, processor, fileOnly);
      //if (fileOnly && ! (result instanceof PsiFile) && ! (result instanceof PsiDirectory)) return null;
      return result;
    }
    return ret;
  }

  @Nullable
  private static PsiElement resolveExportedName(PyFile parent, String referencedName) {
    final PsiElement exportedName = parent.findExportedName(referencedName);
    if (exportedName instanceof PyImportElement) {
      return ((PyImportElement) exportedName).getElementNamed(referencedName);
    }
    return exportedName;
  }

  @Nullable
  private static PsiElement resolveInDirectory(
    final String referencedName, final PsiFile containingFile, final PsiDirectory dir, ResolveProcessor processor, boolean isFileOnly
  ) {
    if (referencedName == null) return null;
    final PsiFile file = dir.findFile(referencedName + PyNames.DOT_PY);
    // findFile() does case-insensitive search, and we need exactly matching case (see PY-381)
    if (file != null && FileUtil.getNameWithoutExtension(file.getName()).equals(referencedName)) {
      return file;
    }
    final PsiDirectory subdir = dir.findSubdirectory(referencedName);
    if (subdir != null) return subdir;
    else if (! isFileOnly) {
      // not a subdir, not a file; could be a name in parent/__init__.py
      final PsiFile initPy = dir.findFile(PyNames.INIT_DOT_PY);
      if (initPy == containingFile) return null; // don't dive into the file we're in
      if (initPy instanceof PyFile) {
        return resolveExportedName((PyFile)initPy, referencedName);
      }
    }
    return null;
  }


  private static void addImportedNames(PyImportElement[] import_elts, Collection<String> collected_names) {
    if (import_elts != null && collected_names != null) {
      for (PyImportElement ielt : import_elts) {
        String s;
        PyReferenceExpression ref = ielt.getImportReference();
        if (ref != null) {
          s = ref.getReferencedName();
          if (s != null) collected_names.add(s);
        }
      }
    }
  }

  /**
   * Finds reasonable names to import to complete a patrial name.
   * @param partial_ref reference containing the partial name.
   * @return an array of names ready for getVariants().
   */
  public static Object[] suggestImportVariants(final PyReferenceExpression partial_ref) {
    List<Object> variants = new ArrayList<Object>();
    if (partial_ref == null) return variants.toArray();
    PsiFile current_file = partial_ref.getContainingFile();
    if (current_file != null) current_file = current_file.getOriginalFile();
    int relative_level = 0;
    final Set<String> names_already = new java.util.HashSet<String>(); // don't propose already imported names
    // are we in "import _" or "from foo import _"?
    PyFromImportStatement from_import = PsiTreeUtil.getParentOfType(partial_ref, PyFromImportStatement.class);
    if (from_import != null && partial_ref.getParent() != from_import) { // in "from foo import _"
      PyReferenceExpression src = from_import.getImportSource();
      if (src != null) {
        PsiElement mod_candidate = src.resolve();
        if (mod_candidate instanceof PyExpression) {
          addImportedNames(from_import.getImportElements(), names_already); // don't propose already imported items
          // collect what's within module file
          final VariantsProcessor processor = new VariantsProcessor(new PyResolveUtil.FilterNameNotIn(names_already));
          PyResolveUtil.treeCrawlUp(processor, true, mod_candidate);
          variants.addAll(processor.getResultList());
          // try to collect submodules
          PyExpression module = (PyExpression)mod_candidate;
          PyType qualifierType = module.getType();
          if (qualifierType != null) {
            ProcessingContext ctx = new ProcessingContext();
            for (Object ex : variants) { // just in case: file's definitions shadow submodules
              if (ex instanceof PyReferenceExpression) {
                names_already.add(((PyReferenceExpression)ex).getReferencedName());
              }
            }
            // collect submodules
            ctx.put(PyType.CTX_NAMES, names_already);
            Collections.addAll(variants, qualifierType.getCompletionVariants(partial_ref, ctx));
          }
          return variants.toArray();
        }
      }
      else { // null source, must be a "from ... import"
        relative_level = from_import.getRelativeLevel();
        if (relative_level > 0) {
          PsiDirectory relative_dir = stepBackFrom(current_file, relative_level);
          if (relative_dir != null) {
            addImportedNames(from_import.getImportElements(), names_already);
            fillFromDir(relative_dir, current_file, names_already, variants);
          }
        }
      }
    }
    // in "import _" or "from _ import"
    if (from_import != null) addImportedNames(from_import.getImportElements(), names_already);
    else {
      PyImportStatement import_stmt = PsiTreeUtil.getParentOfType(partial_ref, PyImportStatement.class);
      if (import_stmt != null) {
        addImportedNames(import_stmt.getImportElements(), names_already);
      }
    }
    // look at current dir
    if (current_file != null && relative_level == 0 && ! isAbsoluteImportEnabledFor(current_file)) {
      fillFromDir(current_file.getParent(), current_file, names_already, variants);
    }
    if (relative_level == 0) {
      // look in SDK
      final CollectingRootVisitor visitor = new CollectingRootVisitor(partial_ref.getManager());
      final Module module = ModuleUtil.findModuleForPsiElement(partial_ref);
      if (module != null) {
        ModuleRootManager.getInstance(module).processOrder(new SdkRootVisitingPolicy(visitor), null);
        for (String name : visitor.getResult()) {
          if (PyNames.isIdentifier(name) && !names_already.contains(name)) variants.add(name); // to thwart stuff like "__phello__.foo"
        }
      }
    }

    return ArrayUtil.toObjectArray(variants);
  }

  // adds variants found under given dir
  private static void fillFromDir(PsiDirectory target_dir, PsiFile source_file, Set<String> names_already, List<Object> variants) {
    if (target_dir != null) {
      for (PsiElement dir_item : target_dir.getChildren()) {
        if (dir_item != source_file) {
          if (dir_item instanceof PsiDirectory) {
            final PsiDirectory dir = (PsiDirectory)dir_item;
            if (dir.findFile(PyNames.INIT_DOT_PY) != null) {
              final String name = dir.getName();
              if (PyNames.isIdentifier(name) && !names_already.contains(name)) variants.add(name);
            }
          }
          else if (dir_item instanceof PsiFile) { // plain file
            String filename = ((PsiFile)dir_item).getName();
            if (!PyNames.INIT_DOT_PY.equals(filename) && filename.endsWith(PyNames.DOT_PY)) {
              final String name = filename.substring(0, filename.length() - PyNames.DOT_PY.length());
              if (PyNames.isIdentifier(name) && !names_already.contains(name)) variants.add(name);
            }
          }
        }
      }
    }
  }

  /**
   * Tries to find roots that contain given vfile, and among them the root that contains at the smallest depth.
   */
  private static class PathChoosingVisitor implements SdkRootVisitor {

    private final VirtualFile myFile;
    private String myFname;
    private String myResult = null;
    private int myDots = Integer.MAX_VALUE; // how many dots in the path

    private PathChoosingVisitor(VirtualFile file) {
      myFile = file;
      myFname = file.getPath();
      // cut off the ext
      int pos = myFname.lastIndexOf('.');
      if (pos > 0) myFname = myFname.substring(0, pos);
      // cut off the final __init__ if it's there; we want imports directly from a module
      pos = myFname.lastIndexOf(PyNames.INIT);
      if (pos > 0) myFname = myFname.substring(0, pos-1); // pos-1 also cuts the '/' that came before "__init__"
    }

    public boolean visitRoot(VirtualFile root) {
      // does it ever fit?
      String root_name = root.getPath()+"/";
      if (myFname.startsWith(root_name)) {
        String bet = myFname.substring(root_name.length()).replace('/', '.'); // "/usr/share/python/foo/bar" -> "foo.bar"
        // count the dots
        int dots = 0;
        for (int i = 0; i < bet.length(); i += 1) if (bet.charAt(i) == '.') dots += 1;
        // a better variant?
        if (dots < myDots) {
          myDots = dots;
          myResult = bet;
        }
      }
      return true; // visit all roots
    }

    public String getResult() {
      return myResult;
    }
  }

  /**
   * Looks for a way to import given file.
   * @param foothold an element in the file to import to (maybe the file itself); used to determine module, roots, etc.
   * @param vfile file which importable name we want to find.
   * @return a possibly qualified name under which the file may be imported, or null. If there's more than one way (overlapping roots),
   * the name with fewest qualifiers is selected.
   */
  @Nullable
  public static String findShortestImportableName(PsiElement foothold, VirtualFile vfile) {
    PathChoosingVisitor visitor = new PathChoosingVisitor(vfile);
    visitRoots(foothold, visitor);
    return visitor.getResult();
  }


  private static class SdkRootVisitingPolicy extends RootPolicy<PsiElement> {
    private final SdkRootVisitor myVisitor;

    public SdkRootVisitingPolicy(SdkRootVisitor visitor) {
      myVisitor = visitor;
    }

    @Nullable
    public PsiElement visitJdkOrderEntry(final JdkOrderEntry jdkOrderEntry, final PsiElement value) {
      if (value != null) return value;  // for chaining in processOrder()
      visitGivenRoots(jdkOrderEntry.getRootFiles(OrderRootType.SOURCES), myVisitor);
      visitGivenRoots(jdkOrderEntry.getRootFiles(OrderRootType.CLASSES), myVisitor);
      return null;
    }

    @Nullable
    @Override
    public PsiElement visitLibraryOrderEntry(LibraryOrderEntry libraryOrderEntry, PsiElement value) {
      if (value != null) return value;  // for chaining in processOrder()
      visitGivenRoots(libraryOrderEntry.getRootFiles(OrderRootType.SOURCES), myVisitor);
      visitGivenRoots(libraryOrderEntry.getRootFiles(OrderRootType.CLASSES), myVisitor);
      return null;
    }
  }

  public static enum ROLE_IN_IMPORT {
    /**
     * The reference is not inside an import statement.
     */
    NONE,

    /**
     * The reference is inside import and refers to a module
     */
    AS_MODULE,

    /**
     * The reference is inside import and refers to a name imported from a module
     */
    AS_NAME
  }

  /**
   * @param reference what we test
   * @return the role of reference in enclosing import statement, if any
   */
  public static ROLE_IN_IMPORT getRoleInImport(@NotNull PsiReference reference) {
    PsiElement parent = PsiTreeUtil.getParentOfType(
      reference.getElement(),
      PyImportElement.class, PyFromImportStatement.class
    );
    if (parent instanceof PyFromImportStatement) return ROLE_IN_IMPORT.AS_MODULE; // from foo ...
    if (parent instanceof PyImportElement) {
      PsiElement statement = parent.getParent();
      if (statement instanceof PyImportStatement) return ROLE_IN_IMPORT.AS_MODULE; // import foo,...
      else if (statement instanceof PyFromImportStatement) {
        PyFromImportStatement importer = (PyFromImportStatement)statement; // from ??? import foo
        if (importer.getImportSource() == null && importer.getRelativeLevel() > 0) return ROLE_IN_IMPORT.AS_MODULE; // from . import foo,...
        else return ROLE_IN_IMPORT.AS_NAME; // from bar import foo,...
      }
    }
    return ROLE_IN_IMPORT.NONE;
  }
}