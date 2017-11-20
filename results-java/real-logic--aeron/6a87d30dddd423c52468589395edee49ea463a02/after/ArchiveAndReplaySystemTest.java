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
import io.aeron.archiver.messages.*;
import io.aeron.driver.*;
import io.aeron.logbuffer.*;
import io.aeron.protocol.*;
import org.agrona.*;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.*;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

import static io.aeron.archiver.ArchiveFileUtil.archiveMetaFileFormatDecoder;
import static io.aeron.archiver.ArchiveFileUtil.archiveMetaFileName;
import static io.aeron.logbuffer.FrameDescriptor.FRAME_ALIGNMENT;
import static org.junit.Assert.fail;

public class ArchiveAndReplaySystemTest
{
    private static final int TIMEOUT = 5000;
    private static final boolean DEBUG = false;
    private static final String REPLY_URI = "aeron:udp?endpoint=127.0.0.1:54327";
    private static final String REPLAY_URI = "aeron:udp?endpoint=127.0.0.1:54326";
    private static final String PUBLISH_URI = "aeron:udp?endpoint=127.0.0.1:54325";
    private static final int PUBLISH_STREAM_ID = 1;
    private static final int MAX_FRAGMENT_SIZE = 1024;
    private static final double MEGABYTE = (1024.0 * 1024.0);
    private final MediaDriver.Context driverCtx = new MediaDriver.Context();
    private final Archiver.Context archiverCtx = new Archiver.Context();
    private Aeron publishingClient;
    private Archiver archiver;
    private MediaDriver driver;
    private UnsafeBuffer buffer = new UnsafeBuffer(new byte[4096]);
    private File archiveFolder;
    private int streamInstanceId;
    private String source;
    private long remaining;
    private int nextFragmentOffset;
    private int fragmentCount;
    private int[] fragmentLength;
    private long totalDataLength;
    private long totalArchiveLength;
    private long archived;
    private int initialTermOffset;
    private int lastTermOffset;
    private volatile int lastTermId = -1;
    private Throwable trackerError;
    private Random rnd = new Random();
    private long seed;
    @Rule
    public TestWatcher ruleExample = new TestWatcher()
    {
        protected void failed(final Throwable e, final Description description)
        {
            System.err.println(
                "ArchiveAndReplaySystemTest failed with random seed:" + ArchiveAndReplaySystemTest.this.seed);
        }
    };
    private int replyStreamId = 100;

    @Before
    public void setUp() throws Exception
    {
        seed = System.nanoTime();
        rnd.setSeed(seed);

        final int modes = ThreadingMode.values().length;
        final ThreadingMode threadingMode = ThreadingMode.values()[rnd.nextInt(modes)];
        driverCtx.threadingMode(threadingMode);
        println("ThreadingMode: " + threadingMode);

        driverCtx.errorHandler(LangUtil::rethrowUnchecked);
        driver = MediaDriver.launch(driverCtx);
        archiveFolder = TestUtil.makeTempFolder();
        archiverCtx.archiveFolder(archiveFolder);
        archiver = Archiver.launch(archiverCtx);
        println("Archiver started, folder: " + archiverCtx.archiveFolder().getAbsolutePath());
        publishingClient = Aeron.connect();

    }

    @After
    public void closeEverything() throws Exception
    {
        CloseHelper.quietClose(publishingClient);
        CloseHelper.quietClose(archiver);
        CloseHelper.quietClose(driver);

        IoUtil.delete(archiveFolder, false);
        driverCtx.deleteAeronDirectory();
    }

    private void requestArchive(final Publication archiverServiceRequest, final String channel, final int streamId)
    {
        new MessageHeaderEncoder()
            .wrap(buffer, 0)
            .templateId(ArchiveStartRequestEncoder.TEMPLATE_ID)
            .blockLength(ArchiveStartRequestEncoder.BLOCK_LENGTH)
            .schemaId(ArchiveStartRequestEncoder.SCHEMA_ID)
            .version(ArchiveStartRequestEncoder.SCHEMA_VERSION);

        final ArchiveStartRequestEncoder encoder = new ArchiveStartRequestEncoder()
            .wrap(buffer, MessageHeaderEncoder.ENCODED_LENGTH)
            .channel(channel)
            .streamId(streamId);

        offer(
            archiverServiceRequest,
            buffer,
            0,
            encoder.encodedLength() + MessageHeaderEncoder.ENCODED_LENGTH,
            TIMEOUT);
    }

