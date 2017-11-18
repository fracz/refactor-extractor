package com.intellij.codeInsight.completion;

import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.scope.CompletionElement;
import com.intellij.codeInsight.completion.scope.CompletionProcessor;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.codeInsight.lookup.LookupItemUtil;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.*;
import com.intellij.psi.filters.ContextGetter;
import com.intellij.psi.filters.ElementExtractorFilter;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.impl.source.jsp.jspJava.JspClassLevelDeclarationStatement;
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference;
import com.intellij.psi.infos.CandidateInfo;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ReflectionCache;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ik
 * Date: 31.01.2003
 * Time: 17:38:14
 * To change this template use Options | File Templates.
 */

public class CompletionVariant {
  protected static short DEFAULT_TAIL_TYPE = TailType.SPACE;

  private final Set<Scope> myScopeClasses = new HashSet<Scope>();
  private ElementFilter myPosition;
  private final List<CompletionVariantItem> myCompletionsList = new ArrayList<CompletionVariantItem>();
  private final Set<Class> myScopeClassExceptions = new HashSet<Class>();
  private InsertHandler myInsertHandler = null;
  private final Map<Object, Serializable> myItemProperties = new HashMap<Object, Serializable>();
  private boolean caseInsensitive;

  public CompletionVariant(){}

  public CompletionVariant(Class scopeClass, ElementFilter position){
    includeScopeClass(scopeClass);
    if (scopeClass == PsiClass.class) {
      includeScopeClass(JspClassLevelDeclarationStatement.class); // hack for JSP completion
    }
    myPosition = position;
  }

  public CompletionVariant(ElementFilter position){
    myPosition = position;
  }

  public boolean isScopeAcceptable(PsiElement scope){
    return isScopeClassAcceptable(scope.getClass());
  }

  public boolean isScopeFinal(PsiElement scope){
    return isScopeClassFinal(scope.getClass());
  }

  public InsertHandler getInsertHandler(){
    return myInsertHandler;
  }

  public void setInsertHandler(InsertHandler handler){
    myInsertHandler = handler;
  }

  public void setItemProperty(Object id, Serializable value){
    myItemProperties.put(id, value);
  }

  public boolean isScopeClassFinal(Class scopeClass){
    for (final Object myScopeClass : myScopeClasses) {
      Scope scope = (Scope)myScopeClass;
      if (ReflectionCache.isAssignable(scope.myClass, scopeClass) && scope.myIsFinalScope) {
        return true;
      }
    }
    return false;
  }

  public boolean isScopeClassAcceptable(Class scopeClass){
    boolean ret = false;

    for (final Object myScopeClass : myScopeClasses) {
      final Class aClass = ((Scope)myScopeClass).myClass;
      if (ReflectionCache.isAssignable(aClass, scopeClass)) {
        ret = true;
        break;
      }
    }

    if(ret){
      for (final Object myScopeClassException : myScopeClassExceptions) {
        final Class aClass = (Class)myScopeClassException;
        if (ReflectionCache.isAssignable(aClass, scopeClass)) {
          ret = false;
          break;
        }
      }
    }
    return ret;
  }

  public void excludeScopeClass(Class<?> aClass){
    myScopeClassExceptions.add(aClass);
  }

  public void includeScopeClass(Class<?> aClass){
    myScopeClasses.add(new Scope(aClass, false));
  }

  public void includeScopeClass(Class<?> aClass, boolean isFinalScope){
    myScopeClasses.add(new Scope(aClass, isFinalScope));
  }

  public void addCompletionFilterOnElement(ElementFilter filter){
    addCompletionFilterOnElement(filter, TailType.NONE);
  }

  public void addCompletionFilterOnElement(ElementFilter filter, int tailType){
    addCompletion(new ElementExtractorFilter(filter), tailType);
  }

  public void addCompletionFilter(ElementFilter filter, int tailType){
    addCompletion(filter, tailType);
  }

  public void addCompletionFilter(ElementFilter filter){
    addCompletionFilter(filter, TailType.NONE);
  }

  public void addCompletion(@NonNls String keyword){
    addCompletion(keyword, DEFAULT_TAIL_TYPE);
  }

  public void addCompletion(@NonNls String keyword, int tailType){
    addCompletion((Object)keyword, tailType);
  }

  public void addCompletion(KeywordChooser chooser){
    addCompletion(chooser, DEFAULT_TAIL_TYPE);
  }

