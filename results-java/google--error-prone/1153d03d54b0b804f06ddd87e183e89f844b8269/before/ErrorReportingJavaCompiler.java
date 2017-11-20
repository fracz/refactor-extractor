/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Context.Factory;
import com.sun.tools.javac.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 *
 * @author alexeagle@google.com (Alex Eagle)
 */
public class ErrorReportingJavaCompiler extends JavaCompiler {

  /**
   * A map of compilation units to the number of classes in that file error-prone has encountered.
   */
  private Map<CompilationUnitTree, Integer> classDefsEncountered =
      new HashMap<CompilationUnitTree, Integer>();

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
      //@Override for OpenJDK 7 only
      public JavaCompiler make(Context context) {
        return new ErrorReportingJavaCompiler(context);
      }
      //@Override for OpenJDK 6 only
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

  public void profilePostFlow(Env<AttrContext> attrContextEnv) {
    try {
      // For profiling with YourKit, add to classpath:
      // <Profiler Installation Directory>/lib/yjp-controller-api-redist.jar
//      Controller controller = new Controller();
//      controller.startCPUProfiling(ProfilingModes.CPU_SAMPLING, "");
      postFlow(attrContextEnv);
//      controller.stopCPUProfiling();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
  * Run Error Prone analysis after performing dataflow checks.
  */
  public void postFlow(Env<AttrContext> env) {
    runErrorPronePhase(env, log, context, classDefsEncountered);
  }

  /**
   * Run the error-prone compiler phase using the Scanner class from the context object,
   * indexed under the com.google.errorprone.Scanner class.
   *
   * @param env The environment that is ready to scan
   * @param log The compiler log to write to
   * @param context The compiler Context object
   * @param classDefsEncountered A map of compilation units to number of enclosed classes
   * encountered
   */
  public static void runErrorPronePhase(Env<AttrContext> env, Log log, Context context,
      Map<CompilationUnitTree, Integer> classDefsEncountered) {
    DescriptionListener logReporter = new JavacErrorDescriptionListener(log,
        env.toplevel.endPositions,
        env.enclClass.sym.sourcefile != null
            ? env.enclClass.sym.sourcefile
            : env.toplevel.sourcefile);
    VisitorState visitorState = new VisitorState(context, logReporter);
    Scanner scanner = context.get(Scanner.class);
    if (scanner == null) {
      throw new IllegalStateException(
          "No TreePathScanner registered in context. Is annotation processing enabled? " +
          "Please report bug to error-prone: " +
          "http://code.google.com/p/error-prone/issues/entry");
    }

    /* Each env corresponds to a top-level class but not necessarily a single file. We want to scan
     * a file all at once so that we see the file-level nodes like imports and package declarations.
     *
     * For the common case where a file contains only one class, we immediately scan the file.
     * For files that contain more than one class, we keep track of those files and the number of
     * enclosed class definitions we've seen. When we've seen all class definitions for that file,
     * we scan the whole file.
     */
    if (env.toplevel.getTypeDecls().size() == 1) {
      scanner.scan(env.toplevel, visitorState);
    } else {
      Integer seenCount = classDefsEncountered.get(env.toplevel);
      if (seenCount == null) {
        seenCount = 1;
      } else {
        seenCount++;
      }

      if (seenCount == env.toplevel.getTypeDecls().size()) {
        scanner.scan(env.toplevel, visitorState);
        // Remove compilation unit from map so it can be garbage collected.
        classDefsEncountered.remove(env.toplevel);
      } else {
        classDefsEncountered.put(env.toplevel, seenCount);
      }
    }
  }

}