package com.jetbrains.python.psi.types;

import com.google.common.base.CharMatcher;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.documentation.DocstringUtil;
import com.jetbrains.python.psi.LanguageLevel;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.stubs.PyClassNameIndex;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yole
 */
public class PyTypeParser {
  private static final Pattern PARAMETRIZED_CLASS = Pattern.compile("([a-zA-Z0-9._]+) of (.+)");

  private PyTypeParser() {
  }

  @Nullable
  public static PyType getTypeByName(PsiElement anchor, String type) {
    if (type == null) {
      return null;
    }

    type = type.trim();
    if (type.endsWith(".")) {
      type = type.substring(0, type.length()-1);
    }
    if (type.equals("None")) {
      return PyNoneType.INSTANCE;
    }

    if (type.startsWith("(") && type.endsWith(")")) {
      return parseTupleType(anchor, type.substring(1, type.length()-1));
    }
    if (type.contains(" or ")) {
      return parseUnionType(anchor, type);
    }

    final PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(anchor);

    if (type.equals("unknown")) {
      return null;
    }
    if (type.equals("string")) {
      return builtinCache.getStringType(LanguageLevel.forElement(anchor));
    }
    if (type.equals("str")) {
      return builtinCache.getStrType();
    }
    if (type.equals("bytes")) {
      return builtinCache.getBytesType(LanguageLevel.forElement(anchor));
    }
    if (type.equals("boolean")) {
      return builtinCache.getBoolType();
    }
    if (type.equals("file object")) {
      return builtinCache.getObjectType("file");
    }
    if (type.equals("dictionary")) {
      return builtinCache.getObjectType("dict");
    }
    if (type.startsWith("dict from")) {
      return parseDictType(anchor, type.substring(9).trim());
    }
    if (type.equals("integer")) {
      return builtinCache.getIntType();
    }
    final Matcher m = PARAMETRIZED_CLASS.matcher(type);
    if (m.matches()) {
      final PyType objType = parseObjectType(anchor, m.group(1), builtinCache);
      final PyType elementType = getTypeByName(anchor, m.group(2));
      if (objType != null) {
        if (objType instanceof PyClassType && elementType != null) {
          return new PyCollectionTypeImpl(((PyClassType)objType).getPyClass(), false, elementType);
        }
        return objType;
      }
    }
    return parseObjectType(anchor, type, builtinCache);
  }

  @Nullable
  private static PyType parseObjectType(PsiElement anchor, String type, PyBuiltinCache builtinCache) {
    final PyClassType classType = builtinCache.getObjectType(type);
    if (classType != null) {
      return classType;
    }
    final PsiFile anchorFile = anchor.getContainingFile();
    if (anchorFile instanceof PyFile) {
      final PyClass aClass = ((PyFile)anchorFile).findTopLevelClass(type);
      if (aClass != null) {
        return new PyClassType(aClass, false);
      }
    }
    if (StringUtil.isJavaIdentifier(type)) {
      final Collection<PyClass> classes = PyClassNameIndex.find(type, anchor.getProject(), true);
      if (classes.size() == 1) {
        return new PyClassType(classes.iterator().next(), false);
      }
    }
    if (CharMatcher.JAVA_LETTER_OR_DIGIT.or(CharMatcher.is('.')).or(CharMatcher.is('_')).matchesAllOf(type)) {
      final int pos = type.lastIndexOf('.');
      final String shortName = pos > 0 ? type.substring(pos + 1) : type;
      final Collection<PyClass> classes = PyClassNameIndex.find(shortName, anchor.getProject(), true);
      for (PyClass aClass : classes) {
        if (type.equals(aClass.getQualifiedName())) {
          return new PyClassType(aClass, false);
        }
      }
      // Workaround for stdlib modules _abcoll, _functools, etc.
      for (PyClass aClass : classes) {
        if (aClass.getQualifiedName().startsWith("_")) {
          return new PyClassType(aClass, false);
        }
      }
    }
    return null;
  }

  private static PyType parseTupleType(PsiElement anchor, String elementTypeNames) {
    final List<String> elements = StringUtil.split(elementTypeNames, ",");
    PyType[] elementTypes = new PyType[elements.size()];
    for (int i = 0; i < elementTypes.length; i++) {
      elementTypes [i] = getTypeByName(anchor, elements.get(i).trim());
    }
    return new PyTupleType(anchor, elementTypes);
  }

  private static PyType parseListType(PsiElement anchor, String elementTypeName) {
    PyClass list = PyBuiltinCache.getInstance(anchor).getClass("list");
    PyType elementType = getTypeByName(anchor, elementTypeName);
    return new PyCollectionTypeImpl(list, false, elementType);
  }

  @Nullable
  private static PyType parseDictType(PsiElement anchor, String fromToTypeNames) {
    int pos = fromToTypeNames.indexOf(" to ");
    if (pos > 0) {
      String toTypeName = fromToTypeNames.substring(pos + 4).trim();
      PyClass dict = PyBuiltinCache.getInstance(anchor).getClass("dict");
      return new PyCollectionTypeImpl(dict, false, getTypeByName(anchor, toTypeName));
    }
    return PyBuiltinCache.getInstance(anchor).getDictType();
  }

  @Nullable
  private static PyType parseUnionType(PsiElement anchor, String type) {
    final List<String> elements = StringUtil.split(type, " or ");
    PyType result = null;
    for (String element : elements) {
      PyType elementType = getTypeByName(anchor, element);
      if (elementType != null) {
        if (result == null) {
          result = elementType;
        }
        else {
          result = PyUnionType.union(result, elementType);
        }
      }
    }
    return result;
  }

  public static Map<TextRange, PyType> parseDocstring(PsiElement anchor, String docstring, int position) {
    int length = docstring.length();
    docstring = DocstringUtil.unifyDocstring(docstring);
    PyType type = getTypeByName(anchor, docstring);
    Map<TextRange, PyType> res = new HashMap<TextRange, PyType>();

    if (type instanceof PyClassType && type.getName() != null)
      res.put(new TextRange(position, position + length), type);

    if (type instanceof PyUnionType) {
      List<PyType> types = ((PyUnionType)type).getMembers();
      for (int i = 0; i != types.size(); ++i) {
        String name = types.get(i).getName();
        if (name != null) {
          int start = position+docstring.indexOf(name);
          if (start != -1)
            res.put(new TextRange(start, start+name.length()), types.get(i));
        }
      }
    }

    return res;
  }
}