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
package org.gradle.api.testing.execution.control.server.messagehandlers;

import org.apache.mina.core.session.IoSession;
import org.gradle.api.testing.execution.PipelineDispatcher;
import org.gradle.api.testing.execution.control.messages.client.NextActionRequestMessage;
import org.gradle.api.testing.execution.control.messages.server.ExecuteTestActionMessage;
import org.gradle.api.testing.execution.control.messages.server.StopForkActionMessage;
import org.gradle.api.testing.execution.control.messages.server.WaitActionMesssage;
import org.gradle.api.testing.execution.control.refork.ReforkDecisionContext;
import org.gradle.api.testing.execution.control.refork.ReforkController;
import org.gradle.api.testing.execution.control.server.TestServerClientHandle;
import org.gradle.api.testing.fabric.TestClassProcessResult;
import org.gradle.api.testing.fabric.TestClassRunInfo;
import org.gradle.api.testing.reporting.Report;
import org.gradle.api.testing.reporting.TestClassProcessResultReportInfo;

import java.util.List;

/**
 * @author Tom Eyckmans
 */
public class NextActionRequestMessageHandler extends AbstractTestServerControlMessageHandler {

    protected NextActionRequestMessageHandler(PipelineDispatcher pipelineDispatcher) {
        super(pipelineDispatcher);
    }

    public void handle(IoSession ioSession, Object controlMessage, TestServerClientHandle client) {
        final NextActionRequestMessage message = (NextActionRequestMessage) controlMessage;
        final int forkId = message.getForkId();
        final int pipelineId = pipeline.getId();

        if (pipelineDispatcher.isStopping()) {
            stopClient(ioSession, pipelineId, client);
        } else {
            if (pipelineDispatcher.isAllTestsExecuted() && pipelineDispatcher.isPipelineSplittingEnded()) {
                stopClient(ioSession, pipelineId, client);

                pipelineDispatcher.stop();
            } else {
                final TestClassProcessResult previousProcesTestResult = message.getPreviousProcessedTestResult();

                processPreviousTestResult(forkId, previousProcesTestResult);

                if (isReforkNeeded(message)) {
                    restartClient(ioSession, pipelineId, client);
                } else {
                    final TestClassRunInfo nextTest = client.nextTest(pipelineDispatcher);

                    if (nextTest == null) {
                        ioSession.write(new WaitActionMesssage(pipelineId, 1000));
                    }
                    else {
                        ioSession.write(new ExecuteTestActionMessage(pipelineId, nextTest));
                    }
                }
            }
        }
    }

    void stopClient(IoSession ioSession, int pipelineId, TestServerClientHandle client) {
        ioSession.write(new StopForkActionMessage(pipelineId));
    }

    void restartClient(IoSession ioSession, int pipelineId, TestServerClientHandle client) {
        client.restarting();

        ioSession.write(new StopForkActionMessage(pipelineId));
    }

    void processPreviousTestResult(int forkId, TestClassProcessResult previousProcessResult) {
        // TODO submit to thread pool before reporting to different reports.
        if (previousProcessResult != null) {
            final List<Report> reports = pipeline.getReports();
            final TestClassProcessResultReportInfo result = new TestClassProcessResultReportInfo(forkId, pipeline, previousProcessResult);
            for ( final Report report : reports ) {
                report.addReportInfo(result);
            }
        }
    }

    boolean isReforkNeeded(NextActionRequestMessage message) {
        boolean reforkNeeded = false;

        final ReforkController reforkController = pipeline.getReforkController();
        if ( reforkController != null ) {
            final ReforkDecisionContext reforkDecisionContext = message.getReforkDecisionContext();

            if ( reforkDecisionContext != null )
                reforkNeeded = reforkController.reforkNeeded(reforkDecisionContext);
        }

        return reforkNeeded;
    }
}