    private void requestArchiveStop(final Publication archiverServiceRequest, final String channel, final int streamId)
    {
        new MessageHeaderEncoder()
            .wrap(buffer, 0)
            .templateId(ArchiveStopRequestEncoder.TEMPLATE_ID)
            .blockLength(ArchiveStopRequestEncoder.BLOCK_LENGTH)
            .schemaId(ArchiveStopRequestEncoder.SCHEMA_ID)
            .version(ArchiveStopRequestEncoder.SCHEMA_VERSION);

        final ArchiveStopRequestEncoder encoder = new ArchiveStopRequestEncoder()
            .wrap(buffer, MessageHeaderEncoder.ENCODED_LENGTH)
            .channel(channel)
            .streamId(streamId);

        offer(
            archiverServiceRequest,
            buffer,
            0,
            encoder.encodedLength() + MessageHeaderEncoder.ENCODED_LENGTH,
            TIMEOUT);
    }

    private void requestReplay(
        final Publication archiverServiceRequest,
        final int streamInstanceId,
        final int termId,
        final int termOffset,
        final long length,
        final String replyChannel,
        final int replyStreamId)
    {
        new MessageHeaderEncoder()
            .wrap(buffer, 0)
            .templateId(ReplayRequestEncoder.TEMPLATE_ID)
            .blockLength(ReplayRequestEncoder.BLOCK_LENGTH)
            .schemaId(ReplayRequestEncoder.SCHEMA_ID)
            .version(ReplayRequestEncoder.SCHEMA_VERSION);

        final ReplayRequestEncoder encoder = new ReplayRequestEncoder()
            .wrap(buffer, MessageHeaderEncoder.ENCODED_LENGTH)
            .streamInstanceId(streamInstanceId)
            .termId(termId)
            .termOffset(termOffset)
            .length((int) length)
            .replyChannel(replyChannel)
            .replyStreamId(replyStreamId);

        println(encoder.toString());
        offer(archiverServiceRequest,
              buffer,
              0,
              encoder.encodedLength() + MessageHeaderEncoder.ENCODED_LENGTH,
              TIMEOUT);
    }

    private void requestArchiveList(
        final Publication archiverServiceRequest,
        final String replyChannel,
        final int replyStreamId,
        final int from,
        final int to)
    {
        new MessageHeaderEncoder()
            .wrap(buffer, 0)
            .templateId(ListStreamInstancesRequestEncoder.TEMPLATE_ID)
            .blockLength(ListStreamInstancesRequestEncoder.BLOCK_LENGTH)
            .schemaId(ListStreamInstancesRequestEncoder.SCHEMA_ID)
            .version(ListStreamInstancesRequestEncoder.SCHEMA_VERSION);

        final ListStreamInstancesRequestEncoder encoder = new ListStreamInstancesRequestEncoder()
            .wrap(buffer, MessageHeaderEncoder.ENCODED_LENGTH)
            .replyStreamId(replyStreamId)
            .from(from)
            .to(to)
            .replyChannel(replyChannel);
        println(encoder.toString());
        offer(archiverServiceRequest,
              buffer,
              0,
              encoder.encodedLength() + MessageHeaderEncoder.ENCODED_LENGTH,
              TIMEOUT);
    }

