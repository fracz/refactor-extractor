package com.jetbrains.python.documentation;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.LineTokenizer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.PythonFileType;
import com.jetbrains.python.codeInsight.controlflow.ScopeOwner;
import com.jetbrains.python.console.PyConsoleUtil;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.impl.PyCallExpressionHelper;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.resolve.QualifiedResolveResult;
import com.jetbrains.python.psi.resolve.RootVisitor;
import com.jetbrains.python.psi.resolve.RootVisitorHost;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeParser;
import com.jetbrains.python.psi.types.TypeEvalContext;
import com.jetbrains.python.toolbox.ChainIterable;
import com.jetbrains.python.toolbox.Maybe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jetbrains.python.documentation.DocumentationBuilderKit.*;

class DocumentationBuilder {
  private final PsiElement myElement;
  private final PsiElement myOriginalElement;
  private ChainIterable<String> myResult;
  private ChainIterable<String> myProlog;      // sequence for reassignment info, etc
  private ChainIterable<String> myBody;        // sequence for doc string
  private ChainIterable<String> myEpilog;      // sequence for doc "copied from" notices and such

  private static final Pattern ourSpacesPattern = Pattern.compile("^\\s+");

  public DocumentationBuilder(PsiElement element, PsiElement originalElement) {
    myElement = element;
    myOriginalElement = originalElement;
    myResult = new ChainIterable<String>();
    myProlog = new ChainIterable<String>();
    myBody = new ChainIterable<String>();
    myEpilog = new ChainIterable<String>();

    myResult.add(myProlog).addWith(TagCode, myBody).add(myEpilog); // pre-assemble; then add stuff to individual cats as needed
    myResult = wrapInTag("html", wrapInTag("body", myResult));
  }

