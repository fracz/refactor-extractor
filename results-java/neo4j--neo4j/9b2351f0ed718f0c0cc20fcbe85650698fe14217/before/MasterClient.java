package org.neo4j.kernel.ha;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.queue.BlockingReadHandler;
import org.neo4j.kernel.IdType;
import org.neo4j.kernel.impl.ha.IdAllocation;
import org.neo4j.kernel.impl.ha.LockResult;
import org.neo4j.kernel.impl.ha.Master;
import org.neo4j.kernel.impl.ha.Response;
import org.neo4j.kernel.impl.ha.SlaveContext;
import org.neo4j.kernel.impl.ha.TransactionStream;
import org.neo4j.kernel.impl.ha.TransactionStreams;

/**
 * The {@link Master} a slave should use to communicate with its master. It
 * serializes requests and sends them to the master, more specifically
 * {@link MasterServer} (which delegates to {@link MasterImpl}
 * on the master side.
 */
public class MasterClient extends CommunicationProtocol implements Master
{
    private static Client initClient( String hostNameOrIp, int port )
    {
        ExecutorService executor = Executors.newCachedThreadPool();
        ClientBootstrap bootstrap = new ClientBootstrap( new NioClientSocketChannelFactory(
                executor, executor ) );
        BlockingReadHandler<ChannelBuffer> blockingReadHandler = new BlockingReadHandler<ChannelBuffer>();
        bootstrap.setPipelineFactory( new ClientPipelineFactory( blockingReadHandler ) );
        ChannelFuture channelFuture = bootstrap.connect(
                new InetSocketAddress( hostNameOrIp, port ) );
        Client client = new Client( blockingReadHandler, channelFuture );
        client.awaitConnected();
        System.out.println( "Client connected to " + hostNameOrIp + ":" + port );
        return client;
    }

    private final Client client;

    public MasterClient( String hostNameOrIp, int port )
    {
        client = initClient( hostNameOrIp, port );
    }

    private <T> Response<T> sendRequest( RequestType type,
            SlaveContext slaveContext, Serializer serializer, Deserializer<T> deserializer )
    {
        try
        {
            // Send 'em over the wire
            ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
            buffer.writeByte( type.ordinal() );
            if ( type.includesSlaveContext() )
            {
                writeSlaveContext( buffer, slaveContext );
            }
            serializer.write( buffer );
            client.channel.write(buffer);

            // Read response
            ChannelBuffer message =
//                client.blockingReadHandler.read();
                client.blockingReadHandler.read( 20, TimeUnit.SECONDS );
            if ( message == null )
            {
                throw new HaCommunicationException( "Channel has been closed" );
            }
            T response = deserializer.read( message );
            TransactionStreams txStreams = type.includesSlaveContext() ?
                    readTransactionStreams( message ) : TransactionStreams.EMPTY;
            return new Response<T>( response, txStreams );
        }
        catch ( IOException e )
        {
            throw new HaCommunicationException( e );
        }
        catch ( InterruptedException e )
        {
            throw new HaCommunicationException( e );
        }
    }

    public IdAllocation allocateIds( final IdType idType )
    {
        return sendRequest( RequestType.ALLOCATE_IDS, null, new Serializer()
        {
            public void write( ChannelBuffer buffer ) throws IOException
            {
                buffer.writeByte( idType.ordinal() );
            }
        }, new Deserializer<IdAllocation>()
        {
            public IdAllocation read( ChannelBuffer buffer ) throws IOException
            {
                return readIdAllocation( buffer );
            }
        } ).response();
    }

    public Response<Integer> createRelationshipType( SlaveContext context, final String name )
    {
        return sendRequest( RequestType.CREATE_RELATIONSHIP_TYPE, context, new Serializer()
        {
            public void write( ChannelBuffer buffer ) throws IOException
            {
                writeString( buffer, name );
            }
        }, new Deserializer<Integer>()
        {
            @SuppressWarnings( "boxing" )
            public Integer read( ChannelBuffer buffer ) throws IOException
            {
                return buffer.readInt();
            }
        } );
    }

    public Response<LockResult> acquireNodeWriteLock( SlaveContext context, final int eventIdentifier,
            long... nodes )
    {
        return sendRequest( RequestType.ACQUIRE_NODE_WRITE_LOCK, context,
                new AcquireLockSerializer( eventIdentifier, nodes ), LOCK_RESULT_DESERIALIZER );
    }

