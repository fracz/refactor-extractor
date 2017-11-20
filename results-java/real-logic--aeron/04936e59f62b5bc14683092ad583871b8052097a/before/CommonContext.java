/*
 * Copyright 2014 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.aeron.util;

import java.io.File;
import java.io.IOException;

import static java.lang.Boolean.getBoolean;
import static java.lang.Integer.getInteger;
import static java.lang.System.getProperty;

/**
 * Location of context that is common between the client and the media driver.
 */
public class CommonContext implements AutoCloseable
{
    /** Directory of the data buffers */
    public static final String DATA_DIR_PROP_NAME = "aeron.dir.data";
    /** Default directory for data buffers */
    public static final String DATA_DIR_PROP_DEFAULT = IoUtil.tmpDirName() + "aeron" + File.separator + "data";

    /** Directory of the conductor buffers */
    public static final String ADMIN_DIR_PROP_NAME = "aeron.dir.conductor";
    /** Default directory for conductor buffers */
    public static final String ADMIN_DIR_PROP_DEFAULT = IoUtil.tmpDirName() + "aeron" + File.separator + "conductor";

    /** Directory for the counters */
    public static final String COUNTERS_DIR_PROP_NAME = "aeron.dir.counters";
    /** Default directory for conductor buffers */
    public static final String COUNTERS_DIR_PROP_DEFAULT = IoUtil.tmpDirName() + "aeron" + File.separator + "counters";

    /** Length of the maximum transport unit of the media driver's protocol */
    private static final String MTU_LENGTH_NAME = "aeron.mtu.length";
    private static final int MTU_LENGTH_DEFAULT = 1280;

    public static final String TO_DRIVER_FILE = "to-driver";
    public static final String TO_CLIENTS_FILE = "to-clients";

    /** Attempt to delete directories on exit */
    public static final String DIRS_DELETE_ON_EXIT_PROP_NAME = "aeron.dir.delete.on.exit";

    private String dataDirName;
    private String adminDirName;
    private String countersDirName;
    private int mtuLength;
    private boolean dirsDeleteOnExit;
    private File toDriverPath;
    private File toClientsPath;

    public CommonContext init() throws IOException
    {
        dataDirName(getProperty(DATA_DIR_PROP_NAME, DATA_DIR_PROP_DEFAULT));
        adminDirName(getProperty(ADMIN_DIR_PROP_NAME, ADMIN_DIR_PROP_DEFAULT));
        countersDirName(getProperty(COUNTERS_DIR_PROP_NAME, COUNTERS_DIR_PROP_DEFAULT));
        mtuLength(getInteger(MTU_LENGTH_NAME, MTU_LENGTH_DEFAULT));
        dirsDeleteOnExit(getBoolean(DIRS_DELETE_ON_EXIT_PROP_NAME));
        toDriverPath(new File(adminDirName(), TO_DRIVER_FILE));
        toClientsPath(new File(adminDirName(), TO_CLIENTS_FILE));

        return this;
    }

    public void validateConfiguration()
    {
        IoUtil.checkDirectoryExists(new File(adminDirName), "adminDir");
    }

    public String dataDirName()
    {
        return dataDirName;
    }

    public CommonContext dataDirName(final String dataDirName)
    {
        this.dataDirName = dataDirName;
        return this;
    }

    public String countersDirName()
    {
        return countersDirName;
    }

    public CommonContext countersDirName(final String countersDirName)
    {
        this.countersDirName = countersDirName;
        return this;
    }

    public int mtuLength()
    {
        return mtuLength;
    }

    public CommonContext mtuLength(final int mtuLength)
    {
        this.mtuLength = mtuLength;
        return this;
    }

    public boolean dirsDeleteOnExit()
    {
        return dirsDeleteOnExit;
    }

    public CommonContext dirsDeleteOnExit(final boolean dirsDeleteOnExit)
    {
        this.dirsDeleteOnExit = dirsDeleteOnExit;
        return this;
    }

    public File toDriverPath()
    {
        return toDriverPath;
    }

    public CommonContext toDriverPath(final File toDriverPath)
    {
        this.toDriverPath = toDriverPath;
        return this;
    }

    public File toClientsPath()
    {
        return toClientsPath;
    }

    public CommonContext toClientsPath(final File toClientsPath)
    {
        this.toClientsPath = toClientsPath;
        return this;
    }

    public CommonContext adminDirName(final String adminDirName)
    {
        this.adminDirName = adminDirName;
        return this;
    }

    public String adminDirName()
    {
        return adminDirName;
    }

    public void close() throws Exception
    {

    }
}