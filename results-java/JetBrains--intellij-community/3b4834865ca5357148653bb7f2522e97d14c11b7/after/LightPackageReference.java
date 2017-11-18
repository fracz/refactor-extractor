package com.intellij.psi.impl.light;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.infos.CandidateInfo;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.IncorrectOperationException;

public class LightPackageReference extends LightElement implements PsiJavaCodeReferenceElement {
  private final String myPackageName;
  private final PsiPackage myRefPackage;

  public LightPackageReference(PsiManager manager, PsiPackage refPackage) {
    super(manager);
    myPackageName = null;
    myRefPackage = refPackage;
  }

  public LightPackageReference(PsiManager manager, String packageName) {
    super(manager);
    myPackageName = packageName;
    myRefPackage = null;
  }

  public PsiElement resolve(){
    if (myPackageName != null){
      return myManager.findPackage(myPackageName);
    }
    else {
      return myRefPackage;
    }
  }

  public JavaResolveResult advancedResolve(boolean incompleteCode){
    return new CandidateInfo(resolve(), PsiSubstitutor.EMPTY);
  }

  public JavaResolveResult[] multiResolve(boolean incompleteCode){
    final JavaResolveResult result = advancedResolve(incompleteCode);
    if(result != JavaResolveResult.EMPTY) return new JavaResolveResult[]{result};
    return JavaResolveResult.EMPTY_ARRAY;
  }

  public String getText(){
    if (myPackageName != null){
      return myPackageName;
    }
    else {
      return myRefPackage.getQualifiedName();
    }
  }

  public PsiReference getReference() {
    return this;
  }

  public String getCanonicalText(){
    return getText();
  }

  public PsiElement copy(){
    if (myPackageName != null){
      return new LightPackageReference(myManager, myPackageName);
    }
    else{
      return new LightPackageReference(myManager, myRefPackage);
    }
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    //TODO?
    throw new UnsupportedOperationException();
  }

  public PsiElement bindToElement(PsiElement element) throws IncorrectOperationException {
    //TODO?
    throw new UnsupportedOperationException();
  }

  public void accept(PsiElementVisitor visitor){
    visitor.visitReferenceElement(this);
  }

  public String toString(){
    return "PsiJavaCodeReferenceElement:" + getText();
  }

  public boolean isReferenceTo(PsiElement element) {
    if (!(element instanceof PsiPackage)) return false;
    return element.getManager().areElementsEquivalent(element, resolve());
  }

  public Object[] getVariants() {
    throw new RuntimeException("Variants are not available for light references");
  }

  public boolean isSoft(){
    return false;
  }

  public void processVariants(PsiScopeProcessor processor){
    throw new RuntimeException("Variants are not available for light references");
  }

  public PsiElement getReferenceNameElement() {
    return null;
  }

  public PsiReferenceParameterList getParameterList() {
    return null;
  }

  public String getQualifiedName() {
    return getText();
  }

  public String getReferenceName() {
    if (myPackageName != null){
      return PsiNameHelper.getShortClassName(myPackageName);
    }
    else {
      return myRefPackage.getName();
    }
  }

  public TextRange getRangeInElement() {
    return new TextRange(0, getTextLength());
  }

  public PsiElement getElement() {
    return this;
  }

  public boolean isValid() {
    return myRefPackage == null || myRefPackage.isValid();
  }

  public PsiType[] getTypeParameters() {
    return PsiType.EMPTY_ARRAY;
  }

  public PsiElement getQualifier() {
    return null;
  }

  public boolean isQualified() {
    return false;
  }
}