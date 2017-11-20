/*
 * Copyright 2014-2017 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.aeron.archiver;

import io.aeron.ExclusivePublication;
import io.aeron.Publication;
import io.aeron.archiver.codecs.ControlResponseCode;
import io.aeron.archiver.codecs.RecordingDescriptorDecoder;
import io.aeron.logbuffer.ExclusiveBufferClaim;
import io.aeron.logbuffer.FrameDescriptor;
import io.aeron.protocol.DataHeaderFlyweight;
import org.agrona.CloseHelper;
import org.agrona.LangUtil;
import org.agrona.concurrent.EpochClock;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

import static io.aeron.logbuffer.FrameDescriptor.frameFlags;
import static io.aeron.logbuffer.FrameDescriptor.frameType;
import static io.aeron.protocol.DataHeaderFlyweight.RESERVED_VALUE_OFFSET;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * A replay session with a client which works through the required request response flow and streaming of recorded data.
 * The {@link ArchiveConductor} will initiate a session on receiving a ReplayRequest
 * (see {@link io.aeron.archiver.codecs.ReplayRequestDecoder}). The session will:
 * <ul>
 * <li>Validate request parameters and respond with appropriate error if unable to replay </li>
 * <li>Wait for replay subscription to connect to the requested replay publication. If no subscription appears within
 * LINGER_LENGTH_MS the session will terminate and respond will error.</li>
 * <li>Once the replay publication is connected send an OK response to control client</li>
 * <li>Stream recorded data into the replayPublication {@link ExclusivePublication}</li>
 * <li>If the replay is aborted part way through, send a ReplayAborted message and terminate.</li>
 * <li>Once replay is terminated the publication kept open for LINGER_LENGTH_MS then the session is closed.</li>
 * </ul>
 */
class ReplaySession implements Session
{
    enum State
    {
        INIT, REPLAY, LINGER, INACTIVE, CLOSED
    }

    static final long LINGER_LENGTH_MS = 1000;
    private static final int REPLAY_SEND_BATCH_SIZE = Integer.getInteger("io.aeron.archiver.replay.send.batch", 8);

    private final ExclusiveBufferClaim bufferClaim = new ExclusiveBufferClaim();
    private final RecordingFragmentReader.SimplifiedControlledPoll fragmentPoller = this::onFragment;

    private final long replaySessionId;
    private final long correlationId;
    private final Publication controlPublication;
    private final EpochClock epochClock;

    private final ExclusivePublication replayPublication;
    private final RecordingFragmentReader cursor;

    private ControlSessionProxy threadLocalControlSessionProxy;
    private State state = State.INIT;
    private long lingerSinceMs;

    ReplaySession(
        final long recordingId,
        final long replayPosition,
        final long replayLength,
        final ArchiveConductor.ReplayPublicationSupplier supplier,
        final Publication controlPublication,
        final File archiveDir,
        final ControlSessionProxy threadLocalControlSessionProxy,
        final long replaySessionId,
        final long correlationId,
        final EpochClock epochClock,
        final String replayChannel,
        final int replayStreamId,
        final ByteBuffer threadLocalMetaBuffer)
    {
        this.controlPublication = controlPublication;
        this.threadLocalControlSessionProxy = threadLocalControlSessionProxy;
        this.replaySessionId = replaySessionId;
        this.correlationId = correlationId;
        this.epochClock = epochClock;
        this.lingerSinceMs = epochClock.time();

        final String recordingMetaFileName = ArchiveUtil.recordingDescriptorFileName(recordingId);
        final File recordingMetaFile = new File(archiveDir, recordingMetaFileName);
        if (!recordingMetaFile.exists())
        {
            final String errorMessage = recordingMetaFile.getAbsolutePath() + " not found";
            closeOnError(new IllegalArgumentException(errorMessage), errorMessage);
        }

        RecordingDescriptorDecoder descriptorDecoder = null;
        try
        {
            descriptorDecoder = ArchiveUtil.loadRecordingDescriptor(recordingMetaFile, threadLocalMetaBuffer);
        }
        catch (final IOException ex)
        {
            closeOnError(ex, recordingMetaFile.getAbsolutePath() + " : failed to load");
        }

        Objects.requireNonNull(descriptorDecoder);
        final long joinPosition = descriptorDecoder.joinPosition();
        final int mtuLength = descriptorDecoder.mtuLength();
        final int termBufferLength = descriptorDecoder.termBufferLength();
        final int initialTermId = descriptorDecoder.initialTermId();

        if (replayPosition < joinPosition)
        {
            closeOnError(null, "Requested replay start position(=" + replayPosition +
                ") is less than recording join position(=" + joinPosition + ")");
        }

        RecordingFragmentReader cursor = null;
        try
        {
            cursor = new RecordingFragmentReader(
                descriptorDecoder.joinPosition(),
                descriptorDecoder.endPosition(),
                descriptorDecoder.termBufferLength(),
                descriptorDecoder.segmentFileLength(),
                recordingId,
                archiveDir,
                replayPosition,
                replayLength);
        }
        catch (final IOException ex)
        {
            closeOnError(ex, "Failed to open cursor for a recording");
        }

        Objects.requireNonNull(cursor);
        this.cursor = cursor;

        ExclusivePublication replayPublication = null;
        try
        {
            replayPublication = supplier.newReplayPublication(
                replayChannel,
                replayStreamId,
                cursor.fromPosition(), // may differ from replayPosition due to first fragment alignment
                mtuLength,
                initialTermId,
                termBufferLength);
        }
        catch (final Exception ex)
        {
            CloseHelper.quietClose(cursor);
            closeOnError(ex, "Failed to create replay publication");
        }

        this.replayPublication = replayPublication;
    }

