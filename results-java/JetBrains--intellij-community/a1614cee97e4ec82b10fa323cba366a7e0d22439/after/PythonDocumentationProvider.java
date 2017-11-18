package com.jetbrains.python;

import com.intellij.lang.documentation.QuickDocumentationProvider;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.xml.util.XmlStringUtil;
import com.jetbrains.python.console.PydevConsoleRunner;
import com.jetbrains.python.console.PydevDocumentationProvider;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.impl.PyCallExpressionHelper;
import com.jetbrains.python.psi.resolve.ResolveImportUtil;
import com.jetbrains.python.psi.resolve.RootVisitor;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.toolbox.ChainIterable;
import com.jetbrains.python.toolbox.FP;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides quick docs for classes, methods, and functions.
 */
public class PythonDocumentationProvider extends QuickDocumentationProvider {

  // provides ctrl+hover info
  public String getQuickNavigateInfo(final PsiElement element) {
    if (element instanceof PyFunction) {
      PyFunction func = (PyFunction)element;
      StringBuilder cat = new StringBuilder();
      PyClass cls = func.getContainingClass();
      if (cls != null) {
        String cls_name = cls.getName();
        cat.append("class ").append(cls_name).append("\n ");
        // It would be nice to have class import info here, but we don't know the ctrl+hovered reference and context
      }
      return describeFunction(func, LSame2, ", ", LSame2, LSame1).toString();
    }
    else if (element instanceof PyClass) {
      PyClass cls = (PyClass)element;
      return describeClass(cls, LSame2).toString();
    }
    return null;
  }

  private final static @NonNls String BR = "<br>";

  private static @NonNls String combUp(@NonNls String what) {
    return XmlStringUtil.escapeString(what).replace("\n", BR).replace(" ", "&nbsp;");
  }

  /**
   * Creates a HTML description of function definition.
   * @param fun the function
   * @param cat string buffer to append to
   * @param deco_name_wrapper puts a tag around decorator name
   * @param deco_separator is added between decorators
   * @param func_name_wrapper puts a tag around the function name
   * @param escaper sanitizes values that come directly from doc string or code
   * @return cat for easy chaining
   */
  private static ChainIterable<String> describeFunction(
    PyFunction fun,
    FP.Lambda1<Iterable<String>, Iterable<String>> deco_name_wrapper,
    String deco_separator,
    FP.Lambda1<Iterable<String>, Iterable<String>> func_name_wrapper,
    FP.Lambda1<String, String> escaper
  ) {
    ChainIterable<String> cat = new ChainIterable<String>(null);
    PyDecoratorList deco_list = fun.getDecoratorList();
    if (deco_list != null) {
      for (PyDecorator deco : deco_list.getDecorators()) {
        cat.add(describeDeco(deco, deco_name_wrapper, escaper)).add(deco_separator); // can't easily pass describeDeco to map() %)
      }
    }
    cat.add("def ").addWith(func_name_wrapper, $(fun.getName()));
    cat.add(escaper.apply(PyUtil.getReadableRepr(fun.getParameterList(), false)));
    final PyType returnType = fun.getReturnType();
    cat.add(escaper.apply("\nInferred return type: ")).add(returnType == null ? "unknown" : returnType.getName());
    return cat;
  }

  /**
   * Creates a HTML description of function definition.
   * @param cls the class
   * @param cat string buffer to append to
   * @return cat for easy chaining
   */
  private static ChainIterable<String> describeClass(
    PyClass cls,
    FP.Lambda1<Iterable<String>, Iterable<String>> name_wrapper
  ) {
    ChainIterable<String> cat = new ChainIterable<String>();
    cat.add("class ").addWith(name_wrapper, $(cls.getName()));
    final PyExpression[] ancestors = cls.getSuperClassExpressions();
    if (ancestors.length > 0) {
      cat.add("(").add(interleave(FP.map(LReadableRepr, ancestors), ", ")).add(")");
    }
    // TODO: for py3k, show decorators
    return cat;
  }

