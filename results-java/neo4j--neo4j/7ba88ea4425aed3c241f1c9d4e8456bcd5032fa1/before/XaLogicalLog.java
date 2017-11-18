/*
 * Copyright (c) 2002-2010 "Neo Technology,"
 *     Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.transaction.xaframework;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.neo4j.kernel.Config;
import org.neo4j.kernel.impl.util.ArrayMap;
import org.neo4j.kernel.impl.util.FileUtils;

/**
 * <CODE>XaLogicalLog</CODE> is a transaction and logical log combined. In
 * this log information about the transaction (such as started, prepared and
 * committed) will be written. All commands participating in the transaction
 * will also be written to the log.
 * <p>
 * Normally you don't have to do anything with this log except open it after it
 * has been instanciated (see {@link XaContainer}). The only method that may be
 * of use when implementing a XA compatible resource is the
 * {@link #getCurrentTxIdentifier}. Leave everything else be unless you know
 * what you're doing.
 * <p>
 * When the log is opened it will be scaned for uncompleted transactions and
 * those transactions will be re-created. When scan of log is complete all
 * transactions that hasn't entered prepared state will be marked as done
 * (implies rolledback) and dropped. All transactions that have been prepared
 * will be held in memory until the transaction manager tells them to commit.
 * Transaction that already started commit but didn't get flagged as done will
 * be re-committed.
 */
public class XaLogicalLog
{
    private Logger log;

    private static final char CLEAN = 'C';
    private static final char LOG1 = '1';
    private static final char LOG2 = '2';

    private FileChannel fileChannel = null;
    private final ByteBuffer buffer;
    private LogBuffer writeBuffer = null;
    private long previousLogLastCommittedTx = -1;
    private long logVersion = 0;
    private ArrayMap<Integer,LogEntry.Start> xidIdentMap =
        new ArrayMap<Integer,LogEntry.Start>( 4, false, true );
    private Map<Integer,XaTransaction> recoveredTxMap =
        new HashMap<Integer,XaTransaction>();
    private int nextIdentifier = 1;
    private boolean scanIsComplete = false;

    private String fileName = null;
    private final XaResourceManager xaRm;
    private final XaCommandFactory cf;
    private final XaTransactionFactory xaTf;
    private char currentLog = CLEAN;
    private boolean keepLogs = false;
    private boolean autoRotate = true;
    private long rotateAtSize = 10*1024*1024; // 10MB
    private boolean backupSlave = false;
    private boolean slave = false;
    private boolean useMemoryMapped = true;

    XaLogicalLog( String fileName, XaResourceManager xaRm, XaCommandFactory cf,
        XaTransactionFactory xaTf, Map<Object,Object> config )
    {
        this.fileName = fileName;
        this.xaRm = xaRm;
        this.cf = cf;
        this.xaTf = xaTf;
        this.useMemoryMapped = getMemoryMapped( config );
        log = Logger.getLogger( this.getClass().getName() + "/" + fileName );
        buffer = ByteBuffer.allocateDirect( 9 + Xid.MAXGTRIDSIZE
            + Xid.MAXBQUALSIZE * 10 );
    }

    private boolean getMemoryMapped( Map<Object,Object> config )
    {
        String configValue = config != null ?
                (String) config.get( Config.USE_MEMORY_MAPPED_BUFFERS ) : null;
        return configValue != null ? Boolean.parseBoolean( configValue ) : true;
    }

    synchronized void open() throws IOException
    {
        String activeFileName = fileName + ".active";
        if ( !new File( activeFileName ).exists() )
        {
            if ( new File( fileName ).exists() )
            {
                // old < b8 xaframework with no log rotation and we need to
                // do recovery on it
                open( fileName );
            }
            else
            {
                open( fileName + ".1" );
                setActiveLog( LOG1 );
            }
        }
        else
        {
            FileChannel fc = new RandomAccessFile( activeFileName ,
                "rw" ).getChannel();
            byte bytes[] = new byte[256];
            ByteBuffer buf = ByteBuffer.wrap( bytes );
            int read = fc.read( buf );
            fc.close();
            if ( read != 4 )
            {
                throw new IllegalStateException( "Read " + read +
                    " bytes from " + activeFileName + " but expected 4" );
            }
            buf.flip();
            char c = buf.asCharBuffer().get();
            File copy = new File( fileName + ".copy" );
            if ( copy.exists() )
            {
                if ( !copy.delete() )
                {
                    log.warning( "Unable to delete " + copy.getName() );
                }
            }
            if ( c == CLEAN )
            {
                // clean
                String newLog = fileName + ".1";
                File file = new File( newLog );
                if ( file.exists() )
                {
                    fixCleanKill( newLog );
                }
                file = new File( fileName + ".2" );
                if ( file.exists() )
                {
                    fixCleanKill( fileName + ".2" );
                }
                open( newLog );
                setActiveLog( LOG1 );
            }
            else if ( c == LOG1 )
            {
                String newLog = fileName + ".1";
                if ( !new File( newLog ).exists() )
                {
                    throw new IllegalStateException(
                        "Active marked as 1 but no " + newLog + " exist" );
                }
                currentLog = LOG1;
                File otherLog = new File( fileName + ".2" );
                if ( otherLog.exists() )
                {
                    if ( !otherLog.delete() )
                    {
                        log.warning( "Unable to delete " + copy.getName() );
                    }
                }
                open( newLog );
            }
            else if ( c == LOG2 )
            {
                String newLog = fileName + ".2";
                if ( !new File( newLog ).exists() )
                {
                    throw new IllegalStateException(
                        "Active marked as 2 but no " + newLog + " exist" );
                }
                File otherLog = new File( fileName + ".1" );
                if ( otherLog.exists() )
                {
                    if ( !otherLog.delete() )
                    {
                        log.warning( "Unable to delete " + copy.getName() );
                    }
                }
                currentLog = LOG2;
                open( newLog );
            }
            else
            {
                throw new IllegalStateException( "Unknown active log: " + c );
            }
        }
        if ( !useMemoryMapped )
        {
            writeBuffer = new DirectMappedLogBuffer( fileChannel );
        }
        else
        {
            writeBuffer = new MemoryMappedLogBuffer( fileChannel );
        }
    }

