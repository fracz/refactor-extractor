package com.jetbrains.python.psi.types;

import com.google.common.base.CharMatcher;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.documentation.DocStringUtil;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.impl.PyImportedModule;
import com.jetbrains.python.psi.impl.PyQualifiedName;
import com.jetbrains.python.psi.stubs.PyClassNameIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yole
 */
public class PyTypeParser {
  private static final Pattern PARAMETRIZED_CLASS = Pattern.compile("([a-zA-Z0-9._]+) of (.+)");
  private static final Pattern DICT_TYPE = Pattern.compile("dict from (.+) to (.+)");

  private PyTypeParser() {
  }

  @Nullable
  public static PyType getTypeByName(PsiElement anchor, String type) {
    final ParseResult result = parse(anchor, type);
    return result.getType();
  }

  @NotNull
  public static ParseResult parse(PsiElement anchor, String type) {
    final Map<TextRange, PyType> types = new HashMap<TextRange, PyType>();
    final Map<PyType, TextRange> fullRanges = new HashMap<PyType, TextRange>();
    final PyType t = parse(anchor, type, types, fullRanges, 0);
    return new ParseResult(t, types, fullRanges);
  }

  public static class ParseResult {
    private PyType myType;
    private Map<TextRange, PyType> myTypes;
    private Map<PyType, TextRange> myFullRanges;

    ParseResult(PyType type, Map<TextRange, PyType> types, Map<PyType, TextRange> fullRanges) {
      myType = type;
      myTypes = types;
      myFullRanges = fullRanges;
    }

    public PyType getType() {
      return myType;
    }

    public Map<TextRange, PyType> getTypes() {
      return myTypes;
    }

    public Map<PyType, TextRange> getFullRanges() {
      return myFullRanges;
    }
  }

  @Nullable
  private static PyType parse(PsiElement anchor, String type, Map<TextRange, PyType> types, Map<PyType, TextRange> fullRanges, int offset) {
    if (type == null) {
      return null;
    }

    final String trimmed = DocStringUtil.trimDocString(type);
    offset += type.indexOf(trimmed);
    type = trimmed;
    final TextRange whole = new TextRange(offset, offset + type.length());
    if (type.equals("None")) {
      final PyType t = PyNoneType.INSTANCE;
      types.put(whole, t);
      return t;
    }
    if (type.startsWith("(") && type.endsWith(")")) {
      return parseTupleType(anchor, type.substring(1, type.length() - 1), types, fullRanges, offset + 1);
    }
    if (type.contains(" or ")) {
      return parseUnionType(anchor, type, types, fullRanges, offset);
    }

    final PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(anchor);

    if (type.equals("unknown")) {
      return null;
    }
    if (type.equals("string")) {
      PyType t = builtinCache.getStringType(LanguageLevel.forElement(anchor));
      types.put(whole, t);
      return t;
    }
    if (type.equals("str")) {
      final PyType t = builtinCache.getStrType();
      types.put(whole, t);
      return t;
    }
    if (type.equals("bytes")) {
      final PyType t = builtinCache.getBytesType(LanguageLevel.forElement(anchor));
      types.put(whole, t);
      return t;
    }
    if (type.equals("boolean")) {
      final PyType t = builtinCache.getBoolType();
      types.put(whole, t);
      return t;
    }
    if (type.equals("file object")) {
      final PyType t = builtinCache.getObjectType("file");
      types.put(whole, t);
      return t;
    }
    if (type.equals("dictionary")) {
      final PyType t = builtinCache.getObjectType("dict");
      types.put(whole, t);
      return t;
    }
    if (type.startsWith("dict from")) {
      return parseDictFromToType(anchor, type, types, fullRanges, offset);
    }
    if (type.equals("integer")) {
      final PyType t = builtinCache.getIntType();
      types.put(whole, t);
      return t;
    }
    final Matcher m = PARAMETRIZED_CLASS.matcher(type);
    if (m.matches()) {
      final PyType objType = parseObjectType(anchor, m.group(1), builtinCache, types, fullRanges, offset + m.start(1));
      final PyType elementType = parse(anchor, m.group(2), types, fullRanges, offset + m.start(2));
      if (objType != null) {
        if (objType instanceof PyClassType && elementType != null) {
          return new PyCollectionTypeImpl(((PyClassType)objType).getPyClass(), false, elementType);
        }
        return objType;
      }
    }
    return parseObjectType(anchor, type, builtinCache, types, fullRanges, offset);
  }