    @Test(timeout = 60000)
    public void archiveAndReplay() throws IOException, InterruptedException
    {
        try (Publication archiverServiceRequest = publishingClient.addPublication(
            archiverCtx.serviceRequestChannel(), archiverCtx.serviceRequestStreamId());
             Subscription archiverNotifications = publishingClient.addSubscription(
                 archiverCtx.archiverNotificationsChannel(), archiverCtx.archiverNotificationsStreamId()))
        {

            awaitPublicationIsConnected(archiverServiceRequest, TIMEOUT);
            awaitSubscriptionIsConnected(archiverNotifications, TIMEOUT);
            println("Archive service connected");

            verifyEmptyDescriptorList(archiverServiceRequest);

            requestArchive(archiverServiceRequest, PUBLISH_URI, PUBLISH_STREAM_ID);

            println("Archive requested");

            final Publication publication = publishingClient.addPublication(PUBLISH_URI, PUBLISH_STREAM_ID);
            awaitPublicationIsConnected(publication, TIMEOUT);

            awaitArchiveForPublicationStartedNotification(archiverNotifications, publication);

            verifyDescriptorListOngoingArchive(archiverServiceRequest, publication, 0);
            final int messageCount = prepAndSendMessages(archiverNotifications, publication);
            verifyDescriptorListOngoingArchive(archiverServiceRequest, publication, totalArchiveLength);

            Assert.assertNull(trackerError);
            println("All data arrived");

            println("Request stop archive");
            requestArchiveStop(archiverServiceRequest, PUBLISH_URI, PUBLISH_STREAM_ID);
            awaitArchiveStoppedNotification(archiverNotifications);

            verifyDescriptorListOngoingArchive(archiverServiceRequest, publication, totalArchiveLength);

            println("Stream instance id: " + streamInstanceId);
            println("Meta data file printout: ");

            validateMetaDataFile(publication);
            validateArchiveFile(messageCount, streamInstanceId);
            validateArchiveFileChunked(messageCount, streamInstanceId);
            validateReplay(archiverServiceRequest, publication, messageCount);
        }
    }

    private void verifyEmptyDescriptorList(final Publication archiverServiceRequest)
    {
        final int replyStreamId = this.replyStreamId++;
        try (Subscription archiverListReply =
                 publishingClient.addSubscription(REPLY_URI, replyStreamId))
        {
            requestArchiveList(archiverServiceRequest, REPLY_URI, replyStreamId, 0, 100);
            awaitSubscriptionIsConnected(archiverListReply, TIMEOUT);
            poll(archiverListReply, (b, offset, length, header) ->
            {
                final MessageHeaderDecoder hDecoder = new MessageHeaderDecoder().wrap(b, offset);
                Assert.assertEquals(ArchiverResponseDecoder.TEMPLATE_ID, hDecoder.templateId());
            }, 1, TIMEOUT);
        }
    }

    private void verifyDescriptorListOngoingArchive(
        final Publication archiverServiceRequest,
        final Publication publication,
        final long archiveLength)
    {
        final int replyStreamId = this.replyStreamId++;
        try (Subscription archiverListReply =
                 publishingClient.addSubscription(REPLY_URI, replyStreamId))
        {
            requestArchiveList(archiverServiceRequest, REPLY_URI, replyStreamId, streamInstanceId, streamInstanceId);
            awaitSubscriptionIsConnected(archiverListReply, TIMEOUT);
            println("Await result");
            poll(archiverListReply, (b, offset, length, header) ->
            {
                final MessageHeaderDecoder hDecoder = new MessageHeaderDecoder().wrap(b, offset);
                Assert.assertEquals(ArchiveDescriptorDecoder.TEMPLATE_ID, hDecoder.templateId());
                final ArchiveDescriptorDecoder decoder = new ArchiveDescriptorDecoder();
                decoder.wrap(b,
                             offset + MessageHeaderDecoder.ENCODED_LENGTH,
                             hDecoder.blockLength(),
                             hDecoder.version());
                Assert.assertEquals(streamInstanceId, decoder.streamInstanceId());
                Assert.assertEquals(PUBLISH_STREAM_ID, decoder.streamId());
                Assert.assertEquals(publication.initialTermId(), decoder.imageInitialTermId());
                final long archiveFullLength = ArchiveFileUtil.archiveFullLength(decoder);
                Assert.assertEquals(archiveFullLength, archiveLength);
                //....
            }, 1, TIMEOUT);
        }
    }

