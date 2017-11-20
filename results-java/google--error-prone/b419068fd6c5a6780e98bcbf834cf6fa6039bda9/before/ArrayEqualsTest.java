/*
 * Copyright 2012 Google Inc. All Rights Reserved.
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

package com.google.errorprone.bugpatterns.array_equals;

import com.google.errorprone.DiagnosticTestHelper;
import com.google.errorprone.ErrorProneCompiler;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;

import static com.google.errorprone.DiagnosticTestHelper.diagnosticMessage;
import static com.google.errorprone.DiagnosticTestHelper.hasDiagnosticOnAllMatchingLines;
import static java.util.regex.Pattern.compile;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;

/**
 * @author eaftan@google.com (Eddie Aftandilian)
 */
public class ArrayEqualsTest {

  private ErrorProneCompiler compiler;
  private DiagnosticTestHelper diagnosticHelper;

  @Before
  public void setUp() {
    diagnosticHelper = new DiagnosticTestHelper();
    compiler = new ErrorProneCompiler.Builder()
        .report(new ArrayEquals.Scanner())
        .listenToDiagnostics(diagnosticHelper.collector)
        .build();
  }

  @Test
  public void testPositiveCase() throws Exception {
    File source = new File(this.getClass().getResource("PositiveCases.java").toURI());
    assertThat(compiler.compile(new String[]{"-Xjcov", source.getAbsolutePath()}), is(1));
    assertThat(diagnosticHelper.getDiagnostics(),
        hasDiagnosticOnAllMatchingLines(source, compile(".*//BUG\\s*$")));
    Matcher<Iterable<? super Diagnostic<JavaFileObject>>> matcher =
        hasItem(diagnosticMessage(containsString("Did you mean 'if (Arrays.equals(a, b)) {")));
    assertThat(diagnosticHelper.getDiagnostics(), matcher);
  }

  @Test
  public void testNegativeCase() throws Exception {
    File source = new File(this.getClass().getResource("NegativeCases.java").toURI());
    assertThat(compiler.compile(new String[]{source.getAbsolutePath()}), is(0));
  }

}