    public void close()
    {
        if (state == State.CLOSED)
        {
            throw new IllegalStateException();
        }

        CloseHelper.quietClose(replayPublication);
        CloseHelper.quietClose(cursor);
        state = State.CLOSED;
    }

    public long sessionId()
    {
        return replaySessionId;
    }

    public int doWork()
    {
        int workDone = 0;
        if (state == State.REPLAY)
        {
            workDone += replay();
        }
        else if (state == State.INIT)
        {
            workDone += init();
        }
        else if (state == State.LINGER)
        {
            workDone += linger();
        }

        return workDone;
    }

    public void abort()
    {
        if (controlPublication.isConnected())
        {
            threadLocalControlSessionProxy.sendReplayAborted(
                correlationId,
                replaySessionId,
                replayPublication.position(),
                controlPublication);
        }

        state = State.INACTIVE;
    }

    public boolean isDone()
    {
        return state == State.INACTIVE;
    }

    State state()
    {
        return state;
    }

    void setThreadLocalControlSessionProxy(final ControlSessionProxy proxy)
    {
        threadLocalControlSessionProxy = proxy;
    }

    private int replay()
    {
        try
        {
            final int polled = cursor.controlledPoll(fragmentPoller, REPLAY_SEND_BATCH_SIZE);
            if (cursor.isDone())
            {
                lingerSinceMs = epochClock.time();
                state = State.LINGER;
            }

            return polled;
        }
        catch (final Exception ex)
        {
            return closeOnError(ex, "Cursor read failed");
        }
    }

    private boolean onFragment(final UnsafeBuffer termBuffer, final int offset, final int length)
    {
        if (isDone())
        {
            return false;
        }

        final int frameOffset = offset - DataHeaderFlyweight.HEADER_LENGTH;
        final int frameType = frameType(termBuffer, frameOffset);

        final long result = frameType == FrameDescriptor.PADDING_FRAME_TYPE ?
            replayPublication.appendPadding(length) :
            replayFrame(termBuffer, offset, length, frameOffset);

        if (result > 0)
        {
            return true;
        }
        else if (result == Publication.CLOSED || result == Publication.NOT_CONNECTED)
        {
            closeOnError(null, "Replay stream has been shutdown mid-replay");
        }

        return false;
    }

    private long replayFrame(final UnsafeBuffer termBuffer, final int offset, final int length, final int frameOffset)
    {
        final long result = replayPublication.tryClaim(length, bufferClaim);
        if (result > 0)
        {
            try
            {
                bufferClaim
                    .flags(frameFlags(termBuffer, frameOffset))
                    .reservedValue(termBuffer.getLong(frameOffset + RESERVED_VALUE_OFFSET, LITTLE_ENDIAN))
                    .buffer().putBytes(bufferClaim.offset(), termBuffer, offset, length);
            }
            finally
            {
                bufferClaim.commit();
            }
        }

        return result;
    }

    private int linger()
    {
        if (isLingerDone() || !replayPublication.isConnected())
        {
            state = State.INACTIVE;
        }
        return 0;
    }

    private boolean isLingerDone()
    {
        return epochClock.time() - LINGER_LENGTH_MS > lingerSinceMs;
    }

    private int init()
    {
        if (!replayPublication.isConnected())
        {
            if (isLingerDone())
            {
                return closeOnError(null, "No connection established for replay");
            }

            return 0;
        }

        threadLocalControlSessionProxy.sendOkResponse(correlationId, controlPublication);
        state = State.REPLAY;

        return 1;
    }

    private int closeOnError(final Throwable e, final String errorMessage)
    {
        this.state = State.INACTIVE;
        if (controlPublication.isConnected())
        {
            threadLocalControlSessionProxy.sendError(
                correlationId,
                ControlResponseCode.ERROR,
                errorMessage,
                controlPublication);
        }

        if (e != null)
        {
            LangUtil.rethrowUnchecked(e);
        }

        return 0;
    }

}