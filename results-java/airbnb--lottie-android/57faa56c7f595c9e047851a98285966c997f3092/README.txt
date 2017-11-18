commit 57faa56c7f595c9e047851a98285966c997f3092
Author: Gabriel Peal <gpeal@users.noreply.github.com>
Date:   Mon Dec 5 13:50:31 2016 -0800

    Major refactor of observables and parenting (#18)

    Removed Obervable completely in favor of animation listeners. Previously, a single observable was used in the model layer which prevented a Composition from being reused since the Observable would be reused too... This should allow Composition caching.

    As part of the change, I redid how parenting works and removed the need for ParentLayer or ChildContainerLayers which should be a big win for simplicity.

    Also created some better toString methods.

    TODO: the end of LoopPlayOnce is broken. It will be fixed in a subsequent PR.