package com.jetbrains.python.psi.search;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.searches.ExtensibleQueryFactory;
import com.intellij.util.Query;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;

import java.util.List;

/**
 * @author yole
 */
public class PySuperMethodsSearch extends ExtensibleQueryFactory<PsiElement, PySuperMethodsSearch.SearchParameters> {
  public static PySuperMethodsSearch INSTANCE = new PySuperMethodsSearch();

  public static PyFunction getBaseMethod(List<PsiElement> superMethods,
                                         PyClass containingClass) {

    for (PyClass ancestor : containingClass.iterateAncestorClasses()) {
      for (PsiElement method : superMethods) {
        if (ancestor.equals(((PyFunction)method).getContainingClass()))
          return (PyFunction)method;
      }
    }
    return (PyFunction) superMethods.get(superMethods.size()-1);
  }

  public static class SearchParameters {
    private final PyFunction myDerivedMethod;
    private final boolean myDeepSearch;

    public SearchParameters(final PyFunction derivedMethod, boolean deepSearch) {
      myDerivedMethod = derivedMethod;
      myDeepSearch = deepSearch;
    }

    public PyFunction getDerivedMethod() {
      return myDerivedMethod;
    }

    public boolean isDeepSearch() {
      return myDeepSearch;
    }
  }

  private PySuperMethodsSearch() {
    super("Pythonid");
  }

  public static Query<PsiElement> search(final PyFunction derivedMethod) {
    final SearchParameters parameters = new SearchParameters(derivedMethod, false);
    return INSTANCE.createUniqueResultsQuery(parameters);
  }

  public static Query<PsiElement> search(final PyFunction derivedMethod, boolean deepSearch) {
    final SearchParameters parameters = new SearchParameters(derivedMethod, deepSearch);
    return INSTANCE.createUniqueResultsQuery(parameters);
  }
}