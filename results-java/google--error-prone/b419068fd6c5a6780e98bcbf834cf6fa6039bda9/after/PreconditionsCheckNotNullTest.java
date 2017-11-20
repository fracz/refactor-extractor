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

package com.google.errorprone.bugpatterns.preconditions_checknotnull;

import com.google.errorprone.CompilationHelper;
import com.google.errorprone.DiagnosticTestHelper;
import com.google.errorprone.ErrorProneCompiler;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * @author eaftan@google.com (Eddie Aftandilian)
 */
public class PreconditionsCheckNotNullTest {

  private CompilationHelper compilationHelper;

  @Before
  public void setUp() {
    DiagnosticTestHelper diagnosticHelper = new DiagnosticTestHelper();
    ErrorProneCompiler compiler = new ErrorProneCompiler.Builder()
        .report(new PreconditionsCheckNotNull.Scanner())
        .listenToDiagnostics(diagnosticHelper.collector)
        .build();
    compilationHelper = new CompilationHelper(diagnosticHelper, compiler);
  }

  @Test
  public void testPositiveCase1() throws Exception {
    compilationHelper.assertCompileFails(
        new File(this.getClass().getResource("PositiveCase1.java").toURI()),
        "Did you mean '",
        "Did you mean 'Preconditions.checkNotNull(thing, \"thing is null\")",
        // TODO(eaftan): This last case has a bug where the replacement is incorrect.  Uncomment
        // to see what it should do.
        //"Did you mean 'Preconditions.checkNotNull(thing, \"a string literal that\'s got two parts\"");
        "Did you mean 'Preconditions.checkNotNull(");
  }

  @Test
  public void testPositiveCase2() throws Exception {
    compilationHelper.assertCompileFails(
        new File(this.getClass().getResource("PositiveCase2.java").toURI()),
        "Did you mean '");
  }

  @Test
  public void testNegativeCase1() throws Exception {
    compilationHelper.assertCompileSucceeds(
        new File(this.getClass().getResource("NegativeCase1.java").toURI()));
  }

  @Test
  public void testNegativeCase2() throws Exception {
    compilationHelper.assertCompileSucceeds(
        new File(this.getClass().getResource("NegativeCase2.java").toURI()));
  }

}