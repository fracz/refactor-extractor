package im.actor.model;

/**
 * Created by ex3ndr on 16.02.15.
 */
public interface MainThreadProvider {
    public void runOnUiThread(Runnable runnable);

    public boolean isMainThread();

    public boolean isSingleThread();
}