  public String build() {
    final ChainIterable<String> reassign_cat = new ChainIterable<String>(); // sequence for reassignment info, etc
    PsiElement followed = resolveToDocStringOwner(reassign_cat);

    // check if we got a property ref.
    // if so, element is an accessor, and originalElement if an identifier
    // TODO: use messages from resources!
    PyClass cls;
    PsiElement outer = null;
    boolean is_property = false;
    String accessor_kind = "None";
    if (myOriginalElement != null) {
      String element_name = myOriginalElement.getText();
      if (PyUtil.isPythonIdentifier(element_name)) {
        outer = myOriginalElement.getParent();
        if (outer instanceof PyQualifiedExpression) {
          PyExpression qual = ((PyQualifiedExpression)outer).getQualifier();
          if (qual != null) {
            PyType type = qual.getType(TypeEvalContext.fast());
            if (type instanceof PyClassType) {
              cls = ((PyClassType)type).getPyClass();
              if (cls != null) {
                Property property = cls.findProperty(element_name);
                if (property != null) {
                  is_property = true;
                  final AccessDirection dir = AccessDirection.of((PyElement)outer);
                  Maybe<PyFunction> accessor = property.getByDirection(dir);
                  myProlog
                    .addItem("property ").addWith(TagBold, $().addWith(TagCode, $(element_name)))
                    .addItem(" of ").add(PythonDocumentationProvider.describeClass(cls, TagCode, true, true))
                  ;
                  if (accessor.isDefined() && property.getDoc() != null) {
                    myBody.addItem(": ").addItem(property.getDoc()).addItem(BR);
                  }
                  else {
                    final PyFunction getter = property.getGetter().valueOrNull();
                    if (getter != null && getter != myElement) {
                      // not in getter, getter's doc comment may be useful
                      PyStringLiteralExpression docstring = getter.getDocStringExpression();
                      if (docstring != null) {
                        myProlog
                          .addItem(BR).addWith(TagItalic, $("Copied from getter:")).addItem(BR)
                          .addItem(docstring.getStringValue())
                        ;
                      }
                    }
                    myBody.addItem(BR);
                  }
                  myBody.addItem(BR);
                  if (accessor.isDefined() && accessor.value() == null) followed = null;
                  if (dir == AccessDirection.READ) accessor_kind = "Getter";
                  else if (dir == AccessDirection.WRITE) accessor_kind = "Setter";
                  else accessor_kind = "Deleter";
                  if (followed != null) myEpilog.addWith(TagSmall, $(BR, BR, accessor_kind, " of property")).addItem(BR);
                }
              }
            }
          }
        }
      }
    }

    if (myProlog.isEmpty() && !is_property && !isAttribute()) {
      myProlog.add(reassign_cat);
    }

    // now followed may contain a doc string
    if (followed instanceof PyDocStringOwner) {
      String docString = null;
      PyStringLiteralExpression doc_expr = ((PyDocStringOwner) followed).getDocStringExpression();
      if (doc_expr != null) docString = doc_expr.getStringValue();
      // doc of what?
      if (followed instanceof PyClass) {
        cls = (PyClass)followed;
        myBody.add(PythonDocumentationProvider.describeDecorators(cls, TagItalic, BR, LCombUp));
        myBody.add(PythonDocumentationProvider.describeClass(cls, TagBold, true, false));
      }
      else if (followed instanceof PyFunction) {
        PyFunction fun = (PyFunction)followed;
        if (!is_property) {
          cls = fun.getContainingClass();
          if (cls != null) {
            myBody.addWith(TagSmall, PythonDocumentationProvider.describeClass(cls, TagCode, true, true)).addItem(BR).addItem(BR);
          }
        }
        else cls = null;
        myBody
          .add(PythonDocumentationProvider.describeDecorators(fun, TagItalic, BR, LCombUp))
          .add(PythonDocumentationProvider.describeFunction(fun, TagBold, LCombUp));
        if (docString == null) {
          addInheritedDocString(fun, cls);
        }
      }
      else if (followed instanceof PyFile) {
        addModulePath((PyFile) followed);
      }
      if (docString != null) {
        myBody.addItem(BR);
        addFormattedDocString(myElement, docString, myBody, myEpilog);
      }
    }
    else if (is_property) {
      // if it was a normal accessor, ti would be a function, handled by previous branch
      String accessor_message;
      if (followed != null) accessor_message = "Declaration: ";
      else accessor_message = accessor_kind + " is not defined.";
      myBody.addWith(TagItalic, $(accessor_message)).addItem(BR);
      if (followed != null) myBody.addItem(combUp(PyUtil.getReadableRepr(followed, false)));
    }
    else if (isAttribute()) {
      addAttributeDoc();
    }
    else if (followed instanceof PyNamedParameter) {
      myBody.addItem(combUp(PyUtil.getReadableRepr(followed, false)));
      addParameterDoc((PyNamedParameter) followed);
    }
    else if (followed != null && outer instanceof PyReferenceExpression) {
      myBody
        .addItem(BR)
        .addItem(combUp(PythonDocumentationProvider.describeExpressionType((PyReferenceExpression)outer)));
    }
    if (myBody.isEmpty() && myEpilog.isEmpty()) return null; // got nothing substantial to say!
    else return myResult.toString();
  }

  private boolean isAttribute() {
    return myElement instanceof PyTargetExpression &&
             (PyUtil.isInstanceAttribute((PyTargetExpression)myElement) ||
              PsiTreeUtil.getParentOfType(myElement, ScopeOwner.class) instanceof PyClass);
  }

  @Nullable
  private PsiElement resolveToDocStringOwner(ChainIterable<String> prolog_cat) {
    // here the ^Q target is already resolved; the resolved element may point to intermediate assignments
    if (myElement instanceof PyTargetExpression) {
      final String target_name = myElement.getText();
      //prolog_cat.add(TagSmall.apply($("Assigned to ", element.getText(), BR)));
      prolog_cat.addWith(TagSmall, $(PyBundle.message("QDOC.assigned.to.$0", target_name)).addItem(BR));
      return ((PyTargetExpression)myElement).findAssignedValue();
    }
    if (myElement instanceof PyReferenceExpression) {
      //prolog_cat.add(TagSmall.apply($("Assigned to ", element.getText(), BR)));
      prolog_cat.addWith(TagSmall, $(PyBundle.message("QDOC.assigned.to.$0", myElement.getText())).addItem(BR));
      final QualifiedResolveResult resolveResult = ((PyReferenceExpression)myElement).followAssignmentsChain(PyResolveContext.noImplicits());
      return resolveResult.isImplicit() ? null : resolveResult.getElement();
    }
    // it may be a call to a standard wrapper
    if (myElement instanceof PyCallExpression) {
      final PyCallExpression call = (PyCallExpression)myElement;
      Pair<String, PyFunction> wrap_info = PyCallExpressionHelper.interpretAsModifierWrappingCall(call, myOriginalElement);
      if (wrap_info != null) {
        String wrapper_name = wrap_info.getFirst();
        PyFunction wrapped_func = wrap_info.getSecond();
        //prolog_cat.addWith(TagSmall, $("Wrapped in ").addWith(TagCode, $(wrapper_name)).add(BR));
        prolog_cat.addWith(TagSmall, $(PyBundle.message("QDOC.wrapped.in.$0", wrapper_name)).addItem(BR));
        return wrapped_func;
      }
    }
    return myElement;
  }

