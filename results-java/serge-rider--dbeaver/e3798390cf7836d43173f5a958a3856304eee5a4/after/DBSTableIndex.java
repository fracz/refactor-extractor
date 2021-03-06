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
package org.jkiss.dbeaver.model.struct.rdb;

import org.jkiss.dbeaver.model.DBPQualifiedObject;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraint;
import org.jkiss.dbeaver.model.struct.DBSEntityReferrer;
import org.jkiss.dbeaver.model.struct.DBSObjectContainer;

import java.util.Collection;
import java.util.List;

/**
 * DBSTableIndex
 */
public interface DBSTableIndex extends DBSEntityConstraint, DBSEntityReferrer, DBPQualifiedObject
{
    /**
     * Index container. In complex databases it is schema or catalog where index defined.
     * Also the table can be index container.
     * @return container
     */
    DBSObjectContainer getContainer();

    DBSTable getTable();

    boolean isUnique();

    DBSIndexType getIndexType();

    List<? extends DBSTableIndexColumn> getAttributeReferences(DBRProgressMonitor monitor);

}