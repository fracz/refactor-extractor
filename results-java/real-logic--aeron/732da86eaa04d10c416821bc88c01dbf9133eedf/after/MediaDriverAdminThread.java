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

import uk.co.real_logic.aeron.mediadriver.buffer.BufferManagementStrategy;
import uk.co.real_logic.aeron.util.ClosableThread;
import uk.co.real_logic.aeron.util.command.ChannelMessageFlyweight;
import uk.co.real_logic.aeron.util.command.ControlProtocolEvents;
import uk.co.real_logic.aeron.util.command.ErrorCode;
import uk.co.real_logic.aeron.util.command.LibraryFacade;
import uk.co.real_logic.aeron.util.concurrent.AtomicBuffer;
import uk.co.real_logic.aeron.util.concurrent.ringbuffer.ManyToOneRingBuffer;
import uk.co.real_logic.aeron.util.concurrent.ringbuffer.RingBuffer;
import uk.co.real_logic.aeron.util.protocol.ErrorHeaderFlyweight;
import uk.co.real_logic.aeron.util.protocol.HeaderFlyweight;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Admin thread to take commands from Producers and Consumers as well as handle NAKs and retransmissions
 */
public class MediaDriverAdminThread extends ClosableThread implements LibraryFacade
{

    private final RingBuffer commandBuffer;
    private final ReceiverThreadCursor receiverThreadCursor;
    private final NioSelector nioSelector;
    private final ReceiverThread receiverThread;
    private final SenderThread senderThread;
    private final BufferManagementStrategy bufferManagementStrategy;
    private final RingBuffer adminReceiveBuffer;

    private final Map<UdpDestination, SrcFrameHandler> srcDestinationMap;
    private final ChannelMap<SenderChannel> sendChannels;

    private final ChannelMessageFlyweight channelMessage = new ChannelMessageFlyweight();
    private final ErrorHeaderFlyweight errorHeaderFlyweight = new ErrorHeaderFlyweight();

    public MediaDriverAdminThread(final MediaDriver.TopologyBuilder builder,
                                  final ReceiverThread receiverThread,
                                  final SenderThread senderThread)
    {
        this.commandBuffer = builder.adminThreadCommandBuffer();
        this.receiverThreadCursor = new ReceiverThreadCursor(builder.receiverThreadCommandBuffer(), receiverThread);
        this.bufferManagementStrategy = builder.bufferManagementStrategy();
        this.nioSelector = builder.adminNioSelector();
        this.receiverThread = receiverThread;
        this.senderThread = senderThread;
        this.sendChannels = new ChannelMap<>();
        this.srcDestinationMap = new HashMap<>();

        try
        {
            final ByteBuffer buffer = builder.adminBufferStrategy().toMediaDriver();
            this.adminReceiveBuffer = new ManyToOneRingBuffer(new AtomicBuffer(buffer));
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Unable to create the admin media buffers", e);
        }
    }

    /**
     * Add NAK frame to command buffer for this thread
     * @param header for the NAK frame
     */
    public static void addNakEvent(final RingBuffer buffer, final HeaderFlyweight header)
    {
        // TODO: add NAK frame to this threads command buffer
    }

    public static void addRcvCreateNewTermBufferEvent(final RingBuffer buffer,
                                                      final UdpDestination destination,
                                                      final long sessionId,
                                                      final long channelId,
                                                      final long termId)
    {
        // TODO: add event to command buffer
    }

    @Override
    public void process()
    {
        // TODO: nioSelector.processKeys
        adminReceiveBuffer.read((eventTypeId, buffer, index, length) ->
        {
            // TODO: call onAddChannel, etc.

            switch (eventTypeId)
            {
                case ControlProtocolEvents.ADD_CHANNEL:
                    channelMessage.reset(buffer, index);
                    onAddChannel(channelMessage.destination(),
                                 channelMessage.sessionId(),
                                 channelMessage.channelId());
                    return;
                case ControlProtocolEvents.REMOVE_CHANNEL:
                    channelMessage.reset(buffer, index);
                    onRemoveChannel(channelMessage.destination(),
                                    channelMessage.sessionId(),
                                    channelMessage.channelId());
                    return;
            }
        });
        // TODO: read from commandBuffer and dispatch to onNakEvent, etc.
    }

    @Override
    public void sendErrorResponse(final int code, final byte[] message)
    {
        // TODO: construct error response for control buffer and write it in
    }

    @Override
    public void sendError(final int code, final byte[] message)
    {
        // TODO: construct error notification for control buffer and write it in
    }

    @Override
    public void sendNewBufferNotification(final long sessionId,
                                          final long channelId,
                                          final long termId,
                                          final boolean isSender,
                                          final String destination)
    {

    }

