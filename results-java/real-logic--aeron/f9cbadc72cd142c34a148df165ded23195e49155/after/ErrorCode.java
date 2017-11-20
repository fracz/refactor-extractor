/*
 * Copyright 2014 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.aeron.util.command;

/**
 * Error codes between mediadriver and library
 */
public enum ErrorCode
{
    GENERIC_ERROR(0);

    private final int value;

    ErrorCode(final int value)
    {
        this.value = value;
    }

    public int value()
    {
        return value;
    }

    public static ErrorCode get(final int value)
    {
        for (final ErrorCode code : Singleton.VALUES)
        {
            if (code.value == value)
            {
                return code;
            }
        }

        throw new IllegalArgumentException("no ErrorCode for value: " + value);
    }

    static class Singleton
    {
        public static final ErrorCode[] VALUES = ErrorCode.values();
    }
}