/*
 * Copyright (c) 2002-2009 "Neo Technology,"
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
package org.neo4j.kernel.impl.transaction;

import java.util.Map;

import javax.transaction.TransactionManager;

import org.neo4j.kernel.impl.transaction.xaframework.XaDataSource;

/**
 * Can reads a XA data source configuration file and registers all the data
 * sources defined there or be used to manually add XA data sources.
 * <p>
 * This module will create a instance of each {@link XaDataSource} once started
 * and will close them once stopped.
 *
 * @see XaDataSourceManager
 */
public class TxModule
{
    private static final String MODULE_NAME = "TxModule";

    private boolean startIsOk = true;
    private String txLogDir = "var/tm";

    private final TransactionManager txManager;
    private final XaDataSourceManager xaDsManager;

    public TxModule( String txLogDir )
    {
        this.txLogDir = txLogDir;
        this.txManager = new TxManager( txLogDir );
        this.xaDsManager = new XaDataSourceManager();
    }

    public TxModule( boolean readOnly )
    {
        if ( readOnly )
        {
            this.txManager = new ReadOnlyTxManager();
            this.xaDsManager = new XaDataSourceManager();
        }
        else
        {
            throw new IllegalStateException( "Read only must be set for this constructor" );
        }
    }

    public void init()
    {
    }

    public void start()
    {
        if ( !startIsOk )
        {
            return;
        }
        if ( txManager instanceof TxManager )
        {
            ((TxManager)txManager).init( xaDsManager );
        }
        else
        {
            ((ReadOnlyTxManager)txManager).init( xaDsManager );
        }
        startIsOk = false;
    }

    public void reload()
    {
        stop();
        start();
    }

    public void stop()
    {
        xaDsManager.unregisterAllDataSources();
        if ( txManager instanceof TxManager )
        {
            ((TxManager)txManager).stop();
        }
        else
        {
            ((ReadOnlyTxManager)txManager).stop();
        }
    }

    public void destroy()
    {
    }

    public String getModuleName()
    {
        return MODULE_NAME;
    }

    /**
     * Use this method to add data source that can participate in transactions
     * if you don't want a data source configuration file.
     *
     * @param name
     *            The data source name
     * @param className
     *            The (full) class name of class
     * @param resourceId
     *            The resource id identifying datasource
     * @param params
     *            The configuration map for the datasource
     * @throws LifecycleException
     */
    public XaDataSource registerDataSource( String dsName, String className,
        byte resourceId[], Map<?,?> params )
    {
        XaDataSourceManager xaDsMgr = xaDsManager;
        String name = dsName.toLowerCase();
        if ( xaDsMgr.hasDataSource( name ) )
        {
            throw new TransactionFailureException( "Data source[" + name
                + "] has already been registered" );
        }
        try
        {
            XaDataSource dataSource = xaDsMgr.create( className, params );
            xaDsMgr.registerDataSource( name, dataSource, resourceId );
            return dataSource;
        }
        catch ( Exception e )
        {
            throw new TransactionFailureException(
                "Could not create data source [" + name
                + "], see nested exception for cause of error", e );
        }
    }

    public XaDataSource registerDataSource( String dsName, String className,
        byte resourceId[], Map<?,?> params, boolean useExisting )
    {
        XaDataSourceManager xaDsMgr = xaDsManager;
        String name = dsName.toLowerCase();
        if ( xaDsMgr.hasDataSource( name ) )
        {
            if ( useExisting )
            {
                return xaDsMgr.getXaDataSource( name );
            }
            throw new TransactionFailureException( "Data source[" + name
                + "] has already been registered" );
        }
        try
        {
            XaDataSource dataSource = xaDsMgr.create( className, params );
            xaDsMgr.registerDataSource( name, dataSource, resourceId );
            return dataSource;
        }
        catch ( Exception e )
        {
            throw new TransactionFailureException(
                "Could not create data source " + name + "[" + name + "]", e );
        }
    }

    public String getTxLogDirectory()
    {
        return txLogDir;
    }

    public TransactionManager getTxManager()
    {
        return txManager;
    }

    public XaDataSourceManager getXaDataSourceManager()
    {
        return xaDsManager;
    }
}