    private int prepAndSendMessages(final Subscription archiverNotifications, final Publication publication)
        throws InterruptedException
    {
        final int messageCount = 128 + rnd.nextInt(10000);
        fragmentLength = new int[messageCount];
        for (int i = 0; i < messageCount; i++)
        {
            final int messageLength = 64 + rnd.nextInt(MAX_FRAGMENT_SIZE - 64) - DataHeaderFlyweight.HEADER_LENGTH;
            fragmentLength[i] = messageLength + DataHeaderFlyweight.HEADER_LENGTH;
            totalDataLength += fragmentLength[i];
        }
        final CountDownLatch waitForData = new CountDownLatch(1);
        printf("Sending %d messages, total length=%d %n", messageCount, totalDataLength);

        trackArchiveProgress(publication, archiverNotifications, waitForData);
        publishDataToBeArchived(publication, messageCount);
        waitForData.await();
        return messageCount;
    }

    private void validateMetaDataFile(final Publication publication) throws IOException
    {
        final File metaFile = new File(archiveFolder, archiveMetaFileName(streamInstanceId));
        Assert.assertTrue(metaFile.exists());

        if (DEBUG)
        {
            ArchiveFileUtil.printMetaFile(metaFile);
        }
        final ArchiveDescriptorDecoder decoder =
            archiveMetaFileFormatDecoder(new File(archiveFolder, archiveMetaFileName(streamInstanceId)));
        Assert.assertEquals(publication.initialTermId(), decoder.initialTermId());
        Assert.assertEquals(publication.sessionId(), decoder.sessionId());
        Assert.assertEquals(publication.streamId(), decoder.streamId());
        Assert.assertEquals(publication.termBufferLength(), decoder.termBufferLength());

        Assert.assertEquals(totalArchiveLength, ArchiveFileUtil.archiveFullLength(decoder));
        // length might exceed data sent due to padding
        Assert.assertTrue(totalDataLength + " <= " + totalArchiveLength, totalDataLength <= totalArchiveLength);


        IoUtil.unmap(decoder.buffer().byteBuffer());
    }

    private void awaitArchiveForPublicationStartedNotification(
        final Subscription archiverNotifications,
        final Publication publication)
    {
        // the archiver has subscribed to the publication, now we wait for the archive start message
        poll(archiverNotifications, (buffer, offset, length, header) ->
        {
            final MessageHeaderDecoder hDecoder = new MessageHeaderDecoder().wrap(buffer, offset);
            Assert.assertEquals(ArchiveStartedNotificationDecoder.TEMPLATE_ID, hDecoder.templateId());

            final ArchiveStartedNotificationDecoder mDecoder = new ArchiveStartedNotificationDecoder()
                .wrap(
                    buffer,
                    offset + MessageHeaderDecoder.ENCODED_LENGTH,
                    hDecoder.blockLength(),
                    hDecoder.version());

            streamInstanceId = mDecoder.streamInstanceId();
            Assert.assertEquals(mDecoder.streamId(), PUBLISH_STREAM_ID);
            Assert.assertEquals(mDecoder.sessionId(), publication.sessionId());
            source = mDecoder.source();
            final String channel = mDecoder.channel();
            Assert.assertEquals(channel, PUBLISH_URI);
            println("Archive started. source: " + source);
        }, 1, TIMEOUT);
    }

    private void awaitArchiveStoppedNotification(final Subscription archiverNotifications)
    {
        poll(archiverNotifications, (buffer, offset, length, header) ->
        {
            final MessageHeaderDecoder hDecoder = new MessageHeaderDecoder().wrap(buffer, offset);
            Assert.assertEquals(ArchiveStoppedNotificationDecoder.TEMPLATE_ID, hDecoder.templateId());

            final ArchiveStoppedNotificationDecoder mDecoder = new ArchiveStoppedNotificationDecoder()
                .wrap(
                    buffer,
                    offset + MessageHeaderDecoder.ENCODED_LENGTH,
                    hDecoder.blockLength(),
                    hDecoder.version());

            Assert.assertEquals(mDecoder.streamInstanceId(), streamInstanceId);
        }, 1, TIMEOUT);

        println("Archive stopped");
    }

