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

package org.gradle.api.internal.changedetection;

import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.TaskExecutionHistory;
import org.gradle.api.internal.TaskInternal;
import org.gradle.api.internal.changedetection.rules.*;
import org.gradle.api.internal.execution.RebuildTaskInputChanges;
import org.gradle.api.internal.file.collections.SimpleFileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskInputChanges;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class DefaultTaskArtifactStateRepository implements TaskArtifactStateRepository {
    private static final int MAX_OUT_OF_DATE_MESSAGES = 10;

    private static final Logger LOGGER = Logging.getLogger(DefaultTaskArtifactStateRepository.class);
    private final TaskHistoryRepository taskHistoryRepository;
    private final FileSnapshotter outputFilesSnapshotter;
    private final FileSnapshotter inputFilesSnapshotter;

    public DefaultTaskArtifactStateRepository(TaskHistoryRepository taskHistoryRepository, FileSnapshotter outputFilesSnapshotter, FileSnapshotter inputFilesSnapshotter) {
        this.taskHistoryRepository = taskHistoryRepository;
        this.outputFilesSnapshotter = outputFilesSnapshotter;
        this.inputFilesSnapshotter = inputFilesSnapshotter;
    }

    public TaskArtifactState getStateFor(final TaskInternal task) {
        return new TaskArtifactStateImpl(task, taskHistoryRepository.getHistory(task));
    }

    private class UpToDateStates {
        private TaskUpToDateState noHistoryState;
        private TaskUpToDateState inputFilesState;
        private TaskUpToDateState inputPropertiesState;
        private TaskUpToDateState taskTypeState;
        private TaskUpToDateState outputFilesState;
        private SummaryUpToDateState allTaskChanges;
        private SummaryUpToDateState rebuildChanges;

        public UpToDateStates(TaskInternal task, TaskExecution lastExecution, TaskExecution thisExecution, FileSnapshotter outputFilesSnapshotter, FileSnapshotter inputFilesSnapshotter) {
            noHistoryState = NoHistoryUpToDateRule.create(task, lastExecution);
            taskTypeState = TaskTypeChangedUpToDateRule.create(task, lastExecution, thisExecution);
            inputPropertiesState = InputPropertiesChangedUpToDateRule.create(task, lastExecution, thisExecution);
            outputFilesState = OutputFilesChangedUpToDateRule.create(task, lastExecution, thisExecution, outputFilesSnapshotter);
            inputFilesState = InputFilesChangedUpToDateRule.create(task, lastExecution, thisExecution, inputFilesSnapshotter);
            allTaskChanges = new SummaryUpToDateState(MAX_OUT_OF_DATE_MESSAGES, noHistoryState, taskTypeState, inputPropertiesState, outputFilesState, inputFilesState);
            rebuildChanges = new SummaryUpToDateState(1, noHistoryState, taskTypeState, inputPropertiesState, outputFilesState);
        }

        public TaskUpToDateState getInputFilesState() {
            return inputFilesState;
        }

        public SummaryUpToDateState getAllTaskState() {
            return allTaskChanges;
        }

        public SummaryUpToDateState getRebuildState() {
            return rebuildChanges;
        }
    }

    private class TaskArtifactStateImpl implements TaskArtifactState, TaskExecutionHistory {
        private final TaskInternal task;
        private final TaskExecution lastExecution;
        private final TaskExecution thisExecution;
        private final TaskHistoryRepository.History history;
        private boolean upToDate;
        private UpToDateStates states;

        public TaskArtifactStateImpl(TaskInternal task, TaskHistoryRepository.History history) {
            this.task = task;
            this.lastExecution = history.getPreviousExecution();
            this.thisExecution = history.getCurrentExecution();
            this.history = history;
        }

        public boolean isUpToDate() {
            final List<String> messages = new ArrayList<String>();
            getStates().getAllTaskState().findChanges(new Action<TaskUpToDateChange>() {
                public void execute(TaskUpToDateChange taskUpToDateStateChange) {
                    messages.add(taskUpToDateStateChange.getMessage());
                }
            });
            if (messages.isEmpty()) {
                upToDate = true;
                LOGGER.info("Skipping {} as it is up-to-date.", task);
                return true;
            }
            logUpToDateMessages(messages);
            return false;
        }

        private void logUpToDateMessages(List<String> messages) {
            if (LOGGER.isInfoEnabled()) {
                Formatter formatter = new Formatter();
                formatter.format("Executing %s due to:", task);
                for (String message : messages) {
                    formatter.format("%n  %s", message);
                }
                LOGGER.info(formatter.toString());
            }
        }

        public TaskInputChanges getInputChanges() {
            assert !upToDate : "Should not be here if the task is up-to-date";

            if (incrementalRequiresRebuild()) {
                return new RebuildTaskInputChanges(task);
            }
            return new IncrementalTaskInputChanges(getStates().getInputFilesState());
        }

        public FileCollection getOutputFiles() {
            return lastExecution != null && lastExecution.getOutputFilesSnapshot() != null ? lastExecution.getOutputFilesSnapshot().getFiles() : new SimpleFileCollection();
        }

        public TaskExecutionHistory getExecutionHistory() {
            return this;
        }

        public void afterTask() {
            if (upToDate) {
                return;
            }

            getStates().getAllTaskState().snapshotAfterTask();
            history.update();
        }

        public void beforeTask() {
        }

        public void finished() {
            // TODO:DAZ Release any pent-up state: paused UpToDateStates etc.
        }

        private UpToDateStates getStates() {
            if (states == null) {
                // Calculate initial state - note this is potentially expensive
                states = new UpToDateStates(task, lastExecution, thisExecution, outputFilesSnapshotter, inputFilesSnapshotter);
            }
            return states;
        }

        public boolean incrementalRequiresRebuild() {
            return getStates().getRebuildState().hasChanges();
        }
    }

    private static class NoHistoryUpToDateRule {
        public static TaskUpToDateState create(final TaskInternal task, final TaskExecution previousExecution) {
            return new SimpleUpToDateState() {
                @Override
                protected void addAllChanges(List<TaskUpToDateChange> changes) {
                    if (previousExecution == null) {
                        changes.add(new DescriptiveChange("No history is available for %s.", task));
                    }
                }

                public void snapshotAfterTask() {
                }
            };
        }
    }

}