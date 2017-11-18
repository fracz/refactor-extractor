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
package org.gradle.api.testing.execution.fork;

import org.gradle.api.testing.execution.Pipeline;
import org.gradle.api.testing.execution.fork.policies.ForkPolicyForkInfo;
import org.gradle.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Tom Eyckmans
 */
public class ForkInfo {
    private final int id;
    private final Pipeline pipeline;
    private ForkPolicyForkInfo policyInfo;
    private boolean restarting;

    private List<ForkInfoListener> listeners = new CopyOnWriteArrayList<ForkInfoListener>();

    public ForkInfo(int id, Pipeline pipeline) {
        this.id = id;
        this.pipeline = pipeline;
    }

    public int getId() {
        return id;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public ForkPolicyForkInfo getForkPolicyInfo() {
        return policyInfo;
    }

    public void setPolicyInfo(ForkPolicyForkInfo policyInfo) {
        this.policyInfo = policyInfo;
    }

    public boolean isRestarting() {
        return restarting;
    }

    public void setRestarting(boolean restarting) {
        this.restarting = restarting;
    }

    public void addListener(ForkInfoListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ForkInfoListener listener) {
        listeners.remove(listener);
    }

    public void started() {
        final List<ForkInfoListener> currentListeners = new ArrayList<ForkInfoListener>(listeners);
        for (final ForkInfoListener currentListener : currentListeners) {
            ThreadUtils.join(ThreadUtils.run(new Runnable() {
                public void run() {
                    currentListener.started(id);
                }
            }));
        }
    }

    public void stopped(final Throwable cause) {
        final List<ForkInfoListener> currentListeners = new ArrayList<ForkInfoListener>(listeners);
        for (final ForkInfoListener currentListener : currentListeners) {
            ThreadUtils.join(ThreadUtils.run(new Runnable() {
                public void run() {
                    currentListener.stopped(id, cause);
                }
            }));
        }
    }
}