    private void publishDataToBeArchived(final Publication publication, final int messageCount)
    {
        final int positionBitsToShift = Integer.numberOfTrailingZeros(publication.termBufferLength());
        final long initialPosition = publication.position();
        initialTermOffset = LogBufferDescriptor.computeTermOffsetFromPosition(
            initialPosition, positionBitsToShift);
        // clear out the buffer we write
        for (int i = 0; i < 1024; i++)
        {
            buffer.putByte(i, (byte) 'z');
        }
        buffer.putStringAscii(32, "TEST");

        for (int i = 0; i < messageCount; i++)
        {
            final int dataLength = fragmentLength[i] - DataHeaderFlyweight.HEADER_LENGTH;
            buffer.putInt(0, i);
            printf("Sending: index=%d length=%d %n", i, dataLength);
            offer(publication, buffer, 0, dataLength, TIMEOUT);
        }
        lastTermOffset = LogBufferDescriptor.computeTermOffsetFromPosition(publication.position(),
                                                                           positionBitsToShift);
        final int termIdFromPosition =
            LogBufferDescriptor.computeTermIdFromPosition(
                publication.position(), positionBitsToShift, publication.initialTermId());
        totalArchiveLength = (termIdFromPosition - publication.initialTermId()) * publication.termBufferLength() +
                             (lastTermOffset - initialTermOffset);
        Assert.assertEquals(totalArchiveLength, publication.position() - initialPosition);
        lastTermId = termIdFromPosition;
    }

    private void validateReplay(
        final Publication archiverServiceRequest,
        final Publication publication,
        final int messageCount)
    {
        // request replay
        final int replyStreamId = this.replyStreamId++;
        requestReplay(
            archiverServiceRequest,
            streamInstanceId,
            publication.initialTermId(),
            0,
            totalArchiveLength,
            REPLAY_URI,
            replyStreamId);

        try (Subscription replay = publishingClient.addSubscription(REPLAY_URI, replyStreamId))
        {
            awaitSubscriptionIsConnected(replay, TIMEOUT);
            poll(replay, (buffer, offset, length, header) ->
            {
                final MessageHeaderDecoder hDecoder = new MessageHeaderDecoder().wrap(buffer, offset);
                Assert.assertEquals(ArchiverResponseDecoder.TEMPLATE_ID, hDecoder.templateId());

                final ArchiverResponseDecoder mDecoder = new ArchiverResponseDecoder()
                    .wrap(
                        buffer,
                        offset + MessageHeaderDecoder.ENCODED_LENGTH,
                        hDecoder.blockLength(),
                        hDecoder.version());
                Assert.assertEquals(mDecoder.err(), "");
            }, 1, TIMEOUT);


            // break replay back into data
            final HeaderFlyweight mHeader = new HeaderFlyweight();
            this.nextFragmentOffset = 0;
            fragmentCount = 0;
            remaining = totalDataLength;

            while (remaining > 0)
            {
                poll(replay, (termBuffer, termOffset, chunkLength, header) ->
                {
                    Assert.assertEquals(ReplaySession.REPLAY_DATA_HEADER, termBuffer.getLong(termOffset));
                    validateFragmentsInChunk(mHeader, messageCount,
                                             termBuffer, termOffset + 8, chunkLength - 8);
                }, 1, TIMEOUT);
            }
            Assert.assertEquals(messageCount, fragmentCount);
            Assert.assertEquals(0, remaining);
        }
    }

    private void validateArchiveFile(final int messageCount, final int streamInstanceId) throws IOException
    {
        final StreamInstanceArchiveFragmentReader archiveDataFileReader =
            new StreamInstanceArchiveFragmentReader(streamInstanceId, archiveFolder);
        fragmentCount = 0;
        remaining = totalDataLength;
        archiveDataFileReader.poll(
            (bb, offset, length, header) ->
            {
                Assert.assertEquals(
                    fragmentLength[fragmentCount] - DataHeaderFlyweight.HEADER_LENGTH, length);
                Assert.assertEquals(fragmentCount, bb.getInt(offset));
                Assert.assertEquals('z', bb.getByte(offset + 4));
                remaining -= fragmentLength[fragmentCount];
                fragmentCount++;
            });
        Assert.assertEquals(0, remaining);
        Assert.assertEquals(messageCount, fragmentCount);
    }