    private void fixCleanKill( String fileName ) throws IOException
    {
        File file = new File( fileName );
        if ( !keepLogs )
        {
            if ( !file.delete() )
            {
                throw new IllegalStateException(
                    "Active marked as clean and unable to delete log " +
                    fileName );
            }
        }
        else
        {
            renameCurrentLogFileAndIncrementVersion( fileName, file.length() );
        }
    }

    private void open( String fileToOpen ) throws IOException
    {
        fileChannel = new RandomAccessFile( fileToOpen, "rw" ).getChannel();
        if ( fileChannel.size() != 0 )
        {
            doInternalRecovery( fileToOpen );
        }
        else
        {
            logVersion = xaTf.getCurrentVersion();
            buffer.clear();
            buffer.putLong( logVersion );
            long lastTxId = xaTf.getLastCommittedTx();
            buffer.putLong( lastTxId );
            previousLogLastCommittedTx = lastTxId;
            buffer.flip();
            fileChannel.write( buffer );
            scanIsComplete = true;
        }
    }

    public boolean scanIsComplete()
    {
        return scanIsComplete;
    }

    private int getNextIdentifier()
    {
        nextIdentifier++;
        if ( nextIdentifier < 0 )
        {
            nextIdentifier = 1;
        }
        return nextIdentifier;
    }

    // returns identifier for transaction
    // [TX_START][xid[gid.length,bid.lengh,gid,bid]][identifier][format id]
    public synchronized int start( Xid xid ) throws XAException
    {
        if ( backupSlave )
        {
            throw new XAException( "Resource is configured as backup slave, " +
                "no new transactions can be started for " + fileName + "." +
                currentLog );
        }
        int xidIdent = getNextIdentifier();
        try
        {
            byte globalId[] = xid.getGlobalTransactionId();
            byte branchId[] = xid.getBranchQualifier();
            int formatId = xid.getFormatId();
            long position = writeBuffer.getFileChannelPosition();
            writeBuffer.put( LogEntry.TX_START ).put( (byte) globalId.length ).put(
                (byte) branchId.length ).put( globalId ).put( branchId )
                .putInt( xidIdent ).putInt( formatId );
            xidIdentMap.put( xidIdent,
                    new LogEntry.Start( xid, xidIdent, position ) );
        }
        catch ( IOException e )
        {
            throw new XAException( "Logical log couldn't start transaction: "
                + e );
        }
        return xidIdent;
    }

    // [TX_PREPARE][identifier]
    public synchronized void prepare( int identifier ) throws XAException
    {
        assert xidIdentMap.get( identifier ) != null;
        try
        {
            writeBuffer.put( LogEntry.TX_PREPARE ).putInt( identifier );
            writeBuffer.force();
        }
        catch ( IOException e )
        {
            throw new XAException( "Logical log unable to mark prepare ["
                + identifier + "] " + e );
        }
    }

    // [TX_1P_COMMIT][identifier]
    public synchronized void commitOnePhase( int identifier, long txId )
        throws XAException
    {
        assert xidIdentMap.get( identifier ) != null;
        assert txId != -1;
        try
        {
            writeBuffer.put( LogEntry.TX_1P_COMMIT ).putInt(
                identifier ).putLong( txId );
            writeBuffer.force();
        }
        catch ( IOException e )
        {
            throw new XAException( "Logical log unable to mark 1P-commit ["
                + identifier + "] " + e );
        }
    }


    // [DONE][identifier]
    public synchronized void done( int identifier ) throws XAException
    {
        if ( backupSlave )
        {
            return;
        }
        assert xidIdentMap.get( identifier ) != null;
        try
        {
            writeBuffer.put( LogEntry.DONE ).putInt( identifier );
            xidIdentMap.remove( identifier );
        }
        catch ( IOException e )
        {
            throw new XAException( "Logical log unable to mark as done ["
                + identifier + "] " + e );
        }
    }

    // [DONE][identifier] called from XaResourceManager during internal recovery
    synchronized void doneInternal( int identifier ) throws IOException
    {
        buffer.clear();
        buffer.put( LogEntry.DONE ).putInt( identifier );
        buffer.flip();
        fileChannel.write( buffer );
        xidIdentMap.remove( identifier );
    }