  private void addInheritedDocString(PyFunction fun, PyClass cls) {
    boolean not_found = true;
    String meth_name = fun.getName();
    if (cls != null && meth_name != null) {
      final boolean is_constructor = PyNames.INIT.equals(meth_name);
      // look for inherited and its doc
      Iterable<PyClass> classes = cls.iterateAncestorClasses();
      if (is_constructor) {
        // look at our own class again and maybe inherit class's doc
        classes = new ChainIterable<PyClass>(cls).add(classes);
      }
      for (PyClass ancestor : classes) {
        PyStringLiteralExpression doc_elt = null;
        PyFunction inherited = null;
        boolean is_from_class = false;
        if (is_constructor) doc_elt = cls.getDocStringExpression();
        if (doc_elt != null) is_from_class = true;
        else inherited = ancestor.findMethodByName(meth_name, false);
        if (inherited != null) {
          doc_elt = inherited.getDocStringExpression();
        }
        if (doc_elt != null) {
          String inherited_doc = doc_elt.getStringValue();
          if (inherited_doc.length() > 1) {
            myEpilog.addItem(BR).addItem(BR);
            String ancestor_name = ancestor.getName();
            String marker = (cls == ancestor)? PythonDocumentationProvider.LINK_TYPE_CLASS : PythonDocumentationProvider.LINK_TYPE_PARENT;
            final String ancestor_link = $().addWith(new DocumentationBuilderKit.LinkWrapper(marker + ancestor_name), $(ancestor_name)).toString();
            if (is_from_class) myEpilog.addItem(PyBundle.message("QDOC.copied.from.class.$0", ancestor_link));
            else {
              myEpilog.addItem(PyBundle.message("QDOC.copied.from.$0.$1", ancestor_link, meth_name));
            }
            myEpilog.addItem(BR).addItem(BR);
            ChainIterable<String> formatted = new ChainIterable<String>();
            ChainIterable<String> unformatted = new ChainIterable<String>();
            addFormattedDocString(fun, inherited_doc, formatted, unformatted);
            myEpilog.addWith(TagCode, formatted).add(unformatted);
            not_found = false;
            break;
          }
        }
      }

      if (not_found) {
        // above could have not worked because inheritance is not searched down to 'object'.
        // for well-known methods, copy built-in doc string.
        // TODO: also handle predefined __xxx__ that are not part of 'object'.
        if (PyNames.UnderscoredAttributes.contains(meth_name)) {
          addPredefinedMethodDoc(fun, meth_name);
        }
      }
    }
  }

  private void addPredefinedMethodDoc(PyFunction fun, String meth_name) {
    PyClassType objtype = PyBuiltinCache.getInstance(fun).getObjectType(); // old- and new-style classes share the __xxx__ stuff
    if (objtype != null) {
      PyClass objcls = objtype.getPyClass();
      if (objcls != null) {
        PyFunction obj_underscored = objcls.findMethodByName(meth_name, false);
        if (obj_underscored != null) {
          PyStringLiteralExpression predefined_doc_expr = obj_underscored.getDocStringExpression();
          String predefined_doc = predefined_doc_expr != null? predefined_doc_expr.getStringValue() : null;
          if (predefined_doc != null && predefined_doc.length() > 1) { // only a real-looking doc string counts
            addFormattedDocString(fun, predefined_doc, myBody, myBody);
            myEpilog.addItem(BR).addItem(BR).addItem(PyBundle.message("QDOC.copied.from.builtin"));
          }
        }
      }
    }
  }