    private void validateArchiveFileChunked(final int messageCount, final int streamInstanceId) throws IOException
    {
        final ArchiveDescriptorDecoder decoder =
            archiveMetaFileFormatDecoder(new File(archiveFolder, archiveMetaFileName(streamInstanceId)));
        final long archiveFullLength = ArchiveFileUtil.archiveFullLength(decoder);
        final int initialTermId = decoder.initialTermId();
        final int termBufferLength = decoder.termBufferLength();
        final int initialTermOffset = decoder.initialTermOffset();

        IoUtil.unmap(decoder.buffer().byteBuffer());
        try (StreamInstanceArchiveChunkReader cursor =
                 new StreamInstanceArchiveChunkReader(
                     streamInstanceId,
                     archiveFolder,
                     initialTermId,
                     termBufferLength,
                     initialTermId,
                     initialTermOffset,
                     archiveFullLength))
        {
            fragmentCount = 0;
            final HeaderFlyweight mHeader = new HeaderFlyweight();
            this.nextFragmentOffset = 0;
            remaining = totalDataLength;
            while (!cursor.isDone())
            {
                cursor.readChunk(
                    (termBuffer, termOffset, chunkLength) ->
                    {
                        validateFragmentsInChunk(mHeader,
                                                 messageCount,
                                                 termBuffer,
                                                 termOffset,
                                                 chunkLength);
                        return true;
                    }, 4096 - DataHeaderFlyweight.HEADER_LENGTH);
            }
        }
        Assert.assertEquals(messageCount, fragmentCount);
        Assert.assertEquals(0, remaining);
    }

    private void validateFragmentsInChunk(
        final HeaderFlyweight mHeader,
        final int messageCount,
        final DirectBuffer termBuffer,
        final int termOffset,
        final int chunkLength)
    {
        printf("Chunk: length=%d \t, offset=%d%n", chunkLength, termOffset);

        int messageStart;
        int frameLength;
        while (nextFragmentOffset < chunkLength)
        {
            messageStart = termOffset + this.nextFragmentOffset;
            mHeader.wrap(termBuffer, messageStart, HeaderFlyweight.HEADER_LENGTH);
            frameLength = mHeader.frameLength();

            if (mHeader.headerType() == DataHeaderFlyweight.HDR_TYPE_DATA)
            {
                Assert.assertTrue("Fragments exceed messages",
                                  this.fragmentCount < messageCount);
                Assert.assertEquals("Fragment:" + this.fragmentCount,
                                    fragmentLength[fragmentCount], frameLength);

                if (messageStart + 32 < termOffset + chunkLength)
                {
                    final int index = termBuffer.getInt(messageStart + DataHeaderFlyweight.HEADER_LENGTH);
                    Assert.assertEquals(String.format("Fragment: length=%d, foffset=%d, " +
                                                      "getInt(0)=%d, toffset=%d",
                                                      frameLength, (this.nextFragmentOffset % chunkLength),
                                                      index, termOffset),
                                        this.fragmentCount, index);
                    printf("Fragment: length=%d \t, offset=%d \t, getInt(0)=%d %n",
                           frameLength, (this.nextFragmentOffset % chunkLength), index);
                }
                remaining -= frameLength;
                this.fragmentCount++;
            }
            final int alignedLength = BitUtil.align(frameLength, FRAME_ALIGNMENT);
            this.nextFragmentOffset += alignedLength;
        }
        this.nextFragmentOffset -= chunkLength;

    }

