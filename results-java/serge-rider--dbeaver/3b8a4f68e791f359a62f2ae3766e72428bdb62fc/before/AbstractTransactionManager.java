/*
 * Copyright (C) 2010-2015 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.model.impl;

import org.jkiss.dbeaver.model.DBPTransactionIsolation;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCSavepoint;
import org.jkiss.dbeaver.model.exec.DBCTransactionManager;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

/**
* AbstractTransactionManager
*/
public class AbstractTransactionManager implements DBCTransactionManager {

    @Override
    public DBPTransactionIsolation getTransactionIsolation()
        throws DBCException
    {
        return null;
    }

    @Override
    public void setTransactionIsolation(DBRProgressMonitor monitor, DBPTransactionIsolation transactionIsolation)
        throws DBCException
    {
        throw new DBCException("Transaction isolation change not supported");
    }

    @Override
    public boolean isAutoCommit()
        throws DBCException
    {
        return true;
    }

    @Override
    public void setAutoCommit(DBRProgressMonitor monitor, boolean autoCommit)
        throws DBCException
    {
        if (!autoCommit) {
            throw new DBCException("Transactional mode not supported");
        }
    }

    @Override
    public boolean supportsSavepoints()
    {
        return false;
    }

    @Override
    public DBCSavepoint setSavepoint(DBRProgressMonitor monitor, String name)
        throws DBCException
    {
        throw new DBCException("Savepoint not supported");
    }

    @Override
    public void releaseSavepoint(DBRProgressMonitor monitor, DBCSavepoint savepoint)
        throws DBCException
    {
        throw new DBCException("Savepoint not supported");
    }

    @Override
    public void commit(DBRProgressMonitor monitor)
        throws DBCException
    {
        // do nothing
    }

    @Override
    public void rollback(DBRProgressMonitor monitor, DBCSavepoint savepoint)
        throws DBCException
    {
        throw new DBCException("Transactions not supported");
    }
}