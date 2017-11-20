/*
 * Copyright 2014-2017 Real Logic Ltd.
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
package io.aeron.archiver;

import io.aeron.*;
import org.agrona.*;

class ControlSession implements ArchiveConductor.Session, ControlRequestListener
{
    enum State
    {
        INIT, ACTIVE, INACTIVE, CLOSED
    }

    private final Image image;
    private final ControlSessionProxy proxy;
    private final ArchiveConductor conductor;
    private final ControlRequestAdapter adapter = new ControlRequestAdapter(this);
    private ExclusivePublication reply;
    private State state = State.INIT;

    ControlSession(final Image image, final ControlSessionProxy clientProxy, final ArchiveConductor conductor)
    {
        this.image = image;
        this.proxy = clientProxy;
        this.conductor = conductor;
    }

    public void abort()
    {
        state = State.INACTIVE;
    }

    public boolean isDone()
    {
        return state == State.CLOSED;
    }

    public void remove(final ArchiveConductor conductor)
    {
    }

    public int doWork()
    {
        switch (state)
        {
            case INIT:
                return waitForConnection();

            case ACTIVE:
                if (image.isClosed() || !reply.isConnected())
                {
                    state = State.INACTIVE;
                }
                else
                {
                    return image.poll(adapter, 16);
                }
                break;

            case INACTIVE:
                CloseHelper.quietClose(reply);
                state = State.CLOSED;
                break;

            case CLOSED:
                break;

            default:
                throw new IllegalStateException();
        }

        return 0;
    }

    private int waitForConnection()
    {
        if (reply == null)
        {
            try
            {
                image.poll(adapter, 1);
            }
            catch (final Exception e)
            {
                state = State.INACTIVE;
                LangUtil.rethrowUnchecked(e);
            }
        }
        else if (reply.isConnected())
        {
            state = State.ACTIVE;
        }
        // TODO: timeout

        return 0;
    }

    public void onConnect(final String channel, final int streamId)
    {
        if (state != State.INIT)
        {
            throw new IllegalStateException();
        }

        reply = conductor.clientConnect(channel, streamId);
    }

    public void onStopRecording(final long correlationId, final long recordingId)
    {
        if (state != State.ACTIVE)
        {
            throw new IllegalStateException();
        }

        try
        {
            conductor.stopRecording(recordingId);
            proxy.sendResponse(reply, null, correlationId);
        }
        catch (final Exception e)
        {
            proxy.sendResponse(reply, e.getMessage(), correlationId);
        }
    }

    public void onStartRecording(final long correlationId, final String channel, final int streamId)
    {
        if (state != State.ACTIVE)
        {
            throw new IllegalStateException();
        }

        try
        {
            conductor.startRecording(channel, streamId);
            proxy.sendResponse(reply, null, correlationId);
        }
        catch (final Exception e)
        {
            // e.printStackTrace(); TODO: logging?
            proxy.sendResponse(reply, e.getMessage(), correlationId);
        }
    }

    public void onListRecordings(final long correlationId, final long fromId, final long toId)
    {
        if (state != State.ACTIVE)
        {
            throw new IllegalStateException();
        }

        conductor.listRecordings(correlationId, reply, fromId, toId);
    }

    public void onAbortReplay(final long correlationId)
    {
        if (state != State.ACTIVE)
        {
            throw new IllegalStateException();
        }

        conductor.stopReplay(0);
    }

    public void onStartReplay(
        final long correlationId,
        final int replayStreamId,
        final String replayChannel,
        final long recordingId,
        final long position,
        final long length)
    {
        if (state != State.ACTIVE)
        {
            throw new IllegalStateException();
        }

        conductor.startReplay(
            correlationId,
            reply,
            replayStreamId,
            replayChannel,
            recordingId,
            position,
            length);
    }
}