package com.jetbrains.python.psi.resolve;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementFactory;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class VariantsProcessor implements PsiScopeProcessor {
  private final Map<String, LookupElement> myVariants = new HashMap<String, LookupElement>();

  protected String my_notice;

  public VariantsProcessor() {
    // empty
  }

  public VariantsProcessor(final PyResolveUtil.Filter filter) {
    my_filter = filter;
  }

  protected PyResolveUtil.Filter my_filter;

  public void setNotice(@Nullable String notice) {
    my_notice = notice;
  }

  protected void setupItem(LookupItem item) {
    if (my_notice != null) {
      setItemNotice(item, my_notice);
    }
  }

  protected static void setItemNotice(final LookupItem item, String notice) {
    item.setAttribute(item.TAIL_TEXT_ATTR, notice);
    item.setAttribute(item.TAIL_TEXT_SMALL_ATTR, "");
  }

  public LookupElement[] getResult() {
    final Collection<LookupElement> variants = myVariants.values();
    return variants.toArray(new LookupElement[variants.size()]);
  }

  public List<LookupElement> getResultList() {
    return new ArrayList<LookupElement>(myVariants.values());
  }

  public boolean execute(PsiElement element, ResolveState substitutor) {
    if (my_filter != null && !my_filter.accept(element)) return true; // skip whatever the filter rejects
    // TODO: refactor to look saner; much code duplication
    if (element instanceof PsiNamedElement) {
      final PsiNamedElement psiNamedElement = (PsiNamedElement)element;
      final String name = psiNamedElement.getName();
      if (!myVariants.containsKey(name)) {
        final LookupItem lookup_item = (LookupItem)LookupElementFactory.getInstance().createLookupElement(psiNamedElement);
        setupItem(lookup_item);
        myVariants.put(name, lookup_item);
      }
    }
    else if (element instanceof PyReferenceExpression) {
      PyReferenceExpression expr = (PyReferenceExpression)element;
      String referencedName = expr.getReferencedName();
      if (referencedName != null && !myVariants.containsKey(referencedName)) {
        final LookupItem lookup_item = (LookupItem)LookupElementFactory.getInstance().createLookupElement(referencedName);
        setupItem(lookup_item);
        myVariants.put(referencedName, lookup_item);
      }
    }
    else if (element instanceof NameDefiner) {
      final NameDefiner definer = (NameDefiner)element;
      for (PyElement expr : definer.iterateNames()) {
        if (expr != null) { // NOTE: maybe rather have SingleIterables skip nulls outright?
          String referencedName = expr.getName();
          if (referencedName != null && !myVariants.containsKey(referencedName)) {
            final LookupItem lookup_item = (LookupItem)LookupElementFactory.getInstance().createLookupElement(referencedName);
            setupItem(lookup_item);
            if (definer instanceof PyImportElement) { // set notice to imported module name if needed
              PsiElement maybe_from_import = definer.getParent();
              if (maybe_from_import instanceof PyFromImportStatement) {
                final PyFromImportStatement from_import = (PyFromImportStatement)maybe_from_import;
                PyReferenceExpression src = from_import.getImportSource();
                if (src != null) {
                  setItemNotice(lookup_item, " | " + src.getName());
                }
              }
            }
            myVariants.put(referencedName, lookup_item);
          }
        }
      }
    }

    return true;
  }

  @Nullable
  public <T> T getHint(Class<T> hintClass) {
    return null;
  }

  public void handleEvent(Event event, Object associated) {
  }

}