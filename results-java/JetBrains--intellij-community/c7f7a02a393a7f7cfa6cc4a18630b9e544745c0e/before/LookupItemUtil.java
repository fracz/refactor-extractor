package com.intellij.codeInsight.lookup;

import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.template.Template;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.meta.PsiMetaDataBase;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: ik
 * Date: 06.02.2003
 * Time: 16:05:20
 * To change this template use Options | File Templates.
 */
public class LookupItemUtil{
  private static final Logger LOG = Logger.getInstance("#com.intellij.codeInsight.lookup.LookupItemUtil");

  private LookupItemUtil() {
  }

  @Nullable
  public static LookupItem addLookupItem(Set<LookupItem> set, @NotNull Object object, String prefix) {
    return addLookupItem(set, object, prefix, -1);
  }

  @Nullable
  public static LookupItem addLookupItem(Set<LookupItem> set, @NotNull Object object, String prefix, InsertHandler handler) {
    LookupItem item = addLookupItem(set, object, prefix, -1);
    if (item != null) {
      item.setAttribute(LookupItem.INSERT_HANDLER_ATTR, handler);
    }
    return item;
  }

  @Nullable
  private static LookupItem addLookupItem(Set<LookupItem> set, @NotNull Object object, String prefix, int tailType) {
    if (object instanceof PsiType) {
      PsiType psiType = (PsiType)object;
      for (final LookupItem lookupItem : set) {
        Object o = lookupItem.getObject();
        if (o.equals(psiType)) {
          return lookupItem;
        }
      }
    }

    for (LookupItem lookupItem : set) {
      if(lookupItem.getObject().equals(lookupItem)) return null;
    }
    LookupItem item = objectToLookupItem(object);
    String text = item.getLookupString();
    if (CompletionUtil.startsWith(text, prefix)) {
      item.setLookupString(text);
      if (tailType >= 0) {
        item.setAttribute(CompletionUtil.TAIL_TYPE_ATTR, tailType);
      }
      return set.add(item) ? item : null;
    }
    return null;
  }

  public static void addLookupItems(Set<LookupItem> set, Object[] objects, String prefix) {
    for (Object object : objects) {
      LOG.assertTrue(object != null, "Lookup item can't be null!");
      addLookupItem(set, object, prefix);
    }
  }

  public static void removeLookupItem(Set<LookupItem> set, Object object) {
    Iterator iter = set.iterator();
    while (iter.hasNext()) {
      LookupItem item = (LookupItem)iter.next();
      if (object.equals(item.getObject())) {
        iter.remove();
        break;
      }
    }
  }

  public static boolean containsItem(Set<LookupItem> set, Object object) {
    for (final Object aSet : set) {
      final LookupItem item = (LookupItem)aSet;
      if (object != null && object.equals(item.getObject())) {
        return true;
      }
    }
    return false;
  }

  public static LookupItem objectToLookupItem(Object object) {
    if (object instanceof LookupItem) return (LookupItem)object;

    String s = null;
    LookupItem item = new LookupItem(object, "");
    if (object instanceof PsiElement){
      PsiElement element = (PsiElement) object;
      if(element.getUserData(PsiUtil.ORIGINAL_KEY) != null){
        element = element.getUserData(PsiUtil.ORIGINAL_KEY);
        object = element;
        item = new LookupItem(object, "");
      }
      s = PsiUtil.getName(element);
    }
    int tailType = TailType.NONE;
    if (object instanceof PsiMethod) {
      PsiMethod method = (PsiMethod)object;
      s = method.getName();
      PsiType type = method.getReturnType();
      if (type == PsiType.VOID) {
        tailType = TailType.SEMICOLON;
      }
    }
    else if (object instanceof PsiPackage) {
      tailType = TailType.DOT;
    }
    else if (object instanceof PsiKeyword) {
      s = ((PsiKeyword)object).getText();
    }
    else if (object instanceof PsiExpression) {
      s = ((PsiExpression)object).getText();
    }
    else if (object instanceof PsiType) {
      PsiType type = (PsiType)object;
      final PsiType original = type;
      int dim = 0;
      while (type instanceof PsiArrayType) {
        type = ((PsiArrayType)type).getComponentType();
        dim++;
      }

      if (type instanceof PsiClassType) {
        PsiClassType.ClassResolveResult classResolveResult = ((PsiClassType)type).resolveGenerics();
        final PsiClass psiClass = classResolveResult.getElement();
        final PsiSubstitutor substitutor = classResolveResult.getSubstitutor();
        final String text = type.getCanonicalText();
        String typeString = text;
        if (text.indexOf('<') > 0) {
          typeString = text.substring(0, text.indexOf('<'));
        }
        s = text.substring(typeString.lastIndexOf('.') + 1);

        item = psiClass != null ? new LookupItem(psiClass, s) : new LookupItem(text, s);
        item.setAttribute(LookupItem.SUBSTITUTOR, substitutor);
      }
      else {
        s = type.getPresentableText();
      }

      if (dim > 0) {
        final StringBuffer tail = new StringBuffer();
        for (int i = 0; i < dim; i++) {
          tail.append("[]");
        }
        item.setAttribute(LookupItem.TAIL_TEXT_ATTR, " " + tail.toString());
        item.setAttribute(LookupItem.TAIL_TEXT_SMALL_ATTR, "");
        item.setAttribute(LookupItem.BRACKETS_COUNT_ATTR, dim);
      }
      item.setAttribute(LookupItem.TYPE, original);
    }
    else if (object instanceof PsiMetaDataBase) {
      s = ((PsiMetaDataBase)object).getName();
    }
    else if (object instanceof String) {
      s = (String)object;
    }
    else if (object instanceof Template) {
      s = "";
    }
    else if (object instanceof PresentableLookupValue) {
      s = ((PresentableLookupValue)object).getPresentation();
    }

    if (s == null) {
      LOG.assertTrue(false, "Null string for object: " + object + " of class " + (object != null ?object.getClass():null));
    }
    if (object instanceof LookupValueWithTail) {
      item.setAttribute(LookupItem.TAIL_TEXT_ATTR, " " + ((LookupValueWithTail)object).getTailText());
    }
    item.setLookupString(s);
    item.setAttribute(CompletionUtil.TAIL_TYPE_ATTR, tailType);
    return item;
  }

  public static int doSelectMostPreferableItem(final LookupItemPreferencePolicy itemPreferencePolicy,
                                                 final String prefix,
                                                 Object[] items) {
    if (itemPreferencePolicy == null){
      return -1;
    }
    else{
      itemPreferencePolicy.setPrefix(prefix);
      LookupItem prefItem = null;
      int prefItemIndex = -1;

      for(int i = 0; i < items.length; i++){
        LookupItem item = (LookupItem)items[i];
        final Object obj = item.getObject();
        if (obj instanceof PsiElement && !((PsiElement)obj).isValid()) continue;
        if (prefItem == null){
          prefItem = item;
          prefItemIndex = i;
        }
        else{
          int d = itemPreferencePolicy.compare(item, prefItem);
          if (d < 0){
            prefItem = item;
            prefItemIndex = i;
          }
        }
      }
      return prefItem != null ? prefItemIndex : -1;
    }
  }
}