  @Nullable
  private static PyType parseObjectType(PsiElement anchor, String type, PyBuiltinCache builtinCache, Map<TextRange, PyType> types,
                                        Map<PyType, TextRange> fullRanges, int offset) {
    final TextRange whole = new TextRange(offset, offset + type.length());
    final PyClassType classType = builtinCache.getObjectType(type);
    if (classType != null) {
      types.put(whole, classType);
      return classType;
    }
    final PsiFile anchorFile = anchor.getContainingFile();
    if (anchorFile instanceof PyFile) {
      final PyClass aClass = ((PyFile)anchorFile).findTopLevelClass(type);
      if (aClass != null) {
        final PyType t = new PyClassType(aClass, false);
        types.put(whole, t);
        return t;
      }
    }
    if (StringUtil.isJavaIdentifier(type)) {
      final Collection<PyClass> classes = PyClassNameIndex.find(type, anchor.getProject(), true);
      if (classes.size() == 1) {
        final PyType t = new PyClassType(classes.iterator().next(), false);
        types.put(whole, t);
        return t;
      }
    }
    if (CharMatcher.JAVA_LETTER_OR_DIGIT.or(CharMatcher.is('.')).or(CharMatcher.is('_')).matchesAllOf(type)) {
      final List<TextRange> ranges = splitRanges(type, ".");
      final TextRange classRange = !ranges.isEmpty() ? ranges.remove(ranges.size() - 1) : new TextRange(0, type.length());
      if (!ranges.isEmpty()) {
        final TextRange first = ranges.get(0);
        for (TextRange range : ranges) {
          final PyQualifiedName moduleName = PyQualifiedName.fromDottedString(first.union(range).substring(type));
          final PyType t = new PyImportedModuleType(new PyImportedModule(anchor.getContainingFile(), moduleName));
          types.put(range.shiftRight(offset), t);
        }
      }
      final String shortName = classRange.substring(type);
      final Collection<PyClass> classes = PyClassNameIndex.find(shortName, anchor.getProject(), true);
      for (PyClass aClass : classes) {
        if (type.equals(aClass.getQualifiedName())) {
          final PyType t = new PyClassType(aClass, false);
          types.put(classRange.shiftRight(offset), t);
          fullRanges.put(t, whole);
          return t;
        }
      }
      // Workaround for stdlib modules _abcoll, _functools, etc.
      for (PyClass aClass : classes) {
        if (aClass.getQualifiedName().startsWith("_")) {
          final PyType t = new PyClassType(aClass, false);
          types.put(classRange.shiftRight(offset), t);
          fullRanges.put(t, whole);
          return t;
        }
      }
    }
    return null;
  }

  private static PyType parseTupleType(PsiElement anchor, String elementTypeNames, Map<TextRange, PyType> types,
                                       Map<PyType, TextRange> fullRanges, int offset) {
    final List<TextRange> ranges = splitRanges(elementTypeNames, ",");
    final List<PyType> elementTypes = new ArrayList<PyType>();
    for (TextRange range : ranges) {
      elementTypes.add(parse(anchor, range.substring(elementTypeNames), types, fullRanges, offset + range.getStartOffset()));
    }
    return new PyTupleType(anchor, elementTypes.toArray(new PyType[elementTypes.size()]));
  }

  @Nullable
  private static PyType parseDictFromToType(PsiElement anchor, String type, Map<TextRange, PyType> types,
                                            Map<PyType, TextRange> fullRanges, int offset) {
    final Matcher m = DICT_TYPE.matcher(type);
    PyClassType dict = PyBuiltinCache.getInstance(anchor).getDictType();
    if (dict != null) {
      if (m.matches()) {
        PyType to = parse(anchor, m.group(2), types, fullRanges, offset + m.start(2));
        return new PyCollectionTypeImpl(dict.getPyClass(), false, to);
      }
      return dict;
    }
    return null;
  }

  @Nullable
  private static PyType parseUnionType(PsiElement anchor, String type, Map<TextRange, PyType> types,
                                       Map<PyType, TextRange> fullRanges, int offset) {
    final List<TextRange> ranges = splitRanges(type, " or ");
    PyType result = null;
    for (TextRange range : ranges) {
      final PyType t = parse(anchor, range.substring(type), types, fullRanges, offset + range.getStartOffset());
      if (t != null) {
        result = (result == null) ? t : PyUnionType.union(result, t);
      }
    }
    return result;
  }

  @NotNull
  private static List<TextRange> splitRanges(@NotNull final String s, @NotNull final String separator) {
    List<TextRange> ranges = new ArrayList<TextRange>();
    final int len = s.length();
    final int seplen = separator.length();
    int end;
    for (int start = 0; start < len; start = end + seplen) {
      final int n = s.indexOf(separator, start);
      end = (n == -1) ? len : n;
      ranges.add(new TextRange(start, end));
    }
    return ranges;
  }
}