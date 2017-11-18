/*
 * Copyright (C) 2012 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.greenrobot.dao;

/**
 * An operation that will be enqueued for asynchronous execution.
 *
 * @author Markus
 *
 * @see AsyncSession
 */
public class AsyncOperation {
    public static enum OperationType {
        Insert, InsertInTxIterable, InsertInTxArray, //
        InsertOrReplace, InsertOrReplaceInTxIterable, InsertOrReplaceInTxArray, //
        Update, UpdateInTxIterable, UpdateInTxArray, //
        Delete, DeleteInTxIterable, DeleteInTxArray, //
        TransactionRunnable, TransactionCallable
    }

    public static final int FLAG_MERGE_TX = 1;

    final OperationType type;
    final AbstractDao<Object, ?> dao;
    /** Entity, Iterable<Entity>, Entity[], or Runnable. */
    final Object parameter;
    final int flags;

    volatile long timeStarted;
    volatile long timeCompleted;
    volatile boolean completed;
    volatile Throwable throwable;
    volatile Object result;

    AsyncOperation(OperationType type, AbstractDao<?, ?> dao, Object parameter) {
        this(type, dao, parameter, 0);
    }

    @SuppressWarnings("unchecked")
    AsyncOperation(OperationType type, AbstractDao<?, ?> dao, Object parameter, int flags) {
        this.type = type;
        this.flags = flags;
        this.dao = (AbstractDao<Object, ?>) dao;
        this.parameter = parameter;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public OperationType getType() {
        return type;
    }

    public Object getParameter() {
        return parameter;
    }

    /**
     * If the operation has a result AND the operation is completed ({@link #isCompleted()}), the result will be
     * available here.
     */
    public Object getResult() {
        return result;
    }

    /** @return true if this operation may be merged with others into a single database transaction. */
    public boolean isMergeTx() {
        return (flags & FLAG_MERGE_TX) != 0;
    }

    /**
     * @return true if this operation is mergeable with the given operation. Checks for null, {@link #FLAG_MERGE_TX},
     *         and if the database instances match.
     */
    boolean isMergeableWith(AsyncOperation other) {
        return other != null && isMergeTx() && other.isMergeTx() && dao.getDatabase() == other.dao.getDatabase();
    }

    public long getTimeStarted() {
        return timeStarted;
    }

    public long getTimeCompleted() {
        return timeCompleted;
    }

    public long getDuration() {
        if (timeCompleted == 0) {
            throw new DaoException("This operation did not yet complete");
        } else {
            return timeCompleted - timeStarted;
        }
    }

    public boolean isFailed() {
        return throwable != null;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isCompletedSucessfully() {
        return completed && throwable == null;
    }

    /** Reset to prepare another execution run. */
    void reset() {
        timeStarted = 0;
        timeCompleted = 0;
        completed = false;
        throwable = null;
        result = null;
    }

}