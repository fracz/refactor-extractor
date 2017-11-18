/*
 * Copyright 2007-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle;

import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.groovy.scripts.ScriptSource;
import org.gradle.util.HelperUtil;
import org.hamcrest.Matcher;
import static org.hamcrest.Matchers.*;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

@RunWith(JMock.class)
public class BuildExceptionReporterTest {
    private final JUnit4Mockery context = new JUnit4Mockery();
    private Logger logger = context.mock(Logger.class);
    private StartParameter startParameter = new StartParameter();
    private BuildExceptionReporter reporter = new BuildExceptionReporter(logger, startParameter);

    @Test
    public void reportsBuildFailure() {
        final GradleException exception = new GradleException("<message>");
        final Matcher<String> errorMessage = allOf(containsString("Build failed with an exception."),
                containsString("<message>"));

        context.checking(new Expectations() {{
            one(logger).error(with(errorMessage));
        }});

        reporter.buildFinished(HelperUtil.createBuildResult(exception));
    }

    @Test
    public void reportsBuildFailureWhenCauseHasNoMessage() {
        final GradleException exception = new GradleException();
        final Matcher<String> errorMessage = allOf(containsString("Build failed with an exception."),
                containsString(GradleException.class.getName() + " (no error message)"));

        context.checking(new Expectations() {{
            one(logger).error(with(errorMessage));
        }});

        reporter.buildFinished(HelperUtil.createBuildResult(exception));
    }

    @Test
    public void reportsGradleScriptException() {
        final GradleScriptException exception
                = new GradleScriptException("<message>", new RuntimeException("<cause>"),
                context.mock(ScriptSource.class, "script")) {
            @Override
            public String getLocation() {
                return "<location>";
            }
        };
        GradleScriptException wrapper = new GradleScriptException("<wrapper>", exception, context.mock(ScriptSource.class, "wrapper"));

        final Matcher<String> errorMessage = allOf(containsString("Build failed with an exception."),
                containsString("<location>"),
                containsString("<message>"),
                containsString("Cause: <cause>"));

        context.checking(new Expectations() {{
            one(logger).error(with(errorMessage));
        }});

        reporter.buildFinished(HelperUtil.createBuildResult(wrapper));
    }

    @Test
    public void reportsGradleScriptExceptionWithMultipleCauses() {
        final GradleScriptException exception
                = new GradleScriptException("<message>", new RuntimeException("<cause>"),
                context.mock(ScriptSource.class, "script")) {
            @Override
            public String getLocation() {
                return "<location>";
            }

            @Override
            public List<Throwable> getReportableCauses() {
                return Arrays.asList(new RuntimeException("<outer>"), getCause());
            }
        };
        GradleScriptException wrapper = new GradleScriptException("<wrapper>", exception, context.mock(ScriptSource.class, "wrapper"));

        final Matcher<String> errorMessage = allOf(containsString("Build failed with an exception."),
                containsString("<location>"),
                containsString("<message>"),
                containsString("Cause: <outer>"),
                containsString("<cause>"));

        context.checking(new Expectations() {{
            one(logger).error(with(errorMessage));
        }});

        reporter.buildFinished(HelperUtil.createBuildResult(wrapper));
    }

    @Test
    public void reportsGradleScriptExceptionWhenCauseHasNoMessage() {
        final GradleScriptException exception
                = new GradleScriptException("<message>", new RuntimeException(),
                context.mock(ScriptSource.class)) {
            @Override
            public String getLocation() {
                return "<location>";
            }
        };
        final Matcher<String> errorMessage = allOf(containsString("Build failed with an exception."),
                containsString("<location>"),
                containsString("<message>"),
                containsString("Cause: " + RuntimeException.class.getName() + " (no error message)"));

        context.checking(new Expectations() {{
            one(logger).error(with(errorMessage));
        }});

        reporter.buildFinished(HelperUtil.createBuildResult(exception));
    }

    @Test
    public void reportsBuildFailureWhenOptionsHaveNotBeenSet() {
        final GradleException exception = new GradleException("<message>");
        final Matcher<String> errorMessage = allOf(containsString("Build failed with an exception."),
                containsString("<message>"));

        context.checking(new Expectations() {{
            one(logger).error(with(errorMessage));
        }});

        reporter = new BuildExceptionReporter(logger, startParameter);
        reporter.buildFinished(HelperUtil.createBuildResult(exception));
    }

    @Test
    public void reportsInternalFailure() {
        final RuntimeException failure = new RuntimeException("<message>");

        context.checking(new Expectations() {{
            one(logger).error(with(containsString("Build aborted because of an internal error.")), with(sameInstance(failure)));
        }});

        reporter.buildFinished(HelperUtil.createBuildResult(failure));
    }

    @Test
    public void reportsInternalFailureWhenOptionsHaveNotBeenSet() {
        final RuntimeException failure = new RuntimeException("<message>");

        context.checking(new Expectations() {{
            one(logger).error(with(containsString("Build aborted because of an internal error.")), with(sameInstance(failure)));
        }});

        reporter = new BuildExceptionReporter(logger, startParameter);
        reporter.buildFinished(HelperUtil.createBuildResult(failure));
    }

    @Test
    public void doesNothingWheBuildIsSuccessful() {
        reporter.buildFinished(HelperUtil.createBuildResult(null));
    }

}