/*
 * Copyright (C) 2010-2013 Serge Rieder
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
package org.jkiss.dbeaver.ui.editors.sql.log;

import org.jkiss.dbeaver.model.exec.DBCStatement;
import org.jkiss.dbeaver.runtime.qm.QMEventFilter;
import org.jkiss.dbeaver.runtime.qm.QMMetaEvent;
import org.jkiss.dbeaver.runtime.qm.meta.*;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditor;

/**
 * SQL log filter
 */
class SQLLogFilter implements QMEventFilter {

    private SQLEditor editor;

    public SQLLogFilter(SQLEditor editor)
    {
        this.editor = editor;
    }

    @Override
    public boolean accept(QMMetaEvent event)
    {
        // Accept only following events:
        // - statement execution (if statement belongs to specific editor)
        // - transaction/savepoint changes (if txn belongs to current datasource)
        // - session changes (if session belongs to active datasource)
        QMMObject object = event.getObject();
        if (object instanceof QMMSessionInfo) {
            return ((QMMSessionInfo)object).getContainer() == editor.getDataSourceContainer();
        } else {
            if (object instanceof QMMStatementExecuteInfo) {
                DBCStatement statement = ((QMMStatementExecuteInfo) object).getStatement().getReference();
                return statement != null && statement.getSource() == editor;
            } else if (object instanceof QMMStatementInfo) {
                DBCStatement statement = ((QMMStatementInfo) object).getReference();
                return statement != null && statement.getSource() == editor;
            } else if (object instanceof QMMTransactionInfo) {
                return ((QMMTransactionInfo)object).getSession().getReference() == editor.getDataSource();
            } else if (object instanceof QMMTransactionSavepointInfo) {
                return ((QMMTransactionSavepointInfo)object).getTransaction().getSession().getReference() == editor.getDataSource();
            }
        }
        return false;
    }

}