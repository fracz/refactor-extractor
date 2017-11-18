package com.intellij.debugger.impl;

import com.intellij.debugger.actions.DebuggerAction;
import com.intellij.debugger.engine.StackFrameContext;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.evaluation.*;
import com.intellij.debugger.engine.evaluation.expression.EvaluatorBuilder;
import com.intellij.debugger.engine.evaluation.expression.EvaluatorBuilderImpl;
import com.intellij.debugger.ui.*;
import com.intellij.debugger.ui.impl.watch.DebuggerTreeNodeExpression;
import com.intellij.debugger.ui.tree.DebuggerTreeNode;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.psi.*;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.execution.ExecutionException;
import com.intellij.util.ArrayUtil;
import com.sun.jdi.Value;
import com.sun.tools.jdi.TransportService;
import org.jdom.Element;

import java.net.ServerSocket;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;

/*
 * Copyright (c) 2000-2004 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */

public class DebuggerUtilsImpl extends DebuggerUtilsEx{
  private static final Logger LOG = Logger.getInstance("#com.intellij.debugger.impl.DebuggerUtilsImpl");

  public String getComponentName() {
    return "DebuggerUtils";
  }

  public void initComponent() { }

  public void disposeComponent() {
  }

  public PsiExpression substituteThis(PsiExpression expressionWithThis, PsiExpression howToEvaluateThis, Value howToEvaluateThisValue, StackFrameContext context)
    throws EvaluateException {
    return DebuggerTreeNodeExpression.substituteThis(expressionWithThis, howToEvaluateThis, howToEvaluateThisValue);
  }

  public EvaluatorBuilder getEvaluatorBuilder() {
    return EvaluatorBuilderImpl.getInstance();
  }

  public DebuggerTreeNode getSelectedNode(DataContext context) {
    return DebuggerAction.getSelectedNode(context);
  }

  public DebuggerContextImpl getDebuggerContext(DataContext context) {
    return DebuggerAction.getDebuggerContext(context);
  }

  public Element writeTextWithImports(TextWithImports text) {
    Element element = new Element("TextWithImports");

    element.setAttribute("text", text.toString());
    element.setAttribute("type", text.isExpressionText() ? "expression" : "code fragment");
    return element;
  }

  public TextWithImports readTextWithImports(Element element) {
    LOG.assertTrue("TextWithImports".equals(element.getName()));

    String text = element.getAttributeValue("text");
    if ("expression".equals(element.getAttributeValue("type"))) {
      return EvaluationManager.getInstance().createExpressionFragment(text);
    } else {
      return EvaluationManager.getInstance().createCodeBlockFragment(text);
    }
  }

  public void writeTextWithImports(Element root, String name, TextWithImports value) {
    LOG.assertTrue(value.isExpressionText());
    JDOMExternalizerUtil.writeField(root, name, value.toString());
  }

  public TextWithImports readTextWithImports(Element root, String name) {
    String s = JDOMExternalizerUtil.readField(root, name);
    if(s == null) return null;
    return EvaluationManager.getInstance().createExpressionFragment(s);
  }

  public TextWithImports createExpressionText(PsiExpression expression) {
    LOG.assertTrue(expression.isValid());
    return EvaluationManager.getInstance().createExpressionFragment(expression);
  }

  public TextWithImports createExpressionWithImports(String expression) {
    return EvaluationManager.getInstance().createExpressionFragment(expression);
  }

  public PsiElement getContextElement(StackFrameContext context) {
    return PositionUtil.getContextElement(context);
  }

  public PsiClass chooseClassDialog(String title, Project project) {
    TreeClassChooser dialog = TreeClassChooserFactory.getInstance(project).createProjectScopeChooser(title);
    dialog.showDialog();
    return dialog.getSelectedClass();
  }

  public CompletionEditor createEditor(Project project, PsiElement context, String recentsId) {
    return new DebuggerExpressionComboBox(project, context, recentsId);
  }

  private static final int findAvailableSocketPort() throws ExecutionException {
    int port;
    try {
      final ServerSocket serverSocket = new ServerSocket(0);
      port = serverSocket.getLocalPort();
      //workaround for linux : calling close() immediately after opening socket
      //may result that socket is not closed
      synchronized(serverSocket) {
        try {
          serverSocket.wait(1);
        }
        catch (InterruptedException e) {
          LOG.error(e);
        }
      }
      serverSocket.close();
    }
    catch (IOException e) {
      throw new ExecutionException(DebugProcessImpl.processError(e));
    }
    return port;
  }

  public String findAvailableDebugAddress(final boolean useSockets) throws ExecutionException {
    final TransportService transportService = getTransportService(useSockets);

    if(useSockets) {
      final int freePort = findAvailableSocketPort();
      return Integer.toString(freePort);
    }

    try {
      String address  = transportService.startListening();
      transportService.stopListening(address);
      return address;
    }
    catch (IOException e) {
      throw new ExecutionException(DebugProcessImpl.processError(e));
    }
  }
}