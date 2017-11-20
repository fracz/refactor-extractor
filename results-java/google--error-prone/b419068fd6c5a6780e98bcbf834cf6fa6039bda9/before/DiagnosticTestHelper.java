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

package com.google.errorprone;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.internal.matchers.StringContains.containsString;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

/**
 * Utility class for tests which need to assert on the diagnostics produced during compilation.
 *
 * @author alexeagle@google.com (Alex Eagle)
 */
public class DiagnosticTestHelper {

  public DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<JavaFileObject>();

  public static Matcher<Diagnostic<JavaFileObject>> suggestsRemovalOfLine(int line) {
    return allOf(diagnosticOnLine(line), diagnosticMessage(containsString("remove this line")));
  }

  public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
    return collector.getDiagnostics();
  }

  public String describe() {
    StringBuilder stringBuilder = new StringBuilder().append("Diagnostics:\n");
    for (Diagnostic<? extends JavaFileObject> diagnostic : getDiagnostics()) {
      stringBuilder.append("  [")
          .append(diagnostic.getLineNumber()).append(":")
          .append(diagnostic.getColumnNumber())
          .append("]\t");
      stringBuilder.append(diagnostic.getMessage(Locale.getDefault()).replaceAll("\n", "\\\\n"));
      stringBuilder.append("\n");
    }
    return stringBuilder.toString();
  }

  public static TypeSafeDiagnosingMatcher<Diagnostic<JavaFileObject>> diagnosticLineAndColumn(
      final long line, final long column) {
    return new TypeSafeDiagnosingMatcher<Diagnostic<JavaFileObject>>() {
      @Override
      protected boolean matchesSafely(
          Diagnostic<JavaFileObject> item, Description mismatchDescription) {
        mismatchDescription
            .appendText("line:column")
            .appendValue(item.getLineNumber())
            .appendText(":")
            .appendValue(item.getColumnNumber());
        return item.getLineNumber() == line && item.getColumnNumber() == column;
      }

      @Override
      public void describeTo(Description description) {
        description
            .appendText("a diagnostic on line:column ")
            .appendValue(line)
            .appendText(":")
            .appendValue(column);
      }
    };
  }

  public static TypeSafeDiagnosingMatcher<Diagnostic<JavaFileObject>> diagnosticOnLine(
      final long line) {
    return new TypeSafeDiagnosingMatcher<Diagnostic<JavaFileObject>>() {
      @Override
      protected boolean matchesSafely(
          Diagnostic<JavaFileObject> item, Description mismatchDescription) {
        mismatchDescription
            .appendText("line ")
            .appendValue(item.getLineNumber());
        return item.getLineNumber() == line;
      }

      @Override
      public void describeTo(Description description) {
        description
            .appendText("a diagnostic on line ")
            .appendValue(line);
      }
    };
  }

  public static TypeSafeDiagnosingMatcher<Diagnostic<JavaFileObject>> diagnosticMessage(
      final Matcher<String> matcher) {
    return new TypeSafeDiagnosingMatcher<Diagnostic<JavaFileObject>>() {
      @Override
      protected boolean matchesSafely(
          Diagnostic<JavaFileObject> item, Description mismatchDescription) {
        return matcher.matches(item.getMessage(Locale.getDefault()));
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("a diagnostic with message ").appendDescriptionOf(matcher);
      }
    };
  }

  /**
   * Matches an Iterable of diagnostics if it contains a diagnostic on each line of the source file
   * that matches the pattern.  Does not match if a diagnostic appears on a line that is *not*
   * tagged with the pattern.
   *
   * @param source                    file to find matching lines
   * @param expectedDiagnosticComment any Pattern, used to match complete lines
   * @return a Hamcrest matcher
   */
  public static Matcher<Iterable<Diagnostic<? extends JavaFileObject>>>
  hasDiagnosticOnAllMatchingLines(final File source, Pattern expectedDiagnosticComment)
      throws IOException {
    List<Matcher<? super Iterable<Diagnostic<? extends JavaFileObject>>>> matchers =
        new ArrayList<Matcher<? super Iterable<Diagnostic<? extends JavaFileObject>>>>();

    final LineNumberReader reader = new LineNumberReader(new FileReader(source));
    do {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      Matcher<Iterable<? super Diagnostic<JavaFileObject>>> matcher;
      if (expectedDiagnosticComment.matcher(line).matches()) {
        matcher = hasItem(diagnosticOnLine(reader.getLineNumber()));
      } else {
        matcher = not(hasItem(diagnosticOnLine(reader.getLineNumber())));
      }
      matchers.add(matcher);
    } while (true);

    return allOf(matchers);
  }
}