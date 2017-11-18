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
package org.gradle.tooling.internal.consumer.parameters;

import com.google.common.collect.ImmutableList;
import org.gradle.api.Nullable;
import org.gradle.internal.event.ListenerBroadcast;
import org.gradle.tooling.Failure;
import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.build.*;
import org.gradle.tooling.events.build.internal.*;
import org.gradle.tooling.events.task.*;
import org.gradle.tooling.events.task.internal.*;
import org.gradle.tooling.events.test.*;
import org.gradle.tooling.events.test.internal.*;
import org.gradle.tooling.internal.consumer.DefaultFailure;
import org.gradle.tooling.internal.protocol.InternalBuildProgressListener;
import org.gradle.tooling.internal.protocol.InternalFailSafeProgressListenersProvider;
import org.gradle.tooling.internal.protocol.InternalFailure;
import org.gradle.tooling.internal.protocol.events.*;

import java.util.*;

/**
 * Converts progress events sent from the tooling provider to the tooling client to the corresponding event types available on the public Tooling API, and broadcasts the converted events to the
 * matching progress listeners. This adapter handles all the different incoming progress event types (except the original logging-derived progress listener).
 */
class BuildProgressListenerAdapter implements InternalBuildProgressListener, InternalFailSafeProgressListenersProvider {

    static final OperationDescriptor DESCRIPTOR_NOT_FOUND = new OperationDescriptor() {
        @Override
        public String getName() {
            return "unknown";
        }

        @Override
        public String getDisplayName() {
            return "descriptor not found";
        }

        @Nullable
        @Override
        public OperationDescriptor getParent() {
            return null;
        }
    };

    private final ListenerBroadcast<TestProgressListener> testProgressListeners = new ListenerBroadcast<TestProgressListener>(TestProgressListener.class);
    private final ListenerBroadcast<TaskProgressListener> taskProgressListeners = new ListenerBroadcast<TaskProgressListener>(TaskProgressListener.class);
    private final ListenerBroadcast<BuildProgressListener> buildProgressListeners = new ListenerBroadcast<BuildProgressListener>(BuildProgressListener.class);
    private final Map<Object, OperationDescriptor> descriptorCache = new HashMap<Object, OperationDescriptor>();
    private final List<Throwable> listenerFailures = new ArrayList<Throwable>();

    private boolean failSafeListeners;

    BuildProgressListenerAdapter(BuildProgressListenerConfiguration configuration) {
        this.testProgressListeners.addAll(configuration.getTestListeners());
        this.taskProgressListeners.addAll(configuration.getTaskListeners());
        this.buildProgressListeners.addAll(configuration.getBuildListeners());
    }

    @Override
    public void setListenerFailSafeMode(boolean failSafe) {
        failSafeListeners = failSafe;
    }

    public List<Throwable> getListenerFailures() {
        return ImmutableList.copyOf(listenerFailures);
    }

