/*
 * Copyright (C) 2010-2014 Serge Rieder
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
package org.jkiss.dbeaver.ui.search.data;

import org.jkiss.dbeaver.core.Log;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.DBDAttributeConstraint;
import org.jkiss.dbeaver.model.data.DBDDataFilter;
import org.jkiss.dbeaver.model.data.DBDDataReceiver;
import org.jkiss.dbeaver.model.exec.*;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.model.navigator.DBNModel;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSDataContainer;
import org.jkiss.dbeaver.model.struct.DBSEntity;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;
import org.jkiss.dbeaver.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.ui.search.IObjectSearchListener;
import org.jkiss.dbeaver.ui.search.IObjectSearchQuery;

import java.math.BigDecimal;
import java.util.*;

public class SearchDataQuery implements IObjectSearchQuery {

    static final Log log = Log.getLog(SearchDataQuery.class);

    private final SearchDataParams params;

    private SearchDataQuery(SearchDataParams params)
    {
        this.params = params;
    }

    @Override
    public String getLabel()
    {
        return params.getSearchString();
    }

    @Override
    public void runQuery(DBRProgressMonitor monitor, IObjectSearchListener listener)
        throws DBException
    {
        listener.searchStarted();
        try {
            String searchString = params.getSearchString();

            //monitor.subTask("Collect tables");
            Set<DBPDataSource> dataSources = new HashSet<DBPDataSource>();
            for (DBSDataContainer searcher : params.sources) {
                dataSources.add(searcher.getDataSource());
            }

            // Search
            DBNModel dbnModel = DBNModel.getInstance();

            monitor.beginTask(
                "Search \"" + searchString + "\" in " + params.sources.size() + " table(s) / " + dataSources.size() + " database(s)",
                params.sources.size());
            try {
                for (DBSDataContainer dataContainer : params.sources) {
                    if (monitor.isCanceled()) {
                        break;
                    }
                    String objectName = DBUtils.getObjectFullName(dataContainer);
                    DBNDatabaseNode node = dbnModel.findNode(dataContainer);
                    if (node == null) {
                        log.warn("Can't find tree node for object \"" + objectName + "\"");
                        continue;
                    }
                    monitor.subTask(objectName);
                    SearchTableMonitor searchMonitor = new SearchTableMonitor();
                    DBCSession session = dataContainer.getDataSource().openSession(searchMonitor, DBCExecutionPurpose.UTIL, "Search rows in " + objectName);
                    try {
                        TestDataReceiver dataReceiver = new TestDataReceiver(searchMonitor);
                        findRows(session, dataContainer, dataReceiver);

                        if (dataReceiver.rowCount > 0) {
                            SearchDataObject object = new SearchDataObject(node, dataReceiver.rowCount, dataReceiver.filter);
                            listener.objectsFound(monitor, Collections.singleton(object));
                        }
                    } catch (DBCException e) {
                        log.error("Error searching string in '" + objectName + "'", e);
                    } finally {
                        session.close();
                    }

                    monitor.worked(1);
                }
            } finally {
                monitor.done();
            }
        } finally {
            listener.searchFinished();
        }
    }

    private DBCStatistics findRows(
        @NotNull DBCSession session,
        @NotNull DBSDataContainer dataContainer,
        @NotNull TestDataReceiver dataReceiver) throws DBCException
    {
        DBSEntity entity;
        if (dataContainer instanceof DBSEntity) {
            entity = (DBSEntity) dataContainer;
        } else {
            log.warn("Data container " + dataContainer + " isn't entity");
            return null;
        }
        try {

            List<DBDAttributeConstraint> constraints = new ArrayList<DBDAttributeConstraint>();
            for (DBSEntityAttribute attribute : entity.getAttributes(session.getProgressMonitor())) {
                if (params.fastSearch) {
                    if (!DBUtils.isIndexedAttribute(session.getProgressMonitor(), attribute)) {
                        continue;
                    }
                }
                DBCLogicalOperator operator;
                Object value;
                switch (attribute.getDataKind()) {
                    case NUMERIC:
                        if (!params.searchNumbers) {
                            continue;
                        }
                        operator = DBCLogicalOperator.EQUALS;
                        try {
                            value = new Integer(params.searchString);
                        } catch (NumberFormatException e) {
                            try {
                                value = new Long(params.searchString);
                            } catch (NumberFormatException e1) {
                                try {
                                    value = new Double(params.searchString);
                                } catch (NumberFormatException e2) {
                                    try {
                                        value = new BigDecimal(params.searchString);
                                    } catch (Exception e3) {
                                        value = params.searchString;
                                    }
                                }
                            }
                        }
                        break;
                    case LOB:
                        if (!params.searchLOBs) {
                            continue;
                        }
                    case STRING:
                        operator = DBCLogicalOperator.LIKE;
                        value = "%" + params.searchString + "%";
                        break;
                    default:
                        continue;
                }
                DBDAttributeConstraint constraint = new DBDAttributeConstraint(attribute, constraints.size());
                constraint.setOperator(operator);
                constraint.setValue(value);
                constraints.add(constraint);
            }
            if (constraints.isEmpty()) {
                return null;
            }
            dataReceiver.filter = new DBDDataFilter(constraints);
            dataReceiver.filter.setAnyConstraint(true);
            return dataContainer.readData(session, dataReceiver, dataReceiver.filter, -1, -1, 0);
        } catch (DBException e) {
            throw new DBCException("Error finding rows", e);
        }
    }

    public static SearchDataQuery createQuery(SearchDataParams params)
        throws DBException
    {
        return new SearchDataQuery(params);
    }

    private class SearchTableMonitor extends VoidProgressMonitor {

        private volatile boolean canceled;

        private SearchTableMonitor() {
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }
    }

    private class TestDataReceiver implements DBDDataReceiver {

        private SearchTableMonitor searchMonitor;
        private int rowCount = 0;
        private DBDDataFilter filter;

        public TestDataReceiver(SearchTableMonitor searchMonitor) {
            this.searchMonitor = searchMonitor;
        }

        @Override
        public void fetchStart(DBCSession session, DBCResultSet resultSet, long offset, long maxRows) throws DBCException {

        }

        @Override
        public void fetchRow(DBCSession session, DBCResultSet resultSet) throws DBCException {
            rowCount++;
            if (rowCount >= params.maxResults) {
                searchMonitor.canceled = true;
            }
        }

        @Override
        public void fetchEnd(DBCSession session) throws DBCException {

        }

        @Override
        public void close() {

        }
    }

}