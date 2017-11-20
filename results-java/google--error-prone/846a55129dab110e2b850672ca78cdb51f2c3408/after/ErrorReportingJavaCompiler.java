// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.errorprone;

import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Context.Factory;

import java.util.Queue;

/**
 *
 * @author alexeagle@google.com (Alex Eagle)
 */
public class ErrorReportingJavaCompiler extends JavaCompiler {

  public ErrorReportingJavaCompiler(Context context) {
    super(context);
  }

  /**
   * Adds an initialization hook to the Context, such that each subsequent
   * request for a JavaCompiler (i.e., a lookup for 'compilerKey' of our
   * superclass, JavaCompiler) will actually construct and return our version.
   * It's necessary since many new JavaCompilers may
   * be requested for later stages of the compilation (annotation processing),
   * within the same Context. And it's the preferred way for extending behavior
   * within javac, per the documentation in {@link com.sun.tools.javac.util.Context}.
   */
  public static void preRegister(final Context context) {
    context.put(compilerKey, new Factory<JavaCompiler>() {
      @Override
      public JavaCompiler make() {
        return new ErrorReportingJavaCompiler(context);
      }
    });
  }

  @Override
  protected void flow(Env<AttrContext> attrContextEnv, Queue<Env<AttrContext>> envs) {
    super.flow(attrContextEnv, envs);
    postFlow(attrContextEnv);
  }

  /**
   * Run Error Prone analysis after performing dataflow checks.
   */
  public void postFlow(Env<AttrContext> env) {
    JavacErrorRefactorListener logReporter = new JavacErrorRefactorListener(log,
        env.toplevel.endPositions,
        env.enclClass.sym.sourcefile != null
            ? env.enclClass.sym.sourcefile
            : env.toplevel.sourcefile);
    VisitorState visitorState = new VisitorState(context, logReporter);
    Scanner scanner = (Scanner) context.get(TreePathScanner.class);
    scanner.scan(env.toplevel, visitorState);
  }
}