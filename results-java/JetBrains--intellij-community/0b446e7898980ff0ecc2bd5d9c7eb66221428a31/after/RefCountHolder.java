
package com.intellij.codeInsight.daemon.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.statistics.StatisticsManager;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.containers.BidirectionalMap;

import java.util.*;

public class RefCountHolder {
  private static final Logger LOG = Logger.getInstance("#com.intellij.codeInsight.daemon.impl.RefCountHolder");

  private final PsiFile myFile;

  private BidirectionalMap<PsiReference,PsiElement> myLocalRefsMap = new BidirectionalMap<PsiReference, PsiElement>();

  private HashMap<PsiNamedElement,Boolean> myDclsUsedMap = new HashMap<PsiNamedElement,Boolean>();
  private HashMap<String,XmlTag> myXmlId2TagMap = new HashMap<String,XmlTag>();
  private Map<PsiReference, PsiImportStatementBase> myImportStatements = new HashMap<PsiReference, PsiImportStatementBase>();
  private Set<PsiNamedElement> myUsedElements = new HashSet<PsiNamedElement>();

  public RefCountHolder(PsiFile file) {
    myFile = file;
  }

  public void clear() {
    myLocalRefsMap.clear();
    myImportStatements.clear();
    myDclsUsedMap.clear();
    myXmlId2TagMap.clear();
    myUsedElements.clear();
  }

  public void registerLocallyReferenced(PsiNamedElement result) {
    myDclsUsedMap.put(result,Boolean.TRUE);
  }

  public void registerLocalDcl(PsiNamedElement dcl) {
    myDclsUsedMap.put(dcl,Boolean.FALSE);
    addStatistics(dcl);
  }

  private static void addStatistics(final PsiNamedElement dcl) {
    final PsiType typeByPsiElement = PsiUtil.getTypeByPsiElement(dcl);
    final StatisticsManager.NameContext context = StatisticsManager.getContext(dcl);
    if(typeByPsiElement != null && context != null) {
      StatisticsManager.getInstance().incNameUseCount(typeByPsiElement, context, dcl.getName());
    }
  }

  public void registerTagWithId(String id, XmlTag tag) {
    myXmlId2TagMap.put(id,tag);
  }

  public XmlTag getTagById(String id) {
    return myXmlId2TagMap.get(id);
  }

  public void registerReference(PsiReference ref, JavaResolveResult resolveResult) {
    PsiElement refElement = resolveResult.getElement();
    if (refElement != null && getFile().equals(refElement.getContainingFile())) {
      registerLocalRef(ref, refElement);
    }

    PsiElement resolveScope = resolveResult.getCurrentFileResolveScope();
    if (resolveScope instanceof PsiImportStatementBase) {
      registerImportStatement(ref, (PsiImportStatementBase)resolveScope);
    }
  }

  private void registerImportStatement (PsiReference ref, PsiImportStatementBase importStatement) {
    myImportStatements.put(ref, importStatement);
  }

  public boolean isRedundant(PsiImportStatementBase importStatement) {
    return !myImportStatements.values().contains(importStatement);
  }

  private void registerLocalRef(PsiReference ref, PsiElement refElement) {
    if (refElement instanceof PsiMethod && PsiTreeUtil.isAncestor(refElement, ref.getElement(), true)) return; // filter self-recursive calls
    if (refElement instanceof PsiClass && PsiTreeUtil.isAncestor(refElement, ref.getElement(), true)) return; // filter inner use of itself
    myLocalRefsMap.put(ref, refElement);
    if(refElement instanceof PsiNamedElement) {
      PsiNamedElement namedElement = (PsiNamedElement)refElement;
      if(!myUsedElements.contains(namedElement)) {
        myUsedElements.add(namedElement);
        addStatistics(namedElement);
      }
    }
  }

  public void removeInvalidRefs() {
    for(Iterator<PsiReference> iterator = myLocalRefsMap.keySet().iterator(); iterator.hasNext();){
      PsiReference ref = iterator.next();
      if (!ref.getElement().isValid()){
        PsiElement value = myLocalRefsMap.get(ref);
        iterator.remove();
        List<PsiReference> array = myLocalRefsMap.getKeysByValue(value);
        array.remove(ref);
      }
    }
    for (Iterator<PsiReference> iterator = myImportStatements.keySet().iterator(); iterator.hasNext();) {
      PsiReference ref = iterator.next();
      if (!ref.getElement().isValid()) iterator.remove();
    }
  }

  public int getRefCount(PsiElement element) {
    List<PsiReference> array = myLocalRefsMap.getKeysByValue(element);
    if(array != null) return array.size();

    Boolean usedStatus = myDclsUsedMap.get(element);
    if (usedStatus == Boolean.TRUE) return 1;

    return 0;
  }

  public int getReadRefCount(PsiElement element) {
    LOG.assertTrue(element instanceof PsiVariable);
    List<PsiReference> array = myLocalRefsMap.getKeysByValue(element);
    if (array == null) return 0;
    int count = 0;
    for (PsiReference ref : array) {
      PsiElement refElement = ref.getElement();
      if (!(refElement instanceof PsiExpression)) { // possible with uncomplete code
        count++;
        continue;
      }
      if (PsiUtil.isAccessedForReading((PsiExpression)refElement)) {
        if (refElement.getParent() instanceof PsiExpression &&
            refElement.getParent().getParent() instanceof PsiExpressionStatement &&
            PsiUtil.isAccessedForWriting((PsiExpression)refElement)) {
          continue; // "var++;"
        }
        count++;
      }
    }
    return count;
  }

  public int getWriteRefCount(PsiElement element) {
    LOG.assertTrue(element instanceof PsiVariable);
    List<PsiReference> array = myLocalRefsMap.getKeysByValue(element);
    if (array == null) return 0;
    int count = 0;
    for (PsiReference ref : array) {
      final PsiElement refElement = ref.getElement();
      if (!(refElement instanceof PsiExpression)) { // possible with uncomplete code
        count++;
        continue;
      }
      if (PsiUtil.isAccessedForWriting((PsiExpression)refElement)) {
        count++;
      }
    }
    return count;
  }

  public PsiFile getFile() {
    return myFile;
  }

  public PsiNamedElement[] getUnusedDcls() {
    List<PsiNamedElement> result = new LinkedList<PsiNamedElement>();
    Set<Map.Entry<PsiNamedElement, Boolean>> entries = myDclsUsedMap.entrySet();

    for (final Map.Entry<PsiNamedElement, Boolean> entry : entries) {
      if (entry.getValue() == Boolean.FALSE) result.add(entry.getKey());
    }

    return result.toArray(new PsiNamedElement[result.size()]);
  }
}