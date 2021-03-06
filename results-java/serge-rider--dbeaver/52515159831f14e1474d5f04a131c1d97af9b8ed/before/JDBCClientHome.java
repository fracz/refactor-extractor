/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2015 Serge Rieder (serge@jkiss.org)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jkiss.dbeaver.model.impl.jdbc;

import org.jkiss.dbeaver.model.DBPClientHome;

import java.io.File;

/**
 * JDBCClientHome
 */
public abstract class JDBCClientHome implements DBPClientHome
{
    private final String id;
    private final File path;

    protected JDBCClientHome(String id, String path)
    {
        this.id = id;
        this.path = new File(path);
    }

    @Override
    public String getHomeId()
    {
        return id;
    }

    @Override
    public File getHomePath()
    {
        return path;
    }

}