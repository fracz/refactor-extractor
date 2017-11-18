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
package com.navercorp.pinpoint.plugin.okhttp;

import static com.navercorp.pinpoint.common.trace.HistogramSchema.*;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

import com.navercorp.pinpoint.common.trace.ServiceType;


/**
 *
 * @author jaehong.kim
 *
 */
public final class OkHttpConstants {
    private OkHttpConstants() {
    }

    public static final ServiceType OK_HTTP_CLIENT = ServiceType.of(9058, "OK_HTTP_CLIENT", NORMAL_SCHEMA, RECORD_STATISTICS);
    public static final ServiceType OK_HTTP_CLIENT_INTERNAL = ServiceType.of(9059, "OK_HTTP_CLIENT_INTERNAL", "OK_HTTP_CLIENT", NORMAL_SCHEMA);

    public static final String BASIC_METHOD_INTERCEPTOR = "com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor";
    public static final String METADATA_ASYNC_TRACE_ID = "com.navercorp.pinpoint.bootstrap.interceptor.AsyncTraceIdAccessor";
    public static final String SEND_REQUEST_SCOPE = "SendRequestScope";
    public static final String CALL_SCOPE = "CallScope";

    public static final String FIELD_USER_REQUEST = "userRequest";
    public static final String FIELD_USER_RESPONSE = "userResponse";
    public static final String FIELD_CONNECTION = "connection";
    public static final String FIELD_HTTP_URL = "url";
}