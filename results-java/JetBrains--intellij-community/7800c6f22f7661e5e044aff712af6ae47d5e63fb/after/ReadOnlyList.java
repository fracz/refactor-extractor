package org.hanuna.gitalk.common;

/**
 * @author erokhins
 */
public interface ReadOnlyList<T> extends Iterable<T>  {
    public int size();
    public T get(int index);
}