package org.hanuna.gitalk.common.generatemodel;

import org.hanuna.gitalk.common.CacheGet;
import org.hanuna.gitalk.common.Get;
import org.hanuna.gitalk.common.generatemodel.generator.Generator;
import org.hanuna.gitalk.common.readonly.ReadOnlyList;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * @author erokhins
 * postion is position in CompressedList
 * index is index positionElement in positionItems
 */
public class RuntimeGenerateCompressedList<T> implements CompressedList<T> {
    private final CacheGet<Integer, T> cache = new CacheGet<Integer, T>(new Get<Integer, T>() {
        @Override
        public T get(Integer key) {
            return RuntimeGenerateCompressedList.this.get(key);
        }
    }, 100);

    private final Generator<T> generator;
    private final int intervalSave;
    private final List<PositionItem> positionItems = new ArrayList<PositionItem>();
    private int size;

    public RuntimeGenerateCompressedList(Generator<T> generator, T firstT, int size, int intervalSave) {
        this.generator = generator;
        this.intervalSave = intervalSave;
        this.size = size;
        positionItems.add(new PositionItem(0, firstT));
        int curPosition = intervalSave;
        T prevT = firstT;
        while (curPosition < size) {
            prevT = generator.generate(prevT, intervalSave);
            positionItems.add(new PositionItem(curPosition, prevT));
            curPosition = curPosition + intervalSave;
        }
    }

    public RuntimeGenerateCompressedList(Generator<T> generator, T firstT, int size) {
        this(generator, firstT, size, 20);
    }

    private int binarySearch(int position) {
        assert positionItems.size() > 0;
        int x = 0;
        int y = positionItems.size() - 1;
        while (y - x > 1) {
            int z = (x + y) / 2;
            if (positionItems.get(z).getPosition() <= position) {
                x = z;
            } else {
                y = z;
            }
        }
        if (positionItems.get(y).getPosition() <= position) {
            return y;
        }
        return x;
    }

    @NotNull
    @Override
    public ReadOnlyList<T> getList() {
        return ReadOnlyList.newReadOnlyList(new AbstractList<T>() {
            @Override
            public T get(int index) {
                return cache.get(index);
            }

            @Override
            public int size() {
                return size;
            }
        });
    }


    private void fixPositionsTail(int startIndex, int deltaPosition) {
        for (int i = startIndex; i < positionItems.size(); i++) {
            PositionItem positionItem = positionItems.get(i);
            positionItem.setPosition(positionItem.getPosition() + deltaPosition);
        }
    }

    private List<PositionItem> regenerateMediate(PositionItem prevSavePositionItem, int downSavePosition) {
        List<PositionItem> mediateSave = new ArrayList<PositionItem>();
        T prevT = prevSavePositionItem.getT();
        int curTPosition = prevSavePositionItem.getPosition() + intervalSave;

        while (curTPosition < downSavePosition - intervalSave) {
            prevT = generator.generate(prevT, intervalSave);
            mediateSave.add(new PositionItem(curTPosition, prevT));
            curTPosition = curTPosition + intervalSave;
        }
        return mediateSave;
    }

    @Override
    public void recalculate(@NotNull Replace replace) {
        cache.clear();
        int deltaPosition = replace.addElementsCount() - replace.removeElementsCount();
        int upSaveIndex = binarySearch(replace.from());
        PositionItem upSavePositionItem = positionItems.get(upSaveIndex);

        int downSaveIndex = upSaveIndex;
        while (downSaveIndex < positionItems.size() && positionItems.get(downSaveIndex).getPosition() < replace.to()) {
            downSaveIndex++;
        }

        size = size + deltaPosition;
        fixPositionsTail(downSaveIndex, deltaPosition);

        int downSavePosition = size;
        if (downSaveIndex < positionItems.size()) {
            downSavePosition = positionItems.get(downSaveIndex).getPosition();
        }
        List<PositionItem> mediate = regenerateMediate(upSavePositionItem, downSavePosition);

        positionItems.subList(upSaveIndex + 1, downSaveIndex).clear();
        positionItems.addAll(upSaveIndex + 1, mediate);
    }

    private T get(int position) {
        if (position < 0 || position >= size) {
            throw new IllegalArgumentException();
        }
        int saveIndex = binarySearch(position);
        final PositionItem positionItem = positionItems.get(saveIndex);
        assert position >= positionItem.getPosition();
        return generator.generate(positionItem.getT(), position - positionItem.getPosition());
    }

    private class PositionItem {
        private int position;
        private final T t;

        private PositionItem(int position, T t) {
            this.position = position;
            this.t = t;
        }

        public int getPosition() {
            return position;
        }

        public T getT() {
            return t;
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }

}