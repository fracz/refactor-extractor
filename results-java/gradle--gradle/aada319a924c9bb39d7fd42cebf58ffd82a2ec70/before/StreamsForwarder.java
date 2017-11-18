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

package org.gradle.process.internal.streams;

import org.gradle.util.DisconnectableInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;

/**
 * by Szczepan Faber, created at: 4/17/12
 */
public class StreamsForwarder {

    private final OutputStream standardOutput;
    private final OutputStream errorOutput;
    private final InputStream input;

    public StreamsForwarder(OutputStream standardOutput, OutputStream errorOutput, InputStream input) {
        this.standardOutput = standardOutput;
        this.errorOutput = errorOutput;
        this.input = input;
    }

    public StreamsForwarder(OutputStream standardOutput) {
        this(standardOutput, SafeStreams.systemErr(), SafeStreams.emptyInput());
    }

    public StreamsForwarder() {
        this(SafeStreams.systemOut(), SafeStreams.systemErr(), SafeStreams.emptyInput());
    }

    public void start(Executor executor) {
        executor.execute(standardInputRunner);
        executor.execute(errorOutputRunner);
        executor.execute(standardOutputRunner);
    }

    public void close() throws IOException {
        standardInputRunner.closeInput();
    }

    private ExecOutputHandleRunner standardOutputRunner;
    private ExecOutputHandleRunner errorOutputRunner;
    private ExecOutputHandleRunner standardInputRunner;

    public void connectStreams(Process process) {
        InputStream instr = new DisconnectableInputStream(input);

        standardOutputRunner = new ExecOutputHandleRunner("read process standard output",
                process.getInputStream(), standardOutput);
        errorOutputRunner = new ExecOutputHandleRunner("read process error output", process.getErrorStream(),
                errorOutput);
        standardInputRunner = new ExecOutputHandleRunner("write process standard input",
                instr, process.getOutputStream());
    }
}