/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.api.internal.tasks.testing.junit.result;

import org.apache.commons.io.IOUtils;
import org.gradle.api.tasks.testing.*;
import org.gradle.internal.UncheckedException;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.util.Collections.emptySet;

/**
 * by Szczepan Faber, created at: 11/13/12
 */
public class TestReportDataCollector implements TestListener, TestOutputListener, TestResultsProvider {

    private final Map<String, TestClassResult> results = new HashMap<String, TestClassResult>();
    private final File resultsDir;
    CachingFileWriter cachingFileWriter = new CachingFileWriter(10); //TODO SF calculate based on parallel forks

    public TestReportDataCollector(File resultsDir) {
        this.resultsDir = resultsDir;
        if (!resultsDir.isDirectory()) {
            throw new IllegalArgumentException("Directory [" + resultsDir + "] for binary test results does not exist or it is not a valid folder.");
        }
        if (resultsDir.list().length > 0) {
            throw new IllegalArgumentException("Directory [" + resultsDir + "] for binary test results must be empty!");
        }
    }

    public void beforeSuite(TestDescriptor suite) {
    }

    public void afterSuite(TestDescriptor suite, TestResult result) {
        if (suite.getParent() == null) {
            cachingFileWriter.closeAll();
        }
    }

    public void beforeTest(TestDescriptor testDescriptor) {
    }

    public void afterTest(TestDescriptor testDescriptor, TestResult result) {
        if (!testDescriptor.isComposite()) {
            TestMethodResult methodResult = new TestMethodResult(testDescriptor.getName(), result);
            TestClassResult classResult = results.get(testDescriptor.getClassName());
            if (classResult == null) {
                classResult = new TestClassResult(result.getStartTime());
                results.put(testDescriptor.getClassName(), classResult);
            }

            classResult.add(methodResult);
        }
    }

    public void onOutput(TestDescriptor testDescriptor, TestOutputEvent outputEvent) {
        String className = testDescriptor.getClassName();
        if (className == null) {
            //this means that we receive an output before even starting any class (or too late).
            //we don't have a place for such output in any of the reports so skipping.
            return;
        }
        cachingFileWriter.writeUTF(outputsFile(className, outputEvent.getDestination()), outputEvent.getMessage());
    }

    private File outputsFile(String className, TestOutputEvent.Destination destination) {
        return destination == TestOutputEvent.Destination.StdOut? standardOutputFile(className) : standardErrorFile(className);
    }

    private File standardErrorFile(String className) {
        return new File(resultsDir, className + ".stderr");
    }

    private File standardOutputFile(String className) {
        return new File(resultsDir, className + ".stdout");
    }

    public Map<String, TestClassResult> getResults() {
        return results;
    }

    public Iterable<String> getOutputs(final String className, final TestOutputEvent.Destination destination) {
        final File file = outputsFile(className, destination);
        if (!file.exists()) {
            return emptySet();
        }
        final Iterator<String> outputs = new OutputsIterator(file);
        return new Iterable<String>() {
            public Iterator<String> iterator() {
                return outputs;
            }
        };
    }

    private static class OutputsIterator implements Iterator<String> {
        DataInputStream in;
        String line;

        public OutputsIterator(File file) {
            try {
                in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            } catch (IOException e) {
                throw UncheckedException.throwAsUncheckedException(e);
            }
        }

        public boolean hasNext() {
            try {
                line = in.readUTF();
                return true;
            } catch (EOFException e) {
                //finished reading...
                IOUtils.closeQuietly(in);
                return false;
            } catch (IOException e) {
                IOUtils.closeQuietly(in);
                throw UncheckedException.throwAsUncheckedException(e);
            }
        }

        public String next() {
            return line;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}