/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.stomp;

import io.netty.handler.codec.DecoderResult;

/**
 * Defines a common interface for all {@link StompSubframe} implementations.
 */
public interface StompSubframe {
    /**
     * Returns the result of decoding this object.
     */
    DecoderResult decoderResult();

    /**
     * Updates the result of decoding this object. This method is supposed to be invoked by
     * {@link StompSubframeDecoder}. Do not call this method unless you know what you are doing.
     */
    void setDecoderResult(DecoderResult result);
}