    @Override
    public List<String> getSubscribedOperations() {
        if (testProgressListeners.isEmpty() && taskProgressListeners.isEmpty() && buildProgressListeners.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> operations = new ArrayList<String>();
        if (!testProgressListeners.isEmpty()) {
            operations.add(InternalBuildProgressListener.TEST_EXECUTION);
        }
        if (!taskProgressListeners.isEmpty()) {
            operations.add(InternalBuildProgressListener.TASK_EXECUTION);
        }
        if (!buildProgressListeners.isEmpty()) {
            operations.add(InternalBuildProgressListener.BUILD_EXECUTION);
        }
        return operations;
    }

    @Override
    public void onEvent(Object event) {
        if (failSafeListeners) {
            try {
                doBroadcast(event);
            } catch (Throwable t) {
                listenerFailures.add(t);
            }
        } else {
            doBroadcast(event);
        }
    }

    private void doBroadcast(Object event) {
        if (event instanceof InternalTestProgressEvent) {
            broadcastTestProgressEvent((InternalTestProgressEvent) event);
        } else if (event instanceof InternalTaskProgressEvent) {
            broadcastTaskProgressEvent((InternalTaskProgressEvent) event);
        } else if (event instanceof InternalBuildProgressEvent) {
            broadcastBuildProgressEvent((InternalBuildProgressEvent) event);
        }
    }

    private void broadcastTestProgressEvent(InternalTestProgressEvent event) {
        TestProgressEvent testProgressEvent = toTestProgressEvent(event);
        if (testProgressEvent != null) {
            testProgressListeners.getSource().statusChanged(testProgressEvent);
        }
    }

    private void broadcastTaskProgressEvent(InternalTaskProgressEvent event) {
        TaskProgressEvent taskProgressEvent = toTaskProgressEvent(event);
        if (taskProgressEvent != null) {
            taskProgressListeners.getSource().statusChanged(taskProgressEvent);
        }
    }

    private void broadcastBuildProgressEvent(InternalBuildProgressEvent event) {
        BuildProgressEvent buildProgressEvent = toBuildProgressEvent(event);
        if (buildProgressEvent != null) {
            buildProgressListeners.getSource().statusChanged(buildProgressEvent);
        }
    }

    private TestProgressEvent toTestProgressEvent(InternalTestProgressEvent event) {
        if (event instanceof InternalTestStartedProgressEvent) {
            return testStartedEvent((InternalTestStartedProgressEvent) event);
        } else if (event instanceof InternalTestFinishedProgressEvent) {
            return testFinishedEvent((InternalTestFinishedProgressEvent) event);
        } else {
            return null;
        }
    }

    private TaskProgressEvent toTaskProgressEvent(InternalTaskProgressEvent event) {
        if (event instanceof InternalTaskStartedProgressEvent) {
            return taskStartedEvent((InternalTaskStartedProgressEvent) event);
        } else if (event instanceof InternalTaskFinishedProgressEvent) {
            return taskFinishedEvent((InternalTaskFinishedProgressEvent) event);
        } else {
            return null;
        }
    }

    private BuildProgressEvent toBuildProgressEvent(InternalBuildProgressEvent event) {
        if (event instanceof InternalBuildStartedProgressEvent) {
            return buildStartedEvent((InternalBuildStartedProgressEvent) event);
        } else if (event instanceof InternalBuildFinishedProgressEvent) {
            return buildFinishedEvent((InternalBuildFinishedProgressEvent) event);
        } else {
            return null;
        }
    }

    private TestStartEvent testStartedEvent(InternalTestStartedProgressEvent event) {
        TestOperationDescriptor testDescriptor = addTestDescriptor(event.getDescriptor());
        return new DefaultTestStartEvent(event.getEventTime(), event.getDisplayName(), testDescriptor);
    }

    private TaskStartEvent taskStartedEvent(InternalTaskStartedProgressEvent event) {
        TaskOperationDescriptor descriptor = addTaskDescriptor(event.getDescriptor());
        return new DefaultTaskStartEvent(event.getEventTime(), event.getDisplayName(), descriptor);
    }

    private BuildProgressEvent buildStartedEvent(InternalBuildStartedProgressEvent event) {
        BuildOperationDescriptor descriptor = addBuildDescriptor(event.getDescriptor());
        return new DefaultBuildStartEvent(event.getEventTime(), event.getDisplayName(), descriptor);
    }

    private TestFinishEvent testFinishedEvent(InternalTestFinishedProgressEvent event) {
        TestOperationDescriptor descriptor = removeTestDescriptor(event.getDescriptor());
        return new DefaultTestFinishEvent(event.getEventTime(), event.getDisplayName(), descriptor, toTestResult(event.getResult()));
    }

    private TaskFinishEvent taskFinishedEvent(InternalTaskFinishedProgressEvent event) {
        TaskOperationDescriptor descriptor = removeTaskDescriptor(event.getDescriptor());
        return new DefaultTaskFinishEvent(event.getEventTime(), event.getDisplayName(), descriptor, toTaskResult(event.getResult()));
    }

    private BuildFinishEvent buildFinishedEvent(InternalBuildFinishedProgressEvent event) {
        BuildOperationDescriptor descriptor = removeBuildDescriptor(event.getDescriptor());
        return new DefaultBuildFinishEvent(event.getEventTime(), event.getDisplayName(), descriptor, toBuildResult(event.getResult()));
    }

    private synchronized TestOperationDescriptor addTestDescriptor(InternalTestDescriptor descriptor) {
        OperationDescriptor cachedTestDescriptor = this.descriptorCache.get(descriptor.getId());
        if (cachedTestDescriptor != null) {
            throw new IllegalStateException(String.format("Operation %s already available.", toString(descriptor)));
        }
        TestOperationDescriptor newTestDescriptor = toTestDescriptor(descriptor);
        descriptorCache.put(descriptor.getId(), newTestDescriptor);
        return newTestDescriptor;
    }

    private synchronized TaskOperationDescriptor addTaskDescriptor(InternalTaskDescriptor descriptor) {
        OperationDescriptor cachedTestDescriptor = this.descriptorCache.get(descriptor.getId());
        if (cachedTestDescriptor != null) {
            throw new IllegalStateException(String.format("Operation %s already available.", toString(descriptor)));
        }
        TaskOperationDescriptor newTaskDescriptor = toTaskDescriptor(descriptor);
        descriptorCache.put(descriptor.getId(), newTaskDescriptor);
        return newTaskDescriptor;
    }

    private synchronized BuildOperationDescriptor addBuildDescriptor(InternalBuildDescriptor descriptor) {
        OperationDescriptor cached = this.descriptorCache.get(descriptor.getId());
        if (cached != null) {
            throw new IllegalStateException(String.format("Operation %s already available.", toString(descriptor)));
        }
        BuildOperationDescriptor newBuildDescriptor = toBuildDescriptor(descriptor);
        descriptorCache.put(descriptor.getId(), newBuildDescriptor);
        return newBuildDescriptor;
    }

    private synchronized TestOperationDescriptor removeTestDescriptor(InternalTestDescriptor descriptor) {
        OperationDescriptor cachedTestDescriptor = this.descriptorCache.remove(descriptor.getId());
        if (cachedTestDescriptor == null) {
            throw new IllegalStateException(String.format("Operation %s is not available.", toString(descriptor)));
        }
        return assertDescriptorType(TestOperationDescriptor.class, cachedTestDescriptor);
    }

    private synchronized TaskOperationDescriptor removeTaskDescriptor(InternalTaskDescriptor descriptor) {
        OperationDescriptor cachedTestDescriptor = this.descriptorCache.remove(descriptor.getId());
        if (cachedTestDescriptor == null) {
            throw new IllegalStateException(String.format("Operation %s is not available.", toString(descriptor)));
        }
        return assertDescriptorType(TaskOperationDescriptor.class, cachedTestDescriptor);
    }

    private synchronized BuildOperationDescriptor removeBuildDescriptor(InternalBuildDescriptor descriptor) {
        OperationDescriptor cachedTestDescriptor = this.descriptorCache.remove(descriptor.getId());
        if (cachedTestDescriptor == null) {
            throw new IllegalStateException(String.format("Operation %s is not available.", toString(descriptor)));
        }
        return assertDescriptorType(BuildOperationDescriptor.class, cachedTestDescriptor);
    }

    @SuppressWarnings("unchecked")
    private <T extends OperationDescriptor> T assertDescriptorType(Class<T> type, OperationDescriptor descriptor) {
        Class<? extends OperationDescriptor> descriptorClass = descriptor.getClass();
        if (!type.isAssignableFrom(descriptorClass)) {
            throw new IllegalStateException(String.format("Unexpected descriptor type. Required %s but found %s", type.getName(), descriptorClass.getName()));
        }
        return (T) descriptor;
    }

    private TestOperationDescriptor toTestDescriptor(InternalTestDescriptor descriptor) {
        OperationDescriptor parent = getCachedDescriptor(descriptor.getParentId());
        if (descriptor instanceof InternalJvmTestDescriptor) {
            InternalJvmTestDescriptor jvmTestDescriptor = (InternalJvmTestDescriptor) descriptor;
            return new DefaultJvmTestOperationDescriptor(descriptor.getName(), descriptor.getDisplayName(), parent,
                toJvmTestKind(jvmTestDescriptor.getTestKind()), jvmTestDescriptor.getSuiteName(), jvmTestDescriptor.getClassName(), jvmTestDescriptor.getMethodName());
        } else {
            return new DefaultTestOperationDescriptor(descriptor.getName(), descriptor.getDisplayName(), parent);
        }
    }

    private static JvmTestKind toJvmTestKind(String testKind) {
        if (InternalJvmTestDescriptor.KIND_SUITE.equals(testKind)) {
            return JvmTestKind.SUITE;
        } else if (InternalJvmTestDescriptor.KIND_ATOMIC.equals(testKind)) {
            return JvmTestKind.ATOMIC;
        } else {
            return JvmTestKind.UNKNOWN;
        }
    }

    private TaskOperationDescriptor toTaskDescriptor(InternalTaskDescriptor descriptor) {
        OperationDescriptor parent = getCachedDescriptor(descriptor.getParentId());
        return new DefaultTaskOperationDescriptor(descriptor.getName(), descriptor.getDisplayName(), descriptor.getTaskPath(), parent);
    }

    private BuildOperationDescriptor toBuildDescriptor(InternalBuildDescriptor descriptor) {
        OperationDescriptor parent = getCachedDescriptor(descriptor.getParentId());
        return new DefaultBuildOperationDescriptor(descriptor.getName(), descriptor.getDisplayName(), parent);
    }

    private synchronized OperationDescriptor getCachedDescriptor(Object id) {
        if (id == null) {
            return null;
        }
        OperationDescriptor operationDescriptor = descriptorCache.get(id);
        if (operationDescriptor == null) {
            operationDescriptor = DESCRIPTOR_NOT_FOUND;
        }
        return operationDescriptor;
    }

    private TestOperationResult toTestResult(InternalTestResult result) {
        if (result instanceof InternalTestSuccessResult) {
            return new DefaultTestSuccessResult(result.getStartTime(), result.getEndTime());
        } else if (result instanceof InternalTestSkippedResult) {
            return new DefaultTestSkippedResult(result.getStartTime(), result.getEndTime());
        } else if (result instanceof InternalTestFailureResult) {
            return new DefaultTestFailureResult(result.getStartTime(), result.getEndTime(), toFailures(result.getFailures()));
        } else {
            return null;
        }
    }

    private static TaskOperationResult toTaskResult(InternalTaskResult result) {
        if (result instanceof InternalTaskSuccessResult) {
            return new DefaultTaskSuccessResult(result.getStartTime(), result.getEndTime(), ((InternalTaskSuccessResult) result).isUpToDate());
        } else if (result instanceof InternalTaskSkippedResult) {
            return new DefaultTaskSkippedResult(result.getStartTime(), result.getEndTime(), ((InternalTaskSkippedResult) result).getSkipMessage());
        } else if (result instanceof InternalTaskFailureResult) {
            return new DefaultTaskFailureResult(result.getStartTime(), result.getEndTime(), toFailures(result.getFailures()));
        } else {
            return null;
        }
    }

    private static BuildOperationResult toBuildResult(InternalBuildResult result) {
        if (result instanceof InternalBuildSuccessResult) {
            return new DefaultBuildSuccessResult(result.getStartTime(), result.getEndTime());
        } else if (result instanceof InternalBuildFailureResult) {
            return new DefaultBuildFailureResult(result.getStartTime(), result.getEndTime(), toFailures(result.getFailures()));
        } else {
            return null;
        }
    }

    private static List<Failure> toFailures(List<? extends InternalFailure> causes) {
        if (causes == null) {
            return null;
        }
        List<Failure> failures = new ArrayList<Failure>();
        for (InternalFailure cause : causes) {
            failures.add(toFailure(cause));
        }
        return failures;
    }

    private static Failure toFailure(InternalFailure origFailure) {
        return origFailure == null ? null : new DefaultFailure(
            origFailure.getMessage(),
            origFailure.getDescription(),
            toFailures(origFailure.getCauses()));
    }

    private static String toString(InternalTestDescriptor testDescriptor) {
        if (testDescriptor instanceof InternalJvmTestDescriptor) {
            return String.format("TestOperationDescriptor[id(%s), name(%s), className(%s), parent(%s)]",
                testDescriptor.getId(), testDescriptor.getName(), ((InternalJvmTestDescriptor) testDescriptor).getClassName(), testDescriptor.getParentId());
        } else {
            return String.format("TestOperationDescriptor[id(%s), name(%s), parent(%s)]", testDescriptor.getId(), testDescriptor.getName(), testDescriptor.getParentId());
        }
    }

    private static String toString(InternalTaskDescriptor taskDescriptor) {
        return String.format("TaskOperationDescriptor[id(%s), name(%s), parent(%s)]", taskDescriptor.getId(), taskDescriptor.getName(), taskDescriptor.getParentId());
    }

    private static String toString(InternalBuildDescriptor buildDescriptor) {
        return String.format("BuildOperationDescriptor[id(%s), name(%s), parent(%s)]", buildDescriptor.getId(), buildDescriptor.getName(), buildDescriptor.getParentId());
    }

}