  //
  private static Iterable<String> describeDeco(
    PyDecorator deco,
    final FP.Lambda1<Iterable<String>, Iterable<String>> name_wrapper, //  addWith in tags, if need be
    final FP.Lambda1<String, String> arg_wrapper   // add escaping, if need be
  ) {
    ChainIterable<String> cat = new ChainIterable<String>();
    cat.add("@").addWith(name_wrapper, $(PyUtil.getReadableRepr(deco.getCallee(), true)));
    if (deco.hasArgumentList()) {
      PyArgumentList arglist = deco.getArgumentList();
      if (arglist != null) {
        cat
          .add("(")
          .add(interleave(FP.map(FP.combine(LReadableRepr, arg_wrapper), arglist.getArguments()), ", "))
          .add(")")
        ;
      }
    }
    return cat;
  }

  private static @NotNull ChainIterable<String> combUpDocString(@NotNull String docstring) {
    ChainIterable<String> cat = new ChainIterable<String>();
    // detect common indentation
    String[] fragments = Pattern.compile("\n").split(docstring);
    Pattern spaces_pat = Pattern.compile("^\\s+");
    boolean is_first = true;
    final int IMPOSSIBLY_BIG = 999999;
    int cut_width = IMPOSSIBLY_BIG;
    for (String frag : fragments) {
      if (frag.length() == 0) continue;
      int pad_width = 0;
      final Matcher matcher = spaces_pat.matcher(frag);
      if (matcher.find()) {
        pad_width = matcher.end();
        if (is_first) {
          is_first = false;
          if (pad_width == 0) continue; // first line may have zero padding // first line may have zero padding
        }
      }
      if (pad_width < cut_width) cut_width = pad_width;
    }
    // remove common indentation
    if (cut_width > 0 && cut_width < IMPOSSIBLY_BIG) {
      for (int i=0; i < fragments.length; i+= 1) {
        if (fragments[i].length() > 0) fragments[i] = fragments[i].substring(cut_width);
      }
    }
    // reconstruct back, dropping first empty fragment as needed
    is_first = true;
    for (String frag : fragments) {
      if (is_first && spaces_pat.matcher(frag).matches()) continue; // ignore all initial whitespace
      if (is_first) is_first = false;
      else cat.add(BR);
      cat.add(combUp(frag));
    }
    return cat;
  }

  // provides ctrl+Q doc
  public String generateDoc(PsiElement element, final PsiElement originalElement) {
    if (element != null && PydevConsoleRunner.isInPydevConsole(element) ||
      originalElement != null && PydevConsoleRunner.isInPydevConsole(originalElement)){
      return PydevDocumentationProvider.createDoc(element, originalElement);
    }
    ChainIterable<String> cat = new ChainIterable<String>(); // our main output sequence
    final ChainIterable<String> prolog_cat = new ChainIterable<String>(); // sequence for reassignment info, etc
    final ChainIterable<String> doc_cat = new ChainIterable<String>(); // sequence for doc string
    final ChainIterable<String> epilog_cat = new ChainIterable<String>(); // sequence for doc "copied from" notices and such

    cat.add(prolog_cat).addWith(TagCode, doc_cat).add(epilog_cat); // pre-assemble; then add stuff to individual cats as needed
    cat = wrapInTag("html", wrapInTag("body", cat));
    element = resolveToDocStringOwner(element, originalElement, prolog_cat);

    // now element may contain a doc string
    if (element instanceof PyDocStringOwner) {
      String docString = null;
      PyStringLiteralExpression doc_expr = ((PyDocStringOwner) element).getDocStringExpression();
      if (doc_expr != null) docString = doc_expr.getStringValue();
      // doc of what?
      if (element instanceof PyClass) {
        PyClass cls = (PyClass)element;
        doc_cat.addWith(TagSmall, describeClass(cls, TagBold));
      }
      else if (element instanceof PyFunction) {
        PyFunction fun = (PyFunction)element;
        PyClass cls = fun.getContainingClass();
        if (cls != null) doc_cat.addWith(TagSmall, $("class ", cls.getName(), BR));
        doc_cat.add(describeFunction(fun, TagItalic, BR, TagBold, LCombUp));
        if (docString == null) {
          addInheritedDocString(fun, cls, doc_cat, epilog_cat);
        }
      }
      else if (element instanceof PyFile) {
        // what to prepend to a module description??
        String path = VfsUtil.urlToPath(((PyFile)element).getUrl());
        if ("".equals(path)) {
          prolog_cat.addWith(TagSmall, $(PyBundle.message("QDOC.module.path.unknown")));
        }
        else {
          RootFinder finder = new RootFinder(path);
          ResolveImportUtil.visitRoots(element, finder);
          final String root_path = finder.getResult();
          if (root_path != null) {
            String after_part = path.substring(root_path.length());
            prolog_cat.addWith(TagSmall, $(root_path).addWith(TagBold, $(after_part)));
          }
          else prolog_cat.addWith(TagSmall, $(path));
        }
      }
      else { // not a func, not a class
        doc_cat.add(combUp(PyUtil.getReadableRepr(element, false)));
      }
      if (docString != null) {
        doc_cat.add(BR).add(combUpDocString(docString));
      }
      else if (prolog_cat.isEmpty() && doc_cat.isEmpty() && epilog_cat.isEmpty()) return null; // got nothing to say!
      return cat.toString();
    }
    return null;
  }