  private static void addFormattedDocString(PsiElement element, @NotNull String docstring,
                                            ChainIterable<String> formattedOutput, ChainIterable<String> unformattedOutput) {
    Project project = element.getProject();
    PyDocumentationSettings documentationSettings = PyDocumentationSettings.getInstance(project);
    List<String> result = new ArrayList<String>();
    String[] lines = removeCommonIndentation(docstring);
    String preparedDocstring = StringUtil.join(lines, "\n");
    if (documentationSettings.isEpydocFormat(element.getContainingFile())) {
      Module module = ModuleUtil.findModuleForPsiElement(element);
      String formatted = null;
      final EpydocString epydocString = new EpydocString(preparedDocstring);
      if (module != null) {
        formatted = EpydocRunner.formatDocstring(module, preparedDocstring);
      }
      if (formatted == null) {
        formatted = epydocString.getDescription();
      }
      result.add(formatted);
      result.add(formatStructuredDocString(epydocString));
      unformattedOutput.add(result);
      return;
    }
    else if (documentationSettings.isReSTFormat(element.getContainingFile())) {
      Module module = ModuleUtil.findModuleForPsiElement(element);
      String formatted = null;
      if (module != null) {
        formatted = ReSTRunner.formatDocstring(module, preparedDocstring);
      }
      if (formatted == null) {
        formatted = new SphinxDocString(preparedDocstring).getDescription();
      }
      result.add(formatted);
      unformattedOutput.add(result);
      return;
    }
    boolean is_first;

    // reconstruct back, dropping first empty fragment as needed
    is_first = true;
    int tabSize = CodeStyleSettingsManager.getSettings(project).getTabSize(PythonFileType.INSTANCE);
    for (String line : lines) {
      if (is_first && ourSpacesPattern.matcher(line).matches()) continue; // ignore all initial whitespace
      if (is_first) is_first = false;
      else result.add(BR);
      int leadingTabs = 0;
      while (leadingTabs < line.length() && line.charAt(leadingTabs) == '\t') {
        leadingTabs++;
      }
      if (leadingTabs > 0) {
        line = StringUtil.repeatSymbol(' ', tabSize * leadingTabs) + line.substring(leadingTabs);
      }
      result.add(combUp(line));
    }
    formattedOutput.add(result);
  }

  private void addParameterDoc(PyNamedParameter followed) {
    PyFunction function = PsiTreeUtil.getParentOfType(followed, PyFunction.class);
    if (function != null) {
      final String docString = PyUtil.strValue(function.getDocStringExpression());
      StructuredDocString structuredDocString = StructuredDocString.parse(docString);
      if (structuredDocString != null) {
        final String name = followed.getName();
        final String type = structuredDocString.getParamType(name);
        if (type != null) {
          final PyType pyType = PyTypeParser.getTypeByName(followed, type);
          if (pyType instanceof PyClassType && ((PyClassType)pyType).getPyClass() != null) {
            myBody.addItem(": ").
                addWith(new DocumentationBuilderKit.LinkWrapper(PythonDocumentationProvider.LINK_TYPE_PARAM),
                        $(pyType.getName()));
          }
          else
            myBody.addItem(": ").addItem(type);
        }
        final String desc = structuredDocString.getParamDescription(name);
        if (desc != null) {
          myEpilog.addItem(BR).addItem(desc);
        }
      }
    }
  }

  private void addAttributeDoc() {
    PyClass cls = PsiTreeUtil.getParentOfType(myElement, PyClass.class);
    assert cls != null;
    String type = PyUtil.isInstanceAttribute((PyExpression)myElement) ? "Instance attribute " : "Class attribute ";
    myProlog
      .addItem(type).addWith(TagBold, $().addWith(TagCode, $(((PyTargetExpression)myElement).getName())))
      .addItem(" of class ").addWith(PythonDocumentationProvider.LinkMyClass, $().addWith(TagCode, $(cls.getName()))).addItem(BR)
    ;

    final String docString = PyUtil.strValue(PyUtil.getAttributeDocString((PyTargetExpression)myElement));
    if (docString != null) {
      addFormattedDocString(myElement, docString, myBody, myEpilog);
    }
  }

