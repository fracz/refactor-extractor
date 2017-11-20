package im.actor.model.util;

/**
 * Created by ex3ndr on 09.02.15.
 */
public abstract class ThreadLocalCompat<T> {
    public abstract T get();

    public abstract void set(T v);

    public abstract void remove();
}