    // [TX_2P_COMMIT][identifier]
    public synchronized void commitTwoPhase( int identifier, long txId )
        throws XAException
    {
        assert xidIdentMap.get( identifier ) != null;
        assert txId != -1;
        try
        {
            writeBuffer.put( LogEntry.TX_2P_COMMIT ).putInt(
                identifier ).putLong( txId );
            writeBuffer.force();
        }
        catch ( IOException e )
        {
            throw new XAException( "Logical log unable to mark 2PC ["
                + identifier + "] " + e );
        }
    }

    // [COMMAND][identifier][COMMAND_DATA]
    public synchronized void writeCommand( XaCommand command, int identifier )
        throws IOException
    {
        checkLogRotation();
        assert xidIdentMap.get( identifier ) != null;
        writeBuffer.put( LogEntry.COMMAND ).putInt( identifier );
        command.writeToFile( writeBuffer ); // fileChannel, buffer );
    }

    private void applyEntry( LogEntry entry ) throws IOException
    {
        if ( entry instanceof LogEntry.Start )
        {
            applyStartEntry( (LogEntry.Start) entry );
        }
        else if ( entry instanceof LogEntry.Prepare )
        {
            applyPrepareEntry( (LogEntry.Prepare ) entry );
        }
        else if ( entry instanceof LogEntry.Command )
        {
            applyCommandEntry( (LogEntry.Command ) entry );
        }
        else if ( entry instanceof LogEntry.OnePhaseCommit )
        {
            applyOnePhaseCommitEntry( (LogEntry.OnePhaseCommit ) entry );
        }
        else if ( entry instanceof LogEntry.TwoPhaseCommit )
        {
            applyTwoPhaseCommitEntry( (LogEntry.TwoPhaseCommit ) entry );
        }
        else if ( entry instanceof LogEntry.Done )
        {
            applyDoneEntry( (LogEntry.Done ) entry );
        }
    }

    private void applyStartEntry( LogEntry.Start entry) throws IOException
    {
        int identifier = entry.getIdentifier();
        if ( identifier >= nextIdentifier )
        {
            nextIdentifier = (identifier + 1);
        }
        // re-create the transaction
        Xid xid = entry.getXid();
        xidIdentMap.put( identifier, entry );
        XaTransaction xaTx = xaTf.create( identifier );
        xaTx.setRecovered();
        recoveredTxMap.put( identifier, xaTx );
        xaRm.injectStart( xid, xaTx );
    }


    private void applyPrepareEntry( LogEntry.Prepare prepareEntry ) throws IOException
    {
        // get the tx identifier
        int identifier = prepareEntry.getIdentifier();
        LogEntry.Start entry = xidIdentMap.get( identifier );
        if ( entry == null )
        {
            throw new IOException( "Unknown xid for identifier " + identifier );
        }
        Xid xid = entry.getXid();
        if ( xaRm.injectPrepare( xid ) )
        {
            // read only we can remove
            xidIdentMap.remove( identifier );
            recoveredTxMap.remove( identifier );
        }
    }

