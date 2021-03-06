/*
 * Copyright (C) 2010-2012 Serge Rieder
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

package org.jkiss.dbeaver.model.runtime;

import org.jkiss.dbeaver.DBException;

/**
 * Object which can block execution flow.
 * Such as socket, statement or connection, etc.
 */
public interface DBRBlockingObject {

    /**
     * Cancels block.
     * In actual implementation this object may not block process at the moment of invocation
     * of this methid. Implementor should check object's state and cancel blocking on demand.
     * @throws DBException on error
     */
    void cancelBlock() throws DBException;

}