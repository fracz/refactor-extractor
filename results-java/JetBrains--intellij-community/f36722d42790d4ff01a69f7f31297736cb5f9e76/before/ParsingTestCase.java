
package com.intellij.testFramework;

import com.intellij.openapi.application.ex.PathManagerEx;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.psi.impl.source.jsp.JspFileImpl;
import com.intellij.lang.jsp.JspxFileViewProviderImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;

public abstract class ParsingTestCase extends LightIdeaTestCase {
  private final String myDataPath;
  protected String myFileExt;
  private final String myFullDataPath;
  protected PsiFile myFile;

  public ParsingTestCase(String dataPath, String fileExt) {
    myDataPath = dataPath;
    myFullDataPath = testDataPath() + "/psi/" + myDataPath;
    myFileExt = fileExt;
  }

  protected String testDataPath() {
    return PathManagerEx.getTestDataPath();
  }

  protected void doTest(boolean checkResult) throws Exception{
    String name = getTestName(false);
    String text = loadFile(name + "." + myFileExt);
    myFile = createFile(name + "." + myFileExt, text);
    myFile.accept(new PsiRecursiveElementVisitor() {
      public void visitElement(PsiElement element) {super.visitElement(element);}
      public void visitReferenceExpression(PsiReferenceExpression expression) {}
    });
    assertEquals(text, myFile.getText());
    if (checkResult){
      checkResult(name + ".txt", myFile);
    }
    else{
      toParseTreeText(myFile);
    }
    if(myFile instanceof JspFileImpl) ((JspxFileViewProviderImpl)((JspFileImpl)myFile).getViewProvider()).checkAllTreesEqual();
  }

  protected void checkResult(String targetDataName, final PsiFile file) throws Exception {
    final PsiElement[] psiRoots = file.getPsiRoots();
    if(psiRoots.length > 1){
      for (int i = 0; i < psiRoots.length; i++) {
        final PsiElement psiRoot = psiRoots[i];
        checkResult(targetDataName + "." + i, toParseTreeText((PsiElement)psiRoot).trim());
      }
    }
    else{
      checkResult(targetDataName, toParseTreeText(file).trim());
    }
  }

  protected void checkResult(String targetDataName, final String text) throws Exception {
    try{
      String expectedText = loadFile(targetDataName);
      /*
      if (!expectedText.equals(treeText)){
        int length = Math.min(expectedText.length(), treeText.length());
        for(int i = 0; i < length; i++){
          char c1 = expectedText.charAt(i);
          char c2 = treeText.charAt(i);
          if (c1 != c2){
            System.out.println("i = " + i);
            System.out.println("c1 = " + c1);
            System.out.println("c2 = " + c2);
          }
        }
      }
      */
      assertEquals(expectedText, text);
    }
    catch(FileNotFoundException e){
      String fullName = myFullDataPath + File.separatorChar + targetDataName;
      FileWriter writer = new FileWriter(fullName);
      writer.write(text);
      writer.close();
      assertTrue("No output text found. File " + fullName + " created.", false);
    }
  }

  protected String toParseTreeText(final PsiElement file) {
    return DebugUtil.treeToString(com.intellij.psi.impl.source.SourceTreeToPsiMap.psiElementToTree(file), false);
  }

  protected String loadFile(String name) throws Exception {
    String fullName = myFullDataPath + File.separatorChar + name;
    String text = new String(FileUtil.loadFileText(new File(fullName))).trim();
    text = StringUtil.convertLineSeparators(text);
    return text;
  }
}