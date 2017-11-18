package slavetest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public abstract class AbstractHaTest
{
    static final RelationshipType REL_TYPE = DynamicRelationshipType.withName( "HA_TEST" );
    static final File PARENT_PATH = new File( "target/dbs" );
    static final File MASTER_PATH = new File( PARENT_PATH, "master" );
    static final File SLAVE_PATH = new File( PARENT_PATH, "slave" );

    protected static File slavePath( int num )
    {
        return new File( SLAVE_PATH, "" + num );
    }

    public static void verify( GraphDatabaseService refDb, GraphDatabaseService... dbs )
    {
        for ( GraphDatabaseService otherDb : dbs )
        {
            Set<Node> otherNodes = IteratorUtil.addToCollection( otherDb.getAllNodes().iterator(),
                    new HashSet<Node>() );
            for ( Node node : refDb.getAllNodes() )
            {
                Node otherNode = otherDb.getNodeById( node.getId() );
                verifyNode( node, otherNode, otherDb );
                otherNodes.remove( otherNode );
            }
            assertTrue( otherNodes.isEmpty() );
        }
    }

    private static String tab( int times )
    {
        StringBuilder builder = new StringBuilder();
        for ( int i = 0; i < times; i++ )
        {
            builder.append( "\t" );
        }
        return builder.toString();
    }

    private static void verifyNode( Node node, Node otherNode, GraphDatabaseService otherDb )
    {
        verifyProperties( node, otherNode );
        Set<Long> otherRelIds = new HashSet<Long>();
        for ( Relationship otherRel : otherNode.getRelationships( Direction.OUTGOING ) )
        {
            otherRelIds.add( otherRel.getId() );
        }

        for ( Relationship rel : node.getRelationships( Direction.OUTGOING ) )
        {
            Relationship otherRel = otherDb.getRelationshipById( rel.getId() );
            verifyProperties( rel, otherRel );
            if ( rel.getStartNode().getId() != otherRel.getStartNode().getId() )
            {
                throw new RuntimeException( "Start node differs on " + rel );
            }
            if ( rel.getEndNode().getId() != otherRel.getEndNode().getId() )
            {
                throw new RuntimeException( "End node differs on " + rel );
            }
            if ( !rel.getType().name().equals( otherRel.getType().name() ) )
            {
                throw new RuntimeException( "Type differs on " + rel );
            }
            otherRelIds.remove( rel.getId() );
        }

        if ( !otherRelIds.isEmpty() )
        {
            throw new RuntimeException( "Other node " + otherNode + " has more relationships " +
                    otherRelIds );
        }
    }

    private static void verifyProperties( PropertyContainer entity, PropertyContainer otherEntity )
    {
        Set<String> otherKeys = IteratorUtil.addToCollection(
                otherEntity.getPropertyKeys().iterator(), new HashSet<String>() );
        for ( String key : entity.getPropertyKeys() )
        {
            Object value1 = entity.getProperty( key );
            Object value2 = otherEntity.getProperty( key );
            if ( !value1.equals( value2 ) )
            {
                throw new RuntimeException( entity + " not equals property '" + key + "': " +
                        value1 + ", " + value2 );
            }
            otherKeys.remove( key );
        }
        if ( !otherKeys.isEmpty() )
        {
            throw new RuntimeException( "Other node " + otherEntity + " has more properties: " +
                    otherKeys );
        }
    }

    public static <T> void assertCollection( Collection<T> collection, T... expectedItems )
    {
        String collectionString = join( ", ", collection.toArray() );
        assertEquals( collectionString, expectedItems.length,
                collection.size() );
        for ( T item : expectedItems )
        {
            assertTrue( collection.contains( item ) );
        }
    }

    public static <T> String join( String delimiter, T... items )
    {
        StringBuffer buffer = new StringBuffer();
        for ( T item : items )
        {
            if ( buffer.length() > 0 )
            {
                buffer.append( delimiter );
            }
            buffer.append( item.toString() );
        }
        return buffer.toString();
    }

    protected void initDeadMasterAndSlaveDbs( int numSlaves ) throws IOException
    {
        FileUtils.deleteDirectory( PARENT_PATH );
        GraphDatabaseService masterDb =
                new EmbeddedGraphDatabase( MASTER_PATH.getAbsolutePath() );
        masterDb.shutdown();
        for ( int i = 0; i < numSlaves; i++ )
        {
            FileUtils.copyDirectory( MASTER_PATH, slavePath( i ) );
        }
    }

    protected abstract void initializeDbs( int numSlaves ) throws Exception;

    protected abstract void pullUpdates( int... slaves ) throws Exception;

    protected abstract <T> T executeJob( Job<T> job, int onSlave ) throws Exception;

    protected abstract <T> T executeJobOnMaster( Job<T> job ) throws Exception;

    protected abstract void startUpMaster() throws Exception;

    protected abstract Job<Void> getMasterShutdownDispatcher();

    @Test
    public void slaveCreateNode() throws Exception
    {
        initializeDbs( 1 );
        executeJob( new CommonJobs.CreateSomeEntitiesJob(), 0 );
    }

    @Test
    public void testMultipleSlaves() throws Exception
    {
        initializeDbs( 3 );
        executeJob( new CommonJobs.CreateSubRefNodeJob( CommonJobs.REL_TYPE.name(), null, null ), 0 );
        executeJob( new CommonJobs.SetSubRefPropertyJob( "name", "Hello" ), 1 );
        pullUpdates( 0, 2 );
    }

    @Test
    public void testMasterFailure() throws Exception
    {
        initializeDbs( 1 );
        Serializable[] result = executeJob( new CommonJobs.CreateSubRefNodeMasterFailJob(
                getMasterShutdownDispatcher() ), 0 );
        assertFalse( (Boolean) result[0] );
        startUpMaster();
        long nodeId = (Long) result[1];
        Boolean existed = executeJob( new CommonJobs.GetNodeByIdJob( nodeId ), 0 );
        assertFalse( existed.booleanValue() );
    }

    @Test
    public void testSlaveConstraintViolation() throws Exception
    {
        initializeDbs( 1 );

        Long nodeId = executeJob( new CommonJobs.CreateSubRefNodeJob(
                CommonJobs.REL_TYPE.name(), null, null ), 0 );
        Boolean successful = executeJob( new CommonJobs.DeleteNodeJob( nodeId.longValue() ), 0 );
        assertFalse( successful.booleanValue() );
    }

    @Test
    public void testMasterConstrainViolation() throws Exception
    {
        initializeDbs( 1 );

        Long nodeId = executeJob( new CommonJobs.CreateSubRefNodeJob( CommonJobs.REL_TYPE.name(),
                "name", "Mattias" ), 0 );
        Boolean successful = executeJobOnMaster( new CommonJobs.DeleteNodeJob( nodeId.longValue() ) );
        assertFalse( successful.booleanValue() );
        pullUpdates();
    }

    @Test
    public void testGetRelationships() throws Exception
    {
        initializeDbs( 1 );

        assertEquals( (Integer) 1, executeJob( new CommonJobs.CreateSubRefNodeWithRelCountJob(
                CommonJobs.REL_TYPE.name(), CommonJobs.REL_TYPE.name(), CommonJobs.KNOWS.name() ), 0 ) );
        assertEquals( (Integer) 2, executeJob( new CommonJobs.CreateSubRefNodeWithRelCountJob(
                CommonJobs.REL_TYPE.name(), CommonJobs.REL_TYPE.name(), CommonJobs.KNOWS.name() ), 0 ) );
        assertEquals( (Integer) 2, executeJob( new CommonJobs.GetRelationshipCountJob(
                CommonJobs.REL_TYPE.name(), CommonJobs.KNOWS.name() ), 0 ) );
        assertEquals( (Integer) 2, executeJobOnMaster( new CommonJobs.GetRelationshipCountJob(
                CommonJobs.REL_TYPE.name(), CommonJobs.KNOWS.name() ) ) );
    }

    @Test
    public void testNoTransaction() throws Exception
    {
        initializeDbs( 1 );

        executeJobOnMaster( new CommonJobs.CreateSubRefNodeJob(
                CommonJobs.REL_TYPE.name(), null, null ) );
        assertFalse( executeJob( new CommonJobs.CreateNodeOutsideOfTxJob(), 0 ).booleanValue() );
        assertFalse( executeJobOnMaster( new CommonJobs.CreateNodeOutsideOfTxJob() ).booleanValue() );
    }

    @Test
    public void testNodeDeleted() throws Exception
    {
        initializeDbs( 1 );

        Long nodeId = executeJobOnMaster( new CommonJobs.CreateNodeJob() );
        pullUpdates();
        assertTrue( executeJobOnMaster( new CommonJobs.DeleteNodeJob(
                nodeId.longValue() ) ).booleanValue() );
        assertFalse( executeJob( new CommonJobs.SetNodePropertyJob( nodeId.longValue(), "something",
                "some thing" ), 0 ) );
    }

    @Test
    public void testDeadlock() throws Exception
    {
        initializeDbs( 2 );

        Long[] nodes = executeJobOnMaster( new CommonJobs.CreateNodesJob( 2 ) );
        pullUpdates();

        Fetcher<DoubleLatch> fetcher = getDoubleLatch();
        Worker w1 = new Worker( 0, new CommonJobs.Worker1Job( nodes[0], nodes[1], fetcher ) );
        Worker w2 = new Worker( 1, new CommonJobs.Worker2Job( nodes[0], nodes[1], fetcher ) );
        w1.start();
        w2.start();
        while ( w1.isAlive() || w2.isAlive() )
        {
            Thread.sleep( 500 );
        }
        boolean case1 = w2.successfull && !w2.deadlocked && !w1.successfull && w1.deadlocked;
        boolean case2 = !w2.successfull && w2.deadlocked && w1.successfull && !w1.deadlocked;
        assertTrue( case1 != case2 );
        assertTrue( case1 || case2  );
        pullUpdates();
    }

    protected abstract void shutdownDbs() throws Exception;

    protected abstract Fetcher<DoubleLatch> getDoubleLatch() throws Exception;

    private class Worker extends Thread
    {
        private boolean successfull;
        private boolean deadlocked;
        private final int slave;
        private final Job<Boolean[]> job;

        Worker( int slave, Job<Boolean[]> job )
        {
            this.slave = slave;
            this.job = job;
        }

        @Override
        public void run()
        {
            try
            {
                Boolean[] result = executeJob( job, slave );
                successfull = result[0];
                deadlocked = result[1];
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
    }
}