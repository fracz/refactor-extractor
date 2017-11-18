package com.jetbrains.python;

import org.jetbrains.annotations.NonNls;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author dcheryasov
 */
public class PyNames {
  private PyNames() {
  }

  @NonNls public static final String INIT = "__init__";
  @NonNls public static final String DOT_PY = ".py";
  @NonNls public static final String INIT_DOT_PY = INIT + DOT_PY;

  @NonNls public static final String NEW = "__new__";

  @NonNls public static final String OBJECT = "object";
  @NonNls public static final String NONE = "None";

  @NonNls public static final String CLASSMETHOD = "classmethod";
  @NonNls public static final String STATICMETHOD = "staticmethod";

  @NonNls private static final Set<String> _UnderscoredAttributes = new HashSet<String>();
  static {
    _UnderscoredAttributes.add("__all__");
    _UnderscoredAttributes.add("__author__");
    _UnderscoredAttributes.add("__bases__");
    //_UnderscoredAttributes.add("__builtins__");
    //_UnderscoredAttributes.add("__debug__");
    //_UnderscoredAttributes.add("__dict__");
    _UnderscoredAttributes.add("__docformat__");
    _UnderscoredAttributes.add("__file__");
    //_UnderscoredAttributes.add("__future__");
    //_UnderscoredAttributes.add("__import__");
    _UnderscoredAttributes.add("__members__");
    _UnderscoredAttributes.add("__metaclass__");
    _UnderscoredAttributes.add("__mod__");
    _UnderscoredAttributes.add("__mro__");
    _UnderscoredAttributes.add("__name__");
    _UnderscoredAttributes.add("__path__");
    _UnderscoredAttributes.add("__self__");
    _UnderscoredAttributes.add("__slots__");
    _UnderscoredAttributes.add("__version__");
  }

  /**
   * Contains all known predefined names of "__foo__" form.
   */
  public static Set<String> UnderscoredAttributes = Collections.unmodifiableSet(_UnderscoredAttributes);

  public static class BuiltinDescription {
    private final String mySignature;

    public BuiltinDescription(String signature) {
      mySignature = signature;
    }

    public String getSignature() {
      return mySignature;
    }

    // TODO: doc string, too
  }

  private static final Map<String, BuiltinDescription> _BuiltinMethods = new TreeMap<String, BuiltinDescription>();


