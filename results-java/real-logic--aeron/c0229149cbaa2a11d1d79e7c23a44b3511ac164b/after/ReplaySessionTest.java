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

import io.aeron.ExclusivePublication;
import io.aeron.archiver.codecs.ControlResponseCode;
import io.aeron.logbuffer.*;
import io.aeron.protocol.DataHeaderFlyweight;
import org.agrona.*;
import org.agrona.concurrent.*;
import org.junit.*;
import org.mockito.Mockito;

import java.io.*;

import static io.aeron.archiver.TestUtil.makeTempDir;
import static io.aeron.protocol.DataHeaderFlyweight.HEADER_LENGTH;
import static io.aeron.protocol.HeaderFlyweight.HDR_TYPE_DATA;
import static io.aeron.protocol.HeaderFlyweight.HDR_TYPE_PAD;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReplaySessionTest
{
    private static final String REPLAY_CHANNEL = "aeron:ipc";
    private static final int REPLAY_STREAM_ID = 101;
    private static final int RECORDING_ID = 0;
    private static final int TERM_BUFFER_LENGTH = 4096 * 4;
    private static final int INITIAL_TERM_ID = 8231773;
    private static final int INITIAL_TERM_OFFSET = 1024;
    private static final long JOINING_POSITION = INITIAL_TERM_OFFSET;
    private static final long RECORDING_POSITION = INITIAL_TERM_OFFSET;
    private static final int MTU_LENGTH = 4096;
    private static final long TIME = 0;
    private static final int REPLAY_SESSION_ID = 0;
    private static final int FRAME_LENGTH = 1024;
    private File archiveDir;

    private int messageCounter = 0;
    private ControlSessionProxy proxy;
    private EpochClock epochClock;

    @Before
    public void before() throws Exception
    {
        archiveDir = makeTempDir();
        proxy = Mockito.mock(ControlSessionProxy.class);
        epochClock = mock(EpochClock.class);
        try (Recorder recorder = new Recorder.Builder()
            .archiveDir(archiveDir)
            .epochClock(epochClock)
            .recordingId(RECORDING_ID)
            .termBufferLength(TERM_BUFFER_LENGTH)
            .initialTermId(INITIAL_TERM_ID)
            .joiningPosition(JOINING_POSITION)
            .mtuLength(MTU_LENGTH)
            .sessionId(1)
            .streamId(1)
            .channel("channel")
            .sourceIdentity("sourceIdentity")
            .forceWrites(true)
            .forceMetadataUpdates(true)
            .build())
        {
            when(epochClock.time()).thenReturn(TIME);

            final UnsafeBuffer buffer = new UnsafeBuffer(BufferUtil.allocateDirectAligned(TERM_BUFFER_LENGTH, 64));
            buffer.setMemory(0, TERM_BUFFER_LENGTH, (byte)0);

            final DataHeaderFlyweight headerFwt = new DataHeaderFlyweight();
            final Header header = new Header(INITIAL_TERM_ID, Integer.numberOfLeadingZeros(TERM_BUFFER_LENGTH));
            header.buffer(buffer);

            recordFragment(recorder, buffer, headerFwt, header, 0, FrameDescriptor.UNFRAGMENTED, HDR_TYPE_DATA);
            recordFragment(recorder, buffer, headerFwt, header, 1, FrameDescriptor.BEGIN_FRAG_FLAG, HDR_TYPE_DATA);
            recordFragment(recorder, buffer, headerFwt, header, 2, FrameDescriptor.END_FRAG_FLAG, HDR_TYPE_DATA);
            recordFragment(recorder, buffer, headerFwt, header, 3, FrameDescriptor.UNFRAGMENTED, HDR_TYPE_PAD);
        }
    }

    @After
    public void after()
    {
        IoUtil.delete(archiveDir, false);
    }

    private void recordFragment(
        final Recorder recorder,
        final UnsafeBuffer buffer,
        final DataHeaderFlyweight headerFlyweight,
        final Header header,
        final int message,
        final byte flags,
        final int type)
    {
        final int offset = INITIAL_TERM_OFFSET + message * FRAME_LENGTH;
        headerFlyweight.wrap(buffer, offset, HEADER_LENGTH);
        headerFlyweight
            .termOffset(offset)
            .termId(INITIAL_TERM_ID)
            .reservedValue(message)
            .headerType(type)
            .flags(flags)
            .frameLength(FRAME_LENGTH);

        buffer.setMemory(
            offset + HEADER_LENGTH,
            FRAME_LENGTH - HEADER_LENGTH,
            (byte)message);

        header.offset(offset);

        recorder.writeFragment(
            buffer,
            header);
    }

    @Test
    public void verifyRecordingFile() throws IOException
    {
        // Verify file reader matches file writer
        try (RecordingFragmentReader reader = new RecordingFragmentReader(RECORDING_ID, archiveDir))
        {
            int polled = reader.controlledPoll(
                (buffer, offset, length, header) ->
                {
                    assertEquals(offset, INITIAL_TERM_OFFSET + HEADER_LENGTH);
                    assertEquals(length, FRAME_LENGTH - HEADER_LENGTH);
                    assertEquals(header.headerType(), HDR_TYPE_DATA);
                    assertEquals((byte)header.flags(), FrameDescriptor.UNFRAGMENTED);

                    return true;
                },
                1);

            assertEquals(1, polled);
            polled = reader.controlledPoll(
                (buffer, offset, length, header) ->
                {
                    assertEquals(offset, INITIAL_TERM_OFFSET + FRAME_LENGTH + HEADER_LENGTH);
                    assertEquals(length, FRAME_LENGTH - HEADER_LENGTH);
                    assertEquals(header.headerType(), HDR_TYPE_DATA);
                    assertEquals((byte)header.flags(), FrameDescriptor.BEGIN_FRAG_FLAG);

                    return true;
                },
                1);

            assertEquals(1, polled);
            polled = reader.controlledPoll(
                (buffer, offset, length, header) ->
                {
                    assertEquals(offset, INITIAL_TERM_OFFSET + 2 * FRAME_LENGTH + HEADER_LENGTH);
                    assertEquals(length, FRAME_LENGTH - HEADER_LENGTH);
                    assertEquals(header.headerType(), HDR_TYPE_DATA);
                    assertEquals((byte)header.flags(), FrameDescriptor.END_FRAG_FLAG);

                    return true;
                },
                1);

            assertEquals(1, polled);
            polled = reader.controlledPoll(
                (buffer, offset, length, header) ->
                {
                    assertEquals(offset, INITIAL_TERM_OFFSET + 3 * FRAME_LENGTH + HEADER_LENGTH);
                    assertEquals(length, FRAME_LENGTH - HEADER_LENGTH);
                    assertEquals(header.headerType(), HDR_TYPE_PAD);
                    assertEquals((byte)header.flags(), FrameDescriptor.UNFRAGMENTED);

                    return true;
                },
                1);

            assertEquals(0, polled);
        }
    }

    @Test
    public void shouldReplayPartialDataFromFile()
    {
        final long correlationId = 1L;
        final ExclusivePublication replay = Mockito.mock(ExclusivePublication.class);
        final ExclusivePublication control = Mockito.mock(ExclusivePublication.class);

        final ArchiveConductor conductor = Mockito.mock(ArchiveConductor.class);

        final ReplaySession replaySession = replaySession(
            RECORDING_ID, RECORDING_POSITION, FRAME_LENGTH, correlationId, replay, control, conductor);

        when(control.isClosed()).thenReturn(false);
        when(control.isConnected()).thenReturn(true);

        when(replay.isClosed()).thenReturn(false);
        when(replay.isConnected()).thenReturn(false);

        replaySession.doWork();

        assertEquals(replaySession.state(), ReplaySession.State.INIT);

        when(replay.isConnected()).thenReturn(true);
        when(control.isConnected()).thenReturn(true);

        replaySession.doWork();
        assertEquals(replaySession.state(), ReplaySession.State.REPLAY);

        verify(proxy, times(1)).sendOkResponse(control, correlationId);
        verify(conductor).newReplayPublication(
            REPLAY_CHANNEL,
            REPLAY_STREAM_ID,
            RECORDING_POSITION,
            MTU_LENGTH,
            INITIAL_TERM_ID,
            TERM_BUFFER_LENGTH);

        final UnsafeBuffer termBuffer = new UnsafeBuffer(BufferUtil.allocateDirectAligned(4096, 64));
        mockTryClaim(replay, termBuffer);
        assertNotEquals(0, replaySession.doWork());
        assertThat(messageCounter, is(1));

        validateFrame(termBuffer, 0, FrameDescriptor.UNFRAGMENTED, HDR_TYPE_DATA);

        assertFalse(replaySession.isDone());

        when(epochClock.time()).thenReturn(ReplaySession.LINGER_LENGTH_MS + TIME + 1L);
        replaySession.doWork();
        assertTrue(replaySession.isDone());
    }

    @Test
    public void shouldReplayPartialUnalignedDataFromFile()
    {
        final long correlationId = 1L;
        final ExclusivePublication replay = Mockito.mock(ExclusivePublication.class);
        final ExclusivePublication control = Mockito.mock(ExclusivePublication.class);

        final ArchiveConductor conductor = Mockito.mock(ArchiveConductor.class);

        final ReplaySession replaySession = replaySession(
            RECORDING_ID, RECORDING_POSITION + 1, FRAME_LENGTH, correlationId, replay, control, conductor);

        when(conductor.newReplayPublication(
            eq(REPLAY_CHANNEL),
            eq(REPLAY_STREAM_ID),
            eq(RECORDING_POSITION + FRAME_LENGTH),
            eq(MTU_LENGTH),
            eq(INITIAL_TERM_ID),
            eq(TERM_BUFFER_LENGTH))).thenReturn(replay);

        when(replay.isClosed()).thenReturn(false);
        when(control.isClosed()).thenReturn(false);

        when(replay.isConnected()).thenReturn(true);
        when(control.isConnected()).thenReturn(true);

        replaySession.doWork();

        replaySession.doWork();
        assertEquals(replaySession.state(), ReplaySession.State.REPLAY);

        verify(proxy, times(1)).sendOkResponse(control, correlationId);
        verify(conductor).newReplayPublication(
            REPLAY_CHANNEL,
            REPLAY_STREAM_ID,
            RECORDING_POSITION + FRAME_LENGTH,
            MTU_LENGTH,
            INITIAL_TERM_ID,
            TERM_BUFFER_LENGTH);

        final UnsafeBuffer termBuffer = new UnsafeBuffer(BufferUtil.allocateDirectAligned(4096, 64));
        mockTryClaim(replay, termBuffer);
        assertNotEquals(0, replaySession.doWork());
        assertThat(messageCounter, is(1));

        assertEquals(FRAME_LENGTH, termBuffer.getInt(DataHeaderFlyweight.FRAME_LENGTH_FIELD_OFFSET));
        assertEquals(FrameDescriptor.BEGIN_FRAG_FLAG, termBuffer.getByte(DataHeaderFlyweight.FLAGS_FIELD_OFFSET));
        assertEquals(1, termBuffer.getLong(DataHeaderFlyweight.RESERVED_VALUE_OFFSET));
        assertEquals(HDR_TYPE_DATA, termBuffer.getInt(DataHeaderFlyweight.TYPE_FIELD_OFFSET));
        assertEquals(1, termBuffer.getByte(DataHeaderFlyweight.HEADER_LENGTH));

        final int expectedFrameLength = 1024;
        assertEquals(expectedFrameLength, termBuffer.getInt(0));
        assertFalse(replaySession.isDone());

        when(epochClock.time()).thenReturn(ReplaySession.LINGER_LENGTH_MS + TIME + 1L);
        replaySession.doWork();
        assertTrue(replaySession.isDone());
    }

    @Test
    public void shouldReplayFullDataFromFile()
    {
        final long length = 4 * FRAME_LENGTH;
        final long correlationId = 1L;
        final ExclusivePublication replay = Mockito.mock(ExclusivePublication.class);
        final ExclusivePublication control = Mockito.mock(ExclusivePublication.class);

        final ArchiveConductor conductor = Mockito.mock(ArchiveConductor.class);

        final ReplaySession replaySession = replaySession(
            RECORDING_ID, RECORDING_POSITION, length, correlationId, replay, control, conductor);

        when(control.isClosed()).thenReturn(false);
        when(control.isConnected()).thenReturn(true);

        when(replay.isClosed()).thenReturn(false);
        when(replay.isConnected()).thenReturn(false);

        replaySession.doWork();

        assertEquals(replaySession.state(), ReplaySession.State.INIT);

        when(replay.isConnected()).thenReturn(true);
        when(control.isConnected()).thenReturn(true);

        replaySession.doWork();
        assertEquals(replaySession.state(), ReplaySession.State.REPLAY);

        verify(proxy, times(1)).sendOkResponse(control, correlationId);
        verify(conductor).newReplayPublication(
            REPLAY_CHANNEL,
            REPLAY_STREAM_ID,
            RECORDING_POSITION,
            MTU_LENGTH,
            INITIAL_TERM_ID,
            TERM_BUFFER_LENGTH);

        final UnsafeBuffer termBuffer = new UnsafeBuffer(BufferUtil.allocateDirectAligned(4096, 64));
        mockTryClaim(replay, termBuffer);

        assertNotEquals(0, replaySession.doWork());
        assertThat(messageCounter, is(4));

        validateFrame(termBuffer, 0, FrameDescriptor.UNFRAGMENTED, HDR_TYPE_DATA);
        validateFrame(termBuffer, 1, FrameDescriptor.BEGIN_FRAG_FLAG, HDR_TYPE_DATA);
        validateFrame(termBuffer, 2, FrameDescriptor.END_FRAG_FLAG, HDR_TYPE_DATA);
        validateFrame(termBuffer, 3, FrameDescriptor.UNFRAGMENTED, HDR_TYPE_PAD);

        assertFalse(replaySession.isDone());

        when(epochClock.time()).thenReturn(ReplaySession.LINGER_LENGTH_MS + TIME + 1L);
        replaySession.doWork();
        assertTrue(replaySession.isDone());
    }

    @Test
    public void shouldAbortReplay()
    {
        final long length = 1024L;
        final long correlationId = 1L;
        final ExclusivePublication replay = Mockito.mock(ExclusivePublication.class);
        final ExclusivePublication control = Mockito.mock(ExclusivePublication.class);

        final ArchiveConductor conductor = Mockito.mock(ArchiveConductor.class);

        final ReplaySession replaySession = replaySession(
            RECORDING_ID, RECORDING_POSITION, length, correlationId, replay, control, conductor);

        when(control.isClosed()).thenReturn(false);
        when(replay.isClosed()).thenReturn(false);
        when(replay.isConnected()).thenReturn(true);
        when(control.isConnected()).thenReturn(true);

        replaySession.doWork();

        verify(proxy, times(1)).sendOkResponse(control, correlationId);
        assertEquals(replaySession.state(), ReplaySession.State.REPLAY);

        replaySession.abort();
        assertEquals(replaySession.state(), ReplaySession.State.INACTIVE);
        verify(proxy, times(1)).sendReplayAborted(control, correlationId, REPLAY_SESSION_ID, replay.position());

        replaySession.doWork();
        assertTrue(replaySession.isDone());
    }

    @Test
    public void shouldFailToReplayDataForNonExistentStream()
    {
        final long length = 1024L;
        final long correlationId = 1L;
        final ExclusivePublication replay = Mockito.mock(ExclusivePublication.class);
        final ExclusivePublication control = Mockito.mock(ExclusivePublication.class);

        final ArchiveConductor conductor = Mockito.mock(ArchiveConductor.class);
        final ReplaySession replaySession = replaySession(
            RECORDING_ID + 1, RECORDING_POSITION, length, correlationId, replay, control, conductor);

        when(replay.isClosed()).thenReturn(false);
        when(control.isClosed()).thenReturn(false);

        when(replay.isConnected()).thenReturn(true);
        when(control.isConnected()).thenReturn(true);

        assertEquals(1, replaySession.doWork());

        verify(proxy, times(1))
            .sendError(eq(control), eq(ControlResponseCode.ERROR), notNull(), eq(correlationId));

        assertTrue(replaySession.isDone());
    }

    @Test
    public void shouldGiveUpIfPublishersAreNotConnectedAfterOneSecond()
    {
        final long length = 1024L;
        final long correlationId = 1L;
        final ExclusivePublication replay = Mockito.mock(ExclusivePublication.class);
        final ExclusivePublication control = Mockito.mock(ExclusivePublication.class);

        final ArchiveConductor conductor = Mockito.mock(ArchiveConductor.class);
        final ReplaySession replaySession = replaySession(
            RECORDING_ID, RECORDING_POSITION, length, correlationId, replay, control, conductor);

        when(replay.isClosed()).thenReturn(false);
        when(control.isClosed()).thenReturn(false);
        when(replay.isConnected()).thenReturn(false);

        // does not switch to replay mode until BOTH publications are established
        replaySession.doWork();

        when(epochClock.time()).thenReturn(ReplaySession.LINGER_LENGTH_MS + TIME + 1L);
        replaySession.doWork();
        assertTrue(replaySession.isDone());
    }

    private ReplaySession replaySession(
        final long recordingId,
        final long recordingPosition, final long length,
        final long correlationId,
        final ExclusivePublication replay,
        final ExclusivePublication control,
        final ArchiveConductor conductor)
    {
        when(conductor.newReplayPublication(
            eq(REPLAY_CHANNEL),
            eq(REPLAY_STREAM_ID),
            eq(recordingPosition),
            eq(MTU_LENGTH),
            eq(INITIAL_TERM_ID),
            eq(TERM_BUFFER_LENGTH))).thenReturn(replay);

        return new ReplaySession(
            recordingId,
            recordingPosition,
            length,
            conductor,
            control,
            archiveDir,
            proxy,
            REPLAY_SESSION_ID,
            correlationId,
            epochClock,
            REPLAY_CHANNEL,
            REPLAY_STREAM_ID);
    }

    private void validateFrame(
        final UnsafeBuffer buffer,
        final int message,
        final byte flags,
        final int type)
    {
        final int offset = message * FRAME_LENGTH;
        assertEquals(FRAME_LENGTH, buffer.getInt(offset + DataHeaderFlyweight.FRAME_LENGTH_FIELD_OFFSET));
        assertEquals(flags, buffer.getByte(offset + DataHeaderFlyweight.FLAGS_FIELD_OFFSET));
        assertEquals(message, buffer.getLong(offset + DataHeaderFlyweight.RESERVED_VALUE_OFFSET));
        assertEquals(type, buffer.getInt(offset + DataHeaderFlyweight.TYPE_FIELD_OFFSET));
        assertEquals(message, buffer.getByte(offset + DataHeaderFlyweight.HEADER_LENGTH));
    }

    private void mockTryClaim(final ExclusivePublication replay, final UnsafeBuffer mockTermBuffer)
    {
        when(replay.tryClaim(anyInt(), any(ExclusiveBufferClaim.class))).then(
            (invocation) ->
            {
                final int claimedSize = invocation.getArgument(0);
                final ExclusiveBufferClaim buffer = invocation.getArgument(1);
                buffer.wrap(mockTermBuffer, messageCounter * FRAME_LENGTH, claimedSize + HEADER_LENGTH);
                messageCounter++;

                return (long)claimedSize;
            });
    }
}