    @Override
    public void onAddChannel(final String destination, final long sessionId, final long channelId)
    {
        // TODO: to accommodate error handling, probably need to pass in Flyweight itself...
        try
        {
            final UdpDestination srcDestination = UdpDestination.parse(destination);
            SenderChannel channel = sendChannels.get(srcDestination, sessionId, channelId);
            if (channel != null)
            {
                // TODO: error. channel and sessionId already exist
            }

            SrcFrameHandler frameHandler = srcDestinationMap.get(srcDestination);
            if (frameHandler == null)
            {
                frameHandler = new SrcFrameHandler(srcDestination, nioSelector, commandBuffer);
                srcDestinationMap.put(srcDestination, frameHandler);
            }

            // new channel, so generate "random"-ish termId and create term buffer
            final long termId = (long)(Math.random() * 0xFFFFFFFFL);  // FIXME: this may not be random enough
            final ByteBuffer buffer = bufferManagementStrategy.addSenderTerm(srcDestination, sessionId, channelId, termId);

            channel = new SenderChannel(frameHandler, buffer, srcDestination, sessionId, channelId, termId);
            sendChannels.put(srcDestination, sessionId, channelId, channel);

            // tell the client admin thread of the new buffer
            sendNewBufferNotification(sessionId, channelId, termId, true, destination);

            // add channel to sender thread atomic array so it can be integrated in
            senderThread.addChannel(channel);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            sendErrorResponse(ErrorCode.GENERIC_ERROR.value(), e.getMessage().getBytes());
            // TODO: log this as well as send the error response
        }
    }

    @Override
    public void onRemoveChannel(final String destination, final long sessionId, final long channelId)
    {
        try
        {
            final UdpDestination srcDestination = UdpDestination.parse(destination);
            final SenderChannel channel = sendChannels.remove(srcDestination, sessionId, channelId);
            if (channel == null)
            {
                // TODO: error
            }

            // remove from buffer management
            bufferManagementStrategy.removeSenderChannel(srcDestination, sessionId, channelId);

            senderThread.removeChannel(channel);

            // if no more channels, then remove framehandler and close it
            if (sendChannels.isEmpty(srcDestination))
            {
                SrcFrameHandler frameHandler = srcDestinationMap.remove(srcDestination);
                frameHandler.close();
            }
        }
        catch (Exception e)
        {
            // TODO: log this as well as send the error response
            e.printStackTrace();
            sendErrorResponse(ErrorCode.GENERIC_ERROR.value(), e.getMessage().getBytes());
        }
    }

    @Override
    public void onRemoveTerm(final String destination, final long sessionId, final long channelId, final long termId)
    {
        try
        {
            final UdpDestination srcDestination = UdpDestination.parse(destination);
            final SrcFrameHandler src = srcDestinationMap.get(srcDestination);

            if (null == src)
            {
                throw new IllegalArgumentException("destination unknown for term remove: " + destination);
            }

            // remove from buffer management, but will be unmapped once SenderThread releases it and it can be GCed
            bufferManagementStrategy.removeSenderTerm(srcDestination, sessionId, channelId, termId);

            // TODO: inform SenderThread

            // if no more channels, then remove framehandler and close it
            if (0 == bufferManagementStrategy.countChannels(sessionId))
            {
                srcDestinationMap.remove(srcDestination);
                src.close();
            }
        }
        catch (Exception e)
        {
            sendErrorResponse(ErrorCode.GENERIC_ERROR.value(), e.getMessage().getBytes());
            // TODO: log this as well as send the error response
        }
    }

    @Override
    public void onAddReceiver(final String destination, final long[] channelIdList)
    {
        // instruct receiver thread of new framehandler and new channelIdlist for such
        receiverThreadCursor.addNewReceiverEvent(destination, channelIdList);

        // this thread does not add buffers. The RcvFrameHandler handle methods will send an event for this thread
        // to create buffers as needed
    }

    @Override
    public void onRemoveReceiver(final String destination, final long[] channelIdList)
    {
        // instruct receiver thread to get rid of channels and destination
        receiverThreadCursor.addRemoveReceiverEvent(destination, channelIdList);
    }

    @Override
    public void onRequestTerm(final long sessionId, final long channelId, final long termId)
    {

    }

    private void onNakEvent(final HeaderFlyweight header)
    {
        // TODO: handle the NAK.
    }

    private void onRcvCreateNewTermBufferEvent(final String destination,
                                               final long sessionId,
                                               final long channelId,
                                               final long termId)
    {
        // TODO: create new buffer via strategy, then instruct the receiver thread that we are done and it can grab it

        receiverThreadCursor.addTermBufferCreatedEvent(destination, sessionId, channelId, termId);
    }
}