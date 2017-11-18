package jet;

public final class LongRange implements Range<Long>, LongIterable, JetObject {
    private final static TypeInfo typeInfo = TypeInfo.getTypeInfo(IntRange.class, false);

    private final long start;
    private final long count;

    public LongRange(long startValue, long count) {
        this.start = startValue;
        this.count = count;
    }

    public LongRange(long startValue, long count, boolean reversed) {
        this(startValue, reversed ? -count : count);
    }

    public LongRange(int startValue, int count, boolean reversed, int defaultMask) {
        this(startValue, reversed ? -count : count, (defaultMask & 4) == 0);
    }

    @Override
    public boolean contains(Long item) {
        if (item == null) return false;
        if (count >= 0) {
            return item >= start && item < start + count;
        }
        return item <= start && item > start + count;
    }

    public boolean getIsReversed() {
        return count < 0;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return start+count-1;
    }

    public long getSize() {
        return count < 0 ? -count : count;
    }

    public LongRange minus() {
        return new LongRange(getEnd(), -count);
    }

    @Override
    public LongIterator iterator() {
        return new MyIterator(start, count);
    }

    @Override
    public TypeInfo<?> getTypeInfo() {
        return typeInfo;
    }

    @Override
    public JetObject getOuterObject() {
        return null;
    }

    public static IntRange count(int length) {
        return new IntRange(0, length);
    }

    public static IntRange rangeTo(int from, int to) {
        if(from > to) {
            return new IntRange(to, from-to+1, true);
        }
        else {
            return new IntRange(from, to-from+1);
        }
    }

    private static class MyIterator extends LongIterator {
        private final static TypeInfo typeInfo = TypeInfo.getTypeInfo(MyIterator.class, false);

        private long cur;
        private long count;

        private final boolean reversed;

        public MyIterator(long startValue, long count) {
            cur = startValue;
            reversed = count < 0;
            this.count = reversed ? -count : count;
        }

        @Override
        public boolean getHasNext() {
            return count > 0;
        }

        @Override
        public long nextLong() {
            count--;
            if(reversed) {
                return cur--;
            }
            else {
                return cur++;
            }
        }

        @Override
        public TypeInfo<?> getTypeInfo() {
            return typeInfo;
        }

        @Override
        public JetObject getOuterObject() {
            return null;
        }
    }
}