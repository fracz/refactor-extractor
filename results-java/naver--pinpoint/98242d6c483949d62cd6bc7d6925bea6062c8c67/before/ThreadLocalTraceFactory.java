/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.TraceType;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.context.storage.AsyncStorage;
import com.navercorp.pinpoint.profiler.context.storage.Storage;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.util.NamedThreadLocal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author emeroad
 * @author Taejin Koo
 */
public class ThreadLocalTraceFactory implements TraceFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ThreadLocal<Trace> threadLocal = new NamedThreadLocal<Trace>("Trace");

    private final TraceContext traceContext;

    private final StorageFactory storageFactory;
    private final Sampler sampler;

    // Unique id for tracing a internal stacktrace and calculating a slow time of activethreadcount
    // moved here in order to make codes simpler for now
    private final AtomicLong transactionId = new AtomicLong(0);

    public ThreadLocalTraceFactory(TraceContext traceContext, StorageFactory storageFactory, Sampler sampler) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        if (storageFactory == null) {
            throw new NullPointerException("storageFactory must not be null");
        }
        if (sampler == null) {
            throw new NullPointerException("sampler must not be null");
        }
        this.traceContext = traceContext;
        this.storageFactory = storageFactory;
        this.sampler = sampler;
    }


    /**
     * Return Trace object after validating whether it can be sampled or not.
     * @return Trace
     */
    @Override
    public Trace currentTraceObject() {
        final Trace trace = threadLocal.get();
        if (trace == null) {
            return null;
        }
        if (trace.canSampled()) {
            return trace;
        }
        return null;
    }

    /**
     * Return Trace object without validating
     * @return
     */
    @Override
    public Trace currentRpcTraceObject() {
        final Trace trace = threadLocal.get();
        if (trace == null) {
            return null;
        }
        return trace;
    }

    @Override
    public Trace currentRawTraceObject() {
        return threadLocal.get();
    }

    @Override
    public Trace disableSampling() {
        checkBeforeTraceObject();
        final Trace metricTrace = newDisableTrace();
        threadLocal.set(metricTrace);

        return metricTrace;
    }

    // continue to trace the request that has been determined to be sampled on previous nodes
    @Override
    public Trace continueTraceObject(final TraceId traceID) {
        checkBeforeTraceObject();

        // TODO need to modify how to bind a datasender
        // always set true because the decision of sampling has been  made on previous nodes
        // TODO need to consider as a target to sample in case Trace object has a sampling flag (true) marked on previous node.
        final boolean sampling = true;
        final DefaultTrace trace = new DefaultTrace(traceContext, traceID, sampling);
        // final Storage storage = storageFactory.createStorage();
        final Storage storage = storageFactory.createStorage();
        trace.setStorage(storage);
        threadLocal.set(trace);
        return trace;
    }


    @Override
    public Trace continueTraceObject(Trace trace) {
        checkBeforeTraceObject();

        threadLocal.set(trace);
        return trace;
    }

    private void checkBeforeTraceObject() {
        final Trace old = this.threadLocal.get();
        if (old != null) {
            final PinpointException exception = new PinpointException("already Trace Object exist.");
            if (logger.isWarnEnabled()) {
                logger.warn("beforeTrace:{}", old, exception);
            }
            throw exception;
        }
    }

    @Override
    public Trace newTraceObject() {
        return newTraceObject(TraceType.DEFAULT);
    }

    @Override
    public Trace newTraceObject(TraceType traceType) {
        checkBeforeTraceObject();
        // TODO need to modify how to inject a datasender
        final boolean sampling = sampler.isSampling();
        if (sampling) {
            // final Storage storage = storageFactory.createStorage();
            final DefaultTrace trace = new DefaultTrace(traceContext, nextTransactionId(), sampling);

            final Storage storage = storageFactory.createStorage();
            trace.setStorage(storage);
            trace.setTraceType(traceType);
            threadLocal.set(trace);
            return trace;
        } else {
            final Trace disableTrace = newDisableTrace();
            threadLocal.set(disableTrace);
            return disableTrace;
        }
    }

    @Override
    public Trace removeTraceObject() {
        final Trace trace = currentRawTraceObject();
        this.threadLocal.remove();

        return trace;
    }


    private Trace newDisableTrace() {
        return new DisableTrace();
    }

    private long nextTransactionId() {
        return this.transactionId.getAndIncrement();
    }

    public Trace continueAsyncTraceObject(AsyncTraceId traceId, int asyncId, long startTime) {
        checkBeforeTraceObject();

        final TraceId parentTraceId = traceId.getParentTraceId();
        final boolean sampling = true;
        final DefaultTrace trace = new DefaultTrace(traceContext, parentTraceId, sampling);
        final Storage storage = storageFactory.createStorage();
        trace.setStorage(new AsyncStorage(storage));

        final AsyncTrace asyncTrace = new AsyncTrace(trace, asyncId, traceId.nextAsyncSequence(), startTime);
        threadLocal.set(asyncTrace);

        return asyncTrace;
    }
}