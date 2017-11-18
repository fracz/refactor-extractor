/*
 * Copyright 2015 the original author or authors.
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

package org.gradle.internal.filewatch.jdk7;

import org.gradle.internal.concurrent.Stoppable;
import org.gradle.internal.filewatch.FileWatchInputs;
import org.gradle.internal.filewatch.FileWatcherService;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultFileWatcher implements FileWatcherService {
    private final ExecutorService executor;

    public DefaultFileWatcher(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public Stoppable watch(FileWatchInputs inputs, Runnable callback) {
        CountDownLatch latch = createLatch();
        AtomicBoolean runningFlag = new AtomicBoolean(true);
        Future<?> execution = executor.submit(new FileWatcherExecutor(runningFlag, callback, new ArrayList(inputs.getDirectoryTrees()), new ArrayList(inputs.getFiles()), latch));
        try {
            // wait until watching is active
            latch.await();
        } catch (InterruptedException e) {
            // ignore
        }
        return new FileWatcherStopper(runningFlag, execution);
    }

    protected CountDownLatch createLatch() {
        return new CountDownLatch(1);
    }

    static class FileWatcherStopper implements Stoppable {
        private final AtomicBoolean runningFlag;
        private final Future<?> execution;
        private static final int STOP_TIMEOUT_SECONDS = 10;

        public FileWatcherStopper(AtomicBoolean runningFlag, Future<?> execution) {
            this.runningFlag = runningFlag;
            this.execution = execution;
        }

        @Override
        public synchronized void stop() {
            if (runningFlag.compareAndSet(true, false)) {
                waitForStop();
            }
        }

        private void waitForStop() {
            try {
                execution.get(STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // ignore
            } catch (ExecutionException e) {
                // ignore
            } catch (TimeoutException e) {
                throw new RuntimeException("Running FileWatcherService wasn't stopped in timeout limits.", e);
            }
        }
    }
}