/*
 * Copyright 2011 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.netty.channel.socket.nio;

import io.netty.channel.AbstractChannelSink;
import io.netty.channel.Channel;
import io.netty.channel.ChannelEvent;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.ChannelRunnableWrapper;

public abstract class AbstractNioChannelSink extends AbstractChannelSink {

    @Override
    public ChannelFuture execute(ChannelPipeline pipeline, final Runnable task) {
        Channel ch = pipeline.channel();
        if (ch instanceof AbstractNioChannel) {
            AbstractNioChannel channel = (AbstractNioChannel) ch;
            ChannelRunnableWrapper wrapper = new ChannelRunnableWrapper(pipeline.channel(), task);
            channel.getWorker().executeInIoThread(wrapper);
            return wrapper;
        }
        return super.execute(pipeline, task);


    }

    @Override
    protected boolean isFireExceptionCaughtLater(ChannelEvent event, Throwable actualCause) {
        Channel channel = event.getChannel();
        boolean fireLater = false;
        if (channel instanceof AbstractNioChannel) {
            fireLater = !((AbstractNioChannel) channel).getWorker().isIoThread();
        }
        return fireLater;
    }

}