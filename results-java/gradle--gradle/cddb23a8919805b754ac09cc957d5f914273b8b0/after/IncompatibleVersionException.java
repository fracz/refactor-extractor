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

package org.gradle.tooling.model;

/**
 * Thrown when the tooling api client client attempts to use a method that does not exist
 * in the version of gradle the tooling api is connected to.
 * <p>
 * Typically, to resolve such problem you change/upgrade the target version of Gradle the tooling api is connected to.
 * Alternatively, you can handle and ignore this exception.
 */
public class IncompatibleVersionException extends UnsupportedOperationException {
    //TODO SF needs to be renamed because it overlaps with UnsupportedVersionException

    public IncompatibleVersionException(String s) {
        super(s);
    }

    public IncompatibleVersionException(String message, Throwable cause) {
        super(message, cause);
    }
}