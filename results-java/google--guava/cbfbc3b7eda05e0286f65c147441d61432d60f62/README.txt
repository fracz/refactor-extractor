commit cbfbc3b7eda05e0286f65c147441d61432d60f62
Author: Charles Fry <fry@google.com>
Date:   Wed Nov 9 09:56:30 2011 -0500

    Javadoc improvements:
    - Emphasize that an empty SetMultimap and an empty ListMultimap are equal.
    - ListenableFuture listeners can also run in the thread that cancels the future (hat tip to <https://plus.google.com/114212977194801425439/posts/eFxZv6ibkM9>, which tipped me off to this by describing a related bug)... plus other minor improvements.
    - ListeningExecutorService can throw RejectedExecutionException.
    - Subclasses of AbstractListeningExecutorService must implement execute() and the shutdown/termination methods.
    Also, fix a comment in AbstractListeningExecutorServiceTest.
    -------------
    Created by MOE: http://code.google.com/p/moe-java
    MOE_MIGRATED_REVID=25284749