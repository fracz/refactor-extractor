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
package org.jkiss.dbeaver.ext.mssql.model;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.Top;
import org.jkiss.dbeaver.core.Log;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCQueryTransformer;
import org.jkiss.dbeaver.model.exec.DBCStatement;
import org.jkiss.utils.CommonUtils;

/**
* Query transformer for TOP
*/
public class QueryTransformerTop implements DBCQueryTransformer {

    static final Log log = Log.getLog(QueryTransformerTop.class);

    private Number offset;
    private Number length;
    private boolean limitSet;

    @Override
    public void setParameters(Object... parameters) {
        this.offset = (Number) parameters[0];
        this.length = (Number) parameters[1];
    }

    @Override
    public String transformQueryString(String query) throws DBCException {
        limitSet = false;
        try {
            Statement statement = CCJSqlParserUtil.parse(query);
            if (statement instanceof Select) {
                Select select = (Select) statement;
                if (select.getSelectBody() instanceof PlainSelect) {
                    PlainSelect selectBody = (PlainSelect) select.getSelectBody();
                    if (selectBody.getTop() == null && CommonUtils.isEmpty(selectBody.getIntoTables())) {
                        Top top = new Top();
                        top.setPercentage(false);
                        top.setRowCount(offset.longValue() + length.longValue());
                        selectBody.setTop(top);

                        limitSet = true;
                        return statement.toString();
                    }
                }
            }
        } catch (Throwable e) {
            // ignore
            log.debug(e);
        }
        return query;
    }

    @Override
    public void transformStatement(DBCStatement statement, int parameterIndex) throws DBCException {
        if (!limitSet) {
            statement.setLimit(offset.longValue(), length.longValue());
        }
    }
}