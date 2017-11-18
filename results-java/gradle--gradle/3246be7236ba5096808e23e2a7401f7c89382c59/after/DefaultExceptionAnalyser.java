/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.initialization;

import org.gradle.api.GradleScriptException;
import org.gradle.api.ScriptCompilationException;
import org.gradle.api.internal.Contextual;
import org.gradle.api.internal.ExceptionAnalyser;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.tasks.TaskExecutionException;
import org.gradle.groovy.scripts.Script;
import org.gradle.groovy.scripts.ScriptExecutionListener;
import org.gradle.groovy.scripts.ScriptSource;
import org.gradle.listener.ListenerManager;

import java.util.HashMap;
import java.util.Map;

public class DefaultExceptionAnalyser implements ExceptionAnalyser, ScriptExecutionListener {
    private final Map<String, ScriptSource> scripts = new HashMap<String, ScriptSource>();

    public DefaultExceptionAnalyser(ListenerManager listenerManager) {
        listenerManager.addListener(this);
    }

    public void beforeScript(Script script) {
        ScriptSource source = script.getScriptSource();
        scripts.put(source.getFileName(), source);
    }

    public void afterScript(Script script, Throwable result) {
    }

    public Throwable transform(Throwable exception) {
        Throwable actualException = findDeepest(exception);
        if (actualException == null) {
            return exception;
        }

        // todo - remove this special case
        if (actualException instanceof ScriptCompilationException) {
            return actualException;
        }

        ScriptSource source = null;
        Integer lineNumber = null;
        for (Throwable currentException = actualException; currentException != null; currentException = currentException.getCause()) {
            for (StackTraceElement element : currentException.getStackTrace()) {
                if (scripts.containsKey(element.getFileName())) {
                    source = scripts.get(element.getFileName());
                    lineNumber = element.getLineNumber() >= 0 ? element.getLineNumber() : null;
                    break;
                }
            }
        }

        if (source == null) {
            if (!(actualException instanceof TaskExecutionException)) {
                return actualException;
            }
            TaskExecutionException taskExecutionException = (TaskExecutionException) actualException;
            source = ((ProjectInternal) taskExecutionException.getTask().getProject()).getBuildScriptSource();
        }

        return new GradleScriptException(actualException.getMessage(), actualException, source, lineNumber);
    }

    private Throwable findDeepest(Throwable exception) {
        Throwable result = null;
        for (Throwable current = exception; current != null; current = current.getCause()) {
            // todo - remove this special case
            if (current instanceof TaskExecutionException) {
                return current;
            }
            if (current.getClass().getAnnotation(Contextual.class) != null) {
                result = current;
            }
        }
        return result;
    }
}