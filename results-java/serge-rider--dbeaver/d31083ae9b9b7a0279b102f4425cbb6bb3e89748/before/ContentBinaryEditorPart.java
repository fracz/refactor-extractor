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
package org.jkiss.dbeaver.ui.editors.content.parts;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.jkiss.dbeaver.ext.IContentEditorPart;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.ui.editors.binary.BinaryEditor;

import javax.activation.MimeType;

/**
 * LOB Binary Editor
 */
public class ContentBinaryEditorPart extends BinaryEditor implements IContentEditorPart {

    public ContentBinaryEditorPart()
    {
    }

    @Override
    public void initPart(IEditorPart contentEditor, MimeType mimeType)
    {
    }

    @Override
    public IEditorActionBarContributor getActionBarContributor()
    {
        return null;
    }

    @Override
    public String getContentTypeTitle()
    {
        return "Binary";
    }

    @Override
    public Image getContentTypeImage()
    {
        return DBIcon.TYPE_BINARY.getImage();
    }

    @Override
    public String getPreferredMimeType()
    {
        return "application";
    }

    @Override
    public long getMaxContentLength()
    {
        return Long.MAX_VALUE;
    }

    /**
     * Any content is valid for binary editor so always returns true
     * @return
     */
    @Override
    public boolean isPreferredContent()
    {
        return false;
    }

    @Override
    public boolean isOptionalContent()
    {
        return false;
    }

}