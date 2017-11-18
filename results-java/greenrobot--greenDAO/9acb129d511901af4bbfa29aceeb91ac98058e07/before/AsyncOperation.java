package de.greenrobot.dao;

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

    public AsyncOperation(OperationType type, AbstractDao<?, ?> dao, Object parameter) {
        this(type, dao, parameter, 0);
    }

    @SuppressWarnings("unchecked")
    public AsyncOperation(OperationType type, AbstractDao<?, ?> dao, Object parameter, int flags) {
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

    public Object getResult() {
        return result;
    }

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