  private class RootFinder implements RootVisitor {
    private String myResult;
    private String myPath;

    private RootFinder(String path) {
      myPath = path;
    }

    public boolean visitRoot(VirtualFile root) {
      String vpath = VfsUtil.urlToPath(root.getUrl());
      if (myPath.startsWith(vpath)) {
        myResult = vpath;
        return false;
      }
      else return true;
    }

    String getResult() {
      return myResult;
    }
  }

  private static PsiElement resolveToDocStringOwner(PsiElement element, PsiElement originalElement, ChainIterable<String> prolog_cat) {
    // here the ^Q target is already resolved; the resolved element may point to intermediate assignments
    boolean reassignment_marked = false;
    if (element instanceof PyTargetExpression) {
      if (! reassignment_marked) {
        //prolog_cat.add(TagSmall.apply($("Assigned to ", element.getText(), BR)));
        prolog_cat.addWith(TagSmall, $(PyBundle.message("QDOC.assigned.to.$0", element.getText())).add(BR));
        reassignment_marked = true;
      }
      element = ((PyTargetExpression)element).findAssignedValue();
    }
    if (element instanceof PyReferenceExpression) {
      if (! reassignment_marked) {
        //prolog_cat.add(TagSmall.apply($("Assigned to ", element.getText(), BR)));
        prolog_cat.addWith(TagSmall, $(PyBundle.message("QDOC.assigned.to.$0", element.getText())).add(BR));
        reassignment_marked = true;
      }
      element = ((PyReferenceExpression)element).followAssignmentsChain().getElement();
    }
    // it may be a call to a standard wrapper
    if (element instanceof PyCallExpression) {
      Pair<String, PyFunction> wrap_info = PyCallExpressionHelper.interpretAsStaticmethodOrClassmethodWrappingCall(
        (PyCallExpression)element, originalElement
      );
      if (wrap_info != null) {
        String wrapper_name = wrap_info.getFirst();
        PyFunction wrapped_func = wrap_info.getSecond();
        //prolog_cat.addWith(TagSmall, $("Wrapped in ").addWith(TagCode, $(wrapper_name)).add(BR));
        prolog_cat.addWith(TagSmall, $(PyBundle.message("QDOC.wrapped.in.$0", wrapper_name)).add(BR));
        element = wrapped_func;
      }

    }
    if (element instanceof PyFunction && PyNames.INIT.equals(((PyFunction)element).getName())) {
      final PyStringLiteralExpression expression = ((PyFunction)element).getDocStringExpression();
      if (expression == null) {
        PyClass containingClass = ((PyFunction) element).getContainingClass();
        if (containingClass != null) {
          element = containingClass;
        }
      }
    }
    return element;
  }

