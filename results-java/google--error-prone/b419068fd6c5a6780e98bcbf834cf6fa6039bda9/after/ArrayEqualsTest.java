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

import com.google.errorprone.CompilationHelper;
import com.google.errorprone.DiagnosticTestHelper;
import com.google.errorprone.ErrorProneCompiler;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * @author eaftan@google.com (Eddie Aftandilian)
 */
public class ArrayEqualsTest {

  private CompilationHelper compilationHelper;

  @Before
  public void setUp() {
    DiagnosticTestHelper diagnosticHelper = new DiagnosticTestHelper();
    ErrorProneCompiler compiler = new ErrorProneCompiler.Builder()
        .report(new ArrayEquals.Scanner())
        .listenToDiagnostics(diagnosticHelper.collector)
        .build();
    compilationHelper = new CompilationHelper(diagnosticHelper, compiler);
  }

  @Test
  public void testPositiveCase() throws Exception {
    compilationHelper.assertCompileFails(
        new File(this.getClass().getResource("PositiveCases.java").toURI()),
        "Did you mean 'if (Arrays.equals(a, b)) {",
        "Did you mean 'if (Arrays.equals(a, b)) {",
        "Did you mean 'if (Arrays.equals(s.toCharArray(), b)) {",
        "Did you mean 'if (Arrays.equals(a, s.toCharArray())) {",
        "Did you mean 'if (Arrays.equals(s1.toCharArray(), s2.toCharArray())) {");
  }

  @Test
  public void testNegativeCase() throws Exception {
    compilationHelper.assertCompileSucceeds(
        new File(this.getClass().getResource("NegativeCases.java").toURI()));
  }

}