package com.metaweb.gridworks.importers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.metaweb.gridworks.importers.XmlImportUtilities.ImportColumnGroup;
import com.metaweb.gridworks.model.Project;

public class XmlImporter implements Importer {

    final static Logger logger = LoggerFactory.getLogger("XmlImporter");

    public static final int BUFFER_SIZE = 64 * 1024;

    public boolean takesReader() {
        return false;
    }

    public void read(Reader reader, Project project, Properties options)
            throws Exception {

        throw new UnsupportedOperationException();
    }

    public void read(
        InputStream inputStream,
        Project project,
        Properties options
    ) throws Exception {
        logger.trace("XmlImporter.read");
        PushbackInputStream pis = new PushbackInputStream(inputStream,BUFFER_SIZE);

        String[] recordPath = null;
        {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytes_read = 0;
            while (bytes_read < BUFFER_SIZE) {
                int c = pis.read(buffer, bytes_read, BUFFER_SIZE - bytes_read);
                if (c == -1) break;
                bytes_read +=c ;
            }
            pis.unread(buffer, 0, bytes_read);

            if (options.containsKey("importer-record-tag")) {
                recordPath = XmlImportUtilities.detectPathFromTag(
                        new ByteArrayInputStream(buffer, 0, bytes_read),
                        options.getProperty("importer-record-tag"));
            } else {
                recordPath = XmlImportUtilities.detectRecordElement(
                        new ByteArrayInputStream(buffer, 0, bytes_read));
            }
        }

        ImportColumnGroup rootColumnGroup = new ImportColumnGroup();

        XmlImportUtilities.importXml(pis, project, recordPath, rootColumnGroup);
        XmlImportUtilities.createColumnsFromImport(project, rootColumnGroup);

        project.columnModel.update();
    }

    public boolean canImportData(String contentType, String fileName) {
        if (contentType != null) {
            contentType = contentType.toLowerCase().trim();

            if("application/xml".equals(contentType) ||
                      "text/xml".equals(contentType) ||
                      "application/rss+xml".equals(contentType) ||
                      "application/atom+xml".equals(contentType)) {
                return true;
            }
        } else if (fileName != null) {
            fileName = fileName.toLowerCase();
            if (
                    fileName.endsWith(".xml") ||
                    fileName.endsWith(".atom") ||
                    fileName.endsWith(".rss")
                ) {
                return true;
            }
        }
        return false;
    }

}