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
package org.jkiss.dbeaver.ui.editors.content;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.IContentEditorPart;
import org.jkiss.dbeaver.ext.IDataSourceProvider;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.data.*;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.impl.TemporaryContentStorage;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.runtime.RuntimeUtils;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.utils.ContentUtils;
import org.jkiss.utils.CommonUtils;

import java.io.*;

/**
 * ContentEditorInput
 */
public class ContentEditorInput implements IPathEditorInput, IDataSourceProvider
{
    static final Log log = LogFactory.getLog(ContentEditorInput.class);

    private DBDAttributeController valueController;
    private IContentEditorPart[] editorParts;
    private IFile contentFile;
    private boolean contentDetached = false;

    ContentEditorInput(
        DBDAttributeController valueController,
        IContentEditorPart[] editorParts,
        DBRProgressMonitor monitor)
        throws DBException
    {
        this.valueController = valueController;
        this.editorParts = editorParts;
        this.prepareContent(monitor);
    }

    public DBDValueController getValueController()
    {
        return valueController;
    }

    public void refreshContent(DBRProgressMonitor monitor, DBDAttributeController valueController) throws DBException
    {
        this.valueController = valueController;
        this.prepareContent(monitor);
    }

    IContentEditorPart[] getEditors()
    {
        return editorParts;
    }

    @Override
    public boolean exists()
    {
        return false;
    }

    @Override
    public ImageDescriptor getImageDescriptor()
    {
        return DBIcon.TYPE_LOB.getImageDescriptor();
    }

    @Override
    public String getName()
    {
        String tableName = valueController.getAttributeMetaData().getTableName();
        String inputName = CommonUtils.isEmpty(tableName) ?
            valueController.getAttributeMetaData().getName() :
            tableName + "." + valueController.getAttributeMetaData().getName();
        if (isReadOnly()) {
            inputName += " [Read Only]";
        }
        return inputName;
    }

    @Override
    public IPersistableElement getPersistable()
    {
        return null;
    }

    @Override
    public String getToolTipText()
    {
        return getName();
    }

    @Override
    public Object getAdapter(Class adapter)
    {
        return null;
    }

    private DBDContent getContent()
        throws DBCException
    {
        Object value = valueController.getValue();
        if (value instanceof DBDContent) {
            return (DBDContent)value;
        } else {
            throw new DBCException("Value do not support streaming");
        }
    }

    private void prepareContent(DBRProgressMonitor monitor)
        throws DBException
    {
        DBDContent content = getContent();
        DBDContentStorage storage = content.getContents(monitor);

        release(monitor);
        if (storage instanceof DBDContentStorageLocal) {
            // User content's storage directly
            contentFile = ((DBDContentStorageLocal)storage).getDataFile();
            contentDetached = true;
        } else {
            // Copy content to local file
            try {
                // Create file
                contentFile = ContentUtils.createTempContentFile( monitor, valueController.getColumnId());

                // Write value to file
                if (!content.isNull()) {
                    copyContentToFile(content, monitor);
                }
            }
            catch (IOException e) {
                // Delete temp file
                if (contentFile != null && contentFile.exists()) {
                    try {
                        contentFile.delete(true, false, monitor.getNestedMonitor());
                    }
                    catch (CoreException e1) {
                        log.warn("Could not delete temporary content file", e);
                    }
                }
                throw new DBException(e);
            }
        }

        // Mark file as readonly
        if (valueController.isReadOnly()) {
            ResourceAttributes attributes = contentFile.getResourceAttributes();
            if (attributes != null) {
                attributes.setReadOnly(true);
                try {
                    contentFile.setResourceAttributes(attributes);
                }
                catch (CoreException e) {
                    throw new DBException(e);
                }
            }
        }
    }

    void release(DBRProgressMonitor monitor)
    {
        if (contentFile != null && !contentDetached) {
            ContentUtils.deleteTempFile(monitor, contentFile);
            contentDetached = true;
        }
    }

    public IFile getFile() {
        return contentFile;
    }

    public IStorage getStorage()
        throws CoreException
    {
        return contentFile;
    }

    @Override
    public IPath getPath()
    {
        return contentFile == null ? null : contentFile.getLocation();
    }

    public boolean isReadOnly() {
        return valueController.isReadOnly();
    }

    void saveToExternalFile(File file, IProgressMonitor monitor)
        throws CoreException
    {
        try {
            ContentUtils.saveContentToFile(
                contentFile.getContents(true),
                file,
                RuntimeUtils.makeMonitor(monitor));
        }
        catch (IOException e) {
            throw new CoreException(RuntimeUtils.makeExceptionStatus(e));
        }
    }

    void loadFromExternalFile(File extFile, IProgressMonitor monitor)
        throws CoreException
    {
        try {
            InputStream inputStream = new FileInputStream(extFile);
            try {
/*
                ResourceAttributes atts = contentFile.getResourceAttributes();
                atts.setReadOnly(false);
                contentFile.setResourceAttributes(atts);
*/

                File intFile = contentFile.getLocation().toFile();
                OutputStream outputStream = new FileOutputStream(intFile);
                try {
                    ContentUtils.copyStreams(inputStream, extFile.length(), outputStream, RuntimeUtils.makeMonitor(monitor));
                }
                finally {
                    outputStream.close();
                }

                // Append zero empty content to trigger content refresh
                contentFile.appendContents(
                    new ByteArrayInputStream(new byte[0]),
                    true,
                    false,
                    monitor);
            }
            finally {
                inputStream.close();
            }
        }
        catch (Throwable e) {
            throw new CoreException(RuntimeUtils.makeExceptionStatus(e));
        }
    }

    private void copyContentToFile(DBDContent contents, DBRProgressMonitor monitor)
        throws DBException, IOException
    {
        DBDContentStorage storage = contents.getContents(monitor);
        if (contents.isNull() || storage == null) {
            log.warn("Could not copy null content");
            return;
        }

        if (ContentUtils.isTextContent(contents)) {
            ContentUtils.copyReaderToFile(monitor, storage.getContentReader(), storage.getContentLength(), storage.getCharset(), contentFile);
        } else {
            ContentUtils.copyStreamToFile(monitor, storage.getContentStream(), storage.getContentLength(), contentFile);
        }
    }

    void updateContentFromFile(IProgressMonitor monitor)
        throws DBException, IOException
    {
        if (valueController.isReadOnly()) {
            throw new DBCException("Could not update read-only value");
        }

        DBRProgressMonitor localMonitor = RuntimeUtils.makeMonitor(monitor);
        DBDContent content = getContent();
        DBDContentStorage storage = content.getContents(localMonitor);
        if (storage instanceof DBDContentStorageLocal) {
            // Nothing to update - we user content's storage
            contentDetached = true;
        } else {
            // Create new storage and pass it to content
            storage = new TemporaryContentStorage(contentFile);
            contentDetached = content.updateContents(localMonitor, storage);
        }
        valueController.updateValue(content);
    }

    ////////////////////////////////////////////////////////
    // IDatabaseEditorInput methods

    @Override
    public DBPDataSource getDataSource() {
        return valueController.getDataSource();
    }

}