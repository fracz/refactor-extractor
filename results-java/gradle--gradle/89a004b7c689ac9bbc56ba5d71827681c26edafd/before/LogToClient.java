/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.launcher.daemon.server.exec;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.launcher.daemon.logging.DaemonMessages;
import org.gradle.launcher.daemon.protocol.Build;
import org.gradle.logging.LoggingManagerInternal;
import org.gradle.logging.internal.OutputEvent;
import org.gradle.logging.internal.OutputEventListener;

class LogToClient extends BuildCommandOnly {

    private static final Logger LOGGER = Logging.getLogger(LogToClient.class);

    private final LoggingManagerInternal loggingManager;

    public LogToClient(LoggingManagerInternal loggingManager) {
        this.loggingManager = loggingManager;
    }

    protected void doBuild(final DaemonCommandExecution execution, Build build) {
        OutputEventListener listener = new OutputEventListener() {
            public void onOutput(OutputEvent event) {
                try {
                    execution.getConnection().dispatch(event);
                } catch (Exception e) {
                    //Ignore. It means the client has disconnected so no point sending him any log output.
                    //we should be checking if client still listens elsewhere anyway.
                }
            }
        };

        loggingManager.setLevel(build.getStartParameter().getLogLevel());
        LOGGER.info("About to start relaying all logs to the client via the connection.");
        loggingManager.start();
        loggingManager.addOutputEventListener(listener);
        LOGGER.info(DaemonMessages.STARTED_RELAYING_LOGS);

        try {
            execution.proceed();
        } finally {
            loggingManager.removeOutputEventListener(listener);
            loggingManager.stop();
        }
    }
}