  public void addCompletion(KeywordChooser chooser, int tailType){
    addCompletion((Object)chooser, tailType);
  }

  public void addCompletion(ContextGetter chooser){
    addCompletion(chooser, DEFAULT_TAIL_TYPE);
  }

  public void addCompletion(ContextGetter chooser, int tailType){
    addCompletion((Object)chooser, tailType);
  }

  private void addCompletion(Object completion, int tail){
    myCompletionsList.add(new CompletionVariantItem(completion, tail));
  }

  public void addCompletion(@NonNls String[] keywordList){
    addCompletion(keywordList, DEFAULT_TAIL_TYPE);
  }

  public void addCompletion(String[] keywordList, int tailType){
    for (String aKeywordList : keywordList) {
      addCompletion(aKeywordList, tailType);
    }
  }

  public boolean isVariantApplicable(PsiElement position, PsiElement scope){
    return isScopeAcceptable(scope) && myPosition.isAcceptable(position, scope);
  }

  public void addReferenceCompletions(PsiReference reference, PsiElement position, Set<LookupItem> set, CompletionContext prefix){
    for (final CompletionVariantItem ce : myCompletionsList) {
      addReferenceCompletions(reference, position, set, prefix, ce);
    }
  }

  @Nullable
  private LookupItem addLookupItem(Set<LookupItem> set, CompletionVariantItem element, @NotNull Object completion, CompletionContext context){
    LookupItem ret = LookupItemUtil.objectToLookupItem(completion);
    if(ret == null) return null;

    if(getInsertHandler() != null){
      ret.setAttribute(LookupItem.INSERT_HANDLER_ATTR, getInsertHandler());
      ret.setTailType(TailType.UNKNOWN);
    }
    else {
      ret.setTailType(element.myTailType);
    }

    for (final Object key : myItemProperties.keySet()) {
      if (key == LookupItem.FORCE_SHOW_FQN_ATTR && ret.getObject() instanceof PsiClass) {
        @NonNls String packageName = ((PsiClass)ret.getObject()).getQualifiedName();
        if (packageName != null && packageName.lastIndexOf('.') > 0) {
          packageName = packageName.substring(0, packageName.lastIndexOf('.'));
        }
        else {
          packageName = "";
        }
        if (packageName.length() == 0) {
          packageName = "default package";
        }

        ret.setAttribute(LookupItem.TAIL_TEXT_ATTR, " (" + packageName + ")");
        ret.setAttribute(LookupItem.TAIL_TEXT_SMALL_ATTR, "");
      }
      else {
        if (completion instanceof PsiMember && key == LookupItem.FORCE_QUALIFY) {
          final PsiMember completionElement = (PsiMember)completion;
          final PsiClass containingClass = completionElement.getContainingClass();
          if (containingClass != null) {
            final String className = containingClass.getName();
            ret.setLookupString(className + "." + ret.getLookupString());
            ret.setAttribute(key, myItemProperties.get(key));
          }
        }
        ret.setAttribute(key, myItemProperties.get(key));
      }
    }

    final String lookupString = ret.getLookupString();
    if(CompletionUtil.checkName(lookupString, context, caseInsensitive)){
      set.add(ret);
      return ret;
    }

    return null;
  }

  public void addKeywords(PsiElementFactory factory, Set<LookupItem> set, CompletionContext context, PsiElement position){
    for (final CompletionVariantItem ce : myCompletionsList) {
      final Object comp = ce.myCompletion;
      if (comp instanceof OffsetDependant) {
        ((OffsetDependant)comp).setOffset(context.startOffset);
      }

      if (comp instanceof String) {
        addKeyword(factory, set, ce, comp, context);
      }
      else if (comp instanceof ContextGetter) {
        final Object[] elements = ((ContextGetter)comp).get(position, context);
        for (Object element : elements) {
          addLookupItem(set, ce, element, context);
        }
      }
      // TODO: KeywordChooser -> ContextGetter
      else if (comp instanceof KeywordChooser) {
        final String[] keywords = ((KeywordChooser)comp).getKeywords(context, position);
        for (String keyword : keywords) {
          addKeyword(factory, set, ce, keyword, context);
        }
      }
    }
  }

