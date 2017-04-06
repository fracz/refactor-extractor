commit ed7d84ca5f74ad63bf57e9a86e2fd63a202cd7e9
Author: Nik Everett <nik9000@gmail.com>
Date:   Wed Jul 29 12:02:46 2015 -0400

    Core: Improve toString on EsThreadPoolExecutor

    Improving the toString allows for nicer error reporting. Also cleaned up
    the way that EsRejectedExecutionException notices that it was rejected
    from a shutdown thread pool. I left javadocs about how its not 100% correct
    but good enough for most uses.

    The improved toString on EsThreadPoolExecutor mean every one of them needs
    a name. In most cases the name to use is obvious. In tests I use the name
    of the test method and in real thread pools I use the name of the thread
    pool. In non-ThreadPool executors I use the thread's name.

    Closes #9732