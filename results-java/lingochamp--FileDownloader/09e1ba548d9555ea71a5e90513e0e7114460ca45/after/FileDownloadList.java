/*
 * Copyright (c) 2015 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liulishuo.filedownloader;

import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.util.FileDownloadLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jacksgong on 12/21/15.
 * <p/>
 * Storing all tasks in processing in the Main-Process.
 */
public class FileDownloadList {


    private final static class HolderClass {
        private final static FileDownloadList INSTANCE = new FileDownloadList();
    }

    public static FileDownloadList getImpl() {
        return HolderClass.INSTANCE;
    }

    private final ArrayList<BaseDownloadTask> list;

    private FileDownloadList() {
        list = new ArrayList<>();
    }

    boolean isEmpty() {
        return list.isEmpty();
    }

    int size() {
        return list.size();
    }

    /**
     * @param id download id
     * @return get counts os same id
     */
    int count(final int id) {
        int size = 0;
        synchronized (list) {
            for (BaseDownloadTask baseDownloadTask : list) {
                if (baseDownloadTask.getDownloadId() == id) {
                    size++;
                }
            }
        }
        return size;
    }

    public BaseDownloadTask get(final int id) {
        synchronized (list) {
            for (BaseDownloadTask baseDownloadTask : list) {
                // when FileDownloadMgr#checkDownloading
                if (baseDownloadTask.getDownloadId() == id) {
                    return baseDownloadTask;
                }
            }
        }
        return null;
    }

    List<BaseDownloadTask> getList(final int id) {
        final List<BaseDownloadTask> list = new ArrayList<>();
        synchronized (this.list) {
            for (BaseDownloadTask baseDownloadTask : this.list) {
                if (baseDownloadTask.getDownloadId() == id) {
                    list.add(baseDownloadTask);
                }
            }
        }

        return list;
    }

    boolean contains(final BaseDownloadTask download) {
        return list.contains(download);
    }

    List<BaseDownloadTask> copy(final FileDownloadListener listener) {
        final List<BaseDownloadTask> targetList = new ArrayList<>();
        synchronized (list) {
            // Prevent size changing
            for (BaseDownloadTask task : list) {
                if (task.getListener() == listener) {
                    targetList.add(task);
                }
            }
            return targetList;
        }
    }

    BaseDownloadTask[] copy() {
        synchronized (list) {
            // Prevent size changing
            BaseDownloadTask[] copy = new BaseDownloadTask[list.size()];
            return list.toArray(copy);
        }
    }

    /**
     * Divert all data in list 2 destination list
     */
    void divert(final List<BaseDownloadTask> destination) {
        synchronized (list) {
            synchronized (destination) {
                destination.addAll(list);
            }

            list.clear();
        }
    }

    boolean removeByWarn(final BaseDownloadTask willRemoveDownload) {
        return remove(willRemoveDownload, FileDownloadStatus.warn);
    }

    boolean removeByError(final BaseDownloadTask willRemoveDownload) {
        return remove(willRemoveDownload, FileDownloadStatus.error);
    }

    boolean removeByPaused(final BaseDownloadTask willRemoveDownload) {
        return remove(willRemoveDownload, FileDownloadStatus.paused);
    }

    boolean removeByCompleted(final BaseDownloadTask willRemoveDownload) {
        return remove(willRemoveDownload, FileDownloadStatus.completed);
    }

    /**
     * @param willRemoveDownload will be remove
     * @param removeByStatus     must remove by status {@link com.liulishuo.filedownloader.model.FileDownloadStatus#warn}
     *                           {@link com.liulishuo.filedownloader.model.FileDownloadStatus#paused}
     *                           {@link com.liulishuo.filedownloader.model.FileDownloadStatus#completed}
     *                           {@link com.liulishuo.filedownloader.model.FileDownloadStatus#error}
     */
    boolean remove(final BaseDownloadTask willRemoveDownload, final byte removeByStatus) {
        boolean succeed;
        synchronized (list) {
            succeed = list.remove(willRemoveDownload);
        }
        if (FileDownloadLog.NEED_LOG) {
            if (list.size() == 0) {
                FileDownloadLog.v(this, "remove %s left %d %d", willRemoveDownload, removeByStatus, list.size());
            }
        }

        if (succeed) {
            // Notify 2 Listener
            switch (removeByStatus) {
                case FileDownloadStatus.warn:
                    willRemoveDownload.getMessenger().notifyWarn();
                    break;
                case FileDownloadStatus.error:
                    willRemoveDownload.getMessenger().notifyError();
                    break;
                case FileDownloadStatus.paused:
                    willRemoveDownload.getMessenger().notifyPaused();
                    break;
                case FileDownloadStatus.completed:
                    Throwable ex = null;
                    try {
                        willRemoveDownload.getMessenger().notifyBlockComplete();
                    } catch (Throwable e) {
                        ex = e;
                    }

                    if (ex != null) {
                        willRemoveDownload.catchException(ex);
                        willRemoveDownload.getMessenger().notifyError();
                    } else {
                        willRemoveDownload.getMessenger().notifyCompleted();
                    }
                    break;
            }

        } else {
            FileDownloadLog.e(this, "remove error, not exist: %s", willRemoveDownload);
        }

        return succeed;
    }

    void add(final BaseDownloadTask downloadInternal) {
        ready(downloadInternal);

        // Notify 2 Listener
        downloadInternal.getMessenger().notifyBegin();
    }

    void ready(final BaseDownloadTask task) {
        if (task.isMarkedAdded2List()) {
            return;
        }

        synchronized (list) {
            if (list.contains(task)) {
                FileDownloadLog.w(this, "already has %s", task);
            } else {
                task.markAdded2List();
                list.add(task);
                if (FileDownloadLog.NEED_LOG) {
                    FileDownloadLog.v(this, "add list in all %s %d %d", task, task.getStatus(), list.size());
                }
            }
        }
    }
}