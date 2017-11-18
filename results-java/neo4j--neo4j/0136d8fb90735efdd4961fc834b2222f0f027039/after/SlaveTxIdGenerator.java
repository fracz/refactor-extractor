package org.neo4j.kernel.ha;

import java.io.IOException;
import java.util.Arrays;

import javax.transaction.TransactionManager;

import org.neo4j.kernel.ha.zookeeper.ZooKeeperException;
import org.neo4j.kernel.impl.ha.Broker;
import org.neo4j.kernel.impl.ha.Response;
import org.neo4j.kernel.impl.ha.ResponseReceiver;
import org.neo4j.kernel.impl.ha.TransactionStream;
import org.neo4j.kernel.impl.transaction.TxManager;
import org.neo4j.kernel.impl.transaction.xaframework.TxIdGenerator;
import org.neo4j.kernel.impl.transaction.xaframework.TxIdGeneratorFactory;
import org.neo4j.kernel.impl.transaction.xaframework.XaDataSource;

public class SlaveTxIdGenerator implements TxIdGenerator
{
    public static class SlaveTxIdGeneratorFactory implements TxIdGeneratorFactory
    {
        private final Broker broker;
        private final ResponseReceiver receiver;

        public SlaveTxIdGeneratorFactory( Broker broker, ResponseReceiver receiver )
        {
            this.broker = broker;
            this.receiver = receiver;
        }

        public TxIdGenerator create( TransactionManager txManager )
        {
            return new SlaveTxIdGenerator( broker, receiver, txManager );
        }
    }

    private final Broker broker;
    private final ResponseReceiver receiver;
    private final TxManager txManager;

    public SlaveTxIdGenerator( Broker broker, ResponseReceiver receiver,
            TransactionManager txManager )
    {
        this.broker = broker;
        this.receiver = receiver;
        this.txManager = (TxManager) txManager;
    }

    public long generate( XaDataSource dataSource, int identifier )
    {
        try
        {
            int eventIdentifier = txManager.getEventIdentifier();
            Response<Long> response = broker.getMaster().commitSingleResourceTransaction(
                    receiver.getSlaveContext( eventIdentifier ),
                    dataSource.getName(), new TransactionStream( Arrays.asList(
                            dataSource.getPreparedTransaction( identifier ) ) ) );
            return receiver.receive( response );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
        catch ( ZooKeeperException e )
        {
            receiver.somethingIsWrong( e );
            throw e;
        }
        catch ( HaCommunicationException e )
        {
            receiver.somethingIsWrong( e );
            throw e;
        }
    }
}