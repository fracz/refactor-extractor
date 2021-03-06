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

import uk.co.real_logic.aeron.util.BasicBufferStrategy;
import uk.co.real_logic.aeron.util.FileMappingConvention;
import uk.co.real_logic.aeron.util.IoUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;

/**
 * Basic buffer management where each Term is a file.
 */
public class BasicBufferManagementStrategy extends BasicBufferStrategy implements BufferManagementStrategy
{
    private static final long BUFFER_SIZE = 256 * 1024;

    private final FileChannel templateFile;

    public BasicBufferManagementStrategy(final String dataDir)
    {
        super(dataDir);
        IoUtil.ensureDirectoryExists(senderDir, "sender");
        IoUtil.ensureDirectoryExists(receiverDir, "receiver");
        templateFile = createTemplateFile();
    }

    /**
     * Create a blank, zero'd out file of the correct size.
     * This lets us just use transferTo to initialize the buffers.
     */
    private FileChannel createTemplateFile()
    {
        try
        {
            final File tempFile = File.createTempFile("templateFile", "bin");
            tempFile.deleteOnExit();

            final RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "rw");
            final FileChannel templateFile = randomAccessFile.getChannel();
            IoUtil.fill(templateFile, 0, BUFFER_SIZE, (byte) 0);
            return templateFile;
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Unable to create temporary file", e);
        }
    }

    /**
     * Maps a buffer for a given term, ensuring that a file of the correct size is created.
     */
    public MappedByteBuffer mapTerm(final File rootDir,
                                    final long sessionId,
                                    final long channelId,
                                    final long termId,
                                    final long requiredSize) throws Exception
    {
        final File termIdFile = FileMappingConvention.termIdFile(rootDir, sessionId, channelId, termId, true);
        // must be checked at this point, opening a RandomAccessFile will cause this to be true
        final boolean fileExists = termIdFile.exists();
        try (final RandomAccessFile randomAccessFile = new RandomAccessFile(termIdFile, "rw"))
        {
            long size = requiredSize;
            final FileChannel channel = randomAccessFile.getChannel();

            if (fileExists)
            {
                size = randomAccessFile.length();
            }
            else
            {
                long transferred = templateFile.transferTo(0, requiredSize, channel);
                if (transferred != requiredSize)
                {
                    throw new IllegalStateException("Unable to initialize the required size of " + requiredSize);
                }
            }

            return channel.map(READ_WRITE, 0, size);
        }
    }

    @Override
    public void addSenderTerm(final long sessionId, final long channelId, final long termId) throws Exception
    {
        registerTerm(sessionId, channelId, termId, srcTermMap, () ->
        {
            return mapTerm(senderDir, sessionId, channelId, termId, BUFFER_SIZE);
        });
    }

    @Override
    public void removeSenderTerm(final long sessionId, final long channelId, final long termId)
    {
        srcTermMap.remove(sessionId, channelId, termId);
    }

    @Override
    public void removeSenderChannel(final long sessionId, final long channelId)
    {
        srcTermMap.remove(sessionId, channelId);
    }

    @Override
    public void addReceiverTerm(final UdpDestination destination,
                                final long sessionId,
                                final long channelId,
                                final long termId) throws Exception
    {

    }

    @Override
    public ByteBuffer lookupReceiverTerm(final UdpDestination destination,
                                         final long sessionId,
                                         final long channelId,
                                         final long termId)
    {
        return null;
    }

    @Override
    public int countSessions()
    {
        return srcTermMap.sessionCount();
    }

    @Override
    public int countChannels(final long sessionId)
    {
        return srcTermMap.channelCount(sessionId);
    }

    @Override
    public int countTerms(final long sessionId, final long channelId)
    {
        return srcTermMap.termCount(sessionId, channelId);
    }

    @Override
    public int countSessions(final UdpDestination destination)
    {
        return rcvTermMap.sessionCount();
    }

    @Override
    public int countChannels(final UdpDestination destination, final long sessionId)
    {
        return rcvTermMap.channelCount(sessionId);
    }

    @Override
    public int countTerms(final UdpDestination destination, final long sessionId, final long channelId)
    {
        return rcvTermMap.termCount(sessionId, channelId);
    }
}