    public Response<LockResult> acquireNodeReadLock( SlaveContext context, int eventIdentifier,
            long... nodes )
    {
        return sendRequest( RequestType.ACQUIRE_NODE_READ_LOCK, context,
                new AcquireLockSerializer( eventIdentifier, nodes ), LOCK_RESULT_DESERIALIZER );
    }

    public Response<LockResult> acquireRelationshipWriteLock( SlaveContext context, int eventIdentifier,
            long... relationships )
    {
        return sendRequest( RequestType.ACQUIRE_RELATIONSHIP_WRITE_LOCK, context,
                new AcquireLockSerializer( eventIdentifier, relationships ),
                LOCK_RESULT_DESERIALIZER );
    }

    public Response<LockResult> acquireRelationshipReadLock( SlaveContext context, int eventIdentifier,
            long... relationships )
    {
        return sendRequest( RequestType.ACQUIRE_RELATIONSHIP_READ_LOCK, context,
                new AcquireLockSerializer( eventIdentifier, relationships ),
                LOCK_RESULT_DESERIALIZER );
    }

    public Response<Long> commitSingleResourceTransaction( SlaveContext context,
            final int eventIdentifier, final String resource,
            final TransactionStream transactionStream )
    {
        return sendRequest( RequestType.COMMIT, context, new Serializer()
        {
            public void write( ChannelBuffer buffer ) throws IOException
            {
                buffer.writeInt( eventIdentifier );
                writeString( buffer, resource );
                writeTransactionStream(buffer, transactionStream);
            }
        }, new Deserializer<Long>()
        {
            @SuppressWarnings( "boxing" )
            public Long read( ChannelBuffer buffer ) throws IOException
            {
                return buffer.readLong();
            }
        });
    }

    public Response<Void> doneCommitting( SlaveContext context, final int eventIdentifier )
    {
        return sendRequest( RequestType.DONE_COMMITTING, context, new Serializer()
        {
            public void write( ChannelBuffer buffer ) throws IOException
            {
                buffer.writeInt( eventIdentifier );
            }
        }, VOID_DESERIALIZER );
    }

    public Response<Void> rollbackTransaction( SlaveContext context, final int eventIdentifier )
    {
        return sendRequest( RequestType.ROLLBACK, context, new Serializer()
        {
            public void write( ChannelBuffer buffer ) throws IOException
            {
                buffer.writeInt( eventIdentifier );
            }
        }, VOID_DESERIALIZER );
    }

    public void rollbackOngoingTransactions( SlaveContext context )
    {
        throw new UnsupportedOperationException( "Should never be called from the client side" );
    }

    public Response<Void> pullUpdates( SlaveContext context )
    {
        return sendRequest( RequestType.PULL_UPDATES, context, EMPTY_SERIALIZER, VOID_DESERIALIZER );
    }

    private static class Client
    {
        private BlockingReadHandler<ChannelBuffer> blockingReadHandler;
        private ChannelFuture channelFuture;
        private Channel channel;

        public Client( BlockingReadHandler<ChannelBuffer> blockingReadHandler,
                ChannelFuture channelFuture )
        {
            this.blockingReadHandler = blockingReadHandler;
            this.channelFuture = channelFuture;
            this.channel = channelFuture.getChannel();
        }

        public void close()
        {
            channel.close();
        }

        public void awaitConnected()
        {
            channelFuture.awaitUninterruptibly();
        }
    }

    private static class ClientPipelineFactory implements ChannelPipelineFactory
    {
        private BlockingReadHandler<ChannelBuffer> blockingReadHandler;

        public ClientPipelineFactory( BlockingReadHandler<ChannelBuffer> blockingReadHandler )
        {
            this.blockingReadHandler = blockingReadHandler;
        }

        public ChannelPipeline getPipeline() throws Exception
        {
            ChannelPipeline pipeline = Channels.pipeline();
            pipeline.addLast( "frameDecoder", new LengthFieldBasedFrameDecoder( MAX_FRAME_LENGTH,
                    0, 4, 0, 4 ) );
            pipeline.addLast( "frameEncoder", new LengthFieldPrepender( 4 ) );
            pipeline.addLast( "blockingHandler", blockingReadHandler );
            return pipeline;
        }
    }

    public void shutdown()
    {
        client.close();
    }
}