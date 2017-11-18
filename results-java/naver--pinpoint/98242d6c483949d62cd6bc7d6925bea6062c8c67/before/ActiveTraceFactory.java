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
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.TraceType;

import java.util.List;

/**
 * @author Taejin Koo
 * @author emeroad
 */
public class ActiveTraceFactory implements TraceFactory {

    // memory leak defense threshold
    private static final int THREAD_LEAK_LIMIT = 1024 * 10;

    private final TraceFactory delegate;
    private final ActiveTraceRepository activeTraceRepository = new ActiveTraceRepository();

    private volatile boolean activeTraceTracking = false;



    private ActiveTraceFactory(TraceFactory delegate) {
        if (delegate == null) {
            throw new NullPointerException("delegate must not be null");
        }
        this.delegate = delegate;
    }

    public static TraceFactory wrap(TraceFactory traceFactory) {
        return new ActiveTraceFactory(traceFactory);
    }

    @Override
    public Trace currentTraceObject() {
        return this.delegate.currentTraceObject();
    }

    @Override
    public Trace currentRpcTraceObject() {
        return this.delegate.currentRpcTraceObject();
    }

    @Override
    public Trace currentRawTraceObject() {
        return this.delegate.currentRawTraceObject();
    }

    @Override
    public Trace disableSampling() {
        final Trace trace = this.delegate.disableSampling();
//      todo need dummy trace
//        attachTrace(trace);
        return trace;
    }

    @Override
    public Trace continueTraceObject(TraceId traceID) {
        final Trace trace = this.delegate.continueTraceObject(traceID);
        attachTrace(trace);
        return trace;
    }

    @Override
    public Trace continueTraceObject(Trace continueTrace) {
        final Trace trace = this.delegate.continueTraceObject(continueTrace);
        attachTrace(trace);
        return trace;
    }

    @Override
    public Trace continueAsyncTraceObject(AsyncTraceId traceId, int asyncId, long startTime) {
        return this.delegate.continueAsyncTraceObject(traceId, asyncId, startTime);
    }

    @Override
    public Trace newTraceObject() {
        final Trace trace = delegate.newTraceObject();
        attachTrace(trace);
        return trace;
    }


    @Override
    public Trace newTraceObject(TraceType traceType) {
        final Trace trace = delegate.newTraceObject(traceType);
        if (TraceType.DEFAULT == traceType) {
            attachTrace(trace);
        }
        return trace;
    }

    private void attachTrace(Trace trace) {
        // TODO Fix
        if (!trace.canSampled()) {
            return;
        }
        if (activeTraceTracking) {
            final long spanId = trace.getTraceId().getSpanId();
            // fix startTime, find Key;
            final ActiveTraceInfo activeTraceInfo = new ActiveTraceInfo(spanId, System.currentTimeMillis());
            this.activeTraceRepository.addActiveTrace(spanId, activeTraceInfo);
        }
    }

    @Override
    public Trace removeTraceObject() {
        final Trace trace = delegate.removeTraceObject();
        detachTrace(trace);
        return trace;
    }

    private void detachTrace(Trace trace) {
        if (!trace.canSampled()) {
            // TODO fix
            return;
        }
//        if (activeTraceTracking) {
            //  TODO incomplete state checking.
//        }
        long spanId = trace.getTraceId().getSpanId();
        this.activeTraceRepository.removeActiveTrace(spanId);
    }


    public void enable() {
        this.activeTraceTracking = true;
    }

    public void disable() {
        this.activeTraceTracking = false;
    }

    public List<ActiveTraceInfo> collect() {
        return this.activeTraceRepository.collect();
    }

}