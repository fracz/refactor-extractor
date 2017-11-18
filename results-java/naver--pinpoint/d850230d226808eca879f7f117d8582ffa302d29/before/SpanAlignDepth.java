package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.bo.SpanEventBo;

public class SpanAlignDepth {
    private static final int DEFAULT_DEPTH = 0;
    private static final int PARENT_DEPTH = -1;

    private Depth align;
    private Depth async;
    private Depth sync;

    public SpanAlignDepth(final int depth) {
        if (depth < DEFAULT_DEPTH) {
            throw new IllegalArgumentException("invalid depth. depth=" + depth);
        }

        align = new Depth(depth);
        sync = new Depth(depth);
        async = new Depth(depth);
    }

    public int getCurrentDepth() {
        return align.current;
    }

    public boolean isParentMissing(final SpanEventBo spanEventBo) {
        if(isAsyncEvent(spanEventBo)) {
            return async.missing;
        }
        return sync.missing;
    }

    public boolean findMissing(final SpanEventBo spanEventBo) {
        final int sequence = spanEventBo.getSequence();
        if (isAsyncEvent(spanEventBo)) {
            if (async.first) {
                if (sequence != 0) {
                    return true;
                }
            } else {
                if (sequence != 0 && sequence > async.sequence + 1) {
                    return true;
                }
            }
        } else {
            if (sequence > sync.sequence + 1) {
                return true;
            }
        }

        return false;
    }

    public int getMissingDepth(final SpanEventBo spanEventBo) {
        if (isAsyncEvent(spanEventBo)) {
            if (isFirstEvent(spanEventBo) || async.first) {
                // start async event.
                async.parent = align.parent + align.current;
                async.first = false;
            }

            if (isSameParent(spanEventBo)) {
                if (async.parent == 0) {
                    async.current = 1;
                }
            } else {
                int depth = spanEventBo.getDepth();
                if (depth > async.current + 1) {
                    // rebalance.
                    depth = async.current + 1;
                }
                async.current = async.parent + depth;
            }
            async.missing = true;
            align.current = async.current;
        } else {
            if (isSameParent(spanEventBo)) {
                if (align.current == 0) {
                    sync.current = 1;
                }
            } else {
                int depth = spanEventBo.getDepth();
                if (depth > sync.current + 1) {
                    // rebalance.
                    depth = sync.current + 1;
                }
                sync.current = sync.parent + depth;
            }
            sync.missing = true;
            align.current = sync.current;
        }

        return align.current;

    }

    public int getDepth(final SpanEventBo spanEventBo) {
        if (isAsyncEvent(spanEventBo)) {
            if (isFirstEvent(spanEventBo) || async.first) {
                // start async event.
                async.parent = align.parent + align.current;
                async.first = false;
            }

            if (!isSameParent(spanEventBo)) {
                int depth = spanEventBo.getDepth();
                async.current = async.parent + depth;
            }
            align.current = async.current;
        } else {
            if (!isSameParent(spanEventBo)) {
                int depth = spanEventBo.getDepth();
                sync.current = sync.parent + depth;
            }
            align.current = sync.current;
        }

        return align.current;
    }

    private boolean isFirstEvent(final SpanEventBo spanEventBo) {
        return spanEventBo.getSequence() == 0;
    }

    private boolean isSameParent(final SpanEventBo spanEventBo) {
        return spanEventBo.getDepth() == PARENT_DEPTH;
    }

    private boolean isAsyncEvent(final SpanEventBo spanEventBo) {
        return spanEventBo.getAsyncId() != -1;
    }

    private class Depth {
        boolean first = true;
        int parent;
        int current;
        int sequence;
        boolean missing = false;

        public Depth(int depth) {
            parent = depth;
            current = depth;
        }
    }
}