  private static String[] removeCommonIndentation(String docstring) {
    // detect common indentation
    String[] lines = LineTokenizer.tokenize(docstring, false);
    boolean is_first = true;
    int cut_width = Integer.MAX_VALUE;
    int firstIndentedLine = 0;
    for (String frag : lines) {
      if (frag.length() == 0) continue;
      int pad_width = 0;
      final Matcher matcher = ourSpacesPattern.matcher(frag);
      if (matcher.find()) {
        pad_width = matcher.end();
      }
      if (is_first) {
        is_first = false;
        if (pad_width == 0) {    // first line may have zero padding
          firstIndentedLine = 1;
          continue;
        }
      }
      if (pad_width < cut_width) cut_width = pad_width;
    }
    // remove common indentation
    if (cut_width > 0 && cut_width < Integer.MAX_VALUE) {
      for (int i = firstIndentedLine; i < lines.length; i+= 1) {
        if (lines[i].length() > 0) lines[i] = lines[i].substring(cut_width);
      }
    }
    List<String> result = new ArrayList<String>();
    for (String line : lines) {
      if (line.startsWith(PyConsoleUtil.ORDINARY_PROMPT)) break;
      result.add(line);
    }
    return result.toArray(new String[result.size()]);
  }

  private static String formatStructuredDocString(StructuredDocString docString) {
    StringBuilder result = new StringBuilder();

    final String attributeDescription = docString.getAttributeDescription();
    if (attributeDescription != null) {
      result.append(attributeDescription);
      final String attrType = docString.getParamType(null);
      if (attrType != null) {
        result.append(" <i>Type: ").append(attrType).append("</i>");
      }
    }

    formatParameterDescriptions(docString, result, false);
    formatParameterDescriptions(docString, result, true);

    final String returnDescription = docString.getReturnDescription();
    final String returnType = docString.getReturnType();
    if (returnDescription != null || returnType != null) {
      result.append("<br><b>Return value:</b><br>");
      if (returnDescription != null) {
        result.append(returnDescription);
      }
      if (returnType != null) {
        result.append(" <i>Type: ").append(returnType).append("</i>");
      }
    }

    final List<String> raisedException = docString.getRaisedExceptions();
    if (raisedException.size() > 0) {
      result.append("<br><b>Raises:</b><br>");
      for (String s : raisedException) {
        result.append("<b>").append(s).append("</b> - ").append(docString.getRaisedExceptionDescription(s)).append("<br>");
      }
    }

    List<String> additionalTags = docString.getAdditionalTags();
    if (!additionalTags.isEmpty()) {
      result.append("<br/><br/><b>Additional:</b><br/>");
      result.append("<table>");
      for (String tagName : additionalTags) {
        final List<Substring> args = docString.getTagArguments(tagName);
        for(Substring arg : args) {
          final String s = arg.toString();
          result.append("<tr><td align=\"right\"><b>").append(tagName);
          result.append(" ").append(s).append(":</b>");
          result.append("</td><td>").append(docString.getTagValue(tagName, s)).append("</td></tr>");
        }
        result.append("</table>");
      }
    }
    return result.toString();
  }

  private static void formatParameterDescriptions(StructuredDocString docString,
                                                  StringBuilder result,
                                                  boolean keyword) {
    List<String> parameters = keyword ? docString.getKeywordArguments() : docString.getParameters();
    if (parameters.size() > 0) {
      result.append("<br><b>").append(keyword ? "Keyword arguments:" : "Parameters").append("</b><br>");
      for (String parameter : parameters) {
        final String description = keyword ? docString.getKeywordArgumentDescription(parameter) : docString.getParamDescription(parameter);
        result.append("<b>").append(parameter).append("</b>: ").append(description);
        final String paramType = docString.getParamType(parameter);
        if (paramType != null) {
          result.append(" <i>Type: ").append(paramType).append("</i>");
        }
        result.append("<br>");
      }
    }
  }

  private void addModulePath(PyFile followed) {
    // what to prepend to a module description?
    final VirtualFile file = followed.getVirtualFile();
    if (file == null) {
      myProlog.addWith(TagSmall, $(PyBundle.message("QDOC.module.path.unknown")));
    }
    else {
      final String path = file.getPath();
      RootFinder finder = new RootFinder(path);
      RootVisitorHost.visitRoots(followed, finder);
      final String root_path = finder.getResult();
      if (root_path != null) {
        String after_part = path.substring(root_path.length());
        myProlog.addWith(TagSmall, $(root_path).addWith(TagBold, $(after_part)));
      }
      else myProlog.addWith(TagSmall, $(path));
    }
  }

  private static class RootFinder implements RootVisitor {
    private String myResult;
    private String myPath;

    private RootFinder(String path) {
      myPath = path;
    }

    public boolean visitRoot(VirtualFile root, Module module, Sdk sdk) {
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
}