  static {
    final BuiltinDescription _only_self_descr = new BuiltinDescription("(self)");
    final BuiltinDescription _self_other_descr = new BuiltinDescription("(self, other)");
    final BuiltinDescription _self_item_descr = new BuiltinDescription("(self, item)");
    final BuiltinDescription _self_key_descr = new BuiltinDescription("(self, key)");

    _BuiltinMethods.put("__abs__", _only_self_descr);
    _BuiltinMethods.put("__add__", _self_other_descr);
    //_BuiltinMethods.put("__all__", _only_self_descr);
    //_BuiltinMethods.put("__author__", _only_self_descr);
    //_BuiltinMethods.put("__bases__", _only_self_descr);
    _BuiltinMethods.put("__call__", new BuiltinDescription("(self, *args, **kwargs)"));
    //_BuiltinMethods.put("__class__", _only_self_descr);
    _BuiltinMethods.put("__cmp__", _self_other_descr);
    _BuiltinMethods.put("__coerce__", _self_other_descr);
    _BuiltinMethods.put("__contains__", _self_item_descr);
    //_BuiltinMethods.put("__debug__", _only_self_descr);
    _BuiltinMethods.put("__del__", _only_self_descr);
    _BuiltinMethods.put("__delattr__", _self_item_descr);
    _BuiltinMethods.put("__delitem__", _self_key_descr);
    _BuiltinMethods.put("__delslice__", new BuiltinDescription("(self, i, j)"));
    //_BuiltinMethods.put("__dict__", _only_self_descr);
    _BuiltinMethods.put("__div__", _self_other_descr);
    _BuiltinMethods.put("__divmod__", _self_other_descr);
    //_BuiltinMethods.put("__doc__", _only_self_descr);
    //_BuiltinMethods.put("__docformat__", _only_self_descr);
    _BuiltinMethods.put("__eq__", _self_other_descr);
    //_BuiltinMethods.put("__file__", _only_self_descr);
    _BuiltinMethods.put("__float__", _only_self_descr);
    _BuiltinMethods.put("__floordiv__", _self_other_descr);
    //_BuiltinMethods.put("__future__", _only_self_descr);
    _BuiltinMethods.put("__ge__", _self_other_descr);
    _BuiltinMethods.put("__getattr__", _self_item_descr);
    _BuiltinMethods.put("__getattribute__", _self_item_descr);
    _BuiltinMethods.put("__getitem__", _self_item_descr);
    //_BuiltinMethods.put("__getslice__", new BuiltinDescription("(self, i, j)"));
    _BuiltinMethods.put("__gt__", _self_other_descr);
    _BuiltinMethods.put("__hash__", _only_self_descr);
    _BuiltinMethods.put("__hex__", _only_self_descr);
    _BuiltinMethods.put("__iadd__", _self_other_descr);
    //_BuiltinMethods.put("__import__", _only_self_descr);
    _BuiltinMethods.put("__imul__", _self_other_descr);
    _BuiltinMethods.put(INIT, _only_self_descr);
    _BuiltinMethods.put("__int__", _only_self_descr);
    _BuiltinMethods.put("__invert__", _only_self_descr);
    _BuiltinMethods.put("__iter__", _only_self_descr);
    _BuiltinMethods.put("__le__", _self_other_descr);
    _BuiltinMethods.put("__len__", _only_self_descr);
    _BuiltinMethods.put("__long__", _only_self_descr);
    _BuiltinMethods.put("__lshift__", _self_other_descr);
    _BuiltinMethods.put("__lt__", _self_other_descr);
    //_BuiltinMethods.put("__members__", _only_self_descr);
    //_BuiltinMethods.put("__metaclass__", _only_self_descr);
    _BuiltinMethods.put("__mod__", _self_other_descr);
    //_BuiltinMethods.put("__mro__", _only_self_descr);
    _BuiltinMethods.put("__mul__", _self_other_descr);
    //_BuiltinMethods.put("__name__", _only_self_descr);
    _BuiltinMethods.put("__ne__", _self_other_descr);
    _BuiltinMethods.put("__neg__", _only_self_descr);
    _BuiltinMethods.put(NEW, new BuiltinDescription("(cls, *args, **kwargs)"));
    _BuiltinMethods.put("__nonzero__", _only_self_descr);
    _BuiltinMethods.put("__oct__", _only_self_descr);
    _BuiltinMethods.put("__or__", _self_other_descr);
    //_BuiltinMethods.put("__path__", _only_self_descr);
    _BuiltinMethods.put("__pos__", _only_self_descr);
    _BuiltinMethods.put("__pow__", new BuiltinDescription("(self, power, modulo=None)"));
    _BuiltinMethods.put("__radd__", _self_other_descr);
    _BuiltinMethods.put("__rdiv__", _self_other_descr);
    _BuiltinMethods.put("__rdivmod__", _self_other_descr);
    _BuiltinMethods.put("__reduce__", _only_self_descr);
    _BuiltinMethods.put("__repr__", _only_self_descr);
    _BuiltinMethods.put("__rfloordiv__", _self_other_descr);
    _BuiltinMethods.put("__rlshift__", _self_other_descr);
    _BuiltinMethods.put("__rmod__", _self_other_descr);
    _BuiltinMethods.put("__rmul__", _self_other_descr);
    _BuiltinMethods.put("__ror__", _self_other_descr);
    _BuiltinMethods.put("__rpow__", new BuiltinDescription("(self, power, modulo=None)"));
    _BuiltinMethods.put("__rrshift__", _self_other_descr);
    _BuiltinMethods.put("__rsub__", _self_other_descr);
    _BuiltinMethods.put("__rtruediv__", _self_other_descr);
    _BuiltinMethods.put("__rxor__", _self_other_descr);
    _BuiltinMethods.put("__setattr__", new BuiltinDescription("(self, key, value)"));
    _BuiltinMethods.put("__setitem__", new BuiltinDescription("(self, key, value)"));
    _BuiltinMethods.put("__setslice__", new BuiltinDescription("(self, i, j, sequence)"));
    //_BuiltinMethods.put("__self__", _only_self_descr);
    //_BuiltinMethods.put("__slots__", _only_self_descr);
    _BuiltinMethods.put("__str__", _only_self_descr);
    _BuiltinMethods.put("__sub__", _self_other_descr);
    _BuiltinMethods.put("__truediv__", _self_other_descr);
    //_BuiltinMethods.put("__version__", _only_self_descr);
    _BuiltinMethods.put("__xor__", _self_other_descr);
  }

  public static final Map<String, BuiltinDescription> BuiltinMethods = Collections.unmodifiableMap(_BuiltinMethods);

  // canonical names, not forced by interpreter
  public static final String CANONICAL_SELF = "self";

  @NonNls private static final Set<String> _Keywords = new HashSet<String>();
  static {
    _Keywords.add("and");
    _Keywords.add("del");
    _Keywords.add("from");
    _Keywords.add("not");
    _Keywords.add("while");
    _Keywords.add("as");
    _Keywords.add("elif");
    _Keywords.add("global");
    _Keywords.add("or");
    _Keywords.add("with");
    _Keywords.add("assert");
    _Keywords.add("else");
    _Keywords.add("if");
    _Keywords.add("pass");
    _Keywords.add("yield");
    _Keywords.add("break");
    _Keywords.add("except");
    _Keywords.add("import");
    _Keywords.add("print");
    _Keywords.add("class");
    _Keywords.add("exec");
    _Keywords.add("in");
    _Keywords.add("raise");
    _Keywords.add("continue");
    _Keywords.add("finally");
    _Keywords.add("is");
    _Keywords.add("return");
    _Keywords.add("def");
    _Keywords.add("for");
    _Keywords.add("lambda");
    _Keywords.add("try");
  }

  /**
   * Contains keywords as of CPython 2.5.
   */
  public static Set<String> Keywords = Collections.unmodifiableSet(_Keywords);

  /**
   * TODO: dependency on language level.
   * @param name what to check
   * @return true iff the name is either a keyword or a reserved name, like None.
   *
   */
  public static boolean isReserved(@NonNls String name) {
    return Keywords.contains(name) || NONE.equals(name) || "as".equals(name) || "with".equals(name);
  }

  // NOTE: includes unicode only good for py3k
  private final static Pattern IDENTIFIER_PATTERN = Pattern.compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*");

  /**
   * TODO: dependency on language level.
   * @param name what to check
   * @return true iff name is not reserved and is a well-formed identifier.
   */
  public static boolean isIdentifier(@NonNls String name) {
    return ! isReserved(name) && IDENTIFIER_PATTERN.matcher(name).matches();
  }


}