  private void addKeyword(PsiElementFactory factory, Set<LookupItem> set, final CompletionVariantItem ce, final Object comp, CompletionContext context){
    for (final Object aSet : set) {
      final LookupItem item = (LookupItem)aSet;
      if (item.getObject().toString().equals(comp.toString())) {
        return;
      }
    }
    if(factory == null){
      addLookupItem(set, ce, comp, context);
    }
    else{
      try{
        final PsiKeyword keyword = factory.createKeyword((String)comp);
        addLookupItem(set, ce, keyword, context);
      }
      catch(IncorrectOperationException e){
        addLookupItem(set, ce, comp, context);
      }
    }
  }

  public boolean hasReferenceFilter(){
    for (final Object aMyCompletionsList : myCompletionsList) {
      if (((CompletionVariantItem)aMyCompletionsList).myCompletion instanceof ElementFilter) {
        return true;
      }
    }
    return false;
  }

  public boolean hasKeywordCompletions(){
    for (final Object aMyCompletionsList : myCompletionsList) {
      final Object completion = ((CompletionVariantItem)aMyCompletionsList).myCompletion;
      if (!(completion instanceof ElementFilter)) {
        return true;
      }
    }
    return false;
  }

  protected void addReferenceCompletions(PsiReference reference, PsiElement position, Set<LookupItem> set,
                                         CompletionContext context, CompletionVariantItem item){
    if(item.myCompletion instanceof ElementFilter){
      final CompletionProcessor processor = new CompletionProcessor(context, position, (ElementFilter)item.myCompletion);

      if (reference instanceof PsiMultiReference) {
        int javaReferenceStart = -1;

        PsiReference[] references = getReferences((PsiMultiReference)reference);

        for (PsiReference ref : references) {
          if (ref instanceof PsiJavaReference) {
            int newStart = ref.getElement().getTextRange().getStartOffset() + ref.getRangeInElement().getStartOffset();
            if (javaReferenceStart == -1) {
              javaReferenceStart = newStart;
            } else {
              if (newStart == javaReferenceStart) continue;
            }
          }
          addReferenceCompletions(ref, position, set, context, item);
        }
      }
      else if(reference instanceof PsiJavaReference){
        ((PsiJavaReference)reference).processVariants(processor);
      }
      else{
        final Object[] completions = reference.getVariants();
        if(completions == null) return;

        for (Object completion : completions) {
          if (completion instanceof PsiElement) {
            processor.execute((PsiElement)completion, PsiSubstitutor.EMPTY);
          }
          else if (completion instanceof CandidateInfo) {
            final CandidateInfo info = (CandidateInfo)completion;
            if (info.isValidResult()) {
              processor.execute(info.getElement(), PsiSubstitutor.EMPTY);
            }
          }
          else {
            addLookupItem(set, item, completion, context);
          }
        }
      }

      Collection<CompletionElement> results = processor.getResults();
      if (results != null) {
        for (CompletionElement element : results) {
          final LookupItem lookupItem = addLookupItem(set, item, element.getElement(), context);
          if (lookupItem != null) {
            lookupItem.setAttribute(LookupItem.SUBSTITUTOR, element.getSubstitutor());
            if (element.getQualifier() != null){
              CompletionUtil.setQualifierType(lookupItem, element.getQualifier());
            }
          }
        }
      }
    }
  }

  private static PsiReference[] getReferences(final PsiMultiReference multiReference) {
    final PsiReference[] references = multiReference.getReferences();
    final List<PsiReference> hard = ContainerUtil.findAll(references, new Condition<PsiReference>() {
      public boolean value(final PsiReference object) {
        return !object.isSoft();
      }
    });
    if (!hard.isEmpty()) {
      return hard.toArray(new PsiReference[hard.size()]);
    }
    return references;
  }


  private static class Scope{
    Class myClass;
    boolean myIsFinalScope;

    Scope(Class aClass, boolean isFinalScope){
      myClass = aClass;
      myIsFinalScope = isFinalScope;
    }
  }

  protected static class CompletionVariantItem{
    public Object myCompletion;
    public int myTailType;

    public CompletionVariantItem(Object completion, int tailtype){
      myCompletion = completion;
      myTailType = tailtype;
    }

    public String toString(){
      return myCompletion.toString();
    }
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public String toString(){
    return "completion variant at " + myPosition.toString() + " completions: " + myCompletionsList;
  }

  public boolean isCaseInsensitive() {
    return caseInsensitive;
  }

  public void setCaseInsensitive(boolean caseInsensitive) {
    this.caseInsensitive = caseInsensitive;
  }
}