    private void applyOnePhaseCommitEntry( LogEntry.OnePhaseCommit commit )
        throws IOException
    {
        int identifier = commit.getIdentifier();
        long txId = commit.getTxId();
        LogEntry.Start entry = xidIdentMap.get( identifier );
        if ( entry == null )
        {
            throw new IOException( "Unknown xid for identifier " + identifier );
        }
        Xid xid = entry.getXid();
        try
        {
            XaTransaction xaTx = xaRm.getXaTransaction( xid );
            xaTx.setCommitTxId( txId );
            xaRm.injectOnePhaseCommit( xid );
        }
        catch ( XAException e )
        {
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
    }

    private void applyDoneEntry( LogEntry.Done done ) throws IOException
    {
        // get the tx identifier
        int identifier = done.getIdentifier();
        LogEntry.Start entry = xidIdentMap.get( identifier );
        if ( entry == null )
        {
            throw new IOException( "Unknown xid for identifier " + identifier );
        }
        Xid xid = entry.getXid();
        xaRm.pruneXid( xid );
        xidIdentMap.remove( identifier );
        recoveredTxMap.remove( identifier );
    }

    private void applyTwoPhaseCommitEntry( LogEntry.TwoPhaseCommit commit ) throws IOException
    {
        int identifier = commit.getIdentifier();
        long txId = commit.getTxId();
        LogEntry.Start entry = xidIdentMap.get( identifier );
        if ( entry == null )
        {
            throw new IOException( "Unknown xid for identifier " + identifier );
        }
        Xid xid = entry.getXid();
        if ( xid == null )
        {
            throw new IOException( "Xid null for identifier " + identifier );
        }
        try
        {
            XaTransaction xaTx = xaRm.getXaTransaction( xid );
            xaTx.setCommitTxId( txId );
            xaRm.injectTwoPhaseCommit( xid );
        }
        catch ( XAException e )
        {
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
    }

    private void applyCommandEntry( LogEntry.Command entry ) throws IOException
    {
        int identifier = entry.getIdentifier();
        XaCommand command = entry.getXaCommand();
        if ( command == null )
        {
            throw new IOException( "Null command for identifier " + identifier );
        }
        command.setRecovered();
        XaTransaction xaTx = recoveredTxMap.get( identifier );
        xaTx.injectCommand( command );
    }

    private void checkLogRotation() throws IOException
    {
        if ( autoRotate &&
            writeBuffer.getFileChannelPosition() >= rotateAtSize )
        {
            long currentPos = writeBuffer.getFileChannelPosition();
            long firstStartEntry = getFirstStartEntry( currentPos );
            // only rotate if no huge tx is running
            if ( ( currentPos - firstStartEntry ) < rotateAtSize / 2 )
            {
                rotate();
            }
        }
    }

    private void renameCurrentLogFileAndIncrementVersion( String logFileName,
        long endPosition ) throws IOException
    {
//        System.out.println( " ---- Performing clean close on " + logFileName + " -----" );
//        DumpLogicalLog.main( new String[] { logFileName } );
//        System.out.println( " ----- end ----" );
        File file = new File( logFileName );
        if ( !file.exists() )
        {
            throw new IOException( "Logical log[" + logFileName +
                "] not found" );
        }
        String newName = fileName + ".v" + xaTf.getAndSetNewVersion();
        File newFile = new File( newName );
        boolean renamed = FileUtils.renameFile( file, newFile );

        if ( !renamed )
        {
            throw new IOException( "Failed to rename log to: " + newName );
        }
        else
        {
            try
            {
                FileChannel channel = new RandomAccessFile( newName,
                    "rw" ).getChannel();
                FileUtils.truncateFile( channel, endPosition );
            }
            catch ( IOException e )
            {
                log.log( Level.WARNING,
                    "Failed to truncate log at correct size", e );
            }
        }
//        System.out.println( " ---- Created " + newName + " -----" );
//        DumpLogicalLog.main( new String[] { newName } );
//        System.out.println( " ----- end ----" );
    }

    private void deleteCurrentLogFile( String logFileName ) throws IOException
    {
        File file = new File( logFileName );
        if ( !file.exists() )
        {
            throw new IOException( "Logical log[" + logFileName +
                "] not found" );
        }
        boolean deleted = FileUtils.deleteFile( file );
        if ( !deleted )
        {
            log.warning( "Unable to delete clean logical log[" + logFileName +
                "]" );
        }
    }

    private void releaseCurrentLogFile() throws IOException
    {
        if ( writeBuffer != null )
        {
            writeBuffer.force();
            writeBuffer = null;
        }
        fileChannel.close();
        fileChannel = null;
    }

    public synchronized void close() throws IOException
    {
        if ( fileChannel == null || !fileChannel.isOpen() )
        {
            log.fine( "Logical log: " + fileName + " already closed" );
            return;
        }
        long endPosition = writeBuffer.getFileChannelPosition();
        if ( xidIdentMap.size() > 0 )
        {
            log.info( "Close invoked with " + xidIdentMap.size() +
                " running transaction(s). " );
            writeBuffer.force();
            writeBuffer = null;
            fileChannel.close();
            log.info( "Dirty log: " + fileName + "." + currentLog +
                " now closed. Recovery will be started automatically next " +
                "time it is opened." );
            return;
        }
        releaseCurrentLogFile();
        char logWas = currentLog;
        if ( currentLog != CLEAN ) // again special case, see above
        {
            setActiveLog( CLEAN );
        }
        if ( !keepLogs || backupSlave )
        {
            if ( logWas == CLEAN )
            {
                // special case going from old xa version with no log rotation
                // and we started with a recovery
                deleteCurrentLogFile( fileName );
            }
            else
            {
                deleteCurrentLogFile( fileName + "." + logWas );
            }
        }
        else
        {
            renameCurrentLogFileAndIncrementVersion( fileName + "." +
                logWas, endPosition );
        }
    }

    private void doInternalRecovery( String logFileName ) throws IOException
    {
        log.info( "Non clean shutdown detected on log [" + logFileName +
            "]. Recovery started ..." );
        // get log creation time
        buffer.clear();
        buffer.limit( 16 );
        if ( fileChannel.read( buffer ) != 16 )
        {
            log.info( "Unable to read timestamp information, "
                + "no records in logical log." );
            fileChannel.close();
            boolean success = FileUtils.renameFile( new File( logFileName ),
                new File( logFileName + "_unknown_timestamp_" +
                    System.currentTimeMillis() + ".log" ) );
            assert success;
            fileChannel = new RandomAccessFile( logFileName,
                "rw" ).getChannel();
            return;
        }
        buffer.flip();
        logVersion = buffer.getLong();
        long lastCommittedTx = buffer.getLong();
        previousLogLastCommittedTx = lastCommittedTx;
        log.fine( "Logical log version: " + logVersion + " with committed tx[" +
            lastCommittedTx + "]" );
        long logEntriesFound = 0;
        long lastEntryPos = fileChannel.position();
        LogEntry entry;
        while ( (entry = readEntry()) != null )
        {
            applyEntry( entry );
            logEntriesFound++;
            lastEntryPos = fileChannel.position();
        }
        // make sure we overwrite any broken records
        fileChannel.position( lastEntryPos );
        // zero out the slow way since windows don't support truncate very well
        buffer.clear();
        while ( buffer.hasRemaining() )
        {
            buffer.put( (byte)0 );
        }
        buffer.flip();
        long endPosition = fileChannel.size();
        do
        {
            long bytesLeft = fileChannel.size() - fileChannel.position();
            if ( bytesLeft < buffer.capacity() )
            {
                buffer.limit( (int) bytesLeft );
            }
            fileChannel.write( buffer );
            buffer.flip();
        } while ( fileChannel.position() < endPosition );
        fileChannel.position( lastEntryPos );
        scanIsComplete = true;
        log.fine( "Internal recovery completed, scanned " + logEntriesFound
            + " log entries." );
        xaRm.checkXids();
        if ( xidIdentMap.size() == 0 )
        {
            log.fine( "Recovery completed." );
        }
        else
        {
            log.fine( "[" + logFileName + "] Found " + xidIdentMap.size()
                + " prepared 2PC transactions." );
            for ( LogEntry.Start startEntry : xidIdentMap.values() )
            {
                log.fine( "[" + logFileName + "] 2PC xid[" +
                    startEntry.getXid() + "]" );
            }
        }
        recoveredTxMap.clear();
    }

    // for testing, do not use!
    void reset()
    {
        xidIdentMap.clear();
        recoveredTxMap.clear();
    }

    private LogEntry readEntry() throws IOException
    {
        long position = fileChannel.position();
        LogEntry entry = LogIoUtils.readEntry( buffer, fileChannel, cf );
        if ( entry instanceof LogEntry.Start )
        {
            ((LogEntry.Start) entry).setStartPosition( position );
        }
        return entry;
    }

    private ArrayMap<Thread,Integer> txIdentMap =
        new ArrayMap<Thread,Integer>( 5, true, true );

    void registerTxIdentifier( int identifier )
    {
        txIdentMap.put( Thread.currentThread(), identifier );
    }

    void unregisterTxIdentifier()
    {
        txIdentMap.remove( Thread.currentThread() );
    }

    /**
     * If the current thread is committing a transaction the identifier of that
     * {@link XaTransaction} can be obtained invoking this method.
     *
     * @return the identifier of the transaction committing or <CODE>-1</CODE>
     *         if current thread isn't committing any transaction
     */
    public int getCurrentTxIdentifier()
    {
        Integer intValue = txIdentMap.get( Thread.currentThread() );
        if ( intValue != null )
        {
            return intValue;
        }
        return -1;
    }

    public ReadableByteChannel getLogicalLog( long version ) throws IOException
    {
        String name = fileName + ".v" + version;
        if ( !new File( name ).exists() )
        {
            throw new IOException( "No such log version:" + version );
        }
        return new RandomAccessFile( name, "r" ).getChannel();
    }

    private List<LogEntry> extractPreparedTransactionFromLog( long identifier,
            ReadableByteChannel log ) throws IOException
    {
        buffer.clear();
        buffer.limit( 16 );
        log.read( buffer );
        List<LogEntry> logEntryList = new ArrayList<LogEntry>();
        LogEntry entry;
        while ( (entry = LogIoUtils.readEntry( buffer, log, cf )) != null )
        {
            if ( entry.getIdentifier() != identifier )
            {
                continue;
            }
            if ( entry instanceof LogEntry.Start || entry instanceof LogEntry.Command )
            {
                logEntryList.add( entry );
            }
            else
            {
                throw new RuntimeException( "Expected start or command entry but found: " + entry );
            }
        }
        if ( logEntryList.isEmpty() )
        {
            throw new IOException( "Transaction for internal identifier[" + identifier +
                    "] not found in current log" );
        }
        return logEntryList;
    }

    private List<LogEntry> extractTransactionFromLog( long txId,
            long expectedVersion, ReadableByteChannel log ) throws IOException
    {
        buffer.clear();
        buffer.limit( 16 );
        log.read( buffer );
        buffer.flip();
        long versionInLog = buffer.getLong();
        assertExpectedVersion( expectedVersion, versionInLog );
        long prevTxId = buffer.getLong();
        assertLogCanContainTx( txId, prevTxId );
        List<LogEntry> logEntryList = null;
        Map<Integer,List<LogEntry>> transactions =
            new HashMap<Integer,List<LogEntry>>();
        LogEntry entry;
        while ( (entry = LogIoUtils.readEntry( buffer, log, cf )) != null &&
                logEntryList == null )
        {
            if ( entry instanceof LogEntry.Start )
            {
                List<LogEntry> list = new LinkedList<LogEntry>();
                list.add( entry );
                transactions.put( entry.getIdentifier(), list );
            }
            else if ( entry instanceof LogEntry.Commit )
            {
                if ( ((LogEntry.Commit) entry).getTxId() == txId )
                {
                    logEntryList = transactions.get( entry.getIdentifier() );
                    logEntryList.add( entry );
                }
                else
                {
                    transactions.remove( entry.getIdentifier() );
                }
            }
            else if ( entry instanceof LogEntry.Command )
            {
                transactions.get( entry.getIdentifier() ).add( entry );
            }
            else if ( entry instanceof LogEntry.Done )
            {
                transactions.remove( entry.getIdentifier() );
            }
            else
            {
                throw new RuntimeException( "Unknown entry: " + entry );
            }
        }
        if ( logEntryList == null )
        {
            throw new IOException( "Transaction[" + txId +
                    "] not found in log (" + expectedVersion + ", " +
                    prevTxId + ")" );
        }
        return logEntryList;
    }

    private void assertLogCanContainTx( long txId, long prevTxId ) throws IOException
    {
        if ( prevTxId >= txId )
        {
            throw new IOException( "Log says " + txId +
                    " can not exist in this log (prev tx id=" + prevTxId + ")" );
        }
    }

    private void assertExpectedVersion( long expectedVersion, long versionInLog )
            throws IOException
    {
        if ( versionInLog != expectedVersion )
        {
            throw new IOException( "Expected version " + expectedVersion +
                    " but got " + versionInLog );
        }
    }

    private String generateUniqueName( String baseName )
    {
        String tmpName = baseName + "-" + System.currentTimeMillis();
        while ( new File( tmpName ).exists() )
        {
            tmpName = baseName + "-" + System.currentTimeMillis() + "_";
        }
        return tmpName;
    }

    public synchronized ReadableByteChannel getPreparedTransaction( long identifier )
            throws IOException
    {
        String name = fileName + ".ptx_" + identifier;
        File txFile = new File( name );
        if ( txFile.exists() )
        {
            return new RandomAccessFile( name, "r" ).getChannel();
        }

        ReadableByteChannel log = getLogicalLogOrMyself( logVersion );
        List<LogEntry> logEntryList = extractPreparedTransactionFromLog( identifier, log );
        log.close();

        writeOutLogEntryList( logEntryList, name, "temporary-ptx-write-out-" + identifier );
        return new RandomAccessFile( name, "r" ).getChannel();
    }

    private void writeOutLogEntryList( List<LogEntry> logEntryList, String name,
            String tmpNameHint ) throws IOException
    {
        String tmpName = generateUniqueName( tmpNameHint );
        FileChannel txLog = new RandomAccessFile( tmpName, "rw" ).getChannel();
        LogBuffer buf = new DirectMappedLogBuffer( txLog );
        for ( LogEntry entry : logEntryList )
        {
            LogIoUtils.writeLogEntry( entry, buf );
        }
        buf.force();
        txLog.close();
        if ( !new File( tmpName ).renameTo( new File( name ) ) )
        {
            throw new IOException( "Failed to rename " + tmpName + " to " +
                name );
        }
    }

    public synchronized ReadableByteChannel getCommittedTransaction( long txId )
        throws IOException
    {
        // check if written out
        String name = fileName + ".tx_" + txId;
        File txFile = new File( name );
        if ( txFile.exists() )
        {
            return new RandomAccessFile( name, "r" ).getChannel();
        }

        long version = findLogContainingTxId( txId );
        if ( version == -1 )
        {
            throw new RuntimeException( "txId:" + txId + " not found in any logical log " +
            		"(starting at " + logVersion + " and searching backwards" );
        }

        // extract transaction
        ReadableByteChannel log = getLogicalLogOrMyself( version );
        List<LogEntry> logEntryList =
            extractTransactionFromLog( txId, version, log );
        log.close();

        writeOutLogEntryList( logEntryList, name, "temporary-tx-write-out-" + txId );
        ReadableByteChannel result = new RandomAccessFile( name, "r" ).getChannel();
        return result;
    }

    private ReadableByteChannel getLogicalLogOrMyself( long version ) throws IOException
    {
        if ( version < logVersion )
        {
            return getLogicalLog( version );
        }
        else if ( version == logVersion )
        {
            String currentLogName =
                fileName + (currentLog == LOG1 ? ".1" : ".2" );
            return new RandomAccessFile( currentLogName, "r" ).getChannel();
        }
        else
        {
            throw new RuntimeException( "Version[" + version +
                    "] is higher then current log version[" + logVersion + "]" );
        }
    }

    private long findLogContainingTxId( long txId ) throws IOException
    {
        long version = logVersion;
        long committedTx = previousLogLastCommittedTx;
        while ( version >= 0 )
        {
            ReadableByteChannel log = getLogicalLogOrMyself( version );
            ByteBuffer buf = ByteBuffer.allocate( 16 );
            if ( log.read( buf ) != 16 )
            {
                throw new IOException( "Unable to read log version " +
                        version );
            }
            buf.flip();
            long readVersion = buf.getLong();
            if ( readVersion != version )
            {
                throw new IOException( "Got " + readVersion +
                        " from log when expecting " + version );
            }
            committedTx = buf.getLong();
            log.close();
            if ( committedTx <= txId )
            {
                break;
            }
            version--;
        }
        return version;
    }

    public long getLogicalLogLength( long version )
    {
        String name = fileName + ".v" + version;
        File file = new File( name );
        if ( !file.exists() )
        {
            return -1;
        }
        return file.length();
    }

    public boolean hasLogicalLog( long version )
    {
        String name = fileName + ".v" + version;
        return new File( name ).exists();
    }

    public boolean deleteLogicalLog( long version )
    {
        String name = fileName + ".v" + version;
        File file = new File(name );
        if ( file.exists() )
        {
            return FileUtils.deleteFile( file );
        }
        return false;
    }

    public void makeBackupSlave()
    {
        if ( xidIdentMap.size() > 0 )
        {
            throw new IllegalStateException( "There are active transactions" );
        }
        backupSlave = true;
    }

    private class LogApplier
    {
        private final ReadableByteChannel byteChannel;

        private LogEntry.Start startEntry;

        LogApplier( ReadableByteChannel byteChannel )
        {
            this.byteChannel = byteChannel;
        }

        boolean readAndApplyEntry() throws IOException
        {
            LogEntry entry = LogIoUtils.readEntry( buffer, byteChannel, cf );
            if ( entry != null )
            {
                applyEntry( entry );
            }
            return entry != null;
        }

        boolean readAndApplyAndWriteEntry( int newXidIdentifier ) throws IOException
        {
            LogEntry entry = LogIoUtils.readEntry( buffer, byteChannel, cf );
            if ( entry != null )
            {
                entry.setIdentifier( newXidIdentifier );
                if ( entry instanceof LogEntry.Commit )
                {
                    // hack to get done record written after commit record
                    LogIoUtils.writeLogEntry( entry, writeBuffer );
                    applyEntry( entry );
                }
                else
                {
                    if ( entry instanceof LogEntry.Start )
                    {
                        startEntry = (LogEntry.Start) entry;
                    }
                    applyEntry( entry );
                    LogIoUtils.writeLogEntry( entry, writeBuffer );
                }
                return true;
            }
            return false;
        }

    }

    public synchronized void applyLog( ReadableByteChannel byteChannel )
        throws IOException
    {
        if ( !backupSlave )
        {
            throw new IllegalStateException( "This is not a backup slave" );
        }
        if ( xidIdentMap.size() > 0 )
        {
            throw new IllegalStateException( "There are active transactions" );
        }
        buffer.clear();
        buffer.limit( 16 );
        if ( byteChannel.read( buffer ) != 16 )
        {
            throw new IOException( "Unable to read log version" );
        }
        buffer.flip();
        logVersion = buffer.getLong();
        long previousCommittedTx = buffer.getLong();
        if ( logVersion != xaTf.getCurrentVersion() )
        {
            throw new IllegalStateException( "Tried to apply version " +
                logVersion + " but expected version " +
                xaTf.getCurrentVersion() );
        }
        log.fine( "Logical log version: " + logVersion +
            "(previous committed tx=" + previousCommittedTx + ")" );
        long logEntriesFound = 0;
        LogApplier logApplier = new LogApplier( byteChannel );
        while ( logApplier.readAndApplyEntry() )
        {
            logEntriesFound++;
        }
        byteChannel.close();
        xaTf.flushAll();
        xaTf.getAndSetNewVersion();
        xaRm.reset();
        log.info( "Log[" + fileName + "] version " + logVersion +
                " applied successfully." );
    }

    public synchronized void applyTransactionWithoutTxId( ReadableByteChannel byteChannel,
            long nextTxId ) throws IOException
    {
        if ( nextTxId != (xaTf.getLastCommittedTx() + 1) )
        {
            throw new IllegalStateException( "Tried to apply tx " +
                nextTxId + " but expected transaction " +
                (xaTf.getCurrentVersion() + 1) );
        }
        log.fine( "Logical log version: " + logVersion +
            ", committing tx=" + nextTxId + ")" );
//        System.out.println( "applyTxWithoutTxId#start @ pos: " + writeBuffer.getFileChannelPosition() );
        long logEntriesFound = 0;
        LogApplier logApplier = new LogApplier( byteChannel );
        int xidIdent = getNextIdentifier();
        while ( logApplier.readAndApplyAndWriteEntry( xidIdent ) )
        {
            logEntriesFound++;
        }
        byteChannel.close();
        LogEntry.Start entry = logApplier.startEntry;
        if ( entry == null )
        {
            throw new IOException( "Unable to find start entry" );
        }
//        System.out.println( "applyTxWithoutTxId#before 1PC @ pos: " + writeBuffer.getFileChannelPosition() );
        LogEntry.OnePhaseCommit commit = new LogEntry.OnePhaseCommit(
                xidIdent, nextTxId );
        LogIoUtils.writeLogEntry( commit, writeBuffer );
        Xid xid = entry.getXid();
        try
        {
            XaTransaction xaTx = xaRm.getXaTransaction( xid );
            xaTx.setCommitTxId( nextTxId );
            xaRm.commit( xid, true );
        }
        catch ( XAException e )
        {
            e.printStackTrace();
            throw new IOException( e.getMessage() );
        }
//        LogEntry.Done done = new LogEntry.Done( entry.getIdentifier() );
//        LogIoUtils.writeLogEntry( done, writeBuffer );
        // xaTf.setLastCommittedTx( nextTxId ); // done in doCommit
        log.info( "Tx[" + nextTxId + "] " + " applied successfully." );
//        System.out.println( "applyTxWithoutTxId#end @ pos: " + writeBuffer.getFileChannelPosition() );
    }

    public synchronized void applyTransaction( ReadableByteChannel byteChannel )
        throws IOException
    {
//        System.out.println( "applyFullTx#start @ pos: " + writeBuffer.getFileChannelPosition() );
        long logEntriesFound = 0;
        LogApplier logApplier = new LogApplier( byteChannel );
        int xidIdent = getNextIdentifier();
        while ( logApplier.readAndApplyAndWriteEntry( xidIdent ) )
        {
            logEntriesFound++;
        }
        byteChannel.close();
//        System.out.println( "applyFullTx#end @ pos: " + writeBuffer.getFileChannelPosition() );
    }

    public synchronized void rotate() throws IOException
    {
        xaTf.flushAll();
        String newLogFile = fileName + ".2";
        String currentLogFile = fileName + ".1";
        char newActiveLog = LOG2;
        long currentVersion = xaTf.getCurrentVersion();
        String oldCopy = fileName + ".v" + currentVersion;
        if ( currentLog == CLEAN || currentLog == LOG2 )
        {
            newActiveLog = LOG1;
            newLogFile = fileName + ".1";
            currentLogFile = fileName + ".2";
        }
        else
        {
            assert currentLog == LOG1;
        }
        if ( new File( newLogFile ).exists() )
        {
            throw new IOException( "New log file: " + newLogFile +
                " already exist" );
        }
        if ( new File( oldCopy ).exists() )
        {
            throw new IOException( "Copy log file: " + oldCopy +
                " already exist" );
        }
//        System.out.println( " ---- Performing rotate on " + currentLogFile + " -----" );
//        DumpLogicalLog.main( new String[] { currentLogFile } );
//        System.out.println( " ----- end ----" );
        long endPosition = writeBuffer.getFileChannelPosition();
        writeBuffer.force();
        FileChannel newLog = new RandomAccessFile(
            newLogFile, "rw" ).getChannel();
        buffer.clear();
        buffer.putLong( currentVersion + 1 );
        long lastTx = xaTf.getLastCommittedTx();
        buffer.putLong( lastTx ).flip();
        previousLogLastCommittedTx = lastTx;
        if ( newLog.write( buffer ) != 16 )
        {
            throw new IOException( "Unable to write log version to new" );
        }
        fileChannel.position( 0 );
        buffer.clear();
        buffer.limit( 16 );
        if( fileChannel.read( buffer ) != 16 )
        {
            throw new IOException( "Verification of log version failed" );
        }
        buffer.flip();
        long verification = buffer.getLong();
        if ( verification != currentVersion )
        {
            throw new IOException( "Verification of log version failed, " +
                " expected " + currentVersion + " got " + verification );
        }
        if ( xidIdentMap.size() > 0 )
        {
            fileChannel.position( getFirstStartEntry( endPosition ) );
        }
        LogEntry entry;
        while ((entry = LogIoUtils.readEntry( buffer, fileChannel, cf )) != null )
        {
            if ( xidIdentMap.get( entry.getIdentifier() ) != null )
            {
                if ( entry instanceof LogEntry.Start )
                {
                    ((LogEntry.Start) entry).setStartPosition( newLog.position() );
                }
                LogBuffer newLogBuffer = new DirectLogBuffer( newLog, buffer );
                LogIoUtils.writeLogEntry( entry, newLogBuffer );
            }
        }
        newLog.force( false );
        releaseCurrentLogFile();
        setActiveLog( newActiveLog );
        if ( keepLogs )
        {
            renameCurrentLogFileAndIncrementVersion( currentLogFile,
                endPosition );
        }
        else
        {
            deleteCurrentLogFile( currentLogFile );
            xaTf.getAndSetNewVersion();
        }
        if ( xaTf.getCurrentVersion() != ( currentVersion + 1 ) )
        {
            throw new IOException( "version change failed" );
        }
        fileChannel = newLog;
        if ( !useMemoryMapped )
        {
            writeBuffer = new DirectMappedLogBuffer( fileChannel );
        }
        else
        {
            writeBuffer = new MemoryMappedLogBuffer( fileChannel );
        }
    }

    private long getFirstStartEntry( long endPosition )
    {
        long firstEntryPosition = endPosition;
        for ( LogEntry.Start entry : xidIdentMap.values() )
        {
            if ( entry.getStartPosition() < firstEntryPosition )
            {
                assert entry.getStartPosition() > 0;
                firstEntryPosition = entry.getStartPosition();
            }
        }
        return firstEntryPosition;
    }

    private void setActiveLog( char c ) throws IOException
    {
        if ( c != CLEAN && c != LOG1 && c != LOG2 )
        {
            throw new IllegalArgumentException( "Log must be either clean, " +
                "1 or 2" );
        }
        if ( c == currentLog )
        {
            throw new IllegalStateException( "Log should not be equal to " +
                "current " + currentLog );
        }
        ByteBuffer bb = ByteBuffer.wrap( new byte[4] );
        bb.asCharBuffer().put( c ).flip();
        FileChannel fc = new RandomAccessFile( fileName + ".active" ,
            "rw" ).getChannel();
        int wrote = fc.write( bb );
        if ( wrote != 4 )
        {
            throw new IllegalStateException( "Expected to write 4 -> " + wrote );
        }
        fc.force( false );
        fc.close();
        currentLog = c;
    }
    public void setKeepLogs( boolean keep )
    {
        this.keepLogs = keep;
    }

    public boolean isLogsKept()
    {
        return this.keepLogs;
    }

    public void setAutoRotateLogs( boolean autoRotate )
    {
        this.autoRotate = autoRotate;
    }

    public boolean isLogsAutoRotated()
    {
        return this.autoRotate;
    }

    public void setLogicalLogTargetSize( long size )
    {
        this.rotateAtSize = size;
    }

    public long getLogicalLogTargetSize()
    {
        return this.rotateAtSize;
    }

    public String getFileName( long version )
    {
        return fileName + ".v" + version;
    }
}