    private void trackArchiveProgress(
        final Publication publication,
        final Subscription archiverNotifications,
        final CountDownLatch waitForData)
    {
        final Thread t = new Thread(
            () ->
            {
                try
                {
                    archived = 0;
                    long start = System.currentTimeMillis();
                    long startBytes = remaining;
                    // each message is fragmentLength[fragmentCount]
                    while (lastTermId == -1 || archived < totalArchiveLength)
                    {
                        poll(archiverNotifications, (buffer, offset, length, header) ->
                        {
                            final MessageHeaderDecoder hDecoder =
                                new MessageHeaderDecoder().wrap(buffer, offset);
                            Assert.assertEquals(ArchiveProgressNotificationDecoder
                                    .TEMPLATE_ID,
                                hDecoder.templateId());

                            final ArchiveProgressNotificationDecoder mDecoder =
                                new ArchiveProgressNotificationDecoder()
                                    .wrap(
                                        buffer,
                                        offset + MessageHeaderDecoder.ENCODED_LENGTH,
                                        hDecoder.blockLength(),
                                        hDecoder.version());
                            Assert.assertEquals(streamInstanceId,
                                mDecoder.streamInstanceId());

                            println(mDecoder.toString());
                            archived = publication.termBufferLength() *
                                       (mDecoder.termId() - mDecoder.initialTermId()) +
                                       (mDecoder.termOffset() -
                                        mDecoder.initialTermOffset());
                            printf("a=%d total=%d %n", archived, totalArchiveLength);
                        }, 1, TIMEOUT);

                        final long end = System.currentTimeMillis();
                        final long deltaTime = end - start;
                        if (deltaTime > TIMEOUT)
                        {
                            start = end;
                            final long deltaBytes = remaining - startBytes;
                            startBytes = remaining;
                            final double mbps = ((deltaBytes * 1000.0) / deltaTime) / MEGABYTE;
                            printf("Archive reported speed: %f MB/s %n", mbps);
                        }
                    }
                    final long end = System.currentTimeMillis();
                    final long deltaTime = end - start;

                    final long deltaBytes = remaining - startBytes;
                    final double mbps = ((deltaBytes * 1000.0) / deltaTime) / MEGABYTE;
                    printf("Archive reported speed: %f MB/s %n", mbps);
                }
                catch (Throwable throwable)
                {
                    trackerError = throwable;
                }
                waitForData.countDown();
            });

        t.setDaemon(true);
        t.start();
    }

    private void poll(final Subscription s, final FragmentHandler f, final int count, final long timeout)
    {
        final long limit = System.currentTimeMillis() + timeout;
        while (0 >= s.poll(f, count))
        {
            LockSupport.parkNanos(TIMEOUT);
            if (limit < System.currentTimeMillis())
            {
                fail("Poll has timed out");
            }
        }
    }

    private long offer(
        final Publication publication,
        final UnsafeBuffer buffer,
        final int offset,
        final int length,
        final long timeout)
    {
        final long limit = System.currentTimeMillis() + timeout;
        long newPosition;

        while ((newPosition = publication.offer(buffer, offset, length)) < 0)
        {
            LockSupport.parkNanos(TIMEOUT);
            if (limit < System.currentTimeMillis())
            {
                fail("Offer has timed out");
            }
        }

        return newPosition;
    }

    private void awaitSubscriptionIsConnected(final Subscription subscription, final long timeout)
    {
        final long limit = System.currentTimeMillis() + timeout;
        while (subscription.imageCount() == 0)
        {
            LockSupport.parkNanos(TIMEOUT);
            if (limit < System.currentTimeMillis())
            {
                fail("awaitSubscriptionIsConnected has timed out");
            }
        }
    }

    private void awaitPublicationIsConnected(final Publication publication, final long timeout)
    {
        final long limit = System.currentTimeMillis() + timeout;
        while (!publication.isConnected())
        {
            LockSupport.parkNanos(TIMEOUT);
            if (limit < System.currentTimeMillis())
            {
                fail("awaitPublicationIsConnected has timed out");
            }
        }
    }

    private void printf(final String s, final Object... args)
    {
        if (DEBUG)
        {
            System.out.printf(s, args);
        }
    }

    private void println(final String s)
    {
        if (DEBUG)
        {
            System.out.println(s);
        }
    }
}