  private static void addInheritedDocString(PyFunction fun, PyClass cls, ChainIterable<String> doc_cat, ChainIterable<String> epilog_cat) {
    boolean not_found = true;
    String meth_name = fun.getName();
    if (cls != null && meth_name != null ) {
      // look for inherited and its doc
      for (PyClass ancestor : cls.iterateAncestors()) {
        PyFunction inherited = ancestor.findMethodByName(meth_name, false);
        if (inherited != null) {
          PyStringLiteralExpression doc_elt = inherited.getDocStringExpression();
          if (doc_elt != null) {
            String inherited_doc = doc_elt.getStringValue();
            if (inherited_doc.length() > 1) {
              epilog_cat
                .add(BR).add(BR)
                .add(PyBundle.message("QDOC.copied.from.$0.$1", ancestor.getName(), meth_name))
                .add(BR).add(BR)
                .addWith(TagCode, combUpDocString(inherited_doc))
              ;
              not_found = false;
              break;
            }
          }
        }
      }

      if (not_found) {
        // above could have not worked because inheritance is not searched down to 'object'.
        // for well-known methods, copy built-in doc string.
        // TODO: also handle predefined __xxx__ that are not part of 'object'.
        if (PyNames.UnderscoredAttributes.contains(meth_name)) {
          PyClassType objtype = PyBuiltinCache.getInstance(fun).getObjectType(); // old- and new-style classes share the __xxx__ stuff
          if (objtype != null) {
            PyClass objcls = objtype.getPyClass();
            if (objcls != null) {
              PyFunction obj_underscored = objcls.findMethodByName(meth_name, false);
              if (obj_underscored != null) {
                PyStringLiteralExpression predefined_doc_expr = obj_underscored.getDocStringExpression();
                String predefined_doc = predefined_doc_expr != null? predefined_doc_expr.getStringValue() : null;
                if (predefined_doc != null && predefined_doc.length() > 1) { // only a real-looking doc string counts
                  doc_cat.add(combUpDocString(predefined_doc));
                  epilog_cat.add(BR).add(BR).add(PyBundle.message("QDOC.copied.from.builtin"));
                }
              }
            }
          }
        }
      }
    }
  }

  private static final FP.Lambda1<String, String> LCombUp = new FP.Lambda1<String, String>() {
    public String apply(String argname) {
      return combUp(argname);
    }
  };

  private static final FP.Lambda1<String, String> LSame1 = new FP.Lambda1<String, String>() {
    public String apply(String name) {
      return name;
    }
  };


  private static ChainIterable<String> wrapInTag(String tag, Iterable<String> content) {
    return new ChainIterable<String>(Collections.singleton("<" + tag + ">")).add(content).add("</" + tag + ">");
  }

  private static ChainIterable<String> $(String... content) {
    return new ChainIterable<String>(Arrays.asList(content));
  }


  // make a first-order curried objects out of wrapInTag()
  private static class TagWrapper implements FP.Lambda1<Iterable<String>, Iterable<String>> {
    private final String myTag;

    TagWrapper(String tag) {
      myTag = tag;
    }

    public Iterable<String> apply(Iterable<String> contents) {
      return wrapInTag(myTag, contents);
    }

  }

  private static final TagWrapper TagBold = new TagWrapper("b");
  private static final TagWrapper TagItalic = new TagWrapper("i");
  private static final TagWrapper TagSmall = new TagWrapper("small");
  private static final TagWrapper TagCode = new TagWrapper("code");

  private static final FP.Lambda1<Iterable<String>, Iterable<String>> LSame2 = new FP.Lambda1<Iterable<String>, Iterable<String>>() {
    public Iterable<String> apply(Iterable<String> what) {
      return what;
    }
  };

  public static FP.Lambda1<PyExpression, String> LReadableRepr = new FP.Lambda1<PyExpression, String>() {
    public String apply(PyExpression arg) {
      return PyUtil.getReadableRepr(arg, true);
    }
  };

  private static <T> Iterable<T> interleave(Iterable<T> source, T filler) {
    List<T> ret = new LinkedList<T>();
    boolean is_next = false;
    for (T what : source) {
      if (is_next) ret.add(filler);
      else is_next = true;
      ret.add(what);
    }
    return ret;
  }
}