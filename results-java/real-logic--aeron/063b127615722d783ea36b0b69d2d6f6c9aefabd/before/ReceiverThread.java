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
package uk.co.real_logic.aeron.mediadriver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

/**
 * Event loop for JVM based mediadriver
 *
 * Contains Selector logic
 *
 * Does not provide timers
 */
public class ReceiverThread implements AutoCloseable, Runnable
{
    /** Default select timeout (20 msec) */
    public static final long DEFAULT_SELECT_TIMEOUT = 20; // TODO: should probably be a property in MediaDriver.

    private Selector selector;
    private volatile boolean done;
    private final ByteBuffer commandBuffer;
    private final ByteBuffer adminCommandBuffer;

    public ReceiverThread(final ByteBuffer commandBuffer, final ByteBuffer adminCommandBuffer) throws Exception
    {
        this.selector = Selector.open(); // yes, SelectorProvider, blah, blah
        this.commandBuffer = commandBuffer;
        this.adminCommandBuffer = adminCommandBuffer;
        this.done = false;
    }

    /**
     * Register channel for read.
     * @param channel to select for read
     * @param obj to associate with read
     * @return SelectionKey for registration for cancel
     * @throws Exception
     */
    public SelectionKey registerForRead(final SelectableChannel channel, final ReadHandler obj) throws Exception
    {
        final SelectionKey key = channel.register(selector, SelectionKey.OP_READ, obj);
        // now wake up if blocked
        wakeup();

        return key;
    }

    /**
     * Cancel pending reads for selection key.
     * @param key to cancel
     */
    public void cancelRead(final SelectionKey key)
    {
        key.cancel();
    }

    /**
     * Main loop of the ReceiverThread
     *
     * Everything is done here and bubbles up via the handlers.
     */
    public void run()
    {
        try
        {
            while (!done)
            {
                select();
                handleSelectedKeys();
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Close ReceiverThread down. Returns immediately.
     */
    public void close()
    {
        done = true;
        wakeup();
        // TODO: if needed, use a CountdownLatch to sync...
    }

    /**
     * Wake up ReceiverThread if blocked.
     */
    public void wakeup()
    {
        // TODO: add in control for state here. Only wakeup if actually blocked as it is usually expensive.
        selector.wakeup();
    }

    /**
     * Explicit event loop processing (Primarily for unit testing)
     */
    public void process()
    {
        try
        {
            selectNow();
            handleSelectedKeys();
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void select() throws Exception
    {
        int readyChannels = selector.select(DEFAULT_SELECT_TIMEOUT);
    }

    private void selectNow() throws IOException
    {
        int readyChannels = selector.selectNow();
    }

    private void handleReadable(final SelectionKey key)
    {
        try
        {
            ((ReadHandler)key.attachment()).onRead();
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    private void handleSelectedKeys() throws Exception
    {
        // Try this as we would like to be able to have the JVM optimize the Set<SelectionKey>
        // instead of instrumenting it.
        // Only have to handle readable at the moment. Will change if this is used with TCP.
        // Could filter based on key.attachment() being instanceof ReadHandler
        //        selector.selectedKeys().stream()
        //                .filter(SelectionKey::isReadable)
        //                .forEach(this::handleReadable);

        final Set<SelectionKey> selectedKeys = selector.selectedKeys();
        if (selectedKeys.isEmpty())
        {
            return;
        }

        final Iterator<SelectionKey> iter = selectedKeys.iterator();
        while (iter.hasNext())
        {
            final SelectionKey key = iter.next();
            if (key.isReadable())
            {
                handleReadable(key);
                iter.remove();
            }
        }
    }
}