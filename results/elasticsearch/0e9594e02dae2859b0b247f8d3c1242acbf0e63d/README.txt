commit 0e9594e02dae2859b0b247f8d3c1242acbf0e63d
Author: javanna <cavannaluca@gmail.com>
Date:   Thu Jul 24 17:25:50 2014 +0200

    Internal: use AtomicInteger instead of volatile int for the current action filter position

    Also improved filter chain tests to not rely on execution time, and made filter chain tests look more similar to what happens in reality by removing multiple threads creation in testTooManyContinueProcessing (something we don't support anyway, makes little sense to test it).

    Closes #7021