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

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.context.DefaultTrace;
import com.navercorp.pinpoint.profiler.context.DefaultTraceContext;
import com.navercorp.pinpoint.profiler.context.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.storage.SpanStorage;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.LoggingDataSender;
import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.PinpointSocketReconnectEventListener;

import org.apache.thrift.TBase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class TraceTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void trace() {
        DefaultTraceId traceID = new DefaultTraceId("agent", 0, 1);
        DefaultTraceContext defaultTraceConetxt = getDefaultTraceContext();
        DefaultTrace trace = new DefaultTrace(defaultTraceConetxt , traceID);
        trace.setStorage(new SpanStorage(LoggingDataSender.DEFAULT_LOGGING_DATA_SENDER));
        trace.traceBlockBegin();

        // http server receive
        trace.recordServiceType(ServiceType.UNKNOWN);
        trace.recordRpcName("http://");

        trace.recordEndPoint("http:localhost:8080");
        trace.recordAttribute(AnnotationKey.API, "VALUE");

        // get data form db
        getDataFromDB(trace);

        // response to client

        trace.traceBlockEnd();
    }


    @Test
    public void popEventTest() {
        DefaultTraceId traceID = new DefaultTraceId("agent", 0, 1);
        DefaultTraceContext defaultTraceConetxt = getDefaultTraceContext();
        DefaultTrace trace = new DefaultTrace(defaultTraceConetxt, traceID);
        TestDataSender dataSender = new TestDataSender();
        trace.setStorage(new SpanStorage(LoggingDataSender.DEFAULT_LOGGING_DATA_SENDER));
//        trace.traceBlockBegin();

        // response to client

        trace.traceRootBlockEnd();

        logger.info(String.valueOf(dataSender.event));
    }

    private DefaultTraceContext getDefaultTraceContext() {
        DefaultTraceContext defaultTraceContext = new DefaultTraceContext();
        defaultTraceContext.setAgentInformation(new AgentInformation("agentId", "applicationName", System.currentTimeMillis(), 10, "test", "127.0.0.1", ServiceType.TOMCAT.getCode(), Version.VERSION));
        return defaultTraceContext;
    }

    public class TestDataSender implements EnhancedDataSender {
        public boolean event;

        @Override
        public boolean send(TBase<?, ?> data) {
            event = true;
            return false;
        }

        @Override
        public void stop() {
        }

        @Override
        public boolean request(TBase<?, ?> data) {
            return send(data);
        }

        @Override
        public boolean request(TBase<?, ?> data, int retry) {
            return send(data);
        }

        @Override
        public boolean request(TBase<?, ?> data, FutureListener<ResponseMessage> listener) {
            return send(data);
        }

        @Override
        public boolean addReconnectEventListener(PinpointSocketReconnectEventListener eventListener) {
            return false;
        }

        @Override
        public boolean removeReconnectEventListener(PinpointSocketReconnectEventListener eventListener) {
            return false;
        }

        @Override
        public boolean isNetworkAvailable() {
            return true;
        }
    }

    private void getDataFromDB(Trace trace) {
        trace.traceBlockBegin();

        // db server request
        trace.recordServiceType(ServiceType.MYSQL);
        trace.recordRpcName("rpc");

        trace.recordAttribute(AnnotationKey.SQL, "SELECT * FROM TABLE");

        // get a db response

        trace.traceBlockEnd();


    }
}