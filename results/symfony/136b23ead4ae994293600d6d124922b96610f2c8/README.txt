commit 136b23ead4ae994293600d6d124922b96610f2c8
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Fri Mar 18 09:01:19 2011 +0100

    [EventDispatcher] refactored the code

     * The array returned by getListeners() now removes the listener hash as the key (as this is an implementation detail)
     * The sort method now guarantees that a listener registered before another will stay in the same order even for the same priority (for BC)
     * Made various optimizations