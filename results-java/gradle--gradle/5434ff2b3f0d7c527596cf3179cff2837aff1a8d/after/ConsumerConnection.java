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

package org.gradle.tooling.internal.consumer.connection;

import org.gradle.tooling.internal.protocol.BuildOperationParametersVersion1;
import org.gradle.tooling.internal.protocol.BuildParametersVersion1;
import org.gradle.tooling.internal.protocol.ConnectionMetaDataVersion1;

/**
 * by Szczepan Faber, created at: 12/22/11
 */
public interface ConsumerConnection {

    void stop();

    ConnectionMetaDataVersion1 getMetaData();

    <T> T getModel(Class<T> type, BuildOperationParametersVersion1 operationParameters) throws UnsupportedOperationException, IllegalStateException;

    void executeBuild(BuildParametersVersion1 buildParameters, BuildOperationParametersVersion1 operationParameters) throws IllegalStateException;
}